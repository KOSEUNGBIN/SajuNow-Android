package landvibe.co.kr.destiny01.common;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.impl.cookie.BasicClientCookie;
import landvibe.co.kr.destiny01.main.MainActivity;
import landvibe.co.kr.destiny01.util.DeEncrypter;

public class CheckRegId {

    private AsyncHttpClient client = new AsyncHttpClient();
    // 쿠키의 결과를 담을 변수
    private String[] result;
    private int user_no;

    private PersistentCookieStore cookieStore;

    public void checkRegid(String URL, final String userkey, final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> Info = am.getRunningTasks(1);
        ComponentName name = Info.get(0).topActivity;

        Log.d("checkRegId", "-----------------------------------------------");
        Log.d("checkRegId", "" + name.getClassName());
        DeEncrypter deEncrypter = new DeEncrypter();
        String token = deEncrypter.decrypt(URLDecoder.decode(userkey));
        result = token.split("\\?");
        user_no = Integer.parseInt(result[0]);

        RequestParams params_ = new RequestParams();

        params_.put("userkey", userkey);
        params_.put("user_no", String.valueOf(user_no));
        client.addHeader("Cookie", userkey);
        client.post(URL + "/user/compare/regid", params_, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.d("checkRegId", "onSuccess ");
                    Log.d("checkRegId", "statusCode : "+ statusCode);
                    Log.d("checkRegId", "result : " + response.getString("result"));
                    Log.d("checkRegId", "compare regid : " + response.getInt("code"));

                    // 계정 (Reg_ID)이 차단 되었을 경우
                    if (response.getInt("code") == -1 && response.getString("result").equals("block")) {
                        Log.d("checkRegId", "BLOCKING ");
                        Toast.makeText(context, "계정이 차단되어 로그아웃 됩니다.\n 고객센터에 문의해주세요.", Toast.LENGTH_LONG).show();
                        logout( context);
                        LoginManager.getInstance().logOut();
                    }
                    // 계정 (Reg_ID) 이 존재할 경우
                    else
                        ;

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (statusCode == 200) {

                    Log.d("checkRegId", "onFailure ");
                    Log.d("checkRegId", "statusCode : "+ statusCode);

                    //  200  ==>  서버에 접근은 하였으나, 인증과정에서 Token 값이 바뀌었기 때문에 인증이 되지않는 경우 이다.
                    Toast.makeText(context, "다른 디바이스에서 해당 계정으로 로그인 되었습니다.\n 본 디바이스는 로그아웃 됩니다.", Toast.LENGTH_LONG).show();
                    logout( context);
                } else {
                    Toast.makeText(context, "오류가 발생하였습니다.\n" +
                            "다시 시도해주세요." + responseString, Toast.LENGTH_LONG).show();
                    //  그외의 에러 코드가 들어온다.
                }
            }
        });

        Log.d("checkRegId", "-----------------------------------------------");
    }

    public void logout(final Context context) {

        Log.d("checkRegId", "-- logout start --");
        cookieStore = new PersistentCookieStore(context);
        cookieStore.clear();
        BasicClientCookie newCookie = new BasicClientCookie("email_cookie", "login");
        newCookie.setVersion(1);
        newCookie.setDomain("{Server Domain}");
        newCookie.setPath("/");
        newCookie.setValue(result[1]);
        cookieStore.addCookie(newCookie);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        Log.d("checkRegId", "-- logout end --");
    }



}
