 package com.askcs.ADK;
 
 import com.askcs.ADK.lib.SessionHandler;
 import com.askcs.ADK.lib.Settings;
 import com.askcs.webservices.AskPortType;
 import com.askcs.webservices.ResourceDataResponse;
 
 public class AskatarMessage {
 	
 	protected String messageUUID=null;
 	protected String message="";
 	protected String type="askMessage";
 	protected String sender="";
 	protected Boolean beenRead=false;
 	protected Integer prio=0;
 	
 	public AskatarMessage(){ //Needed for flexJson
 		
 	}
 	public AskatarMessage(String messageUUID,String message, String type, String sender, Integer prio){
 		this.messageUUID = messageUUID;
 		this.message = message;
 		this.type = type;
 		this.beenRead = false;
 		this.sender = sender;
 		this.prio = prio;
 	}	
 	public AskatarMessage(String uuid, SessionHandler sh){
 		ResourceDataResponse res;
 		Boolean error=false;
 		AskPortType askport = sh.getAskPort();
 		
 		res = askport.getResourceDataByTag(sh.getSessionId(), uuid, "message", "TXT");
 		if(res.getError()==0) this.message = res.getResult().getValue(); else error=true;
 		res = askport.getResourceDataByTag(sh.getSessionId(), uuid, "type", "TXT");
 		if(res.getError()==0) this.type = res.getResult().getValue(); else error=true;
 		res = askport.getResourceDataByTag(sh.getSessionId(), uuid, "beenRead", "TXT");
 		if(res.getError()==0 && res.getResult().getValue() != null) this.beenRead=true;
 		res = askport.getResourceDataByTag(sh.getSessionId(), uuid, "sender", "TXT");
 		if(res.getError()==0) this.sender = res.getResult().getValue(); else error=true;
 		res = askport.getResourceDataByTag(sh.getSessionId(), uuid, "prio", "TXT");
 		try {
 			if(res.getError()==0) this.prio = new Integer(res.getResult().getValue()); else error=true;
 		} catch (NumberFormatException e) { error=true; }
 
 		if (!error) this.messageUUID = uuid;
 	}
 	
 	private boolean setField(SessionHandler sh, String uuid, String tag,String value){
 		ResourceDataResponse res;
 		AskPortType askport = Settings.askport;
 		
 		res = askport.getResourceDataByTag(sh.getSessionId(), uuid, tag, "TXT");
 		if(res.getError()==0){
 			return askport.setResourceData(sh.getSessionId(), res.getResult().getResUUID(), uuid, res.getResult().getName(), res.getResult().getDescription(), res.getResult().getCategory(), res.getResult().getTag(),value).isResult();
 		} else return false;
 	}
 	private boolean createField(SessionHandler sh, String uuid, String tag,String value){
 		AskPortType askport = Settings.askport;
		if (askport.createResource(sh.getSessionId(), "", value, uuid, "field "+tag, "From appservices", "Node "+uuid, tag, value).getResult()!=null) return true;
 		return false;
 	}
 	private boolean deleteField(SessionHandler sh, String uuid, String tag){
 		AskPortType askport = Settings.askport;
 		ResourceDataResponse res;
 		res = askport.getResourceDataByTag(sh.getSessionId(), uuid, tag, "TXT");
 		if(res.getError()==0 && res.getResult().getResUUID()!=null){
 			return askport.removeResource(sh.getSessionId(), res.getResult().getResUUID()).isResult();	
 		} else {
 			return true; //already gone!
 		}
 		
 	}
 	private boolean setField(SessionHandler sh, String uuid, String tag,Boolean value){
		if (value) return createField(sh, uuid, tag,value.toString());
 		return deleteField(sh, uuid, tag);
 	}
 	private boolean setField(SessionHandler sh, String uuid, String tag,Integer value){
 		return setField(sh, uuid,tag,value.toString());
 	}
 		
 	public boolean update(SessionHandler sh){
 		Boolean noerror=true;
 		if (noerror) noerror=setField(sh,this.messageUUID,"message",this.message);
 		if (noerror) noerror=setField(sh,this.messageUUID,"type",this.type);
 		if (noerror) noerror=setField(sh,this.messageUUID,"sender",this.sender);
 		if (noerror) noerror=setField(sh,this.messageUUID,"prio",this.prio);
 		if (noerror) noerror=setField(sh,this.messageUUID,"beenRead",this.beenRead);
 		
 		return noerror;
 	}
 
 	public String getMessageUUID() {
 		return messageUUID;
 	}
 
 	public String getMessage() {
 		return message;
 	}
 
 	public String getType() {
 		return type;
 	}
 
 	public String getSender() {
 		return sender;
 	}
 
 	public Boolean getBeenRead() {
 		return beenRead;
 	}
 
 	public Integer getPrio() {
 		return prio.intValue();
 	}
 
 	public void setMessageUUID(String messageUUID) {
 		this.messageUUID = messageUUID;
 	}
 
 	public void setMessage(String message) {
 		this.message = message;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public void setSender(String sender) {
 		this.sender = sender;
 	}
 
 	public void setBeenRead(Boolean beenRead) {
 		this.beenRead = beenRead;
 	}
 
 	public void setPrio(Integer prio) {
 		this.prio = prio;
 	}
 }
