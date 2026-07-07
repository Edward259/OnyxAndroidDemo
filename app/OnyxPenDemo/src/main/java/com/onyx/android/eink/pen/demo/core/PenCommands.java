package com.onyx.android.eink.pen.demo.core;

import androidx.annotation.NonNull;

import com.onyx.android.eink.pen.demo.brush.data.ShapeFactory;
import com.onyx.android.eink.pen.demo.eventhandler.PenStateHandler;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * High-level pen commands for UI and session code. All pen-thread work delegates to {@link PenExecutor}.
 */
public final class PenCommands {

    private PenCommands() {
    }

    public static void run(@NonNull PenManager penManager, @NonNull PenExecutor.PenTask task) {
        executor(penManager).submit(task);
    }

    @NonNull
    public static Observable<PenManager> runAsync(@NonNull PenManager penManager,
                                                  @NonNull PenExecutor.PenTask task) {
        return executor(penManager).submitObservable(task)
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static void pauseHostSession(@NonNull PenManager penManager) {
        run(penManager, PenManager::pauseHostSession);
    }

    public static void onHostSurfaceDestroyed(@NonNull PenManager penManager) {
        run(penManager, PenManager::onHostSurfaceDestroyed);
    }

    public static void applyStrokeWidth(@NonNull PenBundle bundle, @NonNull PenManager penManager,
                                        int shapeType, float width) {
        bundle.setCurrentStrokeWidth(width);
        bundle.savePenLineWidth(shapeType, width);
        run(penManager, pm -> pm.setStrokeWidth(width));
    }

    public static void applyStrokeColor(@NonNull PenBundle bundle, @NonNull PenManager penManager, int color) {
        bundle.setCurrentStrokeColor(color);
        run(penManager, pm -> pm.setStrokeColor(color));
    }

    @NonNull
    public static Observable<PenManager> applyStrokeStyle(@NonNull PenBundle bundle, @NonNull PenManager penManager,
                                                          int shapeType, int texture) {
        bundle.setCurrentShapeType(shapeType);
        bundle.setCurrentTexture(texture);
        int strokeStyle = ShapeFactory.getStrokeStyle(shapeType, texture);
        return runAsync(penManager, pm -> pm.setStrokeStyle(strokeStyle));
    }

    public static void applyToolSwitch(@NonNull PenManager penManager,
                                       @NonNull PenStateHandler penStateHandler) {
        run(penManager, PenManager::applyToolSwitchWithRefresh);
        penStateHandler.pauseResumePenAfterModeSwitch();
    }

    @NonNull
    private static PenExecutor executor(@NonNull PenManager penManager) {
        return PenBundle.getInstance().getPenExecutor();
    }
}
