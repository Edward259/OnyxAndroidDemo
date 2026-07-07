package com.onyx.android.eink.pen.demo.core.session;

import android.graphics.Point;
import android.graphics.Rect;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.onyx.android.sdk.pen.RawInputCallback;
import com.onyx.android.sdk.pen.TouchHelper;
import com.onyx.android.sdk.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public final class TouchHelperSession {

    private static final float MM_OF_ONE_INCH = 25.4f;

    private SurfaceView surfaceView;
    private TouchHelper touchHelper;
    private volatile boolean hostSurfaceAttached;

    @WorkerThread
    public void bindHostView(@NonNull SurfaceView view, RawInputCallback callback) {
        if (touchHelper == null) {
            touchHelper = TouchHelper.create(view, callback);
            touchHelper.setPostInputEvent(true);
        } else {
            touchHelper.bindHostView(view, callback);
        }
        surfaceView = view;
    }

    @WorkerThread
    public void updateLimitRect(View floatMenuLayout) {
        if (touchHelper == null || surfaceView == null) {
            return;
        }
        Rect limitRect = ViewUtils.localVisibleRect(surfaceView);
        Rect funcMenuExcludeRect = ViewUtils.relativelyParentRect(floatMenuLayout);
        List<Rect> excludeRectList = new ArrayList<>();
        excludeRectList.add(funcMenuExcludeRect);
        touchHelper.setLimitRect(limitRect, excludeRectList);
    }

    @WorkerThread
    public void restartRawDrawing() {
        if (touchHelper != null) {
            touchHelper.restartRawDrawing();
        }
    }

    @WorkerThread
    public void onHostSurfaceDestroyed() {
        hostSurfaceAttached = false;
        if (touchHelper != null) {
            touchHelper.closeRawDrawing();
        }
    }

    @WorkerThread
    public void pauseHostSession() {
        if (touchHelper != null) {
            touchHelper.setRawDrawingEnabled(false);
        }
    }

    public boolean isHostSurfaceAttached() {
        return hostSurfaceAttached;
    }

    public void setHostSurfaceAttached(boolean attached) {
        hostSurfaceAttached = attached;
    }

    public boolean isHostSurfaceValid() {
        if (surfaceView == null) {
            return false;
        }
        try {
            return surfaceView.getHolder().getSurface().isValid();
        } catch (Exception e) {
            return false;
        }
    }

    public void destroy() {
        hostSurfaceAttached = false;
        surfaceView = null;
        if (touchHelper != null) {
            touchHelper.closeRawDrawing();
            touchHelper = null;
        }
    }

    public TouchHelper getTouchHelper() {
        return touchHelper;
    }

    public SurfaceView getSurfaceView() {
        return surfaceView;
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
    }

    public void setViewPoint(View renderView, Point viewPoint) {
        Rect rect = ViewUtils.globalVisibleRect(renderView);
        viewPoint.set(rect.left, rect.top);
    }

    public Rect getViewRect() {
        Rect rect = new Rect();
        if (surfaceView != null) {
            surfaceView.getLocalVisibleRect(rect);
        }
        return rect;
    }

    @WorkerThread
    public void setDrawExcludeRect(List<Rect> excludeRectList) {
        if (touchHelper != null) {
            touchHelper.setExcludeRect(excludeRectList);
        }
    }

    @WorkerThread
    public void setStrokeWidthMm(float penWidthMm, float densityDpi) {
        if (touchHelper == null) {
            return;
        }
        float penWidthPx = penWidthMm * densityDpi / MM_OF_ONE_INCH;
        touchHelper.setStrokeWidth(penWidthPx);
    }

    @WorkerThread
    public void setStrokeWidthPx(float penWidthPx) {
        if (touchHelper != null) {
            touchHelper.setStrokeWidth(penWidthPx);
        }
    }

    @WorkerThread
    public void setStrokeStyle(int style) {
        if (touchHelper != null) {
            touchHelper.setStrokeStyle(style);
        }
    }

    @WorkerThread
    public void setStrokeColor(int color) {
        if (touchHelper != null) {
            touchHelper.setStrokeColor(color);
        }
    }

    @WorkerThread
    public void setPenUpRefreshTimeMs(int time) {
        if (touchHelper != null) {
            touchHelper.setPenUpRefreshTimeMs(time);
        }
    }

    @WorkerThread
    public void setRawDrawingEnabled(boolean enable) {
        if (touchHelper != null) {
            touchHelper.setRawDrawingEnabled(enable);
        }
    }

    @WorkerThread
    public void setRawDrawingRenderEnabled(boolean enable) {
        if (touchHelper != null) {
            touchHelper.setRawDrawingRenderEnabled(enable);
        }
    }

    @WorkerThread
    public void setRawInputReaderEnable(boolean enable) {
        if (touchHelper != null) {
            touchHelper.setRawInputReaderEnable(enable);
        }
    }

    @WorkerThread
    public void forceRawDrawingEnabled() {
        if (touchHelper != null) {
            touchHelper.forceSetRawDrawingEnabled(true);
        }
    }

    @WorkerThread
    public void forceSetRawDrawingEnabled(boolean enable) {
        if (touchHelper != null) {
            touchHelper.forceSetRawDrawingEnabled(enable);
        }
    }

    @WorkerThread
    public boolean isRawDrawingCreated() {
        return touchHelper != null && touchHelper.isRawDrawingCreated();
    }

    @WorkerThread
    public boolean isRawDrawingRenderEnabled() {
        return touchHelper != null && touchHelper.isRawDrawingRenderEnabled();
    }

    @WorkerThread
    public void setEraserRawDrawingEnabled(boolean enabled, int eraserStrokeStyle) {
        if (touchHelper != null) {
            touchHelper.setEraserRawDrawingEnabled(enabled, eraserStrokeStyle);
        }
    }

    @WorkerThread
    public void setBrushRawDrawing(boolean drawing) {
        if (touchHelper != null) {
            touchHelper.setBrushRawDrawingEnabled(drawing);
        }
    }
}
