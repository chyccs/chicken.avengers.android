/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chickenlabs.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.chickenlabs.MainActivity;
import com.chickenlabs.R;
import com.google.android.gms.gcm.GcmListenerService;

import java.util.Random;

public class MyGcmListenerService extends GcmListenerService
{
    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived( String from, Bundle data )
    {
        //int id = data.getInt("id");
        int id = new Random().nextInt();
        String tag = data.getString( "tag" );
        String title = data.getString( "title" );
        String message = data.getString( "message" );
        String action = data.getString( "action" );

        Log.d( TAG, "From : " + from );
        Log.d( TAG, "Tag : " + tag );
        Log.d( TAG, "Title : " + title );
        Log.d( TAG, "Message : " + message );
        Log.d( TAG, "Action : " + action );

        if ( from.startsWith( "/topics/" ) )
        {
            // message received from some topic.
        }
        else
        {
            // normal downstream message.
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification( id, tag, title, message, action );
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification conta ining the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification( int id, String tag, String title, String message, String action )
    {
        Intent intent = new Intent( this, MainActivity.class );
        intent.addFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP );
        intent.putExtra( "uri", action );

        PendingIntent pendingIntent = PendingIntent.getActivity( this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT );

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder( this )
                .setSmallIcon( R.mipmap.ic_noti )
                .setTicker( title )
                .setContentTitle( title )
                .setContentText( message )
                .setAutoCancel( true )
                .setSound( Uri.parse( "android.resource://" + getPackageName() + "/" + R.raw.notisound ) )
                .setVibrate(new long[] {100, 100, 100, 100} )
                .setPriority( NotificationCompat.PRIORITY_HIGH )
                .setVisibility( NotificationCompat.VISIBILITY_PUBLIC )
                .setContentIntent( pendingIntent );

        NotificationManager notificationManager = ( NotificationManager ) getSystemService( Context.NOTIFICATION_SERVICE );

        notificationManager.notify( id, notificationBuilder.build() );
    }
}
