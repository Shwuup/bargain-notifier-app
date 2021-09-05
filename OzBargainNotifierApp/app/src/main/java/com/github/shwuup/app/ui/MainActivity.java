package com.github.shwuup.app.ui;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.WorkManager;

import com.github.shwuup.BuildConfig;
import com.github.shwuup.R;
import com.github.shwuup.app.AddEvent;
import com.github.shwuup.app.DeleteEvent;
import com.github.shwuup.app.MergedObservable;
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

import io.reactivex.rxjava3.core.Observable;
import kotlin.Unit;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
  private TextInputEditText editText;
  private KeywordFileManager keywordFileManager;
  private CustomAdapter adapter;
  private Context context;
  private KeywordApiManager keywordApiManager;
  private Observable<Event> enterKeyEvents;
  private Observable<Event> deleteEvents;
  private String token;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    }
    context = getApplicationContext();
    keywordFileManager = new KeywordFileManager(context);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    toolbar.getOverflowIcon().setColorFilter(Color.WHITE , PorterDuff.Mode.SRC_ATOP);
    setSupportActionBar(toolbar);
    editText = findViewById(R.id.editText);
    setupRecyclerView();
    createNotificationChannel();
    token = getToken();
    keywordApiManager =
        new KeywordApiManager(
            ServiceGenerator.createService(KeywordService.class), keywordFileManager, token);

    Observable<Unit> deleteAllButtonObservable = RxView.clicks(findViewById(R.id.deleteAllButton));
    deleteAllButtonObservable.subscribe(__ -> showConfirmationForDeleteAll());
    enterKeyEvents = getEnterKeyEvents();
    deleteEvents = adapter.getSubject();

    AddEvent addEvent = new AddEvent(enterKeyEvents, this::doOnAdd);
    DeleteEvent deleteEvent = new DeleteEvent(deleteEvents, this::doOnDelete);

    MergedObservable.updateKeywordsApiRequest(addEvent, deleteEvent, keywordApiManager)
        .subscribe(
            l -> {
              WorkManager.getInstance(context).cancelUniqueWork("keywordRequest");
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

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater=getMenuInflater();
    inflater.inflate(R.menu.main_menu, menu);
    return true;
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

  public Observable<Event> getEnterKeyEvents() {
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
