 package org.tcrun.slickij.data;
 
 import com.google.code.morphia.query.Query;
 import com.google.inject.Inject;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriInfo;
 import org.bson.types.ObjectId;
 import org.tcrun.slickij.api.TestplanResource;
 import org.tcrun.slickij.api.data.*;
 import org.tcrun.slickij.api.data.testqueries.NamedTestcaseQuery;
 import org.tcrun.slickij.api.data.dao.ConfigurationDAO;
 import org.tcrun.slickij.api.data.dao.ProjectDAO;
 import org.tcrun.slickij.api.data.dao.ResultDAO;
 import org.tcrun.slickij.api.data.dao.TestcaseDAO;
 import org.tcrun.slickij.api.data.dao.TestplanDAO;
 import org.tcrun.slickij.api.data.dao.TestrunDAO;
 
 /**
  *
  * @author jcorbett
  */
 public class TestplanResourceImpl implements TestplanResource
 {
 	TestplanDAO m_testplanDAO;
 	TestcaseDAO m_testcaseDAO;
 	TestrunDAO m_testrunDAO;
 	ProjectDAO m_projectDAO;
 	ResultDAO m_resultDAO;
 	ConfigurationDAO m_configDAO;
 
 	@Inject
 	public TestplanResourceImpl(TestplanDAO p_testplanDAO, ProjectDAO p_projectDAO, TestcaseDAO p_testcaseDAO, TestrunDAO p_testrunDAO, ResultDAO p_resultDAO, ConfigurationDAO p_configDAO)
 	{
 		m_testplanDAO = p_testplanDAO;
 		m_projectDAO = p_projectDAO;
 		m_testcaseDAO = p_testcaseDAO;
 		m_testrunDAO = p_testrunDAO;
 		m_resultDAO = p_resultDAO;
 		m_configDAO = p_configDAO;
 	}
 
 	@Override
 	public List<Testplan> getTestPlans(UriInfo uriInfo)
 	{
 		MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
 		if(params.containsKey("username") && params.containsKey("projectid"))
 			return getTestPlans(params.getFirst("projectid"), uriInfo.getQueryParameters().getFirst("username"));
 		Query<Testplan> query = m_testplanDAO.createQuery();
         if(params.containsKey("projectid"))
         {
             try
             {
                 query.criteria("project.id").equal(new ObjectId(params.getFirst("projectid")));
             } catch(RuntimeException ex)
             {
                 throw new WebApplicationException(ex, Status.BAD_REQUEST);
             }
         }
 		if(params.containsKey("createdby"))
 		{
 			query.criteria("createdBy").equal(params.getFirst("createdby"));
 		}
         if(params.containsKey("name"))
         {
             query.criteria("name").equal(params.getFirst("name"));
         }
 		return query.asList();
 	}
 
 	public List<Testplan> getTestPlans(String projectid, String username)
 	{
 		ObjectId projectId = null;
 		try
 		{
 			projectId = new ObjectId(projectid);
 		} catch(RuntimeException ex)
 		{
 			throw new WebApplicationException(ex, Status.BAD_REQUEST);
 		}
 		return m_testplanDAO.getAllTestplansForUser(projectId, username);
 	}
 
 	@Override
 	public Testplan getTestPlan(String testplanId)
 	{
 		Testplan plan = m_testplanDAO.get(new ObjectId(testplanId));
 		if(plan == null)
 			throw new WebApplicationException(new Exception("Cannot find testplan with id " + testplanId), Status.NOT_FOUND);
 		return plan;
 	}
 
 	@Override
 	public Testplan addNewTestplan(Testplan testplan)
 	{
 		try
 		{
 			testplan.validate();
 		} catch(InvalidDataError error)
 		{
 			throw new WebApplicationException(error, Status.BAD_REQUEST);
 		}
 		Project project = m_projectDAO.findByReference(testplan.getProject());
 		if(project == null)
 			throw new WebApplicationException(new Exception("Cannot find project."), Status.BAD_REQUEST);
 		m_testplanDAO.save(testplan);
 		return testplan;
 	}
 
 	@Override
 	public Testplan updateTestplan(String testplanId, Testplan update)
 	{
 		Testplan plan = getTestPlan(testplanId);
 
 		if(update.getName() != null && !update.getName().equals("") && !update.getName().equals(plan.getName()))
 			plan.setName(update.getName());
 		if(update.getCreatedBy() != null && !update.getCreatedBy().equals("") && !update.getCreatedBy().equals(plan.getCreatedBy()))
 			plan.setCreatedBy(update.getCreatedBy());
 		if(update.getIsprivate() != null && !update.getIsprivate().equals(plan.getIsprivate()))
 			plan.setIsprivate(update.getIsprivate());
 		if(update.getProject() != null)
 		{
 			Project project = m_projectDAO.findByReference(update.getProject());
 			if(project != null)
 				plan.setProject(update.getProject());
 			else
 				throw new WebApplicationException(new Exception("Unable to find project with id '" + update.getProject().getId() + "' and name '" + update.getProject().getName() + "'"), Status.BAD_REQUEST);
 		}
 		if(update.getSharedWith() != null)
 			plan.setSharedWith(update.getSharedWith());
 		if(update.getQueries() != null)
 			plan.setQueries(update.getQueries());
 
 		m_testplanDAO.save(plan);
 		return plan;
 	}
 
 	@Override
 	public Testrun runTestPlan(String testplanId)
 	{
 		Testplan plan = getTestPlan(testplanId);
 		List<Testcase> testcases = getTestcases(plan);
 		Testrun run = new Testrun();
 		run.setProject(plan.getProject());
 		run.setTestplanId(plan.getObjectId());
 		run.setName("Testrun for testplan " + plan.getName());
 		run.setDateCreated(new Date());
 		Project project = m_projectDAO.findByReference(plan.getProject());
 		if(project != null)
 		{
 			Release release = project.findRelease(project.getDefaultRelease());
 			if(release != null)
 			{
 				run.setRelease(release.createReference());
 				Build build = release.findBuild(release.getDefaultBuild());
 				if(build != null)
 					run.setBuild(build.createReference());
 			}
 		}
 		m_testrunDAO.save(run);
 		for(Testcase test : testcases)
 		{
 			Result result = new Result();
 			result.setTestcase(test.createReference());
 			result.setProject(test.getProject());
 			result.setStatus(ResultStatus.NO_RESULT);
 			result.setRunstatus(RunStatus.TO_BE_RUN);
 			result.setComponent(test.getComponent());
 			result.setTestrun(run.createReference());
 			result.setRelease(run.getRelease());
 			result.setBuild(run.getBuild());
            result.setRecorded(new Date());
            result.setHistory(m_resultDAO.getHistory(result));
 			m_resultDAO.save(result);
             m_testrunDAO.addNewResultStatusToRun(run.getObjectId(), ResultStatus.NO_RESULT);
 		}
 		return run;
 	}
 
 	@Override
 	public Testrun runTestPlan(String testplanId, TestplanRunParameters parameters)
 	{
 		Testplan plan = getTestPlan(testplanId);
 		List<Testcase> testcases = getTestcases(plan);
 		Date date = new Date();
 		Testrun run = new Testrun();
 		run.setProject(plan.getProject());
 		run.setTestplanId(plan.getObjectId());
 		run.setName("Testrun for testplan " + plan.getName());
 		run.setDateCreated(date);
 		if(parameters.getConfig() != null)
 		{
 			Configuration config = m_configDAO.findConfigurationByReference(parameters.getConfig());
 			if(config != null)
 				run.setConfig(parameters.getConfig());
 			else
 				throw new NotFoundError(Configuration.class);
 		}
 		if(parameters.getRuntimeOptions() != null)
 		{
 			Configuration config = m_configDAO.findConfigurationByReference(parameters.getRuntimeOptions());
 			if(config != null)
 			{
 				run.setRuntimeOptions(parameters.getRuntimeOptions());
 				Boolean isRequirement = false;
 				for(Map.Entry<String, String> entry : config.getConfigurationData().entrySet())
 				{
 					String key = entry.getKey();
 					String value = entry.getValue();
 					//add the config as a configuration override
 					ConfigurationOverride override = new  ConfigurationOverride();
 					override.setKey(key);
 					override.setValue(value);
 					override.setIsRequirement(isRequirement); //isRequirement is false
 					//setting the parameters' overrides: these will be set in the results as well
 					if(parameters.getOverrides() == null)
 						parameters.setOverrides(new ArrayList<ConfigurationOverride>());
 					parameters.getOverrides().add(override);
 				}
 				
 			}
 			else
 				throw new NotFoundError(Configuration.class);
 		}
 		Project project = m_projectDAO.findByReference(plan.getProject());
 		if(project != null)
 		{
 			if(parameters.getRelease() != null)
 			{
 				Release release = project.findReleaseByReference(parameters.getRelease());
 				if(release != null)
 				{
 					run.setRelease(parameters.getRelease());
 					if(parameters.getBuild() != null)
 					{
 						Build build = release.findBuildByReference(parameters.getBuild());
 						if(build != null)
 							run.setBuild(parameters.getBuild());
 					}
 				}
 			} else
             {
                 // try to get the default release and the default build
                 if(project.getDefaultRelease() != null && !project.getDefaultRelease().isEmpty())
                 {
                     Release release = project.findRelease(project.getDefaultRelease());
                     if(release != null)
                     {
                         run.setRelease(release.createReference());
                         if(release.getDefaultBuild() != null && !release.getDefaultBuild().isEmpty())
                         {
                             Build build = release.findBuild(release.getDefaultBuild());
                             if(build != null)
                             {
                                 run.setBuild(build.createReference());
                             }
                         }
                     }
                 }
             }
 		}
 		m_testrunDAO.save(run);
 		for(Testcase test : testcases)
 		{
 			Result result = new Result();
 			result.setTestcase(test.createReference());
 			if(parameters.getOverrides() != null)
 			{
 				for(ConfigurationOverride override : parameters.getOverrides())
 				{
 					//for(DataDrivenPropertyType ddtype : test.getDataDriven())
 					//{
 						//if(override.getKey().equals(ddtype.getName()))
 						//{
 							if(result.getConfigurationOverride() == null)
 								result.setConfigurationOverride(new ArrayList<ConfigurationOverride>());
 							result.getConfigurationOverride().add(override);
 						//}
 					//}
 				}
 			}
 			if(parameters.getConfig() != null)
 				result.setConfig(parameters.getConfig());
 			result.setProject(test.getProject());
 			result.setStatus(ResultStatus.NO_RESULT);
 			result.setRunstatus(RunStatus.TO_BE_RUN);
 			result.setComponent(test.getComponent());
 			result.setTestrun(run.createReference());
 			result.setRelease(run.getRelease());
 			result.setBuild(run.getBuild());
            result.setRecorded(new Date());
            result.setHistory(m_resultDAO.getHistory(result));
 			m_resultDAO.save(result);
             m_testrunDAO.addNewResultStatusToRun(run.getObjectId(), ResultStatus.NO_RESULT);
 		}
 		return run;
 	}
 
 	protected List<Testcase> getTestcases(Testplan plan)
 	{
 		List<Testcase> tests = new ArrayList<Testcase>();
 		for(NamedTestcaseQuery query : plan.getQueries())
 			tests.addAll(m_testcaseDAO.findTestsByTestcaseQuery(query.getQuery()));
 		return tests;
 	}
 
 	@Override
 	public List<Testcase> getTestcases(String testplanId)
 	{
 		Testplan plan = getTestPlan(testplanId);
 		return getTestcases(plan);
 	}
 
     @Override
     public Integer getTestcaseCount(@PathParam("testplanid") String testplanId) {
         Testplan plan = getTestPlan(testplanId);
         Integer testcount = 0;
         for(NamedTestcaseQuery query : plan.getQueries())
             testcount += (int)m_testcaseDAO.countTestsFromTestcaseQuery(query.getQuery());
         return testcount;
     }
 
     @Override
 	public void deleteTestplan(String testplanId)
 	{
 		m_testplanDAO.deleteById(new ObjectId(testplanId));
 	}
 
 	@Override
 	public DataExtension<Testplan> addDataExtension(String testplanId, DataExtension<Testplan> dataExtension)
 	{
 		Testplan testplan = getTestPlan(testplanId);
 		try
 		{
 			dataExtension.validate();
 		} catch(InvalidDataError ex)
 		{
 			throw new WebApplicationException(ex, Status.BAD_REQUEST);
 		}
 		testplan.getExtensions().add(dataExtension);
 		m_testplanDAO.save(testplan);
 		return dataExtension;
 	}
 
 	protected DataExtension<Testplan> findDataExtension(Testplan testplan, String extensionId)
 	{
 		DataExtension<Testplan> extension = null;
 		for(DataExtension<Testplan> potential : testplan.getExtensions())
 		{
 			if(extensionId.equals(potential.getId()))
 			{
 				extension = potential;
 				break;
 			}
 		}
 
 		if(extension == null)
 			throw new WebApplicationException(new Exception("Cannot find extension with id " + extensionId), Status.NOT_FOUND);
 
 		return extension;
 	}
 
 	@Override
 	public DataExtension<Testplan> updateDataExtension(String testplanId, String extensionId, DataExtension<Testplan> dataExtension)
 	{
 		Testplan testplan = getTestPlan(testplanId);
 		DataExtension<Testplan> extension = findDataExtension(testplan, extensionId);
 		extension.update(dataExtension);
 		m_testplanDAO.save(testplan);
 		return extension;
 	}
 
 	@Override
 	public List<DataExtension<Testplan>> deleteDataExtension(String testplanId, String extensionId)
 	{
 		Testplan testplan = getTestPlan(testplanId);
 		DataExtension<Testplan> extension = findDataExtension(testplan, extensionId);
 		testplan.getExtensions().remove(extension);
 		m_testplanDAO.save(testplan);
 		return testplan.getExtensions();
 	}
 }
