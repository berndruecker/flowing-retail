package io.flowing.retail.shipping.port.message;

import java.util.Date;
import java.util.UUID;

public class Message<T> {

  private String messageType;
  private String id = UUID.randomUUID().toString(); // unique id of this message
  private String traceId = UUID.randomUUID().toString(); // trace id, default: new unique
  private String sender = "Shipping"; // for new messages
  private Date timestamp = new Date();

  private T payload;
  
  public Message() {    
  }
  
  public Message(String type, T payload) {
    this.messageType = type;
    this.payload = payload;
  }
  
  public Message(String type, String traceId, T payload) {
    this.messageType = type;
    this.traceId = traceId;
    this.payload = payload;
  }

  public String getMessageType() {
    return messageType;
  }

  public void setMessageType(String messageType) {
    this.messageType = messageType;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public T getPayload() {
    return payload;
  }

  public void setPayload(T payload) {
    this.payload = payload;
  }

  @Override
  public String toString() {
    return "Message [messageType=" + messageType + ", id=" + id + ", timestamp=" + timestamp + ", payload=" + payload + "]";
  }

  public String getTraceId() {
    return traceId;
  }

  public void setTraceId(String traceId) {
    this.traceId = traceId;
  }

  public String getSender() {
    return sender;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }
}
