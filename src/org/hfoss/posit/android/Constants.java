/*
 * File: Constants.java
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

public class Constants {
	
	public static final int ERR_PASSWORD_MISSING = 0x13;
	public static final int ERR_IMEI_MISSING = 0x14;
	public static final int ERR_EMAIL_INVALID = 0x12;
	public static final int ERR_PASSWORD_UNMATCHED = 0x11;
	public static final int ERR_PASSWORD2_INVALID = 0x10;
	public static final int ERR_PASSWORD1_INVALID = 0x0F;
	public static final int ERR_LASTNAME_MISSING = 0x0E;
	public static final int ERR_FIRSTNAME_MISSING = 0x0D;
	public static final int ERR_EMAIL_MISSING = 0x0C;
	public static final int ERR_AUTHKEY_MISSING = 0x0B;
	public static final int ERR_AUTHKEY_INVALID = 0x0A;
	public static final int REGISTRATION_EMAILEXISTS = 0x09;
	public static final int AUTHN_NOTLOGGEDIN = 0x08;
	public static final int AUTHN_FAILED = 0x07;
	public static final int AUTHN_OK = 0x06;
	public static final int IMAGES_GET_THUMBNAILS = 0x04;
	public static final int IMAGES_GET_FULL = 0x03;
	public static final int PROJECTS_CLOSED = 0x02;
	public static final int PROJECTS_OPEN = 0x01;
	public static final int PROJECTS_ALL = 0x00;
}
