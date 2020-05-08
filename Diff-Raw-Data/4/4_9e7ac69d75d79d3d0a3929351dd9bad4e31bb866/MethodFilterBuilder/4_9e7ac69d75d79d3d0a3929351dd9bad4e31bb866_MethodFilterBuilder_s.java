 package org.kasource.commons.reflection;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.kasource.commons.reflection.filter.classes.ClassFilter;
 import org.kasource.commons.reflection.filter.methods.AnnotatedMethodFilter;
 import org.kasource.commons.reflection.filter.methods.AssignableFromMethodFilter;
 import org.kasource.commons.reflection.filter.methods.AssignableToMethodFilter;
 import org.kasource.commons.reflection.filter.methods.MetaAnnotatedMethodFilter;
 import org.kasource.commons.reflection.filter.methods.MethodFilter;
 import org.kasource.commons.reflection.filter.methods.MethodFilterList;
 import org.kasource.commons.reflection.filter.methods.ModifierMethodFilter;
 import org.kasource.commons.reflection.filter.methods.NameMethodFilter;
 import org.kasource.commons.reflection.filter.methods.NegationMethodFilter;
 import org.kasource.commons.reflection.filter.methods.NumberOfParametersMethodFilter;
 import org.kasource.commons.reflection.filter.methods.OrMethodFilter;
 import org.kasource.commons.reflection.filter.methods.ParameterClassMethodFilter;
 import org.kasource.commons.reflection.filter.methods.ReturnTypeAssignableFromMethodFilter;
 import org.kasource.commons.reflection.filter.methods.ReturnTypeMethodFilter;
 import org.kasource.commons.reflection.filter.methods.SignatureMethodFilter;
 
 /**
  * Builder for MethodFilters.
  * <p>
  * This class offers functionality for building more complex
  * filter compositions in an expressive manner. The filters added will be
  * evaluated with AND operator. This builder allows both NOT and OR as
  * additional operators.
  * <p>
  * Examples:
  * {@code
  *  MethodFilter publicGetter = new MethodFilterBuilder().isPublic().name("get[A-Z]\\w*").not().hasReturnType(Void.TYPE).build();
  *  MethodFilter nonPublicfilter = new MethdodFilterBuilder().not().isPublic().build();
  *  MethodFilter privateOrProtected = new MethodFilterBuilder().isPrivate().or().isProtected().build();
  * }
  * 
  * @author rikardwi
  **/
 public class MethodFilterBuilder {
    public static final MethodFilter FILTER_GETTERS = new MethodFilterBuilder().name("get[A-Z].*").or().name("is[A-Z].*").or().name("has[A-Z]*").isPublic().not().returnType(Void.TYPE).numberOfParameters(0).build();
    public static final MethodFilter FILTER_SETTERS = new MethodFilterBuilder().name("set[A-Z].*").isPublic().returnType(Void.TYPE).numberOfParameters(1).build();
     
     
     private enum Operator {NONE, NOT, OR};
     private List<MethodFilter> filters = new ArrayList<MethodFilter>();
     private Operator operator = Operator.NONE;
     
     /**
      * Adds filter to filter list and apply operator.
      * 
      * @param filter filter to add.
      **/
     private void add(MethodFilter filter) {
         switch(operator) {
         case NOT:
             filters.add(new NegationMethodFilter(filter));
             operator = Operator.NONE;
             break;
         case OR:
             filters.set(filters.size() - 1, 
                         new OrMethodFilter(filters.get(filters.size() - 1), filter));
             operator = Operator.NONE;
             break;
         default:
             filters.add(filter);
             break;
         }
     }
     
     /**
      * Set current operator to NOT, which will be applied
      * on the next filter added.
      * 
      * @return The builder to support method chaining.
      **/
     public MethodFilterBuilder not() {
         operator = Operator.NOT;
         return this;
     }
     
     /**
      * Set current operator to OR, which will be applied
      * on the next filter added, by applying OR to the last filter
      * added and the next.
      * 
      * @return The builder to support method chaining.
      **/
     public MethodFilterBuilder or() {
         operator = Operator.OR;
         return this;
     }
     
     /**
      * Adds a filter for public methods only.
      * 
      * @return The builder to support method chaining.
      **/
     public MethodFilterBuilder isPublic() {
         add(new ModifierMethodFilter(Modifier.PUBLIC));
         return this;
     }
     
     /**
      * Adds a filter for protected methods only.
      * 
      * @return The builder to support method chaining.
      **/
     public MethodFilterBuilder isProtected() {
         add(new ModifierMethodFilter(Modifier.PROTECTED));
         return this;
     }
     
     /**
      * Adds a filter for private methods only.
      * 
      * @return The builder to support method chaining.
      **/
     public MethodFilterBuilder isPrivate() {
         add(new ModifierMethodFilter(Modifier.PRIVATE));
         return this;
     }
     
     /**
      * Adds a filter for static methods only.
      * 
      * @return The builder to support method chaining.
      **/
     public MethodFilterBuilder isStatic() {
         add(new ModifierMethodFilter(Modifier.STATIC));
         return this;
     }
     
     /**
      * Adds a filter for synchronized methods only.
      * 
      * @return The builder to support method chaining.
      **/
     public MethodFilterBuilder isSynchronized() {
         add(new ModifierMethodFilter(Modifier.SYNCHRONIZED));
         return this;
     }
     
     /**
      * Adds a filter for default access methods only.
      * 
      * @return The builder to support method chaining.
      **/
     public MethodFilterBuilder isDefault() {
         add(new NegationMethodFilter(new ModifierMethodFilter(Modifier.PUBLIC & Modifier.PROTECTED & Modifier.PRIVATE)));
         return this;
     }
     
     /**
      * Adds a filter for methods methods that has the supplied 
      * modifiers.
      * 
      * @param modifiers Modifiers to match.
      * 
      * @return The builder to support method chaining.
      **/
     public MethodFilterBuilder byModifiers(int modifiers) {
         add(new ModifierMethodFilter(modifiers));
         return this;
     }
     
     /**
      * Adds a filter for methods which names matches
      * the supplied name regular expression.
      * 
      * @param nameRegExp Regular Expression to match method name with.
      * 
      * @return The builder to support method chaining.
      **/
     public MethodFilterBuilder name(String nameRegExp) {
         add(new NameMethodFilter(nameRegExp));
         return this;
     }
     
     /**
      * Adds a filter for methods which is annotated
      * with the supplied annotation.
      * 
      * @param annotation The annotation to match.
      * 
      * @return The builder to support method chaining.
      **/
     public MethodFilterBuilder annotated(Class<? extends Annotation> annotation) {
         add(new AnnotatedMethodFilter(annotation));
         return this;
     }
     
     /**
      * Adds a filter for methods which is annotated
      * with an annotation which is annotated with 
      * the supplied annotation.
      * 
      * @param annotation The meta annotation to match method annotations with.
      * 
      * @return The builder to support method chaining.
      **/
     public MethodFilterBuilder metaAnnotated(Class<? extends Annotation> inheritedAnnotation) {
         add(new MetaAnnotatedMethodFilter(inheritedAnnotation));
         return this;
     }
     
     /**
      * Adds a filter for methods which has the exact signature (types) as
      * the supplied parameter types.
      * 
      * @param params    Varargs type of the parameters, may be empty.
      * 
      * @return The builder to support method chaining.
      **/
     public MethodFilterBuilder hasSignature(Class<?>... params) {
         add(new SignatureMethodFilter(params));
         return this;
     }
     
     /**
      * Adds a filter for methods which has a
      * return type matching the supplied parameter returnType.
      * 
      * @param returnType The class to match return type with.
      * 
      * @return The builder to support method chaining.
      **/
     public MethodFilterBuilder returnType(Class<?> returnType) {
         add(new ReturnTypeMethodFilter(returnType));
         return this;
     }
     
     /**
      * Adds a filter for methods which has a
      * return type that extends the supplied parameter returnType.
      * 
      * @param returnType The class to match return type with.
      * 
      * @return The builder to support method chaining.
      **/
     public MethodFilterBuilder returnTypeExtends(Class<?> returnType) {
         add(new ReturnTypeAssignableFromMethodFilter(returnType));
         return this;
     }
     
     /**
      * Adds a filter for methods with matching number of
      * parameters as the supplied parameter: numberOfParameters.
      * 
      * @param numberOfParameters The number of parameters to match.
      * 
      * @return The builder to support method chaining.
      **/
     public MethodFilterBuilder numberOfParameters(int numberOfParameters) {
         add(new NumberOfParametersMethodFilter(numberOfParameters));
         return this;
     }
     
     /**
      * Adds a filter that allows only methods which parameter types extends the supplied
      * types in the superType parameter. 
      * 
      * Only methods with same number of parameters as the extendsClass parameter will
      * be allowed.   
      * 
      * @param extendsClass Varargs classes that the parameters types should extend, may be empty.
      * 
      * @return The builder to support method chaining.
      **/
     public MethodFilterBuilder parametersExtendsType(Class<?>... superType) {
         add(new AssignableFromMethodFilter(superType));
         return this;
     }
     
     /**
      * Adds a filter that allows only methods which parameter types which is a base class of the supplied
      * types in the baseType parameter. 
      * 
      * Only methods with same number of parameters as the extendsClass parameter will
      * be allowed.   
      * 
      * @param extendsClass Varargs classes that the parameters types should extend, may be empty.
      * 
      * @return The builder to support method chaining.
      **/
     public MethodFilterBuilder parametersSuperType(Class<?>... baseType) {
         add(new AssignableToMethodFilter(baseType));
         return this;
     }
     
     /**
      * Adds a filter for methods that has a parameter which extends the supplied type in the
      * extendsType parameter.
      * 
      * If a method does not have a parameter at parameterIndex (too few parameters) its not allowed. 
      * 
      * @param parameterIndex    The index of the parameter to inspect.
      * @param superType         The class the parameter should extend.
      * 
      * @return The builder to support method chaining.
      **/
     public MethodFilterBuilder parameterExtendsType(int parameterIndex, Class<?> superType) {
         add(new AssignableFromMethodFilter(parameterIndex, superType));
         return this;
     }
     
     /**
      * Adds a filter for methods that has a parameter which is a base class to the supplied type in the
      * baseType parameter.
      * 
      * If a method does not have a parameter at parameterIndex (too few parameters) its not allowed. 
      * 
      * @param parameterIndex    The index of the parameter to inspect.
      * @param baseType          The class the parameter should have a base class.
      * 
      * @return The builder to support method chaining.
      **/
     public MethodFilterBuilder parameterSuperType(int parameterIndex, Class<?> baseType) {
         add(new AssignableToMethodFilter(parameterIndex, baseType));
         return this;
     }
     
    
     /**
      * Adds a filter for methods that has a parameter which type passes the supplied class filter.
      * 
      * If a method does not have a parameter at parameterIndex (too few parameters) its not allowed. 
      * <p>
      * Example:
      * {@code
      *  MethodFilter filter = new MethodFilterBuilder().parameterTypeFilter(0, new ClassFilterBuilder().annotated(MyAnnotation.class).build()).build();
      * }
      * Created a filter for methods which first parameter type should be annotated with @MyAnnotation.
      * 
      * @param parameterIndex    The index of the parameter to inspect.
      * @param filter            The filter which the parameter type should pass.
      * 
      * @return The builder to support method chaining.
      **/
     public MethodFilterBuilder parameterTypeFilter(int parameterIndex, ClassFilter filter) {
         add(new ParameterClassMethodFilter(parameterIndex, filter));
         return this;
     }
     
     /**
      * Adds a filter that allows only methods which parameter types passes the class filter supplied
      * in the parameter filter. 
      * 
      * Only methods with same number of parameters as the filter parameter will
      * be allowed.   
      * 
      * @param filter Varargs class filters that the parameters should pass, may be empty.
      * 
      * @return The builder to support method chaining.
      **/
     public MethodFilterBuilder parametersTypesFilter(ClassFilter... filter) {    
         add(new ParameterClassMethodFilter(filter));
         return this;
     }
     
     /**
      * Returns the MethodFilter built.
      * 
      * @return the MethodFilter built.
      * @throws IllegalStateException if no filter was specified before calling build()
      **/
     public MethodFilter build() {
         if(filters.isEmpty()) {
             throw new IllegalStateException("No filter specified.");
         }
         if(filters.size() == 1) {
             return filters.get(0);
         }
         MethodFilter[] methodFilters = new MethodFilter[filters.size()];
         filters.toArray(methodFilters);
         return new MethodFilterList(methodFilters);
     }
     
 }
