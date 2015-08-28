package org.wso2.carbon.mediation.camel;

import org.apache.camel.Exchange;
import org.apache.camel.util.ObjectHelper;
import org.apache.log4j.Logger;
import org.wso2.carbon.api.CarbonMessage;
import org.wso2.carbon.common.CarbonMessageImpl;
import org.wso2.carbon.http.netty.common.Constants;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Carbon-Camel header transformation
 */
public class CarbonCamelMessageUtil {

    private static Logger log = Logger.getLogger(CarbonCamelMessageUtil.class);

    //get carbon headers from client request and set in the camel exchange in message
    public void setCamelHeadersToClientRequest(Exchange exchange, Map<String, Object> transportHeaders,
                                               CarbonMessage request) {
        //exchange.getIn().setHeaders(transportHeaders);

        ConcurrentHashMap<String, Object> headers = new ConcurrentHashMap<>();

        if (request.getProperty("HTTP_METHOD") != null) {
            headers.put(Exchange.HTTP_METHOD, request.getProperty("HTTP_METHOD"));
        }

        // strip query parameters from the uri
        String s = request.getURI();
        if (s.contains("?")) {
            s = ObjectHelper.before(s, "?");
        }

        // we want the full path for the url, as the client may provide the url in the HTTP headers as absolute or relative, eg
        //   /foo
        //   http://servername/foo
        String http = request.getProtocol() + "://";
        if (!s.startsWith(http)) {
            s = http + transportHeaders.get("Host") + s;
        }

        headers.put(Exchange.HTTP_URL, s);
        // uri is without the host and port
        URI uri = null;
        try {
            uri = new URI(request.getURI());
        } catch (URISyntaxException e) {
            log.error("Could not decode the URI in the message : " + request.getURI());
        }
        // uri is path and query parameters
        headers.put(Exchange.HTTP_URI, uri.getPath());
        //HTTP_PATH vs HTTP_URI ?
        headers.put(Exchange.HTTP_PATH, uri.getPath());

        if (uri.getQuery() != null) {
            headers.put(Exchange.HTTP_QUERY, uri.getQuery());
        }
        if (uri.getRawQuery() != null) {
            headers.put(Exchange.HTTP_RAW_QUERY, uri.getRawQuery());
        }
        if (transportHeaders.get("Content-Type") != null) {
            headers.put(Exchange.CONTENT_TYPE, transportHeaders.get("Content-Type"));
        }
        if (transportHeaders.get("SOAPAction") != null) {
            headers.put(Exchange.SOAP_ACTION, transportHeaders.get("SOAPAction"));
        }

        Iterator it = transportHeaders.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();

            if ("Content-Type".equals(pair.getKey()) || "SOAPAction".equals(pair.getKey())) {
                //these headers are already added
            } else {
                headers.put((String) pair.getKey(), pair.getValue());
            }
            it.remove();
        }

        exchange.getIn().setHeaders(headers);
    }

    //get camel headers from mediated request and set in carbon message
    public void setCarbonHeadersToBackendRequest(Exchange exchange, String host, int port, String uri) {

        CarbonMessageImpl request = (CarbonMessageImpl) exchange.getIn().getBody();
        Map<String, Object> headers = exchange.getIn().getHeaders();

        if (request != null) {

            ConcurrentHashMap<String, Object> carbonBackEndRequestHeaders = new ConcurrentHashMap<>();

            request.setHost(host);
            request.setPort(port);
            request.setURI(uri);

            Iterator it = headers.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String key = (String) pair.getKey();
                if (key.equals(Exchange.CONTENT_TYPE)) {
                    carbonBackEndRequestHeaders.put("Content-Type", pair.getValue());
                } else if (key.equals(Exchange.SOAP_ACTION)) {
                    carbonBackEndRequestHeaders.put("SOAPAction", pair.getValue());
                } else if (key.startsWith("Camel")) {
                    //skip these headers
                } else {
                    carbonBackEndRequestHeaders.put(key, pair.getValue());
                }
                it.remove();
            }

            if (port != 80) {
                carbonBackEndRequestHeaders.put("Host", host + ":" + port);
            } else {
                carbonBackEndRequestHeaders.put("Host", host);
            }

            request.setProperty(Constants.TRANSPORT_HEADERS, carbonBackEndRequestHeaders);
        }
    }

    //get carbon headers from backend response and set in camel exchange out message
    public void setCamelHeadersToBackendResponse(Exchange exchange, Map<String, Object> transportHeaders) {
        exchange.getOut().setHeaders(transportHeaders);
    }

}
