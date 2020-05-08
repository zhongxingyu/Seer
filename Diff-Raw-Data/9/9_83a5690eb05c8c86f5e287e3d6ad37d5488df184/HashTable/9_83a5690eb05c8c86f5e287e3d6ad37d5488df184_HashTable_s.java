 /*** HashTable with open addressing* With quadratic probing* @version cpe 103 section 4** @author Mathew Duhon and Jason Dreisbach* @version Program 4*/
 import java.util.*;
 public class HashTable
 {
    private HashEntry[] table;
    private int count;
    
    private class HashEntry
    {
       public Object item;
       public boolean isActive;
       public HashEntry(Object x)
       {
          item = x;
          isActive = true;
       }
    }
    
    
    /**
     * creates a new Hashtable of size s
     * @param: s the size of the hashtable to be created
     * @Pre-conditions: none
     * @Post-conditions: initialize the empty hashtable of size s
     *                   null
     */
    public HashTable(int size)
    {
       count = 0;
       table = new HashEntry[nextPrime(2*size)];
    }
    
    
    private class Iter implements java.util.Iterator
    {
       public HashEntry[] table;
       public int cursor;
       
       public Iter(HashEntry[] table)
       {
          cursor = 0;
          this.table = table;
       }
       public boolean hasNext()
       {
          int i = cursor;
          while(true)
          {
             if( i >= table.length)
             {
                return false;
             }
             if(table[i] != null)
             {
                if(table[i].isActive)
                   break;
             }
             i++;
          }
          return true;
       }
       public Object next()
       {
          Object ret = null;
          while(ret == null)
          {
             if( cursor >= table.length)
             {
                System.out.println(cursor +"=?"+table.length);
                throw new NoSuchElementException();
             }
             if(table[cursor] != null)
             {
                if(table[cursor].isActive)
                   ret = table[cursor].item;
             }
             cursor++;
          }
          return ret;
       }
       public void remove()
       {
          throw new UnsupportedOperationException();
       }
    }
 
    
    
    public Object find(Object item)
    {
       int index = findPosition(item);
      return table[index] != null && table[index].isActive;
    }
    
    
    
    public void insert(Object item)
    {
       int index = findPosition(item);
       if(table[index] == null)
       {
          table[index] = new HashEntry(item);
          count++;
          if(count >= table.length/2)
          {
             rehash();
          }
       }
       else 
       {
          if(!table[index].isActive)
          {
             table[index].isActive = true;            
          }
       }
    }
    
    
 
    
    public void delete(Object item)
    {
       int index = findPosition(item);
       if(table[index] != null)
       {
          table[index].isActive = false;
         count--;
       }
    }
    
    
    public int elementCount()
    {
       return count;
    }
    
    
    public boolean isEmpty()
    {
       return count == 0;
    }
    
    
    public void makeEmpty()
    {
       count = 0;
       for( int i = 0; i < table.length; i++)
       {
          table[i] = null;
       }
    }
    
    
    public void printTable()
    { 
       for( int i = 0; i <table.length; i++)
       {
          System.out.print("[" + i + "]: ");
          if(table[i] != null)
          {
             System.out.print(table[i].item);
             if(table[i].isActive)
                System.out.println(", active");
             else
                System.out.println(", inactive");
          }
          else
             System.out.println("empty");
       }
       System.out.println();
    }
    
    
    public Iterator iterator()
    {
       return new Iter(table);
    }
    
    
    private int nextPrime(int x)
    {
      int prime = x;
      boolean found = false;
      while(!found)
      {
         found = true;
         for(int i = 2; i < prime; i++)
         {
            if(prime % i == 0)
            {
               found = false;
               break;
            }
         }
         if(!found)
             prime++;
      }
      return prime;
    }
       
    
    private int findPosition(Object x)
    {
       int i = 0;
       int hashValue = hash(x);
       int index = hashValue;
       while(table[index] != null &&  !x.equals(table[index].item))
       {
          i++;
          index = (hashValue+i*i) % table.length;
       }
       return index;
    }
    
    
    private void rehash()
    {
       HashEntry[] temp = table;
       table = new HashEntry[nextPrime(2*temp.length)];
       count = 0;
       for(int i = 0; i < temp.length; i++)
       {
          if(temp[i] != null && temp[i].isActive)
          {
             insert(temp[i].item);
          }
       }
    }
    
    
    private int hash(Object x)
    {
       return Math.abs(x.hashCode()) % table.length;
    }
 
 }
