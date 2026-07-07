package com.onyx.android.eink.pen.demo.core;

import android.graphics.Color;
import android.graphics.Rect;

import androidx.annotation.NonNull;

import com.onyx.android.eink.pen.demo.brush.data.ShapeFactory;
import com.onyx.android.eink.pen.demo.brush.data.ShapeType;
import com.onyx.android.eink.pen.demo.brush.util.PenInfoUtils;
import com.onyx.android.eink.pen.demo.erase.data.EraseTypes;
import com.onyx.android.eink.pen.demo.erase.util.EraseUnits;
import com.onyx.android.eink.pen.demo.erase.util.EraserTrackHelper;
import com.onyx.android.sdk.data.note.PenTexture;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PenBundle {
    private static PenBundle instance;

    private PenManager penManager;
    private EventBus eventBus;

    public Map<Integer, Float> penLineWidthMap = new HashMap<>();
    private int currentShapeType = ShapeFactory.SHAPE_BRUSH_SCRIBBLE;
    private int currentStrokeColor = Color.BLACK;
    private int currentTexture = PenTexture.CHARCOAL_SHAPE_V1;
    private float currentStrokeWidth;

    private boolean isErasing = false;
    private int currentEraseType = EraseTypes.ERASER_STROKE;
    private final Map<Integer, Float> eraseWidthMap = new HashMap<>();
    private final Map<Integer, Boolean> displayEraseTrackMap = new HashMap<>();

    private boolean enablePenUpRefresh = false;
    private int penUpRefreshTimeMs = 500;

    private List<Rect> excludeRectList;

    private PenExecutor penExecutor;

    private PenBundle() {
        initDefaultPenLineWidth();
        initDefaultEraseWidth();
        initDefaultEraseTrack();
        setCurrentStrokeWidth(getPenLineWidth(currentShapeType));
    }

    private void initDefaultEraseTrack() {
        for (int eraseType : new int[] {
                EraseTypes.ERASER_STROKE,
                EraseTypes.ERASER_MOVE,
                EraseTypes.ERASER_AREA }) {
            displayEraseTrackMap.put(eraseType,
                    EraserTrackHelper.defaultTrackEnabled(eraseType));
        }
    }

    private void initDefaultEraseWidth() {
        eraseWidthMap.put(EraseTypes.ERASER_STROKE,
                EraseUnits.getDefaultEraseWidth(EraseTypes.ERASER_STROKE));
        eraseWidthMap.put(EraseTypes.ERASER_MOVE,
                EraseUnits.getDefaultEraseWidth(EraseTypes.ERASER_MOVE));
    }

    private void initDefaultPenLineWidth() {
        for (ShapeType style : ShapeType.values()) {
            int shapeType = style.getValue();
            penLineWidthMap.put(shapeType, PenInfoUtils.getShapeDefaultStrokeWidth(shapeType));
        }
    }

    public static PenBundle getInstance() {
        if (instance == null) {
            instance = new PenBundle();
        }
        return instance;
    }

    public PenManager getPenManager() {
        if (penManager == null) {
            penManager = new PenManager(getEventBus());
        }
        return penManager;
    }

    @NonNull
    public PenExecutor getPenExecutor() {
        if (penExecutor == null) {
            penExecutor = PenExecutor.create(getPenManager());
        }
        return penExecutor;
    }

    public EventBus getEventBus() {
        if (eventBus == null) {
            eventBus = new EventBus();
        }
        return eventBus;
    }

    public int getCurrentShapeType() {
        return currentShapeType;
    }

    public void setCurrentShapeType(int currentShapeType) {
        this.currentShapeType = currentShapeType;
    }

    public float getCurrentStrokeWidth() {
        return currentStrokeWidth;
    }

    public void setCurrentStrokeWidth(float currentStrokeWidth) {
        this.currentStrokeWidth = currentStrokeWidth;
    }

    public int getCurrentStrokeColor() {
        return currentStrokeColor;
    }

    public void setCurrentStrokeColor(int currentStrokeColor) {
        this.currentStrokeColor = currentStrokeColor;
    }

    public int getCurrentTexture() {
        return currentTexture;
    }

    public void setCurrentTexture(int currentTexture) {
        this.currentTexture = currentTexture;
    }

    public void savePenLineWidth(int shapeType, float lineWidth) {
        penLineWidthMap.put(shapeType, lineWidth);
    }

    public float getPenLineWidth(int shapeType) {
        Float lineWidth = penLineWidthMap.get(shapeType);
        if (lineWidth != null) {
            return lineWidth;
        }
        return PenInfoUtils.getShapeDefaultStrokeWidth(shapeType);
    }

    public boolean isErasing() {
        return isErasing;
    }

    public void setErasing(boolean erasing) {
        isErasing = erasing;
    }

    public float getCurrentEraseWidth() {
        return getEraseWidth(currentEraseType);
    }

    public void setCurrentEraseWidth(float currentEraseWidth) {
        eraseWidthMap.put(currentEraseType, currentEraseWidth);
    }

    public int getCurrentEraseType() {
        return currentEraseType;
    }

    public void setCurrentEraseType(int currentEraseType) {
        this.currentEraseType = currentEraseType;
    }

    public float getEraseWidth(int eraseType) {
        Float width = eraseWidthMap.get(eraseType);
        if (width == null) {
            width = eraseType == EraseTypes.ERASER_STROKE
                    ? EraseUnits.getDefaultEraseWidth(EraseTypes.ERASER_STROKE)
                    : EraseUnits.getDefaultEraseWidth(EraseTypes.ERASER_MOVE);
        }
        if (eraseType == EraseTypes.ERASER_MOVE) {
            width = EraseUnits.clampEraseWidth(width, eraseType);
            eraseWidthMap.put(eraseType, width);
        }
        return width;
    }

    public void setEraseWidth(int eraseType, float width) {
        if (eraseType == EraseTypes.ERASER_MOVE) {
            width = EraseUnits.clampEraseWidth(width, eraseType);
        }
        eraseWidthMap.put(eraseType, width);
    }

    public boolean isDisplayEraseTrack(int eraseType) {
        Boolean enabled = displayEraseTrackMap.get(eraseType);
        if (enabled == null) {
            return EraserTrackHelper.defaultTrackEnabled(eraseType);
        }
        return enabled;
    }

    public void setDisplayEraseTrack(int eraseType, boolean displayEraseTrack) {
        displayEraseTrackMap.put(eraseType, displayEraseTrack);
    }

    public boolean isEnablePenUpRefresh() {
        return enablePenUpRefresh;
    }

    public void setEnablePenUpRefresh(boolean enablePenUpRefresh) {
        this.enablePenUpRefresh = enablePenUpRefresh;
    }

    public int getPenUpRefreshTimeMs() {
        return penUpRefreshTimeMs;
    }

    public void setPenUpRefreshTimeMs(int penUpRefreshTimeMs) {
        this.penUpRefreshTimeMs = penUpRefreshTimeMs;
    }

    public List<Rect> getExcludeRectList() {
        return excludeRectList;
    }

    public void setExcludeRectList(List<Rect> excludeRectList) {
        this.excludeRectList = excludeRectList;
    }

    public void resetInteractiveStateOnSessionEnd() {
        isErasing = false;
    }
}
