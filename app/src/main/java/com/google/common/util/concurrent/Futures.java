package com.google.common.util.concurrent;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

@Beta
public final class Futures {
    private static final AsyncFunction<ListenableFuture<Object>, Object> DEREFERENCER = new AsyncFunction<ListenableFuture<Object>, Object>() {
        public ListenableFuture<Object> apply(ListenableFuture<Object> input) {
            return input;
        }
    };
    private static final Ordering<Constructor<?>> WITH_STRING_PARAM_FIRST = Ordering.natural().onResultOf(new Function<Constructor<?>, Boolean>() {
        public Boolean apply(Constructor<?> input) {
            return Boolean.valueOf(Arrays.asList(input.getParameterTypes()).contains(String.class));
        }
    }).reverse();

    private interface FutureCombiner<V, C> {
        C combine(List<Optional<V>> list);
    }

    private Futures() {
    }

    public static <V, X extends Exception> CheckedFuture<V, X> makeChecked(ListenableFuture<V> future, Function<? super Exception, X> mapper) {
        return new MappingCheckedFuture((ListenableFuture) Preconditions.checkNotNull(future), mapper);
    }

    private static abstract class ImmediateFuture<V> implements ListenableFuture<V> {
        private static final Logger log = Logger.getLogger(ImmediateFuture.class.getName());

        /* synthetic */ ImmediateFuture(ImmediateFuture immediateFuture) {
            this();
        }

        public abstract V get() throws ExecutionException;

        private ImmediateFuture() {
        }

        public void addListener(Runnable listener, Executor executor) {
            Preconditions.checkNotNull(listener, "Runnable was null.");
            Preconditions.checkNotNull(executor, "Executor was null.");
            try {
                executor.execute(listener);
            } catch (RuntimeException e) {
                log.log(Level.SEVERE, "RuntimeException while executing runnable " + listener + " with executor " + executor, e);
            }
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        public V get(long timeout, TimeUnit unit) throws ExecutionException {
            Preconditions.checkNotNull(unit);
            return get();
        }

        public boolean isCancelled() {
            return false;
        }

        public boolean isDone() {
            return true;
        }
    }

    private static class ImmediateSuccessfulFuture<V> extends ImmediateFuture<V> {
        @Nullable
        private final V value;

        ImmediateSuccessfulFuture(@Nullable V value2) {
            super((ImmediateFuture) null);
            this.value = value2;
        }

        public V get() {
            return this.value;
        }
    }

    private static class ImmediateSuccessfulCheckedFuture<V, X extends Exception> extends ImmediateFuture<V> implements CheckedFuture<V, X> {
        @Nullable
        private final V value;

        ImmediateSuccessfulCheckedFuture(@Nullable V value2) {
            super((ImmediateFuture) null);
            this.value = value2;
        }

        public V get() {
            return this.value;
        }

        public V checkedGet() {
            return this.value;
        }

        public V checkedGet(long timeout, TimeUnit unit) {
            Preconditions.checkNotNull(unit);
            return this.value;
        }
    }

    private static class ImmediateFailedFuture<V> extends ImmediateFuture<V> {
        private final Throwable thrown;

        ImmediateFailedFuture(Throwable thrown2) {
            super((ImmediateFuture) null);
            this.thrown = thrown2;
        }

        public V get() throws ExecutionException {
            throw new ExecutionException(this.thrown);
        }
    }

    private static class ImmediateCancelledFuture<V> extends ImmediateFuture<V> {
        private final CancellationException thrown = new CancellationException("Immediate cancelled future.");

        ImmediateCancelledFuture() {
            super((ImmediateFuture) null);
        }

        public boolean isCancelled() {
            return true;
        }

        public V get() {
            throw AbstractFuture.cancellationExceptionWithCause("Task was cancelled.", this.thrown);
        }
    }

    private static class ImmediateFailedCheckedFuture<V, X extends Exception> extends ImmediateFuture<V> implements CheckedFuture<V, X> {
        private final X thrown;

        ImmediateFailedCheckedFuture(X thrown2) {
            super((ImmediateFuture) null);
            this.thrown = thrown2;
        }

