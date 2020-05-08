 package com.mpower.dao;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.springframework.stereotype.Repository;
 
 import com.mpower.domain.ReportDataSubSource;
 import com.mpower.domain.ReportField;
 import com.mpower.domain.ReportFieldGroup;
 
 @Repository("reportFieldDao")
 public class JPAReportFieldDao implements ReportFieldDao {
 	@PersistenceContext
 	private EntityManager em;
 	
 
 	public ReportField copy(ReportField f) {
 		return new ReportField(f);
 	}
 
 	
 	public void delete(ReportField f) {
 		em.remove(f);
 
 	}
 
 	
 	public ReportField findById(long Id) {
 		return em.find(ReportField.class,Id);
 	}
 
 	
 	public List<ReportField> getAll() {
 		Query q = em
 		.createQuery("SELECT reportdatafield from com.mpower.domain.ReportField reportdatafield");
 		return  q.getResultList();
 }
 
 	
 	public List<ReportField> getAllByGroupId(Long id) {
		Query q = em.createQuery("select reportField from ReportField reportField join reportField.reportFieldGroup as rfg where rfg.id = ?");

 		q.setParameter(1, id);
 		return q.getResultList();
 	}
 
 	
 	public void save(ReportField f) {
 		em.persist(f);
 
 	}
 
 	
 	public ReportField update(ReportField f) {
 		em.merge(f);
 		return null;
 	}
 
 
 
 	
 
 }
