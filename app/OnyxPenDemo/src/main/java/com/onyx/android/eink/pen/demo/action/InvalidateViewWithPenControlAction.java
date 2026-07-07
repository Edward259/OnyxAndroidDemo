package com.onyx.android.eink.pen.demo.action;

import com.onyx.android.eink.pen.demo.core.PenBundle;
import com.onyx.android.eink.pen.demo.core.PenManager;
import com.onyx.android.eink.pen.demo.action.pen.PauseRawPenAction;
import com.onyx.android.eink.pen.demo.action.pen.ResumeRawPenAction;
import com.onyx.android.eink.pen.demo.data.RawPenArgs;
import com.onyx.android.eink.pen.demo.event.PenEvent;
import com.onyx.android.sdk.rx.RxBaseAction;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

/**
 * Pause SF pen → render host view to screen → resume via {@link PenEvent} gates.
 */
public class InvalidateViewWithPenControlAction extends RxBaseAction<InvalidateViewWithPenControlAction> {

    private RawPenArgs rawPenArgs = RawPenArgs.pauseResumeArgs();

    public InvalidateViewWithPenControlAction() {
    }

    public InvalidateViewWithPenControlAction(RawPenArgs rawPenArgs) {
        this.rawPenArgs = rawPenArgs;
    }

    public InvalidateViewWithPenControlAction setRawPenArgs(RawPenArgs rawPenArgs) {
        this.rawPenArgs = rawPenArgs;
        return this;
    }

    @Override
    protected Observable<InvalidateViewWithPenControlAction> create() {
        return new PauseRawPenAction(rawPenArgs).build()
                .observeOn(getScheduler())
                .map(o -> render())
                .flatMap(o -> new ResumeRawPenAction(rawPenArgs).build())
                .map(o -> this);
    }

    private InvalidateViewWithPenControlAction render() throws Exception {
        getPenManager().renderToScreen();
        return this;
    }

    public PenManager getPenManager() {
        return PenBundle.getInstance().getPenManager();
    }

    public Scheduler getScheduler() {
        return getPenManager().getObserveOn();
    }
}
