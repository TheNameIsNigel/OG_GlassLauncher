package android.support.v4.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.SparseArray;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiresApi(16)
class NotificationCompatJellybean {
    static final String EXTRA_ALLOW_GENERATED_REPLIES = "android.support.allowGeneratedReplies";
    static final String EXTRA_DATA_ONLY_REMOTE_INPUTS = "android.support.dataRemoteInputs";
    private static final String KEY_ACTION_INTENT = "actionIntent";
    private static final String KEY_ALLOWED_DATA_TYPES = "allowedDataTypes";
    private static final String KEY_ALLOW_FREE_FORM_INPUT = "allowFreeFormInput";
    private static final String KEY_CHOICES = "choices";
    private static final String KEY_DATA_ONLY_REMOTE_INPUTS = "dataOnlyRemoteInputs";
    private static final String KEY_EXTRAS = "extras";
    private static final String KEY_ICON = "icon";
    private static final String KEY_LABEL = "label";
    private static final String KEY_REMOTE_INPUTS = "remoteInputs";
    private static final String KEY_RESULT_KEY = "resultKey";
    private static final String KEY_TITLE = "title";
    public static final String TAG = "NotificationCompat";
    private static Class<?> sActionClass;
    private static Field sActionIconField;
    private static Field sActionIntentField;
    private static Field sActionTitleField;
    private static boolean sActionsAccessFailed;
    private static Field sActionsField;
    private static final Object sActionsLock = new Object();
    private static Field sExtrasField;
    private static boolean sExtrasFieldAccessFailed;
    private static final Object sExtrasLock = new Object();

    NotificationCompatJellybean() {
    }

