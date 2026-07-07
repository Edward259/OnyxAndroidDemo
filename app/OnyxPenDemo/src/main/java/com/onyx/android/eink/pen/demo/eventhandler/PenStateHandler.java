package com.onyx.android.eink.pen.demo.eventhandler;

import com.onyx.android.eink.pen.demo.action.RefreshScreenAction;
import com.onyx.android.eink.pen.demo.action.pen.PauseRawPenAction;
import com.onyx.android.eink.pen.demo.action.pen.PauseResumeRawPenAction;
import com.onyx.android.eink.pen.demo.core.PenBundle;
import com.onyx.android.eink.pen.demo.core.PenManager;
import com.onyx.android.eink.pen.demo.data.RawPenArgs;
import com.onyx.android.eink.pen.demo.erase.util.EraserTrackHelper;
import com.onyx.android.eink.pen.demo.event.ActivityFocusChangedEvent;
import com.onyx.android.eink.pen.demo.event.DemoFloatMenuStateChangeEvent;
import com.onyx.android.eink.pen.demo.event.FloatButtonChangedEvent;
import com.onyx.android.eink.pen.demo.event.FloatButtonMenuStateChangedEvent;
import com.onyx.android.eink.pen.demo.event.NotificationPanelChangeEvent;
import com.onyx.android.eink.pen.demo.event.PausePenEvent;
import com.onyx.android.eink.pen.demo.event.PenEvent;
import com.onyx.android.eink.pen.demo.event.PopupWindowChangeEvent;
import com.onyx.android.eink.pen.demo.event.ResumePenEvent;
import com.onyx.android.eink.pen.demo.event.StatusBarChangeEvent;
import com.onyx.android.eink.pen.demo.request.ResumeRawDrawingRequest;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashSet;
import java.util.Set;

/**
 * Central pause / resume gates and EventBus routing for SF raw drawing.
 */
public class PenStateHandler {

    private static final int SYSTEM_FLOAT_MENU_DELAY_RESUME_PEN_TIME_MS = 500;

    private int popupShowingCount;
    private boolean statusBarShowing;
    private boolean notificationPanelShowing;
    private boolean floatButtonActivated;
    private boolean demoFloatMenuActivated;
    private boolean hasFocus = true;
    private boolean activityResumed;
    private boolean isPause;
    private final Set<String> pausePausers = new HashSet<>();

    public void onActivityResume() {
        activityResumed = true;
    }

    public void onActivityPause() {
        activityResumed = false;
    }

    public void pauseFromLifecycle(String source) {
        applyPausePen(source);
    }

    public void resumeFromLifecycle(String source) {
        applyResumePen(source, 0);
    }

    public void refreshScreen() {
        new RefreshScreenAction().execute();
    }

    public void pauseResumePenAfterModeSwitch() {
        pauseResumeRawPen(PenEvent.DELAY_ENABLE_RAW_DRAWING_MILLS);
    }

    public void resumePenAfterErase() {
        PenBundle bundle = getPenBundle();
        int eraseType = bundle.getCurrentEraseType();
        boolean resumeRender = !bundle.isErasing()
                || EraserTrackHelper.useSfTrack(bundle, eraseType);
        ResumeRawDrawingRequest request = new ResumeRawDrawingRequest(getPenManager())
                .setResumeRawDrawingRender(resumeRender)
                .setResumeRawInputReader(true)
                .setDelayResumePenTimeMs(PenEvent.DELAY_ENABLE_RAW_DRAWING_MILLS);
        getPenBundle().getPenExecutor().submitRequest(request);
    }

    public void quit() {
        popupShowingCount = 0;
        statusBarShowing = false;
        notificationPanelShowing = false;
        floatButtonActivated = false;
        demoFloatMenuActivated = false;
        hasFocus = true;
        activityResumed = false;
        isPause = false;
        pausePausers.clear();
    }

    private void applyPausePen(String className) {
        if (className != null) {
            pausePausers.add(className);
        }
        isPause = true;
        pauseResumeRawPen();
    }

    private void applyResumePen(String className, int delayResumePenTimeMs) {
        if (className != null) {
            pausePausers.remove(className);
        } else {
            pausePausers.clear();
        }
        isPause = !pausePausers.isEmpty();
        if (delayResumePenTimeMs > 0) {
            pauseResumeRawPen(delayResumePenTimeMs);
        } else {
            pauseResumeRawPen();
        }
    }

