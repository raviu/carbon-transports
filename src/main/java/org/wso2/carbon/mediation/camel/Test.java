package org.wso2.carbon.mediation.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

/**
 *
 */
public class Test {
    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();

        context.addRoutes(new RouteBuilder() {
            public void configure() {
/*
                from("wso2-gw:http://localhost:9090/one")
                        .to("wso2-gw:http://localhost:8080/ravi");
*/
                from("wso2-gw:http://localhost:9090/service")
                        .choice()
                            .when(header("routeId").regex("r1"))
                                .to("wso2-gw:http://localhost:8080/echo/backend")
                            .when(header("routeId").regex("r2"))
                                .to("wso2-gw:http://localhost:7070/test/endpoint")
                            .otherwise()
                                .to("wso2-gw:http://localhost:6060/endpoint/default");
            }
        });
        context.start();

    }
}
