 package fr.cg95.cvq.service.authority.impl;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import fr.cg95.cvq.business.authority.RecreationCenter;
 import fr.cg95.cvq.dao.authority.IRecreationCenterDAO;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.service.authority.IRecreationCenterService;
 
 /**
  * Implementation of the recreation center service.
  *
  * @author bor@zenexity.fr
  */
 public final class RecreationCenterService implements IRecreationCenterService {
 
     private static Logger logger = Logger.getLogger(RecreationCenterService.class);
 
     private IRecreationCenterDAO recreationCenterDAO;
 
     @Override
     public RecreationCenter getByName(String name) {
         return recreationCenterDAO.findByName(name);
     }
 
     @Override
     public RecreationCenter getById(final Long id)
         throws CvqObjectNotFoundException {
         return (RecreationCenter)recreationCenterDAO.findById(
             RecreationCenter.class, id);
     }
 
     @Override
     public List<RecreationCenter> getAll() {
         return recreationCenterDAO.listAll();
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.WRITE)
     public Long create(final RecreationCenter recreationCenter) {
         Long recreationCenterId = null;
         if (recreationCenter != null)
             recreationCenterId = recreationCenterDAO.create(recreationCenter);
         logger.debug("Created recreation center object with id : "
             + recreationCenterId);
         return recreationCenterId;
     }
 
     @Override
     public void modify(RecreationCenter recreationCenter) {
         recreationCenterDAO.update(recreationCenter);
     }
 
     public void setDAO(IRecreationCenterDAO recreationCenterDAO) {
         this.recreationCenterDAO = recreationCenterDAO;
     }
 }
