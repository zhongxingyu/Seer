 package nl.minicom.evenexus.inventory;
 
 import java.math.BigInteger;
 import java.util.List;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.RejectedExecutionHandler;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import javax.inject.Inject;
 import javax.inject.Provider;
 import javax.inject.Singleton;
 
 import nl.minicom.evenexus.eveapi.ApiParser.Api;
 import nl.minicom.evenexus.eveapi.importers.ImportListener;
 import nl.minicom.evenexus.eveapi.importers.ImportManager;
 import nl.minicom.evenexus.persistence.Database;
 import nl.minicom.evenexus.persistence.interceptor.Transactional;
 
 import org.hibernate.Session;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Singleton
 public class InventoryManager {
 	
 	private static final Logger LOG = LoggerFactory.getLogger(InventoryManager.class);
 	
 	private final Database database;
 	private final Provider<InventoryWorker> workerProvider;
 	private final ThreadPoolExecutor executor;
 	
 	@Inject
 	public InventoryManager(ImportManager importManager, 
 			Provider<InventoryWorker> workerProvider, Database database) {
 		
 		this.workerProvider = workerProvider;
 		this.database = database;
 		this.executor = new ThreadPoolExecutor(5, 10, 5, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(256, true));
 		
 		executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
 			@Override
 			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
 				LOG.warn("InventoryManager did not accept new runnable job!");
 			}
 		});
 		
 		importManager.addListener(Api.CHAR_WALLET_TRANSACTIONS, new ImportListener() {
 			@Override
 			public void onImportComplete() {
 				processUnprocessedTransactions();
 			}
 		});
 	}	
 	
 	public void processUnprocessedTransactions() {
 		LOG.info("Calculating inventory and profits for unprocessed transactions");
 		List<BigInteger> typeIds = queryUnprocessedTypeIds();
 		for (BigInteger typeId : typeIds) {
 			InventoryWorker worker = workerProvider.get();
 			worker.initialize(typeId.longValue());
 			executor.submit(worker);
 		}
 	}
 	
 	@Transactional
 	@SuppressWarnings("unchecked")
 	private List<BigInteger> queryUnprocessedTypeIds() {
 		Session session = database.getCurrentSession();
		String query = "select distinct(t.typeId) from WalletTransaction t where t.price > 0 and t.remaining > 0";
 		return (List<BigInteger>) session.createQuery(query).list();
 	}
 
 }
