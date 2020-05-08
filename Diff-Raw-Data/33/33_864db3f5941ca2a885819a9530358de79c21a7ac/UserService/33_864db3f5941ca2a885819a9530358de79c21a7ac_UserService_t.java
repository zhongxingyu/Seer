 package ru.dimafeng.springresttest.service;
 
 import ru.dimafeng.springresttest.model.User;
 
 import java.util.List;
 
 public interface UserService {
 
     List<User> getUsers();
 
     User addUser(User newUser);

    public void removeUser(long id);
 }
