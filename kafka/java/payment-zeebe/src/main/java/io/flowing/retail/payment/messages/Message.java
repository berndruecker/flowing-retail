package io.flowing.retail.payment.messages;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;

public class Message<T> {

  // Cloud Events compliant 
  private String type;
  private String id = UUID.randomUUID().toString(); // unique id of this message
  private String source="Payment Zeebe";
  private Date time = new Date();
  private T data;
  private String datacontenttype="application/json";
  private String specversion="1.0";
  
  // Extension attributes
  private String traceid = UUID.randomUUID().toString(); // trace id, default: new unique
  private String correlationid; // id which can be used for correlation later if required
  private String group = "flowing-retail";
  
  public Message() {    
  }
  
  public Message(String type, T payload) {
    this.type = type;
    this.data = payload;
  }
  
  public Message(String type, String traceid, T payload) {
    this.type = type;
    this.traceid = traceid;
    this.data = payload;
  }

  @Override
  public String toString() {
    return "Message [type=" + type + ", id=" + id + ", time=" + time + ", data=" + data + ", correlationid=" + correlationid + ", traceid=" + traceid + "]";
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public String getTraceid() {
    return traceid;
  }

  public void setTraceid(String traceid) {
    this.traceid = traceid;
  }

  public String getCorrelationid() {
    return correlationid;
  }

  public Message<T> setCorrelationid(String correlationid) {
    this.correlationid = correlationid;
    return this;
  }

  public String getSource() {
    return source;
  }

  public String getDatacontenttype() {
    return datacontenttype;
  }

  public String getSpecversion() {
    return specversion;
  }

  public String getGroup() {
    return group;
  }

}
