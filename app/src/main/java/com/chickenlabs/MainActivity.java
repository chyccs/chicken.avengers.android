package com.chickenlabs;

import org.json.JSONArray;
import org.json.JSONException;

import com.chickenlabs.gcm.QuickstartPreferences;
import com.chickenlabs.gcm.RegistrationIntentService;
import com.chickenlabs.provider.CalendarProvider;
import com.chickenlabs.provider.HttpWorks;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity
{
    public static final String TAG = "com.chickenlabs";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private boolean isReceiverRegistered;

    private WebView mWebView;

    private AlphaAnimation mAnim;

    private Handler mHandler;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        setContentView( R.layout.activity_main );

        mHandler = new Handler();

        TextView splashTitle = ( TextView ) findViewById( R.id.splash_title );
        TextView splashMsg = ( TextView ) findViewById( R.id.splash_message );

        splashTitle.setTypeface( Typeface.createFromAsset(getAssets(), "NanumGothicBold.ttf") );
        splashMsg.setTypeface( Typeface.createFromAsset(getAssets(), "NanumGothic.ttf") );

        mWebView = ( WebView ) findViewById( R.id.webView );
        mWebView.setAlpha( 0.0f );

        mAnim = new AlphaAnimation( 0.1f, 1.0f );
        mAnim.setDuration( 1500 );
        mAnim.setAnimationListener( new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart( Animation animation )
            {
                mWebView.setAlpha( 1.0f );
            }

            @Override
            public void onAnimationEnd( Animation animation )
            {
            }

            @Override
            public void onAnimationRepeat( Animation animation )
            {
            }
        } );

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled( true );
        webSettings.setAllowContentAccess( true );
        webSettings.setAllowFileAccess( true );
        webSettings.setAllowFileAccessFromFileURLs( true );
        webSettings.setAllowUniversalAccessFromFileURLs( true );
        webSettings.setAppCacheEnabled( true );
        webSettings.setJavaScriptCanOpenWindowsAutomatically( true );

        mWebView.setWebViewClient( new WebClient() );

        mWebView.loadUrl( getString( R.string.server_uri ) );

        mRegistrationBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive( Context context, Intent intent )
            {

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );

                boolean sentToken = sharedPreferences.getBoolean( QuickstartPreferences.SENT_TOKEN_TO_SERVER, false );
            }
        };

        // Registering BroadcastReceiver
        registerReceiver();

        if ( checkPlayServices() )
        {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent( this, RegistrationIntentService.class );
            startService( intent );
        }
    }

    private void registerReceiver()
    {
        if ( !isReceiverRegistered )
        {
            LocalBroadcastManager.getInstance( this ).registerReceiver( mRegistrationBroadcastReceiver, new IntentFilter( QuickstartPreferences.REGISTRATION_COMPLETE ) );
            isReceiverRegistered = true;
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices()
    {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();

        int resultCode = apiAvailability.isGooglePlayServicesAvailable( this );

        if ( resultCode != ConnectionResult.SUCCESS )
        {
            if ( apiAvailability.isUserResolvableError( resultCode ) )
            {
                apiAvailability.getErrorDialog( this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST ).show();
            }
            else
            {
                Log.i( TAG, "This device is not supported." );
                finish();
            }

            return false;
        }

        return true;
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        LocalBroadcastManager.getInstance( this ).unregisterReceiver( mRegistrationBroadcastReceiver );
        isReceiverRegistered = false;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        return super.onCreateOptionsMenu( menu );
    }

    public class WebClient extends WebViewClient
    {
        public boolean shouldOverrideUrlLoading( WebView view, String url )
        {
            if ( url.startsWith( "https://open.kakao.com/o/" ) )
            {
                Intent i = new Intent( Intent.ACTION_VIEW );
                i.setData( Uri.parse( url ) );
                MainActivity.this.startActivity( i );

                return true;
            }
            else if ( url.startsWith( "skype:" ) )
            {
                try
                {
                    Intent i = new Intent( Intent.ACTION_VIEW );
                    i.setData( Uri.parse( url ) );
                    MainActivity.this.startActivity( i );
                }
                catch(Exception e)
                {
                    Toast.makeText(  MainActivity.this, getString( R.string.msg_not_exist_skype ), Toast.LENGTH_LONG );
                }

                return true;
            }
            else if ( url.endsWith( "&signin=true" ) )
            {
                Uri u = Uri.parse( url );
                String studentId = u.getQueryParameter( "sid" );

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( MainActivity.this );

                sharedPreferences.edit().putString( QuickstartPreferences.STUDENT_ID, studentId ).apply();

                String token = sharedPreferences.getString( QuickstartPreferences.TOKEN, null );

                Log.i( TAG, "sid : " + studentId + ", token : " + token );

                if ( token != null )
                {
                    RegistrationIntentService.sendRegistrationToServer( MainActivity.this, token, studentId );
                }

                view.loadUrl( url );
            }
            else if ( url.startsWith( "app://set_alarm" ) )
            {
                Uri u = Uri.parse( url );

                String studentId = u.getQueryParameter( "studentId" );

                RequestParams params = new RequestParams();
                params.put( "studentId", studentId );

                HttpWorks.getInstance().get( MainActivity.this, "class/remains", params, new JsonHttpResponseHandler()
                {
                    @Override
                    public void onSuccess( JSONArray arg0 )
                    {
                        try
                        {
                            CalendarProvider cp = CalendarProvider.getInstance( MainActivity.this );

                            cp.removeAllSchedule();

                            for ( int i = 0; i < arg0.length(); i++ )
                            {
                                String title = arg0.getJSONObject( i ).getString( "title" );
                                long startTime = Long.parseLong( arg0.getJSONObject( i ).getString( "startTime" ) );
                                int duration = Integer.parseInt( arg0.getJSONObject( i ).getString( "duration" ) );


                                cp.addNewSchedule( title, "", startTime, duration );

                                //Log.i(TAG, "jsondate => " + title + " startTime : " + startTime + " duration : " + duration );
                            }

                            Toast.makeText( MainActivity.this, "수업 스케쥴이 등록 되었습니다.", Toast.LENGTH_LONG ).show();

                        }
                        catch ( JSONException e )
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        super.onSuccess( arg0 );
                    }

                    @Override
                    public void onFailure( Throwable arg0, String arg1 )
                    {
                        super.onFailure( arg0, arg1 );
                        Toast.makeText( MainActivity.this, "수업 스케쥴 등록이 실패하였습니다.", Toast.LENGTH_LONG ).show();
                    }

                    @Override
                    public void onFinish()
                    {
                        super.onFinish();
                    }

                    @Override
                    public void onStart()
                    {
                        super.onStart();
                    }

                } );

                return true;
            }
            else
            {
                view.loadUrl( url );
            }

            return super.shouldOverrideUrlLoading( view, url );
        }

        @Override
        public void onPageStarted( WebView view, String url, Bitmap favicon )
        {
            super.onPageStarted( view, url, favicon );
        }

        @Override
        public void onPageFinished( final WebView view, String url )
        {
            if ( view.getAlpha() != 1.0f )
            {
                mHandler.postDelayed( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        view.startAnimation( mAnim );
                    }
                }, 1000 );
            }

            super.onPageFinished( view, url );
        }
    }
}
