package com.test.arduinosocket.core;

/**
 * Created by administrator on 4/20/2017.
 */

public interface CommandResponseHandler {
    void handleResponse(CommandData response, Device device);
}
