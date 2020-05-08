 package edu.mx.utvm.congreso.service;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Service;
 
 import edu.mx.utvm.congreso.dao.impl.PreRegisterInformationDaoImpl;
 import edu.mx.utvm.congreso.dominio.PreRegisterInformation;
 import edu.mx.utvm.congreso.dominio.UserRole;
 import edu.mx.utvm.congreso.mail.MailService;
 import edu.mx.utvm.congreso.util.Util;
 @Service
 public class PreRegisterInformationServiceImpl implements PreRegisterInformationService{
 
 	@Autowired
 	private MailService mail;
 	
 	@Autowired
 	private InformationAccountService accountService;
 	
 	@Autowired
 	private UserRoleService roleService;
 	
 	@Autowired	
 	private PreRegisterInformationDaoImpl informationDao;
 	
 	@Value("${URL_CONFIRM_PREREGISTER}")
 	private String urlConfirm;
 	
 	@Value("${MAIL_SENDER}")
 	private String mailSender;
 		
 	
 	private String referenceKeyGenerator(String name, String secondName,
 			String thirdName, String token) {
 		StringBuffer referenceKey = new StringBuffer();
 		referenceKey.append(name.toUpperCase());
 		referenceKey.append(secondName.toUpperCase());
 		referenceKey.append(thirdName.toUpperCase());
 		referenceKey.append("|");
 		referenceKey.append(token.toUpperCase());
 		return referenceKey.toString();
 	}
 	
 	@Override
 	public void saveCapure(PreRegisterInformation preRegisterInformation) {
		/* Generate token and set to object*/
		String token = Util.generateToken(preRegisterInformation.getInformationAccount().getEmail());
		preRegisterInformation.getInformationAccount().setToken(token);
		
 		accountService.save(preRegisterInformation.getInformationAccount());
 		roleService.save(preRegisterInformation.getUserRole());
 		informationDao.createCapture(preRegisterInformation);
 	}
 	
 	@Override
 	public void save(PreRegisterInformation preRegisterInformation) {
 
     	/* Build name */
     	StringBuffer nombre = new StringBuffer();
     	nombre.append(preRegisterInformation.getName()).append(" ");
     	nombre.append(preRegisterInformation.getSecondName()).append(" ");
     	nombre.append(preRegisterInformation.getThirdName()).append(" ");
     	
 		/* Generate token and set to object*/
 		String token = Util.generateToken(preRegisterInformation.getInformationAccount().getEmail());
 		preRegisterInformation.getInformationAccount().setToken(token);
 		
 		/* Generate reference key and set to object*/
 		String referenceKey = referenceKeyGenerator(preRegisterInformation
 				.getName().replaceAll(" ", ""), preRegisterInformation
 				.getSecondName().trim(), preRegisterInformation.getThirdName()
 				.trim(), preRegisterInformation.getInformationAccount()
 				.getToken());
 		
 		referenceKey.replaceAll(" ", referenceKey);
 		
 		preRegisterInformation.getInformationAccount().setReferenceKey(referenceKey);
 		
 		/* Generate url confirm */
 		String urlConfirm = this.urlConfirm + token;
 		
 		/* Mapa de propiedades */
     	Map<String, String> model = new HashMap<String, String>();
     	model.put("nombre", nombre.toString());
     	model.put("url", urlConfirm);
 
     	System.out.println(urlConfirm);
     	
 		mail.sendMail(mailSender, preRegisterInformation
 				.getInformationAccount().getEmail(), "Confirmacin de cuenta",
 				model, MailService.TEMPLATE_CONFIRMATION_ACCOUNT);
 		
 		accountService.save(preRegisterInformation.getInformationAccount());
 		roleService.save(preRegisterInformation.getUserRole());
 		informationDao.create(preRegisterInformation);
 	}
 
 
 	@Override
 	public List<PreRegisterInformation> findAllPreRegisters() {
 		return informationDao.findAll();
 	}
 
 	@Override
 	public boolean getPaymentStatus(String token) {
 		return informationDao.getPaymentStatus(token);
 	}
 
 
 	@Override
 	public void changePaymentStatus(boolean status, String token) {
 		informationDao.changePaymentStatus(status, token);
 		PreRegisterInformation byToken = informationDao.findPreRegisterInformationByToken(token);
 		String paymentStatus = "";
 		if(status){
 			byToken.getUserRole().setAuthority(UserRole.ROLE_PREREGISTERED_SUCCESS_PAYMENT);
 			paymentStatus = "PAGADO";
 		}else{
 			byToken.getUserRole().setAuthority(UserRole.ROLE_PREREGISTERED_SUCCESS);
 			paymentStatus = "NO PAGADO";
 		}
 		
 		roleService.update(byToken.getUserRole());
 		
 		
     	/* Build name */
     	StringBuffer nombre = new StringBuffer();
     	nombre.append(byToken.getName()).append(" ");
     	nombre.append(byToken.getSecondName()).append(" ");
     	nombre.append(byToken.getThirdName()).append(" ");		
 		
 		/* Mapa de propiedades */
     	Map<String, String> model = new HashMap<String, String>();
     	model.put("paymentStatus", paymentStatus);
     	model.put("nombre", nombre.toString());
     	
     	/* Send mail */
 		mail.sendMail(mailSender, byToken.getInformationAccount().getEmail(), "Estado del pago",
 				model, MailService.TEMPLATE_CHANGED_PAYMENT_STATUS);
 	}
 
 
 	@Override
 	public PreRegisterInformation findPreRegisterInformationByToken(String token) {
 		return informationDao.findPreRegisterInformationByToken(token);
 	}
 
 	@Override
 	public List<PreRegisterInformation> findAllPreRegistersByParamSearch(
 			String searchParameter) {
 		return informationDao.findAllPreRegistersByParamSearch(searchParameter);
 	}
 
 	@Override
 	public PreRegisterInformation findPreRegisterInformationByUserName(
 			String userName) {
 		return informationDao.findPreRegisterInformationByUserName(userName);
 	}
 
 	@Override
 	public void updateName(PreRegisterInformation preRegisterInformation,
 			String email) {
 		informationDao.updateName(preRegisterInformation, email);
 	}
 }
