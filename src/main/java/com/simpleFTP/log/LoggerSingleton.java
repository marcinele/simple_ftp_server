package com.simpleFTP.log;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerSingleton {
    private static Logger logger;
    private static FileHandler fh;

    public static Logger getLogger(){
        if(logger == null && fh == null){
            logger = Logger.getLogger(LoggerSingleton.class.getName());
            File file = new File("logs" + File.separator + "logs.txt");
            File fileFolder = new File("logs");
            if(!fileFolder.exists()){
                fileFolder.mkdir();
            }
            if(!file.exists()){
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                fh = new FileHandler("logs/log.txt");
            } catch (IOException e) {
                e.printStackTrace();
            }

            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            logger.setUseParentHandlers(false);
        }
        return logger;
    }
}
