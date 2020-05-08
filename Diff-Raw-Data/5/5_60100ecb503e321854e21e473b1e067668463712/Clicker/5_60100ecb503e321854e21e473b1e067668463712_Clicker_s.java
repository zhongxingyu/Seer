 package tk.yourchanges.clicker.clicker;
 
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import java.util.concurrent.TimeUnit;
 
 
 
 
 
 import org.apache.commons.lang.math.RandomUtils;
 import org.jsoup.Connection;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.openqa.selenium.*;
 import org.openqa.selenium.Proxy.ProxyType;
 import org.openqa.selenium.chrome.ChromeDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.firefox.FirefoxProfile;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 import org.openqa.selenium.ie.InternetExplorerDriver;
 import org.openqa.selenium.remote.CapabilityType;
 import org.openqa.selenium.remote.DesiredCapabilities;
 
 
 /**
  * 
  * @author <a href="mailto:yourchanges@gmail.com">Yuanjun Li</a>
  *
  */
 public class Clicker {
 	private WebDriver driver;
 	private ClickerConfig conf;
 	private HashSet<String> urls;
 	
 	public Clicker(File confFile){		
 		this.init(confFile);
 	}
 	
 	private void init(File confFile){		
 		String jsonString = Utils.readFile(confFile);
 		conf = JsonMapper.buildNonEmptyMapper().fromJson(jsonString , ClickerConfig.class);
 		if(conf.getProxyHostPortString().length==0){
 			conf.setUseProxy(false);
 		}		
 		try {
 			urls = getArticleList();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public Clicker(){		
 		String file = this.getClass().getClassLoader().getResource("conf.json").getFile();
 		this.init(new File(file));
 	}
 	
 	
 	/**
 	 * 默认启动基于第一个代理地址的WebDriver
 	 * 
 	 * @throws Exception
 	 */
 	public void setUp() throws Exception {
 		setUp(0);
 	}
 	
 	/**
 	 * 启动基于第proxyIndex个代理地址的WebDriver
 	 * 
 	 * @param proxyIndex
 	 * @throws Exception
 	 */
 	public void setUp(int proxyIndex) throws Exception {
 		if("firefox".equalsIgnoreCase(conf.getBrowser())){
 			setUpFirefoxDriver(proxyIndex);
 		} else if("ie".equalsIgnoreCase(conf.getBrowser())){
 			setUpIEDriver(proxyIndex);
 		} else if("chrome".equalsIgnoreCase(conf.getBrowser())){
 			setUpChromeDriver(proxyIndex);
 		} else if("htmlunit".equalsIgnoreCase(conf.getBrowser())){
 			setUpHtmlUnitDriver(proxyIndex);
 		} else{
 			throw new RuntimeException("当前还不支持你指定的浏览器：" + conf.getBrowser());
 		}
 		
 		if(driver!=null){
 			driver.manage().timeouts().setScriptTimeout(conf.getJavascriptTimeout(), TimeUnit.SECONDS);
			driver.manage().timeouts().pageLoadTimeout(conf.getPageLoadTimeout(), TimeUnit.SECONDS);
 			driver.manage().timeouts().implicitlyWait(conf.getElementSearchTimeout(), TimeUnit.SECONDS);
 		}
 		
 	}
 	
 	private void setUpChromeDriver(int proxyIndex){
 		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
 		if(conf.isUseProxy()){
 			System.out.println("正在使用代理："+conf.getProxyHostPortString()[proxyIndex]);
 			
 			org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
 			//proxy.setProxyType(ProxyType.MANUAL);
 			proxy.setHttpProxy("http://" + conf.getProxyHostPortString()[proxyIndex]);
 			capabilities.setCapability(CapabilityType.PROXY, proxy);
         }
 		driver = new ChromeDriver(capabilities);		
 	}
 	
 	private void setUpFirefoxDriver(int proxyIndex){
 		FirefoxProfile profile = new FirefoxProfile();
 		if(conf.isUseProxy()){
 			System.out.println("正在使用代理："+conf.getProxyHostPortString()[proxyIndex]);
 			
 			profile.setPreference("network.proxy.type", ProxyType.MANUAL.ordinal());
 			profile.setPreference("network.proxy.http", conf.getProxyHost(proxyIndex));
 			profile.setPreference("network.proxy.http_port", conf.getProxyPort(proxyIndex));
 			profile.setPreference("network.proxy.ssl", conf.getProxyHost(proxyIndex));
 		    profile.setPreference("network.proxy.ssl_port", conf.getProxyPort(proxyIndex));
 		}
 		driver = new FirefoxDriver(profile);
 	}
 	
 	private void setUpIEDriver(int proxyIndex){
 		DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
 		if(conf.isUseProxy()){
 			System.out.println("基于IE的代理配置尚不被支持. 请使用其他浏览器。");
 			conf.setUseProxy(false);
 			
 			/*org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
 			proxy.setProxyType(ProxyType.MANUAL);
 			proxy.setHttpProxy(conf.getProxyHostPortString()[proxyIndex]);
 			//proxy.setSslProxy(conf.getProxyHostPortString()[proxyIndex]);
 			capabilities.setCapability(CapabilityType.PROXY, proxy);*/
         }
 		driver = new InternetExplorerDriver(capabilities);
 	}
 	
 	private void setUpHtmlUnitDriver(int proxyIndex){
 		DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
 		if(conf.isUseProxy()){
 			System.out.println("正在使用代理："+conf.getProxyHostPortString()[proxyIndex]);
 			
 			org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
 			//proxy.setProxyType(ProxyType.MANUAL);
 			proxy.setHttpProxy(conf.getProxyHostPortString()[proxyIndex]);
 			capabilities.setCapability(CapabilityType.PROXY, proxy);
 			capabilities.setBrowserName("firefox");
         }
 		driver = new HtmlUnitDriver(capabilities);
 		((HtmlUnitDriver)driver).setJavascriptEnabled(true);
 	}
 	
 	/**
 	 * 获取一个页面里的文章列表
 	 * 
 	 * @param set
 	 * @param url
 	 * @return
 	 * @throws Exception
 	 */
 	private HashSet<String> getArticleList(HashSet<String> set, String url) throws Exception {		
 		Connection conn = Jsoup.connect(url);
 		Document doc= conn.get();
 		Elements elements= doc.select(conf.getArticleLinkXpath());
 		
 		for(Element e :elements){
 			System.out.println(e.attr("href"));
 			set.add(e.attr("href"));
 		}
 		
 		return set;
 	}
 	
 	/**
 	 * 获取所有URL对应页面里的文章列表
 	 * 
 	 * @return
 	 * @throws Exception
 	 */
 	public HashSet<String> getArticleList() throws Exception {
 		System.out.println("获取到下面文章链接：");
 		
 		HashSet<String> set =new HashSet<String>();
 		for(String url:conf.getBaseUrl()){
 			set = getArticleList(set, url);
 		}
 		return set;
 	}
 	
 	
 	
 	/**
 	 * 先本地不用代理跑一次，然后每个代理跑一次
 	 */
 	public void runAll(){
 		
 		boolean flag = this.conf.isUseProxy();
 		
 		runWithoutProxy();
 		
 		this.conf.setUseProxy(flag);
 		if(this.conf.isUseProxy()){
 			for(int i=0;i<conf.getProxyHostPortString().length;i++){
 				runFlow(i);
 			}
 		}		
 	}
 	
 	/**
 	 * 本地不用代理跑一次
 	 */
 	public void runWithoutProxy(){
 		this.conf.setUseProxy(false);
 		runFlow(0);
 	}
 	
 	private void runFlow(int i){
 		try{
 	        this.setUp(i);
 	        this.run();		        
         }catch(Exception e){
         	e.printStackTrace();
         }finally{
         	try {
 				this.tearDown();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
         }
 	}
 
 	/**
 	 * 先进行setUp()<br/><br/>
 	 * 跑所有文章，如果处理的文章数大于ClickerConfig.getHandleArticleNumbers()就停止
 	 * 
 	 * @throws Exception
 	 */
 	public void run() throws Exception {		
 		int i=0;
 		ArrayList<String> list = new ArrayList<String>(urls);
 		
 		for(;;){
 			if((i++)>=conf.getHandleArticleNumPerProxy()){
 				break;
 			}
 			
 			//随机获取一个URL
 			String url = list.get(RandomUtils.nextInt(list.size()-1));
 			
 			System.out.println("处理："+url);
 			
 			//先浏览一次，防止点击率过高
 			driver.get(url);  												 	//点进文章
 			
 			//广告块
 			driver.get(url);  												 	//点进文章
 			Utils.sleep(500);													//等待页面加载完毕
 			
 			//广告1..n
 			for(String adXpath:conf.getAdXpath()){
 				try{
 					driver.findElement(By.xpath(adXpath)).click();
 					Utils.sleep(500);
 				}catch(Exception e){
 					e.printStackTrace();
 				}
 			}
 			
 			Utils.sleep(1000);			
 		}
 	}
 
 	/**
 	 * 关闭WebDriver
 	 * @throws Exception
 	 */
 	public void tearDown() throws Exception {
 		Utils.sleep(conf.getAdShowTime());//广告载入，展示时间
 		if(driver != null){
 			driver.quit();		
 			driver =null;
 		}
 	}
 
 	/*private boolean isElementPresent(By by) {
 		try {
 			driver.findElement(by);
 			return true;
 		} catch (NoSuchElementException e) {
 			return false;
 		}
 	}*/
 }
 
