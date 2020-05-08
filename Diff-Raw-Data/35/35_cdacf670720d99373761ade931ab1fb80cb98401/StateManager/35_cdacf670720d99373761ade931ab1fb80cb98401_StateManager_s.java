 package org.iucn.sis.client.api.container;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.iucn.sis.client.api.caches.AssessmentCache;
 import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.client.api.caches.WorkingSetCache;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.WorkingSet;
 
 import com.solertium.util.events.ComplexListener;
 import com.solertium.util.events.CoreObservable;
 
public class StateManager implements CoreObservable<ComplexListener<StateChangeEvent>> {
 	
 	public enum StateChangeEventType {
 		BeforeStateChanged(0), StateChanged(1);
 		
 		private final int value;
 		
 		private StateChangeEventType(int value) {
 			this.value = value;
 		}
 		
 		public int getValue() {
 			return value;
 		}
 	}
 	
 	public static final StateManager impl = new StateManager();
 	
 	private final Map<Integer, List<ComplexListener<StateChangeEvent>>> listeners;
 	
 	private Integer workingSet;
 	private Integer taxon;
 	private Integer assessment;
 	
 	private StateManager() {
 		listeners = new HashMap<Integer, List<ComplexListener<StateChangeEvent>>>();
 	}
 	
 	public void addStateChangeListener(StateChangeEventType event, ComplexListener<StateChangeEvent> listener) {
 		addListener(event.getValue(), listener);
 	}
 	
 	public Assessment getAssessment() {
 		return assessment == null ? null : AssessmentCache.impl.getAssessment(assessment);
 	}
 	
 	public Taxon getTaxon() {
 		return taxon == null ? null : TaxonomyCache.impl.getTaxon(taxon);
 	}
 	
 	public WorkingSet getWorkingSet() {
 		return workingSet == null ? null : WorkingSetCache.impl.getWorkingSet(workingSet);
 	}
 
 	public void reset() {
		setState(null, null, null);
 	}
 	
 	/**
 	 * Sets the current taxon to the given 
 	 * taxon and resets the current assessment, 
 	 * if any.
 	 * @param taxon
 	 */
 	public void setTaxon(Taxon taxon) {
 		setState(getWorkingSet(), taxon, null);
 	}
 	
 	/**
 	 * Sets the current working set to the given 
 	 * working set, and resets the current taxon 
 	 * and assessment, if any
 	 * @param workingSet
 	 */
 	public void setWorkingSet(WorkingSet workingSet) {
 		setState(workingSet, null, null);
 	}
 	
 	/**
 	 * Sets the current assessment.
 	 * @param assessment
 	 */
 	public void setAssessment(Assessment assessment) {
 		setState(getWorkingSet(), getTaxon(), assessment);
 	}
 	
 	/**
 	 * Sets the state of the current taxon and assessment, 
 	 * while respecting the value of the current working 
 	 * set.
 	 * @param taxon
 	 * @param assessment
 	 */
 	public void setState(Taxon taxon, Assessment assessment) {
 		setState(getWorkingSet(), taxon, assessment);
 	}
 	
 	/**
 	 * Sets the entire state of the system to the given values.
 	 * The navigation will be re-drawn.
 	 * @param workingSet
 	 * @param taxon
 	 * @param assessment
 	 */
 	public void setState(WorkingSet workingSet, Taxon taxon, Assessment assessment) {
 		setState(workingSet, taxon, assessment, this);
 	}
 	
 	/**
 	 * Sets the entire state of the system to the given values, 
 	 * and relates the state change event's source to the given 
 	 * source.  Note that if anything other than the the 
 	 * navigation panel is the source of the event, the navigation 
 	 * will be re-drawn after the state change.  
 	 * @param workingSet
 	 * @param taxon
 	 * @param assessment
 	 * @param source
 	 */
 	public void setState(WorkingSet workingSet, Taxon taxon, Assessment assessment, Object source) {
 		setState(new StateChangeEvent(workingSet, taxon, assessment, source));
 	}
 	
 	/**
 	 * Fires the given state change event.  All values will 
 	 * be overwritten.  With great power comes great 
 	 * responsibility.
 	 * @param event
 	 */
 	public void setState(StateChangeEvent event) {
		if (hasChanges(getWorkingSet(), event.getWorkingSet()) || 
 				hasChanges(getTaxon(), event.getTaxon()) || 
 				hasChanges(getAssessment(), event.getAssessment())) {
 	
 			if (!fireEvent(StateChangeEventType.BeforeStateChanged.getValue(), event)) {
 				this.workingSet = event.getWorkingSet() == null ? null : event.getWorkingSet().getId();
 				this.taxon = event.getTaxon() == null ? null : event.getTaxon().getId();
 				this.assessment = event.getAssessment() == null ? null : event.getAssessment().getId();
 				
 				if (taxon != null)
 					TaxonomyCache.impl.updateRecentTaxa();
 				if (assessment != null)
 					AssessmentCache.impl.updateRecentAssessments();
 				
 				fireEvent(StateChangeEventType.StateChanged.getValue(), event.getSource(), event.getUrl());
 			}
 		}
 	}
 	
 	private boolean hasChanges(Object currentValue, Object newValue) {
 		if (currentValue == null)
 			return newValue != null;
 		else
 			return !currentValue.equals(newValue);
 	}
 	
 	public void doLogout() {
 		listeners.clear();
 		workingSet = null;
 		taxon = null;
 		assessment = null;
 	}
 	
 	public void addListener(int eventType, ComplexListener<StateChangeEvent> listener) {
 		Integer key = new Integer(eventType);
 		List<ComplexListener<StateChangeEvent>> group = listeners.get(key);
 		if (group == null)
 			group = new ArrayList<ComplexListener<StateChangeEvent>>();
 		group.add(listener);
 		listeners.put(key, group);
 	}
 
 	public boolean fireEvent(int eventType) {
 		return fireEvent(eventType, this, null);
 	}
 	
 	public boolean fireEvent(int eventType, Object source, String url) {
 		return fireEvent(eventType, new StateChangeEvent(getWorkingSet(), getTaxon(), getAssessment(), source, url));
 	}
 	
 	/**
 	 * Fires an event.
 	 * @param eventType
 	 * @param event
 	 * @return true if the event got canceled, false otherwise
 	 */
 	public boolean fireEvent(int eventType, StateChangeEvent event) {
 		Debug.println("Firing event {0}", eventType);
 		Integer key = new Integer(eventType);
 		List<ComplexListener<StateChangeEvent>> group = listeners.get(key);
 		
 		boolean retValue = false;
 		if (group != null) {
 			for (ComplexListener<StateChangeEvent> listener : group) {
 				listener.handleEvent(event);
 				retValue |= event.isCanceled();
 			}
 		}
 		
 		Debug.println("{0} listeners fired for event {1}, canceled = {2}", group == null ? 0 : group.size(), eventType, retValue);
 		return retValue;
 	}
 
 	public void removeAllListeners() {
 		listeners.clear();
 	}
 
 	public void removeListener(int eventType, ComplexListener<StateChangeEvent> listener) {
 		Integer key = new Integer(eventType);
 		List<ComplexListener<StateChangeEvent>> group = listeners.get(key);
 		if (group != null)
 			group.remove(listener);
 	}
 	
 }
