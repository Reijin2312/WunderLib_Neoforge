package de.ambertation.wunderlib.general;

import de.ambertation.wunderlib.WunderLib;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

/**
 * A wrapper class for Log4j logging functionality in Minecraft mods.
 * This class provides simplified logging methods for different log levels 
 * (DEBUG, INFO, WARN, ERROR) with various parameter options.
 * 
 * By default, it uses the WunderLib mod ID for logger identification,
 * but can be extended with custom mod IDs through the protected constructor.
 */
public class Logger {
    private final org.apache.logging.log4j.Logger LOGGER;

    /**
     * Creates a new Logger instance using the WunderLib mod ID.
     */
    public Logger() {
        this(WunderLib.MOD_ID);
    }

    /**
     * Creates a new Logger instance with a specified mod ID.
     * 
     * @param modID the mod ID to use for logger identification
     */
    protected Logger(String modID) {
        LOGGER = LogManager.getLogger(modID);
    }

    /**
     * Log a message with level DEBUG on this logger.
     *
     * @param message the message string to be logged
     */
    public void debug(String message) {
        LOGGER.log(Level.DEBUG, message);
    }

    /**
     * Log a message with parameters with level DEBUG on this logger.
     *
     * @param message the message string to be logged
     * @param params  the parameters to the message
     */
    public void debug(String message, Object... params) {
        LOGGER.log(Level.DEBUG, message, params);
    }

    /**
     * Log a message with level INFO on this logger.
     *
     * @param message the message string to be logged
     */
    public void info(String message) {
        LOGGER.log(Level.INFO, message);
    }

    /**
     * Log a message with parameters with level INFO on this logger.
     *
     * @param message the message string to be logged
     * @param params  the parameters to the message
     */
    public void info(String message, Object... params) {
        LOGGER.log(Level.INFO, message, params);
    }

    /**
     * Log a message with level WARN on this logger.
     *
     * @param message the message string to be logged
     */
    public void warn(String message) {
        LOGGER.log(Level.WARN, message);
    }


    /**
     * Log a message with level WARN on this logger.
     *
     * @param message the message string to be logged
     * @param params  the parameters to the message
     */
    public void warn(String message, Object... params) {
        LOGGER.log(Level.WARN, message, params);
    }


    /**
     * Log a message with level WARN on this logger.
     *
     * @param message the message string to be logged
     * @param ex      the exception to log, including its stack trace.
     */
    public void warn(String message, Exception ex) {
        LOGGER.log(Level.WARN, message, ex);
    }

    /**
     * Log a message with level WARN on this logger.
     *
     * @param message the message string to be logged
     * @param param   the parameter to the message
     * @param ex      the exception to log, including its stack trace.
     */
    public void warn(String message, Object param, Exception ex) {
        LOGGER.log(Level.WARN, message, param, ex);
    }

    /**
     * Log a message with level ERROR on this logger.
     *
     * @param message the message string to be logged
     */
    public void error(String message) {
        LOGGER.log(Level.ERROR, message);
    }

    /**
     * Log a message with level ERROR on this logger.
     *
     * @param message the message string to be logged
     * @param param   the parameter to the message
     * @param ex      the exception to log, including its stack trace.
     */
    public void error(String message, Object param, Exception ex) {
        LOGGER.error(message, param, ex);
    }


    /**
     * Log a message with level ERROR on this logger.
     *
     * @param message the message string to be logged
     * @param ex      the exception to log, including its stack trace.
     */
    public void error(String message, Exception ex) {
        LOGGER.error(message, ex);
    }
}

