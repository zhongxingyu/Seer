 package uk.co.richardgoater.stats.persistence;
 
 import javax.persistence.Column;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.Transient;
 
 @MappedSuperclass
 public abstract class GameData {
 	
 	private int gamedataid;
 	protected int playerid;
 	protected int weeknum;
 	protected int seasonid;
 	@Transient
 	private Player player;
 	
 	public GameData()
 	{	
 		super();
 	}
 	
 	public GameData(int playerid, int weeknum) {
 		super();
 		this.playerid = playerid;
 		this.weeknum = weeknum;		
 	}
 
 	@Id
 	@GeneratedValue
 	@Column(name = "GAMEDATAID")	
 	public int getGamedataid() {
 		return gamedataid;
 	}
 
 	public void setGamedataid(int gamedataid) {
 		this.gamedataid = gamedataid;
 	}
 	
 	@Column(name = "PLAYERID")	
 	public int getPlayerid() {
 		return playerid;
 	}
 
 	public void setPlayerid(int playerid) {
 		this.playerid = playerid;
 	}
 	
 	@Column(name = "WEEKNUM")
 	public int getWeeknum() {
 		return weeknum;
 	}
 
 	public void setWeeknum(int weeknum) {
 		this.weeknum = weeknum;
 	}
 
 	
 	@Transient
 	protected double divide(double numerator, double denominator) {
 		if(denominator == 0)
 			return 0.0;
 		else
			return numerator/denominator;
 	}
 	
 	@Transient
	public double round(double d, int c) {
 		int temp=(int)((d*Math.pow(10,c)));
 		return (((double)temp)/Math.pow(10,c));
 	}
 	
 	@Transient
 	public abstract String[] getVisibleColumns();
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + gamedataid;
 		result = prime * result + playerid;
 		result = prime * result + weeknum;
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		GameData other = (GameData) obj;
 		if (gamedataid != other.gamedataid)
 			return false;
 		if (playerid != other.playerid)
 			return false;
 		if (weeknum != other.weeknum)
 			return false;
 		return true;
 	}
 
 	@Transient
 	public void setPlayer(Player p) {
 		player = p;
 		playerid = p.getPlayerid();
 	}
 	
 	@Transient
 	public Player getPlayer() {
 		return player;
 	}
 	
 	@Column(name = "SEASONID")
 	public int getSeasonid() {
 		return seasonid;
 	}
 
 	public void setSeasonid(int seasonid) {
 		this.seasonid = seasonid;
 	}
 }
