 package org.mosaic.dao.impl;
 
 import com.google.common.reflect.TypeToken;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import org.mosaic.dao.DaoException;
 import org.mosaic.dao.Query;
 import org.mosaic.dao.extract.ListResultSetExtractor;
 import org.mosaic.dao.extract.ResultSetExtractor;
 import org.mosaic.modules.Service;
 import org.mosaic.util.conversion.ConversionService;
 import org.mosaic.util.io.CharStream;
 import org.mosaic.util.method.MethodHandle;
 import org.mosaic.util.method.MethodParameter;
 import org.mosaic.util.reflection.TypeTokens;
 
 import static java.lang.String.format;
 import static java.util.Arrays.asList;
 import static org.mosaic.dao.extract.ResultSetExtractors.createResultSetExtractorFor;
 
 /**
  * @author arik
  */
 final class QueryAction extends Action
 {
     @Nonnull
     private final String sql;
 
     @Nonnull
     private final List<JdbcParamter> queryParameters;
 
     @Nonnull
     private final ResultSetExtractor resultSetExtractor;
 
     @Nonnull
     @Service
     private ConversionService conversionService;
 
     QueryAction( @Nonnull MethodHandle methodHandle, @Nonnull String dataSourceName )
     {
         super( methodHandle, dataSourceName, true );
 
         Map<String, MethodParameter> parametersByName = new HashMap<>();
         for( MethodParameter methodParameter : methodHandle.getParameters() )
         {
             parametersByName.put( methodParameter.getName(), methodParameter );
         }
 
        Query ann = getMethodHandle().getAnnotation( Query.class ).get();
 
         // parse named parameters
         StringBuilder sql = new StringBuilder( ann.value().length() );
         List<JdbcParamter> queryParameters = new LinkedList<>();
         CharStream stream = new CharStream( ann.value() );
         while( stream.hasNext() )
         {
             char c = stream.next();
             if( c == ':' )
             {
                 String parameterName = stream.readWhileNotAnyOf( " ~`!@#$%^&*()-_=+[{]};:'\"\\|,<.>/?" );
                 MethodParameter methodParameter = parametersByName.get( parameterName );
                 if( methodParameter == null )
                 {
                     String daoName = getMethodHandle().getDeclaringClass().getSimpleName();
                     String daoMethodName = getMethodHandle().getName();
                     String msg = "method '%s' in @Dao '%s' uses unknown parameter: %s";
                     throw new DaoException( format( msg, daoMethodName, daoName, parameterName ) );
                 }
 
                 queryParameters.add( new JdbcParamter( methodParameter, queryParameters.size() ) );
                 sql.append( "?" );
             }
             else
             {
                 sql.append( c );
             }
         }
         this.sql = sql.toString();
         this.queryParameters = queryParameters;
 
         // if one of the parameters if of type ResultSetExtractor - we'll use that
         MethodParameter resultSetExtractorParameter = null;
         for( MethodParameter methodParameter : getMethodHandle().getParameters() )
         {
             if( methodParameter.getType().getRawType().equals( ResultSetExtractor.class ) )
             {
                 if( resultSetExtractorParameter != null )
                 {
                     String daoName = getMethodHandle().getDeclaringClass().getSimpleName();
                     String daoMethodName = getMethodHandle().getName();
                     String msg = "method '%s' in @Dao '%s' has more than one %s parameter";
                     throw new DaoException( format( msg, daoMethodName, daoName, ResultSetExtractor.class.getSimpleName() ) );
                 }
                 else
                 {
                     resultSetExtractorParameter = methodParameter;
                 }
             }
         }
 
         // determine value extractor from return type
         TypeToken<?> returnType = methodHandle.getReturnType();
         Class<?> rawReturnType = returnType.getRawType();
         if( rawReturnType.equals( List.class ) )
         {
             TypeToken<?> itemType = TypeTokens.ofList( rawReturnType );
             if( resultSetExtractorParameter == null )
             {
                 this.resultSetExtractor = new ListResultSetExtractor<>( createResultSetExtractorFor( itemType ) );
             }
             else
             {
                 TypeToken<?> extractionTargetType = resultSetExtractorParameter.getType().resolveType( ResultSetExtractor.class.getTypeParameters()[ 0 ] );
                 if( returnType.isAssignableFrom( extractionTargetType ) )
                 {
                     this.resultSetExtractor = new ProvidedResultSetExtractor( resultSetExtractorParameter );
                 }
                 else if( itemType.isAssignableFrom( extractionTargetType ) )
                 {
                     this.resultSetExtractor = new ListResultSetExtractor<>( new ProvidedResultSetExtractor<>( resultSetExtractorParameter ) );
                 }
                 else
                 {
                     String daoName = getMethodHandle().getDeclaringClass().getSimpleName();
                     String daoMethodName = getMethodHandle().getName();
                     String msg = "method '%s' in @Dao '%s' declares incompatible %s to its return type";
                     throw new DaoException( format( msg, daoMethodName, daoName, ResultSetExtractor.class.getSimpleName() ) );
                 }
             }
         }
         else if( resultSetExtractorParameter == null )
         {
             this.resultSetExtractor = createResultSetExtractorFor( returnType );
         }
         else
         {
             TypeToken<?> extractionTargetType = resultSetExtractorParameter.getType().resolveType( ResultSetExtractor.class.getTypeParameters()[ 0 ] );
             if( returnType.isAssignableFrom( extractionTargetType ) )
             {
                 this.resultSetExtractor = new ProvidedResultSetExtractor( resultSetExtractorParameter );
             }
             else
             {
                 String daoName = getMethodHandle().getDeclaringClass().getSimpleName();
                 String daoMethodName = getMethodHandle().getName();
                 String msg = "method '%s' in @Dao '%s' declares incompatible %s to its return type";
                 throw new DaoException( format( msg, daoMethodName, daoName, ResultSetExtractor.class.getSimpleName() ) );
             }
         }
     }
 
     @Nullable
     @Override
     protected Object invoke( @Nonnull Connection connection, @Nonnull Object... arguments ) throws SQLException
     {
         try( PreparedStatement stmt = connection.prepareStatement( this.sql ) )
         {
             for( int i = 0; i < this.queryParameters.size(); i++ )
             {
                 this.queryParameters.get( i ).apply( stmt, arguments[ i ] );
             }
 
             try( ResultSet rs = stmt.executeQuery() )
             {
                 return this.resultSetExtractor.extract( rs, arguments );
             }
         }
     }
 
     @SuppressWarnings( "unchecked" )
     private class ProvidedResultSetExtractor<Type> extends ResultSetExtractor<Type>
     {
         @Nonnull
         private final MethodParameter resultSetExtractMethodParameter;
 
         private ProvidedResultSetExtractor( @Nonnull MethodParameter resultSetExtractMethodParameter )
         {
             this.resultSetExtractMethodParameter = resultSetExtractMethodParameter;
         }
 
         @Override
         public Type extract( @Nonnull ResultSet rs, @Nonnull Object... arguments ) throws SQLException
         {
             int parameterIndex = this.resultSetExtractMethodParameter.getIndex();
             if( parameterIndex >= arguments.length )
             {
                 String daoName = this.resultSetExtractMethodParameter.getMethod().getDeclaringClass().getSimpleName();
                 String daoMethodName = this.resultSetExtractMethodParameter.getMethod().getName();
                 String parameterName = this.resultSetExtractMethodParameter.getName();
                 String msg = "parameter '%s' of method '%s' in @Dao '%s' has invalid index for list of arguments: %s";
                 throw new DaoException( format( msg, parameterName, daoMethodName, daoName, asList( arguments ) ) );
             }
 
             Object extractor = arguments[ parameterIndex ];
             if( extractor == null )
             {
                 String daoName = this.resultSetExtractMethodParameter.getMethod().getDeclaringClass().getSimpleName();
                 String daoMethodName = this.resultSetExtractMethodParameter.getMethod().getName();
                 String parameterName = this.resultSetExtractMethodParameter.getName();
                 String msg = "parameter '%s' of method '%s' in @Dao '%s' should be a %s but is null instead";
                 throw new DaoException( format( msg, parameterName, daoMethodName, daoName, ResultSetExtractor.class.getName() ) );
             }
             else if( extractor instanceof ResultSetExtractor )
             {
                 ResultSetExtractor<Type> resultSetExtractor = ( ResultSetExtractor<Type> ) extractor;
                 return resultSetExtractor.extract( rs, arguments );
             }
 
             String daoName = this.resultSetExtractMethodParameter.getMethod().getDeclaringClass().getSimpleName();
             String daoMethodName = this.resultSetExtractMethodParameter.getMethod().getName();
             String parameterName = this.resultSetExtractMethodParameter.getName();
             String msg = "parameter '%s' of method '%s' in @Dao '%s' should be of type %s but is instead %s";
             throw new DaoException( format( msg, parameterName, daoMethodName, daoName, ResultSetExtractor.class.getName(), extractor.getClass().getName() ) );
         }
     }
 }
