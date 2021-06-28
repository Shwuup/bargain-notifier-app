package com.github.shwuup.app;

import androidx.work.BackoffPolicy;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.github.shwuup.R;
import com.github.shwuup.app.token.CreateTokenApiWorker;
import com.github.shwuup.app.token.TokenApiManager;
import com.github.shwuup.app.token.TokenApiService;
import com.github.shwuup.app.token.UpdateTokenApiWorker;
import com.github.shwuup.app.util.ServiceGenerator;
import com.github.shwuup.app.util.SharedPref;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.TimeUnit;

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

        Timber.d(remoteMessage.getData().toString());
        // do notification
    }
    // [START on_new_token]

    /**
     * There are two scenarios when onNewToken is called:
     * 1) When a new token is generated on initial app startup
     * 2) Whenever an existing token is changed
     * Under #2, there are three scenarios when the existing token is changed:
     * A) App is restored to a new device
     * B) User uninstalls/reinstalls the app
     * C) User clears app data
     */
    @Override
    public void onNewToken(String newToken) {
        boolean isOnInitialStartup = SharedPref.readString(this, this.getString(R.string.preference_token_key)).equals("not found");
        TokenApiManager tokenManager = new TokenApiManager(ServiceGenerator.createService(TokenApiService.class));
        if (isOnInitialStartup) {
            Timber.d("Creating token on initial startup...");
            Data tokenData = new Data.Builder().putString("Token", newToken).build();
            tokenManager.addToken(newToken).subscribe(success -> {
                        SharedPref.writeString(this, this.getString(R.string.preference_token_key), newToken);
                        Timber.d("Successfully created token!");
                    },
                    error -> createRecurringRequest(tokenData, CreateTokenApiWorker.class));
        } else {
            Timber.d("Updating token...");
            String oldToken = SharedPref.readString(this, this.getString(R.string.preference_token_key));
            Data tokenData = new Data.Builder().putString("Token", newToken).putString("Old token", oldToken).build();
            tokenManager.updateToken(oldToken, newToken).subscribe(success -> {
                        SharedPref.writeString(this, this.getString(R.string.preference_token_key), newToken);
                        Timber.d("Successfully updated token!");
                    },
                    error -> {
                        createRecurringRequest(tokenData, UpdateTokenApiWorker.class);
                    });
        }
    }

    private void createRecurringRequest(Data tokenData, Class<? extends ListenableWorker> tokenClass) {
        OneTimeWorkRequest myWorkRequest =
                new OneTimeWorkRequest.Builder(tokenClass)
                        .setInputData(tokenData)
                        .setBackoffCriteria(
                                BackoffPolicy.LINEAR,
                                3,
                                TimeUnit.MINUTES)
                        .build();
        WorkManager.getInstance(this).enqueueUniqueWork("syncApi", ExistingWorkPolicy.REPLACE, myWorkRequest);

    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {

    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken();
    }

}
