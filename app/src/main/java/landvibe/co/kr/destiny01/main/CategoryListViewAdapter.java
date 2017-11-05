package landvibe.co.kr.destiny01.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.loopj.android.http.PersistentCookieStore;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import landvibe.co.kr.destiny01.R;
import landvibe.co.kr.destiny01.profile.ProfileActivity;

/**
 * Created by user on 2016-01-30.
 */
public class CategoryListViewAdapter extends BaseAdapter {
    private LayoutInflater m_inflater;
    private ArrayList<CategoryListViewItem> m_categoryListViewItems;
    private int m_layout;
    private Context m_context;
    private int m_categoryNumber;
    private PersistentCookieStore m_cookieStore;

    public CategoryListViewAdapter(Context context, int layout, ArrayList<CategoryListViewItem> categoryListViewItems, int categoryNumber, PersistentCookieStore cookieStore) {
        m_context = context;
        m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        m_categoryListViewItems = categoryListViewItems;
        m_layout = layout;
////////////////////////////////////////////////////////////////////////////
        m_categoryNumber = categoryNumber; //카테고리 넘버 수정
////////////////////////////////////////////////////////////////////////////

        m_cookieStore = cookieStore;
    }

    public void replaceList(ArrayList<CategoryListViewItem> categoryListViewItem) {
        m_categoryListViewItems = categoryListViewItem;
    }

    @Override
    public int getCount() {
        return m_categoryListViewItems.size();
    }

    @Override
    public CategoryListViewItem getItem(int position) {
        return m_categoryListViewItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewholder;

        if (convertView == null) {
            convertView = m_inflater.inflate(m_layout, parent, false);

            viewholder = new ViewHolder();
            viewholder.m_imageView = (ImageView) convertView.findViewById(R.id.category_image);
            viewholder.m_nickName = (TextView) convertView.findViewById(R.id.category_nickname);
            viewholder.m_reviewCount = (TextView) convertView.findViewById(R.id.review_count);
            viewholder.m_scoreAverage = (ImageView) convertView.findViewById(R.id.category_score_average);
            viewholder.m_counsel = (TextView) convertView.findViewById(R.id.category_counsel);
            viewholder.m_categoryDetaill = (TextView) convertView.findViewById(R.id.category_detail);
            viewholder.m_favoriteCount =(TextView) convertView.findViewById(R.id.favorite_count);


            convertView.setTag(viewholder);
        } else {
            viewholder = (ViewHolder) convertView.getTag();
        }

        final CategoryListViewItem categoryListViewItem = m_categoryListViewItems.get(position);

        String url = m_context.getString(R.string.URL) + "/company/image/" + categoryListViewItem.getCompanyNo();

        GlideUrl glideUrl = new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Cookie", "PASSWORD")
                .build());

        Glide.with(m_context)
                .load(glideUrl)
                .bitmapTransform(new CropCircleTransformation(m_context))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(viewholder.m_imageView);

        viewholder.m_nickName.setText(categoryListViewItem.getNickName());

        if (categoryListViewItem.getScoreAverage() > 4.5)
            viewholder.m_scoreAverage.setImageResource(R.drawable.star_5_0);
        else if (categoryListViewItem.getScoreAverage() > 4)
            viewholder.m_scoreAverage.setImageResource(R.drawable.star_4_5);
        else if (categoryListViewItem.getScoreAverage() > 3.5)
            viewholder.m_scoreAverage.setImageResource(R.drawable.star_4_0);
        else if (categoryListViewItem.getScoreAverage() > 3)
            viewholder.m_scoreAverage.setImageResource(R.drawable.star_3_5);
        else if (categoryListViewItem.getScoreAverage() > 2.5)
            viewholder.m_scoreAverage.setImageResource(R.drawable.star_3_0);
        else if (categoryListViewItem.getScoreAverage() > 2)
            viewholder.m_scoreAverage.setImageResource(R.drawable.star_2_5);
        else if (categoryListViewItem.getScoreAverage() > 1.5)
            viewholder.m_scoreAverage.setImageResource(R.drawable.star_2_0);
        else if (categoryListViewItem.getScoreAverage() > 1.0)
            viewholder.m_scoreAverage.setImageResource(R.drawable.star_1_5);
        else if (categoryListViewItem.getScoreAverage() > 0.5)
            viewholder.m_scoreAverage.setImageResource(R.drawable.star_1_0);
        else
            viewholder.m_scoreAverage.setImageResource(R.drawable.star_0_5);

            if ((m_categoryNumber != 1 ? categoryListViewItem.isPossibility_result() : categoryListViewItem.isSimple_Possibility_result()) && !categoryListViewItem.getReg_id().equals("LOGOUT") && !categoryListViewItem.getReg_id().equals("BLOCK")) {
                viewholder.m_counsel.setText("상담가능");
                viewholder.m_counsel.setBackgroundColor(Color.parseColor("#153bde"));
            } else {
                if (!categoryListViewItem.isSwitch_possibility() || categoryListViewItem.getReg_id().equals("LOGOUT") || categoryListViewItem.getReg_id().equals("BLOCK")) {
                    viewholder.m_counsel.setText("부재중");
                    viewholder.m_counsel.setBackgroundColor(Color.parseColor("#999999"));
                } else {
                    viewholder.m_counsel.setText("상담중");
                    viewholder.m_counsel.setBackgroundColor(Color.parseColor("#b90e0e"));
                }
            }


        viewholder.m_favoriteCount.setText(categoryListViewItem.getFavoriteCount());
        viewholder.m_reviewCount.setText(categoryListViewItem.getReviewCount());
        viewholder.m_categoryDetaill.setText(categoryListViewItem.getCategoryDetaill());

        // 해당 view를 터치시, 쿠키가 존재할 때만 Profile 액티비로 넘긴다.
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(m_context, ProfileActivity.class);
                Activity activity = (Activity) m_context;
                intent.putExtra("company_no", categoryListViewItem.getCompanyNo());
                intent.putExtra("category_no", m_categoryNumber);
                m_context.startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);

            }
        });


        return convertView;

    }

    class ViewHolder {
        ImageView m_imageView;
        TextView m_nickName;
        TextView m_reviewCount;
        TextView m_favoriteCount;
        ImageView m_scoreAverage;
        TextView m_counsel;
        TextView m_categoryDetaill;
    }
}
