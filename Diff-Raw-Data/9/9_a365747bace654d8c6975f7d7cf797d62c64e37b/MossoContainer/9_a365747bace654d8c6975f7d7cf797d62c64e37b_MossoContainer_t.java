 /**
  * Elastic Grid
  * Copyright (C) 2008-2009 Elastic Grid, LLC.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.elasticgrid.storage.mosso.cloudfiles;
 
 import com.elasticgrid.storage.Container;
 import com.elasticgrid.storage.Storable;
 import com.elasticgrid.storage.StorageException;
 import com.mosso.client.cloudfiles.FilesClient;
 import com.mosso.client.cloudfiles.FilesContainer;
 import com.mosso.client.cloudfiles.FilesObject;
 import java.util.List;
 import java.util.ArrayList;
 import java.io.File;
 import java.io.IOException;
 import org.apache.commons.httpclient.HttpException;
import javax.activation.MimetypesFileTypeMap;
 
 /**
  * {@link Container} providing support for Mosso Cloud Files.
  *
  * @author Jerome Bernard
  */
 public class MossoContainer implements Container {
     private final FilesClient mosso;
     private final FilesContainer mossoContainer;
    private final MimetypesFileTypeMap mimes;
 
     public MossoContainer(final FilesClient mosso, final FilesContainer mossoContainer) {
         this.mosso = mosso;
         this.mossoContainer = mossoContainer;
        this.mimes = new MimetypesFileTypeMap();
     }
 
     public String getName() {
         return mossoContainer.getName();
     }
 
     public List<Storable> listStorables() throws StorageException {
         try {
             mosso.login();
             List<FilesObject> objects = mossoContainer.getObjects();
             List<Storable> storables = new ArrayList<Storable>(objects.size());
             for (FilesObject object : objects) {
                 storables.add(new MossoStorable(mosso, object));
             }
             return storables;
         } catch (Exception e) {
             throw new StorageException("Can't list storables", e);
         }
     }
 
     public Storable uploadStorable(String key, File file) throws StorageException {
         try {
             mosso.login();
             // upload the file
            mosso.storeObjectAs(getName(), file, mimes.getContentType(file), key);
             // retrieve mosso object
             List<FilesObject> objects = mossoContainer.getObjects(key);
             FilesObject object = null;
             for (FilesObject o : objects)
                 if (o.getName().equals(file.getName()))
                     object = o;
             return new MossoStorable(mosso, object);
         } catch (Exception e) {
             throw new StorageException("Can't upload storable from file", e);
         }
     }
 }
