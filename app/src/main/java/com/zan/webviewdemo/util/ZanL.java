package com.zan.webviewdemo.util;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.zan.webviewdemo.util.ZanL.LogType.d;
import static com.zan.webviewdemo.util.ZanL.LogType.e;
import static com.zan.webviewdemo.util.ZanL.LogType.i;
import static com.zan.webviewdemo.util.ZanL.LogType.v;
import static com.zan.webviewdemo.util.ZanL.LogType.w;
import static com.zan.webviewdemo.util.ZanL.LogType.wtf;


public class ZanL {

	private static boolean sDebug = true;
	private static String sTag = "Zan--ACE";
	private static final int JSON_INDENT = 2;

	enum LogType {
		d, i, e, w, v, wtf
	}

	public static boolean isDebug() {
		return sDebug;
	}

	public static void init(boolean debug, String tag) {
		sDebug = debug;
		sTag = tag;
	}

	public static void normalE(String tag, String msg) {
		if (!sDebug) {
			return;
		}
		/*msg = msg.trim();
		if ((msg.startsWith("{") && msg.endsWith("}")) || (msg.startsWith("[") && msg.endsWith("]"))) {
			msg = getPrettyJson(msg);
		}*/
		Log.e(tag, msg);
	}

	public static void e(String msg, String params) {
		e(null, msg, params);
	}

	public static void e(String tag, String msg, Object... params) {
		doLog(e, tag, msg, params);
	}

	public static void d(String msg, String params) {
		d(null, msg, params);
	}

	public static void d(String tag, String msg, Object... params) {
		doLog(d, tag, msg, params);
	}

	public static void i(String msg, String params) {
		i(null, msg, params);
	}

	public static void i(String tag, String msg, Object... params) {
		doLog(i, tag, msg, params);
	}

	public static void w(String msg, String params) {
		w(null, msg, params);
	}

	public static void w(String tag, String msg, Object... params) {
		doLog(w, tag, msg, params);
	}

	public static void v(String msg, String params) {
		v(null, msg, params);
	}

	public static void v(String tag, String msg, Object... params) {
		doLog(v, tag, msg, params);
	}

	public static void wtf(String msg, String params) {
		wtf(null, msg, params);
	}

	public static void wtf(String tag, String msg, Object... params) {
		doLog(wtf, tag, msg, params);
	}

	private static void doLog(LogType logType, String tag, String msg, Object[] params) {
		if (!sDebug) {
			return;
		}
		if (null != msg && msg.contains("%s")) {
			LogText.doLog(logType, getFinalTag(tag), String.format(msg, params));
		} else {
			StringBuilder sb = new StringBuilder();
			for (Object param : params) {
				sb.append(" --- ").append(param);
			}
			LogText.doLog(logType, getFinalTag(tag), msg + sb.toString());
		}
	}

	public static void jsonE(String json) {
		jsonE(null, json);
	}

	public static void jsonE(String tag, String json) {
		if (!sDebug) {
			return;
		}
		LogText.doLog(e, getFinalTag(tag), getPrettyJson(json));
	}

	public static void jsonE(String tag, String msg, String json) {
		if (!sDebug) {
			return;
		}
		LogText.doLog(e, getFinalTag(tag), String.format("%s\n%s", msg, getPrettyJson(json)));
	}

	public static void jsonW(String tag, String msg, String json) {
		if (!sDebug) {
			return;
		}
		LogText.doLog(w, getFinalTag(tag), String.format("%s\n%s", msg, getPrettyJson(json)));
	}

