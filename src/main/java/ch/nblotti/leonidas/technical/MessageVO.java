package ch.nblotti.leonidas.technical;

public class MessageVO {

  private int accountID;
  private long orderID;

  private ENTITY_ACTION entityAction;
  private MESSAGE_TYPE messageType;

  public MessageVO() {
  }

  public MessageVO(long orderID, int accountID, MESSAGE_TYPE messageType, ENTITY_ACTION entityAction) {
    this.accountID = accountID;
    this.orderID = orderID;
    this.entityAction = entityAction;
    this.messageType = messageType;
  }


  public int getAccountID() {
    return accountID;
  }

  public void setAccountID(int accountID) {
    this.accountID = accountID;
  }

  public long getOrderID() {
    return orderID;
  }

  public void setOrderID(long orderID) {
    this.orderID = orderID;
  }


  public ENTITY_ACTION getEntityAction() {
    return entityAction;
  }

  public void setEntityAction(ENTITY_ACTION entityAction) {
    this.entityAction = entityAction;
  }

  public MESSAGE_TYPE getMessageType() {
    return messageType;
  }

  public void setMessageType(MESSAGE_TYPE messageType) {
    this.messageType = messageType;
  }

  public enum ENTITY_ACTION {
    CREATE, DELETE, UPDATE, CANCEL
  }

  public enum MESSAGE_TYPE {
    MARKET_ORDER, CASH_ENTRY, SECURITY_ENTRY, SECURITY_POSITION, CASH_POSITION
  }


}
