 package com.server.cx.service.cx.impl;
 
 import com.cl.cx.platform.dto.DataItem;
 import com.cl.cx.platform.dto.DataPage;
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.server.cx.dao.cx.GraphicInfoDao;
 import com.server.cx.dao.cx.HolidayTypeDao;
 import com.server.cx.dao.cx.UserHolidayMGraphicDao;
 import com.server.cx.dao.cx.spec.GraphicInfoSpecifications;
 import com.server.cx.entity.cx.GraphicInfo;
 import com.server.cx.entity.cx.HolidayType;
 import com.server.cx.entity.cx.UserHolidayMGraphic;
 import com.server.cx.exception.CXServerBusinessException;
 import com.server.cx.exception.NotSubscribeTypeException;
 import com.server.cx.model.ActionBuilder;
 import com.server.cx.service.cx.HolidayTypeService;
 import com.server.cx.service.cx.UserSubscribeTypeService;
 import com.server.cx.service.util.BusinessFunctions;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Sort;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.annotation.Nullable;
 import java.util.List;
 import java.util.Map;
 
 @Service(value = "holidayTypeService")
 @Transactional(readOnly = true)
 public class HolidayTypeServiceImpl extends UserCheckService implements HolidayTypeService {
     private static final Logger LOGGER = LoggerFactory.getLogger(HolidayTypeServiceImpl.class);
     
     @Autowired
     private BasicService basicService;
     @Autowired
     private HolidayTypeDao holidayTypeDao;
     @Autowired
     private GraphicInfoDao graphicInfoDao;
     @Autowired
     private BusinessFunctions businessFunctions;
 
     @Autowired
     private UserHolidayMGraphicDao userHolidayMGraphicDao;
 
     @Autowired
     private UserSubscribeTypeService userSubscribeTypeService;
 
     @Autowired
     private ActionBuilder actionBuilder;
 
     @Override
     public DataPage queryAllHolidayTypes(String imsi) throws NotSubscribeTypeException {
         checkAndSetUserInfoExists(imsi);
         //检查是否订购了节日包
         userSubscribeTypeService.checkSubscribeType(userInfo, "holiday");
         List<HolidayType> holidayTypes = Lists.newArrayList(holidayTypeDao.findAll());
         final String baseHref = basicService.generateHolidayTypesVisitURL(imsi);
         List<DataItem> holidayTypeList = generateHolidayTypeList(imsi, holidayTypes);
 
         DataPage dataPage = new DataPage();
         dataPage.setOffset(0);
         dataPage.setLimit(holidayTypeList.size());
         dataPage.setTotal(1);
         dataPage.setHref(baseHref);
         dataPage.setItems(holidayTypeList);
         dataPage.setActions(actionBuilder.buildCancelSubscribeHolidayAction(imsi));
         return dataPage;
 
     }
     
    @Transactional(readOnly = false)
     @Override
     public DataPage subscribeAndQueryHoliayTypes(String imsi) {
         LOGGER.info("Into subscribeAndQueryHoliayTypes imsi = " + imsi);
         checkAndSetUserInfoExists(imsi);
         userSubscribeTypeService.checkUserUnSubscribeType(userInfo, "holiday");
         userSubscribeTypeService.subscribeType(userInfo, "holiday");
         return queryAllHolidayTypes(imsi);
     }
     
     @Override
     public GraphicInfo getFirstChild(Long holidayTypeId) {
         List<GraphicInfo> graphicInfos = graphicInfoDao.findAll(GraphicInfoSpecifications.holidayTypeGraphicInfoExcludedUsed(holidayTypeId,null),new Sort(Sort.Direction.DESC,"createdOn"));
         if(graphicInfos != null && graphicInfos.size()>0){
             for(GraphicInfo graphicInfo : graphicInfos){
                 if(graphicInfo.getPrice() == 0D){
                     return graphicInfo;
                 }
             }
 
         }
         throw new CXServerBusinessException("该节日包下没有数据!");
     }
 
     private List<DataItem> generateHolidayTypeList(String imsi, List<HolidayType> holidayTypes) {
         List<UserHolidayMGraphic>  userHolidayMGraphics = userHolidayMGraphicDao.findByUserInfo(userInfo);
         List<HolidayType> holidayTypeList = Lists.transform(userHolidayMGraphics, businessFunctions.userHolidayMGraphicTransformToHolidayType());
 
         Map<Long,UserHolidayMGraphic> userHolidayMGraphicMap =  Maps.uniqueIndex(userHolidayMGraphics, new Function<UserHolidayMGraphic, Long>() {
             @Override
             public Long apply(@Nullable UserHolidayMGraphic input) {
                 if(input == null) return null;
                 HolidayType holidayType = input.getHolidayType();
                 return holidayType.getId();
             }
         });
 
         return Lists.transform(holidayTypes,businessFunctions.holidayTypeTransformToDataItem(imsi,holidayTypeList,userHolidayMGraphicMap));
     }
 
     @Override
     public void cancelSubscribeHolidayType(String imsi) {
         userSubscribeTypeService.cancelSubscribeType(imsi, "holiday");
     }
 
 }
