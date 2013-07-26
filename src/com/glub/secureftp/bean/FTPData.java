
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: FTPData.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.bean;

import java.net.*;

/**
 * The <code>FTPData</code> interface is used to handle the transfer of data
 * from the FTP server.
 *
 * @author Brian Knight
 * @version $Revision: 47 $, $Date: 2009-05-16 10:10:12 -0700 (Sat, 16 May 2009) $
 * @since 2.1.2
 */

public interface FTPData {

  /**
   * Set the control socket (used for abort).
   *
   * @param control  the control socket
   */
  public void setControlSocket( Socket control );

  /**
   * Get the control socket (used for abort).
   *
   * @return the control socket.
   */
  public Socket getControlSocket(); 

  /**
   * Handle the data transfer.
   *
   * @throws FTPException   if the FTP server returns an error.
   */
  public void doTransfer() throws FTPException;

  /**
   * Abort the data transfer.
   *
   * @throws FTPException   if the FTP server returns an error.
   */
  public void abortTransfer() throws FTPException;


  /**
   * Called when an aborted transfer is complete. This should not be
   * called directly. 
   */
  public void abortComplete();

}
