package landvibe.co.kr.destiny01.login;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.IOException;

import cz.msebera.android.httpclient.Header;
import landvibe.co.kr.destiny01.chat.QuickstartPreferences;
import landvibe.co.kr.destiny01.R;

/**
 * Created by saltfactory on 6/8/15.
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "error";

    public RegistrationIntentService() {
        super(TAG);
    }

    /**
     * GCM을 위한 Instance ID의 토큰을 생성하여 가져온다.
     * @param intent
     */
    @SuppressLint("LongLogTag")
    @Override
    protected void onHandleIntent(Intent intent) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams param = new RequestParams();

        client.addHeader("Cookie","PASSWORD");

        // GCM Instance ID의 토큰을 가져오는 작업이 시작되면 LocalBoardcast로 GENERATING 액션을 알려 ProgressBar가 동작하도록 한다.
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(new Intent(QuickstartPreferences.REGISTRATION_GENERATING));
        // GCM을 위한 Instance ID를 가져온다.
        InstanceID instanceID = InstanceID.getInstance(this);
        String token = null;
        try {
            synchronized (TAG) {
                // GCM 앱을 등록하고 획득한 설정파일인 google-services.json을 기반으로 SenderID를 자동으로 가져온다.
                String default_senderId = "105957183161";
                // GCM 기본 scope는 "GCM"이다.
                String scope = GoogleCloudMessaging.INSTANCE_ID_SCOPE;
                // Instance ID에 해당하는 토큰을 생성하여 가져온다.
                token = instanceID.getToken(default_senderId, scope, null);

                param.add("user_no", String.valueOf(intent.getIntExtra("user_no", -1)));
                param.add("user_reg_id", token);
                // DB - update 에 접근
                client.post(getString(R.string.URL) + "/user/update/regid", param, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int i, Header[] headers, byte[] bytes) {
                        Log.d("error", "Regid update Success");
                    }

                    @Override
                    public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                        Log.d("error", "Regid update fail..");


                    }

                });


            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // GCM Instance ID에 해당하는 토큰을 획득하면 LocalBoardcast에 COMPLETE 액션을 알린다.
        // 이때 토큰을 함께 넘겨주어서 UI에 토큰 정보를 활용할 수 있도록 했다.
        Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
        registrationComplete.putExtra("token", token);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }
}
