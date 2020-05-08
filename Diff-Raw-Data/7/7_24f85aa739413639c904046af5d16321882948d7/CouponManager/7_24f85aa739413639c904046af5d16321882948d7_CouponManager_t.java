 package net.lala.CouponCodes.api;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import net.lala.CouponCodes.CouponCodes;
 import net.lala.CouponCodes.api.coupon.Coupon;
 import net.lala.CouponCodes.api.coupon.EconomyCoupon;
 import net.lala.CouponCodes.api.coupon.ItemCoupon;
 import net.lala.CouponCodes.api.coupon.RankCoupon;
 import net.lala.CouponCodes.api.events.EventHandle;
 import net.lala.CouponCodes.sql.SQL;
import net.lala.CouponCodes.sql.options.MySQLOptions;
 
 /**
  * CouponManager.java - Allows other plugins to interact with coupons
  * @author mike101102
  */
 public class CouponManager {
 	
 	private CouponCodes plugin;
 	private SQL sql;
 	
 	public CouponManager(CouponCodes plugin, SQL sql) {
 		this.plugin = plugin;
 		this.sql = sql;
 	}
 	
 	public boolean addCouponToDatabase(Coupon coupon) throws SQLException {		
 		Connection con = sql.getConnection();
 		if (coupon instanceof ItemCoupon) {
 			ItemCoupon c = (ItemCoupon) coupon;
 			PreparedStatement p = con.prepareStatement("INSERT INTO couponcodes VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
 			p.setString(1, c.getName());
 			p.setString(2, c.getType());
 			p.setInt(3, c.getUseTimes());
 			p.setString(4, plugin.convertHashToString2(c.getUsedPlayers()));
 			p.setString(5, plugin.convertHashToString(c.getIDs()));
 			p.setInt(6, 0);
 			p.setString(7, "");
 			p.setInt(8, c.getTime());
 			p.addBatch();
 			con.setAutoCommit(false);
 			p.executeBatch();
 			con.setAutoCommit(true);
 		}
 		else if (coupon instanceof EconomyCoupon) {
 			EconomyCoupon c = (EconomyCoupon) coupon;
 			PreparedStatement p = con.prepareStatement("INSERT INTO couponcodes VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
 			p.setString(1, c.getName());
 			p.setString(2, c.getType());
 			p.setInt(3, c.getUseTimes());
 			p.setString(4, plugin.convertHashToString2(c.getUsedPlayers()));
 			p.setString(5, "");
 			p.setInt(6, c.getMoney());
 			p.setString(7, "");
 			p.setInt(8, c.getTime());
 			p.addBatch();
 			con.setAutoCommit(false);
 			p.executeBatch();
 			con.setAutoCommit(true);
 		}
 		else if (coupon instanceof RankCoupon) {
 			RankCoupon c = (RankCoupon) coupon;
 			PreparedStatement p = con.prepareStatement("INSERT INTO couponcodes VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
 			p.setString(1, c.getName());
 			p.setString(2, c.getType());
 			p.setInt(3, c.getUseTimes());
 			p.setString(4, plugin.convertHashToString2(c.getUsedPlayers()));
 			p.setString(5, "");
 			p.setInt(6, 0);
 			p.setString(7, c.getGroup());
 			p.setInt(8, c.getTime());
 			p.addBatch();
 			con.setAutoCommit(false);
 			p.executeBatch();
 			con.setAutoCommit(true);
 		}
 		EventHandle.callCouponAddToDatabaseEvent(coupon);
 		return true;
 	}
 	
 	public boolean removeCouponFromDatabase(Coupon coupon) throws SQLException {
 		if (!couponExists(coupon)) return false;		
 		sql.query("DELETE FROM couponcodes WHERE name='"+coupon.getName()+"'");
 		EventHandle.callCouponRemoveFromDatabaseEvent(coupon);
 		return true;
 	}
 	
 	public boolean couponExists(Coupon coupon) throws SQLException {
 		return getCoupons().contains(coupon.getName());
 	}
 	
 	public boolean couponExists(String name) throws SQLException {
 		return getCoupons().contains(name);
 	}
 	
 	public ArrayList<String> getCoupons() throws SQLException {
 		ArrayList<String> c = new ArrayList<String>();
 		try {
 			ResultSet rs = sql.query("SELECT name FROM couponcodes");
 			if (rs.equals(null)) return null;
 			while (rs.next())
 				c.add(rs.getString(1));
 			return c;
 		} catch (NullPointerException e) {
 			return c;
 		}
 	}
 	
 	public void updateCoupon(Coupon coupon) throws SQLException {
 		if (coupon instanceof ItemCoupon) {
 			ItemCoupon c = (ItemCoupon) coupon;
 			sql.query("UPDATE couponcodes SET ctype='"+c.getType()+"' WHERE name='"+c.getName()+"'");
 			sql.query("UPDATE couponcodes SET usetimes='"+c.getUseTimes()+"' WHERE name='"+c.getName()+"'");
 			sql.query("UPDATE couponcodes SET usedplayers='"+plugin.convertHashToString2(c.getUsedPlayers())+"' WHERE name='"+c.getName()+"'");
 			sql.query("UPDATE couponcodes SET ids='"+plugin.convertHashToString(c.getIDs())+"' WHERE name='"+c.getName()+"'");
 			sql.query("UPDATE couponcodes SET timeuse='"+c.getTime()+"' WHERE name='"+c.getName()+"'");
 		}
 		else if (coupon instanceof EconomyCoupon) {
 			EconomyCoupon c = (EconomyCoupon) coupon;
 			sql.query("UPDATE couponcodes SET ctype='"+c.getType()+"' WHERE name='"+c.getName()+"'");
 			sql.query("UPDATE couponcodes SET usetimes='"+c.getUseTimes()+"' WHERE name='"+c.getName()+"'");
 			sql.query("UPDATE couponcodes SET usedplayers='"+plugin.convertHashToString2(c.getUsedPlayers())+"' WHERE name='"+c.getName()+"'");
 			sql.query("UPDATE couponcodes SET money='"+c.getMoney()+"' WHERE name='"+c.getName()+"'");
 			sql.query("UPDATE couponcodes SET timeuse='"+c.getTime()+"' WHERE name='"+c.getName()+"'");
 		}
 		else if (coupon instanceof RankCoupon) {
 			RankCoupon c = (RankCoupon) coupon;
 			sql.query("UPDATE couponcodes SET ctype='"+c.getType()+"' WHERE name='"+c.getName()+"'");
 			sql.query("UPDATE couponcodes SET usetimes='"+c.getUseTimes()+"' WHERE name='"+c.getName()+"'");
 			sql.query("UPDATE couponcodes SET usedplayers='"+plugin.convertHashToString2(c.getUsedPlayers())+"' WHERE name='"+c.getName()+"'");
 			sql.query("UPDATE couponcodes SET groupname='"+c.getGroup()+"' WHERE name='"+c.getName()+"'");
 			sql.query("UPDATE couponcodes SET timeuse='"+c.getTime()+"' WHERE name='"+c.getName()+"'");
 		}
 	}
 	
 	public Coupon getCoupon(String coupon) throws SQLException {
 		if (!couponExists(coupon)) return null;
 		ResultSet rs1 = sql.query("SELECT * FROM couponcodes WHERE name='"+coupon+"'");
		if (sql.getDatabaseOptions() instanceof MySQLOptions) rs1.first();
 		int usetimes = rs1.getInt("usetimes");
 		int time = rs1.getInt("timeuse");
 		
 		HashMap<String, Boolean> usedplayers = plugin.convertStringToHash2(rs1.getString("usedplayers"));
 		ResultSet rs2 = sql.query("SELECT ctype FROM couponcodes WHERE name='"+coupon+"'");
		if (sql.getDatabaseOptions() instanceof MySQLOptions) rs2.first();
 		
 		if (rs2.getString(1).equalsIgnoreCase("Item")) {
 			return createNewItemCoupon(coupon, usetimes, time, plugin.convertStringToHash(rs1.getString("ids")), usedplayers);
 		}
 		
 		else if (rs2.getString(1).equalsIgnoreCase("Economy")) {
 			return createNewEconomyCoupon(coupon, usetimes, time, usedplayers, rs1.getInt("money"));
 		}
 		else if (rs2.getString(1).equalsIgnoreCase("Rank")) {
 			return createNewRankCoupon(coupon, rs1.getString("groupname"), usetimes, time, usedplayers);
 		} else {
 			return null;
 		}
 	}
 	
 	public ItemCoupon createNewItemCoupon(String name, int usetimes, int time, HashMap<Integer, Integer> ids, HashMap<String, Boolean> usedplayers) {
 		return new ItemCoupon(name, usetimes, time, usedplayers, ids);
 	}
 	
 	public EconomyCoupon createNewEconomyCoupon(String name, int usetimes, int time, HashMap<String, Boolean> usedplayers, int money) {
 		return new EconomyCoupon(name, usetimes, time, usedplayers, money);
 	}
 	
 	public RankCoupon createNewRankCoupon(String name, String group, int usetimes, int time, HashMap<String, Boolean> usedplayers) {
 		return new RankCoupon(name, group, usetimes, time, usedplayers);
 	}
 }
