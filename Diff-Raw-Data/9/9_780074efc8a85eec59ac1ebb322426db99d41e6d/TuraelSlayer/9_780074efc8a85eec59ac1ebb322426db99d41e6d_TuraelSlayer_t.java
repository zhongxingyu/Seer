 package scripts;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.util.HashMap;
 
 import org.tribot.api.DynamicClicking;
 import org.tribot.api.General;
 import org.tribot.api.Timing;
 import org.tribot.api.input.Keyboard;
 import org.tribot.api.input.Mouse;
 import org.tribot.api2007.Banking;
 import org.tribot.api2007.Camera;
 import org.tribot.api2007.ChooseOption;
 import org.tribot.api2007.Game;
 import org.tribot.api2007.GameTab;
 import org.tribot.api2007.GroundItems;
 import org.tribot.api2007.Interfaces;
 import org.tribot.api2007.Inventory;
 import org.tribot.api2007.NPCChat;
 import org.tribot.api2007.NPCs;
 import org.tribot.api2007.Objects;
 import org.tribot.api2007.PathFinding;
 import org.tribot.api2007.Player;
 import org.tribot.api2007.Players;
 import org.tribot.api2007.Projection;
 import org.tribot.api2007.Screen;
 import org.tribot.api2007.Skills;
 import org.tribot.api2007.Walking;
 import org.tribot.api2007.WebWalking;
 import org.tribot.api2007.types.RSGroundItem;
 import org.tribot.api2007.types.RSInterface;
 import org.tribot.api2007.types.RSItem;
 import org.tribot.api2007.types.RSModel;
 import org.tribot.api2007.types.RSNPC;
 import org.tribot.api2007.types.RSObject;
 import org.tribot.api2007.types.RSPlayer;
 import org.tribot.api2007.types.RSTile;
 import org.tribot.script.Script;
 import org.tribot.script.ScriptManifest;
 import org.tribot.script.interfaces.MessageListening07;
 import org.tribot.script.interfaces.Painting;
 import org.tribot.script.interfaces.EventBlockingOverride;
 
 @ScriptManifest(authors = { "Yaw hide" }, version = 0.311, category = "Slayer", name = "Yaw hide's Easy Slayer")
 public class TuraelSlayer extends Script implements MessageListening07, Painting, EventBlockingOverride{
 
 	//food Ids
 	//int[] foodID = {333, 379};
 	int[] foodID = { 333, 329, 379, 361, 7946, 1897 };
 	int[] junk = { 886, 1539, 9003, 229, 1623, 1355, 440, 7767, 117,
 			6963, 554, 556, 829, 1971, 687, 464, 1973, 1917, 808, 1454, 6180,
 			6965, 1969, 6183, 6181 };
 	int[] loot = {563, 560, 561, 562, 564, 565, 566, 7937, 816, 1747, 536, 9142, 868, 563,
 			1615, 1319, 1373, 1247, 1303, 1249, 1123, 1149, 1201,
 			1186, 1113, 1079, 892, 565, 560, 561, 563, 2361, 2366, 443, 
 			985, 987, 2363, 1617, 1619, 574};
 	String[] names = {"Law rune", "Death rune", "Nature rune", "Chaos rune", "Cosmic rune", 
 			"Blood rune", "Soul rune", "Pure essence", "Adamant dart(p)",
 			"Black dragonhide", "Dragon bones", "Mithril bolts",
 			"Rune knife", "Law rune", "Dragonstone",
 			"Rune 2h sword", "Rune battleaxe",
 			"Rune spear", "Rune longsword", "Dragon spear",
 			"Adamant platebody", "Dragon med helm", 
 			"Rune kiteshield", "Rune sq shield", "Rune chainbody",
 			"Rune platelegs", "Rune arrow", "Blood rune", "Death rune",
 			"Nature rune", "Law rune", "Adamantite bar", "Shield left half",
 			"Silver ore", "Half of a key", "Half of a key",
 			"Runite bar", "Uncut diamond", "Uncut ruby", "Air orb",};
 	
 	//TODO LOOT()
 	public void LOOT(){
 		RSGroundItem[] Nests = GroundItems.findNearest(loot);
 		
 		if (!Inventory.isFull() && Nests.length > 0) {
 			if(checkLootInArea(Nests[0])){
 				if (!Nests[0].isOnScreen()) {
 					Walking.clickTileMM(Nests[0].getPosition(), 1);
 					sleep(100, 150);
 					Camera.turnToTile(Nests[0].getPosition());
 					sleep(200, 300);
 					waitIsMovin();
 				}
 				String str = map.get(Nests[0].getID());
 				Nests[0].click("Take " + str);
 				waitForInv(Nests[0].getID());
 			}
 		}
 	}
 	
 	
 	HashMap<Integer, String> map = new HashMap<Integer, String>(loot.length);
 	
 	//slayer items
 	int slayGem = 4155;
 	int COINS = 995;
 	int VTAB = 8007;
 	int FTAB = 8009;
 	int CTAB = 8010;
 	int LTAB = 8008;
 	int ATAB = 8011;
 	int EARMUFFS = 4166;
 	int TINDER = 590;
 	int SALT = 4161;
 	int SPINY = 4551;
 	int LAW = 563;
 	int EARTH = 556;
 	int AIR = 556;
 	int WATER = 555;
 	int FIRE = 554;
 	int[] WATERSKIN = {1823, 1825, 1827, 1829};
 	int COOLER = 6696;
 	int ECTO = 4251;
 	int rope = 954;
 	int SHANTY = 1854;
 	
 	//kalphites 40exp
 	
 	
 	
 	RSTile shantyBankTile = new RSTile(3309, 3120, 0);
 	
 	//antiban stuff
 	private long antiban = System.currentTimeMillis();
 	//private int[] evilchicken = { 2465, 2467 };
 	//private int swarm = 407;
 	
 	// Slayer teleport items
 	int[] gamesNecklace = {3853, 3855, 3857, 3859, 3861, 3863, 3865, 3867}; // goes from 8 to 1
 	int[] rod = {2552, 2554, 2556, 2558, 2560, 2562, 2564, 2566};
 	int[] antiPoison = {2448, 181, 183, 185, 193, 2446, 175}; // start with super 4-1
 	int[] lightsources = {4531, 4539, 4550};
 	int[] depositAllExcept = { 3853, 3855, 3857, 3859, 3861, 3863, 3865, slayGem, VTAB, FIRE, LAW, AIR};
 	
 	
 	String lastServerMessage;
 	int numLeftToKill;
 	String currTask;
 
 	@Override
 	public void run() {
 		
 		onStart();
 		
 		do{
 		getNumSlayTask();
 		sleep(500,600);
 		} while(currTask == "");
 		
 		if(currTask.equals("None")){
 			
 			if(pos().distanceTo(turaelT) <=5){
 				getTask();
 				getNumSlayTask();
 			}
 			else if (inArea(mainCastle[0], mainCastle[1], pos()))
 				gotoTurael();
 			else if (inArea(burthorpeArea[0], burthorpeArea[1], pos())){
 				WebWalking.walkTo(turaelT);
 				waitIsMovin();
 			}
 			else
 				gotoTurael();
 			
 		}
 		else if (isAtTask()){
 			println("I am at my task");
 			if (!haveReqEquip(1))
 				GOTO_BANK();
 			else
 				FIGHT();
 		}
 		else{
 			println("I am NOT at my task pre");
 			if (!haveReqEquip(0)){
 				if(pos().distanceTo(varrockBankT) <= 8)
 					BANKING();
 				else
 					GOTO_BANK();				
 			}
 			else
 				GOTO_TASK();
 		}
 		
 		while(true){
 			
 			current_xp = Skills.getXP("Constitution") + Skills.getXP("Strength") 
 			+ Skills.getXP("Attack") + Skills.getXP("Defense") + Skills.getXP("Range");
 			current_xpSlayer = Skills.getXP("Slayer");
 			XpToLVrange = Skills.getXPToNextLevel("Range");
 			XpToLVhp = Skills.getXPToNextLevel("Constitution");
 			XpToLVstr = Skills.getXPToNextLevel("Strength");
 			XpToLVatt = Skills.getXPToNextLevel("Attack");
 			XpToLVdef = Skills.getXPToNextLevel("Defense");
 			XpToLVslayer = Skills.getXPToNextLevel("Slayer");
 			
 			
 			
 			if (System.currentTimeMillis() - antiban > 150000){
 				antiban = System.currentTimeMillis() ;
 				getNumSlayTask();
 			}
 			
 			if(getHp() < 30){
 				eatFood();
 			}
 			
 			if(currTask.equals("None")){
 				
 				if(pos().distanceTo(turaelT) <=10){
 					getTask();
 					getNumSlayTask();
 				}
 				else if (inArea(mainCastle[0], mainCastle[1], pos()))
 					gotoTurael();
 				else if (inArea(burthorpeArea[0], burthorpeArea[1], pos())){
 					WebWalking.walkTo(turaelT);
 					waitIsMovin();
 				}
 				else
 					gotoTurael();
 				
 			}
 			else if (isAtTask()){
 				if (!haveReqEquip(1))
 					GOTO_BANK();
 				else
 					FIGHT();
 			}
 			else{
				
 				if (!haveReqEquip(0) || Inventory.find(loot).length > 0){
 					if(pos().distanceTo(varrockBankT) <= 8)
 						BANKING();
 					else
 						GOTO_BANK();				
 				}
 				else
 					GOTO_TASK();
 			}
 			
 			sleep(50,100);
 		}
 		
 	}
 
 	
 	
 	public void onStart(){
 		Mouse.setSpeed(General.random(150, 170));
 		sleep(200,300);
 		Walking.control_click = true;
 		Walking.walking_timeout = 5000L;
 		putMap();
 		sleep(200, 250);
 		
 		useTabs = false;
 		
 		checkMagicLevel();
 		sleep(100, 150);
 		
 	}
 	
 	boolean useTabV = true;
 	boolean useTabF = true;
 	boolean useTabC = true;
 	boolean useTabL = true;
 	boolean useTabA = true;
 	boolean useTabs;
 	//TODO checkMagicLevel
 	public void checkMagicLevel(){
 		int lv = Skills.getCurrentLevel("Magic");
 		if(!useTabs){
 			if (lv < 31 && lv >= 25){
 				useTabV = false;
 			}
 			else if (lv < 37){
 				useTabV = false;
 				useTabL = false;
 			}
 			else if (lv < 45){
 				useTabV = false;
 				useTabL = false;
 				useTabF = false;
 			}
 			else if (lv < 51){
 				useTabV = false;
 				useTabL = false;
 				useTabF = false;
 				useTabC = false;
 			}
 			else if (lv >= 51){
 				useTabV = false;
 				useTabL = false;
 				useTabF = false;
 				useTabC = false;
 				useTabA = false;
 			}
 			else
 				useTabs = true;
 		}
 	}
 
 	public void withdrawTabOrRune(int destination){ // 0 is V, 1 is F, 2 is C, 3 is L, 4 is A
 		RSItem[] law = Inventory.find(LAW);
 		RSItem[] earth = Inventory.find(EARTH);
 		RSItem[] air = Inventory.find(AIR);
 		RSItem[] water = Inventory.find(WATER);
 		RSItem[] fire = Inventory.find(FIRE);
 		RSItem[] vtab = Inventory.find(VTAB);
 		RSItem[] ftab = Inventory.find(FTAB);
 		RSItem[] ctab = Inventory.find(CTAB);
 		RSItem[] ltab = Inventory.find(LTAB);
 		RSItem[] atab = Inventory.find(ATAB);
 		
 		if(useTabs){
 			switch(destination){
 			case 0:
 				if(vtab.length == 0){
 					withdraw(10, VTAB);
 					sleep(200,300);
 				}
 				break;
 			case 1:
 				if(ftab.length == 0){
 					withdraw(10, FTAB);
 					sleep(200,300);
 				}
 				break;
 			case 2:
 				if(ctab.length == 0){
 					withdraw(10, CTAB);
 					sleep(200,300);
 				}
 				break;
 			case 3:
 				if(ltab.length == 0){
 					withdraw(10, LTAB);
 					sleep(200,300);
 				}
 				break;
 			case 4:
 				if(atab.length == 0){
 					withdraw(10, ATAB);
 					sleep(200,300);
 				}
 				break;
 			}
 		}
 		else{
 			switch(destination){
 			case 0:
 				if(useTabV){
 					if(vtab.length == 0){
 						withdraw(10, VTAB);
 						sleep(200,300);
 					}
 				}
 				else {
 					if(!haveRune(1)){
 						withdraw(10, LAW);
 						sleep(200,300);
 					}
 					if(!haveRune(2)){
 						withdraw(10, AIR);
 						sleep(200,300);
 					}
 					if(!haveRune(3)){
 						withdraw(10, FIRE);
 						sleep(200,300);
 					}
 				}
 				break;
 			case 1: 
 				if(useTabF){
 				if(ftab.length == 0){
 					withdraw(10, FTAB);
 					sleep(200,300);
 				}
 				}
 				else{
 					if(!haveRune(1)){
 						withdraw(10, LAW);
 						sleep(200,300);
 					}
 					if(!haveRune(2)){
 						withdraw(10, AIR);
 						sleep(200,300);
 					}
 					if(!haveRune(4)){
 						withdraw(10, WATER);
 						sleep(200,300);
 					}
 				}
 				break;
 			case 2:
 				if(useTabC){
 				if(ctab.length == 0){
 					withdraw(10, CTAB);
 					sleep(200,300);
 				}
 				}
 				else {
 					if(!haveRune(1)){
 						withdraw(10, LAW);
 						sleep(200,300);
 					}
 					if (!haveRune(2)){
 						withdraw(10, AIR);
 						sleep(200,300);	
 					}
 				}
 				break;
 			case 3:
 				if(useTabL){
 				if(ltab.length == 0){
 					withdraw(10, LTAB);
 					sleep(200,300);
 				}
 				}
 				else{ 
 					if(!haveRune(1)){ 
 						withdraw(10, LAW);
 						sleep(200,300);
 					}
 					if(!haveRune(2)){
 						withdraw(10, AIR);
 						sleep(200,300);
 					}
 					if(!haveRune(0)){
 						withdraw(10, EARTH);
 						sleep(200,300);
 					}
 				}
 				break;
 			case 4:
 				
 				if(useTabA){
 					if (atab.length == 0) {
 						withdraw(10, ATAB);
 						sleep(200, 300);
 					}
 				}
 				else{ 
 					if(!haveRune(1)){ 
 						withdraw(10, LAW);
 						sleep(200,300);
 					}
 					if (!haveRune(4)){
 						withdraw(10, WATER);
 						sleep(200,300);	
 					}
 				}
 				break;
 			}
 		}
 	}
 	
 	public boolean haveRune(int option){ //Earth 0, Law 1, Air 2, Fire 3, Water 4, 
 		RSItem[] law = Inventory.find(LAW);
 		RSItem[] earth = Inventory.find(EARTH);
 		RSItem[] air = Inventory.find(AIR);
 		RSItem[] water = Inventory.find(WATER);
 		RSItem[] fire = Inventory.find(FIRE);
 		switch(option){
 		case 0:
 			if(earth.length == 0)
 				return false;
 			break;
 		case 1:
 			if(law.length == 0)
 				return false;
 			else{
 				if(law[0].getStack() < 2)
 					return false;
 			}
 			break;
 		case 2:
 			if(air.length == 0)
 				return false;
 			else{
 				if(air[0].getStack() < 5)
 					return false;
 			}
 			break;
 		case 3:
 			if(fire.length == 0)
 				return false;
 			break;
 		case 4:
 			if(water.length == 0)
 				return false;
 			else{
 				if(water[0].getStack() < 2)
 					return false;
 			}
 			break;
 		}
 		return true;
 	}
 	
 	public void useGameNeck(){
 		state="Using games Neck";
 		GameTab.open(org.tribot.api2007.GameTab.TABS.INVENTORY);
 		sleep(200,300);
 		
 		RSItem[] gamesNeck = Inventory.find(gamesNecklace);
 		if (gamesNeck.length > 0){
 			
 			if (gamesNeck[0].click("Rub")){
 				sleep(3500,4750);
 				RSInterface gamebox = Interfaces.get(230, 1);
 				if (gamebox != null){
 					gamebox.click("Continue");
 					sleep(5200,5250);
 				}
 			}
 		}
 	}
 	//TODO useROD
 	public void useROD(){
 		state = "Using ROD";
 		GameTab.open(org.tribot.api2007.GameTab.TABS.INVENTORY);
 		sleep(200,300);
 		
 		RSItem[] ROD = Inventory.find(rod);
 		
 		if (ROD.length > 0){
 			
 			if (ROD[0].click("Rub")){
 				sleep(2500,2750);
 				RSInterface ringbox = Interfaces.get(230, 1);
 				if (ringbox != null){
 					
 					ringbox.click("Continue");
 					sleep(4200,4250);
 				}
 			}
 		}
 	}
 	
 	// area RSTiles for gotoTurael
 	RSTile[] gamesNeckTeleSpot = {new RSTile(2203, 4942, 0), new RSTile(2214, 4936, 0)};
 	RSTile gamesNeckStairsObj = new RSTile(2207, 4935, 0);
 	
 	RSTile[] middleStairs = {new RSTile(2204, 4938, 1), new RSTile(2209, 4933, 1)};
 	RSTile middleStairsObj = new RSTile(2205, 4935, 1);
 	
 	RSTile[] mainCastle = {new RSTile(2892, 3569, 0), new RSTile(2904, 3558, 0)};
 	RSTile[] innerDoorsTile = {new RSTile(2898, 3558, 0), new RSTile(2899, 3558, 0)};
 	/*
 	private RSTile[] toTuraelPath = { new RSTile(2899, 3553, 0), new RSTile(2903, 3546, 0), 
 			new RSTile(2913, 3546, 0), new RSTile(2921, 3542, 0), new RSTile(2928, 3536, 0) };
 	*/
 	RSTile stairAtGameNeckTele =  new RSTile(2207, 4935, 0);
 	
 	public void gotoTurael(){
 		state = "Going to Turael";
 		RSItem[] gamesNeck = Inventory.find(gamesNecklace);
 		RSObject[] GAMESNECKSTAIROBJ = Objects.getAt(stairAtGameNeckTele);
 		Mouse.setSpeed(General.random(130,150));
 		
 		if(inArea(gamesNeckTeleSpot[0], gamesNeckTeleSpot[1], pos())){
 						
 			if (GAMESNECKSTAIROBJ.length > 0){
 				if(GAMESNECKSTAIROBJ[0].isOnScreen()){
 					if(clickOBJ(GAMESNECKSTAIROBJ[0], "Climb-up"))
 						waitIsMovin();
 				}
 				else
 					Walking.blindWalkTo(gamesNeckStairsObj);
 			}
 				
 		}
 		else if (inArea(middleStairs[0], middleStairs[1], pos())){
 			RSObject[] MIDDLESTAIRSOBJ = Objects.getAt(middleStairsObj);
 			if (MIDDLESTAIRSOBJ.length > 0 && clickOBJ(MIDDLESTAIRSOBJ[0], "Climb-up"))
 				waitIsMovin();
 		}
 		else if (inArea(mainCastle[0], mainCastle[1], pos())){
 			
 			RSObject[] door = Objects.getAt(innerDoorsTile[0]);
 			if(door.length > 0){
 				if(door[0].isOnScreen())
 					clickRSObjectAt(innerDoorsTile[0], "Open");
 				else
 					Walking.walkTo(innerDoorsTile[0]);
 			}
 			sleep(1000,1200);
 			WebWalking.walkTo(turaelT);
 			
 		}
 		else{
 			if (gamesNeck.length > 0){
 				
 				if (!inCombat()){
 					println("Going to rub games Necklace");
 					useGameNeck();
 				}
 			}
 		}
 	}
 	
 	
 	
 	//getting a new Task
 	//TODO getTask
 	RSTile[] TuraelArea = {new RSTile(2925, 3542, 0), new RSTile(2932, 3533, 0)};
 		
 	public boolean getTask(){
 		state ="Getting a new task";
 		RSNPC[] turael = NPCs.findNearest("Turael"); // id 70
 
 		if (inArea(TuraelArea[0], TuraelArea[1], pos())) {
 			if (clickNPC(turael[0], "Talk-to Turael")) {
 				waitIsMovin();
 				sleep(150,200);
 				if (NPCChat.clickContinue(true)) {
 					sleep(100,150);
 					if (NPCChat.selectOption("I need another assignment", true)){
 						sleep(100,150);
 						if (NPCChat.clickContinue(true)){
 							sleep(100,150);
 							if(NPCChat.clickContinue(true))
 								if (NPCChat.selectOption("No, that's okay; I'll take a task from you.", true)){
 									sleep(100,150);
 									if(NPCChat.clickContinue(true))
 										return true;
 								}
 									
 						}
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
 	// get the number of monsters left to finish task
 	int numLeftSlayTask;
 	String currSlayTask;
 	
 	public void getNumSlayTask(){
 		state = "Check task #";
 		RSItem[] SLAYGEM = Inventory.find(slayGem);
 		
 		if (SLAYGEM.length > 0){
 			//Mouse.setSpeed(General.random(330,350));
 			
 			if (SLAYGEM[0].click("Check")){
 				sleep(1500,1800);
 				
 				//serverMessageRecieved(currSlayTask);
 								
 				//String LeftSlayTask = SlayerTask.getMonsterToKill();
 				//numLeftSlayTask = SlayerTask.getAmmountLeft();
 				println("Current Task: " + currTask + "   Left to kill: "+ numLeftToKill);
 			}
 			Mouse.setSpeed(General.random(150, 170));
 		}
 	}
 	
 	
 	
 	// goto zombie task, uses similarities in toSkeletons method
 	private RSTile[] toZombies = { new RSTile(3105, 9909, 0), new RSTile(3115, 9909, 0), 
 			new RSTile(3126, 9909, 0), new RSTile(3131, 9911, 0), new RSTile(3139, 9907, 0), 
 			new RSTile(3141, 9900, 0), new RSTile(3139, 9889, 0), new RSTile(3147, 9883, 0) };
 	
 	
 	
 	RSTile zombieMiddle = new RSTile(3145, 9886, 0);	
 
 	
 	// goto lumby bank
 	//private RSTile[] toStairs1F = { new RSTile(3221, 3218, 0), new RSTile(3214, 3214, 0), new RSTile(3209, 3211, 0) };
 	RSTile toStairsTile = new RSTile(3204, 3207, 0);
 	RSTile toStairs1FTile = new RSTile(3204, 3207, 1);
 	RSTile toStairs2FTile = new RSTile(3204, 3207, 2);
 	RSTile lumbyBankTile = new RSTile(3208, 3218, 2);
 	
 	RSTile[] lumby2FArea = { new RSTile(3205, 3228, 2), new RSTile(3215, 3208, 2) };
 	RSTile[] lumby1FArea = { new RSTile(3205, 3228, 1), new RSTile(3215, 3208, 1) };
 	RSTile[] lumbyArea = { new RSTile(3205, 3228, 0), new RSTile(3225, 3208, 0) };
 	
 	
 	// goto varrock bank after teleporting
 	private RSTile[] toVWBankTeleport = { new RSTile(3212, 3423, 0), new RSTile(3202, 3428, 0), 
 			new RSTile(3190, 3429, 0), new RSTile(3184, 3436, 0) };
 	RSTile[] varrockWBankArea = {new RSTile(3185, 3433, 0), new RSTile(3180, 3441, 0)};
 	
 	public void gotoVWBankTelespot(){
 		
 		//if (inArea(varrockTeleportArea[0], varrockTeleportArea[1], pos()))
 		Walking.walkPath(toVWBankTeleport);
 	}
 	
 		
 	/*
 	// icefiends stuff
 	private RSTile[] fromEdgyToIceFiendsPath = { new RSTile(3087, 3493, 0), new RSTile(3083, 3501, 0), 
 			new RSTile(3076, 3504, 0), new RSTile(3070, 3510, 0), new RSTile(3060, 3513, 0),
 			new RSTile(3052, 3507, 0), new RSTile(3052, 3499, 0), new RSTile(3051, 3490, 0), 
 			new RSTile(3051, 3479, 0), new RSTile(3049, 3471, 0), new RSTile(3042, 3471, 0), 
 			new RSTile(3034, 3471, 0), new RSTile(3022, 3472, 0), new RSTile(3015, 3474, 0), 
 			new RSTile(3009, 3477, 0) };
 	
 	
 		private RSTile[] toBatsPath = { new RSTile(2756, 3472, 0), new RSTile(2753, 3464, 0), 
 			new RSTile(2746, 3458, 0), new RSTile(2738, 3457, 0), new RSTile(2731, 3452, 0), 
 			new RSTile(2730, 3445, 0), new RSTile(2729, 3436, 0), new RSTile(2730, 3429, 0), 
 			new RSTile(2729, 3421, 0), new RSTile(2729, 3412, 0), new RSTile(2732, 3402, 0),
 			new RSTile(2740, 3402, 0), new RSTile(2749, 3404, 0), new RSTile(2754, 3402, 0) };
 	
 	
 	
 	
 	// to varrock east bank
 	private RSTile[] toVarrockEBankPath = { new RSTile(3220, 3428, 0), new RSTile(3229, 3429, 0), 
 			new RSTile(3239, 3429, 0), new RSTile(3247, 3428, 0), new RSTile(3252, 3420, 0) };
 	
 	// goblins
 	private RSTile[] toGoblinsPath = { new RSTile(3228, 3219, 0), new RSTile(3234, 3223, 0), 
 			new RSTile(3244, 3226, 0), new RSTile(3255, 3226, 0), new RSTile(3254, 3233, 0), 
 			new RSTile(3251, 3238, 0), new RSTile(3247, 3242, 0), new RSTile(3246, 3246, 0) };
 	
 	
 	// to cows
 	private RSTile[] toCowsGatePath = { new RSTile(3228, 3219, 0), new RSTile(3234, 3223, 0), 
 			new RSTile(3244, 3226, 0), new RSTile(3255, 3226, 0), new RSTile(3254, 3233, 0), 
 			new RSTile(3251, 3238, 0), new RSTile(3252, 3244, 0), new RSTile(3253, 3249, 0), 
 			new RSTile(3250, 3254, 0), new RSTile(3250, 3260, 0), new RSTile(3251, 3266, 0) };
 	RSTile cowGate = new RSTile(2923, 3292, 0);
 	private RSTile[] fromGateToCows = { new RSTile(3256, 3266, 0), new RSTile(3257, 3274, 0), 
 			new RSTile(3256, 3279, 0), new RSTile(3254, 3287, 0) };
 	RSTile cowsMiddle = new RSTile(2925, 3283, 0);
 	
 	// to spiders
 	private RSTile[] toSpidersPath = { new RSTile(3227, 3219, 0), new RSTile(3232, 3224, 0), 
 			new RSTile(3228, 3233, 0), new RSTile(3222, 3238, 0), new RSTile(3215, 3239, 0), 
 			new RSTile(3208, 3240, 0), new RSTile(3202, 3241, 0), new RSTile(3198, 3241, 0), 
 			new RSTile(3189, 3241, 0), new RSTile(3183, 3243, 0), new RSTile(3176, 3242, 0), 
 			new RSTile(3169, 3240, 0), new RSTile(3165, 3244, 0) };
 	
 	
 	// to rats
 	private RSTile[] toRatsPath = { new RSTile(3228, 3219, 0), new RSTile(3234, 3214, 0), 
 			new RSTile(3236, 3208, 0), new RSTile(3238, 3200, 0), new RSTile(3242, 3193, 0), 
 			new RSTile(3241, 3186, 0), new RSTile(3237, 3180, 0), new RSTile(3230, 3174, 0), 
 			new RSTile(3221, 3175, 0), new RSTile(3215, 3175, 0)};
 	
 	//to lumby swamp
 	private RSTile[] toLumbySwampPath = { new RSTile(3228, 3219, 0), new RSTile(3234, 3214, 0), 
 			new RSTile(3236, 3208, 0), new RSTile(3238, 3200, 0), new RSTile(3242, 3193, 0), 
 			new RSTile(3241, 3186, 0), new RSTile(3237, 3180, 0), new RSTile(3230, 3174, 0), 
 			new RSTile(3221, 3175, 0), new RSTile(3215, 3175, 0), new RSTile(3207, 3175, 0), 
 			new RSTile(3199, 3174, 0), new RSTile(3191, 3179, 0), new RSTile(3185, 3178, 0), 
 			new RSTile(3181, 3175, 0), new RSTile(3176, 3174, 0), new RSTile(3169, 3173, 0) };
 	
 	
 	// to alkarid gate
 	private RSTile[] toAlkaridGatePath = { new RSTile(3228, 3219, 0), new RSTile(3234, 3224, 0), 
 			new RSTile(3240, 3226, 0), new RSTile(3246, 3226, 0), new RSTile(3253, 3226, 0), 
 			new RSTile(3260, 3227, 0), new RSTile(3266, 3228, 0) };
 	
 	
 	// to scorpions
 	private RSTile[] toScorionsFromGate = { new RSTile(3275, 3233, 0), new RSTile(3280, 3240, 0), 
 			new RSTile(3284, 3247, 0), new RSTile(3286, 3252, 0), new RSTile(3288, 3258, 0), 
 			new RSTile(3290, 3265, 0), new RSTile(3294, 3271, 0), new RSTile(3297, 3278, 0), 
 			new RSTile(3298, 3282, 0), new RSTile(3299, 3288, 0), new RSTile(3298, 3293, 0) };
 	
 	
 	*/
 	RSTile alkaridGateTile = new RSTile(3267, 3227, 0);
 	RSTile scorpionMiddle = new RSTile(3298, 3293, 0);
 	RSTile darkHole = new RSTile(3169, 3172, 0);
 	RSTile spidersMiddle = new RSTile(3165, 3244, 0);
 	RSTile[] spiderLumby = {new RSTile(3130, 3262, 0), new RSTile(3230, 3200, 0)};
 	
 	
 	RSTile kalphiteMiddle = new RSTile(3506, 9522, 2);
 	RSTile edgeLadderTile = new RSTile(3097, 9867, 0);
 	private RSTile[] toGate = { new RSTile(3096, 9867, 0), new RSTile(3096, 9877, 0), 
 			new RSTile(3095, 9887, 0), new RSTile(3095, 9895, 0), new RSTile(3096, 9904, 0),
 			new RSTile(3102, 9909, 0)};
 	private RSTile[] toSkeletons = { new RSTile(3105, 9909, 0), new RSTile(3115, 9909, 0), 
 			new RSTile(3126, 9909, 0), new RSTile(3131, 9911, 0) };
 	RSTile edgeDungeonGateTile = new RSTile(3103, 9909, 0);
 	RSTile[] toEdgeGateArea = {new RSTile(3094, 9915, 0), new RSTile(3103, 9867, 0)};
 	RSTile[] afterEdgeGateArea = {new RSTile(3104, 9917, 0), new RSTile(3151,9870, 0)};
 	RSTile[] skeletonArea = {new RSTile(3104, 9917, 0), new RSTile(3141, 9141, 0)};
 	RSTile skeletonMiddle = new RSTile(3131, 9911, 0);	
 	RSTile[] zombieArea = {new RSTile(3136, 9910, 0), new RSTile(3151, 9876, 0)};
 	RSTile beforeIceFiends = new RSTile(3031, 3473, 0);
 	/*
 	private RSTile[] toLizards = { new RSTile(3304, 3113, 0), new RSTile(3309, 3108, 0), 
 			new RSTile(3316, 3101, 0), new RSTile(3322, 3096, 0), new RSTile(3327, 3090, 0), 
 			new RSTile(3334, 3085, 0), new RSTile(3341, 3079, 0), new RSTile(3348, 3081, 0), 
 			new RSTile(3355, 3081, 0), new RSTile(3361, 3082, 0), new RSTile(3368, 3082, 0), 
 			new RSTile(3375, 3082, 0), new RSTile(3384, 3081, 0), new RSTile(3391, 3076, 0), 
 			new RSTile(3398, 3071, 0), new RSTile(3406, 3064, 0) };
 	*/
 	
 	
 
 
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/***** killing da monstas *****/
 	//TODO FIGHT
 	public void FIGHT(){
 		
 		state = "Fight";
 		if (currTask.equals("banshees")){
 			if(!checkForHelm(1)){
 				equipHelm(1);
 				sleep(1000,1200);
 				if(!checkForHelm(1)){
 					equipHelm(1);
 					sleep(1000,1200);
 				}
 			}
 			if (!inArea(awkwardBansheeSq[0], awkwardBansheeSq[1], pos()) && inArea(bansheeArea[0], bansheeArea[1], pos())) {
 				Mouse.setSpeed(General.random(340, 360));
 				
 				attackInactiveMonster("Banshee");
 				Mouse.setSpeed(General.random(150, 170));
 			} 
 			else {
 				Walking.walkPath(Walking.generateStraightPath(bansheesT));
 				waitIsMovin();
 			}
 				
 		}
 		else if (currTask.equals( "bats")){
 			if (pos().distanceTo(batMiddle)<=15) {
 				Mouse.setSpeed(General.random(340, 360));
 				
 				attackInactiveMonster("Giant bat");
 				Mouse.setSpeed(General.random(150, 170));
 			} 
 			else {
 				Walking.walkPath(toBat);
 				waitIsMovin();
 			}
 				
 		}
 		else if (currTask.equals( "bears")){
 
 			if (pos().distanceTo(bearsMiddle) <= 20) {
 				Mouse.setSpeed(General.random(340, 360));
 
 				attackInactiveMonster("Grizzly bear");
 				Mouse.setSpeed(General.random(150, 170));
 			} else {
 				WebWalking.walkTo(bearsMiddle);
 				waitIsMovin();
 			}
 		}
 		else if (currTask.equals( "birds")){ // no idea atm
 			if (pos().distanceTo(birdMiddle)<=15) {
 				Mouse.setSpeed(General.random(340, 360));
 				
 				attackInactiveMonster("Terrorbird");
 				Mouse.setSpeed(General.random(150, 170));
 			} 
 			else {
 				Walking.walkPath(Walking.generateStraightPath(birdMiddle));
 				waitIsMovin();
 			}
 				
 		}
 		else if (currTask.equals( "cave_bugs")){
 			if(!checkForHelm(0))
 				equipHelm(0);
 			
 			if (inArea(caveBugArea[0], caveBugArea[1], pos())) {
 				Mouse.setSpeed(General.random(340, 360));
 				attackInactiveMonster("Cave bug");
 				Mouse.setSpeed(General.random(150, 170));
 			} 
 			else {
 				Walking.walkPath(Walking.generateStraightPath(caveSlimeT));
 				waitIsMovin();
 			}
 		}
 		else if (currTask.equals( "cave_crawlers")){
 			if (pos().distanceTo(caveCrawlerT)<=15) {
 				Mouse.setSpeed(General.random(340, 360));
 				
 				attackInactiveMonster("Cave crawler");
 				Mouse.setSpeed(General.random(150, 170));
 			} 
 			else {
 				Walking.walkPath(Walking.generateStraightPath(caveCrawlerT));
 				waitIsMovin();
 			}
 		}
 		else if (currTask.equals( "cave_slimes")){
 			if(!checkForHelm(0))
 				equipHelm(0);
 			
 			if (inArea(caveSlimeArea[0], caveSlimeArea[1], pos()) || inArea(caveSlimeArea2[0], caveSlimeArea2[1], pos())) {
 				Mouse.setSpeed(General.random(340, 360));
 				attackInactiveMonster("Cave slime");
 				Mouse.setSpeed(General.random(150, 170));
 			} 
 			else {
 				Walking.walkPath(Walking.generateStraightPath(caveSlimeT));
 				waitIsMovin();
 			}
 		}
 		else if (currTask.equals( "cows")){
 			if (pos().distanceTo(cowsMiddle)<=15) {
 				Mouse.setSpeed(General.random(340, 360));
 				
 				attackInactiveMonster("Cow");
 				Mouse.setSpeed(General.random(150, 170));
 			} 
 			else {
 				Walking.walkPath(Walking.generateStraightPath(cowsMiddle));
 				waitIsMovin();
 			}
 		}
 		else if (currTask.equals( "crawling_hands")){ 
 			
 			if (pos().distanceTo(crawlingHandsT)<=15) {
 				Mouse.setSpeed(General.random(340, 360));
 				
 				attackInactiveMonster("Crawling Hand");
 				Mouse.setSpeed(General.random(150, 170));
 			} 
 			else {
 				Walking.walkPath(Walking.generateStraightPath(crawlingHandsT));
 				waitIsMovin();
 			}
 				
 		}
 		else if (currTask.equals( "dogs")){
 			if (inArea(dogArea1[0], dogArea1[1], pos()) || inArea(dogArea2[0], dogArea2[1], pos())) {
 				Mouse.setSpeed(General.random(340, 360));
 				attackInactiveMonster("Guard dog");
 				Mouse.setSpeed(General.random(150, 170));
 			} 
 			else {
 				Walking.walkPath(Walking.generateStraightPath(dogsMiddle));
 				waitIsMovin();
 			}	
 		}
 		else if (currTask.equals( "dwarves")){ 
 			if (pos().distanceTo(dwarfMiddle) <=15) {
 				Mouse.setSpeed(General.random(340, 360));
 				attackInactiveMonster("Dwarf");
 				Mouse.setSpeed(General.random(150, 170));
 			} 
 			else {
 				println("Walking to the middle tile");
 				WebWalking.walkTo(dwarfMiddle);
 				waitIsMovin();
 			}
 				
 		}
 		else if (currTask.equals( "ghosts")){
 			if (pos().distanceTo(ghostMiddle)<=20) {
 				Mouse.setSpeed(General.random(340, 360));
 				
 				attackInactiveMonster("Ghost");
 				Mouse.setSpeed(General.random(150, 170));
 			} 
 			else {
 				Walking.walkPath(toGhost);
 				waitIsMovin();
 			}
 				
 		}
 		else if (currTask.equals( "goblins")){
 			if (inArea(goblinArea[0], goblinArea[1], pos())) {
 				Mouse.setSpeed(General.random(340, 360));
 				attackInactiveMonster("Goblin");
 				Mouse.setSpeed(General.random(150, 170));
 			} 
 			else {
 				Walking.walkPath(Walking.generateStraightPath(goblinMiddle));
 				waitIsMovin();
 			}	
 				
 		}
 		else if (currTask.equals( "icefiends")){
 			if (distanceFromTile(iceFiendsMiddle, 15)) {
 				Mouse.setSpeed(General.random(340, 360));
 				attackInactiveMonster("Icefiend");
 				Mouse.setSpeed(General.random(150, 170));
 			} 
 			else {
 				Walking.walkPath(Walking.generateStraightPath(iceFiendsMiddle));
 				waitIsMovin();
 			}
 				
 		}
 		else if (currTask.equals("kalphite")){ 
 			
 			if (distanceFromTile(kalphiteMiddle, 15)) {
 				Mouse.setSpeed(General.random(340, 360));
 				
 				attackInactiveMonster("Kalphite Worker");
 			
 				Mouse.setSpeed(General.random(150, 170));
 				
 				/*RSNPC[] kalphite = NPCs.findNearest("Kalphite Worker");
 				
 				if (kalphite.length > 0) {
 					
 				
 					
 					println("there are lots of kalphites around");
 					int i = isNotFighting(kalphite);
 					if (i > -1) {
 						println("not fighting a kalphite, lets go kill");
 
 						if (clickModel(kalphite[i].getModel(), "Attack", true)) {
 							println("successfully attacked a kalphite");
 							waitIsMovin();
 							waitTillDead(kalphite);
 						} 
 						else if (mobNotOnScreen(kalphite[i])) {
 							runAndTurnCamera(kalphite[i]);
 						}
 						Mouse.setSpeed(General.random(150, 170));
 
 					}
 				}*/
 			} 
 			else {
 				Walking.walkPath(Walking.generateStraightPath(kalphiteMiddle));
 				waitIsMovin();
 			}
 		}
 		else if (currTask.equals( "minotaurs")){ 
 			
 			if (distanceFromTile(minotaurT, 15)) {
 				Mouse.setSpeed(General.random(340, 360));
 				
 				attackInactiveMonster("Minotaur");
 				Mouse.setSpeed(General.random(150, 170));
 			} 
 			else {
 				Walking.walkPath(Walking.generateStraightPath(kalphiteMiddle));
 				waitIsMovin();
 			}
 		}
 		else if (currTask.equals( "monkeys")){
 			if (pos().distanceTo(monkeyT)<=15) {
 				Mouse.setSpeed(General.random(340, 360));
 				
 				attackInactiveMonster("Monkey");
 				Mouse.setSpeed(General.random(150, 170));
 			} 
 			else {
 				Walking.walkPath(Walking.generateStraightPath(monkeyT));
 				waitIsMovin();
 			}
 				
 		}
 		else if (currTask.equals( "rats")){
 			if (pos().distanceTo(ratsMiddle) <= 15) {
 				Mouse.setSpeed(General.random(340, 360));
 				attackInactiveMonster("Giant rat");
 				Mouse.setSpeed(General.random(150, 170));
 			} 
 			else {
 				Walking.walkPath(Walking.generateStraightPath(ratsMiddle));
 				waitIsMovin();
 			}
 				
 		}
 		else if (currTask.equals( "scorpions")){
 			if (pos().distanceTo(scorpionMiddle) <= 15) {
 				Mouse.setSpeed(General.random(340, 360));
 				attackInactiveMonster("Scorpion");
 				Mouse.setSpeed(General.random(150, 170));
 			} 
 			else {
 				Walking.walkPath(Walking.generateStraightPath(scorpionMiddle));
 				waitIsMovin();
 			}
 			
 		}
 		else if (currTask.equals( "skeletons")){
 			
 			if (inArea(skeletonArea[0], skeletonArea[1], pos())) {
 				Mouse.setSpeed(General.random(340, 360));
 				attackInactiveMonster("Skeleton");
 				Mouse.setSpeed(General.random(150, 170));
 			} 
 			else {
 				Walking.walkPath(Walking.generateStraightPath(skeletonMiddle));
 				waitIsMovin();
 			}
 		}
 		else if (currTask.equals( "spiders")){
 			if (pos().distanceTo(spidersMiddle) <= 15) {
 				Mouse.setSpeed(General.random(340, 360));
 				
 				attackInactiveMonster("Giant spider");
 				Mouse.setSpeed(General.random(150, 170));
 			} 
 			else {
 				WebWalking.walkTo(spidersMiddle);
 				waitIsMovin();
 			}
 			
 		}
 		else if (currTask.equals( "werewolves")){
 			if (inArea(canafisArea[0], canafisArea[1], pos())) {
 				Mouse.setSpeed(General.random(340, 360));
 				attackInactiveMonster("");
 				Mouse.setSpeed(General.random(150, 170));
 			} 
 			else {
 				Walking.walkPath(Walking.generateStraightPath(zombieMiddle));
 				waitIsMovin();
 			}
 				
 		}
 		else if (currTask.equals( "wolves")){ 
 			if (distanceFromTile(wolfMiddle, 15)) {
 				Mouse.setSpeed(General.random(340, 360));
 				attackInactiveMonster("White wolf");
 				Mouse.setSpeed(General.random(150, 170));
 			} 
 			else {
 				Walking.walkPath(Walking.generateStraightPath(wolfMiddle));
 				waitIsMovin();
 			}
 				
 		}
 		else if (currTask.equals( "desert_lizards")){
 			if (distanceFromTile(toDesertLizards[toDesertLizards.length -1], 15)) {
 				Mouse.setSpeed(General.random(340, 360));
 				attackInactiveMonster("Small Lizard");
 				Mouse.setSpeed(General.random(150, 170));
 			} 
 			else {
 				WebWalking.walkTo(toDesertLizards[toDesertLizards.length -1]);
 				waitIsMovin();
 			}
 		}
 		else if (currTask.equals( "zombies")){ 
 			if (inArea(zombieArea[0], zombieArea[1], pos())) {
 				Mouse.setSpeed(General.random(340, 360));
 				attackInactiveMonster("Zombie");
 				Mouse.setSpeed(General.random(150, 170));
 			} 
 			else {
 				Walking.walkPath(Walking.generateStraightPath(zombieMiddle));
 				waitIsMovin();
 			}
 		}		
 		
 	}
 	
 	
 	
 	/***** City Areas *****/
 	RSTile[] alkharidArea = {new RSTile(3265, 3322, 0), new RSTile(3324, 3117, 0) };
 	RSTile[] desertArea = {new RSTile(3223, 3115, 0), new RSTile(3430, 3030, 0) };
 	
 	
 	//TODO GOTO_BANK
 	public void GOTO_BANK() {
 		
 		if (pos().distanceTo(varrockBankT) <=7){
 			BANKING();
 		}
 		else {
 			state = "Going to bank";
 			Mouse.setSpeed(General.random(140, 170));
 
 			println("Going to closeset bank");
 			if (pos().distanceTo(varrockTeleT) > 50)
 				emergTele();
 			sleep(200, 300);
 			WebWalking.walkToBank();
 			sleep(50, 100);
 
 			println("done webwalking");
 		}
 	}
 	
 	public boolean BANKING() {
 		state = "banking";
 		if (Banking.isPinScreenOpen()){
 			Banking.inPin();
 			sleep(200, 300);
 			bank();
 			sleep(200,300);
 			return true;
 		}
 		else if (Banking.isBankScreenOpen()){
 			bank();
 			sleep(200, 300);
 			return true;
 		}
 		if (!Banking.isBankScreenOpen()) {
 			if (Banking.openBankBanker()) {
 				if (Banking.isPinScreenOpen()) {
 					Banking.inPin();
 					sleep(200, 300);
 				}
 
 				if (Banking.isBankScreenOpen()) {
 					bank();
 					sleep(200, 300);
 				}
 				return true;
 			} 
 			else if (Banking.openBankBooth()) {
 				if (Banking.isPinScreenOpen()) {
 					Banking.inPin();
 					sleep(200, 300);
 				}
 
 				if (Banking.isBankScreenOpen()) {
 					sleep(200, 300);
 					bank();
 					sleep(200, 300);
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	//TODO bank
 	public void bank(){
 		
 		Banking.depositAllExcept(depositAllExcept);
 		sleep(200,300);
 		
 		
 		RSItem[] coins = Inventory.find(COINS);
 		RSItem[] vtab = Inventory.find(VTAB);
 		RSItem[] ftab = Inventory.find(FTAB);
 		RSItem[] ctab = Inventory.find(CTAB);
 		RSItem[] ltab = Inventory.find(LTAB);
 		RSItem[] atab = Inventory.find(ATAB);
 		RSItem[] gem = Inventory.find(slayGem);
 		RSItem[] earmuffs = Inventory.find(EARMUFFS);
 		RSItem[] lightsource = Inventory.find(lightsources);
 		
 		RSItem[] tinder = Inventory.find(TINDER);
 		//RSItem[] salt = Inventory.find(SALT);
 		RSItem[] spiny = Inventory.find(SPINY);
 		RSItem[] law = Inventory.find(LAW);
 		RSItem[] earth = Inventory.find(EARTH);
 		RSItem[] air = Inventory.find(AIR);
 		RSItem[] water = Inventory.find(WATER);
 		RSItem[] fire = Inventory.find(FIRE);
 		RSItem[] waterskin = Inventory.find(WATERSKIN);
 		RSItem[] cooler = Inventory.find(COOLER);
 		RSItem[] ecto = Inventory.find(ECTO);
 		RSItem[] ROPE = Inventory.find(rope);
 		RSItem[] ROD = Inventory.find(rod);
 		RSItem[] games = Inventory.find(gamesNecklace);
 		RSItem[] food = Inventory.find(foodID);
 		RSItem[] anti = Inventory.find(antiPoison);
 		RSItem[] shanty = Inventory.find(SHANTY);
 		
 		Banking.depositAllExcept(depositAllExcept);
 		sleep(200,300);
 		Mouse.clickBox(471, 78, 478, 84, 1);
 		sleep(200,300);
 		
 		if (games.length == 0){
 			withdraw(1, gamesNecklace);
 			sleep(200,300);
 		}
 		if (food.length < 10){
 			withdraw(10, foodID);
 			sleep(200,300);
 		}
 		
 		if(gem.length == 0){
 			withdraw(1, slayGem);
 			sleep(200,300);
 		}
 		
 		if(vtab.length == 0 || (vtab.length > 0 && vtab[0].getStack() < 10)){
 			withdrawTabOrRune(0);
 		}
 		
 		
 		if (currTask.equals("banshees")){
			if(ecto.length == 0){
				
 				withdraw(1, ECTO);
 				sleep(200,300);
			}
			if(!checkForHelm(1) && earmuffs.length > 0){
 				withdraw(1, EARMUFFS);
 				sleep(200,300);
 			}
 				
 		}
 		else if (currTask.equals( "bats")){
 			withdrawTabOrRune(1);
 			sleep(200,300);
 		}
 		else if (currTask.equals( "bears")){
 			withdrawTabOrRune(4);
 			sleep(200,300);
 			
 				
 		}
 		else if (currTask.equals( "birds")){ // no idea atm
 			if(atab.length == 0){
 				withdrawTabOrRune(4);
 				sleep(200,300);
 			}
 				
 		}
 		else if (currTask.equals("cave_bugs")){
 			if(tinder.length == 0 && ltab.length == 0 && lightsource.length == 0 && spiny.length == 0){
 				
 				withdraw(1, lightsources);
 				sleep(200,300);
 				withdraw(1, SPINY);
 				sleep(200,300);
 				withdraw(1, TINDER);
 				sleep(200,300);
 			}
 			withdrawTabOrRune(3);
 			sleep(200,300);
 		}
 		else if (currTask.equals( "cave_crawlers")){
 			
 				withdrawTabOrRune(2);
 				sleep(200,300);
 			
 			if (anti.length == 0 ){
 				
 				withdraw(1, antiPoison);
 				sleep(200,300);
 				withdraw(1, antiPoison);
 				sleep(200,300);
 			}
 		}
 		else if (currTask.equals( "cave_slimes")){
 			if(tinder.length == 0 && lightsource.length == 0 && anti.length == 0 && spiny.length == 0){
 				
 				withdraw(1, lightsources[0]);
 				sleep(200,300);
 				withdraw(1, antiPoison);
 				sleep(200,300);
 				withdraw(1, antiPoison);
 				sleep(200,300);
 				withdraw(1, SPINY);
 				sleep(200,300);
 				withdraw(1, TINDER);
 				sleep(200,300);
 			}
 			withdrawTabOrRune(3);
 			sleep(200,300);
 		}
 		else if (currTask.equals( "cows")){
 			
 				withdrawTabOrRune(1);
 				sleep(200,300);
 			
 		}
 		else if (currTask.equals( "crawling_hands")){ 
 			if(ecto.length == 0){
 				withdraw(1, ECTO);
 				sleep(200,300);
 			}
 				
 		}
 		else if (currTask.equals( "dogs")){
 		
 				withdrawTabOrRune(4);
 				sleep(200,300);
 			
 				
 		}
 		else if (currTask.equals( "dwarves")){ 
 			
 				withdrawTabOrRune(0);
 				sleep(200,300);
 			
 				
 		}
 		else if (currTask.equals( "ghosts")){
 	
 				withdrawTabOrRune(1);
 				sleep(200,300);
 			
 				
 		}
 		else if (currTask.equals( "goblins")){
 	
 				withdrawTabOrRune(3);
 				sleep(200,300);
 			
 				
 		}
 		else if (currTask.equals( "icefiends")){
 		
 				withdrawTabOrRune(1);
 				sleep(200,300);
 			
 				
 		}
 		else if (currTask.equals("kalphite")){ 
 			if(ROPE.length == 0 && waterskin.length == 0 && shanty.length == 0){
 				withdraw(1, rope);
 				sleep(200,300);
 				withdraw(1, WATERSKIN);
 				sleep(200,300);
 				withdraw(5, SHANTY);
 				sleep(200,300);
 				withdraw(1, rod);
 				sleep(200,300);
 			}
 				
 		}
 		else if (currTask.equals( "minotaurs")){ 
 			
 				withdrawTabOrRune(0);
 				sleep(200,300);
 			
 				
 		}
 		else if (currTask.equals( "monkeys")){
 			
 				withdrawTabOrRune(1);
 				sleep(200,300);
 			
 			if (coins.length > 0){
 				if(coins[0].getStack() < 10000){
 					withdraw(10000, COINS);
 					sleep(200,300);
 				}
 			}
 			else{
 				withdraw(10000, COINS);
 				sleep(200,300);
 			}
 				
 		}
 		else if (currTask.equals( "rats")){
 			
 				withdrawTabOrRune(0);
 				sleep(200,300);
 			
 				
 		}
 		else if (currTask.equals( "scorpions")){
 			if(ROD.length == 0){
 				withdraw(1, rod);
 				sleep(200,300);
 			}
 			
 		}
 		else if (currTask.equals( "skeletons")){
 			
 				withdrawTabOrRune(0);
 				sleep(200,300);
 			
 			
 		}
 		else if (currTask.equals( "spiders")){
 		
 				withdrawTabOrRune(3);
 				sleep(200,300);
 			
 			
 		}
 		else if (currTask.equals( "werewolves")){
 			if(ecto.length == 0){
 				withdraw(1, ECTO);
 				sleep(200,300);
 			}
 				
 		}
 		else if (currTask.equals( "wolves")){ 
 		
 				withdrawTabOrRune(2);
 				sleep(200,300);
 			
 				
 		}
 		else if (currTask.equals( "desert_lizards")){
 			if(ROD.length == 0 && waterskin.length == 0 && shanty.length == 0 && cooler.length < numLeftToKill+10){
 				withdraw(1, rod);
 				sleep(200,300);
 				withdraw(8, WATERSKIN);
 				sleep(200,300);
 				withdraw(5, SHANTY);
 				sleep(200,300);
 				withdraw(numLeftToKill + 30, COOLER);
 				sleep(200,300);
 			}
 				
 		}
 		else if (currTask.equals( "zombies")){ 
 	
 				withdrawTabOrRune(0);
 				sleep(200,300);
 			
 		}		
 		
 		Banking.close();
 		sleep(200,300);
 		
 		BANK_SECTION = 0;
 	}
 	
 	//TODO haveReqEquip
 	public boolean haveReqEquip(int option){
 		state="item check";
 		RSItem[] coins = Inventory.find(COINS);
 		RSItem[] vtab = Inventory.find(VTAB);
 		RSItem[] ftab = Inventory.find(FTAB);
 		RSItem[] ctab = Inventory.find(CTAB);
 		RSItem[] ltab = Inventory.find(LTAB);
 		RSItem[] atab = Inventory.find(ATAB);
 		RSItem[] gem = Inventory.find(slayGem);
 		RSItem[] earmuffs = Inventory.find(EARMUFFS);
 		RSItem[] lightsource = Inventory.find(lightsources);
 		
 		RSItem[] tinder = Inventory.find(TINDER);
 		//RSItem[] salt = Inventory.find(SALT);
 		RSItem[] spiny = Inventory.find(SPINY);
 		RSItem[] law = Inventory.find(LAW);
 		RSItem[] earth = Inventory.find(EARTH);
 		RSItem[] air = Inventory.find(AIR);
 		RSItem[] water = Inventory.find(WATER);
 		RSItem[] fire = Inventory.find(FIRE);
 		RSItem[] waterskin = Inventory.find(WATERSKIN);
 		RSItem[] cooler = Inventory.find(COOLER);
 		RSItem[] ecto = Inventory.find(ECTO);
 		RSItem[] ROPE = Inventory.find(rope);
 		RSItem[] ROD = Inventory.find(rod);
 		RSItem[] games = Inventory.find(gamesNecklace);
 		RSItem[] food = Inventory.find(foodID);
 		RSItem[] anti = Inventory.find(antiPoison);
 		RSItem[] shanty = Inventory.find(SHANTY);
 		
 		println("step 1");
 		if (option == 0){
 			
 			if (games.length == 0){
 				println("Dont have either a games necklace");
 				return false;
 			}
 			else if (food.length == 0){
 				println("Dont have food");
 				return false;
 			}
 			else if (gem.length == 0){
 				println("Dont have slayer gem");
 				return false;
 			}
 			else if (useTabs){
 				
 				if (vtab.length == 0){
 					println("Dont have  varrock tabs");
 					return false;
 				}
 				
 			}
 			else if (!useTabs){
 				if (!haveRune(1) || !haveRune(2) || !haveRune(3)){
 					println("Dont have varrock runes");
 					return false;
 				}
 				
 			}
 			
 			if (currTask.equals("banshees")) {
 				if (ecto.length > 0 && (checkForHelm(1) || earmuffs.length > 0))
 					return true;
 				
 			}
 			else if (currTask.equals( "bats")){
 				
 				if(useTabs || useTabF){
 					
 					if(ftab.length > 0)
 						return true;
 				}
 				else{
 					
 					if (!haveRune(1) || !haveRune(2) || !haveRune(4)){
 						return false;
 					}
 					else
 						return true;
 				}
 				
 			}
 			else if (currTask.equals( "bears")){
 				
 				if(useTabs || useTabA){
 					
 					if(atab.length > 0)
 						return true;
 				}
 				else{
 					println("step 4");
 					if (!haveRune(1) || !haveRune(4)){
 						
 						return false;
 					}
 					else
 						return true;
 				}
 			}
 			else if (currTask.equals( "birds")){ // no idea atm
 				if(useTabs || useTabA){
 					if(atab.length > 0)
 						return true;
 				}
 				else{
 					if (!haveRune(1) || !haveRune(4))
 						return false;
 					else
 						return true;
 				}
 			}
 			else if (currTask.equals("cave_bugs")){
 				if(tinder.length > 0 && lightsource.length > 0 && (checkForHelm(0) || spiny.length > 0))
 					return true;
 				if(useTabs || useTabL){
 					if(ltab.length > 0)
 						return true;
 				}
 				else{
 					if (!haveRune(1) || !haveRune(0) || !haveRune(2))
 						return false;
 					else
 						return true;
 				}
 			}
 			else if (currTask.equals( "cave_crawlers")){
 				if(anti.length > 0)
 					return true;
 				if(useTabs || useTabC){
 					if(ctab.length > 0)
 						return true;
 				}
 				else{
 					if (!haveRune(1) || !haveRune(2))
 						return false;
 					else
 						return true;
 				}
 		}
 			else if (currTask.equals( "cave_slimes")){
 				if(tinder.length > 0 && lightsource.length > 0 && anti.length > 0 && (checkForHelm(0) || spiny.length > 0))
 					return true;
 				if(useTabs || useTabL){
 					if(ltab.length > 0)
 						return true;
 				}
 				else{
 					if (!haveRune(1) || !haveRune(0) || !haveRune(2))
 						return false;
 					else
 						return true;
 				}
 		}
 			else if (currTask.equals( "cows")){
 				if(useTabs || useTabF){
 					if(ftab.length > 0)
 						return true;
 				}
 				else{
 					if (!haveRune(1) || !haveRune(2) || !haveRune(4))
 						return false;
 					else
 						return true;
 				}
 		}
 			else if (currTask.equals( "crawling_hands")){
 			if(ecto.length > 0)
 				return true;
 		}
 			else if (currTask.equals( "dogs")){
 				if(useTabs || useTabA){
 					if(atab.length > 0)
 						return true;
 				}
 				else{
 					if (!haveRune(1) || !haveRune(4))
 						return false;
 					else
 						return true;
 				}
 		}
 			else if (currTask.equals( "dwarves")){ 
 			if(useTabs){
 				if(vtab.length > 0)
 					return true;
 			}
 			else{
 				if (!haveRune(1) || !haveRune(2) || !haveRune(3)){
 					println("Dont have  varrock runes");
 					return false;
 					
 				}
 				else
 					return true;
 			}
 		}
 			else if (currTask.equals( "ghosts")){
 				if(useTabs || useTabF){
 					if(ftab.length > 0)
 						return true;
 				}
 				else{
 					if (!haveRune(1) || !haveRune(2) || !haveRune(4))
 						return false;
 					else
 						return true;
 				}
 		}
 			else if (currTask.equals( "goblins")){
 				if(useTabs || useTabL){
 					if(ltab.length > 0)
 						return true;
 				}
 				else{
 					if (!haveRune(1) || !haveRune(0) || !haveRune(2))
 						return false;
 					else
 						return true;
 				}
 		}
 			else if (currTask.equals( "icefiends")){
 				if(useTabs || useTabF){
 					if(ftab.length > 0)
 						return true;
 				}
 				else{
 					if (!haveRune(1) || !haveRune(2) || !haveRune(4))
 						return false;
 					else
 						return true;
 				}
 		}
 			else if( currTask.equals( "kalphite")){
 			//println("chceking if I have all the stuff for kalphite");
 				if (inArea(desertArea[0], desertArea[1], pos())){
 					if(ROPE.length > 0 && waterskin.length > 0)
 						return true;
 			}
 				else if (inArea(alkharidArea[0], alkharidArea[1], pos())){
 					if(ROPE.length > 0 && waterskin.length > 0 && shanty.length > 0)
 						return true;
 			}
 				else if (inArea(kalphiteTunnel[0], kalphiteTunnel[1], pos()))
 					return true;
 				else{
 					if(ROPE.length > 0 && waterskin.length > 0 && shanty.length > 0 && ROD.length > 0)
 						return true;
 			}
 		} 
 			else if (currTask.equals("minotaurs")) {
 			if(useTabs){
 				if(vtab.length > 0)
 					return true;
 			}
 			else{
 				if (!haveRune(1) || !haveRune(2) || !haveRune(3)){
 					println("Dont have  varrock runes");
 					return false;
 					
 				}
 				else
 					return true;
 			}
 			} 
 			else if (currTask.equals("monkeys")) {
 				if (coins.length == 0)
 					return false;
 				if(useTabs || useTabF){
 					if(ftab.length > 0)
 						return true;
 				}
 				else{
 					if (!haveRune(1) || !haveRune(2) || !haveRune(4))
 						return false;
 					else
 						return true;
 				}				
 			} 
 			else if (currTask.equals("rats")) {
 				if(useTabs){
 					if(vtab.length > 0)
 						return true;
 				}
 				else{
 					if (!haveRune(1)|| !haveRune(2) || !haveRune(3)){
 						println("Dont have  varrock runes");
 						return false;
 					}
 					else
 						return true;
 				}
 			} 
 			else if (currTask.equals("scorpions")) {
 				if (ROD.length > 0)
 					return true;
 			} 
 			else if (currTask.equals("skeletons")) {
 				if(useTabs){
 					if(vtab.length > 0)
 						return true;
 				}
 				else{
 					if (!haveRune(1) || !haveRune(2) || !haveRune(3)){
 						println("Dont have  varrock runes");
 						return false;
 					}
 					else
 						return true;
 				}
 			} 
 			else if (currTask.equals("spiders")) {
 				if(useTabs || useTabL){
 					if(ltab.length > 0)
 						return true;
 				}
 				else{
 					if (!haveRune(1) || !haveRune(0) || !haveRune(2))
 						return false;
 					else
 						return true;
 				}
 			} 
 			else if (currTask.equals("werewolves")) {
 				if (ecto.length > 0)
 					return true;
 			} 
 			else if (currTask.equals("wolves")) {
 				if(useTabs || useTabC){
 					if(ctab.length > 0)
 						return true;
 				}
 				else{
 					if (!haveRune(1) || !haveRune(2))
 						return false;
 					else
 						return true;
 				}
 			} 
 			else if (currTask.equals("desert_lizards")) {
 			if (inArea(desertArea[0], desertArea[1], pos())) {
 					if (waterskin.length > 0 && cooler.length > 0)
 						return true;
 				} 
 			else if (inArea(alkharidArea[0], alkharidArea[1], pos())) {
 					if (waterskin.length > 0 && shanty.length > 0 && cooler.length > 0)
 						return true;
 			} 
 			else {
 				if (ROD.length > 0 && waterskin.length > 0 && shanty.length > 0 && cooler.length > 0)
 					return true;
 			}
 		} 
 			else if (currTask.equals("zombies")) {
 				if(useTabs){
 					if(vtab.length > 0)
 						return true;
 				}
 				else{
 					if (!haveRune(1) || !haveRune(2) || !haveRune(3)){
 						println("Dont have  varrock runes");
 						return false;
 					}
 					else
 						return true;
 				}
 			}
 			return false;
 		}
 		/***** option 1 **** 
 		 * I am at my task already
 		 * 
 		 * 
 		 * 
 		 * 
 		 * */
 		else{ 
 			if (games.length == 0){
 				println("Dont have either a games necklace");
 				return false;
 			}
 			else if (food.length == 0){
 				println("Dont have food");
 				return false;
 			}
 			else if (gem.length == 0){
 				println("Dont have slayer gem");
 				return false;
 			}
 			else if (useTabs){
 				if (vtab.length == 0){
 					println("Dont have  varrock tabs");
 					return false;
 				}
 			}
 			else if (!useTabs){
 				if (!haveRune(1) || !haveRune(2) || !haveRune(3)){
 					println("Dont have  varrock runes");
 					return false;
 				}
 			}
 			//games.length == 0 || food.length == 0 || gem.length == 0 || vtab.length > 0){
 			if (currTask.equals( "banshees")){
 				if(ecto.length > 0 && (checkForHelm(1) || earmuffs.length > 0))
 					return true;
 				println("Dont have ecto or earmuffs");
 			}
 			else if (currTask.equals( "bats")){
 				//if(ftab.length > 0)
 					return true;
 			}
 			else if (currTask.equals( "bears")){
 				//if(atab.length > 0)
 				return true;
 					
 			}
 			else if (currTask.equals( "birds")){ // no idea atm
 				//if(atab.length > 0)
 					return true;
 			}
 			else if (currTask.equals( "cave_bugs")){
 				if(ltab.length > 0 && lightsource.length > 0 && (checkForHelm(0) || spiny.length > 0))
 					return true;
 			}
 			else if (currTask.equals( "cave_crawlers")){
 				if(ctab.length > 0 && anti.length > 0)
 					return true;
 			}
 			else if (currTask.equals( "cave_slimes")){
 				if(tinder.length > 0 && ltab.length > 0 && lightsource.length > 0 && anti.length > 0 && (checkForHelm(0) || spiny.length > 0))
 					return true;
 			}
 			else if (currTask.equals( "cows")){
 				//if(ftab.length > 0)
 					return true;
 			}
 			else if (currTask.equals( "crawling_hands")){
 				if(ecto.length > 0)
 					return true;
 			}
 			else if (currTask.equals( "dogs")){
 				//if(atab.length > 0)
 					return true;
 			}
 			else if (currTask.equals( "dwarves")){ 
 				//if(vtab.length > 0)
 					return true;
 			}
 			else if (currTask.equals( "ghosts")){
 				//if(ftab.length > 0)
 					return true;
 			}
 			else if (currTask.equals( "goblins")){
 				//if(ltab.length > 0)
 					return true;
 			}
 			else if (currTask.equals( "icefiends")){
 				//if(ftab.length > 0)
 					return true;
 			}
 			else if( currTask.equals( "kalphite")){
 				return true;
 			}
 			else if (currTask.equals( "minotaurs")){ 
 				//if(vtab.length > 0)
 					return true;
 			}
 			else if (currTask.equals( "monkeys")){ 
 				//if(ftab.length > 0)
 					return true;
 			}
 			else if (currTask.equals( "rats")){
 				//if(vtab.length > 0)
 					return true;
 			}
 			else if (currTask.equals( "scorpions")){ 
 				return true;
 			}
 			else if (currTask.equals( "skeletons")){
 				//if(vtab.length > 0)
 					return true;
 			}
 			else if (currTask.equals( "spiders")){
 				//if(ltab.length > 0)
 					return true;
 			}
 			else if (currTask.equals( "werewolves")){
 				if(ecto.length > 0)
 					return true;
 			}
 			else if (currTask.equals( "wolves")){ 
 				//if(ctab.length > 0)
 					return true;
 			}
 			else if (currTask.equals( "desert_lizards")){
 				if(waterskin.length > 0 && cooler.length > 0)
 					return true;
 			}
 			else if (currTask.equals( "zombies")){ 
 				//if(vtab.length > 0)
 					return true;
 			}
 			return false;
 		}
 	}
 	
 	
 	
 	//, Bat, Bear, Birds, , Cave Slimes, Cows, 
 	//Dog, Dwarf, Ghost, Goblin, Icefiend, , Minotaur, Monkey, Giant rat or Rat, 
 	//, Skeleton, Spider, , Wolf, Desert Lizard, and Zombie
 	
 	//Done
 	//Banshee Cave Crawlers Crawling Hand Kalphite Scorpion Werewolf
 	
 	RSTile bansheesT = new RSTile(3442, 3545, 0); //673
 	RSTile[] bansheeArea = { new RSTile(3431, 3567, 0), new RSTile(3453, 3531, 0)};
 	RSTile[] slayerTowerArea = {new RSTile(3405, 3579, 0), new RSTile(3453, 3532, 0)};
 	
 	RSTile slayerTowerEntranceT = new RSTile(3428, 3535, 0);
 	RSTile crawlingHandsT = new RSTile(3417, 3557, 0); //714
 	RSTile[] toCanafis = { new RSTile(3659, 3524, 0), new RSTile(3657, 3531, 0), 
 			new RSTile(3651, 3532, 0), new RSTile(3645, 3533, 0), new RSTile(3639, 3533, 0), 
 			new RSTile(3630, 3534, 0), new RSTile(3623, 3534, 0), new RSTile(3616, 3534, 0), 
 			new RSTile(3610, 3534, 0), new RSTile(3604, 3534, 0), new RSTile(3597, 3534, 0), 
 			new RSTile(3591, 3532, 0), new RSTile(3584, 3530, 0), new RSTile(3578, 3529, 0), 
 			new RSTile(3570, 3529, 0), new RSTile(3562, 3528, 0), new RSTile(3553, 3528, 0), 
 			new RSTile(3544, 3528, 0), new RSTile(3537, 3527, 0), new RSTile(3529, 3527, 0), 
 			new RSTile(3520, 3523, 0), new RSTile(3513, 3520, 0), new RSTile(3511, 3512, 0), 
 			new RSTile(3507, 3503, 0), new RSTile(3504, 3500, 0), new RSTile(3501, 3496, 0),
 			new RSTile(3498, 3492, 0), new RSTile(3494, 3489, 0) };
 	RSTile[] toSlayerTower = { new RSTile(3659, 3524, 0), new RSTile(3657, 3531, 0), 
 			new RSTile(3651, 3532, 0), new RSTile(3645, 3533, 0), new RSTile(3639, 3533, 0), 
 			new RSTile(3630, 3534, 0), new RSTile(3623, 3534, 0), new RSTile(3616, 3534, 0), 
 			new RSTile(3610, 3534, 0), new RSTile(3604, 3534, 0), new RSTile(3597, 3534, 0), 
 			new RSTile(3591, 3532, 0), new RSTile(3584, 3530, 0), new RSTile(3578, 3529, 0),
 			new RSTile(3570, 3529, 0), new RSTile(3562, 3528, 0), new RSTile(3553, 3528, 0), 
 			new RSTile(3544, 3528, 0), new RSTile(3537, 3527, 0), new RSTile(3529, 3527, 0), 
 			new RSTile(3520, 3523, 0), new RSTile(3513, 3520, 0), new RSTile(3511, 3512, 0), 
 			new RSTile(3507, 3503, 0), new RSTile(3504, 3500, 0), new RSTile(3501, 3496, 0), 
 			new RSTile(3498, 3492, 0), new RSTile(3494, 3489, 0), new RSTile(3488, 3483, 0), 
 			new RSTile(3482, 3478, 0), new RSTile(3476, 3477, 0), new RSTile(3468, 3475, 0), 
 			new RSTile(3463, 3478, 0), new RSTile(3457, 3486, 0), new RSTile(3452, 3492, 0), 
 			new RSTile(3446, 3497, 0), new RSTile(3442, 3505, 0), new RSTile(3436, 3511, 0), 
 			new RSTile(3432, 3518, 0), new RSTile(3429, 3524, 0), new RSTile(3428, 3532, 0), 
 			new RSTile(3428, 3535, 0) };
 	RSTile[] toCrawlingHands = { new RSTile(3428, 3538, 0), new RSTile(3428, 3542, 0), 
 			new RSTile(3428, 3548, 0), new RSTile(3426, 3554, 0), new RSTile(3421, 3557, 0), 
 			new RSTile(3415, 3558, 0), new RSTile(3412, 3562, 0), new RSTile(3412, 3570, 0),
 			new RSTile(3418, 3570, 0) };
 	RSTile[] toBanshees = { new RSTile(3428, 3538, 0), new RSTile(3427, 3543, 0), 
 			new RSTile(3428, 3548, 0), new RSTile(3426, 3554, 0), new RSTile(3421, 3557, 0), 
 			new RSTile(3415, 3558, 0), new RSTile(3412, 3562, 0), new RSTile(3412, 3570, 0), 
 			new RSTile(3418, 3570, 0), new RSTile(3424, 3572, 0), new RSTile(3431, 3572, 0), 
 			new RSTile(3437, 3573, 0), new RSTile(3445, 3573, 0), new RSTile(3446, 3567, 0), 
 			new RSTile(3443, 3563, 0), new RSTile(3438, 3560, 0), new RSTile(3435, 3555, 0), 
 			new RSTile(3436, 3550, 0), new RSTile(3442, 3545, 0) };
 	RSTile[] betweenCanafisEctoArea = {new RSTile(3475, 3537, 0), new RSTile(3665, 3470, 0)};
 	RSTile[] canafisArea = {new RSTile(3470, 3510, 0), new RSTile(3520, 3466, 0)};
 	RSTile[] betweenCanafisSlayerTowerArea = {new RSTile(3400, 3537, 0), new RSTile(3475, 3470, 0)};
 	RSTile[] slayerTowerDoorArea = {new RSTile(3426, 3535, 0), new RSTile(3431, 3533, 0)};
 	RSTile[] awkwardBansheeSq = {new RSTile(3430, 3546, 0), new RSTile(3436, 3541, 0)};
 	RSTile[] awkwardBansheeSqPath = { new RSTile(3429, 3543, 0), new RSTile(3428, 3551, 0), new RSTile(3422, 3556, 0), 
 				new RSTile(3414, 3557, 0), new RSTile(3412, 3563, 0), new RSTile(3412, 3571, 0) };
 	
 	
 	RSTile lumbySwampT = new RSTile(3169, 3173, 0);
 	RSTile[] toSlimes = { new RSTile(3165, 9573, 0), new RSTile(3156, 9573, 0), 
 			new RSTile(3148, 9571, 0), new RSTile(3149, 9565, 0), new RSTile(3153, 9556, 0), 
 			new RSTile(3153, 9554, 0) };
 	RSTile caveSlimeT = new RSTile(3153, 9554, 0);
 	RSTile[] toLumbySwampArea = {new RSTile(3160, 3223, 0), new RSTile(3248, 3165, 0)};
 	RSTile[] swampArea = {new RSTile(3144, 9578, 0), new RSTile(3170, 9543, 0)};
 	RSTile[] caveSlimeArea = {new RSTile(9147, 9554, 0), new RSTile(9546, 9543, 0)};
 	RSTile[] caveSlimeArea2 = {new RSTile(3145, 9567, 0), new RSTile(3163, 9551, 0)};
 	
 	RSTile lumbySwampEntranceT = new RSTile(3169, 3172, 0);
 	RSTile afterLumbySwampEntranceT = new RSTile(3169, 9571, 0);
 	RSTile[] toCaveSlimeT = {new RSTile(3165, 9573, 0), new RSTile(3157, 9573, 0), 
 			new RSTile(3147, 9567, 0), new RSTile(3152, 9561, 0), new RSTile(3153, 9554, 0)};
 	RSTile nearedgeTrapdoorTile = new RSTile(3094, 3471, 0);
 	
 	RSTile[] caveBugArea = {new RSTile(3144, 9583, 0), new RSTile(3160, 9568, 0)};
 	RSTile[] toCaveBug = {new RSTile(3165, 9573, 0), new RSTile(3157, 9573, 0), new RSTile(3151, 9573, 0)};
 	
 	RSTile renekSlayerT = new RSTile(2796, 3614, 0);
 	RSTile afterCaveEntrance = new RSTile(2807, 10001, 0);
 	int CaveEntrance = 2292; // Enter
 	RSTile caveEntranceT = new RSTile(2796, 3615, 0);
 	RSTile renekSlayCave = new RSTile(2797, 3614, 0);
 	RSTile[] toCCinside = { new RSTile(2804, 10001, 0), new RSTile(2798, 9997, 0), new RSTile(2792, 9996, 0),
 			new RSTile(2788, 9996, 0) };
 	RSTile[] toRenekSlayCaveArea = {new RSTile(2633, 3640, 0), new RSTile(2810, 3470, 0)};
 	RSTile[] toRenekSlayCave = { new RSTile(2752, 3477, 0), new RSTile(2746, 3479, 0), new RSTile(2740, 3480, 0), 
 			new RSTile(2731, 3484, 0), new RSTile(2724, 3483, 0), new RSTile(2715, 3484, 0), 
 			new RSTile(2706, 3484, 0), new RSTile(2699, 3484, 0), new RSTile(2692, 3484, 0), 
 			new RSTile(2687, 3486, 0), new RSTile(2687, 3492, 0), new RSTile(2687, 3499, 0), 
 			new RSTile(2687, 3504, 0), new RSTile(2687, 3509, 0), new RSTile(2686, 3514, 0), 
 			new RSTile(2684, 3518, 0), new RSTile(2682, 3523, 0), new RSTile(2681, 3528, 0), 
 			new RSTile(2679, 3533, 0), new RSTile(2678, 3538, 0), new RSTile(2678, 3546, 0), 
 			new RSTile(2675, 3550, 0), new RSTile(2673, 3554, 0), new RSTile(2668, 3557, 0), 
 			new RSTile(2662, 3559, 0), new RSTile(2660, 3565, 0), new RSTile(2658, 3570, 0), 
 			new RSTile(2654, 3576, 0), new RSTile(2653, 3582, 0), new RSTile(2653, 3588, 0), 
 			new RSTile(2654, 3597, 0), new RSTile(2655, 3603, 0), new RSTile(2659, 3607, 0), 
 			new RSTile(2666, 3607, 0), new RSTile(2670, 3607, 0), new RSTile(2677, 3609, 0), 
 			new RSTile(2685, 3609, 0), new RSTile(2691, 3609, 0), new RSTile(2699, 3608, 0), 
 			new RSTile(2708, 3608, 0), new RSTile(2716, 3607, 0), new RSTile(2725, 3606, 0), 
 			new RSTile(2733, 3606, 0), new RSTile(2739, 3603, 0), new RSTile(2747, 3601, 0), 
 			new RSTile(2754, 3599, 0), new RSTile(2761, 3598, 0), new RSTile(2770, 3595, 0), 
 			new RSTile(2778, 3595, 0), new RSTile(2786, 3597, 0), new RSTile(2789, 3602, 0), 
 			new RSTile(2792, 3609, 0), new RSTile(2795, 3615, 0) };
 	RSTile[] caveCrawlerArea = {new RSTile(2778, 10004, 0), new RSTile(2812, 9988, 0)};
 	
 	
 	
 	RSTile caveCrawlerT = new RSTile(2788, 9995, 0);
 	String[] wereWolves = {"Yuri", "Zoja", "Lev", "Svetlana", "Boris", "Irina", "Sofiya", 
 			"Yadviga", "Nikita", "Fidelio", "Joseph", "Georgy"};
 	RSTile wereWolvesT = new RSTile(3494, 3489, 0);
 	RSTile duelAreaGateCloseT = new RSTile(3312, 3234, 0); //3197
 	RSTile scorpionT = new RSTile(3298, 3194, 0);
 
 	
 	RSTile shantyPassT = new RSTile(3303, 3117, 0);
 	RSTile[] afterShantyPassArea = {new RSTile(3297, 3115, 0), new RSTile(3315, 3106, 0)};
 	RSTile bankChestShantyT = new RSTile(3308, 3120, 0); //id is 2693
 	
 	RSTile camelotTeleSpot = new RSTile(2757, 3476, 0);
 	RSTile lumbyTeleT = new RSTile(3223, 3219, 0);
 	RSTile varrockTeleT = new RSTile(3211, 3424, 0);
 	RSTile ardougneTeleT = new RSTile(2661, 3303, 0);
 	
 	RSTile[] ectoTeleArea = {new RSTile(3651, 3529, 0), new RSTile(3667, 3510, 0)};
 	
 	
 	RSTile[] rodTeleSpotArea = {new RSTile(3312, 3239, 0), new RSTile(3318, 3231, 0)};
 	RSTile rodGateTile = new RSTile(3312, 3234, 0);
 	
 	
 	RSTile[] toKalphite = { new RSTile(3304, 3113, 0), new RSTile(3297, 3107, 0), 
 			new RSTile(3290, 3107, 0), new RSTile(3282, 3106, 0), new RSTile(3272, 3107, 0), 
 			new RSTile(3262, 3108, 0), new RSTile(3256, 3108, 0), new RSTile(3248, 3108, 0), 
 			new RSTile(3239, 3105, 0), new RSTile(3232, 3103, 0), new RSTile(3227, 3107, 0) };
 	RSTile tunnelTile = new RSTile(3227, 3108, 0);
 	RSTile inTunnel = new RSTile(3484, 9510, 2);
 	
 	RSTile[] kalphiteTunnel = {new RSTile(3475, 9530, 2), new RSTile(3515, 9506, 2) };
 	RSTile[] toKalphitePart2 = { new RSTile(3488, 9510, 2), new RSTile(3495, 9509, 2),
 			new RSTile(3498, 9515, 2), new RSTile(3503, 9520, 2), new RSTile(3507, 9522, 2) };
 	
 	// desert lizards
 	RSTile[] toDesertLizards = { new RSTile(3305, 3111, 0), new RSTile(3308, 3107, 0), 
 			new RSTile(3310, 3101,0), new RSTile(3315, 3098, 0), new RSTile(3322, 3094, 0), 
 			new RSTile(3330, 3092, 0), new RSTile(3338, 3090, 0), new RSTile(3345, 3089, 0), 
 			new RSTile(3352, 3086, 0), new RSTile(3360, 3083, 0), new RSTile(3366, 3082, 0), 
 			new RSTile(3373, 3082, 0), new RSTile(3382, 3081, 0), new RSTile(3388, 3076, 0),
 			new RSTile(3392, 3069, 0), new RSTile(3398, 3066, 0), new RSTile(3405, 3062, 0) };
 	
 	RSTile manHoleT = new RSTile(3237, 3457, 0);
 	RSTile afterManHoleT = new RSTile(3237, 3457, 0);
 	RSTile manHole = new RSTile(3237, 3458, 0);
 	RSTile ratsMiddle =  new RSTile(3237, 9867, 0);
 	
 	RSTile dwarfMiddle = new RSTile(3011, 3451, 0);
 	RSTile[] dwarfArea = { new RSTile(3989, 3475, 0), new RSTile(3070, 3415, 0)}; 
 	RSTile[] trickyDwarf = { new RSTile(3021, 3455, 0), new RSTile(3025, 3445, 0)}; 
 	RSTile[] dwarfAttackArea = { new RSTile(3003, 3462, 0), new RSTile(3020, 3442, 0)}; 
 	
 	
 	RSTile faladorTeleT = new RSTile(2965, 3376, 0);
 	
 	RSTile lowWallT = new RSTile(2935, 3355, 0);
 	RSTile lowWall = new RSTile(2938, 3355, 0);
 	RSTile afterLowWall = new RSTile(2934, 3355, 0);
 	RSTile tavernlyLadder = new RSTile(2884, 3396, 0);
 	RSTile tavernlyLadderT = new RSTile(2884, 3397, 0);
 	RSTile afterTavernlyLadderT = new RSTile(2884, 9798, 0);
 	RSTile[] toGhost = { new RSTile(2884, 9799, 0), new RSTile(2884, 9806, 0), 
 			new RSTile(2884, 9814, 0), new RSTile(2884, 9820, 0), new RSTile(2884, 9825, 0), 
 			new RSTile(2884, 9834, 0), new RSTile(2884, 9840, 0), new RSTile(2888, 9846, 0), 
 			new RSTile(2894, 9848, 0), new RSTile(2898, 9849, 0) };
 	RSTile ghostMiddle = new RSTile(2898, 9849, 0);
 	
 	RSTile[] toBat = { new RSTile(2884, 9799, 0), new RSTile(2884, 9806, 0), 
 			new RSTile(2884, 9814, 0), new RSTile(2884, 9820, 0), new RSTile(2884, 9825, 0), 
 			new RSTile(2884, 9834, 0), new RSTile(2884, 9840, 0), new RSTile(2888, 9846, 0), 
 			new RSTile(2894, 9848, 0), new RSTile(2898, 9849, 0), new RSTile(2904, 9849, 0), 
 			new RSTile(2911, 9849, 0), new RSTile(2914, 9841, 0), new RSTile(2914, 9835, 0), 
 			new RSTile(2915, 9833, 0), new RSTile(2914, 9828) };
 	RSTile batMiddle = new RSTile(2915, 9833, 0);
 	RSTile[] toTavernlyDungPath = { new RSTile(2928, 3357, 0), new RSTile(2925, 3363, 0), new RSTile(2921, 3370, 0), 
 			new RSTile(2914, 3373, 0), new RSTile(2907, 3377, 0), new RSTile(2901, 3381, 0), 
 			new RSTile(2895, 3386, 0), new RSTile(2892, 3391, 0), new RSTile(2885, 3392, 0), 
 			new RSTile(2884, 3396, 0) };
 	RSTile[] toTavLadderDown = {new RSTile(2877, 3403, 0), new RSTile(2935, 3346, 0)};
 	RSTile[] insideTavBatArea = {new RSTile(2880, 9853, 0), new RSTile(2931, 9794, 0)};
 	RSTile[] insideTavGhostArea = {new RSTile(2880, 9853, 0), new RSTile(2924, 9794, 0)};
 	
 	
 	RSTile bearsMiddle = new RSTile(2699, 3333, 0);
 	
 	RSTile dogGate = new RSTile(2636, 3307, 0);
 	RSTile dogsMiddle = new RSTile(2635, 3321, 0);
 	RSTile[] dogArea1 = {new RSTile(2626, 3315, 0), new RSTile(2642, 3308, 0)};
 	RSTile[] dogArea2 = {new RSTile(2624, 3331, 0), new RSTile(2642, 3315, 0)};
 	RSTile[] ardyArea = {new RSTile(2602, 3340), new RSTile(2690, 3265, 0) };
 	
 	
 	RSTile[] toWolfArea = { new RSTile(2735, 3495, 0), new RSTile(2866, 3428, 0)};
 	RSTile wolfMiddle = new RSTile(2847, 3481, 0);
 	RSTile[] wolfArea = { new RSTile(2837, 3491, 0), new RSTile(2850, 3468, 0)};
 	
 	RSTile varrockBankT = new RSTile(3183, 3437, 0);
 	RSTile[] burthorpeArea = {new RSTile(2893, 3566, 0), new RSTile(2937, 3530, 0)};
 	RSTile turaelT = new RSTile(2931, 3536, 0);
 	RSTile[] varrockArea = {new RSTile(3176, 3448, 0), new RSTile(3255, 3386, 0) };
 	RSTile[] toStrongholdArea = {new RSTile(3074, 3440, 0), new RSTile(3180, 3408, 0) };
 	RSTile strongHoldHole = new RSTile(3081, 3420, 0);
 	RSTile teleCenterT = new RSTile(1863, 5238, 0);
 	RSTile[] afterPortal = {new RSTile(1905, 5229, 0), new RSTile(1916, 5210, 0) };
 	RSTile[] afterPortal2 = {new RSTile(1911, 5209, 0), new RSTile(1912, 5207, 0) };
 	RSTile[] afterPortal3 = {new RSTile(1907, 5206, 0), new RSTile(1912, 5202, 0) };
 	RSTile[] afterPortal4 = {new RSTile(1904, 5204, 0), new RSTile(1906, 5203, 0) };
 	RSTile strongHoldDoor = new RSTile(1911, 5209, 0);
 	RSTile strongHoldDoor2 = new RSTile(1911, 5206, 0);
 	RSTile strongHoldDoor3 = new RSTile(1907, 5204, 0);
 	RSTile strongHoldDoor4 = new RSTile(1904, 5204, 0);
 	RSTile minotaurT = new RSTile(1898, 5196, 0);
 	RSTile[] minotaurArea = {new RSTile(1891, 5202, 0), new RSTile(1905, 5189, 0) };
 	RSTile[] pathToDoor1 = {new RSTile(1912, 5215,0), new RSTile(1912, 5210, 0)};
 	RSTile afterStrongHoldHole = new RSTile(1859, 5243, 0);
 	
 	RSTile goblinDoor = new RSTile(3246, 3244, 0);
 	RSTile goblinMiddle = new RSTile(3249, 3240, 0);
 	RSTile[] goblinArea = {new RSTile(3239, 3254, 0), new RSTile(3267, 3220, 0)};
 	RSTile[] goblinHut = {new RSTile(3243, 3248, 0), new RSTile(3248, 3244, 0)};
 	
 	RSTile iceFiendsMiddle = new RSTile(3004, 3474, 0);
 	RSTile[] toIceFieldsArea = {iceFiendsMiddle, toStrongholdArea[1] };
 	RSTile[] beforeToIceFiendsArea = {new RSTile(3002, 3488, 0), new RSTile(3033, 3462, 0) };
 	RSTile[] beforeDwarfArea = {new RSTile(3000, 3485, 0), new RSTile(3080, 3419, 0) };
 	
 	RSTile[] edgeTrapDoorArea = {new RSTile(3050, 3486, 0), varrockArea[0] };
 	RSTile edgeTrapdoorTile = new RSTile(3097, 3468, 0);
 	
 	RSTile cowGate = new RSTile(2923, 3292, 0);
 	//private RSTile[] fromGateToCows = { new RSTile(3256, 3266, 0), new RSTile(3257, 3274, 0), 
 	//		new RSTile(3256, 3279, 0), new RSTile(3254, 3287, 0) };
 	RSTile cowsMiddle = new RSTile(2925, 3283, 0);
 	
 	RSTile inBoat = new RSTile(2956, 3143, 1);
 	RSTile afterPortBoat = new RSTile(2958, 3146, 0);
 	RSTile monkeyT = new RSTile(2878, 3158, 0);
 	RSTile portBoatT = new RSTile(3027, 3217, 0);
 	RSTile[] toMonkeyArea = {new RSTile(2861, 3180, 0), new RSTile(2959, 3138, 0) };
 	RSTile[] fallyArea = {new RSTile(2936, 3390, 0), new RSTile(3030, 3323, 0) };
 	RSTile[] toBoatFromFally = {fallyArea[0], new RSTile(3032, 3203, 0) };
 	RSTile[] toMonkey = { new RSTile(2952, 3146, 0), new RSTile(2946, 3146, 0), new RSTile(2939, 3146, 0), 
 			new RSTile(2934, 3149, 0), new RSTile(2927, 3151, 0), new RSTile(2920, 3150, 0), 
 			new RSTile(2913, 3151, 0), new RSTile(2909, 3153, 0), new RSTile(2904, 3154, 0), 
 			new RSTile(2899, 3156, 0), new RSTile(2894, 3155, 0), new RSTile(2888, 3153, 0), 
 			new RSTile(2883, 3155, 0), new RSTile(2879, 3158, 0) };
 	
 	RSTile[] toGStrongholdArea = {new RSTile(2447, 3388, 0), ardyArea[1]};
 	RSTile strongholdDoorT = new RSTile(2461, 3380, 0);
 	RSTile afterStrongholdDoorT = new RSTile(2461, 3387, 0);
 	
 	RSTile birdMiddle = new RSTile(2378, 3433, 0);
 	RSTile[] toBirds = { new RSTile(2459, 3393, 0), new RSTile(2453, 3396, 0), new RSTile(2447, 3397, 0), 
 			new RSTile(2441, 3398, 0), new RSTile(2433, 3400, 0), new RSTile(2428, 3404, 0), 
 			new RSTile(2424, 3410, 0), new RSTile(2418, 3416, 0), new RSTile(2412, 3420, 0), 
 			new RSTile(2408, 3426, 0), new RSTile(2402, 3428, 0), new RSTile(2396, 3428, 0),
 			new RSTile(2388, 3427, 0), new RSTile(2382, 3429, 0), new RSTile(2378, 3433, 0) };
 	RSTile[] gnomeArea = {new RSTile(2373, 3450, 0), new RSTile(2467, 3383, 0)};
 	RSTile birdGateS = new RSTile(2380, 3425, 0);
 	RSTile birdGateN = new RSTile(2384, 3436, 0);
 	
 	RSTile[] bearArea = {new RSTile(2602, 3350, 0), new RSTile(2730, 3265, 0) };
 	
 	//TODO GOTO_TASK
 	public boolean GOTO_TASK(){
 		state="Going to task";
 		Mouse.setSpeed(General.random(140, 160));
 		sleep(100,150);
 		
 		if (currTask.equals( "banshees")){
 			
 			if(!inArea(awkwardBansheeSq[0], awkwardBansheeSq[1], pos()) && inArea(bansheeArea[0], bansheeArea[1], pos())){
 				return true;
 			}
 			else if (inArea(awkwardBansheeSq[0], awkwardBansheeSq[1], pos())){
 				Keyboard.pressKey((char) KeyEvent.VK_CONTROL);
 				Walking.walkPath(awkwardBansheeSqPath);
 				waitIsMovin();
 				Keyboard.releaseKey((char) KeyEvent.VK_CONTROL);
 			}
 			else if (inArea(slayerTowerDoorArea[0], slayerTowerDoorArea[1], pos())){
 				
 				if(!checkForHelm(1))
 					equipHelm(1);
 					
 				RSObject[] slayerDoor = Objects.getAt(slayerTowerEntranceT);
 				if(slayerDoor.length > 0){
 					//if(DynamicClicking.clickRSObject(slayerDoor[0],  "Open"))
 					clickRSObjectAt(slayerTowerEntranceT, "Open");
 						sleep(1000,1200);
 				}
 				Walking.walkPath(toBanshees);
 			}
 			else if(inArea(slayerTowerArea[0], slayerTowerArea[1], pos())){
 				
 				Walking.walkPath(toBanshees);
 				waitIsMovin();
 	
 			}
 			else if(inArea(betweenCanafisEctoArea[0], betweenCanafisEctoArea[1], pos())){
 				Walking.walkPath(toSlayerTower);
 				waitIsMovin();
 				
 			}
 			else if(inArea(canafisArea[0], canafisArea[1], pos())){
 				Walking.walkPath(toSlayerTower);
 				waitIsMovin();
 	
 			}
 			else if(inArea(betweenCanafisSlayerTowerArea[0], betweenCanafisSlayerTowerArea[1], pos())){
 				Walking.walkPath(toSlayerTower);
 				waitIsMovin();
 	
 			}
 			
 			else if(inArea(ectoTeleArea[0], ectoTeleArea[1], pos())){
 				Walking.walkPath(toSlayerTower);
 				waitIsMovin();
 				
 			}
 			else
 				useTeleport(5);
 			
 		}
 		
 		else if (currTask.equals( "bats")){
 			if(pos().distanceTo(faladorTeleT) <=10){
 				WebWalking.walkTo(lowWall);
 				waitIsMovin();
 			}
 			else if (pos().distanceTo(lowWall) <=2){
 				RSObject[] LOWWALL = Objects.getAt(lowWallT);
 				if (LOWWALL.length > 0){
 					Camera.setCameraAngle(General.random(40, 60));
 					if(LOWWALL[0].click("Climb-over")){
 						sleep(5000,6000);
 						Camera.setCameraAngle(General.random(100, 110));
 						//WebWalking.walkTo(tavernlyLadder);
 					}
 				}
 				
 			}
 			else if (pos().distanceTo(afterLowWall) <= 5){
 				Walking.walkPath(toTavernlyDungPath);
 				waitIsMovin();
 			}
 			else if (pos().distanceTo(tavernlyLadderT) <= 5){
 				RSObject[] TAVLAD = Objects.getAt(tavernlyLadderT);
 				if (TAVLAD.length > 0){
 					if(TAVLAD[0].click("Climb-down")){
 						sleep(4000,5000);
 					}
 				}
 			}
 			else if (inArea(toTavLadderDown[0], toTavLadderDown[1], pos())){
 				Walking.walkPath(toTavernlyDungPath);
 				waitIsMovin();
 			}
 			else if (pos().distanceTo(afterTavernlyLadderT) <=5){
 				Walking.walkPath(toBat);
 				waitIsMovin();
 			}
 			else if (inArea(fallyArea[0], fallyArea[1], pos())){
 				WebWalking.walkTo(lowWall);
 				waitIsMovin();
 			}
 			else if (pos().distanceTo(batMiddle) <=15)
 				return true;
 			else if (inArea(insideTavBatArea[0], insideTavBatArea[1], pos())){
 				Walking.walkPath(toBat);
 				waitIsMovin();
 			}
 			else
 				useTeleport(1);
 		}
 		
 		else if (currTask.equals( "bears")){
 			if(pos().distanceTo(bearsMiddle)<= 20)
 				return true;
 			else if (inArea(bearArea[0], bearArea[1], pos())) {
 				WebWalking.walkTo(bearsMiddle);
 				waitIsMovin();
 			} 
 			else if (inArea(ardyArea[0], ardyArea[1], pos())) {
 				WebWalking.walkTo(bearsMiddle);
 				waitIsMovin();
 			}
 			else
 				useTeleport(4);
 		}
 		
 		else if (currTask.equals( "birds")){ // no idea atm
 			
 			if(pos().distanceTo(birdMiddle) <=10){
 				return true;
 			}
 			else if (pos().distanceTo(afterStrongholdDoorT) <=2){
 				Walking.walkPath(toBirds);
 				waitIsMovin();
 			}
 			else if (pos().distanceTo(strongholdDoorT) <=4){
 				RSObject[] GATE = Objects.find(10, "Gate");
 				if(GATE.length > 0){
 					if(GATE[0].isOnScreen()){
 						if(GATE[0].click("Open"))
 							sleep(5000,6000);
 					}
 				}
 			}
 			else if (inArea(gnomeArea[0], gnomeArea[1], pos())){
 				Walking.walkPath(toBirds);
 				waitIsMovin();
 			}
 			else if(inArea(toGStrongholdArea[0], toGStrongholdArea[1], pos())){
 				WebWalking.walkTo(strongholdDoorT);
 				waitIsMovin();
 			}
 			else
 				useTeleport(4);
 			
 		}
 		
 		else if (currTask.equals( "cave_crawlers")){
 			if(inArea(caveCrawlerArea[0], caveCrawlerArea[1], pos()))
 				return true;
 			else if(pos().distanceTo(caveEntranceT) <= 5){
 				Camera.turnToTile(renekSlayCave);
 				sleep(200,300);
 				RSObject[] caveE = Objects.getAt(renekSlayCave);
 				if (caveE.length > 0){
 					if(caveE[0].isOnScreen() && caveE[0].click("Enter")){
 						sleep(1000,1200);
 						Walking.walkPath(toCCinside);
 						waitIsMovin();
 					}
 				}
 			}
 			
 			else if (pos().distanceTo(camelotTeleSpot) <=15){
 				//WebWalking.walkTo(caveCrawlerT);
 				Walking.walkPath(toRenekSlayCave);
 				waitIsMovin();
 			}
 			else if (inArea(toRenekSlayCaveArea[0], toRenekSlayCaveArea[1], pos())){
 				//WebWalking.walkTo(caveCrawlerT);
 				Walking.walkPath(toRenekSlayCave);
 				waitIsMovin();
 			}
 			else 
 				useTeleport(2);
 		}
 		
 		else if (currTask.equals( "cave_bugs")){
 			if(pos().distanceTo(lumbyTeleT) <= 7){
 				WebWalking.walkTo(lumbySwampT);
 				waitIsMovin();
 			}
 			else if(pos().distanceTo(lumbySwampT) <= 6){
 				RSObject[] darkHole = Objects.getAt(lumbySwampEntranceT);
 				if (darkHole.length > 0){
 					if(darkHole[0].isOnScreen()){
 						if(darkHole[0].click("Climb-down"))
 							sleep(2000,2300);
 					}
 				}
 			}
 			else if(pos().distanceTo(afterLumbySwampEntranceT) <=5){
 				if(!checkForHelm(0)){
 					equipHelm(0);
 					sleep(200,300);
 				}
 				Walking.walkPath(toCaveBug);
 				waitIsMovin();
 			}
 			else if (inArea(swampArea[0], swampArea[1], pos())){
 				if(!checkForHelm(0)){
 					equipHelm(0);
 					sleep(200,300);
 				}
 				Walking.walkPath(toCaveBug);
 				waitIsMovin();
 			}
 			else if (inArea(toLumbySwampArea[0], toLumbySwampArea[1], pos())){
 				WebWalking.walkTo(lumbySwampT);
 				waitIsMovin();
 			}
 			else if(inArea(caveBugArea[0], caveBugArea[1], pos()))
 				return true;
 			else
 				useTeleport(3);
 		}
 		
 		else if (currTask.equals( "cave_slimes")){
 			if(pos().distanceTo(lumbyTeleT) <= 7){
 				WebWalking.walkTo(lumbySwampT);
 				waitIsMovin();
 			}
 			else if(pos().distanceTo(lumbySwampT) <= 6){
 				RSObject[] darkHole = Objects.getAt(lumbySwampEntranceT);
 				if (darkHole.length > 0){
 					if(darkHole[0].isOnScreen()){
 						if(darkHole[0].click("Climb-down"))
 							sleep(2000,2300);
 					}
 				}
 			}
 			else if(pos().distanceTo(afterLumbySwampEntranceT) <=5){
 				if(!checkForHelm(0)){
 					equipHelm(0);
 					sleep(200,300);
 				}
 				Walking.walkPath(toSlimes);
 				waitIsMovin();
 			}
 			else if (inArea(swampArea[0], swampArea[1], pos())){
 				if(!checkForHelm(0)){
 					equipHelm(0);
 					sleep(200,300);
 				}
 				Walking.walkPath(toSlimes);
 				waitIsMovin();
 			}
 			else if (inArea(toLumbySwampArea[0], toLumbySwampArea[1], pos())){
 				WebWalking.walkTo(lumbySwampT);
 				waitIsMovin();
 			}
 			else if(inArea(caveSlimeArea[0], caveSlimeArea[1], pos()) || inArea(caveSlimeArea2[0], caveSlimeArea2[1], pos()))
 				return true;
 			else
 				useTeleport(3);
 		}
 		
 		else if (currTask.equals( "cows")){
 			if(pos().distanceTo(faladorTeleT) > 15){
 				WebWalking.walkTo(cowGate);
 				waitIsMovin();
 			}
 			else if (pos().distanceTo(cowGate) <= 5){
 				RSObject[] cowgate = Objects.getAt(cowGate);
 				if(cowgate.length > 0){
 					if(cowgate[0].isOnScreen()){
 						Camera.setCameraRotation(0);
 						Camera.setCameraAngle(General.random(40, 60));
 						if(cowgate[0].click("Open")){
 							sleep(500,1000);
 							Camera.setCameraAngle(General.random(100, 110));
 							WebWalking.walkTo(cowsMiddle);
 							waitIsMovin();
 						
 						}
 					}
 				}
 				else{
 					WebWalking.walkTo(cowsMiddle);
 					waitIsMovin();
 				}
 			}
 			else if (pos().distanceTo(cowsMiddle)<=15)
 				return true;
 			else
 				useTeleport(1);
 		}
 		
 		else if (currTask.equals( "crawling_hands")){
 			if(pos().distanceTo(crawlingHandsT)<=15){
 				return true;
 			}
 			else if (inArea(slayerTowerDoorArea[0], slayerTowerDoorArea[1], pos())){
 				
 				RSObject[] slayerDoor = Objects.getAt(slayerTowerEntranceT);
 				if(slayerDoor.length > 0){
 					//if(DynamicClicking.clickRSObject(slayerDoor[0],  "Open"))
 					clickRSObjectAt(slayerTowerEntranceT, "Open");
 						sleep(1000,1200);
 				}
 				Walking.walkPath(toCrawlingHands);
 			}
 			else if(inArea(slayerTowerArea[0], slayerTowerArea[1], pos())){
 				
 				Walking.walkPath(toCrawlingHands);
 				waitIsMovin();
 	
 			}
 			else if(inArea(betweenCanafisEctoArea[0], betweenCanafisEctoArea[1], pos())){
 				Walking.walkPath(toSlayerTower);
 				waitIsMovin();
 				
 			}
 			else if(inArea(canafisArea[0], canafisArea[1], pos())){
 				Walking.walkPath(toSlayerTower);
 				waitIsMovin();
 	
 			}
 			else if(inArea(betweenCanafisSlayerTowerArea[0], betweenCanafisSlayerTowerArea[1], pos())){
 				Walking.walkPath(toSlayerTower);
 				waitIsMovin();
 	
 			}
 			
 			else if(inArea(ectoTeleArea[0], ectoTeleArea[1], pos())){
 				Walking.walkPath(toSlayerTower);
 				waitIsMovin();
 				
 			}
 			else
 				useTeleport(5);
 		}
 		
 		else if (currTask.equals( "dogs")){
 			if(pos().distanceTo(ardougneTeleT) <= 10){
 				WebWalking.walkTo(dogGate);
 				waitIsMovin();
 			}
 			else if (pos().distanceTo(dogGate) <= 5){
 				RSObject[] doggate = Objects.getAt(dogGate);
 				if(doggate.length > 0){
 					if(doggate[0].isOnScreen()){
 						Camera.setCameraRotation(0);
 						Camera.setCameraAngle(General.random(40, 60));
 						if(doggate[0].click("Open")){
 							sleep(500,1000);
 							Camera.setCameraAngle(General.random(100, 110));
 							WebWalking.walkTo(dogsMiddle);
 							waitIsMovin();
 						
 						}
 					}
 				}
 				else{
 					WebWalking.walkTo(dogsMiddle);
 					waitIsMovin();
 				}
 			}
 			else if (inArea(ardyArea[0], ardyArea[1], pos())){
 				WebWalking.walkTo(dogGate);
 				waitIsMovin();
 			}
 			else if (inArea(dogArea1[0], dogArea1[1], pos()) || inArea(dogArea2[0], dogArea2[1], pos()))
 				return true;
 			else
 				useTeleport(4);
 		}
 		
 		else if (currTask.equals( "dwarves")){ 
 			if(pos().distanceTo(dwarfMiddle)<=15)
 				return true;
 			else if (inArea(dwarfArea[0], dwarfArea[1], pos())){
 				WebWalking.walkTo(dwarfMiddle);
 				waitIsMovin();
 			}
 			else if (inArea(beforeDwarfArea[0], beforeDwarfArea[1], pos())){
 				WebWalking.walkTo(dwarfMiddle);
 				waitIsMovin();
 			}
 			else if(inArea(varrockArea[0], varrockArea[1], pos())){
 				WebWalking.walkTo(beforeIceFiends);
 				waitIsMovin();
 			}
 			else if (inArea(toStrongholdArea[0], toStrongholdArea[1], pos())){
 				WebWalking.walkTo(beforeIceFiends);
 				waitIsMovin();
 			}
 			else 
 				useTeleport(0);
 			
 		}
 		
 		else if (currTask.equals( "ghosts")){
 			if(pos().distanceTo(faladorTeleT) <=10){
 				WebWalking.walkTo(lowWall);
 				waitIsMovin();
 			}
 			else if (pos().distanceTo(lowWall) <=2){
 				RSObject[] LOWWALL = Objects.getAt(lowWallT);
 				if (LOWWALL.length > 0){
 					Camera.setCameraAngle(General.random(40, 60));
 					if(LOWWALL[0].click("Climb-over")){
 						sleep(5000,6000);
 						Camera.setCameraAngle(General.random(100, 110));
 						//WebWalking.walkTo(tavernlyLadder);
 					}
 				}
 				
 			}
 			else if (pos().distanceTo(afterLowWall) <= 5){
 				Walking.walkPath(toTavernlyDungPath);
 				waitIsMovin();
 			}
 			else if (pos().distanceTo(tavernlyLadderT) <= 5){
 				RSObject[] TAVLAD = Objects.getAt(tavernlyLadderT);
 				if (TAVLAD.length > 0){
 					if(TAVLAD[0].click("Climb-down")){
 						sleep(4000,5000);
 					}
 				}
 			}
 			else if (pos().distanceTo(afterTavernlyLadderT) <=5){
 				Walking.walkPath(toGhost);
 				waitIsMovin();
 			
 			}
 			else if (inArea(toTavLadderDown[0], toTavLadderDown[1], pos())){
 				Walking.walkPath(toTavernlyDungPath);
 				waitIsMovin();
 			}
 			else if (inArea(fallyArea[0], fallyArea[1], pos())){
 				WebWalking.walkTo(lowWall);
 				waitIsMovin();
 			}
 			else if (pos().distanceTo(ghostMiddle) <=20)
 				return true;
 			else if (inArea(insideTavBatArea[0], insideTavBatArea[1], pos())){
 				Walking.walkPath(toGhost);
 				waitIsMovin();
 			}
 			else
 				useTeleport(1);
 		}
 		
 		else if (currTask.equals( "goblins")){
 			if(inArea(goblinArea[0], goblinArea[1], pos()))
 				return true;
 			else if(pos().distanceTo(lumbyTeleT) > 15)
 				useTeleport(3);
 			else {
 				WebWalking.walkTo(goblinMiddle);
 				waitIsMovin();
 			}
 			
 		}
 		
 		else if (currTask.equals( "icefiends")){
 			if(pos().distanceTo(iceFiendsMiddle)<=15)
 				return true;
 			else if (inArea(beforeToIceFiendsArea[0], beforeToIceFiendsArea[1], pos())){
 				Walking.walkPath(Walking.generateStraightPath(iceFiendsMiddle));
 				waitIsMovin();
 			}
 			else if(inArea(varrockArea[0], varrockArea[1], pos())){
 				WebWalking.walkTo(beforeIceFiends);
 				waitIsMovin();
 			}
 			else if (inArea(toStrongholdArea[0], toStrongholdArea[1], pos())){
 				WebWalking.walkTo(beforeIceFiends);
 				waitIsMovin();
 			}
 			else if (inArea(toIceFieldsArea[0], toIceFieldsArea[1], pos())){
 				WebWalking.walkTo(beforeIceFiends);
 				waitIsMovin();
 			}
 			else
 				useTeleport(0);
 			
 		}
 		
 		else if( currTask.equals( "kalphite")){
 						
 			if (inArea(rodTeleSpotArea[0], rodTeleSpotArea[1], pos())){
 				RSObject[] gate = Objects.findNearest(10, "Gate");
 				if(gate.length > 0){
 					if (clickOBJ(gate[0], "Open")){
 						sleep(100,150);
 						waitIsMovin();
 					}
 				}
 				WebWalking.walkTo(shantyPassT);
 				waitIsMovin();
 			
 			}
 			else if (inArea(afterShantyPassArea[0], afterShantyPassArea[1], pos())){
 				//WebWalking.walkTo(tunnelTile);
 				Walking.walkPath(toKalphite);
 				waitIsMovin();
 			}
 			else if (pos().distanceTo(shantyPassT) <= 6){
 				RSObject[] shanty = Objects.find(10, "Shantay pass");
 				
 				if (shanty.length > 0){
 					if (distanceFromTile(shanty[0].getPosition(), 5)) {
 						if (clickOBJ(shanty[0], "Go-through")){
 							sleep(100, 150);
 							waitIsMovin();
 							if (NPCChat.clickContinue(true)) {
 								sleep(100,150);
 								if (NPCChat.clickContinue(true)) {
 									sleep(100,150);
 									if (NPCChat.clickContinue(true)) {
 										sleep(100,150);
 									}
 								}
 							}
 						}
 					}
 					waitIsMovin();
 					sleep(2000,2300);
 				}
 			}
 			else if (pos().distanceTo(tunnelTile) <= 5){
 				RSObject[] tunnel = Objects.getAt(tunnelTile);
 				RSItem[] ROPE = Inventory.find(rope);
 				println("About to go down to kalphite");
 				if (tunnel.length > 0 && ROPE.length > 0) {
 					if (tunnel[0].getID() == 3828) {
 						if (clickOBJ(tunnel[0], "Climb-down")) {
 							sleep(2200, 2500);
 						}
 					}
 					else {
 						ROPE[0].click("Use");
 						sleep(100, 150);
 						if (clickOBJ(tunnel[0], "Use rope")) {
 							sleep(100, 150);
 							waitIsMovin();
 							sleep(100, 150);
 							tunnel = Objects.getAt(tunnelTile);
 							if (clickOBJ(tunnel[0], "Climb-down")) {
 								sleep(2200, 2500);
 							}
 						}
 					}
 				}
 				
 			}
 			else if (pos().distanceTo(inTunnel) <= 5){
 				println("Walking to kalphites");
 				Walking.walkPath(toKalphitePart2);
 				waitIsMovin();
 			}
 			else if (inArea(kalphiteTunnel[0], kalphiteTunnel[1], pos())){
 				if(pos().getY() > 9523){
 					
 					Walking.walkPath(Walking.generateStraightPath(new RSTile(2505, 9525, 2)));
 					waitIsMovin();
 				}
 				else{
 				Walking.walkPath(toKalphitePart2);
 				waitIsMovin();
 				}
 			}
 			else if (inArea(alkharidArea[0], alkharidArea[1], pos())){
 				WebWalking.walkTo(shantyPassT);
 				waitIsMovin();
 			}
 			else if (inArea(desertArea[0], desertArea[1], pos())){
 				Walking.walkPath(toKalphite);
 				waitIsMovin();
 			}
 			else if (pos().distanceTo(kalphiteMiddle) <= 15)
 				return true;
 			else
 				useROD();
 			
 		}
 		
 		else if (currTask.equals( "minotaurs")){ 
 			
 			if(pos().distanceTo(strongHoldHole) <=5){
 				clickRSObjectAt(strongHoldHole, "Climb-down");
 			}
 			else if(pos().distanceTo(teleCenterT) <=4){
 				clickRSObjectAt(teleCenterT, "Use");
 				sleep(1000,1200);
 			}
 			else if (pos().distanceTo(afterStrongHoldHole) <=3){
 				Walking.blindWalkTo(teleCenterT);
 				waitIsMovin();
 			}
 			else if (inArea(afterPortal4[0], afterPortal4[1], pos())){
 				clickRSObjectAt(strongHoldDoor4, "Open");
 				if(chatup())
 					chatting();
 			}
 			else if (inArea(afterPortal3[0], afterPortal3[1], pos())){
 				clickRSObjectAt(strongHoldDoor3, "Open");
 				if(chatup())
 					chatting();				
 			}
 			else if (inArea(afterPortal2[0], afterPortal2[1], pos())){
 				clickRSObjectAt(strongHoldDoor2, "Open");
 				if(chatup())
 					chatting();
 			}
 			else if (pos().distanceTo(strongHoldDoor) <=5){
 				clickRSObjectAt(strongHoldDoor, "Open");
 				if(chatup())
 					chatting();
 			}
 			else if (pos().distanceTo(new RSTile(1903, 5203, 0)) <=3)
 				Walking.blindWalkTo(minotaurT);
 			else if (inArea(afterPortal[0], afterPortal[1], pos())){
 				
 				Walking.walkPath(pathToDoor1);
 				waitIsMovin();
 			}
 			else if(inArea(toStrongholdArea[0], toStrongholdArea[1], pos()) 
 					|| inArea(varrockArea[0], varrockArea[1], pos())){
 				WebWalking.walkTo(strongHoldHole);
 				waitIsMovin();				
 			}
 			else if (pos().distanceTo(minotaurT) <=15)
 				return true;
 			else
 				useTeleport(0);
 		}
 		
 		else if (currTask.equals( "monkeys")){ 
 			RSNPC[] captain = NPCs.findNearest("Seaman Tobias");
 			RSNPC[] captain2 = NPCs.findNearest("Seaman Thresnor");
 			RSNPC[] captain3 = NPCs.findNearest("Seaman Lorris");
 			
 			if(pos().distanceTo(monkeyT) <= 15){
 				return true;
 			}
 			else if (pos().distanceTo(portBoatT) <= 5){
 				if(captain.length > 0 && captain[0].click("Pay-fare")){
 					if (NPCChat.clickContinue(true)) {
 						sleep(5000,6000);
 					}
 				}
 				else if(captain2.length > 0 && captain2[0].click("Pay-fare")){
 					if (NPCChat.clickContinue(true)) {
 						sleep(5000,6000);
 					}
 				}
 				else if(captain3.length > 0 && captain3[0].click("Pay-fare")){
 					if (NPCChat.clickContinue(true)) {
 						sleep(5000,6000);
 					}
 				}
 			}
 			else if (pos().distanceTo(afterPortBoat)<= 5 && pos().getPlane() == 0){
 				WebWalking.walkTo(monkeyT);
 				//Walking.walkPath(toMonkey);
 				waitIsMovin();
 			}
 			else if (pos().distanceTo(inBoat) <= 5){
 				RSObject[] gangplank = Objects.findNearest(10,  "Gangplank");
 				
 				if (gangplank.length > 0){
 					if(gangplank[0].click("Cross")){
 						sleep(3000,3500);
 					}
 				}
 			}
 			else if (inArea(fallyArea[0], fallyArea[1], pos())){
 				WebWalking.walkTo(portBoatT);
 				waitIsMovin();
 			}
 			
 			else if (inArea(toBoatFromFally[0], toBoatFromFally[1], pos())){
 				WebWalking.walkTo(portBoatT);
 				waitIsMovin();
 			}
 			else if (inArea(toMonkeyArea[0], toMonkeyArea[1], pos())){
 				WebWalking.walkTo(monkeyT);
 				//Walking.walkPath(toMonkey);
 				waitIsMovin();
 			}
 			else
 				useTeleport(1);
 		}
 		
 		else if (currTask.equals( "rats")){
 			if(pos().distanceTo(varrockTeleT) <= 10){
 				WebWalking.walkTo(manHoleT);
 				waitIsMovin();
 			}
 			else if (pos().distanceTo(manHole) <= 5){
 				RSObject[] MANHOLEclose = Objects.getAt(manHoleT); //881
 				if(MANHOLEclose.length > 0){
 					if(MANHOLEclose[0].isOnScreen()){
 						if(MANHOLEclose[0].click("Open")){
 							sleep(1000,1200);
 							MANHOLEclose = Objects.getAt(manHoleT);
 							MANHOLEclose[0].click("Climb-down");
 						}
 						else{
 							MANHOLEclose[0].click("Climb-down");
 						}
 						sleep(1000,1200);
 					}
 				}
 			}
 			else if(pos().distanceTo(afterManHoleT) <=6){
 				Walking.blindWalkTo(ratsMiddle);
 				waitIsMovin();
 			
 			}
 			else if (pos().distanceTo(ratsMiddle) <=15)
 				return true;
 			else
 				useTeleport(0);
 		}
 		
 		else if (currTask.equals( "scorpions")){ 
 			if (inArea(rodTeleSpotArea[0], rodTeleSpotArea[1], pos())){
 				RSObject[] gate = Objects.findNearest(10, "Gate");
 				if(gate.length > 0){
 					if (clickOBJ(gate[0], "Open")){
 						sleep(100,150);
 						waitIsMovin();
 					}
 				}
 				WebWalking.walkTo(shantyPassT);
 				waitIsMovin();
 			
 			}
 			else if (inArea(afterShantyPassArea[0], afterShantyPassArea[1], pos())){
 				WebWalking.walkTo(scorpionMiddle);
 				waitIsMovin();
 			
 			}
 			else if (pos().distanceTo(scorpionMiddle) <= 15)
 				return true;
 			else if (inArea(alkharidArea[0], alkharidArea[1], pos())){
 				WebWalking.walkTo(scorpionMiddle);
 				waitIsMovin();
 			}
 			else
 				useROD();
 		}
 		
 		else if (currTask.equals( "skeletons")){
 			if(pos().distanceTo(varrockTeleT) <= 10){
 				WebWalking.walkTo(nearedgeTrapdoorTile);
 				waitIsMovin();
 			}
 			if(pos().distanceTo(nearedgeTrapdoorTile) <=5){
 				RSObject[] edgeTrapdoor = Objects.getAt(edgeTrapdoorTile);
 				Camera.setCameraRotation(General.random(192, 244));
 				if (edgeTrapdoor.length > 0) {
 					if (edgeTrapdoor[0].isOnScreen()) {
 						
 									Point p = Projection.tileToScreen(edgeTrapdoorTile,  0);
 									sleep(200,300); 
 									Mouse.move(p);
 									if(Game.getUptext().equals("Open Trapdoor")){
 										Mouse.click(p,  1);
 										waitIsMovin();
 										sleep(1000, 1500);
 										Mouse.click(p,1);
 										waitIsMovin();
 										sleep(1000, 1500);
 									}
 									else{
 										Mouse.click(p,  1);
 										waitIsMovin();
 										sleep(1000, 1500);
 									}
 									
 									/*
 										if (edgeTrapdoor[0].click("Open")) {// clickOBJ(edgeTrapdoor[0],
 																			// "Open"))
 																			// {
 											waitIsMovin();
 											sleep(1000, 1500);
 											edgeTrapdoor = Objects.getAt(edgeTrapdoorTile);
 											// clickOBJ(edgeTrapdoor[0], "Climb-down");
 											edgeTrapdoor[0].click("Climb-down");
 											waitIsMovin();
 											sleep(1000, 1500);
 											Camera.setCameraRotation(General.random(0,
 													10));
 										}
 									}
 									else {
 										edgeTrapdoor[0].click("Climb-down");
 										waitIsMovin();
 										sleep(1000, 1500);
 										Camera.setCameraRotation(General.random(0, 10));
 									}*/
 						
 					}
 				}
 				
 			}
 			
 			
 			else if(distanceFromTile(edgeLadderTile, 4)){
 				
 				Walking.walkPath(toGate);
 				waitIsMovin();
 			
 			}
 			else if (distanceFromTile(edgeDungeonGateTile, 4)){
 				RSObject[] edgeGate = Objects.getAt(edgeDungeonGateTile);
 				if (edgeGate.length > 0){
 					Camera.setCameraAngle(General.random(40,50));
 					if (clickOBJ(edgeGate[0], "Open")){
 						sleep(100,150);
 						Camera.setCameraAngle(General.random(100,120));
 					}
 				}
 				else{
 					Walking.walkPath(toSkeletons);
 					waitIsMovin();
 				}
 			}
 			else if (inArea(afterEdgeGateArea[0], afterEdgeGateArea[1], pos())){
 				Walking.walkPath(toSkeletons);
 				waitIsMovin();
 			}
 			else if (inArea(toEdgeGateArea[0], toEdgeGateArea[1], pos())){
 				WebWalking.walkTo(edgeDungeonGateTile);
 				waitIsMovin();
 			}
 			else if (inArea(edgeTrapDoorArea[0], edgeTrapDoorArea[1], pos())){
 				WebWalking.walkTo(nearedgeTrapdoorTile);
 				waitIsMovin();
 			}
 			else if (inArea(toStrongholdArea[0], toStrongholdArea[1], pos())){
 				WebWalking.walkTo(nearedgeTrapdoorTile);
 				waitIsMovin();
 			}
 			else if (inArea(varrockArea[0], varrockArea[1], pos())){
 				WebWalking.walkTo(nearedgeTrapdoorTile);
 				waitIsMovin();
 			}	
 			else if (inArea(skeletonArea[0], skeletonArea[1], pos()))
 				return true;
 			else
 				useTeleport(0);
 		}
 		
 		else if (currTask.equals( "spiders")){
 			if(pos().distanceTo(spidersMiddle)<=15)
 				return true;
 			else if (inArea(spiderLumby[0], spiderLumby[1], pos())){
 				WebWalking.walkTo(spidersMiddle);
 				waitIsMovin();
 			}
 			else 
 				useTeleport(3);
 		}
 		
 		else if (currTask.equals( "werewolves")){
 			
 			if(inArea(canafisArea[0], canafisArea[1], pos()))
 				return true;
 			else if(inArea(betweenCanafisEctoArea[0], betweenCanafisEctoArea[1], pos())){
 				
 				Walking.walkPath(toCanafis);
 				waitIsMovin();
 				
 			}
 			else if(inArea(ectoTeleArea[0], ectoTeleArea[1], pos())){
 				Walking.walkPath(toCanafis);
 				waitIsMovin();
 			}				
 			else
 				useTeleport(5);
 			
 		}
 		
 		else if (currTask.equals( "wolves")){ 
 			if(pos().distanceTo(wolfMiddle)<=15)
 				return true;
 			else if (inArea(toWolfArea[0], toWolfArea[1], pos())){
 				WebWalking.walkTo(wolfMiddle);
 				waitIsMovin();
 			}
 			else
 				useTeleport(2);
 		}
 		
 		else if (currTask.equals( "desert_lizards")){
 			if (inArea(rodTeleSpotArea[0], rodTeleSpotArea[1], pos())){
 				RSObject[] gate = Objects.findNearest(10, "Gate");
 				if(gate.length > 0){
 					if (clickOBJ(gate[0], "Open")){
 						sleep(100,150);
 						waitIsMovin();
 					}
 				}
 				WebWalking.walkTo(shantyPassT);
 				waitIsMovin();
 			
 			}/*
 			else if (pos().distanceTo(new RSTile(3307, 3235, 0)) <=4){
 				WebWalking.walkTo(shantyPassT);
 				waitIsMovin();
 			}*/
 			else if (inArea(afterShantyPassArea[0], afterShantyPassArea[1], pos())){
 				//WebWalking.walkTo(toDesertLizards[toDesertLizards.length]);
 				Walking.walkPath(toDesertLizards);
 				waitIsMovin();
 			
 			}
 			else if (pos().distanceTo(shantyPassT) <= 6){
 				RSObject[] shanty = Objects.find(10, "Shantay pass");
 				
 				if (shanty.length > 0){
 					if (distanceFromTile(shanty[0].getPosition(), 5)) {
 						if (clickOBJ(shanty[0], "Go-through")){
 							sleep(100, 150);
 							waitIsMovin();
 							if (NPCChat.clickContinue(true)) {
 								sleep(100,150);
 								if (NPCChat.clickContinue(true)) {
 									sleep(100,150);
 									if (NPCChat.clickContinue(true)) {
 										sleep(100,150);
 									}
 								}
 							}
 						}
 					}
 					waitIsMovin();
 					sleep(2000,2300);
 				}
 			}
 			else if (pos().distanceTo(toDesertLizards[toDesertLizards.length -1]) <= 15)
 				return true;
 			else
 				useROD();
 			
 		}
 		
 		else if (currTask.equals( "zombies")){ 
 			if(pos().distanceTo(varrockTeleT) <= 10){
 				WebWalking.walkTo(nearedgeTrapdoorTile);
 				waitIsMovin();
 			}
 			if(pos().distanceTo(nearedgeTrapdoorTile) <=5){
 				RSObject[] edgeTrapdoor = Objects.getAt(edgeTrapdoorTile);
 				Camera.setCameraRotation(General.random(192, 244));
 				
 				if (distanceFromTile(nearedgeTrapdoorTile, 4)){
 					if(edgeTrapdoor.length > 0){
 						if (edgeTrapdoor[0].isOnScreen()) {
 							Point p = Projection.tileToScreen(edgeTrapdoorTile,  0);
 							sleep(200,300); 
 							Mouse.move(p);
 							if(Game.getUptext().equals("Open Trapdoor")){
 								Mouse.click(p,  1);
 								waitIsMovin();
 								sleep(1000, 1500);
 								Mouse.click(p,1);
 								waitIsMovin();
 								sleep(1000, 1500);
 							}
 							else{
 								Mouse.click(p,  1);
 								waitIsMovin();
 								sleep(1000, 1500);
 							}
 							
 							/*
 								if (edgeTrapdoor[0].click("Open")) {// clickOBJ(edgeTrapdoor[0],
 																	// "Open"))
 																	// {
 									waitIsMovin();
 									sleep(1000, 1500);
 									edgeTrapdoor = Objects.getAt(edgeTrapdoorTile);
 									// clickOBJ(edgeTrapdoor[0], "Climb-down");
 									edgeTrapdoor[0].click("Climb-down");
 									waitIsMovin();
 									sleep(1000, 1500);
 									Camera.setCameraRotation(General.random(0,
 											10));
 								}
 							}
 							else {
 								edgeTrapdoor[0].click("Climb-down");
 								waitIsMovin();
 								sleep(1000, 1500);
 								Camera.setCameraRotation(General.random(0, 10));
 							}*/
 						}
 					}
 				}
 				 
 			}
 			
 			
 			else if(distanceFromTile(edgeLadderTile, 4)){
 				
 				Walking.walkPath(toGate);
 				waitIsMovin();
 			
 			}
 			else if (distanceFromTile(edgeDungeonGateTile, 4)){
 				RSObject[] edgeGate = Objects.getAt(edgeDungeonGateTile);
 				if (edgeGate.length > 0){
 					Camera.setCameraAngle(General.random(40,50));
 					if (clickOBJ(edgeGate[0], "Open")){
 						sleep(100,150);
 						Camera.setCameraAngle(General.random(100,120));
 					}
 				}
 				else{
 					Walking.walkPath(toZombies);
 					waitIsMovin();
 				}
 			}
 			else if (inArea(afterEdgeGateArea[0], afterEdgeGateArea[1], pos())){
 				Walking.walkPath(toZombies);
 				waitIsMovin();
 			}
 			else if (inArea(toEdgeGateArea[0], toEdgeGateArea[1], pos())){
 				WebWalking.walkTo(edgeDungeonGateTile);
 				waitIsMovin();
 			}
 			else if (inArea(edgeTrapDoorArea[0], edgeTrapDoorArea[1], pos())){
 				WebWalking.walkTo(nearedgeTrapdoorTile);
 				waitIsMovin();
 			}
 			else if (inArea(toStrongholdArea[0], toStrongholdArea[1], pos())){
 				WebWalking.walkTo(nearedgeTrapdoorTile);
 				waitIsMovin();
 			}
 			else if (inArea(varrockArea[0], varrockArea[1], pos())){
 				WebWalking.walkTo(nearedgeTrapdoorTile);
 				waitIsMovin();
 			}			
 			else if (inArea(zombieArea[0], zombieArea[1], pos()))
 				return true;
 			else
 				useTeleport(0);
 		}
 		
 		return false;
 	}
 	//TODO isAtTask
 	public boolean isAtTask(){
 		state = "Check for task area";
 		if (currTask.equals( "banshees")){
 			if(!inArea(awkwardBansheeSq[0], awkwardBansheeSq[1], pos()) && 
 					inArea(bansheeArea[0], bansheeArea[1], pos()))
 				return true;			
 		}
 		
 		else if (currTask.equals( "bats")){
 			if (pos().distanceTo(batMiddle) <=15)
 				return true;
 		}
 		
 		else if (currTask.equals( "bears")){
 			if(pos().distanceTo(bearsMiddle)<= 20)
 				return true;
 			 
 		}
 		
 		else if (currTask.equals( "birds")){ // no idea atm
 			if(pos().distanceTo(birdMiddle) <=10)
 				return true;
 		}
 		
 		else if (currTask.equals( "cave_bugs")){
 			if(inArea(caveBugArea[0], caveBugArea[1], pos()))
 				return true;
 		}
 		
 		else if (currTask.equals( "cave_crawlers")){
 			if(inArea(caveCrawlerArea[0], caveCrawlerArea[1], pos()))
 				return true;
 		}
 		
 		else if (currTask.equals( "cave_slimes")){
 			if(inArea(caveSlimeArea[0], caveSlimeArea[1], pos()) || inArea(caveSlimeArea2[0], caveSlimeArea2[1], pos()))
 				return true;
 		}
 		
 		else if (currTask.equals( "cows")){
 			if (pos().distanceTo(cowsMiddle)<=15)
 				return true;
 		}
 		
 		else if (currTask.equals( "crawling_hands")){
 			if(pos().distanceTo(crawlingHandsT)<=15)
 				return true;	
 		}
 		
 		else if (currTask.equals( "dogs")){
 			if (inArea(dogArea1[0], dogArea1[1], pos()) || inArea(dogArea2[0], dogArea2[1], pos()))
 				return true;
 		}
 		
 		else if (currTask.equals( "dwarves")){ 
 			if(inArea(dwarfAttackArea[0], dwarfAttackArea[1], pos()))
 				return true;
 		}
 		
 		else if (currTask.equals( "ghosts")){
 			if (pos().distanceTo(ghostMiddle) <=20)
 				return true;
 		}
 		
 		else if (currTask.equals( "goblins")){
 			if(inArea(goblinArea[0], goblinArea[1], pos()))
 				return true;
 		}
 		
 		else if (currTask.equals( "icefiends")){
 			if(pos().distanceTo(iceFiendsMiddle)<=15)
 				return true;
 		}
 		
 		else if( currTask.equals( "kalphite")){
 			if (pos().distanceTo(kalphiteMiddle) <= 15)
 				return true;
 		}
 		
 		else if (currTask.equals( "minotaurs")){ 
 			if(inArea(minotaurArea[0], minotaurArea[1], pos()))
 				return true;
 		}
 		
 		else if (currTask.equals( "monkeys")){ 
 			
 			if(pos().distanceTo(monkeyT) <=15)
 				return true;
 		}
 		
 		else if (currTask.equals( "rats")){
 			if (pos().distanceTo(ratsMiddle) <=15)
 				return true;
 		}
 		
 		else if (currTask.equals( "scorpions")){ 
 			if (pos().distanceTo(scorpionMiddle) <= 15)
 				return true;
 		}
 		
 		else if (currTask.equals( "skeletons")){
 			if (inArea(skeletonArea[0], skeletonArea[1], pos()))
 				return true;
 		}
 		
 		else if (currTask.equals( "spiders")){
 			if(pos().distanceTo(spidersMiddle)<=15)
 				return true;
 		}
 		
 		else if (currTask.equals( "werewolves")){
 			if(inArea(canafisArea[0], canafisArea[1], pos()))
 				return true;
 		}
 		
 		else if (currTask.equals( "wolves")){ 
 			if (pos().distanceTo(wolfMiddle) <= 15)
 				return true;
 		}
 		
 		else if (currTask.equals( "desert_lizards")){ 	
 			if (pos().distanceTo(toDesertLizards[toDesertLizards.length - 1]) <= 25)
 				return true;
 		}
 		
 		else if (currTask.equals( "zombies")){ 
 			if (inArea(zombieArea[0], zombieArea[1], pos()))
 				return true;
 		}
 		
 		return false;
 	}
 
 	
 	
 	//TODO useTeleport
 	public boolean useTeleport(int option){
 		state = "Teleporting...";
 		RSItem[] vtab = Inventory.find(VTAB);
 		RSItem[] ftab = Inventory.find(FTAB);
 		RSItem[] ctab = Inventory.find(CTAB);
 		RSItem[] ltab = Inventory.find(LTAB);
 		RSItem[] atab = Inventory.find(ATAB);
 		RSItem[] ecto = Inventory.find(ECTO);
 		RSItem[] ROD = Inventory.find(rod);
 		RSItem[] games = Inventory.find(gamesNecklace);
 		
 		int tabOrRune = 1; 
 		
 		switch (option) {
 		case 0:
 			if (useTabV) tabOrRune = 0;
 			if (tabOrRune == 0) {
 				if (vtab.length > 0) {
 					if (vtab[0].click("Break")) {
 						sleep(3000, 4000);
 						return true;
 					}
 				}
 			} 
 			else {
 				if (haveRune(1) && haveRune(2) && haveRune(3)) {
 					GameTab.open(org.tribot.api2007.GameTab.TABS.MAGIC);
 					sleep(200, 300);
 
 					RSInterface vtele = Interfaces.get(192, 15);
 					if (vtele.click("Cast Varrock Teleport"))
 						sleep(3000, 3500);
 					return true;
 				}
 			}
 			break;
 		case 1:
 			if (useTabF) tabOrRune = 0;
 			if (tabOrRune == 0) {
 				if (ftab.length > 0) {
 					if (ftab[0].click("Break")) {
 						sleep(3000, 4000);
 						return true;
 					}
 				}
 			} 
 			else {
 				if (haveRune(1) && haveRune(2) && haveRune(4)) {
 					GameTab.open(org.tribot.api2007.GameTab.TABS.MAGIC);
 					sleep(200, 300);
 
 					RSInterface ftele = Interfaces.get(192, 21);
 					if (ftele.click("Cast Falador Teleport"))
 						sleep(3000, 3500);
 					return true;
 				}
 			}
 			break;
 		case 2:
 			if (useTabC) tabOrRune = 0;
 			if (tabOrRune == 0) {
 				if (ctab.length > 0) {
 					if (ctab[0].click("Break")) {
 						sleep(3000, 4000);
 						return true;
 					}
 				}
 			} 
 			else {
 				if (haveRune(1) && haveRune(2)) {
 					GameTab.open(org.tribot.api2007.GameTab.TABS.MAGIC);
 					sleep(200, 300);
 
 					RSInterface ctele = Interfaces.get(192, 26);
 					if (ctele.click("Cast Camelot Teleport"))
 						sleep(3000, 3500);
 					return true;
 				}
 			}
 			break;
 		case 3:
 			if (useTabL) tabOrRune = 0;
 			if (tabOrRune == 0) {
 				if (ltab.length > 0) {
 					if (ltab[0].click("Break")) {
 						sleep(3000, 4000);
 						return true;
 					}
 				}
 			} 
 			else {
 				if (haveRune(1) && haveRune(2) && haveRune(0)) {
 					GameTab.open(org.tribot.api2007.GameTab.TABS.MAGIC);
 					sleep(200, 300);
 
 					RSInterface ltele = Interfaces.get(192, 18);
 					if (ltele.click("Cast Lumbridge Teleport"))
 						sleep(3000, 3500);
 					return true;
 				}
 			}
 			break;
 		case 4:
 			if (useTabA) tabOrRune = 0;
 			if (tabOrRune == 0) {
 				if (atab.length > 0) {
 					if (atab[0].click("Break")) {
 						sleep(3000, 4000);
 						return true;
 					}
 				}
 			} 
 			else {
 				if (haveRune(1) && haveRune(4)) {
 					GameTab.open(org.tribot.api2007.GameTab.TABS.MAGIC);
 					sleep(200, 300);
 
 					RSInterface atele = Interfaces.get(192, 32);
 					if (atele.click("Cast Ardougne Teleport"))
 						sleep(3000, 3500);
 					return true;
 				}
 			}
 			break;
 		case 5:
 			if (ecto.length > 0) {
 				if (ecto[0].click("Empty")) {
 					sleep(7000, 8000);
 					waitIsMovin();
 					return true;
 				}
 			}
 			break;
 		case 6:
 			if (ROD.length > 0) {
 				useROD();
 				return true;
 			}
 			break;
 		case 7:
 			if (games.length > 0) {
 				useGameNeck();
 				return true;
 			}
 			break;
 		}
 		return false;
 	}
 	
 	//TODO checkLootInArea
 	public boolean checkLootInArea(RSGroundItem loot){
 		RSTile loots = loot.getPosition();
 		
 		if (currTask.equals("banshees")) {
 			if (!inArea(awkwardBansheeSq[0], awkwardBansheeSq[1], loots)
 					&& inArea(bansheeArea[0], bansheeArea[1], loots))
 				return true;
 		}
 
 		else if (currTask.equals("bats")) {
 			if (loots.distanceTo(batMiddle) <= 15)
 				return true;
 		}
 
 		else if (currTask.equals("bears")) {
 			if (loots.distanceTo(bearsMiddle) <= 20)
 				return true;
 
 		}
 
 		else if (currTask.equals("birds")) {
 			if (loots.distanceTo(birdMiddle) <= 10)
 				return true;
 		}
 
 		else if (currTask.equals("cave_bugs")) {
 			if (inArea(caveBugArea[0], caveBugArea[1], loots))
 				return true;
 		} else if (currTask.equals("cave_crawlers")) {
 			if (inArea(caveCrawlerArea[0], caveCrawlerArea[1], loots))
 				return true;
 		}
 
 		else if (currTask.equals("cave_slime")) {
 			if (inArea(caveSlimeArea[0], caveSlimeArea[1], loots)
 					|| inArea(caveSlimeArea2[0], caveSlimeArea2[1], loots)) {
 				return true;
 			}
 		} else if (currTask.equals("cows")) {
 			if (loots.distanceTo(cowsMiddle) <= 15)
 				return true;
 		}
 
 		else if (currTask.equals("crawling_hands")) {
 			if (loots.distanceTo(crawlingHandsT) <= 15)
 				return true;
 		}
 
 		else if (currTask.equals("dogs")) {
 			if (inArea(dogArea1[0], dogArea1[1], loots)
 					|| inArea(dogArea2[0], dogArea2[1], loots))
 				return true;
 		}
 
 		else if (currTask.equals("dwarves")) {
 			if (!inArea(trickyDwarf[0], trickyDwarf[1], loots)
 					&& inArea(dwarfAttackArea[0], dwarfAttackArea[1], loots))
 				return true;
 		}
 
 		else if (currTask.equals("ghosts")) {
 			if (loots.distanceTo(ghostMiddle) <= 20)
 				return true;
 		}
 
 		else if (currTask.equals("goblins")) {
 			if (inArea(goblinArea[0], goblinArea[1], loots)
 					&& !inArea(goblinHut[0], goblinHut[1], loots))
 				return true;
 		}
 
 		else if (currTask.equals("icefiends")) {
 			if (loots.distanceTo(iceFiendsMiddle) <= 15)
 				return true;
 		}
 
 		else if (currTask.equals("kalphite")) {
 			if (loots.distanceTo(kalphiteMiddle) <= 15)
 				return true;
 		}
 
 		else if (currTask.equals("minotaurs")) {
 			if (inArea(minotaurArea[0], minotaurArea[1], loots))
 				return true;
 		}
 
 		else if (currTask.equals("monkeys")) {
 
 			if (loots.distanceTo(monkeyT) <= 15)
 				return true;
 		}
 
 		else if (currTask.equals("rats")) {
 			if (loots.distanceTo(ratsMiddle) <= 15)
 				return true;
 		}
 
 		else if (currTask.equals("scorpions")) {
 			if (loots.distanceTo(scorpionMiddle) <= 15)
 				return true;
 		} 
 		else if (currTask.equals("skeletons")) {
 			if (inArea(skeletonArea[0], skeletonArea[1], loots))
 				return true;
 		} 
 		else if (currTask.equals("spiders")) {
 			if (loots.distanceTo(spidersMiddle) <= 15)
 				return true;
 		}
 
 		else if (currTask.equals("werewolves")) {
 			if (inArea(canafisArea[0], canafisArea[1], loots))
 				return true;
 		} 
 		else if (currTask.equals("wolves")) {
 			if (inArea(wolfArea[0], wolfArea[1], loots))
 				return true;
 		} 
 		else if (currTask.equals("desert_lizards")) {
 			if (loots.distanceTo(toDesertLizards[toDesertLizards.length - 1]) <= 25)
 				return true;
 		}
 
 		else if (currTask.equals("zombies")) {
 			if (inArea(zombieArea[0], zombieArea[1], loots))
 				return true;
 		}
 
 		return false;
 	}
 	
 	@Override
 	public void clanMessageRecieved(String arg0, String arg1) {
 		
 
 	}
 
 	@Override
 	public void playerMessageRecieved(String arg0, String arg1) {
 		
 
 	}
 	// TODO serverMessaging
 	@Override
 	public void serverMessageRecieved(String message) {
 		int currLeftToKill;
 		
 		if(message.startsWith("You're assigned to kill")){
 			String[] arr = message.split(" ");
 			if (arr.length == 10){
 				currLeftToKill = Integer.parseInt(arr[6]);
 				currTask = arr[4].substring(0, arr[4].length() - 1);
 				
 				if (numLeftToKill > currLeftToKill) {
 
 					lastServerMessage = message;
 
 				}
 				numLeftToKill = currLeftToKill;
 			}
 			else{ // cave crawlers, cave slimes, desert lizards
 				currLeftToKill = Integer.parseInt(arr[7]);
 				currTask = arr[4] + "_" + arr[5].substring(0, arr[5].length() - 1);
 
 				if (numLeftToKill > currLeftToKill) {
 
 					lastServerMessage = message;
 
 				}
 				numLeftToKill = currLeftToKill;
 			}
 		}
 		else if (message.equals("You've completed your task; return to a Slayer master.")){
 			currTask = "None";
 			numLeftToKill = 0;
 		}
 		else if (message.equals("You need something new to hunt.")){
 			currTask = "None";
 			numLeftToKill = 0;
 		}
 		else if (message.endsWith("You have been poisoned!")){
 			useAnti();
 			sleep(1000,1200);
 		}
 	}
 	//TODO waitTillDead
 	public void waitTillDead(RSNPC npc){
 		boolean hi = false;
 		for (int i = 0; i < 800; i++, sleep(30, 40)) {
 			if(getHp() < 40)
 				eatFood();
 			
 			if (currTask.equals("desert_lizards") && npc.getHealth() < 3 && npc.isInteractingWithMe() && npc.isValid()) {
 				RSItem[] cooler = Inventory.find(COOLER);
 				if (cooler.length > 0) {
 					if (cooler[0].click("Use")) {
 						sleep(300, 400);
 						npc.click("Use Ice cooler");
 						sleep(2000, 2200);
 					}
 				}
 			}
 			if (npc == null || Player.getRSPlayer().getInteractingCharacter() == null || !npc.isInteractingWithMe() ){// || npc[0].getCombatCycle() > 0)) {
 				
 				println("npc is dead");
 				hi = true;
 				
 				break;
 			}
 		}
 		if (!hi)
 			println("timeout");
 	}
 	
 	public int isNotFighting(RSNPC[] npc){
 		for (int i = 0; i < 57; i++, sleep(30, 40)) {
 			if (npc.length > 0 && !npc[0].isInteractingWithMe() && !inCombat() && npc[0].getCombatCycle() <= 0) {
 				return 0;
 			}
 			else if (npc.length > 1 && !npc[1].isInteractingWithMe() && !inCombat() && npc[1].getCombatCycle() <= 0) {
 				return 1;
 			}
 		}
 		return -1;
 	}
 
 	public void	useAnti(){
 		RSItem[] antiP = Inventory.find(antiPoison);
 		if(antiP.length > 0){
 			if(antiP[0].click("Drink")){
 				println("Just drank a dose of antipoison");
 				sleep(500,1000);
 			}
 		}
 		
 	}
 	
 	public boolean isFull() {
 		// Mouse.setSpeed(General.random(150, 160));
 		sleep(5, 7);
 		return Inventory.isFull();
 	}
 
 	public void cameraDrag() {
 		Camera.setCameraRotation(General.random(88, 92));
 	}
 
 	public void cameraPortal() {
 		Camera.setCameraRotation(General.random(178, 182));
 	}
 
 	public void cameraBank() {
 		Camera.setCameraRotation(General.random(0, 2));
 	}
 
 	public boolean inCombat() {
 		sleep(5, 7);
 		return Player.getRSPlayer().isInCombat();
 	}
 
 	public int getHp() {
 		double x = Skills.getCurrentLevel("Constitution");
 		double y = Skills.getActualLevel("Constitution");
 		return (int) ((x / y) * 100);
 	}
 
 	public RSTile pos() {
 		return Player.getPosition();
 	}
 
 	public boolean inArea(RSTile nw, RSTile se, RSTile pos) {
 
 		int posX = pos.getX();
 		int posY = pos.getY();
 		int posP = pos.getPlane();
 		int nwX = nw.getX();
 		int nwY = nw.getY();
 		int seX = se.getX();
 		int seY = se.getY();
 		int nwP = nw.getPlane();
 		int seP = se.getPlane();		
 
 		if (posX >= nwX && posX <= seX && posY >= seY && posY <= nwY
 				&& posP == nwP && posP == seP) {
 			return true;
 		} else
 			return false;
 	}
 
 	public void checkXP() {
 
 		GameTab.open(org.tribot.api2007.GameTab.TABS.STATS);
 		sleep(80, 100);
 		Mouse.moveBox(557, 307, 600, 321);
 		sleep(1700, 2200);
 		GameTab.open(org.tribot.api2007.GameTab.TABS.INVENTORY);
 		sleep(80, 100);
 
 	}
 
 	public void checkFriends() {
 
 		GameTab.open(org.tribot.api2007.GameTab.TABS.FRIENDS);
 		sleep(80, 100);
 		Mouse.moveBox(616, 240, 664, 261);
 		sleep(1700, 2200);
 		GameTab.open(org.tribot.api2007.GameTab.TABS.INVENTORY);
 		sleep(80, 100);
 	}
 
 	private void ANTIBAN() {
 
 		int x = General.random(1, 2);
 		if (x == 1)
 			checkXP();
 		else
 			checkFriends();
 		antiban = System.currentTimeMillis();
 	}
 
 	private boolean clickNPC(RSNPC npc, String option) {
 		//Mouse.setSpeed(General.random(100,120));
 		RSTile loc = null;
 		if (npc != null && npc.isOnScreen()) {
 			loc = npc.getPosition();
 			Mouse.move(Projection.tileToScreen(loc, 10));
 			if (Game.isUptext(option)) {
 				Mouse.click(1);
 				return true;
 			} else {
 				Mouse.click(3);
 				if (ChooseOption.isOpen()) {
 					ChooseOption.select(option);
 				}
 			}
 		}
 		return false;
 	}
 
 	
 	private boolean clickOBJ(RSObject npc, String option) {
 		Camera.turnToTile(npc.getPosition());
 		// Mouse.setSpeed(170);
 		if (npc.isOnScreen()) {
 			Point[] points = npc.getModel().getAllVisiblePoints();
 
 			if (points.length > 0) {
 				Mouse.move(points[points.length / 2]);
 				// Mouse.move(new Point(points[0].x, points[0].y));
 				if (Game.isUptext(option)) {
 					Mouse.click(1);
 					return true;
 				} else {
 					Mouse.click(3);
 					if (ChooseOption.isOpen()) {
 						ChooseOption.select(option);
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	public void drawModel(RSModel model, Graphics g, boolean fill) {
 		if (model.getAllVisiblePoints().length != 0) {
 			if (fill) {
 				// fill triangles
 				for (Polygon p : model.getTriangles()) {
 					g.fillPolygon(p);
 				}
 			} else {
 				// draw triangles
 				for (Polygon p : model.getTriangles()) {
 					g.drawPolygon(p);
 				}
 			}
 		}
 	}
 	
 	public void drawPath(RSTile[] path, Graphics g, boolean minimap) {
 		Point[] points = new Point[path.length];
 		if (minimap) {
 			// Drawing on the other whatever
 			for (int i = 0; i < path.length - 1; i++) {
 				Point tilePoint = Projection.tileToMinimap(path[i]);
 				if (Projection.isInMinimap(tilePoint)) {
 					if (i < path.length - 1) {
 						Point nextPoint = Projection.tileToMinimap(path[i + 1]);
 						if (Projection.isInMinimap(nextPoint)) {
 							g.drawLine(tilePoint.x, tilePoint.y, nextPoint.x,
 									nextPoint.y);
 						}
 					}
 					g.fillOval(tilePoint.x, tilePoint.y, 3, 3);
 				}
 			}
 		} else {
 			for (int i = 0; i < path.length - 1; i++) {
 				if (path[i].isOnScreen()) {
 					if (i > 0) { // could just start index
 						Point currentTile = Projection.tileToScreen(path[i], 0);
 						Point lastTile = Projection
 								.tileToScreen(path[i - 1], 0);
 						g.drawLine(currentTile.x, currentTile.y, lastTile.x,
 								lastTile.y);
 					}
 					//g.setColor("#FF0000");
 					drawTile(path[i], g, false);
 				}
 			}
 		}
 	}
 	
 	public void drawTile(RSTile tile, Graphics g, boolean fill) {
 		if (tile.isOnScreen()) {
 			if (fill) {
 				g.fillPolygon(Projection.getTileBoundsPoly(tile, 0));
 			} else {
 				g.drawPolygon(Projection.getTileBoundsPoly(tile, 0));
 			}
 		}
 	}
 	
 	public boolean isMovin() {
 
 		return Player.isMoving();
 	}
 	
 	public void waitIsMovin(){
 		
 		for(int i = 0; i < 57; i++, sleep(30, 40)){
 			if (!isMovin())
 				break;
 		}
 	}
 
 	public boolean gotFood() {
 
 		return Inventory.getCount(foodID) > 0;
 	}
 
 	public int lootCount(int ID) {
 
 		return Inventory.getCount(ID);
 	}
 
 	// improved waiting for loot
 	private void waitForInv(int lootID) {
 		int k = 0;
 
 		while (k < 200	&& Player.isMoving()) {
 			sleep(80);
 			k++;
 		}
 	}
 
 	public boolean clickModel(RSModel model, String option, boolean rightClick)
 
 	{
 
 		Point[] points = model.getAllVisiblePoints();
 		int length = points.length;
 		if (length != 0) {
 			Point p = points[//General.random(0, length - 1)];
 			                 length / 2];
 			Mouse.move(p);
 			{
 				String top = org.tribot.api2007.Game.getUptext();
 				if (top.contains(option)) {
 					Mouse.click(p, 0);
 					return true;
 				} 
 				else if (rightClick) {
 					Mouse.click(3);
 					Timer t = new Timer(500);
 					while (t.isRunning() && !ChooseOption.isOpen())
 						sleep(50, 100);
 					if (!ChooseOption.isOpen())
 						return false;
 					if (ChooseOption.select(option))
 						return true;
 				}
 			}
 		}
 		return false;
 	}
 	int BANK_SECTION = 0;
 	boolean WITHDRAWAL_FAIL_SAFE = true;
 
 	/** Withdraws any items from bank if found
 	* 
 	* @param a (The amount to withdraw)
 	* @param b (The id as a single integer or an array of integers)
 	* 
 	* By: Platinum Force Scripts
 	* 
 	**/
 	public void withdraw(int count, int... ids){
 		
 		int[] countBefore = new int[ids.length];
 		int[] stackBefore = new int[ids.length];
 		
 		for(int i=0; i<ids.length; i++){
 			countBefore[i] = Inventory.getCount(ids[i]);
 			stackBefore[i] = 0;
 			RSItem[] item = Inventory.find(ids[i]);
 			if(item.length > 0)
 				stackBefore[i] = item[0].getStack();
 		}
 		
 		int section,scrollTo,itemX,itemY;
 		int currentItem = 0;
 		int currentItemCount = 0;
 		
 		for(int i=0; i<ids.length; i++){
 			
 			RSItem[] items = Banking.find(ids[i]);
 			
 			//If the item was not found, skip to next item
 			if(items.length == 0) continue;
 			
 			if(ids[i] != currentItem){
 				currentItem = ids[i];
 				currentItemCount = 1;
 			}
 			else currentItemCount++;
 			
 			//If you have tried to withdraw the current item 4 times, continue to the next item 
 			if(currentItemCount == 4)
 				continue;
 			
 			//"section" is the section of the bank (each section has 6 rows starting from the top)
 			//The math is: (item's position in bank) divided by (8 items per row) divided by (6 rows per section)
 			//This determines which section of the bank to scroll to to find the item
 			section = (int)Math.floor((items[0].getIndex())/8.0/6.0);
 			
 			//"itemX" is the actual x position of the item after scrolling to it's section in the bank
 			itemX = (int)Math.ceil((items[0].getIndex())%8);
 			
 			//"itemY" is the actual y position of the item after scrolling to it's section in the bank
 			itemY = (int)(Math.floor(items[0].getIndex()/8)%6);
 			
 			if(section > 7){
 				section = 8;
 				scrollTo = 260;
 				itemY = 4+(int)(Math.ceil(items[0].getIndex()/8)-48);
 			}
 			else {
 				//"scrollTo" is the pixel (y offset) to scroll to, or to click (On the banks scroll bar)
 				scrollTo = (int)Math.round((section * 23.5) + 86);
 			}
 			
 			//This determines if an item is located in the section of the bank that you already scrolled to.
 			//If so, there is no reason to scroll the the same spot again, you're already there!
 			//Whenever you close your bank reset "BANK_SECTION" to zero!
 			if(section != BANK_SECTION){
 				Mouse.moveBox(469, scrollTo, 481, scrollTo);
 				Mouse.click(Mouse.getPos(), 1);
 				BANK_SECTION = section;
 				sleep(150);
 			}
 			
 			//Withdraw 1
 			if(count == 1){
 				Mouse.move(80+(itemX*47)+General.random(11, 21), 59+(itemY*38)+General.random(15, 23));
 				Mouse.click(Mouse.getPos(), 1);
 			}
 			//Widthdraw 5 or 10
 			else if(count == 5 || count == 10){
 				for(int k=0; k<4; k++){
 					Mouse.move(80+(itemX*47)+General.random(11, 21), 59+(itemY*38)+General.random(15, 23));
 					if(selectOption("Withdraw "+count))
 						break;
 				}
 			}
 			//Withdraw All
 			else if(count == 0){
 				for(int k=0; k<4; k++){
 					Mouse.move(80+(itemX*47)+General.random(11, 21), 59+(itemY*38)+General.random(15, 23));
 					if(selectOption("Withdraw All"))
 						break;
 				}
 			}
 			else { 
 				//Withdraw X
 				boolean withdrawn = false;
 				while(bankIsOpen() && !withdrawn){
 					for(int k=0; k<4; k++){
 						Mouse.move(80+(itemX*47)+General.random(11, 21), 59+(itemY*38)+General.random(15, 23));
 						if(selectOption("Withdraw X")){
 							int cnt=0;
 							while(!enterAmount()){
 								if(cnt >= 20) break;
 								sleep(100);
 								cnt++;
 							}
 							if(enterAmount()){
 								Keyboard.typeString(count+"");
 								Keyboard.pressEnter();
 								withdrawn = true;
 								break;
 							}
 						}
 					}
 				}
 			}
 			
 			sleep(300);
 			
 			//If the inventory amount before has not changed for this item, you have failed to withdraw it.
 			//It will try again. And if the item isn't found in the bank, it will skip it.
 			
 			boolean countChanged = false;
 			boolean stackChanged = false;
 			
 			//Check if inventory item amount has changed within 2 seconds
 			for(int j=0; j<20; j++){
 				
 				int stackAfter = 0;
 				RSItem[] item = Inventory.find(ids[i]);
 				if(item.length > 0)
 					stackAfter = item[0].getStack();
 				
 				//Check if the amount of items has changed for non-stackable items
 				if(Inventory.getCount(ids[i]) > countBefore[i]){
 					countChanged = true;
 					break;
 				}
 				//Check if stack size has changed for stackable items
 				else if(stackAfter > stackBefore[i]){
 					stackChanged = true;
 					break;
 				}
 				sleep(100);
 			}
 			if(!countChanged && !stackChanged)
 				i--;
 			else if(ids.length > 1)
 				return;
 		}
 		return;
 	}
 	
 	/** Make sure an option is valid **/
 	
 	private boolean selectOption(String option){
 		
 		Point pos = Mouse.getPos();
 		for(int i=0; i<1; i++){				
 			Mouse.click(pos, 3);
 			sleep(100);
 			if(ChooseOption.isOptionValid(option) && ChooseOption.select(option))
 				return true;
 			else Mouse.move((int)Mouse.getPos().getX()-General.random(10, 20), (int)Mouse.getPos().getY()-General.random(50,60));
 		}
 		return false;
 	}
 
 
 	/** Check color at image coordinate **/
 	
 	private boolean enterAmount(){
 
 		BufferedImage asterisk = Screen.getGameImage().getSubimage(259, 429, 1, 1);
 		int[] r = new int[] {0,6};
 		int[] g = new int[] {0,6};
 		int[] b = new int[] {122,134};
 		
 		Color rgb = new Color(asterisk.getRGB(0,0));
 		int red = rgb.getRed();
 		int green = rgb.getGreen();
 		int blue = rgb.getBlue();
 		
 		//Check for colors between these values
 		if(red >= r[0] && red <= r[1] && green >= g[0] && green <= g[1] && blue >= b[0] && blue <= b[1])
 			return true;
 		
 		return false;
 	}
 
 
 	/** Open bank **/
 	
 	private boolean bankIsOpen(){
 		
 		//A method to check if the bank is open
 		return Banking.isBankScreenOpen();
 	}
 	
 	public boolean distanceFromTile(RSTile tile, int distance){
 		return pos().distanceTo(tile) <= distance;
 	}
 	
 	public void runAndTurnCamera(RSNPC mob){
 		Walking.walking_timeout = 500L;
 		Walking.clickTileMM(mob.getPosition(),  1);
 		Camera.turnToTile(mob.getPosition());
 	}
 	
 	public boolean mobNotOnScreen(RSNPC mob){
 		return !mob.isOnScreen();
 	}
 	
 	
 	public void emergTele(){
 		RSItem[] vtab = Inventory.find(VTAB);
 		
 		if (!useTabs){
 			println("going to varrock using runes");
 			useTeleport(0);
 		}
 		else if(vtab.length > 0)
 			vtab[0].click("Break");
 		else{
 			stopScript();
 			println("No more Varracok teletabs, you must have vteletabs");
 		}
 		sleep(4000,5000);
 	}
 	
 	private void chatting()
     {
         String ops[] = {
             "Don't give him my password.", "Don't tell them anything and inform Jagex through the game website.", "Nobody", "Game Inbox on the RuneScape website.", "The birthday of a famous person or event.", "Talk to any banker in RuneScape.", "Nowhere.", "Recovering your account if it is stolen.", "Use the 'Recover a Lost Password' section on the RuneScape website.", "Only on the RuneScape website.", 
             "Memorable", "No", "Politely tell them no and then use the 'Report Abuse' button.", "No, it might steal my password.", "Don't tell them anything and click the 'Report Abuse' button.", "Don't give them the information and send an 'Abuse report'.", "Every couple of months", "Virus scan my computer then change my password and recoveries.", "No.", "To help me recover my password if I forget it or it is stolen."
         };
         if(NPCChat.getClickContinueInterface() != null)
             NPCChat.clickContinue(true);
         sleep(1000,1200);
         if(Interfaces.get(230, 0) != null || Interfaces.get(228, 0) != null)
         {
             String ans[] = NPCChat.getOptions();
             if(ans.length > 0)
             {
                 for(int a = 0; a < ans.length; a++)
                 {
                     for(int i = 0; i < ops.length; i++)
                         if(ans[a].toString().equals(ops[i]))
                             NPCChat.selectOption(ops[i], true);
 
                 }
 
             }
         }
         sleep(1000,1200);
         if(NPCChat.getClickContinueInterface() != null)
             NPCChat.clickContinue(true);
         sleep(5000,6000);
     }
 		
 	private boolean chatup()  {
 		println("chat is up to check if we are a bot");
 		return Interfaces.get(242, 1) != null || Interfaces.get(244, 1) != null || Interfaces.get(243, 1) != null || Interfaces.get(230, 0) != null || Interfaces.get(228, 0) != null;
 	}
 
 	private boolean modsnear(){
 		RSPlayer allPlayers[] = Players.getAll();
 	        if(allPlayers.length > 0)
 	        {
 	            RSPlayer arsplayer[];
 	            int k = (arsplayer = allPlayers).length;
 	            for(int j = 0; j < k; j++)
 	            {
 	                RSPlayer i = arsplayer[j];
 	                if(i != null && i.getName() != null && i.getName().contains("Mod "))
 	                    return true;
 	            }
 
 	            return false;
 	        } else
 	        {
 	            return false;
 	        }
 	    }
 	boolean specon;
 	 private int specialPercent()   {
 	        if(specon)
 	            return Game.getSettingsArray()[300] / 10;
 	        else
 	            return 0;
 	    }
 	 
 	    private boolean specialSelected()
 	    {
 	        return Game.getSettingsArray()[301] == 1;
 	    }
 
 	    private void selectspecial()
 	    {
 	        GameTab.open(org.tribot.api2007.GameTab.TABS.COMBAT);
 	        if(GameTab.getOpen() == org.tribot.api2007.GameTab.TABS.COMBAT)
 	        {
 	            Mouse.clickBox(573, 412, 711, 432, 1);
 	            for(int f = 0; f < 2000 && !specialSelected() && specialPercent() == 100; f++)
 	                sleep(1L);
 
 	            for(int f = 0; f < 2000 && specialSelected() && specialPercent() == 100; f++)
 	                sleep(1L);
 
 	        }
 	    }
 	    
 	    
 	    
 	    
 	    
 	    
 	    
 	    /**** brian methods ****/
 	    //TODO getClosesetInactiveNPC
 	    public RSNPC getClosestInactiveNPC(String id) {
 	    	RSNPC[] npcs = NPCs.findNearest(id);
 	    	if(currTask.equals("werewolves"))
 	    		npcs = NPCs.findNearest(wereWolves);
 	    	if(npcs.length > 0)	{
 	    		for(int x = 0; x < npcs.length; x++){
 	    			if(npcs[x].isInteractingWithMe() && npcs[x].isValid()){ // not sure if this fixes anything
 	    				return npcs[x];
 	    			}
 	    			else if(currTask.equals("werewolves")){
 	    				if(inArea(canafisArea[0], canafisArea[1], npcs[x].getPosition())){
 	    					if(!npcs[x].isInCombat() && PathFinding.isTileWalkable(npcs[x].getAnimablePosition()))
 	    		    			return npcs[x];
 	    				}
 	    			}
 	    			else if (currTask.equals("bats")){
 	    				if(npcs[x].getPosition().distanceTo(batMiddle) <= 15){
 	    					
 	    					if(!npcs[x].isInCombat() && PathFinding.isTileWalkable(npcs[x].getAnimablePosition()))
 	    		    			return npcs[x];
 	    				}
 	    			}
 	    			else if(currTask.equals("wolves")){
 	    				if(inArea(wolfArea[0], wolfArea[1], npcs[x].getPosition())){
 	    					
 	    					if(!npcs[x].isInCombat() && PathFinding.isTileWalkable(npcs[x].getAnimablePosition()))
 	    		    			return npcs[x];
 	    				}
 	    			}
 	    			else if (currTask.equals("dogs")){
 	    				if(inArea(dogArea1[0], dogArea1[1], npcs[x].getPosition()) || inArea(dogArea2[0], 
 	    						dogArea2[1], npcs[x].getPosition())){
 	    					
 	    					if(!npcs[x].isInCombat() && PathFinding.isTileWalkable(npcs[x].getAnimablePosition()))
 	    		    			return npcs[x];
 	    				}
 	    			}
 	    			else if (currTask.equals("goblins")){
 	    				RSObject[] gDoor = Objects.getAt(goblinDoor);
 	    				if(inArea(goblinHut[0], goblinHut[1], npcs[x].getPosition()) && gDoor.length > 0){
 	    					if(gDoor[0].isOnScreen()){
 	    						clickRSObjectAt(goblinDoor, "Open");
 	    						sleep(1000,1200);
 	    					}
 	    				}
 	    				if(!npcs[x].isInCombat() && PathFinding.isTileWalkable(npcs[x].getAnimablePosition()))
     		    			return npcs[x];
 	    				
 	    			}
 	    			else if (currTask.equals("birds")){
 	    				RSObject[] gDoor1 = Objects.getAt(birdGateS);
 	    				RSObject[] gDoor2 = Objects.getAt(birdGateN);
 	    				if(gDoor1.length > 0 && pos().distanceTo(gDoor1[0].getPosition()) <=3){
 	    					if(gDoor1[0].isOnScreen()){
 	    						if(pos().distanceTo(birdGateS) <= 3){
 	    							clickRSObjectAt(birdGateS, "Open");
 	    							sleep(1000,1200);
 	    						}
 	    					}
 	    				}
 	    				else if(gDoor2.length > 0 && pos().distanceTo(gDoor2[0].getPosition()) <=3){
 	    					if(gDoor2[0].isOnScreen()){
 	    						if (pos().distanceTo(birdGateN) <= 3){
 	    							clickRSObjectAt(birdGateN, "Open");
 	    							sleep(1000,1200);
 	    						}
 	    					}
 	    				}		
 	    				
 	    				if(!npcs[x].isInCombat() && PathFinding.isTileWalkable(npcs[x].getAnimablePosition()))
     		    			return npcs[x];
 	    				
 	    			}
 	    			else if (currTask.equals("cows")){
 	    				RSObject[] gDoor = Objects.getAt(cowGate);
 	    				if(gDoor.length > 0 && pos().distanceTo(gDoor[0].getPosition()) <=3){
 	    					if(gDoor[0].isOnScreen()){
 	    						if(pos().distanceTo(cowGate) <= 3){
 	    							clickRSObjectAt(cowGate, "Open");
 	    							sleep(1000,1200);
 	    						}
 	    					}
 	    				}
 	    				if(!npcs[x].isInCombat() && PathFinding.isTileWalkable(npcs[x].getAnimablePosition()))
     		    			return npcs[x];
 	    			}
 	    			else if (currTask.equals("dwarves")){
 	    				if(!inArea(trickyDwarf[0], trickyDwarf[1], npcs[x].getPosition()) && inArea(dwarfAttackArea[0], 
 	    						dwarfAttackArea[1], npcs[x].getPosition())){
 	    					if(!npcs[x].isInCombat() && PathFinding.isTileWalkable(npcs[x].getAnimablePosition()))
 	    		    			return npcs[x];
 	    				}
 	    			}
 	    			else if(currTask.equals("skeletons")){
 	    				if(inArea(skeletonArea[0], skeletonArea[1], npcs[x].getPosition())){
 	    					if(!npcs[x].isInCombat() && PathFinding.isTileWalkable(npcs[x].getAnimablePosition()))
 	    		    			return npcs[x];
 	    				}
 	    			}
 	    			else if(currTask.equals("cave_slime")){
 	    				if(inArea(caveSlimeArea[0], caveSlimeArea[1], npcs[x].getPosition()) || inArea(caveSlimeArea2[0], caveSlimeArea2[1], 
 	    						npcs[x].getPosition())
 	    		    			){
 	    					if(!npcs[x].isInCombat() && PathFinding.isTileWalkable(npcs[x].getAnimablePosition()))
 	    		    			return npcs[x];
 	    				}
 	    			}
 	    			else if(currTask.equals("cave_bugs")){
 	    				if(inArea(caveBugArea[0], caveBugArea[1], npcs[x].getPosition())){
 	    					if(!npcs[x].isInCombat() && PathFinding.isTileWalkable(npcs[x].getAnimablePosition()))
 	    		    			return npcs[x];
 	    				}
 	    			}
 	    			
 	    			else if(!npcs[x].isInCombat() && PathFinding.isTileWalkable(npcs[x].getAnimablePosition()))
 	    				return npcs[x];
 	    		}
 	    	}
 	    	return null;
 	    }
 
 
 	    public RSNPC attackInactiveMonster(String id)   {
 	    	RSNPC monster = getClosestInactiveNPC(id);
 	    	if(monster != null)	{
 	    		if (monster.isOnScreen()){
 	    			if(DynamicClicking.clickRSModel(monster.getModel(), "Attack")){
 	    			//if(clickModel(monster.getModel(), "Attack", false)){
 	    				waitIsMovin();
 	    				waitTillDead(monster);
 	    				LOOT();
 	    				sleep(100,120);
 	    			}
 	    		}
 	    		else if (monster.getPosition().distanceTo(Player.getPosition()) <= 4){
 	    			Camera.turnToTile(monster.getPosition());
 	    			sleep(2000,2500);
 	    		}
 	    		else{
 	    			Walking.walkTo(monster.getPosition());
 	    			sleep(2000,2500);
 	    		}
 	    	}
 	    	return monster;
 	    }
 	    public boolean needsToEat(int percToEat)
 	    {
 	    	double currLevel = Skills.getCurrentLevel("Hitpoints");
 	    	int hitpointsLevel = Skills.getActualLevel("Hitpoints");
 	    	double decimal = currLevel / (double)(hitpointsLevel);
 	    	int percent = (int)(decimal * 100);
 	    	if (percent <= percToEat)
 	    		return true;
 	    	return false;
 	    }
 
 	    public void eatFood(int[] foodIds)
 	    {
 	    	RSItem[] food = Inventory.find(foodIds);
 	    	if(food.length > 0)
 	    	{
 	    		food[0].click("Eat");
 	    		sleep(400, 800);
 	    	}
 	    }
 	    public void clickRSObject(int distance, int id, String action)
 	    {
 	    	boolean successful = false;
 	    	while(!successful)
 	    	{
 	    		RSObject[] objects = Objects.find(distance, id);
 	    		if (objects.length > 0)
 	    		{
 	    			int r = Projection.angleToTile(objects[0].getAnimablePosition());
 	    			Camera.setCameraRotation(r + General.random(0, 90));
 	    			sleep(600, 800);
 	    			successful = DynamicClicking.clickRSObject(objects[0], action);
 	    			waitIsMovin();
 	    			sleep(1800, 2400);
 	    			
 	    		}
 	    	}
 	    }
 
 	    	
 	    public void clickRSObjectAt(RSTile tile, String action)
 	    {
 	    	boolean successful = false;
 	    	while(!successful)
 	    	{
 	    		RSObject[] objects = Objects.getAt(tile);
 	    		if(objects.length > 0)
 	    		{
 	    			int r = Projection.angleToTile(objects[0].getAnimablePosition());
 	    			Camera.setCameraRotation(r + General.random(0, 80));
 	    			sleep(600, 800);
 	    			successful = DynamicClicking.clickRSObject(objects[0], action);
 	    			sleep(1800, 2400);
 	    		}
 	    	}
 	    }
 	    
 	    public boolean checkForHelm(int option){
 			RSItem[] helm = Interfaces.get(387, 28).getItems();
 			int helmID = 0;
 			if(option == 0) // spiny
 				helmID = SPINY;
 			else if (option == 1) //earmuffs
 				helmID = EARMUFFS;
 			for (int i = 0; i < helm.length; i++) { // looping threw items equiped
 				if (helm[i].getID() == helmID) {
 					return true;
 				}
 			}
 			return false;
 		}
 	    
 	    
 	    public boolean equipHelm(int option){
 	    	
 	    	RSItem[] helm0 = Inventory.find(SPINY);
 	    	RSItem[] helm1 = Inventory.find(EARMUFFS);
 			
 			if(option == 0){ // spiny
 				if(helm0.length > 0)
 					if(helm0[0].click("Wear"))
 						return true;
 			}
 			else if (option == 1){ //earmuffs
 				if(helm1.length > 0)
 					if(helm1[0].click("Wear"))
 						return true;
 			}
 			
 			
 			return false;
 	    }
 	    
 	    //START: Code generated using Enfilade's Easel
 	    private final Color color1 = new Color(255, 255, 255);
 	    private final Color color2 = new Color(0, 0, 0);
 	    private final Color color3 = new Color(0, 204, 0);
 	    private final Color color4 = new Color(0, 0, 255);
 	    private final Color color5 = new Color(102, 102, 0);
 	    private final Color color6 = new Color(255, 0, 0);
 	    private final Color color7 = new Color(0, 0, 0);
 
 	    private final BasicStroke stroke1 = new BasicStroke(1);
 	    private final BasicStroke stroke2 = new BasicStroke(3);
 
 	    private final Font font1 = new Font("Arial", 3, 24);
 	    private final Font font2 = new Font("Arial", 0, 14);
 	    
 
 	    public void onRepaint(Graphics g1) {
 	        Graphics2D g = (Graphics2D)g1;
 	        g.setColor(color1);
 	        g.fillRect(7, 345, 505, 132);
 	        g.setColor(color2);
 	        g.setStroke(stroke1);
 	        g.drawRect(7, 345, 505, 132);
 	        g.setFont(font1);
 	        g.setColor(color3);
 	        g.drawString("Yaw hide's Easy Slayer", 130, 369);
 	        g.setFont(font2);
 	        g.setColor(color4);
 	        g.drawString("Slayer Exp/hr", 19, 399);
 	        g.drawString("Combat Exp/hr", 20, 422);
 	        g.drawString("State", 367, 432);
 	        g.setColor(color5);
 	        g.fillRoundRect(485, 347, 25, 22, 16, 16);
 	        g.setColor(color3);
 	        g.drawRoundRect(485, 347, 25, 22, 16, 16);
 	        g.setColor(color6);
 	        g.setStroke(stroke2);
 	        g.drawLine(489, 351, 509, 364);
 	        g.drawLine(506, 352, 489, 363);
 	        g.setColor(color4);
 	        g.drawString("Version", 368, 465);
 	        g.setColor(color7);
 	       
 	        
 	        long timeRan = System.currentTimeMillis() - start_time;
 			int xpGained = current_xp - startXp;
 			int xpGainedSlayer = current_xpSlayer - startXpSlayer;
 			double multiplier = timeRan / 3600000D;
 			int xpPerHour = (int) (xpGained / multiplier);
 			int xpPerHourSlay = (int) (xpGainedSlayer / multiplier);
 			double show_xp_to_lvrange = ((XpToLVrange / xpPerHour) * 60);
 			double show_xp_to_lvstr = ((XpToLVstr / xpPerHour) * 60);
 			double show_xp_to_lvatt = ((XpToLVatt / xpPerHour) * 60);
 			double show_xp_to_lvdef = ((XpToLVdef / xpPerHour) * 60);
 			double show_xp_to_lvhp = ((XpToLVhp / xpPerHour) * 60);
 			double show_xp_to_lvslayer = ((XpToLVslayer / xpPerHourSlay) * 60);
 
 			g.drawString(""+ version, 420, 465);
 			g.drawString("" + Timing.msToString(timeRan), 111, 460);
 			g.setFont(font2);
 	        g.setColor(color4);
 	        g.drawString("Time running ", 20, 460);
 	        g.setColor(color7);
 			g.drawString("" + xpGained + " (" + xpPerHour
 					+ "/h)", 120, 420);
 			g.drawString("" + xpGainedSlayer + " (" + xpPerHourSlay
 					+ "/h)", 113,400);
 			/*
 			g.drawString(""+ (int) (show_xp_to_lvslayer / 60)
 			+ " hours "	+ ((int) show_xp_to_lvslayer - ((int) (show_xp_to_lvslayer / 60)) * 60)
 							+ " mins.", 140, 400);*/
 			
 			
 			g.drawString("" + state, 406, 432);
 			
 			g.drawString("NumLeft: " + numLeftToKill, 366, 406);
 			g.drawString("CurrTask: "+currTask, 369, 385);
 			
 	    }
 	    //END: Code generated using Enfilade's Easel
 	   
 		@Override
 		public void onPaint(Graphics arg0) {
 						
 			if (showPaint) {
 				onRepaint(arg0);
 			}
 			
 			
 			
 		}
 		
 		// ALL PAINT STUFF
 		int startXp = Skills.getXP("Constitution") + Skills.getXP("Strength") 
 				+ Skills.getXP("Attack") + Skills.getXP("Defense") + Skills.getXP("Range");
 		int startXpSlayer = Skills.getXP("Slayer");
 		
 		double version;
 		int current_xp = 0;
 		int current_xpSlayer = 0;
 		final long start_time = System.currentTimeMillis();
 		
 		double XpToLVslayer = Skills.getXPToNextLevel("Slayer");
 		double XpToLVhp = Skills.getXPToNextLevel("Constitution");
 		double XpToLVatt = Skills.getXPToNextLevel("Attack");
 		double XpToLVstr = Skills.getXPToNextLevel("Strength");
 		double XpToLVdef = Skills.getXPToNextLevel("Defense");
 		double XpToLVrange = Skills.getXPToNextLevel("Range");
 		String state = "null";
 		boolean showPaint = true;
 		Rectangle hideZone = new Rectangle(488, 348, 509, 368);
 
 
 		public TuraelSlayer() {
 			version = ((ScriptManifest) getClass().getAnnotation(
 					ScriptManifest.class)).version();
 		}
 
 	@Override
 	public OVERRIDE_RETURN overrideKeyEvent(KeyEvent e) {
 		return OVERRIDE_RETURN.SEND;
 		/*** returning send to allow keys to be pressed, (obv) ***/
 	}
 
 	@Override
 	public OVERRIDE_RETURN overrideMouseEvent(MouseEvent e) {
 		try {
 			if (hideZone.contains(e.getPoint())) {
 				if (e.getID() == MouseEvent.MOUSE_CLICKED) {
 					e.consume();
 					if (!showPaint)
 						showPaint = true;
 					else
 						showPaint = false;
 					return OVERRIDE_RETURN.DISMISS;
 				} else if (e.getID() == MouseEvent.MOUSE_PRESSED)
 					return OVERRIDE_RETURN.DISMISS;
 			}
 			return OVERRIDE_RETURN.PROCESS;
 
 		} catch (Exception e2) {
 			return OVERRIDE_RETURN.DISMISS;
 		}
 	}
 
 	public void putMap() {
 		for (int i = 0; i < loot.length; i++) {
 			if (i >= names.length) {
 				map.put(loot[i], names[names.length - 1]);
 			} else
 				map.put(loot[i], names[i]);
 		}
 	}
 	
 	public void eatFood(){
 		RSItem [] food = Inventory.find(foodID);
 		if(food.length > 0){
 			food[0].click("Eat");
 			sleep(600,700);
 		}
 		else{
 			println("You are out of food in inventory, lets bank!");
 			emergTele();
 			sleep(600,700);
 		}
 		
 	}
 		
 }
