package com.simpleFTP.security;

import java.io.File;

public class PathValidator {

    // Returns null if path is invalid, absolute path otherwise.
    public static String GeneralValidator(String arg, String cwd_prefix, String cwd){
        if(arg.startsWith(File.separator + "ftp")){
            return cwd_prefix + File.separator + arg;
        } else if(arg.startsWith(File.separator) || arg.startsWith("-")){
            return null;
        } else if(arg.startsWith("." + File.separator)){
            return cwd_prefix + File.separator + cwd + File.separator + arg.replaceFirst("."+File.separator, "");
        } else{
            return cwd_prefix + File.separator + cwd + File.separator + arg;
        }
    }

    public static boolean CdupValidator(String cwd){
        String[] cwd_splitted;
        String[] os = System.getProperty("os.name").split(" ");
        if (os[0].equals("Windows")){
            String separator = File.separator + File.separator;
            cwd_splitted = cwd.split(separator);
        }
        else {
            cwd_splitted = cwd.split(File.separator);
        }
        return cwd_splitted.length > 2;
    }

    public static String CwdValidator(String arg, String cwd){
        if(arg.startsWith(File.separator + "ftp")){
            return arg;
        } else if(arg.startsWith(File.separator)){
            return null;
        } else if(arg.startsWith("." + File.separator)){
            return cwd + arg.replaceFirst(".", "");
        } else{
            return cwd + File.separator + arg;
        }
    }

    public static String DeleValidator(String arg, String cwd_prefix, String cwd){
        String path = PathValidator.GeneralValidator(arg, cwd_prefix, cwd);
        if(path != null && !path.endsWith("ftp")){
            return path;
        }
        return null;
    }
}
