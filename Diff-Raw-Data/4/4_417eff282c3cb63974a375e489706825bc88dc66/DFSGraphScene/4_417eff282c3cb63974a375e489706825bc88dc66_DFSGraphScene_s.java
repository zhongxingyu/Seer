 package imy.lnu.ai.dfs;
 
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import javax.swing.AbstractAction;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import org.netbeans.api.visual.action.*;
 import org.netbeans.api.visual.anchor.AnchorFactory;
 import org.netbeans.api.visual.anchor.AnchorShape;
 import org.netbeans.api.visual.anchor.PointShape;
 import org.netbeans.api.visual.graph.GraphScene;
 import org.netbeans.api.visual.router.RouterFactory;
 import org.netbeans.api.visual.vmd.VMDConnectionWidget;
 import org.netbeans.api.visual.vmd.VMDNodeWidget;
 import org.netbeans.api.visual.widget.*;
 
 /**
  *
  * @author mcangel
  */
 public class DFSGraphScene extends GraphScene<String, Integer>
 {
 	private LayerWidget mainLayer = new LayerWidget(this);
 	private LayerWidget connectionLayer = new LayerWidget(this);
 	private LayerWidget interractionLayer = new LayerWidget(this);
 	private int edgeCounter = 0;
 	private WidgetAction connectAction = ActionFactory.createConnectAction(interractionLayer, new SceneConnectProvider());
 	private WidgetAction reconnetAction = ActionFactory.createReconnectAction(new SceneReconnectProvider());
 
 	public DFSGraphScene()
 	{
 		this.addChild(mainLayer);
 		this.addChild(connectionLayer);
 		this.addChild(interractionLayer);
 		
 		getActions().addAction(ActionFactory.createPopupMenuAction(new PopupMenuProvider() {
 
 			@Override
			public JPopupMenu getPopupMenu(Widget widget, Point localLocation)
 			{
 				final Point localPoint = localLocation;
 				JPopupMenu menu = new JPopupMenu("Menu");
 				JMenuItem createPlaceMenuItem = new JMenuItem(new AbstractAction() {
 
 					@Override
 					public void actionPerformed(ActionEvent e)
 					{
 						addNode("New place "+(new java.util.Date()).getTime()).setPreferredLocation(localPoint);
 					}
 				});
 				createPlaceMenuItem.setText("Create place");
 				menu.add(createPlaceMenuItem);
 				return menu;
 			}
 		}));
 	}
 
 	@Override
 	protected Widget attachNodeWidget(String node)
 	{
 		VMDNodeWidget widget = new VMDNodeWidget(this);
 		widget.setNodeName(node);
 		LabelWidget lw = new LabelWidget(this);
 		widget.addChild(lw);
 		lw.getActions().addAction(connectAction);
 		mainLayer.addChild(widget);
 		
 		widget.getActions().addAction(createSelectAction());
 		widget.getActions().addAction(ActionFactory.createMoveAction());
 		
 		return widget;
 	}
 
 	@Override
 	protected Widget attachEdgeWidget(Integer edge)
 	{
 		VMDConnectionWidget connection = new VMDConnectionWidget(this,
 				RouterFactory.createOrthogonalSearchRouter(mainLayer, connectionLayer));
 		connection.setTargetAnchorShape(AnchorShape.NONE);
 		connection.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
 
 		connection.getActions().addAction(createObjectHoverAction());
 		connection.getActions().addAction(createSelectAction());
 		connection.getActions().addAction(reconnetAction);
 		//connection.getActions().addAction(ActionFactory.createOrthogonalMoveControlPointAction());
 
 		connectionLayer.addChild(connection);
 		return connection;
 	}
 
 	@Override
 	protected void attachEdgeSourceAnchor(Integer edge, String oldSourceNode, String sourceNode)
 	{
 		Widget w = sourceNode != null ? findWidget(sourceNode) : null;
 		((ConnectionWidget) findWidget(edge)).setSourceAnchor(AnchorFactory.createRectangularAnchor(w));
 	}
 
 	@Override
 	protected void attachEdgeTargetAnchor(Integer edge, String oldTargetNode, String targetNode)
 	{
 		Widget w = targetNode != null ? findWidget(targetNode) : null;
 		((ConnectionWidget) findWidget(edge)).setTargetAnchor(AnchorFactory.createRectangularAnchor(w));
 	}
 //TODO: add connection weight prompter
 	private class SceneConnectProvider implements ConnectProvider
 	{
 		private String source = null;
 		private String target = null;
 
 		@Override
 		public boolean isSourceWidget(Widget sourceWidget)
 		{
 			Object object = findObject(sourceWidget);
 			source = isObject(object) ? (String) object : null;
 			return source != null;
 		}
 
 		@Override
 		public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget)
 		{
 			Object object = findObject(targetWidget);
 			target = isObject(object) ? (String) object : null;
 			if (target != null)
 			{
 				return !source.equals(target) ? ConnectorState.ACCEPT : ConnectorState.REJECT_AND_STOP;
 			}
 			return object != null ? ConnectorState.REJECT_AND_STOP : ConnectorState.REJECT;
 		}
 
 		@Override
 		public boolean hasCustomTargetWidgetResolver(Scene scene)
 		{
 			return false;
 		}
 
 		@Override
 		public Widget resolveTargetWidget(Scene scene, Point sceneLocation)
 		{
 			return null;
 		}
 
 		@Override
 		public void createConnection(Widget sourceWidget, Widget targetWidget)
 		{
 			Integer edge = new Integer(edgeCounter++);
 			addEdge(edge);
 			setEdgeSource(edge, source);
 			setEdgeTarget(edge, target);
 		}
 	}
 
 	private class SceneReconnectProvider implements ReconnectProvider
 	{
 		Integer edge;
 		String originalNode;
 		String replacementNode;
 
 		@Override
 		public void reconnectingStarted(ConnectionWidget connectionWidget, boolean reconnectingSource)
 		{
 		}
 
 		@Override
 		public void reconnectingFinished(ConnectionWidget connectionWidget, boolean reconnectingSource)
 		{
 		}
 
 		@Override
 		public boolean isSourceReconnectable(ConnectionWidget connectionWidget)
 		{
 			Object object = findObject(connectionWidget);
 			edge = isEdge(object) ? (Integer) object : null;
 			originalNode = edge != null ? getEdgeSource(edge) : null;
 			return originalNode != null;
 		}
 
 		@Override
 		public boolean isTargetReconnectable(ConnectionWidget connectionWidget)
 		{
 			Object object = findObject(connectionWidget);
 			edge = isEdge(object) ? (Integer) object : null;
 			originalNode = edge != null ? getEdgeTarget(edge) : null;
 			return originalNode != null;
 		}
 
 		@Override
 		public ConnectorState isReplacementWidget(ConnectionWidget connectionWidget, Widget replacementWidget, boolean reconnectingSource)
 		{
 			Object object = findObject(replacementWidget);
 			replacementNode = isObject(object) ? (String) object : null;
 			if (replacementNode != null)
 			{
 				return ConnectorState.ACCEPT;
 			}
 			return object != null ? ConnectorState.REJECT_AND_STOP : ConnectorState.REJECT;
 		}
 
 		@Override
 		public boolean hasCustomReplacementWidgetResolver(Scene scene)
 		{
 			return false;
 		}
 
 		@Override
 		public Widget resolveReplacementWidget(Scene scene, Point sceneLocation)
 		{
 			return null;
 		}
 
 		@Override
 		public void reconnect(ConnectionWidget connectionWidget, Widget replacementWidget, boolean reconnectingSource)
 		{
 			if (replacementWidget == null)
 			{
 				removeEdge(edge);
 			} else if (reconnectingSource)
 			{
 				setEdgeSource(edge, replacementNode);
 			} else
 			{
 				setEdgeTarget(edge, replacementNode);
 			}
 		}
 	}
 }
