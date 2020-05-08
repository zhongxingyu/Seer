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
 package org.freezedry.persistence.keyvalue.renderers;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 import org.freezedry.persistence.annotations.PersistMap;
 import org.freezedry.persistence.containers.Pair;
 import org.freezedry.persistence.keyvalue.KeyValueBuilder;
 import org.freezedry.persistence.keyvalue.renderers.decorators.StringDecorator;
 import org.freezedry.persistence.tree.InfoNode;
 import org.freezedry.persistence.utils.Constants;
 import org.freezedry.persistence.utils.ReflectionUtils;
 
 /**
  * Renders the subtree of the semantic model that represents a {@link Map}. {@link Map}s are rendered
  * as the map's persistence name followed by the decorated key. For example, a {@code Map< String, Double >}
  * would be persisted in the following format when using the default decorator and settings:
  * <code><pre>
  * Person.friends{"Polly"} = "bird"
  * Person.friends{"Sparky"} = "dog"
  * </pre></code>
  * or for a more complicated map such as {@code Map< String, Map< String, String > >}:
  * <code><pre>
  * Person.groups{"numbers"}{"one"} = "ONE"
  * Person.groups{"numbers"}{"two"} = "TWO"
  * Person.groups{"numbers"}{"three"} = "THREE"
  * Person.groups{"letters"}{"a"} = "AY"
  * Person.groups{"letters"}{"b"} = "BEE"
  * </pre></code>
  * 
  * @author Robert Philipp
  */
 public class MapRenderer extends AbstractPersistenceRenderer {
 
 	private static final Logger LOGGER = Logger.getLogger( MapRenderer.class );
 
 	private static String OPEN = "{";
 	private static String CLOSE = "}";
 	
 	private String mapEntryName = PersistMap.ENTRY_PERSIST_NAME;
 	private String mapKeyName = PersistMap.KEY_PERSIST_NAME;
 	private String mapValueName = PersistMap.VALUE_PERSIST_NAME;
 	
 	private final StringDecorator keyDecorator;
 	private final String decorationRegex;
 	private final Pattern decorationPattern;
 	private final String validationRegex;
 	private final Pattern validationPattern;
 
 	/**
 	 * Constructs a key-value {@link MapRenderer} for renderering the key-values for a {@link Map}
 	 * @param builder The {@link KeyValueBuilder} that makes calls to this renderer. Recall that this
 	 * is part of a recursive algorithm.
 	 * @param keyDecorator The decorator for the key. For example, it may surround the key with "<code>{</code>"
 	 * and "<code>}</code>". 
 	 */
 	public MapRenderer( final KeyValueBuilder builder, final String openKey, final String closeKey )
 	{
 		super( builder );
 		
 		// create the decorator for the key
 		this.keyDecorator = new StringDecorator( openKey, closeKey );
 		
 		// create the regular expression that determines if a string is renderered by this class
 		// people[0] is a collection, but people{"test"}[0] is a map< string, list< integer > >
 		// so we want to check that only word characters precede the "["
 		final String open = Pattern.quote( openKey );
 		final String close = Pattern.quote( closeKey );
 		
 		// create and compile the regex pattern for the decoration
 		decorationRegex = open + "\\p{Punct}?\\w+\\p{Punct}?" + close;
 		decorationPattern = Pattern.compile( decorationRegex );
 		
 		// create and compile the regex pattern for validating the complete key
 		validationRegex = "\\w+" + decorationRegex + "|^" + decorationRegex;
 		validationPattern = Pattern.compile( validationRegex );
 	}
 
 	/**
 	 * Constructs a key-value {@link MapRenderer} for renderering the key-values for a {@link Map}.
 	 * Decorates the key with the default decorations.
 	 * @param builder The {@link KeyValueBuilder} that makes calls to this renderer. Recall that this
 	 * is part of a recursive algorithm.
 	 */
 	public MapRenderer( final KeyValueBuilder builder )
 	{
 		this( builder, OPEN, CLOSE );
 	}
 	
 	/**
 	 * Copy constructor
 	 * @param renderer The {@link MapRenderer} to copy
 	 */
 	public MapRenderer( final MapRenderer renderer )
 	{
 		super( renderer );
 		
 		this.keyDecorator = renderer.keyDecorator.getCopy();
 		this.decorationRegex = renderer.decorationRegex;
 		this.decorationPattern = renderer.decorationPattern;
 		this.validationRegex = renderer.validationRegex;
 		this.validationPattern = renderer.validationPattern;
 	}
 
 	/**
 	 * @param name the persistence name in the {@link InfoNode}s representing {@link Map.Entry}
 	 * The default value is {@link PersistMap.ENTRY_PERSIST_NAME} 
 	 */
 	public void setMapEntryName( final String name )
 	{
 		this.mapEntryName = name;
 	}
 	
 	/**
 	 * @return the persistence name in the {@link InfoNode}s representing {@link Map.Entry}
 	 */
 	public String getMapEntryName()
 	{
 		return mapEntryName;
 	}
 
 	/**
 	 * @param name the persistence name in the {@link InfoNode}s representing {@link Map} keys
 	 * The default value is {@link PersistMap.KEY_PERSIST_NAME}
 	 */
 	public void setMapKeyName( final String name )
 	{
 		this.mapKeyName = name;
 	}
 
 	/**
 	 * @return the persistence name in the {@link InfoNode}s representing {@link Map} keys
 	 */
 	public String getMapKeyName()
 	{
 		return mapKeyName;
 	}
 
 	/**
 	 * @param name the persistence name in the {@link InfoNode}s representing {@link Map} values
 	 * The default value is {@link PersistMap.VALUE_PERSIST_NAME}
 	 */
 	public void setMapValueName( final String name )
 	{
 		this.mapValueName = name;
 	}
 
 	/**
 	 * @return the persistence name in the {@link InfoNode}s representing {@link Map} values
 	 */
 	public String getMapValueName()
 	{
 		return mapValueName;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer#buildKeyValuePair(org.freezedry.persistence.tree.InfoNode, java.lang.String, java.util.List)
 	 */
 	@Override
 	public void buildKeyValuePair( final InfoNode infoNode, 
 								   final String key, 
 								   final List< Pair< String, Object > > keyValues, 
 								   final boolean isWithholdPersistName )
 	{
 		// [Division:months{January}[0], 1]
 		// [Division:months{January}[1], 2]
 		// [Division:systems{ALM}, "Investments and Capital Markets Division"]
 		// [Division:systems{SAP}, "Single Family Division"]
 		for( InfoNode node : infoNode.getChildren() )
 		{
 			// the node should be a MapEntry class, if not, then we've got problems, which
 			// we will not hesitate to report to the proper authorities.
			if( ReflectionUtils.isSuperclass( Map.Entry.class, node.getClazz() ) )
 			{
 				// there should be two nodes hanging off the MapEntry: the key and the value.
 				// each of these may have their own subnodes.
 				final List< InfoNode > entryNodes = node.getChildren();
 				if( entryNodes.size() != 2 )
 				{
 					final StringBuffer message = new StringBuffer();
 					message.append( "The MapRenderer expects MapEntry nodes to have exactly 2 subnodes." + Constants.NEW_LINE );
 					message.append( "  Number of subnodes: " + entryNodes.size() + Constants.NEW_LINE );
 					message.append( "  Persist Name: " + node.getPersistName() );
 					LOGGER.error( message.toString() );
 					throw new IllegalStateException( message.toString() );
 				}
 				
 				// find the info node that holds the key, and the info node that holds the value
 				InfoNode keyNode = null;
 				InfoNode valueNode = null;
 				final String name1 = entryNodes.get( 0 ).getPersistName(); 
 				final String name2 = entryNodes.get( 1 ).getPersistName();
 				if( name1.equals( mapKeyName ) && name2.equals( mapValueName ) )
 				{
 					keyNode = entryNodes.get( 0 );
 					valueNode = entryNodes.get( 1 );
 				}
 				else if( name2.equals( mapKeyName ) && name1.equals( mapValueName ) )
 				{
 					keyNode = entryNodes.get( 1 );
 					valueNode = entryNodes.get( 0 );
 				}
 				else
 				{
 					final StringBuffer message = new StringBuffer();
 					message.append( "The MapRenderer expects MapEntry nodes to have a key subnode and a value subnode." + Constants.NEW_LINE );
 					message.append( "  Required Key Subnode Persist Name: " + mapKeyName + Constants.NEW_LINE );
 					message.append( "  Required Value Subnode Persist Name: " + mapValueName + Constants.NEW_LINE );
 					message.append( "  First Node's Persist Name: " + name1 + Constants.NEW_LINE );
 					message.append( "  Second Node's Persist Name: " + name2 );
 					LOGGER.error( message.toString() );
 					throw new IllegalStateException( message.toString() );
 				}
 				
 				// no we can continue to parse the nodes.
 				String newKey = createKey( key, infoNode );
 				if( keyNode.isLeafNode() )
 				{
 					String value;
 					final Object object = keyNode.getValue();
 					final Class< ? > clazz = object.getClass();
 					if( containsDecorator( clazz ) )
 					{
 						value = getDecorator( clazz ).decorate( object );
 					}
 					else
 					{
 						value = object.toString();
 					}
 					newKey += keyDecorator.decorate( value );
 				}
 				else
 				{
 					// TODO currently we have a slight problem here for compound keys.
 					final StringBuffer message = new StringBuffer();
 					message.append( "The MapRenderer doesn't allow compound (composite) keys at this point." + Constants.NEW_LINE );
 					message.append( "  Current Key: " + newKey + Constants.NEW_LINE );
 					LOGGER.error( message.toString() );
 					throw new IllegalStateException( message.toString() );
 				}
 				
 				// create the key-value pair and return it. we know that we have value node, and that
 				// the persistence name of the value is "Value" or something else set by the user. we
 				// don't want to write that out, so we simply remove the value from the node.
 				valueNode.setPersistName( "" );
 				getPersistenceBuilder().createKeyValuePairs( valueNode, newKey, keyValues, true );
 				
 				// mark the node as processed so that it doesn't get processed again
 				node.setIsProcessed( true );
 			}
 			else
 			{
 				final StringBuffer message = new StringBuffer();
 				message.append( "The MapRenderer expects the root node of the map to have only subnodes of type MapEntry." + Constants.NEW_LINE );
 				message.append( "  InfoNode Type: " + (node.getClazz() == null ? "[null]" : node.getClazz().getName()) + Constants.NEW_LINE );
 				message.append( "  Persist Name: " + node.getPersistName() );
 				LOGGER.error( message.toString() );
 				throw new IllegalStateException( message.toString() );
 			}
 		}
 	}
 
 	/*
 	 * Creates a new key based on the specified key and the persistence name found in the info node
 	 * @param key The current key
 	 * @param node The {@link InfoNode}
 	 * @return a new key based on the specified key and the persistence name found in the info node
 	 */
 	private String createKey( final String key, final InfoNode node )
 	{
 		String newKey = key;
 		if( node.getPersistName() != null && !node.getPersistName().isEmpty() )
 		{
 			newKey += getPersistenceBuilder().getSeparator() + node.getPersistName();
 		}
 		return newKey;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer#buildInfoNode(org.freezedry.persistence.tree.InfoNode, java.util.List)
 	 */
 	@Override
 	public void buildInfoNode( final InfoNode parentNode, final List< Pair< String, String > > keyValues )
 	{
 		// nothing to do
 		if( keyValues == null || keyValues.isEmpty() )
 		{
 			return;
 		}
 		
 		// grab the group name for the map, and create the compound node
 		// that holds the map entries of the map as child nodes, and add it
 		// to the parent node
 		final String group = getGroupName( keyValues.get( 0 ).getFirst() );
 		final InfoNode mapNode = InfoNode.createCompoundNode( null, group, null );
 		parentNode.addChild( mapNode );
 		
 		// construct the patterns to determine if the node should be a compound node,
 		// in which case we recurse back to the builder, or a leaf node, in which case
 		// we simply create it here and add it to the collection node
 		final String compoundRegex = "^" + group + decorationRegex;
 		final Pattern compoundPattern = Pattern.compile( compoundRegex );
 		
 		final String leafRegex = compoundRegex + "$";
 		final Pattern leafPattern = Pattern.compile( leafRegex );
 		
 		// we want to have groups by the map key. the map key is the map key in the key-value pair that.
 		// for example, in friends{"Polly"}, the map key is "Polly" (including the quotes). then we
 		// can parse each group into its proper node.
 		final Map< String, List< Pair< String, String > > > mapKeyGroups = getMapKeyGroups( keyValues );
 		for( Map.Entry< String, List< Pair< String, String > > > entry : mapKeyGroups.entrySet() )
 		{
 			// for each group, i.e. each key, we need a map entry node attached to the map node.
 			final InfoNode mapEntryNode = InfoNode.createCompoundNode( null, mapEntryName, null );
 			mapNode.addChild( mapEntryNode );
 
 			// grab the map key and the list of key-values associated with that map key
 			final String mapKey = entry.getKey();
 			
 			// run through the list of key-values creating the child nodes for the map-entry node
 			final List< Pair< String, String > > keyValueGroup = entry.getValue();
 			final List< Pair< String, String > > copiedKeyValues = new ArrayList<>( keyValueGroup );
 			for( Pair< String, String > keyValue : keyValueGroup )
 			{
 				// check to see if any items have been removed from the list. this could happen
 				// when there is a compound node that we have combined, and removed all the entries
 				// from this list
 				if( !copiedKeyValues.contains( keyValue ) )
 				{
 					continue;
 				}
 
 				// grab the key
 				final String key = keyValue.getFirst();
 				
 				// we must figure out whether this is a compound node or a leaf node
 				final Matcher compoundMatcher = compoundPattern.matcher( key );
 				final Matcher leafMatcher = leafPattern.matcher( key );
 				if( leafMatcher.find() )
 				{
 					// its a leaf, create the key node
 					final String rawMapKey = getDecorator( mapKey ).undecorate( mapKey );
 					final InfoNode keyNode = InfoNode.createLeafNode( null, rawMapKey, mapKeyName, null );
 					mapEntryNode.addChild( keyNode );
 					
 					// so now we need to figure out what the value is. we know that
 					// it must be a number (integer, double) or a string.
 					final String value = keyValue.getSecond();
 					final String rawValue = getDecorator( value ).undecorate( value );
 					
 					// create the leaf info node and add it to the collection node
 					final InfoNode valueNode = InfoNode.createLeafNode( null, rawValue, mapValueName, null );
 					mapEntryNode.addChild( valueNode );
 				}
 				else if( compoundMatcher.find() )
 				{
 					// its a compound node, create the key node
 					final String rawMapKey = getDecorator( mapKey ).undecorate( mapKey );
 					final InfoNode keyNode = InfoNode.createLeafNode( null, rawMapKey, mapKeyName, null );
 					mapEntryNode.addChild( keyNode );
 					
 					// in this case, we'll have several entries that have the same index, so
 					// we'll need to pull those out and put them into a new key-value list
 					final String separator = getPersistenceBuilder().getSeparator();
 //					final String valueName = mapEntryName + separator + mapValueName;
 					final List< Pair< String, String > > mapValueKeyValues = new ArrayList<>();
 					for( Pair< String, String > copiedKeyValue : keyValues )
 					{
 						final String copiedKey = copiedKeyValue.getFirst();
 						final String keyFirstElement = extractMapKeyPart( key.split( Pattern.quote( separator ) )[ 0 ] ); 
 						if( copiedKey.startsWith( keyFirstElement ) )
 						{
 							// strip the first element off the key. this could mean one of three things:
 							// 1. for something like months{"April"}[1] we remove all but the [1]
 							// 2. for something like months{"April"}.Date we remove all but the Date
 							// 3. for something like months{"April"}[1].Mood we remove all but [1].Mood
 							final String strippedKey = mapValueName + stripFirstElement( copiedKey, separator );
 //							final String strippedKey = valueName + stripFirstElement( copiedKey, separator );
 							
 							// add the key to the list of keys that belong to the compound node
 							mapValueKeyValues.add( new Pair< String, String >( strippedKey, copiedKeyValue.getSecond() ) );
 							
 							// and remove the element from the list of key values
 							copiedKeyValues.remove( copiedKeyValue );
 						}
 					}
 					
 //					final InfoNode valueNode = InfoNode.createCompoundNode( null, mapValueName, null );
 //					mapEntryNode.addChild( valueNode );
 
 					// call the builder (which called this method) to build the compound node
 					getPersistenceBuilder().createInfoNode( mapEntryNode, mapValueName, mapValueKeyValues );
 //					getPersistenceBuilder().createInfoNode( valueNode, "", mapValueKeyValues );
 				}
 				else
 				{
 					// error
 					final StringBuffer message = new StringBuffer();
 					message.append( "The key neither represents a leaf node nor a compound node. This is a real problem!" + Constants.NEW_LINE );
 					message.append( "  Parent Node Persistence Name: " + parentNode.getPersistName() + Constants.NEW_LINE );
 					message.append( "  Key: " + key + Constants.NEW_LINE );
 					LOGGER.error( message.toString() );
 					throw new IllegalArgumentException( message.toString() );
 				}
 			}
 		}
 	}
 	
 	/*
 	 * Extracts the map keys and creates a {@link Map} that has as its keys these map keys. The values are the
 	 * key-value pairs associated with each of these map keys. For leaf nodes, the list of key-value pairs will
 	 * be of size one. For compound nodes, the list will have a size greater than or equal to one.
 	 * @param keyValues The list of key values for the group.
 	 * @return a {@link Map} whose keys are the map keys in the list of key-value pairs
 	 */
 	private Map< String, List< Pair< String, String > > > getMapKeyGroups( final List< Pair< String, String > > keyValues )
 	{
 		final Map< String, List< Pair< String, String > > > keyGroups = new LinkedHashMap<>();
 		for( Pair< String, String > keyValue : keyValues )
 		{
 			final String mapKey = extractMapKey( keyValue.getFirst() );
 			if( keyGroups.containsKey( mapKey ) )
 			{
 				keyGroups.get( mapKey ).add( keyValue );
 			}
 			else
 			{
 				final List< Pair< String, String > > keyValueList = new ArrayList<>();//Arrays.asList( keyValue );
 				keyValueList.add( keyValue );
 				keyGroups.put( mapKey, keyValueList );
 			}
 		}
 		return keyGroups;
 	}
 	
 	/*
 	 * Extracts the map key from the key. For example, if the key is <code>months{"January"}[0]</code>, then this method
 	 * will return the {@link String} <code>"January"</code> (including the quotes). Or, for example, if the key is <code>id{234}</code>
 	 * this method will return the {@link String} <code>234</code>.
 	 * @param key The key from which to extract the map key
 	 * @return the map key as a {@link String}.
 	 */
 	private String extractMapKey( final String key )
 	{
 		String mapKey = null;
 		final Matcher matcher = decorationPattern.matcher( key );
 		if( matcher.find() )
 		{
 			mapKey = keyDecorator.undecorate( key.substring( matcher.start(), matcher.end() ) );
 		}
 		return mapKey;
 	}
 	
 	/*
 	 * Extracts the map key part from the key. For example, if the key is <code>months{"January"}[0]</code>
 	 * this method will return <code>months{"January"}</code>.
 	 * @param key The key from which to extract the map-key part
 	 * @return the map key part from the key
 	 */
 	private String extractMapKeyPart( final String key )
 	{
 		String mapKey = null;
 		final Matcher matcher = decorationPattern.matcher( key );
 		if( matcher.find() )
 		{
 			mapKey = key.substring( 0, matcher.end() );
 		}
 		return mapKey;
 	}
 	
 	/*
 	 * Removes the map key part from the key. For example, if the key is <code>months{"January"}[0]</code>
 	 * this method will return <code>[0]</code>.
 	 * @param key The key from which to strip the map-key part
 	 * @return the key, stripped of the map key part
 	 */
 	private String removeMapKeyPart( final String key )
 	{
 		String keyRemainder = null;
 		final Matcher matcher = decorationPattern.matcher( key );
 		if( matcher.find() )
 		{
 			keyRemainder = key.substring( matcher.end() );
 		}
 		return keyRemainder;
 	}
 	
 	private String stripFirstElement( final String key, final String separator )
 	{
 		final String remainder = removeMapKeyPart( key );
 		if( remainder.startsWith( separator ) )
 		{
 			remainder.replaceAll( "^" + Pattern.quote( separator ), "" );
 		}
 		return remainder;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer#isRenderer(java.lang.String)
 	 */
 	@Override
 	public boolean isRenderer( String keyElement )
 	{
 		return validationPattern.matcher( keyElement ).find();
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer#getGroupName(java.lang.String)
 	 */
 	@Override
 	public String getGroupName( final String key )
 	{
 		final Matcher matcher = decorationPattern.matcher( key );
 		String group = null;
 		if( matcher.find() )
 		{
 			group = key.substring( 0, matcher.start() );
 		}
 		return group;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.freezedry.persistence.copyable.Copyable#getCopy()
 	 */
 	@Override
 	public MapRenderer getCopy()
 	{
 		return new MapRenderer( this );
 	}
 }
