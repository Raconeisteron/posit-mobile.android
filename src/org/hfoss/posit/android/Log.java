package org.hfoss.posit.android;

import org.hfoss.posit.android.utilities.Utils;
/**
 * Special Logging class that catches some of the null pointer exceptions that can potentially
 * @author pgautam
 *
 */
public class Log {
	public static void e(String TAG, String message) {
		try {
			android.util.Log.e(TAG, message);
		} catch (NullPointerException ne) {
			android.util.Log.e(TAG + "", "Got Null pointer exception "
					+ message);
		}
	}

	public static void i(String TAG, String message) {
		try {
			if (Utils.debug) {
				android.util.Log.i(TAG, message);
			}
		} catch (NullPointerException ne) {
			android.util.Log.e(TAG + "", "Got Null pointer exception "
					+ message);
		}
	}

	public static void w(String TAG, String message) {
		try {
			if (Utils.debug) {
				android.util.Log.w(TAG, message);
			}
		} catch (NullPointerException ne) {
			android.util.Log.e(TAG + "", "Got Null pointer exception "
					+ message);
		}
	}

	public static void v(String TAG, String message) {
		try {
			if (Utils.debug) {
				android.util.Log.v(TAG, message);
			}
		} catch (NullPointerException ne) {
			android.util.Log.e(TAG + "", "Got Null pointer exception "
					+ message);
		}
	}
	
	public static void d(String TAG, String message) {
		try {
			if (Utils.debug) {
				android.util.Log.d(TAG, message);
			}
		} catch (NullPointerException ne) {
			android.util.Log.e(TAG + "", "Got Null pointer exception "
					+ message);
		}
	}
}
