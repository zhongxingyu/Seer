 package de.hh.changeRing.infrastructure.jsfExtension;
 
 import de.hh.changeRing.BaseEntity;
 import de.hh.changeRing.Context;
 import de.hh.changeRing.advertisement.Advertisement;
 import de.hh.changeRing.user.User;
 
 import javax.enterprise.inject.Model;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.faces.convert.FacesConverter;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import static java.lang.Long.parseLong;
 
 /**
  * ----------------GNU General Public License--------------------------------
  * <p/>
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * <p/>
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * <p/>
  * ----------------in addition-----------------------------------------------
  * <p/>
  * In addition, each military use, and the use for interest profit will be
  * excluded.
  * Environmental damage caused by the use must be kept as small as possible.
  */
 @SuppressWarnings("UnusedDeclaration")
 public abstract class EntityConverter<TYPE extends BaseEntity> implements Converter {
     @FacesConverter("userConverter")
     public static class UserConverter extends EntityConverter<User> {
         public UserConverter() {
             super(User.class);
         }
     }
 
     @FacesConverter("advertisementConverter")
     public static class AdvertisementConverter extends EntityConverter<Advertisement> {
         public AdvertisementConverter() {
             super(Advertisement.class);
         }
     }
 
     private Class<TYPE> clazz;
 
     protected EntityConverter(Class<TYPE> clazz) {
         this.clazz = clazz;
     }
 
     @Override
     public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String s) {
         try {
            return new Context(facesContext).getNamedBean(BaseEntityConversionHelper.class).getData(clazz, parseLong(s));
         } catch (NumberFormatException e) {
             return null;
         }
     }
 
     @Override
     public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object o) {
         return o instanceof BaseEntity
                 ? ((BaseEntity) o).getId().toString()
                 : "Bitte wählen";
     }
 
     @Model
    public static class BaseEntityConversionHelper {
         @PersistenceContext
         private EntityManager entityManager;
 
         public <T> T getData(Class<T> type, Object obj) {
             return entityManager.find(type, obj);
         }
     }
 }
