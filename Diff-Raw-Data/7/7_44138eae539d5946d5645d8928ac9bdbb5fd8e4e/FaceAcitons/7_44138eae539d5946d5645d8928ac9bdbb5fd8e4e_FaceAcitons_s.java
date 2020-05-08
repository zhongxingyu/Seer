 package br.ufsc.facebookapp.iu;
 
 import java.util.Scanner;
 import facebook4j.Friend;
 import facebook4j.ResponseList;
 import facebook4j.api.FriendMethods;
 
 import br.ufsc.facebookapp.excecoes.PostEmBranco;
 
 public class FaceAcitons {
 
 	Scanner leitor;
 	
 	@SuppressWarnings("resource")
 	public FaceAcitons()
 	{
 		leitor = new Scanner(System.in).useDelimiter(System.getProperty("line.separator"));
 	}
 	
 	public String postar() throws PostEmBranco
 	{
 		String s = leitor.next();
 		s = s.trim();
 		if(s.length() == 0)
 			throw new PostEmBranco();
 		else
 			return s;
 		
 	}
 	
 	public String friendList (ResponseList<Friend> friends)
 	{
 		String f = "";
 		for (int i= 0; i< 50; i++)
			f = "\n" + friends.get(i).getName();
 		return f;
 	}
 
 	public String newsFeed (ResponseList<Post> feed)
 	{
 		String f = "";
 		for (int i= 0; i< 10; i++)
 		{
			f = "\n" + feed.get(i).getName();
			f = "\n" + feed.get(i).getMessage() + "\n";
 		}
 		return f;
 	}
 }
