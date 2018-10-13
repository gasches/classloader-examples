import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class InputStreamClassloader extends ClassLoader {
    private static final int BUF_SIZE = 1024;

    private final BufferedInputStream is;

    public InputStreamClassloader(InputStream is) {
        if (is instanceof BufferedInputStream) {
            this.is = (BufferedInputStream) is;
        } else {
            this.is = new BufferedInputStream(is);
        }
    }

    public InputStreamClassloader(ClassLoader parent, InputStream is) {
        super(parent);
        this.is = new BufferedInputStream(is);
    }

    @Override
    protected Class<?> findClass(String name) {
        System.out.printf("Load class %s with InputStreamClassloader\n", name);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] bbuf = new byte[BUF_SIZE];
        int n;
        try {
            while ((n = is.read(bbuf)) > 0) {
                baos.write(bbuf, 0, n);
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
        byte[] classData = baos.toByteArray();
        return defineClass(name, classData, 0, classData.length);
    }
}
