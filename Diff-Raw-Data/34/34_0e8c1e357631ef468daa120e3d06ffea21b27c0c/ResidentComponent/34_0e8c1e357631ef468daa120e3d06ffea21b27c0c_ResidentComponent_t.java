 package net.croxis.plugins.civilmineation.components;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Entity;
import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 
 import com.avaje.ebean.validation.NotNull;
 
 @Entity()
 @Table(name = "civ_residents")
 public class ResidentComponent{
 	@Id
 	private int id;
	@OneToOne(fetch = FetchType.LAZY)
 	private Ent entityID;
 	private String name;
 	@ManyToOne
 	private CityComponent city;
 	@NotNull
 	private long lastOnline;
 	@NotNull
 	private long registered;
 	private boolean isNPC = false;
 	private String title;
 	private String surname;
     @Transient
     private String chatFormattedName;
     
     private String texturePackNode;
 	private String capeNode;    	
 	private String musicNode;
 	private boolean repeateMusic;
 	
 	private boolean mayor;
 	private boolean cityAssistant;
 	private boolean civAssistant;
 	
 	@ManyToMany(targetEntity=ResidentComponent.class)
 	@JoinTable(
 			name="civ_friends_map",
 			joinColumns=@JoinColumn(name="resident_id", referencedColumnName="ID"),
 			inverseJoinColumns=@JoinColumn(name="friend_id", referencedColumnName="ID")
 			)
     private List<ResidentComponent> friends = new ArrayList<ResidentComponent>();
 
 	public CityComponent getCity() {
 		return city;
 	}
 
 	public void setCity(CityComponent city) {
 		this.city = city;
 	}
 
 	public long getLastOnline() {
 		return lastOnline;
 	}
 
 	public void setLastOnline(long lastOnline) {
 		this.lastOnline = lastOnline;
 	}
 
 	public long getRegistered() {
 		return registered;
 	}
 
 	public void setRegistered(long registered) {
 		this.registered = registered;
 	}
 
 	public boolean isNPC() {
 		return isNPC;
 	}
 
 	public void setNPC(boolean isNPC) {
 		this.isNPC = isNPC;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getSurname() {
 		return surname;
 	}
 
 	public void setSurname(String surname) {
 		this.surname = surname;
 	}
 
 	public String getChatFormattedName() {
 		return chatFormattedName;
 	}
 
 	public void setChatFormattedName(String chatFormattedName) {
 		this.chatFormattedName = chatFormattedName;
 	}
 
 	public String getTexturePackNode() {
 		return texturePackNode;
 	}
 
 	public void setTexturePackNode(String texturePackNode) {
 		this.texturePackNode = texturePackNode;
 	}
 
 	public String getCapeNode() {
 		return capeNode;
 	}
 
 	public void setCapeNode(String capeNode) {
 		this.capeNode = capeNode;
 	}
 
 	public String getMusicNode() {
 		return musicNode;
 	}
 
 	public void setMusicNode(String musicNode) {
 		this.musicNode = musicNode;
 	}
 
 	public boolean isRepeateMusic() {
 		return repeateMusic;
 	}
 
 	public void setRepeateMusic(boolean repeateMusic) {
 		this.repeateMusic = repeateMusic;
 	}
 
 	public int getId() {
 		return id;
 	}
 	public void setId(int id) {
 		this.id = id;
 	}
 	public Ent getEntityID() {
 		return entityID;
 	}
 	public void setEntityID(Ent entityID) {
 		this.entityID = entityID;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public List<ResidentComponent> getFriends() {
 		return friends;
 	}
 
 	public void setFriends(List<ResidentComponent> friends) {
 		this.friends = friends;
 	}
 
 	public boolean isCityAssistant() {
 		return cityAssistant;
 	}
 
 	public void setCityAssistant(boolean cityAssistant) {
 		this.cityAssistant = cityAssistant;
 	}
 
 	public boolean isMayor() {
 		return mayor;
 	}
 
 	public void setMayor(boolean mayor) {
 		this.mayor = mayor;
 	}
 
 	public boolean isCivAssistant() {
 		return civAssistant;
 	}
 
 	public void setCivAssistant(boolean civAssistant) {
 		this.civAssistant = civAssistant;
 	}
 	
 
 }
