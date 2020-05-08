 /*
  * This file is part of Disconnected.
  * Copyright (c) 2013 QuarterCode <http://www.quartercode.com/>
  *
  * Disconnected is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Disconnected is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Disconnected. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.quartercode.disconnected.sim.comp;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import javax.xml.bind.annotation.XmlElement;
 import com.quartercode.disconnected.sim.comp.Vulnerability.Vulnerable;
 import com.quartercode.disconnected.sim.comp.media.File;
 import com.quartercode.disconnected.sim.comp.media.File.FileType;
 import com.quartercode.disconnected.sim.comp.media.MediaProvider;
 import com.quartercode.disconnected.sim.comp.net.Address;
 import com.quartercode.disconnected.sim.comp.net.Packet;
 import com.quartercode.disconnected.sim.comp.program.Process;
 import com.quartercode.disconnected.sim.comp.program.Program;
 
 /**
  * This class stores information about an operating system.
  * This also contains a list of all vulnerabilities this operating system has.
  * 
  * @see HostedComputerPart
  * @see Desktop
  * @see Vulnerability
  */
 public class OperatingSystem extends HostedComputerPart implements Vulnerable {
 
     /**
      * This enum represents the right levels a user can has on an operating system.
      * The right level defines what a user can or cannot do. If a user has a right level, he can use every other right level below his one.
      * 
      * @see OperatingSystem
      * @see Program
      */
     public static enum RightLevel {
 
         /**
          * A guest only has a minimum of rights. You can compare a guest access with a kiosk mode for operating systems.
          */
         GUEST,
         /**
          * A user is typically using installed applications, but he doesn't modify the computer or os in any way.
          */
         USER,
         /**
          * An adiministrator modifies the computer or the os, for example he can install programs or change system properties.
          */
         ADMIN,
         /**
          * The system authority is the superuser on the os and can do everything the os provides.
          */
         SYSTEM;
     }
 
     private static final long   serialVersionUID = 1L;
 
     @XmlElement (name = "vulnerability")
     private List<Vulnerability> vulnerabilities  = new ArrayList<Vulnerability>();
 
    @XmlElement
     private Process             rootProcess;
     private Desktop             desktop;
 
     /**
      * Creates a new empty operating system.
      * This is only recommended for direct field access (e.g. for serialization).
      */
     protected OperatingSystem() {
 
     }
 
     /**
      * Creates a new operating system and sets the host computer, the name, the version, the vulnerabilities and the times the os needs for switching on/off.
      * 
      * @param host The host computer this part is built in.
      * @param name The name the operating system has.
      * @param version The current version the operating system has.
      * @param vulnerabilities The vulnerabilities the operating system has.
      */
     public OperatingSystem(Computer host, String name, Version version, List<Vulnerability> vulnerabilities) {
 
         super(host, name, version);
 
         this.vulnerabilities = vulnerabilities == null ? new ArrayList<Vulnerability>() : vulnerabilities;
        rootProcess = new Process(this, null, 0, getFile("C:/bin/kernel"), null);
         desktop = new Desktop(this);
     }
 
     @Override
     public List<Vulnerability> getVulnerabilities() {
 
         return Collections.unmodifiableList(vulnerabilities);
     }
 
     /**
      * Returns the root process with the pid 0.
      * The root process gets started by the os kernel.
      * 
      * @return The root process with the pid 0.
      */
     public Process getRootProcess() {
 
         return rootProcess;
     }
 
     /**
      * Returns a list of all current running processes.
      * 
      * @return A list of all current running processes.
      */
     public List<Process> getAllProcesses() {
 
         List<Process> processes = new ArrayList<Process>();
         processes.add(rootProcess);
         processes.addAll(rootProcess.getAllChildren());
         return processes;
     }
 
     /**
      * Returns the process which is bound to the given address.
      * This returns null if there isn't any process with the given binding.
      * 
      * @param binding The address to inspect.
      * @return The process which binds itself to the given address.
      */
     public Process getProcess(Address binding) {
 
         for (Process process : getAllProcesses()) {
             if (process.getExecutor().getPacketListener(binding) != null) {
                 return process;
             }
         }
 
         return null;
     }
 
     /**
      * Calculates and returns a new pid for a new process.
      * 
      * @return The calculated pid.
      */
     public synchronized int requestPid() {
 
         List<Integer> pids = new ArrayList<Integer>();
         for (Process process : getAllProcesses()) {
             pids.add(process.getPid());
         }
         int pid = 0;
         while (pids.contains(pid)) {
             pid++;
         }
         return pid;
     }
 
     /**
      * Returns the desktop the os displays.
      * The desktop displays windows which can be opened by programs.
      * 
      * @return The desktop the os displays.
      */
     public Desktop getDesktop() {
 
         return desktop;
     }
 
     /**
      * Returns a list containing all media which are connected to this computer.
      * 
      * @return A list containing all media which are connected to this computer.
      */
     public List<MediaProvider> getMedia() {
 
         return getHost().getHardware(MediaProvider.class);
     }
 
     /**
      * Returns the connected media which uses the given letter.
      * If there is no media with the given letter, this will return null.
      * 
      * @param letter The letter the returned media needs to use.
      * @return The connected media which uses the given letter.
      */
     public MediaProvider getMedia(char letter) {
 
         for (MediaProvider media : getMedia()) {
             if (media.getLetter() == letter) {
                 return media;
             }
         }
 
         return null;
     }
 
     /**
      * Returns the connected media on which the file under the given path is stored.
      * A path is a collection of files seperated by a seperator.
      * This requires a global os path.
      * If there is no media the file is stored on, this will return null.
      * 
      * @param path The file represented by this path is stored on the returned media.
      * @return The connected media on which the file under the given path is stored.
      */
     public MediaProvider getMedia(String path) {
 
         if (path.contains(":")) {
             return getMedia(path.split(":")[0].charAt(0));
         } else {
             return null;
         }
     }
 
     /**
      * Returns the file which is stored on a media of the computer this os is running on under the given path.
      * A path is a collection of files seperated by a seperator.
      * This will look up the file using a global os path.
      * 
      * @param path The path to look in for the file.
      * @return The file which is stored on a media of the computer this os is running on under the given path.
      */
     public File getFile(String path) {
 
         MediaProvider media = getMedia(path);
         if (media != null) {
             return media.getFile(path.split(":")[1]);
         } else {
             return null;
         }
     }
 
     /**
      * Creates a new file using the given path and type on this computer and returns it.
      * If the file already exists, the existing file will be returned.
      * A path is a collection of files seperated by a seperator.
      * This will get the file location using a global os path.
      * 
      * @param path The path the new file will be located under.
      * @param type The file type the new file should has.
      * @return The new file (or the existing one, if the file already exists).
      */
     public File addFile(String path, FileType type) {
 
         MediaProvider media = getMedia(path);
         if (media != null) {
             return media.addFile(path.split(":")[1], type);
         } else {
             return null;
         }
     }
 
     /**
      * Sends a new packet from the sender to the receiver address of the given packet.
      * 
      * @param packet The packet to send.
      */
     public void sendPacket(Packet packet) {
 
         packet.getSender().getIp().getHost().sendPacket(packet);
     }
 
     /**
      * This method takes an incoming packet and distributes it to the target port.
      * 
      * @param packet The packet which came in and called the method.
      */
     public void handlePacket(Packet packet) {
 
         if (getProcess(packet.getReceiver()) != null) {
             getProcess(packet.getReceiver()).getExecutor().receivePacket(packet);
         }
     }
 
     @Override
     public int hashCode() {
 
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + (rootProcess == null ? 0 : rootProcess.hashCode());
         result = prime * result + (vulnerabilities == null ? 0 : vulnerabilities.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
 
         if (this == obj) {
             return true;
         }
         if (!super.equals(obj)) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         OperatingSystem other = (OperatingSystem) obj;
         if (rootProcess == null) {
             if (other.rootProcess != null) {
                 return false;
             }
         } else if (!rootProcess.equals(other.rootProcess)) {
             return false;
         }
         if (vulnerabilities == null) {
             if (other.vulnerabilities != null) {
                 return false;
             }
         } else if (!vulnerabilities.equals(other.vulnerabilities)) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toInfoString() {
 
         return super.toInfoString() + ", " + vulnerabilities.size() + " vulns";
     }
 
     @Override
     public String toString() {
 
         return getClass().getName() + "[" + toInfoString() + "]";
     }
 
 }
