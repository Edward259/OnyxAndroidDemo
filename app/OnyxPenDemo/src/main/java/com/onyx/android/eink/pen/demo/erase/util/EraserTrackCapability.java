package com.onyx.android.eink.pen.demo.erase.util;

import com.onyx.android.sdk.utils.ReflectUtil;

import java.lang.reflect.Method;

final class EraserTrackCapability {

    private static final String VIEW_UPDATE_HELPER_CLASS = "android.onyx.ViewUpdateHelper";
    private static final String METHOD_SET_ERASER_RAW_DRAWING_ENABLED =
            "setEraserRawDrawingEnabled";

    private static volatile Boolean supportsMoveStrokeSfTrack;

    private EraserTrackCapability() {
    }

    static boolean supportsMoveStrokeSfTrack() {
        Boolean cached = supportsMoveStrokeSfTrack;
        if (cached != null) {
            return cached;
        }
        synchronized (EraserTrackCapability.class) {
            cached = supportsMoveStrokeSfTrack;
            if (cached != null) {
                return cached;
            }
            cached = probeSupportsMoveStrokeSfTrack();
            supportsMoveStrokeSfTrack = cached;
            return cached;
        }
    }

    private static boolean probeSupportsMoveStrokeSfTrack() {
        Class<?> viewUpdateHelperClass = ReflectUtil.classForName(VIEW_UPDATE_HELPER_CLASS);
        if (viewUpdateHelperClass == null) {
            return false;
        }
        Method method = ReflectUtil.getMethodSafely(
                viewUpdateHelperClass,
                METHOD_SET_ERASER_RAW_DRAWING_ENABLED,
                boolean.class,

                int.class);
        return method != null;
    }
}
