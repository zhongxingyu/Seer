 package net.maltera.daranable.edinet.model;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 
 public class Term 
 extends TermReference {
 	private String name;
 	private Date start, finish;
 	private boolean name_dirty, start_dirty, finish_dirty, in_database;
 	private Repository repo;
 	
 	private Term( int year, int serial ) {
 		super( year, serial );
 	}
 	
 	public static Term load( Repository repo, ResultSet result ) 
 	throws SQLException {
 		Term inst = new Term( result.getInt( "year" ), 
 				result.getInt( "serial" ) );
 		
 		inst.name = result.getString( "name" );
 		
 		java.sql.Date temp_start, temp_finish;
 		temp_start = result.getDate( "start" );
 		temp_finish = result.getDate( "finish" );
 		
 		inst.start = ( null != temp_start ? 
 				new Date( temp_start.getTime() ) : null );
 		inst.finish = ( null != temp_finish ? 
 				new Date( temp_finish.getTime() ) : null );
 		
 		inst.repo = repo;
 		
 		inst.in_database = true;
 		
 		return inst;
 	}
 	
 	public static Term create( Repository repo ) {
 		final Term inst = new Term( -1, -1 );
 		
 		inst.repo = repo;
 		inst.in_database = false;
 		
 		return inst;
 	}
 	
 	public void setYear( int year ) {
 		if ( in_database ) {
 			 throw new IllegalStateException( 
 					 "year may not be set for a term " +
 					 "which exists in the database" );
 		}
 		
 		this.year = year;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void setName( String name ) {
 		if ( this.name == name ) return;
 		
 		this.name = name;
 		this.name_dirty = true;
 	}
 	
 	public Date getStartDate() {
 		return (Date) start.clone();
 	}
 	
 	public void setStartDate( Date start ) {
 		if ( null == this.start ? null == start : this.start.equals( start ) )
 			return;
 		
 		this.start = (Date) start.clone();
 		this.start_dirty = true;
 	}
 	
 	public Date getFinishDate() {
 		return (Date) finish.clone();
 	}
 	
 	public void setFinishDate( Date end ) {
 		if ( null == this.finish ? null == finish : 
 				this.finish.equals( finish ) ) return;
 		
 		this.finish = (Date) end.clone();
 		this.finish_dirty = true;
 	}
 	
 	private int createSerial( int year ) 
 	throws SQLException {
 		Connection connection = repo.getDatabase().getConnection();
 		PreparedStatement stmt = connection.prepareStatement(
 						"SELECT MAX( term.serial ) \n" +
 						"FROM terms AS term \n" +
 						"WHERE term.year = ?" );
 		
 		stmt.setInt( 1, year );
 		
 		ResultSet result = stmt.executeQuery();
 		
 		if ( !result.next() ) return 0;
 		
 		return result.getInt( 1 ) + 1;		
 	}
 	
 	private PreparedStatement stmtInsert;
 	
 	private void insert() 
 	throws SQLException {
 		if (null == name)
 			throw new IllegalStateException(
 					"name must be set for new records" );
 		
 		final Connection connection = repo.getDatabase().getConnection();
 		
 		this.serial = this.createSerial( this.year );
 		
 		if ( null == this.stmtInsert ) {
 			this.stmtInsert = connection.prepareStatement( 
 					"INSERT INTO terms ( \n" +
 					"    year, serial, name, start, finish \n" +
 					") VALUES ( ?, ?, ?, ?, ? )" );
 		}
 		
 		stmtInsert.setInt( 1, year );
 		stmtInsert.setInt( 2, serial );
 		stmtInsert.setString( 3, name );
 		stmtInsert.setDate( 4, ( start == null ?
 				null : new java.sql.Date( start.getTime() ) ) );
 		stmtInsert.setDate( 5, ( finish == null ?
 				null : new java.sql.Date( finish.getTime() ) ) );
 		
 		stmtInsert.execute();
 		this.in_database = true;
 	}
 	
 	public void commit() 
 	throws SQLException {
 		if (!in_database) {
 			insert();
 			return;
 		}
 		
 		if ( !name_dirty && !start_dirty && !finish_dirty ) return;
 		
 		Connection connection = repo.getDatabase().getConnection();
 		PreparedStatement stmt;
 		
 		stmt = connection.prepareStatement( 
 				"UPDATE terms \n" +
 				"SET name = ?, start = ?, finish = ? \n" +
 				"WHERE year = ? AND serial = ?" );
 		
 		stmt.setString( 1, name );
 		stmt.setDate( 2, ( start == null ?
 				null : new java.sql.Date( start.getTime() ) ) );
 		stmt.setDate( 3, ( finish == null ?
 				null : new java.sql.Date( finish.getTime() ) ) );
 		stmt.setInt( 4, year );
 		stmt.setInt( 5, serial );
 		
 		stmt.execute();
 	}
 }
