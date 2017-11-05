package landvibe.co.kr.destiny01.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import landvibe.co.kr.destiny01.R;
import landvibe.co.kr.destiny01.ReportActivity;

/**
 * Created by jik on 2016-03-11.
 * 해당 채팅방을 보고 있을 때 역술인이 상담종료를 눌렀을 때 후기 작성을 물어보는 dialog
 */
public class ChatEndDialog extends Activity implements View.OnClickListener {
    private Button chat_end_dialog_ok_btn;
    private Button chat_end_dialog_cancle_btn;
    private int historyno;
    private AsyncHttpClient client = new AsyncHttpClient();
    private RequestParams param = new RequestParams();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_end_dialog);

        Intent getintent = getIntent();

        historyno = getintent.getExtras().getInt("his_no");
        chat_end_dialog_ok_btn = (Button) findViewById(R.id.chat_end_dialog_ok_btn);
        chat_end_dialog_ok_btn.setOnClickListener(this);
        chat_end_dialog_cancle_btn = (Button) findViewById(R.id.chat_end_dialog_cancle_btn);
        chat_end_dialog_cancle_btn.setOnClickListener(this);

        Log.d("error", "chatenddialog " + historyno);
        //서버와 통신해 is_report_alarmed를 true로 update;
        // is_report_alarmed는 후기 작성여부를 물어봤나 판단
        client.post(getString(R.string.URL) + "/history/update/alarmed" + historyno, param, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                Toast.makeText(ChatEndDialog.this,
//                        "후기 확인이 업데이트 됬습니다", Toast.LENGTH_LONG)
//                        .show();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  String responseString, Throwable throwable) {
//                Toast.makeText(ChatEndDialog.this,
//                        "후기 업데이트중 에러에러!", Toast.LENGTH_LONG)
//                        .show();

            }
        });

    }

    @Override
    public void onClick(View v) {
        if (v == chat_end_dialog_cancle_btn) {        //취소버튼
            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
            intent.putExtra("history_no", historyno);
            intent.putExtra("end_yn", true);
            startActivity(intent);
            finish();
        } else        //확인버튼      후기작성으로 넘어간다
        {
            Intent okintent = new Intent(this, ReportActivity.class);
            okintent.putExtra("his_no", historyno);
            startActivity(okintent);
        }
    }
}