        public V get() throws ExecutionException {
            throw new ExecutionException(this.thrown);
        }

        public V checkedGet() throws Exception {
            throw this.thrown;
        }

        public V checkedGet(long timeout, TimeUnit unit) throws Exception {
            Preconditions.checkNotNull(unit);
            throw this.thrown;
        }
    }

    public static <V> ListenableFuture<V> immediateFuture(@Nullable V value) {
        return new ImmediateSuccessfulFuture(value);
    }

    public static <V, X extends Exception> CheckedFuture<V, X> immediateCheckedFuture(@Nullable V value) {
        return new ImmediateSuccessfulCheckedFuture(value);
    }

    public static <V> ListenableFuture<V> immediateFailedFuture(Throwable throwable) {
        Preconditions.checkNotNull(throwable);
        return new ImmediateFailedFuture(throwable);
    }

    public static <V> ListenableFuture<V> immediateCancelledFuture() {
        return new ImmediateCancelledFuture();
    }

    public static <V, X extends Exception> CheckedFuture<V, X> immediateFailedCheckedFuture(X exception) {
        Preconditions.checkNotNull(exception);
        return new ImmediateFailedCheckedFuture(exception);
    }

    public static <V> ListenableFuture<V> withFallback(ListenableFuture<? extends V> input, FutureFallback<? extends V> fallback) {
        return withFallback(input, fallback, MoreExecutors.directExecutor());
    }

    public static <V> ListenableFuture<V> withFallback(ListenableFuture<? extends V> input, FutureFallback<? extends V> fallback, Executor executor) {
        Preconditions.checkNotNull(fallback);
        return new FallbackFuture(input, fallback, executor);
    }

    private static class FallbackFuture<V> extends AbstractFuture<V> {
        /* access modifiers changed from: private */
        public volatile ListenableFuture<? extends V> running;

        FallbackFuture(ListenableFuture<? extends V> input, final FutureFallback<? extends V> fallback, Executor executor) {
            this.running = input;
            Futures.addCallback(this.running, new FutureCallback<V>() {
                public void onSuccess(V value) {
                    FallbackFuture.this.set(value);
                }

                public void onFailure(Throwable t) {
                    if (!FallbackFuture.this.isCancelled()) {
                        try {
                            ListenableFuture unused = FallbackFuture.this.running = fallback.create(t);
                            if (FallbackFuture.this.isCancelled()) {
                                FallbackFuture.this.running.cancel(FallbackFuture.this.wasInterrupted());
                            } else {
                                Futures.addCallback(FallbackFuture.this.running, new FutureCallback<V>() {
                                    public void onSuccess(V value) {
                                        FallbackFuture.this.set(value);
                                    }

                                    public void onFailure(Throwable t) {
                                        if (FallbackFuture.this.running.isCancelled()) {
                                            FallbackFuture.this.cancel(false);
                                        } else {
                                            FallbackFuture.this.setException(t);
                                        }
                                    }
                                }, MoreExecutors.directExecutor());
                            }
                        } catch (Throwable e) {
                            FallbackFuture.this.setException(e);
                        }
                    }
                }
            }, executor);
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            if (!super.cancel(mayInterruptIfRunning)) {
                return false;
            }
            this.running.cancel(mayInterruptIfRunning);
            return true;
        }
    }

    public static <I, O> ListenableFuture<O> transform(ListenableFuture<I> input, AsyncFunction<? super I, ? extends O> function) {
        ChainingListenableFuture<I, O> output = new ChainingListenableFuture<>(function, input, (ChainingListenableFuture) null);
        input.addListener(output, MoreExecutors.directExecutor());
        return output;
    }

    public static <I, O> ListenableFuture<O> transform(ListenableFuture<I> input, AsyncFunction<? super I, ? extends O> function, Executor executor) {
        Preconditions.checkNotNull(executor);
        ChainingListenableFuture<I, O> output = new ChainingListenableFuture<>(function, input, (ChainingListenableFuture) null);
        input.addListener(rejectionPropagatingRunnable(output, output, executor), MoreExecutors.directExecutor());
        return output;
    }

