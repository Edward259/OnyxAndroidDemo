package com.onyx.android.eink.pen.demo.shape;

import com.onyx.android.eink.pen.demo.helper.RendererHelper;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.NeoBrushPenWrapper;
import com.onyx.android.sdk.pen.PenUtils;

import java.util.ArrayList;
import java.util.List;

public class NewBrushScribbleShape extends Shape {

    @Override
    public void render(RendererHelper.RenderContext renderContext) {
        List<TouchPoint> points = touchPointList.getPoints();
        applyStrokeStyle(renderContext);
        if (points == null || points.size() < 2) {
            return;
        }
        List<TouchPoint> neoBrushPoints = computeNeoBrushPoints();
        if (neoBrushPoints == null || neoBrushPoints.size() < 2) {
            return;
        }
        if (isTransparent()) {
            PenUtils.drawStrokeByPointSize(renderContext.canvas, renderContext.paint,
                    neoBrushPoints, true);
            PenUtils.drawStrokeByPointSize(renderContext.canvas, renderContext.paint,
                    expandSizes(neoBrushPoints, 2.0f), true);
        } else {
            PenUtils.drawStrokeByPointSize(renderContext.canvas, renderContext.paint,
                    neoBrushPoints, false);
        }
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

    private List<TouchPoint> computeNeoBrushPoints() {
        List<TouchPoint> points = touchPointList.getPoints();
        if (points == null || points.size() < 2) {
            return null;
        }
        List<TouchPoint> copy = new ArrayList<>(points.size());
        for (TouchPoint p : points) {
            copy.add(new TouchPoint(p));
        }
        return NeoBrushPenWrapper.computeStrokePoints(copy,
                getRenderStrokeWidth(), EpdController.getMaxTouchPressure());
    }
}
