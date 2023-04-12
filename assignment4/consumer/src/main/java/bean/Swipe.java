package bean;

public class Swipe {

  public String getSwiper() {
    return swiper;
  }

  public void setSwiper(String swiper) {
    this.swiper = swiper;
  }

  public String getSwipee() {
    return swipee;
  }

  public void setSwipee(String swipee) {
    this.swipee = swipee;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public int getAttitude() {
    return attitude;
  }

  public void setAttitude(int attitude) {
    this.attitude = attitude;
  }

  public Swipe(String swiper, String swipee, String comment, int attitude) {
    this.swiper = swiper;
    this.swipee = swipee;
    this.comment = comment;
    this.attitude = attitude;
  }

  private String swiper;
  private String swipee;
  private String comment;

  private int attitude;


}
