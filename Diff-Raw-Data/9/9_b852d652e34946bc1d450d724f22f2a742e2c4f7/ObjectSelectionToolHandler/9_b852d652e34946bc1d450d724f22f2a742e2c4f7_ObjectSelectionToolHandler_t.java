 package net.untoldwind.moredread.ui.tools.selection;
 
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.List;
 
 import net.untoldwind.moredread.annotations.Singleton;
 import net.untoldwind.moredread.model.scene.AbstractSpatialNode;
 import net.untoldwind.moredread.model.scene.INode;
import net.untoldwind.moredread.model.scene.ISpatialNode;
 import net.untoldwind.moredread.model.scene.Scene;
 import net.untoldwind.moredread.ui.controls.IModelControl;
 import net.untoldwind.moredread.ui.controls.IViewport;
 import net.untoldwind.moredread.ui.controls.Modifier;
 import net.untoldwind.moredread.ui.controls.impl.FullScreenModelControl;
 import net.untoldwind.moredread.ui.controls.impl.MoveCrossModelControl;
 import net.untoldwind.moredread.ui.tools.IToolController;
 import net.untoldwind.moredread.ui.tools.spi.IToolAdapter;
 import net.untoldwind.moredread.ui.tools.spi.IToolHandler;
 
 import com.jme.math.Vector2f;
 import com.jme.math.Vector3f;
 
 @Singleton
 public class ObjectSelectionToolHandler implements IToolHandler {
 	@Override
 	public void activated(final IToolController toolController,
 			final Scene scene) {
 	}
 
 	@Override
 	public void aborted() {
 	}
 
 	@Override
 	public List<? extends IModelControl> getModelControls(final Scene scene,
 			final IViewport viewport) {
 		final List<IModelControl> controls = new ArrayList<IModelControl>();
 		for (final INode node : scene.getSceneSelection().getSelectedNodes()) {
 			if (node instanceof AbstractSpatialNode) {
 				controls.add(new MoveCrossModelControl(
 						new TransformToolAdapter(scene)));
 				break;
 			}
 		}
 		controls.add(new FullScreenModelControl(new SelectToolAdapter(scene,
 				viewport)));
 
 		return controls;
 	}
 
 	private static class SelectToolAdapter implements IToolAdapter {
 		private final Scene scene;
 		private final IViewport viewport;
 
 		private SelectToolAdapter(final Scene scene, final IViewport viewport) {
 			this.scene = scene;
 			this.viewport = viewport;
 		}
 
 		@Override
 		public Vector3f getCenter() {
 			return new Vector3f(0, 0, 0);
 		}
 
 		@Override
 		public boolean handleMove(final IModelControl modelControl,
 				final Vector3f point, final EnumSet<Modifier> modifiers) {
 			return false;
 		}
 
 		@Override
 		public boolean handleClick(final IModelControl modelControl,
 				final Vector3f point, final EnumSet<Modifier> modifiers) {
 			if (modifiers.contains(Modifier.LEFT_MOUSE_BUTTON)) {
 				final INode node = viewport.pickNode(new Vector2f(point.x,
 						point.y));
 
 				if (node != null) {
 					if (modifiers.contains(Modifier.SHIFT_KEY)) {
 						scene.getSceneSelection().switchSelectedNode(node);
 					} else {
 						scene.getSceneSelection().setSelectedNode(node);
 					}
 				}
 				return true;
 			}
 			return false;
 		}
 
 		@Override
 		public boolean handleDragStart(final IModelControl modelControl,
 				final Vector3f point, final EnumSet<Modifier> modifiers) {
 			return false;
 		}
 
 		@Override
 		public boolean handleDragMove(final IModelControl modelControl,
 				final Vector3f point, final EnumSet<Modifier> modifiers) {
 			return false;
 		}
 
 		@Override
 		public boolean handleDragEnd(final IModelControl modelControl,
 				final Vector3f point, final EnumSet<Modifier> modifiers) {
 			return false;
 		}
 	}
 
 	private static class TransformToolAdapter implements IToolAdapter {
 		private final Scene scene;
 
 		private TransformToolAdapter(final Scene scene) {
 			this.scene = scene;
 		}
 
 		@Override
 		public Vector3f getCenter() {
 			final Vector3f center = new Vector3f();
 			int count = 0;
 			for (final INode node : scene.getSceneSelection()
 					.getSelectedNodes()) {
				if (node instanceof ISpatialNode) {
					final ISpatialNode spatialNode = (AbstractSpatialNode) node;
					center.addLocal(spatialNode.getWorldBoundingBox()
							.getCenter());
 					count++;
 				}
 			}
 			if (count > 0) {
 				center.divideLocal(count);
 			}
 
 			return center;
 		}
 
 		@Override
 		public boolean handleMove(final IModelControl modelControl,
 				final Vector3f point, final EnumSet<Modifier> modifiers) {
 			return false;
 		}
 
 		@Override
 		public boolean handleClick(final IModelControl modelControl,
 				final Vector3f point, final EnumSet<Modifier> modifiers) {
 			return false;
 		}
 
 		@Override
 		public boolean handleDragStart(final IModelControl modelControl,
 				final Vector3f point, final EnumSet<Modifier> modifiers) {
 			return false;
 		}
 
 		@Override
 		public boolean handleDragMove(final IModelControl modelControl,
 				final Vector3f point, final EnumSet<Modifier> modifiers) {
 			final Vector3f midCenter = getCenter();
 
 			scene.getSceneChangeHandler().begin(true);
 
 			try {
 				updateScene(point, midCenter);
 			} finally {
 				scene.getSceneChangeHandler().savepoint();
 			}
 			return true;
 		}
 
 		@Override
 		public boolean handleDragEnd(final IModelControl modelControl,
 				final Vector3f point, final EnumSet<Modifier> modifiers) {
 			final Vector3f midCenter = getCenter();
 
 			scene.getSceneChangeHandler().begin(true);
 
 			try {
 				updateScene(point, midCenter);
 			} finally {
 				scene.getSceneChangeHandler().commit();
 			}
 			return true;
 		}
 
 		private void updateScene(final Vector3f point, final Vector3f midCenter) {
 			final List<INode> changedNodes = new ArrayList<INode>();
 
 			for (final INode node : scene.getSceneSelection()
 					.getSelectedNodes()) {
 				if (node instanceof AbstractSpatialNode) {
 					final AbstractSpatialNode spatialNode = (AbstractSpatialNode) node;
 					final Vector3f centerDiff = spatialNode
 							.getWorldTranslation().subtract(midCenter);
 
 					Vector3f localCenter;
 					if (node.getParent() != null) {
 						localCenter = spatialNode.getParent().worldToLocal(
 								point.add(centerDiff), new Vector3f());
 					} else {
 						localCenter = point.add(centerDiff);
 					}
 
 					spatialNode.setLocalTranslation(localCenter);
 					changedNodes.add(spatialNode);
 				}
 			}
 		}
 	}
 }
