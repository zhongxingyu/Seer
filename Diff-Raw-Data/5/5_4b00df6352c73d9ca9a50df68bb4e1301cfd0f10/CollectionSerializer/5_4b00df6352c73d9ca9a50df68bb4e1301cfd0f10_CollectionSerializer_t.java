 /*
  * The MIT License (MIT)
  *
  * Copyright (c) 2013 Steven Willems
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package be.idevelop.fiber;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import static be.idevelop.fiber.ObjectCreator.OBJECT_CREATOR;
 import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.EMPTY_SET;
 
 public class CollectionSerializer<C extends Collection> extends Serializer<C> {
 
     public CollectionSerializer(Class<C> collectionClass) {
         super(collectionClass);
 
         OBJECT_CREATOR.registerClass(collectionClass);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public C read(Input input) {
         int length = input.readInteger();
         C collection;
         if (length == 0) {
             if (List.class.isAssignableFrom(getSerializedClass())) {
                 collection = (C) EMPTY_LIST;
             } else if (Set.class.isAssignableFrom(getSerializedClass())) {
                collection = (C) EMPTY_SET;
             } else {
                 collection = createNewInstance(input.createReferenceId());
             }
         } else {
             collection = createNewInstance(input.createReferenceId());
             List<Object> tempList = new ArrayList<Object>(length);
             for (int i = 0; i < length; i++) {
                 tempList.add(input.read());
             }
             collection.addAll(tempList);
         }
         return collection;
     }
 
     protected C createNewInstance(int referenceId) {
         return OBJECT_CREATOR.createNewInstance(getSerializedClass(), referenceId);
     }
 
     @Override
     public void write(C collection, Output output) {
         output.writeInt(collection.size());
         for (Object o : collection) {
             output.write(o);
         }
     }
 }
