 package org.cloudsicle.master.allocation;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.cloudsicle.communication.SocketSender;
 import org.cloudsicle.main.jobs.CombineJob;
 import org.cloudsicle.main.jobs.CompressJob;
 import org.cloudsicle.main.jobs.DownloadJob;
 import org.cloudsicle.main.jobs.ForwardJob;
 import org.cloudsicle.main.jobs.IJob;
 import org.cloudsicle.master.Monitor;
 import org.cloudsicle.master.slaves.ResourcePool;
 import org.cloudsicle.master.slaves.SlaveVM;
 import org.cloudsicle.messages.Activity;
 import org.cloudsicle.messages.Allocation;
 import org.cloudsicle.messages.JobMetaData;
 
 import com.jcraft.jsch.JSchException;
 
 public class JobPerVMAllocator extends AbstractAllocator {
 	
 	public JobPerVMAllocator(ResourcePool pool, Monitor monitor) {
 		super(pool, monitor);
 	}
 
 	public void allocate(JobMetaData job){
 		SlaveVM vm = this.pool.requestVM();
 		vm.assignJob(job);
 		Allocation alloc = new Allocation();
 		alloc.allocate(vm, job.getFiles()); 
 		createActivity(job, alloc);					
 	}
 	
 	private void createActivity(JobMetaData meta, Allocation alloc) {
 
 		ArrayList<IJob> list = new ArrayList<IJob>();
 		HashMap<Integer, HashMap<Integer, String>> allocs = alloc.getAllocations();
 
 		for (Integer vmId : allocs.keySet()) {
 			SlaveVM vm = this.pool.getVMById(vmId);
 			
 			SocketSender sender = new SocketSender(true, vm.getIp());
 
 			ArrayList<Integer> filelist = new ArrayList<Integer>();
 			HashMap<Integer, String> files =  allocs.get(vm.getId());
 			filelist.addAll(files.keySet());
 			DownloadJob d = new DownloadJob(filelist, meta.getSender());
 			CombineJob c = new CombineJob(filelist);
 			CompressJob comp = new CompressJob();
 			ForwardJob f = new ForwardJob(true);
 			c.setIP(meta.getSender());
 			comp.setIP(meta.getSender());
 			f.setIP(meta.getSender());
			f.setRemoteFileName(vm.getIp().getHostAddress());
 			list.add(d);
 			list.add(c);
 			list.add(comp);
 			list.add(f);
 			
 			Activity activity = new Activity(list);
 			activity.setClient(meta.getSender());
 			activity.setVM(vm);
 
 			try {
 				System.out.println("Sending Activity to "
 						+ vm.getId() + "@" + vm.getIp().getHostAddress());
 				sender.send(activity, true);
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (JSchException e) {
 				e.printStackTrace();
 			}
 		}
 		monitor.moveJobToRunning(meta.getId());
 	}
 
 }
