/*
 * File: Communicator.java
 * 
 * Copyright (C) 2009 The Humanitarian FOSS Project (http://www.hfoss.org)
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
package org.hfoss.posit.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.hfoss.posit.Find;
import org.hfoss.posit.R;
import org.hfoss.posit.provider.MyDBHelper;
import org.hfoss.posit.utilities.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * The communication module for POSIT.  Handles most calls to the server to get information regarding
 * projects and finds.
 * 
 * 
 */
public class Communicator {
	private static final String COLUMN_IMEI = "imei";

	/*
	 * You should be careful with putting names for server. DO NOT always trust
	 * DNS.
	 */

	private static String server;
	private static String authKey;
	private static String imei;

	private static int projectId;

	private static String TAG = "Communicator";
	private String responseString;
	private Context mContext;
	private SharedPreferences applicationPreferences;
	private HttpParams mHttpParams;
	private HttpClient mHttpClient;
	private ThreadSafeClientConnManager mConnectionManager;

	public Communicator(Context _context) {
		mContext = _context;

		mHttpParams = new BasicHttpParams();
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", new PlainSocketFactory(), 80));
		mConnectionManager = new ThreadSafeClientConnManager(mHttpParams, registry);
		mHttpClient = new DefaultHttpClient(mConnectionManager, mHttpParams);

		PreferenceManager.setDefaultValues(mContext, R.xml.posit_preferences, false);
		applicationPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		setApplicationAttributes(applicationPreferences.getString("AUTHKEY", ""), 
				applicationPreferences.getString("SERVER_ADDRESS", server), 
				applicationPreferences.getInt("PROJECT_ID", projectId));
		TelephonyManager manager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		imei = manager.getDeviceId();

	}

	private void setApplicationAttributes(String aKey, String serverAddress, int projId){
		authKey = aKey;
		server = serverAddress;
		projectId = projId;
	}

	
	
	/**
	 * NOTE: Calls doHTTPGet
	 * 
	 * Get all open projects from the server.  Eventually, the goal is to be able to get different types
	 * of projects depending on the privileges of the user.
	 * @return a list of all the projects and their information, encoded as maps
	 * @throws JSONException 
	 */
	public ArrayList<HashMap<String,Object>> getProjects(){
		String url = server + "/api/listOpenProjects?authKey=" + authKey;
		ArrayList<HashMap<String, Object>> list;
		responseString = doHTTPGET(url);
		if(Utils.debug)
			Log.i(TAG, responseString);
//		if (Utils.isSuccessfulHttpResultCode(responseString)) {
//			responseString = Utils.stripHttpResultCode(responseString);
			list = new ArrayList<HashMap<String, Object>>();
			try {
				list = (ArrayList<HashMap<String, Object>>) (new ResponseParser(responseString).parse());
			} catch (JSONException e1) {
				Log.i(TAG, "getProjects JSON exception " + e1.getMessage());
				e1.printStackTrace();
				return null;
			}
//		} else {
//			// Should probably throw an exception
//			return null;
//		}
//
		return list;
	}

	/**
	 * Registers the phone being used with the given server address, the authentication key,
	 * and the phone's imei
	 * 
	 * @param server 
	 * @param authKey
	 * @param imei
	 * @return whether the registration was successful
	 */
	public boolean registerDevice(String server, String authKey, String imei){
		  // server = "http://192.168.1.105/posit";
		String url = server + "/api/registerDevice?authKey=" +authKey 
		+ "&imei=" + imei;
		Log.i(TAG, "registerDevice URL=" + url);

		try {
			responseString = doHTTPGET(url);
		} catch (Exception e) {
			Utils.showToast(mContext, e.getMessage());
		}

		if (responseString.equals("false"))
			return false;
		else return true;
	}
	/*
	 * Send one find to the server.
	 * @param find a reference to the Find object
	 * @param action -- either  'create' or 'update'
	 */
	public boolean sendFind (Find find, String action) {
		String url;
		HashMap<String, String> sendMap = find.getContentMapGuid();
		cleanupOnSend(sendMap);
		sendMap.put("imei", imei);

		if (action.equals("create")) {
			url = server +"/api/createFind?authKey="+authKey;
		} else {
			url = server +"/api/updateFind?authKey="+authKey;
		}
		if(Utils.debug) {
			Log.i(TAG,"SendFind=" + sendMap.toString());			
		}
		
		try {
			responseString = doHTTPPost(url, sendMap);
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
			Utils.showToast(mContext, e.getMessage());
		}
		if(Utils.debug)
			Log.i(TAG, "sendFind.ResponseString: " + responseString);
		if (responseString.indexOf("True") != -1) {
			find.setSyncStatus(true);
			return true;
		} else
			return false;

		/**
		try {
			//this is for easiness, it would be more efficient to do in one query

			//find.setServerId(1);
			//find.setRevision(Integer.parseInt(responseMap.get(MyDBHelper.COLUMN_REVISION).toString()));
			//find.setSyncStatus(true);
			ContentValues cv = new ContentValues();
			cv.put("synced", "1");
			cv.put("sid", responseString);
			//cv.put("sid", sendMap.get("identifier"));
			find.updateToDB(cv);
			//find.delete();

		}
		catch (Exception e) {
			Log.e(TAG, "Cannot send the find"+e.getMessage());
		}
		**/
	}

