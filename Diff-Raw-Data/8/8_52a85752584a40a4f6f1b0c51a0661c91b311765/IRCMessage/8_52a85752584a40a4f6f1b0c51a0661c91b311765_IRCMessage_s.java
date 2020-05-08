 package org.openstatic.irc;
 
 import java.util.Vector;
 import java.util.Enumeration;
 import org.json.*;
 
 public class IRCMessage
 {
     protected String cmd;
     protected String source;
     protected Vector<String> args;
     protected Vector<String> destination;
     
     // constructor
     public IRCMessage(String line)
     {
         this.args = new Vector<String>();
         this.destination = null;
         String[] cmdArray = line.split(" ");
         if (cmdArray[0].startsWith(":"))
         {
             this.source = cmdArray[0].substring(1);
             this.cmd = cmdArray[1];
             this.args = doArgs(cmdArray,2);
         } else {
             this.cmd = cmdArray[0];
             this.args = doArgs(cmdArray,1);
         }
     }
     
     public IRCMessage(JSONObject job) throws JSONException
     {
         this.source = job.getString("source");
         this.cmd = job.getString("command");
         this.args = new Vector<String>();
         if (job.has("args"))
         {
             JSONArray arguments = job.getJSONArray("args");
             for (int a = 0; a < arguments.length(); a++)
             {
                 String c_arg = arguments.getString(a);
                 this.args.add(c_arg);
             }
         }
         this.destination = null;
         if (job.has("destination"))
         {
             JSONArray dest = job.getJSONArray("destination");
             this.destination = new Vector<String>();
             for (int b = 0; b < dest.length(); b++)
             {
                 this.destination.add(dest.getString(b));
             }
         }
         
     }
     
     public IRCMessage(String cmd, String source)
     {
         this.cmd = cmd;
         this.args = new Vector<String>();
         this.source = source;
         this.destination = null;
     }
 
     public IRCMessage(String cmd, IrcUser source)
     {
         this.cmd = cmd;
         this.args = new Vector<String>();
         this.source = source.toString();
         this.destination = null;
     }
 
     public IRCMessage(IRCMessage msg, String source)
     {
         this.cmd = msg.getCommand();
         this.args = msg.getArgs();
         this.source = source;
         this.destination = msg.getDestinations();
     }
     
     public IRCMessage(IRCMessage msg, IrcUser source)
     {
         this.cmd = msg.getCommand();
         this.args = msg.getArgs();
         this.source = source.toString();
         this.destination = msg.getDestinations();
     }
     
     public IRCMessage()
     {
         this.cmd = "";
         this.args = new Vector<String>();
        this.source = "";
         this.destination = null;
     }
     
     // add arg for command handler recreation
     public void addArg(String value)
     {
         this.args.add(value);
     }
     
     public void clearArgs()
     {
         this.args = new Vector<String>();
     }
 
     
     // process that funny argument layout
     private Vector<String> doArgs(String[] ary, int start)
     {
         Vector<String> return_vector = new Vector<String>();
         for (int i = start; i < ary.length; i++)
         {
             if (ary[i].startsWith(":"))
             {
                 StringBuffer lastarg = new StringBuffer(ary[i].substring(1));
                 i++;
                 while (i < ary.length)
                 {
                     lastarg.append(" " + ary[i]);
                     i++;
                 }
                 return_vector.add(lastarg.toString());
             } else {
                 return_vector.add(ary[i]);
             }
         }
         return return_vector;
     }
     
     // return the source part of a line
     public String getSource()
     {
         return this.source;
     }
     
     public void setSource(String value)
     {
         this.source = value;
     }
     
     public void setSource(IrcUser u)
     {
         this.source = u.toString();
     }
     
     public void addDestination(IrcUser value)
     {
         if (this.destination == null)
         {
             this.destination = new Vector<String>();
         }
         this.destination.add(value.toString());
     }
     
     public void addDestination(String value)
     {
         if (this.destination == null)
         {
             this.destination = new Vector<String>();
         }
         this.destination.add(value);
     }
     
     public void clearDestination()
     {
         this.destination = null;
     }
     
     public boolean inDestination(String value)
     {
         boolean ret_bool = false;
         if (this.destination != null)
         {
             for (Enumeration<String> e = this.destination.elements(); e.hasMoreElements(); )
             {
                 if (e.nextElement().equals(value))
                 {
                     ret_bool = true;
                 }
             }
         } else {
             ret_bool = true;
         }
         return ret_bool;
     }
 
     public boolean inDestination(IrcUser value)
     {
         boolean ret_bool = false;
         if (this.destination != null)
         {
             for (Enumeration<String> e = this.destination.elements(); e.hasMoreElements(); )
             {
                 if (value.is(e.nextElement()))
                 {
                     ret_bool = true;
                 }
             }
         } else {
             ret_bool = true;
         }
         return ret_bool;
     }
     
     public String getDestination()
     {
         if (this.destination != null)
         {
             StringBuffer destinations = new StringBuffer("");
             for (Enumeration<String> e = this.destination.elements(); e.hasMoreElements(); )
             {
                 destinations.append(e.nextElement());
                 if (e.hasMoreElements())
                 {
                     destinations.append(",");
                 }
             }
             return destinations.toString();
         } else {
             return null;
         }
     }
     
     public Vector<String> getDestinations()
     {
         return this.destination;
     }
     
     // return the command like JOIN, PART, etc
     public String getCommand()
     {
         return this.cmd;
     }
     
     public void setCommand(String value)
     {
         this.cmd = value;
     }
     
     // The Echo Response is for some commands that need to be echoed back to the client
     public String getEchoResponse()
     {
         return this.toString();
     }
     
     public String toString()
     {
         StringBuffer args_string = new StringBuffer("");
         for (Enumeration<String> e = args.elements(); e.hasMoreElements(); )
         {
             String ca = e.nextElement();
             if (ca.contains(" "))
             {
                 ca = ":" + ca;
             }
             args_string.append(ca);
             if (e.hasMoreElements())
             {
                 args_string.append(" ");
             }
         }
         return this.cmd + " " + args_string.toString();
     }
     
     // check to see if a command matches a string
     public boolean is(String text)
     {
         if (this.cmd.equals(text))
         {
             return true;
         } else {
             return false;
         }
     }
     
     // retrieve the arguments by index to a command
     public String getArg(int index)
     {
         try
         {
             return this.args.elementAt(index);
         } catch (Exception e) {
             return null;
         }
     }
     
     public Vector<String> getArgs()
     {
         return this.args;
     }
     
     public int argCount()
     {
         return this.args.size();
     }
     
     public JSONObject toJSONObject()
     {
         JSONObject job = new JSONObject();
         try
         {
             job.put("source", this.getSource());
             if (this.destination != null)
             {
                 job.put("destination", this.destination);
             }
             job.put("command", this.getCommand());
             job.put("args", this.args);
         } catch (Exception e) {}
         return job;
     }
     
     public static IRCMessage prepare(String cmd)
     {
         IRCMessage newm = new IRCMessage();
         newm.setCommand(cmd);
         return newm;
     }
 }
