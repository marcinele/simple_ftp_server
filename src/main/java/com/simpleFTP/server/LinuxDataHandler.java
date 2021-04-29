package com.simpleFTP.server;

import java.nio.file.Files;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;

public class LinuxDataHandler {
    private Path path;

    LinuxDataHandler(Path path){
        this.path = path;
    }
    public String usingJava7Metadata() throws IOException {
        PosixFileAttributes attrs = Files.readAttributes(this.path, PosixFileAttributes.class);
        BasicFileAttributes attributes = Files.readAttributes(this.path, BasicFileAttributes.class);
        // convert objects to 'rwx' style strings:
        return PosixFilePermissions.toString(attrs.permissions());
    }
}
