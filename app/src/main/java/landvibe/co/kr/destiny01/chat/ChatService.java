package landvibe.co.kr.destiny01.chat;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;

import cz.msebera.android.httpclient.Header;
import landvibe.co.kr.destiny01.HistoryActivity;
import landvibe.co.kr.destiny01.R;
import landvibe.co.kr.destiny01.util.DeEncrypter;

//import static android.content.Intent.getIntent;

/**
 * 리시버에서 받은 STATUS에 따라 각 상황에 다른 기능 수행
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ChatService extends Service {
    private int history_no, temp = -1;
    private String STATUS;
    private String topActivityName;
    private String msg;
    private String sender_name;
    private int select_history;
    private static final String TAG = "ChatService";
    private String[] convertSeleteHistory = {"전문", "간단", "관상손금", "해몽", "작명"};

    private SharedPreferences alarmSwitch_SP;

    NotificationManager nm;
    Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        history_no = intent.getIntExtra("broadcast_history_no", 0);
        STATUS = intent.getStringExtra("STATUS");
        topActivityName = intent.getStringExtra("topActivityName");
        msg = intent.getStringExtra("msg");
        sender_name = intent.getStringExtra("sender_name");
        select_history = intent.getIntExtra("select_history", -2);

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        alarmSwitch_SP = getSharedPreferences("alarmSwitch", MODE_PRIVATE);

        switch (STATUS) {
            case "TURN_ON_SCREEN_AND_MESSAGE": {
                Log.d("error", "TURN_ON_SCREEN_AND_MESSAGE");
                if (topActivityName.equals("landvibe.co.kr.destiny01.chat.ChatActivity")) {
                    Intent redirectIntent = new Intent(context, RedirectActivity.class);
                    redirectIntent.putExtra("broadcast_history_no", history_no);
                    redirectIntent.putExtra("topActivityName", topActivityName);
                    redirectIntent.putExtra("end_yn", false);
                    redirectIntent.putExtra("msg", msg);
                    redirectIntent.putExtra("sender_name", sender_name);
                    redirectIntent.putExtra("select_history", select_history);
                    redirectIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(redirectIntent);
                } else if (topActivityName.equals("landvibe.co.kr.destiny01.HistoryActivity")) {
                    Intent redirectIntent = new Intent(context, RedirectActivity.class);
                    redirectIntent.putExtra("broadcast_history_no", history_no);
                    redirectIntent.putExtra("topActivityName", topActivityName);
                    redirectIntent.putExtra("end_yn", false);
                    redirectIntent.putExtra("msg", msg);
                    redirectIntent.putExtra("sender_name", sender_name);
                    redirectIntent.putExtra("select_history", select_history);
                    redirectIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(redirectIntent);

                    if (alarmSwitch_SP.getBoolean("alarmSwitchCondition", false)) {
                        Notification();
                    }
                } else {
                    if (alarmSwitch_SP.getBoolean("alarmSwitchCondition", false)) {
                        Notification();
                    }
                }

                break;
            }
            case "TURN_OFF_SCREEN_AND_MESSAGE": {
                Log.d("error", "TURN_OFF_SCREEN_AND_MESSAGE");
                if (alarmSwitch_SP.getBoolean("alarmSwitchCondition", false)) {
                    Notification();
                }
                break;
            }
            case "TURN_OFF_SCREEN_AND_ENDMESSAGE": {


                Log.d("error", "TURN_OFF_SCREEN_AND_ENDMESSAGE");
                // 앱내의 Gcm_end_message 파일 내의 end_message를 true로 end_message_history_no을 history_no로 설정한다.
                SharedPreferences gcmEndMessage = getSharedPreferences("Gcm_end_message", MODE_PRIVATE);
                SharedPreferences.Editor editor = gcmEndMessage.edit();

//                Toast.makeText(ChatService.this, "" + gcmEndMessage.getAll(), Toast.LENGTH_LONG).show();

                editor.putBoolean("end_message", true);
                editor.putInt("end_message_history_no", history_no);
                editor.putString("end_message_name", sender_name);
                editor.putInt("end_message_select_history", select_history);

                editor.commit();
                if (alarmSwitch_SP.getBoolean("alarmSwitchCondition", false)) {
                    NotificationEnd();
                }

//                Toast.makeText(ChatService.this, "" + gcmEndMessage.getAll(), Toast.LENGTH_LONG).show();


                break;
            }
            case "TURN_ON_SCREEN_AND_ENDMESSAGE": {
                Log.d("error", "TURN_ON_SCREEN_AND_ENDMESSAGE");

                if (checkedTopActivity(topActivityName) == null) {
                    // 앱내의 Gcm_end_message 파일 내의 end_message를 true로 end_message_history_no을 history_no로 설정한다.
                    SharedPreferences gcmEndMessage = getSharedPreferences("Gcm_end_message", MODE_PRIVATE);
                    SharedPreferences.Editor editor = gcmEndMessage.edit();

                    editor.putBoolean("end_message", true);
                    editor.putInt("end_message_history_no", history_no);
                    editor.putString("end_message_name", sender_name);
                    editor.putInt("end_message_select_history", select_history);
                    editor.commit();

                } else {
                    Intent redirectIntent = new Intent(context, RedirectActivity.class);
                    redirectIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    redirectIntent.putExtra("broadcast_history_no", history_no);
                    redirectIntent.putExtra("topActivityName", topActivityName);
                    redirectIntent.putExtra("end_yn", true);
                    redirectIntent.putExtra("msg", msg);
                    redirectIntent.putExtra("sender_name", sender_name);
                    redirectIntent.putExtra("select_history", select_history);
                    context.startActivity(redirectIntent);
                }
                if (alarmSwitch_SP.getBoolean("alarmSwitchCondition", false)) {
                    NotificationEnd();
                }

            }
            default:
                break;
        }

        Log.d("error", "서비스 끝났슈");
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        nm.cancel(history_no);
        temp = -1;
    }

    public void Notification() {


        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams param = new RequestParams();

        ////////user_no 얻어옴/////////////
        final PersistentCookieStore cookieStore = new PersistentCookieStore(this);
        client.addHeader("Cookie", cookieStore.getCookies().get(0).getValue());
        DeEncrypter deEncrypter = new DeEncrypter();

        String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        String[] result = token.split("\\?");
        int user_no = Integer.parseInt(result[0]);
//////////////////////////////////////////////////
        client.post(getString(R.string.URL) + "/history/allcompany/" + user_no, param, new JsonHttpResponseHandler() {
            //user_no에 등록된 history 정보를 가져옴

            long notreadmsg;

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    notreadmsg = response.getJSONObject("result").getLong("NotReadAll");
                    int not_read_msg = (int) notreadmsg;
                    Log.d("error", "노티 시작");
                    nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    Resources res = getResources();

                    // 로그인 상태일때 Notification을 눌렀을 시 HistoryActivity로 인텐트를 넘긴다.

                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                            new Intent(context, HistoryActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                            .setContentTitle("사주 Now")
                            .setContentText(sender_name + " 님이 " + convertSeleteHistory[select_history] + " 메세지를 보냈습니다")
                            .setTicker(sender_name + " 님이 " + convertSeleteHistory[select_history] + " 메세지를 보냈습니다")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .setWhen(System.currentTimeMillis())
                            .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
                            .setNumber(not_read_msg);


                    Notification n = builder.build();

                    ///////////////다른 채팅방에서 notification를 보내면 최신 notification만 띄어준다////////////
                    if (temp == -1) {
                        nm.notify(history_no, n);
                        temp = history_no;
                    } else if (history_no == temp) {
                        nm.notify(history_no, n);
                    } else if (history_no != temp && temp != -1) {
                        nm.cancel(temp);
                        nm.notify(history_no, n);
                        temp = history_no;
                    }
                    //////////////////////////////////////////////
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("error", "History_ListInsert_Fail");
            }
        });


    }

    public void NotificationEnd() {

        Log.d("error", "종료노티 시작");
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Resources res = getResources();


        // 로그인 상태일때 Notification을 눌렀을 시 HistoryActivity로 인텐트를 넘긴다.

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, HistoryActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle("사주 Now")
                .setContentText(sender_name + " 님이 " + convertSeleteHistory[select_history] + "상담을 종료했습니다")
                .setTicker(sender_name + " 님이 " + convertSeleteHistory[select_history] + "상담을 종료했습니다")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);


        Notification n = builder.build();
        if (temp == -1) {
            nm.notify(history_no, n);
            temp = history_no;
        } else if (history_no == temp) {
            nm.notify(history_no, n);
        } else if (history_no != temp && temp != -1) {
            nm.cancel(temp);
            nm.notify(history_no, n);
            temp = history_no;
        }

    }

    private String checkedTopActivity(String topActivity) {
        if (topActivity.equals("landvibe.co.kr.destiny01.chat.ChatActivity") || topActivity.equals("landvibe.co.kr.destiny01.main.MainActivity") || topActivity.equals("landvibe.co.kr.destiny01.profile.ProfileActivity") || topActivity.equals("landvibe.co.kr.destiny01.InformationActivity") || topActivity.equals("landvibe.co.kr.destiny01.MyCompanyActivity") || topActivity.equals("landvibe.co.kr.destiny01.HistoryActivity") || topActivity.equals("landvibe.co.kr.destiny01.UserEditActivity") || topActivity.equals("landvibe.co.kr.destiny01.FriendActivity") || topActivity.equals("landvibe.co.kr.destiny01.config.ConfigActivity") || topActivity.equals("landvibe.co.kr.destiny01.chat.SimpleChatActivity") || topActivity.equals("landvibe.co.kr.destiny01.chat.SimplePrepareActivity") || topActivity.equals("landvibe.co.kr.destiny01.FaqActivity")) {
            return topActivity;
        }

        return null;
    }

}