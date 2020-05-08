 package org.rev317.debug;
 
 import org.parabot.core.paint.AbstractDebugger;
 import org.rev317.api.methods.Equipment;
 import org.rev317.api.wrappers.hud.Tab;
 
import java.awt.*;

 /**
  * 
  * @author Paradox, Demmonic
  *
  */
 class DEquipment extends AbstractDebugger {
 	
 	private boolean enabled = false;
 	
 	@Override
 	public void toggle() {
 		enabled = !enabled;
 	}
 
 	@Override
 	public boolean isEnabled() {
 		return enabled;
 	}
 
 	@Override
 	public void paint(Graphics g) {
 		if (Tab.getOpened() != Tab.EQUIPMENT) {
 			return;
 		}
 		for (Equipment e : Equipment.values()) {
 			if (e.getEquippedId() != 0) {
 				g.drawString("" + e.getEquippedId(), e.getPoint().x, e.getPoint().y);
 			}
 		}
 	}
 	
 }
