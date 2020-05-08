 package virt.vmware.backup;
 
 import java.io.*;
 import java.util.*;
 
 import javax.xml.bind.*;
 
 import virt.vmware.backup.conf.*;
 import virt.vmware.backup.report.*;
 import virt.vmware.vcli.*;
 import virt.vmware.vcli.VIClient.ExportVMResponse;
 
 public final class BackupAgent implements Runnable
 {
 	public static String VERSION = BackupAgent.class.getPackage().getSpecificationVersion() + "." +BackupAgent.class.getPackage().getImplementationVersion();
 	public static String NAME = BackupAgent.class.getPackage().getImplementationTitle();
 	
 	private VIClient vcli;
 	
 	private BackupConfiguration conf;
 	private BackupReport report;
 	//private Set<WorkerThread> workers;
 
 	private boolean rep = false;
 	
 	public BackupAgent(File file) throws Exception
 	{
 		JAXBContext context = JAXBContext.newInstance( BackupConfiguration.class );
 	    Unmarshaller m = context.createUnmarshaller();
 	    conf = (BackupConfiguration) m.unmarshal(new FileInputStream(file));
 	    
 	    report = new BackupReport();
 	    
 	    report.host = conf.host;
 	    report.user = conf.username;
 	    report.date = new java.util.Date();
 	    
 	    //workers = new HashSet<WorkerThread>();
 	}
 	
 	public void setReport(boolean rep)
 	{
 		this.rep = rep;
 	}
 	
 	public boolean isReport()
 	{
 		return rep;
 	}
 	
 	@Override
 	public void run()
 	{
 		long backuptime = System.currentTimeMillis();
 		try
 		{
 			try
 			{
 				try {
 					vcli = new VIClient(conf.protocol + "://" + conf.host + ":" + conf.port + "/sdk");
 				} catch (VIClientException vice) {
 					for(BackupVMTask bckpvm : conf.backupVMTask)
 					{
 						 BackupVMReport rep = new BackupVMReport();
 						 rep.vmName = bckpvm.name;
 						 rep.time = 0;
 						 rep.message = vice.getMessage();
 						 rep.result = Result.FAIL;
 						 report.backupVMReport.add(rep);
 					}
 					report.result = Result.FAIL;
 					throw new Exception(vice.getMessage());
 				}
 				
 				try {
 					vcli.login(conf.username, conf.password);
 				} catch (VIClientException vice) {
 					for(BackupVMTask bckpvm : conf.backupVMTask)
 					{
 						 BackupVMReport rep = new BackupVMReport();
 						 rep.vmName = bckpvm.name;
 						 rep.time = 0;
 						 rep.message = vice.getMessage();
 						 rep.result = Result.FAIL;
 						 report.backupVMReport.add(rep);
 					}
 					report.result = Result.FAIL;
 					throw new Exception(vice.getMessage());
 				}
 				
 				if(conf.mountDatastoreTask != null)
 				{
 					report.mountDatastoreReport = new MountDatastoreReport();
 					long time = System.currentTimeMillis();
 					try
 					{
 						vcli.mount(conf.mountDatastoreTask.server,
 								conf.mountDatastoreTask.path,
 								conf.mountDatastoreTask.name);
 						report.mountDatastoreReport.result = Result.SUCCESS;
 						report.mountDatastoreReport.time = (System.currentTimeMillis() - time) / 1000;
 					} catch (VIClientException vice) {
 						report.mountDatastoreReport.message = vice.getMessage();
 						report.mountDatastoreReport.time = (System.currentTimeMillis() - time) / 1000;
 						vice.printStackTrace();
 						if(conf.mountDatastoreTask.critical)
 						{
 							report.mountDatastoreReport.result = Result.FAIL;
 							for(BackupVMTask bckpvm : conf.backupVMTask)
 							{
 								 BackupVMReport rep = new BackupVMReport();
 								 rep.vmName = bckpvm.name;
 								 rep.time = 0;
 								 rep.message = vice.getMessage();
 								 rep.result = Result.FAIL;
 								 report.backupVMReport.add(rep);
 							}
 							report.result = Result.FAIL;
 							throw new Exception("Failed to mount remote datastore [" + conf.mountDatastoreTask.name + "] " + conf.mountDatastoreTask.server + ":" + conf.mountDatastoreTask.path);
 						} else {
 							report.mountDatastoreReport.result = Result.WARNING;
 						}
 					}
 				}
 
 				//FIXME Run backup operation in parallel (?)
 				/**
 				 * Backup operation run in a serial way
 				 * Using the same URLConnection in parallel from multiple threads break things
 				 */
 //				for(BackupVMTask bckpvm : conf.getBackupTasks())
 //				{
 //					WorkerThread worker = new WorkerThread(bckpvm);
 //					worker.start();
 //					workers.add(worker);
 //				}
 //				
 //				while(true)
 //				{
 //					boolean completed = true;
 //					for(WorkerThread worker : workers)
 //						if(worker.isRunning())
 //						{
 //							completed = false;
 //							break;
 //						}
 //					if(completed)
 //						break;
 //					Thread.sleep(1000);
 //				}
 				
 				for(BackupVMTask bckpvm : conf.backupVMTask)
 				{
 					// Check if the VM is present in the Inventory
 					if(!vcli.findvm(bckpvm.name, bckpvm.uuid))
 					{
 						BackupVMReport vmrep = new BackupVMReport();
 						vmrep.vmName = bckpvm.name;
 						vmrep.result = Result.FAIL;
 						vmrep.message = "Virtual Machine was not found in the Inventory";
 						report.backupVMReport.add(vmrep);
 						report.result = Result.FAIL;
 						continue;
 					}
 					WorkerThread worker = new WorkerThread(bckpvm);
 					worker.start();
 					while(worker.isRunning())
 					{
 						Thread.sleep(1000);
 					}
 					report.backupVMReport.add(worker.getReport());
 				}				
 				
 				if(conf.umountDatastoreTask != null)
 				{
 					report.umountDatastoreReport = new UmountDatastoreReport();
 					long time = System.currentTimeMillis();
 					try
 					{
 						vcli.unmount(conf.umountDatastoreTask.name);
 						report.umountDatastoreReport.result = Result.SUCCESS;
 						report.umountDatastoreReport.time = (System.currentTimeMillis() - time) / 1000;
 					} catch (VIClientException vice) {
 						report.umountDatastoreReport.message = vice.getMessage();
 						report.umountDatastoreReport.time = (System.currentTimeMillis() - time) / 1000;
 						vice.printStackTrace();
 						if(conf.umountDatastoreTask.critical)
 						{
 							report.umountDatastoreReport.result = Result.FAIL;
 							throw new Exception("Failed to umount remote datastore [" + conf.mountDatastoreTask.name + "] " + conf.mountDatastoreTask.server + ":" + conf.mountDatastoreTask.path);
 						} else {
 							report.umountDatastoreReport.result = Result.WARNING;
 						}
 					}
 				}
 	
 				try {
 					vcli.logout();
 				} catch (VIClientException vice) {
 					vice.printStackTrace();
 				}
 				
 			} catch (Exception e) {
 				e.printStackTrace();
 				
 				if(report.result == Result.UNKNOWN)
 					report.result = Result.SUCCESS;
 				for(BackupVMReport bvmr : report.backupVMReport)
 					if(bvmr.result == Result.FAIL)
 						report.result = Result.FAIL;
 				
 				report.criticalErrorReport = new CriticalErrorReport(e.getMessage());
 				
 				try
 				{
 					report.time = (System.currentTimeMillis() - backuptime) / 1000;
 					String xmlfile = "vmbackup-" + new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()) + ".report.xml";
 					
 					JAXBContext context = JAXBContext.newInstance( BackupReport.class );
 					Marshaller m = context.createMarshaller();
 				    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
 				    m.marshal(report ,new FileOutputStream(new File(xmlfile)));
 				    
 				    System.exit(0);
 				} catch (JAXBException jaxbe) {
 					jaxbe.printStackTrace();
 				} catch (FileNotFoundException fnfe) {
 					fnfe.printStackTrace();
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if(report.result == Result.UNKNOWN)
 				report.result = Result.SUCCESS;
 			for(BackupVMReport bvmr : report.backupVMReport)
 				if(bvmr.result == Result.FAIL)
 					report.result = Result.FAIL;
 			try
 			{
 				report.time = (System.currentTimeMillis() - backuptime) / 1000;
 				String xmlfile = "vmbackup-" + new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()) + ".report.xml";
 				
 				JAXBContext context = JAXBContext.newInstance( BackupReport.class );
 				Marshaller m = context.createMarshaller();
 			    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
 			    m.marshal(report ,new FileOutputStream(new File(xmlfile)));
 			    
 			} catch (JAXBException jaxbe) {
 				jaxbe.printStackTrace();
 			} catch (FileNotFoundException fnfe) {
 				fnfe.printStackTrace();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		System.exit(0);
 	}
 	
 	@SuppressWarnings("unused")
 	private final class WorkerThread extends Thread
 	{
 		private BackupVMTask task;
 		private BackupVMReport report = new BackupVMReport();
 		private boolean running = false;
 		private boolean interrupted = false;
 		
 		public WorkerThread(BackupVMTask task)
 		{
 			this.task = task;
 		}
 		
 		public boolean isRunning()
 		{
 			return running;
 		}
 		
 		@Override
 		public synchronized void start() {
 			interrupted = false;
 			running = true;
 			super.start();
 		}
 
 		@Override
 		public void interrupt() {
 			interrupted = true;
 			super.interrupt();
 		}
 
 		@Override
 		public boolean isInterrupted() {
 			return !running;
 		}
 
 		@Override
 		public void run()
 		{
 			long timevm = System.currentTimeMillis();
 			report.vmName = task.name;
 
 			try
 			{
 				if(task.mountDatastoreTask != null)
 				{
 					report.mountDatastoreReport = new MountDatastoreReport();
 					long time = System.currentTimeMillis();
 					try
 					{
 						vcli.mount(task.mountDatastoreTask.server,
 								task.mountDatastoreTask.path,
 								task.mountDatastoreTask.name);
 						report.mountDatastoreReport.result = Result.SUCCESS;
 						report.mountDatastoreReport.time = (System.currentTimeMillis() - time) / 1000;
 					} catch (VIClientException vice) {
 						report.mountDatastoreReport.message = vice.getMessage();
 						report.mountDatastoreReport.time = (System.currentTimeMillis() - time) / 1000;
 						vice.printStackTrace();
 						if(task.mountDatastoreTask.critical)
 						{
 							report.mountDatastoreReport.result = Result.FAIL;
 							report.message = "Failed to mount remote datastore [" + task.mountDatastoreTask.name + "] " + task.mountDatastoreTask.server + ":" + task.mountDatastoreTask.path;
 							throw new Exception("Failed to mount remote datastore [" + task.mountDatastoreTask.name + "] " + task.mountDatastoreTask.server + ":" + task.mountDatastoreTask.path);
 						} else {
 							report.mountDatastoreReport.result = Result.WARNING;
 						}
 					}
 				}
 				
 				if(task.shutdownTask != null)
 				{
 					report.shutdownReport = new ShutdownGuestReport();
 					long time = System.currentTimeMillis();
 					try
 					{
 						try{ vcli.shutdown(task.name, task.uuid, task.shutdownTask.timeout); } catch (RuntimeException re) { re.printStackTrace(); }
 						report.shutdownReport.result = Result.SUCCESS;
 						report.shutdownReport.time = (System.currentTimeMillis() - time) / 1000;
 					} catch (VIClientException vice) {
 						report.shutdownReport.message = vice.getMessage();
 						report.shutdownReport.time = (System.currentTimeMillis() - time) / 1000;
 						vice.printStackTrace();
 						if(task.shutdownTask.critical)
 						{
 							report.shutdownReport.result = Result.FAIL;
 							report.message = "Failed to shutdown VM";
 							throw new Exception("Failed to shutdown VM");
 						} else {
 							report.shutdownReport.result = Result.WARNING;
 							
 							if(task.powerOffTask != null)
 							{
 								report.powerOffReport = new PowerOffReport();
 								time = System.currentTimeMillis();
 								try
 								{
 									try{ vcli.poweroff(task.name, task.uuid); } catch (RuntimeException re) { re.printStackTrace(); }
 									report.powerOffReport.result = Result.SUCCESS;
 									report.powerOffReport.time = (System.currentTimeMillis() - time) / 1000;
 								} catch (VIClientException vice2) {
 									report.powerOffReport.message = vice2.getMessage();
 									report.powerOffReport.time = (System.currentTimeMillis() - time) / 1000;
 									vice2.printStackTrace();
 									if(task.powerOffTask.critical)
 									{
 										report.powerOffReport.result = Result.FAIL;
 										report.message = "Failed to poweroff VM";
 										throw new Exception("Failed to poweroff VM");
 									} else {
 										report.powerOffReport.result = Result.WARNING;
 									}
 								}
 							}
 						}
 					}
 				} else {
 					if(task.powerOffTask != null)
 					{
 						report.powerOffReport = new PowerOffReport();
 						long time = System.currentTimeMillis();
 						try
 						{
 							try{ vcli.poweroff(task.name, task.uuid); } catch (RuntimeException re) { re.printStackTrace(); }
 							report.powerOffReport.result = Result.SUCCESS;
 							report.powerOffReport.time = (System.currentTimeMillis() - time) / 1000;
 						} catch (VIClientException vice2) {
 							report.powerOffReport.message = vice2.getMessage();
 							report.powerOffReport.time = (System.currentTimeMillis() - time) / 1000;
 							vice2.printStackTrace();
 							if(task.powerOffTask.critical)
 							{
 								report.powerOffReport.result = Result.FAIL;
 								report.message = "Failed to poweroff VM";
 								throw new Exception("Failed to poweroff VM");
 							} else {
 								report.powerOffReport.result = Result.WARNING;
 							}
 						}
 					}
 				}
 				
 				if(task.copyFileTask != null)
 				{
 					report.copyFileReport = new CopyFileReport();
 					long time = System.currentTimeMillis();
 					try
 					{
 						Set<Object[]> result = vcli.layoutvm(task.name, task.uuid);
 						Set<Object[]> copyme = new HashSet<Object[]>();
 						
 						// Files that are to be skipped
 						for(Object[] o : result)
 						{
 							if(((String)o[0]).matches(task.copyFileTask.exclude == null ? "" : task.copyFileTask.exclude))
 							{
 								CopyFileReport.Skipped skipped = new CopyFileReport.Skipped();
 								skipped.name = (String)o[0];
 								skipped.type = (String)o[1];
 								skipped.size = (Long)o[2];
 								report.copyFileReport.skipped.add(skipped);
 							}								
 							else
 								copyme.add(o);
 						}
 						
 						// We need to create the folder structure first
 						Set<String> folders = new HashSet<String>();
 						for(Object[] o : copyme)
 						{
 							String file = (String)o[0];
 							String folder = file.substring(file.indexOf(']') + 1, file.lastIndexOf('/')).trim();
 							// It's already to be created? Skip it
 							if(folders.contains(folder))
 								continue;
 							// It's a parent folder of one of the folders to be created? Skip it
 							for(String f : folders)
 								if(f.contains(folder))
 									continue;
 							// We need to create it
 							folders.add(folder);
 						}
 						for(String folder : folders)
 							try
 							{
 								vcli.mkdir(task.copyFileTask.datastore, folder);
 							} catch (Exception e) {
 								;
 							}
 						
 						Set<String> validfiles = new HashSet<String>();
 						validfiles.add("config");			// .vmx
 						validfiles.add("extendedConfig");	// .vmxf
 						validfiles.add("snapshotList");		// .vmsd
 						validfiles.add("diskDescriptor");	// .vmdk
 						validfiles.add("nvram");			// .nvram
 						validfiles.add("snapshotList");		// .vmsd
 						validfiles.add("snapshotData");		// .vmsn
 						validfiles.add("diskExtent");		// -flat.vmdk;-delta.vmdk
 						
 						// Now we can copy the files
 						for(Object[] o : copyme)
 						{
 							String file = (String)o[0];
 							String dest = "[" + task.copyFileTask.datastore + "]" + file.substring(file.indexOf(']') + 1);
 							String taskid;
 							
 							String filetype = (String)o[1];							
 							
 							if(validfiles.contains(filetype))
 							{
 								if(filetype.equals("diskDescriptor"))
 								{
 									try // We mask off Exceptions since if the file to be deleted is not found we get an error here
 									{
 										taskid = vcli.rmdisk(dest);
 										while(!vcli.polltask(taskid))
 										{
 											try { Thread.sleep(1000); } catch (InterruptedException ie) {}
 										}
 									} catch (VIClientException vice) { }
 									taskid = vcli.copydisk(file, dest);
 								}
 								else
 								{
 									try // We mask off Exceptions since if the file to be deleted is not found we get an error here
 									{
 										taskid = vcli.rmfile(dest);
 										while(!vcli.polltask(taskid))
 										{
 											try { Thread.sleep(1000); } catch (InterruptedException ie) {}
 										}
 									} catch (VIClientException vice) { }
 									taskid = vcli.copyfile(file, dest);
 								}
 								
 								long timefile = System.currentTimeMillis();
 								try
 								{
 									while(!vcli.polltask(taskid))
 									{
 										try { Thread.sleep(1000); } catch (InterruptedException ie) {}
 									}
 									CopyFileReport.Copied copied = new CopyFileReport.Copied();
 									copied.name = (String)o[0];
 									copied.type = (String)o[1];
 									copied.size = (Long)o[2];
 									copied.time = (System.currentTimeMillis() - timefile) / 1000;
 									report.copyFileReport.copied.add(copied);
 								} catch (VIClientException vice) {
 									CopyFileReport.Failed failed = new CopyFileReport.Failed();
 									failed.name = (String)o[0];
 									failed.type = (String)o[1];
 									failed.size = (Long)o[2];
 									failed.time = (System.currentTimeMillis() - timefile) / 1000;
 									report.copyFileReport.failed.add(failed);
 								}
 							}
 						}
 						if(report.copyFileReport.failed.size() > 0)
 						{
 							report.copyFileReport.result = Result.FAIL;
 							report.message = "Failed to copy VM files to Datastore [" + task.copyFileTask.datastore + "]";
 							throw new VIClientException("Failed to copy VM files to Datastore [" + task.copyFileTask.datastore + "]");
 						}
 						else
 							report.copyFileReport.result = Result.SUCCESS;
 						report.copyFileReport.time = (System.currentTimeMillis() - time) / 1000;
 					} catch (VIClientException vice) {
 						report.copyFileReport.message = vice.getMessage();
 						report.copyFileReport.time = (System.currentTimeMillis() - time) / 1000;
 						vice.printStackTrace();
 						if(task.copyFileTask.critical)
 						{
 							report.copyFileReport.result = Result.FAIL;
 							report.message = "Failed to copy VM files to Datastore [" + task.copyFileTask.datastore + "]";
 							// Ok, copying the files failed, but we may still need the VM to be powered on
 							if(task.powerOnTask != null)
 							{
 								report.powerOnReport = new PowerOnReport();
 								long time2 = System.currentTimeMillis();
 								try
 								{
 									vcli.poweron(task.name, task.uuid);
 									report.powerOnReport.result = Result.SUCCESS;
 									report.powerOnReport.time = (System.currentTimeMillis() - time2) / 1000;
 								} catch (VIClientException vice2) {
 									report.powerOnReport.message = vice2.getMessage();
 									report.powerOnReport.time = (System.currentTimeMillis() - time2) / 1000;
 									vice2.printStackTrace();
 									if(task.powerOnTask.critical)
 									{
 										report.powerOnReport.result = Result.FAIL;
 										report.message = "Failed to poweron VM";
 										throw new Exception("Failed to poweron VM");
 									} else {
 										report.powerOnReport.result = Result.WARNING;
 									}
 								}
 							}
 							throw new Exception("Failed to copy VM files to Datastore [" + task.copyFileTask.datastore + "]");
 						} else {
 							report.copyFileReport.result = Result.WARNING;
 						}
 					}
 				}
 				
 				if(task.exportOVFTask != null)
 				{
 					report.exportOVFReport = new ExportOVFReport();
 					long time = System.currentTimeMillis();
 					try
 					{
						ExportVMResponse expvm = vcli.exportvm(task.name);
						String baseName = (task.exportOVFTask.name.equals("") ? task.name : task.exportOVFTask.name);
 						File ovfPath = new File(
 							task.exportOVFTask.path + File.separator +
 							task.exportOVFTask.prefix +
 							baseName +
 							task.exportOVFTask.suffix
 						);
 						ovfPath.mkdirs();
 						// Write OVF manifest
 						File manifest = new File(ovfPath, baseName + ".ovf");
 						try
 						{
 							RandomAccessFile out = new RandomAccessFile(manifest, "rw");
 							out.writeBytes(expvm.ovfDescriptor);
 							out.close();
 						} catch (IOException ioe) {
 							report.exportOVFReport.result = Result.FAIL;
 							report.message = "Failed to write OVF manifest to '" + manifest + "'";
 							throw new VIClientException("Failed to export VirtualMachine files from Datastore");
 						}
 						// Write OVF files
 						for(ExportVMResponse.HttpNfcLease.Info.DeviceUrl url : expvm.nfcLease.nfcInfo.urls)
 						{
 							long timefile = System.currentTimeMillis();
 							try
 							{
 								File dest = new File(ovfPath, url.targetId);
 								vcli.nfcget(url.url, dest);
 								ExportOVFReport.Device device = new ExportOVFReport.Device();
 								device.file = url.targetId;
 								device.size = dest.length();
 								device.deviceid = url.key;
 								device.time = (System.currentTimeMillis() - timefile) / 1000;
 								report.exportOVFReport.device.add(device);
 							} catch (VIClientException vice) {
 								report.exportOVFReport.result = Result.FAIL;
 								report.message = "Failed to export VirtualMachine files from Datastore";
 								throw new VIClientException("Failed to export VirtualMachine files from Datastore");
 							}
 						}
 						// Be polite and close NFC session
 						vcli.nfsclose(expvm.nfcLease.sessionId);
 						report.exportOVFReport.result = Result.SUCCESS;
 						report.exportOVFReport.time = (System.currentTimeMillis() - time) / 1000;
 					} catch (VIClientException vice) {
 						report.exportOVFReport.message = vice.getMessage();
 						report.exportOVFReport.time = (System.currentTimeMillis() - time) / 1000;
 						vice.printStackTrace();
 						if(task.exportOVFTask.critical)
 						{
 							report.exportOVFReport.result = Result.FAIL;
 							report.message = "Failed to export VM [" + task.exportOVFTask.name + "] in OVF format.";
 							if(task.powerOnTask != null)
 							{
 								report.powerOnReport = new PowerOnReport();
 								long time2 = System.currentTimeMillis();
 								try
 								{
									vcli.poweron(task.name, task.uuid);
 									report.powerOnReport.result = Result.SUCCESS;
 									report.powerOnReport.time = (System.currentTimeMillis() - time2) / 1000;
 								} catch (VIClientException vice2) {
 									report.powerOnReport.message = vice2.getMessage();
 									report.powerOnReport.time = (System.currentTimeMillis() - time2) / 1000;
 									vice2.printStackTrace();
 									if(task.powerOnTask.critical)
 									{
 										report.powerOnReport.result = Result.FAIL;
 										report.message = "Failed to poweron VM";
 										throw new Exception("Failed to poweron VM");
 									} else {
 										report.powerOnReport.result = Result.WARNING;
 									}
 								}
 							}
 							throw new Exception("Failed to export VirtualMachine [" + task.exportOVFTask.name + "] in OVF format.");
 						} else {
 							report.exportOVFReport.result = Result.WARNING;
 						}
 					}
 				}
 				
 				if(task.powerOnTask != null)
 				{
 					report.powerOnReport = new PowerOnReport();
 					long time = System.currentTimeMillis();
 					try
 					{
 						vcli.poweron(task.name, task.uuid);
 						report.powerOnReport.result = Result.SUCCESS;
 						report.powerOnReport.time = (System.currentTimeMillis() - time) / 1000;
 					} catch (VIClientException vice2) {
 						report.powerOnReport.message = vice2.getMessage();
 						report.powerOnReport.time = (System.currentTimeMillis() - time) / 1000;
 						vice2.printStackTrace();
 						if(task.powerOnTask.critical)
 						{
 							report.powerOnReport.result = Result.FAIL;
 							report.message = "Failed to poweron VM";
 							throw new Exception("Failed to poweron VM");
 						} else {
 							report.powerOnReport.result = Result.WARNING;
 						}
 					}
 				}
 					
 				if(task.umountDatastoreTask != null)
 				{
 					report.umountDatastoreReport = new UmountDatastoreReport();
 					long time = System.currentTimeMillis();
 					try
 					{
 						vcli.unmount(task.umountDatastoreTask.name);
 						report.umountDatastoreReport.result = Result.SUCCESS;
 						report.umountDatastoreReport.time = (System.currentTimeMillis() - time) / 1000;
 					} catch (VIClientException vice) {
 						report.umountDatastoreReport.message = vice.getMessage();
 						report.umountDatastoreReport.time = (System.currentTimeMillis() - time) / 1000;
 						vice.printStackTrace();
 						if(task.umountDatastoreTask.critical)
 						{
 							report.umountDatastoreReport.result = Result.FAIL;
 							throw new Exception("Failed to umount remote datastore [" + task.mountDatastoreTask.name + "] " + task.mountDatastoreTask.server + ":" + task.mountDatastoreTask.path);
 						} else {
 							report.umountDatastoreReport.result = Result.WARNING;
 						}
 					}
 				}
 				
 				report.result = Result.SUCCESS;
 				report.time = (System.currentTimeMillis() - timevm) / 1000;
 			} catch (Exception e) {
 				e.printStackTrace();
 				report.criticalErrorReport = new CriticalErrorReport(e.getMessage());
 				report.result = Result.FAIL;
 				report.time = (System.currentTimeMillis() - timevm) / 1000;
 			}
 
 			running = false;
 		}
 		
 		public BackupVMReport getReport()
 		{
 			return report;
 		}
 	}
 }
