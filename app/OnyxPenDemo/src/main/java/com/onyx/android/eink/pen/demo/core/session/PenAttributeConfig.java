package com.onyx.android.eink.pen.demo.core.session;

import android.graphics.Color;

import androidx.annotation.WorkerThread;

import com.onyx.android.eink.pen.demo.core.PenBundle;
import com.onyx.android.eink.pen.demo.brush.data.ShapeFactory;
import com.onyx.android.eink.pen.demo.erase.data.EraseTypes;
import com.onyx.android.eink.pen.demo.erase.util.EraserTrackHelper;
import com.onyx.android.sdk.data.PenConstant;
import com.onyx.android.sdk.device.Device;
import com.onyx.android.sdk.pen.TouchHelper;
import com.onyx.android.sdk.pen.style.StrokeStyle;
import com.onyx.android.sdk.utils.ResManager;

/**
 * Applies brush / erase TouchHelper and Device stroke parameters from {@link PenBundle} state.
 */
public final class PenAttributeConfig {

    private final TouchHelperSession session;
    private static final Float ERASE_PEN_OPACITY = 0.5F;
    private static final Float ERASE_PEN_BLACK_OPACITY = 0.1F;

    public PenAttributeConfig(TouchHelperSession session) {
        this.session = session;
    }

    @WorkerThread
    public void applyPenAttrsFromBundle(PenBundle penBundle) {
        if (session.getTouchHelper() == null) {
            return;
        }
        if (!penBundle.isErasing()) {
            applyBrushTouchHelperParams(penBundle);
        } else {
            applyEraseTouchHelperParams(penBundle);
        }
        warmDashDeviceParameters(penBundle);
        if (!penBundle.isErasing()) {
            applyCapEraseStrokeConfig(penBundle);
        } else {
            applyEraseResumeAttrs(penBundle);
            if (shouldForceRawDrawingInEraseMode(penBundle)) {
                session.forceRawDrawingEnabled();
            }
        }
    }

    @WorkerThread
    public void resetToBrushPenAttrs(PenBundle penBundle) {
        if (session.getTouchHelper() == null) {
            return;
        }
        applyBrushTouchHelperParams(penBundle);
        warmDashDeviceParameters(penBundle);
        applyCapEraseStrokeConfig(penBundle);
    }

    @WorkerThread
    public void applyAreaErasePreviewParams() {
        PenBundle penBundle = PenBundle.getInstance();
        if (EraserTrackHelper.useSfTrack(penBundle, EraseTypes.ERASER_AREA)) {
            applySfAreaTouchHelperParams();
            warmDashDeviceParameters(penBundle);
            applyCapEraseStrokeConfig(penBundle);
            session.forceRawDrawingEnabled();
        } else {
            applyLegacyEraseTouchHelperParams();
            warmDashDeviceParameters(penBundle);
            session.setRawInputReaderEnable(true);
        }
    }

    @WorkerThread
    public void applyAreaEraseCapPreviewParams() {
        PenBundle penBundle = PenBundle.getInstance();
        if (EraserTrackHelper.useSfTrack(penBundle, EraseTypes.ERASER_AREA)) {
            warmDashDeviceParameters(penBundle);
            applyCapEraseStrokeConfig(penBundle);
            session.setRawDrawingRenderEnabled(true);
            session.forceRawDrawingEnabled();
        } else {
            applyLegacyEraseTouchHelperParams();
            warmDashDeviceParameters(penBundle);
            session.setRawDrawingRenderEnabled(false);
        }
    }

    @WorkerThread
    public void applyStrokeMoveErasePreviewParams() {
        PenBundle penBundle = PenBundle.getInstance();
        applyStrokeMoveEraseTouchHelperParams(penBundle);
        warmDashDeviceParameters(penBundle);
        applyCapEraseStrokeConfig(penBundle);
        if (shouldForceRawDrawingInEraseMode(penBundle)) {
            session.forceRawDrawingEnabled();
        }
    }

    @WorkerThread
    public void applyStrokeMoveEraseCapPreviewParams() {
        PenBundle penBundle = PenBundle.getInstance();
        if (EraserTrackHelper.useSfTrack(penBundle, penBundle.getCurrentEraseType())) {
            applySfMoveStrokeTouchHelperParams(penBundle);
        }
        warmDashDeviceParameters(penBundle);
        applyCapEraseStrokeConfig(penBundle);
        session.setRawDrawingRenderEnabled(true);
        session.forceRawDrawingEnabled();
    }

    @WorkerThread
    public void restartRawPenSession(PenBundle penBundle) {
        if (session.getTouchHelper() == null) {
            return;
        }
        session.restartRawDrawing();
        applyPenAttrsFromBundle(penBundle);
        session.setRawInputReaderEnable(true);
        if (!penBundle.isErasing()) {
            session.forceSetRawDrawingEnabled(true);
            return;
        }
        if (shouldForceRawDrawingInEraseMode(penBundle)) {
            session.forceSetRawDrawingEnabled(true);
            return;
        }
        session.setRawInputReaderEnable(false);
        session.setRawInputReaderEnable(true);
    }

