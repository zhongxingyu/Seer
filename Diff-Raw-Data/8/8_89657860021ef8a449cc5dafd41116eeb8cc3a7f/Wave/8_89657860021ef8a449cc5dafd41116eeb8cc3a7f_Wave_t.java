
namespace Fretless
 {
   class Wave
   {
     /* **************************************************************************** */
     public sealed class Render_Context { }
     /* **************************************************************************** */
     public sealed class Drawing_Context { }
     /* **************************************************************************** */
     public sealed class Result
     {
       public double[] buffer;
     }
     /* **************************************************************************** */
     public interface ICursor//Slicer, playerhead, Cursor, generator? 
     {
       /* **************************************************************************** */
       void GetNextChunk(double t1, Result buf);
     }
     /* **************************************************************************** */
     public interface IPlayable
     {
       /* **************************************************************************** */
       ICursor Launch_Cursor(Render_Context rc); // from start, t0 not supported
       /* **************************************************************************** */
       ICursor Launch_Cursor(Render_Context rc, double t0);
     }
     /* **************************************************************************** */
     public interface IDrawable
     {
       /* **************************************************************************** */
       void Draw_Me(Drawing_Context dc);
     }
 
     #region real use
     /* **************************************************************************** */
     public class Playable : IPlayable
     {
       /* **************************************************************************** */
       ICursor IPlayable.Launch_Cursor(Render_Context rc) { /* from start, t0 not supported */ return new Cursor(this, rc); }
       /* **************************************************************************** */
       ICursor IPlayable.Launch_Cursor(Render_Context rc, double t0) { return new Cursor(this, rc); }
       /* **************************************************************************** */
       public class Cursor : ICursor // MyCursor
       {
         Playable MyPlayable;
         Render_Context MyRC;
         public double currentT; // and whatever state info
         public Cursor(Playable Playable0, Render_Context MyRC0) { MyPlayable = Playable0; MyRC = MyRC0; }
         /* **************************************************************************** */
         public void GetNextChunk(double t1, Result buf)
         {// this?
         }
       }
     }
     #endregion real use
 
     /* **************************************************************************** */
     public class Group : IPlayable
     {
       // we need all the local coordinates
 #if false
       boxes 
       how to store local coords if this can be in multiple places?
       as before, the parent owns the local offset coords. this means offset boxes, or offset/scale etc. boxes.
 
       but do we really want to support a single playable with multiple parents?  
       for one, it means that no playable can have a pointer to its parent. 
       well... when you click on a root you create a temp tree that does have inner parent pointers.
         also, when you drag and drop to a node, that node's parents have to be found to find the node. so a stack is available.
 
       so self-collision is prevented by:
       select the floater, a temp tree stack is created.  not important?
       drag the floater around, find other nodes by search from root node.
         every time a target is seen, look up its temp tree stack for myself.  if found, reject this target.
 
       SpatialSearch(globalrootnode, xyloc, resultstack)
       if (resultstack.GotHit()){
         climb resultstack upward(?) and look for self.
       }
 
       basically, parent pointers are always generated dynamically, by always searching from root.
       so you need a stack type, or treepath type. treestack?
 
       why have single playable with multiple parents?  the idea was to replicate an edit across many places as a pattern.
 
       alternative is to have separate nodes, but all reference a single library object.  
       The object should reference back to them in a list.  The list isn't saved, just every lib object has a guid.
 
       with multparent, the clonish behavior only works within one run-time.
 
       with a library using guids, there may be version control issues.  we do not want to get into that.
 
       for one, a master node only exists within a single file.  it can be imported or exported, but only as a new identity.
 
       can we edit a master node straight out of the library?
 
       again, a master node may be defined as having children.  all the children are the same for every instance of the master.
 
       master nodes would not need location boxes, if each instance kept xy inside itself. xy and all other transforms.
 
       either way, transforms must be separate from their child objects. either as separate objects, or walled off inside same object.
         separate object (transform box) is probably for the better.  transform box can point to parent.  
       
       in the short run, support cursor type, but still make every node a unique instance.  try to avoid needing parent pointers and see how it goes.
 
 
 
 #endif
       /* **************************************************************************** */
       ICursor IPlayable.Launch_Cursor(Render_Context rc) { /* from start, t0 not supported */ return new Cursor(this, rc); }
       /* **************************************************************************** */
       ICursor IPlayable.Launch_Cursor(Render_Context rc, double t0) { return new Cursor(this, rc); }
       /* **************************************************************************** */
       public class Cursor : ICursor // MyCursor
       {
         Group MyPlayable;
         Render_Context MyRC;
         public double currentT; // and whatever state info
         public Cursor(Group Playable0, Render_Context MyRC0) { MyPlayable = Playable0; MyRC = MyRC0; }
         /* **************************************************************************** */
         public void GetNextChunk(double t1, Result buf)
         {// this?
         }
       }
     }
   }
 }
