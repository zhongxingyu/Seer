 package de.bdh.ks;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 
 public class KSHelper 
 {
 	Main m;
 	public ArrayList<World> worlds;
 	public KSHelper(Main main) {
 		
 		this.worlds = new ArrayList<World>();
 		String w = configManager.worlds;
 		if(w != null && w.length() > 0)
 		{
 			String[] tmpBoh = w.split(",");
 			for (String bl: tmpBoh) {
 				if(Bukkit.getWorld(bl) != null)
 				{
 					System.out.println("[KB] Enabled for World: "+bl);
 					this.worlds.add(Bukkit.getWorld(bl));
 				}
 			}
 		}
 		this.m = main;
 	}
 	
 	@SuppressWarnings("deprecation")
 	public int giveItem(Player p, ItemStack is)
 	{
 		PlayerInventory inv = p.getInventory();
 		int blockType = is.getTypeId();
 		int subType = is.getDurability();
 		int amount = is.getAmount();
 
 		int addedAmount = 0;
 		int stackSize = Material.getMaterial(blockType).getMaxStackSize();
 		int freeslot = -1;
 		while(amount - addedAmount > 0)
 		{
 			freeslot = -1;
 			if(configManager.brautec == 0)
 			{
 				for (int i =  0; i < 36; i++) 
 		    	{
 					ItemStack tmp = inv.getItem(i);
 		    		if(tmp != null)
 		    		{
 		    			if(tmp.getEnchantments().size() == 0 && tmp.getTypeId() == blockType && tmp.getDurability() == subType && tmp.getAmount() < tmp.getMaxStackSize())
 		    			{
 		    				freeslot = i;
 		    			}
 		    		} else
 		    			freeslot = i;
 		    	}	 
 			}
 			
 			if(freeslot == -1)
 				freeslot = inv.firstEmpty();
 			
 			if(freeslot == -1 || freeslot >= 36)
 			{
 				p.updateInventory();
 				return addedAmount;
 			}
 			
 			if (inv.getItem(freeslot) == null || inv.getItem(freeslot).getType() == Material.AIR)
 			{
 				if (amount - addedAmount >= stackSize)
 				{
 					inv.setItem(freeslot, new ItemStack(blockType, stackSize, (short) subType));
 					addedAmount += stackSize;
 				}
 				else
 				{
 					inv.setItem(freeslot, new ItemStack(blockType, amount - addedAmount, (short) subType));
 					addedAmount += amount - addedAmount;
 				}
 			}
 			else
 			{
 				ItemStack stack = inv.getItem(freeslot);
 				int diff = Math.min(stackSize - stack.getAmount(), amount - addedAmount);
 				addedAmount += diff;
 				stack.setAmount(stack.getAmount() + diff);
 			}
 		}
 		p.updateInventory();
 		return addedAmount;
 	}
 	
 	public int getDelivery(Player p)
 	{
 		int ret = 0;
 		try
 		{
 			ItemStack i = null;
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps,ps2 = null;
         	StringBuilder b = (new StringBuilder()).append("SELECT id,money,type,subtype,amount FROM ").append(configManager.SQLTable).append("_deliver WHERE player = ?");
     		ps = conn.prepareStatement(b.toString());
     		ps.setString(1, p.getName());
     		ResultSet rs = ps.executeQuery();
     		boolean remove = false;
     		boolean fisent = false;
     		int money = 0;
     		int blocks = 0;
     		while(rs.next())
     		{
     			remove = false;
     			if(rs.getInt("money") > 0)
     			{
     				System.out.println("[KS] Delivering to User "+p.getName()+ " Money: "+rs.getInt("money"));
     				Main.econ.depositPlayer(p.getName(), rs.getInt("money"));
     				++ret;
     				money += rs.getInt("money");
     				
     				remove = true;
     			} else if(rs.getInt("type") > 0 && rs.getInt("amount") > 0)
     			{
     				i = new ItemStack(rs.getInt("type"));
     				if(rs.getInt("subtype") != 0)
     					i.setDurability((short) rs.getInt("subtype"));
     				i.setAmount(rs.getInt("amount"));
     				
     				int given = this.giveItem(p, i);
     				blocks += given;
     				if(given == rs.getInt("amount"))
     				{
         				System.out.println("[KS] Delivering to User "+p.getName()+ " Item: "+rs.getInt("type")+":"+rs.getInt("subtype")+" Amount: "+rs.getInt("amount"));
     					remove = true;
     					++ret;
     				} else 
     				{
     					//Inventar voll - müssen die Delivery anpassen
         				if(given > 0)
         				{
         					//ein Teil wurde gegeben
             				System.out.println("[KS] Part-Delivering to User "+p.getName()+ " Item: "+rs.getInt("type")+":"+rs.getInt("subtype")+" Amount: "+given+"/"+rs.getInt("amount"));
             				b = (new StringBuilder()).append("UPDATE ").append(configManager.SQLTable).append("_deliver SET amount = ? WHERE id = ? LIMIT 1");
             				ps2 = conn.prepareStatement(b.toString());
             				ps2.setInt(1, rs.getInt("amount") - given);
             				ps2.setInt(2, rs.getInt("id"));
             				ps2.executeUpdate();
         				} 
         				
         				if(fisent == false)
     					{
     						fisent = true;
     						Main.lng.msg(p, "err_full_inv");
     					}
     				}
     			} else remove = true;
     			
     			if(remove == true)
     			{
     				b = (new StringBuilder()).append("DELETE FROM ").append(configManager.SQLTable).append("_deliver WHERE id = ? LIMIT 1");
     				ps2 = conn.prepareStatement(b.toString());
     				ps2.setInt(1, rs.getInt("id"));
     				ps2.executeUpdate();
     				
     			}
     		}
     		
     		if(money > 0)
     			Main.lng.msg(p, "suc_rec_money",new Object[]{money});
     		if(blocks > 0)
     			Main.lng.msg(p, "suc_rec_item",new Object[]{blocks});
     		
     		if(ps2 != null)
 				ps2.close();
     		
     		if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 			
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to get prices: ").append(e).toString());
 		}
 		return ret;
 	}
 
 	public boolean removeRequest(int id)
 	{
 		return this.removeRequest(id,null);
 	}
 	
 	public boolean removeRequest(int id,Player p)
 	{
 		try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps,ps2=null;
         	boolean ret = false;
         	StringBuilder b = (new StringBuilder()).append("SELECT sworld,price,amount,type,subtype,player FROM ").append(configManager.SQLTable).append("_request WHERE id = ? LIMIT 0,1");
         	ps = conn.prepareStatement(b.toString());
     		ps.setInt(1, id);
     		ResultSet rs = ps.executeQuery();
 			
     		while(rs.next())
     		{
     			if(p == null || p.hasPermission("ks.admin") || (p != null && p.getName().equalsIgnoreCase(rs.getString("player"))))
     			{
     				if(rs.getString("sworld").length() > 0)
     					updateSign(id,true,false);
     				ret = true;
 	    			System.out.println((new StringBuilder()).append("[KS] abort request with id: ").append(id).toString());
 	    			int money = rs.getInt("amount")*rs.getInt("price");
 	    			this.addDelivery(rs.getString("player"),money);
 	    			b = (new StringBuilder()).append("DELETE FROM ").append(configManager.SQLTable).append("_request WHERE id = ? LIMIT 1");
 	            	ps2 = conn.prepareStatement(b.toString());
 	            	ps2.setInt(1, id);
 	            	ps2.executeUpdate();
     			} 
             	
     		}
     		
     		if(ps2 != null)
 				ps2.close();
         	if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 			
 			return ret;
 		} catch(SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to remove request: ").append(e).toString());
 		}
 		return false;
 	}
 	
 	public boolean removeAuction(int id)
 	{
 		return this.removeAuction(id,null);
 	}
 	
 	public boolean removeAuction(int id,Player p)
 	{
 		try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps,ps2=null;
         	boolean ret = false;
         	StringBuilder b = (new StringBuilder()).append("SELECT sworld,amount,type,subtype,player FROM ").append(configManager.SQLTable).append("_offer WHERE id = ? LIMIT 0,1");
         	ps = conn.prepareStatement(b.toString());
     		ps.setInt(1, id);
     		ResultSet rs = ps.executeQuery();
 			
     		while(rs.next())
     		{
     			if(p == null || p.hasPermission("ks.admin") || (p != null && p.getName().equalsIgnoreCase(rs.getString("player"))))
     			{
     				if(rs.getString("sworld").length() > 0)
     					updateSign(id,true,true);
     				
     				ret = true;
 	    			System.out.println((new StringBuilder()).append("[KS] abort auction with id: ").append(id).toString());
 	    			ItemStack i = new ItemStack(rs.getInt("type"));
 	    			if(rs.getInt("subtype") != 0)
 	    				i.setDurability((short) rs.getInt("subtype"));
 	    			i.setAmount(rs.getInt("amount"));
 	    			
 	    			this.addDelivery(rs.getString("player"), i);
 	    			b = (new StringBuilder()).append("DELETE FROM ").append(configManager.SQLTable).append("_offer WHERE id = ? LIMIT 1");
 	            	ps2 = conn.prepareStatement(b.toString());
 	            	ps2.setInt(1, id);
 	            	ps2.executeUpdate();
     			} 
             	
     		}
     		
     		if(ps2 != null)
 				ps2.close();
         	if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 			
 			return ret;
 		} catch(SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to remove auction: ").append(e).toString());
 		}
 		return false;
 	}
 	
 	//Hat er was im AH?
 	public int hasDelivery(Player p)
 	{
 		try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	StringBuilder b = (new StringBuilder()).append("SELECT COUNT(*) as c FROM ").append(configManager.SQLTable).append("_deliver WHERE player = ?");
     		ps = conn.prepareStatement(b.toString());
     		ps.setString(1, p.getName());
     		ResultSet rs = ps.executeQuery();
     		int am = 0;	
     		while(rs.next())
     		{
     			am = rs.getInt("c");
     		}
     		
     		if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 			
 			return am;
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to get prices: ").append(e).toString());
 		}
 		return 0;
 	}
 	
 	public int getRequestAmountFromPlayer(String p)
 	{
 		return this.getRequestAmountFromPlayer(p,null);
 	}
 	
 	public int getRequestAmountFromPlayer(String p, ItemStack i)
 	{
 		int ret = 0;
 		try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	StringBuilder b = (new StringBuilder()).append("SELECT COUNT(*) as c FROM ").append(configManager.SQLTable).append("_request WHERE player = ?");
         	if(i != null)
         		b.append(" AND type = ? AND subtype = ?");
         	
         	String strg = b.toString();
     		ps = conn.prepareStatement(strg);
     		ps.setString(1, p);
     		if(i != null)
     		{
     			ps.setInt(2, i.getTypeId());
     			ps.setInt(3, i.getDurability());
     		}
     		
     		ResultSet rs = ps.executeQuery();
     			
     		while(rs.next())
     		{
     			ret = rs.getInt("c");
     		}
     		
     		if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to get request amount: ").append(e).toString());
 			ret = -1;
 		}
 		
 		return ret;
 	}
 
 	
 	public int getOfferAmountFromPlayer(String p)
 	{
 		return this.getOfferAmountFromPlayer(p,null);
 	}
 	
 	public int getOfferAmountFromPlayer(String p, ItemStack i)
 	{
 		int ret = 0;
 		try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	StringBuilder b = (new StringBuilder()).append("SELECT COUNT(*) as c FROM ").append(configManager.SQLTable).append("_offer WHERE player = ?");
         	if(i != null)
         		b.append(" AND type = ? AND subtype = ?");
         	
         	String strg = b.toString();
     		ps = conn.prepareStatement(strg);
     		ps.setString(1, p);
     		if(i != null)
     		{
     			ps.setInt(2, i.getTypeId());
     			ps.setInt(3, i.getDurability());
     		}
     		
     		ResultSet rs = ps.executeQuery();
     			
     		while(rs.next())
     		{
     			ret = rs.getInt("c");
     		}
     		
     		if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to get offer amount: ").append(e).toString());
 			ret = -1;
 		}
 		
 		return ret;
 	}
 	
 	
 	public Map<Integer,KSOffer> getRequestsFromPlayer(String p,int am, int begin)
 	{
 		return this.getRequestsFromPlayer(p, null,am,begin);
 	}
 	
 	public Map<Integer,KSOffer> getRequestsFromPlayer(String p, ItemStack i, int am, int begin)
 	{
 		HashMap<Integer,KSOffer> hm = new HashMap<Integer,KSOffer>();
 		try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	StringBuilder b = (new StringBuilder()).append("SELECT id,type,subtype,amount,price FROM ").append(configManager.SQLTable).append("_request WHERE player = ? ");
         	if(i != null)
         		b.append("AND type = ? AND subtype = ? ");
         	
         	b.append("LIMIT ").append(begin).append(",").append(am);
         	String strg = b.toString();
     		ps = conn.prepareStatement(strg);
     		ps.setString(1, p);
     		if(i != null)
     		{
     			ps.setInt(2, i.getTypeId());
     			ps.setInt(3, i.getDurability());
     		}
     		
     		ResultSet rs = ps.executeQuery();
     		
     		KSOffer f;
     		ItemStack is;
     		while(rs.next())
     		{
     			is = new ItemStack(rs.getInt("type"));
     			if(rs.getInt("subtype") != 0)
     				is.setDurability((short) rs.getInt("subtype"));
     			
     			f = new KSOffer(is,p,rs.getInt("price"), rs.getInt("amount"));
     			hm.put(rs.getInt("id"), f);
     		}
     		
     		if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to get offers from player: ").append(e).toString());
 		}
 		return hm;
 	}
 	
 	public Map<Integer,KSOffer> getOffersFromPlayer(String p,int am, int begin)
 	{
 		return this.getOffersFromPlayer(p, null,am,begin);
 	}
 	
 	public Map<Integer,KSOffer> getOffersFromPlayer(String p, ItemStack i, int am, int begin)
 	{
 		HashMap<Integer,KSOffer> hm = new HashMap<Integer,KSOffer>();
 		try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	StringBuilder b = (new StringBuilder()).append("SELECT id,type,subtype,amount,price FROM ").append(configManager.SQLTable).append("_offer WHERE player = ? ");
         	if(i != null)
         		b.append("AND type = ? AND subtype = ? ");
         	
         	b.append("LIMIT ").append(begin).append(",").append(am);
         	String strg = b.toString();
     		ps = conn.prepareStatement(strg);
     		ps.setString(1, p);
     		if(i != null)
     		{
     			ps.setInt(2, i.getTypeId());
     			ps.setInt(3, i.getDurability());
     		}
     		
     		ResultSet rs = ps.executeQuery();
     		
     		KSOffer f;
     		ItemStack is;
     		while(rs.next())
     		{
     			is = new ItemStack(rs.getInt("type"));
     			if(rs.getInt("subtype") != 0)
     				is.setDurability((short) rs.getInt("subtype"));
     			
     			f = new KSOffer(is,p,rs.getInt("price"), rs.getInt("amount"));
     			hm.put(rs.getInt("id"), f);
     		}
     		
     		if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to get offers from player: ").append(e).toString());
 		}
 		return hm;
 	}
 	
 	public int getOfferAmount()
 	{
 		int ret = 0;
 		try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	StringBuilder b = (new StringBuilder()).append("SELECT COUNT(A.d) as c FROM (SELECT 1 as d FROM ").append(configManager.SQLTable).append("_offer GROUP BY type,subtype HAVING d) AS A");
         	
         	String strg = b.toString();
     		ps = conn.prepareStatement(strg);
     		
     		ResultSet rs = ps.executeQuery();
     			
     		while(rs.next())
     		{
     			ret = rs.getInt("c");
     		}
     		
     		if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to get global offer amount: ").append(e).toString());
 			ret = -1;
 		}
 		
 		return ret;
 	}
 	
 	public HashMap<String,Long> durchDate = new HashMap<String,Long>();
 	public HashMap<String,Double> durchWert = new HashMap<String,Double>();
 	public double getDurchschnitsspreis(ItemStack i, int zeit)
 	{
 		int blockid = i.getTypeId();
 		int subid = i.getDurability();
 		
 		double ret = 0;
 		boolean redo = true;
 		String block = KrimBlockName.getNameById(blockid, subid);
 		if(this.durchWert.get(block) != null && this.durchDate.get(block) != null)
 		{
 			long timer = this.durchDate.get(block);
 			ret = this.durchWert.get(block);
 			//Gecached
 			
 			if (Math.abs(System.currentTimeMillis() - timer) < 1000*60*60*3)
 				redo = false;
 		}
 		
 		if(redo == true || ret == 0)
 		try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	StringBuilder b = (new StringBuilder()).append("SELECT SUM(amount) as a, SUM(price) as p FROM ").append(configManager.SQLTable).append("_transaction WHERE type=? AND subtype=? AND zeit >= CURRENT_DATE() - INTERVAL ? DAY");
         	
         	String strg = b.toString();
     		ps = conn.prepareStatement(strg);
     		ps.setInt(1, blockid);
     		ps.setInt(2, subid);
     		ps.setInt(3, zeit);
     		ResultSet rs = ps.executeQuery();
     			
     		while(rs.next())
     		{	
     			if(rs.getInt("a") > 0)
     				ret = (rs.getInt("p") * 1.0) / (rs.getInt("a") * 1.0);
     		}
     		
     		this.durchDate.put(block, System.currentTimeMillis());
     		this.durchWert.put(block,ret);
     		
     		if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to get normal price: ").append(e).toString());
 			ret = -1;
 		}
 		
 		return ret;
 	}
 	
 	public Map<Integer,KSOffer> getOffers(int orderby, int am, int begin)
 	{
 		HashMap<Integer,KSOffer> hm = new HashMap<Integer,KSOffer>();
 		try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	StringBuilder b = (new StringBuilder()).append("SELECT id,type,subtype,amount,price FROM ").append(configManager.SQLTable).append("_offer GROUP BY type,subtype ORDER BY ? ");
 
         	
         	b.append("LIMIT ").append(begin).append(",").append(am);
         	String strg = b.toString();
     		ps = conn.prepareStatement(strg);
     		if(orderby == 1)	
 	    		ps.setString(1, "price ASC");
     		else
     			ps.setString(1, "zeit DESC");
     		
     		ResultSet rs = ps.executeQuery();
     		
     		KSOffer f;
     		ItemStack is;
     		while(rs.next())
     		{
     			is = new ItemStack(rs.getInt("type"));
     			if(rs.getInt("subtype") != 0)
     				is.setDurability((short) rs.getInt("subtype"));
     			
     			f = new KSOffer(is,null,rs.getInt("price"), rs.getInt("amount"));
     			hm.put(rs.getInt("id"), f);
     		}
     		
     		if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to get offers: ").append(e).toString());
 		}
 		return hm;
 	}
 	
 	public int getRequestsAmount()
 	{
 		int ret = 0;
 		try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	StringBuilder b = (new StringBuilder()).append("SELECT COUNT(A.d) as c FROM (SELECT 1 as d FROM ").append(configManager.SQLTable).append("_request GROUP BY type,subtype HAVING d) AS A");
         	
         	String strg = b.toString();
     		ps = conn.prepareStatement(strg);
     		
     		ResultSet rs = ps.executeQuery();
     			
     		while(rs.next())
     		{
     			ret = rs.getInt("c");
     		}
     		
     		if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to get global request amount: ").append(e).toString());
 			ret = -1;
 		}
 		
 		return ret;
 	}
 	
 	public Map<Integer,KSOffer> getRequests(int orderby, int am, int begin)
 	{
 		HashMap<Integer,KSOffer> hm = new HashMap<Integer,KSOffer>();
 		try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	StringBuilder b = (new StringBuilder()).append("SELECT id,type,subtype,amount,max(price) as price FROM ").append(configManager.SQLTable).append("_request GROUP BY type,subtype ORDER BY ? ");
 
         	
         	b.append("LIMIT ").append(begin).append(",").append(am);
         	String strg = b.toString();
     		ps = conn.prepareStatement(strg);
     		if(orderby == 1)	
 	    		ps.setString(1, "price DESC");
     		else
     			ps.setString(1, "zeit DESC");
     		
     		ResultSet rs = ps.executeQuery();
     		
     		KSOffer f;
     		ItemStack is;
     		while(rs.next())
     		{
     			is = new ItemStack(rs.getInt("type"));
     			if(rs.getInt("subtype") != 0)
     				is.setDurability((short) rs.getInt("subtype"));
     			
     			f = new KSOffer(is,null,rs.getInt("price"), rs.getInt("amount"));
     			hm.put(rs.getInt("id"), f);
     		}
     		
     		if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to get requests: ").append(e).toString());
 		}
 		return hm;
 	}
 	
 	public Map<Integer,KSOffer> getTransactionsByPlayer(String p,boolean from, int am, int begin)
 	{
 		return this.getTransactionsByPlayer(p,null,from,am,begin);
 	}
 	
 	//Name, Itemlimiter, FROMPLAYER/TOPLAYER, Einträge, Einträge beginnend bei
 	public Map<Integer,KSOffer> getTransactionsByPlayer(String p, ItemStack i, boolean from, int am, int begin)
 	{
 		HashMap<Integer,KSOffer> hm = new HashMap<Integer,KSOffer>();
 		try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	StringBuilder b = (new StringBuilder()).append("SELECT type,subtype,amount,price,fromplayer,toplayer,zeit FROM ").append(configManager.SQLTable).append("_transaction WHERE ");
         	if(from == true)
         		b.append("fromplayer = ? ");
         	else
         		b.append("toplayer = ? ");
         	if(i != null)
         		b.append("AND type = ? AND subtype = ? ");
         	
         	b.append("ORDER BY zeit DESC LIMIT ").append(begin).append(",").append(am);
         	String strg = b.toString();
     		ps = conn.prepareStatement(strg);
     		ps.setString(1, p);
     		if(i != null)
     		{
     			ps.setInt(2, i.getTypeId());
     			ps.setInt(3, i.getDurability());
     		}
     		
     		ResultSet rs = ps.executeQuery();
     		
     		KSOffer f;
     		ItemStack is;
     		while(rs.next())
     		{
     			is = new ItemStack(rs.getInt("type"));
     			if(rs.getInt("subtype") != 0)
     				is.setDurability((short) rs.getInt("subtype"));
     			f = new KSOffer(is,rs.getString("fromplayer"),rs.getString("toplayer"),rs.getInt("price"), rs.getInt("amount"),rs.getTimestamp("zeit"));
     			hm.put(rs.getInt("id"), f);
     		}
     		
     		if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to get transactions from player: ").append(e).toString());
 		}
 		return hm;
 	}
 	
 	public void giveBack(KSOffer of)
 	{
 		try
 		{
     		Player plx = Bukkit.getServer().getPlayerExact(of.ply);
     		plx.getInventory().addItem(of.getItemStack());
 		} catch(Exception e)
 		{	
 		}
 	
 	}
 	
 	public Map<Integer,KSOffer> getPrices(ItemStack i, int rows)
 	{
 		if(canbeSold(i) == false)
 			return null;
 		
 		HashMap<Integer,KSOffer> ret = new HashMap<Integer,KSOffer>();
 		try
 		{
 			KSOffer k;
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	StringBuilder b = (new StringBuilder()).append("SELECT SUM(amount) as m, price, player FROM ").append(configManager.SQLTable).append("_offer WHERE type = ? AND subtype = ? GROUP BY price ORDER BY price ASC limit 0,?");
 
         	String strg = b.toString();
     		ps = conn.prepareStatement(strg);
     		ps.setInt(1, i.getTypeId());
     		ps.setInt(2, i.getDurability());
     		ps.setInt(3, rows);
     		ResultSet rs = ps.executeQuery();
     			
     		while(rs.next())
     		{
     			k = new KSOffer(i,rs.getString("player"),rs.getInt("price"),rs.getInt("m"));
     			ret.put(rs.getInt("price"), k);
     		}
     		
     		if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to get prices: ").append(e).toString());
 		}
 		
 		return ret;
 	}
 	
 	public Map<Integer,KSOffer> getRequestsOf(ItemStack i, int rows)
 	{
 		if(canbeSold(i) == false)
 			return null;
 		
 		HashMap<Integer,KSOffer> ret = new HashMap<Integer,KSOffer>();
 		try
 		{
 			KSOffer k;
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	StringBuilder b = (new StringBuilder()).append("SELECT SUM(amount) as m, price, player FROM ").append(configManager.SQLTable).append("_request WHERE type = ? AND subtype = ? GROUP BY price ORDER BY price DESC limit 0,?");
 
         	String strg = b.toString();
     		ps = conn.prepareStatement(strg);
     		ps.setInt(1, i.getTypeId());
     		ps.setInt(2, i.getDurability());
     		ps.setInt(3, rows);
     		ResultSet rs = ps.executeQuery();
     			
     		while(rs.next())
     		{
     			k = new KSOffer(i,rs.getString("player"),rs.getInt("price"),rs.getInt("m"));
     			ret.put(rs.getInt("price"), k);
     		}
     		
     		if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to get requests: ").append(e).toString());
 		}
 		
 		return ret;
 	}
 	
 	
 	//Hole Maximale Angebotsmenge für Material
 	public int getMaxAmount(ItemStack i)
 	{
 		return this.getMaxAmount(i, -1);
 	}
 	
 	public int getLowestPrice(ItemStack i)
 	{
 		if(canbeSold(i) == false)
 			return -1;
 		
 		int ret = 0;
 		try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	StringBuilder b = (new StringBuilder()).append("SELECT price as pr FROM ").append(configManager.SQLTable).append("_offer WHERE type = ? AND subtype = ? ORDER BY price ASC limit 0,1");
 
         	String strg = b.toString();
     		ps = conn.prepareStatement(strg);
     		ps.setInt(1, i.getTypeId());
     		ps.setInt(2, i.getDurability());
     		ResultSet rs = ps.executeQuery();
     			
     		while(rs.next())
     		{
     			ret = rs.getInt("pr");
     		}
     		
     		if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to get lowest price: ").append(e).toString());
 		}
 		
 		return ret;
 	}
 	
 	//Hole maximale Angebotsmenge für Material mit max. Preis
 	public int getMaxAmount(ItemStack i, int maxPrice)
 	{
 		if(canbeSold(i) == false)
 			return -1;
 		
 		int ret = 0;
 		try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	StringBuilder b = (new StringBuilder()).append("SELECT SUM(amount) as am FROM ").append(configManager.SQLTable).append("_offer WHERE type = ? AND subtype = ?");
     		if(maxPrice != -1)
     			b.append(" AND price <= ? ");
         	String strg = b.toString();
     		ps = conn.prepareStatement(strg);
     		ps.setInt(1, i.getTypeId());
     		
     		ps.setInt(2, i.getDurability());
     		
     		if(maxPrice != -1)
     			ps.setInt(3, maxPrice);
     		
     		ResultSet rs = ps.executeQuery();
     			
     		while(rs.next())
     		{
     			ret = rs.getInt("am");
     		}
     		
     		if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to get max amount: ").append(e).toString());
 		}
 		
 		return ret;
 	}
 	
 	//Füge Request ein
 	public int enlistRequest(KSOffer of)
 	{
 		if(this.canbeSold(of.getItemStack()) == false)
 			return -2;
 		try
 		{
 			if(of.admin == 0)
 			{
 				double money = Main.econ.getBalance(of.ply);
 				if(money < of.getFullPrice())
 					return -1;
 				
 				if(!Main.econ.withdrawPlayer(of.ply, of.getFullPrice()).transactionSuccess())
 					return -1;
 			}
 			
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	StringBuilder b = (new StringBuilder()).append("INSERT INTO ").append(configManager.SQLTable).append("_request (type,subtype,amount,price,player,admin) VALUES (?,?,?,?,?,?)");
     		ps = conn.prepareStatement(b.toString());
     		ps.setInt(1, of.getItemStack().getTypeId());
     		ps.setInt(2,of.getItemStack().getDurability());
     		ps.setInt(3, of.getAmount());
     		ps.setInt(4, of.getPrice());
     		ps.setString(5, of.getPlayer());
     		ps.setInt(6, of.admin);
     		ps.executeUpdate();
     		
     		if(ps != null)
 				ps.close();
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to request Item: ").append(e).toString());
 			return -3;
 		}
 		
 		return 1;
 	}
 	
 	//Returns Served Amount
 	public int serveRequests(KSOffer of)
 	{
 		try
 		{
 			int amount = of.getAmount();
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
        	StringBuilder b = (new StringBuilder()).append("SELECT amount,id,price FROM ").append(configManager.SQLTable).append("_request WHERE type = ? AND subtype = ? AND price >= ? ORDER BY price DESC, admin ASC LIMIT 0,50");
     		ps = conn.prepareStatement(b.toString());
     		ps.setInt(1, of.i.getTypeId());
     		ps.setInt(2, of.i.getDurability());
     		ps.setInt(3, of.price);
     		
     		ResultSet rs = ps.executeQuery();
 			int tmp = 0;
     		while(rs.next())
     		{
     			if(amount > 0)
     			{
     				System.out.println("[KS] Serving "+rs.getInt("id")+" - "+amount+"/"+rs.getInt("amount") + " by player "+of.ply);
     				tmp = this.serveRequest(rs.getInt("id"),of,amount);
     				if(tmp != -1 && of.admin == 0)
     				{
     					amount -= tmp;
     				}
     			} 
     		}
 
     		if(ps != null)
 				ps.close();
     		if(rs != null)
 				rs.close();
     		
     		return of.getAmount() - amount;
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to serve requests: ").append(e).toString());
 			return -2;
 		}
 	}
 	
 	public int serveRequest(int id, KSOffer of, int amount)
 	{
 		try
 		{
 			int ret = 0;
 			KSOffer ks = null;
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps,ps2=null;
         	
         	StringBuilder b = (new StringBuilder()).append("SELECT sworld,amount,price,type,subtype,player,admin FROM ").append(configManager.SQLTable).append("_request WHERE id = ? LIMIT 0,1");
         	ps = conn.prepareStatement(b.toString());
     		ps.setInt(1, id);
     		
     		boolean found = false;
     		ResultSet rs = ps.executeQuery();
 			
     		while(rs.next())
     		{
     			found = true;
     			ItemStack i = null;
     			i = new ItemStack(rs.getInt("type"));
     			i.setDurability((short) rs.getInt("subtype"));
     			
     			
     			if(rs.getInt("admin") == 1)
     			{
     				//Admin angebote sind immer unlimited.
     				ret = amount;
     			}
     			else if(rs.getInt("amount") > amount)
     			{
     				ret = amount;
     				//Update angebot
     				b = (new StringBuilder()).append("UPDATE ").append(configManager.SQLTable).append("_request SET amount = ? WHERE id = ? LIMIT 1");
     				ps2 = conn.prepareStatement(b.toString());
     				ps2.setInt(1, (rs.getInt("amount") - amount));
     				ps2.setInt(2,id);
     				ps2.executeUpdate();
     				
     				if(rs.getString("sworld").length() > 0)
     					this.updateSign(id, false, false);
 
     			} else
     			{
     				ret = rs.getInt("amount");
     				
     				if(rs.getString("sworld").length() > 0)
     					this.updateSign(id, true, false);
     				
     				//Entferne angebot
     				b = (new StringBuilder()).append("DELETE FROM ").append(configManager.SQLTable).append("_request WHERE id = ? LIMIT 1");
     				ps2 = conn.prepareStatement(b.toString());
     				ps2.setInt(1,id);
     				ps2.executeUpdate();
     			}
 
     			ks = new KSOffer(i,rs.getString("player"),rs.getInt("price"),ret);
     			if(rs.getInt("admin") == 1)
     				ks.admin = 1;
     		}
 
     		
     		if(found == true && ks != null)
     		{
 				b = (new StringBuilder()).append("INSERT INTO ").append(configManager.SQLTable).append("_transaction (type,subtype,fromplayer,toplayer,amount,price) VALUES (?,?,?,?,?,?)");
 				ps2 = conn.prepareStatement(b.toString());
 				ps2.setInt(1,ks.getItemStack().getTypeId());
 				ps2.setInt(2,ks.getItemStack().getDurability());
 				ps2.setString(4, ks.getPlayer());
 				ps2.setString(3, of.getPlayer());
 				ps2.setInt(5,ks.getAmount());
 				ps2.setInt(6,ks.getFullPrice());
 				ps2.executeUpdate();
 				
 				//Anbieter
 				if(ks.admin == 0)
 					this.addDelivery(ks.getPlayer(), ks.getItemStack());
 				
 				//Käufer
 				if(of.admin == 0)
 					this.addDelivery(of.getPlayer(), ks.getFullPrice());
 				
     		}
     		if(ps != null)
 				ps.close();
     		if(rs != null)
 				rs.close();
     		if(ps2 != null)
 				ps2.close();
     		
     		if(!found)
     			return -1;
     		
     		return ret;
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to buy IDItem: ").append(e).toString());
 			return -1;
 		}
 	}
 	
 	//Füge Item in das AH ein
 	public boolean enlistItem(KSOffer of)
 	{
 
 		if(this.canbeSold(of.getItemStack()) == false)
 			return false;
 		
 		int srvd = this.serveRequests(of);
 		if(of.admin == 0)
 		{
 			if(of.getAmount() > srvd)
 			{
 				//Rest verkaufen
 				of.setAmount(of.getAmount() - srvd);
 			} else //schon verkauft.
 				return true;
 		}
 		
 		try
 		{
 			
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	StringBuilder b = (new StringBuilder()).append("INSERT INTO ").append(configManager.SQLTable).append("_offer (type,subtype,amount,price,player,admin) VALUES (?,?,?,?,?,?)");
     		ps = conn.prepareStatement(b.toString());
     		ps.setInt(1, of.getItemStack().getTypeId());
     		ps.setInt(2,of.getItemStack().getDurability());
     		ps.setInt(3, of.getAmount());
     		ps.setInt(4, of.getPrice());
     		ps.setString(5, of.getPlayer());
     		ps.setInt(6, of.admin);
     		ps.executeUpdate();
     		
     		if(ps != null)
 				ps.close();
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to enlist Item: ").append(e).toString());
 			return false;
 		}
 		
 		return true;
 	}
 	
 	public String getOwnerofId(KSId id)
 	{
 		String w = "";
 		if(id.type == 1)
 		{
 			//OFFER
 			w = "_offer";
 		} else if(id.type == 2)
 		{
 			//REQUEST
 			w = "_request";
 		}
 		
 		Connection conn = Main.Database.getConnection();
     	PreparedStatement ps;
     	String name = null;
     	try
     	{
 	    	StringBuilder b = (new StringBuilder()).append("SELECT player FROM ").append(configManager.SQLTable).append(w).append(" WHERE id = ? LIMIT 0,1");
 	    	ps = conn.prepareStatement(b.toString());
 			ps.setInt(1, id.id);
 			
 			ResultSet rs = ps.executeQuery();
 			
 			while(rs.next())
 			{
 				name = rs.getString("player");
 			}
     	} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to enlist Item: ").append(e).toString());
 			return null;
 		}
     	
     	return name;
 	}
 	
 	public void setSign(KSId id, Block b)
 	{
 		String w = "";
 		boolean offer = false;
 		if(id.type == 1)
 		{
 			//OFFER
 			w = "_offer";
 			offer = true;
 		} else if(id.type == 2)
 		{
 			//REQUEST
 			w = "_request";
 		}
 		
 		if(w.length() > 0)
 		{
 			this.updateSign(id.id, true, offer); //zerstöre altes Schild
 			Connection conn = Main.Database.getConnection();
 	    	PreparedStatement ps;
 	    	StringBuilder bld = (new StringBuilder()).append("UPDATE ").append(configManager.SQLTable).append(w).append(" SET signx=?,signy=?,signz=?,sworld=? WHERE id = ? LIMIT 1");
 	    	try 
 	    	{
 				ps = conn.prepareStatement(bld.toString());
 				ps.setInt(1, b.getX());
 				ps.setInt(2, b.getY());
 				ps.setInt(3, b.getZ());
 				ps.setString(4, b.getWorld().getName());
 				ps.setInt(5, id.id);
 				ps.executeUpdate();
 				
 				if(ps != null)
 					ps.close();
 				
 	    	} catch (SQLException e) 
 	    	{
 	    		System.out.println((new StringBuilder()).append("[KS] unable to set sign: ").append(e).toString());
 			}	
 		}
 	}
 	
 	public void updateSign(int id, boolean delete,boolean offer)
 	{
 		Block block = null;
 		int price=0,amount=0,type=0,subtype=0;
 		
 		Connection conn = Main.Database.getConnection();
     	PreparedStatement ps;
     	String what = "";
     	if(offer)
     		what = "_offer";
     	else
     		what = "_request";
     	
     	StringBuilder b = (new StringBuilder()).append("SELECT sworld,signx,signy,signz,amount,price,type,subtype,player,admin FROM ").append(configManager.SQLTable).append(what).append(" WHERE id = ? LIMIT 0,1");
     	try 
     	{
 			ps = conn.prepareStatement(b.toString());
 			ps.setInt(1, id);
 
 			ResultSet rs = ps.executeQuery();
 			
 			while(rs.next())
 			{
 				if(rs.getString("sworld").length() > 0)
 				{
 					World w = Bukkit.getWorld(rs.getString("sworld"));
 					if(w != null)
 						block = w.getBlockAt(rs.getInt("signx"),rs.getInt("signy"),rs.getInt("signz"));
 				}
 				price = rs.getInt("price");
 				amount = rs.getInt("amount");
 				type = rs.getInt("type");
 				subtype = rs.getInt("subtype");
 			}
 			
 			if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 			
     	} catch (SQLException e) 
     	{
     		System.out.println((new StringBuilder()).append("[KS] unable to update sign: ").append(e).toString());
 		}
 		
     	if(block != null && (block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST))
     	{
     		//OKAY UPDATE SIGN
     		if(amount <= 0 || delete == true)
     			block.setType(Material.AIR);
     		else
     		{
     			if(block.getState() instanceof Sign)
     			{
     				String way = "";
     				if(offer)
     					way = "Sell";
     				else
     					way = "Buy";
     				
     				Sign s = (Sign) block.getState();
     				s.setLine(0, way+": "+id);
     				s.setLine(1, KrimBlockName.getNameById(type, subtype));
     				s.setLine(2, "Amount: "+amount);
     				s.setLine(3, "Price: "+price);
     				s.update();
     			}
     		}
     	}
 	}
 	
 	public boolean serveOfferBySign(Player p, Block b)
 	{
 		if(!p.hasPermission("ks.sign"))
 			return false;
 		
 		if(b != null && b.getState() instanceof Sign)
 		{
 			Sign s = (Sign) b.getState();
 			String line = s.getLine(0).toLowerCase();
 			int id = 0,type=0;
 			try
 			{
 				if(line.startsWith("sell: "))
 				{
 					line = line.replace("sell: ", "");
 					//offer
 					
 					if(!p.hasPermission("ks.buy"))
 						return false;
 					
 					id = Integer.parseInt(line);
 					type = 1;
 				} else if(line.startsWith("buy: "))
 				{
 					line = line.replace("buy: ", "");
 					//request
 					
 					if(!p.hasPermission("ks.sell"))
 						return false;
 					
 					id = Integer.parseInt(line);
 					type = 2;
 				}
 			} catch (Exception e) { }
 			
 			if(type != 0 && id != 0)
 			{
 				Connection conn = Main.Database.getConnection();
 		    	PreparedStatement ps;
 		    	boolean done = false;
 		    	String what = "";
 		    	if(type == 1)
 		    		what = "_offer";
 		    	else
 		    		what = "_request";
 		    	
 		    	StringBuilder bl = (new StringBuilder()).append("SELECT sworld,signx,signy,signz,amount,price,type,subtype,player,admin FROM ").append(configManager.SQLTable).append(what).append(" WHERE id = ? LIMIT 0,1");
 		    	try 
 		    	{
 					ps = conn.prepareStatement(bl.toString());
 					ps.setInt(1, id);
 
 					ResultSet rs = ps.executeQuery();
 					
 					while(rs.next())
 					{
 						if(b.getX() == rs.getInt("signx") && b.getY() == rs.getInt("signy") && b.getZ() == rs.getInt("signz") && b.getWorld().getName().equalsIgnoreCase(rs.getString("sworld")))
 						{
 							if(type == 1)
 							{
 								//Kaufe
 								if(Main.econ.getBalance(p.getName()) >= rs.getInt("price"))
 								{
 									buyItem(id, 1, p.getName());
 									Main.econ.withdrawPlayer(p.getName(), rs.getInt("price"));
 									done = true;
 								} else
 									Main.lng.msg(p,"err_nomoney");
 							}
 							else	
 							{
 								//Verkaufe
 								ItemStack i = new ItemStack(Material.AIR);
 								i.setTypeId(rs.getInt("type"));
 								if(rs.getInt("subtype") > 0)
 									i.setDurability((byte)rs.getInt("subtype"));
 								
 								if(p.getItemInHand().getType() == i.getType())
 								{
 									if((rs.getInt("subtype") > 0 && p.getItemInHand().getDurability() == (byte)rs.getInt("subtype")) || (p.getItemInHand().getDurability() == 0 && rs.getInt("subtype") == 0))
 										i.setAmount(p.getItemInHand().getAmount());
 								} else
 									i.setAmount(1);
 								
 								if(rs.getInt("amount") < i.getAmount())
 									i.setAmount(rs.getInt("amount"));
 								
 								KSOffer o = new KSOffer(i,p.getName(),rs.getInt("price"));
 								int am = Main.helper.removeItemsFromPlayer(p, i, i.getAmount());
 								if(am > 0 && am == i.getAmount())
 								{
 									if(o.payFee())
 									{
 										serveRequest(id, o, i.getAmount());
 										done = true;
 									} else
 									{
 										Main.lng.msg(p,"err_nomoney_fee",new Object[]{o.getFee()}); 
 										Main.helper.giveBack(o);
 									}
 								} else
 	            				{
 	            					Main.lng.msg(p,"err_noitem");
 	            				}
 							}
 							
 						}
 						
 					}
 					
 					if(ps != null)
 						ps.close();
 					if(rs != null)
 						rs.close();
 					
 		    	} catch (SQLException e) 
 		    	{
 		    		System.out.println((new StringBuilder()).append("[KS] unable to serve sign: ").append(e).toString());
 				}
 		    	
 		    	return done;
 			} else return false;
 		} else return false;
 	}
 	public int buyItem(int id, int amount, String p)
 	{
 		try
 		{
 			int ret = 0;
 			KSOffer ks = null;
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps,ps2=null;
         	
         	StringBuilder b = (new StringBuilder()).append("SELECT sworld,amount,price,type,subtype,player,admin FROM ").append(configManager.SQLTable).append("_offer WHERE id = ? LIMIT 0,1");
         	ps = conn.prepareStatement(b.toString());
     		ps.setInt(1, id);
     		
     		boolean found = false;
     		boolean admin = false;
     		ResultSet rs = ps.executeQuery();
 			
     		while(rs.next())
     		{
     			found = true;
     			ItemStack i = null;
     			i = new ItemStack(rs.getInt("type"));
     			i.setDurability((short) rs.getInt("subtype"));
     			
     			
     			if(rs.getInt("admin") == 1)
     			{
     				admin = true;
     				//Admin angebote sind immer unlimited.
     				ret = amount;
     			}
     			else if(rs.getInt("amount") > amount)
     			{
     				ret = amount;
     				//Update angebot
     				b = (new StringBuilder()).append("UPDATE ").append(configManager.SQLTable).append("_offer SET amount = ? WHERE id = ? LIMIT 1");
     				ps2 = conn.prepareStatement(b.toString());
     				ps2.setInt(1, (rs.getInt("amount") - amount));
     				ps2.setInt(2,id);
     				ps2.executeUpdate();
     				
     				if(rs.getString("sworld").length() > 0)
     					this.updateSign(id, false, true);
 
     			} else
     			{
     				ret = rs.getInt("amount");
     				
     				if(rs.getString("sworld").length() > 0)
     					this.updateSign(id, true, true);
     				
     				//Entferne angebot
     				b = (new StringBuilder()).append("DELETE FROM ").append(configManager.SQLTable).append("_offer WHERE id = ? LIMIT 1");
     				ps2 = conn.prepareStatement(b.toString());
     				ps2.setInt(1,id);
     				ps2.executeUpdate();
     			}
 
     			ks = new KSOffer(i,rs.getString("player"),rs.getInt("price"),ret);
     		}
 
     		
     		if(found == true && ks != null)
     		{
 				b = (new StringBuilder()).append("INSERT INTO ").append(configManager.SQLTable).append("_transaction (type,subtype,fromplayer,toplayer,amount,price) VALUES (?,?,?,?,?,?)");
 				ps2 = conn.prepareStatement(b.toString());
 				ps2.setInt(1,ks.getItemStack().getTypeId());
 				ps2.setInt(2,ks.getItemStack().getDurability());
 				ps2.setString(3, ks.getPlayer());
 				if(p == null)
 					ps2.setString(4,"admin");
 				else
 					ps2.setString(4,p);
 				ps2.setInt(5,ks.getAmount());
 				ps2.setInt(6,ks.getFullPrice());
 				ps2.executeUpdate();
 				
 				//Käufer
 				if(p != null)
 					this.addDelivery(p, ks.getItemStack());
 				
 				//Verkäufer
 				if(admin == false)
 					this.addDelivery(ks.ply, ks.getFullPrice());
 				
     		}
     		if(ps != null)
 				ps.close();
     		if(rs != null)
 				rs.close();
     		if(ps2 != null)
 				ps2.close();
     		if(!found)
     			return -1;
     		
     		return ret;
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to buy IDItem: ").append(e).toString());
 			return -1;
 		}
 	}
 	
 	//Kaufe Item, gibt zurück wieviele er wirklich gekauft hat
 	public int buyItems(ItemStack i, int maxPrice, String p)
 	{
 		try
 		{
 			int amount = i.getAmount();
 			int pay = 0;
 		
 			if(p != null)
 			{
 				double money = Main.econ.getBalance(p);
 				int maxbuy = (int) (money / maxPrice);
 				
 				if(maxbuy < 1)
 					return -1;
 				else if(maxbuy < amount)
 				{
 					amount = maxbuy;
 					i.setAmount(maxbuy);
 				}
 			}
 			
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
        	StringBuilder b = (new StringBuilder()).append("SELECT amount,id,price FROM ").append(configManager.SQLTable).append("_offer WHERE type = ? AND subtype = ? AND price <= ? ORDER BY price ASC, admin ASC LIMIT 0,50");
     		ps = conn.prepareStatement(b.toString());
     		ps.setInt(1, i.getTypeId());
     		ps.setInt(2, i.getDurability());
     		ps.setInt(3, maxPrice);
     		
     		ResultSet rs = ps.executeQuery();
 			int tmp = 0;
     		while(rs.next())
     		{
     			if(amount > 0)
     			{
     				System.out.println("[KS] Buying "+rs.getInt("id")+" - "+amount+"/"+rs.getInt("amount") + " by player "+p);
     				tmp = this.buyItem(rs.getInt("id"),amount,p);
     				if(tmp != -1)
     				{
     					pay += tmp * rs.getInt("price");
     					amount -= tmp;
     				}
     			} 
     		}
 
     		if(ps != null)
 				ps.close();
     		if(rs != null)
 				rs.close();
     		
     		if(p != null)
     		{
 	    		if(! Main.econ.withdrawPlayer(p, pay).transactionSuccess() || Main.econ.getBalance(p) < 0)
 	    		{
 	    			System.out.println("[KS] FAULT! This should never happen! Player "+p+" didn't have enough money to pay: "+pay);
 	    		}
     		}
     		
     		return i.getAmount() - amount;
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to buy Items: ").append(e).toString());
 			return -2;
 		}
 	}
 	
 	//Entferne Requests, welche über 14 Tage zurückliegen
 	public void pruneRequests()
 	{
 		try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	StringBuilder b = (new StringBuilder()).append("SELECT id FROM ").append(configManager.SQLTable).append("_request WHERE admin = 0 AND zeit < DATE_ADD(CURDATE(), INTERVAL -14 DAY)");
     		ps = conn.prepareStatement(b.toString());
     		ResultSet rs = ps.executeQuery();
     		while(rs.next())
     		{
     			System.out.println((new StringBuilder()).append("[KS] Prune Request ID: ").append(rs.getInt("id")).toString());
     			this.removeRequest(rs.getInt("id"));
     		}
 
     		if(ps != null)
 				ps.close();
     		if(rs != null)
 				rs.close();
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to prune auctions: ").append(e).toString());
 		}
 	}
 	
 	//Entferne Deliverys, welche über 60 Tage zurückliegen
 	public void pruneDelivery()
 	{
 		try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	StringBuilder b = (new StringBuilder()).append("DELETE FROM ").append(configManager.SQLTable).append("_deliver WHERE zeit < DATE_ADD(CURDATE(), INTERVAL -60 DAY)");
     		ps = conn.prepareStatement(b.toString());
     		ps.executeUpdate();
 
     		if(ps != null)
 				ps.close();
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to prune delivery: ").append(e).toString());
 		}
 	}
 	
 	//Entferne Auktionen, die über 30 Tage alt sind
 	public void pruneAuctions()
 	{
 		try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	StringBuilder b = (new StringBuilder()).append("SELECT id FROM ").append(configManager.SQLTable).append("_offer WHERE admin = 0 AND zeit < DATE_ADD(CURDATE(), INTERVAL -30 DAY)");
     		ps = conn.prepareStatement(b.toString());
     		ResultSet rs = ps.executeQuery();
     		while(rs.next())
     		{
     			System.out.println((new StringBuilder()).append("[KS] Prune Auction ID: ").append(rs.getInt("id")).toString());
     			this.removeAuction(rs.getInt("id"));
     		}
 
     		if(ps != null)
 				ps.close();
     		if(rs != null)
 				rs.close();
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to prune auctions: ").append(e).toString());
 		}
 	}
 	
 	//Setze in Abarbeitsungstabelle - nur Geld
 	public boolean addDelivery(String p, int money)
 	{
 		//sollte nie passieren, aber sicher ist sicher
 		if(p.equals("admin"))
 			return true;
 		
 		if(money < 1)
 			return false;
 		
 		try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	StringBuilder b = (new StringBuilder()).append("INSERT INTO ").append(configManager.SQLTable).append("_deliver (money,player) VALUES (?,?)");
     		ps = conn.prepareStatement(b.toString());
     		ps.setInt(1, money);
     		ps.setString(2, p);
     		ps.executeUpdate();
     		
     		if(ps != null)
 				ps.close();
     		
     		this.pokeDelivery(p);
     		return true;
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to add money delivery: ").append(e).toString());
 			return false;
 		}
 	}
 	
 	//Setze in Abarbeitungstabelle - Items
 	public boolean addDelivery(String p, ItemStack i)
 	{
 		//sollte nie passieren, aber sicher ist sicher
 		if(p.equals("admin"))
 			return true;
 		
 		try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	StringBuilder b = (new StringBuilder()).append("INSERT INTO ").append(configManager.SQLTable).append("_deliver (type,subtype,amount,player) VALUES (?,?,?,?)");
     		ps = conn.prepareStatement(b.toString());
     		ps.setInt(1, i.getTypeId());
     		ps.setInt(2, i.getDurability());
     		ps.setInt(3, i.getAmount());
     		ps.setString(4, p);
     		ps.executeUpdate();
     		
     		if(ps != null)
 				ps.close();
     		
     		this.pokeDelivery(p);
     		return true;
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KS] unable to add item delivery: ").append(e).toString());
 			return false;
 		}
 	}
 	
 	public boolean canbeSold(ItemStack i)
 	{
 		if(i != null && i.getType() != null)
 		{
 			//Keine benutzten Gegenstände verkaufbar
 			if(i.getType().getMaxDurability() != 0)
 			{
 				if(i.getType().getMaxDurability() * 0.1f < i.getDurability())
 					return false;
 			}
 			//Keine Verzauberten Gegenstände verkaufbar
 			if(i.getEnchantments() != null && i.getEnchantments().size() > 0)
 				return false;
 			
 			if(i.getType() == Material.AIR)
 				return false;
 			
 			if(i.getAmount() <= 0)
 				return false;
 			
 		} else return false;
 		
 		return true;
 	}
 	
 	//Nimm Items vom Spieler
 	public int removeItemsFromPlayer(Player p, ItemStack i, int amount)
 	{
 	
 		int taken = 0;
 		int firstempty = p.getInventory().firstEmpty();
 		if(firstempty == -1 && configManager.brautec >= 1)
 		{
 			Main.lng.msg(p,"err_full_inv");
 			return 0;
 		} else
 		{
 			for (int u =  0; u < 36; u++) 
 	    	{
 				ItemStack tmp = p.getInventory().getItem(u);
 	    		if(tmp != null && tmp.getTypeId() == i.getTypeId())
 	    		{
 	    			if(tmp.getDurability() == i.getDurability())
 					{
 						if(canbeSold(tmp))
 						{
 							if(amount > 0)
 							{
 								if(tmp.getAmount() <= amount)
 								{
 									taken += tmp.getAmount();
 									amount -= tmp.getAmount();
 									p.getInventory().setItem(u, null);
 								} else
 								{
 									ItemStack n = new ItemStack(tmp.getType());
 									n.setDurability(tmp.getDurability());
 									n.setAmount(tmp.getAmount() - amount);
 									
 									if(configManager.brautec >= 1)
 									{
 										p.getInventory().setItem(u, null);
 										p.getInventory().setItem(firstempty,n);
 									}
 									else
 										p.getInventory().setItem(u, n);
 									
 									taken += amount;
 									amount = 0;
 								}
 							}
 						}
 					}
 	    		} 
 	    	}	
 		}
 		return taken;
 	}
 	
 	//Sende die Infos über das Item wie zb aktueller Preis usw
 	public void sendInfos(Player p, ItemStack i)
 	{
 		String name = KrimBlockName.getNameByItemStack(i);
 		Main.lng.msg(p, "info",new Object[]{name});
 		if(this.canbeSold(i) == false)
 			Main.lng.msg(p, "err_notrade");
 		else
 		{
 			int am = this.getMaxAmount(i);
 			Main.lng.msg(p, "amount_sale",new Object[]{am});
 			
 			if(configManager.werte.get(KrimBlockName.getIdByItemStack(i)) != null)
 			{
 				Main.lng.msg(p, "default_price",new Object[]{ configManager.werte.get(KrimBlockName.getIdByItemStack(i))});
 			}
 			
 			DecimalFormat twoDForm = new DecimalFormat("#.##");
 	        Double val = Double.valueOf(twoDForm.format(this.getDurchschnitsspreis(i, 14)));
 	        
 			Main.lng.msg(p, "average_price",new Object[]{ val.toString() });
 			
 			if(am > 0)
 			{
 				for(Map.Entry<Integer,KSOffer> m : this.getPrices(i, 4).entrySet())
 				{
 					Main.lng.msg(p, "offer", new Object[]{m.getValue().amount,m.getKey()});
 				}
 			}
 			
 			
 			for(Map.Entry<Integer,KSOffer> m : this.getRequestsOf(i, 3).entrySet())
 			{
 				Main.lng.msg(p, "request", new Object[]{m.getValue().amount,m.getKey()});
 			}
 		}
 	}
 
 	
 	public boolean ahNear(CommandSender s)
 	{
 		if(s instanceof Player)
 			return this.ahNear((Player)s);
 		else return false;
 	}
 	public boolean ahNear(Player p)
 	{
 		if(!this.worlds.contains(p.getWorld()))
     		return false;
 		
 		if(configManager.enderForTransaction == 0)
 			return true;
 		
 		if(configManager.ender == 0)
 			return true;
 		
 		int rad = 5;
 		Block temp;
 
 		Block b = p.getLocation().getBlock();
 		for(int i$ = (rad * -1); i$ < rad; i$++)
         {
         	for(int j$ = (rad * -1); j$ < rad; j$++)
             {
         		for(int k$ = (rad * -1); k$ < rad; k$++)
         		{
         			temp = b.getRelative(i$, j$, k$);
         			if(temp.getTypeId() == configManager.interactBlock)
         			{
         				if(configManager.interactBlockSub != 0)
         				{
         					if(temp.getData() == configManager.interactBlockSub)
         						return true;
         				} else
         					return true;
         			}
         		} 
             }
 		}
 		return false;
 	}
 	//Player hat nun etwas im Delivery
 	public void pokeDelivery(String p)
 	{
 		try
 		{
     		Player plx = Bukkit.getServer().getPlayerExact(p);
     		
     		if(ahNear(plx))
     		{
     			this.getDelivery(plx);
     		} else
     		{
     			if(!plx.hasPermission("kb.admin"))
     			{
 	    			if(configManager.ender == 1)
 	        			Main.lng.msg(plx, "goto_ah");
 	        		else
 	        			Main.lng.msg(plx, "collect");
     			}
     		}
     		
 		} catch(Exception e)
 		{
 			
 		}
 	}
 }
