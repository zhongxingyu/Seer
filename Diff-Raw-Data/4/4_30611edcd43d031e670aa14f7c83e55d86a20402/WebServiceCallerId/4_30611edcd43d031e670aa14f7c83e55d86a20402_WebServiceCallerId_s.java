 package org.cagrid.gaards.authentication;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.ws.WebServiceContext;
 import javax.xml.ws.handler.MessageContext;
 
 import org.cagrid.gaards.pki.CertUtil;
 
 public class WebServiceCallerId {
 
 	public static String getCallerId(WebServiceContext wsContext) {
 		String callerId = null;
 		if (wsContext == null)
 			return callerId;
 		MessageContext mContext = wsContext.getMessageContext();
 		if (mContext == null)
 			return callerId;
 		HttpServletRequest servletRequest = (HttpServletRequest) mContext.get(MessageContext.SERVLET_REQUEST);
 		if (servletRequest == null)
 			return callerId;
 		java.security.cert.X509Certificate[] certs = (java.security.cert.X509Certificate[]) servletRequest.getAttribute("javax.servlet.request.X509Certificate");
 		if ((certs == null) || (certs.length == 0)) {
 			return callerId;
 		}
		String dn = certs[0].getSubjectDN().getName();
 		StringBuffer sb = new StringBuffer();
 		int index = dn.lastIndexOf(",");
 		while (index != -1) {
 			String str = dn.substring(index + 1).trim() + ",";
 			sb.append(str);
 			dn = dn.substring(0, index);
 			index = dn.lastIndexOf(",");
 		}
 		sb.append(dn);
 		callerId = CertUtil.subjectToIdentity(sb.toString());
 		return callerId;
 	}
 }
