 package com.xpn.xwiki.watch.client.ui.menu;
 
 import com.xpn.xwiki.watch.client.data.Group;
 import com.xpn.xwiki.watch.client.ui.WatchWidget;
 import com.xpn.xwiki.watch.client.ui.dialog.GroupDialog;
 import com.xpn.xwiki.watch.client.ui.dialog.FeedDialog;
 import com.xpn.xwiki.watch.client.ui.dialog.StandardFeedDialog;
 import com.xpn.xwiki.watch.client.ui.dialog.FeedDeleteDialog;
 import com.xpn.xwiki.watch.client.Watch;
 import com.xpn.xwiki.watch.client.Feed;
 import com.xpn.xwiki.watch.client.Constants;
 import com.xpn.xwiki.gwt.api.client.app.XWikiAsyncCallback;
 import com.xpn.xwiki.gwt.api.client.dialog.Dialog;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.*;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 import java.util.*;
 
 /**
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  * <p/>
  * This is free software;you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation;either version2.1of
  * the License,or(at your option)any later version.
  * <p/>
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY;without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the GNU
  * Lesser General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software;if not,write to the Free
  * Software Foundation,Inc.,51 Franklin St,Fifth Floor,Boston,MA
  * 02110-1301 USA,or see the FSF site:http://www.fsf.org.
  *
  * @author ldubost
  */
 
 public class FeedTreeWidget  extends WatchWidget {
     private Tree groupTree = new Tree();
     public FeedTreeWidget() {
         super();
     }
 
     public String getName() {
         return "feedtree";
     }
     
     public FeedTreeWidget(Watch watch) {
         super(watch);
         setPanel(new FlowPanel());
         initWidget(panel);
         init();
     }
 
     public void init() {
         super.init();
         HTML titleHTML = new HTML(watch.getTranslation("feedtree.title"));
         titleHTML.setStyleName(watch.getStyleName("feedtree", "title"));
         titleHTML.addClickListener(new ClickListener() {
             public void onClick(Widget widget) {
                 watch.launchConfig("feeds");
             }
         });
         panel.add(titleHTML);
 
         Image configImage = new Image(watch.getSkinFile(Constants.IMAGE_CONFIG));
         configImage.setStyleName(watch.getStyleName("feedtree", "image"));
         configImage.setTitle(watch.getTranslation("config"));
         configImage.addClickListener(new ClickListener() {
             public void onClick(Widget widget) {
                 watch.launchConfig("feeds");
             }
         });
         panel.add(configImage);
         groupTree.setStyleName(watch.getStyleName("feedtree","groups"));
         panel.add(groupTree);
     }
 
     public void refreshData() {
         // we need to make sure the feed tree has been prepared
         makeFeedTree();
     }
 
     private void  makeFeedTree() {
         //get the state of the tree items on first level -> the groups tree items
         HashMap itemsState = new HashMap();
         for (int i = 0; i < this.groupTree.getItemCount(); i++) {
             TreeItem currentTreeItem = this.groupTree.getItem(i);
             //get user object
             ItemObject userObj = (ItemObject)currentTreeItem.getUserObject();
             itemsState.put(userObj.getKey(), new Boolean(currentTreeItem.getState()));
         }
         //get the selected item to set it back when the tree is refreshed 
         TreeItem selectedTreeItem = this.groupTree.getSelectedItem();
         String selectedItemKey = null;
         if (selectedTreeItem != null) {
             ItemObject selectedItemObject = (ItemObject)selectedTreeItem.getUserObject();
             if (selectedItemObject != null) {
                 selectedItemKey = selectedItemObject.getKey();
             }
         }
         // clear all trees
         groupTree.clear();
 
         Map feedsbygroup = watch.getConfig().getFeedsByGroupList();
         Map groups = watch.getConfig().getGroups();
 
         List keys = new ArrayList(feedsbygroup.keySet());
         Collections.sort(keys);
         Iterator groupit = keys.iterator();
         while (groupit.hasNext()) {
             final String groupname = (String) groupit.next();
             Group currentGroup = (Group) groups.get(groupname);
             if (currentGroup == null) {
                 currentGroup = new Group();
                 currentGroup.setName(groupname);
             }
             if ((groupname!=null)&&(!groupname.trim().equals(""))) {
                 Map groupFeeds = (Map) feedsbygroup.get(groupname);
                 TreeItem groupItemTree = new TreeItem();
                 //set the TreeItem's object
                 GroupTreeItemObject groupObj = new GroupTreeItemObject(groupname, currentGroup);
                 groupItemTree.setUserObject(groupObj);
                 //check if selected
                 boolean selected = false;
                 if (selectedItemKey != null && groupname.equals(selectedItemKey)) {
                     selected = true;
                     selectedTreeItem = groupItemTree;
                 }
                 groupItemTree.setWidget(groupObj.getWidget(selected));
                 groupTree.addItem(groupItemTree);
                 List feedList = new ArrayList(groupFeeds.keySet());
                 Collections.sort(feedList);
                 Iterator feedgroupit = feedList.iterator();
                 while (feedgroupit.hasNext()) {
                     String feedname = (String) feedgroupit.next();
                     Feed feed = (Feed) groupFeeds.get(feedname);
                     //set it's userObject to the name of the group + name of the feed since a 
                     //feed can be part of multiple groups and we need to identify it uniquely.
                     String itemTreeKey = groupname + "." + feedname;
                     ItemObject feedObj = new FeedTreeItemObject(itemTreeKey, feed);
                     TreeItem feedItem = new TreeItem();
                     feedItem.setUserObject(feedObj);
                     selected = false;
                     if (selectedItemKey != null && itemTreeKey.equals(selectedItemKey)) {
                         selected = true;
                         selectedTreeItem = feedItem;
                     }
                     feedItem.setWidget(feedObj.getWidget(selected));
                     groupItemTree.addItem(feedItem);
                 }
                 //expand it if necessary
                 Boolean state = (Boolean)itemsState.get(groupname);
                 if (state != null) {
                     groupItemTree.setState(state.booleanValue());
                 }
                 groupTree.addItem(groupItemTree);
             }
         }
         //set the selected tree item
         this.groupTree.setSelectedItem(selectedTreeItem);
     }
 
     public void resizeWindow() {
         // Watch.setMaxHeight(panel);
     }
     
     public class GroupTreeItemObject extends ItemObject {
         public GroupTreeItemObject(String key, Object data)
         {
             super(key, data);
         }
 
         public Widget getWidget(boolean selected)
         {
             final Group group = (Group)this.data;
             HTML title = new HTML(group.getName(), true);
            title.setStyleName(watch.getStyleName("feedtree","link"));
             title.addClickListener(new ClickListener() {
                 public void onClick(Widget widget) {
                     watch.refreshOnGroupChange(group.getPageName().trim().equals("") 
                                                ? group.getName() : group.getPageName());
                 }
             });            
             Widget widget = title;
             //if group is All group or it is a non-existent group, we shouldn't be able to edit it
             if (selected && (!group.getName().equals(watch.getTranslation("all")))
                 && !group.getPageName().equals("")) {
                 //create a composite with link as main widget and some actions
                 widget = new TextWidgetComposite(title);
                 Label editLabel = new Label(watch.getTranslation("feedtree.edit"));
                 editLabel.addClickListener(new ClickListener() {
                     public void onClick (Widget widget) {
                         GroupDialog gDialog = new GroupDialog(watch, "addgroup", 
                             Dialog.BUTTON_CANCEL | Dialog.BUTTON_NEXT, group);
                         gDialog.setAsyncCallback(new AsyncCallback() {
                             public void onFailure(Throwable throwable) {
                                 //nothing
                             }
                             public void onSuccess(Object object) {
                                 Group newGroup = (Group)object;
                                 watch.getDataManager().updateGroup(newGroup, new XWikiAsyncCallback(watch) {
                                     public void onFailure(Throwable caught) {
                                         super.onFailure(caught);
                                     }
     
                                     public void onSuccess(Object result) {
                                         super.onSuccess(result);
                                         // We need to refreshData the tree
                                         watch.refreshOnNewGroup();
                                         watch.refreshOnNewKeyword();
                                     }
                                 });
                             }
                         });
                         gDialog.show();
                     }
                 });
                 TextWidgetComposite editComposite = new TextWidgetComposite(editLabel);
                 Label deleteLabel = new Label(watch.getTranslation("feedtree.delete"));
                 deleteLabel.addClickListener(new ClickListener() {
                    public void onClick(Widget widget) {
                        String confirmString = watch.getTranslation("removegroup.confirm", 
                                                                    new String[] {group.getName()});
                        boolean confirm = Window.confirm(confirmString);
                        if (confirm) {
                            watch.getDataManager().removeGroup(group, new XWikiAsyncCallback(watch) {
                                public void onFailure(Throwable caught) {
                                    super.onFailure(caught);
                                }
                                public void onSuccess(Object result) {
                                    super.onSuccess(result);
                                    // We need to refreshData the tree
                                    watch.refreshOnNewGroup();
                                    watch.refreshOnNewKeyword();
                                }
                            });
                        } else {
                            //nothing
                        }
                    } 
                 });
                 TextWidgetComposite deleteComposite = new TextWidgetComposite(deleteLabel);
                 //set styles
                 editComposite.setStyleName(watch.getStyleName("feedtree", "groupaction") 
                     + " " + watch.getStyleName("feedtree", "editgroup"));
                 deleteComposite.setStyleName(watch.getStyleName("feedtree", "groupaction") 
                     + " " + watch.getStyleName("feedtree", "deletegroup"));
                 //add the two actions to the composite, in reverse order since they will
                 //be floated to the right
                 ((TextWidgetComposite)widget).add(deleteComposite);
                 ((TextWidgetComposite)widget).add(editComposite);
             }
             return widget;
         }
     }
     
     public class FeedTreeItemObject extends ItemObject {
 
         public FeedTreeItemObject(String key, Object data)
         {
             super(key, data);
         }
         
         private String getFavIcon(Feed feed) {
             return watch.getFavIcon(feed);
         }
 
         public Widget getWidget(boolean selected)
         {
             //cast data to feed
             final Feed feed = (Feed)this.data;
             String feedtitle =  feed.getName() + "(" + feed.getNb() + ")";
             String imgurl = getFavIcon(feed);     
             if (imgurl!=null)
              feedtitle = "<img src=\"" + imgurl + "\" class=\"" + watch.getStyleName("feedtree","logo-icon") + "\" alt=\"\" />" + feedtitle;
             HTML title = new HTML(feedtitle, true);
             title.addClickListener(new ClickListener() {
                 public void onClick(Widget widget) {
                     watch.refreshOnFeedChange(feed);
                 } 
             });
            title.setStyleName(watch.getStyleName("feedtree","link"));            
             Widget widget = title;
             
             //if selected, generate the two action links
             if (selected) {
                 widget = new TextWidgetComposite(title);
                 //create the inner item
                 Label editLabel = new Label(watch.getTranslation("feedtree.edit"));
                 editLabel.addClickListener(new ClickListener() {
                     public void onClick (Widget widget) {
                         FeedDialog feedDialog = new StandardFeedDialog(watch, "standard", Dialog.BUTTON_CANCEL | Dialog.BUTTON_NEXT, feed);
                         feedDialog.setAsyncCallback(new AsyncCallback() {
                             public void onFailure(Throwable throwable) {
                                 //nothing
                             }
                             public void onSuccess(Object object) {
                                 Feed newfeed = (Feed) object;
                                 watch.getDataManager().updateFeed(newfeed, new XWikiAsyncCallback(watch) {
                                     public void onFailure(Throwable caught) {
                                         super.onFailure(caught);
                                     }
     
                                     public void onSuccess(Object result) {
                                         super.onSuccess(result);
                                         // We need to refreshData the tree
                                         watch.refreshOnNewFeed();
                                     }
                                 });
                             }
                         });
                         feedDialog.show();
                     }
                 });
                 TextWidgetComposite editComposite = new TextWidgetComposite(editLabel);
                 
                 Label deleteLabel = new Label(watch.getTranslation("feedtree.delete"));
                 deleteLabel.addClickListener(new ClickListener() {
                    public void onClick(Widget widget) {
                        //use a delete feed dialog
                        FeedDeleteDialog deleteDialog = new FeedDeleteDialog(watch, "removefeed", feed);
                        deleteDialog.show();
                    }
                 });
                 TextWidgetComposite deleteComposite = new TextWidgetComposite(deleteLabel);
                 //set styles
                 editComposite.setStyleName(watch.getStyleName("feedtree", "feedaction") 
                     + " " + watch.getStyleName("feedtree", "editfeed"));
                 deleteComposite.setStyleName(watch.getStyleName("feedtree", "feedaction") 
                     + " " + watch.getStyleName("feedtree", "deletefeed"));
                 
                 //add the two actions to the composite, in reverse order since they will
                 //be floated to the right
                 ((TextWidgetComposite)widget).add(deleteComposite);
                 ((TextWidgetComposite)widget).add(editComposite);
             }
             return widget;
         }
     }
 }
