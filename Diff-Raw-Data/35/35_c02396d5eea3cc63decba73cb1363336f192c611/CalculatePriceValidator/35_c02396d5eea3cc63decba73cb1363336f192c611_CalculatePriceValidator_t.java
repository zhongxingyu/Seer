 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package beans;
 
 import data.Storage;
 import javax.faces.application.FacesMessage;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.validator.FacesValidator;
 import javax.faces.validator.Validator;
 import javax.faces.validator.ValidatorException;
 
 /**
  *
  * @author christianlinde
  */
 @FacesValidator(value = "calculatePriceValidator")
 public class CalculatePriceValidator implements Validator {
 
     @Override
     public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
       if ( ! ((String) value).equals("") ) {
         String[] splitString = ((String) value).split(" ");
         // Check if the currency present
        if ( splitString.length == 2 )  {
            if (!splitString[1].equals("€") && !splitString[1].equals("$") ) {
                FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Unbekannte Währung angegeben.", null);
                throw new ValidatorException(fm);
            }
         }
      }
     }
 }
