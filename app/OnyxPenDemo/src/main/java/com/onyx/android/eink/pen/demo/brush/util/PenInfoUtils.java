package com.onyx.android.eink.pen.demo.brush.util;

import com.onyx.android.eink.pen.demo.brush.data.ShapeFactory;
import com.onyx.android.eink.pen.demo.erase.data.EraseTypes;
import com.onyx.android.eink.pen.demo.erase.util.EraseUnits;
import com.onyx.android.sdk.data.PenConstant;
import com.onyx.android.sdk.device.Device;

import java.util.ArrayList;
import java.util.List;

public class PenInfoUtils {

    /** Default pressure sensitivity for brush pen when device params are unavailable. */
    private static final float DEFAULT_PRESSURE_SENSITIVITY = 0.3f;

    public static float getShapeDefaultStrokeWidth(int shapeType) {
        switch (shapeType) {
            case ShapeFactory.SHAPE_PENCIL_SCRIBBLE:
                return PenConstant.PENCIL_PEN_DEFAULT_STROKE_WIDTH_MM;
            case ShapeFactory.SHAPE_NEO_BRUSH_SCRIBBLE:
                return PenConstant.BRUSH_PEN_DEFAULT_STROKE_WIDTH_MM;
            case ShapeFactory.SHAPE_MARKER_SCRIBBLE:
                return PenConstant.MARKER_PEN_DEFAULT_STROKE_WIDTH_MM;
            case ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE:
                return PenConstant.CHARCOAL_PEN_DEFAULT_STROKE_WIDTH_MM;
            default:
                return PenConstant.DEFAULT_STROKE_WIDTH_MM;
        }
    }

    public static List<Float> getPenWidthRange(int shapeType) {
        float minStrokeWidth = getMinStrokeWidth(shapeType);
        float maxStrokeWidth = getMaxStrokeWidth(shapeType);
        List<Float> strokeWidthValues = new ArrayList<>();
        if (shapeType == ShapeFactory.SHAPE_MARKER_SCRIBBLE) {
            for (float i = minStrokeWidth; i <= maxStrokeWidth; i = i + PenConstant.MARKER_STROKE_WIDTH_GAP) {
                strokeWidthValues.add(i);
            }
        } else {
            for (float i = minStrokeWidth;
                 i < PenConstant.NORMAL_STROKE_WIDTH_DIVIDER;
                 i += PenConstant.NORMAL_STROKE_WIDTH_MIN_GAP) {
                strokeWidthValues.add(i);
            }
            for (float i = PenConstant.NORMAL_STROKE_WIDTH_DIVIDER;
                 i <= maxStrokeWidth;
                 i += PenConstant.NORMAL_STROKE_WIDTH_MAX_GAP) {
                strokeWidthValues.add(i);
            }
        }
        return strokeWidthValues;
    }

    public static float getMaxStrokeWidth(int shapeType) {
        if (isMarkerStrokeStyle(shapeType)) {
            return PenConstant.MAX_MARKER_STROKE_WIDTH;
        } else {
            return PenConstant.MAX_NORMAL_STROKE_WIDTH;
        }
    }

    public static float getMinStrokeWidth(int shapeType) {
        if (isMarkerStrokeStyle(shapeType)) {
            return PenConstant.MIN_MARKER_STROKE_WIDTH;
        } else {
            return PenConstant.MIN_NORMAL_STROKE_WIDTH;
        }
    }

    public static float getStrokeWidthGap(int shapeType, boolean plusClick, float strokeWidth) {
        if (isMarkerStrokeStyle(shapeType)) {
            return PenConstant.MARKER_STROKE_WIDTH_GAP;
        }
        if (plusClick) {
            return strokeWidth < PenConstant.NORMAL_STROKE_WIDTH_DIVIDER ?
                    PenConstant.NORMAL_STROKE_WIDTH_MIN_GAP : PenConstant.NORMAL_STROKE_WIDTH_MAX_GAP;
        } else {
            return strokeWidth <= PenConstant.NORMAL_STROKE_WIDTH_DIVIDER ?
                    PenConstant.NORMAL_STROKE_WIDTH_MIN_GAP : PenConstant.NORMAL_STROKE_WIDTH_MAX_GAP;
        }
    }

    public static boolean isMarkerStrokeStyle(int shapeType) {
        return ShapeFactory.isMarkerShape(shapeType);
    }

    public static List<Float> getEraseWidthRange() {
        return EraseUnits.getEraseWidthRange(EraseTypes.ERASER_MOVE);
    }

    public static List<Float> getEraseWidthRange(int eraseType) {
        return EraseUnits.getEraseWidthRange(eraseType);
    }

    public static float getPressureSensitivity(int shapeType) {
        int strokeStyle = ShapeFactory.getStrokeStyle(shapeType, 0);
        float[] parameters = Device.currentDevice().getStrokeParameters(strokeStyle);
        if (parameters == null || parameters.length < 1) {
            return DEFAULT_PRESSURE_SENSITIVITY;
        }
        return parameters[0];
    }
}
