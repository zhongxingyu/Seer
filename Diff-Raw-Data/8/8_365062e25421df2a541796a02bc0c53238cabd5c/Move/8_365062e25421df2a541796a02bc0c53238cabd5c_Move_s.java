 package cubetech.common;
 
 import cubetech.Game.Game;
 import cubetech.collision.CollisionResult;
 import cubetech.entities.Event;
 import cubetech.misc.Ref;
 
 import org.lwjgl.util.vector.Vector2f;
 
 /**
  *
  * @author mads
  */
 public class Move {
     // Constants loaded
     private static float accel = 8f;
     private static float friction = 4f;
     private static float spectator_friction = 2f;
     private static float stopspeed = 15f;
     private static float gravity;
     private static float pullacceleration;
     private static int speed = 100;
     private static float jumpvel = 100;
     private static int jumpmsec = 250;
     private static float stepheight = 10;
     private static int movemode = 1;
 
     private static int pull1 = 100;
     private static int pull2 = 200;
     private static int pull3 = 300;
     private static int pull4 = 400;
     private static int pull5 = 500;
     private static int pull6 = 600;
     private static float pullstep = 0.75f;
 
     private static Vector2f org_origin = new Vector2f();
     private static Vector2f org_velocity = new Vector2f();
 
     private static int msec;
     private static float frametime;
 
     public class MoveType {
         public static final int NORMAL = 0;
         public static final int NOCLIP = 1;
         public static final int SPECTATOR = 2;
         public static final int DEAD = 3;
         public static final int EDITMODE = 4;
     }
 
     public static void Move(MoveQuery query) {
         int finaltime = query.cmd.serverTime;
 
         if(finaltime < query.ps.commandTime)
             return; // shouldn't happen
 
         if(finaltime > query.ps.commandTime + 1000)
             query.ps.commandTime = finaltime - 1000;
 
         try {
             gravity = Ref.cvars.Find("sv_gravity").fValue;
             speed = Ref.cvars.Find("sv_speed").iValue;
             jumpvel = Ref.cvars.Find("sv_jumpvel").fValue;
             jumpmsec = Ref.cvars.Find("sv_jumpmsec").iValue;
             accel = Ref.cvars.Find("sv_acceleration").fValue;
             friction = Ref.cvars.Find("sv_friction").fValue;
             stopspeed = Ref.cvars.Find("sv_stopspeed").fValue;
             pullacceleration = Ref.cvars.Find("sv_pullacceleration").fValue;
             stepheight = Ref.cvars.Find("sv_stepheight").fValue;
             movemode = Ref.cvars.Find("sv_movemode").iValue;
             pull1 = Ref.cvars.Find("sv_pull1").iValue;
             pull2 = Ref.cvars.Find("sv_pull2").iValue;
             pull3 = Ref.cvars.Find("sv_pull3").iValue;
             pull4 = Ref.cvars.Find("sv_pull4").iValue;
             pull5 = Ref.cvars.Find("sv_pull5").iValue;
             pull6 = Ref.cvars.Find("sv_pull6").iValue;
             pullstep = Ref.cvars.Find("sv_pullstep").fValue;
 
         } catch(Exception ex) {
             
         }
 
         Vector2f start = new Vector2f(query.ps.origin);
         // chop the move up if it is too long, to prevent framerate
 	// dependent behavior
         int totalMsec = finaltime - query.ps.commandTime;
         while(query.ps.commandTime < finaltime) {
             int stepMsec = finaltime - query.ps.commandTime;
             if(stepMsec > 66)
                 stepMsec = 66;
             query.cmd.serverTime = query.ps.commandTime + stepMsec;
             MoveSingle(query);
         }
         Vector2f end = new Vector2f(query.ps.origin);
 
         Vector2f.sub(start, end, start);
         float movelen = start.length() * totalMsec;
         query.ps.movetime += (int)movelen;
     }
 
     
 
     private static void MoveSingle(MoveQuery query) {
         // determine the time
         msec = query.cmd.serverTime - query.ps.commandTime;
         if(msec < 1)
             msec = 1;
         if(msec > 200)
             msec = 200;
         query.ps.commandTime = query.cmd.serverTime;
         frametime = msec * 0.001f;
         DropTimers(query); // Decrement timers
         query.ps.UpdateViewAngle(query.cmd);
 
         if(query.ps.moveType == MoveType.DEAD)
             return; // Dead players can't move
 
         
         
         // Direction player wants to move
         Vector2f wishdir = new Vector2f();
         // Handle horizontal wish direction
         if(!query.ps.applyPull ) {
             if(query.cmd.Left)
                 wishdir.x -= 1f;
             if(query.cmd.Right)
                 wishdir.x += 1f;
         }
 //        else {
 //            wishdir.x = 1f;
 //        }
 
         // let player move up and down if not running in normal movement mode
         if(query.ps.moveType != MoveType.NORMAL) {
             if(query.cmd.Up)
                 wishdir.y += 1f;
             if(query.cmd.Down)
                 wishdir.y -= 1f;
         }
 
         query.onGround = isOnGround(query);
         if(query.onGround && query.ps.powerups[0] > 0 && !query.ps.jumpDown)
             query.ps.canDoubleJump = true;
 
 //        float len = wishdir.length();
         if(wishdir.length() != 0f) {
             wishdir.normalise();
 
             if(query.onGround && (query.groundPlane.x != 0f && query.groundPlane.y != 0f)) {
                 // project wishdir along ground normal
                 float xloss = wishdir.x;
                 ClipVelocity(wishdir, wishdir, query.groundPlane, 1.001f, query);
                 query.blocked = 0;
                 xloss -= wishdir.x;
                 wishdir.scale(speed * (1+(xloss)));
             } else
                 wishdir.scale(speed);
         }
 
 
 //        float len = (float)Math.sqrt(wishdir.x * wishdir.x + wishdir.y * wishdir.y);
 //        if(len != 0f) {
 //            wishdir.x /= len;
 //            wishdir.y /= len;
 //        }
         
 
         
 
         boolean ignoreGravity = false;
         if(query.cmd.Up && !query.onGround && query.ps.moveType == MoveType.NORMAL && query.ps.jumpTime > 0) {
             ignoreGravity = true;
         }
 
         if(!query.onGround && query.ps.moveType == MoveType.NORMAL && !ignoreGravity) {
             query.ps.velocity.y -= gravity * frametime * 0.5f;
         }
 
         
         if( query.ps.moveType == MoveType.NORMAL) {
             if(query.ps.jumpDown && !query.cmd.Up)
                 query.ps.jumpDown = false;
             
             if(query.cmd.Up && !query.ps.jumpDown) {
                 if((!query.onGround && query.ps.canDoubleJump)) {
                     Jump(query);
                     query.ps.canDoubleJump = false;
                     query.ps.jumpDown = true;
                 } else if(query.onGround) {
                     Jump(query);
                     query.ps.canDoubleJump = query.ps.powerups[0] > 0;
                     query.ps.jumpDown = true;
                 }
                 
             }
 
 //            if(query.cmd.Up &&  & !query.ps.jumpDown) {
 //
 //            }
         }
 
         
 
         if(query.ps.moveType == MoveType.NORMAL)
             UpdateStepSound(query);
 
         if(query.ps.moveType == MoveType.NORMAL && 
                 ((movemode == 1 && query.ps.applyPull) || (movemode == 2))) {
             FrictionSpecial(query);
         }
 
         if((query.onGround && !query.ps.applyPull) || query.ps.moveType != MoveType.NORMAL) {
 //            if(movemode == 1)
 //                FrictionSpecial(query);
 //            else
                 Friction(query);
         }
 
         
 
         WalkMove(wishdir, query);
 
         if(!query.onGround && query.ps.moveType == MoveType.NORMAL && !ignoreGravity) {
             query.ps.velocity.y -= gravity * frametime * 0.5f;
         }
     }
 
     static void Jump(MoveQuery pm) {
         pm.ps.jumpTime = jumpmsec;
         pm.ps.velocity.y = jumpvel;
         AddEvent(pm, Event.JUMP, 0);
     }
 
     static boolean isOnGround(MoveQuery pm) {
         Vector2f end = new Vector2f(pm.ps.origin);
         end.y -= 1f;
         CollisionResult trace = pm.Trace(pm.ps.origin, end, Game.PlayerMins, Game.PlayerMaxs, pm.tracemask, pm.ps.clientNum);
         if(trace.frac < 1f) {
             pm.groundPlane = trace.HitAxis;
             return true;
         }
         return false;
     }
 
     static void Friction(MoveQuery pm) {
 
        float speed2 = (float)Math.sqrt(pm.ps.velocity.x * pm.ps.velocity.x + pm.ps.velocity.y * pm.ps.velocity.y);
        if(speed2 < 0.1f)
            return;
 
        float fric = pm.ps.applyPull ? friction : 4;
 
        if(pm.ps.moveType == MoveType.SPECTATOR)
            fric = spectator_friction;
 
        float control = (speed2 < stopspeed ? stopspeed : speed2);
        float drop = control * fric * frametime;
 
        float newspeed = speed2 - drop;
        if(newspeed < 0)
            newspeed = 0;
 
        newspeed /= speed2;
 
        pm.ps.velocity.x *= newspeed;
        pm.ps.velocity.y *= newspeed;
    }
 
     
     static void FrictionSpecial(MoveQuery pm) {
         if(!pm.cmd.Left)
             return;
         
        float speed2 = (float)Math.sqrt(pm.ps.velocity.x * pm.ps.velocity.x);
        if(speed2 < 0.1f)
            return;
 
        float fric = friction;
 
        float control = speed2;
        float drop = control * fric * frametime;
 
        float newspeed = speed2 - drop;
        if(newspeed < 0)
            newspeed = 0;
 
        newspeed /= speed2;
 
        pm.ps.velocity.x *= newspeed;
 //       pm.ps.velocity.y *= newspeed;
    }
 
     static void WalkMove(Vector2f wishdir, MoveQuery pm) {
        // normalize
        float wishspeed = (float)Math.sqrt(wishdir.x * wishdir.x + wishdir.y * wishdir.y);
        if(wishspeed > 0) {
            wishdir.x /= wishspeed;
            wishdir.y /= wishspeed;
        }
        int maxspeed = speed;
        if(wishspeed > maxspeed)
        {
            wishdir.x *= (maxspeed/wishspeed);
            wishdir.y *= (maxspeed/wishspeed);
            wishspeed = maxspeed;
        }
 
        float currentSpeed = Vector2f.dot(pm.ps.velocity, wishdir);
        float addSpeed = wishspeed  - currentSpeed;
        if(addSpeed > 0f)
        {
            float accelspeed = accel * frametime * wishspeed;
            if(accelspeed > addSpeed)
                accelspeed = addSpeed;
 
            pm.ps.velocity.x += accelspeed * wishdir.x;
            pm.ps.velocity.y += accelspeed * wishdir.y;
        }
 
        float spd = Math.abs(pm.ps.velocity.x);
        float actualaccel = pullacceleration;
        
        if(spd > pull1)
            actualaccel *= pullstep;
        if(spd > pull2)
            actualaccel *= pullstep;
        if(spd > pull3)
            actualaccel *= pullstep;
        if(spd > pull4)
            actualaccel *= pullstep;
        if(spd > pull5)
            actualaccel *= pullstep;
        if(spd > pull6)
            actualaccel *= pullstep;
 
        if((pm.ps.applyPull && movemode == 1) || (movemode == 2 && pm.cmd.Right))
            pm.ps.velocity.x += actualaccel * frametime;
 
        float speed2 = (float)Math.sqrt(pm.ps.velocity.x * pm.ps.velocity.x + pm.ps.velocity.y * pm.ps.velocity.y);
        if(speed2 < 1f)
        {
            pm.ps.velocity.x = 0f;
            pm.ps.velocity.y = 0f;
            return;
        }
 
        // If noclipping, just apply the velocity and be done with it
        if(pm.ps.moveType == MoveType.NOCLIP || pm.ps.moveType == MoveType.EDITMODE) {
            pm.ps.origin.x += pm.ps.velocity.x * frametime;
            pm.ps.origin.y += pm.ps.velocity.y * frametime;
            return;
        }
        
        org_origin.x = pm.ps.origin.x;
         org_origin.y = pm.ps.origin.y;
         org_velocity.x = pm.ps.velocity.x;
         org_velocity.y = pm.ps.velocity.y;
 
 
        pm.blocked = 0;
        int tries = 0;
        CollisionResult res = null;
        float timeLeft = frametime;
        do {
 
            float destx = pm.ps.origin.x + pm.ps.velocity.x * timeLeft;
            float desty = pm.ps.origin.y + pm.ps.velocity.y * timeLeft;
 
            res = pm.Trace(pm.ps.origin, new Vector2f(destx, desty), Game.PlayerMins, Game.PlayerMaxs, pm.tracemask, pm.ps.clientNum);
 
            // Move up
            pm.ps.origin.x += pm.ps.velocity.x * timeLeft * res.frac;
            pm.ps.origin.y += pm.ps.velocity.y * timeLeft * res.frac;
 
            timeLeft *= 1f-res.frac;
            tries++;
            if(res.frac != 1f) {
 
                if((res.HitAxis.x == 0f && res.HitAxis.y == 0f) ) {
                    // Stuck
                    pm.ps.velocity.set(0,0);
                    pm.ps.origin.set(org_origin);
                    int test = 2;
                    return;
                }
                // Clip velocity and try to move the remaining bit               
                ClipVelocity(pm.ps.velocity, pm.ps.velocity, res.HitAxis, 1.001f, pm);
 
                // Blocked
            }
        } while( timeLeft > 0.0f && tries < 5);
 
        if((pm.blocked & 1) == 1 && (pm.onGround || org_velocity.y > 0.1f)) {
            // Store normal move results
            Vector2f saved_org = new Vector2f(pm.ps.origin);
            Vector2f saved_vel = new Vector2f(pm.ps.velocity);
 
            // Revert to pre-walkmove
            pm.ps.origin.set(org_origin);
            pm.ps.velocity.set(org_velocity);
 
            if(!TryStepMove(pm)) {
                pm.ps.origin.set(saved_org);
                pm.ps.velocity.set(saved_vel);
               if(saved_vel.length() < 10f && org_velocity.length() > 10f)
                 AddEvent(pm, Event.HIT_WALL, 0);
            } else {
                float heightDiff = pm.ps.origin.y - org_origin.y;
                AddEvent(pm, Event.STEP, (int)(heightDiff*100));
            }
       }
    }
 
     // try a series of up, forward, down moves
    private static boolean TryStepMove(MoveQuery pm) {
        // Up
        Vector2f up = new Vector2f(pm.ps.origin);
        up.y += stepheight;
        CollisionResult res = pm.Trace(pm.ps.origin, up, Game.PlayerMins, Game.PlayerMaxs, pm.tracemask, pm.ps.clientNum);
        if(res.frac != 1f) {
            return false;
        }
 
        // Forward
        pm.ps.origin.y += stepheight;
        up.set(pm.ps.velocity);
        up.scale(frametime);
        Vector2f.add(pm.ps.origin, up, up);
 
        res = pm.Trace(pm.ps.origin, up, Game.PlayerMins, Game.PlayerMaxs, pm.tracemask, pm.ps.clientNum);
        if(res.frac != 1f) {
            return false;
        }
 
        // Press down
        up.set(pm.ps.velocity);
        up.scale(frametime);
        Vector2f.add(pm.ps.origin, up, pm.ps.origin);
        up.set(pm.ps.origin);
        up.y -= stepheight;
        res = pm.Trace(pm.ps.origin, up, Game.PlayerMins, Game.PlayerMaxs, pm.tracemask, pm.ps.clientNum);
        if(res.frac == 1f) {
            return false;
        }
 
        pm.ps.origin.y -= res.frac * stepheight;
 
        return true; // success
    }
 
     
 
     // result = in - 2*n (n*in);
     private static void ClipVelocity(Vector2f in, Vector2f out, Vector2f normal, float overbounce, MoveQuery pm) {
         // Normalize the normal :)
 
         
         float len = in.length();
         Vector2f result = new Vector2f(normal);
         result.normalise();
 
         if(result.x > 0.9 || result.x < -0.9)
             pm.blocked = 1; // step/wall
         if(result.x < 0.5 && result.x > -0.5f)
             pm.blocked = 2; // floor
         
         float dot = Vector2f.dot(result, in) * overbounce;
         float change = result.x * dot;
         out.x = in.x - change;
         change = result.y * dot;
         out.y = in.y - change;
 
         if(out.x > -0.001 && out.x < 0.001 && out.y > -0.001 && out.y < 0.001)
             return;
 
 //        if((pm.blocked & 2) == 0)
 //            return;
 //
 //        out.normalise();
 //        out.scale(len);
     }
 
     private static void UpdateStepSound(MoveQuery pm) {
         if(pm.ps.stepTime > 0)
             return; // not time yet
 
         float speed = pm.ps.velocity.length();
 
         if(speed > 70f && pm.onGround) {
             pm.ps.stepTime = 300;
             AddEvent(pm, Event.FOOTSTEP, 0);
         }
     }
 
     private static void AddEvent(MoveQuery pm, int event, int eventParam) {
         pm.ps.AddPredictableEvent(event, eventParam);
     }
 
     private static void DropTimers(MoveQuery pm) {
         pm.ps.stepTime -= msec;
         pm.ps.jumpTime -= msec;
         if(pm.ps.stepTime < 0)
             pm.ps.stepTime = 0;
         if(pm.ps.jumpTime < 0)
             pm.ps.jumpTime = 0;
     }
 }
