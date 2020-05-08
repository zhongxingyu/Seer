 package n3phele.service.model;
 
 /**
  * (C) Copyright 2010-2013. Nigel Cook. All rights reserved.
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  * 
  * Licensed under the terms described in LICENSE file that accompanied this code, (the "License"); you may not use this file
  * except in compliance with the License. 
  * 
  *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on 
  *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
  *  specific language governing permissions and limitations under the License.
  */
 
 import static n3phele.service.model.core.Helpers.URItoString;
 import static n3phele.service.model.core.Helpers.stringToURI;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 import javax.xml.bind.annotation.XmlType;
 
import n3phele.service.actions.ServiceAction;
import n3phele.service.actions.StackServiceAction;
 import n3phele.service.model.core.Entity;
 import n3phele.service.rest.impl.ActionResource;
 import n3phele.service.rest.impl.NarrativeResource;
 
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.annotation.Cache;
 import com.googlecode.objectify.annotation.Id;
 import com.googlecode.objectify.annotation.Index;
 import com.googlecode.objectify.annotation.Parent;
 import com.googlecode.objectify.annotation.Unindex;
 import com.googlecode.objectify.condition.IfNotNull;
 import com.googlecode.objectify.condition.IfNotZero;
 import com.googlecode.objectify.condition.IfTrue;
 
 @XmlRootElement(name="CloudProcess")
 @XmlType(name="CloudProcess", propOrder={"description", "state", "running", "waitTimeout", "pendingInit", "pendingCall", "pendingCancel", "pendingDump", "pendingAssertion", 
 		"dependentOn", "dependencyFor", "start", "complete", "finalized", "action", "parent", "pendingOnExit", "topLevel", "narrative","costPerHour","epoch","account"})
 @Unindex
 @Cache
 @com.googlecode.objectify.annotation.Entity
 public class CloudProcess extends Entity {
 	@Id protected Long id;
 	protected ActionState state = ActionState.NEWBORN;
 
 	protected Date running = null;
 	protected Date waitTimeout = null;
 	protected boolean pendingInit = false;
 	protected boolean pendingCall = false;
 	protected boolean pendingCancel = false;
 	protected boolean pendingDump = false;
 	protected ArrayList<String> pendingAssertion = new ArrayList<String>();
 	protected ArrayList<String> dependentOn = new ArrayList<String>();
 	protected ArrayList<String> dependencyFor = new ArrayList<String>();
 	@Index protected Date start = null;
 	@Index protected Date complete = null;
 	@Index protected boolean finalized = false;
 	@Index protected String action = null;
 	@Index protected String parent = null;
 	@Parent Key<CloudProcess> root;
 	@Index(IfTrue.class) boolean topLevel = false;
 	protected ArrayList<String> pendingOnExit = new ArrayList<String>();
 
 
 	@Index(IfNotZero.class)protected double costPerHour = 0;
 	@Index(IfNotNull.class)protected Date epoch = null;
 	@Index(IfNotNull.class)protected String account = null;
 	@Index(IfTrue.class) boolean isService = false;
 	
 	public CloudProcess() {}
 	/** Describes a cloud process. 
 	 * @param owner process owner
 	 * @param parent Parent to be notified on process state changes
 	 * @param taskId Reference to the action managed by the process
 	 */
 	public CloudProcess(URI owner, String name, CloudProcess parent, boolean topLevel, Action task, boolean isService)  {
 		super(name, null, owner, false);
 		this.action = task.getUri().toString();
 		this.topLevel = topLevel;
 		if(parent != null) {
 			this.parent = parent.getUri().toString();
 			if(parent.root == null) {
 				this.root = Key.create(parent);
 			} else {
 				this.root = parent.root;
 			}
 		} else {
 			this.parent = null;
 			this.root = null;
 		}
 		this.isService = isService;
 	}
 	
 	/*
 	 * Getters and Settings
 	 */
 	/**
 	 * @param account the process account
 	 */
 	public void setAccount(String account){
 		this.account = account;
 	}
 	
 	/**
 	 * @return the account associated with the process
 	 */
 	public String getAccount(){
 		return this.account;
 	}
 	
 	/**
 	 * @return the epoch of the server
 	 */
 	public Date getEpoch(){
 		return epoch;
 	}
 	
 	/**
 	 * @param epoch the epoch date of the server
 	 */
 	public void setEpoch(Date epoch){
 		this.epoch = epoch;
 	}
 	
 	/**
 	 * @return the cost per hour
 	 */
 	public double getCostPerHour(){
 		return costPerHour;
 	}
 	
 	/**
 	 * @param costPerHour the cost per hour to set
 	 */
 	public void setCostPerHour(double costPerHour){
 		this.costPerHour = costPerHour;
 	}
 	
 	/**
 	 * @return the id
 	 */
 	@XmlTransient
 	public Long getId() {
 		return id;
 	}
 	/**
 	 * @param id the id to set
 	 */
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	/**
 	 * @return the process state
 	 */
 	public ActionState getState() {
 		return state;
 	}
 	/**
 	 * @param state the process state to set
 	 */
 	public void setState(ActionState state) {
 		this.state = state;
 	}
 	
 	/**
 	 * @return the date process was queued to run
 	 */
 	public Date getRunning() {
 		return running;
 	}
 	/**
 	 * @param running the date process was queued to run
 	 */
 	public void setRunning(Date running) {
 		this.running = running;
 	}
 	
 	/**
 	 * @return the time the process execution delay until, unless signalled
 	 */
 	public Date getWaitTimeout() {
 		return waitTimeout;
 	}
 	/**
 	 * @param waitTimeout the time the process will delay until, unless signalled
 	 */
 	public void setWaitTimeout(Date waitTimeout) {
 		this.waitTimeout = waitTimeout;
 	}
 	/**
 	 * @return pending Init operation
 	 */
 	public boolean isPendingInit() {
 		return pendingInit;
 	}
 	/**
 	 * @return pending Init operation
 	 */
 	public boolean getPendingInit() {
 		return pendingInit;
 	}
 	/**
 	 * @param pendingInit set pending Init state
 	 */
 	public void setPendingInit(boolean pendingInit) {
 		this.pendingInit = pendingInit;
 	}
 	/**
 	 * @return pending Call operation
 	 */
 	public boolean isPendingCall() {
 		return pendingCall;
 	}
 	/**
 	 * @return pending Call operation
 	 */
 	public boolean getPendingCall() {
 		return pendingCall;
 	}
 	/**
 	 * @param pendingCall set pending Call
 	 */
 	public void setPendingCall(boolean pendingCall) {
 		this.pendingCall = pendingCall;
 	}
 	/**
 	 * @return pending Cancel operation
 	 */
 	public boolean isPendingCancel() {
 		return pendingCancel;
 	}
 	/**
 	 * @return pending Cancel operation
 	 */
 	public boolean getPendingCancel() {
 		return pendingCancel;
 	}
 	/**
 	 * @param pendingCancel set pending Cancel state
 	 */
 	public void setPendingCancel(boolean pendingCancel) {
 		this.pendingCancel = pendingCancel;
 	}
 	/**
 	 * @return pending Dump operation
 	 */
 	public boolean isPendingDump() {
 		return pendingDump;
 	}
 	/**
 	 * @return pending Dump operation
 	 */
 	public boolean getPendingDump() {
 		return pendingDump;
 	}
 	/**
 	 * @param pendingDump set the pending Dump state
 	 */
 	public void setPendingDump(boolean pendingDump) {
 		this.pendingDump = pendingDump;
 	}
 	
 	/**
 	 * @return pending Assertion(s)
 	 */
 	public List<String> getPendingAssertion() {
 		return pendingAssertion;
 	}
 	/**
 	 * @param pendingAssertion set pending Assertion list
 	 */
 	public void setPendingAssertion(List<String> pendingAssertion) {
 		this.pendingAssertion.clear();
 		this.pendingAssertion.addAll(pendingAssertion);
 	}
 	/**
 	 * @return TRUE if process has pending assertions
 	 */
 	public boolean hasPendingAssertions() {
 		return this.pendingAssertion.size() != 0;
 	}
 	
 	/**
 	 * @return TRUE if has currently pending actions
 	 */
 	public boolean hasPending() {
 		return pendingInit || pendingCall || pendingCancel || pendingDump || pendingAssertion.size() != 0;
 	}
 	/**
 	 * @return the dependentOn
 	 */
 	public ArrayList<String> getDependentOn() {
 		return dependentOn;
 	}
 	/**
 	 * @param dependentOn the dependentOn to set
 	 */
 	public void setDependentOn(ArrayList<String> dependentOn) {
 		this.dependentOn = dependentOn;
 	}
 	/**
 	 * @return the dependencyFor
 	 */
 	public ArrayList<String> getDependencyFor() {
 		return dependencyFor;
 	}
 	/**
 	 * @param dependencyFor the dependencyFor to set
 	 */
 	public void setDependencyFor(ArrayList<String> dependencyFor) {
 		this.dependencyFor = dependencyFor;
 	}
 	/**
 	 * @return the date execution started
 	 */
 	public Date getStart() {
 		return start;
 	}
 	/**
 	 * @param start the execution start date
 	 */
 	public void setStart(Date start) {
 		this.start = start;
 	}
 	/**
 	 * @return the execution complete date
 	 */
 	public Date getComplete() {
 		return complete;
 	}
 	/**
 	 * @param complete the execution complete date to set
 	 */
 	public void setComplete(Date complete) {
 		this.complete = complete;
 	}
 	/**
 	 * @return process execution finalized
 	 */
 	public boolean isFinalized() {
 		return finalized;
 	}
 	/**
 	 * @param finalized the finalized to set
 	 */
 	public void setFinalized(boolean finalized) {
 		this.finalized = finalized;
 	}
 	/**
 	 * @return the action
 	 */
 	public URI getAction() {
 		return stringToURI(action);
 	}
 	/**
 	 * @param action the action to set
 	 */
 	public void setAction(URI task) {
 		this.action = URItoString(task);
		Action action = ActionResource.dao.load(task);
		if((action.getClass().equals(StackServiceAction.class))||(action.getClass().equals(ServiceAction.class))) this.isService = true;
 	}
 	/**
 	 * @return the parent
 	 */
 	public URI getParent() {
 		return stringToURI(parent);
 	}
 	/**
 	 * @param parent the parent to set
 	 */
 	public void setParent(URI parent) {
 		this.parent = URItoString(parent);
 	}
 	
 	/**
 	 * @return the root
 	 */
 	@XmlTransient
 	public Key<CloudProcess> getRoot() {
 		return root;
 	}
 	/**
 	 * @param root the root to set
 	 */
 	public void setRoot(Key<CloudProcess> root) {
 		this.root = root;
 	}
 
 	/**
 	 * @return the narrative
 	 */
 	@XmlElement
 	public Collection<Narrative> getNarrative() {
 		return this.topLevel? NarrativeResource.dao.getNarratives(this.getUri()) :
 							  NarrativeResource.dao.getProcessNarratives(this.getUri());
 	}
 	/**
 	 * @param narrative the narrative to set
 	 */
 	public void setNarrative(Collection<Narrative> narrative) {
 		
 	}
 	
 	/**
 	 * @return the description
 	 */
 	@XmlElement
 	public String getDescription() {
 		return ActionResource.dao.load(this.getAction()).getDescription();
 	}
 	/**
 	 * @param description this function does nothing, but appease jersey
 	 */
 	public void setDescription(String description) {
 		
 	}
 	
 	/**
 	 * @return the topLevel
 	 */
 	public boolean isTopLevel() {
 		return topLevel;
 	}
 	
 	/**
 	 * @return the topLevel
 	 */
 	public boolean getTopLevel() {
 		return topLevel;
 	}
 	
 	/**
 	 * @param topLevel the topLevel to set
 	 */
 	public void setTopLevel(boolean topLevel) {
 		this.topLevel = topLevel;
 	}
 	
 	/**
 	 * @return the pendingOnExit
 	 */
 	public List<URI> getPendingOnExit() {
 		ArrayList<URI> result = new ArrayList<URI>();
 		if(this.pendingOnExit != null) {
 			for(String s : this.pendingOnExit){
 				result.add(URI.create(s));
 			}
 		}
 		return result;
 	}
 	/**
 	 * @param pendingOnExit the pendingOnExit to set
 	 */
 	public void setPendingOnExit(List<URI> pendingOnExit) {
 		if(this.pendingOnExit != null) {
 			this.pendingOnExit.clear();
 		} else {
 			this.pendingOnExit = new ArrayList<String>();
 		}
 		if(pendingOnExit != null) {
 			for(URI u : pendingOnExit) {
 				this.pendingOnExit.add(u.toString());
 			}
 		}
 	}
 	
 	public void addPendingOnExit(List<CloudProcess> additions) {
 		if(this.pendingOnExit == null) {
 			this.pendingOnExit = new ArrayList<String>();
 		}
 		if(additions != null) {
 			for(CloudProcess p : additions) {
 				this.pendingOnExit.add(p.getUri().toString());
 			}
 		}
 	}
 	
 	/**
 	 * @return true if the process has pending OnExit processes
 	 */
 	public boolean hasPendingOnExit() {
 		return this.pendingOnExit != null && !this.pendingOnExit.isEmpty();
 	}
 	
 	/**
 	 * @return true is process is in zombie state
 	 */
 	public boolean isZombie() {
 		return this.state.ordinal() >= ActionState.ONEXIT.ordinal();
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return String
 				.format("CloudProcess [id=%s, state=%s, running=%s, waitTimeout=%s, pendingInit=%s, pendingCall=%s, pendingCancel=%s, pendingDump=%s, pendingAssertion=%s, dependentOn=%s, dependencyFor=%s, start=%s, complete=%s, finalized=%s, action=%s, parent=%s, root=%s, topLevel=%s, pendingOnExit=%s, costPerHour=%s, epoch=%s, account=%s]",
 						this.id, this.state, this.running, this.waitTimeout,
 						this.pendingInit, this.pendingCall, this.pendingCancel,
 						this.pendingDump, this.pendingAssertion,
 						this.dependentOn, this.dependencyFor, this.start,
 						this.complete, this.finalized, this.action,
 						this.parent, this.root, this.topLevel,
 						this.pendingOnExit, this.costPerHour, this.epoch,
 						this.account);
 	}
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result
 				+ ((this.account == null) ? 0 : this.account.hashCode());
 		result = prime * result
 				+ ((this.action == null) ? 0 : this.action.hashCode());
 		result = prime * result
 				+ ((this.complete == null) ? 0 : this.complete.hashCode());
 		long temp;
 		temp = Double.doubleToLongBits(this.costPerHour);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		result = prime
 				* result
 				+ ((this.dependencyFor == null) ? 0 : this.dependencyFor
 						.hashCode());
 		result = prime
 				* result
 				+ ((this.dependentOn == null) ? 0 : this.dependentOn.hashCode());
 		result = prime * result
 				+ ((this.epoch == null) ? 0 : this.epoch.hashCode());
 		result = prime * result + (this.finalized ? 1231 : 1237);
 		result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
 		result = prime * result
 				+ ((this.parent == null) ? 0 : this.parent.hashCode());
 		result = prime
 				* result
 				+ ((this.pendingAssertion == null) ? 0 : this.pendingAssertion
 						.hashCode());
 		result = prime * result + (this.pendingCall ? 1231 : 1237);
 		result = prime * result + (this.pendingCancel ? 1231 : 1237);
 		result = prime * result + (this.pendingDump ? 1231 : 1237);
 		result = prime * result + (this.pendingInit ? 1231 : 1237);
 		result = prime
 				* result
 				+ ((this.pendingOnExit == null) ? 0 : this.pendingOnExit
 						.hashCode());
 		result = prime * result
 				+ ((this.root == null) ? 0 : this.root.hashCode());
 		result = prime * result
 				+ ((this.running == null) ? 0 : this.running.hashCode());
 		result = prime * result
 				+ ((this.start == null) ? 0 : this.start.hashCode());
 		result = prime * result
 				+ ((this.state == null) ? 0 : this.state.hashCode());
 		result = prime * result + (this.topLevel ? 1231 : 1237);
 		result = prime
 				* result
 				+ ((this.waitTimeout == null) ? 0 : this.waitTimeout.hashCode());
 		return result;
 	}
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!super.equals(obj))
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		CloudProcess other = (CloudProcess) obj;
 		if (this.account == null) {
 			if (other.account != null)
 				return false;
 		} else if (!this.account.equals(other.account))
 			return false;
 		if (this.action == null) {
 			if (other.action != null)
 				return false;
 		} else if (!this.action.equals(other.action))
 			return false;
 		if (this.complete == null) {
 			if (other.complete != null)
 				return false;
 		} else if (!this.complete.equals(other.complete))
 			return false;
 		if (Double.doubleToLongBits(this.costPerHour) != Double
 				.doubleToLongBits(other.costPerHour))
 			return false;
 		if (this.dependencyFor == null) {
 			if (other.dependencyFor != null)
 				return false;
 		} else if (!this.dependencyFor.equals(other.dependencyFor))
 			return false;
 		if (this.dependentOn == null) {
 			if (other.dependentOn != null)
 				return false;
 		} else if (!this.dependentOn.equals(other.dependentOn))
 			return false;
 		if (this.epoch == null) {
 			if (other.epoch != null)
 				return false;
 		} else if (!this.epoch.equals(other.epoch))
 			return false;
 		if (this.finalized != other.finalized)
 			return false;
 		if (this.id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!this.id.equals(other.id))
 			return false;
 		if (this.parent == null) {
 			if (other.parent != null)
 				return false;
 		} else if (!this.parent.equals(other.parent))
 			return false;
 		if (this.pendingAssertion == null) {
 			if (other.pendingAssertion != null)
 				return false;
 		} else if (!this.pendingAssertion.equals(other.pendingAssertion))
 			return false;
 		if (this.pendingCall != other.pendingCall)
 			return false;
 		if (this.pendingCancel != other.pendingCancel)
 			return false;
 		if (this.pendingDump != other.pendingDump)
 			return false;
 		if (this.pendingInit != other.pendingInit)
 			return false;
 		if (this.pendingOnExit == null) {
 			if (other.pendingOnExit != null)
 				return false;
 		} else if (!this.pendingOnExit.equals(other.pendingOnExit))
 			return false;
 		if (this.root == null) {
 			if (other.root != null)
 				return false;
 		} else if (!this.root.equals(other.root))
 			return false;
 		if (this.running == null) {
 			if (other.running != null)
 				return false;
 		} else if (!this.running.equals(other.running))
 			return false;
 		if (this.start == null) {
 			if (other.start != null)
 				return false;
 		} else if (!this.start.equals(other.start))
 			return false;
 		if (this.state != other.state)
 			return false;
 		if (this.topLevel != other.topLevel)
 			return false;
 		if (this.waitTimeout == null) {
 			if (other.waitTimeout != null)
 				return false;
 		} else if (!this.waitTimeout.equals(other.waitTimeout))
 			return false;
 		return true;
 	}
 
 }
