 /******************************************************************************
  * Copyright (C) 2012 ShenZhen 1000funs Information Technology Co.,Ltd
  * All Rights Reserved.
  *****************************************************************************/
 package com.magiccube.food.dao;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.beans.BeanUtils;
 
 import com.magiccube.core.base.dao.BaseDAO;
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
 public class FoodDAO extends BaseDAO {
 
 	public int insertFood(FoodVO foodVO) {
 		this.sqlSessionTemplate.insert("com.magiccube.food.insertFood", foodVO);
 		return foodVO.getId();
 	}
 
 	public int queryIdForFoodVO(FoodVO foodVO) {
 		return (Integer) this.sqlSessionTemplate.selectOne(
 				"com.magiccube.food.queryIdForFoodVO", foodVO);
 	}
 
 	public FoodVO getFood(int id) {
 		int shopId = 1; // TODO:从环境中取shopId
 		FoodVO queryVO = new FoodVO();
 		queryVO.setId(id);
 		queryVO.setShopId(shopId);
 		return (FoodVO) this.sqlSessionTemplate.selectOne(
 				"com.magiccube.food.getFood", queryVO);
 	}
 
	
	public FoodGroupVO getGroup(int id) {
		return this.sqlSessionTemplate.selectOne("com.magiccube.food.getGroup", id);
	}
	
 	public FoodGroupVO getGroup(int id) {
 		return this.sqlSessionTemplate.selectOne("com.magiccube.food.getGroup", id);
 	}
 	
 	public FoodGroupVO getGroup(int id) {
 		return this.sqlSessionTemplate.selectOne("com.magiccube.food.getGroup", id);
 	}
 	
 	/**
 	 * 获取一个套餐
 	 * 
 	 * @param id
 	 *            packageId
 	 * @return PackageVO
 	 */
 	public PackageVO getPackage(int id) {
 		FoodVO foodVO = getFood(id);
 		List<PackageItemVO> items = queryPackageItems(id);
 		PackageVO packageVO = new PackageVO();
 		BeanUtils.copyProperties(foodVO, packageVO);
 		packageVO.setItems(items);
 		return packageVO;
 	}
 
 	public int insertFoodReShop(FoodVO foodVO) {
 		return (Integer) this.sqlSessionTemplate.insert(
 				"com.magiccube.food.insertFoodReShop", foodVO);
 	}
 
 	public int insertPackageItem(PackageItemVO packageItemVO) {
 		this.sqlSessionTemplate.insert("com.magiccube.food.insertPackageItem",
 				packageItemVO);
 		return packageItemVO.getId();
 	}
 
 	/**
 	 * 新增分组信息
 	 * 
 	 * @param foodGroupVO
 	 * @return id
 	 */
 	public int insertFoodGroup(FoodGroupVO foodGroupVO) {
 		this.sqlSessionTemplate.insert("com.magiccube.food.insertFoodGroup",
 				foodGroupVO);
 		return foodGroupVO.getId();
 	}
 
 	/**
 	 * 更新分组信息
 	 * 
 	 * @param foodGroupVO
 	 * @return
 	 */
 	public int updateGroup(FoodGroupVO foodGroupVO) {
 		return this.sqlSessionTemplate.update("com.magiccube.food.updateGroup",
 				foodGroupVO);
 	}
 
 	/**
 	 * 更新食品信息
 	 * 
 	 * @param foodVO
 	 * @return
 	 */
 	public int updateFood(FoodVO foodVO) {
 		return this.sqlSessionTemplate.update("com.magiccube.food.updateFood",
 				foodVO);
 	}
 
 	/**
 	 * 更新FoodReShop
 	 * 
 	 * @param foodVO
 	 * @return
 	 */
 	public int updateFoodReShop(FoodVO foodVO) {
 		return this.sqlSessionTemplate.update(
 				"com.magiccube.food.updateFoodReShop", foodVO);
 	}
 
 	/**
 	 * 更新FoodReShop droped字段
 	 * 
 	 * @param foodId
 	 * @return
 	 */
 	public int updateFoodReShopDrop(int foodId, boolean droped) {
 		int shopId = 1;
 		FoodVO foodVO = new FoodVO();
 		foodVO.setShopId(shopId);
 		foodVO.setId(foodId);
 		foodVO.setDroped(droped);
 		return this.sqlSessionTemplate.update(
 				"com.magiccube.food.updateFoodReShopDrop", foodVO);
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
 		int shopId = 1;
 		FoodVO foodVO = new FoodVO();
 		foodVO.setShopId(shopId);
 		foodVO.setId(foodId);
 		foodVO.setStock(amountToMinus);
 		return this.sqlSessionTemplate.update(
 				"com.magiccube.food.updateFoodReShopStock", foodVO);
 	}
 
 	public int deleteGroup(int id) {
 		return this.sqlSessionTemplate.update("com.magiccube.food.deleteGroup",
 				id);
 	}
 
 	public int deleteFood(int id) {
 		return this.sqlSessionTemplate.update("com.magiccube.food.deleteFood",
 				id);
 	}
 
 	/**
 	 * 删除食物关联
 	 * 
 	 * @param foodId
 	 *            食物ID
 	 * @return 成功删除的条数
 	 */
 	public int deleteFoodReShop(int foodId) {
 		int shopId = 1; // TODO:从环境中取shopId
 		FoodVO foodVO = new FoodVO();
 		foodVO.setId(foodId);
 		foodVO.setShopId(shopId);
 		return this.sqlSessionTemplate.delete(
 				"com.magiccube.food.deleteFoodReShop", foodVO);
 	}
 
 	/**
 	 * 删除整个分组的食物关联信息
 	 * 
 	 * @param groupId
 	 * @return
 	 */
 	public int deleteFoodReShopByGroup(int groupId) {
 		int shopId = 1; // TODO:从环境中取shopId
 		FoodVO foodVO = new FoodVO();
 		foodVO.setShopId(shopId);
 		foodVO.setGroupId(groupId);
 		return this.sqlSessionTemplate.delete(
 				"com.magiccube.food.deleteFoodReShopByGroup", foodVO);
 	}
 
 	/**
 	 * 删除套餐关联
 	 * 
 	 * @param packageId
 	 *            套餐id
 	 * @return 成功删除的条数
 	 */
 	public int deletePackageItem(int packageId) {
 		return this.sqlSessionTemplate.delete(
 				"com.magiccube.food.deletePackageItem", packageId);
 	}
 
 	public List<FoodVO> queryFoodsByShopId(int shopId) {
 		List<FoodVO> result = this.sqlSessionTemplate.selectList(
 				"com.magiccube.food.queryFoodsByShopId", shopId);
 		return result;
 	}
 
 	public int queryCountOfFood() {
 		return (Integer) this.sqlSessionTemplate
 				.selectOne("com.magiccube.food.queryCountOfFood");
 	}
 
 	/**
 	 * 查询所有分组及食物的信息(包括空分组)
 	 * 
 	 * @param foodQueryCondition
 	 * @return
 	 */
 	public List<GroupFoods> queryAllGroupAndFoods(
 			FoodQueryCondition foodQueryCondition) {
 		List<FoodVO> foods = this.sqlSessionTemplate.selectList(
 				"com.magiccube.food.queryAllGroupAndFoods", foodQueryCondition);
 		return transferFoodVOToGroupFoods(foods);
 	}
 
 	/**
 	 * 查询可用的分组及食物信息(不包含空分组)
 	 * 
 	 * @param foodQueryCondition
 	 * @return
 	 */
 	public List<GroupFoods> queryAvailableGroupAndFoods(
 			FoodQueryCondition foodQueryCondition) {
 		List<FoodVO> foods = this.sqlSessionTemplate.selectList(
 				"com.magiccube.food.queryAvailableGroupAndFoods",
 				foodQueryCondition);
 		return transferFoodVOToGroupFoods(foods);
 	}
 
 	/**
 	 * 查询所有套餐分组
 	 * 
 	 * @param shopId
 	 * @return
 	 */
 	public List<GroupPackages> queryAllGroupPackages(int shopId) {
 		FoodQueryCondition foodQueryCondition = new FoodQueryCondition(shopId,
 				2);
 		List<FoodVO> foods = this.sqlSessionTemplate.selectList(
 				"com.magiccube.food.queryAvailableGroupAndFoods",
 				foodQueryCondition);
 
 		List<GroupPackages> lst = new ArrayList<GroupPackages>();
 		int oldGroupId = 0;
 		GroupPackages groupPackages = null;
 		for (FoodVO food : foods) {
 			int groupId = food.getGroupId();
 			if (groupId != oldGroupId) {
 				// reset oldid
 				oldGroupId = groupId;
 
 				groupPackages = new GroupPackages();
 				groupPackages.setId(groupId);
 				groupPackages.setGroupName(food.getGroupName());
 				groupPackages.setImage(food.getGroupImage());
 				groupPackages.setDetail(food.getGroupDetail());
 				lst.add(groupPackages);
 			}
 			if (food.getId() != 0) {
 				PackageVO packageVO = getPackage(food.getId());
 				groupPackages.add(packageVO);
 			}
 		}
 		// add last one
 		if (groupPackages != null && !lst.contains(groupPackages))
 			lst.add(groupPackages);
 
 		return lst;
 	}
 
 	/**
 	 * transfer the FoodVO to Map<String, List<FoodVO>>
 	 * 
 	 * @param foods
 	 * @return
 	 */
 	private List<GroupFoods> transferFoodVOToGroupFoods(List<FoodVO> foods) {
 		List<GroupFoods> lst = new ArrayList<GroupFoods>();
 		int oldGroupId = 0;
 		GroupFoods groupFoods = null;
 		for (FoodVO food : foods) {
 			int groupId = food.getGroupId();
 			if (groupId != oldGroupId) {
 				// reset oldid
 				oldGroupId = groupId;
 
 				groupFoods = new GroupFoods();
 				groupFoods.setId(groupId);
 				groupFoods.setGroupName(food.getGroupName());
 				groupFoods.setImage(food.getGroupImage());
 				groupFoods.setDetail(food.getGroupDetail());
 				lst.add(groupFoods);
 			}
 			if (food.getId() != 0)
 				groupFoods.addFood(food);
 		}
 		// add last one
 		if (groupFoods != null && !lst.contains(groupFoods))
 			lst.add(groupFoods);
 		return lst;
 	}
 
 	public List<FoodGroupVO> queryGroups(int type) {
 		List<FoodGroupVO> result = this.sqlSessionTemplate.selectList(
 				"com.magiccube.food.queryGroups", type);
 		return result;
 	}
 
 	public List<FoodGroupVO> queryAllGroups() {
 		List<FoodGroupVO> result = this.sqlSessionTemplate
 				.selectList("com.magiccube.food.queryAllGroups");
 		return result;
 	}
 
 	/**
 	 * 查询单品食物
 	 * 
 	 * @return
 	 */
 	public List<FoodVO> querySingleFoods(FoodQueryCondition queryCondition) {
 		List<FoodVO> foods = this.sqlSessionTemplate.selectList(
 				"com.magiccube.food.querySingleFoods", queryCondition);
 		return foods;
 	}
 
 	/**
 	 * 查询可用食物(每种食物在每个店铺只能被添加一次)
 	 * 
 	 * @return
 	 */
 	public List<FoodVO> queryAvailableFoods(FoodQueryCondition queryCondition) {
 		List<FoodVO> foods = this.sqlSessionTemplate.selectList(
 				"com.magiccube.food.queryAvailableFoods", queryCondition);
 		return foods;
 	}
 
 	/**
 	 * 查询单品食物的总数
 	 * 
 	 * @return
 	 */
 	public int querySingleFoodsCount() {
 		int ret = (Integer) this.sqlSessionTemplate
 				.selectOne("com.magiccube.food.querySingleFoodsCount");
 		return ret;
 	}
 
 	public List<PackageItemVO> queryPackageItems(int packageId) {
 		List<PackageItemVO> items = this.sqlSessionTemplate.selectList(
 				"com.magiccube.food.queryPackageItems", packageId);
 		return items;
 	}
 }
