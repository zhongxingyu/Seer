 package transformation;
 
 import petrinetze.IArc;
 import petrinetze.INode;
 import petrinetze.IPetrinet;
 import petrinetze.IPlace;
 import petrinetze.IRenew;
 import petrinetze.ITransition;
 import java.util.Set;
 
 public class Transformation implements ITransformation {
 	
 	private final IPetrinet N;
 	private final IMorphism morphism;
 	private final IRule rule; 
 	
 	/**
 	 * Constructor for the class Transformation
 	 * @param net,the petrinet to transform
 	 * @param morph, the morphism to use
 	 * @param r, the rule that should apply
 	 */	
 	public Transformation(IPetrinet net,IMorphism morph,IRule r){
 		N = net; morphism = morph; rule = r;
 		
 	}
 	/**
 	 * Constructor for the class Transformation
 	 * @param net, petrinet to transform
 	 * @param Rule, the rule to apply
 	 */	
 	public Transformation(IPetrinet net,IRule Rule){
 		N = net; 
 		rule = Rule;
 		morphism = MorphismFactory.createMorphism(N,Rule.L());
 		
 	}		
 	/**
 	 * Returns the IPetrinet of this transformation.
 	 * This net will be changed when transform() is called.
 	 * @return the IRule of this transformation.
 	 */
 	@Override
 	public IPetrinet N() {
 		return N;
 	}
 	
 	/**
 	 * Returns the IMorphism of this transformation.
 	 * @return the IMorphism of this transformation.
 	 */
 	@Override
 	public IMorphism morphism() {
 		return morphism;
 	}
 	
 	/**
 	 * Returns the IRule of this transformation.
 	 * @return the IRule of this transformation.
 	 */	
 	@Override
 	public IRule rule() {
 		return rule;
 	}		
 	/**
 	 * Method for starting the transform. 
 	 * 
 	 */
 	@Override
 	public void transform() {
 		IPetrinet K = rule.K();
 		Set<INode> KNode = K.getAllGraphElement().getAllNodes();
 		Set<IArc> KArc = K.getAllArcs();		
 		for (INode i : KNode)  // Add K - L Places
 		{
			if(rule.fromKtoL(i) == null) {
 				if(i instanceof IPlace){   
 					N.createPlace(i.getName());					
 				}
 				else{
 					IRenew rnw = ((ITransition) i).getRnw();
 					N.createTransition(i.getName(),rnw);
 				}
 			}				  
 		}
 		for (IArc a : KArc){ //Add K - L Arcs
			if(rule.fromKtoL(a) == null) {
 				if(a.getStart() instanceof IPlace){
 					N.createArc(a.getName(),morphism.morph((IPlace)a.getStart()),morphism.morph((ITransition)a.getEnd()));
 				}
 				else{
 					N.createArc(a.getName(),morphism.morph((ITransition)a.getStart()),morphism.morph((IPlace)a.getEnd()));
 				}	
 			}
 		}
 		for (INode i : KNode) // Delete K - R Places
 		{
			if (rule.fromKtoR(i) == null) {
 				if(i instanceof IPlace){
 					N.deletePlaceById(morphism.morph((IPlace)i).getId());
 				}
 				else {
 					N.deleteTransitionByID(morphism.morph((ITransition)i).getId());
 				}
 			}
 		}
 		for (IArc i : KArc){ // Delete K - R Arcs
			if (rule.fromKtoR(i) == null) {
 				N.deleteArcByID(morphism.morph(i).getId());
 			}			
 		}
 	}
 
 }
