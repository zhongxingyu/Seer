 package org.jboss.test.deployers.deployer.support;
 
 import java.util.Set;
 
 import org.jboss.deployers.spi.DeploymentException;
 import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
 import org.jboss.deployers.spi.deployer.managed.ManagedDeploymentCreator;
 import org.jboss.deployers.structure.spi.DeploymentUnit;
 import org.jboss.managed.api.ManagedDeployment;
 
 public class MCFDeployer
    extends AbstractSimpleRealDeployer<DSMetaData>
   implements ManagedDeploymentCreator
 
 {
    public MCFDeployer(Class<DSMetaData> input)
    {
       super(input);
    }
 
    @Override
    public void deploy(DeploymentUnit unit, DSMetaData deployment)
       throws DeploymentException
    {
    }
 
   public void build(DeploymentUnit unit, ManagedDeployment md)
    {      
    }
 
 }
