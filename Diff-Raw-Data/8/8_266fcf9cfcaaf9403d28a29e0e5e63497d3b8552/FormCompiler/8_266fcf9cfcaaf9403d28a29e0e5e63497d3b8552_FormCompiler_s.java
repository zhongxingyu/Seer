 package org.oobium.build.esp.compiler;
 
 import static org.oobium.build.esp.dom.EspPart.Type.MarkupElement;
 import static org.oobium.utils.StringUtils.blank;
 import static org.oobium.utils.StringUtils.className;
 import static org.oobium.utils.StringUtils.getterName;
 import static org.oobium.utils.StringUtils.hasserName;
 import static org.oobium.utils.StringUtils.titleize;
 import static org.oobium.utils.StringUtils.varName;
 
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.oobium.app.http.Action;
 import org.oobium.build.esp.dom.EspElement;
 import org.oobium.build.esp.dom.EspPart;
 import org.oobium.build.esp.dom.elements.JavaElement;
 import org.oobium.build.esp.dom.elements.MarkupElement;
 import org.oobium.build.esp.dom.parts.MethodArg;
 import org.oobium.persist.Model;
 
 
 public class FormCompiler {
 
 	private static boolean containsFileInput(EspElement element) {
 		switch(element.getType()) {
 		case MarkupElement:
 			MarkupElement h = (MarkupElement) element;
 			if("file".equals(h.getTag().getText())) {
 				return true;
 			}
 			if(h.hasChildren()) {
 				for(EspElement child : h.getChildren()) {
 					if(containsFileInput(child)) {
 						return true;
 					}
 				}
 			}
 			return false;
 		case JavaElement:
 			JavaElement j = (JavaElement) element;
 			if(j.hasChildren()) {
 				for(EspElement child : j.getChildren()) {
 					if(containsFileInput(child)) {
 						return true;
 					}
 				}
 			}
 			return false;
 		default:
 			return false;
 		}
 	}
 	
 	private static String getFormModelNameValue(MarkupElement form) {
 		if(form.hasEntry("as")) {
 			return form.getEntry("as").getText().trim();
 		}
		String arg = form.getArg(0).getText().trim();
		for(int i = 0; i < arg.length(); i++) {
			if(!Character.isJavaIdentifierPart(arg.charAt(i))) {
				return "varName((" + arg + ").getClass())";
			}
		}
		return "\"" + arg + "\"";
 	}
 	
 	private static String getFormModelNameVar(int start) {
 		return "formModelName$" + start;
 	}
 	
 	private static String getFormModelVar(int start) {
 		return "formModel$" + start;
 	}
 	
 	private static String getFormModelVar(MarkupElement form) {
 		if(form.hasJavaType()) {
 			return getFormModelVar(form.getStart());
 		} else {
 			String action = (form.getArgs().size() == 2) ? form.getArg(1).getText().trim() : null;
 			boolean hasMany = !(action == null || "create".equalsIgnoreCase(action) || "update".equalsIgnoreCase(action) || "delete".equalsIgnoreCase(action));
 			if(hasMany) {
 				return getFormModelVar(form.getStart());
 			} else {
 				String arg = form.getArg(0).getText().trim();
 				for(int i = 0; i < arg.length(); i++) {
 					if(!Character.isJavaIdentifierPart(arg.charAt(i))) {
 						return getFormModelVar(form.getStart());
 					}
 				}
 				return arg;
 			}
 		}
 	}
 	
 
 	private final EspCompiler parent;
 	private MarkupElement form;
 	
 	public FormCompiler(EspCompiler parent) {
 		this.parent = parent;
 	}
 
 	private void appendFormFieldName(String name, List<MethodArg> fields) {
 		StringBuilder body = parent.getBody();
 		if(parent.inJava()) {
 			body.append("h(").append(name);
 			for(MethodArg field : fields) {
 				body.append(" + \"[\" + ");
 				parent.build(field, body);
 				body.append(" + \"]\"");
 			}
 			body.append(')');
 		} else {
 			body.append("\").append(").append(name).append(").append(\"");
 			for(MethodArg field : fields) {
 				body.append('[');
 				parent.build(field, body);
 				body.append(']');
 			}
 		}
 	}
 	
 	private StringBuilder appendValueGetter(String model, List<MethodArg> fields) {
 		StringBuilder body = parent.getBody();
 		if(fields.size() == 1) {
 			body.append(model).append('.').append(getter(fields.get(0)));
 		} else {
 			int last = fields.size() - 1;
 			for(int i = 0; i < fields.size(); i++) {
 				body.append(model);
 				for(int j = 0; j <= i; j++) {
 					body.append('.').append((j == i && j != last) ? hasser(fields.get(j)) : getter(fields.get(j)));
 				}
 				if(i < last) {
 					body.append("?(");
 				}
 			}
 			for(int i = 0; i < last; i++) {
 				body.append("):\"\"");
 			}
 		}
 		return body;
 	}
 	
 	public void buildCheck(MarkupElement check) {
 		StringBuilder body = parent.getBody();
 		if(check.hasArgs()) {
 			List<MethodArg> fields = check.getArgs();
 			String model = getFormModel(check);
 			String modelName = getFormModelName(check);
 			if(!blank(modelName)) {
 				String sbName = parent.sbName(body);
 				body.append("<input type=\\\"hidden\\\" name=\\\"");
 				if(check.hasEntry("name")) {
 					parent.build(check.getEntry("name").getValue(), body);
 				} else {
 					appendFormFieldName(modelName, fields);
 				}
 				body.append("\\\" value=\\\"false\\\" />");
 				
 				body.append("<input type=\\\"checkbox\\\"");
 				body.append(" id=\\\"");
 				if(check.hasId()) {
 					parent.build(check.getId(), body);
 				} else if(check.hasEntry("id")) {
 					parent.build(check.getEntry("id").getValue(), body);
 				} else {
 					appendFormFieldName(modelName, fields);
 				}
 				body.append("\\\"");
 				parent.buildClasses(check, model, fields);
 				body.append(" name=\\\"");
 				if(check.hasEntry("name")) {
 					parent.build(check.getEntry("name").getValue(), body);
 				} else {
 					appendFormFieldName(modelName, fields);
 				}
 				body.append("\\\" value=\\\"true\\\"");
 				parent.buildAttrs(check, "id", "name", "value", "type");
 				parent.prepForJava(body);
 				body.append("if(");
 				appendValueGetter(model, fields);
 				body.append(") {\n");
 				parent.incJavaLevel();
 				parent.indent(body);
 				body.append(sbName).append(".append(\" CHECKED\");\n");
 				parent.decJavaLevel();
 				parent.indent(body);
 				body.append("}\n");
 				parent.prepForMarkup(body);
 			}
 		} else {
 			body.append("<input type=\\\"checkbox\\\"");
 			parent.buildId(check);
 			parent.buildClasses(check);
 			parent.buildAttrs(check, "type");
 		}
 		body.append(" />");
 	}
 	
 	public void buildDateInputs(MarkupElement date) {
 		StringBuilder body = parent.getBody();
 		body.append("<span");
 		buildFormField(date, false);
 		body.append('>');
 		parent.prepForJava(body);
 		body.append("dateTimeTags(").append(parent.sbName(body)).append(", ");
 		if(date.hasEntry("name")) {
 			parent.build(date.getEntry("name"), body);
 		}
 		else if(date.hasId()) {
 			parent.build(date.getId(), body);
 		}
 		else if(date.hasEntry("id")) {
 			parent.build(date.getEntry("id"), body);
 		}
 		else if(date.hasArgs()) {
 			String modelName = getFormModelName(date);
 			if(!blank(modelName)) {
 				appendFormFieldName(modelName, date.getArgs());
 			}else {
 				body.append("\"datetime\"");
 			}
 		}
 		else {
 			body.append("\"datetime\"");
 		}
 		body.append(", ");
 		if(date.hasEntry("format")) {
 			parent.build(date.getEntry("format"), body);
 		} else {
 			body.append("\"MMM/dd/yyyy\"");
 		}
 		body.append(", ");
 		if(date.hasArgs()) {
 			String model = getFormModel(date);
 			if(blank(model)) {
 				parent.build(date.getArg(0), body);
 			} else {
 				appendValueGetter(model, date.getArgs());
 			}
 		} else {
 			body.append("new java.util.Date()");
 		}
 		body.append(");\n");
 		parent.prepForMarkup(body);
 		body.append("</span>");
 	}
 
 	public void buildDecimal(MarkupElement input) {
 		StringBuilder body = parent.getBody();
 		body.append("<input type=\\\"text\\\"");
 		if(input.hasArgs()) {
 			List<MethodArg> fields = input.getArgs();
 			String model = getFormModel(input);
 			String modelName = getFormModelName(input);
 			if(!blank(modelName)) {
 				body.append(" id=");
 				if(input.hasId()) {
 					parent.build(input.getId(), body);
 				} else if(input.hasEntry("id")) {
 					parent.build(input.getEntry("id").getValue(), body);
 				} else {
 					appendFormFieldName(modelName, fields);
 				}
 				parent.buildClasses(input, model, fields);
 				body.append(" name=");
 				if(input.hasEntry("name")) {
 					parent.build(input.getEntry("name").getValue(), body);
 				} else {
 					appendFormFieldName(modelName, fields);
 				}
 				if(input.hasEntry("value")) {
 					body.append(" value=");
 					parent.build(input.getEntry("value").getValue(), body);
 				} else {
 					body.append(" value=\\\"\").append(f(");
 					appendValueGetter(model, fields);
 					body.append(")).append(\"\\\"");
 				}
 				parent.buildAttrs(input, "id", "name", "value", "onkeypress", "scale");
 			}
 		} else {
 			if(input.hasEntry("type")) {
 				body.append(" type=\\\"");
 				parent.build(input.getEntry("type").getValue(), body);
 				body.append("\\\"");
 			}
 			parent.buildId(input);
 			parent.buildClasses(input);
 			parent.buildAttrs(input, "type", "onkeypress", "scale");
 		}
 		
 		String js = "var k=window.event?event.keyCode:event.which;" +
 					"if(k==127||k<32){return true}" +
 					"if(k==46||(k>47&&k<58)){" +
 						"var ix=this.value.indexOf('.');" +
 						"if(ix==-1){return true}" +
 						"if(k!=46){" +
 							"if(document.selection && document.selection.createRange){var pos=document.selection.createRange().getBookmark().charCodeAt(2)-2;}" +
 							"else if(this.setSelectionRange){var pos=this.selectionStart;}" +
 							"if(pos<=ix||this.value.length<(ix+{scale}+1)){return true;}" +
 						"}" +
 					"}" +
 					"return false;";
 
 		if(input.hasEntry("scale")) {
 			js.replace("{scale}", input.getEntry("scale").getText());
 		} else {
 			js = js.replace("{scale}", "2");
 		}
 		
 		if(input.hasEntry("onkeypress")) {
 			MethodArg part = input.getEntry("onkeypress");
 			body.append(" onkeypress=\\\"");
 			parent.build(part, body);
 			if(body.charAt(body.length()-1) != ';') {
 				body.append(';');
 			}
 			body.append(js).append("\\\"");
 		} else {
 			body.append(" onkeypress=\\\"").append(js).append("\\\"");
 		}
 		body.append(" />");
 	}
 	
 	public void buildErrors(MarkupElement element) {
 		StringBuilder body = parent.getBody();
 		parent.prepForJava(body);
 
 		body.append("errorsBlock(").append(parent.sbName(body)).append(", ");
 
 		if(element.hasArgs()) {
 			parent.build(element.getArg(0), body);
 		} else {
 			String model = getFormModel(element);
 			if(!blank(model)) {
 				body.append(model);
 			}
 		}
 
 		if(element.hasEntry("title")) {
 			body.append(", ");
 			parent.build(element.getEntry("title"), body);
 		} else {
 			body.append(", null");
 		}
 		
 		if(element.hasEntry("message")) {
 			body.append(", ");
 			parent.build(element.getEntry("message"), body);
 		} else {
 			body.append(", null");
 		}
 		
 		body.append(");\n");
 	}
 	
 	public void buildFields(MarkupElement fields) {
 		List<MethodArg> args = fields.getArgs();
 		if(args != null && args.size() == 1) {
 			StringBuilder body = parent.getBody();
 			String modelName = getFormModelNameVar(fields.getStart());
 
 			parent.prepForJava(body);
 			body.append("String ").append(modelName).append(" = ").append(getFormModelNameValue(fields)).append(";\n");
 			parent.prepForMarkup(body);
 
 			body.append("<input type=\\\"hidden\\\" name=\\\"\").append(").append(modelName).append(").append(\"[id]\\\"");
 			body.append(" value=\\\"");
 			parent.build(fields.getArg(0), body);
 			body.append("\\\" />");
 		}
 	}
 	
 	private void buildForm() {
 		ESourceFile esf = parent.getSourceFile();
 		StringBuilder body = parent.getBody();
 		
 		List<MethodArg> args = form.getArgs();
 		if(args != null && (args.size() == 1 || args.size() == 2)) {
 			String action = (args.size() == 2) ? args.get(1).getText().trim() : null;
 			boolean hasMany = !(action == null || "create".equalsIgnoreCase(action) || "update".equalsIgnoreCase(action) || "delete".equalsIgnoreCase(action));
 
 			String type = parent.getJavaTypeName(form.getJavaType(), Model.class);
 
 			String model = getFormModelVar(form);
 			String modelVal = args.get(0).getText().trim();
 			String modelName = getFormModelNameVar(form.getStart());
 			
 			if("Model".equals(type)) {
 				if(hasMany) {
 					type = className(action.replaceAll("\"", ""));
 				} else {
 					Pattern p = Pattern.compile("\\s*new\\s+(\\w+)\\s*\\(");
 					Matcher m = p.matcher(modelVal);
 					if(m.find()) {
 						type = m.group(1);
 					}
 				}
 			}
 			
 			parent.prepForJava(body);
 			if(!model.equals(modelVal)) {
 				if(type.equals("Model")) {
 					esf.addImport(Model.class.getCanonicalName());
 				}
 				body.append(type).append(' ').append(model).append(" = ");
 				if(hasMany) {
 					body.append("new ").append(type).append("()");
 				} else {
 					body.append(modelVal);
 				}
 				body.append(";\n");
 				parent.indent(body);
 			}
 			body.append("String ").append(modelName).append(" = ");
 			if(hasMany) {
 				body.append('"').append(varName(type)).append('"');
 			} else {
 				body.append(getFormModelNameValue(form));
 			}
 			body.append(";\n");
 			parent.prepForMarkup(body);
 			
 			body.append('<').append(form.getTag().getText());
 			parent.buildId(form);
 			parent.buildClasses(form);
 			parent.buildAttrs(form, "action", "method");
 
 			String method = null;
 			
 			if(action != null) {
 				if("create".equalsIgnoreCase(action)) {
 					method = "post";
 					esf.addStaticImport(Action.class.getCanonicalName() + ".create");
 					body.append(" action=\\\"\").append(pathTo(").append(model).append(", create)).append(\"\\\"");
 				} else if("update".equalsIgnoreCase(action)) {
 					method = "put";
 					esf.addStaticImport(Action.class.getCanonicalName() + ".update");
 					body.append(" action=\\\"\").append(pathTo(").append(model).append(", update)).append(\"\\\"");
 				} else if("destroy".equalsIgnoreCase(action)) {
 					method = "delete";
 					esf.addStaticImport(Action.class.getCanonicalName() + ".destroy");
 					body.append(" action=\\\"\").append(pathTo(").append(model).append(", destroy)).append(\"\\\"");
 				} else {
 					method = "post";
 					body.append(" action=\\\"\").append(pathTo(").append(modelVal).append(", ").append(action).append(")).append(\"\\\"");
 				}
 			}
 			if(method == null && form.hasEntry("method")) {
 				EspPart value = form.getEntry("method").getValue();
 				if(value != null) {
 					method = value.getText();
 					if(form.hasEntry("action")) {
 						body.append(" action=\\\"");
 						parent.build(form.getEntry("action").getValue(), body);
 						body.append("\\\"");
 					} else {
 						if("post".equalsIgnoreCase(method)) {
 							esf.addStaticImport(Action.class.getCanonicalName() + ".create");
 							body.append(" action=\\\"\").append(pathTo(").append(model).append(", create)).append(\"\\\"");
 						} else if("put".equalsIgnoreCase(method)) {
 							esf.addStaticImport(Action.class.getCanonicalName() + ".update");
 							body.append(" action=\\\"\").append(pathTo(").append(model).append(", update)).append(\"\\\"");
 						} else if("delete".equalsIgnoreCase(method)) {
 							esf.addStaticImport(Action.class.getCanonicalName() + ".destroy");
 							body.append(" action=\\\"\").append(pathTo(").append(model).append(", destroy)).append(\"\\\"");
 						} else {
 							esf.addStaticImport(Action.class.getCanonicalName() + ".create");
 							body.append(" action=\\\"\").append(pathTo(").append(model).append(", create)).append(\"\\\"");
 						}
 					}
 				}
 			}
 			if(method == null && form.hasEntry("action")) {
 				EspPart value = form.getEntry("action").getValue();
 				if(value != null) {
 					action = value.getText().trim();
 					if("create".equalsIgnoreCase(action)) {
 						method = "post";
 						esf.addStaticImport(Action.class.getCanonicalName() + ".create");
 						body.append(" action=\\\"\").append(pathTo(").append(model).append(", create)).append(\"\\\"");
 					} else if("update".equalsIgnoreCase(action)) {
 						method = "put";
 						esf.addStaticImport(Action.class.getCanonicalName() + ".update");
 						body.append(" action=\\\"\").append(pathTo(").append(model).append(", update)).append(\"\\\"");
 					} else if("destroy".equalsIgnoreCase(action)) {
 						method = "delete";
 						esf.addStaticImport(Action.class.getCanonicalName() + ".destroy");
 						body.append(" action=\\\"\").append(pathTo(").append(model).append(", destroy)).append(\"\\\"");
 					} else {
 						method = "post";
 						body.append(" action=\\\"");
 						parent.build(value, body);
 						body.append("\\\"");
 					}
 				}
 			}
 			if(method == null) {
 				esf.addImport(Action.class.getCanonicalName());
 				body.append(" action=\\\"\").append(pathTo(").append(model).append(", ").append(model).append(".isNew() ? Action.create : Action.update)).append(\"\\\"");
 			}
 			body.append(" method=\\\"post\\\"");
 			if(form.hasEntry("enctype")) {
 				body.append(" enctype=\\\"");
 				parent.build(form.getEntry("enctype").getValue(), body);
 				body.append("\\\"");
 			} else {
 				if(containsFileInput(form)) {
 					body.append(" enctype=\\\"multipart/form-data\\\"");
 				}
 			}
 			body.append('>');
 			if(method == null) {
 				parent.prepForJava(body);
 				body.append("if(!").append(model).append(".isNew()) {\n");
 				parent.indent(body);
 				body.append('\t').append(parent.sbName(body)).append(".append(\"<input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" />\");\n");
 				parent.indent(body);
 				body.append("}\n");
 				parent.prepForMarkup(body);
 			} else if(method.equalsIgnoreCase("put")) {
 				body.append("<input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" />");
 			} else if(method.equalsIgnoreCase("delete")) {
 				body.append("<input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"DELETE\\\" />");
 			}
 			if(hasMany) {
 				body.append("<input type=\\\"hidden\\\" name=\\\"id\\\" value=\\\"");
 				parent.build(args.get(0), body);
 				body.append("\\\" />");
 			} else {
 				body.append("<input type=\\\"hidden\\\" name=\\\"\").append(").append(modelName).append(").append(\"[id]\\\"");
 				body.append(" value=\\\"");
 				parent.build(args.get(0), body);
 				body.append("\\\" />");
 			}
 		} else {
 			parent.prepForMarkup(body);
 			body.append('<').append(form.getTag().getText());
 			parent.buildId(form);
 			parent.buildClasses(form);
 			parent.buildAttrs(form);
 			body.append('>');
 		}
 	}
 	
 	public void buildForm(MarkupElement form) {
 		this.form = form;
 		buildForm();
 		parent.buildChildren(form);
 		parent.buildClosingTag("form");
 		this.form = null;
 	}
 	
 	private void buildFormField(MarkupElement input, boolean hasValue) {
 		if(input.hasArgs()) {
 			List<MethodArg> fields = input.getArgs();
 			String model = getFormModel(input);
 			String modelName = getFormModelName(input);
 			if(!blank(modelName)) {
 				StringBuilder body = parent.getBody();
 				body.append(" id=\\\"");
 				if(input.hasId()) {
 					parent.build(input.getId(), body);
 				} else if(input.hasEntry("id")) {
 					parent.build(input.getEntry("id").getValue(), body);
 				} else {
 					appendFormFieldName(modelName, fields);
 				}
 				body.append("\\\"");
 				parent.buildClasses(input, model, fields);
 				body.append(" name=\\\"");
 				if(input.hasEntry("name")) {
 					parent.build(input.getEntry("name").getValue(), body);
 				} else {
 					appendFormFieldName(modelName, fields);
 				}
 				body.append("\\\"");
 				if(hasValue) {
 					if(input.hasEntry("value")) {
 						parent.appendAttr("value", input.getEntry("value"));
 					} else {
 						String sbName = parent.sbName(body);
 						body.append(" value=\\\"\");\n");
 						parent.indent(body).append(sbName).append(".append(f(");
 						appendValueGetter(model, fields).append("));\n");
 						parent.indent(body).append(sbName).append(".append(\"\\\"");
 					}
 				}
 				parent.buildAttrs(input, "id", "name", "value", "type");
 			}
 		} else {
 			parent.buildId(input);
 			parent.buildClasses(input);
 			parent.buildAttrs(input, "type");
 		}
 	}
 	
 	void buildInput(MarkupElement input) {
 		String tag = input.getTag().getText();
 		StringBuilder body = parent.getBody();
 		body.append("<input");
 		if(input.hasEntry("type")) {
 			parent.appendAttr("type", input.getEntry("type"));
 		} else {
 			if("hidden".equals(tag))        body.append(" type=\\\"hidden\\\"");
 			else if("file".equals(tag))     body.append(" type=\\\"file\\\"");
 			else if("decimal".equals(tag))  body.append(" type=\\\"text\\\"");
 			else if("number".equals(tag))   body.append(" type=\\\"text\\\"");
 			else if("password".equals(tag)) body.append(" type=\\\"password\\\"");
 			else if("radio".equals(tag))    body.append(" type=\\\"radio\\\"");
 			else if("text".equals(tag))     body.append(" type=\\\"text\\\"");
 		}
 		buildFormField(input, !"password".equals(tag));
 		body.append(" />");
 	}
 	
 	public void buildLabel(MarkupElement label) {
 		StringBuilder body = parent.getBody();
 		parent.prepForMarkup(body);
 		body.append("<label");
 		parent.buildId(label);
 
 		String sbName = parent.sbName(body);
 		
 		if(label.hasArgs()) {
 			List<MethodArg> fields = label.getArgs();
 			String model = getFormModel(label);
 			String modelName = getFormModelName(label);
 			if(!blank(modelName)) {
 				parent.buildClasses(label, model, fields);
 				parent.buildAttrs(label, "for", "text");
 				body.append(" for=\\\"");
 				if(label.hasEntry("for")) {
 					parent.build(label.getEntry("for").getValue(), body);
 				} else {
 					appendFormFieldName(modelName, fields);
 				}
 				body.append("\\\"");
 				
 				body.append('>');
 				if(label.hasEntry("text")) {
 					int pos = body.length();
 					parent.build(label.getEntry("text"), body);
 					if(pos < body.length()) {
 						if(body.charAt(pos) == '\\' && pos+1 < body.length() && body.charAt(pos+1) == '"') {
 							body.delete(pos, pos+2);
 						}
 						if(body.charAt(body.length()-2) == '\\' && body.charAt(body.length()-1) == '"') {
 							body.delete(body.length()-2, body.length());
 						}
 					}
 				}
 				else if(!label.hasInnerText()) {
 					MethodArg arg = fields.get(fields.size()-1);
 					String text = parent.getSimpleString(arg.getValue());
 					if(text == null) {
 						parent.prepForJava(body);
 						body.append(sbName).append(".append(h(titleize(");
 						parent.build(arg, body);
 						body.append(")));\n");
 						parent.prepForMarkup(body);
 					} else {
 						body.append(titleize(text));
 					}
 				}
 				if(label.hasInnerText()) {
 					parent.build(label.getInnerText(), body);
 				}
 
 				parent.prepForJava(body);
 				body.append("if(");
 				if(label.hasEntry("required")) {
 					parent.build(label.getEntry("required").getValue(), body, true);
 				} else {
 					body.append(model).append(".isRequired(");
 					for(int i = 0; i < fields.size(); i++) {
 						if(i != 0) body.append(", ");
 						parent.build(fields.get(i), body, true);
 					}
 					body.append(")");
 				}
 				body.append(") {\n");
 				if(label.hasEntry("requiredClass")) {
 					parent.indent(body);
 					body.append('\t').append(sbName).append(".append(\"<span class=\\\"");
 					parent.build(label.getEntry("requiredClass").getValue(), body);
 					body.append("\\\">*</span>\");\n");
 				} else {
 					parent.indent(body);
 					body.append('\t').append(sbName).append(".append(\"<span class=\\\"required\\\">*</span>\");\n");
 				}
 				parent.indent(body);
 				body.append("}\n");
 			}
 		} else {
 			parent.buildClasses(label);
 			parent.buildAttrs(label, "required", "requiredClass");
 			if(label.hasEntry("required")) {
 				
 			}
 			body.append('>');
 			if(label.hasInnerText()) {
 				parent.build(label.getInnerText(), body);
 			}
 			if(label.hasEntry("required")) {
 				parent.prepForJava(body);
 				body.append("if(");
 				parent.build(label.getEntry("required"), body, true);
 				body.append(") {\n");
 				if(label.hasEntry("requiredClass")) {
 					parent.indent(body);
 					body.append('\t').append(sbName).append(".append(\"<span class=\\\"");
 					parent.build(label.getEntry("requiredClass"), body);
 					body.append("\\\">*</span>\");\n");
 				} else {
 					parent.indent(body);
 					body.append('\t').append(sbName).append(".append(\"<span class=\\\"required\\\">*</span>\");\n");
 				}
 				parent.indent(body);
 				body.append("}\n");
 			}
 		}
 		
 		parent.buildClosingTag("label");
 	}
 	
 	public void buildNumber(MarkupElement input) {
 		StringBuilder body = parent.getBody();
 		body.append("<input type=\\\"text\\\"");
 		if(input.hasArgs()) {
 			List<MethodArg> fields = input.getArgs();
 			String model = getFormModel(input);
 			String modelName = getFormModelName(input);
 			if(!blank(modelName)) {
 				body.append(" id=\\\"");
 				if(input.hasId()) {
 					parent.build(input.getId(), body);
 				} else if(input.hasEntry("id")) {
 					parent.build(input.getEntry("id").getValue(), body);
 				} else {
 					appendFormFieldName(modelName, fields);
 				}
 				body.append("\\\"");
 				parent.buildClasses(input, model, fields);
 				body.append(" name=\\\"");
 				if(input.hasEntry("name")) {
 					parent.build(input.getEntry("name").getValue(), body);
 				} else {
 					appendFormFieldName(modelName, fields);
 				}
 				body.append("\\\"");
 				if(input.hasEntry("value")) {
 					body.append(" value=\\\"");
 					parent.build(input.getEntry("value").getValue(), body);
 				} else {
 					body.append(" value=\\\"\").append(f(");
 					appendValueGetter(model, fields);
 					body.append(")).append(\"");
 				}
 				body.append("\\\"");
 				parent.buildAttrs(input, "id", "name", "value", "onkeypress");
 			}
 		} else {
 			if(input.hasEntry("type")) {
 				body.append(" type=\\\"");
 				parent.build(input.getEntry("type").getValue(), body);
 				body.append("\\\"");
 			}
 			parent.buildId(input);
 			parent.buildClasses(input);
 			parent.buildAttrs(input, "type", "onkeypress");
 		}
 		
 		String js = "var k=window.event?event.keyCode:event.which;return (k==127||k<32||(k>47&&k<58));";
 
 		if(input.hasEntry("onkeypress")) {
 			body.append(" onkeypress=\\\"");
 			parent.build(input.getEntry("onkeypress"), body);
 			if(body.charAt(body.length()-1) != ';') {
 				body.append(';');
 			}
 			body.append(js).append("\\\"");
 		} else {
 			body.append(" onkeypress=\\\"").append(js).append("\\\"");
 		}
 		body.append(" />");
 	}
 	
 	public void buildResetInput(MarkupElement element) {
 		StringBuilder body = parent.getBody();
 		body.append("<input");
 		body.append(" type=\\\"reset\\\"");
 		parent.buildId(element);
 		parent.buildClasses(element);
 		parent.buildAttrs(element, "type");
 		if(!element.hasEntry("value")) {
 			body.append(" value=\\\"Reset\\\"");
 		}
 		body.append(" />");
 	}
 
 	public void buildSelect(MarkupElement select) {
 		StringBuilder body = parent.getBody();
 		body.append("<select");
 		buildFormField(select, false);
 		body.append(">");
 	}
 
 	public void buildSelectOption(MarkupElement option) {
 		StringBuilder body = parent.getBody();
 		body.append("<option");
 		parent.buildId(option);
 		parent.buildClasses(option);
 		if(option.hasArgs()) {
 			parent.buildAttrs(option, "value");
 			body.append(" value=");
 			parent.build(option.getArg(0), body);
 		} else {
 			parent.buildAttrs(option);
 		}
 		body.append('>');
 		if(option.hasInnerText()) {
 			parent.build(option.getInnerText(), body);
 		}
 	}
 	
 	// options<Member>(findAllMembers(), text:"option.getNameLF()", value:"option.getId(), required: "false", sort:"option.getNameLF()")
 	public void buildSelectOptions(MarkupElement element) {
 		if(element.hasArgs()) {
 			StringBuilder body = parent.getBody();
 			parent.prepForJava(body);
 
 			MethodArg options = element.getArg(0);
 			Object selection = element.hasArg(1) ? element.getArg(1) : getSelectionGetter(element);
 			if(element.hasEntry("text") || element.hasEntry("value")) {
 				String sbName = parent.sbName(body);
 				if(blank(selection)) {
 					body.append("for(");
 					String var = varName(parent.getJavaTypeName(element, Object.class));
 					body.append(" ").append(var).append(" : ");
 					parent.build(options, body, true);
 					body.append(") {\n");
 					parent.indent(body);
 					if(element.hasEntry("title")) {
 						body.append('\t').append(sbName).append(".append(\"<option title=\\\"\").append(");
 						parent.build(element.getEntry("title"), body);
 						body.append(").append(\"\\\" value=\\\"\").append(");
 					} else {
 						body.append('\t').append(sbName).append(".append(\"<option value=\\\"\").append(");
 					}
 					if(element.hasEntry("value")) {
 						parent.build(element.getEntry("value"), body);
 					} else {
 						body.append("f(").append(var).append(')');
 					}
 					body.append(").append(\"\\\" >\").append(");
 					if(element.hasEntry("text")) {
 						parent.build(element.getEntry("text"), body);
 					} else {
 						body.append("h(String.valueOf(").append(var).append("))");
 					}
 					body.append(").append(\"</option>\");\n");
 					parent.indent(body);
 					body.append("}\n");
 				} else {
 					String selectionVar = "selection$" + element.getStart();
 					String selectedVar = "selected$" + element.getStart();
 					
 					body.append("Object ").append(selectionVar).append(" = ").append(selection).append(";\n");
 					parent.indent(body);
 					body.append("for(");
 					parent.build(element.getJavaType(), body);
 					String var = varName(parent.getJavaTypeName(element.getJavaType(), Object.class));
 					body.append(" ").append(var).append(" : ");
 					parent.build(options, body);
 					body.append(") {\n");
 					parent.indent(body);
 					body.append("\tboolean ").append(selectedVar).append(" = isEqual(");
 					if(element.hasEntry("value")) {
 						parent.build(element.getEntry("value"), body);
 					} else {
 						body.append(var);
 					}
 					body.append(", ").append(selectionVar).append(");\n");
 					if(element.hasEntry("title")) {
 						parent.indent(body);
 						body.append('\t').append(sbName).append(".append(\"<option title=\\\"\").append(");
 						parent.build(element.getEntry("title"), body);
 						body.append(").append(\"\\\" value=\\\"\").append(f(");
 					} else {
 						parent.indent(body);
 						body.append('\t').append(sbName).append(".append(\"<option value=\\\"\").append(");
 					}
 					if(element.hasEntry("value")) {
 						parent.build(element.getEntry("value"), body);
 					} else {
 						body.append("f(").append(var).append(")");
 					}
 					body.append(").append(\"\\\" \").append(").append(selectedVar).append(" ? \"selected >\" : \">\").append(");
 					if(element.hasEntry("text")) {
 						parent.build(element.getEntry("text"), body);
 					} else {
 						body.append("h(String.valueOf(").append(var).append("))");
 					}
 					body.append(").append(\"</option>\");\n");
 					parent.indent(body);
 					body.append("}\n");
 				}
 			} else {
 				String required = null;
 				if(element.hasEntry("required")) {
 					// TODO
 //					required = element.hasEntry("required") ? parent.toArg(element.getEntry("required").getText()) : "false";
 				}
 				String sbName = parent.sbName(body);
 				if(blank(selection)) {
 					body.append(sbName).append(".append(optionTags(");
 					parent.build(options, body, true);
 					if(blank(required)) {
 						body.append("));\n");
 					} else {
 						body.append("), ").append(required).append(");\n");
 					}
 				} else {
 					body.append(sbName).append(".append(optionTags(");
 					parent.build(options, body, true);
 					if(selection instanceof MethodArg) {
 						body.append(", ");
 						parent.build((MethodArg) selection, body);
 					} else {
 						body.append(", ").append(selection);
 					}
 					if(blank(required)) {
 						String model = getFormModel(element);
 						if(!blank(model)) {
 							List<MethodArg> fields = ((MarkupElement) element.getParent()).getArgs();
 							if(fields == null) {
 								body.append("));\n");
 							} else {
 								body.append(", ").append(model).append(".isRequired(");
 								for(int i = 0; i < fields.size(); i++) {
 									if(i != 0) body.append(", ");
 									parent.build(fields.get(i), body);
 								}
 								body.append(")));\n");
 							}
 						} else {
 							body.append("));\n");
 						}
 					} else {
 						body.append(", ").append(required).append("));\n");
 					}
 				}
 			}
 		}
 	}
 
 	public void buildSubmit(MarkupElement element) {
 		StringBuilder body = parent.getBody();
 		body.append("<input");
 		body.append(" type=\\\"submit\\\"");
 		parent.buildId(element);
 		parent.buildClasses(element);
 		parent.buildAttrs(element, "type");
 		if(!element.hasEntry("value")) {
 			if(element.hasInnerText()) {
 				body.append(" value=\\\"");
 				parent.build(element.getInnerText(), body);
 				body.append("\\\"");
 			} else {
 				String model = getFormModel(element);
 				if(blank(model)) {
 					body.append(" value=\\\"Submit\\\"");
 				} else {
 					String modelName = getFormModelName(element);
 					String action = getFormAction(element);
 					if("create".equalsIgnoreCase(action)) {
 						body.append(" value=\\\"Create ");
 					} else if("update".equalsIgnoreCase(action)) {
 						body.append(" value=\\\"Update ");
 					} else {
 						body.append(" value=\\\"\").append(").append(model).append(".isNew() ? \"Create \" : \"Update ");
 					}
 					body.append("\").append(titleize(").append(modelName).append(")).append(\"\\\"");
 				}
 			}
 		}
 		body.append(" />");
 	}
 	
 	public void buildTextArea(MarkupElement textarea) {
 		StringBuilder body = parent.getBody();
 		body.append("<textarea");
 		buildFormField(textarea, false);
 		body.append(">");
 		if(textarea.hasArgs()) {
 			body.append("\").append(f(");
 			appendValueGetter(getFormModel(textarea), textarea.getArgs());
 			body.append(")).append(\"");
 		} else {
 			if(textarea.hasInnerText()) {
 				parent.build(textarea.getInnerText(), body);
 			}
 		}
 	}
 	
 	private MarkupElement getForm(MarkupElement formField) {
 		EspPart parent = formField.getParent();
 		while(parent != null) {
 			if(parent.isA(MarkupElement)) {
 				MarkupElement element = (MarkupElement) parent;
 				if("form".equals(element.getTag().getText())) {
 					List<MethodArg> args = element.getArgs();
 					if(args != null && (args.size() == 1 || args.size() == 2)) {
 						return element;
 					}
 					break; // forms shouldn't be nested...
 				} else if("fields".equals(element.getTag().getText())) {
 					List<MethodArg> args = element.getArgs();
 					if(args != null && args.size() == 1) {
 						return element; // args.get(0).getText().trim();
 					}
 					// fields section may be nested...
 				}
 			}
 			parent = parent.getParent();
 		}
 		return null;
 	}
 
 	private String getFormAction(MarkupElement formField) {
 		EspPart parent = formField.getParent();
 		while(parent != null) {
 			if(parent.isA(MarkupElement)) {
 				MarkupElement h = (MarkupElement) parent;
 				if("form".equals(h.getTag().getText())) {
 					List<MethodArg> args = h.getArgs();
 					if(args != null && args.size() == 2) {
 						return args.get(1).getText().trim();
 					}
 					if(h.hasEntry("method")) {
 						EspPart part = h.getEntry("method").getValue();
 						if(part != null) {
 							String method = part.getText().trim();
 							if("post".equalsIgnoreCase(method)) {
 								return "create";
 							} else if("put".equalsIgnoreCase(method)) {
 								return "update";
 							}
 						}
 						return null;
 					}
 					if(h.hasEntry("action")) {
 						EspPart part = h.getEntry("action").getValue();
 						if(part != null) {
 							return part.getText().trim();
 						}
 						return null;
 					}
 					break; // forms shouldn't be nested...
 				}
 			}
 			parent = parent.getParent();
 		}
 		return null;
 	}
 	
 	private String getFormModel(MarkupElement formField) {
 		MarkupElement form = getForm(formField);
 		if(form != null) {
 			return getFormModelVar(form);
 		}
 		return null;
 	}
 
 	private String getFormModelName(MarkupElement formField) {
 		MarkupElement form = getForm(formField);
 		if(form != null) {
 			return getFormModelNameVar(form.getStart());
 		}
 		return null;
 	}
 	
 	private String getSelectionGetter(MarkupElement options) {
 		EspPart parent = options.getParent();
 		if(parent instanceof MarkupElement && "select".equals(((MarkupElement) parent).getTag().getText())) {
 			MarkupElement select = (MarkupElement) parent;
 			String model = getFormModel(select);
 			if(!blank(model)) {
 				StringBuilder sb = new StringBuilder();
 				List<MethodArg> fields = select.getArgs();
 				if(fields != null) {
 					if(fields.size() == 1) {
 						sb.append(model).append('.').append(getter(fields.get(0)));
 					} else {
 						int last = fields.size() - 1;
 						for(int i = 0; i < fields.size(); i++) {
 							sb.append(model);
 							for(int j = 0; j <= i; j++) {
 								sb.append('.').append((j == i && j != last) ? hasser(fields.get(j)) : getter(fields.get(j)));
 							}
 							if(i < last) {
 								sb.append("?(");
 							}
 						}
 						for(int i = 0; i < last; i++) {
 							sb.append("):\"\"");
 						}
 					}
 				}
 				return sb.toString();
 			}
 		}
 		return null;
 	}
 	
 	private String getter(MethodArg part) {
 		String text = parent.getSimpleString(part.getValue());
 		if(text == null) {
 			StringBuilder sb = new StringBuilder();
 			sb.append("get(");
 			parent.build(part, sb, true);
 			sb.append(")");
 			return sb.toString();
 		}
 		if(parent.inJava()) {
 			text = text.substring(1, text.length()-1);
 		}
 		return getterName(text) + "()";
 	}
 	
 	private String hasser(MethodArg part) {
 		String text = parent.getSimpleString(part.getValue());
 		if(text == null) {
 			StringBuilder sb = new StringBuilder();
 			sb.append("isSet(");
 			parent.build(part, sb);
 			sb.append(")");
 			return sb.toString();
 		}
 		return hasserName(text) + "()";
 	}
 
 	public boolean inForm() {
 		return form != null;
 	}
 	
 }
