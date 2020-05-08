 package net.alpha01.jwtest.util;
 
 import java.math.BigInteger;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import net.alpha01.jwtest.beans.Attachment;
 import net.alpha01.jwtest.beans.Result;
 import net.alpha01.jwtest.beans.Step;
 import net.alpha01.jwtest.beans.TestCase;
 import net.alpha01.jwtest.dao.AttachmentMapper;
 import net.alpha01.jwtest.dao.AttachmentMapper.AttachmentAssociation;
 import net.alpha01.jwtest.dao.ResultMapper;
 import net.alpha01.jwtest.dao.SqlConnection;
 import net.alpha01.jwtest.dao.SqlSessionMapper;
 import net.alpha01.jwtest.dao.StepMapper;
 import net.alpha01.jwtest.dao.TestCaseMapper;
 import net.alpha01.jwtest.dao.TestCaseMapper.Dependency;
 import net.alpha01.jwtest.exceptions.JWTestException;
 
 import org.apache.ibatis.exceptions.PersistenceException;
 import org.apache.log4j.Logger;
 
 public class TestCaseUtil {
 
 	public static boolean isUsedTestCase(TestCase testCase) {
 		SqlSessionMapper<TestCaseMapper> sesMapper = SqlConnection.getSessionMapper(TestCaseMapper.class);
 		// Check if there is any result with this testcase
 		List<Result> tres = sesMapper.getSqlSession().getMapper(ResultMapper.class).getAllByTestCase(testCase.getId());
 		sesMapper.close();
		boolean allOpened=true;
		for  (Result r : tres){
			allOpened = allOpened && r.getSession().isOpened();
		}
		return !allOpened;
 	}
 
 	public static HashMap<BigInteger, BigInteger> updateTestCase(TestCase testCase, List<TestCase> dependencies) throws JWTestException {
 		SqlSessionMapper<TestCaseMapper> sesMapper = SqlConnection.getSessionMapper(TestCaseMapper.class);
 		if (!isUsedTestCase(testCase)) {
 			// there isn't any result associated to this testcase, so it's not
 			// necessary create a new testcase version
 			try {
 				if (sesMapper.getMapper().update(testCase).equals(1)) {
 					Logger.getLogger(TestCaseUtil.class).info("TestCase updated");
 					sesMapper.getMapper().deleteDependencies(testCase.getId());
 					Iterator<TestCase> itd = dependencies.iterator();
 					while (itd.hasNext()) {
 						TestCase dep = itd.next();
 						sesMapper.getMapper().addDependency(new Dependency(testCase.getId(), dep.getId()));
 					}
 					sesMapper.commit();
 					sesMapper.close();
 				} else {
 					sesMapper.rollback();
 					sesMapper.close();
 					throw new JWTestException(TestCaseUtil.class, "ERROR: TestCase not updated");
 				}
 			} catch (PersistenceException e) {
 				throw new JWTestException(TestCaseUtil.class, "ERROR: TestCase chiave duplicata");
 			}
 			return null;
 		} else {
 			HashMap<BigInteger, BigInteger> stepsId = new HashMap<BigInteger, BigInteger>();
 			// There is some result associated to this testCase, so i have to
 			// create a new testcase version
 			try {
 				StepMapper stepMapper = sesMapper.getSqlSession().getMapper(StepMapper.class);
 				AttachmentMapper attachMapper = sesMapper.getSqlSession().getMapper(AttachmentMapper.class);
 				TestCase origTestCase = sesMapper.getMapper().get(testCase.getId());
 				List<Step> tsteps = stepMapper.getAll(testCase.getId().intValue());
 				List<Attachment> tattachs = attachMapper.getByRequirement(testCase.getId());
 				testCase.setId(null);
 				if (sesMapper.getMapper().add(testCase).equals(1)) {
 					// set to previous testCase version the new testcase version
 					// id
 					origTestCase.setNew_version(testCase.getId());
 					if (sesMapper.getMapper().update(origTestCase).equals(1)) {
 						// cloning steps to new testCase
 						for (Step ts : tsteps) {
 							BigInteger origId = ts.getId();
 							ts.setId_testcase(testCase.getId());
 							ts.setId(null);
 							if (!stepMapper.add(ts).equals(1)) {
 								sesMapper.rollback();
 								sesMapper.close();
 								throw new JWTestException(TestCaseUtil.class, "ERROR cloning steps");
 							}
 							stepsId.put(origId, ts.getId());
 						}
 						// cloning attachments
 						for (Attachment atch : tattachs) {
 							if (!attachMapper.associateRequirement(new AttachmentAssociation(atch.getId(), testCase.getId())).equals(1)) {
 								sesMapper.rollback();
 								sesMapper.close();
 								throw new JWTestException(TestCaseUtil.class, "ERROR cloning attachments association");
 							}
 						}
 					} else {
 						sesMapper.rollback();
 						sesMapper.close();
 						throw new JWTestException(TestCaseUtil.class, "ERROR updating  Step");
 					}
 					Logger.getLogger(TestCaseUtil.class).info("TestCase updated");
 					Iterator<TestCase> itd = dependencies.iterator();
 					while (itd.hasNext()) {
 						TestCase dep = itd.next();
 						sesMapper.getMapper().addDependency(new Dependency(testCase.getId(), dep.getId()));
 					}
 					sesMapper.commit();
 					sesMapper.close();
 					return stepsId;
 				} else {
 					sesMapper.rollback();
 					sesMapper.close();
 					throw new JWTestException(TestCaseUtil.class, "ERROR: TestCase not updated");
 				}
 			} catch (PersistenceException e) {
 				throw new JWTestException(TestCaseUtil.class, "ERROR: TestCase chiave duplicata");
 			}
 		}
 	}
 
 	public static void deleteTestCase(TestCase testCase) throws JWTestException {
 		SqlSessionMapper<TestCaseMapper> sesDelTestMapper = SqlConnection.getSessionMapper(TestCaseMapper.class);
 		if (isUsedTestCase(testCase)) {
 			testCase.setNew_version(BigInteger.valueOf(-1));
 			if (sesDelTestMapper.getMapper().update(testCase).equals(1)) {
 				Logger.getLogger(TestCaseUtil.class).info("Testcase update to -1");
 				sesDelTestMapper.commit();
 				sesDelTestMapper.close();
 			} else {
 				sesDelTestMapper.rollback();
 				sesDelTestMapper.close();
 				throw new JWTestException(TestCaseUtil.class, "ERROR: Testcase not updated to -1 SQL ERROR");
 			}
 		} else {
 			if (sesDelTestMapper.getMapper().delete(testCase).equals(1)) {
 				Logger.getLogger(TestCaseUtil.class).info("Testcase deleted");
 				sesDelTestMapper.commit();
 				sesDelTestMapper.close();
 			} else {
 				sesDelTestMapper.rollback();
 				sesDelTestMapper.close();
 				throw new JWTestException(TestCaseUtil.class, "ERROR: Testcase not deleted SQL ERROR");
 			}
 		}
 		JWTestUtil.cleanDeadElements();
 	}
 	
 	public static void cleanTestCase(){
 		SqlSessionMapper<TestCaseMapper> sesDelTestMapper = SqlConnection.getSessionMapper(TestCaseMapper.class);
 		sesDelTestMapper.getMapper().cleanTestCase();
 		sesDelTestMapper.commit();
 		sesDelTestMapper.close();
 	}
 }
