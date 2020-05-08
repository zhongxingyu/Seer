 package main.jadrin.bartender;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import main.jadrin.ontology.CheckElement;
 import main.jadrin.ontology.Drink;
 import main.jadrin.ontology.DrinkOntology;
 import main.jadrin.ontology.Ingredient;
 import main.jadrin.ontology.QueryOntology;
 import main.jadrin.ontology.Type;import gnu.prolog.database.PrologTextLoader;
 import gnu.prolog.database.PrologTextLoaderState;
 import gnu.prolog.io.ParseException;
 import gnu.prolog.io.TermReader;
 import gnu.prolog.term.AtomTerm;
 import gnu.prolog.term.CompoundTerm;
 import gnu.prolog.term.DoubleQuotesTerm;
 import gnu.prolog.term.IntegerTerm;
 import gnu.prolog.term.Term;
 import gnu.prolog.term.VariableTerm;
 import gnu.prolog.vm.Environment;
 import gnu.prolog.vm.Evaluate;
 import gnu.prolog.vm.Interpreter;
 import gnu.prolog.vm.Interpreter.Goal;
 import gnu.prolog.vm.PrologCode;
 import gnu.prolog.vm.PrologException;
 import jade.content.Concept;
 import jade.content.lang.Codec;
 import jade.content.lang.sl.SLCodec;
 import jade.content.onto.Ontology;
 import jade.core.Agent;
 import jade.domain.DFService;
 import jade.domain.FIPAException;
 import jade.domain.FIPANames;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.Property;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 
 public class BartenderAgent extends Agent { 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 8576658642439840374L;
 	private static final String SERVICE_NAME = "Bartender";
 	private Environment environment;
 	private Interpreter interpreter;
 	private Codec codec = new SLCodec();
 	private Ontology queryOntology = QueryOntology.getInstance();
 	private Ontology drinkOntology = DrinkOntology.getInstance();
 
 
 
 	private void registerService() {
 		DFAgentDescription dfd = new DFAgentDescription();
 		dfd.setName(getAID());
 		ServiceDescription sd = new ServiceDescription();
 		sd.setName(SERVICE_NAME);
 		sd.setType("drink-knowledge");
 		sd.addOntologies(QueryOntology.NAME);
 		sd.addOntologies(DrinkOntology.NAME);
 		sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
 		sd.addProperties(new Property("country", "Poland"));
 		dfd.addServices(sd);
 
 		try {
 			DFService.register(this, dfd);
 		}
 		catch (FIPAException e) {
 			System.err.println(getLocalName() +
 					" registration with DF unsucceeded. Reason: " + e.getMessage());
 			doDelete();
 		}
 	}
 
 	public String getCodecName()
 	{
 		return this.codec.getName();
 	}
 
 	protected void setup() 
 	{ 
 		getContentManager().registerLanguage(codec);
 		getContentManager().registerOntology(queryOntology);
 		getContentManager().registerOntology(drinkOntology);
 
 		//Prolog setup:
 		this.environment =  new Environment();
 
 		URL knowledgeFile = getClass().getResource("/main/jadrin/resources/knowledge.pro");
 		URL rulesFile = getClass().getResource("/main/jadrin/resources/rules.pro");
 		environment.ensureLoaded(AtomTerm.get(knowledgeFile.getFile()));
 		environment.ensureLoaded(AtomTerm.get(rulesFile.getFile()));
 
 
 
 		this.interpreter = environment.createInterpreter();
 		environment.runInitialization(interpreter);
 		//    	
 
 		registerService();
 		addBehaviour(new CommunicateBehaviour(this)); // handles A,B and C case from documentation
 	}
 
 	public CheckElement whatIsThat(CheckElement check){
 
 		if (check == null) return new CheckElement();
 
 		Term[] args = {buildTerm(check.getName())};
 		//Term[] args = {AtomTerm.get(check.getName())};
 		CheckElement result = check;
 		CompoundTerm goalTermIngredient = new CompoundTerm(AtomTerm.get("is_ingredient"), args);
 		CompoundTerm goalTermDrink = new CompoundTerm(AtomTerm.get("is_drink"), args);
 		CompoundTerm goalTermPartOf = new CompoundTerm(AtomTerm.get("is_part_of"), args);
 
 		int isIngredient = PrologCode.FAIL;
 		int isDrink = PrologCode.FAIL;
 		int isPartOf =PrologCode.FAIL;
 		try {
 			isIngredient = interpreter.runOnce(goalTermIngredient);
 			isDrink = interpreter.runOnce(goalTermDrink);
 			isPartOf = interpreter.runOnce(goalTermPartOf);			
 
 		} catch (PrologException e) {
 			e.printStackTrace();
 		}
 
 		result.setPartOf(false);	
 		if(isPartOf ==  PrologCode.SUCCESS){
 			result.setPartOf(true);	
 		}
 
 
 		if(isIngredient == PrologCode.SUCCESS){
 			result.setType(Type.INGREDIENT);
 			return result;
 		}
 		if(isDrink == PrologCode.SUCCESS) {
 			result.setType(Type.DRINK);
 			return result;
 		}
 
 		result.setType(Type.UNKNOWN);
 		return result; 	
 	}
 
 	public LinkedList<Drink> getDrinksWithGivenIngredients(ArrayList<Ingredient> ingredientsArray){
 
 		//		LinkedList<Term> terms = new LinkedList<Term>();
 		LinkedList<Drink> drinkList = new LinkedList<Drink>();
 		
 		for (Ingredient ing : ingredientsArray) {
 			String ingredient = ing.getName();
 			Term ingredientTerm = buildTermList(ingredient);
 
 			VariableTerm drinkName = new VariableTerm("DrinkName");
 			VariableTerm fullIngredientsList = new VariableTerm("FullIngredientsList");
 			VariableTerm recipe = new VariableTerm("Recipe");
 
 			Term[] args = { ingredientTerm , drinkName, fullIngredientsList,recipe };
 
 			CompoundTerm goalTerm = new CompoundTerm(AtomTerm.get("what_can_i_do"), args);
 			Interpreter.Goal goal= interpreter.prepareGoal(goalTerm);
 
 			int rc;
 			try {
 				rc = interpreter.execute(goal);
 				while(rc == PrologCode.SUCCESS || rc == PrologCode.SUCCESS_LAST){
 					//rc = interpreter.runOnce(goalTerm);
 					Term drinkDeref  = drinkName.dereference();
 					Term fullIngredientsDeref = fullIngredientsList.dereference();
 					Term recipeDeref = recipe.dereference();	
 					drinkList.add(createDrink(drinkDeref,fullIngredientsDeref,recipeDeref ));
 					rc = interpreter.execute(goal);
 				}
 			} catch (PrologException e) {
 				e.printStackTrace();
 			}	
 		}
 		return drinkList;
 
 	}
 
 	private Drink createDrink(String drinkName, Term fullIngredientsDeref,
 			Term recipeDeref) {
 
 		String ingredients[] = tokenizeIngredients(fullIngredientsDeref);
 		String recipe = tokenizeTerm(recipeDeref).replace("\\x20\\", " ");
 		return new Drink(drinkName, ingredients, recipe);
 	}
 
 	private Drink createDrink(Term drinkDeref, Term fullIngredientsDeref,
 			Term recipeDeref) {
 
 		String drinkName = tokenizeTerm(drinkDeref);
 		return createDrink(drinkName, fullIngredientsDeref, recipeDeref);
 	}
 
 	private String[] tokenizeIngredients(Term fullIngredientsDeref) {
 		String str = fullIngredientsDeref.toString().replace("'", "").replace(",", "").replace("[[", "").replace("]]", "").replace("][", " ");
 		StringTokenizer tokenizer = new StringTokenizer(str.toString(), "[]");
 		ArrayList<String> ingredients = new ArrayList<String> ();
 
 		while(tokenizer.hasMoreTokens()){
 			ingredients.add(tokenizer.nextToken());
 		}
 
 		return ingredients.toArray(new String[ingredients.size()]);
 	}
 
 	private String tokenizeTerm(Term drinkDeref) {
 		StringTokenizer tokenizer = new StringTokenizer(drinkDeref.toString(), "['], ");
 		StringBuffer stringBuffer = new StringBuffer();
 		while(tokenizer.hasMoreTokens()){
 			stringBuffer.append(tokenizer.nextToken());
 			stringBuffer.append(" ");
 		}
 
 		return stringBuffer.toString();
 	}	
 
 	public Drink getDrinkRecipe(String drinkName){
 
 		VariableTerm fullIngredientsList = new VariableTerm("FullIngredientsList");
 		VariableTerm recipe = new VariableTerm("Recipe");
 		Term drinkTerm = buildTerm(drinkName);
 		Term[] args1 = { drinkTerm, fullIngredientsList};	
 		CompoundTerm goalTermIngredients = new CompoundTerm(AtomTerm.get("ingredients"), args1);
 		Term[] args2 = { drinkTerm, recipe};	
 		CompoundTerm goalTermRecipe= new CompoundTerm(AtomTerm.get("recipe"), args2);
 		int rc1 = PrologCode.FAIL;
 		int rc2 = PrologCode.FAIL;
 		try {
 			rc1 = interpreter.runOnce(goalTermIngredients);	
 			rc2 = interpreter.runOnce(goalTermRecipe);	
 		} catch (PrologException e) {
 			e.printStackTrace();
 		}
 
 		if((rc2 ==PrologCode.SUCCESS || rc2 == PrologCode.SUCCESS_LAST) 
 				&& (rc1 ==PrologCode.SUCCESS || rc1 == PrologCode.SUCCESS_LAST)){
 			Term fullIngredientsDeref = fullIngredientsList.dereference();
 			Term recipeDeref = recipe.dereference();
 			return createDrink(drinkName, fullIngredientsDeref, recipeDeref);
 		}
 
 		return null;
 	}
 	/**
 	 * 
 	 * @param arrayList
 	 * @return Drink object with ingredients containing only missing ones, proper name, recipe
 	 */
 	public Drink getMissingIngredientsAndRecipe(ArrayList<Ingredient> arrayList , String drinkName){
 		VariableTerm missingIngredients = new VariableTerm("MissingIngredients");
 		VariableTerm recipe = new VariableTerm("Recipe");
 
 		Term drinkTerm = buildTerm(drinkName);
 		Term ingredientsTerm = buildTermList(arrayList);
 
 		Term[] args1 = { ingredientsTerm, drinkTerm, missingIngredients, recipe };	
 		CompoundTerm goalTermMissingIngredients= new CompoundTerm(AtomTerm.get("what_is_missing"), args1);
 		int rc = PrologCode.FAIL;
 		try {
 			rc = interpreter.runOnce(goalTermMissingIngredients);		
 		} catch (PrologException e) {
 			e.printStackTrace();
 		}
 
 		if(rc ==PrologCode.SUCCESS || rc == PrologCode.SUCCESS_LAST){
 			Term missingIngredientsDeref = missingIngredients.dereference();
 			Term recipeDeref = recipe.dereference();		
 			return createDrink(drinkName,missingIngredients, recipeDeref);
 		}
 		return null;
 	}
 
 	private Term buildTermList(ArrayList<Ingredient> arrayList) {
 		
 		String[] str = new String [arrayList.size()];
 		for(int i = 0 ; i<arrayList.size(); i++ ){
 			str[i]= arrayList.get(i).getName();
 		}
 		return buildTermList(str);
 	}
 
 	private Term buildTerm(String termStr) {
 		List<Term> terms = new LinkedList<Term>();
 		terms.add(AtomTerm.get(termStr) );
 		return CompoundTerm.getList(terms); 
 	}
 
 	private Term buildTermList(String str) {
 		StringTokenizer tokenizer = new StringTokenizer(str);
 		List<Term> terms = new LinkedList<Term>();
 		while (tokenizer.hasMoreElements()) {
 			terms.add(buildTerm(tokenizer.nextToken()) );
 		}
 		return CompoundTerm.getList(terms); 
 	}
 
 	private Term buildTermList(String[] str) {
 		List<Term> terms = new LinkedList<Term>();
 		for (String string : str) {
 			terms.add(buildTerm(string) );
 		}
 		return CompoundTerm.getList(terms); 
 	}
 
 }
