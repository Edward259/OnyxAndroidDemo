package com.onyx.android.eink.pen.demo.action.pen;

import com.onyx.android.eink.pen.demo.data.RawPenArgs;
import com.onyx.android.sdk.rx.RxBaseAction;

import io.reactivex.Observable;

public class PauseResumeRawPenAction extends RxBaseAction<PauseResumeRawPenAction> {

    private RawPenArgs pauseArgs = RawPenArgs.pauseArgs();
    private RawPenArgs resumeArgs = RawPenArgs.resumeArgs();

    public PauseResumeRawPenAction setPauseArgs(RawPenArgs pauseArgs) {
        this.pauseArgs = pauseArgs;
        return this;
    }

    public PauseResumeRawPenAction setResumeArgs(RawPenArgs resumeArgs) {
        this.resumeArgs = resumeArgs;
        return this;
    }

    @Override
    protected Observable<PauseResumeRawPenAction> create() {
        return new PauseRawPenAction(pauseArgs).build()
                .flatMap(o -> new ResumeRawPenAction(resumeArgs).build())
                .map(o -> this);
    }
}
