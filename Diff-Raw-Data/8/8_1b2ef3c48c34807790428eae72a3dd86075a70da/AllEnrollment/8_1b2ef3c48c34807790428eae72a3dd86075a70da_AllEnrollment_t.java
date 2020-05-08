 package org.motechproject.ghana.national.repository;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.ektorp.ComplexKey;
 import org.ektorp.CouchDbConnector;
 import org.ektorp.ViewQuery;
 import org.ektorp.support.View;
 import org.motechproject.dao.MotechAuditableRepository;
 import org.motechproject.ghana.national.domain.CWCEnrollment;
 import org.motechproject.ghana.national.vo.MotechProgramName;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Repository;
 
 import java.util.List;
 
 @Repository
 public class AllEnrollment extends MotechAuditableRepository<CWCEnrollment> {
 
     @Autowired
     public AllEnrollment(@Qualifier("couchDbConnector") CouchDbConnector db) {
         super(CWCEnrollment.class, db);
     }
 
     @View(name = "find_by_motech_patient_id", map = "function(doc) { if(doc.type === 'CWCEnrollment') emit(doc.patientId, doc); }")
     public CWCEnrollment findBy(String patientId) {
         if (StringUtils.isEmpty(patientId)) return null;
         ViewQuery viewQuery = createQuery("find_by_motech_patient_id").key(patientId).includeDocs(true);
         return getEnrollmentForView(viewQuery);
     }
 
     @View(name = "find_by_motech_patient_id_and_program_name", map = "function(doc) { if(doc.type === 'CWCEnrollment') emit([doc.patientId, doc.program.name], doc); }")
     public CWCEnrollment findBy(String patientId, MotechProgramName programName) {
        ViewQuery viewQuery = createQuery("find_by_motech_patient_id_and_program_name").key(ComplexKey.of(patientId, programName.name())).includeDocs(true);
         return getEnrollmentForView(viewQuery);
     }
 
     private CWCEnrollment getEnrollmentForView(ViewQuery viewQuery) {
         List<CWCEnrollment> CWCEnrollments = db.queryView(viewQuery, CWCEnrollment.class);
         return CollectionUtils.isEmpty(CWCEnrollments) ? null : CWCEnrollments.get(0);
     }
 }
