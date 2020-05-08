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
 package org.seasar.teeda.core.taglib.core;
 
 import java.io.IOException;
 import java.util.Locale;
 
 import javax.faces.application.ViewHandler;
 import javax.faces.component.UIViewRoot;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.tagext.BodyContent;
 import javax.servlet.jsp.tagext.BodyTag;
 import javax.servlet.jsp.tagext.Tag;
 
 import org.seasar.framework.mock.servlet.MockHttpServletResponse;
 import org.seasar.teeda.core.mock.MockBodyContent;
 import org.seasar.teeda.core.mock.MockFacesContextImpl;
 import org.seasar.teeda.core.mock.MockJspWriter;
 import org.seasar.teeda.core.mock.MockPageContext;
 import org.seasar.teeda.core.mock.MockResponseWriter;
 import org.seasar.teeda.core.unit.ExceptionAssert;
 import org.seasar.teeda.core.unit.TeedaTestCase;
 
 /**
  * @author yone
  */
 public class ViewTagTest extends TeedaTestCase {
 
     public void testGetComponentType() throws Exception {
         // # Arrange #
         ViewTag tag = new ViewTag();
 
         // # Act & Assert #
         assertEquals("javax.faces.ViewRoot", tag.getComponentType());
     }
 
     public void testGetRenderType() throws Exception {
         // # Arrange #
         ViewTag tag = new ViewTag();
 
         // # Act & Assert #
         assertEquals(null, tag.getRendererType());
     }
 
    public void testSetProperties_locale() throws Exception {
         // # Arrange #
         MockPageContext pageContext = new MockPageContext();
         pageContext.setRequest(getRequest());
         UIViewRoot viewRoot = createUIViewRoot();
        viewRoot.setLocale(Locale.CANADA);
         ViewTag tag = new ViewTag();
         tag.setPageContext(pageContext);
 
         // # Act #
        tag.setLocale(Locale.FRENCH.toString());
         tag.setProperties(viewRoot);
 
         // # Assert #
        assertEquals(Locale.FRENCH, viewRoot.getLocale());
     }
 
    public void testSetProperties_nullLocale() throws Exception {
         // # Arrange #
         MockPageContext pageContext = new MockPageContext();
         pageContext.setRequest(getRequest());
         UIViewRoot viewRoot = createUIViewRoot();
        viewRoot.setLocale(Locale.CANADA);
         ViewTag tag = new ViewTag();
         tag.setPageContext(pageContext);
 
         // # Act #
         tag.setProperties(viewRoot);
 
         // # Assert #
        assertEquals(Locale.CANADA, viewRoot.getLocale());
     }
 
     public void testDoStartTag() throws Exception {
         // # Arrange #
         ViewTag tag = new ViewTag();
         MockPageContext pageContext = new MockPageContext();
         pageContext.setRequest(getRequest());
         pageContext.setResponse(getResponse());
         tag.setPageContext(pageContext);
         tag.setParent(null);
 
         // # Act #
         int rc = tag.doStartTag();
 
         // # Assert #
         assertEquals(BodyTag.EVAL_BODY_BUFFERED, rc);
     }
 
     public void testDoStartTag_NullContext() throws Exception {
         // # Arrange #
         ViewTag tag = new ViewTag();
         MockPageContext pageContext = new MockPageContext();
         pageContext.setRequest(getRequest());
         pageContext.setResponse(getResponse());
         tag.setPageContext(pageContext);
         tag.setParent(null);
         setCurrentInstanceNull();
         try {
             // # Act #
             tag.doStartTag();
             fail();
         } catch (JspException e) {
             // # Assert #
             ExceptionAssert.assertMessageExist(e);
             //System.out.println(e.getMessage());
         }
     }
 
     public void testDoStartTag_WriterException() throws Exception {
         // # Arrange #
         ViewTag tag = new ViewTag();
         MockPageContext pageContext = new MockPageContext();
         pageContext.setRequest(getRequest());
         pageContext.setResponse(getResponse());
         tag.setPageContext(pageContext);
         tag.setParent(null);
 
         MockResponseWriter writer = new MockResponseWriter() {
             public void startDocument() throws IOException {
                 throw new IOException("TestException");
             }
         };
         getFacesContext().setResponseWriter(writer);
 
         try {
             // # Act #
             tag.doStartTag();
             fail();
         } catch (JspException e) {
             // # Assert #
             ExceptionAssert.assertMessageExist(e);
             //System.out.println(e.getMessage());
         }
     }
 
     public void testDoAfterBody() throws Exception {
         // # Arrange #
         ViewTag tag = new ViewTag();
         tag.setBodyContent(new MockBodyContent());
 
         // # Act #
         int rc = tag.doAfterBody();
 
         // # Assert #
         assertEquals(BodyTag.EVAL_PAGE, rc);
     }
 
     public void testDoAfterBody_NullContext() throws Exception {
         // # Arrange #
         ViewTag tag = new ViewTag();
         tag.setBodyContent(new MockBodyContent());
         setCurrentInstanceNull();
 
         try {
             // # Act #
             tag.doAfterBody();
             fail();
         } catch (NullPointerException e) {
             // # Assert #
             ExceptionAssert.assertMessageExist(e);
             //System.out.println(e.getMessage());
         }
     }
 
     public void testDoAfterBody_NullBodyContent() throws Exception {
         // # Arrange #
         ViewTag tag = new ViewTag() {
             public BodyContent getBodyContent() {
                 return null;
             }
         };
         tag.setBodyContent(new MockBodyContent());
 
         try {
             // # Act #
             tag.doAfterBody();
             fail();
         } catch (JspException e) {
             // # Assert #
             ExceptionAssert.assertMessageExist(e);
             //System.out.println(e.getMessage());
         }
     }
 
     public void testDoAfterBody_WriteException() throws Exception {
         // # Arrange #
         ViewTag tag = new ViewTag();
         final IOException toThrowException = new IOException("TestException");
         MockJspWriter writer = new MockJspWriter() {
             public void write(String s) throws IOException {
                 throw toThrowException;
             }
         };
         tag.setBodyContent(new MockBodyContent(writer));
         try {
             // # Act #
             tag.doAfterBody();
             fail();
         } catch (JspException e) {
             // # Assert #
             final Throwable cause = e.getRootCause();
             assertSame(toThrowException, cause);
         }
     }
 
     public void testDoEndTag() throws Exception {
         // # Arrange #
         ViewTag tag = new ViewTag();
         MockPageContext pageContext = new MockPageContext();
         pageContext.setRequest(getRequest());
         pageContext.setResponse(getResponse());
         tag.setPageContext(pageContext);
         tag.setParent(null);
         tag.setBodyContent(new MockBodyContent());
         tag.doStartTag();
         tag.doAfterBody();
 
         // # Act #
         int rc = tag.doEndTag();
 
         // # Assert #
         assertEquals(rc, Tag.EVAL_PAGE);
     }
 
     public void testDoEndTag_SetCharacterEncoding() throws Exception {
         // # Arrange #
         ViewTag tag = new ViewTag();
         MockPageContext pageContext = new MockPageContext();
         pageContext.setRequest(getRequest());
         MockHttpServletResponse response = getResponse();
         response.setCharacterEncoding("windows-31j");
         pageContext.setResponse(response);
         pageContext.setSession(getRequest().getSession());
         tag.setPageContext(pageContext);
         tag.setParent(null);
         tag.setBodyContent(new MockBodyContent());
         tag.doStartTag();
         tag.doAfterBody();
 
         // # Act #
         tag.doEndTag();
 
         // # Assert #
         assertEquals("windows-31j", pageContext.getSession().getAttribute(
                 ViewHandler.CHARACTER_ENCODING_KEY));
     }
 
     public void testDoEndTag_WriteException() throws Exception {
         // # Arrange #
         final ViewTag tag = new ViewTag();
         final IOException toThrowException = new IOException("TestException");
         MockResponseWriter writer = new MockResponseWriter() {
             public void endDocument() throws IOException {
                 throw toThrowException;
             }
         };
         getFacesContext().setResponseWriter(writer);
         MockPageContext pageContext = new MockPageContext();
         pageContext.setRequest(getRequest());
         pageContext.setResponse(getResponse());
         tag.setPageContext(pageContext);
         tag.setParent(null);
         tag.setBodyContent(new MockBodyContent());
         tag.doStartTag();
         tag.doAfterBody();
         getFacesContext().setResponseWriter(writer);
 
         try {
             // # Act #
             tag.doEndTag();
             fail();
         } catch (final JspException e) {
             // # Assert #
             final Throwable cause = e.getRootCause();
             assertSame(toThrowException, cause);
         }
     }
 
     private UIViewRoot createUIViewRoot() {
         return (UIViewRoot) createUIComponent();
     }
 
     protected UIViewRoot createUIComponent() {
         return new UIViewRoot();
     }
 
     private void setCurrentInstanceNull() {
         OrgFacesContextImpl contextImpl = new OrgFacesContextImpl();
         contextImpl.setCurrentInstanceNull();
     }
 
     private class OrgFacesContextImpl extends MockFacesContextImpl {
         public void setCurrentInstanceNull() {
             setCurrentInstance(null);
         }
     }
 
 }
