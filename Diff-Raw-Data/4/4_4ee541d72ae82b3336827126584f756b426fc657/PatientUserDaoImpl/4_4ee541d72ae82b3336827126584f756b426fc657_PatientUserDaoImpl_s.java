 package com.worthsoln.repository.impl;
 
 import com.worthsoln.patientview.model.PatientUser;
 import com.worthsoln.repository.AbstractHibernateDAO;
 import com.worthsoln.repository.PatientUserDao;
 import org.springframework.stereotype.Repository;
 
 import javax.persistence.Query;
 import java.util.List;
 
 @Repository(value = "patientUserDao")
 public class PatientUserDaoImpl extends AbstractHibernateDAO<PatientUser> implements PatientUserDao {
 
     @Override
     public PatientUser getPatientUserByRadarNo(long radarNo) {
         Query query = getEntityManager().createQuery(
                 "SELECT patientUserId " +
                 "FROM tbl_patient_users " +
                 "WHERE RADAR_NO = :radarNo");
         query.setParameter("radarNo", radarNo);
 
         List<Integer> rawPatientUserList = query.getResultList();
 
        PatientUser patientUser = new PatientUser();
 
         if (rawPatientUserList.size() != 0) {
             patientUser.setPatientUserId(rawPatientUserList.get(0));
         }
 
         return patientUser;
     }
 
 }
