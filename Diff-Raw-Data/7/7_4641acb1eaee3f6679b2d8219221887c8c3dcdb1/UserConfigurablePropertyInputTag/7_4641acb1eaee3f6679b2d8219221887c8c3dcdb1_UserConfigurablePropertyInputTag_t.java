 /*
$Revision: 1.2 $
$Date: 2007-09-08 18:28:03 $
 
 The Web CGH Software License, Version 1.0
 
 Copyright 2003 RTI. This software was developed in conjunction with the
 National Cancer Institute, and so to the extent government employees are
 co-authors, any rights in such works shall be subject to Title 17 of the
 United States Code, section 105.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 
 1. Redistributions of source code must retain the above copyright notice, this 
 list of conditions and the disclaimer of Article 3, below. Redistributions in 
 binary form must reproduce the above copyright notice, this list of conditions 
 and the following disclaimer in the documentation and/or other materials 
 provided with the distribution.
 
 2. The end-user documentation included with the redistribution, if any, must 
 include the following acknowledgment:
 
 "This product includes software developed by the RTI and the National Cancer 
 Institute."
 
 If no such end-user documentation is to be included, this acknowledgment shall 
 appear in the software itself, wherever such third-party acknowledgments 
 normally appear.
 
 3. The names "The National Cancer Institute", "NCI", 
 Research Triangle Institute, and "RTI" must not be used to endorse or promote 
 products derived from this software.
 
 4. This license does not authorize the incorporation of this software into any 
 proprietary programs. This license does not authorize the recipient to use any 
 trademarks owned by either NCI or RTI.
 
 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, 
 (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE
 NATIONAL CANCER INSTITUTE, RTI, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 package org.rti.webgenome.webui.taglib;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Map;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.tagext.TagSupport;
 
 import org.rti.webgenome.analysis.SimpleUserConfigurableProperty;
 import org.rti.webgenome.analysis.UserConfigurableProperty;
 import org.rti.webgenome.analysis.UserConfigurablePropertyWithOptions;
 import org.rti.webgenome.util.SystemUtils;
 
 /**
  * Creates an HTML input element suitable for inputting
  * user configurable parameter values.
  * @author dhall
  *
  */
 public final class UserConfigurablePropertyInputTag extends TagSupport {
 	
 	/** Serlialized version ID. */
 	private static final long serialVersionUID = 
 		SystemUtils.getLongApplicationProperty("serial.version.uid");
 	
 	
 	/** Name of a bean of type <code>UserConfigurableProperty</code>. */
 	private String name = null;
 	
 	/**
 	 * Prefix prepended to input name so that downstream actions
 	 * can determine which query parameters denote user
 	 * configurable properties.
 	 */
 	private String prefix = "";
 	
 	
 	/**
 	 * Set name of a bean of type
 	 * <code>UserConfigurableProperty</code>.
 	 * @param name Bean name
 	 */
 	public void setName(final String name) {
 		this.name = name;
 	}
 
 
 	/**
 	 * Set prefix prepended to input name so that downstream actions
 	 * can determine which query parameters denote user
 	 * configurable properties.
 	 * @param prefix Prefix
 	 */
 	public void setPrefix(final String prefix) {
 		this.prefix = prefix;
 	}
 
 
 
 	/**
 	 * Do after start tag parsed.
 	 * @throws JspException if anything goes wrong.
 	 * @return Return value
 	 */
 	@Override
 	public int doStartTag() throws JspException {
 		
 		// Get bean
 		if (this.name == null) {
 			throw new JspException("Name of bean missing");
 		}
 		Object bean = pageContext.findAttribute(this.name);
 		if (bean == null) {
 			throw new JspException("Cannot find bean named '"
 					+ this.name + "' in any scope");
 		}
 		if (!(bean instanceof UserConfigurableProperty)) {
 			throw new JspException("Bean '" + this.name
 					+ "' is not type UserConfigurableProperty");
 		}
 		UserConfigurableProperty prop = (UserConfigurableProperty) bean;
 		
 		// Create input
 		Writer out = pageContext.getOut();
 		try {
 			String propName = this.prefix + prop.getName();
 			if (prop instanceof SimpleUserConfigurableProperty) {
 				out.write("<input type=\"text\" name=\""
 						+ propName + "\" value=\""
 						+ prop.getCurrentValue() + "\">");
 			} else if (prop instanceof UserConfigurablePropertyWithOptions) {
 				out.write("<select name=\"" + propName + "\">");
 				Map<String, String> opMap = (
 						(UserConfigurablePropertyWithOptions) prop)
 						.getOptions();
 				for (String key : opMap.keySet()) {
 					String value = opMap.get(key);
 					out.write("<option value=\"" + key + "\"");
					if (key.equals(prop.getCurrentValue())) {
 						out.write(" selected");
 					}
 					out.write(">" + value + "</option>");
 				}
 				out.write("</select>");
 			}
 		} catch (IOException e) {
 			throw new JspException("Error writing to page");
 		}
 		
 		return TagSupport.SKIP_BODY;
 	}
 }
