package com.aygo.eciComm.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.aygo.eciComm.model.enums.UserRole;
import com.aygo.eciComm.model.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class User extends Component {

	private String email;
	private String firstName;
	private String lastName;
	private String phoneNumber;
	private UserStatus status;
	private Set<UserRole> roles;
	private String defaultShippingAddress;
	private String defaultBillingAddress;
	private Map<String, String> preferences;
	private Boolean emailVerified;
	private Boolean phoneVerified;
	private Instant lastLoginAt;

	@DynamoDbPartitionKey
	@DynamoDbAttribute("userId")
	@JsonProperty("id")
	@Override
	public String getId() {
		return id;
	}

	@DynamoDbAttribute("email")
	@JsonProperty("email")
	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email != null ? email.toLowerCase() : null;
	}

	@DynamoDbAttribute("firstName")
	@JsonProperty("firstName")
	@NotBlank(message = "First name is required")
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@DynamoDbAttribute("lastName")
	@JsonProperty("lastName")
	@NotBlank(message = "Last name is required")
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@DynamoDbAttribute("phoneNumber")
	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	@DynamoDbAttribute("status")
	public UserStatus getUserStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	@DynamoDbAttribute("roles")
	public Set<UserRole> getRoles() {
		return roles != null ? roles : new HashSet<>();
	}

	public void setRoles(Set<UserRole> roles) {
		this.roles = roles;
	}

	@DynamoDbAttribute("defaultShippingAddress")
	public String getDefaultShippingAddress() {
		return defaultShippingAddress;
	}

	public void setDefaultShippingAddress(String defaultShippingAddress) {
		this.defaultShippingAddress = defaultShippingAddress;
	}

	@DynamoDbAttribute("defaultBillingAddress")
	public String getDefaultBillingAddress() {
		return defaultBillingAddress;
	}

	public void setDefaultBillingAddress(String defaultBillingAddress) {
		this.defaultBillingAddress = defaultBillingAddress;
	}

	@DynamoDbAttribute("preferences")
	public Map<String, String> getPreferences() {
		return preferences != null ? preferences : new HashMap<>();
	}

	public void setPreferences(Map<String, String> preferences) {
		this.preferences = preferences;
	}

	@DynamoDbAttribute("emailVerified")
	public Boolean getEmailVerified() {
		return emailVerified;
	}

	public void setEmailVerified(Boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	@DynamoDbAttribute("phoneVerified")
	public Boolean getPhoneVerified() {
		return phoneVerified;
	}

	public void setPhoneVerified(Boolean phoneVerified) {
		this.phoneVerified = phoneVerified;
	}

	@DynamoDbAttribute("lastLoginAt")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
	public Instant getLastLoginAt() {
		return lastLoginAt;
	}

	public void setLastLoginAt(Instant lastLoginAt) {
		this.lastLoginAt = lastLoginAt;
	}

	@Override
	public void beforeWrite() {
		super.beforeWrite();
		if (this.id == null) {
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
			String randomPart = UUID.randomUUID().toString().substring(0, 8);
			this.id = String.format("USR_%s_%s", timestamp, randomPart);
			this.setId(this.id);
		}

		// Ensure roles is never empty
		if (this.roles == null || this.roles.isEmpty()) {
			this.roles = new HashSet<>();
			this.roles.add(UserRole.CUSTOMER);
		}

		// Set other defaults
		if (this.status == null) {
			this.status = UserStatus.ACTIVE;
		}
		if (this.emailVerified == null) {
			this.emailVerified = false;
		}
		if (this.phoneVerified == null) {
			this.phoneVerified = false;
		}
	}

	@JsonIgnore
	public String getFullName() {
		return String.format("%s %s", firstName, lastName).trim();
	}
}