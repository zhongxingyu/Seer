 package me.MnC.MnC_SERVER_MOD.RPG;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 
 import me.MnC.MnC_SERVER_MOD.MnC_SERVER_MOD;
 
 import org.bukkit.block.Block;
 
 public class GugaProfession2 extends GugaProfession
 {
 	private int blocksBroken = 0;
 	
 	protected int userId;
 	
 	private GugaProfession2(String n,int id,int exp)
 	{
 		super(n,exp,MnC_SERVER_MOD.getInstance());
 		this.userId = id;
 	}
 	
 	protected GugaProfession2(){
 		super("UNKNOWN",0,MnC_SERVER_MOD.getInstance());
 	}
 	
 	public void onBlockBreak(Block block)
 	{
		if(block.getTypeId() == 50 || block.getTypeId() == 78 )
 			return;
 		
 		blocksBroken++;
		this.GainExperience(4);	
 		if(blocksBroken>=100)
 		{
 			save();
 		}
 	}
 
 	public void addExperience(int exp)
 	{
 		this.GainExperience(exp);
 		this.save();
 	}
 	
 	public synchronized void save()
 	{
 		blocksBroken = 0;
 		PreparedStatement stat=null;
 		try
 		{
 		    stat = plugin.dbConfig.getConection().prepareStatement("UPDATE `mnc_profession` prof SET prof.experience = ? WHERE prof.user_id = ?");
 		    stat.setInt(1, this.xp);
 		    stat.setInt(2, this.userId);
 		    stat.executeUpdate();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		finally{
 			try {
 				if(stat!=null)
 					stat.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	@Deprecated
 	public String GetPlayerName(){ return "";}
 	
 	/**
 	 * 
 	 * @param name Name of the player
 	 * @param playerId Id of the player (This is actually used to load the profession data)
 	 * @return null if there is no profession for player playerId, valid GugaProfession2 class instance otherwise 
 	 */
 	public static GugaProfession2 loadProfession(String name,int playerId)
 	{
 		if(playerId==0)
 			return null;
 				
 		GugaProfession2 profession = null;
 		try(PreparedStatement stat = MnC_SERVER_MOD.getInstance().dbConfig.getConection().prepareStatement("SELECT experience FROM `mnc_profession` WHERE user_id=?");)
 		{
 			stat.setInt(1, playerId);
 			ResultSet result = stat.executeQuery();
 			if(result.next())
 			{
 				int exp = result.getInt("experience");
 				profession = new GugaProfession2(name,playerId,exp);
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		return profession;
 	}
 }
