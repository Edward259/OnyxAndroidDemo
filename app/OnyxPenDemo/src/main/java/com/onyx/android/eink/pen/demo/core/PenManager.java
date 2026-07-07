package com.onyx.android.eink.pen.demo.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.onyx.android.eink.pen.demo.brush.shape.Shape;
import com.onyx.android.eink.pen.demo.core.session.PenAttributeConfig;
import com.onyx.android.eink.pen.demo.core.session.TouchHelperSession;
import com.onyx.android.eink.pen.demo.render.InteractiveMode;
import com.onyx.android.eink.pen.demo.render.RendererHelper;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.api.device.epd.UpdateMode;
import com.onyx.android.sdk.pen.RawInputCallback;
import com.onyx.android.sdk.pen.TouchHelper;
import com.onyx.android.sdk.rx.RxScheduler;
import com.onyx.android.sdk.utils.BitmapUtils;
import com.onyx.android.sdk.utils.ResManager;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

public class PenManager {

    private final EventBus eventBus;
    private final TouchHelperSession touchHelperSession = new TouchHelperSession();
    private final PenAttributeConfig attributeConfig = new PenAttributeConfig(touchHelperSession);

    private RxScheduler rxScheduler;
    private RendererHelper rendererHelper;
    private InteractiveMode currentMode = InteractiveMode.SCRIBBLE;
    private List<Shape> drawShape = new ArrayList<>();

