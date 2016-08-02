package com.chickenlabs;

import com.chickenlabs.gcm.QuickstartPreferences;
import com.chickenlabs.gcm.RegistrationIntentService;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.linroid.filtermenu.library.FilterMenu;
import com.linroid.filtermenu.library.FilterMenuLayout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Color;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.FeedbackManager;
import net.hockeyapp.android.LoginManager;
import net.hockeyapp.android.Tracking;
import net.hockeyapp.android.UpdateManager;
import net.hockeyapp.android.metrics.MetricsManager;
import net.hockeyapp.android.metrics.model.Extension;

public class MainActivity extends Activity
{
    public static final String TAG = "com.chickenlabs";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private boolean isReceiverRegistered;

    private WebView mWebView;

    private AlphaAnimation mAnim;

    private Handler mHandler;

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

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

        FrameLayout space = (FrameLayout)findViewById( R.id.webViewFrame );
        space.setPadding( 0, getStatusBarHeight(), 0, 0 );

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
                initFilterMenu();
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

        load(getIntent());

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

        MetricsManager.register(this, getApplication());
        FeedbackManager.register(this);

        LoginManager.register(this, getString(R.string.hockey_app_security), LoginManager.LOGIN_MODE_ANONYMOUS);
        LoginManager.verifyLogin(this, getIntent());

        checkForUpdates();
    }

    private void initFilterMenu()
    {
        FilterMenuLayout layout = (FilterMenuLayout ) findViewById(R.id.filter_menu);
        layout.setVisibility( View.VISIBLE );
        FilterMenu menu = new FilterMenu.Builder(this)
                .addItem( android.R.drawable.ic_menu_info_details )
                .addItem( android.R.drawable.ic_menu_send )
                .addItem( android.R.drawable.ic_menu_report_image )
                .addItem( android.R.drawable.ic_menu_help )
                .attach(layout)
                .withListener( new FilterMenu.OnMenuChangeListener()
                {
                    @Override
                    public void onMenuItemClick( View view, int position )
                    {
                        if ( position == 0 )
                        {
                            try
                            {
                                PackageInfo pInfo = getPackageManager().getPackageInfo( getPackageName(), 0 );
                                String version = pInfo.versionName;

                                mWebView.loadUrl( getString( R.string.server_uri ) + getString( R.string.info_path ) + "?version=" + version );
                            }catch(Exception e)
                            {

                            }
                        }
                        else if ( position == 1 )
                        {
                            FeedbackManager.showFeedbackActivity( MainActivity.this );
                        }
                        else if ( position == 2 )
                        {
                            FeedbackManager.setActivityForScreenshot( MainActivity.this );
                            FeedbackManager.takeScreenshot( MainActivity.this );
                            FeedbackManager.unsetCurrentActivityForScreenshot( MainActivity.this );
                        }
                        else if ( position == 3 )
                        {
                            Intent i = new Intent( Intent.ACTION_VIEW );
                            i.setData( Uri.parse( getString(R.string.chatting_uri) ) );
                            MainActivity.this.startActivity( i );
                        }
                    }

                    @Override
                    public void onMenuCollapse()
                    {
                    }

                    @Override
                    public void onMenuExpand()
                    {
                    }
                }).build();
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
        Tracking.stopUsage(this);
        super.onPause();
        LocalBroadcastManager.getInstance( this ).unregisterReceiver( mRegistrationBroadcastReceiver );
        isReceiverRegistered = false;
        unregisterManagers();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver();
        Tracking.startUsage(this);
        checkForCrashes();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterManagers();
    }


    private void checkForCrashes() {
        CrashManager.register( this );
    }

    private void checkForUpdates() {
        // Remove this for store builds!
        UpdateManager.register( this );
    }

    private void unregisterManagers() {
        UpdateManager.unregister();
    }

    private void load(Intent intent)
    {
        String uri = "";

        if (intent == null || (uri = intent.getStringExtra( "uri" )) == null ) {
            uri = "";
        }

        Log.d(TAG,  getString( R.string.server_uri ) + uri);
        mWebView.loadUrl( getString( R.string.server_uri ) + uri );
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent( intent );
        load(intent);
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
