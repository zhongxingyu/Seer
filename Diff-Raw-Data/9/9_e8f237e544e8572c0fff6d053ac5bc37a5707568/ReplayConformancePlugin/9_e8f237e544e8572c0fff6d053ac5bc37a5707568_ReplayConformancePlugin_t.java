 package org.processmining.plugins.petrinet.replayfitness;
 
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import java.util.Vector;
 
 import javax.swing.JComponent;
 
 
 
 
 import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
 import org.deckfour.xes.classification.XEventClass;
 import org.deckfour.xes.classification.XEventClasses;
 import org.deckfour.xes.classification.XEventClassifier;
 import org.deckfour.xes.extension.std.XConceptExtension;
 import org.deckfour.xes.info.XLogInfo;
 import org.deckfour.xes.info.XLogInfoFactory;
 import org.deckfour.xes.info.impl.XLogInfoImpl;
 import org.deckfour.xes.model.XEvent;
 import org.deckfour.xes.model.XLog;
 import org.deckfour.xes.model.XTrace;
 
 import org.processmining.connections.logmodel.LogPetrinetConnectionImpl;
 import org.processmining.contexts.uitopia.UIPluginContext;
 import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
 
 import org.processmining.framework.connections.ConnectionCannotBeObtained;
 
 import org.processmining.framework.plugin.PluginContext;
 import org.processmining.framework.plugin.annotations.Plugin;
 import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
 import org.processmining.models.graphbased.AttributeMap;
 import org.processmining.models.graphbased.directed.petrinet.Petrinet;
 import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
 import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
 import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
 import org.processmining.models.graphbased.directed.petrinet.elements.Place;
 import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
 import org.processmining.models.semantics.petrinet.Marking;
 import org.processmining.models.semantics.petrinet.PetrinetSemantics;
 import org.processmining.models.semantics.petrinet.impl.PetrinetSemanticsFactory;
 
 import org.processmining.plugins.connectionfactories.logpetrinet.LogPetrinetConnectionFactoryUI;
 import org.processmining.plugins.petrinet.replay.ReplayAction;
 import org.processmining.plugins.petrinet.replay.Replayer;
 
 
 
 public class ReplayConformancePlugin {
 
 
 
 
 	public TotalConformanceResult getConformanceDetails(PluginContext context, XLog log, Petrinet net, Marking marking, ReplayFitnessSetting setting,Map<Transition, XEventClass> map  ) {
 		TotalConformanceResult totalResult = new TotalConformanceResult();
 		totalResult.setTotal(new ConformanceResult("Total"));
 		totalResult.setList(new Vector<ConformanceResult>());
 
 		XEventClasses classes = getEventClasses(log);
 		if(map==null){
 		//Map<Transition, XEventClass> 
 		map = getMapping(classes, net);
 		}
 		context.getConnectionManager().addConnection(new LogPetrinetConnectionImpl(log, classes, net, map));
 		
 		PetrinetSemantics semantics = PetrinetSemanticsFactory.regularPetrinetSemantics(Petrinet.class);
 
 		Replayer<ReplayFitnessCost> replayer = new Replayer<ReplayFitnessCost>(context, net, semantics, map,
 				ReplayFitnessCost.addOperator);
 
 		int replayedTraces = 0;
 		int i =0;
 		for (XTrace trace : log) {
 			List<XEventClass> list = getList(trace, classes);
 			try {
 				System.out.println("Replay :" + ++i);
 				List<Transition> sequence = replayer.replayTrace(marking, list, setting);
 				String tracename = getTraceName(trace);
 				updateConformance(net, marking, sequence, semantics, totalResult,tracename);
 				replayedTraces++;
 				System.out.println("Replayed");
 
 			} catch (Exception ex) {
 				System.out.println("Failed");
 				context.log("Replay of trace " + trace + " failed: " + ex.getMessage());
 			}
 		}
 
 		context.log("(based on a successful replay of " + replayedTraces + " out of " + log.size() + " traces)");
 
 		totalResult.getTotal().updateConformance();
 		ReplayRuposConnection connection = new ReplayRuposConnection(totalResult, log, net);
 		context.getConnectionManager().addConnection(connection);
 		return totalResult;
 	}
 
 	private static String getTraceName(XTrace trace) {
 		String traceName = XConceptExtension.instance().extractName(trace);
 		return (traceName != null ? traceName : "<unknown>");
 	}
 
 	private void addArcUsage(Arc arc, ConformanceResult fitnessResult, ConformanceResult totalResult) {
 		Integer numUsage = totalResult.getMapArc().get(arc);
 		totalResult.getMapArc().put(arc, numUsage == null ? 1 : numUsage+1);
 		numUsage = fitnessResult.getMapArc().get(arc);
 		fitnessResult.getMapArc().put(arc, numUsage == null ? 1 : numUsage+1);
 	}
 
 
 	private void updateConformance(Petrinet net, Marking initMarking, List<Transition> sequence, PetrinetSemantics semantics, TotalConformanceResult totalResult, String tracename) {
 		Marking marking = new Marking(initMarking);
 		int producedTokens = marking.size();
 		int consumedTokens = 0;
 		int producedTrace  = marking.size();
 		int consumedTrace = 0;
 
 		int missingTrace=0;
 		ConformanceResult tempConformanceResult = new ConformanceResult(tracename);
 		totalResult.getList().add(tempConformanceResult);
 
 		for (Transition transition : sequence) {
 			boolean fittingTransition = true;
 			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preset = net
 			.getInEdges(transition);
 			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : preset) {
 				if (edge instanceof Arc) {
 					Arc arc = (Arc) edge;
 					addArcUsage(arc, tempConformanceResult, totalResult.getTotal());
 					Place place = (Place) arc.getSource();
 					int consumed = arc.getWeight();
 					int missing = 0;
 					//
 					if (arc.getWeight() > marking.occurrences(place)) {
 						missing = arc.getWeight() - marking.occurrences(place);
 					}
 					for (int i = missing; i < consumed; i++) {
 						marking.remove(place);
 					}
 					// Rupos patches
 					for (int i = 0; i < missing; i++) {
 						totalResult.getTotal().getMissingMarking().add(place);
 						tempConformanceResult.getMissingMarking().add(place);
 					}
 					consumedTokens += consumed;
 					consumedTrace += consumed;
 					missingTrace +=missing;
 					//se sono mancati token, questa transizione non ha fittato per questa traccia
 					if (missing>0) fittingTransition = false;
 				}
 			}
 			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = net
 			.getOutEdges(transition);
 			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : postset) {
 				if (edge instanceof Arc) {
 					Arc arc = (Arc) edge;
 					addArcUsage(arc, tempConformanceResult, totalResult.getTotal());
 					Place place = (Place) arc.getTarget();
 					int produced = arc.getWeight();
 					for (int i = 0; i < produced; i++) {
 						marking.add(place);
 					}
 					producedTokens += produced;
 					producedTrace +=produced;
 				}
 			}
 			if (!fittingTransition) {
 				Integer numTraceNotFittingForTransaction = totalResult.getTotal().getMapTransition().get(transition);
 				totalResult.getTotal().getMapTransition().put(transition, numTraceNotFittingForTransaction == null ? 1 : numTraceNotFittingForTransaction+1);
 				//sulla singola traccia, il numero di tracce che non fittano la transizione è 0 o 1
 				tempConformanceResult.getMapTransition().put(transition, 1);
 			}
 
 		}
 		consumedTokens += marking.size();
 		consumedTrace += marking.size();
 		int remainingTokens = marking.isEmpty() ? 0 : marking.size() - 1;
 		
 		// Rupos patches
 		totalResult.getTotal().getRemainingMarking().addAll(marking);
 		tempConformanceResult.getRemainingMarking().addAll(marking);
 
 		//calcola la fitness del singolo trace
 		tempConformanceResult.setProducedTokens(producedTokens);
 		tempConformanceResult.setConsumedTokens(consumedTokens);
 
 		// Per il totale
 		totalResult.getTotal().setProducedTokens(totalResult.getTotal().getProducedTokens() + producedTokens);
 		totalResult.getTotal().setConsumedTokens(totalResult.getTotal().getConsumedTokens() + consumedTokens);
 
 		// Calcola la fitness per il singolo trace
 		tempConformanceResult.updateConformance();
 	}
 
 	private List<XEventClass> getList(XTrace trace, XEventClasses classes) {
 		List<XEventClass> list = new ArrayList<XEventClass>();
 		for (XEvent event : trace) {
 			list.add(classes.getClassOf(event));
 		}
 		return list;
 	}
 
 	private XEventClasses getEventClasses(XLog log) {
 		XEventClassifier classifier = XLogInfoImpl.STANDARD_CLASSIFIER;
 		XLogInfo summary = XLogInfoFactory.createLogInfo(log, classifier);
 		XEventClasses eventClasses = summary.getEventClasses(classifier);
 		return eventClasses;
 	}
 
 	private Map<Transition, XEventClass> getMapping(XEventClasses classes, Petrinet net) {
 		Map<Transition, XEventClass> map = new HashMap<Transition, XEventClass>();
 
 		for (Transition transition : net.getTransitions()) {
 			for (XEventClass eventClass : classes.getClasses()) {
 				if (eventClass.getId().equals(transition.getAttributeMap().get(AttributeMap.LABEL))) {
 					map.put(transition, eventClass);
 				}
 			}
 		}
 		return map;
 	}
	
 
 	private void suggestActions(ReplayFitnessSetting setting, XLog log, Petrinet net) {
 		boolean hasInvisibleTransitions = false;
 		Collection<String> transitionLabels = new HashSet<String>();
 		for (Transition transition : net.getTransitions()) {
 			transitionLabels.add((String) transition.getAttributeMap().get(AttributeMap.LABEL));
 			if (transition.isInvisible()) {
 				hasInvisibleTransitions = true;
 			}
 		}
 		Collection<String> eventClassLabels = new HashSet<String>();
 		for (XEventClass eventClass : getEventClasses(log).getClasses()) {
 			eventClassLabels.add(eventClass.getId());
 		}
 		setting.setAction(ReplayAction.INSERT_ENABLED_MATCH, true);
 		setting.setAction(ReplayAction.INSERT_DISABLED_MATCH, true);
 		if (transitionLabels.containsAll(eventClassLabels)) {
 			/*
 			 * For every event class there is at least one transition. Thus,
 			 * there is always a matching transition.
 			 */
 			setting.setAction(ReplayAction.REMOVE_HEAD, false);
 			setting.setAction(ReplayAction.INSERT_ENABLED_MISMATCH, false);
 			setting.setAction(ReplayAction.INSERT_DISABLED_MISMATCH, false);
 		} else {
 			setting.setAction(ReplayAction.REMOVE_HEAD, true);
 			setting.setAction(ReplayAction.INSERT_ENABLED_MISMATCH, true);
 			setting.setAction(ReplayAction.INSERT_DISABLED_MISMATCH, true);
 		}
 		if (hasInvisibleTransitions || !eventClassLabels.containsAll(transitionLabels)) {
 			setting.setAction(ReplayAction.INSERT_ENABLED_INVISIBLE, true);
 		} else {
 			/*
 			 * There are no explicit invisible transitions and all transitions
 			 * correspond to event classes.
 			 */
 			setting.setAction(ReplayAction.INSERT_ENABLED_INVISIBLE, false);
 		}
 	}
 
 	// Rupos public methos
 	@Plugin(name = "ConformaceDetailsUI", returnLabels = { "Conformace Total" }, returnTypes = { TotalConformanceResult.class }, parameterLabels = {}, userAccessible = true)
 	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "di.unipi.it", email = "di.unipi.it")
 	public TotalConformanceResult getConformanceDetails(UIPluginContext context, XLog log, Petrinet net) {
 		ReplayFitnessSetting setting = new ReplayFitnessSetting();
 		suggestActions(setting, log, net);
 		ReplayRuposUI ui = new ReplayRuposUI(setting);
 		//context.showWizard("Configure Conformance Settings", true, false, ui.initComponents());
 		
         
 		//Build and show the UI to make the mapping
 		LogPetrinetConnectionFactoryUI lpcfui = new LogPetrinetConnectionFactoryUI(log, net);
 		//InteractionResult result = context.showWizard("Mapping Petrinet - Log", false, true,  lpcfui.initComponents());
 
 		//Create map or not according to the button pressed in the UI
 		Map<Transition, XEventClass> map=null;
 		InteractionResult result =null;
 		/*
 		 * The wizard loop.
 		 */
 		boolean sem=true;
 		/*
 		 * Show the current step.
 		 */
 		JComponent mapping = lpcfui.initComponents();
 		JComponent config = ui.initComponents();
 		result = context.showWizard("Mapping Petrinet - Log", true, false, mapping );
 		while (sem) {
 			
 			switch (result) {
 				case NEXT :
 					/*
 					 * Show the next step. 
 					 */
 					result =context.showWizard("Configure Conformance Settings", false, true, config);
 					ui.setWeights();
 					break;
 				case PREV :
 					/*
 					 * Move back. 
 					 */
 					result = context.showWizard("Mapping Petrinet - Log", true, false,  mapping);
 					break;
 				case FINISHED :
 					/*
 					 * Return  final step.
 					 */
 					map = lpcfui.getMap();
 					sem=false;
 					break;
 				default :
 					/*
 					 * Should not occur.
 					 */
 					context.log("press Cancel");
 					return null;
 			}
 		}
 		//if (result == InteractionResult.FINISHED) {
 		//	 map = lpcfui.getMap();
 		//}
 
 		Marking marking;
 
 		try {
 			InitialMarkingConnection connection = context.getConnectionManager().getFirstConnection(
 					InitialMarkingConnection.class, context, net);
 			marking = connection.getObjectWithRole(InitialMarkingConnection.MARKING);
 		} catch (ConnectionCannotBeObtained ex) {
 			context.log("Petri net lacks initial marking");
 			return null;
 		}
        
 		TotalConformanceResult totalResult = getConformanceDetails(context, log, net,marking, setting,map);
 
 
 		return totalResult;
 	}
 
 	@Plugin(name = "ConformanceDetails", returnLabels = { "Conformance Total" }, returnTypes = { TotalConformanceResult.class }, parameterLabels = {}, userAccessible = true)
 	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "di.unipi.it", email = "di.unipi.it")
 	public TotalConformanceResult getConformanceDetails(PluginContext context, XLog log, Petrinet net) {
 		ReplayFitnessSetting setting = new ReplayFitnessSetting();
 		suggestActions(setting, log, net);
 
 		TotalConformanceResult total = getConformanceDetails(context, log, net, setting);
 
 
 		return total;
 	}
 
 	@Plugin(name = "ConformanceDetailsSettingsWithMarking", returnLabels = { "Conformance Total" }, returnTypes = { TotalConformanceResult.class }, parameterLabels = {}, userAccessible = true)
 	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "di.unipi.it", email = "di.unipi.it")
 	public TotalConformanceResult getFitnessDetails(PluginContext context, XLog log, Petrinet net, ReplayFitnessSetting setting, Marking marking) {
 
 
 		Map<Transition, XEventClass> map=null;
 		TotalConformanceResult total = getConformanceDetails(context, log, net, marking, setting,map);
 
 		return total;
 	}
 
 	@Plugin(name = "ConformanceDetailsSettings", returnLabels = { "Conformance Total" }, returnTypes = { TotalConformanceResult.class }, parameterLabels = {}, userAccessible = true)
 	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "di.unipi.it", email = "di.unipi.it")
 	public TotalConformanceResult getConformanceDetails(PluginContext context, XLog log, Petrinet net, ReplayFitnessSetting setting) {
 
 		Marking marking;
 
 		try {
 			InitialMarkingConnection connection = context.getConnectionManager().getFirstConnection(
 					InitialMarkingConnection.class, context, net);
 			marking = connection.getObjectWithRole(InitialMarkingConnection.MARKING);
 		} catch (ConnectionCannotBeObtained ex) {
 			context.log("Petri net lacks initial marking");
 			return null;
 		}
 		Map<Transition, XEventClass> map=null;
 		TotalConformanceResult total = getConformanceDetails(context, log, net, marking, setting,map);
 
 		return total;
 	}
 
	  @Plugin(name = "FitnessSuggestSettings", returnLabels = { "Settings" }, returnTypes = { ReplayFitnessSetting.class }, parameterLabels = {}, userAccessible = true)
   // @UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "T. Yuliani and H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
     public ReplayFitnessSetting suggestSettings(PluginContext context, XLog log, Petrinet net) {
 	ReplayFitnessSetting settings = new ReplayFitnessSetting();
 	suggestActions(settings, log, net);
 	return settings;
     }
/* 
    @Visualizer
 	@Plugin(name = "Fitness Result Visualizer", parameterLabels = "String", returnLabels = "Label of String", returnTypes = JComponent.class)
 	public static JComponent visualize(PluginContext context, TotalFitnessResult tovisualize) {
 		return StringVisualizer.visualize(context, tovisualize.toString());
 	}*/
 
 
 
 }
