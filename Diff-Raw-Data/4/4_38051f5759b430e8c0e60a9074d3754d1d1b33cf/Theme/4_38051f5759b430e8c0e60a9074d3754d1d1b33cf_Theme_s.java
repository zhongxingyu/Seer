 package info.micdm.ftr;
 
 import info.micdm.ftr.async.DownloadThemePageTask;
 import info.micdm.ftr.async.TaskManager;
 import info.micdm.ftr.utils.DateUtils;
 import info.micdm.ftr.utils.Log;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 /**
  * Класс темы.
  * @author Mic, 2011
  *
  */
 public class Theme {
 
 	public interface OnPageLoadedCommand {
 		public void callback(ThemePage page);
 	}
 	
 	interface OnPageCountLoadedCommand {
 		public void callback(Integer pageCount);
 	}
 	
 	/**
 	 * Идентификатор группы, в которой находится тема.
 	 */
 	protected Integer _groupId;
 	
 	/**
 	 * Идентификатор темы.
 	 */
 	protected Integer _id;
 	
 	/**
 	 * Время последнего обновления темы.
 	 */
 	protected Date _updated;
 	
 	/**
 	 * Автор темы.
 	 */
 	protected String _author;
 	
 	/**
 	 * Название темы.
 	 */
 	private String _title;
 
 	/**
 	 * Количество страниц в теме.
 	 */
 	protected Integer _pageCount;
 	
 	/**
 	 * Загруженные страницы.
 	 */
	protected ArrayList<ThemePage> _pages = new ArrayList<ThemePage>();
 	
 	public Theme(Integer groupId, Integer id, Date updated, String author, String title) {
 		_groupId = groupId;
 		_id = id;
 		_updated = updated;
 		_author = author;
 		_title = title;
 	}
 
 	@Override
 	public String toString() {
 		return _author + ", " + getUpdatedAsString() + "\n" + getTitle();
 	}
 	
 	/**
 	 * Возвращает идентификатор группы.
 	 */
 	public Integer getGroupId() {
 		return _groupId;
 	}
 	
 	/**
 	 * Возвращает идентификатор темы.
 	 */
 	public Integer getId() {
 		return _id;
 	}
 	
 	/**
 	 * Возвращает дату последнего обновления в виде человекопонятной строки.
 	 */
 	public String getUpdatedAsString() {
 		return DateUtils.getRelativeTimeAsString(_updated).toString();
 	}
 	
 	/**
 	 * Возвращает автора темы.
 	 */
 	public String getAuthor() {
 		return _author;
 	}
 	
 	/**
 	 * Возвращает заголовок темы.
 	 */
 	public String getTitle() {
 		return _title;
 	}
 	
 	/**
 	 * Возвращает количество страниц.
 	 */
 	public Integer getPageCount() {
 		return _pageCount;
 	}
 	
 	/**
 	 * Загружает количество страниц.
 	 */
 	protected void _loadPageCount(TaskManager taskManager, final OnPageCountLoadedCommand onLoaded) {
 		Log.debug("loading page count");
 		DownloadThemePageTask task = new DownloadThemePageTask("Загружается количество страниц", this, 0);
 		taskManager.run(task, new TaskManager.OnTaskFinished() {
 			@Override
 			public void callback(Object result) {
 				DownloadThemePageTask.Result loaded = (DownloadThemePageTask.Result)result;
 				_pageCount = loaded.getPageCount();
 				Log.debug("page count loaded: " + _pageCount);
 				if (_pageCount == 1) {
 					_pages.add(loaded.getPage());
 				}
 				onLoaded.callback(_pageCount);
 			}
 		});
 	}
 
 	/**
 	 * Загружает страницу с указанным номером.
 	 */
 	protected void _loadPage(TaskManager taskManager, Integer pageNumber, final OnPageLoadedCommand onLoaded) {
 		Log.debug("loading page #" + pageNumber);
 		DownloadThemePageTask task = new DownloadThemePageTask("Загружается страница №" + (pageNumber + 1), this, pageNumber);
 		taskManager.run(task, new TaskManager.OnTaskFinished() {
 			@Override
 			public void callback(Object result) {
 				DownloadThemePageTask.Result loaded = (DownloadThemePageTask.Result)result;
 				_pageCount = loaded.getPageCount();
 				ThemePage page = loaded.getPage();
 				_pages.add(page);
 				onLoaded.callback(page);
 			}
 		});
 	}
 	
 	/**
 	 * Загружает первую страницу.
 	 */
 	public void loadFirstPage(final TaskManager taskManager, final OnPageLoadedCommand onLoaded) {
 		if (_pageCount == null) {
 			_loadPageCount(taskManager, new OnPageCountLoadedCommand() {
 				@Override
 				public void callback(Integer pageCount) {
 					if (pageCount == 1) {
 						onLoaded.callback(_pages.get(0));
 					} else {
 						_loadPage(taskManager, 0, onLoaded);
 					}
 				}
 			});
 		} else {
 			_loadPage(taskManager, 0, onLoaded);
 		}
 	}
 	
 	/**
 	 * Загружает следующую страницу.
 	 */
 	public void loadNextPage(final TaskManager taskManager, final OnPageLoadedCommand onLoaded) {
 		Integer nextPageNumber = _pages.size();
 		if (nextPageNumber < _pageCount) {
 			_loadPage(taskManager, nextPageNumber, onLoaded);
 		}
 	}
 }