	public void sendMedia(int identifier, long findId, String data, String mimeType) {
		HashMap<String, String> sendMap = new HashMap<String, String>();
		String url = null;

		if (mimeType == "image/jpeg") {
			url = server + "/api/attachPicture?authKey=" +authKey;
			sendMap.put("id", ""+identifier);
			sendMap.put("findId", ""+findId);
			sendMap.put("dataFull", data);
			sendMap.put("mimeType", mimeType);

			responseString = doHTTPPost(url, sendMap);
			if(Utils.debug)
				Log.i(TAG, "sendImage.ResponseString: " + responseString);
		}
	}

	public void updateFind (Find find) {
		HashMap<String, String> sendMap = find.getContentMapSID();
		cleanupOnSend(sendMap);
		String url = server +"/api/updateFind?authKey="+authKey;
		if(Utils.debug) {
			Log.i(TAG,sendMap.toString());
		}
		//SEND_FIND_URL += "&id="+sendMap.get("_id")
		try {
			responseString = doHTTPPost(url, sendMap);	
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
			Utils.showToast(mContext, e.getMessage());
		}
		if(Utils.debug)
			Log.i(TAG, "sendFind.ResponseString: " + responseString);
		try {
			//this is for easiness, it would be more efficient to do in one query

			//find.setServerId(1);
			//find.setRevision(Integer.parseInt(responseMap.get(MyDBHelper.COLUMN_REVISION).toString()));
			//find.setSyncStatus(true);
			ContentValues cv = new ContentValues();
			cv.put("synced", "1");
			cv.put("sid", sendMap.get("identifier"));
			find.updateToDB(cv);
			//find.delete();

		}
		catch (Exception e) {
			Log.e(TAG, e.getStackTrace().toString());
		}
	}

	/**
	 * cleanup the item key,value pairs so that we can send the data.
	 * @param sendMap
	 */
	private void cleanupOnSend(HashMap<String, String> sendMap) {
		sendMap.put("find_time", sendMap.get("time"));
		sendMap.remove("time");
		sendMap.put("projectId", projectId+"");
//	sendMap.put("id",sendMap.get("identifier"));
		sendMap.put("barcode_id",sendMap.get("identifier"));

		addRemoteIdentificationInfo(sendMap);
		latLongHack(sendMap);
	}
	/**
	 * Add the standard values to our request. We might as well use this as initializer for our 
	 * requests.
	 * 
	 * @param sendMap
	 */
	private void addRemoteIdentificationInfo(HashMap<String, String> sendMap) {
		//sendMap.put(COLUMN_APP_KEY, appKey);
		sendMap.put(COLUMN_IMEI, Utils.getIMEI(mContext));
	}

	private void latLongHack(HashMap<String, String> sendMap) {
		Log.i(TAG,"latlongHack " + sendMap.toString());
		if (sendMap.get("latitude").toString().equals("")||
				sendMap.get("latitude").toString().equals(""))
			sendMap.put("latitude","-1");
		if (sendMap.get("longitude").toString().equals("")||
				sendMap.get("longitude").toString().equals(""))
			sendMap.put("longitude","-1");
	}

