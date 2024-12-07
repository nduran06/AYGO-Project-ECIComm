package com.aygo.eciComm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sagemakerruntime.SageMakerRuntimeClient;

@Configuration
public class SageMakerConfig {

	@Value("${aws.region}")
	private String awsRegion;

	@Bean
	public SageMakerRuntimeClient sageMakerRuntimeClient() {
		return SageMakerRuntimeClient.builder().region(Region.US_EAST_1)
				.credentialsProvider(DefaultCredentialsProvider.create()).build();
	}
}