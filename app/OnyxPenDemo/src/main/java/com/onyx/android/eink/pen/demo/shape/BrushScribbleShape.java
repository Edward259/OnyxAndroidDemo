package com.onyx.android.eink.pen.demo.shape;

import android.graphics.Paint;

import com.onyx.android.eink.pen.demo.helper.RendererHelper;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.NeoFountainPenWrapper;
import com.onyx.android.sdk.pen.NeoPen;
import com.onyx.android.sdk.pen.NeoPenRender;
import com.onyx.android.sdk.pen.PenUtils;
import com.onyx.android.sdk.pen.utils.FountainShapes;

import java.util.ArrayList;
import java.util.List;

public class BrushScribbleShape extends Shape {

    @Override
    public void render(RendererHelper.RenderContext renderContext) {
        List<TouchPoint> points = touchPointList.getPoints();
        applyStrokeStyle(renderContext);
        if (points == null || points.size() < 2) {
            return;
        }
        applyFountainPaintStyle(renderContext);
        if (renderFountainV2(renderContext, points)) {
            if (isTransparent()) {
                renderFountainV2(renderContext, points, getRenderStrokeWidth() + 2.0f);
            }
            return;
        }
        List<TouchPoint> fountainPoints = NeoFountainPenWrapper.computeStrokePoints(copyAndNormalizePressure(points),
                1.0f, getRenderStrokeWidth(), 1.0f);
        PenUtils.drawStrokeByPointSize(renderContext.canvas, renderContext.paint, fountainPoints, isTransparent());
        if (isTransparent()) {
            PenUtils.drawStrokeByPointSize(renderContext.canvas, renderContext.paint,
                    expandSizes(fountainPoints, 2.0f), true);
        }
    }

    private void applyFountainPaintStyle(RendererHelper.RenderContext renderContext) {
        renderContext.paint.setStyle(Paint.Style.FILL);
        renderContext.paint.setStrokeWidth(0.0f);
    }

    private boolean renderFountainV2(RendererHelper.RenderContext renderContext, List<TouchPoint> points) {
        return renderFountainV2(renderContext, points, getRenderStrokeWidth());
    }

    private boolean renderFountainV2(RendererHelper.RenderContext renderContext,
                                      List<TouchPoint> points, float strokeWidth) {
        List<TouchPoint> renderPoints = copyAndNormalizePressure(points);
        if (renderPoints.size() < 2 || !NeoFountainPenWrapper.hasPressure(renderPoints)) {
            return false;
        }
        NeoPen pen = FountainShapes.INSTANCE.createNeoPenV2(strokeWidth,
                NeoFountainPenWrapper.MIN_FOUNTAIN_PEN_WIDTH,
                1.0f, 1.0f, 1.0f, 1.0f, null, true, null);
        if (pen == null) {
            return false;
        }
        NeoPenRender penRender = new NeoPenRender(pen);
        penRender.render(renderContext.canvas, renderContext.paint, renderPoints);
        penRender.destroyPen();
        return true;
    }

    private static List<TouchPoint> expandSizes(List<TouchPoint> points, float extra) {
        List<TouchPoint> out = new ArrayList<>(points.size());
        for (TouchPoint p : points) {
            TouchPoint c = new TouchPoint(p);
            c.size = p.size + extra;
            out.add(c);
        }
        return out;
    }

    private List<TouchPoint> copyAndNormalizePressure(List<TouchPoint> points) {
        List<TouchPoint> renderPoints = new ArrayList<>();
        if (points == null) {
            return renderPoints;
        }
        boolean needNormalize = false;
        for (TouchPoint point : points) {
            if (point != null && point.getPressure() > 1.0f) {
                needNormalize = true;
                break;
            }
        }
        for (TouchPoint point : points) {
            if (point == null) {
                continue;
            }
            TouchPoint copy = new TouchPoint(point);
            if (needNormalize) {
                copy.pressure = copy.getPressure() / EpdController.MAX_TOUCH_PRESSURE;
            }
            renderPoints.add(copy);
        }
        return renderPoints;
    }
}
