package landvibe.co.kr.destiny01.chat;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.net.URLDecoder;
import java.util.List;

/**
 * 푸쉬 메세지를 받는 Receiver 정의
 */
public class ChatBroadcastReceiver extends WakefulBroadcastReceiver {

    private PowerManager ppm;
    private static final String TAG = "ChatBroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) { //서버에서 보낸 데이터 intent에 저장
        String action = intent.getAction();
        Log.d(TAG, "action : " + action);

        if (action != null) {
            if (action.equals("com.google.android.c2dm.intent.RECEIVE")) { // 푸시 메시지 수신 시
                try {
                    Log.d("error", "Arrive GCM Signer In App");

                    final String TRUE = "true";
                    String history_no_temp = URLDecoder.decode(intent.getStringExtra("history_no"), "UTF-8");
                    String msg = URLDecoder.decode(intent.getStringExtra("msg"), "UTF-8");
                    String isEnd = URLDecoder.decode(intent.getStringExtra("end_yn"), "UTF-8");
                    String sender_name = URLDecoder.decode(intent.getStringExtra("sender_name"), "UTF-8");
                    String select_history_temp = URLDecoder.decode(intent.getStringExtra("select_history"), "UTF-8");
                    int history_number = Integer.parseInt(history_no_temp);
                    int select_history = Integer.parseInt(select_history_temp);
                    ppm = (PowerManager) context
                            .getSystemService(Context.POWER_SERVICE);


                    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.RunningTaskInfo> Info = am.getRunningTasks(1);
                    ComponentName topActivity = Info.get(0).topActivity;

                    String topActivityName = topActivity.getClassName();        //현재 제일 위에 올라와있는 activity
                    String name = "landvibe.co.kr.destiny01.chat.ChatActivity";      //채팅 액티비티 이름

                    Log.d("error", "현재 액티비티 : "+topActivityName);
                    Intent serviceIntent = new Intent(context, ChatService.class);

                    // 휴대폰이 켜져있는지 유무
                    if(ppm.isScreenOn()) {

                        Log.d("error", "Turn On Phone , Top Activity - " + topActivityName + " , History No : " + history_number + " isEnd : "+isEnd);

                        if(isEnd.equals(TRUE))
                        {
                            Log.d("error", "켜져있고 END MESSAGE");
                            serviceIntent.putExtra("STATUS", "TURN_ON_SCREEN_AND_ENDMESSAGE");
                        }
                        //폰은 켜져잇는데 앱밖에서 메시지 gcm신호를 받은 경우
                        else{
                            Log.d("error", "켜져있고 MESSAGE");
                            serviceIntent.putExtra("STATUS", "TURN_ON_SCREEN_AND_MESSAGE");
                        }

                    }
                    else    //폰꺼져있을 경우
                    {

                        Log.d("error", "Turn Off Phone , Top Activity - " + topActivityName + " , History No : " + history_number + " isEnd : "+isEnd);

                        if(isEnd.equals(TRUE))
                        {
                            Log.d("error", "꺼져있고 END MESSAGE");

                            serviceIntent.putExtra("STATUS", "TURN_OFF_SCREEN_AND_ENDMESSAGE");

                        }
                        //폰은 꺼져잇는데 메시지 gcm신호를 받은 경우
                        else {
                            Log.d("error", "꺼져있고 MESSAGE");

                            serviceIntent.putExtra("STATUS", "TURN_OFF_SCREEN_AND_MESSAGE");

                        }
                    }
                    serviceIntent.putExtra("topActivityName", topActivityName);
                    serviceIntent.putExtra("broadcast_history_no",history_number);
                    serviceIntent.putExtra("msg", msg);
                    serviceIntent.putExtra("sender_name", sender_name);
                    serviceIntent.putExtra("select_history", select_history);
//                    serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startWakefulService(context, serviceIntent);


                } catch(Exception ex) {
                    ex.printStackTrace();
                }


            } else {
                Log.d(TAG, "Unknown action : " + action);
            }
        } else {
            Log.d(TAG, "action is null.");
        }


    }

}