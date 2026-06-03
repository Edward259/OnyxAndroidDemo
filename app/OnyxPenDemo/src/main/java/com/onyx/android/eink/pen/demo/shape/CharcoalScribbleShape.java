package com.onyx.android.eink.pen.demo.shape;

import com.onyx.android.eink.pen.demo.data.ShapeFactory;
import com.onyx.android.eink.pen.demo.helper.RendererHelper;
import com.onyx.android.eink.pen.demo.util.RendererUtils;
import com.onyx.android.sdk.data.PenConstant;
import com.onyx.android.sdk.data.note.PenTexture;
import com.onyx.android.sdk.data.note.ShapeCreateArgs;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.NeoCharcoalPenV2Wrapper;
import com.onyx.android.sdk.pen.NeoCharcoalPenWrapper;
import com.onyx.android.sdk.pen.PenRenderArgs;

import java.util.List;

public class CharcoalScribbleShape extends Shape {

    private static final float CHARCOAL_DISPLAY_SCALE_COMPENSATION = 1.5f;

    @Override
    public void render(RendererHelper.RenderContext renderContext) {
        List<TouchPoint> points = touchPointList.getPoints();
        applyStrokeStyle(renderContext);

        float renderStrokeWidth = getRenderStrokeWidth()
                * CHARCOAL_DISPLAY_SCALE_COMPENSATION;

        PenRenderArgs renderArgs = new PenRenderArgs()
                .setCreateArgs(new ShapeCreateArgs())
                .setCanvas(renderContext.canvas)
                .setPenType(ShapeFactory.getCharcoalPenType(texture))
                .setColor(strokeColor)
                .setErase(isTransparent())
                .setPaint(renderContext.paint)
                .setScreenMatrix(RendererUtils.getPointMatrix(renderContext))
                .setStrokeWidth(renderStrokeWidth)
                .setPoints(points);

        // Mirror Kepler's CharcoalRender: select V1 vs V2 wrapper by texture.
        // (NeoCharcoalPenWrapper internally creates NeoCharcoalPen for V1;
        //  NeoCharcoalPenV2Wrapper creates NeoCharcoalPenV2 for V2.)
        if (texture == PenTexture.CHARCOAL_SHAPE_V2) {
            if (renderStrokeWidth <= PenConstant.CHARCOAL_SHAPE_DRAW_NORMAL_SCALE_WIDTH_THRESHOLD) {
                NeoCharcoalPenV2Wrapper.drawNormalStroke(renderArgs);
            } else {
                renderArgs.setRenderMatrix(RendererUtils.getPointMatrix(renderContext));
                NeoCharcoalPenV2Wrapper.drawBigStroke(renderArgs);
            }
        } else {
            if (renderStrokeWidth <= PenConstant.CHARCOAL_SHAPE_DRAW_NORMAL_SCALE_WIDTH_THRESHOLD) {
                NeoCharcoalPenWrapper.drawNormalStroke(renderArgs);
            } else {
                renderArgs.setRenderMatrix(RendererUtils.getPointMatrix(renderContext));
                NeoCharcoalPenWrapper.drawBigStroke(renderArgs);
            }
        }
    }
}
