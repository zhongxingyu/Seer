 /* Copyright 2010 The Tor Project
  * See LICENSE for licensing information */
 package org.torproject.ernie.db;
 
 import java.io.*;
 import java.text.*;
 import java.util.*;
 import java.util.logging.*;
 
 /**
  * Determines estimates of bridge users per country and day from the
  * extra-info descriptors that bridges publish. In a first step, the
  * number of unique IP addresses that bridges see are normalized to a
  * 24-hour period. In the next step, all bridges are excluded that have
  * been running as a relay. Finally, observations are summed up and
  * written to <code>stats/bridge-stats</code>.
  */
 public class BridgeStatsFileHandler {
 
   /**
    * Two-letter country codes of countries that we care about.
    */
   private SortedSet<String> countries;
 
   /**
    * Intermediate results file containing bridge user numbers by country
    * as seen by single bridges, normalized to 24-hour periods.
    */
   private File bridgeStatsRawFile;
 
   /**
    * Bridge user numbers by country as seen by single bridges on a given
    * day. Map keys are bridge and date written as "bridge,date", map
    * values are lines as read from <code>stats/bridge-stats-raw</code>.
    */
   private SortedMap<String, String> bridgeUsersRaw;
 
   /**
    * Helper file containing the hashed relay identities of all known
    * relays. These hashes are compared to the bridge identity hashes to
    * exclude bridges that have been known as relays from the statistics.
    */
   private File hashedRelayIdentitiesFile;
 
   /**
    * Known hashed relay identities used to exclude bridges that have been
    * running as relays.
    */
   private SortedSet<String> hashedRelays;
 
   /**
    * Helper file containing extra-info descriptors published by 0.2.2.x
    * bridges. If these descriptors contain geoip-stats, they are not
    * included in the results, because stats are very likely broken.
    */
   private File zeroTwoTwoDescriptorsFile;
 
   /**
    * Extra-info descriptors published by 0.2.2.x bridges. If these
    * descriptors contain geoip-stats, they are not included in the
    * results, because stats are very likely broken.
    */
   private SortedSet<String> zeroTwoTwoDescriptors;
 
   /**
    * Final results file containing the number of bridge users per country
    * and day. This file is not read in during initialization, but
    * overwritten at the end of the execution.
    */
   private File bridgeStatsFile;
 
   /**
    * Logger for this class.
    */
   private Logger logger;
 
   /**
    * Initializes this class, including reading in intermediate results
    * files <code>stats/bridge-stats-raw</code> and
    * <code>stats/hashed-relay-identities</code>.
    */
   public BridgeStatsFileHandler(SortedSet<String> countries) {
 
     /* Memorize the set of countries we care about. */
     this.countries = countries;
 
     /* Initialize local data structures to hold results. */
     this.bridgeUsersRaw = new TreeMap<String, String>();
     this.hashedRelays = new TreeSet<String>();
     this.zeroTwoTwoDescriptors = new TreeSet<String>();
 
     /* Initialize file names for intermediate and final results. */
     this.bridgeStatsRawFile = new File("stats/bridge-stats-raw");
     this.bridgeStatsFile = new File("stats/bridge-stats");
     this.hashedRelayIdentitiesFile = new File(
         "stats/hashed-relay-identities");
     this.zeroTwoTwoDescriptorsFile = new File(
         "stats/v022-bridge-descriptors");
 
     /* Initialize logger. */
     this.logger = Logger.getLogger(
         BridgeStatsFileHandler.class.getName());
 
     /* Read in bridge user numbers by country as seen by single bridges,
      * normalized to 24-hour periods. */
     if (this.bridgeStatsRawFile.exists()) {
       try {
         this.logger.fine("Reading file "
             + this.bridgeStatsRawFile.getAbsolutePath() + "...");
         BufferedReader br = new BufferedReader(new FileReader(
             this.bridgeStatsRawFile));
         String line = br.readLine();
         if (line != null) {
           /* The first line should contain headers that we need to parse
            * in order to learn what countries we were interested in when
            * writing this file. */
           if (!line.startsWith("bridge,date,time,")) {
             this.logger.warning("Incorrect first line '" + line + "' in "
                 + this.bridgeStatsRawFile.getAbsolutePath() + "! This line "
                 + "should contain headers! Aborting to read in this "
                 + "file!");
           } else {
             String[] headers = line.split(",");
             for (int i = 3; i < headers.length; i++) {
               if (headers[i].equals("all")) {
                 this.countries.add("zy");
               } else {
                 this.countries.add(headers[i]);
               }
             }
             /* Read in the rest of the file. */
             while ((line = br.readLine()) != null) {
               String[] parts = line.split(",");
               if (parts.length != headers.length) {
                 this.logger.warning("Corrupt line '" + line + "' in file "
                     + this.bridgeStatsRawFile.getAbsolutePath()
                     + "! Aborting to read this file!");
                 break;
               }
               String hashedBridgeIdentity = parts[0];
               String date = parts[1];
               String time = parts[2];
               SortedMap<String, String> obs =
                   new TreeMap<String, String>();
               for (int i = 3; i < parts.length; i++) {
                if (headers[i].equals("all")) {
                  obs.put("zy", parts[i]);
                } else {
                  obs.put(headers[i], parts[i]);
                }
               }
               this.addObs(hashedBridgeIdentity, date, time, obs);
             }
           }
         }
         br.close();
         this.logger.fine("Finished reading file "
             + this.bridgeStatsRawFile.getAbsolutePath() + ".");
       } catch (IOException e) {
         this.logger.log(Level.WARNING, "Failed to read file "
             + this.bridgeStatsRawFile.getAbsolutePath() + "!", e);
       }
     }
 
     /* Read in known hashed relay identities used to exclude bridges that
      * have been running as relays. */
     if (this.hashedRelayIdentitiesFile.exists()) {
       try {
         this.logger.fine("Reading file "
             + this.hashedRelayIdentitiesFile.getAbsolutePath() + "...");
         BufferedReader br = new BufferedReader(new FileReader(
             this.hashedRelayIdentitiesFile));
         String line = null;
         /* Read in all lines from the file and memorize them. */
         while ((line = br.readLine()) != null) {
           this.hashedRelays.add(line);
         }
         br.close();
         this.logger.fine("Finished reading file "
             + this.hashedRelayIdentitiesFile.getAbsolutePath() + ".");
       } catch (IOException e) {
         this.logger.log(Level.WARNING, "Failed to read file "
             + this.hashedRelayIdentitiesFile.getAbsolutePath() + "!", e);
       }
     }
 
     /* Read in known extra-info descriptors published by 0.2.2.x
      * bridges. */
     if (this.zeroTwoTwoDescriptorsFile.exists()) {
       try {
         this.logger.fine("Reading file "
             + this.zeroTwoTwoDescriptorsFile.getAbsolutePath() + "...");
         BufferedReader br = new BufferedReader(new FileReader(
             this.zeroTwoTwoDescriptorsFile));
         String line = null;
         /* Read in all lines from the file and memorize them. */
         while ((line = br.readLine()) != null) {
           this.zeroTwoTwoDescriptors.add(line);
         }
         br.close();
         this.logger.fine("Finished reading file "
             + this.zeroTwoTwoDescriptorsFile.getAbsolutePath() + ".");
       } catch (IOException e) {
         this.logger.log(Level.WARNING, "Failed to read file "
             + this.zeroTwoTwoDescriptorsFile.getAbsolutePath() + "!", e);
       }
     }
   }
 
   /**
    * Adds a hashed relay identity string to the list of bridges that we
    * are going to ignore in the future. If we counted user numbers from
    * bridges that have been running as relays, our numbers would be far
    * higher than what we think is correct.
    */
   public void addHashedRelay(String hashedRelayIdentity) {
     if (!this.hashedRelays.contains(hashedRelayIdentity)) {
       this.logger.finer("Adding new hashed relay identity: "
           + hashedRelayIdentity);
       this.hashedRelays.add(hashedRelayIdentity);
     }
   }
 
   /**
    * Adds an extra-info descriptor identifier published by an 0.2.2.x
    * bridges. If this extra-info descriptor contains geoip-stats, they are
    * not included in the results, because stats are very likely broken.
    */
   public void addZeroTwoTwoDescriptor(String hashedBridgeIdentity,
       String date, String time) {
     String value = hashedBridgeIdentity.toUpperCase() + "," + date + ","
         + time;
     if (!this.zeroTwoTwoDescriptors.contains(value)) {
       this.logger.finer("Adding new bridge 0.2.2.x extra-info "
           + "descriptor: " + value);
       this.zeroTwoTwoDescriptors.add(value);
     }
   }
 
   /**
    * Returns whether the given fingerprint is a known hashed relay
    * identity. <code>BridgeDescriptorParser</code> uses this information
    * to decide whether to continue parsing a bridge extra-descriptor
    * descriptor or not.
    */
   public boolean isKnownRelay(String hashedBridgeIdentity) {
     return this.hashedRelays.contains(hashedBridgeIdentity);
   }
 
   /**
    * Adds bridge user numbers by country as seen by a single bridge on a
    * given date and time. Bridges can publish statistics on unique IP
    * addresses multiple times a day, but we only want to include one
    * observation per day. If we already have an observation from the given
    * bridge and day, we keep the one with the later publication time and
    * discard the other one.
    */
   public void addObs(String hashedIdentity, String date, String time,
       Map<String, String> obs) {
     String key = hashedIdentity + "," + date;
     StringBuilder sb = new StringBuilder(key + "," + time);
     for (String c : this.countries) {
       sb.append("," + (obs.containsKey(c) && !obs.get(c).startsWith("-")
           ? obs.get(c) : "0.0"));
     }
     String value = sb.toString();
     if (!this.bridgeUsersRaw.containsKey(key)) {
       this.logger.finer("Adding new bridge user numbers: " + value);
       this.bridgeUsersRaw.put(key, value);
     } else if (value.compareTo(this.bridgeUsersRaw.get(key)) > 0) {
       this.logger.finer("Replacing existing bridge user numbers (" +
           this.bridgeUsersRaw.get(key) + " with new numbers: "
           + value);
       this.bridgeUsersRaw.put(key, value);
     } else {
       this.logger.finer("Not replacing existing bridge user numbers (" +
           this.bridgeUsersRaw.get(key) + " with new numbers (" + value
           + ").");
     }
   }
 
   /**
    * Writes the list of hashed relay identities and bridge user numbers as
    * observed by single bridges to disk, aggregates per-day statistics for
    * all bridges, and writes those to disk, too.
    */
   public void writeFiles() {
 
     /* Write hashed relay identities to disk. */
     try {
       this.logger.fine("Writing file "
           + this.hashedRelayIdentitiesFile.getAbsolutePath() + "...");
       this.hashedRelayIdentitiesFile.getParentFile().mkdirs();
       BufferedWriter bw = new BufferedWriter(new FileWriter(
           this.hashedRelayIdentitiesFile));
       for (String hashedRelay : this.hashedRelays) {
         bw.append(hashedRelay + "\n");
       }
       bw.close();
       this.logger.fine("Finished writing file "
           + this.hashedRelayIdentitiesFile.getAbsolutePath() + ".");
     } catch (IOException e) {
       this.logger.log(Level.WARNING, "Failed to write "
           + this.hashedRelayIdentitiesFile.getAbsolutePath() + "!", e);
     }
 
     /* Write bridge extra-info descriptor identifiers to disk. */
     try {
       this.logger.fine("Writing file "
           + this.zeroTwoTwoDescriptorsFile.getAbsolutePath() + "...");
       this.zeroTwoTwoDescriptorsFile.getParentFile().mkdirs();
       BufferedWriter bw = new BufferedWriter(new FileWriter(
           this.zeroTwoTwoDescriptorsFile));
       for (String descriptorIdentifier : this.zeroTwoTwoDescriptors) {
         bw.append(descriptorIdentifier + "\n");
       }
       bw.close();
       this.logger.fine("Finished writing file "
           + this.zeroTwoTwoDescriptorsFile.getAbsolutePath() + ".");
     } catch (IOException e) {
       this.logger.log(Level.WARNING, "Failed to write "
           + this.zeroTwoTwoDescriptorsFile.getAbsolutePath() + "!", e);
     }
 
     /* Write observations made by single bridges to disk. */
     try {
       this.logger.fine("Writing file "
           + this.bridgeStatsRawFile.getAbsolutePath() + "...");
       this.bridgeStatsRawFile.getParentFile().mkdirs();
       BufferedWriter bw = new BufferedWriter(new FileWriter(
           this.bridgeStatsRawFile));
       bw.append("bridge,date,time");
       for (String c : this.countries) {
         if (c.equals("zy")) {
           bw.append(",all");
         } else {
           bw.append("," + c);
         }
       }
       bw.append("\n");
       for (String line : this.bridgeUsersRaw.values()) {
         String[] parts = line.split(",");
         String hashedBridgeIdentity = parts[0];
         String date = parts[1];
         String time = parts[2];
         if (!this.hashedRelays.contains(hashedBridgeIdentity) &&
             !this.zeroTwoTwoDescriptors.contains(hashedBridgeIdentity
             + "," + date + "," + time)) {
           bw.append(line + "\n");
         }
       }
       bw.close();
       this.logger.fine("Finished writing file "
           + this.bridgeStatsRawFile.getAbsolutePath() + ".");
     } catch (IOException e) {
       this.logger.log(Level.WARNING, "Failed to write "
           + this.bridgeStatsRawFile.getAbsolutePath() + "!", e);
     }
 
     /* Aggregate per-day statistics. */
     SortedMap<String, double[]> bridgeUsersPerDay =
         new TreeMap<String, double[]>();
     for (String line : this.bridgeUsersRaw.values()) {
       String[] parts = line.split(",");
       String hashedBridgeIdentity = parts[0];
       String date = parts[1];
       String time = parts[2];
       if (!this.hashedRelays.contains(hashedBridgeIdentity) &&
           !this.zeroTwoTwoDescriptors.contains(hashedBridgeIdentity + ","
           + date + "," + time)) {
         double[] users = bridgeUsersPerDay.get(date);
         if (users == null) {
           users = new double[this.countries.size()];
           bridgeUsersPerDay.put(date, users);
         }
         for (int i = 3; i < parts.length; i++) {
           users[i - 3] += Double.parseDouble(parts[i]);
         }
       }
     }
 
     /* Write final results of bridge users per day and country to
      * <code>stats/bridge-stats</code>. */
     try {
       this.logger.fine("Writing file "
           + this.bridgeStatsRawFile.getAbsolutePath() + "...");
       this.bridgeStatsFile.getParentFile().mkdirs();
       BufferedWriter bw = new BufferedWriter(new FileWriter(
           this.bridgeStatsFile));
       bw.append("date");
       for (String c : this.countries) {
         if (c.equals("zy")) {
           bw.append(",all");
         } else {
           bw.append("," + c);
         }
       }
       bw.append("\n");
 
       /* Memorize last written date fill missing dates with NA's. */
       long lastDateMillis = 0L;
       SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
       dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
       for (Map.Entry<String, double[]> e : bridgeUsersPerDay.entrySet()) {
         String date = e.getKey();
         long currentDateMillis = dateFormat.parse(date).getTime();
         if (lastDateMillis == 0L) {
           lastDateMillis = currentDateMillis;
         }
         while (currentDateMillis - 24L * 60L * 60L * 1000L
             > lastDateMillis) {
           lastDateMillis += 24L * 60L * 60L * 1000L;
           bw.append(dateFormat.format(new Date(lastDateMillis)));
           for (int i = 0; i < this.countries.size(); i++) {
             bw.append(",NA");
           }
           bw.append("\n");
         }
         lastDateMillis = currentDateMillis;
 
         /* Write current observation. */
         bw.append(date);
         double[] users = e.getValue();
         for (int i = 0; i < users.length; i++) {
           bw.append("," + String.format("%.2f", users[i]));
         }
         bw.append("\n");
       }
       bw.close();
       this.logger.fine("Finished writing file "
           + this.bridgeStatsFile.getAbsolutePath() + ".");
     } catch (IOException e) {
       this.logger.log(Level.WARNING, "Failed to write "
           + this.bridgeStatsFile.getAbsolutePath() + "!", e);
     } catch (ParseException e) {
       this.logger.log(Level.WARNING, "Failed to write "
           + this.bridgeStatsFile.getAbsolutePath() + "!", e);
     }
   }
 }
 
