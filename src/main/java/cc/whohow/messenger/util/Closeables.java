package cc.whohow.messenger.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Closeables {
    private static final Logger log = LogManager.getLogger();

    public static void close(AutoCloseable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Throwable e) {
            log.error("close {}", closeable);
            log.error(e.getMessage(), e);
        }
    }
}
