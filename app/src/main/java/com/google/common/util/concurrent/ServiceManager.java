package com.google.common.util.concurrent;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.base.Stopwatch;
import com.google.common.base.Supplier;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenerCallQueue;
import com.google.common.util.concurrent.Monitor;
import com.google.common.util.concurrent.Service;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

@Beta
public final class ServiceManager {
    /* access modifiers changed from: private */
    public static final ListenerCallQueue.Callback<Listener> HEALTHY_CALLBACK = new ListenerCallQueue.Callback<Listener>("healthy()") {
        /* access modifiers changed from: package-private */
        public void call(Listener listener) {
            listener.healthy();
        }
    };
    /* access modifiers changed from: private */
    public static final ListenerCallQueue.Callback<Listener> STOPPED_CALLBACK = new ListenerCallQueue.Callback<Listener>("stopped()") {
        /* access modifiers changed from: package-private */
        public void call(Listener listener) {
            listener.stopped();
        }
    };
    /* access modifiers changed from: private */
    public static final Logger logger = Logger.getLogger(ServiceManager.class.getName());
    private final ImmutableList<Service> services;
    private final ServiceManagerState state;

    @Beta
    public static abstract class Listener {
        public void healthy() {
        }

        public void stopped() {
        }

        public void failure(Service service) {
        }
    }

    public ServiceManager(Iterable<? extends Service> services2) {
        ImmutableList<Service> copy = ImmutableList.copyOf(services2);
        if (copy.isEmpty()) {
            logger.log(Level.WARNING, "ServiceManager configured with no services.  Is your application configured properly?", new EmptyServiceManagerWarning((EmptyServiceManagerWarning) null));
            copy = ImmutableList.of(new NoOpService((NoOpService) null));
        }
        this.state = new ServiceManagerState(copy);
        this.services = copy;
        WeakReference<ServiceManagerState> stateReference = new WeakReference<>(this.state);
        for (Service service : copy) {
            service.addListener(new ServiceListener(service, stateReference), MoreExecutors.directExecutor());
            Preconditions.checkArgument(service.state() == Service.State.NEW, "Can only manage NEW services, %s", service);
        }
        this.state.markReady();
    }

    public void addListener(Listener listener, Executor executor) {
        this.state.addListener(listener, executor);
    }

    public void addListener(Listener listener) {
        this.state.addListener(listener, MoreExecutors.directExecutor());
    }

    public ServiceManager startAsync() {
        for (Service service : this.services) {
            Service.State state2 = service.state();
            Preconditions.checkState(state2 == Service.State.NEW, "Service %s is %s, cannot start it.", service, state2);
        }
        for (Service service2 : this.services) {
            try {
                this.state.tryStartTiming(service2);
                service2.startAsync();
            } catch (IllegalStateException e) {
                logger.log(Level.WARNING, "Unable to start Service " + service2, e);
            }
        }
        return this;
    }

    public void awaitHealthy() {
        this.state.awaitHealthy();
    }

    public void awaitHealthy(long timeout, TimeUnit unit) throws TimeoutException {
        this.state.awaitHealthy(timeout, unit);
    }

    public ServiceManager stopAsync() {
        for (Service service : this.services) {
            service.stopAsync();
        }
        return this;
    }

    public void awaitStopped() {
        this.state.awaitStopped();
    }

    public void awaitStopped(long timeout, TimeUnit unit) throws TimeoutException {
        this.state.awaitStopped(timeout, unit);
    }

    public boolean isHealthy() {
        for (Service service : this.services) {
            if (!service.isRunning()) {
                return false;
            }
        }
        return true;
    }

    public ImmutableMultimap<Service.State, Service> servicesByState() {
        return this.state.servicesByState();
    }

    public ImmutableMap<Service, Long> startupTimes() {
        return this.state.startupTimes();
    }

    public String toString() {
        return MoreObjects.toStringHelper((Class<?>) ServiceManager.class).add("services", (Object) Collections2.filter(this.services, Predicates.not(Predicates.instanceOf(NoOpService.class)))).toString();
    }

