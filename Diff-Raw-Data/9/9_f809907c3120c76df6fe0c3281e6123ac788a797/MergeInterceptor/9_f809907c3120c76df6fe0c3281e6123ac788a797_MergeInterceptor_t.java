 package com.dianping.wizard.widget.interceptor;
 
 import com.dianping.wizard.config.Configuration;
 import com.dianping.wizard.exception.WidgetException;
 import com.dianping.wizard.widget.InvocationContext;
 import com.dianping.wizard.widget.Mode;
 import com.dianping.wizard.widget.Widget;
 import com.dianping.wizard.widget.merger.FreemarkerMerger;
 import com.dianping.wizard.widget.merger.Merger;
 import com.dianping.wizard.widget.merger.Template;
 import freemarker.ext.beans.BeansWrapper;
 import freemarker.template.TemplateHashModel;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ltebean
  * Date: 13-4-24
  * Time: 上午9:13
  * To change this template use File | Settings | File Templates.
  */
 public class MergeInterceptor implements Interceptor {
 
     private final Map<String, Object> staticModels = new HashMap<String, Object>();
 
     private final Merger merger = FreemarkerMerger.getInstance();
 
     public MergeInterceptor() {
         List<String> modelList = Configuration.get("freemarker.staticModels", null, List.class);
         if (CollectionUtils.isNotEmpty(modelList)) {
             BeansWrapper wrapper = BeansWrapper.getDefaultInstance();
             TemplateHashModel models = wrapper.getStaticModels();
             for (String clazz : modelList) {
                 try {
                     TemplateHashModel model = (TemplateHashModel) models.get(clazz);
                     staticModels.put(Class.forName(clazz).getSimpleName(), model);
                 } catch (Exception e) {
                     throw new WidgetException("static model initialization error", e);
                 }
             }
         }
     }
 
     @Override
     public String intercept(InvocationContext invocation) throws Exception {
         String resultCode = invocation.invoke();
         if (InvocationContext.NONE.equals(resultCode)) {
             return resultCode;
         }
         Widget widget = invocation.getWidget();
         Mode mode = widget.modes.get(invocation.getModeType());
         if (mode == null) {
             throw new WidgetException("widget(" + widget.name + ") does not support mode:" + invocation.getModeType() + "");
         }
 
         //inject staticModels
         invocation.getContext().putAll(staticModels);
 
         //merge script and put it into the context
        String finalScript=invocation.getScript();
         if (StringUtils.isNotEmpty(mode.script)) {
             String templateName = Template.generateName(widget.name, invocation.getModeType(), "script");
             String script = merger.merge(new Template(templateName,mode.script), invocation.getContext());
            finalScript+=script;
         }
        invocation.getContext().put("script", finalScript);
        invocation.setScript(finalScript);
         //merge html
         if (StringUtils.isNotEmpty(mode.template)) {
             String templateName = Template.generateName(widget.name, invocation.getModeType(), "template");
             invocation.setOutput(merger.merge(new Template(templateName,mode.template), invocation.getContext()));
         }
         return InvocationContext.SUCCESS;
     }
 }
