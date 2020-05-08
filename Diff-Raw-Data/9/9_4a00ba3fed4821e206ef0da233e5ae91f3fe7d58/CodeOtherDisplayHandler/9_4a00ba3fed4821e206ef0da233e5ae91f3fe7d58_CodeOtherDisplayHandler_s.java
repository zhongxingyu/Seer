 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.orangeleap.tangerine.web.customization.tag.fields.handlers.impl.display;
 
 import com.orangeleap.tangerine.web.customization.tag.fields.handlers.impl.lookups.CodeOtherHandler;
 import com.orangeleap.tangerine.domain.customization.SectionDefinition;
 import com.orangeleap.tangerine.domain.customization.SectionField;
 import com.orangeleap.tangerine.controller.TangerineForm;
 import org.springframework.context.ApplicationContext;
 import org.springframework.util.StringUtils;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.jsp.PageContext;
 import java.util.List;
 
 /**
  * User: alexlo
  * Date: Jul 27, 2009
  * Time: 9:28:32 AM
  */
 public class CodeOtherDisplayHandler extends CodeOtherHandler {
 
 	public CodeOtherDisplayHandler(ApplicationContext applicationContext) {
 		super(applicationContext);
 	}
 
 	@Override
 	protected void doHandler(HttpServletRequest request, HttpServletResponse response, PageContext pageContext,
 	                      SectionDefinition sectionDefinition, List<SectionField> sectionFields, SectionField currentField,
 	                      TangerineForm form, String formFieldName, Object fieldValue, StringBuilder sb) {
		sb.append("<div id=\"").append(formFieldName).append("\" class=\"readOnlyField ");
 		sb.append(resolveEntityAttributes(currentField)).append("\">");
 
 		Object codeValue = getCodeDisplayValue(sectionDefinition, currentField, form, formFieldName, fieldValue);
 
 		if (codeValue == null || ! StringUtils.hasText(codeValue.toString())) {
 		    sb.append("&nbsp;");
 		}
 		else {
 		    sb.append(codeValue);
 		}
 		sb.append("</div>");
 	}
 }
