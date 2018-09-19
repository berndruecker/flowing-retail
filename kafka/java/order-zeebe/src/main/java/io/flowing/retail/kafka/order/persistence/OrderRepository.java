package io.flowing.retail.kafka.order.persistence;

import org.springframework.data.repository.CrudRepository;

import io.flowing.retail.kafka.order.domain.Order;

public interface OrderRepository extends CrudRepository<Order, String> {

}
