 /*
  * Copyright (C) 2010 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package novelang.system;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Proxy;
 import java.util.Map;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Maps;
 
 /**
  * Creates an immutable object with chainable mutators.
  *
  * Given such an interface which pairs getters and copy-on-change operators:
  * <pre>
 public interface Vanilla {
 
   String getString() ;
   Vanilla withString( String newString ) ;
 
   int getInt() ;
   Vanilla withInt( int newInt ) ;
 
   float getFloat() ;
   Vanilla withFloat( float newFloat ) ;
 
 }</pre>
  * Basing on {@code get} and {@code with} prefixes and type similarity, the {@link Husk#create(Class)}
  * method generates an object instance behaving as one could expect:
  * <pre>
 final Vanilla initial = Husk.create( Vanilla.class ) ;
 final Vanilla updated = initial.withInt( 1 ).withString( "Foo" ).withFloat( 2.0f ) ;
 assertEquals( "Foo", updated.getString() ) ;  
 </pre>
  * When it's convenient to create one object with several parameters, the
  * {@link Converter#converterClass()} annotation indicates a class containing static methods
  * for conversion.
  * <pre>
 @Husk.Converter( converterClass = SomeConverter.class )
 public interface Convertible {
   String getString() ;
   Convertible withString( int i, float f ) ;
 }
 
 @SuppressWarnings( { "UnusedDeclaration" } )
 public static final class SomeConverter {
   public static String convert( final int i, final float f ) {
     return "" + i + ", " + f ;
   }
 }
 </pre>
  * One known problem is, such static method appear as never called.
  * The {@code @SuppressWarnings( { "UnusedDeclaration" } )} may save some warnings, then.
  * <p>
  * Another limitation: for a given graph of interfaces, only one {@link Converter} is taken
  * in account.
  *
  *
  * @author Laurent Caillette
  */
 public final class Husk {
 
   private Husk() { }
 
   public static< T > T create( final Class< T > huskClass ) {
 
     Preconditions.checkArgument( huskClass.isInterface() ) ;
     final Method[] huskMethods = huskClass.getMethods() ;
 
     final Map< String, PropertyDeclaration > properties = Maps.newHashMap() ;
 
     for( final Method method : huskMethods ) {
       final String methodName = method.getName();
 
       if( methodName.startsWith( "get" ) ) {
 
         if( method.getReturnType() == null ) {
           throw new BadDeclarationException(
               "Bad return type for " + methodName + ", should not be void" ) ;
         }
         if( method.getParameterTypes().length > 0 ) {
           throw new BadDeclarationException(
               "Bad parameter count for " + methodName + ", there should be no parameter" ) ;
         }
         final String propertyName = methodName.substring( 3 ) ;
         getForSure( properties, propertyName ).getter = method ;
 
       } else if( methodName.startsWith( "with" ) ) {
         if( !method.getReturnType().isAssignableFrom( huskClass ) ) { // Support subclassing.
           throw new BadDeclarationException(
               "Bad return type for " + methodName + ": " + method.getReturnType() +
               ", should be " + huskClass.getName()
           ) ;
         }
         if( method.getParameterTypes().length == 0 ) {
           throw new BadDeclarationException(
               "Bad parameter count for " + methodName +
               ", there should be at least one parameter"
           ) ;
         }
         final String propertyName = methodName.substring( 4 ) ;
         getForSure( properties, propertyName ).updater = method ;
 
       } else throw new BadDeclarationException(
           "Unsupported property (must be named getXxx or withXxx):" + methodName ) ;
     }
 
     for( final Map.Entry< String, PropertyDeclaration > entry : properties.entrySet() ) {
       final String propertyName = entry.getKey() ;
       final PropertyDeclaration declaration = entry.getValue() ;
       if( declaration.getter == null ) {
         throw new BadDeclarationException( "Missing get" + propertyName + " method" ) ;
       }
       if( entry.getValue().updater == null ) {
         throw new BadDeclarationException( "Missing with" + entry.getKey() + " method" ) ;
       }
       final Class< ? >[] updaterParameterTypes = declaration.updater.getParameterTypes() ;
       final Class< ? > updaterParameterType0 = updaterParameterTypes[ 0 ] ;
       if( updaterParameterTypes.length > 1
        || declaration.getter.getReturnType() != updaterParameterType0
       ) {
         final Converter converter = findConverterAnnotation( huskClass ) ;
         if( converter == null ) {
           throw new BadDeclarationException(
               "Incompatible types: '" +
               declaration.updater.getName() + "' takes " + updaterParameterType0 +
               ", while '" +
               declaration.getter.getName() + "' returns " + declaration.getter.getReturnType()
           ) ;
         } else {
           final Class< ? > converterClass = converter.converterClass() ;
           final Method convertMethod = findConvertMethod(
               converterClass,
               declaration.getter.getReturnType(),
               declaration.updater.getParameterTypes()
           ) ;
           if( convertMethod == null ) {
             throw new BadDeclarationException( "Can't find converter for " + propertyName ) ;
           } else {
             declaration.converter = convertMethod ;
           }
         }
       }
     }
 
     final ImmutableMap.Builder< String, Method > convertersBuilder =
         new ImmutableMap.Builder< String, Method >() ;
     for( final Map.Entry< String, PropertyDeclaration > entry : properties.entrySet() ) {
       final Method converter = entry.getValue().converter ;
       if( converter != null ) {
         convertersBuilder.put( entry.getKey(), converter ) ;
       }
     }
 
     //noinspection unchecked
     return ( T ) Proxy.newProxyInstance(
         Husk.class.getClassLoader(),
         new Class< ? >[] { huskClass },
         new PropertiesKeeper( huskClass, convertersBuilder.build(), EMPTY_MAP )
     ) ;
   }
 
   private static Converter findConverterAnnotation( final Class< ? > huskClass ) {
    if( huskClass != null ) {
      final Converter converterAnnotation = huskClass.getAnnotation( Converter.class ) ;
       if( converterAnnotation != null ) {
         return converterAnnotation ;
       }
      final Class< ? >[] interfaces = huskClass.getInterfaces() ;
       for( final Class parentInterface : interfaces ) {
         final Converter annotation = findConverterAnnotation( parentInterface );
         if( annotation != null ) {
           return annotation ;
         }
       }
     }
     return null ;
   }
 
   public static class BadDeclarationException extends RuntimeException {
     public BadDeclarationException( final String message ) {
       super( message );
     }
   }
 
   private static PropertyDeclaration getForSure(
       final Map< String, PropertyDeclaration > properties,
       final String propertyName
   ) {
     final PropertyDeclaration existingProperty = properties.get( propertyName ) ;
     if( existingProperty == null ) {
       final PropertyDeclaration newProperty = new PropertyDeclaration() ;
       properties.put( propertyName, newProperty ) ;
       return newProperty ;
     } else {
       return existingProperty ;
     }
   }
 
   private static Method findConvertMethod(
       final Class< ? > converterClass,
       final Class< ? > returnType,
       final Class< ? >[] parameterTypes
   ) {
     for( final Method candidateConvertMethod : converterClass.getMethods() ) {
       if( Modifier.isStatic( candidateConvertMethod.getModifiers() )
        && returnType.isAssignableFrom( candidateConvertMethod.getReturnType() )
        && areCompatible( parameterTypes, candidateConvertMethod.getParameterTypes() )
       ) {
         return candidateConvertMethod ;
       }
     }
     return null ;
   }
 
   private static boolean areCompatible(
       final Class< ? >[] definition,
       final Class< ? >[] occurence
   ) {
     if( definition.length == occurence.length ) {
       for( int i = 0 ; i < occurence.length ; i ++ ) {
         final Class< ? > defined = definition[ i ] ;
         final Class< ? > occuring = occurence[ i ] ;
         if( ! defined.isAssignableFrom( occuring ) ) {
           return false ;
         }
       }
       return true ;
     } else {
       return false ;
     }
   }
 
 
   private static class PropertyDeclaration {
     public Method getter = null ;
     public Method updater = null ;
     public Method converter = null ;
   }
 
 
   private static final ImmutableMap< String ,Object > EMPTY_MAP = ImmutableMap.of() ;
 
 
   private static class PropertiesKeeper implements InvocationHandler {
 
     private final Class< ? > huskClass;
     private final Map< String, Method > converters ;
     private final Map< String, Object > values ;
 
     private PropertiesKeeper(
         final Class< ? > huskClass,
         final Map< String, Method > converters,
         final Map< String, Object > values
     ) {
       this.huskClass = huskClass;
       this.converters = converters ;
       this.values = ImmutableMap.copyOf( values ) ;
     }
 
     public Object invoke(
         final Object proxy,
         final Method method,
         final Object[] args
     ) throws Throwable {
       final String methodName = method.getName() ;
       if( methodName.startsWith( "with" ) ) {
         final String propertyName = methodName.substring( 4 ) ;
         final Object updateValue ;
         final Method converter = converters.get( propertyName ) ;
         if( converter == null ) {
           updateValue = args[ 0 ] ;
         } else {
           updateValue = converter.invoke( null, args ) ;
         }
         final Map< String, Object > updatedValues = Maps.newHashMap() ;
         updatedValues.putAll( values ) ;
         updatedValues.put( propertyName, updateValue ) ;
         return Proxy.newProxyInstance(
             Husk.class.getClassLoader(),
             new Class< ? >[] { huskClass },
             new PropertiesKeeper( huskClass, converters, updatedValues )
         ) ;
       }
       if( methodName.startsWith( "get" ) ) {
         final String propertyName = methodName.substring( 3 ) ;
         final Object value = values.get( propertyName ) ;
         final Class< ? > returnType = method.getReturnType() ;
         if( value == null && returnType.isPrimitive() ) {
           return 0 ;
         } else {
           return value ;
         }
       }
       throw new IllegalStateException(
           "This should not happend: invoking unsupported method " + methodName ) ;
 
     }
   }
 
   @Retention( RetentionPolicy.RUNTIME )
   @Target( ElementType.TYPE )
   public @interface Converter {
     Class< ? > converterClass() ; 
   }
 }
