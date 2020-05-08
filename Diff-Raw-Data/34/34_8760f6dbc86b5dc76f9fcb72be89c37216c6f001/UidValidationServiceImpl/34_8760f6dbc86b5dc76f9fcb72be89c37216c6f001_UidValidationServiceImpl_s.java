 package com.scholastic.sbam.server.servlets;
 
 import java.util.List;
 
 import com.scholastic.sbam.client.services.UidValidationService;
 import com.scholastic.sbam.server.database.codegen.AuthMethod;
 import com.scholastic.sbam.server.database.objects.DbAuthMethod;
 import com.scholastic.sbam.server.database.util.HibernateUtil;
 import com.scholastic.sbam.shared.objects.AuthMethodInstance;
 import com.scholastic.sbam.shared.objects.MethodIdInstance;
 import com.scholastic.sbam.shared.security.SecurityManager;
 import com.scholastic.sbam.shared.util.AppConstants;
 import com.scholastic.sbam.shared.validation.AsyncValidationResponse;
 
 /**
  * This class performs a validation of a user id/password/proxy combination for a method instance (and agreement method or site method) to determine if there
  * are any conflicts, i.e. situations where this address conflicts with another address for a different use, site, or range.
  * 
  * In particular,
  * 
  * Passwords should match.
  * 
  * Proxy IDs should match.
  * 
  * Note that there are circumstances that are undesirable but allowed.  In particular, the same user ID may have existed in the past for another, 
  * expired agreement.  THIS IS NOT DESIREABLE, but it is allowed, primarily to provide backward compatibility to 
  * legacy data.
  * 
  * But these situations should be resolved manually by the users, and so alert messages are generated for the user through this service.
  */
 @SuppressWarnings("serial")
 public class UidValidationServiceImpl extends AuthenticatedServiceServlet implements UidValidationService {
 	
 	public static final int	SAME_SOURCE			=	0;		//	Both ranges belong to the same source (agreement, location, proxy)
 	public static final int	SAME_SOURCE_TYPE	=	1;		//	Both ranges belong to different sources of the same type (agreement, location, proxy)
 	public static final int	DIFF_SOURCE_TYPE	=	2;		//	Both ranges belong to different sources of different types (agreement, location, proxy)
 	
 	public static final int SAME_LOCATION		=	10;		//	Both ranges belong to the same location (i.e. site and location code)
 	public static final int DIFF_LOCATION_CODE	=	20;		//	Both ranges belong to the same location (i.e. site and location code)
 	public static final int DIFF_UCN_SUFFIX		=	30;		//	Both ranges belong to the same UCN, but a different location AND source
 	public static final int DIFF_UCN			=	40;		//	Both ranges belong to entirely different UCNs.
 
 	/**
 	 * Perform backend validation on a field value, given a previous data instance.
 	 * 
 	 * The 
 	 * @param ipLo
 	 * The low IP address.
 	 * @param ipHi
 	 * The high IP address
 	 * @param validationCounter
 	 * A counter used to synchronize a response with the most recent validation call (all responses previous to the current client value will be ignored).
 	 * @return
 	 * An AsyncValidationResponse method identifying the response through the validation counter, and including any error messages.
 	 * @throws Exception
 	 */
 	public AsyncValidationResponse validateUid(String uid, String password, char userType, int proxyId, MethodIdInstance methodId, final int validationCounter) throws Exception {
 
 		authenticate("validate user ID", SecurityManager.ROLE_QUERY);
 		
 		HibernateUtil.openSession();
 		HibernateUtil.startTransaction();
 
 		AsyncValidationResponse response = new AsyncValidationResponse(validationCounter);
 		try {
 			
 			authenticate();
 
 			doValidation(uid, password, userType, proxyId, methodId, response);
 
 		} catch (Exception exc) {
 			exc.printStackTrace();
 			throw exc;
 		}
 		
 		HibernateUtil.endTransaction();
 		HibernateUtil.closeSession();
 		
 		return response;
 	}
 
 	protected void doValidation(String uid, String password, char userType, int proxyId, MethodIdInstance methodId, AsyncValidationResponse response) {
 		
 		List<AuthMethod> authMethods  = DbAuthMethod.findByUid(uid);
 		
 		int otherAgreements = 0;
 		if (authMethods != null) {
 			for (AuthMethod authMethod : authMethods) {
 				AuthMethodInstance amInstance = DbAuthMethod.getInstance(authMethod);
 				//	Don't check a method against itself
 				if (methodId != null && methodId.sourceEquals(amInstance.obtainMethodId())) {
 					continue;
 				}
 				
 				otherAgreements += compare(uid, password, userType, proxyId, methodId, amInstance.getUserId(), amInstance.getPassword(), amInstance.getUserType(), amInstance.getProxyId(), amInstance.obtainMethodId(), response);
 			}
 		}
 		
 		//	One message counting valid assignments within other agreements
 		if (otherAgreements > 0)
 			response.getInfoMessages().add("This user ID exists on " + otherAgreements + " other agreement" +
 						(otherAgreements > 1?"s":"") + " consistent with this assignment.");
 	}
 	
 	public boolean stringEquals(String first, String second) {
 		if (first == null && second == null)
 			return true;
 		if (first == null)
 			return second.length() == 0;
 		if (second == null)
 			return first.length() == 0;
 		return first.equals(second);
 	}
 	
 	protected int compare(String uid, String password, char userType, int proxyId, MethodIdInstance validatedMethodId, String compareUid, String comparePassword, char compareUserType, int compareProxyId, MethodIdInstance compareMethodId, AsyncValidationResponse response) {
 		
 		int siteComparison		= -1;
 		int sourceComparison	= -1;
 		String sourceType		= "";
 //		String compareType		= "";
 		
 		if (validatedMethodId.getAgreementId() > 0)
 			sourceType = "agreement";
 		else if (validatedMethodId.getUcn() > 0)
 			sourceType = "site location";
 		else
 			sourceType = "proxy";	//	Can never happen... can't have UIDs on proxies
 
 //		if (compareMethodId.getAgreementId() > 0)
 //			compareType = "agreement";
 //		else if (compareMethodId.getUcn() > 0)
 //			compareType = "site location";
 //		else
 //			compareType = "proxy";
 		
 		if (validatedMethodId.sourceOwnerEquals(compareMethodId) )
 			sourceComparison = SAME_SOURCE;
 		else {
 			if (
 					 (validatedMethodId.getAgreementId() != 0 && compareMethodId.getAgreementId() != 0)
 				||   (validatedMethodId.getUcn()         != 0 && compareMethodId.getUcn()         != 0)
 				||   (validatedMethodId.getProxyId()     != 0 && compareMethodId.getProxyId()     != 0) 
 			)
 				sourceComparison = SAME_SOURCE_TYPE;
 			else
 				sourceComparison = DIFF_SOURCE_TYPE;
 		}
 		
 		if (validatedMethodId.getForUcn() != compareMethodId.getForUcn())
 			siteComparison = DIFF_UCN;
 		else if (validatedMethodId.getForUcnSuffix() != compareMethodId.getForUcnSuffix())
 			siteComparison = DIFF_UCN_SUFFIX;
 		else if (stringEquals(validatedMethodId.getForSiteLocCode(), validatedMethodId.getForSiteLocCode() ) )
 			siteComparison = SAME_LOCATION;
 		else
 			siteComparison = DIFF_LOCATION_CODE;
 		
 		if (proxyId < 0)
 			proxyId = 0;	// This was a deleted value if negative, saved for reference, so just zero it for comparison
 		if (compareProxyId < 0)
 			compareProxyId = 0;	// This was a deleted value if negative, saved for reference, so just zero it for comparison
 		
 		boolean samePassword = (password == null)? false : password.equals(comparePassword);
 		boolean sameUserType = userType == compareUserType;
 		boolean sameProxyId  = proxyId == compareProxyId;
 		boolean sameEverything = samePassword && sameUserType && sameProxyId;
 		
 		if (sameEverything
 		&&	siteComparison  == SAME_LOCATION
 		&&  sourceComparison== SAME_SOURCE_TYPE
 		&&  validatedMethodId.getAgreementId() > 0)
 			return 1;
 			
 		boolean isError = false;
 		StringBuffer msg = new StringBuffer();
 		
 		msg.append("This user ID ");
 		
 		if (sourceComparison == SAME_SOURCE) {
 				isError = true;
 				msg.append(" already exists on this ");
 				msg.append(sourceType);
 		} else if (compareMethodId.getAgreementId() > 0) {
 			msg.append(" exists on agreement ");
 			msg.append(AppConstants.appendCheckDigit(compareMethodId.getAgreementId()));
 		} else if (compareMethodId.getUcn() > 0) {
 			msg.append(" exists on site location ");
 			msg.append(compareMethodId.getUcn());
 			msg.append("-");
 			msg.append(compareMethodId.getUcnSuffix());
 			msg.append(" ");
 			msg.append(compareMethodId.getSiteLocCode());
 		} else {
 			msg.append(" exists elsewhere");
 		}
 		
 		if (!samePassword) {
 			msg.append(" with the password ");
 			msg.append(comparePassword);
 		}
 		if (!sameProxyId) {
 			if (samePassword)
 				msg.append(" with");
 			else
 				msg.append(" and");
 			msg.append(" proxy ");
 			msg.append(AppConstants.appendCheckDigit(compareProxyId));
 		}
 		if (!sameUserType) {
 			if (!samePassword || !sameProxyId)
 				msg.append(" and");
			msg.append(" is a ");
			msg.append(AuthMethodInstance.getUserTypeDescription(compareUserType).toLowerCase());
 		}
 		
 		if (siteComparison == SAME_LOCATION) {
 			if (validatedMethodId.getForUcn() == 0)
 				msg.append(".");
 			else
 				msg.append(" for this same location.");
 		} else if (siteComparison == DIFF_LOCATION_CODE) {
 			msg.append(" for location code ");
 			msg.append(compareMethodId.getForSiteLocCode());
 			msg.append(".");
 		} else if (siteComparison == DIFF_UCN_SUFFIX) {
 			msg.append(" for a different Global ID.");
 		} else {
 			if (compareMethodId.getForUcn() > 0) {
 				msg.append(" for UCN ");
 				msg.append(compareMethodId.getForUcn());
 				msg.append(".");
 			} else if (validatedMethodId.getForUcn() > 0 && compareMethodId.getAgreementId() > 0 && compareMethodId.getForUcn() == 0) {
 				msg.append(" with no UCN.");
 			} else {
 				msg.append(".");
 			}
 		}
 		
 		if (isError)
 			response.getMessages().add(msg.toString());
 		else
 			response.getAlertMessages().add(msg.toString());
 		
 		return 0;
 	}
 }
