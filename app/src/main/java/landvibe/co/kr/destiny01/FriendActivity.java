package landvibe.co.kr.destiny01;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
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
import java.util.Calendar;

import cz.msebera.android.httpclient.Header;
import landvibe.co.kr.destiny01.chat.SimplePrepareActivity;
import landvibe.co.kr.destiny01.common.CheckRegId;
import landvibe.co.kr.destiny01.common.GcmEndMessage;
import landvibe.co.kr.destiny01.util.DeEncrypter;

/**
 * Created by 고승빈 on 2016-01-30.
 */
public class FriendActivity extends AppCompatActivity implements View.OnFocusChangeListener,  RadioGroup.OnCheckedChangeListener {


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private int friend_no;
    private int user_no;
    private String Birthday;
    private String Birthtime;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;

    private int solarlunar;
    private int gender;
    private EditText friendName;
    private TextView friendBirthday;
    private TextView friendBirthtime;
    private TextView profile_user_id_tv;
    private RadioButton friend1_btn;
    private RadioButton friend2_btn;
    private RadioButton friend3_btn;
    private RadioButton friendman_btn;
    private RadioButton friendwoman_btn;
    private EditText friendEmail;
    private RadioGroup friend_btn;
    private RadioGroup friend_gender;
    private Button friendStore;
    private Button friendConfirm;
    private Button friendUpdate;
    private Button friendDelete;
    private Button friendMenu;

    public AsyncHttpClient client;
    public PersistentCookieStore cookieStore;

    private Spinner emailDomainSpinner;
    private int emailDomain;

    private Toolbar toolbar;
    private DrawerLayout drawer;

    private CheckRegId checkRegid;
    private GcmEndMessage gcmEndMessage;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        ////////////////////////////쿠키 load, 복호화//////////////////////////////////////////////////////////////////////////
        client = new AsyncHttpClient();
        cookieStore = new PersistentCookieStore(this);
        client.addHeader("Cookie", cookieStore.getCookies().get(0).getValue());
        DeEncrypter deEncrypter = new DeEncrypter();

        String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        String[] result = token.split("\\?");
        user_no = Integer.parseInt(result[0]);

        checkRegid = new CheckRegId();
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), FriendActivity.this);
        gcmEndMessage = new GcmEndMessage();
        gcmEndMessage.gcmEndMessage(cookieStore.getCookies().get(0).getValue(),getSharedPreferences("Gcm_end_message", MODE_PRIVATE), FriendActivity.this);

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        Log.d("error", "token : " + token);

        listView(); //  DB에서 데이터를 받아와서 리스트 뷰를 실행시키는 함수


        toolbar = (Toolbar) findViewById(R.id.toolbar_friend);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);




