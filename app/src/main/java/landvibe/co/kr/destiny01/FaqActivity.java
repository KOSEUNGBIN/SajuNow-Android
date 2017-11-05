package landvibe.co.kr.destiny01;/*
서버 연동 예제
AsyncHttp 이용
*/


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.loopj.android.http.PersistentCookieStore;

import landvibe.co.kr.destiny01.common.GcmEndMessage;

public class FaqActivity extends AppCompatActivity  {
    private static final String TAG = "FaqActivity";

    private Toolbar toolbar;
    private DrawerLayout drawer;

    private GcmEndMessage gcmEndMessage;
    private PersistentCookieStore cookieStore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);


        cookieStore = new PersistentCookieStore(this);

        WebView WebView01 = (WebView) findViewById(R.id.webView_faq);
        WebView01.setWebViewClient(new WebViewClient());

        WebSettings webSettings = WebView01.getSettings();
        webSettings.setJavaScriptEnabled(true);

        WebView01.loadUrl("http://saju.oursoccer.co.kr/webview/faq/user");

        gcmEndMessage = new GcmEndMessage();
        gcmEndMessage.gcmEndMessage(cookieStore.getCookies().get(0).getValue(),getSharedPreferences("Gcm_end_message", MODE_PRIVATE), FaqActivity.this);

        toolbar = (Toolbar) findViewById(R.id.toolbar_information);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);


        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        gcmEndMessage.gcmEndMessage(cookieStore.getCookies().get(0).getValue(),getSharedPreferences("Gcm_end_message", MODE_PRIVATE), FaqActivity.this);
    }

    // 새로운 intent를 푸쉬 받았을 떄
    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        Log.d("error", "Main 액티비티 온뉴인텐트  브로드 캐스트에서 받음");

        final int broadcast_history_no = intent.getIntExtra("broadcast_history_no", 0);
        final String sender_name = intent.getStringExtra("sender_name");
        final int select_history = intent.getIntExtra("select_history", 0);

        gcmEndMessage.isSimpleHistory(cookieStore.getCookies().get(0).getValue(),broadcast_history_no, sender_name,select_history, FaqActivity.this);
    }


}
