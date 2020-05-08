 package me.eistee2.minebuilder;
 
 import java.util.ArrayList;
 
 public class ReadOut {
 
 	
 	private static ReadOut instance = null;
 	public static ReadOut getInstance()
 	{
 		if(instance == null)
 		{
 			instance = new ReadOut();
 		}
 		
 		return instance;
 	}
 	
 	private	configer Settings = new configer("plugins/MineBuilder[Exp]","plugins/MineBuilder[Exp]/Settings.yml");
 	private	configer ExpConfig = new configer("plugins/MineBuilder[Exp]","plugins/MineBuilder[Exp]/ExpConfig.yml");
 	private	configer MoneyConfig = new configer("plugins/MineBuilder[Exp]","plugins/MineBuilder[Exp]/MoneyConfig.yml");
 
 	
 	public void ReadAll()
 	{
 		ReadSettings();
 		ReadExpConfig();
 		ReadMoneyConfig();
 	}
 	
 	//To check if any of this functions turned off so when it is false you dosent get Exp / Money
 	public boolean getExpBoolean(int which)
 	{
 		boolean whichcase = false;
 		switch(which)
 		{
 		case 1:
 			whichcase = expMining;
 			break;
 		case 2:
 			whichcase = expBuilding;
 			break;
 		case 3:
 			whichcase = expEntity;
 			break;	
 		case 4:
 			whichcase = expFishing;
 			break;
 		}
 		
 		return whichcase;
 	}
 	
 	public boolean getMoneyBoolean(int which)
 	{
 		boolean whichcase = false;
 		switch(which)
 		{
 		case 1:
 			whichcase = moneyMining;
 			break;
 		case 2:
 			whichcase = moneyBuilding;
 			break;
 		case 3:
 			whichcase = moneyEntity;
 			break;
 		case 4:
 			whichcase = moneyFishing;
 			break;
 		}	
 		
 		return whichcase;
 	}
 	//Check if the id / Entity name is in the array list and when yes it returns the name
 	public String checkMoneyID(int which,int id,String name)
 	{
 		String BlockBack = null;
 		switch(which)
 		{
 		case 1:
 			for ( String block : moneyBreak) 
 			{
 				if (block.indexOf("ItemID;"+name+":") > -1 || block.indexOf("ItemID;"+id+":") > -1)
 				{
 					return block;
 				}
 			}
 			break;
 		case 2:
 			for (String block : moneyPlace) 
 			{
 				if (block.indexOf("ItemID;"+name+":") > -1 || block.indexOf("ItemID;"+id+":") > -1)
 				{
 					BlockBack = block;
 				}
 			}
 			break;
 		case 3:
 			for (String block : moneySlainEntity) 
 			{
 				if (block.indexOf("ItemID;"+name+":") > -1 || block.indexOf("ItemID;"+id+":") > -1)
 				{
 					BlockBack = block;
 				}
 			}
 			break;
 		case 4:
 				if (moneyFish.indexOf("ItemID;"+name+":") > -1 || moneyFish.indexOf("ItemID;"+id+":") > -1)
 				{
 					BlockBack = moneyFish;
 				}
 				break;
 		case 5:
 			for (String block : moneyRandoms) 
 			{
				if (block.indexOf("ItemID;"+name+":") > -1 || block.indexOf("ItemID;"+id+":") > -1)
 				{
 					BlockBack = block;
 				}
 			}
 			break;
 		}
 		
 		return BlockBack;
 
 	}
 	//get the index of the block to save it in the int array
 	public int getExpIndex(int which , String block)
 	{
 		int index = 0;
 		switch(which)
 		{
 		case 1:
 			index = expBreak.indexOf(block);
 			break;
 		case 2:
 			index = expPlace.indexOf(block);
 			break;
 		case 3:
 			index = expSlainEntity.indexOf(block);
 			break;
 		}
 		return index;
 	}
 
 	
 	//Check the id in the money lists
 	public String checkExpID(int which,int id,String name)
 	{
 		String BlockBack = null;
 		switch(which)
 		{
 		case 1:
 			for ( String block : expBreak) 
 			{
 				if (block.indexOf("ItemID;"+name+":") > -1 || block.indexOf("ItemID;"+id+":") > -1)
 				{
 					return block;
 				}
 			}
 			break;
 		case 2:
 			for (String block : expPlace) 
 			{
 				if (block.indexOf("ItemID;"+name+":") > -1 || block.indexOf("ItemID;"+id+":") > -1)
 				{
 					BlockBack = block;
 				}
 			}
 			break;
 		case 3:
 			for (String block : expSlainEntity) 
 			{
 				if (block.indexOf("ItemID;"+name+":") > -1 || block.indexOf("ItemID;"+id+":") > -1)
 				{
 					BlockBack = block;
 				}
 			}
 			break;
 		case 4:
 				if (expFish.indexOf("ItemID;"+name+":") > -1 || expFish.indexOf("ItemID;"+id+":") > -1)
 				{
 					BlockBack = expFish;
 				}
 				break;
 		case 5:
 			for (String block : expRandoms) 
 			{
				if (block.indexOf("ItemID;"+name+":") > -1 || block.indexOf("ItemID;"+id+":") > -1)
 				{
 					BlockBack = block;
 				}
 			}
 			break;
 		}
 		
 		return BlockBack;
 	}
 	
 	//same as getExpIndex
 	public int getMoneyIndex(int which , String block)
 	{
 		int index = 0;
 		switch(which)
 		{
 		case 1:
 			index = moneyBreak.indexOf(block);
 			break;
 		case 2:
 			index = moneyPlace.indexOf(block);
 			break;
 		case 3:
 			index = moneySlainEntity.indexOf(block);
 			break;
 		}
 		return index;
 	}
 	
 	//Returns the levels to expCalculator
 	public ArrayList<String> getLevels()
 	{		
 		return expLevels;
 	}
 	
 	//Check if permissions on
 	public boolean getPermissionStat()
 	{
 		return permissions;
 	}
 	
 	//returns the Vip percentage
 	public int getVipPercentge()
 	{
 		return vipPercentage;
 	}
 	
 	//need for initialize the int arrays 
 	public int getMaxPlayer()
 	{
 		return maxPlayer;
 	}
 	
 	public int getMaxBlockSaved()
 	{
 		return maxBlockSave;
 	}
 	
 	public int getMaxLevel()
 	{
 		return maxLvl;
 	}
 	
 	public boolean useLevel()
 	{
 		return useLvl;
 	}
 	
 	private	int maxPlayer; //This will be the size of the ints where i save every thing
 	private	int maxBlockSave; // This says how much blocks will get saved before the first ones get deleted
 	
 	private	int vipPercentage;
     private	boolean permissions = false;
 	
 	//Booleans for the Exp settings
     private Boolean expGeneral = false;
     private	Boolean expMining = false;
     private	Boolean expBuilding = false;
     private	Boolean expFishing = false;
     private	Boolean expEntity = false;
 	
 	//Booleans for Money Settings
     private	Boolean moneyGeneral = false;
     private	Boolean moneyMining = false;
     private	Boolean moneyBuilding = false;
     private	Boolean moneyFishing = false;
     private	Boolean moneyEntity = false;
 	
 	
 	private void ReadSettings(){
 		String SettingsContent = Settings.fileGetContent();
 		String[] SettingsContentSplit = SettingsContent.split("\n");
 		for(int x = 0;x < SettingsContentSplit.length; x++)
 		{
 			if(SettingsContentSplit[x].contains("MAXplayer:"))
 			{
 				String[] maxPlayerSplit =  SettingsContentSplit[x].split(":");
 				maxPlayer = Integer.parseInt(maxPlayerSplit[1]);
 			}
 			if(SettingsContentSplit[x].contains("MAXblocksaved:"))
 			{
 				String[] maxBlockSplit =  SettingsContentSplit[x].split(":");
 				maxBlockSave = Integer.parseInt(maxBlockSplit[1]);
 			}
 			if(SettingsContentSplit[x].contains("Use Permissions:true"))
 			{
 				permissions = true;
 			}
 			if(SettingsContentSplit[x].contains("Vip percentage0:"))
 			{
 				String[] vipSplit =  SettingsContentSplit[x].split(":");
 				vipPercentage = Integer.parseInt(vipSplit[1]);
 			}
 			
 			//Checking Exp Settings
 			if(SettingsContentSplit[x].equalsIgnoreCase("Give Exp:true"))
 			{
 				expGeneral = true;
 			}
 		
 			if(SettingsContentSplit[x].equalsIgnoreCase("Use MinerExp:true") && expGeneral == true)
 			{
 				expMining = true;
 			}
 			if(SettingsContentSplit[x].equalsIgnoreCase("Use BuilderExp:true") && expGeneral == true)
 			{
 				expBuilding = true;
 			}
 			if(SettingsContentSplit[x].equalsIgnoreCase("Use FishingExp:true") && expGeneral == true)
 			{
 				expFishing = true;
 			}
 			if(SettingsContentSplit[x].equalsIgnoreCase("Use MobExp") && expGeneral == true)
 			{
 				expEntity = true;
 			}
 			
 			//Checking Money settings
 			if(SettingsContentSplit[x].equalsIgnoreCase("Give Money:true"))
 			{
 				moneyGeneral = true;
 			}
 		
 			if(SettingsContentSplit[x].equalsIgnoreCase("Use MinerMoney:true") && moneyGeneral == true)
 			{
 				moneyMining = true;
 			}
 			if(SettingsContentSplit[x].equalsIgnoreCase("Use BuilderMoney:true") && moneyGeneral == true)
 			{
 				moneyBuilding = true;
 			}
 			if(SettingsContentSplit[x].equalsIgnoreCase("Use FishingMoney:true") && moneyGeneral == true)
 			{
 				moneyFishing = true;
 			}
 			if(SettingsContentSplit[x].equalsIgnoreCase("Use MobMoney:true") && moneyGeneral == true)
 			{
 				moneyEntity = true;
 			}
 
 		}
 	}
 	
 	
 	private	int maxLvl = 0;
 	private	Boolean useLvl = false;
 	
 	
 	private	ArrayList<String> expLevels = new ArrayList<String>();
 	
 	private	ArrayList<String> expRandoms = new ArrayList<String>();
 	private	ArrayList<String> expBreak = new ArrayList<String>();
 	private	ArrayList<String> expPlace = new ArrayList<String>();
 	private	ArrayList<String> expSlainEntity = new ArrayList<String>();
 	private	String expFish = "";
 	
 	//Reads the Exp Config and split it into an ArrayList
 	private void ReadExpConfig()
 	{
 		String ExpContent = ExpConfig.fileGetContent();
 		String[] ExpContentSplit = ExpContent.split("\n");
 		
 		expRandoms.removeAll(expRandoms);
 		expBreak.removeAll(expBreak);
 		expPlace.removeAll(expPlace);
 		expSlainEntity.removeAll(expSlainEntity);
 		
 		for(int x = 0; x < ExpContentSplit.length; x++)
 		{
 			
 			if(ExpContentSplit[x].equalsIgnoreCase("Levels:"))
 			{
 				x += 2;
 				for(int levels = x;levels < ExpContentSplit.length; levels++)
 				{
 					if(ExpContentSplit[levels].equalsIgnoreCase("Randoms:"))
 					{
 						x = levels;
 						break;
 					}
 					if(ExpContentSplit[levels].equalsIgnoreCase("Use Level:true"))
 					{
 						useLvl = true;
 					}
 					else if(ExpContentSplit[levels].contains("MAXlvl:"))
 					{
 						String[] maxLevelSplit =  ExpContentSplit[levels].split(":");
 						maxLvl = Integer.parseInt(maxLevelSplit[1]);
 					}
 					else
 					{
 						expLevels.add(ExpContentSplit[levels]);
 					}
 				}
 			}
 			if(ExpContentSplit[x].equalsIgnoreCase("Randoms:"))
 			{
 				x += 2;
 				for(int randoms = x; randoms < ExpContentSplit.length; randoms++)
 				{
 					if(ExpContentSplit[randoms].equalsIgnoreCase("Break BLock:"))
 					{
 						x = randoms;
 						break;
 					}
 					expRandoms.add(ExpContentSplit[randoms]);					
 				}
 			}
 				
 			if(ExpContentSplit[x].equalsIgnoreCase("Break Block:"))
 			{
 				x+=2;
 				for(int breakBlock = x; breakBlock < ExpContentSplit.length; breakBlock++)
 				{
 					if(ExpContentSplit[breakBlock].equalsIgnoreCase("Place Block:"))
 					{
 						x = breakBlock;
 						break;
 					}
 					expBreak.add(ExpContentSplit[breakBlock]);
 				}
 			}
 		
 			if(ExpContentSplit[x].equalsIgnoreCase("Place Block:"))
 			{
 				x+=2;
 				for(int placeBlock = x; placeBlock < ExpContentSplit.length; placeBlock++)
 				{
 					if(ExpContentSplit[placeBlock].equalsIgnoreCase("Slain Mobs:"))
 					{
 						x = placeBlock;
 						break;
 					}
 					expPlace.add(ExpContentSplit[placeBlock]);
 				}
 			}
 			
 			if(ExpContentSplit[x].equalsIgnoreCase("Slain Mobs:"))
 			{
 				x+=2;
 				for(int slain = x; slain < ExpContentSplit.length; slain++)
 				{
 					if(ExpContentSplit[slain].equalsIgnoreCase("Fishing:"))
 					{
 						x = slain;
 						break;
 					}
 					expSlainEntity.add(ExpContentSplit[slain]);
 				}
 			}	
 			if(ExpContentSplit[x].equalsIgnoreCase("Fishing:"))
 			{
 				x+=2;
 				for(int slain = x; slain < ExpContentSplit.length; slain++)
 				{
 					expFish = ExpContentSplit[slain];
 				}
 			 }
 			
 		}
 	}
 	
 	private	ArrayList<String> moneyRandoms = new ArrayList<String>();
 	private	ArrayList<String> moneyBreak = new ArrayList<String>();
 	private	ArrayList<String> moneyPlace = new ArrayList<String>();
 	private	ArrayList<String> moneySlainEntity = new ArrayList<String>();
 	private	String moneyFish = "";
 	
 	//read the MoneyConfig and split them into arraylists
 	private void ReadMoneyConfig()
 	{
 		String MoneyContent = MoneyConfig.fileGetContent();
 		String[] MoneyContentSplit = MoneyContent.split("\n");
 		
 		moneyRandoms.removeAll(moneyRandoms);
 		moneyBreak.removeAll(moneyBreak);
 		moneyPlace.removeAll(moneyPlace);
 		moneySlainEntity.removeAll(moneySlainEntity);
 		
 		for(int x = 0; x < MoneyContentSplit.length; x++)
 		{
 			
 			if(MoneyContentSplit[x].equalsIgnoreCase("Randoms:"))
 			{
 				x += 2;
 				for(int randoms = x; randoms < MoneyContentSplit.length; randoms++)
 				{
 					if(MoneyContentSplit[randoms].equalsIgnoreCase("Break BLock:"))
 					{
 						x = randoms;
 						break;
 					}
 					moneyRandoms.add(MoneyContentSplit[randoms]);					
 				}
 			}
 				
 			if(MoneyContentSplit[x].equalsIgnoreCase("Break Block:"))
 			{
 				x+=2;
 				for(int breakBlock = x; breakBlock < MoneyContentSplit.length; breakBlock++)
 				{
 					if(MoneyContentSplit[breakBlock].equalsIgnoreCase("Place Block:"))
 					{
 						x = breakBlock;
 						break;
 					}
 					moneyBreak.add(MoneyContentSplit[breakBlock]);
 				}
 			}
 		
 			if(MoneyContentSplit[x].equalsIgnoreCase("Place Block:"))
 			{
 				x+=2;
 				for(int placeBlock = x; placeBlock < MoneyContentSplit.length; placeBlock++)
 				{
 					if(MoneyContentSplit[placeBlock].equalsIgnoreCase("Slain Mobs:"))
 					{
 						x = placeBlock;
 						break;
 					}
 					moneyPlace.add(MoneyContentSplit[placeBlock]);
 				}
 			}
 			
 			if(MoneyContentSplit[x].equalsIgnoreCase("Slain Mobs:"))
 			{
 				x+=2;
 				for(int slain = x; slain < MoneyContentSplit.length; slain++)
 				{
 					if(MoneyContentSplit[slain].equalsIgnoreCase("Fishing:"))
 					{
 						x = slain;
 						break;
 					}
 					moneySlainEntity.add(MoneyContentSplit[slain]);
 				}
 			}	
 			if(MoneyContentSplit[x].equalsIgnoreCase("Fishing:"))
 			{
 				x+=2;
 				for(int slain = x; slain < MoneyContentSplit.length; slain++)
 				{
 					moneyFish = MoneyContentSplit[slain];
 				}
 			 }
 			
 		}
 	}
 
 	
 }
