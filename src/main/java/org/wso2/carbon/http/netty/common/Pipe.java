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

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpContent;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Pipe {
    private static Logger log = Logger.getLogger(Pipe.class);

    private String name = "Buffer";

    private byte[] content;

    private BlockingQueue<HttpContent> contentQueue = new LinkedBlockingQueue<HttpContent>();


    private Map trailingheaders = new ConcurrentHashMap<String, String>();

    private Lock lock = new ReentrantLock();

    private Condition readCondition = lock.newCondition();

    public Pipe(String name) {
        this.name = name;

    }


    public void writeContent(DefaultHttpContent defaultHttpContent) {
        lock.lock();
        try {
            ByteBuf buf = defaultHttpContent.content();
            content = new byte[buf.readableBytes()];
            buf.readBytes(content);
            readCondition.signalAll();

        } finally {
            lock.unlock();
        }

    }

    public void writeFullContent(byte[] bytes) {
        content = bytes;
    }


    public byte[] readContent() {
        lock.lock();
        try {
            waitForData();
            return content;
        } catch (IOException e) {
            log.error("Error while reading content.", e);
        } finally {
            lock.unlock();
        }
        return content;
    }

    public void addTrailingHeader(String key, String value) {
        trailingheaders.put(key, value);
    }


    public Map getTrailingheaderMap() {
        return trailingheaders;
    }


    private void waitForData() throws IOException {
        lock.lock();
        try {
            try {
                while (content == null) {
                    readCondition.await();
                }
            } catch (InterruptedException e) {
                throw new IOException("Interrupted while waiting for data");
            }
        } finally {
            lock.unlock();
        }
    }

    public HttpContent getContent() {
        try {
            return contentQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addContent(HttpContent defaultHttpContent) {
        contentQueue.add(defaultHttpContent);
    }
}
