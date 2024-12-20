package com.poojith.microservices.order;

import com.poojith.microservices.order.stubs.InventoryClientStub;
import io.restassured.RestAssured;
import org.flywaydb.database.mysql.MySQLConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.testcontainers.containers.MySQLContainer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class OrderServiceApplicationTests {
	@ServiceConnection
	static MySQLContainer mySQLContainer = new MySQLContainer("mysql:8.3.0");


	@LocalServerPort
	private int port;

	@BeforeEach
	void setUp() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
	}

	static {
		mySQLContainer.start();
	}

	@Test
	void shouldPlaceOrder() {
		String placeOrderJSON= """
				{
				"skuCode" : "iphone_15",
				"price" : 1000,
				"quantity" : 8
				}
				""";
		InventoryClientStub.stubInventoryCall("iphone_15",1);
		var responseBodyString = RestAssured.given()
				.contentType("application/json")
				.body(placeOrderJSON)
				.when()
				.post("/api/order")
				.then()
				.statusCode(202)
				.extract()
				.body()
				.asString();
		assertThat(responseBodyString).isEqualTo("Order placed successfully");
	}

}
