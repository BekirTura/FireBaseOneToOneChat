package com.donetr.chat.donefirebaseexample.custome_view;

import android.content.Context;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatTextView;


public class BadgeView extends AppCompatTextView {
    public BadgeView(Context context) {
        super(context);
        applyCustomFont(context);
    }

    public BadgeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyCustomFont(context);
    }

    public BadgeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        applyCustomFont(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        super.onSizeChanged(w, h, oldw, oldh);
        /*int h = getMeasuredHeight();
        int w = getMeasuredWidth();*/
        if (h == w) {
            return;
        }
        if (h > w) {
            setWidth(h);
        } else {
            setHeight(w);
        }
    }

    public void applyCustomFont(Context context) {
        /*Typeface customFont = FontCache.getTypeface(Constant.LIGHT_FONT, context);
        setTypeface(customFont);*/
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        int h = getMeasuredHeight();
        int w = getMeasuredWidth();
        if (h == w) {
            return;
        }
        if (h > w) {
            setWidth(h);
        } else {
            setHeight(w);
        }
    }
}
