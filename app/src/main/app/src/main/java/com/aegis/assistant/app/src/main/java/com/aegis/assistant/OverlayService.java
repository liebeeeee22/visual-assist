package com.aegis.assistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.app.NotificationCompat;

public class OverlayService extends Service {
    private WindowManager windowManager;
    private LinearLayout overlayView;
    private boolean mode1 = true;
    private boolean mode2 = false;
    private boolean mode3 = true;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, getNotification());
        createOverlay();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                "assist_channel",
                "Visual Assist",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Overlay service running");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private Notification getNotification() {
        return new NotificationCompat.Builder(this, "assist_channel")
                .setContentTitle("Visual Assist Tool")
                .setContentText("Active - Ready")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createOverlay() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = LayoutInflater.from(this);
        overlayView = (LinearLayout) inflater.inflate(R.layout.overlay_menu, null);

        Button btnMode1 = overlayView.findViewById(R.id.btn_mode1);
        Button btnMode2 = overlayView.findViewById(R.id.btn_mode2);
        Button btnMode3 = overlayView.findViewById(R.id.btn_mode3);
        TextView statusText = overlayView.findViewById(R.id.tv_status);

        updateButton(btnMode1, mode1, "TRACKING A");
        updateButton(btnMode2, mode2, "TRACKING B");
        updateButton(btnMode3, mode3, "VISUAL GUIDE");
        updateStatus(statusText);

        btnMode1.setOnClickListener(v -> {
            mode1 = !mode1;
            updateButton(btnMode1, mode1, "TRACKING A");
            updateStatus(statusText);
        });
        btnMode2.setOnClickListener(v -> {
            mode2 = !mode2;
            updateButton(btnMode2, mode2, "TRACKING B");
            updateStatus(statusText);
        });
        btnMode3.setOnClickListener(v -> {
            mode3 = !mode3;
            updateButton(btnMode3, mode3, "VISUAL GUIDE");
            updateStatus(statusText);
        });

        int layoutFlag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 50;
        params.y = 200;

        windowManager.addView(overlayView, params);

        TextView dragHandle = overlayView.findViewById(R.id.drag_handle);
        final int[] lastX = {0};
        final int[] lastY = {0};
        final float[] touchX = {0};
        final float[] touchY = {0};

        dragHandle.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX[0] = params.x;
                    lastY[0] = params.y;
                    touchX[0] = event.getRawX();
                    touchY[0] = event.getRawY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    params.x = lastX[0] + (int)(event.getRawX() - touchX[0]);
                    params.y = lastY[0] + (int)(event.getRawY() - touchY[0]);
                    windowManager.updateViewLayout(overlayView, params);
                    return true;
            }
            return false;
        });
    }

    private void updateButton(Button btn, boolean state, String label) {
        btn.setBackgroundColor(state ? Color.parseColor("#00AA00") : Color.parseColor("#AA0000"));
        btn.setText(label + "\n" + (state ? "ON" : "OFF"));
    }

    private void updateStatus(TextView tv) {
        int activeCount = (mode1 ? 1 : 0) + (mode2 ? 1 : 0) + (mode3 ? 1 : 0);
        tv.setText("● ACTIVE — " + activeCount + "/3 modes ON");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null && windowManager != null) {
            windowManager.removeView(overlayView);
        }
    }
}
