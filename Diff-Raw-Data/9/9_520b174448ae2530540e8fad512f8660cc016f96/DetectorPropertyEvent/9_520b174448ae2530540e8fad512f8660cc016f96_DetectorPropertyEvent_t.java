 /*-
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.diffraction;
 
 import java.util.EventObject;
 
 public class DetectorPropertyEvent extends EventObject {
	private static final long serialVersionUID = -4124035392214967002L;

 	static enum EventType {
 		ORIGIN,
 		BEAM_CENTRE,
 		HPXSIZE,
 		VPXSIZE,
 		NORMAL,
 		GEOMETRY,
 	}
 
 	private EventType type;
 
 	public DetectorPropertyEvent(Object source, EventType propertyType) {
 		super(source);
 		type = propertyType;
 	}
 
 	public EventType getType() {
 		return type;
 	}
 
 	public boolean hasOriginChanged() {
 		return type == EventType.ORIGIN || type == EventType.NORMAL || type == EventType.GEOMETRY;
 	}
 
 	public boolean hasBeamCentreChanged() {
 		return type == EventType.BEAM_CENTRE || type == EventType.GEOMETRY;
 	}
 
 	public boolean hasHPxSizeChanged() {
 		return type == EventType.HPXSIZE;
 	}
 
 	public boolean hasVPxSizeChanged() {
 		return type == EventType.VPXSIZE;
 	}
 
 	public boolean hasNormalChanged() {
 		return type == EventType.NORMAL || type == EventType.GEOMETRY;
 	}
 }
