 package it.antreem.birretta.service.util;
 
 import it.antreem.birretta.service.dao.DaoException;
 import it.antreem.birretta.service.dao.DaoFactory;
 import it.antreem.birretta.service.dao.LocationCategoryDao;
 import it.antreem.birretta.service.model.Badge;
 import it.antreem.birretta.service.model.Drink;
 import it.antreem.birretta.service.model.Location;
 import it.antreem.birretta.service.model.LocationCategory;
 import it.antreem.birretta.service.model.User;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import org.apache.commons.logging.*;
 
 /**
  *
  * @author gmorlini
  */
 public class BadgeFinder {
     private static final Log log = LogFactory.getLog(BadgeFinder.class);
     public List<Badge> checkNewBadge(User user)
     {
         ArrayList<Badge> list= new ArrayList<Badge>();
         List<Integer> oldBadges = user.getBadges();
         List<Integer> newBadges = new ArrayList<Integer>();
         newBadges.addAll(oldBadges);
         List<Drink> myDrinks = DaoFactory.getInstance().getDrinkDao().findDrinksByIdUser(user.getIdUser(), null);
         
         //BADGE_NAME = 0 - DRINK_NUM
         
         if(!oldBadges.contains(BadgeEnum.DRINK_NUM_100.getIdBadge())) 
             
             // se non ho già sbloccato il livello massimo controllo se con questo check si è raggiunto qualcosa di nuovo
            
         //si suppone che ad ogni checkIn venga effetuato un controllo, quindi non è necessario controllare valori maggiori di ma solo
         //il raggiungimento con questo check in del valore di soglia superiore
         {
             log.info("verifica numero bevute complessive");
             int countDrinksByUsername = myDrinks.size();
             if (!oldBadges.contains(BadgeEnum.DRINK_NUM_1.getIdBadge()) && countDrinksByUsername == BadgeEnum.DRINK_NUM_1.getQuantity()) {
                 updateListBadgeLocally(list, newBadges,BadgeEnum.DRINK_NUM_1);
             } else if (!oldBadges.contains(BadgeEnum.DRINK_NUM_5.getIdBadge())  && countDrinksByUsername == BadgeEnum.DRINK_NUM_5.getQuantity()) {
                 updateListBadgeLocally(list, newBadges,BadgeEnum.DRINK_NUM_5);
             } else if (!oldBadges.contains(BadgeEnum.DRINK_NUM_10.getIdBadge())  && countDrinksByUsername == BadgeEnum.DRINK_NUM_10.getQuantity()) {
                updateListBadgeLocally(list, newBadges,BadgeEnum.DRINK_NUM_10);
             } else if (!oldBadges.contains(BadgeEnum.DRINK_NUM_50.getIdBadge())  && countDrinksByUsername == BadgeEnum.DRINK_NUM_50.getQuantity()) {
                updateListBadgeLocally(list, newBadges,BadgeEnum.DRINK_NUM_50);
             } else if ( countDrinksByUsername == BadgeEnum.DRINK_NUM_100.getQuantity()) {
                 updateListBadgeLocally(list, newBadges,BadgeEnum.DRINK_NUM_100);
             }
         }   
        
         //BADGE_NAME = 1 - DRINKER_PUB
          if(!oldBadges.contains(BadgeEnum.DRINKER_PUB_25.getIdBadge()))
          {
              log.info("verifica numero bevute in pub: ");
              int count=0;
              //Esempio individuazione in base a idLocationCategory
              for(Drink d: myDrinks)
              {
                  log.info("-- in pub: "+d.getIdPlace());
                  Location l=DaoFactory.getInstance().getLocationDao().findByIdLocation(d.getIdPlace());
                  if(l.getCategories().contains(LocationCategoryDao.ID_PUB))
                  {
                      count++;
                  }
              }
              if (!oldBadges.contains(BadgeEnum.DRINKER_PUB_1.getIdBadge())  && count == BadgeEnum.DRINKER_PUB_1.getQuantity()) {
                 updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_PUB_1);
             } else if (!oldBadges.contains(BadgeEnum.DRINKER_PUB_3.getIdBadge())  && count == BadgeEnum.DRINKER_PUB_3.getQuantity()) {
                 updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_PUB_3);
             } else if (!oldBadges.contains(BadgeEnum.DRINKER_PUB_5.getIdBadge())  && count == BadgeEnum.DRINKER_PUB_5.getQuantity()) {
                updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_PUB_5);
             } else if (!oldBadges.contains(BadgeEnum.DRINKER_PUB_10.getIdBadge())  && count == BadgeEnum.DRINKER_PUB_10.getQuantity()) {
                updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_PUB_10);
             } else if (count == BadgeEnum.DRINKER_PUB_25.getQuantity()) {
                 updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_PUB_25);
             }
          }
          //BADGE_NAME = 2 - DRINKER_PIZZA
          if(!oldBadges.contains(BadgeEnum.DRINKER_PIZZA_25.getIdBadge()))
          {
              log.info("verifica numero bevute in pzzeria");
              int count=0;
              //Esempio individuazione in base a idLocationCategory
              for(Drink d: myDrinks)
              {
                  Location l=DaoFactory.getInstance().getLocationDao().findByIdLocation(d.getIdPlace());
                  if(l.getCategories().contains(LocationCategoryDao.ID_PIZZA))
                  {
                      count++;
                  }
              }
              if (!oldBadges.contains(BadgeEnum.DRINKER_PIZZA_1.getIdBadge())  && count == BadgeEnum.DRINKER_PIZZA_1.getQuantity()) {
                 updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_PIZZA_1);
             } else if (!oldBadges.contains(BadgeEnum.DRINKER_PIZZA_3.getIdBadge()) && count == BadgeEnum.DRINKER_PIZZA_3.getQuantity()) {
                 updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_PIZZA_3);
             } else if (!oldBadges.contains(BadgeEnum.DRINKER_PIZZA_5.getIdBadge()) && count == BadgeEnum.DRINKER_PIZZA_5.getQuantity()) {
                updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_PIZZA_5);
             } else if (!oldBadges.contains(BadgeEnum.DRINKER_PIZZA_10.getIdBadge()) && count == BadgeEnum.DRINKER_PIZZA_10.getQuantity()) {
                updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_PIZZA_10);
             } else if (count == BadgeEnum.DRINKER_PIZZA_25.getQuantity()) {
                 updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_PIZZA_25);
             }
          }
          
          //BADGE_NAME = 3 - DRINKER_RISTO
          if(!oldBadges.contains(BadgeEnum.DRINKER_RISTO_25.getIdBadge()))
          {
              log.info("verifica numero bevute in ristorante");
              //esempio di ricerca per "name like" in futuro da implementare attraverso sub-category
              String name="Restaurant";
              int count=0;
              count = countDrinkWithLocationCategoryNameLike(myDrinks, name);
              if (!oldBadges.contains(BadgeEnum.DRINKER_RISTO_1.getIdBadge()) && count == BadgeEnum.DRINKER_RISTO_1.getQuantity()) {
                 updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_RISTO_1);
             } else if (!oldBadges.contains(BadgeEnum.DRINKER_RISTO_3.getIdBadge()) && count == BadgeEnum.DRINKER_RISTO_3.getQuantity()) {
                 updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_RISTO_3);
             } else if (!oldBadges.contains(BadgeEnum.DRINKER_RISTO_5.getIdBadge()) && count == BadgeEnum.DRINKER_RISTO_5.getQuantity()) {
                updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_RISTO_5);
             } else if (!oldBadges.contains(BadgeEnum.DRINKER_RISTO_10.getIdBadge()) && count == BadgeEnum.DRINKER_RISTO_10.getQuantity()) {
                updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_RISTO_10);
             } else if (count == BadgeEnum.DRINKER_RISTO_25.getQuantity()) {
                 updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_RISTO_25);
             }
          }
         
          
          //BADGE_NAME = 4 - DRINKER_BAR    
          if(!oldBadges.contains(BadgeEnum.DRINKER_BAR_25.getIdBadge()))
          {
              log.info("verifica numero bevute in bar");
              //esempio di ricerca per "name like" in futuro da implementare attraverso sub-category
              String name="Bar";
              int count=0;
              count = countDrinkWithLocationCategoryNameLike(myDrinks, name);
              if (!oldBadges.contains(BadgeEnum.DRINKER_BAR_1.getIdBadge()) && count == BadgeEnum.DRINKER_BAR_1.getQuantity()) {
                 updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_BAR_1);
             } else if (!oldBadges.contains(BadgeEnum.DRINKER_BAR_3.getIdBadge()) && count == BadgeEnum.DRINKER_BAR_3.getQuantity()) {
                 updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_BAR_3);
             } else if (!oldBadges.contains(BadgeEnum.DRINKER_BAR_5.getIdBadge()) && count == BadgeEnum.DRINKER_BAR_5.getQuantity()) {
                updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_BAR_5);
             } else if (!oldBadges.contains(BadgeEnum.DRINKER_BAR_10.getIdBadge()) && count == BadgeEnum.DRINKER_BAR_10.getQuantity()) {
                updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_BAR_10);
             } else if (count == BadgeEnum.DRINKER_BAR_25.getQuantity()) {
                 updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_BAR_25);
             }
          }
         
          //BADGE_NAME = 5 - DRINKER_OPENSPACE
         //name like outdoors
          
          //BADGE_NAME = 6 - DRINKER_HOME
           if(!oldBadges.contains(BadgeEnum.DRINKER_HOME_25.getIdBadge()))
          {
              log.info("verifica numero bevute a casa");
              int count=0;
              //Esempio individuazione in base a idLocationCategory
              for(Drink d: myDrinks)
              {
                  Location l=DaoFactory.getInstance().getLocationDao().findByIdLocation(d.getIdPlace());
                  if(l.getCategories().contains(LocationCategoryDao.ID_HOME))
                  {
                      count++;
                  }
              }
              if (!oldBadges.contains(BadgeEnum.DRINKER_HOME_1.getIdBadge()) && count == BadgeEnum.DRINKER_HOME_1.getQuantity()) {
                 updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_HOME_1);
             } else if (!oldBadges.contains(BadgeEnum.DRINKER_HOME_3.getIdBadge()) && count == BadgeEnum.DRINKER_HOME_3.getQuantity()) {
                 updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_HOME_3);
             } else if (!oldBadges.contains(BadgeEnum.DRINKER_HOME_5.getIdBadge()) && count == BadgeEnum.DRINKER_HOME_5.getQuantity()) {
                updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_HOME_5);
             } else if (!oldBadges.contains(BadgeEnum.DRINKER_HOME_10.getIdBadge()) && count == BadgeEnum.DRINKER_HOME_10.getQuantity()) {
                updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_HOME_10);
             } else if ( count == BadgeEnum.DRINKER_HOME_25.getQuantity()) {
                 updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_HOME_25);
             }
          }
          //BADGE_NAME = 7 - DRINKER_SPORT
          
          if(!oldBadges.contains(BadgeEnum.DRINKER_SPORT_25.getIdBadge()))
          {
              log.info("verifica numero bevute in stadio");
              //esempio di ricerca per "name like" in futuro da implementare attraverso sub-category
              String name="Stadium";
              int count=0;
              count = countDrinkWithLocationCategoryNameLike(myDrinks, name);
              if (!oldBadges.contains(BadgeEnum.DRINKER_SPORT_1.getIdBadge()) && count == BadgeEnum.DRINKER_SPORT_1.getQuantity()) {
                 updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_SPORT_1);
             } else if (!oldBadges.contains(BadgeEnum.DRINKER_SPORT_3.getIdBadge()) && count == BadgeEnum.DRINKER_SPORT_3.getQuantity()) {
                 updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_SPORT_3);
             } else if (!oldBadges.contains(BadgeEnum.DRINKER_SPORT_5.getIdBadge()) && count == BadgeEnum.DRINKER_SPORT_5.getQuantity()) {
                updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_SPORT_5);
             } else if (!oldBadges.contains(BadgeEnum.DRINKER_SPORT_10.getIdBadge()) && count == BadgeEnum.DRINKER_SPORT_10.getQuantity()) {
                updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_SPORT_10);
             } else if (count == BadgeEnum.DRINKER_SPORT_25.getQuantity()) {
                 updateListBadgeLocally(list, newBadges,BadgeEnum.DRINKER_SPORT_25);
             }
          }
          //BADGE_NAME = 8 - DRINKER_TRAVELER
          //TODO  ...
          /**
           * PER BADGE_NAME = 9 - DRINK_NIGHT
           * if(currentHour between 20:00 and 6:00) get al DRINKS bevuti  nella stessa fascia oraria 
           * and if(numero drink totali ==2) return idBadge=41, if(numero drink totali ==3) return idBadge=42 
           */
          //Date now=new Date();
          Calendar now = Calendar.getInstance();
          int h=now.get(Calendar.HOUR_OF_DAY);
          int m=now.get(Calendar.MINUTE);
          if(h>20 || h<6)
          //currentHour between 20:00 and 6:00
          {
              log.info("verifica numero bevute nella notte");
              Date startLimit=null;
              Date endLimit=null;
              if(h>20)
              //giorno vecchio
              {
              //20 di sera
              Calendar today20 = Calendar.getInstance();
              today20.set(Calendar.HOUR_OF_DAY, 20);
              today20.set(Calendar.MINUTE, 0);
              today20.set(Calendar.MILLISECOND, 0);
              //6 di mattina
              Calendar today6 =Calendar.getInstance();
              today6.set(Calendar.HOUR_OF_DAY, 6);
              today6.set(Calendar.MINUTE, 0);
              today6.set(Calendar.MILLISECOND, 0);
              //6 di domani
              today6.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH)+1);
              startLimit=today20.getTime();
              endLimit=today6.getTime();
              }
              else if(h<6)
              {
              //20 di sera
              Calendar today20 = Calendar.getInstance();
              today20.set(Calendar.HOUR_OF_DAY, 20);
              today20.set(Calendar.MINUTE, 0);
              today20.set(Calendar.MILLISECOND, 0);
              //6 di mattina
              Calendar today6 =Calendar.getInstance();
              today6.set(Calendar.HOUR_OF_DAY, 6);
              today6.set(Calendar.MINUTE, 0);
              today6.set(Calendar.MILLISECOND, 0);
              //20 di ieri
              today20.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH)-1);
              startLimit=today20.getTime();
              endLimit=today6.getTime();
              }
              else
              {
                  log.error("vicolo cieco PUPPA!!");
              }
              int count=0;
              for(Drink d : myDrinks)
              {
                  if(d.getInsertedOn().after(startLimit) && d.getInsertedOn().before(endLimit))
                  {
                      count++;     
                  }
              }
              log.info("numero bevute di "+user.getUsername()+":"+count);
               if (!oldBadges.contains(BadgeEnum.DRINK_NIGHT_2.getIdBadge()) && count == BadgeEnum.DRINK_NIGHT_2.getQuantity()) {
                 updateListBadgeLocally(list, newBadges,BadgeEnum.DRINK_NIGHT_2);
             } else if (!oldBadges.contains(BadgeEnum.DRINK_NIGHT_3.getIdBadge()) && count == BadgeEnum.DRINK_NIGHT_3.getQuantity()) {
                 updateListBadgeLocally(list, newBadges,BadgeEnum.DRINK_NIGHT_3);
             } else if (!oldBadges.contains(BadgeEnum.DRINK_NIGHT_4.getIdBadge()) && count == BadgeEnum.DRINK_NIGHT_4.getQuantity()) {
                updateListBadgeLocally(list, newBadges,BadgeEnum.DRINK_NIGHT_4);
             } else if (!oldBadges.contains(BadgeEnum.DRINK_NIGHT_5.getIdBadge()) && count == BadgeEnum.DRINK_NIGHT_5.getQuantity()) {
               updateListBadgeLocally(list, newBadges,BadgeEnum.DRINK_NIGHT_5);
             } else if (count == BadgeEnum.DRINK_NIGHT_10.getQuantity()) {
                 updateListBadgeLocally(list, newBadges,BadgeEnum.DRINK_NIGHT_10);
             }
          }
          /*
           * PER NOME BADGE = 11 get i 9 DRINK precendenti, 
           * ordinati temporlamente dal più vicino al + distante: 
           * if(il primo è dentro la mezz’ora da quello attuale) idBadge=51, 
           * if(il secondo è tra mezz’ora ed un’ora rispetto a quello attuale) idBadge=52, 
           * if(il terzo è fra l’ora e le due ora rispetto a quello attuale) idBadge=53, 
           * if(il quarto è fra le due e le tre ore rispetto a quello attuale) idBadge=54,
           * if(il nono è fra le tre e le sei ora rispetto a quello attuale) idBadge=55
           */
          
          
          /*
           * PER NOME BADGE = 12 (idBadge=56,57,58,59,60,61) get numero DRINKS totali con FOTO per idUser 
           * solo se nel checkin apena fattoc’è una foto,
           * if(numero drink totali ==1) return idBadge=56, 
           * if(numero drink totali ==5) return idBadge=57 etcetc....
           */
          
          /*
           * PER NOME BADGE = 13 (idBadge=62,63,64,65,66) if(checkin time tra le 20 e le 6) get i DRINK
           * precedenti nella stessa sera, if(ce ne sono 2 con 2 birre differenti e non ho sbloccato ancora il primo badge) 
           * idBadge=62, 
           * if(ce ne sono 3 con 3 birre differenti e non ho sbloccato ancora il secondo badge) id Badge=63. 
           */
          //finiti i controlli update dell'user
         user.setBadges(newBadges);
         user.setCounterBadges(newBadges.size());
         DaoFactory.getInstance().getUserDao().updateUser(user);
         //  ^- si potrebbe creare un metodo che aggiorni sono il campo badges per maggiore efficienza
         
         return list;
     }
 
     protected int countDrinkWithLocationCategoryNameLike(List<Drink> myDrinks, String name) throws DaoException {
         //Esempio individuazione in base a idLocationCategory
         int count=0;
         for(Drink d: myDrinks)
         {
             Location l=DaoFactory.getInstance().getLocationDao().findByIdLocation(d.getIdPlace());
             for(String idCategory : l.getCategories())
             {
                LocationCategory lc = DaoFactory.getInstance().getLocationCategoryDao().findLocationCategoryByIdCategory(idCategory);
                if(lc.getName().contains(name))
                count++;
             }
         }
         return count;
     }
    /**
      * Questo metodo aggiorna le liste dei badge di un utente, dei nuovi badges sbloccati e salva su mongoDB
      * se non presente, per facilitare succesive operazioni di ricerca
      * @param e BadgeEnum badge in input da aggiungere alle liste
      * @param list lista di badge che dovrà poi essere restituita nella response del checkIn
      * @param newBadges lista di idBadges da abbinare all'user
      */
     private void updateListBadgeLocally(ArrayList<Badge> list, List<Integer> newBadges,BadgeEnum e) {
         log.info("aggiunta badge "+ e.getIdBadge());
         //update lista da restituire per indicare nuovi badge sbloccati
         list.add(getBadgeFromEnum(e));
         //aggiornamento lista badge posseduti da un utente
         newBadges.add(e.getIdBadge());
         //salvataggio badge
         if(DaoFactory.getInstance().getBadgeDao().findByIdBadge(e.getIdBadge())==null)
             DaoFactory.getInstance().getBadgeDao().saveBadge(getBadgeFromEnum(e));
     }
     
     private Badge getBadgeFromEnum(BadgeEnum e)
     {
         Badge b=new Badge();
         b.setCategory(e.getCategory());
         b.setCod(e.getCod());
         b.setIdBadge(e.getIdBadge());
         b.setImage(e.getImage());
         b.setName(e.getName());
         return b;
     }
 }