    private static Runnable rejectionPropagatingRunnable(final AbstractFuture<?> outputFuture, final Runnable delegateTask, final Executor delegateExecutor) {
        return new Runnable() {
            public void run() {
                final AtomicBoolean thrownFromDelegate = new AtomicBoolean(true);
                try {
                    Executor executor = delegateExecutor;
                    final Runnable runnable = delegateTask;
                    executor.execute(new Runnable() {
                        public void run() {
                            thrownFromDelegate.set(false);
                            runnable.run();
                        }
                    });
                } catch (RejectedExecutionException e) {
                    if (thrownFromDelegate.get()) {
                        outputFuture.setException(e);
                    }
                }
            }
        };
    }

    public static <I, O> ListenableFuture<O> transform(ListenableFuture<I> input, Function<? super I, ? extends O> function) {
        Preconditions.checkNotNull(function);
        ChainingListenableFuture<I, O> output = new ChainingListenableFuture<>(asAsyncFunction(function), input, (ChainingListenableFuture) null);
        input.addListener(output, MoreExecutors.directExecutor());
        return output;
    }

    public static <I, O> ListenableFuture<O> transform(ListenableFuture<I> input, Function<? super I, ? extends O> function, Executor executor) {
        Preconditions.checkNotNull(function);
        return transform(input, asAsyncFunction(function), executor);
    }

    private static <I, O> AsyncFunction<I, O> asAsyncFunction(final Function<? super I, ? extends O> function) {
        return new AsyncFunction<I, O>() {
            public ListenableFuture<O> apply(I input) {
                return Futures.immediateFuture(function.apply(input));
            }
        };
    }

    public static <I, O> Future<O> lazyTransform(final Future<I> input, final Function<? super I, ? extends O> function) {
        Preconditions.checkNotNull(input);
        Preconditions.checkNotNull(function);
        return new Future<O>() {
            public boolean cancel(boolean mayInterruptIfRunning) {
                return input.cancel(mayInterruptIfRunning);
            }

            public boolean isCancelled() {
                return input.isCancelled();
            }

            public boolean isDone() {
                return input.isDone();
            }

            public O get() throws InterruptedException, ExecutionException {
                return applyTransformation(input.get());
            }

            public O get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return applyTransformation(input.get(timeout, unit));
            }

            private O applyTransformation(I input) throws ExecutionException {
                try {
                    return function.apply(input);
                } catch (Throwable t) {
                    throw new ExecutionException(t);
                }
            }
        };
    }

    private static class ChainingListenableFuture<I, O> extends AbstractFuture<O> implements Runnable {
        private AsyncFunction<? super I, ? extends O> function;
        private ListenableFuture<? extends I> inputFuture;
        /* access modifiers changed from: private */
        public volatile ListenableFuture<? extends O> outputFuture;

        /* synthetic */ ChainingListenableFuture(AsyncFunction function2, ListenableFuture inputFuture2, ChainingListenableFuture chainingListenableFuture) {
            this(function2, inputFuture2);
        }

        private ChainingListenableFuture(AsyncFunction<? super I, ? extends O> function2, ListenableFuture<? extends I> inputFuture2) {
            this.function = (AsyncFunction) Preconditions.checkNotNull(function2);
            this.inputFuture = (ListenableFuture) Preconditions.checkNotNull(inputFuture2);
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            if (!super.cancel(mayInterruptIfRunning)) {
                return false;
            }
            cancel(this.inputFuture, mayInterruptIfRunning);
            cancel(this.outputFuture, mayInterruptIfRunning);
            return true;
        }

        private void cancel(@Nullable Future<?> future, boolean mayInterruptIfRunning) {
            if (future != null) {
                future.cancel(mayInterruptIfRunning);
            }
        }

