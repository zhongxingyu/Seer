 package ru.xrm.app;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
import org.hibernate.NonUniqueObjectException;
 import org.hibernate.Session;
 
 import ru.xrm.app.config.Config;
 import ru.xrm.app.domain.Section;
 import ru.xrm.app.domain.Vacancy;
 import ru.xrm.app.httpclient.CachingHttpFetcher;
 import ru.xrm.app.httpclient.UrlHelper;
 import ru.xrm.app.misc.HibernateUtil;
 import ru.xrm.app.misc.VacancyPage;
 import ru.xrm.app.parsers.VacancyListOnePageParser;
 import ru.xrm.app.parsers.VacancySectionParser;
 import ru.xrm.app.threads.OnePageWorker;
 
 public class App 
 {
 	static final int THREAD_NUMBER = 2;
 	static final String ENCODING = "windows-1251";
 
 	public static void main( String[] args )
 	{
 		System.out.println(java.lang.Runtime.getRuntime().maxMemory()); 
 
 		UrlHelper urlHelper= UrlHelper.getInstance();
 		CachingHttpFetcher cf;
 		Config config=new Config();
 		try{
 			config.load("config.xml");
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 
 		String homePage="http://e1.ru/business/job";
 
 		String basename=urlHelper.getBasename(homePage);
 
 		cf=CachingHttpFetcher.getInstance();
 
 		// home page
 		String content = cf.fetchWithRetry(homePage,ENCODING,1000);
 
 		VacancySectionParser vsp=new VacancySectionParser(config, content);
 		List<Section> sections=vsp.parse();
 
 		Date d1=new Date();
 		ExecutorService executorService=Executors.newFixedThreadPool(THREAD_NUMBER);
 
 		List<Future<List<Vacancy>>> allVacancyListParts=new LinkedList<Future<List<Vacancy>>>();
 
 		// for all sections
 		for (Section section:sections){
 			System.out.format("\n*** NEW SECTION: %s ***\n",section.getName());
 			String vacancyListCurrentPageUrl=section.getHref();
 
 			vacancyListCurrentPageUrl=urlHelper.constructAbsoluteUrl(vacancyListCurrentPageUrl, basename);
 
 			// get first page of vacancies
 			content=cf.fetchWithRetry(vacancyListCurrentPageUrl,ENCODING,1000);
 			VacancyListOnePageParser onePageParser=new VacancyListOnePageParser(config, content);
 			// get pages except current, link to first page we'll get from other pages
 			List<VacancyPage> pages=onePageParser.getPages();		
 			// if we have just one page, then getPages returns zero size list
 			if (pages.size()==0){
 				System.err.println("there is ONE page per Section");
 				pages.add(new VacancyPage(vacancyListCurrentPageUrl));
 			}
 
 			List<VacancyPage> allSectionPages=new ArrayList<VacancyPage>(pages);
 
 			Deque<VacancyPage> pageQueue = new LinkedList<VacancyPage>();
 			// push all pages to pageQueue
 			for (VacancyPage p:pages){
 				pageQueue.addLast(p);
 			}
 
 			int pageCounter=0;
 
 			// loop through all pages
 			while(true){
 				if (pageQueue.isEmpty())break;
 				VacancyPage p=pageQueue.poll();
 				String vacancyNextPageUrl=p.getHref();
 				vacancyNextPageUrl=urlHelper.constructAbsoluteUrl(vacancyNextPageUrl, basename);
 
 				System.out.format("%d ",pageCounter);
 				// try to add some new pages
 				content=cf.fetchWithRetry(vacancyNextPageUrl, ENCODING, 1000);
 				if (content==null){
 					System.err.format("Cannot download page %s adding it to end of queue\n",vacancyNextPageUrl);
 					pageQueue.addLast(p);
 					continue;
 				}
 
 				onePageParser.setHtml(content);
 				List<VacancyPage> newPages=onePageParser.getPages();
 				for (VacancyPage newPage:newPages){
 					boolean exists=false;
 					for (VacancyPage ap:allSectionPages){
 						if (ap.getHref().equals(newPage.getHref())){
 							exists=true;
 						}
 					}
 
 					if (!exists){
 						allSectionPages.add(newPage);
 						pageQueue.addLast(newPage);
 					}
 				}
 
 				// FIXME: should be vacancyNextPageUrl not vacancyListCurrentPageUrl
 				Future<List<Vacancy>> partVacancyList=executorService.submit(new OnePageWorker(config, vacancyListCurrentPageUrl, basename, ENCODING, cf));
 				allVacancyListParts.add(partVacancyList);
 
 				pageCounter++;
 				if (pageCounter%30==0){
 					System.out.println();
 				}
 				break; // FIXME
 			}// loop pages
 			System.out.println();
 			//break; // FIME
 		}// loop sections
 
 
 		System.out.format("<<< Done filling pages >>>\n");
 
 		// Wait for all threads ends
 		boolean allReady=false;
 
 		int threadCounter;
 
 		do{
 			threadCounter=0;
 			allReady=true;
 			for (Future<List<Vacancy>> f:allVacancyListParts){
 				if (!f.isDone()){
 					allReady=false;
 					threadCounter++;
 				}
 			}
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			System.out.format("remaining tasks: %d \n", threadCounter);
 		}while(!allReady);
 
 		List<Vacancy> allVacancies=new LinkedList<Vacancy>();
 		for (Future<List<Vacancy>> f:allVacancyListParts){
 			try {
 				List<Vacancy> lv=f.get();
 				System.out.print("[");
 				for (int i=0;i<lv.size();i++){
 					System.out.format("%d ",lv.get(i).getId());
 				}
 				System.out.println("]");
 				allVacancies.addAll(lv);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} catch (ExecutionException e) {
 				e.printStackTrace();
 			}
 		}
 		System.out.format("Total vacancies: %s\n", allVacancies.size());
 
 		System.out.format("Vacancy and count\n");
 		Map<String,Integer> stats=new HashMap<String,Integer>();
 		for (Vacancy v:allVacancies){
 			//System.out.print(v);
 			if (v.getSection()==null){
 				System.err.format("There is empty section for vacancy\n");
 				System.err.println(v);
 			}else{
 				String key=v.getSection().getName();
 				Integer value;
 				if (!stats.containsKey(key)){
 					value=1;
 				}else{
 					value=stats.get(key)+1;
 				}
 				stats.put(key, value);
 			}
 		}
 
 		for(String vacancy:stats.keySet()){
 			System.out.format("%s \t %s\n",vacancy,stats.get(vacancy));
 		}
 
 		executorService.shutdown();
 		
 		Date d2=new Date();
 		System.out.format("\n %d ms. \n",d2.getTime()-d1.getTime());
 		System.out.println("Done Loading!");
 		
 		d1=new Date();
 		System.out.println("Start storing to database");
 		Session session=HibernateUtil.getSessionFactory().getCurrentSession();
 
 		// get stored section, delete them
 		session.beginTransaction();
 		List<Section> allStoredSection = session.createQuery("from Section").list();
 		List<Vacancy> allStoredVacancies = session.createQuery("from Vacancy").list();
 		allStoredSection.clear();
 		allStoredVacancies.clear();
 		session.getTransaction().commit();
 
 		session=HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		for (Section s:sections){
 			session.saveOrUpdate(s);
 		}
 		int c=0;
 		int dups=0;
 		int upds=0;
 		Set<Vacancy> vacs=new HashSet<Vacancy>();
 		for (Vacancy v:allVacancies){
 			c++;
 			
 			if (vacs.contains(v)){
 				System.out.format("Duplicate id %s\n",v.getId());
 				dups++;
 				continue;
 			}
 			else{
 				upds++;
 				session.saveOrUpdate(v);
 				vacs.add(v);
 			}
 			
 			if (c%20==0){
 				session.flush();
 				session.clear();
 			}
 		}
 		session.getTransaction().commit();
 		System.out.println("Done!");
 		d2=new Date();
 		System.out.format("\n %d ms. \n",d2.getTime()-d1.getTime());
 		System.out.format("Duplicates %d Updates %d\n", dups, upds);
 	}
 }
