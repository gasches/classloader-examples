import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Application {

    private static final int DEFAULT_PORT = 8080;
    private static final int DEFAULT_NTHREADS = 5;

    public static void main(String[] args) throws Exception {
        int port = (args.length > 0) ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        int nThreads = (args.length > 1) ? Integer.parseInt(args[1]) : DEFAULT_NTHREADS;

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new ExecHandler());
        server.setExecutor(Executors.newFixedThreadPool(nThreads));
        server.start();
        System.out.printf("Server is started at port %d with %d threads.\n", port, nThreads);
    }

    static class ExecHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String className = path.replace("\\s", "");
            if (className.startsWith("/")) {
                className = className.substring(1);
            }
            if (className.isEmpty()) {
                writeResp(exchange, 400, "Undefined class name");
                return;
            }

            System.out.println("Handle request for class: " + className);

            InputStream is = exchange.getRequestBody();
            StringWriter sw = new StringWriter();
            PrintWriter writer = new PrintWriter(sw);
            try {
                InputStreamClassloader classloader = new InputStreamClassloader(is);
                Class<?> clazz = classloader.loadClass(className);
                Consumer<PrintWriter> consumer = (Consumer<PrintWriter>) clazz.newInstance();
                consumer.accept(writer);
            } catch (Exception e) {
                writeResp(exchange, 400, e.getMessage());
                return;
            }
            writeResp(exchange, 200, sw.toString());
        }

        private static void writeResp(HttpExchange exchange, int code, String str) throws IOException {
            byte[] bytes = str.getBytes(StandardCharsets.US_ASCII);
            exchange.sendResponseHeaders(code, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
                os.flush();
            }
        }
    }
}
