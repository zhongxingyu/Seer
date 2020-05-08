 package at.edu.hti.concurrency.prodcon;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
 
 public class ProducerConsumerWorker implements ProducerConsumerStarter {
 
 	private Configuration config;
 
 	private LinkedBlockingQueue<String> queue;
 
 	private List<Consumer> consumers;
 	private List<Producer> producers;
 
 	private static Integer counter = 0;
 
 	@Override
 	public void setUpTest(Configuration configuration) {
 		this.config = configuration;
 		queue = new LinkedBlockingQueue<String>(config.getBufferSize());
 		consumers = new ArrayList<Consumer>(config.getNumberOfConsumers());
 		producers = new ArrayList<Producer>(config.getNumberOfProcuders());
 
 		for (int i = 0; i < config.getNumberOfConsumers(); i++) {
 			consumers.add(new Consumer("CONS-SPANNI-HERFERT-SCHMITT"+i));
 		}
 
 		for (int i = 0; i < config.getNumberOfProcuders(); i++) {
 			producers.add(new Producer("PROD-SPANNI-HERFERT-SCHMITT"+i));
 		}
 
 	}
 
 	@Override
 	public long runProducerConsumerTest() {
 		for (Producer producer : producers) {
 			new Thread(producer).start();
 		}
 
 		for (Consumer consumer : consumers) {
 			new Thread(consumer).start();
 		}
 
 		return 0;
 	}
 
 	private class Consumer implements Runnable {
 
 		private String name;
 
 		public Consumer(String name) {
 			this.name = name;
 		}
 
 		@Override
 		public void run() {
 			while (true) {
 				try {
					String element = queue.poll(100, TimeUnit.MILLISECONDS);
					if (element == null){
						return;
					}
 					System.out.println(this + "=" + element);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		@Override
 		public String toString() {
 			return name;
 		}
 	}
 
 	private class Producer implements Runnable {
 
 		private String name;
 
 		public Producer(String name) {
 			this.name = name;
 		}
 
 		@Override
 		public void run() {
 			while (true) {
 				int myThreadCounter = 0;
 				synchronized (counter) {
 					if (counter == config.getNumberOfItems()) {
 						System.out.println(this+" is fertig!");
 						return;
 					} else {
 						myThreadCounter = counter;
 						counter++;
 					}
 				}
 				try {
 					queue.put("ITEM" + myThreadCounter);
 					System.out.println(this + "=" + "ITEM" + myThreadCounter);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		@Override
 		public String toString() {
 			return name;
 		}
 
 	}
 
 	public static void main(String[] args) {
 		ProducerConsumerStarter prodCom = new ProducerConsumerWorker();
 		prodCom.setUpTest(new Configuration());
 		prodCom.runProducerConsumerTest();
 	}
 
 }
