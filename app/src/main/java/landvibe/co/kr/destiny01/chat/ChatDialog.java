package landvibe.co.kr.destiny01.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import java.util.Timer;
import java.util.TimerTask;

import landvibe.co.kr.destiny01.HistoryActivity;
import landvibe.co.kr.destiny01.R;

/**
 * Created by jik on 2016-03-01.
 * * 화면이 꺼져있을 때 메세지가 온 경우
 * ChatBroadcastReceiver에서 호출됨
 */

public class ChatDialog extends Activity implements View.OnClickListener {
    private Window wind;
    private PowerManager.WakeLock mWakeLock;
    private Button chat_dialog_ok_btn;
    private Button chat_dialog_cancle_btn;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat_dialog);
        //this.setFinishOnTouchOutside(false);
        Log.d("error", "다이얼로그 들어왔슈");
        chat_dialog_ok_btn= (Button) findViewById(R.id.chat_dialog_ok_btn);
        chat_dialog_ok_btn.setOnClickListener(this);
        chat_dialog_cancle_btn= (Button) findViewById(R.id.chat_dialog_cancle_btn);
        chat_dialog_cancle_btn.setOnClickListener(this);

        wind = this.getWindow();
        wind.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);     //keyGuard를 푼다
        wind.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        wind.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);       //스크린을 킨다

        wakeLock(this);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                finish();
            }
        }
                , 4000);        //4초후 자동 종료
        releaseWakeLock();
    }

    public void wakeLock(Context context) {
        if (mWakeLock != null) {
            return;
        }
        PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);

        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, "LODU");
        mWakeLock.acquire(10000);
    }

    public void releaseWakeLock() {
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    @Override
    public void onClick(View v) {
        if(v==chat_dialog_cancle_btn)
        {finish();}
        else if(v==chat_dialog_ok_btn)      //확인시 상담내역 창으로 이동
        {
            Intent intent = new Intent(this,HistoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}

