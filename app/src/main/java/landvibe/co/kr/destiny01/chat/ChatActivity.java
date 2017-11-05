package landvibe.co.kr.destiny01.chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import landvibe.co.kr.destiny01.R;
import landvibe.co.kr.destiny01.ReportActivity;
import landvibe.co.kr.destiny01.common.CheckRegId;
import landvibe.co.kr.destiny01.common.GcmEndMessage;
import landvibe.co.kr.destiny01.util.DeEncrypter;

import static android.support.v4.content.WakefulBroadcastReceiver.startWakefulService;

/**
 * Created by kimgh6554 on 16. 2. 12..
 */
public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ChatActivity";

    private EditText msgEdit;
    private Button sendBtn;
    private ImageButton plusBtn;
    private ImageView chatImagelistLarge;
    private LinearLayout chatFrontLayout;
    private LinearLayout chatBoxLayout;

    private LinearLayout chatSelectChoice_1;
    private LinearLayout chatSelectChoice_2;
    private LinearLayout chatSelectChoice_3;
    private ListView chatSelectList;
    private Button chatSelectChoiceBtnInformation;
    private Button chatSelectChoiceBtnPickture;
    private Button chatSelectChoiceBtnMyinformation;
    private Button chatSelectChoiceBtnFriendinformation;
    private Button chatSelectSendBtn;
    private ListView chat_list;
    private Button chatHelpBtn;
    private String company_name;
    Bitmap resized;

    private JSONArray jsonArray_selectDialog = null;


    private Intent intent;
    public Uri mImageCaptureUri = null;
    private int user_no;
    private int history_no;
    private boolean end_yn;
    private boolean chat_start;
    private int is_report_alarmed;
    private int select_history;

    private int chatSelectCount;

    private int chatSelectListSize;

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;

    private int imagePostion = 1;


    private int temp = -1;
    private String sender_name;
    private String result[];

    private SharedPreferences alarmSwitch_SP;

    List<Object> list = new ArrayList<Object>();

    AsyncHttpClient client = new AsyncHttpClient();
    PersistentCookieStore cookieStore;

    private InputMethodManager inputMethodManager;

    private Map<String, String> map = new HashMap<>();

    private CheckRegId checkRegid;
    private GcmEndMessage gcmEndMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        history_no = intent.getIntExtra("history_no", 0);
        end_yn = intent.getBooleanExtra("end_yn", false);
        chat_start = intent.getBooleanExtra("chat_start", false);
        user_no = intent.getIntExtra("user_no", 0);
        select_history = intent.getIntExtra("select_history", -2);

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        Log.d("error", convertTimeFormat("2016-05-04 14:25:24.0"));

        Log.d("error", convertTimeFormat("2016-05-04 14:25"));

        try {
            company_name = intent.getStringExtra("sender_name");
        } catch (NullPointerException e) {
            company_name = intent.getStringExtra("sender_name");
        }

