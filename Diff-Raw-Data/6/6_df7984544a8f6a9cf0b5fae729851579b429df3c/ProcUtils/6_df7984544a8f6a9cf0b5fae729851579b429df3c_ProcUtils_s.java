 /**
  * Copyright (C) 2011 Ovea <dev@ovea.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.ovea.system.util;
 
 import com.sun.jna.Platform;
 import com.sun.jna.Pointer;
 import com.sun.jna.platform.win32.Kernel32;
 import com.sun.jna.platform.win32.WinNT;
 import org.hyperic.sigar.Sigar;
 import org.hyperic.sigar.SigarException;
 
 import java.lang.management.ManagementFactory;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * @author Mathieu Carbou (mathieu.carbou@gmail.com)
  */
 public final class ProcUtils {
 
     private ProcUtils() {
     }
 
     public static long pid(Process process) {
         String cName = process.getClass().getName();
         if (cName.equals("java.lang.UNIXProcess")) {
             /* get the PID on unix/linux systems */
             try {
                 Field f = process.getClass().getDeclaredField("pid");
                 f.setAccessible(true);
                 return f.getInt(process);
             } catch (Throwable e) {
                 throw new IllegalStateException("Unable to recover PID from Unix process" + process);
             }
         } else if (cName.equals("java.lang.ProcessImpl") || cName.equals("java.lang.Win32Process")) {
             /* determine the pid on windows plattforms */
             WinNT.HANDLE handle = new WinNT.HANDLE();
             try {
                 Field f = process.getClass().getDeclaredField("handle");
                 f.setAccessible(true);
                 long handl = f.getLong(process);
                 handle.setPointer(Pointer.createConstant(handl));
                 return Kernel32.INSTANCE.GetProcessId(handle);
             } catch (Throwable e) {
                 throw new IllegalStateException("Unable to recover PID from Windows process" + process);
             } finally {
                 Kernel32.INSTANCE.CloseHandle(handle);
             }
         } else {
             throw new IllegalArgumentException("Process type not supported: " + process.getClass().getName());
         }
     }
 
     public static long currentPID() {
         // lazy instanciation
         return RuntimePID.get();
     }
 
     public static void terminate(Process process) {
         long pid = pid(process);
         String cName = process.getClass().getName();
         if (cName.equals("java.lang.UNIXProcess")) {
             /* get the PID on unix/linux systems */
             try {
                 Method destroyProcess = process.getClass().getDeclaredMethod("destroyProcess", int.class);
                 destroyProcess.setAccessible(true);
                 destroyProcess.invoke(null, (int) pid);
             } catch (Throwable e) {
                 terminate(pid);
             }
         } else if (cName.equals("java.lang.ProcessImpl") || cName.equals("java.lang.Win32Process")) {
             /* determine the pid on windows plattforms */
             try {
                 Field f = process.getClass().getDeclaredField("handle");
                 f.setAccessible(true);
                 Method destroyProcess = process.getClass().getDeclaredMethod("terminateProcess", long.class);
                 destroyProcess.setAccessible(true);
                 destroyProcess.invoke(null, f.getLong(process));
             } catch (Throwable e) {
                 terminate(pid);
             }
         } else {
             throw new IllegalArgumentException("Process type not supported: " + process.getClass().getName());
         }
     }
 
     public static boolean exist(long pid) {
         try {
            SigarLoader.instance().getProcState(pid);
            return true;
         } catch (SigarException e) {
             return false;
         }
     }
 
     public static void terminate(long pid) {
         kill(pid, "SIGTERM");
     }
 
     public static void kill(long pid) {
         kill(pid, "SIGKILL");
     }
 
     public static void kill(long pid, String signal) {
         if (Platform.isWindows() || Platform.isWindowsCE()) {
             WinNT.HANDLE handle = Kernel32.INSTANCE.OpenProcess(0x0001, true, (int) pid);
             try {
                 if (handle != null) {
                     Kernel32.INSTANCE.TerminateProcess(handle, 0);
                 }
             } finally {
                 if (handle != null) {
                     Kernel32.INSTANCE.CloseHandle(handle);
                 }
             }
         } else {
             Sigar sigar = SigarLoader.instance();
             int n = Sigar.getSigNum(signal);
             try {
                 sigar.kill(pid, n);
             } catch (SigarException ignored) {
             }
         }
     }
 
     private static final class RuntimePID {
         private static final long pid;
 
         static {
             /* tested on: */
             /* - windows xp sp 2, java 1.5.0_13 */
             /* - mac os x 10.4.10, java 1.5.0 */
             /* - debian linux, java 1.5.0_13 */
             /* all return pid@host, e.g 2204@antonius */
             String processName = ManagementFactory.getRuntimeMXBean().getName();
             Matcher matcher = Pattern.compile("^([0-9]+)@.+$", Pattern.CASE_INSENSITIVE).matcher(processName);
             if (matcher.matches()) {
                 pid = Long.parseLong(matcher.group(1));
             } else {
                 throw new IllegalStateException("Unable to recover PID from process identifier: " + processName);
             }
         }
 
         public static long get() {
             return pid;
         }
     }
 
 }
