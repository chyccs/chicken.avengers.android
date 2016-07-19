package com.chickenlabs;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;

public class SplashActivity extends Activity
{
    private long enterTime = 0;
    private long exitTime = 0;

    private Handler mHandler;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_splash );

        mHandler = new Handler();
        mHandler.postDelayed( new Runnable()
        {
            @Override
            public void run ()
            {
                initialize();
            }
        }, 100 );
    }

    private void initialize ()
    {
        //        if ( Configuration.SKIP_LOADING )
        //        {
        //            chkPermissions();
        //        }

        enterTime = System.currentTimeMillis();

        mHandler.postDelayed( new Runnable()
        {
            @Override
            public void run()
            {
                goMainActivity();
            }
        }, 10000 );
    }

    private void goMainActivity ()
    {
        exitTime = System.currentTimeMillis();

        long timediff = exitTime - enterTime;

        if(timediff < 1000)
        {
            mHandler.postDelayed( new Runnable()
            {
                @Override
                public void run ()
                {
                    Intent intent = new Intent( SplashActivity.this, MainActivity.class );
                    startActivity( intent );
                }
            }, 1000 - timediff );

        }
        else
        {
            timediff = 0;

            //Intent intent = new Intent( SplashActivity.this, MainActivity.class );
            //startActivity( intent );
            finish();
        }

        // 화면 전환 간 MainActivity 로딩으로 인해 폰 배경이 보이지 않게 하기 위한 트릭
        mHandler.postDelayed( new Runnable()
        {
            @Override
            public void run ()
            {
                finish();
            }
        }, 2000 - timediff );
    }
}