        public void run() {
            try {
                try {
                    final ListenableFuture<? extends O> outputFuture2 = (ListenableFuture) Preconditions.checkNotNull(this.function.apply(Uninterruptibles.getUninterruptibly(this.inputFuture)), "AsyncFunction may not return null.");
                    this.outputFuture = outputFuture2;
                    if (isCancelled()) {
                        outputFuture2.cancel(wasInterrupted());
                        this.outputFuture = null;
                        this.function = null;
                        this.inputFuture = null;
                        return;
                    }
                    outputFuture2.addListener(new Runnable() {
                        public void run() {
                            try {
                                ChainingListenableFuture.this.set(Uninterruptibles.getUninterruptibly(outputFuture2));
                            } catch (CancellationException e) {
                                ChainingListenableFuture.this.cancel(false);
                            } catch (ExecutionException e2) {
                                ChainingListenableFuture.this.setException(e2.getCause());
                            } finally {
                                ListenableFuture unused = ChainingListenableFuture.this.outputFuture = null;
                            }
                        }
                    }, MoreExecutors.directExecutor());
                    this.function = null;
                    this.inputFuture = null;
                } catch (UndeclaredThrowableException e) {
                    setException(e.getCause());
                    this.function = null;
                } catch (Throwable th) {
                    this.function = null;
                    this.inputFuture = null;
                    throw th;
                }
            } catch (CancellationException e2) {
                cancel(false);
                this.function = null;
                this.inputFuture = null;
            } catch (ExecutionException e3) {
                setException(e3.getCause());
                this.function = null;
                this.inputFuture = null;
            }
        }
    }

    public static <V> ListenableFuture<V> dereference(ListenableFuture<? extends ListenableFuture<? extends V>> nested) {
        return transform(nested, DEREFERENCER);
    }

    @Beta
    public static <V> ListenableFuture<List<V>> allAsList(ListenableFuture<? extends V>... futures) {
        return listFuture(ImmutableList.copyOf((E[]) futures), true, MoreExecutors.directExecutor());
    }

    @Beta
    public static <V> ListenableFuture<List<V>> allAsList(Iterable<? extends ListenableFuture<? extends V>> futures) {
        return listFuture(ImmutableList.copyOf(futures), true, MoreExecutors.directExecutor());
    }

    private static final class WrappedCombiner<T> implements Callable<T> {
        final Callable<T> delegate;
        CombinerFuture<T> outputFuture;

        WrappedCombiner(Callable<T> delegate2) {
            this.delegate = (Callable) Preconditions.checkNotNull(delegate2);
        }

        public T call() throws Exception {
            try {
                return this.delegate.call();
            } catch (ExecutionException e) {
                this.outputFuture.setException(e.getCause());
                return null;
            } catch (CancellationException e2) {
                this.outputFuture.cancel(false);
                return null;
            }
        }
    }

    private static final class CombinerFuture<V> extends ListenableFutureTask<V> {
        ImmutableList<ListenableFuture<?>> futures;

