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
 
 import java.util.Map;
 
 import se.chalmers.dat255.sleepfighter.model.IdProvider;
 import se.chalmers.dat255.sleepfighter.persist.ChallengeParamDao;
 import se.chalmers.dat255.sleepfighter.utils.StringUtils;
 
 import com.google.common.collect.Maps;
 import com.j256.ormlite.field.DatabaseField;
 import com.j256.ormlite.table.DatabaseTable;
 
 /**
  * ChallengeParam models one parameter for a ChallengeConfig<br/>
  * this is provided for the database mostly and is reworked<br/>
  * on runtime to fit ChallengeConfig as a map.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Oct 3, 2013
  */
 @DatabaseTable(tableName = "challenge_params", daoClass = ChallengeParamDao.class)
 public class ChallengeParam implements IdProvider {
 	public static final String CHALLENGE_ID_COLUMN = "challenge_id";
 
	@DatabaseField(columnName = CHALLENGE_ID_COLUMN, uniqueCombo = true, index = true)
 	private int challengeId;
 
 	@DatabaseField(uniqueCombo = true, index = true)
 	private String key;
 
 	@DatabaseField
 	private String value;
 
 	/**
 	 * Constructs the parameter with no given values.
 	 */
 	public ChallengeParam() {
 	}
 
 	/**
 	 * Constructs the parameter with given challenge ID, key and value.
 	 *
 	 * @param id the ID of the challenge config.
 	 * @param key the key of the parameter.
 	 * @param value the value of the parameter.
 	 */
 	public ChallengeParam( int id, String key, String value ) {
 		this.challengeId = id;
 		this.key = key;
 		this.value = value;
 	}
 
 	@Override
 	public int getId() {
 		return this.challengeId;
 	}
 
 	/**
 	 * Returns the key of the param.
 	 *
 	 * @return the key-
 	 */
 	public String getKey() {
 		return this.key;
 	}
 
 	/**
 	 * Returns the value of the param.
 	 *
 	 * @return the value.
 	 */
 	public String getValue() {
 		return this.value;
 	}
 
 	@Override
 	public String toString() {
 		final Map<String, String> prop = Maps.newHashMap();
 		prop.put( "id", Integer.toString( this.getId() ) );
 		prop.put( "key", this.getKey() );
 		prop.put( "value", this.getValue() );
 
 		return "ChallengeParam[" + StringUtils.PROPERTY_MAP_JOINER.join( prop ) + "]";
 	}
 }
