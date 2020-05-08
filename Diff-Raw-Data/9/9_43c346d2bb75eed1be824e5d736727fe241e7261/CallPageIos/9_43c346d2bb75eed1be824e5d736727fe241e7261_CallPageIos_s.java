 package tests.page.ios;
 
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.openqa.selenium.TimeoutException;
 
 import tests.page.CallPage;
 import tests.page.android.CallPageAndroid;
 
 import com.annotation.FindBy;
 import com.element.UIView;
 import com.mobile.driver.nativedriver.NativeDriver;
 import com.mobile.driver.page.PageFactory;
 import com.mobile.driver.wait.Sleeper;
 
 public class CallPageIos extends CallPage {
 
 	@FindBy(locator = "В сети")
 	// "LinphoneRegistrationOk")
 	public UIView status;
 	
 	@FindBy(locator = "/window[1]/scrollview[1]/webview[1]/text[1]")
 	public UIView online;
 
 	@FindBy(locator = "//window[1]/scrollview[1]/webview[1]/link[1]")
 	private UIView one;
 
 	@FindBy(locator = "//window[1]/scrollview[1]/webview[1]/link[2]")
 	private UIView two;
 
 	@FindBy(locator = "//window[1]/scrollview[1]/webview[1]/link[3]")
 	private UIView three;
 
 	@FindBy(locator = "//window[1]/scrollview[1]/webview[1]/link[4]")
 	private UIView four;
 
 	@FindBy(locator = "//window[1]/scrollview[1]/webview[1]/link[5]")
 	private UIView five;
 
 	@FindBy(locator = "//window[1]/scrollview[1]/webview[1]/link[6]")
 	private UIView six;
 
 	@FindBy(locator = "//window[1]/scrollview[1]/webview[1]/link[7]")
 	private UIView seven;
 
 	@FindBy(locator = "//window[1]/scrollview[1]/webview[1]/link[8]")
 	private UIView eight;
 
 	@FindBy(locator = "//window[1]/scrollview[1]/webview[1]/link[9]")
 	private UIView nine;
 
 	@FindBy(locator = "//window[1]/scrollview[1]/webview[1]/link[10]")
 	private UIView star;
 
 	@FindBy(locator = "//window[1]/scrollview[1]/webview[1]/link[11]")
 	private UIView zero;
 
 	@FindBy(locator = "//window[1]/scrollview[1]/webview[1]/link[12]")
 	private UIView grill;
 
 	@FindBy(locator = "//window[1]/scrollview[1]/webview[1]/textfield[1]")
 	private UIView digitDisplay;
 
 	@FindBy(locator = "//window[1]/scrollview[1]/webview[1]/textfield[1]")
 	public UIView fieldNumber;
 
 	@FindBy(locator = "//window[2]/UIAKeyboard[1]/UIAKey[29]")
 	private UIView moreNumber;
 
 	@FindBy(locator = "//window[2]/UIAKeyboard[1]/UIAKey[28]")
 	private UIView deleteButton;
 
 	@FindBy(locator = "//window[1]/scrollview[1]/webview[1]/link[14]/link[1]/text[1]")
 	// "//window[1]/scrollview[1]/webview[1]/link[14]/link[1]")//"//window[1]/scrollview[1]/webview[1]/link[16]/link[1]/text[1]")
 	private UIView contact;
 
 	@FindBy(locator = "Select All")
 	private UIView selectAll;
 
 	@FindBy(locator = "Cut")
 	private UIView cutButton;
 
 	@FindBy(locator = "//window[2]/toolbar[1]/button[1]")
 	private UIView doneButton;
 
 	@FindBy(locator = "Подключение...")
 	private UIView nameConnection;
 
 	@FindBy(locator = "//window[1]/scrollview[1]/webview[1]/text[1]")
 	private UIView nameAbonent;
 
 	@FindBy(locator = "//window[1]/scrollview[1]/webview[1]")
 	private UIView webview;
 
 	@FindBy(locator = "//window[1]/scrollview[1]/webview[1]/link[19]")
 	private UIView settingsTab;
 	
 	@FindBy(locator = "//window[1]/scrollview[1]/webview[1]/text[2]")
 	private UIView timerCall;
 
 	private static final Logger LOGGER = Logger.getLogger(CallPageIos.class);
 	
 	public CallPageIos(NativeDriver driver) {
 		super(driver);
 	}
 
 	@Override
 	public void checkPage() {
 		try {
 			status.waitForElementByName(WAIT_WHILE_LOGIN);
 		}
 		catch(TimeoutException e) {
 			LOGGER.error("Login was unsuccessfull. Can't find " + status);
 			throw new com.mobile.driver.wait.exception.TimeoutException("Call page didn't download");
 		}
 	}
 
 	@Override
 	public boolean isStatusAvailable() {
 		// TODO Auto-generated method stub
		return online.getText().equals("В сети");
 	}
 
 	private List<UIView> dial() {
 		List<UIView> container = new ArrayList<UIView>();
 		container.add(one);
 		container.add(two);
 		container.add(three);
 		container.add(four);
 		container.add(five);
 		container.add(six);
 		container.add(seven);
 		container.add(eight);
 		container.add(nine);
 		container.add(star);
 		container.add(zero);
 		container.add(grill);
 		return container;
 	}
 
 	public void inputAllDigites() {
 
 		for (UIView digit : dial()) {
 			digit.touch();
 		}
 
 	}
 
 	public void inputFromNativeKeyboard(String text ){
 		fieldNumber.touchLong();
 		fieldNumber.type(text);
 		doneButton.touch();
 	}
 
 	public String getTextFieldDigitDisplay() {
 		return digitDisplay.getText();
 	}
 
 	public void clearField() {
 		// fieldNumber.touchLong();
 		// selectAll.touchByName();
 		// cutButton.touchByName();
 		while (!fieldNumber.getText().isEmpty())
 			deleteLastSymbol();
 	}
 
 	@Override
 	public void deleteLastSymbol() {
 		Rectangle point = nine.getLocation();
 		double y = 135;
 		double x = 58;
 		nine.touchWithCoordinates(point.getX() + x, point.getY() + y);
 	}
 
 	public void clickContact() {
 		// contact.touch();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public SettingsPageIos navigateToSettingsTab() {
 		settingsTab.touch();
 		return new SettingsPageIos(driver);
 	}
 
 	public void clickCallButton() {
 		Rectangle point = nine.getLocation();
 		double x = 154;
 		double y = 135;
 		nine.touchWithCoordinates(point.getX() - x, point.getY() + y);
 		Sleeper.SYSTEM_SLEEPER.sleep(3000);
 	}
 
 	public String getNameConnection() {
 		return nameConnection.getFoundBy().toString();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public CallPageIos cancelCall() {
 		Rectangle point = webview.getLocation();
 		double x = 24;
 		double y = 412;
 		webview.touchWithCoordinates(point.getX() + x, point.getY() + y);
 		return PageFactory.initElements(driver, CallPageIos.class);
 	}
 
 	public String getNameAbonent() {
 		return nameAbonent.getAttribute("label");
 	}
 	
 	@Override
 	public String getTimer() {
 		return timerCall.getAttribute("label");
 	}
 
 }
