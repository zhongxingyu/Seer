 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer;
 
 import imi.character.avatar.Avatar;
 import imi.character.statemachine.corestates.ActionInfo;
 import imi.character.statemachine.corestates.ActionState;
 import imi.character.statemachine.corestates.CycleActionState;
 import java.util.HashMap;
 
 /**
  *
  * Overload AvatarContext to add playMiscAnimation
  *
  * @author paulby
  */
 public class WlAvatarContext extends imi.character.avatar.AvatarContext {
 
     private HashMap<String, ActionInfo> actionMap = new HashMap();
     private ActionInfo currentActionInfo = null;
 
     public WlAvatarContext(Avatar avatar) {
         super(avatar);
 
         if (avatar.getCharacterParams().isAnimateBody())
             for(ActionInfo actionInfo : getGenericAnimations()) {
                 actionMap.put(actionInfo.getAnimationName(), actionInfo);
             }
     }
 
     /**
      * Return the names of the animations available to this character
      * @return
      */
    public Iterable<String> getAnimationNames() {
         return actionMap.keySet();
     }
 
    public void playMiscAnimation(String name) {
         if (getavatar().getCharacterParams().isAnimateBody()) {
             setMiscAnimation(name);
 
             // Force the trigger, note that this transition is so fast that the
             // state machine may not actually change state. Therefore in triggerAlert
             // we check for the trigger and force the state change.
             triggerReleased(TriggerNames.MiscAction.ordinal());
             triggerPressed(TriggerNames.MiscAction.ordinal());
             triggerReleased(TriggerNames.MiscAction.ordinal());
         }
     }
 
    public void setMiscAnimation(String animationName) {
         currentActionInfo = actionMap.get(animationName);
         ActionState action = (ActionState) gameStates.get(CycleActionState.class);
         action.setAnimationSetBoolean(false);
         currentActionInfo.apply(action);
     }
 
     @Override
     protected void triggerAlert(int trigger, boolean pressed) {
         if (pressed && trigger==TriggerNames.MiscAction.ordinal()) {
             // Force animation to play if this is a Misc trigger
             setCurrentState((ActionState) gameStates.get(CycleActionState.class));
         }
     }
 }
