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
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.admin.vbs.cube.common.container.Container;
 import ch.admin.vbs.cube.common.container.IContainerFactory;
 import ch.admin.vbs.cube.common.keyring.IKeyring;
 import ch.admin.vbs.cube.core.I18nBundleProvider;
 import ch.admin.vbs.cube.core.ISession.IOption;
 import ch.admin.vbs.cube.core.network.vpn.VpnManager;
 import ch.admin.vbs.cube.core.vm.Vm;
 import ch.admin.vbs.cube.core.vm.VmController;
 import ch.admin.vbs.cube.core.vm.VmModel;
 import ch.admin.vbs.cube.core.vm.VmStatus;
 import ch.admin.vbs.cube.core.vm.vbox.VBoxProduct;
 
 public class PowerOff extends AbstractCtrlTask {
 	/** Logger */
 	private static final Logger LOG = LoggerFactory.getLogger(PowerOff.class);
 
 	public PowerOff(VmController vmController, IKeyring keyring, Vm vm, IContainerFactory containerFactory, VpnManager vpnManager, VBoxProduct product,
 			Container transfer, VmModel vmModel, IOption option) {
 		super(vmController, keyring, vm, containerFactory, vpnManager, product, transfer, vmModel, option);
 	}
 
 	@Override
 	public void run() {
 		// set temporary status
 		ctrl.setTempStatus(vm, VmStatus.STOPPING);
 		vm.setProgressMessage(I18nBundleProvider.getBundle().getString("vm.stopping"));
 		ctrl.refreshVmStatus(vm);
 		// stop VM
 		try {
 			product.poweroffVm(vm, vmModel);
 		} catch (Exception e) {
 			LOG.error("Failed to poweroff VM", e);
 		}
 		// stop VPN
 		try {
 			vpnManager.closeVpn(vm);
 		} catch (Exception e) {
 			LOG.error("Failed to stop VPN", e);
 		}
 		// unregister VM
 		try {
 			product.unregisterVm(vm);
 		} catch (Exception e) {
 			LOG.error("Failed to unregister VM", e);
 		}
 		// unmount containers
 		try {
 			containerFactory.unmountContainer(vm.getVmContainer());
 		} catch (Exception e) {
 			LOG.error("Failed to unmount VM's containers", e);
 		}
 		try {
 			containerFactory.unmountContainer(vm.getRuntimeContainer());
 		} catch (Exception e) {
 			LOG.error("Failed to unmount VM's containers", e);
 		}
 		ctrl.clearTempStatus(vm);
 		ctrl.refreshVmStatus(vm);
 	}
 }
