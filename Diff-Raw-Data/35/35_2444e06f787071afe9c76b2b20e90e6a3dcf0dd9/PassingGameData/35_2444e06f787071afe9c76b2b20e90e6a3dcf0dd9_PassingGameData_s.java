 package uk.co.richardgoater.stats.persistence;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 @SuppressWarnings("unused")
 @Entity
 @Table(name = "GAMEDATA_PASSING")
 public class PassingGameData extends GameDataWithLongest {
 	
 	private int att;
 	private int comp;
 	private int yds;
 	@Transient private double compPerCent;
 	private int td;
 	private int ints;
 	private int sck;
 	private int sackYds;
 	@Transient private double ydsPerAtt;
 	@Transient private double ydsPerComp;
 	
 	@Transient
 	public String[] getVisibleColumns() {
 		return new String[]{
 				"player.name", "player.number", "player.position",
 				"att",
 				"comp",
 				"yds",
 				"compPerCent",
 				"displayLong",
 				"td",
 				"ints",
 				"sck",
 				"sackYds",
 				"ydsPerAtt",
 				"ydsPerComp"
 		};
 	}
 	
 	public PassingGameData() {}
 	
 	public PassingGameData (Number playerid, Number att, Number comp, 
 			Number yds, Number longest, Number td, Number ints,	
 			Number sck,	Number sackYds)
 	{
 		this.playerid = playerid.intValue();
 		this.att = att.intValue();
 		this.comp = comp.intValue();
 		this.yds = yds.intValue();
 		this.longest = longest.intValue();
 		this.td = td.intValue();
 		this.ints = ints.intValue();
 		this.sck = sck.intValue();
 		this.sackYds = sackYds.intValue();
 	}
 	
 	@Column(name = "ATT")
 	public int getAtt() {
 		return att;
 	}
 
 	public void setAtt(int att) {
 		this.att = att;
 	}
 	
 	@Column(name = "COMP")
 	public int getComp() {
 		return comp;
 	}
 
 	public void setComp(int comp) {
 		this.comp = comp;
 	}
 	
 	@Column(name = "YDS")
 	public int getYds() {
 		return yds;
 	}
 
 	public void setYds(int yds) {
 		this.yds = yds;
 	}
 	
 	@Transient
 	public double getCompPerCent() {
 		if(att == 0) return 0;
		else
			return (comp / att) * 100;
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
 	
 	@Column(name = "INTS")
 	public int getInts() {
 		return ints;
 	}
 
 	public void setInts(int ints) {
 		this.ints = ints;
 	}
 	
 	@Column(name = "SCK")
 	public int getSck() {
 		return sck;
 	}
 
 	public void setSck(int sck) {
 		this.sck = sck;
 	}
 	
 	@Column(name = "SACKYDS")
 	public int getSackYds() {
 		return sackYds;
 	}
 
 	public void setSackYds(int sackYds) {
 		this.sackYds = sackYds;
 	}
 	
 	@Transient
 	public double getYdsPerComp() {
 		if(comp == 0) return 0;
 		else
			return yds / comp;
 	}
 
 }
