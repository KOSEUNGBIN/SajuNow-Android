/*
서버 연동 예제
AsyncHttp 이용
*/


package landvibe.co.kr.destiny01.pay;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.apache.http.util.EncodingUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;
import landvibe.co.kr.destiny01.R;
import landvibe.co.kr.destiny01.chat.SimpleChatActivity;
import landvibe.co.kr.destiny01.common.CheckRegId;
import landvibe.co.kr.destiny01.util.DeEncrypter;

public class PaySimpleActivity extends AppCompatActivity  {
    private static final String TAG = "InformationActivity";

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private Handler mHandler;
    private boolean mFlag = false;
    private WebView WebView01;
    private AsyncHttpClient client = new AsyncHttpClient();
    private RequestParams param = new RequestParams();
    private PersistentCookieStore cookieStore;
    private String company_name;
    private int user_no;
    private int company_no;
    private int select_history;
    private int unitprice;
    private String[] result;
    private CheckRegId checkRegid;
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        final Context myApp = this;

        Intent intent = getIntent();
        company_name = intent.getStringExtra("sender_name");
        select_history = intent.getIntExtra("select_history",-2);
        user_no = intent.getIntExtra("user_no",-1);
        company_no = (int)intent.getLongExtra("company_no",-1);
        message = intent.getStringExtra("message");
        unitprice = (int)intent.getLongExtra("unitprice",-1);
        ////////////////////////////쿠키 load, 복호화//////////////////////////////////////////////////////////////////////////
        client = new AsyncHttpClient();
        cookieStore = new PersistentCookieStore(this);
        client.addHeader("Cookie", cookieStore.getCookies().get(0).getValue());
        DeEncrypter deEncrypter = new DeEncrypter();

