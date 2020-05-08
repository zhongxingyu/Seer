 /* Created Feb 23, 2009 by Andrew Butler */
 package org.muis.core;
 
 import static org.muis.core.MuisConstants.Events.*;
 
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.muis.core.MuisConstants.CoreStage;
 import org.muis.core.MuisConstants.States;
 import org.muis.core.event.*;
 import org.muis.core.layout.SimpleSizeGuide;
 import org.muis.core.layout.SizeGuide;
 import org.muis.core.mgr.*;
 import org.muis.core.mgr.MuisLifeCycleManager.Controller;
 import org.muis.core.style.BackgroundStyles;
 import org.muis.core.style.MuisStyle;
 import org.muis.core.style.Texture;
 import org.muis.core.style.attach.*;
 import org.muis.core.tags.State;
 import org.muis.core.tags.StateSupport;
 
 import prisms.arch.event.ListenerManager;
 import prisms.util.ArrayUtils;
 
 /** The base display element in MUIS. Contains base methods to administer content (children, style, placement, etc.) */
 @StateSupport({@State(name = States.CLICK_NAME, priority = States.CLICK_PRIORITY),
 		@State(name = States.RIGHT_CLICK_NAME, priority = States.RIGHT_CLICK_PRIORITY),
 		@State(name = States.MIDDLE_CLICK_NAME, priority = States.MIDDLE_CLICK_PRIORITY),
 		@State(name = States.HOVER_NAME, priority = States.HOVER_PRIORITY),
 		@State(name = States.FOCUS_NAME, priority = States.FOCUS_PRIORITY)})
 public abstract class MuisElement {
 	/**
 	 * Used to lock this elements' child sets
 	 *
 	 * @see MuisLock
 	 */
 	public static final String CHILDREN_LOCK_TYPE = "Muis Child Lock";
 
 	private final MuisLifeCycleManager theLifeCycleManager;
 	private MuisLifeCycleManager.Controller theLifeCycleController;
 	private final StateEngine theStateEngine;
 	private final MuisMessageCenter theMessageCenter;
 
 	private MuisDocument theDocument;
 	private MuisToolkit theToolkit;
 	private MuisElement theParent;
 	private MuisClassView theClassView;
 
 	private String theNamespace;
 	private String theTagName;
 
 	private final AttributeManager theAttributeManager;
 	private final ChildList theChildren;
 
 	private final ImmutableChildList<MuisElement> theExposedChildren;
 	private final ElementStyle theStyle;
 	private final CompoundStyleListener theDefaultStyleListener;
 
 	@SuppressWarnings({"rawtypes"})
 	private final ListenerManager<MuisEventListener> theListeners;
 
 	private final CoreStateControllers theStateControllers;
 
 	private int theZ;
 
 	private ElementBounds theBounds;
 	private SizeGuide theHSizer;
 	private SizeGuide theVSizer;
 
 	private boolean isFocusable;
 	private Rectangle theCacheBounds;
 	private long thePaintDirtyTime;
 	private long theLayoutDirtyTime;
 
 	/** Creates a MUIS element */
 	public MuisElement() {
 		theListeners = new ListenerManager<>(MuisEventListener.class);
 		theMessageCenter = new MuisMessageCenter(null, null, this);
 		theLifeCycleManager = new MuisLifeCycleManager(this, new MuisLifeCycleManager.ControlAcceptor() {
 			@Override
 			public void setController(Controller controller) {
 				theLifeCycleController = controller;
 			}
 		}, CoreStage.READY.toString());
 		theStateEngine = new StateEngine();
 		theStateControllers = new CoreStateControllers();
 		String lastStage = null;
 		for(CoreStage stage : CoreStage.values())
 			if(stage != CoreStage.OTHER && stage != CoreStage.READY) {
 				theLifeCycleManager.addStage(stage.toString(), lastStage);
 				lastStage = stage.toString();
 			}
 		theBounds = new ElementBounds(this);
 		theChildren = new ChildList(this);
 		theExposedChildren = new ImmutableChildList<>(theChildren);
 		theAttributeManager = new AttributeManager(this);
 		theCacheBounds = new Rectangle();
 		theStyle = new ElementStyle(this);
 		theDefaultStyleListener = new CompoundStyleListener(this) {
 			@Override
 			public void styleChanged(MuisStyle style) {
 				repaint(null, false);
 			}
 		};
 		theDefaultStyleListener.addDomain(BackgroundStyles.getDomainInstance());
 		theDefaultStyleListener.addDomain(org.muis.core.style.LightedStyle.getDomainInstance());
 		theDefaultStyleListener.add();
 		MuisEventListener<MuisElement> childListener = new MuisEventListener<MuisElement>() {
 			@Override
 			public void eventOccurred(MuisEvent<MuisElement> event, MuisElement element) {
 				relayout(false);
 				if(event.getType() == CHILD_REMOVED) {
 					// Need to repaint where the element left even if nothing changes as a result of the layout
 					unregisterChild(event.getValue());
 					repaint(element.theBounds.getBounds(), false);
 				} else if(event.getType() == CHILD_ADDED) {
 					registerChild(event.getValue());
 				}
 			}
 
 			@Override
 			public boolean isLocal() {
 				return true;
 			}
 		};
 		addListener(CHILD_ADDED, childListener);
 		addListener(CHILD_REMOVED, childListener);
 		theChildren.addChildListener(BOUNDS_CHANGED, new MuisEventListener<Rectangle>() {
 			@Override
 			public void eventOccurred(MuisEvent<Rectangle> event, MuisElement element) {
 				Rectangle paintRect = event.getValue().union(((MuisPropertyEvent<Rectangle>) event).getOldValue());
 				repaint(paintRect, false);
 			}
 
 			@Override
 			public boolean isLocal() {
 				return true;
 			}
 		});
 		Object styleWanter = new Object();
 		theAttributeManager.accept(styleWanter, org.muis.core.style.attach.StyleAttributeType.STYLE_ATTRIBUTE);
 		theAttributeManager.accept(styleWanter, GroupPropertyType.attribute);
 		addListener(ATTRIBUTE_CHANGED,
 			(MuisEventListener<Object>) org.muis.core.style.attach.StyleAttributeType.STYLE_ATTRIBUTE.getPathAccepter());
 		final boolean [] groupCallbackLock = new boolean[1];
 		addListener(ATTRIBUTE_CHANGED, new org.muis.core.event.AttributeChangedListener<String []>(
 			org.muis.core.style.attach.GroupPropertyType.attribute) {
 			@Override
 			public void attributeChanged(AttributeChangedEvent<String []> event) {
 				if(groupCallbackLock[0])
 					return;
 				groupCallbackLock[0] = true;
 				try {
 					ArrayList<NamedStyleGroup> groups = new ArrayList<>();
 					for(NamedStyleGroup group : getDocument().groups())
 						groups.add(group);
 					ArrayUtils.adjust(groups.toArray(new NamedStyleGroup[0]), event.getValue(),
 						new ArrayUtils.DifferenceListener<NamedStyleGroup, String>() {
 							@Override
 							public boolean identity(NamedStyleGroup o1, String o2) {
 								return o1.getName().equals(o2);
 							}
 
 							@Override
 							public NamedStyleGroup added(String o, int mIdx, int retIdx) {
 								getStyle().addGroup(getDocument().getGroup(o));
 								return null;
 							}
 
 							@Override
 							public NamedStyleGroup removed(NamedStyleGroup o, int oIdx, int incMod, int retIdx) {
 								if(o.isMember(MuisElement.this))
 									getStyle().removeGroup(o);
 								return null;
 							}
 
 							@Override
 							public NamedStyleGroup set(NamedStyleGroup o1, int idx1, int incMod, String o2, int idx2, int retIdx) {
 								if(!o1.isMember(MuisElement.this))
 									getStyle().addGroup(o1);
 								return null;
 							}
 						});
 				} finally {
 					groupCallbackLock[0] = false;
 				}
 			}
 		});
 		addListener(GroupMemberEvent.TYPE, new MuisEventListener<NamedStyleGroup>() {
 			@Override
 			public void eventOccurred(MuisEvent<NamedStyleGroup> event, MuisElement element) {
 				if(groupCallbackLock[0])
 					return;
 				groupCallbackLock[0] = true;
 				try {
 					GroupMemberEvent gme = (GroupMemberEvent) event;
 					ArrayList<String> groupList = new ArrayList<>();
 					for(NamedStyleGroup group : getStyle().groups(true))
 						groupList.add(group.getName());
 					String [] groups = groupList.toArray(new String[groupList.size()]);
 					if(!ArrayUtils.equals(groups, atts().get(GroupPropertyType.attribute)))
 						try {
 							atts().set(GroupPropertyType.attribute, groups);
 						} catch(MuisException e) {
 							msg().warn("Error reconciling " + GroupPropertyType.attribute + " attribute with group membership change", e,
 								"group", gme.getValue());
 						}
 				} finally {
 					groupCallbackLock[0] = false;
 				}
 			}
 
 			@Override
 			public boolean isLocal() {
 				return true;
 			}
 		});
 		addListener(BOUNDS_CHANGED, new MuisEventListener<Rectangle>() {
 			@Override
 			public void eventOccurred(MuisEvent<Rectangle> event, MuisElement element) {
 				Rectangle old = ((MuisPropertyEvent<Rectangle>) event).getOldValue();
 				if(event.getValue().width != old.width || event.getValue().height != old.height)
 					relayout(false);
 			}
 
 			@Override
 			public boolean isLocal() {
 				return true;
 			}
 		});
 		theLifeCycleManager.runWhen(new Runnable() {
 			@Override
 			public void run() {
 				repaint(null, false);
 			}
 		}, CoreStage.INIT_SELF.toString(), 2);
 		addAnnotatedStates();
 		addStateListeners();
 		theLifeCycleController.advance(CoreStage.PARSE_SELF.toString());
 	}
 
 	private void addAnnotatedStates() {
 		Class<?> type = getClass();
 		while(MuisElement.class.isAssignableFrom(type)) {
 			StateSupport states = type.getAnnotation(StateSupport.class);
 			if(states != null)
 				for(State state : states.value()) {
 					try {
 						theStateEngine.addState(new MuisState(state.name(), state.priority()));
 					} catch(IllegalArgumentException e) {
 						msg().warn(e.getMessage(), "state", state);
 					}
 				}
 			type = type.getSuperclass();
 		}
 	}
 
 	private void addStateListeners() {
 		theStateControllers.clicked = theStateEngine.control(States.CLICK);
 		theStateControllers.rightClicked = theStateEngine.control(States.RIGHT_CLICK);
 		theStateControllers.middleClicked = theStateEngine.control(States.MIDDLE_CLICK);
 		theStateControllers.hovered = theStateEngine.control(States.HOVER);
 		theStateControllers.focused = theStateEngine.control(States.FOCUS);
 		addListener(MOUSE, new MuisEventListener<Void>() {
 			@Override
 			public void eventOccurred(MuisEvent<Void> event, MuisElement element) {
 				org.muis.core.event.MouseEvent mouse = (org.muis.core.event.MouseEvent) event;
 				switch (mouse.getMouseEventType()) {
 				case pressed:
 					switch (mouse.getButtonType()) {
 					case LEFT:
 						theStateControllers.clicked.setActive(true, mouse);
 						break;
 					case RIGHT:
 						theStateControllers.rightClicked.setActive(true, mouse);
 						break;
 					case MIDDLE:
 						theStateControllers.middleClicked.setActive(true, mouse);
 						break;
 					default:
 						break;
 					}
 					break;
 				case released:
 					switch (mouse.getButtonType()) {
 					case LEFT:
 						theStateControllers.clicked.setActive(false, mouse);
 						break;
 					case RIGHT:
 						theStateControllers.rightClicked.setActive(false, mouse);
 						break;
 					case MIDDLE:
 						theStateControllers.middleClicked.setActive(false, mouse);
 						break;
 					default:
 						break;
 					}
 					break;
 				case clicked:
 					break;
 				case moved:
 					break;
 				case entered:
 					theStateControllers.hovered.setActive(true, mouse);
 					for(org.muis.core.event.MouseEvent.ButtonType button : theDocument.getPressedButtons()) {
 						switch (button) {
 						case LEFT:
 							theStateControllers.clicked.setActive(true, mouse);
 							break;
 						case RIGHT:
 							theStateControllers.rightClicked.setActive(true, mouse);
 							break;
 						case MIDDLE:
 							theStateControllers.middleClicked.setActive(true, mouse);
 							break;
 						default:
 							break;
 						}
 					}
 					break;
 				case exited:
 					theStateControllers.clicked.setActive(false, mouse);
 					theStateControllers.rightClicked.setActive(false, mouse);
 					theStateControllers.middleClicked.setActive(false, mouse);
 					theStateControllers.hovered.setActive(false, mouse);
 					break;
 				}
 			}
 
 			@Override
 			public boolean isLocal() {
 				return true;
 			}
 		});
 		addListener(FOCUS, new MuisEventListener<Void>() {
 			@Override
 			public void eventOccurred(MuisEvent<Void> event, MuisElement element) {
 				org.muis.core.event.FocusEvent focus = (org.muis.core.event.FocusEvent) event;
 				theStateControllers.focused.setActive(focus.isFocus(), focus);
 			}
 
 			@Override
 			public boolean isLocal() {
 				return true;
 			}
 		});
 	}
 
 	/** @return The document that this element belongs to */
 	public final MuisDocument getDocument() {
 		return theDocument;
 	}
 
 	/** @return The tool kit that this element belongs to */
 	public final MuisToolkit getToolkit() {
 		return theToolkit;
 	}
 
 	/** @return The MUIS class view that allows for instantiation of child elements */
 	public final MuisClassView getClassView() {
 		return theClassView;
 	}
 
 	/** @return The namespace that this tag was instantiated in */
 	public final String getNamespace() {
 		return theNamespace;
 	}
 
 	/** @return The name of the tag that was used to instantiate this element */
 	public final String getTagName() {
 		return theTagName;
 	}
 
 	/** @return The state engine that controls this element's states */
 	public StateEngine getStateEngine() {
 		return theStateEngine;
 	}
 
 	/**
 	 * Short-hand for {@link #getStateEngine()}
 	 *
 	 * @return The state engine that controls this element's states
 	 */
 	public StateEngine state() {
 		return getStateEngine();
 	}
 
 	/** @return The manager of this element's attributes */
 	public AttributeManager getAttributeManager() {
 		return theAttributeManager;
 	}
 
 	/**
 	 * Short-hand for {@link #getAttributeManager()}
 	 *
 	 * @return The manager of this element's attributes
 	 */
 	public AttributeManager atts() {
 		return getAttributeManager();
 	}
 
 	/** @return The style that modifies this element's appearance */
 	public final ElementStyle getStyle() {
 		return theStyle;
 	}
 
 	// Life cycle methods
 
 	/**
 	 * Returns a life cycle manager that allows subclasses to customize and hook into the life cycle for this element.
 	 *
 	 * @return The life cycle manager for this element
 	 */
 	public MuisLifeCycleManager getLifeCycleManager() {
 		return theLifeCycleManager;
 	}
 
 	/**
 	 * Short-hand for {@link #getLifeCycleManager()}
 	 *
 	 * @return The life cycle manager for this element
 	 */
 	public MuisLifeCycleManager life() {
 		return getLifeCycleManager();
 	}
 
 	/**
 	 * Initializes an element's core information
 	 *
 	 * @param doc The document that this element belongs to
 	 * @param toolkit The toolkit that this element belongs to
 	 * @param classView The class view for this element
 	 * @param parent The parent that this element is under
 	 * @param namespace The namespace used to create this element
 	 * @param tagName The tag name used to create this element
 	 */
 	public final void init(MuisDocument doc, MuisToolkit toolkit, MuisClassView classView, MuisElement parent, String namespace,
 		String tagName) {
 		theLifeCycleController.advance(CoreStage.INIT_SELF.toString());
 		if(doc == null)
 			throw new IllegalArgumentException("Cannot create an element without a document");
 		if(theDocument != null)
 			throw new IllegalStateException("An element cannot be initialized twice", null);
 		theDocument = doc;
 		theToolkit = toolkit;
 		theNamespace = namespace;
 		theTagName = tagName;
 		theClassView = classView;
 		setParent(parent);
 		theLifeCycleController.advance(CoreStage.PARSE_CHILDREN.toString());
 	}
 
 	/**
 	 * Initializes an element's descendants
 	 *
 	 * @param children The child elements specified in the MUIS XML
 	 * @return The child list that the children are populated into
 	 */
 	public ElementList<? extends MuisElement> initChildren(MuisElement [] children) {
 		theLifeCycleController.advance(CoreStage.INIT_CHILDREN.toString());
 		try (MuisLock lock = theDocument.getLocker().lock(CHILDREN_LOCK_TYPE, this, true)) {
 			theChildren.clear();
 			theChildren.addAll(children);
 		}
 		for(MuisElement child : children)
 			registerChild(child);
 		if(theBounds.getWidth() != 0 && theBounds.getHeight() != 0) // No point laying out if there's nothing to show
 			relayout(false);
 		theLifeCycleController.advance(CoreStage.INITIALIZED.toString());
 		return theChildren;
 	}
 
 	/**
 	 * Called when a child is introduced to this parent
 	 *
 	 * @param child The child that has been added to this parent
 	 */
 	protected void registerChild(MuisElement child) {
 		if(child.getParent() != this)
 			child.setParent(this);
 	}
 
 	/**
 	 * Called when a child is removed to this parent
 	 *
 	 * @param child The child that has been removed from this parent
 	 */
 	protected void unregisterChild(MuisElement child) {
 		if(child.getParent() == this)
 			child.setParent(null);
 	}
 
 	/** Called to initialize an element after all the parsing and linking has been performed */
 	public final void postCreate() {
 		theLifeCycleController.advance(CoreStage.STARTUP.toString());
 		for(MuisElement child : theChildren)
 			child.postCreate();
 		theLifeCycleController.advance(CoreStage.READY.toString());
 	}
 
 	// End life cycle methods
 
 	// Messaging methods
 
 	/**
 	 * Returns a message center that allows messaging on this element
 	 *
 	 * @return This element's message center
 	 */
 	public MuisMessageCenter getMessageCenter() {
 		return theMessageCenter;
 	}
 
 	/**
 	 * Short-hand for {@link #getMessageCenter()}
 	 *
 	 * @return This element's message center
 	 */
 	public MuisMessageCenter msg() {
 		return getMessageCenter();
 	}
 
 	/** @return The worst type of messages in this element and its children */
 	public MuisMessage.Type getWorstMessageType() {
 		MuisMessage.Type ret = theMessageCenter.getWorstMessageType();
 		for(MuisElement child : theChildren) {
 			MuisMessage.Type childType = child.theMessageCenter.getWorstMessageType();
 			if(ret == null || ret.compareTo(childType) < 0)
 				ret = childType;
 		}
 		return ret;
 	}
 
 	/** @return All messages in this element or any of its children */
 	public Iterable<MuisMessage> allMessages() {
 		ArrayList<Iterable<MuisMessage>> centers = new ArrayList<>();
 		centers.add(theMessageCenter);
 		for(MuisElement child : theChildren)
 			centers.add(child.allMessages());
 		return ArrayUtils.iterable(centers.toArray(new Iterable[centers.size()]));
 	}
 
 	// End messaging methods
 
 	// Hierarchy methods
 
 	/** @return This element's parent in the DOM tree */
 	public final MuisElement getParent() {
 		return theParent;
 	}
 
 	/**
 	 * Sets this element's parent after initialization
 	 *
 	 * @param parent The new parent for this element
 	 */
 	protected final void setParent(MuisElement parent) {
 		if(theParent != null) {
 			theParent.theChildren.remove(this);
 		}
 		theParent = parent;
 		fireEvent(new MuisEvent<>(ELEMENT_MOVED, theParent), false, false);
 	}
 
 	/** @return An unmodifiable list of this element's children */
 	public ImmutableChildList<? extends MuisElement> getChildren() {
 		return theExposedChildren;
 	}
 
 	/**
 	 * Short-hand for {@link #getChildren()}
 	 *
 	 * @return An unmodifiable list of this element's children
 	 */
 	public ImmutableChildList<? extends MuisElement> ch() {
 		return getChildren();
 	}
 
 	/** @return An augmented, modifiable {@link List} of this element's children */
 	protected ChildList getChildManager() {
 		return theChildren;
 	}
 
 	/**
 	 * Checks to see if this element is in the subtree rooted at the given element
 	 *
 	 * @param ancestor The element whose subtree to check
 	 * @return Whether this element is in the ancestor's subtree
 	 */
 	public final boolean isAncestor(MuisElement ancestor) {
 		if(ancestor == this)
 			return true;
 		MuisElement parent = theParent;
 		while(parent != null) {
 			if(parent == ancestor)
 				return true;
 			parent = parent.theParent;
 		}
 		return false;
 	}
 
 	/**
 	 * @param x The x-coordinate of a point relative to this element's upper left corner
 	 * @param y The y-coordinate of a point relative to this element's upper left corner
 	 * @return The deepest (and largest-Z) descendant of this element whose bounds contain the given point
 	 */
 	public final MuisElement deepestChildAt(int x, int y) {
 		MuisElement current = this;
 		MuisElement [] children = current.theChildren.at(x, y);
 		while(children.length > 0) {
 			x -= current.theBounds.getX();
 			y -= current.theBounds.getY();
 			current = children[0];
 			children = current.theChildren.at(x, y);
 		}
 		return current;
 	}
 
 	// End hierarchy methods
 
 	/**
 	 * @return The default style listener to add domains and styles to listen to. When one of the registered styles changes, this element
 	 *         repaints itself.
 	 */
 	public final CompoundStyleListener getDefaultStyleListener() {
 		return theDefaultStyleListener;
 	}
 
 	// Bounds methods
 
 	/** @return The bounds of this element */
 	public final ElementBounds getBounds() {
 		return theBounds;
 	}
 
 	/** @return The bounds of this element */
 	public final ElementBounds bounds() {
 		return theBounds;
 	}
 
 	/** @return The z-index determining the order in which this element is drawn among its siblings */
 	public final int getZ() {
 		return theZ;
 	}
 
 	/** @param z The z-index determining the order in which this element is drawn among its siblings */
 	public final void setZ(int z) {
 		if(theZ == z)
 			return;
 		theZ = z;
 		if(theParent != null)
 			theParent.repaint(new Rectangle(theBounds.getX(), theBounds.getY(), theBounds.getWidth(), theBounds.getHeight()), false);
 	}
 
 	/** @return The size policy for this item's width */
 	public SizeGuide getWSizer() {
 		if(theHSizer == null)
 			theHSizer = new SimpleSizeGuide();
 		return theHSizer;
 	}
 
 	/** @return The size policy for this item's height */
 	public SizeGuide getHSizer() {
 		if(theVSizer == null)
 			theVSizer = new SimpleSizeGuide();
 		return theVSizer;
 	}
 
 	/** @return This element's position relative to the document's root */
 	public final Point getDocumentPosition() {
 		int x = 0;
 		int y = 0;
 		MuisElement el = this;
 		while(el.theParent != null) {
 			x += el.theBounds.getX();
 			y += el.theBounds.getY();
 			el = el.theParent;
 		}
 		return new Point(x, y);
 	}
 
 	// End bounds methods
 
 	/**
 	 * @return Whether positional events are consumed by this element, or whether they should be propagated to elements under this element.
 	 *         By default, this method returns true if and only if the background transparency is one.
 	 */
 	public boolean isClickThrough() {
 		return getStyle().getSelf().get(BackgroundStyles.transparency) >= 1;
 	}
 
 	/** @return Whether this element is able to accept the focus for the document */
 	public boolean isFocusable() {
 		return isFocusable;
 	}
 
 	/** @param focusable Whether this element should be focusable */
 	protected final void setFocusable(boolean focusable) {
 		isFocusable = focusable;
 	}
 
 	// Event methods
 
 	/**
 	 * Adds a listener for an event type to this element
 	 *
 	 * @param <T> The type of the property that the event type represents
 	 * @param type The event type to listen for
 	 * @param listener The listener to notify when an event of the given type occurs
 	 */
 	public final <T> void addListener(MuisEventType<T> type, MuisEventListener<? super T> listener) {
 		theListeners.addListener(type, listener);
 	}
 
 	/** @param listener The listener to remove from this element */
 	public final void removeListener(MuisEventListener<?> listener) {
 		theListeners.removeListener(listener);
 	}
 
 	/**
 	 * Removes all listeners for the given event type whose class is exactly equal (not an extension of) the given listener type
 	 *
 	 * @param <T> The type of the property that the event type represents
 	 * @param type The type of event to stop listening for
 	 * @param listenerType The listener type to remove
 	 */
 	public final <T> void removeListener(MuisEventType<T> type, Class<? extends MuisEventListener<? super T>> listenerType) {
 		for(MuisEventListener<?> listener : theListeners.getListeners(type))
 			if(listener.getClass() == listenerType)
 				theListeners.removeListener(listener);
 	}
 
 	/**
 	 * Fires an event on this element
 	 *
 	 * @param <T> The type of the event's property
 	 * @param event The event to fire
 	 * @param fromDescendant Whether the event was fired on one of this element's descendants or on this element specifically
 	 * @param propagateUp Whether the event should be fired on this element's ancestors as well
 	 */
 	public final <T> void fireEvent(MuisEvent<T> event, boolean fromDescendant, boolean propagateUp) {
 		MuisEventListener<T> [] listeners = theListeners.getListeners(event.getType());
 		for(MuisEventListener<T> listener : listeners)
 			if(!fromDescendant || !listener.isLocal())
 				listener.eventOccurred(event, this);
 		if(propagateUp && theParent != null)
 			theParent.fireEvent(event, true, true);
 	}
 
 	// End event methods
 
 	/**
 	 * Generates an XML-representation of this element's content
 	 *
 	 * @param indent The indention string to use for each level away from the margin
 	 * @param deep Whether to print this element's children
 	 * @return The XML string representing this element
 	 */
 	public final String asXML(String indent, boolean deep) {
 		StringBuilder ret = new StringBuilder();
 		appendXML(ret, indent, 0, deep);
 		return ret.toString();
 	}
 
 	/**
 	 * Appends this element's XML-representation to a string builder
 	 *
 	 * @param str The string builder to append to
 	 * @param indent The indention string to use for each level away from the margin
 	 * @param level The depth of this element in the structure being printed
 	 * @param deep Whether to print this element's children
 	 */
 	protected final void appendXML(StringBuilder str, String indent, int level, boolean deep) {
 		for(int L = 0; L < level; L++)
 			str.append(indent);
 		str.append('<');
 		if(theNamespace != null)
 			str.append(theNamespace).append(':');
 		str.append(theTagName);
 		if(theAttributeManager.holders().iterator().hasNext())
 			str.append(' ').append(theAttributeManager.toString());
 		if(!deep || theChildren.isEmpty()) {
 			str.append(' ').append('/').append('>');
 			return;
 		}
 		str.append('>');
 		if(deep) {
 			for(MuisElement child : theChildren) {
 				str.append('\n');
 				child.appendXML(str, indent, level + 1, deep);
 			}
 			str.append('\n');
 		}
 		for(int L = 0; L < level; L++)
 			str.append(indent);
 		str.append('<').append('/');
 		if(theNamespace != null)
 			str.append(theNamespace).append(':');
 		str.append(theTagName).append('>');
 	}
 
 	@Override
 	public String toString() {
 		return asXML("", false);
 	}
 
 	// Layout methods
 
 	/**
 	 * Causes this element to adjust the position and size of its children in a way defined in this element type's implementation. By
 	 * default this does nothing.
 	 */
 	public void doLayout() {
 		theLayoutDirtyTime = 0;
 		for(MuisElement child : getChildren())
 			child.doLayout();
 		repaint(null, false);
 	}
 
 	/**
 	 * Causes a call to {@link #doLayout()}
 	 *
 	 * @param now Whether to perform the layout action now or allow it to be performed asynchronously
 	 */
 	public final void relayout(boolean now) {
 		if(theBounds.getWidth() <= 0 || theBounds.getHeight() <= 0)
 			return; // No point layout out if there's nothing to show
 		theLayoutDirtyTime = System.currentTimeMillis();
 		MuisEventQueue.get().scheduleEvent(new MuisEventQueue.LayoutEvent(this, now), now);
 	}
 
 	/** @return The last time a layout event was scheduled for this element */
 	public final long getLayoutDirtyTime() {
 		return theLayoutDirtyTime;
 	}
 
 	// End layout methods
 
 	// Paint methods
 
 	/** @return The graphics object to use to draw this element */
 	public Graphics2D getGraphics() {
 		int x = 0, y = 0;
 		MuisElement el = this;
 		while(el.theParent != null) {
 			x += theBounds.getX();
 			y += theBounds.getY();
 			el = el.theParent;
 		}
 		java.awt.Graphics2D graphics = theDocument.getGraphics();
 		if(el != theDocument.getRoot()) {
 			graphics = (Graphics2D) graphics.create(x, y, 0, 0);
 			return graphics;
 		}
 		graphics = (Graphics2D) graphics.create(x, y, theBounds.getWidth(), theBounds.getHeight());
 		return graphics;
 	}
 
 	/**
 	 * @return The bounds within which this element may draw and receive events, relative to the layout x,y position. This may extend
 	 *         outside the element's layout bounds (e.g. for a menu, which expands, but does not cause a relayout when it does so).
 	 */
 	public Rectangle getPaintBounds() {
 		return new Rectangle(0, 0, theBounds.getWidth(), theBounds.getHeight());
 	}
 
 	/**
 	 * Renders this element in a graphics context.
 	 *
 	 * @param graphics The graphics context to render this element in
 	 * @param area The area to draw
 	 */
 	public final void paint(java.awt.Graphics2D graphics, Rectangle area) {
 		if((area != null && (area.width == 0 || area.height == 0)) || theBounds.getWidth() == 0 || theBounds.getHeight() == 0)
 			return;
 		Rectangle paintBounds = getPaintBounds();
 		theCacheBounds.setBounds(paintBounds);
 		theCacheBounds.x += theBounds.getX();
 		theCacheBounds.y += theBounds.getY();
 		Rectangle preClip = graphics.getClipBounds();
 		try {
 			graphics.setClip(paintBounds.x, paintBounds.y, paintBounds.width, paintBounds.height);
 			paintSelf(graphics, area);
 			paintChildren(graphics, area);
 		} finally {
 			graphics.setClip(preClip);
 		}
 	}
 
 	/**
 	 * Causes this element to be repainted.
 	 *
 	 * @param area The area in this element that needs to be repainted. May be null to specify that the entire element needs to be redrawn.
 	 * @param now Whether this element should be repainted immediately or not. This parameter should usually be false when this is called as
 	 *            a result of a user operation such as a mouse or keyboard event because this allows all necessary paint events to be
 	 *            performed at one time with no duplication after the event is finished. This parameter should be true if this is called
 	 *            from an independent thread.
 	 */
 	public final void repaint(Rectangle area, boolean now) {
 		if(theBounds.getWidth() <= 0 || theBounds.getHeight() <= 0)
 			return; // No point painting if there's nothing to show
 		thePaintDirtyTime = System.currentTimeMillis();
 		MuisEventQueue.get().scheduleEvent(new MuisEventQueue.PaintEvent(this, area, now), now);
 	}
 
 	/**
 	 * Renders this element's background or its content, but NOT its children. Children are rendered by
 	 * {@link #paintChildren(java.awt.Graphics2D, Rectangle)}. By default, this merely draws the element's background color.
 	 *
 	 * @param graphics The graphics context to draw in
 	 * @param area The area to paint
 	 */
 	public void paintSelf(java.awt.Graphics2D graphics, Rectangle area) {
 		Texture tex = getStyle().getSelf().get(BackgroundStyles.texture);
 		if(tex != null)
 			tex.render(graphics, this, area);
 	}
 
 	/**
 	 * Draws this element's children
 	 *
 	 * @param graphics The graphics context to render in
 	 * @param area The area in this element's coordinates to repaint
 	 */
 	public final void paintChildren(java.awt.Graphics2D graphics, Rectangle area) {
 		MuisElement [] children = ch().sortByZ();
 		if(children.length == 0)
 			return;
 		if(area == null)
 			area = theBounds.getBounds();
 		int translateX = 0;
 		int translateY = 0;
 		try {
 			Rectangle childArea = new Rectangle();
 			for(MuisElement child : children) {
 				int childX = child.theBounds.getX();
 				int childY = child.theBounds.getY();
 				translateX += childX;
 				translateY += childY;
 				childArea.x = area.x - translateX;
 				childArea.y = area.y - translateY;
 				if(childArea.x < 0)
 					childArea.x = 0;
 				if(childArea.y < 0)
 					childArea.y = 0;
 				childArea.width = area.width - childArea.x;
 				if(childArea.x + childArea.width > child.theBounds.getWidth())
 					childArea.width = child.theBounds.getWidth() - childArea.x;
 				childArea.height = area.height - childArea.y;
 				if(childArea.y + childArea.height > child.theBounds.getHeight())
 					childArea.height = child.theBounds.getHeight() - childArea.y;
 				graphics.translate(translateX, translateY);
				child.paint(graphics, childArea);
 				translateX = -childX;
 				translateY = -childY;
 			}
 		} finally {
 			if(translateX != 0 || translateY != 0)
				graphics.translate(-translateX, -translateY);
 		}
 	}
 
 	/** @return The last time a paint event was scheduled for this element */
 	public final long getPaintDirtyTime() {
 		return thePaintDirtyTime;
 	}
 
 	/** @return This element's bounds as of the last time it was painted */
 	public final Rectangle getCacheBounds() {
 		return theCacheBounds;
 	}
 
 	// End paint methods
 
 	@Override
 	public final boolean equals(Object o) {
 		return super.equals(o);
 	}
 
 	@Override
 	public final int hashCode() {
 		return super.hashCode();
 	}
 
 	private static class CoreStateControllers {
 		StateEngine.StateController clicked;
 
 		StateEngine.StateController rightClicked;
 
 		StateEngine.StateController middleClicked;
 
 		StateEngine.StateController hovered;
 
 		StateEngine.StateController focused;
 	}
 }