        CombinerFuture(Callable<V> callable, ImmutableList<ListenableFuture<?>> futures2) {
            super(callable);
            this.futures = futures2;
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            ImmutableList<ListenableFuture<?>> futures2 = this.futures;
            if (!super.cancel(mayInterruptIfRunning)) {
                return false;
            }
            for (ListenableFuture<?> future : futures2) {
                future.cancel(mayInterruptIfRunning);
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public void done() {
            super.done();
            this.futures = null;
        }

        /* access modifiers changed from: protected */
        public void setException(Throwable t) {
            super.setException(t);
        }
    }

    public static <V> ListenableFuture<V> nonCancellationPropagating(ListenableFuture<V> future) {
        return new NonCancellationPropagatingFuture(future);
    }

    private static class NonCancellationPropagatingFuture<V> extends AbstractFuture<V> {
        NonCancellationPropagatingFuture(final ListenableFuture<V> delegate) {
            Preconditions.checkNotNull(delegate);
            Futures.addCallback(delegate, new FutureCallback<V>() {
                public void onSuccess(V result) {
                    NonCancellationPropagatingFuture.this.set(result);
                }

                public void onFailure(Throwable t) {
                    if (delegate.isCancelled()) {
                        NonCancellationPropagatingFuture.this.cancel(false);
                    } else {
                        NonCancellationPropagatingFuture.this.setException(t);
                    }
                }
            }, MoreExecutors.directExecutor());
        }
    }

    @Beta
    public static <V> ListenableFuture<List<V>> successfulAsList(ListenableFuture<? extends V>... futures) {
        return listFuture(ImmutableList.copyOf((E[]) futures), false, MoreExecutors.directExecutor());
    }

    @Beta
    public static <V> ListenableFuture<List<V>> successfulAsList(Iterable<? extends ListenableFuture<? extends V>> futures) {
        return listFuture(ImmutableList.copyOf(futures), false, MoreExecutors.directExecutor());
    }

    @Beta
    public static <T> ImmutableList<ListenableFuture<T>> inCompletionOrder(Iterable<? extends ListenableFuture<? extends T>> futures) {
        final ConcurrentLinkedQueue<AsyncSettableFuture<T>> delegates = Queues.newConcurrentLinkedQueue();
        ImmutableList.Builder<ListenableFuture<T>> listBuilder = ImmutableList.builder();
        SerializingExecutor executor = new SerializingExecutor(MoreExecutors.directExecutor());
        for (final ListenableFuture<? extends T> future : futures) {
            AsyncSettableFuture<T> delegate = AsyncSettableFuture.create();
            delegates.add(delegate);
            future.addListener(new Runnable() {
                public void run() {
                    ((AsyncSettableFuture) delegates.remove()).setFuture(future);
                }
            }, executor);
            listBuilder.add((Object) delegate);
        }
        return listBuilder.build();
    }

    public static <V> void addCallback(ListenableFuture<V> future, FutureCallback<? super V> callback) {
        addCallback(future, callback, MoreExecutors.directExecutor());
    }

    public static <V> void addCallback(final ListenableFuture<V> future, final FutureCallback<? super V> callback, Executor executor) {
        Preconditions.checkNotNull(callback);
        future.addListener(new Runnable() {
            public void run() {
                try {
                    callback.onSuccess(Uninterruptibles.getUninterruptibly(future));
                } catch (ExecutionException e) {
                    callback.onFailure(e.getCause());
                } catch (RuntimeException e2) {
                    callback.onFailure(e2);
                } catch (Error e3) {
                    callback.onFailure(e3);
                }
            }
        }, executor);
    }

    public static <V, X extends Exception> V get(Future<V> future, Class<X> exceptionClass) throws Exception {
        Preconditions.checkNotNull(future);
        Preconditions.checkArgument(!RuntimeException.class.isAssignableFrom(exceptionClass), "Futures.get exception type (%s) must not be a RuntimeException", exceptionClass);
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw newWithCause(exceptionClass, e);
        } catch (ExecutionException e2) {
            wrapAndThrowExceptionOrError(e2.getCause(), exceptionClass);
            throw new AssertionError();
        }
    }

    public static <V, X extends Exception> V get(Future<V> future, long timeout, TimeUnit unit, Class<X> exceptionClass) throws Exception {
        Preconditions.checkNotNull(future);
        Preconditions.checkNotNull(unit);
        Preconditions.checkArgument(!RuntimeException.class.isAssignableFrom(exceptionClass), "Futures.get exception type (%s) must not be a RuntimeException", exceptionClass);
        try {
            return future.get(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw newWithCause(exceptionClass, e);
        } catch (TimeoutException e2) {
            throw newWithCause(exceptionClass, e2);
        } catch (ExecutionException e3) {
            wrapAndThrowExceptionOrError(e3.getCause(), exceptionClass);
            throw new AssertionError();
        }
    }

    private static <X extends Exception> void wrapAndThrowExceptionOrError(Throwable cause, Class<X> exceptionClass) throws Exception {
        if (cause instanceof Error) {
            throw new ExecutionError((Error) cause);
        } else if (cause instanceof RuntimeException) {
            throw new UncheckedExecutionException(cause);
        } else {
            throw newWithCause(exceptionClass, cause);
        }
    }

    public static <V> V getUnchecked(Future<V> future) {
        Preconditions.checkNotNull(future);
        try {
            return Uninterruptibles.getUninterruptibly(future);
        } catch (ExecutionException e) {
            wrapAndThrowUnchecked(e.getCause());
            throw new AssertionError();
        }
    }

    private static void wrapAndThrowUnchecked(Throwable cause) {
        if (cause instanceof Error) {
            throw new ExecutionError((Error) cause);
        }
        throw new UncheckedExecutionException(cause);
    }

    private static <X extends Exception> X newWithCause(Class<X> exceptionClass, Throwable cause) {
        for (Constructor<X> constructor : preferringStrings(Arrays.asList(exceptionClass.getConstructors()))) {
            X instance = (Exception) newFromConstructor(constructor, cause);
            if (instance != null) {
                if (instance.getCause() == null) {
                    instance.initCause(cause);
                }
                return instance;
            }
        }
        throw new IllegalArgumentException("No appropriate constructor for exception of type " + exceptionClass + " in response to chained exception", cause);
    }

    private static <X extends Exception> List<Constructor<X>> preferringStrings(List<Constructor<X>> constructors) {
        return WITH_STRING_PARAM_FIRST.sortedCopy(constructors);
    }

    @Nullable
    private static <X> X newFromConstructor(Constructor<X> constructor, Throwable cause) {
        Class<?>[] paramTypes = constructor.getParameterTypes();
        Object[] params = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            if (paramType.equals(String.class)) {
                params[i] = cause.toString();
            } else if (!paramType.equals(Throwable.class)) {
                return null;
            } else {
                params[i] = cause;
            }
        }
        try {
            return constructor.newInstance(params);
        } catch (IllegalArgumentException e) {
            return null;
        } catch (InstantiationException e2) {
            return null;
        } catch (IllegalAccessException e3) {
            return null;
        } catch (InvocationTargetException e4) {
            return null;
        }
    }

