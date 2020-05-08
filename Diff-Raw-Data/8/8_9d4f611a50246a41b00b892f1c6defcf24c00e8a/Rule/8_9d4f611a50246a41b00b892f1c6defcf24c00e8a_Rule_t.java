 package transformation;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import petrinetze.ActionType;
 import petrinetze.IArc;
 import petrinetze.INode;
 import petrinetze.IPetrinet;
 import petrinetze.IPetrinetListener;
 import petrinetze.IPlace;
 import petrinetze.ITransition;
 import petrinetze.impl.Petrinet;
 
 /**
  * 
  * @author Philipp Khn
  *
  */
 
 public class Rule implements IRule
 {
 	
 	private final IPetrinet k;
 	private final IPetrinet l;
 	private final IPetrinet r;
 	private final Listener kListener;
 	private final Listener lListener;
 	private final Listener rListener;
 	private final Map<INode, INode> lKSameNodes;
 	private final Map<INode, INode> rKSameNodes;
 	private final Map<IArc, IArc> lkSameEdges;
 	private final Map<IArc, IArc> rKSameEdges;
 	
 	public Rule()
 	{
 		k = new Petrinet();
 		l = new Petrinet();
 		r = new Petrinet();
 		
 		lKSameNodes = new HashMap<INode, INode>();
 		rKSameNodes = new HashMap<INode, INode>();
 		lkSameEdges = new HashMap<IArc, IArc>();
 		rKSameEdges = new HashMap<IArc, IArc>();
 		
 		kListener = new Listener(Net.k, l, k, r);
 		lListener = new Listener(Net.l, l, k, r);
 		rListener = new Listener(Net.r, l, k, r);
 	}
 	
 	@Override
 	protected void finalize() throws Throwable 
 	{
 		super.finalize();
 		k.removePetrinetListener(kListener);
 		l.removePetrinetListener(lListener);
 		r.removePetrinetListener(rListener);
 	}
 
 	@Override
 	public IPetrinet K() {
 		return k;
 	}
 
 	@Override
 	public IPetrinet L() {
 		return l;
 	}
 
 	@Override
 	public IPetrinet R() {
 		return r;
 	}
 	
 	private enum Net
 	{
 		l,
 		k,
 		r
 	}
 	
	private boolean rChanging = false;
	private boolean kChanging = false;
	private boolean lChanging = false;
	
 	private class Listener implements IPetrinetListener
 	{
 		private final Net net;
 		private final IPetrinet k;
 		private final IPetrinet l;
 		private final IPetrinet r;
 		
 		Listener(Net net, IPetrinet l, IPetrinet k, IPetrinet r)
 		{
 			this.net = net;
 			this.k = k;
 			this.l = l;
 			this.r = r;
 			if(net == Net.l)
 				l.addPetrinetListener(this);
 			else if(net == Net.k)
 				k.addPetrinetListener(this);
 			else if(net == Net.r)
 				r.addPetrinetListener(this);
 			
 		}
 
 		@Override
 		public void changed(IPetrinet petrinet, INode element, ActionType actionType) 
 		{
 			if(net == Net.l)
 			{
 				if(!kChanging && !rChanging)
 				{
 					lChanging = true;
 					lNodeChanged(element, actionType);
 					lChanging = false;
 				}
 			}
 			else if(net == Net.k)
 			{
 				if(!lChanging && !rChanging)
 				{
 					kChanging = true;
 					kNodeChanged(element, actionType);
 					kChanging = false;
 				}
 			}
 			else if(net == Net.r)
 			{
 				if(!lChanging && !kChanging)
 				{
 					rChanging = true;
 					rNodeChanged(element, actionType);
 					rChanging = false;
 				}
 			}
 		}
 
 		@Override
 		public void changed(IPetrinet petrinet, IArc element, ActionType actionType) 
 		{
 			if(net == Net.l)
 			{
 				if(!kChanging && !rChanging)
 				{
 					lChanging = true;
 					lEdgeChanged(element, actionType);
 					lChanging = false;
 				}
 			}
 			else if(net == Net.k)
 			{
 				if(!lChanging && !rChanging)
 				{
 					kChanging = true;
 					kEdgeChanged(element, actionType);
 					kChanging = false;
 				}
 			}
 			else if(net == Net.r)
 			{
 				if(!lChanging && !kChanging)
 				{
 					rChanging = true;
 					rEdgeChanged(element, actionType);
 					rChanging = false;
 				}
 			}
 		}
 		
 		private void lNodeChanged(INode element, ActionType actionType)
 		{
 			if(actionType == ActionType.added)
 			{
 				if(element instanceof IPlace && !lKSameNodes.containsKey(element))
 				{
 					INode node = k.createPlace(element.getName());
 					lKSameNodes.put(element, node);
 				}
 				else if(element instanceof ITransition && !lKSameNodes.containsKey(element))
 				{
 					INode node = k.createTransition(element.getName(), ((ITransition)element).getRnw());
 					lKSameNodes.put(element, node);
 				}
 			}
 			else if(actionType == ActionType.deleted)
 			{
 				if(element instanceof IPlace && !r.getAllPlaces().contains(element) && lKSameNodes.containsKey(element))
 				{
 					INode node = lKSameNodes.get(element);
 					lKSameNodes.remove(element);
 					k.deletePlaceById(node.getId());
 				}
 				else if(element instanceof ITransition && !r.getAllTransitions().contains(element) && lKSameNodes.containsKey(element))
 				{
 					INode node = lKSameNodes.get(element);
 					lKSameNodes.remove(element);
 					k.deleteTransitionByID(node.getId());
 				}
 			}
 		}
 		private void kNodeChanged(INode element, ActionType actionType)
 		{
 			if(actionType == ActionType.added)
 			{
 				if(element instanceof IPlace)
 				{
 					if(!lKSameNodes.containsValue(element))
 					{
 						INode node = l.createPlace(element.getName());
 						lKSameNodes.put(node, element);
 					}
 					if(!rKSameNodes.containsValue(element))
 					{
 						INode node = r.createPlace(element.getName());
 						rKSameNodes.put(node, element);
 					}
 				}
 				else if(element instanceof ITransition)
 				{
 					if(!lKSameNodes.containsValue(element))
 					{
 						INode node = l.createTransition(element.getName(), ((ITransition)element).getRnw());
 						lKSameNodes.put(node, element);
 					}
 					if(!rKSameNodes.containsValue(element))
 					{
 						INode node = r.createTransition(element.getName(), ((ITransition)element).getRnw());
 						rKSameNodes.put(node, element);
 					}
 				}
 			}
 			else if(actionType == ActionType.deleted)
 			{
 				if(element instanceof IPlace)
 				{
 					if(lKSameNodes.containsValue(element))
 					{
 						INode node = getKeyFromValue(lKSameNodes, element);
 						lKSameNodes.remove(element);
 						l.deletePlaceById(node.getId());
 					}
 					if(rKSameNodes.containsValue(element))
 					{
 						INode node = getKeyFromValue(rKSameNodes, element);
 						rKSameNodes.remove(element);
 						r.deletePlaceById(node.getId());
 					}
 				}
 				else if(element instanceof ITransition && !r.getAllTransitions().contains(element) && lKSameNodes.containsKey(element))
 				{
 					if(lKSameNodes.containsValue(element))
 					{
 						INode node = getKeyFromValue(lKSameNodes, element);
 						lKSameNodes.remove(element);
 						l.deletePlaceById(node.getId());
 					}
 					if(rKSameNodes.containsValue(element))
 					{
 						INode node = getKeyFromValue(rKSameNodes, element);
 						rKSameNodes.remove(element);
 						r.deletePlaceById(node.getId());
 					}
 				}
 			}
 		}
 		
 		private void rNodeChanged(INode element, ActionType actionType)
 		{
 			if(actionType == ActionType.added)
 			{
 				if(element instanceof IPlace && !rKSameNodes.containsKey(element))
 				{
 					INode node = k.createPlace(element.getName());
 					rKSameNodes.put(element, node);
 				}
 				else if(element instanceof ITransition && !rKSameNodes.containsKey(element))
 				{
 					INode node = k.createTransition(element.getName(), ((ITransition)element).getRnw());
 					rKSameNodes.put(element, node);
 				}
 			}
 			else if(actionType == ActionType.deleted)
 			{
 				if(element instanceof IPlace && !r.getAllPlaces().contains(element) && rKSameNodes.containsKey(element))
 				{
 					INode node = rKSameNodes.get(element);
 					rKSameNodes.remove(element);
 					k.deletePlaceById(node.getId());
 				}
 				else if(element instanceof ITransition && !r.getAllTransitions().contains(element) && rKSameNodes.containsKey(element))
 				{
 					INode node = rKSameNodes.get(element);
 					rKSameNodes.remove(element);
 					k.deleteTransitionByID(node.getId());
 				}
 			}
 		}
 		
 		private void lEdgeChanged(IArc element, ActionType actionType)
 		{
 			if(actionType == ActionType.added)
 			{
 				if(!lkSameEdges.containsKey(element))
 				{
                     INode start = lKSameNodes.get(element.getStart());
                     INode end = lKSameNodes.get(element.getEnd());
 					IArc edge = k.createArc(element.getName(), start, end);
 
 					lkSameEdges.put(element, edge);
 				}
 			}
 			else if(actionType == ActionType.deleted)
 			{
 				if(lkSameEdges.containsKey(element))
 				{
 					IArc edge = lkSameEdges.get(element);
 					lkSameEdges.remove(element);
 					k.deleteArcByID(edge.getId());
 				}
 			}
 		}
 		private void kEdgeChanged(IArc element, ActionType actionType)
 		{
 			if(actionType == ActionType.added)
 			{
 				if(!lkSameEdges.containsValue(element))
 				{
                     INode start = getKeyFromValue(lKSameNodes, element.getStart());
                     INode end = getKeyFromValue(lKSameNodes, element.getEnd());
 
                     IArc edge = l.createArc(element.getName(), start, end);
 
 
                     lkSameEdges.put(edge, element);
 				}
 				if(!rKSameEdges.containsValue(element))
 				{
                     INode start = getKeyFromValue(rKSameNodes, element.getStart());
                     INode end = getKeyFromValue(rKSameNodes, element.getEnd());
                     IArc edge = r.createArc(element.getName(), start, end);
                     rKSameEdges.put(edge, element);
 				}
 			}
 			else if(actionType == ActionType.deleted)
 			{
 				if(lkSameEdges.containsValue(element))
 				{
 					IArc edge = getKeyFromValue(lkSameEdges, element);
 					lkSameEdges.remove(element);
 					l.deleteArcByID(edge.getId());
 				}
 				if(rKSameEdges.containsValue(element))
 				{
 					IArc edge = getKeyFromValue(rKSameEdges, element);
 					rKSameEdges.remove(element);
 					r.deleteArcByID(edge.getId());
 				}
 			}
 		}
 		
 		private void rEdgeChanged(IArc element, ActionType actionType)
 		{
 			if(actionType == ActionType.added)
 			{
 				if(!rKSameEdges.containsKey(element))
 				{
                     INode start = (rKSameNodes.get(element.getStart()));
                     INode end = rKSameNodes.get(element.getEnd());
                     IArc edge = k.createArc(element.getName(), start, end);
 					rKSameEdges.put(element, edge);
 				}
 			}
 			else if(actionType == ActionType.deleted)
 			{
 				if(rKSameEdges.containsKey(element))
 				{
 					IArc edge = rKSameEdges.get(element);
 					rKSameEdges.remove(element);
 					k.deleteArcByID(edge.getId());
 				}
 			}
 		}	
 	}
 
 	private <K, V> K getKeyFromValue(Map<K, V> map, V value)
 	{
 		for(Entry<K, V> e : map.entrySet())
 			if(e.getValue().equals(value))
 				return e.getKey();
 		return null;
 	}
 
 	@Override
 	public INode fromKtoL(INode node) {
 		return getKeyFromValue(lKSameNodes, node);
 	}
 
 	@Override
 	public IArc fromKtoL(IArc edge) {
 		return getKeyFromValue(lkSameEdges, edge);
 	}
 
 	@Override
 	public INode fromKtoR(INode node) {
 		return getKeyFromValue(rKSameNodes, node);
 	}
 
 	@Override
 	public IArc fromKtoR(IArc edge) {
 		return getKeyFromValue(rKSameEdges, edge);
 	}
 
 	@Override
 	public INode fromLtoK(INode node) {
 		if(lKSameNodes.containsKey(node))
 			return lKSameNodes.get(node);
 		return null;
 	}
 
 	@Override
 	public IArc fromLtoK(IArc edge) {
 		if(lkSameEdges.containsKey(edge))
 			return lkSameEdges.get(edge);
 		return null;
 	}
 
 	@Override
 	public INode fromRtoK(INode node) {
 		if(rKSameNodes.containsKey(node))
 			return rKSameNodes.get(node);
 		return null;
 	}
 
 	@Override
 	public IArc fromRtoK(IArc edge) {
 		if(rKSameEdges.containsKey(edge))
 			return rKSameEdges.get(edge);
 		return null;
 	}
 
 
 }
