 package com.minecraftdimensions.bungeesuite.objects;
 
 public class ServerData {
     String serverName;
     String shortName;
     String forcedChannel;
     boolean forceChannel;
     boolean usingFactionChannels;
     int localDistance;
     boolean connectionMessages;
 
 
     public ServerData( String name, String shortName, boolean force, String channel, boolean facs, int localDistance, boolean connectionMessages ) {
         this.serverName = name;
         this.shortName = shortName;
         this.forceChannel = force;
         this.forcedChannel = channel;
         this.usingFactionChannels = facs;
         this.localDistance = localDistance;
         this.connectionMessages = connectionMessages;
     }
 
     public ServerData( String deserialise ) {
         String data[] = deserialise.split( "~" );
         serverName = data[0];
         shortName = data[1];
         forceChannel = Boolean.parseBoolean( data[2] );
         forcedChannel = data[3];
         usingFactionChannels = Boolean.parseBoolean( data[4] );
         localDistance = Integer.parseInt( data[5] );
         connectionMessages = Boolean.parseBoolean( data[6] );
     }
 
     public String getServerName() {
         return serverName;
     }
 
     public String getServerShortName() {
         return shortName;
     }
 
     public String getForcedChannel() {
         return forcedChannel;
     }
 
     public boolean forcingChannel() {
         return forceChannel;
     }
 
     public boolean usingFactions() {
         return usingFactionChannels;
     }
 
     public String serialise() {
        return serverName + "~" + shortName + "~" + forceChannel + "~" + forcedChannel + "~" + usingFactionChannels + "~" + localDistance + "~" + connectionMessages;
     }
 }
