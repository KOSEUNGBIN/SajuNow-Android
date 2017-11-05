package landvibe.co.kr.destiny01.profile;

/**
 * Created by jik on 2016-03-08.
 * 후기에 보여줄item - 닉네임, 작성시간, 후기내용, 별점
 */
public class profileReportListViewItem {
    private String name;
    private String date;
    private String context;
    private float rating_bar;

    public profileReportListViewItem(String Name, String Date, String Context, float rating)
    {
        name = Name;
        date = Date;
        context=Context;
        rating_bar = rating;
    }

    public float getRating_bar() {
        return rating_bar;
    }

    public void setRating_bar(float rating_bar) {
        this.rating_bar = rating_bar;
    }
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }


}
