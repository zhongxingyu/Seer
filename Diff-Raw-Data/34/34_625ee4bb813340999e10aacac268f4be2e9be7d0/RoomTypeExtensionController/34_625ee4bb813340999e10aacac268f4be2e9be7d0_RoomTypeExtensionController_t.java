 package com.xlthotel.core.admin.controller;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.ckeditor.CKEditorConfig;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.xlthotel.core.admin.service.HotelFacilityService;
 import com.xlthotel.core.admin.service.HotelService;
 import com.xlthotel.core.admin.service.PhotoService;
 import com.xlthotel.core.admin.service.RoomTypeExtensionService;
 import com.xlthotel.core.admin.service.RoomTypeService;
 import com.xlthotel.foundation.ckeditor.CKEditorConfigFactory;
 import com.xlthotel.foundation.common.Condition;
 import com.xlthotel.foundation.common.Page;
 import com.xlthotel.foundation.common.PageOrder;
 import com.xlthotel.foundation.common.SimpleConditionImpl;
 import com.xlthotel.foundation.common.SimpleOrderImpl;
 import com.xlthotel.foundation.common.SimplePageImpl;
 import com.xlthotel.foundation.common.SimpleServletRequestUtils;
 import com.xlthotel.foundation.common.SystemPropertyConfig;
 import com.xlthotel.foundation.orm.entity.Hotel;
 import com.xlthotel.foundation.orm.entity.HotelFacility;
 import com.xlthotel.foundation.orm.entity.MediaItem;
 import com.xlthotel.foundation.orm.entity.RoomType;
 import com.xlthotel.foundation.orm.entity.RoomTypeExtension;
 
 @Controller
 public class RoomTypeExtensionController {
 	
 	@Autowired
 	private RoomTypeExtensionService roomTypeExtensionService;
 	
 	@Autowired
 	private HotelService hotelService;
 	
 	@Autowired
 	private RoomTypeService roomTypeService;
 	
 	@Autowired
 	private HotelFacilityService hotelFacilityService;
 	
 	@Autowired
 	private PhotoService photoService;
 	
 	@Autowired
 	private SystemPropertyConfig systemPropertyConfig;
 	
 	@RequestMapping(method=RequestMethod.GET, value="/servlet/admin/roomTypeExtList.do")
 	public ModelAndView getRoomTypeExtList() {
 		Page page = new SimplePageImpl();
 		page.setIndex(0);
 		page.setCount(10);
 		
 		PageOrder order = new SimpleOrderImpl();
 		order.setOrderColumn("hotel.name");
 		order.setSequence(PageOrder.Sequence.ASC.toString());
 		List<RoomTypeExtension> roomTypeExtList = roomTypeExtensionService.getRoomTypeExtensionsList(page, null, order);
 		
 		List<Hotel> hotelList = getDefaultHotelList();
 		
 		List<RoomType> roomTypeList = getDefaultRoomTypeList();
 		
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		returnModel.put("page", page);
 		returnModel.put("order", order);
 		returnModel.put("hotelList", hotelList);
 		returnModel.put("roomTypeList", roomTypeList);
 		returnModel.put("roomTypeExtList", roomTypeExtList);
 		
 		return new ModelAndView("/roomTypeExt/roomTypeExtList", returnModel);
 	}
 	
 	@RequestMapping(method=RequestMethod.POST, value="/servlet/admin/roomTypeExtList.do")
 	public ModelAndView getRoomTypeExtList(HttpServletRequest request, HttpServletResponse response) {
 		Page page = getPageFromRequest(request);
 		PageOrder order = getOrderFromRequest(request);
 		
 		String postMethod = SimpleServletRequestUtils.getStringParameter(request, "postMethod", "");
 		if (postMethod.equals("updateRoomStatus")) {
 			List<String> selectedRoomTypeExt = getSelectedRoomTypeExt(request);
 			int status = SimpleServletRequestUtils.getIntParameter(request, "statusToUpdate", -1);
 			if (status >= RoomTypeExtension.Status.Disable.getValue() || status <= RoomTypeExtension.Status.Enable.getValue()) {
 				roomTypeExtensionService.updateRoomTypeExtStatus(selectedRoomTypeExt, status);
 			}
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
 		
 		List<Hotel> hotelList = getDefaultHotelList();
 		
 		List<RoomType> roomTypeList = getDefaultRoomTypeList();
 		
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		returnModel.put("page", page);
 		returnModel.put("order", order);
 		returnModel.put("roomTypeExtList", roomTypeExtList);
 		returnModel.put("selectedHotelId", hotelId);
 		returnModel.put("selectedRoomTypeId", roomTypeId);
 		returnModel.put("hotelList", hotelList);
 		returnModel.put("roomTypeList", roomTypeList);
 		return new ModelAndView("/roomTypeExt/roomTypeExtList", returnModel);
 	}
 	
 	@RequestMapping(method=RequestMethod.GET, value="/servlet/admin/createRoomTypeExt.do")
 	public ModelAndView createRoomTypeExtension() {
 		RoomTypeExtension roomTypeExtension = new RoomTypeExtension();
 		roomTypeExtension.setIsOrder(0);
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		List<Hotel> hotelList = getDefaultHotelList();
 		List<RoomType> roomTypeList = getDefaultRoomTypeList();
 		setupCKEditor(returnModel);
 		createFacilityModelView(returnModel);
 		returnModel.put("hotelList", hotelList);
 		returnModel.put("roomTypeList", roomTypeList);
 		returnModel.put("roomTypeExtOperationMethod", "save");
 		returnModel.put("roomTypeExt", roomTypeExtension);
 		return new ModelAndView("/roomTypeExt/roomTypeExtDetail", returnModel);
 	}
 	
 	@RequestMapping(method=RequestMethod.GET, value="/servlet/admin/updateRoomTypeExt.do")
 	public ModelAndView updateRoomTypeExtension(@RequestParam(value="id") String id) {
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		RoomTypeExtension roomTypeExtension = roomTypeExtensionService.find(id);
 		List<Hotel> hotelList = getDefaultHotelList();
 		List<RoomType> roomTypeList = getDefaultRoomTypeList();
 		createPhotosModelView(roomTypeExtension, returnModel);
 		createFacilityModelView(roomTypeExtension.convertFacilityToIdList(), returnModel);
 		setupCKEditor(returnModel);
 		returnModel.put("hotelList", hotelList);
 		returnModel.put("roomTypeList", roomTypeList);
 		returnModel.put("roomTypeExtOperationMethod", "update");
 		returnModel.put("roomTypeExt", roomTypeExtension);
 		return new ModelAndView("/roomTypeExt/roomTypeExtDetail", returnModel);
 	}
 	
 	@RequestMapping(method=RequestMethod.POST, value="/servlet/admin/saveRoomTypeExt.do")
 	public ModelAndView saveRoomTypeExtension(HttpServletRequest request,
 			HttpServletResponse response,
 			@RequestParam(value = "roomTypeExtOperationMethod") String method) throws IOException {
 		RoomTypeExtension roomTypeExtension = bindRoomTypeExt(request, method);
 		Map<String, String> photoTitles = bindPhotoTitles(request);
 		List<String> selectedHotelFacilities = getSelectedHotelFacilities(request);
 		List<String> errors = checkRoomTypeExtension(roomTypeExtension);
 		checkPhotoTitles(photoTitles, errors);
 		if (errors.size() > 0) {
 			Map<String, Object> returnModel = new HashMap<String, Object>();
 			createPhotosModelView(request, returnModel, photoTitles);
 			setDeleteUrlBackToModelView(request, returnModel);
 			createFacilityModelView(selectedHotelFacilities, returnModel);
 			List<Hotel> hotelList = getDefaultHotelList();
 			List<RoomType> roomTypeList = getDefaultRoomTypeList();
 			setupCKEditor(returnModel);
 			returnModel.put("hotelList", hotelList);
 			returnModel.put("roomTypeList", roomTypeList);
 			returnModel.put("roomTypeExt", roomTypeExtension);
 			returnModel.put("roomTypeExtOperationMethod", method);
 			returnModel.put("errors", errors);
 			return new ModelAndView("/roomTypeExt/roomTypeExtDetail", returnModel);
 		}
 		
 		String photoIdString = mergeAndDeletePhotos(roomTypeExtension.getId(), request, photoTitles);
 		roomTypeExtension.setPhotos(photoIdString);
 		if (method.equals("save")) {
 			roomTypeExtensionService.save(roomTypeExtension, selectedHotelFacilities);
 		} else {
 			roomTypeExtensionService.update(roomTypeExtension, selectedHotelFacilities);
 		}
 		response.sendRedirect("roomTypeExtList.do");
 		return null;
 	}
 	
 	private List<String> checkRoomTypeExtension(RoomTypeExtension roomTypeExtension) {
 		List<String> errors = new ArrayList<String>();
 		if (roomTypeExtension.getHotel() == null || roomTypeExtension.getHotel().getId() == null) {
 			errors.add("请选择酒店");
 		} else {
 			Hotel hotel = hotelService.find(roomTypeExtension.getHotel().getId());
 			if (hotel == null) {
 				errors.add("请选择酒店");
 			} else {
 				roomTypeExtension.setHotel(hotel);
 			}
 		}
 		
 		if (roomTypeExtension.getRoomType() == null || roomTypeExtension.getRoomType().getId() == null) {
 			errors.add("请选择房型");
 		} else {
 			RoomType roomType = roomTypeService.find(roomTypeExtension.getRoomType().getId());
 			if (roomType == null) {
 				errors.add("请选请选择房型酒店");
 			} else {
 				roomTypeExtension.setRoomType(roomType);
 			}
 		}
 		
 		if (roomTypeExtension.getStatus() < RoomTypeExtension.Status.Disable.getValue() || 
 				roomTypeExtension.getStatus() > RoomTypeExtension.Status.Enable.getValue()) {
 			errors.add("房间状态格式不正确");
 		}
 		
 		BigDecimal value1 = new BigDecimal(1);
 		
 		if (value1.compareTo(roomTypeExtension.getListingPrice()) == 1) {
 			errors.add("挂牌价格式不正确");
 		}
 		
 		if (value1.compareTo(roomTypeExtension.getExcutingPrice()) == 1) {
 			errors.add("执行价格式不正确");
 		}
 		
 		if (value1.compareTo(roomTypeExtension.getOnlinePrice()) == 1) {
 			errors.add("网络价格式不正确");
 		}
 		
 		if (value1.compareTo(roomTypeExtension.getMemberPrice()) == 1) {
 			errors.add("会员价格式不正确");
 		}
 		
 		if (value1.compareTo(roomTypeExtension.getGoldenMemberPrice()) == 1) {
 			errors.add("VIP价格式不正确");
 		}
 		
 		if (value1.compareTo(roomTypeExtension.getCurrentPrice()) == 1) {
 			errors.add("现行价格式不正确");
 		}
 		
 		return errors;
 	}
 	
 	private RoomTypeExtension bindRoomTypeExt(HttpServletRequest request, String method) {
 		RoomTypeExtension roomTypeExtension = new RoomTypeExtension();
 		roomTypeExtension.setListingPrice(BigDecimal.valueOf(SimpleServletRequestUtils.getDoubleParameter(request, "listingPrice", -1)));
 		roomTypeExtension.setExcutingPrice(BigDecimal.valueOf(SimpleServletRequestUtils.getDoubleParameter(request, "excutingPrice", -1)));
 		roomTypeExtension.setOnlinePrice(BigDecimal.valueOf(SimpleServletRequestUtils.getDoubleParameter(request, "onlinePrice", -1)));
 		roomTypeExtension.setMemberPrice(BigDecimal.valueOf(SimpleServletRequestUtils.getDoubleParameter(request, "memberPrice", -1)));
 		roomTypeExtension.setGoldenMemberPrice(BigDecimal.valueOf(SimpleServletRequestUtils.getDoubleParameter(request, "goldenMemberPrice", -1)));
 		roomTypeExtension.setCurrentPrice(BigDecimal.valueOf(SimpleServletRequestUtils.getDoubleParameter(request, "currentPrice", -1)));
 		Hotel hotel = new Hotel();
 		hotel.setId(SimpleServletRequestUtils.getStringParameter(request, "selectedHotel", ""));
 		roomTypeExtension.setHotel(hotel);
 		RoomType roomType = new RoomType();
 		roomType.setId(SimpleServletRequestUtils.getStringParameter(request, "selectedRoomType", ""));
 		roomTypeExtension.setRoomType(roomType);
 		roomTypeExtension.setInfo(SimpleServletRequestUtils.getStringParameter(request, "roomTypeExtInfo", ""));
 		roomTypeExtension.setStatus(SimpleServletRequestUtils.getIntParameter(request, "selectStatus", -1));
 		roomTypeExtension.setIsOrder(SimpleServletRequestUtils.getIntParameter(request, "isOrder", 0));
 		if (method.equals("update")) {
 			roomTypeExtension.setId(SimpleServletRequestUtils.getStringParameter(request, "id", ""));
 		}
 		return roomTypeExtension;
 	}
 	
 	private List<RoomType> getDefaultRoomTypeList() {
 		PageOrder roomTypeListOrder = new SimpleOrderImpl();
 		roomTypeListOrder.setOrderColumn("name");
 		roomTypeListOrder.setSequence(PageOrder.Sequence.ASC.toString());
		Condition condition = new SimpleConditionImpl();
		condition.setEntityName("RoomType");
		condition.putCondition("status", "=", "status", RoomType.Status.Enable.getValue());
		List<RoomType> roomTypeList = roomTypeService.getRoomTypeList(null, condition, roomTypeListOrder);
 		return roomTypeList;
 	}
 
 	private List<Hotel> getDefaultHotelList() {
 		PageOrder hotelListOrder = new SimpleOrderImpl();
 		hotelListOrder.setOrderColumn("name");
 		hotelListOrder.setSequence(PageOrder.Sequence.ASC.toString());
 		List<Hotel> hotelList = hotelService.getHotelList(null, null, hotelListOrder);
 		return hotelList;
 	}
 	
 	private List<String> getSelectedRoomTypeExt(HttpServletRequest request) {
 		return Lists.newArrayList(SimpleServletRequestUtils.getStringParameters(request, "selectRoomTypeExt"));
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
 	
 	private List<String> getSelectedHotelFacilities(HttpServletRequest request) {
 		return Lists.newArrayList(SimpleServletRequestUtils.getStringParameters(request, "selectHotelFacility"));
 	}
 	
 	private List<String> getPhotoIds(HttpServletRequest request) {
 		List<String> newPhotoIds = new ArrayList<String>();
 		for (int i = 1; i < 7; i++) {
 			String viewName = "roomTypeExtPhotoId" + i;
 			String id = SimpleServletRequestUtils.getStringParameter(request, viewName, "");
 			if (StringUtils.isNotBlank(id)) {
 				newPhotoIds.add(id);
 			}
 		}
 		return newPhotoIds;
 	}
 	
 	private String mergeAndDeletePhotos(String roomTypeExtId, HttpServletRequest request, Map<String, String> photoTitles) {
 		List<String> newIds = getPhotoIds(request);
 		if (roomTypeExtId != null) {
 			RoomTypeExtension oldRoomTypeExt = roomTypeExtensionService.find(roomTypeExtId);
 			List<String> oldIds = oldRoomTypeExt.convertPhotosToList();
 			for (String id : oldIds) {
 				if (!newIds.contains(id)) {
 					photoService.deleteMediaItem(id);
 				}
 			}
 		}
 		for (String id: newIds) {
 			String text = photoTitles.get(id);
 			MediaItem mediaItem = photoService.getMediaItem(id);
 			mediaItem.setText(text);
 			photoService.updateMediaItem(mediaItem);
 		}
 		return StringUtils.join(newIds, ",");
 	}
 	
 	private void setDeleteUrlBackToModelView(HttpServletRequest request, Map<String, Object> returnModel) {
 		for (int i = 1; i < 7; i++) {
 			String viewName = "photoDeleteUrl" + i;
 			String url = SimpleServletRequestUtils.getStringParameter(request, viewName, "");
 			if (url.contains("@donotremove@")) {
 				url = url.replaceFirst("@donotremove@", "");
 			}
 			returnModel.put(viewName, url);
 		}
 	}
 	
 	private Map<String, String> bindPhotoTitles(HttpServletRequest request) {
 		Map<String, String> result = Maps.newHashMap();
 		for (int i = 1; i < 7; i++) {
 			String titleHTMLId = "photoText" + i;
 			String PhotoHTMLId = "roomTypeExtPhotoId" + i;
 			String title = SimpleServletRequestUtils.getStringParameter(request, titleHTMLId, "");
 			String photoId = SimpleServletRequestUtils.getStringParameter(request, PhotoHTMLId, "");
 			if (StringUtils.isNotBlank(photoId)) {
 				result.put(photoId, title);
 			}
 		}
 		return result;
 	}
 	
 	private void checkPhotoTitles(Map<String, String> photoTitles, List<String> errors) {
 		for (String title : photoTitles.values()) {
 			if (title.length() > 20) {
 				errors.add("图片标题长度不能超过20个字");
 				return;
 			}
 		}
 	}
 	
 	private void createPhotosModelView(HttpServletRequest request,
 			Map<String, Object> modelView, Map<String, String> photoTitles) {
 		List<MediaItem> photos = new ArrayList<MediaItem>(6);
 		for (int i = 1; i < 7; i++) {
 			String mediaItemId = SimpleServletRequestUtils.getStringParameter(
 					request, "roomTypeExtPhotoId" + i, "");
 			MediaItem mediaItem = null;
 			if (StringUtils.isNotBlank(mediaItemId)) {
 				mediaItem = photoService.getMediaItemModel(mediaItemId);
 				mediaItem.setText(photoTitles.get(mediaItemId));
 			}
 			photos.add(mediaItem);
 		}
 		modelView.put("photos", photos);
 	}
 	
 	private void createPhotosModelView(RoomTypeExtension roomTypeExt, Map<String, Object> modelView) {
 		String photoRawString = roomTypeExt.getPhotos();
 		if (StringUtils.isBlank(photoRawString)) {
 			return;
 		}
 		String[] photoIds = photoRawString.split(",");
 		List<MediaItem> photos = new ArrayList<MediaItem>();
 		for (int i = 0; i < Math.min(photoIds.length, 6); i++) {
 			MediaItem item = photoService.getMediaItemModel(photoIds[i]);
 			photos.add(item);
 		}
 		modelView.put("photos", photos);
 	}
 	
 	private void createFacilityModelView(Map<String, Object> model) {
 		PageOrder order = new SimpleOrderImpl();
 		order.setOrderColumn("name");
 		order.setSequence(PageOrder.Sequence.ASC.toString());
 		List<HotelFacility> hotelFacilities = hotelFacilityService.getHotelFacilityList(null, null, order);
 		model.put("defaultFacilities", hotelFacilities);
 	}
 	
 	private void createFacilityModelView(List<String> oldFacility, Map<String, Object> model) {
 		PageOrder order = new SimpleOrderImpl();
 		order.setOrderColumn("name");
 		order.setSequence(PageOrder.Sequence.ASC.toString());
 		List<HotelFacility> hotelFacilities = hotelFacilityService.getHotelFacilityList(null, null, order);
 		model.put("defaultFacilities", hotelFacilities);
 		model.put("oldFacilities", StringUtils.join(oldFacility, ","));
 	}
 	
 	private void setupCKEditor(Map<String, Object> model) {
 		CKEditorConfigFactory ckeeditorConfigFactory = new CKEditorConfigFactory(systemPropertyConfig);
 		CKEditorConfig settings = ckeeditorConfigFactory.getDefaulConfig();
 		model.put("ckeditorSettings", settings);
 	}
 }
