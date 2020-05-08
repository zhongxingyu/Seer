 package fr.cg95.cvq.service.authority;
 
 import java.util.TreeSet;
 
 import fr.cg95.cvq.business.authority.LocalAuthority;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.testtool.ServiceTestCase;
 
 /**
  * The tests for the local authority registry
  * 
  * @author jsb@zenexity.fr
  */
 public class LocalAuthorityRegistryTest extends ServiceTestCase {
 
     private LocalAuthority la;
     
     @Override
    public void onSetUp() throws Exception {
        super.onSetUp();
        
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.BACK_OFFICE_CONTEXT);
         la = SecurityContext.getCurrentSite();
         assertEquals(la.getServerNames().size(), 1);        
     }
     
     public void testGetLocalAuthorityByServerName()
         throws CvqException {
 
         String oldServerName = la.getServerNames().first();
         TreeSet<String> serverNames = new TreeSet<String>();
         serverNames.add(oldServerName + "new");
 
         localAuthorityRegistry.setLocalAuthorityServerNames(serverNames);
         continueWithNewTransaction();
         assertEquals(la.getServerNames().size(), 1);
         assertEquals(localAuthorityRegistry.getLocalAuthorityByServerName(oldServerName + "new").getId().longValue(), SecurityContext.getCurrentSite().getId().longValue());
         assertNull(localAuthorityRegistry.getLocalAuthorityByServerName(oldServerName));
         localAuthorityRegistry.addLocalAuthorityServerName(oldServerName);
         localAuthorityRegistry.removeLocalAuthorityServerName(oldServerName + "new");
     }
 
     public void testAddLocalAuthorityServerName()
         throws CvqException {
 
         localAuthorityRegistry.addLocalAuthorityServerName("sn2");
         continueWithNewTransaction();
         assertEquals(la.getServerNames().size(), 2);
         assertEquals(localAuthorityRegistry.getLocalAuthorityByServerName("sn2").getId().longValue(), SecurityContext.getCurrentSite().getId().longValue());
         localAuthorityRegistry.removeLocalAuthorityServerName("sn2");
     }
 
     public void testRegisterLocalAuthorityServerName()
         throws CvqException {
 
         localAuthorityRegistry.registerLocalAuthorityServerName("sn3");
         continueWithNewTransaction();
         assertEquals(la.getServerNames().size(), 1);
         assertEquals(localAuthorityRegistry.getLocalAuthorityByServerName("sn3").getId().longValue(), SecurityContext.getCurrentSite().getId().longValue());
         localAuthorityRegistry.unregisterLocalAuthorityServerName("sn3");
     }
 
     public void testRemoveLocalAuthorityServerName()
         throws CvqException {
 
         String oldServerName = la.getServerNames().first();
         localAuthorityRegistry.removeLocalAuthorityServerName(oldServerName);
         continueWithNewTransaction();
         assertEquals(la.getServerNames().size(), 0);
         assertNull(localAuthorityRegistry.getLocalAuthorityByServerName(oldServerName));
         localAuthorityRegistry.addLocalAuthorityServerName(oldServerName);
     }
 
     public void testUnregisterLocalAuthorityServerName()
         throws CvqException {
 
         localAuthorityRegistry.unregisterLocalAuthorityServerName(la.getServerNames().first());
         continueWithNewTransaction();
         assertEquals(la.getServerNames().size(), 1);
         assertNull(localAuthorityRegistry.getLocalAuthorityByServerName(la.getServerNames().first()));
         localAuthorityRegistry.registerLocalAuthorityServerName(la.getServerNames().first());
     }
 
     public void testSetLocalAuthorityServerNames()
         throws CvqException{
 
         String oldServerName = la.getServerNames().first();
         TreeSet<String> newServerNames = new TreeSet<String>();
         newServerNames.add(oldServerName + "bis");
         newServerNames.add(oldServerName + "ter");
         localAuthorityRegistry.setLocalAuthorityServerNames(newServerNames);
         continueWithNewTransaction();
         assertEquals(la.getServerNames().size(), 2);
         for (String serverName : newServerNames) {
             assertEquals(localAuthorityRegistry.getLocalAuthorityByServerName(serverName).getId().longValue(), SecurityContext.getCurrentSite().getId().longValue());
         }
         assertNull(localAuthorityRegistry.getLocalAuthorityByServerName(oldServerName));
         newServerNames = new TreeSet<String>();
         newServerNames.add(oldServerName);
         localAuthorityRegistry.setLocalAuthorityServerNames(newServerNames);
     }
 }
