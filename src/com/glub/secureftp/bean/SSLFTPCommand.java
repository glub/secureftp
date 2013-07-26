
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: SSLFTPCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.bean;

import java.io.*;

/**
 * The <code>SSLFTPCommand</code> class is responsible for handling the SSL
 * command extensions used in the File Transfer Protocol.
 *
 * @author Gary Cohen
 * @version $Revision: 47 $, $Date: 2009-05-16 10:10:12 -0700 (Sat, 16 May 2009) $
 * @since 2.5
 */

public class SSLFTPCommand extends FTPCommand {
  /** Used to set the <code>PROT</code> mode to private. */
  public static final char PRIVATE_DATA_CHANNEL      = 'P';

  /** Used to set the <code>PROT</code> mode to clear. */
  public static final char CLEAR_DATA_CHANNEL        = 'C';

  /** Used to set the <code>PROT</code> mode to safe. */
  public static final char SAFE_DATA_CHANNEL         = 'S';

  /** Used to set the <code>PROT</code> mode to confidential. */
  public static final char CONFIDENTIAL_DATA_CHANNEL = 'E';

  /**
   * Create a new <code>SSLFTPCommand</code> object.
   *
   * @param reader      based on the input stream from the control socket.
   * @param writer      based on the output stream from the control socket.
   */
  public SSLFTPCommand( BufferedReader reader, PrintWriter writer ) {
    super( reader, writer );
  }

  /**
   * Create a new <code>SSLFTPCommand</code> object.
   *
   * @param reader         based on the input stream from the control socket.
   * @param writer         based on the output stream from the control socket.
   * @param sendCmdStream  stream used to report commands set to the FTP server.
   * @param recvCmdStream  stream used to report commands received from the
   *                       FTP server.
   */
  public SSLFTPCommand( BufferedReader reader, PrintWriter writer,
                        OutputStream sendCmdStream, 
                        OutputStream recvCmdStream ) {
    super( reader, writer, sendCmdStream, recvCmdStream );
  }

  /**
   * Specify the authorization type. This is used during the initialization of
   * an explicit SSL connection.
   *
   * @param     authType    the auth type to send (e.g. SSL, TLS, etc.)
   *
   * @throws FTPAuthNotSupportedException    if the server doesn't support this
   *                                         auth mode (or the auth command).
   * @throws FTPException                    if the FTP server returns an error
   *                                         code.
   * @throws IllegalArgumentException        if <code>authType</code> is 
   *                                         missing.
   */
  public synchronized void auth( String authType ) 
                           throws FTPAuthNotSupportedException, FTPException, 
                                  IllegalArgumentException {
    _auth( authType );
  }

  /**
   * Specify the protection buffer. This must be called prior to the 
   * <code>PROT</code> command.
   *
   * @param  bufferSize     the size to pad the protection buffer (usually 0).
   *
   * @throws FTPException   if the FTP server returns an error code.
   * 
   * @see #prot(char)
   */
  public synchronized void pbsz( int bufferSize ) throws FTPException {
    _pbsz( bufferSize );
  }

  /**
   * Specify the protection mode. This must be called after the 
   * <code>PBSZ</code> command is sent.
   *
   * @param securityMode    the security mode to set the data channel to.
   *
   * @throws FTPException   if the FTP server returns an error code.
   * 
   * @see #PRIVATE_DATA_CHANNEL
   * @see #CLEAR_DATA_CHANNEL
   * @see #SAFE_DATA_CHANNEL
   * @see #CONFIDENTIAL_DATA_CHANNEL
   * @see #pbsz(int)
   */
  public synchronized void prot( char securityMode ) throws FTPException {
    _prot( securityMode );
  }

  /**
   * Convert the control socket back to a clear control socket.
   *
   * @throws FTPException   if the FTP server returns an error code.
   */
  public synchronized void ccc() throws FTPException {
    _ccc();
  }


  /*
   *
   * The methods below are here for obfuscation purposes.
   *
   */

  private synchronized void _auth( String authType ) 
                           throws FTPAuthNotSupportedException, FTPException, 
                                  IllegalArgumentException {
     if ( authType == null ) {
       throw new IllegalArgumentException( "Missing authorization type" );
     }

     sendCmd("AUTH " + authType);
     recvCmd();

     // Auth was successful if reply code is 234
     if ( replyCode == 234 ) { }

     // Auth result 334 is not correct, but some servers return it
     else if ( replyCode == 334 ) { }
     
     else if ( replyCode == 500 || replyCode == 502 || replyCode == 504 ||
               replyCode == 530 ) {
       throw new FTPAuthNotSupportedException();
     }

     else if ( replyCode == 534 ) {
       throw new FTPPolicyRestrictionException();
     }

     else {
       throw new FTPException(replyMessage);
     } 
  }

  private synchronized void _pbsz( int bufferSize ) throws FTPException {
    if ( bufferSize < 0 ) {
      bufferSize = 0;
    }
 
    sendCmd("PBSZ " + bufferSize);
    recvCmd();

    // pbsz was successful if reply code is 200
    //if ( replyCode == 200 ) {}
    if ( replyCode >= 200 && replyCode < 300 ) {}

    // apache is returning this eventhough it shouldn't
    else if ( replyCode == 503 ) {}

    else if ( replyCode == 534 ) {
      throw new FTPPolicyRestrictionException();
    }

    else {
      throw new FTPException(replyMessage);
    }
  }

  private synchronized void _prot( char securityMode ) throws FTPException {
    sendCmd("PROT " + securityMode);
    recvCmd();

    // prot was successful if reply code is 200
    //if ( replyCode == 200 ) {}
    if ( replyCode >= 200 && replyCode < 300 ) {}

    else if ( replyCode == 534 ) {
      throw new FTPPolicyRestrictionException();
    }

    else {
      throw new FTPException(replyMessage);
    }
  }

  private synchronized void _ccc() throws FTPException {
    sendCmd("CCC");
    recvCmd();

    // ccc was successful if reply code is 200
    if ( replyCode >= 200 && replyCode < 300 ) {}

    else {
      throw new FTPException(replyMessage);
    }
  }
}
