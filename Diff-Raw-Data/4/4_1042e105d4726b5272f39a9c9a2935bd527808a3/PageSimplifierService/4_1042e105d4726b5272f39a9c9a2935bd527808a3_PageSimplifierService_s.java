 package mdettlaff.mobilemachine.service;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import mdettlaff.mobilemachine.domain.SimplifiedWebpage;
 import mdettlaff.mobilemachine.repository.WebpageRepository;
 
 import org.apache.http.client.ClientProtocolException;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 @Service
 public class PageSimplifierService {
 
 	private final WebpageRepository repository;
 	private final HttpService httpService;
 	private final int maxPageSizeInBytes;
 
 	@Autowired
 	public PageSimplifierService(WebpageRepository repository, HttpService httpService, int maxPageSizeInBytes) {
 		this.repository = repository;
 		this.httpService = httpService;
 		this.maxPageSizeInBytes = maxPageSizeInBytes;
 	}
 
 	public SimplifiedWebpage simplify(String url) throws ClientProtocolException, IOException {
 		if (repository.getByUrl(url) == null) {
 			repository.put(url, createSimplifiedWebpage(url));
 		}
 		return repository.getByUrl(url);
 	}
 
 	SimplifiedWebpage createSimplifiedWebpage(String url) throws ClientProtocolException, IOException {
 		String html = httpService.download(url);
 		String body = extractBody(html);
		String htmlWithUrsReplaced = replaceUrls(body);
		List<String> atoms = splitIntoAtoms(htmlWithUrsReplaced);
 		List<String> pages = splitIntoPages(atoms);
 		String title = extractTitle(html);
 		return new SimplifiedWebpage(title, pages);
 	}
 
 	private String extractTitle(String html) {
 		Pattern pattern = Pattern.compile("<title>(.*?)</title>");
 		Matcher matcher = pattern.matcher(html);
 		if (matcher.find()) {
 			return matcher.group(1);
 		}
 		return "Unknown title";
 	}
 
 	private String extractBody(String html) {
 		Document document = Jsoup.parse(html);
 		Elements body = document.select("#wikitext");
 		return body.html();
 	}
 
 	private String replaceUrls(String html) {
 		return html.replaceAll(
 				"href=\"(http://tvtropes.org/pmwiki/.*?)\"",
 				"href=\"/simplified?url=$1&page=0\"");
 	}
 
 	private List<String> splitIntoAtoms(String html) {
 		String oneLineHtml = html.replaceAll("\\n", "");
 		String[] atomsWithoutSeparators = oneLineHtml.split("</li>\\s+<li>");
 		List<String> atoms = new ArrayList<String>();
 		for (int i = 0; i < atomsWithoutSeparators.length; i++) {
 			String atomWithoutSeparator = atomsWithoutSeparators[i];
 			StringBuilder atom = new StringBuilder();
 			if (i != 0) {
 				atom.append("<li>");
 			}
 			atom.append(atomWithoutSeparator);
 			if (i != atomsWithoutSeparators.length - 1) {
 				atom.append("</li>");
 			}
 			atoms.add(atom.toString());
 		}
 		return atoms;
 	}
 
 	private List<String> splitIntoPages(List<String> atoms) {
 		List<String> pages = new ArrayList<String>();
 		StringBuilder currentPage = new StringBuilder();
 		for (String atom : atoms) {
 			if (currentPage.length() + atom.length() <= maxPageSizeInBytes) {
 				currentPage.append(atom);
 			} else {
 				addNonEmptyPage(pages, currentPage);
 				currentPage = new StringBuilder(atom);
 			}
 		}
 		addNonEmptyPage(pages, currentPage);
 		return pages;
 	}
 
 	private void addNonEmptyPage(List<String> pages, StringBuilder currentPage) {
 		if (currentPage.length() > 0) {
 			pages.add(formatHtml(currentPage.toString()));
 		}
 	}
 
 	private String formatHtml(String html) {
 		// TODO improve, this is ugly
 		return html.replace(">", ">\n");
 	}
 }
