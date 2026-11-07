package de.kevin_stefan.minecraft.shared.common;

import java.util.logging.Logger;

public class SharedLogger {

    private final Logger logger;
    private boolean debug = false;

    public SharedLogger(Logger pluginLogger) {
        this.logger = pluginLogger;
    }

    public SharedLogger(Logger logger, boolean debug) {
        this.logger = logger;
        setDebug(debug);
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return debug;
    }

    public void info(String msg) {
        logger.info(msg);
    }

    public void warning(String msg) {
        logger.warning(msg);
    }

    public void severe(String msg) {
        logger.severe(msg);
    }

    public void debug(String msg) {
        try {
            StackWalker stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
            StackWalker.StackFrame frame = stackWalker.walk(stream -> stream.skip(1).limit(1).toList()).getFirst();
            String caller = String.format("L%s %s#%s: ", frame.getLineNumber(), frame.getDeclaringClass().getSimpleName(), frame.getMethodName());
            logger.info(caller + msg);
        } catch (Exception e) {
            logger.info("DEBUG: " + msg);
        }
    }

}
