 package hlmp.SubProtocol.FileTransfer;
 
 import java.util.Hashtable;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.UUID;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import hlmp.CommLayer.MessageTypeList;
 import hlmp.CommLayer.NetUser;
 import hlmp.CommLayer.SubProtocol;
 import hlmp.CommLayer.Messages.Message;
 import hlmp.CommLayer.Observers.AddUserEventObserverI;
 import hlmp.CommLayer.Observers.ConnectEventObserverI;
 import hlmp.CommLayer.Observers.DisconnectEventObserverI;
 import hlmp.CommLayer.Observers.ReconnectingEventObserverI;
 import hlmp.SubProtocol.FileTransfer.Constants.FileMessageHandlerState;
 import hlmp.SubProtocol.FileTransfer.Constants.FileTransferProtocolType;
 import hlmp.SubProtocol.FileTransfer.ControlI.FileHandlerI;
 import hlmp.SubProtocol.FileTransfer.ControlI.FileListHandlerI;
 import hlmp.SubProtocol.FileTransfer.Messages.FileCompleteMessage;
 import hlmp.SubProtocol.FileTransfer.Messages.FileErrorMessage;
 import hlmp.SubProtocol.FileTransfer.Messages.FileListMessage;
 import hlmp.SubProtocol.FileTransfer.Messages.FileListRequestMessage;
 import hlmp.SubProtocol.FileTransfer.Messages.FilePartMessage;
 import hlmp.SubProtocol.FileTransfer.Messages.FileRequestMessage;
 import hlmp.SubProtocol.FileTransfer.Messages.FileWaitMessage;
 
 
 public class FileTransferProtocol extends SubProtocol implements AddUserEventObserverI, ConnectEventObserverI, DisconnectEventObserverI, ReconnectingEventObserverI {
 
 	public FileHandlerI controlFileHandler;
 	public FileListHandlerI controlFileListHandler;
 	private FileMessageHandlerQueue fileMessageDownloadQueue;
 	private FileMessageHandlerQueue fileMessageUploadQueue;
 	private Timer timer;
 	private AtomicInteger timerPoint;
 	private Thread timerThread;
 	private AtomicInteger fileMessageHandlerPoint;
 	private Object fileMessageHandlerLock;
 	private FileData fileData;
 	private Hashtable<UUID,FileMessageHandler> activeDownloads;
 	private Hashtable<UUID,FileMessageHandler> activeUploads;
 
 
 	public FileTransferProtocol(FileHandlerI controlFileHandler, FileListHandlerI controlFileListHandler, FileData fileData) {
 		this.controlFileHandler = controlFileHandler;
 		this.controlFileListHandler = controlFileListHandler;
 		this.fileData = fileData;
 		init();
 	}
 
 	private void init() {
 		fileMessageDownloadQueue = new FileMessageHandlerQueue();
 		fileMessageUploadQueue = new FileMessageHandlerQueue();
 		activeDownloads = new Hashtable<UUID,FileMessageHandler>();
 		activeUploads = new Hashtable<UUID,FileMessageHandler>();
 		fileMessageHandlerLock = new Object();
 		timerPoint = new AtomicInteger(0);
 		fileMessageHandlerPoint = new AtomicInteger(0);
 		timer = new Timer();
 	}
 
 
 	// Concurrent Event process
 
 	private TimerTask getFileTransferTimerTask(){
     	TimerTask t = new TimerTask() {
 
 			@Override
 			public void run() {
 				if (timerPoint.compareAndSet(0, 1)) {
 					timerThread = getFileTransferTimerInterationThread();
 					timerThread.setName("Timer FileTransfer Thread");
 					timerThread.start();
 	                try {
 						timerThread.join();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 	                timerPoint.set(0);
 	            }
 				
 			}
     		
     	};
     	return t;
     }
 	
 	private Thread getFileTransferTimerInterationThread() {
     	Thread t = new Thread() {
 
 			@Override
 			public void run() {
 				try {
 					processFiles();
 				} catch (InterruptedException e) {
 				}
 			}
     		
     	};
     	return t; 
     }
 	
 	
 	public void proccessMessage(Message message) {
 		switch (message.getType()) {
 			case FileTransferProtocolType.FILEREQUESTMESSAGE: {
 				FileRequestMessage fileRequestMessage = (FileRequestMessage)message;
 				FileInformation fileInformation = fileData.getFileList().getFileInformation(fileRequestMessage.getFileId());
 				if (fileInformation != null) {
 					FileMessageSender fileMessageSender = new FileMessageSender(fileRequestMessage.getSenderNetUser(), fileRequestMessage.getFileHandlerId(), this, fileInformation, fileData);
 					synchronized (fileMessageHandlerLock) {
 						if (!activeUploads.contains(fileMessageSender.getId())) {
 							if (fileMessageUploadQueue.put(fileMessageSender)) {
 								controlFileHandler.uploadFileQueued(fileRequestMessage.getSenderNetUser(), fileMessageSender.getId().toString(), fileInformation.getName());
 							}
 						}
 					}
 				}
 				else {
 					FileErrorMessage fileErrorMessage = new FileErrorMessage(fileRequestMessage.getSenderNetUser(), fileRequestMessage.getFileHandlerId());
 					this.sendMessageEvent(fileErrorMessage);
 				}
 				break;
 			}
 			case FileTransferProtocolType.FILEERRORMESSAGES: {
 				FileErrorMessage fileErrorMessage = (FileErrorMessage)message;
 				synchronized (fileMessageHandlerLock) {
 					if (activeUploads.contains(fileErrorMessage.getFileHandlerId())) {
 						FileMessageHandler fileMessageHandler = activeUploads.get(fileErrorMessage.getFileHandlerId());
 						controlFileHandler.uploadFileFailed(fileMessageHandler.getId().toString());
 						fileMessageHandler.close();
 						activeUploads.remove(fileMessageHandler.getId());
 					}
 					else if (activeDownloads.contains(fileErrorMessage.getFileHandlerId())) {
 						FileMessageHandler fileMessageHandler = activeDownloads.get(fileErrorMessage.getFileHandlerId());
 						controlFileHandler.downloadFileFailed(fileMessageHandler.getId().toString());
 						fileMessageHandler.close();
 						activeDownloads.remove(fileMessageHandler.getId());
 					}
 				}
 				break;
 			}
 			case FileTransferProtocolType.FILEPARTMESSAGE: {
 				FilePartMessage filePartMessage = (FilePartMessage)message;
 				synchronized (fileMessageHandlerLock) {
 					if (activeDownloads.contains(filePartMessage.getFileHandlerId())) {
 						FileMessageHandler fileMessageHandler = activeDownloads.get(filePartMessage.getFileHandlerId());
 						fileMessageHandler.attendMessage(filePartMessage);
 						fileMessageHandler.waitUp(fileData.getFileRiseUp());
 					}
 					else {
 						FileErrorMessage fileErrorMessage = new FileErrorMessage(filePartMessage.getSenderNetUser(), filePartMessage.getFileHandlerId());
 						this.sendMessageEvent(fileErrorMessage);
 					}
 				}
 				break;
 			}
 			case FileTransferProtocolType.FILEWAITMESSAGE: {
 				FileWaitMessage fileWaitMessage = (FileWaitMessage)message;
 				synchronized (fileMessageHandlerLock) {
 					if (activeDownloads.contains(fileWaitMessage.getFileHandlerId())) {
 						FileMessageHandler fileMessageHandler = activeDownloads.get(fileWaitMessage.getFileHandlerId());
 						fileMessageHandler.waitUp(fileData.getFileRiseUp());
 					}
 					else if (activeUploads.contains(fileWaitMessage.getFileHandlerId())) {
 						FileMessageHandler fileMessageHandler = activeUploads.get(fileWaitMessage.getFileHandlerId());
 						fileMessageHandler.waitUp(fileData.getFileRiseUp());
 					}
 					else {
 						if (!fileMessageDownloadQueue.contains(fileWaitMessage.getFileHandlerId()) && !fileMessageUploadQueue.contains(fileWaitMessage.getFileHandlerId())) {
 							FileErrorMessage fileErrorMessage = new FileErrorMessage(fileWaitMessage.getSenderNetUser(), fileWaitMessage.getFileHandlerId());
 							this.sendMessageEvent(fileErrorMessage);
 						}
 					}
 				}
 				break;
 			}
 			case FileTransferProtocolType.FILECOMPLETEMESSAGE: {
 				FileCompleteMessage fileCompleteMessage = (FileCompleteMessage)message;
 				synchronized (fileMessageHandlerLock) {
 					if (activeUploads.contains(fileCompleteMessage.getFileHandlerId())) {
 						FileMessageHandler fileMessageHandler = activeUploads.get(fileCompleteMessage.getFileHandlerId());
 						fileMessageHandler.setState(FileMessageHandlerState.COMPLETED);
 					}
 				}
 				break;
 			}
 			case FileTransferProtocolType.FILELISTREQUESTMESSAGE: {
 				FileListRequestMessage fileListRequestMessage = (FileListRequestMessage)message;
 				FileListMessage fileListMessage = new FileListMessage(fileListRequestMessage.getSenderNetUser(), fileData.getFileList());
 				this.sendMessageEvent(fileListMessage);
 				break;
 			}
 			case FileTransferProtocolType.FILELISTMESSAGE: {
 				FileListMessage fileListMessage = (FileListMessage)message;
 				controlFileListHandler.addFileList(fileListMessage.getSenderNetUser(), fileListMessage.getFileList());
 				break;
 			}
 		}
 	}
 
 	public void errorMessage(Message message) {
 		switch (message.getType()) {
 			case FileTransferProtocolType.FILEREQUESTMESSAGE: {
 				FileRequestMessage fileRequestMessage = (FileRequestMessage)message;
 				synchronized (fileMessageHandlerLock) {
 					if (activeDownloads.contains(fileRequestMessage.getFileHandlerId()))
 					{
 						FileMessageHandler fileMessageHandler = activeDownloads.get(fileRequestMessage.getFileHandlerId());
 						controlFileHandler.downloadFileFailed(fileMessageHandler.getId().toString());
 						fileMessageHandler.close();
 						activeDownloads.remove(fileMessageHandler.getId());
 					}
 				}
 				break;
 			}
 			case FileTransferProtocolType.FILEERRORMESSAGES: {
 				break;
 			}
 			case FileTransferProtocolType.FILEPARTMESSAGE: {
 				FilePartMessage filePartMessage = (FilePartMessage)message;
 				synchronized (fileMessageHandlerLock) {
 					if (activeUploads.contains(filePartMessage.getFileHandlerId()))
 					{
 						FileMessageHandler fileMessageHandler = activeUploads.get(filePartMessage.getFileHandlerId());
 						controlFileHandler.uploadFileFailed(fileMessageHandler.getId().toString());
 						fileMessageHandler.close();
 						activeUploads.remove(fileMessageHandler.getId());
 					}
 				}
 				break;
 			}
 			case FileTransferProtocolType.FILEWAITMESSAGE: {
 				FileWaitMessage fileWaitMessage = (FileWaitMessage)message;
 				synchronized (fileMessageHandlerLock) {
 					if (activeDownloads.contains(fileWaitMessage.getFileHandlerId()))
 					{
 						FileMessageHandler fileMessageHandler = activeDownloads.get(fileWaitMessage.getFileHandlerId());
 						controlFileHandler.downloadFileFailed(fileMessageHandler.getId().toString());
 						fileMessageHandler.close();
 						activeDownloads.remove(fileMessageHandler.getId());
 					}
 					else if (activeUploads.contains(fileWaitMessage.getFileHandlerId()))
 					{
 						FileMessageHandler fileMessageHandler = activeUploads.get(fileWaitMessage.getFileHandlerId());
 						controlFileHandler.uploadFileFailed(fileMessageHandler.getId().toString());
 						fileMessageHandler.close();
 						activeUploads.remove(fileMessageHandler.getId());
 					}
 				}
 				break;
 			}
 			case FileTransferProtocolType.FILECOMPLETEMESSAGE: {
 				break;
 			}
 			case FileTransferProtocolType.FILELISTREQUESTMESSAGE: {
 				break;
 			}
 			case FileTransferProtocolType.FILELISTMESSAGE: {
 				break;
 			}
 		}
 	}
 
 	public void sendFileRequest(NetUser netUser, FileInformation fileInformation) {
 		FileMessageReceiver fileMessageReceiber = new FileMessageReceiver(netUser, this, fileInformation, fileData);
 		if (fileMessageDownloadQueue.put(fileMessageReceiber)) {
 			controlFileHandler.downloadFileQueued(netUser, fileMessageReceiber.getId().toString(), fileInformation.getName());
 		}
 	}
 
 	public void sendFileListRequest(NetUser netUser) {
 		FileListRequestMessage fileListRequestMessage = new FileListRequestMessage(netUser);
 		this.sendMessageEvent(fileListRequestMessage);
 	}
 
 	public void sendFileList(NetUser netUser) {
 		FileListMessage fileListMessage = new FileListMessage(netUser, fileData.getFileList());
 		this.sendMessageEvent(fileListMessage);
 	}
 
 	public void stop() {
 		try {
 			timer.cancel();
 		} catch (Exception e) {
 			
 		}
 		try {
 			timerThread.interrupt();
 			timerThread.join();
 		} catch (Exception e) {
 		}
 		init();
 	}
 
 	public void start() {
 		timer.schedule(getFileTransferTimerTask(), 0, fileData.getTimeIntervalTimer());
 	}
 
 	public MessageTypeList getMessageTypes() {
 		MessageTypeList typeCollection = new MessageTypeList();
 		typeCollection.add(FileTransferProtocolType.FILECOMPLETEMESSAGE, new FileCompleteMessage());
 		typeCollection.add(FileTransferProtocolType.FILEERRORMESSAGES, new FileErrorMessage());
 		typeCollection.add(FileTransferProtocolType.FILEPARTMESSAGE, new FilePartMessage());
 		typeCollection.add(FileTransferProtocolType.FILEREQUESTMESSAGE, new FileRequestMessage());
 		typeCollection.add(FileTransferProtocolType.FILEWAITMESSAGE, new FileWaitMessage());
 		typeCollection.add(FileTransferProtocolType.FILELISTREQUESTMESSAGE, new FileListRequestMessage());
 		typeCollection.add(FileTransferProtocolType.FILELISTMESSAGE, new FileListMessage());
 		return typeCollection;
 	}
 
 	private void processFiles() throws InterruptedException {
 		if (fileMessageHandlerPoint.compareAndSet(0, 1)) {
 			synchronized (fileMessageHandlerLock) {
 				for (FileMessageHandler fileMessageHandler : activeDownloads.values()) {
 					switch (fileMessageHandler.getState()) {
 						case FileMessageHandlerState.WAITING: {
 							fileMessageHandler.open();
 							controlFileHandler.downloadFileOpened(fileMessageHandler.getId().toString());
 							break;
 						}
 						case FileMessageHandlerState.OPEN: {
 							fileMessageHandler.setTimeOut(fileMessageHandler.getTimeOut()-1);
 							if (fileMessageHandler.getTimeOut() <= 0) {
 								fileMessageHandler.resetTimeOut();
 								this.sendMessageEvent(new FileWaitMessage(fileMessageHandler.getRemoteNetUser(), fileMessageHandler.getId()));
 							}
 							break;
 						}
 						case FileMessageHandlerState.ACTIVE: {
 							fileMessageHandler.execute();
 							controlFileHandler.downloadFileTransfer(fileMessageHandler.getId().toString(), fileMessageHandler.completed());
 							fileMessageHandler.setTimeOut(fileMessageHandler.getTimeOut()-1);
 							if (fileMessageHandler.getTimeOut() <= 0)
 							{
 								fileMessageHandler.resetTimeOut();
 								this.sendMessageEvent(new FileWaitMessage(fileMessageHandler.getRemoteNetUser(), fileMessageHandler.getId()));
 							}
 							break;
 						}
 						case FileMessageHandlerState.ERROR: {
 							controlFileHandler.downloadFileFailed(fileMessageHandler.getId().toString());
 							fileMessageHandler.close();
 							activeDownloads.remove(fileMessageHandler.getId());
 							break;
 						}
 						case FileMessageHandlerState.COMPLETED: {
 							controlFileHandler.downloadFileComplete(fileMessageHandler.getId().toString(), fileMessageHandler.getFileName());
 							fileMessageHandler.close();
 							activeDownloads.remove(fileMessageHandler.getId());
 							break;
 						}
 					}
 				}
 			}
 
 			synchronized (fileMessageHandlerLock) {
 				for (FileMessageHandler fileMessageHandler : activeUploads.values()) {
 					switch (fileMessageHandler.getState()) {
 						case FileMessageHandlerState.WAITING: {
 							fileMessageHandler.open();
 							controlFileHandler.uploadFileOpened(fileMessageHandler.getId().toString());
 							break;
 						}
 						case FileMessageHandlerState.OPEN: {
 							fileMessageHandler.setTimeOut(fileMessageHandler.getTimeOut()-1);
 							if (fileMessageHandler.getTimeOut() <= 0)
 							{
 								fileMessageHandler.resetTimeOut();
 								this.sendMessageEvent(new FileWaitMessage(fileMessageHandler.getRemoteNetUser(), fileMessageHandler.getId()));
 							}
 							break;
 						}
 						case FileMessageHandlerState.ACTIVE: {
 							fileMessageHandler.execute();
 							controlFileHandler.uploadFileTransfer(fileMessageHandler.getId().toString(), fileMessageHandler.completed());
 							break;
 						}
 						case FileMessageHandlerState.ERROR: {
 							controlFileHandler.uploadFileFailed(fileMessageHandler.getId().toString());
 							fileMessageHandler.close();
 							activeUploads.remove(fileMessageHandler.getId());
 							break;
 						}
 						case FileMessageHandlerState.COMPLETED: {
 							controlFileHandler.uploadFileComplete(fileMessageHandler.getId().toString());
 							fileMessageHandler.close();
 							activeUploads.remove(fileMessageHandler.getId());
 							break;
 						}
 					}
 				}
 			}
 			synchronized (fileMessageHandlerLock) {
 				if (activeDownloads.size() < fileData.getSimulteneusDownload()) {
 					FileMessageHandler fileMessageHandler = fileMessageDownloadQueue.draw();
 					if (fileMessageHandler != null) {
 						activeDownloads.put(fileMessageHandler.getId(), fileMessageHandler);
 					}
 				}
 			}
 			synchronized (fileMessageHandlerLock) {
 				if (activeUploads.size() < fileData.getSimulteneusUpload()) {
 					FileMessageHandler fileMessageHandler = fileMessageUploadQueue.draw();
 					if (fileMessageHandler != null) {
 						activeUploads.put(fileMessageHandler.getId(), fileMessageHandler);
 					}
 				}
 			}
 			fileMessageHandlerPoint.set(0);
 		}
 	}
 
 	
 	
 	// HLMP Communication observer event
 	
 	public void reconnectingEventUpdate() {
 		this.stop();
 	}
 
 	public void disconnectEventUpdate() {
 		this.stop();
 	}
 
 	public void connectEventUpdate() {
 		this.start();
 	}
 
 	public void addUserEventUpdate(NetUser netUser) {
 		this.sendFileListRequest(netUser);		
 	}
 }
