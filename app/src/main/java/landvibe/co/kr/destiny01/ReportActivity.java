package landvibe.co.kr.destiny01;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import landvibe.co.kr.destiny01.chat.ChatActivity;
import landvibe.co.kr.destiny01.chat.SimpleChatActivity;
import landvibe.co.kr.destiny01.util.DeEncrypter;

/**
 * Created by jik on 2016-03-11.
 * <p>
 * 후기 작성
 */
public class ReportActivity extends AppCompatActivity implements View.OnClickListener {
    private Button report_ok_btn;
    private Button report_cancle_btn;
    private EditText report_comment;
    private AsyncHttpClient client = new AsyncHttpClient();
    private RequestParams param = new RequestParams();
    private ArrayList<String> spinner_item;
    private int historyno;
    private int select_history;
    private boolean end;
    private Toolbar toolbar;
    private String sender_name;
    private RatingBar ratingBar;
    private PersistentCookieStore cookieStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        //this.setFinishOnTouchOutside(false);
        Intent getintent = getIntent();
        historyno = getintent.getExtras().getInt("his_no");
        select_history = getintent.getExtras().getInt("select_history");
        sender_name = getintent.getExtras().getString("sender_name");

        toolbar = (Toolbar) findViewById(R.id.toolbar_report);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Log.d("error", "chatenddialog " + historyno);

        ////////////////////////////쿠키 load, 복호화//////////////////////////////////////////////////////////////////////////
        client = new AsyncHttpClient();
        cookieStore = new PersistentCookieStore(this);
        client.addHeader("Cookie", cookieStore.getCookies().get(0).getValue());
        DeEncrypter deEncrypter = new DeEncrypter();

        String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        String[] result = token.split("\\?");

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        spinner_item = new ArrayList<String>();
        spinner_item.add("0.5");
        spinner_item.add("1");
        spinner_item.add("1.5");
        spinner_item.add("2");
        spinner_item.add("2.5");
        spinner_item.add("3");
        spinner_item.add("3.5");
        spinner_item.add("4");
        spinner_item.add("4.5");
        spinner_item.add("5");
        Log.d("error", "report1111 ");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinner_item);


        report_ok_btn = (Button) findViewById(R.id.report_ok_btn);
        report_ok_btn.setOnClickListener(this);
        report_cancle_btn = (Button) findViewById(R.id.report_cancle_btn);
        report_cancle_btn.setOnClickListener(this);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setRating(3);
        report_comment = (EditText) findViewById(R.id.report_text_et);
        Log.d("error", "report2222 ");

    }

    @Override
    public void onClick(View v) {
        if (v == report_cancle_btn)
            onBackPressed();
        else {

            float score = ratingBar.getRating();
            Log.d("error", "평점 = " + score);
            String comment = report_comment.getText().toString();

            param.put("comment", comment);
            param.put("score", score);
            param.put("company_no", historyno);
            client.post(getString(R.string.URL) + "/company/report/insert", param, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        Toast.makeText(ReportActivity.this, "후기가 작성되었습니다.", Toast.LENGTH_LONG).show();
                        select_history = response.getJSONObject("result").getInt("select_history");
                        onBackPressed();
                    } catch (JSONException e) {
                        Log.i("connect", "exception");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                                      String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    Toast.makeText(ReportActivity.this, "후기작성 중 오류가 발생하였습니다.", Toast.LENGTH_LONG).show();
                    onBackPressed();
                }
            });

        }

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

    @Override
    public void onBackPressed() {

        Log.d("exex", "select_history : " + select_history + " sender_name : " + sender_name);

        Intent intent = new Intent(ReportActivity.this, select_history == 1 ? SimpleChatActivity.class : ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("history_no", historyno);
        intent.putExtra("end_yn", true);
        intent.putExtra("sender_name", sender_name);
        intent.putExtra("select_history", select_history);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
        super.onBackPressed();
    }
}


