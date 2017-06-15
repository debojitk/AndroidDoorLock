package com.test.arduinosocket.activity.layouts;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.test.arduinosocket.R;


/**
 * Created by administrator on 5/27/2017.
 */

public class UnlockBarLayout extends RelativeLayout
{
    private OnUnlockListener listener = null;

    private ImageView imgSlider = null;
    private ImageView imgArrowLeft = null;
    private ImageView imgArrowRight = null;

    private int thumbWidth = 0;
    boolean sliding = false;
    private int sliderPosition;
    int initialSliderPosition;
    float initialSlidingX = 0;
    int baseSlidingPosition;
    int baseSlidingPositionLeftArrow;

    public UnlockBarLayout(Context context)
    {
        super(context);
        init(context, null);
    }

    public UnlockBarLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    public UnlockBarLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context, attrs);
    }
    public void setOnUnlockListener(OnUnlockListener listener)
    {
        this.listener = listener;
    }

    public void reset()
    {
        final LayoutParams params = (LayoutParams) imgSlider.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(params.leftMargin, baseSlidingPosition-thumbWidth/2);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator)
            {
                params.leftMargin = (Integer) valueAnimator.getAnimatedValue();
                imgSlider.requestLayout();
            }
        });
        animator.setDuration(300);
        animator.start();
        animator.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                imgArrowLeft.setVisibility(VISIBLE);
                imgArrowRight.setVisibility(VISIBLE);
            }
        });
    }
    public Animator animateLeftArrow()
    {
        final LayoutParams params = (LayoutParams) imgArrowLeft.getLayoutParams();
        final ValueAnimator animator = ValueAnimator.ofInt(params.leftMargin, params.leftMargin-30);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator)
            {
                params.leftMargin = (Integer) valueAnimator.getAnimatedValue();

                imgArrowLeft.setAlpha(1-valueAnimator.getAnimatedFraction());
                imgArrowLeft.requestLayout();
            }
        });
        animator.setDuration(800);
        animator.setRepeatCount(Animation.INFINITE);
        animator.start();
        return animator;
    }
    public Animator animateRightArrow()
    {
        final LayoutParams params = (LayoutParams) imgArrowRight.getLayoutParams();
        final ValueAnimator animator = ValueAnimator.ofInt(params.leftMargin, params.leftMargin+30);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator)
            {
                params.leftMargin = (Integer) valueAnimator.getAnimatedValue();

                imgArrowRight.setAlpha(1-valueAnimator.getAnimatedFraction());
                imgArrowRight.requestLayout();
            }
        });
        animator.setDuration(800);
        animator.setRepeatCount(Animation.INFINITE);
        animator.start();
        return animator;
    }



    private void init(Context context, AttributeSet attrs)
    {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.slide_image_layout, this, true);

        // Retrieve layout elements
        imgSlider = (ImageView) findViewById(R.id.img_thumb);
        imgArrowLeft = (ImageView) findViewById(R.id.img_arrow_left);
        imgArrowRight = (ImageView) findViewById(R.id.img_arrow_right);

        // Get padding
        thumbWidth = dpToPx(80); // 60dp + 2*10dp
        post(new Runnable() {
            @Override
            public void run() {
                sliderPosition = getMeasuredWidth()/2;
                initialSliderPosition = sliderPosition;
                initialSlidingX = 0;
                baseSlidingPosition=sliderPosition;
                //thumbWidth=imgSlider.getWidth();
                setMarginLeft(imgSlider,sliderPosition-(thumbWidth/2));
                setMarginLeft(imgArrowLeft,sliderPosition-(thumbWidth/2)-120);
                setMarginLeft(imgArrowRight,sliderPosition);
                animateLeftArrow();
                animateRightArrow();
            }
        });
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event)
    {
        super.onTouchEvent(event);
        //swipe start
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            if (event.getX() > sliderPosition-thumbWidth/2 && event.getX() < (sliderPosition + thumbWidth/2))
            {
                sliding = true;
                initialSlidingX = event.getX()-thumbWidth/2;
                initialSliderPosition = sliderPosition;
                imgArrowLeft.setVisibility(INVISIBLE);
                imgArrowRight.setVisibility(INVISIBLE);
            }
        }
        //swipe complete, finger released
        else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_OUTSIDE)
        {
            if (sliderPosition >= (getMeasuredWidth() - thumbWidth))
            {
                if (listener != null) listener.onReject();
            }else if (sliderPosition <= 0){
                if (listener != null) listener.onAccept();
            } else {
                sliding = false;
                sliderPosition = baseSlidingPosition;
                reset();
            }
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE && sliding)
        {
            Log.d("log", "x="+event.getX());

            //sliderPosition = (int) (initialSliderPosition + (event.getX() - initialSlidingX));
            sliderPosition = (int) ((event.getX() - thumbWidth/2));
            if (sliderPosition <= 0) {
                sliderPosition = 0;
            }else if (sliderPosition >= (getMeasuredWidth() - thumbWidth)) {
                sliderPosition = (int) (getMeasuredWidth()  - thumbWidth);
            }
            Log.d("log", "sliderPosition="+sliderPosition);
            setMarginLeft(imgSlider,sliderPosition);
        }

        return true;
    }

    private void setMarginLeft(View imgView, int leftMargin)
    {
        if (imgView == null) return;
        LayoutParams params = (LayoutParams) imgView.getLayoutParams();
        params.setMargins(leftMargin, 0, 0, 0);
        imgView.setLayoutParams(params);
    }

    private int dpToPx(int dp)
    {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float)dp * density);
    }

    public static interface OnUnlockListener {
        void onAccept();
        void onReject();
    }


}