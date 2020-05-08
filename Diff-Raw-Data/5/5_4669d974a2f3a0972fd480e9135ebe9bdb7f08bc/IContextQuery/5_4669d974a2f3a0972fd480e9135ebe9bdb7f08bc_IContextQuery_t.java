 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.services;
 
 import org.eclipse.tcf.protocol.IService;
 import org.eclipse.tcf.protocol.IToken;
 
 /**
  * ContextQuery allows to search for context that match a pattern.
  *
  * Query Syntax and Semantics
  *
  *        query = [ "/" ], { part, "/" }, part ;
  *        part = string | "*" | "**" | properties ;
  *        properties = property, { ",", property } ;
  *        property = string, "=", value ;
  *        value = string | number | boolean ;
  *        string = quoted string | symbol ;
  *        quoted string = '"', {any-character - ('"' | '\') | ('\', ('"' | '\'))}, '"' ;
  *        symbol = letter, { letter | digit } ;
  *        number = digit, { digit } ;
  *        boolean = "true" | "false" ;
  *        letter = ? A-Z, a-z or _ ? ;
  *        digit = ? 0-9 ? ;
  *        any-character = ? any character ? ;
  *
  * To give a feel for the syntax, here are some examples, and what a user
  * might mean when providing such a query:
  *
  * httpd
  *        Matches all contexts named "httpd".
  *
  * pid=4711
  *        Matches any context with a property pid, which has the value 4711.
  *
  * /server/**
 *        Matches all contexts which are descendants of the top level context
  *        named "server".
  *
  * "Linux 2.6.14"/Kernel/*
  *       Matches all kernel processes in operating systems named "Linux 2.6.14".
  *
  * pid=4711/*
  *        All threads in processes with the pid 4711.
  *
  * /server/** /HasState=true
 *        All threads which are descendants of the context "server".
  *
  * The contexts are assumed to be placed in a tree. Each context has zero
  * or one parent. If it has zero parents it is a child of the root of the
  * tree.
  *
  * A query consists of a sequence of parts separated by "/". This
  * sequence specifies a path through the context tree. A context matches
  * the query if the last part of the query matches the properties of the
  * context and the parent of the context matches the query excluding the
  * last part. The properties of a context matches a part if each property
  * specified in the part matches the property of the same name in the
  * context or if the name of the context matches the string specified in
  * the part. There are also two wild cards. The part "*" matches any
  * context. The part "**" matches any sequence of contexts. If the query
  * starts with a "/" the first part of the query must match a child of
  * the root of the context tree.
  */
 public interface IContextQuery extends IService {
 
     /**
      * Service name.
      */
     static final String NAME = "ContextQuery";
 
     /**
      * Execute a context query and return array of matching context IDs.
      * @param query - context query string.
      * @param done - command result call back object.
      * @return - pending command handle.
      */
     IToken query(String query, DoneQuery done);
 
     /**
      * Call back interface for 'query' command.
      */
     interface DoneQuery {
         /**
          * Called when 'query' command is done.
          * @param token - command handle.
          * @param error - error object or null.
          * @param contexts - array of context IDs.
          */
         void doneQuery(IToken token, Exception error, String[] contexts);
     }
 
     /**
      * Get list of attribute names available for context queries.
      * @param done - command result call back object.
      * @return - pending command handle.
      */
     IToken getAttrNames(DoneGetAttrNames done);
 
     /**
      * Call back interface for 'getAttrNames' command.
      */
     interface DoneGetAttrNames {
         /**
          * Called when 'query' command is done.
          * @param token - command handle.
          * @param error - error object or null.
          * @param contexts - array of context IDs.
          */
         void doneGetAttrNames(IToken token, Exception error, String[] names);
     }
 }
