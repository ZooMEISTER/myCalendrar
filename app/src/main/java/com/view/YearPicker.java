package com.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.DatePicker;

import java.util.Locale;

public final class YearPicker extends DatePicker {
    public YearPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) getChildAt(0)).getChildAt(0);
        int groupChildCount = viewGroup.getChildCount();
        switch (Locale.getDefault().getLanguage()) {
            case "zh":
                if (groupChildCount == 3) {
                    viewGroup.getChildAt(2).setVisibility(GONE);
                    viewGroup.getChildAt(1).setVisibility(GONE);
                }
                else if (groupChildCount == 5) {
                    viewGroup.getChildAt(3).setVisibility(GONE);
                    viewGroup.getChildAt(4).setVisibility(GONE);
                }
                break;
            case "en":
                if (groupChildCount == 3) {
                    viewGroup.getChildAt(1).setVisibility(GONE);
                    viewGroup.getChildAt(0).setVisibility(GONE);
                }
                else if (groupChildCount == 5) {
                    viewGroup.getChildAt(2).setVisibility(GONE);
                    viewGroup.getChildAt(3).setVisibility(GONE);
                }
                break;

        }
    }

}