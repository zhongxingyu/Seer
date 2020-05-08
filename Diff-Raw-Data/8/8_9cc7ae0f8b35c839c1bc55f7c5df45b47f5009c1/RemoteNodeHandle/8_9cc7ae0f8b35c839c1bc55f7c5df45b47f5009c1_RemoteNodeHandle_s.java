 package com.geekcommune.communication;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import com.geekcommune.friendlybackup.format.BaseData;
 import com.geekcommune.friendlybackup.proto.Basic;
 
 public class RemoteNodeHandle extends BaseData<Basic.RemoteNodeHandle> {
 
     private static final String CONNECT_STRING_SEP = ":";
 
     private String name;
     private String email;
     private InetAddress address;
     private int port;
 
     public RemoteNodeHandle(String name, String email, String connectString) throws UnknownHostException {
         this.name = name;
         this.email = email;
         String[] cstringPart = connectString.split(CONNECT_STRING_SEP);
         this.address = InetAddress.getByName(cstringPart[0]);
         this.port = Integer.parseInt(cstringPart[1]);
     }
 
     public Basic.RemoteNodeHandle toProto() {
         Basic.RemoteNodeHandle.Builder bldr = Basic.RemoteNodeHandle.newBuilder();
         bldr.setConnectString(getConnectString());
         bldr.setEmail(email);
         bldr.setName(name);
         bldr.setVersion(1);
         
         return bldr.build();
     }
 
    private String getConnectString() {
         return "" + address.getHostAddress() + CONNECT_STRING_SEP + port;
     }
 
     @Override
     public boolean equals(Object obj) {
         if( obj instanceof RemoteNodeHandle ) {
             RemoteNodeHandle rhs = (RemoteNodeHandle) obj;
             return name.equals(rhs.name) && email.equals(rhs.email) && getConnectString().equals(rhs.getConnectString());
         } else {
             return false;
         }
     }
 
     @Override
     public int hashCode() {
         return (name + "~" + email + "~" + getConnectString()).hashCode();
     }
     
     @Override
     public String toString() {
         return "RemoteNodeHandle(" + name + ", " + email + ", " + getConnectString() + ")"; 
     }
     
     public static RemoteNodeHandle fromProto(Basic.RemoteNodeHandle proto) throws UnknownHostException {
         versionCheck(1, proto.getVersion(), proto);
         
         String name = proto.getName();
         String email = proto.getEmail();
         String connectString = proto.getConnectString();
         
         return new RemoteNodeHandle(name, email, connectString);
     }
 
     public InetAddress getAddress() {
         return address;
     }
 
     public int getPort() {
         return port;
     }
 
     public String getName() {
         return name;
     }
 
     public String getEmail() {
         return email;
     }
 }
