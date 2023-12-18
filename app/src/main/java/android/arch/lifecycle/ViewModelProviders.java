package android.arch.lifecycle;

import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import java.lang.reflect.InvocationTargetException;

public class ViewModelProviders {
    @SuppressLint({"StaticFieldLeak"})
    private static DefaultFactory sDefaultFactory;

    private static void initializeFactoryIfNeeded(Application application) {
        if (sDefaultFactory == null) {
            sDefaultFactory = new DefaultFactory(application);
        }
    }

    @MainThread
    public static ViewModelProvider of(@NonNull Fragment fragment) {
        FragmentActivity activity = fragment.getActivity();
        if (activity != null) {
            initializeFactoryIfNeeded(activity.getApplication());
            return new ViewModelProvider(ViewModelStores.of(fragment), (ViewModelProvider.Factory) sDefaultFactory);
        }
        throw new IllegalArgumentException("Can't create ViewModelProvider for detached fragment");
    }

    @MainThread
    public static ViewModelProvider of(@NonNull FragmentActivity activity) {
        initializeFactoryIfNeeded(activity.getApplication());
        return new ViewModelProvider(ViewModelStores.of(activity), (ViewModelProvider.Factory) sDefaultFactory);
    }

    @MainThread
    public static ViewModelProvider of(@NonNull Fragment fragment, @NonNull ViewModelProvider.Factory factory) {
        return new ViewModelProvider(ViewModelStores.of(fragment), factory);
    }

    @MainThread
    public static ViewModelProvider of(@NonNull FragmentActivity activity, @NonNull ViewModelProvider.Factory factory) {
        return new ViewModelProvider(ViewModelStores.of(activity), factory);
    }

    public static class DefaultFactory extends ViewModelProvider.NewInstanceFactory {
        private Application mApplication;

        public DefaultFactory(@NonNull Application application) {
            this.mApplication = application;
        }

        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (!AndroidViewModel.class.isAssignableFrom(modelClass)) {
                return super.create(modelClass);
            }
            try {
                return (ViewModel) modelClass.getConstructor(new Class[]{Application.class}).newInstance(new Object[]{this.mApplication});
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            } catch (IllegalAccessException e2) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e2);
            } catch (InstantiationException e3) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e3);
            } catch (InvocationTargetException e4) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e4);
            }
        }
    }
}
