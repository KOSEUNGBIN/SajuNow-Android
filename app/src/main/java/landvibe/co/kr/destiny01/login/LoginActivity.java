package landvibe.co.kr.destiny01.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import cz.msebera.android.httpclient.Header;
import landvibe.co.kr.destiny01.R;
import landvibe.co.kr.destiny01.UserEditActivity;
import landvibe.co.kr.destiny01.main.MainActivity;

public class LoginActivity extends Activity implements OnClickListener {
	TextView registerBtn;
	Button loginBtn;
	EditText idEdit;
	EditText passwordEdit;
	CallbackManager callbackManager;
	Button fb;
	Button naver;
	LoginButton loginButton;
	OAuthLoginButton mOAuthLoginButton;
	private final String LOGOUT = "LOGOUT";
	private final String DUPLICATED = "DUPLICATED";
	private final String BLOCK = "BLOCK";
	private TokenBroadcastReceiver tokenReceiverService;
	private String reg_id;
	private OAuthLogin mOAuthLoginModule;


	private String OAUTH_CLIENT_ID = "fe1rKOenYxJCFjro9ljY";
	private String OAUTH_CLIENT_SECRET = "VegLwon4V_";
	private String OAUTH_CLIENT_NAME = "사주나우";

	public void getInstanceIdToken(int user_no) {
		// Start IntentService to register this application with GCM.
		Intent intent = new Intent(this, RegistrationIntentService.class);
		intent.putExtra("user_no", user_no);
		startService(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FacebookSdk.sdkInitialize(getApplicationContext());
		setContentView(R.layout.activity_login);

		//Start our own service
		Intent intent = new Intent(this, PreRegistrationIntentService.class);
		startService(intent);

		registerBtn = (TextView) findViewById(R.id.login_signup_btn);
		loginBtn = (Button) findViewById(R.id.login_login_btn);

		idEdit = (EditText) findViewById(R.id.login_email_edit);
		passwordEdit = (EditText) findViewById(R.id.login_password_edit);

		loginBtn.setOnClickListener(this);
		registerBtn.setOnClickListener(this);

		tokenReceiverService = new TokenBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(PreRegistrationIntentService.MY_ACTION);
		registerReceiver(tokenReceiverService, intentFilter);


		callbackManager = CallbackManager.Factory.create();
		fb = (Button) findViewById(R.id.fb);


		loginButton = (LoginButton) findViewById(R.id.login_button);
		loginButton.setBackgroundResource(R.drawable.facebook_login);
		loginButton.setReadPermissions(Arrays.asList(
				"public_profile", "email"));


		loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess(final LoginResult loginResult) {
				Log.d("naver", loginResult.toString());
				GraphRequest request;
				request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {


					@Override
					public void onCompleted(final JSONObject user, GraphResponse response) {
						if (response.getError() != null) {
						} else {
							Log.i("TAG", "user: " + user.toString());
							Log.i("TAG", "AccessToken: " + loginResult.getAccessToken().getToken());


							String email = "";
							String name = "";
							String gender = "";
							String facebook_id = "";

							Log.d("naver", response.toString());
							AsyncHttpClient clientInside = new AsyncHttpClient();
							RequestParams param = new RequestParams();

							final PersistentCookieStore myCookieStore = new PersistentCookieStore(getApplicationContext());
							myCookieStore.clear();
							clientInside.setCookieStore(myCookieStore);
							clientInside.addHeader("Cookie", "PASSWORD");
							try {
								email = user.getString("email");
								name = user.getString("name");
								gender = user.getString("gender");
								facebook_id = user.getString("id");
							} catch (JSONException e) {
								e.printStackTrace();
							}
							final String email_ = email;
							final String name_ = name;
							final String facebook_id_ = facebook_id;
							param.put("facebook_id", facebook_id);
							clientInside.post(getString(R.string.URL) + "/user/login/facebook", param, new JsonHttpResponseHandler() {
								@Override
								public void onSuccess(int statusCode, Header[] headers,
								                      final JSONObject response_) {
									super.onSuccess(statusCode, headers, response_);

									try {
										if (response_.getInt("code") == 0) {
											Log.d("login", " toString : " + response_.toString());
											final int user_no = response_.getJSONObject("result").getInt("user_no");
											if (response_.getJSONObject("result").getString("user_reg_id").equals(LOGOUT)) {
												getInstanceIdToken(user_no);
												Log.d("login", "  myCookieStore.getCookies().size() : " + myCookieStore.getCookies().size());

												Toast.makeText(
														LoginActivity.this,
														name_ + "님 안녕하세요", Toast.LENGTH_SHORT)
														.show();

												Intent intent = new Intent(LoginActivity.this, MainActivity.class);
												startActivity(intent);
												intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
												overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
												finish();
											} else if (response_.getJSONObject("result").getString("user_reg_id").equals(DUPLICATED)) {
												Log.d("login", " 중복된 경우 : " + getApplicationContext().toString());
												AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
												builder.setTitle("주의")
														.setMessage("다른 디바이스에서 사용중인 계정입니다.\n기존의 디바이스를 로그아웃 시키고 이 디바이스를 사용하려면 확인을 눌러주세요")
														.setPositiveButton("확인", new DialogInterface.OnClickListener() {
															@Override
															public void onClick(DialogInterface dialog, int which) {
																Log.d("login", " setPositiveButton(\"확인\", : " + getApplicationContext().toString());
																duplicateLoginNaver(user_no, email_, false);
																dialog.dismiss();
															}
														})
														.setNegativeButton("취소", new DialogInterface.OnClickListener() {
															@Override
															public void onClick(DialogInterface dialog, int which) {
																Log.d("login", " setPositiveButton(\"취소\", : " + getApplicationContext().toString());
																dialog.dismiss();
															}
														});
												AlertDialog dialog = builder.create();
												dialog.show();

												Toast.makeText(
														LoginActivity.this,
														"현재 활성화된 다른 계정이 존재합니다.", Toast.LENGTH_SHORT)
														.show();
											} else if (response_.getJSONObject("result").getString("user_reg_id").equals(BLOCK)) {
												Toast.makeText(
														LoginActivity.this,
														"해당 계정은 차단된 상태입니다. 고객센터에 문의해주세요", Toast.LENGTH_SHORT)
														.show();
											}
										} else {
											AsyncHttpClient client = new AsyncHttpClient();
											client.addHeader("Cookie", "PASSWORD");
											RequestParams param = new RequestParams();
											param.put("email", email_);
											param.put("password", "-1");
											param.put("name", name_);
											param.put("facebook_id", facebook_id_);
											param.put("naver_id", "-1");
											client.post(getString(R.string.URL) + "/user/insert", param, new JsonHttpResponseHandler() {

												@Override
												public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
													super.onSuccess(statusCode, headers, response);
													try {
														if (response.getInt("code") == 0) {
															Log.d("login", " response : " + response.toString());
															int user_no_ = response.getInt("result");
															Log.d("login", " user_no_ : " + user_no_);
															if (user_no_ >= 0) {
																// 회원가입이 성공시 SharedPreference에 알림음 설정을 True로 설정
																SharedPreferences alarmSwitch = getSharedPreferences("alarmSwitch", MODE_PRIVATE);
																SharedPreferences.Editor editor = alarmSwitch.edit();

																editor.putBoolean("alarmSwitchCondition", true);
																editor.commit();
																duplicateLoginNaver(response.getInt("result"), email_, true);

															}


														} else {
															SharedPreferences alarmSwitch = getSharedPreferences("alarmSwitch", MODE_PRIVATE);
															SharedPreferences.Editor editor = alarmSwitch.edit();

															editor.putBoolean("alarmSwitchCondition", true);
															editor.commit();
															duplicateLoginNaver(response.getInt("result"), email_, true);
														}
													} catch (JSONException e) {
														e.printStackTrace();
													}
												}

												@Override
												public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
													Log.d("회원가입 중 오류", "");
													Toast.makeText(LoginActivity.this,
															"회원가입 중 오류가 발생하였습니다.", Toast.LENGTH_LONG)
															.show();
												}

											});


										}
									} catch (Exception e) {

										Log.d("login", e + " toString : " + response_.toString());
										Toast.makeText(LoginActivity.this,
												"아이디 또는 비밀번호를 다시 확인하세요.\n 등록되지 않은 아이디이거나, 비밀번호를 잘못 입력하셨습니다.", Toast.LENGTH_LONG)
												.show();
									}


								}

								@Override
								public void onFailure(int statusCode, Header[] headers,
								                      String responseString, Throwable throwable) {
									super.onFailure(statusCode, headers,
											responseString, throwable);
									Toast.makeText(LoginActivity.this,

											"아이디 또는 비밀번호를 다시 확인하세요.\n 등록되지 않은 아이디이거나, 비밀번호를 잘못 입력하셨습니다.",
											Toast.LENGTH_LONG).show();
								}
							});


							setResult(RESULT_OK);
						}
					}
				});


