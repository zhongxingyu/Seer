 package collection;
 
 import debug.IndentingPrintWriter;
 import debug.Printable;
 
 public class FingerTreeSeq<T> implements Seq<T> {
   private Item root;
 
   public FingerTreeSeq() {
     root = new Item.Empty();
   }
 
   private FingerTreeSeq(Item that) {
     root = that;
   }
 
   @Override
   @SuppressWarnings({"unchecked"})
   public T head() throws EmptyException {
     return (T) root.head();
   }
 
   @Override
   public Seq<T> tail() throws EmptyException {
     throw new UnsupportedOperationException();
   }
 
   @Override
   public FingerTreeSeq<T> cons(Object v) {
     return new FingerTreeSeq<T>(root.cons(new Elem(v)));
   }
 
   @Override
   public FingerTreeSeq<T> snoc(Object v) {
     return new FingerTreeSeq<T>(root.snoc(new Elem(v)));
   }
 
   @Override
   public Seq<T> concat(Seq<T> that) {
     throw new UnsupportedOperationException();
   }
 
   @Override
   public int size() {
     return root.size();
   }
 
   @Override
   @SuppressWarnings({"unchecked"})
   public T nth(int index) throws RangeException {
     return (T) root.nth(index);
   }
 
   void dump(IndentingPrintWriter w) {
     root.print(w);
     w.flush();
   }
 
   private interface Measured {
     int size();
 
     Object nth(int index);
 
     Object head();
   }
 
   private static class Elem implements Measured, Printable {
     final Object e;
 
     Elem(Object e) {
       this.e = e;
     }
 
     @Override
     public int size() {
       return 1;
     }
 
     @Override
     public Object nth(int index) {
       if (index == 0) {
         return e;
       }
       throw new RangeException();
     }
 
     @Override
     public Object head() {
       return e;
     }
 
     @Override
     public void print(IndentingPrintWriter w) {
       Printable.Util.print(w, e);
     }
   }
 
   private abstract static class Digit implements Measured, Printable {
     abstract Item.Deep cons(Digit digit, Item item, Measured v);
 
     abstract Item.Deep snoc(Measured v, Item item, Digit digit);
 
     static int measure(Measured... items) {
       int m = 0;
       for (Measured item : items) {
         m = m + item.size();
       }
       return m;
     }
 
     static void print(IndentingPrintWriter w, String name, Object... items) {
       w.write("Digit." + name + "[");
      FingerTreeSeq.print(w, items);
       w.write("]");
     }
 
     static class One extends Digit {
       final Measured a;
       final int v;
 
       One(Measured a) {
         this.a = a;
         v = measure(this.a);
       }
 
       @Override
       Item.Deep cons(Digit digit, Item item, Measured v) {
         return new Item.Deep(digit, item, new Two(a, v));
       }
 
       @Override
       Item.Deep snoc(Measured v, Item item, Digit digit) {
         return new Item.Deep(new Two(v, a), item, digit);
       }
 
       @Override
       public final int size() {
         return v;
       }
 
       @Override
       public Object nth(int index) {
         if (index < a.size()) {
           return a.nth(index);
         }
         throw new RangeException();
       }
 
       @Override
       public Object head() {
         return a.head();
       }
 
       @Override
       public void print(IndentingPrintWriter w) {
         print(w, getClass().getSimpleName(), a);
       }
     }
 
     static class Two extends Digit {
       final Measured a;
       final Measured b;
       final int v;
 
       Two(Measured a, Measured b) {
         this.a = a;
         this.b = b;
         v = measure(this.a, this.b);
       }
 
       @Override
       Item.Deep cons(Digit digit, Item item, Measured v) {
         return new Item.Deep(digit, item, new Three(a, b, v));
       }
 
       @Override
       Item.Deep snoc(Measured v, Item item, Digit digit) {
         return new Item.Deep(new Three(v, a, b), item, digit);
       }
 
       @Override
       public final int size() {
         return v;
       }
 
       @Override
       public Object nth(int index) {
         if (index < a.size()) {
           return a.nth(index);
         }
         index -= a.size();
         if (index < b.size()) {
           return b.nth(index);
         }
         throw new RangeException();
       }
 
       @Override
       public Object head() {
         return b.head();
       }
 
       @Override
       public void print(IndentingPrintWriter w) {
         print(w, getClass().getSimpleName(), a, b);
       }
     }
 
     static class Three extends Digit {
       final Measured a;
       final Measured b;
       final Measured c;
       final int v;
 
       Three(Measured a, Measured b, Measured c) {
         this.a = a;
         this.b = b;
         this.c = c;
         v = measure(this.a, this.b, this.c);
       }
 
       @Override
       Item.Deep cons(Digit digit, Item item, Measured v) {
         return new Item.Deep(digit, item, new Four(a, b, c, v));
       }
 
       @Override
       Item.Deep snoc(Measured v, Item item, Digit digit) {
         return new Item.Deep(new Four(v, a, b, c), item, digit);
       }
 
       @Override
       public final int size() {
         return v;
       }
 
       @Override
       public Object nth(int index) {
         if (index < a.size()) {
           return a.nth(index);
         }
         index -= a.size();
         if (index < b.size()) {
           return b.nth(index);
         }
         index -= b.size();
         if (index < c.size()) {
           return c.nth(index);
         }
         throw new RangeException();
       }
 
       @Override
       public Object head() {
         return c.head();
       }
 
       @Override
       public void print(IndentingPrintWriter w) {
         print(w, getClass().getSimpleName(), a, b, c);
       }
     }
 
     static class Four extends Digit {
       final Measured a;
       final Measured b;
       final Measured c;
       final Measured d;
       final int v;
 
       Four(Measured a, Measured b, Measured c, Measured d) {
         this.a = a;
         this.b = b;
         this.c = c;
         this.d = d;
         v = measure(this.a, this.b, this.c, this.d);
       }
 
       @Override
       Item.Deep cons(Digit digit, Item item, Measured v) {
         return new Item.Deep(
             digit,
             item.cons(new Node.Node3(a, b, c)),
             new Digit.Two(d, v));
       }
 
       @Override
       Item.Deep snoc(Measured v, Item item, Digit digit) {
         return new Item.Deep(
             new Digit.Two(v, a),
             item.snoc(new Node.Node3(b, c, d)),
             digit);
       }
 
       @Override
       public final int size() {
         return v;
       }
 
       @Override
       public Object nth(int index) {
         if (index < a.size()) {
           return a.nth(index);
         }
         index -= a.size();
         if (index < b.size()) {
           return b.nth(index);
         }
         index -= b.size();
         if (index < c.size()) {
           return c.nth(index);
         }
         index -= c.size();
         if (index < d.size()) {
           return d.nth(index);
         }
         throw new RangeException();
       }
 
       @Override
       public Object head() {
         return d.head();
       }
 
       @Override
       public void print(IndentingPrintWriter w) {
         print(w, getClass().getSimpleName(), a, b, c, d);
       }
     }
   }
 
   private abstract static class Node implements Measured, Printable {
     static void print(IndentingPrintWriter w, Object... items) {
       w.write("Node(");
      FingerTreeSeq.print(w, items);
       w.write(")");
     }
 
     static class Node2 extends Node {
       final Measured a, b;
       final int v;
 
       Node2(Measured a, Measured b) {
         this.a = a;
         this.b = b;
         v = a.size() + b.size();
       }
 
       @Override
       public int size() {
         return v;
       }
 
       @Override
       public Object nth(int index) {
         if (index < a.size()) {
           return a.nth(index);
         }
         index -= a.size();
         if (index < b.size()) {
           return b.nth(index);
         }
         throw new RangeException();
       }
 
       @Override
       public Object head() {
         return b.head();
       }
 
       @Override
       public void print(IndentingPrintWriter w) {
         print(w, a, b);
       }
     }
 
     static class Node3 extends Node {
       final Measured a, b, c;
       final int v;
 
       Node3(Measured a, Measured b, Measured c) {
         this.a = a;
         this.b = b;
         this.c = c;
         v = a.size() + b.size() + c.size();
       }
 
       @Override
       public int size() {
         return v;
       }
 
       @Override
       public Object nth(int index) {
         if (index < a.size()) {
           return a.nth(index);
         }
         index -= a.size();
         if (index < b.size()) {
           return b.nth(index);
         }
         index -= b.size();
         if (index < c.size()) {
           return c.nth(index);
         }
         throw new RangeException();
       }
 
       @Override
       public Object head() {
         return c.head();
       }
 
       @Override
       public void print(IndentingPrintWriter w) {
         print(w, a, b, c);
       }
     }
   }
 
   private abstract static class Item implements Measured, Printable {
     abstract Item cons(Measured v);
 
     abstract Item snoc(Measured v);
 
     static class Empty extends Item {
       @Override
       Single cons(Measured v) {
         return new Item.Single(v);
       }
 
       @Override
       Single snoc(Measured v) {
         return new Item.Single(v);
       }
 
       @Override
       public int size() {
         return 0;
       }
 
       @Override
       public Object nth(int index) {
         throw new RangeException();
       }
 
       @Override
       public Object head() {
         throw new EmptyException();
       }
 
       @Override
       public void print(IndentingPrintWriter w) {
         w.write("[EMPTY]");
       }
     }
 
     static class Single extends Item {
       final Measured v;
 
       Single(Measured v) {
         this.v = v;
       }
 
       @Override
       Deep cons(Measured v) {
         return new Item.Deep(
             new Digit.One(this.v),
             new Item.Empty(),
             new Digit.One(v));
       }
 
       @Override
       Deep snoc(Measured v) {
         return new Item.Deep(
             new Digit.One(v),
             new Item.Empty(),
             new Digit.One(this.v));
       }
 
       @Override
       public int size() {
         return v.size();
       }
 
       @Override
       public Object nth(int index) {
         return v.nth(index);
       }
 
       @Override
       public Object head() {
         return v.head();
       }
 
       @Override
       public void print(IndentingPrintWriter w) {
         if (v instanceof Printable) {
           w.write("Single(");
           ((Printable) v).print(w);
           w.write(")");
         }
         else {
           w.print(v);
         }
       }
     }
 
     static class Deep extends Item {
       final Digit dl;
       final Item item;
       final Digit dr;
       final int v;
 
       Deep(Digit dl, Item item, Digit dr) {
         this.dl = dl;
         this.item = item;
         this.dr = dr;
         v = this.dl.size() + this.item.size() + this.dr.size();
       }
 
       @Override
       Item cons(Measured v) {
         return dr.cons(dl, item, v);
       }
 
       @Override
       Deep snoc(Measured v) {
         return dl.snoc(v, item, dr);
       }
 
       @Override
       public int size() {
         return v;
       }
 
       @Override
       public Object nth(int index) {
         if (index < dl.size()) {
           return dl.nth(index);
         }
         index -= dl.size();
         if (index < item.size()) {
           return item.nth(index);
         }
         index -= item.size();
         if (index < dr.size()) {
           return dr.nth(index);
         }
         throw new RangeException();
       }
 
       @Override
       public Object head() {
         return dr.head();
       }
 
       @Override
       public void print(IndentingPrintWriter w) {
         w.write("Deep(\n");
         w.indent("    |");
         dl.print(w);
         w.write(";\n");
         item.print(w);
         w.write(";\n");
         dr.print(w);
         w.unindent();
         w.write("\n    )");
       }
     }
   }
 
  private static void print(IndentingPrintWriter w, Object[] items) {
     for (int i = 0; i < items.length; i++) {
       if (items[i] instanceof Node) {
         w.write("\n");
         w.indent("   ");
         Printable.Util.print(w, items[i]);
         if (i < items.length - 1) {
           w.write(",");
         }
         w.unindent();
       }
       else {
         Printable.Util.print(w, items[i]);
         if (i < items.length - 1) {
           w.write(",");
         }
       }
     }
   }
 }
