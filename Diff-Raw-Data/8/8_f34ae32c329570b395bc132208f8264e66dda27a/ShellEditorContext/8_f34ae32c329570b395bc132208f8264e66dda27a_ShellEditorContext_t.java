 package org.openxava.ex.editor.shell;
 
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.openxava.controller.ModuleContext;
 import org.openxava.ex.cl.ClassLoaderUtil;
 import org.openxava.ex.utils.FreeMarkerEngine;
 import org.openxava.formatters.IFormatter;
 import org.openxava.model.meta.MetaProperty;
 import org.openxava.util.Messages;
 import org.openxava.view.View;
 import org.openxava.web.WebEditors;
 import org.openxava.web.meta.MetaEditor;
 import org.openxava.web.meta.MetaWebEditors;
 
 import freemarker.template.TemplateException;
 
 /**
  * The wrapper for editor "/xava-ex/editors/Shell.jsp"
  * @author root
  *
  */
 public class ShellEditorContext {
 	public static final String PROP_SHELL_CLASS = "xava-ex.editor.shell.class";
 	
 	public static final String render(ShellEditorContext ctx){
 		return getEditor(ctx).render(ctx);
 	}
 	public static final String renderReadOnly(ShellEditorContext ctx){
 		return getEditor(ctx).renderReadOnly(ctx);
 	}
 	private static IShellEditor getEditor(ShellEditorContext ctx){
 		IShellEditor ie;
 		try {
 			ie = (IShellEditor) ClassLoaderUtil.forName(IShellEditor.class, ctx.getRendererClassName()).newInstance();
 			return ie;
 		} catch (InstantiationException e) {
 			throw new RuntimeException(e);
 		} catch (IllegalAccessException e) {
 			throw new RuntimeException(e);
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	private ModuleContext context;
 	private Messages errors;
 	private View view;
 	private MetaProperty metaProperty;
 	private String rawValue;
 	private HttpServletRequest request;
 	private String rendererClassName;
 	private String propertyKey;
 	private String contextPath;
 	public ShellEditorContext(
 			String propertyKey, ModuleContext context, View view, MetaProperty metaProperty,
 			String rawValue, Messages errors, HttpServletRequest request) {
 		this.propertyKey = propertyKey;
 		this.context = context;
 		this.view = view;
 		this.metaProperty = metaProperty;
 		this.rawValue = rawValue;
 		this.errors = errors;
 		this.request = request;
 		this.rendererClassName = request.getParameter(PROP_SHELL_CLASS);
 		this.contextPath = request.getContextPath();
 	}
 	public ModuleContext getContext() {
 		return context;
 	}
 	public Messages getErrors() {
 		return errors;
 	}
 	public View getView() {
 		return view;
 	}
 	public MetaProperty getMetaProperty() {
 		return metaProperty;
 	}
 	public String getRawValue() {
 		return rawValue;
 	}
 	public HttpServletRequest getRequest() {
 		return request;
 	}
 	public String getRendererClassName() {
 		return rendererClassName;
 	}
 	public String getPropertyKey() {
 		return propertyKey;
 	}
 	
 	/**
 	 * Return the formatter of current property;
 	 * FIXME: Now always return the formatter defined in editors.xml, see {@link WebEditors#getMetaEditorFor(org.openxava.model.meta.MetaMember, String)}.
 	 */
 	public IFormatter getPropertyFormatter(){
 		//FIXME: Now always return the formatter defined in editors.xml
 		MetaEditor editor = MetaWebEditors.getMetaEditorFor(metaProperty);
 		return editor.getFormatter();
 	}
 
 	/**
 	 * render html segment from FreeMarker Template
 	 * @param resource
 	 * @param moreDatas
 	 * @param loaderClass
 	 * @return
 	 * @throws TemplateException
 	 */
 	public String parseFtl(String resource, Map<String, Object> moreDatas, Class<?> loaderClass){
 		try {
 			FreeMarkerEngine engine = new FreeMarkerEngine();
 			engine.setModel("ctx", this);
 			engine.setModel("contextPath", contextPath);
 			engine.setModel("propertyKey", this.getPropertyKey());
 			engine.setModel("rawValue", this.getRawValue());
 			engine.setModel("values", this.getView().getValues());
 			engine.setModels(moreDatas);
 			String result;
			result = engine.parseResource(loaderClass, resource, "UTF-8");
 			return result;
 		} catch (TemplateException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	public String getContextPath() {
 		return contextPath;
 	}
 }
