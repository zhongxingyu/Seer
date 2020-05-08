 package session;
 
 import java.util.Vector;
 
 public class Session extends Thread{
 
 	private static Integer status;
 	private static Vector<Object> contacts; // TODO: typer
 	private static Vector<Object> groups; 	// TODO: typer
 	private static String pseudo;
 	private static String perso_msg;
 	
 	public Session()
 	{
 		setStatus(0);
 		setPseudo("");
 		contacts = new Vector<Object>();
 		groups = new Vector<Object>();
 		setPerso_msg("");
 	}
 	
 	public void run()
 	{
 		Update();
 	}
 	
 	public static void Update()
 	{
 		while(true)
 		{
 			
 		}
 	}
 	
 	public static void CreateNewContact(Object cont)
 	{
 		contacts.add(cont);
 	}
 	
 	public static void CreateNewGroup(Object grp)
 	{
		groups.add(grp);
 	}
 	
 	public static void setStatus(Integer st) { Session.status = st;	}
 	public static Integer getStatus() {	return status; }
 	public static void setPseudo(String pseudo) { Session.pseudo = pseudo; }
 	public static String getPseudo() { return pseudo; }
 	public static void setPerso_msg(String perso_msg) {	Session.perso_msg = perso_msg; }
 	public static String getPerso_msg() { return perso_msg; }
 }
