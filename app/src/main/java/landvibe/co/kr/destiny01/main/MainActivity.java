package landvibe.co.kr.destiny01.main;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.viewpagerindicator.CirclePageIndicator;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;

import cz.msebera.android.httpclient.Header;
import landvibe.co.kr.destiny01.FriendActivity;
import landvibe.co.kr.destiny01.HistoryActivity;
import landvibe.co.kr.destiny01.InformationActivity;
import landvibe.co.kr.destiny01.MyCompanyActivity;
import landvibe.co.kr.destiny01.R;
import landvibe.co.kr.destiny01.UserEditActivity;
import landvibe.co.kr.destiny01.ad.AdvertisementPageFragment;
import landvibe.co.kr.destiny01.ad.LoopViewPager;
import landvibe.co.kr.destiny01.common.CheckRegId;
import landvibe.co.kr.destiny01.common.GcmEndMessage;
import landvibe.co.kr.destiny01.config.ConfigActivity;
import landvibe.co.kr.destiny01.login.LoginActivity;
import landvibe.co.kr.destiny01.util.DeEncrypter;

/**
 * Created by user on 2016-01-30.
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int CATEGORY_PAGE_NUMBER = 6;
    private int m_advertisementCount = 5;
    private ViewPager m_categoryViewPager;
    private PagerAdapter m_categoryPagerAdapter;
    private ViewPager m_advertisementViewPager;
    private PagerAdapter m_advertisementPagerAdapter;
    private CountDownTimer m_timer;
    private int m_currentPosition;
    private BackPressCloseSystem backPressCloseSystem;

    private ProgressDialog progressDialog;

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private Menu nav_Menu;

    private TextView user_email;
    private TextView history_count;

    private PersistentCookieStore cookieStore;
    private String[] result;
    private int user_no;
    private TabLayout tabLayout;

    private AsyncHttpClient client_user = new AsyncHttpClient();

    private CheckRegId checkRegid;
    private GcmEndMessage gcmEndMessage;
    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        tabLayout = (TabLayout) findViewById(R.id.category_tabs);
        final CirclePageIndicator advertisementCirclePageIndicator = (CirclePageIndicator) findViewById(R.id.advertisement_indicator);

        m_categoryViewPager = (ViewPager) findViewById(R.id.category_pager);
        m_categoryPagerAdapter = new CategoryPagerAdapter(getSupportFragmentManager());
        m_categoryViewPager.setAdapter(m_categoryPagerAdapter);

        tabLayout.setupWithViewPager(m_categoryViewPager);

        m_advertisementViewPager = (ViewPager) findViewById(R.id.advertisement_pager);
        m_advertisementPagerAdapter = new AdvertisementPagerAdapter(getSupportFragmentManager());
        m_advertisementViewPager.setAdapter(m_advertisementPagerAdapter);

        advertisementCirclePageIndicator.setViewPager(m_advertisementViewPager);
        backPressCloseSystem = new BackPressCloseSystem(this);

        checkRegid = new CheckRegId();
        gcmEndMessage = new GcmEndMessage();


        toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        drawer = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.main_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // 액션바에 email,상담중인 개수 표현
        navigationView.setNavigationItemSelectedListener(this);
        final View header = navigationView.getHeaderView(0);
        nav_Menu = navigationView.getMenu();

        user_email = (TextView) header.findViewById(R.id.user_email);
        history_count = (TextView) header.findViewById(R.id.history_count);

        onRestart();


        //광고 fragment가 자동으로 넘어가게 하기 위한 타이머 설정 및 적용
        m_timer = new CountDownTimer(5 * 1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                //Log.i("position", "onTick : " + m_currentPosition);
            }

            @Override
            public void onFinish() {
                if (m_currentPosition == m_advertisementCount - 1) {
                    //	Log.i("position", "onFinish - if : " + m_currentPosition);
                    advertisementCirclePageIndicator.setCurrentItem(0);
                } else {
                    //	Log.i("position", "onFinish - else : " + m_currentPosition);
                    advertisementCirclePageIndicator.setCurrentItem(m_currentPosition + 1);
                }
            }
        };
        m_timer.start();


        //광고 fragment indicator 설정
        advertisementCirclePageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


            }

            @Override
            public void onPageSelected(int position) {
                Log.i("position", "onPageSelected : " + m_currentPosition);
                m_currentPosition = position;
                m_timer.cancel();
                m_timer.start();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    @Override
    public void onBackPressed() {
        backPressCloseSystem.onBackPressed();

        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
    }

    //카테고리 페이지 어뎁터
    private class CategoryPagerAdapter extends FragmentPagerAdapter {
        private final String[] m_TITLES = {"전문사주", "간단사주", "관상손금", "해몽", "작명"}; //indicator 제목

        public CategoryPagerAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }

        //페이지 넘버에 해당하는 fragment 생성
        @Override
        public Fragment getItem(int iPageNumber) {
            return CategoryPageFragment.create(iPageNumber);
        }

        @Override
        public int getCount() {
            return m_TITLES.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return m_TITLES[position];
        }
    }

    private class AdvertisementPagerAdapter extends FragmentPagerAdapter {

        public AdvertisementPagerAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }

        @Override
        public Fragment getItem(int iPageNumber) {
            return AdvertisementPageFragment.create(LoopViewPager.toRealPosition(iPageNumber, m_advertisementCount));
        }

        @Override
        public int getCount() {
            return m_advertisementCount;
        }
    }

    public class BackPressCloseSystem {

        private long backKeyPressedTime = 0;
        private Toast toast;

        private Activity activity;

        public BackPressCloseSystem(Activity activity) {
            this.activity = activity;
        }

        public void onBackPressed() {

            if (isAfter2Seconds()) {
                backKeyPressedTime = System.currentTimeMillis();
                // ����ð��� �ٽ� �ʱ�ȭ

                toast = Toast.makeText(activity,
                        "앱을 종료하려면 뒤로가기 버튼을 한번 더 눌러주세요.",
                        Toast.LENGTH_SHORT);
                toast.show();

                return;
            }

            if (isBefore2Seconds()) {

                programShutdown();
                toast.cancel();
                PersistentCookieStore persistentCookieStore = new PersistentCookieStore(getApplicationContext());
                //persistentCookieStore.clear();
            }
        }

        private boolean isAfter2Seconds() {
            return System.currentTimeMillis() > backKeyPressedTime + 2000;
            // 2�� ������ ���
        }

        private boolean isBefore2Seconds() {
            return System.currentTimeMillis() <= backKeyPressedTime + 2000;
            // 2�ʰ� ������ �ʾ��� ���

        }

        private void programShutdown() {
            activity.moveTaskToBack(true);
            activity.finish();
            //     android.os.Process.killProcess(android.os.Process.myPid());
            //  System.exit(0);


        }
    }

    // 휴대폰의 옵션메뉴 버튼에 대한 menu(sidebar_bottom_common)의 처리 부분
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu items for use in the action bar
////        MenuInflater inflater = getMenuInflater();
////        inflater.inflate(R.menu.sidebar_bottom_common, menu);
//
//
//
//
//        return super.onCreateOptionsMenu(menu);
//    }

//    // 옵션메뉴에 대한 이벤트 처리
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        int id = item.getItemId();
//
////        //noinspection SimplifiableIfStatement
////        if (id == R.id.logout) {
////
////            PersistentCookieStore cookieStore = new PersistentCookieStore(getApplicationContext());
////            updateRegistId();
////            cookieStore.clear();
////            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
////            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
////            startActivity(intent);
////
////            return true;
////        }
//        return super.onOptionsItemSelected(item);
//    }
//

    // 터치에 대한의 메뉴창 처리 부분
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    // 터치에 대한의 메뉴창의 이벤트 처리 부분
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
    }

    // "메뉴" 버튼 클릭시 나올 NavigationView, DrawerLayout을 정의한 함수
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        profile_user_id_tv = (TextView) this.findViewById(R.id.user_id);
//        profile_user_id_tv.setText(result[1]);
//
//        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
//
//            @SuppressWarnings("StatementWithEmptyBody")
//            @Override
//            public boolean onNavigationItemSelected(MenuItem item) {
//                // Handle navigation view item clicks here.

        if (cookieStore.getCookies().size() > 0 && cookieStore.getCookies().get(0).getName().equals("UserKey")) {

            // 로그인시에만 해당 액티비티 전환이 가능하다.

            int id = item.getItemId();
            Log.d("error", "in on NavigationItemSelected");


            // Handle the camera action
            switch (id) {
                case R.id.sidebar_user_inform: {
                    // 공지사항으로 이동

                    Intent myIntent = new Intent(getApplicationContext(), InformationActivity.class);
//                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    drawer.closeDrawer(GravityCompat.START);
                    startActivity(myIntent);
                    overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                    break;

                }
                case R.id.sidebar_user_mycompany: {

                    // 나의 역술인으로 이동
                    Intent myIntent = new Intent(getApplicationContext(), MyCompanyActivity.class);
//                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    drawer.closeDrawer(GravityCompat.START);
                    startActivity(myIntent);
                    overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                    break;
                }
                case R.id.sidebar_user_history: {

                    // 상담내역으로 이동
                    Intent myIntent = new Intent(getApplicationContext(), HistoryActivity.class);
//                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    drawer.closeDrawer(GravityCompat.START);
                    startActivity(myIntent);
                    overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                    break;
                }
                case R.id.sidebar_user_myinfo: {

                    // 나의 정보변경으로 이동
                    Intent myIntent = new Intent(getApplicationContext(), UserEditActivity.class);
                    myIntent.putExtra("from", "main");
//                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    drawer.closeDrawer(GravityCompat.START);
                    startActivity(myIntent);
                    overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                    break;
                }
                case R.id.sidebar_user_addfriend: {

                    // 지인 정보로 이동
                    Intent myIntent = new Intent(getApplicationContext(), FriendActivity.class);
//                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    drawer.closeDrawer(GravityCompat.START);
                    startActivity(myIntent);
                    overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                    break;
                }


                case R.id.sidebar_user_login: {

                    //  로그아웃시 누르면 로그인 화면으로 이동한다.
                    Intent myIntet = new Intent(getApplicationContext(), LoginActivity.class);
//                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    drawer.closeDrawer(GravityCompat.START);
                    startActivity(myIntet);
                    overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                    break;
                }

                case R.id.sidebar_user_config: {
                    Intent myIntent = new Intent(getApplicationContext(), ConfigActivity.class);
//                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    drawer.closeDrawer(GravityCompat.START);
                    startActivity(myIntent);
                    overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                    break;
                }
            }
        } else {
            // 로그아웃중에 터치 시 로그인 화면으로 이동한다.

            Intent myIntent = new Intent(getApplicationContext(), LoginActivity.class);
//                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            drawer.closeDrawer(GravityCompat.START);
            startActivity(myIntent);
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
        }
        // 사이드바 하위 메뉴에 이벤트 처리에 대한 주석
                        /*else if (id == R.id.nav_share) {

                        } else if (id == R.id.nav_send) {

                        }*/

