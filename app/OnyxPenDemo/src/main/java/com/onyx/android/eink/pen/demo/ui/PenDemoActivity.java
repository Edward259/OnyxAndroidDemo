package com.onyx.android.eink.pen.demo.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.databinding.DataBindingUtil;

import com.onyx.android.eink.pen.demo.core.PenManager;
import com.onyx.android.eink.pen.demo.core.PenTool;
import com.onyx.android.eink.pen.demo.core.PenSession;
import com.onyx.android.eink.pen.demo.R;
import com.onyx.android.eink.pen.demo.databinding.ActivityPenDemoBinding;
import com.onyx.android.eink.pen.demo.erase.ui.EraseSettingPop;
import com.onyx.android.eink.pen.demo.event.ApplyFastModeEvent;
import com.onyx.android.eink.pen.demo.brush.ui.PenSettingPop;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.api.device.epd.UpdateMode;
import com.onyx.android.sdk.utils.SystemPropertiesUtil;
import com.onyx.android.sdk.utils.ViewUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

public class PenDemoActivity extends Activity {

    private ActivityPenDemoBinding binding;
    private PenSession penSession;

    private final View.OnClickListener brushButtonClickListener = this::onBrushButtonClickImpl;
    private final View.OnClickListener eraseButtonClickListener = this::onEraseButtonClickImpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pen_demo);

        EpdController.enablePost(binding.getRoot(), 1);
        penSession = PenSession.create(this);
        penSession.initialize();
        initView();
        initListener();
        syncUiFromBundle();
    }

    @Override
    protected void onPause() {
        penSession.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        penSession.onResume();
    }

    @NotNull
    private LinearLayout getFloatMenuLayout() {
        return binding.floatMenuContainer.root;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        penSession.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onDestroy() {
        penSession.onDestroy();
        super.onDestroy();
    }

    private void initView() {
        ViewUtils.setViewVisibleOrGone(binding.penUpContainer, !SystemPropertiesUtil.isTablet());
        getSurfaceView().getHolder().addCallback(
                penSession.createSurfaceCallback(getSurfaceView(), getFloatMenuLayout()));
    }

    private void initListener() {
        getSurfaceView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        binding.brushButton.setOnClickListener(brushButtonClickListener);
        binding.eraseButton.setOnClickListener(eraseButtonClickListener);
        binding.floatMenuContainer.floatBrushButton.setOnClickListener(brushButtonClickListener);
        binding.floatMenuContainer.floatEraseButton.setOnClickListener(eraseButtonClickListener);

        bindDualCheckBox(binding.brushCheck, binding.floatMenuContainer.floatBrushCheck, this::onBrushCheckImpl);
        bindDualCheckBox(binding.eraseCheck, binding.floatMenuContainer.floatEraseCheck, this::onEraseCheckImpl);
        bindDualCheckBox(binding.penUpCheck, binding.floatMenuContainer.floatPenUpCheck, this::onPenUpCheckImpl);
    }

    private void bindDualCheckBox(CompoundButton main, CompoundButton floating,
                                  CompoundButton.OnCheckedChangeListener onChanged) {
        main.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (floating.isChecked() != isChecked) {
                floating.setChecked(isChecked);
                return;
            }
            onChanged.onCheckedChanged(buttonView, isChecked);
        });
        floating.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (main.isChecked() != isChecked) {
                main.setChecked(isChecked);
                return;
            }
            onChanged.onCheckedChanged(buttonView, isChecked);
        });
    }

    private void onPenUpCheckImpl(CompoundButton buttonView, boolean isChecked) {
        penSession.setPenUpRefreshEnabled(isChecked);
    }

    private void onEraseCheckImpl(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            binding.brushCheck.setChecked(false);
            penSession.setTool(PenTool.fromEraseType(penSession.getPenBundle().getCurrentEraseType()));
        }
    }

    private void onBrushCheckImpl(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            binding.eraseCheck.setChecked(false);
            penSession.setTool(PenTool.BRUSH);
        }
    }

    private void syncUiFromBundle() {
        PenTool tool = penSession.getCurrentTool();
        boolean erasing = tool.isErase();
        binding.brushCheck.setChecked(!erasing);
        binding.eraseCheck.setChecked(erasing);
        binding.penUpCheck.setChecked(penSession.getPenBundle().isEnablePenUpRefresh());
        binding.floatMenuContainer.floatBrushCheck.setChecked(!erasing);
        binding.floatMenuContainer.floatEraseCheck.setChecked(erasing);
        binding.floatMenuContainer.floatPenUpCheck.setChecked(
                penSession.getPenBundle().isEnablePenUpRefresh());
    }

    private void onBrushButtonClickImpl(View view) {
        binding.brushCheck.setChecked(true);
        PenSettingPop penSettingPop = new PenSettingPop(view.getContext());
        showPenSettingPop(view, penSettingPop);
    }

    private void onEraseButtonClickImpl(View view) {
        binding.eraseCheck.setChecked(true);
        EraseSettingPop eraseSettingPop = new EraseSettingPop(view.getContext())
                .setPenSession(penSession);
        showEraseSettingPop(view, eraseSettingPop);
    }

    private void showEraseSettingPop(View view, EraseSettingPop eraseSettingPop) {
        if (view.getId() == R.id.erase_button) {
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            int x = location[0] + view.getWidth();
            eraseSettingPop.showAsDropDown(view, x, 0, Gravity.NO_GRAVITY);
        } else if (view.getId() == R.id.float_erase_button) {
            eraseSettingPop.showAsDropDown(view, 0, 0, Gravity.NO_GRAVITY);
        }
    }

    private void showPenSettingPop(View view, PenSettingPop penSettingPop) {
        if (view.getId() == R.id.brush_button) {
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            int x = location[0] + view.getWidth();
            penSettingPop.showAsDropDown(view, x, 0, Gravity.NO_GRAVITY);

        } else if (view.getId() == R.id.float_brush_button) {
            penSettingPop.showAsDropDown(view, 0, 0, Gravity.NO_GRAVITY);
        }
    }

    private void applyApplicationFastMode(boolean enable) {
        if (enable) {
            EpdController.applyTransientUpdate(UpdateMode.ANIMATION_X);
        } else {
            EpdController.clearTransientUpdate(true);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onApplyFastModeEvent(ApplyFastModeEvent event) {
        applyApplicationFastMode(event.enable);
    }

    private SurfaceView getSurfaceView() {
        return binding.surfaceView;
    }

    public PenManager getPenManager() {
        return penSession.getPenManager();
    }
}
