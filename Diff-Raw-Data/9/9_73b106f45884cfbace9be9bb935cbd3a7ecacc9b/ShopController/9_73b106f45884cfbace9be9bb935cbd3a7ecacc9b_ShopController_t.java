 package com.magiccube.shop.action;
 
 import java.io.UnsupportedEncodingException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.BeanUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.annotation.Validated;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.context.request.WebRequest;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.servlet.View;
 import org.springframework.web.servlet.view.RedirectView;
 import org.springframework.web.servlet.view.json.MappingJacksonJsonView;
 
 import com.magiccube.common.model.EnvironmentInfoVO;
 import com.magiccube.config.model.ConfigVO;
 import com.magiccube.config.service.ConfigService;
 import com.magiccube.core.base.action.QueryForm;
 import com.magiccube.core.util.tools.AjaxUtils;
 import com.magiccube.core.util.tools.JsonUtil;
 import com.magiccube.core.util.tools.PosService;
 import com.magiccube.food.action.FoodAction;
 import com.magiccube.food.model.FoodGroupVO;
 import com.magiccube.food.model.FoodQueryCondition;
 import com.magiccube.food.model.FoodVO;
 import com.magiccube.food.model.GroupFoods;
 import com.magiccube.food.model.GroupPackages;
 import com.magiccube.food.model.PackageVO;
 import com.magiccube.order.action.OrderAction;
 import com.magiccube.order.model.OrderQueryCondition;
 import com.magiccube.order.model.OrderVO;
 import com.magiccube.order.model.OrderView;
 import com.magiccube.order.service.OrderWebSocketServlet;
 import com.magiccube.shop.model.FoodForm;
 import com.magiccube.shop.model.FoodReShopForm;
 import com.magiccube.shop.model.GroupForm;
 import com.magiccube.shop.model.OrderQueryForm;
 import com.magiccube.shop.util.ShopUtil;
 import com.magiccube.stat.action.FoodBean;
 import com.magiccube.stat.action.GroupBean;
 import com.magiccube.stat.model.QuantityPerFoodVO;
 import com.magiccube.stat.service.StatService;
 import com.magiccube.user.action.UserAction;
 import com.magiccube.user.model.UserVO;
 
 /**
  * 店铺管理控制器
  * 
  * function method url 
  * --------- ------ -------- 
  * 主页 GET /shop 
  * 订单管理待处理 GET /shop/todo 
  * 订单管理已处理 GET /shop/history 
  * 配餐模式 GET /shop/catering 
  * 套餐模式 GET /shop/package
  * 
  * 分类管理 GET /shop/group 
  * 新增分类 POST /shop/group 
  * 获取一个group GET /shop/group/{id}
  * 删除一个group DELETE /shop/group/{id} 
  * 更新一个group POST /shop/group/{id} 
  * 更新订单状态 PUT /shop/group/{id}?status={value}
  * 
  * 食物管理 GET /shop/food 
  * 获取食物列表(json) GET + produces /shop/food 
  * 新增食物 POST /shop/food 
  * 获取食物 GET /shop/food/{id} 
  * 删除食物 DELETE /shop/food/{id} 
  * 更新食物 POST /shop/food/{id}
  * 
  * 更新是否自动出单 PUT /shop/autoprint/{value} 
  * 出单 PUT /shop/issue/{id}
  * 
  * 新增/更新食物关联 POST /shop/foodreshop 
  * 删除食物关联 DELETE /shop/foodreshop/{id}
  * 更新食物/套餐上下架 PUT /shop/foodreshop/{id}?droped={droped}
  * 
  * 新增套餐 POST /shop/package 
  * 删除套餐 DELETE /shop/package/{id} 
  * 获取套餐 GET /shop/package/{id}
  * 
  * 检测电话号码状态 GET /shop/phonestate?phone={phone} 
  * 批量检测电话号码状态 POST /shop/phonestates?phones={phone1,phone2}
  * 
  * 店铺基本数据 GET /shop/shopdata
  * 店长工作台 GET /shop/dashboard
  * 
  * @author jcchen
  * 
  */
 @Controller
 @RequestMapping("/shop")
 public class ShopController {
 
 	final static Logger logger = LoggerFactory.getLogger(ShopController.class);
 
 	/**
 	 * orderAction
 	 */
 	@Autowired
 	private OrderAction orderAction;
 
 	/**
 	 * foodAction
 	 */
 	@Autowired
 	private FoodAction foodAction;
 
 	/**
 	 * userAction
 	 */
 	@Autowired
 	private UserAction userAction;
 
 	/**
 	 * 将model映射为json返回
 	 */
 	@Autowired
 	private MappingJacksonJsonView mappingJacksonJsonView;
 
 	/**
 	 * 配置服務類
 	 */
 	@Autowired
 	private ConfigService configService;
 	
 	@Autowired
 	private StatService statService;
 
 	// Invoked on every request
 	/**
 	 * 当前请求是否为ajax请求. 每次请求都会调用此方法
 	 * 
 	 * @param request
 	 *            WebRequest
 	 * @param model
 	 *            Model
 	 */
 	@ModelAttribute
 	public void ajaxAttribute(WebRequest request, Model model) {
 		model.addAttribute("ajaxRequest", AjaxUtils.isAjaxRequest(request));
 	}
 
 	/**
 	 * index
 	 * 
 	 * @return index page
 	 */
 	@RequestMapping("")
 	public String toIndex() {
 		return "shop/index";
 	}
 
 	@RequestMapping("/todo")
 	public String toToodo(Model model, @ModelAttribute OrderQueryForm queryForm) {
 
 		OrderQueryCondition condition = new OrderQueryCondition();
 		BeanUtils.copyProperties(queryForm, condition);
 		List<OrderView> list = orderAction.queryNewOrderViewList(condition);
 		model.addAttribute("orderList", list);
 		return "shop/todo";
 	}
 
 	@RequestMapping("/history")
 	public String toHistory(Model model,
 			@ModelAttribute OrderQueryForm queryForm,
 			@RequestParam(required = false) boolean fromMenu) {
 		if (fromMenu) {
 			// set default value
 			Date[] today = getTodayStartEndTime();
 			queryForm.setStartDate(today[0]);
 			queryForm.setEndDate(today[1]);
 		}
 
 		if (queryForm == null) {
 			print("go into history. queryForm = null");
 			queryForm = new OrderQueryForm();
 		}
 		if (queryForm.getPageNo() == 0)
 			queryForm.setPageNo(1);
 		queryForm.setPageSize(6);
 		if (queryForm.getOrderStatus() == null)
 			queryForm.setOrderStatus(OrderVO.ORDER_STATUS_DEALED + ","
 					+ OrderVO.ORDER_STATUS_EXCEPTION + ","
 					+ OrderVO.ORDER_STATUS_EVALUATED);
 
 		OrderQueryCondition condition = new OrderQueryCondition();
 		BeanUtils.copyProperties(queryForm, condition);
 		int count = orderAction.queryHistoryOrdersCount(condition);
 		queryForm.setRecordCount(count);
 		condition.setPage(queryForm);
 
 		List<OrderView> list = orderAction.queryHistoryOrderViewList(condition);
 		model.addAttribute("orderList", list);
 
 		return "shop/history";
 	}
 
 	@RequestMapping("/catering")
 	public String toCatering(Model model) {
 		List<GroupFoods> groupFoodsList = getCateringFoods();
 		model.addAttribute("groupFoodsList", groupFoodsList);
 
 		List<FoodVO> foodList = getAvailableFoods(FoodVO.TYPE_FOOD);
 		model.addAttribute("foodList", foodList);
 		return "shop/catering";
 	}
 
 	@RequestMapping("/package")
 	public String toPackage(Model model) {
 		int shopId = 1;
 
 		// List<GroupFoods> packageFoodsList = getGroupPackages();
 		List<GroupPackages> packageFoodsList = getGroupPackages();
 		model.addAttribute("packageFoodsList", packageFoodsList);
 
 		// 新增套餐时使用
 		FoodQueryCondition foodQueryCondition = new FoodQueryCondition(shopId,
 				FoodVO.TYPE_FOOD);
 		List<GroupFoods> groupFoodsList = foodAction
 				.queryAvailableGroupAndFoods(foodQueryCondition);
 		model.addAttribute("groupFoodsList", groupFoodsList);
 		return "shop/package";
 	}
 
 	@RequestMapping("/group")
 	public String toGroup(Model model) {
 		List<FoodGroupVO> lstGroups = foodAction.queryAllGroups();
 		model.addAttribute("groupList", lstGroups);
 		return "shop/group";
 	}
 
 	@RequestMapping("/food")
 	public String toFood(@ModelAttribute QueryForm queryForm, Model model) {
 		FoodQueryCondition condition = new FoodQueryCondition();
 		int count = foodAction.querySingleFoodsCount();
 		queryForm.setRecordCount(count);
 		condition.setPage(queryForm);
 		List<FoodVO> foods = foodAction.querySingleFoods(condition);
 		model.addAttribute("foodList", foods);
 		return "shop/food";
 	}
 
 	@RequestMapping(value = "/food", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
 	public @ResponseBody
 	List<FoodVO> getAvailableFoods(int type) {
 		int shopId = 1;
 		FoodQueryCondition condition = new FoodQueryCondition();
 		condition.setPageNo(1);
 		condition.setPageSize(Integer.MAX_VALUE);
 		condition.setShopId(shopId);
 		condition.setType(type);
 		List<FoodVO> foods = foodAction.queryAvailableFoods(condition);
 		return foods;
 	}
 
 	/**
 	 * 保存分组.(支持ajax/form submit)
 	 * 
 	 * @return View. 如果是Ajax请求，返回json数据; 如果是普通的Form请求提交，直接跳转到相应的页面.
 	 */
 	@RequestMapping(value = "/group", method = RequestMethod.POST)
 	public View saveGroup(@RequestParam MultipartFile file,
 			@ModelAttribute GroupForm groupForm,
 			@ModelAttribute("ajaxRequest") boolean ajaxRequest, Model model) {
 
 		FoodGroupVO vo = saveGroupToDB(file, groupForm);
 		if (ajaxRequest) {
 			model.addAttribute("image", vo.getImage());
 			model.addAttribute("id", vo.getId());
 			return mappingJacksonJsonView;
 		} else {
 			return new RedirectView("/shop/group", true);
 		}
 	}
 
 	/**
 	 * 保存食物
 	 */
 	@RequestMapping(value = "/food", method = RequestMethod.POST)
 	public View saveFood(@RequestParam MultipartFile file,
 			@ModelAttribute FoodForm foodForm,
 			@ModelAttribute("ajaxRequest") boolean ajaxRequest, Model model) {
 		FoodVO vo = saveFoodToDB(file, foodForm);
 		if (ajaxRequest) {
 			model.addAttribute("image", vo.getImage());
 			model.addAttribute("id", vo.getId());
 			return mappingJacksonJsonView;
 		} else {
 			return new RedirectView("/shop/food", true);
 		}
 	}
 
 	@RequestMapping(value = "/group/{id}", method = RequestMethod.DELETE)
 	public @ResponseBody
 	boolean deleteGroup(@PathVariable int id) {
 		int ret = foodAction.deleteGroup(id);
 		return ret > 0;
 	}
 
 	@RequestMapping(value = "/food/{id}", method = RequestMethod.DELETE)
 	public @ResponseBody
 	boolean deleteFood(@PathVariable int id) {
 		int ret = foodAction.deleteFood(id);
 		return ret > 0;
 	}
 
 	@RequestMapping(value = "/group/{id}", method = RequestMethod.GET)
 	public @ResponseBody
 	FoodGroupVO getGroup(@PathVariable int id) {
 		return foodAction.getGroup(id);
 	}
 
 	@RequestMapping(value = "/food/{id}")
 	public @ResponseBody
 	FoodVO getFood(@PathVariable int id) {
 		return foodAction.getFood(id);
 	}
 
 	@RequestMapping(value = "/group/{id}", method = RequestMethod.POST)
 	public @ResponseBody
 	boolean updateGroup(@RequestParam(required = false) MultipartFile file,
 			@ModelAttribute GroupForm groupForm,
 			@ModelAttribute("ajaxRequest") boolean ajaxRequest, Model model) {
 
 		String image = null;
 		FoodGroupVO vo = new FoodGroupVO();
 		transferGroupFormToFoodGroupVO(groupForm, vo);
 		if (file != null) {
 			image = ShopUtil.saveImage(file);
 			if (!image.equals("")) {
 				vo.setImage(image);
 			}
 		}
 		int ret = foodAction.updateGroup(vo);
 
 		return ret > 0;
 	}
 
 	@RequestMapping(value = "/food/{id}", method = RequestMethod.POST)
 	public @ResponseBody
 	boolean updateFood(@RequestParam(required = false) MultipartFile file,
 			@ModelAttribute FoodForm foodForm,
 			@ModelAttribute("ajaxRequest") boolean ajaxRequest, Model model) {
 
 		String image = null;
 		FoodVO vo = new FoodVO();
 		vo.setId(foodForm.getId());
 		vo.setFoodName(foodForm.getFoodName());
 		vo.setDetail(foodForm.getDetail());
 		if (file != null) {
 			image = ShopUtil.saveImage(file);
 			if (!image.equals("")) {
 				vo.setImage(image);
 			}
 		}
 		int ret = foodAction.updateFood(vo);
 
 		return ret > 0;
 	}
 
 	/**
 	 * 更新订单状态
 	 * 
 	 * @return boolean true-更新成功; false-更新失败;
 	 */
 	@RequestMapping(value = "/order/{id}", method = RequestMethod.PUT, params = "status")
 	public @ResponseBody
 	boolean updateOrderStatus(@PathVariable int id, @RequestParam int status) {
 		return orderAction.updateOrderStatus(id, status);
 	}
 
 	/**
 	 * 标记Order为异常状态
 	 * 
 	 * @param id
 	 * @param exceptionDesc
 	 * @return
 	 * @throws UnsupportedEncodingException 
 	 */
 	@RequestMapping(value = "/order/{id}", method = RequestMethod.PUT, params = "exceptionDesc")
 	public @ResponseBody
 	boolean markOrderAsException(@PathVariable int id,
 			@RequestParam String exceptionDesc) throws UnsupportedEncodingException {
 		String expDesc = new String(exceptionDesc.getBytes("ISO-8859-1"), "UTF-8");
 		return orderAction.updateExceptionDetailAndAsException(id,
 				expDesc);
 	}
 
 	/**
 	 * 是否自动出单(更新配置项信息)
 	 * 
 	 * @param value
 	 *            true/false 表示开启 or 关闭.
 	 * @return true-开启成功; false-开启失败;
 	 */
 	@RequestMapping(value = "/autoprint/{value}", method = RequestMethod.PUT)
 	public @ResponseBody
 	boolean updateAutoPrint(@PathVariable String value) {
 		return orderAction.updateAutoPrint(value);
 	}
 
 	/**
 	 * 获取是否自动出单
 	 * 
 	 * @return boolean true-更新成功; false-更新失败;
 	 */
 	@RequestMapping("/autoprint")
 	public @ResponseBody
 	boolean getAutoPrint() {
 		return orderAction.getAutoPrint();
 	}
 
 	@RequestMapping(value = "/foodreshop", method = RequestMethod.POST)
 	public String saveFoodReShop(@Validated FoodReShopForm foodReShopForm,
 			BindingResult result) {
 		// if(result.hasErrors()) {
 		// print(result);
 		// }
 		int shopId = 1; //
 		FoodVO foodVO = new FoodVO();
 		BeanUtils.copyProperties(foodReShopForm, foodVO);
 		foodVO.setShopId(shopId);
 		String actionType = foodReShopForm.getActionType();
 		if ("add".equals(actionType)) {
 			foodAction.insertFoodReShop(foodVO);
 		} else {
 			foodAction.updateFoodReShop(foodVO);
 		}
 		return "redirect:/shop/catering";
 	}
 
 	/**
 	 * 配餐模式-删除; 删除一个食品关联
 	 * 
 	 * @param foodId
 	 *            foodId
 	 * @return true-删除成功; false-删除失败
 	 */
 	@RequestMapping(value = "/foodreshop/{id}", method = RequestMethod.DELETE)
 	public @ResponseBody
 	boolean deleteFoodReShop(@PathVariable(value = "id") int foodId) {
 		int ret = foodAction.deleteFoodReShop(foodId);
 		return ret > 0;
 	}
 
 	/**
 	 * 更新上架/下架状态
 	 * 
 	 * @param foodId
 	 *            食物/套餐ID
 	 * @param droped
 	 *            上架/下架状态
 	 * @return
 	 */
 	@RequestMapping(value = "/foodreshop/{id}", method = RequestMethod.PUT)
 	public @ResponseBody
 	boolean updateFoodReShopDroped(@PathVariable(value = "id") int foodId,
 			@RequestParam boolean droped) {
 		int ret = foodAction.updateFoodReShopDroped(foodId, droped);
 		return ret > 0;
 	}
 
 	@RequestMapping(value = "/package", method = RequestMethod.POST)
 	public String savePackage(
 			@RequestParam(required = false) MultipartFile file,
 			@Validated FoodReShopForm foodReShopForm, BindingResult result) {
 		int shopId = 1;
 		PackageVO packageVO = new PackageVO();
 		BeanUtils.copyProperties(foodReShopForm, packageVO);
 		packageVO.setShopId(shopId);
 		packageVO.setType(FoodVO.TYPE_PACKAGE);
 		if (file != null) {
 			String image = ShopUtil.saveImage(file);
 			if (!image.equals("")) {
 				packageVO.setImage(image);
 			}
 		}
 
 		// items
 		String strItemIds = foodReShopForm.getItemIds();
 		if (null != strItemIds && !strItemIds.trim().equals("")) {
 			String[] itemIds = strItemIds.split(",");
 			for (String itemId : itemIds) {
 				packageVO.addItem(itemId);
 			}
 		}
 
 		String actionType = foodReShopForm.getActionType();
 		if ("add".equals(actionType)) {
 			// save
 			foodAction.insertPackage(packageVO);
 		} else {
 			foodAction.updatePackage(packageVO);
 		}
 
 		return "redirect:/shop/package";
 	}
 
 	/**
 	 * 获取一个套餐
 	 * 
 	 * @param packageId
 	 * @return
 	 */
 	@RequestMapping(value = "/package/{id}", method = RequestMethod.GET)
 	public @ResponseBody
 	PackageVO getPackage(@PathVariable(value = "id") int packageId) {
 		PackageVO vo = foodAction.getPackage(packageId);
 		return vo;
 	}
 
 	/**
 	 * 套餐模式，删除一个套餐
 	 * 
 	 * @param packageId
 	 * @return
 	 */
 	@RequestMapping(value = "/package/{id}", method = RequestMethod.DELETE)
 	public @ResponseBody
 	boolean deletePackage(@PathVariable(value = "id") int packageId) {
 		boolean ret = foodAction.deletePackage(packageId);
 		return ret;
 	}
 
 	@RequestMapping(value = "/issue/{id}", method = RequestMethod.PUT)
 	public @ResponseBody
 	boolean issue(@PathVariable int id) {
 		return orderAction.issue(id);
 	}
 
 	@ExceptionHandler
 	public String handle(Exception e) {
 		// return e.getMessage();
 		e.printStackTrace();
 		logger.error("发生异常了", e);
 		return "error/500";
 	}
 
 	/**
 	 * 获取配餐所有食物列表(outer join, 空分组也会查出来)
 	 * 
 	 * @return List<GroupFoods>
 	 */
 	private List<GroupFoods> getCateringFoods() {
 		int shopId = 1;
 		FoodQueryCondition foodQueryCondition = new FoodQueryCondition(shopId,
 				FoodVO.TYPE_FOOD);
 		List<GroupFoods> lstGroupFoods = foodAction
 				.queryAllGroupAndFoods(foodQueryCondition);
 		return lstGroupFoods;
 	}
 
 	/**
 	 * 获取套餐所有食物列表
 	 * 
 	 * @return List<GroupFoods>
 	 */
 	private List<GroupPackages> getGroupPackages() {
 		int shopId = 1;
 		List<GroupPackages> groupPackages = foodAction
 				.queryAllGroupPackagesIncludeEmpty(shopId);
 		return groupPackages;
 	}
 
 	/**
 	 * 将GroupForm 转化为 FoodGroupVO
 	 * 
 	 * @param form
 	 *            GroupForm
 	 * @param vo
 	 *            FoodGroupVO
 	 */
 	private void transferGroupFormToFoodGroupVO(GroupForm form, FoodGroupVO vo) {
 		vo.setGroupName(form.getGroupName());
 		vo.setDetail(form.getDetail());
 		vo.setType(form.getType());
 		vo.setId(form.getId());
 		vo.setSort(form.getSort());
 	}
 
 	/**
 	 * save group to db.
 	 * 
 	 * @param file
 	 *            MultipartFile
 	 * @param groupForm
 	 *            GroupForm
 	 * @return FoodGroupVO
 	 * @throws IllegalStateException
 	 *             图片为空时，抛异常.
 	 */
 	private FoodGroupVO saveGroupToDB(MultipartFile file, GroupForm groupForm)
 			throws IllegalStateException {
 		// save the image file to upload directory
 		String imageName = "";
 		try {
 			imageName = ShopUtil.saveImage(file);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		if (imageName.equals("")) {
 			throw new IllegalStateException("add group fail!");
 		}
 
 		// save vo to db
 		FoodGroupVO vo = new FoodGroupVO();
 		transferGroupFormToFoodGroupVO(groupForm, vo);
 		vo.setImage(imageName);
 		int id = foodAction.insertFoodGroup(vo);
 		vo.setId(id);
 
 		return vo;
 	}
 
 	/**
 	 * 保存food 到 DB
 	 */
 	private FoodVO saveFoodToDB(MultipartFile file, FoodForm foodForm)
 			throws IllegalStateException {
 		// save the image file to upload directory
 		String imageName = "";
 		try {
 			imageName = ShopUtil.saveImage(file);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		if (imageName.equals("")) {
 			throw new IllegalStateException("add group fail!");
 		}
 
 		// save vo to db
 		FoodVO vo = new FoodVO();
 		vo.setFoodName(foodForm.getFoodName());
 		vo.setDetail(foodForm.getDetail());
 		vo.setType(FoodVO.TYPE_FOOD);
 		vo.setImage(imageName);
 		int id = foodAction.insertFood(vo);
 		vo.setId(id);
 
 		return vo;
 	}
 
 	@RequestMapping(value = "/phonestate", params = "p")
 	public @ResponseBody
 	int getPhoneState(@RequestParam(value = "p") String phone) {
 		return userAction.getPhoneState(phone);
 	}
 
 	@RequestMapping(value = "/phonestates", method = RequestMethod.POST)
 	public @ResponseBody
 	int[] getPhoneStates(String[] phones) {
 		return userAction.getPhoneStates(phones);
 	}
 
 	@RequestMapping(value = "/shopdata", method = RequestMethod.GET)
 	public @ResponseBody
 	String getShopData() {
 		UserVO user = userAction.getCurrentUser();
 
 		int currShopId = 1;
 		int todoCount = orderAction.getTodoCount(currShopId);
 
 		Map<String, Object> ret = new HashMap<String, Object>();
 		ret.put("user", user);
 		ret.put("todo", todoCount);
 
 		String json = JsonUtil.objectToJson(ret);
 		return json;
 	}
 	
 	@RequestMapping(value = "/dashboard", method = RequestMethod.GET)
 	public String toDashboard(Model model) {
 		Date[] todays = getTodayStartEndTime();
 		
 //		test
 //		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 //		Date[] todays = null;
 //		try {
 //			todays = new Date[] {
 //					sdf.parse("2013-08-05"), 
 //					sdf.parse("2013-08-06")
 //			};
 //		} catch (ParseException e) {
 //			e.printStackTrace();
 //		}
 		
 		List<QuantityPerFoodVO> quantities = statService.queryQuantityPerFood(1, todays[0], todays[1]);
 		
 		LinkedHashMap<GroupBean, List<FoodBean>> rets = new LinkedHashMap<GroupBean, List<FoodBean>>();
 		if(quantities.size() > 0) {
 			// 取出总数: 第一位
 			QuantityPerFoodVO quantityPerFood = quantities.get(0);
 			GroupBean group = new GroupBean(0, "合计");
 			List<FoodBean> foods = new ArrayList<FoodBean>();
 			FoodBean food = new FoodBean();
 			food.setId(0);
 			food.setFoodName("合计");
 			food.setType(0);
 			food.setImage("");
 			food.setAmount(quantityPerFood.getAmount());
 			foods.add(food);
 			rets.put(group, foods);
 			
 			for(int i=1, len=quantities.size(); i<len; i++) {
 				quantityPerFood = quantities.get(i);
 				group = new GroupBean(quantityPerFood.getGroupId(), quantityPerFood.getGroupName());
 				foods = rets.get(group);
 				if(foods == null) {
 					foods = new ArrayList<FoodBean>();
 					rets.put(group, foods);
 				}
 				
 				food = new FoodBean();
 				food.setId(quantityPerFood.getFoodId());
 				food.setFoodName(quantityPerFood.getFoodName());
 				food.setType(quantityPerFood.getFoodType());
 				food.setImage(quantityPerFood.getFoodImage());
 				food.setAmount(quantityPerFood.getAmount());
 				
 				foods.add(food);
 			}
 		}
 		
 		
 		model.addAttribute("quantities", rets);
 		return "shop/dashboard";
 	}
 
 	private Date[] getTodayStartEndTime() {
 		Date[] ret = new Date[2];
 		Calendar c = Calendar.getInstance();
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		c.set(Calendar.HOUR_OF_DAY, 0);
 		c.set(Calendar.MINUTE, 0);
 		c.set(Calendar.SECOND, 0);
 		ret[0] = c.getTime();
 
 		c.set(Calendar.HOUR_OF_DAY, 23);
 		c.set(Calendar.MINUTE, 59);
 		c.set(Calendar.SECOND, 59);
 		ret[1] = c.getTime();
 
 		return ret;
 	}
 
 	// /////////////////////// TEST ///////////////
 
 	@RequestMapping("/test")
 	public @ResponseBody
 	String test() {
 		return EnvironmentInfoVO.WEBROOT;
 	}
 
 	@RequestMapping("/testprint/{id}")
 	public @ResponseBody
 	String testprint(@PathVariable int id) {
 		OrderView orderView = orderAction.getOrderView(id);
 		PosService.print(orderView);
 		return "{}";
 	}
 
 	@RequestMapping("/websocket")
 	public @ResponseBody
 	String invokeWebSocket(@RequestParam String m) {
 		OrderWebSocketServlet.broadcast(m);
 		return "{}";
 	}
 
 	private <T> void print(T msg) {
 		if (msg != null)
 			System.out.println(">>>>>>>>>>>>>>>>>>>>\n" + msg.toString()
 					+ "\n<<<<<<<<<<<<<<<<<<");
 	}
 
 	/**
 	 * 店铺相关信息配置
 	 * 
 	 * @param model
 	 * @return
 	 */
 	@RequestMapping("/config")
 	public String config(Model model) {
 
 		ConfigVO openConfig = configService.getConfig("open-time");
 		ConfigVO closeConfig = configService.getConfig("closing-time");
 		ConfigVO sundayCloseConfig = configService.getConfig("sunday-close");
 
 		model.addAttribute("openTime",
 				openConfig == null ? null : openConfig.getValue());
 		model.addAttribute("closingTime", closeConfig == null ? null
 				: closeConfig.getValue());
 		
 		model.addAttribute(
 				"sundayClose",
				(sundayCloseConfig == null || "1".equals(sundayCloseConfig.getValue())) ? true
 						: false); // 默认星期天不营业
 		return "shop/config";
 	}
 
 }
