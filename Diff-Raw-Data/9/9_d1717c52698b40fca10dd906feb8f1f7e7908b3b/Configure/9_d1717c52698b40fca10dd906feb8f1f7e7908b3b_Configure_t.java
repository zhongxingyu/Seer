 package org.sdu.database;
 
 import java.io.FileReader;
 import java.util.Scanner;
 
 /**
  * Read the configuration file.
  * 
 * @version 0.1 rev 8001 Jan. 6, 2013
  * Copyright (c) HyperCube Dev Team
  */
 class Configure {
 	static String line, databaseAddress, webserverAddress, siteDirectory,
 			database, table, user, password;
 
 	static void read() throws Exception {
 		FileReader in = new FileReader("database.conf");
 		Scanner conf = new Scanner(in);
 		while (conf.hasNextLine()) {
 			String line = conf.nextLine();
 			if (line.equals("[database_address]"))
 				databaseAddress = conf.next();
 			if (line.equals("[webserver_address]"))
 				webserverAddress = conf.next();
			if (line.equals("[site_directory]")) {
 				siteDirectory = conf.next();
				if (!siteDirectory.endsWith("/"))
					siteDirectory += "/";
			}
 			if (line.equals("[database]"))
 				database = conf.next();
 			if (line.equals("[table]"))
 				table = conf.next();
 			if (line.equals("[user]"))
 				user = conf.next();
 			if (line.equals("[password]"))
 				password = conf.next();
 		}
 		conf.close();
 		in.close();
 	}
 }
