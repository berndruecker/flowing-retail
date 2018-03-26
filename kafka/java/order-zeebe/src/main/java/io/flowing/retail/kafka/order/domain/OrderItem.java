package io.flowing.retail.kafka.order.domain;

public class OrderItem {

  private String articleId;
  private int amount;
  
  public String getArticleId() {
    return articleId;
  }
  public OrderItem setArticleId(String articleId) {
    this.articleId = articleId;
    return this;
  }
  public int getAmount() {
    return amount;
  }
  public OrderItem setAmount(int amount) {
    this.amount = amount;
    return this;
  }
  @Override
  public String toString() {
    return "OrderItem [articleId=" + articleId + ", amount=" + amount + "]";
  }
}
