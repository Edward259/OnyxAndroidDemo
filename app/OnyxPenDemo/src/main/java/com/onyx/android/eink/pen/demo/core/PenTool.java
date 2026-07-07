package com.onyx.android.eink.pen.demo.core;

import androidx.annotation.NonNull;

import com.onyx.android.eink.pen.demo.erase.data.EraseTypes;

/**
 * Unified tool selector replacing separate brush/erase mode and erase-type APIs.
 */
public enum PenTool {
    BRUSH,
    ERASE_STROKE,
    ERASE_MOVE,
    ERASE_AREA;

    public boolean isErase() {
        return this != BRUSH;
    }

    public int toEraseType() {
        switch (this) {
            case ERASE_STROKE:
                return EraseTypes.ERASER_STROKE;
            case ERASE_MOVE:
                return EraseTypes.ERASER_MOVE;
            case ERASE_AREA:
                return EraseTypes.ERASER_AREA;
            default:
                throw new IllegalStateException("Not an erase tool: " + this);
        }
    }

    @NonNull
    public static PenTool fromEraseType(int eraseType) {
        switch (eraseType) {
            case EraseTypes.ERASER_MOVE:
                return ERASE_MOVE;
            case EraseTypes.ERASER_AREA:
                return ERASE_AREA;
            default:
                return ERASE_STROKE;
        }
    }

    @NonNull
    public static PenTool fromBundle(@NonNull PenBundle bundle) {
        return bundle.isErasing() ? fromEraseType(bundle.getCurrentEraseType()) : BRUSH;
    }
}
