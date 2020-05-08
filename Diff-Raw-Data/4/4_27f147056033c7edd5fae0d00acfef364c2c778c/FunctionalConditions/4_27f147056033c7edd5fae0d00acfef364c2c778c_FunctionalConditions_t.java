 /*******************************************************************************
  * eAdventure (formerly <e-Adventure> and <e-Game>) is a research project of the e-UCM
  *          research group.
  *   
  *    Copyright 2005-2012 e-UCM research group.
  *  
  *     e-UCM is a research group of the Department of Software Engineering
  *          and Artificial Intelligence at the Complutense University of Madrid
  *          (School of Computer Science).
  *  
  *          C Profesor Jose Garcia Santesmases sn,
  *          28040 Madrid (Madrid), Spain.
  *  
  *          For more info please visit:  <http://e-adventure.e-ucm.es> or
  *          <http://www.e-ucm.es>
  *  
  *  ****************************************************************************
  * This file is part of eAdventure, version 1.5.
  * 
  *   You can access a list of all the contributors to eAdventure at:
  *          http://e-adventure.e-ucm.es/contributors
  *  
  *  ****************************************************************************
  *       eAdventure is free software: you can redistribute it and/or modify
  *      it under the terms of the GNU Lesser General Public License as published by
  *      the Free Software Foundation, either version 3 of the License, or
  *      (at your option) any later version.
  *  
  *      eAdventure is distributed in the hope that it will be useful,
  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
  *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *      GNU Lesser General Public License for more details.
  *  
  *      You should have received a copy of the GNU Lesser General Public License
  *      along with Adventure.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.engine.core.control.functionaldata;
 
 import es.eucm.eadventure.common.data.chapter.conditions.Condition;
 import es.eucm.eadventure.common.data.chapter.conditions.Conditions;
 import es.eucm.eadventure.common.data.chapter.conditions.FlagCondition;
 import es.eucm.eadventure.common.data.chapter.conditions.GlobalState;
 import es.eucm.eadventure.common.data.chapter.conditions.GlobalStateCondition;
 import es.eucm.eadventure.common.data.chapter.conditions.VarCondition;
 import es.eucm.eadventure.engine.core.control.FlagSummary;
 import es.eucm.eadventure.engine.core.control.Game;
 import es.eucm.eadventure.engine.core.control.VarSummary;
 
 public class FunctionalConditions {
 
     private Conditions conditions;
 
     public FunctionalConditions( Conditions conditions ) {
 
         this.conditions = conditions;
     }
 
     /**
      * Returns whether all the conditions are ok
      * 
      * @return true if all the conditions are ok, false otherwise
      */
     public boolean allConditionsOk( ) {
 
         boolean conditionsOK = true;
 
         conditionsOK = evaluateSimpleConditionsWithAND( );
 
         for( int i = 0; i < conditions.getEitherConditionsBlockCount( ); i++ ) {
             Conditions eitherCondition = conditions.getEitherBlock( i );
             if( conditionsOK )
                 conditionsOK = new FunctionalConditions( eitherCondition ).evaluateSimpleConditionsWithOR( );
         }
 
         return conditionsOK;
     }
 
     /**
      * Returns whether all the conditions are satisfied
      * 
      * @return true if all the conditions are satisfied, false otherwise
      */
     private boolean evaluateSimpleConditionsWithAND( ) {
 
         boolean evaluation = true;
 
         FlagSummary flags = Game.getInstance( ).getFlags( );
         VarSummary vars = Game.getInstance( ).getVars( );
 
         for( Condition condition : conditions.getSimpleConditions( ) ) {
             if( evaluation ) {
                 if( condition.getType( ) == Condition.FLAG_CONDITION ) {
                     FlagCondition flagCondition = (FlagCondition) condition;
                     evaluation = flagCondition.isActiveState( ) == flags.isActiveFlag( condition.getId( ) );
                 }
                 else if( condition.getType( ) == Condition.VAR_CONDITION ) {
                     VarCondition varCondition = (VarCondition) condition;
                     int actualValue = vars.getValue( varCondition.getId( ) );
                     int state = varCondition.getState( );
                     int value = varCondition.getValue( );
                     evaluation = evaluateVarCondition( state, value, actualValue );
                 }
                 else if( condition.getType( ) == Condition.GLOBAL_STATE_CONDITION ) {
                     String globalStateId = condition.getId( );
                     GlobalStateCondition gsCondition = (GlobalStateCondition) condition;
                     GlobalState gs = Game.getInstance( ).getCurrentChapterData( ).getGlobalState( globalStateId );
                    evaluation = (gsCondition.getState( ) == GlobalStateCondition.GS_NOT_SATISFIED) ^ new FunctionalConditions( gs ).allConditionsOk( );
                 }
             }
         }
         return evaluation;
     }
 
     /**
      * Evaluates a var condition according to the state (function to use for
      * evaluation), value of comparison, and the actual value of the var
      * 
      * @param state >,
      *            >=, =, !=, < or <=
      * @param value
      *            The value to compare with
      * @param actualValue
      *            The actual value assigned to the var so far
      * @return True if condition is true; false otherwise
      */
     private boolean evaluateVarCondition( int state, int value, int actualValue ) {
 
         if( state == VarCondition.VAR_EQUALS ) {
             return actualValue == value;
         }
         else if( state == VarCondition.VAR_NOT_EQUALS ) {
             return actualValue != value;
         }
         
         else if( state == VarCondition.VAR_GREATER_EQUALS_THAN ) {
             return actualValue >= value;
         }
         else if( state == VarCondition.VAR_GREATER_THAN ) {
             return actualValue > value;
         }
         else if( state == VarCondition.VAR_LESS_EQUALS_THAN ) {
             return actualValue <= value;
         }
         else if( state == VarCondition.VAR_LESS_THAN ) {
             return actualValue < value;
         }
         else
             return false;
     }
 
     /**
      * Returns whether at least one condition is satisfied
      * 
      * @return true if at least one condition is satisfied, false otherwise
      */
     private boolean evaluateSimpleConditionsWithOR( ) {
 
         boolean evaluation = false;
 
         FlagSummary flags = Game.getInstance( ).getFlags( );
         VarSummary vars = Game.getInstance( ).getVars( );
 
         for( Condition condition : conditions.getSimpleConditions( ) )
             if( !evaluation ) {
                 if( condition.getType( ) == Condition.FLAG_CONDITION ) {
                     FlagCondition flagCondition = (FlagCondition) condition;
                     evaluation = flagCondition.isActiveState( ) == flags.isActiveFlag( condition.getId( ) );
                 }
                 else if( condition.getType( ) == Condition.VAR_CONDITION ) {
                     VarCondition varCondition = (VarCondition) condition;
                     int actualValue = vars.getValue( varCondition.getId( ) );
                     int state = varCondition.getState( );
                     int value = varCondition.getValue( );
                     evaluation = evaluateVarCondition( state, value, actualValue );
                 }
                 else if( condition.getType( ) == Condition.GLOBAL_STATE_CONDITION ) {
                     String globalStateId = condition.getId( );
                     GlobalStateCondition gsCondition = (GlobalStateCondition) condition;
                     GlobalState gs = Game.getInstance( ).getCurrentChapterData( ).getGlobalState( globalStateId );
                     evaluation = (gsCondition.getState( ) == GlobalStateCondition.GS_NOT_SATISFIED) ^ new FunctionalConditions( gs ).allConditionsOk( );
                 }
             }
 
         return evaluation;
     }
 
 }
