 package fr.loria.parole.artimate;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.logging.Logger;
 
 import com.ardor3d.extension.animation.skeletal.AnimationManager;
 import com.ardor3d.extension.animation.skeletal.blendtree.ClipSource;
 import com.ardor3d.extension.animation.skeletal.blendtree.SimpleAnimationApplier;
 import com.ardor3d.extension.animation.skeletal.clip.AbstractAnimationChannel;
 import com.ardor3d.extension.animation.skeletal.clip.AnimationClip;
 import com.ardor3d.extension.animation.skeletal.clip.AnimationClipInstance;
 import com.ardor3d.extension.animation.skeletal.clip.JointChannel;
 import com.ardor3d.extension.animation.skeletal.layer.AnimationLayer;
 import com.ardor3d.extension.animation.skeletal.state.ImmediateTransitionState;
 import com.ardor3d.extension.animation.skeletal.state.SteadyState;
 import com.ardor3d.extension.model.collada.jdom.data.ColladaStorage;
 import com.ardor3d.extension.model.collada.jdom.data.SkinData;
 
 import fr.loria.parole.artimate.data.Unit;
 import fr.loria.parole.artimate.data.UnitDB;
 import fr.loria.parole.artimate.data.UnitSequence;
 import fr.loria.parole.artimate.data.io.XWavesSegmentation;
 
 public class Animation {
 
 	private static final Logger logger = Logger.getLogger(Animation.class.getName());
 
 	private UnitDB unitDB;
 
 	private AnimationManager manager;
 
 	public Animation(AnimationManager manager) {
 		this.manager = manager;
 		// Add our "applier logic".
 		manager.setApplier(new SimpleAnimationApplier());
 	}
 
 	public void setupAnimations(ColladaStorage storage) {
 		// Check if there is any animationdata in the file
 		if (storage.getJointChannels().isEmpty() || storage.getSkins().isEmpty()) {
 			logger.warning("No animations found!");
 			return;
 		}
 
 		List<SkinData> skinDatas = storage.getSkins();
 
 		manager.addPose(skinDatas.get(0).getPose());
 
 		XWavesSegmentation segmentation = null;
 		try {
 			segmentation = new XWavesSegmentation("all.lab");
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		for (Unit segment : segmentation) {
 			final AnimationClip clip = new AnimationClip(segment.getLabel());
 
 			for (final JointChannel channel : storage.getJointChannels()) {
 				float start = (float) segment.getStart();
 				float end = (float) segment.getEnd();
 				JointChannel subChannel = (JointChannel) channel.getSubchannelByTime(start, end);
 				// add it to a clip
 				clip.addChannel(subChannel);
 			}
 
 			// Add the state directly to the unit in the DB
 			final SteadyState animState = new SteadyState(Integer.toString(segment.getIndex()));
 			animState.setSourceTree(new ClipSource(clip, manager));
 			segment.setAnimation(animState);
 		}
 		unitDB = new UnitDB(segmentation);
 	}
 
 	public void playSequence(UnitSequence targets) {
 		// create sequence of states
 		ArrayList<SteadyState> stateSequence = new ArrayList<SteadyState>();
 
 		ListIterator<Unit> units = targets.iterator();
 		while (units.hasNext()) {
 			Unit unit = units.next();
 
 			SteadyState state = copyAnimation(unit);
 			// add state to sequence
 			stateSequence.add(state);
 
 			// add end transition so that state jumps to next in sequence at end (except for last)
 			if (units.hasNext()) {
 				Unit nextUnit = targets.get(units.nextIndex());
 				String nextAnimationID = Integer.toString(nextUnit.getIndex());
 				state.setEndTransition(new ImmediateTransitionState(nextAnimationID));
 			}
 		}
 
 		// reset animation layer and add states
 		AnimationLayer layer = resetAnimationLayer();
 		for (SteadyState state : stateSequence) {
 			layer.addSteadyState(state);
 		}
 
 		// play animation layer by setting current to first state
 		layer.setCurrentState(stateSequence.get(0), true);
 	}
 
 	private SteadyState copyAnimation(Unit unit) {
 		// get animation state from base layer
		SteadyState baseState = (SteadyState) unitDB.getUnitList(unit.getLabel()).get(0).getAnimation();
 
 		// get clip source and clip from base layer
 		ClipSource oldClipSource = (ClipSource) baseState.getSourceTree();
 		AnimationClip oldClip = oldClipSource.getClip();
 
 		// create new clip and clip source
 		AnimationClip newClip = new AnimationClip(Integer.toString(unit.getIndex()));
 		for (AbstractAnimationChannel channel : oldClip.getChannels()) {
 			newClip.addChannel(channel);
 		}
 		ClipSource newClipSource = new ClipSource(newClip, manager);
 
 		// get clip instance for clip, which allows us to...
 		AnimationClipInstance newClipInstance = manager.getClipInstance(newClip);
 		// ...set the time scale
 		double requestedDuration = unit.getDuration();
 		float baseDuration = oldClip.getMaxTimeIndex();
 		double timeScale = requestedDuration / baseDuration;
 		newClipInstance.setTimeScale(timeScale);
 
 		// create new state using this clip source
 		SteadyState state = new SteadyState(Integer.toString(unit.getIndex()));
 		state.setSourceTree(newClipSource);
 
 		return state;
 	}
 
 	private AnimationLayer resetAnimationLayer() {
 		String layerName = "-ANIMATION_LAYER-";
 		// remove layer if it exists
 		AnimationLayer layer = manager.findAnimationLayer(layerName);
 		manager.removeAnimationLayer(layer);
 		// create new layer
 		layer = new AnimationLayer(layerName);
 		manager.addAnimationLayer(layer);
 		return layer;
 	}
 
 }
