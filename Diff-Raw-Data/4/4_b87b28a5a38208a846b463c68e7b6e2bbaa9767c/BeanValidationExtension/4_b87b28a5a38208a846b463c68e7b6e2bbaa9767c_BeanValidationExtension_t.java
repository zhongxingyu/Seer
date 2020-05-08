 package de.chkal.prettyfaces.validation;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import javax.el.ELContext;
 import javax.el.ExpressionFactory;
 import javax.el.ValueExpression;
 import javax.el.ValueReference;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.validator.ValidatorException;
 import javax.validation.ConstraintViolation;
 import javax.validation.Validation;
 import javax.validation.Validator;
 import javax.validation.ValidatorFactory;
 import javax.validation.groups.Default;
 
 import com.ocpsoft.pretty.faces.config.mapping.RequestParameter;
 import com.ocpsoft.pretty.faces.config.mapping.UrlMapping;
 import com.ocpsoft.pretty.faces.spi.ValidationExtension;
 
 /**
  * PrettyFaces extension for Bean Validation integration
  * 
  * @author Christian Kaltepoth
  */
 public class BeanValidationExtension implements ValidationExtension
 {
 
    /**
     * The JSR303 {@link ValidatorFactory}
     */
    private ValidatorFactory validatorFactory;
 
    /**
     * The {@link ExpressionFactory} to use
     */
    private ExpressionFactory expressionFactory;
 
    /**
     * Default constructor
     */
    public BeanValidationExtension()
    {
       validatorFactory = Validation.buildDefaultValidatorFactory();
       expressionFactory = ExpressionFactory.newInstance();
    }
 
   /*
    * @see com.ocpsoft.pretty.faces.spi.ValidationExtension#validate(javax.faces.context.FacesContext, com.ocpsoft.pretty.faces.config.mapping.UrlMapping, com.ocpsoft.pretty.faces.config.mapping.RequestParameter, java.lang.Object)
    */
    public void validate(FacesContext facesContext, UrlMapping mapping, RequestParameter requestParameter, Object value)
          throws ValidatorException
    {
 
       // build value reference
       ELContext elContext = facesContext.getELContext();
       String expression = requestParameter.getExpression().getELExpression();
       ValueReference reference = getValueReference(elContext, expression);
 
       // get referenced object and its property
       Object base = reference.getBase();
       Class baseClass = base.getClass();
       Object property = reference.getProperty();
 
       // build and run Validator
       Validator validator = validatorFactory.getValidator();
       Set<ConstraintViolation<Object>> violations = 
          validator.validateValue(baseClass, property.toString(), value, Default.class);
 
       // any validations?
       if (violations.size() > 0)
       {
 
          // Build list of FacesMessages
          List<FacesMessage> messages = new ArrayList<FacesMessage>();
          for (ConstraintViolation<Object> violation : violations)
          {
             messages.add(new FacesMessage(FacesMessage.SEVERITY_ERROR, violation.getMessage(), violation.getMessage()));
          }
          
          // report validation failure
          throw new ValidatorException(messages);
 
       }
 
    }
 
    /**
     * Create a {@link ValueReference} form the supplied EL expression
     * 
     * @param elContext
     *           The EL context
     * @param expression
     *           The expression
     * @return {@link ValueReference} instance
     */
    private ValueReference getValueReference(ELContext elContext, String expression)
    {
       ValueExpression valueExpression = expressionFactory.createValueExpression(elContext, expression, Object.class);
       return valueExpression.getValueReference(elContext);
    }
 
 }
