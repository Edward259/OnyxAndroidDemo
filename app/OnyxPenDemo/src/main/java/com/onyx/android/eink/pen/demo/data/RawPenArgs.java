package com.onyx.android.eink.pen.demo.data;

import com.onyx.android.eink.pen.demo.event.PenEvent;

public class RawPenArgs {

    private boolean pauseRawDrawingRender;
    private boolean pauseRawInputReader;
    private boolean resumeRawDrawingRender;
    private boolean resumeRawInputReader;
    private int resumeDelayTime = PenEvent.DELAY_ENABLE_RAW_DRAWING_MILLS;

    public static RawPenArgs nothingArgs() {
        return new RawPenArgs();
    }

    public static RawPenArgs pauseArgs() {
        RawPenArgs args = new RawPenArgs();
        args.pauseRawDrawingRender = true;
        args.pauseRawInputReader = true;
        return args;
    }

    public static RawPenArgs resumeArgs() {
        RawPenArgs args = new RawPenArgs();
        args.resumeRawDrawingRender = true;
        args.resumeRawInputReader = true;
        return args;
    }

    public static RawPenArgs pauseResumeArgs() {
        RawPenArgs args = pauseArgs();
        args.resumeRawDrawingRender = true;
        args.resumeRawInputReader = true;
        return args;
    }

    public boolean isPauseRawDrawingRender() {
        return pauseRawDrawingRender;
    }

    public RawPenArgs setPauseRawDrawingRender(boolean pauseRawDrawingRender) {
        this.pauseRawDrawingRender = pauseRawDrawingRender;
        return this;
    }

    public boolean isPauseRawInputReader() {
        return pauseRawInputReader;
    }

    public RawPenArgs setPauseRawInputReader(boolean pauseRawInputReader) {
        this.pauseRawInputReader = pauseRawInputReader;
        return this;
    }

    public boolean isResumeRawDrawingRender() {
        return resumeRawDrawingRender;
    }

    public RawPenArgs setResumeRawDrawingRender(boolean resumeRawDrawingRender) {
        this.resumeRawDrawingRender = resumeRawDrawingRender;
        return this;
    }

    public boolean isResumeRawInputReader() {
        return resumeRawInputReader;
    }

    public RawPenArgs setResumeRawInputReader(boolean resumeRawInputReader) {
        this.resumeRawInputReader = resumeRawInputReader;
        return this;
    }

    public int getResumeDelayTime() {
        return resumeDelayTime;
    }

    public RawPenArgs setResumeDelayTime(int resumeDelayTime) {
        this.resumeDelayTime = resumeDelayTime;
        return this;
    }
}
