package landvibe.co.kr.destiny01.chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;

import cz.msebera.android.httpclient.Header;
import landvibe.co.kr.destiny01.R;
import landvibe.co.kr.destiny01.ReportActivity;
import landvibe.co.kr.destiny01.common.CheckRegId;
import landvibe.co.kr.destiny01.common.GcmEndMessage;
import landvibe.co.kr.destiny01.util.DeEncrypter;

/**
 * Created by 고승빈 on 2016-06-03.
 */
public class SimpleChatActivity extends AppCompatActivity implements View.OnClickListener {

    // 쿠키
    private PersistentCookieStore cookieStore;
    private AsyncHttpClient client = new AsyncHttpClient();

    // 쿠키의 결과를 담을 변수
    private String[] result;
    private int user_no;
    private int history_no;
    private int is_report_alarmed;
    private boolean end_yn;
    private int select_history;
    private String sender_name;


    private Toolbar toolbar;
    private String company_name;

    private TextView question1_tv;
    private TextView question2_tv;
    private TextView question3_tv;
    private TextView answer1_tv;
    private TextView answer2_tv;
    private TextView answer3_tv;
    private TextView answer1_mark_tv;
    private TextView answer2_mark_tv;
    private TextView answer3_mark_tv;
    private TextView waiting_tv;

    private Button simple_chat_report_btn;

