 package com.xlthotel.core.admin.controller;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.json.JSONObject;
 import org.json.JSONWriter;
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
 import com.xlthotel.core.admin.service.RegionService;
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
 import com.xlthotel.foundation.orm.entity.Region;
 
 @Controller
 public class HotelController {
 
 	@Autowired
 	private HotelService hotelService;
 	
 	@Autowired
 	private HotelFacilityService hotelFacilityService;
 	
 	@Autowired
 	private PhotoService photoService;
 	
 	@Autowired
 	private SystemPropertyConfig systemPropertyConfig;
 	
 	@Autowired
 	private RegionService regionService;
 	
 	@RequestMapping(method=RequestMethod.GET, value="/servlet/admin/hotelList.do")
 	public ModelAndView getHotelList() {
 		Page page = new SimplePageImpl();
 		page.setIndex(0);
 		page.setCount(10);
 		
 		PageOrder order = new SimpleOrderImpl();
 		order.setOrderColumn("status");
 		order.setSequence(PageOrder.Sequence.DESC.toString());
 		
 		List<Hotel> hotelList = hotelService.getHotelList(page, null, order);
 		List<Region> provinceList = regionService.getProvinces();
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		returnModel.put("hotelList", hotelList);
 		returnModel.put("provinceList", provinceList);
 		returnModel.put("page", page);
 		returnModel.put("order", order);
 		return new ModelAndView("/hotel/hotelList", returnModel);
 	}
 	
 	@RequestMapping(method=RequestMethod.POST, value="/servlet/admin/hotelList.do")
 	public ModelAndView getHotelList(HttpServletRequest request, HttpServletResponse response) {
 		String postMethod = SimpleServletRequestUtils.getStringParameter(request, "postMethod", "");
 		if (postMethod.equals("updateHotelStatus")) {
 			List<String> selectedHotel = getSelectedHotel(request);
 			int status = SimpleServletRequestUtils.getIntParameter(request, "statusToUpdate", -1);
 			if (status >= Hotel.Status.Disable.getValue() || status <= Hotel.Status.Enable.getValue()) {
 				hotelService.updateHotelStatus(selectedHotel, status);
 			}
 		}
 		
 		Page page = getPageFromRequest(request);
 		PageOrder order = getOrderFromRequest(request);
 		
 		String name = SimpleServletRequestUtils.getStringParameter(request, "searchName", "");
 		String pId = SimpleServletRequestUtils.getStringParameter(request, "selectedProvince", "");
 		String cId = SimpleServletRequestUtils.getStringParameter(request, "selectedCity", "");
 		Condition condition = new SimpleConditionImpl();
 		condition.setEntityName("Hotel");
 		if (StringUtils.isNotBlank(name)) {
 			condition.putCondition("name", "like", "name", name);
 		}
 		
 		if (StringUtils.isNotBlank(pId) && StringUtils.isBlank(cId)) {
 			condition.putCondition("region.pNodeId", "=", "pId", pId);
 		} else if (StringUtils.isNotBlank(cId)) {
 			condition.putCondition("region.nodeId", "=", "cId", cId);
 		}
 		
 		List<Hotel> hotelList = hotelService.getHotelList(page, condition, order);
 		List<Region> provinceList = regionService.getProvinces();
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		returnModel.put("hotelList", hotelList);
 		returnModel.put("page", page);
 		returnModel.put("order", order);
 		returnModel.put("searchName", name);
 		returnModel.put("pId", pId);
 		returnModel.put("cId", cId);
 		returnModel.put("provinceList", provinceList);
 		if (StringUtils.isNotBlank(pId)) {
 			List<Region> cityList = regionService.getCities(pId);
 			returnModel.put("cityList", cityList);
 		}
 		return new ModelAndView("/hotel/hotelList", returnModel);
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "/servlet/admin/hotel/getCity.do")
 	public void getCity(HttpServletRequest request,
 			HttpServletResponse response,
 			@RequestParam(value = "pId") String pId) {
 		try {
 			response.setContentType("text/plain");
 			response.setCharacterEncoding("UTF-8");
 			List<Region> cityList = regionService.getCities(pId);
 			Writer writer = response.getWriter();
 			JSONWriter jsonWriter = new JSONWriter(writer).array();
 			for (Region city : cityList) {
 				Map<String, Object> map = new HashMap<String, Object>();
 				map.put("nodeName", city.getNodeName());
 				map.put("nodeId", city.getNodeId());
 				jsonWriter.value(new JSONObject(map));
 			}
 			jsonWriter.endArray();
 			writer.flush();
 			writer.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@RequestMapping(method=RequestMethod.GET, value="/servlet/admin/createHotel.do")
 	public ModelAndView createHotel() {
 		Hotel hotel = new Hotel();
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		setupCKEditor(returnModel);
 		List<Region> provinceList = regionService.getProvinces();
 		returnModel.put("hotel", hotel);
 		returnModel.put("provinceList", provinceList);
 		returnModel.put("hotelOperationMethod", "save");
 		createFacilityModelView(returnModel);
 		return new ModelAndView("/hotel/hotelDetail", returnModel);
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
 	
 	@RequestMapping(method=RequestMethod.GET, value="/servlet/admin/updateHotel.do")
 	public ModelAndView updateHotel(@RequestParam(value="id") String id) {
 		Hotel hotel = hotelService.find(id);
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		createPhotosModelView(hotel, returnModel);
 		createLocationModelView(hotel, returnModel);
 		createFacilityModelView(hotel.convertFacilityToIdList(), returnModel);
 		setupCKEditor(returnModel);
 		List<Region> provinceList = regionService.getProvinces();
 		returnModel.put("provinceList", provinceList);
 		if (hotel.getRegion() != null) {
 			List<Region> cityList = regionService.getCities(hotel.getRegion().getpNodeId());
 			returnModel.put("cityList", cityList);
 		}
 		returnModel.put("hotel", hotel);
 		returnModel.put("hotelOperationMethod", "update");
 		return new ModelAndView("/hotel/hotelDetail", returnModel);
 	}
 	
 	private void createPhotosModelView(Hotel hotel, Map<String, Object> modelView) {
 		String photoRawString = hotel.getPhotos();
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
 	
 	private void createPhotosModelView(HttpServletRequest request,
 			Map<String, Object> modelView, Map<String, String> photoTitles) {
 		List<MediaItem> photos = new ArrayList<MediaItem>(6);
 		for (int i = 1; i < 7; i++) {
 			String mediaItemId = SimpleServletRequestUtils.getStringParameter(
 					request, "hotelPhotoId" + i, "");
 			MediaItem mediaItem = null;
 			if (StringUtils.isNotBlank(mediaItemId)) {
 				mediaItem = photoService.getMediaItemModel(mediaItemId);
 				mediaItem.setText(photoTitles.get(mediaItemId));
 			}
 			photos.add(mediaItem);
 		}
 		modelView.put("photos", photos);
 	}
 	
 	private void createLocationModelView(Hotel hotel, Map<String, Object> modelView) {
 		String location = hotel.getLocation();
 		if (StringUtils.isNotBlank(location)) {
 			String[] parts = location.split(",");
			if (parts.length == 2) {
				modelView.put("hotelLongitude", parts[0]);
				modelView.put("hotelLatitude", parts[1]);
			}
 		}
 	}
 	
 	@RequestMapping(method = RequestMethod.POST, value = "/servlet/admin/saveHotel.do")
 	public ModelAndView saveHotel(HttpServletRequest request,
 			HttpServletResponse response,
 			@RequestParam(value = "hotelOperationMethod") String method) throws IOException {
 		Hotel hotel = bindHotel(request, method);
 		Map<String, String> photoTitles = bindPhotoTitles(request);
 		List<String> selectedHotelFacilities = getSelectedHotelFacilities(request);
 		List<String> errors = checkHotel(hotel);
 		checkPhotoTitles(photoTitles, errors);
 		if (errors.size() > 0) {
 			Map<String, Object> returnModel = new HashMap<String, Object>();
 			createPhotosModelView(request, returnModel, photoTitles);
 			createLocationModelView(hotel, returnModel);
 			setupCKEditor(returnModel);
 			List<Region> provinceList = regionService.getProvinces();
 			returnModel.put("provinceList", provinceList);
 			returnModel.put("hotel", hotel);
 			returnModel.put("hotelOperationMethod", method);
 			returnModel.put("errors", errors);
 			setDeleteUrlBackToModelView(request, returnModel);
 			createFacilityModelView(selectedHotelFacilities, returnModel);
 			return new ModelAndView("/hotel/hotelDetail", returnModel);
 		}
 		String photoIdString = mergeAndDeletePhotos(hotel.getId(), request, photoTitles);
 		hotel.setPhotos(photoIdString);
 		
 		if (method.equals("save")) {
 			hotelService.saveHotel(hotel, selectedHotelFacilities);
 		} else {
 			hotelService.updateHotel(hotel, selectedHotelFacilities);
 		}
 		response.sendRedirect("hotelList.do");
 		return null;
 	}
 	
 	private void checkPhotoTitles(Map<String, String> photoTitles, List<String> errors) {
 		for (String title : photoTitles.values()) {
 			if (title.length() > 20) {
 				errors.add("图片标题长度不能超过20个字");
 				return;
 			}
 		}
 	}
 	
 	private Map<String, String> bindPhotoTitles(HttpServletRequest request) {
 		Map<String, String> result = Maps.newHashMap();
 		for (int i = 1; i < 7; i++) {
 			String titleHTMLId = "photoText" + i;
 			String PhotoHTMLId = "hotelPhotoId" + i;
 			String title = SimpleServletRequestUtils.getStringParameter(request, titleHTMLId, "");
 			String photoId = SimpleServletRequestUtils.getStringParameter(request, PhotoHTMLId, "");
 			if (StringUtils.isNotBlank(photoId)) {
 				result.put(photoId, title);
 			}
 		}
 		return result;
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
 	
 	private Hotel bindHotel(HttpServletRequest request, String method) {
 		Hotel hotel = new Hotel();
 		String name = SimpleServletRequestUtils.getStringParameter(request, "hotelName", "");
 		String lat = SimpleServletRequestUtils.getStringParameter(request, "hotelLat", "");
 		String lon = SimpleServletRequestUtils.getStringParameter(request, "hotelLon", "");
 		int status = SimpleServletRequestUtils.getIntParameter(request, "selectStatus", -1);
 		String info = SimpleServletRequestUtils.getStringParameter(request, "hotelInfo", "");
 		String shortInfo = SimpleServletRequestUtils.getStringParameter(request, "hotelShortInfo", "");
 		String cId = SimpleServletRequestUtils.getStringParameter(request, "selectedCity", "");
 		Region region = regionService.getRegion(cId);
 		hotel.setRegion(region);
 		hotel.setName(name);
 		hotel.setLocation(lon + "," + lat);
 		hotel.setStatus(status);
 		hotel.setInfo(info);
 		hotel.setShortInfo(shortInfo);
 		if (method.equals("update")) {
 			String hotelId = SimpleServletRequestUtils.getStringParameter(request, "id", "");
 			hotel.setId(hotelId);
 		}
 		return hotel;
 	}
 	
 	private String mergeAndDeletePhotos(String hotelId, HttpServletRequest request, Map<String, String> photoTitles) {
 		List<String> newIds = getPhotoIds(request);
 		if (hotelId != null) {
 			Hotel oldHotel = hotelService.find(hotelId);
 			List<String> oldIds = oldHotel.converPhotosToList();
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
 	
 	private List<String> checkHotel(Hotel hotel) {
 		List<String> errors = new ArrayList<String>();
 		if (StringUtils.isBlank(hotel.getName())) {
 			errors.add("酒店名称为空");
 		}
 		if (StringUtils.isBlank(hotel.getLocation())) {
 			errors.add("酒店地理座标为空");
 		} else {
 			String location = hotel.getLocation();
 			String[] parts = location.split(",");
 			if (parts.length != 2) {
 				errors.add("酒店地理座标格式不正确");
 			} else if (!NumberUtils.isNumber(parts[0]) || 
 					!NumberUtils.isNumber(parts[1])) {
 				errors.add("酒店地理座标格式不正确");
 			}
 		}
 		if (StringUtils.isBlank(hotel.getShortInfo())) {
 			errors.add("酒店简介为空");
 		}
 		if (StringUtils.isBlank(hotel.getInfo())) {
 			errors.add("酒店详细介绍为空");
 		}
 		if (hotel.getStatus() < Hotel.Status.Disable.getValue() || 
 				hotel.getStatus() > Hotel.Status.Enable.getValue()) {
 			errors.add("酒店状态格式不正确");
 		}
 		if (hotel.getRegion() == null) {
 			errors.add("酒店所属城市为空");
 		}
 		return errors;
 	}
 	
 	private Page getPageFromRequest(HttpServletRequest request) {
 		Page page = new SimplePageImpl();
 		page.setIndex(SimpleServletRequestUtils.getIntParameter(request, "pageIndex", 0));
 		page.setCount(SimpleServletRequestUtils.getIntParameter(request, "pageCount", 10));
 		return page;
 	}
 	
 	private PageOrder getOrderFromRequest(HttpServletRequest request) {
 		PageOrder order = new SimpleOrderImpl();
 		order.setOrderColumn(SimpleServletRequestUtils.getStringParameter(request, "orderColumn", "createDate"));
 		order.setSequence(SimpleServletRequestUtils.getStringParameter(request, "orderSequence", "desc"));
 		return order;
 	}
 	
 	private List<String> getSelectedHotel(HttpServletRequest request) {
 		return Lists.newArrayList(SimpleServletRequestUtils.getStringParameters(request, "selectHotel"));
 	}
 	
 	private List<String> getSelectedHotelFacilities(HttpServletRequest request) {
 		return Lists.newArrayList(SimpleServletRequestUtils.getStringParameters(request, "selectHotelFacility"));
 	}
 	
 	private List<String> getPhotoIds(HttpServletRequest request) {
 		List<String> newPhotoIds = new ArrayList<String>();
 		for (int i = 1; i < 7; i++) {
 			String viewName = "hotelPhotoId" + i;
 			String id = SimpleServletRequestUtils.getStringParameter(request, viewName, "");
 			if (StringUtils.isNotBlank(id)) {
 				newPhotoIds.add(id);
 			}
 		}
 		return newPhotoIds;
 	}
 }
