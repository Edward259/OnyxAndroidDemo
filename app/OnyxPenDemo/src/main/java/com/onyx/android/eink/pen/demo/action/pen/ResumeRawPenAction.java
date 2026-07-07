package com.onyx.android.eink.pen.demo.action.pen;

import com.onyx.android.eink.pen.demo.core.PenBundle;
import com.onyx.android.eink.pen.demo.core.PenManager;
import com.onyx.android.eink.pen.demo.data.RawPenArgs;
import com.onyx.android.eink.pen.demo.event.PenEvent;
import com.onyx.android.sdk.rx.RxBaseAction;
import com.onyx.android.sdk.utils.EventBusUtils;

import io.reactivex.Observable;

public class ResumeRawPenAction extends RxBaseAction<ResumeRawPenAction> {

    private final RawPenArgs rawPenArgs;

    public ResumeRawPenAction() {
        this(RawPenArgs.resumeArgs());
    }

    public ResumeRawPenAction(RawPenArgs rawPenArgs) {
        this.rawPenArgs = rawPenArgs;
    }

    @Override
    protected Observable<ResumeRawPenAction> create() {
        return getPenManager().createObservable().map(o -> run());
    }

    private ResumeRawPenAction run() {
        if (!rawPenArgs.isResumeRawDrawingRender() && !rawPenArgs.isResumeRawInputReader()) {
            return this;
        }
        EventBusUtils.safelyPostEvent(getPenManager().getEventBus(),
                new PenEvent(rawPenArgs.isResumeRawDrawingRender(),
                        rawPenArgs.isResumeRawInputReader(),
                        rawPenArgs.getResumeDelayTime()));
        return this;
    }

    private PenManager getPenManager() {
        return PenBundle.getInstance().getPenManager();
    }
}
