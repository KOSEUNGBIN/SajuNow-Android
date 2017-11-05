package landvibe.co.kr.destiny01.chat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Order;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import landvibe.co.kr.destiny01.R;
import landvibe.co.kr.destiny01.common.CheckRegId;
import landvibe.co.kr.destiny01.common.GcmEndMessage;
import landvibe.co.kr.destiny01.pay.PaySimpleActivity;
import landvibe.co.kr.destiny01.util.DeEncrypter;


public class SimplePrepareActivity extends AppCompatActivity implements View.OnClickListener, Validator.ValidationListener {

    private String DIVISOR = "`c!4~D]s";

    // 쿠키
    private PersistentCookieStore cookieStore;
    private AsyncHttpClient client = new AsyncHttpClient();
    private android.support.v7.app.AlertDialog.Builder alertDialog;
    private View view;

    // 쿠키의 결과를 담을 변수
    private String[] result;
    private int user_no;
    private long company_no;
    private String company_name;
    private String message;
    private int select_history;
    private long unitprice;

    private Toolbar toolbar;

    @Order(1)
    @NotEmpty(sequence = 1,message = "첫 번째 질문을 입력해주세요.")
    private EditText simple_prepare_EditText_1;
    @Order(2)
    @NotEmpty(sequence = 2,message = "두 번째 질문을 입력해주세요.")
    private EditText simple_prepare_EditText_2;
    @Order(3)
    @NotEmpty(sequence = 3,message = "세 번째 질문을 입력해주세요.")
    private EditText simple_prepare_EditText_3;
    private Button simple_prepare_insert_Button;

    private CheckRegId checkRegid;
    private GcmEndMessage gcmEndMessage;

    // Validation 선언
    private Validator validator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_prepare);

        toolbar = (Toolbar) findViewById(R.id.simple_prepare_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        company_no = intent.getLongExtra("company_no", 0);
        company_name = intent.getStringExtra("sender_name");
        select_history = intent.getIntExtra("select_history",-2);
        unitprice = intent.getLongExtra("unitprice",-1);

        ////////////////////////////쿠키 load, 복호화//////////////////////////////////////////////////////////////////////////

        cookieStore = new PersistentCookieStore(this);
        DeEncrypter deEncrypter = new DeEncrypter();
        Log.d("error", "token : " + URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        result = token.split("\\?");
        user_no = Integer.parseInt(result[0]);
        client.addHeader("Cookie", cookieStore.getCookies().get(0).getValue());

        checkRegid = new CheckRegId();
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), SimplePrepareActivity.this);
        gcmEndMessage = new GcmEndMessage();
        gcmEndMessage.gcmEndMessage(cookieStore.getCookies().get(0).getValue(), getSharedPreferences("Gcm_end_message", MODE_PRIVATE), SimplePrepareActivity.this);

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        simple_prepare_EditText_1 = (EditText) findViewById(R.id.simple_prepare_et_1);
        simple_prepare_EditText_2 = (EditText) findViewById(R.id.simple_prepare_et_2);
        simple_prepare_EditText_3 = (EditText) findViewById(R.id.simple_prepare_et_3);
        simple_prepare_insert_Button = (Button) findViewById(R.id.simple_prepare_insert_btn);
        simple_prepare_insert_Button.setOnClickListener(this);

        // validation 사용
        validator = new Validator(this);
        validator.setValidationListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), SimplePrepareActivity.this);
        gcmEndMessage.gcmEndMessage(cookieStore.getCookies().get(0).getValue(), getSharedPreferences("Gcm_end_message", MODE_PRIVATE), SimplePrepareActivity.this);

        super.onRestart();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        super.overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
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


    // 새로운 intent를 푸쉬 받았을 떄
    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        Log.d("error", "SimplePrepareActivity 액티비티 온뉴인텐트  브로드 캐스트에서 받음");

        final int broadcast_history_no = intent.getIntExtra("broadcast_history_no", 0);
        final String sender_name = intent.getStringExtra("sender_name");
        final int select_history = intent.getIntExtra("select_history", -2);

        gcmEndMessage.isSimpleHistory(cookieStore.getCookies().get(0).getValue(), broadcast_history_no, sender_name, select_history, SimplePrepareActivity.this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.simple_prepare_insert_btn: {
                validator.validate(true);
                break;
            }
        }
    }

    @Override
    public void onValidationSucceeded() {
        simple_prepare_insert_Button.setClickable(false);

        message = simple_prepare_EditText_1.getText() + DIVISOR + simple_prepare_EditText_2.getText() + DIVISOR + simple_prepare_EditText_3.getText();

        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String time = sdfNow.format(new Date(System.currentTimeMillis()));

        //채팅창으로 진입
        final RequestParams params = new RequestParams();
        params.put("user_no", user_no);
        params.put("company_no", company_no);
        params.put("start_date", time);
        params.put("select_history", select_history);

        client.post(getString(R.string.URL) + "/history/status/simple", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    simple_prepare_insert_Button.setClickable(true);
                    switch ((int) response.getLong("result")) {
                        case -1: {
                            Toast.makeText(SimplePrepareActivity.this, "역술인이 상담 중입니다.", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case -10: {
                            Toast.makeText(SimplePrepareActivity.this, "역술인이 부재 중입니다.", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case -100: {
                            Toast.makeText(SimplePrepareActivity.this, "계정이 차단되어 로그아웃 됩니다.\n 고객센터에 문의해주세요.", Toast.LENGTH_LONG).show();
                            checkRegid.logout(getApplicationContext());
                            break;
                        }
                        case -1000: {

                            alertDialog = new android.support.v7.app.AlertDialog.Builder(SimplePrepareActivity.this);
                            view = getLayoutInflater().inflate(R.layout.simple_prepare_dialog,null);
                            alertDialog.setView(view);
                            alertDialog.setPositiveButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }

                            });
                            alertDialog.setNegativeButton("결제 진행", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(SimplePrepareActivity.this, PaySimpleActivity.class);
                                    intent.putExtra("user_no", user_no);
                                    intent.putExtra("sender_name", company_name);
                                    intent.putExtra("select_history", select_history);
                                    intent.putExtra("company_no", company_no);
                                    intent.putExtra("message", message);
                                    intent.putExtra("unitprice", unitprice);

                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                                    finish();
                                }

                            });

                            alertDialog.show();



                            break;
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(SimplePrepareActivity.this,
                            "오류가 발생하였습니다.\n" +
                                    "다시 시도해주세요.", Toast.LENGTH_LONG)
                            .show();
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
                    checkRegid.logout(SimplePrepareActivity.this);
                } else {
                    Toast.makeText(getApplicationContext(),  "오류가 발생하였습니다.\n" +
                            "다시 시도해주세요." + responseString, Toast.LENGTH_LONG).show();
                    //  그외의 에러 코드가 들어온다.
                }
                simple_prepare_insert_Button.setClickable(true);
            }
        });
    }

    // Validation Error시 호출되는 함수
    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            // Display error messages ;)
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
