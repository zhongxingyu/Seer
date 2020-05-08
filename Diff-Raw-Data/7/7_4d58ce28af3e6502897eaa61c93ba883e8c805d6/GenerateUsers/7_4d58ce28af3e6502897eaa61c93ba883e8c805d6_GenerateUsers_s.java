 package com.datastax.video;
 
 import com.google.common.io.Files;
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  *
  * @author Mark Kerzner
  */
 public class GenerateUsers {
 
     public static void main(String argv[]) {
         if (argv.length != 2) {
             System.out.println("Arguments: number-users, number-emails-per-user");
             System.exit(0);
         }
         GenerateUsers generator = new GenerateUsers();
         int nUsers = Integer.parseInt(argv[0]);
         int nEmails = Integer.parseInt(argv[1]);
 
         try {
             generator.generate(nUsers, nEmails);
         } catch (Exception e) {
             System.out.println("Problem writig files: " + e.getMessage());
             e.printStackTrace(System.out);
         }
     }
 
     private void generate(int nUsers, int nEmails) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
         new File(Util.ROOT_DIR).mkdirs();
         Charset charset = Charset.forName("US-ASCII");
         if (nEmails < 1) nEmails = 1;
         // users
         String userFile = Util.ROOT_DIR + "/" + Util.USERS;
         Util util = new Util();
         new File(userFile).delete();
         for (int u = 1; u <= nUsers; ++u) {
             StringBuilder insert = new StringBuilder();
             insert.append("INSERT INTO users (username, firstname, lastname, email, password, created_date) VALUES (");
             String firstName = util.generateName();
             String lastName = util.generateName();
             String username = firstName + lastName;
             insert.append("'").append(username).append("',").append("'").append(firstName).append("',").append("'").append(lastName).append("',");
             insert.append("[");
             for (int e = 0; e < nEmails; ++e) {
                 insert.append("'").append(util.generateName()).append("@").append(util.generateDomain()).append("'");
                 if (e < nEmails -1) insert.append(",");
             }
             insert.append("],");
             insert.append("'").append(util.generateName()).append("',");
            insert.append("'").append(dateFormat.format(new Date())).append("');");
             Files.append(insert.toString(), new File(userFile), charset);
         }
 
     }
 }
