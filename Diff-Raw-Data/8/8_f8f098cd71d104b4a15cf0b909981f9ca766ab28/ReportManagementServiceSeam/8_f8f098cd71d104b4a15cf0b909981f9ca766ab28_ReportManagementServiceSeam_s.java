 package de.objectcode.time4u.server.ejb.seam.impl;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.List;
 
 import javax.ejb.Local;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jboss.annotation.ejb.LocalBinding;
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.AutoCreate;
 import org.jboss.seam.annotations.Factory;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.jboss.seam.annotations.datamodel.DataModel;
 import org.jboss.seam.annotations.security.Restrict;
 
 import de.objectcode.time4u.server.ejb.seam.api.IReportManagementServiceLocal;
 import de.objectcode.time4u.server.ejb.seam.api.io.XMLIO;
 import de.objectcode.time4u.server.ejb.seam.api.report.BaseReportDefinition;
 import de.objectcode.time4u.server.entities.report.ReportDefinitionEntity;
 
 @Stateless
 @Local(IReportManagementServiceLocal.class)
 @LocalBinding(jndiBinding = "time4u-server/seam/ReportManagementServiceSeam/local")
 @Name("ReportManagementService")
 @AutoCreate
 @Scope(ScopeType.CONVERSATION)
 public class ReportManagementServiceSeam implements IReportManagementServiceLocal
 {
   private final static Log LOG = LogFactory.getLog(ReportManagementServiceSeam.class);
 
   @PersistenceContext(unitName = "time4u")
   private EntityManager m_manager;
 
   @DataModel("admin.reportDefinitionList")
   List<ReportDefinitionEntity> m_reportDefinitions;
 
   @SuppressWarnings("unchecked")
  @Restrict("#{s:hasRole('admin')}")
   @Factory("admin.reportDefinitionList")
   public void initReportDefinitions()
   {
     final Query query = m_manager.createQuery("from " + ReportDefinitionEntity.class.getName() + " r order by r.id");
 
     m_reportDefinitions = query.getResultList();
   }
 
   public ReportDefinitionEntity getReportDefinitionEntity(final String reportId)
   {
     return m_manager.find(ReportDefinitionEntity.class, reportId);
   }
 
   public void storeReportDefinitionEntity(final ReportDefinitionEntity reportDefinitionEntity)
   {
     try {
       final BaseReportDefinition reportDefinition = XMLIO.INSTANCE.read(new StringReader(reportDefinitionEntity
           .getDefinitionXml()));
       if (reportDefinitionEntity.getId() == null || reportDefinitionEntity.getId().length() == 0) {
         reportDefinitionEntity.setId(reportDefinition.getName());
       }
       reportDefinitionEntity.setName(reportDefinition.getName());
       reportDefinitionEntity.setDescription(reportDefinition.getDescription());
       reportDefinitionEntity.setType(reportDefinition.getEntityType().toString());
 
       m_manager.merge(reportDefinitionEntity);
 
       initReportDefinitions();
     } catch (final IOException e) {
       LOG.error("Exception", e);
     }
   }
 
   public void deleteReportDeinfitionEntity(final String reportId)
   {
     final ReportDefinitionEntity entity = m_manager.find(ReportDefinitionEntity.class, reportId);
 
     m_manager.remove(entity);
 
     initReportDefinitions();
   }
 }
