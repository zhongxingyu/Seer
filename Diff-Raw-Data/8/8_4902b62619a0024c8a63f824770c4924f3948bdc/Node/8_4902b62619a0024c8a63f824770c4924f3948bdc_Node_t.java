 /*
  * Copyright 2013 Emanuele Tamponi
  *
  * This file is part of object-graph.
  *
  * object-graph is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * object-graph is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with object-graph.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.objectgraph.core;
 
 import com.esotericsoftware.kryo.Kryo;
 import com.objectgraph.pluginsystem.PluginManager;
 import com.objectgraph.utils.PathUtils;
 import org.objenesis.instantiator.ObjectInstantiator;
 import org.objenesis.strategy.InstantiatorStrategy;
 import org.objenesis.strategy.StdInstantiatorStrategy;
 import org.pcollections.HashTreePSet;
 import org.pcollections.PSet;
 
 import java.util.*;
 
 /**
  * Main class of the Object-Graph Framework, provides event handling, error checking and more.
  * <p/>
  * This class provides various facilities to program in a dynamic and event-driven way
  * using standard Java objects, and provide easy access from UI. Subclasses of Node must implement:
  * <ul>
  * <li>A method that returns the names of the "local properties", through {@link #getProperties()}</li>
  * <li>Methods to get/set a local property: {@link #getLocal(String)}, {@link #setLocal(String, Object)}</li>
  * <li>A method to know the declared type for each property, {@link #getDeclaredPropertyType(String)}</li>
  * </ul>
  * <p/>
  * In turn, the following services are provided:
  * <dl>
  * <dt>Automatic dispatch of events through the graph defined by Node properties</dt>
  * <dd>Suppose that a Node, say X, has a property {@code "a"} which in turn references a Node Y with a property
  * {@code "b"}. If b is changed by using {@link #set(String, Object)}, an event is fired and gets propagated to Y
  * through the path "Y.b" and then to X through the path Y.a.b; any number of Triggers can be attached to
  * each Node, which are activated by a matching event. See below and {@link #addTrigger(Trigger)}.</dd>
  * <p/>
  * <dt>Event handling made easy</dt>
  * <dd>For each Event that gets propagated to this Node, each registered Trigger is requested to check if an action is
  * required for that event. For example, if you want to bind a property, say {@code c}, to a transformation
  * of two other properties, say {@code a, b}, you can do this by putting the following line in the constructor:
  * <pre>
  * public NodeType() {
  *       :
  *       :
  *     addTrigger(new Dependency("c", "transform", "a", "b"));
  *       :
  *       :
  * }
  * </pre>
  * together with a protected or public method {@code transform()} that takes two arguments of the same type of a and b
  * and that returns the same type as c.</dd>
  * <p/>
  * <dt>Constrained assignments</dt> <dd>TODO describe constrained assignments</dd>
  * <p/>
  * <dt>Runtime error checking</dt> <dd>When using almost any kind of UI, the user of the interface
  * will enter inconsistent or wrong values for some property. In those cases, you don't want that an exception
  * stops the execution of the UI: instead, it would be great to show an error message with an useful description
  * of the problem. In any cases, if a Node is requested to achieve a computation, a first step would be to check
  * if there are some Errors in it and in this case provide the user a notification and stop the computation
  * until the problems are fixed. This can be obtained using {@link #addErrorCheck(ErrorCheck)}.</dd>
  * </dl>
  *
  * @author Emanuele Tamponi
  */
 public abstract class Node implements EventRecipient {
 
     private final Set<Trigger<?>> triggers = new HashSet<>();
 
     private final Map<String, Set<ErrorCheck<?, ?>>> errorChecks = new HashMap<>();
 
     private final Map<String, Set<Constraint<?, ?>>> constraints = new HashMap<>();
 
     private final static Kryo kryo;
 
     static {
         kryo = new Kryo() {
             InstantiatorStrategy s = new StdInstantiatorStrategy();
             @Override protected ObjectInstantiator newInstantiator(final Class type) {
                 if (Node.class.isAssignableFrom(type)) {
                     return s.newInstantiatorOf(type);
                 }
                 else {
                     return super.newInstantiator(type);
                 }
             }
         };
         kryo.addDefaultSerializer(Node.class, NodeSerializer.class);
         kryo.addDefaultSerializer(ListNode.class, NodeSerializer.class);
         kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
     }
 
     /**
      * A customised instance of Kryo for serialization and cloning.
      * <p/>
      * This Kryo instance has been set up to work nicely with Nodes: you should use it whenever you
      * need to serialize or clone a Node.
      *
      * @return <strong>The</strong> Kryo instance that should be used when working with Nodes
      */
     public static Kryo getKryo() {
         kryo.setClassLoader(PluginManager.getClassLoader());
         return kryo;
     }
 
     /**
      * Performs some tweaks on properties.
      * <p/>
      * You should call this method at the end of the constructor of every subclass, to assure that every property gets
      * correctly initialized. What this method does, is to register properties that may have been initialized outside
      * the constructor. For example, supposing that {@code Child} is a {@link Node} implementation:
      * <pre>
      *     public MyNode extends ObjectNode {
      *         {@literal @}Property Child child = new Child(); // This is initialized outside the constructor, without set()
      *     }
      * </pre>
      * in this case, as you see, the property MyNode.child is not initialised using the {@link #set(String, Object)}
      * method. By calling {@code initialiseNode()} at the end of the constructor, every such property get correctly
      * set.
      *
      */
     protected void initialiseNode() {
         for (String property : getProperties()) {
             if (get(property) instanceof Node) { // && !get(property, Node.class).getParentPaths().containsKey(this)) {
                 Object value = get(property);
                 setLocal(property, null);
                 set(property, value);
             }
         }
     }
 
     /**
      * Adds an {@link EventRecipient} parent to this Node.
      * <p/>
      * The event dispatch system works using the notion of "parent" EventRecipients: whenever an Event gets propagated
      * to this Node, it gets dispatched to every parent through a path defined by the {@code property} parameter.
      * <p/>
      * This method should be used with great care, as it is used internally by {@link #set(String, Object)} and other
      * methods, and you should not need to use it directly.
      *
      * @param parent   the parent EventRecipient
      * @param property the name of the property that connects the parent to this Node
      */
     public void addParentPath(EventRecipient parent, String property) {
         ParentRegistry.register(parent, property, this);
     }
 
     /**
      * Removes a previously defined parent EventRecipient
      * <p/>
      * See {@link #addParentPath(EventRecipient, String)} for a definition of parents.
      *
      * @param parent   the parent EventRecipient to remove from the parent list
      * @param property the name of the property that connects the parent to this Node
      */
     public void removeParentPath(EventRecipient parent, String property) {
         ParentRegistry.unregister(parent, property, this);
     }
 
     /**
      * Set the content of the property specified by the given path and fires a SetProperty event in case of success.
      * <p/>
      * You should use this method to set Node properties (either directly, or wrapped in a standard setter method). It
      * is connected with the event system, so that once the property has been set, a new {@link Event} with type
      * {@link SetProperty} {@link EventType} gets fired and propagates through the graph defined by parents paths.
      * <p/>
      * This method can be used to set either a <i>local</i> property, or a <i>nested</i> property, that is, a property
      * that belongs to a Node which is in turn a property of this Node (the path can be arbitrarly deep).
      * <p/>
      * To show how you can use this method, consider the following example:
      * <pre>
      *     public class MyChild extends ObjectNode {
      *         {@literal @}Property String s;
      *          :
      *          :
      *     }
      *
      *     public class MyNode extends ObjectNode {
      *         {@literal @}Property MyChild child;
      *          :
      *          :
      *     }
      *
      *     public static void main(String... args) {
      *         MyNode node = new MyNode();
      *         node.set("child.s", "Hello, World!");
      *     }
      * </pre>
      *
      * @param path the named path to the property that should be set
      * @param value the new value of the property
      */
     public void set(String path, Object value) {
         if (path.isEmpty()) {
             throw new MalformedPathException(this, path, "Empty path cannot be set.");
         }
 
         int firstSplit = path.indexOf('.');
         if (firstSplit < 0) {
             if (!hasProperty(path)) {
                 throw new PropertyNotExistsException(this, path);
             }
 
             Object oldValue = getLocal(path);
             if (oldValue != value) {
                 if (oldValue instanceof Node) {
                     ((Node) oldValue).removeParentPath(this, path);
                 }
 
                 setLocal(path, value);
 
                 if (value instanceof Node) {
                     ((Node) value).addParentPath(this, path);
                 }
 
                 fireEvent(new Event(path, new SetProperty(new RootedProperty(this, path), oldValue, value)));
             }
         } else {
             String localProperty = path.substring(0, firstSplit);
             String remainingPath = path.substring(firstSplit + 1);
             Node local = getLocal(localProperty);
             if (local != null) {
                 local.set(remainingPath, value);
             }
         }
     }
 
     /**
      * Defines how to set a local property
      *
      * The Node abstract class does not define how properties are handled "internally" by implementations. The way with
      * which a certain named property is set is defined by the implementation itself. Look at {@link ObjectNode} and
      * {@link ListNode} for examples. ObjectNode defines properties by using the annotation {@link com.objectgraph.core.ObjectNode.Property},
      * while ListNode defines a property by the index with which you'd access an element in the list. Look also at the example
      * in {@link #set(String, Object)}.
      *
      * @param property the name of the property that should be set
      * @param value the new value of the property
      */
     protected abstract void setLocal(String property, Object value);
 
     /**
      * Returns the value of the property defined by the given path, and casts it to the given type.
      * <p/>
      * This is used to access local or nested properties by their runtime names.
      *
      * @param path the path to the property
      * @param type the type to which you want to cast the value
      * @return the value of the property, or null if some intermediate Node is null
      */
     @SuppressWarnings("unchecked")
     public <T> T get(String path, Class<T> type) {
         return (T) get(path);
     }
 
     /**
      * Returns the value of the property defined by the given path.
      * <p/>
      * This is used to access local or nested properties by their runtime names.
      *
      * @param path the path to the property
      * @return the value of the property, or null if some intermediate Node is null
      */
     @SuppressWarnings("unchecked")
     public <T> T get(String path) {
         if (path.isEmpty()) {
             return (T) this;
         }
 
         int firstSplit = path.indexOf('.');
         if (firstSplit < 0) {
             if (!hasProperty(path)) {
                 throw new PropertyNotExistsException(this, path);
             }
 
             return getLocal(path);
         } else {
             String localProperty = path.substring(0, firstSplit);
             String remainingPath = path.substring(firstSplit + 1);
             Node local = getLocal(localProperty);
             if (local != null) {
                 return local.get(remainingPath);
             }
             else {
                 return null;
             }
         }
     }
 
     /**
      * Defines how to access to a local property
      *
      * The Node class does not specify how to access local property. This method returns the value of the property whose
      * name is given as parameter. See {@link ObjectNode} or {@link ListNode} for a reference implementation.
      *
      * @param property the name of the local property
      * @return the value of the property
      */
     protected abstract <T> T getLocal(String property);
 
     /**
      * Checks if the given property is present in the current object
      * <p/>
      * Consider that some methods throw exceptions if an invalid property name is given as parameter.
      *
      * @param property the name of the property
      * @return {@code true} if the property is present; {@code false} in the opposite case
      */
     public boolean hasProperty(String property) {
         return getProperties().contains(property);
     }
 
     /**
      * A list of property names
      *
      * @return a sorted list of all property names in the current object.
      */
     public abstract List<String> getProperties();
 
     /**
      * A list of properties that are not controlled by any {@link Trigger}.
      * <p/>
      * Each trigger can define one or more "controlled" or "bound" properties. The concept of bound properties is
      * particularly useful when coupled with dynamic GUI generation, as you can decide that bound properties should not
      * show up in the user interface, or should be shown in read-only mode.
      *
      * @return a list of properties not controlled by any Trigger
      */
     public List<String> getFreeProperties() {
         List<String> ret = new ArrayList<>(getProperties());
         ret.removeAll(getControlledProperties());
         return ret;
     }
 
     /**
      * A list of properties controlled by some {@link Trigger}
      * <p/>
      * See {@link #getFreeProperties()} as a reference.
      *
      * @return a list of properties controlled by at least one Trigger
      */
     public List<String> getControlledProperties() {
         List<String> ret = new ArrayList<>();
         getControlledProperties("", ret, HashTreePSet.<Node>singleton(this));
         return ret;
     }
 
     private void getControlledProperties(String prefixPath, List<String> controlled, PSet<Node> seen) {
         for (Trigger<?> t : triggers) {
             for (String path : t.getControlledPaths()) {
                 if (PathUtils.isParent(prefixPath, path)) {
                     controlled.add(PathUtils.toLocalProperty(path));
                 }
             }
         }
 
         for (EventRecipient p : getParentPaths().keySet()) {
             if (p instanceof Node && !seen.contains(p)) {
                 Node parent = (Node)p;
                 for (String path : getParentPaths().get(parent)) {
                     parent.getControlledProperties(PathUtils.appendPath(path, prefixPath), controlled, seen.plus(parent));
                 }
             }
 
         }
     }
 
     /**
      * The current set of parents
      *
      * Returns a map whose keys are the current parents of this Node. Parents are automatically garbaged by the garbage
      * collector, so the map returned by this method is the set of parents <i>at the time in which the method has been
      * called</i>.
      *
      * @return a map whose keys are the parents and whose values are the properties with which the parent is connected
      * to this Node
      */
     public Map<EventRecipient, Set<String>> getParentPaths() {
         return ParentRegistry.getParentPaths(this);
     }
 
     /**
      * Fires an {@link Event} starting from this node.
      *
      * With this method one can start the propagation of an Event from this node. To see how Nodes propagate Events, see
      * {@link #handleEvent(Event, org.pcollections.PSet)}.
      *
      * This method is intended for internal use and should be used with great care.
      *
      * @param e the Event to fire
      */
     public void fireEvent(Event e) {
         handleEvent(e, HashTreePSet.<EventRecipient>singleton(this));
     }
 
     /**
      * Implements Event handling for Nodes: checks local {@link Trigger}s and propagates the received Event to parents
      *
      * Once an {@link Event} reachs this Node, every {@link Trigger} registered using {@link #addTrigger(Trigger)} is checked
      * against the event, and triggered if necessary.
      * <p/>
      * After that, the event is propagated to every parent through the relative path. The second parameters is used to
      * assure that the method doesn't loop if cycles are found.
      *
      * @param e the Event that reachs this object
      * @param visited other objects already visited in the current dispatch chain of this Event.
      */
     @Override
     public void handleEvent(Event e, PSet<EventRecipient> visited) {
         for (Trigger<?> t : triggers) {
             t.check(e);
         }
 
         for (EventRecipient parent : getParentPaths().keySet()) {
             if (visited.contains(parent)) {
                 continue;
             }
            try {
                for (String path : getParentPaths().get(parent)) {
                    parent.handleEvent(e.backPropagate(path), visited.plus(parent));
                }
            } catch (NullPointerException ex) {
                // This happens because of weak references
             }
         }
     }
 
     /**
      * Register a {@link Trigger} to this Node.
      *
      * Registering a Trigger means that the trigger will be checked for each Event that reaches this Node. The method also
      * set the {@code node} field of the Trigger.
      *
      * @param t the Trigger to be registered
      */
     @SuppressWarnings("unchecked")
     public <N extends Node> void addTrigger(Trigger<N> t) {
         t.setNode((N)this);
         triggers.add(t);
     }
 
     /**
      * Removes a previously registered Trigger.
      *
      * @param t the Trigger to be unregistered
      */
     @SuppressWarnings("unchecked")
     public <N extends Node> void removeTrigger(Trigger<N> t) {
         if (t.getNode() != this) {
             throw new NodeHelperUsedByOtherException(t, t.getNode(), this);
         }
         triggers.remove(t);
         t.setNode(null);
     }
 
     /**
      * Returns the runtime or declared type for the given property
      *
      * @param property the name of the property
      * @param runtime whether to check for the declared type or the runtime type
      * @return the type of the property, or {@code null} if runtime is {@code true} and the property value is null
      */
     public Class<?> getPropertyType(String property, boolean runtime) {
         if (!hasProperty(property)) {
             throw new PropertyNotExistsException(this, property);
         }
         if (runtime) {
             Object content = getLocal(property);
             return content == null ? null : content.getClass();
         } else {
             return getDeclaredPropertyType(property);
         }
     }
 
     /**
      * Returns the declared property type for the given property
      *
      * @param property the name of the property
      * @return the type with which the given property has been declared
      */
     protected abstract Class<?> getDeclaredPropertyType(String property);
 
     /**
      * Register an {@link ErrorCheck}
      *
      * @param e the ErrorCheck to be registered
      */
     @SuppressWarnings("unchecked")
     public <N extends Node> void addErrorCheck(ErrorCheck<N, ?> e) {
         e.setNode((N) this);
         if (!errorChecks.containsKey(e.getPath())) {
             errorChecks.put(e.getPath(), new HashSet<ErrorCheck<?, ?>>());
         }
         errorChecks.get(e.getPath()).add(e);
     }
 
     /**
      * Removes a previously defined {@link ErrorCheck}
      *
      * @param e the ErrorCheck to be removed
      */
     public void removeErrorCheck(ErrorCheck<?, ?> e) {
         if (e.getNode() != this) {
             throw new NodeHelperUsedByOtherException(e, e.getNode(), this);
         }
 
         errorChecks.get(e.getPath()).remove(e);
         if (errorChecks.get(e.getPath()).isEmpty()) {
             errorChecks.remove(e.getPath());
         }
         e.setNode(null);
     }
 
     /**
      * Recursively find errors in nested paths using registered {@link ErrorCheck}s
      *
      * @return a map with the properties which generated the {@link Error}s as keys and a set of errors as values
      */
     public Map<String, Set<Error>> getErrors() {
         Map<String, Set<Error>> ret = new LinkedHashMap<>();
         getErrors(ret, "", new HashSet<Node>());
         return ret;
     }
 
     private void getErrors(Map<String, Set<Error>> errors, String path, Set<Node> seen) {
         if (seen.contains(this)) {
             return;
         }
         seen.add(this);
         for (Set<ErrorCheck<?, ?>> checks : errorChecks.values()) {
             for (ErrorCheck<?, ?> check: checks) {
                 Error error = check.getError();
                 if (error != null) {
                     String completePath = PathUtils.appendPath(path, check.getPath());
                     if (!errors.containsKey(completePath)) {
                         errors.put(completePath, new HashSet<Error>());
                     }
                     errors.get(completePath).add(error);
                 }
             }
         }
         for (String property : getProperties()) {
             Object content = get(property);
             if (content instanceof Node) {
                 ((Node) content).getErrors(errors, PathUtils.appendPath(path, property), seen);
             }
         }
     }
 
     /**
      * Register a new {@link Constraint}
      *
      * {@link Constraint}s are a special kind of {@link ErrorCheck}s that can be used by the {@link PluginManager} to
      * find <i>compatible</i> implementations of a given property. See the documentation of {@link PluginManager} for
      * further help.
      *
      * @param constraint the Constraint to register
      */
     @SuppressWarnings("unchecked")
     public <N extends Node> void addConstraint(Constraint<N, ?> constraint) {
         // TODO move the Constraint system to the PluginManager
         constraint.setNode((N) this);
         if (!constraints.containsKey(constraint.getPath())) {
             constraints.put(constraint.getPath(), new HashSet<Constraint<?, ?>>());
         }
 
         constraints.get(constraint.getPath()).add(constraint);
         addErrorCheck(constraint);
     }
 
     /**
      * Remove a previously registered {@link Constraint}
      *
      * @param constraint the constraint to be removed
      */
     public void removeConstraint(Constraint<?, ?> constraint) {
         removeErrorCheck(constraint);
 
         constraints.get(constraint.getPath()).remove(constraint);
         if (constraints.get(constraint.getPath()).isEmpty()) {
             constraints.remove(constraint.getPath());
         }
     }
 
     /**
      * Return a list of values compatible with every {@link Constraint} defined for this property in this Node or in its parents.
      *
      * @param property the name of the property
      * @return a {@link List} of newly instantiated objects that can be assigned to the given property
      */
     @SuppressWarnings({"unchecked", "rawtypes"})
     public List<?> getPossiblePropertyValues(String property) {
         if (!hasProperty(property)) {
             throw new PropertyNotExistsException(this, property);
         }
 
         List<Constraint<?, ?>> list = new ArrayList<>();
         getConstraints(property, list, HashTreePSet.<Node>empty());
 
         return PluginManager.getImplementations(getPropertyType(property, false), list);
     }
 
     private void getConstraints(String path, List<Constraint<?, ?>> list, PSet<Node> seen) {
         for(String constrainedPath: constraints.keySet()) {
             if (PathUtils.samePath(constrainedPath, path)) {
                 list.addAll(constraints.get(constrainedPath));
             }
         }
 
         for (EventRecipient p : getParentPaths().keySet()) {
             if (p instanceof Node && !seen.contains(p)) {
                 Node parent = (Node)p;
                 for (String parentPath : getParentPaths().get(parent)) {
                     parent.getConstraints(PathUtils.appendPath(parentPath, path), list, seen.plus(parent));
                 }
             }
         }
     }
 
     /**
      * Recursively find all the {@link ErrorCheck}s relative to the given property.
      *
      * This method goes through the graph of the parents and looks for {@link ErrorCheck} whose
      * {@link com.objectgraph.core.ErrorCheck#getPath()} is compatible with the given property.
      *
      * @param property the name of the property
      * @return a List of {@link ErrorCheck}s
      */
     public List<ErrorCheck<?, ?>> getErrorChecks(String property) {
         if (!hasProperty(property)) {
             throw new PropertyNotExistsException(this, property);
         }
 
         List<ErrorCheck<?, ?>> list = new ArrayList<>();
         getErrorChecks(property, list, HashTreePSet.<Node>empty());
 
         return list;
     }
 
     private void getErrorChecks(String path, List<ErrorCheck<?, ?>> list, PSet<Node> seen) {
         for(String constrainedPath: errorChecks.keySet()) {
             if (PathUtils.samePath(constrainedPath, path)) {
                 list.addAll(errorChecks.get(constrainedPath));
             }
         }
 
         for (EventRecipient p : getParentPaths().keySet()) {
             if (p instanceof Node && !seen.contains(p)) {
                 Node parent = (Node)p;
                 for (String parentPath : getParentPaths().get(parent)) {
                     parent.getErrorChecks(PathUtils.appendPath(parentPath, path), list, seen.plus(parent));
                 }
             }
         }
     }
 
     /**
      * Return a {@link RootedProperty} relative to the given property.
      *
      * @param property the name of the property
      * @return a {@link RootedProperty} object
      */
     public RootedProperty getRootedProperty(String property) {
         if (!hasProperty(property)) {
             throw new PropertyNotExistsException(this, property);
         }
 
         return new RootedProperty(this, property);
     }
 
 }
