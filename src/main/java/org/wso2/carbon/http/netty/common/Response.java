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
package org.wso2.carbon.http.netty.common;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.HashMap;
import java.util.Map;

public class Response {
    private Map<String, String> httpheaders = new HashMap<String, String>();
    private Map<String, String> httptrailingHeaders = new HashMap<String, String>();

    private HttpResponseStatus status = HttpResponseStatus.OK;

    private ChannelHandlerContext inboundChannelHandlerContext;

    private FullHttpResponse fullHttpResponse;
    private byte[] contentBytes;

    private String statusLine = "OK";

    private Pipe pipe;

    public Pipe getPipe() {
        return pipe;
    }

    public void setPipe(Pipe pipe) {
        this.pipe = pipe;
    }

    public ChannelHandlerContext getInboundChannelHandlerContext() {
        return inboundChannelHandlerContext;
    }

    public void setInboundChannelHandlerContext(ChannelHandlerContext inboundChannelHandlerContext) {
        this.inboundChannelHandlerContext = inboundChannelHandlerContext;
    }

    public Map<String, String> getHttptrailingHeaders() {
        return httptrailingHeaders;
    }

    public void addHttpTrailingheaders(String key, String value) {
        this.httptrailingHeaders.put(key, value);
    }

    public byte[] getContentBytes() {
        return contentBytes;
    }

    public void setContentBytes(byte[] contentBytes) {
        this.contentBytes = contentBytes;
    }

    public Map<String, String> getHttpheaders() {
        return httpheaders;
    }

    public void addHttpheaders(String key, String value) {
        this.httpheaders.put(key, value);
    }


    public FullHttpResponse getFullHttpResponse() {
        return fullHttpResponse;
    }

    public void setFullHttpResponse(FullHttpResponse fullHttpResponse) {
        this.fullHttpResponse = fullHttpResponse;
    }

    public HttpResponseStatus getStatus() {
        return status;
    }


    public void addHeader(String name, String value) {

        httpheaders.put(name, value);

    }

    public void setStatus(HttpResponseStatus status) {
        this.status = status;
    }

    public void removeHeader(String name) {
        httpheaders.remove(name);
    }


    public String getStatusLine() {
        return statusLine;
    }

    public void setStatusLine(String statusLine) {
        this.statusLine = statusLine;
    }

    public String getHeader(String name) {
        return httpheaders.get(name);
    }
}
