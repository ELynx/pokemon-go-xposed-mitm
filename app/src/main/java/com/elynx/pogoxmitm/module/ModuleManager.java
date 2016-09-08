package com.elynx.pogoxmitm.module;

import com.github.aeonlucid.pogoprotos.networking.Requests;

import com.google.protobuf.ByteString;

import java.util.List;

/**
 * Interface defining communications between MitM provider and single hack
 */
public interface ModuleManager {
    /**
     * Module name in module list
     * @return Module name
     */
    String userspaceName();

    /**
     * Module description in module list
     * @return Module brief description
     */
    String userspaceBrief();

    /**
     * Module "about" info, presented to user on demand
     * Will be rendered as HTML
     * @return Module about info
     */
    String userspaceInfo();

    /**
     * Called once per module life cycle
     * Upon call module starts with default environment
     * Failed modules do not receive any communications, and are off/disabled in module list
     * @return True if module may be executed, False otherwise
     */
    boolean init();

    /**
     * Types of requests this module wants to receive
     * May be different from response types
     * @return Request types processed in module
     */
    List<Requests.RequestType> requestTypes();

    /**
     * Types of responses this module wants to receive
     * May be different from request types
     * @return Response types processed in module
     */
    List<Requests.RequestType> responseTypes();

    /**
     * Process request from client to server
     * @param type Type of request
     * @param data Data of request
     * @return Modified data if changes are made, null otherwise
     */
    ByteString processRequest(Requests.RequestType type, ByteString data);

    /**
     * Process response from server to client
     * @param type Type of response
     * @param data Data of response
     * @return Modified data if changes are made, null otherwise
     */
    ByteString processResponse(Requests.RequestType type, ByteString data);
}