    private CheckRegId checkRegid;
    private GcmEndMessage gcmEndMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_chat);

        Intent intent = getIntent();
        history_no = intent.getIntExtra("history_no", 0);
        sender_name = intent.getStringExtra("sender_name");
        select_history = intent.getIntExtra("select_history", -1);
        end_yn = intent.getBooleanExtra("end_yn", false);

        toolbar = (Toolbar) findViewById(R.id.simple_chat_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ////////////////////////////쿠키 load, 복호화//////////////////////////////////////////////////////////////////////////

        cookieStore = new PersistentCookieStore(this);
        DeEncrypter deEncrypter = new DeEncrypter();
        Log.d("error", "token : " + URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        result = token.split("\\?");
        user_no = Integer.parseInt(result[0]);
        client.addHeader("Cookie", cookieStore.getCookies().get(0).getValue());

        checkRegid = new CheckRegId();
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), SimpleChatActivity.this);
        gcmEndMessage = new GcmEndMessage();
        gcmEndMessage_custum();
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        question1_tv = (TextView) findViewById(R.id.question1_tv);
        question2_tv = (TextView) findViewById(R.id.question2_tv);
        question3_tv = (TextView) findViewById(R.id.question3_tv);
        answer1_tv = (TextView) findViewById(R.id.answer1_tv);
        answer2_tv = (TextView) findViewById(R.id.answer2_tv);
        answer3_tv = (TextView) findViewById(R.id.answer3_tv);
        answer1_mark_tv = (TextView) findViewById(R.id.answer1_mark_tv);
        answer2_mark_tv = (TextView) findViewById(R.id.answer2_mark_tv);
        answer3_mark_tv = (TextView) findViewById(R.id.answer3_mark_tv);
        waiting_tv = (TextView) findViewById(R.id.waiting_tv);
        simple_chat_report_btn = (Button) findViewById(R.id.simple_chat_report_btn);
        simple_chat_report_btn.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), SimpleChatActivity.this);
        gcmEndMessage_custum();
        super.onRestart();
    }

    @Override
    public void onBackPressed() {

        if (is_report_alarmed == 0 && end_yn) {
            gcmEndMessage.EndDialog(cookieStore.getCookies().get(0).getValue(), history_no, sender_name, select_history, SimpleChatActivity.this);
            // 읽었으므로 true
            is_report_alarmed = 1;
        } else {
            super.onBackPressed();
            super.overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
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

    // 새로운 intent를 푸쉬 받았을 떄
    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        final int broadcast_history_no = intent.getIntExtra("broadcast_history_no", 0);
        final String sender_name = intent.getStringExtra("sender_name");
        final int select_history = intent.getIntExtra("select_history", 0);

        if (history_no == broadcast_history_no) {
            simpleChatList();              // 상담내역 호출
            gcmEndMessage.EndDialog(cookieStore.getCookies().get(0).getValue(), history_no, sender_name, select_history, SimpleChatActivity.this);
            simple_chat_report_btn.setText("후기작성");
        } else {
            gcmEndMessage.isSimpleHistory(cookieStore.getCookies().get(0).getValue(), broadcast_history_no, sender_name, select_history, SimpleChatActivity.this);
        }
    }

    private void gcmEndMessage_custum() {
        final SharedPreferences gcmEndMessage_chat = getSharedPreferences("Gcm_end_message", MODE_PRIVATE);
        final int end_message_history_no = gcmEndMessage_chat.getInt("end_message_history_no", 0);

        if (gcmEndMessage_chat.getBoolean("end_message", false)) {                      // 끝
            if (end_message_history_no == history_no) {     // 해당 채팅창에 있을 경우

                SharedPreferences.Editor editor = gcmEndMessage_chat.edit();
                editor.clear();
                editor.commit();        // 쿠키 삭제

                simpleChatList();              // 상담내역 호출
                gcmEndMessage.EndDialog(cookieStore.getCookies().get(0).getValue(), history_no, sender_name, select_history, SimpleChatActivity.this);
                simple_chat_report_btn.setText("후기작성");
            } else {                                           // 다른 채팅방에 있을 경우
                gcmEndMessage.gcmEndMessage(cookieStore.getCookies().get(0).getValue(), getSharedPreferences("Gcm_end_message", MODE_PRIVATE), SimpleChatActivity.this);
            }
        } else {
            simpleChatList();
        }
    }

    private void simpleChatList() {
        RequestParams param = new RequestParams();
        client.post(getString(R.string.URL) + "/history/" + history_no, param, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {

                    is_report_alarmed = (int) response.getJSONObject("result").getLong("is_report_alarmed");
                    JSONArray jsonArray = response.getJSONObject("result").getJSONArray("msgList");
                    String[] simple_chat_question_tx_result = jsonArray.getJSONObject(0).getString("message").split("`c!4~D]s");

               /*     question1_tv.setVisibility(View.VISIBLE);
                    question2_tv.setVisibility(View.VISIBLE);
                    question3_tv.setVisibility(View.VISIBLE);*/

                    try {
                        question1_tv.setText(simple_chat_question_tx_result[0]);
                        question2_tv.setText(simple_chat_question_tx_result[1]);
                        question3_tv.setText(simple_chat_question_tx_result[2]);
                    } catch (Exception e) {
                        Log.d("exception", "" + e);
                    }


                    if (response.getJSONObject("result").getBoolean("end_yn")) {

                        simple_chat_report_btn.setVisibility(is_report_alarmed != 2 ? View.VISIBLE : View.GONE);

                        String[] simple_chat_answer_tx_result = jsonArray.getJSONObject(1).getString("message").split("`c!4~D]s");

                        answer1_tv.setVisibility(View.VISIBLE);
                        answer2_tv.setVisibility(View.VISIBLE);
                        answer3_tv.setVisibility(View.VISIBLE);
                        answer1_mark_tv.setVisibility(View.VISIBLE);
                        answer2_mark_tv.setVisibility(View.VISIBLE);
                        answer3_mark_tv.setVisibility(View.VISIBLE);
                        waiting_tv.setVisibility(View.GONE);
                        answer1_tv.setText(simple_chat_answer_tx_result[0]);
                        answer2_tv.setText(simple_chat_answer_tx_result[1]);
                        answer3_tv.setText(simple_chat_answer_tx_result[2]);

                        // 후시작성 다이얼로그와 update is_report_alarmed 통신
                    } else {
                        simple_chat_report_btn.setVisibility(View.GONE);

                        answer1_tv.setVisibility(View.GONE);
                        answer2_tv.setVisibility(View.GONE);
                        answer3_tv.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (statusCode == 200) {
                    //  200  ==>  서버에 접근은 하였으나, 인증과정에서 Token 값이 바뀌었기 때문에 인증이 되지않는 경우 이다.
                    Toast.makeText(getApplicationContext(), "다른 디바이스에서 해당 계정으로 로그인 되었습니다.\n 본 디바이스는 로그아웃 됩니다.", Toast.LENGTH_LONG).show();
                    checkRegid.logout(SimpleChatActivity.this);
                } else {
                    Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.\n" +
                            "다시 시도해주세요." + responseString, Toast.LENGTH_LONG).show();
                    //  그외의 에러 코드가 들어온다.
                }
            }

        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.simple_chat_report_btn: {
                RequestParams param_history_alarm = new RequestParams();
                param_history_alarm.put("history_no", history_no);
                param_history_alarm.put("is_report_alarmed", 1);
                client.post("http://{Server Domain}/history/update/alarmed", param_history_alarm, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Intent intent = new Intent(SimpleChatActivity.this, ReportActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra("his_no", history_no);
                        intent.putExtra("sender_name", company_name);
                        intent.putExtra("select_history", select_history);

                        startActivity(intent);
                        finish();
//                        Toast.makeText(SimpleChatActivity.this,
//                                "History is_report_alarmed Update Successed ", Toast.LENGTH_LONG)
//                                .show();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                        Toast.makeText(SimpleChatActivity.this,
//                                "History is_report_alarmed Update Fail ", Toast.LENGTH_LONG)
//                                .show();
                    }
                });
                break;
            }
            default: {
                break;
            }
        }
    }
}

