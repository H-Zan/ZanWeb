/*
 * Copyright (C)  LeonDevLifeLog(https://github.com/Justson/AgentWeb)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zan.webviewdemo.behavior;

import android.content.Context;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.zan.webviewdemo.util.ZanL;


/**
 * 与toolbar联动隐藏底部菜单
 */
public class BottomNavigationViewBehavior extends CoordinatorLayout.Behavior<View> {
    public BottomNavigationViewBehavior() {
    }

    public BottomNavigationViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
//
//    @Override
//    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
//        ((CoordinatorLayout.LayoutParams) child.getLayoutParams()).topMargin = parent
//                .getMeasuredHeight() - child.getMeasuredHeight();
//        return super.onLayoutChild(parent, child, layoutDirection);
//    }
//
//
//    @Override
//    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
//        //1. care about who
////        return dependency instanceof AppBarLayout;
//        ZanL.e("aaaaaaaaaaaa","layoutDependsOn");
//
//        boolean b = dependency instanceof NestedScrollWebView;
//        if (b) {
//            ZanL.e("aaaaaaaaaaaa","dependency instanceof NestedScrollWebView");
//        }
//        return b;
//    }

    /*@Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        //得到依赖View的滑动距离
        int top = ((AppBarLayout.Behavior) ((CoordinatorLayout.LayoutParams) dependency
                .getLayoutParams()).getBehavior()).getTopAndBottomOffset();
        //因为BottomNavigation的滑动与ToolBar是反向的，所以取负值
        ViewCompat.setTranslationY(child, -(top * child.getMeasuredHeight() / dependency
                .getMeasuredHeight()));
        return false;
    }*/

//    @Override
//    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
//        ZanL.e("aaaaaaaaaaaa","onDependentViewChanged");
//
//        /*//得到依赖View的滑动距离
//        int top = ((AppBarLayout.Behavior) ((CoordinatorLayout.LayoutParams) dependency
//                .getLayoutParams()).getBehavior()).getTopAndBottomOffset();
//        //因为BottomNavigation的滑动与ToolBar是反向的，所以取负值
//        ViewCompat.setTranslationY(child, -(top * child.getMeasuredHeight() / dependency
//                .getMeasuredHeight()));*/
//
//        int offset = dependency.getTop() - child.getTop();
//        ZanL.e("aaaaaaaaaaaa","offset:"+offset);
//
//        ViewCompat.offsetTopAndBottom(child, offset);
//        return true;
//    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout,
                                       View child, View directTargetChild, View target, int nestedScrollAxes) {
        ZanL.e("aaaaaaaaaaaa","onStartNestedScroll");

        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target,
                        nestedScrollAxes);
    }

    private boolean mIsAnimatingOut = false;
    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, View child,
                               View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        ZanL.e("aaaaaaaaaaaa","onNestedScroll:dyConsumed:"+dyConsumed);

        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed,
                dyUnconsumed);

//        if (dyConsumed > 0 && !mIsAnimatingOut && child.getVisibility() == View.VISIBLE) {
        if (dyConsumed > 0) {
            ZanL.e("aaaaaaaaaaaa","setVisibility:GONE:");
//            child.setVisibility(View.GONE);
//            child.hide();
            animateOut(child);
            mIsAnimatingOut = true;
//        } else if (dyConsumed <= 0 && child.getVisibility() != View.VISIBLE) {
        } else if (dyConsumed <= 0) {
            ZanL.e("aaaaaaaaaaaa","setVisibility:VISIBLE:");
            mIsAnimatingOut = false;
//            child.setVisibility(View.VISIBLE);

//            child.show();
            animateIn(child);
        }
    }

    // Same animation that FloatingActionButton.Behavior uses to hide the FAB when the AppBarLayout exits
    private void animateOut(final View button) {
        if (Build.VERSION.SDK_INT >= 14) {
            ViewCompat.animate(button).translationY(button.getHeight() + getMarginBottom(button))
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .withLayer()
                    .setListener(new ViewPropertyAnimatorListener() {
                        public void onAnimationStart(View view) {
                            mIsAnimatingOut = true;
                        }

                        public void onAnimationCancel(View view) {
                            mIsAnimatingOut = false;
                        }

                        public void onAnimationEnd(View view) {
                            mIsAnimatingOut = false;
                            view.setVisibility(View.GONE);
                        }
                    }).start();
        }
    }
    // Same animation that FloatingActionButton.Behavior uses to show the FAB when the AppBarLayout enters
    private void animateIn(View button) {
        button.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= 14) {
            ViewCompat.animate(button).translationY(0)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .withLayer()
                    .setListener(null)
                    .start();
        }
    }

    private int getMarginBottom(View v) {
        int marginBottom = 0;
        final ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
        }
        return marginBottom;
    }

}
