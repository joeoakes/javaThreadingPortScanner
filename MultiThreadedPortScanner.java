import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadedPortScanner {
    private static final int TIMEOUT = 200; // Milliseconds
    private static final int MAX_PORT = 65535;
    private static final int THREAD_POOL_SIZE = 100; // Adjust this based on your system's capabilities

    // Using ConcurrentHashMap to handle concurrent access
    private Map<Integer, Boolean> portStatus = new ConcurrentHashMap<>();

    public void scanPorts(String host) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        for (int port = 1; port <= MAX_PORT; port++) {
            final int currentPort = port;
            executor.submit(() -> {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(host, currentPort), TIMEOUT);
                    portStatus.put(currentPort, true); // Port is open
                } catch (IOException e) {
                    portStatus.put(currentPort, false); // Port is closed or filtered
                }
            });
        }

        executor.shutdown(); // Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be accepted.
        while (!executor.isTerminated()) {
            // Wait until all tasks are finished
        }

        printResults();
    }

    private void printResults() {
        portStatus.forEach((port, isOpen) -> {
            if (isOpen) {
                System.out.println("Port " + port + " is open.");
            }
        });
    }

    public static void main(String[] args) {
        MultiThreadedPortScanner scanner = new MultiThreadedPortScanner();
        String host = "127.0.0.1"; // Replace with the IP address you want to scan
        scanner.scanPorts(host);
    }
}
