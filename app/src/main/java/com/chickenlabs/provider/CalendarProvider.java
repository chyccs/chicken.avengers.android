package com.chickenlabs.provider;

import java.util.TimeZone;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.util.Log;

public class CalendarProvider
{
	public static final String DEBUG_TAG = "CalendarProvider";

	public static final String DEFAULT_EVENT_LOCATION = "ChickenLanguage";
	
	public static CalendarProvider myInstance;

	private Context mContext;

	private SharedPreferences mPref;

	private CalendarProvider( Context context )
	{
		mContext = context;

		mPref = PreferenceManager.getDefaultSharedPreferences( context );
	}

	public static synchronized CalendarProvider getInstance( Context context )
	{
		if ( myInstance == null )
		{
			myInstance = new CalendarProvider( context );
		}

		return myInstance;
	}

	public void removeAllSchedule()
	{
		// Run query
		Cursor cur = null;

		ContentResolver cr = mContext.getContentResolver();

		String selection = "(" + Events.EVENT_LOCATION + " = ?)";

		String[] selectionArgs = new String[] { DEFAULT_EVENT_LOCATION };

		// Submit the query and get a Cursor object back.
		cur = cr.query( Events.CONTENT_URI, new String[] { Events._ID, // 0
				Events.EVENT_LOCATION }, selection, selectionArgs, null );

		while ( cur.moveToNext() )
		{
			long eventId = cur.getLong( 0 );
			removeSchedule( eventId );
		}
	}

	public void removeSchedule( long eventId )
	{
		ContentResolver cr = mContext.getContentResolver();
		Uri deleteUri = ContentUris.withAppendedId( Events.CONTENT_URI, eventId );
		int rows = cr.delete( deleteUri, null, null );
		Log.i( DEBUG_TAG, "Rows deleted: " + rows );
	}

	public boolean addNewSchedule( String title, String description, long dtms, long duration )
	{
		long calID = 1, startMillis = 0, endMillis = 0; 

		startMillis = dtms;
		endMillis = startMillis + duration;

		ContentResolver cr = mContext.getContentResolver();
		
		ContentValues values = new ContentValues();
		values.put(Events.DTSTART, startMillis);
		values.put(Events.DTEND, endMillis);
		values.put(Events.TITLE, title);
		values.put(Events.DESCRIPTION, description);
		values.put(Events.CALENDAR_ID, calID);
		values.put(Events.EVENT_LOCATION, DEFAULT_EVENT_LOCATION);
		values.put(Events.HAS_ALARM, 1);
		values.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

		Uri uri = cr.insert(Events.CONTENT_URI, values);

		// get the event ID that is the last element in the Uri
		long eventID = Long.parseLong(uri.getLastPathSegment());
		
		if ( eventID <= 0 )
		{
			return false;
		}
		
		ContentValues avalues = new ContentValues();
		avalues.put(Reminders.MINUTES, 5);
		avalues.put(Reminders.EVENT_ID, eventID);
		avalues.put(Reminders.METHOD, Reminders.METHOD_ALERT);
		
		Uri auri = cr.insert(Reminders.CONTENT_URI, avalues);

		return true;
	}
}
