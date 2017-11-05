package landvibe.co.kr.destiny01.main;

/**
 * Created by user on 2016-01-30.
 */
public class CategoryListViewItem {
    private long companyNo;
    private String reviewCount;
    private String favoriteCount;
    private String nickName;
    private float scoreAverage;
    private boolean simple_chat_possibility_result;
    private boolean chat_possibility;
    private boolean switch_possibility;
    private boolean possibility_result;
    private String categoryDetaill;
    private String reg_id;

    public CategoryListViewItem(long companyNo, String reviewCount, String favoriteCount, String nickName, float scoreAverage,boolean simple_chat_possibility_result, boolean chat_possibility, boolean switch_possibility, boolean possibility_result, String categoryDetaill, String reg_id) {
        this.companyNo = companyNo;
        this.reviewCount = reviewCount;
        this.favoriteCount = favoriteCount;
        this.nickName = nickName;
        this.scoreAverage = scoreAverage;
        this.simple_chat_possibility_result = simple_chat_possibility_result;
        this.chat_possibility = chat_possibility;
        this.switch_possibility = switch_possibility;
        this.possibility_result = possibility_result;
        this.categoryDetaill = categoryDetaill;
        this.reg_id = reg_id;
    }

    public long getCompanyNo() {
        return companyNo;
    }

    public void setCompanyNo(long companyNo) {
        this.companyNo = companyNo;
    }

    public String getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(String reviewCount) {
        this.reviewCount = reviewCount;
    }

    public String getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(String favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public float getScoreAverage() {
        return scoreAverage;
    }

    public void setScoreAverage(float scoreAverage) {
        this.scoreAverage = scoreAverage;
    }

    public boolean isSimple_Possibility_result() {
        return simple_chat_possibility_result;
    }

    public void setSimple_Possibility_result(boolean simple_chat_possibility_result) {
        this.simple_chat_possibility_result = simple_chat_possibility_result;
    }

    public boolean isChat_possibility() {
        return chat_possibility;
    }

    public void setChat_possibility(boolean chat_possibility) {
        this.chat_possibility = chat_possibility;
    }

    public boolean isSwitch_possibility() {
        return switch_possibility;
    }

    public void setSwitch_possibility(boolean switch_possibility) {
        this.switch_possibility = switch_possibility;
    }

    public boolean isPossibility_result() {
        return possibility_result;
    }

    public void setPossibility_result(boolean possibility_result) {
        this.possibility_result = possibility_result;
    }

    public String getCategoryDetaill() {
        return categoryDetaill;
    }

    public void setCategoryDetaill(String categoryDetaill) {
        this.categoryDetaill = categoryDetaill;
    }

    public String getReg_id() {
        return reg_id;
    }

    public void setReg_id(String reg_id) {
        this.reg_id = reg_id;
    }
}
