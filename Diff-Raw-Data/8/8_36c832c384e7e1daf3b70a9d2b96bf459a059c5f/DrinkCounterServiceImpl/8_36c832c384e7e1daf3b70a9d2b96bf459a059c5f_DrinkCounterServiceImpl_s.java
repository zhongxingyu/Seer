 package drinkcounter;
 
 import drinkcounter.dao.DrinkDAO;
 import drinkcounter.dao.UserDAO;
 import drinkcounter.dao.PartyDAO;
 import drinkcounter.model.Drink;
 import drinkcounter.model.User;
 import drinkcounter.model.Party;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import org.apache.commons.lang.StringUtils;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author Toni
  */
 @Service
 public class DrinkCounterServiceImpl implements DrinkCounterService {
 
     private static final Logger log = LoggerFactory.getLogger(DrinkCounterServiceImpl.class);
     @Autowired
     private PartyDAO partyDao;
     @Autowired
     private DrinkDAO drinkDao;
     @Autowired
     private UserDAO userDAO;
 
     @Override
     @Transactional
     public Party startParty(String partyName) {
         if(StringUtils.isBlank(partyName)){
             throw new IllegalArgumentException("Party name cannot be empty");
         }
 
         Party party = new Party();
         party.setName(partyName);
         party.setStartTime(new Date());
         partyDao.save(party);
 
         log.info("Party {}, id {} was started!", partyName, party.getId());
         return party;
     }
 
     @Override
     @Transactional
     public void updateParty(Party party) {
         partyDao.save(party);
     }
 
     @Override
     public List<Party> listParties() {
         return partyDao.readAll();
     }
 
     @Override
     public Party getParty(int partyId) {
         return partyDao.readByPrimaryKey(partyId);
     }
 
     @Override
     public List<User> listUsersByParty(int partyId) {
         return new LinkedList<User>(getParty(partyId).getParticipants());
     }
     
     @Override
     @Transactional
     public void linkUserToParty(int userId, int partyIdentifier) {
         Party party = getParty(partyIdentifier);
         User user = userDAO.readByPrimaryKey(userId);
         party.addParticipant(user);
         partyDao.save(party);
         log.info("User with name {} was added to party {}", user.getName(), party.getName());
     }
 
     @Override
     @Transactional
     public void unlinkUserFromParty(int userId, int partyId) {
         Party party = getParty(partyId);
         User user = userDAO.readByPrimaryKey(userId);
         party.removeParticipant(user);
         partyDao.save(party);
         log.info("User with name {} was added to party {}", user.getName(), party.getName());
     }
 
     @Override
     @Transactional
     public int addDrink(int userIdentifier) {
         User user = userDAO.readByPrimaryKey(userIdentifier);
         Drink drink = new Drink();
         drink.setDrinker(user);
         drink.setTimeStamp(new Date());
 
         user.drink(drink);
         
         drinkDao.save(drink);
         log.info("User {} has drunk a drink", user.getName());
 
         return drink.getId();
     }
 
     @Override
     public List<Drink> getDrinks(int userIdentifier) {
         User user = userDAO.readByPrimaryKey(userIdentifier);
         return drinkDao.findByDrinker(user);
     }
 
     @Override
     @Transactional
     public void removeDrinkFromUser(int userId, int drinkId) {
         User user = userDAO.readByPrimaryKey(userId);
         Drink drink = drinkDao.readByPrimaryKey(drinkId);
         user.removeDrink(drink);
         drinkDao.delete(drink);
     }
 
     @Override
     @Transactional
     public void addDrinkToDate(int userId, String date) {
         DateTimeFormatter parser = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");
         DateTime dt = parser.parseDateTime(date);
         
         if (dt.isAfterNow()) throw new IllegalArgumentException(date);
 
         User user = userDAO.readByPrimaryKey(userId);
         Drink drink = new Drink();
         drink.setDrinker(user);
         drink.setTimeStamp(dt.toDate());
         user.drink(drink);
         drinkDao.save(drink);
         log.info("User {} has drunk a drink at {}", user.getName(), dt.toString());
     }
 }
