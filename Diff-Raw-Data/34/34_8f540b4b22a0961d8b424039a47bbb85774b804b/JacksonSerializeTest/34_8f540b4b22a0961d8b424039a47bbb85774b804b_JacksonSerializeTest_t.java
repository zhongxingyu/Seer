 package com.github.francofabio.vraptor.serialization.jackson;
 
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.core.IsEqual.equalTo;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.github.francofabio.vraptor.serialization.jackson.JacksonSerialization;
 
 public class JacksonSerializeTest {
 
     private ByteArrayOutputStream output;
     private HttpServletResponse response;
     private JacksonSerialization jacksonSerialization;
     private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
     private String currentDateAsStr;
     private Date currentDate;
 
     @Before
     public void setup() throws Exception {
         this.output = new ByteArrayOutputStream();
         this.response = mock(HttpServletResponse.class);
         when(response.getWriter()).thenReturn(new PrintWriter(output));
         this.jacksonSerialization = new JacksonSerialization(response);
         this.currentDate = new Date();
         this.currentDateAsStr = sdf.format(currentDate);
     }
 
     public static class Group {
 
         private Long id;
         private String name;
         private List<Product> products;
 
         public Group() {
             super();
         }
 
         public Group(Long id, String name) {
             super();
             this.id = id;
             this.name = name;
         }
 
         public Long getId() {
             return id;
         }
 
         public void setId(Long id) {
             this.id = id;
         }
 
         public String getName() {
             return name;
         }
 
         public void setName(String name) {
             this.name = name;
         }
 
         public List<Product> getProducts() {
             return products;
         }
 
         public void setProducts(List<Product> products) {
             this.products = products;
         }
 
     }
 
     public static class Product {
 
         private Long id;
         private String name;
         private Date creationDate;
         private Group group;
        private Object data;
 
         public Product() {
             super();
         }
 
         public Product(Long id) {
             super();
             this.id = id;
         }
 
         public Product(Long id, String name) {
             super();
             this.id = id;
             this.name = name;
         }
 
         public Product(Long id, String name, Date creationDate) {
             super();
             this.id = id;
             this.name = name;
             this.creationDate = creationDate;
         }
 
         public Product(Long id, String name, Date creationDate, Group group) {
             super();
             this.id = id;
             this.name = name;
             this.creationDate = creationDate;
             this.group = group;
         }
 
         public Long getId() {
             return id;
         }
 
         public void setId(Long id) {
             this.id = id;
         }
 
         public String getName() {
             return name;
         }
 
         public void setName(String name) {
             this.name = name;
         }
 
         public Date getCreationDate() {
             return creationDate;
         }
 
         public void setCreationDate(Date creationDate) {
             this.creationDate = creationDate;
         }
 
         public Group getGroup() {
             return group;
         }
 
         public void setGroup(Group group) {
             this.group = group;
         }
 
        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

     }
 
     public static class Order {
 
         private Long id;
         private Customer customer;
         private Address delivery;
         private List<Product> products;
 
         public Order() {
             this.products = new ArrayList<JacksonSerializeTest.Product>();
         }
 
         public Order(Long id, Customer customer) {
             this();
             this.id = id;
         }
 
         public Order(Long id, Customer customer, Address delivery) {
             this();
             this.id = id;
             this.customer = customer;
             this.delivery = delivery;
         }
 
         public Long getId() {
             return id;
         }
 
         public void setId(Long id) {
             this.id = id;
         }
 
         public List<Product> getProducts() {
             return products;
         }
 
         public void setProducts(List<Product> products) {
             this.products = products;
         }
 
         public void addProduct(Product product) {
             this.products.add(product);
         }
 
         public Customer getCustomer() {
             return customer;
         }
 
         public void setCustomer(Customer customer) {
             this.customer = customer;
         }
 
         public Address getDelivery() {
             return delivery;
         }
 
         public void setDelivery(Address delivery) {
             this.delivery = delivery;
         }
 
     }
 
     public static class HardDisk extends Product {
 
         private long capacity;
 
         public HardDisk() {
             super();
         }
 
         public HardDisk(Long id) {
             super(id);
         }
 
         public HardDisk(Long id, String name, long capacity) {
             super(id, name);
             this.setCapacity(capacity);
         }
 
         public HardDisk(Long id, String name, Date creationDate, long capacity) {
             super(id, name, creationDate);
             this.setCapacity(capacity);
         }
 
         public HardDisk(Long id, String name, Date creationDate, long capacity, Group group) {
             super(id, name, creationDate, group);
             this.setCapacity(capacity);
         }
 
         public long getCapacity() {
             return capacity;
         }
 
         public void setCapacity(long capacity) {
             this.capacity = capacity;
         }
 
     }
 
     public static class Address {
         private String street;
         private String city;
         private String zipCode;
 
         public Address() {
             super();
         }
 
         public Address(String street, String city, String zipCode) {
             super();
             this.street = street;
             this.city = city;
             this.zipCode = zipCode;
         }
 
         public String getStreet() {
             return street;
         }
 
         public void setStreet(String street) {
             this.street = street;
         }
 
         public String getCity() {
             return city;
         }
 
         public void setCity(String city) {
             this.city = city;
         }
 
         public String getZipCode() {
             return zipCode;
         }
 
         public void setZipCode(String zipCode) {
             this.zipCode = zipCode;
         }
 
     }
 
     public static class Customer {
         private Long id;
         private String name;
         private Address address;
 
         public Customer(Long id, String name, Address address) {
             super();
             this.id = id;
             this.name = name;
             this.address = address;
         }
 
         public Long getId() {
             return id;
         }
 
         public void setId(Long id) {
             this.id = id;
         }
 
         public String getName() {
             return name;
         }
 
         public void setName(String name) {
             this.name = name;
         }
 
         public Address getAddress() {
             return address;
         }
 
         public void setAddress(Address address) {
             this.address = address;
         }
 
     }
 
     private String jsonResult() {
         return output.toString();
     }
 
     private Group createGroup(Long id) {
         return new Group(id, "Group " + id);
     }
 
     private Product createProduct(Long id) {
         return new Product(id, "Product " + id, currentDate);
     }
 
     private Product createProductWithGroup(Long idProduct, Long idGroup) {
         Product product = createProduct(idProduct);
         product.setGroup(createGroup(idGroup));
         return product;
     }
 
     @Test
     public void shouldSerializePojo() {
         String expectedResult = "{\"product\":{\"id\":1,\"name\":\"Product 1\",\"creationDate\":\"" + currentDateAsStr
                 + "\"}}";
         Product product = createProduct(1L);
 
         jacksonSerialization.from(product).serialize();
         assertThat(jsonResult(), is(equalTo(expectedResult)));
     }
 
     @Test
     public void shouldSerializeCollectionOfPojo() {
         String expectedResult = "{\"productList\":[{\"id\":1,\"name\":\"Product 1\",\"creationDate\":\""
                 + currentDateAsStr + "\"},{\"id\":2,\"name\":\"Product 2\",\"creationDate\":\"" + currentDateAsStr
                 + "\"}]}";
         List<Product> products = new ArrayList<JacksonSerializeTest.Product>();
         products.add(createProduct(1L));
         products.add(createProduct(2L));
 
         jacksonSerialization.from(products).serialize();
         assertThat(jsonResult(), is(equalTo(expectedResult)));
     }
 
     @Test
     public void shouldUseAlias() {
         String expectedResult = "{\"myProduct\":{\"id\":1,\"name\":\"Product 1\",\"creationDate\":\""
                 + currentDateAsStr + "\"}}";
         Product product = createProduct(1L);
 
         jacksonSerialization.from(product, "myProduct").serialize();
         assertThat(jsonResult(), is(equalTo(expectedResult)));
     }
 
     @Test
     public void shouldSerializeMap() {
         String expectedResult = "{\"person\":{\"email\":\"ff@gmail.com\",\"idade\":28,\"name\":\"fabio franco\"}}";
         Map<String, Object> json = new HashMap<String, Object>();
         json.put("name", "fabio franco");
         json.put("idade", 28);
         json.put("email", "ff@gmail.com");
 
         jacksonSerialization.from(json, "person").serialize();
         assertThat(jsonResult(), is(equalTo(expectedResult)));
     }
 
     @Test
     public void shouldSerializeList() {
         String expectedResult = "{\"list\":[1,2,3,4,5,6,\"json\"]}";
 
         List<Object> list = new ArrayList<Object>();
         list.addAll(Arrays.asList(1, 2, 3, 4, 5, 6));
         list.add("json");
 
         jacksonSerialization.from(list, "list").serialize();
         assertThat(jsonResult(), is(equalTo(expectedResult)));
     }
 
     @Test
     public void shouldIncludeField() {
         String expectedResult = "{\"product\":{\"id\":1,\"name\":\"Product 1\",\"creationDate\":\"" + currentDateAsStr
                 + "\",\"group\":{\"id\":1,\"name\":\"Group 1\"}}}";
 
         Product product = createProductWithGroup(1L, 1L);
 
         jacksonSerialization.from(product).include("group").serialize();
         assertThat(jsonResult(), is(equalTo(expectedResult)));
     }
 
     @Test
     public void shouldIncludeFieldFromCollection() {
         String expectedResult = "{\"order\":{\"id\":1,\"products\":[{\"id\":1,\"name\":\"Product 1\",\"creationDate\":\""
                 + currentDateAsStr
                 + "\",\"group\":{\"id\":1,\"name\":\"Group 1\"}},{\"id\":2,\"name\":\"Product 2\",\"creationDate\":\""
                 + currentDateAsStr + "\",\"group\":{\"id\":2,\"name\":\"Group 2\"}}]}}";
 
         Order order = new Order(1L, new Customer(1L, "Franco", new Address("rua", "cidade", "9800989")));
         order.addProduct(createProductWithGroup(1L, 1L));
         order.addProduct(createProductWithGroup(2L, 2L));
 
         jacksonSerialization.from(order).include("products", "products.group").serialize();
         assertThat(jsonResult(), is(equalTo(expectedResult)));
     }
 
     @Test
     public void shouldSerializeParentFields() {
         String expectedResult = "{\"hardDisk\":{\"capacity\":2987000009,\"id\":1,\"name\":\"Samsumg ZTX A9000\"}}";
 
         HardDisk hd = new HardDisk(1L, "Samsumg ZTX A9000", 2987000009L);
 
         jacksonSerialization.from(hd).serialize();
         assertThat(jsonResult(), is(equalTo(expectedResult)));
     }
 
     @Test
     public void shouldExcludeParentField() {
         String expectedResult = "{\"hardDisk\":{\"capacity\":2987000009,\"name\":\"Samsumg ZTX A9000\"}}";
 
         HardDisk hd = new HardDisk(1L, "Samsumg ZTX A9000", 2987000009L);
 
         jacksonSerialization.from(hd).exclude("id").serialize();
         assertThat(jsonResult(), is(equalTo(expectedResult)));
     }
 
     @Test
     public void shouldExcludeField() {
         String expectedResult = "{\"product\":{\"id\":1,\"name\":\"Product 1\",\"creationDate\":\"" + currentDateAsStr
                 + "\",\"group\":{\"name\":\"Group 1\"}}}";
 
         Group group = new Group(1L, "Group 1");
         Product product = new Product(1L, "Product 1", currentDate, group);
 
         jacksonSerialization.from(product).include("group").exclude("group.id").serialize();
         assertThat(jsonResult(), is(equalTo(expectedResult)));
     }
 
     @Test
     public void shouldExcudeHierarchicalField() {
         String expectedResult = "{\"order\":{\"id\":1,\"customer\":{\"id\":1,\"name\":\"Franco\"},"
                 + "\"delivery\":{\"street\":\"delivery street\",\"city\":\"Bristol\",\"zipCode\":\"09887990\"},"
                 + "\"products\":[{\"id\":1,\"name\":\"Product 1\",\"creationDate\":\"" + currentDateAsStr
                 + "\",\"group\":{\"name\":\"Group 1\"}},{\"id\":2,\"name\":\"Product 2\",\"creationDate\":\""
                 + currentDateAsStr + "\",\"group\":{\"name\":\"Group 2\"}}]}}";
 
         Order order = new Order(1L, new Customer(1L, "Franco", new Address("rua", "cidade", "9800989")), new Address(
                 "delivery street", "Bristol", "09887990"));
         order.addProduct(createProductWithGroup(1L, 1L));
         order.addProduct(createProductWithGroup(2L, 2L));
 
         jacksonSerialization.from(order).include("customer", "delivery", "products", "products.group")
                 .exclude("products.group.id").serialize();
         assertThat(jsonResult(), is(equalTo(expectedResult)));
     }
 
     @Test
     public void shouldSerializeWithouRoot() {
         String expectedResult = "{\"id\":1,\"name\":\"Product 1\",\"creationDate\":\"" + currentDateAsStr
                 + "\",\"group\":{\"name\":\"Group 1\"}}";
 
         Group group = new Group(1L, "Group 1");
         Product product = new Product(1L, "Product 1", currentDate, group);
 
         jacksonSerialization.withoutRoot().from(product).include("group").exclude("group.id").serialize();
         assertThat(jsonResult(), is(equalTo(expectedResult)));
     }
 
     @Test
     public void shouldSerializeIndented() {
        String expectedResult = "{\n  \"product\" : {\n    \"id\" : 1,\n    \"name\" : \"Product 1\",\n    \"creationDate\" : \"" + currentDateAsStr + "\",\n    \"group\" : {\n      \"id\" : 1,\n      \"name\" : \"Group 1\"\n    }\n  }\n}";
 
         Group group = new Group(1L, "Group 1");
         Product product = new Product(1L, "Product 1", currentDate, group);
 
         jacksonSerialization.indented().from(product).include("group").serialize();
         assertThat(jsonResult(), is(equalTo(expectedResult)));
     }
 
    @Test
    public void shouldSerializeObjectAttribute() {
        String expectedResult = "{\"product\":{\"id\":1,\"name\":\"Product 1\",\"creationDate\":\"" + currentDateAsStr
                + "\",\"data\":\"data object for product\"}}";

        Product product = new Product(1L, "Product 1", currentDate);
        product.setData("data object for product");

        jacksonSerialization.from(product).serialize();
        assertThat(jsonResult(), is(equalTo(expectedResult)));
    }

 }
