 ///////////////////////////////////////////////
 // This file contains all of the enums used in this app
 ///////////////////////////////////////////////
 
 // Defines game states
 enum AppStates
 {
   LoadCore, 
   Menu, 
   Game, 
   Release, 
   Exit
 }
 
 // Defines menu screens/scenes
 enum mmScenes
 {
   Main, 
   Play, 
   Help, 
   Options, 
   Quit, 
   Derp,
   Exit
 }
 
 enum directions
 {
   Up, 
   Down,
   Left,
   Right
 }
 
 // defines entites.
 enum EntityType
 {
  // genericize AI. All ai should behave in a similar way
  Projectile, 
   Sun,
   Platform,
   Planet,
   Starfield
 }
 
 enum PlatformType
 {
   Guass, 
   Missile,
   Torpedo,
   Pulse
 }
 
 enum Resource
 {
  Missile, 
  AI,
   Sun,
   Platform,
   Planet,
   Starfield
 }
 