    public static SparseArray<Bundle> buildActionExtrasMap(List<Bundle> actionExtrasList) {
        SparseArray<Bundle> actionExtrasMap = null;
        int count = actionExtrasList.size();
        for (int i = 0; i < count; i++) {
            Bundle actionExtras = actionExtrasList.get(i);
            if (actionExtras != null) {
                if (actionExtrasMap == null) {
                    actionExtrasMap = new SparseArray<>();
                }
                actionExtrasMap.put(i, actionExtras);
            }
        }
        return actionExtrasMap;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004c, code lost:
        return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static android.os.Bundle getExtras(android.app.Notification r8) {
        /*
            r7 = 0
            java.lang.Object r5 = sExtrasLock
            monitor-enter(r5)
            boolean r4 = sExtrasFieldAccessFailed     // Catch:{ all -> 0x0067 }
            if (r4 == 0) goto L_0x000a
            monitor-exit(r5)
            return r7
        L_0x000a:
            java.lang.reflect.Field r4 = sExtrasField     // Catch:{ IllegalAccessException -> 0x005c, NoSuchFieldException -> 0x004d }
            if (r4 != 0) goto L_0x0037
            java.lang.Class<android.app.Notification> r4 = android.app.Notification.class
            java.lang.String r6 = "extras"
            java.lang.reflect.Field r3 = r4.getDeclaredField(r6)     // Catch:{ IllegalAccessException -> 0x005c, NoSuchFieldException -> 0x004d }
            java.lang.Class<android.os.Bundle> r4 = android.os.Bundle.class
            java.lang.Class r6 = r3.getType()     // Catch:{ IllegalAccessException -> 0x005c, NoSuchFieldException -> 0x004d }
            boolean r4 = r4.isAssignableFrom(r6)     // Catch:{ IllegalAccessException -> 0x005c, NoSuchFieldException -> 0x004d }
            if (r4 != 0) goto L_0x0031
            java.lang.String r4 = "NotificationCompat"
            java.lang.String r6 = "Notification.extras field is not of type Bundle"
            android.util.Log.e(r4, r6)     // Catch:{ IllegalAccessException -> 0x005c, NoSuchFieldException -> 0x004d }
            r4 = 1
            sExtrasFieldAccessFailed = r4     // Catch:{ IllegalAccessException -> 0x005c, NoSuchFieldException -> 0x004d }
            monitor-exit(r5)
            return r7
        L_0x0031:
            r4 = 1
            r3.setAccessible(r4)     // Catch:{ IllegalAccessException -> 0x005c, NoSuchFieldException -> 0x004d }
            sExtrasField = r3     // Catch:{ IllegalAccessException -> 0x005c, NoSuchFieldException -> 0x004d }
        L_0x0037:
            java.lang.reflect.Field r4 = sExtrasField     // Catch:{ IllegalAccessException -> 0x005c, NoSuchFieldException -> 0x004d }
            java.lang.Object r2 = r4.get(r8)     // Catch:{ IllegalAccessException -> 0x005c, NoSuchFieldException -> 0x004d }
            android.os.Bundle r2 = (android.os.Bundle) r2     // Catch:{ IllegalAccessException -> 0x005c, NoSuchFieldException -> 0x004d }
            if (r2 != 0) goto L_0x004b
            android.os.Bundle r2 = new android.os.Bundle     // Catch:{ IllegalAccessException -> 0x005c, NoSuchFieldException -> 0x004d }
            r2.<init>()     // Catch:{ IllegalAccessException -> 0x005c, NoSuchFieldException -> 0x004d }
            java.lang.reflect.Field r4 = sExtrasField     // Catch:{ IllegalAccessException -> 0x005c, NoSuchFieldException -> 0x004d }
            r4.set(r8, r2)     // Catch:{ IllegalAccessException -> 0x005c, NoSuchFieldException -> 0x004d }
        L_0x004b:
            monitor-exit(r5)
            return r2
        L_0x004d:
            r1 = move-exception
            java.lang.String r4 = "NotificationCompat"
            java.lang.String r6 = "Unable to access notification extras"
            android.util.Log.e(r4, r6, r1)     // Catch:{ all -> 0x0067 }
        L_0x0057:
            r4 = 1
            sExtrasFieldAccessFailed = r4     // Catch:{ all -> 0x0067 }
            monitor-exit(r5)
            return r7
        L_0x005c:
            r0 = move-exception
            java.lang.String r4 = "NotificationCompat"
            java.lang.String r6 = "Unable to access notification extras"
            android.util.Log.e(r4, r6, r0)     // Catch:{ all -> 0x0067 }
            goto L_0x0057
        L_0x0067:
            r4 = move-exception
            monitor-exit(r5)
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.app.NotificationCompatJellybean.getExtras(android.app.Notification):android.os.Bundle");
    }

    public static NotificationCompat.Action readAction(int icon, CharSequence title, PendingIntent actionIntent, Bundle extras) {
        RemoteInput[] remoteInputs = null;
        RemoteInput[] dataOnlyRemoteInputs = null;
        boolean allowGeneratedReplies = false;
        if (extras != null) {
            remoteInputs = fromBundleArray(getBundleArrayFromBundle(extras, NotificationCompatExtras.EXTRA_REMOTE_INPUTS));
            dataOnlyRemoteInputs = fromBundleArray(getBundleArrayFromBundle(extras, EXTRA_DATA_ONLY_REMOTE_INPUTS));
            allowGeneratedReplies = extras.getBoolean(EXTRA_ALLOW_GENERATED_REPLIES);
        }
        return new NotificationCompat.Action(icon, title, actionIntent, extras, remoteInputs, dataOnlyRemoteInputs, allowGeneratedReplies);
    }

    public static Bundle writeActionAndGetExtras(Notification.Builder builder, NotificationCompat.Action action) {
        builder.addAction(action.getIcon(), action.getTitle(), action.getActionIntent());
        Bundle actionExtras = new Bundle(action.getExtras());
        if (action.getRemoteInputs() != null) {
            actionExtras.putParcelableArray(NotificationCompatExtras.EXTRA_REMOTE_INPUTS, toBundleArray(action.getRemoteInputs()));
        }
        if (action.getDataOnlyRemoteInputs() != null) {
            actionExtras.putParcelableArray(EXTRA_DATA_ONLY_REMOTE_INPUTS, toBundleArray(action.getDataOnlyRemoteInputs()));
        }
        actionExtras.putBoolean(EXTRA_ALLOW_GENERATED_REPLIES, action.getAllowGeneratedReplies());
        return actionExtras;
    }

    public static int getActionCount(Notification notif) {
        int length;
        synchronized (sActionsLock) {
            Object[] actionObjects = getActionObjectsLocked(notif);
            length = actionObjects != null ? actionObjects.length : 0;
        }
        return length;
    }

    public static NotificationCompat.Action getAction(Notification notif, int actionIndex) {
        SparseArray<Bundle> actionExtrasMap;
        synchronized (sActionsLock) {
            try {
                Object[] actionObjects = getActionObjectsLocked(notif);
                if (actionObjects != null) {
                    Object actionObject = actionObjects[actionIndex];
                    Bundle actionExtras = null;
                    Bundle extras = getExtras(notif);
                    if (!(extras == null || (actionExtrasMap = extras.getSparseParcelableArray(NotificationCompatExtras.EXTRA_ACTION_EXTRAS)) == null)) {
                        actionExtras = actionExtrasMap.get(actionIndex);
                    }
                    NotificationCompat.Action readAction = readAction(sActionIconField.getInt(actionObject), (CharSequence) sActionTitleField.get(actionObject), (PendingIntent) sActionIntentField.get(actionObject), actionExtras);
                    return readAction;
                }
            } catch (IllegalAccessException e) {
                Log.e(TAG, "Unable to access notification actions", e);
                sActionsAccessFailed = true;
            }
        }
        return null;
    }

    private static Object[] getActionObjectsLocked(Notification notif) {
        synchronized (sActionsLock) {
            if (!ensureActionReflectionReadyLocked()) {
                return null;
            }
            try {
                Object[] objArr = (Object[]) sActionsField.get(notif);
                return objArr;
            } catch (IllegalAccessException e) {
                Log.e(TAG, "Unable to access notification actions", e);
                sActionsAccessFailed = true;
                return null;
            }
        }
    }

    private static boolean ensureActionReflectionReadyLocked() {
        if (sActionsAccessFailed) {
            return false;
        }
        try {
            if (sActionsField == null) {
                sActionClass = Class.forName("android.app.Notification$Action");
                sActionIconField = sActionClass.getDeclaredField(KEY_ICON);
                sActionTitleField = sActionClass.getDeclaredField(KEY_TITLE);
                sActionIntentField = sActionClass.getDeclaredField(KEY_ACTION_INTENT);
                sActionsField = Notification.class.getDeclaredField("actions");
                sActionsField.setAccessible(true);
            }
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Unable to access notification actions", e);
            sActionsAccessFailed = true;
        } catch (NoSuchFieldException e2) {
            Log.e(TAG, "Unable to access notification actions", e2);
            sActionsAccessFailed = true;
        }
        return !sActionsAccessFailed;
    }

    static NotificationCompat.Action getActionFromBundle(Bundle bundle) {
        Bundle extras = bundle.getBundle(KEY_EXTRAS);
        boolean allowGeneratedReplies = false;
        if (extras != null) {
            allowGeneratedReplies = extras.getBoolean(EXTRA_ALLOW_GENERATED_REPLIES, false);
        }
        return new NotificationCompat.Action(bundle.getInt(KEY_ICON), bundle.getCharSequence(KEY_TITLE), (PendingIntent) bundle.getParcelable(KEY_ACTION_INTENT), bundle.getBundle(KEY_EXTRAS), fromBundleArray(getBundleArrayFromBundle(bundle, KEY_REMOTE_INPUTS)), fromBundleArray(getBundleArrayFromBundle(bundle, KEY_DATA_ONLY_REMOTE_INPUTS)), allowGeneratedReplies);
    }

    static Bundle getBundleForAction(NotificationCompat.Action action) {
        Bundle actionExtras;
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_ICON, action.getIcon());
        bundle.putCharSequence(KEY_TITLE, action.getTitle());
        bundle.putParcelable(KEY_ACTION_INTENT, action.getActionIntent());
        if (action.getExtras() != null) {
            actionExtras = new Bundle(action.getExtras());
        } else {
            actionExtras = new Bundle();
        }
        actionExtras.putBoolean(EXTRA_ALLOW_GENERATED_REPLIES, action.getAllowGeneratedReplies());
        bundle.putBundle(KEY_EXTRAS, actionExtras);
        bundle.putParcelableArray(KEY_REMOTE_INPUTS, toBundleArray(action.getRemoteInputs()));
        return bundle;
    }

    private static RemoteInput fromBundle(Bundle data) {
        ArrayList<String> allowedDataTypesAsList = data.getStringArrayList(KEY_ALLOWED_DATA_TYPES);
        Set<String> allowedDataTypes = new HashSet<>();
        if (allowedDataTypesAsList != null) {
            for (String type : allowedDataTypesAsList) {
                allowedDataTypes.add(type);
            }
        }
        return new RemoteInput(data.getString(KEY_RESULT_KEY), data.getCharSequence(KEY_LABEL), data.getCharSequenceArray(KEY_CHOICES), data.getBoolean(KEY_ALLOW_FREE_FORM_INPUT), data.getBundle(KEY_EXTRAS), allowedDataTypes);
    }

    private static Bundle toBundle(RemoteInput remoteInput) {
        Bundle data = new Bundle();
        data.putString(KEY_RESULT_KEY, remoteInput.getResultKey());
        data.putCharSequence(KEY_LABEL, remoteInput.getLabel());
        data.putCharSequenceArray(KEY_CHOICES, remoteInput.getChoices());
        data.putBoolean(KEY_ALLOW_FREE_FORM_INPUT, remoteInput.getAllowFreeFormInput());
        data.putBundle(KEY_EXTRAS, remoteInput.getExtras());
        Set<String> allowedDataTypes = remoteInput.getAllowedDataTypes();
        if (allowedDataTypes != null && (!allowedDataTypes.isEmpty())) {
            ArrayList<String> allowedDataTypesAsList = new ArrayList<>(allowedDataTypes.size());
            for (String type : allowedDataTypes) {
                allowedDataTypesAsList.add(type);
            }
            data.putStringArrayList(KEY_ALLOWED_DATA_TYPES, allowedDataTypesAsList);
        }
        return data;
    }

    private static RemoteInput[] fromBundleArray(Bundle[] bundles) {
        if (bundles == null) {
            return null;
        }
        RemoteInput[] remoteInputs = new RemoteInput[bundles.length];
        for (int i = 0; i < bundles.length; i++) {
            remoteInputs[i] = fromBundle(bundles[i]);
        }
        return remoteInputs;
    }

    private static Bundle[] toBundleArray(RemoteInput[] remoteInputs) {
        if (remoteInputs == null) {
            return null;
        }
        Bundle[] bundles = new Bundle[remoteInputs.length];
        for (int i = 0; i < remoteInputs.length; i++) {
            bundles[i] = toBundle(remoteInputs[i]);
        }
        return bundles;
    }

    private static Bundle[] getBundleArrayFromBundle(Bundle bundle, String key) {
        Parcelable[] array = bundle.getParcelableArray(key);
        if ((array instanceof Bundle[]) || array == null) {
            return (Bundle[]) array;
        }
        Bundle[] typedArray = (Bundle[]) Arrays.copyOf(array, array.length, Bundle[].class);
        bundle.putParcelableArray(key, typedArray);
        return typedArray;
    }
}