    private static class CombinedFuture<V, C> extends AbstractFuture<C> {
        private static final Logger logger = Logger.getLogger(CombinedFuture.class.getName());
        final boolean allMustSucceed;
        FutureCombiner<V, C> combiner;
        ImmutableCollection<? extends ListenableFuture<? extends V>> futures;
        final AtomicInteger remaining;
        Set<Throwable> seenExceptions;
        final Object seenExceptionsLock = new Object();
        List<Optional<V>> values;

        CombinedFuture(ImmutableCollection<? extends ListenableFuture<? extends V>> futures2, boolean allMustSucceed2, Executor listenerExecutor, FutureCombiner<V, C> combiner2) {
            this.futures = futures2;
            this.allMustSucceed = allMustSucceed2;
            this.remaining = new AtomicInteger(futures2.size());
            this.combiner = combiner2;
            this.values = Lists.newArrayListWithCapacity(futures2.size());
            init(listenerExecutor);
        }

        /* access modifiers changed from: protected */
        public void init(Executor listenerExecutor) {
            addListener(new Runnable() {
                public void run() {
                    if (CombinedFuture.this.isCancelled()) {
                        for (ListenableFuture<?> future : CombinedFuture.this.futures) {
                            future.cancel(CombinedFuture.this.wasInterrupted());
                        }
                    }
                    CombinedFuture.this.futures = null;
                    CombinedFuture.this.values = null;
                    CombinedFuture.this.combiner = null;
                }
            }, MoreExecutors.directExecutor());
            if (this.futures.isEmpty()) {
                set(this.combiner.combine(ImmutableList.of()));
                return;
            }
            for (int i = 0; i < this.futures.size(); i++) {
                this.values.add((Object) null);
            }
            final int i2 = 0;
            for (final ListenableFuture<? extends V> listenable : this.futures) {
                int i3 = i2;
                listenable.addListener(new Runnable() {
                    public void run() {
                        CombinedFuture.this.setOneValue(i2, listenable);
                    }
                }, listenerExecutor);
                i2++;
            }
        }

        private void setExceptionAndMaybeLog(Throwable throwable) {
            boolean visibleFromOutputFuture = false;
            boolean firstTimeSeeingThisException = true;
            if (this.allMustSucceed) {
                visibleFromOutputFuture = super.setException(throwable);
                synchronized (this.seenExceptionsLock) {
                    if (this.seenExceptions == null) {
                        this.seenExceptions = Sets.newHashSet();
                    }
                    firstTimeSeeingThisException = this.seenExceptions.add(throwable);
                }
            }
            if ((throwable instanceof Error) || (this.allMustSucceed && (!visibleFromOutputFuture) && firstTimeSeeingThisException)) {
                logger.log(Level.SEVERE, "input future failed.", throwable);
            }
        }

