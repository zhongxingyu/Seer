 package com.xuechong.utils.json;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.opensymphony.xwork2.ActionInvocation;
 import com.opensymphony.xwork2.interceptor.Interceptor;
 
 /**
  * @author xuechong
  */
 public class JsonInterceptor implements Interceptor{
 	
 	private static final Logger logger = Logger.getLogger(JsonInterceptor.class);
 	private static final String KEY = "";
 	private static final String JSON_PAGE = "";
 	private static final String RESULT_NAME = "jsonresult";
 	
 	@Override
 	public String intercept(ActionInvocation invocation) throws Exception {
 		if (invocation.getInvocationContext().
 				getParameters().get(KEY).equals(KEY)){
 			
 			try{
 				invocation.invoke();
 			} catch (Exception e){
 				throw e;//when ex occurs ,just throw it to others
 			}
 			String json = formatJson(invocation);
 			invocation.getStack().setValue(RESULT_NAME, json);
 			return JSON_PAGE;
 		}else{
 			return invocation.invoke();
 		}
 	}
 	
 	private String formatJson(ActionInvocation invocation) throws Exception {
 		Object action = invocation.getAction();
 		Map<String,String> results = new HashMap<String, String>();
 		for (Field field : action.getClass().getDeclaredFields()) {
 			JSON anno = field.getAnnotation(JSON.class);
 			if (anno !=null){
 				results.put(anno.name(), getJsonValue(anno.transFormerClass(),field.get(action)));
 			}
 		}
 		
 		for (Method getter : action.getClass().getDeclaredMethods()) {
 			JSON anno = getter.getAnnotation(JSON.class);
 			if(anno!=null){
 				results.put(anno.name(), getJsonValue(anno.transFormerClass(),getter.invoke(action)));
 			}
 		}
 		StringBuilder json = new StringBuilder("[");
 		for (String key : results.keySet()) {
 			json.append("\"").append(key).append("\"")
			.append(":").append(results.get(key));
 		}
 		json.append("]");
 		return json.toString();
 	}
 	
 	private String getJsonValue(Class transformerCla,Object fieldValue) 
 	throws InstantiationException, IllegalAccessException{
 		JsonTransformer transFormer= (JsonTransformer) transformerCla.newInstance();
 		return transFormer.transForm(fieldValue);
 	}
 
 	@Override
 	public void destroy() {}
 	@Override
 	public void init() {}
 }
