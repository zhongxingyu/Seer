 package com.dottydingo.hyperion.service.validation;
 
 import com.dottydingo.hyperion.exception.HyperionException;
 import com.dottydingo.hyperion.exception.ValidationException;
 import com.dottydingo.hyperion.service.persistence.PersistenceContext;
 import org.springframework.context.MessageSource;
 
 
 
 /**
  */
 public class DefaultValidator<C,P> implements Validator<C, P>
 {
    public static final String REQUIRED_FIELD = "validation_required_field";
    public static final String FIELD_LENGTH = "validation_field_length";
    public static final String CHANGE_NOT_ALLOWED = "validation_change_not_allowed";
 
     private MessageSource messageSource;
 
     public void setMessageSource(MessageSource messageSource)
     {
         this.messageSource = messageSource;
     }
 
     @Override
     public void validateCreate(C clientObject, PersistenceContext persistenceContext)
     {
         ValidationErrorContext errorContext = new ValidationErrorContext();
         validateCreate(clientObject,errorContext,persistenceContext);
         if(errorContext.hasErrors())
             throw new ValidationException(buildValidationErrorMessage(errorContext));
     }
 
     @Override
     public void validateUpdate(C clientObject, P persistentObject, PersistenceContext persistenceContext)
     {
         ValidationErrorContext errorContext = new ValidationErrorContext();
         validateUpdate(clientObject,persistentObject,errorContext,persistenceContext);
         if(errorContext.hasErrors())
             throw new ValidationException(buildValidationErrorMessage(errorContext));
     }
 
     @Override
     public void validateDelete(P persistentObject, PersistenceContext persistenceContext)
     {
         ValidationErrorContext errorContext = new ValidationErrorContext();
         validateDelete(persistentObject, errorContext,persistenceContext);
         if(errorContext.hasErrors())
             throw new ValidationException(buildValidationErrorMessage(errorContext));
     }
 
     protected String buildValidationErrorMessage(ValidationErrorContext errorContext)
     {
         StringBuilder sb = new StringBuilder();
         sb.append("The following validation errors have occurred:");
         for (ValidationErrorHolder holder : errorContext.getValidationErrors())
         {
             sb.append("\n");
             sb.append(messageSource.getMessage(holder.getResourceCode(), holder.getParameters(), null));
         }
         return sb.toString();
     }
 
     protected void validateRequired(ValidationErrorContext errorContext, String fieldName, Object value)
     {
         if(value == null)
             errorContext.addValidationError(REQUIRED_FIELD, fieldName, fieldName);
     }
 
     protected void validateNotBlank(ValidationErrorContext errorContext,String fieldName, String value)
     {
         if(value != null && value.length() == 0)
             errorContext.addValidationError(REQUIRED_FIELD, fieldName, fieldName, value);
     }
 
     protected void validateNotChanged(ValidationErrorContext errorContext,String fieldName, Object clientValue,Object persistentValue)
     {
         if(clientValue != null && !clientValue.equals(persistentValue))
             errorContext.addValidationError(CHANGE_NOT_ALLOWED, fieldName, fieldName);
 
     }
 
     protected void validateLength(ValidationErrorContext errorContext,String fieldName, String value, int maxLength)
     {
         if(value != null && value.length() > maxLength)
             errorContext.addValidationError(FIELD_LENGTH, fieldName, fieldName, maxLength);
     }
 
     protected void validateRevisionMatch(String type, Integer clientRevision,Integer persistentRevision)
     {
         if(clientRevision != null && !clientRevision.equals(persistentRevision))
             throw new HyperionException(409,
                     String.format("The value for %s is stale. The supplied revision %s does not match the existing revision %s.",
                             type,clientRevision,persistentRevision));
     }
 
     protected boolean valueChanged(Object clientValue,Object persistentValue)
     {
         return clientValue != null && !clientValue.equals(persistentValue);
     }
 
 
     protected void validateCreate(C clientObject,ValidationErrorContext errorContext,PersistenceContext persistenceContext)
     {
 
     }
 
     protected void validateUpdate(C clientObject, P persistentObject,ValidationErrorContext errorContext,PersistenceContext persistenceContext)
     {
 
     }
 
     protected void validateDelete(P persistentObject,ValidationErrorContext errorContext,PersistenceContext persistenceContext)
     {
 
     }
 }
