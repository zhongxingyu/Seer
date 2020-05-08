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
 import java.util.Vector;
 import org.ned.client.Content;
 import org.ned.client.command.*;
 import org.ned.client.library.advanced.LibraryElement;
 import org.ned.client.view.customComponents.NedList;
 import org.ned.client.view.renderer.CategoryListCellRenderer;
 
 public class CategoryScreen extends NedFormBase implements ActionListener {
 
     private NedList mCategoryList;
 
     public CategoryScreen( String id ) {
         super( id );
         setNedTitle( this.mNewLibModel != null ? mNewLibModel.getName() : " " );
 
        mCategoryList = new NedList( this.mNewLibModel != null ? mNewLibModel.
                getChildern() : new Vector( 0 ) );
         mCategoryList.setContextMenu( new CategoryContextMenu( mCategoryList, 2 ) );
         mCategoryList.setRenderer( new CategoryListCellRenderer() );
         mCategoryList.setSelectedIndex( 0 );
         mCategoryList.setPreferredW( Display.getInstance().getDisplayWidth() );
         addComponent( mCategoryList );
 
         addCommand( BackCategoryCommand.getInstance().getCommand() );
         addCommand( HelpCommand.getInstance().getCommand() );
         addCommand( DownloadsQueueViewCommand.getInstance().getCommand() );
         addCommand( DeleteContentCommand.getInstance().getCommand() );
         addCommand( DownloadAllCategoryScreenCommand.getInstance().getCommand() );
         addCommand( SearchDialogCommand.getInstance().getCommand() );
 
         mCategoryList.addActionListener( this );
         addGameKeyListener( Display.GAME_LEFT, this );
         addGameKeyListener( Display.GAME_RIGHT, this );
         addCommandListener( this );
     }
 
     public void actionPerformed( ActionEvent evt ) {
         Object src = evt.getSource();
 
         if ( src == BackCategoryCommand.getInstance().getCommand()
                 || evt.getKeyEvent() == Display.GAME_LEFT ) {
             if ( mNewLibModel != null ) {
                BackCategoryCommand.getInstance().execute( mNewLibModel.
                        getParent().getId() );
             } else {
                 BackToMainScreenCommand.getInstance().execute( null );
             }
         } else if ( src instanceof List || evt.getKeyEvent()
                 == Display.GAME_RIGHT ) {
            LibraryElement content = (LibraryElement)mCategoryList.
                    getSelectedItem();
             if ( content != null ) {
                 BrowseCategoryCommand.getInstance().execute( content.getId() );
             }
         } else if ( src == DeleteContentCommand.getInstance().getCommand() ) {
             DeleteContentCommand.getInstance().execute( mCategoryList );
         } else if ( src == DownloadAllCategoryScreenCommand.getInstance().
                 getCommand() ) {
             DownloadAllCategoryScreenCommand.getInstance().execute( mCategoryList );
         } else if ( src == SearchDialogCommand.getInstance().getCommand() ) {
            Content content = ((LibraryElement)mCategoryList.getSelectedItem()).
                    getDetails();
             if ( content != null ) {
                 SearchDialogCommand.getInstance().execute( content.getId() );
             }
         } else if ( src == showFreeMem ) {
             GeneralAlert.show( String.valueOf( Runtime.getRuntime().freeMemory() ), GeneralAlert.INFO );
         } else if ( src == HelpCommand.getInstance().getCommand() ) {
             Object[] params = {this.getClass(), mNewLibModel.getId()};
             HelpCommand.getInstance().execute( params );
         } else if ( src == DownloadsQueueViewCommand.getInstance().getCommand() ) {
             DownloadsQueueViewCommand.getInstance().execute( mNewLibModel );
         }
     }
 }
