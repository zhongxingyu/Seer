 package org.polyforms.repository.jpa.support;
 
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.inject.Named;
 import javax.persistence.Query;
 
 import org.polyforms.parameter.ArgumentProvider;
 import org.polyforms.parameter.Parameter;
 import org.polyforms.parameter.ParameterMatcher;
 import org.polyforms.parameter.Parameters;
 import org.polyforms.parameter.provider.ArgumentAt;
 import org.polyforms.parameter.support.AbstractParameterMatcher;
 import org.polyforms.parameter.support.MethodParameter;
 import org.polyforms.parameter.support.MethodParameters;
 import org.polyforms.parameter.support.SourceParameters;
 import org.polyforms.repository.jpa.QueryParameterBinder;
 import org.polyforms.util.ArrayUtils;
 
 /**
  * The JPA2 implementation of {@link QueryParameterBinder}.
  * 
  * @author Kuisong Tong
  * @since 1.0
  */
 @Named
 public class Jpa2QueryParameterBinder implements QueryParameterBinder {
     private static final javax.persistence.Parameter<?>[] EMPTY_QUERY_PARAMETERS = new javax.persistence.Parameter<?>[0];
     private static final Comparator<javax.persistence.Parameter<?>> COMPARATOR = new Comparator<javax.persistence.Parameter<?>>() {
         /**
          * {@inheritDoc}
          */
         public int compare(final javax.persistence.Parameter<?> arg0, final javax.persistence.Parameter<?> arg1) {
            return arg0.getName().compareTo(arg1.getName());
         }
     };
     private final Map<Method, ArgumentProvider[]> argumentProvidersCache = new HashMap<Method, ArgumentProvider[]>();
     private final ParameterMatcher<MethodParameter, org.polyforms.parameter.Parameter> parameterMatcher = new QueryParameterMatcher();
 
     /**
      * {@inheritDoc}
      */
     public void bind(final Query query, final Method method, final Object[] arguments) {
         final javax.persistence.Parameter<?>[] parameters = getQueryParameters(query);
         final ArgumentProvider[] argumentProviders = matchParameters(method, parameters);
 
         for (int i = 0; i < argumentProviders.length; i++) {
             final Object argument = argumentProviders[i].get(arguments);
             final javax.persistence.Parameter<?> parameter = parameters[i];
             if (parameter.getPosition() == null) {
                 query.setParameter(parameter.getName(), argument);
             } else {
                 query.setParameter(parameter.getPosition(), argument);
             }
         }
     }
 
     private javax.persistence.Parameter<?>[] getQueryParameters(final Query query) {
         final javax.persistence.Parameter<?>[] parameters = query.getParameters().toArray(EMPTY_QUERY_PARAMETERS);
         Arrays.sort(parameters, COMPARATOR);
         return parameters;
     }
 
     private ArgumentProvider[] matchParameters(final Method method, final javax.persistence.Parameter<?>[] parameters) {
         if (!argumentProvidersCache.containsKey(method)) {
             final MethodParameters methodParameters = new MethodParameters(method.getDeclaringClass(), method);
             methodParameters.applyAnnotation();
 
             final QueryParameters queryParameters = new QueryParameters(parameters);
             argumentProvidersCache.put(method, parameterMatcher.match(methodParameters, queryParameters));
         }
 
         final ArgumentProvider[] argumentProviders = argumentProvidersCache.get(method);
         return argumentProviders;
     }
 }
 
 class QueryParameterMatcher extends AbstractParameterMatcher<MethodParameter, org.polyforms.parameter.Parameter> {
     @Override
     protected ArgumentProvider getArgumentProvider(final SourceParameters sourceParameters,
             final org.polyforms.parameter.Parameter parameter) {
         return new ArgumentAt(sourceParameters.match(parameter).getIndex());
     }
 }
 
 class QueryParameters implements Parameters<Parameter> {
     private final Parameter[] parameters;
 
     protected QueryParameters(final javax.persistence.Parameter<?>[] queryParameters) {
         parameters = new Parameter[queryParameters.length];
 
         for (int i = 0; i < queryParameters.length; i++) {
             final javax.persistence.Parameter<?> queryParameter = queryParameters[i];
             final Parameter parameter = new Parameter();
             parameter.setType(queryParameter.getParameterType());
             parameter.setName(queryParameter.getName());
             parameter.setIndex(queryParameter.getPosition() == null ? i : (queryParameter.getPosition() - 1));
             parameters[i] = parameter;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Parameter[] getParameters() {
         return ArrayUtils.clone(parameters);
     }
 }
