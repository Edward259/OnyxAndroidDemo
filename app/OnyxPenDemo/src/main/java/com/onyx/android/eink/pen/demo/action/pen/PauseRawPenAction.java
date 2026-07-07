package com.onyx.android.eink.pen.demo.action.pen;

import com.onyx.android.eink.pen.demo.core.PenBundle;
import com.onyx.android.eink.pen.demo.core.PenManager;
import com.onyx.android.eink.pen.demo.data.RawPenArgs;
import com.onyx.android.sdk.rx.RxBaseAction;

import io.reactivex.Observable;

public class PauseRawPenAction extends RxBaseAction<PauseRawPenAction> {

    private final RawPenArgs rawPenArgs;

    public PauseRawPenAction() {
        this(RawPenArgs.pauseArgs());
    }

    public PauseRawPenAction(RawPenArgs rawPenArgs) {
        this.rawPenArgs = rawPenArgs;
    }

    @Override
    protected Observable<PauseRawPenAction> create() {
        return getPenManager().createObservable().map(o -> run());
    }

    private PauseRawPenAction run() {
        PenManager penManager = getPenManager();
        if (rawPenArgs.isPauseRawDrawingRender() && rawPenArgs.isPauseRawInputReader()) {
            penManager.setRawDrawingEnabled(false);
            return this;
        }
        if (rawPenArgs.isPauseRawDrawingRender()) {
            penManager.setRawDrawingRenderEnabled(false);
        }
        if (rawPenArgs.isPauseRawInputReader()) {
            penManager.setRawInputReaderEnable(false);
        }
        return this;
    }

    private PenManager getPenManager() {
        return PenBundle.getInstance().getPenManager();
    }
}
