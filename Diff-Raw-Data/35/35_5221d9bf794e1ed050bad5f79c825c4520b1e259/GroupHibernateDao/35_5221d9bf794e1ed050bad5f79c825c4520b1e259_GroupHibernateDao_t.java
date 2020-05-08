 package org.jtalks.common.model.dao.hibernate;
 
 import org.hibernate.Query;
 import org.jtalks.common.model.dao.GroupDao;
 import org.jtalks.common.model.entity.Group;
 import org.jtalks.common.model.entity.User;
 import ru.javatalks.utils.general.Assert;
 
 import java.util.List;
 
 /**
  *
  */
public class GroupHibernateDao extends GenericDaoImpl<Group> implements GroupDao {
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings("unchecked")
     public List<Group> getAll() {
         return getSession().createQuery("from Group").list();
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     @Override
     public List<Group> getMatchedByName(String name) {
         Assert.throwIfNull(name, "name");
 
         Query query = getSession().createQuery("from Group g where g.name like ?");
         query.setString(0, "%" + name + "%");
 
         return query.list();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Group> getGroupsOfUser(User user) {
         Assert.throwIfNull(user, "user");
 
         Query query = getSession().createQuery("from Group g where g.users contains ?");
         query.setParameter(0, "%" + user + "%");
 
         return query.list();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Group getGroupByName(String name) {
         Assert.throwIfNull(name, "name");
 
         Query query = getSession().createQuery("from Group g where g.name = ?");
         query.setString(0, name);
 
         return (Group) query.uniqueResult();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void delete(Group group) {
         getSession().update(group);
 
         group.getUsers().clear();
         saveOrUpdate(group);
         super.delete(group);
     }
 }
