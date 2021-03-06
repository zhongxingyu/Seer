 /*
  * Copyright (c) 2004-2013 Universidade do Porto - Faculdade de Engenharia
  * Laboratório de Sistemas e Tecnologia Subaquática (LSTS)
  * All rights reserved.
  * Rua Dr. Roberto Frias s/n, sala I203, 4200-465 Porto, Portugal
  *
  * This file is part of Neptus, Command and Control Framework.
  *
  * Commercial Licence Usage
  * Licencees holding valid commercial Neptus licences may use this file
  * in accordance with the commercial licence agreement provided with the
  * Software or, alternatively, in accordance with the terms contained in a
  * written agreement between you and Universidade do Porto. For licensing
  * terms, conditions, and further information contact lsts@fe.up.pt.
  *
  * European Union Public Licence - EUPL v.1.1 Usage
  * Alternatively, this file may be used under the terms of the EUPL,
  * Version 1.1 only (the "Licence"), appearing in the file LICENCE.md
  * included in the packaging of this file. You may not use this work
  * except in compliance with the Licence. Unless required by applicable
  * law or agreed to in writing, software distributed under the Licence is
  * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
  * ANY KIND, either express or implied. See the Licence for the specific
  * language governing permissions and limitations at
  * https://www.lsts.pt/neptus/licence.
  *
  * For more information please see <http://lsts.fe.up.pt/neptus>.
  *
  * Author: José Pinto
  * Nov 22, 2012
  */
 package pt.up.fe.dceg.neptus.console.plugins;
 
 import java.util.Iterator;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import pt.up.fe.dceg.neptus.NeptusLog;
 import pt.up.fe.dceg.neptus.console.ConsoleLayout;
 import pt.up.fe.dceg.neptus.console.events.ConsoleEventVehicleStateChanged;
 import pt.up.fe.dceg.neptus.console.events.ConsoleEventVehicleStateChanged.STATE;
 import pt.up.fe.dceg.neptus.i18n.I18n;
 import pt.up.fe.dceg.neptus.imc.Teleoperation;
 import pt.up.fe.dceg.neptus.imc.VehicleState;
 import pt.up.fe.dceg.neptus.imc.VehicleState.OP_MODE;
 import pt.up.fe.dceg.neptus.plugins.PluginDescription;
 import pt.up.fe.dceg.neptus.plugins.SimpleSubPanel;
 import pt.up.fe.dceg.neptus.plugins.update.IPeriodicUpdates;
 import pt.up.fe.dceg.neptus.util.comm.manager.imc.ImcSystemsHolder;
 
 import com.google.common.eventbus.Subscribe;
 
 /**
  * @author zp
  * 
  */
 @PluginDescription(name = "Vehicle State Monitor")
 public class VehicleStateMonitor extends SimpleSubPanel implements IPeriodicUpdates {
 
     private static final long serialVersionUID = 1L;
     protected ConcurrentMap<String, VehicleState> systemStates = new ConcurrentHashMap<String, VehicleState>();
 
     public VehicleStateMonitor(ConsoleLayout console) {
         super(console);
         setVisibility(false);
     }
 
     @Override
     public long millisBetweenUpdates() {
         return 1500;
     }
 
     @Override
     public boolean update() {
         Iterator<String> it = systemStates.keySet().iterator();
         while (it.hasNext()) {
             String system = it.next();
             try {
                 if (!ImcSystemsHolder.getSystemWithName(system).isActive()) {
                     systemStates.remove(system);
                     post(new ConsoleEventVehicleStateChanged(system, I18n.text("No communication received for more than 10 seconds"), STATE.DISCONNECTED));
                     console.getSystem(system).setVehicleState(STATE.DISCONNECTED);
                 }
             }
             catch (Exception e) {
                 NeptusLog.pub().debug(
                         VehicleStateMonitor.class.getSimpleName() + " for " + system + " gave an error: "
                                 + e.getMessage());
             }
         }
         return true;
     }
 
     @Subscribe
     public void consume(VehicleState msg) {
         String src = msg.getSourceName();
         if (src == null)
             return;
         String text = "";
         if (!msg.getLastError().isEmpty())
             text += msg.getLastError() + "\n";
         text += msg.getErrorEnts();
         VehicleState oldState = systemStates.get(src);
         if (oldState == null) {// first time
             post(new ConsoleEventVehicleStateChanged(src, text, STATE.valueOf(msg.getOpMode().toString())));
             console.getSystem(src).setVehicleState(STATE.valueOf(msg.getOpMode().toString()));
             systemStates.put(src, msg);
         }
         else {
             OP_MODE last = oldState.getOpMode();
             OP_MODE current = msg.getOpMode();
             if (last != current) {
                 systemStates.put(src, msg);
                 if (msg.getManeuverType() == Teleoperation.ID_STATIC) {
                     post(new ConsoleEventVehicleStateChanged(src, text, STATE.TELEOPERATION));
                     console.getSystem(src).setVehicleState(STATE.TELEOPERATION);
                 }
                if (last == OP_MODE.CALIBRATION && current == OP_MODE.SERVICE) {
                    return; // ignore
                }
                 else {
                     post(new ConsoleEventVehicleStateChanged(src, text, STATE.valueOf(msg.getOpMode().toString())));
                     console.getSystem(src).setVehicleState(STATE.valueOf(msg.getOpMode().toString()));
                 }
             }
         }
     }
 
     @Override
     public void initSubPanel() {
     }
 
     @Override
     public void cleanSubPanel() {
     }
 
 }
