package io.flowing.retail.checkout;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.ZeebeClientBuilder;
import io.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;

@Configuration
public class ZeebeClientConfiguration {

	@Value("${camunda.cloud.clusterId:#{null}}")
	private String clusterId;

	@Value("${camunda.cloud.baseUrl:zeebe.camunda.io}")
	private String baseUrl;

	@Value("${camunda.cloud.clientId:#{null}}")
	private String clientId;

	@Value("${camunda.cloud.clientSecret:#{null}}")
	private String clientSecret;

	@Value("${camunda.cloud.authUrl:'https://login.cloud.camunda.io/oauth/token'}")
	private String authUrl;

	@Bean
	public ZeebeClient zeebe() {
		if (clusterId==null) {
			System.out.println("*** Connect to Zeebe locally (no Camunda Cloud cluster id set) ***");
			
			ZeebeClient client = ZeebeClient.newClientBuilder().usePlaintext().build();
			System.out.println("*** Connected sucessfully to Zeebe locally ***");
			return client;
		} else {
			System.out.println("*** Connect to Camunda Cloud (cluster id "+clusterId+") ***");
			
			final String broker = clusterId + "." + baseUrl + ":443";
	
			final OAuthCredentialsProviderBuilder c = new OAuthCredentialsProviderBuilder();
			final OAuthCredentialsProvider cred = c.audience(clusterId + "." + baseUrl).clientId(clientId)
					.clientSecret(clientSecret).authorizationServerUrl(authUrl).build();
	
			final ZeebeClientBuilder clientBuilder = ZeebeClient.newClientBuilder().brokerContactPoint(broker)
					.credentialsProvider(cred);
	
			ZeebeClient client = clientBuilder.build();
	
			System.out.println("*** Connected sucessfully to Camunda Cloud ***");
	
			return client;
		}
	}

}
