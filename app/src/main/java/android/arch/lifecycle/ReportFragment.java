package android.arch.lifecycle;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.arch.lifecycle.Lifecycle;
import android.os.Bundle;
import android.support.annotation.RestrictTo;

@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public class ReportFragment extends Fragment {
    private static final String REPORT_FRAGMENT_TAG = "android.arch.lifecycle.LifecycleDispatcher.report_fragment_tag";
    private ActivityInitializationListener mProcessListener;

    interface ActivityInitializationListener {
        void onCreate();

        void onResume();

        void onStart();
    }

    public static void injectIfNeededIn(Activity activity) {
        FragmentManager manager = activity.getFragmentManager();
        if (manager.findFragmentByTag(REPORT_FRAGMENT_TAG) == null) {
            manager.beginTransaction().add(new ReportFragment(), REPORT_FRAGMENT_TAG).commit();
            manager.executePendingTransactions();
        }
    }

    static ReportFragment get(Activity activity) {
        return (ReportFragment) activity.getFragmentManager().findFragmentByTag(REPORT_FRAGMENT_TAG);
    }

    private void dispatchCreate(ActivityInitializationListener listener) {
        if (listener != null) {
            listener.onCreate();
        }
    }

    private void dispatchStart(ActivityInitializationListener listener) {
        if (listener != null) {
            listener.onStart();
        }
    }

    private void dispatchResume(ActivityInitializationListener listener) {
        if (listener != null) {
            listener.onResume();
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dispatchCreate(this.mProcessListener);
        dispatch(Lifecycle.Event.ON_CREATE);
    }

    public void onStart() {
        super.onStart();
        dispatchStart(this.mProcessListener);
        dispatch(Lifecycle.Event.ON_START);
    }

    public void onResume() {
        super.onResume();
        dispatchResume(this.mProcessListener);
        dispatch(Lifecycle.Event.ON_RESUME);
    }

    public void onPause() {
        super.onPause();
        dispatch(Lifecycle.Event.ON_PAUSE);
    }

    public void onStop() {
        super.onStop();
        dispatch(Lifecycle.Event.ON_STOP);
    }

    public void onDestroy() {
        super.onDestroy();
        dispatch(Lifecycle.Event.ON_DESTROY);
        this.mProcessListener = null;
    }

    private void dispatch(Lifecycle.Event event) {
        if (getActivity() instanceof LifecycleRegistryOwner) {
            ((LifecycleRegistryOwner) getActivity()).getLifecycle().handleLifecycleEvent(event);
        }
    }

    /* access modifiers changed from: package-private */
    public void setProcessListener(ActivityInitializationListener processListener) {
        this.mProcessListener = processListener;
    }
}
