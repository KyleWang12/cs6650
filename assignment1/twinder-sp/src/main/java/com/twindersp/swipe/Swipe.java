package com.twindersp.swipe;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class Swipe {

  @NotBlank(message = "swiper is mandatory")
  @Pattern(regexp = "^[0-9]+$", message = "swiper must be numeric")
  private String swiper;
  @NotBlank(message = "swipee is mandatory")
  @Pattern(regexp = "^[0-9]+$", message = "swipee must be numeric")
  private String swipee;
  @NotBlank(message = "comment is mandatory")
  private String comment;

  public Swipe(String swiper, String swipee, String comment) {
    this.swiper = swiper;
    this.swipee = swipee;
    this.comment = comment;
  }

  public Swipe() {
  }

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
}
