 /**
 *
  */
 package org.wings.adapter;
 
 import org.wings.IntegrationFrame;
 import org.wings.Resource;
 import org.wings.conf.Integration;
 import org.wings.resource.DynamicResource;
 import org.wings.resource.ReloadResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 
 /**
  * <code>AbstractIntegrationAdapter</code>.
 *
  * <pre>
  * Date: Jul 25, 2008
  * Time: 6:36:21 PM
  * </pre>
  *
  * @author <a href="mailto:Roman.Raedle@uni-konstanz.de">Roman R&auml;dle</a>
  * @version $Id
  */
 public abstract class AbstractIntegrationAdapter implements IntegrationAdapter {
 
    private final transient static Log LOG = LogFactory.getLog(AbstractIntegrationAdapter.class);

    protected IntegrationFrame frame;

 	protected Integration integration;

 	private DynamicResource defaultResource;

 	/**
 	 * @param frame
 	 * @param integration
 	 */
 	public AbstractIntegrationAdapter(IntegrationFrame frame, Integration integration) {
 		this.frame = frame;
 		this.integration = integration;

 		defaultResource = frame.getDynamicResource(ReloadResource.class);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.wings.adapter.IntegrationAdapter#setFrame(org.wings.SFrame)
 	 */
 	public void setFrame(IntegrationFrame frame) {
 		this.frame = frame;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.wings.adapter.IntegrationAdapter#setIntegration(org.wings.conf.Integration)
 	 */
 	public void setIntegration(Integration integration) {
 		this.integration = integration;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.wings.session.ResourceMapper#mapResource(java.lang.String)
 	 */
 	public Resource mapResource(String url) {
 		try {
 			navigate(url);
		} catch (Exception e) {
            LOG.error("Cannot resolve \"" + url+ "\"");
 		}
 		return defaultResource;
 	}

 	/**
 	 * @param url
 	 */
 	protected abstract void navigate(String url) throws Exception;
 }
