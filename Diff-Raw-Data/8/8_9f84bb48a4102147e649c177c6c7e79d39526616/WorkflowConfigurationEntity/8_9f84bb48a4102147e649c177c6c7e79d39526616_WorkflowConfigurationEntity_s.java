 package com.sobek.workflow.entity;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Lob;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 
 import com.sobek.common.util.SystemProperties;
 import com.sobek.pgraph.Pgraph;
 
@Entity(name="WORKFLOW_CONFIGURATION")
 @NamedQueries({
		@NamedQuery(name=WorkflowConfigurationEntity.GET_CONFIG_BY_NAME, query="SELECT wfe FROM WorkflowConfigurationEntity wfe WHERE wfe.name = :name")
 	})
 public class WorkflowConfigurationEntity {
 	
 	public static final String GET_CONFIG_BY_NAME = "WorkflowConfigurationEntity.getConfigByName";
 	public static final String NAME_PARAMETER = "name";
 
 	@Id
 	@Column(name="NAME")
 	private String name = "";
 	
 	@Lob
 	@Column(name="PGRAPH")
 	private Pgraph pgraph;
 
 	@SuppressWarnings("unused")
 	private WorkflowConfigurationEntity() {
 		// Required by JPA
 	}
 
 	public WorkflowConfigurationEntity(String name, Pgraph pgraph) {
 		if(name == null || name.isEmpty()
 				|| pgraph == null)
 		{
 			throw new IllegalArgumentException(
 					"One or more invalid values were passed to the " +
 					this.getClass().getName() + " constructor.  The given values " +
 					"were:" + SystemProperties.NEW_LINE +
 					"Name (cannot be null or empty) : " + name + SystemProperties.NEW_LINE +
 					"Pgraph (cannot be null) : " + pgraph + SystemProperties.NEW_LINE);
 		}
 
 		this.name = name;
 		this.pgraph = pgraph;
 	}
 	
 	
 
 	public String getName() {
 		return this.name;
 	}
 
 	public Pgraph getPgraph() {
 		return this.pgraph;
 	}
 
 	public void setPgraph(Pgraph pgraph) {
 		if(pgraph != null) {
 			this.pgraph = pgraph;
 		}
 	}
 
 	
 }
