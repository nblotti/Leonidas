package ch.nblotti.leonidas.security.technical;

public class Message {

  private String entity_id;

  private ENTITY_ACTION entityAction;
  private MESSAGE_TYPE messageType;

  public Message() {
  }

  public Message(String entity_id, MESSAGE_TYPE messageType, ENTITY_ACTION entityAction) {
    this.entity_id = entity_id;
    this.entityAction = entityAction;
    this.messageType = messageType;
  }


  public String getEntity_id() {
    return entity_id;
  }

  public void setEntity_id(String entity_id) {
    this.entity_id = entity_id;
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

  public static enum ENTITY_ACTION {
    CREATE, DELETE, UPDATE, CANCEL
  }

  public static enum MESSAGE_TYPE {
    MARKET_ORDER, CASH_ENTRY, SECURITY_ENTRY, SECURITY_POSITION, CASH_POSITION
  }


}
