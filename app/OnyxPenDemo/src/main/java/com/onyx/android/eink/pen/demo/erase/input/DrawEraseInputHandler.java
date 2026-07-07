package com.onyx.android.eink.pen.demo.erase.input;

import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.onyx.android.eink.pen.demo.core.PenBundle;
import com.onyx.android.eink.pen.demo.core.PenCommands;
import com.onyx.android.eink.pen.demo.core.PenExecutor;
import com.onyx.android.eink.pen.demo.core.PenManager;
import com.onyx.android.eink.pen.demo.erase.EraseController;
import com.onyx.android.eink.pen.demo.erase.bean.EraseContext;
import com.onyx.android.eink.pen.demo.erase.data.EraseTypes;
import com.onyx.android.eink.pen.demo.event.PenEvent;
import com.onyx.android.eink.pen.demo.eventhandler.PenStateHandler;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.RawInputCallback;
import com.onyx.android.sdk.pen.data.TouchPointList;
import com.onyx.android.sdk.rx.ObservableHolder;

import java.util.ArrayList;
import java.util.List;

public class DrawEraseInputHandler extends RawInputCallback {

    public interface ShapeCommitCallback {
        void onCommitShape(TouchPointList touchPointList);
    }

    public interface EraseFinishedCallback {
        void onEraseFinished();
    }

    private final PenBundle penBundle;
    private final PenManager penManager;
    private final EraseController eraseController;
    private final ShapeCommitCallback shapeCommitCallback;
    private final EraseFinishedCallback eraseFinishedCallback;

    private ObservableHolder<TouchPoint> eraseObservable;
    private EraseContext eraseContext;
    private RectF pendingPenUpRefreshRect;
    private boolean drawingStrokeCommitted;
    private boolean capEraseInBrushMode;
    private boolean eraseParamsReady;
    private final List<TouchPoint> pendingEraseMoves = new ArrayList<>();

    public DrawEraseInputHandler(@NonNull PenBundle penBundle,
                                 @NonNull PenManager penManager,
                                 @NonNull PenStateHandler penStateHandler,
                                 @NonNull PenExecutor penExecutor,
                                 @NonNull ShapeCommitCallback shapeCommitCallback,
                                 @Nullable EraseFinishedCallback eraseFinishedCallback) {
        this.penBundle = penBundle;
        this.penManager = penManager;
        this.eraseController = new EraseController(penBundle, penManager, penStateHandler, penExecutor);
        this.shapeCommitCallback = shapeCommitCallback;
        this.eraseFinishedCallback = eraseFinishedCallback;
    }

    @Override
    public void onBeginRawDrawing(boolean shortcutDrawing, TouchPoint touchPoint) {
        if (penBundle.isErasing()) {
            onBeginRawErasing(false, touchPoint);
            return;
        }
        drawingStrokeCommitted = false;
        pendingPenUpRefreshRect = null;
    }

    @Override
    public void onEndRawDrawing(boolean outLimitRegion, TouchPoint touchPoint) {
        if (penBundle.isErasing()) {
            onEndRawErasing(outLimitRegion, touchPoint);
        }
    }

    @Override
    public void onRawDrawingTouchPointMoveReceived(TouchPoint touchPoint) {
        if (penBundle.isErasing()) {
            onRawErasingTouchPointMoveReceived(touchPoint);
        }
    }

    @Override
    public void onRawDrawingTouchPointListReceived(TouchPointList touchPointList) {
        if (penBundle.isErasing()) {
            onRawErasingTouchPointListReceived(touchPointList);
            return;
        }
        shapeCommitCallback.onCommitShape(touchPointList);
        drawingStrokeCommitted = true;
        flushPendingPenUpRefresh();
    }

    @Override
    public void onBeginRawErasing(boolean shortcutErasing, TouchPoint point) {
        capEraseInBrushMode = !penBundle.isErasing();
        removeEraseObserver();
        eraseParamsReady = false;
        pendingEraseMoves.clear();
        if (penBundle.getCurrentEraseType() == EraseTypes.ERASER_AREA) {
            eraseContext = eraseController.begin(point, shortcutErasing, capEraseInBrushMode, null);
            eraseParamsReady = true;
            return;
        }
        eraseContext = eraseController.begin(
                point, shortcutErasing, capEraseInBrushMode, () -> onEraseParamsReady(point));
    }

    @Override
    public void onEndRawErasing(boolean outLimitRegion, TouchPoint point) {
        boolean wasCapInBrush = capEraseInBrushMode;
        capEraseInBrushMode = false;
        if (wasCapInBrush) {
            penManager.getEventBus().post(PenEvent.pauseDrawingRender());
        }
    }

    @Override
    public void onRawErasingTouchPointMoveReceived(TouchPoint point) {
        if (eraseContext != null) {
            eraseContext.addErasePoint(point);
        }
        if (penBundle.getCurrentEraseType() == EraseTypes.ERASER_AREA) {
            return;
        }
        if (!eraseParamsReady) {
            pendingEraseMoves.add(new TouchPoint(point));
            return;
        }
        dispatchEraseMove(point);
    }

    @Override
    public void onRawErasingTouchPointListReceived(TouchPointList pointList) {
        if (eraseContext != null) {
            eraseContext.addErasePoints(pointList);
            eraseContext.setFinishing(true);
        }
        removeEraseObserver();
        EraseContext finishingContext = eraseContext;
        eraseController.finish(finishingContext, () -> {
            eraseContext = null;
            eraseParamsReady = false;
            pendingEraseMoves.clear();
            if (eraseFinishedCallback != null) {
                eraseFinishedCallback.onEraseFinished();
            }
        });
    }

    @Override
    public void onPenUpRefresh(RectF refreshRect) {
        if (!penBundle.isEnablePenUpRefresh()) {
            return;
        }
        if (penBundle.isErasing() || capEraseInBrushMode || eraseContext != null) {
            return;
        }
        if (drawingStrokeCommitted) {
            performPenUpRefresh(refreshRect);
        } else {
            pendingPenUpRefreshRect = new RectF(refreshRect);
        }
    }

    private void onEraseParamsReady(TouchPoint downPoint) {
        EraseContext context = eraseContext;
        if (context == null || context.isFinishing()) {
            return;
        }
        eraseParamsReady = true;
        if (penBundle.getCurrentEraseType() != EraseTypes.ERASER_AREA) {
            eraseObservable = eraseController.openMoveEraseBuffer(context);
            TouchPointList firstPoints = new TouchPointList();
            firstPoints.add(new TouchPoint(downPoint));
            eraseController.onErasing(firstPoints, context);
            for (TouchPoint pending : pendingEraseMoves) {
                dispatchEraseMove(pending);
            }
            pendingEraseMoves.clear();
        }
    }

    private void dispatchEraseMove(TouchPoint point) {
        if (eraseObservable != null) {
            eraseObservable.onNext(point);
        }
    }

    private void flushPendingPenUpRefresh() {
        if (pendingPenUpRefreshRect != null) {
            performPenUpRefresh(pendingPenUpRefreshRect);
            pendingPenUpRefreshRect = null;
        }
    }

    private void performPenUpRefresh(RectF refreshRect) {
        PenCommands.run(penManager, pm -> pm.refreshPartial(refreshRect));
    }

    private void removeEraseObserver() {
        if (eraseObservable != null) {
            eraseObservable.dispose();
        }
        eraseObservable = null;
    }
}
