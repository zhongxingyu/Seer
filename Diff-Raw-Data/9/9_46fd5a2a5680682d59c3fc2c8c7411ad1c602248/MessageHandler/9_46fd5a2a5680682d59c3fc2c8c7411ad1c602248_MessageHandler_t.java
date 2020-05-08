 package polly.eventhandler;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 import org.apache.log4j.Logger;
 
 import de.skuzzle.polly.parsing.Context;
 import de.skuzzle.polly.parsing.Declarations;
 import de.skuzzle.polly.parsing.InputParser;
 import de.skuzzle.polly.parsing.ParseException;
 import de.skuzzle.polly.parsing.Type;
 import de.skuzzle.polly.parsing.tree.ChannelLiteral;
 import de.skuzzle.polly.parsing.tree.Expression;
 import de.skuzzle.polly.parsing.tree.IdentifierLiteral;
 import de.skuzzle.polly.parsing.tree.ListLiteral;
 import de.skuzzle.polly.parsing.tree.Literal;
 import de.skuzzle.polly.parsing.tree.Root;
 import de.skuzzle.polly.parsing.tree.UserLiteral;
 import de.skuzzle.polly.sdk.Command;
 import de.skuzzle.polly.sdk.CommandManager;
 import de.skuzzle.polly.sdk.Signature;
 import de.skuzzle.polly.sdk.Types;
 import de.skuzzle.polly.sdk.eventlistener.IrcUser;
 import de.skuzzle.polly.sdk.eventlistener.MessageEvent;
 import de.skuzzle.polly.sdk.eventlistener.MessageListener;
 import de.skuzzle.polly.sdk.exceptions.InsufficientRightsException;
 import de.skuzzle.polly.sdk.exceptions.UnknownCommandException;
 import de.skuzzle.polly.sdk.exceptions.UnknownSignatureException;
 import de.skuzzle.polly.sdk.model.User;
 
 
 import polly.core.TypeMapper;
 import polly.core.UserManagerImpl;
 import polly.util.Pair;
 
 
 
 
 public class MessageHandler implements MessageListener {
 
 	private static Logger logger = Logger.getLogger(MessageHandler.class.getName());
     private CommandManager commands;
     private UserManagerImpl userManager;
     private String encodingName;
     
     public MessageHandler(CommandManager commandManager, UserManagerImpl userManager, 
             String encoding) {
         this.commands = commandManager;
         this.userManager = userManager;
         this.encodingName = encoding;
     }
     
 
     
     @Override
     public void publicMessage(MessageEvent e) {
         this.execute(e, false);
     }
 
     
     
     @Override
     public void privateMessage(MessageEvent e) {
         this.execute(e, true);
     }
 
     
     
     @Override
     public void actionMessage(MessageEvent e) {
         // TODO Auto-generated method stub
     }
     
     
     
     private void execute(MessageEvent e, boolean isQuery) {
         User executor = this.getUser(e.getUser());
         
         Pair<Command, Signature> p = this.parse(executor, e, e.getChannel());
         if (p == null) {
             return;
         }
         
         long i = System.currentTimeMillis();
         try {
             logger.debug("Executing '" + p.getSecond().toString() + 
                     "' on channel " + e.getChannel());
 
             
             p.getFirst().doExecute(executor, e.getChannel(), isQuery, p.getSecond());
             long time = System.currentTimeMillis() - i;
             logger.debug("Executed. Runtime: " + time + "ms");
         } catch (InsufficientRightsException e1) {
             e.getSource().sendMessage(e.getChannel(), "Du kannst den Befehl '" + 
                     p.getFirst().getCommandName() + "' nicht ausfhren.");
         } catch (Exception e1) {
             long time = System.currentTimeMillis() - i;
             logger.error("Exception while executing command '" + 
                     p.getFirst().getCommandName() + "': " + e1.getMessage(), e1);
             e.getSource().sendMessage(e.getChannel(), 
                     "Interner Fehler beim Ausfhren des Befehls.");
             logger.debug("Execution time: " + time + "ms");
         }
     }
     
     
     
     private User getUser(IrcUser user) {
         User u = this.userManager.getUser(user);
         if (u == null) {
             u = new polly.data.User("~UNKNOWN", "blabla", 0);
             u.setCurrentNickName(user.getNickName());
         }
         return u;
     }
     
     
 
     private Pair<Command, Signature> parse(User executor, MessageEvent e, 
             String replyChannel) {
         
         long i = System.currentTimeMillis();
         try {
         	logger.trace("Parsing input '" + e.getMessage() + "'");
         	// .trim(): remove trailing spaces to prevent strange error messages for
         	// inputs that have an additional space char.
        	InputParser parser = new InputParser();
        	Root root = parser.parse(e.getMessage().trim(), this.encodingName);

             if (root == null) {
             	logger.trace("No valid input");
             	return null;
             }
 
             logger.trace("Creating local context for command execution");
             Context c = this.createContext(e, executor);
             
             logger.trace("Starting context check");
             root.contextCheck(c);
             
             logger.trace("Collapsing all parameters");
             root.collapse(new Stack<Literal>());
 
             logger.trace("Resolving signature");
             Signature sig = this.createSignature(root);
             Command cmd = this.commands.getCommand(sig);
             long time = System.currentTimeMillis() - i;
             
             logger.debug("Signature resolved: " + sig.toString());
             logger.trace("Lookup time: " + time + "ms");
             return new Pair<Command, Signature>(cmd, sig);
             
         } catch (ParseException e1) {
             long time = System.currentTimeMillis() - i;
             logger.warn("Parse error: " + e1.getMessage(), e1);
             logger.debug("Calculationtime was " + time + "ms");
             
             String marked = e1.getPosition().mark(e.getMessage());
             e.getSource().sendMessage(replyChannel, e1.getMessage());
             e.getSource().sendMessage(replyChannel, "Eingabe: " + marked);
             
         } catch (UnknownSignatureException e1) {
         	logger.warn("Unknown signature '" + e1.getSignature().toString() 
         			+ "' called.");
         	e.getSource().sendMessage(replyChannel, "Unbekannte Signatur: " + 
         			e1.getSignature().toString());
 
         } catch (UnknownCommandException e1) {
             logger.warn("Unknown command: " + e1.getMessage());
             e.getSource().sendMessage(replyChannel, 
                     "Unbekannter Befehl: " + e1.getMessage());
             
         } catch (Exception other) {
         	logger.error("Unknown error: " + other.getMessage(), other);   
         	e.getSource().sendMessage(replyChannel, "Interner Fehler!");
         }
         return null;
     }
     
     
     
     private Context createContext(MessageEvent e, User user) throws ParseException {
         Declarations d = this.userManager.getDeclarations(user);
                 
         List<Expression> channels = new ArrayList<Expression>();
         for (String channel : e.getSource().getChannels()) {
             channels.add(new ChannelLiteral(channel));
         }
         
         // ISSUE: 0000008
         List<Expression> users = new ArrayList<Expression>();
         for (String u : e.getSource().getChannelUser(e.getChannel())) {
             users.add(new UserLiteral(u));
         }
         d.add(new IdentifierLiteral("me"), new UserLiteral(e.getUser().getNickName()));
         d.add(new IdentifierLiteral("here"), new ChannelLiteral(e.getChannel()));
         d.add(new IdentifierLiteral("all"), new ListLiteral(channels, Type.CHANNEL));
         d.add(new IdentifierLiteral("each"), new ListLiteral(users, Type.USER));
 
         logger.trace("    me   := " + e.getUser().getNickName());
         logger.trace("    here := " + e.getChannel());
         logger.trace("    all  := " + channels.toString());
         logger.trace("    each := " + users);
         
         //d = Declarations.createContext(d);
         return new Context(d, this.userManager.getNamespaces());
     }
     
     
     
     private Signature createSignature(Root root) throws UnknownSignatureException {
         List<Types> parameters = new ArrayList<Types>();
         for (Literal lit : root.getResults()) {
         	parameters.add(TypeMapper.literalToTypes(lit));
         }
         return new Signature(root.getName().getCommandName(), -1, parameters);
     }
 }
