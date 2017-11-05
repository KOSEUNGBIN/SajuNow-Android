package landvibe.co.kr.destiny01.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import landvibe.co.kr.destiny01.FaqActivity;
import landvibe.co.kr.destiny01.FriendActivity;
import landvibe.co.kr.destiny01.HistoryActivity;
import landvibe.co.kr.destiny01.InformationActivity;
import landvibe.co.kr.destiny01.config.ConfigActivity;
import landvibe.co.kr.destiny01.main.MainActivity;
import landvibe.co.kr.destiny01.MyCompanyActivity;
import landvibe.co.kr.destiny01.UserEditActivity;
import landvibe.co.kr.destiny01.profile.ProfileActivity;

/**
 * Created by jik on 2016-03-03.
 * 리시버에서 chatactivity 넘겨주는 activity
 */
public class RedirectActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if(intent.getStringExtra("topActivityName").equals("landvibe.co.kr.destiny01.chat.ChatActivity"))
            intent.setClass(RedirectActivity.this, ChatActivity.class);
        else if(intent.getStringExtra("topActivityName").equals("landvibe.co.kr.destiny01.main.MainActivity"))
            intent.setClass(RedirectActivity.this, MainActivity.class);
        else if(intent.getStringExtra("topActivityName").equals("landvibe.co.kr.destiny01.profile.ProfileActivity"))
            intent.setClass(RedirectActivity.this, ProfileActivity.class);
        else if(intent.getStringExtra("topActivityName").equals("landvibe.co.kr.destiny01.InformationActivity"))
            intent.setClass(RedirectActivity.this, InformationActivity.class);
        else if(intent.getStringExtra("topActivityName").equals("landvibe.co.kr.destiny01.MyCompanyActivity"))
            intent.setClass(RedirectActivity.this, MyCompanyActivity.class);
        else if(intent.getStringExtra("topActivityName").equals("landvibe.co.kr.destiny01.HistoryActivity"))
            intent.setClass(RedirectActivity.this, HistoryActivity.class);
        else if(intent.getStringExtra("topActivityName").equals("landvibe.co.kr.destiny01.UserEditActivity"))
            intent.setClass(RedirectActivity.this, UserEditActivity.class);
        else if(intent.getStringExtra("topActivityName").equals("landvibe.co.kr.destiny01.FriendActivity"))
            intent.setClass(RedirectActivity.this, FriendActivity.class);
        else if(intent.getStringExtra("topActivityName").equals("landvibe.co.kr.destiny01.config.ConfigActivity"))
            intent.setClass(RedirectActivity.this, ConfigActivity.class);
        else if(intent.getStringExtra("topActivityName").equals("landvibe.co.kr.destiny01.chat.SimpleChatActivity"))
            intent.setClass(RedirectActivity.this, SimpleChatActivity.class);
        else if(intent.getStringExtra("topActivityName").equals("landvibe.co.kr.destiny01.chat.SimplePrepareActivity"))
            intent.setClass(RedirectActivity.this, SimplePrepareActivity.class);
        else if(intent.getStringExtra("topActivityName").equals("landvibe.co.kr.destiny01.FaqActivity"))
            intent.setClass(RedirectActivity.this, FaqActivity.class);


        Log.d("error", "Redirect 액티비티 " + intent.getStringExtra("topActivityName") +" 로 이동 완료");
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }


}
