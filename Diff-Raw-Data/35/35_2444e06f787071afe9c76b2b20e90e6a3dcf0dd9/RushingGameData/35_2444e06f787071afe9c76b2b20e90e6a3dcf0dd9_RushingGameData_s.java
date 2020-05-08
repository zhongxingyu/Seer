 package uk.co.richardgoater.stats.persistence;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 @SuppressWarnings("unused")
 @Entity
 @Table(name = "GAMEDATA_RUSHING")
 public class RushingGameData extends GameDataWithLongest {
 	
 	private int att;
 	private int yds;
 	@Transient private double ydsPerAtt;
 	private int td;
 	
 	@Transient
 	public String[] getVisibleColumns() {
 		return new String[]{
 				"player.name", "player.number", "player.position",
 				"att",
 				"yds",
 				"ydsPerAtt",
 				"displayLong",
 				"td"
 		};
 	}
 	
 	public RushingGameData() {}
 	
 	public RushingGameData( Number playerid, Number att,
 			Number yds, Number longest, Number td)
 	{
 		this.playerid = playerid.intValue();
 		this.att = att.intValue();
 		this.yds = yds.intValue();
 		this.longest = longest.intValue();
 		this.td = td.intValue();
 	}
 	
 	@Column(name = "ATT")
 	public int getAtt() {
 		return att;
 	}
 
 	public void setAtt(int att) {
 		this.att = att;
 	}
 	
 	@Column(name = "YDS")
 	public int getYds() {
 		return yds;
 	}
 
 	public void setYds(int yds) {
 		this.yds = yds;
 	}
 	
 	@Transient
 	public double getYdsPerAtt() {
 		if(att == 0) return 0;
 		else
			return yds / att;
 	}
 	
 	@Column(name = "TD")
 	public int getTd() {
 		return td;
 	}
 
 	public void setTd(int td) {
 		this.td = td;
 	}
 }
