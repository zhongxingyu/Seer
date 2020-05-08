 package no.runsafe.accounts;
 
 import no.runsafe.accounts.commands.Token;
 import no.runsafe.framework.RunsafePlugin;
 import no.runsafe.framework.command.Command;
 
 public class Plugin extends RunsafePlugin
 {
 	@Override
 	protected void PluginSetup()
 	{
 		this.addComponent(Engine.class);
 
 		Command account = new Command("account", "A collection of account management tools.", null);
 		account.addSubCommand(getInstance(Token.class));
 		this.addComponent(account);
 	}
}
