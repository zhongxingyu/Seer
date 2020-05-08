 // Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
 // for details. All rights reserved. Use of this source code is governed by a
 // BSD-style license that can be found in the LICENSE file.
 
 package com.google.dart.compiler.backend.js.ast;
 
 public class JsCatch extends SourceInfoAwareJsNode implements HasCondition {
    protected final JsCatchScope scope;
     private JsBlock body;
     private JsExpression condition;
    private JsParameter parameter;
 
    public JsCatch(JsScope parent, String ident) {
         super();
        assert (parent != null);
        parent.declareFreshName(ident);
        scope = new JsCatchScope(parent, ident);
        parameter = new JsParameter(scope.findName(ident));
     }
 
     public JsBlock getBody() {
         return body;
     }
 
     @Override
     public JsExpression getCondition() {
         return condition;
     }
 
     public JsParameter getParameter() {
        return parameter;
    }

    public JsScope getScope() {
        return scope;
     }
 
     public void setBody(JsBlock body) {
         this.body = body;
     }
 
     @Override
     public void setCondition(JsExpression condition) {
         this.condition = condition;
     }
 
     @Override
     public void accept(JsVisitor v) {
         v.visitCatch(this);
     }
 
     @Override
     public void acceptChildren(JsVisitor visitor) {
        visitor.accept(parameter);
         if (condition != null) {
             visitor.accept(condition);
         }
         visitor.accept(body);
     }
 }
