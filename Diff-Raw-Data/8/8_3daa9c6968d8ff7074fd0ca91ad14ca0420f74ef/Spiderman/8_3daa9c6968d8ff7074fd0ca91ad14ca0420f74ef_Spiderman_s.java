 package org.eweb4j.spiderman.spider;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.RejectedExecutionHandler;
 import java.util.concurrent.SynchronousQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import org.eweb4j.spiderman.infra.SpiderIOC;
 import org.eweb4j.spiderman.infra.SpiderIOCs;
 import org.eweb4j.spiderman.plugin.BeginPoint;
 import org.eweb4j.spiderman.plugin.DigPoint;
 import org.eweb4j.spiderman.plugin.DoneException;
 import org.eweb4j.spiderman.plugin.DupRemovalPoint;
 import org.eweb4j.spiderman.plugin.EndPoint;
 import org.eweb4j.spiderman.plugin.ExtensionPoint;
 import org.eweb4j.spiderman.plugin.ExtensionPoints;
 import org.eweb4j.spiderman.plugin.FetchPoint;
 import org.eweb4j.spiderman.plugin.ParsePoint;
 import org.eweb4j.spiderman.plugin.PluginManager;
 import org.eweb4j.spiderman.plugin.Point;
 import org.eweb4j.spiderman.plugin.PojoPoint;
 import org.eweb4j.spiderman.plugin.TargetPoint;
 import org.eweb4j.spiderman.plugin.TaskPollPoint;
 import org.eweb4j.spiderman.plugin.TaskPushPoint;
 import org.eweb4j.spiderman.plugin.TaskSortPoint;
 import org.eweb4j.spiderman.task.Task;
 import org.eweb4j.spiderman.task.TaskQueue;
 import org.eweb4j.spiderman.xml.Plugin;
 import org.eweb4j.spiderman.xml.Plugins;
 import org.eweb4j.spiderman.xml.Site;
 import org.eweb4j.spiderman.xml.Target;
 import org.eweb4j.util.CommonUtil;
 import org.eweb4j.util.xml.BeanXMLUtil;
 import org.eweb4j.util.xml.XMLReader;
 import org.eweb4j.util.xml.XMLWriter;
 
 
 public class Spiderman {
 
 	public final SpiderIOC ioc = SpiderIOCs.create();
 	public Boolean isShutdownNow = false;
 	private ExecutorService pool = null;
 	private Collection<Site> sites = null;
 	private SpiderListener listener = null;
 	
 	private boolean isSchedule = false;
 	private Timer timer = new Timer();
 	private String scheduleTime = "1h";
 	private String scheduleDelay = "1m";
 	private int scheduleTimes = 0;
 	private int maxScheduleTimes = 0;
 	
 	public final static Spiderman me() {
 		return new Spiderman();
 	}
 	
 	/**
 	 * @date 2013-1-17 下午01:43:52
 	 * @param listener
 	 * @return
 	 */
 	public Spiderman init(SpiderListener listener) {
 		return listen(listener).init();
 	}
 	
 	public Spiderman init(){
 		if (this.listener == null)
 			this.listener = new SpiderListenerAdaptor();
 		isShutdownNow = false;
 		sites = null;
 		pool = null;
 		try {
 			loadPlugins();
 			initSites();
 			initPool();
 		} catch (Exception e){
 			listener.onInfo(Thread.currentThread(),null, "Spiderman init error.");
 			listener.onError(Thread.currentThread(), null, "Spiderman init error.", e);
 		}
 		return this;
 	}
 	
 	public Spiderman listen(SpiderListener listener){
 		this.listener = listener;
 		return this;
 	}
 	
 	public Spiderman startup() {
 		if (isSchedule) {
 			final Spiderman _this = this;
 			timer.schedule(new TimerTask() {
 				public void run() {
 					//限制schedule的次数
 					if (_this.maxScheduleTimes > 0 && _this.scheduleTimes >= _this.maxScheduleTimes){
 						_this.cancel();
 						_this.listener.onInfo(Thread.currentThread(), null, "Spiderman has completed and cancel the schedule.");
 						_this.isSchedule = false;
 					} else {
 						//阻塞，判断之前所有的网站是否都已经停止完全
 						//加个超时
 						long start = System.currentTimeMillis();
 						long timeout = 1*60*1000;
 						while (true) {
 							try {
 								if ((System.currentTimeMillis() - start) > timeout){
 									_this.listener.onError(Thread.currentThread(), null, "timeout of restart blocking check...", new Exception());
 									for (Site site : _this.sites) {
 										if (!site.isStop){
 											site.destroy(_this.listener, _this.isShutdownNow);
 										}
 									}
 									break;
 								}
 								if (_this.sites == null || _this.sites.isEmpty())
 									break;
 								
 								Thread.sleep(1*1000);
 								boolean canBreak = true;
 								for (Site site : _this.sites) {
 									if (!site.isStop){
 										canBreak = false;
 										_this.listener.onInfo(Thread.currentThread(), null, "can not restart spiderman cause there has running-tasks of this site -> "+site.getName()+"...");
 									}
 								}
 								
 								if (canBreak)
 									break;
 							} catch (Exception e) {
 								_this.listener.onError(Thread.currentThread(), null, "", e);
 								throw new RuntimeException("Spiderman can not schedule", e);
 							}
 						}
 						
 						//只有所有的网站资源都已被释放[特殊情况timeout]完全才重启Spiderman
 						_this.scheduleTimes++;
 						String strTimes = _this.scheduleTimes+"";
 						if (_this.maxScheduleTimes > 0)
 							strTimes += "/"+_this.maxScheduleTimes;
 						
 						_this.listener.onInfo(Thread.currentThread(), null, "Spiderman has scheduled "+strTimes+" times.");
 						_this.init()._startup().keepStrict(scheduleTime);
 					}
 				}
 			}, new Date(), (CommonUtil.toSeconds(scheduleTime).intValue() + CommonUtil.toSeconds(scheduleDelay).intValue())*1000);
 			
 			return this;
 		}
 		
 		return _startup();
 	}
 	
 	private Spiderman _startup(){
 		for (Site site : sites){
 			pool.execute(new Spiderman._Executor(site));
 			listener.onInfo(Thread.currentThread(), null, "spider tasks of site[" + site.getName() + "] start... ");
 		}
 		return this;
 	}
 	
 	public void shutdown(){
 		if (sites != null) {
 			for (Site site : sites){
 				site.destroy(listener, false);
 				listener.onInfo(Thread.currentThread(), null, "Site[" + site.getName() + "] destroy... ");
 			}
 		}
 		if (pool != null)
 			pool.shutdown();
 	}
 	
 	public void shutdownNow(){
 		if (sites != null) {
 			for (Site site : sites){
 				site.destroy(listener, true);
 				listener.onInfo(Thread.currentThread(), null, "Site[" + site.getName() + "] destroy... ");
 			}
 		}
 		
 		if (pool != null)
 			pool.shutdownNow();
 		
 		isShutdownNow = true;
 	}
 	
 	//-------- Schedule ------------
 	
 	public Spiderman blocking(){
 		while (isSchedule){
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return this;
 	}
 	
 	public Spiderman schedule(){
 		return schedule(null);
 	}
 	
 	public Spiderman schedule(String time){
 		if (time != null && time.trim().length() > 0)
 			this.scheduleTime = time;
 		this.isSchedule = true;
 		return this;
 	}
 	
 	public Spiderman delay(String delay){
 		if (delay != null && delay.trim().length() > 0)
 			this.scheduleDelay = delay;
 		return this;
 	}
 	
 	public Spiderman times(int maxTimes){
 		if (maxTimes > 0)
 			this.maxScheduleTimes = maxTimes;
 		return this;
 	}
 	
 	public Spiderman cancel(){
 		this.timer.cancel();
 		timer = new Timer();
 		return this;
 	}
 	//------------------------------
 	
 	public Spiderman keepStrict(String time){
 		return keepStrict(CommonUtil.toSeconds(time).longValue()*1000);
 	}
 	
 	public Spiderman keepStrict(long time){
 		try {
 			Thread.sleep(time);
 		} catch (InterruptedException e) {
 		}
 		shutdownNow();
 		return this;
 	}
 	
 	public Spiderman keep(String time){
 		return keep(CommonUtil.toSeconds(time).longValue()*1000);
 	}
 	
 	public Spiderman keep(long time){
 		try {
 			Thread.sleep(time);
 		} catch (InterruptedException e) {
 		}
 		shutdown();
 		return this;
 	}
 	
 	private void loadPlugins() throws Exception{
 		File siteFolder = new File(Settings.website_xml_folder());
 		if (!siteFolder.exists())
 			throw new Exception("can not found WebSites folder -> " + siteFolder.getAbsolutePath());
 		
 		if (!siteFolder.isDirectory())
 			throw new Exception("WebSites -> " + siteFolder.getAbsolutePath() + " must be folder !");
 		
 		File[] files = siteFolder.listFiles();
 		if (files == null || files.length == 0){
 			//generate a site.xml file
 			File file = new File(siteFolder.getAbsoluteFile()+File.separator+"_site_sample_.xml");
 			Site site = new Site();
 			
 			Plugins plugins = new Plugins();
 			plugins.getPlugin().add(PluginManager.createPlugin());
 			site.setPlugins(plugins);
 			
 			XMLWriter writer = BeanXMLUtil.getBeanXMLWriter(file, site);
 			writer.setBeanName("site");
 			writer.setClass("site", Site.class);
 			writer.write();
 		}
 		
 		sites = new ArrayList<Site>(files.length);
 		for (File file : files){
 			if (!file.exists())
 				continue;
 			if (!file.isFile())
 				continue;
 			if (!file.getName().endsWith(".xml"))
 				continue;
 			XMLReader reader = BeanXMLUtil.getBeanXMLReader(file);
 			reader.setBeanName("site");
 			reader.setClass("site", Site.class);
 			Site site = reader.readOne();
 			if (site == null)
 				throw new Exception("site xml file error -> " + file.getAbsolutePath());
 			if ("1".equals(site.getEnable())){
 				sites.add(site);
 			}
 		}
 	}
 	
 	private void initSites() throws Exception{
 		for (Site site : sites){
 			if (site.getName() == null || site.getName().trim().length() == 0)
 				throw new Exception("site name required");
 			if (site.getUrl() == null || site.getUrl().trim().length() == 0)
 				throw new Exception("site url required");
 			if (site.getTargets() == null || site.getTargets().getTarget().isEmpty())
 				throw new Exception("site target required");
 			
 			List<Target> targets = site.getTargets().getTarget();
 			if (targets == null || targets.isEmpty())
 				throw new Exception("can not get any url target of site -> " + site.getName());
 			
 			//---------------------插件初始化开始----------------------------
 			listener.onInfo(Thread.currentThread(), null, "plugins loading begin...");
 			Collection<Plugin> plugins = site.getPlugins().getPlugin();
 			//加载网站插件配置
 			try {
 				PluginManager pluginMgr = new PluginManager();
 				pluginMgr.loadPluginConf(plugins, listener);
 				
 				//加载TaskPoll扩展点实现类
 				ExtensionPoint<TaskPollPoint> taskPollPoint = pluginMgr.getExtensionPoint(ExtensionPoints.task_poll);
 				if (taskPollPoint != null) {
 					site.taskPollPointImpls = taskPollPoint.getExtensions();
 					firstInitPoint(site.taskPollPointImpls, site, listener);
 				}
 				
 				//加载Begin扩展点实现类
 				ExtensionPoint<BeginPoint> beginPoint = pluginMgr.getExtensionPoint(ExtensionPoints.begin);
 				if (beginPoint != null){
 					site.beginPointImpls = beginPoint.getExtensions();
 					firstInitPoint(site.beginPointImpls, site, listener);
 				}
 				
 				//加载Fetch扩展点实现类
 				ExtensionPoint<FetchPoint> fetchPoint = pluginMgr.getExtensionPoint(ExtensionPoints.fetch);
 				if (fetchPoint != null){
 					site.fetchPointImpls = fetchPoint.getExtensions();
 					firstInitPoint(site.fetchPointImpls, site, listener);
 				}
 				
 				//加载Dig扩展点实现类
 				ExtensionPoint<DigPoint> digPoint = pluginMgr.getExtensionPoint(ExtensionPoints.dig);
 				if (digPoint != null){
 					site.digPointImpls = digPoint.getExtensions();
 					firstInitPoint(site.digPointImpls, site, listener);
 				}
 				
 				//加载DupRemoval扩展点实现类
 				ExtensionPoint<DupRemovalPoint> dupRemovalPoint = pluginMgr.getExtensionPoint(ExtensionPoints.dup_removal);
 				if (dupRemovalPoint != null){
 					site.dupRemovalPointImpls = dupRemovalPoint.getExtensions();
 					firstInitPoint(site.dupRemovalPointImpls, site, listener);
 				}
 				//加载TaskSort扩展点实现类
 				ExtensionPoint<TaskSortPoint> taskSortPoint = pluginMgr.getExtensionPoint(ExtensionPoints.task_sort);
 				if (taskSortPoint != null){
 					site.taskSortPointImpls = taskSortPoint.getExtensions();
 					firstInitPoint(site.taskSortPointImpls, site, listener);
 				}
 				
 				//加载TaskPush扩展点实现类
 				ExtensionPoint<TaskPushPoint> taskPushPoint = pluginMgr.getExtensionPoint(ExtensionPoints.task_push);
 				if (taskPushPoint != null){
 					site.taskPushPointImpls = taskPushPoint.getExtensions();
 					firstInitPoint(site.taskPushPointImpls, site, listener);
 				}
 				
 				//加载Target扩展点实现类
 				ExtensionPoint<TargetPoint> targetPoint = pluginMgr.getExtensionPoint(ExtensionPoints.target);
 				if (targetPoint != null){
 					site.targetPointImpls = targetPoint.getExtensions();
 					firstInitPoint(site.targetPointImpls, site, listener);
 				}
 				
 				//加载Parse扩展点实现类
 				ExtensionPoint<ParsePoint> parsePoint = pluginMgr.getExtensionPoint(ExtensionPoints.parse);
 				if (parsePoint != null){
 					site.parsePointImpls = parsePoint.getExtensions();
 					firstInitPoint(site.parsePointImpls, site, listener);
 				}
 				
 				//加载Pojo扩展点实现类
 				ExtensionPoint<PojoPoint> pojoPoint = pluginMgr.getExtensionPoint(ExtensionPoints.pojo);
 				if (pojoPoint != null){
 					site.pojoPointImpls = pojoPoint.getExtensions();
 					firstInitPoint(site.pojoPointImpls, site, listener);
 				}
 				
 				//加载End扩展点实现类
 				ExtensionPoint<EndPoint> endPoint = pluginMgr.getExtensionPoint(ExtensionPoints.end);
 				if (endPoint != null){
 					site.endPointImpls = endPoint.getExtensions();
 					firstInitPoint(site.endPointImpls, site, listener);
 				}
 			//---------------------------插件初始化完毕----------------------------------
 			} catch(Exception e){
 				throw new Exception("Site["+site.getName()+"] loading plugins fail", e);
 			}
 			
 			//初始化网站的队列容器
 			site.queue = new TaskQueue();
 			site.queue.init();
 			//初始化网站目标Model计数器
 			site.counter = new Counter();
 		}
 	}
 	
 	private void firstInitPoint(Collection<? extends Point> points, Site site, SpiderListener listener){
 		for (Point point : points){
 			point.init(site, listener);
 		}
 	}
 	
 	private void initPool(){
 		if (pool == null){
 			int size = sites.size();
 			if (size == 0)
 				throw new RuntimeException("there is no website to fetch...");
 			pool = new ThreadPoolExecutor(size, size,
                     60L, TimeUnit.SECONDS,
                     new LinkedBlockingQueue<Runnable>());
 			
 			listener.onInfo(Thread.currentThread(), null, "init thread pool size->"+size+" success ");
 		}
 	}
 	
 	private class _Executor implements Runnable{
 		private Site site = null;
 		
 		public _Executor(Site site){
 			this.site = site;
 			String strSize = site.getThread();
 			int size = Integer.parseInt(strSize);
 			listener.onInfo(Thread.currentThread(), null, "site thread size -> " + size);
 			RejectedExecutionHandler rejectedHandler = new RejectedExecutionHandler() {
 				public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
 					//拿到被弹出来的爬虫引用
 					Spider spider = (Spider)r;
 					try {
 						//将该爬虫的任务 task 放回队列
 						spider.pushTask(Arrays.asList(spider.task));
 						String info = "repush the task->"+spider.task+" to the Queue.";
 						spider.listener.onError(Thread.currentThread(), spider.task, info, new Exception(info));
 					} catch (Exception e) {
 						String err = "could not repush the task to the Queue. cause -> " + e.toString();
 						spider.listener.onError(Thread.currentThread(), spider.task, err, e);
 					}
 				}
 			};
 			
 			if (size > 0)
 				this.site.pool = new ThreadPoolExecutor(size, size,
 						60L, TimeUnit.SECONDS,
 	                    new LinkedBlockingQueue<Runnable>(),
 	                    rejectedHandler);
 			else
 				this.site.pool = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
 	                    60L, TimeUnit.SECONDS,
 	                    new SynchronousQueue<Runnable>(),
 	                    rejectedHandler);
 		}
 		
 		public void run() {
 			// 运行种子任务
 			Task feedTask = new Task(new String(this.site.getUrl()), this.site, 10);
 			Spider feedSpider = new Spider();
 			feedSpider.init(feedTask, listener);
 			feedSpider.run();
 			
 			final float times = CommonUtil.toSeconds(this.site.getSchedule()) * 1000;
 			long start = System.currentTimeMillis();
 			while(true){
 				
 				try {
 					//扩展点：TaskPoll
 					Task task = null;
 					Collection<TaskPollPoint> taskPollPoints = site.taskPollPointImpls;
 					if (taskPollPoints != null && !taskPollPoints.isEmpty()){
 						for (TaskPollPoint point : taskPollPoints){
 							task = point.pollTask();
 						}
 					}
 					
 					if (task == null){
 						long wait = CommonUtil.toSeconds(site.getWaitQueue()).longValue();
 //						listener.onInfo(Thread.currentThread(), null, "queue empty wait for -> " + wait + " seconds");
 						if (wait > 0) {
 							try {
 								Thread.sleep(wait * 1000);
 							} catch (Exception e){
 								
 							}
 						}
 						continue;
 					}
 					
 					Spider spider = new Spider();
 					spider.init(task, listener);
 					
 					this.site.pool.execute(spider);
 				}catch (DoneException e) {
 					listener.onInfo(Thread.currentThread(), null, e.toString());
 					return ;
 				} catch (Exception e) {
 					listener.onError(Thread.currentThread(), null, e.toString(), e);
 				}finally{
 					long cost = System.currentTimeMillis() - start;
 					if (cost >= times){ 
 						// 运行种子任务
 						feedSpider.run();
 						listener.onInfo(Thread.currentThread(), null, " shcedule FeedSpider of Site->"+site.getName()+" per "+times+", now cost time ->"+cost);
 						start = System.currentTimeMillis();//重新计时
 					}
 				}
 			}
 		}
 	} 
 	
 }
