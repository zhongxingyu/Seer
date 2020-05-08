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
 
 package ch.admin.vbs.cube.core.vm.ctrtasks;
 
 import java.io.File;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.admin.vbs.cube.common.Chronos;
 import ch.admin.vbs.cube.common.container.Container;
 import ch.admin.vbs.cube.common.container.IContainerFactory;
 import ch.admin.vbs.cube.common.keyring.EncryptionKey;
 import ch.admin.vbs.cube.common.keyring.IKeyring;
 import ch.admin.vbs.cube.core.I18nBundleProvider;
 import ch.admin.vbs.cube.core.ISession.IOption;
 import ch.admin.vbs.cube.core.network.vpn.VpnManager;
 import ch.admin.vbs.cube.core.network.vpn.VpnManager.VpnListener;
 import ch.admin.vbs.cube.core.vm.IVmProduct.VmProductState;
 import ch.admin.vbs.cube.core.vm.Vm;
 import ch.admin.vbs.cube.core.vm.VmController;
 import ch.admin.vbs.cube.core.vm.VmException;
 import ch.admin.vbs.cube.core.vm.VmModel;
 import ch.admin.vbs.cube.core.vm.VmState;
 import ch.admin.vbs.cube.core.vm.VmVpnState;
 import ch.admin.vbs.cube.core.vm.vbox.VBoxProduct;
 
 public class Start extends AbstractCtrlTask {
 	/** Logger */
 	private static final Logger LOG = LoggerFactory.getLogger(Start.class);
 	private static final long START_TIMEOUT = 40000; // ms
 	private static final long UNKNOWN_STATE_TIMEOUT = 4000; // ms
 
 	public Start(VmController vmController, IKeyring keyring, Vm vm, IContainerFactory containerFactory, VpnManager vpnManager, VBoxProduct product,
 			Container transfer, VmModel vmModel, IOption option) {
 		super(vmController, keyring, vm, containerFactory, vpnManager, product, transfer, vmModel, option);
 	}
 
 	@Override
 	public void run() {
 		EncryptionKey vmKey = null;
 		EncryptionKey rtKey = null;
 		try {
 			Chronos c = new Chronos();
 			vmModel.fireVmStateUpdatedEvent(vm);
 			c.zap("fireVmStateUpdateEvent()");
 			// set temporary status
 			ctrl.setTempStatus(vm, VmState.STARTING);
 			//
 			vm.setProgressMessage(I18nBundleProvider.getBundle().getString("vm.starting"));
 			ctrl.refreshVmState(vm);
 			c.zap("refresh vm state");
 			//
 			vmKey = keyring.getKey(vm.getVmContainer().getId());
 			c.zap("got vm's key");
 			rtKey = keyring.getKey(vm.getRuntimeContainer().getId());
 			c.zap("got runtime's key");
 			containerFactory.mountContainer(vm.getVmContainer(), vmKey);
 			c.zap("mounted vm container");
 			containerFactory.mountContainer(vm.getRuntimeContainer(), rtKey);
 			c.zap("mounted runtime container");
 			rtKey.shred(); // shred key asap
 			c.zap("shred runtime key");
 			vmKey.shred(); // shred key asap
 			c.zap("shred vm key");
 			// prepare transfer folders
 			File sessionTransferFolder = new File(transfer.getMountpoint(), vm.getId() + "_transfer");
 			vm.setTempFolder(new File(sessionTransferFolder, "temporary"));
 			vm.setImportFolder(new File(sessionTransferFolder, "import"));
 			vm.setExportFolder(new File(sessionTransferFolder, "export"));
 			vm.getTempFolder().mkdirs();
 			vm.getImportFolder().mkdirs();
 			vm.getExportFolder().mkdirs();
 			c.zap("configure vm object");
 			//
 			product.registerVm(vm); // register VM
 			c.zap("register vm");
 			vpnManager.openVpn(vm, keyring, new VpnListener() {
 				@Override
 				public void opened() {
 					try {
 						// could not connect when starting or restoring. wait
 						while (product.getProductState(vm) == VmProductState.STARTING) {
 							Thread.sleep(500);
 						}
 						// connect cable to trigger host's network manager
 						product.connectNic(vm, true);
 						//
 					} catch (Exception e) {
 						LOG.error("VM's VPN was opened but we failed to connect VM's NIC", e);
 						vm.setVpnState(VmVpnState.NOT_CONNECTED);
 						vmModel.fireVmStateUpdatedEvent(vm);
 					}
 					vm.setVpnState(VmVpnState.CONNECTED);
 					vmModel.fireVmStateUpdatedEvent(vm);
 				}
 				@Override
 				public void closed() {
 					try {
 						product.connectNic(vm, false);
 					} catch (VmException e) {
 						LOG.error("Failed to disconnect NIC",e);
 					}
 					vm.setVpnState(VmVpnState.NOT_CONNECTED);
 					vmModel.fireVmStateUpdatedEvent(vm);
 				}
 				@Override
 				public void connecting() {	
 					try {
 						product.connectNic(vm, false);
						vm.setVpnState(VmVpnState.CONNECTING);
 					} catch (VmException e) {
						LOG.error("Failed to connect NIC",e);
						vm.setVpnState(VmVpnState.NOT_CONNECTED);
 					}
 					vmModel.fireVmStateUpdatedEvent(vm);
 				}
 
 				@Override
 				public void failed() {
 					try {
 						product.connectNic(vm, false);
 					} catch (VmException e) {
 						LOG.error("VM's VPN failed and we failed to disconnect NIC", e);
 					}
 					vm.setVpnState(VmVpnState.NOT_CONNECTED);
 					vmModel.fireVmStateUpdatedEvent(vm);
 				}
 			}); // open vpn
 			c.zap("VPN opened (in another thred)");
 
 			product.startVm(vm); // start VM
 			c.zap("vm started");
 			/*
 			 * wait that the VM reach the state RUNNING. If it fails within the
 			 * timeout, remove the tempStatus flag and let VmContorller handle
 			 * it.
 			 */
 			boolean isUnknown = false;
 			long isUnknownSince = 0;
 			long timeout = System.currentTimeMillis() + START_TIMEOUT;
 			while (System.currentTimeMillis() < timeout) {
 				VmProductState ps = product.getProductState(vm);
 				if (ps == VmProductState.RUNNING || ps == VmProductState.ERROR) {
 					c.zap("Reached state ["+ps+"]");
 					break;
 				} else if (ps == VmProductState.UNKNOWN) {
 					if (isUnknown) {
 						// was already unknown. check timeout
 						if (isUnknownSince + UNKNOWN_STATE_TIMEOUT < System.currentTimeMillis()) {
 							/*
 							 * VM was too long in UNKNOWN state (VirtualBox vm
 							 * is 'saved' or 'stopped' which is normal between
 							 * the 'register' and 'start' commands or between
 							 * 'save/poweroff' and 'unregister' commands) but
 							 * which should last only few seconds. Do not wait
 							 * until START_TIMEOUT is reached and break asap.
 							 */
 							LOG.debug("VM was in UNKNOWN state for to many seconds. It is abnormal.");
 							break;
 						}
 					} else {
 						// just entered unknown state
 						isUnknown = true;
 						isUnknownSince = System.currentTimeMillis();
 						LOG.debug("Reached an unknown state (will break if VM stay in this state for too long).");
 					}
 				} else {
 					isUnknown = false;
 				}
 				LOG.debug("Wait VM to be RUNNING..");
 				Thread.sleep(500);
 			}
 			c.zap("VM reached state ["+product.getProductState(vm)+"]");
 			LOG.debug("Wait VM reached state [{}]", product.getProductState(vm));
 		} catch (Exception e) {
 			LOG.error("Failed to start VM", e);
 		} finally {
 			// shred keys
 			if (rtKey != null)
 				rtKey.shred(); // just in case..
 			if (vmKey != null)
 				vmKey.shred(); // just in case..			
 			ctrl.clearTempStatus(vm);
 			ctrl.refreshVmState(vm);
 		}
 	}
 }
