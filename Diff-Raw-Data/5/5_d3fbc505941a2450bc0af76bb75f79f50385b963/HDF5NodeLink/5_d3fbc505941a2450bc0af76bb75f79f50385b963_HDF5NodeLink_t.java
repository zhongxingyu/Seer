 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.diamond.scisoft.analysis.hdf5;
 
import java.io.Serializable;

 /**
  * Link two HDF5 nodes together. The name of the link provides a reference for users to the destination node
  */
public class HDF5NodeLink implements Serializable {
 	private HDF5Node from;
 	private HDF5Node to;
 	private String name;
 	private String path;
 
 	/**
 	 * A node link
 	 * @param path to source
 	 * @param link name (ends in '/' for groups)
 	 * @param source node which link starts from (can be null)
 	 * @param destination node which link points to
 	 */
 	public HDF5NodeLink(final String path, final String link, final HDF5Node source, final HDF5Node destination) {
 		if (link == null || destination == null) {
 			throw new IllegalArgumentException("Path name, link name and destination must be defined");
 		}
 		this.path = path == null ? "" : path;
 		name = link;
 		from = source;
 		to = destination;
 //		if ((to instanceof HDF5Group) && !name.endsWith(HDF5Node.SEPARATOR)) {
 //			throw new IllegalArgumentException("If destination is a group then name must end with a separator character");
 //		}
 	}
 
 	public HDF5Node getSource() {
 		return from;
 	}
 
 	public HDF5Node getDestination() {
 		return to;
 	}
 
 	public boolean isDestinationADataset() {
 		return to instanceof HDF5Dataset;
 	}
 
 	public boolean isDestinationAGroup() {
 		return to instanceof HDF5Group;
 	}
 
 	public boolean isDestinationASymLink() {
 		return to instanceof HDF5SymLink;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getPath() {
 		return path;
 	}
 
 	@Override
 	public String toString() {
 		return path + name + '\n' + to.toString();
 	}
 
 	public String getFullName() {
 		return path + name;
 	}
 }
