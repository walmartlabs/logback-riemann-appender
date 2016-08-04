package org.gorillalabs.logback;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jenslehnhoff on 27.07.16.
 */
public class LogTestMain {

    final static Logger logger = LoggerFactory.getLogger(LogTestMain.class);

    public static void main(String [] args){

        logger.info("Test 1 -  Info");
        logger.debug("Test 2 - Debug");
        logger.trace("Test 3 - Trace");
        logger.warn("Test 4 - Warn");
        logger.error("Test 5 - Error");

    }

}