				Bundle parameters = new Bundle();
				parameters.putString("fields", "id,name,email,gender,birthday");
				request.setParameters(parameters);
				request.executeAsync();
			}

			@Override
			public void onCancel() {
				Log.d("naver", "cancel");
			}

			@Override
			public void onError(FacebookException error) {
				Log.d("naver", error.toString());
			}

		});


		mOAuthLoginModule = OAuthLogin.getInstance();
		mOAuthLoginModule.init(
				LoginActivity.this
				, OAUTH_CLIENT_ID
				, OAUTH_CLIENT_SECRET
				, OAUTH_CLIENT_NAME
		);
		naver = (Button) findViewById(R.id.naver);
		mOAuthLoginButton = (OAuthLoginButton) findViewById(R.id.buttonOAuthLoginImg);
		mOAuthLoginButton.setOAuthLoginHandler(mOAuthLoginHandler);


////////////////////////////////////////////////////////////////////////////////
		// idEdit.setText("1");
		// passwordEdit.setText("123");
////////////////////////////////////////////////////////////////////////////////


	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		callbackManager.onActivityResult(requestCode, resultCode, data);
	}


	private OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
		@Override
		public void run(boolean success) {


			if (success) {

				String at = mOAuthLoginModule.getAccessToken(getApplicationContext());
				Log.d("naver", at);
				final AsyncHttpClient client = new AsyncHttpClient();
				RequestParams params = new RequestParams();
				client.addHeader("Authorization", "Bearer " + at);

				client.get("https://openapi.naver.com/v1/nid/me", params, new JsonHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
						super.onSuccess(statusCode, headers, response);
						String email = "";
						String name = "";
						String gender = "";
						String naver_id = "";
						Log.d("naver", response.toString());
						AsyncHttpClient clientInside = new AsyncHttpClient();
						RequestParams param = new RequestParams();

						final PersistentCookieStore myCookieStore = new PersistentCookieStore(getApplicationContext());
						myCookieStore.clear();
						clientInside.setCookieStore(myCookieStore);
						clientInside.addHeader("Cookie", "PASSWORD");
						try {
							email = response.getJSONObject("response").getString("email");
							name = response.getJSONObject("response").getString("name");
							gender = response.getJSONObject("response").getString("gender");
							naver_id = response.getJSONObject("response").getString("id");
						} catch (JSONException e) {
							e.printStackTrace();
						}
						final String email_ = email;
						final String name_ = name;
						final String naver_id_ = naver_id;
						param.put("naver_id", naver_id);
						clientInside.post(getString(R.string.URL) + "/user/login/naver", param, new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(int statusCode, Header[] headers,
							                      final JSONObject response_) {
								super.onSuccess(statusCode, headers, response_);

								try {
									if (response_.getInt("code") == 0) {
										Log.d("login", " toString : " + response_.toString());
										final int user_no = response_.getJSONObject("result").getInt("user_no");
										if (response_.getJSONObject("result").getString("user_reg_id").equals(LOGOUT)) {
											getInstanceIdToken(user_no);

											Toast.makeText(
													LoginActivity.this,
													name_ + "님 안녕하세요", Toast.LENGTH_SHORT)
													.show();

											Intent intent = new Intent(LoginActivity.this, MainActivity.class);
											startActivity(intent);
											intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
											overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
											finish();
										} else if (response_.getJSONObject("result").getString("user_reg_id").equals(DUPLICATED)) {
											AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
											builder.setTitle("주의")
													.setMessage("다른 디바이스에서 사용중인 계정입니다.\n기존의 디바이스를 로그아웃 시키고 이 디바이스를 사용하려면 확인을 눌러주세요")
													.setPositiveButton("확인", new DialogInterface.OnClickListener() {
														@Override
														public void onClick(DialogInterface dialog, int which) {
															duplicateLoginNaver(user_no, email_, false);
															dialog.dismiss();
														}
													})
													.setNegativeButton("취소", new DialogInterface.OnClickListener() {
														@Override
														public void onClick(DialogInterface dialog, int which) {
															dialog.dismiss();
														}
													});
											AlertDialog dialog = builder.create();
											dialog.show();

											Toast.makeText(
													LoginActivity.this,
													"현재 활성화된 다른 계정이 존재합니다.", Toast.LENGTH_SHORT)
													.show();
										} else if (response_.getJSONObject("result").getString("user_reg_id").equals(BLOCK)) {
											Toast.makeText(
													LoginActivity.this,
													"해당 계정은 차단된 상태입니다. 고객센터에 문의해주세요", Toast.LENGTH_SHORT)
													.show();
										}
									} else {
										AsyncHttpClient client = new AsyncHttpClient();
										client.addHeader("Cookie", "PASSWORD");
										RequestParams param = new RequestParams();
										param.put("email", email_);
										param.put("password", "-1");
										param.put("name", name_);
										param.put("naver_id", naver_id_);
										param.put("facebook_id", "-1");
										client.post(getString(R.string.URL) + "/user/insert", param, new JsonHttpResponseHandler() {

											@Override
											public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
												super.onSuccess(statusCode, headers, response);
												try {
													Log.d("login", " response : " + response.toString());
													int user_no_ = response.getInt("result");
													Log.d("login", " user_no_ : " + user_no_);
													if (user_no_ >= 0) {
														// 회원가입이 성공시 SharedPreference에 알림음 설정을 True로 설정
														SharedPreferences alarmSwitch = getSharedPreferences("alarmSwitch", MODE_PRIVATE);
														SharedPreferences.Editor editor = alarmSwitch.edit();

														editor.putBoolean("alarmSwitchCondition", true);
														editor.commit();
														duplicateLoginNaver(response.getInt("result"), email_, true);


													}
												} catch (JSONException e) {
													e.printStackTrace();
												}
											}

											@Override
											public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
												Log.d("회원가입 중 오류", "");
												Toast.makeText(LoginActivity.this,
														"회원가입 중 오류가 발생하였습니다.", Toast.LENGTH_LONG)
														.show();
											}

										});


									}
								} catch (Exception e) {

									Log.d("login", e + " toString : " + response_.toString());
									Toast.makeText(LoginActivity.this,
											"아이디 또는 비밀번호를 다시 확인하세요.\n 등록되지 않은 아이디이거나, 비밀번호를 잘못 입력하셨습니다.", Toast.LENGTH_LONG)
											.show();
								}


							}

							@Override
							public void onFailure(int statusCode, Header[] headers,
							                      String responseString, Throwable throwable) {
								super.onFailure(statusCode, headers,
										responseString, throwable);
								Toast.makeText(LoginActivity.this,

										"아이디 또는 비밀번호를 다시 확인하세요.\n 등록되지 않은 아이디이거나, 비밀번호를 잘못 입력하셨습니다.",
										Toast.LENGTH_LONG).show();
							}
						});

					}

					@Override
					public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
						super.onFailure(statusCode, headers, responseString, throwable);
						Log.d("naver", "Fail : " + responseString);
					}
				});


			}
		}

		;
	};

	private class TokenBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub

			reg_id = arg1.getStringExtra("token");
			Log.d("error", "reg_id in onReceive : " + reg_id);

			unregisterReceiver(tokenReceiverService);


		}
	}

	@Override
	protected void onResume() {

		super.onResume();
	   /* final PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
	    if(myCookieStore.getCookies().size() > 0 && myCookieStore.getCookies().get(0).getName().equals("UserKey")){

            getInstanceIdToken(Integer.parseInt(new DeEncrypter().decrypt(URLDecoder.decode(myCookieStore.getCookies().get(0).getValue())).split("\\?")[0]));
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }
        else if(myCookieStore.getCookies().size() == 0)
        {
            return;
        }
        else
         idEdit.setText(myCookieStore.getCookies().get(0).getValue());*/
	}

	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
		super.onBackPressed();
	}

	@Override
	public void onClick(View v) {
		if (registerBtn == v) {
			Intent myIntent = new Intent(getApplicationContext(), SignUpActivity.class);
			startActivity(myIntent);
		}

		if (v == fb) {
			loginButton.performClick();
		}

		if (v == naver) {
			mOAuthLoginButton.performClick();
		}

		if (loginBtn == v) {
			if (passwordEdit.getText().equals("-1")) {
				Toast.makeText(LoginActivity.this,
						"아이디 또는 비밀번호를 다시 확인하세요.\n 등록되지 않은 아이디이거나, 비밀번호를 잘못 입력하셨습니다.", Toast.LENGTH_LONG)
						.show();
			} else {
				final AsyncHttpClient client = new AsyncHttpClient();
				RequestParams params = new RequestParams();
				final PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
				myCookieStore.clear();
				client.setCookieStore(myCookieStore);
				client.addHeader("Cookie", "PASSWORD");
				params.put("email", idEdit.getText());
				params.put("password", passwordEdit.getText());
				params.put("user_reg_id", reg_id);
				client.post(getString(R.string.URL) + "/user/login", params, new JsonHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers,
					                      JSONObject response) {
						super.onSuccess(statusCode, headers, response);
						try {
							Log.d("login", "user_reg_id : " + response.getString("user_reg_id"));
							if (response.getString("user_reg_id").equals(LOGOUT)) {

								getInstanceIdToken(response.getInt("user_no"));

								Toast.makeText(
										LoginActivity.this,
										response.getString("name")
												+ "님 안녕하세요", Toast.LENGTH_SHORT)
										.show();

								Intent intent = new Intent(LoginActivity.this, MainActivity.class);
								startActivity(intent);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
								overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
								finish();
							} else if (response.getString("user_reg_id").equals(DUPLICATED)) {
								AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
								builder.setTitle("주의")
										.setMessage("다른 디바이스에서 사용중인 계정입니다.\n기존의 디바이스를 로그아웃 시키고 이 디바이스를 사용하려면 확인을 눌러주세요")
										.setPositiveButton("확인", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												duplicateLogin(false);
												dialog.dismiss();
											}
										})
										.setNegativeButton("취소", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
											}
										});
								AlertDialog dialog = builder.create();
								dialog.show();

								Toast.makeText(
										LoginActivity.this,
										"현재 활성화된 다른 계정이 존재합니다.", Toast.LENGTH_SHORT)
										.show();
							} else if (response.getString("user_reg_id").equals(BLOCK)) {
								Toast.makeText(
										LoginActivity.this,
										"해당 계정은 차단된 상태입니다. 고객센터에 문의해주세요", Toast.LENGTH_SHORT)
										.show();
							}
						} catch (Exception e) {
							Toast.makeText(LoginActivity.this,
									"아이디 또는 비밀번호를 다시 확인하세요.\n 등록되지 않은 아이디이거나, 비밀번호를 잘못 입력하셨습니다.", Toast.LENGTH_LONG)
									.show();
						}


					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
					                      String responseString, Throwable throwable) {
						super.onFailure(statusCode, headers,
								responseString, throwable);
						Toast.makeText(LoginActivity.this,
								"아이디 또는 비밀번호를 다시 확인하세요.\n 등록되지 않은 아이디이거나, 비밀번호를 잘못 입력하셨습니다.",
								Toast.LENGTH_LONG).show();
					}
				});
			}
		}
	}


	public void duplicateLoginNaver(final int user_no, String email, final boolean isFromSignUp) {
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		final PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
		myCookieStore.clear();
		client.setCookieStore(myCookieStore);
		client.addHeader("Cookie", "PASSWORD");
		params.put("email", email);
		params.put("user_no", user_no);
		Log.d("naver", "user_no : " + user_no);

		client.post(getString(R.string.URL) + "/user/change/userkey/naver", params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				super.onSuccess(statusCode, headers, response);

				getInstanceIdToken(user_no);

				Intent intent;
				if (isFromSignUp) {
					intent = new Intent(LoginActivity.this, UserEditActivity.class);
					intent.putExtra("from", "signup");
				} else {
					intent = new Intent(LoginActivity.this, MainActivity.class);
				}
				startActivity(intent);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
				finish();


			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				super.onFailure(statusCode, headers, responseString, throwable);
				Toast.makeText(
						LoginActivity.this, "", Toast.LENGTH_SHORT)
						.show();
			}
		});
	}

	public void duplicateLogin(final boolean isFromSignUp) {
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		final PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
		myCookieStore.clear();
		client.setCookieStore(myCookieStore);
		client.addHeader("Cookie", "PASSWORD");
		params.put("email", idEdit.getText());
		params.put("password", passwordEdit.getText());

		client.post(getString(R.string.URL) + "/user/change/userkey", params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				super.onSuccess(statusCode, headers, response);

				try {
					getInstanceIdToken(response.getInt("user_no"));


					Intent intent;
					if (isFromSignUp) {
						intent = new Intent(LoginActivity.this, UserEditActivity.class);
						intent.putExtra("from", "signup");
					} else {
						intent = new Intent(LoginActivity.this, MainActivity.class);
					}
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(intent);
					overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
					finish();


				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				super.onFailure(statusCode, headers, responseString, throwable);
			}
		});
	}


}