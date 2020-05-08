 package dk.bemyndigelsesregister.bemyndigelsesservice.server;
 
 import dk.bemyndigelsesregister.bemyndigelsesservice.domain.Bemyndigelse;
 import dk.bemyndigelsesregister.bemyndigelsesservice.domain.LinkedSystem;
 import dk.bemyndigelsesregister.bemyndigelsesservice.domain.Status;
 import dk.bemyndigelsesregister.bemyndigelsesservice.server.dao.*;
 import dk.bemyndigelsesregister.shared.service.SystemService;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.springframework.stereotype.Repository;
 
 import javax.inject.Inject;
 import java.util.Collection;
 
 import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
 
 @Repository
 public class BemyndigelseManagerImpl implements BemyndigelseManager {
     private static Logger logger = Logger.getLogger(BemyndigelseManagerImpl.class);
     @Inject
     BemyndigelseDao bemyndigelseDao;
 
     @Inject
     SystemService systemService;
 
     @Inject
     ArbejdsfunktionDao arbejdsfunktionDao;
 
     @Inject
     RettighedDao rettighedDao;
 
     @Inject
     LinkedSystemDao linkedSystemDao;
 
     @Override
     public Bemyndigelse opretAnmodningOmBemyndigelse(String linkedSystemKode, String bemyndigendeCpr, String bemyndigedeCpr, String bemyndigedeCvr, String arbejdsfunktionKode, String rettighedKode, String systemKode, DateTime gyldigFra, DateTime gyldigTil) {
         final Bemyndigelse bemyndigelse = createBemyndigelse(linkedSystemKode, bemyndigendeCpr, bemyndigedeCpr, bemyndigedeCvr, arbejdsfunktionKode, rettighedKode, Status.BESTILT, systemKode, gyldigFra, gyldigTil);
 
         bemyndigelseDao.save(bemyndigelse);
 
         return bemyndigelse;
     }
 
     private Bemyndigelse createBemyndigelse(String linkedSystemKode, String bemyndigendeCpr, String bemyndigedeCpr, String bemyndigedeCvr, String arbejdsfunktionKode, String rettighedKode, Status status, String systemKode, DateTime gyldigFra, DateTime gyldigTil) {
         LinkedSystem linkedSystem = linkedSystemDao.findByKode(linkedSystemKode);
         DateTime now = systemService.getDateTime();
 
         final Bemyndigelse bemyndigelse = new Bemyndigelse();
         bemyndigelse.setKode(systemService.createUUIDString());
         bemyndigelse.setBemyndigendeCpr(bemyndigendeCpr);
         bemyndigelse.setBemyndigedeCpr(bemyndigedeCpr);
         bemyndigelse.setBemyndigedeCvr(bemyndigedeCvr);
 
         bemyndigelse.setArbejdsfunktion(arbejdsfunktionDao.findByKode(linkedSystem, arbejdsfunktionKode));
 
         bemyndigelse.setStatus(status);
 
         bemyndigelse.setRettighed(rettighedDao.findByKode(linkedSystem, rettighedKode));
         bemyndigelse.setLinkedSystem(linkedSystemDao.findByKode(systemKode));
 
         final DateTime validFrom = defaultIfNull(gyldigFra, now);
         final DateTime validTo = defaultIfNull(gyldigTil, now.plusYears(100));
         if (!validFrom.isBefore(validTo)) {
             throw new IllegalArgumentException("GyldigFra=" + validFrom + " must be before GyldigTil=" + validTo);
         }
         bemyndigelse.setGyldigFra(validFrom);
         bemyndigelse.setGyldigTil(validTo);
 
         bemyndigelse.setSidstModificeret(now);
         bemyndigelse.setSidstModificeretAf("Service");
 
         bemyndigelse.setVersionsid(1);
         return bemyndigelse;
     }
 
     @Override
     public Collection<Bemyndigelse> godkendBemyndigelser(Collection<String> bemyndigelsesKoder) {
         Collection<Bemyndigelse> bemyndigelser = bemyndigelseDao.findByKoder(bemyndigelsesKoder);
 
         for (Bemyndigelse bemyndigelse : bemyndigelser) {
             approveBemyndigelseAndShutdownConflicts(bemyndigelse);
         }
         return bemyndigelser;
     }
 
     private void approveBemyndigelseAndShutdownConflicts(Bemyndigelse bemyndigelse) {
         final Collection<Bemyndigelse> existingBemyndigelser = bemyndigelseDao.findByInPeriod(
                 bemyndigelse.getBemyndigedeCpr(),
                 bemyndigelse.getBemyndigedeCvr(),
                 bemyndigelse.getArbejdsfunktion(),
                 bemyndigelse.getRettighed(),
                 bemyndigelse.getLinkedSystem(),
                 bemyndigelse.getGyldigFra(),
                 bemyndigelse.getGyldigTil()
         );
         for (Bemyndigelse existingBemyndigelse : existingBemyndigelser) {
             logger.info("Shutting down Bemyndigelse with kode=" + existingBemyndigelse.getKode());
             existingBemyndigelse.setGyldigTil(bemyndigelse.getGyldigFra());
            existingBemyndigelse.setSidstModificeret(systemService.getDateTime());
             bemyndigelseDao.save(existingBemyndigelse);
         }
         bemyndigelse.setStatus(Status.GODKENDT);
        bemyndigelse.setSidstModificeret(systemService.getDateTime());
         bemyndigelse.setGodkendelsesdato(systemService.getDateTime());
         bemyndigelseDao.save(bemyndigelse);
     }
 
     @Override
     public Bemyndigelse opretGodkendtBemyndigelse(String linkedSystemKode, String bemyndigendeCpr, String bemyndigedeCpr, String bemyndigedeCvr, String arbejdsfunktionKode, String rettighedKode, String systemKode, DateTime gyldigFra, DateTime gyldigTil) {
         Bemyndigelse bemyndigelse = createBemyndigelse(linkedSystemKode, bemyndigendeCpr, bemyndigedeCpr, bemyndigedeCvr, arbejdsfunktionKode, rettighedKode, Status.GODKENDT, systemKode, gyldigFra, gyldigTil);
 
         approveBemyndigelseAndShutdownConflicts(bemyndigelse);
 
         return bemyndigelse;
     }
 }
