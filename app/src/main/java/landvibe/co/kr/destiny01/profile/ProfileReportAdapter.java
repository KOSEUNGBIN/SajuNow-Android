package landvibe.co.kr.destiny01.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import landvibe.co.kr.destiny01.R;

/**
 * Created by Administrator on 2016-02-23.
 */
public class ProfileReportAdapter extends BaseAdapter {
    private ArrayList<JSONObject> m_arraylistveiew;
    private LayoutInflater m_Inflater;
    private int m_layout;
    private Context m_context;
    profileReportListViewItem reportListViewItem;

    public ProfileReportAdapter(Context context, int layout, ArrayList<JSONObject> list)
    {
        //ProfileReportFragment에서 받은 list 설정

        m_arraylistveiew = list;
        m_Inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        m_layout = layout;
        m_context = context;

    }


    @Override
    public int getCount() {
        return m_arraylistveiew.size();
    }

    @Override
    public Object getItem(int position) {
        return m_arraylistveiew.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = m_Inflater.inflate(m_layout,parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.profile_review_add_nickname);
            holder.sentence = (TextView) convertView.findViewById(R.id.profile_review_add_context);
            holder.date = (TextView) convertView.findViewById(R.id.profile_review_date);
            holder.ratingbar = (RatingBar) convertView.findViewById(R.id.rating);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        try {
            float rate = m_arraylistveiew.get(position).getInt("score");
            reportListViewItem = new profileReportListViewItem(m_arraylistveiew.get(position).getString("user_nickname"),m_arraylistveiew.get(position).getString("register_date"),m_arraylistveiew.get(position).getString("comment"),rate);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Date to = new Date();
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            to = transFormat.parse(reportListViewItem.getDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //list의 내용을 holder에 지정
        holder.name.setText(reportListViewItem.getName());
        holder.sentence.setText(reportListViewItem.getContext());
        holder.date.setText( calculateTime(to));
        holder.ratingbar.setRating(reportListViewItem.getRating_bar());
        holder.ratingbar.setIsIndicator(true);
        return convertView;
    }
    static class ViewHolder
    {
        TextView name;
        TextView date;
        TextView sentence;
        RatingBar ratingbar;
    }

    private static class TIME_MAXIMUM
    {
        public static final int SEC = 60;
        public static final int MIN = 60;
        public static final int HOUR = 24;
        public static final int DAY = 30;
        public static final int MONTH = 12;
    }

    public String calculateTime(Date date)
    {

        long curTime = System.currentTimeMillis();
        long regTime = date.getTime();
        long diffTime = (curTime - regTime) / 1000;

        String msg = null;

        if (diffTime < TIME_MAXIMUM.SEC)
        {
            // sec
            msg = diffTime + "초전";
        }
        else if ((diffTime /= TIME_MAXIMUM.SEC) < TIME_MAXIMUM.MIN)
        {
            // min
            System.out.println(diffTime);

            msg = diffTime + "분전";
        }
        else if ((diffTime /= TIME_MAXIMUM.MIN) < TIME_MAXIMUM.HOUR)
        {
            // hour
            msg = (diffTime ) + "시간전";
        }
        else if ((diffTime /= TIME_MAXIMUM.HOUR) < TIME_MAXIMUM.DAY)
        {
            // day
            msg = (diffTime ) + "일전";
        }
        else if ((diffTime /= TIME_MAXIMUM.DAY) < TIME_MAXIMUM.MONTH)
        {
            // day
            msg = (diffTime ) + "달전";
        }
        else
        {
            msg = (diffTime) + "년전";
        }

        return msg;
    }


}
