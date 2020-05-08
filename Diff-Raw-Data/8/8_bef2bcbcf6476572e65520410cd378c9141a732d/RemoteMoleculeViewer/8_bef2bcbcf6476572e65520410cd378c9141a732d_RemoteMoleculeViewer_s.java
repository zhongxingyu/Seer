 /**
  * This file is part of Atomic Tagging.
  * 
  * Atomic Tagging is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
  * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * Atomic Tagging is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with Atomic Tagging. If not, see
  * <http://www.gnu.org/licenses/>.
  */
 package org.atomictagging.moleculehandler.base;
 
 import java.io.File;
 
 import org.atomictagging.core.moleculehandler.GenericViewer;
 import org.atomictagging.core.moleculehandler.IMoleculeViewer;
 import org.atomictagging.core.moleculehandler.MoleculeHandlerFactory;
 import org.atomictagging.core.types.CoreTags;
 import org.atomictagging.core.types.IAtom;
 import org.atomictagging.core.types.IMolecule;
 
 /**
  * @author Stephan Mann
  */
 public class RemoteMoleculeViewer extends GenericViewer {
 
 	@Override
 	public boolean canHandle( IMolecule molecule ) {
 		if ( molecule.getAtomTags().contains( CoreTags.FILEREF_REMOTE_TAG ) ) {
 			return true;
 		}
 		return false;
 	}
 
 
 	@Override
 	public void showMolecule( IMolecule molecule ) {
 		for ( IAtom atom : molecule.getAtoms() ) {
 			if ( atom.getTags().contains( CoreTags.FILEREF_TAG )
 					&& atom.getTags().contains( CoreTags.FILEREF_REMOTE_TAG ) ) {
 
				File file = new File( atom.getData() );
 				if ( file.canRead() ) {
 					IMoleculeViewer viewer = MoleculeHandlerFactory.getInstance().getNextViewer( molecule, this );
 					viewer.showMolecule( molecule );
 				}
 			}
 		}
 
 	}
 
 
 	@Override
 	public int getOrdinal() {
 		return Integer.MAX_VALUE - 100;
 	}
 
 
 	@Override
 	public String getUniqueId() {
 		return "atomictagging-remoteviewer";
 	}
 
 }
