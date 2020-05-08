 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is S23M.
  *
  * The Initial Developer of the Original Code is
  * SoftMetaWare Limited (SoftMetaWare).
  * Portions created by the Initial Developer are
  * Copyright (C) 2011 SoftMetaWare Ltd.
  * All Rights Reserved.
  *
  * Contributor(s):
  * Jorn Bettin
  * ***** END LICENSE BLOCK ***** */
package org.s23m.cell.api.computation;
 
 import org.s23m.cell.Set;
 import org.s23m.cell.api.VisitorFunction;
 
 public interface Computation {
 	Set initializeWalk(final VisitorFunction visitorFunction);
 	Set walkDownThenRight(final VisitorFunction visitorFunction);
 	Set walkDownThenLeft(final VisitorFunction visitorFunction);
 	Set walkRightThenDown(final VisitorFunction visitorFunction);
 	Set walkLeftThenDown(final VisitorFunction visitorFunction);
 }
