 package lang.marvol.backend;
 
 import java.util.ArrayList;
 
 import lang.marvol.backend.pos.NewPose;
 import lang.marvol.backend.pos.predefined.ArmPoses;
 import lang.marvol.backend.pos.predefined.ArmTwistPoses;
 import lang.marvol.backend.pos.predefined.ElbowPoses;
 import lang.marvol.backend.pos.predefined.HandPoses;
 import lang.marvol.backend.pos.predefined.HeadHorPoses;
 import lang.marvol.backend.pos.predefined.HeadVerPoses;
 import lang.marvol.backend.pos.predefined.LegPoses;
 
 import com.aldebaran.proxy.ALMotionProxy;
import com.aldebaran.proxy.ALTextToSpeechProxy;
 import com.aldebaran.proxy.Variant;
 
 public class DanceInterpreter {
 
 	static String addr = null;
 	static int curPos = 0;
 	static ALMotionProxy mp = null;
 	static Thread curThread = null;
 	static boolean shouldStop = false;
//	private static ALTextToSpeechProxy speech;
 
 	// This is required so that we can use the 'Variant' object
 	static {
 		System.loadLibrary("jnaoqi");
 	}
 
 	static void stiffnessOn(ALMotionProxy proxy) {
 		proxy.stiffnessInterpolation(new Variant(new String[] { "Body" }),
 				new Variant(new float[] { 1f }),
 				new Variant(new float[] { 1f }));
 	}
 
 	public static void init(String addr) {
 		mp = new ALMotionProxy(addr, 9559);
//		speech = new ALTextToSpeechProxy(addr, 9559);
//		speech.say("Let's dance!");
 		stiffnessOn(mp);
 
 		curPos = 0;
 	}
 
 	// gives back current position
 	public static void interrupt() throws InterruptedException {
 		if (curThread == null) {
 			return;
 		}
 		shouldStop = true;
 		curThread.join();
 		shouldStop = false;
 		curThread = null;
 		
 	}
 
 	public static void asyncDoMove(ArrayList<NewPose> p) {
 		if (curThread != null) {
 			return;
 		}
 		curThread = new Thread(new MoveRunnable(p));
 		curThread.start();
 	}
 
 	public static void main(String[] argv) {
 		// init("192.16.201.36");
 		init("127.0.0.1");
 		ArrayList<NewPose> p = new ArrayList<NewPose>();
 		NewPose i = NewPose.Init;
 
 		NewPose rightPoint = i.moveRightArm(ArmPoses.ForwardsUpSideWays)
 				.rightElbow(ElbowPoses.Stretch).rightHand(HandPoses.Open)
 				.look(HeadHorPoses.Right).chin(HeadVerPoses.Up)
 				.moveLeftArm(ArmPoses.ForwardsDownSideWays);
 
 		NewPose leftFirst = i.moveArms(ArmPoses.Sideways)
 				.elbows(ElbowPoses.Bend).twistArms(ArmTwistPoses.Inwards);
 
 		// NewPose leftFirst3 = leftFirst2.mirror();
 		NewPose maarten = i.moveArms(ArmPoses.Sideways);
 		p.add(i);
 		p.add(i.moveArms(ArmPoses.ForwardsDown).elbows(ElbowPoses.Bend)
 				.twistArms(ArmTwistPoses.FarInwards));
 
 		for (int z = 0; z < 3; z++) {
 			NewPose bla = i.moveLegs(LegPoses.HawaiiLeft)
 					.moveRightArm(ArmPoses.Sideways)
 					.twistRightArm(ArmTwistPoses.Outwards)
 					.rightHand(HandPoses.Open).look(HeadHorPoses.FarRight)
 					.moveLeftArm(ArmPoses.Forwards)
 					.twistLeftArm(ArmTwistPoses.Inwards)
 					.leftElbow(ElbowPoses.Bend);
 			for (int j = 0; j < 3; j++) {
 				p.add(i.moveLegs(LegPoses.LuckyLuke)
 						.moveArms(ArmPoses.ForwardsUp).hands(HandPoses.Open));
 				p.add(bla);
 				p.add(i.moveLegs(LegPoses.LuckyLuke)
 						.moveArms(ArmPoses.ForwardsUp).hands(HandPoses.Open));
 				p.add(bla.mirror());
 			}
 
 			for (int q = 0; q < 3; q++) {
 				NewPose leftFirst2 = i.moveArms(ArmPoses.Sideways)
 						.elbows(ElbowPoses.Bend)
 						.twistLeftArm(ArmTwistPoses.Outwards)
 						.leftHand(HandPoses.Open).look(HeadHorPoses.FarLeft)
 						.twistRightArm(ArmTwistPoses.FarInwards)
 						.moveLegs(LegPoses.HawaiiLeft);
 				p.add(leftFirst2);
 				p.add(leftFirst2.mirror());
 
 				// p.add(i.moveLegs(LegPoses.HawaiiRight));
 				// p.add(i.moveLegs(LegPoses.Squat));
 				// p.add(i.moveArms(ArmPoses.Down));
 				// p.add(i.moveLegs(LegPoses.Squat).moveArms(ArmPoses.Sideways));
 				// .add(i.moveLegs(LegPoses.Init));
 
 			}
 
 		}
 		for (int j = 0; j < 3; j++) {
 			NewPose bla = i.moveLegs(LegPoses.Squat)
 					.moveLeftArm(ArmPoses.ForwardsUp)
 					.moveRightArm(ArmPoses.ForwardsDown)
 					.elbows(ElbowPoses.Bend).chin(HeadVerPoses.Up)
 					.look(HeadHorPoses.Right);
 			p.add(bla);
 			NewPose fly = i.moveLeftArm(ArmPoses.SidewaysUp).moveRightArm(
 					ArmPoses.SidewaysDown);
 			p.add(fly);
 			p.add(bla.mirror());
 			p.add(fly.mirror());
 		}
 		NewPose rightHere = i.moveRightArm(ArmPoses.ForwardsDown)
 				.rightElbow(ElbowPoses.Bend).rightHand(HandPoses.Closed)
 				.look(HeadHorPoses.Right).chin(HeadVerPoses.Down)
 				.moveLeftArm(ArmPoses.Sideways).leftElbow(ElbowPoses.Bend);
 		for (int j = 0; j < 2; j++) {
 			p.add(rightHere);
 			p.add(rightHere.mirror());
 		}
 
 		asyncDoMove(p);
 		try {
 			curThread.join();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
