package com.github.shwuup.app.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.shwuup.BuildConfig;
import com.github.shwuup.R;
import com.github.shwuup.app.KeywordManager;
import com.github.shwuup.app.MyAdapter;
import com.github.shwuup.app.SeenDealsManager;
import com.github.shwuup.app.ServiceGenerator;
import com.github.shwuup.app.token.TokenApiService;
import com.github.shwuup.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;


import timber.log.Timber;

public class ainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.github.shwuup.app.MESSAGE";
    public static final String BASE_URL = "http://api.myservice.com/";
    private static final String TAG = "MainActivity";


    private KeywordManager keywordManager;
    private SeenDealsManager seenDealsManager;
    private NotificationManager notificationManager;
    private RecyclerView recyclerView;
    private MyAdapter mAdapter;
    private LinearLayoutManager layoutManager;
    private EditText editText;
    private Context ctx;

//    private void setListeners() {
//        TextInputLayout textInputLayout = findViewById(R.id.editText);
//        EditText editText = textInputLayout.getEditText();
//
//        editText.setOnEditorActionListener((view, actionId, event) -> {
//            if (actionId == EditorInfo.IME_ACTION_GO) {
//                onAdd();
//                return true;
//            } else {
//                return false;
//            }
//        });
//    }

//    public void showConfirmationForDeleteAll(View view) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Confirmation");
//        builder.setMessage("Are you sure you want to delete all keywords?");
//
//        builder.setPositiveButton("Delete", (dialog, id) -> onDeleteAll());
//        builder.setNegativeButton("Cancel", (dialog, id) -> {
//            dialog.cancel();
//        });
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ctx = getApplicationContext();

        binding.deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Timber.d("Not deleted successfully");
                        } else {
                            Timber.d("Token deleted successfully");
                        }
                    }
                });
            }
        });

        binding.updateTokenButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Timber.d("Not updated successfully");
                        } else {
                            Timber.d("Token updated successfully");
                        }
                    }
                });
            }
        });

        binding.sendTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get token
                // [START log_reg_token]
                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                    return;
                                }
                                // Get new FCM registration token
                                String token = task.getResult();
                                //send that shit
                                TokenApiService tokenApiService = ServiceGenerator.createService(TokenApiService.class);
//                                Call<Token> call = tokenApiService.addToken(new Token(token));
//                                call.enqueue(new Callback<Token>() {
//                                    @Override
//                                    public void onResponse(Call<Token> call, Response<Token> response) {
//                                        Timber.d("body: %s", response.body());
//                                        // The network call was a success and we got a response
//                                    }
//                                    @Override
//                                    public void onFailure(Call<Token> call, Throwable t) {
//                                        // the network call was a failure
//                                        Timber.e(t);
//                                    }
//                                });
                            }
                        });
            }

        });

        binding.logTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get token
                // [START log_reg_token]
                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                    return;
                                }

                                // Get new FCM registration token
                                String token = task.getResult();

                                // Log and toast
                                String msg = getString(R.string.msg_token_fmt, token);
                                Log.d(TAG, msg);
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                // [END log_reg_token]
            }
        });


    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(getString(R.string.channel_id));
            if (notificationChannel != null) {
                notificationManager.deleteNotificationChannel(getString(R.string.channel_id));
            }
            CharSequence name = getString(R.string.channel_name);
            String CHANNEL_ID = getString(R.string.channel_id);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            channel.enableLights(true);
            channel.enableVibration(true);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setSound(Uri.parse("android.resource://"
                    + ctx.getPackageName() + "/"
                    + R.raw.kaching), audioAttributes);

            notificationManager.createNotificationChannel(channel);
        }
    }

//    public void addKeyword() {
//        TextInputLayout textInputLayout = findViewById(R.id.editText);
//        EditText editText = textInputLayout.getEditText();
//
//        String keyword = editText.getText().toString().toLowerCase();
//        if (keyword.equals("")) {
//            textInputLayout.setError("You need to enter a keyword");
//        } else {
//            keywordManager.addKeyword(keyword);
//            this.mAdapter.add(new Keyword(keyword));
//            editText.getText().clear();
//        }
//    }

//    public void onAdd(View view) {
//        addKeyword();
//    }
//
//    public void onAdd() {
//        addKeyword();
//    }
//
//    public void onDelete(View view) {
//        ViewParent parent = view.getParent();
//        ViewGroup parentView = (ViewGroup) parent;
//        TextView text = null;
//        for (int i = 0; i < parentView.getChildCount(); i++) {
//            View child = parentView.getChildAt(i);
//            if (!(child instanceof Button)) {
//                text = (TextView) child;
//            }
//        }
//        String keyword = text.getText().toString();
//        this.keywordManager.deleteKeyword(keyword);
//        this.mAdapter.delete(keyword);
//    }
//
//    public void onDeleteAll() {
//        keywordManager.deleteAll();
//        this.mAdapter.clear();
//    }
}
