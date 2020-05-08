 package pageControllerLayer;
 
 import java.io.Serializable;
 
 import javax.ejb.EJB;
 import javax.enterprise.context.Conversation;
 import javax.enterprise.context.ConversationScoped;
 import javax.enterprise.inject.Produces;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import daoLayer.AgreementDaoBean;
 import annotations.Current;
 import businessLayer.AbstractShareTable;
 import businessLayer.Agreement;
 import businessLayer.Installment;
 
 @Named("agreementManagerPCB")
 @ConversationScoped
 public class AgreementManagerPageControllerBean implements Serializable{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private Agreement currentAgreement;
 	private Installment currentInstallment;
 	private int selectedAgreementId;
 	private AbstractShareTable currentShareTable;
 	@EJB private AgreementDaoBean agreementDao;
 
 
 	public AgreementManagerPageControllerBean() {
 	}
 	
 	@Inject private Conversation conversation;
 	
 	
 	
 	
 	public int getSelectedAgreementId() {
 		return selectedAgreementId;
 	}
 
 	public void setSelectedAgreementId(int selectedAgreementId) {
 		this.selectedAgreementId = selectedAgreementId;
 	}
 
 	public void begin(){
 		
 		conversation.begin();
 		
 	}
 	
 	public String createAgreement(){
 		
 		begin();
 		currentAgreement = new Agreement();
 		currentShareTable = currentAgreement.getShareTable();
 		return "/resources/sections/agreementWiz.xhtml";
 	}
 	
 	public String addInstallment(){
 		
 		begin();
 		currentAgreement = agreementDao.getById(selectedAgreementId);
 		Installment i = new Installment();
 		i.setAgreement(currentAgreement);
 		currentInstallment = i;
 		currentAgreement.getInstallments().add(i);
 		return "/resources/sections/InstallmentWiz.xhtml";
  
 		
 		
 	}
 	
 	public String save(){
 		
 		agreementDao.create(currentAgreement);
 		close();
		return "/home.xhtml";
 	}
 	
 	public void close(){
 		
 		conversation.end();
 		agreementDao.close();
 	}
 	
 	@ConversationScoped @Produces @Current
 	public Agreement getCurrentAgreement(){
 		
 		return currentAgreement;
 		
 	}
 	
 	@ConversationScoped @Produces @Current
 	public AbstractShareTable getCurrentShareTable(){
 		return currentShareTable;
 	}
 
 	public Conversation getConversation() {
 		return conversation;
 	}
 	
 	@ConversationScoped @Produces @Current
 	public Installment getCurrentInstallment(){
 		
 		return currentInstallment;
 	}
 	
 	
 
 
 }
