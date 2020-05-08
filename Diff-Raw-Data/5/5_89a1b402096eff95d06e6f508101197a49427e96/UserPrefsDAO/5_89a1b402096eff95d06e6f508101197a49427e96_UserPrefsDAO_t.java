 package com.ttu_swri.goggles.persistence.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Set;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 
 import com.google.gson.Gson;
 import com.ttu_swri.datamodel.ElementMate;
 import com.ttu_swri.datamodel.ElementMessage;
 import com.ttu_swri.datamodel.ElementPoi;
 import com.ttu_swri.goggles.persistence.ElementDAO;
 
 public class UserPrefsDAO implements ElementDAO {
 	
 	Context context;
 	
 	private static final String POI = "POI";
 	private static final String MESSAGE = "MESSAGE";
 	private static final String MATE = "MATE";
 	
 	private SharedPreferences poiPrefs;
 	private SharedPreferences msgPrefs;
 	private SharedPreferences matePrefs;
 	private Gson gson;
 	
 	@SuppressWarnings("unused")
 	private UserPrefsDAO(){
 		//unused
 	}
 	
 	public UserPrefsDAO(Context context){
 		this.context = context;
 		this.poiPrefs = this.context.getSharedPreferences(POI, Context.MODE_PRIVATE);
 		this.msgPrefs = this.context.getSharedPreferences(MESSAGE, Context.MODE_PRIVATE);
 		this.matePrefs = this.context.getSharedPreferences(MATE, Context.MODE_PRIVATE);
 		this.gson = new Gson();
 	}
 	
 	public void deleteAllUserPrefs(){
 		this.poiPrefs.edit().clear().commit();
 		this.msgPrefs.edit().clear().commit();
 		this.matePrefs.edit().clear().commit();
 	}
 	
 	public void deleteAllPOIs(){
 		this.poiPrefs.edit().clear().commit();
 	}
 	
 	public void deleteAllMessages(){
 		this.msgPrefs.edit().clear().commit();
 	}	
 	
 	public void deleteAllMates(){
 		this.matePrefs.edit().clear().commit();
 	}		
 
 	@Override
 	public String saveElementPoi(ElementPoi elementPoi) throws UnsupportedOperationException {
 		if(elementPoi == null || elementPoi.getId() == null || elementPoi.getId().trim().isEmpty()){
 			throw new UnsupportedOperationException("ElementPoi POI cannot be null and ElementPoi.id cannot be null or empty");
 		}
 		
 		Editor edit = this.poiPrefs.edit();
 		String serializedPoi = gson.toJson(elementPoi);		
 		edit.putString(elementPoi.getId(), serializedPoi);
 		edit.commit();
 		return elementPoi.getId();
 	}
 
 	@Override
 	public void saveElementPois(Collection<ElementPoi> elementPoiCollection)  throws UnsupportedOperationException {
 		if(elementPoiCollection == null || elementPoiCollection.isEmpty()){
 			throw new UnsupportedOperationException("ElementPoi collection is null or empty");
 		}
 		for(ElementPoi poi : elementPoiCollection){
 			saveElementPoi(poi);
 		}
 	}
 
 	@Override
 	public String saveElementMessage(ElementMessage elementMessage)  throws UnsupportedOperationException {
 		if(elementMessage == null || elementMessage.getId() == null || elementMessage.getId().trim().equals("")){
 			throw new UnsupportedOperationException("Null Message object or invalid Message ID");
 		}
 		
 		Editor edit = this.msgPrefs.edit();
 		String serializedMsg = gson.toJson(elementMessage);		
 		edit.putString(elementMessage.getId(), serializedMsg);
 		edit.commit();
 		return elementMessage.getId();
 	}
 
 	@Override
 	public void saveElementMessages(Collection<ElementMessage> elementMessages)  throws UnsupportedOperationException {
 		if(elementMessages == null || elementMessages.isEmpty()){
 			throw new UnsupportedOperationException("ElementMessage collection is null or empty");
 		}
 		for(ElementMessage msg : elementMessages){
 			saveElementMessage(msg);
 		}
 	}
 
 	@Override
 	public String saveElementMate(ElementMate elementMate)  throws UnsupportedOperationException {
 		if(elementMate == null || elementMate.getId() == null || elementMate.getId().trim().equals("")){
 			throw new UnsupportedOperationException("Null ElementMate object or invalid ElementMate ID");
 		}
 		
 		Editor edit = this.matePrefs.edit();
 		String serializedMsg = gson.toJson(elementMate);		
 		edit.putString(elementMate.getId(), serializedMsg);
 		edit.commit();
 		return elementMate.getId();
 	}
 
 	@Override
 	public void saveElementMates(Collection<ElementMate> elementMates)  throws UnsupportedOperationException {
 		if(elementMates == null || elementMates.isEmpty()){
 			throw new UnsupportedOperationException("ElementMate collection is null or empty");
 		}
 		for(ElementMate mate : elementMates){
 			saveElementMate(mate);
 		}
 
 	}
 
 	@Override
 	public ElementPoi getElementPoi(String id) {		
 		return gson.fromJson(poiPrefs.getString(id, null), ElementPoi.class);
 	}
 
 	@Override
 	public Collection<ElementPoi> getElementPois() {
 		ArrayList<ElementPoi> poiList = new ArrayList<ElementPoi>();
 		Set<String> keySet =  poiPrefs.getAll().keySet();
 		for(String key : keySet){		 
 		poiList.add(gson.fromJson(poiPrefs.getString(key, null), ElementPoi.class));
 		}
 		return poiList;
 	}
 
 	@Override
 	public ElementMessage getElementMessage(String id)  throws UnsupportedOperationException {
 		if(id == null || id.isEmpty()){
 			throw new UnsupportedOperationException("id cannot be null or empty");
 		}
 		return gson.fromJson(msgPrefs.getString(id, null), ElementMessage.class);
 	}
 
 	@Override
 	public Collection<ElementMessage> getElementMessages() {
 		ArrayList<ElementMessage> msgList = new ArrayList<ElementMessage>();
 		Set<String> keySet =  msgPrefs.getAll().keySet();
 		for(String key : keySet){		 
			msgList.add(gson.fromJson(msgPrefs.getString(key, null), ElementMessage.class));
 		}
 		return msgList;
 	}
 
 	@Override
 	public ElementMate getElementMate(String id)  throws UnsupportedOperationException {
 		if(id == null || id.isEmpty()){
 			throw new UnsupportedOperationException("id cannot be null or empty");
 		}		
 		return gson.fromJson(matePrefs.getString(id, null), ElementMate.class);
 	}
 
 	@Override
 	public Collection<ElementMate> getElementMates() {
 		ArrayList<ElementMate> mateList = new ArrayList<ElementMate>();
 		Set<String> keySet =  matePrefs.getAll().keySet();
 		for(String key : keySet){		 
			mateList.add(gson.fromJson(matePrefs.getString(key, null), ElementMate.class));
 		}
 		return mateList;
 	}
 
 }
