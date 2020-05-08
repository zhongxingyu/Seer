 package de.noonex.moddropplugin.configuration;
 
 import org.bukkit.Material;
 
 import de.noonex.moddropplugin.drops.AbstractDrop;
 import de.noonex.moddropplugin.drops.NormalDrop;
 
 public class DropNode extends AbstractNode
 {
 	public DropNode()
 	{
 		super("DropNode");
 	}
 
 	@Override
 	boolean IsPossibleChild(String name)
 	{
 		return name.equals("AmountNode");
 	}
 
 	@Override
 	boolean IsResponsible(String keyword)
 	{
 		return keyword.equalsIgnoreCase("drops");
 	}
 
 	@Override
 	void ExecuteNode(String parameter, AbstractDrop previousResult)
 	{
 		Material material;
 		
 		try
 		{
 			int id = Integer.parseInt(parameter);
 			material = Material.getMaterial(id);
 		}
 		catch(NumberFormatException ex)
 		{
 			material = Material.getMaterial(parameter);
 		}
 		
		previousResult = new NormalDrop(material, (byte)0);
 	}
 
 }
