/*
 * Copyright (c) 2013 University of Vienna, Austria. All rights reserved.
 * Released under the terms of the BSD 2-Clause License.
 * http://opensource.org/licenses/BSD-2-Clause
 */
package dera.core;

public abstract class Bridge extends AbstractEventActor {

    protected String host;
    protected int port;
    protected boolean secure;

	public Bridge(final String id, String host, int port, boolean secure) {
		super(id);
        this.host = host;
        this.port = port;
        this.secure = secure;
	}

    public final boolean isSecure() {
        return secure;
    }

    public final void setSecure(boolean secure) {
        this.secure = secure;
    }

    public final String getHost() {
		return host;
	}

	public final void setHost(String host) {
		this.host = host;
	}

    public final int getPort() {
        return port;
    }

    public final void setPort(int port) {
        this.port = port;
    }

}
