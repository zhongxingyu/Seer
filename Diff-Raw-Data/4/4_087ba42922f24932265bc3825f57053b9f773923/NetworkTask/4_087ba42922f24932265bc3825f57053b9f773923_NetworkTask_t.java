 package network;
 
 import interfaces.MediatorNetwork;
 import interfaces.Network;
 import interfaces.NetworkTransfer;
 
 import java.util.Iterator;
 import java.util.List;
 
 import javax.swing.SwingWorker;
 
 import data.Service;
 import data.Service.Status;
 
 public class NetworkTask extends SwingWorker<Service, Service> {
 	private MediatorNetwork med;
 	private Service service;
 
 	public NetworkTask(MediatorNetwork med, Service service) {
 		this.med = med;
 		this.service = service;
 		
 		service.setStatus(Status.TRANSFER_IN_PROGRESS);
 	}
 
 	@Override
 	protected Service doInBackground() throws Exception {
 		System.out.println(Thread.currentThread());
 		
		int DELAY = 100;
		int count = 100;
 		int i = 0;
 		try {
 			while (i < count) {
 				i++;
 				Thread.sleep(DELAY);
 				service.setProgress(i);
 				
 				publish(service);
 			}
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		return service;
 	}
 
 	protected void process(List<Service> services) {
 		System.out.println(Thread.currentThread());
 
 		for (Service service:  services) {
 			med.transferProgressNotify(service);
 		}
 	}
 
 	@Override
 	protected void done() {
 		System.out.println(Thread.currentThread());
 		if (isCancelled()) {
 			System.out.println("Cancelled !");
 			service.setStatus(Status.TRANSFER_FAILED);
 		} else {
 			System.out.println("Done !");
 			service.setStatus(Status.TRANSFER_COMPLETE);
 		}
 	}
 }
