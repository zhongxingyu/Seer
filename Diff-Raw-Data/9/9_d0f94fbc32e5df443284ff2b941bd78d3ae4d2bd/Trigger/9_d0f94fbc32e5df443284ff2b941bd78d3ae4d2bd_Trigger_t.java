 package de.kumpelblase2.dragonslair.api;
 
 import java.sql.*;
 import java.util.*;
 import de.kumpelblase2.dragonslair.*;
 import de.kumpelblase2.dragonslair.utilities.GeneralUtilities;
 
 public class Trigger
 {
 	private int id;
 	private TriggerType type;
 	private Option[] type_options;
 	private List<Integer> events = new ArrayList<Integer>();
 	private final Set<Cooldown> cooldowns = Collections.synchronizedSet(new HashSet<Cooldown>());
 
 	public Trigger()
 	{
 		this.id = -1;
 		this.type_options = new Option[0];
 	}
 
 	public Trigger(final ResultSet result)
 	{
 		try
 		{
 			this.id = result.getInt(TableColumns.Triggers.ID);
 			this.type = TriggerType.valueOf(result.getString(TableColumns.Triggers.TYPE).toUpperCase());
 			final String options = result.getString(TableColumns.Triggers.TYPE_OPTIONS);
 			if(options.contains(":"))
 			{
 				final String[] optionSplitt = options.split(";");
 				this.type_options = new Option[optionSplitt.length];
 				for(int i = 0; i < optionSplitt.length; i++)
 				{
 					try
 					{
 						final String[] splitt = optionSplitt[i].split(":");
 						this.type_options[i] = new Option(splitt[0], splitt[1]);
 					}
 					catch(final Exception e)
 					{
 						DragonsLairMain.Log.warning("Unable to parse trigger option: " + optionSplitt[i]);
 					}
 				}
 			}
 			else
 				this.type_options = new Option[0];
 			
 			final String eventsString = result.getString(TableColumns.Triggers.ACTION_EVENT_ID);
 			if(eventsString != null && eventsString.contains(";"))
 			{
 				final String[] eventsSplitt = eventsString.split(";");
 				final Integer[] eventIDs = new Integer[eventsSplitt.length];
 				for(int i = 0; i < eventsSplitt.length; i++)
 				{
 					try
 					{
 						eventIDs[i] = Integer.parseInt(eventsSplitt[i]);
 					}
 					catch(final Exception e)
 					{
 						DragonsLairMain.Log.warning("Unable to parse event id " + eventsSplitt[i]);
 						continue;
 					}
 				}
 				
 				this.events = new ArrayList<Integer>(Arrays.asList(eventIDs));
 			}
 			else
 			{
 				try
 				{
 					if(eventsString != null && !eventsString.equals(""))
 					{
 						final Integer id = Integer.parseInt(eventsString);
 						this.events.add(id);
 					}
 				}
 				catch(final Exception e)
 				{
 					DragonsLairMain.Log.warning("Unable to parse event id " + eventsString);
 				}
 			}
 			
 			final String cooldownString = result.getString(TableColumns.Triggers.COOLDOWNS);
 			if(cooldownString != null && cooldownString.length() > 0)
 			{
 				final String[] splitted = cooldownString.split(";");
 				for(final String cd : splitted)
 				{
 					final String[] cdSplit = cd.split(":");
 					this.cooldowns.add(new Cooldown(cdSplit[0], Integer.parseInt(cdSplit[1])));
 				}
 			}
 		}
 		catch(final SQLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public int getID()
 	{
 		return this.id;
 	}
 
 	public TriggerType getType()
 	{
 		return this.type;
 	}
 
 	public void setType(final TriggerType inType)
 	{
 		this.type = inType;
 	}
 
 	public Option[] getOptions()
 	{
 		return this.type_options;
 	}
 
 	public String getOption(final String key)
 	{
 		for(final Option o : this.type_options)
 			if(o.getType().equals(key))
 				return o.getValue();
 		return null;
 	}
 
 	public void setOptions(final String... inOptions)
 	{
 		final Option[] tmpOptions = new Option[inOptions.length];
 		for(int i = 0; i < inOptions.length; i++)
 			try
 			{
 				final String[] splitt = inOptions[i].split(":");
 				tmpOptions[i] = new Option(splitt[0], splitt[1]);
 			}
 			catch(final Exception e)
 			{
 				DragonsLairMain.Log.warning("Unable to parse trigger option: " + inOptions[i]);
 			}
 		this.type_options = tmpOptions;
 	}
 
 	public void setOptions(final Option... options)
 	{
 		this.type_options = options;
 	}
 
 	public List<Integer> getEventIDs()
 	{
 		return this.events;
 	}
 
 	public void setEventIDs(final List<Integer> inEvent)
 	{
 		this.events = inEvent;
 	}
 
 	public void save()
 	{
 		try
 		{
 			GeneralUtilities.recalculateOptions(this);
 			final StringBuilder optionString = new StringBuilder();
 			for(int i = 0; i < this.type_options.length; i++)
 			{
 				optionString.append(this.type_options[i].getType() + ":" + this.type_options[i].getValue());
 				if(i != this.type_options.length - 1)
 					optionString.append(";");
 			}
 			final StringBuilder eventString = new StringBuilder();
 			for(int i = 0; i < this.events.size(); i++)
 			{
 				if(this.events.get(i) == 0)
 					continue;
 				eventString.append(this.events.get(i));
 				if(i != this.events.size() - 1)
 					eventString.append(";");
 			}
 			if(this.id != -1)
 			{
 				final PreparedStatement st = DragonsLairMain.createStatement("REPLACE INTO " + Tables.TRIGGERS + "(" + "trigger_id," + "trigger_type," + "trigger_type_options," + "trigger_action_event," + "trigger_cooldowns" + ") VALUES(?,?,?,?,?)");
 				st.setInt(1, this.id);
 				st.setString(2, this.type.toString().toLowerCase());
 				st.setString(3, optionString.toString());
 				st.setString(4, eventString.toString());
 				st.setString(5, this.getCooldowns());
 				st.execute();
 			}
 			else
 			{
 				final PreparedStatement st = DragonsLairMain.createStatement("INSERT INTO " + Tables.TRIGGERS + "(" + "trigger_type," + "trigger_type_options," + "trigger_action_event," + "trigger_cooldowns" + ") VALUES(?,?,?,?)");
 				st.setString(1, this.type.toString().toLowerCase());
 				st.setString(2, optionString.toString());
 				st.setString(3, eventString.toString());
 				st.setString(4, this.getCooldowns());
 				st.execute();
 				final ResultSet keys = st.getGeneratedKeys();
 				if(keys.next())
 					this.id = keys.getInt(1);
 			}
 		}
 		catch(final Exception e)
 		{
 			DragonsLairMain.Log.warning("Unable to save trigger " + this.id);
 			DragonsLairMain.Log.warning(e.getMessage());
 		}
 	}
 
 	public String getOptionString()
 	{
 		final StringBuilder sb = new StringBuilder();
 		for(int i = 0; i < this.type_options.length; i++)
 		{
 			sb.append(this.type_options[i].getType() + ":" + this.type_options[i].getValue());
 			if(i != this.type_options.length)
 				sb.append(";");
 		}
 		return sb.toString();
 	}
 
 	public void addOption(final Option o)
 	{
 		final List<Option> options = new ArrayList<Option>(Arrays.asList(this.type_options));
 		options.add(o);
 		this.type_options = options.toArray(new Option[0]);
 	}
 
 	public void setOption(final String key, final String value)
 	{
 		for(final Option o : this.type_options)
 			if(o.getType().equals(key))
 			{
 				o.setValue(value);
 				return;
 			}
 		this.addOption(new Option(key, value));
 	}
 
 	public void removeOption(final String key)
 	{
 		final List<Option> options = new ArrayList<Option>(Arrays.asList(this.type_options));
 		for(int i = 0; i < options.size(); i++)
 			if(options.get(i).getType().equals(key))
 			{
 				options.remove(i);
 				return;
 			}
 	}
 
 	public void remove()
 	{
 		try
 		{
			final PreparedStatement st = DragonsLairMain.createStatement("DELETE FROM " + Tables.TRIGGERS + " WHERE `trigger_id` = ?");
 			st.setInt(1, this.getID());
 			st.execute();
 		}
 		catch(final Exception e)
 		{
 			DragonsLairMain.Log.warning("Unable to remove trigger with id " + this.getID());
 			DragonsLairMain.Log.warning(e.getMessage());
 		}
 	}
 
 	public boolean canUse(final String name)
 	{
 		final Iterator<Cooldown> cooldown = this.cooldowns.iterator();
 		while(cooldown.hasNext())
 		{
 			final Cooldown cd = cooldown.next();
 			if(cd.equals(name))
 			{
 				if(!cd.isOnCooldown())
 				{
 					this.cooldowns.remove(cd);
 					return false;
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public int getCooldown()
 	{
 		if(this.getOption("cooldown") == null)
 			return GeneralUtilities.getDefaultCooldown(this.getType());
 		else
 			return Integer.parseInt(this.getOption("cooldown"));
 	}
 
 	public void use(final String name)
 	{
 		this.cooldowns.add(new Cooldown(name, this.getCooldown()));
 	}
 
 	private String getCooldowns()
 	{
 		final StringBuilder sb = new StringBuilder();
 		for(final Cooldown cd : this.cooldowns)
 		{
 			sb.append(cd.getDungeonName() + ":" + cd.getRemainingTime());
 			sb.append(";");
 		}
 		if(sb.length() > 1)
 			sb.substring(0, sb.length() - 1);
 		return sb.toString();
 	}
 
 	public void removeCooldown(final String name)
 	{
 		final Iterator<Cooldown> cooldown = this.cooldowns.iterator();
 		while(cooldown.hasNext())
 		{
 			final Cooldown cd = cooldown.next();
 			if(cd.equals(name))
 			{
 				this.cooldowns.remove(cd);
 				return;
 			}
 		}
 	}
 
 	public void clearCooldowns()
 	{
 		final Iterator<Cooldown> cooldown = this.cooldowns.iterator();
 		while(cooldown.hasNext())
 		{
 			final Cooldown cd = cooldown.next();
 			if(!cd.isOnCooldown())
 				this.cooldowns.remove(cd);
 		}
 	}
 }
