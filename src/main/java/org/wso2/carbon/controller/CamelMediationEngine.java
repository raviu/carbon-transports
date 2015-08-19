package org.wso2.carbon.controller;

import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.log4j.Logger;
import org.wso2.carbon.api.CarbonMessage;
import org.wso2.carbon.api.TransportSender;
import org.wso2.carbon.common.CarbonMessageImpl;
import org.wso2.carbon.http.netty.common.Constants;
import org.wso2.carbon.http.netty.sender.Sender;
import org.wso2.carbon.mediation.camel.CamelMediationConsumer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Camel routing is added here
 */
public class CamelMediationEngine implements org.wso2.carbon.api.Engine {

    private static Logger log = Logger.getLogger(POCMediationEngine.class);

    private static String ENGINE_PROTOCOL = "http";
    private TransportSender sender;
    private CamelContext context = new DefaultCamelContext();
    //    private CamelMediationConsumer consumer;
    //    private CamelMediationProducer producer;
    private CarbonMessage outMsg;
    private final ConcurrentHashMap<String, CamelMediationConsumer> consumers =
            new ConcurrentHashMap<String, CamelMediationConsumer>();

    public CamelMediationEngine(Sender sender) {
        this.sender = sender;
        this.sender.setEngine(this);
    }

    public boolean init() {
/*        try {
            initCamelMediation();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return true;
    }

    private void initCamelMediation() throws Exception {
        context = new DefaultCamelContext();

        context.addRoutes(new RouteBuilder() {
            public void configure() {

                from("wso2-gw:http://localhost:9090/service").choice().when(header("routeId").regex("r1"))
                                                             .to("wso2-gw:http://localhost:8080/echo/backend")
                                                             .when(header("routeId").regex("r2"))
                                                             .to("wso2-gw:http://localhost:7070/test/endpoint")
                                                             .otherwise()
                                                             .to("wso2-gw:http://localhost:6060/endpoint/default");
            }
        });
        context.start();
    }

    //TODO change the receive method as receive(CarbonMessage cmsg, AsyncCallback processor)
    public boolean receive(CarbonMessage cmsg, CarbonAsyncCallback requestCallback) {
        if (cmsg.getDirection() == CarbonMessageImpl.IN) {

            //start mediation
            if (log.isDebugEnabled()) {
                log.debug("Channel: {} received body: {}" + cmsg.getId().toString());
            }
            Map<String, Object> transportHeaders =
                    (Map<String, Object>) cmsg.getProperty(cmsg.getProtocol(), Constants.TRANSPORT_HEADERS);
            CamelMediationConsumer consumer = decideConsumer(cmsg.getProtocol(), transportHeaders);
            if (consumer != null) {
                final Exchange exchange = consumer.getEndpoint().createExchange(transportHeaders,cmsg);
                exchange.setPattern(ExchangePattern.InOut);
                // we want to handle the UoW
                try {
                    consumer.createUoW(exchange);
                } catch (Exception e) {
                    log.error("Unit of Work creation failed");
                }
                processAsynchronously(exchange,consumer,requestCallback);
            }
        } else {
            sender.sendBack(cmsg);
        }
        return true;
    }

    private void processAsynchronously(final Exchange exchange, final CamelMediationConsumer consumer, CarbonAsyncCallback requestCallback) {
        consumer.getAsyncProcessor().process(exchange, new AsyncCallback() {
            @Override public void done(boolean done) {
                //TODO need to send the response back
                //System.out.println("done");
                requestCallback.done(exchange.getOut());
            }
        });
    }

    private CamelMediationConsumer decideConsumer(String protocol, Map<String, Object> headers) {
        //TODO//change to variable to uri and add it to Transport headers
        String messageURL = protocol + "//" + headers.get("Host") + headers.get("URI");
        messageURL = "http://localhost:9090/service";
        for (String key : consumers.keySet()) {
            if (key.contains(messageURL)) {
                return consumers.get(key);
            }
        }
        log.info("No route found for the message URL : " + messageURL);
        return null;
    }

    public void sendMessage(CarbonMessage cmsg) {
        sender.send(cmsg);
    }

    public void sendBackMessage(CarbonMessage cmsg){
        sender.sendBack(cmsg);
    }

    public void addConsumer(String key, CamelMediationConsumer consumer) {
        consumers.put(key, consumer);
    }

    public void removeConsumer(String endpointKey) {

    }

}