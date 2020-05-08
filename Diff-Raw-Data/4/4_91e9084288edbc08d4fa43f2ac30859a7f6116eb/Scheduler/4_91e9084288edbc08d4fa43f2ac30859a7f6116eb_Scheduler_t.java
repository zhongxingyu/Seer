 package com.austingulati.opsys.project1;
 
 import java.util.ArrayList;
 import java.util.List;
 
 abstract class Scheduler
 {
     List<Process> processes = new ArrayList<Process>(), finishedProcesses = new ArrayList<Process>();
     Process currentProcess = null;
     Integer time = 0, waitingTime = 0, maxTime = 0, averageTime = 0, minTime = 0;
 
     public void addProcess(Process process)
     {
         processes.add(new Process(process));
     }
 
     public void addProcesses(List<Process> processes)
     {
         for(Process process : processes)
         {
             addProcess(process);
         }
     }
 
     public Boolean hasProcesses()
     {
         return processes.size() > 0;
     }
 
     // Returns the current process
     abstract Process getProcess();
 
     // Returns the name of the scheduler
     abstract String getName();
 
     // Increments to the next step
     public void tick()
     {
         if(getWaitingTime() > 0)
         {
             setWaitingTime(getWaitingTime() - 1);
             for(Process process : processes)
             {
                 // All processes are paused
                 process.pause();
             }
             return;
         }
 
         if(hasCurrentProcess())
         {
             getCurrentProcess().run();
             if(getCurrentProcess().getTimeRemaining() == 0)
             {
                 Process lastProcess = getCurrentProcess();
                 System.out.printf("[time %dms] Process %d completed its CPU burst (turnaround time %dms, initial wait time %dms, total wait time %dms)\n", time, lastProcess.getId(), lastProcess.getTimeTotal(), lastProcess.getTimeInitiallyWaiting(), lastProcess.getTimeWaiting());
                 removeProcess(lastProcess);
                 setCurrentProcess(null);
             }
         }
 
         Process nextProcess = getProcess();
         if(getCurrentProcess() == null && nextProcess != null)
         {
             System.out.printf("[time %dms] Process %d created (requires %dms CPU time)\n", time, nextProcess.getId(), nextProcess.getTimeRemaining());
         }
         setCurrentProcess(nextProcess);
 
         for(Process process : processes)
         {
             if(getCurrentProcess() == null || process.getId() != getCurrentProcess().getId())
             {
                 process.pause();
             }
         }
 
         time++;
     }
 
     public Boolean hasCurrentProcess()
     {
         return currentProcess != null;
     }
 
     public Process getCurrentProcess()
     {
         return currentProcess;
     }
 
     public void setCurrentProcess(Process process)
     {
         this.currentProcess = process;
     }
 
     public Integer getTime()
     {
         return time;
     }
 
     public void setWaitingTime(Integer waitingTime)
     {
         this.waitingTime = waitingTime;
     }
 
     public Integer getWaitingTime()
     {
         return waitingTime;
     }
 
     public void removeProcess(Process process)
     {
         processes.remove(process);
         finishedProcesses.add(process);
     }
 
     public void printResults()
     {
         int minTurnaroundTime = Integer.MAX_VALUE;
         int avgTurnaroundTime = 0;
         int maxTurnaroundTime = 0;
 
         int minInitialWaitTime = Integer.MAX_VALUE;
         int avgInitialWaitTime = 0;
         int maxInitialWaitTime = 0;
         
         int minTotalWaitTime = Integer.MAX_VALUE;
         int avgTotalWaitTime = 0;
         int maxTotalWaitTime = 0;
 
         for(Process process : finishedProcesses)
         {
             avgTurnaroundTime += process.getTimeTotal();
             avgInitialWaitTime += process.getTimeInitiallyWaiting();
             avgTotalWaitTime += process.getTimeWaiting();
 
 
             if (process.getTimeTotal() > maxTurnaroundTime)
             {
                 maxTurnaroundTime = process.getTimeTotal();
             }
             else if (process.getTimeTotal() < minTurnaroundTime)
             {
                 // Not working for the Shortest Job First schedulers, got a soccer game
                 // can someone look into this?
                 System.out.printf("getTimeTotal() is %dms\n", process.getTimeTotal());
                 minTurnaroundTime = process.getTimeTotal();
             }
 
             if (process.getTimeInitiallyWaiting() > maxInitialWaitTime)
             {
                 maxInitialWaitTime = process.getTimeInitiallyWaiting();
             }
             else if (process.getTimeInitiallyWaiting() < minInitialWaitTime)
             {
                 minInitialWaitTime = process.getTimeInitiallyWaiting();
             }
 
             if (process.getTimeWaiting() > maxTotalWaitTime)
             {
                 maxTotalWaitTime = process.getTimeWaiting();
             }
             else if (process.getTimeWaiting() < minTotalWaitTime)
             {
                 minTotalWaitTime = process.getTimeWaiting();
             }
         }
 
         int finishedProcessesSize = finishedProcesses.size();
         avgTurnaroundTime /= finishedProcessesSize;
        avgInitialWaitTime /= finishedProcessesSize;
         avgTotalWaitTime /= finishedProcessesSize;
 
         System.out.printf("Number of CPUs: %d\n", Main.M);
         System.out.printf("Turnaround time: min %dms ms; avg %dms ms; max %dms ms\n", minTurnaroundTime, avgTurnaroundTime, maxTurnaroundTime);
         System.out.printf("Initial wait time: min %dms ms; avg %dms ms; max %dms ms\n", minInitialWaitTime, avgInitialWaitTime, maxInitialWaitTime);
         System.out.printf("Total wait time: min %dms ms; avg %dms ms; max %dms ms\n\n", minTotalWaitTime, avgTotalWaitTime, maxTotalWaitTime);
     }
 }
