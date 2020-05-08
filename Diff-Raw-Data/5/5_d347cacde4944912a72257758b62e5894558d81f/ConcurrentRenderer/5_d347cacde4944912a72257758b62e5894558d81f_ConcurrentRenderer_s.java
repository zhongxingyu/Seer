 package com.dianping.wizard.widget.concurrent;
 
 import com.dianping.wizard.exception.WidgetException;
 import com.dianping.wizard.repo.WidgetRepo;
 import com.dianping.wizard.repo.WidgetRepoFactory;
 import com.dianping.wizard.widget.InvocationContext;
 import com.dianping.wizard.widget.RenderingResult;
 import com.dianping.wizard.widget.Widget;
 import com.dianping.wizard.widget.WidgetRenderer;
 import com.dianping.wizard.widget.extensions.ExtensionsManager;
 import com.dianping.wizard.widget.interceptor.Interceptor;
 import com.dianping.wizard.widget.interceptor.InterceptorConfig;
 import org.apache.log4j.Logger;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.concurrent.Future;
 
 /**
  * @author ltebean
  */
 public class ConcurrentRenderer implements WidgetRenderer {
 
     private WidgetRepo widgetRepo = WidgetRepoFactory.getRepo("default");
 
     private Logger logger = Logger.getLogger(this.getClass());
 
     @Override
     public RenderingResult render(Widget widget, String mode, Map<String, Object> params) {
         if (widget == null) {
             throw new IllegalArgumentException("widget can not be null");
         }
         if (params == null) {
             params = new HashMap<String, Object>();
         }
         Iterator<Interceptor> interceptors = InterceptorConfig.getInstance().getInterceptors("default");
         InvocationContext invocation = new InvocationContext(widget, mode, params, interceptors);
         invocation.getContext().putAll(ExtensionsManager.getInstance().getExtension());
        Map<String, Future<RenderingResult>> tasks = LayoutParser.parseAndExecute(widget, mode, invocation.getContext());
         params.put("CONCURRENT_TASKS",tasks);
         RenderingResult result = new RenderingResult();
         try {
             String resultCode = invocation.invoke();
             if (InvocationContext.SUCCESS.equals(resultCode)) {
                 result.output = invocation.getOutput();
                 result.script = invocation.getScript();
             } else if (InvocationContext.NONE.equals(resultCode)) {
                 return result;
             } else {
                 throw new WidgetException("unknown result code-" + resultCode + " returned by widget:" + widget.name);
             }
         } catch (Exception e) {
             logger.error("rendering error", e);
         }
         return result;
     }
 
     @Override
     public RenderingResult render(String widgetName, String mode, Map<String, Object> params) {
         Widget widget = widgetRepo.loadByName(widgetName);
         if (widget == null) {
             throw new WidgetException("widget not found:" + widgetName);
         }
         return this.render(widget, mode, params);
     }
 
     @Override
     public RenderingResult render(Widget widget, Map<String, Object> params) {
         return this.render(widget,Widget.ModeType.Display.value,params);
     }
 
     @Override
     public RenderingResult render(String widgetName, Map<String, Object> params) {
         return this.render(widgetName,Widget.ModeType.Display.value,params);
     }
 }
