 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *     documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 20th September 2010
  */
 package au.edu.uts.eng.remotelabs.rigclient.server.pages;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.IConfigDescriptions;
 import au.edu.uts.eng.remotelabs.rigclient.util.IConfigDescriptions.Property;
 
 /**
  * Configuration page.
  */
 public class ConfigPage extends AbstractPage
 {
     /** Sorted configuration stanzas. */
     private  Map<String, Map<String, String>> stanzas;
     
     /** Configuration properties. */
     private Map<String, String> props;
     
     /** Configuration descriptions. */
     private IConfigDescriptions desc;
     
     /** Stanza to load. */
     private String uriSuffix;
     
     @Override
     public void preService(HttpServletRequest req)
     {
         this.props = this.config.getAllProperties();
         this.desc = ConfigFactory.getDescriptions();
         
         /* Group the properties by stanza. */
         this.stanzas = new HashMap<String, Map<String, String>>();
         for (Entry<String, String> e : this.props.entrySet())
         {
             Property p = this.desc.getPropertyDescription(e.getKey());
             String sta = "Unknown";
             if (p != null)
             {
                 /* The property description exists so add the property to the 
                  * configured stanza. */
                 sta = p.getStanza();
             }
             if (!this.stanzas.containsKey(sta)) this.stanzas.put(sta, new HashMap<String, String>());
             this.stanzas.get(sta).put(e.getKey(), e.getValue());
         }
         
         String url = "";
         try
         {
             url = URLDecoder.decode(req.getRequestURI(), 
                     req.getCharacterEncoding() == null ? "UTF-8" : req.getCharacterEncoding());
         }
         catch (UnsupportedEncodingException ex)
         {
            this.logger.error("Unknown character encoding from HTTP request. Reqest character encoding  is '" + 
                    req.getCharacterEncoding() + "'. Going to attempt to use the UTF-8 encoding.");
            try
            {
                url = URLDecoder.decode(req.getRequestURI(), "UTF-8");
            }
            catch (UnsupportedEncodingException e1)
            {
                this.logger.error("Failed URL decoding with 'UTF-8' character encoding, failing request.");
            } 
         }
         
         if (!"/config".equals(url))
         {
             this.uriSuffix = url.substring(url.lastIndexOf("/config") + 8);
             if (this.stanzas.containsKey(this.uriSuffix) && !"POST".equalsIgnoreCase(req.getMethod()))
             {
                 this.framing = false;
             }
         }
         else this.uriSuffix = url;
     }
  
     @Override
     public void contents(HttpServletRequest req, HttpServletResponse resp) throws IOException
     {
         String displayStanza = "Rig";
         boolean showRestartMessage = false;
         
         if (this.stanzas.containsKey(this.uriSuffix))
         {
             if ("POST".equalsIgnoreCase(req.getMethod()))
             {
                 /* Stored the posted values. */
                 displayStanza = this.uriSuffix;
                 
                 boolean changed = false;
                 
                 @SuppressWarnings("unchecked")
                 Enumeration<String> params = req.getParameterNames();
                 while (params.hasMoreElements())
                 {
                     String param = params.nextElement();
                     String value = req.getParameter(param);
                     
                     String confVal = this.config.getProperty(param);
                    if (confVal != null && !confVal.equals(value))
                     {
                         this.logger.info("Changing configuration property '" + param + "' to '" + value + "'.");
                         
                         /* Property is changed so save it. */
                         this.props.put(param, value);
                         this.config.setProperty(param, value);
                         changed = true;
                         
                         Property p = this.desc.getPropertyDescription(param);
                         if (p == null || p.needsRestart()) showRestartMessage = true;
                     }
                 }
                 
                 if (changed) this.config.serialise();
             }
             else
             {
                 this.getConfigStanza(this.uriSuffix);
                 return;
             }
         }
         
         /* Display restart warning. */
         if (showRestartMessage)
         {
            this.println("<div id='confrestartwarning' class='ui-state ui-state-error ui-corner-all confalert'>");
            this.println("  <span class='ui-icon ui-icon-alert helphinticon'> </span>");
            this.println("  The change will be applied next time the rig client is started.");
             this.println("</div>");
         }
 
         /* Add tabs to page. */
         this.println("<div id='lefttabbar'>");
         this.println("  <ul id='lefttablist'>");
 
         int i = 0;
         Set<String> orderedStanzas = new LinkedHashSet<String>(this.desc.getStanzas());
         orderedStanzas.retainAll(this.stanzas.keySet());
         orderedStanzas.addAll(this.stanzas.keySet());
 
         for (String s : orderedStanzas)
         {
             String id = s.replace(" ", "");
 
             String classes = "Rig".equals(s) ? "selectedtab" : "notselectedtab";
             if (i == 0) classes += " ui-corner-tl";
             else if (i == this.stanzas.size() - 1) classes += " ui-corner-bl";
 
             this.println("<li><a id='" + id + "tab' class='" + classes + "' onclick='loadConfStanza(\"" + s + "\")'>");
             this.println("  <div class='conftab'>");
             this.println("    " + s);
             this.println("  </div>");
             this.println("</a></li>");
 
             i++;
         }
 
         this.println("  </ul>");
         this.println("</div>");
 
         this.println("<div id='contentspane' class='ui-corner-tr ui-corner-bottom'>");
         this.getConfigStanza(displayStanza);
         this.println("</div>");
 
         this.println("<script type='text/javascript'>");
         this.println(
                 "$(document).ready(function() {\n" +
                 "     $('#confform').validationEngine();\n" +
                 "     $('#confform').jqTransform();\n" +
                 "     $('.jqTransformInputWrapper').css('width', '305px');\n" +
                 "     $('.jqTransformInputInner div input').css('width', '100%');\n");
 
         /* Contents pane height. */
         this.println(
                 "     $('#contentspane').css('height', $(window).height() - 230);");
         this.println(
                 "     $(window).resize(function() { \n" +
                 "         $('#contentspane').css('height', $(window).height() - 230);\n" +
                 "     });");
 
         this.println("});");		
         this.println("</script>");
     }
     
     /**
      * Gets the configuration for the supplied stanza.
      * 
      * @param stanza stanza name
      */
     private void getConfigStanza(String stanza)
     {
         Map<String, String> stanzaProps = this.stanzas.get(stanza);
         List<Property> stanzaPropList = this.desc.getPropertyDescriptions(stanza);
         
         this.println("<form id='confform' action='/config/" + stanza + "' method='POST'>");
         this.println("  <table id='contentstable'>");
         int i = 0;
         
         if (stanzaPropList != null)
         {
             for (Property p : stanzaPropList)
             {
                 String name = p.getName();
                 stanzaProps.remove(name);
                 String val = this.config.getProperty(name);
                 if (val == null) val = ""; // If the property value isn't set.
                 
                 this.println("<tr class='" + (i % 2 == 0 ? "evenrow" : "oddrow") + "'>");
                 
                 /* Property details. */
                 this.println("  <td class='pcol'>");
                 this.println("      <span class='pkey'>" + name + "</span>");
                 this.println("      <div class='pdesc'>" + p.getDescription() + "</div>");
                 this.println("  </td>");
                 this.println("  <td class='pval'>");
                 
                 String vd = "validate[";
                 vd += p.isMandatory() ? "required" : "optional";
                 switch (p.getType())
                 {
                     case BOOLEAN:
                         if ("true".equalsIgnoreCase(val))
                         {
                             this.println("<div><input type='radio' name='" + name + "' value='true' checked='checked' />" +
                                     "<span class='pblabel'>true</span></div>");
                         }
                         else
                         {
                             this.println("<div><input type='radio' name='" + name + "' value='true' />" +
                                     "<span class='pblabel'>true</span></div>");
                         }
                         
                         if ("false".equalsIgnoreCase(val))
                         {
                             this.println("<div style='margin-top: 10px'><input type='radio' name='" + name + "' value='false' checked='checked' />" +
                                     "<span class='pblabel'>false</span></div>");
                         }
                         else
                         {
                             this.println("<div style='margin-top: 10px'><input type='radio' name='" + name + "' value='false' />" +
                                     "<span class='pblabel'>false</span></div>");
                         }
                         break;
                     case CHAR:
                         if (vd.charAt(vd.length() - 1) != '[') vd += ',';
                         vd += "length[1,1]]";
                         this.println("<input id='" + name + "' type='text' name='" + name + "' class='" + vd + "' value='" + 
                                 val + "' />");
                         break;
                     case FLOAT:
                         if (vd.charAt(vd.length() - 1) != '[') vd += ',';
                         vd += "custom[onlyNumber]]";
                         this.println("<input id='" + name + "' type='text' name='" + name + "' class='" + vd + "' value='" + 
                                 val + "' />");
                         break;
                     case INTEGER:
                         if (vd.charAt(vd.length() - 1) != '[') vd += ',';
                         vd += "custom[onlyNumber]]";
                         this.println("<input id='" + name + "' type='text' name='" + name + "' class='" + vd + "' value='" + 
                                 val + "' />");
                         break;
                     case STRING:
                         this.println("<input id='" + name + "' type='text' name='" + name + "' class='" + vd + "]' value='" + 
                                 val + "' />");
                         break;
                 }
 
                 this.println("  </td>");
                 this.println("</tr>");
                 i++;
             }
             
             /* Any other properties that may exist. */
             for (Entry<String, String> e : stanzaProps.entrySet())
             {
                 this.println("<tr class='" + (i % 2 == 0 ? "evenrow" : "oddrow") + "'>");
                 this.println("  <td class='pcol'>");
                 this.println("      <span class='pkey'>" + e.getKey() + "</span>");
                 this.println("  </td>");
                 this.println("  <td class='pval'>");
                 this.println("      <input id='" + e.getKey() + "' type='text' name='" + e.getKey() + "' value='" + 
                             e.getValue() + "' />");
                 this.println("  </td>");
                 this.println("</tr>");
                 i++;
             }
         }
         this.println("  </table>");
         
         this.println("  <div class='submitbut'>");
         this.println("      <input class='submitbutton' type='submit' value='Save' />");
         this.println("  </div>");
         
         this.println("</form>");
     }
 
     @Override
     protected String getPageType()
     {
         return "Configuration";
     }
 }
