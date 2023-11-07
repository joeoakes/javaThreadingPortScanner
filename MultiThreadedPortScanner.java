import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/*
ExecutorService to manage a pool of threads
ConcurrentHashMap to store the results of the scan
oop through the range of ports we want to scan and
submit a new task to the executor for each port
task tries to open a socket connection to the specified port
The Port is either open or closed
Each thread has its own stack space, typically around 1MB
 */

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
        int activeThreadCount = Thread.activeCount();

        // Print the number of active threads
        System.out.println("Number of active threads: " + activeThreadCount);

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
        long startTime = System.nanoTime(); //Start the timer
        MultiThreadedPortScanner scanner = new MultiThreadedPortScanner();
        String host = "127.0.0.1"; // Replace with the IP address you want to scan
        scanner.scanPorts(host);
        long endTime = System.nanoTime(); //End the timer
        long timeElapsed = (endTime - startTime) / 1_000_000;  //nanoseconds to milliseconds
        System.out.println("Execution time in milliseconds: " + timeElapsed);

    }
}
