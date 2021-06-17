package com.github.shwuup.app;

import android.content.Context;

import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.github.shwuup.app.models.Token;
import com.github.shwuup.app.token.TokenApiManager;
import com.github.shwuup.app.token.TokenApiService;
import com.github.shwuup.app.token.TokenApiSyncWorker;

import net.lachlanmckee.timberjunit.TimberTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import okhttp3.ResponseBody;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TokenApiSyncWorkerUnitTest {
    @Rule
    public TimberTestRule logAllAlwaysRule = TimberTestRule.logAllAlways();
    @Mock
    Context context;
    @Mock
    WorkerParameters workerParams;
    @Mock
    TokenApiService mockService;
    TokenApiSyncWorker worker;
    TokenApiManager tokenApiManager;
    Retrofit retrofit;
    MockWebServer server;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        server = new MockWebServer();
        retrofit =
                new Retrofit.Builder()
                        .baseUrl(server.url("").toString())
                        .addConverterFactory(GsonConverterFactory.create())
                        .addCallAdapterFactory(RxJava3CallAdapterFactory.create()).build();
    }

    @Test
    public void testCreateWork_success() throws InterruptedException {
        TestObserver<ListenableWorker.Result> testObserver = new TestObserver<>();
        server.enqueue(new MockResponse().setBody("hey").setResponseCode(200));
        TokenApiService service = retrofit.create(TokenApiService.class);
        Single<Response<ResponseBody>> call = service.addToken(new Token("test_token"));
        when(mockService.addToken(any(Token.class))).thenReturn(call);
        tokenApiManager = new TokenApiManager(mockService);


        Data inputData = new Data.Builder()
                .putString("Token", "test_token")
                .build();

        when(workerParams.getInputData()).thenReturn(inputData);
        worker = new TokenApiSyncWorker(context, workerParams, tokenApiManager);

        worker.createWork().subscribe(testObserver);
        testObserver.await();

        testObserver.assertValue(ListenableWorker.Result.success());
    }
}
