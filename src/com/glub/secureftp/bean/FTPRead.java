
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: FTPRead.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.bean;

import java.io.*;
import java.net.*;
import java.util.zip.*;

import com.glub.util.*;

/**
 * The <code>FTPRead</code> class is responsible for reading data from the
 * FTP server.
 *
 * @author Brian Knight
 * @version $Revision: 47 $, $Date: 2009-05-16 10:10:12 -0700 (Sat, 16 May 2009) $
 * @since 2.5.19
 */

public class FTPRead implements FTPData {
  /** The control socket. */
  private Socket control = null;

  /** The socket to read from (passive). */
  private Socket sock = null;

  /** The socket to read from (active). */
  private ServerSocket servsock = null;

  /** The input stream from the socket. */
  private InputStream input = null;

  /** The output stream from the socket. */
  private OutputStream output = null;

  /** Used to read in an ascii transfer. */
  private BufferedReader reader = null;

  /** Used to write in an ascii transfer. */
  private PrintWriter writer = null;

  /** Used to set the ascii encoding. */
  private String encoding = null;

  /** A reference to a Progress object. */
  private Progress progress = null;

  /** The starting size of the data being transferred. */
  private long startsize = 0;

  /** The filesize of the data being transferred. */
  private long filesize = 0;

  /** If the transfer is aborted, this will be true */
  private boolean aborted = false;

  /** Do we have to wait for the aborted process to cleanup? **/
  private boolean waitForAbort = false;

  /** Is the stream zlib compressed (mode z)? **/
  private boolean zLibCompressed = false;

  /**
   * Create a new <code>FTPRead</code> object to be used to retrieve binary
   * data passively.
   *
   * @param  s   the data socket.
   * @param  os  the data stream.
   */ 
  public FTPRead( Socket s, OutputStream os ) {
    this( s, os, null, 0, 0 );
  }

  /**
   * Create a new <code>FTPRead</code> object to be used to retrieve binary
   * data with progress information passively.
   *
   * @param  s     the data socket.
   * @param  os    the data stream.
   * @param  p     a <code>Progress</code> object which is used to update 
   *               download status.
   * @param  start the start size of the file being downloaded
   *         (used in progress).
   * @param  stop  the stop size of the file being downloaded
   *         (used in progress).
   */ 
  public FTPRead( Socket s, OutputStream os, Progress p, 
                  long start, long stop ) {
    sock = s;
    output = os;
    progress = p;
    startsize = start;
    filesize = stop;
  }

  /**
   * Create a new <code>FTPRead</code> object to be used to retrieve ascii
   * data passively.
   *
   * @param  s   the data socket.
   * @param  w   the data writer.
   */ 
  public FTPRead( Socket s, PrintWriter w ) {
    this( s, w, null, 0, 0 );
  }

  /**
   * Create a new <code>FTPRead</code> object to be used to retrieve ascii
   * data with progress information passively.
   *
   * @param  s     the data socket.
   * @param  w     the data writer.
   * @param  p     a <code>Progress</code> object which is used to update 
   *               download status.
   * @param  start the start size of the file being downloaded
   *         (used in progress).
   * @param  stop  the stop size of the file being downloaded
   *         (used in progress).
   */ 
  public FTPRead( Socket s, PrintWriter w, Progress p, 
                  long start, long stop ) {
    sock = s;
    writer = w;
    progress = p;
    startsize = start;
    filesize = stop;
  }

  /**
   * Create a new <code>FTPRead</code> object to be used to retrieve ascii
   * data with progress information passively.
   *
   * @param  s     the data socket.
   * @param  w     the data writer.
   * @param  p     a <code>Progress</code> object which is used to update 
   *               download status.
   * @param  start the start size of the file being downloaded
   *         (used in progress).
   * @param  stop  the stop size of the file being downloaded
   *         (used in progress).
   * @param  e     the data encoding.
   */ 
  public FTPRead( Socket s, PrintWriter w, Progress p, 
                  long start, long stop, String e ) {
    sock = s;
    writer = w;
    progress = p;
    startsize = start;
    filesize = stop;
    encoding = e;
  }

  /**
   * Create a new <code>FTPRead</code> object to be used to retrieve binary
   * data actively.
   *
   * @param  ss  the data server socket.
   * @param  os  the data stream.
   */ 
  public FTPRead( ServerSocket ss, OutputStream os ) {
    this( ss, os, null, 0, 0 );
  }

  /**
   * Create a new <code>FTPRead</code> object to be used to retrieve binary
   * data with progress information actively.
   *
   * @param  ss    the data server socket.
   * @param  os    the data stream.
   * @param  p     a <code>Progress</code> object which is used to update 
   *               download status.
   * @param  start the start size of the file being downloaded
   *         (used in progress).
   * @param  stop  the stop size of the file being downloaded
   *         (used in progress).
   */ 
  public FTPRead( ServerSocket ss, OutputStream os, Progress p, 
                  long start, long stop ) {
    servsock = ss;
    output = os;
    progress = p;
    startsize = start;
    filesize = stop;
  }

  /**
   * Create a new <code>FTPRead</code> object to be used to retrieve ascii
   * data actively.
   *
   * @param  ss  the data server socket.
   * @param  w   the data writer.
   */ 
  public FTPRead( ServerSocket ss, PrintWriter w ) {
    this( ss, w, null, 0, 0 );
  }