	private static String getPrettyJson(String jsonStr) {
		try {
			jsonStr = jsonStr.trim();
			if (jsonStr.startsWith("{")) {
				JSONObject jsonObject = new JSONObject(jsonStr);
				return jsonObject.toString(JSON_INDENT);
			}
			if (jsonStr.startsWith("[")) {
				JSONArray jsonArray = new JSONArray(jsonStr);
				return jsonArray.toString(JSON_INDENT);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "Invalid Json, Please Check: " + jsonStr;
	}


	private static String getFinalTag(String tag) {
		if (!TextUtils.isEmpty(tag)) {
			return tag;
		}
		return sTag;
	}

	private static class LogText {
		private static final String START_DIVIDER = "~~~~~~~~~~~~~~~~~~~~~ START ~~~~~~~~~~~~~~~~~~~~~";
		private static final String ENDDD_DIVIDER = "────────────────────── END ──────────────────────";
		private static final String EMPTY_DIVIDER = "                                                 ";
		private static final String END_DIVIDER = String.format("%s\n%s\n%s", ENDDD_DIVIDER, EMPTY_DIVIDER,
		                                                        EMPTY_DIVIDER);
		//		private static final String START_DIVIDER = "───────────────────── START ─────────────────────\n";
		//		private static final String ENDDD_DIVIDER = "══════════════════════ END ══════════════════════\n";

		private String mTag;
		private LogType mLogType;

		LogText(LogType logType, String tag) {
			mLogType = logType;
			mTag = tag;
		}

		static void doLog(LogType logType, String tag, String content) {
			LogText logText = new LogText(logType, tag);
			logText.setup(content);
		}

		void setup(String content) {
			setUpHeader();
			setUpContent(content);
			setUpFooter();
		}

		private void setUpHeader() {
			switch (mLogType) {
				case e:
					Log.e(mTag, START_DIVIDER);
					Log.e(mTag, EMPTY_DIVIDER);
					break;
				case d:
					Log.d(mTag, START_DIVIDER);
					Log.d(mTag, EMPTY_DIVIDER);
					break;
				case w:
					Log.w(mTag, START_DIVIDER);
					Log.w(mTag, EMPTY_DIVIDER);
					break;
				case i:
					Log.i(mTag, START_DIVIDER);
					Log.i(mTag, EMPTY_DIVIDER);
					break;
				case v:
					Log.v(mTag, START_DIVIDER);
					Log.v(mTag, EMPTY_DIVIDER);
					break;
				case wtf:
					Log.wtf(mTag, START_DIVIDER);
					Log.wtf(mTag, EMPTY_DIVIDER);
					break;
				default:
					break;
			}
		}

		private void setUpFooter() {
			switch (mLogType) {
				case e:
					Log.e(mTag, EMPTY_DIVIDER);
					Log.e(mTag, END_DIVIDER);
					break;
				case d:
					Log.d(mTag, EMPTY_DIVIDER);
					Log.d(mTag, END_DIVIDER);
					break;
				case w:
					Log.w(mTag, EMPTY_DIVIDER);
					Log.w(mTag, END_DIVIDER);
					break;
				case i:
					Log.i(mTag, EMPTY_DIVIDER);
					Log.i(mTag, END_DIVIDER);
					break;
				case v:
					Log.v(mTag, EMPTY_DIVIDER);
					Log.v(mTag, END_DIVIDER);
					break;
				case wtf:
					Log.wtf(mTag, EMPTY_DIVIDER);
					Log.wtf(mTag, END_DIVIDER);
					break;
				default:
					break;
			}
		}

		void setUpContent(String content) {
			StackTraceElement targetStackTraceElement = getTargetStackTraceElement();
			String fileName = targetStackTraceElement.getFileName();
			int lineNumber = targetStackTraceElement.getLineNumber();
			String format = String.format("(%s:%s)", fileName, lineNumber);
			switch (mLogType) {
				case e:
					Log.e(mTag, content);
					Log.e(mTag, format);
					break;
				case d:
					Log.d(mTag, content);
					Log.d(mTag, format);
					break;
				case w:
					Log.w(mTag, content);
					Log.w(mTag, format);
					break;
				case i:
					Log.i(mTag, content);
					Log.i(mTag, format);
					break;
				case v:
					Log.v(mTag, content);
					Log.v(mTag, format);
					break;
				case wtf:
					Log.wtf(mTag, content);
					Log.wtf(mTag, format);
					break;
				default:
					break;
			}
			/*Log.e(mTag, content);
//			"(" + fileName + ":" + lineNumber + ")"
			Log.e(mTag, format);*/

		}

		private StackTraceElement getTargetStackTraceElement() {
			// find the target invoked method
			StackTraceElement targetStackTrace = null;
			boolean shouldTrace = false;
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			for (StackTraceElement stackTraceElement : stackTrace) {
				boolean isLogMethod = stackTraceElement.getClassName().equals(ZanL.class.getName());
				if (shouldTrace && !isLogMethod) {
					targetStackTrace = stackTraceElement;
					break;
				}
				shouldTrace = isLogMethod;
			}
			return targetStackTrace;
		}
	}
}
