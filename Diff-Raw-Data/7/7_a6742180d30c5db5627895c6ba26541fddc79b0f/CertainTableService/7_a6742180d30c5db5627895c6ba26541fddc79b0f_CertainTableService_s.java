 package com.worldofzaar.service;
 
 import com.worldofzaar.adapter.TablesAdapter;
 import com.worldofzaar.dao.CertainTableDao;
 import com.worldofzaar.dao.UserDao;
 import com.worldofzaar.entity.CertainTable;
 import com.worldofzaar.entity.User;
 import com.worldofzaar.modelAttribute.ApiTable;
 import com.worldofzaar.util.UserInformation;
 import com.worldofzaar.util.WOZConsts;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Дмитрий
  * Date: 30.11.13
  * Time: 9:47
  * To change this template use File | Settings | File Templates.
  */
 public class CertainTableService {
     HashMap<Integer, CertainTable> tableMap = new HashMap<Integer, CertainTable>();
 
     private int getMinLevel(int level) {
         if (level >= 1 && level < 5)
             return 1;
         if (level >= 5 && level < 10)
             return 5;
         if (level >= 10 && level < 20)
             return 10;
         return 1;
     }
 
     private int getMaxLevel(int level) {
         if (level >= 1 && level < 5)
             return 4;
         if (level >= 5 && level < 10)
             return 9;
         if (level >= 10 && level < 20)
             return 19;
         return 4;
     }
 
     public boolean getIn(ApiTable table, HttpServletRequest request) {
        if (!table.valid())
            return false;
         UserInformation userInformation = new UserInformation(request);
         CertainTableDao certainTableDao = new CertainTableDao();
         List<CertainTable> tables = certainTableDao.getCertainTables(table.getSize(), table.getCost(),
                 getMinLevel(userInformation.getUser().getGameProfile().getLevel()),
                 getMaxLevel(userInformation.getUser().getGameProfile().getLevel()));
         if (tables.size() == table.getSize())
             return false;
         else if (tables.size() < table.getSize()) {
             return getIn(tables, table, userInformation);
         }
         return false;
     }
 
     public boolean getOut(HttpServletRequest request) {
         UserInformation userInformation = new UserInformation(request);
         CertainTableDao certainTableDao = new CertainTableDao();
         return certainTableDao.deleteCertainTable(userInformation);
 
     }
 
     //Get in table if position is empty.
     private boolean getIn(List<CertainTable> tables, ApiTable table, UserInformation userInformation) {
         CardInDeckService cardInDeckService = new CardInDeckService();
         int userCardsCount = cardInDeckService.getCountOfActiveDeckCards(userInformation);
         if (userCardsCount != WOZConsts.MINIMUM_CARDS_COUNT)
             return false;
         if (tables.size() < table.getSize()) {
             for (CertainTable ct : tables) {
                 tableMap.put(ct.getSeatPosition(), ct);
             }
             CertainTable ct = tableMap.get(table.getPosition());
             if (ct != null) {
                 return false;
             } else {
                 UserDao userDao = new UserDao();
                 User user = userDao.find(userInformation.getUserId());
                 CertainTable newCT = new CertainTable();
                 newCT.setSeatPosition(table.getPosition());
                 newCT.setTableCost(table.getCost());
                 newCT.setTableSize(table.getSize());
                 newCT.setUser(user);
                 newCT.setLevel(user.getGameProfile().getLevel());
                 CertainTableDao certainTableDao = new CertainTableDao();
                 certainTableDao.add(newCT);
                 //Create table if table full/
                 if (tables.size() + 1 == table.getSize()) {
                     tables = certainTableDao.getCertainTables(table.getSize(), table.getCost(), getMinLevel(table.getLevel()), getMaxLevel(table.getLevel()));
                     startTable(tables, userInformation);
                 }
                 return true;
             }
         }
         return false;
     }
 
     private boolean startTable(List<CertainTable> tables, UserInformation userInformation) {
         if (tables == null)
             return false;
         if (tables.get(0).getTableSize() != tables.size())
             return false;
         GameService gameService = new GameService();
         gameService.createNewGame(tables);
 
         return false;
     }
 
     //Get tables
     public TablesAdapter getTables(int cost, HttpServletRequest request) {
         UserInformation userInformation = new UserInformation(request);
         CertainTableDao certainTableDao = new CertainTableDao();
         List<CertainTable> tables = certainTableDao.getCertainTables(cost, getMinLevel(userInformation.getUser().getGameProfile().getLevel()),
                 getMaxLevel(userInformation.getUser().getGameProfile().getLevel()));
         TablesAdapter tablesAdapter = new TablesAdapter(tables);
         return tablesAdapter;
     }
 
 }
