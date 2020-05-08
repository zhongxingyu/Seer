 package net.sf.javascribe.patterns.js.navigation;
 
 import java.util.List;
 
 import net.sf.javascribe.api.ProcessorContext;
 import net.sf.javascribe.api.JavascribeException;
 import net.sf.javascribe.api.annotation.Processor;
 import net.sf.javascribe.api.annotation.ProcessorMethod;
 import net.sf.javascribe.api.annotation.Scannable;
 import net.sf.javascribe.langsupport.javascript.JavascriptConstants;
 import net.sf.javascribe.langsupport.javascript.JavascriptSourceFile;
 import net.sf.javascribe.langsupport.javascript.JavascriptUtils;
 import net.sf.javascribe.langsupport.javascript.JavascriptVariableType;
 import net.sf.javascribe.patterns.js.page.PageUtils;
 
 @Scannable
 @Processor
 public class PageNavigationProcessor {
 
 	@ProcessorMethod(componentClass=PageNavigation.class)
 	public void process(PageNavigation comp,ProcessorContext ctx) throws JavascribeException {
 		
 		ctx.setLanguageSupport("Javascript");
 		
 		JavascriptSourceFile src = JavascriptUtils.getSourceFile(ctx);
 		if (comp.getName().trim().length() == 0) {
 			throw new JavascribeException(
 					"Found a Page Navigation component with no name.");
 		}
 		
 		System.out.println("Processing Page Navigation '"+comp.getName()+"'");
 		
 		JavascriptVariableType type = new JavascriptVariableType(
 				JavascriptConstants.JS_TYPE + comp.getName());
 		ctx.getTypes().addType(type);
 
 		src.getSource().append("var "+comp.getName()+" = { currentPage : null, currentDiv : null };\n");
 		
 		if (comp.getPage().size()==0) {
 			throw new JavascribeException("A Page Navigation component must have at least 1 page.");
 		}
 		
 		StringBuilder showPageCode = new StringBuilder();
 		StringBuilder refreshPageCode = new StringBuilder();
 		
 		src.getSource().append(comp.getName()+".switchPage = function(pageName,data) {\n")
 			.append("if (this.currentPage!=null) {\n")
 			.append("var temp = this.currentPage;\n")
 			.append("$('#'+this.currentDiv).hide('"+comp.getHide()+"',{},400,function() { "+comp.getName()+".showPage(pageName,data); });\n");
 
 		boolean first = true;
 		for(Page p : comp.getPage()) {
 			if (p.getOnHide().trim().length()>0) {
 				if (first) first = false;
 				else src.getSource().append("else ");
 				src.getSource().append("if (temp=='"+p.getName()+"') {\n");
 				src.getSource().append(p.getName()+".controller.dispatch('"+p.getOnHide()+"');\n");
 			}
 		}
 		
 		src.getSource().append("} else {\n")
 			.append("this.showPage(pageName,data);\n")
 			.append("}\n}.bind("+comp.getName()+");\n");
 		
 		showPageCode.append(comp.getName()+".showPage = function(pageName,data) {\n");
 		showPageCode.append("this.currentPage=pageName;\n");
 		showPageCode.append("var div = null;\n");
 		first = true;
 		for(Page p : comp.getPage()) {
 			// Get the page and model types
 			JavascriptVariableType pageType = PageUtils.getPageType(ctx, p.getName());
 			if (pageType==null) throw new JavascribeException("Could not find type for page '"+p.getName()+"'");
 			String modelTypeName = pageType.getAttributeType("model");
			JavascriptVariableType modelType = null;
			if (modelTypeName!=null) {
				modelType = (JavascriptVariableType)ctx.getType(modelTypeName);
			}
 			// Append to showPage
 			if (first) first = false;
 			else showPageCode.append("else ");
 			showPageCode.append("if (pageName=='"+p.getName()+"') {\n")
 				.append("div = '").append(p.getDiv()).append("';\n")
 				.append("this.currentDiv = div;\n");
 			// Set page model attribs from data parameter, if necessary
 			showPageCode.append("if (data!=null) {\n");
 			if (modelType!=null) {
 				List<String> att = modelType.getAttributeNames();
 				for(String s : att) {
 					showPageCode.append("if (data.hasOwnProperty('"+s+"')) { ")
 						.append(p.getName()+".model.set").append(Character.toUpperCase(s.charAt(0)))
 						.append(s.substring(1)).append("(data."+s+");}\n");
 				}
 			}
 			showPageCode.append("}\n");
 			if (p.getOnShow().trim().length()>0) {
 				showPageCode.append(p.getName()+".controller.dispatch('"+p.getOnShow()+"');\n");
 			}
 			showPageCode.append("}\n");
 		}
 		showPageCode.append("else { alert('Unrecognized page '+this.currentPage); return; }\n");
 		showPageCode.append("$('#'+this.currentDiv).show('"+comp.getShow()+"',{},400,null);\n");
 		showPageCode.append("}.bind("+comp.getName()+");\n");
 		
 		refreshPageCode.append(comp.getName()+".refreshCurrentPage = function() {\n")
 			.append("if (this.currentPage!=null) {\n");
 		first = true;
 		for(Page p : comp.getPage()) {
 			if (p.getOnRefresh().trim().length()>0) {
 				if (first) first = false;
 				else refreshPageCode.append("else ");
 				refreshPageCode.append("if (this.currentPage=='"+p.getName()+"') {\n");
 				refreshPageCode.append(p.getName()+".controller.dispatch('"+p.getOnRefresh()+"');\n");
 				refreshPageCode.append("}\n");
 			}
 		}
 		refreshPageCode.append("}\n}.bind("+comp.getName()+");\n");
 		
 		
 		src.getSource().append(showPageCode);
 		src.getSource().append(refreshPageCode);
 	}
 
 }
 
