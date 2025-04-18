package fctreddit.server.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.logging.Logger;
import java.util.*;

/**
 * <p>
 * A class to perform service discovery, based on periodic service contact
 * endpoint announcements over multicast communication.
 * </p>
 *
 * <p>
 * Servers announce their *name* and contact *uri* at regular intervals. The
 * server actively collects received announcements.
 * </p>
 *
 * <p>
 * Service announcements have the following format:
 * </p>
 *
 * <p>
 * &lt;service-name-string&gt;&lt;delimiter-char&gt;&lt;service-uri-string&gt;
 * </p>
 */
public class Discovery {
    private static final Logger Log = Logger.getLogger(Discovery.class.getName());

    static {
        // addresses some multicast issues on some TCP/IP stacks
        System.setProperty("java.net.preferIPv4Stack", "true");
        // summarizes the logging format
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
    }

    // The pre-aggreed multicast endpoint assigned to perform discovery.
    // Allowed IP Multicast range: 224.0.0.1 - 239.255.255.255
    static final public InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("226.226.226.226", 2266);
    static final int DISCOVERY_ANNOUNCE_PERIOD = 1000;
    static final int DISCOVERY_RETRY_TIMEOUT = 5000;
    static final int MAX_DATAGRAM_SIZE = 65536;

    // Used separate the two fields that make up a service announcement.
    private static final String DELIMITER = "\t";

    private final InetSocketAddress addr;
    private final String serviceName;
    private final String serviceURI;
    private final MulticastSocket ms;
    private final Map<String, List<String>> services;

    /**
     * @param serviceName the name of the service to announce
     * @param serviceURI  an uri string - representing the contact endpoint of the
     *                    service being announced
     * @throws IOException
     * @throws UnknownHostException
     * @throws SocketException
     */
    public Discovery(InetSocketAddress addr, String serviceName, String serviceURI)
            throws SocketException, UnknownHostException, IOException {
        this.addr = addr;
        this.serviceName = serviceName;
        this.serviceURI = serviceURI;
        this.services = new HashMap<>();

        if (this.addr == null) {
            throw new RuntimeException("A multinet address has to be provided.");
        }

        this.ms = new MulticastSocket(addr.getPort());
        this.ms.joinGroup(addr, NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));

    }

    public Discovery(InetSocketAddress addr) throws IOException {
        this(addr, null, null);
    }

    /**
     * Starts sending service announcements at regular intervals...
     *
     * @throws IOException
     */
    public void start() {
        // If this discovery instance was initialized with information about a service,
        // start the thread that makes the
        // periodic announcement to the multicast address.
        if (this.serviceName != null && this.serviceURI != null) {

            Log.info(String.format("Starting Discovery announcements on: %s for: %s -> %s", addr, serviceName,
                    serviceURI));

            byte[] announceBytes = String.format("%s%s%s", serviceName, DELIMITER, serviceURI).getBytes();
            DatagramPacket announcePkt = new DatagramPacket(announceBytes, announceBytes.length, addr);

            try {
                // start thread to send periodic announcements
                new Thread(() -> {
                    for (;;) {
                        try {
                            ms.send(announcePkt);
                            Thread.sleep(DISCOVERY_ANNOUNCE_PERIOD);
                        } catch (Exception e) {
                            e.printStackTrace();
                            // do nothing
                        }
                    }
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // start thread to collect announcements received from the network.
        new Thread(() -> {
            DatagramPacket pkt = new DatagramPacket(new byte[MAX_DATAGRAM_SIZE], MAX_DATAGRAM_SIZE);
            for (;;) {
                try {
                    pkt.setLength(MAX_DATAGRAM_SIZE);
                    ms.receive(pkt);
                    String msg = new String(pkt.getData(), 0, pkt.getLength());
                    String[] msgElems = msg.split(DELIMITER);
                    if (msgElems.length == 2) { // periodic announcement
                        System.out.printf("FROM %s (%s) : %s\n", pkt.getAddress().getHostName(),
                                pkt.getAddress().getHostAddress(), msg);
                        // TODO: to complete by recording the received information
                        synchronized (services) {
                            services.putIfAbsent(msgElems[0], new ArrayList<>());
                            if(!services.get(msgElems[0]).contains(msgElems[1])) {
                                services.get(msgElems[0]).add(msgElems[1]);
                            }
                            synchronized (this) {
                                this.notifyAll();
                            }
                        }

                    }
                } catch (IOException e) {
                    // do nothing
                }
            }
        }).start();
    }

    /**
     * Returns the known services.
     *
     * @param serviceName the name of the service being discovered
     * @param minReplies  - minimum number of requested URIs. Blocks until the
     *                    number is satisfied.
     * @return an array of URI with the service instances discovered.
     *
     */
    public synchronized URI[] knownUrisOf(String serviceName, int minReplies) {
        List<String> serviceUris;

        synchronized (services) {
            serviceUris = services.computeIfAbsent(serviceName, k -> new ArrayList<>());

        }
            while (serviceUris.size() < minReplies) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return new URI[0];
                }
            }

        List<URI> uriList = new ArrayList<>();
        for (String s : serviceUris) {
            try {
                uriList.add(URI.create(s));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return uriList.toArray(new URI[0]);
    }

    // Main just for testing purposes
    public static void main(String[] args) throws Exception {
        Discovery discovery = new Discovery(DISCOVERY_ADDR, "test",
                "http://" + InetAddress.getLocalHost().getHostAddress());
        discovery.start();
    }
}