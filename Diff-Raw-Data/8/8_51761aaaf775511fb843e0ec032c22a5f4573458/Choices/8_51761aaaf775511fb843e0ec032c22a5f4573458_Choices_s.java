 package com.oreilly.common.interaction.text.helpers;
 
 import java.util.ArrayList;
import java.util.HashMap;
 
 import com.oreilly.common.interaction.text.Interaction;
 import com.oreilly.common.interaction.text.InteractionPage;
 import com.oreilly.common.interaction.text.error.AbortInteraction;
 import com.oreilly.common.interaction.text.error.ContextDataRequired;
 import com.oreilly.common.interaction.text.error.GeneralInteractionError;
 
 
 public class Choices {
 	
	HashMap< String, Choice > choices = new HashMap< String, Choice >();
 	String prependToNumber = "";
 	String appendToNumber = ". ";
 	
 	
 	static public Choices getChoices( InteractionPage page, Interaction interaction ) throws ContextDataRequired {
 		return interaction.getContextData( Choices.class, interaction, getChoiceKey( page ) );
 	}
 	
 	
 	static protected String getChoiceKey( Object page ) {
 		return page.getClass().getName() + "_choices";
 	}
 	
 	
 	public Choices( InteractionPage page, Interaction interaction ) {
 		interaction.context.put( getChoiceKey( page ), this );
 	}
 	
 	
 	public Choices withNumberStyle( String prepend, String append ) {
 		prependToNumber = prepend;
 		appendToNumber = append;
 		return this;
 	}
 	
 	
 	public void addAlias( Choice choice, String alias ) {
 		choices.put( alias.toLowerCase().trim(), choice );
 	}
 	
 	
 	public ArrayList< String > generateChoiceList() {
 		ArrayList< String > result = new ArrayList< String >();
 		// make sure we don't have null values
 		if ( prependToNumber == null )
 			prependToNumber = "";
 		if ( appendToNumber == null )
 			appendToNumber = "";
 		// making a list... 
 		for ( String key : choices.keySet() ) {
 			Choice choice = choices.get( key );
			result.add( 0, prependToNumber + key + appendToNumber + choice.text );
 		}
 		return result;
 	}
 	
 	
 	public Choice addInternalChoice( String text, String returnValue ) {
 		return addInternalChoice( choices.size() + 1, text, returnValue );
 	}
 	
 	
 	public Choice addInternalChoice( int number, String text, String returnValue ) {
 		ChoiceInternal choice = new ChoiceInternal( this, text, returnValue );
 		choices.put( Integer.toString( number ), choice );
 		return choice;
 	}
 	
 	
 	public Choice addPageChoice( String text, InteractionPage... pages ) {
 		return addPageChoice( choices.size() + 1, text, pages );
 	}
 	
 	
 	public Choice addPageChoice( int number, String text, InteractionPage... pages ) {
 		ChoicePage choice = new ChoicePage( this, text, pages );
 		choices.put( Integer.toString( number ), choice );
 		return choice;
 	}
 	
 	
 	public Choice addAbortChoice( String text ) {
 		return addAbortChoice( choices.size() + 1, text );
 	}
 	
 	
 	public Choice addAbortChoice( int number, String text ) {
 		ChoiceAbort choice = new ChoiceAbort( this, text );
 		choices.put( Integer.toString( number ), choice );
 		return choice;
 	}
 	
 	
 	public String takeAction( InteractionPage page, Interaction interaction, Object data ) throws AbortInteraction,
 			ContextDataRequired, GeneralInteractionError {
 		String key = data.toString().toLowerCase().trim();
 		Choice choice = choices.get( key );
 		if ( choice == null )
 			throw new GeneralInteractionError( "Unable to determine your choice based on \"" + data.toString() + "\"" );
 		if ( choice instanceof ChoiceAbort )
 			throw new AbortInteraction();
 		if ( choice instanceof ChoicePage ) {
 			interaction.addPages( ( (ChoicePage)choice ).pages );
 			interaction.context.remove( getChoiceKey( page ) );
 			return null;
 		}
 		if ( choice instanceof ChoiceInternal ) {
 			interaction.context.remove( getChoiceKey( page ) );
 			return page.takeAction( interaction, ( (ChoiceInternal)choice ).returnValue );
 		}
 		// otherwise...
 		throw new GeneralInteractionError( "Unsupported choice type" );
 	}
 	
 }
