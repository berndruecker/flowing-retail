package io.flowing.retail.order.port.message;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface Inputs {
    String INPUT_ORDER_PLACED_EVENTS = "input-order-placed-events";
    String INPUT_ALL_EVENTS = "input-all-events";

    @Input(INPUT_ORDER_PLACED_EVENTS)
    SubscribableChannel inputOrderPlacedEvents();

    @Input(INPUT_ALL_EVENTS)
    SubscribableChannel inputAllEvents();
}
