 package framework.base.controller;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.context.request.RequestContextHolder;
 import org.springframework.web.context.request.ServletRequestAttributes;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.googlecode.jsonplugin.JSONException;
 import com.googlecode.jsonplugin.JSONUtil;
 
 import framework.base.common.BaseConstants;
 import framework.base.common.BaseConstants.CacheKey;
 import framework.base.common.BaseConstants.CommonPageParam;
 import framework.base.utils.PublicCacheUtil;
 
 /**
  * @author hjin
  * @cratedate 2013-8-5 上午11:01:11
  */
 @Controller
 public abstract class BaseController implements Serializable
 {
 	private static final long serialVersionUID = -3740647827551448127L;
 	/**
 	 * 项目路径
 	 */
 	protected String contextPath;
 	/**
 	 * 请求访问后缀
 	 */
 	protected String controllerSuffix;
 
 	/**
 	 * @return the controllerSuffix
 	 */
 	@ModelAttribute
 	public void _init()
 	{
 		controllerSuffix = PublicCacheUtil
 		        .getString(CacheKey.GLOBAL_CONTROLLER_SUFFIX);
 
 		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
 		        .getRequestAttributes()).getRequest();
 		contextPath = request.getContextPath();
 	}
 
 	/**
 	 * 日志对象
 	 */
 	protected Logger logger;
 
 	/**
 	 * 初始化日志对象,在子类中实现,以子类的class进行初始化
 	 * 
 	 * @author hjin
 	 * @cratedate 2013-8-6 下午4:28:53
 	 */
 	@ModelAttribute
 	public abstract void setLogger();
 
 	/**
 	 * 返回通用成功页面
 	 * 
 	 * @param msg
 	 * @param continueUrl
 	 * @return
 	 * @author hjin
 	 * @cratedate 2013-8-20 上午10:25:23
 	 */
 	public ModelAndView createOkModel(ModelAndView model, String msg,
 	        String continueUrl)
 	{
 		ModelAndView m = model != null ? model : new ModelAndView(
 		        PublicCacheUtil.getString(CommonPageParam.GLOBAL_PAGE_RESULT));
 		
 		if (m.getViewName() == null)
 		{
 			m.setViewName(PublicCacheUtil
 			        .getString(CommonPageParam.GLOBAL_PAGE_RESULT));
 		}
 		
 		m.addObject(BaseConstants.CommonPageParam.PROCESS_RESULT, "1");
 		m.addObject(BaseConstants.CommonPageParam.SHOW_MSG, msg);
 		m.addObject(BaseConstants.CommonPageParam.PAGE_CONTINUE_URL,
 		        continueUrl);
 		return m;
 	}
 
 	/**
 	 * 返回通用失败页面
 	 * 
 	 * @param msg
 	 * @param continueUrl
 	 * @return
 	 * @author hjin
 	 * @cratedate 2013-8-20 上午10:25:23
 	 */
 	public ModelAndView createFailModel(ModelAndView model, String msg,
 	        String continueUrl)
 	{
 		ModelAndView m = model != null ? model : new ModelAndView(
 		        PublicCacheUtil.getString(CommonPageParam.GLOBAL_PAGE_RESULT));

		if (m.getViewName() == null)
		{
			m.setViewName(PublicCacheUtil
			        .getString(CommonPageParam.GLOBAL_PAGE_RESULT));
		}
		
 		m.addObject(BaseConstants.CommonPageParam.PROCESS_RESULT, "0");
 		m.addObject(BaseConstants.CommonPageParam.SHOW_MSG, msg);
 		m.addObject(BaseConstants.CommonPageParam.PAGE_CONTINUE_URL,
 		        continueUrl);
 		return m;
 	}
 
 	/**
 	 * 返回msg
 	 * 
 	 * @param json
 	 * @author hjin
 	 * @cratedate 2013-9-14 下午5:04:34
 	 */
 	public void ajaxResponse(Object responseObj, HttpServletResponse response)
 	{
 		try
 		{
 			response.setCharacterEncoding("utf-8");
 			response.getWriter().print(JSONUtil.serialize(responseObj));
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		catch (JSONException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * 表示成功的jsonmap
 	 * 
 	 * @return
 	 * @author hjin
 	 * @cratedate 2013-9-19 下午7:38:11
 	 */
 	public Map<String, Object> createSuccessJSONMap()
 	{
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("result", "1");
 		return map;
 	}
 
 	/**
 	 * 表示失败的jsonmap
 	 * 
 	 * @return
 	 * @author hjin
 	 * @cratedate 2013-9-19 下午7:38:11
 	 */
 	public Map<String, Object> createFailJSONMap(String failMsg)
 	{
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("result", "0");
 		map.put("message", failMsg);
 		return map;
 	}
 }
