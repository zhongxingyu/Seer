 package com.xlthotel.core.admin.controller;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.json.JSONObject;
 import org.json.JSONWriter;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.google.common.collect.Lists;
 import com.xlthotel.core.admin.orm.entity.AdminUserInfo;
 import com.xlthotel.core.admin.service.HotelService;
 import com.xlthotel.core.admin.service.OrderRoomService;
 import com.xlthotel.core.admin.service.RoomService;
 import com.xlthotel.core.admin.service.RoomTypeExtensionService;
 import com.xlthotel.core.admin.service.RoomTypeService;
 import com.xlthotel.foundation.common.Condition;
 import com.xlthotel.foundation.common.Constant;
 import com.xlthotel.foundation.common.DateUtils;
 import com.xlthotel.foundation.common.Page;
 import com.xlthotel.foundation.common.PageOrder;
 import com.xlthotel.foundation.common.PageOrder.Sequence;
 import com.xlthotel.foundation.common.SimpleConditionImpl;
 import com.xlthotel.foundation.common.SimpleOrderImpl;
 import com.xlthotel.foundation.common.SimplePageImpl;
 import com.xlthotel.foundation.common.SimpleServletRequestUtils;
 import com.xlthotel.foundation.orm.entity.Hotel;
 import com.xlthotel.foundation.orm.entity.OrderRoom;
 import com.xlthotel.foundation.orm.entity.Room;
 import com.xlthotel.foundation.orm.entity.RoomType;
 import com.xlthotel.foundation.orm.entity.RoomTypeExtension;
 
 @Controller
 public class OrderRoomController {
 	@Autowired
 	private OrderRoomService orderRoomService;
 	
 	@Autowired
 	RoomTypeService roomTypeService;
 	
 	@Autowired
 	private HotelService hotelService;
 	
 	@Autowired
 	private RoomService roomService;
 	
 	@Autowired
 	private RoomTypeExtensionService roomTypeExtensionService;
 	
 	@RequestMapping(method=RequestMethod.GET, value="/servlet/admin/orderRoomList.do")
 	public ModelAndView getOrderRoomList(HttpServletRequest request) {
 		String permittedHotelId = getPermittedHotelId(request);
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		List<Hotel> hotelList = getDefaultHotelList(permittedHotelId);
 		List<RoomType> roomTypeList = getDefaultRoomTypeList();
 		
 		Page page = new SimplePageImpl();
 		page.setIndex(0);
 		page.setCount(10);
 		
 		PageOrder order = new SimpleOrderImpl();
 		order.setOrderColumn("orderDate");
 		order.setSequence(PageOrder.Sequence.DESC.toString());
 		List<OrderRoom> orderRoomList = getOrderRoomList(permittedHotelId, page, order);
 		
 		returnModel.put("orderRoomList", orderRoomList);
 		returnModel.put("hotelList", hotelList);
 		returnModel.put("roomTypeList", roomTypeList);
 		returnModel.put("page", page);
 		returnModel.put("order", order);
 		return new ModelAndView("/orderRoom/orderRoomList", returnModel);
 	}
 	
 	@RequestMapping(method=RequestMethod.POST, value="/servlet/admin/orderRoomList.do")
 	public ModelAndView getOrderRoomList(HttpServletRequest request, HttpServletResponse response) {
 		String permittedHotelId = getPermittedHotelId(request);
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		
 		String method = SimpleServletRequestUtils.getStringParameter(request, "postMethod", "");
 		if (method.equals("deleteOrderRoom")) {
 			List<String> orderRoomIds = getSelectedOrderRoom(request);
 			orderRoomService.deleteOrderRoom(orderRoomIds);
 		}
 		
 		List<Hotel> hotelList = getDefaultHotelList(permittedHotelId);
 		List<RoomType> roomTypeList = getDefaultRoomTypeList();
 		
 		Page page = getPageFromRequest(request);
 		PageOrder order = getOrderFromRequest(request);
 		
 		String hotelId = SimpleServletRequestUtils.getStringParameter(request, "searchHotel", "");
 		if (!permittedHotelId.equals("0") && !permittedHotelId.equals(hotelId)) {
 			hotelId = permittedHotelId;
 		}
 		String roomTypeId = SimpleServletRequestUtils.getStringParameter(request, "searchRoomType", "");
 		int selectedSource = SimpleServletRequestUtils.getIntParameter(request, "searchSource", -1);
 
 		Condition condition = new SimpleConditionImpl();
 		condition.setEntityName("OrderRoom");
 		
 		String minDateStr = SimpleServletRequestUtils.getStringParameter(request, "minDate", "");
 		String maxDateStr = SimpleServletRequestUtils.getStringParameter(request, "maxDate", "");
 		Date minDate;
 		Date maxDate;
 		if (StringUtils.isNotBlank(minDateStr) && StringUtils.isNotBlank(maxDateStr)) {
 			minDate = DateUtils.parseOrderDate(minDateStr);
 			maxDate = DateUtils.parseOrderDate(maxDateStr);
 			condition.putCondition("orderDate", "between", "date", minDate, maxDate);
 		} else if (StringUtils.isNotBlank(minDateStr) && StringUtils.isBlank(maxDateStr)) {
 			minDate = DateUtils.parse("yyyy-MM-dd", minDateStr);
 			maxDate = DateUtils.parse("yyyy-MM-dd", DateUtils.getNowDate());
 			condition.putCondition("orderDate", "between", "date", minDate, maxDate);
 		} else if (StringUtils.isBlank(minDateStr) && StringUtils.isNotBlank(maxDateStr)) {
 			minDate = DateUtils.parse("yyyy-MM-dd", DateUtils.getDateOne());
 			maxDate = DateUtils.parse("yyyy-MM-dd", maxDateStr);
 			condition.putCondition("orderDate", "between", "date", minDate, maxDate);
 		}
 		
 		if (StringUtils.isNotBlank(hotelId)) {
 			condition.putCondition("room.hotel.id", "=", "hotelId", hotelId);
 		}
 		
 		if (StringUtils.isNotBlank(roomTypeId)) {
 			condition.putCondition("room.roomType.id", "=", "roomTypeId", roomTypeId);
 		}
 		
 		if (selectedSource >= OrderRoom.Source.Web.getValue()
 				&& selectedSource <= OrderRoom.Source.Real.getValue()) {
 			condition.putCondition("source", "=", "selectedSource",
 					selectedSource);
 		}
 		
 		List<OrderRoom> orderRoomList = orderRoomService.getOrderRoomList(page, condition, order);
 		
 		returnModel.put("orderRoomList", orderRoomList);
 		returnModel.put("hotelList", hotelList);
 		returnModel.put("roomTypeList", roomTypeList);
 		returnModel.put("page", page);
 		returnModel.put("order", order);
 		returnModel.put("minDate", minDateStr);
 		returnModel.put("maxDate", maxDateStr);
 		returnModel.put("selectedSource", selectedSource);
 		returnModel.put("selectedHotelId", hotelId);
 		returnModel.put("selectedRoomTypeId", roomTypeId);
 		return new ModelAndView("/orderRoom/orderRoomList", returnModel);
 	}
 	
 	@RequestMapping(method=RequestMethod.GET, value="/servlet/admin/createOrderRoom.do")
 	public ModelAndView createOrderRoom(HttpServletRequest request) {
 		String permittedHotelId = getPermittedHotelId(request);
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		OrderRoom orderRoom = new OrderRoom();
 		List<Hotel> hotelList = getDefaultHotelList(permittedHotelId);
 		List<RoomType> roomTypeList = getDefaultRoomTypeList();
 		returnModel.put("hotelList", hotelList);
 		returnModel.put("roomTypeList", roomTypeList);
 		returnModel.put("orderRoom", orderRoom);
 		return new ModelAndView("/orderRoom/orderRoomDetail", returnModel);
 	}
 	
 	@RequestMapping(method=RequestMethod.POST, value="/servlet/admin/saveOrderRoom.do")
 	public ModelAndView saveOrderRoom(HttpServletRequest request, HttpServletResponse response) throws IOException {
 		String permittedHotelId = getPermittedHotelId(request);
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		String hotelId = SimpleServletRequestUtils.getStringParameter(request, "selectedHotel", "");
 		String hotelStatus = SimpleServletRequestUtils.getStringParameter(request, "selectHotelStatusOccupied", "");
 		if (StringUtils.isNotBlank(hotelStatus)) {
 			if (hotelStatus.equals("true")) {
 				hotelService.updateHotelStatus(Lists.newArrayList(hotelId), 2);
 			} else {
 				hotelService.updateHotelStatus(Lists.newArrayList(hotelId), 1);
 			}
 			response.sendRedirect("orderRoomList.do");
 			return null;
 		}
 		String minDateStr = SimpleServletRequestUtils.getStringParameter(request, "minDate", "");
 		String maxDateStr = SimpleServletRequestUtils.getStringParameter(request, "maxDate", "");
 		String roomId = SimpleServletRequestUtils.getStringParameter(request, "selectedRoom", "");
 		Room room = roomService.findRoomById(roomId);
 		int source = SimpleServletRequestUtils.getIntParameter(request, "selectSource", -1);
 		List<String> errors = checkOrderRoom(room, minDateStr, maxDateStr, source);
 		if (errors.size() > 0) {
 			hotelId = SimpleServletRequestUtils.getStringParameter(request, "selectedHotel", "");
 			String roomTypeId = SimpleServletRequestUtils.getStringParameter(request, "selectedRoomType", "");
 			List<Room> roomList = retrieveRoomByHotelAndRoomType(hotelId, roomTypeId);
 			List<Hotel> hotelList = getDefaultHotelList(permittedHotelId);
 			List<RoomType> roomTypeList = getDefaultRoomTypeList();
 			returnModel.put("errors", errors);
 			returnModel.put("room", room);
 			returnModel.put("roomList", roomList);
 			returnModel.put("hotelList", hotelList);
 			returnModel.put("roomTypeList", roomTypeList);
 			returnModel.put("minDate", minDateStr);
 			returnModel.put("maxDate", maxDateStr);
 			return new ModelAndView("/orderRoom/orderRoomDetail", returnModel);
 		}
 		Date from = DateUtils.parseOrderDate(minDateStr);
 		Date to = DateUtils.parseOrderDate(maxDateStr);
 		List<Date> dateList = DateUtils.splitDate(from, to);
 		orderRoomService.save(room, dateList, source, null);
 		response.sendRedirect("orderRoomList.do");
 		return null;
 	}
 	
 	@RequestMapping(method = RequestMethod.GET, value = "/servlet/admin/orderRoom/getRoom.do")
 	public void getRoom(HttpServletRequest request,
 			HttpServletResponse response,
 			@RequestParam(value = "hotelId") String hotelId,
 			@RequestParam(value = "roomTypeId") String roomTypeId) {
 		try {
 			response.setContentType("text/plain");
 			response.setCharacterEncoding("UTF-8");
 			List<Room> roomList = retrieveRoomByHotelAndRoomType(hotelId,
 					roomTypeId);
 			Writer writer = response.getWriter();
 			JSONWriter jsonWriter = new JSONWriter(writer).array();
 			for (Room room : roomList) {
 				Map<String, Object> map = new HashMap<String, Object>();
 				map.put("id", room.getId());
 				map.put("number", room.getNumber());
 				jsonWriter.value(new JSONObject(map));
 			}
 			jsonWriter.endArray();
 			writer.flush();
 			writer.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	//房间类型关闭页面初始化
 	@RequestMapping(method=RequestMethod.GET, value="/servlet/admin/isHotelTypeOrder.do")
 	public ModelAndView isHotelOrderTypeList(HttpServletRequest request, HttpServletResponse response) {
 		String permittedHotelId = getPermittedHotelId(request);
 		Page page = new SimplePageImpl();
 		page.setIndex(0);
 		page.setCount(10);
 		
 		PageOrder order = new SimpleOrderImpl();
 		order.setOrderColumn("hotel.name");
 		order.setSequence(PageOrder.Sequence.ASC.toString());
 		List<Hotel> hotelList = getDefaultHotelList(permittedHotelId);
 		
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		returnModel.put("page", page);
 		returnModel.put("order", order);
 		returnModel.put("hotelList", hotelList);
 		return new ModelAndView("/orderRoom/isHotelTypeOrder", returnModel);
 	}
 	//房间类型关闭页面查询
 	@RequestMapping(method=RequestMethod.POST, value="/servlet/admin/isHotelTypeOrderQuery.do")
 	public ModelAndView getRoomTypeExtList(HttpServletRequest request, HttpServletResponse response) {
 		Page page = getPageFromRequest(request);
 		PageOrder order = getOrderFromRequest(request);
 		String permittedHotelId = getPermittedHotelId(request);
 		String postMethod = SimpleServletRequestUtils.getStringParameter(request, "postMethod", "");
 		if (postMethod.equals("updateIsOrder")) {
 			int status = SimpleServletRequestUtils.getIntParameter(request, "isOrderStatus", -1);
 			String  isOrderId = SimpleServletRequestUtils.getStringParameter(request, "isOrderId", "");
 			roomTypeExtensionService.updateRoomTypeExtIsOrder(isOrderId, status);
 		}
 		
 		String hotelId = SimpleServletRequestUtils.getStringParameter(request, "searchHotel", "");
 		String roomTypeId = SimpleServletRequestUtils.getStringParameter(request, "searchRoomType", "");
 		
 		Condition condition = new SimpleConditionImpl();
 		condition.setEntityName("RoomTypeExtension");
 		
 		if (StringUtils.isNotBlank(hotelId)) {
 			condition.putCondition("hotel.id", "=", "hotelId", hotelId);
 		}
 		
 		if (StringUtils.isNotBlank(roomTypeId)) {
 			condition.putCondition("roomType.id", "=", "roomTypeId", roomTypeId);
 		}
 		
 		List<RoomTypeExtension> roomTypeExtList = roomTypeExtensionService.getRoomTypeExtensionsList(page, condition, order);
 		
 		List<Hotel> hotelList = getDefaultHotelList(permittedHotelId);
 		
 		List<RoomType> roomTypeList = roomTypeService.getRoomTypeListForRoomsInHotel(hotelId);
 		
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		returnModel.put("page", page);
 		returnModel.put("order", order);
 		returnModel.put("roomTypeExtList", roomTypeExtList);
 		returnModel.put("selectedHotelId", hotelId);
 		returnModel.put("selectedRoomTypeId", roomTypeId);
 		returnModel.put("hotelList", hotelList);
 		returnModel.put("roomTypeList", roomTypeList);
 		return new ModelAndView("/orderRoom/isHotelTypeOrder", returnModel);
 	}
 
 	private List<Room> retrieveRoomByHotelAndRoomType(String hotelId,
 			String roomTypeId) {
 		Condition condition = new SimpleConditionImpl();
 		condition.setEntityName("Room");
 		condition.putCondition("hotel.id", "=", "hotelId", hotelId);
 		condition.putCondition("roomType.id", "=", "roomTypeId", roomTypeId);
 		PageOrder order = new SimpleOrderImpl();
 		order.setOrderColumn("number");
 		order.setSequence(Sequence.ASC.toString());
 		List<Room> roomList = roomService.getRoomList(null, condition, order);
 		return roomList;
 	}
 	
 	private List<String> checkOrderRoom(Room room, String minDateStr, String maxDateStr, int source) {
 		List<String> errors = new ArrayList<String>();
 		if (room == null) {
 			errors.add("无效的房间");
 		} else {
 			Date minDate = DateUtils.parseOrderDate(minDateStr);
 			Date maxDate = DateUtils.parseOrderDate(maxDateStr);
 			Date today = DateUtils.parseOrderDate(DateUtils.getNowDate());
 			if (minDate == null || maxDate == null) {
 				errors.add("预定时间格式不正确");
 			} else {
 				if (minDate.compareTo(maxDate) > 0) {
 					errors.add("预定开始时间应小于或者等于结束时间");
 				} else if (minDate.compareTo(today) < 0) {
 					errors.add("预定开始时间应从今天开始");
 				} else {
 					List<OrderRoom> orderedRoomList = orderRoomService.checkOrdered(room, minDate, maxDate);
 					if (orderedRoomList != null && orderedRoomList.size() > 0) {
 						for (OrderRoom orderedRoom : orderedRoomList) {
 							errors.add(DateUtils.formatDate("yyyy-MM-dd", orderedRoom.getOrderDate())
 									+ "的"
 									+ orderedRoom.getRoom().getHotel()
 											.getName()
 									+ "-"
 									+ orderedRoom.getRoom().getRoomType()
 											.getName() + "-"
 									+ orderedRoom.getRoom().getNumber()
 									+ "已被预订");
 						}
 					}
 				}
 			}
 		}
 		if (source < OrderRoom.Source.Web.getValue() || source > OrderRoom.Source.Real.getValue()) {
 			errors.add("预定来源不正确");
 		}
 		return errors;
 	}
 	
 	private List<String> getSelectedOrderRoom(HttpServletRequest request) {
 		return Lists.newArrayList(SimpleServletRequestUtils.getStringParameters(request, "selectOrderRoom"));
 	}
 	
 	private List<Hotel> getDefaultHotelList(String permittedHotelId) {
 		PageOrder hotelListOrder = new SimpleOrderImpl();
 		hotelListOrder.setOrderColumn("name");
 		hotelListOrder.setSequence(PageOrder.Sequence.ASC.toString());
 		Condition cond = null;
 		if (StringUtils.isNotBlank(permittedHotelId) && !permittedHotelId.equals("0")) {
 			cond = new SimpleConditionImpl();
 			cond.setEntityName("Hotel");
 			cond.putCondition("id", "=", "hotelId", permittedHotelId);
 		}
 		List<Hotel> hotelList = hotelService.getHotelList(null, cond, hotelListOrder);
 		return hotelList;
 	}
 	
 	private List<RoomType> getDefaultRoomTypeList() {
 		PageOrder roomTypeListOrder = new SimpleOrderImpl();
 		roomTypeListOrder.setOrderColumn("name");
 		roomTypeListOrder.setSequence(PageOrder.Sequence.ASC.toString());
		List<RoomType> roomTypeList = roomTypeService.getRoomTypeList(null, null, roomTypeListOrder);
 		return roomTypeList;
 	}
 	
 	private Page getPageFromRequest(HttpServletRequest request) {
 		Page page = new SimplePageImpl();
 		page.setIndex(SimpleServletRequestUtils.getIntParameter(request, "pageIndex", 0));
 		page.setCount(SimpleServletRequestUtils.getIntParameter(request, "pageCount", 10));
 		return page;
 	}
 	
 	private PageOrder getOrderFromRequest(HttpServletRequest request) {
 		PageOrder order = new SimpleOrderImpl();
 		order.setOrderColumn(SimpleServletRequestUtils.getStringParameter(request, "orderColumn", "hotel.name"));
 		order.setSequence(SimpleServletRequestUtils.getStringParameter(request, "orderSequence", "desc"));
 		return order;
 	}
 	
 	private String getPermittedHotelId(HttpServletRequest request) {
 		Object obj = request.getSession().getAttribute(Constant.USER_SESSION_KEY);
 		if (obj instanceof AdminUserInfo) {
 			AdminUserInfo adminuser = (AdminUserInfo) obj;
 			return adminuser.getHotelId();
 		}
 		return "";
 	}
 	
 	private List<OrderRoom> getOrderRoomList(String permittedHotelId, Page page, PageOrder pageOrder) {
 		List<OrderRoom> orderRoomList = new ArrayList<OrderRoom>();
 		Condition cond = null;
 		if (StringUtils.isNotBlank(permittedHotelId) && !permittedHotelId.equals("0")) {
 			cond = new SimpleConditionImpl();
 			cond.setEntityName("OrderRoom");
 			cond.putCondition("room.hotel.id", "=", "hotelId", permittedHotelId);
 		}
 		orderRoomList = orderRoomService.getOrderRoomList(page, cond, pageOrder);
 		return orderRoomList;
 	}
 }
