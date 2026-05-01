package ca.gc.aafc.dina.user.api.config;

import jakarta.inject.Named;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import ca.gc.aafc.dina.messaging.config.RabbitMQQueueProperties;

@ConfigurationProperties(prefix = "dina.messaging.user")
@Component
@Named("userMessageQueueProperties")
public class UserMessageQueueProperties extends RabbitMQQueueProperties {
}
