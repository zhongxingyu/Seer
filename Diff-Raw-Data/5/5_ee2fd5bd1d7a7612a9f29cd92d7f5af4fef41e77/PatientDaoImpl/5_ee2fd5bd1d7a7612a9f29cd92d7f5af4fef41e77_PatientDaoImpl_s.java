 /*
  * PatientView
  *
  * Copyright (c) Worth Solutions Limited 2004-2013
  *
  * This file is part of PatientView.
  *
  * PatientView is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  * PatientView is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License along with PatientView in a file
  * titled COPYING. If not, see <http://www.gnu.org/licenses/>.
  *
  * @package PatientView
  * @link http://www.patientview.org
  * @author PatientView <info@patientview.org>
  * @copyright Copyright (c) 2004-2013, Worth Solutions Limited
  * @license http://www.gnu.org/licenses/gpl-3.0.html The GNU General Public License V3.0
  */
 
 package org.patientview.repository.impl;
 
 import org.patientview.model.Patient;
 import org.patientview.model.Patient_;
 import org.patientview.patientview.logon.PatientLogonWithTreatment;
 import org.patientview.patientview.model.Specialty;
 import org.patientview.patientview.unit.UnitUtils;
 import org.patientview.repository.AbstractHibernateDAO;
 import org.patientview.repository.PatientDao;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Repository;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 import javax.sql.DataSource;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 @Repository(value = "patientDao")
 public class PatientDaoImpl extends AbstractHibernateDAO<Patient> implements PatientDao {
 
     private JdbcTemplate jdbcTemplate;
 
     @Inject
     private DataSource dataSource;
 
     @PostConstruct
     public void init() {
         jdbcTemplate = new JdbcTemplate(dataSource);
     }
 
     @Override
     public Patient get(String nhsno, String unitcode) {
 
         CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
         CriteriaQuery<Patient> criteria = builder.createQuery(Patient.class);
         Root<Patient> from = criteria.from(Patient.class);
         List<Predicate> wherePredicates = new ArrayList<Predicate>();
 
         wherePredicates.add(builder.equal(from.get(Patient_.nhsno), nhsno));
         wherePredicates.add(builder.equal(from.get(Patient_.unitcode), unitcode));
 
         buildWhereClause(criteria, wherePredicates);
 
         try {
             return getEntityManager().createQuery(criteria).getSingleResult();
         } catch (Exception e) {
             return null;
         }
     }
 
     @Override
     public void delete(String nhsno, String unitcode) {
 
         if (nhsno == null || nhsno.length() == 0 || unitcode == null || unitcode.length() == 0) {
             throw new IllegalArgumentException("Required parameters nhsno and unitcode to delete patient");
         }
 
         Patient patient = get(nhsno, unitcode);
 
        if (patient != null) {
             delete(patient);
         }
     }
 
     @Override
     public List<Patient> get(String centreCode) {
 
         CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
         CriteriaQuery<Patient> criteria = builder.createQuery(Patient.class);
         Root<Patient> from = criteria.from(Patient.class);
         List<Predicate> wherePredicates = new ArrayList<Predicate>();
 
         wherePredicates.add(builder.equal(from.get(Patient_.unitcode), centreCode));
 
         buildWhereClause(criteria, wherePredicates);
         return getEntityManager().createQuery(criteria).getResultList();
     }
 
     @Override
     public List getUnitPatientsWithTreatmentDao(String unitcode, String nhsno, String name, boolean showgps,
                                                 Specialty specialty) {
         String sql = "SELECT "
                 + "user.username,  user.password, user.name, user.email, user.emailverified, user.accountlocked, "
                 + "usermapping.nhsno, usermapping.unitcode, emailverification.lastverificationdate, "
                 + "user.firstlogon, user.lastlogon, patient.treatment, patient.dateofbirth, patient.rrtModality,  "
                 + "pv_user_log.lastdatadate "
                 + "FROM user "
                 + "LEFT JOIN emailverification ON USER.username = emailverification.username, "
                 + "specialtyuserrole, usermapping "
                 + "LEFT JOIN patient ON usermapping.nhsno = patient.nhsno "
                 + "LEFT JOIN pv_user_log ON usermapping.nhsno = pv_user_log.nhsno "
                 + "WHERE specialtyuserrole.role = 'patient' "
                 + "AND user.username = usermapping.username "
                 + "AND user.id = specialtyuserrole.user_id "
                 + "AND usermapping.unitcode <> '" + UnitUtils.PATIENT_ENTERS_UNITCODE + "' "
                 + "AND    (patient.nhsno, patient.unitcode) NOT IN (SELECT dest_nhsno, dest_unitcode "
                 + "                                         FROM   rdr_patient_linkage) ";
                 // TODO Coming back to amend this method to match the one below
         sql += "AND usermapping.unitcode = ? ";
 
         if (nhsno != null && nhsno.length() > 0) {
             sql += "AND usermapping.nhsno LIKE ? ";
         }
 
         if (name != null && name.length() > 0) {
             sql += "AND user.name LIKE ? ";
         }
 
         if (!showgps) {
             sql += "AND user.name NOT LIKE '%-GP' ";
         }
 
         sql += "AND specialtyuserrole.specialty_id = ? ORDER BY user.name ASC ";
 
         List<Object> params = new ArrayList<Object>();
 
         params.add(unitcode);
 
         if (nhsno != null && nhsno.length() > 0) {
             params.add('%' + nhsno + '%');
         }
 
         if (name != null && name.length() > 0) {
             params.add('%' + name + '%');
         }
         params.add(specialty.getId());
 
         return jdbcTemplate.query(sql, params.toArray(), new PatientLogonWithTreatmentExtendMapper());
     }
 
 
     @Override
     public List getAllUnitPatientsWithTreatmentDao(String nhsno, String name, boolean showgps,
                                                    Specialty specialty) {
 
         StringBuilder query = new StringBuilder();
         query.append("SELECT usr.username ");
         query.append(",      usr.password ");
         query.append(",      usr.name ");
         query.append(",      usr.email ");
         query.append(",      usr.emailverified ");
         query.append(",      usr.accountlocked ");
         query.append(",      ptt.nhsno ");
         query.append(",      ptt.unitcode ");
         query.append(",      em.lastverificationdate ");
         query.append(",      usr.firstlogon ");
         query.append(",      usr.lastlogon ");
         query.append(",      ptt.treatment ");
         query.append(",      ptt.dateofbirth ");
         query.append(",      ptt.rrtModality ");
         query.append(",      pvl.lastdatadate ");
         query.append("FROM   user usr ");
         query.append("LEFT JOIN usermapping usm ON usm.username = usr.username ");
         query.append("LEFT JOIN patient ptt ON usm.nhsno = ptt.nhsno AND usm.unitcode = ptt.unitcode ");
         query.append("LEFT JOIN emailverification em ON usr.username = em.username ");
         query.append("LEFT JOIN specialtyuserrole str ON str.user_id = usr.id ");
         query.append("LEFT JOIN pv_user_log pvl ON ptt.nhsno = pvl.nhsno ");
         query.append("WHERE  str.role = 'patient' ");
         query.append("AND    usr.id = str.user_id ");
         query.append("AND    usm.unitcode <> 'PATIENT' ");
 
         query.append("AND    (ptt.nhsno, ptt.unitcode) NOT IN (SELECT dest_nhsno, dest_unitcode ");
         query.append("                                         FROM   rdr_patient_linkage) ");
 
 
         if (nhsno != null && nhsno.length() > 0) {
             query.append("AND usm.nhsno LIKE ? ");
         }
 
         if (name != null && name.length() > 0) {
             query.append("AND usr.name LIKE ? ");
         }
 
         if (!showgps) {
             query.append("AND usr.name NOT LIKE '%-GP' ");
         }
 
         query.append("AND    str.specialty_id = ?  ORDER BY usr.name ASC  ");
 
         List<Object> params = new ArrayList<Object>();
 
         if (nhsno != null && nhsno.length() > 0) {
             params.add('%' + nhsno + '%');
         }
 
         if (name != null && name.length() > 0) {
             params.add('%' + name + '%');
         }
         params.add(specialty.getId());
 
         return jdbcTemplate.query(query.toString(), params.toArray(), new PatientLogonWithTreatmentExtendMapper());
 
     }
 
     @Override
     public List<PatientLogonWithTreatment> getUnitPatientsAllWithTreatmentDao(String unitcode, Specialty specialty) {
         String sql = "SELECT "
                 + "   user.username,  "
                 + "   user.password, "
                 + "   user.name, "
                 + "   user.email, "
                 + "   user.emailverified, "
                 + "   user.lastlogon, "
                 + "   usermapping.nhsno, "
                 + "   usermapping.unitcode, "
                 + "   user.firstlogon, "
                 + "   user.accountlocked, "
                 + "   patient.treatment, "
                 + "   patient.dateofbirth "
                 + "FROM "
                 + "   user, "
                 + "   specialtyuserrole, "
                 + "   usermapping "
                 + "LEFT JOIN "
                 + "   patient ON usermapping.nhsno = patient.nhsno "
                 + "WHERE "
                 + "   usermapping.username = user.username "
                 + "AND "
                 + "   user.id = specialtyuserrole.user_id "
                 + "AND "
                 + "   usermapping.unitcode = ? "
                 + "AND "
                 + "   specialtyuserrole.role = 'patient' "
                 + "AND "
                 + "   user.name NOT LIKE '%-GP' "
                 + "AND "
                 + "   specialtyuserrole.specialty_id = ? "
                 + "ORDER BY "
                 + "   user.name ASC";
 
         List<Object> params = new ArrayList<Object>();
 
         params.add(unitcode);
         params.add(specialty.getId());
 
         return jdbcTemplate.query(sql, params.toArray(), new PatientLogonWithTreatmentMapper());
     }
 
     @Override
     public List<Patient> getUktPatients() {
 
         String sql = "SELECT DISTINCT patient.nhsno, patient.surname, patient.forename, "
                 + " patient.dateofbirth, patient.postcode FROM patient, user, usermapping "
                 + " WHERE patient.nhsno REGEXP '^[0-9]{10}$' AND patient.nhsno = usermapping.nhsno "
                 + "AND user.username = usermapping.username "
                 + " AND usermapping.username NOT LIKE '%-GP' AND user.dummypatient = 0";
 
         return jdbcTemplate.query(sql, new PatientMapper());
     }
 
     private class PatientMapper implements RowMapper<Patient> {
 
         @Override
         public Patient mapRow(ResultSet resultSet, int i) throws SQLException {
 
             Patient patient = new Patient();
             patient.setNhsno(resultSet.getString("nhsno"));
             patient.setSurname(resultSet.getString("surname"));
             patient.setForename(resultSet.getString("forename"));
             patient.setDateofbirth(resultSet.getString("dateofbirth"));
             patient.setPostcode(resultSet.getString("postcode"));
 
             return patient;
         }
     }
 
     private class PatientLogonWithTreatmentMapper implements RowMapper<PatientLogonWithTreatment> {
         @Override
         public PatientLogonWithTreatment mapRow(ResultSet resultSet, int i) throws SQLException {
             PatientLogonWithTreatment patientLogonWithTreatment = new PatientLogonWithTreatment();
 
             patientLogonWithTreatment.setUsername(resultSet.getString("username"));
             patientLogonWithTreatment.setPassword(resultSet.getString("password"));
             patientLogonWithTreatment.setName(resultSet.getString("name"));
             patientLogonWithTreatment.setEmail(resultSet.getString("email"));
             patientLogonWithTreatment.setEmailverified(resultSet.getBoolean("emailverified"));
             patientLogonWithTreatment.setAccountlocked(resultSet.getBoolean("accountlocked"));
             patientLogonWithTreatment.setNhsno(resultSet.getString("nhsno"));
             patientLogonWithTreatment.setFirstlogon(resultSet.getBoolean("firstlogon"));
             patientLogonWithTreatment.setLastlogon(resultSet.getDate("lastlogon"));
             patientLogonWithTreatment.setUnitcode(resultSet.getString("unitcode"));
             patientLogonWithTreatment.setTreatment(resultSet.getString("treatment"));
             patientLogonWithTreatment.setDateofbirth(resultSet.getString("dateofbirth"));
 
             return patientLogonWithTreatment;
         }
     }
 
     private class PatientLogonWithTreatmentExtendMapper extends PatientLogonWithTreatmentMapper {
         @Override
         public PatientLogonWithTreatment mapRow(ResultSet resultSet, int i) throws SQLException {
             PatientLogonWithTreatment patientLogonWithTreatment = super.mapRow(resultSet, i);
 
             patientLogonWithTreatment.setLastverificationdate(resultSet.getDate("lastverificationdate"));
             patientLogonWithTreatment.setRrtModality(resultSet.getInt("rrtModality"));
             patientLogonWithTreatment.setLastdatadate(resultSet.getDate("lastdatadate"));
             return patientLogonWithTreatment;
         }
     }
 }
