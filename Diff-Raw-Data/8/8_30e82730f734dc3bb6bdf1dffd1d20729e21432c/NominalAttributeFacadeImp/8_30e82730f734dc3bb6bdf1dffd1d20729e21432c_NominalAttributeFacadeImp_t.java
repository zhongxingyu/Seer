 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package qa.qcri.aidr.predictui.facade.imp;
 
 import java.util.ArrayList;
 import qa.qcri.aidr.predictui.facade.*;
 import java.util.List;
 import javax.ejb.Local;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import qa.qcri.aidr.predictui.dto.CrisisAttributesDTO;
 import qa.qcri.aidr.predictui.entities.NominalAttribute;
 import qa.qcri.aidr.predictui.entities.NominalLabel;
 
 /**
  *
  * @author Imran
  */
 @Stateless
 public class NominalAttributeFacadeImp implements NominalAttributeFacade {
 
     @PersistenceContext(unitName = "qa.qcri.aidr.predictui-EJBS")
     private EntityManager em;
 
     public List<NominalAttribute> getAllAttributes() {
         List<NominalAttribute> attributesList = null;
         try {
             Query query = em.createNamedQuery("NominalAttribute.findAll", NominalAttribute.class);
             attributesList = query.getResultList();
             for (NominalAttribute attribute : attributesList) {
                 try {
                     Query labelQuery = em.createNamedQuery("NominalLabel.findByNominalAttribute", NominalLabel.class);
                     labelQuery.setParameter("nominalAttribute", attribute);
                     attribute.setNominalLabelCollection(labelQuery.getResultList());
                 } catch (NoResultException e) {
                     attribute.setNominalLabelCollection(null);
                 }
             }
         } catch (NoResultException e) {
             return null;
         }
 
         return attributesList;
 
     }
 
     public List<CrisisAttributesDTO> getAllAttributesExceptCrisis(int crisisID) {
         List<CrisisAttributesDTO> attributesList = new ArrayList();
         //String sql = "select na.*, mf.crisisID from nominal_attribute na\n"
         //        + " left join model_family mf on na.nominalAttributeID = mf.nominalAttributeID where mf.crisisID != :crisisID";
         String sql = "select na.*, mf.crisisID from nominal_attribute na\n" +
                      "  left join model_family mf on na.nominalAttributeID = mf.nominalAttributeID and mf.crisisID = :crisisID where mf.crisisID is null";
         try {
             Query query = em.createNativeQuery(sql);
             query.setParameter("crisisID", crisisID);
             List<Object[]> rows = query.getResultList();
             CrisisAttributesDTO attribute;
             for (Object[] row : rows) {
                 attribute = new CrisisAttributesDTO();
                 attribute.setNominalAttributeID(((Integer) row[0]).intValue());
                attribute.setUserID(((Integer) row[1]).intValue());
                attribute.setName((String) row[2]);
                attribute.setDescription((String) row[3]);
                attribute.setCode(((String) row[4]));
 //            if (row[5] != null)
 //                attribute.setCrisisID(((Integer)row[5]).intValue());
 //            else
 //                attribute.setCrisisID(null);
                 attributesList.add(attribute);
             }
             return attributesList;
         } catch (NoResultException e) {
             return null;
         }
     }
 
     public NominalAttribute addAttribute(NominalAttribute attribute) {
         em.persist(attribute);
         return attribute;
     }
 
     public NominalAttribute editAttribute(NominalAttribute attribute) {
         if (attribute == null) {
             throw new RuntimeException("Missing data values");
         }
         NominalAttribute attFound = em.find(NominalAttribute.class, attribute.getNominalAttributeID());
         if (attFound != null) {
             em.merge(attribute);
             return attribute;
         } else {
             throw new RuntimeException("Can't edit");
         }
 
     }
 
     public List<NominalAttribute> getAllAttributesbyByCollection(int crisisID) {
         return null;
     }
 
     public NominalAttribute getAttributeByID(int attributeID) {
         NominalAttribute attribute = null;
         try {
             Query query = em.createNamedQuery("NominalAttribute.findByNominalAttributeID", NominalAttribute.class);
             query.setParameter("nominalAttributeID", attributeID);
             attribute = (NominalAttribute) query.getSingleResult();
             try {
                 Query labelQuery = em.createNamedQuery("NominalLabel.findByNominalAttribute", NominalLabel.class);
                 labelQuery.setParameter("nominalAttribute", attribute);
                 attribute.setNominalLabelCollection(labelQuery.getResultList());
             } catch (NoResultException e) {
                 attribute.setNominalLabelCollection(null);
             }
 
         } catch (NoResultException e) {
             return null;
         }
         return attribute;
     }
 }
