package android.support.v4.app;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.ArraySet;
import android.support.v4.util.DebugUtils;
import android.support.v4.util.LogWriter;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/* compiled from: FragmentManager */
final class FragmentManagerImpl extends FragmentManager implements LayoutInflater.Factory2 {
    static final Interpolator ACCELERATE_CUBIC = new AccelerateInterpolator(1.5f);
    static final Interpolator ACCELERATE_QUINT = new AccelerateInterpolator(2.5f);
    static final int ANIM_DUR = 220;
    public static final int ANIM_STYLE_CLOSE_ENTER = 3;
    public static final int ANIM_STYLE_CLOSE_EXIT = 4;
    public static final int ANIM_STYLE_FADE_ENTER = 5;
    public static final int ANIM_STYLE_FADE_EXIT = 6;
    public static final int ANIM_STYLE_OPEN_ENTER = 1;
    public static final int ANIM_STYLE_OPEN_EXIT = 2;
    static boolean DEBUG = false;
    static final Interpolator DECELERATE_CUBIC = new DecelerateInterpolator(1.5f);
    static final Interpolator DECELERATE_QUINT = new DecelerateInterpolator(2.5f);
    static final String TAG = "FragmentManager";
    static final String TARGET_REQUEST_CODE_STATE_TAG = "android:target_req_state";
    static final String TARGET_STATE_TAG = "android:target_state";
    static final String USER_VISIBLE_HINT_TAG = "android:user_visible_hint";
    static final String VIEW_STATE_TAG = "android:view_state";
    static Field sAnimationListenerField = null;
    SparseArray<Fragment> mActive;
    final ArrayList<Fragment> mAdded = new ArrayList<>();
    ArrayList<Integer> mAvailBackStackIndices;
    ArrayList<BackStackRecord> mBackStack;
    ArrayList<FragmentManager.OnBackStackChangedListener> mBackStackChangeListeners;
    ArrayList<BackStackRecord> mBackStackIndices;
    FragmentContainer mContainer;
    ArrayList<Fragment> mCreatedMenus;
    int mCurState = 0;
    boolean mDestroyed;
    Runnable mExecCommit = new Runnable() {
        public void run() {
            FragmentManagerImpl.this.execPendingActions();
        }
    };
    boolean mExecutingActions;
    boolean mHavePendingDeferredStart;
    FragmentHostCallback mHost;
    private final CopyOnWriteArrayList<Pair<FragmentManager.FragmentLifecycleCallbacks, Boolean>> mLifecycleCallbacks = new CopyOnWriteArrayList<>();
    boolean mNeedMenuInvalidate;
    int mNextFragmentIndex = 0;
    String mNoTransactionsBecause;
    Fragment mParent;
    ArrayList<OpGenerator> mPendingActions;
    ArrayList<StartEnterTransitionListener> mPostponedTransactions;
    Fragment mPrimaryNav;
    FragmentManagerNonConfig mSavedNonConfig;
    SparseArray<Parcelable> mStateArray = null;
    Bundle mStateBundle = null;
    boolean mStateSaved;
    ArrayList<Fragment> mTmpAddedFragments;
    ArrayList<Boolean> mTmpIsPop;
    ArrayList<BackStackRecord> mTmpRecords;

    /* compiled from: FragmentManager */
    interface OpGenerator {
        boolean generateOps(ArrayList<BackStackRecord> arrayList, ArrayList<Boolean> arrayList2);
    }

    FragmentManagerImpl() {
    }

    static boolean modifiesAlpha(AnimationOrAnimator anim) {
        if (anim.animation instanceof AlphaAnimation) {
            return true;
        }
        if (!(anim.animation instanceof AnimationSet)) {
            return modifiesAlpha(anim.animator);
        }
        List<Animation> anims = ((AnimationSet) anim.animation).getAnimations();
        for (int i = 0; i < anims.size(); i++) {
            if (anims.get(i) instanceof AlphaAnimation) {
                return true;
            }
        }
        return false;
    }

