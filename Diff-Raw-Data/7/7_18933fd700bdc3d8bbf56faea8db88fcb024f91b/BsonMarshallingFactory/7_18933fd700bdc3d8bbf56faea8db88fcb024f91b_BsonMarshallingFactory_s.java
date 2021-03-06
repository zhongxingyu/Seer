<<<<<<< HEAD:mingo-core/src/main/java/com/mingo/marshall/BsonMarshallingFactory.java
 /**
  * Copyright 2012-2013 The Mingo Team
  * <p/>
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.mingo.marshall;
=======
 package com.mingo.mapping.marshall;
>>>>>>> iss4:mingo-core/src/main/java/com/mingo/mapping/marshall/BsonMarshallingFactory.java

 
 public interface BsonMarshallingFactory {
 
     BsonMarshaller createMarshaller();
 
     BsonUnmarshaller createUnmarshaller();
 
     JsonToDBObjectMarshaller createJsonToDbObjectMarshaller();
 }
