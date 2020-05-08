 /*
  * FindBugs - Find bugs in Java programs
  * Copyright (C) 2003-2005 University of Maryland
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package edu.umd.cs.findbugs.detect;
 
 import org.apache.bcel.classfile.Code;
 
 import edu.umd.cs.findbugs.BugInstance;
 import edu.umd.cs.findbugs.BugReporter;
 import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;
 
 public class DumbMethods extends OpcodeStackDetector {
 
 	private final BugReporter bugReporter;
 
 	public DumbMethods(BugReporter bugReporter) {
 		this.bugReporter = bugReporter;
 	}
 
 	@Override
 	public void sawOpcode(int seen) {
		if (seen == INVOKESTATIC && (getClassConstantOperand().equals("java/lang/System")
				|| getClassConstantOperand().equals("java/lang/Runtime"))
 				&& getNameConstantOperand().equals("runFinalizersOnExit")) {
 			bugReporter.reportBug(new BugInstance(this, "DM_RUN_FINALIZERS_ON_EXIT", HIGH_PRIORITY)
 					.addClassAndMethod(this).addSourceLine(this));
 		}
 
 		if (((seen == INVOKESTATIC && getClassConstantOperand().equals("java/lang/System")) || (seen == INVOKEVIRTUAL && getClassConstantOperand()
 				.equals("java/lang/Runtime")))
 				&& getNameConstantOperand().equals("gc")
 				&& getSigConstantOperand().equals("()V")) {
 			bugReporter.reportBug(new BugInstance(this, "DM_GC", HIGH_PRIORITY).addClassAndMethod(
 					this).addSourceLine(this));
 		}
 	}
 
 }
