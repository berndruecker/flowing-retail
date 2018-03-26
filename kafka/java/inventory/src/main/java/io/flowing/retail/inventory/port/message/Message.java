package io.flowing.retail.inventory.port.message;

import java.util.Date;
import java.util.UUID;

public class Message<T> {

  private String messageType;
  private String id = UUID.randomUUID().toString(); // unique id of this message
  private String traceId = UUID.randomUUID().toString(); // trace id, default: new unique
  private String sender = "Inventory";
  private Date timestamp = new Date();
  private String correlationId; // id which can be used for correlation later if required

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

  public Message<T> setMessageType(String messageType) {
    this.messageType = messageType;
    return this;
  }

  public String getId() {
    return id;
  }

  public Message<T> setId(String id) {
    this.id = id;
    return this;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public Message<T> setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  public T getPayload() {
    return payload;
  }

  public Message<T> setPayload(T payload) {
    this.payload = payload;
    return this;
  }

  public String getTraceId() {
    return traceId;
  }

  public Message<T> setTraceId(String traceId) {
    this.traceId = traceId;
    return this;    
  }

  public String getSender() {
    return sender;
  }

  public Message<T> setSender(String sender) {
    this.sender = sender;
    return this;    
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public Message<T> setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
    return this;
  }
  
  @Override
  public String toString() {
    return "Message [messageType=" + messageType + ", id=" + id + ", timestamp=" + timestamp + ", payload=" + payload + ", correlationId=" + correlationId + "]";
  }

}