    private static final class ServiceManagerState {
        final Monitor.Guard awaitHealthGuard = new Monitor.Guard(this.monitor) {
            public boolean isSatisfied() {
                if (ServiceManagerState.this.states.count(Service.State.RUNNING) == ServiceManagerState.this.numberOfServices || ServiceManagerState.this.states.contains(Service.State.STOPPING) || ServiceManagerState.this.states.contains(Service.State.TERMINATED)) {
                    return true;
                }
                return ServiceManagerState.this.states.contains(Service.State.FAILED);
            }
        };
        @GuardedBy("monitor")
        final List<ListenerCallQueue<Listener>> listeners = Collections.synchronizedList(new ArrayList());
        final Monitor monitor = new Monitor();
        final int numberOfServices;
        @GuardedBy("monitor")
        boolean ready;
        @GuardedBy("monitor")
        final SetMultimap<Service.State, Service> servicesByState = Multimaps.newSetMultimap(new EnumMap(Service.State.class), new Supplier<Set<Service>>() {
            public Set<Service> get() {
                return Sets.newLinkedHashSet();
            }
        });
        @GuardedBy("monitor")
        final Map<Service, Stopwatch> startupTimers = Maps.newIdentityHashMap();
        @GuardedBy("monitor")
        final Multiset<Service.State> states = this.servicesByState.keys();
        final Monitor.Guard stoppedGuard = new Monitor.Guard(this.monitor) {
            public boolean isSatisfied() {
                return ServiceManagerState.this.states.count(Service.State.TERMINATED) + ServiceManagerState.this.states.count(Service.State.FAILED) == ServiceManagerState.this.numberOfServices;
            }
        };
        @GuardedBy("monitor")
        boolean transitioned;

        ServiceManagerState(ImmutableCollection<Service> services) {
            this.numberOfServices = services.size();
            this.servicesByState.putAll(Service.State.NEW, services);
        }

        /* access modifiers changed from: package-private */
        public void tryStartTiming(Service service) {
            this.monitor.enter();
            try {
                if (this.startupTimers.get(service) == null) {
                    this.startupTimers.put(service, Stopwatch.createStarted());
                }
            } finally {
                this.monitor.leave();
            }
        }

        /* access modifiers changed from: package-private */
        public void markReady() {
            this.monitor.enter();
            try {
                if (!this.transitioned) {
                    this.ready = true;
                    return;
                }
                List<Service> servicesInBadStates = Lists.newArrayList();
                for (Service service : servicesByState().values()) {
                    if (service.state() != Service.State.NEW) {
                        servicesInBadStates.add(service);
                    }
                }
                throw new IllegalArgumentException("Services started transitioning asynchronously before the ServiceManager was constructed: " + servicesInBadStates);
            } finally {
                this.monitor.leave();
            }
        }

        /* access modifiers changed from: package-private */
        public void addListener(Listener listener, Executor executor) {
            Preconditions.checkNotNull(listener, "listener");
            Preconditions.checkNotNull(executor, "executor");
            this.monitor.enter();
            try {
                if (!this.stoppedGuard.isSatisfied()) {
                    this.listeners.add(new ListenerCallQueue(listener, executor));
                }
            } finally {
                this.monitor.leave();
            }
        }

        /* access modifiers changed from: package-private */
        public void awaitHealthy() {
            this.monitor.enterWhenUninterruptibly(this.awaitHealthGuard);
            try {
                checkHealthy();
            } finally {
                this.monitor.leave();
            }
        }

        /* access modifiers changed from: package-private */
        public void awaitHealthy(long timeout, TimeUnit unit) throws TimeoutException {
            this.monitor.enter();
            try {
                if (!this.monitor.waitForUninterruptibly(this.awaitHealthGuard, timeout, unit)) {
                    throw new TimeoutException("Timeout waiting for the services to become healthy. The following services have not started: " + Multimaps.filterKeys(this.servicesByState, Predicates.in(ImmutableSet.of(Service.State.NEW, Service.State.STARTING))));
                }
                checkHealthy();
            } finally {
                this.monitor.leave();
            }
        }

        /* access modifiers changed from: package-private */
        public void awaitStopped() {
            this.monitor.enterWhenUninterruptibly(this.stoppedGuard);
            this.monitor.leave();
        }

