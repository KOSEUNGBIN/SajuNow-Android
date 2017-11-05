package landvibe.co.kr.destiny01.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.impl.cookie.BasicClientCookie;
import landvibe.co.kr.destiny01.R;
import landvibe.co.kr.destiny01.main.MainActivity;
import landvibe.co.kr.destiny01.util.DeEncrypter;

/**
 * Created by Administrator on 2016-02-23.
 */
public class ProfileInfoFragment extends Fragment {
    private AsyncHttpClient client = new AsyncHttpClient();
    private PersistentCookieStore cookieStore;
    private String[] result;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_introduce, container, false);
        final TextView profile_hello_sentense_tv = (TextView) view.findViewById(R.id.profile_hello_sentense_tv);
        final TextView profile_experience_sentense_tv = (TextView) view.findViewById(R.id.profile_experience_sentense_tv);
        final TextView profile_category_detail_sentense_tv = (TextView) view.findViewById(R.id.profile_category_detail_sentense_tv);
        final TextView profile_introduce_sentense_tv = (TextView) view.findViewById(R.id.profile_introduce_sentense_tv);

        //////user_no 얻어옴////////////  --> regid 때문에 다시 주석 풀음
        cookieStore = new PersistentCookieStore(getActivity());
        client.addHeader("Cookie", "PASSWORD");
//        DeEncrypter deEncrypter = new DeEncrypter();
//        Log.d("error", "token : " + URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
//        String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
//        result = token.split("\\?");
//        int user_no = Integer.parseInt(result[0]);
        //////////////////////

//        client.addHeader("Cookie","PASSWORD");
        RequestParams param = new RequestParams();

        client.get(getString(R.string.URL) + "/company/join/report/" + getActivity().getIntent().getExtras().getLong("company_no"), param, new JsonHttpResponseHandler() {
            //해당 company에 등록된 report를 모두 불러온다
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    profile_hello_sentense_tv.setText(response.getString("greeting"));
                    profile_experience_sentense_tv.setText(response.getString("experience"));
                    profile_category_detail_sentense_tv.setText(response.getString("category_detail"));
                    profile_introduce_sentense_tv.setText(response.getString("introduce"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            }
        });


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

}

