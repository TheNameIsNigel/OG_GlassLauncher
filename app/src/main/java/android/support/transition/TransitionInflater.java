package android.support.transition;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.support.annotation.NonNull;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v4.util.ArrayMap;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.InflateException;
import android.view.ViewGroup;
import java.io.IOException;
import java.lang.reflect.Constructor;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class TransitionInflater {
    private static final ArrayMap<String, Constructor> CONSTRUCTORS = new ArrayMap<>();
    private static final Class<?>[] CONSTRUCTOR_SIGNATURE = {Context.class, AttributeSet.class};
    private final Context mContext;

    private TransitionInflater(@NonNull Context context) {
        this.mContext = context;
    }

    public static TransitionInflater from(Context context) {
        return new TransitionInflater(context);
    }

    public Transition inflateTransition(int resource) {
        XmlResourceParser parser = this.mContext.getResources().getXml(resource);
        try {
            Transition createTransitionFromXml = createTransitionFromXml(parser, Xml.asAttributeSet(parser), (Transition) null);
            parser.close();
            return createTransitionFromXml;
        } catch (XmlPullParserException e) {
            throw new InflateException(e.getMessage(), e);
        } catch (IOException e2) {
            throw new InflateException(parser.getPositionDescription() + ": " + e2.getMessage(), e2);
        } catch (Throwable th) {
            parser.close();
            throw th;
        }
    }

    public TransitionManager inflateTransitionManager(int resource, ViewGroup sceneRoot) {
        XmlResourceParser parser = this.mContext.getResources().getXml(resource);
        try {
            TransitionManager createTransitionManagerFromXml = createTransitionManagerFromXml(parser, Xml.asAttributeSet(parser), sceneRoot);
            parser.close();
            return createTransitionManagerFromXml;
        } catch (XmlPullParserException e) {
            InflateException ex = new InflateException(e.getMessage());
            ex.initCause(e);
            throw ex;
        } catch (IOException e2) {
            InflateException ex2 = new InflateException(parser.getPositionDescription() + ": " + e2.getMessage());
            ex2.initCause(e2);
            throw ex2;
        } catch (Throwable th) {
            parser.close();
            throw th;
        }
    }

    private Transition createTransitionFromXml(XmlPullParser parser, AttributeSet attrs, Transition parent) throws XmlPullParserException, IOException {
        Transition transition = null;
        int depth = parser.getDepth();
        TransitionSet transitionSet = parent instanceof TransitionSet ? (TransitionSet) parent : null;
        while (true) {
            int type = parser.next();
            if ((type != 3 || parser.getDepth() > depth) && type != 1) {
                if (type == 2) {
                    String name = parser.getName();
                    if ("fade".equals(name)) {
                        transition = new Fade(this.mContext, attrs);
                    } else if ("changeBounds".equals(name)) {
                        transition = new ChangeBounds(this.mContext, attrs);
                    } else if ("slide".equals(name)) {
                        transition = new Slide(this.mContext, attrs);
                    } else if ("explode".equals(name)) {
                        transition = new Explode(this.mContext, attrs);
                    } else if ("changeImageTransform".equals(name)) {
                        transition = new ChangeImageTransform(this.mContext, attrs);
                    } else if ("changeTransform".equals(name)) {
                        transition = new ChangeTransform(this.mContext, attrs);
                    } else if ("changeClipBounds".equals(name)) {
                        transition = new ChangeClipBounds(this.mContext, attrs);
                    } else if ("autoTransition".equals(name)) {
                        transition = new AutoTransition(this.mContext, attrs);
                    } else if ("changeScroll".equals(name)) {
                        transition = new ChangeScroll(this.mContext, attrs);
                    } else if ("transitionSet".equals(name)) {
                        transition = new TransitionSet(this.mContext, attrs);
                    } else if ("transition".equals(name)) {
                        transition = (Transition) createCustom(attrs, Transition.class, "transition");
                    } else if ("targets".equals(name)) {
                        getTargetIds(parser, attrs, parent);
                    } else if ("arcMotion".equals(name)) {
                        if (parent == null) {
                            throw new RuntimeException("Invalid use of arcMotion element");
                        }
                        parent.setPathMotion(new ArcMotion(this.mContext, attrs));
                    } else if ("pathMotion".equals(name)) {
                        if (parent == null) {
                            throw new RuntimeException("Invalid use of pathMotion element");
                        }
                        parent.setPathMotion((PathMotion) createCustom(attrs, PathMotion.class, "pathMotion"));
                    } else if (!"patternPathMotion".equals(name)) {
                        throw new RuntimeException("Unknown scene name: " + parser.getName());
                    } else if (parent == null) {
                        throw new RuntimeException("Invalid use of patternPathMotion element");
                    } else {
                        parent.setPathMotion(new PatternPathMotion(this.mContext, attrs));
                    }
                    if (transition == null) {
                        continue;
                    } else {
                        if (!parser.isEmptyElementTag()) {
                            createTransitionFromXml(parser, attrs, transition);
                        }
                        if (transitionSet != null) {
                            transitionSet.addTransition(transition);
                            transition = null;
                        } else if (parent != null) {
                            throw new InflateException("Could not add transition to another transition.");
                        }
                    }
                }
            }
        }
        return transition;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0064, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0089, code lost:
        throw new android.view.InflateException("Could not instantiate " + r10 + " class " + r1, r3);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.Object createCustom(android.util.AttributeSet r9, java.lang.Class r10, java.lang.String r11) {
        /*
            r8 = this;
            r5 = 0
            java.lang.String r4 = "class"
            java.lang.String r1 = r9.getAttributeValue(r5, r4)
            if (r1 != 0) goto L_0x0024
            android.view.InflateException r4 = new android.view.InflateException
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.StringBuilder r5 = r5.append(r11)
            java.lang.String r6 = " tag must have a 'class' attribute"
            java.lang.StringBuilder r5 = r5.append(r6)
            java.lang.String r5 = r5.toString()
            r4.<init>(r5)
            throw r4
        L_0x0024:
            android.support.v4.util.ArrayMap<java.lang.String, java.lang.reflect.Constructor> r5 = CONSTRUCTORS     // Catch:{ Exception -> 0x0064 }
            monitor-enter(r5)     // Catch:{ Exception -> 0x0064 }
            android.support.v4.util.ArrayMap<java.lang.String, java.lang.reflect.Constructor> r4 = CONSTRUCTORS     // Catch:{ all -> 0x0061 }
            java.lang.Object r2 = r4.get(r1)     // Catch:{ all -> 0x0061 }
            java.lang.reflect.Constructor r2 = (java.lang.reflect.Constructor) r2     // Catch:{ all -> 0x0061 }
            if (r2 != 0) goto L_0x0050
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0061 }
            java.lang.ClassLoader r4 = r4.getClassLoader()     // Catch:{ all -> 0x0061 }
            java.lang.Class r4 = r4.loadClass(r1)     // Catch:{ all -> 0x0061 }
            java.lang.Class r0 = r4.asSubclass(r10)     // Catch:{ all -> 0x0061 }
            if (r0 == 0) goto L_0x0050
            java.lang.Class<?>[] r4 = CONSTRUCTOR_SIGNATURE     // Catch:{ all -> 0x0061 }
            java.lang.reflect.Constructor r2 = r0.getConstructor(r4)     // Catch:{ all -> 0x0061 }
            r4 = 1
            r2.setAccessible(r4)     // Catch:{ all -> 0x0061 }
            android.support.v4.util.ArrayMap<java.lang.String, java.lang.reflect.Constructor> r4 = CONSTRUCTORS     // Catch:{ all -> 0x0061 }
            r4.put(r1, r2)     // Catch:{ all -> 0x0061 }
        L_0x0050:
            r4 = 2
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ all -> 0x0061 }
            android.content.Context r6 = r8.mContext     // Catch:{ all -> 0x0061 }
            r7 = 0
            r4[r7] = r6     // Catch:{ all -> 0x0061 }
            r6 = 1
            r4[r6] = r9     // Catch:{ all -> 0x0061 }
            java.lang.Object r4 = r2.newInstance(r4)     // Catch:{ all -> 0x0061 }
            monitor-exit(r5)     // Catch:{ Exception -> 0x0064 }
            return r4
        L_0x0061:
            r4 = move-exception
            monitor-exit(r5)     // Catch:{ Exception -> 0x0064 }
            throw r4     // Catch:{ Exception -> 0x0064 }
        L_0x0064:
            r3 = move-exception
            android.view.InflateException r4 = new android.view.InflateException
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "Could not instantiate "
            java.lang.StringBuilder r5 = r5.append(r6)
            java.lang.StringBuilder r5 = r5.append(r10)
            java.lang.String r6 = " class "
            java.lang.StringBuilder r5 = r5.append(r6)
            java.lang.StringBuilder r5 = r5.append(r1)
            java.lang.String r5 = r5.toString()
            r4.<init>(r5, r3)
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.transition.TransitionInflater.createCustom(android.util.AttributeSet, java.lang.Class, java.lang.String):java.lang.Object");
    }

    private void getTargetIds(XmlPullParser parser, AttributeSet attrs, Transition transition) throws XmlPullParserException, IOException {
        int depth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if ((type == 3 && parser.getDepth() <= depth) || type == 1) {
                return;
            }
            if (type == 2) {
                if (parser.getName().equals("target")) {
                    TypedArray a = this.mContext.obtainStyledAttributes(attrs, Styleable.TRANSITION_TARGET);
                    int id = TypedArrayUtils.getNamedResourceId(a, parser, "targetId", 1, 0);
                    if (id != 0) {
                        transition.addTarget(id);
                    } else {
                        int id2 = TypedArrayUtils.getNamedResourceId(a, parser, "excludeId", 2, 0);
                        if (id2 != 0) {
                            transition.excludeTarget(id2, true);
                        } else {
                            String transitionName = TypedArrayUtils.getNamedString(a, parser, "targetName", 4);
                            if (transitionName != null) {
                                transition.addTarget(transitionName);
                            } else {
                                String transitionName2 = TypedArrayUtils.getNamedString(a, parser, "excludeName", 5);
                                if (transitionName2 != null) {
                                    transition.excludeTarget(transitionName2, true);
                                } else {
                                    String className = TypedArrayUtils.getNamedString(a, parser, "excludeClass", 3);
                                    if (className != null) {
                                        try {
                                            transition.excludeTarget(Class.forName(className), true);
                                        } catch (ClassNotFoundException e) {
                                            a.recycle();
                                            throw new RuntimeException("Could not create " + className, e);
                                        }
                                    } else {
                                        String className2 = TypedArrayUtils.getNamedString(a, parser, "targetClass", 0);
                                        if (className2 != null) {
                                            transition.addTarget(Class.forName(className2));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    a.recycle();
                } else {
                    throw new RuntimeException("Unknown scene name: " + parser.getName());
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0058, code lost:
        return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.support.transition.TransitionManager createTransitionManagerFromXml(org.xmlpull.v1.XmlPullParser r8, android.util.AttributeSet r9, android.view.ViewGroup r10) throws org.xmlpull.v1.XmlPullParserException, java.io.IOException {
        /*
            r7 = this;
            int r0 = r8.getDepth()
            r2 = 0
        L_0x0005:
            int r3 = r8.next()
            r4 = 3
            if (r3 != r4) goto L_0x0012
            int r4 = r8.getDepth()
            if (r4 <= r0) goto L_0x0058
        L_0x0012:
            r4 = 1
            if (r3 == r4) goto L_0x0058
            r4 = 2
            if (r3 != r4) goto L_0x0005
            java.lang.String r1 = r8.getName()
            java.lang.String r4 = "transitionManager"
            boolean r4 = r1.equals(r4)
            if (r4 == 0) goto L_0x002b
            android.support.transition.TransitionManager r2 = new android.support.transition.TransitionManager
            r2.<init>()
            goto L_0x0005
        L_0x002b:
            java.lang.String r4 = "transition"
            boolean r4 = r1.equals(r4)
            if (r4 == 0) goto L_0x003a
            if (r2 == 0) goto L_0x003a
            r7.loadTransition(r9, r8, r10, r2)
            goto L_0x0005
        L_0x003a:
            java.lang.RuntimeException r4 = new java.lang.RuntimeException
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "Unknown scene name: "
            java.lang.StringBuilder r5 = r5.append(r6)
            java.lang.String r6 = r8.getName()
            java.lang.StringBuilder r5 = r5.append(r6)
            java.lang.String r5 = r5.toString()
            r4.<init>(r5)
            throw r4
        L_0x0058:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.transition.TransitionInflater.createTransitionManagerFromXml(org.xmlpull.v1.XmlPullParser, android.util.AttributeSet, android.view.ViewGroup):android.support.transition.TransitionManager");
    }

    private void loadTransition(AttributeSet attrs, XmlPullParser parser, ViewGroup sceneRoot, TransitionManager transitionManager) throws Resources.NotFoundException {
        Transition transition;
        TypedArray a = this.mContext.obtainStyledAttributes(attrs, Styleable.TRANSITION_MANAGER);
        int transitionId = TypedArrayUtils.getNamedResourceId(a, parser, "transition", 2, -1);
        int fromId = TypedArrayUtils.getNamedResourceId(a, parser, "fromScene", 0, -1);
        Scene sceneForLayout = fromId < 0 ? null : Scene.getSceneForLayout(sceneRoot, fromId, this.mContext);
        int toId = TypedArrayUtils.getNamedResourceId(a, parser, "toScene", 1, -1);
        Scene sceneForLayout2 = toId < 0 ? null : Scene.getSceneForLayout(sceneRoot, toId, this.mContext);
        if (transitionId >= 0 && (transition = inflateTransition(transitionId)) != null) {
            if (sceneForLayout2 == null) {
                throw new RuntimeException("No toScene for transition ID " + transitionId);
            } else if (sceneForLayout == null) {
                transitionManager.setTransition(sceneForLayout2, transition);
            } else {
                transitionManager.setTransition(sceneForLayout, sceneForLayout2, transition);
            }
        }
        a.recycle();
    }
}
