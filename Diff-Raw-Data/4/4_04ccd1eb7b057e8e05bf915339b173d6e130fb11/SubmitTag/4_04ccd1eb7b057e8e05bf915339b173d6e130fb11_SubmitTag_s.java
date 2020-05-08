 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.struts.taglib.html;
 
 import javax.servlet.jsp.JspException;
 
 import org.seasar.framework.util.Base64Util;
 import org.seasar.framework.util.IntegerConversionUtil;
 import org.seasar.framework.util.StringUtil;
 import org.seasar.struts.taglib.TagUtil;
 import org.seasar.struts.util.S2StrutsContextUtil;
 
 /**
  * @author Satoshi Kimura
  */
 public class SubmitTag extends org.apache.struts.taglib.html.SubmitTag {
     private static final long serialVersionUID = 3565695013866921990L;
 
     protected String indexId;
 
     protected String action;
 
     protected boolean cancel;
 
     public int doEndTag() throws JspException {
         setMethodBindingExpression();
         setCancelAction();
        return super.doEndTag();
     }
 
     public void release() {
         super.release();
         this.indexId = null;
         this.action = null;
         this.cancel = false;
     }
 
     /**
      * @return Returns the indexId.
      */
     public String getIndexId() {
         return this.indexId;
     }
 
     /**
      * @param indexId
      *            The indexName to set.
      */
     public void setIndexId(String indexId) {
         this.indexId = indexId;
 
         if (indexId != null) {
             super.indexed = true;
         }
     }
 
     public String getAction() {
         return this.action;
     }
 
     public void setAction(String action) {
         this.action = action;
     }
 
     public boolean isCancel() {
         return this.cancel;
     }
 
     public void setCancel(boolean cancel) {
         this.cancel = cancel;
     }
 
     protected void prepareIndex(StringBuffer handlers, String name) throws JspException {
         if (this.indexId == null) {
             super.prepareIndex(handlers, name);
             return;
         }
         Object index = super.pageContext.getAttribute(this.indexId);
         handlers.append("[");
         handlers.append(IntegerConversionUtil.toPrimitiveInt(index));
         handlers.append("]");
     }
 
     protected void setMethodBindingExpression() throws JspException {
         if (StringUtil.isEmpty(this.action)) {
             return;
         }
         if (StringUtil.isEmpty(super.property)) {
             super.property = Base64Util.encode(this.action.getBytes());
         }
         String val = super.value;
         if (val == null) {
             val = super.text;
         }
         if (val == null) {
             val = getDefaultValue();
         }
         String mappingName = TagUtil.getActionMappingName(this.pageContext);
         S2StrutsContextUtil.setMethodBindingExpression(mappingName, super.property, val, this.action);
     }
 
     protected void setCancelAction() throws JspException {
         if (!this.cancel) {
             return;
         }
 
         String val = super.value;
         if (val == null) {
             val = super.text;
         }
         if (val == null) {
             val = getDefaultValue();
         }
         String mappingName = TagUtil.getActionMappingName(this.pageContext);
         S2StrutsContextUtil.setCancelAction(mappingName, super.property, val);
     }
 
 }
