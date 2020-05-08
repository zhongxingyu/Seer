 
 import java.util.Collection;
 import java.util.Map;
 
 import org.eweb4j.config.EWeb4JConfig;
 import org.eweb4j.config.Log;
 import org.eweb4j.config.LogFactory;
 import org.eweb4j.spiderman.fetcher.Page;
 import org.eweb4j.spiderman.spider.SpiderListener;
 import org.eweb4j.spiderman.spider.Spiderman;
 import org.eweb4j.spiderman.task.Task;
 import org.eweb4j.util.CommonUtil;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class TestSpider {
 	private final static Log log = LogFactory.getConfigLogger(TestSpider.class);
 	@BeforeClass
 	public static void prepare() throws Exception {
 		String err = EWeb4JConfig.start();
 		if (err != null)
 			throw new Exception(err);
 		
 	}
 	@Test
 	public void test() throws Exception {
 		Spiderman.init(new SpiderListener() {
 			public void onNewUrls(Thread thread, Task task, Collection<String> newUrls) {
 //				log.debug("newUrls->"+newUrls);
 			}
 			public void onDupRemoval(Thread currentThread, Task task, Collection<Task> validTasks) {
 				
 			}
 			public void onNewTasks(Thread thread, Task task, Collection<Task> newTasks) {
 				
 			}
 			public void onTargetPage(Thread thread, Task task, Page page) {
 				
 			}
 			public void onParse(Thread thread, Task task, Map<String, Object> model, int count) {
 				log.debug("get target model["+count+"] -> " + CommonUtil.toJson(model));
 			}
 			public void onInfo(Thread thread, String info) {
 //				log.debug(info);
 			}
 			public void onError(Thread thread, String err, Exception e) {
 				e.printStackTrace();
 			}
 		});
 
 		// 启动爬虫
 		Spiderman.start();
		//运行10s
		Thread.sleep(CommonUtil.toSeconds("10s").longValue()*1000);
 		// 关闭爬虫
 		Spiderman.stop();
 	}
 }
