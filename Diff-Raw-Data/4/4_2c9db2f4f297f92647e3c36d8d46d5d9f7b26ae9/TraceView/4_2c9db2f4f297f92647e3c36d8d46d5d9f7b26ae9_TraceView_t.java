 /*******************************************************************************
  * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal.debug.ui.trace;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationListener;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.jface.resource.ColorRegistry;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabFolder2Adapter;
 import org.eclipse.swt.custom.CTabFolderEvent;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.tcf.core.AbstractChannel;
 import org.eclipse.tcf.internal.debug.model.TCFLaunch;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.IPeer;
 import org.eclipse.tcf.protocol.JSON;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.util.TCFTask;
 import org.eclipse.ui.IWorkbenchPreferenceConstants;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 
 
 public class TraceView extends ViewPart implements Protocol.ChannelOpenListener {
 
     private Composite parent;
     private CTabFolder tabs;
     private Label no_data;
     private final Map<CTabItem,Page> tab2page = new HashMap<CTabItem,Page>();
     private final ILaunchManager launch_manager = DebugPlugin.getDefault().getLaunchManager();
 
     private final ILaunchConfigurationListener launch_conf_listener = new ILaunchConfigurationListener() {
 
         public void launchConfigurationAdded(ILaunchConfiguration cfg) {
             cfg = launch_manager.getMovedFrom(cfg);
             if (cfg != null) launchConfigurationChanged(cfg);
         }
 
         public void launchConfigurationChanged(final ILaunchConfiguration cfg) {
             HashSet<IChannel> set = new HashSet<IChannel>();
             for (final ILaunch launch : launch_manager.getLaunches()) {
                 if (launch instanceof TCFLaunch && cfg.equals(launch.getLaunchConfiguration())) {
                     set.add(((TCFLaunch)launch).getChannel());
                 }
             }
             for (final IChannel channel : set) {
                 parent.getDisplay().asyncExec(new Runnable() {
                     public void run() {
                         for (final Page p : tab2page.values()) {
                             if (p.channel == channel) {
                                 p.tab.setToolTipText(new TCFTask<String>(p.channel) {
                                     public void run() {
                                         done(getPageToolTipText(p.channel));
                                     }
                                 }.getE());
                             }
                         }
                     }
                 });
             }
         }
 
         public void launchConfigurationRemoved(ILaunchConfiguration cfg) {
         }
     };
 
     private class Page implements AbstractChannel.TraceListener {
 
         final AbstractChannel channel;
 
         private CTabItem tab;
         private Text text;
 
         private final StringBuffer bf = new StringBuffer();
         private int bf_line_cnt = 0;
         private boolean closed;
         private boolean scroll_locked;
         private int key_pressed;
         private int mouse_button_pressed;
 
         private final Thread update_thread = new Thread() {
             public void run() {
                 synchronized (Page.this) {
                     while (!closed) {
                         if (bf_line_cnt > 0 && (!scroll_locked || bf_line_cnt >= 5000)) {
                             Runnable r = new Runnable() {
                                 public void run() {
                                     String str = null;
                                     int cnt = 0;
                                     synchronized (Page.this) {
                                         str = bf.toString();
                                         cnt = bf_line_cnt;
                                         bf.setLength(0);
                                         bf_line_cnt = 0;
                                     }
                                     if (text == null) return;
                                     if (text.getLineCount() > 1000 - cnt) {
                                         String s = text.getText();
                                         int n = 0;
                                         int i = -1;
                                         while (n < cnt) {
                                             int j = s.indexOf('\n', i + 1);
                                             if (j < 0) break;
                                             i = j;
                                             n++;
                                         }
                                         if (i >= 0) {
                                             text.setText(s.substring(i + 1));
                                         }
                                     }
                                     text.append(str);
                                 }
                             };
                             parent.getDisplay().asyncExec(r);
                         }
                         try {
                             Page.this.wait(1000);
                         }
                         catch (InterruptedException e) {
                             break;
                         }
                     }
                 }
             }
         };
 
         Page(AbstractChannel channel) {
             this.channel = channel;
             update_thread.setName("TCF Trace View");
             update_thread.start();
         }
 
         private void updateScrollLock() {
             if (text == null) {
                 scroll_locked = false;
             }
             else {
                 scroll_locked = key_pressed > 0 || mouse_button_pressed > 0 || text.getSelectionCount() > 0;
             }
         }
 
         public void dispose() {
             if (closed) return;
             Protocol.invokeAndWait(new Runnable() {
                 public void run() {
                     channel.removeTraceListener(Page.this);
                 }
             });
             synchronized (this) {
                 closed = true;
                 update_thread.interrupt();
             }
             try {
                 update_thread.join();
             }
             catch (InterruptedException e) {
                 e.printStackTrace();
             }
             if (tab != null) {
                 tab2page.remove(tab);
                 tab.dispose();
                 tab = null;
             }
             text = null;
             if (tab2page.isEmpty()) hideTabs();
         }
 
         public synchronized void onChannelClosed(Throwable error) {
             if (error == null) {
                 parent.getDisplay().asyncExec(new Runnable() {
                     public void run() {
                         dispose();
                     }
                 });
             }
             else {
                 bf.append("Channel terminated: " + error);
                 bf_line_cnt++;
             }
         }
 
         public synchronized void onMessageReceived(char type, String token,
                 String service, String name, byte[] data) {
             try {
                 if ("Locator".equals(service) && "peerHeartBeat".equals(name)) return;
                 appendTime(bf);
                 bf.append("Inp: ");
                 bf.append(type);
                 if (token != null) {
                     bf.append(' ');
                     bf.append(token);
                 }
                 if (service != null) {
                     bf.append(' ');
                     bf.append(service);
                 }
                 if (name != null) {
                     bf.append(' ');
                     bf.append(name);
                 }
                 if (data != null) {
                     appendData(bf, data);
                 }
                 bf.append('\n');
                 bf_line_cnt++;
             }
             catch (UnsupportedEncodingException x) {
                 x.printStackTrace();
             }
         }
 
         public synchronized void onMessageSent(char type, String token,
                 String service, String name, byte[] data) {
             try {
                 if ("Locator".equals(service) && "peerHeartBeat".equals(name)) return;
                 appendTime(bf);
                 bf.append("Out: ");
                 bf.append(type);
                 if (token != null) {
                     bf.append(' ');
                     bf.append(token);
                 }
                 if (service != null) {
                     bf.append(' ');
                     bf.append(service);
                 }
                 if (name != null) {
                     bf.append(' ');
                     bf.append(name);
                 }
                 if (data != null) {
                     appendData(bf, data);
                 }
                 bf.append('\n');
                 bf_line_cnt++;
             }
             catch (UnsupportedEncodingException x) {
                 x.printStackTrace();
             }
         }
     }
 
     @Override
     public void createPartControl(Composite parent) {
         this.parent = parent;
         Protocol.invokeAndWait(new Runnable() {
             public void run() {
                 IChannel[] arr = Protocol.getOpenChannels();
                 for (IChannel c : arr) onChannelOpen(c);
                 Protocol.addChannelOpenListener(TraceView.this);
             }
         });
         if (tab2page.size() == 0) hideTabs();
         launch_manager.addLaunchConfigurationListener(launch_conf_listener);
     }
 
     @Override
     public void setFocus() {
         if (tabs != null) tabs.setFocus();
     }
 
     @Override
     public void dispose() {
         launch_manager.removeLaunchConfigurationListener(launch_conf_listener);
         final Page[] pages = tab2page.values().toArray(new Page[tab2page.size()]);
         Protocol.invokeAndWait(new Runnable() {
             public void run() {
                 Protocol.removeChannelOpenListener(TraceView.this);
             }
         });
         for (Page p : pages) p.dispose();
         assert tab2page.isEmpty();
         if (tabs != null) {
             tabs.dispose();
             tabs = null;
         }
         if (no_data != null) {
             no_data.dispose();
             no_data = null;
         }
         super.dispose();
     }
 
     private String getPageTitle(IChannel c) {
         IPeer rp = c.getRemotePeer();
         String title = rp.getName();
         String host = rp.getAttributes().get(IPeer.ATTR_IP_HOST);
         String port = rp.getAttributes().get(IPeer.ATTR_IP_PORT);
         if (host != null) {
             title += ", " + host;
             if (port != null) {
                 title += ":" + port;
             }
         }
         return title;
     }
 
     private String getPageToolTipText(IChannel c) {
         StringBuffer bf = new StringBuffer();
         for (ILaunch launch : launch_manager.getLaunches()) {
             if (launch instanceof TCFLaunch && ((TCFLaunch)launch).getChannel() == c) {
                 if (bf.length() > 0) bf.append('\n');
                 bf.append("Launch configuration: ");
                 bf.append(launch.getLaunchConfiguration().getName());
             }
         }
         IPeer rp = c.getRemotePeer();
         String host = rp.getAttributes().get(IPeer.ATTR_IP_HOST);
         if (host != null) {
             if (bf.length() > 0) bf.append('\n');
             bf.append("Agent address: ");
             bf.append(host);
             String port = rp.getAttributes().get(IPeer.ATTR_IP_PORT);
             if (port != null) {
                 bf.append(':');
                 bf.append(port);
             }
         }
         if (bf.length() > 0) bf.append('\n');
         bf.append("Agent name: ");
         bf.append(rp.getName());
         String user_name = rp.getAttributes().get(IPeer.ATTR_USER_NAME);
         if (user_name != null) {
             bf.append('\n');
             bf.append("Agent user: ");
             bf.append(user_name);
         }
         return bf.toString();
     }
 
     public void onChannelOpen(final IChannel channel) {
         if (!(channel instanceof AbstractChannel)) return;
         AbstractChannel c = (AbstractChannel)channel;
         final Page p = new Page(c);
         c.addTraceListener(p);
         final String title = getPageTitle(c);
         final String tool_tip = getPageToolTipText(c);
         parent.getDisplay().asyncExec(new Runnable() {
             public void run() {
                if (parent.isDisposed()) return;
                 showTabs();
                 p.tab = new CTabItem(tabs, SWT.NONE);
                 tab2page.put(p.tab, p);
                 p.tab.setText(title);
                 p.tab.setToolTipText(tool_tip);
                 p.text = new Text(tabs, SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY | SWT.MULTI);
                 p.text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
                 p.text.addKeyListener(new KeyListener() {
                     public void keyReleased(KeyEvent e) {
                         if (p.key_pressed > 0) p.key_pressed--;
                         if (e.character == SWT.ESC) {
                             p.key_pressed = 0;
                         }
                         p.updateScrollLock();
                     }
                     public void keyPressed(KeyEvent e) {
                         p.key_pressed++;
                         p.updateScrollLock();
                         if (e.character == SWT.ESC) {
                             p.text.clearSelection();
                         }
                     }
                 });
                 p.text.addMouseListener(new MouseListener() {
                     public void mouseUp(MouseEvent e) {
                         p.mouse_button_pressed--;
                         p.updateScrollLock();
                     }
                     public void mouseDown(MouseEvent e) {
                         p.mouse_button_pressed++;
                         p.updateScrollLock();
                     }
                     public void mouseDoubleClick(MouseEvent e) {
                     }
                 });
                 p.tab.setControl(p.text);
                 if (tabs.getSelection() == null) tabs.setSelection(p.tab);
             }
         });
     }
 
     private void appendTime(StringBuffer bf) {
         String s = Long.toString(System.currentTimeMillis());
         int l = s.length();
         if (l < 6) return;
         bf.append(s.charAt(l - 6));
         bf.append(s.charAt(l - 5));
         bf.append(s.charAt(l - 4));
         bf.append('.');
         bf.append(s.charAt(l - 3));
         bf.append(s.charAt(l - 2));
         bf.append(s.charAt(l - 1));
         bf.append(' ');
     }
 
     private void appendData(StringBuffer bf, byte[] data) throws UnsupportedEncodingException {
         int pos = bf.length();
         try {
             Object[] o = JSON.parseSequence(data);
             for (int i = 0; i < o.length; i++) {
                 bf.append(' ');
                 appendJSON(bf, o[i]);
             }
         }
         catch (Throwable z) {
             bf.setLength(pos);
             for (int i = 0; i < data.length; i++) {
                 bf.append(' ');
                 int x = (data[i] >> 4) & 0xf;
                 int y = data[i] & 0xf;
                 bf.append((char)(x < 10 ? '0' + x : 'a' + x - 10));
                 bf.append((char)(y < 10 ? '0' + y : 'a' + y - 10));
             }
         }
     }
 
     private void appendJSON(StringBuffer bf, Object o) {
         if (o instanceof byte[]) {
             int l = ((byte[])o).length;
             bf.append('(');
             bf.append(l);
             bf.append(')');
         }
         else if (o instanceof Collection) {
             int cnt = 0;
             bf.append('[');
             for (Object i : (Collection<?>)o) {
                 if (cnt > 0) bf.append(',');
                 appendJSON(bf, i);
                 cnt++;
             }
             bf.append(']');
         }
         else if (o instanceof Map) {
             int cnt = 0;
             bf.append('{');
             for (Object k : ((Map<?,?>)o).keySet()) {
                 if (cnt > 0) bf.append(',');
                 bf.append(k.toString());
                 bf.append(':');
                 appendJSON(bf, ((Map<?,?>)o).get(k));
                 cnt++;
             }
             bf.append('}');
         }
         else if (o instanceof String) {
             bf.append('"');
             String s = (String)o;
             int l = s.length();
             for (int i = 0; i < l; i++) {
                 char ch = s.charAt(i);
                 if (ch < ' ') {
                     bf.append('\\');
                     bf.append('u');
                     for (int j = 0; j < 4; j++) {
                         int x = (ch >> (4 * (3 - j))) & 0xf;
                         bf.append((char)(x < 10 ? '0' + x : 'a' + x - 10));
                     }
                 }
                 else {
                     bf.append(ch);
                 }
             }
             bf.append('"');
         }
         else {
             bf.append(o);
         }
     }
 
     private void showTabs() {
         boolean b = false;
         if (no_data != null) {
             no_data.dispose();
             no_data = null;
             b = true;
         }
        if (tabs == null && !parent.isDisposed()) {
             tabs = new CTabFolder(parent, SWT.FLAT | SWT.CLOSE);
             ColorRegistry reg = JFaceResources.getColorRegistry();
             Color c1 = reg.get("org.eclipse.ui.workbench.ACTIVE_TAB_BG_START"); //$NON-NLS-1$
             Color c2 = reg.get("org.eclipse.ui.workbench.ACTIVE_TAB_BG_END"); //$NON-NLS-1$
             tabs.setSelectionBackground(new Color[]{c1, c2}, new int[]{100}, true);
             tabs.setSelectionForeground(reg.get("org.eclipse.ui.workbench.ACTIVE_TAB_TEXT_COLOR")); //$NON-NLS-1$
             tabs.setSimple(PlatformUI.getPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS));
             tabs.addCTabFolder2Listener(new CTabFolder2Adapter() {
                 public void close(CTabFolderEvent event) {
                     CTabItem s = (CTabItem)event.item;
                     Page p = tab2page.get(s);
                     if (p != null) p.dispose();
                     else s.dispose();
                     event.doit = false;
                 }
             });
             Menu menu = new Menu(tabs);
             MenuItem mi_close = new MenuItem(menu, SWT.NONE);
             mi_close.setText("Close");
             mi_close.addSelectionListener(new SelectionListener() {
                 public void widgetDefaultSelected(SelectionEvent e) {
                 }
                 public void widgetSelected(SelectionEvent e) {
                     if (tabs == null) return;
                     CTabItem s = tabs.getSelection();
                     Page p = tab2page.get(s);
                     if (p != null) p.dispose();
                     else s.dispose();
                 }
             });
             MenuItem mi_close_all = new MenuItem(menu, SWT.NONE);
             mi_close_all.setText("Close All");
             mi_close_all.addSelectionListener(new SelectionListener() {
                 public void widgetDefaultSelected(SelectionEvent e) {
                 }
                 public void widgetSelected(SelectionEvent e) {
                     if (tabs == null) return;
                     CTabItem[] s = tabs.getItems();
                     for (CTabItem i : s) {
                         Page p = tab2page.get(i);
                         if (p != null) p.dispose();
                         else i.dispose();
                     }
                 }
             });
             tabs.setMenu(menu);
             b = true;
         }
         if (b) parent.layout();
     }
 
     private void hideTabs() {
         boolean b = false;
         if (tabs != null) {
             tabs.dispose();
             tabs = null;
             b = true;
         }
         if (!parent.isDisposed()) {
             if (no_data == null) {
                 no_data = new Label(parent, SWT.NONE);
                 no_data.setText("No open communication channels at this time.");
                 b = true;
             }
             if (b) parent.layout();
         }
     }
 }
