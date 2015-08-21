package org.wso2.carbon.mediation.camel;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultAsyncProducer;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.api.CarbonCallback;
import org.wso2.carbon.api.CarbonMessage;
import org.wso2.carbon.common.CarbonMessageImpl;
import org.wso2.carbon.controller.CamelMediationEngine;
import org.wso2.carbon.http.netty.common.Constants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The CamelMediation producer handle the request and response with the backend.
 */
public class CamelMediationProducer extends DefaultAsyncProducer {
    private static final transient Log LOG = LogFactory.getLog(CamelMediationProducer.class);
    private CamelMediationEngine engine;
    private static String ENGINE_PROTOCOL = "http";
    private Map<String, CamelEndPointURL> endPointURLMap = new ConcurrentHashMap<String, CamelEndPointURL>();

    public CamelMediationProducer(CamelMediationEndpoint endpoint) {
        super(endpoint);
    }

    public CamelMediationProducer(CamelMediationEndpoint endpoint, CamelMediationEngine engine) {
        super(endpoint);
        this.engine = engine;
    }

    public boolean process(Exchange exchange, AsyncCallback callback) {
        //change the header parameters according to the routed endpoint url
        setCarbonHeaders(exchange);
        engine.getSender()
              .send((CarbonMessage) exchange.getIn().getBody(), new NettyHttpBackEndCallback(exchange, callback));
        return false;
    }

    private void setCarbonHeaders(Exchange exchange) {
        CarbonMessageImpl request;
        //TODO change
        Boolean routingMatched = (Boolean) exchange.getProperties().get(Exchange.FILTER_MATCHED);

        if (routingMatched) {
            String camelToEndPoint = (String) exchange.getProperties().get(Exchange.TO_ENDPOINT);

            Map<String, Object> headers = exchange.getIn().getHeaders();
            request = (CarbonMessageImpl) exchange.getIn().getBody();
            if (request != null) {
                //decode the camel endpoint url and populate the carbon message routing parameters
                CamelEndPointURL camelEndPointURLob = endPointURLMap.get(camelToEndPoint);
                if (null == camelEndPointURLob) {
                    String proxyURL = ObjectHelper.after(camelToEndPoint, "//");
                    String temp1 = ObjectHelper.after(proxyURL, "//");
                    String temp2 = ObjectHelper.after(temp1, ":");
                    camelEndPointURLob = new CamelEndPointURL(ObjectHelper.before(temp1, ":"),
                                                              Integer.parseInt(ObjectHelper.before(temp2, "/")),
                                                              "/" + ObjectHelper.after(temp2, "/"));
                    endPointURLMap.put(camelToEndPoint, camelEndPointURLob);

                    String host = ObjectHelper.before(temp1, ":");
                    int port = Integer.parseInt(ObjectHelper.before(temp2, "/"));
                    String uri = "/" + ObjectHelper.after(temp2, "/");

                    request.setHost(host);
                    request.setPort(port);
                    request.setURI(uri);
                    headers.put("Host", host + ":" + port);
                } else {
                    request.setHost(camelEndPointURLob.getHost());
                    request.setPort(camelEndPointURLob.getPort());
                    request.setURI(camelEndPointURLob.getUri());
                }

                headers.put("Host", request.getHost() + ":" + request.getPort());
                request.setProperty(request.getProtocol(), Constants.TRANSPORT_HEADERS, headers);
            }
        }
    }

    @Override public Endpoint getEndpoint() {
        return (CamelMediationEndpoint) super.getEndpoint();
    }

    private class NettyHttpBackEndCallback implements CarbonCallback {
        private final Exchange exchange;
        private final AsyncCallback callback;

        public NettyHttpBackEndCallback(Exchange exchange, AsyncCallback callback) {
            this.exchange = exchange;
            this.callback = callback;
        }

        @Override public void done(CarbonMessage cMsg) {
            if (cMsg != null) {
                Map<String, Object> transportHeaders =
                        (Map<String, Object>) cMsg.getProperty(cMsg.getProtocol(), Constants.TRANSPORT_HEADERS);
                if (transportHeaders != null) {
                    exchange.getOut().setHeaders(transportHeaders);
                    exchange.getOut().setBody(cMsg);
                } else {
                    log.warn("Backend response : Received empty headers in carbon message...");
                }
            } else {
                log.warn("Backend response not received for request...");
            }
            callback.done(false);
        }
    }
}