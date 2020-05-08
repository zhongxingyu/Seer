 package br.com.camiloporto.cloudfinance.web;
 
 import java.util.Set;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.ConstraintViolationException;
 
 import org.springframework.roo.addon.javabean.RooJavaBean;
 
 @RooJavaBean
 public abstract class AbstractOperationResponse {
 	
 	private boolean success;
 	
 	private String[] errors = new String[]{};
 	
 	public AbstractOperationResponse() {
 	}
 	
 	public AbstractOperationResponse(boolean success) {
 		this.success = success;
 	}
 
 	public AbstractOperationResponse(ConstraintViolationException e) {
 		this(false);
 		configureErrorMessages(e.getConstraintViolations());
 	}
 
 	private void configureErrorMessages(
 			Set<ConstraintViolation<?>> constraintViolations) {
 		this.errors = new String[constraintViolations.size()];
 		int i = 0;
 		for (ConstraintViolation<?> cv : constraintViolations) {
 			System.out
					.println("AbstractOperationResponse.configureErrorMessages() " + cv.getPropertyPath() );
			errors[i] = cv.getMessage();
 		}
 	}
 }
