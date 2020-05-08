 package com.chezhu.web;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts2.interceptor.ServletRequestAware;
 import org.apache.struts2.interceptor.ServletResponseAware;
 
 import com.chezhu.dao.FilterViewService;
 import com.chezhu.dao.SparkViewService;
 import com.chezhu.dao.StyleViewService;
 import com.chezhu.dao.SupplyService;
 import com.chezhu.dao.UnknownRecordService;
 import com.chezhu.dao.model.FilterView;
 import com.chezhu.dao.model.SparkView;
 import com.chezhu.dao.model.StyleView;
 import com.chezhu.dao.model.Supply;
 import com.chezhu.dao.model.UnknownRecord;
 import com.chezhu.util.ContextUtil;
 import com.chezhu.util.StyleNameCache;
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.ActionSupport;
 
 public class QueryAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {
 	
 	private HttpServletResponse response;
 	private HttpServletRequest request;
 	
 	
 	private FilterViewService filterViewService = ContextUtil.getBean(FilterViewService.class, "filterViewService");
 	private StyleViewService styleViewService = ContextUtil.getBean(StyleViewService.class, "styleViewService");
 	private SupplyService supplyService = ContextUtil.getBean(SupplyService.class, "supplyService");
 	private SparkViewService sparkViewService = ContextUtil.getBean(SparkViewService.class, "sparkViewService");
 	private UnknownRecordService unknownRecordService = ContextUtil.getBean(UnknownRecordService.class, "unknownRecordService");
 	
 	private String filterQueryStr;
 	private List<String> filterSupplyItem;
 	private String sparkQueryStr;
 	private List<String> sparkSupplyItem;
 
 	public String getFilterQueryStr() {
 		return filterQueryStr;
 	}
 
 	public void setFilterQueryStr(String filterQueryStr) {
 		this.filterQueryStr = filterQueryStr;
 	}
 
 	public List<String> getFilterSupplyItem() {
 		return filterSupplyItem;
 	}
 
 	public void setFilterSupplyItem(List<String> filterSupplyItem) {
 		this.filterSupplyItem = filterSupplyItem;
 	}
 
 	public String getSparkQueryStr() {
 		return sparkQueryStr;
 	}
 
 	public void setSparkQueryStr(String sparkQueryStr) {
 		this.sparkQueryStr = sparkQueryStr;
 	}
 
 	public List<String> getSparkSupplyItem() {
 		return sparkSupplyItem;
 	}
 
 	public void setSparkSupplyItem(List<String> sparkSupplyItem) {
 		this.sparkSupplyItem = sparkSupplyItem;
 	}
 
 	// 使用Ajax方式查询所有车型列表
 	public String init() throws Exception {
 		String data = StyleNameCache.getInstance().getAllStyleNamesJson();
 		//System.out.println(data);
 		response.setContentType("text/json; charset=UTF-8");
 		response.setCharacterEncoding("UTF-8");
 		response.getWriter().println(data);
 		response.getWriter().flush();
 		response.getWriter().close();
 		return null;
 	}
 
 	// 查询三滤数据
 	public String queryFilter() throws Exception {
 		ActionContext context = ActionContext.getContext();
 		List<StyleView> styles = styleViewService.query(filterQueryStr);
 		if(styles == null || styles.size() == 0) {
 			saveUnknownRecord(filterQueryStr);
 			context.put("noresult", "yes");
 			return SUCCESS;
 		}
 		
 		Map<String, Map<String, FilterView>> filters = filterViewService.queryFilters(filterQueryStr, filterSupplyItem);
 		// 为前台页面显示结果排序用，后期可以考虑优化为内存提取数据
 		List<Supply> supplies = supplyService.getSuppliesById(filterSupplyItem);
 		
 		context.put("styles", styles);
 		context.put("filters", filters);
 		context.put("orderFilterSupplies", supplies);
 
 		return SUCCESS;
 		
 	}
 	
 	// 查询三滤数据
 	public String querySpark() throws Exception {
 		ActionContext context = ActionContext.getContext();
 		
 		List<StyleView> styles = styleViewService.query(sparkQueryStr);
 		if(styles == null || styles.size() == 0) {
 			saveUnknownRecord(sparkQueryStr);
 			context.put("noresult", "yes");
 			return SUCCESS;
 		}
 		
 		Map<String, Map<String, SparkView>> sparks = sparkViewService.querySparks(sparkQueryStr, sparkSupplyItem);
 		// 为前台页面显示结果排序用，后期可以考虑优化为内存提取数据
 		List<Supply> supplies = supplyService.getSuppliesById(sparkSupplyItem);
 		
 		
 		context.put("styles", styles);
 		context.put("sparks", sparks);
 		context.put("orderSparkSupplies", supplies);
 
 		return SUCCESS;
 	}
 	
 	private void saveUnknownRecord(String record) {
 		String ip = request.getRemoteAddr();
		System.out.println(ip);
 		
 		UnknownRecord unknownRecord = new UnknownRecord();
 		unknownRecord.setRecord(record);
 		unknownRecord.setIp(ip);
 		unknownRecord.setCreateTime(new Date());
 		
 		unknownRecordService.addUnknownRecord(unknownRecord);
 	}
 	
 	// 支持
 	public String support() throws Exception {
 		
 		return SUCCESS;
 	}
 	
 	// 建议
 	public String suggest() throws Exception {
 		
 		return SUCCESS;
 	}
 
 	public void setServletResponse(HttpServletResponse response) {
 		this.response = response;
 	}
 
	@Override
 	public void setServletRequest(HttpServletRequest request) {
 		this.request = request;
 	}
 	
 	
 }
