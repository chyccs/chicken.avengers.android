package com.chickenlabs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.chickenlabs.provider.CalendarProvider;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class AlarmActivity extends Activity
{
	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		Intent intent = getIntent();

		// scheme 확인
		if ( !intent.getData().getScheme().equals( "setAlarms" ) )
		{
			finish();
		}

		// 데이터 받기
		String strDate = intent.getData().getQueryParameter( "dates" );
		String strTime = intent.getData().getQueryParameter( "time" );
		
		String[] dates = strDate.split( "|" );
		
		CalendarProvider cp = CalendarProvider.getInstance( this );
		
		cp.removeAllSchedule();
		
		for ( String date : dates )
		{
			Date dt = stringToDate( date + " " + strTime );
			cp.addNewSchedule( "title", "desc", dt.getTime(), 15 * 60 * 1000 );
		}
		
		finish();
	}
	
	private Date stringToDate( String strDate )
	{
		SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try
		{
			return transFormat.parse(strDate);
		}
		catch ( ParseException e )
		{
			return null;
		}
	}


}
