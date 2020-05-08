 package com.euroit.militaryshop.persistence.dao.impl;
 
 import com.euroit.eshop.persistence.dao.impl.BaseEntityManagerSupport;
 import com.euroit.militaryshop.dto.DictionaryEntryDto;
 import com.euroit.militaryshop.enums.DictionaryName;
 import com.euroit.militaryshop.persistence.dao.DictionaryEntryDao;
 import com.euroit.militaryshop.persistence.entity.DictionaryEntry;
 import org.springframework.stereotype.Component;
 
 import javax.persistence.Query;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;

 import java.util.List;
 
 @Component
 public class DictionaryEntryDaoImpl extends BaseEntityManagerSupport
 		implements DictionaryEntryDao {
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<DictionaryEntry> getAllEntries() {
		Query query = em.createQuery("SELECT FROM DictionaryEntry de");
 		
 		return query.getResultList();
 	}
 	
 	@Override
 	public long getAllEntriesCount() {
 		CriteriaBuilder cb = em.getCriteriaBuilder();
 		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
 		cq.select(cb.count(cq.from(DictionaryEntry.class)));
 		return em.createQuery(cq).getSingleResult();
 	}
 
 	@Override
 	public long createOrSave(DictionaryEntryDto dto) {
 		if (dto == null) {
 			return 0;
 		}
 		
 		DictionaryEntry dictEntry = null;
 		
 		if (dto.getId() != 0) {
 			dictEntry = em.find(DictionaryEntry.class, dto.getId());
 		}
 		
 		if (dictEntry == null) {
 			dictEntry = new DictionaryEntry();
 		}
 		
 		dictEntry.setDictionaryName(dto.getDictionaryName());
 		dictEntry.setValue(dto.getValue());
 		
 		return em.merge(dictEntry).getKey().getId();
 	}
 
 	@Override
 	public DictionaryEntry findDictionaryEntryById(long entryId) {
 		return em.find(DictionaryEntry.class, entryId);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<DictionaryEntry> getEntriesByDictName(DictionaryName dictName) {
 		Query q = em.createQuery("SELECT FROM DictionaryEntry de WHERE de.dictionaryName=:dictName");
 		q.setParameter("dictName", dictName.name());
 		
 		return q.getResultList();
 	}
 
 	@Override
 	public DictionaryEntry findDictionaryEntryByValueAndDictName(String value, DictionaryName dictName) {
 		Query q = em.createQuery("SELECT FROM DictionaryEntry de WHERE de.value=:value AND de.dictionaryName=:dictName");
 		q.setParameter("value", value);
 		q.setParameter("dictName", dictName.name());
 		
 		return (DictionaryEntry)q.getSingleResult();
 	}
 
 	
 }
