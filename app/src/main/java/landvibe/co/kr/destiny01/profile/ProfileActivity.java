/*
서버 연동 예제
AsyncHttp 이용
*/


package landvibe.co.kr.destiny01.profile;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.jpardogo.android.googleprogressbar.library.GoogleProgressBar;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import landvibe.co.kr.destiny01.R;
import landvibe.co.kr.destiny01.chat.ChatActivity;
import landvibe.co.kr.destiny01.chat.SimpleChatActivity;
import landvibe.co.kr.destiny01.chat.SimplePrepareActivity;
import landvibe.co.kr.destiny01.common.CheckRegId;
import landvibe.co.kr.destiny01.common.GcmEndMessage;
import landvibe.co.kr.destiny01.login.LoginActivity;
import landvibe.co.kr.destiny01.pay.PayNormalActivity;
import landvibe.co.kr.destiny01.util.DeEncrypter;

public class ProfileActivity extends AppCompatActivity
        implements View.OnClickListener {

    private static final String TAG = "ProfileActivity";

    // 상담하기 버튼에 반응하는 alert 다이얼로그에 필요한 변수
    private String historyInsertText;
    private int historyInsert_result;
    private AlertDialog.Builder alertDialogBuilder;
    private View view;
    private long company_no_first_maintain;

    private RelativeLayout drawerLayout;
    private LinearLayout chechLayout;
    private TextView profile_user_id_tv;
    private TextView profile_nickname_tv;
    private ImageView selected_iv;
    private Button submitImageButton;
    private ImageButton favorite_iv;
    private Boolean favoriteChecked = false;
    private Button insert_chat_btn;
    //메인화면에서 선택된 역술인과 카테고리를 인텐트로 받아오는 변수
    private int category_no;
    private long company_no;
    private int user_no;
    // 상담하기 버튼의 화면 개수에 대한 카운트
    private int count = 0;
    int reversecount = 0;
    private int onoffline = 0;  //
    private int choiceme_no = 0;
    private boolean choice_me = true;
    private int choicefriend_no = 0;
    private boolean choice_friend = true;
    private String[] item;
    private Integer[] itemm;
    private String[] result;
    int REQUEST_CAMERA = 0, SELECT_FILE = 1;

    // 해당 역술인의 카테고리 리스트
    private JSONArray jsonArray_category;

    private boolean isHistorySimple;
    private AlertDialog alertDialog;

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private String company_name;
    PersistentCookieStore cookieStore;


    private AsyncHttpClient client_user = new AsyncHttpClient();
    private AsyncHttpClient client_guest = new AsyncHttpClient();
    private RequestParams param = new RequestParams();

    GoogleProgressBar mBar;

    private CheckRegId checkRegid;
    private GcmEndMessage gcmEndMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        drawerLayout = (RelativeLayout) findViewById(R.id.menu_top);
        Intent intent = getIntent();
        company_no = intent.getExtras().getLong("company_no");

        category_no = intent.getIntExtra("category_no", -2);
        company_no_first_maintain = category_no;

        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "Please wait...");

        toolbar = (Toolbar) findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        favorite_iv = (ImageButton) findViewById(R.id.favorite);
        favorite_iv.setOnClickListener(this);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        ProfileAdapter adapter = new ProfileAdapter(getSupportFragmentManager());

        adapter.addFragment(new ProfileInfoFragment(), "정보");
        adapter.addFragment(new ProfileReportFragment(), "후기");
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        // 상담하기 버튼
        insert_chat_btn = (Button) findViewById(R.id.insert_chat_btn);
        insert_chat_btn.setOnClickListener(this);


        ////////////////////////////쿠키 load, 복호화//////////////////////////////////////////////////////////////////////////
        cookieStore = new PersistentCookieStore(this);
        client_guest.addHeader("Cookie", "PASSWORD");
        if (cookieStore.getCookies().size() > 0 && cookieStore.getCookies().get(0).getName().equals("UserKey")) {

            client_user.addHeader("Cookie", cookieStore.getCookies().get(0).getValue());

            // 로그인시에만 상담하기 버튼으로 보여준다.
            insert_chat_btn.setText("상담하기");

            DeEncrypter deEncrypter = new DeEncrypter();
            Log.d("error", "token : " + URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
            String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
            result = token.split("\\?");
            user_no = Integer.parseInt(result[0]);

            checkRegid = new CheckRegId();
            checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), ProfileActivity.this);
            gcmEndMessage = new GcmEndMessage();
            gcmEndMessage.gcmEndMessage(cookieStore.getCookies().get(0).getValue(), getSharedPreferences("Gcm_end_message", MODE_PRIVATE), ProfileActivity.this);

            // 로그인 시에만 즐겨찾기 버튼을 보여준다.
            favorite_iv.setVisibility(View.VISIBLE);
            getFavorite(company_no, user_no);


        } else {
            // 비로그인시에는 상담하기 버튼을 로그인으로 텍스트를 바꾼다.
            // 클릭시 로그인화면으로 이동하는 것은 클릭리스너에서 참고
            insert_chat_btn.setText("로그인");

            // 비로그인시 즐겨찾기 버튼을 없앤다.
            favorite_iv.setVisibility(View.GONE);
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        //별점 처리
        final RatingBar ratingBar = (RatingBar) findViewById(R.id.rating);
        final TextView textStar = (TextView) findViewById(R.id.textStar);
        profile_nickname_tv = (TextView) this.findViewById(R.id.profile_nickname_tv);

        GlideUrl glideUrl = new GlideUrl(getString(R.string.URL) + "/company/image/" + getIntent().getExtras().getLong("company_no"), new LazyHeaders.Builder()
                .addHeader("Cookie", "PASSWORD")
                .build());

        Glide.with(getApplicationContext())
                .load(glideUrl)
                .bitmapTransform(new CropCircleTransformation(getApplicationContext()))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into((ImageView) findViewById(R.id.imageButton));

        client_guest.get(getString(R.string.URL) + "/company/join/category/" + getIntent().getExtras().getLong("company_no"), param, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {

                    company_name = response.getString("nick_name");
                    double rate = (Double) (Math.ceil(response.getDouble("score_average") * 2) / 2);
                    profile_nickname_tv.setText(company_name);
                    textStar.setText("" + rate);
                    ratingBar.setRating((float) response.getDouble("score_average"));
                    ratingBar.setIsIndicator(true);
                    isHistorySimple = response.getBoolean("history_simple");

                    jsonArray_category = response.getJSONArray("companyToCategory");

                    progressDialog.dismiss();

                } catch (JSONException e) {

                    e.printStackTrace();
                }
            }

            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (statusCode == 200) {
                    //  200  ==>  서버에 접근은 하였으나, 인증과정에서 Token 값이 바뀌었기 때문에 인증이 되지않는 경우 이다.
                    Toast.makeText(getApplicationContext(), "다른 디바이스에서 해당 계정으로 로그인 되었습니다.\n 본 디바이스는 로그아웃 됩니다.", Toast.LENGTH_LONG).show();
                    checkRegid.logout(ProfileActivity.this);
                } else {
                    Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.\n" +
                            "다시 시도해주세요." + responseString, Toast.LENGTH_LONG).show();
                    //  그외의 에러 코드가 들어온다.
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 상담하기 버튼
            case R.id.insert_chat_btn: {


                if (cookieStore.getCookies().size() > 0 && cookieStore.getCookies().get(0).getName().equals("UserKey")) {
                    insert_chat_btn.setClickable(false);
                    createChat();
                } else {
                    // 비로그인시 로그인화면으로 전환한다.
                    Intent myIntet = new Intent(getApplicationContext(), LoginActivity.class);
                    Toast.makeText(ProfileActivity.this, "비로그인 상태입니다. 로그인을 해주세요.", Toast.LENGTH_SHORT).show();
                    startActivity(myIntet);
                    overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                    finish();
                }
                break;
            }
            case R.id.favorite: {
                setFavorite(company_no, favoriteChecked);
                break;
            }
        }
    }


    // 옵션메뉴에 대한 이벤트 처리
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
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
        super.onBackPressed();

    }


    private void createChat() {


        ///// 상담하기 버튼에 대한 안내 다이얼로그 정의
        alertDialogBuilder = new AlertDialog.Builder(ProfileActivity.this);
        view = getLayoutInflater().inflate(R.layout.profile_announce_dialog, null);
        historyInsert_result = 0;

        if (category_no >= 0) {
            SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String time = sdfNow.format(new Date(System.currentTimeMillis()));

            Log.d("test", "category_no : " + category_no);


            //채팅창으로 진입
            final RequestParams params = new RequestParams();
            params.put("user_no", user_no);
            params.put("company_no", company_no);
            params.put("start_date", time);
            params.put("select_history", category_no);

            String URL = getString(R.string.URL) + "/history/status/" + (category_no == 1 ? "simple" : "general");

            client_user.post(URL, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    try {
                        insert_chat_btn.setClickable(true);

                        // 역술인이 상담 가능하지 않은 상태중에서
                        // Chat_possibillity가 False인 경우,
                        // Chat_switch와 상관없이 상담이 불가이다.
                        // 상담 중으로 토스트를 띄운다.

                        Log.d("test", "(int) response.getLong(\"result\") : " + (int) response.getLong("result"));
                        switch ((int) response.getLong("result")) {
                            case -1: {
                                historyInsertText = "역술인이 상담 중입니다.";
                                break;
                            }

                            // 역술인이 상담 가능하지 않은 상태중에서
                            // Chat_possibillity가 True인 경우,
                            // 상담 가능을 제외하고
                            // Chat_switch에 의존되므로
                            // 부재중으로 토스트를 띄운다.
                            case -10: {
                                Log.d("test", "adfasdfasdfasdfasdf : " + (int) response.getLong("result"));
                                historyInsertText = company_name + " 역술인이 부재 중입니다.";
                                break;
                            }
                            // 관리자에 의해, User가 BLOCK 상태인 경우
                            case -100: {
                                historyInsertText = "계정이 차단되어 로그아웃 됩니다.\n 고객센터에 문의해주세요.";
                                checkRegid.logout(ProfileActivity.this);
                                break;
                            }
                            // 결제 진행해야 하는 상태
                            case -1000: {
                                historyInsertText = getString(getResources().getIdentifier("profile_announce_" + category_no, "string", getPackageName()));
                                historyInsert_result = -1000;
                                break;
                            }
                            // general에서만 적용이 된다.
                            // 다른 카테고리의 채팅방이 존재할 때
                            // 해당 카테고리의 히스토리가 존재하지 않을 경우
                            case 0:
                            case -2:
                            case -3:
                            case -4: {

                                String category_name = getResources().getStringArray(R.array.profile_category_choice_items)[(-1 * (int) response.getLong("result"))];
                                historyInsertText = company_name + " 역술인과 " + category_name + " 상담을 진행하고 있습니다.\n" + category_name + " 상담 종류 후 이용해 주세요.";
                                break;
                            }
                            // 채팅방이 존재할 때
                            // 해당 카테고리의 히스토리가 존재할 경우  ->  해당 채팅방으로 입장
                            default: {

                                historyInsertText = "기존의 " + company_name + " 역술인과 종료되지 않은 상담이 있습니다.\n해당 채팅방으로 입장합시겠습니까?";
                                historyInsert_result = (int) response.getLong("result");

                                break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
//                        Toast.makeText(ProfileActivity.this,
//                                "insert Catched", Toast.LENGTH_LONG)
//                                .show();
                    }


                    alertDialogBuilder.setTitle(getResources().getStringArray(R.array.profile_category_choice_items)[category_no] + " 상담 안내");
                    alertDialogBuilder.setView(view);


                    alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            category_no = company_no_first_maintain == -1 ? category_no = -1 : category_no;
                        }
                    });

                    android.support.v7.widget.AppCompatTextView profile_announce_tv = (android.support.v7.widget.AppCompatTextView) view.findViewById(R.id.profile_announce_tv);
                    profile_announce_tv.setText(historyInsertText);

                    if (historyInsert_result != 0) {
                        alertDialogBuilder.setPositiveButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                dialog.dismiss();
                            }
                        });
                    }
                    alertDialogBuilder.setNegativeButton((historyInsert_result == -1000 ? "동의" : "확인"), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                Log.d("asd", "historyInsert_result : " + historyInsert_result);
                                if (historyInsert_result == -1000) {
                                    Intent intent = new Intent(ProfileActivity.this, (category_no == 1 ? SimplePrepareActivity.class : PayNormalActivity.class));
                                    intent.putExtra("user_no", user_no);
                                    intent.putExtra("sender_name", company_name);
                                    intent.putExtra("select_history", category_no);
                                    intent.putExtra("company_no", company_no);
                                    for (int i = 0; i < jsonArray_category.length(); i++) {
                                        if (category_no == (int) jsonArray_category.getJSONObject(i).getLong("category_code")) {
                                            intent.putExtra("unitprice", jsonArray_category.getJSONObject(i).getLong("unitprice"));
                                            break;
                                        }
                                    }
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                                    finish();
                                } else if (historyInsert_result > 0) {
                                    Log.d("error", "history Insert On success");

                                    Intent intent = new Intent(ProfileActivity.this, (category_no == 1 ? SimpleChatActivity.class : ChatActivity.class));
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    intent.putExtra("history_no", historyInsert_result);
                                    intent.putExtra("user_no", user_no);
                                    intent.putExtra("end_yn", false);
                                    intent.putExtra("sender_name", company_name);
                                    intent.putExtra("select_history", category_no);

                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                                    finish();
                                } else {
                                    // ERROR
                                }
                                dialog.cancel();
                                dialog.dismiss();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                                      String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers,
                            responseString, throwable);

                    if (statusCode == 200) {
                        //  200  ==>  서버에 접근은 하였으나, 인증과정에서 Token 값이 바뀌었기 때문에 인증이 되지않는 경우 이다.
                        Toast.makeText(getApplicationContext(), "다른 디바이스에서 해당 계정으로 로그인 되었습니다.\n 본 디바이스는 로그아웃 됩니다.", Toast.LENGTH_LONG).show();
                        checkRegid.logout(ProfileActivity.this);
                    } else {
                        Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.\n" +
                                "다시 시도해주세요." + responseString, Toast.LENGTH_LONG).show();
                        //  그외의 에러 코드가 들어온다.
                    }
                    insert_chat_btn.setClickable(true);
                }
            });


        } else if (category_no == -1) {

            alertDialogBuilder.setTitle("상담 선택");
            String[] company_categorys = new String[5];

            try {

                for (int i = 0; i < jsonArray_category.length(); i++) {
                    Log.d("ggg", "jsonArray_category.length()++ : " + (int) jsonArray_category.getJSONObject(i).getLong("category_code"));
                    company_categorys[i] = jsonArray_category.getJSONObject(i).getString("category_name");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            alertDialogBuilder.setItems(company_categorys, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        category_no = (int) jsonArray_category.getJSONObject(which).getLong("category_code");
                        insert_chat_btn.performClick();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            insert_chat_btn.setClickable(true);
            alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
            //  error
        }

    }

    public static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        Boolean isError;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
            this.isError = false;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
                isError = true;
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            Log.e("Error", "" + isError);
            if (!isError)
                bmImage.setImageBitmap(result);
            else
                bmImage.setImageResource(R.drawable.profile_default);
        }
    }


    // favorite 0 이 off 1이 on
    void setFavorite(long company_no, final boolean favorite) {

        String url;
        RequestParams params;

        if (!favorite) {
            url = getString(R.string.URL) + "/user/favorite/insert/";
        } else {
            url = getString(R.string.URL) + "/user/favorite/delete/";
        }

        url += (user_no + "/" + company_no);

        params = new RequestParams();

        Log.i("접속", "접속 전 " + user_no + " / " + company_no);
        client_user.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (favorite) {
                    favorite_iv.setImageResource(R.drawable.heart_off);
                    favoriteChecked = false;
                } else {
                    favorite_iv.setImageResource(R.drawable.heart_on);
                    favoriteChecked = true;
                }
                Log.i("접속", "성공");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.i("접속", "실패");
            }
        });
        Log.i("접속", "접속 후");
    }


    void getFavorite(long company_no, long user_no) {

        String url;
        RequestParams params;

        url = getString(R.string.URL) + "/company/favorite/user/";

        params = new RequestParams();

        params.add("company_no", String.valueOf(company_no));
        params.add("user_no", String.valueOf(user_no));

        client_user.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("result").equals("true")) {
                        try {
                            favorite_iv.setImageResource(R.drawable.heart_on);
                        } catch (OutOfMemoryError e) {

                        }

                        favoriteChecked = true;

                    } else {
                        favorite_iv.setImageResource(R.drawable.heart_off);
                        favoriteChecked = false;
                    }

                } catch (Exception e) {
                    Log.i("접속", "접속 성공 예외 발생");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable t) {
                if (statusCode == 200) {
                    //  200  ==>  서버에 접근은 하였으나, 인증과정에서 Token 값이 바뀌었기 때문에 인증이 되지않는 경우 이다.
                    Toast.makeText(getApplicationContext(), "다른 디바이스에서 해당 계정으로 로그인 되었습니다.\n 본 디바이스는 로그아웃 됩니다.", Toast.LENGTH_LONG).show();
                    checkRegid.logout(ProfileActivity.this);
                } else {
                    Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.\n" +
                            "다시 시도해주세요." + responseString, Toast.LENGTH_LONG).show();
                    //  그외의 에러 코드가 들어온다.
                }
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        insert_chat_btn.setVisibility(View.VISIBLE);

        if (cookieStore.getCookies().size() > 0 && cookieStore.getCookies().get(0).getName().equals("UserKey")) {
            checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), ProfileActivity.this);
            gcmEndMessage.gcmEndMessage(cookieStore.getCookies().get(0).getValue(), getSharedPreferences("Gcm_end_message", MODE_PRIVATE), ProfileActivity.this);

            insert_chat_btn.setText("상담하기");
        } else {
            insert_chat_btn.setText("로그인");
        }


    }

    // 새로운 intent를 푸쉬 받았을 떄
    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        final int broadcast_history_no = intent.getIntExtra("broadcast_history_no", 0);
        final String sender_name = intent.getStringExtra("sender_name");
        final int select_history = intent.getIntExtra("select_history", -2);

        gcmEndMessage.isSimpleHistory(cookieStore.getCookies().get(0).getValue(), broadcast_history_no, sender_name, select_history, ProfileActivity.this);
    }
}
