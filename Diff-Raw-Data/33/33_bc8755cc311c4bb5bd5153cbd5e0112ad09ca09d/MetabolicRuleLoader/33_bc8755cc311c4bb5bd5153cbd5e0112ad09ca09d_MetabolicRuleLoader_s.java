 package uk.ac.ed.inf.Metabolic.parser;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.pathwayeditor.contextadapter.publicapi.IContext;
 import org.pathwayeditor.contextadapter.publicapi.IValidationRuleConfig;
 import org.pathwayeditor.contextadapter.publicapi.IValidationRuleDefinition;
 import org.pathwayeditor.contextadapter.publicapi.IValidationRuleDefinition.RuleLevel;
 import org.pathwayeditor.contextadapter.toolkit.validation.IDefaultValidationRuleConfigLoader;
 import org.pathwayeditor.contextadapter.toolkit.validation.ValidationRuleConfig;
 import org.pathwayeditor.contextadapter.toolkit.validation.ValidationRuleDefinition;
 
 import uk.ac.ed.inf.Metabolic.MetabolicContextAdapterServiceProvider;
 
 /**
  * <br>
  * $Id:$
  * 
  * @author Anatoly Sorokin
  * @date 28 Jun 2008
  * 
  */
 public class MetabolicRuleLoader implements IDefaultValidationRuleConfigLoader {
 
 	private static MetabolicRuleLoader instance;
 
 	static final int ERROR_ID = 1000;
 	static final int STOICH_ERROR_ID = 1001;
 	static final int IC_ERROR_ID = 1002;
 	static final int RE_PARAM_ERROR_ID = 1003;
 	static final int NOT_REGISTERED_ERROR_ID=1004;
 	static final int CONSUMPTION_TO_REVERSIBLE_ERROR_ID=1005;
 	//Compound definition discrepancy
 	static final int COMP_DEF_ERROR_ID=1006;
 	//Reaction definition discrepancy
 	static final int RE_DEF_ERROR_ID=1007;
 //	error("Reaction for Activation relation is not registered in the model");
 //	error("Molecule for Cativation relation is not registered in the model");
 //	error("Molecule for Catalysis relation is not registered in the model");
 //	error("Reaction for Inhibiton relation is not registered in the model");
 //	error("Molecule for Inhibiton relation is not registered in the model");
 //	error("Molecule in Production relation is not registered in the model");
 //	error("Molecule in Consumption relation is not registered in the model");
 //	error("Consumption link to reversible reaction");
 
	static final int ORPHAN_ERROR_ID=1008;
	static IValidationRuleDefinition stoich, ic, reParam, notReg, consRev,compDef,reDef,ERROR,orphanDef;
 
 	private static Map<IValidationRuleDefinition, IParserRule> rules = new HashMap<IValidationRuleDefinition, IParserRule>();
 	static IValidationRuleConfig configStoich, configIC, configReparam,
			configNotReg, configConsRev,configCompDef,configReDef,ERROR_CONFIG,configOrphanDef;
 	static IContext context;
 
 	static {
 		context = MetabolicContextAdapterServiceProvider.getInstance()
 				.getContext();
 		notReg = new ValidationRuleDefinition(context,
 				"Object is not registered with the model", "NDOM parser", NOT_REGISTERED_ERROR_ID,
 				RuleLevel.MANDATORY);
 		consRev = new ValidationRuleDefinition(context,
 				"Consumption link to reversible reaction", "NDOM parser", CONSUMPTION_TO_REVERSIBLE_ERROR_ID,
 				RuleLevel.MANDATORY);
 		compDef=new ValidationRuleDefinition(context,"Compound definition discrepancy","NDOM parser",COMP_DEF_ERROR_ID,
 				RuleLevel.GUIDELINE);
 		reDef=new ValidationRuleDefinition(context,"Reaction definition discrepancy","NDOM parser",RE_DEF_ERROR_ID,
 				RuleLevel.GUIDELINE);
		orphanDef=new ValidationRuleDefinition(context,"Orphan object","NDOM parser",ORPHAN_ERROR_ID,
 				RuleLevel.GUIDELINE);
 		ERROR = new ValidationRuleDefinition(context, "Exception!", "Graph",
 				ERROR_ID, RuleLevel.MANDATORY);
 
 		configNotReg = new ValidationRuleConfig(notReg, true, true); // not run,
 		configConsRev = new ValidationRuleConfig(consRev, true, true); // not run,
		configOrphanDef = new ValidationRuleConfig(orphanDef, true, false); // not run,
 		// error
 		configCompDef = new ValidationRuleConfig(compDef, false, true); // not run,
 		configReDef = new ValidationRuleConfig(reDef, false, true); // not run,
 		// error
 		ERROR_CONFIG = new ValidationRuleConfig(ERROR, true, true); // not run,
 																	// error
 		stoichiometryIntDefinition();
 		initConcentrDoubleDefinition();
 		reParametersRegexDefinition();
 	}
 
 	private static void stoichiometryIntDefinition() {
 		IntPropertyRule r = new IntPropertyRule("STOICH");
 		stoich = new ValidationRuleDefinition(context,"Stoichiometry",  "NDOM parser",STOICH_ERROR_ID, RuleLevel.MANDATORY);
 		configStoich = new ValidationRuleConfig(stoich, true, true);// must be
 																	// run,
 																	// error
 		r.setRuleDef(stoich);
 		rules.put(stoich, r);
 	}
 
 	private static void initConcentrDoubleDefinition() {
 		DoublePropertyRule r = new DoublePropertyRule("IC");
 		ic = new ValidationRuleDefinition(
 				context, "Initial concentration", "NDOM parser",IC_ERROR_ID, RuleLevel.MANDATORY);
 		configIC = new ValidationRuleConfig(ic, true, true); // must be run,
 																// error
 		r.setRuleDef(ic);
 		rules.put(ic, r);
 	}
 
 	private static void reParametersRegexDefinition() {
 		RegexpPropertyRule r = new RegexpPropertyRule("Parameters",
 				"^(\\s*\\w+\\s*=\\s*[0-9eE\\-+.]+\\s*;)+$");
 		r.setEmptyValid(true);
 		reParam = new ValidationRuleDefinition(
 				context,
 				"Reaction parameters",  "NDOM parser",RE_PARAM_ERROR_ID, RuleLevel.MANDATORY);
 		configReparam = new ValidationRuleConfig(reParam, true, true);
 		r.setRuleDef(reParam);
 		rules.put(reParam, r);
 	}
 
 	private MetabolicRuleLoader() {
 
 	}
 
 	public Set<IValidationRuleConfig> loadDefaultRuleConfigurations() {
 		Set<IValidationRuleConfig> rc = new HashSet<IValidationRuleConfig>(
 				Arrays.asList(new IValidationRuleConfig[] { configStoich,
						configIC, configReparam, configNotReg, configConsRev,configCompDef,configReDef,configOrphanDef,ERROR_CONFIG }));
 		return rc;
 	}
 
 	public static IDefaultValidationRuleConfigLoader getInstance() {
 		if (instance == null) {
 			instance = new MetabolicRuleLoader();
 		}
 		return instance;
 	}
 
 	public IParserRule getRuleByDef(IValidationRuleDefinition def) {
 		if (rules.containsKey(def)) {
 			return rules.get(def);
 		} else {
 			IParserRule res = null;
 			for (IValidationRuleDefinition d : rules.keySet()) {
 				if (d.getRuleNumber() == def.getRuleNumber()) {
 					res = rules.get(d);
 					break;
 				}
 			}
 			return res;
 		}
 
 	}
 }
 
 /*
  * $Log:$
  */
