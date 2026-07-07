package com.onyx.android.eink.pen.demo.erase.util;

import com.onyx.android.eink.pen.demo.core.PenBundle;
import com.onyx.android.eink.pen.demo.erase.data.EraseTypes;
import com.onyx.android.sdk.pen.TouchHelper;
import com.onyx.android.sdk.pen.style.StrokeStyle;

/**
 * Erase track policy and SF stroke-style mapping.
 *
 * <ul>
 *   <li>Area erase: SF dash when track is enabled</li>
 *   <li>Move/stroke erase: SF soft-eraser on new firmware; app-layer circle on old firmware</li>
 *   <li>Track toggle off: no preview ({@link Layer#NONE})</li>
 * </ul>
 *
 */
public final class EraserTrackHelper {

    public enum Layer {
        /** SF raw-drawing preview (dash or soft-eraser). */
        SF,
        /** App bitmap/screen circle preview (old firmware move/stroke only). */
        APP,
        /** Track disabled by user. */
        NONE
    }

    private EraserTrackHelper() {
    }

    public static boolean supportsMoveStrokeSfTrack() {
        return EraserTrackCapability.supportsMoveStrokeSfTrack();
    }

    public static boolean defaultTrackEnabled(int eraseType) {
        switch (eraseType) {
            case EraseTypes.ERASER_AREA:
                return true;
            case EraseTypes.ERASER_MOVE:
            case EraseTypes.ERASER_STROKE:
                return supportsMoveStrokeSfTrack();
            default:
                return false;
        }
    }

    public static Layer resolveLayer(PenBundle bundle, int eraseType) {
        if (!bundle.isDisplayEraseTrack(eraseType)) {
            return Layer.NONE;
        }
        if (eraseType == EraseTypes.ERASER_AREA) {
            return Layer.SF;
        }
        if (EraseTypes.isMoveOrStrokeErase(eraseType)) {
            return supportsMoveStrokeSfTrack() ? Layer.SF : Layer.APP;
        }
        return Layer.NONE;
    }

    public static boolean useSfTrack(PenBundle bundle, int eraseType) {
        return resolveLayer(bundle, eraseType) == Layer.SF;
    }

    public static boolean useAppTrack(PenBundle bundle, int eraseType) {
        return resolveLayer(bundle, eraseType) == Layer.APP;
    }

    public static boolean shouldForceRawDrawing(PenBundle bundle, int eraseType) {
        return useSfTrack(bundle, eraseType);
    }

    public static int eraserStrokeStyle(int eraseType) {
        if (EraseTypes.isMoveOrStrokeErase(eraseType)) {
            return StrokeStyle.SOFT_ERASER;
        }
        return TouchHelper.STROKE_STYLE_DASH;
    }
}
