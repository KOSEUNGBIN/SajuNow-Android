package landvibe.co.kr.destiny01.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.mobsandgeeks.saripaar.QuickRule;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Checked;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Order;
import com.mobsandgeeks.saripaar.annotation.Password;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;
import landvibe.co.kr.destiny01.R;
import landvibe.co.kr.destiny01.UserEditActivity;

//import com.google.android.gms.appindexing.AppIndex;
//import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by 고승빈 on 2016-01-30.
 */
public class SignUpActivity extends AppCompatActivity implements View.OnClickListener, Validator.ValidationListener {

    @Order(1)
    @NotEmpty(sequence = 1, message = "이메일을 입력해주세요.")
    @Email(sequence = 2, message = "유효하지 않은 이메일 형식입니다.")
    private EditText user_edit_email_ed;
    @Order(2)
    @NotEmpty(sequence = 1, message = "패스워드를 입력해주세요.")
    @Length(sequence = 2, max = 12, message = "유효하지 않은 패스워드입니다.\n 6 ~ 12 이내의 숫자+문자+특수문자로 작성해 주세요.")
    @Password(sequence = 3, min = 6, scheme = Password.Scheme.ALPHA_NUMERIC_SYMBOLS, message = "유효하지 않은 패스워드입니다.\n 6 ~ 12 이내의 숫자+문자+특수문자로 작성해 주세요.")
    private EditText user_edit_password_ed;
    @Order(3)
    @NotEmpty(message = "이름을 입력해주세요.")
    private EditText user_edit_name_ed;
    @Order(4)
    @Checked(message = "이용약관을 선택해 주세요.")
    private CheckBox clause_one_agree_cb;
    @Order(5)
    @Checked(message = "개인정보 취급을 선택해 주세요.")
    private CheckBox clause_two_agree_cb;


    // 가입 - 취소 버튼 변수 정의
    private Button user_edit_cancle_bt;
    private Button user_edit_ok_bt;

    // 이메일 중복 버튼 변수 정의
    private Button signup_email_complex_btn;
    // 가입을 눌러서 서버에 올리기 전에, 앱에서 중복 확인에 필요한 변수
    private boolean isEmailComplex;
    private boolean isProblemEmail;

    // 이용약관을 보기위한 이미지 버튼뷰
    private ImageButton clause_one_imgbtn;
    private ImageButton clause_two_imgbtn;

    // Validation 선언
    private Validator validator;

    private AsyncHttpClient client = new AsyncHttpClient();

    // client.get(getString(R.string.URL)+"/landvibe-api/user/1", param, new JsonHttpResponseHandler() {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        client.addHeader("Cookie", "PASSWORD");

        user_edit_email_ed = (EditText) findViewById(R.id.signup_email_et);
        user_edit_password_ed = (EditText) findViewById(R.id.signup_password_et);
        user_edit_name_ed = (EditText) findViewById(R.id.signup_name_et);


        clause_one_agree_cb = (CheckBox) findViewById(R.id.clause_one_agree_cb);
        clause_two_agree_cb = (CheckBox) findViewById(R.id.clause_two_agree_cb);


        clause_one_agree_cb.setOnClickListener(this);
        clause_two_agree_cb.setOnClickListener(this);

        clause_one_imgbtn = (ImageButton) findViewById(R.id.clause_one_imgbtn);
        clause_two_imgbtn = (ImageButton) findViewById(R.id.clause_two_imgbtn);
        clause_one_imgbtn.setOnClickListener(this);
        clause_two_imgbtn.setOnClickListener(this);

        signup_email_complex_btn = (Button) findViewById(R.id.signup_email_complex_btn);
        signup_email_complex_btn.setOnClickListener(this);

        user_edit_ok_bt = (Button) findViewById(R.id.signup_submit_bt);
        user_edit_cancle_bt = (Button) findViewById(R.id.signup_cancel_bt);
        user_edit_ok_bt.setOnClickListener(this);
        user_edit_cancle_bt.setOnClickListener(this);

        user_edit_email_ed.requestFocus();// 이메일 edittext에 제일 먼저 포커스를 준다

        isEmailComplex = true;

