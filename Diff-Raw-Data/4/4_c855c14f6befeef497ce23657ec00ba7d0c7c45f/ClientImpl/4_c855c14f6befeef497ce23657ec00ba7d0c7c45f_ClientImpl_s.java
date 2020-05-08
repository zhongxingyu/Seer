 package org.jegrid.impl;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.spi.LoggingEvent;
 import org.jegrid.*;
 import org.jegrid.log.LogEventPump;
 
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * The client - manages a list of tasks.
  * <br> User: jdavis
  * Date: Sep 30, 2006
  * Time: 7:49:52 AM
  */
 public class ClientImpl implements ClientImplementor
 {
     private static Logger log = Logger.getLogger(ClientImpl.class);
 
     private int nextTaskId = 1000;
    private final Map tasksById = new HashMap();
     private Bus bus;
     private GridImplementor grid;
     private Comparator serverComparator;
     private LogEventPump logPump;
     private static final int DEFAULT_BACKGROUND_TIMEOUT = 10000;
 
     public ClientImpl(Bus bus, GridImplementor grid)
     {
         this.bus = bus;
         this.grid = grid;
         serverComparator = new ServerComparator();
         logPump = new LogEventPump();
     }
 
     private int nextTaskId()
     {
         synchronized (this)
         {
             return nextTaskId++;
         }
     }
 
     public Bus getBus()
     {
         return bus;
     }
 
     NodeAddress[] getSeverAddresses(int max)
     {
         // Get the cached status.
         GridStatus status = grid.getGridStatus(true);
         Iterator iter = status.iterator();
         List list = new LinkedList();
         while (iter.hasNext())
         {
             NodeStatus node = (NodeStatus) iter.next();
             if (node.getType() == Grid.TYPE_SERVER && node.getAvailableWorkers() > 0)
                 list.add(node);
         }
         if (list.size() == 0)
             return new NodeAddress[0];
         // Sort the list, put the most capable servers first.
         Collections.sort(list, serverComparator);
 
         // If max < 0 that means get all the servers.
         // If max > 0 don't get more than 'max' servers.
         if (max > 0)
             list = list.subList(0, Math.min(list.size(), max));
 
         NodeAddress[] rv = new NodeAddress[list.size()];
         int i = 0;
         for (Iterator iterator = list.iterator(); iterator.hasNext();)
         {
             NodeStatus nodeStatus = (NodeStatus) iterator.next();
             rv[i] = nodeStatus.getNodeAddress();
             i++;
         }
         return rv;
     }
 
     private void addTask(TaskImpl task)
     {
         synchronized (tasksById)
         {
             tasksById.put(task.getTaskId(), task);
         }
     }
 
     public Task createTask(Serializable taskKey)
     {
         TaskImpl task = new TaskImpl(grid, this, nextTaskId(), null, taskKey);
         addTask(task);
         return task;
     }
 
     public NodeAddress[] waitForServers(int max, int min, long timeout) throws InterruptedException
     {
         long start = System.currentTimeMillis();
         boolean loop = true;
         NodeAddress[] addresses = null;
         while (loop)
         {
             addresses = getSeverAddresses(max);
             int count = (addresses == null) ? 0 : addresses.length;
             if (count < min)
             {
                 // Wait for a membership change.
                 if (timeout == WAIT_FOREVER)
                     grid.waitForServers();
                 else if (timeout == NO_WAIT)
                     loop = false;
                 else
                 {
                     long remaining = timeout - (start - System.currentTimeMillis());
                     if (log.isDebugEnabled())
                         log.debug("waitForServers() count=" + count + " remaining=" + remaining);
                     if (remaining <= 0)
                         loop = false;
                     else
                         grid.waitForServers();
                 }
             }
             else
                 loop = false;
         }
         return addresses;
     }
 
     public void background(TaskRequest request, long timeout)
     {
         boolean loop = true;
         while (loop)
         {
             try
             {
                 NodeAddress[] addresses = waitForServers(-1, 1, WAIT_FOREVER);
                 if (addresses != null)
                 {
                     // Try to assign a worker thread to this task.
                     for (int i = 0; i < addresses.length; i++)
                     {
                         NodeAddress address = addresses[i];
                         if (log.isDebugEnabled())
                             log.debug("background() : trying " + address);
                         AssignResponse response = bus.assignTask(address, request);
                         if (response != null)
                         {
                             if (response.accepted())
                             {
                                 log.info("Background task accepted by " + address);
                                 loop = false;
                                 break;
                             }
                         }
                     }
                 }
                 // If loop is still true, that means no server accepted the work so continue...
                 log.info("No servers accepted, retrying...");
             }
             catch (RpcTimeoutException e)
             {
                 log.warn(e, e);
             }
             catch (InterruptedException e)
             {
                 throw new GridException(e);
             }
         }
     }
 
     public TaskData getNextInput(TaskId taskId, NodeAddress server, TaskData output)
     {
         TaskImpl task = findTask(taskId);
         if (task == null)
         {
             if (log.isDebugEnabled())
                 log.debug("getNextInput() : No task " + taskId);
             return null;
         }
         return task.getNextInput(server, output);
     }
 
     private TaskImpl findTask(TaskId taskId)
     {
         TaskImpl task;
         synchronized (tasksById)
         {
             task = (TaskImpl) tasksById.get(taskId);
         }
         return task;
     }
 
     public void taskFailed(TaskId taskId, GridException throwable)
     {
         TaskImpl task = findTask(taskId);
         if (task == null)
         {
             if (log.isDebugEnabled())
                 log.debug("taskFailed() : No task " + taskId);
             return;
         }
         task.onFailure(throwable);
     }
 
     public void onMembershipChange(Set joined, Set left)
     {
         synchronized (tasksById)
         {
             for (Iterator iterator = tasksById.values().iterator(); iterator.hasNext();)
             {
                 TaskImpl task = (TaskImpl) iterator.next();
                 task.onMembershipChange(joined, left);
             }
         }
     }
 
     public void append(TaskId taskId, LoggingEvent event)
     {
         // Put the loggging event from the remote node onto the queue.
         logPump.append(event);
     }
 
     public void onComplete(TaskImpl task)
     {
         synchronized (tasksById)
         {
             tasksById.remove(task.getTaskId());
             if (log.isDebugEnabled())
                 log.info("Task complete: " + task.getTaskId());
         }
     }
 }