//                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//                drawer.closeDrawer(GravityCompat.START);
//                return true;
//            }
//        });
//        drawer.openDrawer(navigationView);
//        //drawer.closeDrawer(GravityCompat.START); // 닫힘
//
//        Toast.makeText(this, "구둔", Toast.LENGTH_SHORT).show();
        return true;
    }


    // 새로운 intent를 푸쉬 받았을 떄
    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        final int broadcast_history_no = intent.getIntExtra("broadcast_history_no", 0);
        final String sender_name = intent.getStringExtra("sender_name");
        final int select_history = intent.getIntExtra("select_history", -2);

        gcmEndMessage.isSimpleHistory(cookieStore.getCookies().get(0).getValue(), broadcast_history_no, sender_name, select_history, MainActivity.this);

    }


    @Override
    protected void onRestart() {
        super.onRestart();
        ////////////////////if 로그인이 됬을시 실행/////비로그인시에는 로그인 버튼이 들어가게////////
        cookieStore = new PersistentCookieStore(this);
        if (cookieStore.getCookies().size() > 0 && cookieStore.getCookies().get(0).getName().equals("UserKey")) {
            RequestParams params = new RequestParams();
            client_user.addHeader("Cookie", cookieStore.getCookies().get(0).getValue());
            DeEncrypter deEncrypter = new DeEncrypter();
            Log.d("error", "token : " + URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
            String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
            result = token.split("\\?");
            user_no = Integer.parseInt(result[0]);

            checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), MainActivity.this);
            gcmEndMessage.gcmEndMessage(cookieStore.getCookies().get(0).getValue(), getSharedPreferences("Gcm_end_message", MODE_PRIVATE), MainActivity.this);

            // 상담 중인 개수와 공지사항이 일주일 내에 온것이 있는지 여부를 확인한다.
            client_user.post(getString(R.string.URL) + "/history/consert/count/user/" + user_no, params, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        user_email.setText("" + result[1] + "님");
                        history_count.setVisibility(View.VISIBLE);

                        JSONObject resultObject = response.getJSONObject("result");

                        // 상담 중인 개수
                        long count = resultObject.getLong("count");

                        if (count >= 0)
                            history_count.setText(count + "건 상담중");

                        //  공지 사항 최신판 여부
                        long information_new = resultObject.getLong("information_new");

                        if (information_new > 0)
                            nav_Menu.findItem(R.id.sidebar_user_inform).setTitle(Html.fromHtml("공지사항<font color='#800020'>   New</font>"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    super.onSuccess(statusCode, headers, response);

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.d("error", "Error\n" + responseString);
                    if (statusCode == 200) {
                        //  200  ==>  서버에 접근은 하였으나, 인증과정에서 Token 값이 바뀌었기 때문에 인증이 되지않는 경우 이다.
                        Toast.makeText(getApplicationContext(), "다른 디바이스에서 해당 계정으로 로그인 되었습니다.\n 본 디바이스는 로그아웃 됩니다.", Toast.LENGTH_LONG).show();
                        checkRegid.logout(MainActivity.this);

                    } else {
                        Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.\n" +
                                "다시 시도해주세요." + responseString, Toast.LENGTH_LONG).show();
                        //  그외의 에러 코드가 들어온다.
                    }
                    Toast.makeText(MainActivity.this, "오류가 발생하였습니다.\n" +
                            "다시 시도해주세요.", Toast.LENGTH_LONG).show();

                }
            });

            // 로그인 시 메뉴에서 보여줄 항목
            nav_Menu.findItem(R.id.sidebar_user_myinfo).setVisible(true);
            nav_Menu.findItem(R.id.sidebar_user_login).setVisible(false);

        } else {
            // 비로그인 시 메뉴에서 보여줄 항목

            user_email.setText("로그인이 필요합니다.");
            history_count.setVisibility(View.GONE);
            nav_Menu.findItem(R.id.sidebar_user_myinfo).setVisible(false);
            nav_Menu.findItem(R.id.sidebar_user_login).setVisible(true);
        }
    }
}