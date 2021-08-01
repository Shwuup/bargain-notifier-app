package com.github.shwuup.app.ui;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;

import com.github.shwuup.BuildConfig;
import com.github.shwuup.R;
import com.github.shwuup.app.keyword.KeywordApiManager;
import com.github.shwuup.app.keyword.KeywordFileManager;
import com.github.shwuup.app.keyword.KeywordService;
import com.github.shwuup.app.keyword.KeywordWorker;
import com.github.shwuup.app.models.Event;
import com.github.shwuup.app.util.ServiceGenerator;
import com.github.shwuup.app.util.SharedPref;
import com.github.shwuup.app.util.WorkerUtil;
import com.google.android.material.textfield.TextInputEditText;
import com.jakewharton.rxbinding4.view.RxView;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import kotlin.Unit;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
  private TextInputEditText editText;
  private KeywordFileManager keywordFileManager;
  private CustomAdapter adapter;
  private Context context;
  private KeywordApiManager keywordApiManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    }
    context = getApplicationContext();
    keywordFileManager = new KeywordFileManager(context);
    setContentView(R.layout.activity_main);
    editText = findViewById(R.id.editText);
    setupRecyclerView();
    createNotificationChannel();
    keywordApiManager =
        new KeywordApiManager(
            ServiceGenerator.createService(KeywordService.class), keywordFileManager);
    String token = getToken();

    Observable<Unit> deleteAllButtonObservable = RxView.clicks(findViewById(R.id.deleteAllButton));
    deleteAllButtonObservable.subscribe(__ -> showConfirmationForDeleteAll());
    Observable<Event> addButtonObservable = getDoneClicksObservable();
    Observable<Event> deleteButtonObservable = adapter.getSubject();

    Observable.merge(addButtonObservable, deleteButtonObservable)
        .flatMap(
            event -> {
              if (event.name.equals("add")) {
                doOnAdd();
              } else if (event.name.equals("delete")) {
                doOnDelete(event.metadata);
              }
              return Observable.just(event);
            })
        .debounce(10, TimeUnit.SECONDS)
        .flatMap(x -> keywordApiManager.updateKeywords(token).toObservable())
        .subscribe(
            __ -> {
              Timber.d("Successfully made the api call!");
            },
            error -> {
              Timber.e(error);
              Data tokenData = new Data.Builder().putString("Token", token).build();
              WorkerUtil.createRecurringWorkRequest(
                  tokenData,
                  KeywordWorker.class,
                  ExistingWorkPolicy.REPLACE,
                  context,
                  "keywordRequest");
            });
  }

  private void createNotificationChannel() {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = getString(R.string.channel_name);
      int importance = NotificationManager.IMPORTANCE_HIGH;
      NotificationChannel channel =
          new NotificationChannel(getResources().getString(R.string.channel_id), name, importance);
      // Register the channel with the system; you can't change the importance
      // or other notification behaviors after this
      NotificationManager notificationManager = getSystemService(NotificationManager.class);
      notificationManager.createNotificationChannel(channel);
    }
  }

  private String getToken() {
    return SharedPref.readString(context, this.getString(R.string.preference_token_key));
  }

  private void doOnAdd() {
    String newKeyword = editText.getText().toString();
    editText.setText("");
    addKeyword(newKeyword);
  }

  private void doOnDelete(String keyword) {
    keywordFileManager.deleteKeyword(keyword);
    adapter.getKeywords().remove(keyword);
  }

  private void setupRecyclerView() {
    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    LinearLayoutManager layoutManager = new LinearLayoutManager(context);
    recyclerView.setLayoutManager(layoutManager);
    adapter = new CustomAdapter(keywordFileManager.readKeywords());
    recyclerView.setAdapter(adapter);
  }

  private void addKeyword(String keyword) {
    keywordFileManager.addKeyword(keyword);
    this.adapter.add(keyword);
    Timber.d("added keyword");
  }

  public void deleteAllKeywords() {
    keywordFileManager.deleteAll();
    this.adapter.getKeywords().clear();
  }

  public Observable<Event> getDoneClicksObservable() {
    return Observable.create(
        emitter ->
            editText.setOnEditorActionListener(
                (textView, actionId, keyEvent) -> {
                  if (actionId == EditorInfo.IME_ACTION_GO) {
                    emitter.onNext(new Event("add"));
                    return true;
                  }
                  return false;
                }));
  }

  public void showConfirmationForDeleteAll() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Confirmation");
    builder.setMessage("Are you sure you want to delete all keywords?");

    builder.setPositiveButton("Delete", (dialog, id) -> deleteAllKeywords());
    builder.setNegativeButton(
        "Cancel",
        (dialog, id) -> {
          dialog.cancel();
        });

    AlertDialog dialog = builder.create();
    dialog.show();
  }
}
