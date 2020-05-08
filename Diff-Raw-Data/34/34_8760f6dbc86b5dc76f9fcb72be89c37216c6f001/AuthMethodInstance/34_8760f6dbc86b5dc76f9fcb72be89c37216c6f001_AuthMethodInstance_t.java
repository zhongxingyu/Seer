 package com.scholastic.sbam.shared.objects;
 
 import java.util.Date;
 
 import com.extjs.gxt.ui.client.data.BeanModel;
 import com.extjs.gxt.ui.client.data.BeanModelFactory;
 import com.extjs.gxt.ui.client.data.BeanModelLookup;
 import com.extjs.gxt.ui.client.data.BeanModelTag;
 import com.google.gwt.user.client.rpc.IsSerializable;
 import com.scholastic.sbam.shared.util.AppConstants;
 
 public class AuthMethodInstance extends IpAddressInstance implements BeanModelTag, IsSerializable {
 	
 	private static BeanModelFactory beanModelfactory;
 	
 	public static enum UserTypes {
		COOKIE ("C","Cookie", "a cookie"),
		PUP("P", "Permanent", "a pup");
 		private String code;
 		private String name;
		private String indefiniteNoun;
		UserTypes(String code, String name, String indefiniteNoun) {
 			this.code = code;
 			this.name = name;
			this.indefiniteNoun = indefiniteNoun;
 		}
 		public String getCode() {
 			return code;
 		}
 		public String getName() {
 			return name;
 		}
		public String getIndefiniteNoun() {
			return indefiniteNoun;
		}
 	}
 	
 	public static final String AM_IP 	= MethodIdInstance.AM_IP;
 	public static final String AM_URL	= MethodIdInstance.AM_URL;
 	public static final String AM_UID	= MethodIdInstance.AM_UID;
 
 	private int		agreementId;
 	private int		ucn;
 	private int		ucnSuffix;
 	private String	siteLocCode;
 	
 	private String	methodType;
 	private int		methodKey;
 
 	private int		forUcn;
 	private int		forUcnSuffix;
 	private String	forSiteLocCode;
 	
 	private String	url;
 	private	String	userId;
 	private String	password;
 	private char	userType;
 	private long	ipLo;
 	private long	ipHi;
 	
 	private String	ipRangeCode;
 	
 	private int		proxyId;
 	
 	private char	remote;
 	private char	approved;
 	private char	validated;
 	private char	activated;
 
 	private Date	activatedDatetime;
 	private Date	deactivatedDatetime;
 	private Date	reactivatedDatetime;
 	
 	private String	orgPath;
 	
 	private String	note;
 
 	private char	status;
 	private Date	createdDatetime;
 	private Date	updatedDatetime;
 	
 	private ProxyInstance		proxy;
 	private GenericCodeInstance methodTypeInstance;
 	private SiteInstance site;
 	
 	@Override
 	public void markForDeletion() {
 		setStatus('X');
 	}
 
 	@Override
 	public boolean thisIsDeleted() {
 		return status == 'X';
 	}
 
 	@Override
 	public boolean thisIsValid() {
 		return true;
 	}
 
 	@Override
 	public String returnTriggerProperty() {
 		return "junk";
 	}
 
 	@Override
 	public String returnTriggerValue() {
 		return "junk";
 	}
 
 	public char getStatus() {
 		return status;
 	}
 
 	public void setStatus(char status) {
 		this.status = status;
 	}
 
 	public Date getCreatedDatetime() {
 		return createdDatetime;
 	}
 
 	public void setCreatedDatetime(Date createdDatetime) {
 		this.createdDatetime = createdDatetime;
 	}
 
 	public int getAgreementId() {
 		return agreementId;
 	}
 
 	public void setAgreementId(int agreementId) {
 		this.agreementId = agreementId;
 	}
 
 	public int getUcn() {
 		return ucn;
 	}
 
 	public void setUcn(int ucn) {
 		this.ucn = ucn;
 	}
 
 	public int getUcnSuffix() {
 		return ucnSuffix;
 	}
 
 	public void setUcnSuffix(int ucnSuffix) {
 		this.ucnSuffix = ucnSuffix;
 	}
 
 	public String getSiteLocCode() {
 		return siteLocCode;
 	}
 
 	public void setSiteLocCode(String siteLocCode) {
 		this.siteLocCode = siteLocCode;
 	}
 
 	public String getMethodType() {
 		return methodType;
 	}
 
 	public void setMethodType(String methodType) {
 		this.methodType = methodType;
 		methodTypeInstance = new GenericCodeInstance(methodType, methodType.toUpperCase());
 	}
 
 	public int getMethodKey() {
 		return methodKey;
 	}
 
 	public void setMethodKey(int methodKey) {
 		this.methodKey = methodKey;
 	}
 	
 	public void syncMethodType() {
 		if (ipLo > 0)
 			methodType = AM_IP;
 		else if (url != null && url.length() > 0)
 			methodType = AM_URL;
 		else
 			methodType = AM_UID;
 	}
 
 	public int getForUcn() {
 		return forUcn;
 	}
 
 	public void setForUcn(int forUcn) {
 		this.forUcn = forUcn;
 	}
 
 	public int getForUcnSuffix() {
 		return forUcnSuffix;
 	}
 
 	public void setForUcnSuffix(int forUcnSuffix) {
 		this.forUcnSuffix = forUcnSuffix;
 	}
 
 	public String getForSiteLocCode() {
 		return forSiteLocCode;
 	}
 
 	public void setForSiteLocCode(String forSiteLocCode) {
 		this.forSiteLocCode = forSiteLocCode;
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 //		syncMethodKey();
 	}
 
 	public String getUserId() {
 		return userId;
 //		syncMethodKey();
 	}
 
 	public void setUserId(String userId) {
 		this.userId = userId;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public char getUserType() {
 		return userType;
 	}
 	
 	public boolean isUserType(UserTypes type) {
 		return (type.getCode().charAt(0) == userType);
 	}
 
 	public void setUserType(char userType) {
 		this.userType = userType;
 	}
 
 	public long getIpLo() {
 		return ipLo;
 	}
 
 	public void setIpLo(long ipLo) {
 		this.ipLo = ipLo;
 		//	This is optimized to not bother computing the range if it has already been set, or both IPs are not yet set
 		if (this.ipLo != 0 && this.ipHi != 0 && (this.ipRangeCode == null || this.ipRangeCode.length() == 0) )
 			assignIpRangeCode();
 //		syncMethodKey();
 	}
 
 	public long getIpHi() {
 		return ipHi;
 	}
 
 	public void setIpHi(long ipHi) {
 		this.ipHi = ipHi;
 		//	This is optimized to not bother computing the range if it has already been set, or both IPs are not yet set
 		if (this.ipLo != 0 && this.ipHi != 0 && (this.ipRangeCode == null || this.ipRangeCode.length() == 0) )
 			assignIpRangeCode();
 //		syncMethodKey();
 	}
 
 	public String getIpRangeCode() {
 		return ipRangeCode;
 	}
 	
 	public void assignIpRangeCode() {
 		ipRangeCode = getCommonIpRangeCode(ipLo, ipHi);
 	}
 
 	public void setIpRangeCode(String ipRangeCode) {
 		this.ipRangeCode = ipRangeCode;
 	}
 
 	public int getProxyId() {
 		return proxyId;
 	}
 
 	public void setProxyId(int proxyId) {
 		this.proxyId = proxyId;
 	}
 
 	public char getRemote() {
 		return remote;
 	}
 	
 	public boolean isRemote() {
 		return (remote == 'y' || remote == 'Y');
 	}
 
 	public void setRemote(char remote) {
 		this.remote = remote;
 	}
 
 	public char getApproved() {
 		return approved;
 	}
 
 	public void setApproved(char approved) {
 		this.approved = approved;
 	}
 	
 	public boolean isApproved() {
 		return (approved == 'y' || approved == 'Y');
 	}
 
 	public char getValidated() {
 		return validated;
 	}
 	
 	public boolean isValidated() {
 		return (validated == 'y' || validated == 'Y');
 	}
 
 	public void setValidated(char validated) {
 		this.validated = validated;
 	}
 
 	public char getActivated() {
 		return activated;
 	}
 	
 	public boolean isActivated() {
 		return (activated == 'y' || activated == 'Y');
 	}
 
 	public void setActivated(char activated) {
 		this.activated = activated;
 	}
 
 	public Date getActivatedDatetime() {
 		return activatedDatetime;
 	}
 
 	public void setActivatedDatetime(Date activatedDatetime) {
 		this.activatedDatetime = activatedDatetime;
 	}
 
 	public Date getDeactivatedDatetime() {
 		return deactivatedDatetime;
 	}
 
 	public void setDeactivatedDatetime(Date deactivatedDatetime) {
 		this.deactivatedDatetime = deactivatedDatetime;
 	}
 
 	public Date getReactivatedDatetime() {
 		return reactivatedDatetime;
 	}
 
 	public void setReactivatedDatetime(Date reactivatedDatetime) {
 		this.reactivatedDatetime = reactivatedDatetime;
 	}
 
 	public Date getUpdatedDatetime() {
 		return updatedDatetime;
 	}
 
 	public void setUpdatedDatetime(Date updatedDatetime) {
 		this.updatedDatetime = updatedDatetime;
 	}
 
 	public String getOrgPath() {
 		return orgPath;
 	}
 
 	public void setOrgPath(String orgPath) {
 		this.orgPath = orgPath;
 	}
 
 	public String getNote() {
 		return note;
 	}
 
 	public void setNote(String note) {
 		this.note = note;
 	}
 
 	public GenericCodeInstance getMethodTypeInstance() {
 		return methodTypeInstance;
 	}
 
 	public void setMethodTypeInstance(GenericCodeInstance methodTypeInstance) {
 		this.methodTypeInstance = methodTypeInstance;
 	}
 
 	public ProxyInstance getProxy() {
 		return proxy;
 	}
 
 	public void setProxy(ProxyInstance proxy) {
 		this.proxy = proxy;
 		if (proxy != null)
 			this.proxyId = proxy.getProxyId();
 	}
 
 	public SiteInstance getSite() {
 		return site;
 	}
 
 	public void setSite(SiteInstance site) {
 		this.site = site;
 		this.setForUcn(site.getUcn());
 		this.setForUcnSuffix(site.getUcnSuffix());
 		this.setForSiteLocCode(site.getSiteLocCode());
 	}
 	
 	public String getStatusDescription() {
 		return AppConstants.getStatusDescription(status);
 	}
 	
 	public String getMethodDisplay() {
 		if (AM_IP.equals(methodType)) {
 			return getBriefIpDisplay(ipLo, ipHi);
 //			if (ipHi == 0 || ipHi == ipLo)
 //				return getOctetForm(ipLo);
 //			if (ipLo == 0)
 //				return getOctetForm(ipHi);
 //			return getOctetForm(ipLo) + " - " + getOctetForm(ipHi);
 		} else if (AM_UID.equals(methodType)) {
 			return userId + " / " + password;
 		} else if (AM_URL.equals(methodType)) {
 			return url;
 		}
 		return "Unrecognized method type " + methodType;
 	}
 	
 	public String getIpLoDisplay() {
 		if (AM_IP.equals(methodType)) {
 			if (ipLo == 0)
 				return getOctetForm(ipHi);
 			return getOctetForm(ipLo);
 		}
 		return "";
 	}
 	
 	public String getIpHiDisplay() {
 		if (AM_IP.equals(methodType)) {
 			if (ipHi== 0)
 				return getOctetForm(ipLo);
 			return getOctetForm(ipHi);
 		}
 		return "";
 	}
 	
 	public MethodIdInstance obtainMethodId() {
 		MethodIdInstance mid = new MethodIdInstance();
 		mid.setAgreementId(agreementId);
 		mid.setUcn(ucn);
 		mid.setUcnSuffix(ucnSuffix);
 		mid.setSiteLocCode(siteLocCode);
 		mid.setMethodType(methodType);
 		mid.setMethodKey(methodKey);
 		mid.setForUcn(forUcn);
 		mid.setForUcnSuffix(forUcnSuffix);
 		mid.setForSiteLocCode(forSiteLocCode);
 		mid.setProxyId(0);
 		mid.setIpId(0);
 		return mid;
 	}
 	
 	public static String getUserTypeDescription(char userTypeCode) {
 		for (UserTypes userType : UserTypes.values() )
			if (userType.getCode().charAt(0) == userTypeCode)
 				return userType.getName();
 		return "Unknown";
 	}
 	
	public static String getUserTypeNoun(char userTypeCode) {
		for (UserTypes userType : UserTypes.values() )
			if (userType.getCode().charAt(0) == userTypeCode)
				return userType.getIndefiniteNoun();
		return "an unknown type";
	}
	
 	public void setValuesFrom(AuthMethodInstance fromInstance) {
 		this.agreementId				=	fromInstance.agreementId;
 		this.ucn						=	fromInstance.ucn;
 		this.ucnSuffix					=	fromInstance.ucnSuffix;
 		this.siteLocCode				=	fromInstance.siteLocCode;
 		
 		this.methodType					=	fromInstance.	methodType;
 		this.methodKey					=	fromInstance.	methodKey;
 		
 		this.forUcn						=	fromInstance.	forUcn;
 		this.forUcnSuffix				=	fromInstance.	forUcnSuffix;
 		this.forSiteLocCode				=	fromInstance.	forSiteLocCode;
 			
 		this.url						=	fromInstance.	url;
 		this.userId						=	fromInstance.	userId;
 		this.password					=	fromInstance.	password;
 		this.userType					=	fromInstance.	userType;
 		this.ipLo						=	fromInstance.	ipLo;
 		this.ipHi						=	fromInstance.	ipHi;
 		this.ipRangeCode				=	fromInstance.	ipRangeCode;
 			
 		this.proxyId					=	fromInstance.	proxyId;
 			
 		this.remote						=	fromInstance.	remote;
 		this.approved					=	fromInstance.	approved;
 		this.validated					=	fromInstance.	validated;
 		this.activated					=	fromInstance.	activated;
 	
 		this.activatedDatetime			=	fromInstance.	activatedDatetime;
 		this.deactivatedDatetime		=	fromInstance.	deactivatedDatetime;
 		this.reactivatedDatetime		=	fromInstance.	reactivatedDatetime;
 			
 		this.orgPath					=	fromInstance.	orgPath;
 			
 		this.note						=	fromInstance.	note;
 	
 		this.status						=	fromInstance.	status;
 		this.createdDatetime			=	fromInstance.	createdDatetime;
 		this.updatedDatetime			=	fromInstance.	updatedDatetime;
 		
 		this.site						=	fromInstance.	site;
 	}
 	
 	public String getUniqueKey() {
 		return agreementId + ":" + ucn + ":" + ucnSuffix + ":" + siteLocCode + ":" + methodType + ":" + methodKey;
 	}
 
 	public static BeanModel obtainModel(AuthMethodInstance instance) {
 		if (beanModelfactory == null)
 			beanModelfactory  = BeanModelLookup.get().getFactory(AuthMethodInstance.class);
 		BeanModel model = beanModelfactory.createModel(instance);
 		return model;
 	}
 
 	public String toString() {
 		return getUniqueKey();
 	}
 }
