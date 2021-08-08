package com.github.shwuup.app;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.work.Data;
import androidx.work.DelegatingWorkerFactory;
import androidx.work.ListenableWorker;
import androidx.work.testing.TestListenableWorkerBuilder;

import com.github.shwuup.R;
import com.github.shwuup.app.models.Token;
import com.github.shwuup.app.token.TokenApiManager;
import com.github.shwuup.app.token.TokenApiService;
import com.github.shwuup.app.token.UpdateTokenApiWorker;
import com.github.shwuup.app.util.SharedPref;

import net.lachlanmckee.timberjunit.TimberTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import okhttp3.ResponseBody;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class UpdateTokenApiWorkerTest {
  @Rule public TimberTestRule logAllAlwaysRule = TimberTestRule.logAllAlways();
  @Rule public MockitoRule rule = MockitoJUnit.rule();
  DelegatingWorkerFactory myWorkerFactory;
  @Mock TokenApiService mockService;
  MockWebServer server;
  TokenApiManager tokenApiManager;
  Retrofit retrofit;
  private Context context;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    server = new MockWebServer();
    retrofit =
        new Retrofit.Builder()
            .baseUrl(server.url("").toString())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build();
  }

  @Test
  public void testCreateTokenApiWorker_success() throws InterruptedException {
    // set up config
    SharedPref.writeString(
        context, context.getString(R.string.preference_token_key), "test_old_token");

    server.enqueue(new MockResponse().setBody("testBody").setResponseCode(200));
    TokenApiService service = retrofit.create(TokenApiService.class);
    Single<Response<ResponseBody>> call =
        service.updateToken(new Token("test_token", "test_old_token"));
    when(mockService.updateToken(any(Token.class))).thenReturn(call);
    myWorkerFactory = MainApplication.addApiSyncFactory(new DelegatingWorkerFactory(), mockService);
    Data inputData =
        new Data.Builder()
            .putString("Token", "test_token")
            .putString("Old token", "test_old_token")
            .build();
    UpdateTokenApiWorker worker =
        TestListenableWorkerBuilder.from(context, UpdateTokenApiWorker.class)
            .setWorkerFactory(myWorkerFactory)
            .setInputData(inputData)
            .build();
    TestObserver<ListenableWorker.Result> testObserver = new TestObserver<>();

    worker.createWork().subscribe(testObserver);
    testObserver.await();
    testObserver.assertValue(ListenableWorker.Result.success());
    String expectedTokenResult = "test_token";
    String actualTokenResult =
        SharedPref.readString(context, context.getString(R.string.preference_token_key));
    assertThat(actualTokenResult, is(expectedTokenResult));
  }

  @Test
  public void testCreateTokenApiWorker_retry() throws InterruptedException {
    TokenApiService service = retrofit.create(TokenApiService.class);
    myWorkerFactory = MainApplication.addApiSyncFactory(new DelegatingWorkerFactory(), service);
    Data inputData =
        new Data.Builder()
            .putString("Token", "test_token")
            .putString("Old token", "test_old_token")
            .build();

    UpdateTokenApiWorker worker =
        TestListenableWorkerBuilder.from(context, UpdateTokenApiWorker.class)
            .setWorkerFactory(myWorkerFactory)
            .setInputData(inputData)
            .build();
    TestObserver<ListenableWorker.Result> testObserver = new TestObserver<>();

    worker.createWork().subscribe(testObserver);
    testObserver.await();
    testObserver.assertValue(ListenableWorker.Result.retry());
  }
}
