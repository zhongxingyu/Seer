 package com.instudies.pages.blocks;
 
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.FindBy;
 import org.openqa.selenium.support.PageFactory;
 
 import com.instudies.pages.MainPage;
 import com.instudies.pages.Page;
 
 public class LeftSidebarBlockNavigationUserBlock extends Page {
 
 	public LeftSidebarBlockNavigationUserBlock(WebDriver driver) {
 		super(driver);
 	}
 	
 	@Override
 	public void open() {}
 	
 	@FindBy(css="a[data-test-linkuserprofile]")
 	public WebElement linkUserProfile;
 	
 	@FindBy(css="a[data-test-linklogout]")
 	public WebElement linkLogout;
 	
 	@FindBy(css="a[data-test-linkallactivity]")
 	public WebElement linkAllActivity;
 
	@FindBy(css="a[data-test-linkinbox]")
	public WebElement linkinbox;
 	
 	public MainPage logout() {
 		linkLogout.click();
 		return PageFactory.initElements(driver, MainPage.class);
 	}
 	
 }
