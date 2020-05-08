 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.chalmers.dat255.sleepfighter.model.challenge;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import se.chalmers.dat255.sleepfighter.model.IdProvider;
 import se.chalmers.dat255.sleepfighter.utils.message.Message;
 import se.chalmers.dat255.sleepfighter.utils.message.MessageBus;
 
import com.google.common.base.Objects;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.j256.ormlite.field.DatabaseField;
 import com.j256.ormlite.table.DatabaseTable;
 
 /**
  * ChallengeConfigSet models the set of challenges that are enabled for an Alarm.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Oct 3, 2013
  */
 @DatabaseTable(tableName = "challenge_set")
 public class ChallengeConfigSet implements IdProvider {
 	public static final String ID_COLUMN = "id";
 
 	/* --------------------------------
 	 * Defined Events.
 	 * --------------------------------
 	 */
 
 	/**
 	 * ChallengeConfigSet.Event is the interface for all events in ChallengeConfigSet
 	 *
 	 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 	 * @version 1.0
 	 * @since Oct 4, 2013
 	 */
 	public static interface Event extends Message {
 		/**
 		 * Returns the ChallengeConfigSet that triggered the event.
 		 *
 		 * @return the set.
 		 */
 		public ChallengeConfigSet getSet();
 
 		/**
 		 * Returns the old value.
 		 *
 		 * @return the old value.
 		 */
 		public Object getOldValue();
 	}
 
 	/**
 	 * ChallengeEvent is the interface for all events that relate to a specific ChallengeConfig.
 	 *
 	 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 	 * @version 1.0
 	 * @since Oct 4, 2013
 	 */
 	public static interface ChallengeEvent extends Event {
 		/**
 		 * Returns the specific ChallengeConfig that this related to this event.
 		 *
 		 * @return the config object.
 		 */
 		public ChallengeConfig getChallengeConfig();
 	}
 
 	/**
 	 * Base event for all Event subclasses.
 	 *
 	 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 	 * @version 1.0
 	 * @since Oct 4, 2013
 	 */
 	private abstract static class BaseEvent implements Event {
 		private ChallengeConfigSet set;
 		private Object oldValue;
 
 		/**
 		 * Constructs the event given the set, and the old value.
 		 *
 		 * @param set the ChallengeConfigSet that triggered the event.
 		 * @param old the old value.
 		 */
 		protected BaseEvent( ChallengeConfigSet set, Object old ) {
 			this.set = set;
 			this.oldValue = old;
 		}
 
 		/**
 		 * Returns the ChallengeConfigSet that triggered the event.
 		 *
 		 * @return the set.
 		 */
 		public ChallengeConfigSet getSet() {
 			return this.set;
 		}
 
 		/**
 		 * Returns the old value.
 		 *
 		 * @return the old value.
 		 */
 		public Object getOldValue() {
 			return this.oldValue;
 		}
 	}
 
 	/**
 	 * Base event for all ChallengeEvent subclasses.
 	 *
 	 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 	 * @version 1.0
 	 * @since Oct 4, 2013
 	 */
 	private abstract static class ChallengeBaseEvent extends BaseEvent implements ChallengeEvent {
 		private ChallengeConfig config;
 
 		/**
 		 * Constructs the event given the set, and the old value.
 		 *
 		 * @param set the ChallengeConfigSet that triggered the event.
 		 * @param old the old value.
 		 * @param config the specific ChallengeConfig related to event.
 		 */
 		protected ChallengeBaseEvent( ChallengeConfigSet set, Object old, ChallengeConfig config ) {
 			super( set, old );
 			this.config = config;
 		}
 
 		@Override
 		public ChallengeConfig getChallengeConfig() {
 			return this.config;
 		}
 	}
 
 	/**
 	 * EnabledEvent is issued when {@link ChallengeConfigSet#setEnabled(boolean)}<br/>
 	 * changes the value of a call to {@link ChallengeConfigSet#isEnabled()}.
 	 *
 	 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 	 * @version 1.0
 	 * @since Oct 4, 2013
 	 */
 	public static final class EnabledEvent extends BaseEvent {
 		/**
 		 * Constructs the event given the set, and the old value.
 		 *
 		 * @param set the ChallengeConfigSet that triggered the event.
 		 * @param old the old value.
 		 */
 		private EnabledEvent( ChallengeConfigSet set, Object old ) {
 			super( set, old );
 		}
 	}
 
 	/**
 	 * ChallengeEnabledEvent is issued when a call to<br/>
 	 * {@link ChallengeConfigSet#setEnabled(ChallengeType, boolean)}<br/>
 	 * changes the value of a call to {@link ChallengeConfig#isEnabled()}.
 	 *
 	 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 	 * @version 1.0
 	 * @since Oct 4, 2013
 	 */
 	public static final class ChallengeEnabledEvent extends ChallengeBaseEvent {
 		/**
 		 * Constructs the event given the set, and the old value.
 		 *
 		 * @param set the ChallengeConfigSet that triggered the event.
 		 * @param old the old value.
 		 * @param config the specific ChallengeConfig related to event.
 		 */
 		public ChallengeEnabledEvent( ChallengeConfigSet set, Object old, ChallengeConfig config ) {
 			super( set, old, config );
 		}
 	}
 
 	/**
 	 * ChallengeParamEvent is issued when a call to<br/>
 	 * {@link ChallengeConfigSet#setConfigParam(ChallengeType, String, String)}<br/>
 	 * or any similar method causes a call to {@link ChallengeConfig#getParam(String)}<br/>
 	 * to return a different value.
 	 * 
 	 *
 	 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 	 * @version 1.0
 	 * @since Oct 4, 2013
 	 */
 	public static final class ChallengeParamEvent extends ChallengeBaseEvent {
 		private String key;
 
 		/**
 		 * Constructs the event given the set, the old value, and the parameter key.
 		 *
 		 * @param set the ChallengeConfigSet that triggered the event.
 		 * @param old the old value.
 		 * @param config the specific ChallengeConfig related to event.
 		 * @param key the key of the changed parameter.
 		 */
 		public ChallengeParamEvent( ChallengeConfigSet set, Object old, ChallengeConfig config, String key ) {
 			super( set, old, config );
 			this.key = key;
 		}
 
 		/**
 		 * Returns the key the changed parameter.
 		 *
 		 * @return the key.
 		 */
 		public String getKey() {
 			return this.key;
 		}
 	}
 
 	/* --------------------------------
 	 * Fields.
 	 * --------------------------------
 	 */
 
 	@DatabaseField(generatedId = true, columnName = ID_COLUMN)
 	private int id;
 
 	@DatabaseField
 	private boolean enabled = true;
 
 	private Map<ChallengeType, ChallengeConfig> challenges;
 
 	private MessageBus<Message> messageBus;
 
 	/* --------------------------------
 	 * Constructors.
 	 * --------------------------------
 	 */
 
 	/**
 	 * Constructs a config set with no challenges initially put in.<br/>
 	 * It is expected that every ChallengeConfigSet will bind all ChallengeType:s to a corresponding ChallengeConfig,<br/>
 	 * this constructor must be therefore be followed by a series of {@link #putChallenge(ChallengeConfig)} calls.
 	 */
 	public ChallengeConfigSet() {
 		this.challenges = Maps.newEnumMap( ChallengeType.class );
 	}
 
 	/**
 	 * Constructs a config set with no challenges initially put in and with either enabled or disabled state.<br/>
 	 * It is expected that every ChallengeConfigSet will bind all ChallengeType:s to a corresponding ChallengeConfig,<br/>
 	 * this constructor must be therefore be followed by a series of {@link #putChallenge(ChallengeConfig)} calls.
 	 *
 	 * @param enabled true if any challenge should be enabled.
 	 */
 	public ChallengeConfigSet( boolean enabled ) {
 		this();
 		this.enabled = enabled;
 	}
 
 	/**
 	 * Copy constructor.
 	 *
 	 * @param rhs the set to copy from.
 	 */
 	public ChallengeConfigSet( ChallengeConfigSet rhs ) {
 		this( rhs.enabled );
 		this.setMessageBus( rhs.messageBus );
 
 		for ( ChallengeConfig config : rhs.challenges.values() ) {
 			this.putChallenge( new ChallengeConfig( config ) );
 		}
 	}
 
 	/* --------------------------------
 	 * Public Interface.
 	 * --------------------------------
 	 */
 
 	/**
 	 * Sets the message bus to publish events to.<br/>
 	 *
 	 * @param messageBus the message bus, or null if no messages should be received.
 	 */
 	public void setMessageBus( MessageBus<Message> messageBus ) {
 		this.messageBus = messageBus;
 	}
 
 	/**
 	 * Returns the currently used message bus or null if none.
 	 *
 	 * @return the message bus.
 	 */
 	public MessageBus<Message> getMessageBus() {
 		return this.messageBus;
 	}
 
 	@Override
 	public int getId() {
 		return this.id;
 	}
 
 	/**
 	 * Returns whether or not challenges are enabled for specific set (Alarm).
 	 *
 	 * @return true if enabled.
 	 */
 	public boolean isEnabled() {
 		return this.enabled;
 	}
 
 	/**
 	 * Sets whether or not challenges should be enabled at all for this set.<br/>
 	 * Setting this to true to does not enable all challenge types - but setting to false
 	 *
 	 * @param enabled whether or not to enable or disable challenges.
 	 */
 	public void setEnabled( boolean enabled ) {
 		if ( this.enabled == enabled ) {
 			return;
 		}
 
 		boolean old = this.enabled;
 		this.enabled = enabled;
 
 		this.publish( new EnabledEvent( this, old ) );
 	}
 
 	/**
 	 * Sets whether or not a specific challenge type should be enabled or not.<br/>
 	 * For this to actually mean that a challenge can occur,<br/>
 	 * a call to {@link #isEnabled()} must return true.
 	 *
 	 * @param type
 	 * @param enabled
 	 */
 	public void setEnabled( ChallengeType type, boolean enabled ) {
 		ChallengeConfig config = this.getConfig( type );
 
 		boolean old = config.isEnabled();
 		if ( old == enabled ) {
 			return;
 		}
 
 		config.setEnabled( enabled );
 
 		this.publish( new ChallengeEnabledEvent( this, old, config ) );
 	}
 
 	/**
 	 * Returns the list of enabled types.
 	 *
 	 * @return the enabled challenge types.
 	 */
 	public Set<ChallengeType> getEnabledTypes() {
 		Set<ChallengeType> types = Sets.newHashSet();
 
 		for ( Entry<ChallengeType, ChallengeConfig> entry : this.challenges.entrySet() ) {
 			if ( entry.getValue().isEnabled() ) {
 				types.add( entry.getKey() );
 			}
 		}
 
 		return types;
 	}
 
 	/**
 	 * Returns the set of defined types for the set.
 	 *
 	 * @return the set of defined types.
 	 */
 	public Set<ChallengeType> getDefinedTypes() {
 		return this.challenges.keySet();
 	}
 
 	/**
 	 * Returns a configuration for given challenge type.
 	 *
 	 * @param type the type.
 	 * @return the configuration.
 	 */
 	public ChallengeConfig getConfig( ChallengeType type ) {
 		return this.challenges.get( type );
 	}
 
 	/**
 	 * Returns an immutable collection of all challenge config objects.
 	 *
 	 * @return the collection.
 	 */
 	public Collection<ChallengeConfig> getConfigs() {
 		return Collections.unmodifiableCollection( this.challenges.values() );
 	}
 
 	/**
 	 * FACADE: Sets a config parameter for type with key to value.
 	 *
 	 * @see ChallengeConfig#setParam(String, String)
 	 * @param type the challenge type.
 	 * @param key the key.
 	 * @param value the value.
 	 * @return the old value.
 	 */
 	public void setConfigParam( ChallengeType type, String key, String value ) {
 		ChallengeConfig config = this.getConfig( type );
 		String old = config.setParam( key, value );
 
		if ( Objects.equal( old, value ) ) {
 			return;
 		}
 
 		this.publish( new ChallengeParamEvent( this, old, config, key ) );
 	}
 
 	/**
 	 * <p>Puts a {@link ChallengeConfig} in collection of challenges,<br/>
 	 * bypassing any and all checks, and does not send any event to bus.</p>
 	 *
 	 * <p>Only recommended for advance use such as persistence, factorization.</p>
 	 *
 	 * @param challenge the {@link ChallengeConfig} to put.
 	 */
 	public void putChallenge( ChallengeConfig challenge ) {
 		this.challenges.put( challenge.getType(), challenge );
 	}
 
 	/* --------------------------------
 	 * Private Methods.
 	 * --------------------------------
 	 */
 
 	/**
 	 * Publishes an event to event bus.
 	 *
 	 * @param event the event to publish.
 	 */
 	private void publish( Event event ) {
 		if ( this.messageBus == null ) {
 			return;
 		}
 
 		this.messageBus.publish( event );
 	}
 }
