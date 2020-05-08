 
 package com.myfitnesspal.qa.test.acceptance;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.support.PageFactory;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import com.myfitnesspal.qa.data.UserData;
 import com.myfitnesspal.qa.foundation.BasicTestCase;
 import com.myfitnesspal.qa.pages.user.AddFoodPage;
 import com.myfitnesspal.qa.pages.user.FoodDiaryPage;
 import com.myfitnesspal.qa.pages.user.LoginPage;
 import com.myfitnesspal.qa.utils.rerunner.RetryAnalyzer;
 
 public class FoodAddFromMyFoodsTest extends BasicTestCase
 {
 
 	private LoginPage loginPage;
 
 	private FoodDiaryPage foodDiaryPage;
 
 	private AddFoodPage addFoodPage;
 
 	UserData user = mfpUser2;
 
 	@Test(retryAnalyzer = RetryAnalyzer.class, groups =
 	{ "ui_acceptance" })
 	public void testFoodAddFromMyFoodsList()
 	{
 		loginPage = PageFactory.initElements(driver, LoginPage.class);
 		loginPage.open();
 		loginPage.login(user.getLogin(), user.getPassword());
 
 		foodDiaryPage = loginPage.getTopMainMenu().clickFood();
 		foodDiaryPage = foodDiaryPage.getTopMainMenu().getTopFoodMenu().clickFoodDiary();
 		foodDiaryPage.initUrl(user.getLogin());
 		Assert.assertTrue(isPageOpened(foodDiaryPage), "Food Diary page wasn't opened");
 
 		String caloriesBefore = foodDiaryPage.valueTotalCalories.getText();
 
 		addFoodPage = foodDiaryPage.addBreakfastFood();
 
		click("btn MostUsed", addFoodPage.btnMostUsed);
 		int countOfQuickFoodsOnMostUsed = addFoodPage.getCountOfQuickAddItems();
		LOGGER.info("countOfQuickFoodsOnMostUsed=" + countOfQuickFoodsOnMostUsed);
 
		click("btn Recent", addFoodPage.btnRecent);
 		int countOfQuickFoodsOnRecent = addFoodPage.getCountOfQuickAddItems();
		LOGGER.info("countOfQuickFoodsOnRecent=" + countOfQuickFoodsOnRecent);
 
		click("btn MyFoods", addFoodPage.btnMyFoods);
 		int countOfQuickFoodsOnMyFoods = driver.findElements(By.xpath(AddFoodPage.QUICK_ADD_FOODS_XPATH)).size();
 		System.out.println(countOfQuickFoodsOnMyFoods);
 		Assert.assertTrue(countOfQuickFoodsOnMyFoods > countOfQuickFoodsOnRecent, "List of my foods to add is empty");
 		foodDiaryPage = addFoodPage.addCheckedFoodLine(countOfQuickFoodsOnRecent, 2, 2);
 		foodDiaryPage.initUrl(user.getLogin());
 		Assert.assertTrue(isPageOpened(foodDiaryPage), "Food Diary page wasn't opened");
 
 		String caloriesAfter = foodDiaryPage.valueTotalCalories.getText();
 
 		Assert.assertNotEquals(getDouble(caloriesBefore), getDouble(caloriesAfter), "Calories are the same as before food adding");
 	}
 
 }
