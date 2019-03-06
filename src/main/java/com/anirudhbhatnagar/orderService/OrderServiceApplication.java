package com.anirudhbhatnagar.orderService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import com.anirudhbhatnagar.orderService.dto.order.CustomerOrderDetails;
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;

@SpringBootApplication
@EnableFeignClients
@EnableCircuitBreaker
public class OrderServiceApplication {
	
	@Bean
	  public ServletRegistrationBean getServlet() {
	    HystrixMetricsStreamServlet streamServlet = new HystrixMetricsStreamServlet();
	    ServletRegistrationBean registrationBean = new ServletRegistrationBean(streamServlet);
	    registrationBean.setLoadOnStartup(1);
	    registrationBean.addUrlMappings("/hystrix.stream");
	    registrationBean.setName("HystrixMetricsStreamServlet");
	    return registrationBean;
	  }

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}

}
