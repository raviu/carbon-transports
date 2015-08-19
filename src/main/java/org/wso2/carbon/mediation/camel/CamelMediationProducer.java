package org.wso2.carbon.mediation.camel;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultAsyncProducer;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.api.CarbonMessage;
import org.wso2.carbon.common.CarbonMessageImpl;
import org.wso2.carbon.controller.CamelMediationEngine;
import org.wso2.carbon.http.netty.common.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * The CamelMediation producer.
 */
public class CamelMediationProducer extends DefaultAsyncProducer {
    private static final transient Log LOG = LogFactory.getLog(CamelMediationProducer.class);
    //private CamelMediationEndpoint endpoint;
    private CamelMediationEngine engine;
    private static String ENGINE_PROTOCOL = "http";
    //    private CarbonMessage cmsg;
    private Map<String, CamelEndPointURL> endPointURLMap = new HashMap<String, CamelEndPointURL>();

    public CamelMediationProducer(CamelMediationEndpoint endpoint) {
        super(endpoint);
    }

    public CamelMediationProducer(CamelMediationEndpoint endpoint, CamelMediationEngine engine) {
        super(endpoint);
        this.engine = engine;
    }

    public boolean process(Exchange exchange, AsyncCallback callback) {
        setCarbonHeaders(exchange);
        engine.sendMessage((CarbonMessage) exchange.getIn().getBody(),
                           new NettyHttpBackEndCallback(exchange, callback));
        //pass carbon message and call back handler

        return false;
    }

    private void setCarbonHeaders(Exchange exchange) {
        CarbonMessageImpl requst;

        Boolean routingMatched = (Boolean) exchange.getProperties().get(Exchange.FILTER_MATCHED);

        if (routingMatched) {
            String camelToEndPoint = (String) exchange.getProperties().get(Exchange.TO_ENDPOINT);

            Map<String, Object> headers = exchange.getIn().getHeaders();
            requst = (CarbonMessageImpl) exchange.getIn().getBody();
            if (requst != null) {
                //TODO
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

                    requst.setHost(host);
                    requst.setPort(port);
                    requst.setURI(uri);
                    headers.put("Host", host + ":" + port);
                } else {
                    requst.setHost(camelEndPointURLob.getHost());
                    requst.setPort(camelEndPointURLob.getPort());
                    requst.setURI(camelEndPointURLob.getUri());
                }
                headers.put("Host", requst.getHost() + ":" + requst.getPort());
                requst.setProperty(requst.getProtocol(), Constants.TRANSPORT_HEADERS, headers);
            }
        }
    }

    @Override public Endpoint getEndpoint() {
        return (CamelMediationEndpoint) super.getEndpoint();
    }

    private class NettyHttpBackEndCallback implements AsyncCallback{
        private final Exchange exchange;
        private final AsyncCallback callback;

        private NettyHttpBackEndCallback(Exchange exchange, AsyncCallback callback) {
            this.exchange = exchange;
            this.callback = callback;
        }

        @Override public void done(boolean b) {

        }
    }
}