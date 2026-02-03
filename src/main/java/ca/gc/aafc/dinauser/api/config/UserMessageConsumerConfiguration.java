package ca.gc.aafc.dinauser.api.config;

import javax.inject.Named;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import ca.gc.aafc.dina.messaging.config.RabbitMQConsumerConfiguration;
import ca.gc.aafc.dina.messaging.config.RabbitMQQueueProperties;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "dina.messaging", name = "isConsumer", havingValue = "true")
public class UserMessageConsumerConfiguration extends RabbitMQConsumerConfiguration {

  public UserMessageConsumerConfiguration(@Named("userMessageQueueProperties")
                                                RabbitMQQueueProperties queueProperties) {
    super(queueProperties);
  }

  @Bean("userMessageQueue")
  @Override
  public Queue createQueue() {
    return super.createQueue();
  }

  @Bean("userMessageDeadLetterQueue")
  @Override
  public Queue createDeadLetterQueue() {
    return super.createDeadLetterQueue();
  }
}
