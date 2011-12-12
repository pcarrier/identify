package sslify.web;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import sslify.*;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.security.GeneralSecurityException;

@Slf4j
public class X509Servlet extends HttpServlet {
    private final X509CertificateFactory x509CertificateFactory;

    @Inject
    X509Servlet(X509CertificateFactory x509CertificateFactory) {
        this.x509CertificateFactory = x509CertificateFactory;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        final String path = req.getPathInfo();
        final String user = path.substring(1);
        final long time = System.currentTimeMillis();
        PrintStream printStream = null;

        resp.setDateHeader("Date", time);
        resp.setContentType("text/plain");

        try {
            printStream = new PrintStream(resp.getOutputStream());
            if(!path.matches("/[a-z-]+")) {
                this.notFound(new Exception("Invalid username"), path, resp, printStream);
            }
            printStream.println(x509CertificateFactory.get(user).toPEM());
        } catch (SshPublicKey.SshPublicKeyLoadingException e) {
            this.notFound(e, path, resp, printStream);
        } catch (CertInfoFactory.MissingUserException e) {
            this.notFound(e, path, resp, printStream);
        } catch (CertInfoFactory.CachedFailureException e) {
            this.notFound(e, path, resp, printStream);
        } catch (GeneralSecurityException e) {
            this.internalError(e, path, resp, printStream);
        } catch (NamingException e) {
            this.internalError(e, path, resp, printStream);
        } catch (IOException e) {
            this.internalError(e, path, resp, printStream);
        } catch (ConfigProperties.ConfigLoadingException e) {
            this.internalError(e, path, resp, printStream);
        } finally {
            if (printStream != null) {
                printStream.close();
            }
        }
    }

    private void notFound(final Exception exception,
                          final String path,
                          final HttpServletResponse resp,
                          final PrintStream stream) {
        log.info("404 {} ({})", path, exception.toString());
        this.fail(HttpServletResponse.SC_NOT_FOUND, exception, resp, stream);
    }

    private void internalError(final Exception exception,
                               final String path,
                               final HttpServletResponse resp,
                               final PrintStream stream) {
        log.info("500 {} ({})", path, exception.toString());
        this.fail(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception, resp, stream);
    }

    private void fail(final int code,
                      final Exception exception,
                      final HttpServletResponse resp,
                      final PrintStream stream) {
        resp.setStatus(code);
        stream.print("Oops! Error ");
        stream.print(code);
        stream.println("!");
        exception.printStackTrace(stream);
    }
}
