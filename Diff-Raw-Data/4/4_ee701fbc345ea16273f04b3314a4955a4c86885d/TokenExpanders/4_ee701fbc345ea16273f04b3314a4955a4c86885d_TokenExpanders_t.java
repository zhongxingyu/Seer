 package uk.co.codefoo.bukkit.saywhat.GameVariableToken;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentSkipListMap;
 
 import org.bukkit.entity.Player;
 
 public class TokenExpanders {
 	private ConcurrentSkipListMap<String, TokenExpander> tokenExpanderMap;
 	
 	public TokenExpanders()
 	{
 		tokenExpanderMap = new ConcurrentSkipListMap<String, TokenExpander>();
 
 		add("MyLocation");
 		add("InMyHand");
 		add("MOTD");
 		add("ServerName");
 	}
 
     private void add(String tokenClassName)
     {
         try
         {
             String fqTokenClassName = String.format(
                     "%s.%s", 
                     this.getClass().getPackage().getName(), 
                     tokenClassName);
 
             Class<? extends TokenExpander> tokenExpanderClass 
                 = Class.forName(fqTokenClassName).asSubclass(TokenExpander.class);
 
             TokenExpander tokenExpander = tokenExpanderClass.newInstance();
 
             tokenExpanderMap.put(tokenExpander.getToken().getName(), tokenExpander);
         }
         catch (Exception ex)
         {
             ex.printStackTrace(System.err);
         }
     }
 
     public String expandAllTokens(String message, Player currentPlayer)
     {
         if (!message.contains(Token.TokenPrefix))
         {
             return message;
         }
         if (tokenExpanderMap == null)
         {
             return message;
         }
 
         String result = message;
         for (Map.Entry<String, TokenExpander> tokenEntry : tokenExpanderMap.entrySet())
         {
            if (!message.contains(tokenEntry.getKey()))
            {
                continue;
            }
             result = result.replace(tokenEntry.getKey(), tokenEntry.getValue().getGameVariableValue(currentPlayer)); 
         }
         return result;
     }
     
     public String getTokenDescription(String tokenKey)
     {
         String result;
 
         if (tokenExpanderMap.size() == 0)
         {
             result="No tokens defined.";
             return result;
         }
         
         TokenExpander tokenExpander = tokenExpanderMap.get(tokenKey);
 
         if (tokenExpander==null)
         {
             result=String.format("There is no '%s' token.", tokenKey);
             return result;
         }
     
         result = String.format(
                 "'%s': %s.", 
                 tokenKey, 
                 tokenExpander.getToken().getDescription());
         return result;
     }
 
     public String getAllTokenKeys()
     {
         String result;
         
         if (tokenExpanderMap.size() == 0)
         {
             result="No tokens defined.";
             return result;
         }
 
         StringBuilder tokenListBuilder = new StringBuilder();
         tokenListBuilder.append("Tokens: ");
         for (Map.Entry<String, TokenExpander> tokenMapEntry : tokenExpanderMap.entrySet())
         {
             tokenListBuilder.append(tokenMapEntry.getKey());
             tokenListBuilder.append(' ');
         }
         result=tokenListBuilder.toString();
         return result;
     }
 }
