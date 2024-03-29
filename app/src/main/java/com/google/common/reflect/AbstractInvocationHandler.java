package com.google.common.reflect;

import com.google.common.annotations.Beta;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import javax.annotation.Nullable;

@Beta
public abstract class AbstractInvocationHandler implements InvocationHandler {
    private static final Object[] NO_ARGS = new Object[0];

    /* access modifiers changed from: protected */
    public abstract Object handleInvocation(Object obj, Method method, Object[] objArr) throws Throwable;

    public final Object invoke(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
        boolean z = false;
        if (args == null) {
            args = NO_ARGS;
        }
        if (args.length == 0 && method.getName().equals("hashCode")) {
            return Integer.valueOf(hashCode());
        }
        if (args.length == 1 && method.getName().equals("equals") && method.getParameterTypes()[0] == Object.class) {
            Object arg = args[0];
            if (arg == null) {
                return false;
            }
            if (proxy == arg) {
                return true;
            }
            if (isProxyOfSameInterfaces(arg, proxy.getClass())) {
                z = equals(Proxy.getInvocationHandler(arg));
            }
            return Boolean.valueOf(z);
        } else if (args.length != 0 || !method.getName().equals("toString")) {
            return handleInvocation(proxy, method, args);
        } else {
            return toString();
        }
    }

    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String toString() {
        return super.toString();
    }

    private static boolean isProxyOfSameInterfaces(Object arg, Class<?> proxyClass) {
        if (proxyClass.isInstance(arg)) {
            return true;
        }
        if (Proxy.isProxyClass(arg.getClass())) {
            return Arrays.equals(arg.getClass().getInterfaces(), proxyClass.getInterfaces());
        }
        return false;
    }
}
