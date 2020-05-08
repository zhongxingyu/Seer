 package fuzzer;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 
 import com.gargoylesoftware.htmlunit.CookieManager;
 import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.WebResponse;
 import com.gargoylesoftware.htmlunit.html.HtmlElement;
 import com.gargoylesoftware.htmlunit.html.HtmlForm;
 import com.gargoylesoftware.htmlunit.html.HtmlInput;
 import com.gargoylesoftware.htmlunit.util.Cookie;
 
 import site.Form;
 import site.Page;
 import site.Site;
 
 public class Fuzzer {
 	private WebClient webClient;
 	private List<String> sensitiveData;
 	private List<String> maliciousInputs;
 	private double randomThreshold;
 	private boolean complete;
 	private Random random;
 
 	public Fuzzer(WebClient webClient, String sensitiveDataFilePath,
 			String maliciousInputFilePath, String userNamesFilePath,
 			String passwordsFilePath, double randomThreshold, boolean complete)
 			throws IOException {
 		this.webClient = webClient;
 		this.sensitiveData = getSensitiveData(sensitiveDataFilePath);
 		this.maliciousInputs = MaliciousInputs
 				.getMaliciousInputs(maliciousInputFilePath);
 		this.randomThreshold = randomThreshold;
 		this.complete = complete;
 		this.random = new Random(0);
 	}
 
 	private List<String> getSensitiveData(String sensitiveDataFilePath)
 			throws IOException {
 		BufferedReader reader = new BufferedReader(new FileReader(
 				sensitiveDataFilePath));
 
 		try {
 			ArrayList<String> sensitiveData = new ArrayList<String>();
 			String line = reader.readLine();
 			while (line != null) {
 				sensitiveData.add(line);
 				line = reader.readLine();
 			}
 
 			return this.sensitiveData;
 		} finally {
 			reader.close();
 		}
 	}
 
 	public void fuzz(Site site) throws FailingHttpStatusCodeException,
 			MalformedURLException, IOException {
 		fuzzPages(site);
 		fuzzForms(site);
 	}
 
 	private void fuzzForms(Site site) throws IOException {
 		for (Form formWrapper : site.getForms()) {
 			HtmlForm form = formWrapper.getForm();
 			for (String input : maliciousInputs) {
 				if (!complete && random.nextDouble() >= randomThreshold) {
 					continue;
 				}
 				for (HtmlInput inputElement : formWrapper.getInputs()) {
 					inputElement.setValueAttribute(input);
 				}
 				HtmlElement submit = ((HtmlElement) form
 						.getFirstByXPath("//input[@id='submit']"));
 				if (submit == null) {
 					continue; // nothing we can do. If the user is really
 								// determined, they can hardcode the id
 				}
 				if (submit.click().getWebResponse().getContentAsString()
 						.contains(input)) {
 					System.out
 							.println("Form failed to sanitize user input, possible vulnerability:   "
 									+ input + "\n" + form);
 				}
 			}
 		}
 	}
 
 	private void fuzzPages(Site site) throws FailingHttpStatusCodeException,
 			MalformedURLException, IOException {
 		List<Page> pages = site.getPages();
		int n = 0;
 		for (Page page : pages) {
 			fuzz(page);
 			fuzzCookies(page);
 		}
 	}
 
 	private void fuzz(Page page) throws FailingHttpStatusCodeException,
 			MalformedURLException, IOException {
 		for (String argument : page.getArguments()) {
 			if (!complete && random.nextDouble() >= randomThreshold) {
 				continue;
 			}
 			for (String input : maliciousInputs) {
 				if (!complete && random.nextDouble() >= randomThreshold) {
 					continue;
 				}
 				String urlToTest = page.getURL().toExternalForm() + "?"
 						+ argument + "=" + input;
 				try {
 					webClient.getPage(urlToTest);
 				} catch (FailingHttpStatusCodeException e) {
 					if (!isResponseSanitized(e.getResponse(), input)) {
 						System.out.println("Unsanitized user input at:\t"
 								+ urlToTest);
 					}
 					continue;
 				} catch (IOException e) {
 					continue;
 				}
 				System.out.println("Possible vulnerability at:\t" + urlToTest);
 			}
 		}
 	}
 
 	private boolean isResponseSanitized(WebResponse response, String input) {
 		if (response.getContentAsString().contains(input)) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 	
 	private void fuzzCookies(Page p){
 		URL url = p.getURL();
 		try {
			webClient.getPage(url.toString());
 		} catch (FailingHttpStatusCodeException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		CookieManager cookieManager = webClient.getCookieManager();
 		for (Cookie cookie : cookieManager.getCookies(url)) {
 			String name = cookie.getName();
 			String domain = cookie.getDomain();
 			Date expires = cookie.getExpires();
 			String path = cookie.getPath();
 			boolean secure = cookie.isSecure();
 			Cookie newCookie = new Cookie(domain,name,"aaaaaaaaaaaaaaa",path,expires,secure);
 			cookieManager.removeCookie(cookie);
 			cookieManager.addCookie(newCookie);
 			try {
				webClient.getPage(url.toString());
 			} catch (FailingHttpStatusCodeException e) {
 				// TODO Auto-generated catch block
 				System.out.println(url.toString() + ": Altering " + cookie.getName() + "caused a FailingHttpStatusCodeException.");
 				continue;
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				System.out.println(url.toString() + ": Altering " + cookie.getName() + "caused a MalformedURLException.");
 				continue;
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				System.out.println(url.toString() + ": Altering " + cookie.getName() + "caused an IOException.");
 				continue;
 			}
 			cookieManager.removeCookie(newCookie);
 			cookieManager.addCookie(cookie);
 		}
 		
 	}
 
 }
