 import java.io.*;
 import java.net.*;
 public class Receiver
 {
 public static void main(String []args) throws Exception
 {
 String s1=" ",s2=" ";
 int start=Integer.parseInt(args[0]);
 int port=Integer.parseInt(args[1]);
 ServerSocket ss=new ServerSocket(port);
 Socket s=ss.accept();
 BufferedReader br=new BufferedReader(new InputStreamReader(s.getInputStream()));
 String data=null;
 
 int count=start;
 
 PrintWriter pw=null;
 while((data=br.readLine())!=null)
 {
 
 if(data.equals("Some body stop me !")||count==start) {
 pw=new PrintWriter("output/kddcup"+count+".arff");
 System.out.println(" Receiving  file "+"kddcup"+count+".arff"+" ..... ");
 count++;
 }
 pw.println(data);
 pw.flush();

 }
 
 
 }
 }
 
