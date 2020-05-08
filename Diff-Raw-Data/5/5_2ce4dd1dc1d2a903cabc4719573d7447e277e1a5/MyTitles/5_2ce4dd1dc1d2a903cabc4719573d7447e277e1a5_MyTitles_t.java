 // Copyright (C) 2011 by Danny de Jong
 //
 // Permission is hereby granted, free of charge, to any person obtaining a copy
 // of this software and associated documentation files (the "Software"), to deal
 // in the Software without restriction, including without limitation the rights
 // to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 // copies of the Software, and to permit persons to whom the Software is
 // furnished to do so, subject to the following conditions:
 // 
 // The above copyright notice and this permission notice shall be included in
 // all copies or substantial portions of the Software.
 // 
 // THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 // IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 // FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 // AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 // LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 // OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 // THE SOFTWARE.
 
 package com.dannycrafts.myTitles;
 
 import java.io.IOException;
 
 import com.dannycrafts.myTitles.database.*;
 
 public class MyTitles {
     
 	private Plugin plugin;
 	private String usagePluginId;
 	
 	protected MyTitles( Plugin plugin, String usagePluginId )
 	{
 		this.plugin = plugin;
 		this.usagePluginId = usagePluginId;
 	}
 	
 	public Player getPlayer( String playerName ) throws IOException
 	{
 		long playerIndex = plugin.playerDatabase.findRow( (short)0, playerName );
 		if ( playerIndex == -1 )
 			return null;
 		
 		return new Player( plugin, playerIndex );
 	}
 	
 	public Player getPlayer( org.bukkit.entity.Player player ) throws IOException
 	{
 		return getPlayer( player.getName() );
 	}
 	
 	public Title getTitle( String titleName ) throws IOException
 	{		
		long titleId = plugin.titleDatabase.findRow( new SearchCriteria( (short)0, usagePluginId ), new SearchCriteria( (short)1, titleName ) );
 		if ( titleId == -1 )
 			return null;

 		return new Title( titleId );
 	}
 	
 	public Title[] getTitles() throws IOException
 	{
 		long[] rows = plugin.titleDatabase.findRows( new SearchCriteria( (short)0, usagePluginId ) );
 		
 		Title[] titles = new Title[rows.length];
 		
 		for ( int i = 0; i < rows.length; i++ )
 			titles[i] = new Title( rows[i] );
 		
 		return titles;
 	}
 	
 	private boolean isValidName( String name )
 	{
 		for ( int i = 0; i < name.length(); i++ )
 		{
 			int codePoint = name.codePointAt(i);
 			
 			if (	codePoint != 45 &&
 					codePoint < 48 && codePoint > 57 &&
 					codePoint < 65 && codePoint > 90 &&
 					codePoint != 95 &&
 					codePoint < 97 && codePoint > 122 )
 				return false;
 		}
 		
 		return true;
 	}
 	
 	public void putTitles( Title.Info[] titles ) throws Exception
 	{
 		Title[] registeredTitles = getTitles();
 		
 		for ( int i = 0; i < registeredTitles.length; i++ )
 		{
 			Title registeredTitle = registeredTitles[i];
 			Title.Info registeredTitleInfo = registeredTitle.getInfo();
 			Title.Info titleInfo = null;
 			
 			boolean titleUsed = false;
 			for ( int j = 0; j < titles.length; j++ )
 			{
 				titleInfo = titles[j];
 				
 				if ( titleInfo.name.equalsIgnoreCase( registeredTitleInfo.name ) )
 				{
 					titleUsed = true;
 					break;
 				}
 			}
 			
 			if ( titleUsed == false )
 			{
 				unregisterTitle( registeredTitle );
 				registeredTitles[i] = null;
 			}
 			else
 			{
 				if (	( titleInfo.affixes.prefix == null && registeredTitleInfo.affixes.prefix != null ) ||
 						( titleInfo.affixes.prefix != null && !titleInfo.affixes.prefix.equals( registeredTitleInfo.affixes.prefix ) )	)
 					registeredTitle.setPrefix( titleInfo.affixes.prefix );
 				if (	( titleInfo.affixes.suffix == null && registeredTitleInfo.affixes.suffix != null ) ||
 						( titleInfo.affixes.suffix != null && !titleInfo.affixes.suffix.equals( registeredTitleInfo.affixes.suffix ) )	)
 					registeredTitle.setSuffix( titleInfo.affixes.suffix );
 			}
 		}
 		
 		for ( int i = 0; i < titles.length; i++ )
 		{
 			Title.Info titleInfo = titles[i];
 			
 			boolean titleFree = true;
 			for ( int j = 0; j < registeredTitles.length; j++ )
 			{
 				if ( registeredTitles[j] != null && titleInfo.name.equalsIgnoreCase( registeredTitles[j].getName() ) )
 				{
 					titleFree = false;
 					break;
 				}
 			}
 			
 			if ( titleFree == true )
 				registerTitle( titleInfo );
 		}
 	}
 	
 	public boolean registerTitle( Title.Info titleInfo ) throws IOException, Title.InvalidNameException
 	{
 		if ( !isValidName( titleInfo.name ) ) throw new Title.InvalidNameException();
 		
 		return plugin.titleDatabase.addUniqueRow( new SearchCriteria( (short)1, titleInfo.name ),
 				new StringCell( usagePluginId ), new StringCell( titleInfo.name ), new StringCell( titleInfo.affixes.prefix ), new StringCell( titleInfo.affixes.suffix )
 		) != -1;
 	}
 		
 	public boolean registerTitle( String name, String prefix, String suffix ) throws IOException, Title.InvalidNameException
 	{
 		return registerTitle( new Title.Info( name, prefix, suffix ) );
 	}
 	
 	public boolean unregisterTitle( String name ) throws IOException
 	{		
 		return unregisterTitle( getTitle( name ) );
 	}
 	
 	public boolean unregisterTitle( Title title ) throws IOException
 	{
 		long[] players = plugin.collectionDatabase.findRows( (short)1, title.id );
 		for ( int i = 0; i < players.length; i++ )
 		{
 			Player player = new Player( plugin, players[i] );
 				
 			org.bukkit.entity.Player onlinePlayer = plugin.getServer().getPlayer( player.getName() );
 			if ( onlinePlayer != null )
 				onlinePlayer.sendMessage( "You lost title \"" + title.getName() + "\"." );
 			
 			if ( player.takeTitle( title ) == false ) return false;
 		}
 
 		plugin.titleVariationDatabase.removeRows( new SearchCriteria( (short)0, title.id ) );
 		return plugin.titleDatabase.removeRow( title.id );
 	}
 }
