package landvibe.co.kr.destiny01;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.impl.cookie.BasicClientCookie;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import landvibe.co.kr.destiny01.chat.ChatActivity;
import landvibe.co.kr.destiny01.chat.SimpleChatActivity;
import landvibe.co.kr.destiny01.chat.SimplePrepareActivity;
import landvibe.co.kr.destiny01.common.CheckRegId;
import landvibe.co.kr.destiny01.common.GcmEndMessage;
import landvibe.co.kr.destiny01.main.MainActivity;
import landvibe.co.kr.destiny01.util.DeEncrypter;

/**
 * Created by 고승빈 on 2016-02-22.
 */
public class HistoryActivity extends AppCompatActivity {

    private int user_no;
    private int history_no;
    private boolean end_yn;
    private String result[];

    private Toolbar toolbar;
    private DrawerLayout drawer;

    private PersistentCookieStore cookieStore;
    private AsyncHttpClient client_user;

    private CheckRegId checkRegid;
    private GcmEndMessage gcmEndMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        cookieStore = new PersistentCookieStore(this);
        client_user = new AsyncHttpClient();
        client_user.addHeader("Cookie", cookieStore.getCookies().get(0).getValue());
        DeEncrypter deEncrypter = new DeEncrypter();
        String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        result = token.split("\\?");
        user_no = Integer.parseInt(result[0]);
        Log.d("error", "user_no : " + user_no);

        history_listview();

