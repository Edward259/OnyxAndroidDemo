package com.onyx.android.eink.pen.demo.event;

public class ResumePenEvent {

    private final String className;
    private int delayResumePenTimeMs;

    public ResumePenEvent(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public int getDelayResumePenTimeMs() {
        return delayResumePenTimeMs;
    }

    public ResumePenEvent setDelayResumePenTimeMs(int delayResumePenTimeMs) {
        this.delayResumePenTimeMs = delayResumePenTimeMs;
        return this;
    }
}
