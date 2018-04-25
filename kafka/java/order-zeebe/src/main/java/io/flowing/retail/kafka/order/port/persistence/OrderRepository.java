package io.flowing.retail.kafka.order.port.persistence;

import org.springframework.data.repository.CrudRepository;

import io.flowing.retail.kafka.order.domain.Order;

public interface OrderRepository extends CrudRepository<Order, String> {

}
