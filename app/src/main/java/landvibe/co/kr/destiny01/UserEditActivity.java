package landvibe.co.kr.destiny01;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import cz.msebera.android.httpclient.Header;
import landvibe.co.kr.destiny01.common.GcmEndMessage;
import landvibe.co.kr.destiny01.util.DeEncrypter;

/**
 * Created by 고승빈 on 2016-01-30.
 */
public class UserEditActivity extends AppCompatActivity implements OnCheckedChangeListener, View.OnClickListener {

    private TextView userEdit_birthday_tx;
    private TextView userEdit_birthtime_tx;

    private RadioGroup userEdit_solarunar_rg;
    private RadioGroup userEdit_gender_rg;

    private RadioButton userEdit_minus_common_rb;
    private RadioButton userEdit_minus_special_rb;
    private RadioButton userEdit_plus_common_rb;
    private RadioButton userEdit_man_rb;
    private RadioButton userEdit_woman_rb;

    private Button userEdit_submit_bt;
    private Button userEdit_cancel_bt;

    private Toolbar toolbar;

    private GcmEndMessage gcmEndMessage;

    private AsyncHttpClient client = new AsyncHttpClient();
    private PersistentCookieStore cookieStore;
    private int user_no;

    private String from;

    private String birthDay;
    private String birthTime;
    private String DateTime;
    private int solarlunar = -1;
    private int gender = -1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit);

        Intent intent = getIntent();
        from = intent.getStringExtra("from");

        ////////////////////////////쿠키 load, 복호화//////////////////////////////////////////////////////////////////////////
        client = new AsyncHttpClient();
        cookieStore = new PersistentCookieStore(this);
        client.addHeader("Cookie", cookieStore.getCookies().get(0).getValue());
        DeEncrypter deEncrypter = new DeEncrypter();

        String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        String[] result = token.split("\\?");
        user_no = Integer.parseInt(result[0]);

        gcmEndMessage = new GcmEndMessage();
        gcmEndMessage.gcmEndMessage(cookieStore.getCookies().get(0).getValue(), getSharedPreferences("Gcm_end_message", MODE_PRIVATE), UserEditActivity.this);

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        toolbar = (Toolbar) findViewById(R.id.toolbar_useredit);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userEdit_birthday_tx = (TextView) findViewById(R.id.userEdit_birthday_tx);
        userEdit_birthtime_tx = (TextView) findViewById(R.id.userEdit_birthtime_tx);

        userEdit_solarunar_rg = (RadioGroup) findViewById(R.id.userEdit_solarunar_rg);
        userEdit_gender_rg = (RadioGroup) findViewById(R.id.userEdit_gender_rg);

        userEdit_solarunar_rg.setOnCheckedChangeListener(this);
        userEdit_gender_rg.setOnCheckedChangeListener(this);

        userEdit_minus_common_rb = (RadioButton) findViewById(R.id.userEdit_minus_common_rb);
        userEdit_minus_special_rb = (RadioButton) findViewById(R.id.userEdit_minus_special_rb);
        userEdit_plus_common_rb = (RadioButton) findViewById(R.id.userEdit_plus_common_rb);
        userEdit_man_rb = (RadioButton) findViewById(R.id.userEdit_man_rb);
        userEdit_woman_rb = (RadioButton) findViewById(R.id.userEdit_woman_rb);

        userEdit_submit_bt = (Button) findViewById(R.id.userEdit_submit_bt);
        userEdit_cancel_bt = (Button) findViewById(R.id.userEdit_cancel_bt);

        userEdit_birthday_tx.setOnClickListener(this);
        userEdit_birthtime_tx.setOnClickListener(this);
        userEdit_submit_bt.setOnClickListener(this);
        userEdit_cancel_bt.setOnClickListener(this);



        if (!from.equals("signup")) {
            RequestParams param = new RequestParams();

            client.post(getString(R.string.URL) + "/user/" + user_no, param, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);

                    try {
                        String [] birthTemp = response.getString("birthday").split("-");
                        String [] birthTimeTemp = response.getString("birthday_detail").split(":");
                        birthDay = response.getString("birthday");
                        birthTime = response.getString("birthday_detail");
                        userEdit_birthday_tx.setText(birthTemp[0]+"년 "+birthTemp[1]+"월 "+birthTemp[2]+"일");
                        userEdit_birthtime_tx.setText(birthTimeTemp[0]+"시 "+birthTimeTemp[1]+"분 ");

                        solarlunar = response.getInt("solarlunar");
                        if(solarlunar == 0)
                            userEdit_minus_common_rb.setChecked(true);
                        else if(solarlunar == 1)
                            userEdit_minus_special_rb.setChecked(true);
                        else if(solarlunar == 2)
                            userEdit_plus_common_rb.setChecked(true);
                        else
                            ;
                        gender = response.getInt("gender");
                        if(gender == 0)
                            userEdit_man_rb.setChecked(true);
                        else if(gender == 1)
                            userEdit_woman_rb.setChecked(true);
                        else
                            ;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.d("error", "fail!!");
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            // DatePicker Dialog
            case R.id.userEdit_birthday_tx :{
                DatePickerDialog.OnDateSetListener callBack = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        birthDay = isExistZero(year) + "-" + isExistZero(1 + monthOfYear) + "-" + isExistZero(dayOfMonth);
                        String birthday = "" + year + "년 " + (1 + monthOfYear) + "월 " + dayOfMonth + "일";
                        userEdit_birthday_tx.setText(birthday);
                    }
                };
                DatePickerDialog signDatePickerDialog = new DatePickerDialog(UserEditActivity.this, callBack, Integer.parseInt(DateTime.substring(0,4)), Integer.parseInt(DateTime.substring(4,6))-1, Integer.parseInt(DateTime.substring(6,8)));
                signDatePickerDialog.show();
                break;
            }
            case R.id.userEdit_birthtime_tx :{
                TimePickerDialog.OnTimeSetListener callBack = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        birthTime = isExistZero(hourOfDay) + ":" + isExistZero(minute) + ":00";
                        String birthtime = "" + hourOfDay + "시 " + minute + "분";
                        userEdit_birthtime_tx.setText(birthtime);
                    }
                };
                TimePickerDialog signTimePickerDialog = new TimePickerDialog(UserEditActivity.this, callBack, Integer.parseInt(DateTime.substring(8,10)), Integer.parseInt(DateTime.substring(10,12)), true);
                signTimePickerDialog.show();
                break;
            }
            case R.id.userEdit_submit_bt : {
                RequestParams param = new RequestParams();

                param.put("user_no",user_no);
                param.put("birthday",birthDay);
                param.put("birthday_detail",birthTime);
                param.put("solarlunar",solarlunar);
                param.put("gender",gender);

                Log.d("useredit","user_no : "+user_no);
                Log.d("useredit","birthday : "+birthDay);
                Log.d("useredit","birthday_detail : "+birthTime);
                Log.d("useredit","solarlunar : "+solarlunar);
                Log.d("useredit","gender : "+gender);

                client.post(getString(R.string.URL) + "/user/update", param, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Toast.makeText(UserEditActivity.this, "정보가 변경되었습니다.", Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(UserEditActivity.this, "오류가 발생하였습니다.\n" +
                                "다시 시도해주세요."+responseBody, Toast.LENGTH_LONG).show();
                    }
                });

                break;
            }
            case R.id.userEdit_cancel_bt : {
                onBackPressed();
                break;
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

    @Override
    protected void onStart() {
        super.onStart();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        DateTime = dateFormat.format(calendar.getTime());
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        gcmEndMessage.gcmEndMessage(cookieStore.getCookies().get(0).getValue(), getSharedPreferences("Gcm_end_message", MODE_PRIVATE), UserEditActivity.this);
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

        gcmEndMessage.isSimpleHistory(cookieStore.getCookies().get(0).getValue(), broadcast_history_no, sender_name, select_history, UserEditActivity.this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group == userEdit_solarunar_rg) {
            if (checkedId == R.id.userEdit_minus_common_rb)
                solarlunar = 0;
            else if (checkedId == R.id.userEdit_minus_special_rb)
                solarlunar = 1;
            else if (checkedId == R.id.userEdit_plus_common_rb)
                solarlunar = 2;
        } else if (group == userEdit_gender_rg) {
            if (checkedId == R.id.userEdit_man_rb)
                gender = 0;
            else if (checkedId == R.id.userEdit_woman_rb)
                gender = 1;
        }
    }

    public String isExistZero(int a){
        if(a/10 == 0){
            return "0"+a;
        }
        return ""+a;
    }
}






