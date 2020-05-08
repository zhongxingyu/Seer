 /*
  * Copyright (C) 2012 Fabian Hirschmann <fabian@hirschm.net>
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
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package com.github.fhirschmann.clozegen.lib.constraints.api;
 
 import com.github.fhirschmann.clozegen.lib.components.GapAnnotator;
 import org.uimafit.component.Resource_ImplBase;
 
 /**
  * A constraint.
  *
  * <p>
  * This interface is used in order to inject a constraint into
  * {@link GapAnnotator}. Example implementation of this can be
 * found in {@link com.github.fhirschmann.clozegen.lib.adapter.constraint}.
  * </p>
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public abstract class ConstraintResource
     extends Resource_ImplBase implements ConstraintProvider {
 }
