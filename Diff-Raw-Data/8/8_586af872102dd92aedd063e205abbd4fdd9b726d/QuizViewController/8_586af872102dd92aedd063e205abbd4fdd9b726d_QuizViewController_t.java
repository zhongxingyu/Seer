 package com.blogspot.jmelon.portlet.quiz.controller;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.portlet.PortletPreferences;
 import javax.portlet.ReadOnlyException;
 import javax.portlet.RenderRequest;
 import javax.portlet.RenderResponse;
 import javax.portlet.ResourceRequest;
 import javax.portlet.ResourceResponse;
 
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.util.StringUtils;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.portlet.ModelAndView;
 import org.springframework.web.portlet.bind.annotation.ResourceMapping;
 
 import com.blogspot.jmelon.portlet.quiz.model.QuizPrefs;
 import com.blogspot.jmelon.portlet.quiz.model.QuizQuestion;
 import com.blogspot.jmelon.portlet.quiz.model.QuizResult;
 import com.blogspot.jmelon.portlet.quiz.model.utilities.QuizResultsComparator;
 import com.liferay.portal.kernel.util.Constants;
 import com.liferay.portal.kernel.util.WebKeys;
 import com.liferay.portal.theme.ThemeDisplay;
 import com.liferay.portlet.journalcontent.util.JournalContentUtil;
 
 /**
  * See the file "LICENSE" for the full license governing this code.
  * 
  * @author <a href="jmelon.blogspot.com">Michał Mela</a>
  */
 @Controller
 @RequestMapping("VIEW")
 public class QuizViewController {
 
 	private static final String PARAM_ANSWERS = "answers";
 	
 	private static final String JSON_VIEW = "jsonView";
 	private static final String VIEW_JSP = "quiz/view";
 	
 	private static final String ATTR_CONTENT = "content";
 	private static final String ATTR_RESOLUTION = "resolution";
 	private static final String RESOLUTION_SUCCESS = "success";
 	private static final String RESOLUTION_FAILED = "failed";
 	
 	private static final Logger LOGGER = LoggerFactory.getLogger(QuizViewController.class);
 	// private static final String JSON_VIEW = "jsonView";
 
 	private static final String BLANK = "";
 
 	private static final String QUIZ_PREFS = "quizPrefs";
 
 	@RequestMapping
 	public ModelAndView view(RenderRequest request, RenderResponse response, ModelAndView mav)
 	        throws JsonParseException, JsonMappingException, IOException {
 		PortletPreferences portletPrefs = request.getPreferences();
 		String prefsJson = portletPrefs.getValue(QUIZ_PREFS, BLANK);
 
 		LOGGER.debug("Saved portlet preferences: {}", prefsJson);
 
 		ObjectMapper om = new ObjectMapper();
 		QuizPrefs quizPrefs = om.readValue(prefsJson, QuizPrefs.class);
 
 		mav.addObject("prefs", quizPrefs);
 		mav.setView(VIEW_JSP);
 		return mav;
 	}
 
 	@ResourceMapping("submitQuiz")
 	public String submitQuiz(ResourceRequest request, ResourceResponse response, Model model,
 	        @RequestParam(PARAM_ANSWERS) String answersParam) throws UnsupportedEncodingException, IOException,
 	        ReadOnlyException {
 		LOGGER.debug("received ansers: {}", answersParam);
 
 		if (StringUtils.hasText(answersParam)) {
 
 			List<Integer> answers = new LinkedList<Integer>();
 			for (String answer : answersParam.split(",")) {
 				answers.add(Integer.parseInt(answer));
 			}
 
 			String resolution = RESOLUTION_SUCCESS;
 
 			PortletPreferences portletPrefs = request.getPreferences();
 			String prefsJson = portletPrefs.getValue(QUIZ_PREFS, BLANK);
 
 			ObjectMapper om = new ObjectMapper();
 			QuizPrefs quizPrefs = om.readValue(prefsJson, QuizPrefs.class);
 
 			// count points
 			int points = 0;
 			final List<QuizQuestion> allQuestions = quizPrefs.getAllQuestions();
 			for (int i = 0; i < allQuestions.size(); i++) {
 				final QuizQuestion quizQuestion = allQuestions.get(i);
 				final Integer answerIndex = answers.get(i);
 
 				if (quizQuestion.getAnswers().size() > answerIndex) {
 					points += quizQuestion.getAnswers().get(answerIndex).getPoints();
 				} else {
 					resolution = RESOLUTION_FAILED;
 					break;
 				}
 			}
 
 			// get result content
 			if (RESOLUTION_SUCCESS.equals(resolution)) {
 				// TODO assure sorting while saving prefs
 				List<QuizResult> allResults = quizPrefs.getResults();
 				Collections.sort(allResults, new QuizResultsComparator());
 
 				QuizResult result = getFinalResultFromPoints(points, allResults);
 				ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
 
 				long groupId = getGroupId(result, themeDisplay);
 				String articleId = result.getResultArticleId();
 				String templateId = result.getResultTemplateId();
 				String language = request.getLocale().getLanguage();
 				String content;
 				if (templateId == null) {
 					content = JournalContentUtil.getContent(groupId, articleId, Constants.VIEW, language, themeDisplay);
 				} else {
 					content = JournalContentUtil.getContent(groupId, articleId, templateId, Constants.VIEW, language,
 					        themeDisplay);
 				}
 
 				model.addAttribute(ATTR_CONTENT, content);
 			}
 
 			model.addAttribute(ATTR_RESOLUTION, resolution);
 		} else {
 			LOGGER.warn("Received empty answers list");
 			model.addAttribute(ATTR_RESOLUTION, RESOLUTION_FAILED);
 		}
 		return JSON_VIEW;
 	}
 
 	private static long getGroupId(QuizResult result, ThemeDisplay themeDisplay) {
 		if ("SCOPE".equals(result.getResultGroup())) {
 			return themeDisplay.getScopeGroupId();
 		} else if ("GLOBAL".equals(result.getResultGroup())) {
 			return themeDisplay.getCompanyGroupId();
 		} else {
 			throw new IllegalArgumentException("Result group is neither SCOPE or GLOBAL");
 		}
 	}
 
 	private QuizResult getFinalResultFromPoints(int points, List<QuizResult> allResults) {
 		QuizResult result = null;
 		if (allResults.size() == 1) {
 			result = allResults.get(0);
 		} else {
 			// we assume results are sorted by their lower bound
 			for (int i = 1; i < allResults.size(); i++) {
 				if (allResults.get(i).getBound() > points) {
 					result = allResults.get(i - 1);
					break;
 				}
 			}
 			if (result == null) {
 				result = allResults.get(allResults.size() - 1);
 			}
 		}
 		return result;
 	}
 
 }
