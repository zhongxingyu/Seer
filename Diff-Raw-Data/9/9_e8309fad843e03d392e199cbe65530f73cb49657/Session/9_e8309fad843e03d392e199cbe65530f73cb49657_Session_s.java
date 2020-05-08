 /**
  * Copyright (C) 2011 / cube-team <https://cube.forge.osor.eu>
  *
  * Licensed under the Apache License, Version 2.0 (the "License").
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package ch.admin.vbs.cube.core.impl;
 
 import java.util.ArrayList;
 import java.util.ResourceBundle;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.admin.vbs.cube.common.container.Container;
 import ch.admin.vbs.cube.common.container.IContainerFactory;
 import ch.admin.vbs.cube.common.keyring.IIdentityToken;
 import ch.admin.vbs.cube.common.keyring.IKeyring;
 import ch.admin.vbs.cube.common.keyring.impl.KeyringProvider;
 import ch.admin.vbs.cube.core.I18nBundleProvider;
 import ch.admin.vbs.cube.core.ISession;
 import ch.admin.vbs.cube.core.ISessionUI;
 import ch.admin.vbs.cube.core.vm.Vm;
 import ch.admin.vbs.cube.core.vm.VmController;
 import ch.admin.vbs.cube.core.vm.VmModel;
 import ch.admin.vbs.cube.core.vm.VmStatus;
 import ch.admin.vbs.cube.core.vm.list.DescriptorModelCache;
 import ch.admin.vbs.cube.core.vm.list.WSDescriptorUpdater;
 
 /**
  * A session object is created for each user, once he is successfully logged in.
  * This object own its own thread used to open or close the session, since it is
  * slow (opening/closing encrypted containers). It should not block the caller
  * since it is the LoginMachin that MUST be free to react to user interactions
  * (insert/remove its token)
  * 
  * The stateCnt variable ensure we detect if state changes have been requested
  * between the beginning of the switch statement and its end. If there was one
  * or many changed, the last request will be proceeded (skipping all other
  * requests) in order to reach the last demanded state.
  */
 public class Session implements Runnable, ISession {
 	/** Logger */
 	private static final Logger LOG = LoggerFactory.getLogger(Session.class);
 	private IIdentityToken id;
 	private Thread thread;
 	private int stateCnt = 0;
 	private State state = State.idle;
 	private long timestamp;
 	private Object lock = new Object();
 	private final ISessionUI sessionUI;
 	private IKeyring keyring;
 	private Container transfer;
 	private IContainerFactory containerFactory;
 	private TransferContainerFactory trfFactory = new TransferContainerFactory();
 	private ResourceBundle bundle;
 	private DescriptorModelCache descModelCache;
 	private VmModel vmModel;
 	private WSDescriptorUpdater descWs;
 	private final VmController vmController;
 	private Executor exec = Executors.newCachedThreadPool();
 
 	public Session(IIdentityToken id, ISessionUI clientUI, VmController vmController) {
 		this.id = id;
 		this.sessionUI = clientUI;
 		this.vmController = vmController;
 		vmModel = new VmModel();
 		vmController.registerVmModel(vmModel);
 		bundle = I18nBundleProvider.getBundle();
 		thread = new Thread(this, "Session-(" + id.getSubjectName() + ")");
 		thread.start();
 	}
 
 	/**
 	 * @return true is Web Service is started and connected.
 	 */
 	public boolean isSessionOnline() {
 		return descWs == null ? false : descWs.isConnected();
 	}
 
 	@Override
 	public VmModel getModel() {
 		return vmModel;
 	}
 
 	@Override
 	public IIdentityToken getId() {
 		return id;
 	}
 
 	@Override
 	public void setId(IIdentityToken id) {
 		this.id = id;
 		keyring.setId(id);
 		// restart the web service since the keystore has been updated
 		synchronized (vmModel) {
 			if (descWs != null) {
 				descWs.stop();
 				descWs = new WSDescriptorUpdater(vmModel, id.getBuilder());
 				descWs.start();
 			}
 		}
 	}
 
 	@Override
 	public void run() {
 		boolean online = false;
 		while (true) {
 			State s = state;
 			timestamp = System.currentTimeMillis();
 			int flag = stateCnt;
			LOG.debug("Proceed state [{}]", s);
 			switch (s) {
 			case idle:
 			case error:
 				//
 				synchronized (lock) {
 					// check online/offline
 					if (online != isSessionOnline()) {
 						online = isSessionOnline();
 						sessionUI.notifySessionState(this, new SessionStateDTO(id, state, online));
 					}
 					// wait
 					try {
 						// it may have changed since switch statement (->
 						// deadlock);
 						if (state == s)
 							lock.wait(1000);
 					} catch (InterruptedException e) {
 						LOG.error("Failure", e);
 					}
 				}
 				break;
 			case open:
 				// Transfer Container
 				sessionUI.showDialog(bundle.getString("login.open_transfer"), this);
 				try {
 					if (transfer == null) {
 						LOG.debug("Initialize transfer container");
 						transfer = trfFactory.initTransfer(id);
 					} else {
 						LOG.debug("Transfer container is already opened");
 					}
 				} catch (Exception e1) {
 					LOG.error("Failed to open init transfer container", e1);
 					sessionUI.showDialog(bundle.getString("login.error_transfer"), this);
 					state = State.error;
 					break;
 				}
 				// Keyring
 				sessionUI.showDialog(bundle.getString("login.open_keyring"), this);
 				try {
 					if (keyring == null) {
 						IKeyring kr = KeyringProvider.getInstance().getKeyring();
 						kr.open(id, transfer.getMountpoint());
 						keyring = kr;
 						// setup cache to save descriptor in a file in keyring
 						descModelCache = new DescriptorModelCache(vmModel, keyring.getFile("descriptors-cache"));
 						descModelCache.start();
 					}
 				} catch (Exception e) {
 					LOG.error("Failed to open keyring", e);
 					sessionUI.showDialog(bundle.getString("login.error_keyring"), this);
 					state = State.error;
 					break;
 				}
 				sessionUI.showWorkspace(this);
 				// start web service
 				try {
 					if (descWs == null) {
 						descWs = new WSDescriptorUpdater(vmModel, id.getBuilder());
 						descWs.start();
 					}
 				} catch (Exception e) {
 					LOG.error("Failed to setup descriptor cache", e);
 					sessionUI.showDialog(bundle.getString("login.error_descriptor"), this);
 					state = State.error;
 					break;
 				}
 				// and wait
 				synchronized (lock) {
 					if (flag == stateCnt) {
 						state = State.idle;
 					}
 				}
 				break;
 			case lock:
 				// lock session (actually do nothing)
 				LOG.error("session locked");
 				// and wait
 				synchronized (lock) {
 					if (flag == stateCnt) {
 						state = State.idle;
 					}
 				}
 				break;
 			case close:
 				// standby all VMs
 				closeAllVms();
 				// stop web service
 				if (descWs != null) {
 					descWs.stop();
 					descWs = null;
 				}
 				// unregister vm
 				vmController.unregisterVmModel(vmModel);
 				// close keyring
 				try {
 					keyring.close();
 					keyring = null;
 				} catch (Exception e) {
 					LOG.error("Failed to close keyring", e);
 				}
 				// close transfer volume
 				try {
 					trfFactory.disposeTransfer(transfer);
 					transfer = null;
 				} catch (Exception e) {
 					LOG.error("Failed to close keyring", e);
 				}
 				LOG.debug("Session close. Session's thread stopped.");
 				// exit thread
 				return;
 			default:
 				break;
 			}
			LOG.debug("State [{}] completed in [{} sec]", s, String.format("%.3f", (System.currentTimeMillis() - timestamp) / 1000f));
 		}
 	}
 
 	@Override
 	public void open() {
 		LOG.debug("Trigger 'open'");
 		synchronized (lock) {
 			state = State.open;
 			stateCnt++;
 			lock.notifyAll();
 		}
 	}
 
 	@Override
 	public void lock() {
 		LOG.debug("Trigger 'lock'");
 		synchronized (lock) {
 			state = State.lock;
 			stateCnt++;
 			lock.notifyAll();
 		}
 	}
 
 	@Override
 	public void close() {
 		LOG.debug("Trigger 'close'");
 		synchronized (lock) {
 			state = State.close;
 			stateCnt++;
 			lock.notifyAll();
 		}
 	}
 
 	@Override
 	public void setContainerFactory(IContainerFactory containerFactory) {
 		this.containerFactory = containerFactory;
 		trfFactory.setContainerFactory(this.containerFactory);
 	}
 
 	@Override
 	public void controlVm(String vmId, final VmCommand cmd, final IOption option) {
 		if ((state == State.idle || state == State.close) && keyring != null) {
 			final Vm vm = vmModel.findByInstanceUid(vmId);
 			if (vm == null) {
 				LOG.debug("VM not found [" + vmId + "] in this session.");
 			} else {
 				exec.execute(new Runnable() {
 					@Override
 					public void run() {
 						vmController.controlVm(vm, vmModel, cmd, id, keyring, transfer, option);
 					}
 				});
 			}
 		} else {
 			LOG.debug("Ignore command [" + cmd + "] because session is not ready [" + state + " / " + keyring + "].");
 		}
 	}
 
 	private void closeAllVms() {
 		// Index running VMs
 		ArrayList<Vm> vmIndex = new ArrayList<Vm>();
 		// Start 'SAVE' for all running VMs
 		for (final Vm vm : vmModel.getVmList()) {
 			if (vm.getVmStatus() == VmStatus.RUNNING) {
 				vmIndex.add(vm);
 				LOG.debug("Save VM [{}]", vm.getDescriptor().getRemoteCfg().getName());
 				controlVm(vm.getId(), VmCommand.SAVE, null);
 			}
 		}
 		LOG.debug("VM to be saved before closing the session [" + vmIndex.size() + "]");
 		// Wait that all VMs have been saved
 		long timeout = System.currentTimeMillis() + 30000;
 		int remindBkp = -1;
 		while (System.currentTimeMillis() < timeout) {
 			int remind = 0;
 			for (Vm vm : vmIndex) {
 				if (vm.getVmStatus() == VmStatus.STOPPING || vm.getVmStatus() == VmStatus.RUNNING) {
 					LOG.debug("Wait on VM [{}][{}]", vm.getDescriptor().getRemoteCfg().getName(), vm.getVmStatus());
 					remind++;
 				}
 			}
 			if (remind == 0) {
 				LOG.debug("All VMs [{}] are stopped [{} secs]", vmIndex.size(), (timeout - System.currentTimeMillis()) / 1000);
 				return;
 			} else {
 				if (remindBkp != remind) {
 					remindBkp = remind;
 					sessionUI.showDialog(I18nBundleProvider.getBundle().getString("login.waitstopped") + " (" + remind + " VMs)", this);
 					LOG.debug("Wait on [{}] VMs", remind);
 					try {
 						Thread.sleep(1000);
 					} catch (Exception e) {
 					}
 				}
 			}
 			try {
 				Thread.sleep(500);
 			} catch (Exception e) {
 			}
 		}
 		LOG.error("Not all VMs have been saved during logout");
 	}
 
 	public static class SessionStateDTO implements ISessionStateDTO {
 		private final boolean online;
 		private final IIdentityToken id;
 		private final State state;
 
 		public SessionStateDTO(IIdentityToken id, State state, boolean online) {
 			this.id = id;
 			this.state = state;
 			this.online = online;
 		}
 
 		public boolean isOnline() {
 			return online;
 		}
 
 		public IIdentityToken getId() {
 			return id;
 		}
 
 		public State getState() {
 			return state;
 		}
 	}
 }
