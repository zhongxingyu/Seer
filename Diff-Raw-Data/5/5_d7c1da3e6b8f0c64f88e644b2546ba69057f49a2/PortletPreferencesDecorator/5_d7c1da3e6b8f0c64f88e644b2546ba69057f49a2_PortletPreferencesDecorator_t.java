 /*
     Copyright (c) 2013 Alessandro Coppo
     All rights reserved.
 
     Redistribution and use in source and binary forms, with or without
     modification, are permitted provided that the following conditions
     are met:
     1. Redistributions of source code must retain the above copyright
        notice, this list of conditions and the following disclaimer.
     2. Redistributions in binary form must reproduce the above copyright
        notice, this list of conditions and the following disclaimer in the
        documentation and/or other materials provided with the distribution.
     3. The name of the author may not be used to endorse or promote products
        derived from this software without specific prior written permission.
 
     THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
     IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
     OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
     IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
     INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
     NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
     DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
     THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
     (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
     THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 package it.webalice.alexcoppo.lrxl.portlet;
 
 import com.liferay.portal.kernel.log.Log;
 import com.liferay.portal.kernel.log.LogFactoryUtil;
 import java.io.IOException;
 import javax.portlet.PortletPreferences;
 import javax.portlet.PortletRequest;
 import javax.portlet.ReadOnlyException;
 import javax.portlet.ValidatorException;
 import net.sf.jautl.text.BasicMarshaller;
 import net.sf.jautl.text.StringMarshaller;
 
 /**
  * This class is the base of the classes implementing specific portlet preferences.
  * 
  * The idea is to derive from this class, adding portlet specific methods like
  *
  * <code>
  * private static String FOO_TAG = "foo";
  * 
  * public void storeFoo(long foo) {
 *     setProperty(FOO_TAG, getMarshaller().marshall(foo));
  * }
  * 
  * public long loadFoo(long defaultFoo) {
 *     return getMarshaller().asLong(getProperty(FOO_TAG, null), defaultFoo);
  * }
  * </code>
  */
 public class PortletPreferencesDecorator {
     private static Log _log = LogFactoryUtil.getLog(PortletPreferencesDecorator.class);
     private PortletPreferences pp;
     private StringMarshaller sm;
     
     public PortletPreferencesDecorator() {
         this(new BasicMarshaller());
     }
     
     public PortletPreferencesDecorator(StringMarshaller sm) {
         this.sm = sm;
     }
     
     public void attach(PortletPreferences pp) {
         this.pp = pp;
     }
     
     public void attach(PortletRequest pr) {
         attach(pr.getPreferences());
     }
     
     public void persist() {
         try {
             pp.store();
         } catch (IOException ioe) {
             _log.error(ioe);
         } catch (ValidatorException ve) {
             _log.error(ve);
         }
     }
     
     public void setProperty(String propertyName, String value) {
         try {
             pp.setValue(propertyName, value);
         } catch(ReadOnlyException roe) {
             _log.error(roe);
         }
     }
     
     public String getProperty(String propertyName, String defValue) {
         return pp.getValue(propertyName, defValue);
     }
     
     protected StringMarshaller getMarshaller() {
         return sm;
     }
 }
