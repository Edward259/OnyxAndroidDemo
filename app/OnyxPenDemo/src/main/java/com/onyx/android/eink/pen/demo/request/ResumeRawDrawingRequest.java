package com.onyx.android.eink.pen.demo.request;

import com.onyx.android.eink.pen.demo.core.PenBundle;
import com.onyx.android.eink.pen.demo.core.PenManager;
import com.onyx.android.sdk.rx.RxRequest;
import com.onyx.android.sdk.utils.ThreadUtils;

public class ResumeRawDrawingRequest extends RxRequest {

    private final PenManager penManager;
    private volatile boolean resumeRawDrawingRender;
    private volatile boolean resumeRawInputReader;
    private volatile int delayResumePenTimeMs;

    public ResumeRawDrawingRequest(PenManager penManager) {
        this.penManager = penManager;
    }

    public ResumeRawDrawingRequest setResumeRawDrawingRender(boolean resumeRawDrawingRender) {
        this.resumeRawDrawingRender = resumeRawDrawingRender;
        return this;
    }

    public ResumeRawDrawingRequest setResumeRawInputReader(boolean resumeRawInputReader) {
        this.resumeRawInputReader = resumeRawInputReader;
        return this;
    }

    public ResumeRawDrawingRequest setDelayResumePenTimeMs(int delayResumePenTimeMs) {
        this.delayResumePenTimeMs = delayResumePenTimeMs;
        return this;
    }

    @Override
    public void execute() throws Exception {
        if (penManager.getTouchHelper() == null) {
            return;
        }
        if (!resumeRawDrawingRender && !resumeRawInputReader) {
            return;
        }
        ThreadUtils.mySleep(delayResumePenTimeMs);
        PenBundle penBundle = PenBundle.getInstance();
        if (!penManager.getTouchHelper().isRawDrawingCreated()) {
            penManager.restartRawPenSession(penBundle);
        } else {
            penManager.restoreRawPenInput(penBundle);
        }
        penManager.setPenUpRefreshTimeMs(penBundle.getPenUpRefreshTimeMs());
        penManager.setDrawExcludeRect(penBundle.getExcludeRectList());
        if (resumeRawInputReader) {
            penManager.setRawInputReaderEnable(true);
        }
        if (resumeRawDrawingRender) {
            penManager.setRawDrawingRenderEnabled(true);
        } else if (resumeRawInputReader) {
            penManager.setRawDrawingRenderEnabled(false);
        }
    }
}
