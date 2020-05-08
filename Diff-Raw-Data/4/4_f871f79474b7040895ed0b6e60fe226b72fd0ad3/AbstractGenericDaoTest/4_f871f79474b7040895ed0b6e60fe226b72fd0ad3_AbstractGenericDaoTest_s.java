 package com.mick8569.springhub.dao;
 
 import com.mick8569.springhub.models.entities.AbstractGenericEntity;
 import org.fest.assertions.api.Assertions;
 import org.junit.Test;
 import org.mockito.Mockito;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
import java.math.BigInteger;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class AbstractGenericDaoTest {
 
 	private final EntityManager entityManager = Mockito.mock(EntityManager.class);
 
 	@Test
 	public void test_flush() {
 		dao().flush();
 		Mockito.verify(entityManager).flush();
 	}
 
 	@Test
 	public void test_clear() {
 		dao().clear();
 		Mockito.verify(entityManager).clear();
 	}
 
 	@Test
 	public void test_merge() {
 		FooEntity foo = new FooEntity();
 		dao().merge(foo);
 		Mockito.verify(entityManager).merge(foo);
 	}
 
 	@Test
 	public void test_persist() {
 		FooEntity foo = new FooEntity();
 		dao().persist(foo);
 		Mockito.verify(entityManager).persist(foo);
 	}
 
 	@Test
 	public void test_remove() {
 		FooEntity foo = new FooEntity();
 		dao().remove(foo);
 		Mockito.verify(entityManager).remove(foo);
 	}
 
 	@Test
 	public void test_isManaged() {
 		FooEntity foo = new FooEntity();
 		dao().isManaged(foo);
 		Mockito.verify(entityManager).contains(foo);
 	}
 
 	@Test
 	public void test_detach() {
 		FooEntity foo = new FooEntity();
 		dao().detach(foo);
 		Mockito.verify(entityManager).detach(foo);
 	}
 
 	@Test
 	public void test_refresh() {
 		FooEntity foo = new FooEntity();
 		dao().refresh(foo);
 		Mockito.verify(entityManager).refresh(foo);
 	}
 
 	@Test
 	public void test_find() {
 		FooEntity foo = new FooEntity();
		Mockito.when(entityManager.find(FooEntity.class, BigInteger.ONE)).thenReturn(foo);
 		FooEntity result = dao().find(1L);
 		Assertions.assertThat(result).isNotNull().isEqualTo(foo);
 		Mockito.verify(entityManager).find(FooEntity.class, 1L);
 	}
 
 	@Test
 	public void test_findAll() {
 		List<FooEntity> entities = Arrays.asList(
 			new FooEntity(),
 			new FooEntity()
 		);
 
 		Query query  = Mockito.mock(Query.class);
 		Mockito.when(entityManager.createQuery("SELECT x FROM FooEntity x")).thenReturn(query);
 		Mockito.when(query.getResultList()).thenReturn(entities);
 
 		List<FooEntity> results = dao().findAll();
 		Assertions.assertThat(results).isNotNull().hasSize(2).isEqualTo(entities);
 		Mockito.verify(entityManager).createQuery("SELECT x FROM FooEntity x");
 		Mockito.verify(query).getResultList();
 	}
 
 	@Test
 	public void test_count() {
 		Query query  = Mockito.mock(Query.class);
 		Mockito.when(entityManager.createQuery("SELECT COUNT(x) FROM FooEntity x")).thenReturn(query);
 		Mockito.when(query.getSingleResult()).thenReturn(2L);
 
 		long count = dao().count();
 		Assertions.assertThat(count).isEqualTo(2);
 		Mockito.verify(entityManager).createQuery("SELECT COUNT(x) FROM FooEntity x");
 		Mockito.verify(query).getSingleResult();
 	}
 
 	@Test
 	public void test_getEntityList_singleQuery() {
 		List<FooEntity> entities = Arrays.asList(
 				new FooEntity(),
 				new FooEntity()
 		);
 
 		String str = "SELECT x FROM FooEntity x WHERE x.ID = 1";
 		Query query  = Mockito.mock(Query.class);
 		Mockito.when(entityManager.createQuery(str)).thenReturn(query);
 		Mockito.when(query.getResultList()).thenReturn(entities);
 
 		List<FooEntity> results = dao().getEntityList(str);
 		Assertions.assertThat(results).isNotNull().hasSize(2).isEqualTo(entities);
 		Mockito.verify(entityManager).createQuery(str);
 		Mockito.verify(query).getResultList();
 		Mockito.verify(query, Mockito.never()).setParameter(Mockito.anyString(), Mockito.anyObject());
 		Mockito.verify(query, Mockito.never()).setMaxResults(Mockito.anyInt());
 	}
 
 	@Test
 	public void test_getEntityList_singleQueryWithLimit() {
 		List<FooEntity> entities = Arrays.asList(
 				new FooEntity(),
 				new FooEntity()
 		);
 
 		String str = "SELECT x FROM FooEntity x WHERE x.ID = 1";
 		Query query  = Mockito.mock(Query.class);
 		Mockito.when(entityManager.createQuery(str)).thenReturn(query);
 		Mockito.when(query.getResultList()).thenReturn(entities);
 
 		List<FooEntity> results = dao().getEntityList(str, 10);
 		Assertions.assertThat(results).isNotNull().hasSize(2).isEqualTo(entities);
 		Mockito.verify(entityManager).createQuery(str);
 		Mockito.verify(query).getResultList();
 		Mockito.verify(query, Mockito.never()).setParameter(Mockito.anyString(), Mockito.anyObject());
 		Mockito.verify(query).setMaxResults(10);
 	}
 
 	@Test
 	public void test_getEntityList_singleQueryWithParameters() {
 		List<FooEntity> entities = Arrays.asList(
 				new FooEntity(),
 				new FooEntity()
 		);
 
 		String str = "SELECT x FROM FooEntity x WHERE x.ID = :id";
 		Map<String, Object> parameters = new HashMap<String, Object>();
 		parameters.put("id", 1L);
 
 		Query query  = Mockito.mock(Query.class);
 		Mockito.when(entityManager.createQuery(str)).thenReturn(query);
 		Mockito.when(query.getResultList()).thenReturn(entities);
 
 		List<FooEntity> results = dao().getEntityList(str, parameters);
 		Assertions.assertThat(results).isNotNull().hasSize(2).isEqualTo(entities);
 		Mockito.verify(entityManager).createQuery(str);
 		Mockito.verify(query).getResultList();
 		Mockito.verify(query).setParameter("id", 1L);
 		Mockito.verify(query, Mockito.never()).setMaxResults(Mockito.anyInt());
 	}
 
 	@Test
 	public void test_getEntityList_singleQueryWithLimitAndParameters() {
 		List<FooEntity> entities = Arrays.asList(
 				new FooEntity(),
 				new FooEntity()
 		);
 
 		String str = "SELECT x FROM FooEntity x WHERE x.ID = :id";
 		Map<String, Object> parameters = new HashMap<String, Object>();
 		parameters.put("id", 1L);
 
 		Query query  = Mockito.mock(Query.class);
 		Mockito.when(entityManager.createQuery(str)).thenReturn(query);
 		Mockito.when(query.getResultList()).thenReturn(entities);
 
 		List<FooEntity> results = dao().getEntityList(str, parameters, 10);
 		Assertions.assertThat(results).isNotNull().hasSize(2).isEqualTo(entities);
 		Mockito.verify(entityManager).createQuery(str);
 		Mockito.verify(query).getResultList();
 		Mockito.verify(query).setParameter("id", 1L);
 		Mockito.verify(query).setMaxResults(10);
 	}
 
 	@Test
 	public void test_getSingleEntity() {
 		FooEntity foo = new FooEntity();
 		String str = "SELECT x FROM FooEntity x WHERE x.ID = 1";
 
 		Query query  = Mockito.mock(Query.class);
 		Mockito.when(entityManager.createQuery(str)).thenReturn(query);
 		Mockito.when(query.getSingleResult()).thenReturn(foo);
 
 		FooEntity result = dao().getSingleEntity(str);
 		Assertions.assertThat(result).isNotNull().isEqualTo(foo);
 		Mockito.verify(entityManager).createQuery(str);
 		Mockito.verify(query).getSingleResult();
 		Mockito.verify(query, Mockito.never()).setParameter(Mockito.anyString(), Mockito.anyObject());
 	}
 
 	@Test
 	public void test_getSingleEntity_withParameters() {
 		FooEntity foo = new FooEntity();
 
 		Map<String, Object> parameters = new HashMap<String, Object>();
 		parameters.put("id", 1L);
 		String str = "SELECT x FROM FooEntity x WHERE x.ID = :id";
 
 		Query query  = Mockito.mock(Query.class);
 		Mockito.when(entityManager.createQuery(str)).thenReturn(query);
 		Mockito.when(query.getSingleResult()).thenReturn(foo);
 
 		FooEntity result = dao().getSingleEntity(str, parameters);
 		Assertions.assertThat(result).isNotNull().isEqualTo(foo);
 		Mockito.verify(entityManager).createQuery(str);
 		Mockito.verify(query).getSingleResult();
 		Mockito.verify(query).setParameter("id", 1L);
 	}
 
 	@Test
 	public void test_getCount() {
 		String str = "SELECT COUNT(x) FROM FooEntity x WHERE x.ID = 1";
 
 		Query query  = Mockito.mock(Query.class);
 		Mockito.when(entityManager.createQuery(str)).thenReturn(query);
 		Mockito.when(query.getSingleResult()).thenReturn(2L);
 
 		long result = dao().getCount(str);
 		Assertions.assertThat(result).isEqualTo(2);
 		Mockito.verify(entityManager).createQuery(str);
 		Mockito.verify(query).getSingleResult();
 		Mockito.verify(query, Mockito.never()).setParameter(Mockito.anyString(), Mockito.anyObject());
 	}
 
 	@Test
 	public void test_getCount_withParameter() {
 		Map<String, Object> parameters = new HashMap<String, Object>();
 		parameters.put("id", 1L);
 		String str = "SELECT COUNT(x) FROM FooEntity x WHERE x.ID = :id";
 
 		Query query  = Mockito.mock(Query.class);
 		Mockito.when(entityManager.createQuery(str)).thenReturn(query);
 		Mockito.when(query.getSingleResult()).thenReturn(2L);
 
 		long result = dao().getCount(str, parameters);
 		Assertions.assertThat(result).isEqualTo(2);
 		Mockito.verify(entityManager).createQuery(str);
 		Mockito.verify(query).getSingleResult();
 		Mockito.verify(query).setParameter("id", 1L);
 	}
 
 	private AbstractGenericDao<FooEntity> dao() {
 		return new AbstractGenericDao<FooEntity>() {
 			@Override
 			protected EntityManager entityManager() {
 				return entityManager;
 			}
 		};
 	}
 
 	@Test
 	public void test_getSingleEntity_noResultException() {
 		FooEntity foo = new FooEntity();
 		String str = "SELECT x FROM FooEntity x WHERE x.ID = 1";
 
 		Query query  = Mockito.mock(Query.class);
 		Mockito.when(entityManager.createQuery(str)).thenReturn(query);
 		Mockito.when(query.getSingleResult()).thenThrow(NoResultException.class);
 
 		FooEntity result = dao().getSingleEntity(str);
 		Assertions.assertThat(result).isNull();
 		Mockito.verify(entityManager).createQuery(str);
 		Mockito.verify(query).getSingleResult();
 	}
 
 
 	private static class FooEntity extends AbstractGenericEntity {
 		@Override
 		public Long entityId() {
 			return 1L;
 		}
 	}
 }
