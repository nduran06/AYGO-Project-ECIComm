package com.aygo.eciComm.model;

import java.time.Instant;
import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.aygo.eciComm.model.enums.ItemStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbVersionAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class BaseItem {

	protected String id;
	private String type;
	private ItemStatus status;
	private Instant createdAt;
	private Instant updatedAt;
	private String createdBy;
	private String updatedBy;
	private Long version;

	public abstract String getId();

	public void setId(String id) {
		this.id = id;
	}

	@DynamoDbAttribute("type")
	@JsonProperty("type")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@DynamoDbAttribute("status")
	@JsonProperty("status")
	public ItemStatus getStatus() {
		return status;
	}

	public void setStatus(ItemStatus status) {
		this.status = status;
	}

	@DynamoDbAttribute("createdAt")
	@JsonProperty("createdAt")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	@DynamoDbAttribute("updatedAt")
	@JsonProperty("updatedAt")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	@DynamoDbAttribute("createdBy")
	@JsonProperty("createdBy")
	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	@DynamoDbAttribute("updatedBy")
	@JsonProperty("updatedBy")
	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	@DynamoDbVersionAttribute
	@JsonProperty("version")
	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public void beforeWrite() {
		Instant now = Instant.now();
		if (this.createdAt == null) {
			this.createdAt = now;
		}
		this.updatedAt = now;

		// Get current user from security context if available
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String currentUser = auth != null ? auth.getName() : "system";

		if (this.createdBy == null) {
			this.createdBy = currentUser;
		}
		this.updatedBy = currentUser;

		// Ensure status is set
		if (this.status == null) {
			this.status = ItemStatus.ACTIVE;
		}
	}

	// Implement equals and hashCode using pattern matching
	@Override
	public boolean equals(Object obj) {
		return obj instanceof BaseItem other && Objects.equals(id, other.id) && Objects.equals(type, other.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, type);
	}

	@Override
	public String toString() {
		return new StringBuilder(getClass().getSimpleName()).append(" {").append("id='")
				.append(id != null ? id : "null").append("', ").append("type='").append(type != null ? type : "null")
				.append("', ").append("status=").append(status != null ? status : "null").append(", ")
				.append("createdAt=").append(createdAt != null ? createdAt : "null").append(", ").append("updatedAt=")
				.append(updatedAt != null ? updatedAt : "null").append("}").toString();
	}
}
