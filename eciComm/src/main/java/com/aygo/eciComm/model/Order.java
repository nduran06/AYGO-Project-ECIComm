package com.aygo.eciComm.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.aygo.eciComm.model.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class Order extends Component {

	private String userId;
	private List<OrderItem> items;
	private BigDecimal totalAmount;
	private OrderStatus status;
	private String shippingAddress;
	private String billingAddress;
	private String paymentMethod;
	private Instant orderDate;
	private String trackingNumber;

	@DynamoDbPartitionKey
	@DynamoDbAttribute("orderId")
	@JsonProperty("id")
	@Override
	public String getId() {
		return this.id;
	}

	@DynamoDbAttribute("userId")
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@DynamoDbAttribute("items")
	public List<OrderItem> getItems() {
		return items;
	}

	public void setItems(List<OrderItem> items) {
		this.items = items;
	}

	@DynamoDbAttribute("totalAmount")
	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	@DynamoDbAttribute("status")
	public OrderStatus getOrderStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	@DynamoDbAttribute("shippingAddress")
	public String getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(String shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	@DynamoDbAttribute("billingAddress")
	public String getBillingAddress() {
		return billingAddress;
	}

	public void setBillingAddress(String billingAddress) {
		this.billingAddress = billingAddress;
	}

	@DynamoDbAttribute("paymentMethod")
	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	@DynamoDbAttribute("orderDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
	public Instant getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Instant orderDate) {
		this.orderDate = orderDate;
	}

	@DynamoDbAttribute("trackingNumber")
	public String getTrackingNumber() {
		return trackingNumber;
	}

	public void setTrackingNumber(String trackingNumber) {
		this.trackingNumber = trackingNumber;
	}

	/*@DynamoDbBeforeWrite
	public void beforeWrite() {
		super.beforeWrite();
		if (this.orderId == null) {
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
			String randomPart = UUID.randomUUID().toString().substring(0, 8);
			this.orderId = String.format("ORD_%s_%s", timestamp, randomPart);
			this.setId(this.orderId);
		}
		if (this.orderDate == null) {
			this.orderDate = Instant.now();
		}
	}*/
}