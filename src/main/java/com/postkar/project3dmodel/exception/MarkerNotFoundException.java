package com.postkar.project3dmodel.exception;

public class MarkerNotFoundException extends RuntimeException {
    public MarkerNotFoundException(String markerId) {
        super("Marker not found: " + markerId);
    }
}
