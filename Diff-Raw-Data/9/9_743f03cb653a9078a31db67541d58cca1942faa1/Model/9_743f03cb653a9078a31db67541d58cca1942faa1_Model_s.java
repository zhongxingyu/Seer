 package gx.realtime;
 
 import gx.browserchannel.BrowserChannel;
 import gx.browserchannel.message.SaveMessage;
 import gx.realtime.operation.ValueChangedOperation.ValueType;
 import gx.util.RandomUtils;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.*;
 import java.util.Map.Entry;
 
 /**
  * Data model for the document. Contains an object graph that can be referenced
  * via the root node.
  */
 public class Model extends EventTarget
 {
 
     public class NoCompoundOperationInProgressException extends Exception
     {
     }
 
     private Document document;
     private boolean initialized;
     private boolean readOnly;
     private CollaborativeMap root;
 
     /**
      * Undo / Redo stacks
      */
     private LinkedList<RevertableEvent> undoableMutations;
     private LinkedList<RevertableEvent> redoableMutations;
 
     /**
      * Current compound operation.
      */
     private CompoundOperation compoundOperation;
 
     /**
      * Keep track of all the nodes in the data model, indexed
      * by their ID.
      */
     private Map<String, Object> nodes = new HashMap<>();
 
     /**
      * Constructor. Should not be called directly, a model can be
      * retrieved via the document.
      *
      * @param document
      */
     protected Model(Document document)
     {
         this.document = document;
         initialized = false;
         readOnly = false;
 
         root = new CollaborativeMap("root", this);
         undoableMutations = new LinkedList<>();
         redoableMutations = new LinkedList<>();
     }
 
     /**
      * Starts a compound operation. When beginCompoundOperation() is called, all subsequent edits to the data model will be batched together in the
      * undo stack and revision history until endCompoundOperation() is called. Compound operations may be nested inside other compound operations.
      * Note that the compound operation MUST start and end in the same synchronous execution block. If this invariant is violated, the data model
      * will become invalid and all future changes will fail.
      */
     public void beginCompoundOperation()
     {
         compoundOperation = new CompoundOperation(document.getSession().getSessionId(), document.getMe().getUserId(), true);
     }
 
     /**
      * Starts a compound operation. If a name is given, that name will be recorded in the mutation for use in revision history, undo menus, etc.
      * When beginCompoundOperation() is called, all subsequent edits to the data model will be batched together in the undo stack and revision history until
      * endCompoundOperation() is called. Compound operations may be nested inside other compound operations. Note that the compound operation
      * MUST start and end in the same synchronous execution block. If this invariant is violated, the data model will become invalid and all future changes will fail.
      *
      * @param opt_name The name for this compount operation.
      */
     public void beginCompoundOperation(String opt_name)
     {
         compoundOperation = new CompoundOperation(document.getSession().getSessionId(), document.getMe().getUserId(), true, opt_name);
     }
 
     public void beginCreationCompoundOperation()
     {
         //TODO
     }
 
     /**
      * Creates and returns a new collaborative object. This can be used to create custom collaborative objects. For built in types, use the specific create* functions.
      *
      * @param collabType The type of the CollaborativeObject.
      * @param <T>        The generic type of the CollaborativeObject, if applicable.
      * @return A newly created CollaborativeObject.
      */
     public <T extends CollaborativeObject> T create(Class<T> collabType)
     {
         return create(RandomUtils.getRandomAlphaNumeric(), collabType);
     }
 
     /**
      * Creates and returns a new collaborative object. This can be used to create custom collaborative objects. For built in types, use the specific create* functions.
      *
      * @param id         The id that should be assigned to the newly created CollaborativeObject.
      * @param collabType The type of the CollabObject.
      * @param <T>        The generic type of the CollabObject, if applicable.
      * @return A newly created CollaborativeObject.
      */
     private <T extends CollaborativeObject> T create(String id, Class<T> collabType)
     {
         T collabObj;
         try {
             collabObj = collabType.getConstructor(String.class, Model.class).newInstance(id, this);
         } catch (InstantiationException e) {
             e.printStackTrace();
             return null;
         } catch (IllegalAccessException e) {
             e.printStackTrace();
             return null;
         } catch (NoSuchMethodException e) {
             e.printStackTrace();
             return null;
         } catch (InvocationTargetException e) {
             e.printStackTrace();
             return null;
         }
         return collabObj;
     }
 
     /**
      * Creates an empty Collaborative List.
      *
      * @param <E> The generic type of the Collaborative List that should be created.
      * @return A new empty Collaborative List.
      */
     public <E> CollaborativeList<E> createList()
     {
         return new CollaborativeList<E>(RandomUtils.getRandomAlphaNumeric(), this);
     }
 
     /**
      * Creates a Collaborative List with the given initial values.
      *
      * @param opt_initialValue Initial value for the list.
      * @param <E>              The generic type of the newly created list.
      * @return A Collaborative List with the given initial values.
      */
     public <E> CollaborativeList<E> createList(List<E> opt_initialValue)
     {
         CollaborativeList<E> result = createList();
         result.pushAll(opt_initialValue);
         return result;
     }
 
     /**
      * Creates an empty Collaborative Map.
      *
      * @return A newly created empty Collaborative Map.
      */
     public CollaborativeMap createMap()
     {
         return new CollaborativeMap(RandomUtils.getRandomAlphaNumeric(), this);
     }
 
     /**
      * Creates a collaborative map with the given values as initial content.
      *
      * @param opt_initialValue Initial value for the map.
      * @return A newly created Collaborative Map with the given values as initial content.
      */
     public CollaborativeMap createMap(Map<String, Object> opt_initialValue)
     {
         CollaborativeMap result = createMap();
         Set<Entry<String, Object>> entries = opt_initialValue.entrySet();
         for (Entry<String, Object> entry : entries) {
             result.set(entry.getKey(), entry.getValue());
         }
         return result;
     }
 
     /**
      * Creates a collaborative string.
      *
      * @return A collaborative string.
      */
     public CollaborativeString createString()
     {
         return new CollaborativeString(RandomUtils.getRandomAlphaNumeric(), this);
     }
 
     /**
      * Creates a collaborative string with the given initial value.
      *
      * @param opt_initialValue Sets the initial value for this string.
      * @return A collaborative string with the given initial value.
      */
     public CollaborativeString createString(String opt_initialValue)
     {
         CollaborativeString result = createString();
         result.setText(opt_initialValue);
         return result;
     }
 
     /**
      * Ends a compound operation. This method will throw an exception if no compound operation is in progress.
      *
      * @throws NoCompoundOperationInProgressException
      *
      */
     public void endCompoundOperation() throws NoCompoundOperationInProgressException
     {
         if (compoundOperation == null || !compoundOperation.isInProgress()) {
             throw new NoCompoundOperationInProgressException();
         } else {
             compoundOperation.setInProgress(false);
             fireEvent(compoundOperation, true);
             //reset
             compoundOperation = null;
         }
     }
 
     /**
      * Returns the root of the object model.
      *
      * @return The root of the object model.
      */
     public CollaborativeMap getRoot()
     {
         return root;
     }
 
     /**
      * Returns whether the model is initialized.
      *
      * @return Whether the model is initialized.
      */
     public boolean isInitialized()
     {
         return initialized;
     }
 
     /**
      * This method is a wrapper function for storing BaseModelEvents on the undoableMutations stack.
      * It checks whether the given BaseModelEvent is revertable. Iff so, it pushes it on the stack, clears the redo stack and fires an
      * UndoRedoStateChangedEvent iff the canUndo or canRedo state has changed.
      *
      * @param e The event that should be registered.
      */
     private void registerMutation(Event e)
     {
         if (e instanceof RevertableEvent) {
             boolean oldUndo = canUndo();
             boolean oldRedo = canRedo();
 
             undoableMutations.push((RevertableEvent) e);
             redoableMutations.clear();
 
             if (oldUndo != canUndo() || oldRedo != canRedo()) {
                 UndoRedoStateChangedEvent urscEvent = new UndoRedoStateChangedEvent(this, canRedo(), canUndo());
                 this.fireEvent(urscEvent);
             }
         }
     }
 
     /**
      * Redo the last thing the active collaborator undid.
      */
     public void redo()
     {
         boolean oldUndo = canUndo();
         boolean oldRedo = canRedo();
 
         RevertableEvent redoableEvent = redoableMutations.pop();
         fireEvent(redoableEvent);
 
         if (oldUndo != canUndo() || oldRedo != canRedo()) {
             UndoRedoStateChangedEvent urscEvent = new UndoRedoStateChangedEvent(this, canRedo(), canUndo());
             this.fireEvent(urscEvent);
         }
     }
 
     /**
      * Undo the last thing the active collaborator did.
      */
     public void undo()
     {
         boolean oldUndo = canUndo();
         boolean oldRedo = canRedo();
 
         RevertableEvent undoableEvent = undoableMutations.pop();
         BaseModelEvent reverseEvent = undoableEvent.getReverseEvent();
         fireEvent(reverseEvent, false);
         redoableMutations.push(undoableEvent);
 
         if (oldUndo != canUndo() || oldRedo != canRedo()) {
             UndoRedoStateChangedEvent urscEvent = new UndoRedoStateChangedEvent(this, canRedo(), canUndo());
             this.fireEvent(urscEvent);
         }
     }
 
     /**
      * @return True iff the model can currently redo.
      */
     public boolean canRedo()
     {
         return redoableMutations.size() > 0;
     }
 
     /**
      * @return True if the model can currently undo.
      */
     public boolean canUndo()
     {
         return undoableMutations.size() > 0;
     }
 
     /**
      * @return The mode of the document. If true, the document is readonly. If false it is editable.
      */
     public boolean isReadOnly()
     {
         return readOnly;
     }
 
     private EventHandler<ObjectAddedEvent> getObjectAddedBuilder()
     {
         return (event) -> {
             String id = event.getTargetId();
             System.out.println("OBJECT_ADDED: " + id);
             CollaborativeObject collabObject = create(id, event.getObjectType());
             nodes.put(id, collabObject);
 
             // Update the root node if we're adding an object with the
             // special "root" ID
             if (id.equals("root")) {
                 root = (CollaborativeMap) collabObject;
             }
         };
     }
 
     private EventHandler<ValuesAddedEvent> getValuesAddedBuilder()
     {
         return (event) -> {
             String id = event.getTargetId();
             System.out.println("VALUES_ADDED: " + id);
 
 
         };
     }
 
     private EventHandler<ValueChangedEvent> getValueChangedBuilder()
     {
         return (event) -> {
             String id = event.getTargetId();
             System.out.println("VALUE_CHANGED: " + id);
         };
     }
 
     private EventHandler<ValuesSetEvent> getValuesSetBuilder()
     {
         return (event) -> {
             String id = event.getTargetId();
             System.out.println("VALUES_SET: " + id);
         };
     }
 
     private EventHandler<ValuesRemovedEvent> getValuesRemovedBuilder()
     {
         return (event) -> {
             String id = event.getTargetId();
             System.out.println("VALUES_REMOVED: " + id);
         };
     }
 
     protected void addNodeFromEvent(ObjectAddedEvent event)
     {
         String id = event.getTargetId();
 
         System.out.println("OBJECT_ADDED: " + id);
         CollaborativeObject collabObject = create(id, event.getObjectType());
         nodes.put(id, collabObject);
 
         // Update the root node if we're adding an object with the
         // special "root" ID
         if (id.equals("root")) {
             root = (CollaborativeMap) collabObject;
         }
     }
 
     /**
      * Update our local data model based on the given (remote) event.
      *
      * @param event
      */
     private void updateModel(BaseModelEvent event)
     {
         String id = event.getTargetId();
         CollaborativeObject collabObject = (CollaborativeObject) nodes.get(id);
         collabObject.updateModel(event);
     }
 
     /**
      * An event may still contains some node IDs in string form,
      * deserialize these now when we know the objects have been created.
      *
      * @param event
      */
     private void deserializeEvent(BaseModelEvent event)
     {
         // Set the target node
         EventTarget targetNode = (EventTarget) nodes.get(event.getTargetId());
         event.setTarget(targetNode);
 
         if (event instanceof ValueChangedEvent) {
             ValueChangedEvent vcEvent = (ValueChangedEvent) event;
             ValueType valueType = vcEvent.getValueType();
             if (valueType.equals(ValueType.COLLABORATIVE_OBJECT)) {
                 Object node = nodes.get(vcEvent.getNewValue());
                 vcEvent.setNewValue(node);
 
                 // The target node should be a map (since it's a ValueChangedEvent)
                 CollaborativeMap targetMap = (CollaborativeMap) targetNode;
 
                 // Set the "old value" using the current value in the model
                 Object currentValue = targetMap.get(vcEvent.getProperty());
                 vcEvent.setOldValue(currentValue);
             }
         }
     }
 
     /**
      * Take an incoming remote event (event received from browserchannel), and use it to update our local
      * model and fire it to the target object, if possible.
      *
      * @param event
      */
     protected void handleRemoteEvent(BaseModelEvent event)
     {
         String targetId = event.getTargetId();
         Object node = getNode(targetId);
 
         if (node == null) {
             // Unknown target ID, so ignore
             //TODO: logging
             return;
         }
 
         if (!(node instanceof EventTarget)) {
             // Not an event target, so ignore
             return;
         }
 
         // TODO: fire childs of ObjectChangedEvent or parent first?
 
         // Unpack contained events
         if (event instanceof ObjectChangedEvent) {
             for (BaseModelEvent e : ((ObjectChangedEvent) event).getEvents()) {
                 deserializeEvent(e);
                 updateModel(e);
                 fireEvent(e);
             }
         }
 
         // The event may still contains some node IDs in string form,
         // deserialize these now that we know the objects have been created
         deserializeEvent(event);
 
         // Update out local mode based on this remote event
         updateModel(event);
 
         fireEvent(event);
     }
 
     public void fireEvent(Event event, boolean register)
     {
         if (event instanceof CompoundOperation) {
             executeCompountOperation((CompoundOperation) event, register);
 
         } else if (compoundOperation != null && compoundOperation.isInProgress() && event instanceof RevertableEvent) {
             compoundOperation.addEvent((RevertableEvent) event);
 
         } else {
             super.fireEvent(event);
 
             //if other target, fire on target
             if (!this.equals(event.getTarget())) {
                 event.getTarget().fireEvent(event);
             }
 
             if (register) {
                 registerMutation(event);
             }
         }
     }
 
     private void executeCompountOperation(CompoundOperation event, boolean register)
     {
         List<RevertableEvent> events = event.getEvents();
         for (RevertableEvent cEvent : events) {
             this.fireEvent(cEvent, false);
         }
 
         if (register) {
             registerMutation(event);
         }
     }
 
     @Override
     public void fireEvent(Event event)
     {
         fireEvent(event, true);
     }
 
     /**
      * @return The document of this Model.
      */
     protected Document getDocument()
     {
         return document;
     }
 
     /**
      * Sends a BaseModelEvent to the remote server.
      *
      * @param event
      */
     public void sendToRemote(BaseModelEvent event)
     {
         BrowserChannel channel = document.getBrowserChannel();
         SaveMessage message = new SaveMessage(event);
         channel.queue(message);
     }
 
     /**
      * @param id The if of the node we are looking for.
      * @return The node with the given id iff found. Null otherwise.
      */
     private Object getNode(String id)
     {
         return nodes.get(id);
     }
 
     /**
      * @return A String representation of this Model.
      */
     public String toString()
     {
         return "Model(nodes=" + nodes + ")";
     }
 }
