package com.onyx.android.eink.pen.demo.core;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.onyx.android.eink.pen.demo.brush.shape.Shape;
import com.onyx.android.eink.pen.demo.brush.util.ShapeUtils;
import com.onyx.android.eink.pen.demo.core.PenCommands;
import com.onyx.android.eink.pen.demo.erase.input.DrawEraseInputHandler;
import com.onyx.android.eink.pen.demo.event.ActivityFocusChangedEvent;
import com.onyx.android.eink.pen.demo.eventhandler.PenStateHandler;
import com.onyx.android.eink.pen.demo.receiver.GlobalDeviceReceiver;
import com.onyx.android.eink.pen.demo.ui.view.FloatingMenuDragHandler;
import com.onyx.android.sdk.pen.RawInputCallback;
import com.onyx.android.sdk.pen.data.TouchPointList;
import com.onyx.android.sdk.rx.RxFilter;
import com.onyx.android.sdk.utils.BroadcastHelper;
import com.onyx.android.sdk.utils.EventBusUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Single integration entry for pen write/erase on a {@link SurfaceView}.
 */
public final class PenSession {

    private static final String ONYX_ACTION_REQUIRE_FLOAT_BUTTON_STATUS = "onyx.action.REQUIRE_FLOAT_BUTTON_STATUS";
    private static final String ARGS_STATUS = "args_status";

    private final Activity activity;
    private final PenStateHandler penStateHandler = new PenStateHandler();
    private final GlobalDeviceReceiver deviceReceiver = new GlobalDeviceReceiver();
    private final RxFilter<Boolean> surfaceChangedFilter = new RxFilter<>();

    private DrawEraseInputHandler drawEraseInputHandler;
    private FloatingMenuDragHandler dragHandler;
    private SurfaceView pendingSurfaceView;
    private View pendingExcludeOverlay;
    private boolean isInit;

    private PenSession(@NonNull Activity activity) {
        this.activity = activity;
    }

    public static PenSession create(@NonNull Activity activity) {
        return new PenSession(activity);
    }

    public void initialize() {
        if (isInit) {
            return;
        }
        isInit = true;
        deviceReceiver.enable(activity, true);
        setFloatButtonStatusBroadcast(true);
        EventBusUtils.ensureRegister(getPenManager().getEventBus(), activity);
        EventBusUtils.ensureRegister(getPenManager().getEventBus(), penStateHandler);
        subscribeSurfaceChanged();
    }

    @NonNull
    public PenTool getCurrentTool() {
        return PenTool.fromBundle(getPenBundle());
    }

    public void setTool(@NonNull PenTool tool) {
        if (tool == PenTool.BRUSH) {
            getPenBundle().setErasing(false);
        } else {
            getPenBundle().setErasing(true);
            getPenBundle().setCurrentEraseType(tool.toEraseType());
        }
        applyToolSwitch();
    }

    public void setStrokeWidthMm(int shapeType, float widthMm) {
        PenCommands.applyStrokeWidth(getPenBundle(), getPenManager(), shapeType, widthMm);
    }

    public void setStrokeColor(int color) {
        PenCommands.applyStrokeColor(getPenBundle(), getPenManager(), color);
    }

    public void setShapeType(int shapeType, int texture) {
        PenCommands.applyStrokeStyle(getPenBundle(), getPenManager(), shapeType, texture)
                .subscribe();
    }

    public void setPenUpRefreshEnabled(boolean enable) {
        getPenBundle().setEnablePenUpRefresh(enable);
        refreshScreen();
    }

    public void setDisplayEraseTrack(int eraseType, boolean display) {
        getPenBundle().setDisplayEraseTrack(eraseType, display);
        if (getPenBundle().isErasing()
                && getPenBundle().getCurrentEraseType() == eraseType) {
            applyToolSwitch();
        } else {
            refreshScreen();
        }
    }

    public boolean isDisplayEraseTrack(int eraseType) {
        return getPenBundle().isDisplayEraseTrack(eraseType);
    }

