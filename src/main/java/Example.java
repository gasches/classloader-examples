import java.io.PrintWriter;
import java.util.function.Consumer;

public class Example implements Consumer<PrintWriter> {

    @Override
    public void accept(PrintWriter writer) {
        writer.println("Hello, class loader!");
    }
}
