 package org.microtitan.diffusive.diffuser.restful;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.abdera.model.Feed;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
 import org.freezedry.persistence.copyable.Copyable;
 import org.microtitan.diffusive.Constants;
 import org.microtitan.diffusive.diffuser.Diffuser;
 import org.microtitan.diffusive.diffuser.restful.atom.Atom;
 import org.microtitan.diffusive.utils.ReflectionUtils;
 
 /**
  * Encapsulates the key used to identify a {@link Diffuser} for a specific method. The {@link Diffuser} ID
  * is based on:
  * <ul>
  * 	<li>the name of the {@link Class} that contains the diffused method</li>
  * 	<li>the name of the method</li>
  * 	<li>a list of the class names for each of the arguments passed to the method</li>
  * 	<li>the {@link Class} name of the return type</li>
  * </ul>
  * The {@link Diffuser} ID is constructed as follows:<p>
  * {@code class:method(argument1,argument2,argument3,...,argumentN)-returnType}<p> 
  * where the "{@code class}" is the fully qualified class name; the method is the name of the method;
  * the arguments are all the fully qualified class names of the argument; and the {@code returnType}
  * is the fully qualified class name of the return type (which could be {@code void.class}.
  * 
  * The {@link DiffuserSignature} can be instantiated through the constructors, or preferably, through one of the
  * four {@code create(...)} methods, or the {@link #parse(String)} method.
  * 
  * The {@link DiffuserSignature} objects are immutable.
  * 
  * @author Robert Philipp
  */
 public class DiffuserSignature implements Copyable< DiffuserSignature > {
 	
 	private static final Logger LOGGER = Logger.getLogger( DiffuserSignature.class );
 	
 	// signature punctuation
 	public static final String CLASS_METHOD_SEPARATOR = ":";
 	public static final String ARGUMENT_SEPARATOR = ",";
 	public static final String ARGUMENT_OPEN = "(";
 	public static final String ARGUMENT_CLOSE = ")";
 	public static final String RETURN_TYPE_SEPARATOR = "-";
 	public static final String ENCODED_ARRAY_IDENTIFIER = ";";
 	public static final String ARRAY_IDENTIFIER = "[";
 	
 	// regular expression for matching a parsing the diffusive signature
 	private static final String VALID_NAME = "[a-zA-Z]+[\\w]*";
 	private static final String PRIMITIVE_ARRAY = createPrimitiveArray( ENCODED_ARRAY_IDENTIFIER );
 	private static final String OBJECT_ARRAY = createObjectArray( ENCODED_ARRAY_IDENTIFIER, VALID_NAME );
 	private static final String VALID_CLASS_NAME = createValidClassName( PRIMITIVE_ARRAY, OBJECT_ARRAY );
 	private static final String VALID_METHOD_NAME = VALID_NAME;
 	private static final String ARGUMENT_TYPE_LIST = "(" + VALID_CLASS_NAME + "(" + Pattern.quote( ARGUMENT_SEPARATOR ) + VALID_CLASS_NAME +")*)?";
 	private static final String RETURN_TYPE_CLASS_NAME = "(" + Pattern.quote( RETURN_TYPE_SEPARATOR ) + VALID_CLASS_NAME + ")?";
 	private static final String REGEX = "^" + 
 											VALID_CLASS_NAME + 
 											Pattern.quote( CLASS_METHOD_SEPARATOR )+ 
 											VALID_METHOD_NAME + 
 											Pattern.quote( ARGUMENT_OPEN ) +
 												ARGUMENT_TYPE_LIST +
 											Pattern.quote( ARGUMENT_CLOSE ) + 
 											RETURN_TYPE_CLASS_NAME +
 										 "$";
 	private static final Pattern REGEX_PATTERN = Pattern.compile( REGEX );
 
 	// for the hashCode method
 	private volatile int hashCode;
 
 	// signature
 	private final String className;
 	private final String methodName;
 	private final String returnTypeClassName;
 	private final List< String > argumentTypes;
 	private final String signature;
 	
 	/**
 	 * Constructs a {@link DiffuserSignature} based on the specified return type name, the name of the class containing the
 	 * diffusive method, the method to diffuse, and the list of argument type names. 
 	 * @param returnTypeClassName The name of the return type
 	 * @param className The name of the class that contains the method to diffuse
 	 * @param methodName The name of the method to diffuse
 	 * @param argumentTypes The class names of the arguments passed to the method
 	 */
 	public DiffuserSignature( final String returnTypeClassName, final String className, final String methodName, final List< String > argumentTypes )
 	{
 		this.className = className;
 		this.methodName = methodName;
 		this.returnTypeClassName = ( returnTypeClassName == null ? void.class.getName() : returnTypeClassName );
 		this.argumentTypes = argumentTypes;
 		
 		// construct the signature
 		this.signature = createId( returnTypeClassName, className, methodName, argumentTypes );
 		
 		hashCode = 0;
 	}
 	
 	/**
 	 * Constructs a {@link DiffuserSignature} for a method that does not return a value, the name of the class containing the
 	 * diffusive method, the method to diffuse, and the list of argument type names. 
 	 * @param className The name of the class that contains the method to diffuse
 	 * @param methodName The name of the method to diffuse
 	 * @param argumentTypes The class names of the arguments passed to the method
 	 */
 	public DiffuserSignature( final String className, final String methodName, final List< String > argumentTypes )
 	{
 		this( void.class.getName(), className, methodName, argumentTypes );
 	}
 	
 	/**
 	 * Constructs a {@link DiffuserSignature} by parsing an existing diffuser ID string. The {@link Diffuser} ID 
 	 * is constructed as follows:<p>
 	 * {@code class:method(argument1,argument2,argument3,...,argumentN)-returnType}<p> 
 	 * where the "{@code class}" is the fully qualified class name; the method is the name of the method;
 	 * the arguments are all the fully qualified class names of the argument; and the {@code returnType}
 	 * is the fully qualified class name of the return type (which could be {@code void.class}.
 	 * @param id The diffuser ID string
 	 */
 	public DiffuserSignature( final String id )
 	{
 		this( parse( id ) );
 	}
 	
 	/**
 	 * Copy constructor that creates a copy of the specified diffuser ID. {@link DiffuserSignature} objects are
 	 * immutable, so this isn't terribly necessary. 
 	 * @param id The {@link DiffuserSignature} to copy
 	 */
 	public DiffuserSignature( final DiffuserSignature id )
 	{
 		this( id.returnTypeClassName, id.className, id.methodName, new ArrayList<>( id.argumentTypes ) );
 	}
 	
 	/**
 	 * @return the class name of the return value
 	 */
 	public String getReturnTypeClassName()
 	{
 		return returnTypeClassName;
 	}
 	
 	/**
 	 * @return The {@link Class} of the return value
 	 */
 	public Class< ? > getReturnTypeClazz()
 	{
 		return ReflectionUtils.getClazz( decodeType( returnTypeClassName ) );
 	}
 	
 	/**
 	 * @return The name of the class that contains the method to be diffused
 	 */
 	public String getClassName()
 	{
 		return className;
 	}
 	
 	/**
 	 * @return The {@link Class} that contains the method to be diffused
 	 */
 	public Class< ? > getClazz()
 	{
 		return ReflectionUtils.getClazz( decodeType( className ) );
 	}
 	
 	/**
 	 * @return the name of the method that is diffused
 	 */
 	public String getMethodName()
 	{
 		return methodName;
 	}
 	
 	/**
 	 * @return A list of {@link Class} names corresponding the to arguments passed to the diffused method
 	 */
 	public List< String > getArgumentTypeNames()
 	{
 		return argumentTypes;
 	}
 	
 	/**
 	 * @return A list of {@link Class} objects corresponding the to arguments passed to the diffused method
 	 */
 	public List< Class< ? > > getArgumentTypes()
 	{
 		final List< Class< ? > > types = new ArrayList<>();
 		for( String type : argumentTypes )
 		{
 			types.add( ReflectionUtils.getClazz( decodeType( type ) ) );
 		}
 		return types;
 	}
 	
 	/**
 	 * Constructs a diffuser ID string for a method that doesn't have a return value
 	 * @param clazz The {@link Class} that contains the diffused method
 	 * @param methodName The name of the diffused method
 	 * @param argumentTypes The {@link Class} objects of the arguments passed into the diffused method
 	 * @return a diffuser ID string for a method that doesn't have a return value
 	 */
 	public static final String createId( final Class< ? > clazz, final String methodName, final Class< ? >...argumentTypes )
 	{
 		return createId( void.class, clazz, methodName, argumentTypes );
 	}
 	
 	/**
 	 * Constructs a diffuser ID string
 	 * @param returnType The {@link Class} object representing the method's return type
 	 * @param clazz The {@link Class} that contains the diffused method
 	 * @param methodName The name of the diffused method
 	 * @param argumentTypes The {@link Class} objects of the arguments passed into the diffused method
 	 * @return a diffuser ID string
 	 */
 	public static final String createId( final Class< ? > returnType, final Class< ? > clazz, final String methodName, final Class< ? >...argumentTypes )
 	{
 		final List< String > argumentTypeNames = new ArrayList<>();
 		for( Class< ? > argType : argumentTypes )
 		{
 			argumentTypeNames.add( argType.getName() );
 		}
 		final String returnTypeClassName = ( returnType == null ? void.class.getName() : returnType.getName() );
 		return createId( returnTypeClassName, clazz.getName(), methodName, argumentTypeNames );
 	}
 
 	/**
 	 * Constructs the diffuser ID string, for a method that does not return a value, based on 
 	 * the specified values
 	 * @param containingClassName The name of the class that contains the method to diffuse
 	 * @param methodName The name of the method to diffuse
 	 * @param argumentTypes The class names of the arguments passed to the method
 	 * @return the diffuser ID string based on the specified values
 	 */
 	public static final String createId( final String containingClassName, 
 			 							 final String methodName, 
 			 							 final List< String > argumentTypes )
 	{
 		return createId( void.class.getName(), containingClassName, methodName, argumentTypes );
 	}
 	
 	/**
 	 * Constructs the diffuser ID string based on the specified values
 	 * @param returnType The name of the return type
 	 * @param containingClassName The name of the class that contains the method to diffuse
 	 * @param methodName The name of the method to diffuse
 	 * @param argumentTypes The class names of the arguments passed to the method
 	 * @return the diffuser ID string based on the specified values
 	 */
 	public static final String createId( final String returnType, 
 										 final String containingClassName, 
 										 final String methodName, 
 										 final List< String > argumentTypes )
 	{
 		// create the name/id for the diffuser
 		final StringBuffer buffer = new StringBuffer();
 		buffer.append( containingClassName + CLASS_METHOD_SEPARATOR );
 		buffer.append( methodName + ARGUMENT_OPEN );
 		for( int i = 0; i < argumentTypes.size(); ++i )
 		{
			buffer.append( argumentTypes.get( i ) );
 			if( i < argumentTypes.size()-1 )
 			{
 				buffer.append( ARGUMENT_SEPARATOR );
 			}
 		}
 		buffer.append( ARGUMENT_CLOSE );
 		buffer.append( RETURN_TYPE_SEPARATOR + returnType );
 		
 		// attempt to URL encode the signature
 		return buffer.toString();
 	}
 
 	/**
 	 * Encodes the array type identifier from {@link #ARRAY_IDENTIFIER} (={@value #ARRAY_IDENTIFIER}) to
 	 * {@link #ENCODED_ARRAY_IDENTIFIER} (={@value #ENCODED_ARRAY_IDENTIFIER}). For example, an {@code int[]}
 	 * is translated from "{@code [I}" to "{@code ;I}", and a {@code String[]} is translated from 
 	 * "{@code [Ljava.lang.String}" to "{@code ;Ljava.lang.String}".
 	 * @param className The name of the array class
 	 * @return The encode class name
 	 */
 	public static final String encodeType( final String className )
 	{
 		return encodeType( className, true );
 	}
 	
 	/**
 	 * Encodes the array type identifier from {@link #ENCODED_ARRAY_IDENTIFIER} (={@value #ENCODED_ARRAY_IDENTIFIER}) to
 	 * {@link #ARRAY_IDENTIFIER} (={@value #ARRAY_IDENTIFIER}). For example, an {@code int[]}
 	 * is translated from "{@code ;I}" to "{@code [I}", and a {@code String[]} is translated from 
 	 * "{@code ;Ljava.lang.String}" to "{@code [Ljava.lang.String}".
 	 * @param className The name of the array class
 	 * @return The encode class name
 	 */
 	public static final String decodeType( final String className )
 	{
 		return encodeType( className, false );
 	}
 	
 	/**
 	 * Converts array class names from their encoded and decoded forms.
 	 * @param className The name of the array class
 	 * @param encode set to true to encode from {@link #ARRAY_IDENTIFIER} (={@value #ARRAY_IDENTIFIER}); to
 	 * {@link #ENCODED_ARRAY_IDENTIFIER} (={@value #ENCODED_ARRAY_IDENTIFIER}) and set to false to decode from
 	 * from {@link #ENCODED_ARRAY_IDENTIFIER} (={@value #ENCODED_ARRAY_IDENTIFIER}) to
 	 * {@link #ARRAY_IDENTIFIER} (={@value #ARRAY_IDENTIFIER})
 	 * @return
 	 */
 	private static String encodeType( final String className, final boolean encode )
 	{
 		final String arrayIdentifier = ( encode ? ARRAY_IDENTIFIER : ENCODED_ARRAY_IDENTIFIER );
 		final String encodedArrayIdentifier = ( encode ? ENCODED_ARRAY_IDENTIFIER : ARRAY_IDENTIFIER );
 		
 		final StringBuffer buffer = new StringBuffer();
 		
 		final String primitiveArray = createPrimitiveArray( arrayIdentifier );
 		final String objectArray = createObjectArray( arrayIdentifier, VALID_NAME );
 
 		// if this is a primitive array, then we strip off the letter code at the end and encode
 		if( Pattern.matches( "^" + primitiveArray + "$", className ) )
 		{
 			final String primitiveType = className.substring( className.length()-1 );
 			final int arrayDimension = className.length()-1;
 			for( int i = 0; i < arrayDimension; ++i )
 			{
 				buffer.append( encodedArrayIdentifier );
 			}
 			buffer.append( primitiveType );
 			
 		}
 		// next see if this is the more specific that an object array (i.e. valid class name for
 		// a non-object array class)
 		else if( Pattern.matches( "^" + createClassName( VALID_NAME ) + "$", className ) )
 		{
 			buffer.append( className );
 		}
 		// it must be an object array
 		else if( Pattern.matches( "^" + objectArray + "$", className ) )
 		{
 			final int arrayDimension = className.indexOf( "L" );
 			final String objectType = className.substring( arrayDimension );
 			for( int i = 0; i < arrayDimension; ++i )
 			{
 				buffer.append( encodedArrayIdentifier );
 			}
 			buffer.append( objectType );
 		}
 		return buffer.toString();
 	}
 
 	
 	/**
 	 * @return The diffuser ID string
 	 */
 	public String getId()
 	{
 		return signature;
 	}
 
 	/**
 	 * Constructs a {@link DiffuserSignature} for a method that doesn't have a return value
 	 * @param clazz The {@link Class} that contains the diffused method
 	 * @param methodName The name of the diffused method
 	 * @param argumentTypes The {@link Class} objects of the arguments passed into the diffused method
 	 * @return a {@link DiffuserSignature} for a method that doesn't have a return value
 	 */
 	public static final synchronized DiffuserSignature create( final Class< ? > clazz, 
 														final String methodName, 
 														final Class< ? >...argumentTypes )
 	{
 		return create( void.class, clazz, methodName, argumentTypes );
 	}
 	
 	/**
 	 * Constructs a {@link DiffuserSignature}
 	 * @param returnType The {@link Class} object representing the method's return type
 	 * @param clazz The {@link Class} that contains the diffused method
 	 * @param methodName The name of the diffused method
 	 * @param argumentTypes The {@link Class} objects of the arguments passed into the diffused method
 	 * @return a {@link DiffuserSignature}
 	 */
 	public static final synchronized DiffuserSignature create( final Class< ? > returnType, 
 														final Class< ? > clazz, 
 														final String methodName, 
 														final Class< ? >...argumentTypes )
 	{
 		final List< String > argumentTypeNames = new ArrayList<>();
 		for( Class< ? > argType : argumentTypes )
 		{
 			argumentTypeNames.add( argType.getName() );
 		}
 		final String returnTypeClassName = ( returnType == null ? void.class.getName() : returnType.getName() );
 		return create( returnTypeClassName, clazz.getName(), methodName, argumentTypeNames );
 	}
 
 	/**
 	 * Constructs the {@link DiffuserSignature}, for a method that does not return a value, based on 
 	 * the specified values
 	 * @param containingClassName The name of the class that contains the method to diffuse
 	 * @param methodName The name of the method to diffuse
 	 * @param argumentTypes The class names of the arguments passed to the method
 	 * @return the {@link DiffuserSignature} based on the specified values
 	 */
 	public static final synchronized DiffuserSignature create( final String containingClassName, 
 														final String methodName, 
 														final List< String > argumentTypes )
 	{
 		return create( void.class.getName(), containingClassName, methodName, argumentTypes );
 	}
 	
 	/**
 	 * Constructs the {@link DiffuserSignature} based on the specified values
 	 * @param returnType The name of the return type
 	 * @param containingClassName The name of the class that contains the method to diffuse
 	 * @param methodName The name of the method to diffuse
 	 * @param argumentTypes The class names of the arguments passed to the method
 	 * @return the {@link DiffuserSignature} based on the specified values
 	 */
 	public static final synchronized DiffuserSignature create( final String returnType, 
 														final String containingClassName, 
 														final String methodName, 
 														final List< String > argumentTypes )
 	{
 		return new DiffuserSignature( returnType, containingClassName, methodName, argumentTypes );
 	}
 	
 	/**
 	 * Returns true if the specified signature is a valid signature; false otherwise. The {@link Diffuser} ID 
 	 * is constructed as follows:<p>
 	 * {@code class:method(argument1,argument2,argument3,...,argumentN)-returnType}<p> 
 	 * where the "{@code class}" is the fully qualified class name; the method is the name of the method;
 	 * the arguments are all the fully qualified class names of the argument; and the {@code returnType}
 	 * is the fully qualified class name of the return type (which could be {@code void.class}.
 	 * @param signature2 The signature
 	 * @return true if the specified signature is a valid signature; false otherwise
 	 */
 	public static boolean isValid( final String signature )
 	{
 		boolean isValid = false;
 		if( signature != null && !signature.isEmpty() )
 		{
 			isValid = REGEX_PATTERN.matcher( signature ).matches();
 		}
 		return isValid;
 	}
 	
 	/**
 	 * Parses the specified diffuser ID string into a {@link DiffuserSignature} object. The {@link Diffuser} ID 
 	 * is constructed as follows:<p>
 	 * {@code class:method(argument1,argument2,argument3,...,argumentN)-returnType}<p> 
 	 * where the "{@code class}" is the fully qualified class name; the method is the name of the method;
 	 * the arguments are all the fully qualified class names of the argument; and the {@code returnType}
 	 * is the fully qualified class name of the return type (which could be {@code void.class}.
 	 * @param signature The diffuser ID string representing the signature
 	 * @return a {@link DiffuserSignature} object based on the specified diffuser ID string
 	 */
 	public static final synchronized DiffuserSignature parse( final String signature )
 	{
 		return parse( signature, true );
 	}
 	
 	/**
 	 * Parses the specified diffuser ID string into a {@link DiffuserSignature} object. The {@link Diffuser} ID 
 	 * is constructed as follows:<p>
 	 * {@code class:method(argument1,argument2,argument3,...,argumentN)-returnType}<p> 
 	 * where the "{@code class}" is the fully qualified class name; the method is the name of the method;
 	 * the arguments are all the fully qualified class names of the argument; and the {@code returnType}
 	 * is the fully qualified class name of the return type (which could be {@code void.class}.
 	 * @param signature The diffuser ID string representing the signature
 	 * @param isFirstPass true if this is first pass, which if fails, strips out the spaces and tries again
 	 * @return a {@link DiffuserSignature} object based on the specified diffuser ID string
 	 */
 	private static final synchronized DiffuserSignature parse( final String signature, boolean isFirstPass )
 	{
 		String className = null;
 		String methodName = null;
 		String returnClassName = null;
 		List< String > argumentTypes = null;
 		
 		// parse
 		Matcher matcher = REGEX_PATTERN.matcher( signature );
 		if( matcher.matches() )
 		{
 			// grab the class name
 			matcher = Pattern.compile( "^" + VALID_CLASS_NAME ).matcher( signature );
 			matcher.find();
 			className = matcher.group();
 			
 			// grab the method name (we know at this point that we have at least "class:method"
 			matcher = Pattern.compile( "^" + VALID_METHOD_NAME ).matcher( signature.split( Pattern.quote( CLASS_METHOD_SEPARATOR ) )[ 1 ] );
 			matcher.find();
 			methodName = matcher.group();
 			
 			// now parse out the argument types
 			final String argString = "^" + ARGUMENT_TYPE_LIST + Pattern.quote( ARGUMENT_CLOSE );
 			matcher = Pattern.compile( argString ).matcher( signature.split( Pattern.quote( ARGUMENT_OPEN ) )[ 1 ] );
 			matcher.find();
 			final String endString = matcher.group();
 			if( endString.equals( ARGUMENT_CLOSE ) )
 			{
 				argumentTypes = new ArrayList<>();
 			}
 			else
 			{
 				argumentTypes = Arrays.asList( endString.split( Pattern.quote( ARGUMENT_CLOSE ) )[ 0 ].split( Pattern.quote( ARGUMENT_SEPARATOR ) ) );
 			}
 			
 			// now parse out the return type
 			final String[] returnTypes = signature.split( Pattern.quote( RETURN_TYPE_SEPARATOR ) );
 			if( returnTypes != null && returnTypes.length > 1 )
 			{
 				final String returnTypeString = "^" + VALID_CLASS_NAME + "$";
 				matcher = Pattern.compile( returnTypeString ).matcher( returnTypes[ 1 ] );
 				matcher.find();
 				returnClassName = matcher.group();
 			}
 			else
 			{
 				returnClassName = void.class.getName();
 			}
 		}
 		else
 		{
 			final StringBuffer message = new StringBuffer();
 			message.append( "Failed to parse signature: Invalid DiffuserId" + Constants.NEW_LINE );
 			message.append( "  Specified DiffuserId: " + signature + Constants.NEW_LINE );
 			message.append( "  Regex: " + REGEX + Constants.NEW_LINE );
 			if( isFirstPass )
 			{
 				try
 				{
 					final DiffuserSignature sig = DiffuserSignature.parse( signature.replaceAll( "\\s", "" ), false );
 					message.append( Constants.NEW_LINE + "  Hint: try removing spaces from signature" + Constants.NEW_LINE );
 					message.append( "  Recommended DiffuserId: " + sig.getId() );
 				}
 				catch( IllegalArgumentException e ) {}
 			}
 			LOGGER.error( message.toString() );
 			throw new IllegalArgumentException( message.toString() );
 		}
 		
 		// return
 		return new DiffuserSignature( returnClassName, className, methodName, argumentTypes );
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.freezedry.persistence.copyable.Copyable#getCopy()
 	 */
 	@Override
 	public DiffuserSignature getCopy()
 	{
 		return new DiffuserSignature( this );
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals( final Object object )
 	{
 		if( !(object instanceof DiffuserSignature) )
 		{
 			return false;
 		}
 		
 		final DiffuserSignature id = (DiffuserSignature)object;
 		if( signature.equals( id.signature ) )
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.sun.java.Object#hashCode()
 	 */
 	@Override
 	public int hashCode()
 	{
 		int result = hashCode;
 		if( result == 0 )
 		{
 			result = 17;
 			result = 31 * result + ( signature == null ? 0 : signature.hashCode() );
 			hashCode = result;
 		}
 		return hashCode;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString()
 	{
 		final StringBuffer buffer = new StringBuffer();
 		buffer.append( "ID: " + getId() + Constants.NEW_LINE );
 		buffer.append( "  Class Name: " + className + Constants.NEW_LINE );
 		buffer.append( "  Method Name: " + methodName + Constants.NEW_LINE );
 		buffer.append( "  Return Class Name: " + returnTypeClassName + Constants.NEW_LINE );
 		if( argumentTypes == null || argumentTypes.isEmpty() )
 		{
 			buffer.append( "  [No Method Arguements] " + Constants.NEW_LINE );
 		}
 		else
 		{
 			buffer.append( "  Argument Type List" + Constants.NEW_LINE );
 			for( String argumentType : argumentTypes )
 			{
 				buffer.append( "    " + argumentType + Constants.NEW_LINE );
 			}
 		}
 		return buffer.toString();
 	}
 
 	private static String createPrimitiveArray( final String arrayIdentifier )
 	{
 		return "((" + Pattern.quote( arrayIdentifier ) + ")+[ZBCDFIJS])";
 	}
 	
 	private static String createClassName( final String validName )
 	{
 		return "(" + validName + "(\\." + validName + ")*)";
 	}
 	
 	private static String createObjectArray( final String arrayIdentifier, final String validName )
 	{
 //		return "((" + Pattern.quote( arrayIdentifier ) + ")+[L])?(" + validName + "(\\." + validName + ")*)+";
 		return "((" + Pattern.quote( arrayIdentifier ) + ")+[L])?" + createClassName( validName ) + "+";
 	}
 	
 	private static String createValidClassName( final String primitiveArray, final String objectArray )
 	{
 		return "(" + primitiveArray + "|" + objectArray + ")";
 	}
 
 	
 	public static void main( String[] args ) throws IllegalArgumentException
 	{
 //		// parse
 //		final String validName = "[a-zA-Z]+[\\w]*";
 //		final String primitiveArray = "(" + Pattern.quote( ARRAY_IDENTIFIER ) + ")+[ZBCDFIJS]";
 //		final String objectArray = "(" + Pattern.quote( ARRAY_IDENTIFIER ) + ")+[L]";
 //		final String validClassName = "((" + primitiveArray + ")|((" + objectArray + ")?(" + validName + "(\\." + validName + ")*)+))";
 //		final String validMethodName = validName;
 //		final String argumentTypeList = "(" + validClassName + "(" + Pattern.quote( ARGUMENT_SEPARATOR ) + validClassName +")*)?";
 //		final String returnTypeClassName = "(" + Pattern.quote( RETURN_TYPE_SEPARATOR ) + validClassName + ")?";
 //		final String regex = "^" + 
 //								validClassName + 
 //								Pattern.quote( CLASS_METHOD_SEPARATOR )+ 
 //								validMethodName + 
 //								Pattern.quote( ARGUMENT_OPEN ) +
 //									argumentTypeList +
 //								Pattern.quote( ARGUMENT_CLOSE ) + 
 //								returnTypeClassName +
 //							 "$";
 //		
 ////		final Pattern pattern = Pattern.compile( "^" + argumentTypeList + "$" );
 //		final Pattern pattern = Pattern.compile( regex );
 //		Matcher matcher = pattern.matcher( "java.lang.String:concat([I,[Ljava.lang.String,[I,[Zd)" );
 ////		Matcher matcher = pattern.matcher( "[[[I,[Ljava[I" );
 //		if( matcher.find() )
 //		{
 //			System.out.println( matcher.group() );
 //		}
 //		else
 //		{
 //			System.out.println( "not found" );
 //		}
 //		System.exit( 0 );
 		
 //		System.out.println( int[].class.getName() );
 //		System.out.println( int.class.getName() );
 //		System.out.println( String[].class.getName() );
 		DOMConfigurator.configure( "log4j.xml" );
 		Logger.getRootLogger().setLevel( Level.DEBUG );
 //
 //		System.out.println( "1   " + DiffuserSignature.parse( "java.lang.String:concat(java.lang.String,java.lang.String)" ).toString() );
 //		System.out.println( "2   " + DiffuserSignature.parse( "java.lang.String:concat(java.lang.String)" ).toString() );
 //		System.out.println( "2a  " + DiffuserSignature.parse( "java.lang.String:concat(java.lang.String)-java.lang.Double" ).toString() );
 //		System.out.println( "3   " + DiffuserSignature.parse( "java.lang.String:concat()" ).toString() );
 ////		System.out.println( "3a  " + DiffuserSignature.parse( "java.lang.String:concat();java.lang.Double" ).toString() );
 ////		System.out.println( "4   " + DiffuserSignature.parse( "java.lang.String:concat( java.lang.String, java.lang.String )" ).toString() );
 ////		System.out.println( "5   " + DiffuserSignature.parse( "java.lang.String:concat.test(java.lang.String,java.lang.String)" ).toString() );
 		System.out.println( "6   " + DiffuserSignature.parse( "java.lang.String:concat(;I)" ).toString() );
 		System.out.println( "7   " + DiffuserSignature.parse( "java.lang.String:concat(;;Ljava.lang.String,;I)" ).toString() );
 		System.out.println( "7   " + DiffuserSignature.parse( "java.lang.String:concat(;I,;Z)" ).toString() );
 //		System.out.println( "8   " + DiffuserSignature.parse( "java.lang.String:concat([I,[[Ljava.lang.String,[I,[Z,[L)" ).toString() );
 		
 		Feed feed = Atom.createFeed( URI.create( "http://microtitan.org/diffusers/java.lang.String:concat(;;Ljava.lang.String,;I)-void" ), "test", Calendar.getInstance().getTime() );
 		System.out.println( feed.toString() );
 		
 		System.out.println( encodeType( "[[[Ljava.lang.Double" ) + ", " + decodeType( encodeType( "[[[Ljava.lang.Double" ) ) );
 		System.out.println( encodeType( "[Ljava.lang.Double" ) + ", " + decodeType( encodeType( "[Ljava.lang.Double" ) ) );
 		System.out.println( encodeType( "[I" ) + ", " + decodeType( encodeType( "[I" ) ) );
 		System.out.println( encodeType( "[Z" ) + ", " + decodeType( encodeType( "[Z" ) ) );
 		
 		DiffuserSignature sig = DiffuserSignature.parse( ";Ljava.lang.String:concat(;;Ljava.lang.String,;I,int,;;Z)-;I" );
 		System.out.println( sig.toString() );
 		System.out.println( sig.getArgumentTypes() );
 		System.out.println( sig.getClazz() + ", " + sig.getClassName() );
 		System.out.println( sig.getReturnTypeClazz() + ", " + sig.getReturnTypeClassName() );
 	}
 }
