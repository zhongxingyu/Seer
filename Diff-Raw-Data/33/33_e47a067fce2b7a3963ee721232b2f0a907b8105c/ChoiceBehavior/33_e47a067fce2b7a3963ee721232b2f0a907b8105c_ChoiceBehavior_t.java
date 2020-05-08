 package homework3.android;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 public class ChoiceBehavior extends BehaviorBase {
 	private boolean m_firstOnly = false;
 	private int m_type = SINGLE;
 
 	private HashMap<GraphicalObject, Boolean> m_savedSelectionState = new HashMap<GraphicalObject, Boolean>();
 	
 	private List<GraphicalObject> m_selectedObjects = new ArrayList<GraphicalObject>();
 	private Selectable m_interimSelectedObject;
 	public ChoiceBehavior (int type, boolean firstOnly){
 		m_type = type;
 		m_firstOnly = firstOnly;
 	}
 
 	public ChoiceBehavior (int type, boolean firstOnly, Group attachedTo){
 		m_type = type;
 		m_firstOnly = firstOnly;
 	}
 
 	
 	public List<GraphicalObject> getSelection ()
 	{
 		return m_selectedObjects;
 	}
 
 	public static final int SINGLE = 0;
 	public static final int TOGGLE = 1;
 	public static final int MULTIPLE = 2;
 
 	public ChoiceBehavior(Group tiedTo) {
 		super(tiedTo);
 		// TODO Auto-generated constructor stub
 	}
 
 	public ChoiceBehavior(BehaviorEvent start, BehaviorEvent stop, Group tiedTo) {
 		super(start, stop, DEFAULT_RUNNING, tiedTo);
 	}
 
 	@Override
 	protected void behaviorStarted(BehaviorEvent event) {
 		for (GraphicalObject child : m_group.getChildren()) {
 			if(child instanceof Selectable){
 				m_savedSelectionState.put(child, ((Selectable)child).isSelected());
 			}
 			
 			if(child.contains(event.getX(), event.getY()) && child instanceof Selectable)
 			{
 				setInterimObject((Selectable)child);
 			}
 		}
 	}
 
 	private void setInterimObject(Selectable child)
 	{
 		if(m_interimSelectedObject != null)
 		{
 //			m_interimSelectedObject.setSelected(m_savedSelectionState.get((GraphicalObject)m_interimSelectedObject));
 			m_interimSelectedObject.setInterimSelected(false);
 		}
 		child.setInterimSelected(true);
 		m_interimSelectedObject = child;
 
 	}
 	
 	private void finalizeSelection()
 	{
 		m_selectedObjects.clear();
 		// toggle selection
 		for(GraphicalObject child : m_group.getChildren() )
 		{
 			if(child instanceof Selectable)
 				((Selectable)child).setInterimSelected(false);
 		}
 		
 		
 		if(m_type == SINGLE)
 		{
 			// deselect all currently selected indices
 			for(GraphicalObject child : m_group.getChildren() )
 			{
 				if(child instanceof Selectable)
 					((Selectable)child).setSelected(false);
 			}
 			m_interimSelectedObject.setSelected(true);
 			
 		} else if (m_type == TOGGLE)
 		{
 			
 			// Toggle the current item, deselect all other items
			for(GraphicalObject child : m_group.getChildren() )
 			{
				if(child instanceof Selectable && child != m_interimSelectedObject)
 					((Selectable)child).setSelected(false);
 			}
 			m_interimSelectedObject.setSelected(!m_interimSelectedObject.isSelected());
 		} else 
 		{
 			m_interimSelectedObject.setSelected(!m_interimSelectedObject.isSelected());
 		}
 		m_selectedObjects.clear();
 		for(GraphicalObject child : m_group.getChildren() )
 		{
 			if(child instanceof Selectable)
 				if(((Selectable)child).isSelected())
 				{
 					m_selectedObjects.add(child);
 				}
 		}
 		m_interimSelectedObject = null;
 	}
 	
 	protected boolean startConditionSatisfied(BehaviorEvent event)
 	{
 		for (GraphicalObject child : m_group.getChildren()) {
 			if(child.contains(event.getX(), event.getY()) && child instanceof Selectable) return true;
 		}
 		return false;
 	}
 	
 	@Override
 	protected void doRunningInside(BehaviorEvent event) {
 		// ignore future moves if first only
 		if(m_firstOnly) return;
 		
 		for (GraphicalObject child : m_group.getChildren()) {
 			if(child.contains(event.getX(), event.getY()) && child instanceof Selectable)
 			{
 				// check if we have already
 				setInterimObject((Selectable)child);
 			}
 		}
 	}
 
 	@Override
 	protected void doRunningOutside(BehaviorEvent event) {
 		// do nothing?
 
 	}
 
 	@Override
 	protected void onStopped(BehaviorEvent event) {
 		finalizeSelection();
 	}
 
 	@Override
 	protected void onCancelled(BehaviorEvent event) {
 		// restore item selection to previous selection
 	    Iterator it = m_savedSelectionState.entrySet().iterator();
 	    while (it.hasNext()) {
 	        Map.Entry pairs = (Map.Entry)it.next();
 	        Selectable s = (Selectable) pairs.getKey();
 	        s.setSelected((Boolean)pairs.getValue());
 	        it.remove(); // avoids a ConcurrentModificationException
 	    }
 	}
 	public boolean isFirstOnly() {
 		return m_firstOnly;
 	}
 	public void setFirstOnly(boolean firstOnly) {
 		m_firstOnly = firstOnly;
 	}
 	public int getType() {
 		return m_type;
 	}
 	public void setType(int type) {
 		m_type = type;
 	}
 
 }
