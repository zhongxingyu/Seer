 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.fi.pv243.mymaps.serviceImpl;
 
 import cz.muni.fi.pv243.mymaps.dao.ViewDao;
 import cz.muni.fi.pv243.mymaps.dto.MyMap;
 import cz.muni.fi.pv243.mymaps.dto.User;
 import cz.muni.fi.pv243.mymaps.dto.View;
 import cz.muni.fi.pv243.mymaps.entities.ViewEntity;
 import cz.muni.fi.pv243.mymaps.service.ViewService;
 import cz.muni.fi.pv243.mymaps.util.EntityDTOconvertor;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
import javax.inject.Inject;
 
 /**
  *
  * @author Kuba
  */
 public class ViewServiceImpl implements ViewService {
 
    @Inject
     private ViewDao viewDao;
 
     @Override
     public void createView(View view) {
         if (view == null) {
             throw new IllegalArgumentException("View can not be null.");
         }
 
         ViewEntity newView = EntityDTOconvertor.convertView(view);
 
         viewDao.create(newView);
     }
 
     @Override
     public void updateView(View view) {
         if (view == null) {
             throw new IllegalArgumentException("View can not be null.");
         }
 
         ViewEntity newView = EntityDTOconvertor.convertView(view);
 
         viewDao.update(newView);
     }
 
     @Override
     public void deleteView(View view) {
         if (view == null) {
             throw new IllegalArgumentException("View can not be null.");
         }
 
         ViewEntity newView = EntityDTOconvertor.convertView(view);
 
         viewDao.delete(newView);
     }
 
     @Override
     public View getViewById(Long id) {
         if (id == null) {
             throw new IllegalArgumentException("ID can not be null.");
         }
 
         return EntityDTOconvertor.convertView(viewDao.getById(id));
 
     }
 
     @Override
     public List<View> findViewsByName(String name) {
         if (name == null) {
             throw new IllegalArgumentException("Name can not be null.");
         }
 
         List<ViewEntity> views = viewDao.findViewsByName(name);
         List<View> newViews = new ArrayList<>();
         for (ViewEntity u : views) {
             newViews.add(EntityDTOconvertor.convertView(u));
         }
         return newViews;
 
     }
 
     @Override
     public View getViewByMap(MyMap myMap) {
         if (myMap == null) {
             throw new IllegalArgumentException("Map can not be null.");
         }
         View view = myMap.getView();
 
         return view;
     }
 
     @Override
     public Collection<View> getViewsByUser(User user) {
         if (user == null) {
             throw new IllegalArgumentException("User can not be null.");
         }
 
         Collection<View> userViews = user.getViews();
 
         return userViews;
     }
 }
