 package com.conx.logistics.app.whse.dao.services.impl;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.conx.logistics.app.whse.dao.services.IDockTypeDAOService;
 import com.conx.logistics.app.whse.domain.constants.DockTypeCustomCONSTANTS;
 import com.conx.logistics.app.whse.domain.docktype.DockType;
 import com.conx.logistics.common.utils.Validator;
import com.conx.logistics.mdm.domain.organization.Organization;
 
 @Transactional
 @Repository
 public class DockTypeDAOImpl implements IDockTypeDAOService {
 	protected Logger logger = LoggerFactory.getLogger(this.getClass());	
     /**
      * Spring will inject a managed JPA {@link EntityManager} into this field.
      */
     @PersistenceContext
     private EntityManager em;	
     
 	public void setEm(EntityManager em) {
 		this.em = em;
 	}
 
 	@Override
 	public DockType get(long id) {
 		return em.getReference(DockType.class, id);
 	}    
 
 	@Override
 	public List<DockType> getAll() {
 		return em.createQuery("select o from com.conx.logistics.app.whse.domain.docktype.DockType o record by o.id",DockType.class).getResultList();
 	}
 	
 	@Override
 	public DockType getByCode(String code) {
 		DockType org = null;
 		
 		try
 		{
			CriteriaBuilder builder = em.getCriteriaBuilder();
			CriteriaQuery<DockType> query = builder.createQuery(DockType.class);
			Root<DockType> rootEntity = query.from(DockType.class);
			ParameterExpression<String> p = builder.parameter(String.class);
			query.select(rootEntity).where(builder.equal(rootEntity.get("code"), p));

			TypedQuery<DockType> typedQuery = em.createQuery(query);
			typedQuery.setParameter(p, code);
			
			org = typedQuery.getSingleResult();
			/*
 			TypedQuery<DockType> q = em.createQuery("select o from com.conx.logistics.app.whse.domain.docktype.DockType o WHERE o.code = :code",DockType.class);
 			q.setParameter("code", code);
 						
 			org = q.getSingleResult();
			*/
 		}
 		catch(NoResultException e){}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		catch(Error e)
 		{
 			StringWriter sw = new StringWriter();
 			e.printStackTrace(new PrintWriter(sw));
 			String stacktrace = sw.toString();
 			logger.error(stacktrace);
 		}		
 		
 		return org;
 	}	
 
 	@Override
 	public DockType add(DockType record) {
 		record = em.merge(record);
 		
 		return record;
 	}
 
 	@Override
 	public void delete(DockType record) {
 		em.remove(record);
 	}
 
 	@Override
 	public DockType update(DockType record) {
 		return em.merge(record);
 	}
 
 
 	@Override
 	public DockType provide(DockType record) {
 		DockType existingRecord = getByCode(record.getCode());
 		if (Validator.isNull(existingRecord))
 		{		
 			record = update(record);
 			try {
 				//em.flush();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return record;
 	}
 	
 	@Override
 	public DockType provide(String code, String name) {
 		DockType res = null;
 		if ((res = getByCode(code)) == null)
 		{
 			DockType unit = new DockType();
 
 			unit.setCode(code);
 			unit.setName(name);
 
 			res = add(unit);
 		}
 		return res;
 	}	
 	
 	@Override
 	public void provideDefaults()
 	{
 		provide(DockTypeCustomCONSTANTS.TYPE_AH, DockTypeCustomCONSTANTS.TYPE_AH_DESCRIPTION);
 		provide(DockTypeCustomCONSTANTS.TYPE_PLT, DockTypeCustomCONSTANTS.TYPE_PLT_DESCRIPTION);
 		provide(DockTypeCustomCONSTANTS.TYPE_ROO, DockTypeCustomCONSTANTS.TYPE_ROO_DESCRIPTION);
 		provide(DockTypeCustomCONSTANTS.TYPE_SGC, DockTypeCustomCONSTANTS.TYPE_SGC_DESCRIPTION);
 		provide(DockTypeCustomCONSTANTS.TYPE_SGE, DockTypeCustomCONSTANTS.TYPE_SGE_DESCRIPTION);
 		provide(DockTypeCustomCONSTANTS.TYPE_UM, DockTypeCustomCONSTANTS.TYPE_UM_DESCRIPTION);
 	}
 }
