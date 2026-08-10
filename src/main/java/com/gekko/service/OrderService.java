package com.gekko.service;

import com.gekko.dto.OrderRequest;
import com.gekko.entity.Customer;
import com.gekko.entity.OrderEntity;
import com.gekko.entity.Subscription;
import com.gekko.repository.CustomerRepository;
import com.gekko.repository.OrderRepository;
import com.gekko.repository.SubscriptionRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * OrderService implements the commerce-controller responsibilities in Gekko.
 * - persists incoming order
 * - creates initial subscription record
 * - publishes event to BRIM topic so BRIM can create contract
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public static final String BRIM_CONTRACT_TOPIC = "brim-contract-requests";

    public OrderService(OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        SubscriptionRepository subscriptionRepository,
                        KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public OrderEntity createAndProcessOrder(com.gekko.dto.OrderRequest req) {
        // 1. find or create customer
        Customer customer = customerRepository.findByExternalId(req.getCustomerExternalId())
                .orElseGet(() -> {
                    Customer c = new Customer();
                    c.setExternalId(req.getCustomerExternalId());
                    c.setName("Unknown");
                    c.setEmail("unknown@example.com");
                    return customerRepository.save(c);
                });

        // 2. create order record
        OrderEntity order = new OrderEntity();
        order.setExternalId(req.getExternalOrderId());
        order.setCustomer(customer);
        order.setProductCode(req.getProductCode());
        order.setAmount(req.getAmount());
        order.setCurrency(req.getCurrency());
        order.setStatus("CREATED");

        OrderEntity saved = orderRepository.save(order);

        // 3. create a subscription record in PENDING state
        Subscription sub = new Subscription();
        sub.setOrder(saved);
        sub.setStatus("PENDING");
        subscriptionRepository.save(sub);

        // 4. publish a message to BRIM topic to request contract creation asynchronously
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderExternalId", saved.getExternalId());
        payload.put("customerExternalId", customer.getExternalId());
        payload.put("productCode", saved.getProductCode());
        // real systems would include more fields & authentication

        kafkaTemplate.send(BRIM_CONTRACT_TOPIC, saved.getExternalId(), payload);

        // update order status
        saved.setStatus("SENT_TO_BRIM");
        return orderRepository.save(saved);
    }

    public Optional<OrderEntity> findByExternalId(String externalId) {
        return orderRepository.findByExternalId(externalId);
    }
}
