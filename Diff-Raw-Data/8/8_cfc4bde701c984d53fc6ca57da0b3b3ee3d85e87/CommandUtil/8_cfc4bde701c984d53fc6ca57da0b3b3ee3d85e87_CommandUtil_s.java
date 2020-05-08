 /* Copyright (c) 2012 Walker Crouse, <http://windwaker.net/>
  *
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 package net.windwaker.permissions.cmd;
 
 import net.windwaker.permissions.WindPerms;
 import net.windwaker.permissions.api.GroupManager;
 import net.windwaker.permissions.api.UserManager;
 import net.windwaker.permissions.api.permissible.Group;
 import net.windwaker.permissions.api.permissible.User;
 import net.windwaker.permissions.cmd.sub.GroupCommands;
 import net.windwaker.permissions.cmd.sub.PermissionsCommands;
 import net.windwaker.permissions.cmd.sub.UserCommands;
 
 import org.spout.api.command.CommandContext;
 import org.spout.api.command.CommandSource;
 import org.spout.api.command.annotated.Command;
 import org.spout.api.command.annotated.NestedCommand;
 import org.spout.api.exception.CommandException;
 
 /**
  * Holds cmd nesters and static util methods for cmd handling.
  */
 public class CommandUtil {
 	public CommandUtil(WindPerms plugin) {
 	}
 
 	/**
 	 * Gets a group from the given {@link CommandContext} and the index to get it from.
 	 * @param args
 	 * @param index
 	 * @return group
 	 * @throws CommandException is group is null
 	 */
 	public static Group getGroup(GroupManager groupManager, CommandContext args, int index) throws CommandException {
 		Group group = groupManager.getGroup(args.getString(index));
 		if (group == null) {
 			throw new CommandException("Group not found!");
 		}
 		return group;
 	}
 
 	/**
 	 * Gets a user from the given {@link CommandContext} and the index to get it from.
 	 * @param args
 	 * @param index
 	 * @return user
 	 * @throws CommandException if user is null
 	 */
 	public static User getUser(UserManager userManager, CommandContext args, int index) throws CommandException {
 		User user = userManager.getUser(args.getString(index));
 		if (user == null) {
 			throw new CommandException("User not found!");
 		}
 		return user;
 	}
 
 	/**
 	 * Gets a boolean value from the given {@link CommandContext} and the index to get it from.
 	 * @param args
 	 * @param index
 	 * @return boolean value
 	 */
 	public static boolean getBoolean(CommandContext args, int index) {
 		return Boolean.valueOf(args.getString(index));
 	}
 
 	/**
 	 * Checks if the given {@link CommandSource} has permission for the given node.
 	 * @param source
 	 * @param node
 	 * @throws CommandException if source does not have permission
 	 */
 	public static void checkPermission(CommandSource source, String node) throws CommandException {
 		if (!source.hasPermission(node)) {
 			throw new CommandException("You don't have permission to do that!");
 		}
 	}
 
 	/**
 	 * Nester for {@link PermissionsCommands}
 	 * @param args
 	 * @param source
 	 */
 	@Command(aliases = {"pr", "permissions", "wp", "windperms"}, desc = "General WindPerms commands.")
	@NestedCommand(commands = PermissionsCommands.class)
 	public void permissions(CommandContext args, CommandSource source) {
 	}
 
 	/**
 	 * Nester for {@link GroupCommands}
 	 * @param args
 	 * @param source
 	 */
 	@Command(aliases = {"group", "g"}, desc = "Modifies a group")
	@NestedCommand(commands = GroupCommands.class)
 	public void group(CommandContext args, CommandSource source) {
 	}
 
 	/**
 	 * Nester for {@link UserCommands}
 	 * @param args
 	 * @param source
 	 */
 	@Command(aliases = {"user", "u"}, desc = "Modify Permissions users.", usage = "<info|set|add|remove|check> [group|perm|build] [user] [group:groupName|perm:node|bool:build|identifier] [bool]", min = 0, max = 5)
	@NestedCommand(commands = UserCommands.class)
 	public void user(CommandContext args, CommandSource source) {
 	}
 }
