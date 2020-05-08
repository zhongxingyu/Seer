 package com.opensajux.view;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.primefaces.model.LazyDataModel;
 import org.primefaces.model.SortOrder;
 
 import com.google.appengine.api.datastore.Text;
 import com.opensajux.common.PaginationParameters;
 import com.opensajux.entity.News;
 import com.opensajux.jsf.context.ViewScoped;
 import com.opensajux.service.NewsService;
 
 @ViewScoped
 @Named
 public class NewsBean implements Serializable {
 	private static final long serialVersionUID = -6774765469039891357L;
 
 	@Inject
 	private NewsService newsService;
 	private LazyDataModel<News> newsModel;
 	private News[] selectedNews;
 	private News[] filteredNews;
 	private String newsTitle = "";
 	private String newsContent = "";
 	private News selected = new News();
 
 	public LazyDataModel<News> getNews() {
 		if (newsModel == null) {
 			newsModel = new LazyDataModel<News>() {
 				private static final long serialVersionUID = 71468727114834068L;
 
 				@Override
 				public int getRowCount() {
 					return newsService.getCount().intValue();
 				}
 
 				@Override
 				public List<News> load(int first, int pageSize, String sortField, SortOrder sortOrder,
 						Map<String, String> filters) {
 					PaginationParameters param = new PaginationParameters();
 					param.setFirst(first);
 					param.setPageSize(pageSize);
					param.setSortField(sortField == null ? "publishDate" : sortField);
					param.setSortOrder(sortOrder == null ? "desc" : sortOrder == SortOrder.ASCENDING ? "asc" : "desc");
 
 					return newsService.getNews(param);
 				}
 
 				@Override
 				public Object getRowKey(News news) {
 					return news.getKey().getId() + "";
 				}
 
 				@Override
 				public News getRowData(String rowKey) {
 					return newsService.getById(rowKey);
 				}
 			};
 			newsModel.setPageSize(5);
 		}
 		return newsModel;
 	}
 
 	/**
 	 * @return the selectedNews
 	 */
 	public News[] getSelectedNews() {
 		return selectedNews;
 	}
 
 	/**
 	 * @param selectedNews
 	 *            the selectedNews to set
 	 */
 	public void setSelectedNews(News[] selectedNews) {
 		this.selectedNews = selectedNews;
 	}
 
 	/**
 	 * @return the filteredNews
 	 */
 	public News[] getFilteredNews() {
 		return filteredNews;
 	}
 
 	/**
 	 * @param filteredNews
 	 *            the filteredNews to set
 	 */
 	public void setFilteredNews(News[] filteredNews) {
 		this.filteredNews = filteredNews;
 	}
 
 	/**
 	 * @return the newsTitle
 	 */
 	public String getNewsTitle() {
 		return newsTitle;
 	}
 
 	/**
 	 * @param newsTitle
 	 *            the newsTitle to set
 	 */
 	public void setNewsTitle(String newsTitle) {
 		this.newsTitle = newsTitle;
 	}
 
 	/**
 	 * @return the newsContent
 	 */
 	public String getNewsContent() {
 		return newsContent;
 	}
 
 	/**
 	 * @param newsContent
 	 *            the newsContent to set
 	 */
 	public void setNewsContent(String newsContent) {
 		this.newsContent = newsContent;
 	}
 
 	public void saveNews() {
 		News news;
 
 		if (selected != null) {
 			news = selected;
 			news.setUpdatedDate(new Date());
 		} else {
 			news = new News();
 			news.setPublishDate(new Date());
 			news.setUpdatedDate(news.getPublishDate());
 		}
 		news.setTitle(newsTitle);
 		news.setContent(new Text(newsContent));
 		newsService.saveNews(news);
 	}
 
 	public void removeNews() {
 		newsService.removeNews(selectedNews);
 		clearFields();
 	}
 
 	public void add() {
 		clearFields();
 	}
 
 	/**
 	 * 
 	 */
 	private void clearFields() {
 		newsTitle = "";
 		newsContent = "";
 		selected = null;
 		selectedNews = null;
 	}
 
 	/**
 	 * @return the selected
 	 */
 	public News getSelected() {
 		return selected;
 	}
 
 	/**
 	 * @param selected
 	 *            the selected to set
 	 */
 	public void setSelected(News selected) {
 		this.selected = selected;
 		newsTitle = selected.getTitle();
 		newsContent = selected.getContent().getValue();
 	}
 }
