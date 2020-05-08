 package me.MnC.MnC_SERVER_MOD.locker;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import me.MnC.MnC_SERVER_MOD.DatabaseManager;
 import me.MnC.MnC_SERVER_MOD.MinecraftPlayer;
 import me.MnC.MnC_SERVER_MOD.MnC_SERVER_MOD;
 import me.MnC.MnC_SERVER_MOD.UserManager;
 import me.MnC.MnC_SERVER_MOD.Handlers.GameMasterHandler;
 import me.MnC.MnC_SERVER_MOD.chat.ChatHandler;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class BlockLocker implements Listener
 {
 	LockerCommandsExecutor commandsExecutor;
 	
 	public BlockLocker()
 	{
 		this.commandsExecutor = new LockerCommandsExecutor(this);
 	}
 	
 	public boolean LockBlock(Block block,String owner)
 	{
 		LockableBlock blockType = LockableBlock.getByID(block.getTypeId());
 		if(blockType == null)
 			return false;
 		try(PreparedStatement stat = DatabaseManager.getConnection().prepareStatement("INSERT INTO `mnc_blocklock_locks` (owner_id, x, y, z, world,type) SELECT  u.id, ?, ?, ?, ?, ? FROM `mnc_users` u WHERE u.username_clean = ? LIMIT 1;");)
 		{
 			stat.setInt(1, block.getLocation().getBlockX());
 			stat.setInt(2, block.getLocation().getBlockY());
 			stat.setInt(3, block.getLocation().getBlockZ());
 			stat.setString(4, block.getLocation().getWorld().getName());
 			stat.setString(5, blockType.getName());
 			stat.setString(6, owner.toLowerCase());
 			return stat.executeUpdate()==1;
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public void UnlockLock(int lock_id)
 	{
 		try(PreparedStatement stat = DatabaseManager.getConnection().prepareStatement("DELETE mnc_blocklock_locks,mnc_blocklock_perms FROM mnc_blocklock_locks LEFT JOIN mnc_blocklock_perms ON mnc_blocklock_locks.id=mnc_blocklock_perms.lock_id WHERE mnc_blocklock_locks.id=?");)
 		{
 			stat.setInt(1, lock_id);
 			stat.executeUpdate();
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 		
 	public boolean isLocked(Block block)
 	{
 		if(findBlockLock(block) != 0)
 			return true;
 		return false;
 	}
 
 	public int findBlockLock(Block block)
 	{
 		if(block == null)
 			return 0;
 		int lock_id = getBlockLockId(block.getWorld().getName(),block.getX(),block.getY(),block.getZ());
 		if(lock_id == 0 && block.getType() == Material.CHEST)
 		{
 			Block doubleChest = findDoubleBlock(block);
 			if(doubleChest!=null)
 				lock_id = getBlockLockId(block.getWorld().getName(),doubleChest.getX(),doubleChest.getY(),doubleChest.getZ());
 		}
 		return lock_id;
 	}
 	
 	int getBlockLockId(String world,int x,int y, int z)
 	{
 		try(PreparedStatement stat = DatabaseManager.getConnection().prepareStatement("SELECT`id` as lock_id FROM `mnc_blocklock_locks` WHERE world=? AND x=? AND y=? AND z=? LIMIT 1;");)
 		{
 			stat.setString(1,world);
 			stat.setInt(2, x);
 			stat.setInt(3, y);
 			stat.setInt(4, z);
 			try(ResultSet rset = stat.executeQuery();)
 			{
 				if(rset.next())
 					return rset.getInt("lock_id");
 			}
 			catch(SQLException e)
 			{
 				e.printStackTrace();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 	
 	public boolean hasBlockAccess(int player_id, Block block)
 	{
		//TODO: maybe remake to use less queries
 		int lock_id = findBlockLock(block);
 		
 		try(PreparedStatement stat = DatabaseManager.getConnection().prepareStatement("SELECT owner_id FROM mnc_blocklock_locks WHERE id=? LIMIT 1");)
 		{
 			stat.setInt(1, lock_id);
 			try(ResultSet rset = stat.executeQuery();)
 			{
 				if(rset.next())
 				{
 					if(rset.getInt("owner_id") == player_id)
 						return true;
 				}
 				else
 					return true;
 			}
 			catch(SQLException e)
 			{
 				e.printStackTrace();
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		try(PreparedStatement stat = DatabaseManager.getConnection().prepareStatement("SELECT '1' as permission FROM mnc_blocklock_perms WHERE user_id=? AND lock_id=?");)
 		{
 			stat.setInt(1,player_id);
 			stat.setInt(2, lock_id);
 			try(ResultSet rset = stat.executeQuery();)
 			{
 				if(rset.next() && rset.getBoolean("permission"))
 					return true;
 			}
 			catch(SQLException e)
 			{
 				e.printStackTrace();
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	private Block findDoubleBlock(Block block)
 	{
 		BlockFace[] faces = {BlockFace.NORTH,BlockFace.EAST,BlockFace.SOUTH,BlockFace.WEST};
 		for(BlockFace f : faces)
 		{
 			Block b = block.getRelative(f);
 			if(b.getTypeId() == block.getTypeId())
 				return b;
 		}
 		return null;
 	}
 	
 	public String getLockOwner(Block block)
 	{
 		if(block == null)
 			return "";
 		try(PreparedStatement stat = DatabaseManager.getConnection().prepareStatement("SELECT u.`username` as owner_name FROM `mnc_blocklock_locks` bl JOIN `mnc_users` u ON u.`id`=bl.`owner_id` WHERE bl.`x`=? AND bl.`y`=? AND bl.`z`=? AND bl.`world`=?");)
 		{
 			stat.setInt(1, block.getX());
 			stat.setInt(2, block.getY());
 			stat.setInt(3, block.getZ());
 			stat.setString(4,block.getWorld().getName());
 			try(ResultSet rset = stat.executeQuery();)
 			{
 				if(rset.next())
 					return rset.getString("owner_name");
 				
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return "";
 	}
 	
 	public String getLockOwner(int lockId)
 	{
 		if(lockId == 0)
 			return "";
 		try(PreparedStatement stat = DatabaseManager.getConnection().prepareStatement("SELECT u.`username` as owner_name FROM `mnc_blocklock_locks` bl JOIN `mnc_users` u ON u.`id`=bl.`owner_id` WHERE bl.`id`=?");)
 		{
 			stat.setInt(1, lockId);
 			try(ResultSet rset = stat.executeQuery();)
 			{
 				if(rset.next())
 					return rset.getString("owner_name");
 				
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return "";
 	}
 	
 	public void addAccess(int lock_id, String playername)
 	{
 		try(PreparedStatement stat = DatabaseManager.getConnection().prepareStatement("INSERT IGNORE INTO `mnc_blocklock_perms` VALUES(?,(SELECT `id` FROM mnc_users WHERE username_clean = ?))");)
 		{
 			stat.setInt(1,lock_id);
 			stat.setString(2, playername.toLowerCase());
 			stat.executeUpdate();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void removeAccess(int lock_id, String playername)
 	{
 		try(PreparedStatement stat = DatabaseManager.getConnection().prepareStatement("DELETE FROM `mnc_blocklock_perms` WHERE `lock_id`=? AND `user_id`=(SELECT `id` FROM mnc_users WHERE username_clean = ?) LIMIT 1");)
 		{
 			stat.setInt(1,lock_id);
 			stat.setString(2, playername.toLowerCase());
 			stat.executeUpdate();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public ArrayList<String> listAccesses(int lock_id)
 	{
 		try(PreparedStatement stat = DatabaseManager.getConnection().prepareStatement("SELECT u.username as username FROM `mnc_blocklock_perms` blp LEFT JOIN `mnc_users` u ON u.id=blp.user_id WHERE `lock_id`=?");)
 		{
 			stat.setInt(1,lock_id);
 			try(ResultSet rset = stat.executeQuery();)
 			{
 				ArrayList<String> x =  new ArrayList<String>();
 				while(rset.next())
 				{
 					x.add(rset.getString("username"));
 				}
 				return x;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return new ArrayList<String>();
 	}
 
 	public void registerCommands()
 	{
 		MnC_SERVER_MOD plugin = MnC_SERVER_MOD.getInstance();
 		try{
 			plugin.getCommand("locker").setExecutor(this.commandsExecutor);
 		}catch(Exception e){}
 		try{
 			plugin.getCommand("lock").setExecutor(this.commandsExecutor);
 		}catch(Exception e){}
 		try{
 			plugin.getCommand("unlock").setExecutor(this.commandsExecutor);
 		}catch(Exception e){}
 		try{
 			plugin.getCommand("lmod").setExecutor(this.commandsExecutor);
 		}catch(Exception e){}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onBlockBreak(BlockBreakEvent event)
 	{
 		Block block = event.getBlock();
 		Player player = event.getPlayer();
 		if (LockableBlock.isLockableBlock(block.getTypeId()))
 		{
 			int lock_id = findBlockLock(block);
 			if(lock_id > 0)
 			{
 				String owner = getLockOwner(lock_id);
 				event.setCancelled(true);
 				if (owner.equalsIgnoreCase(player.getName()))
 				{
 					ChatHandler.InfoMsg(player, "Nemuzete rozbit zamcenou truhlu.");
 				}
 				else
 				{
 					ChatHandler.FailMsg(player, "Nemuzete rozbit cizi zamcenou truhlu! " + ChatColor.YELLOW + owner + ChatColor.RED + " je vlastnikem teto truhly.");
 				}
 				return;
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onBlockPlace(BlockPlaceEvent event)
 	{
 		Block block = event.getBlockPlaced();
 		Player player = event.getPlayer();
 		if(LockableBlock.isLockableBlock(block.getTypeId()))
 		{
 			if(this.getBlockLockId(block.getWorld().getName(), block.getX(), block.getY(), block.getZ()) != 0)
 			{
 				ChatHandler.InfoMsg(player,"Polozil jste truhlu na misto kde driv byla zamcena. Kontaktujte GM/admina pro odemceni.");
 				return;
 			}
 			
 			if(block.getType() == Material.CHEST)
 			{
 				Block doubleChest = findDoubleBlock(block);
 				
 				if(doubleChest != null)
 				{
					int lock = getBlockLockId(doubleChest.getWorld().getName(),doubleChest.getX(),doubleChest.getY(),doubleChest.getZ());
					if(lock != 0 && !getLockOwner(lock).equalsIgnoreCase(player.getName()))
 					{
 						ChatHandler.FailMsg(player, "Nemuzete postavit truhlu vedle cizi zamcene!");
 						event.setCancelled(true);
 						return;
 					}
 					else
 					{
						LockBlock(block, event.getPlayer().getName());
 						ChatHandler.SuccessMsg(player, "Vase dvojita truhla byla zamcena.");
 						return;
 					}
 				}
 				else
 				{
 					LockBlock(block, event.getPlayer().getName());
 					ChatHandler.SuccessMsg(player, "Vase truhla byla zamcena.");
 					return;
 				}
 			}
 			else if(block.getType() == Material.FURNACE || block.getType() == Material.BURNING_FURNACE)
 			{
 				LockBlock(block,event.getPlayer().getName());
 				ChatHandler.SuccessMsg(player, "Vase pec byla zamcena.");
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onPlayerInteract(PlayerInteractEvent event)
 	{
 		if(event.getAction() != Action.RIGHT_CLICK_BLOCK)
 			return;
 		
 		Block block = event.getClickedBlock();
 		MinecraftPlayer player = UserManager.getInstance().getUser(event.getPlayer().getName()); 
 		
 		LockableBlock lockableBlock = LockableBlock.getByID(block.getTypeId());
 		if(lockableBlock == null)
 			return;
 		
 		if(!(hasBlockAccess(player.getId(),block)) && !GameMasterHandler.IsAtleastGM(event.getPlayer().getName()))
 		{
 			event.setCancelled(true);
 			ChatHandler.FailMsg(event.getPlayer(),"Tento blok je zamcen!");
 		}
 	}
 	
 }
