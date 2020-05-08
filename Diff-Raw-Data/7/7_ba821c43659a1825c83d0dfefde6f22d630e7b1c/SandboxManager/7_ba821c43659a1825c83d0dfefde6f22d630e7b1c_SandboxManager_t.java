 package swarm.client.view.sandbox;
 
 import swarm.client.view.ViewContext;
 import swarm.client.view.tabs.code.I_CodeLoadListener;
 import swarm.shared.debugging.U_Debug;
 import swarm.shared.entities.E_CodeSafetyLevel;
 import swarm.shared.structs.Code;
 
 import com.google.gwt.user.client.Element;
 
 public class SandboxManager
 {
 	public interface I_StartUpCallback
 	{
 		void onStartUpComplete(boolean success);
 	}
 	
 	private final CajaSandboxManager m_cajaSandboxMngr;
 	private final InlineFrameSandboxManager m_iframeSandboxMngr;
 	
 	private final CellApi m_cellApi;
 	
 	public SandboxManager(ViewContext viewContext, I_StartUpCallback callback, String apiNamespace, boolean useVirtualSandbox)
 	{
 		m_cellApi = new CellApi(viewContext);
 		m_cellApi.registerApi(apiNamespace);
 		
 		m_iframeSandboxMngr = new InlineFrameSandboxManager(apiNamespace);
 		m_cajaSandboxMngr = new CajaSandboxManager(m_cellApi, callback, apiNamespace, useVirtualSandbox);
 	}
 	
 	public void allowScrolling(Element element, boolean yesOrNo)
 	{
 		if( m_cajaSandboxMngr != null )
 		{
 			m_cajaSandboxMngr.allowScrolling(element, yesOrNo);
 		}
 	}
 	
 	public void start(Element host, Code code, String cellNamespace, I_CodeLoadListener listener)
 	{
 		E_CodeSafetyLevel codeLevel = code.getSafetyLevel();
 		//codeLevel = codeLevel == smE_CodeSafetyLevel.VIRTUAL_DYNAMIC_SANDBOX ? smE_CodeSafetyLevel.LOCAL_SANDBOX : codeLevel;
 		
 		switch(codeLevel)
 		{
 			case NO_SANDBOX_STATIC:
 			case NO_SANDBOX_DYNAMIC:
 			{
 				if( m_cajaSandboxMngr != null )
 				{
 					m_cajaSandboxMngr.stop(host);
 				}
 				
 				m_iframeSandboxMngr.stop(host);
 				
 				host.setInnerHTML(code.getRawCode());
 				listener.onCodeLoad();
 				
 				break;
 			}
 			case LOCAL_SANDBOX:
 			{
 				if( m_cajaSandboxMngr != null )
 				{
 					m_cajaSandboxMngr.stop(host);
 				}
 				
				host.setInnerHTML("");
				
 				m_iframeSandboxMngr.start_local(host, code.getRawCode(), listener);  break;
 			}
 			case REMOTE_SANDBOX:
 			{
 				if( m_cajaSandboxMngr != null )
 				{
 					m_cajaSandboxMngr.stop(host);
 				}
 				
				host.setInnerHTML("");
				
 				m_iframeSandboxMngr.start_remote(host, code.getRawCode(), listener);  break;
 			}
 			case VIRTUAL_DYNAMIC_SANDBOX:
 			{
 				m_iframeSandboxMngr.stop(host);
 				
 				if( m_cajaSandboxMngr != null )
 				{
 					m_cajaSandboxMngr.start_dynamic(host, code.getRawCode(), cellNamespace, listener);
 				}
 				
 				break;
 			}
			
 			case VIRTUAL_STATIC_SANDBOX:
 			{
 				m_iframeSandboxMngr.stop(host);
 				
 				if( m_cajaSandboxMngr != null )
 				{
 					m_cajaSandboxMngr.start_static(host, code.getRawCode(), cellNamespace, listener);
 				}
 					
 				break;
 			}
 		}
 	}
 	
 	/*public boolean isRunning()
 	{
 		return m_cajaSandboxMngr.isRunning() || m_iframeSandboxMngr.isRunning();
 	}*/
 	
 	public void stop(Element host)
 	{
 		if( m_cajaSandboxMngr != null )
 		{
 			m_cajaSandboxMngr.stop(host);
 		}
 		
 		m_iframeSandboxMngr.stop(host);
 	}
 }
