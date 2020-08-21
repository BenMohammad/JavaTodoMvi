package com.benmohammad.javatodomvi.tasks;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.view.ViewCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ScrollChildSwipeRefreshLayout extends SwipeRefreshLayout {

    private View scrollUpChild;

    public ScrollChildSwipeRefreshLayout(Context context){
        super(context);
    }

    public ScrollChildSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean canChildScrollUp() {
        if(scrollUpChild != null) {
            return ViewCompat.canScrollVertically(scrollUpChild, -1);
        }
        return super.canChildScrollUp();
    }

    public void setScrollUpChild(View v) {
        scrollUpChild = v;
    }


}
