package sslify;

import org.simpleframework.http.core.Container;

public interface HttpServer extends Container {
    void run() throws Exception;
}
