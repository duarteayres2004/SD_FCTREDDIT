package fctreddit.server.rest;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

import fctreddit.impl.server.rest.ContentsResource;
import fctreddit.server.discovery.Discovery;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;


public class ContentsServer {

    private static final Logger Log = Logger.getLogger(ContentsServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
    }

    public static final int PORT = 8081;
    public static final String SERVICE = "ContentService";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";

    public static void main(String[] args) {
        try {
            ResourceConfig config = new ResourceConfig();
            config.register(ContentsResource.class);

            String ip = InetAddress.getLocalHost().getHostAddress();
            String serverURI = String.format(SERVER_URI_FMT, ip, PORT);


            Discovery discovery = new Discovery(Discovery.DISCOVERY_ADDR, "Content", serverURI);
            discovery.start();

            JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config);

            Log.info(String.format("%s Server ready @ %s\n", SERVICE, serverURI));
        } catch (Exception e) {
            Log.severe(e.getMessage());
        }
    }
}
