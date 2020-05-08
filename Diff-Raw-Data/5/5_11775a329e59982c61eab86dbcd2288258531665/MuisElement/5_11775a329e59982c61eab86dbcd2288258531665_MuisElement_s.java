 /*
  * Created Feb 23, 2009 by Andrew Butler
  */
 package org.muis.core;
 
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.muis.core.MuisSecurityPermission.PermissionType;
 import org.muis.core.event.MuisEvent;
 import org.muis.core.event.MuisEventListener;
 import org.muis.core.event.MuisEventType;
 import org.muis.core.event.MuisPropertyEvent;
 import org.muis.core.layout.SimpleSizePolicy;
 import org.muis.core.layout.SizePolicy;
 import org.muis.core.style.*;
 
 import prisms.arch.event.ListenerManager;
 import prisms.util.ArrayUtils;
 
 /** The base display element in MUIS. Contains base methods to administer content (children, style, placement, etc.) */
 public abstract class MuisElement implements org.muis.core.layout.Sizeable, MuisMessage.MuisMessageCenter
 {
 	/** Manages the life cycle of an element */
 	public class MuisLifeCycleManager
 	{
 		private volatile String [] theStages;
 
 		private int theCurrentStage;
 
 		private volatile LifeCycleListener [] theLifeCycleListeners;
 
 		private final Object theListenerLock;
 
 		MuisLifeCycleManager(String... stages)
 		{
 			theStages = stages;
 			theLifeCycleListeners = new LifeCycleListener[0];
 			theListenerLock = new Object();
 		}
 
 		/** @return The current stage in the element's life cycle */
 		public String getStage()
 		{
 			return theStages[theCurrentStage];
 		}
 
 		/** @return All stages in this life cycle, past, present, and future */
 		public String [] getStages()
 		{
 			return theStages.clone();
 		}
 
 		/**
 		 * @param stage The stage to check
 		 * @return <0 if the current stage is before the given stage, 0, if the current stage is the given stage, or >0 if the current stage
 		 *         is after the given stage
 		 */
 		public int isAfter(String stage)
 		{
 			int index = ArrayUtils.indexOf(theStages, stage);
 			if(index < 0)
 				throw new IllegalArgumentException("Unrecognized life cycle stage \"" + stage + "\"");
 			return theCurrentStage - index;
 		}
 
 		/** @param listener The listener to be notified when the life cycle stage changes */
 		public void addListener(LifeCycleListener listener)
 		{
 			synchronized(theListenerLock)
 			{
 				int idx = ArrayUtils.indexOf(theLifeCycleListeners, listener);
 				if(idx < 0)
 					theLifeCycleListeners = ArrayUtils.add(theLifeCycleListeners, listener);
 			}
 		}
 
 		/** @param listener The listener to remove from notification */
 		public void removeListener(LifeCycleListener listener)
 		{
 			synchronized(theListenerLock)
 			{
 				int idx = ArrayUtils.indexOf(theLifeCycleListeners, listener);
 				if(idx >= 0)
 					theLifeCycleListeners = ArrayUtils.remove(theLifeCycleListeners, idx);
 			}
 		}
 
 		/**
 		 * @param stage The stage to add to this life cycle
 		 * @param afterStage The stage (already registered in this life cycle manager) to add the new stage after
 		 */
 		public void addStage(String stage, String afterStage)
 		{
 			if(theCurrentStage > 0)
 				error("Life cycle stages may not be added after the " + theStages[0] + " stage", null, "stage", stage);
 			if(afterStage == null && theStages.length > 0)
 			{
 				error("afterStage must not be null--stages cannot be inserted before " + theStages[0], null, "stage", stage);
 				return;
 			}
 			int idx = prisms.util.ArrayUtils.indexOf(theStages, afterStage);
 			if(idx < 0)
 			{
 				error("afterStage \"" + afterStage + "\" not found. Cannot add stage.", null, "stage", stage);
 				return;
 			}
 			theStages = prisms.util.ArrayUtils.add(theStages, stage, idx + 1);
 		}
 
 		/** Advances the life cycle stage of the element to the given stage. Called from MuisElement. */
 		private void advanceLifeCycle(String toStage)
 		{
 			String [] stages = theStages;
 			LifeCycleListener [] listeners = theLifeCycleListeners;
 			int goal = ArrayUtils.indexOf(stages, toStage);
 			if(goal <= theCurrentStage)
 			{
 				error("Stage " + toStage + " has already been transitioned", null, "stage", toStage);
 				return;
 			}
 			while(theCurrentStage < stages.length - 1 && !stages[theCurrentStage].equals(toStage))
 			{
 				// Transition one stage forward
 				String oldStage = stages[theCurrentStage];
 				String newStage = stages[theCurrentStage + 1];
 				/*
 				 * Call listeners for the pre-transition in reverse order so that the first listener added gets notified just before the
 				 * transition actually occurs so nobody has a chance to override its actions
 				 */
 				for(int L = listeners.length - 1; L >= 0; L--)
 					listeners[L].preTransition(oldStage, newStage);
 				theCurrentStage++;
 				for(LifeCycleListener listener : listeners)
 					listener.postTransition(oldStage, newStage);
 			}
 		}
 	}
 
 	/** A listener to be notified when the life cycle of an element transitions to a new stage */
 	public interface LifeCycleListener
 	{
 		/**
 		 * @param fromStage The stage that is being concluded and transitioned out of
 		 * @param toStage The stage to be transitioned into
 		 */
 		void preTransition(String fromStage, String toStage);
 
 		/**
 		 * @param oldStage The stage that is concluded and transitioned out of
 		 * @param newStage The stage that has just begun
 		 */
 		void postTransition(String oldStage, String newStage);
 	}
 
 	/** The stages of MUIS element creation recognized by the MUIS core, except for {@link #OTHER} */
 	public static enum CoreStage
 	{
 		/**
 		 * The element is being constructed without any knowledge of its document or other context. During this stage, internal variables
 		 * should be created and initialized with default values. Initial listeners may be added to the element at this time as well.
 		 */
 		CREATION,
 		/** The element is being populated with the attributes from its XML. This is a transition time where no work is done by the core. */
 		PARSE_SELF,
 		/**
 		 * The {@link MuisElement#init(MuisDocument, MuisToolkit, MuisClassView, MuisElement, String, String)} method is initializing this
 		 * element's context.
 		 */
 		INIT_SELF,
 		/** The children of this element as configured in XML are being parsed. This is a transition time where no work is done by the core. */
 		PARSE_CHILDREN,
 		/** The {@link MuisElement#initChildren(MuisElement[])} method is populating this element with its contents */
 		INIT_CHILDREN,
 		/**
 		 * This element is fully initialized, but the rest of the document may be loading. This is a transition time where no work is done
 		 * by the core.
 		 */
 		INITIALIZED,
 		/**
 		 * The {@link MuisElement#postCreate()} method is performing context-sensitive initialization work. The core performs attribute
 		 * checks during this time. Before this stage, attributes may be added which are not recognized by the element. During this stage,
 		 * all unchecked attributes are checked and errors are logged for any attributes that are unrecognized or malformatted as well as
 		 * for any required attributes whose values have not been set. During and after this stage, no attributes may be set in the element
 		 * unless they have been {@link MuisElement#acceptAttribute(MuisAttribute) accepted} and the type is correct. An element's children
 		 * are started up at the tail end of this stage, so note that when an element transitions out of this stage, its contents will be in
 		 * the {@link #READY} stage, but its parent will still be in the {@link #STARTUP} stage, though all its attribute work has
 		 * completed.
 		 */
 		STARTUP,
 		/** The element has been fully initialized within the full document context and is ready to render and receive events */
 		READY,
 		/** Represents any stage that the core does not know about */
 		OTHER;
 
 		/**
 		 * @param name The name of the stage to get the enum value for
 		 * @return The enum value of the named stage, unless the stage is not recognized by the MUIS core, in which case {@link #OTHER} is
 		 *         returned
 		 */
 		public static CoreStage get(String name)
 		{
 			for(CoreStage stage : values())
 				if(stage != OTHER && stage.toString().equals(name))
 					return stage;
 			return OTHER;
 		}
 	}
 
 	// TODO Add code for attach events
 
 	private class AttributeHolder
 	{
 		private final MuisAttribute<?> attr;
 
 		private boolean required;
 
 		AttributeHolder(MuisAttribute<?> anAttr, boolean req)
 		{
 			attr = anAttr;
 			required = req;
 		}
 
 		/**
 		 * Validates an attribute value
 		 *
 		 * @param attr The attribute to validate
 		 * @param value The non-null attribute value to validate
 		 * @param el The element that the attribute is for
 		 * @param required Whether the attribute is required or not
 		 * @return Null if the attribute value is valid for this attribute; an error string describing why the value is not valid otherwise
 		 */
 		private String validate(String value)
 		{
 			String val = attr.type.validate(MuisElement.this, value);
 			if(val == null)
 				return null;
 			else
 				return (required ? "Required attribute " : "Attribute ") + attr.name + " " + val;
 		}
 
 		@Override
 		public String toString()
 		{
 			return attr.toString() + (required ? " (required)" : " (optional)");
 		}
 	}
 
 	/** The event type representing a mouse event */
 	public static final MuisEventType<Void> MOUSE_EVENT = new MuisEventType<Void>("Mouse Event", null);
 
 	/** The event type representing a scrolling action */
 	public static final MuisEventType<Void> SCROLL_EVENT = new MuisEventType<Void>("Scroll Event", null);
 
 	/** The event type representing a physical keyboard event */
 	public static final MuisEventType<Void> KEYBOARD_EVENT = new MuisEventType<Void>("Keyboard Event", null);
 
 	/**
 	 * The event type representing the input of a character. This is distinct from a keyboard event in several situations. For example:
 	 * <ul>
 	 * <li>The user presses and holds a character key, resulting in a character input, a pause, and then many sequential character inputs
 	 * until the user releases the key.</li>
 	 * <li>The user copies text and pastes it into a text box. This may result in one or two keyboard events or even mouse events
 	 * (menu->Paste) followed by a character event with the pasted text.</li>
 	 * </ul>
 	 */
 	public static final MuisEventType<Void> CHARACTER_INPUT = new MuisEventType<Void>("Character Input", null);
 
 	/** The event type representing focus change on an element */
 	public static final MuisEventType<Void> FOCUS_EVENT = new MuisEventType<Void>("Focus Event", null);
 
 	/** The event type representing the relocation of this element within its parent */
 	public static final MuisEventType<Rectangle> BOUNDS_CHANGED = new MuisEventType<Rectangle>("Bounds Changed", null);
 
 	/** The event type representing the successful setting of an attribute */
 	public static final MuisEventType<MuisAttribute<?>> ATTRIBUTE_SET = new MuisEventType<MuisAttribute<?>>("Attribute Set",
 		(Class<MuisAttribute<?>>) (Class<?>) MuisAttribute.class);
 
 	/** The event type representing the change of an element's stage property */
 	public static final MuisEventType<CoreStage> STAGE_CHANGED = new MuisEventType<CoreStage>("Stage Changed", CoreStage.class);
 
 	/**
 	 * The event type representing the event when an element is moved from one parent element to another. The event property is the new
 	 * parent element. This method is NOT called from {@link #init(MuisDocument, MuisToolkit, MuisClassView, MuisElement, String, String)}
 	 */
 	public static final MuisEventType<MuisElement> ELEMENT_MOVED = new MuisEventType<MuisElement>("Element Moved", MuisElement.class);
 
 	/**
 	 * The event type representing the event when a child is added to an element. The event property is the child that was added. This
 	 * method is NOT called from {@link #initChildren(MuisElement[])}.
 	 */
 	public static final MuisEventType<MuisElement> CHILD_ADDED = new MuisEventType<MuisElement>("Child Added", MuisElement.class);
 
 	/**
 	 * The event type representing the event when a child is removed from an element. The event property is the child that was removed. This
 	 * method is NOT called from {@link #initChildren(MuisElement[])}.
 	 */
 	public static final MuisEventType<MuisElement> CHILD_REMOVED = new MuisEventType<MuisElement>("Child Removed", MuisElement.class);
 
 	/** The event type representing the addition of a message to an element. */
 	public static final MuisEventType<MuisMessage> MESSAGE_ADDED = new MuisEventType<MuisMessage>("Message Added", MuisMessage.class);
 
 	/** The event type representing the removal of a message from an element. */
 	public static final MuisEventType<MuisMessage> MESSAGE_REMOVED = new MuisEventType<MuisMessage>("Message Removed", MuisMessage.class);
 
 	/**
 	 * Used to lock this elements' child sets
 	 *
 	 * @see MuisLock
 	 */
 	public static final String CHILDREN_LOCK_TYPE = "Muis Child Lock";
 
 	private final MuisLifeCycleManager theLifeCycleManager;
 
 	private MuisDocument theDocument;
 
 	private MuisToolkit theToolkit;
 
 	private MuisElement theParent;
 
 	private MuisClassView theClassView;
 
 	private String theNamespace;
 
 	private String theTagName;
 
 	private ConcurrentHashMap<String, AttributeHolder> theAcceptedAttrs;
 
 	private ConcurrentHashMap<MuisAttribute<?>, Object> theAttrValues;
 
 	private ConcurrentHashMap<String, String> theRawAttributes;
 
 	private MuisElement [] theChildren;
 
 	private final ElementStyle theStyle;
 
 	private int theX;
 
 	private int theY;
 
 	private int theZ;
 
 	private int theW;
 
 	private int theH;
 
 	private int theCacheX;
 
 	private int theCacheY;
 
 	private int theCacheW;
 
 	private int theCacheH;
 
 	private SizePolicy theHSizer;
 
 	private SizePolicy theVSizer;
 
 	@SuppressWarnings({"rawtypes"})
 	private ListenerManager<MuisEventListener> theListeners;
 
 	@SuppressWarnings("rawtypes")
 	private ListenerManager<MuisEventListener> theChildListeners;
 
 	private java.util.ArrayList<MuisMessage> theMessages;
 
 	private MuisMessage.Type theWorstMessageType;
 
 	private MuisMessage.Type theWorstChildMessageType;
 
 	private boolean isFocusable;
 
 	private long thePaintDirtyTime;
 
 	private long theLayoutDirtyTime;
 
 	/** Creates a MUIS element */
 	public MuisElement()
 	{
 		theLifeCycleManager = new MuisLifeCycleManager();
 		String lastStage = null;
 		for(CoreStage stage : CoreStage.values())
 			if(stage != CoreStage.OTHER)
 			{
 				theLifeCycleManager.addStage(stage.toString(), lastStage);
 				lastStage = stage.toString();
 			}
 		theChildren = new MuisElement[0];
 		theAcceptedAttrs = new ConcurrentHashMap<>();
 		theAttrValues = new ConcurrentHashMap<>();
 		theRawAttributes = new ConcurrentHashMap<>();
 		theListeners = new ListenerManager<>(MuisEventListener.class);
 		theChildListeners = new ListenerManager<>(MuisEventListener.class);
 		theMessages = new java.util.ArrayList<>();
 		if(this instanceof MuisTextElement)
 			theStyle = new TextStyle((MuisTextElement) this);
 		else
 			theStyle = new ElementStyle(this);
 		org.muis.core.style.StyleListener sl = new org.muis.core.style.StyleListener(this) {
 			@Override
 			public void styleChanged(MuisStyle style)
 			{
 				repaint(null, false);
 			}
 		};
 		sl.addDomain(org.muis.core.style.BackgroundStyles.getDomainInstance());
 		sl.add();
 		MuisEventListener<MuisElement> childListener = new MuisEventListener<MuisElement>() {
 			@Override
 			public void eventOccurred(MuisEvent<MuisElement> event, MuisElement element)
 			{
 				relayout(false);
 				if(event.getType() == CHILD_REMOVED)
 					// Need to repaint where the element left even if nothing changes as a result of the layout
 					repaint(new Rectangle(element.getX(), element.getY(), element.getWidth(), element.getHeight()), false);
 			}
 
 			@Override
 			public boolean isLocal()
 			{
 				return true;
 			}
 		};
 		addListener(CHILD_ADDED, childListener);
 		addListener(CHILD_REMOVED, childListener);
 		addChildListener(BOUNDS_CHANGED, new MuisEventListener<Rectangle>() {
 			@Override
 			public void eventOccurred(MuisEvent<Rectangle> event, MuisElement element)
 			{
 				Rectangle paintRect = event.getValue().union(((MuisPropertyEvent<Rectangle>) event).getOldValue());
 				repaint(paintRect, false);
 			}
 
 			@Override
 			public boolean isLocal()
 			{
 				return true;
 			}
 		});
 		acceptAttribute(StyleAttributeType.ATTRIBUTE);
 		addListener(BOUNDS_CHANGED, new MuisEventListener<Rectangle>() {
 			@Override
 			public void eventOccurred(MuisEvent<Rectangle> event, MuisElement element)
 			{
 				Rectangle old = ((MuisPropertyEvent<Rectangle>) event).getOldValue();
 				if(event.getValue().width != old.width || event.getValue().height != old.height)
 					relayout(false);
 			}
 
 			@Override
 			public boolean isLocal()
 			{
 				return true;
 			}
 		});
 		theLifeCycleManager.addListener(new LifeCycleListener() {
 
 			@Override
 			public void preTransition(String fromStage, String toStage)
 			{
 			}
 
 			@Override
 			public void postTransition(String oldStage, String newStage)
 			{
 				if(oldStage.equals(CoreStage.INIT_SELF.toString()))
 					repaint(null, false);
 			}
 		});
 		theLifeCycleManager.advanceLifeCycle(CoreStage.PARSE_SELF.toString());
 	}
 
 	// Life cycle methods
 
 	/**
 	 * Returns a life cycle manager that allows subclasses to customize and hook into the life cycle for this element.
 	 *
 	 * @return The life cycle manager for this element
 	 */
 	public MuisLifeCycleManager getLifeCycleManager()
 	{
 		return theLifeCycleManager;
 	}
 
 	/**
 	 * Short-hand for {@link #getLifeCycleManager()}
 	 *
 	 * @return The life cycle manager for this element
 	 */
 	public MuisLifeCycleManager life()
 	{
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
 		String tagName)
 	{
 		theLifeCycleManager.advanceLifeCycle(CoreStage.INIT_SELF.toString());
 		if(doc == null)
 			throw new IllegalArgumentException("Cannot create an element without a document");
 		if(theDocument != null)
 			throw new IllegalStateException("An element cannot be initialized twice", null);
 		theDocument = doc;
 		theToolkit = toolkit;
 		theParent = parent;
 		theNamespace = namespace;
 		theTagName = tagName;
 		theClassView = classView;
 		theChildren = new MuisElement[0];
 		theLifeCycleManager.advanceLifeCycle(CoreStage.PARSE_CHILDREN.toString());
 	}
 
 	/**
 	 * Initializes an element's descendants
 	 *
 	 * @param children The child elements specified in the MUIS XML
 	 */
 	public void initChildren(MuisElement [] children)
 	{
 		theLifeCycleManager.advanceLifeCycle(CoreStage.INIT_CHILDREN.toString());
 		try (MuisLock lock = theDocument.getLocker().lock(CHILDREN_LOCK_TYPE, this, true))
 		{
 			for(MuisElement child : theChildren)
 				if(child.getParent() == this)
 					child.setParent(null);
 			theChildren = children;
 		}
 		for(MuisElement child : children)
 			registerChild(child);
 		if(theW != 0 && theH != 0) // No point laying out if there's nothing to show
 			relayout(false);
 		theLifeCycleManager.advanceLifeCycle(CoreStage.INITIALIZED.toString());
 	}
 
 	/**
 	 * Called when a child is introduced to this parent
 	 *
 	 * @param child The child that has been added to this parent
 	 */
 	protected void registerChild(MuisElement child)
 	{
 		for(Object type : theChildListeners.getAllProperties())
 			for(MuisEventListener<Object> listener : theChildListeners.getRegisteredListeners(type))
 				child.addListener((MuisEventType<Object>) type, listener);
 	}
 
 	/**
 	 * Called when a child is removed to this parent
 	 *
 	 * @param child The child that has been removed from this parent
 	 */
 	protected void unregisterChild(MuisElement child)
 	{
 		for(Object type : theChildListeners.getAllProperties())
 			for(MuisEventListener<Object> listener : theChildListeners.getRegisteredListeners(type))
 				child.removeListener(listener);
 	}
 
 	/** Called to initialize an element after all the parsing and linking has been performed */
 	public void postCreate()
 	{
 		theLifeCycleManager.advanceLifeCycle(CoreStage.STARTUP.toString());
 		for(AttributeHolder holder : theAcceptedAttrs.values())
 		{
 			MuisAttribute<?> attr = holder.attr;
 			boolean required = holder.required;
 			Object value = theAttrValues.get(attr);
 			if(value == null && required)
 				fatal("Required attribute " + attr + " not set", null);
 			if(value instanceof String)
 			{
 				String valError = holder.validate((String) value);
 				if(valError != null)
 					fatal(valError, null);
 				try
 				{
 					value = attr.type.parse(this, (String) value);
 					theAttrValues.put(attr, value);
 				} catch(MuisException e)
 				{
 					if(required)
 						fatal("Required attribute " + attr + " could not be parsed", e, "attribute", attr, "value", value);
 					else
 						error("Attribute " + attr + " could not be parsed", e, "attribute", attr, "value", value);
 					theAttrValues.remove(attr);
 				}
 			}
 			fireEvent(new MuisEvent<MuisAttribute<?>>(ATTRIBUTE_SET, attr), false, false);
 		}
 		for(java.util.Map.Entry<String, String> attr : theRawAttributes.entrySet())
 			error("Attribute " + attr.getKey() + " is not accepted in this element", null, "value", attr.getValue());
 		for(MuisElement child : theChildren)
 			child.postCreate();
 		theRawAttributes = null;
 		theLifeCycleManager.advanceLifeCycle(CoreStage.READY.toString());
 	}
 
 	// End life cycle methods
 
 	/**
 	 * Checks whether the given permission can be executed in the current context
 	 *
 	 * @param type The type of permission
 	 * @param value The value associated with the permission
 	 * @throws SecurityException If the permission is denied
 	 */
 	public final void checkSecurity(PermissionType type, Object value) throws SecurityException
 	{
 		SecurityManager mgr = System.getSecurityManager();
 		if(mgr != null)
 			mgr.checkPermission(new MuisSecurityPermission(type, null, this, value));
 	}
 
 	// Attribute methods
 
 	/**
 	 * Sets an attribute typelessly
 	 *
 	 * @param attr The name of the attribute to set
 	 * @param value The string representation of the attribute's value
 	 * @return The parsed value for the attribute, or null if this element has not been initialized
 	 * @throws MuisException If the attribute is not accepted in this element, the value is null and the attribute is required, or this
 	 *             element has already been initialized and the value is not valid for the given attribute
 	 */
 	public final Object setAttribute(String attr, String value) throws MuisException
 	{
 		AttributeHolder holder = theAcceptedAttrs.get(attr);
 		if(holder == null)
 		{
 			if(theLifeCycleManager.isAfter(CoreStage.STARTUP.toString()) >= 0)
 				throw new MuisException("Attribute " + attr + " is not accepted in this element");
 			theRawAttributes.put(attr, value);
 			return null;
 		}
 		return setAttribute(holder.attr, value);
 	}
 
 	/**
 	 * Sets the value of an attribute for the element. If this element has not been fully initialized (by {@link #postCreate()}, the
 	 * attribute's value will be validated and parsed during {@link #postCreate()}. If this element has been initialized, the value will be
 	 * validated immediately and a {@link MuisException} will be thrown if the value is not valid.
 	 *
 	 * @param <T> The type of the attribute to set
 	 * @param attr The attribute to set
 	 * @param value The value for the attribute
 	 * @return The parsed value for the attribute, or null if this element has not been initialized
 	 * @throws MuisException If the attribute is not accepted in this element, the value is null and the attribute is required, or this
 	 *             element has already been initialized and the value is not valid for the given attribute
 	 */
 	public final <T> T setAttribute(MuisAttribute<T> attr, String value) throws MuisException
 	{
 		checkSecurity(PermissionType.setAttribute, attr);
 		if(theRawAttributes != null)
 			theRawAttributes.remove(attr.name);
 		if(theLifeCycleManager.isAfter(CoreStage.STARTUP.toString()) >= 0)
 		{
 			AttributeHolder holder = theAcceptedAttrs.get(attr.name);
 			if(holder == null)
 				throw new MuisException("Attribute " + attr + " is not accepted in this element");
 			if(value == null && holder.required)
 				throw new MuisException("Attribute " + attr + " is required--cannot be set to null");
 			String valError = holder.validate(value);
 			if(valError != null)
 				throw new MuisException(valError);
 			T ret = attr.type.parse(this, value);
 			theAttrValues.put(attr, ret);
 			fireEvent(new MuisEvent<MuisAttribute<?>>(ATTRIBUTE_SET, attr), false, false);
 			return ret;
 		}
 		theAttrValues.put(attr, value);
 		return null;
 	}
 
 	/**
 	 * Sets an attribute's type-correct value
 	 *
 	 * @param <T> The type of the attribute to set
 	 * @param attr The attribute to set
 	 * @param value The value to set for the attribute in this element
 	 * @throws MuisException If the attribute is not accepted in this element or the value is not valid
 	 */
 	public <T> void setAttribute(MuisAttribute<T> attr, T value) throws MuisException
 	{
 		checkSecurity(PermissionType.setAttribute, attr);
 		if(theRawAttributes != null)
 			theRawAttributes.remove(attr.name);
 		AttributeHolder holder = theAcceptedAttrs.get(attr.name);
 		if(holder == null)
 			throw new MuisException("Attribute " + attr + " is not accepted in this element");
 		if(value == null && holder.required)
 			throw new MuisException("Attribute " + attr + " is required--cannot be set to null");
 		if(value != null)
 		{
 			T newValue = attr.type.cast(value);
 			if(newValue == null)
 				throw new MuisException("Value " + value + ", type " + value.getClass().getName() + " is not valid for atribute " + attr);
 		}
 		theAttrValues.put(attr, value);
 		fireEvent(new MuisEvent<MuisAttribute<?>>(ATTRIBUTE_SET, attr), false, false);
 	}
 
 	/**
 	 * @param name The name of the attribute to get
 	 * @return The value of the named attribute
 	 */
 	public final Object getAttribute(String name)
 	{
 		AttributeHolder holder = theAcceptedAttrs.get(name);
 		if(holder == null)
 			return null;
 		return theAttrValues.get(holder.attr);
 	}
 
 	/**
 	 * Gets the value of an attribute in this element
 	 *
 	 * @param <T> The type of the attribute to get
 	 * @param attr The attribute to get the value of
 	 * @return The value of the attribute in this element
 	 */
 	public <T> T getAttribute(MuisAttribute<T> attr)
 	{
 		AttributeHolder storedAttr = theAcceptedAttrs.get(attr.name);
 		if(storedAttr != null && !storedAttr.attr.equals(attr))
 			return null; // Same name, but different attribute
 		Object stored = theAttrValues.get(attr);
 		if(stored == null)
 			return null;
 		if(theLifeCycleManager.isAfter(CoreStage.STARTUP.toString()) < 0 && stored instanceof String)
 			try
 			{
 				T ret = attr.type.parse(this, (String) stored);
 				theAttrValues.put(attr, ret);
 				return ret;
 			} catch(MuisException e)
 			{
 				if(storedAttr != null && storedAttr.required)
 					fatal("Required attribute " + attr + " could not be parsed from " + stored, e, "attribute", attr, "value", stored);
 				else
 					error("Attribute " + attr + " could not be parsed from " + stored, e, "attribute", attr, "value", stored);
 				return null;
 			}
 		else
 			return (T) stored;
 	}
 
 	/**
 	 * Specifies a required attribute for this element
 	 *
 	 * @param attr The attribute that must be specified for this element
 	 */
 	public final void requireAttribute(MuisAttribute<?> attr)
 	{
 		if(theLifeCycleManager.isAfter(CoreStage.STARTUP.toString()) > 0)
 			throw new IllegalStateException("Attributes cannot be specified after an element is initialized");
 		AttributeHolder holder = theAcceptedAttrs.get(attr.name);
 		if(holder != null)
 		{
 			if(holder.attr.equals(attr))
 			{
 				holder.required = true;
 				return; // The attribute is already required
 			}
 			else
 				throw new IllegalStateException("An attribute named " + attr.name + " (" + holder.attr
 					+ ") is already accepted in this element");
 		}
 		else
 		{
 			holder = new AttributeHolder(attr, true);
 			theAcceptedAttrs.put(attr.name, holder);
 			String strVal = theRawAttributes.remove(attr.name);
 			if(strVal != null)
 			{
 				String valError = holder.validate(strVal);
 				if(valError != null)
 					error(valError, null, "attribute", attr);
 				else
 					try
 					{
 						setAttribute((MuisAttribute<Object>) attr, attr.type.parse(this, strVal));
 					} catch(MuisException e)
 					{
 						error("Could not parse pre-set value \"" + strVal + "\" of attribute " + attr.name, e, "attribute", attr);
 					}
 			}
 		}
 	}
 
 	/**
 	 * Marks an accepted attribute as not required
 	 *
 	 * @param attr The attribute to accept but not require
 	 */
 	public final void unrequireAttribute(MuisAttribute<?> attr)
 	{
 		if(theLifeCycleManager.isAfter(CoreStage.STARTUP.toString()) > 0)
 			throw new IllegalStateException("Attributes cannot be specified after an element is initialized");
 		AttributeHolder holder = theAcceptedAttrs.get(attr.name);
 		if(holder != null)
 		{
 			if(holder.attr.equals(attr))
 			{
 				holder.required = false;
 				return; // The attribute is already accepted
 			}
 			else
 				throw new IllegalStateException("An attribute named " + attr.name + " (" + holder.attr
 					+ ") is already accepted in this element");
 		}
 		else
 			theAcceptedAttrs.put(attr.name, new AttributeHolder(attr, false));
 	}
 
 	/**
 	 * Specifies an optional attribute for this element
 	 *
 	 * @param attr The attribute that must be specified for this element
 	 */
 	public final void acceptAttribute(MuisAttribute<?> attr)
 	{
 		if(theLifeCycleManager.isAfter(CoreStage.STARTUP.toString()) > 0)
 			throw new IllegalStateException("Attributes cannot be specified after an element is initialized");
 		AttributeHolder holder = theAcceptedAttrs.get(attr.name);
 		if(holder != null)
 		{
 			if(holder.attr.equals(attr))
 				return; // The attribute is already accepted
 			else
 				throw new IllegalStateException("An attribute named " + attr.name + " (" + holder.attr
 					+ ") is already accepted in this element");
 		}
 		else
 		{
 			holder = new AttributeHolder(attr, false);
 			theAcceptedAttrs.put(attr.name, holder);
 			String strVal = theRawAttributes.remove(attr.name);
 			if(strVal != null)
 			{
 				String valError = holder.validate(strVal);
 				if(valError != null)
 					error(valError, null, "attribute", attr);
 				else
 					try
 					{
 						setAttribute((MuisAttribute<Object>) attr, attr.type.parse(this, strVal));
 					} catch(MuisException e)
 					{
 						error("Could not parse pre-set value \"" + strVal + "\" of attribute " + attr.name, e, "attribute", attr);
 					}
 			}
 		}
 	}
 
 	/**
 	 * Undoes acceptance of an attribute. This method does not remove any attribute value associated with this element. It merely disables
 	 * the attribute. If the attribute is accepted on this element later, this element's value of that attribute will be preserved.
 	 *
 	 * @param attr The attribute to not allow in this element
 	 */
 	public final void rejectAttribute(MuisAttribute<?> attr)
 	{
 		if(theLifeCycleManager.isAfter(CoreStage.STARTUP.toString()) > 0)
 			return;
 		AttributeHolder holder = theAcceptedAttrs.get(attr.name);
 		if(holder != null)
 			if(holder.attr.equals(attr))
 				// We do not remove the values--we just disable them
 				theAcceptedAttrs.remove(attr.name);
 			else
 				return;
 	}
 
 	/**
 	 * @return The number of attributes set for this element
 	 */
 	public final int getAttributeCount()
 	{
 		return theAcceptedAttrs.size();
 	}
 
 	/**
 	 * @return All attributes that are accepted in this element
 	 */
 	public final MuisAttribute<?> [] getAttributes()
 	{
 		MuisAttribute<?> [] ret = new MuisAttribute[theAcceptedAttrs.size()];
 		int i = 0;
 		for(AttributeHolder holder : theAcceptedAttrs.values())
 			ret[i++] = holder.attr;
 		return ret;
 	}
 
 	/**
 	 * @param attr The attribute to check
 	 * @return Whether the given attribute can be set in this element
 	 */
 	public final boolean isAccepted(MuisAttribute<?> attr)
 	{
 		AttributeHolder holder = theAcceptedAttrs.get(attr.name);
 		return holder != null && holder.attr.equals(attr);
 	}
 
 	/**
 	 * @param attr The attribute to check
 	 * @return Whether the given attribute is required in this element
 	 */
 	public final boolean isRequired(MuisAttribute<?> attr)
 	{
 		AttributeHolder holder = theAcceptedAttrs.get(attr.name);
 		if(holder == null || !holder.attr.equals(attr))
 			return false;
 		return holder.required;
 	}
 
 	// End attribute methods
 
 	// Hierarchy methods
 
 	/** @return The document that this element belongs to */
 	public final MuisDocument getDocument()
 	{
 		return theDocument;
 	}
 
 	/** @return This element's parent in the DOM tree */
 	public final MuisElement getParent()
 	{
 		return theParent;
 	}
 
 	/**
 	 * Sets this element's parent after initialization
 	 *
 	 * @param parent The new parent for this element
 	 */
 	protected final void setParent(MuisElement parent)
 	{
 		checkSecurity(PermissionType.setParent, parent);
 		if(theParent != null)
 		{
 			int parentIndex = ArrayUtils.indexOf(theParent.theChildren, this);
 			if(parentIndex >= 0)
 				theParent.removeChild(parentIndex);
 		}
 		theParent = parent;
 		reEvalChildWorstMessage();
 		fireEvent(new MuisEvent<MuisElement>(ELEMENT_MOVED, theParent), false, false);
 	}
 
 	/** @return The number of children that this element has */
 	public final int getChildCount()
 	{
 		return theChildren.length;
 	}
 
 	/**
 	 * @param index The index of the child to get
 	 * @return This element's child at the given index
 	 */
 	public final MuisElement getChild(int index)
 	{
 		return theChildren[index];
 	}
 
 	/** @return This element's children */
 	public final MuisElement [] getChildren()
 	{
 		return theChildren.clone();
 	}
 
 	/**
 	 * @param child The child to get the index of
 	 * @return The index of the given child in this element's children, or -1 if the given element is not a child under this element
 	 */
 	public final int getChildIndex(MuisElement child)
 	{
 		return ArrayUtils.indexOf(theChildren, child);
 	}
 
 	/**
 	 * Adds a child to this element. Protected because this operation will not be desirable to all implementations (e.g. images). Override
 	 * as public in implementations where this functionality should be exposed publicly (containers).
 	 *
 	 * @param child The child to add
 	 * @param index The index to add the child at, or -1 to add the child as the last element
 	 */
 	protected void addChild(MuisElement child, int index)
 	{
 		checkSecurity(PermissionType.addChild, child);
 		if(index < 0)
 			index = theChildren.length;
 		try (MuisLock lock = theDocument.getLocker().lock(CHILDREN_LOCK_TYPE, this, true))
 		{
 			child.setParent(this);
 			theChildren = ArrayUtils.add(theChildren, child, index);
 		}
 		fireEvent(new MuisEvent<MuisElement>(CHILD_ADDED, child), false, false);
 	}
 
 	/**
 	 * Removes a child from this element. Protected because this operation will not be desirable to all implementations (e.g. images).
 	 * Override as public in implementations where this functionality should be exposed publicly (containers).
 	 *
 	 * @param index The index of the child to remove, or -1 to remove the last element
 	 * @return The element that was removed
 	 */
 	protected MuisElement removeChild(int index)
 	{
 		if(index < 0)
 			index = theChildren.length - 1;
 		MuisElement ret = theChildren[index];
 		checkSecurity(PermissionType.removeChild, ret);
 		try (MuisLock lock = theDocument.getLocker().lock(CHILDREN_LOCK_TYPE, this, true))
 		{
 			ret.setParent(null);
 			theChildren = ArrayUtils.remove(theChildren, index);
 		}
 		fireEvent(new MuisEvent<MuisElement>(CHILD_REMOVED, ret), false, false);
 		return ret;
 	}
 
 	/**
 	 * Checks to see if this element is in the subtree rooted at the given element
 	 *
 	 * @param ancestor The element whose subtree to check
 	 * @return Whether this element is in the ancestor's subtree
 	 */
 	public final boolean isAncestor(MuisElement ancestor)
 	{
 		if(ancestor == this)
 			return true;
 		MuisElement parent = theParent;
 		while(parent != null)
 		{
 			if(parent == ancestor)
 				return true;
 			parent = parent.theParent;
 		}
 		return false;
 	}
 
 	/**
 	 * @param x The x-coordinate of a point relative to this element's upper left corner
 	 * @param y The y-coordinate of a point relative to this element's upper left corner
 	 * @return All children of this element whose bounds contain the given point
 	 */
 	public final MuisElement [] childrenAt(int x, int y)
 	{
 		MuisElement [] children = sortByZ(theChildren);
 		MuisElement [] ret = new MuisElement[0];
 		for(MuisElement child : children)
 		{
 			int relX = x - child.theX;
 			if(relX < 0 || relX >= child.theW)
 				continue;
 			int relY = y - child.theY;
 			if(relY < 0 || relY >= child.theH)
 				continue;
 			ret = ArrayUtils.add(ret, child);
 		}
 		return ret;
 	}
 
 	/**
 	 * @param x The x-coordinate of a point relative to this element's upper left corner
 	 * @param y The y-coordinate of a point relative to this element's upper left corner
 	 * @return The deepest (and largest-Z) descendant of this element whose bounds contain the given point
 	 */
 	public final MuisElement deepestChildAt(int x, int y)
 	{
 		MuisElement current = this;
 		MuisElement [] children = current.childrenAt(x, y);
 		while(children.length > 0)
 		{
 			x -= current.theX;
 			y -= current.theY;
 			current = children[0];
 			children = current.childrenAt(x, y);
 		}
 		return current;
 	}
 
 	// End hierarchy methods
 
 	/** @return The style that modifies this element's appearance */
 	public final ElementStyle getStyle()
 	{
 		return theStyle;
 	}
 
 	/** @return The tool kit that this element belongs to */
 	public final MuisToolkit getToolkit()
 	{
 		return theToolkit;
 	}
 
 	/** @return The MUIS class view that allows for instantiation of child elements */
 	public final MuisClassView getClassView()
 	{
 		return theClassView;
 	}
 
 	/** @return The namespace that this tag was instantiated in */
 	public final String getNamespace()
 	{
 		return theNamespace;
 	}
 
 	/** @return The name of the tag that was used to instantiate this element */
 	public final String getTagName()
 	{
 		return theTagName;
 	}
 
 	// Bounds methods
 
 	/** @return The x-coordinate of this element's upper left corner */
 	public final int getX()
 	{
 		return theX;
 	}
 
 	/** @param x The x-coordinate for this element's upper left corner */
 	public final void setX(int x)
 	{
 		if(theX == x)
 			return;
 		checkSecurity(PermissionType.setBounds, null);
 		Rectangle preBounds = new Rectangle(theX, theY, theW, theH);
 		theX = x;
 		fireEvent(new MuisPropertyEvent<Rectangle>(BOUNDS_CHANGED, preBounds, new Rectangle(theX, theY, theW, theH)), false, false);
 	}
 
 	/** @return The y-coordinate of this element's upper left corner */
 	public final int getY()
 	{
 		return theY;
 	}
 
 	/** @param y The y-coordinate for this element's upper left corner */
 	public final void setY(int y)
 	{
 		if(theY == y)
 			return;
 		checkSecurity(PermissionType.setBounds, null);
 		Rectangle preBounds = new Rectangle(theX, theY, theW, theH);
 		theY = y;
 		fireEvent(new MuisPropertyEvent<Rectangle>(BOUNDS_CHANGED, preBounds, new Rectangle(theX, theY, theW, theH)), false, false);
 	}
 
 	/**
 	 * @param x The x-coordinate for this element's upper left corner
 	 * @param y The y-coordinate for this element's upper left corner
 	 */
 	public final void setPosition(int x, int y)
 	{
 		if(theX == x && theY == y)
 			return;
 		checkSecurity(PermissionType.setBounds, null);
 		Rectangle preBounds = new Rectangle(theX, theY, theW, theH);
 		theX = x;
 		theY = y;
 		fireEvent(new MuisPropertyEvent<Rectangle>(BOUNDS_CHANGED, preBounds, new Rectangle(theX, theY, theW, theH)), false, false);
 	}
 
 	/** @return The z-index determining the order in which this element is drawn among its siblings */
 	public final int getZ()
 	{
 		return theZ;
 	}
 
 	/** @param z The z-index determining the order in which this element is drawn among its siblings */
 	public final void setZ(int z)
 	{
 		if(theZ == z)
 			return;
 		checkSecurity(PermissionType.setZ, null);
 		theZ = z;
 		if(theParent != null)
 			theParent.repaint(new Rectangle(theX, theY, theW, theH), false);
 	}
 
 	/** @return The width of this element */
 	public final int getWidth()
 	{
 		return theW;
 	}
 
 	/** @param width The width for this element */
 	public final void setWidth(int width)
 	{
 		if(theW == width)
 			return;
 		checkSecurity(PermissionType.setBounds, null);
 		Rectangle preBounds = new Rectangle(theX, theY, theW, theH);
 		theW = width;
 		fireEvent(new MuisPropertyEvent<Rectangle>(BOUNDS_CHANGED, preBounds, new Rectangle(theX, theY, theW, theH)), false, false);
 	}
 
 	/** @return The height of this element */
 	public final int getHeight()
 	{
 		return theH;
 	}
 
 	/** @param height The height for this element */
 	public final void setHeight(int height)
 	{
 		if(theH == height)
 			return;
 		checkSecurity(PermissionType.setBounds, null);
 		Rectangle preBounds = new Rectangle(theX, theY, theW, theH);
 		theH = height;
 		fireEvent(new MuisPropertyEvent<Rectangle>(BOUNDS_CHANGED, preBounds, new Rectangle(theX, theY, theW, theH)), false, false);
 	}
 
 	/**
 	 * @param width The width for this element
 	 * @param height The height for this element
 	 */
 	public final void setSize(int width, int height)
 	{
 		if(theW == width && theH == height)
 			return;
 		checkSecurity(PermissionType.setBounds, null);
 		Rectangle preBounds = new Rectangle(theX, theY, theW, theH);
 		theW = width;
 		theH = height;
 		fireEvent(new MuisPropertyEvent<Rectangle>(BOUNDS_CHANGED, preBounds, new Rectangle(theX, theY, theW, theH)), false, false);
 	}
 
 	/**
 	 * @param x The x-coordinate for this element's upper left corner
 	 * @param y The y-coordinate for this element's upper left corner
 	 * @param width The width for this element
 	 * @param height The height for this element
 	 */
 	public final void setBounds(int x, int y, int width, int height)
 	{
 		if(theX == x && theY == y && theW == width && theH == height)
 			return;
 		checkSecurity(PermissionType.setBounds, null);
 		Rectangle preBounds = new Rectangle(theX, theY, theW, theH);
 		theX = x;
 		theY = y;
 		theW = width;
 		theH = height;
 		fireEvent(new MuisPropertyEvent<Rectangle>(BOUNDS_CHANGED, preBounds, new Rectangle(theX, theY, theW, theH)), false, false);
 	}
 
 	@Override
 	public SizePolicy getWSizer(int height)
 	{
 		if(theHSizer == null)
 			theHSizer = new SimpleSizePolicy();
 		return theHSizer;
 	}
 
 	@Override
 	public SizePolicy getHSizer(int width)
 	{
 		if(theVSizer == null)
 			theVSizer = new SimpleSizePolicy();
 		return theVSizer;
 	}
 
 	/** @return This element's position relative to the document's root */
 	public final Point getDocumentPosition()
 	{
 		int x = 0;
 		int y = 0;
 		MuisElement el = this;
 		while(el.theParent != null)
 		{
 			x += el.theX;
 			y += el.theY;
 			el = el.theParent;
 		}
 		return new Point(x, y);
 	}
 
 	// End bounds methods
 
 	/** @return Whether this element is able to accept the focus for the document */
 	public boolean isFocusable()
 	{
 		return isFocusable;
 	}
 
 	/** @param focusable Whether this element should be focusable */
 	protected final void setFocusable(boolean focusable)
 	{
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
 	public final <T> void addListener(MuisEventType<T> type, MuisEventListener<? super T> listener)
 	{
 		theListeners.addListener(type, listener);
 	}
 
 	/** @param listener The listener to remove from this element */
 	public final void removeListener(MuisEventListener<?> listener)
 	{
 		theListeners.removeListener(listener);
 	}
 
 	/**
 	 * Removes all listeners for the given event type whose class is exactly equal (not an extension of) the given listener type
 	 *
 	 * @param type The type of event to stop listening for
 	 * @param listenerType The listener type to remove
 	 */
 	public final void removeListener(MuisEventType<?> type, Class<? extends MuisEventListener<?>> listenerType)
 	{
 		for(MuisEventListener<?> listener : theListeners.getListeners(type))
 			if(listener.getClass() == listenerType)
 				theListeners.removeListener(listener);
 	}
 
 	/**
 	 * Adds a listener for an event type to this element's direct children
 	 *
 	 * @param <T> The type of the property that the event represents
 	 * @param type The event type to listen for
 	 * @param listener The listener to notify when an event of the given type occurs
 	 */
 	public final <T> void addChildListener(MuisEventType<T> type, MuisEventListener<? super T> listener)
 	{
 		theChildListeners.addListener(type, listener);
 		for(MuisElement child : theChildren)
 			child.addListener(type, listener);
 	}
 
 	/** @param listener The listener to remove from this element's children */
 	public final void removeChildListener(MuisEventListener<?> listener)
 	{
 		theChildListeners.removeListener(listener);
 		for(MuisElement child : theChildren)
 			child.removeListener(listener);
 	}
 
 	/**
 	 * Fires an event on this element
 	 *
 	 * @param <T> The type of the event's property
 	 * @param event The event to fire
 	 * @param fromDescendant Whether the event was fired on one of this element's descendants or on this element specifically
 	 * @param toAncestors Whether the event should be fired on this element's ancestors as well
 	 */
 	public final <T> void fireEvent(MuisEvent<T> event, boolean fromDescendant, boolean toAncestors)
 	{
 		checkSecurity(PermissionType.fireEvent, event.getType());
 		MuisEventListener<T> [] listeners = theListeners.getListeners(event.getType());
 		for(MuisEventListener<T> listener : listeners)
 			if(!fromDescendant || !listener.isLocal())
 				listener.eventOccurred(event, this);
 		if(toAncestors && theParent != null)
 			theParent.fireEvent(event, true, true);
 	}
 
 	/**
 	 * Fires a user-generated event on this element, propagating up toward the document root unless canceled
 	 *
 	 * @param event The event to fire
 	 */
 	public final void fireUserEvent(org.muis.core.event.UserEvent event)
 	{
 		checkSecurity(PermissionType.fireEvent, event.getType());
 		MuisEventListener<Void> [] listeners = theListeners.getListeners(event.getType());
 		for(MuisEventListener<Void> listener : listeners)
 			if(event.getElement() == this || !listener.isLocal())
 				listener.eventOccurred(event, this);
 		if(!event.isCanceled() && theParent != null)
 			theParent.fireEvent(event, true, true);
 	}
 
 	/**
 	 * Fires appropriate listeners on this element's subtree for a positioned event which occurred within this element's bounds
 	 *
 	 * @param event The event that occurred
 	 * @param x The x-coordinate of the position at which the event occurred, relative to this element's upper-left corner
 	 * @param y The y-coordinate of the position at which the event occurred, relative to this element's upper-left corner
 	 */
 	protected final void firePositionEvent(org.muis.core.event.PositionedUserEvent event, int x, int y)
 	{
 		checkSecurity(PermissionType.fireEvent, event.getType());
 		MuisElement [] children = childrenAt(x, y);
 		for(int c = children.length - 1; c >= 0; c--)
 		{
 			if(event.isCanceled())
 				continue;
 			MuisElement child = children[c];
 			int relX = x - child.theX;
 			int relY = y - child.theY;
 			child.firePositionEvent(event, relX, relY);
 		}
 		if(!event.isCanceled())
 			fireEvent(event, event.getElement() == this, false);
 	}
 
 	// End event methods
 
 	// Messaging events
 
 	@Override
 	public final void fatal(String message, Throwable exception, Object... params)
 	{
 		message(MuisMessage.Type.FATAL, message, exception, params);
 	}
 
 	@Override
 	public final void error(String message, Throwable exception, Object... params)
 	{
 		message(MuisMessage.Type.ERROR, message, exception, params);
 	}
 
 	@Override
 	public final void warn(String message, Object... params)
 	{
 		message(MuisMessage.Type.WARNING, message, null, params);
 	}
 
 	@Override
 	public final void warn(String message, Throwable exception, Object... params)
 	{
 		message(MuisMessage.Type.WARNING, message, exception, params);
 	}
 
 	/**
 	 * Records a message in this element
 	 *
 	 * @param type The type of the message
 	 * @param text The text of the message
 	 * @param exception The exception which may have caused the message
 	 * @param params Any parameters relevant to the message
 	 */
 	@Override
 	public final void message(MuisMessage.Type type, String text, Throwable exception, Object... params)
 	{
 		MuisMessage message = new MuisMessage(this, type, theLifeCycleManager.getStage(), text, exception, params);
 		theMessages.add(message);
 		if(theWorstMessageType == null || theWorstMessageType.compareTo(type) > 0)
 			theWorstMessageType = type;
 		if(theParent != null)
 			theParent.childMessage(message);
 		fireEvent(new MuisEvent<MuisMessage>(MESSAGE_ADDED, message), false, true);
 	}
 
 	/** @param message The message to remove from this element */
 	public final void removeMessage(MuisMessage message)
 	{
 		if(!theMessages.remove(message))
 			return;
 		reEvalWorstMessage();
 		if(theParent != null)
 			theParent.childMessageRemoved(message);
 		fireEvent(new MuisEvent<MuisMessage>(MESSAGE_REMOVED, message), false, true);
 	}
 
 	/** @return The number of messages in this element */
 	public final int getMessageCount()
 	{
 		return theMessages.size();
 	}
 
 	/**
 	 * @param index The index of the element to get
 	 * @return The message at the given index
 	 */
 	public final MuisMessage getMessage(int index)
 	{
 		return theMessages.get(index);
 	}
 
 	/** @return All messages attached to this element */
 	public final MuisMessage [] getElementMessages()
 	{
 		return theMessages.toArray(new MuisMessage[theMessages.size()]);
 	}
 
 	/** @return All messages attached to this element or its descendants */
 	@Override
 	public final MuisMessage [] getAllMessages()
 	{
 		java.util.ArrayList<MuisMessage> ret = new java.util.ArrayList<MuisMessage>();
 		ret.addAll(theMessages);
 		for(MuisElement child : theChildren)
 			addMessages(ret, child);
 		return ret.toArray(new MuisMessage[ret.size()]);
 	}
 
 	private final static void addMessages(java.util.ArrayList<MuisMessage> ret, MuisElement el)
 	{
 		ret.addAll(el.theMessages);
 		for(MuisElement child : el.theChildren)
 			addMessages(ret, child);
 	}
 
 	/** @return The worst type of message associated with the MUIS element subtree rooted at this element */
 	@Override
 	public final MuisMessage.Type getWorstMessageType()
 	{
 		if(theWorstMessageType == null)
 			return theWorstChildMessageType;
 		if(theWorstMessageType.compareTo(theWorstChildMessageType) > 0)
 			return theWorstMessageType;
 		return theWorstChildMessageType;
 	}
 
 	/** @return The worst type of message associated with this element */
 	public final MuisMessage.Type getWorstElementMessageType()
 	{
 		return theWorstMessageType;
 	}
 
 	/** @return The worst type of message associated with any of this element's descendants */
 	public final MuisMessage.Type getWorstChildMessageType()
 	{
 		return theWorstChildMessageType;
 	}
 
 	private final void childMessage(MuisMessage message)
 	{
 		if(theWorstChildMessageType == null || message.type.compareTo(theWorstChildMessageType) > 0)
 			theWorstChildMessageType = message.type;
 		if(theParent != null)
 			theParent.childMessage(message);
 	}
 
 	private final void childMessageRemoved(MuisMessage message)
 	{
 		if(theParent != null)
 			theParent.childMessageRemoved(message);
 	}
 
 	private final void reEvalWorstMessage()
 	{
 		MuisMessage.Type type = null;
 		for(MuisMessage message : theMessages)
 			if(type == null || message.type.compareTo(type) > 0)
 				type = message.type;
 		if(theWorstMessageType == null ? type != null : theWorstMessageType != type)
 		{
 			theWorstMessageType = type;
 			if(theParent != null && theParent.theWorstChildMessageType != null && theParent.theWorstChildMessageType == type)
 				theParent.reEvalChildWorstMessage();
 		}
 	}
 
 	private final void reEvalChildWorstMessage()
 	{
 		MuisMessage.Type type = null;
 		for(MuisElement child : theChildren)
 		{
 			MuisMessage.Type childType = child.getWorstMessageType();
 			if(type == null || childType.compareTo(type) > 0)
 				type = childType;
 		}
 		if(theWorstChildMessageType == null ? type != null : theWorstChildMessageType != type)
 		{
 			theWorstChildMessageType = type;
 			if(theParent != null && theParent.theWorstChildMessageType != null && theParent.theWorstChildMessageType == type)
 				theParent.reEvalChildWorstMessage();
 		}
 	}
 
 	// End messaging methods
 
 	/**
 	 * Sorts a set of elements by z-index in ascending order. This operation is useful for rendering children in correct sequence and in
 	 * determining which elements should receive events first.
 	 *
 	 * @param children The children to sort by z-index. This array is not modified.
 	 * @return The sorted array
 	 */
 	public static final MuisElement [] sortByZ(MuisElement [] children)
 	{
 		if(children.length < 2)
 			return children;
 		boolean sameZ = true;
 		int z = children[0].theZ;
 		for(int c = 1; c < children.length; c++)
 			if(children[c].theZ != z)
 			{
 				sameZ = false;
 				break;
 			}
 		if(!sameZ)
 		{
 			children = children.clone();
 			java.util.Arrays.sort(children, new java.util.Comparator<MuisElement>() {
 				@Override
 				public int compare(MuisElement el1, MuisElement el2)
 				{
 					return el1.theZ - el2.theZ;
 				}
 			});
 		}
 		return children;
 	}
 
 	/**
 	 * Generates an XML-representation of this element's content
 	 *
 	 * @param indent The indention string to use for each level away from the margin
 	 * @return The XML string representing this element
 	 */
 	public final String asXML(String indent)
 	{
 		StringBuilder ret = new StringBuilder();
 		appendXML(ret, indent, 0);
 		return ret.toString();
 	}
 
 	/**
 	 * Appends this element's XML-representation to a string builder
 	 *
 	 * @param str The string builder to append to
 	 * @param indent The indention string to use for each level away from the margin
 	 * @param level The depth of this element in the structure being printed
 	 */
 	protected final void appendXML(StringBuilder str, String indent, int level)
 	{
 		for(int L = 0; L < level; L++)
 			str.append(indent);
 		str.append('<');
 		if(theNamespace != null)
 		{
 			str.append(theNamespace);
 			str.append(':');
 		}
 		str.append(theTagName);
 		for(java.util.Map.Entry<MuisAttribute<?>, Object> entry : theAttrValues.entrySet())
 		{
 			str.append(' ');
 			str.append(entry.getKey().name);
 			str.append('=');
 			str.append('"');
 			str.append(entry.getValue());
 			str.append('"');
 		}
 		if(theChildren.length == 0)
 		{
 			str.append('/');
 			str.append('>');
 			return;
 		}
 		str.append('>');
 		for(int c = 0; c < theChildren.length; c++)
 		{
 			str.append('\n');
 			theChildren[c].appendXML(str, indent, level + 1);
 		}
 		str.append('\n');
 		for(int L = 0; L < level; L++)
 			str.append(indent);
 		str.append('<');
 		str.append('/');
 		if(theNamespace != null)
 		{
 			str.append(theNamespace);
 			str.append(':');
 		}
 		str.append(theTagName);
 		str.append('>');
 	}
 
 	// Layout methods
 
 	/**
 	 * Causes this element to adjust the position and size of its children in a way defined in this element type's implementation. By
 	 * default this does nothing.
 	 */
 	public void doLayout()
 	{
 		theLayoutDirtyTime = 0;
 		for(MuisElement child : getChildren())
 			child.doLayout();
 	}
 
 	/**
 	 * Causes a call to {@link #doLayout()}
 	 *
 	 * @param now Whether to perform the layout action now or allow it to be performed asynchronously
 	 */
 	public void relayout(boolean now)
 	{
 		if(theW <= 0 || theH <= 0)
 			return; // No point layout out if there's nothing to show
 		theLayoutDirtyTime = System.currentTimeMillis();
 		MuisEventQueue.get().scheduleEvent(new MuisEventQueue.LayoutEvent(this, now), now);
 	}
 
 	/** @return The last time a layout event was scheduled for this element */
 	public long getLayoutDirtyTime()
 	{
 		return theLayoutDirtyTime;
 	}
 
 	// End layout methods
 
 	// Paint methods
 
 	/** @return The graphics object to use to draw this element */
 	public Graphics2D getGraphics()
 	{
 		int x = 0, y = 0;
 		MuisElement el = this;
 		while(el.theParent != null)
 		{
 			x += getX();
 			y += getY();
 			el = el.theParent;
 		}
 		java.awt.Graphics2D graphics = theDocument.getGraphics();
 		if(el != theDocument.getRoot())
 		{
 			graphics = (Graphics2D) graphics.create(x, y, 0, 0);
 			return graphics;
 		}
 		graphics = (Graphics2D) graphics.create(x, y, theW, theH);
 		return graphics;
 	}
 
 	/**
 	 * Renders this element in a graphics context.
 	 *
 	 * @param graphics The graphics context to render this element in
 	 * @param area The area to draw
 	 */
 	public void paint(java.awt.Graphics2D graphics, Rectangle area)
 	{
 		if((area != null && (area.width == 0 || area.height == 0)) || theW == 0 || theH == 0)
 			return;
 		theCacheW = theW;
 		theCacheH = theH;
 		paintSelf(graphics, area);
 		Rectangle clipBounds = graphics.getClipBounds();
 		if(clipBounds == null)
 			clipBounds = new Rectangle(0, 0, theW, theH);
 		// TODO Should we clip?
 		graphics.setClip(clipBounds.x, clipBounds.y, theW, theH);
 		try
 		{
 			paintChildren(graphics, theChildren, area);
 		} finally
 		{
 			graphics.setClip(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
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
 	public final void repaint(Rectangle area, boolean now)
 	{
 		if(theW <= 0 || theH <= 0)
 			return; // No point painting if there's nothing to show
 		thePaintDirtyTime = System.currentTimeMillis();
 		MuisEventQueue.get().scheduleEvent(new MuisEventQueue.PaintEvent(this, area, now), now);
 	}
 
 	/**
 	 * Renders this element's background or its content, but NOT its children. Children are rendered by
 	 * {@link #paintChildren(java.awt.Graphics2D, MuisElement [], Rectangle)}. By default, this merely draws the element's background color.
 	 *
 	 * @param graphics The graphics context to draw in
 	 * @param area The area to paint
 	 */
 	public void paintSelf(java.awt.Graphics2D graphics, Rectangle area)
 	{
 		java.awt.Color bg = getStyle().get(BackgroundStyles.color);
 		if(getStyle().isSet(BackgroundStyles.transparency))
 			bg = new java.awt.Color(bg.getRGB() | (getStyle().get(BackgroundStyles.transparency).intValue() << 24));
 		graphics.setColor(bg);
 		int x = area == null ? 0 : area.x;
 		int y = area == null ? 0 : area.y;
 		int w = area == null ? theW : (area.width < theW ? area.width : theW);
 		int h = area == null ? theH : (area.height < theH ? area.height : theH);
 		graphics.fillRect(x, y, w, h);
 	}
 
 	/** Updates this element's cached position with its real position */
 	protected final void updateCachePosition()
 	{
 		theCacheX = theX;
 		theCacheY = theY;
 	}
 
 	/**
 	 * Draws this element's children
 	 *
 	 * @param graphics The graphics context to render in
 	 * @param children The children to render
 	 * @param area The area in this element's coordinates to repaint
 	 */
 	public void paintChildren(java.awt.Graphics2D graphics, MuisElement [] children, Rectangle area)
 	{
 		if(children.length == 0)
 			return;
 		if(area == null)
 			area = new Rectangle(theX, theY, theW, theH);
 		children = sortByZ(children);
 		int translateX = 0;
 		int translateY = 0;
 		try
 		{
 			Rectangle childArea = new Rectangle();
 			for(MuisElement child : children)
 			{
 				child.updateCachePosition();
 				int childX = child.theX;
 				int childY = child.theY;
 				translateX += childX;
 				translateY += childY;
 				childArea.x = area.x - translateX;
 				childArea.y = area.y - translateY;
 				if(childArea.x < 0)
 					childArea.x = 0;
 				if(childArea.y < 0)
 					childArea.y = 0;
 				childArea.width = area.width - childArea.x;
 				if(childArea.x + childArea.width > child.getWidth())
 					childArea.width = child.getWidth() - childArea.x;
 				childArea.height = area.height - childArea.y;
 				if(childArea.y + childArea.height > child.getHeight())
 					childArea.height = child.getHeight() - childArea.y;
 				graphics.translate(translateX, translateY);
 				child.paint(graphics, childArea);
 				translateX -= childX;
 				translateY -= childY;
 			}
 		} finally
 		{
 			if(translateX != 0 || translateY != 0)
 				graphics.translate(-translateX, -translateY);
 		}
 	}
 
 	/** @return The last time a paint event was scheduled for this element */
 	public long getPaintDirtyTime()
 	{
 		return thePaintDirtyTime;
 	}
 
 	/** @return This element's bounds as of the last time it was painted */
 	public Rectangle getCacheBounds()
 	{
 		return new Rectangle(theCacheX, theCacheY, theCacheW, theCacheH);
 	}
 
 	// End paint methods
 }
