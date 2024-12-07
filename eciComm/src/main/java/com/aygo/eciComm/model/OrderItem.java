package com.aygo.eciComm.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class OrderItem extends Component {
	
    private String orderId;
    private String productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private String productName;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("orderItemId")
	@JsonProperty("id")
    @Override
    public String getId() {
        return this.id;
    }
    
    @DynamoDbSortKey
    @DynamoDbAttribute("orderId")
    public String getOrderId() {
        return this.orderId;
    }
    
    public void setOrderId(String orderId) {
    	this.orderId = orderId;
    }
    
    // Getters and Setters with DynamoDB annotations
    @DynamoDbAttribute("productId")
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @DynamoDbAttribute("quantity")
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @DynamoDbAttribute("unitPrice")
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    @DynamoDbAttribute("subtotal")
    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @DynamoDbAttribute("productName")
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
