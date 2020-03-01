package com.castellanos;

public class FileStorageException  extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = 1900311552106683387L;

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}