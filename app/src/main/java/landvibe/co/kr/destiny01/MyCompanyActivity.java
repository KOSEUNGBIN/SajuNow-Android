package landvibe.co.kr.destiny01;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import landvibe.co.kr.destiny01.common.GcmEndMessage;
import landvibe.co.kr.destiny01.main.CategoryListViewAdapter;
import landvibe.co.kr.destiny01.main.CategoryListViewItem;
import landvibe.co.kr.destiny01.util.DeEncrypter;

/**
 * Created by user on 2016-02-15.
 */
public class MyCompanyActivity extends AppCompatActivity  {
    private String m_url;
    private AsyncHttpClient m_client;
    private RequestParams m_params;
    private JSONObject m_obj;
    private ArrayList<CategoryListViewItem> m_categoryListViewItems;
    private CategoryListViewAdapter m_categoryListViewAdapter;
    private PullToRefreshListView m_listView;

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private PersistentCookieStore m_cookieStore;

    private GcmEndMessage gcmEndMessage;
    private  PersistentCookieStore cookieStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_company);


        cookieStore = new PersistentCookieStore(this);

        m_listView = (PullToRefreshListView) findViewById(R.id.my_company_listview);
        m_listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                loadList(true);
            }
        });

        m_categoryListViewItems = new ArrayList<>();
        m_cookieStore = new PersistentCookieStore(this);
        m_categoryListViewAdapter = new CategoryListViewAdapter(this, R.layout.category_listview_items, m_categoryListViewItems, -1,m_cookieStore);
        m_url = getString(R.string.URL)+"/company/join/user/join/category/";
        m_url += Integer.parseInt(new DeEncrypter().getInstance().decrypt(URLDecoder.decode(m_cookieStore.getCookies().get(0).getValue())).split("\\?")[0]); //user_no
        m_url += "/all";

        loadList(false);

        toolbar = (Toolbar) findViewById(R.id.toolbar_mycompany);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        gcmEndMessage = new GcmEndMessage();
        gcmEndMessage.gcmEndMessage(cookieStore.getCookies().get(0).getValue(),getSharedPreferences("Gcm_end_message", MODE_PRIVATE), MyCompanyActivity.this);

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
        gcmEndMessage.gcmEndMessage(cookieStore.getCookies().get(0).getValue(),getSharedPreferences("Gcm_end_message", MODE_PRIVATE),  MyCompanyActivity.this);
    }

    // 새로운 intent를 푸쉬 받았을 떄
    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        Log.d("error", "Main 액티비티 온뉴인텐트  브로드 캐스트에서 받음");

        final int broadcast_history_no = intent.getIntExtra("broadcast_history_no", 0);
        final String sender_name = intent.getStringExtra("sender_name");
        final int select_history = intent.getIntExtra("select_history", 0);

        gcmEndMessage.isSimpleHistory(cookieStore.getCookies().get(0).getValue(),broadcast_history_no, sender_name,select_history, MyCompanyActivity.this);

    }



    public void loadList(final boolean isReloaded) {
        m_client = new AsyncHttpClient();
        m_params = new RequestParams();
        m_client.addHeader("Cookie",m_cookieStore.getCookies().get(0).getValue());
        m_client.post(m_url, m_params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    m_categoryListViewItems.clear();
                    for (int i = 0; i < response.length(); i++) {
                        Log.i("abc", "1");
                        m_obj = response.getJSONObject(i);
                        if (m_obj.getLong("user_no") == Integer.parseInt(new DeEncrypter().getInstance().decrypt(URLDecoder.decode(m_cookieStore.getCookies().get(0).getValue())).split("\\?")[0])) {
                            m_categoryListViewItems.add(new CategoryListViewItem(m_obj.getLong("company_no"), m_obj.getString("report_count"), m_obj.getString("favorite_count"),  m_obj.getString("nick_name"), (float) m_obj.getDouble("score_average"),m_obj.getBoolean("simple_chat_possibility_result"),m_obj.getBoolean("chat_possibility"),m_obj.getBoolean("chat_switch"),m_obj.getBoolean("chat_possibility_result"), m_obj.getString("category_detail"), m_obj.getString("company_reg_id")));
                        }
                        Log.i("abc", "2");

                        m_categoryListViewAdapter.replaceList(m_categoryListViewItems);
                        m_listView.setAdapter(m_categoryListViewAdapter);

                        if(isReloaded)
                            m_listView.onRefreshComplete();
                    }
                    Log.i("abc", "3");
                } catch (Exception e) {
                    Log.i("abc", "exception");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.i("abc", "fail");
            }
        });
    }


}
