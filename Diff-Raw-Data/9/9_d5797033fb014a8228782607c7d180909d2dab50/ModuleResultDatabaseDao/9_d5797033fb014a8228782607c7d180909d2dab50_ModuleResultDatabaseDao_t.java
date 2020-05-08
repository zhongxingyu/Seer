 package org.kalibro.core.persistence;
 
 import java.util.*;
 
 import javax.persistence.TypedQuery;
 
 import org.kalibro.Granularity;
 import org.kalibro.Module;
 import org.kalibro.ModuleResult;
 import org.kalibro.core.persistence.record.ModuleResultRecord;
 import org.kalibro.dao.ModuleResultDao;
 import org.kalibro.dto.DataTransferObject;
 
 /**
  * Database access implementation for {@link ModuleResultDao}.
  * 
  * @author Carlos Morais
  */
 public class ModuleResultDatabaseDao extends DatabaseDao<ModuleResult, ModuleResultRecord> implements ModuleResultDao {
 
 	ModuleResultDatabaseDao() {
 		super(ModuleResultRecord.class);
 	}
 
 	@Override
 	public SortedSet<ModuleResult> childrenOf(Long moduleResultId) {
 		TypedQuery<ModuleResultRecord> query = createRecordQuery("moduleResult.parent = :parentId");
 		query.setParameter("parentId", moduleResultId);
 		return DataTransferObject.toSortedSet(query.getResultList());
 	}
 
 	@Override
 	public SortedMap<Date, ModuleResult> historyOf(Long moduleResultId) {
 		String moduleName = ModuleResultRecord.persistedName(get(moduleResultId).getModule().getName());
 		String repositoryId = "(SELECT p.repository FROM ModuleResult mr, Processing p " +
 			"WHERE p.id = mr.processing AND mr.id = :moduleResultId)";
 		TypedQuery<Object[]> query = createQuery("SELECT processing.date, result " +
 			"FROM ModuleResult result, Processing processing WHERE processing.id = result.processing " +
 			"AND result.moduleName = :moduleName AND processing.repository = " + repositoryId, Object[].class);
 		query.setParameter("moduleName", moduleName);
		query.setParameter("moduleResultId", moduleResultId);
 		List<Object[]> results = query.getResultList();
 
 		SortedMap<Date, ModuleResult> history = new TreeMap<Date, ModuleResult>();
 		for (Object[] result : results)
 			history.put(new Date((Long) result[0]), ((ModuleResultRecord) result[1]).convert());
 		return history;
 	}
 
 	public ModuleResult save(ModuleResult moduleResult, Long processingId) {
 		return save(new ModuleResultRecord(moduleResult, processingId)).convert();
 	}
 
 	public ModuleResult prepareResultFor(Module module, Long processingId) {
 		ModuleResult moduleResult = findResultFor(module, processingId).convert();
 		moduleResult.getModule().setGranularity(module.getGranularity());
 		return moduleResult;
 	}
 
 	private ModuleResultRecord findResultFor(Module module, Long processingId) {
 		if (!exists("WHERE " + moduleCondition(module),
 			"processingId", processingId, "module", moduleParameter(module))) {
 			ModuleResultRecord parent = findParentOf(module, processingId);
 			save(new ModuleResultRecord(module, parent == null ? null : parent.id(), processingId));
 		}
 		return getResultFor(module, processingId);
 	}
 
 	private ModuleResultRecord findParentOf(Module module, Long processingId) {
 		if (module.getGranularity() == Granularity.SOFTWARE)
 			return null;
 		return findResultFor(module.inferParent(), processingId);
 	}
 
 	private ModuleResultRecord getResultFor(Module module, Long processingId) {
 		TypedQuery<ModuleResultRecord> query = createRecordQuery(moduleCondition(module));
 		query.setParameter("processingId", processingId);
 		query.setParameter("module", moduleParameter(module));
 		return query.getSingleResult();
 	}
 
 	private String moduleCondition(Module module) {
 		String attribute = (module.getGranularity() == Granularity.SOFTWARE) ? "Granularity" : "Name";
 		return "moduleResult.processing = :processingId AND moduleResult.module" + attribute + " = :module";
 	}
 
 	private Object moduleParameter(Module module) {
 		if (module.getGranularity() == Granularity.SOFTWARE)
 			return Granularity.SOFTWARE.name();
 		return ModuleResultRecord.persistedName(module.getName());
 	}
 }
