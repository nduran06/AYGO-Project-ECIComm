package com.aygo.eciComm.config;

import java.util.TimeZone;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class JacksonConfig {

	@Bean
	public ObjectMapper objectMapper() {

		ObjectMapper mapper = new ObjectMapper();

		// The JavaTimeModule provides serializers and deserializers for all Java time
		// classes
		mapper.registerModule(new JavaTimeModule());

		// Disable the default timestamp-based date rendering
		// --> This tells Jackson to use ISO-8601 formatted strings instead
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		// Configure timezone handling
		mapper.setTimeZone(TimeZone.getDefault());

		return mapper;
	}
}