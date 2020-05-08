 package com.orangeleap.tangerine.web.customization.tag.fields.handlers.impl.display;
 
 import com.orangeleap.tangerine.web.customization.tag.fields.handlers.impl.picklists.multi.MultiPicklistAdditionalHandler;
 import com.orangeleap.tangerine.domain.customization.SectionDefinition;
 import com.orangeleap.tangerine.domain.customization.SectionField;
 import com.orangeleap.tangerine.domain.customization.Picklist;
 import com.orangeleap.tangerine.controller.TangerineForm;
 import org.springframework.context.ApplicationContext;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.jsp.PageContext;
 import java.util.List;
 
 /**
  * User: alexlo
  * Date: Jul 8, 2009
  * Time: 5:09:26 PM
  */
 public class MultiPicklistAdditionalDisplayHandler extends MultiPicklistAdditionalHandler {
 
 	public MultiPicklistAdditionalDisplayHandler(ApplicationContext applicationContext) {
 		super(applicationContext);
 	}
 
 	@Override
 	protected void doHandler(HttpServletRequest request, HttpServletResponse response, PageContext pageContext,
 	                      SectionDefinition sectionDefinition, List<SectionField> sectionFields, SectionField currentField,
 	                      TangerineForm form, String formFieldName, Object fieldValue, StringBuilder sb) {
 		Picklist picklist = resolvePicklist(currentField, pageContext);
 		createTop(request, pageContext, formFieldName, sb);
 		createContainerBegin(request, pageContext, formFieldName, sb);
 		createMultiPicklistBegin(currentField, formFieldName, picklist, sb);
 		createLeft(sb);
 		String selectedRefs = createMultiPicklistOptions(pageContext, picklist, fieldValue, sb);
 		createLabelTextInput(pageContext, currentField, formFieldName, sb);
 		createRight(sb);
 
 		createAdditionalFields(currentField, form, formFieldName, sb);
 
		createMultiPicklistEnd(sb);

 		createContainerEnd(sb);
 		createBottom(request, pageContext, formFieldName, sb);
 		createSelectedRefs(formFieldName, selectedRefs, sb);
 	}
 
 	@Override
 	protected void writeDeleteLink(StringBuilder sb, String deleteHandler) {
 		// override to not write a delete link
 	}
 
 	@Override
 	protected String getContainerCssClass() {
 		return "readOnly";
 	}
 }
