 package org.tcrun.slickij.data.dao;
 
 import org.tcrun.slickij.api.data.ResultStatus;
 import org.tcrun.slickij.api.data.TestRunSummary;
 import org.tcrun.slickij.api.data.dao.TestrunDAO;
 import org.tcrun.slickij.api.data.dao.ResultDAO;
 import com.google.code.morphia.Datastore;
 import com.google.code.morphia.dao.BasicDAO;
 import com.google.code.morphia.query.Query;
 import com.google.code.morphia.query.UpdateOperations;
 import com.google.code.morphia.query.UpdateResults;
 import com.google.inject.Inject;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import org.bson.BSONObject;
 import org.bson.types.ObjectId;
 import org.tcrun.slickij.api.data.LogEntry;
 import org.tcrun.slickij.api.data.Testrun;
 import org.tcrun.slickij.api.data.Result;
 import org.tcrun.slickij.api.data.RunStatus;
 import org.tcrun.slickij.api.data.StoredFile;
 import org.tcrun.slickij.api.data.Testplan;
 import org.tcrun.slickij.api.data.TestplanReference;
 import org.tcrun.slickij.api.data.TestrunReference;
 
 /**
  *
  * @author jcorbett
  */
 public class TestrunDAOImpl extends BasicDAO<Testrun, ObjectId> implements TestrunDAO
 {
 	private ResultDAO resdao = null;
 
 	@Inject
 	public TestrunDAOImpl(Datastore ds, ResultDAO resultDAO)
 	{
 		super(Testrun.class, ds);
 		resdao = resultDAO;
 	}
 
 	@Override
 	public Testrun findByTestrunReference(TestrunReference ref)
 	{
 		Testrun result = null;
 		if(ref.getTestrunObjectId() != null)
 		{
 			Query<Testrun> query = createQuery();
 			result = query.field("id").equal(ref.getTestrunObjectId()).get();
 		}
 		if(result == null && ref.getName() != null)
 		{
 			Query<Testrun> query = createQuery();
 			result = query.field("name").equal(ref.getName()).get();
 		}
 
 		if(result != null && ref.getTestrunObjectId() == null)
 			ref.setTestrunId(result.getObjectId());
 		if(result != null && ref.getName() == null)
 			ref.setName(result.getName());
 
 		return result;
 	}
 
 	@Override
 	public TestRunSummary getSummary(Testrun run)
 	{
 		// to understand what is going on here read:
 		//  - the group command: http://www.mongodb.org/display/DOCS/Aggregation
 		//  - this comment: http://www.mongodb.org/display/DOCS/Java+Tutorial#comment-134207880
 
 		TestplanDAOImpl tpdao = new TestplanDAOImpl(ds);
 		TestplanReference tpref = new TestplanReference();
 		if(run.getTestplanObjectId() != null)
 		{
 			Testplan plan = tpdao.get(run.getTestplanObjectId());
 			tpref = new TestplanReference(plan);
 		}
 
 		TestRunSummary summary = new TestRunSummary();
 		DBCollection resultsCol = ds.getCollection(Result.class);
 		BasicDBObject key = new BasicDBObject();
 		key.put("status", true);
 		BasicDBObject condition = new BasicDBObject();
 		condition.put("testrun.testrunId", run.getObjectId());
 		BasicDBObject initial = new BasicDBObject();
 		initial.put("count", 0);
 		String reduce = "function(obj, prev) { prev.count++; }";
 
 		DBObject resultsObject = resultsCol.group(key, condition, initial, reduce);
 		List<DBObject> results = (List<DBObject>)resultsObject;
         summary.setTotalTime(0);
 		for(DBObject statuscount : results)
 		{
 			summary.getResultsByStatus().put((String) statuscount.get("status"), new Long(String.format("%.0f", (Double) statuscount.get("count"))));
 		}
 
 		return summary;
 	}
 
 	@Override
 	public void rescheduleByStatus(ObjectId testrunid, ResultStatus status)
 	{
 			Query<Result> updateQuery = resdao.createQuery();
 			updateQuery.criteria("testrun.testrunId").equal(testrunid);
 			updateQuery.criteria("runstatus").equal(RunStatus.FINISHED);
 			updateQuery.criteria("status").equal(status);
 			UpdateOperations<Result> updateOps = ds.createUpdateOperations(Result.class);
 			updateOps.set("runstatus", RunStatus.TO_BE_RUN);
 			updateOps.set("status", ResultStatus.NO_RESULT);
 			updateOps.set("log", new ArrayList<LogEntry>());
 			updateOps.set("files", new ArrayList<StoredFile>());
 			updateOps.set("recorded", new Date());
 			updateOps.unset("hostname");
 			updateOps.unset("reason");
 			ds.update(updateQuery, updateOps);
 	}
 
     @Override
     public void addNewResultStatusToRun(ObjectId testrunid, ResultStatus status)
     {
         Query<Testrun> updateQuery = createQuery();
         updateQuery.criteria("id").equal(testrunid);
         UpdateOperations<Testrun> updateOps = ds.createUpdateOperations(Testrun.class);
         updateOps.inc("summary.resultsByStatus." + status.toString());
         ds.update(updateQuery, updateOps);
     }
 
     @Override
     public void changeResultStatus(ObjectId testrunid, ResultStatus old, ResultStatus newStatus)
     {
         Query<Testrun> updateQuery = createQuery();
         updateQuery.criteria("id").equal(testrunid);
         UpdateOperations<Testrun> updateOps = ds.createUpdateOperations(Testrun.class);
         updateOps.dec("summary.resultsByStatus." + old.toString());
         updateOps.inc("summary.resultsByStatus." + newStatus.toString());
         ds.update(updateQuery, updateOps);
     }
 
     @Override
     public void deleteResultStatusFromRun(ObjectId testrunid, ResultStatus status)
     {
         Query<Testrun> updateQuery = createQuery();
         updateQuery.criteria("id").equal(testrunid);
         UpdateOperations<Testrun> updateOps = ds.createUpdateOperations(Testrun.class);
         updateOps.dec("summary.resultsByStatus." + status.toString());
         ds.update(updateQuery, updateOps);
     }
 
     @Override
     public Testrun updateSummary(Testrun run)
     {
         run.setSummary(getSummary(run));
         save(run);
         return run;
     }
 }
