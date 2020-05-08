 package hu.advancedweb.service.impl;
 
 import hu.advancedweb.lms.evaluation.DefaultExamEvaluatorLogic;
 import hu.advancedweb.lms.evaluation.ExamTest;
 import hu.advancedweb.model.ExamConfig;
 import hu.advancedweb.service.ExamConfigLocalServiceUtil;
 import hu.advancedweb.service.base.ExamConfigLocalServiceBaseImpl;
 
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.javatuples.Pair;
 import org.json.simple.JSONObject;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Preconditions;
 import com.liferay.portal.kernel.exception.PortalException;
 import com.liferay.portal.kernel.exception.SystemException;
 
 /**
  * The implementation of the exam config local service.
  *
  * <p>
  * All custom service methods should be put in this class. Whenever methods are added, rerun ServiceBuilder to copy their definitions into the {@link hu.advancedweb.service.ExamConfigLocalService} interface.
  *
  * <p>
  * This is a local service. Methods of this service will not have security checks based on the propagated JAAS credentials because this service can only be accessed from within the same VM.
  * </p>
  *
  * @author Brian Wing Shun Chan
  * @see hu.advancedweb.service.base.ExamConfigLocalServiceBaseImpl
  * @see hu.advancedweb.service.ExamConfigLocalServiceUtil
  */
 public class ExamConfigLocalServiceImpl extends ExamConfigLocalServiceBaseImpl {
     /*
      * NOTE FOR DEVELOPERS:
      *
      * Never reference this interface directly. Always use {@link hu.advancedweb.service.ExamConfigLocalServiceUtil} to access the exam config local service.
      */
 
 	public ExamConfig createExamConfig(long companyId, long groupId, String questions, String evaluator) throws SystemException {
 		ExamConfig result = createExamConfig(counterLocalService.increment());
 		result.setCompanyId(companyId);
 		result.setGroupId(groupId);
 		result.setQuestions(questions);
 		result.setEvaluator(evaluator);
 		result = updateExamConfig(result);
 		examConfigPersistence.clearCache();// TODO:workaround
 		return result;
 
 	}
 
 	public ExamConfig createExamConfig(long companyId, long groupId, ExamTest test, Optional<String> evaluator, Optional<DefaultExamEvaluatorLogic> evaluatorLogic) throws SystemException {
 		Preconditions.checkArgument(evaluator.isPresent() && evaluatorLogic.isPresent() == false || evaluator.isPresent() == false && evaluatorLogic.isPresent(), "Evaluator must be present iff evaluatorlogic is absent");
 
 		String evaluatorString = evaluator.isPresent() ? evaluator.get() : generateDefaultEvaluatorJavascript(evaluatorLogic.get());
 
 		String questions = JSONObject.toJSONString(test.tests);
 
		return ExamConfigLocalServiceUtil.createExamConfig(companyId, groupId, questions, evaluatorString);
 	}
 
 	private String generateDefaultEvaluatorJavascript(DefaultExamEvaluatorLogic logic) {
 		StringBuilder result = new StringBuilder();
 
 		result.append("/* - Generated Evaluator Logic - */");
 		result.append(System.getProperty("line.separator"));
 		result.append("function validate(){");
 		result.append(System.getProperty("line.separator"));
 		for (Entry<String, Map<String, Pair<String, Integer>>> pages : logic.correctAnswers.entrySet()) {
 			for (Entry<String, Pair<String, Integer>> exercises : pages.getValue().entrySet()) {
 				result.append("    defaultEvaluatorLogic.addCorrectAnswer('" + pages.getKey() + "', '" + exercises.getKey() + "', '" + exercises.getValue().getValue0() + "', " + exercises.getValue().getValue1() + ");");
 				result.append(System.getProperty("line.separator"));
 			}
 		}
 		result.append(System.getProperty("line.separator"));
 		result.append("    defaultEvaluator.evaluate(examAnswers,defaultEvaluatorLogic,validationDataObj);");
 		result.append(System.getProperty("line.separator"));
 		result.append("}");
 
 		return result.toString();
 	}
 
 	/** Updates the config */
 	public ExamConfig updateExamConfig(long id, ExamTest test, Optional<String> evaluator, Optional<DefaultExamEvaluatorLogic> evaluatorLogic) throws PortalException, SystemException {
 		Preconditions.checkArgument(evaluator.isPresent() && evaluatorLogic.isPresent() == false || evaluator.isPresent() == false && evaluatorLogic.isPresent(), "Evaluator must be present iff evaluatorlogic is absent");
 
 		ExamConfig config = ExamConfigLocalServiceUtil.getExamConfig(id);
 
 		String evaluatorString = evaluator.isPresent() ? evaluator.get() : generateDefaultEvaluatorJavascript(evaluatorLogic.get());
 
 		String questions = JSONObject.toJSONString(test.tests);
 
 		config.setEvaluator(evaluatorString);
 		config.setQuestions(questions);
 
		return ExamConfigLocalServiceUtil.updateExamConfig(config);
 	}
 
 }
