 package org.kalibro.core.persistence;
 
 import java.util.*;
 
 import javax.persistence.TypedQuery;
 
 import org.kalibro.MetricResult;
 import org.kalibro.ModuleResult;
 import org.kalibro.core.persistence.record.DescendantResultRecord;
 import org.kalibro.core.persistence.record.MetricResultRecord;
 import org.kalibro.core.persistence.record.ModuleResultRecord;
 import org.kalibro.dao.MetricResultDao;
 import org.kalibro.dto.DataTransferObject;
 
 /**
  * Database access implementation for {@link MetricResultDao}.
  * 
  * @author Carlos Morais
  */
 public class MetricResultDatabaseDao extends DatabaseDao<MetricResult, MetricResultRecord> implements MetricResultDao {
 
 	MetricResultDatabaseDao() {
 		super(MetricResultRecord.class);
 	}
 
 	@Override
 	public List<Double> descendantResultsOf(Long metricResultId) {
 		String moduleResult = "(SELECT mr.moduleResult FROM MetricResult mr WHERE mr.id = :metricResultId)";
 		String configuration = "(SELECT mr.configuration.id FROM MetricResult mr WHERE mr.id = :metricResultId)";
 		String queryString = "SELECT dr FROM DescendantResult dr " +
 			"WHERE dr.moduleResult = " + moduleResult + " AND dr.configuration = " + configuration;
 		TypedQuery<DescendantResultRecord> query = createQuery(queryString, DescendantResultRecord.class);
 		query.setParameter("metricResultId", metricResultId);
 		return DataTransferObject.toList(query.getResultList());
 	}
 
 	@Override
 	public SortedSet<MetricResult> metricResultsOf(Long moduleResultId) {
 		TypedQuery<MetricResultRecord> query = createRecordQuery("metricResult.moduleResult = :moduleResultId");
 		query.setParameter("moduleResultId", moduleResultId);
 		return DataTransferObject.toSortedSet(query.getResultList());
 	}
 
 	@Override
 	public SortedMap<Date, MetricResult> historyOf(String metricName, Long moduleResultId) {
 		ModuleResult moduleResult = new ModuleResultDatabaseDao().get(moduleResultId);
 		String moduleName = ModuleResultRecord.persistedName(moduleResult.getModule().getName());
 		String repositoryId = "(SELECT p.repository FROM ModuleResult mor, Processing p " +
 			"WHERE p.id = mor.processing AND mor.id = :moduleResultId)";
 		TypedQuery<Object[]> query = createQuery("SELECT processing.date, metricResult " +
 			"FROM MetricResult metricResult, ModuleResult moduleResult, Processing processing " +
 			"WHERE processing.id = moduleResult.processing AND moduleResult.id = metricResult.moduleResult " +
 			"AND metricResult.configuration.metricName = :metricName AND moduleResult.moduleName = :moduleName " +
 			"AND processing.repository = " + repositoryId, Object[].class);
 		query.setParameter("metricName", metricName);
 		query.setParameter("moduleName", moduleName);
 		query.setParameter("moduleResultId", moduleResultId);
 		List<Object[]> results = query.getResultList();
 
 		SortedMap<Date, MetricResult> history = new TreeMap<Date, MetricResult>();
 		for (Object[] result : results)
 			history.put(new Date((Long) result[0]), ((MetricResultRecord) result[1]).convert());
 		return history;
 	}
 
 	public MetricResult save(MetricResult metricResult, Long moduleResultId) {
 		return save(new MetricResultRecord(metricResult, moduleResultId)).convert();
 	}
 
 	public boolean metricResultExists(Long moduleResultId, Long configurationId) {
		String queryString = "SELECT count(mr) FROM MetricResult mr " +
 			"WHERE mr.moduleResult = :moduleResultId AND mr.configuration.id = :configurationId";
		TypedQuery<Long> query = createQuery(queryString, Long.class);
 		query.setParameter("moduleResultId", moduleResultId);
 		query.setParameter("configurationId", configurationId);
 		return query.getSingleResult() > 0;
 	}
 
 	public void addDescendantResults(List<Double> descendantResults, Long moduleResultId, Long configurationId) {
 		List<DescendantResultRecord> records = new ArrayList<DescendantResultRecord>();
 		for (Double descendantResult : descendantResults)
 			records.add(new DescendantResultRecord(descendantResult, moduleResultId, configurationId));
 		saveAll(records);
 	}
 }
