/*
 * Copyright 2023 Thunderberry.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.altindag.ssl.server.service;

import nl.altindag.ssl.SSLFactory;

/**
 * @author Hakan Altindag
 */
public interface Server {

    void stop();

    static Server createDefault(SSLFactory sslFactory) {
        return createDefault(sslFactory, 8443);
    }

    static Server createDefault(SSLFactory sslFactory, int port) {
        return createDefault(sslFactory, port, "Hello World!");
    }

    static Server createDefault(SSLFactory sslFactory, int port, String responseBody) {
        return builder(sslFactory)
                .withPort(port)
                .withResponseBody(responseBody)
                .withDelayedResponseTime(-1)
                .build();
    }

    static Builder builder(SSLFactory sslFactory) {
        return new Builder(sslFactory);
    }

    class Builder {

        private final SSLFactory sslFactory;
        private int port = 8443;
        private String responseBody = "Hello World!";
        private int delayResponseTimeInMilliseconds = -1;

        private Builder(SSLFactory sslFactory) {
            this.sslFactory = sslFactory;
        }

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public Builder withResponseBody(String responseBody) {
            this.responseBody = responseBody;
            return this;
        }

        public Builder withDelayedResponseTime(int delayResponseTimeInMilliseconds) {
            this.delayResponseTimeInMilliseconds = delayResponseTimeInMilliseconds;
            return this;
        }

        public Server build() {
            return new NettyServer(sslFactory, port, responseBody, delayResponseTimeInMilliseconds);
        }

    }

}
