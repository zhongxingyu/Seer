 package engine;
 
 import java.awt.Color;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 
 import petrinet.Arc;
 import petrinet.INode;
 import petrinet.Petrinet;
 import petrinet.Place;
 import petrinet.Renews;
 import petrinet.Transition;
 import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
 import engine.attribute.ArcAttribute;
 import engine.attribute.PlaceAttribute;
 import engine.attribute.TransitionAttribute;
 import engine.handler.NodeTypeEnum;
 import engine.handler.petrinet.PetrinetManipulation;
 import engine.ihandler.IPetrinetManipulation;
 import engine.session.SessionManager;
 import exceptions.EngineException;
 
 public class EngineMockupForPersistence implements IPetrinetManipulation {
 
 	private IPetrinetManipulation iPetrinetManipulation;
 	private final int id;
 	
 	public EngineMockupForPersistence() {
 		
 		iPetrinetManipulation = PetrinetManipulation.getInstance();
 		
 		id = iPetrinetManipulation.createPetrinet();
		System.out.println("Petrinet ID: " + id);
 	}
 	
 	public int build() throws EngineException {		
 		
 		createPlace(id, new Point2D.Double(10, 10));
 		createPlace(id, new Point2D.Double(10, 100));
 		createPlace(id, new Point2D.Double(100, 10));
 		createPlace(id, new Point2D.Double(100, 100));
 
 		createTransition(id, new Point2D.Double(55, 10));
 		createTransition(id, new Point2D.Double(10, 55));
 		createTransition(id, new Point2D.Double(100, 55));
 		createTransition(id, new Point2D.Double(55, 100));
 
		Petrinet petrinet = SessionManager.getInstance().getPetrinetData(id).getPetrinet();
 
 		List<Place> 	 places 	 = new ArrayList<Place>(petrinet.getAllPlaces());
 		List<Transition> transitions = new ArrayList<Transition>(petrinet.getAllTransitions());
 		
 		createArc(id, places.get(0),      transitions.get(0));
 		createArc(id, transitions.get(0), places.get(1));
 		
 		createArc(id, places.get(1),      transitions.get(1));
 		createArc(id, transitions.get(1), places.get(2));
 		
 		createArc(id, places.get(2),      transitions.get(2));
 		createArc(id, transitions.get(2), places.get(3));
 		
 		createArc(id, places.get(3),      transitions.get(3));
 		createArc(id, transitions.get(3), places.get(0));
 		
 		SessionManager.getInstance().getPetrinetData(id	).getPetrinet();
 		
 		return id;
 		
 	}
 	
 	public void saveTest(int id, String path, String filename, String format) throws EngineException{
 		
 		this.save(id, path, filename, format);
 		
 	}
 	
 	public void loadTest(String path, String filename){
 		
 		this.load(path, filename);
 		
 	}
 	
 	@Override
 	public void createArc(int id, INode from, INode to) throws EngineException {
 		
 		iPetrinetManipulation.createArc(id, from, to);
 		
 	}
 
 	@Override
 	public void createPlace(int id, Point2D coordinate) throws EngineException {
 		
 		iPetrinetManipulation.createPlace(id, coordinate);
 		
 	}
 
 	@Override
 	public int createPetrinet() {
 
 		return id;
 		
 	}
 
 	@Override
 	public void createTransition(int id, Point2D coordinate)
 			throws EngineException {
 		
 		iPetrinetManipulation.createTransition(id, coordinate);
 		
 	}
 
 	@Override
 	public void deleteArc(int id, Arc arc) throws EngineException {
 		
 		iPetrinetManipulation.deleteArc(id, arc);
 		
 	}
 
 	@Override
 	public void deletePlace(int id, INode place) throws EngineException {
 		
 		iPetrinetManipulation.deletePlace(id, place);
 		
 	}
 
 	@Override
 	public void deleteTransition(int id, INode transition)
 			throws EngineException {
 		
 		iPetrinetManipulation.deleteTransition(id, transition);
 		
 	}
 
 	@Override
 	public ArcAttribute getArcAttribute(int id, Arc arc) throws EngineException {
 		
 		return iPetrinetManipulation.getArcAttribute(id, arc);
 		
 	}
 
 	@Override
 	public AbstractLayout<INode, Arc> getJungLayout(int id)
 			throws EngineException {
 		
 		return iPetrinetManipulation.getJungLayout(id);
 		
 	}
 
 	@Override
 	public PlaceAttribute getPlaceAttribute(int id, INode place)
 			throws EngineException {
 
 		return iPetrinetManipulation.getPlaceAttribute(id, place);
 		
 	}
 
 	@Override
 	public TransitionAttribute getTransitionAttribute(int id, INode transition)
 			throws EngineException {
 
 		return iPetrinetManipulation.getTransitionAttribute(id, transition);
 		
 	}
 
 	@Override
 	public void moveGraph(int id, Point2D relativePosition)
 			throws EngineException {
 
 		iPetrinetManipulation.moveGraph(id, relativePosition);
 		
 	}
 
 	@Override
 	public void moveNode(int id, INode node, Point2D relativePosition)
 			throws EngineException {
 		
 		iPetrinetManipulation.moveNode(id, node, relativePosition);
 		
 	}
 
 	@Override
 	public void save(int id, String path, String filename, String format)
 			throws EngineException {
 		
 		iPetrinetManipulation.save(id, path, filename, format);
 		
 	}
 	
 	@Override
 	public int load(String path, String filename) {
 		
 		return iPetrinetManipulation.load(path, filename);
 		
 	}
 
 	@Override
 	public void setMarking(int id, INode place, int marking)
 			throws EngineException {
 
 		iPetrinetManipulation.setMarking(id, place, marking);
 		
 	}
 
 	@Override
 	public void setPname(int id, INode place, String pname)
 			throws EngineException {
 
 		iPetrinetManipulation.setPname(id, place, pname);
 		
 	}
 
 	@Override
 	public void setTlb(int id, INode transition, String tlb)
 			throws EngineException {
 
 		iPetrinetManipulation.setTlb(id, transition, tlb);
 		
 	}
 
 	@Override
 	public void setTname(int id, INode transition, String tname)
 			throws EngineException {
 
 		iPetrinetManipulation.setTname(id, transition, tname);
 		
 	}
 
 	@Override
 	public void setWeight(int id, Arc arc, int weight) throws EngineException {
 
 		iPetrinetManipulation.setWeight(id, arc, weight);
 		
 	}
 	
 	@Override
 	public void setRnw(int id, INode transition, Renews renews) throws EngineException {
 		
 		iPetrinetManipulation.setRnw(id, transition, renews);
 		
 	}
 
 	@Override
 	public NodeTypeEnum getNodeType(INode node) throws EngineException {
 		
 		return iPetrinetManipulation.getNodeType(node);
 		
 	}
 
 	@Override
 	public void setPlaceColor(int id, INode place, Color color) throws EngineException {
 		
 		iPetrinetManipulation.setPlaceColor(id, place, color);
 		
 	}
 
 	
 }