//        drawer = (DrawerLayout) findViewById(R.id.friend_drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.setDrawerListener(toggle);
//        toggle.syncState();
//
//        NavigationView navigationView = (NavigationView) findViewById(R.id.friend_nav_view);
//        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            v.setBackgroundResource(R.drawable.lost_fucus_border);
        }
    }


    // ListAdapter 정의 부분
    public class ListAdapter extends BaseAdapter implements View.OnFocusChangeListener{

        JSONArray jsonArray = null;
        JSONObject jsonObject = null;
        Context context = null;
        LayoutInflater inflater = null;

        //  ListAdapter 생성자
        public ListAdapter(Context context, JSONArray jsonArray) {
            this.context = context;
            this.jsonArray = jsonArray;
            this.inflater = LayoutInflater.from(this.context);

        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                v.setBackgroundResource(R.drawable.lost_fucus_border);
            }
        }

        //  ListAdapter 뷰홀더
        class ViewHolder {
            TextView text1;
            TextView text2;
            Button friend_listupdate_btn;
        }


        //  ListAdapter 개수
        @Override
        public int getCount() {
            if (jsonArray.length() != 0)
                return jsonArray.length();
            else
                return 0;

        }

        //  ListAdapter 데이터
        @Override
        public Object getItem(int position) {
            try {
                jsonObject = jsonArray.getJSONObject(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        //  ListAdapter 위치
        @Override
        public long getItemId(int position) {
            return position;
        }

        //  ListAdapter 뷰 --> 여기부터 길다
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View layoutView = convertView;
            ViewHolder viewHolder = null;


            if (layoutView == null) {

                // 뷰,뷰홀더 초기화
                inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                layoutView = inflater.inflate(R.layout.activity_friend_review, null);
                viewHolder = new ViewHolder();
                viewHolder.text1 = (TextView) layoutView.findViewById(R.id.text1);
                viewHolder.text2 = (TextView) layoutView.findViewById(R.id.text2);
                viewHolder.friend_listupdate_btn = (Button) layoutView.findViewById(R.id.friend_list_update_btn);

                // 뷰 저장
                layoutView.setTag(viewHolder);

            } else {
                // 뷰 재사용
                viewHolder = (ViewHolder) layoutView.getTag();
            }


            try {
                // 뷰 안에 view DB에서 받아온 값 할당
                viewHolder.text2.setText(jsonArray.getJSONObject(position).getString("name"));
                viewHolder.text1.setText(jsonArray.getJSONObject(position).getInt("friend_no") + "");


                // "관리" 버튼 이벤트에 대한 내용
                final ViewHolder finalViewHolder = viewHolder;
                viewHolder.friend_listupdate_btn.setOnClickListener(new View.OnClickListener() {    // 버튼 클릭 리스너 정의
                    @Override
                    public void onClick(View v) {

                        // Dialog 생성
                        final Dialog dialog = new Dialog(FriendActivity.this);
                        dialog.setTitle("지인정보 수정");

                        // Dialog에 "activity_friend_add_btn" 레이아웃 추가
                        dialog.setContentView(R.layout.activity_friend_add_btn);

                        // Dialog 크기설정
                        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                        params.width = WindowManager.LayoutParams.MATCH_PARENT;
                        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

                        // Dialog에 param의 정보 추가
                        dialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

                        // dialog 안에 Id 초기화
                        friendConfirm = (Button) dialog.findViewById(R.id.friendConfirm);
                        friendUpdate = (Button) dialog.findViewById(R.id.friendUpdate);
                        friendDelete = (Button) dialog.findViewById(R.id.friendDelete);
                        friendStore = (Button) dialog.findViewById(R.id.friendStore);
                        friendEmail = (EditText) dialog.findViewById(R.id.friendEmail);
                        friendName = (EditText) dialog.findViewById(R.id.friendName);
                        friendBirthday = (TextView) dialog.findViewById(R.id.friendBirthday);
                        friendBirthtime = (TextView) dialog.findViewById(R.id.friendBirthtime);
                        friend1_btn = (RadioButton) dialog.findViewById(R.id.friend_1_btn);
                        friend2_btn = (RadioButton) dialog.findViewById(R.id.friend_2_btn);
                        friend3_btn = (RadioButton) dialog.findViewById(R.id.friend_3_btn);
                        friendman_btn = (RadioButton) dialog.findViewById(R.id.friend_man_btn);
                        friendwoman_btn = (RadioButton) dialog.findViewById(R.id.friend_woman_btn);
                        friend_btn = (RadioGroup) dialog.findViewById(R.id.friend_btn);
                        friend_gender = (RadioGroup) dialog.findViewById(R.id.friend_gender);

                        // RadioGroup의 버튼 리스너 등록
                        friend_btn.setOnClickListener(this);
                        friend_gender.setOnClickListener(this);

                        emailDomainSpinner = (Spinner) dialog.findViewById(R.id.friendEmailDomain);
                        ArrayAdapter<?> emailAdapter = ArrayAdapter.createFromResource(FriendActivity.this,
                                R.array.email_List, R.layout.spinner_item);
                        emailAdapter
                                .setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
                        emailDomainSpinner.setAdapter(emailAdapter);
                        emailDomainSpinner
                                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                                    @Override
                                    public void onItemSelected(AdapterView<?> parent,
                                                               View view, int position, long id) {
                                        // TODO Auto-generated method stub
                                        emailDomain = emailDomainSpinner.getSelectedItemPosition();
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {
                                        // TODO Auto-generated method stub

                                    }

                                });
                        emailDomainSpinner.setOnFocusChangeListener(ListAdapter.this);


                        // Dialog "수정" 버튼에서 DB에서 불러온 데이터 삽입 -> 수정 전의 데이터를 DB에서 불러온다.

                        final RequestParams param = new RequestParams();
                        //param.add("user_no", ""+10);

                        // DB - getByNo 에 접근

                        client.post(getString(R.string.URL) + "/friend/one/" + finalViewHolder.text1.getText().toString(), param, new JsonHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                super.onSuccess(statusCode, headers, response);

                                try {
                                    //  friend_no을 초기화하여 -> Update시 URL에 덧붙일 용도
                                    friend_no = response.getInt("friend_no");

                                    // DB에서 받아온 값으로 Dialog내의 View들에 값 할당
                                    friendName.setText(response.getString("name"));


                                    String isChekedComplexedDomain[] = response.getString("email").toString().split("@");
                                    String[] emailDomain = getResources().getStringArray(R.array.email_List);
                                    if (isChekedComplexedDomain.length > 2) {
                                        String isChekedComplexedDomainTemp = "";
                                        for (int i = 0; i < isChekedComplexedDomain.length-1; i++)
                                            isChekedComplexedDomainTemp += isChekedComplexedDomain[i];

                                        friendEmail.setText(isChekedComplexedDomainTemp);

                                        for (int i = 0; i < emailDomain.length; i++) {
                                            if (emailDomain[i].equals(isChekedComplexedDomain[isChekedComplexedDomain.length-1])) {
                                                emailDomainSpinner.setSelection(i);
                                                break;
                                            }
                                        }
                                    }
                                    else{
                                        friendEmail.setText(isChekedComplexedDomain[0]);
                                        for (int i = 0; i < emailDomain.length; i++) {
                                            if (emailDomain[i].equals(isChekedComplexedDomain[1])) {
                                                emailDomainSpinner.setSelection(i);
                                                break;
                                            }
                                        }

                                    }

                                    String friend_birthDay[] = response.getString("birthday").split("-");
                                    String friend_birthTime[] = response.getString("birthday_detail").split(":");
                                    friendBirthday.setText(friend_birthDay[0] + "년 " + friend_birthDay[1] + "월 " + friend_birthDay[2] + "일");
                                    friendBirthtime.setText(friend_birthTime[0] + "시 " + friend_birthTime[1] + "분 " + friend_birthTime[2] + "초");

                                    solarlunar = response.getInt("solarlunar");
                                    gender = response.getInt("gender");
                                    // RadioGroup에 체크할 RadioButton을 정한다.
                                    switch (solarlunar) {
                                        case 0:
                                            friend_btn.check(R.id.friend_1_btn);
                                            break;
                                        case 1:
                                            friend_btn.check(R.id.friend_2_btn);
                                            break;
                                        case 2:
                                            friend_btn.check(R.id.friend_3_btn);
                                            break;
                                        default:
                                            break;

                                    }

                                    // RadioGroup에 체크할 RadioButton을 정한다.
                                    switch (gender) {
                                        case 0:
                                            friend_gender.check(R.id.friend_man_btn);
                                            break;
                                        case 1:
                                            friend_gender.check(R.id.friend_woman_btn);
                                            break;

                                        default:
                                            break;

                                    }

                                    friendName.requestFocus();

                                    friend_gender.setClickable(false);
                                    friend_btn.setClickable(false);

                                    Log.d("error", "Friend_Update_Information_DB_Success");

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.d("error", "Friend_Update_Information_DB_Fail_SettingError");

                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                Log.d("error", "Friend_Update_Information_DB_Fail");

                                if (statusCode == 200) {
                                    //  200  ==>  서버에 접근은 하였으나, 인증과정에서 Token 값이 바뀌었기 때문에 인증이 되지않는 경우 이다.
                                    Toast.makeText(getApplicationContext(), "다른 디바이스에서 해당 계정으로 로그인 되었습니다.\n 본 디바이스는 로그아웃 됩니다.", Toast.LENGTH_LONG).show();
                                    checkRegid.logout( FriendActivity.this);
                                } else {
                                    Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.\n" +
                                            "다시 시도해주세요." + responseString, Toast.LENGTH_LONG).show();
                                    //  그외의 에러 코드가 들어온다.
                                }

                            }
                        });

                        // Dialog의 "저장하기" 버튼을 없애고 "삭제","수정","확인" 버튼을 보이게 한다.
                        friendStore.setVisibility(View.GONE);
                        friendConfirm.setVisibility(View.VISIBLE);
                        friendUpdate.setVisibility(View.VISIBLE);
                        friendDelete.setVisibility(View.VISIBLE);

                        //  "확인" 버튼에 대한 이벤트 처리
                        friendConfirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();   // Dialog를 닫는다.
                            }
                        });

                        // "수정" 버튼에 대한 이벤트 처리
                        friendUpdate.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {

                                boolean insertFriendStore = true;

                                final String friendname = friendName.getText().toString().trim();
                                final String friendemail = friendEmail.getText().toString().trim();


                                if (friendname.isEmpty()) {
                                    Toast.makeText(FriendActivity.this, "이름을 입력해주세요.", Toast.LENGTH_LONG).show();
                                    friendName.requestFocus();
                                    insertFriendStore = false;

                                } else {
                                    for (int i = 0; i < friendname.length(); i++) {
                                        if (friendname.charAt(i) == ' ') {
                                            Toast.makeText(FriendActivity.this, "이름 입력란에 공백이 있습니다.\n다시 입력해주세요.", Toast.LENGTH_LONG).show();
                                            friendName.requestFocus();
                                            insertFriendStore = false;
                                            break;
                                        }
                                    }
                                }
                                if (friendemail.isEmpty()) {
                                    if (insertFriendStore) {
                                        Toast.makeText(FriendActivity.this, "이메일을 입력해주세요.", Toast.LENGTH_LONG).show();
                                        friendEmail.requestFocus();
                                    }
                                    insertFriendStore = false;
                                }else if (emailDomain == 0) {
                                    if (insertFriendStore)
                                        Toast.makeText(FriendActivity.this, "도메인을 선택하여 주세요.", Toast.LENGTH_SHORT).show();
                                    insertFriendStore = false;
                                }
                                else {
                                    for (int i = 0; i < friendemail.length(); i++) {
                                        if (friendemail.charAt(i) == ' ') {
                                            if (insertFriendStore) {
                                                Toast.makeText(FriendActivity.this, "이메일 입력란에 공백이 있습니다.\n다시 입력해주세요.", Toast.LENGTH_LONG).show();
                                                friendEmail.requestFocus();
                                            }
                                            insertFriendStore = false;
                                            break;
                                        }
                                    }
                                }

                                String isChekedComplexedDomain[] = friendemail.split("@");
                                if (isChekedComplexedDomain.length > 1 && insertFriendStore) {
                                    String[] emailDomain = getResources().getStringArray(R.array.email_List);
                                    for (int i = 0; i < emailDomain.length; i++) {
                                        if (isChekedComplexedDomain[isChekedComplexedDomain.length - 1].equals(emailDomain[i])) {
                                            insertFriendStore = false;
                                            Toast.makeText(FriendActivity.this,
                                                    "이메일에 도메인 주소를 사용할 수 없습니다. \n 다시 기입해 주세요.", Toast.LENGTH_LONG)
                                                    .show();
                                            friendEmail.requestFocus();
                                            break;
                                        }
                                    }
                                }

                                if (insertFriendStore) {

                                    // 추가등록 다이얼로그에서 입력된 생년월일을 불러와 년,월,일로 잘라 DB에 맞는 날짜포맷으로 바꾼다.
                                    final String birdayYear[] = friendBirthday.getText().toString().split("년");
                                    final String birdayMonth[] = birdayYear[1].split("월");
                                    final String birdayDay[] = birdayMonth[1].split("일");

                                    // 추가등록 다이얼로그에서 입력된 탄생시간을 불러와 시,분으로 잘라 DB에 맞는 날짜포맷으로 바꾼다.
                                    final String friendbirthHour[] = friendBirthtime.getText().toString().split("시");
                                    final String friendbirthMinute[] = friendbirthHour[1].split("분");

                                    // DB에서 받아온 정보로 RadionButton 체크
                                    onCheckedChanged(friend_btn, friend_btn.getCheckedRadioButtonId());
                                    onCheckedChanged(friend_gender, friend_gender.getCheckedRadioButtonId());

                                    String friendEditEmailDomain_Spinner = emailDomainSpinner.getSelectedItem().toString().trim();

                                    Log.d("error", birdayYear[0] + "-" + birdayMonth[0].trim() + "-" + birdayDay[0].trim());
                                    Log.d("error", friendbirthHour[0] + ":" + friendbirthMinute[0].trim() + ":00");

                                    // Dialog "추가하기" 버튼에서 DB에서 불러온 데이터 삽입
                                    final RequestParams param = new RequestParams();
                                    param.add("friend_no", "" + friend_no);
                                    param.add("user_no", "" + user_no);
                                    param.add("email", friendemail+"@"+friendEditEmailDomain_Spinner);
                                    param.add("name", friendname);
                                    param.add("birthday", birdayYear[0] + "-" + birdayMonth[0].trim() + "-" + birdayDay[0].trim());
                                    param.add("birthday_detail", friendbirthHour[0] + ":" + friendbirthMinute[0].trim() + ":00");
                                    param.add("solarlunar", solarlunar + "");
                                    param.add("gender", gender + "");
                                    param.add("modify_date", Calendar.getInstance().getTime().toString());
                                    // DB - update 에 접근
                                    client.post(getString(R.string.URL) + "/friend/update", param, new AsyncHttpResponseHandler() {
                                        @Override
                                        public void onSuccess(int i, Header[] headers, byte[] bytes) {
                                            listView(); // listview 초기화
                                            Log.d("error", "Friend_Update_DB_Success");
                                            dialog.dismiss(); // Dialog를 닫는다.
                                        }

                                        @Override
                                        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                                            Log.d("error", "Friend_Update_DB_Fail");

                                            Toast.makeText(FriendActivity.this, "오류가 발생하였습니다.\n" +
                                                    "다시 시도해주세요.", Toast.LENGTH_LONG).show();
                                        }

                                    });
                                }


                            }
                        });

                        //  "삭제" 버튼에 대한 이벤트 처리
                        friendDelete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                final RequestParams param = new RequestParams();

                                // DB - delete 에 접근
                                client.get(getString(R.string.URL) + "/friend/delete/" + finalViewHolder.text1.getText().toString(), param, new AsyncHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int i, Header[] headers, byte[] bytes) {
                                        listView(); // listView를 초기화
                                        Log.d("error", "Friend_Delete_Success");
                                        dialog.dismiss();   // Dialog를 닫는다.
                                    }

                                    @Override
                                    public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                                        Log.d("error", "Friend_Delete_Fail");
                                        Toast.makeText(FriendActivity.this, "오류가 발생하였습니다.\n" +
                                                "다시 시도해주세요.", Toast.LENGTH_LONG).show();
                                    }


                                });


                            }
                        });

                        // "생년월일" EditText를 누르면 DatePickerDialog를 띄운다.
                        friendBirthday.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DatePickerDialog.OnDateSetListener callBack = new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                        String Month = "" + (1 + monthOfYear);
                                        String Day = "" + dayOfMonth;
                                        if ((1 + monthOfYear) / 10 == 0)
                                            Month = "0" + (1 + monthOfYear);
                                        if (dayOfMonth / 10 == 0)
                                            Day = "0" + dayOfMonth;
                                        Birthday = year + "년 " + Month + "월 " + Day + "일 ";
                                        friendBirthday.setText(Birthday);
                                    }
                                };
                                datePickerDialog = new DatePickerDialog(FriendActivity.this, callBack, 2000, 0, 1);
                                datePickerDialog.show();
                            }
                        });
                        // "탄생시간" EditText를 누르면 TimePickerDialog를 띄운다.
                        friendBirthtime.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TimePickerDialog.OnTimeSetListener callBack = new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        String hour = hourOfDay + "";
                                        String minutes = minute + "";
                                        if (hourOfDay / 10 == 0)
                                            hour = "0" + hourOfDay;
                                        if (minute / 10 == 0)
                                            minutes = "0" + minute;

                                        Birthtime = hour + "시 " + minutes + "분 " + "00초";
                                        friendBirthtime.setText(Birthtime);
                                    }
                                };
                                timePickerDialog = new TimePickerDialog(FriendActivity.this, callBack, 8, 00, true);
                                timePickerDialog.show();
                            }
                        });

                        dialog.show();  // "관리" 버튼을 눌렀을 때 Dialog를 띄운다.
                    }   // "관리" 버튼 리스너의 onClick 끝

                }); // "관리" 버튼 리스너 끝
            } catch (JSONException e1) {
                e1.printStackTrace();

            }


            // listView의 View 하나에 이벤트 대한 다이얼로그 내용
            final ViewHolder finalViewHolder1 = viewHolder; // listView의 View 하나에 대한 URL 접근을 위해 필요

            // 하나의 View 클릭에 대한 내용
            layoutView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {


                    // Dialog 생성
                    final Dialog dialog = new Dialog(FriendActivity.this);
                    dialog.setTitle("지인정보");

                    // Dialog의 View 지정
                    dialog.setContentView(R.layout.activity_friend_add_btn);

                    // 다이얼로그 크기설정 을 param에 넣어 Dialog에 적용한다.
                    WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                    params.width = WindowManager.LayoutParams.MATCH_PARENT;
                    params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    dialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);


                    //  다이얼 로그 내 View Id 값 할당
                    friendConfirm = (Button) dialog.findViewById(R.id.friendConfirm);
                    friendUpdate = (Button) dialog.findViewById(R.id.friendUpdate);
                    friendDelete = (Button) dialog.findViewById(R.id.friendDelete);
                    friendStore = (Button) dialog.findViewById(R.id.friendStore);
                    friendEmail = (EditText) dialog.findViewById(R.id.friendEmail);
                    friendName = (EditText) dialog.findViewById(R.id.friendName);
                    friendBirthday = (TextView) dialog.findViewById(R.id.friendBirthday);
                    friendBirthtime = (TextView) dialog.findViewById(R.id.friendBirthtime);
                    friend1_btn = (RadioButton) dialog.findViewById(R.id.friend_1_btn);
                    friend2_btn = (RadioButton) dialog.findViewById(R.id.friend_2_btn);
                    friend3_btn = (RadioButton) dialog.findViewById(R.id.friend_3_btn);
                    friendman_btn = (RadioButton) dialog.findViewById(R.id.friend_man_btn);
                    friendwoman_btn = (RadioButton) dialog.findViewById(R.id.friend_woman_btn);
                    friend_btn = (RadioGroup) dialog.findViewById(R.id.friend_btn);
                    friend_gender = (RadioGroup) dialog.findViewById(R.id.friend_gender);

                    final Spinner emailDomainSpinnerTemp = (Spinner) dialog.findViewById(R.id.friendEmailDomain);
                    final TextView friendDomainStandard = (TextView) dialog.findViewById((R.id.friendDomainStandard));
                    emailDomainSpinnerTemp.setVisibility(View.GONE);
                    friendDomainStandard.setVisibility(View.GONE);

                    final RequestParams param = new RequestParams();
                    //param.add("user_no", ""+10);
                    // DB - getByNo 에 접근
                    client.post(getString(R.string.URL) + "/friend/one/" + finalViewHolder1.text1.getText().toString(), param, new JsonHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            super.onSuccess(statusCode, headers, response);

                            try {
                                // DB에서 친구 정보를 불러오고, 각 EditText의 포커스를 뺏는다.
                                friendEmail.setText(response.getString("email"));
                                friendEmail.setFocusable(false);
                                friendName.setText(response.getString("name"));
                                friendName.setFocusable(false);

                                String friend_birthDay[] = response.getString("birthday").split("-");
                                String friend_birthTime[] = response.getString("birthday_detail").split(":");
                                friendBirthday.setText(friend_birthDay[0] + "년 " + friend_birthDay[1] + "월 " + friend_birthDay[2] + "일");
                                friendBirthtime.setText(friend_birthTime[0] + "시 " + friend_birthTime[1] + "분 " + friend_birthTime[2] + "초");
                                friendBirthday.setFocusable(false);
                                friendBirthtime.setFocusable(false);

                                // Dialog 창에서 성별과 음력/양력의 라디오 버튼을 고정시킨다.
                                friend1_btn.setClickable(false);
                                friend2_btn.setClickable(false);
                                friend3_btn.setClickable(false);
                                friendman_btn.setClickable(false);
                                friendwoman_btn.setClickable(false);

                                solarlunar = response.getInt("solarlunar");
                                gender = response.getInt("gender");

                                // RadioGroup에 체크할 RadioButton을 정한다.
                                switch (solarlunar) {
                                    case 0:
                                        friend_btn.check(R.id.friend_1_btn);
                                        break;
                                    case 1:
                                        friend_btn.check(R.id.friend_2_btn);
                                        break;
                                    case 2:
                                        friend_btn.check(R.id.friend_3_btn);
                                        break;
                                    default:
                                        break;

                                }

                                // RadioGroup에 체크할 RadioButton을 정한다.
                                switch (gender) {
                                    case 0:
                                        friend_gender.check(R.id.friend_man_btn);
                                        break;
                                    case 1:
                                        friend_gender.check(R.id.friend_woman_btn);
                                        break;

                                    default:
                                        break;

                                }
                                Log.d("error", "Friend_Information_DB_Success");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Log.d("error", "friend_information_DB_fail");
                            if (statusCode == 200) {
                                //  200  ==>  서버에 접근은 하였으나, 인증과정에서 Token 값이 바뀌었기 때문에 인증이 되지않는 경우 이다.
                                Toast.makeText(getApplicationContext(), "다른 디바이스에서 해당 계정으로 로그인 되었습니다.\n 본 디바이스는 로그아웃 됩니다.", Toast.LENGTH_LONG).show();
                                checkRegid.logout(FriendActivity.this);
                            } else {
                                Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.\n" +
                                        "다시 시도해주세요." + responseString, Toast.LENGTH_LONG).show();
                                //  그외의 에러 코드가 들어온다.
                            }

                        }
                    });


                    // Dialog 창에서 "저장하기" 버튼 없애고 "확인" 버튼을 보여준다.
                    friendStore.setVisibility(View.GONE);
                    friendConfirm.setVisibility(View.VISIBLE);

                    // "확인" 버튼에 대한 이벤트 처리
                    friendConfirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();   // 다이얼 로그를 닫는다.
                        }
                    });

                    dialog.show();
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            emailDomainSpinnerTemp.setVisibility(View.VISIBLE);
                            friendDomainStandard.setVisibility(View.VISIBLE);
                        }
                    });

                }

            });
            return layoutView;
        } // getView 함수 끝

    }   // ListAdapter 정의 끝


    // RadioGroup내의 버튼 지정에 대한 처리를 하는 함수 - > " RadioGroup.OnCheckedChangeListener" 를 상속 받음
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group == friend_btn) {
            if (checkedId == R.id.friend_1_btn)
                solarlunar = 0;
            else if (checkedId == R.id.friend_2_btn)
                solarlunar = 1;
            else if (checkedId == R.id.friend_3_btn)
                solarlunar = 2;
        } else if (group == friend_gender) {
            if (checkedId == R.id.friend_man_btn)
                gender = 0;
            else if (checkedId == R.id.friend_woman_btn)
                gender = 1;
        }
    }


    // activity_friend 레이아웃 안에 "추가하기" 버튼에 대한 이벤트 처리를 하는 함수(핸들러)
    public void onClick_info(View g) {



        // 추가하기 Dialog 생성 & Dialog 내의 View 설정
        final Dialog dialog = new Dialog(FriendActivity.this);
        dialog.setTitle("지인정보 추가");
        dialog.setContentView(R.layout.activity_friend_add_btn);

        // 다이얼로그 크기설정
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        // 다이얼 로그내의 Id 값 초기화
        friendStore = (Button) dialog.findViewById(R.id.friendStore);
        friendEmail = (EditText) dialog.findViewById(R.id.friendEmail);
        friendName = (EditText) dialog.findViewById(R.id.friendName);
        friendBirthday = (TextView) dialog.findViewById(R.id.friendBirthday);
        friendBirthtime = (TextView) dialog.findViewById(R.id.friendBirthtime);
        friend1_btn = (RadioButton) dialog.findViewById(R.id.friend_1_btn);
        friend2_btn = (RadioButton) dialog.findViewById(R.id.friend_2_btn);
        friend3_btn = (RadioButton) dialog.findViewById(R.id.friend_3_btn);
        friendman_btn = (RadioButton) dialog.findViewById(R.id.friend_man_btn);
        friendwoman_btn = (RadioButton) dialog.findViewById(R.id.friend_woman_btn);
        friend_btn = (RadioGroup) dialog.findViewById(R.id.friend_btn);
        friend_gender = (RadioGroup) dialog.findViewById(R.id.friend_gender);

        friendName.requestFocus();

        emailDomainSpinner = (Spinner) dialog.findViewById(R.id.friendEmailDomain);
        ArrayAdapter<?> emailAdapter = ArrayAdapter.createFromResource(FriendActivity.this,
                R.array.email_List, R.layout.spinner_item);
        emailAdapter
                .setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
        emailDomainSpinner.setAdapter(emailAdapter);
        emailDomainSpinner
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id) {
                        // TODO Auto-generated method stub
                        emailDomain = emailDomainSpinner.getSelectedItemPosition();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // TODO Auto-generated method stub

                    }

                });
        emailDomainSpinner.setOnFocusChangeListener(FriendActivity.this);

        // "생년월일" EditText를 누르면 DatePickerDialog를 띄운다.
        friendBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.OnDateSetListener callBack = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String Month = "" + (1 + monthOfYear);
                        String Day = "" + dayOfMonth;
                        if ((1 + monthOfYear) / 10 == 0)
                            Month = "0" + (1 + monthOfYear);
                        if (dayOfMonth / 10 == 0)
                            Day = "0" + dayOfMonth;
                        Birthday = year + "년 " + Month + "월 " + Day + "일 ";
                        friendBirthday.setText(Birthday);
                    }
                };
                datePickerDialog = new DatePickerDialog(FriendActivity.this, callBack, 2000, 0, 1);
                datePickerDialog.show();
            }
        });
        // "탄생시간" EditText를 누르면 TimePickerDialog를 띄운다.
        friendBirthtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog.OnTimeSetListener callBack = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String hour = hourOfDay + "";
                        String minutes = minute + "";
                        if (hourOfDay / 10 == 0)
                            hour = "0" + hourOfDay;
                        if (minute / 10 == 0)
                            minutes = "0" + minute;

                        Birthtime = hour + "시 " + minutes + "분 " + "00초";
                        friendBirthtime.setText(Birthtime);
                    }
                };
                timePickerDialog = new TimePickerDialog(FriendActivity.this, callBack, 8, 00, true);
                timePickerDialog.show();
            }
        });

        // "저장하기" 버튼에 대한 이벤트 처기
        friendStore.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                boolean insertFriendStore = true;

                final String friendname = friendName.getText().toString().trim();
                final String friendemail = friendEmail.getText().toString().trim();

                if (friendname.isEmpty()) {
                    Toast.makeText(FriendActivity.this, "이름을 입력해주세요.", Toast.LENGTH_LONG).show();
                    friendName.requestFocus();
                    insertFriendStore = false;

                } else {
                    for (int i = 0; i < friendname.length(); i++) {
                        if (friendname.charAt(i) == ' ') {
                            Toast.makeText(FriendActivity.this, "이름 입력란에 공백이 있습니다.\n다시 입력해주세요.", Toast.LENGTH_LONG).show();
                            friendName.requestFocus();
                            insertFriendStore = false;
                            break;
                        }
                    }
                }
                if (friendemail.isEmpty()) {
                    if (insertFriendStore) {
                        Toast.makeText(FriendActivity.this, "이메일을 입력해주세요.", Toast.LENGTH_LONG).show();
                        friendEmail.requestFocus();
                    }
                    insertFriendStore = false;
                }else if (emailDomain == 0) {
                    if (insertFriendStore)
                        Toast.makeText(FriendActivity.this, "도메인을 선택하여 주세요.", Toast.LENGTH_SHORT).show();
                    insertFriendStore = false;
                }
                else {
                    for (int i = 0; i < friendemail.length(); i++) {
                        if (friendemail.charAt(i) == ' ') {
                            if (insertFriendStore) {
                                Toast.makeText(FriendActivity.this, "이메일 입력란에 공백이 있습니다.\n다시 입력해주세요.", Toast.LENGTH_LONG).show();
                                friendEmail.requestFocus();
                            }
                            insertFriendStore = false;
                            break;
                        }
                    }
                }

                String isChekedComplexedDomain[] = friendemail.split("@");
                if (isChekedComplexedDomain.length > 1 && insertFriendStore) {
                    String[] emailDomain = getResources().getStringArray(R.array.email_List);
                    for (int i = 0; i < emailDomain.length; i++) {
                        if (isChekedComplexedDomain[isChekedComplexedDomain.length - 1].equals(emailDomain[i])) {
                            insertFriendStore = false;
                            Toast.makeText(FriendActivity.this,
                                    "이메일에 도메인 주소를 사용할 수 없습니다. \n 다시 기입해 주세요.", Toast.LENGTH_LONG)
                                    .show();
                            friendEmail.requestFocus();
                            break;
                        }
                    }
                }

                if (friendBirthday.getText().toString().isEmpty() && insertFriendStore) {
                    Toast.makeText(FriendActivity.this, "생년월일을 입력해주세요.", Toast.LENGTH_LONG).show();
                    friendBirthday.requestFocus();
                    insertFriendStore = false;
                }
                if (friendBirthtime.getText().toString().isEmpty() && insertFriendStore) {
                    Toast.makeText(FriendActivity.this, "탄생 시간을 입력해주세요.", Toast.LENGTH_LONG).show();
                    friendBirthtime.requestFocus();
                    insertFriendStore = false;
                }
                if (!friendman_btn.isChecked() && !friendwoman_btn.isChecked() && insertFriendStore) {
                    Toast.makeText(FriendActivity.this, "성별을 체크해주세요.", Toast.LENGTH_LONG).show();
                    friend_gender.requestFocus();
                    insertFriendStore = false;
                }
                if (!friend1_btn.isChecked() && !friend2_btn.isChecked() && !friend3_btn.isChecked() && insertFriendStore) {
                    Toast.makeText(FriendActivity.this, "음력/양력을 체크해주세요.", Toast.LENGTH_LONG).show();
                    friend_btn.requestFocus();
                    insertFriendStore = false;
                }


                if (insertFriendStore) {

                    // 추가등록 다이얼로그에서 입력된 생년월일을 불러와 년,월,일로 잘라 DB에 맞는 날짜포맷으로 바꾼다.
                    final String birdayYear[] = friendBirthday.getText().toString().split("년");
                    final String birdayMonth[] = birdayYear[1].split("월");
                    final String birdayDay[] = birdayMonth[1].split("일");

                    // 추가등록 다이얼로그에서 입력된 탄생시간을 불러와 시,분으로 잘라 DB에 맞는 날짜포맷으로 바꾼다.
                    final String friendbirthHour[] = friendBirthtime.getText().toString().split("시");
                    final String friendbirthMinute[] = friendbirthHour[1].split("분");

                    // DB에서 받아온 정보로 RadionButton 체크
                    onCheckedChanged(friend_btn, friend_btn.getCheckedRadioButtonId());
                    onCheckedChanged(friend_gender, friend_gender.getCheckedRadioButtonId());

                    String friendEditEmailDomain_Spinner = emailDomainSpinner.getSelectedItem().toString().trim();

                    Log.d("error", birdayYear[0] + "-" + birdayMonth[0].trim() + "-" + birdayDay[0].trim());
                    Log.d("error", friendbirthHour[0] + ":" + friendbirthMinute[0].trim() + ":00");

                    // Dialog "추가하기" 버튼에서 DB에서 불러온 데이터 삽입
                    final RequestParams param = new RequestParams();
                    param.add("user_no", "" + user_no);
                    param.add("email", friendemail+"@"+friendEditEmailDomain_Spinner);
                    param.add("name", friendname);
                    param.add("birthday", birdayYear[0] + "-" + birdayMonth[0].trim() + "-" + birdayDay[0].trim());
                    param.add("birthday_detail", friendbirthHour[0] + ":" + friendbirthMinute[0].trim() + ":00");
                    param.add("solarlunar", solarlunar + "");
                    param.add("gender", gender + "");
                    param.add("modify_date", Calendar.getInstance().getTime().toString());
                    // DB - insert에 접근
                    client.post(getString(R.string.URL) + "/friend/insert", param, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int i, Header[] headers, byte[] bytes) {
                            Log.d("error", "Friend_Insert_Success");
                            listView();         // listView 새로고침
                            dialog.dismiss();   // 다이얼로그 닫는다.
                        }

                        @Override
                        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                            Log.d("error", "Friend_Insert_Failed");
                            Toast.makeText(FriendActivity.this, "오류가 발생하였습니다.\n" +
                                    "다시 시도해주세요.", Toast.LENGTH_LONG).show();
                        }

                    });
                }


            }


        });

        dialog.show();
    }


    // ListView에 대한 내용
    private void listView() {

        // DB - getByUser 에 접근해 JsonArray로 받아와서 adapter로 listview를 만든다.

        RequestParams param = new RequestParams();


        client.get(getString(R.string.URL) + "/friend/" + user_no, param, new JsonHttpResponseHandler() {


            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);

                ListAdapter listAdapter = new ListAdapter(FriendActivity.this, response);
                ListView friend_list = (ListView) findViewById(R.id.friend_list);
                friend_list.setAdapter(listAdapter);

                Log.d("error", "Friend_ListInsert_Success");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("error", "Friend_ListInsert_Fail");

                if (statusCode == 200) {
                    //  200  ==>  서버에 접근은 하였으나, 인증과정에서 Token 값이 바뀌었기 때문에 인증이 되지않는 경우 이다.
                    Toast.makeText(getApplicationContext(), "다른 디바이스에서 해당 계정으로 로그인 되었습니다.\n 본 디바이스는 로그아웃 됩니다.", Toast.LENGTH_LONG).show();
                    checkRegid.logout(FriendActivity.this);
                } else {
                    Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.\n" +
                            "다시 시도해주세요." + responseString, Toast.LENGTH_LONG).show();
                    //  그외의 에러 코드가 들어온다.
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();


        finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
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
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), FriendActivity.this);
        gcmEndMessage.gcmEndMessage(cookieStore.getCookies().get(0).getValue(),getSharedPreferences("Gcm_end_message", MODE_PRIVATE), FriendActivity.this);

    }

    // 새로운 intent를 푸쉬 받았을 떄
    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        final int broadcast_history_no = intent.getIntExtra("broadcast_history_no", 0);
        final String sender_name = intent.getStringExtra("sender_name");
        final int select_history = intent.getIntExtra("select_history", 0);

        gcmEndMessage.isSimpleHistory(cookieStore.getCookies().get(0).getValue(),broadcast_history_no, sender_name,select_history, FriendActivity.this);
    }







}








