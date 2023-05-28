module nl.altindag.ssl.server {

    requires transitive nl.altindag.ssl;
    requires io.netty.codec;
    requires io.netty.codec.http;
    requires io.netty.transport;
    requires io.netty.buffer;
    requires io.netty.handler;

    exports nl.altindag.ssl.server.service;

}