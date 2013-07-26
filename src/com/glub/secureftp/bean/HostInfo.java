
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: HostInfo.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.bean;

import java.net.*;

/**
 * The <code>HostInfo</code> class is responsible for holding host information.
 *
 * @author Gary Cohen
 * @version $Revision: 47 $, $Date: 2009-05-16 10:10:12 -0700 (Sat, 16 May 2009) $
 * @since 2.0
 */

public class HostInfo {

  /** The hostname */
  private InetAddress hostName;

  /** The port */
  private int port;

  /** Create an empty <code>HostInfo</code> object. */
  public HostInfo() {
    this( (InetAddress)null, 0 );
  }

  /**
   * Create a new <code>HostInfo</code> object.
   *
   * @param  hostname  the hostname.
   * @param  port      the port.
   */
  public HostInfo( InetAddress hostname, int port ) {
    setHostName( hostname );
    setPort( port );
  }

  /**
   * Create a new <code>HostInfo</code> object.
   *
   * @param  hostname  the hostname.
   * @param  port      the port.
   */
  public HostInfo( String hostname, int port ) throws UnknownHostException {
    setHostName( hostname );
    setPort( port );
  }

  /**
   * Get the <code>InetAddress</code> of the host.
   *
   * @return the <code>InetAddress</code>.
   */
  public InetAddress getInetAddress() {
    return hostName;
  }

  /**
   * Get the hostname of the host.
   *
   * @return the hostname.
   */
  public String getHostName() {
    return hostName.getHostName();
  }

  /**
   * Get the IP address of the host.
   *
   * @return the IP address.
   */
  public String getHostAddress() {
    return hostName.getHostAddress();
  }

  /**
   * Set the hostname.
   *
   * @param  hostname  the hostname.
   */
  public void setHostName( InetAddress hostname ) {
    this.hostName = hostname;
  }

  /**
   * Set the hostname.
   *
   * @param  hostname  the hostname.
   */
  public void setHostName( String hostname ) throws UnknownHostException {
    this.hostName = InetAddress.getByName( hostname );
  }

  /**
   * Get the port of the host.
   *
   * @return the port.
   */
  public int getPort() {
    return port;
  }

  /**
   * Set the port.
   *
   * @param  port  the port.
   */
  public void setPort( int port ) {
    this.port = port;
  }
}
