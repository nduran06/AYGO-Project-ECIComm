package com.aygo.eciComm.controller;

import java.net.URI;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aygo.eciComm.exception.UserNotFoundException;
import com.aygo.eciComm.exception.UserValidationException;
import com.aygo.eciComm.model.User;
import com.aygo.eciComm.model.enums.UserStatus;
import com.aygo.eciComm.model.response.ErrorResponse;
import com.aygo.eciComm.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

	private static final Logger log = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserService userService;

	@PostMapping
	public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
		User created = userService.createUser(user);
		return ResponseEntity.created(URI.create("/api/v1/users/" + created.getId())).body(created);
	}

	@GetMapping("/{userId}")
	public ResponseEntity<User> getUser(@PathVariable String userId) {
		return ResponseEntity.ok(userService.getUser(userId));
	}

	@GetMapping("/email/{email}")
	public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
		return ResponseEntity.ok(userService.getUserByEmail(email));
	}

	@PutMapping("/{userId}")
	public ResponseEntity<User> updateUser(@PathVariable String userId, @Valid @RequestBody User user) {
		return ResponseEntity.ok(userService.updateUser(userId, user));
	}

	@PatchMapping("/{userId}/status")
	public ResponseEntity<User> updateStatus(@PathVariable String userId, @RequestBody UserStatus status) {
		return ResponseEntity.ok(userService.updateStatus(userId, status));
	}

	@PostMapping("/{userId}/verify-email")
	public ResponseEntity<User> verifyEmail(@PathVariable String userId) {
		return ResponseEntity.ok(userService.verifyEmail(userId));
	}

	@PostMapping("/{userId}/verify-phone")
	public ResponseEntity<User> verifyPhone(@PathVariable String userId) {
		return ResponseEntity.ok(userService.verifyPhone(userId));
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
		ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), LocalDateTime.now());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(UserValidationException.class)
	public ResponseEntity<ErrorResponse> handleValidation(UserValidationException ex) {
		ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), LocalDateTime.now());
		return ResponseEntity.badRequest().body(error);
	}
}
