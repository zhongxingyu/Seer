 public class ArrayList<E> implements List<E>, Queue<E>, Stack<E>
 {
     private int size;
     private Object[] elements;
     
     public ArrayList()
     {
         this(16);
     }
     
     public ArrayList(int capacity)
     {
         if (capacity < 0)
         {
             throw new IllegalArgumentException();
         }
         elements = new Object[capacity];
     }
     
     public void add(E element)
     {
         if (size == elements.length)
         {
             grow();
         }
         elements[size++] = element;
     }
     
     public void add(int index, E element)
     {
         if (index < 0 || index > size)
         {
             throw new IndexOutOfBoundsException();
         }
         if (size == elements.length)
         {
             grow();
         }
         for (int i = size; i > index; --i)
         {
             elements[i] = elements[i - 1];
         }
         elements[index] = element;
         ++size;
     }
     
     public void clear()
     {
         for (int i = 0; i < size; ++i)
         {
             elements[i] = null;
         }
         size = 0;
     }
     
     public boolean contains(E element)
     {
         return index(element) >= 0;
     }
     
     public ArrayList<E> copy()
     {
         ArrayList<E> copy = new ArrayList<E>(size);
         if (size == 0)
         {
             return copy;
         }
         for (int i = 0; i < size; ++i)
         {
             if (elements[i] instanceof Copyable<?>)
             {
                 Copyable<?> copyable = (Copyable<?>)elements[i];
                 copy.elements[i] = copyable.copy();
             }
             else
             {
                 copy.elements[i] = elements[i];
             }
         }
         return copy;
     }
     
     public E dequeue()
     {
         if (size == 0)
         {
             throw new IllegalStateException();
         }
         return remove(0);
     }
     
     public boolean equals(Object object)
     {
         if (object == this)
         {
             return true;
         }
         if (!(object instanceof ArrayList<?>))
         {
             return false;
         }
         ArrayList<?> arrayList = (ArrayList<?>)object;
         if (size != arrayList.size)
         {
             return false;
         }
         for (int i = 0; i < size; ++i)
         {
             if (!elements[i].equals(arrayList.elements[i]))
             {
                 return false;
             }
         }
         return true;
     }
     
     public boolean empty()
     {
         return size == 0;
     }
     
     public void enqueue(E element)
     {
         add(element);
     }
     
    public Enumerator<E> enumerator()
     {
        return new Enumerator<E>()
         {
            private int index;
             
            public boolean more()
             {
                return index < size;
             }
             
             public E next()
             {
                if (!more())
                 {
                     throw new IllegalStateException();
                 }
                return element(index++);
             }
         };
     }
     
     public E get(int index)
     {
         if (!valid(index))
         {
             throw new IndexOutOfBoundsException();
         }
         return element(index);
     }
     
     public int hashCode()
     {
         int hash = 0;
         for (int i = 0; i < size; ++i)
         {
             hash = hash * 31 + elements[i].hashCode();
         }
         return hash;
     }
     
     public E head()
     {
         if (size == 0)
         {
             throw new IllegalStateException();
         }
         return element(0);
     }
     
     public int index(E element)
     {
         for (int i = 0; i < size; ++i)
         {
             if (elements[i].equals(element))
             {
                 return i;
             }
         }
         return -1;
     }
     
     public E pop()
     {
         if (size == 0)
         {
             throw new IllegalStateException();
         }
         return remove(size - 1);
     }
     
     public void push(E element)
     {
         add(element);
     }
     
     public E remove(int index)
     {
         if (!valid(index))
         {
             throw new IndexOutOfBoundsException();
         }
         E element = element(index);
         for (int i = index; i < size - 1; ++i)
         {
             elements[i] = elements[i + 1];
         }
         elements[size - 1] = null;
         --size;
         return element;
     }
     
     public void remove(E element)
     {
         if (element == null)
         {
             throw new IllegalArgumentException();
         }
         for (int i = 0; i < size; ++i)
         {
             if (element.equals(elements[i]))
             {
                 remove(i);
                 return;
             }
         }
     }
     
     public E set(int index, E element)
     {
         if (!valid(index))
         {
             throw new IndexOutOfBoundsException();
         }
         E old = element(index);
         elements[index] = element;
         return old;
     }
     
     public int size()
     {
         return size;
     }
     
     public E top()
     {
         if (size == 0)
         {
             throw new IllegalStateException();
         }
         return element(size - 1);
     }
     
     public String toString()
     {
         CharArray charArray = new CharArray();
         charArray.append("[");
         for (int i = 0; i < size; ++i)
         {
             if (i != 0)
             {
                 charArray.append(", ");
             }
             charArray.append(elements[i].toString());
         }
         charArray.append("]");
         return charArray.toString();
     }
     
     @SuppressWarnings("unchecked")
     private E element(int index)
     {
         return (E)elements[index];
     }
     
     private void grow()
     {
         Object[] copy = new Object[elements.length * 2];
         for (int i = 0; i < elements.length; ++i)
         {
             copy[i] = elements[i];
         }
         elements = copy;
     }
     
     private boolean valid(int index)
     {
         return size > 0 && index >= 0 && index < size;
     }
 }
