 package com.sensi.service.impl;
 
 import java.util.List;
 
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.sensi.domain.Role;
 import com.sensi.service.RoleService;
 
 @Service("roleService")
 @Transactional
 public class RoleServiceImpl implements RoleService {
 
 	@Autowired
 	private SessionFactory sessionFactory;
 
 	@Override
 	public Long countRoles() {
		return (Long) sessionFactory.getCurrentSession().createQuery("select count(o) from Role o").uniqueResult();
 	}
 
 	@Override
 	public void delete(Role role) {
 		if (role != null && role.getAuthority() != null) {
 			sessionFactory.getCurrentSession().delete(role);
 		}
 	}
 
 	@Override
	public Role findRole(String name) {
 		return (Role) sessionFactory.getCurrentSession().createQuery(
				"select o from Role o where o.name = :name").setString("name", name).uniqueResult();
 	}
 
 	@Override
 	public List<Role> findRoles() {
		return sessionFactory.getCurrentSession().createQuery("select o from Role o order by o.name").list();
 	}
 
 	@Override
 	public void save(Role role) {
 		sessionFactory.getCurrentSession().saveOrUpdate(role);
 	}
 
 }