    private boolean shouldResumeRawRender(boolean resumePen) {
        return resumePen
                && canPenRawRenderInCurrentMode()
                && !notificationPanelShowing
                && !statusBarShowing
                && !isPopupWindowShowing()
                && !floatButtonActivated
                && !demoFloatMenuActivated
                && hasFocus
                && activityResumed
                && !isPause;
    }

    private boolean canPenRawRenderInCurrentMode() {
        PenBundle bundle = getPenBundle();
        if (!bundle.isErasing()) {
            return true;
        }
        return EraserTrackHelper.useSfTrack(bundle, bundle.getCurrentEraseType());
    }

    private boolean shouldResumeRawInput(boolean resumeInput) {
        return resumeInput
                && !notificationPanelShowing
                && !statusBarShowing
                && !isPopupWindowShowing()
                && !floatButtonActivated
                && !demoFloatMenuActivated
                && hasFocus
                && activityResumed
                && !isPause;
    }

    private boolean isPopupWindowShowing() {
        return popupShowingCount > 0;
    }

    private void pauseRawDrawing() {
        new PauseRawPenAction().execute();
    }

    private void pauseResumeRawPen() {
        new PauseResumeRawPenAction().execute();
    }

    private void pauseResumeRawPen(int delayResumePenTimeMs) {
        RawPenArgs resumeArgs = RawPenArgs.resumeArgs().setResumeDelayTime(delayResumePenTimeMs);
        new PauseResumeRawPenAction().setResumeArgs(resumeArgs).execute();
    }

    private void resumeRawDrawing(boolean resumeRender, boolean resumeInput, int delayResumePenTime) {
        final boolean render = shouldResumeRawRender(resumeRender);
        final boolean input = shouldResumeRawInput(resumeInput);
        if (!render && !input) {
            return;
        }
        ResumeRawDrawingRequest request = new ResumeRawDrawingRequest(getPenManager())
                .setResumeRawDrawingRender(render)
                .setResumeRawInputReader(input)
                .setDelayResumePenTimeMs(delayResumePenTime);
        getPenBundle().getPenExecutor().submitRequest(request);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPausePenEvent(PausePenEvent event) {
        applyPausePen(event.getClassName());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResumePenEvent(ResumePenEvent event) {
        applyResumePen(event.getClassName(), event.getDelayResumePenTimeMs());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPenEvent(PenEvent event) {
        resumeRawDrawing(event.isResumeDrawingRender(),
                event.isResumeRawInputReader(),
                event.getDelayResumePenTimeMs());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStatusBarChangeEvent(StatusBarChangeEvent event) {
        statusBarShowing = event.show;
        if (event.show) {
            pauseRawDrawing();
        } else {
            resumeRawDrawing(true, true, PenEvent.DELAY_ENABLE_RAW_DRAWING_MILLS);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNotificationPanelChangeEvent(NotificationPanelChangeEvent event) {
        notificationPanelShowing = event.show;
        if (event.show) {
            pauseRawDrawing();
        } else {
            resumeRawDrawing(true, true, PenEvent.DELAY_ENABLE_RAW_DRAWING_MILLS);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onActivityFocusChangedEvent(ActivityFocusChangedEvent event) {
        hasFocus = event.hasFocus;
        if (hasFocus && statusBarShowing) {
            statusBarShowing = false;
        }
        refreshScreen();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFloatButtonChangedEvent(FloatButtonChangedEvent event) {
        floatButtonActivated = event.active;
        new RefreshScreenAction()
                .setDelayResumePenTimeMs(SYSTEM_FLOAT_MENU_DELAY_RESUME_PEN_TIME_MS)
                .execute();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFloatButtonMenuStateChangedEvent(FloatButtonMenuStateChangedEvent event) {
        floatButtonActivated = event.active;
        new RefreshScreenAction()
                .setDelayResumePenTimeMs(SYSTEM_FLOAT_MENU_DELAY_RESUME_PEN_TIME_MS)
                .execute();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDemoFloatMenuStateChangeEvent(DemoFloatMenuStateChangeEvent event) {
        demoFloatMenuActivated = event.active;
        refreshScreen();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPopupWindowChangeEvent(PopupWindowChangeEvent event) {
        if (event.show) {
            popupShowingCount++;
            pauseRawDrawing();
        } else {
            popupShowingCount = Math.max(0, popupShowingCount - 1);
            resumeRawDrawing(true, true, PenEvent.POPUP_RESUME_PEN_TIME_MS);
        }
    }

    private PenBundle getPenBundle() {
        return PenBundle.getInstance();
    }

    private PenManager getPenManager() {
        return getPenBundle().getPenManager();
    }
}
