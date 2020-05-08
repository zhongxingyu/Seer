 package gov.nih.nci.cadsr.cdecurate.tool.tags;
 
 
 import gov.nih.nci.cadsr.cdecurate.util.CurationToolProperties;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.tagext.TagSupport;
 
 import org.jboss.Version;
 
 
 public class FooterTag extends TagSupport {
 
 	/**This JSP tag library class generates footer
 	 * 
 	 */
 	private static final long serialVersionUID = 2566181324552741768L;
 
 	public int doEndTag() throws JspException{
 		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
 		JspWriter NCIFooter = this.pageContext.getOut();
 		String jbossVersion = Version.getInstance().getMajor() + "." + Version.getInstance().getMinor() + "." + Version.getInstance().getRevision();
 		try {
 			NCIFooter.println("<div class=\"xyz\">"
 							+ "<table class=\"footerBanner1\" cellspacing=\"0\" cellpadding=\"0\">"
 							+ "<col /><col /><col />"
 							+ "<tr><td class=\"footerItem\" align=\"left\"><span CLASS=\"footerItemVer\">"+CurationToolProperties.getFactory().getProperty("curationtool.version")+"&nbsp;("+jbossVersion+"/"+System.getProperty("java.version")+")</span></td>"
 							+ "<td class=\"footerItem\" align=\"center\"><span CLASS=\"footerItemSep\">|</span><span CLASS=\"footerItemNormal\"  onmouseover=\"this.className = 'footerItemFocus';\" "
 							+ " onmouseout=\"this.className = 'footerItemNormal';\" "
 							+ "onclick=\"window.open('http://ncicb.nci.nih.gov/NCICB/about/contact_us', '_blank');\">"
 							+ "CONTACT US"
 							+ "</span><span CLASS=\"footerItemSep\">|</span><span CLASS=\"footerItemNormal\""
 							+ "onmouseover=\"this.className = 'footerItemFocus';\""
 							+ "onmouseout=\"this.className = 'footerItemNormal';\""
 							+ "onclick=\"window.open('http://www.nih.gov/about/privacy.htm', '_blank');\">"
 							+ "PRIVACY NOTICE"
 							+ "</span><span CLASS=\"footerItemSep\">|</span><span CLASS=\"footerItemNormal\""
 							+ "onmouseover=\"this.className = 'footerItemFocus';\""
 							+ "onmouseout=\"this.className = 'footerItemNormal';\""
 							+ "onclick=\"window.open('http://www.nih.gov/about/disclaim.htm', '_blank');\">"
 							+ "DISCLAIMER"
 							+ "</span><span CLASS=\"footerItemSep\">|</span><span CLASS=\"footerItemNormal\""
 							+ " onmouseover=\"this.className = 'footerItemFocus';\""
 							+ "onmouseout=\"this.className = 'footerItemNormal';\""
 							+ "onclick=\"window.open('http://www3.cancer.gov/accessibility/nci508.htm', '_blank');\">"
 							+ "ACCESSIBILITY"
 							+ "</span><span CLASS=\"footerItemSep\">|</span><span CLASS=\"footerItemNormal\""
 							+ "onmouseover=\"this.className = 'footerItemFocus';\""
 							+ "onmouseout=\"this.className = 'footerItemNormal';\""
 							+ "onclick=\"window.open('http://ncicb.nci.nih.gov/NCICB/support', '_blank');\">"
 							+ " APPLICATION SUPPORT"
 							+ "</span><span CLASS=\"footerItemSep\">|</span><span CLASS=\"footerItemNormal\""
 							+ "onmouseover=\"this.className = 'footerItemFocus';\""
 							+ "onmouseout=\"this.className = 'footerItemNormal';\""
							+ "onclick=\"window.open('https://cdecurate-stage2.nci.nih.gov/help/', '_blank');\">"
 							+ "  HELP"
 							+ "</span><span CLASS=\"footerItemSep\">|</span>"
 							+ "</td>"
 							+ "<td class=\"footerItem\" align=\"right\"><span CLASS=\"footerItemVer\">"+CurationToolProperties.getFactory().getProperty("curationtool.version")+"&nbsp;("+jbossVersion+"/"+System.getProperty("java.version")+")</span></td>"
 							+ "</tr>"
 							+ "</table>"
 							+"<div class=\"footerBanner2\">"
 			                +"<a href=\"http://www.cancer.gov/\""
 			                +"target=\"_blank\"><img src=\""+ request.getContextPath() +"/images/footer_nci.gif\"  width=\"63\" height=\"31\" alt=\"National Cancer Institute\"  border=\"0\"/></a>"
 			                +"<a href=\"http://www.dhhs.gov/\"  target=\"_blank\"><img src=\""+ request.getContextPath() +"/images/footer_hhs.gif\"  width=\"39\" height=\"31\" alt=\"Department of Health and Human Services\"  border=\"0\"/></a>"
 			                +"<a href=\"http://www.nih.gov/\" target=\"_blank\"><img src=\""+ request.getContextPath() +"/images/footer_nih.gif\"   width=\"46\" height=\"31\" alt=\"National Institutes of Health\"  border=\"0\"/></a>"
 			                +"<a href=\"http://www.firstgov.gov/\"  target=\"_blank\"><img  src=\""+ request.getContextPath() +"/images/footer_firstgov.gif\" width=\"91\" height=\"31\" alt=\"FirstGov.gov\" border=\"0\"/></a>"
 			                +"</div></div>");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return EVAL_PAGE;
 
 	}
 }
