 package fr.blemale.dropwizard.todo.api.todo.external;
 
 import com.fasterxml.jackson.annotation.JsonCreator;
 import com.fasterxml.jackson.annotation.JsonProperty;
 import fr.blemale.dropwizard.todo.core.Todo;
 import fr.blemale.dropwizard.todo.resources.TodoResource;
 
 import javax.validation.constraints.NotNull;
 import javax.ws.rs.core.UriBuilder;
 
 public class ExternalTodo {
     @NotNull
     @JsonProperty
     private final long id;
     @NotNull
     @JsonProperty
     private final String title;
     @JsonProperty
     private final String content;
     @NotNull
     @JsonProperty
     private final String selfUrl;
 
     @JsonCreator
     public ExternalTodo(@JsonProperty("id") long id, @JsonProperty("title") String title, @JsonProperty("content") String content, @JsonProperty("selfUrl") String selfUrl) {
         this.id = id;
         this.title = title;
         this.content = content;
         this.selfUrl = selfUrl;
     }
 
     public long getId() {
         return id;
     }
 
     public String getTitle() {
         return title;
     }
 
     public String getContent() {
         return content;
     }
 
     public String getSelfUrl() {
         return selfUrl;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         ExternalTodo that = (ExternalTodo) o;
 
         if (id != that.id) return false;
         if (content != null ? !content.equals(that.content) : that.content != null) return false;
         if (!selfUrl.equals(that.selfUrl)) return false;
         return title.equals(that.title);
 
     }
 
     @Override
     public int hashCode() {
         int result = (int) (id ^ (id >>> 32));
         result = 31 * result + title.hashCode();
         result = 31 * result + (content != null ? content.hashCode() : 0);
         result = 31 * result + selfUrl.hashCode();
         return result;
     }
 
     public static class Mapper {
         public ExternalTodo fromTodo(Todo todo) {
             return new ExternalTodo(
                     todo.getId(),
                     todo.getTitle(),
                     todo.getContent().or(""),
                    UriBuilder.fromResource(TodoResource.class).build(todo.getId()).getPath());
         }
     }
 }
