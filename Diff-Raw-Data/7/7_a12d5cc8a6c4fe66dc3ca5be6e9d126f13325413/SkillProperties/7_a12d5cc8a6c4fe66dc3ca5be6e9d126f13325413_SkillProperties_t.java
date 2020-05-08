 import java.io.IOException;
 import java.util.logging.Logger;
 
 public class SkillProperties {
 	
 	static final Logger log = Logger.getLogger("Minecraft");
 	
 
 	public int[] DestroySkill = new int[100];
 	public int[] CreateSkill = new int[100];
 	public int[] Durability = new int[100]; 
 	public String[] Skills;
 	public String[] Rang;
 	public int[] Exp;
 	public int[] Tools = new int[400];
 	public int[] ToolLevels = new int[400];
 	
 	public int basedurability;
 	public int tobroke;
 	public int savetimer;
 	int BaseToolSkill = 0;
 	
 	public SkillProperties()
 	{
 		for(int i =0 ;i < 100;i++)
 		{
 			this.DestroySkill[i] = -1;
 			this.CreateSkill[i] = -1;
 		}
 		for (int i = 0; i < 400;i++)
 		{
 			this.Tools[i] = -1;
 			this.ToolLevels[i] = 0;
 		}
 
 		if(!this.LoadConfig())
 		{
 
 			Skills = new String[0];
 			Rang = new String[0];
 			Exp = new int[1];
 			Exp[0] = 50000;
 			this.DefaultConfig();
 		}
 		
 	}
 	public boolean LoadConfig()
 	{
 		try{
 			PropertiesFile props  = new PropertiesFile("Skills.properties");
 			
 			String[] temp;
 			props.load();
 			
 			
 			this.basedurability = props.getInt("base-durability", 1);
 	        this.tobroke = props.getInt("to-broke", 5);
 	        this.savetimer = props.getInt("save-timer", 30000);
 	        this.BaseToolSkill = props.getInt("base-toolskill", 0);
 	        
 	        if(!props.containsKey("Durability"))
 	        	return false;
 	        temp = props.getString("Durability").split(",");
 	        for(String str:temp)
 	        {	
 	        	String[] temp2 = str.split("-");
 	        	this.Durability[Integer.parseInt(temp2[0])] = Integer.parseInt(temp2[1]);
 	        }
 	        
 	        if(!props.containsKey("Rang"))
 	        	return false;
 	        temp = props.getString("Rang").split(",");
 	        Rang = new String[temp.length];
 	        for(int i=0;i < temp.length;i++)
 	        {
 	        	Rang[i]=temp[i];
 	        }
 	        
 	        if(!props.containsKey("Exp"))
 	        	return false;
 	        temp = props.getString("Exp").split(",");
 	        Exp = new int[temp.length];
 	        for(int i=0;i < temp.length;i++)
 	        {
 	        	Exp[i]=Integer.parseInt(temp[i]);
 	        }
 	        
 	        if(!props.containsKey("SkillNames"))
 	        	return false;
 	        temp = props.getString("SkillNames").split(",");
 	        Skills = new String[temp.length];
 	        for(int i=0;i < temp.length;i++)
 	        {
 	        	Skills[i]=temp[i];
 	        }
 	        
 	        for(int i=0;i < Skills.length;i++)
 	        {
 	        	if(props.containsKey(Skills[i]+"Destroy"))
 	        	{
 	        		temp = props.getString(Skills[i]+"Destroy").split(",");
 	        		for(String str: temp)
 	        		{
 	        			DestroySkill[Integer.parseInt(str)] = i;
 	        		}
 	        	}
 	        	if(props.containsKey(Skills[i]+"Tools"))
 	        	{
 	        		temp = props.getString(Skills[i]+"Tools").split(",");
 	        		for(String str: temp)
 	        		{
 	    	        	String[] temp2 = str.split("-");
 	    	        	this.Tools[Integer.parseInt(temp2[0])] = i;
 	    	        	this.ToolLevels[Integer.parseInt(temp2[0])] = Integer.parseInt(temp2[1]);
 	    	        	
 	        		}
 	        	}
 	        	if(props.containsKey(Skills[i]+"Create"))
 	        	{
 	        		temp = props.getString(Skills[i]+"Create").split(",");
 	        		for(String str: temp)
 	        		{
 	        			CreateSkill[Integer.parseInt(str)] = i;
 	        		}
 	        	}
 	        }
 	        return true;
 			
 			
 		}catch(IOException ioe)
 		{
 			return false;
 		}
 		
 		
 	}
 
 	public void DefaultConfig()
 	{
 		try
 		{
 			PropertiesFile props  = new PropertiesFile("Skills.properties");
 
 			props = new PropertiesFile("Skills.properties");
 			props.load();
 			
 			props.setInt("base-durability", 1);
 			props.setInt("to-broke", 5);
 			props.setInt("save-timer", 30000);
 			props.setInt("base-toolskill", 0);
 			props.save();
 
 		}catch(IOException ioe){}
 		
 		
 		
 	}
 	
 	public int GetDestroySkill(Block block)
 	{
 		return this.DestroySkill[block.getType()];
 	
 	}
 	public int GetToolLevel(int ItemInHand)
 	{
 		if(ItemInHand == -1)
 			return this.BaseToolSkill;
 		int skill = this.Tools[ItemInHand];
 		if ( skill == -1 )
 			return this.BaseToolSkill;
 		else
 			return this.ToolLevels[ItemInHand];
 	
 	}
 	public int GetDurability(Block block)
 	{
 		int durability = block.getData();
 		if(durability == 0)
 		{
 			if(this.Durability[block.getType()] > 0){
 				durability = this.Durability[block.getType()];
 			}
 			else
 			{
 				durability = this.basedurability;
 			}
 			etc.getServer().setBlockData(block.getX(), block.getY(), block.getZ(), durability);
 		}
 		return durability;
 	}
 	public int GetCreateSkill(Block block)
 	{
 		return this.CreateSkill[block.getType()];
 	}
 	public int GetLevelFromExp(int expirence)
 	{
 		for(int i = 1;i < this.Exp.length ; i++)
 		{
 			if((expirence < this.Exp[i] ) &&   (expirence > this.Exp[i-1] )) 
				return i;
 		}
		return (expirence > this.Exp[this.Exp.length-1])? this.Exp.length-1:1;
 		
 	}
 	public String GetRangFromLevel(int level)
 	{
		level--;
 		if(level < this.Rang.length)
 			return this.Rang[level];
 		else
 			return "";
 	}
 }
