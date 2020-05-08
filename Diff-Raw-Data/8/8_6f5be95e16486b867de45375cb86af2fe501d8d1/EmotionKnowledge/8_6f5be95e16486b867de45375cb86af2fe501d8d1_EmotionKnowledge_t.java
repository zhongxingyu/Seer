 package maro.core;
 
 import maro.wrapper.Dumper;
 import maro.wrapper.OwlApi;
 
 import jason.asSyntax.ASSyntax;
 import jason.bb.BeliefBase;
 
 import java.util.Iterator;
 import java.util.HashMap;
 import java.util.Set;
 
 public class EmotionKnowledge {
 
 	protected FeelingsThreshold ft;
 	protected OwlApi oaw = null;
 	protected String filename;
 	protected String agName;
 	protected Emotion emotion;
 	protected Integer lastStepFeeling;
 
 	public EmotionKnowledge(String name, String fileName) throws Exception {
 		filename = fileName;
 		agName = name;
 
 		oaw = new OwlApi();
 		oaw.loadOntologyFromFile(filename);
 
 		Set<String> allEmotions;
 		allEmotions = oaw.getAllEmotions();
 		if (allEmotions == null || allEmotions.isEmpty()) {
 			throw new Exception("Fail on load ontology data");
 		}
 
 		emotion = new Emotion();
 		emotion.setEmotions(allEmotions);
 
 		ft = new FeelingsThreshold();
 	}
 
 	protected boolean changeBelief(Dumper dumper, boolean isAdd) {
 		String functor;
 		String[] terms;
 		Dumper[] annots;
 		int arity;
 
 		if (dumper == null) {
 			return false;
 		}
 
 		terms = dumper.getTerms();
 		arity = dumper.getArity();
 		functor = dumper.getFunctorComplete();
 
 		if (!oaw.isRelevant(arity, functor)) {
 			return false;
 		}
 
 		annots = dumper.getAnnots();
 
 		if (isAdd) {
 			return oaw.add(arity, functor, terms, annots);
 		}
 		//else
 		return oaw.remove(arity, functor, terms, annots);
 	}
 
 	public boolean add(Dumper dumper) {
 		return changeBelief(dumper, true);
 	}
 
 	public boolean remove(Dumper dumper) {
 		return changeBelief(dumper, false);
 	}
 
 	public Iterator<Dumper> getCandidatesByFunctorAndArityIter(String functor, int arity) {
 		if (functor.equals("sameAs") || functor.equals("~sameAs")) {
 			arity = 0;
 		}
 
 		if (!oaw.isRelevant(arity, functor)) {
 			return null;
 		}
 
 		return oaw.getCandidatesByFunctorAndArityIter(arity, functor);
 	}
 
 	public Iterator<Dumper> iterator() {
 		return oaw.iterator();
 	}
 	boolean ignoreFlag = false;
 
 	public void summarize(int step) {
 		if (ft.isLoaded(emotion, agName, oaw) == true) {
 			if (ft.isActive() == false) {
 				if (ignoreFlag == false) {
 					System.out.println("Threshold of emotions are "
 						+ "inconsistent ou fault, ignoring emotions...");
 					ignoreFlag = true;
 				}
 				return; // why lost time?
 			} else {
 				if (ignoreFlag == false) {
 					// Erase items from setup after the loaded
 					oaw.remove(2, "hasSetup", null, null);
 					oaw.remove(2, "isSetupOf", null, null); // inverse of hasSetup
 					oaw.remove(2, "hasThresholdType", null, null);
 					oaw.remove(2, "hasThreshold", null, null);
 					oaw.remove(1, "setup", null, null);
 					ignoreFlag = true;
 				}
 			}
 		}
 
 		for (String s : emotion.getEmotions()) {
 			HashMap<Integer, Integer> e = emotion.getAllValences(s, agName);
 			Integer ret = oaw.summaryOf(agName, s, step, e);
 			if (ret == null) {
 				continue;
 			}
 			emotion.setValence(s, agName, step, ret);
 		}
 	}
 
 	public Set<String> getEmotionType() {
 		Set<String> et = new java.util.HashSet<String>();
 		for (String s : emotion.getEmotions())
 			et.add(s);
 		return et;
 	}
 
 	public Integer getEmotionPotence(String emotionType, String agentName) {
 		Integer valence = emotion.getValence(emotionType, agentName,
 				(lastStepFeeling == null) ? 0 : lastStepFeeling);
 		return valence;
 	}
 	public Integer getEmotionValence(String emotionType, String agentName) {
 		Integer valence = getEmotionPotence(emotionType, agentName);
 		Integer minimum = ft.getThreshold(emotionType);
 		if (valence == null || minimum == null) return null;
		if ((valence > 0 && valence >= minimum)) {
			return valence - minimum;
		}
		if ((valence < 0 && valence <= -minimum)) {
			return valence + minimum;
		}
		return null;
 	}
 
 	public Set<String> feelings(int step) {
 		Set<String> feelingStrLit = new java.util.HashSet<String>();
 
 		if (ft.isActive() == false) {
 			return feelingStrLit;
 		}
 
 		lastStepFeeling = step;
 		for (String s : getEmotionType()) {
 			Integer potence = getEmotionValence(s, agName);
 			int feeling = (potence != null) ? potence : 0;
 
 			if (feeling > 0) {
 				feelingStrLit.add("feeling(" + s + ", " + feeling + ")");
 			}
 		}
 
 		return feelingStrLit;
 	}
 
 	public void dumpData() {
 		if (System.getenv("emotionKnowledgeDebug") == null) {
 			return;
 		}
 
 		try {
 			String filenameDump;
 
 			if (agName.isEmpty()) filenameDump = "/tmp/baka.owl";
 			else filenameDump = "/tmp/"+agName+".owl";
 
 			oaw.dumpOntology(filenameDump);
 		} catch (Exception e) {
 			System.err.println("Error dumping ontology");
 			e.printStackTrace();
 			System.exit(29);
 		}
 	}
 
 	public void loadPreferences(BeliefBase base) throws Exception
 	{
 		Set<Dumper> negatives =
 			oaw.getCandidatesByFunctorAndArity(1, "negative");
 		Set<Dumper> positives =
 			oaw.getCandidatesByFunctorAndArity(1, "positive");
 
 		if (negatives == null && positives == null) {
 			erasePreferenceItens();
 			return ;
 		}
 
 		Set<Dumper> hasAnnotations =
 			oaw.getCandidatesByFunctorAndArity(2, "hasAnnotation");
 		Set<Dumper> hasAnnotationValues =
 			oaw.getCandidatesByFunctorAndArity(2, "hasAnnotationValue");
 
 		if (hasAnnotationValues == null && hasAnnotations == null) {
 			erasePreferenceItens();
 			return ;
 		}
 
 		for (Dumper d : negatives) {
 			String text  = d.getTerms()[0];
 			String value = null;
 			String annot = null;
 
 			for (Dumper a : hasAnnotations) {
 				if (a.getTerms()[0].equals(text)) {
 					annot = a.getTermAsString(1);
 					break;
 				}
 			}
 			for (Dumper v : hasAnnotationValues) {
 				if (v.getTerms()[0].equals(text)) {
 					value = v.getTermAsString(1);
 					break;
 				}
 			}
 
 			if (value != null && annot != null) {
 				base.add(
 					ASSyntax.parseLiteral(
 					new String("priority(repulse,"+annot+","+value+")[source(self)]")
 					)
 				);
 			}
 		}
 
 		for (Dumper d : positives) {
 			String text  = d.getTerms()[0];
 			String value = null;
 			String annot = null;
 
 			for (Dumper a : hasAnnotations) {
 				if (a.getTerms()[0].equals(text)) {
 					annot = a.getTermAsString(1);
 					break;
 				}
 			}
 			for (Dumper v : hasAnnotationValues) {
 				if (v.getTerms()[0].equals(text)) {
 					value = v.getTermAsString(1);
 					break;
 				}
 			}
 
 			if (value != null && annot != null) {
 				base.add(
 					ASSyntax.parseLiteral(
 					new String("priority(attract,"+annot+","+value+")[source(self)]")
 					)
 				);
 			}
 		}
 
 		erasePreferenceItens();
 	}
 
 	private void erasePreferenceItens() {
 		// Erase itens from preference after the preparation
 		oaw.remove(2, "hasAnnotationValue", null, null);
 		oaw.remove(2, "hasAnnotation", null, null);
 		oaw.remove(2, "hasName", null, null);
 
 		oaw.remove(1, "positive", null, null);
 		oaw.remove(1, "negative", null, null);
 		oaw.remove(1, "preference", null, null);
 		oaw.remove(1, "static", null, null);
 		oaw.remove(1, "dynamic", null, null);
 		oaw.remove(1, "annotation", null, null);
 	}
 }
