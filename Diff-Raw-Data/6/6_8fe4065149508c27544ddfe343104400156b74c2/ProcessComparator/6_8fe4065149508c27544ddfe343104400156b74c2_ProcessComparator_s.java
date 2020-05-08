 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.processes.ui.internal.columns;
 
 import java.io.Serializable;
 import java.util.Comparator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.tcf.te.tcf.processes.core.model.ProcessTreeNode;
 
 /**
  * The comparator for the tree column "name".
  */
 public class ProcessComparator implements Comparator<ProcessTreeNode> , Serializable {
     private static final long serialVersionUID = 1L;
 	/*
 	 * (non-Javadoc)
 	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
 	 */
 	@Override
 	public int compare(ProcessTreeNode node1, ProcessTreeNode node2) {
 		if (node1 == null && node2 == null) return 0;
 		if (node1 != null && node2 == null) return 1;
 		if (node1 == null && node2 != null) return -1;
 
 		// Get the labels
		String text1 = node1 == null ? null : node1.name;
		String text2 = node2 == null ? null : node2.name;
 
 		// Normalize labels
 		if (text1 == null) text1 = ""; //$NON-NLS-1$
 		if (text2 == null) text2 = ""; //$NON-NLS-1$
 
 		// The tree sorts not strictly alphabetical. First comes entries starting with numbers,
 		// second entries starting with uppercase and than all the rest. Additional, if a label
 		// contains
 		// uppercase characters, it is sorted in before any labels being lowercase only.
 		if (text1.length() > 0 && text2.length() > 0) {
 			// Get the first characters of both
 			char c1 = text1.charAt(0);
 			char c2 = text2.charAt(0);
 
 			if (Character.isDigit(c1) || Character.isDigit(c2)) {
 				// Check on the differences. If both are digits, the standard compare will do it
 				if (Character.isDigit(c1) && !Character.isDigit(c2)) return -1;
 				if (!Character.isDigit(c1) && Character.isDigit(c2)) return 1;
 			}
 
 			if (Character.isUpperCase(c1) || Character.isUpperCase(c2)) {
 				// Check on the differences. If both are uppercase characters, the standard compare
 				// will do it
 				if (Character.isUpperCase(c1) && !Character.isUpperCase(c2)) return -1;
 				if (!Character.isUpperCase(c1) && Character.isUpperCase(c2)) return 1;
 			}
 
 			Matcher m1 = Pattern.compile("(\\D+)(\\d+)").matcher(text1); //$NON-NLS-1$
 			Matcher m2 = Pattern.compile("(\\D+)(\\d+)").matcher(text2); //$NON-NLS-1$
 			if (m1.matches() && m2.matches()) {
 				String p11 = m1.group(1);
 				String p12 = m1.group(2);
 
 				String p21 = m2.group(1);
 				String p22 = m2.group(2);
 
 				if (p11 != null && p11.equals(p21)) {
 					// Compare the second parts as number
 					try {
 						int result = 0;
 						long l1 = Long.parseLong(p12);
 						long l2 = Long.parseLong(p22);
 
 						if (l1 > l2) result = 1;
 						if (l1 < l2) result = -1;
 
 						return result;
 					}
 					catch (NumberFormatException e) { /* ignored on purpose */
 					}
 				}
 			}
 
 			if (text1.matches(".*[A-Z]+.*") || text2.matches(".*[A-Z]+.*")) { //$NON-NLS-1$ //$NON-NLS-2$
 				if (text1.matches(".*[A-Z]+.*") && !text2.matches(".*[A-Z]+.*")) return -1; //$NON-NLS-1$ //$NON-NLS-2$
 				if (!text1.matches(".*[A-Z]+.*") && text2.matches(".*[A-Z]+.*")) return 1; //$NON-NLS-1$ //$NON-NLS-2$
 
 				// Additionally, it even depends on the position of the first uppercase
 				// character if both strings contains them :-(
 				int minLength = Math.min(text1.length(), text2.length());
 				for (int i = 0; i < minLength; i++) {
 					char ch1 = text1.charAt(i);
 					char ch2 = text2.charAt(i);
 
 					if (Character.isUpperCase(ch1) && !Character.isUpperCase(ch2)) return -1;
 					if (!Character.isUpperCase(ch1) && Character.isUpperCase(ch2)) return 1;
 					// If both are uppercase, we break the loop and compare as usual
 					if (Character.isUpperCase(ch1) && Character.isUpperCase(ch2)) break;
 				}
 			}
 		}
 		return text1.compareTo(text2);
 	}
 }