        // validation 사용
        validator = new Validator(this);
        validator.setValidationListener(this);
        validator.put(user_edit_password_ed, new AllowEvenNumbersRule(0));

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            // 가입 버튼
            case R.id.signup_submit_bt: {

                if (isEmailComplex) {
                    Toast.makeText(this, "이메일 중복확인을 해주세요.", Toast.LENGTH_SHORT).show();
                    break;
                }
                validator.validate(true);
                break;
            }
            // 취소 버튼
            case R.id.signup_cancel_bt: {
                finish();
                break;
            }
            // 이용약관 이미지 버튼
            case R.id.clause_one_imgbtn: {
                Intent intent = new Intent(getApplicationContext(), SignUpClauseOneActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
            }
            case R.id.clause_two_imgbtn: {
                Intent intent = new Intent(getApplicationContext(), SignUpClauseTwoActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
            }
            // 이메일 중복
            case R.id.signup_email_complex_btn: {
                isProblemEmail = false;

                validator.validateTill(user_edit_email_ed, false); // false -> 동기화 방식

                if(!isProblemEmail) {
                    String userEditEmail_EditText = user_edit_email_ed.getText().toString().trim();

                    RequestParams params = new RequestParams();
                    params.put("email", userEditEmail_EditText);

                    client.post(getString(R.string.URL) + "/user/complex", params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            super.onSuccess(statusCode, headers, response);
                            try {
                                if (response.getBoolean("result")) {
                                    Log.d("error", "SignUp ID Not Complexed");

                                    signup_email_complex_btn.setText("VALID Email");
                                    signup_email_complex_btn.setBackgroundColor(0xff000000);
                                    isEmailComplex = false;
                                } else {
                                    Log.d("error", "SignUp ID Complexed");

                                    signup_email_complex_btn.setText("INVALID Email");
                                    signup_email_complex_btn.setBackgroundColor(0xffD8D8D8);
                                    isEmailComplex = true;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers,
                                              String responseString, Throwable throwable) {
                            super.onFailure(statusCode, headers,
                                    responseString, throwable);
                        }
                    });
                }
                break;
            }
        }
    }

    // Validation 성공시 호출되는 함수
    @Override
    public void onValidationSucceeded() {
        String userEditEmail_EditText = user_edit_email_ed.getText().toString().trim();

        RequestParams param = new RequestParams();

        String userEditPasswort_EditText = user_edit_password_ed.getText().toString().trim();
        String userEditName_EditText = user_edit_name_ed.getText().toString().trim();

        Log.d("signup", "email " + userEditEmail_EditText);
        Log.d("signup", "password " + userEditPasswort_EditText);
        Log.d("signup", "name " + userEditName_EditText);

        param.put("email", userEditEmail_EditText);
        param.put("password", userEditPasswort_EditText);
        param.put("name", userEditName_EditText);
        param.put("naver_id", "-1");
        param.put("facebook_id", "-1");

        final String email_ = userEditEmail_EditText;
        final String password_ = userEditPasswort_EditText;


        client.post(getString(R.string.URL) + "/user/insert", param, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    if (response.getInt("result") > 0) {
                        Log.d("회원가입 Success", "");

                        // 회원가입이 성공시 SharedPreference에 알림음 설정을 True로 설정
                        SharedPreferences alarmSwitch = getSharedPreferences("alarmSwitch", MODE_PRIVATE);
                        SharedPreferences.Editor editor = alarmSwitch.edit();

                        editor.putBoolean("alarmSwitchCondition", true);
                        editor.commit();

                        duplicateLogin(email_, password_);


                    } else {
                        Log.d("회원가입 Faulure", "");
                        isEmailComplex = true;
                        user_edit_email_ed.requestFocus();
                        signup_email_complex_btn.setBackgroundColor(0xffD8D8D8);
                        signup_email_complex_btn.setText("Invalid Email");
                        Toast.makeText(SignUpActivity.this,
                                "Email이 존재하는 이메일입니다.\nEmail 중복 확인을 다시 확인해 주세요.", Toast.LENGTH_LONG)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("회원가입 중 오류", "");
                Toast.makeText(SignUpActivity.this,
                        "회원가입 중 오류가 발생하였습니다.", Toast.LENGTH_LONG)
                        .show();
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
                if(view.getId() == R.id.signup_email_et)
                {
                    signup_email_complex_btn.setText("INVALID Email");
                    signup_email_complex_btn.setBackgroundColor(0xffD8D8D8);
                    isEmailComplex = true;
                    isProblemEmail = true;
                }
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    // 사용자 지정 Validation 클래스
    public class AllowEvenNumbersRule extends QuickRule<View> {

        private int isCheck;

        // Override this constructor ONLY if you want sequencing.
        public AllowEvenNumbersRule(int sequence) {
            super(sequence);
        }

        @Override
        public boolean isValid(View view) {
            switch (view.getId()) {
                case R.id.signup_password_et: {
                    isCheck = 1;
                    return !user_edit_password_ed.getText().toString().trim().matches(".*\\s.*");
                }
                default:
                    return true;
            }
        }

        @Override
        public String getMessage(Context context) {

            switch (isCheck) {
                case 1:
                    return "패스워드에 빈칸이 존재합니다.";
                default:
                    return "Validation Error";
            }

        }


    }

    public void duplicateLogin(String email, String password) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        final PersistentCookieStore myCookieStore = new PersistentCookieStore(getApplicationContext());
        myCookieStore.clear();
        client.setCookieStore(myCookieStore);
        client.addHeader("Cookie", "PASSWORD");
        params.put("email", email);
        params.put("password", password);

        client.post(getString(R.string.URL) + "/user/change/userkey", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                try {
                    getInstanceIdToken(response.getInt("user_no"));
                    Intent intent = new Intent(SignUpActivity.this, UserEditActivity.class);
                    intent.putExtra("from", "signup");
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                    finish();


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    public void getInstanceIdToken(int user_no) {
        // Start IntentService to register this application with GCM.
        Intent intent = new Intent(this, RegistrationIntentService.class);
        intent.putExtra("user_no", user_no);
        startService(intent);
    }
}






