 package org.ow2.jonas.azure.pastebean.service.bean;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.ow2.jonas.azure.pastebean.model.Paste;
 import org.ow2.jonas.azure.pastebean.service.HashService;
 import org.ow2.jonas.azure.pastebean.service.PasteService;
 
 /**
  * Session Bean implementation class PasteServiceBean
  */
 @Stateless
 public class PasteServiceBean implements PasteService {
 
 	private EntityManager entityManager;
 	
 	private HashService hashService;
 
     @PersistenceContext
     public void setEntityManager(EntityManager entityManager) {
         this.entityManager = entityManager;
     }
 
     @EJB
     public void setHashService(HashService hashService) {
         this.hashService = hashService;
     }
 
     public Paste createPaste(String name, String desc, String content) {
 		Paste paste = new Paste();
 		paste.setAuthor(name);
 		paste.setDescription(desc);
 		paste.setContent(content);
 		paste.setCreationDate(new Date());

        paste.setHash(hashService.createHash(paste));
 		
 		entityManager.persist(paste);
 		
 		return paste;
 	}
 
 	public void deletePaste(Paste paste) {
 		entityManager.remove(paste);
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Paste> findPastes() {
 		return (List<Paste>) entityManager.createNamedQuery("all").getResultList();
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Paste> findMatchingPastes(String hashFragment) {
 		return (List<Paste>) entityManager.createNamedQuery("from_hash")
				.setParameter("pattern", hashFragment + "%")
 				.getResultList();
 	}
 
 }
