package io.flowing.retail.inventory.domain;

public class Item {

  private String articleId;
  private int amount;

  public Item setArticleId(String articleId) {
    this.articleId = articleId; 
    return this;
  }

  public int getAmount() {
    return amount;
  }

  public Item setAmount(int amount) {
    this.amount = amount;
    return this;
  }

  public String getArticleId() {
    return articleId;
  }
}
