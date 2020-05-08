 package engine.handler.rule;
 
 import java.awt.Color;
 import java.awt.geom.Point2D;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 
 import persistence.Persistence;
 import petrinet.Arc;
 import petrinet.ElementType;
 import petrinet.INode;
 import petrinet.IRenew;
 import petrinet.Petrinet;
 import petrinet.Place;
 import petrinet.RenewCount;
 import petrinet.RenewId;
 import petrinet.Renews;
 import petrinet.Transition;
 import transformation.Rule;
 import transformation.TransformationComponent;
 import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
 import engine.attribute.ArcAttribute;
 import engine.attribute.NodeLayoutAttribute;
 import engine.attribute.PlaceAttribute;
 import engine.attribute.RuleAttribute;
 import engine.attribute.TransitionAttribute;
 import engine.data.JungData;
 import engine.data.PetrinetData;
 import engine.data.RuleData;
 import engine.handler.NodeTypeEnum;
 import engine.handler.RuleNet;
 import engine.handler.petrinet.PetrinetPersistence;
 import engine.session.SessionManager;
 import exceptions.EngineException;
 
 final public class RuleHandler {
 
 	private final SessionManager sessionManager;
 	private static RuleHandler ruleManipulation;
 
 	private RuleHandler() {
 		sessionManager = SessionManager.getInstance();
 	}
 
 	protected static RuleHandler getInstance() {
 		if (ruleManipulation == null) {
 			ruleManipulation = new RuleHandler();
 		}
 
 		return ruleManipulation;
 	}
 
 	// Really Ugly Method
 	public Arc createArc(int id, RuleNet net, INode from, INode to)
 			throws EngineException {
 
 		RuleData ruleData = sessionManager.getRuleData(id);
 
 		if (ruleData == null) {
 			exception("createArc - id of the Rule is wrong");
 			return null;
 		} else {
 			Rule rule = ruleData.getRule();
 			Petrinet petrinet = null;
 			JungData lJungData = ruleData.getLJungData();
 			JungData kJungData = ruleData.getKJungData();
 			JungData rJungData = ruleData.getRJungData();
 
 			if (net.equals(RuleNet.L)) {
 				// Manipulation in L
 				// 
 				petrinet = rule.getL();
 				
 				if (this.getNodeType(from).equals(NodeTypeEnum.Place)
 						&& this.getNodeType(to).equals(NodeTypeEnum.Transition)) {
 					// place => transition
 
 					// cast objects
 					Place fromPlace = (Place) from;
 					Transition toTransition = (Transition) to;
 
 					// create new Arc
 					Arc arc = petrinet.createArc("undefined", fromPlace,
 							toTransition);
 
 					// call Jung
 					try {
 						lJungData.createArc(arc, fromPlace, toTransition);
 					} catch (IllegalArgumentException e) {
 						exception("createArc - can not create Arc in L");
 					}
 					
 					//get automatically added Corresponding Arc in K
 					Arc newArcInK = rule.fromLtoK(arc);
 					
 					//Add this Arc into the JungData of K
 					if (newArcInK != null){
 						Place fromInK = (Place) newArcInK.getStart();
 						Transition toInK = (Transition) newArcInK.getEnd();
 						try {
 							kJungData.createArc(newArcInK, fromInK, toInK);
 						} catch (IllegalArgumentException e) {
 							exception("createArc - can not create Arc in K");
 						}
 					}
 
 					return arc;
 				} else if (this.getNodeType(from).equals(NodeTypeEnum.Transition)
 						&& this.getNodeType(to).equals(NodeTypeEnum.Place)) {
 					// transition => place
 
 					// cast objects
 					Transition fromTransition = (Transition) from;
 					Place toPlace = (Place) to;
 
 					// create new Arc
 					Arc arc = petrinet.createArc("undefined", fromTransition,
 							toPlace);
 
 					// ***************************
 					// TODO: ChangedPetrinetResult
 					// discuss: what to do
 					// ***************************
 
 					// call Jung
 					try {
 						lJungData.createArc(arc, fromTransition, toPlace);
 					} catch (IllegalArgumentException e) {
 						exception("createArc - can not create Arc");
 					}
 					
 					//get automatically added Corresponding Arc in K
 					Arc newArcInK = rule.fromLtoK(arc);
 					
 					//Add this Arc into the JungData of K
 					if (newArcInK != null){
 						Transition fromInK = (Transition) newArcInK.getStart();
 						Place toInK = (Place) newArcInK.getEnd();
 						try {
 							kJungData.createArc(newArcInK, fromInK, toInK);
 						} catch (IllegalArgumentException e) {
 							exception("createArc - can not create Arc");
 						}
 					}
 					return arc;
 				} else {
 					exception("createArc - wrong combi");
 
 					return null;
 				}
 				
 			} else if (net.equals(RuleNet.K)) {
 				// Manipulation in K
 				// Get Petrinet and corresponding JungData
 				petrinet = rule.getK();
 				
 				if (this.getNodeType(from).equals(NodeTypeEnum.Place)
 						&& this.getNodeType(to).equals(NodeTypeEnum.Transition)) {
 					// place => transition
 
 					// cast objects
 					Place fromPlace = (Place) from;
 					Transition toTransition = (Transition) to;
 
 					// create new Arc
 					Arc arc = petrinet.createArc("undefined", fromPlace,
 							toTransition);
 
 					// call Jung
 					try {
 						kJungData.createArc(arc, fromPlace, toTransition);
 					} catch (IllegalArgumentException e) {
 						exception("createArc - can not create Arc in K");
 					}
 					
 					//get automatically added Corresponding Arc in L and R
 					Arc newArcInL = rule.fromKtoL(arc);
 					Arc newArcInR = rule.fromKtoR(arc);
 					
 					//Add this Arc into the JungData of L and R
 					if (newArcInL != null && newArcInR != null){
 						Place fromInL = (Place) newArcInL.getStart();
 						Transition toInL = (Transition) newArcInL.getEnd();
 						Place fromInR = (Place) newArcInR.getStart();
 						Transition toInR = (Transition) newArcInR.getEnd();
 						try {
 							lJungData.createArc(newArcInL, fromInL, toInL); 
 							rJungData.createArc(newArcInR, fromInR, toInR); 
 						} catch (IllegalArgumentException e) {
 							exception("createArc - can not create Arc in L or R");
 						}
 					}
 
 					return arc;
 				} else if (this.getNodeType(from).equals(NodeTypeEnum.Transition)
 						&& this.getNodeType(to).equals(NodeTypeEnum.Place)) {
 					// transition => place
 
 					// cast objects
 					Transition fromTransition = (Transition) from;
 					Place toPlace = (Place) to;
 
 					// create new Arc
 					Arc arc = petrinet.createArc("undefined", fromTransition,
 							toPlace);
 
 					// ***************************
 					// TODO: ChangedPetrinetResult
 					// discuss: what to do
 					// ***************************
 
 					// call Jung
 					try {
 						kJungData.createArc(arc, fromTransition, toPlace);
 					} catch (IllegalArgumentException e) {
 						exception("createArc - can not create Arc in K");
 					}
 					
 					//get automatically added Corresponding Arc in K
 					Arc newArcInL = rule.fromKtoL(arc);
 					Arc newArcInR = rule.fromKtoR(arc);
 					
 					//Add this Arc into the JungData of K
 					if (newArcInL != null && newArcInR != null){
 						Transition fromInL = (Transition) newArcInL.getStart();
 						Place toInL = (Place) newArcInL.getEnd();
 						Transition fromInR = (Transition) newArcInR.getStart();
 						Place toInR = (Place) newArcInR.getEnd();
 						try {
 							lJungData.createArc(newArcInL, fromInL, toInL);
 							rJungData.createArc(newArcInR, fromInR, toInR); 
 						} catch (IllegalArgumentException e) {
 							e.printStackTrace();
 							exception("createArc - can not create Arc in L or R");
 						}
 					}
 					return arc;
 				} else {
 					exception("createArc - wrong combi");
 
 					return null;
 				}
 				
 			} else if (net.equals(RuleNet.R)) {
 				// Manipulation in R
 				// Get Petrinet and corresponding JungData
 				petrinet = rule.getR();
 				
 				if (this.getNodeType(from).equals(NodeTypeEnum.Place)
 						&& this.getNodeType(to).equals(NodeTypeEnum.Transition)) {
 					// place => transition
 
 					// cast objects
 					Place fromPlace = (Place) from;
 					Transition toTransition = (Transition) to;
 
 					// create new Arc
 					Arc arc = petrinet.createArc("undefined", fromPlace,
 							toTransition);
 
 					// call Jung
 					try {
 						rJungData.createArc(arc, fromPlace, toTransition);
 					} catch (IllegalArgumentException e) {
 						exception("createArc - can not create Arc in R");
 					}
 					
 					//get automatically added Corresponding Arc in K
 					Arc newArcInK = rule.fromRtoK(arc);
 					
 					//Add this Arc into the JungData of K
 					if (newArcInK != null){
 						Place fromInK = (Place) newArcInK.getStart();
 						Transition toInK = (Transition) newArcInK.getEnd();
 						try {
 							kJungData.createArc(newArcInK, fromInK, toInK);
 						} catch (IllegalArgumentException e) {
 							exception("createArc - can not create Arc in K");
 						}
 					}
 
 					return arc;
 				} else if (this.getNodeType(from).equals(NodeTypeEnum.Transition)
 						&& this.getNodeType(to).equals(NodeTypeEnum.Place)) {
 					// transition => place
 
 					// cast objects
 					Transition fromTransition = (Transition) from;
 					Place toPlace = (Place) to;
 
 					// create new Arc
 					Arc arc = petrinet.createArc("undefined", fromTransition,
 							toPlace);
 
 					// ***************************
 					// TODO: ChangedPetrinetResult
 					// discuss: what to do
 					// ***************************
 
 					// call Jung
 					try {
 						rJungData.createArc(arc, fromTransition, toPlace);
 					} catch (IllegalArgumentException e) {
 						exception("createArc - can not create Arc in R");
 					}
 					
 					//get automatically added Corresponding Arc in K
 					Arc newArcInK = rule.fromRtoK(arc);
 					
 					//Add this Arc into the JungData of K
 					if (newArcInK != null){
 						Transition fromInK = (Transition) newArcInK.getStart();
 						Place toInK = (Place) newArcInK.getEnd();
 						try {
 							kJungData.createArc(newArcInK, fromInK, toInK);
 						} catch (IllegalArgumentException e) {
 							exception("createArc - can not create Arc in K");
 						}
 					}
 					return arc;
 					
 				} else {
 					exception("createArc - wrong combi");
 
 					return null;
 				}
 				
 			} else {
 				exception("createArc - Not given if Manipulation is in L,K or R");
 				return null;
 			}
 			
 
 		}
 		
 	}
 
 	public INode createPlace(int id, RuleNet net, Point2D coordinate)
 			throws EngineException {
 
 		RuleData ruleData = sessionManager.getRuleData(id);
 
 		if (ruleData == null) {
 			exception("createPlace - id of the Rule is wrong");
 			return null;
 		} else {
 			Rule rule = ruleData.getRule();
 			Petrinet petrinet = null;
 			JungData lJungData = ruleData.getLJungData();
 			JungData kJungData = ruleData.getKJungData();
 			JungData rJungData = ruleData.getRJungData();
 
 			if (net.equals(RuleNet.L)) {
 				// Manipulation in L
 				//
 				petrinet = rule.getL();
 				
 				// create a new Place
 				Place newPlace = petrinet.createPlace("undefined");
 
 				// call JungModificator
 				try {
 					lJungData.createPlace(newPlace, coordinate);
 				} catch (IllegalArgumentException e) {
 					exception("createPlace - can not create Place in L");
 				}
 				
 				//get automatically added Corresponding Place in K
 				Place newPlaceInK = (Place) rule.fromLtoK(newPlace);
 				if(newPlaceInK != null){
 					try {
 						kJungData.createPlace(newPlaceInK, coordinate);
 					} catch (IllegalArgumentException e) {
 						exception("createPlace - can not create Place in K");
 					}
 				}
 
 				return newPlace;
 				
 				
 			} else if (net.equals(RuleNet.K)) {
 				// Manipulation in K
 				// 
 				petrinet = rule.getK();
 				
 				// create a new Place
 				Place newPlace = petrinet.createPlace("undefined");
 
 				// call JungModificator
 				try {
 					kJungData.createPlace(newPlace, coordinate);
 				} catch (IllegalArgumentException e) {
 					exception("createPlace - can not create Place in K");
 				}
 				
 				//get automatically added Corresponding Place in L and R
 				Place newPlaceInL = (Place) rule.fromKtoL(newPlace);
 				Place newPlaceInR = (Place) rule.fromKtoR(newPlace);
 				if(newPlaceInL != null && newPlaceInR != null){
 					try {
 						lJungData.createPlace(newPlaceInL, coordinate);
 						rJungData.createPlace(newPlaceInR, coordinate);
 					} catch (IllegalArgumentException e) {
 						exception("createPlace - can not create Place in K");
 					}
 				}
 
 				return newPlace;
 				
 			} else if (net.equals(RuleNet.R)) {
 				// Manipulation in R
 				// Get Petrinet and corresponding JungData
 				petrinet = rule.getR();
 				
 				// create a new Place
 				Place newPlace = petrinet.createPlace("undefined");
 
 				// call JungModificator
 				try {
 					rJungData.createPlace(newPlace, coordinate);
 				} catch (IllegalArgumentException e) {
 					exception("createPlace - can not create Place in R");
 				}
 				
 				//get automatically added Corresponding Place in K
 				Place newPlaceInK = (Place) rule.fromRtoK(newPlace);
 				if(newPlaceInK != null){
 					try {
 						kJungData.createPlace(newPlaceInK, coordinate);
 					} catch (IllegalArgumentException e) {
 						exception("createPlace - can not create Place in K");
 					}
 				}
 
 				return newPlace;
 				
 			} else {
 				exception("createPlace - Not given if Manipulation is in L,K or R");
 				return null;
 			}
 
 
 		}
 
 	}
 
 	public int createRule() {
 
 		Rule rule = TransformationComponent.getTransformation().createRule();
 
 		RuleData ruleData = sessionManager.createRuleData(rule);
 
 		return ruleData.getId();
 	}
 
 	public INode createTransition(int id, RuleNet net, Point2D coordinate)
 			throws EngineException {
 
 		// get the RuleData from the id and SessionManager
 		RuleData ruleData = sessionManager.getRuleData(id);
 
 		// Test: is id valid
 		if (ruleData == null) {
 			exception("createTransition - id of the Rule is wrong");
 			return null;
 		} else {
 			Rule rule = ruleData.getRule();
 			Petrinet petrinet = null;
 			JungData lJungData = ruleData.getLJungData();
 			JungData kJungData = ruleData.getKJungData();
 			JungData rJungData = ruleData.getRJungData();
 
 			if (net.equals(RuleNet.L)) {
 				// Manipulation in L
 				// 
 				petrinet = rule.getL();
 				
 				// create a new Place
 				Transition newTransition = petrinet.createTransition("undefined");
 
 				// call JungModificator
 				try {
 					lJungData.createTransition(newTransition, coordinate);
 				} catch (IllegalArgumentException e) {
 					exception("createTransition - can not create Transition in L");
 				}
 				
 				//get automatically added Corresponding Place in K
 				Transition newTransitionInK = (Transition) rule.fromLtoK(newTransition);
 				if(newTransitionInK != null){
 					try {
 						kJungData.createTransition(newTransitionInK, coordinate);
 					} catch (IllegalArgumentException e) {
 						exception("createPlace - can not create Transition in K");
 					}
 				}
 
 				return newTransition;
 				
 			} else if (net.equals(RuleNet.K)) {
 				// Manipulation in K
 				//
 				petrinet = rule.getK();
 				
 				// create a new Transition
 				Transition newTransition = petrinet.createTransition("undefined");
 
 				// call JungModificator
 				try {
 					kJungData.createTransition(newTransition, coordinate);
 				} catch (IllegalArgumentException e) {
 					exception("createTransition - can not create Transition in K");
 				}
 				
 				//get automatically added Corresponding Transition in L and R
 				Transition newTransitionInL = (Transition) rule.fromKtoL(newTransition);
 				Transition newTransitionInR = (Transition) rule.fromKtoR(newTransition);
 				if(newTransitionInL != null && newTransitionInR != null){
 					try {
 						lJungData.createTransition(newTransitionInL, coordinate);
 						rJungData.createTransition(newTransitionInR, coordinate);
 					} catch (IllegalArgumentException e) {
 						exception("createTransition - can not create Transition in L or R");
 					}
 				}
 
 				return newTransition;
 				
 			} else if (net.equals(RuleNet.R)) {
 				// Manipulation in R
 				// 
 				petrinet = rule.getR();
 				
 				// create a new Transition
 				Transition newTransition = petrinet.createTransition("undefined");
 
 				// call JungModificator
 				try {
 					rJungData.createTransition(newTransition, coordinate);
 				} catch (IllegalArgumentException e) {
 					exception("createTransition - can not create Transition in R");
 				}
 				
 				//get automatically added Corresponding Transition in K
 				Transition newTransitionInK = (Transition) rule.fromRtoK(newTransition);
 				if(newTransitionInK != null){
 					try {
 						kJungData.createTransition(newTransitionInK, coordinate);
 					} catch (IllegalArgumentException e) {
 						exception("createTransition - can not create Transition in K");
 					}
 				}
 
 				return newTransition;
 				
 			} else {
 				exception("createTransition - Not given if Manipulation is in L,K or R");
 				return null;
 			}
 
 		}
 
 	}
 
 	public void deleteArc(int id, RuleNet net, Arc arc) throws EngineException {
 		// get the RuleData from the id and SessionManager
 		RuleData ruleData = sessionManager.getRuleData(id);
 
 		// Test: is id valid
 		if (ruleData == null) {
 			exception("deleteArc - id of the Rule is wrong");
 		} else {
 			Rule rule = ruleData.getRule();
 			Petrinet petrinet = null;
 			JungData jungData = null;
 
 			if (net.equals(RuleNet.L)) {
 				// Manipulation in L
 				// Get Petrinet and corresponding JungData
 				petrinet = rule.getL();
 				jungData = ruleData.getLJungData();
 			} else if (net.equals(RuleNet.K)) {
 				// Manipulation in K
 				// Get Petrinet and corresponding JungData
 				petrinet = rule.getK();
 				jungData = ruleData.getKJungData();
 			} else if (net.equals(RuleNet.R)) {
 				// Manipulation in R
 				// Get Petrinet and corresponding JungData
 				petrinet = rule.getR();
 				jungData = ruleData.getRJungData();
 			} else {
 				exception("deleteArc - Not given if Manipulation is in L,K or R");
 			}
 			petrinet.deleteElementById(arc.getId());
 
 			// call JundModification and create Collection
 			Collection<Arc> collArc = new HashSet<Arc>();
 			collArc.add(arc);
 
 			Collection<INode> collINodes = new HashSet<INode>();
 
 			try {
 				jungData.delete(collArc, collINodes);
 			} catch (IllegalArgumentException e) {
 				exception("deleteArc - can not delete Arc");
 			}
 		}
 	}
 
 	public void deletePlace(int id, RuleNet net, INode place) throws EngineException {
 		deleteInternal(id, net, place);
 	}
 
 	public void deleteTransition(int id, RuleNet net, INode transition) throws EngineException {
 		deleteInternal(id, net, transition);
 	}
 
 	private void deleteInternal(int id, RuleNet net, INode node) throws EngineException {
 
 		// get the RuleData from the id and SessionManager
 		RuleData ruleData = sessionManager.getRuleData(id);
 
 		// Test: is id valid
 		if (ruleData == null) {
 			exception("deleteInternal - id of the Rule is wrong");
 		} else {
 			Rule rule = ruleData.getRule();
 			Petrinet petrinet = null;
 			JungData jungData = null;
 
 			if (net.equals(RuleNet.L)) {
 				// Manipulation in L
 				// Get Petrinet and corresponding JungData
 				petrinet = rule.getL();
 				jungData = ruleData.getLJungData();
 			} else if (net.equals(RuleNet.K)) {
 				// Manipulation in K
 				// Get Petrinet and corresponding JungData
 				petrinet = rule.getK();
 				jungData = ruleData.getKJungData();
 			} else if (net.equals(RuleNet.R)) {
 				// Manipulation in R
 				// Get Petrinet and corresponding JungData
 				petrinet = rule.getR();
 				jungData = ruleData.getRJungData();
 			} else {
 				exception("deleteInternal - Not given if Manipulation is in L,K or R");
 			}
 
 			// all deleted elements, returned by Petrinet
 			Collection<Integer> allDelElemFromPetrinet = petrinet
 					.deleteElementById(node.getId());
 
 			Iterator<Integer> iter = allDelElemFromPetrinet.iterator();
 			Collection<Arc> collArc = new HashSet<Arc>();
 			Collection<INode> collINodes = new HashSet<INode>();
 
 			while (iter.hasNext()) {
 				int idOfElem = iter.next();
 				ElementType type = petrinet.getNodeType(idOfElem);
 
 				// test type of Element
				if (type.equals(ElementType.ARC)) {
 					collArc.add(petrinet.getArcById(idOfElem));
				} else if (type.equals(ElementType.PLACE)) {
 					collINodes.add(petrinet.getPlaceById(idOfElem));
				} else if (type.equals(ElementType.TRANSITION)) {
 					collINodes.add(petrinet.getTransitionById(idOfElem));
 				} else {
 					exception("deleteInternal - invalid type from id");
 				}
 
 			}
 
 			try {
 				jungData.delete(collArc, collINodes);
 			} catch (IllegalArgumentException e) {
 				exception("deleteInternal - can not delete Place/Transition");
 			}
 
 		}
 
 	}
 
 	public ArcAttribute getArcAttribute(int id, Arc arc) {
 
 		int weight = arc.getMark();
 
 		ArcAttribute arcAttribute = new ArcAttribute(weight);
 
 		return arcAttribute;
 
 	}
 
 	public AbstractLayout<INode, Arc> getJungLayout(int id, RuleNet net) throws EngineException {
 
 		// get the RuleData from the id and SessionManager
 		RuleData ruleData = sessionManager.getRuleData(id);
 
 		// Test: is id valid
 		if (ruleData == null) {
 			exception("getJungLayout - id of the Rule is wrong");
 		} else {
 			//Rule rule = ruleData.getRule();
 			JungData jungData = null;
 
 			if (net.equals(RuleNet.L)) {
 				// Manipulation in L
 				// Get JungData
 				jungData = ruleData.getLJungData();
 			} else if (net.equals(RuleNet.K)) {
 				// Manipulation in K
 				// Get JungData
 				jungData = ruleData.getKJungData();
 			} else if (net.equals(RuleNet.R)) {
 				// Manipulation in R
 				// Get JungData
 				jungData = ruleData.getRJungData();
 			} else {
 				exception("getJungLayout - Not given if Manipulation is in L,K or R");
 			}
 			return jungData.getJungLayout();
 		}
 
 		return null;
 	}
 
 	public PlaceAttribute getPlaceAttribute(int id, INode place)
 			throws EngineException {
 
 		if (this.getNodeType(place).equals(NodeTypeEnum.Place)) {
 			Place p = (Place) place;
 
 			int marking = p.getMark();
 			String pname = p.getName();
 
 			// TODO : change default color
 			Color color = Color.gray;
 
 			PlaceAttribute placeAttribute = new PlaceAttribute(marking, pname,
 					color);
 
 			return placeAttribute;
 		}
 
 		exception("getPlaceAttribute - the INode value is not a Place");
 		return null;
 	}
 
 	public TransitionAttribute getTransitionAttribute(int id, INode transition)
 			throws EngineException {
 
 		if (this.getNodeType(transition).equals(NodeTypeEnum.Transition)) {
 			Transition t = (Transition) transition;
 
 			String tlb = t.getTlb();
 			String tname = t.getName();
 			IRenew rnw = t.getRnw();
 
 			// TODO : change default boolean
 			boolean isActivated = t.isActivated();
 
 			TransitionAttribute transitionAttribute = new TransitionAttribute(
 					tlb, tname, rnw, isActivated);
 
 			return transitionAttribute;
 		}
 
 		exception("getPlaceAttribute - the INode value is not a Transition");
 		return null;
 	}
 
 	public RuleAttribute getRuleAttribute(int id) throws EngineException {
 
 		// get the RuleData from the id and SessionManager
 		RuleData ruleData = sessionManager.getRuleData(id);
 
 		// Test: is id valid
 		if (ruleData == null) {
 			exception("getRuleAttribute - id of the rule is wrong");
 		} else {
 			// get all Id's from L, K, R
 			int lId = ruleData.getRule().getL().getId();
 			int kId = ruleData.getRule().getK().getId();
 			int rId = ruleData.getRule().getR().getId();
 
 			// create a RuleAttribute
 			RuleAttribute ruleAttribute = new RuleAttribute(lId, kId, rId);
 
 			return ruleAttribute;
 		}
 
 		return null;
 	}
 
 	public void moveNode(int id, INode node, Point2D relativePosition)
 			throws EngineException {
 
 		// get the RuleData from the id and SessionManager
 		RuleData ruleData = sessionManager.getRuleData(id);
 
 		// Test: is id valid
 		if (ruleData == null) {
 			exception("moveNode - id of the rule is wrong");
 		} else {
 
 			// get all jungdata => l, k, r
 			JungData lJungData = ruleData.getLJungData();
 			JungData kJungData = ruleData.getKJungData();
 			JungData rJungData = ruleData.getRJungData();
 
 			Map<INode, NodeLayoutAttribute> lLayoutMap = lJungData
 					.getNodeLayoutAttributes();
 			Map<INode, NodeLayoutAttribute> kLayoutMap = kJungData
 					.getNodeLayoutAttributes();
 			Map<INode, NodeLayoutAttribute> rLayoutMap = rJungData
 					.getNodeLayoutAttributes();
 
 			// move node in l
 			moveNodeInternal(lJungData, lLayoutMap, node, relativePosition);
 
 			// move node in k
 			moveNodeInternal(kJungData, kLayoutMap, node, relativePosition);
 
 			// move node in r
 			moveNodeInternal(rJungData, rLayoutMap, node, relativePosition);
 
 		}
 
 	}
 
 	/**
 	 * 
 	 * This is a helper method for moveNode. It calculate the new position.
 	 * 
 	 * @param jungData
 	 * @param layoutMap
 	 * @param node
 	 * @param relativePosition
 	 * @throws EngineException
 	 * 
 	 */
 	private void moveNodeInternal(JungData jungData,
 			Map<INode, NodeLayoutAttribute> layoutMap, INode node,
 			Point2D relativePosition) throws EngineException {
 
 		NodeLayoutAttribute nla = layoutMap.get(node);
 
 		double newPointX = nla.getCoordinate().getX() + relativePosition.getX();
 		double newPointY = nla.getCoordinate().getY() + relativePosition.getY();
 
 		if (newPointX < 0 || newPointY < 0) {
 
 			double x = relativePosition.getX() - newPointX;
 			double y = relativePosition.getY() - newPointY;
 
 			Point2D point = new Point2D.Double(x, y);
 
 			try {
 				jungData.moveNode(node, point);
 			} catch (IllegalArgumentException e) {
 				exception("moveNodeInternal - error: moveNode");
 			}
 
 		} else {
 
 			Point2D point = new Point2D.Double(newPointX, newPointY);
 
 			try {
 				jungData.moveNode(node, point);
 			} catch (IllegalArgumentException e) {
 				exception("moveNodeInternal - error: moveNode");
 			}
 
 		}
 
 	}
 
 	public void save(int id, String path, String filename, String format) throws EngineException {
 
 		// get the Petrinet from the id and SessionManager
 		PetrinetData petrinetData = sessionManager.getPetrinetData(id);
 
 		// Test: is id valid
 		if (petrinetData == null) {
 			exception("save - id of the Petrinet is wrong");
 		} else {
 			
 			Petrinet petrinet = petrinetData.getPetrinet();
 			JungData jungData = petrinetData.getJungData();
 			
 			Map<INode, NodeLayoutAttribute> nodeMap = jungData.getNodeLayoutAttributes();
 			
 			checkNodeLayoutAttribute(nodeMap == null, "save - nodeMap == null");
 			
 			Persistence.saveRule(path + "/" + filename + "." + format, petrinet, nodeMap);
 
 		}
 		
 	}
 	
 	public int load(String path, String filename){
 		
 		return Persistence.loadRule(path + "/" + filename, PetrinetPersistence.getInstance());
 		
 	}
 
 	public void setMarking(int id, INode place, int marking)
 			throws EngineException {
 
 		// get the Petrinet from the id and SessionManager
 		RuleData ruleData = sessionManager.getRuleData(id);
 
 		// Test: is id valid
 		if (ruleData == null) {
 			exception("setMarking - id of the rule is wrong");
 		} else {
 
 			Rule rule = ruleData.getRule();
 			if (this.getNodeType(place).equals(NodeTypeEnum.Place)) {
 				// cast object
 				Place p = (Place) place;
 
 				// set new marking
 				p.setMark(marking);
 				
 				//Synchronize Places in the other parts of the rules
 				RuleNet net = getContainingNet(id, place);
 				if(net.equals(RuleNet.L)){
 					Place placeInK = (Place) rule.fromLtoK(p);
 					placeInK.setMark(marking);
 				} else if (net.equals(RuleNet.K)){
 					Place placeInL = (Place) rule.fromKtoL(p);
 					Place placeInR = (Place) rule.fromKtoR(p);
 					placeInL.setMark(marking);
 					placeInR.setMark(marking);
 				} else if(net.equals(RuleNet.R)){
 					Place placeInK = (Place) rule.fromRtoK(p);
 					placeInK.setMark(marking);
 				}
 
 			}
 
 		}
 
 	}
 
 	public void setPname(int id, INode place, String pname)
 			throws EngineException {
 
 		// get the RuleData from the id and SessionManager
 		RuleData ruleData = sessionManager.getRuleData(id);
 
 		// Test: is id valid
 		if (ruleData == null) {
 			exception("setPname - id of the rule is wrong");
 		} else {
 
 
 			Rule rule = ruleData.getRule();
 			if (this.getNodeType(place).equals(NodeTypeEnum.Place)) {
 				// cast object
 				Place p = (Place) place;
 
 				// set new Pname
 				p.setName(pname);
 				
 				//Synchronize Places in the other parts of the rules
 				RuleNet net = getContainingNet(id, place);
 				if(net.equals(RuleNet.L)){
 					Place placeInK = (Place) rule.fromLtoK(p);
 					placeInK.setName(pname);
 				} else if (net.equals(RuleNet.K)){
 					Place placeInL = (Place) rule.fromKtoL(p);
 					Place placeInR = (Place) rule.fromKtoR(p);
 					placeInL.setName(pname);
 					placeInR.setName(pname);
 				} else if(net.equals(RuleNet.R)){
 					Place placeInK = (Place) rule.fromRtoK(p);
 					placeInK.setName(pname);
 				}
 			}
 
 		}
 
 	}
 	
 	public void setPlaceColor(int id, INode place, Color color) throws EngineException {
 		
 		// get the RuleData from the id and SessionManager
 		RuleData ruleData = sessionManager.getRuleData(id);
 
 		// Test: is id valid
 		if (ruleData == null) {
 			exception("setColor - id of the Rule is wrong");
 		} else {
 			Rule rule = ruleData.getRule();
 			JungData lJungData = ruleData.getLJungData();
 			JungData kJungData = ruleData.getKJungData();
 			JungData rJungData = ruleData.getRJungData();
 			if (this.getNodeType(place).equals(NodeTypeEnum.Place)) {
 				Place p = (Place) place;
 				
 				//Synchronize Places in the other parts of the rules
 				RuleNet net = getContainingNet(id, place);
 				if(net.equals(RuleNet.L)){
 					lJungData.setPlaceColor(p, color);
 					Place placeInK = (Place) rule.fromLtoK(p);
 					kJungData.setPlaceColor(placeInK, color);
 				} else if (net.equals(RuleNet.K)){
 					kJungData.setPlaceColor(p, color);
 					Place placeInL = (Place) rule.fromKtoL(p);
 					Place placeInR = (Place) rule.fromKtoR(p);
 					lJungData.setPlaceColor(placeInL, color);
 					rJungData.setPlaceColor(placeInR, color);
 				} else if(net.equals(RuleNet.R)){
 					rJungData.setPlaceColor(p, color);
 					Place placeInK = (Place) rule.fromRtoK(p);
 					kJungData.setPlaceColor(placeInK, color);
 				}
 			}
 		}
 	}
 
 	public void setTlb(int id, INode transition, String tlb)
 			throws EngineException {
 
 		// get the RuleData from the id and SessionManager
 		RuleData ruleData = sessionManager.getRuleData(id);
 
 		// Test: is id valid
 		if (ruleData == null) {
 			exception("setTlb - id of the rule is wrong");
 		} else {
 
 			Rule rule = ruleData.getRule();
 			if (this.getNodeType(transition).equals(NodeTypeEnum.Transition)) {
 				// cast object
 				Transition t = (Transition) transition;
 
 				// set new Tlb
 				t.setTlb(tlb);
 				
 				//Synchronize Transitions in the other parts of the rules
 				RuleNet net = getContainingNet(id, transition);
 				if(net.equals(RuleNet.L)){
 					Transition transitionInK = (Transition) rule.fromLtoK(t);
 					transitionInK.setTlb(tlb);
 				} else if (net.equals(RuleNet.K)){
 					Transition transitionInL = (Transition) rule.fromKtoL(t);
 					Transition transitionInR = (Transition) rule.fromKtoR(t);
 					transitionInL.setTlb(tlb);
 					transitionInR.setTlb(tlb);
 				} else if(net.equals(RuleNet.R)){
 					Transition transitionInK = (Transition) rule.fromRtoK(t);
 					transitionInK.setTlb(tlb);
 				}
 
 			}
 
 		}
 
 	}
 
 	public void setTname(int id, INode transition, String tname)
 			throws EngineException {
 
 		// get the RuleData from the id and SessionManager
 		RuleData ruleData = sessionManager.getRuleData(id);
 
 		// Test: is id valid
 		if (ruleData == null) {
 			exception("setTname - id of the rule is wrong");
 		} else {
 
 			Rule rule = ruleData.getRule();
 			if (this.getNodeType(transition).equals(NodeTypeEnum.Transition)) {
 				// cast object
 				Transition t = (Transition) transition;
 
 				// set new Tname
 				t.setName(tname);
 				
 				//Synchronize Transitions in the other parts of the rules
 				RuleNet net = getContainingNet(id, transition);
 				if(net.equals(RuleNet.L)){
 					Transition transitionInK = (Transition) rule.fromLtoK(t);
 					transitionInK.setName(tname);
 				} else if (net.equals(RuleNet.K)){
 					Transition transitionInL = (Transition) rule.fromKtoL(t);
 					Transition transitionInR = (Transition) rule.fromKtoR(t);
 					transitionInL.setName(tname);
 					transitionInR.setName(tname);
 				} else if(net.equals(RuleNet.R)){
 					Transition transitionInK = (Transition) rule.fromRtoK(t);
 					transitionInK.setName(tname);
 				}
 
 			}
 
 		}
 
 	}
 
 	public void setRnw(int id, INode transition, Renews renews)
 			throws EngineException {
 
 		// get the RuleData from the id and SessionManager
 		RuleData ruleData = sessionManager.getRuleData(id);
 
 		// Test: is id valid
 		if (ruleData == null) {
 			exception("setWeight - id of the Rule is wrong");
 		} else {
 			Rule rule = ruleData.getRule();
 			if (this.getNodeType(transition).equals(NodeTypeEnum.Transition)) {
 				Transition t = (Transition) transition;
 
 				IRenew rnw = null;
 
 				if (renews.equals(Renews.COUNT)) {
 					rnw = new RenewCount();
 				} else if (renews.equals(Renews.IDENTITY)) {
 					rnw = new RenewId();
 					// TODO Renew as Map<String,String>
 				} else {
 					exception("setRnw - renews is not correct");
 				}
 				t.setRnw(rnw);
 				
 				//Synchronize Transitions in the other parts of the rules
 				RuleNet net = getContainingNet(id, transition);
 				if(net.equals(RuleNet.L)){
 					Transition transitionInK = (Transition) rule.fromLtoK(t);
 					transitionInK.setRnw(rnw);
 				} else if (net.equals(RuleNet.K)){
 					Transition transitionInL = (Transition) rule.fromKtoL(t);
 					Transition transitionInR = (Transition) rule.fromKtoR(t);
 					transitionInL.setRnw(rnw);
 					transitionInR.setRnw(rnw);
 				} else if(net.equals(RuleNet.R)){
 					Transition transitionInK = (Transition) rule.fromRtoK(t);
 					transitionInK.setRnw(rnw);
 				}
 			}
 		}
 	}
 	
 	public void setWeight(int id, Arc arc, int weight) throws EngineException {
 
 		// get the RuleData from the id and SessionManager
 		RuleData ruleData = sessionManager.getRuleData(id);
 
 		// Test: is id valid
 		if (ruleData == null) {
 			exception("setWeight - id of the Rule is wrong");
 		} else {
 
 			Rule rule = ruleData.getRule();
 			// set new weight
 			arc.setMark(weight);
 			
 			//Synchronize Arcs in the other parts of the rules
 			RuleNet net = getContainingNet(id, arc);
 			if(net.equals(RuleNet.L)){
 				Arc arcInK = rule.fromLtoK(arc);
 				arcInK.setMark(weight);
 			} else if (net.equals(RuleNet.K)){
 				Arc arcInL = rule.fromKtoL(arc);
 				Arc arcInR = rule.fromKtoR(arc);
 				arcInL.setMark(weight);
 				arcInR.setMark(weight);
 			} else if(net.equals(RuleNet.R)){
 				Arc arcInR = rule.fromRtoK(arc);
 				arcInR.setMark(weight);
 			}
 
 		}
 
 	}
 		
 	public NodeTypeEnum getNodeType(INode node) {
 
 		if (node instanceof Place) {
 			return NodeTypeEnum.Place;
 		} else if (node instanceof Transition) {
 			return NodeTypeEnum.Transition;
 		} else {
 			return null;
 		}
 
 	}
 
 	private void exception(String value) throws EngineException {
 		throw new EngineException("RuleHandler: " + value);
 	}
 	
 	private void checkNodeLayoutAttribute(boolean value, String errorMessage) throws EngineException{
 		if(value){
 			exception(errorMessage);
 		}
 	}	
 	
 	//Helper Method
 	private RuleNet getContainingNet(int id, INode node){
 		RuleData ruleData = sessionManager.getRuleData(id);
 		Rule rule = ruleData.getRule();
 		if(rule.getL().getAllPlaces().contains(node) || rule.getL().getAllTransitions().contains(node) || rule.getL().getAllArcs().contains(node)){
 			return RuleNet.L;
 		}
 		if(rule.getK().getAllPlaces().contains(node) || rule.getK().getAllTransitions().contains(node) || rule.getK().getAllArcs().contains(node)){
 			return RuleNet.K;
 		}
 		if(rule.getR().getAllPlaces().contains(node) || rule.getR().getAllTransitions().contains(node) || rule.getR().getAllArcs().contains(node)){
 			return RuleNet.K;
 		}
 		return null;
 	}
 	
 
 
 }