  /**
   * Create a new <code>FTPRead</code> object to be used to retrieve ascii
   * data with progress information actively.
   *
   * @param  ss    the data server socket.
   * @param  w     the data writer.
   * @param  p     a <code>Progress</code> object which is used to update 
   *               download status.
   * @param  start the start size of the file being downloaded
   *         (used in progress).
   * @param  stop  the stop size of the file being downloaded
   *         (used in progress).
   */ 
  public FTPRead( ServerSocket ss, PrintWriter w, Progress p, 
                  long start, long stop ) {
    servsock = ss;
    writer = w;
    progress = p;
    startsize = start;
    filesize = stop;
  }

  /**
   * Create a new <code>FTPRead</code> object to be used to retrieve ascii
   * data with progress information actively.
   *
   * @param  ss    the data server socket.
   * @param  w     the data writer.
   * @param  p     a <code>Progress</code> object which is used to update 
   *               download status.
   * @param  start the start size of the file being downloaded
   *         (used in progress).
   * @param  stop  the stop size of the file being downloaded
   *         (used in progress).
   * @param  e     the data encoding.
   */ 
  public FTPRead( ServerSocket ss, PrintWriter w, Progress p, 
                  long start, long stop, String e ) {
    servsock = ss;
    writer = w;
    progress = p;
    startsize = start;
    filesize = stop;
    encoding = e;
  }

  /**
   * Set stream as zLib compressed (mode z).
   *
   * @param compressed  true if compressed
   */
  public void setZLibCompressed( boolean compressed ) {
    zLibCompressed = compressed;
  }

  /**
   * Set the control socket (used for abort).
   *
   * @param control  the control socket
   */
  public void setControlSocket( Socket control ) {
    this.control = control;
  }

  /**
   * Get the control socket (used for abort).
   *
   * @return the control socket.
   */
  public Socket getControlSocket() {
    return control;
  }

  /**
   * Handle the data transfer.
   *
   * @throws FTPException   if the FTP server returns an error.
   */
  public void doTransfer() throws FTPException {
    _doTransfer();
  }

  /**
   * Abort the data transfer.
   *
   * @throws FTPException   if the FTP server returns an error.
   */
  public void abortTransfer() throws FTPException {
    aborted = true;
    waitForAbort = true;
    _abortSockets();
    //cleanup();
  }

  /**
   * Called when an aborted transfer is complete. This should not be
   * called directly.
   */
  public void abortComplete() { 
    waitForAbort = false; 
  }

  /**
   * Housekeeping for the data socket.
   */
  protected void cleanup() {
    _cleanup();
  }


  /*
   *
   * The methods below are here for obfuscation purposes.
   *
   */

  private void _doTransfer() throws FTPException {
    boolean debug = GTOverride.getBoolean("glub.debug");

    if ((output == null && writer == null) ||
        (sock == null && servsock == null))
    {
      return;
    }

    try {
      if (sock == null) {
        sock = servsock.accept();
        Util.close( servsock );
        servsock = null;
      }
    
      input = sock.getInputStream();

      if ( zLibCompressed ) {
        input = new InflaterInputStream( input, new Inflater(), 1024 );
      }

      long nbytes = startsize;

      if (progress != null)
        progress.startProgress();

      if (output != null) {
        byte[] b = new byte[1024];
        int len = 0;
        while ((len = input.read( b )) >= 0) {
          output.write( b, 0, len );
          nbytes += len;
          if (progress != null)
            progress.updateProgress( nbytes, filesize );
        }
        output.flush();
      } 
      else {
        String line = null;

        InputStreamReader isr = null;

        if ( encoding != null )
          isr = new InputStreamReader( input, encoding );
        else
          isr = new InputStreamReader( input );

        reader = new BufferedReader( isr );

        if ( debug ) {
          System.out.println( "Input Encoding = " + isr.getEncoding() );
        }

        while ((line = reader.readLine()) != null) {
          writer.println( line ); // println okay, write to local system
          nbytes += (line.length() + 1);
          if (progress != null)
            progress.updateProgress( nbytes, filesize );
        }
        writer.flush();
      }

      if (progress != null)
        progress.finishProgress();
    }
    catch (IOException ioe) {
      if ( aborted ) {
        throw new FTPAbortException( "ABOR command successful." );
      }
      else {
        throw new FTPException( ioe.getMessage() );
      }
    }
    finally {
      if ( !aborted ) {
        cleanup();
      }
      else {
        while ( waitForAbort ) {
          try {
            Thread.sleep(100);
            //System.out.println("waiting for abort.");
          }
          catch ( InterruptedException ie ) {
          }
        }
	cleanup();
        throw new FTPAbortException( "ABOR command successful." );
      }
    }
  }

  private void _abortSockets() {
    // wait 10 second just in case the server doesn't drop you...
    try {
      Thread.sleep(10000);
    }
    catch ( InterruptedException ie ) {
      //ie.printStackTrace();
    }

    Util.close( sock );
    Util.close( servsock );
  }

  private void _cleanup() {
    Util.close( input );
    Util.close( output );
    Util.close( reader );
    Util.close( writer );
    Util.close( sock );
    Util.close( servsock );
  }
}

