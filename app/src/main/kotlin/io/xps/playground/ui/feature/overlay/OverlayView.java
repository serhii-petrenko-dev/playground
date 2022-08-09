package io.xps.playground.ui.feature.overlay;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.recyclerview.widget.LinearLayoutManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.xps.playground.databinding.OverlayNotificationsBinding;

public class OverlayView implements View.OnTouchListener {

    private final Context context;

    @Nullable
    private OverlayNotificationsBinding binding;
    private NotificationsAdapter adapter;

    protected Point windowSize;
    protected int lastRotation;
    protected WindowManager windowManager;

    public OverlayView(@NotNull Context context) {
        this.context = context;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowSize = getWindowSize(windowManager);
        lastRotation = getDisplayOrientation(windowManager);
    }

    public void attach() {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        binding = OverlayNotificationsBinding.inflate(layoutInflater);
        adapter = new NotificationsAdapter();
        binding.getRoot().setClipChildren(false);
        binding.getRoot().setClipToPadding(false);
        binding.notificationRecycler.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        binding.notificationRecycler.setLayoutManager(layoutManager);
        binding.notificationRecycler.setVisibility(View.VISIBLE);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                getOverlayType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        layoutParams.gravity = Gravity.START | Gravity.TOP;
        windowManager.addView(binding.getRoot(), layoutParams);
    }

    public void displayBubble(String message){
        adapter.displayBubble(message);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        return true;
    }

    private Point getWindowSize(WindowManager windowManager){
        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        return size;
    }

    private int getDisplayOrientation(WindowManager windowManager){
        Display display = windowManager.getDefaultDisplay();
        return display.getOrientation();
    }

    private int getOverlayType(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            //noinspection deprecation
            return WindowManager.LayoutParams.TYPE_PHONE;
        }
    }
}