    public PenManager(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @WorkerThread
    public void attachHost(@NonNull SurfaceView view, View floatMenuLayout,
                           boolean hostViewFocused, RawInputCallback callback) {
        attachHostView(view, floatMenuLayout, hostViewFocused, callback);
        setViewPoint(view);
        restoreDrawShapesToBitmap();
        applyPenAttrsFromBundle(PenBundle.getInstance());
    }

    @WorkerThread
    public void attachHostView(@NonNull SurfaceView view, View floatMenuLayout,
                               boolean hostViewFocused, RawInputCallback callback) {
        if (view.getWidth() == 0 || view.getHeight() == 0) {
            throw new IllegalStateException("can not start when view width or height is 0");
        }
        boolean preserveBitmap = touchHelperSession.getSurfaceView() != null
                && touchHelperSession.getSurfaceView() == view
                && BitmapUtils.isValid(getRenderContext().bitmap);
        if (touchHelperSession.isHostSurfaceAttached()
                && preserveBitmap
                && touchHelperSession.isHostSurfaceValid()) {
            return;
        }
        if (!preserveBitmap) {
            getRenderContext().bitmap = createBitmap(view);
            bindCanvasToBitmap();
        }
        touchHelperSession.bindHostView(view, callback);
        touchHelperSession.updateLimitRect(floatMenuLayout);
        touchHelperSession.restartRawDrawing();
        touchHelperSession.setHostSurfaceAttached(true);
        if (hostViewFocused) {
            touchHelperSession.forceSetRawDrawingEnabled(false);
        }
    }

    public void destroy() {
        getRenderContext().eraseArgs = null;
        getRenderContext().recycleBitmap();
        drawShape.clear();
        currentMode = InteractiveMode.SCRIBBLE;
        touchHelperSession.destroy();
    }

    public void setViewPoint(View renderView) {
        Point viewPoint = getRenderContext().viewPoint;
        if (viewPoint == null) {
            viewPoint = new Point();
            getRenderContext().viewPoint = viewPoint;
        }
        touchHelperSession.setViewPoint(renderView, viewPoint);
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        touchHelperSession.setSurfaceView(surfaceView);
    }

    public TouchHelper getTouchHelper() {
        return touchHelperSession.getTouchHelper();
    }

    public SurfaceView getSurfaceView() {
        return touchHelperSession.getSurfaceView();
    }

    public Rect getViewRect() {
        return touchHelperSession.getViewRect();
    }

    public Bitmap createBitmap(SurfaceView surfaceView) {
        if (surfaceView == null) {
            return null;
        }
        Rect limitRect = new Rect();
        surfaceView.getLocalVisibleRect(limitRect);
        Bitmap bitmap = Bitmap.createBitmap(limitRect.width(), limitRect.height(), Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.WHITE);
        return bitmap;
    }

    @WorkerThread
    private void bindCanvasToBitmap() {
        Bitmap bitmap = getRenderContext().bitmap;
        if (bitmap == null) {
            getRenderContext().canvas = null;
            return;
        }
        getRenderContext().canvas = new Canvas(bitmap);
    }

    @WorkerThread
    public void restoreDrawShapesToBitmap() {
        if (drawShape.isEmpty() || getRenderContext().bitmap == null) {
            return;
        }
        activeRenderMode(InteractiveMode.SCRIBBLE);
        renderToBitmap(new ArrayList<>(drawShape));
    }

    @WorkerThread
    public void redrawAllShapesToBitmap() {
        if (drawShape.isEmpty() || getRenderContext().bitmap == null) {
            return;
        }
        activeRenderMode(InteractiveMode.SCRIBBLE);
        getRenderContext().bitmap.eraseColor(Color.WHITE);
        renderToBitmap(new ArrayList<>(drawShape));
    }

    @WorkerThread
    public void replaceDrawShapes(@NonNull List<Shape> shapes) {
        drawShape.clear();
        drawShape.addAll(shapes);
    }

    @WorkerThread
    public void onHostSurfaceDestroyed() {
        touchHelperSession.onHostSurfaceDestroyed();
    }

    @WorkerThread
    public void pauseHostSession() {
        touchHelperSession.pauseHostSession();
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public RendererHelper getRendererHelper() {
        if (rendererHelper == null) {
            rendererHelper = new RendererHelper();
        }
        return rendererHelper;
    }

    public Observable<PenManager> createObservable() {
        return Observable.just(this).observeOn(getObserveOn());
    }

    public Scheduler getObserveOn() {
        return getRxScheduler().getObserveOn();
    }

    private RxScheduler getRxScheduler() {
        if (rxScheduler == null) {
            rxScheduler = RxScheduler.sharedSingleThreadManager();
        }
        return rxScheduler;
    }

    public List<Shape> getDrawShape() {
        return drawShape;
    }

    @WorkerThread
    public Rect getLimitNoteRect() {
        return getViewRect();
    }

    @WorkerThread
    public void setDrawExcludeRect(List<Rect> excludeRectList) {
        touchHelperSession.setDrawExcludeRect(excludeRectList);
    }

    @WorkerThread
    public void setStrokeWidth(float penWidth) {
        touchHelperSession.setStrokeWidthMm(penWidth,
                ResManager.getAppContext().getResources().getDisplayMetrics().densityDpi);
    }

    @WorkerThread
    public void setStrokeStyle(int style) {
        touchHelperSession.setStrokeStyle(style);
    }

    @WorkerThread
    public void setStrokeColor(int color) {
        touchHelperSession.setStrokeColor(color);
    }

    @WorkerThread
    public void setPenUpRefreshTimeMs(int time) {
        touchHelperSession.setPenUpRefreshTimeMs(time);
    }

    @WorkerThread
    public void setRawDrawingEnabled(boolean enable) {
        touchHelperSession.setRawDrawingEnabled(enable);
    }

    @WorkerThread
    public void setRawDrawingRenderEnabled(boolean enable) {
        touchHelperSession.setRawDrawingRenderEnabled(enable);
    }

    @WorkerThread
    public void setRawInputReaderEnable(boolean enable) {
        touchHelperSession.setRawInputReaderEnable(enable);
    }

    @WorkerThread
    public boolean isRawDrawingCreated() {
        return touchHelperSession.isRawDrawingCreated();
    }

    @WorkerThread
    public boolean isRawDrawingRenderEnabled() {
        return touchHelperSession.isRawDrawingRenderEnabled();
    }

    @WorkerThread
    public void activeRenderMode(InteractiveMode mode) {
        if (currentMode.equals(mode)) {
            return;
        }
        getRendererHelper().getRenderer(currentMode).onDeactivate(getSurfaceView());
        currentMode = mode;
        getRendererHelper().getRenderer(currentMode).onActive(getSurfaceView());
    }

    @WorkerThread
    public InteractiveMode getCurrentMode() {
        return currentMode;
    }

    @WorkerThread
    public RendererHelper.RenderContext getRenderContext() {
        return getRendererHelper().getRenderContext();
    }

    @WorkerThread
    public void renderToScreen() {
        getRendererHelper().renderToScreen(getCurrentMode(), getSurfaceView(), getRenderContext());
    }

    @WorkerThread
    public void renderToBitmap(List<Shape> shapes) {
        getRendererHelper().renderToBitmap(getCurrentMode(), shapes);
    }

    @WorkerThread
    public void applyPenAttrsFromBundle(PenBundle penBundle) {
        attributeConfig.applyPenAttrsFromBundle(penBundle);
    }

    @WorkerThread
    public void resetToBrushPenAttrs(PenBundle penBundle) {
        attributeConfig.resetToBrushPenAttrs(penBundle);
    }

    @WorkerThread
    public void applyAreaErasePreviewParams() {
        attributeConfig.applyAreaErasePreviewParams();
    }

    @WorkerThread
    public void applyAreaEraseCapPreviewParams() {
        attributeConfig.applyAreaEraseCapPreviewParams();
    }

    @WorkerThread
    public void applyStrokeMoveErasePreviewParams() {
        attributeConfig.applyStrokeMoveErasePreviewParams();
    }

    @WorkerThread
    public void applyStrokeMoveEraseCapPreviewParams() {
        attributeConfig.applyStrokeMoveEraseCapPreviewParams();
    }

    @WorkerThread
    public void restartRawPenSession(PenBundle penBundle) {
        attributeConfig.restartRawPenSession(penBundle);
    }

    @WorkerThread
    public void restoreRawPenInput(PenBundle penBundle) {
        attributeConfig.restoreRawPenInput(penBundle);
    }

    @WorkerThread
    public void setEraserRawDrawingEnabled(boolean enabled, int eraserStrokeStyle) {
        touchHelperSession.setEraserRawDrawingEnabled(enabled, eraserStrokeStyle);
    }

    @WorkerThread
    public void setErasePathDrawing(boolean drawing, int eraseType) {
        attributeConfig.setErasePathDrawing(drawing, eraseType);
    }

    @WorkerThread
    public void commitStroke(@NonNull Shape shape) {
        activeRenderMode(InteractiveMode.SCRIBBLE);
        drawShape.add(shape);
        List<Shape> batch = new ArrayList<>();
        batch.add(shape);
        renderToBitmap(batch);
    }

    @WorkerThread
    public void applyErasePenParams() {
        activeRenderMode(InteractiveMode.SCRIBBLE);
        applyPenAttrsFromBundle(PenBundle.getInstance());
    }

    @WorkerThread
    public void applyToolSwitchWithRefresh() {
        setRawDrawingRenderEnabled(false);
        applyErasePenParams();
        renderToScreen();
    }

    @WorkerThread
    public void prepareAppTrackEraseBegin() {
        if (!touchHelperSession.isRawDrawingRenderEnabled()) {
            return;
        }
        activeRenderMode(InteractiveMode.SCRIBBLE);
        getRenderContext().eraseArgs = null;
        renderToScreen();
    }

    private static final float PARTIAL_REFRESH_MAX_AREA_RATIO = 0.6f;

    @WorkerThread
    public void refreshPartial(@NonNull RectF refreshRect) throws Exception {
        RendererHelper.RenderContext context = getRenderContext();
        Bitmap bitmap = context.bitmap;
        if (bitmap != null && shouldUseFullRefresh(refreshRect, bitmap.getWidth(), bitmap.getHeight())) {
            context.clipRect = null;
            activeRenderMode(InteractiveMode.SCRIBBLE);
            renderToScreen();
            return;
        }
        try {
            EpdController.setViewDefaultUpdateMode(getSurfaceView(), UpdateMode.HAND_WRITING_REPAINT_MODE);
            context.clipRect = refreshRect;
            activeRenderMode(InteractiveMode.SCRIBBLE_PARTIAL_REFRESH);
            renderToScreen();
        } finally {
            context.clipRect = null;
            EpdController.resetViewUpdateMode(getSurfaceView());
        }
    }

    private static boolean shouldUseFullRefresh(RectF refreshRect, int bitmapW, int bitmapH) {
        if (bitmapW <= 0 || bitmapH <= 0) {
            return true;
        }
        RectF clipped = new RectF(refreshRect);
        clipped.intersect(0, 0, bitmapW, bitmapH);
        if (clipped.isEmpty()) {
            return true;
        }
        float bitmapArea = bitmapW * (float) bitmapH;
        return (clipped.width() * clipped.height()) / bitmapArea >= PARTIAL_REFRESH_MAX_AREA_RATIO;
    }
}
