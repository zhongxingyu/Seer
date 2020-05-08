 /**
  * 
  */
 package net.frontlinesms.data.repository.hibernate;
 
 import java.util.List;
 
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.dao.DataAccessException;
 import org.springframework.dao.support.DataAccessUtils;
 import org.springframework.transaction.annotation.Transactional;
 
 import net.frontlinesms.data.DuplicateKeyException;
 import net.frontlinesms.data.domain.Contact;
 import net.frontlinesms.data.domain.Group;
 import net.frontlinesms.data.domain.GroupMembership;
 import net.frontlinesms.data.repository.GroupMembershipDao;
 
 /**
  * @author aga
  *
  */
 public class HibernateGroupMembershipDao extends BaseHibernateDao<GroupMembership> implements GroupMembershipDao {
 
 //> CONSTRUCTORS
 	HibernateGroupMembershipDao() {
 		super(GroupMembership.class);
 	}
 	
 //> DAO METHODS
 	/** @see GroupMembershipDao#addMember(Group, Contact) */
 	public boolean addMember(Group g, Contact contact) {
 		GroupMembership membership = new GroupMembership(g, contact);
 		try {
 			super.save(membership);
 			return true;
 		} catch (DuplicateKeyException e) {
 			return false;
 		}
 	}
 
 	/** @see GroupMembershipDao#getActiveMembers(Group) */
 	public List<Contact> getActiveMembers(Group group) {
 		if(group.isRoot()) {
 			String queryString = "SELECT c FROM Contact AS c WHERE c.active=TRUE";
 			return getList(Contact.class, queryString);
 		} else {
 			String queryString = "SELECT DISTINCT mem.contact FROM GroupMembership AS mem, Contact AS c WHERE c.active=TRUE AND (mem.group=? OR mem.group.path LIKE ?)";
 			String childPath = group.getPath() + Group.PATH_SEPARATOR + "%";
 			return getList(Contact.class, queryString, group, childPath);
 		}
 	}
 
 	/** @see GroupMembershipDao#getGroups(Contact) */
 	public List<Group> getGroups(Contact contact) {
 		String queryString = "SELECT mem.group FROM GroupMembership AS mem WHERE mem.contact=?";
 		return getList(Group.class, queryString, contact);
 	}
 
 	/** @see GroupMembershipDao#getMemberCount(Group) */
 	public int getMemberCount(Group group) {
 		if(group.isRoot()) {
 			DetachedCriteria crit = DetachedCriteria.forClass(Contact.class);
 			crit.setProjection(Projections.rowCount());
 			return DataAccessUtils.intResult(this.getHibernateTemplate().findByCriteria(crit));
 		} else {
 			String childPath = group.getPath() + Group.PATH_SEPARATOR + "%";
 			String queryString = "SELECT COUNT(DISTINCT mem.contact) " +
 					"FROM GroupMembership AS mem WHERE mem.group=? OR mem.group.path LIKE ?";
 			return super.getCount(queryString, group, childPath);
 		}
 	}
 
 	/** @see GroupMembershipDao#getMembers(Group) */
 	public List<Contact> getMembers(Group group) {
 		if(group.isRoot()) {
 			return getList(Contact.class, DetachedCriteria.forClass(Contact.class));
 		} else {
 			String childPath = group.getPath() + Group.PATH_SEPARATOR + "%";
 			String queryString = "SELECT DISTINCT mem.contact FROM GroupMembership AS mem WHERE mem.group=? OR mem.group.path LIKE ?";
 			return getList(Contact.class, queryString, new Object[]{group, childPath});
 		}
 	}
 
 	/** @see GroupMembershipDao#getFilteredMembers(Group, String) */
 	public List<Contact> getFilteredMembers(final Group group, String contactFilterString, int startIndex, int limit) {
 		// TODO this is quite an ugly way of doing limits
 		List<Contact> filteredMembers = getFilteredMembers(group, contactFilterString);
 		int toIndex = Math.min(filteredMembers.size(), startIndex + limit);
 		return filteredMembers.subList(startIndex, toIndex);
 	}
 	/** @see GroupMembershipDao#getFilteredMembers(Group, String) */
 	public List<Contact> getFilteredMembers(final Group group, String contactFilterString) {
		contactFilterString = contactFilterString.length() == 0 ? "%" : "%" + contactFilterString + "%";
		
 		if(group.isRoot()) {
 			String queryString = "SELECT c FROM Contact AS c WHERE LOWER(c.name) LIKE LOWER(?) OR LOWER(c.phoneNumber) LIKE LOWER(?)";
 			return getList(Contact.class, queryString, contactFilterString, contactFilterString);
 		} else {
 			String queryString = "SELECT mem.contact FROM GroupMembership AS mem WHERE " +
 					"(mem.group=? OR mem.group.path LIKE ?)" +
 					" AND (LOWER(mem.contact.name) LIKE LOWER(?) OR LOWER(mem.contact.phoneNumber) LIKE LOWER(?))";
 			String childPath = group.getPath() + Group.PATH_SEPARATOR + "%";
 			return getList(Contact.class, queryString, group, childPath, contactFilterString, contactFilterString);
 		}
 	}
 	
 	/** @see GroupMembershipDao#getFilteredMemberCount(Group, String) */
 	public int getFilteredMemberCount(final Group group, String contactFilterString) {
		contactFilterString = contactFilterString.length() == 0 ? "%" : "%" + contactFilterString + "%";
		
 		if(group.isRoot()) {
 			String queryString = "SELECT COUNT(DISTINCT c) FROM Contact AS c WHERE LOWER(c.name) LIKE LOWER(?) OR LOWER(c.phoneNumber) LIKE LOWER(?)";
 			return super.getCount(queryString, contactFilterString, contactFilterString);
 		} else {
 			String queryString = "SELECT COUNT(DISTINCT mem.contact) FROM GroupMembership AS mem WHERE " +
 					"(mem.group=? OR mem.group.path LIKE ?)" +
 					" AND (LOWER(mem.contact.name) LIKE LOWER(?) OR LOWER(mem.contact.phoneNumber) LIKE LOWER(?))";
 			String childPath = group.getPath() + Group.PATH_SEPARATOR + "%";
 			return super.getCount(queryString, group, childPath, contactFilterString, contactFilterString);
 		}
 	}
 
 	/** @see GroupMembershipDao#getMembers(Group, int, int) */
 	public List<Contact> getMembers(Group group, int startIndex, int limit) {
 		List<Contact> allMembers = getMembers(group);
 		return allMembers.subList(startIndex, Math.min(allMembers.size(), startIndex + limit));
 	}
 
 	/** @see GroupMembershipDao#isMember(Group, Contact) */
 	public boolean isMember(Group group, Contact contact) {
 		if(group.isRoot()) return true;
 		
 		String childPath = group.getPath() + Group.PATH_SEPARATOR + "%";
 		String queryString = "SELECT COUNT(*) FROM GroupMembership AS mem WHERE mem.contact=? AND (mem.group=? OR mem.group.path LIKE ?)";
 		return super.getCount(queryString, contact, group, childPath) > 0;
 	}
 
 	/** @see GroupMembershipDao#removeMember(Group, Contact) */
 	@Transactional
 	public boolean removeMember(Group group, Contact contact) {
 		try {
 			DetachedCriteria crit = getMembershipCriteria(group, contact);
 			this.getHibernateTemplate().delete(DataAccessUtils.uniqueResult(this.getList(crit)));
 			return true;
 		} catch(DataAccessException ex) {
 			return false;
 		}
 	}
 
 //> PRIVATE HELPER METHODS
 	/** @return criteria for fetching a {@link GroupMembership} entity */
 	private DetachedCriteria getMembershipCriteria(Group group, Contact contact) {
 		DetachedCriteria crit = super.getCriterion();
 		crit.add(Restrictions.eq("contact", contact));
 		crit.add(Restrictions.eq("group", group));
 		return crit;
 	}
 
 	/**
 	 * Gets a list of E matching the supplied criteria.
 	 * @param criteria
 	 * @return a list of Es matching the supplied criteria
 	 */
 	@SuppressWarnings("unchecked")
 	protected <T> List<T> getList(Class<T> entityClass, DetachedCriteria criteria) {
 		return this.getHibernateTemplate().findByCriteria(criteria);
 	}
 	
 	/**
 	 * Gets a list of E matching the supplied HQL query.
 	 * @param hqlQuery HQL query
 	 * @param values values to insert into the HQL query
 	 * @return a list of Es matching the supplied query
 	 */
 	@SuppressWarnings("unchecked")
 	protected <T> List<T> getList(Class<T> entityClass, String hqlQuery, Object... values) {
 		return this.getHibernateTemplate().find(hqlQuery, values);
 	}
 }
