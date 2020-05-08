 package oshop.web.converter.order;
 
 import org.springframework.stereotype.Component;
 import oshop.model.ShippingAddress;
 import oshop.model.ShippingType;
 import oshop.web.converter.BaseEntityConverter;
 import oshop.web.converter.EntityConverter;
 
 import javax.annotation.Resource;
 
 @Component
 public class ShippingAddressForOrderToDTOConverter extends BaseEntityConverter<ShippingAddress, Integer> {
 
     @Resource(name = "shippingTypeToDTOConverter")
     private EntityConverter<ShippingType, Integer> shippingTypeConverter;
 
     @Override
     protected Class<ShippingAddress> entityClass() {
         return ShippingAddress.class;
     }
 
     @Override
     protected void convert(ShippingAddress entity, ShippingAddress convertedEntity) throws Exception {
         convertedEntity.setShippingType(shippingTypeConverter.convert(entity.getShippingType()));
         convertedEntity.setAddress(entity.getAddress());
        convertedEntity.setCity(entity.getCity());
         convertedEntity.setPhone(entity.getPhone());
     }
 }
