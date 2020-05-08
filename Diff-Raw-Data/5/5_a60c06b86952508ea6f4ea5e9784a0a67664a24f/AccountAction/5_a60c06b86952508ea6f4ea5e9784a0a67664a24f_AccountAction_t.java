 package com.action;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 
 
 import com.dao.SysuserDao;
 import com.opensymphony.xwork2.ActionSupport;
 import com.pojo.Sysuser;
 import com.pojo.Transaction;
 
 
 public class AccountAction extends ActionSupport {
 	public Integer userId;
 	private ArrayList<Sysuser> users;
 	public Sysuser user;
 	public double cash;
 	private String cashString;
 	private int isSuccess = 0;
 	public String optionC = "";
 	public String optionCOrd = "";
 	public String optionE = "";
 	public String optionEOrd = "";
 	public String searchKeyC;
 	public String searchKeyE;
 
 
 
 	public String customerlist() {
 		String searchBy = null;
 		String orderBy = null;
 		boolean isAsc = true;
 		if (optionC.equals("username")) {
 			searchBy = SysuserDao.USERNAME;
 		}
 		if (optionC.equals("firstname")) {
 			searchBy = SysuserDao.FIRSTNAME;
 		}
 		if (optionC.equals("lastname")) {
 			searchBy = SysuserDao.LASTNAME;
 		}
 		if (optionCOrd.equals("usernameA")) {
 			orderBy = SysuserDao.USERNAME;
 			isAsc = true;
 		}
 		if (optionCOrd.equals("usernameD")) {
 			orderBy = SysuserDao.USERNAME;
 			isAsc = false;
 		}
 		if (optionCOrd.equals("firstnameA")) {
 			orderBy = SysuserDao.FIRSTNAME;
 			isAsc = true;
 		}
 		if (optionCOrd.equals("firstnameD")) {
 			orderBy = SysuserDao.FIRSTNAME;
 			isAsc = false;
 		}
 		if (optionCOrd.equals("lastnameA")) {
 			orderBy = SysuserDao.LASTNAME;
 			isAsc = true;
 		}
 		if(optionCOrd.equals("lastnameD")){
 			orderBy = SysuserDao.LASTNAME;
 			isAsc = false;
 		}
 		if(searchKeyC != null){
 			searchKeyC = searchKeyC.trim();
 		}
 		try {
 			this.users = SysuserDao.getInstance().getUser(searchBy, orderBy, isAsc, searchKeyC, Sysuser.USER_TYPE_COSTOMER);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		if (this.users == null) {
 			this.users = new ArrayList<Sysuser>();
 		}
 		return SUCCESS;
 	}
 	public String employeelist() {
 		String searchBy = null;
 		String orderBy = null;
 		boolean isAsc = true;
 		if(optionE.equals("username")){
 			searchBy = SysuserDao.USERNAME;
 		}
 		if(optionE.equals("firstname")){
 			searchBy = SysuserDao.FIRSTNAME;
 		}
 		if(optionE.equals("lastname")){
 			searchBy = SysuserDao.LASTNAME;
 		}
 		if(optionE.equals("default")){
 			searchBy = SysuserDao.USERNAME;
 		}
 		if(optionEOrd.equals("usernameA")){
 			orderBy = SysuserDao.USERNAME;
 			isAsc = true;
 		}
 		if(optionEOrd.equals("usernameD")){
 			orderBy = SysuserDao.USERNAME;
 			isAsc = false;
 		}
 		if(optionEOrd.equals("firstnameA")){
 			orderBy = SysuserDao.FIRSTNAME;
 			isAsc = true;
 		}
 		if(optionEOrd.equals("firstnameD")){
 			orderBy = SysuserDao.FIRSTNAME;
 			isAsc = false;
 		}
 		if(optionEOrd.equals("lastnameA")){
 			orderBy = SysuserDao.LASTNAME;
 			isAsc = true;
 		}
 		if(optionEOrd.equals("lastnameD")){
 			orderBy = SysuserDao.LASTNAME;
 			isAsc = false;
 		}	
 		if(searchKeyE != null){
 			searchKeyE = searchKeyE.trim();
 		}
 		try {
 			this.users = SysuserDao.getInstance().getUser(searchBy, orderBy, isAsc, searchKeyE, Sysuser.USER_TYPE_EMPLOYEE);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (this.users == null) {
 			this.users = new ArrayList<Sysuser>();
 		}
 		return SUCCESS;
 	}
 	public String createCustomer() {
 		isSuccess = 0;
 		if(user != null){	
 			if (user.getUsername() != null && user.getFirstname() != null && user.getLastname() != null) {
 				user.setUsername(user.getUsername().trim());
 				user.setFirstname(user.getFirstname().trim());
 				user.setLastname(user.getLastname().trim());
 				cashString.trim();
 				if(user.getUsername().length() > 18 ){
 					this.addActionError("Username can't be more than 18 characters");
 					isSuccess = -1;
 					return ERROR;
 				}
 				if(user.getFirstname().length() > 18){
 					this.addActionError("Firstname can't be more than 18 characters");
 					isSuccess = -1;
 					return ERROR;
 				}
 				if(user.getLastname().length() >18 ){
 					this.addActionError("Lastname can't be more than 18 characters");
 					isSuccess = -1;
 					return ERROR;
 				}
 				if(!user.getUsername().matches("[a-zA-Z0-9]*") || !user.getFirstname().matches("[a-zA-Z]*") || !user.getLastname().matches("[a-zA-Z]*")){
 					this.addActionError("Username must only contain characters a-z and numbers,Firstname and Lastname only contains characters!");
 					isSuccess = -1;
 					return ERROR;
 				}
 				if(!user.getUsername().equals("") && !user.getFirstname().equals("") && !user.getLastname().equals("") && !cashString.equals("")){
 					if (!checkCashFormat(cashString, 9, 2)){
						this.addActionError("Cash Fomat Incorrect! 1.Cash amount should less than 1,000,000,000; 2.Must be a number with no more than 2 decimals");
 						isSuccess = -1;
 						return ERROR;
 					}else cash = Double.parseDouble(cashString);
 					if (checkRequired(this.user)) {
 						this.addActionError("This username has already exist!");
 						isSuccess = -1;
 						return ERROR;
 					}
 					if(user.getAddrLine1() != null)
 						user.getAddrLine1().trim();
 					if(user.getAddrLine2() != null)
 						user.getAddrLine2().trim();
 					if(user.getCity() != null)
 						user.getCity().trim();
 					if(user.getState() != null)
 						user.getState().trim();
 					if(user.getZip() != null)
 						user.getZip().trim();
 					user.setPassword(AuthorizationFilter.MD5("111111"));
 					user.setCash(Math.round(cash*100));
 					user.setType(0);
 					try {
 						SysuserDao.getInstance().create(user);
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					isSuccess = 1;
 					return SUCCESS;
 				}
 				this.addActionError("Important information shouldn't be empty!");
 				isSuccess = -1;
 				return ERROR;
 			}
 			this.addActionError("Important information shouldn't be empty!");
 			isSuccess = -1;
 			return ERROR;
 		}
 		else return ERROR;
 	}
 
 	public String createEmployee() {
 		isSuccess = 0;
 		if(user != null){
 			if (user.getUsername() != null && user.getFirstname() != null && user.getLastname() != null) {
 				user.setUsername(user.getUsername().trim());
 				user.setFirstname(user.getFirstname().trim());
 				user.setLastname(user.getLastname().trim());
 				if(user.getUsername().length() > 18 ){
 					this.addActionError("Username can't be more than 18 characters");
 					isSuccess = -1;
 					return ERROR;
 				}
 				if(user.getFirstname().length() > 18){
 					this.addActionError("Firstname can't be more than 18 characters");
 					isSuccess = -1;
 					return ERROR;
 				}
 				if(user.getLastname().length() >18 ){
 					this.addActionError("Lastname can't be more than 18 characters");
 					isSuccess = -1;
 					return ERROR;
 				}
 				if(!user.getUsername().matches("[a-zA-Z0-9]*") || !user.getFirstname().matches("[a-zA-Z]*") || !user.getLastname().matches("[a-zA-Z]*")){
					this.addActionError("Username must only contain characters a-z and numbers, Firstname and Lastname only contains characters!");
 					isSuccess = -1;
 					return ERROR;
 				}
 				if(!user.getUsername().equals("") && !user.getFirstname().equals("") && !user.getLastname().equals("")){
 					if (checkRequired(this.user)) {
 						this.addActionError("This username has already exist!");
 						isSuccess = -1;
 						return ERROR;
 					}
 					user.setPassword(AuthorizationFilter.MD5("111111"));
 					user.setAddrLine1("");
 					user.setAddrLine2("");
 					user.setCash(0);
 					user.setCity("");
 					user.setState("");
 					user.setZip("");
 					user.setType(1);
 					try {
 						SysuserDao.getInstance().create(user);
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					isSuccess = 1;
 					return SUCCESS;
 				}
 				this.addActionError("Important information shouldn't be empty!");
 				isSuccess = -1;
 				return ERROR;
 			}
 			this.addActionError("Important information shouldn't be empty!");
 			isSuccess = -1;
 			return ERROR;
 		}
 		else return ERROR;
 	}
 	
 	private boolean checkRequired(Sysuser u) {
 		Sysuser finduser = null;
 		try {
 			finduser = SysuserDao.getInstance().getByUsername(user.getUsername());
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if(finduser != null) return true;
 		else return false;
 	}
 	private static boolean checkCashFormat(String cashString, int beforeD, int afterD){
 			int i, j, flag = 0, loopTime = 0, flag2 = 0;
 			StringBuffer cashCheckZero = new StringBuffer();
 			for(j = 0 ; j < cashString.length() - 1; j++){
 				if(cashString.charAt(j) == '0' && flag2 == 0 && cashString.charAt(j+1) != '.');
 				else {
 					flag2 = 1;
 					cashCheckZero.append(cashString.charAt(j));
 				}
 			}
 			cashCheckZero.append(cashString.charAt(j));
 			cashString = cashCheckZero.toString();
 			for(i = 0; i < cashString.length(); i ++){
 				int asc = cashString.charAt(i);
 				if(i == 0){
 					if(asc < 48 || asc > 57) return false;
 				}
 				if((asc < 48 || asc > 57) && asc != 46) return false;
 				if(asc == 46){
 					flag = 1;
 					break;
 				}
 			}
 			
 			if(i > beforeD) return false;
 			for(i ++; i < cashString.length() && flag == 1; ){
 				int asc = cashString.charAt(i);
 				if(asc < 48 || asc > 57) return false;
 				i++;
 				loopTime ++;
 			}
 			if(loopTime > afterD) return false;
 			return true;
 	}
 	
 	public String viewAccount(){			//just take out a user Instance by ID
 		System.out.println();
 		System.out.println();
 		try {
 			this.user = SysuserDao.getInstance().getByUserId(userId);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return SUCCESS;
 	}
 	
 	public String resetPassword(){
 		try {
 			this.user = SysuserDao.getInstance().getByUserId(userId);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		user.setPassword(AuthorizationFilter.MD5("111111"));
 		try {
 			SysuserDao.getInstance().update(user);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		isSuccess = 2;
 		return SUCCESS;
 	}
 	
 	
 	
 	
 	public int getIsSuccess() {
 		return isSuccess;
 	}
 
 	public void setIsSuccess(int isSuccess) {
 		this.isSuccess = isSuccess;
 	}
 
 	public Integer getUserId() {
 		return userId;
 	}
 
 	public void setUserId(Integer userId) {
 		this.userId = userId;
 	}
 	
 	public ArrayList<Sysuser> getUsers() {
 		return users;
 	}
 
 	public void setUsers(ArrayList<Sysuser> users) {
 		this.users = users;
 	}
 	
 	public Sysuser getUser() {
 		return user;
 	}
 
 	public void setUser(Sysuser user) {
 		this.user = user;
 	}
 	public void setCash(double cash) {
 		this.cash = cash;
 	}
 
 	public void setOptionC(String optionC) {
 		this.optionC = optionC;
 	}
 
 	public void setSearchKeyC(String searchKeyC) {
 		this.searchKeyC = searchKeyC;
 	}
 	public String getCashString() {
 		return cashString;
 	}
 	public void setCashString(String cashString) {
 		this.cashString = cashString;
 	}
 }
