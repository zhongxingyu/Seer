 /*
  * Copyright 2012 Robert Philipp
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.freezedry.persistence;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.Reader;
 import java.lang.reflect.Field;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
 import org.freezedry.persistence.annotations.Persist;
 import org.freezedry.persistence.annotations.PersistCollection;
 import org.freezedry.persistence.annotations.PersistDateAs;
 import org.freezedry.persistence.builders.ArrayNodeBuilder;
 import org.freezedry.persistence.builders.BooleanNodeBuilder;
 import org.freezedry.persistence.builders.CollectionNodeBuilder;
 import org.freezedry.persistence.builders.DateNodeBuilder;
 import org.freezedry.persistence.builders.DoubleNodeBuilder;
 import org.freezedry.persistence.builders.IntegerNodeBuilder;
 import org.freezedry.persistence.builders.LongNodeBuilder;
 import org.freezedry.persistence.builders.MapNodeBuilder;
 import org.freezedry.persistence.builders.NodeBuilder;
 import org.freezedry.persistence.builders.ShortNodeBuilder;
 import org.freezedry.persistence.builders.StringNodeBuilder;
 import org.freezedry.persistence.readers.JsonReader;
 import org.freezedry.persistence.readers.PersistenceReader;
 import org.freezedry.persistence.readers.XmlReader;
 import org.freezedry.persistence.tests.Division;
 import org.freezedry.persistence.tests.Person;
 import org.freezedry.persistence.tree.InfoNode;
 import org.freezedry.persistence.utils.Constants;
 import org.freezedry.persistence.utils.DateUtils;
 import org.freezedry.persistence.utils.ReflectionUtils;
 import org.freezedry.persistence.writers.JsonWriter;
 import org.freezedry.persistence.writers.PersistenceWriter;
 import org.freezedry.persistence.writers.XmlWriter;
 import org.xml.sax.SAXException;
 
 /**
  * Creates a semantic model of the specified {@link Object}, and creates a specified {@link Object} form
  * a specified semantic model. The semantic model is a tree that holds the information about each element 
  * (field) in a {@link InfoNode}. The {@link InfoNode} information is a complete set of information needed 
  * to persist and reconstruct the specified {@link Object}.<p>
  * 
  * The {@link PersistenceEngine} allows you to specify {@link NodeBuilder}s that are responsible for converting
  * an object into the semantic model, and a semantic model back into an object. By default, a set of node builders
  * are created and added to the {@link PersistenceEngine} at construction. However, through the 
  * {@link #addNodeBuilder(Class, NodeBuilder)} method, you can specify which node builder to use for which class.
  * For example, for and instance of the {@link PersistenceEngine}, {@code engine} you can map a {@link Map} to a 
  * {@link MapNodeBuilder}, so that all {@link Map} objects use the {@link MapNodeBuilder} to convert between the 
  * semantic model and an object, by<p>
  * {@code engine.addNodeBuilder( Map.class, new MapNodeBuilder() );}
  * Importantly, in this case, the {@link PersistenceEngine} will use the {@link MapNodeBuilder} for all objects that 
  * implement {@link Map}. That means that a {@link LinkedHashMap} will also use {@link MapNodeBuilder}. Suppose you
  * want to have the {@link LinkedHashMap} used a different {@link NodeBuilder}, say {@code LinkedMapNodeBuilder} (which
  * doesn't exist, but you could write one), then you would perform the mapping as above:<p>
  * {@code engine.addNodeBuilder( LinkedHashMap.class, new LinkedMapNodeBuilder() );}
  * And now, {@link HashMap} would use the {@link MapNodeBuilder}, but {@link LinkedHashMap} would use the 
  * {@code LinkedMapNodeBuilder}.<p>
  * 
  * To customize the behavior of any {@link NodeBuilder} you write, you may also define annotations that works in
  * conjunction with your {@link NodeBuilder}. For example, the {@link PersistCollection} annotation works in 
  * conjunction with the {@link CollectionNodeBuilder}. The field annotation argument {@code elementPersistName}
  * allows you to define the name that is used to persist each element, and the {@code elementType} allows you to define
  * the type ({@link Class}) of each element. Or as another example, the {@link PersistDateAs} annotation allows
  * you to define the format with which a date is persisted.
  * 
  * @see InfoNode
  * @see NodeBuilder
  * @see PersistenceWriter
  * @see PersistenceReader
  * @see Persist
  * 
  * @author Robert Philipp
  */
 public class PersistenceEngine {
 	
 	private static final Logger LOGGER = Logger.getLogger( PersistenceEngine.class );
 	
 	private final Map< Class< ? >, NodeBuilder > nodeBuilders;
 	private NodeBuilder genaralArrayNodeBuilder;
 	
 	/**
 	 * Constructs a {@link PersistenceEngine} with the default {@link InfoNode} info node builders
 	 */
 	public PersistenceEngine()
 	{
 		this.nodeBuilders = createDefaultNodeBuilders();
 		this.genaralArrayNodeBuilder = new ArrayNodeBuilder( this );
 	}
 	
 	/*
 	 * @return a {@link Map} containing the {@link Class}es and their associated {@link InfoNode} {@link Generator}
 	 */
 	private Map< Class< ? >, NodeBuilder > createDefaultNodeBuilders()
 	{
 		// the map holding the class-to-info node builder mapping
 		final Map< Class< ? >, NodeBuilder > builders = new LinkedHashMap<>();
 		
 		// primitive node info node builders (these are intended to create leaf nodes)
 		builders.put( Integer.TYPE, new IntegerNodeBuilder( this ) );
 		builders.put( Double.TYPE, new DoubleNodeBuilder( this ) );
 		builders.put( Long.TYPE, new LongNodeBuilder( this ) );
 		builders.put( Short.TYPE, new ShortNodeBuilder( this ) );
 		builders.put( Boolean.TYPE, new BooleanNodeBuilder( this ) );
 		
 		builders.put( Integer.class, new IntegerNodeBuilder( this ) );
 		builders.put( Double.class, new DoubleNodeBuilder( this ) );
 		builders.put( Long.class, new LongNodeBuilder( this ) );
 		builders.put( Short.class, new ShortNodeBuilder( this ) );
 		builders.put( Boolean.class, new BooleanNodeBuilder( this ) );
 		builders.put( String.class, new StringNodeBuilder( this ) );
 		
 		// other leaf nodes
 		builders.put( Calendar.class, new DateNodeBuilder( this ) );
 		
 		// collection info node builder (specific info node builder)
 		builders.put( Collection.class, new CollectionNodeBuilder( this ) );
 		
 		// map info node builder  (specific info node builder)
 		builders.put( Map.class, new MapNodeBuilder( this ) );
 		
 		// 
 		return builders;
 	}
 	
 	/**
 	 * Adds a {@link NodeBuilder} to be used for generating {@link InfoNode}s for the specified {@link Class}
 	 * @param clazz The {@link Class} of the object to persist and, therefore, for which to generate a node
 	 * @param builder The {@link NodeBuilder} used to generate {@link InfoNode}s for the specified {@link Class}
 	 * @return The {@link NodeBuilder} that used to be associated with the specified {@link Class}; or null if
 	 * no {@link NodeBuilder} was previously associated with the specified {@link Class}
 	 */
 	public NodeBuilder addNodeBuilder( final Class< ? > clazz, final NodeBuilder builder )
 	{
 		return nodeBuilders.put( clazz, builder );
 	}
 	
 	/**
 	 * Finds the {@link NodeBuilder} associated with the class. If the specified class
 	 * doesn't have a info node builder, then it searches for the closest parent class (inheritance)
 	 * and returns true. In this case, it adds an entry to the info node builders map for the
 	 * specified class associating it with the returned info node builder (performance speed-up for
 	 * subsequent calls).
 	 * @param clazz The class for which to find a info node builder
 	 * @return the true if a info node builder was found; false otherwise
 	 */
 	public boolean containsNodeBuilder( final Class< ? > clazz )
 	{
 		return ( getNodeBuilder( clazz ) != null );
 	}
 	
 	/**
 	 * Returns true if the specified field name of the specified {@link Class} has a {@link NodeBuilder} annotation;
 	 * false otherwise. 
 	 * @param clazz The specified {@link Class}
 	 * @param fieldName The field name to check for a {@link NodeBuilder} annotation.
 	 * @return true if the specified field name of the specified {@link Class} has a {@link NodeBuilder} annotation;
 	 * false otherwise.
 	 */
 	public boolean containsAnnotatedNodeBuilder( final Class< ? > clazz, final String fieldName )
 	{
 		boolean contains = false;
 		try
 		{
 			contains = ReflectionUtils.hasNodeBuilderAnnotation( clazz, fieldName );
 		}
 		catch( IllegalArgumentException e ) {}	// swallow the exception
 		
 		return contains;
 	}
 
 	/**
 	 * Finds the {@link NodeBuilder} associated with the class. If the specified class
 	 * doesn't have a info node builder, then it searches for the closest parent class (inheritance)
 	 * and returns that. In this case, it adds an entry to the info node builders map for the
 	 * specified class associating it with the returned info node builder (performance speed-up for
 	 * subsequent calls).
 	 * @param clazz The class for which to find a info node builder
 	 * @return the {@link NodeBuilder} associated with the class
 	 */
 	public NodeBuilder getNodeBuilder( final Class< ? > clazz )
 	{
 		return ReflectionUtils.getItemOrAncestor( clazz, nodeBuilders );
 	}
 
 	/**
 	 * Removes the {@link NodeBuilder} associated with the specified {@link InfoNode}
 	 * @param clazz The {@link Class} of the object to persist for which the associated {@link NodeBuilder}
 	 * will be removed.
 	 * @return The removed {@link NodeBuilder} associated with the specified {@link Class}; if the specified
 	 * {@link Class} was not found in the {@link Map}, then returns null.
 	 */
 	public NodeBuilder removeNodeBuilder( final Class< ? > clazz )
 	{
 		return nodeBuilders.remove( clazz );
 	}
 	
 	/**
 	 * Sets the default {@link NodeBuilder} used for arrays ({@code String[]}, {@code int[]}, {@code Object[]}, etc)
 	 * for objects for which a specific {@link NodeBuilder} hasn't been specified.
 	 * You can add a specific array {@link NodeBuilder} to using {@link #addNodeBuilder(Class, NodeBuilder)} method.
 	 * For example:<p>
 	 * {@code builder.addNodeBuilder( String[].class, new ArrayNodeBuilder() )}<p>
 	 * maps all {@code String[]} objects to use the {@link ArrayNodeBuilder}. 
 	 * @param builder The default {@link NodeBuilder} to use for arrays for which a specific {@link NodeBuilder}
 	 * hasn't been specified.
 	 */
 	public void setGeneralArrayNodeBuilder( final NodeBuilder builder )
 	{
 		this.genaralArrayNodeBuilder = builder;
 	}
 
 	/*
 	 * Searches through the existing node builders to see if they are of the specified {@link Class}.
 	 * If it doesn't find one, then it instantiates a new {@link NodeBuilder} and sets its {@link PersistenceEngine}
 	 * to this object.
 	 * @param nodeBuilderClass The {@link NodeBuilder} {@link Class} to be used for instantiating the object
 	 * @return a {@link NodeBuilder} object associated with the specified node builder {@link Class}
 	 */
 	private NodeBuilder getAnnotatedNodeBuilder( final Class< ? > clazz, final String fieldName )
 	{
 		// grab the node builder class from the field annotation
 		final Class< ? > nodeBuilderClass = ReflectionUtils.getNodeBuilderClass( clazz, fieldName );
 
 		// first we check to see if the node builder has already been constructed, in which case
 		// we use it.
 		NodeBuilder nodeBuilder = null;
 		for( Map.Entry< Class< ? >, NodeBuilder > entry : nodeBuilders.entrySet() )
 		{
 			if( entry.getValue().getClass().equals( nodeBuilderClass ) )
 			{
 				nodeBuilder = entry.getValue();
 				break;
 			}
 		}
 		
 		// if the node builder is still null at this point, then we need to instantiate one.
 		if( nodeBuilder == null )
 		{
 			try
 			{
 				nodeBuilder = (NodeBuilder)nodeBuilderClass.newInstance();
 				nodeBuilder.setPersistenceEngine( this );
 			}
 			catch( InstantiationException | IllegalAccessException e )
 			{
 				final StringBuffer message = new StringBuffer();
 				message.append( "Unable to instantiate the NodeBuilder class." + Constants.NEW_LINE );
 				message.append( "  NodeBuilder Class: " + nodeBuilderClass.getName() );
 				LOGGER.error( message.toString() );
 				throw new IllegalStateException( message.toString(), e );
 			}
 		}
 		return nodeBuilder;
 	}
 	
 	/**
 	 * Creates a semantic model of the specified {@link Object}. The semantic model is a tree
 	 * that holds the information about each element (field) in a {@link InfoNode}. The {@link InfoNode}
 	 * information is a complete set of information needed to persist and reconstruct the 
 	 * specified {@link Object}.
 	 * @param object The object which to convert into a semantic model
 	 * @return The root {@link InfoNode} of the tree representing the semantic model
 	 * @see InfoNode
 	 */
 	public final InfoNode createSemanticModel( final Object object )
 	{
 		// create the root node of the tree, which holds the information about the
 		// object we are being asked to persist.
 		final Class< ? > clazz = object.getClass();
 		final InfoNode rootNode = InfoNode.createRootNode( clazz.getSimpleName(), clazz );
 		
 		// run through the methods building up the semantic model, which is a tree
 		// that holds the information about each element in a node. the node information
 		// is a complete set of information needed to persist and reconstruct an object
 		addNodes( rootNode, object );
 
 		// return the root node of the tree
 		return rootNode;
 	}
 	
 	/*
 	 * Recurses its way down through the objects creating the semantic model. Uses reflection
 	 * and the persistence annotations to construct the model.
 	 * @param currentNode The current {@link InfoNode} in the semantic tree
 	 * @param object The object to convert into a node
 	 * @return The added {@link InfoNode}, which may have child {@link InfoNode}s
 	 */
 	private InfoNode addNodes( final InfoNode currentNode, final Object object )
 	{
 		// run through the fields associated with object's Class< ? >, create the nodes for
 		// each field, and add that node to the current node. recall that this is a recursive
 		// algorithm where createNode(...) may call this method recursively.
 		final Class< ? > clazz = object.getClass();
 		final List< Field > fields = Arrays.asList( clazz.getDeclaredFields() );
 		for( final Field field : fields )
 		{
 			try
 			{
 				// make sure that we can access the field
 				field.setAccessible( true );
 				
 				// create and add the node representing this object to the current node, unless the
 				// node has a null value.
 				final Object fieldObject = field.get( object );
 				if( fieldObject != null )
 				{
 					currentNode.addChild( createNode( clazz, fieldObject, field.getName() ) );
 				}
 			}
 			catch( IllegalAccessException e )
 			{
 				final StringBuffer message = new StringBuffer();
 				message.append( "Attempted to access a field that doesn't exist." );
 				message.append( Constants.NEW_LINE );
 				message.append( "  Object " + clazz.getName() + Constants.NEW_LINE );
 				message.append( "  Field Name: " + field.getName() + Constants.NEW_LINE );
 				LOGGER.error( message.toString(), e );
 			}
 		}
 		return currentNode;
 	}
 	
 	/**
 	 * Creates a {@link InfoNode} for the specified {@link Object}, recursively, calling either {@link #addNodes(InfoNode, Object)}
 	 * or itself to construct {@link InfoNode}s and add them to the tree.
 	 * @param object The object for which to create the node
 	 * @param fieldName The name of the field for which to create the node
 	 * @return the top-level {@link InfoNode} for the object
 	 */
 	public final InfoNode createNode( final Class< ? > containingClass, final Object object, final String fieldName )
 	{
 		// create the InfoNode object (we first have to determine the node type, down the road, we'll check the
 		// factories for registered node info node builders for the Class< ? > of the object)
 		final Class< ? > clazz = object.getClass();
 		
 		// construct the node. There are several cases to consider:
 		// 0. the field annotated with a specified node builder in mind
 		// 1. the object is intended to be a leaf node: create a leaf InfoNode object
 		//    with the appropriately populated values, and we're done.
 		// 2. the object is of a special type: use the appropriate node factory 
 		//    to construct the appropriate node
 		// 3. neither of the first two conditions are met: simply call the addNodes(...)
 		//    method recursively to construct the nodes.
 		// 4. the object is an array, and the array class hasn't been specified in the node 
 		//    builder map, then we must treat it as a generic array.
 		// the first two cases are handled by the info node builders in the info node builders map. even
 		// the leaf node info node builders may need to be overridden. the third case is handled
 		// by the compound node
 		InfoNode node = null;
 		if( containsAnnotatedNodeBuilder( containingClass, fieldName ) )
 		{
 			final NodeBuilder builder = getAnnotatedNodeBuilder( containingClass, fieldName );
 			try
 			{
 				node = builder.createInfoNode( containingClass, object, fieldName );
 			}
 			catch( ReflectiveOperationException e )
 			{
 				final StringBuffer message = new StringBuffer();
 				message.append( "Node Builder failed to create InfoNode:" + Constants.NEW_LINE );
 				message.append( "  Builder: " + builder.getClass().getName() + Constants.NEW_LINE );
 				message.append( "  Containing Class Name: " + containingClass.getName() + Constants.NEW_LINE );
 				message.append( "  Object: " + clazz.getName() + Constants.NEW_LINE );
 				message.append( "  Field Name: " + fieldName + Constants.NEW_LINE );
 				LOGGER.error( message.toString() );
 				throw new IllegalStateException( message.toString(), e );
 			}
 		}
 		else if( containsNodeBuilder( clazz ) )
 		{
 			final NodeBuilder builder = getNodeBuilder( clazz );
 			try
 			{
 				node = builder.createInfoNode( containingClass, object, fieldName );
 			}
 			catch( ReflectiveOperationException e )
 			{
 				final StringBuffer message = new StringBuffer();
 				message.append( "Node Builder failed to create InfoNode:" + Constants.NEW_LINE );
 				message.append( "  Builder: " + builder.getClass().getName() + Constants.NEW_LINE );
 				message.append( "  Containing Class Name: " + containingClass.getName() + Constants.NEW_LINE );
 				message.append( "  Object: " + clazz.getName() + Constants.NEW_LINE );
 				message.append( "  Field Name: " + fieldName + Constants.NEW_LINE );
 				LOGGER.error( message.toString() );
 				throw new IllegalStateException( message.toString(), e );
 			}
 		}
 		else if( clazz.isArray() )
 		{
 			try
 			{
 				node = genaralArrayNodeBuilder.createInfoNode( containingClass, object, fieldName );
 			}
 			catch( ReflectiveOperationException e )
 			{
 				final StringBuffer message = new StringBuffer();
 				message.append( "Default Array Node Builder failed to create InfoNode:" + Constants.NEW_LINE );
 				message.append( "  Builder: " + genaralArrayNodeBuilder.getClass().getName() + Constants.NEW_LINE );
 				message.append( "  Containing Class Name: " + containingClass.getName() + Constants.NEW_LINE );
 				message.append( "  Object: " + clazz.getName() + Constants.NEW_LINE );
 				message.append( "  Field Name: " + fieldName + Constants.NEW_LINE );
 				LOGGER.error( message.toString() );
 				throw new IllegalStateException( message.toString(), e );
 			}
 		}
 		else
 		{
 			// create a new compound node to holds this, since it isn't a leaf node, and
 			// call (recursively) the addNodes(...) method to add the nodes representing the
 			// fields of this object to the newly created compound node.
 			final InfoNode compoundNode = InfoNode.createCompoundNode( fieldName, fieldName, clazz );
 			node = addNodes( compoundNode, object );
 		}
 		
 		// then call addNodes(...) with the newly created node
 		return node;
 	}
 	
 	/**
 	 * Parses the semantic model (a.k.a. content tree) represented by the {@link InfoNode} tree hanging
 	 * off the specified root {@link InfoNode}. The {@link Class} of the returned object is the more specific
 	 * of the specified {@link Class} and the {@link Class} that may be held in the root {@link InfoNode}. 
 	 * The {@link InfoNode}s don't require all the information in each node to be complete. At a minimum
 	 * it needs the field name (or the persist name if that is the same as the field name) and for leaf nodes,
 	 * the value of the node. The {@link PersistenceEngine} will use reflection to fill in missing information.
 	 * However there are limits to what can be obtained by reflection. For example, if the field is a {@link List},
 	 * then the actual concrete {@link Class} type must be available somehow.
 	 * @param clazz The specified {@link Class} of the object to create
 	 * @param rootNode The root {@link InfoNode} representing the semantic model
 	 * @return the object represented by the semantic model
 	 */
 	public Object parseSemanticModel( final Class< ? > clazz, final InfoNode rootNode )
 	{
 		// grab the Class for the root node based on the specified class and the root node
 		final Class< ? > rootClass = ReflectionUtils.getMostSpecificClass( clazz, rootNode );
 		
 		// instantiate the object represented by the root node
 		Object object = null;
 		try
 		{
 			object = rootClass.newInstance();
 		}
 		catch( InstantiationException | IllegalAccessException e )
 		{
 			final StringBuffer message = new StringBuffer();
 			message.append( "Attempted to instantiate object from Class:" + Constants.NEW_LINE );
 			message.append( "  Specified Class Name: " + clazz.getName() + Constants.NEW_LINE );
 			message.append( "  Most Specified Class Name: " + rootClass.getName() + Constants.NEW_LINE );
 			message.append( "  InfoNode: " + rootNode.getClass().getName() + Constants.NEW_LINE );
 			LOGGER.error( message.toString() );
 			throw new IllegalStateException( message.toString(), e );
 		}
 		
 		// calls buildObject
 		buildObject( object, rootNode );
 		
 		return object;
 	}
 	
 	/*
 	 * Builds the specified {@link Object} by setting the fields it contains, using the information in 
 	 * the specified {@link InfoNode}. The fields in the specified {@link Object} are objects that must be 
 	 * instantiated before being set. This method is part of the recursive algorithm that walks down the
 	 * semantic model (a.k.a. content tree) and builds the object. 
 	 * @param object The containing object whose fields to build into objects
 	 * @param currentNode The current {@link InfoNode} in the semantic model.
 	 * @return The specified {@link Object} with all its fields set.
 	 */
 	private Object buildObject( final Object object, final InfoNode currentNode )
 	{
 		// 1. create the object for the specified clazz
 		// 2. create the objects for the fields recursively
 		final Class< ? > clazz = object.getClass();
 		
 		for( InfoNode node : currentNode.getChildren() )
 		{
 			// find the name of the field from the info node
 			final String persistName = node.getPersistName();
 			String fieldName = node.getFieldName();
 			if( fieldName == null || fieldName.isEmpty() )
 			{
 				final Field field = ReflectionUtils.getFieldForPersistenceName( clazz, persistName );
 				if( field != null )
 				{
 					fieldName = field.getName();
 				}
 				else
 				{
 					fieldName = persistName;
 				}
 			}
 
 			// grab the class' field
 			try
 			{
 				final Field field = clazz.getDeclaredField( fieldName );
 				
 				// grab the generic parameter type of the field and add it to the info node
 				final Type type = field.getGenericType();
 				if( type instanceof ParameterizedType )
 				{
 					final List< Type > types = Arrays.asList( ((ParameterizedType)type).getActualTypeArguments() );
 					node.setGenericParameterTypes( types );
 				}
 				
 				// see if the field has a @Persist( instantiateAs = XXXX.class ) annotation
 				final Persist annotation = field.getAnnotation( Persist.class );
 				if( annotation != null )
 				{
 					// if no class information is stored in the node, or if the class stored in the
 					// node is a super class of the instantiate type, then use the instantiate type
 					final Class< ? > instantiateType = annotation.instantiateAs();
 					if( !instantiateType.equals( Persist.Null.class ) )
 					{
 						final Class< ? > nodeClazz = node.getClazz();
						if( nodeClazz == null || ReflectionUtils.isSuperclass( nodeClazz, instantiateType ) )
 						{
 							node.setClazz( instantiateType );
 						}
 					}
 				}
 	
 				// create the object
 				final Class< ? > newClass = ReflectionUtils.getMostSpecificClass( field.getType(), node );
 				final Class< ? > containingClass = field.getDeclaringClass();
 				final Object newObject = createObject( containingClass, newClass, node );
 	
 				// set the field to be accessible (override the accessor)
 				field.setAccessible( true );
 	
 				// set the fields value
 				field.set( object, newObject );
 			}
 			catch( NoSuchFieldException e )
 			{
 				final StringBuffer message = new StringBuffer();
 				message.append( "Attempted to retrieve field for an invalid field name:" + Constants.NEW_LINE );
 				message.append( "  Field Name: " + fieldName + Constants.NEW_LINE );
 				message.append( "  Containing Class: " + object.getClass().getName() + Constants.NEW_LINE );
 				LOGGER.error( message.toString() );
 				throw new IllegalStateException( message.toString(), e );
 			}
 			catch( IllegalAccessException e )
 			{
 				final StringBuffer message = new StringBuffer();
 				message.append( "Attempted to perform an invalid operation on field:" + Constants.NEW_LINE );
 				message.append( "  Field Name: " + fieldName + Constants.NEW_LINE );
 				message.append( "  Containing Class: " + object.getClass().getName() + Constants.NEW_LINE );
 				LOGGER.error( message.toString() );
 				throw new IllegalStateException( message.toString(), e );
 			}
 		}
 		
 		return object;
 	}
 	
 	/**
 	 * Creates and returns the object represented by the specified {@link InfoNode} and {@link Class}. The method
 	 * chooses the most specific type (i.e. farthest down on the inheritance hierarchy) found in the {@link InfoNode}
 	 * or the {@link Class}. The specified {@link InfoNode} doesn't need to hold the type information, if such 
 	 * information is not available.
 	 * @param clazz The {@link Class} type of the object to create (unless the one found in the {@link InfoNode} is
 	 * more specific).
 	 * @param currentNode The {@link InfoNode} containing information about the object to be created
 	 * @return The newly minted object
 	 */
 	public Object createObject( final Class< ? > containingClass, final Class< ? > clazz, final InfoNode currentNode )
 	{
 		// grab the field name from the node
 		String fieldName = currentNode.getFieldName();
 		if( fieldName == null )
 		{
 			final String persistName = currentNode.getPersistName();
 			fieldName = ReflectionUtils.getFieldNameForPersistenceName( containingClass, persistName );
 			if( fieldName == null )
 			{
 				fieldName = persistName;
 			}
 		}
 		
 		// find the node builder need to create the object. if there is
 		// no node builder, then instantiate the object and make a recursive call
 		// to the buildObject(...) method.
 		Object object = null;
 		if( containsAnnotatedNodeBuilder( containingClass, fieldName ) )
 		{
 			final NodeBuilder builder = getAnnotatedNodeBuilder( containingClass, fieldName );
 			try
 			{
 				object = builder.createObject( containingClass, clazz, currentNode );
 			}
 			catch( ReflectiveOperationException e )
 			{
 				final StringBuffer message = new StringBuffer();
 				message.append( "Node Builder failed to create object from Class and InfoNode:" + Constants.NEW_LINE );
 				message.append( "  Class Name: " + clazz.getName() + Constants.NEW_LINE );
 				message.append( "  Builder: " + builder.getClass().getName() + Constants.NEW_LINE );
 				message.append( "  InfoNode: " + currentNode.toString() + Constants.NEW_LINE );
 				LOGGER.error( message.toString() );
 				throw new IllegalStateException( message.toString(), e );
 			}
 		}
 		else if( containsNodeBuilder( clazz ) )
 		{
 			final NodeBuilder builder = getNodeBuilder( clazz );
 			try
 			{
 				object = builder.createObject( containingClass, clazz, currentNode );
 			}
 			catch( ReflectiveOperationException e )
 			{
 				final StringBuffer message = new StringBuffer();
 				message.append( "Node Builder failed to create object from Class and InfoNode:" + Constants.NEW_LINE );
 				message.append( "  Class Name: " + clazz.getName() + Constants.NEW_LINE );
 				message.append( "  Builder: " + builder.getClass().getName() + Constants.NEW_LINE );
 				message.append( "  InfoNode: " + currentNode.toString() + Constants.NEW_LINE );
 				LOGGER.error( message.toString() );
 				throw new IllegalStateException( message.toString(), e );
 			}
 		}
 		else if( clazz.isArray() )
 		{
 			try
 			{
 				object = genaralArrayNodeBuilder.createObject( containingClass, clazz, currentNode );
 			}
 			catch( ReflectiveOperationException e )
 			{
 				final StringBuffer message = new StringBuffer();
 				message.append( "Default Array Node Builder failed to create object from Class and InfoNode:" + Constants.NEW_LINE );
 				message.append( "  Class Name: " + clazz.getName() + Constants.NEW_LINE );
 				message.append( "  Builder: " + genaralArrayNodeBuilder.getClass().getName() + Constants.NEW_LINE );
 				message.append( "  InfoNode: " + currentNode.toString() + Constants.NEW_LINE );
 				LOGGER.error( message.toString() );
 				throw new IllegalStateException( message.toString(), e );
 			}
 		}
 		else
 		{
 			try
 			{
 				// create the new object and then build it recursively
 				final Object newObject = clazz.newInstance();
 				object = buildObject( newObject, currentNode );
 			}
 			catch( InstantiationException | IllegalAccessException e )
 			{
 				final StringBuffer message = new StringBuffer();
 				message.append( "Attempted to instantiate object from Class:" + Constants.NEW_LINE );
 				message.append( "  Class Name: " + clazz.getName() + Constants.NEW_LINE );
 				LOGGER.error( message.toString() );
 				throw new IllegalStateException( message.toString(), e );
 			}
 		}
 		return object;
 	}
 	
 	/**
 	 * 
 	 * @param args
 	 * @throws ReflectiveOperationException
 	 * @throws ParserConfigurationException
 	 * @throws IOException
 	 * @throws SAXException
 	 */
 	public static void main( String[] args )
 	{
 		try
 		{
 			DOMConfigurator.configure( "log4j.xml" );
 	
 			// create the object to persist
 			final Division division = new Division();
 			final Person johnny = new Person( "Hernandez", "Johnny", 13 );
 			johnny.addFriend( "Polly", "bird" );
 			johnny.addFriend( "Sparky", "dog" );
 			for( int i = 0; i < 10; ++i )
 			{
 				johnny.addMood( Math.sin( Math.PI / 4 * i ) );
 			}
 			Map< String, String > group = new LinkedHashMap<>();
 			group.put( "one", "ONE" );
 			group.put( "two", "TWO" );
 			group.put( "three", "THREE" );
 			johnny.addGroup( "numbers", group );
 	
 			group = new LinkedHashMap<>();
 			group.put( "a", "AY" );
 			group.put( "b", "BEE" );
 			johnny.addGroup( "letters", group );
 			
 			johnny.setBirthdate( DateUtils.createDateFromString( "1963-04-22", "yyyy-MM-dd" ) );
 			
 			division.addPerson( johnny );
 	
 			division.addPerson( new Person( "Prosky", "Julie", 15 ) );
 			division.addPerson( new Person( "Jones", "Janet", 13 ) );
 			division.addPerson( new Person( "Ghad", "Booda", 17 ) );
 			
 			division.addMonth( "January", new HashSet<>( Arrays.asList( 1, 2, 3, 31 ) ) );
 			division.addMonth( "February", new HashSet<>( Arrays.asList( 1, 2, 3, 28 ) ) );
 			division.addMonth( "March", new HashSet<>( Arrays.asList( 1, 2, 3, 31 ) ) );
 			division.addMonth( "April", new HashSet<>( Arrays.asList( 1, 2, 3, 30 ) ) );
 			
 			division.setCarNames( new String[] { "civic", "tsx", "accord" } );
 	
 //			int[][] arrayMatrix = { { 11, 12, 13 }, { 21, 22, 23 }, { 31, 32, 33 } };
 //			division.setArrayMatrix( arrayMatrix );
 			
 			List< List< Integer > > collectionMatrix = Arrays.asList( 
 					Arrays.asList( 11, 12, 13 ), 
 					Arrays.asList( 21, 22, 23 ), 
 					Arrays.asList( 31, 32, 33 ) );
 			division.setCollectionMatrix( collectionMatrix );
 			
 			Map< String, Person > personMap = new LinkedHashMap<>();
 			personMap.put( "funny", new Person( "Richard", "Pryor", 63 ) );
 			personMap.put( "sad", new Person( "Jenny", "Jones", 45 ) );
 			personMap.put( "pretty", new Person( "Ginder", "Mendez", 23 ) );
 			division.setPersonMap( personMap );
 			
 			// create the persistence engine that is used to create the semantic model from an object
 			// and used to create an object from the semantic model.
 			final PersistenceEngine engine = new PersistenceEngine();
 			// two ways to override the output behavior programmatically
 //			// override the default date setting and replace the date node builder with the new one 
 //			final DateNodeBuilder dateNodeBuilder = new DateNodeBuilder( engine, "yyyy-MM-dd" );
 //			engine.addNodeBuilder( Calendar.class, dateNodeBuilder );
 			// -- OR --
 //			((DateNodeBuilder)engine.getNodeBuilder( Calendar.class )).setOutputDateFormat( "yyyy-MM-dd" );
 			
 			// create the semantic model
 			final InfoNode rootNode = engine.createSemanticModel( division );
 			System.out.println( rootNode.simpleTreeToString() );
 //			System.out.println( "\n\nCopied Tree\n" );
 //			System.out.println( rootNode.getCopy().simpleTreeToString() );
 	
 			// write out XML
 			try( final PrintWriter printWriter = new PrintWriter( new FileWriter( "person.xml" ) ) )
 			{
 				final XmlWriter writer = new XmlWriter();
 				writer.setDisplayTypeInfo( false );
 				writer.write( rootNode, printWriter );
 			}
 			catch( IOException e )
 			{
 				e.printStackTrace();
 			}
 			try( final PrintWriter printWriter = new PrintWriter( new FileWriter( "person2.xml" ) ) )
 			{
 				final XmlWriter writer = new XmlWriter();
 				writer.setDisplayTypeInfo( false );
 				writer.write( rootNode.getCopy(), printWriter );
 			}
 			catch( IOException e )
 			{
 				e.printStackTrace();
 			}
 			
 			
 			// write out JSON
 			try( final PrintWriter printWriter = new PrintWriter( new FileWriter( "person.json" ) ) )
 			{
 				final JsonWriter writer = new JsonWriter();
 				writer.write( rootNode, printWriter );
 			}
 			catch( IOException e )
 			{
 				e.printStackTrace();
 			}
 			
 			// read in XML
 			final XmlReader reader = new XmlReader();
 	//		reader.setRemoveEmptyTextNodes( false );
 			final InputStream inputStream = new BufferedInputStream( new FileInputStream( "person.xml" ) );
 			final Reader input = new InputStreamReader( inputStream );
 			final InfoNode infoNode = reader.read( Division.class, input );
 			System.out.println( infoNode.simpleTreeToString() );
 			
 			final Object redivision = engine.parseSemanticModel( Division.class, infoNode );
 			System.out.println( redivision );
 
 			// read in JSON 
 			final InputStream jsonInputStream = new BufferedInputStream( new FileInputStream( "person.json" ) );
 			final Reader jsonInput = new InputStreamReader( jsonInputStream );
 			final JsonReader jsonReader = new JsonReader();
 			final InfoNode jsonInfoNode = jsonReader.read( Division.class, jsonInput );
 			System.out.println( jsonInfoNode.simpleTreeToString() );
 			
 			final Object reperson = engine.parseSemanticModel( Division.class, jsonInfoNode );
 			System.out.println( reperson );
 		}
 		catch( Exception e )
 		{
 			e.printStackTrace();
 		}
 	}
 }
