/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.transport.http.netty.internal.config;

import java.util.Collections;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB representation of the Netty transport configuration.
 */
@SuppressWarnings("unused")
@XmlRootElement(name = "transports")
@XmlAccessorType(XmlAccessType.FIELD)
public class TransportsConfiguration {

    @XmlElementWrapper(name = "listeners")
    @XmlElement(name = "listener")
    private Set<ListenerConfiguration> listenerConfigurations;

//    private List<SenderConfiguration> senders;


    public Set<ListenerConfiguration> getListenerConfigurations() {
        return Collections.unmodifiableSet(listenerConfigurations);
    }

    public void setListenerConfigurations(Set<ListenerConfiguration> listenerConfigurations) {
        this.listenerConfigurations = Collections.unmodifiableSet(listenerConfigurations);
    }
}
