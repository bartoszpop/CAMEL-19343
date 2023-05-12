package io.github.bartoszpop.camel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootTest
class Camel19343Test {

  @Autowired
  private ProducerTemplate producerTemplate;

  @Test
  void someRoute_conditionMet() {
    // Act
    var response = producerTemplate.requestBody("direct:some", (Object) null);

    // Assert
    assertEquals("Condition met.", response);
  }

  @Test
  void otherRoute_conditionNotMet() {
    // Act
    var response = producerTemplate.requestBody("direct:other", (Object) null);

    // Assert
    assertEquals("Condition not met.", response);
  }

  @Configuration
  @EnableAutoConfiguration
  static class Config {

    private static final String ROUTE_TEMPLATE_ID = "routeTemplate";

    private static final String FROM_PARAMETER = "from";

    private static final String CHOICE_PARAMETER = "choice";

    @Bean
    public RouteBuilder sampleRouteTemplate() {
      return new EndpointRouteBuilder() {
        @Override
        public void configure() {
          routeTemplate(ROUTE_TEMPLATE_ID)
              .templateParameter(FROM_PARAMETER)
              .templateParameter(CHOICE_PARAMETER)
              .from("{{" + FROM_PARAMETER + "}}")
              // @formatter:off
              .choice()
                .when(simple("{{" + CHOICE_PARAMETER + "}}").isEqualTo("some")) // This predicated is shared between templated routes and initialized with the first route's parameter
                  .setBody(simple("Condition met."))
                .otherwise()
                  .setBody(simple("Condition not met."));
              // @formatter:on
        }
      };
    }

    @Bean
    public RouteBuilder someTemplatedRoute() {
      return new EndpointRouteBuilder() {
        @Override
        public void configure() {
          templatedRoute(ROUTE_TEMPLATE_ID).routeId("someRoute")
              .parameter(FROM_PARAMETER, "direct:some")
              .parameter(CHOICE_PARAMETER, "some");
        }
      };
    }

    @Bean
    public RouteBuilder otherTemplatedRoute() {
      return new EndpointRouteBuilder() {
        @Override
        public void configure() {
          templatedRoute(ROUTE_TEMPLATE_ID).routeId("otherRoute")
              .parameter(FROM_PARAMETER, "direct:other")
              .parameter(CHOICE_PARAMETER, "other");
        }
      };
    }
  }
}
