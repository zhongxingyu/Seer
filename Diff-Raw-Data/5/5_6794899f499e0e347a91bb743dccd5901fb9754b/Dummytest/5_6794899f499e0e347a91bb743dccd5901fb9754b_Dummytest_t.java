 package it.antreem.birretta.service.test.generic;
 
 import it.antreem.birretta.service.BirrettaService;
 import it.antreem.birretta.service.dao.DaoFactory;
 import it.antreem.birretta.service.dto.ErrorDTO;
 import it.antreem.birretta.service.dto.GenericResultDTO;
 import it.antreem.birretta.service.model.Address;
 import it.antreem.birretta.service.model.LocType;
 import it.antreem.birretta.service.model.Location;
 import it.antreem.birretta.service.util.Utils;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import javax.ws.rs.core.Response;
 import org.apache.log4j.Logger;
 import org.junit.Test;
 
 /**
  *
  * @author gmorlini
  */
 public class Dummytest 
 {
     private final static Logger log = Logger.getLogger(Dummytest.class);
     
     @Test
     public void dummytest()
     {
         LocType lt=new LocType();
         lt.setCod("MorlinsCode");
         lt.setDesc("Morlins Pub  and Grill for all");
         if(DaoFactory.getInstance().getLocTypeDao().findLocTypeByCod(lt.getCod())==null)
           DaoFactory.getInstance().getLocTypeDao().saveLocType(lt);
         BirrettaService service= new BirrettaService();
         Location l= new Location();
         Address a= new Address();
         a.setCap("54345");
         a.setNum(new Integer("46"));
         a.setState("IT");
         a.setStreet("Via della vittoria");
      //  l.setAddress(a);
         l.setUrl("mio località");
        //l.setIdLocType(lt.getCod());
         ArrayList<Double> pos=new ArrayList<Double>();
         pos.add(new Double("10"));
         pos.add(new Double("10"));
         l.setPos(pos);
         l.setName("MorlinsRumGrill");
         Response resp= service.insertLoc(l);
         log.debug("inserito: "+ l + "response. " + (resp.getEntity()));
     //    log.debug("inserito: "+ l + "response. " + ((ErrorDTO)resp.getEntity()).getError().getTitle()+" : " +((ErrorDTO)resp.getEntity()).getError().getDesc());
         log.debug("inserito: "+ l + "response. " + ((GenericResultDTO)resp.getEntity()).getMessage());
     
     }
 }
