package com.zan.webviewdemo.util;

import android.support.annotation.Keep;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zan.webviewdemo.App;
import com.zan.webviewdemo.R;

import static com.zan.webviewdemo.C.LogTag.TAG_TOAST_UTIL;


/**
 * Created by ZAN on 2017/7/25.
 * ToastUtil.
 */
@Keep
public class ToastUtil {
	private static String oldMsg;
	private static long lastTime = 0;

	private static Toast mToast = null;
	private static final String TAG = TAG_TOAST_UTIL;

	public static void showAboveView(String msg, View view) {
		int top = getViewTopY(view);
		show(msg, Toast.LENGTH_SHORT, true, top);
	}

	public static int getViewTopY(View view) {
		int[] location = new int[2];
		view.getLocationInWindow(location);
		int y = location[1];
		int measuredHeight = view.getMeasuredHeight();
		return y - measuredHeight;
	}

	public static void showAt(String msg, int viewTopY) {
		show(msg, Toast.LENGTH_SHORT, true, viewTopY);
	}

	private static void show(String msg, int show_length, boolean withCheck, int offset) {
		View view = LayoutInflater.from(App.get().getContext()).inflate(R.layout.layout_toast, null);
		TextView title = (TextView) view.findViewById(R.id.tv_toast);
		LinearLayout ll_toast = (LinearLayout) view.findViewById(R.id.ll_toast);
		title.setText(msg);
		int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		ll_toast.measure(w, h);
		offset -= ll_toast.getMeasuredHeight() / 2;
		if (withCheck) {
			doBetterToast(msg, show_length, view, offset);
		} else {
			doNormalToast(show_length, view);
		}
	}

	private static void doNormalToast(int show_length, View view) {
		if (mToast == null) {
			mToast = new Toast(App.get().getContext());
			mToast.setDuration(show_length);
		}
		mToast.setView(view);
		mToast.show();
	}

	private static void doBetterToast(String msg, int show_length, View view, int offset) {
		if (mToast == null) {
			mToast = new Toast(App.get().getContext());
			mToast.setDuration(show_length);
			showToast(msg, view, offset);
		} else {
			if (System.currentTimeMillis() - lastTime < (show_length == Toast.LENGTH_SHORT ? 2000 : 3500)) {
				if (!oldMsg.equals(msg)) {
					showToast(msg, view, offset);
				}
			} else {
				showToast(msg, view, offset);
			}
		}
	}

	private static void showToast(String msg, View view, int offset) {
		mToast.setView(view);

		mToast.setGravity(Gravity.TOP, 0, offset);
		lastTime = System.currentTimeMillis();
		oldMsg = msg;
		mToast.show();
	}

	public static void cancel() {
		if (mToast != null) {
			mToast.cancel();
		}
	}

	public static void showNormal(String msg) {
		Toast.makeText(App.get().getContext(), msg, Toast.LENGTH_SHORT).show();
	}
}
