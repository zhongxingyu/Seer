 package com.objectpartners.sbdemo.teamjob;
 
 import com.objectpartners.sbdemo.item.GameItem;
 import com.objectpartners.sbdemo.persistant.Team;
 import org.springframework.batch.item.ItemReader;
 import org.springframework.batch.item.NonTransientResourceException;
 import org.springframework.batch.item.ParseException;
 import org.springframework.batch.item.UnexpectedInputException;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileReader;
 
 /**
  * Implementation of ItemReader to read teams from a CSV file
  */
 public class TeamReader implements ItemReader<Team> {
 
     private BufferedReader reader = null;
 
     public TeamReader(String fileName) {
 
         try {
             FileReader fileReader = new FileReader(fileName);
             reader = new BufferedReader(fileReader);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public Team read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
 
         String line = null;
 
         line = reader.readLine();
 
         System.out.println("Reading in a team line: " + line);
 
         if (null == line) {
             return null;
         }
 
         Team team = new Team();
         String[] tokens = line.split(",");

        if(tokens.length != 2) {
            return null;
        }

         team.setName(tokens[0]);
         team.setNickName(tokens[1]);
 
         return team;
     }
 
 }
