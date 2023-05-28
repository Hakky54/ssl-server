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
        return new UndertowServer(sslFactory, port, responseBody);
    }

}