    @WorkerThread
    public void restoreRawPenInput(PenBundle penBundle) {
        if (session.getTouchHelper() == null) {
            return;
        }
        if (!session.isRawDrawingCreated()) {
            restartRawPenSession(penBundle);
            return;
        }
        applyPenAttrsFromBundle(penBundle);
        warmDashDeviceParameters(penBundle);
        session.setRawInputReaderEnable(true);
        if (!penBundle.isErasing()) {
            session.forceSetRawDrawingEnabled(true);
        } else if (shouldForceRawDrawingInEraseMode(penBundle)) {
            session.forceSetRawDrawingEnabled(true);
        } else {
            session.setRawInputReaderEnable(false);
            session.setRawInputReaderEnable(true);
        }
    }

    @WorkerThread
    public void applyCapEraseStrokeConfig(PenBundle penBundle) {
        int eraseType = penBundle.getCurrentEraseType();
        session.setEraserRawDrawingEnabled(
                EraserTrackHelper.useSfTrack(penBundle, eraseType),
                EraserTrackHelper.eraserStrokeStyle(eraseType));
    }

    @WorkerThread
    public void setErasePathDrawing(boolean drawing, int eraseType) {
        session.setEraserRawDrawingEnabled(drawing, EraserTrackHelper.eraserStrokeStyle(eraseType));
    }

    @WorkerThread
    private void applySfAreaTouchHelperParams() {
        session.setBrushRawDrawing(true);
        session.setStrokeStyle(TouchHelper.STROKE_STYLE_DASH);
        session.setStrokeWidthPx(PenConstant.DASH_STROKE_WIDTH);
        session.setStrokeColor(Color.BLACK);
        session.setRawDrawingRenderEnabled(true);
        session.setRawDrawingEnabled(true);
    }

    @WorkerThread
    private void applyEraseTouchHelperParams(PenBundle penBundle) {
        int eraseType = penBundle.getCurrentEraseType();
        if (!EraserTrackHelper.useSfTrack(penBundle, eraseType)) {
            applyLegacyEraseTouchHelperParams();
            return;
        }
        if (eraseType == EraseTypes.ERASER_AREA) {
            applySfAreaTouchHelperParams();
        } else {
            applySfMoveStrokeTouchHelperParams(penBundle);
        }
    }

    @WorkerThread
    private void applyStrokeMoveEraseTouchHelperParams(PenBundle penBundle) {
        applyEraseTouchHelperParams(penBundle);
    }

    @WorkerThread
    private void applySfMoveStrokeTouchHelperParams(PenBundle penBundle) {
        int eraseType = penBundle.getCurrentEraseType();
        float eraseWidth = penBundle.getEraseWidth(eraseType);
        session.setBrushRawDrawing(true);
        session.setStrokeStyle(StrokeStyle.SOFT_ERASER);
        session.setStrokeWidthPx(eraseWidth);
        session.setStrokeColor(Color.BLACK);
        session.setRawDrawingRenderEnabled(true);
        session.setRawDrawingEnabled(true);
        session.setRawInputReaderEnable(true);
        session.setEraserRawDrawingEnabled(true, StrokeStyle.SOFT_ERASER);
    }

    @WorkerThread
    private void applyLegacyEraseTouchHelperParams() {
        session.setEraserRawDrawingEnabled(false, StrokeStyle.SOFT_ERASER);
        session.setBrushRawDrawing(false);
        session.setRawDrawingRenderEnabled(false);
        session.setRawInputReaderEnable(true);
    }

    @WorkerThread
    private void applyBrushTouchHelperParams(PenBundle penBundle) {
        session.setBrushRawDrawing(true);
        session.setStrokeStyle(ShapeFactory.getStrokeStyle(
                penBundle.getCurrentShapeType(), penBundle.getCurrentTexture()));
        session.setStrokeWidthMm(penBundle.getCurrentStrokeWidth(),
                ResManager.getAppContext().getResources().getDisplayMetrics().densityDpi);
        session.setStrokeColor(penBundle.getCurrentStrokeColor());
        session.setRawDrawingRenderEnabled(true);
        session.setRawInputReaderEnable(true);
    }

    @WorkerThread
    private boolean shouldForceRawDrawingInEraseMode(PenBundle penBundle) {
        return EraserTrackHelper.shouldForceRawDrawing(penBundle, penBundle.getCurrentEraseType());
    }

    @WorkerThread
    private void warmDashDeviceParameters(PenBundle penBundle) {
        Device.currentDevice().setStrokeParameters(
                TouchHelper.STROKE_STYLE_DASH, new float[]{PenConstant.DASH_STROKE_WIDTH});
        int eraseType = penBundle.getCurrentEraseType();
        float eraseWidth = penBundle.getEraseWidth(
                EraseTypes.isMoveOrStrokeErase(eraseType) ? eraseType : EraseTypes.ERASER_MOVE);
        Device.currentDevice().setStrokeParameters(StrokeStyle.SOFT_ERASER,
                new float[]{eraseWidth, ERASE_PEN_OPACITY, ERASE_PEN_BLACK_OPACITY});
    }

    @WorkerThread
    public void applyEraseResumeAttrs(PenBundle penBundle) {
        if (!penBundle.isErasing()) {
            return;
        }
        int eraseType = penBundle.getCurrentEraseType();
        boolean sfTrack = EraserTrackHelper.useSfTrack(penBundle, eraseType);
        setErasePathDrawing(sfTrack, eraseType);
        if (!sfTrack) {
            session.setBrushRawDrawing(false);
        }
    }
}
