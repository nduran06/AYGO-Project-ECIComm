package com.aygo.eciComm.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AWSConfig {
	@Value("${aws.region}")
	private String awsRegion;

	// S3 Client configuration
	@Bean
	public S3Client s3Client() {
		return S3Client.builder().region(Region.of(awsRegion))
				// This will use the default credential chain (ideal for both local dev and AWS deployment)
				.credentialsProvider(DefaultCredentialsProvider.create())
				// Adding some sensible defaults for timeouts
				.overrideConfiguration(ClientOverrideConfiguration.builder().apiCallTimeout(Duration.ofSeconds(30))
						.retryPolicy(RetryPolicy.builder().numRetries(3).build()).build())
				.build();
	}
}