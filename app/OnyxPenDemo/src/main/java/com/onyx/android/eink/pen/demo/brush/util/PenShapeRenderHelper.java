package com.onyx.android.eink.pen.demo.brush.util;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.onyx.android.eink.pen.demo.core.PenBundle;
import com.onyx.android.eink.pen.demo.brush.data.ShapeFactory;
import com.onyx.android.sdk.data.note.PenAttrs;
import com.onyx.android.sdk.data.note.ShapeCreateArgs;
import com.onyx.android.sdk.data.note.TiltConfig;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.device.Device;
import com.onyx.android.sdk.pen.PenResult;
import com.onyx.android.sdk.pen.data.TouchPointList;

import java.util.ArrayList;
import java.util.List;

import kotlin.Pair;

public final class PenShapeRenderHelper {

    private static final float DEFAULT_DISPLAY_SCALE = 1.0f;
    private static final Matrix IDENTITY_MATRIX = new Matrix();

    private PenShapeRenderHelper() {
    }

    public static ShapeCreateArgs createShapeCreateArgs(PenBundle penBundle, int shapeType) {
        int texture = shapeType == ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE
                ? penBundle.getCurrentTexture() : 0;
        ShapeCreateArgs args = new ShapeCreateArgs()
                .setDisplayScale(DEFAULT_DISPLAY_SCALE)
                .setPenAttrs(new PenAttrs().setTexture(texture));
        if (shapeType == ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE) {
            args.setTiltConfig(loadCharcoalTiltConfig(texture));
        }
        Float sensitivity = resolvePressureSensitivity(shapeType);
        if (sensitivity != null) {
            args.setPressureSensitivity(sensitivity);
        }
        return args;
    }

    public static ShapeCreateArgs copyShapeCreateArgs(ShapeCreateArgs source) {
        if (source == null) {
            return null;
        }
        ShapeCreateArgs copy = new ShapeCreateArgs()
                .setDisplayScale(source.getDisplayScale())
                .setPressureSensitivity(source.getPressureSensitivity());
        if (source.getPenAttrs() != null) {
            copy.setPenAttrs(new PenAttrs().setTexture(source.getPenAttrs().getTexture()));
        }
        if (source.getTiltConfig() != null) {
            copy.setTiltConfig(new TiltConfig()
                    .setTiltEnabled(source.getTiltConfig().isTiltEnabled())
                    .setTiltScale(source.getTiltConfig().getTiltScale()));
        }
        return copy;
    }

    public static TouchPointList cloneTouchPointList(TouchPointList source) {
        TouchPointList cloned = new TouchPointList();
        if (source == null) {
            return cloned;
        }
        for (TouchPoint point : source.getPoints()) {
            if (point != null) {
                cloned.add(new TouchPoint(point));
            }
        }
        return cloned;
    }

    public static List<TouchPoint> buildRenderPoints(TouchPointList touchPointList) {
        if (touchPointList == null) {
            return new ArrayList<>();
        }
        return TouchPoint.renderPointArray(IDENTITY_MATRIX, touchPointList.getRenderPoints());
    }

    public static void renderPenResults(Canvas canvas, Paint paint,
                                        List<Pair<PenResult, PenResult>> results) {
        if (results == null || results.isEmpty()) {
            return;
        }
        for (Pair<PenResult, PenResult> pair : results) {
            if (pair.getFirst() != null) {
                pair.getFirst().draw(canvas, paint);
            }
        }
        Pair<PenResult, PenResult> last = results.get(results.size() - 1);
        if (last.getSecond() != null) {
            last.getSecond().draw(canvas, paint);
        }
    }

    private static Float resolvePressureSensitivity(int shapeType) {
        if (shapeType == ShapeFactory.SHAPE_BRUSH_SCRIBBLE
                || shapeType == ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE) {
            return PenInfoUtils.getPressureSensitivity(shapeType);
        }
        return null;
    }

    private static TiltConfig loadCharcoalTiltConfig(int texture) {
        int strokeStyle = ShapeFactory.getStrokeStyle(ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE, texture);
        float[] parameters = Device.currentDevice().getStrokeParameters(strokeStyle);
        if (parameters == null || parameters.length < 2) {
            return null;
        }
        return new TiltConfig()
                .setTiltEnabled(parameters[0] != 0)
                .setTiltScale(parameters[1]);
    }
}
