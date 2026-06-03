package com.onyx.android.eink.pen.demo.shape;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import com.onyx.android.eink.pen.demo.helper.RendererHelper;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.BallpointPenRenderWrapper;
import com.onyx.android.sdk.pen.NeoBallpointInkPen;
import com.onyx.android.sdk.pen.NeoPenConfig;

import java.util.List;

public class NormalPencilShape extends Shape {

    @Override
    public void render(RendererHelper.RenderContext renderContext) {
        List<TouchPoint> points = touchPointList.getPoints();
        if (points == null || points.size() < 2) {
            return;
        }
        applyStrokeStyle(renderContext);
        applyBallpointPaintStyle(renderContext);
        if (renderBallpointStroke(renderContext, points)) {
            return;
        }
        drawFallbackPath(renderContext, points);
    }

    private void applyBallpointPaintStyle(RendererHelper.RenderContext renderContext) {
        renderContext.paint.setStyle(isTransparent() ? Paint.Style.FILL_AND_STROKE : Paint.Style.FILL);
    }

    private boolean renderBallpointStroke(RendererHelper.RenderContext renderContext, List<TouchPoint> points) {
        NeoPenConfig penConfig = NeoBallpointInkPen.Companion.defaultPenConfig();
        penConfig.width = getRenderStrokeWidth();
        penConfig.color = renderContext.paint.getColor();
        BallpointPenRenderWrapper penRender = BallpointPenRenderWrapper.Companion.create(penConfig);
        if (penRender == null) {
            return false;
        }
        penRender.render(renderContext.canvas, renderContext.paint, points);
        penRender.destroyPen();
        return true;
    }

    private void drawFallbackPath(RendererHelper.RenderContext renderContext, List<TouchPoint> points) {
        Canvas canvas = renderContext.canvas;
        Paint paint = renderContext.paint;
        Path path = new Path();
        PointF prePoint = new PointF(points.get(0).x, points.get(0).y);
        path.moveTo(prePoint.x, prePoint.y);
        for (TouchPoint point : points) {
            path.quadTo(prePoint.x, prePoint.y, point.x, point.y);
            prePoint.x = point.x;
            prePoint.y = point.y;
        }
        canvas.drawPath(path, paint);
    }
}
