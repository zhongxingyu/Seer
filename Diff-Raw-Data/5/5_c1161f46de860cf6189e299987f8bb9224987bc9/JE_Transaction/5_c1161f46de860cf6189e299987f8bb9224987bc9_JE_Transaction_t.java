 /*
  * Copyright 2009-2010 Amazon Technologies, Inc. or its affiliates.
  * Amazon, Amazon.com and Carbonado are trademarks or registered trademarks
  * of Amazon Technologies, Inc. or its affiliates.  All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.amazon.carbonado.repo.sleepycat;
 
 import com.sleepycat.je.DatabaseException;
 import com.sleepycat.je.Environment;
 import com.sleepycat.je.OperationStatus;
 import com.sleepycat.je.Transaction;
 
 /**
  * Allows BDB-JE to support nested rollback by keeping an undo log in memory.
  *
  * @author Brian S O'Neill
  */
 class JE_Transaction {
     final JE_Transaction mParent;
     final Transaction mTxn;
 
     private UndoAction mUndoLog;
 
     JE_Transaction(Transaction txn) {
         mParent = null;
         mTxn = txn;
     }
 
     JE_Transaction(JE_Transaction parent) {
         mParent = parent;
         mTxn = parent.mTxn;
     }
 
     JE_Transaction createChild() throws DatabaseException {
         return new JE_Transaction(this);
     }
 
     JE_Transaction createChild(Environment env, long timeout) throws DatabaseException {
         return new JE_TimeoutTransaction(this, env, timeout);
     }
 
     void addUndo(final UndoAction action) {
         if (action != null && mParent != null) {
             final UndoAction prev = mUndoLog;
             if (prev == null) {
                 mUndoLog = action;
             } else {
                 mUndoLog = new UndoAction() {
                     public void apply() throws DatabaseException {
                         action.apply();
                         prev.apply();
                     }
                 };
             }
         }
     }
 
     void abort() throws DatabaseException {
         if (mParent == null) {
             mTxn.abort();
         } else {
             UndoAction undo = mUndoLog;
             if (undo != null) {
                 undo.apply();
             }
         }
     }
 
     void commit() throws DatabaseException {
         if (mParent == null) {
             mTxn.commit();
         } else {
             mParent.addUndo(mUndoLog);
         }
     }
 
     void commitSync() throws DatabaseException {
         if (mParent == null) {
             mTxn.commitSync();
         } else {
             mParent.addUndo(mUndoLog);
         }
     }
 
     void commitNoSync() throws DatabaseException {
        if (mParent == null) {
             mTxn.commitNoSync();
         } else {
             mParent.addUndo(mUndoLog);
         }
     }
 
     void commitWriteNoSync() throws DatabaseException {
        if (mParent == null) {
             mTxn.commitWriteNoSync();
         } else {
             mParent.addUndo(mUndoLog);
         }
     }
 
     void setTxnTimeout(long timeout) throws DatabaseException {
         mTxn.setTxnTimeout(timeout);
     }
 
     void setLockTimeout(long timeout) throws DatabaseException {
         if (timeout <= 0) {
             timeout = timeout < 0 ? 0 : 1;
         }
         mTxn.setLockTimeout(timeout);
     }
 
     long getId() throws DatabaseException {
         return mTxn.getId();
     }
 
     void setName(String name) {
         mTxn.setName(name);
     }
 
     String getName() {
         return mTxn.getName();
     }
 
     public String toString() {
         return mTxn.toString();
     }
 
     static interface UndoAction {
         void apply() throws DatabaseException;
     }
 }
