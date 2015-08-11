/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.http.netty.sender.disruptor;


import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.log4j.Logger;
import org.wso2.carbon.api.CarbonMessage;
import org.wso2.carbon.api.TransportSender;
import org.wso2.carbon.http.netty.common.Constants;
import org.wso2.carbon.http.netty.common.Util;
import org.wso2.carbon.http.netty.listener.disruptor.SourceHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class Sender extends TransportSender {
    private static Logger log = Logger.getLogger(Sender.class);

    private List<CarbonMessage> carbonMessageQueue = new ArrayList<CarbonMessage>();

    public Sender() {
        super(Constants.PROTOCOL_NAME);
    }

    public boolean init() {
        return true;
    }



    public boolean send(final CarbonMessage msg)  {
        SourceHandler srcHandler = (SourceHandler) msg.getProperty(
                   Constants.PROTOCOL_NAME, Constants.SRC_HNDLR);
        if(msg.getStatus()==Constants.HEADERS){
            HttpRequest httpRequest = createHttpRequest(msg);
            srcHandler.getChannel().write(httpRequest);

        }else if(msg.getStatus() == Constants.BODY && msg.getEvent() instanceof  LastHttpContent) {
            srcHandler.getChannel().writeAndFlush(msg.getEvent());

        }else if(msg.getStatus() == Constants.BODY && msg.getEvent() instanceof  HttpContent){
            srcHandler.getChannel().write(msg.getEvent());
        }
        return false;
    }


    public boolean sendBack(CarbonMessage msg) {
        final ChannelHandlerContext inboundChCtx = (ChannelHandlerContext)
                   msg.getProperty(Constants.PROTOCOL_NAME, Constants.CHNL_HNDLR_CTX);
        if(msg.getStatus()==Constants.HEADERS){
            final HttpResponse response = createHttpResponse(msg);
            inboundChCtx.write(response);
        }else {
            inboundChCtx.writeAndFlush(msg.getEvent());
        }
        return false;
    }

    private HttpRequest createHttpRequest(CarbonMessage msg) {

        HttpMethod httpMethod = new HttpMethod((String) msg.getProperty(Constants.PROTOCOL_NAME,
                                                                        Constants.HTTP_METHOD));

        HttpVersion httpVersion = new HttpVersion((String) msg.getProperty(Constants.PROTOCOL_NAME,
                                                                           Constants.HTTP_VERSION), true);

        HttpRequest outgoingRequest =
                   new DefaultHttpRequest(httpVersion, httpMethod, msg.getURI(), false);


        Map headers = (Map) msg.getProperty(Constants.PROTOCOL_NAME,
                                            Constants.TRANSPORT_HEADERS);

        Util.setHeaders(outgoingRequest, headers);

        return outgoingRequest;
    }

    private HttpResponse createHttpResponse(CarbonMessage msg) {
        HttpVersion httpVersion = new HttpVersion(Util.getStringValue(msg,
                                                                      Constants.HTTP_VERSION, HTTP_1_1.text()), true);

        int statusCode = (Integer) Util.getIntValue(msg, Constants.HTTP_STATUS_CODE, 200);

        HttpResponseStatus httpResponseStatus = new HttpResponseStatus(statusCode,
                                                                       HttpResponseStatus.valueOf(statusCode).reasonPhrase());

        DefaultHttpResponse outgoingResponse = new DefaultHttpResponse(httpVersion,
                                                                       httpResponseStatus, false);

        Map<String, String> headerMap = (Map<String, String>) msg.getProperty(
                   Constants.PROTOCOL_NAME, Constants.TRANSPORT_HEADERS);

        Util.setHeaders(outgoingResponse, headerMap);

        return outgoingResponse;
    }

    public void addToQueue(CarbonMessage carbonMessage){
        carbonMessageQueue.add(carbonMessage);
    }
}
