package ch.nblotti.leonidas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import javax.jms.ConnectionFactory;
import java.time.format.DateTimeFormatter;


@SpringBootApplication
@EnableJms
@EnableCaching
@EnableScheduling
@PropertySource(value = "classpath:override.properties", ignoreResourceNotFound = true)
public class LeonidasApplication {

  public static void main(String[] args) {
    SpringApplication.run(LeonidasApplication.class, args);
  }

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Bean
  public DefaultJmsListenerContainerFactory factory(ConnectionFactory connectionFactory,
                                                    DefaultJmsListenerContainerFactoryConfigurer configurer) {
    DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();

    // This provides all boot's default to this factory, including the message converter
    configurer.configure(factory, connectionFactory);
    factory.setPubSubDomain(true);
    // You could still override some of Boot's default if necessary.
    return factory;
  }


  @Bean
  public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {

    JmsTemplate jmsTemplate = new JmsTemplate();
    jmsTemplate.setConnectionFactory(connectionFactory);
    jmsTemplate.setMessageConverter(jacksonJmsMessageConverter());
    jmsTemplate.setPubSubDomain(true);
    return jmsTemplate;
  }


  @Bean // Serialize message content to json using TextMessage
  public MessageConverter jacksonJmsMessageConverter() {
    MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
    converter.setTargetType(MessageType.TEXT);
    converter.setTypeIdPropertyName("_type");
    return converter;
  }

  @Bean
  public RestTemplate restTemplate() {
    RestTemplate rt = new RestTemplate();
    rt.getMessageConverters().add(new StringHttpMessageConverter());
    return rt;

  }

  @Bean
  public DateTimeFormatter dateTimeFormatter() {
    return DateTimeFormatter.ofPattern("dd.MM.yyyy");
  }

  @Bean
  public DateTimeFormatter quoteDateTimeFormatter() {
    return    DateTimeFormatter.ofPattern("yyyy-MM-dd");
  }



}
