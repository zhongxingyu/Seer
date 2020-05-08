 package br.usp.cata.web.controller;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 
 import br.com.caelum.vraptor.Get;
 import br.com.caelum.vraptor.Path;
 import br.com.caelum.vraptor.Post;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 import br.com.caelum.vraptor.Validator;
 import br.com.caelum.vraptor.validator.ValidationMessage;
 import br.usp.cata.model.PatternSuggestionElement;
 import br.usp.cata.model.Rule;
 import br.usp.cata.model.RuleCategories;
 import br.usp.cata.model.Source;
 import br.usp.cata.model.TypesOfRules;
 import br.usp.cata.model.TypesOfSources;
 import br.usp.cata.service.RuleService;
 import br.usp.cata.service.SourceService;
 import br.usp.cata.web.interceptor.Transactional;
 
 
 @Resource
 public class UserrulesController {
 	
 	private final Result result;
 	private final Validator validator;
 	private final RuleService ruleService;
 	private final SourceService sourceService;
 	private final UserSession userSession;
 	
 	public UserrulesController(final Result result, final Validator validator,
 		final RuleService ruleService, final SourceService sourceService, final UserSession userSession) {
 		this.result = result;
 		this.validator = validator;
 		this.ruleService = ruleService;
 		this.sourceService = sourceService;
 		this.userSession = userSession;
 	}
 	
 	@Get
 	@Path("/userrules/viewsource/{source.sourceID}")
 	public void viewsource(Source source) {
 		// TODO arrumar para quando não existe source com o id
 		
 		result.include("source", sourceService.findByID(source.getSourceID()));
 	}
 	
 	@Get
 	@Path("/userrules/newrule")
 	public void newrule() {
 		result.include("ruleCategories", RuleCategories.values());
 		result.include("typesOfRules", TypesOfRules.values());
 		result.include("typesOfSources", TypesOfSources.values());
 		result.include("academics", sourceService.findByType(TypesOfSources.ACADEMIC_PUBLISHING));
 		result.include("books", sourceService.findByType(TypesOfSources.BOOK));
 		result.include("handbooks", sourceService.findByType(TypesOfSources.HANDBOOK));
 		result.include("urls", sourceService.findByType(TypesOfSources.INTERNET));
 		result.include("others", sourceService.findByType(TypesOfSources.OTHER));
 	}
 	
 	private void validateRule(Rule newRule, List<PatternSuggestionElement> lemmas,
 			List<PatternSuggestionElement> exactMatchings, Source source) {
 		
 		PatternSuggestionElement lemma = newRule.getLemmaElement();
 		PatternSuggestionElement exactMatching = newRule.getExactMatchingElement();
 		
 		if(lemma.getPattern().equals("") && lemma.getSuggestion().equals("") &&
 				exactMatching.getPattern().equals("") && exactMatching.getSuggestion().equals(""))
 			validator.add(new ValidationMessage(
 					"Você deve cadastrar pelo menos um Lema ou uma Expressão exata.",
 					"Lema ou Expressão exata"));
 		else {
 			if(lemma.getPattern().equals("") != lemma.getSuggestion().equals(""))
 				validator.add(new ValidationMessage(
 						"Você deve cadastrar os dois campos - padrão incorreto e sugestão.",
 						"Lema"));
 			if(exactMatching.getPattern().equals("") != exactMatching.getSuggestion().equals(""))
 				validator.add(new ValidationMessage(
 						"Você deve cadastrar os dois campos - padrão incorreto e sugestão.",
 						"Expressão exata"));
 		}
 		
 		if(source.getSourceID() == null)
 			validator.add(new ValidationMessage(
     				"Você deve associar uma referência à regra.", "Referência"));
 	}
 	
 	@Post
 	@Path("userrules/newrule")
 	public void newrule(Rule newRule, List<PatternSuggestionElement> lemmas,
 			List<PatternSuggestionElement> exactMatchings, Source source) {
 		
 		validateRule(newRule, lemmas, exactMatchings, source);
 		validator.onErrorRedirectTo(UserrulesController.class).newrule();
 		
 		if(newRule.getExplanation().equals(""))
 			newRule.setExplanation(null);
 		
 		if(exactMatchings != null) {
 			HashSet<PatternSuggestionElement> exactMatchingElements = new HashSet<PatternSuggestionElement>();
 			for(PatternSuggestionElement exactMatching : exactMatchings)
 				if(!exactMatching.getPattern().equals("") && !exactMatching.getSuggestion().equals(""))
 					exactMatchingElements.add(exactMatching);
 			
 			if(exactMatchingElements.size() > 0)
 				newRule.setExactMatchingElements(exactMatchingElements);
 		}	
 		if(lemmas != null) {
 			HashSet<PatternSuggestionElement> lemmaElements = new HashSet<PatternSuggestionElement>();
 			for(PatternSuggestionElement lemma : lemmaElements)
 				if(!lemma.getPattern().equals("") && !lemma.getSuggestion().equals(""))
 					lemmaElements.add(lemma);
 			
 			if(lemmaElements.size() > 0)
 				newRule.setExactMatchingElements(lemmaElements);			
 		}
 		
 		newRule.setUser(userSession.getUser());
 		newRule.setDate(new Date());
 		//TODO Mudar para false: as novas regras não devem ser default do sistema
 		newRule.setDefaultRule(true);
 		
 		newRule.setSource(sourceService.findByID(source.getSourceID()));
 		
 		ruleService.save(newRule);
 		
 		result.include("messages", "A Regra foi cadastrada com sucesso.");
 		
 		result.redirectTo(IndexController.class).rules();
 	}
 	
 	@Get
 	@Path("/userrules/newsource")
 	public void newsource() {
 		result.include("typesOfSources", TypesOfSources.values());
 	}
 	
 	public void validateSource(Source source) {
 		switch(source.getType()) {
 			case ACADEMIC_PUBLISHING:
 				if(source.getTitle().equals(""))
 		    		validator.add(new ValidationMessage(
 		    				"O campo não pode ser vazio", "Título"));
 				if(source.getAuthors().equals(""))
 		    		validator.add(new ValidationMessage(
 		    				"O campo não pode ser vazio", "Autor(es)"));
 				
 				if((!source.getPublisher().equals("")) || (!source.getUrl().equals("")))
 					validator.add(new ValidationMessage("Erro inesperado", "Erro"));
 				else {
 					source.setPublisher(null);
 					source.setUrl(null);
 				}
 				
 				if(source.getInstitution().equals(""))
 					source.setInstitution(null);				
 				if(source.getDate().equals(""))
 					source.setDate(null);			
 				if(source.getMoreInformation().equals(""))
 					source.setMoreInformation(null);
 				
 				break;
 				
 			case BOOK:
 			case HANDBOOK:
 				if(source.getTitle().equals(""))
 		    		validator.add(new ValidationMessage(
 		    				"O campo não pode ser vazio", "Título"));
 				if(source.getAuthors().equals(""))
 		    		validator.add(new ValidationMessage(
 		    				"O campo não pode ser vazio", "Autor(es)"));
 				
 				if((!source.getInstitution().equals("")) || (!source.getUrl().equals("")))
 					validator.add(new ValidationMessage("Erro inesperado", "Erro"));
 				else {
 					source.setInstitution(null);
 					source.setUrl(null);
 				}
 				
 				if(source.getPublisher().equals(""))
 					source.setPublisher(null);				
 				if(source.getDate().equals(""))
 					source.setDate(null);				
 				if(source.getMoreInformation().equals(""))
 					source.setMoreInformation(null);
 				
 				break;
 				
 			case INTERNET:
 				if(source.getTitle().equals(""))
 		    		validator.add(new ValidationMessage(
 		    				"O campo não pode ser vazio", "Título"));
 				if(source.getUrl().equals(""))
 		    		validator.add(new ValidationMessage(
 		    				"O campo não pode ser vazio", "URL"));	
 				if(source.getDate().equals(""))
 		    		validator.add(new ValidationMessage(
 		    				"O campo não pode ser vazio", "Data de acesso"));
 				
 				if(!(source.getAuthors().equals("")) || !(source.getInstitution().equals("")) ||
 						!(source.getPublisher().equals("")))
 					validator.add(new ValidationMessage("Erro inesperado", "Erro"));
 				else {
 					source.setAuthors(null);
 					source.setInstitution(null);
 					source.setPublisher(null);
 				}
 				
 				if(source.getMoreInformation().equals(""))
 					source.setMoreInformation(null);
 				
 				break;
 				
 			case OTHER:
 				if(source.getMoreInformation().equals(""))
 		    		validator.add(new ValidationMessage(
 		    				"O campo não pode ser vazio", "Mais informações"));
 				
 				if((!source.getAuthors().equals("")) || (!source.getInstitution().equals("")) ||
 						(!source.getPublisher().equals("")) || (!source.getTitle().equals("")) ||
 						(!source.getUrl().equals("")) || (!source.getDate().equals("")))
 					validator.add(new ValidationMessage("Erro inesperado", "Erro"));
 				else {
 					source.setAuthors(null);
 					source.setInstitution(null);
 					source.setPublisher(null);
 					source.setTitle(null);
 					source.setUrl(null);
 					source.setDate(null);
 				}
 				break;
 				
 			default:
 				break;
 		}
 	}
 	
 	@Post
 	@Path("/userrules/newsource")
 	@Transactional
 	public void newsource(Source newSource) {
 		validateSource(newSource);
 		result.include("selectedType", newSource.getType());
 		validator.onErrorRedirectTo(UserrulesController.class).newsource();
 		
 		newSource.setUser(userSession.getUser());
 		newSource.setRegistrationDate(new Date());
 		
 		sourceService.save(newSource);
 		
 		result.include("messages", "A Referência foi cadastrada com sucesso.");
 
 		result.redirectTo(UserrulesController.class).newrule();
 	}
 
 }
