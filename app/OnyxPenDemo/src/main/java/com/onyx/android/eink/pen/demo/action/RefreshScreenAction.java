package com.onyx.android.eink.pen.demo.action;

import com.onyx.android.eink.pen.demo.core.PenBundle;
import com.onyx.android.eink.pen.demo.core.PenManager;
import com.onyx.android.eink.pen.demo.data.RawPenArgs;
import com.onyx.android.eink.pen.demo.event.PenEvent;
import com.onyx.android.sdk.rx.RxBaseAction;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

/** Full-screen refresh with configurable {@link RawPenArgs} pause/resume around the invalidate. */
public class RefreshScreenAction extends RxBaseAction<RefreshScreenAction> {

    private final RawPenArgs rawPenArgs = RawPenArgs.pauseResumeArgs();
    private int delayRefreshTime;

    public RefreshScreenAction() {
        rawPenArgs.setResumeDelayTime(PenEvent.DELAY_ENABLE_RAW_DRAWING_MILLS);
    }

    @Override
    protected Observable<RefreshScreenAction> create() {
        return getDelayObservable()
                .flatMap(o -> new InvalidateViewWithPenControlAction(rawPenArgs).build()
                        .map(action -> this));
    }

    public RefreshScreenAction setResumeRawDrawing(boolean resumeRawDrawing) {
        rawPenArgs.setResumeRawDrawingRender(resumeRawDrawing);
        rawPenArgs.setResumeRawInputReader(resumeRawDrawing);
        return this;
    }

    public RefreshScreenAction setPauseRawInputReader(boolean pause) {
        rawPenArgs.setPauseRawInputReader(pause);
        return this;
    }

    public RefreshScreenAction setPauseRawDrawingRender(boolean pause) {
        rawPenArgs.setPauseRawDrawingRender(pause);
        return this;
    }

    public RefreshScreenAction setDelayResumePenTimeMs(int delayResumePenTimeMs) {
        rawPenArgs.setResumeDelayTime(delayResumePenTimeMs);
        return this;
    }

    public RefreshScreenAction setDelayRefreshTime(int delayRefreshTime) {
        this.delayRefreshTime = delayRefreshTime;
        return this;
    }

    private Observable<RefreshScreenAction> getDelayObservable() {
        Observable<RefreshScreenAction> observable =
                Observable.just(this)
                .observeOn(getScheduler());
        if (delayRefreshTime == 0 ) {
            return observable;
        }
        return observable.delay(delayRefreshTime, TimeUnit.MILLISECONDS);
    }

    public PenBundle getDataBundle() {
        return PenBundle.getInstance();
    }

    public PenManager getPenManager() {
        return getDataBundle().getPenManager();
    }

    public Scheduler getScheduler() {
        return getPenManager().getObserveOn();
    }
}
