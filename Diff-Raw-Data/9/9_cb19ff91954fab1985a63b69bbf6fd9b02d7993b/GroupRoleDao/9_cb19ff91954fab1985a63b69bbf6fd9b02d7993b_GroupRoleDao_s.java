 package au.org.scoutmaster.dao;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import org.apache.log4j.Logger;
 
 import au.com.vaadinutils.dao.JpaBaseDao;
import au.org.scoutmaster.domain.Contact;
 import au.org.scoutmaster.domain.GroupRole;
 
 import com.vaadin.addon.jpacontainer.JPAContainer;
 
 public class GroupRoleDao extends JpaBaseDao<GroupRole, Long> implements Dao<GroupRole, Long>
 {
 	@SuppressWarnings("unused")
 	static private Logger logger = Logger.getLogger(GroupRoleDao.class);
 
 	public GroupRoleDao()
 	{
 		// inherit the default per request em.
 	}
 
 	public GroupRoleDao(EntityManager em)
 	{
 		super(em);
 	}
 
 	@SuppressWarnings("unchecked")
	public List<Contact> findByName(String name)
 	{
 		Query query = entityManager.createNamedQuery(GroupRole.FIND_BY_NAME);
 		query.setParameter("name", name);
		List<Contact> resultContacts = query.getResultList();
		return resultContacts;
 	}
 
 	@Override
 	public JPAContainer<GroupRole> createVaadinContainer()
 	{
 		return super.createVaadinContainer();
 	}
 }
