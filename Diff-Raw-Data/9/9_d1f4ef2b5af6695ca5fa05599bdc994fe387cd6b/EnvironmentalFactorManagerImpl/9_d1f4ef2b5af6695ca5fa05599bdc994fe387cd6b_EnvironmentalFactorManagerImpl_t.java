 /*
 * $Id: EnvironmentalFactorManagerImpl.java,v 1.7 2006-04-27 15:02:08 pandyas Exp $
  *
  * $Log: not supported by cvs2svn $
 * Revision 1.6  2006/04/19 17:38:26  pandyas
 * Removed TODO text
 *
  * Revision 1.5  2006/04/17 19:11:06  pandyas
  * caMod 2.1 OM changes
  *
  */
 package gov.nih.nci.camod.service.impl;
 
 import gov.nih.nci.camod.domain.EnvironmentalFactor;
 import gov.nih.nci.camod.service.EnvironmentalFactorManager;
 
 import java.util.List;
 
 /**
  * @author rajputs
  */
 public class EnvironmentalFactorManagerImpl extends BaseManager implements EnvironmentalFactorManager
 {
     /**
      * Get all EnvironmentalFactor by id
      * 
      * 
      * @return the matching EnvironmentalFactor objects, or null if not found.
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public List getAll() throws Exception
     {
         log.trace("In EnvironmentalFactorManagerImpl.getAll");
         return super.getAll(EnvironmentalFactor.class);
     }
 
     /**
      * Get a specific EnvironmentalFactor by id
      * 
      * @param id
      *            the unique id for a EnvironmentalFactor
      * 
      * @return the matching EnvironmentalFactor object, or null if not found.
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public EnvironmentalFactor get(String id) throws Exception
     {
         log.trace("In EnvironmentalFactorManagerImpl.get");
         return (EnvironmentalFactor) super.get(id, EnvironmentalFactor.class);
     }
 
     /**
      * Save EnvironmentalFactor
      * 
      * @param EnvironmentalFactor
      *            the EnvironmentalFactor to save
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public void save(EnvironmentalFactor environmentalFactor) throws Exception
     {
         log.debug("In EnvironmentalFactorManagerImpl.save");
         super.save(environmentalFactor);
     }
 
     /**
      * Remove a specific EnvironmentalFactor by id
      * 
      * @param id
      *            the unique id for a EnvironmentalFactor
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public void remove(String id) throws Exception
     {
        log.debug("In EnvironmentalFactorManagerImpl.remove");
         super.remove(id, EnvironmentalFactor.class);
     }
 }
