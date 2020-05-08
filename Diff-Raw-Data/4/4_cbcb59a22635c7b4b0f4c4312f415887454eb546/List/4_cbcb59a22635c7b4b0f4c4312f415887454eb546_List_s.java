 package linkedlists;
 
 public class List<T extends Comparable<? super T>>
 {
     ListNode<T> first;
     ListNode<T> last;
     String name;
 
     public List()
     {
         this( "list" );
     }
 
     public List( String listName )
     {
         name = listName;
         first = last = null;
     }
 
     public void insertAtFront( T item )
     {
         if ( isEmpty() )
             first = last = new ListNode<T>( item );
         else
             first = new ListNode<T>( item, first );
     }
 
     public void insertAtBack( T item )
     {
         if ( isEmpty() )
             first = last = new ListNode<T>( item );
         else
             last = last.next = new ListNode<T>( item );
     }
 
     public void insertInOrder( T item )
     {
         if ( isEmpty() )
         {
             insertAtBack( item );
             return;
         }
         ListNode<T> node = first;
         if ( first.data.compareTo( item ) >= 0 )
         {
             ListNode<T> insert = new ListNode<T>( item );
             insert.next = first;
             first = insert;
             return;
         }
         while ( node.next != null )
         {
             if ( node.next.data.compareTo( item ) >= 0 )
             {
                 ListNode<T> insert = new ListNode<T>( item );
                 insert.next = node.next;
                 node.next = insert;
                 return;
             }
             node = node.next;
         }
     }
 
     public T front() throws EmptyListException
     {
         if ( isEmpty() )
             throw new EmptyListException( name );
 
         T item = first.data;
         return item;
     }
 
     public T removeFromFront() throws EmptyListException
     {
         if ( isEmpty() )
             throw new EmptyListException( name );
 
         T item = first.data;
 
         if ( first == last )
             first = last = null;
         else
             first = first.next;
 
         return item;
     }
 
     public T back() throws EmptyListException
     {
         if ( isEmpty() )
             throw new EmptyListException( name );
 
         T item = last.data;
         return item;
     }
 
     public T removeFromBack() throws EmptyListException
     {
         if ( isEmpty() )
             throw new EmptyListException( name );
 
         T item = last.data;
 
         if ( first == last )
             first = last = null;
         else
         {
             ListNode<T> curr = first;
             while ( curr.next != last )
                 curr = curr.next;
             last = curr;
             curr.next = null;
         }
 
         return item;
     }
 
     public boolean isEmpty()
     {
         return first == null;
     }
 
     public void print()
     {
         if ( isEmpty() )
         {
             System.out.printf( "Empty %s\n", name );
             return;
         }
 
         System.out.printf( "The %s is:", name );
         ListNode<T> curr = first;
 
         while ( curr != null )
         {
             System.out.printf( " %s", curr.data );
             curr = curr.next;
         }
 
         System.out.println( "\n" );
     }
 }