        checkRegid = new CheckRegId();
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), HistoryActivity.this);
        gcmEndMessage = new GcmEndMessage();
        gcmEndMessage.gcmEndMessage(cookieStore.getCookies().get(0).getValue(),getSharedPreferences("Gcm_end_message", MODE_PRIVATE), HistoryActivity.this);

        toolbar = (Toolbar) findViewById(R.id.toolbar_history);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    private class historyListAdapter extends BaseAdapter {

        JSONArray jsonArray = null;
        JSONObject jsonObject = null;
        Context context = null;
        LayoutInflater inflater = null;

        public historyListAdapter(Context context, JSONObject jsonObject) {
            try {
                this.context = context;
                this.jsonArray = jsonObject.getJSONObject("result").getJSONArray("list");
                this.inflater = LayoutInflater.from(this.context);
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        //  chatListAdapter 뷰홀더
        class ViewHolder {
            ImageView history_image;
            TextView history_companyName;
            TextView histor_chatmessage;
            TextView history_offline;
            TextView message_time;
            TextView history_notread;
            TextView select_history;
        }


        @Override
        public int getCount() {
            if (jsonArray.length() != 0)
                return jsonArray.length();
            else
                return 0;
        }

        @Override
        public Object getItem(int position) {
            try {
                jsonObject = jsonArray.getJSONObject(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View layoutView = convertView;
            ViewHolder viewHolder = null;




            if (layoutView == null) {

                // 뷰,뷰홀더 초기화
                inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                layoutView = inflater.inflate(R.layout.history_listview_view, null);
                viewHolder = new ViewHolder();
                viewHolder.history_image = (ImageView) layoutView.findViewById(R.id.history_image);
                viewHolder.history_companyName = (TextView) layoutView.findViewById(R.id.history_companyName);
                viewHolder.histor_chatmessage = (TextView) layoutView.findViewById(R.id.history_chatmessage);
                viewHolder.message_time = (TextView) layoutView.findViewById(R.id.message_time);
                viewHolder.history_notread = (TextView) layoutView.findViewById(R.id.history_notread);
                viewHolder.select_history = (TextView) layoutView.findViewById(R.id.select_history);



                // 뷰 저장
                layoutView.setTag(viewHolder);

            } else {
                // 뷰 재사용


                viewHolder = (ViewHolder) layoutView.getTag();

            }


            try {


                GlideUrl glideUrl = new GlideUrl(getString(R.string.URL) + "/company/image/" + jsonArray.getJSONObject(position).getString("company_no"), new LazyHeaders.Builder()
                        .addHeader("Cookie", "PASSWORD")
                        .build());

                Glide.with(getApplicationContext())
                        .load(glideUrl)
                        .bitmapTransform(new CropCircleTransformation(getApplicationContext()))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(viewHolder.history_image);

                int ex = jsonArray.getJSONObject(position).getInt("user_not_read");

                Date to = new Date();
                SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    to = transFormat.parse(jsonArray.getJSONObject(position).getJSONObject("msg").getString("send_date"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                viewHolder.history_companyName.setText(jsonArray.getJSONObject(position).getString("nick_name"));
                viewHolder.message_time.setText(calculateTime(to));


                viewHolder.histor_chatmessage.setText(jsonArray.getJSONObject(position).getJSONObject("msg").getString("message"));
                Log.d("rrrr","select_history : " +jsonArray.getJSONObject(position).getInt("select_history") + " / history_no : "+jsonArray.getJSONObject(position).getInt("history_no") );

                switch (jsonArray.getJSONObject(position).getInt("select_history")){
                    case 0 : {
                        viewHolder.select_history.setText("전문사주");
                        viewHolder.select_history.setTextColor(Color.parseColor("#8A0829"));
                        break;
                    }
                    case 1 : {
                        viewHolder.histor_chatmessage.setText("");

                        viewHolder.select_history.setText("간단사주");
                        viewHolder.select_history.setTextColor(Color.parseColor("#2E2EFE"));
                        break;
                    }
                    case 2 : {
                        viewHolder.select_history.setText("관상손금");
                        viewHolder.select_history.setTextColor(Color.parseColor("#F6E3CE"));
                        break;
                    }
                    case 3 : {
                        viewHolder.select_history.setText("해몽");
                        viewHolder.select_history.setTextColor(Color.parseColor("#AC58FA"));
                        break;
                    }
                    case 4 : {
                        viewHolder.select_history.setText("작명");
                        viewHolder.select_history.setTextColor(Color.parseColor("#A4A4A4"));
                        break;
                    }
                    default: {
                        break;
                    }
                }


                if (!jsonArray.getJSONObject(position).getBoolean("end_yn")) {
                    viewHolder.history_notread.setText("" + ex);
                    viewHolder.history_notread.setBackgroundResource(R.drawable.message_counter);
                    //viewHolder.history_notread.setBackgroundColor(Color.parseColor("#c0392b"));
                    viewHolder.history_notread.setTextSize(20);
                } else {

                    if (jsonArray.getJSONObject(position).getLong("is_report_alarmed") == 2) {
                        Log.d("error", "positiion : " + position);
                        // viewHolder.history_notread.setText("상담 종료됨 " + jsonArray.getJSONObject(position).getString("end_date").substring(0, 10));
                        viewHolder.history_notread.setText("상담 종료됨");
                        //viewHolder.history_notread.setBackgroundColor(Color.parseColor("#2c3e50"));
                        viewHolder.history_notread.setBackgroundResource(R.drawable.message_terminated);
                        viewHolder.history_notread.setTextSize(8);
                    } else {
                        viewHolder.history_notread.setText("상담 종료됨\n" + "후기 작성");
                        viewHolder.history_notread.setBackgroundResource(R.drawable.message_terminated);
                        // viewHolder.history_notread.setBackgroundColor(Color.parseColor("#3498db"));
                        viewHolder.history_notread.setTextSize(8);
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

            final int historyPosition = position;
            layoutView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.d("error", "historyactivity에서 리스트 하나 눌렀따");

//                    boolean report_check=true;

                    try {

//                            report_check = jsonArray.getJSONObject(historyPosition).getBoolean("is_report_alarmed");
                        history_no = jsonArray.getJSONObject(historyPosition).getInt("history_no");
                        end_yn = jsonArray.getJSONObject(historyPosition).getBoolean("end_yn");
                        int select_history  =  jsonArray.getJSONObject(historyPosition).getInt("select_history");

                        Intent intent = new Intent(HistoryActivity.this, select_history == 1 ? SimpleChatActivity.class : ChatActivity.class);
                        updateRegistId(user_no, history_no);

                        intent.putExtra("history_no", history_no);
                        intent.putExtra("end_yn", end_yn);
                        intent.putExtra("user_no", user_no);
                        intent.putExtra("sender_name", jsonArray.getJSONObject(historyPosition).getString("nick_name"));
                        intent.putExtra("select_history",select_history);

                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                    } catch (JSONException e) {
                        Log.d("error", " e  " +e);
                        e.printStackTrace();
                    }

                }
            });
            final int historyDeletePosition = position;
            final ViewHolder finalViewHolderDelete = viewHolder;
            layoutView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
                    builder.setTitle("상담내역 삭제 확인");

                    try {
                        String historyDelete_endDate = jsonArray.getJSONObject(historyDeletePosition).getString("end_date");
                        Log.d("error", "" + historyDelete_endDate);
                        if (historyDelete_endDate.equals("null")) {
                            history_no = jsonArray.getJSONObject(historyPosition).getInt("history_no");
                            end_yn = jsonArray.getJSONObject(historyPosition).getBoolean("end_yn");
                            final String historyNickName = jsonArray.getJSONObject(historyPosition).getString("nick_name");

                            builder.setMessage(finalViewHolderDelete.history_companyName.getText().toString() + "역술인과 상담 중입니다.\n해당 상담내역을 삭제할 수 없습니다.\n채팅방으로 들어가시겠습니까?")
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(final DialogInterface dialog, int which) {


//                            report_check = jsonArray.getJSONObject(historyPosition).getBoolean("is_report_alarmed");


                                            Intent intent = new Intent(HistoryActivity.this, ChatActivity.class);
                                            updateRegistId(user_no, history_no);

                                            intent.putExtra("history_no", history_no);
                                            intent.putExtra("end_yn", end_yn);
                                            intent.putExtra("user_no", user_no);
                                            intent.putExtra("sender_name", historyNickName);
                                            startActivity(intent);
                                            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                                            dialog.dismiss();
                                            history_listview();

                                        }
                                    })
                                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            history_listview();
                                        }
                                    });
                        } else {


                            String historyDelete_endDate_1[] = historyDelete_endDate.split("-");
                            String historyDelete_endDate_2[] = historyDelete_endDate_1[2].split(" ");
                            String historyDelete_endDate_3[] = historyDelete_endDate_2[1].split(":");


                            builder.setMessage(finalViewHolderDelete.history_companyName.getText().toString() + " 역술인과 " + historyDelete_endDate_1[0] + "년 " + historyDelete_endDate_1[1] + "월 " + historyDelete_endDate_2[0] + "일\n" + historyDelete_endDate_3[0] + "시 " + historyDelete_endDate_3[1] + "분에 끝난 상담내역을 삭제하시겠습니까?")
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(final DialogInterface dialog, int which) {

                                            try {
                                                int historyDeleteNo = jsonArray.getJSONObject(historyPosition).getInt("history_no");

                                                RequestParams historyDelete_param = new RequestParams();
                                                client_user.post(getString(R.string.URL) + "/history/delete/user/" + historyDeleteNo, historyDelete_param, new AsyncHttpResponseHandler() {
                                                    @Override
                                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                                                        Toast.makeText(HistoryActivity.this,
//                                                                "History Delete Success", Toast.LENGTH_LONG)
//                                                                .show();
                                                        dialog.dismiss();
                                                        history_listview();
                                                    }

                                                    @Override
                                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                                        Toast.makeText(HistoryActivity.this,
                                                                "오류가 발생하였습니다.\n" +
                                                                        "다시 시도해주세요.",
                                                                Toast.LENGTH_LONG).show();
                                                        history_listview();
                                                    }
                                                });
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    })
                                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            history_listview();
                                        }
                                    });
                        }
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    return false;
                }
            });


            return layoutView;
        }
    }


    private void history_listview() {

        RequestParams param = new RequestParams();
        client_user.post(getString(R.string.URL) + "/history/allcompany/" + user_no, param, new JsonHttpResponseHandler() {


            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

//                try {
////                    Toast.makeText(HistoryActivity.this,
////                            "안읽은 총개수 : " + response.getJSONObject("result").getLong("NotReadAll"),
////                            Toast.LENGTH_LONG).show();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }

                historyListAdapter listAdapter = new historyListAdapter(HistoryActivity.this, response);
                ListView history_list = (ListView) findViewById(R.id.history_list);
                history_list.setAdapter(listAdapter);


                Log.d("error", "History_List_Aception_Success");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("error", "History_ListInsert_Fail");
                if (statusCode == 200) {
                    //  200  ==>  서버에 접근은 하였으나, 인증과정에서 Token 값이 바뀌었기 때문에 인증이 되지않는 경우 이다.
                    Toast.makeText(getApplicationContext(), "다른 디바이스에서 해당 계정으로 로그인 되었습니다.\n 본 디바이스는 로그아웃 됩니다.", Toast.LENGTH_LONG).show();
                    checkRegid.logout(HistoryActivity.this);
                } else {
                    Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.\n" +
                            "다시 시도해주세요." + responseString, Toast.LENGTH_LONG).show();
                    //  그외의 에러 코드가 들어온다.
                }
            }
        });
    }

    private void updateRegistId(int user_no, long history_no) {

        RequestParams params = new RequestParams();

        params.put("user_no", user_no);
        params.put("history_no", history_no);


        client_user.post(getString(R.string.URL) + "/history/update/user", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d("error", "RegistId Update Successed");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(HistoryActivity.this,
                        "오류가 발생하였습니다.\n" +
                                "다시 시도해주세요.",
                        Toast.LENGTH_LONG).show();
            }
        });

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
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), HistoryActivity.this);
        gcmEndMessage.gcmEndMessage(cookieStore.getCookies().get(0).getValue(),getSharedPreferences("Gcm_end_message", MODE_PRIVATE), HistoryActivity.this);
        history_listview();
    }

    // 새로운 intent를 푸쉬 받았을 떄
    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        final Boolean chat_end_yn = intent.getBooleanExtra("end_yn", false);

        history_listview();

        if (chat_end_yn) {

            final int broadcast_history_no = intent.getIntExtra("broadcast_history_no", 0);
            final String sender_name = intent.getStringExtra("sender_name");
            final int select_history = intent.getIntExtra("select_history", -2);

            gcmEndMessage.isSimpleHistory(cookieStore.getCookies().get(0).getValue(),broadcast_history_no, sender_name,select_history, HistoryActivity.this);
        }


    }





    private static class TIME_MAXIMUM {
        public static final int SEC = 60;
        public static final int MIN = 60;
        public static final int HOUR = 24;
        public static final int DAY = 30;
        public static final int MONTH = 12;
    }

    public String calculateTime(Date date) {

        long curTime = System.currentTimeMillis();
        long regTime = date.getTime();
        long diffTime = (curTime - regTime) / 1000;

        String msg = null;

        if (diffTime < TIME_MAXIMUM.SEC) {
            // sec
            msg = diffTime + "초전";
        } else if ((diffTime /= TIME_MAXIMUM.SEC) < TIME_MAXIMUM.MIN) {
            // min
            System.out.println(diffTime);

            msg = diffTime + "분전";
        } else if ((diffTime /= TIME_MAXIMUM.MIN) < TIME_MAXIMUM.HOUR) {
            // hour
            msg = (diffTime) + "시간전";
        } else if ((diffTime /= TIME_MAXIMUM.HOUR) < TIME_MAXIMUM.DAY) {
            // day
            msg = (diffTime) + "일전";
        } else if ((diffTime /= TIME_MAXIMUM.DAY) < TIME_MAXIMUM.MONTH) {
            // day
            msg = (diffTime) + "달전";
        } else {
            msg = (diffTime) + "년전";
        }

        return msg;
    }


}