 package com.github.marschall.punch.jdbc;
 
 /**
  * Classes to manage tasks on a database.
  *
  * Concepts:
  *  - recovery of failed/unfinished tasks is possible after a VM crash/restart
  *
  * Things to consider:
  *
  * Open issues:
 * - DatabaseRecoveryService: Tasks that were finished after the first call of getFinishedTasks() won't be found
  */