        /* access modifiers changed from: private */
        public void setOneValue(int index, Future<? extends V> future) {
            boolean z;
            boolean z2 = true;
            List<Optional<V>> localValues = this.values;
            if (isDone() || localValues == null) {
                if (!this.allMustSucceed) {
                    z = isCancelled();
                } else {
                    z = true;
                }
                Preconditions.checkState(z, "Future was done before all dependencies completed");
            }
            try {
                Preconditions.checkState(future.isDone(), "Tried to set value from future which is not done");
                V returnValue = Uninterruptibles.getUninterruptibly(future);
                if (localValues != null) {
                    localValues.set(index, Optional.fromNullable(returnValue));
                }
                int newRemaining = this.remaining.decrementAndGet();
                if (newRemaining < 0) {
                    z2 = false;
                }
                Preconditions.checkState(z2, "Less than 0 remaining futures");
                if (newRemaining == 0) {
                    FutureCombiner<V, C> localCombiner = this.combiner;
                    if (localCombiner == null || localValues == null) {
                        Preconditions.checkState(isDone());
                    } else {
                        set(localCombiner.combine(localValues));
                    }
                }
            } catch (CancellationException e) {
                if (this.allMustSucceed) {
                    cancel(false);
                }
                int newRemaining2 = this.remaining.decrementAndGet();
                if (newRemaining2 < 0) {
                    z2 = false;
                }
                Preconditions.checkState(z2, "Less than 0 remaining futures");
                if (newRemaining2 == 0) {
                    FutureCombiner<V, C> localCombiner2 = this.combiner;
                    if (localCombiner2 == null || localValues == null) {
                        Preconditions.checkState(isDone());
                    } else {
                        set(localCombiner2.combine(localValues));
                    }
                }
            } catch (ExecutionException e2) {
                setExceptionAndMaybeLog(e2.getCause());
                int newRemaining3 = this.remaining.decrementAndGet();
                if (newRemaining3 < 0) {
                    z2 = false;
                }
                Preconditions.checkState(z2, "Less than 0 remaining futures");
                if (newRemaining3 == 0) {
                    FutureCombiner<V, C> localCombiner3 = this.combiner;
                    if (localCombiner3 == null || localValues == null) {
                        Preconditions.checkState(isDone());
                    } else {
                        set(localCombiner3.combine(localValues));
                    }
                }
            } catch (Throwable th) {
                int newRemaining4 = this.remaining.decrementAndGet();
                if (newRemaining4 < 0) {
                    z2 = false;
                }
                Preconditions.checkState(z2, "Less than 0 remaining futures");
                if (newRemaining4 == 0) {
                    FutureCombiner<V, C> localCombiner4 = this.combiner;
                    if (localCombiner4 == null || localValues == null) {
                        Preconditions.checkState(isDone());
                    } else {
                        set(localCombiner4.combine(localValues));
                    }
                }
                throw th;
            }
        }
    }

    private static <V> ListenableFuture<List<V>> listFuture(ImmutableList<ListenableFuture<? extends V>> futures, boolean allMustSucceed, Executor listenerExecutor) {
        return new CombinedFuture(futures, allMustSucceed, listenerExecutor, new FutureCombiner<V, List<V>>() {
            public List<V> combine(List<Optional<V>> values) {
                List<V> result = Lists.newArrayList();
                for (Optional<V> element : values) {
                    result.add(element != null ? element.orNull() : null);
                }
                return Collections.unmodifiableList(result);
            }
        });
    }

    private static class MappingCheckedFuture<V, X extends Exception> extends AbstractCheckedFuture<V, X> {
        final Function<? super Exception, X> mapper;

        MappingCheckedFuture(ListenableFuture<V> delegate, Function<? super Exception, X> mapper2) {
            super(delegate);
            this.mapper = (Function) Preconditions.checkNotNull(mapper2);
        }

        /* access modifiers changed from: protected */
        public X mapException(Exception e) {
            return (Exception) this.mapper.apply(e);
        }
    }
}
