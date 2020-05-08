 /**
  *  odt2daisy - OpenDocument to DAISY XML/Audio
  *
  *  (c) Copyright 2008 - 2009 by Vincent Spiewak, All Rights Reserved.
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Lesser Public License as published by
  *  the Free Software Foundation; either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License along
  *  with this program; if not, write to the Free Software Foundation, Inc.,
  *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 package com.versusoft.packages.ooo.odt2daisy.addon;
 
 import com.sun.star.uno.UnoRuntime;
 import com.sun.star.uno.XComponentContext;
 import com.sun.star.lib.uno.helper.Factory;
 import com.sun.star.lang.XSingleComponentFactory;
 import com.sun.star.registry.XRegistryKey;
 import com.sun.star.lib.uno.helper.WeakBase;
 import com.versusoft.packages.ooo.odt2daisy.addon.gui.UnoGUI;
 
 public final class Odt2DaisyAddOn extends WeakBase
         implements com.sun.star.lang.XServiceInfo,
         com.sun.star.frame.XDispatchProvider,
         com.sun.star.lang.XInitialization,
         com.sun.star.frame.XDispatch {
 
     private final XComponentContext m_xContext;
     private com.sun.star.frame.XFrame m_xFrame;
     private static final String m_implementationName = Odt2DaisyAddOn.class.getName();
     private static final String[] m_serviceNames = {
         "com.sun.star.frame.ProtocolHandler"
     };
 
     public Odt2DaisyAddOn(XComponentContext context) {
         m_xContext = context;
     }
 
     public static XSingleComponentFactory __getComponentFactory(String sImplementationName) {
         XSingleComponentFactory xFactory = null;
 
         if (sImplementationName.equals(m_implementationName)) {
             xFactory = Factory.createComponentFactory(Odt2DaisyAddOn.class, m_serviceNames);
         }
         return xFactory;
     }
 
     public static boolean __writeRegistryServiceInfo(XRegistryKey xRegistryKey) {
         return Factory.writeRegistryServiceInfo(m_implementationName,
                 m_serviceNames,
                 xRegistryKey);
     }
 
     // com.sun.star.lang.XServiceInfo:
     public String getImplementationName() {
         return m_implementationName;
     }
 
     public boolean supportsService(String sService) {
         int len = m_serviceNames.length;
 
         for (int i = 0; i < len; i++) {
             if (sService.equals(m_serviceNames[i])) {
                 return true;
             }
         }
         return false;
     }
 
     public String[] getSupportedServiceNames() {
         return m_serviceNames;
     }
 
     // com.sun.star.frame.XDispatchProvider:
     public com.sun.star.frame.XDispatch queryDispatch(com.sun.star.util.URL aURL,
             String sTargetFrameName,
             int iSearchFlags) {
        if (aURL.Protocol.compareTo("com.versusoft.packages.ooo.odt2daisy.addon.odt2daisyaddon:") == 0) {
             if (aURL.Path.compareTo("ExportCommand") == 0) {
                 return this;
             } else if (aURL.Path.compareTo("ExportFullCommand") == 0) {
                 return this;
             }
         }
         return null;
     }
 
     // com.sun.star.frame.XDispatchProvider:
     public com.sun.star.frame.XDispatch[] queryDispatches(
             com.sun.star.frame.DispatchDescriptor[] seqDescriptors) {
         int nCount = seqDescriptors.length;
         com.sun.star.frame.XDispatch[] seqDispatcher =
                 new com.sun.star.frame.XDispatch[seqDescriptors.length];
 
         for (int i = 0; i < nCount; ++i) {
             seqDispatcher[i] = queryDispatch(seqDescriptors[i].FeatureURL,
                     seqDescriptors[i].FrameName,
                     seqDescriptors[i].SearchFlags);
         }
         return seqDispatcher;
     }
 
     // com.sun.star.lang.XInitialization:
     public void initialize(Object[] object)
             throws com.sun.star.uno.Exception {
         if (object.length > 0) {
             m_xFrame = (com.sun.star.frame.XFrame) UnoRuntime.queryInterface(
                     com.sun.star.frame.XFrame.class, object[0]);
         }
     }
 
     // com.sun.star.frame.XDispatch:
     public void dispatch(com.sun.star.util.URL aURL,
             com.sun.star.beans.PropertyValue[] aArguments) {
        if (aURL.Protocol.compareTo("com.versusoft.packages.ooo.odt2daisy.addon.odt2daisyaddon:") == 0) {
 
             if (aURL.Path.compareTo("ExportCommand") == 0) {
 
                 boolean ret;
 
                 UnoGUI gui = new UnoGUI(m_xContext, m_xFrame);
                 ret = gui.saveAsXML();
                 gui.stopStatusIndicator();
 
                 if (ret) {
                    gui.showOkSaveAsXML();
                 }
 
                 gui.flushLogger();
                 return;
 
             } else if (aURL.Path.compareTo("ExportFullCommand") == 0) {
 
                 boolean ret;
                 UnoGUI gui = new UnoGUI(m_xContext, m_xFrame, true);
                 ret = gui.saveAsXML();
                 gui.stopStatusIndicator();
 
                 if (ret) {
                     gui.saveAsFull();
                 }
 
                 gui.flushLogger();
                 return;
 
             }
         }
     }
 
     public void addStatusListener(com.sun.star.frame.XStatusListener xControl,
             com.sun.star.util.URL aURL) {
         // add your own code here
     }
 
     public void removeStatusListener(com.sun.star.frame.XStatusListener xControl,
             com.sun.star.util.URL aURL) {
         // add your own code here
     }
 }
