 /*
  * License (BSD Style License):
  * Copyright (c) 2012
  * Software Engineering
  * Department of Computer Science
  * Technische Universitiät Darmstadt
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * - Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  * - Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  * - Neither the name of the Software Engineering Group or Technische
  * Universität Darmstadt nor the names of its contributors may be used to
  * endorse or promote products derived from this software without specific
  * prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package de.opalproject.vespucci.sliceEditor.features;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.ICustomContext;
 import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
 import org.eclipse.graphiti.mm.algorithms.Text;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.platform.IPlatformImageConstants;
 
 public class DependencyKindCollapseFeature extends AbstractCustomFeature {
 
 	public DependencyKindCollapseFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 	
 	@Override
 	public String getName() {
 		return "Hide \"ALL\" Constraint-Labels";
 	}
 
 	@Override
 	public String getDescription() {
 		return "Hides all constraint-kind text labels displaying \"ALL\"";
 	}
 
 	@Override
 	public boolean canExecute(ICustomContext context) {
 		PictogramElement[] pes = context.getPictogramElements();
 		if (pes != null && pes.length == 1) {
 			if (pes[0] instanceof Diagram) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public String getImageId() {
 		return IPlatformImageConstants.IMG_EDIT_COLLAPSE;
 	}
 
 	@Override
 	public void execute(ICustomContext context) {
 		PictogramElement pictogramElements[] = context.getPictogramElements();
 		Diagram dia = (Diagram) pictogramElements[0];
 		EList<Connection> connections = dia.getConnections();
 		for (Connection connection : connections) {
 			for (ConnectionDecorator cd : connection.getConnectionDecorators()) {
 				if (cd.getGraphicsAlgorithm() instanceof Text
						&& ((Text) cd.getGraphicsAlgorithm()).getValue()
								.equals("ALL")) {
					if (cd.isVisible()) {
 						cd.setVisible(false);
 					} else {
 						cd.setVisible(true);
 					}
 				}
 			}
 		}
 	}
 }
