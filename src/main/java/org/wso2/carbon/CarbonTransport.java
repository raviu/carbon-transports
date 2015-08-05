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

package org.wso2.carbon;

public abstract class CarbonTransport {

    /**
     * Unique ID representing a transport
     */
    protected String id;

    protected State state = State.UNINITIALIZED;

    public CarbonTransport(String id) {
        this.id = id;
    }

    private enum State {
        UNINITIALIZED, STARTED, STOPPED, IN_MAINTENANCE;

        @Override
        public String toString() {
            return name();
        }
    }

    public String getId() {
        return id;
    }


    protected void start() {
        if (state.equals(State.UNINITIALIZED) || state.equals(State.IN_MAINTENANCE) || state.equals(State.STOPPED)) {
            state = State.STARTED;
        } else {
            throw new IllegalStateException("Cannot start transport " + id + ". Current state: " + state);
        }
    }

    protected void stop() {
        if (state.equals(State.STARTED)) {
            state = State.STOPPED;
        } else {
            throw new IllegalStateException("Cannot stop transport " + id + ". Current state: " + state);
        }
    }

    protected void beginMaintenance() {
        if (state.equals(State.STARTED)) {
            state = State.IN_MAINTENANCE;
        } else {
            throw new IllegalStateException("Cannot put transport " + id + " into maintenance. Current state: " + state);
        }
    }

    protected void endMaintenance() {
        if (state.equals(State.IN_MAINTENANCE)) {
            state = State.STARTED;
        } else {
            throw new IllegalStateException("Cannot end maintenance of transport " + id + ". Current state: " + state);
        }
    }

}
