package sslify;

import com.google.inject.Inject;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class HttpServerImpl implements HttpServer {
    private final int port;
    private final X509CertificateFactory x509CertificateFactory;
    static private final String PROP_PORT = "socket.port";

    @Inject
    HttpServerImpl(ConfigPropertiesFactory configPropertiesFactory, X509CertificateFactory x509CertificateFactory) {
        ConfigProperties config = configPropertiesFactory.get(ConfigProperties.Domains.HTTP_SERVER);
        port = Integer.parseInt(config.getProperty(PROP_PORT));
        this.x509CertificateFactory = x509CertificateFactory;
    }

    @Override
    public void run() {
        try {
            Connection connection = new SocketConnection(this);
            SocketAddress address = new InetSocketAddress(port);
            connection.connect(address);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.err.flush();
            System.exit(1);
        }
    }

    @Override
    public void handle(Request request, Response response) {
        PrintStream ps = null;
        try {
            ps = response.getPrintStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ps != null) {
            long time = System.currentTimeMillis();
            response.set("Content-Type", "text/plain");
            response.setDate("Date", time);

            final String user = request.getPath().getName();
            try {
                ps.println(x509CertificateFactory.get(user).toPEM());
            } catch (CertInfoFactoryLDAPImpl.MissingUserException e) {
                this.fail(404, e, response);
            } catch (Exception e) {
                this.fail(500, e, response);
            }
            ps.close();
        }
    }

    private void fail(int code, Exception e, Response response) {
        PrintStream ps = null;
        try {
            ps = response.getPrintStream();
            response.setCode(code);
            response.setText(e.toString());
            ps.println("Oops! Error " + code);
            e.printStackTrace(ps);

        } catch (IOException ignored) {
        } finally {
            if (ps != null)
                ps.close();
        }
    }
}
