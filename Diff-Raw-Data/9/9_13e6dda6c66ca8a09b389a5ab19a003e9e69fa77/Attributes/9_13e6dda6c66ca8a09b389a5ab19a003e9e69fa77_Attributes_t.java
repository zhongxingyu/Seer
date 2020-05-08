 /**
  * Copyright 2004-2009 Tobias Gierke <tobias.gierke@code-sourcery.de>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package de.codesourcery.eve.skills.datamodel;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import de.codesourcery.eve.skills.db.datamodel.AttributeType;
 import de.codesourcery.eve.skills.db.datamodel.Skill;
 
 public class Attributes {
 
 	private final ICharacter character;
 
 	private final Object LOCK = new Object();
 	// guarded-by: LOCK
 	private final Map<AttributeType,Attribute> attributes =
 		new HashMap<AttributeType, Attribute>();
 
 	public Attributes(ICharacter c) {
 		if (c == null) {
 			throw new IllegalArgumentException("c cannot be NULL");
 		}
 		this.character = c;
 	}
 
 	public void reconcile(ICharacter payload) {
 
 		synchronized( LOCK ) {
 			final Attributes other = payload.getAttributes();
 			for ( AttributeType t : AttributeType.values() ) {
 				this.attributes.put(t , other.getBaseAttribute( t ) );
 			}
 		}
 	}
 	
 	/**
 	 * Returns the training speed in SP/hour
 	 * for a given skill.
 	 * 
 	 * The training speed is based on the
 	 * character's effective attribute values
 	 * (base value + implants) for the
 	 * skills primary and secondary attributes.
 	 * 
 	 * @param skillTree SkillTree , used to look-up learning skills
 	 * @param skill the skill to train
 	 * @return training speed in SP/hour
 	 */	
 	public float calcTrainingSpeed(SkillTree skillTree , Skill s) {
 		
 		final AttributeType primary = s.getPrimaryAttribute();
 		final AttributeType secondary = s.getSecondaryAttribute();
 			
 		float primaryValue = getEffectiveAttributeValue(skillTree , primary );
 			
 		float secondaryValue = getEffectiveAttributeValue(skillTree , secondary );
 			
 		return ( primaryValue + ( secondaryValue / 2.0f ) ) * 60.0f;
 	}
 	
 	public float getEffectiveAttributeValue(SkillTree skillTree,AttributeType attribute) {
 		
 		// base value
 		float baseValue = character.getAttributes().getBaseValue( attribute );
 		
 		// implant modifier
 		final float implants  = character.getImplantSet().getAttributeModifier( attribute );
 		
		return ( baseValue + implants );
 	}
 	
 	/**
 	 * Returns the attribute modifier value
 	 * from learning skills (only!) for a given attribute.
 	 * 
 	 * @param character
 	 * @param tree
 	 * @param attributeType
 	 * @return
 	 */
 //	private float getAttributeModifierFromLearningSkills( SkillTree tree , AttributeType attributeType) 
 //	{
 //		float result = 0;
 //		for ( LearningSkillType skillType : LearningSkillType.getAffectingSkills( attributeType ) ) {
 //			
 //			if ( skillType == LearningSkillType.LEARNING ) {
 //				// learnings skill level affects all base attribute values
 //				// , needs to be applied to attribute base value
 //				continue;
 //			}
 //			
 //			final int basicSkillId =
 //				skillType.getBasicSkillTypeId();
 //			
 //			result+= 5; // character.getCurrentLevel( tree.getSkill( basicSkillId ) );
 //			
 //			// final int advancedSkillId = skillType.getAdvancedSkillTypeId();
 //			
 //			result+= 5; // character.getCurrentLevel( tree.getSkill( advancedSkillId ) );
 //		}
 //		
 //		return result;
 //	}
 	
 	public Attribute getBaseAttribute(AttributeType t) {
 		return getAttribute( t ); 
 	}
 
 	private Attribute getAttribute(AttributeType type) {
 
 		if (type == null) {
 			throw new IllegalArgumentException("type cannot be NULL");
 		}
 
 		synchronized( LOCK ) {
 			Attribute  result = attributes.get( type );
 			if ( result == null ) {
 				result = new Attribute( type , 0 );
 				attributes.put( type , result );
 			}
 			return result;
 		}
 	}
 
 	public float getBaseValue(AttributeType type) {
 
 		if (type == null) {
 			throw new IllegalArgumentException("type cannot be NULL");
 		}
 
 		return getAttribute( type ).getValue();
 	}
 
 	public float getValue(SkillTree tree , AttributeType type) {
 
 		/*
 ( Base + Basic_Skill_Lv + Adv_Skill_Lv + Implant_Bonus ) * ( 1+ ( Learning_Lv * 0.02 ) )		 
 		 */
 		float result= getBaseValue( type );
 
 		result += character.getImplantSet().getAttributeModifier( type );
 		// result *= ( 1+ Skill.getBasicLearningSkill( tree , character , LearningSkillType.LEARNING).getLevel()*0.02f );
 		return result;
 	}
 
 	public void setBaseValue(AttributeType type, float value) {
 		getAttribute( type ).setValue( value );
 	}
 
 	public Attributes cloneAttributes() {
 
 		synchronized( LOCK ) {
 			final Attributes result = new Attributes( this.character );
 			for ( AttributeType t : attributes.keySet() ) {
 				result.setBaseValue( t , getAttribute( t ).getValue() );
 			}
 			return result;
 		}
 	}
 
 }
