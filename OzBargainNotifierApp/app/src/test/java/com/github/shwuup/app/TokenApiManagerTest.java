package com.github.shwuup.app;

import com.github.shwuup.app.models.Token;
import com.github.shwuup.app.token.TokenApiManager;
import com.github.shwuup.app.token.TokenApiService;

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
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.mockito.Mockito.when;

public class TokenApiManagerTest {
    @Rule
    public TimberTestRule logAllAlwaysRule = TimberTestRule.logAllAlways();
    MockWebServer server;
    Retrofit retrofit;
    TokenApiService service;
    @Mock
    TokenApiService mockService;

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
    public void testHandleResponseCodes_throwsErrorWhenCode500() throws InterruptedException {
        server.enqueue(new MockResponse().setBody("hey").setResponseCode(500));
        TokenApiManager tokenManager = new TokenApiManager(mockService);
        service = retrofit.create(TokenApiService.class);
        Single<Response<ResponseBody>> call = service.addToken(new Token("test"));
        when(mockService.addToken(new Token("test"))).thenReturn(call);
        TestObserver<Object> testObserver = new TestObserver<>();

        tokenManager.handleResponseCodes(call).subscribe(testObserver);
        testObserver.await();
        testObserver.assertError(HttpException.class);
    }
}
