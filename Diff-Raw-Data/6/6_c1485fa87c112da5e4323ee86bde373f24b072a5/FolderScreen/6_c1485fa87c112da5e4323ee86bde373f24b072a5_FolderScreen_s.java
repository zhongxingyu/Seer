 /*-
  * Copyright (c) 2006, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer. 
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution. 
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.logicprobe.LogicMail.ui;
 
 import java.io.IOException;
 import net.rim.device.api.system.Application;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.Graphics;
 import net.rim.device.api.ui.Keypad;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.Menu;
 import net.rim.device.api.ui.component.TreeField;
 import net.rim.device.api.ui.component.TreeFieldCallback;
 import org.logicprobe.LogicMail.mail.FolderTreeItem;
 import org.logicprobe.LogicMail.mail.IncomingMailClient;
 import org.logicprobe.LogicMail.cache.AccountCache;
 import org.logicprobe.LogicMail.mail.MailException;
 
 /**
  * Mail folder listing
  * (may only be available with IMAP)
  */
 public class FolderScreen extends BaseScreen implements TreeFieldCallback, MailClientHandlerListener {
     private TreeField treeField;
     private IncomingMailClient client;
     private RefreshFolderTreeHandler refreshFolderTreeHandler;
     
     public FolderScreen(IncomingMailClient client) {
         super("Folders");
         this.client = client;
 
         treeField = new TreeField(this, Field.FOCUSABLE);
         treeField.setEmptyString("No folders", 0);
         treeField.setDefaultExpanded(true);
         treeField.setIndentWidth(20);
         
         add(treeField);
         
         AccountCache acctCache =
                 new AccountCache(this.client.getAcctConfig().getAcctName());
 
         FolderTreeItem treeRoot = acctCache.loadFolderTree();
         if(treeRoot != null && treeRoot.hasChildren())
             generateFolderTree(treeRoot);
         else
             refreshFolderTree();
     }
 
     protected boolean onSavePrompt() {
         return true;
     }
 
     public boolean onClose() {
         if(checkClose()) {
             close();
             return true;
         }
         else
             return false;
     }
 
     private MenuItem folderItem = new MenuItem("Select", 100, 10) {
         public void run() {
             openSelectedFolder();
         }
     };
 
     private MenuItem refreshItem = new MenuItem("Refresh", 110, 10) {
         public void run() {
             refreshFolderTree();
         }
     };
     
     private MenuItem compositionItem = new MenuItem("Compose E-Mail", 120, 10) {
         public void run() {
             UiApplication.getUiApplication().pushScreen(new CompositionScreen(client.getAcctConfig()));
         }
     };
 
     protected void makeMenu(Menu menu, int instance) {
         menu.add(folderItem);
         menu.add(refreshItem);
         menu.add(compositionItem);
         super.makeMenu(menu, instance);
     }
 
     public void drawTreeItem(TreeField treeField,
                              Graphics graphics,
                              int node,
                              int y,
                              int width,
                              int indent)
     {
         Object cookie = treeField.getCookie( node );
         if( cookie instanceof FolderTreeItem ) {
             FolderTreeItem item = (FolderTreeItem)cookie;
             graphics.drawText( item.getName(), indent, y, Graphics.ELLIPSIS, width );
         }
     }
     
     public boolean keyChar(char key,
                            int status,
                            int time)
     {
         boolean retval = false;
         switch(key) {
             case Keypad.KEY_ENTER:
                 openSelectedFolder();
                 retval = true;
                 break;
         }
         return retval;
     }
     private boolean checkClose() {
         // Prompt before closing the connection
         if(client.isConnected()) {
             if(Dialog.ask(Dialog.D_YES_NO, "Disconnect from server?") == Dialog.YES) {
                 try { client.close(); } catch (Exception exp) { }
                 return true;
             }
             else
                 return false;
         }
         else {
             return true;
         }
     }
     
     /**
      * Kick off the folder tree refresh process
      */
     public void refreshFolderTree() {
         // Initialize the handler on demand
         if(refreshFolderTreeHandler == null) {
             refreshFolderTreeHandler = new RefreshFolderTreeHandler();
             refreshFolderTreeHandler.setListener(this);
         }
         
         // Start the background process
         refreshFolderTreeHandler.start();
     }
 
     private void openSelectedFolder() {
     	if(treeField == null) {
             return;
         }
         int curNode = treeField.getCurrentNode();
         if(curNode == -1) {
             return;
         }
         Object cookie = treeField.getCookie(curNode);
         if(cookie instanceof FolderTreeItem) {
             UiApplication.getUiApplication().pushScreen(new MailboxScreen(client, (FolderTreeItem)cookie));
         }
     }
     
     private synchronized void generateFolderTree(FolderTreeItem folderRoot) {
         treeField.deleteAll();
         generateFolderTreeHelper(treeField, 0, folderRoot);
         treeField.setDirty(true);
     }
     
     public void generateFolderTreeHelper(TreeField tree, int parent, FolderTreeItem item) {
         int id = (item.getParent() == null) ? 0 : tree.addChildNode(parent, item);
         if(item.hasChildren()) {
             FolderTreeItem[] children = item.children();
            for(int i=0;i<children.length;i++)
                 generateFolderTreeHelper(tree, id, children[i]);
         }
     }
     
     public void mailActionComplete(MailClientHandler source, boolean result) {
         if(source.equals(refreshFolderTreeHandler)) {
             if(refreshFolderTreeHandler.getFolderRoot() != null) {
                 synchronized(Application.getEventLock()) {
                     generateFolderTree(refreshFolderTreeHandler.getFolderRoot());
                 }
             }
         }
     }
 
     /**
      * Implements the folder tree refresh action
      */
     private class RefreshFolderTreeHandler extends MailClientHandler {
         private FolderTreeItem folderRoot;
         public RefreshFolderTreeHandler() {
             super(FolderScreen.this.client, "Refreshing folder tree");
         }
         public void runSession() throws IOException, MailException {
             // Open a connection to the IMAP server, and retrieve
             // the folder tree as a list of delimited items
             FolderTreeItem folderItem;
             try {
                 folderItem = ((IncomingMailClient)client).getFolderTree();
             } catch (MailException exp) {
                 folderItem = null;
                 throw exp;
             }
 
             // Save the results to the cache
             AccountCache acctCache =
                 new AccountCache(((IncomingMailClient)client).getAcctConfig().getAcctName());
             acctCache.saveFolderTree(folderItem);
 
             this.folderRoot = folderItem;
         }
 
         public FolderTreeItem getFolderRoot() {
             return folderRoot;
         }
     }
 
 }
