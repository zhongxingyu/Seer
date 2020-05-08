 package org.cotrix.web.client.view;
 
 import java.util.List;
 
 import org.cotrix.web.shared.UINews;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.DivElement;
 import com.google.gwt.dom.client.Style.Display;
 import com.google.gwt.i18n.shared.DateTimeFormat;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class HomeViewImpl extends Composite implements HomeView {
 
 	private static HomeUiBinder uiBinder = GWT.create(HomeUiBinder.class);
 	protected static DateTimeFormat SDF = DateTimeFormat.getFormat("d MMM yyyy HH:mm"); 
 
 	@UiTemplate("Home.ui.xml")
 	interface HomeUiBinder extends UiBinder<Widget, HomeViewImpl> {
 	}
 
 	@UiField DivElement statisticsLoader;
 	@UiField HTML statistics;
 	
 	@UiField DivElement newsLoader;
 	@UiField HTML news;
 	@UiField ScrollPanel newsMain;
 
 	public HomeViewImpl() {
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 	
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setStatistics(int codelists, int codes, int users, int repositories) {
 		StringBuilder html = new StringBuilder();
 		html.append("<ul>");
 		addStatistic(html, "Codelists", codelists);
 		addStatistic(html, "Codes", codes);
 		addStatistic(html, "Users", users);
 		addStatistic(html, "Repositories", repositories);
 		html.append("</ul>");
 		statistics.setHTML(html.toString());
 		statisticsLoader.getStyle().setDisplay(Display.NONE);
 		statistics.setVisible(true);
 	}
 	
 	protected void addStatistic(StringBuilder html, String name, int value) {
 		html.append("<li>").append(name).append(": <span style=\"float: right;\">").append(value).append("</span></li>");
 	}
 	
 	@Override
 	public void setNews(List<UINews> newses) {
 		StringBuilder html = new StringBuilder();
 		html.append("<ul>");
 		for (UINews news:newses) addNew(html, news);
 		html.append("</ul>");
 		news.setHTML(html.toString());
 		newsLoader.getStyle().setDisplay(Display.NONE);
 		newsMain.setVisible(true);
 	}
 	
 	protected void addNew(StringBuilder html, UINews news) {
 		if (news.getText() == null) return;
 		html.append("<li>")
 		.append("<span style=\"font-size:12px;line-height: 1.4;display: block;\">").append(SDF.format(news.getTimestamp())).append("</span>")
 		.append("<span style=\"line-height: 1.4;display: block;\">").append(news.getText()).append("</span>")
		.append("<span style=\"height: 8px;display: block;margin-top: 5px;border-top: 1px solid #d9d9d9;\"></span></li>");
 	}
 }
