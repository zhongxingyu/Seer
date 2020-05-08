 package com.psychobit.hardcore;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  * Database connection class for the hardcore plugin
  * @author psychobit
  *
  */
 public class HardcoreDAO
 {
 	/**
 	 * Mysql connection
 	 */
 	private Connection _mysql;
 	
 	/**
 	 * Connect to the database
 	 * Set the timezone
 	 * Load the player ID cache
 	 */
 	public void connect( String hostname, String database, String username, String password, File dir )
 	{ 
 		try
 		{
 			Class.forName( "com.mysql.jdbc.Driver" ).newInstance();
 			this._mysql = DriverManager.getConnection( "jdbc:mysql://" + hostname + "/" + database, username, password );
 		} catch( Exception e ) { 
 			System.err.println( "Cannot connect to database server: " + e.getMessage() );
 			return;
 		}
 	}
 	
 	/**
 	 * Close the connection to the database
 	 */
 	public void disconnect( File dir )
 	{
 		if ( this._mysql == null ) return; // Ignore unopened connections
 		try {
 			this._mysql.close();
 		} catch( Exception e ) {
 			System.err.println( "Cannot close database connection: " + e.getMessage() );
 		}
 	}
 	
 	
 	/**
 	 * Check to make sure the player has both a status and survival entry for this month
 	 * Update their last online time 
 	 * @param minecraftID
 	 */
 	public boolean checkPlayer( String minecraftID )
 	{
 		// Sanity Check
 		if ( this._mysql == null ) return false;
 		boolean newPlayer = false;
 		
 		// Make sure they are in the players table
 		try {
 			PreparedStatement s = this._mysql.prepareStatement( "SELECT `Username` FROM `player_status` WHERE `Username` = ? LIMIT 1" );
 			s.setString( 1, minecraftID );
 			ResultSet rs = s.executeQuery();
 			if ( !rs.last() )
 			{
 				s = this._mysql.prepareStatement( "INSERT INTO `player_status` ( `Username`, `Status`, `LastUpdated` ) VALUES( ?, 'Alive', NOW() )" );
 				s.setString( 1, minecraftID );
 				s.executeUpdate();
 				newPlayer = true;
 			}
 		} catch( SQLException e ) {
 			e.printStackTrace();
 		}		
 		
 		// Make sure they have a survival entry for this month
 		try {
 			PreparedStatement s = this._mysql.prepareStatement( "SELECT `Username` FROM `survival` WHERE `Username` = ? AND MONTH(`Joined`) = MONTH( NOW() ) LIMIT 1" );
 			s.setString( 1, minecraftID );
 			ResultSet rs = s.executeQuery();
 			if ( !rs.last() )
 			{
 				s = this._mysql.prepareStatement( "INSERT INTO `survival` ( `Username`, `Joined`, `LastOnline`, `SurvivalTime` ) VALUES( ?, NOW(), NOW(), 0 )" );
 				s.setString( 1, minecraftID );
 				s.executeUpdate();
 				newPlayer = true;
 			} else {
 				s = this._mysql.prepareStatement( "UPDATE `survival` SET `LastOnline` = NOW() WHERE `Username` = ? AND MONTH(`Joined`) = MONTH(NOW()) LIMIT 1" );
 				s.setString( 1, minecraftID );
 				s.executeUpdate();
 			}
 		} catch( SQLException e ) {
 			e.printStackTrace();
 		}
 		return newPlayer;
 	}
 	
 	/**
 	 * Update player's survival time
 	 * @param minecraftID
 	 */
 	public void addSurvivalTime( String minecraftID, double time )
 	{
 		if ( this._mysql == null ) return;
 		try {
 			PreparedStatement s = this._mysql.prepareStatement( "UPDATE `survival` SET `SurvivalTime` = ( `SurvivalTime` + TIME_TO_SEC( TIMEDIFF( NOW(), `LastOnline` ) ) - ? ), `LastOnline` = NOW() WHERE `Username` = ? AND MONTH(`Joined`) = MONTH(NOW()) LIMIT 1" );
 			s.setDouble( 1, time );
 			s.setString( 2, minecraftID );
 			s.executeUpdate();
 		} catch( SQLException e ) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Check if the player is dead or not
 	 * @param minecraftID
 	 * @return
 	 */
 	public boolean playerDead( String minecraftID )
 	{
 		if ( this._mysql == null ) return true;
 		try {
 			PreparedStatement s = this._mysql.prepareStatement( "SELECT `Status` FROM `player_status` WHERE `Username` = ? LIMIT 1" );
 			s.setString( 1, minecraftID );
 			ResultSet rs = s.executeQuery();
 			if ( !rs.last() ) return false;
 			if ( rs.getString( 1 ).equals( "Alive" ) ) return false;
 		} catch( SQLException e ) {
 			e.printStackTrace();
 		}
 		return true; // Default case
 	}
 	
 	/**
 	 * Mark a player as dead
 	 * @param minecraftID Player's name
 	 * @param deathMessage How they died
 	 * @param killer Killer's name
 	 * @param weapon Weapon used 
 	 * @param witness witness
 	 * @param x X coordinate of death
 	 * @param y Y coordinate of death
 	 * @param z Z coordinate of death
 	 * @param world World they died in
 	 * @param lastWords Their last words
 	 */
 	public void killPlayer( String minecraftID, String deathMessage, String killer, String weapon, String witness, int x, int y, int z, String world, String lastWords, int d )
 	{
 		if ( this._mysql == null ) return;
 		
 		// Insert death data
 		try {
 			PreparedStatement s = this._mysql.prepareStatement( "INSERT INTO `deaths` ( `Username`, `Date`, `Cause`, `Killer`, `Weapon`, `Witness`, `X`, `Y`, `Z`, `World`, `LastWords`, `SurvivalTime`, `Revive` ) VALUES( ?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'None' )" );
 			s.setString( 1, minecraftID );
 			s.setString( 2, deathMessage );
 			s.setString( 3, killer );
 			s.setString( 4, weapon );
 			s.setString( 5, witness );
 			s.setInt( 6, x );
 			s.setInt( 7, y );
 			s.setInt( 8, z );
 			s.setString( 9, world );
 			s.setString( 10, lastWords );
 			s.setInt( 11, d );
 			s.executeUpdate();
 		} catch( SQLException ex ) {
 			ex.printStackTrace();
 		}
 		
 		// Mark as dead
 		try {
 			PreparedStatement s = this._mysql.prepareStatement( "UPDATE `player_status` SET `Status` = 'Dead' WHERE `Username` = ? LIMIT 1" );
 			s.setString( 1, minecraftID );
 			s.executeUpdate();
 		} catch( SQLException ex ) {
 			ex.printStackTrace();
 		}
 	}
 
 	/**
 	 * Mark a player as alive
 	 * @param minecraftID
 	 */
 	public void revivePlayer( String minecraftID, String adminID )
 	{
 		if ( this._mysql == null ) return;
 		try {
 			PreparedStatement s = this._mysql.prepareStatement( "UPDATE `player_status` SET `Status` = 'Alive' WHERE `Username` = ? LIMIT 1" );
 			s.setString( 1, minecraftID );
 			s.executeUpdate();
 			s = this._mysql.prepareStatement( "UPDATE `deaths` SET `Revive` = 'Admin' WHERE `Username` = ? ORDER BY `Date` DESC LIMIT 1" );
 			s.setString( 1, minecraftID );
 			s.executeUpdate();
 			s = this._mysql.prepareStatement( "INSERT INTO `redemptions` ( `Username`, `Date`, `Source`, `Admin` ) VALUES ( ?, NOW(), 'Admin', ? )" );
 			s.setString( 1, minecraftID );
 			s.setString( 2, adminID );
 			s.execute();
 		} catch( SQLException ex ) {
 			ex.printStackTrace();
 		}
 	}
 
 	/**
 	 * Check if the player has played here this month
 	 * @param minecraftID
 	 * @return
 	 */
 	public boolean playerActive( String minecraftID )
 	{
 		if ( this._mysql == null ) return true;
 		try {
 			PreparedStatement s = this._mysql.prepareStatement( "SELECT COUNT(*) FROM `survival` WHERE `Username` = ? AND MONTH(`Joined`) = MONTH(NOW()) LIMIT 1" );
 			s.setString( 1, minecraftID );
 			ResultSet rs = s.executeQuery();
 			if ( !rs.last() ) return false;
 			if ( rs.getInt( 1 ) == 0 ) return false;
 		} catch( SQLException e ) {
 			e.printStackTrace();
 		}
 		return true; // Default case
 	}
 	
 	
 	/**
 	 * Get the survival time for a player
 	 * @param minecraftID
 	 * @return
 	 */
	public int survivalTime( String minecraftID )
 	{
 		if ( this._mysql == null ) return -1;
 		try {
 			PreparedStatement s = this._mysql.prepareStatement( "SELECT `SurvivalTime`, TIME_TO_SEC( TIMEDIFF( NOW(), `LastOnline` ) ) FROM `survival` WHERE `Username` = ? AND MONTH(`Joined`) = MONTH(NOW()) LIMIT 1" );
 			s.setString( 1, minecraftID );
 			ResultSet rs = s.executeQuery();
 			if ( rs.last() )
 			{
				if ( rs.getInt( 2 ) > 0 ) return rs.getInt( 1 ) + rs.getInt( 2 );
 				return rs.getInt( 1 );
 			}
 		} catch( SQLException e ) {
 			e.printStackTrace();
 		}
 		return -1; // Default case
 	}
 }
