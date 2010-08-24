/*
 * File: Log.java
 * 
 * Copyright (C) 2010 The Humanitarian FOSS Project (http://www.hfoss.org)
 * 
 * This file is part of POSIT, Portable Open Search and Identification Tool.
 *
 * POSIT is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) as published 
 * by the Free Software Foundation; either version 3.0 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU LGPL along with this program; 
 * if not visit http://www.gnu.org/licenses/lgpl.html.
 * 
 */

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
