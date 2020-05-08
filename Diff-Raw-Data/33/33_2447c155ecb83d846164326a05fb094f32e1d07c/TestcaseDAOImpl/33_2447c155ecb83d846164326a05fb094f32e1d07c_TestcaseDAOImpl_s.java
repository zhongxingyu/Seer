 package org.tcrun.slickij.data.dao;
 
 import org.tcrun.slickij.api.data.dao.TestcaseDAO;
 import com.google.code.morphia.Datastore;
 import com.google.code.morphia.dao.BasicDAO;
 import com.google.code.morphia.query.Query;
 import com.google.inject.Inject;
 import java.util.List;
 import org.bson.types.ObjectId;
 import org.tcrun.slickij.api.data.Testcase;
 import org.tcrun.slickij.api.data.TestcaseReference;
 import org.tcrun.slickij.api.data.testqueries.TestcaseQuery;
 
 /**
  *
  * @author jcorbett
  */
 public class TestcaseDAOImpl extends BasicDAO<Testcase, ObjectId> implements TestcaseDAO
 {
 	@Inject
 	public TestcaseDAOImpl(Datastore ds)
 	{
 		super(Testcase.class, ds);
 	}
 
 	@Override
 	public List<Testcase> findTestsByTestcaseQuery(TestcaseQuery query)
 	{
 		return findTestsByTestcaseQuery(query, false);
 	}
 
 	@Override
 	public List<Testcase> findTestsByTestcaseQuery(TestcaseQuery query, boolean includeDeleted)
 	{
 		Query<Testcase> testquery = createQuery();
 
 		if(includeDeleted)
 		{
 			query.toMorphiaQuery(testquery);
 		} else
 		{
 			testquery.and(testquery.criteria("deleted").equal(Boolean.FALSE), query.toMorphiaQuery(testquery));
 		}
 		return testquery.asList();
 	}
 
 	@Override
 	public long countTestsFromTestcaseQuery(TestcaseQuery query)
 	{
 		Query<Testcase> testquery = createQuery();
 		testquery.and(testquery.criteria("deleted").equal(Boolean.FALSE), query.toMorphiaQuery(testquery));
 		return testquery.countAll();
 	}
 
 	@Override
 	public Testcase findTestcaseByReference(TestcaseReference ref)
 	{
 		Testcase retval = null;
 		if(ref.getTestcaseId() != null)
 			retval = get(ref.getActualId());
 		if(retval == null && ref.getAutomationKey() != null)
 			retval = createQuery().field("automationKey").equal(ref.getAutomationKey()).get();
 		if(retval == null && ref.getAutomationId() != null)
 			retval = createQuery().field("automationId").equal(ref.getAutomationId()).get();
 		if(retval != null)
 		{
 			ref.setActualId(retval.getObjectId());
 			ref.setName(retval.getName());
 			ref.setAutomationTool(retval.getAutomationTool());
 			ref.setAutomationId(retval.getAutomationId());
 			ref.setAutomationKey(retval.getAutomationKey());
 		}
 		return retval;
 	}
 }
