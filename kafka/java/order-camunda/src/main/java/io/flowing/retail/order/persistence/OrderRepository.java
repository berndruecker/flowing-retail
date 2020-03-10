package io.flowing.retail.order.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import io.flowing.retail.order.domain.Order;

@Component
public interface OrderRepository extends CrudRepository<Order, String> {

}
