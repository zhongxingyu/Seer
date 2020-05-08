 package Application;
 
 import Command.CommandParser;
 import Player.PlayerParser;
 import Player.Role;
 import Player.Rounder;
 import UI.Map;
 import UI.UIException;
 import UI.UIObserver;
 import Util.CommandSplitter;
 
 class Controller {
     private final Rounder rounder = new Rounder();
     private final UIObserver ui = new Map();
     private final SubSystem subSystem = new SubSystem(ui);
     private final CommandParser parser = new CommandParser(subSystem.getPropManager(), subSystem.getEstateManager());
 
    public Controller(String players, String initialFund) {
         initializeRounder(players);
        initialFund(initialFund);
     }
 
    public void initializeRounder(String players) {
        PlayerParser parser = new PlayerParser(subSystem.getObservers());
        for (int index = 0; index != players.length(); ++index) {
            Role role = parser.get(players.charAt(index));
            rounder.add(role);
        }
    }

    private void initialFund(String fund) {
         try {
             subSystem.getEstateManager().setInitialFund(Integer.parseInt(fund));
         } catch (java.lang.NumberFormatException e) {
             throw new UIException("输入金额有误。");
         }
     }
 
     public void handleCommand(String input) {
         CommandSplitter splitter = new CommandSplitter(input);
         parser.get(splitter.name()).execute(rounder.current(), splitter.argument());
         rounder.next();
     }
 
     public String getPromptMessageForCurrentPlayer() {
         return rounder.current().name() + ">";
     }
 }
