/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.controller;

import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.apache.log4j.Logger;
import org.wso2.carbon.api.EngineAPI;
import org.wso2.carbon.context.CommonContext;
import org.wso2.carbon.http.netty.common.Constants;
import org.wso2.carbon.http.netty.common.Request;
import org.wso2.carbon.http.netty.sender.Sender;

import java.util.Iterator;
import java.util.Map;

public class Engine implements EngineAPI {
    private static Logger log = Logger.getLogger(Engine.class);

    private Sender sender;

    public Engine(Sender sender) {
        this.sender = sender;
    }

    public boolean init() {
        return true;
    }

    public boolean receive(CommonContext ctx) {
        log.info("Engine receive");
        Request request = (Request) ctx.getProperty(Constants.INCOMING_REQUEST);
        log.info(request.getHttpMethod() + " " + request.getUri() + " " + request.getHttpVersion());

        log.info("Forward Message");
        ctx.setProperty(Constants.TO_SCHEME, "http");
        ctx.setProperty(Constants.TO_HOST, "localhost");
        ctx.setProperty(Constants.TO_PORT, 8280);

        Map headers = (Map) request.getHttpHeaders();
        headers.put("Custom-Header", "Hello");

        ctx.setProperty(Constants.OUTGOING_REQUEST, createHttpRequest(ctx, "/services/echo"));

        sender.send(ctx);

        return true;
    }

    private HttpRequest createHttpRequest(CommonContext context, String uri) {

        Request incomingRequest = (Request) context.getProperty(Constants.INCOMING_REQUEST);

        HttpMethod httpMethod = new HttpMethod(incomingRequest.getHttpMethod().name());
        HttpVersion httpVersion = new HttpVersion(incomingRequest.getHttpVersion().text(), true);
        Map headers = (Map) incomingRequest.getHttpHeaders();

        HttpRequest outgoinRequest = new DefaultHttpRequest(httpVersion, httpMethod, uri);

        if (headers != null) {
            Iterator iterator = headers.keySet().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                outgoinRequest.headers().add(key, headers.get(key));
            }
        }

        return outgoinRequest;
    }


}
