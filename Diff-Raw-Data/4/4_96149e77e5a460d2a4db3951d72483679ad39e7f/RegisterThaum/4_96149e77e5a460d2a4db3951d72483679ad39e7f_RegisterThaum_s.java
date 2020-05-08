 package com.lomeli.diving.thaumcraft.config;
 
 import java.io.InputStream;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.block.Block;
 
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 import com.lomeli.diving.thaumcraft.armor.ItemThaumADS;
 import com.lomeli.diving.item.ItemAirTankFull;
 import com.lomeli.diving.DivingGear;
 
 import thaumcraft.api.*;
 import thaumcraft.api.research.*;
 
 public class RegisterThaum {
 	public static Item thaumADS;
 	public static Item thaumTank;
 	
 	public static int thaumADS_ID;
 	public static int thaumTankID;
 	
 	public static String thaumADS_Name;
 	public static String thaumTankName;
 	
 	public static ResearchItem visTank;
 	public static ResearchItem aqualisHelm;
 	
 	public static String research = "/com/lomeli/diving/thaumcraft/config/research/research.xml";
 	public static void register()
 	{
 		thaumADS = new ItemThaumADS(thaumADS_ID, ThaumcraftApi.armorMatThaumium, DivingGear.proxy.addArmor("ThaumADS"), 0).setIconIndex(32).setItemName("thaumADS");
 		LanguageRegistry.addName(thaumADS, thaumADS_Name);
 		
 		thaumTank = new ItemAirTankFull(thaumTankID).setIconIndex(33).setItemName("thaumTank");
 		LanguageRegistry.addName(thaumTank, thaumTankName);
 		
 		ThaumcraftApi.addArcaneCraftingRecipe("VISTANK", "VISTANK", 30, new ItemStack(thaumTank, 1), new Object[]
 		{
 			"TGT", "IGI", " T ", 'T',ItemApi.getItem("itemResource", 2), 'I',Item.ingotIron, 'G',Item.glassBottle
 		});
 		
 		ThaumcraftApi.addInfusionCraftingRecipe("AQUALIS", "AQUALIS", 50,
 				new ObjectTags().add(EnumTag.ARMOR, 2).add(EnumTag.WATER, 2).add(EnumTag.WIND, 4), 
 				new ItemStack(thaumADS, 1), new Object[]
 		{
 			"WTW","TGT","ATA", 'T',ItemApi.getItem("itemResource", 2), 'G',thaumTank, 'W',ItemApi.getItem("itemShard", 2), 'A',ItemApi.getItem("itemShard", 0)
 		});
 		
 	}
 	
 	
 	public static void researchAqua()
 	{
 		loadXML();
 		ThaumcraftApi.registerResearchXML(research);
		visTank = new ResearchItem("VISTANK", new ObjectTags().add(EnumTag.TOOL, 3).add(EnumTag.CONTROL, 2).add(EnumTag.WATER, 2).add(EnumTag.WIND, 5), -5, 3, new ItemStack(thaumTank, 1)).setParents(new ResearchItem[]{ ResearchList.getResearch("ENCHFABRIC") }).registerResearchItem();
		aqualisHelm = new ResearchItem("AQUALIS", new ObjectTags().add(EnumTag.MAGIC, 4).add(EnumTag.WIND, 10).add(EnumTag.WATER, 5).add(EnumTag.ARMOR, 6), -7,3, new ItemStack(thaumADS, 1)).setParents(new ResearchItem[] { visTank }).setHidden().registerResearchItem();
 	}
 	
 	public static void loadXML()
 	{
 		try
 		{
 			InputStream stream = DivingGear.class.getResourceAsStream(research);
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			ThaumcraftApi.researchDoc = dBuilder.parse(stream);
 			ThaumcraftApi.researchDoc.getDocumentElement().normalize();
 		}catch(Exception e){}
 		if ((ThaumcraftApi.apiResearchFiles != null) && (ThaumcraftApi.apiResearchFiles.size() > 0) && 
 				(ThaumcraftApi.researchDoc != null)) 
 		{
 			for (String file : ThaumcraftApi.apiResearchFiles)
 				try{
 					Document rd = null;
 		            InputStream stream = DivingGear.class.getResourceAsStream(file);
 		            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 		            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 		            rd = dBuilder.parse(stream);
 		            rd.getDocumentElement().normalize();
 		            NodeList nList = rd.getElementsByTagName("project");
 		            for (int temp = 0; temp < nList.getLength(); temp++) {
 		            	Node nNode = nList.item(temp);
 		            	ThaumcraftApi.researchDoc.getDocumentElement().appendChild(ThaumcraftApi.researchDoc.importNode(nNode, true));
 		            }
 		        }
 		       	catch (Exception e) {
 		          e.printStackTrace();
 		       	}
 		}
 		try
 		{
 			NodeList nList = ThaumcraftApi.researchDoc.getElementsByTagName("project");
 			for (int temp = 0; temp < nList.getLength(); temp++) 
 			{
 				Node nNode = nList.item(temp);
 		        if (nNode.getNodeType() == 1) 
 		        {
 		        	Element eElement = (Element)nNode;
 		        	eElement.setIdAttribute("id", true);
 		        }
 			}
 		}catch (Exception e) 
 		{
 			e.printStackTrace();
 		}
 		
 	}
 }
