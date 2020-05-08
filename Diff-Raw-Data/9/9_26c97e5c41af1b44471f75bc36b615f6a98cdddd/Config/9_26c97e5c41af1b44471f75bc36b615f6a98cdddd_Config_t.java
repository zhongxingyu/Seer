 /*
 *
 *	Copyright (c) 2013 Andreas Reder
 *	Author      : Andreas Reder <andreas.reder@lielas.org>
 *	File		: 
 *
 *	This File is part of lielas, see www.lielas.org for more information.
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
 package lielas.core;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 public class Config {
 
	int win = 0;
 	static String appPath;
 
 	String dbUser = "lielas";
 	String dbPass = "lielas";
 	String dbName = "ldb";
 	
 	String langPath = "lang\\";
 	String firstLang = "en.properties";
 	String secondLang = "de.properties";
 	String langDelimiter = "=";
 	String langComment	= "//";
 	
 	String csvDelimiter = ";";
 	String csvFiletypeEnding = ".csv";
 	
 	String sixLowPanPrefix = "";
 	String sixLowPanGatewayIP = "";
 	String sixLowPanUART = "";
 	String sixLowPanBaudrate = "";
 	String sixLowPanTunnel = "";
 	
 	String lbusServerAddress = "";
 	Integer lbusServerPort = 5684;
 	
 	String sqlServerAddress = "localhost";
 	Integer sqlServerPort = 5432;
 	
 	public Config(){
 		if(win == 1){
 			appPath = "E:\\VM Share\\lielas\\lieweb\\";
 		}else{
 			appPath = "/usr/local/lielas/";
 		}
 	}
 	
 	public void LoadSettings(){
 		Properties configFile = new Properties();
 		BufferedInputStream stream;
 		
 		
 		try {
 			stream = new BufferedInputStream(new FileInputStream(appPath + "config.properties"));
 		} catch (Exception e1) {
 			e1.printStackTrace();
 			return;
 		}
 		
 		try {
 			configFile.load(stream);
 			stream.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 
 		
 		langPath = configFile.getProperty("languagePath");
 		firstLang = configFile.getProperty("firstLanguage");
 		secondLang = configFile.getProperty("secondLanguage");
 		langDelimiter = configFile.getProperty("languageDelimiter");
 		langComment = configFile.getProperty("languageComment");
 		
 		csvDelimiter = configFile.getProperty("csvDelimiter");
 		csvFiletypeEnding = configFile.getProperty("csvFiletypeEnding");
 		
 		sixLowPanPrefix = configFile.getProperty("6LowPanPrefix");
 		sixLowPanGatewayIP = configFile.getProperty("6LowPanGwIp");
 		sixLowPanUART = configFile.getProperty("6LowPanUart");
 		sixLowPanBaudrate = configFile.getProperty("6LowPanBaudrate");
 		sixLowPanTunnel = configFile.getProperty("6LowPanTun");
 		
 		lbusServerAddress = configFile.getProperty("lbusServerAddress");
 		lbusServerPort = Integer.parseInt(configFile.getProperty("lbusServerPort"));
 		
 		dbUser = configFile.getProperty("dbUser");
 		dbPass = configFile.getProperty("dbPassword");
 		dbName = configFile.getProperty("dbName");
 		
 		sqlServerAddress = configFile.getProperty("sqlServerAddress");
 		sqlServerPort = Integer.parseInt(configFile.getProperty("sqlServerPort"));
 		
 		
 	}
 	
 	public void SaveSettings(){
 		Properties configFile = new Properties();
 		try{
 			configFile.store(new FileOutputStream("config.properties"), null);
 		} catch ( Exception e){
 			ExceptionHandler.HandleException(e);
 		}
 		
 	}
 
 	public String getDbUser() {
 		return dbUser;
 	}
 
 	public void setDbUser(String dbUser) {
 		this.dbUser = dbUser;
 	}
 
 	public String getDbPass() {
 		return dbPass;
 	}
 
 	public void setDbPass(String dbPass) {
 		this.dbPass = dbPass;
 	}
 
 	public String getDbName() {
 		return dbName;
 	}
 
 	public void setDbName(String dbName) {
 		this.dbName = dbName;
 	}
 	
 	public String getLangPath() {
 		return langPath;
 	}
 
 	public void setLangPath(String langPath) {
 		this.langPath = langPath;
 	}
 
 	public String getFirstLang() {
 		return firstLang;
 	}
 
 	public void setFirstLang(String firstLang) {
 		this.firstLang = firstLang;
 	}
 
 	public String getSecondLang() {
 		return secondLang;
 	}
 
 	public void setSecondLang(String secondLang) {
 		this.secondLang = secondLang;
 	}
 
 	public String getLangDelimiter() {
 		return langDelimiter;
 	}
 
 	public void setLangDelimiter(String langDelimiter) {
 		this.langDelimiter = langDelimiter;
 	}
 
 	public String getLangComment() {
 		return langComment;
 	}
 
 	public void setLangComment(String langComment) {
 		this.langComment = langComment;
 	}
 
 	public String getCsvDelimiter() {
 		return csvDelimiter;
 	}
 
 	public void setCsvDelimiter(String csvDelimiter) {
 		this.csvDelimiter = csvDelimiter;
 	}
 
 	public String getCsvFiletypeEnding() {
 		return csvFiletypeEnding;
 	}
 
 	public void setCsvFiletypeEnding(String csvFiletypeEnding) {
 		this.csvFiletypeEnding = csvFiletypeEnding;
 	}
 
 	public String getSixLowPanPrefix() {
 		return sixLowPanPrefix;
 	}
 
 	public void setSixLowPanPrefix(String sixLowPanPrefix) {
 		this.sixLowPanPrefix = sixLowPanPrefix;
 	}
 
 	public String getSixLowPanGatewayIP() {
 		return sixLowPanGatewayIP;
 	}
 
 	public void setSixLowPanGatewayIP(String sixLowPanGatewayIP) {
 		this.sixLowPanGatewayIP = sixLowPanGatewayIP;
 	}
 
 	public String getSixLowPanUART() {
 		return sixLowPanUART;
 	}
 
 	public void setSixLowPanUART(String sixLowPanUART) {
 		this.sixLowPanUART = sixLowPanUART;
 	}
 
 	public String getLbusServerAddress() {
 		return lbusServerAddress;
 	}
 
 	public void setLbusServerAddress(String lbusServerAddress) {
 		this.lbusServerAddress = lbusServerAddress;
 	}
 
 	public Integer getLbusServerPort() {
 		return lbusServerPort;
 	}
 
 	public void setLbusServerPort(Integer lbusServerPort) {
 		this.lbusServerPort = lbusServerPort;
 	}
 
 	public String getSixLowPanBaudrate() {
 		return sixLowPanBaudrate;
 	}
 
 	public void setSixLowPanBaudrate(String sixLowPanBaudrate) {
 		this.sixLowPanBaudrate = sixLowPanBaudrate;
 	}
 
 	public String getSixLowPanTunnel() {
 		return sixLowPanTunnel;
 	}
 
 	public void setSixLowPanTunnel(String sixLowPanTunnel) {
 		this.sixLowPanTunnel = sixLowPanTunnel;
 	}
 
 	public String getSqlServerAddress() {
 		return sqlServerAddress;
 	}
 
 	public void setSqlServerAddress(String sqlServerAddress) {
 		this.sqlServerAddress = sqlServerAddress;
 	}
 
 	public Integer getSqlServerPort() {
 		return sqlServerPort;
 	}
 
 	public void setSqlServerPort(Integer sqlServerPort) {
 		this.sqlServerPort = sqlServerPort;
 	}
 }
