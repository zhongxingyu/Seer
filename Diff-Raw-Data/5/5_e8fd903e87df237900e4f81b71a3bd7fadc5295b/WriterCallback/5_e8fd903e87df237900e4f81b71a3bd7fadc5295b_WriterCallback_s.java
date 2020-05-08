 /*
  * This is a common dao with basic CRUD operations and is not limited to any 
  * persistent layer implementation
  * 
  * Copyright (C) 2008  Imran M Yousuf (imyousuf@smartitengineering.com)
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.smartitengineering.version.api.dao;
 
 import com.smartitengineering.version.api.Commit;
 
 /**
  * Since write operations will be performed in asynchronous threads, this call-
  * back will enable users to handle the write operation result.
  * @author imyousuf
  */
 public interface WriterCallback {
 
     /**
      * Handle the write operation result.
      * @param commit Commit that was attempted to written, commitId will be set
      *               if commit passes.
      * @param status Status of the write operation
      * @param comment Comment on the outcome of the right operation, in case the
      *                operation is successful it will either be blank (null or
      *                "") or status and if its unsuccessful it will be the cause.
      */
     public void handle(Commit commit,
                        WriteStatus status,
                       String comment);
 }
