package com.aygo.eciComm.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aygo.eciComm.exception.UserNotFoundException;
import com.aygo.eciComm.exception.UserValidationException;
import com.aygo.eciComm.model.User;
import com.aygo.eciComm.model.enums.UserStatus;
import com.aygo.eciComm.repository.UserRepository;

@Service
public class UserService {

	private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private UserRepository userRepository;

	public User createUser(User user) {
		LOG.info("Creating new user with email: {}", user.getEmail());
		validateUser(user);

		// Check if email is already in use
		userRepository.findByEmail(user.getEmail()).ifPresent(existingUser -> {
			throw new UserValidationException("Email already registered");
		});

		return userRepository.save(user);
	}

	public User getUser(String userId) {
		LOG.debug("Fetching user: {}", userId);
		return userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
	}

	public User getUserByEmail(String email) {
		LOG.debug("Fetching user by email: {}", email);
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
	}

	public User updateUser(String userId, User userUpdates) {
		LOG.info("Updating user: {}", userId);
		User existingUser = getUser(userId);

		// Update only non-null fields
		if (userUpdates.getFirstName() != null) {
			existingUser.setFirstName(userUpdates.getFirstName());
		}
		if (userUpdates.getLastName() != null) {
			existingUser.setLastName(userUpdates.getLastName());
		}
		if (userUpdates.getPhoneNumber() != null) {
			existingUser.setPhoneNumber(userUpdates.getPhoneNumber());
			existingUser.setPhoneVerified(false); // Require re-verification
		}
		if (userUpdates.getDefaultShippingAddress() != null) {
			existingUser.setDefaultShippingAddress(userUpdates.getDefaultShippingAddress());
		}
		if (userUpdates.getDefaultBillingAddress() != null) {
			existingUser.setDefaultBillingAddress(userUpdates.getDefaultBillingAddress());
		}
		if (userUpdates.getPreferences() != null) {
			existingUser.getPreferences().putAll(userUpdates.getPreferences());
		}

		validateUser(existingUser);
		return userRepository.save(existingUser);
	}

	public User updateStatus(String userId, UserStatus newStatus) {
		LOG.info("Updating user status: {} to {}", userId, newStatus);
		User user = getUser(userId);
		user.setStatus(newStatus);
		return userRepository.save(user);
	}

	public User verifyEmail(String userId) {
		LOG.info("Verifying email for user: {}", userId);
		User user = getUser(userId);
		user.setEmailVerified(true);
		return userRepository.save(user);
	}

	public User verifyPhone(String userId) {
		LOG.info("Verifying phone for user: {}", userId);
		User user = getUser(userId);
		user.setPhoneVerified(true);
		return userRepository.save(user);
	}

	public void recordLogin(String userId) {
		LOG.info("Recording login for user: {}", userId);
		User user = getUser(userId);
		user.setLastLoginAt(Instant.now());
		userRepository.save(user);
	}

	private void validateUser(User user) {
		List<String> errors = new ArrayList<>();

		if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
			errors.add("Email is required");
		} else if (!isValidEmail(user.getEmail())) {
			errors.add("Invalid email format");
		}

		if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
			errors.add("First name is required");
		}

		if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
			errors.add("Last name is required");
		}

		if (!errors.isEmpty()) {
			throw new UserValidationException(String.join(", ", errors));
		}
	}

	private boolean isValidEmail(String email) {
		return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
	}
}