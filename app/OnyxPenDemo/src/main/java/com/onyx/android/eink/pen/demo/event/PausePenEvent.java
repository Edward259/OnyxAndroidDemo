package com.onyx.android.eink.pen.demo.event;

public class PausePenEvent {

    private final String className;

    public PausePenEvent(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
