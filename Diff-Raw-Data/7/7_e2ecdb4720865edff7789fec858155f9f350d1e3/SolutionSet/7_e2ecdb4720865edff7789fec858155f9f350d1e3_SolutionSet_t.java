 /**
  * Copyright 2012 George Belden
  * 
  * This file is part of ZodiacEngine.
  * 
  * ZodiacEngine is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * ZodiacEngine is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * ZodiacEngine. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.ciphertool.zodiacengine.entities;
 
 import java.io.Serializable;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "solution_set")
 public class SolutionSet implements Serializable {
 
 	private static final long serialVersionUID = 2434992350084225015L;
 
 	private Integer id;
 	private String name;
 	private transient Set<Solution> solutions;
 
 	public SolutionSet() {
 	}
 
 	public SolutionSet(int id) {
 		this.id = id;
 	}
 
	public SolutionSet(String name) {
		this.name = name;
	}

 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(name = "id")
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	/**
 	 * @return the name
 	 */
 	@Column(name = "name")
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param name
 	 *            the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	@OneToMany(fetch = FetchType.LAZY, mappedBy = "id.solutionSet", cascade = CascadeType.ALL)
 	public Set<Solution> getSolutions() {
 		if (this.solutions == null) {
 			this.solutions = new HashSet<Solution>();
 		}
 
 		return this.solutions;
 	}
 
 	public void setSolutions(Set<Solution> solutions) {
 		this.solutions = solutions;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		SolutionSet other = (SolutionSet) obj;
 		if (id == null) {
 			if (other.id != null) {
 				return false;
 			}
 		} else if (!id.equals(other.id)) {
 			return false;
 		}
 		if (name == null) {
 			if (other.name != null) {
 				return false;
 			}
 		} else if (!name.equals(other.name)) {
 			return false;
 		}
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "SolutionSet [id=" + id + ", name=" + name + "]";
 	}
 }
