package com.github.florent37;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

/**
 * Created by florentchampigny on 19/04/15.
 */
public class Baguette {

    /**
     * @hide
     */
    @IntDef({LENGTH_SHORT, LENGTH_LONG})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Duration {
    }

    /**
     * Show the view or text notification for a short period of time.  This time
     * could be user-definable.  This is the default.
     */
    public static final int LENGTH_SHORT = 0;

    /**
     * Show the view or text notification for a long period of time.  This time
     * could be user-definable.
     */
    public static final int LENGTH_LONG = 1;

    private static final int LONG_DELAY = 3500; // 3.5 seconds
    private static final int SHORT_DELAY = 2000; // 2 seconds

    Context mContext;
    CharSequence mText;
    int mDuration;
    View mView;

    private Baguette(Context context, CharSequence text, int duration) {
        this.mContext = context;
        this.mText = text;
        this.mDuration = duration;

        View v = LayoutInflater.from(mContext).inflate(R.layout.baguette_layout, null, false);
        TextView tv = (TextView) v.findViewById(R.id.message);
        tv.setText(text);

        mView = v;
    }

    public static Baguette makeText(Context context, CharSequence text, @Duration int duration) {
        Baguette baguette = new Baguette(context, text, duration);
        return baguette;
    }

    public static Baguette makeText(Context context, int resId, @Duration int duration) {
        return makeText(context, context.getResources().getText(resId), duration);
    }

    private static Baguette currentBaguette = null;
    private Baguette nextBaguette = null;

    public void show() {
        if(currentBaguette == null) {
            currentBaguette = this;
            doShow();
        }
        else{
            currentBaguette.nextBaguette = this;
        }
    }

    public void cancel() {

    }

    private int comuteDuration() {
        if (mDuration == LENGTH_SHORT)
            return SHORT_DELAY;
        else return LONG_DELAY;
    }

    final static Handler mHandler = new Handler(Looper.getMainLooper());

    private void handleShow() {
        if(mView != null && mContext != null && mContext instanceof Activity) {
            ((ViewGroup)((Activity)mContext).getWindow().getDecorView()).addView(mView);
            mView.getViewTreeObserver().addOnDrawListener(new ViewTreeObserver.OnDrawListener() {
                @Override
                public void onDraw() {
                    mView.setAlpha(0);
                    mView.setTranslationY(mView.getHeight());
                    mView.animate().alpha(1).translationY(0).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mHandler.postDelayed(mHide, comuteDuration());
                        }
                    }).setDuration(500).start();
                    mView.getViewTreeObserver().removeOnDrawListener(this);
                }
            });
        }
    }

    private void handleHide() {
        mView.animate().alpha(0).translationY(mView.getHeight()).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ((ViewGroup) mView.getParent()).removeView(mView);
                currentBaguette = nextBaguette;

                if (currentBaguette != null) {
                    currentBaguette.doShow();
                }
            }
        }).setDuration(500).start();
    }

    private void doShow() {
        mHandler.post(mShow);
    }

    private void doHide() {
        mHandler.post(mHide);
    }

    final Runnable mShow = new Runnable() {
        @Override
        public void run() {
            handleShow();
        }
    };

    final Runnable mHide = new Runnable() {
        @Override
        public void run() {
            handleHide();
        }
    };

}
