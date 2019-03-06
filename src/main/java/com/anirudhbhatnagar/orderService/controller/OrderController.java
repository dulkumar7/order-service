package com.anirudhbhatnagar.orderService.controller;

import com.anirudhbhatnagar.orderService.domain.Item;
import com.anirudhbhatnagar.orderService.domain.Order;
import com.anirudhbhatnagar.orderService.dto.customer.Customer;
import com.anirudhbhatnagar.orderService.dto.order.CustomerOrderDetails;
import com.anirudhbhatnagar.orderService.dto.product.Product;
import com.anirudhbhatnagar.orderService.dto.request.CustomerOrderRequest;
import com.anirudhbhatnagar.orderService.repository.OrderRepository;
import com.anirudhbhatnagar.orderService.service.ProductServiceProxy;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private OrderRepository orderRepository;
	private ProductServiceProxy productServiceProxy;

	@Autowired
	public OrderController(OrderRepository orderRepository, ProductServiceProxy productServiceProxy) {
		this.orderRepository = orderRepository;
		this.productServiceProxy = productServiceProxy;
	}

	@GetMapping // ("/orders")
	public List<CustomerOrderDetails> getCustomerOrders(@RequestParam String customerId) {
		final List<Order> order = orderRepository.findByCustomerId(customerId);
		return order.stream().map(o -> toCustomerOrderDetails(o)).collect(Collectors.toList());
	}

	@GetMapping("/{id}")
	@HystrixCommand(fallbackMethod = "fallbackGetOrders")
	public CustomerOrderDetails getOrders(@PathVariable("id") Long orderId) {
		final Order order = orderRepository.findById(orderId).orElse(null);
		if (order == null) {
			return null;
		}
		return toCustomerOrderDetails(order);
	}

	// fallback method of getOrders
	public CustomerOrderDetails fallbackGetOrders(Long orderId) {

		//CustomerOrderDetails orderDetails =
			
			return  CustomerOrderDetails.builder().orderId(00L).createdDate(null)
						.externalReference("not available").items(null).build();

	}

	private CustomerOrderDetails toCustomerOrderDetails(Order order) {
		return CustomerOrderDetails.builder().orderId(order.getId()).createdDate(order.getCreatedDate())
				.externalReference(order.getExternalReference()).items(toItemList(order.getItems())).build();
	}

	private List<com.anirudhbhatnagar.orderService.dto.product.Item> toItemList(List<Item> items) {
		return items.stream().map(item -> toItemDto(item)).collect(Collectors.toList());
	}

	private com.anirudhbhatnagar.orderService.dto.product.Item toItemDto(Item item) {
		return com.anirudhbhatnagar.orderService.dto.product.Item.builder()
				.product(productServiceProxy.getProduct(item.getProductId())).build();
	}

	@PostMapping // ("/orders")
	public Order save(@RequestBody CustomerOrderRequest request) {
		return orderRepository.save(
				Order.builder().customerId(request.getCustomerId()).externalReference(request.getExternalReference())
						.items((request.getItems() == null) ? null : toItems(request.getItems())).build());
	}

	private List<Item> toItems(List<com.anirudhbhatnagar.orderService.dto.request.Item> items) {
		return items.stream()
				.map(item -> Item.builder().productId(item.getProductId()).quantity(item.getQuantity()).build())
				.collect(Collectors.toList());
	}
}
