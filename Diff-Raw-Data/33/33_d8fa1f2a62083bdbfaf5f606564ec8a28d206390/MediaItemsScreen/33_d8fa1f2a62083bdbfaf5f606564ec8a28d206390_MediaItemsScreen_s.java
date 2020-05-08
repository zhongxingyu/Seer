 /*******************************************************************************
  * Copyright (c) 2011-2012 Nokia Corporation
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Comarch team - initial API and implementation
  *******************************************************************************/
 package org.ned.client.view;
 
 import com.sun.lwuit.Display;
 import com.sun.lwuit.List;
 import com.sun.lwuit.events.ActionEvent;
 import com.sun.lwuit.events.ActionListener;
 import com.sun.lwuit.events.SelectionListener;
 import java.util.Vector;
 import org.ned.client.Content;
 import org.ned.client.NedMidlet;
 import org.ned.client.command.*;
 import org.ned.client.library.advanced.LibraryElement;
 import org.ned.client.library.advanced.LibraryHelpers;
 import org.ned.client.view.renderer.MediaItemsListCellRenderer;
 
 public class MediaItemsScreen extends NedFormBase implements ActionListener, SelectionListener {
 
     private List mMediaList;
 
     public MediaItemsScreen( String id ) {
         super( id );
         setNedTitle( mNewLibModel != null ? mNewLibModel.getName() : " " );
 
         mMediaList = new List( mNewLibModel != null
                                ? LibraryHelpers.sortBy( mNewLibModel.getChildern(), NedMidlet.getSettingsManager().
                 getSortBy() ) : new Vector( 0 ) );
         mMediaList.setFixedSelection( List.FIXED_NONE );
         mMediaList.setSelectedIndex( 0 );
         mMediaList.setRenderer( new MediaItemsListCellRenderer() );
         mMediaList.setPreferredW( Display.getInstance().getDisplayWidth() );
         mMediaList.addSelectionListener( this );
         addComponent( mMediaList );
 
         addGameKeyListener( Display.GAME_LEFT, this );
         addGameKeyListener( Display.GAME_FIRE, this );
         mMediaList.addActionListener( this );
         addCommandListener( this );
     }
 
     public MediaItemsScreen( LibraryElement mediaElement ) {
         this( mediaElement.getParent().getId() );
         mMediaList.setSelectedItem( mediaElement );
     }
 
     public void actionPerformed( ActionEvent evt ) {
         Object src = evt.getSource();
 
         if ( src == BackMediaItemsCommand.getInstance().getCommand()
                 || evt.getKeyEvent() == Display.GAME_LEFT ) {
             BackMediaItemsCommand.getInstance().execute( mNewLibModel.getParent().
                     getId() );
         } else if ( src == ShowDetailsCommand.getInstance().getCommand() ) {
             ShowDetailsCommand.getInstance().execute( ((LibraryElement)mMediaList.getSelectedItem()).getDetails() );
         } else if ( src == DeleteContentCommand.getInstance().getCommand() ) {
             DeleteContentCommand.getInstance().execute( mMediaList );
         } else if ( src == InstantDownloadCommand.getInstance().getCommand() ) {
             InstantDownloadCommand.getInstance().execute( ((LibraryElement)mMediaList.getSelectedItem()).getDetails() );
             new DownloadQueueScreen( mMediaList.getSelectedItem() ).show();
         } else if ( src == GoToStartCommand.getInstance().getCommand() ) {
             GoToStartCommand.getInstance().execute( null );
         } else if ( src == AddToDownloadQueueCommand.getInstance().getCommand() ) {
             Content content = ((LibraryElement)mMediaList.getSelectedItem()).getDetails();
             AddToDownloadQueueCommand.getInstance().execute( content );
             new DownloadQueueScreen( (LibraryElement)mMediaList.getSelectedItem() ).show();
         } else if ( src == PlayMediaCommand.getInstance().getCommand() ) {
             LibraryElement content = (LibraryElement)mMediaList.getSelectedItem();
             if ( content.isNew() ) {
                 content.setNew( false );
                 mNewModel.removeFromUpdated( content.getId() );
             }
             PlayMediaCommand.getInstance().execute( content.getDetails() );
         } else if ( src == DownloadAllMediaItemsScreenCommand.getInstance().
                 getCommand() ) {
             DownloadAllMediaItemsScreenCommand.getInstance().execute( mMediaList );
         } else if ( src == SearchDialogCommand.getInstance().getCommand() ) {
             LibraryElement content = ((LibraryElement)mMediaList.getSelectedItem());
             SearchDialogCommand.getInstance().execute( content.getParent().getId() );
         } else if ( src instanceof List ) {
            MediaItemContextMenu menu = null;
             if ( mMediaList.getSelectedItem() != null ) {
                 if ( ((LibraryElement)mMediaList.getSelectedItem()).getDetails().
                         isDownloaded() ) {
                     menu = new MediaItemContextMenu( mMediaList, MediaItemContextMenu.PLAY );
                     menu.setModel( mNewModel );
                 } else {
                     menu = new MediaItemContextMenu( mMediaList, MediaItemContextMenu.DOWNLOAD );
                 }
                 menu.show();
             }
         } else if ( src == ShowLinksCommand.getInstance().getCommand() ) {
             Content content = ((LibraryElement)mMediaList.getSelectedItem()).getDetails();
             ShowLinksCommand.getInstance().execute( content );
         } else if ( src == showFreeMem ) {
             GeneralAlert.show( String.valueOf( Runtime.getRuntime().freeMemory() ), GeneralAlert.INFO );
         } else if ( src == HelpCommand.getInstance().getCommand() ) {
             Object[] params = {this.getClass(), mNewLibModel.getId()};
             HelpCommand.getInstance().execute( params );
         } else if ( src == DownloadsQueueViewCommand.getInstance().getCommand() ) {
             DownloadsQueueViewCommand.getInstance().execute( mNewLibModel );
         }
     }
 
     public void selectionChanged( int oldSel, int newSel ) {
         LibraryElement libElem = (LibraryElement)mMediaList.getModel().getItemAt( newSel );
         removeAllCommands();
         addCommand( BackMediaItemsCommand.getInstance().getCommand() );
         addCommand( DownloadsQueueViewCommand.getInstance().getCommand() );
         addCommand( HelpCommand.getInstance().getCommand() );
 
         if ( libElem != null ) {
             Content content = libElem.getDetails();
             addCommand( DownloadAllMediaItemsScreenCommand.getInstance().
                     getCommand() );
             addCommand( SearchDialogCommand.getInstance().getCommand() );
             if ( content != null && content.isDownloaded() ) {
                 addCommand( DeleteContentCommand.getInstance().getCommand() );
                 addCommand( ShowLinksCommand.getInstance().getCommand() );
                 addCommand( ShowDetailsCommand.getInstance().getCommand() );
                 addCommand( PlayMediaCommand.getInstance().getCommand() );
             } else {
                 addCommand( DeleteContentCommand.getInstance().getCommand() );
                 addCommand( AddToDownloadQueueCommand.getInstance().getCommand() );
                 addCommand( InstantDownloadCommand.getInstance().getCommand() );
                 addCommand( ShowLinksCommand.getInstance().getCommand() );
                 addCommand( ShowDetailsCommand.getInstance().getCommand() );
             }
         }
         addCommand( GoToStartCommand.getInstance().getCommand() );
     }
 }
