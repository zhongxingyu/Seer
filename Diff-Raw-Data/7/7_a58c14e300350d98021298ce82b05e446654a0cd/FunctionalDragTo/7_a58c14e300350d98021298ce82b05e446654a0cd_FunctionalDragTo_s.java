 /**
  * <e-Adventure> is an <e-UCM> research project. <e-UCM>, Department of Software
  * Engineering and Artificial Intelligence. Faculty of Informatics, Complutense
  * University of Madrid (Spain).
  * 
  * @author Del Blanco, A., Marchiori, E., Torrente, F.J. (alphabetical order) *
  * @author Lpez Maas, E., Prez Padilla, F., Sollet, E., Torijano, B. (former
  *         developers by alphabetical order)
  * @author Moreno-Ger, P. & Fernndez-Manjn, B. (directors)
  * @year 2009 Web-site: http://e-adventure.e-ucm.es
  */
 
 /*
  * Copyright (C) 2004-2009 <e-UCM> research group
  * 
  * This file is part of <e-Adventure> project, an educational game & game-like
  * simulation authoring tool, available at http://e-adventure.e-ucm.es.
  * 
  * <e-Adventure> is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option) any
  * later version.
  * 
  * <e-Adventure> is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * <e-Adventure>; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  */
 package es.eucm.eadventure.engine.core.control.functionaldata.functionalactions;
 
 import es.eucm.eadventure.common.data.chapter.Action;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalElement;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalItem;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalNPC;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalPlayer;
 import es.eucm.eadventure.engine.core.data.GameText;
 
 /**
  * A custom action defined by the user that needs to objects to be performed.
  * 
  * @author Eugenio Marchiori
  * 
  */
 public class FunctionalDragTo extends FunctionalAction {
 
     /**
      * The first element of the action
      */
     private FunctionalElement element;
 
     /**
      * The other element of the action
      */
     private FunctionalElement anotherElement;
 
     /**
      * The total time of the action
      */
     private long totalTime;
 
     /**
      * Default constructor for a custom interact action
      * 
      * @param element
      *            The first element of the action
      * @param customActionName
      *            The name of the action
      */
     public FunctionalDragTo( FunctionalElement element) {
         super( null );
         this.type = Action.DRAG_TO;
         originalAction = element.getFirstValidAction( Action.DRAG_TO );
         this.element = element;
         this.requiersAnotherElement = true;
         this.needsGoTo = false;
         this.finished = false;
     }
 
     @Override
     public void drawAditionalElements( ) {
 
     }
 
     @Override
     public void setAnotherElement( FunctionalElement element ) {
         this.anotherElement = element;
         this.requiersAnotherElement = false;
        this.needsGoTo = originalAction.isNeedsGoTo( );
        this.keepDistance = originalAction.getKeepDistance( );
     }
 
     @Override
     public void start( FunctionalPlayer functionalPlayer ) {
         if( !requiersAnotherElement ) {
             this.functionalPlayer = functionalPlayer;
             this.totalTime = 0;
         }
     }
 
     @Override
     public void stop( ) {
         finished = true;
     }
 
     @Override
     public void update( long elapsedTime ) {
 
         if( anotherElement != null ) {
             totalTime += elapsedTime;
             if( totalTime > 1000 ) {
                 
                 if (element instanceof FunctionalItem)
                     spreadDrag((FunctionalItem) element);
                 else if (element instanceof FunctionalNPC)
                     spreadDrag((FunctionalNPC) element);
                 
                 
                 finished = true;
             }
         } 
     }
     
     //TODO include dragTo in FunctionalElement to avoid duplicate code
     private void spreadDrag(FunctionalItem item1){
         
         if( anotherElement instanceof FunctionalItem ) {
             FunctionalItem item2 = (FunctionalItem) anotherElement;
             if( !item1.dragTo(item2 ) ) {
                 if( functionalPlayer.isAlwaysSynthesizer( ) )
                     functionalPlayer.speakWithFreeTTS( GameText.getTextCustomCannot( ), functionalPlayer.getPlayerVoice( ) );
                 else
                     functionalPlayer.speak( GameText.getTextCustomCannot( ) );
             }
         }
         if( anotherElement instanceof FunctionalNPC ) {
             FunctionalNPC npc = (FunctionalNPC) anotherElement;
             if( !item1.dragTo( npc ) ) {
                 if( functionalPlayer.isAlwaysSynthesizer( ) )
                     functionalPlayer.speakWithFreeTTS( GameText.getTextCustomCannot( ), functionalPlayer.getPlayerVoice( ) );
                 else
                     functionalPlayer.speak( GameText.getTextCustomCannot( ) );
             }
         }
     }
     
    private void spreadDrag(FunctionalNPC item1){
         
         if( anotherElement instanceof FunctionalItem ) {
             FunctionalItem item2 = (FunctionalItem) anotherElement;
             if( !item1.dragTo(item2 ) ) {
                 if( functionalPlayer.isAlwaysSynthesizer( ) )
                     functionalPlayer.speakWithFreeTTS( GameText.getTextCustomCannot( ), functionalPlayer.getPlayerVoice( ) );
                 else
                     functionalPlayer.speak( GameText.getTextCustomCannot( ) );
             }
         }
         if( anotherElement instanceof FunctionalNPC ) {
             FunctionalNPC npc = (FunctionalNPC) anotherElement;
             if( !item1.dragTo( npc ) ) {
                 if( functionalPlayer.isAlwaysSynthesizer( ) )
                     functionalPlayer.speakWithFreeTTS( GameText.getTextCustomCannot( ), functionalPlayer.getPlayerVoice( ) );
                 else
                     functionalPlayer.speak( GameText.getTextCustomCannot( ) );
             }
         }
     }
 
 }
