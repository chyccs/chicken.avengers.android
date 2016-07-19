package com.chickenlabs.provider;

import android.content.Context;

import com.chickenlabs.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class HttpWorks
{
	public static final String DEBUG_TAG = "HttpWorks";

	private AsyncHttpClient mClient = new AsyncHttpClient();

	public static HttpWorks myInstance;

	public static synchronized HttpWorks getInstance()
	{
		if ( myInstance == null )
		{
			myInstance = new HttpWorks();
		}

		return myInstance;
	}

	private HttpWorks()
	{
		mClient = new AsyncHttpClient();
	}
	//
	public void get( Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler )
	{
		mClient.addHeader( "authstring", "chickenfriedfighting" );
		mClient.get( getAbsoluteUrl( context, url ), params, responseHandler );
	}

	public void post( Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler )
	{
		mClient.addHeader( "authstring", "chickenfriedfighting" );
		mClient.post( getAbsoluteUrl( context, url ), params, responseHandler );
	}

	private static String getAbsoluteUrl( Context context, String relativeUrl )
	{
		return context.getString( R.string.server_uri )  + relativeUrl;
	}
}
