package landvibe.co.kr.destiny01.login;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import landvibe.co.kr.destiny01.R;

public class SignUpClauseOneActivity extends AppCompatActivity  {
    private Button back_btn;
//단순 기본약관을 보여주는 activity

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_clause_one);

        toolbar = (Toolbar) findViewById(R.id.toolbar_signup_clause_one);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        WebView agreement_1_WebView = (WebView) findViewById(R.id.agreement_1_WebView);
        agreement_1_WebView.setWebViewClient(new WebViewClient());

        WebSettings webSettings = agreement_1_WebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        agreement_1_WebView.loadUrl("http://saju.oursoccer.co.kr/webview/agreement");

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
                // NavUtils.navigateUpFromSameTask(this);
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
