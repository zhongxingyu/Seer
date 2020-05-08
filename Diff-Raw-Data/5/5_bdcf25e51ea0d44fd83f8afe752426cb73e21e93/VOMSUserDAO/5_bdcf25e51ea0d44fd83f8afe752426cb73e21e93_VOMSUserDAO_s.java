 /**
  * Copyright (c) Members of the EGEE Collaboration. 2006-2009.
  * See http://www.eu-egee.org/partners/ for details on the copyright holders.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * Authors:
  * 	Andrea Ceccanti (INFN)
  */
 package org.glite.security.voms.admin.persistence.dao;
 
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import org.glite.security.voms.admin.apiv2.VOMSUserJSON;
 import org.glite.security.voms.admin.configuration.VOMSConfiguration;
 import org.glite.security.voms.admin.configuration.VOMSConfigurationConstants;
 import org.glite.security.voms.admin.error.NotFoundException;
 import org.glite.security.voms.admin.error.NullArgumentException;
 import org.glite.security.voms.admin.error.VOMSException;
 import org.glite.security.voms.admin.event.EventManager;
 import org.glite.security.voms.admin.event.user.UserCreatedEvent;
 import org.glite.security.voms.admin.event.user.UserDeletedEvent;
 import org.glite.security.voms.admin.event.user.UserSignedAUPEvent;
 import org.glite.security.voms.admin.persistence.HibernateFactory;
 import org.glite.security.voms.admin.persistence.dao.generic.DAOFactory;
 import org.glite.security.voms.admin.persistence.error.AlreadyExistsException;
 import org.glite.security.voms.admin.persistence.error.AlreadyMemberException;
 import org.glite.security.voms.admin.persistence.error.AttributeAlreadyExistsException;
 import org.glite.security.voms.admin.persistence.error.AttributeValueAlreadyAssignedException;
 import org.glite.security.voms.admin.persistence.error.NoSuchAttributeException;
 import org.glite.security.voms.admin.persistence.error.NoSuchCAException;
 import org.glite.security.voms.admin.persistence.error.NoSuchGroupException;
 import org.glite.security.voms.admin.persistence.error.NoSuchUserException;
 import org.glite.security.voms.admin.persistence.error.UserAlreadyExistsException;
 import org.glite.security.voms.admin.persistence.error.VOMSDatabaseException;
 import org.glite.security.voms.admin.persistence.model.AUP;
 import org.glite.security.voms.admin.persistence.model.AUPAcceptanceRecord;
 import org.glite.security.voms.admin.persistence.model.AUPVersion;
 import org.glite.security.voms.admin.persistence.model.Certificate;
 import org.glite.security.voms.admin.persistence.model.VOMSAttributeDescription;
 import org.glite.security.voms.admin.persistence.model.VOMSCA;
 import org.glite.security.voms.admin.persistence.model.VOMSGroup;
 import org.glite.security.voms.admin.persistence.model.VOMSMapping;
 import org.glite.security.voms.admin.persistence.model.VOMSRole;
 import org.glite.security.voms.admin.persistence.model.VOMSUser;
 import org.glite.security.voms.admin.persistence.model.VOMSUser.SuspensionReason;
 import org.glite.security.voms.admin.persistence.model.VOMSUserAttribute;
 import org.glite.security.voms.admin.persistence.model.task.SignAUPTask;
 import org.glite.security.voms.admin.util.DNUtil;
 import org.hibernate.Criteria;
 import org.hibernate.ObjectNotFoundException;
 import org.hibernate.Query;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class VOMSUserDAO {
 
 	private static final Logger log = LoggerFactory
 			.getLogger(VOMSUserDAO.class);
 
 	public static VOMSUserDAO instance() {
 
 		HibernateFactory.beginTransaction();
 		return new VOMSUserDAO();
 	}
 
 	private VOMSUserDAO() {
 
 	}
 
 	public void requestAUPReacceptance(VOMSUser user, AUP aup) {
 		if (user == null)
 			throw new NullArgumentException("user cannot be null!");
 
 		if (aup == null)
 			throw new NullArgumentException("aup cannot be null!");
 
 		AUPVersion aupVersion = aup.getActiveVersion();
 
 		if (aupVersion == null)
 			throw new NotFoundException("No registered version found for AUP '"
 					+ aup.getName() + "'.");
 
 		log.debug("User '" + user + "' is request to reaccept aup version '"
 				+ aupVersion + "'");
 
 		AUPAcceptanceRecord r = user.getAUPAccceptanceRecord(aupVersion);
 
 		if (r != null) {
 
 			if (r.getValid())
 				r.setValid(false);
 			else
 				log.debug("Ignoring invalidation of already invalid record.");
 		}
 
 	}
 
 	public void signAUP(VOMSUser user) {
 
 		signAUP(user, DAOFactory.instance().getAUPDAO().getVOAUP());
 	}
 
 	public void signAUP(VOMSUser user, AUP aup) {
 
 		if (user == null)
 			throw new NullArgumentException("user cannot be null!");
 
 		if (aup == null)
 			throw new NullArgumentException("aup cannot be null!");
 
 		AUPVersion aupVersion = aup.getActiveVersion();
 
 		if (aupVersion == null)
 			throw new NotFoundException("No registered version found for AUP '"
 					+ aup.getName() + "'.");
 
 		log.debug("User '" + user + "' signing aup version '" + aupVersion
 				+ "'");
 
 		AUPAcceptanceRecord r = user.getAUPAccceptanceRecord(aupVersion);
 
 		if (r == null) {
 
 			log.debug("Creating new aup acceptance record!");
 			AUPAcceptanceRecord aupRecord = new AUPAcceptanceRecord(user,
 					aupVersion);
 			aupRecord.setLastAcceptanceDate(new Date());
 
 			user.getAupAcceptanceRecords().add(aupRecord);
 
 		} else {
 
 			log.debug("Updating existing acceptance record");
 			// User has signed but has been prompted to resign
 			AUPAcceptanceRecord aupRecord = user
 					.getAUPAccceptanceRecord(aupVersion);
 			aupRecord.setLastAcceptanceDate(new Date());
 			aupRecord.setValid(true);
 
 		}
 
 		SignAUPTask pendingTask;
 
 		do {
 
 			pendingTask = user.getPendingSignAUPTask(aupVersion.getAup());
 
 			if (pendingTask != null) {
 				log.debug("Setting task '" + pendingTask + "' completed");
 				pendingTask.setCompleted();
 			}
 
 		} while (pendingTask != null);
 
 		if (user.isSuspended()
 				&& user.getSuspensionReasonCode().equals(
 						SuspensionReason.FAILED_TO_SIGN_AUP)) {
 
 			log.debug("Restoring user '" + user + "'");
 			user.restore(SuspensionReason.FAILED_TO_SIGN_AUP);
 		}
 
 		HibernateFactory.getSession().saveOrUpdate(user);
 		EventManager
 				.dispatch(new UserSignedAUPEvent(user, aupVersion.getAup()));
 
 	}
 
 	public List<VOMSUser> findUsersWithPendingSignAUPTask(AUP aup) {
 
 		String queryString = "from VOMSUser u join u.tasks t where t.class = SignAUPTask and t.status != 'COMPLETED' and t.aup = :aup";
 
 		Query q = HibernateFactory.getSession().createQuery(queryString);
 		q.setEntity("aup", aup);
 
 		return q.list();
 
 	}
 	
 	public Long countExpiredUsers(){
 		
 		Date now = new Date();
 
 		String queryString = "select count(*) from VOMSUser u where u.endTime < :now";
 		
 		Query q = HibernateFactory.getSession().createQuery(queryString);
 
 		q.setDate("now", now);
 
 		return (Long) q.uniqueResult();
 		
 	}
 
 	public List<VOMSUser> findExpiredUsers() {
 
 		Date now = new Date();
 
 		String queryString = "from VOMSUser u where u.endTime < :now";
 		Query q = HibernateFactory.getSession().createQuery(queryString);
 
 		q.setDate("now", now);
 
 		return q.list();
 	}
 
 	public List<VOMSUser> findAUPFailingUsers(AUP aup) {
 
 		List<VOMSUser> result = new ArrayList<VOMSUser>();
 
 		AUPVersion activeVersion = aup.getActiveVersion();
 
 		// Get users First that do not have any acceptance records for the
 		// active aupVersion
 		String noAcceptanceRecordForActiveAUPVersionQuery = "select u from VOMSUser u where u not in (select u from VOMSUser u join u.aupAcceptanceRecords r where r.aupVersion.active = true)";
 
 		Query q = HibernateFactory.getSession().createQuery(
 				noAcceptanceRecordForActiveAUPVersionQuery);
 		List<VOMSUser> noRecordUsers = q.list();
 
 		result.addAll(noRecordUsers);
 
 		log.debug("Users without acceptance records for currently active aup:"
 				+ result);
 
 		// Add users that have an expired aup acceptance record due to aup
 		// update or acceptance retriggering.
 		String qString = "select u from VOMSUser u join u.aupAcceptanceRecords r where r.aupVersion.active = true and r.lastAcceptanceDate < :lastUpdateTime";
 
 		Query q2 = HibernateFactory.getSession().createQuery(qString);
 
 		q2.setTimestamp("lastUpdateTime", activeVersion.getLastUpdateTime());
 		List<VOMSUser> expiredDueToAUPUpdateUsers = q2.list();
 		result.addAll(expiredDueToAUPUpdateUsers);
 
 		log.debug("Users that signed the AUP before it was last updated:"
 				+ expiredDueToAUPUpdateUsers);
 
 		// Add users that have a valid aup acceptance record that needs to be
 		// checked against
 		// the reacceptance period
 		Query q3 = HibernateFactory
 				.getSession()
 				.createQuery(
 						"select u from VOMSUser u join u.aupAcceptanceRecords r where r.aupVersion.active = true"
 								+ " and r.lastAcceptanceDate > :lastUpdateTime ");
 
 		q3.setTimestamp("lastUpdateTime", activeVersion.getLastUpdateTime());
 
 		List<VOMSUser> potentiallyExpiredUsers = q3.list();
 		HibernateFactory.getSession().flush();
 
 		log.debug("Users that needs checking since their aup acceptance record could be expired:"
 				+ potentiallyExpiredUsers);
 
 		for (VOMSUser u : potentiallyExpiredUsers) {
 
 			AUPAcceptanceRecord r = u.getAUPAccceptanceRecord(aup
 					.getActiveVersion());
 
 			if (r.hasExpired()) {
 				log.debug(String
 						.format("Adding user %s to results due to expired aup acceptance report (aup validity expiration)",
 								u.toString()));
 				result.add(u);
 
 			}
 
 		}
 
 		return result;
 
 	}
 
 	public void addCertificate(VOMSUser u, String dn, String caDn) {
 		assert u != null : "User must be non-null!";
 		assert dn != null : "DN must be non-null!";
 		assert caDn != null : "CA must be non-null!";
 
 		VOMSCA ca = VOMSCADAO.instance().getByName(caDn);
 		if (ca == null)
 			throw new NoSuchCAException("CA '" + caDn
 					+ "' not found in database.");
 
 		Certificate cert = CertificateDAO.instance().findByDNCA(dn, caDn);
 
 		if (cert != null)
 			throw new AlreadyExistsException("Certificate already bound!");
 
 		cert = new Certificate();
 
 		cert.setSubjectString(dn);
 		cert.setCa(ca);
 		cert.setCreationTime(new Date());
 		cert.setSuspended(false);
 
 		cert.setUser(u);
 		u.addCertificate(cert);
 		if (u.isSuspended()) {
 			cert.setSuspended(true);
 			cert.setSuspensionReason(u.getSuspensionReason());
 		}
 
 		HibernateFactory.getSession().saveOrUpdate(cert);
 		HibernateFactory.getSession().saveOrUpdate(u);
 
 	}
 
 	public void addCertificate(VOMSUser u, X509Certificate x509Cert) {
 
 		assert u != null : "User must be non-null!";
 		assert x509Cert != null : "Certificate must be non-null!";
 
 		// Assume the certificate have been already validated
 		// at this stage.
 
 		String caDN = DNUtil.getOpenSSLSubject(x509Cert.getIssuerX500Principal());
 		VOMSCA ca = VOMSCADAO.instance().getByName(caDN);
 
 		if (ca == null)
 			throw new NoSuchCAException("CA '" + caDN + "' not recognized!");
 
 		Certificate cert = CertificateDAO.instance().find(x509Cert);
 
 		if (cert != null)
 			throw new AlreadyExistsException("Certificate already bound!");
 
 		cert = new Certificate();
 
 		String subjectString = DNUtil.getOpenSSLSubject(x509Cert
 				.getSubjectX500Principal());
 		cert.setSubjectString(subjectString);
 		cert.setCreationTime(new Date());
 		cert.setSuspended(false);
 		cert.setCa(ca);
 
 		cert.setUser(u);
 
 		if (u.isSuspended()) {
 			cert.setSuspended(true);
 			cert.setSuspensionReason(u.getSuspensionReason());
 		}
 
 		u.addCertificate(cert);
 
 		HibernateFactory.getSession().saveOrUpdate(cert);
 		HibernateFactory.getSession().saveOrUpdate(u);
 
 	}
 
 	public void addToGroup(VOMSUser u, VOMSGroup g) {
 
 		log.debug("Adding user \"" + u + "\" to group \"" + g + "\".");
 
 		if (!HibernateFactory.getSession().contains(u)) {
 
 			VOMSUser checkUser = findById(u.getId());
 
 			if (checkUser == null)
 				throw new NoSuchUserException("User \"" + u
 						+ "\" not found in database.");
 		}
 
 		// Check that the group exists
 		if (VOMSGroupDAO.instance().findByName(g.getName()) == null)
 			throw new NoSuchGroupException("Group \"" + g
 					+ "\" is not defined in database.");
 
 		VOMSMapping m = new VOMSMapping(u, g, null);
 		if (u.getMappings().contains(m))
 			throw new AlreadyMemberException("User \"" + u
 					+ "\" is already a member of group \"" + g + "\".");
 
 		u.getMappings().add(m);
 
 		HibernateFactory.getSession().save(u);
 
 	}
 
 	public void assignRole(VOMSUser u, VOMSGroup g, VOMSRole r) {
 
 		u.assignRole(g, r);
 		HibernateFactory.getSession().update(u);
 
 	}
 
 	private void checkNullFields(VOMSUser usr) {
 
 		if (!VOMSConfiguration.instance().getBoolean(
 				"voms.admin.compatibility-mode", true)) {
 
 			if (usr.getName() == null)
 				throw new NullArgumentException(
 						"Please specify a name for the user!");
 
 			if (usr.getSurname() == null)
 				throw new NullArgumentException(
 						"Please specify a surname for the user!");
 
 			if (usr.getInstitution() == null)
 				throw new NullArgumentException(
 						"Please specify an instituion for the user!");
 
 			if (usr.getAddress() == null)
 				throw new NullArgumentException(
 						"Please specify an address for the user!");
 
 			if (usr.getPhoneNumber() == null)
 				throw new NullArgumentException(
 						"Please specify a phone number for the user!");
 
 			if (usr.getEmailAddress() == null)
 				throw new NullArgumentException(
 						"Please specify an email address for the user!");
 
 		} else {
 
 			if (usr.getDn() == null)
 				throw new NullArgumentException(
 						"Please specify a dn for the user!");
 
 			if (usr.getEmailAddress() == null)
 				throw new NullArgumentException(
 						"Please specify an email address for the user!");
 		}
 
 	}
 
 	public int countMatches(String searchString) {
 
 		String sString = "%" + searchString + "%";
 
 		String countString = "select count(distinct u) from VOMSUser u join u.certificates as cert where lower(u.surname) like lower(:searchString) "
 				+ "or lower(u.name) like lower(:searchString) or u.emailAddress like :searchString "
 				+ " or lower(u.institution) like lower(:searchString) "
 				+ " or cert.subjectString like(:searchString) or cert.ca.subjectString like(:searchString) "
 				+ " order by u.surname asc";
 
 		Query q = HibernateFactory.getSession().createQuery(countString);
 		q.setString("searchString", sString);
 
 		Long count = (Long) q.uniqueResult();
 
 		return count.intValue();
 
 	}
 
 	public Long countUsers() {
 
 		Query q = HibernateFactory.getSession().createQuery(
 				"select count(*) from VOMSUser");
 
 		Long count = (Long) q.uniqueResult();
 
 		return count;
 	}
 
 	public VOMSUser create(String dn, String caDN, String cn, String certURI,
 			String emailAddress) {
 
 		if (dn == null)
 			throw new NullArgumentException("dn must be non-null!");
 
 		if (caDN == null)
 			throw new NullArgumentException("ca must be non-null!");
 
 		if (emailAddress == null)
 			throw new NullArgumentException("emailAddress must be non-null!");
 
 		VOMSUser u = findByDNandCA(dn, caDN);
 
 		if (u != null)
 			throw new UserAlreadyExistsException(
 					"User "
 							+ u
 							+ " already in org.glite.security.voms.admin.persistence.error!");
 
 		caDN = DNUtil.normalizeDN(caDN);
 
 		VOMSCA ca = VOMSCADAO.instance().getByName(caDN);
 
 		if (ca == null)
 			throw new NoSuchCAException("Unknown ca " + caDN
 					+ ". Will not create user " + dn);
 
 		u = new VOMSUser();
 
 		dn = DNUtil.normalizeDN(dn);
 
 		u.setEmailAddress(emailAddress);
 
 		log.debug("Creating user \"" + u + "\".");
 
 		// Add user to default VO group
 
 		VOMSGroup voGroup = VOMSGroupDAO.instance().getVOGroup();
 		HibernateFactory.getSession().save(u);
 
 		addToGroup(u, voGroup);
 
 		return u;
 	}
 	
 	public VOMSUser create(VOMSUserJSON usr, String certificateSubject, String caSubject){
 		
 		VOMSUser u = VOMSUser.fromVOMSUserJSON(usr);
 		
 		u.setDn(certificateSubject);
 		return create(u, caSubject);
 		
 	}
 	
 	protected Certificate createCertificate(String certificateSubject, String caSubject){
 		
 		CertificateDAO certDAO = CertificateDAO.instance();
 		
 		return certDAO.create(certificateSubject, caSubject);
 		
 		
 	}
 	protected Certificate lookupCertificate(String certificateSubject, String caSubject){
 		
 		CertificateDAO certDAO = CertificateDAO.instance();
 		Certificate cert = certDAO.findByDNCA(certificateSubject, caSubject);
 		
 		return cert;
 	}
 	
 	
 	protected Certificate assertCertificateIsNotBound(String certificateSubject, String caSubject){
 		
 		Certificate cert = lookupCertificate(certificateSubject, caSubject);
 		
 		if (cert != null)
 			throw new UserAlreadyExistsException("A user holding a certificate with the following subject '" + certificateSubject
 					+ "' already exists in this VO.");
 		return cert;
 		
 	}
 	
 	
 	
 	protected VOMSUser initUserMembership(VOMSUser user){
 		
 		Calendar c = Calendar.getInstance();
 		
 		// Membership start and end time 
 		user.setCreationTime(c.getTime());
 
 		// Default lifetime for membership is 12 months
 		int lifetime = VOMSConfiguration.instance().getInt(
 				VOMSConfigurationConstants.DEFAULT_MEMBERSHIP_LIFETIME, 12);
 
 		c.add(Calendar.MONTH, lifetime);
 		user.setEndTime(c.getTime());
 		
 		return user;
 		
 	}
 	
 	
 	public VOMSUser create(VOMSUser usr, String caDN) {
 
 		checkNullFields(usr);
 
 		Certificate cert = assertCertificateIsNotBound(usr.getDn(), caDN);
 		
 		// Initialize user membership 
 		initUserMembership(usr);
 		
 		// Create certificate and link it to the membership
 		cert = createCertificate(usr.getDn(), caDN);
 		
 		usr.addCertificate(cert);
 		
 		HibernateFactory.getSession().save(usr);
 		
 		// Add user to VO root group
 		VOMSGroup voGroup = VOMSGroupDAO.instance().getVOGroup();
 		usr.addToGroup(voGroup);
 
 		EventManager.dispatch(new UserCreatedEvent(usr));
 		return usr;
 
 	}
 
 	public VOMSUser create(VOMSUser usr) {
 
 		return create(usr, null);
 
 	}
 
 	public VOMSUserAttribute createAttribute(VOMSUser u, String attrName,
 			String attrDesc, String value) {
 
 		if (u.getAttributeByName(attrName) != null)
 			throw new AttributeAlreadyExistsException("Attribute \"" + attrName
 					+ "\" already defined for user \"" + u + "\".");
 
 		VOMSAttributeDescription desc = VOMSAttributeDAO.instance()
 				.getAttributeDescriptionByName(attrName);
 
 		if (desc == null)
 			desc = VOMSAttributeDAO.instance().createAttributeDescription(
 					attrName, attrDesc);
 
 		log.debug("Creating attribute \"(" + attrName + "," + value
 				+ ")\" for user \"" + u + "\".");
 		VOMSUserAttribute val = VOMSUserAttribute.instance(desc, value, u);
 		u.addAttribute(val);
 		return val;
 
 	}
 
 	public void delete(Long userId) {
 
 		VOMSUser u = findById(userId);
 
 		if (u == null)
 			throw new NoSuchUserException("User identified by \"" + userId
 					+ "\" not found in database!");
 		try {
 
 			delete(u);
 
 		} catch (ObjectNotFoundException e) {
 			// Still don't understand why sometimes findById fails in returnin
 			// null...
 			throw new NoSuchUserException("User identified by \"" + userId
 					+ "\" not found in database!");
 		}
 	}
 
 	public void delete(VOMSUser u) {
 
 		log.debug("Deleting user \"" + u + "\".");
 
 		u.getCertificates().clear();
 		u.getMappings().clear();
 		u.getAttributes().clear();
 		u.getAupAcceptanceRecords().clear();
 		u.getPersonalInformations().clear();
 		u.getTasks().clear();
 
 		DAOFactory.instance().getRequestDAO().deleteRequestFromUser(u);
 
 		HibernateFactory.getSession().delete(u);
 
 		EventManager.dispatch(new UserDeletedEvent(u));
 
 	}
 
 	public void deleteAll() {
 
 		Iterator users = findAll().iterator();
 
 		while (users.hasNext())
 			delete((VOMSUser) users.next());
 
 	}
 
 	public void deleteAttribute(VOMSUser u, String attrName) {
 
 		if (u.getAttributeByName(attrName) == null)
 			throw new NoSuchAttributeException("Attribute \"" + attrName
 					+ "\" not defined for user \"" + u + "\".");
 
 		log.debug("Deleting attribute \"" + attrName + "\" for user \"" + u
 				+ "\".");
 
 		u.deleteAttributeByName(attrName);
 		HibernateFactory.getSession().update(u);
 
 	}
 
 	public void deleteCertificate(Certificate cert) {
 
 		assert cert != null : "Certificate must be non-null!";
 
 		VOMSUser u = cert.getUser();
 		deleteCertificate(u, cert);
 	}
 
 	public void deleteCertificate(VOMSUser u, Certificate cert) {
 
 		assert u != null : "User must be non-null!";
 		assert cert != null : "Certificate must be non-null!";
 
 		if (u.getCertificates().size() == 1 && u.hasCertificate(cert))
 			throw new VOMSException(
 					"User has only one certificate registered, so it cannot be removed!");
 
 		if (!u.getCertificates().remove(cert)) {
 			// This should never happen
 			throw new VOMSDatabaseException(
 					"Inconsistent database! It was not possible to remove certificate '"
 							+ cert
 							+ "' from user '"
 							+ u
 							+ "', even if it seemed user actually possessed such certificate.");
 		}
 
 	}
 
 	public void dismissRole(VOMSUser u, VOMSGroup g, VOMSRole r) {
 
 		u.dismissRole(g, r);
 		HibernateFactory.getSession().update(u);
 
 	}
 
 	public VOMSUser findBySubject(String subject) {
 		if (subject == null)
 			throw new NullArgumentException("subject cannot be null!");
 
 		String query = "select u from org.glite.security.voms.admin.persistence.model.VOMSUser u join u.certificates c where c.subjectString = :subjectString";
 
 		Query q = HibernateFactory.getSession().createQuery(query);
 
 		q.setString("subjectString", subject);
 
 		VOMSUser u = (VOMSUser) q.uniqueResult();
 
 		return u;
 
 	}
 
 	public VOMSUser findByCertificate(String subject, String issuer) {
 
 		if (subject == null)
 			throw new NullArgumentException("subject cannot be null!");
 
 		if (issuer == null)
 			throw new NullArgumentException("issuer cannot be null!");
 
 		String normalizedSubject = DNUtil.normalizeDN(subject);
 		String normalizedIssuer = DNUtil.normalizeDN(issuer);
 
 		String query = "select u from org.glite.security.voms.admin.persistence.model.VOMSUser u join u.certificates c where c.subjectString = :subjectString and c.ca.subjectString = :issuerSubjectString";
 
 		Query q = HibernateFactory.getSession().createQuery(query);
 
 		q.setString("subjectString", normalizedSubject);
 		q.setString("issuerSubjectString", normalizedIssuer);
 
 		VOMSUser u = (VOMSUser) q.uniqueResult();
 
 		return u;
 	}
 
 	public VOMSUser findByDNandCA(String dn, String caDN) {
 
 		if (dn == null)
 			throw new NullArgumentException("dn must be non-null!");
 
 		if (caDN == null)
 			throw new NullArgumentException("ca must be non-null!");
 
 		return findByCertificate(dn, caDN);
 
 	}
 
 	public VOMSUser findByEmail(String emailAddress) {
 
 		if (emailAddress == null)
 			throw new NullArgumentException("emailAddress must be non-null!");
 
 		String query = "from org.glite.security.voms.admin.persistence.model.VOMSUser as u  where u.emailAddress = :emailAddress";
 		Query q = HibernateFactory.getSession().createQuery(query);
 
 		q.setString("emailAddress", emailAddress);
 
 		return (VOMSUser) q.uniqueResult();
 
 	}
 
 	public VOMSUser findById(Long userId) {
 
 		return (VOMSUser) HibernateFactory.getSession().get(VOMSUser.class,
 				userId);
 	}
 
 	public List findAll() {
 
 		Query q = HibernateFactory.getSession().createQuery(
 				"select u from VOMSUser u order by u.surname asc");
 
 		List result = q.list();
 
 		return result;
 	}
 
 	
 	private SearchResults paginatedFind(Query q, Query countQuery, String searchString, int firstResults, int maxResults) {
 		
 		SearchResults res = SearchResults.instance();
 		
 		res.setSearchString(searchString);
 		 
 		q.setFirstResult(firstResults);
 		q.setMaxResults(maxResults);
 		
 		res.setResults(q.list());
 		
 		Long count = (Long) countQuery.uniqueResult();
 		
 		res.setCount(count.intValue());
 		
 		res.setFirstResult(firstResults);
 		res.setResultsPerPage(maxResults);
 
 		return res;
 	}
 	
 	private String getUserOrderClause(){
 		
 		// If endTime is ignored by configuration, we should not consider
 		// it for sorting the search results
 		boolean endTimeDisabled = VOMSConfiguration
 					.instance()
 					.getBoolean(VOMSConfigurationConstants.DISABLE_MEMBERSHIP_END_TIME, 
 								false);
 		
 		if (endTimeDisabled)
 			return "order by u.surname asc";
 		else
 			return "order by u.endTime asc, u.surname asc";
 		
 	}
 	
 	public SearchResults findAll(int firstResults, int maxResults) {
 
 		
 		String query = "from VOMSUser as u "+getUserOrderClause(); 
 		
 		Query q = HibernateFactory.getSession().createQuery(
 				query);
 		
 		Query countQ = HibernateFactory.getSession().createQuery(
 				"select count(*) from VOMSUser");
 		
 		return paginatedFind(q, countQ, null, firstResults, maxResults);
 	}
 
 	public List findAllNames() {
 
 		String query = "select subjectString, ca.subjectString from Certificate";
 
 		return HibernateFactory.getSession().createQuery(query).list();
 
 	}
 
 	public VOMSUser getByDNandCA(String userDN, String caDN) {
 
 		String queryString = "from Certificate where subjectString = :userDN and ca.subjectString = :caDN";
 
 		Query q = HibernateFactory.getSession().createQuery(queryString);
 
 		q.setString("userDN", userDN);
 		q.setString("caDN", caDN);
 
 		Certificate c = (Certificate) q.uniqueResult();
 
 		if (c == null)
 			return null;
 
 		return c.getUser();
 
 	}
 
 	public VOMSUser getByDNandCA(String userDN, VOMSCA ca) {
 		if (userDN == null)
 			throw new NullArgumentException(
 					"Cannot find a user by name given a null userDN!");
 
 		if (ca == null)
 			throw new NullArgumentException(
 					"Cannot find a user by name and ca given a null ca!");
 
 		return getByDNandCA(userDN, ca.getSubjectString());
 
 	}
 
 	public List getUnAssignedRoles(Long userId, Long groupId) {
 
 		VOMSUser u = findById(userId);
 
 		VOMSGroup g = VOMSGroupDAO.instance().findById(groupId);
 
 		List result = new ArrayList();
 
 		Iterator roles = VOMSRoleDAO.instance().getAll().iterator();
 
 		while (roles.hasNext()) {
 
 			VOMSRole r = (VOMSRole) roles.next();
 
 			if (!u.hasRole(g, r))
 				result.add(r);
 		}
 
 		return result;
 	}
 
 	public List getUnsubscribedGroups(Long userId) {
 
 		// Easy, but not performant (leverage HQL!) implementation
 
 		VOMSUser u = findById(userId);
 		List result = new ArrayList();
 
 		Iterator groups = VOMSGroupDAO.instance().getAll().iterator();
 
 		while (groups.hasNext()) {
 
 			VOMSGroup g = (VOMSGroup) groups.next();
 
 			if (!u.isMember(g))
 				result.add(g);
 		}
 
 		return result;
 	}
 
 	public void removeFromGroup(VOMSUser u, VOMSGroup g) {
 
 		log.debug("Removing user \"" + u + "\" from group \"" + g + "\".");
 
 		if (VOMSGroupDAO.instance().findByName(g.getName()) == null)
 			throw new NoSuchGroupException("Group \"" + g
 					+ "\" is not defined in database.");
 
 		u.removeFromGroup(g);
 
 		HibernateFactory.getSession().save(u);
 	}
 
 	public List<VOMSUser> findSuspendedUsers(){
 		
 		String queryString = "from VOMSUser u where u.suspended = true";
 		Query q = HibernateFactory.getSession().createQuery(queryString);
 
 		return q.list();		
 	}
 	
 	public List<VOMSUser> findSuspendedUsers(int firstResult, int maxResults){
 		
 		
 		String queryString = "from VOMSUser u where u.suspended = true";
 		Query q = HibernateFactory.getSession().createQuery(queryString);
 		
 		return q.list();		
 	}
 	
 	public Long countSuspendedUsers(){
 		
 		String queryString = "select count(*) from VOMSUser u where u.suspended = true";
 		Query q = HibernateFactory.getSession().createQuery(queryString);
 		
 		return (Long) q.uniqueResult();
 		
 	}
 	
 	
 	public SearchResults searchExpired(String searchString, int firstResults,
 			int maxResults) {
 		
 		log.debug("searchString:" + searchString + ",firstResults: "
 				+ firstResults + ",maxResults: " + maxResults);
 
 		if (searchString == null || searchString.trim().equals("")
 				|| searchString.length() == 0)
 			return findAll(firstResults, maxResults);
 
 		SearchResults res = SearchResults.instance();
 
 		String sString = "%" + searchString + "%";
 
 		String queryString = "select distinct u from VOMSUser u join u.certificates as cert where u.expired = true and (lower(u.surname) like lower(:searchString) "
 				+ "or lower(u.name) like lower(:searchString) or u.emailAddress like :searchString "
 				+ " or lower(u.institution) like lower(:searchString) "
 				+ " or cert.subjectString like(:searchString) or cert.ca.subjectString like(:searchString))"
 				+ getUserOrderClause();
 
 		Query q = HibernateFactory.getSession().createQuery(queryString);
 
 		q.setString("searchString", sString);
 		q.setFirstResult(firstResults);
 		q.setMaxResults(maxResults);
 
 		res.setCount(countMatches(searchString));
 		res.setFirstResult(firstResults);
 		res.setResultsPerPage(maxResults);
 		res.setResults(q.list());
 		res.setSearchString(searchString);
 
 		return res;
 	}
 	
 	public SearchResults searchWithPendingAUPRequest(String searchString, 
 		int firstResults,
 		int maxResults) {
 		
 		AUP aup = DAOFactory.instance().getAUPDAO().getVOAUP();
 		
 		String queryString = "from VOMSUser u join u.tasks as t join u.certificates as cert " +
 			"where t.class = SignAUPTask and t.status != 'COMPLETED' " +
 			"and t.aup = :aup";
 		
 		String commonSearchString = "and (lower(u.surname) like lower(:searchString) "
 			+ "or lower(u.name) like lower(:searchString) " 
 			+ "or u.emailAddress like :searchString "
 			+ "or lower(u.institution) like lower(:searchString) "
 			+ "or cert.subjectString like(:searchString) "
 			+ "or cert.ca.subjectString like(:searchString))";
 		
 		if (searchStringEmpty(searchString)){
 			
 			
 			Query q = HibernateFactory.getSession()
 				.createQuery("select distinct u "+queryString)
 				.setEntity("aup", aup);
 			
			String countQuery = String.format("select count(u) %s", queryString);
 			Query countQ = HibernateFactory.getSession()
 				.createQuery(countQuery)
 				.setEntity("aup", aup);
 			
 			return paginatedFind(q, countQ,null,firstResults, maxResults);
 		}else{
 			String sString = "%" + searchString + "%";
 			String commonQueryPart = String.format("%s %s", 
 				queryString, commonSearchString);
 			
 			Query q = HibernateFactory.getSession()
 					.createQuery(String.format("select distinct u %s", commonQueryPart))
 					.setEntity("aup", aup);
 			
 			Query count = HibernateFactory.getSession()
				.createQuery(String.format("select count(u) %s", commonQueryPart))
 				.setEntity("aup", aup);
 			
 			q.setString("searchString", sString);
 			count.setString("searchString", sString);
 			
 			return paginatedFind(q, count, searchString, firstResults, maxResults);
 		}
 		
 	}
 	
 	public SearchResults searchSuspended(String searchString, int firstResults,
 			int maxResults) {
 
 		
 		if (searchStringEmpty(searchString)){	
 			
 			Query q = HibernateFactory.getSession().createQuery("from VOMSUser u where u.suspended = true");
 			Query countQ = HibernateFactory.getSession().createQuery("select count(*) from VOMSUser u where u.suspended = true");
 			
 			return paginatedFind(q, countQ,null,firstResults, maxResults);
 			
 			
 		}else{
 			
 			String sString = "%" + searchString + "%";
 			
 			String commonQueryPart = "from VOMSUser u join u.certificates as cert where u.suspended = true and (lower(u.surname) like lower(:searchString) "
 					+ "or lower(u.name) like lower(:searchString) or u.emailAddress like :searchString "
 					+ " or lower(u.institution) like lower(:searchString) "
 					+ " or cert.subjectString like(:searchString) or cert.ca.subjectString like(:searchString))"
 					+ getUserOrderClause();
 			
 			// FIXME: this is *UGLY*, use Criteria API instead
 			Query q = HibernateFactory.getSession().createQuery(String.format("select distinct u %s", commonQueryPart));
 			Query count = HibernateFactory.getSession().createQuery(String.format("select count(*) %s", commonQueryPart));
 
 			q.setString("searchString", sString);
 			count.setString("searchString", sString);
 			
 			return paginatedFind(q, count, searchString, firstResults, maxResults);
 			
 		}
 	
 	}
 	
 	private boolean searchStringEmpty(String searchString){
 		
 		return (searchString == null || 
 				searchString.trim().equals("")
 				|| searchString.length() == 0);
 		
 	}
 	
 	public SearchResults search(String searchString, int firstResults,
 			int maxResults) {
 		
 		log.debug("searchString:" + searchString + ",firstResults: "
 				+ firstResults + ",maxResults: " + maxResults);
 
 		if (searchStringEmpty(searchString))
 			return findAll(firstResults, maxResults);
 
 		String sString = "%" + searchString + "%";
 
 		String commonPart = "from VOMSUser u join u.certificates as cert where lower(u.surname) like lower(:searchString) "
 				+ "or lower(u.name) like lower(:searchString) or u.emailAddress like :searchString "
 				+ " or lower(u.institution) like lower(:searchString) "
 				+ " or cert.subjectString like(:searchString) or cert.ca.subjectString like(:searchString) "
 				+ getUserOrderClause();
 		
 		Query q = HibernateFactory.getSession().createQuery(String.format("select distinct u %s", commonPart));
 		Query count = HibernateFactory.getSession().createQuery(String.format("select count(*) %s", commonPart));
 		
 		q.setString("searchString", sString);
 		count.setString("searchString", sString);
 		
 		return paginatedFind(q, count, searchString, firstResults, maxResults);
 
 	}
 
 	public VOMSUserAttribute setAttribute(VOMSUser u, String attrName,
 			String attrValue) {
 
 		VOMSAttributeDescription desc = VOMSAttributeDAO.instance()
 				.getAttributeDescriptionByName(attrName);
 
 		log.debug("AttributeDescription:" + desc);
 
 		if (desc == null)
 			throw new NoSuchAttributeException("Attribute '" + attrName
 					+ "' is not defined in this vo.");
 
 		if (!VOMSAttributeDAO.instance().isAttributeValueAlreadyAssigned(u,
 				desc, attrValue)) {
 
 			VOMSUserAttribute val = u.getAttributeByName(desc.getName());
 			if (val == null) {
 				val = VOMSUserAttribute.instance(desc, attrValue, u);
 				u.addAttribute(val);
 			} else
 				val.setValue(attrValue);
 
 			return val;
 		}
 
 		throw new AttributeValueAlreadyAssignedException(
 				"Value '"
 						+ attrValue
 						+ "' for attribute '"
 						+ attrName
 						+ "' has been already assigned to another user in this vo! Choose a different value.");
 
 	}
 
 	public VOMSUser update(VOMSUser u) {
 
 		HibernateFactory.getSession().update(u);
 		return u;
 
 	}
 	
 
 	public Long countExpiringUsers(Integer intervalInDays) {
 		Criteria crit = HibernateFactory.getSession().createCriteria(
 				VOMSUser.class);
 
 		Calendar cal = Calendar.getInstance();
 
 		Date now = cal.getTime();
 		cal.add(Calendar.DAY_OF_YEAR, intervalInDays);
 
 		Date intervalDate = cal.getTime();
 
 		crit.add(Restrictions.between("endTime", now, intervalDate));
 		crit.add(Restrictions.eq("suspended", false));
 		
 		crit.setProjection(Projections.rowCount());
 		
 		return (Long)crit.list().get(0);
 		
 	}
 	
 	
 	public List<VOMSUser> findExpiringUsers(Integer intervalInDays) {
 
 		Criteria crit = HibernateFactory.getSession().createCriteria(
 				VOMSUser.class);
 
 		Calendar cal = Calendar.getInstance();
 
 		Date now = cal.getTime();
 		cal.add(Calendar.DAY_OF_YEAR, intervalInDays);
 
 		Date intervalDate = cal.getTime();
 
 		crit.add(Restrictions.between("endTime", now, intervalDate));
 		crit.add(Restrictions.eq("suspended", false));
 		crit.addOrder(Order.asc("endTime"));
 
 		return crit.list();
 
 	}
 
 }
