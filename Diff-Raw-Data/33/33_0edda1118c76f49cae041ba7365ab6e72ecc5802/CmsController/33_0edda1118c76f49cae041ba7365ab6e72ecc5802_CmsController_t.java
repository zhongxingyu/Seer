 package com.slyak.cms.web;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.FilenameUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.BeanUtils;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.MessageSource;
 import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
 import org.springframework.util.CollectionUtils;
 import org.springframework.util.ObjectUtils;
 import org.springframework.util.StringUtils;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.support.DefaultDataBinderFactory;
 import org.springframework.web.bind.support.WebDataBinderFactory;
 import org.springframework.web.context.ServletContextAware;
 import org.springframework.web.context.request.NativeWebRequest;
 import org.springframework.web.method.support.InvocableHandlerMethod;
 import org.springframework.web.method.support.ModelAndViewContainer;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
 import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;
 import org.springframework.web.servlet.mvc.method.annotation.ServletRequestDataBinderFactory;
 import org.springframework.web.servlet.view.RedirectView;
 import org.springframework.web.util.UrlPathHelper;
 
 import com.slyak.cms.core.model.Global;
 import com.slyak.cms.core.model.Page;
 import com.slyak.cms.core.model.Widget;
 import com.slyak.cms.core.service.CmsService;
 import com.slyak.cms.core.support.ClassUtils;
 import com.slyak.cms.core.support.Setting;
 import com.slyak.cms.core.support.WidgetInfo;
 import com.slyak.cms.core.support.WidgetManager;
 import com.slyak.core.io.FileUtils;
 
 import freemarker.template.Configuration;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 
 @Controller
 public class CmsController implements ServletContextAware,InitializingBean{
 	
 	private static final Logger LOGGER = LoggerFactory.getLogger(CmsController.class);
 
 	private ExecutorService executor = Executors.newScheduledThreadPool(100);
 
 	@Autowired
 	@Qualifier("layout")
 	private Configuration layoutConfiguration;
 	
 	@Autowired
 	@Qualifier("border")
 	private Configuration borderConfiguration;
 	
 	@Autowired
 	private WidgetManager widgetManager;
 	
 	@Autowired
 	private CmsService cmsService;
 	
 	@Autowired
 	private RequestMappingHandlerAdapter requestMappingHandlerAdapter;
 	
 	private UrlPathHelper urlPathHelper = new UrlPathHelper();
 	
 	@Autowired
 	private MessageSource messageSource;
 	
 	private Set<String> layoutNames = new HashSet<String>();
 	
 	private ServletContext servletContext;
 	
 	@Value("#{config['page.layout.location']}")
 	private String layoutLocation;
 	
 	
 	private void loadLayoutNames() throws FileNotFoundException{
 		File d = FileUtils.getFile(layoutLocation, servletContext);
 		if(d.isDirectory()){
 			File[] files = d.listFiles();
 			for (File f : files) {
 				layoutNames.add(FilenameUtils.getBaseName(f.getName()));
 			}
 		}
 	}
 	
 	@RequestMapping("/")
 	public String welcome(final NativeWebRequest request,final Locale locale,ModelMap modelMap) throws InterruptedException, TemplateException, IOException{
 		List<Page> pages = cmsService.findRootPages();
 		if(CollectionUtils.isEmpty(pages)){
 			return "core.welcome";
 		} else {
 			Page page = pages.get(0);
 			return renderPage(page, request, locale, modelMap);
 		}
 	}
 	
 	@RequestMapping(value="/layout/change",method=RequestMethod.GET)
 	public String layoutChange(ModelMap modelMap){
 		modelMap.put("layoutNames", layoutNames);
 		return "alone:core.layoutChange";
 	}
 	
 	@RequestMapping(value="/layout/change",method=RequestMethod.POST)
 	public String layoutChange(Long pageId,String newLayout){
 		Page page = cmsService.findPageById(pageId);
 		cmsService.changePageLayout(page,newLayout);
 		return "redirect:/"+page.getAlias();
 	}
 	
 	
 	@RequestMapping("/{alias}")
 	public String page(@PathVariable String alias,final NativeWebRequest request,final ModelAndViewContainer container,final Locale locale,ModelMap modelMap,HttpServletResponse response) throws InterruptedException,TemplateException, IOException {
 		//try to find page
 		Page page = cmsService.findPageByAlias(alias);
 		if(page == null){
 			response.sendError(HttpServletResponse.SC_NOT_FOUND);
 			return null;
 		}else{
 			return renderPage(page, request, locale, modelMap);
 		}
 	}
 	
 	// render page
 	private String renderPage(Page page,final NativeWebRequest request,final Locale locale,final ModelMap sharedModel) throws InterruptedException,TemplateException, IOException {
 		sharedModel.put("pages", cmsService.findRootPages());
 		sharedModel.put("currentPage", page);
 		
 		List<Widget> widgets = /*page.getWidgets();*/cmsService.findWidgetsByPageId(page.getId());
 		final Map<String,List<Widget>> containerMap = new ConcurrentHashMap<String, List<Widget>>();
 		
 		int fs = widgets.size();
 		final CountDownLatch latch=new CountDownLatch(fs);
 		for (int i = 0; i < fs; i++) {
 			final Widget widget = widgets.get(i);
 			
 			executor.submit(new Callable<Widget>() {
 				@Override
 				public Widget call() throws Exception {
					
					try{
						//render
						renderWidget(sharedModel,widget,request,locale,false, null);
						//put
						String container = widget.getContainer();
						List<Widget> widgets = containerMap.get(container);
						if(widgets == null){
							widgets = new ArrayList<Widget>();
							containerMap.put(container, widgets);
						}
						widgets.add(widget);
					} finally{
						latch.countDown();
 					}
 					return widget;
 				}
 			});
 		}
 		latch.await();
 		
 		//sort by rank
 		Iterator<List<Widget>> rclIt= containerMap.values().iterator();
 		while(rclIt.hasNext()){
 			Collections.sort(rclIt.next());
 		}
 		
 		Map<String, Object> lmodel = new HashMap<String, Object>();
 		lmodel.put("containerMap", containerMap);
 		String content = FreeMarkerTemplateUtils.processTemplateIntoString(layoutConfiguration.getTemplate(page.getLayout(),locale), lmodel);
 		sharedModel.put("content", content);
 		
 		return "core.page";
 	}
 	
 	
 	@RequestMapping(value="/page/create",method=RequestMethod.GET)
 	public String pageCreate(Long pageId,ModelMap modelMap){
 		if(pageId!=null){
 			modelMap.put("currentPage", cmsService.findPageById(pageId));
 		}
 		return "alone:core.pageCreate";
 	}
 	
 	@RequestMapping(value="/page/create",method=RequestMethod.POST)
 	public String pageCreate(Page page,boolean copy,boolean isChildren,Long pageId){
 		if(isChildren){
 			page.setParent(cmsService.findPageById(pageId));
 		}
 		if(copy){
 			Page sourcePage = cmsService.findPageById(pageId);
 			loopSetPage(page,sourcePage);
 		}
 		return savePage(page);
 	}
 	
 	private void loopSetPage(Page page,Page sourcePage){
 		if(page.getName()==null){
 			page.setName(sourcePage.getName());
 		}
 		page.setLayout(sourcePage.getLayout());
 		List<Widget> widgets = /*sourcePage.getWidgets()*/cmsService.findWidgetsByPageId(sourcePage.getId());
 		if(!CollectionUtils.isEmpty(widgets)){
 			List<Widget> newWidgets = new ArrayList<Widget>();
 			for (Widget widget : widgets) {
 				Widget newWidget = new Widget();
 				newWidget.setContainer(widget.getContainer());
 				newWidget.setBorderClass(widget.getBorderClass());
 				newWidget.setName(widget.getName());
 				newWidget.setRank(widget.getRank());
 				if(!CollectionUtils.isEmpty(widget.getSettings())){
 					newWidget.setSettings(new HashMap<String, String>(widget.getSettings()));
 				}
 				newWidget.setTitle(widget.getTitle());
 				newWidget.setBorderTpl(widget.getBorderTpl());
 				newWidget.setPage(page);
 				newWidgets.add(newWidget);
 			}
 //			page.setWidgets(newWidgets);
 			cmsService.saveWidgets(newWidgets);
 		}
 		List<Page> sc = sourcePage.getChildren();
 		if(!CollectionUtils.isEmpty(sc)){
 			List<Page> children = new ArrayList<Page>();
 			for (Page sp : sc) {
 				Page p = new Page();
 				p.setAlias(UUID.randomUUID().toString());
 				p.setParent(page);
 				loopSetPage(p,sp);
 				children.add(p);
 			}
 			page.setChildren(children);
 		}
 	}
 	
 	private String savePage(Page page){
 		cmsService.savePage(page);
 		return "redirect:/"+page.getAlias();
 	}
 	
 	@RequestMapping("/page/remove")
 	public String pageRemove(Long id){
 		cmsService.removePageById(id);
 		return "redirect:/";
 	}
 	
 	@RequestMapping(value="/page/edit",method=RequestMethod.GET)
 	public String pageEdit(Long id,ModelMap modelMap){
 		modelMap.put("currentPage", cmsService.findPageById(id));
 		return "alone:core.pageEdit"; 
 	}
 	
 	@RequestMapping(value="/page/edit",method=RequestMethod.POST)
 	public String pageEdit(Page page){
 		return savePage(page);
 	}
 	
 	
 	@RequestMapping(value="/global/edit",method=RequestMethod.GET)
 	public String globalEdit(ModelMap modelMap){
 		modelMap.put("global", cmsService.findGlobal());
 		return "alone:core.globalEdit"; 
 	}
 	
 	@RequestMapping(value="/global/edit",method=RequestMethod.POST)
 	public String globalEdit(Long pageId,Global global) throws IOException{
 		cmsService.saveGlobal(global);
 		if(pageId==null){
 			return "redirect:/";
 		}else{
 			return "redirect:/"+cmsService.findPageById(pageId).getAlias();
 		}
 	}
 	
 	@RequestMapping(value="/widget/add",method=RequestMethod.GET)
 	public String widgetAdd(ModelMap modelMap){
 		//find page
 		modelMap.put("widgetInfoMap", widgetManager.getWidgetInfos());
 		return "alone:core.widgetAdd";
 	}
 	
 	@RequestMapping(value="/widget/add",method=RequestMethod.POST)
 	@ResponseBody
 	public boolean widgetAdd(Long pageId,String widgetName,final NativeWebRequest request,final ModelAndViewContainer container) throws Exception{
 		Page page = cmsService.findPageById(pageId);
 		Widget widget = createWidget(page,widgetName);
 		cmsService.saveWidget(widget);
 		
 		//on widget add
 		WidgetInfo widgetInfo = findWidgetInfoByWidgetName(widgetName);
 		Method onAdd = widgetInfo.getOnAdd();
 		if(onAdd!=null){		
 			handleWidgetEvent(widgetInfo.getHandler(),onAdd,widget,request,container);
 		}
 		return true;
 	}
 	
 	@RequestMapping(value="/widget/remove",method=RequestMethod.POST)
 	@ResponseBody
 	public boolean widgetRemove(Long widgetId,final NativeWebRequest request,final ModelAndViewContainer container) throws Exception{
 		
 		Widget widget = cmsService.findWidgetById(widgetId);
 		WidgetInfo widgetInfo = findWidgetInfoByWidgetName(widget.getName());
 		
 		//on widget remove
 		Method onRemove = widgetInfo.getOnRemove();
 		if(onRemove!=null){		
 			handleWidgetEvent(widgetInfo.getHandler(),onRemove,widget,request,container);
 		}
 		
 		cmsService.removeWidgetById(widgetId);
 		
 		
 		return true;
 	}
 	
 	@RequestMapping(value="/widget/edit",method=RequestMethod.GET)
 	public String widgetEdit(Long widgetId,ModelMap modelMap,final NativeWebRequest request,final ModelAndViewContainer container) throws Exception{
 		Widget widget = cmsService.findWidgetById(widgetId);
 		modelMap.put("widget", widget);
 		WidgetInfo widgetInfo = findWidgetInfoByWidgetName(widget.getName());
 		Map<String,String> mergedSettings = mergeSettings(widgetInfo.getSettingsMap(),widget.getSettings());
 		modelMap.put("mergedSettings", mergedSettings);
 		modelMap.put("settings", prepare(widgetInfo.getHandler(),widgetInfo.getSettings(), request, container));
 		return "alone:core.widgetEdit";
 	}
 	
 	private List<Setting> prepare(Object handler,List<Setting> settings,final NativeWebRequest request,final ModelAndViewContainer container) throws Exception{
 		if(CollectionUtils.isEmpty(settings)){
 			return Collections.EMPTY_LIST;
 		}else{
 			List<Setting> prepared = new ArrayList<Setting>();
 			for (Setting setting : settings) {
 				Method optionsLoader = setting.getOptionsLoader();
 				if(optionsLoader == null){
 					prepared.add(setting);
 				}else{
 					Setting s = new Setting();
 					BeanUtils.copyProperties(setting, s,new String[]{"options"});
 					InvocableHandlerMethod handlerMethod = createInitBinderMethod(handler, optionsLoader);
 					Object result = handlerMethod.invokeForRequest(request, container);
 					Object[] array = ObjectUtils.toObjectArray(result);
 					String[] casted = new String[array.length];
 					for (int i = 0; i < array.length; i++) {
 						casted[i] = ObjectUtils.getDisplayString(array[i]);
 					}
 					s.setOptions(casted);
 				}
 			}
 			return prepared;
 		}
 	}
 	
 	@RequestMapping(value="/widget/edit",method=RequestMethod.POST)
 	@ResponseBody
 	public boolean widgetEdit(@RequestBody Widget widget,final NativeWebRequest request,final ModelAndViewContainer container) throws Exception{
 		Long widgetId = widget.getId();
 		Widget stored = cmsService.findWidgetById(widgetId);
 		stored.setBorderClass(widget.getBorderClass());
 		stored.setBorderTpl(widget.getBorderTpl());
 		stored.setSettings(widget.getSettings());
 		stored.setTitle(widget.getTitle());
 		cmsService.saveWidget(stored);
 		
 		//on widget add
 		WidgetInfo widgetInfo = findWidgetInfoByWidgetName(stored.getName());
 		Method onEdit = widgetInfo.getOnEdit();
 		if(onEdit!=null){		
 			handleWidgetEvent(widgetInfo.getHandler(),onEdit,stored,request,container);
 		}
 		return true;
 	}
 	
 	private WidgetInfo findWidgetInfoByWidgetName(String widgetName){
 		String[] regionAndName = StringUtils.split(widgetName,".");
 		return widgetManager.getWidgetInfo(regionAndName[0], regionAndName[1]);
 	}
 	
 	private void handleWidgetEvent(Object handler,Method method,Widget widget,final NativeWebRequest request,final ModelAndViewContainer container) throws Exception{
 		InvocableHandlerMethod handlerMethod = createInitBinderMethod(handler, method);
 		handlerMethod.invokeForRequest(request, container, widget);
 	}
 	
 	
 	private Widget createWidget(Page page,String widgetName){
 		Widget w = new Widget();
 		//TODO
 		w.setPage(page);
 		w.setName(widgetName);
 		w.setContainer("row0");
 		w.setRank(-1);
 		w.setBorderTpl("t-c-f.tpl");
 		return w;
 	}
 	
 	@RequestMapping("/widget/{widgetId}")
 	public String widget(@PathVariable Long widgetId,boolean noborder,String border,final NativeWebRequest request,final Locale locale,ModelMap modelMap) throws Exception{
 		Widget widget = cmsService.findWidgetById(widgetId);
 		renderWidget(null,widget,request,locale,noborder, border);
 		modelMap.put("widget", widget);
 		return "core.widget";
 	}
 	
 	@RequestMapping("/widget/sort")
 	@ResponseBody
 	public boolean widgetSort(@RequestBody List<Widget> widgets){
 		cmsService.updateWidgets(widgets,true);
 		return true;
 	}
 	
 	private void renderWidget(ModelMap sharedModel,Widget widget,final NativeWebRequest request,final Locale locale,boolean noborder, String borderClass) {
 		long start = System.currentTimeMillis();
 		String[] regionAndName = StringUtils.split(widget.getName(),".");
 		WidgetInfo widgetInfo = widgetManager.getWidgetInfo(regionAndName[0],regionAndName[1]);
 		
 		Map<String,String> mergedSettings = mergeSettings(widgetInfo.getSettingsMap(),widget.getSettings());
 		//invoke
 		//TODO : optimize it
 		Object handler = widgetInfo.getHandler();
 		ServletInvocableHandlerMethod handlerMethod = createServletInvocableHandlerMethod(handler,widgetInfo.getMethod());
 		
 		ModelAndViewContainer container = new ModelAndViewContainer();
 		
 		String content = null;
 		try{
 			widget.setSettings(mergedSettings);
 			
 			Object mtpl = handlerMethod.invokeForRequest(request,container,widget);
 			
 			ModelMap model = container.getModel();
 			model.addAllAttributes(sharedModel);
 			String ctx = urlPathHelper.getContextPath(request.getNativeRequest(HttpServletRequest.class));
 			//common attrs
 			model.addAttribute("ctx", ctx);
 			model.addAttribute("view", ctx+"/view/"+widget.getPage().getAlias());
 			model.addAttribute("action", ctx+"/action/"+widget.getPage().getAlias());
 			model.addAttribute("resource", ctx+"/widgetResource/"+regionAndName[0]);
 			model.addAttribute("widget", widget);
 			
 			Template fmTemplate = null;
 			if(mtpl.getClass().isAssignableFrom(Template.class)){
 				fmTemplate = (Template)mtpl;
 			}else{
 				//render content
 				Configuration widgetConfiguration = widgetManager.getConfiguration(handler);
 				fmTemplate = widgetConfiguration.getTemplate((String)mtpl,locale);
 			}
 			content = FreeMarkerTemplateUtils.processTemplateIntoString(fmTemplate, model);
 			widget.setContent(content);
 		}catch(Exception e){
 			widget.setContent(e.getMessage());
 		}
 		
 		if(!noborder){
 			String borderTpl = widget.getBorderTpl();
 			if(borderTpl != null){
 				//set title
 				String title = widget.getTitle();
 				if(!StringUtils.hasText(title)){
 					widget.setTitle(messageSource.getMessage(widget.getName(),null, locale));
 				}
 				//render content with border
 				Map<String,Object> cmodel = new HashMap<String, Object>();
 				if(borderClass!=null){
 					widget.setBorderClass(borderClass);
 				}
 				cmodel.put("widget", widget);
 				try {
 					content = FreeMarkerTemplateUtils.processTemplateIntoString(borderConfiguration.getTemplate(borderTpl,locale), cmodel);
 					//the final widget content
 					widget.setContent(content);
 				} catch (Exception e) {
 					widget.setContent(e.getMessage());
 				}
 			}
 		}
 		long end = System.currentTimeMillis();
 		LOGGER.info("Widget {} rendered ,it cost {} ms",new Object[]{widget.getId(),end-start});
 	}
 	
 	private Map<String,String> mergeSettings(Map<String,String> defaultSettings,Map<String,String> settings){
 		if(CollectionUtils.isEmpty(settings)){
 			return defaultSettings;
 		}else{
 			Map<String,String> mergedSettings = new HashMap<String, String>(defaultSettings);
 			Iterator<String> kit = mergedSettings.keySet().iterator();
 			while(kit.hasNext()){
 				String key = kit.next();
 				String storedValue = settings.get(key);
 				if(storedValue!=null){
 					mergedSettings.put(key, storedValue);
 				}
 			}
 			return mergedSettings;
 		}
 	}
 	
 	@RequestMapping("/view/{parentPageAlias}/{region}/{widgetName}")
 	public String renderPageByWidget(@PathVariable String parentPageAlias,@PathVariable String region,@PathVariable String widgetName,final NativeWebRequest request,final Locale locale,ModelMap modelMap) throws InterruptedException, TemplateException, IOException{
 		String wname = region+'.'+widgetName;
 		Page page = cmsService.findPageByWidgetName(wname);
 		if(page == null){
 			//create a child page
 //			page = new Page();
 //			page.setName(region+widgetName);
 //			page.setParent(cmsService.findPageByAlias(parentPageAlias));
 //			WidgetInfo widgetInfo = widgetManager.getWidgetInfo(region, widgetName);
 //			page.setShow(widgetInfo.isShow());
 //			List<Widget> widgets = new ArrayList<Widget>();
 //			widgets.add(createWidget(page, wname));
 //			page.setWidgets(widgets);
 //			cmsService.savePage(page);
 		}	
 		return renderPage(page, request, locale, modelMap);
 	}
 	
 	@RequestMapping("/action/{currentPageAlias}/{region}/{methodName}")
 	public ModelAndView actionByWidget(@PathVariable String currentPageAlias,@PathVariable String region,@PathVariable String methodName,final NativeWebRequest request,final ModelAndViewContainer container) throws Exception{
 		Page page = cmsService.findPageByAlias(currentPageAlias);
 		Object handler = widgetManager.getRegionHandler(region);
 		Method method = null;
 		if(handler!=null){
 			method = ClassUtils.getMethodByName(handler.getClass(), methodName);
 		}
 		if(method!=null){
 			ServletInvocableHandlerMethod handlerMethod = createServletInvocableHandlerMethod(handler, method);
 			handlerMethod.invokeForRequest(request, container,page);
 		}
 		return new ModelAndView(new RedirectView("/"+currentPageAlias),container.getModel());
 	}
 	
 	@RequestMapping("/core/error404")
 	public String error404(ModelMap modelMap){
 		List<Page> pages = cmsService.findRootPages();
 		modelMap.put("pages", pages);
 		return "core.error404";
 	}
 	
 	private InvocableHandlerMethod createInitBinderMethod(Object bean, Method method) {
 		InvocableHandlerMethod binderMethod = new InvocableHandlerMethod(bean, method);
 		binderMethod.setHandlerMethodArgumentResolvers(requestMappingHandlerAdapter.getArgumentResolvers());
 		binderMethod.setDataBinderFactory(new DefaultDataBinderFactory(requestMappingHandlerAdapter.getWebBindingInitializer()));
 		binderMethod.setParameterNameDiscoverer(new LocalVariableTableParameterNameDiscoverer());
 		return binderMethod;
 	}
 	
 	private WebDataBinderFactory getWebDataBinderFactory(Object handler){
 		return new ServletRequestDataBinderFactory(null, requestMappingHandlerAdapter.getWebBindingInitializer());
 	}
 	
 	private ServletInvocableHandlerMethod createServletInvocableHandlerMethod(Object handler, Method method) {
 		ServletInvocableHandlerMethod servletInvocableHandlerMethod = new ServletInvocableHandlerMethod(handler, method);
 		servletInvocableHandlerMethod.setDataBinderFactory(getWebDataBinderFactory(handler));
 		servletInvocableHandlerMethod.setHandlerMethodArgumentResolvers(requestMappingHandlerAdapter.getArgumentResolvers());
 		servletInvocableHandlerMethod.setHandlerMethodReturnValueHandlers(requestMappingHandlerAdapter.getReturnValueHandlers());
 		return servletInvocableHandlerMethod;
 	}
 
 
 	@Override
 	public void setServletContext(ServletContext servletContext) {
 		this.servletContext = servletContext;
 	}
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		loadLayoutNames();
 	}
 	
 }
