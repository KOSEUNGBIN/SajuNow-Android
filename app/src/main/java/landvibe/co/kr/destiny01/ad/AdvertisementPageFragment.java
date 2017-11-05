package landvibe.co.kr.destiny01.ad;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import landvibe.co.kr.destiny01.R;

/**
 * Created by user on 2016-02-16.
 */
public class AdvertisementPageFragment extends Fragment {
    private int m_pageNumber;
    private ViewGroup m_rootView;

    public static Fragment create(int iPageNumber) {
        AdvertisementPageFragment fragment = new AdvertisementPageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("page", iPageNumber);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_pageNumber = getArguments().getInt("page");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        m_rootView = (ViewGroup) inflater.inflate(R.layout.advertisement_page_fragment, container, false);
        ImageView advertisementImageView = (ImageView) m_rootView.findViewById(R.id.advertisement_image_button);

        String url = getString(R.string.URL) + "/company/image/" + m_pageNumber;

        try {
            //임시 광고 이미지 적용
            if (m_pageNumber == 0) {
                Glide.with(getActivity())
                        .load("http://oursoccer.co.kr/study/advertisement/ad_1.jpg")
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(advertisementImageView);
            } else if (m_pageNumber == 1) {
                Glide.with(getActivity())
                        .load("http://oursoccer.co.kr/study/advertisement/ad_2.jpg")
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(advertisementImageView);
            } else if (m_pageNumber == 2) {
                Glide.with(getActivity())
                        .load("http://oursoccer.co.kr/study/advertisement/ad_3.jpg")
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(advertisementImageView);
            } else if (m_pageNumber == 3) {
                Glide.with(getActivity())
                        .load("http://oursoccer.co.kr/study/advertisement/ad_4.jpg")
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(advertisementImageView);
            } else if (m_pageNumber == 4) {
                Glide.with(getActivity())
                        .load("http://oursoccer.co.kr/study/advertisement/ad_5.jpg")
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(advertisementImageView);
            }
        }
        catch (OutOfMemoryError e)
        {

        }

        return m_rootView;
    }
}
