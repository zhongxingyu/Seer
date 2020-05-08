 /******************************************************************************
  * Copyright (C) 2012 ShenZhen 1000funs Information Technology Co.,Ltd
  * All Rights Reserved.
  *****************************************************************************/
 package com.magiccube.food.service;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
 
 import com.magiccube.core.base.service.BaseService;
 import com.magiccube.core.util.tools.CodeGenerator;
 import com.magiccube.food.dao.FoodDAO;
 import com.magiccube.food.model.FoodGroupVO;
 import com.magiccube.food.model.FoodQueryCondition;
 import com.magiccube.food.model.FoodVO;
 import com.magiccube.food.model.GroupFoods;
 import com.magiccube.food.model.GroupPackages;
 import com.magiccube.food.model.PackageItemVO;
 import com.magiccube.food.model.PackageVO;
 
 /**
  * @author Xingling
  * @since jdk6.0
  * @version 2012-12-16 Xingling build
  */
 public class FoodService extends BaseService {
 
 	final static Logger LOGGER = LoggerFactory.getLogger(FoodService.class);
 
 	private FoodDAO foodDAO;
 
 	/**
 	 * 增加食物（不指定店铺）
 	 * 
 	 * @param foodVO
 	 */
 	public int insertFood(FoodVO foodVO) {
 
 		// 生成食物编号
 		/*
 		 * int foodCount=foodDAO.queryCountOfFood(); String foodCode =
 		 * CodeGenerator.getCode(CodeGenerator.TYPE.FOOD,foodCount);
 		 * foodVO.setCode(foodCode);
 		 */
 
 		return foodDAO.insertFood(foodVO);
 	}
 
 	/**
 	 * 增加食物（同时指定到店铺）
 	 * 
 	 * @param foodVO
 	 */
 	public void insertFoodWithShop(FoodVO foodVO) {
 
 		// 生成食物编号
 		int foodCount = foodDAO.queryCountOfFood();
 		String foodCode = CodeGenerator.getCode(CodeGenerator.TYPE.FOOD,
 				foodCount);
 		foodVO.setCode(foodCode);
 
 		foodDAO.insertFood(foodVO);
 
 		// 获取新生成的食物id
 		int foodId = foodDAO.queryIdForFoodVO(foodVO);
 		foodVO.setId(foodId);
 
 		// 插入FoodReShop信息
 		foodDAO.insertFoodReShop(foodVO);
 	}
 
 	public FoodVO getFood(int id) {
 		return foodDAO.getFood(id);
 	}
 	
 	public FoodGroupVO getGroup(int id) {
 		return foodDAO.getGroup(id);
 	}
 	
 	/**
 	 * 获取一个套餐
 	 * 
 	 * @param id
 	 *            packageId
 	 * @return PackageVO
 	 */
 	public PackageVO getPackage(int id) {
 		return foodDAO.getPackage(id);
 	}
 
 	public int insertFoodReShop(FoodVO foodVO) {
 		return foodDAO.insertFoodReShop(foodVO);
 	}
 
 	/**
 	 * insert package_item
 	 * 
 	 * @param packageItemVO
 	 * @return id
 	 */
 	public int insertPackageItem(PackageItemVO packageItemVO) {
 		return foodDAO.insertPackageItem(packageItemVO);
 	}
 
 	/**
 	 * 新增一个套餐.
 	 * 
 	 * @param packageVO
 	 * @return
 	 */
 	public int insertPackage(PackageVO packageVO) {
 		// 1. 先往food表插入一个记录
 		int packageId = insertFood(packageVO);
 
 		// 2. 往package_item表插入相关的关联信息.
 		insertPackageItems(packageVO.getItems(), packageId);
 
 		// 3. 往food_re_shop表插入相关的关联信息
 		insertFoodReShop(packageVO);
 		return packageId;
 	}
 
 	/**
 	 * 更新套餐
 	 * 
 	 * @param packageVO
 	 * @return true-成功; false-失败
 	 */
 	public boolean updatePackage(PackageVO packageVO) {
 		// 1. 先更新food表
 		updateFood(packageVO);
 
 		// 2. 更新food_re_shop表
 		updateFoodReShop(packageVO);
 
 		// 3. 更新package_item表;先删除再插入
 		int packageId = packageVO.getId();
 		deletePackageItem(packageId);
 		insertPackageItems(packageVO.getItems(), packageId);
 
 		return true;
 	}
 
 	/**
 	 * 批量插入packageItems
 	 * 
 	 * @param items
 	 * @param packageId
 	 * @return
 	 */
 	public boolean insertPackageItems(List<PackageItemVO> items, int packageId) {
 		for (PackageItemVO packageItem : items) {
 			packageItem.setPackageId(packageId);
 			insertPackageItem(packageItem);
 		}
 		return true;
 	}
 
 	/**
 	 * 增加食物分组
 	 * 
 	 * @param foodGroupVO
 	 * @return int id
 	 */
 	public int insertFoodGroup(FoodGroupVO foodGroupVO) {
 		return foodDAO.insertFoodGroup(foodGroupVO);
 	}
 
 	/**
 	 * 更新分组信息
 	 * 
 	 * @param foodGroupVO
 	 * @return
 	 */
 	public int updateGroup(FoodGroupVO foodGroupVO) {
 		return foodDAO.updateGroup(foodGroupVO);
 	}
 
 	/**
 	 * 更新食品信息
 	 * 
 	 * @param foodVO
 	 * @return
 	 */
 	public int updateFood(FoodVO foodVO) {
 		return foodDAO.updateFood(foodVO);
 	}
 
 	/**
 	 * 更新FoodReShop
 	 * 
 	 * @param foodVO
 	 * @return
 	 */
 	public int updateFoodReShop(FoodVO foodVO) {
 		return foodDAO.updateFoodReShop(foodVO);
 	}
 
 	/**
 	 * 更新FoodReShop droped字段
 	 * 
 	 * @param foodVO
 	 * @return
 	 */
 	public int updateFoodReShopDrop(int foodId, boolean droped) {
 		return foodDAO.updateFoodReShopDrop(foodId, droped);
 	}
 
 	/**
 	 * 更新食品库存信息
 	 * 
 	 * @param foodId
 	 * @param amountToMinus
 	 *            要减掉的食品数量
 	 * @return
 	 */
 	public int updateFoodReShopStock(int foodId, int amountToMinus) {
 		return foodDAO.updateFoodReShopStock(foodId, amountToMinus);
 	}
 
 	/**
 	 * 查询所有在架食物
 	 * 
 	 * @return
 	 */
 	public List<FoodVO> queryFoods() {
 		// TODO get current shopId, then query it's foods. transiently, use
 		// default value : 1
 		int shopId = 1;
 		return foodDAO.queryFoodsByShopId(shopId);
 	}
 
 	public List<GroupFoods> queryAllGroupAndFoods(
 			FoodQueryCondition foodQueryCondition) {
 		return foodDAO.queryAllGroupAndFoods(foodQueryCondition);
 	}
 
 	public List<GroupPackages> queryAllGroupPackages(int shopId) {
 		return foodDAO.queryAllGroupPackages(shopId);
 	}
 
 	public List<GroupFoods> queryAvailableGroupAndFoods(
 			FoodQueryCondition foodQueryCondition) {
 		return foodDAO.queryAvailableGroupAndFoods(foodQueryCondition);
 	}
 
 	public List<FoodGroupVO> queryGroups(int type) {
 		return foodDAO.queryGroups(type);
 	}
 
 	public List<FoodGroupVO> queryAllGroups() {
 		return foodDAO.queryAllGroups();
 	}
 
 	public int deleteGroup(int id) {
 		int ret = foodDAO.deleteGroup(id);
 
 		// 同时删除与此分组相关联的信息
 		foodDAO.deleteFoodReShopByGroup(id);
 		return ret;
 	}
 
 	public int deleteFood(int id) {
 		int ret = foodDAO.deleteFood(id);
 
 		// 同时删除food_re_shop中与此食物的关联信息
 		deleteFoodReShop(id);
 		return ret;
 	}
 
 	/**
 	 * 删除一个套餐
 	 * 
 	 * @param packageVO
 	 * @return
 	 */
 	public boolean deletePackage(int packageId) {
 		// 1.删除food_re_shop中的相关关联信息
 		deleteFoodReShop(packageId);
 
 		// 2.删除package_item的相关关联信息
 		deletePackageItem(packageId);
 
 		// 3.删除food表中将package删除(deleted=true)
 		deleteFood(packageId);
 
 		return true;
 	}
 
 	/**
 	 * 删除食物关联
 	 * 
 	 * @param foodId
 	 *            食物ID
 	 * @return 成功删除的条数
 	 */
 	public int deleteFoodReShop(int foodId) {
 		return foodDAO.deleteFoodReShop(foodId);
 	}
 
 	/**
 	 * 删除套餐关联
 	 * 
 	 * @param packageId
 	 *            套餐id
 	 * @return 成功删除的条数
 	 */
 	public int deletePackageItem(int packageId) {
 		return foodDAO.deletePackageItem(packageId);
 	}
 
 	/**
 	 * 查询单品食物
 	 * 
 	 * @return
 	 */
 	public List<FoodVO> querySingleFoods(FoodQueryCondition queryCondition) {
 		return foodDAO.querySingleFoods(queryCondition);
 	}
 
 	/**
 	 * 查询可用食物(每种食物在每个店铺只能被添加一次)
 	 * 
 	 * @return
 	 */
 	public List<FoodVO> queryAvailableFoods(FoodQueryCondition queryCondition) {
 		return foodDAO.queryAvailableFoods(queryCondition);
 	}
 
 	/**
 	 * 查询单品食物的总数
 	 * 
 	 * @return
 	 */
 	public int querySingleFoodsCount() {
 		return foodDAO.querySingleFoodsCount();
 	}
 
 	public FoodDAO getFoodDAO() {
 		return foodDAO;
 	}
 
 	public void setFoodDAO(FoodDAO foodDAO) {
 		this.foodDAO = foodDAO;
 	}
 
 }
