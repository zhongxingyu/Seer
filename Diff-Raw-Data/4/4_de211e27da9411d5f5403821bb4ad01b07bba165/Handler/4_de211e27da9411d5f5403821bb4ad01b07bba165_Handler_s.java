 package com.tomclaw.mandarin.core;
 
 import com.tomclaw.mandarin.main.*;
 import com.tomclaw.mandarin.molecus.*;
 import com.tomclaw.tcuilite.*;
 import com.tomclaw.tcuilite.localization.Localization;
 import com.tomclaw.utils.LogUtil;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 
 /**
  * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
  * http://www.tomclaw.com/
  * @author Solkin
  */
 public class Handler {
 
   /**
    * Setting up received roster
    * @param roster 
    */
   public static void setRoster( Hashtable params ) {
     Vector rosterItems = ( Vector ) params.get( "ROSTER" );
     /** Inserting services group item into zero index **/
     GroupItem servicesGroupItem = ( GroupItem ) params.get( "SERVICES" );
     int regServicesCount = servicesGroupItem.getChildsCount();
     int allServicesCount = MidletMain.mainFrame.buddyList.servicesGroupItem.getChildsCount();
     for ( int c = 0; c < regServicesCount; c++ ) {
       BuddyItem regServiceItem = ( BuddyItem ) servicesGroupItem.elementAt( c );
       LogUtil.outMessage( "regServiceItem [ " + c + " ] = " + regServiceItem.getJid() );
       /** Cycling all services **/
       for ( int i = 0; i < allServicesCount; i++ ) {
         ServiceItem allServiceItem =
                 ( ServiceItem ) MidletMain.mainFrame.buddyList.servicesGroupItem.elementAt( i );
         if ( regServiceItem.getJid().equals( allServiceItem.getJid() ) ) {
           /** This is not temp service **/
           allServiceItem.setTemp( false );
           allServiceItem.setSubscription( regServiceItem.getSubscription() );
           /** This is the same service **/
           regServiceItem = null;
           break;
         }
       }
       /** Checking for item existance in all services **/
       if ( regServiceItem != null ) {
         MidletMain.mainFrame.buddyList.servicesGroupItem.addChild( regServiceItem );
       }
     }
     rosterItems.insertElementAt( MidletMain.mainFrame.buddyList.servicesGroupItem, 0 );
     /** Inserting rooms group into second index **/
     rosterItems.insertElementAt( MidletMain.mainFrame.buddyList.roomsGroupItem, 1 );
     /** Creating and inserting temporary items group **/
     MidletMain.mainFrame.buddyList.tempGroupItem = new GroupItem( Localization.getMessage( "TEMPORARY" ) );
     MidletMain.mainFrame.buddyList.tempGroupItem.internalGroupId = GroupItem.GROUP_TEMP_ID;
     MidletMain.mainFrame.buddyList.tempGroupItem.isCollapsed = false;
     rosterItems.addElement( MidletMain.mainFrame.buddyList.tempGroupItem );
     /** Inserting general group to the end **/
     MidletMain.mainFrame.buddyList.generalGroupItem = ( GroupItem ) params.get( "GENERAL" );
     MidletMain.mainFrame.buddyList.generalGroupItem.isGroupVisible = true;
     rosterItems.addElement( MidletMain.mainFrame.buddyList.generalGroupItem );
     /** Setting up collected roster vector **/
     MidletMain.mainFrame.buddyList.setBuddyItems( rosterItems );
     /** Sending request to chatframe for update 
      * buddy items and resources for opened tabs **/
     MidletMain.chatFrame.updateBuddyItems();
     /** Repainting **/
     MainFrame.repaintFrame();
     ChatFrame.repaintFrame();
   }
 
   /**
    * Setting up services
    * @param services 
    */
   public static void setServices( Hashtable services ) {
     /** Definig services group **/
     MidletMain.mainFrame.buddyList.servicesGroupItem = new GroupItem( Localization.getMessage( "SERVICES" ) );
     MidletMain.mainFrame.buddyList.servicesGroupItem.internalGroupId = GroupItem.GROUP_SERVICES_ID;
     /** Cycling elements **/
     Vector items = ( Vector ) services.get( "ITEMS" );
     if ( items != null ) {
       for ( int c = 0; c < items.size(); c++ ) {
         Item item = ( Item ) items.elementAt( c );
         ServiceItem serviceItem = new ServiceItem( item.jid, item.name );
         serviceItem.updateUi();
         if ( serviceItem.isServiceSupported() ) {
           MidletMain.mainFrame.buddyList.servicesGroupItem.addChild( serviceItem );
         }
       }
     }
   }
 
   /**
    * Setting up bookmarks
    * @param services 
    */
   public static void setBookmarks( Vector items ) {
     /** Checking for items existance **/
     if ( items != null ) {
       MidletMain.mainFrame.buddyList.roomsGroupItem.setChilds( items );
     }
   }
 
   /**
    * Setting up bookmarks
    * @param services 
    */
   public static void setBookmarks( Hashtable params ) {
     /** Definig rooms group **/
     MidletMain.mainFrame.buddyList.roomsGroupItem = new GroupItem( Localization.getMessage( "ROOMS" ) );
     MidletMain.mainFrame.buddyList.roomsGroupItem.internalGroupId = GroupItem.GROUP_ROOMS_ID;
     /** Obtain elements **/
     Vector items = ( Vector ) params.get( "BOOKMARKS" );
     /** Setting up items **/
     setBookmarks( items );
   }
 
   /**
    * Showing buddy presence
    * @param from
    * @param show
    * @param priority
    * @param status 
    */
   public static void setPresence( String from, String show, int priority,
           String status, String caps, String ver, boolean isInvalidBuddy,
           Hashtable params ) {
     String clearJid = BuddyList.getClearJid( from );
     String resource = BuddyList.getJidResource( from );
     LogUtil.outMessage( "Presence: " + from );
     /** If this is self presence, changing status in AccountRoot **/
     if ( from.equals( AccountRoot.getFullJid() ) ) {
       AccountRoot.setStatusIndex( StatusUtil.getStatusIndex( show ) );
     } else {
       /** Searching for buddy in roster **/
       BuddyItem buddyItem = MidletMain.mainFrame.buddyList.getBuddyItem( clearJid );
       /** Checking for buddy item existing in roster **/
       if ( buddyItem == null ) {
         /** Buddy not exist, according to the 
          * RFC 3921 presence must be ignored **/
         LogUtil.outMessage( "BuddyItem not exists, according to the RFC 3921, "
                 + "presence must be ignored" );
         return;
       } else {
         /** Buddy exists **/
         LogUtil.outMessage( "Buddy exists" );
         Resource t_resource = buddyItem.getResource( resource );
         t_resource.setStatusIndex( StatusUtil.getStatusIndex( show ) );
         t_resource.setStatusText( status );
         t_resource.setCaps( caps );
         t_resource.setVer( ver );
         buddyItem.setBuddyInvalid( isInvalidBuddy );
         /** Updating buddyItem status **/
         buddyItem.getStatusIndex();
         /** Checking for setting, status and resource usage **/
         if ( Settings.isRemoveOfflineResources
                 && ( StatusUtil.getStatusIndex( show ) == StatusUtil.offlineIndex )
                 && !MidletMain.chatFrame.getBuddyResourceUsed( clearJid, resource ) ) {
           /** Resource is offline, settings is to 
            * remove offline resource and resource is not used **/
           LogUtil.outMessage( "Removing resource: ".concat( resource ) );
           buddyItem.removeResource( resource );
         }
         /** Checking for MUC presence information **/
         if ( !params.isEmpty()
                 && buddyItem.getInternalType() == BuddyItem.TYPE_ROOM_ITEM ) {
           /** Creating room instance from buddy item **/
           RoomItem roomItem = ( RoomItem ) buddyItem;
           /** Checking for error code **/
           if ( params.containsKey( "ERROR_CAUSE" ) ) {
             String error_cause = ( String ) params.get( "ERROR_CAUSE" );
             Handler.showError( "MUC_".concat( error_cause ) );
           } else {
             /** Obtain affiliation, JID, role and nick **/
             String muc_affiliation = null;
             String muc_jid = null;
             String muc_role = null;
             String muc_nick = null;
             if ( params.containsKey( "AFFILIATION" ) ) {
               /** Affiliation in MUC **/
               muc_affiliation = ( String ) params.get( "AFFILIATION" );
               t_resource.setAffiliation(
                       RoomUtil.getAffiliationIndex( muc_affiliation ) );
             }
             if ( params.containsKey( "JID" ) ) {
               /** JID in MUC **/
               muc_jid = ( String ) params.get( "JID" );
               t_resource.setJid( muc_jid );
             }
             if ( params.containsKey( "ROLE" ) ) {
               /** Role in MUC **/
               muc_role = ( String ) params.get( "ROLE" );
               t_resource.setRole( RoomUtil.getRoleIndex( muc_role ) );
             }
             if ( params.containsKey( "NICK" ) ) {
               /** Nick in MUC **/
               muc_nick = ( String ) params.get( "NICK" );
             }
             if ( params.containsKey( "STATUS_110" )
                     || ( muc_jid != null
                     && muc_jid.equals( AccountRoot.getFullJid() ) ) ) {
               /** Inform user that presence refers to itself **/
               if ( muc_affiliation != null ) {
                 /** Applying self-affiliation **/
                 roomItem.setAffiliation(
                         RoomUtil.getAffiliationIndex( muc_affiliation ) );
               }
               /** Checking for role **/
               if ( muc_role != null ) {
                 /** Applying self-role **/
                 roomItem.setRole(
                         RoomUtil.getRoleIndex( muc_role ) );
               }
             }
             /** Status codes **/
             if ( params.containsKey( "STATUS_100" ) ) {
               /** Inform user that any occupant is 
                * allowed to see the user’s full JID **/
               roomItem.setNonAnonimous( true );
             } else {
               /** Room is anonymous **/
               roomItem.setNonAnonimous( false );
             }
             if ( params.containsKey( "STATUS_101" ) ) {
               /** Inform user that his or her affiliation 
                * changed while not in the room **/
               showDialog( "INFO", "AFFL_WAS_CHANGED" );
             }
             if ( params.containsKey( "STATUS_102" ) ) {
               /** Inform occupants that room now shows unavailable members **/
             }
             if ( params.containsKey( "STATUS_103" ) ) {
               /** Inform occupants that room now does 
                * not show unavailable members **/
             }
             if ( params.containsKey( "STATUS_104" ) ) {
               /** Inform occupants that a non-privacy-related room,
                * configuration change has occurred **/
             }
             if ( ( params.containsKey( "STATUS_110" ) )
                     && !params.containsKey( "STATUS_303" ) ) {
               /** Setting up room active or inactive 
                * if it is not already active **/
               boolean isRoomActive = roomItem.getRoomActive();
               if ( roomItem.setRoomActive(
                       t_resource.statusIndex != StatusUtil.offlineIndex ) ) {
                 /** Checking for room became active **/
                 if ( !isRoomActive && roomItem.getRoomActive() ) {
                   /** Handling room entering is complete **/
                   Handler.roomEnteringComplete( roomItem,
                           params.containsKey( "STATUS_201" ), true );
                 }
               } else {
                 /** Hiding wait screen **/
                 MidletMain.screen.setWaitScreenState( false );
               }
             }
             if ( params.containsKey( "STATUS_170" ) ) {
               /** Room logging is enabled **/
             }
             if ( params.containsKey( "STATUS_171" ) ) {
               /** Room logging is disabled **/
             }
             if ( params.containsKey( "STATUS_172" ) ) {
               /** Room is now non-anonymous **/
               roomItem.setNonAnonimous( true );
             }
             if ( params.containsKey( "STATUS_173" ) ) {
               /** Room is now semi-anonymous **/
               roomItem.setNonAnonimous( false );
             }
             if ( params.containsKey( "STATUS_201" ) ) {
               /** Room is created **/
             }
             if ( params.containsKey( "STATUS_210" ) ) {
               /** Inform user that service has assigned or modified,
                * occupant's room nick **/
             }
             if ( params.containsKey( "STATUS_301" ) ) {
               /** User is banned **/
               roomItem.setResourcesOffline();
               /** In this case, user banned, but 
                * status updated for whole room **/
               resource = "";
               /** Showing error **/
               showError( "ROOM_VISITOR_BANNED" );
             }
             if ( params.containsKey( "STATUS_303" ) ) {
               /** Inform all occupants of new room nickname **/
               LogUtil.outMessage( "Inform all occupants of new room nickname" );
               LogUtil.outMessage( "room nick: " + roomItem.getRoomNick() );
               LogUtil.outMessage( "resource : " + t_resource.resource );
               /** Checking for this is our new nick **/
               if ( muc_nick.equals( roomItem.getRoomNick() ) ) {
                 /** This is our nick - update bookmark **/
                 RoomItem item = new RoomItem( roomItem.getJid(),
                         roomItem.getNickName(), roomItem.getMinimize(),
                         roomItem.getAutoJoin() );
                 /** Updating parameters **/
                 item.setRoomNick( muc_nick );
                 item.setRoomPassword( roomItem.getRoomPassword() );
                 /** Mechanism invocation **/
                 Mechanism.sendBookmarksOperation( Mechanism.OPERATION_EDIT,
                         roomItem, item, false, true );
               }
               /** Checking for chat tab **/
               ChatTab chatTab = MidletMain.chatFrame.getChatTab( clearJid,
                       "", false );
               if ( chatTab != null ) {
                 /** Check and prepare message **/
                 String message = "[p][i][b][c=purple]".concat( resource ).
                         concat( "[/c][/b][/i] " ).concat(
                         Localization.getMessage( "CHANGED_NICK" ) ).
                         concat( " [i][b][c=purple]" ).concat( muc_nick ).
                         concat( "[/c][/b][/i][/p]" );
                 /** Showing message in chat tab **/
                 boolean isTabActive = MidletMain.chatFrame.addChatItem(
                         chatTab, AccountRoot.generateCookie(),
                         ChatItem.TYPE_PLAIN_MSG, true, resource, message );
                 if ( !( isTabActive && MidletMain.screen.activeWindow.
                         equals( MidletMain.chatFrame ) ) ) {
                   /** Chat tab is not active or ChatFrame 
                    * is not on the screen **/
                   chatTab.resource.unreadCount++;
                   /** Check for first unread message **/
                   if ( chatTab.resource.unreadCount == 1 ) {
                     /** Buddy item UI update **/
                     chatTab.buddyItem.updateUi();
                     /** Chat tab UI update **/
                     chatTab.updateUi();
                   }
                 }
               }
             }
             if ( params.containsKey( "STATUS_307" ) ) {
               /** User is kicked **/
               roomItem.setResourcesOffline();
               /** In this case, user banned, but 
                * status updated for whole room **/
               resource = "";
               /** Showing error **/
               showError( "ROOM_VISITOR_KICKED" );
             }
             if ( params.containsKey( "STATUS_321" ) ) {
               /** Room is members-only and user affiliation changed **/
               roomItem.setResourcesOffline();
               /** In this case, user banned, but 
                * status updated for whole room **/
               resource = "";
               /** Showing error **/
               showError( "ROOM_MEMBERS_ONLY_AFFL_CHG" );
             }
             if ( params.containsKey( "STATUS_322" ) ) {
               /** User is non-member in members-only room and kicked out **/
               roomItem.setResourcesOffline();
               /** In this case, user banned, but 
                * status updated for whole room **/
               resource = "";
               /** Showing error **/
               showError( "ROOM_BECAME_MEMBERS_ONLY" );
             }
             if ( params.containsKey( "STATUS_332" ) ) {
               /** Inform user that he or she is being removed from the room,
                * because the MUC service is being shut down **/
               roomItem.setResourcesOffline();
               /** In this case, user banned, but 
                * status updated for whole room **/
               resource = "";
               /** Showing error **/
               showError( "SYSTEM_SHUTDOWN" );
             }
           }
         }
         buddyItem.updateUi();
         /** Updating opened chat tab **/
         MidletMain.chatFrame.updateTab( clearJid, resource );
       }
     }
     /** Repainting **/
     MainFrame.repaintFrame();
     ChatFrame.repaintFrame();
     LogUtil.outMessage( "Buddy presence OK" );
   }
 
   /**
    * Shows message in chat frame
    * @param from
    * @param type
    * @param id
    * @param message 
    */
   public static void setMessage( String from, String type, String id, String message, String subject ) {
     String jid = BuddyList.getClearJid( from );
     String nickName;
     String resource;
     /** Checking for message type to detect resource **/
     if ( type.equals( "groupchat" ) ) {
       resource = "";
     } else {
       resource = BuddyList.getJidResource( from );
     }
     /** Checking for chat tab **/
     ChatTab chatTab = MidletMain.chatFrame.getChatTab( jid, resource, false );
     if ( chatTab == null ) {
       /** Searching for buddy **/
       BuddyItem buddyItem = MidletMain.mainFrame.buddyList.getBuddyItem( jid );
       if ( buddyItem == null ) {
         LogUtil.outMessage( "No such buddy in roster. " );
         /** Creating buddy item **/
         buddyItem = MidletMain.mainFrame.buddyList.createTempBuddyItem( jid );
       }
       Resource resourceObject = buddyItem.getResource( resource );
       /** There is no opened chat tab, checking for default resource chat **/
       ChatTab tempTab = MidletMain.chatFrame.getStandAloneOnlineResourceTab( jid );
       if ( tempTab != null && tempTab.resource.resource.equals( "" )
               && !tempTab.isMucTab() ) {
         /** Default empty resource chat found
          This chat tab must be replaced for incoming resource, 
          because, maybe, it really one alive now? **/
         LogUtil.outMessage( "Reassign chat tab from empty to named: ".concat( resource ) );
         tempTab.resource = resourceObject;
         chatTab = tempTab;
         /** Chat tab UI update **/
         chatTab.updateUi();
       } else {
         /** Crating chat tab **/
         chatTab = new ChatTab( buddyItem, resourceObject );
         MidletMain.chatFrame.addChatTab( chatTab, false );
       }
     }
     /** Defining nick name and applying subject **/
     if ( chatTab.isMucTab() ) {
       nickName = BuddyList.getJidResource( from );
       /** Checking for message is topic **/
       if ( subject != null && ( message == null || nickName.length() == 0 ) ) {
         ( ( RoomItem ) chatTab.buddyItem ).setRoomTopic( subject );
       }
     } else {
       nickName = chatTab.buddyItem.getNickName();
     }
     /** Check and prepare message **/
     message = ChatFrame.checkMessage( nickName, message, subject, chatTab.isMucTab() );
     /** Showing message in chat tab **/
     boolean isTabActive = MidletMain.chatFrame.addChatItem( chatTab, id,
             ChatItem.TYPE_PLAIN_MSG, true, nickName, message );
     if ( !( isTabActive && MidletMain.screen.activeWindow.equals( MidletMain.chatFrame ) ) ) {
       /** Chat tab is not active or ChatFrame is not on the screen **/
       chatTab.resource.unreadCount++;
       /** Check for first unread message **/
       if ( chatTab.resource.unreadCount == 1 ) {
         /** Buddy item UI update **/
         chatTab.buddyItem.updateUi();
         /** Chat tab UI update **/
         chatTab.updateUi();
       }
     }
     /** Repainting **/
     MidletMain.screen.repaint();
   }
 
   /**
    * Updating nick name for specified JID
    * @param fullJid
    * @param nick 
    */
   public static void setNickName( String fullJId, String nick ) {
     String jid = BuddyList.getClearJid( fullJId );
     /** Searching for buddy **/
     BuddyItem buddyItem = MidletMain.mainFrame.buddyList.getBuddyItem( jid );
     if ( buddyItem == null ) {
       LogUtil.outMessage( "No such buddy in roster: ".concat( jid ) );
     } else {
       /** Setting up nick name **/
       buddyItem.setNickName( nick );
       buddyItem.updateUi();
       /** Repainting **/
       MainFrame.repaintFrame();
       ChatFrame.repaintFrame();
     }
   }
 
   /** User-side connection established event **/
   public static void connectedEvent() {
     /** Changing user theme **/
     LogUtil.outMessage( "Connected event" );
     Theme.startThemeChange( Settings.themeOfflineResPath, Settings.themeOnlineResPath );
   }
 
   /** User-side disconnect showing **/
   public static void disconnectEvent() {
     if ( AccountRoot.getStatusIndex() != StatusUtil.offlineIndex ) {
       LogUtil.outMessage( "Disconnected event" );
       /** Updating account status **/
       AccountRoot.setStatusIndex( StatusUtil.offlineIndex );
       /** Changing roster items status **/
       MidletMain.mainFrame.buddyList.setRosterOffline();
       MidletMain.chatFrame.updateTabs();
       /** Repainting **/
       MainFrame.repaintFrame();
       ChatFrame.repaintFrame();
       /** Changing user theme **/
       Theme.startThemeChange( Settings.themeOnlineResPath, Settings.themeOfflineResPath );
     }
   }
 
   /** Configures and appends defined register item to the specified popup **/
   public static void appendRegisterEvent( PopupItem popupItem, ServiceItem serviceItem ) {
     serviceItem.getRegisterPopup().name = serviceItem.getJid();
     /** Checking for service connected **/
     if ( serviceItem.isConnected() || serviceItem.isBuddyInvalid() ) {
       popupItem.addSubItem( serviceItem.getUnRegisterPopup() );
     } else {
       popupItem.addSubItem( serviceItem.getRegisterPopup() );
     }
   }
 
   /** Appends command items to specified popup item **/
   public static void appendCommands( final PopupItem parentPopup, Hashtable params ) {
     /** Defining node name **/
     String node = ( String ) params.get( "NODE" );
     if ( node != null ) {
       parentPopup.name = ( String ) node;
     }
     /** Cycling items **/
     Vector items = ( Vector ) params.get( "ITEMS" );
     if ( items != null ) {
       for ( int c = 0; c < items.size(); c++ ) {
         final Item item = ( Item ) items.elementAt( c );
         PopupItem popupItem = new PopupItem( item.name ) {
           public void actionPerformed() {
             /** Showing wait screen **/
             MidletMain.screen.setWaitScreenState( true );
             LogUtil.outMessage( "Command execute: " + item.node + " for: " + item.jid );
             Mechanism.executeCommand( item );
           }
         };
         popupItem.name = item.node;
         parentPopup.addSubItem( popupItem );
       }
     }
   }
 
   /**
    * Updates rooms frame items
    * @param roomsBrowseFrame
    * @param params 
    */
   public static void updateRoomsFrame( final RoomsFrame roomsFrame, Hashtable params ) {
     /** Cycling items **/
     Vector items = ( Vector ) params.get( "ITEMS" );
     /** Creating new swap vector **/
     Vector listItems = new Vector();
     /** Create room list item **/
     ListItem listItem = new ListItem( Localization.getMessage( "CREATE_ROOM" ) );
     listItems.addElement( listItem );
     /** Checking for items in not null-type **/
     if ( items != null ) {
       /** Adding all items to listItems **/
       for ( int c = 0; c < items.size(); c++ ) {
         Item item = ( Item ) items.elementAt( c );
         listItems.addElement( new DiscoItem( item.name, item.jid ) );
       }
     }
     /** Applying collected swap **/
     roomsFrame.getList().items = listItems;
     roomsFrame.updateTime = System.currentTimeMillis();
   }
 
   /**
    * Creates new temporary room
    * @param roomIndex
    * @return RoomItem
    */
   public static RoomItem createTempRoomItem( int roomIndex ) {
     /** Creating new room item **/
     RoomItem roomItem = new RoomItem( String.valueOf( roomIndex ).concat( "@" ).concat( AccountRoot.getRoomHost() ), String.valueOf( roomIndex ), false, false );
     roomItem.setRoomNick( AccountRoot.getNickName() );
     roomItem.setTemp( true );
     roomItem.updateUi();
     /** Adding room item to roster **/
     MidletMain.mainFrame.buddyList.roomsGroupItem.addChild( roomItem );
     return roomItem;
   }
 
   /**
    * Method for caching rooms frame cause it's weight
    * @return RoomsFrame
    */
   public static RoomsFrame getRoomsFrame() {
     /** Checking for rooms frame unexistance **/
     if ( MidletMain.roomsFrame == null ) {
       /** Creating rooms frame instance **/
       MidletMain.roomsFrame = new RoomsFrame();
     }
     if ( System.currentTimeMillis() - MidletMain.roomsFrame.updateTime > Settings.roomsUpdateDelay ) {
       /** Mechanism invocation **/
       Mechanism.sendRoomsItemsDiscoveryRequest();
     }
     return MidletMain.roomsFrame;
   }
 
   public static void resetRoomsCache() {
     /** Checking for rooms frame is initialized **/
     if ( MidletMain.roomsFrame != null ) {
       MidletMain.roomsFrame.resetRoomsCache();
     }
   }
 
   /**
    * Showing rooms frame
    */
   public static void showRoomsFrame() {
     /** Checking for frame not active now **/
     if ( !MidletMain.screen.activeWindow.equals( getRoomsFrame() ) ) {
       /** Showing rooms frame **/
       MidletMain.screen.setActiveWindow( getRoomsFrame() );
     }
   }
 
   /**
    * Creating instace, configurating and showing room more frame
    * @param params 
    */
   public static void showRoomMoreFrame( DiscoItem discoItem, Hashtable params ) {
     /** Creating frame instance **/
     RoomMoreFrame roomMoreFrame = new RoomMoreFrame( discoItem );
     /** Checcking for form exist **/
     if ( params.containsKey( "FORM" ) ) {
       /** Setting up main objects **/
       roomMoreFrame.setMainObjects(
               ( ( Form ) params.get( "FORM" ) ).objects );
     } else {
       roomMoreFrame.setMainObjects( new Vector() );
     }
     /** Obtain keys enumeration **/
     Enumeration keys = params.keys();
     /** Variables **/
     String key;
     /** Cycling all keys **/
     while ( keys.hasMoreElements() ) {
       key = ( String ) keys.nextElement();
       /** Checking for form key **/
       if ( key.equals( "FORM" ) ) {
         /** Form exist **/
       } else if ( key.equals( "IDENT_CATG" ) ) {
         LogUtil.outMessage( key.concat( " = " ).concat( ( String ) params.get( key ) ) );
       } else if ( key.equals( "IDENT_TYPE" ) ) {
         LogUtil.outMessage( key.concat( " = " ).concat( ( String ) params.get( key ) ) );
       } else if ( key.equals( "IDENT_NAME" ) ) {
         LogUtil.outMessage( key.concat( " = " ).concat( ( String ) params.get( key ) ) );
         roomMoreFrame.setHeader( ( String ) params.get( key ) );
       } else {
         LogUtil.outMessage( "feature: ".concat( key ) );
         roomMoreFrame.addFeatureItem( key );
       }
     }
     /** Showing frame **/
     MidletMain.screen.setActiveWindow( roomMoreFrame );
   }
 
   /**
    * Room entering is complete event
    * @param roomItem
    * @param isCreated
    * @param isCleanChat 
    */
   public static void roomEnteringComplete( RoomItem roomItem, boolean isCreated, boolean isCleanChat ) {
     /** Checking for room item temp status **/
     if ( isCreated ) {
       /** Loading room configuration frame */
       Mechanism.configureRoomRequest( roomItem, true );
     } else {
       /** Opening room in chat frame **/
       MidletMain.mainFrame.openDialog( roomItem, roomItem.getResource( "" ), isCleanChat );
       /** Hiding wait screen **/
       MidletMain.screen.setWaitScreenState( false );
     }
   }
 
   public static void showRoomVisitorsListEditFrame( RoomItem roomItem,
           String affiliation, Vector items ) {
     /** Creating frame instance **/
     RoomVisitorsEditFrame roomVisitorsEditFrame = new RoomVisitorsEditFrame( roomItem, affiliation, items );
     /** Disabling wait screen **/
     MidletMain.screen.setWaitScreenState( false );
     /** Setting up frame as current **/
     MidletMain.screen.setActiveWindow( roomVisitorsEditFrame );
   }
 
   /**
    * Closing all opened chat tabs of this jid 
    * and removing buddy item if it temporary
    * Returns boolean flag - true if buddy permanent, false - if not
    * @param jid 
    * @return boolean
    */
   public static boolean closeOpenedTabs( BuddyItem buddyItem ) {
     /** Removing tabs **/
     MidletMain.chatFrame.removeChatTabs( buddyItem.getJid() );
     /** Checking for item is temporary **/
     if ( buddyItem.getTemp() ) {
       /** Removing buddy from groups **/
       MidletMain.mainFrame.buddyList.removeBuddyFromGroups( buddyItem );
       return false;
     }
     return true;
   }
 
   /**
    * Checks for bookmark existance and adds if not exist
    * @param discoItem 
    */
   public static RoomItem getBookmark( DiscoItem discoItem ) {
     /** Checking for bookmarks existance **/
     if ( getBuddyList().roomsGroupItem.getChildsCount() > 0 ) {
       /** Obtain rooms **/
       Vector items = getBuddyList().roomsGroupItem.getChilds();
       /** Cycling all rooms **/
       for ( int c = 0; c < items.size(); c++ ) {
         RoomItem roomItem = ( RoomItem ) items.elementAt( c );
         /** Checking for jid equals **/
         if ( roomItem.getJid().equals( discoItem.getJid() ) ) {
           return roomItem;
         }
       }
     }
     /** No such room item **/
     return null;
   }
 
   /**
    * Returns buddy list
    * @return BuddyList
    */
   public static BuddyList getBuddyList() {
     return MidletMain.mainFrame.buddyList;
   }
 
   /**
    * Returns buddy items JID in array by items specified host
    * @param serviceHost
    * @return String[]
    */
   public static String[] getServiceItems( String serviceHost ) {
     return MidletMain.mainFrame.buddyList.getServiceItems( serviceHost );
   }
 
   /**
    * Sends pong to ping initiator
    * @param xmlWriter
    * @param from
    * @param id 
    */
   public static void sendPong( Session session, String from, String id ) {
     /** Sending pong via mechanism **/
     Mechanism.sendPong( session, from, id );
   }
 
   /**
    * Shows main frame popup item on right soft
    * @param popupItem 
    */
   public static void showMainFrameElementPopup( PopupItem popupItem ) {
     /** Showing right soft **/
     MidletMain.mainFrame.soft.rightSoft = popupItem;
     MidletMain.mainFrame.soft.setRightSoftPressed( true );
     /** Hiding wait screen state **/
     MidletMain.screen.setWaitScreenState( false );
   }
 
   /**
    * Shows command frame for specified item and form
    * @param item
    * @param form 
    */
   public static void showCommandFrame( Item item, Form form, PopupItem rightSoft ) {
     if ( form.objects == null
             || ( form.status != null && form.status.equals( "completed" ) && form.objects.isEmpty() )
             || form.objects.isEmpty() ) {
       MidletMain.screen.setActiveWindow( MidletMain.mainFrame );
     } else {
       CommandFrame commandFrame = new CommandFrame( item, rightSoft );
       commandFrame.setFormData( form );
       MidletMain.screen.setActiveWindow( commandFrame );
     }
     /** Hiding wait notify **/
     MidletMain.screen.setWaitScreenState( false );
   }
 
   /**
    * Showing buddy add frame by specified host, description and prompt
    * @param serviceHost
    * @param desc
    * @param prompt 
    */
   public static void showBuddyAddFrame( String serviceHost, String desc, String prompt ) {
     /** Showing buddy add frame **/
     MidletMain.screen.setActiveWindow( new BuddyAddFrame( serviceHost, desc, prompt ) );
     /** Hiding wait notify **/
     MidletMain.screen.setWaitScreenState( false );
   }
 
   /**
    * Checking for active window, showing main 
    * frame and disabling wait screen state
    */
   public static void showMainFrame() {
     /** Switching to main frame window **/
     showNextFrame( MidletMain.mainFrame );
   }
 
   /**
    * Checking for active window, showing specified 
    * frame and disabling wait screen state
    */
   public static void showNextFrame( Window nextFrame ) {
     /** Checking for active window **/
     if ( !MidletMain.screen.activeWindow.equals( nextFrame ) ) {
       /** Showing main frame **/
       MidletMain.screen.setActiveWindow( nextFrame );
     }
     /** Hiding wait screen state **/
     MidletMain.screen.setWaitScreenState( false );
     /** Repaint main frame **/
     MidletMain.screen.repaint();
   }
 
   /**
    * Checking for online active connection to server
    * @return boolean
    */
   public static boolean sureIsOnline() {
     /** Checking for online status **/
     if ( AccountRoot.isOffline() ) {
       Handler.showError( "ONLINE_REQUIRED" );
       return false;
     }
     return true;
   }
 
   /**
    * Checking for main frame is active 
    * window and sending repaint request
    */
   public static void repaintMainFrame() {
     /** Checking for main frame opened **/
     if ( MidletMain.screen.activeWindow.equals( MidletMain.mainFrame ) ) {
       /** Repainting main frame **/
       MidletMain.screen.repaint();
     }
   }
 
   /**
    * All-in-one subscription mechanisms, followed specified policy
    * @param jid
    * @param actionType 
    */
   public static void showSubscriptionAction( final String jid, final String actionType ) {
     /** Checking for service type of JID **/
     if ( jid.indexOf( '@' ) == -1 ) {
       /** Checking for subscription action type **/
       if ( actionType.equals( "SUBSCRIBE" ) ) {
         /** Automatically approve request **/
         Mechanism.sendSubscriptionApprove( jid );
         /** Automatically generating subscription request **/
         Mechanism.sendSubscriptionRequest( jid );
       }
       return;
     }
     /** Searching for buddy **/
     if ( MidletMain.mainFrame.buddyList.getBuddyItem( jid ) == null ) {
       LogUtil.outMessage( "No such buddy in roster. " );
       /** Creating buddy item **/
       MidletMain.mainFrame.buddyList.createTempBuddyItem( jid );
     }
     /** Checking for subscription action type **/
     if ( actionType.equals( "SUBSCRIBED" ) ) {
       /** Checking for ignore subscription approve messages **/
       if ( Settings.isHideSubscriptionApproveMessage ) {
         /** Nothing to do in this case **/
         return;
       }
     } else if ( actionType.equals( "SUBSCRIBE" ) ) {
       /** Checking for automatic subscription requests approve **/
       if ( Settings.isAutomatedSubscriptionApprove ) {
         LogUtil.outMessage( "Sending subscription approve to buddy: ".concat( jid ) );
         /** Sending subscription approve **/
         Mechanism.sendSubscriptionApprove( jid );
         /** Checking subscription status **/
         BuddyItem buddyItem = MidletMain.mainFrame.buddyList.getBuddyItem( jid );
         /** Checking for buddy item existance and subscription type **/
         if ( buddyItem == null || buddyItem.getSubscription().equals( "none" )
                 || buddyItem.getSubscription().equals( "from" ) ) {
           /** Sending subscription request **/
           LogUtil.outMessage( "Sending subscription request to: ".concat( jid ) );
           Mechanism.sendSubscriptionRequest( jid );
         }
         return;
       }
     }
     /** Obtain window **/
     final Window window = MidletMain.screen.activeWindow;
     /** Creating soft and dialog **/
     final Soft dialogSoft = new Soft( MidletMain.screen );
     /** Left action soft **/
     dialogSoft.leftSoft = new PopupItem( Localization.getMessage( "CLOSE" ) ) {
       public void actionPerformed() {
         window.closeDialog();
       }
     };
     /** Right action soft **/
     dialogSoft.rightSoft = new PopupItem( Localization.getMessage( actionType.concat( "_ACTION" ) ) ) {
       public void actionPerformed() {
         /** Checking for action type **/
         if ( actionType.equals( "SUBSCRIBE" ) ) {
           Mechanism.sendSubscriptionApprove( jid );
         } else if ( actionType.equals( "UNSUBSCRIBED" ) ) {
           Mechanism.sendSubscriptionReject( jid );
         } else {
           /** Nothing to do in this case **/
           return;
         }
         /** Closing dialog after action **/
         dialogSoft.leftSoft.actionPerformed();
       }
     };
     /** Setting up messages **/
     String title = actionType.concat( "_TITILE" );
     String message = Localization.getMessage(
             actionType.concat( "_MESSAGE" ) ).concat( ": " ).concat( jid ).
             concat( "\n" ).
             concat( Localization.getMessage( actionType.concat( "_COMMENT" ) ) );
     /** Showing dialog **/
     showDialog( window, dialogSoft, title, message );
   }
 
   /**
    * Checking for buddy used in chat 
    * frame and storing it into temp group
    * @param buddyItem 
    */
   public static void checkBuddyUsage( BuddyItem buddyItem ) {
     /** Checking for opened dialogs **/
     if ( MidletMain.chatFrame.getBuddyResourceUsed( buddyItem.getJid(), null ) ) {
       /** Setting up "temp" flag **/
       buddyItem.setTemp( true );
       /** Carry this buddy in temporary group **/
       MidletMain.mainFrame.buddyList.tempGroupItem.addChild( buddyItem );
     }
   }
 
   /**
    * Sending roster push result
    * @param iqId 
    */
   public static void sendPushResult( String iqId ) {
     Mechanism.sendIqResult( iqId );
   }
 
   public static void sendDiscoInfo( String cookie, String jid ) {
     /** Sending disco info by mechanism **/
     Mechanism.sendDiscoInfo( cookie, jid );
   }
 
   public static void sendLastActivity( String cookie, String jid ) {
     /** Sending last activity by mechanism **/
     Mechanism.sendLastActivity( cookie, jid );
   }
 
   public static void sendEntityTime( String cookie, String jid ) {
     /** Sending entity time by mechanism **/
     Mechanism.sendEntityTime( cookie, jid );
   }
 
   public static void sendVersion( String cookie, String jid ) {
     /** Sending version by mechanism **/
     Mechanism.sendVersion( cookie, jid );
   }
 
   public static void sendTime() {
   }
 
   public static void showError( String errorCause ) {
     /** Checking error **/
     if ( errorCause.equals( "" ) ) {
       /** Cause unknown **/
       LogUtil.outMessage( "Cause unknown" );
       errorCause = "CAUSE_UNKNOWN";
     }
     /** Showing dialog **/
     showDialog( "ERROR", errorCause );
   }
 
   /**
    * Shows error in active window
    * @param errorCause
    * @param window 
    */
   public static void showDialog( String title, String message ) {
     /** Obtain window **/
     final Window window = MidletMain.screen.activeWindow;
     /** Hiding wait screen state **/
     MidletMain.screen.setWaitScreenState( false );
     /** Creating soft and dialog **/
     Soft dialogSoft = new Soft( MidletMain.screen );
     dialogSoft.leftSoft = new PopupItem( Localization.getMessage( "CLOSE" ) ) {
       public void actionPerformed() {
         window.closeDialog();
       }
     };
     /** Showing dialog **/
     showDialog( window, dialogSoft, title, Localization.getMessage( message ) );
   }
 
   /**
    * Showing frame dialog for specified window, soft, 
    * title (not localized) and message (localized)
    * @param window
    * @param dialogSoft
    * @param title
    * @param message 
    */
   public static void showDialog( Window window, Soft dialogSoft, String title,
           String message ) {
     /** Hiding wait screen state **/
     MidletMain.screen.setWaitScreenState( false );
     /** Creating soft and dialog **/
     Dialog resultDialog = new Dialog( MidletMain.screen, dialogSoft,
             Localization.getMessage( title ), message );
     /** Showing new dialog **/
     window.showDialog( resultDialog );
   }
 }
