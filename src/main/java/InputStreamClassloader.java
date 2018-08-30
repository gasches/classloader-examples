import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamClassloader extends ClassLoader {
    private static final int BUF_SIZE = 1024;

    private final BufferedInputStream is;
    private final String className;

    private Class<?> clazz;

    public InputStreamClassloader(ClassLoader parent, InputStream is, String name) {
        super(parent);
        this.is = new BufferedInputStream(is);
        this.className = name;
    }

    public synchronized Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            if (!className.equals(name)) {
                return super.loadClass(name);
            }

            if (clazz != null) {
                return clazz;
            }

            System.out.println("Load class " + name);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] bbuf = new byte[BUF_SIZE];
            int n;
            while ((n = is.read(bbuf)) > 0) {
                baos.write(bbuf, 0, n);
            }

            byte[] classBytes = baos.toByteArray();
            clazz = defineClass(name, classBytes, 0, classBytes.length);
            resolveClass(clazz);
            return clazz;
        } catch (IOException e) {
            e.printStackTrace();
            throw new ClassNotFoundException(e.getMessage(), e);
        }
    }
}
