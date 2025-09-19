package com.smartassistant.overlay;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Handler;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.view.ViewGroup;

/**
 * Service for displaying gaming tips as floating bubbles over other apps
 */
public class BubbleOverlayService extends Service {
    private static final String TAG = "BubbleOverlayService";
    
    public static final String ACTION_SHOW_TIP = "SHOW_TIP";
    public static final String EXTRA_TIP_TEXT = "tip_text";
    public static final String EXTRA_TIP_TYPE = "tip_type";
    
    // Tip types
    public static final String TIP_TYPE_ATTACK = "ATTACK";
    public static final String TIP_TYPE_DEFENSE = "DEFENSE";
    public static final String TIP_TYPE_ELIXIR = "ELIXIR";
    public static final String TIP_TYPE_TIME = "TIME";
    public static final String TIP_TYPE_GENERAL = "GENERAL";
    
    private WindowManager windowManager;
    private View bubbleView;
    private View expandedView;
    private boolean isExpanded = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        createBubbleView();
        Log.d(TAG, "Bubble Overlay Service created");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            
            if (ACTION_SHOW_TIP.equals(action)) {
                String tipText = intent.getStringExtra(EXTRA_TIP_TEXT);
                String tipType = intent.getStringExtra(EXTRA_TIP_TYPE);
                showTip(tipText, tipType);
            }
        }
        
        return START_STICKY;
    }
    
    private void createBubbleView() {
        // Create bubble view (small floating button)
        bubbleView = new LinearLayout(this);
        bubbleView.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        
        // Create bubble icon
        ImageView bubbleIcon = new ImageView(this);
        bubbleIcon.setImageResource(android.R.drawable.ic_dialog_info); // Use system icon for now
        bubbleIcon.setPadding(20, 20, 20, 20);
        bubbleIcon.setBackgroundResource(android.R.drawable.sym_def_app_icon); // Simple background
        
        ((LinearLayout) bubbleView).addView(bubbleIcon);
        
        // Bubble click listener
        bubbleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExpanded) {
                    collapseBubble();
                } else {
                    expandBubble();
                }
            }
        });
        
        // Bubble touch listener for dragging
        bubbleView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            private boolean isDragging = false;
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = ((WindowManager.LayoutParams) bubbleView.getLayoutParams()).x;
                        initialY = ((WindowManager.LayoutParams) bubbleView.getLayoutParams()).y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isDragging = false;
                        return true;
                        
                    case MotionEvent.ACTION_MOVE:
                        if (!isDragging && (Math.abs(event.getRawX() - initialTouchX) > 10 || 
                            Math.abs(event.getRawY() - initialTouchY) > 10)) {
                            isDragging = true;
                        }
                        
                        if (isDragging) {
                            WindowManager.LayoutParams params = (WindowManager.LayoutParams) bubbleView.getLayoutParams();
                            params.x = initialX + (int) (event.getRawX() - initialTouchX);
                            params.y = initialY + (int) (event.getRawY() - initialTouchY);
                            windowManager.updateViewLayout(bubbleView, params);
                        }
                        return true;
                        
                    case MotionEvent.ACTION_UP:
                        if (!isDragging) {
                            // This was a click, not a drag
                            v.performClick();
                        }
                        return true;
                }
                return false;
            }
        });
        
        // Add bubble to window
        WindowManager.LayoutParams bubbleParams = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        );
        
        bubbleParams.gravity = Gravity.TOP | Gravity.START;
        bubbleParams.x = 100;
        bubbleParams.y = 100;
        
        windowManager.addView(bubbleView, bubbleParams);
    }
    
    private void showTip(String tipText, String tipType) {
        if (tipText == null || tipText.isEmpty()) return;
        
        // Update bubble icon based on tip type
        ImageView bubbleIcon = (ImageView) ((LinearLayout) bubbleView).getChildAt(0);
        updateBubbleIcon(bubbleIcon, tipType);
        
        // Store tip for expanded view
        bubbleView.setTag(tipText);
        
        // Show brief animation to indicate new tip
        animateBubble();
        
        Log.d(TAG, "Showing tip: " + tipText);
    }
    
    private void updateBubbleIcon(ImageView icon, String tipType) {
        // Use different system icons based on tip type
        switch (tipType != null ? tipType : TIP_TYPE_GENERAL) {
            case TIP_TYPE_ATTACK:
                icon.setImageResource(android.R.drawable.ic_media_play); // Arrow-like icon
                break;
            case TIP_TYPE_DEFENSE:
                icon.setImageResource(android.R.drawable.ic_menu_close_clear_cancel); // Shield-like
                break;
            case TIP_TYPE_ELIXIR:
                icon.setImageResource(android.R.drawable.ic_dialog_alert); // Energy icon
                break;
            case TIP_TYPE_TIME:
                icon.setImageResource(android.R.drawable.ic_lock_idle_alarm); // Clock icon
                break;
            default:
                icon.setImageResource(android.R.drawable.ic_dialog_info); // General info
                break;
        }
    }
    
    private void animateBubble() {
        // Create pulse animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(bubbleView, "scaleX", 1.0f, 1.2f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(bubbleView, "scaleY", 1.0f, 1.2f, 1.0f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(500);
        animatorSet.start();
    }
    
    private void expandBubble() {
        if (isExpanded || bubbleView.getTag() == null) return;
        
        String tipText = (String) bubbleView.getTag();
        
        // Create expanded view
        LinearLayout expandedLayout = new LinearLayout(this);
        expandedLayout.setOrientation(LinearLayout.VERTICAL);
        expandedLayout.setPadding(20, 15, 20, 15);
        expandedLayout.setBackgroundColor(0xE0000000); // Semi-transparent black background
        
        TextView tipTextView = new TextView(this);
        tipTextView.setText(tipText);
        tipTextView.setTextColor(0xFFFFFFFF); // White text
        tipTextView.setTextSize(14);
        tipTextView.setMaxWidth(300);
        
        expandedLayout.addView(tipTextView);
        
        // Add close indicator
        TextView closeHint = new TextView(this);
        closeHint.setText("اضغط للإخفاء");
        closeHint.setTextColor(0xFFCCCCCC);
        closeHint.setTextSize(10);
        closeHint.setPadding(0, 10, 0, 0);
        
        expandedLayout.addView(closeHint);
        
        expandedView = expandedLayout;
        
        // Expanded view click listener
        expandedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collapseBubble();
            }
        });
        
        // Add expanded view to window
        WindowManager.LayoutParams expandedParams = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        );
        
        // Position near bubble
        WindowManager.LayoutParams bubbleParams = (WindowManager.LayoutParams) bubbleView.getLayoutParams();
        expandedParams.gravity = Gravity.TOP | Gravity.START;
        expandedParams.x = bubbleParams.x + 60; // Offset from bubble
        expandedParams.y = bubbleParams.y;
        
        windowManager.addView(expandedView, expandedParams);
        isExpanded = true;
        
        // Auto-collapse after 5 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isExpanded) {
                    collapseBubble();
                }
            }
        }, 5000);
    }
    
    private void collapseBubble() {
        if (!isExpanded || expandedView == null) return;
        
        try {
            windowManager.removeView(expandedView);
        } catch (Exception e) {
            Log.w(TAG, "Error removing expanded view", e);
        }
        
        expandedView = null;
        isExpanded = false;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // Remove all views
        try {
            if (bubbleView != null) {
                windowManager.removeView(bubbleView);
            }
            if (expandedView != null) {
                windowManager.removeView(expandedView);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error removing views", e);
        }
        
        Log.d(TAG, "Bubble Overlay Service destroyed");
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}