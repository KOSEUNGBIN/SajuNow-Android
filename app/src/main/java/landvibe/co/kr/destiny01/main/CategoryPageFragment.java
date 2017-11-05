package landvibe.co.kr.destiny01.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import landvibe.co.kr.destiny01.R;

/**
 * Created by user on 2016-01-30.
 */
public class CategoryPageFragment extends Fragment {
    private int m_pageNumber;
    private String m_url;
    private AsyncHttpClient m_client;
    private RequestParams m_params;
    private JSONObject m_obj;
    private boolean m_isTouchBottom;
    private boolean m_isLoadEntireList;
    private boolean m_isFirstCreateView;
    private int m_offset;
    private ArrayList<CategoryListViewItem> m_categoryListViewItems;
    private CategoryListViewAdapter m_categoryListViewAdapter;
    private PullToRefreshListView m_listView;
    private View m_categoryHeader;
    private ViewGroup m_rootView;
    private final int ADD_LIST_NUMBER = 100;
    private PersistentCookieStore cookieStore;

    public static CategoryPageFragment create(int iPageNumber) {
        CategoryPageFragment fragment = new CategoryPageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("page", iPageNumber);
        fragment.setArguments(bundle);
        return fragment;
    }

    //멤버 변수 초기화
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_pageNumber = getArguments().getInt("page");
        m_isTouchBottom = false;
        m_isLoadEntireList = false;
        m_isFirstCreateView = true;
        m_offset = 0;
        m_categoryListViewItems = new ArrayList<>();
        cookieStore = new PersistentCookieStore(getContext());
        m_categoryListViewAdapter = new CategoryListViewAdapter(getActivity(), R.layout.category_listview_items, m_categoryListViewItems, m_pageNumber,cookieStore);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        m_rootView = (ViewGroup) inflater.inflate(R.layout.category_page_fragment, container, false);
        m_listView = (PullToRefreshListView) m_rootView.findViewById(R.id.category_listview);

        //View를 처음 생성할 때만 역술인 정보를 로드함. 이 과정이 생략될 경우 다른 fragment를 보고 올때에도 새로운 역술인 정보를 로드하는 부작용 발생
        if (m_isFirstCreateView) {
            loadCompanyList(false);
            m_isFirstCreateView = false;
        }
        else {
            m_listView.setAdapter(m_categoryListViewAdapter);
        }

        m_listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                loadCompanyList(true);
            }
        });


       // m_listView.setOnScrollListener(this);

        return m_rootView;
    }

/*    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

        //스크롤이 멈추었고 && 마지막 아이템까지 스크롤했으며 && 아직 모든 역술인을 로드하지 않았을 경우 추가로 역술인 정보를 로드한다
        if ((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE ) && (m_isTouchBottom) && (!m_isLoadEntireList)) {
            m_isTouchBottom = false;
            Toast.makeText(getActivity(), "onScrollStateChanged 호출", Toast.LENGTH_SHORT).show();
            m_categoryListViewAdapter.replaceList(m_categoryListViewItems);
            m_listView.setAdapter(m_categoryListViewAdapter);
//            new getList().execute();
            loadCompanyList();
        }
        else if ((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) && view.getFirstVisiblePosition() == 0) {
            Toast.makeText(getActivity(), "refresh 호출", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        //현재 리스트의 마지막 아이템을 보았는지 검사 (바닥까지 스크롤 했는지 검사)
        if (view.isShown()) {
            if ((firstVisibleItem + visibleItemCount >= totalItemCount) && (totalItemCount > 0)) {
                m_isTouchBottom =  ((firstVisibleItem + visibleItemCount >= totalItemCount) && (totalItemCount > 0));
            }
        }
    }*/

    //역술인 정보를 추가로 로드함
    void loadCompanyList(final boolean isReloaded) {
        m_url = getString(R.string.URL)+"/company/outerjoin/user/join/category/";


     //   m_url += Integer.parseInt(new DeEncrypter().getInstance().decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue())).split("\\?")[0]);
        m_url += "0";


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //해당하는 카테고리 정보를 url에 입력
        if(m_pageNumber == 0)
        {
            m_url += "/all";
        }
        else if (m_pageNumber == 1) {
            m_url += "/simple";
        }
        else {
            m_url += "/" + Integer.toString(m_pageNumber);//category number 수정
        }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        m_client = new AsyncHttpClient();
        m_params = new RequestParams();

        Log.i("접속", "접속 전" + Integer.toString(m_pageNumber));
        m_client.addHeader("Cookie","PASSWORD");
        m_client.post(m_url + "/" + m_offset + "/" + ADD_LIST_NUMBER, m_params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    Log.i("접속", "접속 성공" + Integer.toString(m_pageNumber));
                    Log.i("offset", "offset : " + m_offset);
                    m_categoryListViewItems.clear();
                    for (int i = 0; i < response.length(); i++) {
//                        Log.i("접속", "response length : " + response.length());
                        m_obj = response.getJSONObject(i);

                        //역술인 정보를 categoryListViewItems에 담는다
                            m_categoryListViewItems.add(new CategoryListViewItem(m_obj.getLong("company_no"), m_obj.getString("report_count"), m_obj.getString("favorite_count"),  m_obj.getString("nick_name"), (float) m_obj.getDouble("score_average"),m_obj.getBoolean("simple_chat_possibility_result"),m_obj.getBoolean("chat_possibility"),m_obj.getBoolean("chat_switch"),m_obj.getBoolean("chat_possibility_result"), m_obj.getString("category_detail"),  m_obj.getString("company_reg_id")));

                    }

                    //받아온 역술인 정보를 어뎁터에 입력
                    m_categoryListViewAdapter.replaceList(m_categoryListViewItems);
                    m_listView.setAdapter(m_categoryListViewAdapter);

                    if(isReloaded)
                        m_listView.onRefreshComplete();

                    //listView 위치 지정하고 모든 역술인 정보를 로드했는지 검사
                   /* m_listView.setSelection(m_offset);
                    if (response.length() < ADD_LIST_NUMBER) {
                        m_offset += response.length();
                        m_isLoadEntireList = true;
                    } else {
                        m_offset += ADD_LIST_NUMBER;
                    }*/
                } catch (Exception e) {
                    Log.i("접속", "접속 성공 예외 발생" + Integer.toString(m_pageNumber));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.i("접속", "접속 실패");
//                Toast.makeText(getActivity(), m_url, Toast.LENGTH_SHORT).show();
            }
        });
        Log.i("접속", "접속 후" + Integer.toString(m_pageNumber));
    }
}