//        Toast.makeText(getApplicationContext(), "select_history : "+select_history, Toast.LENGTH_LONG).show();
        String[] convertSeleteHistory = {"전문", "간단", "관상손금", "해몽", "작명"};
        setTitle(company_name + "님과의 " + (select_history >= 0 ? convertSeleteHistory[select_history] : "") + " 상담");


        if (end_yn) {
            chatBoxLayout = (LinearLayout) findViewById(R.id.chat_box_layout);
            chatBoxLayout.setVisibility(View.GONE);
        }

        cookieStore = new PersistentCookieStore(this);
        client.addHeader("Cookie", cookieStore.getCookies().get(0).getValue());
        DeEncrypter deEncrypter = new DeEncrypter();
        Log.d("error", "token : " + URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        result = token.split("\\?");

        alarmSwitch_SP = getSharedPreferences("alarmSwitch", MODE_PRIVATE);

        checkRegid = new CheckRegId();
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), ChatActivity.this);
        gcmEndMessage = new GcmEndMessage();
        gcmEndMessage_custum();

        final Dialog dialog = new Dialog(ChatActivity.this);
        dialog.setTitle("Choice ~ !");
        dialog.setContentView(R.layout.chat_select_dialog);

        Log.d("error", "history_no :" + history_no);

        Toolbar toolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        msgEdit = (EditText) findViewById(R.id.send_text);
        sendBtn = (Button) findViewById(R.id.send_btn);
        plusBtn = (ImageButton) findViewById(R.id.plus_btn);

        chatHelpBtn = (Button) findViewById(R.id.chat_help_btn);

        chatImagelistLarge = (ImageView) findViewById(R.id.chat_imagelist_large);
        chatFrontLayout = (LinearLayout) findViewById(R.id.chat_front_layout);

        chatSelectChoice_1 = (LinearLayout) dialog.findViewById(R.id.chat_select_choice_1);
        chatSelectChoice_2 = (LinearLayout) dialog.findViewById(R.id.chat_select_choice_2);
        chatSelectChoice_3 = (LinearLayout) dialog.findViewById(R.id.chat_select_choice_3);
        chatSelectList = (ListView) dialog.findViewById(R.id.chat_select_list);
        chatSelectChoiceBtnInformation = (Button) dialog.findViewById(R.id.chat_select_choice_btn_information);
        chatSelectChoiceBtnPickture = (Button) dialog.findViewById(R.id.chat_select_choice_btn_pickture);
        chatSelectChoiceBtnMyinformation = (Button) dialog.findViewById(R.id.chat_select_choice_btn_myinformation);
        chatSelectChoiceBtnFriendinformation = (Button) dialog.findViewById(R.id.chat_select_choice_btn_friendinformation);
        chatSelectSendBtn = (Button) dialog.findViewById(R.id.chat_select_send_btn);

        chatHelpBtn.setOnClickListener(this);


        sendBtn.setOnClickListener(new View.OnClickListener() {
            //메세지 보내기
            @Override
            public void onClick(View v) {

                //메세지가 없을 경우 안보냄
                if (msgEdit.getText().toString().length() != 0) {
                    sendBtn.setClickable(false);
                    sendMsg(0);
                }

            }

        });
        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                chatSelectChoice_3.setVisibility(View.GONE);
                chatSelectChoice_2.setVisibility(View.GONE);
                chatSelectChoice_1.setVisibility(View.VISIBLE);
                dialog.show();

            }
        });

        chatSelectChoiceBtnInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                chatSelectChoice_3.setVisibility(View.GONE);
                chatSelectChoice_1.setVisibility(View.GONE);
                chatSelectChoice_2.setVisibility(View.VISIBLE);
                dialog.show();
            }
        });

        chatSelectChoiceBtnPickture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                selectImage();

            }
        });

        chatSelectChoiceBtnMyinformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                RequestParams param = new RequestParams();

                client.get(getString(R.string.URL) + "/user/" + user_no, param, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);


                        try {

                            int pos;
                            String friendProfile = null;
                            friendProfile = "이름 : ";
                            friendProfile += (response.getString("name") + "\n");
                            friendProfile += ("성별 : ");
                            if (response.getInt("gender") == 0)
                                friendProfile += "남자\n";
                            else
                                friendProfile += "여자\n";
                            friendProfile += ("탄생일 : ");
                            friendProfile += (response.getString("birthday") + "\n");
                            friendProfile += ("탄생 시간 : ");
                            friendProfile += (response.getString("birthday_detail") + "\n");
                            pos = response.getInt("solarlunar");
                            switch (pos) {
                                case 0:
                                    friendProfile = friendProfile.concat("양력/평달\n");
                                    break;
                                case 1:
                                    friendProfile = friendProfile.concat("음력/평달\n");
                                    break;
                                case 2:
                                    friendProfile = friendProfile.concat("음력/윤달\n");
                                    break;
                                default:
                                    ;

                            }

                            msgEdit.setText(friendProfile);
                            sendMsg(0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.d("error", "Chat_Dialog_List_Insert_Fail");
                        if (statusCode == 200) {
                            //  200  ==>  서버에 접근은 하였으나, 인증과정에서 Token 값이 바뀌었기 때문에 인증이 되지않는 경우 이다.
                            Toast.makeText(getApplicationContext(), "다른 디바이스에서 해당 계정으로 로그인 되었습니다.\n 본 디바이스는 로그아웃 됩니다.", Toast.LENGTH_LONG).show();
                            checkRegid.logout(ChatActivity.this);
                        } else {
                            Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.\n다시 시도해주세요." + responseString, Toast.LENGTH_LONG).show();
                            //  그외의 에러 코드가 들어온다.
                        }
                        Toast.makeText(ChatActivity.this, "오류가 발생하였습니다.\n" +
                                "다시 시도해주세요.", Toast.LENGTH_LONG).show();

                    }
                });
            }
        });

        chatSelectChoiceBtnFriendinformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                RequestParams param = new RequestParams();

                client.get(getString(R.string.URL) + "/friend/" + user_no, param, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        super.onSuccess(statusCode, headers, response);

                        chatSelectListAdapter chatselectlistAdapter = new chatSelectListAdapter(ChatActivity.this, response);
                        chatSelectChoice_1.setVisibility(View.GONE);
                        chatSelectChoice_2.setVisibility(View.GONE);
                        chatSelectChoice_3.setVisibility(View.VISIBLE);
                        chatSelectList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                        chatSelectList.setAdapter(chatselectlistAdapter);
                        chatSelectListSize = chatselectlistAdapter.getCount();


                        Log.d("error", "Chat_Dialog_List_Insert_Success");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.d("error", "Chat_Dialog_List_Insert_Fail");
                        if (statusCode == 200) {
                            //  200  ==>  서버에 접근은 하였으나, 인증과정에서 Token 값이 바뀌었기 때문에 인증이 되지않는 경우 이다.
                            Toast.makeText(getApplicationContext(), "다른 디바이스에서 해당 계정으로 로그인 되었습니다.\n 본 디바이스는 로그아웃 됩니다.", Toast.LENGTH_LONG).show();
                            checkRegid.logout(ChatActivity.this);
                        } else {
                            Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.\n" +
                                    "다시 시도해주세요." + responseString, Toast.LENGTH_LONG).show();
                            //  그외의 에러 코드가 들어온다.
                        }
                        Toast.makeText(ChatActivity.this, "오류가 발생하였습니다.\n" +
                                "다시 시도해주세요.", Toast.LENGTH_LONG).show();

                    }
                });
                dialog.show();
            }
        });

        chatSelectSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < chatSelectList.getAdapter().getCount(); i++) {
                    Log.d("error", "chatSelectCount : " + map.get("" + i));
                    if (map.get("" + i) != null) {
                        try {
                            int pos;
                            String friendProfile = null;
                            friendProfile = "이름 : ";
                            friendProfile += (jsonArray_selectDialog.getJSONObject(Integer.parseInt(map.get("" + i))).getString("name") + "\n");
                            friendProfile += ("성별 : ");
                            if (jsonArray_selectDialog.getJSONObject(Integer.parseInt(map.get("" + i))).getInt("gender") == 0)
                                friendProfile += "남자\n";
                            else
                                friendProfile += "여자\n";
                            friendProfile += ("탄생일 : ");
                            friendProfile += (jsonArray_selectDialog.getJSONObject(Integer.parseInt(map.get("" + i))).getString("birthday") + "\n");
                            friendProfile += ("탄생 시간 : ");
                            friendProfile += (jsonArray_selectDialog.getJSONObject(Integer.parseInt(map.get("" + i))).getString("birthday_detail") + "\n");
                            pos = jsonArray_selectDialog.getJSONObject(Integer.parseInt(map.get("" + i))).getInt("solarlunar");
                            switch (pos) {
                                case 0:
                                    friendProfile = friendProfile.concat("양력/평달\n");
                                    break;
                                case 1:
                                    friendProfile = friendProfile.concat("음력/평달\n");
                                    break;
                                case 2:
                                    friendProfile = friendProfile.concat("음력/윤달\n");
                                    break;
                                default:
                                    ;
                            }
                            msgEdit.setText(friendProfile);
                            sendMsg(0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
                dialog.dismiss();
            }
        });
        chatImagelistLarge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatFrontLayout.setVisibility(View.VISIBLE);

                chatImagelistLarge.setVisibility(View.GONE);
            }
        });

        // 결제가 완료되고 상담이 시작할 때만 상담안내 다이얼로그를 띄운다.
        // chat_start는 결제가 종료된 PayNormalActivity에서만 받아오고 default 는 false.
        if (chat_start) {
            ///// 상담하기 버튼에 대한 안내 다이얼로그 정의
            android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(ChatActivity.this);
            View view = getLayoutInflater().inflate(R.layout.profile_announce_dialog, null);

            alertDialogBuilder.setTitle(getResources().getStringArray(R.array.profile_category_choice_items)[select_history] + " 상담 안내");
            alertDialogBuilder.setView(view);
            android.support.v7.widget.AppCompatTextView profile_announce_tv = (android.support.v7.widget.AppCompatTextView) view.findViewById(R.id.profile_announce_tv);

            profile_announce_tv.setText(getString(getResources().getIdentifier("chat_announce_" + select_history, "string", getPackageName())));

            alertDialogBuilder.setNegativeButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                            /*mAdapter.removeItem(position);
                            mAdapter.notifyDataSetChanged();*/

                    dialog.dismiss();
                }
            });


            android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }


    }

    @Override
    public void onBackPressed() {

        Log.d("error", "해당채팅방");
        //채팅방 일때 메시지를 받으면, count = 0 초기화 통신
        RequestParams param_init_count = new RequestParams();
        client.post(getString(R.string.URL) + "/history/init/user/" + history_no, param_init_count, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d(TAG, "통신 성공");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d(TAG, "통신 실패");
            }
        });
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
            case R.id.chat_menu_help_btn:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //메세지 발신
    private void sendMsg(int sender) {
        //현재시간
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String time = sdfNow.format(new Date(System.currentTimeMillis()));

        //단말기 등록 id
        PersistentCookieStore cookieStore = new PersistentCookieStore(getApplicationContext());

        RequestParams params = new RequestParams();
        params.put("history_no", history_no);
        params.put("sender", sender);
        params.put("send_date", time);
        params.put("select_history", select_history);

//        Toast.makeText(ChatActivity.this, "his no " + history_no + "/ sender " + sender + " / send_date " + time,
//                Toast.LENGTH_LONG).show();

        if (sender == 0)
            params.put("message", msgEdit.getText());
        else
            params.put("message", "image");
/////////////////////


        View view;
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.chat_listview_view, null);
        TextView chat_textlist_me = (TextView) view.findViewById(R.id.chat_textlist_me);
        chat_textlist_me.setVisibility(View.VISIBLE);
        chat_textlist_me.setText(msgEdit.getText().toString());
        TextView send_time = (TextView) view.findViewById(R.id.send_time);
        Log.d("login", ft.format(dNow));
        send_time.setText(convertTimeFormat(ft.format(dNow)));
        RelativeLayout.LayoutParams saveLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        saveLayoutParams.addRule(RelativeLayout.LEFT_OF, chat_textlist_me.getId());
        saveLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        send_time.setLayoutParams(saveLayoutParams);

        chat_list.addFooterView(view);
        list.add(view);
        msgEdit.setText("");

        client.post(getString(R.string.URL) + "/chat/insert", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                /*Toast.makeText(ChatActivity.this,
                        "메세지 보내기 성공 ㅎ", Toast.LENGTH_LONG)
                        .show();*/

                // chat_listview();
                sendBtn.setClickable(true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                Toast.makeText(ChatActivity.this,
//                        "메세지 전송 실패 원인 : " + statusCode,
//                        Toast.LENGTH_LONG).show();

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        DeEncrypter deEncrypter = new DeEncrypter();
        Log.d("error", "token : " + URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        result = token.split("\\?");
        user_no = Integer.parseInt(result[0]);

        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), ChatActivity.this);
        gcmEndMessage_custum();
    }

    // 새로운 intent를 푸쉬 받았을 떄
    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        final Boolean chatEndYn = intent.getBooleanExtra("end_yn", false);
        sender_name = intent.getStringExtra("sender_name");
        final int broadcast_history_no = intent.getIntExtra("broadcast_history_no", 0);
        final String sender_name = intent.getStringExtra("sender_name");
        select_history = intent.getIntExtra("select_history", -2);

        if (chatEndYn) {
            chatBoxLayout = (LinearLayout) findViewById(R.id.chat_box_layout);
            inputMethodManager.hideSoftInputFromWindow(chatBoxLayout.getWindowToken(), 0);

            gcmEndMessage.isSimpleHistory(cookieStore.getCookies().get(0).getValue(), broadcast_history_no, sender_name, select_history, ChatActivity.this);

            if (broadcast_history_no == history_no) {
                chatBoxLayout.setVisibility(View.GONE);
                chatHelpBtn.setText("후기작성");
            } else {
            }
        } else {
            if (broadcast_history_no == history_no) {
                chat_listview();
            } else {
                if (alarmSwitch_SP.getBoolean("alarmSwitchCondition", false)) {
                    intent.putExtra("STATUS", "TURN_OFF_SCREEN_AND_MESSAGE");
                    Intent serviceIntent = new Intent(this, ChatService.class);
                    serviceIntent.putExtras(intent);
                    startWakefulService(this, serviceIntent);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chat_help_btn: {
                if (chatHelpBtn.getText().equals("후기작성")) {
                    Intent intent = new Intent(ChatActivity.this, ReportActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.putExtra("his_no", history_no);
                    intent.putExtra("sender_name", company_name);
                    intent.putExtra("select_history", select_history);

                    startActivity(intent);
                    finish();
                } else {

                }
                break;
            }
            default: {
                break;
            }
        }
    }


    private class chatListAdapter extends BaseAdapter {

        private JSONArray jsonArray = null;
        private JSONObject jsonObject = null;
        private Context context = null;
        private LayoutInflater inflater = null;
        private int count = 0;

        public chatListAdapter(Context context, JSONObject jsonObject) {

            try {
                this.context = context;

                this.jsonObject = jsonObject.getJSONObject("result");
                this.jsonArray = this.jsonObject.getJSONArray("msgList");

                count = jsonArray.length();

                this.inflater = LayoutInflater.from(this.context);

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        //  chatListAdapter 뷰홀더
        class ViewHolder {
            TextView chat_textlist_me;
            TextView chat_textlist_you;
            ImageView chat_imagelist_me;
            ImageView chat_imagelist_you;
            TextView send_time;

            int position;
        }

        public void clearData() {
            // clear the data
            jsonObject = null;
        }


        @Override
        public int getCount() {
            return count;
        }

        @Override
        public Object getItem(int position) {

            try {

                return jsonArray.getJSONObject(position);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View layoutView = convertView;
            ViewHolder viewHolder = null;


            chatImagelistLarge = (ImageView) findViewById(R.id.chat_imagelist_large);
            chatFrontLayout = (LinearLayout) findViewById(R.id.chat_front_layout);

            Log.d("getView", "getView 호출");


            if (layoutView == null) {

                // 뷰,뷰홀더 초기화
                inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                layoutView = inflater.inflate(R.layout.chat_listview_view, null);
                viewHolder = new ViewHolder();
                viewHolder.chat_textlist_me = (TextView) layoutView.findViewById(R.id.chat_textlist_me);
                viewHolder.chat_textlist_you = (TextView) layoutView.findViewById(R.id.chat_textlist_you);
                viewHolder.chat_imagelist_me = (ImageView) layoutView.findViewById(R.id.chat_imagelist_me);
                viewHolder.chat_imagelist_you = (ImageView) layoutView.findViewById(R.id.chat_imagelist_you);
                viewHolder.send_time = (TextView) layoutView.findViewById(R.id.send_time);

                // 뷰 저장
                layoutView.setTag(viewHolder);

            } else {
                // 뷰 재사용
                viewHolder = (ViewHolder) layoutView.getTag();

            }


            try {


                int sender_id = jsonArray.getJSONObject(position).getInt("sender");
                switch (sender_id) {
                    case 0: // User Text 일 경우
                        viewHolder.chat_imagelist_me.setVisibility(View.GONE);
                        viewHolder.chat_imagelist_you.setVisibility(View.GONE);
                        viewHolder.chat_textlist_you.setVisibility(View.GONE);
                        viewHolder.chat_textlist_me.setVisibility(View.VISIBLE);
                        viewHolder.chat_textlist_me.setText(jsonArray.getJSONObject(position).getString("message"));
                        viewHolder.send_time.setText(convertTimeFormat(jsonArray.getJSONObject(position).getString("send_date")));

                        RelativeLayout.LayoutParams saveLayoutParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        saveLayoutParams.addRule(RelativeLayout.LEFT_OF, viewHolder.chat_textlist_me.getId());
                        saveLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        viewHolder.send_time.setLayoutParams(saveLayoutParams);


                        break;
                    case 1: // User Image 일 경우
                        viewHolder.chat_textlist_me.setVisibility(View.GONE);
                        viewHolder.chat_imagelist_me.setVisibility(View.VISIBLE);
                        viewHolder.chat_textlist_you.setVisibility(View.GONE);
                        viewHolder.chat_imagelist_you.setVisibility(View.GONE);
                        GlideUrl glideUrl = new GlideUrl(getString(R.string.URL) + "/history/image/" + history_no + "/" + jsonArray.getJSONObject(position).getLong("message_no"), new LazyHeaders.Builder()
                                .addHeader("Cookie", cookieStore.getCookies().get(0).getValue())
                                .build());
                        Glide
                                .with(getApplicationContext())
                                .load(glideUrl)
                                .override(1000, 333)
                                .into(viewHolder.chat_imagelist_me);


                        viewHolder.send_time.setText(convertTimeFormat(jsonArray.getJSONObject(position).getString("send_date")));

                        RelativeLayout.LayoutParams saveLayoutParams2 = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        saveLayoutParams2.addRule(RelativeLayout.LEFT_OF, viewHolder.chat_imagelist_me.getId());
                        saveLayoutParams2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        viewHolder.send_time.setLayoutParams(saveLayoutParams2);

                        break;
                    case 10: // Company Text 일 경우
                        viewHolder.chat_textlist_me.setVisibility(View.GONE);
                        viewHolder.chat_imagelist_me.setVisibility(View.GONE);
                        viewHolder.chat_imagelist_you.setVisibility(View.GONE);
                        viewHolder.chat_textlist_you.setVisibility(View.VISIBLE);
                        viewHolder.chat_textlist_you.setText(jsonArray.getJSONObject(position).getString("message"));
                        viewHolder.send_time.setText(convertTimeFormat(jsonArray.getJSONObject(position).getString("send_date")));

                        RelativeLayout.LayoutParams saveLayoutParams3 = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        saveLayoutParams3.addRule(RelativeLayout.RIGHT_OF, viewHolder.chat_textlist_you.getId());
                        saveLayoutParams3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        viewHolder.send_time.setLayoutParams(saveLayoutParams3);
                        break;
                    case 11: // Company Image 일 경우
                        viewHolder.chat_textlist_me.setVisibility(View.GONE);
                        viewHolder.chat_imagelist_me.setVisibility(View.GONE);
                        viewHolder.chat_textlist_you.setVisibility(View.GONE);
                        viewHolder.chat_imagelist_you.setVisibility(View.VISIBLE);
                        GlideUrl glideUrl2 = new GlideUrl(getString(R.string.URL) + "/history/image/" + history_no + "/" + jsonArray.getJSONObject(position).getLong("message_no"), new LazyHeaders.Builder()
                                .addHeader("Cookie", cookieStore.getCookies().get(0).getValue())
                                .build());

                        Glide.with(getApplicationContext())
                                .load(glideUrl2)
                                .override(1000, 333)
                                .into(viewHolder.chat_imagelist_you);

                        viewHolder.send_time.setText(convertTimeFormat(jsonArray.getJSONObject(position).getString("send_date")));
                        RelativeLayout.LayoutParams saveLayoutParams4 = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        saveLayoutParams4.addRule(RelativeLayout.RIGHT_OF, viewHolder.chat_imagelist_you.getId());
                        saveLayoutParams4.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        viewHolder.send_time.setLayoutParams(saveLayoutParams4);
                        break;

                    default:
                        break;

                }
            } catch (JSONException e) {

                Log.d("error", "Catched!!!!!!!!!!!!!!!");
                e.printStackTrace();
            }

            viewHolder.chat_imagelist_me.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    Bitmap bmap = getViewBitmap(v);

                    chatImagelistLarge.setImageBitmap(bmap);
                    chatFrontLayout.setVisibility(View.GONE);

                    chatImagelistLarge.setVisibility(View.VISIBLE);


                }
            });

            viewHolder.chat_imagelist_you.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Bitmap bmap = getViewBitmap(v);

                    chatImagelistLarge.setImageBitmap(bmap);
                    chatFrontLayout.setVisibility(View.GONE);

                    chatImagelistLarge.setVisibility(View.VISIBLE);


                }
            });


            return layoutView;
        }
    }

    public void chat_listview() {


        for (int i = 0; i < list.size(); i++) {
            chat_list.removeFooterView((View) list.get(i));
        }

        RequestParams param = new RequestParams();

        imagePostion = 1;

        Log.d("error", "history_no at chatList :" + history_no);

        client.post(getString(R.string.URL) + "/history/" + history_no, param, new JsonHttpResponseHandler() {
//            URL 에 +history_no 추가

            @Override
            public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                try {
                    user_no = (int) response.getJSONObject("result").getLong("user_no");
                    is_report_alarmed = (int) response.getJSONObject("result").getLong("is_report_alarmed");
                    boolean chat_end_yn = response.getJSONObject("result").getBoolean("end_yn");
                    select_history = response.getJSONObject("result").getInt("select_history");
                    Log.d("error", "is_report_alarmed : " + is_report_alarmed);

                    if (chat_end_yn) {
                        chatBoxLayout = (LinearLayout) findViewById(R.id.chat_box_layout);
                        inputMethodManager.hideSoftInputFromWindow(chatBoxLayout.getWindowToken(), 0);
                        chatBoxLayout.setVisibility(View.GONE);

                        if (is_report_alarmed == 0) {
                            gcmEndMessage.isSimpleHistory(cookieStore.getCookies().get(0).getValue(), history_no, company_name, select_history, ChatActivity.this);
                        } else if (chat_end_yn && is_report_alarmed == 1) {
                            chatHelpBtn.setText("후기작성");
                        } else {
                            chatHelpBtn.setText("");
                        }
                    } else {
                        chatHelpBtn.setText("HELP");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                chatListAdapter listAdapter = new chatListAdapter(ChatActivity.this, response);
                chat_list = (ListView) findViewById(R.id.chat_list);
                chat_list.setAdapter(listAdapter);
                chat_list.setSelection(listAdapter.getCount());


                Log.d("error", "Chat_List_Aception_Success");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("error", "Chat_ListInsert_Fail");
                if (statusCode == 200) {
                    //  200  ==>  서버에 접근은 하였으나, 인증과정에서 Token 값이 바뀌었기 때문에 인증이 되지않는 경우 이다.
                    Toast.makeText(getApplicationContext(), "다른 디바이스에서 해당 계정으로 로그인 되었습니다.\n 본 디바이스는 로그아웃 됩니다.", Toast.LENGTH_LONG).show();
                    checkRegid.logout(ChatActivity.this);
                } else {
                    Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.\n다시 시도해주세요." + responseString, Toast.LENGTH_LONG).show();
                    //  그외의 에러 코드가 들어온다.
                }
                Toast.makeText(ChatActivity.this, "오류가 발생하였습니다.\n" +
                        "다시 시도해주세요.", Toast.LENGTH_LONG).show();

            }
        });
    }

    // 사진 고르기 다이얼로그 실행
    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
                    mImageCaptureUri = getLastCaptureImageUri();
                    Log.d("error", "captureURI Path : " + mImageCaptureUri);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Log.d("error", "is EXTERNAL_CONTENT_URI ");
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    //
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
                Log.d("error", "is SELECT_FILE ");

            } else if (requestCode == REQUEST_CAMERA) {
                onCaptureImageResult(data);
                Log.d("error", "is REQUEST_CAMERA ");
            }
        }
    }

    //방금 찍힌 사진 표시
    private void onCaptureImageResult(Intent data) {

        Log.d("error", "onCaptureImage");

        Bitmap thumbnail;
        try {
            thumbnail = (Bitmap) data.getExtras().get("data");
        } catch (Exception e) {
            Intent intent_uri = new Intent();
            intent_uri.putExtra("data", mImageCaptureUri);
            onSelectFromGalleryResult(intent_uri);
            return;
        }


        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        BitmapFactory.Options options = new BitmapFactory.Options();

        if (thumbnail.getWidth() + thumbnail.getHeight() < 1000)
            thumbnail = Bitmap.createScaledBitmap(thumbnail, thumbnail.getWidth(), thumbnail.getHeight(), false);
        else if (thumbnail.getWidth() + thumbnail.getHeight() < 2000)
            thumbnail = Bitmap.createScaledBitmap(thumbnail, thumbnail.getWidth() / 2, thumbnail.getHeight() / 2, false);
        else
            thumbnail = Bitmap.createScaledBitmap(thumbnail, thumbnail.getWidth() / 3, thumbnail.getHeight() / 3, false);


        final File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SaveBitmapToFileCache(thumbnail, destination.getPath());
        //   thumbnail.recycle();
        thumbnail = null;

        File file = null;
        file = new File(destination.getPath());
        RequestParams params = new RequestParams();
        try {
            params.put("profile_picture", file);
            Log.d("error", "image is stored");

        } catch (FileNotFoundException e) {
            Log.d("error", e.toString());
        }

        client.post(getString(R.string.URL) + "/history/upload/image/user/" + history_no, params, new FileAsyncHttpResponseHandler(this) {

            @Override
            public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
                Log.d("error", "fail!!");
            }

            @Override
            public void onSuccess(int i, Header[] headers, File file) {
                Log.d("error", file.getName() + ", " + file.getPath());
                Log.d("error", "success");
                destination.deleteOnExit();
                destination.delete();
//                    sendMsg(11);
                chatListAdapter sampleAdapter = (chatListAdapter) chat_list.getAdapter();
                sampleAdapter.clearData();
                chat_list.setAdapter(sampleAdapter);
                sampleAdapter.notifyDataSetChanged();
                chat_listview();

            }


        });
    }


    //갤러리에서 선택한 사진 표시
    private void onSelectFromGalleryResult(Intent data) {


        Uri selectedImageUri = data.getData();


//        Log.d("error", " data.getExtras().get(\"data\") : " + data.getExtras().get("data"));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor;
        String selectedImagePath = "";
        try {
            cursor = managedQuery(selectedImageUri, projection, null, null,
                    null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();

            selectedImagePath = cursor.getString(column_index);

        } catch (NullPointerException e) {
            selectedImagePath = getOriginalImagePath();
        }

        Log.d("error", "selectedImageUri : " + selectedImagePath);

        final File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inDither = true;


        BitmapFactory.decodeFile(selectedImagePath, options);
        Log.d("error", "outHeight : " + options.outHeight);
        Log.d("error", "outWidth : " + options.outWidth);

        int photoWidth = options.outWidth;
        int photoHeight = options.outHeight;

        int scaleFactor = Math.min(photoWidth / 333, photoHeight / 1000);

        options.inSampleSize = scaleFactor;
        options.inJustDecodeBounds = false;

        resized = BitmapFactory.decodeFile(selectedImagePath, options);


//        if (resized.getHeight() + resized.getWidth() < 1000)
//            resized = Bitmap.createScaledBitmap(resized, resized.getWidth(),  resized.getHeight(), false);
//        else if (options.outHeight + options.outWidth < 2000)
//            resized = Bitmap.createScaledBitmap(resized, resized.getWidth() / 2, resized.getHeight() / 2, false);
//        else if (options.outHeight + options.outWidth < 3000)
//            resized = Bitmap.createScaledBitmap(resized,resized.getWidth() / 3, resized.getHeight() / 3, false);
//        else
//            resized = Bitmap.createScaledBitmap(resized,resized.getWidth()  / 4,resized.getHeight() / 4, false);

        //resized.recycle();
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(selectedImagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int exifOrientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        float exifDegree = exifOrientationToDegrees(exifOrientation);
        Log.d("error", "exifDegree : " + exifOrientation);
        Log.d("error", "exifDegree : " + exifDegree);
        resized = imgRotate(resized, exifDegree);
////////////////////////////////////////////////////

        resized.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SaveBitmapToFileCache(resized, destination.getPath());
        // resized.recycle();
        resized = null;


        File file = null;
        file = new File(destination.getPath());
        Log.d("error", destination.getPath());
        RequestParams params = new RequestParams();
        try {
            params.put("profile_picture", file);
            params.put("sender", 1);
            Log.d("error", "image is stored");

        } catch (FileNotFoundException e) {
            Log.d("error", e.toString());
        }


        client.post(getString(R.string.URL) + "/history/upload/image/user/" + history_no, params, new FileAsyncHttpResponseHandler(this) {

            @Override
            public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
                Log.d("error", "fail!!");
            }

            @Override
            public void onSuccess(int i, Header[] headers, File file) {
                Log.d("error", file.getName() + ", " + file.getPath());
                Log.d("error", "success");
                destination.deleteOnExit();
                destination.delete();
                chatListAdapter sampleAdapter = (chatListAdapter) chat_list.getAdapter();
                sampleAdapter.clearData();
                chat_list.setAdapter(sampleAdapter);
                sampleAdapter.notifyDataSetChanged();
                chat_listview();

            }

        });
    }

    private Bitmap imgRotate(Bitmap bmp, float degree) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();

        Matrix matrix = new Matrix();
        matrix.postRotate(degree);

        Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, false);
        //bmp.recycle();

        return resizedBitmap;
    }

    private void SaveBitmapToFileCache(Bitmap bitmap, String strFilePath) {

        File fileCacheItem = new File(strFilePath);
        OutputStream out = null;

        try {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    // ListAdapter 정의 부분
    public class chatSelectListAdapter extends BaseAdapter {


        JSONObject jsonObject = null;
        Context context = null;
        LayoutInflater inflater = null;

        //  ListAdapter 생성자
        public chatSelectListAdapter(Context context, JSONArray jsonArray) {
            this.context = context;
            jsonArray_selectDialog = jsonArray;
            this.inflater = LayoutInflater.from(this.context);

        }

        //  ListAdapter 뷰홀더
        class ViewHolder {
            TextView chatSelectListNumber;
            TextView chatSelectListName;
            CheckBox chatSelectListCheckbox;
        }


        //  ListAdapter 개수
        @Override
        public int getCount() {
            if (jsonArray_selectDialog.length() != 0)
                return jsonArray_selectDialog.length();
            else
                return 0;

        }

        //  ListAdapter 데이터
        @Override
        public Object getItem(int position) {
            try {
                jsonObject = jsonArray_selectDialog.getJSONObject(position);
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


            chatSelectCount = 0;

            map.clear();

            if (layoutView == null) {

                // 뷰,뷰홀더 초기화
                inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                layoutView = inflater.inflate(R.layout.chat_select_dialog_listview_review, null);
                viewHolder = new ViewHolder();
                viewHolder.chatSelectListNumber = (TextView) layoutView.findViewById(R.id.chat_select_list_number);
                viewHolder.chatSelectListName = (TextView) layoutView.findViewById(R.id.chat_select_list_name);
                viewHolder.chatSelectListCheckbox = (CheckBox) layoutView.findViewById(R.id.chat_select_list_checkbox);


                // 뷰 저장
                layoutView.setTag(viewHolder);

            } else {
                // 뷰 재사용
                viewHolder = (ViewHolder) layoutView.getTag();
            }

            try {
                viewHolder.chatSelectListNumber.setText(jsonArray_selectDialog.getJSONObject(position).getInt("friend_no") + "");
                viewHolder.chatSelectListName.setText(jsonArray_selectDialog.getJSONObject(position).getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }


//             listView의 View 하나에 이벤트 대한 다이얼로그 내용
//             listView의 View 하나에 대한 URL 접근을 위해 필요
//
//             하나의 View 클릭에 대한 내용
            final ViewHolder finalViewHolder = viewHolder;
            layoutView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (finalViewHolder.chatSelectListCheckbox.isChecked()) {
                        finalViewHolder.chatSelectListCheckbox.setChecked(false);
                        chatSelectCount--;
                        map.remove("" + (position));
                    } else {
                        finalViewHolder.chatSelectListCheckbox.setChecked(true);
                        ++chatSelectCount;
                        map.put("" + (position), "" + position);

                    }
                }
            });


            return layoutView;
        } // getView 함수 끝
    }   // ListAdapter 정의 끝

    public Uri getLastCaptureImageUri() {
        Uri uri = null;
        String[] IMAGE_PROJECTION = {
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns._ID,
        };

        try {
            Cursor cursorImages = getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    IMAGE_PROJECTION, null, null, null);
            if (cursorImages != null && cursorImages.moveToLast()) {
                uri = Uri.parse(cursorImages.getString(0)); //경로
                int id = cursorImages.getInt(1); //아이디
                cursorImages.close(); // 커서 사용이 끝나면 꼭 닫아준다.
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uri;
    }

    public String getOriginalImagePath() {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        int column_index_data = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToLast();

        return cursor.getString(column_index_data);
    }

    private void gcmEndMessage_custum() {

        final SharedPreferences gcmEndMessage_chat = getSharedPreferences("Gcm_end_message", MODE_PRIVATE);
        chatBoxLayout = (LinearLayout) findViewById(R.id.chat_box_layout);
        inputMethodManager.hideSoftInputFromWindow(chatBoxLayout.getWindowToken(), 0);

        if (gcmEndMessage_chat.getBoolean("end_message", false)) {
            final int end_message_history_no = gcmEndMessage_chat.getInt("end_message_history_no", 0);

            gcmEndMessage.gcmEndMessage(cookieStore.getCookies().get(0).getValue(), getSharedPreferences("Gcm_end_message", MODE_PRIVATE), ChatActivity.this);

            if (end_message_history_no == history_no) {
                chatBoxLayout.setVisibility(View.GONE);
                chatHelpBtn.setText("후기작성");
            } else {
            }
        } else {
            chat_listview();
        }
    }


    private Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }

    String convertTimeFormat(String original) {

        // 2016-05-04 10:25:24.0
        String hour = original.substring(11, 13);
        String result = "";
        int hour_ = Integer.parseInt(hour);
        if (hour_ > 12) {
            if (hour_ == 12)
                ;
            else
                hour_ -= 12;
            result += "오후 " + hour_;
        } else {
            result += "오전 " + hour_;
        }
        result = result + original.substring(13, 16);

        return result;
    }
}
