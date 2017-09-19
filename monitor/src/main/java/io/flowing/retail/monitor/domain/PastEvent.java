package io.flowing.retail.monitor.domain;

public class PastEvent {

  private String transactionId;
  private String type;
  private String name;
  private String content;
  private String sender;

  public PastEvent() {    
  }
  
  public PastEvent(String type, String name, String transactionId, String sender, String eventContent) {
    this.transactionId = transactionId;
    this.type = type;
    this.name = name;
    this.sender = sender;
    this.content = eventContent;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public String getSender() {
    return sender;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

}
