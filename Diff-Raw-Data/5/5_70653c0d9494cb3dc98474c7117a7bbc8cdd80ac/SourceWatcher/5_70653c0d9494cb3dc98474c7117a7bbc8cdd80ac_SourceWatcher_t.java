 /*
  * Copyright 2012 Frederic SACHOT
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package kesako.watcher.runnable;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Calendar;
 import java.util.Date;
 
 import kesako.utilities.Constant;
 import kesako.utilities.DBUtilities;
 import kesako.utilities.FileUtilities;
 
 
 import org.apache.log4j.Logger;
 
 /**
  * Check the content of a source and update the index<br>
  * The class implements the Log4J logging system.
  * @author Frederic SACHOT
  */
 public class SourceWatcher extends IntervalDBWork{
 	/**
 	 * Log4J logger of the class
 	 */
 	private static final Logger logger = Logger.getLogger(SourceWatcher.class);
 	/**
 	 * id of the source
 	 */
 	private int idSource;
 	/**
 	 * Directory of the source.
 	 */
 	private File directory;
 
 	/**
 	 * Constructor of the watcher. The name of the watcher is: Th_SW< idSource >
 	 * @param idSource id of the source
 	 * @param path path of the source
 	 * @param intervalSeconds number of seconds between to check of the directory.
 	 */
 	public SourceWatcher(int idSource, String path,int intervalSeconds) {
 		super(intervalSeconds,"Th_SW"+Integer.toString(idSource));
 		logger.debug("SourceWatcher");
 		this.idSource=idSource;
 		this.directory=new File(path);
 		if(!this.directory.exists()){
 			logger.fatal("The directory "+path+" doesn't exist");
 		}
 	}
 	/**
 	 * Constructor of the watcher
 	 * @param rs resultset with all data of the source: id, path
 	 * @param intervalSeconds number of seconds between to check of the directory.
 	 * @throws SQLException
 	 */
 	public SourceWatcher(ResultSet rs,int intervalSeconds) throws SQLException{
 		this(rs.getInt("id_source"),rs.getString("chemin"),intervalSeconds);
 	}
 	/**
 	 * Return the id of the source
 	 */
 	public int getIdSource() {
 		return this.idSource;
 	}
 
 	/**
 	 * Return the directory of the file
 	 */
 	public File getDirectory(){
 		return this.directory;
 	}
 
 	public String toString(){
 		return this.idSource+ " : "+this.getThredName()+" : "+this.directory.getAbsolutePath();
 
 	}
 	/**
 	 * Check the content of the directory and add all indexable file to the data-base. 
 	 * If the file fileName.xxx has a meta-file fileName.meta, meta-data is added in the data-base. <br>
 	 * To check subdirectories, the function is a recursive function, and call itself on all subdirectories 
 	 * @param dir directory to check file.
 	 * @throws SQLException 
 	 */
 	private void checkDirectory(Connection cn,File dir) throws SQLException{
 		File file;
 		String query;
 		ResultSet rs;
 		logger.debug("add Directory");
 		query="select flag from t_sources where id_source="+idSource;
 		rs=DBUtilities.executeQuery(cn,query);
 		if(rs.next() && rs.getInt("flag")==Constant.TO_INDEX){
 			if(dir.isDirectory()){
 				File[] children = dir.listFiles();
 				for (int i = 0; i < children.length; i++) {
 					file = children[i];
 					if(file.isFile() && FileUtilities.isValidExtansion(file)){
 						fileProcessing(cn,file);
 					}else{
 						if(file.isDirectory()){
 							checkDirectory(cn,file);
 						}
 					}
 				}
 			}else{
 				if(dir.isFile()&& FileUtilities.isValidExtansion(dir)){
 					fileProcessing(cn,dir);
 				}
 			}
 		}
 	}
 	/**
 	 * If a new file is discovered, the file is added to the data-base.
 	 * If the file or the meta-file, has been modified, the data-base is updated to allow its reindexing. 
 	 * @param file file to add to the data-base.
 	 * @throws SQLException 
 	 */
 	private void fileProcessing(Connection cn,File file){
 		/*
 		 * nouveau 0
 		 * meta 1
 		 * modifié 2
 		 * error 3
 		 */
 		Date dM;
 		File fileMeta;
 		String query="";
 		ResultSet rs;
 		int fileId;
 		Calendar c=Calendar.getInstance();
 		logger.debug("******************************************************************");
 		logger.debug("File Processing "+file.getAbsolutePath());
 		logger.debug("******************************************************************");
 		try {
 			query="start transaction";
 			DBUtilities.executeQuery(cn,query);
 			fileId=DBUtilities.getFileId(cn,file.getAbsolutePath());
 			dM=new Date(file.lastModified());
 			c.setTime(dM);
 			if(fileId<0){ //it's a new file to add 
 				logger.debug("putFile : "+file.getAbsolutePath());
 				query="insert into t_fichiers (ID_SOURCE,NOM,TITRE_F,TITRE_Doc,DATEMODIFIED,DATEEXTRACTED," +
 						"CHEMIN,FLAG,flag_meta,date_meta_modified,author_f,priority) values ("+
 						this.idSource+",'"+
 						DBUtilities.getStringSQL(file.getName())+"','"+
 						DBUtilities.getStringSQL(file.getName())+"','',"+
 						file.lastModified()+",'"+
						c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH)+"','"+
 						DBUtilities.getStringSQL(file.getAbsolutePath())+"',"+
 						Constant.TO_INDEX+","+
 						Constant.TO_EXTRACT_META+",0,'',"+Constant.PRIORITY_NEW_FILE+")";
 				DBUtilities.executeQuery(cn,query);
 			}else{//the file is already added
 				query="select flag,DATEMODIFIED,date_meta_modified from t_fichiers where id_fichier="+fileId;
 				rs=DBUtilities.executeQuery(cn,query);
 				fileMeta=new File(FileUtilities.getFileMetaName(file.getAbsolutePath()));
 				if(rs.next()){
 					if(rs.getLong("DATEMODIFIED")!=file.lastModified()){//the file has been modified
 						query="update t_fichiers set "+
 								"ID_SOURCE="+ this.idSource+","+
 								"TITRE_F='" + DBUtilities.getStringSQL(file.getName())+"',"+
 								"TITRE_Doc='',"+
 								"author_f='',"+
 								"DATEMODIFIED="+file.lastModified()+","+
								"DATEEXTRACTED='"+c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH)+"',"+
 								"FLAG="+Constant.TO_INDEX+","+
 								"flag_meta="+Constant.TO_EXTRACT_META+","+
 								"priority="+Constant.PRIORITY_MODIFIED_FILE+
 								" where id_fichier="+fileId;		
 						DBUtilities.executeQuery(cn,query);
 					}else{
 						if(rs.getInt("flag")==Constant.INDEXED && fileMeta.exists()&&fileMeta.isFile()&&rs.getLong("DATE_META_MODIFIED")!=fileMeta.lastModified()){
 							//the meta-file has been modified
 							query="update t_fichiers set "+
 									"ID_SOURCE="+ this.idSource+","+
 									"flag_meta="+Constant.TO_EXTRACT_META+","+
 									"priority="+Constant.PRIORITY_MODIFIED_FILE+
 									" where id_fichier="+fileId;		
 							DBUtilities.executeQuery(cn,query);
 						}
 					}
 				}else{
 					logger.fatal("No file was found : " + query);
 				}
 			}
 			cn.commit();
 		} catch (SQLException e) {
 			logger.fatal("ERROR : "+query,e);
 			try {
 				cn.rollback();
 			} catch (SQLException e1) {
 				logger.fatal("ERROR Rollback : "+query,e1);
 			}
 		}
 		logger.debug("******************************************************************");
 		logger.debug("END FILE PROCESSING");
 		logger.debug("******************************************************************");
 	}
 	/**
 	 * Check the directory for any changes.
 	 */
 	protected void doWork() {
 		File f;
 		String query="";
 		ResultSet rs;
 		logger.debug("SourceWatcher dowork");
 		Connection cn=getConnection();
 		if(this.directory.exists()){
 			try {
 				query="select flag from t_sources where id_source="+idSource;
 				rs=DBUtilities.executeQuery(cn,query);
 				if(rs.next()){
 					if(rs.getInt("flag")==Constant.TO_INDEX){
 						checkDirectory(cn,this.directory);
 
 						//Now we need to iterate through the list of files and see if any that existed before don't exist anymore
 						query="select id_fichier,chemin from t_fichiers where id_source="+this.idSource;
 						rs=DBUtilities.executeQuery(cn,query);
 						while(rs.next()){
 							f=new File(rs.getString("chemin"));
 							if(!f.exists()){
 								query="update t_fichiers set flag="+Constant.TO_SUPPRESSED+" where id_fichier="+rs.getInt("id_fichier");
 								DBUtilities.executeQuery(cn,query);
 							}
 						}
 					}else{
 						stopWorking();
 					}
 				}else{
 					stopWorking();
 				}
 			} catch (SQLException e) {
 				logger.error(query,e);
 			}
 		}else{
 			stopWorking();
 		}
 		try {
 			cn.close();
 		} catch (SQLException e) {
 			logger.fatal("ERROR closing connection",e);
 		}
 	}
 }
