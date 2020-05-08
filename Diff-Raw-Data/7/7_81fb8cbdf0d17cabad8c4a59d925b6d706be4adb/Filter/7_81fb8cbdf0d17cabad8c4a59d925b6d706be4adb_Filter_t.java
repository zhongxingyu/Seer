 package feedthecat.webpagefilter;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.List;
 
 import org.apache.xml.serialize.DOMSerializerImpl;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlElement;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 
 public class Filter {
 
 	private static final Logger logger = LoggerFactory.getLogger(Filter.class);
 	private final String url;
 
 	public Filter(String url) {
 		this.url = url;
 	}
 
 	public String getResultHtml(Selector selector) {
 		HtmlPage page = getResultPage(selector);
 		return new DOMSerializerImpl().writeToString(page);
 	}
 
 	public HtmlPage loadPage() {
 		WebClient webClient = new WebClient();
		webClient.setThrowExceptionOnFailingStatusCode(false);
		webClient.setThrowExceptionOnScriptError(false);
 		HtmlPage page = null;
 		try {
 			page = webClient.getPage(url);
 		} catch (MalformedURLException e) {
 			logger.debug(e.getMessage(), e);
 		} catch (IOException e) {
 			logger.debug(e.getMessage(), e);
 		}
 		return page;
 	}
 
 	public static String asHtmlSource(List<HtmlElement> elements) {
 		String result = "";
 		for (HtmlElement element : elements) {
 			result += new DOMSerializerImpl().writeToString(element);
 		}
 		return result;
 	}
 
 	public HtmlPage getResultPage(Selector selector) {
 		HtmlPage page = loadPage();
 		filter(page, selector);
 		return page;
 	}
 
 	private void filter(HtmlPage page, Selector selector) {
 		List<HtmlElement> elements = selector.getElements(page
 				.getDocumentElement());
 		HtmlElement root = (HtmlElement) page.getBody().cloneNode(true);
 		if (elements.size() == 1 && elements.get(0).getTagName().equals("html")) {
 			return;
 		}
 		root.removeAllChildren();
 		for (HtmlElement element : elements) {
 			root.appendChild(element);
 		}
 		page.getBody().replace(root);
 	}
 
 	public static String getLinkSource(List<HtmlElement> linkElementList) {
 		for (HtmlElement link : linkElementList) {
 			String href = link.getAttribute("href");
 			if (href != null) {
 				return href;
 			}
 		}
 		return "";
 	}
 }
