 package com.evervoid.client.views.galaxy;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import com.evervoid.client.graphics.geometry.Transform;
 import com.evervoid.client.views.Bounds;
 import com.evervoid.client.views.game.GameView;
 import com.evervoid.client.views.game.MiniView;
 import com.evervoid.state.Galaxy;
 import com.evervoid.state.SolarSystem;
 import com.evervoid.state.Wormhole;
 import com.evervoid.state.geometry.Point;
 import com.evervoid.state.geometry.Point3D;
 
 class MiniGalaxyView extends MiniView
 {
 	private final Map<UISolarSystem, Transform> aSolarSystems;
 	private final Set<UIWormhole> aWormholes;
 	private final float maxSize;
 
 	MiniGalaxyView(final GameView gameview, final Galaxy galaxy)
 	{
 		super(gameview);
 		aSolarSystems = new HashMap<UISolarSystem, Transform>();
 		aWormholes = new HashSet<UIWormhole>();
		maxSize = galaxy.getSize();
 		// NOTE, add wormholes first so they appear in the solar system and not on top of them
 		for (final Wormhole w : galaxy.getWormholes()) {
 			addWormhole(new UIWormhole(w, 1f));
 		}
 		for (final SolarSystem ss : galaxy.getSolarSystems()) {
 			add(new UISolarSystem(ss));
 		}
 	}
 
 	private void add(final UISolarSystem ss)
 	{
 		super.addNode(ss);
 		aSolarSystems.put(ss, ss.getNewTransform());
 	}
 
 	private void addWormhole(final UIWormhole w)
 	{
 		super.addNode(w);
 		aWormholes.add(w);
 	}
 
 	public void rescale()
 	{
		// FIXME .8f shouldn't be here. Understand the bug, beat it.
 		// calculate center of bounds and new scale
 		final Bounds bounds = getBounds();
		final float scale = .8f * (bounds.width < bounds.height ? (float) bounds.width : (float) bounds.height) / maxSize;
 		final Point center = new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
 		// for each solar system
 		for (final UISolarSystem ss : aSolarSystems.keySet()) {
 			// translate and scale accordingly
 			final Point3D point = ss.getPoint().scale(scale);
 			final Transform t = aSolarSystems.get(ss);
 			t.translate(center.x + point.x, center.y + point.y);
 			t.setScale(scale * ss.getSize());
 		}
 		for (final UIWormhole w : aWormholes) {
 			final Transform t = w.getOriginalTransform();
 			// set new scale, move to middle
 			t.setScale(scale);
 			t.translate(center.x, center.y);
 		}
 	}
 
 	@Override
 	public void setBounds(final Bounds bounds)
 	{
 		super.setBounds(bounds);
 		rescale();
 	}
 }
