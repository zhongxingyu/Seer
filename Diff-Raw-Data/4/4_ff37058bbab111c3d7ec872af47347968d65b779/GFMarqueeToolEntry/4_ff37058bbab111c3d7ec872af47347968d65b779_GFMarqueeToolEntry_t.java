 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    mwenz - Bug 327669 - removed dependencies to GEF internal stuff
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.editor;
 
 import org.eclipse.gef.SharedImages;
 import org.eclipse.gef.palette.ToolEntry;
 import org.eclipse.gef.tools.MarqueeSelectionTool;
 import org.eclipse.graphiti.ui.internal.Messages;
 
 /**
  * A palette ToolEntry for a {@link org.eclipse.gef.tools.MarqueeSelectionTool}.
  * 
  * @author hudsonr
  * @since 2.1
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class GFMarqueeToolEntry extends ToolEntry {
 
 	/**
 	 * Creates a new MarqueeToolEntry that can select nodes.
 	 */
 	public GFMarqueeToolEntry() {
 		this(null, null);
 	}
 
 	/**
 	 * Constructor for MarqueeToolEntry.
 	 * 
 	 * @param label
 	 *            the label
 	 */
 	public GFMarqueeToolEntry(String label) {
 		this(label, null);
 	}
 
 	/**
 	 * Constructor for MarqueeToolEntry.
 	 * 
 	 * @param label
 	 *            the label; can be <code>null</code>
 	 * @param description
 	 *            the description (can be <code>null</code>)
 	 */
 	public GFMarqueeToolEntry(String label, String description) {
 		super(label, description, SharedImages.DESC_MARQUEE_TOOL_16, SharedImages.DESC_MARQUEE_TOOL_24, GFMarqueeSelectionTool.class);
 		if (label == null || label.length() == 0)
 			setLabel(Messages.GFMarqueeToolEntry_Marquee);
 		setUserModificationPermission(PERMISSION_NO_MODIFICATION);
 	}
 
 	/**
 	 * Gets the description.
 	 * 
 	 * @return the description
 	 * 
 	 * @see org.eclipse.gef.palette.PaletteEntry#getDescription()
 	 */
 	@Override
 	public String getDescription() {
 		String description = super.getDescription();
 		if (description != null)
 			return description;
 
 		Object value = getToolProperty(MarqueeSelectionTool.PROPERTY_MARQUEE_BEHAVIOR);
 		if (value instanceof Integer) {
 			int selectionType = ((Integer) value).intValue();
			// TODO Replace after 0.8.0 (in 0.8.0 Indigo we need to be
			// compatible with GEF 3.6 and 3.7)
			if (selectionType == MarqueeSelectionTool.BEHAVIOR_NODES_AND_CONNECTIONS)
 				return Messages.GFMarqueeToolEntry_MarqueeBothNodesAndRelatedConnections;
 			if (selectionType == MarqueeSelectionTool.BEHAVIOR_CONNECTIONS_TOUCHED)
 				return Messages.GFMarqueeToolEntry_MarqueeSelectionTouched;
 		}
 		return Messages.GFMarqueeToolEntry_MarqueeNodesTouched;
 	}
 
 }
