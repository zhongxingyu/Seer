 /*******************************************************
 * Class for game instances, will be associated with
 * sessions through being the session data.
 *******************************************************/
 
 import database.DBConnections;
 import java.io.PrintWriter;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.sql.*;
 //Pulled from inclass exmple
 import database.*;
 
 public class GameInstance {
     PlayerCharacter playerChar;
     AresCharacter aresChar;
     stateEnum currentState, startingState;
     String accountName;
     DBConnections dataSource = null;
     Connection conn = null;
     Statement stat = null;
     final int STORE_SIZE = 20;    
     int gold;
     int storeLevel;
     Item[] storeItems;
 
     
     int constantPtsPerLevel = 5;
     int constantWeaponPtsPerLevel = 3;
     int constantArmorPtsPerLevel = 5;
     int constantGoldPerLevel = 20;
 
     
     
     GameInstance()
     {
         playerChar = null;
         aresChar = null;
         currentState = stateEnum.INIT;
         accountName = "Unregistered User";
         int gold = 0;
         int storeLevel = 0;
         Item[] storeItems = new Item[STORE_SIZE];
     }
     
     /****************************************************
      * Connect to the database using class variables
      * SQL Commands
      ***************************************************/
     void connectDB(){
         dataSource = DBConnections.getInstance();  
         conn = dataSource.getConnection();
     }  
     
     /****************************************************
      * Disconnect from the database using class variables
      * SQL Commands
      ***************************************************/
     void disconnectDB(){
 
         DBUtilities.closeStatement(stat);
         dataSource.freeConnection(conn);
     }
     
     /****************************************************
      * Returns the result set result of your query
      * @param query the SQL query you want to execute
      * @param out the printwriter
      ***************************************************/
     ResultSet sqlQuery(String query, PrintWriter out){
         
         ResultSet result = null;
         try{
             connectDB();
              stat = conn.createStatement();
              result = stat.executeQuery(query);
         }catch(Exception ex){
            out.println("Query error:");
            out.println(ex);
         }finally{
             disconnectDB();
             return result;
         } 
     }
     /****************************************************
      * Returns true if your SQL command succeeded, 
      * returns false if it does not
      * @param command the SQL command you want to execute
      * @param out the printwriter
      ***************************************************/
     Boolean sqlCommand(String command, PrintWriter out){
         Boolean result = false;
         try{
             connectDB();
              stat = conn.createStatement();
              stat.execute(command);
              result = true;
              
         }catch(Exception ex){
             out.println("sqlCommand Exception: ");
             out.println(ex);
             
         }finally{
             DBUtilities.closeStatement(stat);
             disconnectDB();
             return result;
         } 
     }
     
     /****************************************************
      * state machine case switch function, called from 
      *      the servlet.
      * @param out output PrintWriter
      * @param request the servlet request
      ***************************************************/
     void advanceGame(PrintWriter out, HttpServletRequest request)
     {
         stateEnum nextState = currentState;
         startingState = currentState;
         do
         {
             currentState = nextState;
             switch(currentState)
             {
                 case INIT:
                     //first connection
                     nextState = initState(out, request);
                     break;
 
                 case BATTLE:
                     //battle function
                     nextState = battleState(out, request);
                     break;
 
                 case STORE:
                     //store function
                     nextState = storeState(out, request);
                     break;
 
                 case REGISTERED_CHARACTER_CREATION:
                     //character creation
                     nextState = registeredCharacterCreationState(out, request);
                     break;
 
                case UNREGISTERED_CHARACTER_CREATION:
                     //character creation
                     nextState = unregisteredCharacterCreationState(out, request);
                     break;
 
                 case IDLING:
                     //idle state
                     nextState = idling(out, request);
                     break;
 
                 case DECISION:
                     //this state is for asking what to do next
                     out.println("Decision");
                     nextState = decisionState(out, request);
                     break;
 
                 case BLACKSMITH:
                     //upgrade items
                     nextState = blackSmithState(out, request);
                     break;
                     
                 case LOGIN:
                     //login
                     nextState = loginState(out, request);
                     break;
                     
                 case ACCOUNT_CREATION:
                     try{
                     nextState = accountCreation(out, request);}
                     catch(SQLException ex)
                     {
                         out.println("What the ");
                         out.println(ex);
                     }
                     break;
                     
                     
                 default:
                     //this should go to a specified state
                     nextState = stateEnum.INIT;
                     initState(out, request);
                     break;
             }
         }while(currentState != nextState);
     }
     
     
     /****************************************************
      * Generates an inventory for the store based on the
      *      player character's level
      * @param level the level of the player character
      * @return an array of new items for the store
      ***************************************************/
     Item[] getStoreInventory(int level, int size)
     {
         final int STORE_LEVEL = level;
 	final int STORE_SIZE = size;
 	Item[] storeItems = new Item[STORE_SIZE];
 		
 		for(int i = 0; i < STORE_SIZE; i++)
 		{
 						
 			//general type index that decides one of the three
                         // item types. // not including error
 			int gi = (int)(Math.round(Math.random() * (3)) + 1);
 			
 			storeItems[i] = generateItem(gi, STORE_LEVEL);
 		}
 		
 		return storeItems;
     }
     
     Item generateItem(int type, int Level)
 	{
 		final String[] armor_name_type = {"Plate Armor", "Leather Armor", "Robe", "Mail Armor", "Magic-Strength Armor stuff", "Magic-Agility Armor stuff", "Armor"};
 		final String[] weapon_name_type = {"Sword", "Axe", "Mace", "Bow", "Crossbow", "Throwing Knives", "Staff", "Wand", "Orb"}; // Could have room for permutations
 		final String[] item_name_type = {"potion"};
                final String[] error_name_type = {"error"};
 		final String[] item_name_quality_description = {"Broken", "Inferior", "Common", "Slightly Better", "Ancient", "Legendary", "Actually Broken"};
		final String[][] general_item_type = {error_name_type, weapon_name_type, armor_name_type, item_name_type};
 		//final String[] item_name_Modifier_description = ["Warrior", "Hunter", "Wizard", "Bandit", "BattleMage", "Magic-Range Thing whatever", "Balance"] // permutation for each thing
 		
 		double base_stats[] = {0, 0, 0, 0};
 		
                 //general type index
 			int gi = type;
 			//System.out.println(gi);
 			// special type index
 			int si = (int)(Math.round(Math.random() * (general_item_type[gi].length - 1)));
 			//System.out.println(si);
 			
 			// armor case
 			if(gi == 2)
 			{
 				switch (si)
 				{
 				case 0: base_stats[0] = 1;
 						base_stats[1] = 0;
 						base_stats[2] = 0;
 						break;
 				case 1: base_stats[0] = 0;
 						base_stats[1] = 1;
 						base_stats[2] = 0;
 						break;
 				case 2: base_stats[0] = 0;
 						base_stats[1] = 0;
 						base_stats[2] = 1;
 						break;
 				case 3: base_stats[0] = .5;
 						base_stats[1] = .5;
 						base_stats[2] = 0;
 						break;
 				case 4: base_stats[0] = .5;
 						base_stats[1] = 0;
 						base_stats[2] = .5;
 						break;
 				case 5: base_stats[0] = 0;
 						base_stats[1] = .5;
 						base_stats[2] = .5;
 						break;
 				case 6: base_stats[0] = 0.3333;
 						base_stats[1] = 0.3333;
 						base_stats[2] = 0.3333;
 						break;
 				
 				}
 			}
 			// weapon case
 			else if(gi == 1)
 			{
 				if((si % 9) < 3)
 				{
 					base_stats[0] = 1;
 				}
 				else if((si % 9) < 6)
 				{
 					base_stats[1] = 1;
 				}
 				else if((si % 9) < 9)
 				{
 					base_stats[2] = 1;
 				}
 			}
 			// item case
 			else if(gi == 3)
 			{
 				switch(si)
 				{
 				// potions have an abitrary larger base value thing
 				case 0: base_stats[3] = 2;
 						break;
 				}
 				
 			}
 			// Higher levels will have a more balance distribution of items
 			// e.g. Cannot possibly find a legendary item until at least level 9
 			
 			double quality = getQuality(Level);
 			int index = (int) Math.round(quality * ((item_name_quality_description.length) - 1));
 			String item_quality = item_name_quality_description[index];
 			
 			String item_type = general_item_type[gi][si];
 			
 			// Get the base damage of each stat
 			// will only affect one stat at the moment
 			
 			int value_sum = 0;
 			for(int j = 0; j < 4; j++)
 			{
 				// multiples the base stat for cases where the base stat is split up in proportions
 				base_stats[j] *=(((quality) * 100) + 20);
 				base_stats[j] = Math.round(base_stats[j]);
 				value_sum += base_stats[j];
 			}
 			
 		String item_name = item_quality + " " + item_type;
 		
 		Item item = new Item(item_name, 0, gi, 0, (int)base_stats[0], (int)base_stats[1],(int)base_stats[2], (int)base_stats[3]);
 		return item;
 
 	
 	}
     /****************************************************
      * Generates an armor item based on the player 
      * character's level
      * @param level the level of the player character
      * @return an item with a type of armor
      ***************************************************/
     Item generateArmor(int level)
     {
         return generateItem(2, level);
     }
     
     /****************************************************
      * Generates a weapon item based on the player 
      * character's level
      * @param level the level of the player character
      * @return an item with a type of weapon
      ***************************************************/
     Item generateWeapon(int level)
     {
         return generateItem(1,level);
     }
     
     /****************************************************
      * Loads a new enemy from the database
      * @param level the players level
      ***************************************************/
     void getNextEnemy(int level)
     {
     
     }
     
     /****************************************************
      * Loads the players current character from the 
      *      database
      ***************************************************/
     void getCurrentCharacter()
     {
         
     }
     
     /****************************************************
      * Adds a newly created character to the database
      * @param name the character's name
      * @param level the level of the character
      * @param bio  a biography about the character
      * @param health the health of the character
      * @param strength the strength of the character
      * @param agility the agility of the character
      * @param magic  the magic of the character
      * @param itemsHeld the items held by the character
      * @return did it work
      ***************************************************/
     Boolean newCharacter(String name, int level, String bio, int health, int strength, int agility, int magic,Item[] itemsHeld)
     {
         return false;
     }
 
     
     /****************************************************
      * The initial state
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum initState(PrintWriter out, HttpServletRequest request) {
         //can log in or unregistered user creation
         if(startingState != stateEnum.INIT)
         {
             //print the page
             out.println("<html>\n" +
 "	<head>\n" +
 "	<!-- Call normalize.css -->\n" +
 "	<link rel=\"stylesheet\" href=\"css/normalize.css\" type=\"text/css\" media=\"screen\">\n" +
 "	<!-- Import Font to be used in titles and buttons -->\n" +
 "	<link href='http://fonts.googleapis.com/css?family=Sanchez' rel='stylesheet' type='text/css'>\n" +
 "	<link href='http://fonts.googleapis.com/css?family=Prosto+One' rel='stylesheet' type='text/css'>\n" +
 "	<!-- Call style.css -->\n" +
 "	<link rel=\"stylesheet\" href=\"css/grid.css\" type=\"text/css\" media=\"screen\">\n" +
 "	<!-- Call style.css -->\n" +
 "	<link rel=\"stylesheet\" href=\"css/style.css\" type=\"text/css\" media=\"screen\">\n" +
 "	<title> Tarsus </title>\n" +
 "	</head>\n" +
 "	<body>\n" +
 "            <form action=\"Tarsus\" method=\"post\">\n" +
 "		<div id=\"header\" class=\"grid10\" align=\"right\">\n" +
 "			<input href=\"index.html\" id=\"tarsusTitle\" /> \n" +
 "			<input class=\"button\" type=\"submit\" value=\"Log in\" name=\"Log in\" /> </div>\n" +
 
 "		<div class=\"grid1\"> </div>\n" +
 "		<div class=\"grid8 centered\">\n" +
 "		<h1 id=\"title\" class=\"centered\">Welcome</h1>\n" +
 "		<p align=\"justify\"> \n" +
 "			Tarsus is a web based Role Playing Game that allows you to create your own character and use it to fight progressively more difficult enemies as you try to make your way to the top. If you already have an account, click the Log In button above. If not, you can make a character using our character maker or your can sign up and start your own adventure.\n" +
 "		</p>\n" +
 "		<div align=\"center\">\n" +
 "                    <input type=\"submit\" value=\"Create a Character\" name=\"Create a Character\" class=frontPageButton />\n" +
 "			<input type=\"submit\" value=\"Sign Up\" name=\"Sign Up\" class=frontPageButton />\n" +
 "		</div>\n" +
 "		</div>\n" +
 "		<div class=\"grid1\"> </div>\n" +
 "            </form>\n" +
 "	</body>\n" +
 "	\n" +
 "</html>");
             return stateEnum.INIT;
         }
         else
         {
             String value1 = request.getParameter("Sign Up");
             String value2 = request.getParameter("Log in");
             String value3 = request.getParameter("Create a Character");
             //state changes
 
 
           
             String value = "";
             if(value1 != null)
                 value = value1;
             if(value2!=null)
                 value = value2;
             if(value3!=null)
                 value = value3;
           
             if(value.equals("Log in"))
                 return stateEnum.LOGIN;
             if(value.equals("Create a Character"))
                 return stateEnum.UNREGISTERED_CHARACTER_CREATION;
             if("Sign Up".equals(value))
                 return stateEnum.ACCOUNT_CREATION; 
         }
        return stateEnum.INIT;
         
     }
 
     /****************************************************
      * The initial state
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     private stateEnum storeState(PrintWriter out, HttpServletRequest request) {
 		
         // have store level as well as the items be static so that it is the same each time the player comes back to the 
         // store unless the player has increased in level
 
         // if level has changed create a new item inventory for the store
         // based on some hash function of the character's level
         if(playerChar.getLevel() != storeLevel)
         {
                 storeLevel = playerChar.getLevel();
                 storeItems = getStoreInventory(storeLevel, STORE_SIZE);
 
         }
                 
 	if(startingState != stateEnum.STORE)
         {
             printStoreState(out);
             return stateEnum.STORE;
         }
         else
         {
             if(request.getParameter(accountName) != null)
                 return stateEnum.DECISION;
             // for buying items from the store
             for (int i = 0; i < storeItems.length - 1; i++)
             {
                 String buyValue = request.getParameter("Buy " + i);
                 if(buyValue != null)
                 {
                     gold -= storeItems[i].getValue();
                     // a bad way of deleting an element
                     storeItems[i] = null;
                     printStoreState(out);
                 }
             }
             // for selling items player's inventory
             for (int i = 0; i < playerChar.itemsHeld.length - 1; i++){
                 String sellValue = request.getParameter("Sell " + i);
                 if(sellValue != null)
                 {
                    gold += Math.round((.6) * playerChar.itemsHeld[i].getValue());
                    
                    // need to drop the item from the table
                    printStoreState(out);
                 }
             }
             return stateEnum.STORE;
         }
 
     }
 
     /****************************************************
      * The initial state
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     private stateEnum battleState(PrintWriter out, HttpServletRequest request) {
         if(startingState != stateEnum.BATTLE)
         {
             Item[] itemsHeld = {generateWeapon(1), generateArmor(1), generateWeapon(50), generateArmor(50)};
             playerChar = new PlayerCharacter("player", "", 1, 10, 1, 2, 3, itemsHeld, itemsHeld[0], itemsHeld[1], 0, 0, 0, 0);
             aresChar = new AresCharacter("enemy", "", 1, 10, 1, 2, 3, itemsHeld, itemsHeld[0], itemsHeld[1], 0, 0, 0, 0);
         }
         
         String startPage = "<html>\n" +
 "	<head>\n" +
 "	<!-- Call normalize.css -->\n" +
 "	<link rel=\"stylesheet\" href=\"../css/normalize.css\" type=\"text/css\" media=\"screen\">\n" +
 "	<!-- Import Font to be used in titles and buttons -->\n" +
 "	<link href='http://fonts.googleapis.com/css?family=Sanchez' rel='stylesheet' type='text/css'>\n" +
 "	<link href='http://fonts.googleapis.com/css?family=Prosto+One' rel='stylesheet' type='text/css'>\n" +
 "	<!-- Call style.css -->\n" +
 "	<link rel=\"stylesheet\" href=\"css/grid.css\" type=\"text/css\" media=\"screen\">\n" +
 "	<!-- Call style.css -->\n" +
 "	<link rel=\"stylesheet\" href=\"css/style.css\" type=\"text/css\" media=\"screen\">\n" +
 "	<title> Tarsus </title>\n" +
 "	</head>\n" +
 "	<body>\n" +
 "		<div id=\"header\" class=\"grid10\" align=\"right\">\n" +
 "			%s \n" +
 "	        </div>\n" +
 "		<div class=\"grid1\"> </div>\n" +
 "		<div class=\"grid8 centered\">\n" +
 "		<br />\n" +
 "		<p align=\"center\">\n" +
 "		</p>\n" +
 "		<div class=\"gridHalf\"> \n";
          String statsTable =       
 "			<h2 align=\"center\"> %s </h2>\n" +
 "			\n" +
 "			<table id=\"table\" align=\"center\">\n" +
 "				<tr>\n" +
 "					<th> Health </th>\n" +
 "					<th> Strength </th>\n" +
 "					<th> Magic </th>\n" +
 "					<th> Agility </th>\n" +
 "				</tr>\n" +
 "				<tr>\n" +
 "					<th> %d </th>\n" +
 "					<td> %d </td>\n" +
 "					<td> %d </td>\n" +
 "					<td> %d </td>\n" +
 "				</tr>\n" +
 "			</table>\n";
         String equippedTable1 = 
 "			\n" +
 "			<h3 align=\"center\"> Equipped </h3>\n" +
 "			<table id=\"table\" align=\"center\">\n" +
 "				<tr>\n" +
 "					<td> </td>\n" +
 "					<th> Name </th>\n" +
 "					<th> Strength </th>\n" +
 "					<th> Magic </th>\n" +
 "					<th> Agility </th>\n" +
 "				</tr>\n" +
 "				<tr>\n" +
 "					<th> Weapon: </th>\n" +
 "					<td> %s </td>\n" +
 "					<td> %d </td>\n" +
 "					<td> %d </td>\n" +
 "					<td> %d </td>\n" +
 "				</tr>\n";
         String equippedTable2 = 
 "                               <tr>\n" +
 "					<th> Armor: </th>\n" +
 "					<td> %s </td>\n" +
 "					<td> %d </td>\n" +
 "					<td> %d </td>\n" +
 "					<td> %d </td>\n" +
 "				</tr>\n" +
 "			</table>\n";
         String betweenCharacters = 
 "		</div>\n" +
 "		<div class=\"gridHalf\"> \n";
         String afterTable = 
 "		\n" +
 "		</div>\n" +
 "                               <form action=\"Tarsus\" method = \"post\">";
         String attackButton =
 "				<input type = \"submit\" class=\"profileButton\" name = \"attack\" value = \"Attack\" />  \n" + 
 "                               <select name = \"itemSelected\"> \n";
         String useButton = 
 "                               </select>" + 
 "				<input type = \"submit\" class=\"profileButton\" name=\"use\" value = \"Use item\" /> \n";
         String lastPart = 
 "                               </form>" + 
 "		<div class=\"grid1\"> </div>\n" +
 "	</body>\n" +
 "	\n" +
 "</html>";
         
         int aresDamage = 0, playerDamage = 0;      
         
         if(startingState == stateEnum.BATTLE)
         {
             String value = null, valueAttack=request.getParameter("attack"), valueUse=request.getParameter("use"), valueOK=request.getParameter("OK"), itemName;
             if(valueAttack!=null)
                 value = valueAttack;
             
             if(valueUse!=null)
             {
                 value=valueUse;
                 itemName = request.getParameter("itemSelected");
             }
             if(valueOK!=null)
                 value=valueOK;
             
             if(!value.equals("OK"))
             {
                 actionEnum playerAction = playerChar.requestAction(request);
                 actionEnum aresAction = aresChar.requestAction(request);
 
                 if(playerAction == actionEnum.ATTACK)
                 {
                     if(playerChar.weapon.getStrength()!=0)
                     {
                         aresDamage = (int) ((playerChar.getStrength()+playerChar.weapon.getStrength())*(Math.random()*.4+.8)-(aresChar.getStrength()*aresChar.armor.getStrength()/100));
                     }
 
                     if(playerChar.weapon.getAgility()!=0)
                     {
                         aresDamage = (int) ((playerChar.getAgility()+playerChar.weapon.getAgility())*(Math.random()*.4+.8)-(aresChar.getAgility()*aresChar.armor.getAgility()/100));
                     }
 
                     if(playerChar.weapon.getMagic()!=0)
                     {
                         aresDamage = (int) ((playerChar.getMagic()+playerChar.weapon.getMagic())*(Math.random()*.4+.8)-(aresChar.getMagic()*aresChar.armor.getMagic()/100));
                     }
                 }
                 if(aresAction == actionEnum.ATTACK)
                 {
                     if(aresChar.weapon.getStrength()!=0)
                     {
                         playerDamage = (int) ((aresChar.getStrength()+aresChar.weapon.getStrength())*(Math.random()*.4+.8)-(playerChar.getStrength()*playerChar.armor.getStrength()/100));
                     }
                     if(aresChar.weapon.getMagic()!=0)
                     {
                         playerDamage = (int) ((aresChar.getMagic()+aresChar.weapon.getMagic())*(Math.random()*.4+.8)-(playerChar.getMagic()*playerChar.armor.getMagic()/100));
                     }
                     if(aresChar.weapon.getAgility()!=0)
                     {
                         playerDamage = (int) ((aresChar.getAgility()+aresChar.weapon.getAgility())*(Math.random()*.4+.8)-(playerChar.getAgility()*playerChar.armor.getAgility()/100));
                     }
                 }
 
                 playerChar.setHealth(playerChar.getHealth() - playerDamage);
                 aresChar.setHealth(aresChar.getHealth() - aresDamage);
 
             }
             
             else if(playerChar.getHealth()<1)
             {
                 //mark the character as dead in the database debug
                 return stateEnum.IDLING;//needs to be changed to profile wants that state has been made debug
             }
             else if(aresChar.getHealth()<1)
                 return stateEnum.DECISION;
         }
         
         out.printf(startPage,accountName);
         out.printf(statsTable, playerChar.name, playerChar.getHealth(), playerChar.getStrength(), playerChar.getMagic(), playerChar.getAgility());
         out.printf(equippedTable1, playerChar.weapon.getName(), playerChar.weapon.getStrength(), playerChar.weapon.getMagic(), playerChar.weapon.getAgility());
         out.printf(equippedTable2, playerChar.armor.getName(), playerChar.armor.getStrength(), playerChar.armor.getMagic(), playerChar.armor.getAgility());
         out.printf(betweenCharacters);
         out.printf(statsTable, aresChar.name, aresChar.getHealth(), aresChar.getStrength(), aresChar.getMagic(), aresChar.getAgility());
         out.printf(equippedTable1, aresChar.weapon.getName(),aresChar.weapon.getStrength(), aresChar.weapon.getMagic(), aresChar.weapon.getAgility());
         out.printf(equippedTable2, aresChar.armor.getName(), aresChar.armor.getStrength(), aresChar.armor.getMagic(), aresChar.armor.getAgility());
         out.printf(afterTable);
         
        out.printf("<div>You have done %d damage to your opponent.\n Your opponent has done %d damage to you.</div>", aresDamage, playerDamage);
         
         if((playerChar.getHealth()>0) && (aresChar.getHealth()>0))
         {
             out.printf(attackButton);
             for(int i=0; i < playerChar.itemsHeld.length;i++)
             {
                 //change first string, the value parameter, to itemId
                 out.printf("<option value = \"%s\"> %s </option> \n", playerChar.itemsHeld[i].getName(),playerChar.itemsHeld[i].getName());
             }
             out.printf(useButton);
         }
         else if(playerChar.getHealth()<1)
         {
             out.printf("The valiant hero has been killed.\n");
             out.printf("<input type=\"submit\" name=\"OK\" value=\"OK\" class=\"profileButton\" /> \n");
         }
    
         
         else if(aresChar.getHealth()<1)
         {
             int newGold = (int) (constantGoldPerLevel*playerChar.getLevel()*(Math.random()*.4+.8));
             //add newGold to the accounts gold
             playerChar.setHealth(playerChar.getMaxHealth());
             out.printf("Congradulations you beat your enemy.\n You get %d gold.\n", newGold);
             out.printf("<input type=\"submit\" name=\"OK\" value=\"OK\" class=\"profileButton\" /> \n");
         }
         out.printf("attack:%d,strength:%d,magic:%d,agility:%d ",playerChar.timesAttacked,playerChar.timesSwitchedToStrength,playerChar.timesSwitchedToMagic, playerChar.timesSwitchedToAgility);
 
         out.printf(lastPart);
         return stateEnum.BATTLE;
     }
 
     /****************************************************
      * Create a registered user character
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum registeredCharacterCreationState(PrintWriter out, HttpServletRequest request) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     
     /****************************************************
      * Create an unregistered user character
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum unregisteredCharacterCreationState(PrintWriter out, HttpServletRequest request) {
         String StartPage = "<html>\n" +
 "	<head>\n" +
 "	<!-- Call normalize.css -->\n" +
 "	<link rel=\"stylesheet\" href=\"css/normalize.css\" type=\"text/css\" media=\"screen\">\n" +
 "	<!-- Import Font to be used in titles and buttons -->\n" +
 "	<link href='http://fonts.googleapis.com/css?family=Sanchez' rel='stylesheet' type='text/css'>\n" +
 "	<link href='http://fonts.googleapis.com/css?family=Prosto+One' rel='stylesheet' type='text/css'>\n" +
 "	<!-- Call style.css -->\n" +
 "	<link rel=\"stylesheet\" href=\"css/grid.css\" type=\"text/css\" media=\"screen\">\n" +
 "	<!-- Call style.css -->\n" +
 "	<link rel=\"stylesheet\" href=\"css/style.css\" type=\"text/css\" media=\"screen\">\n" +
 "	<title> Tarsus </title>\n" +
 "	</head>\n" +
 "       <script>\n" +
 "		function validateForm()\n" +
 "		{\n" +
 "		\n" +
 "               var maxValue = ";
         String secondPart = " \n" +
 "			var strength = parseInt(document.forms[\"createCharacterForm\"][\"strength\"].value); \n" +
 "			var agility = parseInt(document.forms[\"createCharacterForm\"][\"agility\"].value);\n" +
 "			var magic = parseInt(document.forms[\"createCharacterForm\"][\"magic\"].value);\n" +
 "                       var health = parseInt(document.forms[\"createCharacterForm\"][\"magic\"].value);\n" +
 "			var total = strength + agility + magic + health;\n" +
 "			alert(\"Total Experience points used: \" + total);\n" +
 "			if(total > maxValue)\n" +
 "			{\n" +
 "				alert(\"Cannot use more than\" + maxValue + \" experience points.\");\n" +
 "				return false;\n" +
 "			}\n" +
 "		\n" +
 "		}\n" +
 "       </script>" + 
 "	<body>\n" +
                 "<form name=\"createCharacterForm\" action=\"Tarsus\" onsubmit=\"return validateForm()\" method=\"post\">\n" +
 "		<div id=\"header\" class=\"grid10\" align=\"right\">\n" +
                 "<input type=\"Submit\" name=\"Home\" value=\"Home\"  class=\"FrontPageButton\" />" +
 "		<div class=\"grid1\"> </div></div>\n" +
 "		<div class=\"grid8 centered\">\n" +
 "		<h1 id=\"title\" class=\"centered\">Character Creation</h1>\n" +
 "		\n" +
 "		<div class=\"grid2\"> </div>\n" +
 "               <input type = \"hidden\" name = \"level\" value=\"";
         String thirdPart = "\"/>\n"+
 "		<div class=\"grid6\" align=\"center\">\n" +
 "			<h3> Level ";
         String fourthPart = " </h3>\n" +
 "			<p> Experience Points to Allocate: ";
         String fifthPart = "\n" +
 "			</p>\n" +
 "			<p> \n" +
 "				Name: <input type=\"text\" name=\"name\"/>\n" +
 "			</p>\n" +
 "			<p> \n" +
 "				Strength: <input type=\"number\" name=\"strength\"min=\"0\" max=\"100\" value=\"0\"/>\n" +
 "			</p> \n" +
 "			<p> \n" +
 "				Agility: <input type=\"number\" name=\"agility\"min=\"0\" max=\"100\" value=\"0\"/>\n" +
 "			</p>  \n" +
 "			<p> \n" +
 "				Magic: <input type=\"number\" name=\"magic\" min=\"0\" max=\"100\" value=\"0\"/>\n" +
 "			</p>   \n" +
 "			<p> \n" +
 "				Health: <input type=\"number\" name=\"health\" min=\"0\" max=\"100\" value=\"0\"/>\n" +
 "			</p>   \n" +
 "			<p>\n" +
 "				Biography:<textarea name=\"bio\" cols=\"35\" rows=\"3\" maxlength=\"300\"> </textarea> <br /> <a id=\"bioLimitID\">  (Max of 300 Chars)</a>\n" +
 "			</p>\n";
                 String lastPart = 
 "		</div>\n"+
 "		<div class=\"grid10\" align=\"center\">\n" +
 "			<input type =\"submit\" value=\"Create a Character\" class=frontPageButton /> \n" +
 "		</div>\n" +
 "		</form>\n" +
 "		</div>\n" +
 "		<div class=\"grid1\"> </div>\n" +
 "	</body>\n" +
 "	\n" +
 "</html>";
         if(startingState != stateEnum.UNREGISTERED_CHARACTER_CREATION)
         {
             //create new page for it
             Integer level = (int)(Math.random()*50);
             int numItemChoices = 5;
             Item tempItem;
             String submitValue;     
             out.printf(StartPage);
             out.println(((Integer)(level*constantPtsPerLevel)).toString());
             out.printf(secondPart);
             out.printf(level.toString());
             out.printf(thirdPart);
             out.printf(level.toString());
             out.printf(fourthPart);
             out.printf(((Integer)(level*constantPtsPerLevel)).toString());
             out.printf(fifthPart);
             out.printf("<input type=\"hidden\" name=\"level\" value=\"%d\" />\n",level);
 
             out.println("<table><tr><h2>Weapons</h2></tr><tr><th>Strength</th><th>Agility</th><th>Magic</th><th>select</th><tr>");
             for(int i=0; i<numItemChoices; i++)
             {
                 tempItem = generateWeapon(level);
                 submitValue = tempItem.getName()+"="+((Integer)tempItem.itemId).toString()+"+"+((Integer)tempItem.getStrength()).toString()+"-"+((Integer)tempItem.getAgility()).toString()+"*"+((Integer)tempItem.getMagic()).toString()+"_"+((Integer)tempItem.getType()).toString();
                 out.printf("<tr><td>%d</td><td>%d</td><td>%d</td><td><input type=\"radio\" name=\"weapon\" value=\"%s\"></td></tr>\n",tempItem.getStrength(), tempItem.getAgility(), tempItem.getMagic(), submitValue);
             }
             out.println("</table>");
             
             out.println("<table><tr><h2>Armor</h2></tr><tr><th>Strength</th><th>Agility</th><th>Magic</th><th>select</th><tr>");
             for(int i=0; i<numItemChoices; i++)
             {
                 tempItem = generateArmor(level);
                 submitValue = tempItem.getName()+"="+((Integer)tempItem.itemId).toString()+"+"+((Integer)tempItem.getStrength()).toString()+"-"+((Integer)tempItem.getAgility()).toString()+"*"+((Integer)tempItem.getMagic()).toString()+"_"+((Integer)tempItem.getType()).toString();
                 out.printf("<tr><td>%d</td><td>%d</td><td>%d</td><td><input type=\"radio\" name=\"armor\" value=\"%s\"></td></tr>\n",tempItem.getStrength(), tempItem.getAgility(), tempItem.getMagic(), submitValue);
             }
             out.println("</table>");
             out.println(lastPart);
             
             return stateEnum.UNREGISTERED_CHARACTER_CREATION;
         }
         else
         {
            if(request.getParameter("Home").equals("Home"))
            {
                return stateEnum.BATTLE; //debug
            }
 
            String name = (String) request.getParameter("name");
            Integer level = Integer.parseInt(request.getParameter("level"));
            String bio = request.getParameter("bio");
            int health = Integer.parseInt(request.getParameter("health"));
            int strength = Integer.parseInt(request.getParameter("strength"));
            int agility = Integer.parseInt(request.getParameter("agility"));
            int magic = Integer.parseInt(request.getParameter("magic"));
            Item[] items = {new Item(request.getParameter("weapon")), new Item(request.getParameter("armor"))};
 
       
            if(isValidString(name) & isValidString(bio))
            {
                //newCharacter(name, level,bio, health, strength, agility, magic, items);
                
                out.println(name);
                out.printf("level: %d\n",level);
                out.println(bio);
                out.printf("health: %d\n",health);
                out.printf("strength: %d\n",strength);
                out.printf("agility: %d\n",agility);
                out.printf("magic: %d\n",magic);
                out.printf("%s\n",items[0].name);
                out.printf("%d\n",items[0].itemId);
                out.printf("%d\n",items[0].strength);
                out.printf("%d\n",items[0].agility);
                out.printf("%d\n",items[0].magic);
                out.printf("%d\n",items[0].type);
                
                out.printf("%s\n",items[1].name);
                out.printf("%d\n",items[1].itemId);
                out.printf("%d\n",items[1].strength);
                out.printf("%d\n",items[1].agility);
                out.printf("%d\n",items[1].magic);
                out.printf("%d\n",items[1].type);
                return stateEnum.INIT;
            }
            else
            {
                 level = (int)(Math.random()*50);
                 int numItemChoices = 5;
                 Item tempItem;
                 String submitValue;
 
                 out.printf(StartPage);
                 out.println(((Integer)(level*constantPtsPerLevel)).toString());
                 out.printf(secondPart);
                 out.printf(level.toString());
                 out.printf(thirdPart);
                 out.printf(level.toString());
                 out.printf(fourthPart);
                 out.printf(((Integer)(level*constantPtsPerLevel)).toString());
                 out.printf(fifthPart);
                 out.printf("<input type=\"hidden\" name=\"level\" value=\"%d\" />\n",level);
 
                 out.println("<table><tr><h2>Weapons</h2></tr><tr><th>Strength</th><th>Agility</th><th>Magic</th><th>select</th><tr>");
                 for(int i=0; i<numItemChoices; i++)
                 {
                     tempItem = generateWeapon(level);
                     submitValue = tempItem.getName()+"="+((Integer)tempItem.itemId).toString()+"+"+((Integer)tempItem.getStrength()).toString()+"-"+((Integer)tempItem.getAgility()).toString()+"*"+((Integer)tempItem.getMagic()).toString()+"_"+((Integer)tempItem.getType()).toString();
                     out.printf("<tr><td>%d</td><td>%d</td><td>%d</td><td><input type=\"radio\" name=\"weapon\" value=\"%s\"></td></tr>\n",tempItem.getStrength(), tempItem.getAgility(), tempItem.getMagic(), submitValue);
                 }
                 out.println("</table>");
 
                 out.println("<table><tr><h2>Armor</h2></tr><tr><th>Strength</th><th>Agility</th><th>Magic</th><th>select</th><tr>");
                 for(int i=0; i<numItemChoices; i++)
                 {
                     tempItem = generateArmor(level);
                     submitValue = tempItem.getName()+"="+((Integer)tempItem.itemId).toString()+"+"+((Integer)tempItem.getStrength()).toString()+"-"+((Integer)tempItem.getAgility()).toString()+"*"+((Integer)tempItem.getMagic()).toString()+"_"+((Integer)tempItem.getType()).toString();
                     out.printf("<tr><td>%d</td><td>%d</td><td>%d</td><td><input type=\"radio\" name=\"armor\" value=\"%s\"></td></tr>\n",tempItem.getStrength(), tempItem.getAgility(), tempItem.getMagic(), submitValue);
                 }
                 out.println("</table>");
                 out.println(lastPart);
                 out.println("<script>alert(\"Invalid name or bio\");</script>");
                 return stateEnum.UNREGISTERED_CHARACTER_CREATION;
            }
         }
     }
 
     /****************************************************
      * The idling state, logs out after a certain amount of time
      * may be removed
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum idling(PrintWriter out, HttpServletRequest request) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     /****************************************************
      * Asking what the player wants to do next
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum decisionState(PrintWriter out, HttpServletRequest request) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     /****************************************************
      * At the blacksmith and can upgrade items
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum blackSmithState(PrintWriter out, HttpServletRequest request) {
         if(startingState != stateEnum.BLACKSMITH)
         {
             printBlacksmithState(out);
             return stateEnum.BLACKSMITH;
         }
         else
         {
             if(request.getParameter(accountName) != null)
                 return stateEnum.DECISION;
             for (int i = 0; i < playerChar.itemsHeld.length - 1; i++){
                 String tempValue = request.getParameter("Upgrade" + i);
                 if(tempValue != null)
                 {
                     playerChar.itemsHeld[i].upgradeItem();
                     printBlacksmithState(out);
                 }
             }
             return stateEnum.BLACKSMITH;
         }
     }
 
     /****************************************************
      * Registered user login state
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum loginState(PrintWriter out, HttpServletRequest request) {
         if(startingState != stateEnum.LOGIN){
             out.println("<html>\n" +
             "	<head>\n" +
             "	<!-- Call normalize.css -->\n" +
             "	<link rel=\"stylesheet\" href=\"css/normalize.css\" type=\"text/css\" media=\"screen\">\n" +
             "	<!-- Import Font to be used in titles and buttons -->\n" +
             "	<link href='http://fonts.googleapis.com/css?family=Sanchez' rel='stylesheet' type='text/css'>\n" +
             "	<link href='http://fonts.googleapis.com/css?family=Prosto+One' rel='stylesheet' type='text/css'>\n" +
             "	<!-- Call style.css -->\n" +
             "	<link rel=\"stylesheet\" href=\"css/grid.css\" type=\"text/css\" media=\"screen\">\n" +
             "	<!-- Call style.css -->\n" +
             "	<link rel=\"stylesheet\" href=\"css/style.css\" type=\"text/css\" media=\"screen\">\n" +
             "	<title> Tarsus </title>\n" +
             "	</head>\n" +
             "	<div id=\"header\" class=\"grid10\" align=\"right\"> \n" +
             "		<a href=\"index.jsp\" id=\"tarsusTitle\"> TARSUS </a> </div>\n" +
             "	<div class=\"grid1\"> </div>\n" +
             "	<div class=\"grid8 centered\">\n" +
             "		<h1 id=\"title\" class=\"centered\"> Log In</h1>\n" +
             "		<form method=\"post\" action=\"Tarsus\"> \n" +
             "			<p align=\"center\"> \n" +
             "				Username: <input name=\"username\" type=\"text\" /> \n" +
             "			</p>\n" +
             "			<p align=\"center\"> \n" +
             "				Password: <input name=\"password\" type=\"password\" /> \n" +
             "			</p>\n" +
             "			<p align=\"center\"> \n" +
             "				<input class=\"signUpButton\" value=\"Log In\" type=\"submit\"/>\n" +
             "			</p>\n" +
             "		</form>\n" +
             "	</div>\n" +
             "</html>");
                     
         }else{
             String username = request.getParameter("username");
             int password = request.getParameter("password").hashCode();
             if(!isValidString(username)){
                 out.println("Error");
                 return stateEnum.LOGIN;
             }
             String search = "SELECT * FROM Login WHERE username='" + username +
                     "' AND password= MD5('" + password+  "');";
             ResultSet result = sqlQuery(search, out);
             try{
             if(result.isBeforeFirst()){
                     accountName = username;
                     return stateEnum.DECISION;
             }else{
                 out.println("<html>\n" +
                     "	<head>\n" +
                     "	<!-- Call normalize.css -->\n" +
                     "	<link rel=\"stylesheet\" href=\"css/normalize.css\" type=\"text/css\" media=\"screen\">\n" +
                     "	<!-- Import Font to be used in titles and buttons -->\n" +
                     "	<link href='http://fonts.googleapis.com/css?family=Sanchez' rel='stylesheet' type='text/css'>\n" +
                     "	<link href='http://fonts.googleapis.com/css?family=Prosto+One' rel='stylesheet' type='text/css'>\n" +
                     "	<!-- Call style.css -->\n" +
                     "	<link rel=\"stylesheet\" href=\"css/grid.css\" type=\"text/css\" media=\"screen\">\n" +
                     "	<!-- Call style.css -->\n" +
                     "	<link rel=\"stylesheet\" href=\"css/style.css\" type=\"text/css\" media=\"screen\">\n" +
                     "	<title> Tarsus </title>\n" +
                     "	</head>\n" +
                     "	<div id=\"header\" class=\"grid10\" align=\"right\"> \n" +
                     "		<a href=\"index.jsp\" id=\"tarsusTitle\"> TARSUS </a> </div>\n" +
                     "	<div class=\"grid1\"> </div>\n" +
                     "	<div class=\"grid8 centered\">\n" +
                     "		<h1 id=\"title\" class=\"centered\"> Log In</h1>\n" +
                     "		<h3>Invalid Login </h3> \n " +
                     "		<form method=\"post\" action=\"Tarsus\"> \n " +
                     "			<p align=\"center\"> \n" +
                     "				Username: <input name=\"username\" type=\"text\" /> \n" +
                     "			</p>\n" +
                     "			<p align=\"center\"> \n" +
                     "				Password: <input name=\"password\" type=\"password\" /> \n" +
                     "			</p>\n" +
                     "			<p align=\"center\"> \n" +
                     "				<input class=\"signUpButton\" value=\"Log In\" type=\"submit\"/>\n" +
                     "			</p>\n" +
                     "		</form>\n" +
                     "	</div>\n" +
                     "</html>");
                 return stateEnum.LOGIN;
             }
             }catch(Exception ex){
                 out.println("Login SQL Error: " + ex);
             }
         }
         return stateEnum.LOGIN;
     }
 
     /****************************************************
      * Registered user creation state
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum accountCreation(PrintWriter out, HttpServletRequest request) throws SQLException {
         String accountPageBegin = "<html>\n" +
             "	<head>\n" +
             "	<!-- Call normalize.css -->\n" +
             "	<link rel=\"stylesheet\" href=\"css/normalize.css\" type=\"text/css\" media=\"screen\">\n" +
             "	<!-- Import Font to be used in titles and buttons -->\n" +
             "	<link href='http://fonts.googleapis.com/css?family=Sanchez' rel='stylesheet' type='text/css'>\n" +
             "	<link href='http://fonts.googleapis.com/css?family=Prosto+One' rel='stylesheet' type='text/css'>\n" +
             "	<!-- Call style.css -->\n" +
             "	<link rel=\"stylesheet\" href=\"css/grid.css\" type=\"text/css\" media=\"screen\">\n" +
             "	<!-- Call style.css -->\n" +
             "	<link rel=\"stylesheet\" href=\"css/style.css\" type=\"text/css\" media=\"screen\">\n" +
             "	<title> Tarsus </title>\n" +
             "	</head>\n" +
             "	<div id=\"header\" class=\"grid10\" align=\"right\"> \n" +
             "		<a href=\"index.jsp\" id=\"tarsusTitle\"> TARSUS </a> \n" +
             "		<a class=\"button\" href=\"login.html\"> Log In </a> </div>\n" +
             "	<div class=\"grid1\"> </div>\n" +
             "	<div class=\"grid8 centered\">\n" +
             "		<h1 id=\"title\" class=\"centered\"> Sign Up Below</h1>\n";
         String accountPageEnd = 
             "		<form method=\"post\" action=\"Tarsus\"> \n" +
             "			<p align=\"center\"> \n" +
             "				Username: <input name=\"username\" type=\"text\" /> \n" +
             "			</p>\n" +
             "			<p align=\"center\"> \n" +
             "				Password: <input name=\"password\" type=\"password\" /> \n" +
             "			</p>\n" +
             "			<p align=\"center\"> \n" +
             "				Confirm Password: <input name=\"confirmpassword\" type=\"password\" /> \n" +
             "			</p>\n" +
             "			<p align=\"center\"> \n" +
             "				<input class=\"signUpButton\" value=\"Sign Up\" type=\"submit\"/> \n" +
             "			</p>\n" +
             "		</form>\n" +
             "	</div>\n" +
             "	<div class=\"grid1\"> </div>\n" +
             "	\n" +
             "</html>";
         if(startingState != stateEnum.ACCOUNT_CREATION)
         {
             out.println(accountPageBegin + accountPageEnd);
             return stateEnum.ACCOUNT_CREATION;
         }
         else{
             
             String username = request.getParameter("username");
             String findUsername = "SELECT username FROM Login "
                     + "WHERE username = \"" + username + "\";";
             
             Boolean alreadyExists = false;
             try{
                 ResultSet result = sqlQuery(findUsername, out);
                 if(result.isBeforeFirst()){
                     alreadyExists= true;
                 }
                 
             }catch(Exception ex){
                 out.println("username check failure");  //Test Check
                 out.println(ex);
                 alreadyExists=true;
             }
             
             DBUtilities.closeStatement(stat);
             disconnectDB();
             // Check to see if the username is valid
             if(!isValidString(username) || alreadyExists)
             {
                out.println(accountPageBegin + 
                         "<h3 id=\"title\" class=\"centered\"> Invalid Username "
                        + "</h3 \n" + accountPageEnd);
                return stateEnum.ACCOUNT_CREATION;
             }
             
             int password = request.getParameter("password").hashCode();
             int confirmPassword = request.getParameter("confirmpassword").hashCode();
             if(password != confirmPassword){
                 out.println(accountPageBegin + 
                         "<h3 id=\"title\" class=\"centered\"> The Passwords Do "
                         + "Not Match </h3 \n" + accountPageEnd);
                 return stateEnum.ACCOUNT_CREATION;  
             }
             String command = "INSERT INTO Login VALUES ('" + username + "', MD5('"
                     + password +"'));";
             
             try{
             if(sqlCommand(command, out))
             {                
                 DBUtilities.closeStatement(stat);
                 disconnectDB();
                 return stateEnum.LOGIN;
              
             } 
             
             else{
                 out.println(accountPageBegin +"<h1> ERROR! </h1>"+ accountPageEnd);
                 return stateEnum.ACCOUNT_CREATION;
                         
             } 
             }catch(Exception ex)
             {
                 out.println("SQL Command Error:");
                 out.println(ex);
                 return stateEnum.ACCOUNT_CREATION;}
         }
     }
     
     
     /****************************************************
      * Checks the validity of a String for the database
      * @param string the string to check for validity
      * @return the validity
      ***************************************************/
     Boolean isValidString(String string)
     {
         Boolean toBeReturned = true;
         
         if(string.contains("drop"))
             toBeReturned = false;
         if(string.contains("delete"))
             toBeReturned = false;
         if(string.contains(";"))
             toBeReturned = false;
         
         return toBeReturned;
     }
     
     String maxValueScript(int value)
     {
     return ("<script> var maxValue=" + Integer.toString(value) +";</script>");
     }
 	
     double getQuality(int level)
     {
         double ratio = ((double)level) / ((double)level + 1.0);
 	double quality = Math.random() * ratio;
 	return quality;
     }
     
     public void printBlacksmithState(PrintWriter out)
     {
         String startPart = "<html>\n" +
             "	<head>\n" +
             "	<!-- Call normalize.css -->\n" +
             "	<link rel=\"stylesheet\" href=\"../css/normalize.css\" type=\"text/css\" media=\"screen\">\n" +
             "	<!-- Import Font to be used in titles and buttons -->\n" +
             "	<link href='http://fonts.googleapis.com/css?family=Sanchez' rel='stylesheet' type='text/css'>\n" +
             "	<link href='http://fonts.googleapis.com/css?family=Prosto+One' rel='stylesheet' type='text/css'>\n" +
             "	<!-- Call style.css -->\n" +
             "	<link rel=\"stylesheet\" href=\"../css/grid.css\" type=\"text/css\" media=\"screen\">\n" +
             "	<!-- Call style.css -->\n" +
             "	<link rel=\"stylesheet\" href=\"../css/style.css\" type=\"text/css\" media=\"screen\">\n" +
             "	<title> Tarsus </title>\n" +
             "	</head>\n" +
             "	<body>\n" +
             "		<div id=\"header\" class=\"grid10\" align=\"right\">\n" +
             "			<input value=\"Character Page\" name=\"" + accountName + "\" type=\"submit\" id=\"tarsusTitle\" />\n" +
             "			<input class=\"button\" value=\"Log Out\" name=\"Log Out\" /> </div>\n" +
             "		<div class=\"grid1\"> </div>\n" +
             "		<div class=\"grid8 centered\">\n" +
             "		<h1 id=\"title\" class=\"centered\">Blacksmith</h1>\n" +
             "		<table id=\"table\" align=\"center\">\n" +
             "			<tr>\n" +
             "				<td> </td>\n" +
             "				<th> Name </th>\n" +
             "				<th> Strength </th>\n" +
             "				<th> Magic </th>\n" +
             "				<th> Agility </th>\n" +
             "				<th> Type </th>\n" +
             "			</tr>\n" +
             "			<tr>";
         String endPart = "		</table>\n" +
             "		</div>\n" +
             "		<div class=\"grid1\"> </div>\n" +
             "	</body>\n" +
             "	\n" +
             "</html>";
         
         out.println(startPart);
         for (int i = 0; i < playerChar.itemsHeld.length - 1; i++){
             if(playerChar.itemsHeld[i].getUpgradeCount() < 3)
             {
                 out.println("<td> <input value=\"Upgrade" + i + " name=\"Upgrade" + i + " class=\"tableButton\"> /> </td>");
                 out.println("<td>");
                 out.println(playerChar.itemsHeld[i].getName());
                 out.println("</td>");
                 out.println("<td>");
                 out.println(playerChar.itemsHeld[i].getStrength());
                 out.println("</td>");
                 out.println("<td>");
                 out.println(playerChar.itemsHeld[i].getAgility());
                 out.println("</td>");
                 out.println("<td>");
                 out.println(playerChar.itemsHeld[i].getMagic());
                 out.println("</td>");
                 out.println("<td>");
                 out.println(playerChar.itemsHeld[i].getType());
                 out.println("</td>");
                 out.println("</tr>");
             }
         }
         out.println(endPart);
     }
     
     public void printStoreState(PrintWriter out)
     {
         String item_type_string[] = {"Error", "Weapon", "Armor", "Item"};
 
 			
 			String startPart = "<html>\n" +
 		            "	<head>\n" +
 		            "	<!-- Call normalize.css -->\n" +
 		            "	<link rel=\"stylesheet\" href=\"css/normalize.css\" type=\"text/css\" media=\"screen\">\n" +
 		            "	<!-- Import Font to be used in titles and buttons -->\n" +
 		            "	<link href='http://fonts.googleapis.com/css?family=Sanchez' rel='stylesheet' type='text/css'>\n" +
 		            "	<link href='http://fonts.googleapis.com/css?family=Prosto+One' rel='stylesheet' type='text/css'>\n" +
 		            "	<!-- Call style.css -->\n" +
 		            "	<link rel=\"stylesheet\" href=\"css/grid.css\" type=\"text/css\" media=\"screen\">\n" +
 		            "	<!-- Call style.css -->\n" +
 		            "	<link rel=\"stylesheet\" href=\"css/style.css\" type=\"text/css\" media=\"screen\">\n" +
 		            "	<title> Tarsus </title>\n" +
 		            "	</head>\n" +
 		            "	<body>\n" +
 		            "		<div id=\"header\" class=\"grid10\" align=\"right\">\n" +
 		            "			<input value=\"Character Page\" name=\""  + "\" type=\"submit\" id=\"tarsusTitle\" />\n" +
 		            "			<input class=\"button\" type=\"submit\" value=\"Log Out\" name=\"Log Out\" /> </div>\n" +
 		            "		<div class=\"grid1\"> </div>\n" +
 		            "		<div class=\"grid8 centered\">\n" +
 		            "		<h1 id=\"title\" class=\"centered\">Store</h1>\n" +
 		            "		<table id=\"table\" align=\"center\">\n" +
 		            "			<tr>\n" +
 		            "				<td> </td>\n" +
 		            "				<th> Name </th>\n" +
 		            "				<th> Strength </th>\n" +
 		            "				<th> Magic </th>\n" +
 		            "				<th> Agility </th>\n" +
 		            "				<th> Heal </th>\n" +
 		            "				<th> Type </th>\n" + 
 		            "				<th> Price </th>\n" +
 		            "			</tr>\n" +
 		            "			<tr>";
 			String sellPart = "		</table>\n" +
                     "		</div>\n" +
                     "		<div class=\"grid1\"> </div>\n" +
 					"<div class=\"grid1\"> </div>\n" +
 		            "		<div class=\"grid8 centered\">\n" +
 		            "		<h1 id=\"title\" class=\"centered\">Your Items</h1>\n" +
 		            "		<table id=\"table\" align=\"center\">\n" +
 		            "			<tr>\n" +
 		            "				<td> </td>\n" +
 		            "				<th> Name </th>\n" +
 		            "				<th> Strength </th>\n" +
 		            "				<th> Magic </th>\n" +
 		            "				<th> Agility </th>\n" +
 		            "				<th> Heal </th>\n" +
 		            "				<th> Type </th>\n" + 
 		            "				<th> Price </th>\n" +
 		            "			</tr>\n" +
 		            "			<tr>";
 			
 			String buttonPart = ("		</table>\n" +
 	                "		</div>\n" +
 	                "		<div class=\"grid1\"> </div>\n" +
 					"		<div class=\"grid10\" align=\"center\">\n" +
 					"			<input id=\"Form\" type =\"submit\" value=\"Initiate Transaction\" class=frontPageButton /> \n" +
 					"		</div>\n" +
 					"		</form>\n");
 			
             String endPart = 
                     "	</body>\n" +
                     "	\n" +
                     "</html>";
             String script = "<script> function getFormValues() {" +
             "for(var i = 0; i < 20; i++){" +
                 "	var item = document.getElementById('i');" +
                 "	alert(item.getAttribute('name'));" +
 
                             "} return false;} </script>";
 
             out.println(startPart);
 
             out.println("<form name=\"buyItems\" action=\"Tarsus\" onsubmit=\"return getFormValues()\" method=\"post\">\n");
             for (int i = 0; i < storeItems.length; i++){
                 out.println("<td> <input id =\"" + i + "\" type=\"submit\" value=\"Buy for " + storeItems[i].getValue() + "\" name=\"Buy " + i + "\" class=\"tableButton\"> </td>");
                 out.println("<td>");
                 out.println(storeItems[i].getName());
                 out.println("</td>");
                 out.println("<td>");
                 out.println(storeItems[i].getStrength());
                 out.println("</td>");
                 out.println("<td>");
                 out.println(storeItems[i].getAgility());
                 out.println("</td>");
                 out.println("<td>");
                 out.println(storeItems[i].getMagic());
                 out.println("</td>");
                 out.println("<td>");
                 out.println(storeItems[i].getHeal());
                 out.println("</td>");
                 out.println("<td>");
                 out.println(item_type_string[storeItems[i].getType()]);
                 out.println("</td>");
                 out.println("<td>");
                 out.println(storeItems[i].getValue());
                 out.println("</td>");
                 out.println("</tr>");
 			}
 	out.println(sellPart);
 	for (int i = 0; i < playerChar.itemsHeld.length; i++){
                 out.println("<td> <input type=\"submit\" value=\"Sell for " + (int)(0.60 * storeItems[i].getValue()) + "\" name=\"Sell " + i + "\" class=\"tableButton\"> </td>");
                 out.println("<td>");
                 out.println(playerChar.itemsHeld[i].getName());
                 out.println("</td>");
                 out.println("<td>");
                 out.println(playerChar.itemsHeld[i].getStrength());
                 out.println("</td>");
                 out.println("<td>");
                 out.println(playerChar.itemsHeld[i].getAgility());
                 out.println("</td>");
                 out.println("<td>");
                 out.println(playerChar.itemsHeld[i].getMagic());
                 out.println("</td>");
                 out.println("<td>");
                 out.println(playerChar.itemsHeld[i].getHeal());
                 out.println("</td>");
                 out.println("<td>");
                 out.println(playerChar.itemsHeld[storeItems[i].getType()]);
                 out.println("</td>");
                 out.println("<td>");
                 out.println((int)(0.60 * playerChar.itemsHeld[i].getValue()));
                 out.println("</td>");
                 out.println("</tr>");
 			}
         out.println(buttonPart);
         out.println(endPart);
         out.println(script);
     }
 }
