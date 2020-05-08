 package ch.hsr.sa.radiotour.domain;
 
 import java.util.Date;
 
 import com.j256.ormlite.field.DatabaseField;
 import com.j256.ormlite.table.DatabaseTable;
 
 @DatabaseTable(tableName = "Rider")
 public class BicycleRider {
 	@DatabaseField(id = true)
 	private int startNr;
 	@DatabaseField
 	private String name;
 	@DatabaseField
 	private String team;
 	@DatabaseField
 	private String teamShort;
 	@DatabaseField
 	private String country;
 	@DatabaseField
 	private String comment;
 	@DatabaseField
 	private Date official_time;
 	@DatabaseField
 	private Date birthday;
 	@DatabaseField
 	private Date official_deficit;
 	@DatabaseField
 	private Date virtual_deficit;
	@DatabaseField
 	private RiderState activ;
 	@DatabaseField
 	private String category;
 	@DatabaseField
 	private String note;
 	@DatabaseField
 	private String uci;
 	@DatabaseField
 	private int neo;
 	@DatabaseField
 	private String language;
 	@DatabaseField
 	private String url;
 
 	public BicycleRider() {
 
 	}
 
 	public BicycleRider(int startnr, String name, String teamName,
 			String teamShort, String country) {
 		super();
 		this.startNr = startnr;
 		this.name = name;
 		this.team = teamName;
 		this.teamShort = teamShort;
 		this.country = country;
 	}
 
 	public int getStartNr() {
 		return startNr;
 	}
 
 	public void setStartNr(int startNr) {
 		this.startNr = startNr;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getTeamName() {
 		return team;
 	}
 
 	public void setTeamName(String teamName) {
 		this.team = teamName;
 	}
 
 	public String getTeamShort() {
 		if (teamShort == null) {
 			return team;
 		}
 		return teamShort;
 	}
 
 	public void setTeamShort(String teamShort) {
 		this.teamShort = teamShort;
 	}
 
 	public String getCountry() {
 		return country;
 	}
 
 	public void setCountry(String country) {
 		this.country = country;
 	}
 
 	public String getTeam() {
 		return team;
 	}
 
 	public void setTeam(String team) {
 		this.team = team;
 	}
 
 	public String getComment() {
 		return comment;
 	}
 
 	public void setComment(String comment) {
 		this.comment = comment;
 	}
 
 	public Date getOfficial_time() {
 		return official_time;
 	}
 
 	public void setOfficial_time(Date official_time) {
 		this.official_time = official_time;
 	}
 
 	public Date getOfficial_deficit() {
 		return official_deficit;
 	}
 
 	public void setOfficial_deficit(Date official_deficit) {
 		this.official_deficit = official_deficit;
 	}
 
 	public Date getVirtual_deficit() {
 		return virtual_deficit;
 	}
 
 	public void setVirtual_deficit(Date virtual_deficit) {
 		this.virtual_deficit = virtual_deficit;
 	}
 
 	public RiderState getRiderState() {
 		return activ;
 	}
 
 	public void setRiderState(RiderState activ) {
 		this.activ = activ;
 	}
 
 	public String getCategory() {
 		return category;
 	}
 
 	public void setCategory(String category) {
 		this.category = category;
 	}
 
 	public String getUci() {
 		return uci;
 	}
 
 	public void setUci(String uci) {
 		this.uci = uci;
 	}
 
 	public int getNeo() {
 		return neo;
 	}
 
 	public void setNeo(int neo) {
 		this.neo = neo;
 	}
 
 	public String getLanguage() {
 		return language;
 	}
 
 	public void setLanguage(String language) {
 		this.language = language;
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	@Override
 	public String toString() {
 		return startNr + "   " + name;
 	}
 
 	public Date getBirthday() {
 		return birthday;
 	}
 
 	public void setBirthday(Date birthday) {
 		this.birthday = birthday;
 	}
 
 	public String getNote() {
 		return note;
 	}
 
 	public void setNote(String note) {
 		this.note = note;
 	}
 
 }