    @NonNull
    public SurfaceHolder.Callback createSurfaceCallback(@NonNull SurfaceView surfaceView,
                                                        @Nullable View excludeOverlay) {
        return new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                holder.setFormat(PixelFormat.TRANSLUCENT);
                attachSurface(surfaceView, excludeOverlay);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                attachSurface(surfaceView, excludeOverlay);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                PenCommands.onHostSurfaceDestroyed(getPenManager());
            }
        };
    }

    public void attachSurface(@NonNull SurfaceView surfaceView, @Nullable View excludeOverlay) {
        surfaceChangedFilter.onNext(true);
        pendingSurfaceView = surfaceView;
        pendingExcludeOverlay = excludeOverlay;
    }

    public void onPause() {
        penStateHandler.onActivityPause();
        penStateHandler.pauseFromLifecycle(activity.getClass().getSimpleName());
        PenCommands.pauseHostSession(getPenManager());
    }

    public void onResume() {
        penStateHandler.onActivityResume();
        penStateHandler.resumeFromLifecycle(activity.getClass().getSimpleName());
        if (pendingSurfaceView != null
                && pendingSurfaceView.getWidth() > 0
                && pendingSurfaceView.getHeight() > 0) {
            surfaceChangedFilter.onNext(true);
        }
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        EventBusUtils.safelyPostEvent(getPenBundle().getEventBus(), new ActivityFocusChangedEvent(hasFocus));
    }

    public void onDestroy() {
        getPenManager().resetToBrushPenAttrs(getPenBundle());
        getPenManager().destroy();
        getPenBundle().resetInteractiveStateOnSessionEnd();
        surfaceChangedFilter.dispose();
        deviceReceiver.enable(activity, false);
        setFloatButtonStatusBroadcast(false);
        EventBusUtils.ensureUnregister(getPenManager().getEventBus(), penStateHandler);
        EventBusUtils.ensureUnregister(getPenManager().getEventBus(), activity);
        penStateHandler.quit();
        isInit = false;
    }

    public void applyToolSwitch() {
        PenCommands.applyToolSwitch(getPenManager(), penStateHandler);
    }

    public void refreshScreen() {
        penStateHandler.refreshScreen();
    }

    @NonNull
    public RawInputCallback getInputCallback() {
        if (drawEraseInputHandler == null) {
            drawEraseInputHandler = new DrawEraseInputHandler(
                    getPenBundle(),
                    getPenManager(),
                    penStateHandler,
                    getPenBundle().getPenExecutor(),
                    this::commitStroke,
                    this::notifyShapesChanged);
        }
        return drawEraseInputHandler;
    }

    @NonNull
    public PenBundle getPenBundle() {
        return PenBundle.getInstance();
    }

    @NonNull
    public PenManager getPenManager() {
        return getPenBundle().getPenManager();
    }

    private void commitStroke(TouchPointList touchPointList) {
        int shapeType = getPenBundle().getCurrentShapeType();
        Shape shape = ShapeUtils.createShape(getPenBundle(), shapeType, touchPointList);
        PenCommands.run(getPenManager(), pm -> pm.commitStroke(shape));
    }

    private void notifyShapesChanged() {
        // Hook for ContentListener integration.
    }

    private void subscribeSurfaceChanged() {
        surfaceChangedFilter.dispose();
        surfaceChangedFilter.subscribeThrottleLast(300, ignored -> performAttach());
    }

    private void performAttach() {
        if (pendingSurfaceView == null) {
            return;
        }
        PenCommands.runAsync(getPenManager(), pm -> pm.attachHost(
                        pendingSurfaceView,
                        pendingExcludeOverlay,
                        activity.hasWindowFocus(),
                        getInputCallback()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pm -> {
                    penStateHandler.refreshScreen();
                    if (pendingExcludeOverlay != null) {
                        dragHandler = new FloatingMenuDragHandler(pendingExcludeOverlay)
                                .setLimitRect(getPenManager().getLimitNoteRect());
                        pendingExcludeOverlay.setOnTouchListener(dragHandler);
                    }
                });
    }

    private void setFloatButtonStatusBroadcast(boolean enable) {
        Intent intent = new Intent(ONYX_ACTION_REQUIRE_FLOAT_BUTTON_STATUS);
        intent.putExtra(ARGS_STATUS, enable);
        BroadcastHelper.sendBroadcast(activity, intent);
    }
}
