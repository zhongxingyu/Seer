 package ch.hszt.sp.models;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Observable;
 
 import ch.hszt.sp.dao.IGisDAO;
 import ch.hszt.sp.dao.XmlGisDAO;
 import ch.hszt.sp.exceptions.DataAccessException;
 
 public class CShortestPathModel extends Observable implements IShortestPathModel {
 	private ArrayList<CNode> cNode;
 	private ArrayList<CEdge> cEdge;
 	private Map<Integer,CNode> mpNode;
 	private Map<Integer,CEdge> mpEdge;
 	
 	public CShortestPathModel(){}
 	
 	public void notifyObserver() {
 		setChanged();
 		notifyObservers();
 	}
 	
 	public void execut() throws DataAccessException {
 		//set data
 		setNodes();
 		setEdges();
 		setNodesAsMap();
 		setEdgesAsMap();
 		
 		//notify observer
 		notifyObserver();
 	}
 
 	@Override
 	public ArrayList<CNode> getNodes() {
 		return this.cNode;
 	}
 
 	@Override
 	public void setNodes() throws DataAccessException {
 		this.cNode = new ArrayList<CNode>();
 		IGisDAO xgd = new XmlGisDAO();
 		this.cNode = (ArrayList<CNode>) xgd.getNodes();
 	}
 
 	@Override
 	public ArrayList<CEdge> getEdges() {
 		return this.cEdge;
 	}
 
 	@Override
 	public void setEdges() throws DataAccessException {
 		this.cEdge = new ArrayList<CEdge>();
 		IGisDAO xgd = new XmlGisDAO();
 		this.cEdge = (ArrayList<CEdge>) xgd.getEdges();
 	}
 
 	@Override
 	public LinkedList<CNode> getShortestPath(int start, int target) {
 		CDijkstra cd = new CDijkstra(getNodes(), getEdges());		
 		cd.execute(getNodes().get(start));
 		LinkedList<CNode> path = cd.getPath(getNodes().get(target));
 		return path;
 	}
 
 	@Override
 	public Map<Integer,CNode> getNodesAsMap() {
 		return this.mpNode;
 	}
 
 	@Override
 	public void setNodesAsMap() throws DataAccessException {
 		this.mpNode = new HashMap<Integer, CNode>();
 		IGisDAO xgd = new XmlGisDAO();
 		this.mpNode = xgd.getNodesAsMap();
 	}
 
 	@Override
 	public Map<Integer,CEdge> getEdgesAsMap() {
 		return this.mpEdge;
 	}
 
 	@Override
 	public void setEdgesAsMap() throws DataAccessException {
 		this.mpEdge = new HashMap<Integer, CEdge>();
 		IGisDAO xgd = new XmlGisDAO();
 		this.mpEdge = xgd.getEdgesAsMap();
 	}
 
 	@Override
 	public double getDistance(int start, int target) {
 		 CDijkstra cd = new CDijkstra(getNodes(), getEdges());
 		 cd.execute(getNodes().get(start));
 		 return cd.getDistanceOfShortestPath(getNodes().get(target));
 	}
 }
