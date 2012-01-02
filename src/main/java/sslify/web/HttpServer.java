package sslify.web;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import sslify.ConfigProperties;
import sslify.ConfigPropertiesFactory;

import javax.servlet.http.HttpServlet;
import java.util.Arrays;

@Slf4j
public class HttpServer {
    private final int port;
    private final HttpServlet x509servlet;
    static private final String PROP_PORT = "socket.port";

    @Inject
    HttpServer(ConfigPropertiesFactory configPropertiesFactory,
               X509Servlet x509Servlet)
            throws ConfigProperties.ConfigLoadingException {
        ConfigProperties config = configPropertiesFactory.get(ConfigProperties.Domain.HTTP_SERVER);
        this.port = Integer.parseInt(config.getProperty(PROP_PORT));
        this.x509servlet = x509Servlet;
    }

    public void run() throws InterruptedException {
        Server server = null;
        try {
            server = new Server(port);

            HandlerList handlers = new HandlerList();
            DefaultHandler defaultHandler = new DefaultHandler();

            ServletContextHandler context =
                            new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");

            handlers.setHandlers(new Handler[] {context, defaultHandler});

            server.setHandler(context);

            context.addServlet(new ServletHolder(x509servlet),"/x509/*");

            server.start();
            HttpServer.log.info("started");
            server.join();
        } catch (Exception e) {
            log.error("{} {}", e.toString(), Arrays.toString(e.getStackTrace()));
            System.err.flush();
            System.exit(1);
        } finally {
            if (server != null) {
                server.join();
            }
        }
    }
}