        /* access modifiers changed from: package-private */
        public void awaitStopped(long timeout, TimeUnit unit) throws TimeoutException {
            this.monitor.enter();
            try {
                if (!this.monitor.waitForUninterruptibly(this.stoppedGuard, timeout, unit)) {
                    throw new TimeoutException("Timeout waiting for the services to stop. The following services have not stopped: " + Multimaps.filterKeys(this.servicesByState, Predicates.not(Predicates.in(ImmutableSet.of(Service.State.TERMINATED, Service.State.FAILED)))));
                }
            } finally {
                this.monitor.leave();
            }
        }

        /* JADX INFO: finally extract failed */
        /* access modifiers changed from: package-private */
        public ImmutableMultimap<Service.State, Service> servicesByState() {
            ImmutableSetMultimap.Builder<Service.State, Service> builder = ImmutableSetMultimap.builder();
            this.monitor.enter();
            try {
                for (Map.Entry<Service.State, Service> entry : this.servicesByState.entries()) {
                    if (!(entry.getValue() instanceof NoOpService)) {
                        builder.put((Object) entry.getKey(), (Object) entry.getValue());
                    }
                }
                this.monitor.leave();
                return builder.build();
            } catch (Throwable th) {
                this.monitor.leave();
                throw th;
            }
        }

        /* JADX INFO: finally extract failed */
        /* access modifiers changed from: package-private */
        public ImmutableMap<Service, Long> startupTimes() {
            this.monitor.enter();
            try {
                List<Map.Entry<Service, Long>> loadTimes = Lists.newArrayListWithCapacity(this.startupTimers.size());
                for (Map.Entry<Service, Stopwatch> entry : this.startupTimers.entrySet()) {
                    Service service = entry.getKey();
                    Stopwatch stopWatch = entry.getValue();
                    if (!stopWatch.isRunning() && (!(service instanceof NoOpService))) {
                        loadTimes.add(Maps.immutableEntry(service, Long.valueOf(stopWatch.elapsed(TimeUnit.MILLISECONDS))));
                    }
                }
                this.monitor.leave();
                Collections.sort(loadTimes, Ordering.natural().onResultOf(new Function<Map.Entry<Service, Long>, Long>() {
                    public Long apply(Map.Entry<Service, Long> input) {
                        return input.getValue();
                    }
                }));
                ImmutableMap.Builder<Service, Long> builder = ImmutableMap.builder();
                for (Map.Entry<Service, Long> entry2 : loadTimes) {
                    builder.put(entry2);
                }
                return builder.build();
            } catch (Throwable th) {
                this.monitor.leave();
                throw th;
            }
        }

        /* access modifiers changed from: package-private */
        public void transitionService(Service service, Service.State from, Service.State to) {
            boolean z = true;
            Preconditions.checkNotNull(service);
            if (from == to) {
                z = false;
            }
            Preconditions.checkArgument(z);
            this.monitor.enter();
            try {
                this.transitioned = true;
                if (this.ready) {
                    Preconditions.checkState(this.servicesByState.remove(from, service), "Service %s not at the expected location in the state map %s", service, from);
                    Preconditions.checkState(this.servicesByState.put(to, service), "Service %s in the state map unexpectedly at %s", service, to);
                    Stopwatch stopwatch = this.startupTimers.get(service);
                    if (stopwatch == null) {
                        stopwatch = Stopwatch.createStarted();
                        this.startupTimers.put(service, stopwatch);
                    }
                    if (to.compareTo(Service.State.RUNNING) >= 0 && stopwatch.isRunning()) {
                        stopwatch.stop();
                        if (!(service instanceof NoOpService)) {
                            ServiceManager.logger.log(Level.FINE, "Started {0} in {1}.", new Object[]{service, stopwatch});
                        }
                    }
                    if (to == Service.State.FAILED) {
                        fireFailedListeners(service);
                    }
                    if (this.states.count(Service.State.RUNNING) == this.numberOfServices) {
                        fireHealthyListeners();
                    } else if (this.states.count(Service.State.TERMINATED) + this.states.count(Service.State.FAILED) == this.numberOfServices) {
                        fireStoppedListeners();
                    }
                    this.monitor.leave();
                    executeListeners();
                }
            } finally {
                this.monitor.leave();
                executeListeners();
            }
        }

