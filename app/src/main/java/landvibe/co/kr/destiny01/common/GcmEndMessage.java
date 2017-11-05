package landvibe.co.kr.destiny01.common;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.List;

import cz.msebera.android.httpclient.Header;
import landvibe.co.kr.destiny01.ReportActivity;
import landvibe.co.kr.destiny01.chat.SimpleChatActivity;

public class GcmEndMessage {
    public boolean gcmEndMessage(String userkey, final SharedPreferences gcmEndMessage, final Context context) {

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> Info = am.getRunningTasks(1);
        ComponentName name = Info.get(0).topActivity;

        Log.d("gcmEndMessage", "-----------------------------------------------");
        Log.d("gcmEndMessage", "" + name.getClassName());
        if (gcmEndMessage.getBoolean("end_message", false)) {
            final int end_message_history_no = gcmEndMessage.getInt("end_message_history_no", 0);
            final String sender_name = gcmEndMessage.getString("end_message_name", null);
            final int select_history = gcmEndMessage.getInt("end_message_select_history", -2);
            Log.d("gcmEndMessage", "-----------------------------------------------");


            SharedPreferences.Editor editor = gcmEndMessage.edit();
            editor.clear();
            editor.commit();
            isSimpleHistory(userkey, end_message_history_no, sender_name, select_history, context);
            return true;
        } else
            return false;

    }

    public void isSimpleHistory(String userkey, final int end_message_history_no, final String sender_name, final int select_history, final Context context) {
        if (select_history == 1) {   //  간단 사주 종료 메시지
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("상담종료 확인 대화 상자")
                    .setMessage(sender_name + " 역술인이 간단 사주 상담의 답변을 보냈습니다.\n간단 사주 상담 페이지로 이동하시겠습니까?")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent reportIntent = new Intent(context, SimpleChatActivity.class);
                            reportIntent.putExtra("history_no", end_message_history_no);
                            reportIntent.putExtra("sender_name", sender_name);
                            reportIntent.putExtra("select_history", select_history);
                            reportIntent.putExtra("end_yn", true);
                            reportIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            context.startActivity(reportIntent);
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
        } else {  //  전문,해몽,.... 사주 종료 메시지
            EndDialog(userkey, end_message_history_no, sender_name, select_history, context);
        }
    }


    public void EndDialog(String userkey, final int end_message_history_no, final String sender_name, final int select_history, final Context context) {

        String[] convertSeleteHistory = {"전문", "간단", "관상손금", "해몽", "작명"};

        String isHistory = convertSeleteHistory[select_history];
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("상담종료 확인 대화 상자")
                .setMessage(sender_name + " 역술인이 " + isHistory + " 상담을 종료하였습니다.\n" + isHistory + " 상담 후기를 작성 하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent reportIntent = new Intent(context, ReportActivity.class);
                        reportIntent.putExtra("his_no", end_message_history_no);
                        reportIntent.putExtra("sender_name", sender_name);
                        reportIntent.putExtra("select_history", select_history);
                        reportIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        context.startActivity(reportIntent);
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        final AlertDialog dialog = builder.create();

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Cookie", userkey);
        RequestParams param_history_alarm = new RequestParams();
        param_history_alarm.put("history_no", end_message_history_no);
        param_history_alarm.put("is_report_alarmed", 1);
        client.post("http://{Server Domain}/history/update/alarmed", param_history_alarm, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                dialog.show();
//                Toast.makeText(context,
//                        "History is_report_alarmed Update Successed ", Toast.LENGTH_LONG)
//                        .show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                Toast.makeText(context,
//                        "History is_report_alarmed Update Fail ", Toast.LENGTH_LONG)
//                        .show();
            }
        });
    }


}
