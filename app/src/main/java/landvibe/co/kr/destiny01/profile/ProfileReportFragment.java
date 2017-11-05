package landvibe.co.kr.destiny01.profile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import landvibe.co.kr.destiny01.R;

/**
 * Created by Administrator on 2016-02-23.
 * 역술인 프로필의 리뷰 fragment
 */
public class ProfileReportFragment extends Fragment {
    private AsyncHttpClient client = new AsyncHttpClient();
    private RequestParams param = new RequestParams();
    private JSONObject m_obj;
    private JSONArray m_orders;
    private ProfileReportAdapter m_adapter;
    private ArrayList<JSONObject> m_viewitem;
    private int user_no;
    private ListView profile_review_sentense_lv;

    private ViewGroup m_rootView;

    private PersistentCookieStore cookieStore;
    private String[] result;

    @Nullable

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_viewitem = new ArrayList<>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_report, container, false);
        profile_review_sentense_lv = (ListView) view.findViewById(R.id.report_listview);

        ////////user_no 받아옴/////////////     --> regid 때문에 다시 주석 풀음
        cookieStore = new PersistentCookieStore(getActivity());
        client.addHeader("Cookie", "PASSWORD");
//        DeEncrypter deEncrypter = new DeEncrypter();
//        Log.d("error", "token : " + URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
//        String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
//        result = token.split("\\?");
//        user_no = Integer.parseInt(result[0]);
        //////////////////////////////////////////////////////////////

//        client.addHeader("Cookie","PASSWORD");

        client.get(getString(R.string.URL) + "/company/join/report/" + getActivity().getIntent().getExtras().getLong("company_no"), param, new JsonHttpResponseHandler() {
            //company에 등록된 모든 report를 불러온다
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    m_orders = response.getJSONArray("companyToReport");
//                    setText("후기("+m_orders.length()+")");
                    for (int i = 0; i < m_orders.length(); i++) {
                        m_obj = m_orders.getJSONObject(i);
                        m_viewitem.add(m_obj);      //JSONObject를 list에 삽입
                    }
                    Log.d("error", "" + m_orders);
                    m_adapter = new ProfileReportAdapter(getActivity(), R.layout.activity_profile_review, m_viewitem);
                    profile_review_sentense_lv.setAdapter(m_adapter);   //listview에 adapter를 set

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