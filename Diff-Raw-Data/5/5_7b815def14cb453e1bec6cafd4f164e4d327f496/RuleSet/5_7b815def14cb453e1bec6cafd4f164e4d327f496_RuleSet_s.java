 package be.fnord.util.logic.defaultLogic;
 
 import java.util.HashSet;
 import java.util.LinkedList;
 
 import be.fnord.util.logic.WFF;
 
 import com.merriampark.Gilleland.CombinationGenerator;
 
 // Todo add in extra functions for add remove rules
 
 public class RuleSet {
 
 	private LinkedList<DefaultRule> rules = new LinkedList<DefaultRule>();
 
 	public LinkedList<String> getAllConsequences(WFF w){
 		LinkedList<String> _result = new LinkedList<String>();
 		
 		LinkedList<String> base = new LinkedList<String>();
 		for(DefaultRule r : rules){
 			base.add(r.getConsequence());
 		}
 //		System.out.println(base + " " + base.size());
 		
 		for(int j = 1; j <= base.size() ; j++){
 			CombinationGenerator comb = new CombinationGenerator(base.size(), j);
 			
 			while(comb.hasMore()){
 				
 				int[] ar = comb.getNext();
 				String newFormula = "";			
 				for(int i: ar){			
 					newFormula = newFormula + " " + a.e.AND + " " + base.get(i);
 				}
 				
 				if(newFormula.length() > 0) newFormula = newFormula.substring((" " + a.e.AND + " ").length() , newFormula.length());
 				_result.add(newFormula);
 			}
 		};
 		
 		_result = removeInconsistent(_result, w);
 		return _result;
 	}
 	
 	public void addRule(DefaultRule _rule){
 		rules.add(_rule);
 	}
 	
 	public LinkedList<DefaultRule> getRules() {
 		return rules;
 	}
 
 	public void setRules(LinkedList<DefaultRule> rules) {
 		this.rules = rules;
 	}
 
 	// Remove all inconsistent consequences
 	public LinkedList<String> removeInconsistent(LinkedList<String> consequences, WFF world) {
 		LinkedList<String> _newconsequences = new LinkedList<String>();
 		
 		for(String s: consequences){
 			WFF w = new WFF(s); // Create a new well formed formula
 			
 			if(w.isConsistent(world.getFormula())){
 				_newconsequences.add(s);
 			}else{
 //				System.out.println("Removing inconsistent consequence " + s);
 			}
 		}
 		
 		return _newconsequences;
 	}
 	
 	// 
 	public static LinkedList<String> addedConsequence = new LinkedList<String>();
 	
 	public LinkedList<String> generateExtensions(LinkedList<String> possibleExtensions, WFF world){
 		LinkedList<String> _extensions = new LinkedList<String>();
 //		System.out.println("Applyig rules to the world " + world + " ==== " + world.getClosure().trim());
 		
 		for(String possExtension : possibleExtensions){
 			LinkedList<String> _consequences = new LinkedList<String>();
 			WFF currentExtension = new WFF(possExtension); 
 			
 			// So long as one rule fires then we store the consequences of the rule
 			boolean overall = false;
 			for(DefaultRule d : rules){
 //				a.e.println("Trying: " + currentExtension.getFormula());
 				boolean results = testRule(currentExtension, world, d);
 				if(results) overall = true;
 				if(results){
 					// Recurse over rules with consequence as a fact
 					 
 					if(addedConsequence.contains(possExtension) || world.entails(currentExtension)){
 						// Do nothing just add it
 						_consequences.add(d.getConsequence());
 					}else{
 						addedConsequence.add(possExtension);
 						WFF backup = world;
 						world = new WFF(world.getFormula() + " " + a.e.AND + " (" + d.getConsequence() + ")");
 						LinkedList<String> _newCons = generateExtensions(possibleExtensions, world);
 						LinkedList<String> __newCons = new LinkedList<String>();
 						for(String s : _newCons){
 							s = s + " " + a.e.AND + "(" + d.getConsequence() + ")";
 							__newCons.add(s);
 						}
 						_consequences.addAll(__newCons);
 						world = backup;
 					}
 					
 				}
 
 			}
 			
 			if(overall){
 				// We create a deductive closure of the extension and all of the consequences
 				String talliedCons = "";
 				for(String c : _consequences){
 					talliedCons = talliedCons + " " + a.e.AND + " " + c;
 				}
 				if(talliedCons.length() > 0) talliedCons = talliedCons.substring((" " + a.e.AND + " ").length() , talliedCons.length());
 				WFF wffCons = new WFF(talliedCons);
 				
 				String extString = currentExtension.getClosure().trim();
 				String conString = wffCons.getClosure().trim();
 //				System.out.println("ext =" + extString);
 				if(extString.compareTo(conString) == 0)				
 					_extensions.add(possExtension);
 			}
 		}
 		
 		return _extensions;
 	}
 	
 	public LinkedList<String> applyRules(LinkedList<String> possibleExtensions, WFF world){
 		LinkedList<String> _extensions = new LinkedList<String>();
 //		System.out.println("Applyig rules to the world " + world + " ==== " + world.getClosure().trim());
 		System.out.println("!!-------------------------------------------------------");
 		System.out.println(possibleExtensions);
 		for(String possExtension : possibleExtensions){
 			
 			LinkedList<String> _consequences = new LinkedList<String>();
 			WFF currentExtension = new WFF(possExtension); 
 			
 			// So long as one rule fires then we store the consequences of the rule
 			boolean overall = false;
 			for(DefaultRule d : rules){
 				a.e.println("Trying: " + currentExtension.getFormula());
 				boolean results = testRule(currentExtension, world, d);
 				if(results) overall = true;
 				if(results){
 					_consequences.add(currentExtension.getFormula());
 				}
 
 			}
 			System.out.println("_consequences:" + _consequences);
 			System.out.println("~~-------------------------------------------------------");
 			if(overall){
 				// We create a deductive closure of the extension and all of the consequences
 				String talliedCons = "";
 				for(String c : _consequences){
 					talliedCons = talliedCons + " " + a.e.AND + " " + c;
 				}
 				if(talliedCons.length() > 0) talliedCons = talliedCons.substring((" " + a.e.AND + " ").length() , talliedCons.length());
 				WFF wffCons = new WFF(talliedCons);
 				
 				String extString = currentExtension.getClosure().trim();
 				String conString = wffCons.getClosure().trim();
 //				System.out.println("ext =" + extString);
 				if(extString.compareTo(conString) == 0)				
 					_extensions.add(possExtension);
 			}
 		}
 		
 		return _extensions;
 	}	
 	
 	public boolean testRule(WFF ext, WFF world, DefaultRule d){
 		WFF prec = new WFF(d.getPrerequisite());
 //		WFF world = new WFF(_world.getFormula() + " & " + ext.getFormula());
 		if(world.eval(prec)){			
 			// Good start, our prerequisite is true
 			
 			WFF just = new WFF(d.getJustificatoin());
 			if(just.eval(ext)){
 				
 				// It's okay to consider the extension, lets test it for entailment
 				WFF cons = new WFF(d.getConsequence());
 //				System.err.println(ext + " " + cons);
 				if(ext.isConsistent(cons.getFormula())){
 					if(ext.entails(cons)) {
 						System.out.println("Testing extension " + ext.getFormula());
 						System.out.println("\tWorld is " + world + " prec is " + prec);
 						System.out.println("\tRule is " + d.toString());
 						return true;
 					}
 					
 				}
 			}
 		}else{
 //			System.out.println("Failed on " );
 //			System.out.println("\tWorld is " + world + " prec is " + prec);
 		}
 		
 		return false;
 
 	}
 	
 	public HashSet<String> getLongestExtensions(HashSet<String> _ext){
 		HashSet<String> _result = new HashSet<String>();
 		HashSet<String> _remove = new HashSet<String>();
 		_result.add(a.e.EMPTY_FORMULA);
 		boolean updated = false;
 		for(String e : _ext){
 			for(String s: _ext){
 				
 				if(s.compareTo(e) != 0 && s.contains(e)){
 					updated = true;
 					_remove.add(e);
 				}else{					
 					_result.add(s);
 				}
 			}
 		}
 		_result.remove(a.e.EMPTY_FORMULA);
 		for(String s: _remove){
 			_result.remove(s);
 		}
 		if(updated){
 			
 			HashSet<String> _newResult = getLongestExtensions(_result);
 			
 			
 			return _newResult;
 		}
 		return _result;
 	}
 
 		
 }
