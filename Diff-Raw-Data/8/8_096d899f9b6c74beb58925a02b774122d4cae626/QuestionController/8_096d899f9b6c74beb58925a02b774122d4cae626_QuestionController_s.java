 package dk.aau.portal.mkr.qa.web;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.util.StringUtils;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.Errors;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import dk.aau.portal.mkr.qa.Question;
 import dk.aau.portal.mkr.qa.internal.QuestionRepository;
 
 @Controller
 public class QuestionController {
 
 	private QuestionRepository questionRepository;
 	
 	@Autowired
 	public QuestionController(QuestionRepository questionRepository){
 		this.questionRepository = questionRepository;
 	}
 	
 	@RequestMapping(value="/questions", method=RequestMethod.GET)
 	public String listQuestions(Model model){
 		model.addAttribute("questions", questionRepository.getAll());
 		return "listQuestions";
 	}
 	
 	@RequestMapping(value="/questions/new", method=RequestMethod.GET)
 	public String newQuestion(Model model){
 		model.addAttribute("question", new Question());
 		return "newQuestion";
 	}
 	
 	@RequestMapping(value="/questions", method=RequestMethod.POST)
 	public String createQuestion(Question question, BindingResult bindingResult){
 validateQuestion(question, bindingResult);
 		
 		if(bindingResult.hasErrors()){
 			return "newQuestion";
 		}
 		questionRepository.create(question);
 		
 		return "redirect:/questions/"+question.getId();
 	}
 	
 	@RequestMapping(value="/questions/{id}", method=RequestMethod.GET)
	public String showQuestion(@PathVariable int id, Model model){
 		model.addAttribute("question", questionRepository.findQuestion(id));
 		return "question";
 	}
 	
 	@RequestMapping(value="/questions/{id}", method=RequestMethod.POST)
 	public String updateQuestion(Question question, BindingResult bindingResult){
 		validateQuestion(question, bindingResult);
 		
 		if(bindingResult.hasErrors()){
 			return "question";
 		}
 		questionRepository.update(question);
 		
 		return "redirect:/questions/"+question.getId();
 		
 	}
 	
 	@RequestMapping(value="/questions/{id}/destroy", method=RequestMethod.POST)
	public String destroyQuestion(@PathVariable int id){
 		Question question = questionRepository.findQuestion(id);
 		questionRepository.destroy(question);
 		return "redirect:/questions";
 	}
 	
 	public void validateQuestion(Question question, Errors errors){
 		if(!StringUtils.hasText(question.getQuestion())){
 			errors.rejectValue("question", "empty.value");
 		}
 	}
 	
 	@RequestMapping(value="/questions/{id}/edit", method=RequestMethod.GET)
	public String editQuestion(@PathVariable int id, Model model){
 		model.addAttribute("question", questionRepository.findQuestion(id));
 		return "editQuestion";
 	}
 
 }
