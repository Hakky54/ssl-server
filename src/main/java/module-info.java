module nl.altindag.ssl.server {

    requires transitive nl.altindag.ssl;
    requires undertow.core;
    requires xnio.api;

    exports nl.altindag.ssl.server.service;

}