	/**
	 * cleanup the item key,value pairs so that we can receive and save to the internal database
	 * @param rMap
	 */
	public static void cleanupOnReceive(HashMap<String,Object> rMap){
		rMap.put(MyDBHelper.COLUMN_SYNCED,1);
//		rMap.put("identifier", rMap.get("id"));
		rMap.put("identifier", rMap.get("barcode_id"));
		rMap.remove("barcode_id");

		rMap.put("sid", rMap.get("id")); //set the id from the server as sid
		rMap.remove("id");
		rMap.put("projectId", projectId);
		if (rMap.containsKey("add_time")) {
			rMap.put("time", rMap.get("add_time"));
			rMap.remove("add_time");
		}
		if (rMap.containsKey("images")) {
			if(Utils.debug)
				Log.d(TAG, "contains image key");
			rMap.put(MyDBHelper.COLUMN_IMAGE_URI, rMap.get("images"));
			rMap.remove("images");
		}
	}

	/**
	 * Sends a HttpPost request to the given URL. Any JSON 
	 * @param Uri the URL to send to/receive from
	 * @param sendMap the hashMap of data to send to the server as POST data
	 * @return the response from the URL
	 */
	private String doHTTPPost(String Uri, HashMap<String,String> sendMap) {
		if (Uri==null) throw new NullPointerException("The URL has to be passed");
		String responseString=null;
		HttpPost post = new HttpPost();
		if(Utils.debug)
			Log.i("doHTTPPost()","URI = "+Uri);
		try {
			post.setURI(new URI(Uri));
		} catch (URISyntaxException e) {
			Log.e(TAG, "URISyntaxException " + e.getMessage());
			e.printStackTrace();
			return e.getMessage();
		}
		List<NameValuePair> nvp = PositHttpUtils.getNameValuePairs(sendMap);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		try {
			post.setEntity(new UrlEncodedFormEntity(nvp, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "UnsupportedEncodingException " + e.getMessage());
		}
		try {
			responseString = mHttpClient.execute(post, responseHandler);
		} catch (ClientProtocolException e) {
				Log.e(TAG, "ClientProtocolExcpetion" + e.getMessage());
				e.printStackTrace();
				return e.getMessage();
		} catch (IOException e) {
				Log.e(TAG, "IOException " + e.getMessage());	
				e.printStackTrace();
				return e.getMessage();
		} catch (IllegalStateException e) {
				Log.e(TAG, "IllegalStateException: "+ e.getMessage());
				e.printStackTrace();
				return e.getMessage();
		} catch (Exception e) {
				Log.e(TAG, "Exception on HttpPost " + e.getMessage());
				e.printStackTrace();
				return e.getMessage();
		}

		return responseString;
	}
	/**
	 * A wrapper(does some cleanup too) for sending HTTP GET requests to the URI 
	 * 
	 * @param Uri
	 * @return the request from the remote server
	 */
	public String doHTTPGET(String Uri) {
		if (Uri==null) throw new NullPointerException("The URL has to be passed");
		String responseString = null;
		HttpGet httpGet = new HttpGet();
		try{
			httpGet.setURI(new URI(Uri));
		}catch (URISyntaxException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
			return "[Error]" + e.getMessage();
		}
		if (Utils.debug){
			Log.i(TAG, "doHTTPGet Uri = " + Uri);
		}
		ResponseHandler<String> responseHandler = new BasicResponseHandler();

		try {
			responseString = mHttpClient.execute(httpGet, responseHandler);
		} catch (ClientProtocolException e) {
				Log.e(TAG, "ClientProtocolException" + e.getMessage());
				e.printStackTrace();
				return "[Error]" + e.getMessage();
		} catch (IOException e) {
				Log.e(TAG, e.getMessage());	
				e.printStackTrace();
				return "[Error]" + e.getMessage();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
			return "[Error]" + e.getMessage();
		}
		if(Utils.debug)
			Log.i(TAG, "doHTTPGet Response: "+ responseString);
//		if (!Utils.isSuccessfulHttpResultCode(responseString)) {
//			return Utils.stripHttpResultCode(responseString); // Which will have an error message attached
//		}
		return responseString;
	}
	
	/**
	 * Get all the remote finds 
	 * @return a HashMap of the Id and Revision of all the finds in the server
	 */
	public List<HashMap<String,Object>> getAllRemoteFinds() {

		String findUrl = server +"/api/listFinds?projectId="+projectId+"&authKey="+authKey;

		//this is a List of all of the finds
		//each find is represented by a HashMap
		List<HashMap<String,Object>> findsMap = new ArrayList<HashMap<String,Object>>();
		HashMap<String, String> sendMap = new HashMap<String,String>();
		addRemoteIdentificationInfo(sendMap);

		//responseString is the raw json string returned through php
		String responseString = doHTTPPost(findUrl, sendMap);
		Log.i(TAG,"getAllRemoteFinds() return = " + responseString);  
		
		try {
			findsMap = (ArrayList<HashMap<String, Object>>) (new ResponseParser(responseString).parse());

			Iterator<HashMap<String, Object>> it = findsMap.iterator();
			long totalTime = 0;
			while (it.hasNext()) {
				ArrayList<HashMap<String, Object>> imagesMap = new ArrayList<HashMap<String, Object>>();

				long start = System.currentTimeMillis();
				HashMap<String, Object> map = it.next();
				String findId = (String)map.get("id");
				String imageUrl = server +"/api/getPicturesByFind?findId=" + findId + "&authKey=" +authKey;

				
				String imageResponseString = doHTTPPost(imageUrl, sendMap);
				if(!imageResponseString.equals("false")) {
					JSONArray jsonArr = new JSONArray(imageResponseString);
					for(int i = 0; i < jsonArr.length(); i++) {
						JSONObject jsonObj = jsonArr.getJSONObject(i);
						if(Utils.debug)
							Log.i(TAG, "JSON Image Response String: " + jsonObj.toString());

						HashMap<String,Object> imageMap = new HashMap<String,Object>();
						Iterator<String> iterKeys = jsonObj.keys();
						while(iterKeys.hasNext()) {
							String key = iterKeys.next();
							imageMap.put(key, jsonObj.get(key));
						}
						imagesMap.add(imageMap);
					}
				}
				
				/*
				JSONArray imageIds = (JSONArray) map.get("images");
				if(Utils.debug)
					Log.d(TAG, "image ids jsonarray: " + imageIds.toString());

				for (int i=0; i<imageIds.length(); i++) {
					int imageId = imageIds.getInt(i);
					String imageUrl = server +"/api/getPicture?id=" + imageId + "&authKey=" +authKey;
					String imageResponseString = doHTTPPost(imageUrl, sendMap);
					if(Utils.debug)
						Log.i(TAG, "Image Response String: " + imageResponseString);

					JSONObject json = new JSONObject(imageResponseString);
					if(Utils.debug)
						Log.i(TAG, "JSON Image Response String: " + json.toString());

					HashMap<String,Object> imageMap = new HashMap<String,Object>();
					Iterator<String> iterKeys = json.keys();
					while(iterKeys.hasNext()) {
						String key = iterKeys.next();
						imageMap.put(key, json.get(key));
					}
					imagesMap.add(imageMap);
				}*/
				totalTime+=System.currentTimeMillis()-start;
				Log.i("TIME","time = "+(System.currentTimeMillis()-start));
				Log.i("TIME","Total time = "+totalTime);		
				map.put("images", imagesMap);
			}
		} catch (JSONException e) {
				Log.e(TAG, "JSONException" +  e.getMessage());
				e.printStackTrace();
		} 
		if(Utils.debug)
			Log.i(TAG, "The Finds "+ findsMap.toString());
		return findsMap;
	}

	public void updateFindFromServer(Find find) {
		ContentValues vals = getRemoteFindById(find.getId());
	}
	/**
	 * Pull the remote find from the server using the guid provided.
	 * @param guid, a globally unique identifier
	 * @return an associative list of attribute/value pairs
	 */
	public ContentValues getRemoteFindById(String guid) {
		String url = server +"/api/getFind?guid=" +guid +"&authKey=" +authKey;
		HashMap<String, String> sendMap = new HashMap<String,String>();
		addRemoteIdentificationInfo(sendMap);
		sendMap.put("guid", guid+"");
		String responseString = doHTTPPost(url, sendMap);
		ContentValues cv = new ContentValues();
    
		Log.i(TAG,"getRemoteFindById = " + responseString);
		try {
			JSONObject jobj = new JSONObject(responseString);
			cv.put(MyDBHelper.COLUMN_BARCODE, jobj.getString("barcode_id"));
			cv.put(MyDBHelper.PROJECT_ID, jobj.getInt("project_id"));
			cv.put(MyDBHelper.COLUMN_NAME, jobj.getString("name"));
			cv.put(MyDBHelper.COLUMN_DESCRIPTION, jobj.getString("description"));
			cv.put(MyDBHelper.COLUMN_TIME, jobj.getString("add_time"));
			cv.put(MyDBHelper.MODIFY_TIME, jobj.getString("modify_time"));
			cv.put(MyDBHelper.COLUMN_LATITUDE, jobj.getDouble("latitude"));
			cv.put(MyDBHelper.COLUMN_LONGITUDE, jobj.getDouble("longitude"));
			cv.put(MyDBHelper.COLUMN_REVISION,jobj.getInt("revision"));
			return cv;
		} catch (JSONException e) {
			Log.i(TAG, e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * Deprecated
	 * @param remoteFindId
	 * @return
	 */
	public ContentValues getRemoteFindById(long remoteFindId) {
		String url = server +"/api/getFind?id=" +remoteFindId +"&authKey=" +authKey;
		HashMap<String, String> sendMap = new HashMap<String,String>();
		addRemoteIdentificationInfo(sendMap);
		sendMap.put("id", remoteFindId+"");
		try {
			String responseString = doHTTPPost(url, sendMap);
		} catch (Exception e) {
			Log.i(TAG, "Exception " + e.getMessage());
			e.printStackTrace();
			return null;
		} 
		try {
;			HashMap<String, Object> responseMap = (new ResponseParser(responseString).parse()).get(0);
			//JSONArray finds = new JSONArray(responseMap.get("finds").toString());
			/*
			if (finds.length()>0) {
				HashMap<String,Object> rMap = new ResponseParser(finds.getString(0)).parse().get(0);

				return MyDBHelper.getContentValuesFromMap(rMap);
			}*/
			responseMap.put("_id", remoteFindId+"");
			cleanupOnReceive(responseMap);
			return MyDBHelper.getContentValuesFromMap(responseMap);
		}catch (Exception e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Checks if a given image already exists on the server.  Allows for quicker syncing to the server,
	 * as this allows the application to bypass converting from a bitmap to base64 to send to the server
	 * 
	 * @param imageId the id of the image to query
	 * @return whether the image already exists on the server
	 */
	public boolean imageExistsOnServer(int imageId) {
		HashMap<String, String> sendMap = new HashMap<String,String>();
		addRemoteIdentificationInfo(sendMap);
		String imageUrl = server +"/api/getPicture?id=" + imageId + "&authKey=" +authKey;
		String imageResponseString = doHTTPPost(imageUrl, sendMap);
		if (imageResponseString.equals("false"))
			return false;
		else return true;
	}

	public String registerExpeditionPoint(double lat, double lng, int expedition) {
		String result = doHTTPGET(server+"/api/addExpeditionPoint?authKey="+authKey+"&lat="+lat+"&lng="+lng+"&expedition="+expedition);
		return result;
	}	



	public String registerExpeditionPoint(double lat, double lng,  double alt, int expedition) {
		HashMap<String, String> sendMap  = new HashMap<String,String>();
		addRemoteIdentificationInfo(sendMap);
		String addExpeditionUrl = server+"/api/addExpeditionPoint?authKey="+authKey;
		sendMap.put("lat", ""+lat );
		sendMap.put("lng", lng+"");
		sendMap.put("alt", ""+alt);
		sendMap.put("expeditionId", expedition+"");
		String addExpeditionResponseString = doHTTPPost(addExpeditionUrl, sendMap);
		if (Utils.debug){
			Log.i(TAG, "response: " + addExpeditionResponseString);
		}
		return addExpeditionResponseString;
	}

	public int registerExpeditionId(int projectId){
		HashMap<String, String> sendMap  = new HashMap<String,String>();
		addRemoteIdentificationInfo(sendMap);
		String addExpeditionUrl = server+"/api/addExpedition?authKey="+authKey;
		sendMap.put("projectId", ""+projectId );
		String addExpeditionResponseString = doHTTPPost(addExpeditionUrl, sendMap);
		if (Utils.debug){
			Log.i(TAG, "response: " + addExpeditionResponseString);
		}
		try {
			Integer i = Integer.parseInt(addExpeditionResponseString);
			return i;
		}catch (NumberFormatException e ){
			Log.e(TAG, "Invalid response received");
			return -1;
		}

	}
}