        /* access modifiers changed from: package-private */
        @GuardedBy("monitor")
        public void fireStoppedListeners() {
            ServiceManager.STOPPED_CALLBACK.enqueueOn(this.listeners);
        }

        /* access modifiers changed from: package-private */
        @GuardedBy("monitor")
        public void fireHealthyListeners() {
            ServiceManager.HEALTHY_CALLBACK.enqueueOn(this.listeners);
        }

        /* access modifiers changed from: package-private */
        @GuardedBy("monitor")
        public void fireFailedListeners(final Service service) {
            new ListenerCallQueue.Callback<Listener>("failed({service=" + service + "})") {
                /* access modifiers changed from: package-private */
                public void call(Listener listener) {
                    listener.failure(service);
                }
            }.enqueueOn(this.listeners);
        }

        /* access modifiers changed from: package-private */
        public void executeListeners() {
            Preconditions.checkState(!this.monitor.isOccupiedByCurrentThread(), "It is incorrect to execute listeners with the monitor held.");
            for (int i = 0; i < this.listeners.size(); i++) {
                this.listeners.get(i).execute();
            }
        }

        /* access modifiers changed from: package-private */
        @GuardedBy("monitor")
        public void checkHealthy() {
            if (this.states.count(Service.State.RUNNING) != this.numberOfServices) {
                throw new IllegalStateException("Expected to be healthy after starting. The following services are not running: " + Multimaps.filterKeys(this.servicesByState, Predicates.not(Predicates.equalTo(Service.State.RUNNING))));
            }
        }
    }

    private static final class ServiceListener extends Service.Listener {
        final Service service;
        final WeakReference<ServiceManagerState> state;

        ServiceListener(Service service2, WeakReference<ServiceManagerState> state2) {
            this.service = service2;
            this.state = state2;
        }

        public void starting() {
            ServiceManagerState state2 = (ServiceManagerState) this.state.get();
            if (state2 != null) {
                state2.transitionService(this.service, Service.State.NEW, Service.State.STARTING);
                if (!(this.service instanceof NoOpService)) {
                    ServiceManager.logger.log(Level.FINE, "Starting {0}.", this.service);
                }
            }
        }

        public void running() {
            ServiceManagerState state2 = (ServiceManagerState) this.state.get();
            if (state2 != null) {
                state2.transitionService(this.service, Service.State.STARTING, Service.State.RUNNING);
            }
        }

        public void stopping(Service.State from) {
            ServiceManagerState state2 = (ServiceManagerState) this.state.get();
            if (state2 != null) {
                state2.transitionService(this.service, from, Service.State.STOPPING);
            }
        }

        public void terminated(Service.State from) {
            ServiceManagerState state2 = (ServiceManagerState) this.state.get();
            if (state2 != null) {
                if (!(this.service instanceof NoOpService)) {
                    ServiceManager.logger.log(Level.FINE, "Service {0} has terminated. Previous state was: {1}", new Object[]{this.service, from});
                }
                state2.transitionService(this.service, from, Service.State.TERMINATED);
            }
        }

        public void failed(Service.State from, Throwable failure) {
            ServiceManagerState state2 = (ServiceManagerState) this.state.get();
            if (state2 != null) {
                if (!(this.service instanceof NoOpService)) {
                    ServiceManager.logger.log(Level.SEVERE, "Service " + this.service + " has failed in the " + from + " state.", failure);
                }
                state2.transitionService(this.service, from, Service.State.FAILED);
            }
        }
    }

    private static final class NoOpService extends AbstractService {
        /* synthetic */ NoOpService(NoOpService noOpService) {
            this();
        }

        private NoOpService() {
        }

        /* access modifiers changed from: protected */
        public void doStart() {
            notifyStarted();
        }

        /* access modifiers changed from: protected */
        public void doStop() {
            notifyStopped();
        }
    }

    private static final class EmptyServiceManagerWarning extends Throwable {
        /* synthetic */ EmptyServiceManagerWarning(EmptyServiceManagerWarning emptyServiceManagerWarning) {
            this();
        }

        private EmptyServiceManagerWarning() {
        }
    }
}
