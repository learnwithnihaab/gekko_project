package com.gekko.service;

import com.gekko.dto.OrderRequest;
import com.gekko.entity.OrderEntity;

public interface OrderService {
    OrderEntity createOrder(OrderRequest req);
}
