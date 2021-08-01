package com.github.shwuup.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;

import com.github.shwuup.R;
import com.github.shwuup.app.models.Offer;
import com.github.shwuup.app.token.CreateTokenApiWorker;
import com.github.shwuup.app.token.TokenApiManager;
import com.github.shwuup.app.token.TokenApiService;
import com.github.shwuup.app.token.UpdateTokenApiWorker;
import com.github.shwuup.app.util.OffersConverter;
import com.github.shwuup.app.util.ServiceGenerator;
import com.github.shwuup.app.util.SharedPref;
import com.github.shwuup.app.util.WorkerUtil;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;

import timber.log.Timber;

public class MessagingService extends FirebaseMessagingService {

  private static final String TAG = "MessagingService";

  /**
   * Called when message is received.
   *
   * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
   */
  // [START receive_message]
  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {
    Timber.d(remoteMessage.getData().get("default"));
    List<Offer> offers = OffersConverter.deserialize(remoteMessage.getData().get("default"));
    sendNotification(offers);
  }
  // [START on_new_token]

  /**
   * There are two scenarios when onNewToken is called: 1) When a new token is generated on initial
   * app startup 2) Whenever an existing token is changed Under #2, there are three scenarios when
   * the existing token is changed: A) App is restored to a new device B) User uninstalls/reinstalls
   * the app C) User clears app data
   */
  @Override
  public void onNewToken(String newToken) {
    boolean isOnInitialStartup =
        SharedPref.readString(this, this.getString(R.string.preference_token_key))
            .equals("not found");
    TokenApiManager tokenManager =
        new TokenApiManager(ServiceGenerator.createService(TokenApiService.class));
    if (isOnInitialStartup) {
      Timber.d("Creating token on initial startup...");
      Data tokenData = new Data.Builder().putString("Token", newToken).build();
      tokenManager
          .addToken(newToken)
          .subscribe(
              success -> {
                SharedPref.writeString(
                    this, this.getString(R.string.preference_token_key), newToken);
                Timber.d("Successfully created token!");
              },
              error ->
                  WorkerUtil.createRecurringWorkRequest(
                      tokenData,
                      CreateTokenApiWorker.class,
                      ExistingWorkPolicy.REPLACE,
                      this,
                      "syncApi"));
    } else {
      Timber.d("Updating token...");
      String oldToken = SharedPref.readString(this, this.getString(R.string.preference_token_key));
      Data tokenData =
          new Data.Builder().putString("Token", newToken).putString("Old token", oldToken).build();
      tokenManager
          .updateToken(oldToken, newToken)
          .subscribe(
              success -> {
                SharedPref.writeString(
                    this, this.getString(R.string.preference_token_key), newToken);
                Timber.d("Successfully updated token!");
              },
              error ->
                  WorkerUtil.createRecurringWorkRequest(
                      tokenData,
                      UpdateTokenApiWorker.class,
                      ExistingWorkPolicy.REPLACE,
                      this,
                      "syncApi"));
    }
  }

  /**
   * Create and show offer notification
   *
   * @param offers List of offers
   */
  private void sendNotification(List<Offer> offers) {
    String CHANNEL_ID = getResources().getString(R.string.channel_id);
    String GROUP_ID = getResources().getString(R.string.notification_group_id);
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

    for (Offer offer : offers) {
      int oneTimeID = (int) SystemClock.uptimeMillis();
      String title = offer.title;
      String url = offer.url;
      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setData(Uri.parse(url));
      PendingIntent pendingIntent =
          PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      Notification notification =
          new NotificationCompat.Builder(this, CHANNEL_ID)
              .setSmallIcon(R.drawable.notification_icon)
              .setContentTitle("New Deal")
              .setContentText(title)
              .setStyle(new NotificationCompat.BigTextStyle().bigText(title))
              .setPriority(NotificationCompat.PRIORITY_MAX)
              // Set the intent that will fire when the user taps the notification
              .setContentIntent(pendingIntent)
              .setGroup(GROUP_ID)
              .setAutoCancel(true)
              .build();
      notificationManager.notify(oneTimeID, notification);
    }
    Notification summaryNotification =
        new NotificationCompat.Builder(this, CHANNEL_ID)
            // set content text to support devices running API level < 24
            .setContentText("You have new deals!")
            .setSmallIcon(R.drawable.notification_icon)
            // build summary info into InboxStyle template
            // specify which group this notification belongs to
            .setGroup(getResources().getString(R.string.notification_group_id))
            // set this notification as the summary for the group
            .setGroupSummary(true)
            .build();
    notificationManager.notify(0, summaryNotification);
  }

  private void getToken() {
    FirebaseMessaging.getInstance().getToken();
  }
}
