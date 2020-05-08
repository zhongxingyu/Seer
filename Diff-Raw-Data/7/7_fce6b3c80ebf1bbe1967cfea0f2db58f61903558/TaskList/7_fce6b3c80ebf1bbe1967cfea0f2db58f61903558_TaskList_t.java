 /*
  *  BlinzEngine - A library for large 2D world simultions and games.
  *  Copyright (C) 2009  Blinz <gtalent2@gmail.com>
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License version 3 as
  *  published by the Free Software Foundation.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.blinz.util.concurrency;
 
 import java.util.Vector;
 
 /**
  * A Task that contains other Tasks for execution.
  * @author Blinz
  */
 public final class TaskList extends Task {
 
     private class TaskManager extends OnePassTask {
 
         @Override
        protected void run() {
            for (int i = tasksToRemove.size() - 1; i > 0; i--) {
                 tasksToRemove.get(i).drop();
                 tasks.remove(tasksToRemove.remove(i));
             }
            for (int i = tasksToAdd.size() - 1; i > 0; i--) {
                 tasks.get(i).init(taskProcessor);
                 tasks.add(tasksToAdd.remove(i));
             }
         }
     }
     private TaskManager taskManager = new TaskManager();
     private Vector<Task> tasksToAdd = new Vector<Task>();
     private Vector<Task> tasksToRemove = new Vector<Task>();
     private Vector<Task> tasks = new Vector<Task>();
 
     /**
      * Adds the given Task to this TaskExecuter to be executed in the future.
      * @param task
      */
     public final void add(Task task) {
         tasksToAdd.add(task);
     }
 
     /**
      * Removes the given Task and it will no longer be executed in the future.
      * @param task
      */
     public final void remove(Task task) {
         tasksToRemove.add(task);
     }
 
     @Override
     protected void run() {
         if (!taskManager.moveOn) {
             taskManager.enter();
         }
 
         for (Task t : tasks) {
             if (!t.moveOn()) {
                 t.enter();
             }
         }
         for (Task t : tasks) {
             t.reset();
         }
     }
 }
