package com.onyx.android.eink.pen.demo.request;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.SurfaceView;

import com.onyx.android.eink.pen.demo.core.PenManager;
import com.onyx.android.eink.pen.demo.render.RendererUtils;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.api.device.epd.UpdateMode;
import com.onyx.android.sdk.rx.RxRequest;
import com.onyx.android.sdk.utils.RectUtils;

public class PartialRefreshRequest extends RxRequest {

    private final PenManager penManager;
    private final SurfaceView surfaceView;
    private final RectF refreshRect;
    private Bitmap bitmap;

    public PartialRefreshRequest(PenManager penManager, RectF refreshRect) {
        this.penManager = penManager;
        this.surfaceView = null;
        this.refreshRect = refreshRect;
    }

    public PartialRefreshRequest(SurfaceView surfaceView, RectF refreshRect, Bitmap bitmap) {
        this.penManager = null;
        this.surfaceView = surfaceView;
        this.refreshRect = refreshRect;
        this.bitmap = bitmap;
    }

    public PartialRefreshRequest setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        return this;
    }

    @Override
    public void execute() throws Exception {
        if (penManager != null) {
            refreshViaPenManager();
        } else {
            refreshBitmapToSurface();
        }
    }

    private void refreshViaPenManager() throws Exception {
        penManager.refreshPartial(refreshRect);
    }

    private void refreshBitmapToSurface() {
        if (surfaceView == null || bitmap == null) {
            return;
        }
        Rect renderRect = RectUtils.toRect(refreshRect);
        Rect viewRect = RendererUtils.checkSurfaceView(surfaceView);
        EpdController.setViewDefaultUpdateMode(surfaceView, UpdateMode.HAND_WRITING_REPAINT_MODE);
        Canvas canvas = surfaceView.getHolder().lockCanvas(renderRect);
        if (canvas == null) {
            return;
        }
        try {
            canvas.clipRect(renderRect);
            RendererUtils.renderBackground(canvas, viewRect);
            Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            canvas.drawBitmap(bitmap, src, src, null);
        } finally {
            surfaceView.getHolder().unlockCanvasAndPost(canvas);
            EpdController.resetViewUpdateMode(surfaceView);
        }
    }
}
