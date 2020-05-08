 package gov.nih.nci.security.dao;
 
 /**
  *
  *<!-- LICENSE_TEXT_START -->
  *
  *The NCICB Common Security Module (CSM) Software License, Version 3.0 Copyright
  *2004-2005 Ekagra Software Technologies Limited ('Ekagra')
  *
  *Copyright Notice.  The software subject to this notice and license includes both
  *human readable source code form and machine readable, binary, object code form
  *(the 'CSM Software').  The CSM Software was developed in conjunction with the
  *National Cancer Institute ('NCI') by NCI employees and employees of Ekagra.  To
  *the extent government employees are authors, any rights in such works shall be
  *subject to Title 17 of the United States Code, section 105.    
  *
  *This CSM Software License (the 'License') is between NCI and You.  'You (or
  *'Your') shall mean a person or an entity, and all other entities that control,
  *are controlled by, or are under common control with the entity.  'Control' for
  *purposes of this definition means (i) the direct or indirect power to cause the
  *direction or management of such entity, whether by contract or otherwise, or
  *(ii) ownership of fifty percent (50%) or more of the outstanding shares, or
  *(iii) beneficial ownership of such entity.  
  *
  *This License is granted provided that You agree to the conditions described
  *below.  NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up,
  *no-charge, irrevocable, transferable and royalty-free right and license in its
  *rights in the CSM Software to (i) use, install, access, operate, execute, copy,
  *modify, translate, market, publicly display, publicly perform, and prepare
  *derivative works of the CSM Software; (ii) distribute and have distributed to
  *and by third parties the CSM Software and any modifications and derivative works
  *thereof; and (iii) sublicense the foregoing rights set out in (i) and (ii) to
  *third parties, including the right to license such rights to further third
  *parties.  For sake of clarity, and not by way of limitation, NCI shall have no
  *right of accounting or right of payment from You or Your sublicensees for the
  *rights granted under this License.  This License is granted at no charge to You.
  *
  *1.	Your redistributions of the source code for the Software must retain the
  *above copyright notice, this list of conditions and the disclaimer and
  *limitation of liability of Article 6 below.  Your redistributions in object code
  *form must reproduce the above copyright notice, this list of conditions and the
  *disclaimer of Article 6 in the documentation and/or other materials provided
  *with the distribution, if any.
  *2.	Your end-user documentation included with the redistribution, if any, must
  *include the following acknowledgment: 'This product includes software developed
  *by Ekagra and the National Cancer Institute.'  If You do not include such
  *end-user documentation, You shall include this acknowledgment in the Software
  *itself, wherever such third-party acknowledgments normally appear.
  *
  *3.	You may not use the names 'The National Cancer Institute', 'NCI' 'Ekagra
  *Software Technologies Limited' and 'Ekagra' to endorse or promote products
  *derived from this Software.  This License does not authorize You to use any
  *trademarks, service marks, trade names, logos or product names of either NCI or
  *Ekagra, except as required to comply with the terms of this License.
  *
  *4.	For sake of clarity, and not by way of limitation, You may incorporate this
  *Software into Your proprietary programs and into any third party proprietary
  *programs.  However, if You incorporate the Software into third party proprietary
  *programs, You agree that You are solely responsible for obtaining any permission
  *from such third parties required to incorporate the Software into such third
  *party proprietary programs and for informing Your sublicensees, including
  *without limitation Your end-users, of their obligation to secure any required
  *permissions from such third parties before incorporating the Software into such
  *third party proprietary software programs.  In the event that You fail to obtain
  *such permissions, You agree to indemnify NCI for any claims against NCI by such
  *third parties, except to the extent prohibited by law, resulting from Your
  *failure to obtain such permissions.
  *
  *5.	For sake of clarity, and not by way of limitation, You may add Your own
  *copyright statement to Your modifications and to the derivative works, and You
  *may provide additional or different license terms and conditions in Your
  *sublicenses of modifications of the Software, or any derivative works of the
  *Software as a whole, provided Your use, reproduction, and distribution of the
  *Work otherwise complies with the conditions stated in this License.
  *
  *6.	THIS SOFTWARE IS PROVIDED 'AS IS,' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
  *(INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,
  *NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED.  IN NO
  *EVENT SHALL THE NATIONAL CANCER INSTITUTE, EKAGRA, OR THEIR AFFILIATES BE LIABLE
  *FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  *DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  *SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  *CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
  *TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  *THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  *<!-- LICENSE_TEXT_END -->
  *
  */
 
 
 import gov.nih.nci.logging.api.logger.hibernate.HibernateSessionFactoryHelper;
 import gov.nih.nci.security.authorization.ObjectAccessMap;
 import gov.nih.nci.security.authorization.ObjectPrivilegeMap;
 import gov.nih.nci.security.authorization.domainobjects.Application;
 import gov.nih.nci.security.authorization.domainobjects.ApplicationContext;
 import gov.nih.nci.security.authorization.domainobjects.Group;
 import gov.nih.nci.security.authorization.domainobjects.InstanceLevelMappingElement;
 import gov.nih.nci.security.authorization.domainobjects.Privilege;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionElement;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionElementPrivilegeContext;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionGroupRoleContext;
 import gov.nih.nci.security.authorization.domainobjects.Role;
 import gov.nih.nci.security.authorization.domainobjects.User;
 import gov.nih.nci.security.authorization.domainobjects.UserGroupRoleProtectionGroup;
 import gov.nih.nci.security.authorization.jaas.AccessPermission;
 import gov.nih.nci.security.dao.hibernate.ProtectionGroupProtectionElement;
 import gov.nih.nci.security.dao.hibernate.UserGroup;
 import gov.nih.nci.security.exceptions.CSConfigurationException;
 import gov.nih.nci.security.exceptions.CSDataAccessException;
 import gov.nih.nci.security.exceptions.CSException;
 import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
 import gov.nih.nci.security.exceptions.CSTransactionException;
 import gov.nih.nci.security.util.ObjectUpdater;
 import gov.nih.nci.security.util.StringEncrypter;
 import gov.nih.nci.security.util.StringUtilities;
 import gov.nih.nci.security.util.StringEncrypter.EncryptionException;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.security.Principal;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.security.auth.Subject;
 
 import org.hibernate.Criteria;
 import org.hibernate.HibernateException;
 import org.hibernate.PropertyValueException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.criterion.Example;
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.exception.ConstraintViolationException;
 import org.hibernate.exception.GenericJDBCException;
 
 import org.apache.log4j.Logger;
 
 
 /**
  * @author parmarv
  *
  */
 public class AuthorizationDAOImpl implements AuthorizationDAO {
 
 	static final Logger log = Logger.getLogger(AuthorizationDAOImpl.class.getName());
 	
 	/**
 	 * auditLog is an instance of Logger , which is used for Audit Logging
 	 */
 	private static final Logger auditLog = Logger.getLogger("CSM.Audit.Logging.Event.Authorization");		
 
 	
 	private SessionFactory sf = null;
 
 	private Application application = null;
 	
 	private boolean isEncryptionEnabled  = true;
 
 	private String typeOfAccess = "MIXED";
 	private static final String SEPERATOR = "#@#";
 
 	private String localUserOrGroupName = "";
 	
 	private int cacheLevel = 0;
 	
 	private HashMap localCache = new HashMap();
 
 	public AuthorizationDAOImpl(SessionFactory sf, String applicationContextName) throws CSConfigurationException {
 		setHibernateSessionFactory(sf);
 		Application app;
 		try
 		{
 			app = this.getApplicationByName(applicationContextName);
 		}
 		catch (CSObjectNotFoundException e)
 		{
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|"
 								+ applicationContextName
 								+ "||AuthorizationDAOImpl|Failure|No Application found for the Context Name|");
 			throw new CSConfigurationException(
 					"No Application found for the Context Name. "+e.getMessage());
 		}
 		if (app == null) {
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|"
 								+ applicationContextName
 								+ "||AuthorizationDAOImpl|Failure|No Application found for the Context Name|");
 			throw new CSConfigurationException(
 					"Unable to retrieve Application with this Context Name");
 		}
 		this.setApplication(app);
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|"
 							+ applicationContextName
 							+ "||AuthorizationDAOImpl|Success|Instantiated AuthorizationDAOImpl|");
 		
 	}
 
 	public AuthorizationDAOImpl(SessionFactory sf, String applicationContextName, String userOrGroupName, boolean isUserName) 
 	{
 		setHibernateSessionFactory(sf);
 		try {
 			Application app = this.getApplicationByName(applicationContextName);
 			if (app == null) {
 				if (log.isDebugEnabled())
 					log.debug("Authorization|" + applicationContextName + "||AuthorizationDAOImpl|Failure|No Application found for the Context Name|");
 				throw new Exception("Unable to retrieve Application with this Context Name");
 			}
 			this.setApplication(app);
 
 		} catch (Exception ex) {
 			if (log.isDebugEnabled())
 				log.debug("Authorization|" + applicationContextName	+ "||AuthorizationDAOImpl|Failure|Cannot instantiate AuthorizationDAOImpl|"	+ ex.getMessage());
 			throw new RuntimeException(	"Unable to Instantiate the AuthorizationDAOImpl");
 		}
 		
 		populateCache(userOrGroupName, isUserName);
 		
 		localUserOrGroupName = userOrGroupName;
 		if (isUserName)
 			cacheLevel = 1;
 		else
 			cacheLevel = 2;
 		
 		if (log.isDebugEnabled())
 			log.debug("Authorization|" + applicationContextName + "||AuthorizationDAOImpl|Success|Instantiated AuthorizationDAOImpl|");
 		
 	}
 	
 	
 	private void populateCache(String userOrGroupName, boolean isUserName)
 	{
 		Collection protectionElementPrivilegeContexts = null;
 		if (isUserName)
 		{
 			User user = getUser(userOrGroupName);
 			if (user == null)
 			{
 				throw new RuntimeException ("User Name doesnot Exist");
 			}
 			try
 			{
 				protectionElementPrivilegeContexts = getProtectionElementPrivilegeContextForUser(user.getUserId().toString());
 			}
 			catch (CSObjectNotFoundException e)
 			{
 				throw new RuntimeException ("User Name doesnot Exist");
 			}
 		}
 		else
 		{
 			Group group = new Group();
 			group.setGroupName(userOrGroupName);
 			List groups = getObjects(new GroupSearchCriteria(group));
 			if (groups == null || groups.size() == 0)
 			{
 				throw new RuntimeException ("Group Name doesnot Exist");
 			}
 			try
 			{
 				protectionElementPrivilegeContexts = getProtectionElementPrivilegeContextForGroup(((Group)groups.get(0)).getGroupId().toString());
 			}
 			catch (CSObjectNotFoundException e)
 			{
 				throw new RuntimeException ("Group Name doesnot Exist");
 			}
 		}
 		if ( protectionElementPrivilegeContexts != null && protectionElementPrivilegeContexts.size() != 0 )
 		{
 			Iterator iterator = protectionElementPrivilegeContexts.iterator();
 			String key = null;
 			while (iterator.hasNext())
 			{
 				ProtectionElementPrivilegeContext protectionElementPrivilegeContext = (ProtectionElementPrivilegeContext)iterator.next();
 				ProtectionElement protectionElement = protectionElementPrivilegeContext.getProtectionElement();
 				Set privileges = protectionElementPrivilegeContext.getPrivileges();
 				Iterator iterator2 = privileges.iterator();
 				List privilegesName = new ArrayList();
 				while (iterator2.hasNext())
 				{
 					privilegesName.add(((Privilege)iterator2.next()).getName());
 				}
 				if (protectionElement.getAttribute() != null && protectionElement.getAttribute().trim().length() != 0)
 					key = protectionElement.getObjectId() + AuthorizationDAOImpl.SEPERATOR + protectionElement.getAttribute();
 				else
 					key = protectionElement.getObjectId();					
 				localCache.put(key,privilegesName);
 			}
 		}
 		if (log.isDebugEnabled())
 		{
 			if (isUserName)
 				log.debug("Authorization|||populateCache|Success|Loaded Cache for User "+ userOrGroupName +"|");
 			else
 				log.debug("Authorization|||populateCache|Success|Loaded Cache for Group "+ userOrGroupName +"|");
 		}
 	}
 	
 	private boolean checkCachedPermission(String userOrGroupName, String objectId, String attribute, String privilege)
 	{
 		boolean isAllowed = false;
 		String key = null;
 		List privileges = null;
 		if (attribute != null)
 			key = objectId + AuthorizationDAOImpl.SEPERATOR + attribute;
 		else
 			key = objectId;
 		
 		if (localCache.containsKey(key))
 		{
 			privileges = (List) localCache.get(key);
 		}
 		if (privileges != null && (privileges.contains("OWNER") || privileges.contains(privilege)))
 			isAllowed = true;
 		return isAllowed;
 	}
 
 	public void finalize() throws Throwable {
 		super.finalize();
 	}
 
 	public void setHibernateSessionFactory(SessionFactory sf) {
 		this.sf = sf;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.UserProvisioningManager#assignUserToGroup(java.lang.String,
 	 *      java.lang.String)
 	 */
 	public void assignUserToGroup(String userName, String groupName)
 			throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 		try {
 			if (StringUtilities.isBlank(userName)) {
 				throw new CSTransactionException("The userName can't be null");
 			}
 			if (StringUtilities.isBlank(groupName)) {
 				throw new CSTransactionException("The groupName can't be null");
 			}
 
 			Group group = getGroup(groupName);
 			if (group==null) {
 				throw new CSTransactionException("Group does not exist.");
 			}
 
 			User user = getUser(userName);
 			if (user==null) {
 				throw new CSTransactionException("User does not exist.");
 			}
 
 
 			try {
 				user = (User) performEncrytionDecryption(user, true);
 			} catch (EncryptionException e) {
 				throw new CSObjectNotFoundException(e);
 			}
 			log.debug("The Group ID: " + group.getGroupId());
 			log.debug("The User ID: " + user.getUserId());
 			Set groups = getGroups("" + user.getUserId());
 
 			
 			
 			boolean hasGroupAlready = false;
 			if ((null != groups) && (!groups.isEmpty())) {
 				Iterator i = groups.iterator();
 				while (i.hasNext()) {
 					Group temp = (Group) i.next();
 					if (group.getGroupName().equals(temp.getGroupName())) {
 						hasGroupAlready = true;
 						break;
 					}
 				}
 			}
 			
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);			
 			t = s.beginTransaction();
 
 			if (!hasGroupAlready) {
 				UserGroup ug = new UserGroup();
 				ug.setGroup(group);
 				ug.setUser(user);
 				s.save(ug);
 				
 			}
 			
 			t.commit();
 			s.flush();
 		} catch (Exception ex) {
 			log.error(
 					"Fatal error occurred while attempting to associate User "
 							+ userName + " with Group " + groupName, ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 			}
 
 			throw new CSTransactionException(
 					"An error occurred in assignUserToGroup\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 
 			} catch (Exception ex2) {
 			}
 		}
 		auditLog.info("Assigning User " + userName + " to Group " + groupName);
 		
 
 	}
 	
 	public void addGroupsToUser(String userId, String[] groupIds)
 	throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 		try {
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			
 			User user = (User) s.load(User.class,new Long(userId));
 			if(user==null) throw new CSTransactionException("Authorization|||addGroupsToUser || Unable to retrieve User with Id :"+userId);
 			
 			
 			Set groupSet = user.getGroups();
 			if(groupSet==null) groupSet = new HashSet();
 			
 			for (int i = 0; i < groupIds.length; i++) {
 				boolean assigned= false;
 				Iterator iterator = groupSet.iterator();
 				while(iterator.hasNext()){
 					Group group =(Group)iterator.next();
 					if(groupIds[i].equalsIgnoreCase(group.getGroupId().toString()))
 						assigned=true;
 				}
 				if(!assigned){
 					Group group= (Group) s.load(Group.class, Long.parseLong(groupIds[i]));
 					if(group!=null)
 						groupSet.add(group);
 				}
 			}
 			
 			t = s.beginTransaction();
 			s.update(user);
 			t.commit();
 			s.flush();
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||addGroupsToUser|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||addGroupsToUser|Failure|Error occurred in assigning Groups "
 								+ StringUtilities.stringArrayToString(groupIds)
 								+ " to User " + userId + "|" + ex.getMessage());
 			throw new CSTransactionException(
 					"An error occurred in adding Groups to User\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||addGroupsToUser|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||addGroupsToUser|Success|Successful in assigning Groups "
 							+ StringUtilities.stringArrayToString(groupIds)
 							+ " to User " + userId + "|");
 		auditLog.info("Assigning User " + userId + " to Groups");		
 		
 	}
 
 
 
 
 	public void assignGroupsToUser(String userId, String[] groupIds)
 			throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 		try {
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			
 
 			User user = (User) this.getObjectByPrimaryKey(s, User.class,
 					new Long(userId));
 			
 			HashSet newGroups = new HashSet();
 			for (int k = 0; k < groupIds.length; k++) {
 				Group group = (Group) this.getObjectByPrimaryKey(Group.class,
 						groupIds[k]);
 				if (group != null) {
 					newGroups.add(group);
 				}
 			}
 
 			user.setGroups(newGroups);
 			try {
 				user = (User) performEncrytionDecryption(user, true);
 			} catch (EncryptionException e) {
 				throw new CSObjectNotFoundException(e);
 			}
 			t = s.beginTransaction();
 			s.update(user);
 			t.commit();
 			s.flush();
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||assignGroupsToUser|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||assignGroupsToUser|Failure|Error occurred in assigning Groups "
 								+ StringUtilities.stringArrayToString(groupIds)
 								+ " to User " + userId + "|" + ex.getMessage());
 			throw new CSTransactionException(
 					"An error occurred in assigning Groups to User\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||assignGroupsToUser|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||assignGroupsToUser|Success|Successful in assigning Groups "
 							+ StringUtilities.stringArrayToString(groupIds)
 							+ " to User " + userId + "|");
 		auditLog.info("Assigning User " + userId + " to Groups");		
 	}
 	
 	public void addUsersToGroup(String groupId, String[] userIds)
 	throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 		try {
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			
 			Group group = (Group) s.load(Group.class,new Long(groupId));
 			if(group==null) throw new CSTransactionException("Authorization|||addUsersToGroup|| Unable to retrieve Group with Id :"+groupId);
 			
 			Set userSet = group.getUsers();
 						
 			for (int k = 0; k < userIds.length; k++) {
 				boolean assigned= false;
 				Iterator iterator  = userSet.iterator();
 				while(iterator.hasNext()){
 					User user = (User)iterator.next();
 					if(user.getUserId().toString().equalsIgnoreCase(userIds[k]))
 						assigned=true;
 				}
 				if(!assigned){
 					User user = (User) s.load(User.class, Long.parseLong(userIds[k]));
 					if(user!=null)
 						userSet.add(user);
 				}
 			}
 			
 			t = s.beginTransaction();
 			s.update(group);
 			t.commit();
 			s.flush();
 			
 			
 			
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||addUsersToGroup|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||addUsersToGroup|Failure|Error occurred in assigning Users "
 								+ StringUtilities.stringArrayToString(userIds)
 								+ " to Group " + groupId + "|" + ex.getMessage());
 			throw new CSTransactionException(
 					"An error occurred in adding Users to Group\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||addUsersToGroup|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||addUsersToGroup|Success|Successful in assigning Users "
 							+ StringUtilities.stringArrayToString(userIds)
 							+ " to Group " + groupId + "|");
 		auditLog.info("Adding Group " + groupId + " to Users");		
 	}
 
 
 	public void assignUsersToGroup(String groupId, String[] userIds)
 	throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 		try {
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			
 		
 			Group group = (Group) this.getObjectByPrimaryKey(s, Group.class,
 					new Long(groupId));
 			
 			HashSet newUsers = new HashSet();
 			for (int k = 0; k < userIds.length; k++) {
 				User user = (User) this.getObjectByPrimaryKey(User.class,
 						userIds[k]);
 				try {
 					user = (User) performEncrytionDecryption(user, true);
 				} catch (EncryptionException e) {
 					throw new CSObjectNotFoundException(e);
 				}
 				
 				if (user != null) {
 					newUsers.add(user);
 				}
 			}
 		
 			group.setUsers(newUsers);
 			t = s.beginTransaction();
 			s.update(group);
 			t.commit();
 			s.flush();
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||assignUsersToGroup|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||assignUsersToGroup|Failure|Error occurred in assigning Users "
 								+ StringUtilities.stringArrayToString(userIds)
 								+ " to Group " + groupId + "|" + ex.getMessage());
 			throw new CSTransactionException(
 					"An error occurred in assigning Users to Group\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||assignUsersToGroup|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||assignUsersToGroup|Success|Successful in assigning Users "
 							+ StringUtilities.stringArrayToString(userIds)
 							+ " to Group " + groupId + "|");
 		auditLog.info("Assigning Group " + groupId + " to Users");		
 	}
 
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#assignGroupRoleToProtectionGroup(java.lang.String,
 	 *      java.lang.String, java.lang.String)
 	 */
 
 	public void addGroupRoleToProtectionGroup(String protectionGroupId,
 			String groupId, String[] rolesId) throws CSTransactionException {
 
 		Session s = null;
 		Transaction t = null;
         ArrayList roles = new ArrayList();
 		try {
 
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			
 			
 			for (int i = 0; i < rolesId.length; i++){
 				Role role = (Role) this.getObjectByPrimaryKey(s, Role.class,
 						new Long(rolesId[i]));
 				roles.add(role);
 			}
 
 			ProtectionGroup pgroup = (ProtectionGroup) s.load(ProtectionGroup.class, new Long(protectionGroupId));
 			if(pgroup==null) throw new CSTransactionException("Authorization|||addGroupRoleToProtectionGroup || Unable to retrieve Protection Group with Id :"+protectionGroupId);
 
 			Group group = (Group) s.load(Group.class,new Long(groupId));
 			if(group==null) throw new CSTransactionException("Authorization|||addGroupRoleToProtectionGroup || Unable to retrieve Group with Id :"+groupId);
 						
 			Criteria criteria = s
 					.createCriteria(UserGroupRoleProtectionGroup.class);
 			criteria.add(Restrictions.eq("protectionGroup", pgroup));
 			criteria.add(Restrictions.eq("group", group));
 			
 			List list = criteria.list();
 			for(int k=0;k<list.size();k++){
 				UserGroupRoleProtectionGroup ugrpg = (UserGroupRoleProtectionGroup)list.get(k);
 				Role r = ugrpg.getRole();
 				  if(roles.contains(r)){
 				  	roles.remove(r);
 				  }
 			}
 			t = s.beginTransaction();
 			for(int j=0;j<roles.size();j++){
 				Role leftOverRole = (Role)roles.get(j);
 				UserGroupRoleProtectionGroup toBeSaved = new UserGroupRoleProtectionGroup();
 			  	toBeSaved.setGroup(group);
 			  	toBeSaved.setProtectionGroup(pgroup);
 			  	toBeSaved.setRole(leftOverRole);
                 toBeSaved.setUpdateDate(new Date());
                 s.save(toBeSaved);
 			}
 			t.commit();
 			s.flush();
 			auditLog.info("Adding Roles to Group " + group.getGroupName() + " for Protection Group " + pgroup.getProtectionGroupName());
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||addGroupsToUser|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||addGroupRoleToProtectionGroup|Failure|Error Occured in assigning Roles "
 								+ StringUtilities.stringArrayToString(rolesId)
 								+ " to Group "
 								+ groupId
 								+ " and Protection Group"
 								+ protectionGroupId
 								+ "|" + ex.getMessage());
 			throw new CSTransactionException(
 					"An error occurred in adding Protection Group and Roles to a Group\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||addGroupRoleToProtectionGroup|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||GroupRoleToProtectionGroup|Success|Successful in assigning Roles "
 							+ StringUtilities.stringArrayToString(rolesId)
 							+ " to Group "
 							+ groupId
 							+ " and Protection Group"
 							+ protectionGroupId + "|");
 	}
 
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#assignGroupRoleToProtectionGroup(java.lang.String,
 	 *      java.lang.String, java.lang.String)
 	 */
 
 	public void assignGroupRoleToProtectionGroup(String protectionGroupId,
 			String groupId, String[] rolesId) throws CSTransactionException {
 
 		Session s = null;
 		Transaction t = null;
         ArrayList roles = new ArrayList();
 		try {
 
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			
 			
 			for (int i = 0; i < rolesId.length; i++){
 				Role role = (Role) this.getObjectByPrimaryKey(s, Role.class,
 						new Long(rolesId[i]));
 				roles.add(role);
 			}
 
 			ProtectionGroup pgroup = (ProtectionGroup) this
 					.getObjectByPrimaryKey(s, ProtectionGroup.class, new Long(
 							protectionGroupId));
 
 			Group group = (Group) this.getObjectByPrimaryKey(s, Group.class,
 					new Long(groupId));
 			
 						
 			Criteria criteria = s
 					.createCriteria(UserGroupRoleProtectionGroup.class);
 			criteria.add(Restrictions.eq("protectionGroup", pgroup));
 			criteria.add(Restrictions.eq("group", group));
 			
 			List list = criteria.list();
 			t = s.beginTransaction();
 			for(int k=0;k<list.size();k++){
 				UserGroupRoleProtectionGroup ugrpg = (UserGroupRoleProtectionGroup)list.get(k);
 				Role r = ugrpg.getRole();
 				  if(!roles.contains(r)){
 				  	s.delete(ugrpg);
 				  	
 				  }else{
 				  	roles.remove(r);
 				  }
 			}
 			
 			for(int j=0;j<roles.size();j++){
 				Role leftOverRole = (Role)roles.get(j);
 				UserGroupRoleProtectionGroup toBeSaved = new UserGroupRoleProtectionGroup();
 			  	toBeSaved.setGroup(group);
 			  	toBeSaved.setProtectionGroup(pgroup);
 			  	toBeSaved.setRole(leftOverRole);
                 toBeSaved.setUpdateDate(new Date());
                 s.save(toBeSaved);
 			}
 			t.commit();
 			s.flush();
 			auditLog.info("Assigning Roles to Group " + group.getGroupName() + " for Protection Group " + pgroup.getProtectionGroupName());
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||assignGroupsToUser|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||assignGroupRoleToProtectionGroup|Failure|Error Occured in assigning Roles "
 								+ StringUtilities.stringArrayToString(rolesId)
 								+ " to Group "
 								+ groupId
 								+ " and Protection Group"
 								+ protectionGroupId
 								+ "|" + ex.getMessage());
 			throw new CSTransactionException(
 					"An error occurred in assigning Protection Group and Roles to a Group\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||assignGroupRoleToProtectionGroup|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||assignGroupRoleToProtectionGroup|Success|Successful in assigning Roles "
 							+ StringUtilities.stringArrayToString(rolesId)
 							+ " to Group "
 							+ groupId
 							+ " and Protection Group"
 							+ protectionGroupId + "|");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#assignPrivilegesToRole(java.lang.String[],
 	 *      java.lang.String)
 	 */
 
 	public void addPrivilegesToRole(String roleId, String[] privilegeIds)
 			throws CSTransactionException {
 
 		Session s = null;
 		Transaction t = null;
 		try {
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 
 			Role role = (Role) s.load(Role.class,new Long(roleId));
 			if(role==null) throw new CSTransactionException("Authorization|||addPrivilegesToRole|| Unable to retrieve Role with Id :"+roleId);
 			
 			Set<Privilege> privs = role.getPrivileges();
 			
 			for (int k = 0; k < privilegeIds.length; k++) {
 				boolean assigned = false;
 				if(privilegeIds[k]!=null && privilegeIds[k].length()>0){
 					Privilege pr = (Privilege) s.load(Privilege.class,new Long(privilegeIds[k]));
 					if (pr != null) {
 						Iterator it=privs.iterator();
 						while(it.hasNext()){
 							Privilege p = (Privilege)it.next();
 							if(p.equals(pr)) assigned=true;
 						}
 						if(!assigned) privs.add(pr);
 					}
 				}
 			}
 			
 			role.setPrivileges(privs);
 			t = s.beginTransaction();
 			s.update(role);
 			t.commit();
 			s.flush();
 			auditLog.info("Adding Privileges to Role " + role.getName());
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||addPrivilegesToRole|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||addPrivilegesToRole|Failure|Error Occured in assigning Privilege "
 								+ StringUtilities
 										.stringArrayToString(privilegeIds)
 								+ " to Role " + roleId + "|" + ex.getMessage());
 			throw new CSTransactionException(
 					"An error occurred in adding Privileges to Role\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||addPrivilegesToRole|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||addPrivilegesToRole|Success|Success in assigning Privilege "
 							+ StringUtilities.stringArrayToString(privilegeIds)
 							+ " to Role " + roleId + "|");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#assignPrivilegesToRole(java.lang.String[],
 	 *      java.lang.String)
 	 */
 
 	public void assignPrivilegesToRole(String roleId, String[] privilegeIds)
 			throws CSTransactionException {
 
 		Session s = null;
 		Transaction t = null;
 		try {
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			
 
 			Role role = (Role) this.getObjectByPrimaryKey(s, Role.class,
 					new Long(roleId));
 
 			//Set currPriv = role.getPrivileges();
 			Set newPrivs = new HashSet();
 
 			for (int k = 0; k < privilegeIds.length; k++) {
 				log.debug("The new list:" + privilegeIds[k]);
 				Privilege pr = (Privilege) this.getObjectByPrimaryKey(
 						Privilege.class, privilegeIds[k]);
 				if (pr != null) {
 					newPrivs.add(pr);
 				}
 			}
 			role.setPrivileges(newPrivs);
 			t = s.beginTransaction();
 			s.update(role);
 			t.commit();
 			s.flush();
 			auditLog.info("Assigning Privileges to Role " + role.getName());
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||assignPrivilegesToRole|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||assignPrivilegesToRole|Failure|Error Occured in assigning Privilege "
 								+ StringUtilities
 										.stringArrayToString(privilegeIds)
 								+ " to Role " + roleId + "|" + ex.getMessage());
 			throw new CSTransactionException(
 					"An error occurred in assigning Privileges to Role\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||assignPrivilegesToRole|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||assignPrivilegesToRole|Success|Success in assigning Privilege "
 							+ StringUtilities.stringArrayToString(privilegeIds)
 							+ " to Role " + roleId + "|");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#assignProtectionElements(java.lang.String,
 	 *      java.lang.String[], java.lang.String[])
 	 */
 	public void assignProtectionElement(String protectionGroupName,
 			String protectionElementObjectId,
 			String protectionElementAttributeName)
 			throws CSTransactionException {
 
 		Session s = null;
 		Transaction t = null;
 		try {
 
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			t = s.beginTransaction();
 
 			if (StringUtilities.isBlank(protectionGroupName)) {
 				throw new CSTransactionException(
 						"The protectionGroupName can't be null");
 			}
 			if (StringUtilities.isBlank(protectionElementObjectId)) {
 				throw new CSTransactionException(
 						"The protectionElementObjectId can't be null");
 			}
 
 			ProtectionGroup protectionGroup = getProtectionGroup(protectionGroupName);
 			ProtectionElement protectionElement = getProtectionElement(
 					protectionElementObjectId, protectionElementAttributeName);
 
 			Criteria criteria = s
 					.createCriteria(ProtectionGroupProtectionElement.class);
 			criteria.add(Restrictions.eq("protectionGroup", protectionGroup));
 			criteria.add(Restrictions.eq("protectionElement", protectionElement));
 
 			List list = criteria.list();
 
 			if (list.size() == 0) {
 				ProtectionGroupProtectionElement pgpe = new ProtectionGroupProtectionElement();
 				pgpe.setProtectionElement(protectionElement);
 				pgpe.setProtectionGroup(protectionGroup);
 				pgpe.setUpdateDate(new Date());
 
 				s.save(pgpe);
 			} else {
 				throw new CSTransactionException(
 						"This association already exist!");
 			}
 
 			t.commit();
 			s.flush();
 			auditLog.info("Assigning Protection Element with Object Id " + protectionElementObjectId + "Attribute " + protectionElementAttributeName + "to Protection Group" + protectionGroupName);
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||assignProtectionElements|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||assignProtectionElements|Failure|Error Occured in assigning Protection Element with Object Id "
 								+ protectionElementObjectId
 								+ " with protection element attribute "
 								+ protectionElementAttributeName
 								+ " to protection group name: "
 								+ protectionGroupName + "|" + ex.getMessage());
 			throw new CSTransactionException(
 					"An error occurred in assigning Protection Element to Protection Group\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||assignProtectionElements|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||assignProtectionElements|Success|Successful in assigning Protection Element with Object Id "
 							+ protectionElementObjectId
 							+ " with protection element attribute "
 							+ protectionElementAttributeName
 							+ " to protection group name: "
 							+ protectionGroupName + "|");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#assignProtectionElement(java.lang.String,
 	 *      java.lang.String[])
 	 */
 	public void assignProtectionElement(String protectionGroupName,
 			String protectionElementObjectId) throws CSTransactionException {
 
 		this.assignProtectionElement(protectionGroupName,
 				protectionElementObjectId, null);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#assignUserRoleToProtectionGroup(java.lang.String,
 	 *      java.lang.String[], java.lang.String)
 	 */
 	public void addUserRoleToProtectionGroup(String userId,
 			String[] rolesId, String protectionGroupId)
 			throws CSTransactionException {
 		
 		Session s = null;
 		Transaction t = null;
 		ArrayList roles = new ArrayList();
 
 		try {
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			
 			for (int i = 0; i < rolesId.length; i++){
 				Role role = (Role) s.load( Role.class,new Long(rolesId[i]));
 				if(role!=null)
 					roles.add(role);
 			}
 			ProtectionGroup pgroup = (ProtectionGroup) s.load(ProtectionGroup.class, new Long(protectionGroupId));
 			if(pgroup==null) throw new CSTransactionException("Authorization|||addUserRoleToProtectionGroup || Unable to retrieve Protection Group with ID :"+protectionGroupId);
 
 			User user =(User) s.load(User.class,new Long(userId));
 			if(user==null) throw new CSTransactionException("Authorization|||addUserRoleToProtectionGroup || Unable to retrieve User with ID :"+userId);
 			
 			Criteria criteria = s.createCriteria(UserGroupRoleProtectionGroup.class);
 			criteria.add(Restrictions.eq("protectionGroup", pgroup));
 			criteria.add(Restrictions.eq("user", user));
 			
 			List list = criteria.list();
 			for(int k=0;k<list.size();k++){
 				UserGroupRoleProtectionGroup ugrpg = (UserGroupRoleProtectionGroup)list.get(k);
 				Role r = ugrpg.getRole();
 				  if(roles.contains(r)){
 				  	roles.remove(r);
 				  }
 			}
 			t = s.beginTransaction();
 			for(int j=0;j<roles.size();j++){
 				Role leftOverRole = (Role)roles.get(j);
 				UserGroupRoleProtectionGroup toBeSaved = new UserGroupRoleProtectionGroup();
 				toBeSaved.setUser(user);
 			  	toBeSaved.setProtectionGroup(pgroup);
 			  	toBeSaved.setRole(leftOverRole);
                 toBeSaved.setUpdateDate(new Date());
                 s.save(toBeSaved);
 			}
 			t.commit();
 			s.flush();
 			auditLog.info("Adding Roles to User " + user.getLoginName() + " for Protection Group " + pgroup.getProtectionGroupName());
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||addUserRoleToProtectionGroup|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||addGroupRoleToProtectionGroup|Failure|Error Occured in adding Roles "
 								+ StringUtilities.stringArrayToString(rolesId)
 								+ " to User "
 								+ userId
 								+ " and Protection Group"
 								+ protectionGroupId
 								+ "|" + ex.getMessage());
 			throw new CSTransactionException(
 					"An error occurred in adding Protection Group and Roles to a User\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||addUserRoleToProtectionGroup|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||addGroupRoleToProtectionGroup|Success|Successful in assigning Roles "
 							+ StringUtilities.stringArrayToString(rolesId)
 							+ " to User "
 							+ userId
 							+ " and Protection Group"
 							+ protectionGroupId + "|");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#assignUserRoleToProtectionGroup(java.lang.String,
 	 *      java.lang.String[], java.lang.String)
 	 */
 	public void assignUserRoleToProtectionGroup(String userId,
 			String[] rolesId, String protectionGroupId)
 			throws CSTransactionException {
 		
 		Session s = null;
 		Transaction t = null;
 		ArrayList roles = new ArrayList();
 
 		try {
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			
 			for (int i = 0; i < rolesId.length; i++){
 				Role role = (Role) this.getObjectByPrimaryKey(s, Role.class,
 						new Long(rolesId[i]));
 				roles.add(role);
 			}
 			ProtectionGroup pgroup = (ProtectionGroup) this
 					.getObjectByPrimaryKey(s, ProtectionGroup.class, new Long(
 							protectionGroupId));
 
 			User user = (User) this.getObjectByPrimaryKey(s, User.class,
 					new Long(userId));
 			try {
 				user = (User) performEncrytionDecryption(user, true);
 			} catch (EncryptionException e) {
 				throw new CSObjectNotFoundException(e);
 			}
 			
 			Criteria criteria = s.createCriteria(UserGroupRoleProtectionGroup.class);
 			criteria.add(Restrictions.eq("protectionGroup", pgroup));
 			criteria.add(Restrictions.eq("user", user));
 			
 			t = s.beginTransaction();
 			List list = criteria.list();
 			for(int k=0;k<list.size();k++){
 				UserGroupRoleProtectionGroup ugrpg = (UserGroupRoleProtectionGroup)list.get(k);
 				Role r = ugrpg.getRole();
 				  if(!roles.contains(r)){
 				  	s.delete(ugrpg);
 				  	
 				  }else{
 				  	roles.remove(r);
 				  }
 			}
 			
 			for(int j=0;j<roles.size();j++){
 				Role leftOverRole = (Role)roles.get(j);
 				UserGroupRoleProtectionGroup toBeSaved = new UserGroupRoleProtectionGroup();
 			  	toBeSaved.setUser(user);
 			  	toBeSaved.setProtectionGroup(pgroup);
 			  	toBeSaved.setRole(leftOverRole);
                 toBeSaved.setUpdateDate(new Date());
                 s.save(toBeSaved);
 			}
 			
 			t.commit();
 			s.flush();
 			auditLog.info("Assigning Roles to User " + user.getLoginName() + " for Protection Group " + pgroup.getProtectionGroupName());
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||assignUserRoleToProtectionGroup|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||assignGroupRoleToProtectionGroup|Failure|Error Occured in assigning Roles "
 								+ StringUtilities.stringArrayToString(rolesId)
 								+ " to User "
 								+ userId
 								+ " and Protection Group"
 								+ protectionGroupId
 								+ "|" + ex.getMessage());
 			throw new CSTransactionException(
 					"An error occurred in assigning Protection Group and Roles to a User\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||assignUserRoleToProtectionGroup|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||assignGroupRoleToProtectionGroup|Success|Successful in assigning Roles "
 							+ StringUtilities.stringArrayToString(rolesId)
 							+ " to User "
 							+ userId
 							+ " and Protection Group"
 							+ protectionGroupId + "|");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#checkPermission(gov.nih.nci.security.authorization.jaas.AccessPermission,
 	 *      java.lang.String)
 	 */
 	public boolean checkPermission(AccessPermission permission, String userName)
 			throws CSException {
 		if (permission == null) {
 			throw new CSException("permission can't be null !");
 		}
 		String objectId = permission.getName();
 		String privilege = permission.getActions();
 
 		return checkPermission(userName, objectId, privilege);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#checkPermission(gov.nih.nci.security.authorization.jaas.AccessPermission,
 	 *      javax.security.auth.Subject)
 	 */
 	public boolean checkPermission(AccessPermission permission, Subject subject)
 			throws CSException {
 
 		boolean test = false;
 		if (permission == null) {
 			throw new CSException("permission can't be null!");
 		}
 		String objectId = permission.getName();
 		String privilege = permission.getActions();
 		if (subject == null) {
 			throw new CSException("subject can't be null!");
 		}
 		Set ps = subject.getPrincipals();
 		if (ps.size() == 0) {
 			throw new CSException("The subject has no principals!");
 		}
 		Iterator it = ps.iterator();
 
 		while (it.hasNext()) {
 			Principal p = (Principal) it.next();
 			String userName = p.getName();
 			test = this.checkPermission(userName, objectId, privilege);
 			if (test)
 				break;
 		}
 
 		return test;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#checkPermission(java.lang.String,
 	 *      java.lang.String, java.lang.String, java.lang.String)
 	 */
 	public boolean checkPermission(String userName, String objectId,
 			String attributeName, String privilegeName) throws CSException {
 
 		ResultSet rs = null;
 		PreparedStatement preparedStatement = null;
 		boolean test = false;
 		Session s = null;
 
 		Connection connection = null;
 		if (StringUtilities.isBlank(userName)) {
 			throw new CSException("user name can't be null!");
 		}
 		if (StringUtilities.isBlank(objectId)) {
 			throw new CSException("objectId can't be null!");
 		}
 		
 		// Check if cache is enabled for user
 		if (cacheLevel == 1 && localUserOrGroupName.equals(userName))
 			return checkCachedPermission(userName, objectId, attributeName, privilegeName);
 		
 		test = this.checkOwnership(userName, objectId);
 		if (test)
 			return true;
 
 		if (attributeName == null || privilegeName == null) {
 			return false;
 		}
 
 		try {
 
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 
 			connection = s.connection();
 			
 
 			
 			preparedStatement = Queries.getQueryForUserAndGroupForAttribute(userName,
 					objectId, attributeName, privilegeName, this.application.getApplicationId().intValue(), connection);
 			
 			rs = preparedStatement.executeQuery();
 
 			if (rs.next()) {
 				test = true;
 			}
 			rs.close();
 
 			preparedStatement.close();
 
 		} catch (Exception ex) {
 			if (log.isDebugEnabled())
 				log.debug("Failed to get privileges for " + userName + "|"
 						+ ex.getMessage());
 			throw new CSException("Failed to get privileges for " + userName
 					+ "|" + ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 				rs.close();
 				preparedStatement.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||getPrivilegeMap|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 
 		return test;
 	}
 
 	public boolean checkPermission(String userName, String objectId, String attributeName, String attributeValue, String privilegeName) throws CSException {
 
 		ResultSet rs = null;
 		PreparedStatement preparedStatement = null;
 		boolean test = false;
 		Session s = null;
 
 		Connection connection = null;
 		if (StringUtilities.isBlank(userName)) {
 			throw new CSException("user name can't be null!");
 		}
 		if (StringUtilities.isBlank(objectId)) {
 			throw new CSException("objectId can't be null!");
 		}
 		
 		test = this.checkOwnership(userName, objectId);
 		if (test)
 			return true;
 
 		if (attributeName == null || attributeValue == null || privilegeName == null) {
 			return false;
 		}
 
 		try {
 
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			connection = s.connection();
 			
 			
 			preparedStatement = Queries.getQueryForUserAndGroupForAttributeValue(userName,
 					objectId, attributeName, attributeValue, privilegeName, this.application.getApplicationId().intValue(), connection);
 			
 			rs = preparedStatement.executeQuery();
 
 			if (rs.next()) {
 				test = true;
 			}
 			rs.close();
 
 			preparedStatement.close();
 
 		} catch (Exception ex) {
 			if (log.isDebugEnabled())
 				log.debug("Failed to get privileges for " + userName + "|"
 						+ ex.getMessage());
 			throw new CSException("Failed to get privileges for " + userName
 					+ "|" + ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 				rs.close();
 				preparedStatement.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||checkPermissiong|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 
 		return test;
 	}
 
 	
 	public boolean checkPermission(String userName, String objectId,
 			String privilegeName) throws CSException {
 		boolean test = false;
 
 		if (StringUtilities.isBlank(userName)) {
 			throw new CSException("user name can't be null!");
 		}
 		if (StringUtilities.isBlank(objectId)) {
 			throw new CSException("objectId can't be null!");
 		}
 		// Check if cache is enabled for user
 		if (cacheLevel == 1 && localUserOrGroupName.equals(userName))
 			return checkCachedPermission(userName, objectId, null, privilegeName);
 		
 		test = this.checkOwnership(userName, objectId);
 		if (test)
 			return true;
 
 		if (typeOfAccess.equalsIgnoreCase("MIXED")) {
 			test = this.checkPermissionForUserAndGroup(userName, objectId,
 					privilegeName);
 
 			return test;
 		}
 
 		if (typeOfAccess.equalsIgnoreCase("GROUP_ONLY")) {
 			test = this.checkPermissionForUserGroup(userName, objectId,
 					privilegeName);
 
 			return test;
 		}
 
 		if (typeOfAccess.equalsIgnoreCase("USER_ONLY")) {
 			test = this.checkPermissionForUser(userName, objectId,
 					privilegeName);
 
 			return test;
 		}
 
 		return test;
 	}
 	
 	public boolean checkPermissionForGroup(String groupName, String objectId, String attributeName, String privilegeName) throws CSException
 	{
 		boolean hasAccess = false;
 
 		Session session = null;
 		PreparedStatement preparedStatement = null;
 		ResultSet resultSet = null;
 		Connection connection = null;
 		
 		if (StringUtilities.isBlank(groupName)) {
 			throw new CSException("Group name can't be null!");
 		}
 		if (StringUtilities.isBlank(objectId)) {
 			throw new CSException("Object Id can't be null!");
 		}
 		if (StringUtilities.isBlank(privilegeName)) {
 			throw new CSException("Privilege can't be null!");
 		}
 		
 		// Check if cache is enabled for group
 		if (cacheLevel == 2 && localUserOrGroupName.equals(groupName))
 			return checkCachedPermission(groupName, objectId, attributeName, privilegeName);
 		
 		try {
 
 			session = HibernateSessionFactoryHelper.getAuditSession(sf);
 			connection = session.connection();
 			
 			preparedStatement = Queries.getQueryForCheckPermissionForOnlyGroup(groupName, objectId, attributeName, privilegeName, this.application.getApplicationId().intValue(),connection);
 			resultSet = preparedStatement.executeQuery();
 			if (resultSet.next())
 			{
 				hasAccess = true;
 			}
 			resultSet.close();
 			preparedStatement.close();
 
 		} catch (Exception ex)
 		{
 			log.error(ex);
 			if (log.isDebugEnabled())
 				log.debug("Authorization||"	+ groupName	+ "|checkPermission|Failure|Error Occured in checking permissions with group name " + groupName + " object id: " + objectId + " and privilege name " + privilegeName + "|"	+ ex.getMessage());
 			throw new CSException( "An error occurred while checking permissions\n"	+ ex.getMessage(), ex);
 		} finally {
 			try {
 
 				session.close();
 				resultSet.close();
 				preparedStatement.close();
 				
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log.debug("Authorization|||checkPermission|Failure|Error in Closing Session |" + ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log.debug("Authorization||" + groupName + "|checkPermission|Success|Successful in checking permissions with group id "	+ groupName + " object id: " + objectId	+ " and privilege name " + privilegeName + " and the result is " + hasAccess + "|");
 		
 		return hasAccess;
 	}
 	
 	public boolean checkPermissionForGroup(String groupName, String objectId, String attributeName, String attributeValue, String privilegeName) throws CSException {
 		boolean hasAccess = false;
 
 		Session session = null;
 		PreparedStatement preparedStatement = null;
 		ResultSet resultSet = null;
 		Connection connection = null;
 		
 		if (StringUtilities.isBlank(groupName)) {
 			throw new CSException("Group name can't be null!");
 		}
 		if (StringUtilities.isBlank(objectId)) {
 			throw new CSException("Object Id can't be null!");
 		}
 		if (StringUtilities.isBlank(privilegeName)) {
 			throw new CSException("Privilege can't be null!");
 		}
 				
 		try {
 
 			session = HibernateSessionFactoryHelper.getAuditSession(sf);
 			connection = session.connection();
 			
 			preparedStatement = Queries.getQueryForCheckPermissionForOnlyGroup(groupName, objectId, attributeName, attributeValue, privilegeName, this.application.getApplicationId().intValue(),connection);
 			resultSet = preparedStatement.executeQuery();
 			if (resultSet.next())
 			{
 				hasAccess = true;
 			}
 			resultSet.close();
 			preparedStatement.close();
 
 		} catch (Exception ex)
 		{
 			log.error(ex);
 			if (log.isDebugEnabled())
 				log.debug("Authorization||"	+ groupName	+ "|checkPermissionForGroup|Failure|Error Occured in checking permissions with group name " + groupName + " object id: " + objectId + " and privilege name " + privilegeName + "|"	+ ex.getMessage());
 			throw new CSException( "An error occurred while checking permissions\n"	+ ex.getMessage(), ex);
 		} finally {
 			try {
 
 				session.close();
 				resultSet.close();
 				preparedStatement.close();
 				
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log.debug("Authorization|||checkPermissionForGroup|Failure|Error in Closing Session |" + ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log.debug("Authorization||" + groupName + "|checkPermissionForGroup|Success|Successful in checking permissions with group id "	+ groupName + " object id: " + objectId	+ " and privilege name " + privilegeName + " and the result is " + hasAccess + "|");
 		
 		return hasAccess;
 		
 	}
 
 	public boolean checkPermissionForGroup(String groupName, String objectId, String privilegeName) throws CSException
 	{
 		boolean hasAccess = false;
 
 		Session session = null;
 		PreparedStatement preparedStatement= null;
 		ResultSet resultSet = null;
 		Connection connection = null;
 		
 		if (StringUtilities.isBlank(groupName)) {
 			throw new CSException("Group name can't be null!");
 		}
 		if (StringUtilities.isBlank(objectId)) {
 			throw new CSException("Object Id can't be null!");
 		}
 		if (StringUtilities.isBlank(privilegeName)) {
 			throw new CSException("Privilege can't be null!");
 		}
 		
 		// Check if cache is enabled for group
 		if (cacheLevel == 2 && localUserOrGroupName.equals(groupName))
 			return checkCachedPermission(groupName, objectId, null, privilegeName);
 		
 		try {
 
 			session = HibernateSessionFactoryHelper.getAuditSession(sf);
 			connection = session.connection();
 			
 			preparedStatement= Queries.getQueryForCheckPermissionForOnlyGroup(groupName, objectId, privilegeName, this.application.getApplicationId().intValue(),connection);
 			resultSet = preparedStatement.executeQuery();
 			if (resultSet.next())
 			{
 				hasAccess = true;
 			}
 			resultSet.close();
 			preparedStatement.close();
 
 		} catch (Exception ex)
 		{
 			log.error(ex);
 			if (log.isDebugEnabled())
 				log.debug("Authorization||"	+ groupName	+ "|checkPermission|Failure|Error Occured in checking permissions with group name " + groupName + " object id: " + objectId + " and privilege name " + privilegeName + "|"	+ ex.getMessage());
 			throw new CSException( "An error occurred while checking permissions\n"	+ ex.getMessage(), ex);
 		} finally {
 			try {
 
 				session.close();
 				resultSet.close();
 				preparedStatement.close();
 				
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log.debug("Authorization|||checkPermission|Failure|Error in Closing Session |" + ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log.debug("Authorization||" + groupName + "|checkPermission|Success|Successful in checking permissions with group id "	+ groupName + " object id: " + objectId	+ " and privilege name " + privilegeName + " and the result is " + hasAccess + "|");
 		
 		return hasAccess;
 	}
 
 
 	public List getAccessibleGroups(String objectId, String privilegeName) throws CSException
 	{
 	
 		return getAccessibleGroups(objectId, null, privilegeName);
 	}
 
 	public List getAccessibleGroups(String objectId, String attributeName, String privilegeName) throws CSException
 	{
 
 		Session session = null;
 		PreparedStatement preparedStatement = null;
 		ResultSet resultSet = null;
 		Connection connection = null;
 		List groupIds = new ArrayList();
 		List groups = null;
 		
 		if (StringUtilities.isBlank(objectId)) {
 			throw new CSException("Object Id can't be null!");
 		}
 		if (StringUtilities.isBlank(privilegeName)) {
 			throw new CSException("Privilege can't be null!");
 		}
 		if (attributeName != null && (attributeName.trim()).equals(""))
 			throw new CSException("Attribute can't be null!");
 		try 
 		{
 
 			session = HibernateSessionFactoryHelper.getAuditSession(sf);
 			connection = session.connection();
 			
 			
 			if (null == attributeName)
 				preparedStatement = Queries.getQueryForAccessibleGroups(objectId, privilegeName, this.application.getApplicationId().intValue(),connection);
 			else
 				preparedStatement = Queries.getQueryForAccessibleGroupsWithAttribute(objectId, attributeName, privilegeName, this.application.getApplicationId().intValue(),connection);
 			
 			resultSet = preparedStatement.executeQuery();
 			while (resultSet.next())
 			{
 				if (null == groups)
 					groups = new ArrayList();
 				groupIds.add(resultSet.getString(1));
 				//String groupId = resultSet.getString(1);
 				//Group group = (Group) this.getObjectByPrimaryKey(session, Group.class, new Long(groupId));
 				//groups.add(group);
 			}
 			
 			
 			
 			resultSet.close();
 			preparedStatement.close();
 			
 			for (int i = 0; i < groupIds.size(); i++) {
 				Group group = (Group) this.getObjectByPrimaryKey(session, Group.class, new Long(groupIds.get(i).toString()));
 				groups.add(group);
 			}
 			
 		}
 		catch (Exception e) {
 			throw new CSException("Attribute can't be null!");
 		} finally {
 			try {
 				if(resultSet!=null) resultSet.close();
 				if(connection!=null) connection.close();
 				if(preparedStatement!=null) preparedStatement.close();
 				if(session!=null) session.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||getAccessibleGroups||Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		
 		
 		
 		
 		return groups;
 	}
 
 	private boolean checkPermissionForUser(String userName, String objectId,
 			String privilegeName) throws CSException {
 		boolean test = false;
 		Session s = null;
 		PreparedStatement preparedStatement = null;
 		ResultSet rs = null;
 		Connection connection = null;
 		if (userName == null || objectId == null || privilegeName == null) {
 			return false;
 		}
 		try {
 
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 
 			connection = s.connection();
 
 			
 
 			preparedStatement = Queries.getQueryForCheckPermissionForUser(userName,	objectId, privilegeName, this.application.getApplicationId().intValue(),connection);
 			rs = preparedStatement.executeQuery();
 			if (rs.next()) {
 				test = true;
 			}
 			rs.close();
 			preparedStatement.close();
 
 		} catch (Exception ex) {
 			log.error(ex);
 
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization||"
 								+ userName
 								+ "|checkPermission|Failure|Error Occured in checking permissions with user id "
 								+ userName + " object id: " + objectId
 								+ " and privilege name " + privilegeName + "|"
 								+ ex.getMessage());
 			throw new CSException(
 					"An error occurred while checking permissions\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 				rs.close();
 				preparedStatement.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||checkPermission|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization||"
 							+ userName
 							+ "|checkPermission|Success|Successful in checking permissions with user id "
 							+ userName + " object id: " + objectId
 							+ " and privilege name " + privilegeName
 							+ " and the result is " + test + "|");
 		return test;
 	}
 
 	private boolean checkPermissionForUserAndGroup(String userName,
 			String objectId, String privilegeName) throws CSException {
 		log.debug("Method:checkPermissionForUserAndGroup()");
 		boolean test = false;
 		Session s = null;
 		PreparedStatement preparedStatement = null;
 		ResultSet rs = null;
 		Connection connection = null;
 
 		if (userName == null || objectId == null || privilegeName == null) {
 			return false;
 		}
 
 		try {
 
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 
 			connection = s.connection();
 
 			
 
 			preparedStatement = Queries.getQueryForCheckPermissionForUserAndGroup(userName, objectId, privilegeName, this.application.getApplicationId().intValue(),connection);
 			//log.debug("The User/Group query is: " + sql);
 			rs = preparedStatement.executeQuery();
 			if (rs.next()) {
 				test = true;
 			}
 			rs.close();
 			preparedStatement.close();
 
 		} catch (Exception ex) {
 			log.error(ex);
 
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization||"
 								+ userName
 								+ "|checkPermission|Failure|Error Occured in checking permissions with user id "
 								+ userName + " object id: " + objectId
 								+ " and privilege name " + privilegeName + "|"
 								+ ex.getMessage());
 			throw new CSException(
 					"An error occurred while checking permissions\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 				rs.close();
 				preparedStatement.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||checkPermission|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization||"
 							+ userName
 							+ "|checkPermission|Success|Successful in checking permissions with user id "
 							+ userName + " object id: " + objectId
 							+ " and privilege name " + privilegeName
 							+ " and the result is " + test + "|");
 		return test;
 	}
 
 	private boolean checkPermissionForUserGroup(String userName, String objectId,
 			String privilegeName) throws CSException {
 		boolean test = false;
 		Session s = null;
 		PreparedStatement preparedStatement = null;
 		ResultSet rs = null;
 		Connection connection = null;
 		try {
 
 			if (privilegeName == null) {
 				return false;
 			}
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 
 			connection = s.connection();
 			
 			preparedStatement= Queries.getQueryForCheckPermissionForGroup(userName,
 					objectId, privilegeName, this.application.getApplicationId().intValue(),connection);
 			
 			rs = preparedStatement.executeQuery();
 			if (rs.next()) {
 				test = true;
 			}
 			rs.close();
 			preparedStatement.close();
 			
 
 		} catch (Exception ex) {
 			log.error(ex);
 
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization||"
 								+ userName
 								+ "|checkPermission|Failure|Error Occured in checking permissions with user id "
 								+ userName + " object id: " + objectId
 								+ " and privilege name " + privilegeName + "|"
 								+ ex.getMessage());
 			throw new CSException(
 					"An error occurred while checking permissions\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 				rs.close();
 				preparedStatement.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||checkPermission|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization||"
 							+ userName
 							+ "|checkPermission|Success|Successful in checking permissions with user id "
 							+ userName + " object id: " + objectId
 							+ " and privilege name " + privilegeName
 							+ " and the result is " + test + "|");
 		return test;
 	}
 
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#getApplicationContext()
 	 */
 	public ApplicationContext getApplicationContext() {
 
 		ApplicationContext applicationContext = this.getApplication();
 
 		return applicationContext;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#getObjects(gov.nih.nci.security.dao.SearchCriteria)
 	 */
 	public List getObjects(SearchCriteria searchCriteria) {
 		Session s = null;
 		List result = new ArrayList();
 		try {
 
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			Criteria criteria = s
 					.createCriteria(searchCriteria.getObjectType());
 			Hashtable fieldValues = searchCriteria.getFieldAndValues();
 			Enumeration en = fieldValues.keys();
 			while (en.hasMoreElements()) {
 				String str = (String) en.nextElement();
 				String fieldValue = (String) fieldValues.get(str);
 				String fieldValue_ = StringUtilities.replaceInString(
 						fieldValue.trim(), "*", "%");
 				//int i = ((String) fieldValues.get(str)).indexOf("%");
 				int i = fieldValue_.indexOf("%");
 				if (i != -1) {
 					//criteria.add(Restrictions.like(str, fieldValues.get(str)));
 					criteria.add(Restrictions.like(str, fieldValue_));
 				} else {
 					//criteria.add(Restrictions.eq(str, fieldValues.get(str)));
 					criteria.add(Restrictions.eq(str, fieldValue_));
 				}
 			}
 			if (fieldValues.size() == 0) {
 				criteria.add(Restrictions.eqProperty("1", "1"));
 			}
 			log.debug("Message from debug: ObjectType="
 					+ searchCriteria.getObjectType().getName());
 
 			//boolean t =
 			// searchCriteria.getObjectType().getName().equalsIgnoreCase("gov.nih.nci.security.authorization.domainobjects.User")||searchCriteria.getObjectType().getName().equalsIgnoreCase("gov.nih.nci.security.authorization.domainobjects.Privilege");
 
 			//log.debug("Test:"+t);
 
 			//if(!t){
 			//	criteria.add(Restrictions.eq("application", this.application));
 			//}
 
 			if (!(searchCriteria.getObjectType().getName().equalsIgnoreCase(
 					"gov.nih.nci.security.authorization.domainobjects.User")
 					|| searchCriteria
 							.getObjectType()
 							.getName()
 							.equalsIgnoreCase(
 									"gov.nih.nci.security.authorization.domainobjects.Privilege") || searchCriteria
 					.getObjectType()
 					.getName()
 					.equalsIgnoreCase(
 							"gov.nih.nci.security.authorization.domainobjects.Application"))) {
 				criteria.add(Restrictions.eq("application", this.application));
 			}
 
 			List list =  new ArrayList();
  			list = criteria.list();			
 			Collections.sort(list);
 			result.clear();
 			result.addAll(list);
 
 			
 					
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||getObjects|Failure|Error in Obtaining Search Objects from Database |"
 								+ ex.getMessage());
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||getObjects|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||getObjects|Success|Successful in Searching objects from the database |");
 		return result;
 	}
 
 	private List getObjects(Session s, SearchCriteria searchCriteria) {
 		List result = new ArrayList();
 		try {
 
 			Criteria criteria = s
 					.createCriteria(searchCriteria.getObjectType());
 			Hashtable fieldValues = searchCriteria.getFieldAndValues();
 			Enumeration en = fieldValues.keys();
 			while (en.hasMoreElements()) {
 				String str = (String) en.nextElement();
 				String fieldValue = (String) fieldValues.get(str);
 				String fieldValue_ = StringUtilities.replaceInString(
 						fieldValue, "*", "%");
 				//int i = ((String) fieldValues.get(str)).indexOf("%");
 				int i = fieldValue_.indexOf("%");
 				if (i != -1) {
 					//criteria.add(Restrictions.like(str, fieldValues.get(str)));
 					criteria.add(Restrictions.like(str, fieldValue_));
 				} else {
 					//criteria.add(Restrictions.eq(str, fieldValues.get(str)));
 					criteria.add(Restrictions.eq(str, fieldValue_));
 				}
 			}
 			if (fieldValues.size() == 0) {
 				criteria.add(Restrictions.eqProperty("1", "1"));
 			}
 			log.debug("Message from debug: ObjectType="
 					+ searchCriteria.getObjectType().getName());
 
 			//boolean t =
 			// searchCriteria.getObjectType().getName().equalsIgnoreCase("gov.nih.nci.security.authorization.domainobjects.User")||searchCriteria.getObjectType().getName().equalsIgnoreCase("gov.nih.nci.security.authorization.domainobjects.Privilege");
 
 			//log.debug("Test:"+t);
 
 			//if(!t){
 			//	criteria.add(Restrictions.eq("application", this.application));
 			//}
 
 			if (!(searchCriteria.getObjectType().getName().equalsIgnoreCase(
 					"gov.nih.nci.security.authorization.domainobjects.User")
 					|| searchCriteria
 							.getObjectType()
 							.getName()
 							.equalsIgnoreCase(
 									"gov.nih.nci.security.authorization.domainobjects.Privilege") || searchCriteria
 					.getObjectType()
 					.getName()
 					.equalsIgnoreCase(
 							"gov.nih.nci.security.authorization.domainobjects.Application"))) {
 				criteria.add(Restrictions.eq("application", this.application));
 			}
 
 			result = criteria.list();
 			Collections.sort(result);
 			
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||getObjects|Failure|Error in Obtaining Search Objects from Database |"
 								+ ex.getMessage());
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||getObjects|Success|Successful in Searching objects from the database |");
 		return result;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#getPrincipals(java.lang.String)
 	 */
 	public Principal[] getPrincipals(String userName) {
 		ArrayList al = new ArrayList();
 		Set groups = new HashSet();
 		Principal[] ps = null;
 		if (StringUtilities.isBlank(userName)) {
 			return null;
 		}
 
 		try {
 			User user = this.getUser(userName);
 			if (user == null) {
 				return null;
 			}
 			al.add((Principal) user);
 			groups = this.getGroups(user.getUserId().toString());
 			Iterator it = groups.iterator();
 			while (it.hasNext()) {
 				Group grp = (Group) it.next();
 				al.add((Principal) grp);
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		//TypeuWant[] a = (TypeuWant [] ) arraylist.toArray(new
 		// TypeUWant[arraylist.size()])
 		ps = (Principal[]) al.toArray(new Principal[al.size()]);
 
 		return ps;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#getPrivilege(java.lang.String)
 	 */
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#getProtectionElement(java.lang.String)
 	 */
 	public ProtectionElement getProtectionElement(String objectId,
 			String attribute) throws CSObjectNotFoundException {
 		Session s = null;
 		ProtectionElement pe = null;
 		if (StringUtilities.isBlank(objectId)) {
 			throw new CSObjectNotFoundException(
 					"The protection element can't be searched with null objectId");
 		}
 		try {
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 
 			ProtectionElement search = new ProtectionElement();
 			search.setObjectId(objectId);
 			search.setApplication(application);
 			if (attribute != null && attribute.length() > 0) {
 				search.setAttribute(attribute);
 			}
 			//String query = "FROM
 			// gov.nih.nci.security.authorization.domianobjects.Application";
 
 			Criteria c = s.createCriteria(ProtectionElement.class);
 			c.add(Example.create(search));
 			List list = c.list();
 
 			if (list.isEmpty()) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||getProtectionElement|Failure|Protection Element not found for object id "
 									+ objectId
 									+ " and attribute "
 									+ attribute
 									+ "|");
 				throw new CSObjectNotFoundException(
 						"Protection Element not found with these attributes");
 			}
 			pe = (ProtectionElement) list.get(0);
 
 		} catch (Exception ex) {
 			if (log.isDebugEnabled())
 				log
 						.error(
 								"Authorization|||getProtectionElement|Failure|Error in obtaining Protection Element for object id "
 										+ objectId
 										+ " and attribute "
 										+ attribute + "|", ex);
 			throw new CSObjectNotFoundException(
 					"Protection Element is not found with object id= "
 							+ objectId + " and attributeName= " + attribute);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||getProtectionElement|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||getProtectionElement|Success|Successful in obtaining Protection Element for object id "
 							+ objectId + " and attribute " + attribute + "|");
 		return pe;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#getProtectionElement(java.lang.String)
 	 */
 	public ProtectionElement getProtectionElement(String objectId)
 			throws CSObjectNotFoundException {
 		return getProtectionElement(objectId, null);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#getProtectionGroup(java.lang.String)
 	 */
 	public ProtectionGroup getProtectionGroup(String protectionGroupName)
 			throws CSObjectNotFoundException {
 		Session s = null;
 		ProtectionGroup pgrp = null;
 		if (StringUtilities.isBlank(protectionGroupName)) {
 			throw new CSObjectNotFoundException(
 					"The protection group can't searched with null name");
 		}
 		try {
 			ProtectionGroup search = new ProtectionGroup();
 			search.setProtectionGroupName(protectionGroupName);
 			search.setApplication(application);
 			//String query = "FROM
 			// gov.nih.nci.security.authorization.domianobjects.Application";
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			List list = s.createCriteria(ProtectionGroup.class).add(
 					Example.create(search)).list();
 
 			if (list.size() == 0) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||getProtectionGroup|Failure|Protection Group not found for name "
 									+ protectionGroupName + "|");
 				throw new CSObjectNotFoundException(
 						"Protection Group not found");
 			}
 			pgrp = (ProtectionGroup) list.get(0);
 
 		} catch (Exception ex) {
 			if (log.isDebugEnabled()) {
 				log
 						.debug("Authorization|||getProtectionGroup|Failure|Protection Group not found for name "
 								+ protectionGroupName + "|" + ex.getMessage());
 			}
 			throw new CSObjectNotFoundException(
 					"Protection Group not found for name "
 							+ protectionGroupName);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||getProtectionGroup|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||getProtectionGroup|Success|Protection Group found for name "
 							+ protectionGroupName + "|");
 		return pgrp;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#getRole(java.lang.String)
 	 */
 	public Role getRole(String roleName) throws CSObjectNotFoundException {
 		Session s = null;
 		Role role = null;
 		try {
 			Role search = new Role();
 			search.setName(roleName);
 			search.setApplication(application);
 			//String query = "FROM
 			// gov.nih.nci.security.authorization.domianobjects.Application";
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			List list = s.createCriteria(Role.class)
 					.add(Example.create(search)).list();
 
 			if (list.size() == 0) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||getRole|Failure|Role not found for name "
 									+ roleName + "|");
 				throw new CSObjectNotFoundException("Role not found");
 			}
 			role = (Role) list.get(0);
 
 		} catch (Exception ex) {
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||getRole|Failure|Error in obtaining the Role for name "
 								+ roleName + "|" + ex.getMessage());
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||assignGroupRoleToProtectionGroup|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||getRole|Success|Successful in obtaining the Role for name "
 							+ roleName + "|");
 		return role;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#getUser(java.lang.String)
 	 */
 	public User getUser(String loginName) {
 		Session s = null;
 		User user = null;
 		try {
 			User search = new User();
 			search.setLoginName(loginName);
 
 			//String query = "FROM
 			// gov.nih.nci.security.authorization.domianobjects.Application";
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			List list = s.createCriteria(User.class)
 					.add(Example.create(search)).list();
 			//p = (Privilege)s.load(Privilege.class,new Long(privilegeId));
 
 			if (list.size() != 0) {
 				user = (User) list.get(0);
 			}
 			
 			try {
 				user = (User)performEncrytionDecryption(user, false);
 			} catch (EncryptionException e) {
 				throw new CSObjectNotFoundException(e);
 			}
 			
 			
 			
 		
 
 		} catch (Exception ex) {
 
 			if (log.isDebugEnabled())
 				log.error(
 						"Authorization|||getUser|Failure|Error Occured in Getting User for Name "
 								+ loginName + "|", ex);
 
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||getUser|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||getUser|Success|Success in Getting User for Name "
 							+ loginName + "|");
 		return user;
 	}
 	
 	
 	public Set getUsers(String groupId) throws CSObjectNotFoundException {
 		//todo
 		Session s = null;
 		Set users = new HashSet();
 		try {
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 
 			Group group = (Group) this.getObjectByPrimaryKey(s, Group.class,
 					new Long(groupId));
 			users = group.getUsers();
 			
 			List list = new ArrayList();
 			Iterator toSortIterator = users.iterator();
 			while(toSortIterator.hasNext()){
 				User user = (User) toSortIterator.next();
 				try {
 					user = (User)performEncrytionDecryption(user, false);
 				} catch (EncryptionException e) {
 					throw new CSObjectNotFoundException(e);
 				}
 				list.add(user); 
 				
 			}
 			Collections.sort(list);
 			users.clear();
 			users.addAll(list);
 			
 			log.debug("The result size:" + users.size());
 
 		} catch (Exception ex) {
 			log.error(ex);
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||getUsers|Failure|Error in obtaining Users for Group Id "
 								+ groupId + "|" + ex.getMessage());
 			throw new CSObjectNotFoundException(
 					"An error occurred while obtaining Associated Users for the Group\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||getUsers|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||getUsers|Success|Successful in obtaining Users for Group Id "
 							+ groupId + "|");
 		return users;
 
 	}
 
 	
 
 	private Group getGroup(String groupName) {
 		Session s = null;
 		Group group = null;
 		try {
 			Group search = new Group();
 			search.setGroupName(groupName);
 			search.setApplication( getApplication() );
 			//String query = "FROM
 			// gov.nih.nci.security.authorization.domianobjects.Application";
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			List list = s.createCriteria(Group.class).add(
 					Example.create(search)).list();
 			//p = (Privilege)s.load(Privilege.class,new Long(privilegeId));
 
 			if (list.size() != 0) {
 				group = (Group) list.get(0);
 			}
 
 		} catch (Exception ex) {
 			if (log.isDebugEnabled())
 				log.error("Authorization|||getGroup in Getting Group for Name "
 						+ groupName + "|", ex);
 
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 
 			}
 		}
 
 		return group;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#removeGroupFromProtectionGroup(java.lang.String,
 	 *      java.lang.String)
 	 */
 	public void removeGroupFromProtectionGroup(String protectionGroupId,
 			String groupId) throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 		Connection connection = null;
 
 		try {
 
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			t = s.beginTransaction();
 			connection = s.connection();
 			String sql = "delete from csm_user_group_role_pg where protection_group_id=? and group_id=?";
 			PreparedStatement pstmt = connection.prepareStatement(sql);
 			Long pg_id = new Long(protectionGroupId);
 			Long g_id = new Long(groupId);
 			pstmt.setLong(1, pg_id.longValue());
 			pstmt.setLong(2, g_id.longValue());
 
 			int i = pstmt.executeUpdate();
 
 			t.commit();
 			s.flush();
 			auditLog.info("Deassigning Roles and Protection Group Assignment from Group");
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||removeGroupFromProtectionGroup|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||assignGroupRoleToProtectionGroup|Failure|Error Occured in deassigning Group "
 								+ groupId
 								+ " and Protection Group"
 								+ protectionGroupId + "|" + ex.getMessage());
 			throw new CSTransactionException(
 					"An error occured in deassigning Group and Protection Group\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 				
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||removeGroupFromProtectionGroup|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||assignGroupRoleToProtectionGroup|Success|Success in deassigning Group "
 							+ groupId
 							+ " and Protection Group"
 							+ protectionGroupId + "|");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#removeGroupRoleFromProtectionGroup(java.lang.String,
 	 *      java.lang.String, java.lang.String[])
 	 */
 	public void removeGroupRoleFromProtectionGroup(String protectionGroupId,
 			String groupId, String[] rolesId) throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 
 		try {
 
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			
 			ProtectionGroup pgroup = (ProtectionGroup) this
 			.getObjectByPrimaryKey(s, ProtectionGroup.class, new Long(
 					protectionGroupId));
 			
 			Group group = (Group) this.getObjectByPrimaryKey(s, Group.class,
 					new Long(groupId));
 			
 			
 			
 			ArrayList roles = new ArrayList();
 			for (int i = 0; i < rolesId.length; i++) {
 				Role role = (Role) this.getObjectByPrimaryKey(s, Role.class,
 						new Long(rolesId[i]));
 				roles.add(role);
 			}
 			
 			Criteria criteria = s.createCriteria(UserGroupRoleProtectionGroup.class);
 			criteria.add(Restrictions.eq("protectionGroup", pgroup));
 			criteria.add(Restrictions.eq("group", group));
 	
 			List list = criteria.list();
 			t = s.beginTransaction();
 			for(int k=0;k<list.size();k++){
 				UserGroupRoleProtectionGroup ugrpg = (UserGroupRoleProtectionGroup)list.get(k);
 				Role r = ugrpg.getRole();
 				if(roles.contains(r)){
 					s.delete(ugrpg);	
 				}
 			}
 			
 			t.commit();
 			s.flush();
 			auditLog.info("Deassigning Roles From Group " + group.getGroupName() + " for Protection Group " + pgroup.getProtectionGroupName());
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||removeGroupRoleFromProtectionGroup|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||removeGroupRoleFromProtectionGroup|Failure|Error Occured in assigning Roles "
 								+ StringUtilities.stringArrayToString(rolesId)
 								+ " to Group "
 								+ groupId
 								+ " and Protection Group"
 								+ protectionGroupId
 								+ "|" + ex.getMessage());
 			throw new CSTransactionException(
 					"An error occured in assigning Roles and Protection Group to a Group\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||assignGroupRoleToProtectionGroup|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||removeGroupRoleFromProtectionGroup|Success|Successful in assigning Roles "
 							+ StringUtilities.stringArrayToString(rolesId)
 							+ " to Group "
 							+ groupId
 							+ " and Protection Group"
 							+ protectionGroupId + "|");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#removeRole(java.lang.String)
 	 */
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#removeUserFromGroup(java.lang.String,
 	 *      java.lang.String)
 	 */
 	public void removeUserFromGroup(String groupId, String userId)
 			throws CSTransactionException {
 
 		Session s = null;
 		Transaction t = null;
 
 		try {
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			
 			User user = (User) this.getObjectByPrimaryKey(s, User.class,
 					new Long(userId));
 			Group group = (Group) this.getObjectByPrimaryKey(s, Group.class,
 					new Long(groupId));
 			
 			
 			
 			Set groups = user.getGroups();
 			if (groups.contains(group)) {
 				groups.remove(group);
 				user.setGroups(groups);
 				
 				try {
 					user = (User)performEncrytionDecryption(user, true);
 				} catch (EncryptionException e) {
 					throw new CSObjectNotFoundException(e);
 				}
 			
 				
 				t = s.beginTransaction();
 				s.update(user);
 				
 				t.commit();
 				s.flush();
 			}else{
 				//t.rollback();
 			}
 
 			//t.commit();
 			//s.flush();
 			auditLog.info("Deassigning User " + user.getLoginName() + " from Group " + group.getGroupName());
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||removeUserFromGroup|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||removeUserFromGroup|Failure|Error Occured in deassigning User "
 								+ userId
 								+ " from Group "
 								+ groupId
 								+ "|"
 								+ ex.getMessage());
 			throw new CSTransactionException(
 					"An error occured in deassigning User from a Group\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||removeUserFromGroup|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||removeUserFromGroup|Success|Successful in deassigning User "
 							+ userId + " from Group " + groupId + "|");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#removeUserFromProtectionGroup(java.lang.String,
 	 *      java.lang.String)
 	 */
 	public void removeUserFromProtectionGroup(String protectionGroupId,
 			String userId) throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 		Connection connection = null;
 
 		try {
 
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			t = s.beginTransaction();
 			connection = s.connection();
 			String sql = "delete from csm_user_group_role_pg where protection_group_id=? and user_id=?";
 			PreparedStatement pstmt = connection.prepareStatement(sql);
 			Long pg_id = new Long(protectionGroupId);
 			Long u_id = new Long(userId);
 			pstmt.setLong(1, pg_id.longValue());
 			pstmt.setLong(2, u_id.longValue());
 
 			int i = pstmt.executeUpdate();
 			pstmt.close();
 			t.commit();
 			s.flush();
 			auditLog.info("Deassigning Roles and Protection Group Assignment from User");
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||removeUserFromProtectionGroup|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||removeUserFromProtectionGroup|Failure|Error Occured in deassigning User "
 								+ userId
 								+ " from Protection Group "
 								+ protectionGroupId + "|" + ex.getMessage());
 			throw new CSTransactionException(
 					"An error occured in deassigning User from Protection Group\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				
 				s.close();
 
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||removeUserFromProtectionGroup|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||removeUserFromProtectionGroup|Success|Successful in deassigning User "
 							+ userId
 							+ " from Protection Group "
 							+ protectionGroupId + "|");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#removeUserRoleFromProtectionGroup(java.lang.String,
 	 *      java.lang.String, java.lang.String[])
 	 */
 	public void removeUserRoleFromProtectionGroup(String protectionGroupId,
 			String userId, String[] rolesId) throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 
 		try {
 
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			
 			ProtectionGroup pgroup = (ProtectionGroup) this
 			.getObjectByPrimaryKey(s, ProtectionGroup.class, new Long(
 					protectionGroupId));
 
 			User user = (User) this.getObjectByPrimaryKey(s, User.class,
 			new Long(userId));
 			//encrypt password for User.
 			this.performEncrytionDecryption(user,true);
 			
 			ArrayList roles = new ArrayList();
 			for (int i = 0; i < rolesId.length; i++) {
 				Role role = (Role) this.getObjectByPrimaryKey(s, Role.class,
 						new Long(rolesId[i]));
 				roles.add(role);
 			}
 			
 			
 			Criteria criteria = s.createCriteria(UserGroupRoleProtectionGroup.class);
 			criteria.add(Restrictions.eq("protectionGroup", pgroup));
 			criteria.add(Restrictions.eq("user", user));
 			List list = criteria.list();
 			
 			
 			t = s.beginTransaction();
 			
 			for(int k=0;k<list.size();k++){
 				UserGroupRoleProtectionGroup ugrpg = (UserGroupRoleProtectionGroup)list.get(k);
 				Role r = ugrpg.getRole();
 				  if(roles.contains(r)){
 				  	s.delete(ugrpg);
 				  }
 			}
 			
 			
 			t.commit();
 			s.flush();
 
 			auditLog.info("Deassigning Roles From User " + user.getLoginName() + " for Protection Group " + pgroup.getProtectionGroupName());
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||removeUserRoleFromProtectionGroup|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||removeUserRoleFromProtectionGroup|Failure|Error Occured in deassigning Roles "
 								+ StringUtilities.stringArrayToString(rolesId)
 								+ " and Protection Group "
 								+ protectionGroupId
 								+ " for user " + userId + "|" + ex.getMessage());
 			throw new CSTransactionException(
 					"An error occured in deassigning Roles and Protection Group for the User\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||removeUserRoleFromProtectionGroup|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||removeUserRoleFromProtectionGroup|Success|Successful in deassigning Roles "
 							+ StringUtilities.stringArrayToString(rolesId)
 							+ " and Protection Group "
 							+ protectionGroupId
 							+ " for user " + userId + "|");
 	}
 
 	/**
 	 *  
 	 */
 	private User getLightWeightUser(String loginName) {
 		Session s = null;
 		User user = null;
 		try {
 			User search = new User();
 			search.setLoginName(loginName);
 
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			List list = s.createCriteria(User.class)
 					.add(Example.create(search)).list();
 
 			if (list.size() != 0) {
 				user = (User) list.get(0);
 			}
 
 		} catch (Exception ex) {
 			log.fatal("Unable to find Group\n" + ex.getMessage(), ex);
 
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||assignGroupRoleToProtectionGroup|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		return user;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#setOwnerForProtectionElement(java.lang.String,
 	 *      java.lang.String, java.lang.String)
 	 */
 	public void setOwnerForProtectionElement(String loginName, String protectionElementObjectId, String protectionElementAttributeName)	throws CSTransactionException {
 
 		Session s = null;
 		Transaction t = null;
 		if (StringUtilities.isBlank(loginName)) {
 			throw new CSTransactionException("Login Name can't be null");
 		}
 		if (StringUtilities.isBlank(protectionElementObjectId)) {
 			throw new CSTransactionException("Object Id can't be null");
 		}
 		try {
 
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			
 			User user = getLightWeightUser(loginName);
 			
 			
 
 			
 			if (user == null) {
 				throw new CSTransactionException("No user found for this login name");
 			}
 			ProtectionElement pe = new ProtectionElement();
 			pe.setObjectId(protectionElementObjectId);
 			pe.setApplication(application);
 			if (protectionElementAttributeName != null && protectionElementAttributeName.length() > 0) {
 				pe.setAttribute(protectionElementAttributeName);
 			}			
 			SearchCriteria sc = new ProtectionElementSearchCriteria(pe);
 			List l = this.getObjects(s,sc);
 
 			if (l.size() == 0) {
 				throw new CSTransactionException("No Protection Element found for the given object id and attribute");
 			}
 			
 			ProtectionElement protectionElement = (ProtectionElement) l.get(0);
 
 			Set ownerList = protectionElement.getOwners();
 			if (ownerList == null || ownerList.size() == 0)
 			{
 				ownerList = new HashSet();
 				ownerList.add(user);
 			}
 			else
 			{
 				if (!ownerList.contains(user))
 				{
 					ownerList.add(user);
 				}
 			}
 			protectionElement.setOwners(ownerList);
 			t = s.beginTransaction();
 			s.save(protectionElement);
 			t.commit();
 			s.flush();
 			auditLog.info("Assinging User " + loginName + " as Owner for Protection Element with Object Id " + protectionElement.getObjectId() + " and Attribute " + protectionElement.getAttribute());
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||setOwnerForProtectionElement|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||setOwnerForProtectionElement|Failure|Error Setting owner for Protection Element object Name"
 								+ protectionElementObjectId
 								+ " and Attribute Id "
 								+ protectionElementAttributeName
 								+ " for user "
 								+ loginName + "|" + ex.getMessage());
 			throw new CSTransactionException(
 					"An error occured in setting owner for the Protection Element\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||setOwnerForProtectionElement|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||setOwnerForProtectionElement|Success|Success in Setting owner for Protection Element object Name"
 							+ protectionElementObjectId
 							+ " and Attribute Id "
 							+ protectionElementAttributeName
 							+ " for user "
 							+ loginName + "|");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#setOwnerForProtectionElement(java.lang.String,
 	 *      java.lang.String)
 	 */
 	public void setOwnerForProtectionElement(String protectionElementObjectId,
 			String[] userNames) throws CSTransactionException {
 
 		Session s = null;
 		Transaction t = null;
 		if (StringUtilities.isBlank(protectionElementObjectId)) {
 			throw new CSTransactionException("object Id can't be null!");
 		}
 		try {
 			
 
 			Set users = new HashSet();
 
 			for (int i = 0; i < userNames.length; i++) {
 				User user = this.getUser(userNames[i]);
 				if (user != null) {
 					users.add(user);
 				}
 			}
 			ProtectionElement pe = new ProtectionElement();
 			pe.setObjectId(protectionElementObjectId);
 			pe.setApplication(application);
 			SearchCriteria sc = new ProtectionElementSearchCriteria(pe);
 			List l = this.getObjects(sc);
 
 			ProtectionElement protectionElement = (ProtectionElement) l.get(0);
 
 			protectionElement.setOwners(users);
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			t = s.beginTransaction();
 			s.update(protectionElement);
 			t.commit();
 			s.flush();
 			auditLog.info("Assigning Users as Owner for Protection Element with Object Id " + protectionElement.getObjectId() + " and Attribute " + protectionElement.getAttribute());		
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||setOwnerForProtectionElement|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||setOwnerForProtectionElement|Failure|Error Setting owner for Protection Element object Name"
 								+ protectionElementObjectId
 								+ " for users "
 								+ StringUtilities
 										.stringArrayToString(userNames)
 								+ "|"
 								+ ex.getMessage());
 			throw new CSTransactionException(
 					"An error occured in setting multiple owners for the Protection Element\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||setOwnerForProtectionElement|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||setOwnerForProtectionElement|Success|Successful in Setting owner for Protection Element object Name"
 							+ protectionElementObjectId
 							+ " for users "
 							+ StringUtilities.stringArrayToString(userNames)
 							+ "|");
 	}
 
 	public Set getPrivileges(String roleId) throws CSObjectNotFoundException {
 		Session s = null;
 		Set result = new HashSet();
 		try {
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			Role role = (Role) this.getObjectByPrimaryKey(s, Role.class,
 					new Long(roleId));
 			result = role.getPrivileges();
 			
 			List list = new ArrayList();
 			Iterator toSortIterator = result.iterator();
 			while(toSortIterator.hasNext()){ list.add(toSortIterator.next()); }
 			Collections.sort(list);
 			result.clear();
 			result.addAll(list);
 			
 			log.debug("The result size is: " + result.size());
 		} catch (Exception ex) {
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||getPrivileges|Failure|Error obtaining Associated Privileges for Role id "
 								+ roleId + "|" + ex.getMessage());
 			throw new CSObjectNotFoundException(
 					"An error occured in obtaining associated Privileges for the given Role\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||assignGroupRoleToProtectionGroup|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||getPrivileges|Success|Successful in obtaining Associated Privileges for Role id "
 							+ roleId + "|");
 		return result;
 	}
 
 	/**
 	 * public void createUser(User user) throws CSTransactionException { Session
 	 * s = null; Transaction t = null; try { s = HibernateSessionFactoryHelper.getAuditSession(sf); t =
 	 * s.beginTransaction(); user.setUpdateDate(new Date()); s.save(user);
 	 * t.commit(); log.debug("User ID is: " + user.getUserId()); } catch
 	 * (Exception ex) { log.error(ex); try { t.rollback(); } catch (Exception
 	 * ex3) { } throw new CSTransactionException("Could not create the user",
 	 * ex); } finally { try { s.close(); } catch { } } }
 	 */
 
 	public void assignProtectionElements(String protectionGroupId,
 			String[] protectionElementIds) throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 		Set pes = new HashSet();
 		try {
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 		
 
 			ProtectionGroup protectionGroup = (ProtectionGroup) this
 					.getObjectByPrimaryKey(s, ProtectionGroup.class, new Long(
 							protectionGroupId));
 			
 			
 			
 			for (int i = 0; i < protectionElementIds.length; i++) {
 
 				ProtectionElement protectionElement = (ProtectionElement) this
 						.getObjectByPrimaryKey(ProtectionElement.class,
 								protectionElementIds[i]);
 
 				pes.add(protectionElement);
 
 			}
 			protectionGroup.setProtectionElements(pes);
 			t = s.beginTransaction();
 			s.update(protectionGroup);
 			t.commit();
 			s.flush();
 			auditLog.info("Assinging Protection Elements to Protection Group " + protectionGroup.getProtectionGroupName());
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||assignProtectionElements|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||assignProtectionElements|Failure|Error Occured in assigning Protection Elements "
 								+ StringUtilities
 										.stringArrayToString(protectionElementIds)
 								+ " to Protection Group"
 								+ protectionGroupId
 								+ "|" + ex.getMessage());
 			throw new CSTransactionException(
 					"An error occured in assigning Protection Elements to the Protection Group\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||assignProtectionElements|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||assignProtectionElements|Success|Successful in assigning Protection Elements "
 							+ StringUtilities
 									.stringArrayToString(protectionElementIds)
 							+ " to Protection Group" + protectionGroupId + "|");
 	}
 	
 	public void addProtectionElements(String protectionGroupId,
 			String[] protectionElementIds) throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 		
 		try {
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 		
 			ProtectionGroup protectionGroup = (ProtectionGroup) s.load(ProtectionGroup.class,new Long(protectionGroupId));
 			if(protectionGroup==null) throw new CSTransactionException("Authorization|||addProtectionElements|| Unable to retrieve ProtectionGroup with Id :"+protectionGroupId);
 			
 			
 			Set protectionElementSet = protectionGroup.getProtectionElements();
 			if(protectionElementSet==null) protectionElementSet = new HashSet();
 			
 			for (int i = 0; i < protectionElementIds.length; i++) {
 				boolean assigned= false;
 				Iterator iterator = protectionElementSet.iterator();
 				while(iterator.hasNext()){
 					ProtectionElement protectionElement =(ProtectionElement)iterator.next();
 					if(protectionElementIds[i].equalsIgnoreCase(protectionElement.getProtectionElementId().toString()))
 						assigned=true;
 				}
 				if(!assigned){
 					ProtectionElement protectionElement= (ProtectionElement) s.load(ProtectionElement.class, Long.parseLong(protectionElementIds[i]));
 					if(protectionElement!=null)
 						protectionElementSet.add(protectionElement);
 				}
 			}
 			
 			t = s.beginTransaction();
 			s.update(protectionGroup);
 			t.commit();
 			s.flush();
 			auditLog.info("Adding Protection Elements to Protection Group " + protectionGroup.getProtectionGroupName());
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||addProtectionElements|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||addProtectionElements|Failure|Error Occured in addding Protection Elements "
 								+ StringUtilities
 										.stringArrayToString(protectionElementIds)
 								+ " to Protection Group"
 								+ protectionGroupId
 								+ "|" + ex.getMessage());
 			throw new CSTransactionException(
 					"An error occured in adding Protection Elements to the Protection Group\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||addProtectionElements|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||addProtectionElements|Success|Successful in adding Protection Elements "
 							+ StringUtilities
 									.stringArrayToString(protectionElementIds)
 							+ " to Protection Group" + protectionGroupId + "|");
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#deAssignProtectionElements(java.lang.String[],
 	 *      java.lang.String)
 	 */
 	/**
 	 * @param protectionGroupName
 	 * @param protectionElementObjectId
 	 *  
 	 */
 	public void deAssignProtectionElements(String protectionGroupName,
 			String protectionElementObjectId)
 			throws CSTransactionException {
 
  		Session s = null;
 		Transaction t = null;
 		try {
 
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			t = s.beginTransaction();
 
 			if (StringUtilities.isBlank(protectionGroupName)) {
 				throw new CSTransactionException(
 						"The protectionGroupName can't be null");
 			}
 			if (StringUtilities.isBlank(protectionElementObjectId)) {
 				throw new CSTransactionException(
 						"The protectionElementObjectId can't be null");
 			}
 
 			ProtectionGroup protectionGroup = getProtectionGroup(protectionGroupName);
 			ProtectionElement protectionElement = getProtectionElement(
 					protectionElementObjectId, null);
 			
 			ProtectionGroup newPG = new ProtectionGroup();
 			newPG.setProtectionGroupId(protectionGroup.getProtectionGroupId());
 			ProtectionElement newPE = new ProtectionElement();
 			newPE.setProtectionElementId(protectionElement.getProtectionElementId());
 
 			Criteria criteria = s
 					.createCriteria(ProtectionGroupProtectionElement.class);
 			criteria.add(Restrictions.eq("protectionGroup", newPG));
 			criteria.add(Restrictions.eq("protectionElement", newPE));
 
 			List list = criteria.list();
 
 			if (list.size() == 0) {
 				throw new CSTransactionException(
 				"Protection Element association to Protection Group does not exist!");
 				
 			} else {
 				ProtectionGroupProtectionElement pgpe = (ProtectionGroupProtectionElement) list.iterator().next();
 				s.delete(pgpe);
 			}
 
 			t.commit();
 			s.flush();
 			auditLog.info("Deassigning Protection Element with Object Id " + protectionElementObjectId + " from Protection Group" + protectionGroupName);
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||deAssignProtectionElements|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||deAssignProtectionElements|Failure|Error Occured in deassigning Protection Element with Object Id "
 								+ protectionElementObjectId
 								+ " from protection group name: "
 								+ protectionGroupName + "|" + ex.getMessage());
 			throw new CSTransactionException(
 					"An error occurred in deassigning Protection Element from Protection Group\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||deAssignProtectionElements|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||deAssignProtectionElements|Success|Successful in deassigning Protection Element with Object Id "
 							+ protectionElementObjectId
 							+ " from protection group name: "
 							+ protectionGroupName + "|");
 	}
 
 
 
 	public void removeProtectionElementsFromProtectionGroup(
 			String protectionGroupId, String[] protectionElementIds)
 			throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 		
 		Set pgpes = new HashSet();
 		try {
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			
 
 			ProtectionGroup protectionGroup = (ProtectionGroup) this
 					.getObjectByPrimaryKey(s, ProtectionGroup.class, new Long(
 							protectionGroupId));
 
 			for (int i = 0; i < protectionElementIds.length; i++) {
 				
 				ProtectionElement protectionElement = (ProtectionElement) this
 						.getObjectByPrimaryKey(s, ProtectionElement.class,
 								new Long(protectionElementIds[i]));
 
 			
 				
 				Criteria criteria = s.createCriteria(ProtectionGroupProtectionElement.class);
 				criteria.add(Restrictions.eq("protectionGroup", protectionGroup));
 				criteria.add(Restrictions.eq("protectionElement", protectionElement));
 				List list = criteria.list();
 				if (list !=null && !list.isEmpty()) {
 					Iterator it = list.iterator();
 					while(it.hasNext()) pgpes.add(it.next()); 
 					
 				} else {
 					throw new CSTransactionException("This association does not exist!");
 				}
 
 			}
 			t = s.beginTransaction();
 			Iterator iter = pgpes.iterator();
 			while(iter.hasNext()){
 				this.removeObject((ProtectionGroupProtectionElement)iter.next());
 			}
 
 			t.commit();
 			s.flush();
 			auditLog.info("Deassinging Protection Elements from Protection Group " + protectionGroup.getProtectionGroupName());
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||removeProtectionElementsFromProtectionGroup|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			log
 					.debug("Authorization|||removeProtectionElementsFromProtectionGroup|Failure|Error Occured in deassigning Protection Elements "
 							+ StringUtilities
 									.stringArrayToString(protectionElementIds)
 							+ " to Protection Group"
 							+ protectionGroupId
 							+ "|"
 							+ ex.getMessage());
 			throw new CSTransactionException(
 					"An error occured in deassigning Protection Elements from Protection Group\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||removeProtectionElementsFromProtectionGroup|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		log
 				.debug("Authorization|||removeProtectionElementsFromProtectionGroup|Success|Success in deassigning Protection Elements "
 						+ StringUtilities
 								.stringArrayToString(protectionElementIds)
 						+ " to Protection Group" + protectionGroupId + "|");
 	}
 
 	private Object getObjectByPrimaryKey(Session s, Class objectType,
 			Long primaryKey) throws HibernateException,
 			CSObjectNotFoundException {
 
 		if (primaryKey == null) {
 			throw new CSObjectNotFoundException("The primary key can't be null");
 		}
 		Object obj = s.load(objectType, primaryKey);
 		
 		try {
 			obj = performEncrytionDecryption(obj, false);
 		} catch (EncryptionException e) {
 			throw new CSObjectNotFoundException(e);
 		}
 		
 
 		if (obj == null) {
 			log
 					.debug("Authorization|||getObjectByPrimaryKey|Failure|Not found object of type "
 							+ objectType.getName() + "|");
 			throw new CSObjectNotFoundException(objectType.getName()
 					+ " not found");
 		}
 		log
 				.debug("Authorization|||getObjectByPrimaryKey|Success|Success in retrieving object of type "
 						+ objectType.getName() + "|");
 		return obj;
 	}
 
 	public Object getObjectByPrimaryKey(Class objectType, String primaryKey)
 			throws CSObjectNotFoundException {
 		Object oj = null;
 
 		Session s = null;
 		if (StringUtilities.isBlank(primaryKey)) {
 			throw new CSObjectNotFoundException("The primary key can't be null");
 		}
 		try {
 
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			oj = getObjectByPrimaryKey(s, objectType, new Long(primaryKey));
 			
 			
 
 		} catch (Exception ex) {
 			log
 					.debug("Authorization|||getObjectByPrimaryKey|Failure|Error in retrieving object of type "
 							+ objectType.getName() + "|" + ex.getMessage());
 			throw new CSObjectNotFoundException(objectType.getName()
 					+ " not found\n" + ex.getMessage(), ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||getObjectByPrimaryKey|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		log
 				.debug("Authorization|||getObjectByPrimaryKey|Success|Success in retrieving object of type "
 						+ objectType.getName() + "|");
 		return oj;
 	}
 
 	public void removeObject(Object oj) throws CSTransactionException {
 
 		Session s = null;
 		Transaction t = null;
 		try {
 
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			t = s.beginTransaction();
 
 			s.delete(oj);
 
 			t.commit();
 			s.flush();
 			auditLog.info("Deleting the " + oj.getClass().getName().substring(oj.getClass().getName().lastIndexOf(".")+1) + " Object ");
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||removeObject|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||removeObject|Failure|Error in removing object of type "
 								+ oj.getClass().getName()
 								+ "|"
 								+ ex.getMessage());
 			throw new CSTransactionException(
 					"An error occured in removing object of type "
 							+ oj.getClass().getName() + "\n" + ex.getMessage(),
 					ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||removeObject|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||removeObject|Success|Success in removing object of type "
 							+ oj.getClass().getName() + "|");
 	}
 
 	private Application getApplicationByName(String contextName)
 			throws CSObjectNotFoundException {
 		Session s = null;
 		Application app = null;
 		try {
 			Application search = new Application();
 			search.setApplicationName(contextName);
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 		
 			Query q = s.createQuery("from Application as app where app.applicationName='"+contextName+"'");
 			List list = q.list();
 			
 			if (list.size() == 0) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|"
 									+ contextName
 									+ "||getApplicationByName|Failure|No Application Found for the Context Name "
 									+ contextName + "|");
 				throw new CSObjectNotFoundException(
 						"No Application Found for the given Context Name");
 			}
 			app = (Application) list.get(0);
 			
 			//decrypt
 			try {
 				app = (Application) performEncrytionDecryption(app, false);
 			} catch (EncryptionException e) {
 				throw new CSObjectNotFoundException(e);
 			}
 			
 			log.debug("Found the Application");
 
 		} catch (GenericJDBCException eex) {
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|"
 								+ contextName
 								+ "||getApplicationByName|Failure|Error in obtaining database connection. Invalid database login credentials in the application hibernate configuration file");
 			throw new CSObjectNotFoundException(" Invalid database login credentials in the application hibernate configuration file.", eex);
 			
 		} catch (Exception ex) {
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|"
 								+ contextName
 								+ "||getApplicationByName|Failure|Error in obtaining application "
 								+ contextName + "|" + ex.getMessage());
 			throw new CSObjectNotFoundException(
 					"An error occured in retrieving Application for the given Context Name\n"
 							+ ex.getMessage(), ex);
 
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||getApplicationByName|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|"
 							+ contextName
 							+ "||getApplicationByName|Success|Application Found for the Context Name "
 							+ contextName + "|");
 		return app;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.AuthorizationManager#initialize(java.lang.String)
 	 */
 	public void initialize(String applicationContextName) {
 		//do nothing...
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gov.nih.nci.security.UserProvisioningManager#getProtectionGroupRoleContext()
 	 *      We might not implement this method
 	 */
 	public Set getProtectionGroupRoleContextForUser(String userId)
 			throws CSObjectNotFoundException {
 		Set result = new HashSet();
 		Session s = null;
 
 		Connection connection = null;
 		ArrayList pgIds = new ArrayList();
 		try {
 			
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 
 			connection = s.connection();
 
 			StringBuffer stbr = new StringBuffer();
 			stbr.append("SELECT distinct ugrp.protection_group_id "); 
 			stbr.append("FROM csm_user_group_role_pg  ugrp , csm_protection_group pg ");
 			stbr.append("where ugrp.protection_group_id  = pg.protection_group_id and  ");
 			stbr.append("ugrp.user_id = ?");
 			stbr.append(" and pg.application_id = ?");
 			
 			PreparedStatement preparedStatement = connection.prepareStatement(stbr.toString());;
 			int i=1;			
 			preparedStatement.setInt(i++,new Integer(userId).intValue());
 			preparedStatement.setInt(i++,this.application.getApplicationId().intValue());
 			
 			ResultSet rs = preparedStatement.executeQuery();
 			while (rs.next()) {
 				String pg_id = rs.getString(1);
 				pgIds.add(pg_id);
 			}
 			rs.close();
 			preparedStatement.close();
 			
 		} catch (Exception ex) {
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||getProtectionGroupRoleContextForUser|Failure|Error in obtaining the Protection Group - Role Context for the User Id "
 								+ userId + "|" + ex.getMessage());
 			throw new CSObjectNotFoundException(
 					"An error occured in obtaining the Protection Group - Role Context for the User\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||getProtectionGroupRoleContextForUser|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		
 		try{
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			
 			User user = (User) this.getObjectByPrimaryKey(User.class, userId);
 			for (int i = 0; i < pgIds.size(); i++) {
 
 				ProtectionGroup pg = (ProtectionGroup) this
 						.getObjectByPrimaryKey(ProtectionGroup.class, pgIds
 								.get(i).toString());
 				Criteria criteria = s
 						.createCriteria(UserGroupRoleProtectionGroup.class);
 				criteria.add(Restrictions.eq("user", user));
 				criteria.add(Restrictions.eq("protectionGroup", pg));
 				List list = criteria.list();
 
 				Iterator it = list.iterator();
 				Set roles = new HashSet();
 				while (it.hasNext()) {
 					UserGroupRoleProtectionGroup ugrpg = (UserGroupRoleProtectionGroup) it
 							.next();
 					roles.add(ugrpg.getRole());
 				}
 
 				ProtectionGroupRoleContext pgrc = new ProtectionGroupRoleContext();
 				pgrc.setProtectionGroup(pg);
 				pgrc.setRoles(roles);
 				result.add(pgrc);
 			}
 
 		} catch (Exception ex) {
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||getProtectionGroupRoleContextForUser|Failure|Error in obtaining the Protection Group - Role Context for the User Id "
 								+ userId + "|" + ex.getMessage());
 			throw new CSObjectNotFoundException(
 					"An error occured in obtaining the Protection Group - Role Context for the User\n"
 							+ ex.getMessage(), ex);
 		}
 
 		finally {
 			try {
 
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||getProtectionGroupRoleContextForUser|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||getProtectionGroupRoleContextForUser|Success|Successful in obtaining the Protection Group - Role Context for the User Id "
 							+ userId + "|");
 		return result;
 	}
 
 	public Set getProtectionGroupRoleContextForGroup(String groupId)
 			throws CSObjectNotFoundException {
 		Set result = new HashSet();
 		
 		Session s = null;
 		Connection connection = null;
 		ArrayList pgIds = new ArrayList();
 		try {
 			
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 
 			connection = s.connection();
 
 
 			StringBuffer stbr = new StringBuffer();
 			stbr.append("SELECT distinct ugrp.protection_group_id "); 
 			stbr.append("FROM csm_user_group_role_pg  ugrp , csm_group g ");
 			stbr.append("where ugrp.group_id  = g.group_id and  ");
 			stbr.append("ugrp.group_id = ?");
 			stbr.append(" and g.application_id = ?");
 
 			PreparedStatement preparedStatement = connection.prepareStatement(stbr.toString());;
 			int i=1;			
 			preparedStatement.setInt(i++,new Integer(groupId).intValue());
 			preparedStatement.setInt(i++,this.application.getApplicationId().intValue());
 			
 			ResultSet rs = preparedStatement.executeQuery();
 
 			while (rs.next()) {
 				String pg_id = rs.getString(1);
 				pgIds.add(pg_id);
 			}
 			rs.close();
 			preparedStatement.close();
 		} catch (Exception ex) {
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||getProtectionGroupRoleContextForUser|Failure|Error in obtaining the Protection Group - Role Context for the Group Id "
 								+ groupId + "|" + ex.getMessage());
 			throw new CSObjectNotFoundException(
 					"An error occured in obtaining the Protection Group - Role Context for the Group\n"
 							+ ex.getMessage(), ex);
 		}finally {
 			try {
 				
 				s.close();
 				
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||getProtectionGroupRoleContextForUser|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 			
 		try {
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			Group group = (Group) this.getObjectByPrimaryKey(Group.class,
 					groupId);
 			for (int i = 0; i < pgIds.size(); i++) {
 
 				ProtectionGroup pg = (ProtectionGroup) this
 						.getObjectByPrimaryKey(ProtectionGroup.class, pgIds
 								.get(i).toString());
 				Criteria criteria = s
 						.createCriteria(UserGroupRoleProtectionGroup.class);
 				criteria.add(Restrictions.eq("group", group));
 				criteria.add(Restrictions.eq("protectionGroup", pg));
 				List list = criteria.list();
 
 				Iterator it = list.iterator();
 				Set roles = new HashSet();
 				while (it.hasNext()) {
 					UserGroupRoleProtectionGroup ugrpg = (UserGroupRoleProtectionGroup) it
 							.next();
 					roles.add(ugrpg.getRole());
 				}
 
 				ProtectionGroupRoleContext pgrc = new ProtectionGroupRoleContext();
 				pgrc.setProtectionGroup(pg);
 				pgrc.setRoles(roles);
 				result.add(pgrc);
 			}
 
 		} catch (Exception ex) {
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||getProtectionGroupRoleContextForUser|Failure|Error in obtaining the Protection Group - Role Context for the Group Id "
 								+ groupId + "|" + ex.getMessage());
 			throw new CSObjectNotFoundException(
 					"An error occured in obtaining the Protection Group - Role Context for the Group\n"
 							+ ex.getMessage(), ex);
 		}
 
 		finally {
 			try {
 
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||getProtectionGroupRoleContextForUser|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||getProtectionGroupRoleContextForUser|Success|Successful in obtaining the Protection Group - Role Context for the Group Id "
 							+ groupId + "|");
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#getProtectionElementPrivilegeContextForUser(java.lang.String)
 	 */
 	public Set getProtectionElementPrivilegeContextForUser(String userId) throws CSObjectNotFoundException {
 		Set protectionElementPrivilegeContextSet = new HashSet();
 
 		Session s = null;
 		Connection connection = null;
 		PreparedStatement preparedStatement = null;
 		ResultSet rs = null;
 		
 		String currPEId = null;
 		String prevPEId = null;
 
 		String currPrivilegeId = null;
 		Set privileges = null;
 		Privilege privilege = null;
 		
 		List peList = new ArrayList();
 		List privList = new ArrayList();
 		
 		boolean firstTime = true;
 		ProtectionElementPrivilegeContext protectionElementPrivilegeContext = null;
 		
 		try {
 
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			connection = s.connection();
 			
 			preparedStatement= Queries.getQueryforUserPEPrivilegeMap(userId, this.application.getApplicationId().intValue(),connection);
 
 			rs = preparedStatement.executeQuery();
 
 			while(rs.next()){
 				peList.add(rs.getString(1));
 				privList.add(rs.getString(2));
 				
 			}
 			
 			Iterator currPEIdIterator = peList.iterator();
 			Iterator currPrivilegeIdIterator = privList.iterator();
 			
 			while(currPEIdIterator.hasNext()){
 				
 				currPEId = (String)currPEIdIterator.next();
 				currPrivilegeId = (String)currPrivilegeIdIterator.next();
 				
 				if (!currPEId.equals(prevPEId))
 				{
 					protectionElementPrivilegeContext = new ProtectionElementPrivilegeContext();
 					protectionElementPrivilegeContextSet.add(protectionElementPrivilegeContext);
 					ProtectionElement protectionElement = (ProtectionElement) this.getObjectByPrimaryKey(s, ProtectionElement.class, new Long(currPEId));
 					protectionElementPrivilegeContext.setProtectionElement(protectionElement);
 					privileges = new HashSet();
 					protectionElementPrivilegeContext.setPrivileges(privileges);
 					prevPEId = currPEId;
 				}
 				if (currPrivilegeId.equals("0"))
 				{
 					privilege = new Privilege();
 					privilege.setName("OWNER");
 				}
 				else
 				{
 					privilege = (Privilege)this.getObjectByPrimaryKey(s, Privilege.class, new Long(currPrivilegeId));
 				}
 				privileges.add(privilege);
 			}
 			
 			
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			if (log.isDebugEnabled())
 				log.debug("Authorization|||getProtectionElementPrivilegeContextForUser|Failure|Error in Obtaining the PE Privileges Map|" + ex.getMessage());
 		} finally {
 			try {
 				preparedStatement.close();
 				rs.close();
 				
 			} catch (Exception ex2) {
 			}
 
 			try {
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log.debug("Authorization|||getProtectionElementPrivilegeContextForUser|Failure|Error in Closing Session |" + ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log.debug("Authorization|||getProtectionElementPrivilegeContextForUser|Success|Successful in Obtaining the PE Privileges Map|");
 		return protectionElementPrivilegeContextSet;
 	}
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.dao.AuthorizationDAO#getProtectionElementPrivilegeContextForGroup(java.lang.String)
 	 */
 	public Set getProtectionElementPrivilegeContextForGroup(String groupId) throws CSObjectNotFoundException {
 		Set protectionElementPrivilegeContextSet = new HashSet();
 
 		Session s = null;
 		Connection connection = null;
 		PreparedStatement preparedStatement = null;
 		ResultSet rs = null;
 		
 		String currPEId = null;
 		String prevPEId = null;
 
 		String currPrivilegeId = null;
 		Set privileges = null;
 		Privilege privilege = null;
 		
 		List peList = new ArrayList();
 		List privList = new ArrayList();
 
 		boolean firstTime = true;
 		ProtectionElementPrivilegeContext protectionElementPrivilegeContext = null;
 		
 		try {
 
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			connection = s.connection();
 
 			preparedStatement = Queries.getQueryforGroupPEPrivilegeMap(groupId, this.application.getApplicationId().intValue(),connection);
 			
 			rs = preparedStatement.executeQuery();
 			
 			while(rs.next()){
 				peList.add(rs.getString(1));
 				privList.add(rs.getString(2));
 				
 			}
 			
 			Iterator currPEIdIterator = peList.iterator();
 			Iterator currPrivilegeIdIterator = privList.iterator();
 			
 			while(currPEIdIterator.hasNext()){
 				
 				currPEId = (String)currPEIdIterator.next();
 				currPrivilegeId = (String)currPrivilegeIdIterator.next();
 
 				
 				if (!currPEId.equals(prevPEId))
 				{
 					protectionElementPrivilegeContext = new ProtectionElementPrivilegeContext();
 					protectionElementPrivilegeContextSet.add(protectionElementPrivilegeContext);
 					ProtectionElement protectionElement = (ProtectionElement) this.getObjectByPrimaryKey(s, ProtectionElement.class, new Long(currPEId));
 					protectionElementPrivilegeContext.setProtectionElement(protectionElement);
 					privileges = new HashSet();
 					protectionElementPrivilegeContext.setPrivileges(privileges);
 					prevPEId = currPEId;
 				}
 				if (currPrivilegeId.equals("0"))
 				{
 					privilege = new Privilege();
 					privilege.setName("OWNER");
 				}
 				else
 				{
 					privilege = (Privilege)this.getObjectByPrimaryKey(s, Privilege.class, new Long(currPrivilegeId));
 				}
 				privileges.add(privilege);
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			if (log.isDebugEnabled())
 				log.debug("Authorization|||getProtectionElementPrivilegeContextForGroup|Failure|Error in Obtaining the PE Privileges Map|" + ex.getMessage());
 		} finally {
 			try {
 				preparedStatement.close();
 				rs.close();
 				
 			} catch (Exception ex2) {
 			}
 
 			try {
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log.debug("Authorization|||getProtectionElementPrivilegeContextForGroup|Failure|Error in Closing Session |" + ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log.debug("Authorization|||getProtectionElementPrivilegeContextForGroup|Success|Successful in Obtaining the PE Privileges Map|");
 		return protectionElementPrivilegeContextSet;
 	}
 	
 	public void modifyObject(Object obj) throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 		try {
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			t = s.beginTransaction();
 
 			
 			try {
 				obj = performEncrytionDecryption(obj, true);
 			} catch (EncryptionException e) {
 				throw new CSObjectNotFoundException(e);
 			}
 			try{
 				obj = ObjectUpdater.trimObjectsStringFieldValues(obj);
 			}catch(Exception e){
 				throw new CSObjectNotFoundException(e);
 			}
 			
 			
 			s.update(obj);
 			t.commit();
 			s.flush();
 			auditLog.info("Updating the " + obj.getClass().getName().substring(obj.getClass().getName().lastIndexOf(".")+1) + " Object ");
 		} 
 		catch (PropertyValueException pve)
 		{
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log.debug("Authorization|||createObject|Failure|Error in Rolling Back Transaction|" + ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||createObject|Failure|Error in Rolling Back Transaction|" + pve.getMessage());
 			throw new CSTransactionException(
 					"An error occured in updating the "	+ StringUtilities.getClassName(obj.getClass().getName()) + ".\n" + " A null value was passed for a required attribute " + pve.getMessage().substring(pve.getMessage().indexOf(":")), pve);
 		}
 		catch (ConstraintViolationException cve)
 		{
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log.debug("Authorization|||createObject|Failure|Error in Rolling Back Transaction|" + ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||createObject|Failure|Error in Rolling Back Transaction|" + cve.getMessage());
 			throw new CSTransactionException(
 					"An error occured in updating the "	+ StringUtilities.getClassName(obj.getClass().getName()) + ".\n" + " Duplicate entry was found in the database for the entered data" , cve);
 		}
 		catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||modifyObject|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||modifyObject|Failure|Error in modifying the "
 								+ obj.getClass().getName()
 								+ "|"
 								+ ex.getMessage());
 			throw new CSTransactionException(
 					"An error occured in modifying the "
 							+ StringUtilities.getClassName(obj.getClass()
 									.getName()) + "\n" + ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||modifyObject|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||modifyObject|Success|Successful in modifying the "
 							+ obj.getClass().getName() + "|");
 
 	}
 
 	
 
 	public Application getApplication() {
 		return this.application;
 	}
 
 	public void createObject(Object obj) throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 		try {
 			
 			try {
 				obj = performEncrytionDecryption(obj, true);
 			} catch (EncryptionException e) {
 				throw new CSObjectNotFoundException(e);
 			}
 			
 			
 			try{
 				obj = ObjectUpdater.trimObjectsStringFieldValues(obj);
 			}catch(Exception e){
 				throw new CSObjectNotFoundException(e);
 			}
 			
 				
 			
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			t = s.beginTransaction();
 			s.save(obj);
 			t.commit();
 			s.flush();
 			auditLog.info("Creating the " + obj.getClass().getName().substring(obj.getClass().getName().lastIndexOf(".")+1) + " Object ");			
 		} 
 		catch (PropertyValueException pve)
 		{
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log.debug("Authorization|||createObject|Failure|Error in Rolling Back Transaction|" + ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||createObject|Failure|Error in Rolling Back Transaction|" + pve.getMessage());
 			throw new CSTransactionException(
 					"An error occured in creating the "	+ StringUtilities.getClassName(obj.getClass().getName()) + ".\n" + " A null value was passed for a required attribute " + pve.getMessage().substring(pve.getMessage().indexOf(":")), pve);
 		}
 		catch (ConstraintViolationException cve)
 		{
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log.debug("Authorization|||createObject|Failure|Error in Rolling Back Transaction|" + ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||createObject|Failure|Error in Rolling Back Transaction|" + cve.getMessage());
 			throw new CSTransactionException(
 					"An error occured in creating the "	+ StringUtilities.getClassName(obj.getClass().getName()) + ".\n" + " Duplicate entry was found in the database for the entered data" , cve);
 		}		
 		catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||createObject|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||createObject|Failure|Error in creating the "
 								+ obj.getClass().getName()
 								+ "|"
 								+ ex.getMessage());
 			throw new CSTransactionException(
 					"An error occured in creating the "
 							+ StringUtilities.getClassName(obj.getClass()
 									.getName()) + "\n" + ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||createObject|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||createObject|Success|Successful in creating the "
 							+ obj.getClass().getName() + "|");
 	}
 
 	/**
 	 * @param application
 	 *            The application to set.
 	 */
 	public void setApplication(Application application) {
 		this.application = application;
 	}
 
 	public Set getGroups(String userId) throws CSObjectNotFoundException {
 		Session s = null;
 		Set groups = new HashSet();
 		try {
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 
 			User user = (User) this.getObjectByPrimaryKey(s, User.class,
 					new Long(userId));
 			groups = user.getGroups();
 			
 			
 			Iterator groupIterator = groups.iterator();
 			Set removedGroups = new HashSet();
 			while(groupIterator.hasNext()){
 				Group g = (Group)groupIterator.next();
 				if( g.getApplication().getApplicationId().intValue() != this.application.getApplicationId().intValue()){
 					removedGroups.add(g);
 				}	
 			}
 			groups.removeAll(removedGroups);
 			List list = new ArrayList();
 			Iterator toSortIterator = groups.iterator();
 			while(toSortIterator.hasNext()){ list.add(toSortIterator.next()); }
 			Collections.sort(list);
 			groups.clear();
 			groups.addAll(list);
 			
 			log.debug("The result size:" + groups.size());
 
 		} catch (Exception ex) {
 			log.error(ex);
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||getGroups|Failure|Error in obtaining Groups for User Id "
 								+ userId + "|" + ex.getMessage());
 			throw new CSObjectNotFoundException(
 					"An error occurred while obtaining Associated Groups for the User\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||getGroups|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||getGroups|Success|Successful in obtaining Groups for User Id "
 							+ userId + "|");
 		return groups;
 
 	}
 
 	public Set getProtectionElements(String protectionGroupId)
 			throws CSObjectNotFoundException {
 		Session s = null;
 		Set result = new HashSet();
 		try {
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			ProtectionGroup protectionGroup = (ProtectionGroup) this
 					.getObjectByPrimaryKey(s, ProtectionGroup.class, new Long(
 							protectionGroupId));
 			result = protectionGroup.getProtectionElements();
 			
 			List list = new ArrayList();
 			Iterator toSortIterator = result.iterator();
 			while(toSortIterator.hasNext()){ list.add(toSortIterator.next()); }
 			Collections.sort(list);
 			result.clear();
 			result.addAll(list);
 			
 			log.debug("The result size is: " + result.size());
 
 		} catch (Exception ex) {
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||getProtectionElements|Failure|Error in obtaining Protection Elements for Protection Group Id "
 								+ protectionGroupId + "|" + ex.getMessage());
 			throw new CSObjectNotFoundException(
 					"An error occurred while obtaining Associated Protection Elements for the Protection Group\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||getProtectionElements|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||getProtectionElements|Success|Succesful in obtaining Protection Elements for Protection Group Id "
 							+ protectionGroupId + "|");
 		return result;
 	}
 
 	public Set getProtectionGroups(String protectionElementId)
 			throws CSObjectNotFoundException {
 		Session s = null;
 		Set result = new HashSet();
 		try {
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			if (StringUtilities.isBlank(protectionElementId)) {
 				throw new CSObjectNotFoundException("Primary key can't be null");
 			}
 			ProtectionElement protectionElement = (ProtectionElement) this
 					.getObjectByPrimaryKey(s, ProtectionElement.class,
 							new Long(protectionElementId));
 			result = protectionElement.getProtectionGroups();
 			
 			List list = new ArrayList();
 			Iterator toSortIterator = result.iterator();
 			while(toSortIterator.hasNext()){ list.add(toSortIterator.next()); }
 			Collections.sort(list);
 			result.clear();
 			result.addAll(list);
 			
 			log.debug("The result size:" + result.size());
 
 		} catch (Exception ex) {
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||getProtectionGroups|Failure|Error in obtaining Protection Groups for Protection Element Id "
 								+ protectionElementId + "|" + ex.getMessage());
 			throw new CSObjectNotFoundException(
 					"An error occurred while obtaining Associated Protection Groups for the Protection Element\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||getProtectionGroups|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||getProtectionGroups|Success|Successful in obtaining Protection Groups for Protection Element Id "
 							+ protectionElementId + "|");
 		return result;
 	}
 
 	public void addToProtectionGroups(String protectionElementId,
 			String[] protectionGroupIds) throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 
 		try {
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			ProtectionElement protectionElement = (ProtectionElement) s.load(ProtectionElement.class,new Long(protectionElementId));
 			if(protectionElement==null)
 				throw new CSTransactionException("Authorization|||addToProtectionGroups|| Unable to retrieve Protection Element with ProtectionElementId :"+protectionElementId);
 			
 			Set<ProtectionGroup> protectionGroups = protectionElement.getProtectionGroups();
 			if(protectionGroups==null)
 				protectionGroups = new HashSet();
 
 			for (int k = 0; k < protectionGroupIds.length; k++) {
 				boolean assigned = false;
 				if(protectionGroupIds[k]!=null && protectionGroupIds[k].length()>0){
 					ProtectionGroup pr = (ProtectionGroup) s.load(ProtectionGroup.class,new Long(protectionGroupIds[k]));
 					if (pr != null) {
 						Iterator it=protectionGroups.iterator();
 						while(it.hasNext()){
 							ProtectionGroup p = (ProtectionGroup)it.next();
 							if(p.equals(pr)) assigned=true;
 						}
 						if(!assigned) protectionGroups.add(pr);
 					}
 				}
 			}
 			
 			
 			protectionElement.setProtectionGroups(protectionGroups);
 			
 			t = s.beginTransaction();
 			s.update(protectionElement);
 			t.commit();
 			s.flush();
 			auditLog.info("Adding Protection Groups to Protection Element with Object Id " + protectionElement.getObjectId() + " and Attribute " + protectionElement.getAttribute());
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||addToProtectionGroups|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||addToProtectionGroups|Failure|Error in assigning Protection Groups "
 								+ StringUtilities
 										.stringArrayToString(protectionGroupIds)
 								+ " to protection element id "
 								+ protectionElementId + "|" + ex.getMessage());
 			throw new CSTransactionException(
 					"An error occurred in adding Protection Groups to the Protection Element\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||addToProtectionGroups|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||addToProtectionGroups|Success|Successful in adding Protection Groups"
 							+ StringUtilities
 									.stringArrayToString(protectionGroupIds)
 							+ " to protection element id "
 							+ protectionElementId + "|");
 	}
 
 	
 	public void assignToProtectionGroups(String protectionElementId,
 			String[] protectionGroupIds) throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 
 		try {
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			ProtectionElement protectionElement = (ProtectionElement) this
 			.getObjectByPrimaryKey(s, ProtectionElement.class,
 					new Long(protectionElementId));
 			
 			s.close();
 
 			Set newSet = new HashSet();
 
 			for (int k = 0; k < protectionGroupIds.length; k++) {
 				log.debug("The new list:" + protectionGroupIds[k]);
 				ProtectionGroup pg = (ProtectionGroup) this
 						.getObjectByPrimaryKey(ProtectionGroup.class,
 								protectionGroupIds[k]);
 				if (pg != null) {
 					newSet.add(pg);
 				}
 			}
 			protectionElement.setProtectionGroups(newSet);
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			t = s.beginTransaction();
 			s.update(protectionElement);
 			t.commit();
 			s.flush();
 			auditLog.info("Assigning Protection Groups to Protection Element with Object Id " + protectionElement.getObjectId() + " and Attribute " + protectionElement.getAttribute());
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||assignToProtectionGroups|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||assignToProtectionGroups|Failure|Error in assigning Protection Groups "
 								+ StringUtilities
 										.stringArrayToString(protectionGroupIds)
 								+ " to protection element id "
 								+ protectionElementId + "|" + ex.getMessage());
 			throw new CSTransactionException(
 					"An error occurred in assigning Protection Groups to the Protection Element\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||assignToProtectionGroups|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||assignToProtectionGroups|Success|Successful in assigning Protection Groups Back Transaction"
 							+ StringUtilities
 									.stringArrayToString(protectionGroupIds)
 							+ " to protection element id "
 							+ protectionElementId + "|");
 	}
 
 	public void assignParentProtectionGroup(String parentProtectionGroupId,
 		String childProtectionGroupId) throws CSTransactionException {
 		Session s = null;
 		Transaction t = null;
 
 		try {
 			s= HibernateSessionFactoryHelper.getAuditSession(sf);
 			ProtectionGroup parent = null;
 
 			ProtectionGroup child = (ProtectionGroup) this
 					.getObjectByPrimaryKey(s, ProtectionGroup.class, new Long(
 							childProtectionGroupId));
 
 			
 			
 			if (parentProtectionGroupId != null) {
 				parent = (ProtectionGroup) this.getObjectByPrimaryKey(s,
 						ProtectionGroup.class,
 						new Long(parentProtectionGroupId));
 			} else {
 				parent = null;
 			}
 
 			child.setParentProtectionGroup(parent);
 		
 		
 			t = s.beginTransaction();
 			s.update(child);
 			t.commit();
 			s.flush();
 			if ( parent == null ) 
 			{
 				auditLog.info("Parent of Protection Group " + child.getProtectionGroupName() + " successfully removed");
 			}
 			else
 			{
 				auditLog.info("Assigning Protection Group " + parent.getProtectionGroupName() + " as Parent of Protection Group " + child.getProtectionGroupName());
 			}
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||assignParentProtectionGroup|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||assignParentProtectionGroup|Failure|Error in assigning Parent Protection Groups"
 								+ parentProtectionGroupId
 								+ " to protection group id "
 								+ childProtectionGroupId
 								+ "|"
 								+ ex.getMessage());
 			throw new CSTransactionException(
 					"An error occurred in assigning Parent Protection Group to the Protection Group\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||assignParentProtectionGroup|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||assignParentProtectionGroup|Success|Successful in assigning Parent Protection Groups"
 							+ parentProtectionGroupId
 							+ " to protection group id "
 							+ childProtectionGroupId + "|");
 	}
 
 	private ObjectAccessMap getObjectAccessMap(String objectTypeName,
 			String loginName, String privilegeName) {
 		Hashtable accessMap = new Hashtable();
 		Session s = null;
 
 		Connection connection = null;
 		PreparedStatement preparedStatement = null;
 		ResultSet rs = null;
 
 		try {
 
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 
 			connection = s.connection();
 
 			
 			preparedStatement= Queries.getQueryForObjectMap(loginName,objectTypeName,privilegeName,this.application.getApplicationId().intValue(),connection);
 			
 			rs = preparedStatement.executeQuery();
 
 			while (rs.next()) {
 				String att = rs.getString("attribute");
 				log.debug("The attribute is: " + att);
 				Boolean b = new Boolean(true);
 				accessMap.put(att.toLowerCase(), b);
 			}
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||getObjectAccessMap|Failure|Error in Obtaining the Object Access Map|"
 								+ ex.getMessage());
 		} finally {
 			try {
 				
 				preparedStatement.close();
 				rs.close();
 				
 			} catch (Exception ex2) {
 				ex2.printStackTrace();
 			}
 
 			try {
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||getObjectAccessMap|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||getObjectAccessMap|Success|Successful in Obtaining the Object Access Map|");
 		return new ObjectAccessMap(objectTypeName, accessMap);
 	}
 
 	public Object secureObject(String userName, Object obj) throws CSException {
 		Object o = null;
 		if (StringUtilities.isBlank(userName)) {
 			throw new CSException("No user name have been supplied!");
 		}
 		if (obj == null) {
 			return obj;
 		}
 		Field[] fields = obj.getClass().getDeclaredFields();
 		for (int i=0; i < fields.length; i++)
 		{
 			if (fields[i].getType().isPrimitive()) 
 				throw new CSException("The Object to be secured does not follow Java Bean Specification");
 		}
 		try {
 
 			Class cl = obj.getClass();
 			log.debug(cl.getName());
 			ObjectAccessMap accessMap = this.getObjectAccessMap(cl.getName(),
 					userName, "READ");
 			
 			
 
 			log.debug(accessMap.toString());
 
 			o = cl.newInstance();
 			Method methods[] = cl.getDeclaredMethods();
 
 			for (int i = 0; i < methods.length; i++) {
 				Method m = methods[i];
 
 				String name = m.getName();
 				//log.debug("Name from outer block"+name);
 				//log.debug("Para type"+m.getParameterTypes());
 				if (name.startsWith("set")
 						&& (m.getModifiers() == Modifier.PUBLIC)) {
 					String att = name.substring(3, name.length());
 					String methodName = "get" + att;
 					//log.debug(methodName);
 					Method m2 = cl.getMethod(methodName, (Class [])null);
 					//log.debug("Method Name m2"+m2.getName());
 					//log.debug(m2.invoke(obj,null));
 					if (!accessMap.hasAccess(att)) {
 						m.invoke(o, new Object[] { null });
 					} else {
 						m.invoke(o, new Object[] { m2.invoke(obj,(Object []) null) });
 					}
 				}
 			}
 
 		} catch (Exception ex) {
 			if (log.isDebugEnabled())
 				log.debug("Authorization||" + userName
 						+ "|secureObject|Failure|Error in Secure Object|"
 						+ ex.getMessage());
 
 			throw new CSException("Failed to secure the object:"
 					+ ex.getMessage(), ex);
 		}
 
 		return o;
 
 	}
 
 	public Collection secureCollection(String userName, Collection collection)
 			throws CSException {
 		ArrayList result = new ArrayList();
 		if (collection.size() == 0) {
 			return collection;
 		}
 		if (StringUtilities.isBlank(userName)) {
 			throw new CSException("No userName have been supplied!");
 		}
 		try {
 			Iterator it = collection.iterator();
 			List l = (List) collection;
 			Object obj_ = (Object) l.get(0);
 
 			Class cl = obj_.getClass();
 			log.debug(cl.getName());
 			ObjectAccessMap accessMap = this.getObjectAccessMap(cl.getName(),
 					userName, "READ");
 			while (it.hasNext()) {
 				Object obj = (Object) it.next();
 				Object o = cl.newInstance();
 				Method methods[] = cl.getDeclaredMethods();
 
 				for (int i = 0; i < methods.length; i++) {
 					Method m = methods[i];
 
 					String name = m.getName();
 					//log.debug("Name from outer block"+name);
 					//log.debug("Para type"+m.getParameterTypes());
 					if (name.startsWith("set")
 							&& (m.getModifiers() == Modifier.PUBLIC)) {
 						String att = name.substring(3, name.length());
 						String methodName = "get" + att;
 						//log.debug(methodName);
 						Method m2 = cl.getMethod(methodName, (Class [])null);
 						//log.debug("Method Name m2"+m2.getName());
 						//log.debug(m2.invoke(obj,null));
 						if (!accessMap.hasAccess(att)) {
 							m.invoke(o, new Object[] { null });
 						} else {
 							m.invoke(o, new Object[] { m2.invoke(obj, (Object []) null) });
 						}
 					}
 				}
 				result.add(o);
 			}
 
 		} catch (Exception ex) {
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization||"
 								+ userName
 								+ "|secureCollection|Failure|Error in Secure Collection|"
 								+ ex.getMessage());
 
 			throw new CSException("Failed to secure Collection:"
 					+ ex.getMessage(), ex);
 		}
 
 		return result;
 
 	}
 
 	public Object secureUpdate(String userName, Object originalObject,
 			Object mutatedObject) throws CSException {
 		//Object o = null;
 		if (StringUtilities.isBlank(userName)) {
 			throw new CSException("No user name have been supplied!");
 		}
 		if (originalObject == null || mutatedObject == null) {
 			return originalObject;
 		}
 		try {
 
 			Class cl = originalObject.getClass();
 			log.debug(cl.getName());
 			ObjectAccessMap accessMap = this.getObjectAccessMap(cl.getName(),
 					userName, "UPDATE");
              
 			
 			//o = cl.newInstance();
 			Method methods[] = cl.getDeclaredMethods();
 
 			for (int i = 0; i < methods.length; i++) {
 				Method m = methods[i];
 
 				String name = m.getName();
 				log.debug("Method is: " + name);
 				//log.debug("Name from outer block"+name);
 				//log.debug("Para type"+m.getParameterTypes());
 				if (name.startsWith("set")
 						&& (m.getModifiers() == Modifier.PUBLIC)) {
 					String att = name.substring(3, name.length());
 					log.debug("Attribute is: " + att);
 					String methodName = "get" + att;
 					//log.debug(methodName);
 					Method m2 = cl.getMethod(methodName, (Class[])null);
 					//log.debug("Method Name m2"+m2.getName());
 					//log.debug(m2.invoke(obj,null));
 					if (!accessMap.hasAccess(att)) {
 						log.debug("No Access to update attribute: " + att);
 						Object origValue = m2.invoke(originalObject, (Object[]) null);
 						if (origValue != null) {
 							log.debug("Original value is: "
 									+ origValue.toString());
 						}
 						m.invoke(mutatedObject, new Object[] { origValue });
 					} else {
 						log.debug("Access permitted to update attribute: "
 								+ att);
 					}
 				}
 			}
 
 		} catch (Exception ex) {
 			log.error("Error Securing object", ex);
 			if (log.isDebugEnabled())
 				log.debug("Authorization||" + userName
 						+ "|secureUpdate|Failure|Error in Secure Update|"
 						+ ex.getMessage());
 
 			throw new CSException("Failed to secure update the object:"
 					+ ex.getMessage(), ex);
 		}
 
 		return mutatedObject;
 
 	}
 
 	public Set getOwners(String protectionElementId)
 			throws CSObjectNotFoundException {
 
 		Session s = null;
 
 		Set result = new TreeSet();
 		
 		try {
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			ProtectionElement protectionElement = (ProtectionElement) this
 					.getObjectByPrimaryKey(s, ProtectionElement.class,
 							new Long(protectionElementId));
 
 			Set reresult = protectionElement.getOwners();
 			
 			List list = new ArrayList();
 			Iterator toSortIterator = reresult.iterator();
 			while(toSortIterator.hasNext()){ list.add(toSortIterator.next()); }
 			
 			Collections.sort(list);
 			result.addAll(list);
 			
 			
 			log.debug("The result size is: " + result.size());
 
 		} catch (Exception ex) {
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||getOwners|Failure|An Error occured in retrieving the Owners for the Protection Element Id "
 								+ protectionElementId + "|" + ex.getMessage());
 			throw new CSObjectNotFoundException(
 					"An error occured in retrieving the Owners for the Protection Element\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||getOwners|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||getOwners|Success|Successful in retrieving the Owners for the Protection Element Id "
 							+ protectionElementId + "|");
 		return result;
 	}
 	
 	public void addOwners(String protectionElementId, String[] userIds) throws CSTransactionException{
 
 		Session s = null;
 		Transaction t = null;
 		
 		try {
 			
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			ProtectionElement protectionElement = (ProtectionElement) s.load(ProtectionElement.class,Long.parseLong(protectionElementId));
 			if(protectionElement==null) throw new CSTransactionException("Authorization|||addOwners|| Unable to retrieve ProtectionElement with Id :"+protectionElementId);
 			Set userSet = protectionElement.getOwners();
 			if(userSet==null) userSet=new HashSet();
 			
 			for (int i = 0; i < userIds.length; i++) {
 				boolean assigned= false;
 				Iterator iterator = userSet.iterator();
 				while(iterator.hasNext()){
 					User us =(User)iterator.next();
 					if(userIds[i].equalsIgnoreCase(us.getUserId().toString()));
 					assigned=true;
 				}
 				if(!assigned){
 					User user = (User) s.load(User.class, Long.parseLong(userIds[i]));
 					if(user!=null)
 						userSet.add(user);
 				}
 			}
 			
 			t = s.beginTransaction();
 			s.update(protectionElement);
 			t.commit();
 			s.flush();
 			auditLog.info("Adding Users as Owner of Protection Element with Object Id " + protectionElement.getObjectId() + " and Attribute " + protectionElement.getAttribute());
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||addOwners|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||addOwners|Failure|Error in assigning the Owners "
 								+ StringUtilities.stringArrayToString(userIds)
 								+ "for the Protection Element Id "
 								+ protectionElementId + "|");
 			throw new CSTransactionException(
 					"An error occured in assigning Owners to the Protection Element\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||addOwners|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||addOwners|Success|Successful in adding the Owners to Protection Element"
 							+ StringUtilities.stringArrayToString(userIds)
 							+ "for the Protection Element Id "
 							+ protectionElementId + "|");
 	}
 
 
 	public void assignOwners(String protectionElementId, String[] userIds)
 			throws CSTransactionException {
 
 		Session s = null;
 		Transaction t = null;
 
 		try {
 			
 
 			Set users = new HashSet();
 
 			for (int i = 0; i < userIds.length; i++) {
 				User user = (User) this.getObjectByPrimaryKey(User.class,
 						userIds[i]);
 				users.add(user);
 			}
 			ProtectionElement pe = (ProtectionElement) this
 					.getObjectByPrimaryKey(ProtectionElement.class,
 							protectionElementId);
 
 			pe.setOwners(users);
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			t = s.beginTransaction();
 			s.update(pe);
 			t.commit();
 			s.flush();
 			auditLog.info("Assigning Users as Owner of Protection Element with Object Id " + pe.getObjectId() + " and Attribute " + pe.getAttribute());
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||setOwners|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||setOwners|Failure|Error in assigning the Owners "
 								+ StringUtilities.stringArrayToString(userIds)
 								+ "for the Protection Element Id "
 								+ protectionElementId + "|");
 			throw new CSTransactionException(
 					"An error occured in assigning Owners to the Protection Element\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||setOwners|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||setOwners|Success|Successful in assigning the Owners "
 							+ StringUtilities.stringArrayToString(userIds)
 							+ "for the Protection Element Id "
 							+ protectionElementId + "|");
 	}
 
 	public boolean checkOwnership(String userName,
 			String protectionElementObjectId) {
 		boolean test = false;
 		Session s = null;
 		PreparedStatement preparedStatement = null;
 		Connection connection = null;
 		ResultSet rs = null;
 
 		try {
 
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 
 			connection = s.connection();
 
 			StringBuffer stbr = new StringBuffer();
 			stbr
 					.append("Select  user_protection_element_id from"
 							+ " csm_user_pe upe, csm_user u, csm_protection_element pe"
 							+ " where pe.object_id = ?  and u.login_name = ?" 
 							+ " and upe.protection_element_id=pe.protection_element_id"
 							+ " and upe.user_id = u.user_id");
 
 			preparedStatement = connection.prepareStatement(stbr.toString());;
 			int i=1;			
 			preparedStatement.setString(i++,protectionElementObjectId);
 			preparedStatement.setString(i++,userName);
 			
 			rs = preparedStatement.executeQuery();
 			if (rs.next()) {
 				test = true;
 			}
 
 		} catch (Exception ex) {
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization||"
 								+ userName
 								+ "|checkOwnerShip|Failure|Error in checking ownership for user "
 								+ userName + " and Protection Element "
 								+ protectionElementObjectId + "|"
 								+ ex.getMessage());
 		} finally {
 			try {
 				rs.close();
 				preparedStatement.close();
 				
 
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||checkOwnerShip|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 
 			try {
 				s.close();
 			} catch (Exception ex) {
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization||"
 							+ userName
 							+ "|checkOwnerShip|Success|Successful in checking ownership for user "
 							+ userName + " and Protection Element "
 							+ protectionElementObjectId + "|");
 		return test;
 	}
 
 	public Collection getPrivilegeMap(String userName, Collection pEs)
 			throws CSException {
 		ArrayList result = new ArrayList();
 		ResultSet rs = null;
 		PreparedStatement pstmt = null;
 		boolean test = false;
 		Session s = null;
 
 		Connection connection = null;
 
 		if (StringUtilities.isBlank(userName)) {
 			throw new CSException("userName can't be null!");
 		}
 		if (pEs == null) {
 			throw new CSException(
 					"protection elements collection can't be null!");
 		}
 		if (pEs.size() == 0) {
 			return result;
 		}
 
 		try {
 
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 
 			connection = s.connection();
 			
 
 			StringBuffer stbr = new StringBuffer();
 			stbr.append(" select distinct(p.privilege_name)");
 			stbr.append(" from csm_protection_group pg,");
 			stbr.append(" csm_protection_element pe,");
 			stbr.append(" csm_pg_pe pgpe,");
 			stbr.append(" csm_user_group_role_pg ugrpg,");
 			stbr.append(" csm_user u,");
 			stbr.append(" csm_group g,");
 			stbr.append(" csm_user_group ug,");
 			stbr.append(" csm_role_privilege rp,");
 			stbr.append(" csm_privilege p ");
 			stbr.append(" where pgpe.protection_group_id = pg.protection_group_id");
 			stbr.append(" and pgpe.protection_element_id = pe.protection_element_id");
 			stbr.append(" and pe.object_id= ?");
 			stbr.append(" and (pe.attribute is null or pe.attribute=?)");
 			stbr.append(" and pg.protection_group_id = ugrpg.protection_group_id ");
 			stbr.append(" and (( ugrpg.group_id = g.group_id");
 			stbr.append(" and g.group_id = ug.group_id");
 			stbr.append(" and ug.user_id = u.user_id)");
 			stbr.append(" or ");
 			stbr.append(" (ugrpg.user_id = u.user_id))");
 			stbr.append(" and u.login_name=?");
 			stbr.append(" and ugrpg.role_id = rp.role_id ");
 			stbr.append(" and rp.privilege_id = p.privilege_id");
 
 			String sql = stbr.toString();
 			pstmt = connection.prepareStatement(sql);
 
 			Iterator it = pEs.iterator();
 			while (it.hasNext()) {
 				ProtectionElement pe = (ProtectionElement) it.next();
 				ArrayList privs = new ArrayList();
 				if (pe.getObjectId() != null) {
 					pstmt.setString(1, pe.getObjectId());
 					if (pe.getAttribute() != null) {
 						pstmt.setString(2, pe.getAttribute());
 					} else {
 						// Using blank string to act as NULL
 						pstmt.setString(2, "" );
 					}
 					pstmt.setString(3, userName);
 				}
 
 				rs = pstmt.executeQuery();
 
 				while (rs.next()) {
 					String priv = rs.getString(1);
 					Privilege p = new Privilege();
 					p.setName(priv);
 					privs.add(p);
 				}
 				rs.close();
 				ObjectPrivilegeMap opm = new ObjectPrivilegeMap(pe, privs);
 				result.add(opm);
 			}
 			
 			//Collections.sort(result);
 
 			pstmt.close();
 			
 		} catch (Exception ex) {
 			if (log.isDebugEnabled())
 				log.debug("Failed to get privileges for " + userName + "|"
 						+ ex.getMessage());
 			throw new CSException("Failed to get privileges for " + userName
 					+ "|" + ex.getMessage(), ex);
 		} finally {
 			try {
 
 				s.close();
 				rs.close();
 				pstmt.close();
 				
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||getPrivilegeMap|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 
 		return result;
 	}
 
 	
 	private Object performEncrytionDecryption(Object obj, boolean encrypt) throws EncryptionException {
 		
 		if(obj instanceof User){
 			User user = (User)obj;
 			
 			if(this.isEncryptionEnabled && StringUtilities.initTrimmedString(user.getPassword()).length()>0){
 				StringEncrypter stringEncrypter = new StringEncrypter();
 				if(encrypt){
 					user.setPassword(stringEncrypter.encrypt(user.getPassword().trim()));
 				}else{
 					user.setPassword(stringEncrypter.decrypt(user.getPassword().trim()));
 				}
 			}
 			return user;
 		}
 		
 		if(obj instanceof Application){
 			Application application = (Application)obj;
 			
 			if(this.isEncryptionEnabled && StringUtilities.initTrimmedString(application.getDatabasePassword()).length()>0){
 				StringEncrypter stringEncrypter = new StringEncrypter();
 				if(encrypt){
 					application.setDatabasePassword(stringEncrypter.encrypt(application.getDatabasePassword().trim()));
 				}else{
 					application.setDatabasePassword(stringEncrypter.decrypt(application.getDatabasePassword().trim()));
 				}
 			}
 			return application;
 		}
 		
 		return obj;		
 	}
 
 	public Application getApplication(String applicationContextName) throws CSObjectNotFoundException
 	{
 		return getApplicationByName(applicationContextName);
 	}
 	
 	public void removeOwnerForProtectionElement(String loginName, String protectionElementObjectId, String protectionElementAttributeName)	throws CSTransactionException {
 
 		Session s = null;
 		Transaction t = null;
 		if (StringUtilities.isBlank(loginName)) {
 			throw new CSTransactionException("Login Name can't be null");
 		}
 		if (StringUtilities.isBlank(protectionElementObjectId)) {
 			throw new CSTransactionException("Object Id can't be null");
 		}
 		try {
 
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			
 			User user = getLightWeightUser(loginName);
 			
 			
 
 			
 			if (user == null) {
 				throw new CSTransactionException("No user found for this login name");
 			}
 			ProtectionElement pe = new ProtectionElement();
 			pe.setObjectId(protectionElementObjectId);
 			pe.setApplication(application);
 			if (protectionElementAttributeName != null && protectionElementAttributeName.length() > 0) {
 				pe.setAttribute(protectionElementAttributeName);
 			}			
 			SearchCriteria sc = new ProtectionElementSearchCriteria(pe);
 			List l = this.getObjects(s,sc);
 
 			if (l.size() == 0) {
 				throw new CSTransactionException("No Protection Element found for the given object id and attribute");
 			}
 			
 			ProtectionElement protectionElement = (ProtectionElement) l.get(0);
 
 			Set ownerList = protectionElement.getOwners();
 			if (ownerList == null || ownerList.size() == 0)
 			{
 				/*ownerList = new HashSet();
 				ownerList.add(user);*/
 			}
 			else
 			{
 				if (ownerList.contains(user))
 				{
 					ownerList.remove(user);
 				}
 			}
 			protectionElement.setOwners(ownerList);
 			t = s.beginTransaction();
 			s.save(protectionElement);
 			t.commit();
 			s.flush();
 			auditLog.info("Removing User " + loginName + " as Owner for Protection Element with Object Id " + protectionElement.getObjectId() + " and Attribute " + protectionElement.getAttribute());
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||removeOwnerForProtectionElement|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||removeOwnerForProtectionElement|Failure|Error removing owner for Protection Element object Name"
 								+ protectionElementObjectId
 								+ " and Attribute Id "
 								+ protectionElementAttributeName
 								+ " for user "
 								+ loginName + "|" + ex.getMessage());
 			throw new CSTransactionException(
 					"An error occured in removing owner for the Protection Element\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||removeOwnerForProtectionElement|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||setOwnerForProtectionElement|Success|Success in removing owner for Protection Element object Name"
 							+ protectionElementObjectId
 							+ " and Attribute Id "
 							+ protectionElementAttributeName
 							+ " for user "
 							+ loginName + "|");
 	}
 	
 	public void removeOwnerForProtectionElement(String protectionElementObjectId,
 			String[] userNames) throws CSTransactionException {
 
 		Session s = null;
 		Transaction t = null;
 		if (StringUtilities.isBlank(protectionElementObjectId)) {
 			throw new CSTransactionException("object Id can't be null!");
 		}
 		try {
 			
 
 			Set users = new HashSet();
 
 			for (int i = 0; i < userNames.length; i++) {
 				User user = this.getUser(userNames[i]);
 				if (user != null) {
 					users.add(user);
 				}
 			}
 			ProtectionElement pe = new ProtectionElement();
 			pe.setObjectId(protectionElementObjectId);
 			pe.setApplication(application);
 			SearchCriteria sc = new ProtectionElementSearchCriteria(pe);
 			List l = this.getObjects(sc);
 
 			ProtectionElement protectionElement = (ProtectionElement) l.get(0);
 
 			Set ownerList = protectionElement.getOwners();
 			if (ownerList != null && ownerList.size() > 0)
 			{
 				Iterator iterator = users.iterator();
 				while(iterator.hasNext()){
 					User user = (User)iterator.next();
 					if (ownerList.contains(user))
 					{
 						ownerList.remove(user);
 					}
 				}
 			}
 			
 			protectionElement.setOwners(ownerList);
 			s = HibernateSessionFactoryHelper.getAuditSession(sf);
 			t = s.beginTransaction();
 			s.update(protectionElement);
 			t.commit();
 			s.flush();
 			auditLog.info("Removing Users as Owner for Protection Element with Object Id " + protectionElement.getObjectId() + " and Attribute " + protectionElement.getAttribute());		
 		} catch (Exception ex) {
 			log.error(ex);
 			try {
 				t.rollback();
 			} catch (Exception ex3) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||removeOwnerForProtectionElement|Failure|Error in Rolling Back Transaction|"
 									+ ex3.getMessage());
 			}
 			if (log.isDebugEnabled())
 				log
 						.debug("Authorization|||removeOwnerForProtectionElement|Failure|Error removing owner for Protection Element object Name"
 								+ protectionElementObjectId
 								+ " for users "
 								+ StringUtilities
 										.stringArrayToString(userNames)
 								+ "|"
 								+ ex.getMessage());
 			throw new CSTransactionException(
 					"An error occured in removing multiple owners for the Protection Element\n"
 							+ ex.getMessage(), ex);
 		} finally {
 			try {
 				
 				s.close();
 			} catch (Exception ex2) {
 				if (log.isDebugEnabled())
 					log
 							.debug("Authorization|||removeOwnerForProtectionElement|Failure|Error in Closing Session |"
 									+ ex2.getMessage());
 			}
 		}
 		if (log.isDebugEnabled())
 			log
 					.debug("Authorization|||removeOwnerForProtectionElement|Success|Successful in removing owner for Protection Element object Name"
 							+ protectionElementObjectId
 							+ " for users "
 							+ StringUtilities.stringArrayToString(userNames)
 							+ "|");
 	}
 
 	public List getAttributeMap(String userName, String className, String privilegeName)
 	{
 		List attributeList = new ArrayList();
 		ResultSet resultSet = null;
 		
 		Session session = HibernateSessionFactoryHelper.getAuditSession(sf);
 		Connection connection = session.connection();
 		
 		PreparedStatement preparedStatement = null;
 		
 		try
 		{
 			preparedStatement = Queries.getQueryforUserAttributeMap(userName, className, privilegeName, this.application.getApplicationId().intValue(),connection);
 			resultSet = preparedStatement.executeQuery();
 
 			while(resultSet.next())
 			{
 				attributeList.add(resultSet.getString(1));				
 			}
 		} 
 		catch (Exception ex) 
 		{
 			ex.printStackTrace();
 			if (log.isDebugEnabled())
 				log.debug("Authorization|||getAttributeMap|Failure|Error in Obtaining the Attribute Map|" + ex.getMessage());
 		} 
 		finally 
 		{
 			try 
 			{
 				preparedStatement.close();
 				resultSet.close();
 			} 
 			catch (Exception ex2) 
 			{
 			}
 			try 
 			{
 				session.close();
 			} 
 			catch (Exception ex2) 
 			{
 				if (log.isDebugEnabled())
 					log.debug("Authorization|||getAttributeMap|Failure|Error in Closing Session |" + ex2.getMessage());
 			}
 		}
 		
 		if (log.isDebugEnabled())
 			log.debug("Authorization|||getAttributeMap|Success|Successful in Obtaining the Attribute Map|");
 		return attributeList; 		
 	}
 
 	public List getAttributeMapForGroup(String groupName, String className, String privilegeName)
 	{
 		
 		List attributeList = new ArrayList();
 		ResultSet resultSet = null;
 		
 		Session session = HibernateSessionFactoryHelper.getAuditSession(sf);
 		Connection connection = session.connection();
 		
 		PreparedStatement preparedStatement = null;
 		
 		try
 		{
 			preparedStatement = Queries.getQueryforGroupAttributeMap(groupName, className, privilegeName, this.application.getApplicationId().intValue(),connection);
 			resultSet = preparedStatement.executeQuery();
 
 			while(resultSet.next())
 			{
 				attributeList.add(resultSet.getString(1));				
 			}
 		} 
 		catch (Exception ex) 
 		{
 			ex.printStackTrace();
 			if (log.isDebugEnabled())
 				log.debug("Authorization|||getAttributeMapForGroups|Failure|Error in Obtaining the Attribute Map|" + ex.getMessage());
 		} 
 		finally 
 		{
 			try 
 			{
 				preparedStatement.close();
 				resultSet.close();
 			} 
 			catch (Exception ex2) 
 			{
 			}
 			try 
 			{
 				session.close();
 			} 
 			catch (Exception ex2) 
 			{
 				if (log.isDebugEnabled())
 					log.debug("Authorization|||getAttributeMapForGroups|Failure|Error in Closing Session |" + ex2.getMessage());
 			}
 		}
 		
 		if (log.isDebugEnabled())
 			log.debug("Authorization|||getAttributeMapForGroups|Success|Successful in Obtaining the Attribute Map|");
 		return attributeList; 		
 	}
 
 	public void refreshInstanceTables(boolean instanceLevelSecurityForUser) throws CSObjectNotFoundException, CSDataAccessException {
 	
 		
 		
 		
 		//Get Mapping Table Entries for Instance Level Security performance.
 		InstanceLevelMappingElement mappingElement = new InstanceLevelMappingElement();
 		List<InstanceLevelMappingElement> mappingElements = getObjects(new InstanceLevelMappingElementSearchCriteria(mappingElement));
 		if (mappingElements== null || mappingElements.size() == 0)
 		{
 			//throw new RuntimeException ("Instance Level Mappging Elements does not exist");
 			throw new CSObjectNotFoundException("Instance Level Mapping Elements do not exist.");
 		}
 
 		Statement statement = null;
 		Transaction transaction = null;
 		Session session = null;
 		Connection connection = null;
 
 		try{
 			
 			session = HibernateSessionFactoryHelper.getAuditSession(sf);
 			transaction = session.beginTransaction();
 			connection = session.connection();
 			connection.setAutoCommit(false);
 			statement = connection.createStatement();
 			
 			Iterator mappingElementsIterator = mappingElements.iterator();
 			while(mappingElementsIterator.hasNext()){
 				InstanceLevelMappingElement instanceLevelMappingEntry = (InstanceLevelMappingElement) mappingElementsIterator.next();
 				if(instanceLevelMappingEntry !=null ){
 					if(instanceLevelMappingEntry.getActiveFlag()==0){
 						// Not active, so ignore this Object + Attribute from refresh logic.
 						continue;
 					}	
 					if(StringUtilities.isBlank(instanceLevelMappingEntry.getAttributeName()) || StringUtilities.isBlank(instanceLevelMappingEntry.getObjectName()) ||  StringUtilities.isBlank(instanceLevelMappingEntry.getTableName()) 
 								|| StringUtilities.isBlank(instanceLevelMappingEntry.getTableNameForUser()) || StringUtilities.isBlank(instanceLevelMappingEntry.getViewNameForUser())){
 							//Mapping Entry is invalid.
 							throw new CSObjectNotFoundException("Invalid Instance Level Mapping Element. Instance Level Security breach is possible.");
 					}	
 				}else{
 					//Mapping Entry is invalid.
 					continue;
 					//throw new Exception("Invalid Instance Level Mapping Element. Instance Level Security breach is possible.");
 				}
 				//get the Table Name and View Name for each object.
 				
 				String applicationID = this.application.getApplicationId().toString();
 				String peiTableName,tableNameUser,viewNameUser ,tableNameGroup,viewNameGroup = null;
 				String peiObjectId = null;
 				if(StringUtilities.isBlank(instanceLevelMappingEntry.getObjectPackageName())) {
 					peiObjectId = instanceLevelMappingEntry.getObjectName().trim();
 				}else{
 					peiObjectId = instanceLevelMappingEntry.getObjectPackageName().trim() + instanceLevelMappingEntry.getObjectName().trim();
 				}
 				
 				String peiAttribute = instanceLevelMappingEntry.getAttributeName().trim();
 				
 				peiTableName = "CSM_PEI_"+instanceLevelMappingEntry.getObjectName()+"_"+instanceLevelMappingEntry.getAttributeName();
 				
 				
 				if(StringUtilities.isBlank(instanceLevelMappingEntry.getTableNameForUser())){
 					tableNameUser= "CSM_"+instanceLevelMappingEntry.getObjectName()+"_"+instanceLevelMappingEntry.getAttributeName()+"_USER";
 				}else{
 					tableNameUser = instanceLevelMappingEntry.getTableNameForUser();
 				}
 				if(StringUtilities.isBlank(instanceLevelMappingEntry.getViewNameForUser())){
 					viewNameUser= "CSM_VW_"+instanceLevelMappingEntry.getObjectName()+"_"+instanceLevelMappingEntry.getAttributeName()+"_USER";
 				}else{
 					viewNameUser = instanceLevelMappingEntry.getViewNameForUser();
 				}
 				if(StringUtilities.isBlank(instanceLevelMappingEntry.getTableNameForGroup())){
 					tableNameGroup= "CSM_"+instanceLevelMappingEntry.getObjectName()+"_"+instanceLevelMappingEntry.getAttributeName()+"_GROUP";
 				}else{
 					tableNameGroup= instanceLevelMappingEntry.getTableNameForGroup();
 				}
 				if(StringUtilities.isBlank(instanceLevelMappingEntry.getViewNameForGroup())){
 					viewNameGroup= "CSM_VW_"+instanceLevelMappingEntry.getObjectName()+"_"+instanceLevelMappingEntry.getAttributeName()+"_GROUP";
 				}else{
 					viewNameGroup = instanceLevelMappingEntry.getViewNameForGroup();
 				}
 				
 	
 				/* Optional: Add Additional checks regarding Table and View record count. 
 				 * At the time of delete, if the MINUS is close to or greater than 50% of the records of the Table, 
 	             * then truncate table instead of deleting using delete statement.
 	             *
 	             * Note: No buffering until real tests warrant buffering. 
 				 */
 				
 				byte activeFlag = instanceLevelMappingEntry.getActiveFlag();
 				if(activeFlag==1){	
 					
 					//refresh PEI Table
 					statement.addBatch("DELETE FROM "+peiTableName+
 							"	 WHERE application_id = "+applicationID+" AND protection_element_id " +
 							"	 NOT IN (" +
 							"	 SELECT pe.protection_element_id from CSM_PROTECTION_ELEMENT pe" +
 							"    WHERE pe.object_id = '"+peiObjectId+"' AND  pe.attribute = '"+peiAttribute+"' AND  pe.application_id = "+applicationID+" )");
 					statement.executeBatch();
 					statement.addBatch("INSERT INTO "+peiTableName+" (protection_element_id, attribute_value, application_id) " +
 							"		SELECT protection_element_id, attribute_value,application_id from CSM_PROTECTION_ELEMENT pe" +
							"		WHERE attribute_value !=null AND (protection_element_id) " +
							"			NOT IN (" +
							"				SELECT protection_element_id from "+peiTableName+" )");
 					
 					
 					statement.executeBatch();
 					
 					
 					if(instanceLevelSecurityForUser){
 						statement.addBatch("DELETE FROM "+tableNameUser+"" +
 								"	 WHERE (user_ID,privilege_name,attribute_value,application_id) " +
 								"	 NOT IN (" +
 								"	 SELECT user_ID,privilege_name,attribute_value,application_id from "+viewNameUser+
 								     ");");
 						statement.executeBatch();
 						statement.addBatch("INSERT INTO "+tableNameUser+" (user_ID,login_name,privilege_name,attribute_value,application_id) " +
 								"		SELECT DISTINCT user_ID,login_name,privilege_name,attribute_value,application_id from "+viewNameUser+" " +
 								"		WHERE attribute_value!=null AND (user_ID,privilege_name,attribute_value,application_id) " +
 								"			NOT IN ( SELECT user_ID,privilege_name,attribute_value,application_id from "+tableNameUser+" )");
 						
 						
 						statement.executeBatch();
 					}else{
 						statement.addBatch("DELETE FROM "+tableNameGroup+"" +
 								"	 WHERE (group_ID,privilege_name,attribute_value,application_id) " +
 								"	 NOT IN (" +
 								"	 SELECT group_ID,privilege_name,attribute_value,application_id from "+viewNameGroup+
 								     ")");
 						
 						statement.addBatch("INSERT INTO "+tableNameGroup+" (group_ID,group_name,privilege_name,attribute_value,application_id) " +
 								"		SELECT DISTINCT group_ID,group_name,privilege_name,attribute_value,application_id from "+viewNameGroup+" " +
 								"		WHERE (group_ID,privilege_name,attribute_value,application_id) " +
 								"			NOT IN (" +
 								"				SELECT group_ID,privilege_name,attribute_value,application_id from "+tableNameGroup+" )");
 
 						statement.executeBatch();
 					}
 				}				
 			}
 				
 			transaction.commit();
 			statement.close();
 		}catch(CSObjectNotFoundException e1){
 			if(transaction!=null){
 				try {
 					transaction.rollback();
 				} catch (Exception ex3) {
 				}
 			}
 			throw new CSObjectNotFoundException(e1.getMessage());
 		} catch (SQLException e1) {
 			if(transaction!=null){
 				try {
 					transaction.rollback();
 				} catch (Exception ex3) {
 				}
 			}
 			throw new CSDataAccessException("Unable to perform data refresh for instance level security.");
 		}catch (Exception e) {
 			if(transaction!=null){
 				try {
 					transaction.rollback();
 				} catch (Exception ex3) {
 				}
 			}
 			throw new CSDataAccessException("Unable to perform data refresh for instance level security.");
 		}
 		finally 
 		{
 			try{
 				connection.close();
 			}catch (Exception ex2) 
 			{ }
 			try{ 
 				session.close();
 			}catch (Exception ex2) 
 			{
 				if (log.isDebugEnabled())
 					log.debug("Authorization|||refreshInstanceTables|Failure|Error in Closing Session |" + ex2.getMessage());
 			}
 		}
 	}
 	
 	
 
 	public void maintainInstanceTables(String instanceLevelMappingElementId) throws CSObjectNotFoundException, CSDataAccessException {
 //		Get Mapping Table Entries for Instance Level Security performance.
 		InstanceLevelMappingElement mappingElement = new InstanceLevelMappingElement();
 		if(!StringUtilities.isBlank(instanceLevelMappingElementId)){
 			mappingElement.setMappingId(new Long(instanceLevelMappingElementId));
 		}
 		List<InstanceLevelMappingElement> mappingElements = getObjects(new InstanceLevelMappingElementSearchCriteria(mappingElement));
 		if (mappingElements== null || mappingElements.size() == 0)
 		{
 			// No Mapping Elements. So no tables to maintain
 			return;
 		}
 
 		Statement statement = null;
 		Transaction transaction = null;
 		Session session = null;
 		Connection connection = null;
 
 		try{
 			
 			session = HibernateSessionFactoryHelper.getAuditSession(sf);
 			transaction = session.beginTransaction();
 			connection = session.connection();
 			connection.setAutoCommit(false);
 			statement = connection.createStatement();
 			
 			//create view CSM_VW_ROLE_PRIV
 			statement.addBatch("   create or replace view csm_vw_role_priv"
 								+" as"
 								+" select crp.role_id, substr(cp.privilege_name, 1, 30) privilege_name, cr.application_id"
 								+" from csm_role_privilege crp, csm_privilege cp, csm_role cr"
 								+" where crp.role_id = cr.role_id and crp.privilege_id = cp.privilege_id" 
 								+" and cr.active_flag = 1");
 
 			
 			Iterator mappingElementsIterator = mappingElements.iterator();
 			while(mappingElementsIterator.hasNext()){
 				InstanceLevelMappingElement instanceLevelMappingEntry = (InstanceLevelMappingElement) mappingElementsIterator.next();
 				if(instanceLevelMappingEntry !=null ){
 					if(instanceLevelMappingEntry.getActiveFlag()==0){
 						// Not active, so ignore this Object + Attribute from table/view maintain logic.
 						continue;
 					}	
 					if(StringUtilities.isAlphaNumeric(instanceLevelMappingEntry.getAttributeName()) 
 							|| StringUtilities.isAlphaNumeric(instanceLevelMappingEntry.getObjectPackageName())
 							|| StringUtilities.isAlphaNumeric(instanceLevelMappingEntry.getObjectName()) 
 							||  StringUtilities.isAlphaNumeric(instanceLevelMappingEntry.getTableName())  ){
 							
 						//Mapping Entry is valid.
 						
 					}else{
 						//	Mapping Entry is invalid.
 						//ignore this mapping element.
 						continue;
 					}
 				}else{
 					//Mapping Entry is invalid.
 					continue;
 					//throw new Exception("Invalid Instance Level Mapping Element. Instance Level Security breach is possible.");
 				}
 				//mark this mappging entry is maintained.
 				statement.addBatch("UPDATE csm_mapping SET MAINTAINED_FLAG = '1' " +
 						"WHERE mapping_id = "+instanceLevelMappingEntry.getMappingId());
 							
 				//get the Table Name and View Name for each object.
 				
 				String peiTableName,tableNameUser,viewNameUser ,tableNameGroup,viewNameGroup = null;
 				
 				peiTableName = "CSM_PEI_"+instanceLevelMappingEntry.getObjectName()+"_"+instanceLevelMappingEntry.getAttributeName();
 				
 				if(StringUtilities.isBlank(instanceLevelMappingEntry.getTableNameForUser())){
 					tableNameUser= "CSM_"+instanceLevelMappingEntry.getObjectName()+"_"+instanceLevelMappingEntry.getAttributeName()+"_USER";
 					
 				}else{
 					tableNameUser = instanceLevelMappingEntry.getTableNameForUser();
 				}
 				if(StringUtilities.isBlank(instanceLevelMappingEntry.getViewNameForUser())){
 					viewNameUser= "CSM_VW_"+instanceLevelMappingEntry.getObjectName()+"_"+instanceLevelMappingEntry.getAttributeName()+"_USER";
 				}else{
 					viewNameUser = instanceLevelMappingEntry.getViewNameForUser();
 				}
 				if(StringUtilities.isBlank(instanceLevelMappingEntry.getTableNameForGroup())){
 					tableNameGroup= "CSM_"+instanceLevelMappingEntry.getObjectName()+"_"+instanceLevelMappingEntry.getAttributeName()+"_GROUP";
 				}else{
 					tableNameGroup= instanceLevelMappingEntry.getTableNameForGroup();
 				}
 				if(StringUtilities.isBlank(instanceLevelMappingEntry.getViewNameForGroup())){
 					viewNameGroup= "CSM_VW_"+instanceLevelMappingEntry.getObjectName()+"_"+instanceLevelMappingEntry.getAttributeName()+"_GROUP";
 				}else{
 					viewNameGroup = instanceLevelMappingEntry.getViewNameForGroup();
 				}
 				
 	
 				/* Optional: Add Additional checks regarding Table and View record count. 
 				 * At the time of delete, if the MINUS is close to or greater than 50% of the records of the Table, 
 	             * then truncate table instead of deleting using delete statement.
 	             *
 	             * Note: No buffering until real tests warrant buffering. 
 				 */
 				
 				byte activeFlag = instanceLevelMappingEntry.getActiveFlag();
 				if(activeFlag==1){	
 					
 					
 					
 					//create pei table
 					statement.addBatch("CREATE TABLE IF NOT EXISTS "+peiTableName+"  (" +
 							"  APPLICATION_ID bigint(20) NOT NULL," +
 							"  ATTRIBUTE_VALUE bigint(20) NOT NULL," +
 							"  PROTECTION_ELEMENT_ID bigint(20) NOT NULL," +
 							"  PRIMARY KEY  (PROTECTION_ELEMENT_ID)," +
 							"  UNIQUE KEY UQ_MP_OBJ_NAME_ATTRI_VAL_APP_ID (PROTECTION_ELEMENT_ID,ATTRIBUTE_VALUE,APPLICATION_ID)," +
 							"  KEY idx_APPLICATION_ID (APPLICATION_ID)," +
 							"  CONSTRAINT FK_PE_APPLICATION1 FOREIGN KEY FK_PE_APPLICATION1 (APPLICATION_ID) REFERENCES csm_application (APPLICATION_ID) ON DELETE CASCADE ON UPDATE CASCADE" +
 							"  );");
 					
 					//create tableNameForUser							
 					statement.addBatch("CREATE TABLE IF NOT EXISTS "+tableNameUser+" (" +
 							" USER_ID bigint(20) NOT NULL," +
 							" LOGIN_NAME varchar(200) NOT NULL," +
 							" PRIVILEGE_NAME varchar(30) NOT NULL," +
 							" APPLICATION_ID bigint(20) NOT NULL," +
 							" ATTRIBUTE_VALUE bigint(20) NOT NULL," +
 							" UNIQUE KEY UQ_USERID_APID_PRIV (USER_ID,APPLICATION_ID, PRIVILEGE_NAME)," +
 							" UNIQUE KEY UQ_LOGINNAME_APID_PRIV (LOGIN_NAME,APPLICATION_ID, PRIVILEGE_NAME)," +
 							" KEY idx_USER_ID (USER_ID)," +
 							" KEY idx_LOGIN_NAME (LOGIN_NAME)," +
 							" KEY idx_APPLICATION_ID (APPLICATION_ID)," +
 							" KEY idx_PRIVILEGE_NAME (PRIVILEGE_NAME)" +
 							" );"); 
 					
 					//create tableNameForGroup
 					statement.addBatch("CREATE TABLE IF NOT EXISTS "+tableNameGroup+" (" +
 							" GROUP_ID bigint(20) NOT NULL," +
 							" GROUP_NAME varchar(100) NOT NULL," +
 							" PRIVILEGE_NAME varchar(30) NOT NULL," +
 							" APPLICATION_ID bigint(20) NOT NULL," +
 							" ATTRIBUTE_VALUE bigint(20) NOT NULL," +
 							" UNIQUE KEY UQ_GRPID_APID_PRIV (GROUP_ID,APPLICATION_ID, PRIVILEGE_NAME)," +
 							" UNIQUE KEY GRPNM_APID_PRIV (GROUP_NAME,APPLICATION_ID, PRIVILEGE_NAME)," +
 							" KEY idx_GROUP_ID (GROUP_ID)," +
 							" KEY idx_GROUP_NAME (GROUP_NAME)," +
 							" KEY idx_APPLICATION_ID (APPLICATION_ID)," +
 							" KEY idx_PRIVILEGE_NAME (PRIVILEGE_NAME)" +
 							" );");
 					
 					//create viewNameForUser
 					statement.addBatch("create or replace view "+viewNameUser+"_temp" +
 							" as select pr.user_id,u.login_name,pr.role_id,pe.application_id,pe.attribute_value" +
 							" from csm_user_pe cu , "+peiTableName+" pe, csm_user_group_role_pg pr, csm_user u" +
 							" where cu.protection_element_id = pe.protection_element_id and cu.user_id = pr.user_id and pr.user_id = u.user_id;") ;
 					
 					statement.addBatch("create or replace view "+viewNameUser+
 							" as" +
 							" select pe.user_id, pe.login_name ,pr.privilege_name,pe.application_id,pe.attribute_value" +
 							" from "+viewNameUser+"_temp pe,csm_vw_role_priv pr" +
 							" where pe.role_id = pr.role_id");
 
 					
 					//create viewNameForGroup
 					statement.addBatch("create or replace view "+viewNameGroup+"_temp" +
 							" as" +
 							" select pr.group_id, g.group_name,pr.role_id, pe.application_id, pe.attribute_value" +
 							"  from csm_pg_pe cp, "+peiTableName+" pe, csm_user_group_role_pg pr, csm_group g" +
 							" where cp.protection_element_id = pe.protection_element_id" +
 							"  and cp.protection_group_id = pr.protection_group_id and pr.group_id = g.group_id") ;
 					
 					statement.addBatch("create or replace view "+viewNameGroup+
 							" as" +
 							" select pe.group_id, pe.group_name, pr.privilege_name, pe.application_id, pe.attribute_value" +
 							" from "+viewNameGroup+"_temp pe, csm_vw_role_priv pr" +
 							" where pe.role_id = pr.role_id");
 
 					
 				}				
 			}
 				
 			statement.executeBatch();
 			transaction.commit();
 			statement.close();
 		}catch (SQLException e1) {
 			if(transaction!=null){
 				try {
 					transaction.rollback();
 				} catch (Exception ex3) {
 				}
 			}
 			throw new CSDataAccessException("Unable to maintain tables/views for instance level security.");
 		}catch (Exception e) {
 			if(transaction!=null){
 				try {
 					transaction.rollback();
 				} catch (Exception ex3) {
 				}
 			}
 
 			throw new CSDataAccessException("Unable to maintain tables/views for instance level security.");
 		}
 		finally 
 		{
 			try{
 				connection.close();
 			}catch (Exception ex2) 
 			{ }
 			try{ 
 				session.close();
 			}catch (Exception ex2) 
 			{
 				if (log.isDebugEnabled())
 					log.debug("Authorization|||maintainInstanceTables|Failure|Error in Closing Session |" + ex2.getMessage());
 			}
 		}
 		
 	}
 	
 	
 }
