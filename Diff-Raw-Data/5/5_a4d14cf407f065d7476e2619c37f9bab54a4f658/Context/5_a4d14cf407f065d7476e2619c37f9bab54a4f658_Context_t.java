 /*
  * Copyright (C) 2012 TomyLobo
  *
  * This file is part of Routes.
  *
  * Routes is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package eu.tomylobo.routes.commands.system;
 
 import java.util.Arrays;
 
 import eu.tomylobo.abstraction.CommandSender;
 import eu.tomylobo.abstraction.entity.Player;
 
 /**
  * Holds all information about a command invocation.
  *
  * @author TomyLobo
  *
  */
 public class Context {
 	private final CommandSender sender;
 	private final String commandName;
 	private final String label;
 	private final String[] args;
 
 	public Context(CommandSender sender, String commandName, String label, String[] args) {
 		this.sender = sender;
 		this.commandName = commandName;
 		this.label = label;
 		this.args = args;
 	}
 
 	/**
	 * Returns the command sender contained in this Context.
 	 *
	 * @return the command sender.
 	 */
 	public CommandSender getSender() {
 		return sender;
 	}
 
 	/**
 	 * Casts the command sender to a {@link Player}. Throws an exception if it fails.
 	 *
 	 * @return the command sender as a {@link Player}.
 	 * @throws PlayerNeededException if the command sender is not a {@link Player}.
 	 */
 	public Player getPlayer() throws PlayerNeededException {
 		if (!(sender instanceof Player))
 			throw new PlayerNeededException();
 
 		return (Player) sender;
 	}
 
 	/**
 	 * Gets the internal name of the command.
 	 *
 	 * @return
 	 */
 	public String getCommandName() {
 		return commandName;
 	}
 
 	/**
 	 * Gets the name of the command as the user entered it.
 	 *
 	 * @return
 	 */
 	public String getLabel() {
 		return label;
 	}
 
 	/**
 	 * Returns the total number of arguments given.
 	 *
 	 * @return the total number of arguments given.
 	 */
 	public int length() {
 		return args.length;
 	}
 
 	Context getNested() {
 		if (args.length < 1)
 			return null;
 
 		final String subCommandName = commandName+"_"+args[0];
 		final String subLabel = commandName+" "+args[0];
 		final String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
 
 		return new Context(sender, subCommandName, subLabel, subArgs);
 	}
 
 	/**
 	 * Same as {@link #getSender()}.{@link CommandSender#sendMessage(String) sendMessage(message)}
 	 *
 	 * @param message The message to be sent.
 	 */
 	public void sendMessage(String message) {
 		sender.sendMessage(message);
 	}
 
 	/**
 	 * Same as {@link #getSender()}.{@link CommandSender#sendMessage(String) sendMessage(message)}
 	 *
 	 * @param message The message to be sent.
 	 */
 	public void sendFormattedMessage(String format, Object... args) {
 		sender.sendMessage(String.format(format, args));
 	}
 
 	/**
 	 * Gets the specified argument as a string.
 	 *
 	 * @param index index of the argument
 	 * @return the argument
 	 * @throws NotEnoughArgumentsException if there is no argument in the specified place
 	 */
 	public String getString(int index) throws NotEnoughArgumentsException {
 		if (index >= args.length)
 			throw new NotEnoughArgumentsException();
 
 		return args[index];
 	}
 
 	/**
 	 * Gets the specified argument as a string.
 	 *
 	 * @param index index of the argument
 	 * @return the argument or defaultValue if there is no argument in the specified place
 	 */
 	public String getString(int index, String defaultValue) {
 		if (index >= args.length)
 			return defaultValue;
 
 		return args[index];
 	}
 
 	/**
 	 * Gets the specified argument as a byte.
 	 *
 	 * @param index index of the argument
 	 * @return the argument
 	 * @throws NotEnoughArgumentsException if there is no argument in the specified place
 	 */
 	public byte getByte(int index) throws NotEnoughArgumentsException, NumberFormatException {
 		if (index >= args.length)
 			throw new NotEnoughArgumentsException();
 
 		return Byte.parseByte(args[index]);
 	}
 
 	/**
 	 * Gets the specified argument as a byte.
 	 *
 	 * @param index index of the argument
 	 * @return the argument or defaultValue if there is no argument in the specified place
 	 */
 	public byte getByte(int index, byte defaultValue) throws NumberFormatException {
 		if (index >= args.length)
 			return defaultValue;
 
 		return Byte.parseByte(args[index]);
 	}
 
 	/**
 	 * Gets the specified argument as a short.
 	 *
 	 * @param index index of the argument
 	 * @return the argument
 	 * @throws NotEnoughArgumentsException if there is no argument in the specified place
 	 */
 	public short getShort(int index) throws NotEnoughArgumentsException, NumberFormatException {
 		if (index >= args.length)
 			throw new NotEnoughArgumentsException();
 
 		return Short.parseShort(args[index]);
 	}
 
 	/**
 	 * Gets the specified argument as a short.
 	 *
 	 * @param index index of the argument
 	 * @return the argument or defaultValue if there is no argument in the specified place
 	 */
 	public short getShort(int index, short defaultValue) throws NumberFormatException {
 		if (index >= args.length)
 			return defaultValue;
 
 		return Short.parseShort(args[index]);
 	}
 
 	/**
 	 * Gets the specified argument as an int.
 	 *
 	 * @param index index of the argument
 	 * @return the argument
 	 * @throws NotEnoughArgumentsException if there is no argument in the specified place
 	 */
 	public int getInt(int index) throws NotEnoughArgumentsException, NumberFormatException {
 		if (index >= args.length)
 			throw new NotEnoughArgumentsException();
 
 		return Integer.parseInt(args[index]);
 	}
 
 	/**
 	 * Gets the specified argument as an int.
 	 *
 	 * @param index index of the argument
 	 * @return the argument or defaultValue if there is no argument in the specified place
 	 */
 	public int getInt(int index, int defaultValue) throws NumberFormatException {
 		if (index >= args.length)
 			return defaultValue;
 
 		return Integer.parseInt(args[index]);
 	}
 
 	/**
 	 * Gets the specified argument as a long.
 	 *
 	 * @param index index of the argument
 	 * @return the argument
 	 * @throws NotEnoughArgumentsException if there is no argument in the specified place
 	 */
 	public long getLong(int index) throws NotEnoughArgumentsException, NumberFormatException {
 		if (index >= args.length)
 			throw new NotEnoughArgumentsException();
 
 		return Long.parseLong(args[index]);
 	}
 
 	/**
 	 * Gets the specified argument as a long.
 	 *
 	 * @param index index of the argument
 	 * @return the argument or defaultValue if there is no argument in the specified place
 	 */
 	public long getLong(int index, long defaultValue) throws NumberFormatException {
 		if (index >= args.length)
 			return defaultValue;
 
 		return Long.parseLong(args[index]);
 	}
 
 	/**
 	 * Gets the specified argument as a float.
 	 *
 	 * @param index index of the argument
 	 * @return the argument
 	 * @throws NotEnoughArgumentsException if there is no argument in the specified place
 	 */
 	public float getFloat(int index) throws NotEnoughArgumentsException, NumberFormatException {
 		if (index >= args.length)
 			throw new NotEnoughArgumentsException();
 
 		return Float.parseFloat(args[index]);
 	}
 
 	/**
 	 * Gets the specified argument as a float.
 	 *
 	 * @param index index of the argument
 	 * @return the argument or defaultValue if there is no argument in the specified place
 	 */
 	public float getFloat(int index, float defaultValue) throws NumberFormatException {
 		if (index >= args.length)
 			return defaultValue;
 
 		return Float.parseFloat(args[index]);
 	}
 
 	/**
 	 * Gets the specified argument as a double.
 	 *
 	 * @param index index of the argument
 	 * @return the argument
 	 * @throws NotEnoughArgumentsException if there is no argument in the specified place
 	 */
 	public double getDouble(int index) throws NotEnoughArgumentsException, NumberFormatException {
 		if (index >= args.length)
 			throw new NotEnoughArgumentsException();
 
 		return Double.parseDouble(args[index]);
 	}
 
 	/**
 	 * Gets the specified argument as a double.
 	 *
 	 * @param index index of the argument
 	 * @return the argument or defaultValue if there is no argument in the specified place
 	 */
 	public double getDouble(int index, double defaultValue) throws NumberFormatException {
 		if (index >= args.length)
 			return defaultValue;
 
 		return Double.parseDouble(args[index]);
 	}
 
 	/**
 	 * Gets the specified argument as a boolean.
 	 *
 	 * @param index index of the argument
 	 * @return the argument
 	 * @throws NotEnoughArgumentsException if there is no argument in the specified place
 	 */
 	public boolean getBoolean(int index) throws NotEnoughArgumentsException, NumberFormatException {
 		if (index >= args.length)
 			throw new NotEnoughArgumentsException();
 
 		return Boolean.parseBoolean(args[index]);
 	}
 
 	/**
 	 * Gets the specified argument as a boolean.
 	 *
 	 * @param index index of the argument
 	 * @return the argument or defaultValue if there is no argument in the specified place
 	 */
 	public boolean getBoolean(int index, boolean defaultValue) throws NumberFormatException {
 		if (index >= args.length)
 			return defaultValue;
 
 		return Boolean.parseBoolean(args[index]);
 	}
 }
