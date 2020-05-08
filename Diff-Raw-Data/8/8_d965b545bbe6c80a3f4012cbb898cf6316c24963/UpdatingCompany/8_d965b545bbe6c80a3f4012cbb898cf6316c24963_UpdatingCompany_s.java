 package ims.actions;
 
 import java.io.File;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.struts2.ServletActionContext;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import ims.business.UpdateCompany;
 import ims.data.Company;
 import ims.data.User;
 
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.ActionSupport;
 
 public class UpdatingCompany extends ActionSupport{
 	
 	private List<Company> list = null;
 	
 	private String companyUserName=null;
 	private String companyName=null;
 	private String companyTelephone=null;
 	private String contactPerson=null;
 	private String companyAddress;
 	private String companyWeb=null;
 	private String noOfVacancies=null;
 	private String password =null;
 	private String password2=null;
 	private String conPassword=null;
 	private boolean allowed;
 	
 	
 	//to upload profile picture
 	private File ProfilePicture;
 	private String ProfilePictureFileName;
 	private String ProfilePictureContentType;
 	
 
 
 	private Company company;
 	User user = new User();
 	
 	ApplicationContext context = new ClassPathXmlApplicationContext("Spring.xml");
 	UpdateCompany updateCompany = (UpdateCompany)context.getBean("UpdateCompany");
 	
 	// to redirect direct access actions  without login
 	HttpServletRequest request=ServletActionContext.getRequest();
 	HttpSession session=request.getSession();
 	String str=(String)session.getAttribute("userName");
 	
 
 	
 	
 	
 	// this method is used to update company profile 
 	public String updateCompany() throws NoSuchAlgorithmException
 	{
 		// to redirect direct access actions  without login
 		if (str==null) {
 			return ERROR;
 				
 		}
 		
 		if(getCompanyName()==null&&getCompanyTelephone()==null&&getContactPerson()==null&&getCompanyAddress()==null&&getCompanyWeb()==null&&getNoOfVacancies()==null&&getPassword()==null)
 		{
 			return ERROR;
 		}
 		
 	
 		Map session;
 		session = ActionContext.getContext().getSession();
 		String userName = (String) session.get("userName");
 		setList(updateCompany.getDetails(userName));
 		
 		company = list.get(0);
 		System.out.println("its exeisting company"+getCompany().getCompanyName());
 		
 		
 		// uploading profile picture
 		ServletContext servletContext = ServletActionContext.getServletContext(); 
 		if (ProfilePicture != null) {
 			String dataDir = servletContext.getRealPath("/WEB-INF/data"); 
 			File existingFile = new File(dataDir, getCompanyUserName()+".gif");
 			existingFile.delete();
 			File savedFile = new File(dataDir, getCompanyUserName()+".gif"); 
 			
 			ProfilePicture.renameTo(savedFile); } 
 		else {
 			//System.out.println("its not working");
 		}
 		
 		
 	
 		if(getCompanyUserName()!=null)
 		company.setCompanyUserName(getCompanyUserName());
 		if(getCompanyName()!=null)
 		company.setCompanyName(getCompanyName());
 		if(getCompanyTelephone()!=null)
 		company.setCompanyTelephone(getCompanyTelephone());
 		if(getContactPerson()!=null)
 		company.setContactPerson(getContactPerson());
 		if(getCompanyAddress()!=null)
 		company.setCompanyAddress(getCompanyAddress());
 		if(getCompanyWeb()!=null)
 		company.setCompanyWeb(getCompanyWeb());
 		if(getNoOfVacancies()!=null)
 		company.setNoOfVacancies(getNoOfVacancies());
 		if(company.isAllowed()==true)
 		company.setAllowed(true);
 		
 		
 		user.setUserName(getCompanyUserName());
 		user.setType('c');
		if(getPassword()==null)
 		{
 			user.setPassword(getPassword2());
 		}
 		else
 		{
 			// password encriptation
 			 MessageDigest md = MessageDigest.getInstance("MD5");
 		        md.update(password.getBytes());
 		 
 		        byte byteData[] = md.digest();
 		 
 		        //convert the byte to hex format method 1
 		        StringBuffer sb = new StringBuffer();
 		        for (int i = 0; i < byteData.length; i++) {
 		         sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
 		        }
 			user.setPassword(sb.toString());
 		}
 		
 		
 		
 
 		
 		updateCompany.updateCompany(getCompanyUserName(),company,user);
 		
		System.out.println("aaaaaaaaaaaaaaaaa");
 		if(company.isAllowed()==false)
 		{
 			return "UnRegisted";
 			
 			
 		}
 		
 		return SUCCESS;
 		
 	}
 	
 	
 	
  // this method is used to allow company by admin
 	
 
 	public String AllowCompany()
 	{
 		// to redirect direct access actions  without login
 		if (str==null) {
 			return ERROR;
 				
 		}
 
 		
 		updateCompany.AllowCompany(getCompanyUserName());
 		return SUCCESS;
 	}
 	
 	
 	// this method is used to unregister company by admin
 	public String UnRegister()
 	{
 		// to redirect direct access actions  without login
 		if (str==null) {
 			return ERROR;
 				
 		}
 
 		
 		updateCompany.UnRegister(getCompanyUserName());
 		return SUCCESS;
 		
 	}
 	
 	
 	
 	// this mehtod is used to delete company by admin
 	public String DeleteCompany()
 	{
 		// to redirect direct access actions  without login
 		if (str==null) {
 			return ERROR;
 				
 		}
 
 		
 		updateCompany.DeleteCompany(getCompanyUserName());
 		return SUCCESS;
 		
 	}
 	
 	
 	// this method is used to allow view student cv To company
 	public String AllowCvToCompany()
 	{
 		// to redirect direct access actions  without login
 		if (str==null) {
 			return ERROR;
 				
 		}
 			
 		
 		updateCompany.AllowCvToCompany(getCompanyUserName());
 		
 		return SUCCESS;
 	}
 	
 	
 	// this method is used to remove student cv form company
 	public String RemoveCvFromCompany()
 	{
 		// to redirect direct access actions  without login
 		if (str==null) {
 			return ERROR;
 				
 		}
 		
 			
 		updateCompany.RemoveCvFromCompany(getCompanyUserName());
 		
 		return SUCCESS;
 	}
 	
 	
 	
 // this method is used to validate new password and confrom password in updating form	
 	public void validate()
 	{
 		if(getPassword()!=null)
 		{
 			if(getPassword().length()!=0)
 			{
 				
 				if(!(getPassword().equals(getConPassword()))){
 					addFieldError("conPassword", "password not match");
 					
 				}
 				
 			}
 		}
 		
 
 
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	// gettes and setters
 	public File getProfilePicture() {
 		return ProfilePicture;
 	}
 
 	public void setProfilePicture(File profilePicture) {
 		ProfilePicture = profilePicture;
 	}
 
 	public String getProfilePictureFileName() {
 		return ProfilePictureFileName;
 	}
 
 	public void setProfilePictureFileName(String profilePictureFileName) {
 		ProfilePictureFileName = profilePictureFileName;
 	}
 
 	public String getProfilePictureContentType() {
 		return ProfilePictureContentType;
 	}
 
 	public void setProfilePictureContentType(String profilePictureContentType) {
 		ProfilePictureContentType = profilePictureContentType;
 	}
 	public List<Company> getList() {
 		return list;
 	}
 
 	public void setList(List<Company> list) {
 		this.list = list;
 	}
 
 	public String getPassword2() {
 		return password2;
 	}
 
 
 	public void setPassword2(String password2) {
 		this.password2 = password2;
 	}
 	public String getCompanyUserName() {
 		return companyUserName;
 	}
 	public void setCompanyUserName(String companyUserName) {
 		this.companyUserName = companyUserName;
 	}
 	public String getCompanyName() {
 		return companyName;
 	}
 	public void setCompanyName(String companyName) {
 		this.companyName = companyName;
 	}
 	public String getCompanyTelephone() {
 		return companyTelephone;
 	}
 	public void setCompanyTelephone(String companyTelephone) {
 		this.companyTelephone = companyTelephone;
 	}
 	public String getContactPerson() {
 		return contactPerson;
 	}
 	public void setContactPerson(String contactPerson) {
 		this.contactPerson = contactPerson;
 	}
 	public String getCompanyAddress() {
 		return companyAddress;
 	}
 	public void setCompanyAddress(String companyAddress) {
 		this.companyAddress = companyAddress;
 	}
 	public String getCompanyWeb() {
 		return companyWeb;
 	}
 	public void setCompanyWeb(String companyWeb) {
 		this.companyWeb = companyWeb;
 	}
 	public String getNoOfVacancies() {
 		return noOfVacancies;
 	}
 	public void setNoOfVacancies(String noOfVacancies) {
 		this.noOfVacancies = noOfVacancies;
 	}
 	public String getPassword() {
 		return password;
 	}
 	public void setPassword(String password) {
 		this.password = password;
 	}
 	public String getConPassword() {
 		return conPassword;
 	}
 	public void setConPassword(String conPassword) {
 		this.conPassword = conPassword;
 	}
 	public boolean isAllowed() {
 		return allowed;
 	}
 	public void setAllowed(boolean allowed) {
 		this.allowed = allowed;
 	}
 	public Company getCompany() {
 		return company;
 	}
 	public void setCompany(Company company) {
 		this.company = company;
 	}
 	public User getUser() {
 		return user;
 	}
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 }