        String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        result = token.split("\\?");

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        WebView01 = (WebView) findViewById(R.id.webView);
        WebView01.setWebViewClient(new MyWebViewClient());
        WebView01.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result)
            {
                new AlertDialog.Builder(myApp)
                        .setTitle("AlertDialog")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new AlertDialog.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        result.confirm();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();

                return true;
            };
        });

        WebSettings webSettings = WebView01.getSettings();
        webSettings.setJavaScriptEnabled(true);

        String postData = "mid=nowlab" + "&goodcurrency=WON "+ "&langcode=KR" + "&charset=UTF-8" + "&goodname="+ company_name  + "&unitprice="+unitprice+ "&receipttoemail="+result[1]+ "&receipttoname=" +result[1] + "&hashresult="+ "&paymethod=card";
        WebView01.postUrl("https://service.paygate.net/openAPI.jsp", EncodingUtils.getBytes(postData, "BASE64"));

        toolbar = (Toolbar) findViewById(R.id.toolbar_information);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 0) {
                    mFlag = false;
                }
            }
        };




    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // 백 키를 터치한 경우
        if(keyCode == KeyEvent.KEYCODE_BACK){

            // 이전 페이지를 볼 수 있다면 이전 페이지를 보여줌
            if(WebView01.canGoBack()){
                WebView01.goBack();
                return false;
            }

            // 이전 페이지를 볼 수 없다면 백키를 한번 더 터치해서 종료
            else {
                if(!mFlag) {
                    Toast.makeText(this, "'뒤로' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
                    mFlag = true;
                    mHandler.sendEmptyMessageDelayed(0, 2000); // 2초 내로 터치시
                    return false;
                } else {
                    finish();
                }
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private class MyWebViewClient extends WebViewClient {

        public void onReceivedError(WebView view, int errorCode, String description, String fallingUrl) {
            view.loadData("<html><body></body></html>", "text/html", "UTF-8");
        }



        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d("pay","onPageStarted : " + url );
            if(url.equals("http://{PAY REDIRECT DOMAIN}/pay/success"))
            {
                Toast.makeText(PaySimpleActivity.this, "결제 완료" , Toast.LENGTH_SHORT).show();
                createSimpleChat();
                finish();
            }
            else if (url.equals("http://{PAY REDIRECT DOMAIN}/pay/fail"))
            {
                Toast.makeText(PaySimpleActivity.this, "결제 실패" , Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d("pay","onPageFinished : " + url );
        }



        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("pay","url : " + url );
            if (url != null && (url.startsWith("intent://") || url.startsWith("intent:"))) {
                try {
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    Intent existPackage = getPackageManager().getLaunchIntentForPackage(intent.getPackage());
                    if (existPackage != null) {
                        startActivity(intent);
//                        Toast.makeText(PaySimpleActivity.this, "intent:// existPackage != null" , Toast.LENGTH_SHORT).show();
                        Log.d("pay","intent:// existPackage != null" );

                    }
                    else {
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                        marketIntent.setData(Uri.parse("market://details?id="+intent.getPackage()));
//                        Toast.makeText(PaySimpleActivity.this, "intent:// else" , Toast.LENGTH_SHORT).show();
                        Log.d("pay","intent:// else" );
                        startActivity(marketIntent);
                    }
                    return true;
                }catch (Exception e) {
                    e.printStackTrace();
                }
                view.loadUrl(url);
                return false;
            }
             else if (url != null && url.startsWith("market://")) {
                try {
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    if (intent != null) {
//                        Toast.makeText(PaySimpleActivity.this, "market" , Toast.LENGTH_SHORT).show();
                        Log.d("pay", "market");
                        startActivity(intent);
                    }
                    return true;
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                view.loadUrl(url);
                return false;
            }
            else
            {
                Log.d("pay", "else");
                return false;

           
            }





        }
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

    public void createSimpleChat() {
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String time = sdfNow.format(new Date(System.currentTimeMillis()));
        final RequestParams params = new RequestParams();
        params.put("user_no", user_no);
        params.put("company_no", company_no);
        params.put("start_date", time);
        params.put("select_history", select_history);
        params.put("message", message);



        client.post(getString(R.string.URL) + "/history/insert/simple", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    // 역술인이 상담 가능하지 않은 상태중에서
                    // Chat_possibillity가 False인 경우,
                    // Chat_switch와 상관없이 상담이 불가이다.
                    // 상담 중으로 토스트를 띄운다.
                    switch ((int) response.getLong("result")) {
                        case -1: {
                            Toast.makeText(PaySimpleActivity.this, "역술인이 상담 중입니다.", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        // 역술인이 상담 가능하지 않은 상태중에서
                        // Chat_possibillity가 True인 경우,
                        // 상담 가능을 제외하고
                        // Chat_switch에 의존되므로
                        // 부재중으로 토스트를 띄운다.
                        case -10: {
                            Toast.makeText(PaySimpleActivity.this, "역술인이 부재 중입니다.", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        // 관리자에 의해, User가 BLOCK 상태인 경우
                        case -100: {
                            Toast.makeText(PaySimpleActivity.this, "계정이 차단되어 로그아웃 됩니다.\n 고객센터에 문의해주세요.", Toast.LENGTH_LONG).show();
                            checkRegid.logout(getApplicationContext());
                            break;
                        }
                        // 해당 히스토리가 존재할 경우  ->  해당 채팅방으로 입장
                        // 해당 히스토리가 존재하지 않을 경우  ->  채팅방 개설
                        default: {
                            Toast.makeText(PaySimpleActivity.this,
                                    "역술인과 간단 사주 상담을 시작합니다.", Toast.LENGTH_LONG)
                                    .show();

                            Log.d("error", "history Insert On success");

                            Intent intent = new Intent(PaySimpleActivity.this, SimpleChatActivity.class);
//                                  intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            intent.putExtra("history_no", (int) response.getLong("result"));
                            intent.putExtra("user_no", user_no);
                            intent.putExtra("sender_name", company_name);
                            intent.putExtra("select_history", select_history);
                            intent.putExtra("end_yn", false);

                            Log.d("error", "response.getLong : " + response.getLong("result"));

                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                            finish();

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
//                    Toast.makeText(PaySimpleActivity.this,
//                            "insert Catched", Toast.LENGTH_LONG)
//                            .show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers,
                        responseString, throwable);

                if (statusCode == 200) {
                    //  200  ==>  서버에 접근은 하였으나, 인증과정에서 Token 값이 바뀌었기 때문에 인증이 되지않는 경우 이다.
                    Toast.makeText(getApplicationContext(), "다른 디바이스에서 해당 계정으로 로그인 되었습니다.\n 본 디바이스는 로그아웃 됩니다.", Toast.LENGTH_LONG).show();
                    checkRegid.logout(PaySimpleActivity.this);
                } else {
                    Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.\n" +
                            "다시 시도해주세요." + responseString, Toast.LENGTH_LONG).show();
                    //  그외의 에러 코드가 들어온다.
                }
            }
        });

    }


}
