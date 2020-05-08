 package fhdw.ipscrum.shared.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import fhdw.ipscrum.shared.bdas.BDACompare;
 import fhdw.ipscrum.shared.bdas.ManyToOne;
 import fhdw.ipscrum.shared.bdas.OneToMany;
 import fhdw.ipscrum.shared.constants.TextConstants;
 import fhdw.ipscrum.shared.exceptions.ConsistencyException;
 import fhdw.ipscrum.shared.exceptions.DoubleDefinitionException;
 import fhdw.ipscrum.shared.exceptions.ForbiddenStateException;
 import fhdw.ipscrum.shared.exceptions.NoSprintDefinedException;
 import fhdw.ipscrum.shared.exceptions.NoValidValueException;
 import fhdw.ipscrum.shared.model.interfaces.IPerson;
 import fhdw.ipscrum.shared.model.interfaces.IProductBacklogItemState;
 import fhdw.ipscrum.shared.model.interfaces.ISprint;
 import fhdw.ipscrum.shared.model.messages.PBICompletionMessage;
 import fhdw.ipscrum.shared.model.visitor.IProductBacklogItemVisitor;
 import fhdw.ipscrum.shared.observer.Observable;
 import fhdw.ipscrum.shared.observer.PersistentObserver;
 
 /**
  * Represents the abstract Root Class for a ProductBacklogItem.
  */
 public abstract class ProductBacklogItem extends Observable implements
 		BDACompare, Serializable, PersistentObserver {
 
 	private static final long serialVersionUID = 1599696800942615676L;
 
 	/**
 	 * Name of the sprint.
 	 */
 	private String name;
 
 	/**
 	 * Complexity of the PBI.
 	 */
 	private Effort manDayCosts;
 
 	/**
 	 * Last Editor of the PBI.
 	 */
 	private IPerson lastEditor;
 
 	/**
 	 * Returns the bidirectional association to the backlog.
 	 */
 	private ManyToOne<OneToMany<?, ?>, ProductBacklogItem> backlogAssoc;
 
 	/**
 	 * Returns the bidirectional association to the sprint.
 	 */
 	private ManyToOne<OneToMany<?, ?>, ProductBacklogItem> sprintAssoc;
 
 	private List<Relation> relations;
 
 	private List<Hint> hints;
 
 	private List<AcceptanceCriterion> acceptanceCriteria;
 
 	private String description;
 
 	/**
 	 * Default Constructor for GWT serialization.
 	 */
 	protected ProductBacklogItem() {
 	}
 
 	/**
 	 * @param name
 	 *            Name of the PBI.
 	 * @param description
 	 *            String
 	 * @param backlog
 	 *            Backlog of the PBI.
 	 * @throws ForbiddenStateException
 	 * @throws ConsistencyException
 	 * @throws NoValidValueException
 	 *             If the name for the PBI is not valid. Valid names are not
 	 *             null and have not only whitespace characters.
 	 * @throws DoubleDefinitionException
 	 *             If the name of the PBI already exist within the product
 	 *             backlog
 	 */
 	public ProductBacklogItem(final String name, final String description,
 			final ProductBacklog backlog) throws NoValidValueException,
 			DoubleDefinitionException, ConsistencyException,
 			ForbiddenStateException {
 		super();
 
 		this.relations = new ArrayList<Relation>();
 		this.acceptanceCriteria = new ArrayList<AcceptanceCriterion>();
 		this.hints = new ArrayList<Hint>();
 		this.backlogAssoc = new ManyToOne<OneToMany<?, ?>, ProductBacklogItem>(
 				this);
 		this.sprintAssoc = new ManyToOne<OneToMany<?, ?>, ProductBacklogItem>(
 				this);
 
 		this.description = description;
 		this.manDayCosts = new Effort(0);
 		this.initializeState();
 
 		this.name = name;
 		this.setBacklog(backlog);
 
 	}
 
 	/**
 	 * Visitor pattern operation for type determination.
 	 */
 	public abstract void accept(IProductBacklogItemVisitor visitor);
 
 	/**
 	 * adds a new {@link AcceptanceCriterion} to a feature.
 	 * 
 	 * @throws ForbiddenStateException
 	 *             will be thrown if the state does not allow this action
 	 * @throws DoubleDefinitionException
 	 *             will be thrown if the acceptanceCriterion already exists
 	 */
 	public void addAcceptanceCriterion(
 			final AcceptanceCriterion acceptanceCriterion)
 			throws DoubleDefinitionException, ForbiddenStateException {
 		this.getState().addAcceptanceCriterion(acceptanceCriterion);
 	}
 
 	/**
 	 * adds a new {@link Hint} to a feature.
 	 * 
 	 * @throws ForbiddenStateException
 	 *             will be thrown if the state does not allow this action
 	 * @throws DoubleDefinitionException
 	 *             will be thrown if the hint already exists
 	 * 
 	 */
 	public void addHint(final Hint hint) throws DoubleDefinitionException,
 			ForbiddenStateException {
 		this.getState().addHint(hint);
 	}
 
 	/**
 	 * adds a new {@link Relation} to a feature.
 	 * 
 	 * @throws ForbiddenStateException
 	 *             will be thrown if the state does not allow this action
 	 * @throws DoubleDefinitionException
 	 *             will be thrown if the relation already exists
 	 */
 	public void addRelation(final Relation relation)
 			throws DoubleDefinitionException, ForbiddenStateException {
 		this.getState().addRelation(relation);
 	}
 
 	/**
 	 * Sets the state of the feature to "closed".
 	 * 
 	 * @throws ForbiddenStateException
 	 *             will be thrown if the feature is already closed
 	 */
 	public void close() throws ForbiddenStateException {
 		this.getState().close();
		final PBICompletionMessage message = new PBICompletionMessage(this);
		this.notifyObservers(message);
 	}
 
 	protected void doAddAcceptanceCriterion(
 			final AcceptanceCriterion acceptanceCriterion)
 			throws DoubleDefinitionException {
 		final Iterator<AcceptanceCriterion> iterator = this.acceptanceCriteria
 				.iterator();
 		while (iterator.hasNext()) {
 			final AcceptanceCriterion current = iterator.next();
 			if (current.equals(acceptanceCriterion)) {
 				throw new DoubleDefinitionException(
 						fhdw.ipscrum.shared.constants.ExceptionConstants.DOUBLE_DEFINITION_ERROR);
 			}
 		}
 		this.acceptanceCriteria.add(acceptanceCriterion);
 		this.notifyObservers();
 	}
 
 	protected void doAddHint(final Hint hint) throws DoubleDefinitionException {
 		final Iterator<Hint> iterator = this.hints.iterator();
 		while (iterator.hasNext()) {
 			final Hint current = iterator.next();
 			if (current.equals(hint)) {
 				throw new DoubleDefinitionException(
 						fhdw.ipscrum.shared.constants.ExceptionConstants.DOUBLE_DEFINITION_ERROR);
 			}
 		}
 		this.hints.add(hint);
 		this.notifyObservers();
 	}
 
 	// /**
 	// * Checks if a pbi with a same name already exist within the given
 	// backlog.
 	// *
 	// * @param backlog
 	// * Product Backlog
 	// * @param name
 	// * Name of the PBI.
 	// * @throws NoValidValueException
 	// * If name is not valid (see constructor).
 	// * @throws DoubleDefinitionException
 	// * If name already exist.
 	// */
 	// private void checkName(final ProductBacklog backlog, final String name)
 	// throws NoValidValueException, DoubleDefinitionException {
 	// if (name != null && name.trim().length() > 0) {
 	// if (backlog != null) {
 	// for (final ProductBacklogItem item : backlog.getItems()) {
 	// if (!item.equals(this) && item.getName().equals(name)) {
 	// throw new DoubleDefinitionException(
 	// TextConstants.DOUBLE_DEFINITION_PBI);
 	// }
 	// }
 	// }
 	// this.name = name;
 	// } else {
 	// throw new NoValidValueException(TextConstants.MISSING_TEXT_ERROR);
 	// }
 	// }
 
 	protected void doAddRelation(final Relation relation)
 			throws DoubleDefinitionException {
 		final Iterator<Relation> iterator = this.relations.iterator();
 		while (iterator.hasNext()) {
 			final Relation current = iterator.next();
 			if (current.equals(relation)) {
 				throw new DoubleDefinitionException(
 						fhdw.ipscrum.shared.constants.ExceptionConstants.DOUBLE_DEFINITION_ERROR);
 			}
 		}
 		this.relations.add(relation);
 		this.notifyObservers();
 	}
 
 	protected abstract void doClose();
 
 	protected void doRemoveAcceptanceCriterion(
 			final AcceptanceCriterion acceptanceCriterion) {
 		this.acceptanceCriteria.remove(acceptanceCriterion);
 	}
 
 	protected void doRemoveHint(final Hint hint) {
 		this.hints.remove(hint);
 		this.notifyObservers();
 	}
 
 	protected void doRemoveRelation(final Relation relation) {
 		this.relations.remove(relation);
 		this.notifyObservers();
 	}
 
 	protected void doSetDescription(final String description) {
 		this.description = description;
 		this.notifyObservers();
 	}
 
 	protected void doSetLastEditor(final IPerson lastEditor)
 			throws ForbiddenStateException {
 		this.lastEditor = lastEditor;
 		this.notifyObservers();
 	}
 
 	protected void doSetManDayCosts(final Effort manDayCosts)
 			throws NoValidValueException, ForbiddenStateException {
 		if (manDayCosts != null && manDayCosts.getValue() >= 0) {
 			this.manDayCosts.setValue(manDayCosts.getValue());
 			this.notifyObservers();
 		} else {
 			throw new NoValidValueException(TextConstants.MANDAYS_ERROR);
 		}
 	}
 
 	protected void doSetName(final String name) throws NoValidValueException,
 			DoubleDefinitionException, ConsistencyException,
 			ForbiddenStateException {
 		if (this.getBacklog() != null) {
 			if (name != null && name.trim().length() > 0) {
 				if (this.getBacklog() != null) {
 					for (final ProductBacklogItem item : this.getBacklog()
 							.getItems()) {
 						if (item != this && item.getName().equals(name)) {
 							throw new DoubleDefinitionException(
 									TextConstants.DOUBLE_DEFINITION_PBI);
 						}
 					}
 				}
 				this.name = name;
 			} else {
 				throw new NoValidValueException(
 						TextConstants.MISSING_TEXT_ERROR);
 			}
 		} else {
 			throw new ConsistencyException(TextConstants.PBL_PBI_ERROR);
 		}
 	}
 
 	protected void doSetSprint(final ISprint sprint)
 			throws NoSprintDefinedException, ConsistencyException,
 			ForbiddenStateException {
 		if (this.getSprint() != null) {
 			this.getSprint().deleteObserver(this);
 		}
 		if (sprint != null) {
 			this.getBacklog().getProject().isSprintDefined(sprint);
 			this.getSprintAssoc().set((sprint.getToPBIAssoc()));
 			sprint.addObserver(this);
 		} else {
 			this.getSprintAssoc().set(null);
 		}
 
 		this.notifyObservers();
 	}
 
 	@Override
 	public boolean equals(final Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (!super.equals(obj)) {
 			return false;
 		}
 		if (this.getClass() != obj.getClass()) {
 			return false;
 		}
 		final ProductBacklogItem other = (ProductBacklogItem) obj;
 		if (this.backlogAssoc == null) {
 			if (other.backlogAssoc != null) {
 				return false;
 			}
 		} else if (!this.backlogAssoc.equals(other.backlogAssoc)) {
 			return false;
 		}
 		if (this.lastEditor == null) {
 			if (other.lastEditor != null) {
 				return false;
 			}
 		} else if (!this.lastEditor.equals(other.lastEditor)) {
 			return false;
 		}
 		if (this.manDayCosts == null) {
 			if (other.manDayCosts != null) {
 				return false;
 			}
 		} else if (!this.manDayCosts.equals(other.manDayCosts)) {
 			return false;
 		}
 		if (this.name == null) {
 			if (other.name != null) {
 				return false;
 			}
 		} else if (!this.name.equals(other.name)) {
 			return false;
 		}
 		if (this.sprintAssoc == null) {
 			if (other.sprintAssoc != null) {
 				return false;
 			}
 		} else if (!this.sprintAssoc.equals(other.sprintAssoc)) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * @return the acceptanceCriteria
 	 */
 	public List<AcceptanceCriterion> getAcceptanceCriteria() {
 		return Collections.unmodifiableList(this.acceptanceCriteria);
 	}
 
 	/**
 	 * Returns the backlog of the pbi.
 	 */
 	public ProductBacklog getBacklog() {
 		return (ProductBacklog) this.getBacklogAssoc().get();
 	}
 
 	/**
 	 * Returns the bidirectional association to the backlog.
 	 */
 	protected ManyToOne<OneToMany<?, ?>, ProductBacklogItem> getBacklogAssoc() {
 		return this.backlogAssoc;
 	}
 
 	public String getDescription() {
 		return this.description;
 	}
 
 	/**
 	 * @return the hints
 	 */
 	public List<Hint> getHints() {
 		return Collections.unmodifiableList(this.hints);
 	}
 
 	/**
 	 * Returns the last editor of the pbi.
 	 */
 	public IPerson getLastEditor() {
 		return this.lastEditor;
 	}
 
 	/**
 	 * Returns the complexity of the pbi.
 	 */
 	public Effort getManDayCosts() {
 		return this.manDayCosts;
 	}
 
 	/**
 	 * Returns the name of the pbi.
 	 */
 	public String getName() {
 		return this.name;
 	}
 
 	/**
 	 * @return the relations
 	 */
 	public List<Relation> getRelations() {
 		return Collections.unmodifiableList(this.relations);
 	}
 
 	/**
 	 * Returns the sprint if it was set else null will be returned.
 	 */
 	public ISprint getSprint() {
 		return (ISprint) this.getSprintAssoc().get();
 	}
 
 	/**
 	 * Returns the bidirectional association to the sprint.
 	 */
 	protected ManyToOne<OneToMany<?, ?>, ProductBacklogItem> getSprintAssoc() {
 		return this.sprintAssoc;
 	}
 
 	public abstract IProductBacklogItemState getState();
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = this.indirectHashCode();
 		result = prime
 				* result
 				+ ((this.backlogAssoc == null) ? 0 : this.backlogAssoc
 						.hashCode());
 		result = prime * result
 				+ ((this.lastEditor == null) ? 0 : this.lastEditor.hashCode());
 		result = prime
 				* result
 				+ ((this.sprintAssoc == null) ? 0 : this.sprintAssoc.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean indirectEquals(final Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (!super.equals(obj)) {
 			return false;
 		}
 		if (this.getClass() != obj.getClass()) {
 			return false;
 		}
 		final ProductBacklogItem other = (ProductBacklogItem) obj;
 		if (this.lastEditor == null) {
 			if (other.lastEditor != null) {
 				return false;
 			}
 		} else if (!this.lastEditor.equals(other.lastEditor)) {
 			return false;
 		}
 		if (this.manDayCosts == null) {
 			if (other.manDayCosts != null) {
 				return false;
 			}
 		} else if (!this.manDayCosts.equals(other.manDayCosts)) {
 			return false;
 		}
 		if (this.name == null) {
 			if (other.name != null) {
 				return false;
 			}
 		} else if (!this.name.equals(other.name)) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public int indirectHashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime
 				* result
 				+ ((this.manDayCosts == null) ? 0 : this.manDayCosts.hashCode());
 		result = prime * result
 				+ ((this.name == null) ? 0 : this.name.hashCode());
 		return result;
 	}
 
 	/**
 	 * @author stefan pietsch group 2 in phase 2 optional operation for
 	 *         subclasses to initialize before super call, for example
 	 *         initialize new attributes.
 	 */
 	protected abstract void initializeState();
 
 	/**
 	 * removes an {@link AcceptanceCriterion} from this feature.
 	 * 
 	 * @throws ForbiddenStateException
 	 *             will be thrown if the state does not allow this action
 	 */
 	public void removeAcceptanceCriterion(final AcceptanceCriterion criterion)
 			throws ForbiddenStateException {
 		this.getState().removeAcceptanceCriterion(criterion);
 		this.notifyObservers();
 
 	}
 
 	/**
 	 * removes a {@link Hint} from this feature.
 	 * 
 	 * @throws ForbiddenStateException
 	 *             will be thrown if the state does not allow this action
 	 */
 	public void removeHint(final Hint hint) throws ForbiddenStateException {
 		this.getState().removeHint(hint);
 		this.notifyObservers();
 	}
 
 	/**
 	 * removes a {@link Relation} from this feature.
 	 * 
 	 * @throws ForbiddenStateException
 	 *             will be thrown if the state does not allow this action
 	 */
 	public void removeRelation(final Relation relation)
 			throws ForbiddenStateException {
 		this.getState().removeRelation(relation);
 		this.notifyObservers();
 	}
 
 	private void setBacklog(final ProductBacklog backlog) {
 		this.getBacklogAssoc().set(backlog.getAssoc());
 	}
 
 	/**
 	 * Sets the description of the feature object.
 	 * 
 	 * @param description
 	 * @throws ForbiddenStateException
 	 */
 	public void setDescription(final String description)
 			throws ForbiddenStateException {
 		this.getState().setDescription(description);
 	}
 
 	public void setLastEditor(final IPerson lastEditor)
 			throws ForbiddenStateException {
 		this.getState().setLastEditor(lastEditor);
 	}
 
 	/**
 	 * Changes the Complexity of the pbi.
 	 * 
 	 * @param manDayCosts
 	 *            Values smaller 0 are not allowed. 0 means not defined.
 	 * @throws NoValidValueException
 	 *             If the value is smaller 0!
 	 * @throws ForbiddenStateException
 	 *             If the pbi was closed.
 	 */
 	public void setManDayCosts(final Effort manDayCosts)
 			throws NoValidValueException, ForbiddenStateException {
 		this.getState().setManDayCosts(manDayCosts);
 	}
 
 	/**
 	 * Changes the Name of the PBI.
 	 * 
 	 * @param name
 	 *            New Name of the PBI.
 	 * @throws NoValidValueException
 	 *             If the name for the PBI is not valid. Valid names are not
 	 *             null and have not only whitespace characters.
 	 * @throws ForbiddenStateException
 	 *             if the pbi was closed. * @throws DoubleDefinitionException if
 	 *             a pbi with the same name already exist within the product
 	 *             backlog
 	 */
 	public void setName(final String name) throws NoValidValueException,
 			DoubleDefinitionException, ConsistencyException,
 			ForbiddenStateException {
 		this.getState().setName(name);
 	}
 
 	/**
 	 * Changes the sprint of a pbi.
 	 * 
 	 * @param sprint
 	 *            Null Value Means, that the PBI will be removed from the
 	 *            sprint!
 	 * @throws NoSprintDefinedException
 	 *             If the sprint is not defined within the project the pbi
 	 *             belongs to.
 	 * @throws ForbiddenStateException
 	 *             The pbi was closed.
 	 */
 	public void setSprint(final ISprint sprint)
 			throws NoSprintDefinedException, ConsistencyException,
 			ForbiddenStateException {
 		this.getState().setSprint(sprint);
 	}
 
 	@Override
 	public String toString() {
 		return "ProductBacklogItem [aufwand=" + this.manDayCosts + ", name="
 				+ this.name + "]";
 	}
 
 	@Override
 	public void update(Observable observable, Object argument) {
 		this.notifyObservers();
 	}
 }
