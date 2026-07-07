package com.onyx.android.eink.pen.demo.brush.util;

import android.graphics.RectF;

import com.onyx.android.eink.pen.demo.core.PenBundle;
import com.onyx.android.eink.pen.demo.brush.data.ShapeFactory;
import com.onyx.android.eink.pen.demo.erase.shape.AreaEraseShape;
import com.onyx.android.eink.pen.demo.brush.shape.Shape;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.data.TouchPointList;

import java.util.List;

public class ShapeUtils {

    public static Shape createShape(PenBundle penBundle, int shapeType, TouchPointList pointList) {
        Shape shape = ShapeFactory.createShape(shapeType)
                .setShapeType(shapeType)
                .setTouchPointList(PenShapeRenderHelper.cloneTouchPointList(pointList))
                .setStrokeColor(penBundle.getCurrentStrokeColor())
                .setStrokeWidth(penBundle.getCurrentStrokeWidth())
                .setShapeCreateArgs(PenShapeRenderHelper.createShapeCreateArgs(penBundle, shapeType));
        if (shapeType == ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE) {
            shape.setTexture(penBundle.getCurrentTexture());
        }
        shape.updateShapeRect();
        return shape;
    }

    public static Shape cloneShape(Shape source, TouchPointList pointList) {
        Shape shape = ShapeFactory.createShape(source.getShapeType())
                .setShapeType(source.getShapeType())
                .setTouchPointList(PenShapeRenderHelper.cloneTouchPointList(pointList))
                .setStrokeColor(source.getStrokeColor())
                .setStrokeWidth(source.getStrokeWidth())
                .setShapeCreateArgs(PenShapeRenderHelper.copyShapeCreateArgs(source.getShapeCreateArgs()));
        if (source.getShapeType() == ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE) {
            shape.setTexture(source.getTexture());
        }
        shape.updateShapeRect();
        return shape;
    }

    public static AreaEraseShape createAreaEraseShape(TouchPointList pointList) {
        return new AreaEraseShape(pointList);
    }

    public static RectF getBoundingRect(final TouchPointList touchPointList) {
        RectF boundingRect = null;
        List<TouchPoint> list = touchPointList.getPoints();
        for (TouchPoint touchPoint : list) {
            if (boundingRect == null) {
                boundingRect = new RectF(touchPoint.x, touchPoint.y, touchPoint.x, touchPoint.y);
            } else {
                boundingRect.union(touchPoint.x, touchPoint.y);
            }
        }
        return boundingRect;
    }
}