    static boolean modifiesAlpha(Animator anim) {
        if (anim == null) {
            return false;
        }
        if (anim instanceof ValueAnimator) {
            PropertyValuesHolder[] values = ((ValueAnimator) anim).getValues();
            for (PropertyValuesHolder propertyName : values) {
                if ("alpha".equals(propertyName.getPropertyName())) {
                    return true;
                }
            }
        } else if (anim instanceof AnimatorSet) {
            List<Animator> animList = ((AnimatorSet) anim).getChildAnimations();
            for (int i = 0; i < animList.size(); i++) {
                if (modifiesAlpha(animList.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean shouldRunOnHWLayer(View v, AnimationOrAnimator anim) {
        if (v == null || anim == null || Build.VERSION.SDK_INT < 19 || v.getLayerType() != 0 || !ViewCompat.hasOverlappingRendering(v)) {
            return false;
        }
        return modifiesAlpha(anim);
    }

    private void throwException(RuntimeException ex) {
        Log.e(TAG, ex.getMessage());
        Log.e(TAG, "Activity state:");
        PrintWriter pw = new PrintWriter(new LogWriter(TAG));
        if (this.mHost != null) {
            try {
                this.mHost.onDump("  ", (FileDescriptor) null, pw, new String[0]);
            } catch (Exception e) {
                Log.e(TAG, "Failed dumping state", e);
            }
        } else {
            try {
                dump("  ", (FileDescriptor) null, pw, new String[0]);
            } catch (Exception e2) {
                Log.e(TAG, "Failed dumping state", e2);
            }
        }
        throw ex;
    }

    public FragmentTransaction beginTransaction() {
        return new BackStackRecord(this);
    }

    public boolean executePendingTransactions() {
        boolean updates = execPendingActions();
        forcePostponedTransactions();
        return updates;
    }

    public void popBackStack() {
        enqueueAction(new PopBackStackState((String) null, -1, 0), false);
    }

    public boolean popBackStackImmediate() {
        checkStateLoss();
        return popBackStackImmediate((String) null, -1, 0);
    }

    public void popBackStack(String name, int flags) {
        enqueueAction(new PopBackStackState(name, -1, flags), false);
    }

    public boolean popBackStackImmediate(String name, int flags) {
        checkStateLoss();
        return popBackStackImmediate(name, -1, flags);
    }

    public void popBackStack(int id, int flags) {
        if (id < 0) {
            throw new IllegalArgumentException("Bad id: " + id);
        }
        enqueueAction(new PopBackStackState((String) null, id, flags), false);
    }

    public boolean popBackStackImmediate(int id, int flags) {
        checkStateLoss();
        execPendingActions();
        if (id >= 0) {
            return popBackStackImmediate((String) null, id, flags);
        }
        throw new IllegalArgumentException("Bad id: " + id);
    }

    private boolean popBackStackImmediate(String name, int id, int flags) {
        FragmentManager childManager;
        execPendingActions();
        ensureExecReady(true);
        if (this.mPrimaryNav != null && id < 0 && name == null && (childManager = this.mPrimaryNav.peekChildFragmentManager()) != null && childManager.popBackStackImmediate()) {
            return true;
        }
        boolean executePop = popBackStackState(this.mTmpRecords, this.mTmpIsPop, name, id, flags);
        if (executePop) {
            this.mExecutingActions = true;
            try {
                removeRedundantOperationsAndExecute(this.mTmpRecords, this.mTmpIsPop);
            } finally {
                cleanupExec();
            }
        }
        doPendingDeferredStart();
        burpActive();
        return executePop;
    }

    public int getBackStackEntryCount() {
        if (this.mBackStack != null) {
            return this.mBackStack.size();
        }
        return 0;
    }

    public FragmentManager.BackStackEntry getBackStackEntryAt(int index) {
        return this.mBackStack.get(index);
    }

    public void addOnBackStackChangedListener(FragmentManager.OnBackStackChangedListener listener) {
        if (this.mBackStackChangeListeners == null) {
            this.mBackStackChangeListeners = new ArrayList<>();
        }
        this.mBackStackChangeListeners.add(listener);
    }

    public void removeOnBackStackChangedListener(FragmentManager.OnBackStackChangedListener listener) {
        if (this.mBackStackChangeListeners != null) {
            this.mBackStackChangeListeners.remove(listener);
        }
    }

    public void putFragment(Bundle bundle, String key, Fragment fragment) {
        if (fragment.mIndex < 0) {
            throwException(new IllegalStateException("Fragment " + fragment + " is not currently in the FragmentManager"));
        }
        bundle.putInt(key, fragment.mIndex);
    }

    public Fragment getFragment(Bundle bundle, String key) {
        int index = bundle.getInt(key, -1);
        if (index == -1) {
            return null;
        }
        Fragment f = this.mActive.get(index);
        if (f == null) {
            throwException(new IllegalStateException("Fragment no longer exists for key " + key + ": index " + index));
        }
        return f;
    }

    public List<Fragment> getFragments() {
        List<Fragment> list;
        if (this.mAdded.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        synchronized (this.mAdded) {
            list = (List) this.mAdded.clone();
        }
        return list;
    }

    /* access modifiers changed from: package-private */
    public List<Fragment> getActiveFragments() {
        if (this.mActive == null) {
            return null;
        }
        int count = this.mActive.size();
        ArrayList<Fragment> fragments = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            fragments.add(this.mActive.valueAt(i));
        }
        return fragments;
    }

    /* access modifiers changed from: package-private */
    public int getActiveFragmentCount() {
        if (this.mActive == null) {
            return 0;
        }
        return this.mActive.size();
    }

    public Fragment.SavedState saveFragmentInstanceState(Fragment fragment) {
        Bundle result;
        if (fragment.mIndex < 0) {
            throwException(new IllegalStateException("Fragment " + fragment + " is not currently in the FragmentManager"));
        }
        if (fragment.mState <= 0 || (result = saveFragmentBasicState(fragment)) == null) {
            return null;
        }
        return new Fragment.SavedState(result);
    }

    public boolean isDestroyed() {
        return this.mDestroyed;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("FragmentManager{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" in ");
        if (this.mParent != null) {
            DebugUtils.buildShortClassTag(this.mParent, sb);
        } else {
            DebugUtils.buildShortClassTag(this.mHost, sb);
        }
        sb.append("}}");
        return sb.toString();
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        int N;
        int N2;
        int N3;
        int N4;
        int N5;
        String innerPrefix = prefix + "    ";
        if (this.mActive != null && (N5 = this.mActive.size()) > 0) {
            writer.print(prefix);
            writer.print("Active Fragments in ");
            writer.print(Integer.toHexString(System.identityHashCode(this)));
            writer.println(":");
            for (int i = 0; i < N5; i++) {
                Fragment f = this.mActive.valueAt(i);
                writer.print(prefix);
                writer.print("  #");
                writer.print(i);
                writer.print(": ");
                writer.println(f);
                if (f != null) {
                    f.dump(innerPrefix, fd, writer, args);
                }
            }
        }
        int N6 = this.mAdded.size();
        if (N6 > 0) {
            writer.print(prefix);
            writer.println("Added Fragments:");
            for (int i2 = 0; i2 < N6; i2++) {
                writer.print(prefix);
                writer.print("  #");
                writer.print(i2);
                writer.print(": ");
                writer.println(this.mAdded.get(i2).toString());
            }
        }
        if (this.mCreatedMenus != null && (N4 = this.mCreatedMenus.size()) > 0) {
            writer.print(prefix);
            writer.println("Fragments Created Menus:");
            for (int i3 = 0; i3 < N4; i3++) {
                writer.print(prefix);
                writer.print("  #");
                writer.print(i3);
                writer.print(": ");
                writer.println(this.mCreatedMenus.get(i3).toString());
            }
        }
        if (this.mBackStack != null && (N3 = this.mBackStack.size()) > 0) {
            writer.print(prefix);
            writer.println("Back Stack:");
            for (int i4 = 0; i4 < N3; i4++) {
                BackStackRecord bs = this.mBackStack.get(i4);
                writer.print(prefix);
                writer.print("  #");
                writer.print(i4);
                writer.print(": ");
                writer.println(bs.toString());
                bs.dump(innerPrefix, fd, writer, args);
            }
        }
        synchronized (this) {
            if (this.mBackStackIndices != null && (N2 = this.mBackStackIndices.size()) > 0) {
                writer.print(prefix);
                writer.println("Back Stack Indices:");
                for (int i5 = 0; i5 < N2; i5++) {
                    writer.print(prefix);
                    writer.print("  #");
                    writer.print(i5);
                    writer.print(": ");
                    writer.println(this.mBackStackIndices.get(i5));
                }
            }
            if (this.mAvailBackStackIndices != null && this.mAvailBackStackIndices.size() > 0) {
                writer.print(prefix);
                writer.print("mAvailBackStackIndices: ");
                writer.println(Arrays.toString(this.mAvailBackStackIndices.toArray()));
            }
        }
        if (this.mPendingActions != null && (N = this.mPendingActions.size()) > 0) {
            writer.print(prefix);
            writer.println("Pending Actions:");
            for (int i6 = 0; i6 < N; i6++) {
                writer.print(prefix);
                writer.print("  #");
                writer.print(i6);
                writer.print(": ");
                writer.println(this.mPendingActions.get(i6));
            }
        }
        writer.print(prefix);
        writer.println("FragmentManager misc state:");
        writer.print(prefix);
        writer.print("  mHost=");
        writer.println(this.mHost);
        writer.print(prefix);
        writer.print("  mContainer=");
        writer.println(this.mContainer);
        if (this.mParent != null) {
            writer.print(prefix);
            writer.print("  mParent=");
            writer.println(this.mParent);
        }
        writer.print(prefix);
        writer.print("  mCurState=");
        writer.print(this.mCurState);
        writer.print(" mStateSaved=");
        writer.print(this.mStateSaved);
        writer.print(" mDestroyed=");
        writer.println(this.mDestroyed);
        if (this.mNeedMenuInvalidate) {
            writer.print(prefix);
            writer.print("  mNeedMenuInvalidate=");
            writer.println(this.mNeedMenuInvalidate);
        }
        if (this.mNoTransactionsBecause != null) {
            writer.print(prefix);
            writer.print("  mNoTransactionsBecause=");
            writer.println(this.mNoTransactionsBecause);
        }
    }

    static AnimationOrAnimator makeOpenCloseAnimation(Context context, float startScale, float endScale, float startAlpha, float endAlpha) {
        AnimationSet set = new AnimationSet(false);
        ScaleAnimation scale = new ScaleAnimation(startScale, endScale, startScale, endScale, 1, 0.5f, 1, 0.5f);
        scale.setInterpolator(DECELERATE_QUINT);
        scale.setDuration(220);
        set.addAnimation(scale);
        AlphaAnimation alpha = new AlphaAnimation(startAlpha, endAlpha);
        alpha.setInterpolator(DECELERATE_CUBIC);
        alpha.setDuration(220);
        set.addAnimation(alpha);
        return new AnimationOrAnimator((Animation) set, (AnimationOrAnimator) null);
    }

    static AnimationOrAnimator makeFadeAnimation(Context context, float start, float end) {
        AlphaAnimation anim = new AlphaAnimation(start, end);
        anim.setInterpolator(DECELERATE_CUBIC);
        anim.setDuration(220);
        return new AnimationOrAnimator((Animation) anim, (AnimationOrAnimator) null);
    }

    /* access modifiers changed from: package-private */
    public AnimationOrAnimator loadAnimation(Fragment fragment, int transit, boolean enter, int transitionStyle) {
        int styleIndex;
        int nextAnim = fragment.getNextAnim();
        Animation animation = fragment.onCreateAnimation(transit, enter, nextAnim);
        if (animation != null) {
            return new AnimationOrAnimator(animation, (AnimationOrAnimator) null);
        }
        Animator animator = fragment.onCreateAnimator(transit, enter, nextAnim);
        if (animator != null) {
            return new AnimationOrAnimator(animator, (AnimationOrAnimator) null);
        }
        if (nextAnim != 0) {
            boolean isAnim = "anim".equals(this.mHost.getContext().getResources().getResourceTypeName(nextAnim));
            boolean successfulLoad = false;
            if (isAnim) {
                try {
                    Animation animation2 = AnimationUtils.loadAnimation(this.mHost.getContext(), nextAnim);
                    if (animation2 != null) {
                        return new AnimationOrAnimator(animation2, (AnimationOrAnimator) null);
                    }
                    successfulLoad = true;
                } catch (Resources.NotFoundException e) {
                    throw e;
                } catch (RuntimeException e2) {
                }
            }
            if (!successfulLoad) {
                try {
                    Animator animator2 = AnimatorInflater.loadAnimator(this.mHost.getContext(), nextAnim);
                    if (animator2 != null) {
                        return new AnimationOrAnimator(animator2, (AnimationOrAnimator) null);
                    }
                } catch (RuntimeException e3) {
                    if (isAnim) {
                        throw e3;
                    }
                    Animation animation3 = AnimationUtils.loadAnimation(this.mHost.getContext(), nextAnim);
                    if (animation3 != null) {
                        return new AnimationOrAnimator(animation3, (AnimationOrAnimator) null);
                    }
                }
            }
        }
        if (transit == 0 || (styleIndex = transitToStyleIndex(transit, enter)) < 0) {
            return null;
        }
        switch (styleIndex) {
            case 1:
                return makeOpenCloseAnimation(this.mHost.getContext(), 1.125f, 1.0f, 0.0f, 1.0f);
            case 2:
                return makeOpenCloseAnimation(this.mHost.getContext(), 1.0f, 0.975f, 1.0f, 0.0f);
            case 3:
                return makeOpenCloseAnimation(this.mHost.getContext(), 0.975f, 1.0f, 0.0f, 1.0f);
            case 4:
                return makeOpenCloseAnimation(this.mHost.getContext(), 1.0f, 1.075f, 1.0f, 0.0f);
            case 5:
                return makeFadeAnimation(this.mHost.getContext(), 0.0f, 1.0f);
            case 6:
                return makeFadeAnimation(this.mHost.getContext(), 1.0f, 0.0f);
            default:
                if (transitionStyle == 0 && this.mHost.onHasWindowAnimations()) {
                    transitionStyle = this.mHost.onGetWindowAnimations();
                }
                if (transitionStyle == 0) {
                    return null;
                }
                return null;
        }
    }

    public void performPendingDeferredStart(Fragment f) {
        if (!f.mDeferStart) {
            return;
        }
        if (this.mExecutingActions) {
            this.mHavePendingDeferredStart = true;
            return;
        }
        f.mDeferStart = false;
        moveToState(f, this.mCurState, 0, 0, false);
    }

    private static void setHWLayerAnimListenerIfAlpha(View v, AnimationOrAnimator anim) {
        if (v != null && anim != null && shouldRunOnHWLayer(v, anim)) {
            if (anim.animator != null) {
                anim.animator.addListener(new AnimatorOnHWLayerIfNeededListener(v));
                return;
            }
            Animation.AnimationListener originalListener = getAnimationListener(anim.animation);
            v.setLayerType(2, (Paint) null);
            anim.animation.setAnimationListener(new AnimateOnHWLayerIfNeededListener(v, originalListener));
        }
    }

    private static Animation.AnimationListener getAnimationListener(Animation animation) {
        try {
            if (sAnimationListenerField == null) {
                sAnimationListenerField = Animation.class.getDeclaredField("mListener");
                sAnimationListenerField.setAccessible(true);
            }
            return (Animation.AnimationListener) sAnimationListenerField.get(animation);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "No field with the name mListener is found in Animation class", e);
            return null;
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "Cannot access Animation's mListener field", e2);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isStateAtLeast(int state) {
        return this.mCurState >= state;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:134:0x037f, code lost:
        r12 = android.support.v4.os.EnvironmentCompat.MEDIA_UNKNOWN;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:143:0x039f, code lost:
        if (r16 >= 1) goto L_0x0070;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:145:0x03a3, code lost:
        if (r14.mDestroyed == false) goto L_0x03b6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:147:0x03a9, code lost:
        if (r15.getAnimatingAway() == null) goto L_0x04d6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:148:0x03ab, code lost:
        r13 = r15.getAnimatingAway();
        r15.setAnimatingAway((android.view.View) null);
        r13.clearAnimation();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:150:0x03ba, code lost:
        if (r15.getAnimatingAway() != null) goto L_0x03c2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:152:0x03c0, code lost:
        if (r15.getAnimator() == null) goto L_0x04e9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:153:0x03c2, code lost:
        r15.setStateAfterAnimating(r16);
        r16 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:161:0x03f6, code lost:
        if (r16 >= 4) goto L_0x041d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:163:0x03fa, code lost:
        if (DEBUG == false) goto L_0x0416;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:164:0x03fc, code lost:
        android.util.Log.v(TAG, "movefrom STARTED: " + r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:165:0x0416, code lost:
        r15.performStop();
        dispatchOnFragmentStopped(r15, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:167:0x0420, code lost:
        if (r16 >= 3) goto L_0x0443;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:169:0x0424, code lost:
        if (DEBUG == false) goto L_0x0440;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:170:0x0426, code lost:
        android.util.Log.v(TAG, "movefrom STOPPED: " + r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:171:0x0440, code lost:
        r15.performReallyStop();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:173:0x0446, code lost:
        if (r16 >= 2) goto L_0x039c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:175:0x044a, code lost:
        if (DEBUG == false) goto L_0x0466;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:176:0x044c, code lost:
        android.util.Log.v(TAG, "movefrom ACTIVITY_CREATED: " + r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:178:0x0468, code lost:
        if (r15.mView == null) goto L_0x0479;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:180:0x0470, code lost:
        if (r14.mHost.onShouldSaveFragmentState(r15) == false) goto L_0x0479;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:182:0x0474, code lost:
        if (r15.mSavedViewState != null) goto L_0x0479;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:183:0x0476, code lost:
        saveFragmentViewState(r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:184:0x0479, code lost:
        r15.performDestroyView();
        dispatchOnFragmentViewDestroyed(r15, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:185:0x0482, code lost:
        if (r15.mView == null) goto L_0x04c8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:187:0x0486, code lost:
        if (r15.mContainer == null) goto L_0x04c8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:188:0x0488, code lost:
        r15.mView.clearAnimation();
        r15.mContainer.endViewTransition(r15.mView);
        r8 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:189:0x0497, code lost:
        if (r14.mCurState <= 0) goto L_0x04b7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:191:0x049d, code lost:
        if ((!r14.mDestroyed) == false) goto L_0x04b7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:193:0x04a5, code lost:
        if (r15.mView.getVisibility() != 0) goto L_0x04b7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:195:0x04ac, code lost:
        if (r15.mPostponedAlpha < 0.0f) goto L_0x04b7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:196:0x04ae, code lost:
        r8 = loadAnimation(r15, r17, false, r18);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:197:0x04b7, code lost:
        r15.mPostponedAlpha = 0.0f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:198:0x04ba, code lost:
        if (r8 == null) goto L_0x04c1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:199:0x04bc, code lost:
        animateRemoveFragment(r15, r8, r16);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:200:0x04c1, code lost:
        r15.mContainer.removeView(r15.mView);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:201:0x04c8, code lost:
        r15.mContainer = null;
        r15.mView = null;
        r15.mInnerView = null;
        r15.mInLayout = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:203:0x04da, code lost:
        if (r15.getAnimator() == null) goto L_0x03b6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:204:0x04dc, code lost:
        r9 = r15.getAnimator();
        r15.setAnimator((android.animation.Animator) null);
        r9.cancel();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:206:0x04eb, code lost:
        if (DEBUG == false) goto L_0x0507;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:207:0x04ed, code lost:
        android.util.Log.v(TAG, "movefrom CREATED: " + r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:209:0x0509, code lost:
        if (r15.mRetaining != false) goto L_0x0524;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:210:0x050b, code lost:
        r15.performDestroy();
        dispatchOnFragmentDestroyed(r15, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:211:0x0512, code lost:
        r15.performDetach();
        dispatchOnFragmentDetached(r15, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:212:0x0519, code lost:
        if (r19 != false) goto L_0x0070;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:214:0x051d, code lost:
        if (r15.mRetaining != false) goto L_0x0528;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:215:0x051f, code lost:
        makeInactive(r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:216:0x0524, code lost:
        r15.mState = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:217:0x0528, code lost:
        r15.mHost = null;
        r15.mParentFragment = null;
        r15.mFragmentManager = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x01f9, code lost:
        ensureInflatedFragmentView(r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x01ff, code lost:
        if (r16 <= 1) goto L_0x0309;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x0203, code lost:
        if (DEBUG == false) goto L_0x021f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0205, code lost:
        android.util.Log.v(TAG, "moveto ACTIVITY_CREATED: " + r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x0221, code lost:
        if (r15.mFromLayout != false) goto L_0x02f2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x0223, code lost:
        r10 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x0226, code lost:
        if (r15.mContainerId == 0) goto L_0x02a4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x022b, code lost:
        if (r15.mContainerId != -1) goto L_0x0250;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x022d, code lost:
        throwException(new java.lang.IllegalArgumentException("Cannot create fragment " + r15 + " for a container view with no id"));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x0250, code lost:
        r10 = (android.view.ViewGroup) r14.mContainer.onFindViewById(r15.mContainerId);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x025a, code lost:
        if (r10 != null) goto L_0x02a4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x0260, code lost:
        if ((!r15.mRestored) == false) goto L_0x02a4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:?, code lost:
        r12 = r15.getResources().getResourceName(r15.mContainerId);
     */
    /* JADX WARNING: Removed duplicated region for block: B:219:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0076  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void moveToState(android.support.v4.app.Fragment r15, int r16, int r17, int r18, boolean r19) {
        /*
            r14 = this;
            boolean r2 = r15.mAdded
            if (r2 == 0) goto L_0x0008
            boolean r2 = r15.mDetached
            if (r2 == 0) goto L_0x000f
        L_0x0008:
            r2 = 1
            r0 = r16
            if (r0 <= r2) goto L_0x000f
            r16 = 1
        L_0x000f:
            boolean r2 = r15.mRemoving
            if (r2 == 0) goto L_0x0025
            int r2 = r15.mState
            r0 = r16
            if (r0 <= r2) goto L_0x0025
            int r2 = r15.mState
            if (r2 != 0) goto L_0x0046
            boolean r2 = r15.isInBackStack()
            if (r2 == 0) goto L_0x0046
            r16 = 1
        L_0x0025:
            boolean r2 = r15.mDeferStart
            if (r2 == 0) goto L_0x0035
            int r2 = r15.mState
            r3 = 4
            if (r2 >= r3) goto L_0x0035
            r2 = 3
            r0 = r16
            if (r0 <= r2) goto L_0x0035
            r16 = 3
        L_0x0035:
            int r2 = r15.mState
            r0 = r16
            if (r2 > r0) goto L_0x038f
            boolean r2 = r15.mFromLayout
            if (r2 == 0) goto L_0x004b
            boolean r2 = r15.mInLayout
            r2 = r2 ^ 1
            if (r2 == 0) goto L_0x004b
            return
        L_0x0046:
            int r0 = r15.mState
            r16 = r0
            goto L_0x0025
        L_0x004b:
            android.view.View r2 = r15.getAnimatingAway()
            if (r2 != 0) goto L_0x0057
            android.animation.Animator r2 = r15.getAnimator()
            if (r2 == 0) goto L_0x006b
        L_0x0057:
            r2 = 0
            r15.setAnimatingAway(r2)
            r2 = 0
            r15.setAnimator(r2)
            int r4 = r15.getStateAfterAnimating()
            r5 = 0
            r6 = 0
            r7 = 1
            r2 = r14
            r3 = r15
            r2.moveToState(r3, r4, r5, r6, r7)
        L_0x006b:
            int r2 = r15.mState
            switch(r2) {
                case 0: goto L_0x00b6;
                case 1: goto L_0x01f9;
                case 2: goto L_0x0309;
                case 3: goto L_0x0311;
                case 4: goto L_0x033b;
                default: goto L_0x0070;
            }
        L_0x0070:
            int r2 = r15.mState
            r0 = r16
            if (r2 == r0) goto L_0x00b5
            java.lang.String r2 = "FragmentManager"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "moveToState: Fragment state for "
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.StringBuilder r3 = r3.append(r15)
            java.lang.String r4 = " not updated inline; "
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.String r4 = "expected state "
            java.lang.StringBuilder r3 = r3.append(r4)
            r0 = r16
            java.lang.StringBuilder r3 = r3.append(r0)
            java.lang.String r4 = " found "
            java.lang.StringBuilder r3 = r3.append(r4)
            int r4 = r15.mState
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.String r3 = r3.toString()
            android.util.Log.w(r2, r3)
            r0 = r16
            r15.mState = r0
        L_0x00b5:
            return
        L_0x00b6:
            if (r16 <= 0) goto L_0x01f9
            boolean r2 = DEBUG
            if (r2 == 0) goto L_0x00d6
            java.lang.String r2 = "FragmentManager"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "moveto CREATED: "
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.StringBuilder r3 = r3.append(r15)
            java.lang.String r3 = r3.toString()
            android.util.Log.v(r2, r3)
        L_0x00d6:
            android.os.Bundle r2 = r15.mSavedFragmentState
            if (r2 == 0) goto L_0x0129
            android.os.Bundle r2 = r15.mSavedFragmentState
            android.support.v4.app.FragmentHostCallback r3 = r14.mHost
            android.content.Context r3 = r3.getContext()
            java.lang.ClassLoader r3 = r3.getClassLoader()
            r2.setClassLoader(r3)
            android.os.Bundle r2 = r15.mSavedFragmentState
            java.lang.String r3 = "android:view_state"
            android.util.SparseArray r2 = r2.getSparseParcelableArray(r3)
            r15.mSavedViewState = r2
            android.os.Bundle r2 = r15.mSavedFragmentState
            java.lang.String r3 = "android:target_state"
            android.support.v4.app.Fragment r2 = r14.getFragment(r2, r3)
            r15.mTarget = r2
            android.support.v4.app.Fragment r2 = r15.mTarget
            if (r2 == 0) goto L_0x010f
            android.os.Bundle r2 = r15.mSavedFragmentState
            java.lang.String r3 = "android:target_req_state"
            r4 = 0
            int r2 = r2.getInt(r3, r4)
            r15.mTargetRequestCode = r2
        L_0x010f:
            android.os.Bundle r2 = r15.mSavedFragmentState
            java.lang.String r3 = "android:user_visible_hint"
            r4 = 1
            boolean r2 = r2.getBoolean(r3, r4)
            r15.mUserVisibleHint = r2
            boolean r2 = r15.mUserVisibleHint
            if (r2 != 0) goto L_0x0129
            r2 = 1
            r15.mDeferStart = r2
            r2 = 3
            r0 = r16
            if (r0 <= r2) goto L_0x0129
            r16 = 3
        L_0x0129:
            android.support.v4.app.FragmentHostCallback r2 = r14.mHost
            r15.mHost = r2
            android.support.v4.app.Fragment r2 = r14.mParent
            r15.mParentFragment = r2
            android.support.v4.app.Fragment r2 = r14.mParent
            if (r2 == 0) goto L_0x017b
            android.support.v4.app.Fragment r2 = r14.mParent
            android.support.v4.app.FragmentManagerImpl r2 = r2.mChildFragmentManager
        L_0x0139:
            r15.mFragmentManager = r2
            android.support.v4.app.Fragment r2 = r15.mTarget
            if (r2 == 0) goto L_0x0193
            android.util.SparseArray<android.support.v4.app.Fragment> r2 = r14.mActive
            android.support.v4.app.Fragment r3 = r15.mTarget
            int r3 = r3.mIndex
            java.lang.Object r2 = r2.get(r3)
            android.support.v4.app.Fragment r3 = r15.mTarget
            if (r2 == r3) goto L_0x0182
            java.lang.IllegalStateException r2 = new java.lang.IllegalStateException
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "Fragment "
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.StringBuilder r3 = r3.append(r15)
            java.lang.String r4 = " declared target fragment "
            java.lang.StringBuilder r3 = r3.append(r4)
            android.support.v4.app.Fragment r4 = r15.mTarget
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.String r4 = " that does not belong to this FragmentManager!"
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.String r3 = r3.toString()
            r2.<init>(r3)
            throw r2
        L_0x017b:
            android.support.v4.app.FragmentHostCallback r2 = r14.mHost
            android.support.v4.app.FragmentManagerImpl r2 = r2.getFragmentManagerImpl()
            goto L_0x0139
        L_0x0182:
            android.support.v4.app.Fragment r2 = r15.mTarget
            int r2 = r2.mState
            r3 = 1
            if (r2 >= r3) goto L_0x0193
            android.support.v4.app.Fragment r3 = r15.mTarget
            r4 = 1
            r5 = 0
            r6 = 0
            r7 = 1
            r2 = r14
            r2.moveToState(r3, r4, r5, r6, r7)
        L_0x0193:
            android.support.v4.app.FragmentHostCallback r2 = r14.mHost
            android.content.Context r2 = r2.getContext()
            r3 = 0
            r14.dispatchOnFragmentPreAttached(r15, r2, r3)
            r2 = 0
            r15.mCalled = r2
            android.support.v4.app.FragmentHostCallback r2 = r14.mHost
            android.content.Context r2 = r2.getContext()
            r15.onAttach((android.content.Context) r2)
            boolean r2 = r15.mCalled
            if (r2 != 0) goto L_0x01ce
            android.support.v4.app.SuperNotCalledException r2 = new android.support.v4.app.SuperNotCalledException
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "Fragment "
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.StringBuilder r3 = r3.append(r15)
            java.lang.String r4 = " did not call through to super.onAttach()"
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.String r3 = r3.toString()
            r2.<init>(r3)
            throw r2
        L_0x01ce:
            android.support.v4.app.Fragment r2 = r15.mParentFragment
            if (r2 != 0) goto L_0x036d
            android.support.v4.app.FragmentHostCallback r2 = r14.mHost
            r2.onAttachFragment(r15)
        L_0x01d7:
            android.support.v4.app.FragmentHostCallback r2 = r14.mHost
            android.content.Context r2 = r2.getContext()
            r3 = 0
            r14.dispatchOnFragmentAttached(r15, r2, r3)
            boolean r2 = r15.mIsCreated
            if (r2 != 0) goto L_0x0374
            android.os.Bundle r2 = r15.mSavedFragmentState
            r3 = 0
            r14.dispatchOnFragmentPreCreated(r15, r2, r3)
            android.os.Bundle r2 = r15.mSavedFragmentState
            r15.performCreate(r2)
            android.os.Bundle r2 = r15.mSavedFragmentState
            r3 = 0
            r14.dispatchOnFragmentCreated(r15, r2, r3)
        L_0x01f6:
            r2 = 0
            r15.mRetaining = r2
        L_0x01f9:
            r14.ensureInflatedFragmentView(r15)
            r2 = 1
            r0 = r16
            if (r0 <= r2) goto L_0x0309
            boolean r2 = DEBUG
            if (r2 == 0) goto L_0x021f
            java.lang.String r2 = "FragmentManager"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "moveto ACTIVITY_CREATED: "
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.StringBuilder r3 = r3.append(r15)
            java.lang.String r3 = r3.toString()
            android.util.Log.v(r2, r3)
        L_0x021f:
            boolean r2 = r15.mFromLayout
            if (r2 != 0) goto L_0x02f2
            r10 = 0
            int r2 = r15.mContainerId
            if (r2 == 0) goto L_0x02a4
            int r2 = r15.mContainerId
            r3 = -1
            if (r2 != r3) goto L_0x0250
            java.lang.IllegalArgumentException r2 = new java.lang.IllegalArgumentException
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "Cannot create fragment "
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.StringBuilder r3 = r3.append(r15)
            java.lang.String r4 = " for a container view with no id"
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.String r3 = r3.toString()
            r2.<init>(r3)
            r14.throwException(r2)
        L_0x0250:
            android.support.v4.app.FragmentContainer r2 = r14.mContainer
            int r3 = r15.mContainerId
            android.view.View r10 = r2.onFindViewById(r3)
            android.view.ViewGroup r10 = (android.view.ViewGroup) r10
            if (r10 != 0) goto L_0x02a4
            boolean r2 = r15.mRestored
            r2 = r2 ^ 1
            if (r2 == 0) goto L_0x02a4
            android.content.res.Resources r2 = r15.getResources()     // Catch:{ NotFoundException -> 0x037e }
            int r3 = r15.mContainerId     // Catch:{ NotFoundException -> 0x037e }
            java.lang.String r12 = r2.getResourceName(r3)     // Catch:{ NotFoundException -> 0x037e }
        L_0x026c:
            java.lang.IllegalArgumentException r2 = new java.lang.IllegalArgumentException
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "No view found for id 0x"
            java.lang.StringBuilder r3 = r3.append(r4)
            int r4 = r15.mContainerId
            java.lang.String r4 = java.lang.Integer.toHexString(r4)
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.String r4 = " ("
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.StringBuilder r3 = r3.append(r12)
            java.lang.String r4 = ") for fragment "
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.StringBuilder r3 = r3.append(r15)
            java.lang.String r3 = r3.toString()
            r2.<init>(r3)
            r14.throwException(r2)
        L_0x02a4:
            r15.mContainer = r10
            android.os.Bundle r2 = r15.mSavedFragmentState
            android.view.LayoutInflater r2 = r15.performGetLayoutInflater(r2)
            android.os.Bundle r3 = r15.mSavedFragmentState
            android.view.View r2 = r15.performCreateView(r2, r10, r3)
            r15.mView = r2
            android.view.View r2 = r15.mView
            if (r2 == 0) goto L_0x038a
            android.view.View r2 = r15.mView
            r15.mInnerView = r2
            android.view.View r2 = r15.mView
            r3 = 0
            r2.setSaveFromParentEnabled(r3)
            if (r10 == 0) goto L_0x02c9
            android.view.View r2 = r15.mView
            r10.addView(r2)
        L_0x02c9:
            boolean r2 = r15.mHidden
            if (r2 == 0) goto L_0x02d4
            android.view.View r2 = r15.mView
            r3 = 8
            r2.setVisibility(r3)
        L_0x02d4:
            android.view.View r2 = r15.mView
            android.os.Bundle r3 = r15.mSavedFragmentState
            r15.onViewCreated(r2, r3)
            android.view.View r2 = r15.mView
            android.os.Bundle r3 = r15.mSavedFragmentState
            r4 = 0
            r14.dispatchOnFragmentViewCreated(r15, r2, r3, r4)
            android.view.View r2 = r15.mView
            int r2 = r2.getVisibility()
            if (r2 != 0) goto L_0x0387
            android.view.ViewGroup r2 = r15.mContainer
            if (r2 == 0) goto L_0x0384
            r2 = 1
        L_0x02f0:
            r15.mIsNewlyAdded = r2
        L_0x02f2:
            android.os.Bundle r2 = r15.mSavedFragmentState
            r15.performActivityCreated(r2)
            android.os.Bundle r2 = r15.mSavedFragmentState
            r3 = 0
            r14.dispatchOnFragmentActivityCreated(r15, r2, r3)
            android.view.View r2 = r15.mView
            if (r2 == 0) goto L_0x0306
            android.os.Bundle r2 = r15.mSavedFragmentState
            r15.restoreViewState(r2)
        L_0x0306:
            r2 = 0
            r15.mSavedFragmentState = r2
        L_0x0309:
            r2 = 2
            r0 = r16
            if (r0 <= r2) goto L_0x0311
            r2 = 3
            r15.mState = r2
        L_0x0311:
            r2 = 3
            r0 = r16
            if (r0 <= r2) goto L_0x033b
            boolean r2 = DEBUG
            if (r2 == 0) goto L_0x0334
            java.lang.String r2 = "FragmentManager"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "moveto STARTED: "
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.StringBuilder r3 = r3.append(r15)
            java.lang.String r3 = r3.toString()
            android.util.Log.v(r2, r3)
        L_0x0334:
            r15.performStart()
            r2 = 0
            r14.dispatchOnFragmentStarted(r15, r2)
        L_0x033b:
            r2 = 4
            r0 = r16
            if (r0 <= r2) goto L_0x0070
            boolean r2 = DEBUG
            if (r2 == 0) goto L_0x035e
            java.lang.String r2 = "FragmentManager"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "moveto RESUMED: "
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.StringBuilder r3 = r3.append(r15)
            java.lang.String r3 = r3.toString()
            android.util.Log.v(r2, r3)
        L_0x035e:
            r15.performResume()
            r2 = 0
            r14.dispatchOnFragmentResumed(r15, r2)
            r2 = 0
            r15.mSavedFragmentState = r2
            r2 = 0
            r15.mSavedViewState = r2
            goto L_0x0070
        L_0x036d:
            android.support.v4.app.Fragment r2 = r15.mParentFragment
            r2.onAttachFragment(r15)
            goto L_0x01d7
        L_0x0374:
            android.os.Bundle r2 = r15.mSavedFragmentState
            r15.restoreChildFragmentState(r2)
            r2 = 1
            r15.mState = r2
            goto L_0x01f6
        L_0x037e:
            r11 = move-exception
            java.lang.String r12 = "unknown"
            goto L_0x026c
        L_0x0384:
            r2 = 0
            goto L_0x02f0
        L_0x0387:
            r2 = 0
            goto L_0x02f0
        L_0x038a:
            r2 = 0
            r15.mInnerView = r2
            goto L_0x02f2
        L_0x038f:
            int r2 = r15.mState
            r0 = r16
            if (r2 <= r0) goto L_0x0070
            int r2 = r15.mState
            switch(r2) {
                case 1: goto L_0x039c;
                case 2: goto L_0x0443;
                case 3: goto L_0x041d;
                case 4: goto L_0x03f3;
                case 5: goto L_0x03c9;
                default: goto L_0x039a;
            }
        L_0x039a:
            goto L_0x0070
        L_0x039c:
            r2 = 1
            r0 = r16
            if (r0 >= r2) goto L_0x0070
            boolean r2 = r14.mDestroyed
            if (r2 == 0) goto L_0x03b6
            android.view.View r2 = r15.getAnimatingAway()
            if (r2 == 0) goto L_0x04d6
            android.view.View r13 = r15.getAnimatingAway()
            r2 = 0
            r15.setAnimatingAway(r2)
            r13.clearAnimation()
        L_0x03b6:
            android.view.View r2 = r15.getAnimatingAway()
            if (r2 != 0) goto L_0x03c2
            android.animation.Animator r2 = r15.getAnimator()
            if (r2 == 0) goto L_0x04e9
        L_0x03c2:
            r15.setStateAfterAnimating(r16)
            r16 = 1
            goto L_0x0070
        L_0x03c9:
            r2 = 5
            r0 = r16
            if (r0 >= r2) goto L_0x03f3
            boolean r2 = DEBUG
            if (r2 == 0) goto L_0x03ec
            java.lang.String r2 = "FragmentManager"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "movefrom RESUMED: "
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.StringBuilder r3 = r3.append(r15)
            java.lang.String r3 = r3.toString()
            android.util.Log.v(r2, r3)
        L_0x03ec:
            r15.performPause()
            r2 = 0
            r14.dispatchOnFragmentPaused(r15, r2)
        L_0x03f3:
            r2 = 4
            r0 = r16
            if (r0 >= r2) goto L_0x041d
            boolean r2 = DEBUG
            if (r2 == 0) goto L_0x0416
            java.lang.String r2 = "FragmentManager"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "movefrom STARTED: "
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.StringBuilder r3 = r3.append(r15)
            java.lang.String r3 = r3.toString()
            android.util.Log.v(r2, r3)
        L_0x0416:
            r15.performStop()
            r2 = 0
            r14.dispatchOnFragmentStopped(r15, r2)
        L_0x041d:
            r2 = 3
            r0 = r16
            if (r0 >= r2) goto L_0x0443
            boolean r2 = DEBUG
            if (r2 == 0) goto L_0x0440
            java.lang.String r2 = "FragmentManager"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "movefrom STOPPED: "
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.StringBuilder r3 = r3.append(r15)
            java.lang.String r3 = r3.toString()
            android.util.Log.v(r2, r3)
        L_0x0440:
            r15.performReallyStop()
        L_0x0443:
            r2 = 2
            r0 = r16
            if (r0 >= r2) goto L_0x039c
            boolean r2 = DEBUG
            if (r2 == 0) goto L_0x0466
            java.lang.String r2 = "FragmentManager"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "movefrom ACTIVITY_CREATED: "
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.StringBuilder r3 = r3.append(r15)
            java.lang.String r3 = r3.toString()
            android.util.Log.v(r2, r3)
        L_0x0466:
            android.view.View r2 = r15.mView
            if (r2 == 0) goto L_0x0479
            android.support.v4.app.FragmentHostCallback r2 = r14.mHost
            boolean r2 = r2.onShouldSaveFragmentState(r15)
            if (r2 == 0) goto L_0x0479
            android.util.SparseArray<android.os.Parcelable> r2 = r15.mSavedViewState
            if (r2 != 0) goto L_0x0479
            r14.saveFragmentViewState(r15)
        L_0x0479:
            r15.performDestroyView()
            r2 = 0
            r14.dispatchOnFragmentViewDestroyed(r15, r2)
            android.view.View r2 = r15.mView
            if (r2 == 0) goto L_0x04c8
            android.view.ViewGroup r2 = r15.mContainer
            if (r2 == 0) goto L_0x04c8
            android.view.View r2 = r15.mView
            r2.clearAnimation()
            android.view.ViewGroup r2 = r15.mContainer
            android.view.View r3 = r15.mView
            r2.endViewTransition(r3)
            r8 = 0
            int r2 = r14.mCurState
            if (r2 <= 0) goto L_0x04b7
            boolean r2 = r14.mDestroyed
            r2 = r2 ^ 1
            if (r2 == 0) goto L_0x04b7
            android.view.View r2 = r15.mView
            int r2 = r2.getVisibility()
            if (r2 != 0) goto L_0x04b7
            float r2 = r15.mPostponedAlpha
            r3 = 0
            int r2 = (r2 > r3 ? 1 : (r2 == r3 ? 0 : -1))
            if (r2 < 0) goto L_0x04b7
            r2 = 0
            r0 = r17
            r1 = r18
            android.support.v4.app.FragmentManagerImpl$AnimationOrAnimator r8 = r14.loadAnimation(r15, r0, r2, r1)
        L_0x04b7:
            r2 = 0
            r15.mPostponedAlpha = r2
            if (r8 == 0) goto L_0x04c1
            r0 = r16
            r14.animateRemoveFragment(r15, r8, r0)
        L_0x04c1:
            android.view.ViewGroup r2 = r15.mContainer
            android.view.View r3 = r15.mView
            r2.removeView(r3)
        L_0x04c8:
            r2 = 0
            r15.mContainer = r2
            r2 = 0
            r15.mView = r2
            r2 = 0
            r15.mInnerView = r2
            r2 = 0
            r15.mInLayout = r2
            goto L_0x039c
        L_0x04d6:
            android.animation.Animator r2 = r15.getAnimator()
            if (r2 == 0) goto L_0x03b6
            android.animation.Animator r9 = r15.getAnimator()
            r2 = 0
            r15.setAnimator(r2)
            r9.cancel()
            goto L_0x03b6
        L_0x04e9:
            boolean r2 = DEBUG
            if (r2 == 0) goto L_0x0507
            java.lang.String r2 = "FragmentManager"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "movefrom CREATED: "
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.StringBuilder r3 = r3.append(r15)
            java.lang.String r3 = r3.toString()
            android.util.Log.v(r2, r3)
        L_0x0507:
            boolean r2 = r15.mRetaining
            if (r2 != 0) goto L_0x0524
            r15.performDestroy()
            r2 = 0
            r14.dispatchOnFragmentDestroyed(r15, r2)
        L_0x0512:
            r15.performDetach()
            r2 = 0
            r14.dispatchOnFragmentDetached(r15, r2)
            if (r19 != 0) goto L_0x0070
            boolean r2 = r15.mRetaining
            if (r2 != 0) goto L_0x0528
            r14.makeInactive(r15)
            goto L_0x0070
        L_0x0524:
            r2 = 0
            r15.mState = r2
            goto L_0x0512
        L_0x0528:
            r2 = 0
            r15.mHost = r2
            r2 = 0
            r15.mParentFragment = r2
            r2 = 0
            r15.mFragmentManager = r2
            goto L_0x0070
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.app.FragmentManagerImpl.moveToState(android.support.v4.app.Fragment, int, int, int, boolean):void");
    }

    private void animateRemoveFragment(@NonNull final Fragment fragment, @NonNull AnimationOrAnimator anim, int newState) {
        final View viewToAnimate = fragment.mView;
        final ViewGroup container = fragment.mContainer;
        container.startViewTransition(viewToAnimate);
        fragment.setStateAfterAnimating(newState);
        if (anim.animation != null) {
            Animation animation = anim.animation;
            fragment.setAnimatingAway(fragment.mView);
            final Fragment fragment2 = fragment;
            animation.setAnimationListener(new AnimationListenerWrapper(getAnimationListener(animation)) {
                public void onAnimationEnd(Animation animation) {
                    super.onAnimationEnd(animation);
                    container.endViewTransition(viewToAnimate);
                    if (fragment2.getAnimatingAway() != null) {
                        fragment2.setAnimatingAway((View) null);
                        FragmentManagerImpl.this.moveToState(fragment2, fragment2.getStateAfterAnimating(), 0, 0, false);
                    }
                }
            });
            setHWLayerAnimListenerIfAlpha(viewToAnimate, anim);
            fragment.mView.startAnimation(animation);
            return;
        }
        Animator animator = anim.animator;
        fragment.setAnimator(anim.animator);
        animator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator anim) {
                container.endViewTransition(viewToAnimate);
                Animator animator = fragment.getAnimator();
                fragment.setAnimator((Animator) null);
                if (animator != null && container.indexOfChild(viewToAnimate) < 0) {
                    FragmentManagerImpl.this.moveToState(fragment, fragment.getStateAfterAnimating(), 0, 0, false);
                }
            }
        });
        animator.setTarget(fragment.mView);
        setHWLayerAnimListenerIfAlpha(fragment.mView, anim);
        animator.start();
    }

    /* access modifiers changed from: package-private */
    public void moveToState(Fragment f) {
        moveToState(f, this.mCurState, 0, 0, false);
    }

    /* access modifiers changed from: package-private */
    public void ensureInflatedFragmentView(Fragment f) {
        if (f.mFromLayout && (!f.mPerformedCreateView)) {
            f.mView = f.performCreateView(f.performGetLayoutInflater(f.mSavedFragmentState), (ViewGroup) null, f.mSavedFragmentState);
            if (f.mView != null) {
                f.mInnerView = f.mView;
                f.mView.setSaveFromParentEnabled(false);
                if (f.mHidden) {
                    f.mView.setVisibility(8);
                }
                f.onViewCreated(f.mView, f.mSavedFragmentState);
                dispatchOnFragmentViewCreated(f, f.mView, f.mSavedFragmentState, false);
                return;
            }
            f.mInnerView = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void completeShowHideFragment(final Fragment fragment) {
        int visibility;
        if (fragment.mView != null) {
            AnimationOrAnimator anim = loadAnimation(fragment, fragment.getNextTransition(), !fragment.mHidden, fragment.getNextTransitionStyle());
            if (anim == null || anim.animator == null) {
                if (anim != null) {
                    setHWLayerAnimListenerIfAlpha(fragment.mView, anim);
                    fragment.mView.startAnimation(anim.animation);
                    anim.animation.start();
                }
                if (!fragment.mHidden || !(!fragment.isHideReplaced())) {
                    visibility = 0;
                } else {
                    visibility = 8;
                }
                fragment.mView.setVisibility(visibility);
                if (fragment.isHideReplaced()) {
                    fragment.setHideReplaced(false);
                }
            } else {
                anim.animator.setTarget(fragment.mView);
                if (!fragment.mHidden) {
                    fragment.mView.setVisibility(0);
                } else if (fragment.isHideReplaced()) {
                    fragment.setHideReplaced(false);
                } else {
                    final ViewGroup container = fragment.mContainer;
                    final View animatingView = fragment.mView;
                    container.startViewTransition(animatingView);
                    anim.animator.addListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animation) {
                            container.endViewTransition(animatingView);
                            animation.removeListener(this);
                            if (fragment.mView != null) {
                                fragment.mView.setVisibility(8);
                            }
                        }
                    });
                }
                setHWLayerAnimListenerIfAlpha(fragment.mView, anim);
                anim.animator.start();
            }
        }
        if (fragment.mAdded && fragment.mHasMenu && fragment.mMenuVisible) {
            this.mNeedMenuInvalidate = true;
        }
        fragment.mHiddenChanged = false;
        fragment.onHiddenChanged(fragment.mHidden);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x002d, code lost:
        r10 = r8.mView;
        r7 = r15.mContainer;
        r9 = r7.indexOfChild(r10);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void moveFragmentToExpectedState(android.support.v4.app.Fragment r15) {
        /*
            r14 = this;
            r13 = 1
            r12 = 0
            r5 = 0
            if (r15 != 0) goto L_0x0006
            return
        L_0x0006:
            int r2 = r14.mCurState
            boolean r0 = r15.mRemoving
            if (r0 == 0) goto L_0x0016
            boolean r0 = r15.isInBackStack()
            if (r0 == 0) goto L_0x0084
            int r2 = java.lang.Math.min(r2, r13)
        L_0x0016:
            int r3 = r15.getNextTransition()
            int r4 = r15.getNextTransitionStyle()
            r0 = r14
            r1 = r15
            r0.moveToState(r1, r2, r3, r4, r5)
            android.view.View r0 = r15.mView
            if (r0 == 0) goto L_0x007c
            android.support.v4.app.Fragment r8 = r14.findFragmentUnder(r15)
            if (r8 == 0) goto L_0x0045
            android.view.View r10 = r8.mView
            android.view.ViewGroup r7 = r15.mContainer
            int r9 = r7.indexOfChild(r10)
            android.view.View r0 = r15.mView
            int r11 = r7.indexOfChild(r0)
            if (r11 >= r9) goto L_0x0045
            r7.removeViewAt(r11)
            android.view.View r0 = r15.mView
            r7.addView(r0, r9)
        L_0x0045:
            boolean r0 = r15.mIsNewlyAdded
            if (r0 == 0) goto L_0x007c
            android.view.ViewGroup r0 = r15.mContainer
            if (r0 == 0) goto L_0x007c
            float r0 = r15.mPostponedAlpha
            int r0 = (r0 > r12 ? 1 : (r0 == r12 ? 0 : -1))
            if (r0 <= 0) goto L_0x005a
            android.view.View r0 = r15.mView
            float r1 = r15.mPostponedAlpha
            r0.setAlpha(r1)
        L_0x005a:
            r15.mPostponedAlpha = r12
            r15.mIsNewlyAdded = r5
            int r0 = r15.getNextTransition()
            int r1 = r15.getNextTransitionStyle()
            android.support.v4.app.FragmentManagerImpl$AnimationOrAnimator r6 = r14.loadAnimation(r15, r0, r13, r1)
            if (r6 == 0) goto L_0x007c
            android.view.View r0 = r15.mView
            setHWLayerAnimListenerIfAlpha(r0, r6)
            android.view.animation.Animation r0 = r6.animation
            if (r0 == 0) goto L_0x0089
            android.view.View r0 = r15.mView
            android.view.animation.Animation r1 = r6.animation
            r0.startAnimation(r1)
        L_0x007c:
            boolean r0 = r15.mHiddenChanged
            if (r0 == 0) goto L_0x0083
            r14.completeShowHideFragment(r15)
        L_0x0083:
            return
        L_0x0084:
            int r2 = java.lang.Math.min(r2, r5)
            goto L_0x0016
        L_0x0089:
            android.animation.Animator r0 = r6.animator
            android.view.View r1 = r15.mView
            r0.setTarget(r1)
            android.animation.Animator r0 = r6.animator
            r0.start()
            goto L_0x007c
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.app.FragmentManagerImpl.moveFragmentToExpectedState(android.support.v4.app.Fragment):void");
    }

    /* access modifiers changed from: package-private */
    public void moveToState(int newState, boolean always) {
        if (this.mHost == null && newState != 0) {
            throw new IllegalStateException("No activity");
        } else if (always || newState != this.mCurState) {
            this.mCurState = newState;
            if (this.mActive != null) {
                boolean loadersRunning = false;
                int numAdded = this.mAdded.size();
                for (int i = 0; i < numAdded; i++) {
                    Fragment f = this.mAdded.get(i);
                    moveFragmentToExpectedState(f);
                    if (f.mLoaderManager != null) {
                        loadersRunning |= f.mLoaderManager.hasRunningLoaders();
                    }
                }
                int numActive = this.mActive.size();
                for (int i2 = 0; i2 < numActive; i2++) {
                    Fragment f2 = this.mActive.valueAt(i2);
                    if (f2 != null && ((f2.mRemoving || f2.mDetached) && (!f2.mIsNewlyAdded))) {
                        moveFragmentToExpectedState(f2);
                        if (f2.mLoaderManager != null) {
                            loadersRunning |= f2.mLoaderManager.hasRunningLoaders();
                        }
                    }
                }
                if (!loadersRunning) {
                    startPendingDeferredFragments();
                }
                if (this.mNeedMenuInvalidate && this.mHost != null && this.mCurState == 5) {
                    this.mHost.onSupportInvalidateOptionsMenu();
                    this.mNeedMenuInvalidate = false;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void startPendingDeferredFragments() {
        if (this.mActive != null) {
            for (int i = 0; i < this.mActive.size(); i++) {
                Fragment f = this.mActive.valueAt(i);
                if (f != null) {
                    performPendingDeferredStart(f);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void makeActive(Fragment f) {
        if (f.mIndex < 0) {
            int i = this.mNextFragmentIndex;
            this.mNextFragmentIndex = i + 1;
            f.setIndex(i, this.mParent);
            if (this.mActive == null) {
                this.mActive = new SparseArray<>();
            }
            this.mActive.put(f.mIndex, f);
            if (DEBUG) {
                Log.v(TAG, "Allocated fragment index " + f);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void makeInactive(Fragment f) {
        if (f.mIndex >= 0) {
            if (DEBUG) {
                Log.v(TAG, "Freeing fragment index " + f);
            }
            this.mActive.put(f.mIndex, (Object) null);
            this.mHost.inactivateFragment(f.mWho);
            f.initState();
        }
    }

    public void addFragment(Fragment fragment, boolean moveToStateNow) {
        if (DEBUG) {
            Log.v(TAG, "add: " + fragment);
        }
        makeActive(fragment);
        if (fragment.mDetached) {
            return;
        }
        if (this.mAdded.contains(fragment)) {
            throw new IllegalStateException("Fragment already added: " + fragment);
        }
        synchronized (this.mAdded) {
            this.mAdded.add(fragment);
        }
        fragment.mAdded = true;
        fragment.mRemoving = false;
        if (fragment.mView == null) {
            fragment.mHiddenChanged = false;
        }
        if (fragment.mHasMenu && fragment.mMenuVisible) {
            this.mNeedMenuInvalidate = true;
        }
        if (moveToStateNow) {
            moveToState(fragment);
        }
    }

    public void removeFragment(Fragment fragment) {
        if (DEBUG) {
            Log.v(TAG, "remove: " + fragment + " nesting=" + fragment.mBackStackNesting);
        }
        boolean inactive = !fragment.isInBackStack();
        if (!fragment.mDetached || inactive) {
            synchronized (this.mAdded) {
                this.mAdded.remove(fragment);
            }
            if (fragment.mHasMenu && fragment.mMenuVisible) {
                this.mNeedMenuInvalidate = true;
            }
            fragment.mAdded = false;
            fragment.mRemoving = true;
        }
    }

    public void hideFragment(Fragment fragment) {
        if (DEBUG) {
            Log.v(TAG, "hide: " + fragment);
        }
        if (!fragment.mHidden) {
            fragment.mHidden = true;
            fragment.mHiddenChanged = !fragment.mHiddenChanged;
        }
    }

    public void showFragment(Fragment fragment) {
        if (DEBUG) {
            Log.v(TAG, "show: " + fragment);
        }
        if (fragment.mHidden) {
            fragment.mHidden = false;
            fragment.mHiddenChanged = !fragment.mHiddenChanged;
        }
    }

    public void detachFragment(Fragment fragment) {
        if (DEBUG) {
            Log.v(TAG, "detach: " + fragment);
        }
        if (!fragment.mDetached) {
            fragment.mDetached = true;
            if (fragment.mAdded) {
                if (DEBUG) {
                    Log.v(TAG, "remove from detach: " + fragment);
                }
                synchronized (this.mAdded) {
                    this.mAdded.remove(fragment);
                }
                if (fragment.mHasMenu && fragment.mMenuVisible) {
                    this.mNeedMenuInvalidate = true;
                }
                fragment.mAdded = false;
            }
        }
    }

    public void attachFragment(Fragment fragment) {
        if (DEBUG) {
            Log.v(TAG, "attach: " + fragment);
        }
        if (fragment.mDetached) {
            fragment.mDetached = false;
            if (fragment.mAdded) {
                return;
            }
            if (this.mAdded.contains(fragment)) {
                throw new IllegalStateException("Fragment already added: " + fragment);
            }
            if (DEBUG) {
                Log.v(TAG, "add from attach: " + fragment);
            }
            synchronized (this.mAdded) {
                this.mAdded.add(fragment);
            }
            fragment.mAdded = true;
            if (fragment.mHasMenu && fragment.mMenuVisible) {
                this.mNeedMenuInvalidate = true;
            }
        }
    }

    public Fragment findFragmentById(int id) {
        for (int i = this.mAdded.size() - 1; i >= 0; i--) {
            Fragment f = this.mAdded.get(i);
            if (f != null && f.mFragmentId == id) {
                return f;
            }
        }
        if (this.mActive != null) {
            for (int i2 = this.mActive.size() - 1; i2 >= 0; i2--) {
                Fragment f2 = this.mActive.valueAt(i2);
                if (f2 != null && f2.mFragmentId == id) {
                    return f2;
                }
            }
        }
        return null;
    }

    public Fragment findFragmentByTag(String tag) {
        if (tag != null) {
            for (int i = this.mAdded.size() - 1; i >= 0; i--) {
                Fragment f = this.mAdded.get(i);
                if (f != null && tag.equals(f.mTag)) {
                    return f;
                }
            }
        }
        if (!(this.mActive == null || tag == null)) {
            for (int i2 = this.mActive.size() - 1; i2 >= 0; i2--) {
                Fragment f2 = this.mActive.valueAt(i2);
                if (f2 != null && tag.equals(f2.mTag)) {
                    return f2;
                }
            }
        }
        return null;
    }

    public Fragment findFragmentByWho(String who) {
        Fragment f;
        if (!(this.mActive == null || who == null)) {
            for (int i = this.mActive.size() - 1; i >= 0; i--) {
                Fragment f2 = this.mActive.valueAt(i);
                if (f2 != null && (f = f2.findFragmentByWho(who)) != null) {
                    return f;
                }
            }
        }
        return null;
    }

    private void checkStateLoss() {
        if (this.mStateSaved) {
            throw new IllegalStateException("Can not perform this action after onSaveInstanceState");
        } else if (this.mNoTransactionsBecause != null) {
            throw new IllegalStateException("Can not perform this action inside of " + this.mNoTransactionsBecause);
        }
    }

    public boolean isStateSaved() {
        return this.mStateSaved;
    }

    public void enqueueAction(OpGenerator action, boolean allowStateLoss) {
        if (!allowStateLoss) {
            checkStateLoss();
        }
        synchronized (this) {
            if (!this.mDestroyed && this.mHost != null) {
                if (this.mPendingActions == null) {
                    this.mPendingActions = new ArrayList<>();
                }
                this.mPendingActions.add(action);
                scheduleCommit();
            } else if (!allowStateLoss) {
                throw new IllegalStateException("Activity has been destroyed");
            }
        }
    }

    /* access modifiers changed from: private */
    public void scheduleCommit() {
        synchronized (this) {
            boolean z = this.mPostponedTransactions != null ? !this.mPostponedTransactions.isEmpty() : false;
            boolean pendingReady = this.mPendingActions != null && this.mPendingActions.size() == 1;
            if (z || pendingReady) {
                this.mHost.getHandler().removeCallbacks(this.mExecCommit);
                this.mHost.getHandler().post(this.mExecCommit);
            }
        }
    }

    public int allocBackStackIndex(BackStackRecord bse) {
        synchronized (this) {
            if (this.mAvailBackStackIndices == null || this.mAvailBackStackIndices.size() <= 0) {
                if (this.mBackStackIndices == null) {
                    this.mBackStackIndices = new ArrayList<>();
                }
                int index = this.mBackStackIndices.size();
                if (DEBUG) {
                    Log.v(TAG, "Setting back stack index " + index + " to " + bse);
                }
                this.mBackStackIndices.add(bse);
                return index;
            }
            int index2 = this.mAvailBackStackIndices.remove(this.mAvailBackStackIndices.size() - 1).intValue();
            if (DEBUG) {
                Log.v(TAG, "Adding back stack index " + index2 + " with " + bse);
            }
            this.mBackStackIndices.set(index2, bse);
            return index2;
        }
    }

    public void setBackStackIndex(int index, BackStackRecord bse) {
        synchronized (this) {
            if (this.mBackStackIndices == null) {
                this.mBackStackIndices = new ArrayList<>();
            }
            int N = this.mBackStackIndices.size();
            if (index < N) {
                if (DEBUG) {
                    Log.v(TAG, "Setting back stack index " + index + " to " + bse);
                }
                this.mBackStackIndices.set(index, bse);
            } else {
                while (N < index) {
                    this.mBackStackIndices.add((Object) null);
                    if (this.mAvailBackStackIndices == null) {
                        this.mAvailBackStackIndices = new ArrayList<>();
                    }
                    if (DEBUG) {
                        Log.v(TAG, "Adding available back stack index " + N);
                    }
                    this.mAvailBackStackIndices.add(Integer.valueOf(N));
                    N++;
                }
                if (DEBUG) {
                    Log.v(TAG, "Adding back stack index " + index + " with " + bse);
                }
                this.mBackStackIndices.add(bse);
            }
        }
    }

    public void freeBackStackIndex(int index) {
        synchronized (this) {
            this.mBackStackIndices.set(index, (Object) null);
            if (this.mAvailBackStackIndices == null) {
                this.mAvailBackStackIndices = new ArrayList<>();
            }
            if (DEBUG) {
                Log.v(TAG, "Freeing back stack index " + index);
            }
            this.mAvailBackStackIndices.add(Integer.valueOf(index));
        }
    }

    private void ensureExecReady(boolean allowStateLoss) {
        if (this.mExecutingActions) {
            throw new IllegalStateException("FragmentManager is already executing transactions");
        } else if (Looper.myLooper() != this.mHost.getHandler().getLooper()) {
            throw new IllegalStateException("Must be called from main thread of fragment host");
        } else {
            if (!allowStateLoss) {
                checkStateLoss();
            }
            if (this.mTmpRecords == null) {
                this.mTmpRecords = new ArrayList<>();
                this.mTmpIsPop = new ArrayList<>();
            }
            this.mExecutingActions = true;
            try {
                executePostponedTransaction((ArrayList<BackStackRecord>) null, (ArrayList<Boolean>) null);
            } finally {
                this.mExecutingActions = false;
            }
        }
    }

    public void execSingleAction(OpGenerator action, boolean allowStateLoss) {
        if (!allowStateLoss || (this.mHost != null && !this.mDestroyed)) {
            ensureExecReady(allowStateLoss);
            if (action.generateOps(this.mTmpRecords, this.mTmpIsPop)) {
                this.mExecutingActions = true;
                try {
                    removeRedundantOperationsAndExecute(this.mTmpRecords, this.mTmpIsPop);
                } finally {
                    cleanupExec();
                }
            }
            doPendingDeferredStart();
            burpActive();
        }
    }

    private void cleanupExec() {
        this.mExecutingActions = false;
        this.mTmpIsPop.clear();
        this.mTmpRecords.clear();
    }

    /* JADX INFO: finally extract failed */
    public boolean execPendingActions() {
        ensureExecReady(true);
        boolean didSomething = false;
        while (generateOpsForPendingActions(this.mTmpRecords, this.mTmpIsPop)) {
            this.mExecutingActions = true;
            try {
                removeRedundantOperationsAndExecute(this.mTmpRecords, this.mTmpIsPop);
                cleanupExec();
                didSomething = true;
            } catch (Throwable th) {
                cleanupExec();
                throw th;
            }
        }
        doPendingDeferredStart();
        burpActive();
        return didSomething;
    }

    private void executePostponedTransaction(ArrayList<BackStackRecord> records, ArrayList<Boolean> isRecordPop) {
        int index;
        int index2;
        int numPostponed = this.mPostponedTransactions == null ? 0 : this.mPostponedTransactions.size();
        int i = 0;
        while (i < numPostponed) {
            StartEnterTransitionListener listener = this.mPostponedTransactions.get(i);
            if (records != null && (!listener.mIsBack) && (index2 = records.indexOf(listener.mRecord)) != -1 && isRecordPop.get(index2).booleanValue()) {
                listener.cancelTransaction();
            } else if (listener.isReady() || (records != null && listener.mRecord.interactsWith(records, 0, records.size()))) {
                this.mPostponedTransactions.remove(i);
                i--;
                numPostponed--;
                if (records == null || !(!listener.mIsBack) || (index = records.indexOf(listener.mRecord)) == -1 || !isRecordPop.get(index).booleanValue()) {
                    listener.completeTransaction();
                } else {
                    listener.cancelTransaction();
                }
            }
            i++;
        }
    }

    private void removeRedundantOperationsAndExecute(ArrayList<BackStackRecord> records, ArrayList<Boolean> isRecordPop) {
        if (records != null && !records.isEmpty()) {
            if (isRecordPop == null || records.size() != isRecordPop.size()) {
                throw new IllegalStateException("Internal error with the back stack records");
            }
            executePostponedTransaction(records, isRecordPop);
            int numRecords = records.size();
            int startIndex = 0;
            int recordNum = 0;
            while (recordNum < numRecords) {
                if (!records.get(recordNum).mReorderingAllowed) {
                    if (startIndex != recordNum) {
                        executeOpsTogether(records, isRecordPop, startIndex, recordNum);
                    }
                    int reorderingEnd = recordNum + 1;
                    if (isRecordPop.get(recordNum).booleanValue()) {
                        while (reorderingEnd < numRecords && isRecordPop.get(reorderingEnd).booleanValue() && (!records.get(reorderingEnd).mReorderingAllowed)) {
                            reorderingEnd++;
                        }
                    }
                    executeOpsTogether(records, isRecordPop, recordNum, reorderingEnd);
                    startIndex = reorderingEnd;
                    recordNum = reorderingEnd - 1;
                }
                recordNum++;
            }
            if (startIndex != numRecords) {
                executeOpsTogether(records, isRecordPop, startIndex, numRecords);
            }
        }
    }

    private void executeOpsTogether(ArrayList<BackStackRecord> records, ArrayList<Boolean> isRecordPop, int startIndex, int endIndex) {
        boolean allowReordering = records.get(startIndex).mReorderingAllowed;
        boolean addToBackStack = false;
        if (this.mTmpAddedFragments == null) {
            this.mTmpAddedFragments = new ArrayList<>();
        } else {
            this.mTmpAddedFragments.clear();
        }
        this.mTmpAddedFragments.addAll(this.mAdded);
        Fragment oldPrimaryNav = getPrimaryNavigationFragment();
        for (int recordNum = startIndex; recordNum < endIndex; recordNum++) {
            BackStackRecord record = records.get(recordNum);
            if (!isRecordPop.get(recordNum).booleanValue()) {
                oldPrimaryNav = record.expandOps(this.mTmpAddedFragments, oldPrimaryNav);
            } else {
                oldPrimaryNav = record.trackAddedFragmentsInPop(this.mTmpAddedFragments, oldPrimaryNav);
            }
            if (!addToBackStack) {
                addToBackStack = record.mAddToBackStack;
            } else {
                addToBackStack = true;
            }
        }
        this.mTmpAddedFragments.clear();
        if (!allowReordering) {
            FragmentTransition.startTransitions(this, records, isRecordPop, startIndex, endIndex, false);
        }
        executeOps(records, isRecordPop, startIndex, endIndex);
        int postponeIndex = endIndex;
        if (allowReordering) {
            ArraySet<Fragment> addedFragments = new ArraySet<>();
            addAddedFragments(addedFragments);
            postponeIndex = postponePostponableTransactions(records, isRecordPop, startIndex, endIndex, addedFragments);
            makeRemovedFragmentsInvisible(addedFragments);
        }
        if (postponeIndex != startIndex && allowReordering) {
            FragmentTransition.startTransitions(this, records, isRecordPop, startIndex, postponeIndex, true);
            moveToState(this.mCurState, true);
        }
        for (int recordNum2 = startIndex; recordNum2 < endIndex; recordNum2++) {
            BackStackRecord record2 = records.get(recordNum2);
            if (isRecordPop.get(recordNum2).booleanValue() && record2.mIndex >= 0) {
                freeBackStackIndex(record2.mIndex);
                record2.mIndex = -1;
            }
            record2.runOnCommitRunnables();
        }
        if (addToBackStack) {
            reportBackStackChanged();
        }
    }

    private void makeRemovedFragmentsInvisible(ArraySet<Fragment> fragments) {
        int numAdded = fragments.size();
        for (int i = 0; i < numAdded; i++) {
            Fragment fragment = fragments.valueAt(i);
            if (!fragment.mAdded) {
                View view = fragment.getView();
                fragment.mPostponedAlpha = view.getAlpha();
                view.setAlpha(0.0f);
            }
        }
    }

    private int postponePostponableTransactions(ArrayList<BackStackRecord> records, ArrayList<Boolean> isRecordPop, int startIndex, int endIndex, ArraySet<Fragment> added) {
        boolean z;
        int postponeIndex = endIndex;
        for (int i = endIndex - 1; i >= startIndex; i--) {
            BackStackRecord record = records.get(i);
            boolean isPop = isRecordPop.get(i).booleanValue();
            if (record.isPostponed()) {
                z = !record.interactsWith(records, i + 1, endIndex);
            } else {
                z = false;
            }
            if (z) {
                if (this.mPostponedTransactions == null) {
                    this.mPostponedTransactions = new ArrayList<>();
                }
                StartEnterTransitionListener listener = new StartEnterTransitionListener(record, isPop);
                this.mPostponedTransactions.add(listener);
                record.setOnStartPostponedListener(listener);
                if (isPop) {
                    record.executeOps();
                } else {
                    record.executePopOps(false);
                }
                postponeIndex--;
                if (i != postponeIndex) {
                    records.remove(i);
                    records.add(postponeIndex, record);
                }
                addAddedFragments(added);
            }
        }
        return postponeIndex;
    }

    /* access modifiers changed from: private */
    public void completeExecute(BackStackRecord record, boolean isPop, boolean runTransitions, boolean moveToState) {
        if (isPop) {
            record.executePopOps(moveToState);
        } else {
            record.executeOps();
        }
        ArrayList<BackStackRecord> records = new ArrayList<>(1);
        ArrayList<Boolean> isRecordPop = new ArrayList<>(1);
        records.add(record);
        isRecordPop.add(Boolean.valueOf(isPop));
        if (runTransitions) {
            FragmentTransition.startTransitions(this, records, isRecordPop, 0, 1, true);
        }
        if (moveToState) {
            moveToState(this.mCurState, true);
        }
        if (this.mActive != null) {
            int numActive = this.mActive.size();
            for (int i = 0; i < numActive; i++) {
                Fragment fragment = this.mActive.valueAt(i);
                if (fragment != null && fragment.mView != null && fragment.mIsNewlyAdded && record.interactsWith(fragment.mContainerId)) {
                    if (fragment.mPostponedAlpha > 0.0f) {
                        fragment.mView.setAlpha(fragment.mPostponedAlpha);
                    }
                    if (moveToState) {
                        fragment.mPostponedAlpha = 0.0f;
                    } else {
                        fragment.mPostponedAlpha = -1.0f;
                        fragment.mIsNewlyAdded = false;
                    }
                }
            }
        }
    }

    private Fragment findFragmentUnder(Fragment f) {
        ViewGroup container = f.mContainer;
        View view = f.mView;
        if (container == null || view == null) {
            return null;
        }
        for (int i = this.mAdded.indexOf(f) - 1; i >= 0; i--) {
            Fragment underFragment = this.mAdded.get(i);
            if (underFragment.mContainer == container && underFragment.mView != null) {
                return underFragment;
            }
        }
        return null;
    }

    private static void executeOps(ArrayList<BackStackRecord> records, ArrayList<Boolean> isRecordPop, int startIndex, int endIndex) {
        int i = startIndex;
        while (i < endIndex) {
            BackStackRecord record = records.get(i);
            if (isRecordPop.get(i).booleanValue()) {
                record.bumpBackStackNesting(-1);
                record.executePopOps(i == endIndex + -1);
            } else {
                record.bumpBackStackNesting(1);
                record.executeOps();
            }
            i++;
        }
    }

    private void addAddedFragments(ArraySet<Fragment> added) {
        if (this.mCurState >= 1) {
            int state = Math.min(this.mCurState, 4);
            int numAdded = this.mAdded.size();
            for (int i = 0; i < numAdded; i++) {
                Fragment fragment = this.mAdded.get(i);
                if (fragment.mState < state) {
                    moveToState(fragment, state, fragment.getNextAnim(), fragment.getNextTransition(), false);
                    if (fragment.mView != null && (!fragment.mHidden) && fragment.mIsNewlyAdded) {
                        added.add(fragment);
                    }
                }
            }
        }
    }

    private void forcePostponedTransactions() {
        if (this.mPostponedTransactions != null) {
            while (!this.mPostponedTransactions.isEmpty()) {
                this.mPostponedTransactions.remove(0).completeTransaction();
            }
        }
    }

    private void endAnimatingAwayFragments() {
        int numFragments = this.mActive == null ? 0 : this.mActive.size();
        for (int i = 0; i < numFragments; i++) {
            Fragment fragment = this.mActive.valueAt(i);
            if (fragment != null) {
                if (fragment.getAnimatingAway() != null) {
                    int stateAfterAnimating = fragment.getStateAfterAnimating();
                    View animatingAway = fragment.getAnimatingAway();
                    Animation animation = animatingAway.getAnimation();
                    if (animation != null) {
                        animation.cancel();
                        animatingAway.clearAnimation();
                    }
                    fragment.setAnimatingAway((View) null);
                    moveToState(fragment, stateAfterAnimating, 0, 0, false);
                } else if (fragment.getAnimator() != null) {
                    fragment.getAnimator().end();
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0010, code lost:
        return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean generateOpsForPendingActions(java.util.ArrayList<android.support.v4.app.BackStackRecord> r6, java.util.ArrayList<java.lang.Boolean> r7) {
        /*
            r5 = this;
            r4 = 0
            r0 = 0
            monitor-enter(r5)
            java.util.ArrayList<android.support.v4.app.FragmentManagerImpl$OpGenerator> r3 = r5.mPendingActions     // Catch:{ all -> 0x003c }
            if (r3 == 0) goto L_0x000f
            java.util.ArrayList<android.support.v4.app.FragmentManagerImpl$OpGenerator> r3 = r5.mPendingActions     // Catch:{ all -> 0x003c }
            int r3 = r3.size()     // Catch:{ all -> 0x003c }
            if (r3 != 0) goto L_0x0011
        L_0x000f:
            monitor-exit(r5)
            return r4
        L_0x0011:
            java.util.ArrayList<android.support.v4.app.FragmentManagerImpl$OpGenerator> r3 = r5.mPendingActions     // Catch:{ all -> 0x003c }
            int r2 = r3.size()     // Catch:{ all -> 0x003c }
            r1 = 0
        L_0x0018:
            if (r1 >= r2) goto L_0x002a
            java.util.ArrayList<android.support.v4.app.FragmentManagerImpl$OpGenerator> r3 = r5.mPendingActions     // Catch:{ all -> 0x003c }
            java.lang.Object r3 = r3.get(r1)     // Catch:{ all -> 0x003c }
            android.support.v4.app.FragmentManagerImpl$OpGenerator r3 = (android.support.v4.app.FragmentManagerImpl.OpGenerator) r3     // Catch:{ all -> 0x003c }
            boolean r3 = r3.generateOps(r6, r7)     // Catch:{ all -> 0x003c }
            r0 = r0 | r3
            int r1 = r1 + 1
            goto L_0x0018
        L_0x002a:
            java.util.ArrayList<android.support.v4.app.FragmentManagerImpl$OpGenerator> r3 = r5.mPendingActions     // Catch:{ all -> 0x003c }
            r3.clear()     // Catch:{ all -> 0x003c }
            android.support.v4.app.FragmentHostCallback r3 = r5.mHost     // Catch:{ all -> 0x003c }
            android.os.Handler r3 = r3.getHandler()     // Catch:{ all -> 0x003c }
            java.lang.Runnable r4 = r5.mExecCommit     // Catch:{ all -> 0x003c }
            r3.removeCallbacks(r4)     // Catch:{ all -> 0x003c }
            monitor-exit(r5)
            return r0
        L_0x003c:
            r3 = move-exception
            monitor-exit(r5)
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.app.FragmentManagerImpl.generateOpsForPendingActions(java.util.ArrayList, java.util.ArrayList):boolean");
    }

    /* access modifiers changed from: package-private */
    public void doPendingDeferredStart() {
        if (this.mHavePendingDeferredStart) {
            boolean loadersRunning = false;
            for (int i = 0; i < this.mActive.size(); i++) {
                Fragment f = this.mActive.valueAt(i);
                if (!(f == null || f.mLoaderManager == null)) {
                    loadersRunning |= f.mLoaderManager.hasRunningLoaders();
                }
            }
            if (!loadersRunning) {
                this.mHavePendingDeferredStart = false;
                startPendingDeferredFragments();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void reportBackStackChanged() {
        if (this.mBackStackChangeListeners != null) {
            for (int i = 0; i < this.mBackStackChangeListeners.size(); i++) {
                this.mBackStackChangeListeners.get(i).onBackStackChanged();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void addBackStackState(BackStackRecord state) {
        if (this.mBackStack == null) {
            this.mBackStack = new ArrayList<>();
        }
        this.mBackStack.add(state);
    }

    /* access modifiers changed from: package-private */
    public boolean popBackStackState(ArrayList<BackStackRecord> records, ArrayList<Boolean> isRecordPop, String name, int id, int flags) {
        if (this.mBackStack == null) {
            return false;
        }
        if (name == null && id < 0 && (flags & 1) == 0) {
            int last = this.mBackStack.size() - 1;
            if (last < 0) {
                return false;
            }
            records.add(this.mBackStack.remove(last));
            isRecordPop.add(true);
        } else {
            int index = -1;
            if (name != null || id >= 0) {
                int index2 = this.mBackStack.size() - 1;
                while (index >= 0) {
                    BackStackRecord bss = this.mBackStack.get(index);
                    if ((name != null && name.equals(bss.getName())) || (id >= 0 && id == bss.mIndex)) {
                        break;
                    }
                    index2 = index - 1;
                }
                if (index < 0) {
                    return false;
                }
                if ((flags & 1) != 0) {
                    index--;
                    while (index >= 0) {
                        BackStackRecord bss2 = this.mBackStack.get(index);
                        if ((name == null || !name.equals(bss2.getName())) && (id < 0 || id != bss2.mIndex)) {
                            break;
                        }
                        index--;
                    }
                }
            }
            if (index == this.mBackStack.size() - 1) {
                return false;
            }
            for (int i = this.mBackStack.size() - 1; i > index; i--) {
                records.add(this.mBackStack.remove(i));
                isRecordPop.add(true);
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public FragmentManagerNonConfig retainNonConfig() {
        setRetaining(this.mSavedNonConfig);
        return this.mSavedNonConfig;
    }

    private static void setRetaining(FragmentManagerNonConfig nonConfig) {
        if (nonConfig != null) {
            List<Fragment> fragments = nonConfig.getFragments();
            if (fragments != null) {
                for (Fragment fragment : fragments) {
                    fragment.mRetaining = true;
                }
            }
            List<FragmentManagerNonConfig> children = nonConfig.getChildNonConfigs();
            if (children != null) {
                for (FragmentManagerNonConfig child : children) {
                    setRetaining(child);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void saveNonConfig() {
        FragmentManagerNonConfig child;
        ArrayList<Fragment> fragments = null;
        ArrayList<FragmentManagerNonConfig> childFragments = null;
        if (this.mActive != null) {
            for (int i = 0; i < this.mActive.size(); i++) {
                Fragment f = this.mActive.valueAt(i);
                if (f != null) {
                    if (f.mRetainInstance) {
                        if (fragments == null) {
                            fragments = new ArrayList<>();
                        }
                        fragments.add(f);
                        f.mTargetIndex = f.mTarget != null ? f.mTarget.mIndex : -1;
                        if (DEBUG) {
                            Log.v(TAG, "retainNonConfig: keeping retained " + f);
                        }
                    }
                    if (f.mChildFragmentManager != null) {
                        f.mChildFragmentManager.saveNonConfig();
                        child = f.mChildFragmentManager.mSavedNonConfig;
                    } else {
                        child = f.mChildNonConfig;
                    }
                    if (childFragments == null && child != null) {
                        childFragments = new ArrayList<>(this.mActive.size());
                        for (int j = 0; j < i; j++) {
                            childFragments.add((Object) null);
                        }
                    }
                    if (childFragments != null) {
                        childFragments.add(child);
                    }
                }
            }
        }
        if (fragments == null && childFragments == null) {
            this.mSavedNonConfig = null;
        } else {
            this.mSavedNonConfig = new FragmentManagerNonConfig(fragments, childFragments);
        }
    }

    /* access modifiers changed from: package-private */
    public void saveFragmentViewState(Fragment f) {
        if (f.mInnerView != null) {
            if (this.mStateArray == null) {
                this.mStateArray = new SparseArray<>();
            } else {
                this.mStateArray.clear();
            }
            f.mInnerView.saveHierarchyState(this.mStateArray);
            if (this.mStateArray.size() > 0) {
                f.mSavedViewState = this.mStateArray;
                this.mStateArray = null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Bundle saveFragmentBasicState(Fragment f) {
        Bundle result = null;
        if (this.mStateBundle == null) {
            this.mStateBundle = new Bundle();
        }
        f.performSaveInstanceState(this.mStateBundle);
        dispatchOnFragmentSaveInstanceState(f, this.mStateBundle, false);
        if (!this.mStateBundle.isEmpty()) {
            result = this.mStateBundle;
            this.mStateBundle = null;
        }
        if (f.mView != null) {
            saveFragmentViewState(f);
        }
        if (f.mSavedViewState != null) {
            if (result == null) {
                result = new Bundle();
            }
            result.putSparseParcelableArray(VIEW_STATE_TAG, f.mSavedViewState);
        }
        if (!f.mUserVisibleHint) {
            if (result == null) {
                result = new Bundle();
            }
            result.putBoolean(USER_VISIBLE_HINT_TAG, f.mUserVisibleHint);
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public Parcelable saveAllState() {
        int N;
        forcePostponedTransactions();
        endAnimatingAwayFragments();
        execPendingActions();
        this.mStateSaved = true;
        this.mSavedNonConfig = null;
        if (this.mActive == null || this.mActive.size() <= 0) {
            return null;
        }
        int N2 = this.mActive.size();
        FragmentState[] active = new FragmentState[N2];
        boolean haveFragments = false;
        for (int i = 0; i < N2; i++) {
            Fragment f = this.mActive.valueAt(i);
            if (f != null) {
                if (f.mIndex < 0) {
                    throwException(new IllegalStateException("Failure saving state: active " + f + " has cleared index: " + f.mIndex));
                }
                haveFragments = true;
                FragmentState fs = new FragmentState(f);
                active[i] = fs;
                if (f.mState <= 0 || fs.mSavedFragmentState != null) {
                    fs.mSavedFragmentState = f.mSavedFragmentState;
                } else {
                    fs.mSavedFragmentState = saveFragmentBasicState(f);
                    if (f.mTarget != null) {
                        if (f.mTarget.mIndex < 0) {
                            throwException(new IllegalStateException("Failure saving state: " + f + " has target not in fragment manager: " + f.mTarget));
                        }
                        if (fs.mSavedFragmentState == null) {
                            fs.mSavedFragmentState = new Bundle();
                        }
                        putFragment(fs.mSavedFragmentState, TARGET_STATE_TAG, f.mTarget);
                        if (f.mTargetRequestCode != 0) {
                            fs.mSavedFragmentState.putInt(TARGET_REQUEST_CODE_STATE_TAG, f.mTargetRequestCode);
                        }
                    }
                }
                if (DEBUG) {
                    Log.v(TAG, "Saved state of " + f + ": " + fs.mSavedFragmentState);
                }
            }
        }
        if (!haveFragments) {
            if (DEBUG) {
                Log.v(TAG, "saveAllState: no fragments!");
            }
            return null;
        }
        int[] added = null;
        BackStackState[] backStack = null;
        int N3 = this.mAdded.size();
        if (N3 > 0) {
            added = new int[N3];
            for (int i2 = 0; i2 < N3; i2++) {
                added[i2] = this.mAdded.get(i2).mIndex;
                if (added[i2] < 0) {
                    throwException(new IllegalStateException("Failure saving state: active " + this.mAdded.get(i2) + " has cleared index: " + added[i2]));
                }
                if (DEBUG) {
                    Log.v(TAG, "saveAllState: adding fragment #" + i2 + ": " + this.mAdded.get(i2));
                }
            }
        }
        if (this.mBackStack != null && (N = this.mBackStack.size()) > 0) {
            backStack = new BackStackState[N];
            for (int i3 = 0; i3 < N; i3++) {
                backStack[i3] = new BackStackState(this.mBackStack.get(i3));
                if (DEBUG) {
                    Log.v(TAG, "saveAllState: adding back stack #" + i3 + ": " + this.mBackStack.get(i3));
                }
            }
        }
        FragmentManagerState fms = new FragmentManagerState();
        fms.mActive = active;
        fms.mAdded = added;
        fms.mBackStack = backStack;
        if (this.mPrimaryNav != null) {
            fms.mPrimaryNavActiveIndex = this.mPrimaryNav.mIndex;
        }
        fms.mNextFragmentIndex = this.mNextFragmentIndex;
        saveNonConfig();
        return fms;
    }

    /* access modifiers changed from: package-private */
    public void restoreAllState(Parcelable state, FragmentManagerNonConfig nonConfig) {
        if (state != null) {
            FragmentManagerState fms = (FragmentManagerState) state;
            if (fms.mActive != null) {
                List<FragmentManagerNonConfig> childNonConfigs = null;
                if (nonConfig != null) {
                    List<Fragment> nonConfigFragments = nonConfig.getFragments();
                    childNonConfigs = nonConfig.getChildNonConfigs();
                    int count = nonConfigFragments != null ? nonConfigFragments.size() : 0;
                    for (int i = 0; i < count; i++) {
                        Fragment f = nonConfigFragments.get(i);
                        if (DEBUG) {
                            Log.v(TAG, "restoreAllState: re-attaching retained " + f);
                        }
                        int index = 0;
                        while (index < fms.mActive.length && fms.mActive[index].mIndex != f.mIndex) {
                            index++;
                        }
                        if (index == fms.mActive.length) {
                            throwException(new IllegalStateException("Could not find active fragment with index " + f.mIndex));
                        }
                        FragmentState fs = fms.mActive[index];
                        fs.mInstance = f;
                        f.mSavedViewState = null;
                        f.mBackStackNesting = 0;
                        f.mInLayout = false;
                        f.mAdded = false;
                        f.mTarget = null;
                        if (fs.mSavedFragmentState != null) {
                            fs.mSavedFragmentState.setClassLoader(this.mHost.getContext().getClassLoader());
                            f.mSavedViewState = fs.mSavedFragmentState.getSparseParcelableArray(VIEW_STATE_TAG);
                            f.mSavedFragmentState = fs.mSavedFragmentState;
                        }
                    }
                }
                this.mActive = new SparseArray<>(fms.mActive.length);
                for (int i2 = 0; i2 < fms.mActive.length; i2++) {
                    FragmentState fs2 = fms.mActive[i2];
                    if (fs2 != null) {
                        FragmentManagerNonConfig childNonConfig = null;
                        if (childNonConfigs != null && i2 < childNonConfigs.size()) {
                            childNonConfig = childNonConfigs.get(i2);
                        }
                        Fragment f2 = fs2.instantiate(this.mHost, this.mContainer, this.mParent, childNonConfig);
                        if (DEBUG) {
                            Log.v(TAG, "restoreAllState: active #" + i2 + ": " + f2);
                        }
                        this.mActive.put(f2.mIndex, f2);
                        fs2.mInstance = null;
                    }
                }
                if (nonConfig != null) {
                    List<Fragment> nonConfigFragments2 = nonConfig.getFragments();
                    int count2 = nonConfigFragments2 != null ? nonConfigFragments2.size() : 0;
                    for (int i3 = 0; i3 < count2; i3++) {
                        Fragment f3 = nonConfigFragments2.get(i3);
                        if (f3.mTargetIndex >= 0) {
                            f3.mTarget = this.mActive.get(f3.mTargetIndex);
                            if (f3.mTarget == null) {
                                Log.w(TAG, "Re-attaching retained fragment " + f3 + " target no longer exists: " + f3.mTargetIndex);
                            }
                        }
                    }
                }
                this.mAdded.clear();
                if (fms.mAdded != null) {
                    for (int i4 = 0; i4 < fms.mAdded.length; i4++) {
                        Fragment f4 = this.mActive.get(fms.mAdded[i4]);
                        if (f4 == null) {
                            throwException(new IllegalStateException("No instantiated fragment for index #" + fms.mAdded[i4]));
                        }
                        f4.mAdded = true;
                        if (DEBUG) {
                            Log.v(TAG, "restoreAllState: added #" + i4 + ": " + f4);
                        }
                        if (this.mAdded.contains(f4)) {
                            throw new IllegalStateException("Already added!");
                        }
                        synchronized (this.mAdded) {
                            this.mAdded.add(f4);
                        }
                    }
                }
                if (fms.mBackStack != null) {
                    this.mBackStack = new ArrayList<>(fms.mBackStack.length);
                    for (int i5 = 0; i5 < fms.mBackStack.length; i5++) {
                        BackStackRecord bse = fms.mBackStack[i5].instantiate(this);
                        if (DEBUG) {
                            Log.v(TAG, "restoreAllState: back stack #" + i5 + " (index " + bse.mIndex + "): " + bse);
                            PrintWriter pw = new PrintWriter(new LogWriter(TAG));
                            bse.dump("  ", pw, false);
                            pw.close();
                        }
                        this.mBackStack.add(bse);
                        if (bse.mIndex >= 0) {
                            setBackStackIndex(bse.mIndex, bse);
                        }
                    }
                } else {
                    this.mBackStack = null;
                }
                if (fms.mPrimaryNavActiveIndex >= 0) {
                    this.mPrimaryNav = this.mActive.get(fms.mPrimaryNavActiveIndex);
                }
                this.mNextFragmentIndex = fms.mNextFragmentIndex;
            }
        }
    }

    private void burpActive() {
        if (this.mActive != null) {
            for (int i = this.mActive.size() - 1; i >= 0; i--) {
                if (this.mActive.valueAt(i) == null) {
                    this.mActive.delete(this.mActive.keyAt(i));
                }
            }
        }
    }

    public void attachController(FragmentHostCallback host, FragmentContainer container, Fragment parent) {
        if (this.mHost != null) {
            throw new IllegalStateException("Already attached");
        }
        this.mHost = host;
        this.mContainer = container;
        this.mParent = parent;
    }

    public void noteStateNotSaved() {
        this.mSavedNonConfig = null;
        this.mStateSaved = false;
        int addedCount = this.mAdded.size();
        for (int i = 0; i < addedCount; i++) {
            Fragment fragment = this.mAdded.get(i);
            if (fragment != null) {
                fragment.noteStateNotSaved();
            }
        }
    }

    public void dispatchCreate() {
        this.mStateSaved = false;
        dispatchStateChange(1);
    }

    public void dispatchActivityCreated() {
        this.mStateSaved = false;
        dispatchStateChange(2);
    }

    public void dispatchStart() {
        this.mStateSaved = false;
        dispatchStateChange(4);
    }

    public void dispatchResume() {
        this.mStateSaved = false;
        dispatchStateChange(5);
    }

    public void dispatchPause() {
        dispatchStateChange(4);
    }

    public void dispatchStop() {
        this.mStateSaved = true;
        dispatchStateChange(3);
    }

    public void dispatchReallyStop() {
        dispatchStateChange(2);
    }

    public void dispatchDestroyView() {
        dispatchStateChange(1);
    }

    public void dispatchDestroy() {
        this.mDestroyed = true;
        execPendingActions();
        dispatchStateChange(0);
        this.mHost = null;
        this.mContainer = null;
        this.mParent = null;
    }

    /* JADX INFO: finally extract failed */
    private void dispatchStateChange(int nextState) {
        try {
            this.mExecutingActions = true;
            moveToState(nextState, false);
            this.mExecutingActions = false;
            execPendingActions();
        } catch (Throwable th) {
            this.mExecutingActions = false;
            throw th;
        }
    }

    public void dispatchMultiWindowModeChanged(boolean isInMultiWindowMode) {
        for (int i = this.mAdded.size() - 1; i >= 0; i--) {
            Fragment f = this.mAdded.get(i);
            if (f != null) {
                f.performMultiWindowModeChanged(isInMultiWindowMode);
            }
        }
    }

    public void dispatchPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        for (int i = this.mAdded.size() - 1; i >= 0; i--) {
            Fragment f = this.mAdded.get(i);
            if (f != null) {
                f.performPictureInPictureModeChanged(isInPictureInPictureMode);
            }
        }
    }

    public void dispatchConfigurationChanged(Configuration newConfig) {
        for (int i = 0; i < this.mAdded.size(); i++) {
            Fragment f = this.mAdded.get(i);
            if (f != null) {
                f.performConfigurationChanged(newConfig);
            }
        }
    }

    public void dispatchLowMemory() {
        for (int i = 0; i < this.mAdded.size(); i++) {
            Fragment f = this.mAdded.get(i);
            if (f != null) {
                f.performLowMemory();
            }
        }
    }

    public boolean dispatchCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (this.mCurState < 1) {
            return false;
        }
        boolean show = false;
        ArrayList<Fragment> newMenus = null;
        for (int i = 0; i < this.mAdded.size(); i++) {
            Fragment f = this.mAdded.get(i);
            if (f != null && f.performCreateOptionsMenu(menu, inflater)) {
                show = true;
                if (newMenus == null) {
                    newMenus = new ArrayList<>();
                }
                newMenus.add(f);
            }
        }
        if (this.mCreatedMenus != null) {
            for (int i2 = 0; i2 < this.mCreatedMenus.size(); i2++) {
                Fragment f2 = this.mCreatedMenus.get(i2);
                if (newMenus == null || (!newMenus.contains(f2))) {
                    f2.onDestroyOptionsMenu();
                }
            }
        }
        this.mCreatedMenus = newMenus;
        return show;
    }

    public boolean dispatchPrepareOptionsMenu(Menu menu) {
        if (this.mCurState < 1) {
            return false;
        }
        boolean show = false;
        for (int i = 0; i < this.mAdded.size(); i++) {
            Fragment f = this.mAdded.get(i);
            if (f != null && f.performPrepareOptionsMenu(menu)) {
                show = true;
            }
        }
        return show;
    }

    public boolean dispatchOptionsItemSelected(MenuItem item) {
        if (this.mCurState < 1) {
            return false;
        }
        for (int i = 0; i < this.mAdded.size(); i++) {
            Fragment f = this.mAdded.get(i);
            if (f != null && f.performOptionsItemSelected(item)) {
                return true;
            }
        }
        return false;
    }

    public boolean dispatchContextItemSelected(MenuItem item) {
        if (this.mCurState < 1) {
            return false;
        }
        for (int i = 0; i < this.mAdded.size(); i++) {
            Fragment f = this.mAdded.get(i);
            if (f != null && f.performContextItemSelected(item)) {
                return true;
            }
        }
        return false;
    }

    public void dispatchOptionsMenuClosed(Menu menu) {
        if (this.mCurState >= 1) {
            for (int i = 0; i < this.mAdded.size(); i++) {
                Fragment f = this.mAdded.get(i);
                if (f != null) {
                    f.performOptionsMenuClosed(menu);
                }
            }
        }
    }

    public void setPrimaryNavigationFragment(Fragment f) {
        if (f == null || (this.mActive.get(f.mIndex) == f && (f.mHost == null || f.getFragmentManager() == this))) {
            this.mPrimaryNav = f;
            return;
        }
        throw new IllegalArgumentException("Fragment " + f + " is not an active fragment of FragmentManager " + this);
    }

    public Fragment getPrimaryNavigationFragment() {
        return this.mPrimaryNav;
    }

    public void registerFragmentLifecycleCallbacks(FragmentManager.FragmentLifecycleCallbacks cb, boolean recursive) {
        this.mLifecycleCallbacks.add(new Pair(cb, Boolean.valueOf(recursive)));
    }

    public void unregisterFragmentLifecycleCallbacks(FragmentManager.FragmentLifecycleCallbacks cb) {
        synchronized (this.mLifecycleCallbacks) {
            int i = 0;
            int N = this.mLifecycleCallbacks.size();
            while (true) {
                if (i >= N) {
                    break;
                } else if (this.mLifecycleCallbacks.get(i).first == cb) {
                    this.mLifecycleCallbacks.remove(i);
                    break;
                } else {
                    i++;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchOnFragmentPreAttached(Fragment f, Context context, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentPreAttached(f, context, true);
            }
        }
        for (Pair<FragmentManager.FragmentLifecycleCallbacks, Boolean> p : this.mLifecycleCallbacks) {
            if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                ((FragmentManager.FragmentLifecycleCallbacks) p.first).onFragmentPreAttached(this, f, context);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchOnFragmentAttached(Fragment f, Context context, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentAttached(f, context, true);
            }
        }
        for (Pair<FragmentManager.FragmentLifecycleCallbacks, Boolean> p : this.mLifecycleCallbacks) {
            if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                ((FragmentManager.FragmentLifecycleCallbacks) p.first).onFragmentAttached(this, f, context);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchOnFragmentPreCreated(Fragment f, Bundle savedInstanceState, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentPreCreated(f, savedInstanceState, true);
            }
        }
        for (Pair<FragmentManager.FragmentLifecycleCallbacks, Boolean> p : this.mLifecycleCallbacks) {
            if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                ((FragmentManager.FragmentLifecycleCallbacks) p.first).onFragmentPreCreated(this, f, savedInstanceState);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchOnFragmentCreated(Fragment f, Bundle savedInstanceState, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentCreated(f, savedInstanceState, true);
            }
        }
        for (Pair<FragmentManager.FragmentLifecycleCallbacks, Boolean> p : this.mLifecycleCallbacks) {
            if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                ((FragmentManager.FragmentLifecycleCallbacks) p.first).onFragmentCreated(this, f, savedInstanceState);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchOnFragmentActivityCreated(Fragment f, Bundle savedInstanceState, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentActivityCreated(f, savedInstanceState, true);
            }
        }
        for (Pair<FragmentManager.FragmentLifecycleCallbacks, Boolean> p : this.mLifecycleCallbacks) {
            if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                ((FragmentManager.FragmentLifecycleCallbacks) p.first).onFragmentActivityCreated(this, f, savedInstanceState);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchOnFragmentViewCreated(Fragment f, View v, Bundle savedInstanceState, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentViewCreated(f, v, savedInstanceState, true);
            }
        }
        for (Pair<FragmentManager.FragmentLifecycleCallbacks, Boolean> p : this.mLifecycleCallbacks) {
            if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                ((FragmentManager.FragmentLifecycleCallbacks) p.first).onFragmentViewCreated(this, f, v, savedInstanceState);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchOnFragmentStarted(Fragment f, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentStarted(f, true);
            }
        }
        for (Pair<FragmentManager.FragmentLifecycleCallbacks, Boolean> p : this.mLifecycleCallbacks) {
            if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                ((FragmentManager.FragmentLifecycleCallbacks) p.first).onFragmentStarted(this, f);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchOnFragmentResumed(Fragment f, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentResumed(f, true);
            }
        }
        for (Pair<FragmentManager.FragmentLifecycleCallbacks, Boolean> p : this.mLifecycleCallbacks) {
            if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                ((FragmentManager.FragmentLifecycleCallbacks) p.first).onFragmentResumed(this, f);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchOnFragmentPaused(Fragment f, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentPaused(f, true);
            }
        }
        for (Pair<FragmentManager.FragmentLifecycleCallbacks, Boolean> p : this.mLifecycleCallbacks) {
            if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                ((FragmentManager.FragmentLifecycleCallbacks) p.first).onFragmentPaused(this, f);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchOnFragmentStopped(Fragment f, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentStopped(f, true);
            }
        }
        for (Pair<FragmentManager.FragmentLifecycleCallbacks, Boolean> p : this.mLifecycleCallbacks) {
            if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                ((FragmentManager.FragmentLifecycleCallbacks) p.first).onFragmentStopped(this, f);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchOnFragmentSaveInstanceState(Fragment f, Bundle outState, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentSaveInstanceState(f, outState, true);
            }
        }
        for (Pair<FragmentManager.FragmentLifecycleCallbacks, Boolean> p : this.mLifecycleCallbacks) {
            if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                ((FragmentManager.FragmentLifecycleCallbacks) p.first).onFragmentSaveInstanceState(this, f, outState);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchOnFragmentViewDestroyed(Fragment f, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentViewDestroyed(f, true);
            }
        }
        for (Pair<FragmentManager.FragmentLifecycleCallbacks, Boolean> p : this.mLifecycleCallbacks) {
            if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                ((FragmentManager.FragmentLifecycleCallbacks) p.first).onFragmentViewDestroyed(this, f);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchOnFragmentDestroyed(Fragment f, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentDestroyed(f, true);
            }
        }
        for (Pair<FragmentManager.FragmentLifecycleCallbacks, Boolean> p : this.mLifecycleCallbacks) {
            if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                ((FragmentManager.FragmentLifecycleCallbacks) p.first).onFragmentDestroyed(this, f);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchOnFragmentDetached(Fragment f, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentDetached(f, true);
            }
        }
        for (Pair<FragmentManager.FragmentLifecycleCallbacks, Boolean> p : this.mLifecycleCallbacks) {
            if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                ((FragmentManager.FragmentLifecycleCallbacks) p.first).onFragmentDetached(this, f);
            }
        }
    }

    public static int reverseTransit(int transit) {
        switch (transit) {
            case FragmentTransaction.TRANSIT_FRAGMENT_OPEN:
                return 8194;
            case FragmentTransaction.TRANSIT_FRAGMENT_FADE:
                return FragmentTransaction.TRANSIT_FRAGMENT_FADE;
            case 8194:
                return FragmentTransaction.TRANSIT_FRAGMENT_OPEN;
            default:
                return 0;
        }
    }

    public static int transitToStyleIndex(int transit, boolean enter) {
        switch (transit) {
            case FragmentTransaction.TRANSIT_FRAGMENT_OPEN:
                return enter ? 1 : 2;
            case FragmentTransaction.TRANSIT_FRAGMENT_FADE:
                return enter ? 5 : 6;
            case 8194:
                return enter ? 3 : 4;
            default:
                return -1;
        }
    }

    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        int i;
        if (!"fragment".equals(name)) {
            return null;
        }
        String fname = attrs.getAttributeValue((String) null, "class");
        TypedArray a = context.obtainStyledAttributes(attrs, FragmentTag.Fragment);
        if (fname == null) {
            fname = a.getString(0);
        }
        int id = a.getResourceId(1, -1);
        String tag = a.getString(2);
        a.recycle();
        if (!Fragment.isSupportFragmentClass(this.mHost.getContext(), fname)) {
            return null;
        }
        int containerId = parent != null ? parent.getId() : 0;
        if (containerId == -1 && id == -1 && tag == null) {
            throw new IllegalArgumentException(attrs.getPositionDescription() + ": Must specify unique android:id, android:tag, or have a parent with an id for " + fname);
        }
        Fragment fragment = id != -1 ? findFragmentById(id) : null;
        if (fragment == null && tag != null) {
            fragment = findFragmentByTag(tag);
        }
        if (fragment == null && containerId != -1) {
            fragment = findFragmentById(containerId);
        }
        if (DEBUG) {
            Log.v(TAG, "onCreateView: id=0x" + Integer.toHexString(id) + " fname=" + fname + " existing=" + fragment);
        }
        if (fragment == null) {
            fragment = this.mContainer.instantiate(context, fname, (Bundle) null);
            fragment.mFromLayout = true;
            if (id != 0) {
                i = id;
            } else {
                i = containerId;
            }
            fragment.mFragmentId = i;
            fragment.mContainerId = containerId;
            fragment.mTag = tag;
            fragment.mInLayout = true;
            fragment.mFragmentManager = this;
            fragment.mHost = this.mHost;
            fragment.onInflate(this.mHost.getContext(), attrs, fragment.mSavedFragmentState);
            addFragment(fragment, true);
        } else if (fragment.mInLayout) {
            throw new IllegalArgumentException(attrs.getPositionDescription() + ": Duplicate id 0x" + Integer.toHexString(id) + ", tag " + tag + ", or parent id 0x" + Integer.toHexString(containerId) + " with another fragment for " + fname);
        } else {
            fragment.mInLayout = true;
            fragment.mHost = this.mHost;
            if (!fragment.mRetaining) {
                fragment.onInflate(this.mHost.getContext(), attrs, fragment.mSavedFragmentState);
            }
        }
        if (this.mCurState >= 1 || !fragment.mFromLayout) {
            moveToState(fragment);
        } else {
            moveToState(fragment, 1, 0, 0, false);
        }
        if (fragment.mView == null) {
            throw new IllegalStateException("Fragment " + fname + " did not create a view.");
        }
        if (id != 0) {
            fragment.mView.setId(id);
        }
        if (fragment.mView.getTag() == null) {
            fragment.mView.setTag(tag);
        }
        return fragment.mView;
    }

    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return onCreateView((View) null, name, context, attrs);
    }

    /* access modifiers changed from: package-private */
    public LayoutInflater.Factory2 getLayoutInflaterFactory() {
        return this;
    }

    /* compiled from: FragmentManager */
    static class FragmentTag {
        public static final int[] Fragment = {16842755, 16842960, 16842961};
        public static final int Fragment_id = 1;
        public static final int Fragment_name = 0;
        public static final int Fragment_tag = 2;

        FragmentTag() {
        }
    }

    /* compiled from: FragmentManager */
    private class PopBackStackState implements OpGenerator {
        final int mFlags;
        final int mId;
        final String mName;

        PopBackStackState(String name, int id, int flags) {
            this.mName = name;
            this.mId = id;
            this.mFlags = flags;
        }

        public boolean generateOps(ArrayList<BackStackRecord> records, ArrayList<Boolean> isRecordPop) {
            FragmentManager childManager;
            if (FragmentManagerImpl.this.mPrimaryNav != null && this.mId < 0 && this.mName == null && (childManager = FragmentManagerImpl.this.mPrimaryNav.peekChildFragmentManager()) != null && childManager.popBackStackImmediate()) {
                return false;
            }
            return FragmentManagerImpl.this.popBackStackState(records, isRecordPop, this.mName, this.mId, this.mFlags);
        }
    }

    /* compiled from: FragmentManager */
    static class StartEnterTransitionListener implements Fragment.OnStartEnterTransitionListener {
        /* access modifiers changed from: private */
        public final boolean mIsBack;
        private int mNumPostponed;
        /* access modifiers changed from: private */
        public final BackStackRecord mRecord;

        StartEnterTransitionListener(BackStackRecord record, boolean isBack) {
            this.mIsBack = isBack;
            this.mRecord = record;
        }

        public void onStartEnterTransition() {
            this.mNumPostponed--;
            if (this.mNumPostponed == 0) {
                this.mRecord.mManager.scheduleCommit();
            }
        }

        public void startListening() {
            this.mNumPostponed++;
        }

        public boolean isReady() {
            return this.mNumPostponed == 0;
        }

        public void completeTransaction() {
            boolean canceled = this.mNumPostponed > 0;
            FragmentManagerImpl manager = this.mRecord.mManager;
            int numAdded = manager.mAdded.size();
            for (int i = 0; i < numAdded; i++) {
                Fragment fragment = manager.mAdded.get(i);
                fragment.setOnStartEnterTransitionListener((Fragment.OnStartEnterTransitionListener) null);
                if (canceled && fragment.isPostponed()) {
                    fragment.startPostponedEnterTransition();
                }
            }
            this.mRecord.mManager.completeExecute(this.mRecord, this.mIsBack, !canceled, true);
        }

        public void cancelTransaction() {
            this.mRecord.mManager.completeExecute(this.mRecord, this.mIsBack, false, false);
        }
    }

    /* compiled from: FragmentManager */
    private static class AnimationOrAnimator {
        public final Animation animation;
        public final Animator animator;

        /* synthetic */ AnimationOrAnimator(Animator animator2, AnimationOrAnimator animationOrAnimator) {
            this(animator2);
        }

        /* synthetic */ AnimationOrAnimator(Animation animation2, AnimationOrAnimator animationOrAnimator) {
            this(animation2);
        }

        private AnimationOrAnimator(Animation animation2) {
            this.animation = animation2;
            this.animator = null;
            if (animation2 == null) {
                throw new IllegalStateException("Animation cannot be null");
            }
        }

        private AnimationOrAnimator(Animator animator2) {
            this.animation = null;
            this.animator = animator2;
            if (animator2 == null) {
                throw new IllegalStateException("Animator cannot be null");
            }
        }
    }

    /* compiled from: FragmentManager */
    private static class AnimationListenerWrapper implements Animation.AnimationListener {
        private final Animation.AnimationListener mWrapped;

        /* synthetic */ AnimationListenerWrapper(Animation.AnimationListener wrapped, AnimationListenerWrapper animationListenerWrapper) {
            this(wrapped);
        }

        private AnimationListenerWrapper(Animation.AnimationListener wrapped) {
            this.mWrapped = wrapped;
        }

        @CallSuper
        public void onAnimationStart(Animation animation) {
            if (this.mWrapped != null) {
                this.mWrapped.onAnimationStart(animation);
            }
        }

        @CallSuper
        public void onAnimationEnd(Animation animation) {
            if (this.mWrapped != null) {
                this.mWrapped.onAnimationEnd(animation);
            }
        }

        @CallSuper
        public void onAnimationRepeat(Animation animation) {
            if (this.mWrapped != null) {
                this.mWrapped.onAnimationRepeat(animation);
            }
        }
    }

    /* compiled from: FragmentManager */
    private static class AnimateOnHWLayerIfNeededListener extends AnimationListenerWrapper {
        View mView;

        AnimateOnHWLayerIfNeededListener(View v, Animation.AnimationListener listener) {
            super(listener, (AnimationListenerWrapper) null);
            this.mView = v;
        }

        @CallSuper
        public void onAnimationEnd(Animation animation) {
            if (ViewCompat.isAttachedToWindow(this.mView) || Build.VERSION.SDK_INT >= 24) {
                this.mView.post(new Runnable() {
                    public void run() {
                        AnimateOnHWLayerIfNeededListener.this.mView.setLayerType(0, (Paint) null);
                    }
                });
            } else {
                this.mView.setLayerType(0, (Paint) null);
            }
            super.onAnimationEnd(animation);
        }
    }

    /* compiled from: FragmentManager */
    private static class AnimatorOnHWLayerIfNeededListener extends AnimatorListenerAdapter {
        View mView;

        AnimatorOnHWLayerIfNeededListener(View v) {
            this.mView = v;
        }

        public void onAnimationStart(Animator animation) {
            this.mView.setLayerType(2, (Paint) null);
        }

        public void onAnimationEnd(Animator animation) {
            this.mView.setLayerType(0, (Paint) null);
            animation.removeListener(this);
        }
    }
}
