package io.flowing.retail.checkout.domain;

public class Item {

  private String articleId;
  private int amount;
  
  public String getArticleId() {
    return articleId;
  }
  public void setArticleId(String articleId) {
    this.articleId = articleId;
  }
  public int getAmount() {
    return amount;
  }
  public void setAmount(int amount) {
    this.amount = amount;
  }
  @Override
  public String toString() {
    return "Item [articleId=" + articleId + ", amount=" + amount + "]";
  }
}
