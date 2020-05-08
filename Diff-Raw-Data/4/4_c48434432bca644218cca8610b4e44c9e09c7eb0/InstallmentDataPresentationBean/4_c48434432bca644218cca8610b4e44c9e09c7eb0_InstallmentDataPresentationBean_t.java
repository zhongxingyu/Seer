 package presentationLayer;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 
 import javax.enterprise.context.ConversationScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.validator.ValidatorException;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.joda.money.Money;
 
 import util.Config;
 import util.Messages;
 import annotations.TransferObj;
 import businessLayer.Contract;
 import businessLayer.Installment;
 
 @Named("instDataPB")
 @ConversationScoped
 public class InstallmentDataPresentationBean implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	@Inject
 	@TransferObj
 	private Installment installment;
 
 
 	public InstallmentDataPresentationBean() {}
 
 
 	public void validateAmount(FacesContext context, UIComponent component, Object value) {
 		_validateAmount(installment.getWholeAmount());
 	}
 	
 	public void editingValidateAmount(FacesContext context, UIComponent component, Object value) {
 		_validateAmount(Money.zero(Config.currency));
 	}
 	
 	private void _validateAmount(Money startingValue) {
 		Contract c = installment.getContract();
 		List<Installment> installments = c.getInstallments();
 		Money sum = startingValue;
 		for (Installment i : installments) {
 			if (!i.equals(installment)) {
 				sum = sum.plus(i.getWholeAmount());
 			}
 		}
 		if (sum.isGreaterThan(c.getWholeAmount())) {
 			throw new ValidatorException(Messages.getErrorMessage("err_installmentAmount"));
 		}
 	}
 
 
 	public void validateDeadlineDate(FacesContext context, UIComponent component, Object value) {
 
 		try {
 			Date deadline = (Date) value;
 			Date begin = installment.getContract().getBeginDate();
 
			if (deadline.before(begin)) {
 				throw new ValidatorException(Messages.getErrorMessage("err_instInvalidDeadline"));
 			}
 		} catch (ClassCastException e) {
 			String[] params = { (String) component.getAttributes().get("label") };
 			throw new ValidatorException(Messages.getErrorMessage("err_invalidValue", params));
 		}
 	}
 
 }
