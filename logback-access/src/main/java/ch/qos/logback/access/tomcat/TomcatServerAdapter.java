/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.access.tomcat;

import ch.qos.logback.access.spi.ServerAdapter;

import org.apache.catalina.Globals;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * A tomcat specific implementation of the {@link ServerAdapter} interface.
 *
 * @author S&eacute;bastien Pennec
 */
public class TomcatServerAdapter implements ServerAdapter {

    Request request;
    Response response;

    public TomcatServerAdapter(Request tomcatRequest, Response tomcatResponse) {
        this.request = tomcatRequest;
        this.response = tomcatResponse;
    }

    @Override
    public long getContentLength() {
        // Don't need to flush since trigger for log message is after the
        // response has been committed
        long length = response.getBytesWritten(false);
        if (length <= 0) {
            // Protect against nulls and unexpected types as these values
            // may be set by untrusted applications
            Object start = request.getAttribute(
                    Globals.SENDFILE_FILE_START_ATTR);
            if (start instanceof Long) {
                Object end = request.getAttribute(
                        Globals.SENDFILE_FILE_END_ATTR);
                if (end instanceof Long) {
                    length = ((Long) end).longValue() -
                            ((Long) start).longValue();
                }
            }
        }
        return length;
    }

    @Override
    public int getStatusCode() {
        return response.getStatus();
    }

    @Override
    public long getRequestTimestamp() {
        return request.getCoyoteRequest().getStartTime();
    }

    @Override
    public Map<String, String> buildResponseHeaderMap() {
        Map<String, String> responseHeaderMap = new HashMap<String, String>();
        for (String key : response.getHeaderNames()) {
            String value = response.getHeader(key);
            responseHeaderMap.put(key, value);
        }
        return responseHeaderMap;
    }
}
