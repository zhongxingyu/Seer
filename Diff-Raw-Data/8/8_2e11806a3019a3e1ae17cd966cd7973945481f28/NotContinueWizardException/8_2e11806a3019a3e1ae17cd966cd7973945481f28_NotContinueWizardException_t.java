 /* gvSIG. Geographic Information System of the Valencian Government
 *
 * Copyright (C) 2007-2008 Infrastructures and Transports Department
 * of the Valencian Government (CIT)
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 */
 
 /*
 * AUTHORS (In addition to CIT):
 * 2010 {Prodevelop}   {Task}
 */
  
 package org.gvsig.gui.beans.wizard.panel;
 
 import java.awt.Component;
import java.util.Map;
 
 import org.gvsig.tools.exception.BaseException;
 
 /**
  * <p>
  * This exception is thrown if the wizard can not continue by any reason.
  * </p>
  * @author <a href="mailto:jpiera@gvsig.org">Jorge Piera Llodr&aacute;</a>
  */
 public class NotContinueWizardException extends BaseException{
 	private static final long serialVersionUID = -850687874081155952L;
 	private static final String KEY = "not_continue_with_wizard";	
 	private Component component = null;
 	private boolean displayMessage = true;
 	
 	/**
 	 * @see BaseException#BaseException(String, Throwable, String, long)
 	 */
 	public NotContinueWizardException(String message, Throwable cause, Component component) {
 		super(message, cause, KEY, serialVersionUID);		
 		this.component = component;
 	}
 	
 	public NotContinueWizardException(String message, Component component, boolean displayMessage) {
 	    super(message, KEY, serialVersionUID);
 	    this.component = component;
 	    this.displayMessage = displayMessage;
     }
 	
 	public Component getComponent() {
 		return component;
 	}
 	
 	public boolean displayMessage(){
 	    return displayMessage;
 	}
 
	@Override
	protected Map values() {
		// TODO Auto-generated method stub
		return null;
	}

 }
 
