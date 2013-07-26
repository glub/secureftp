
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: FTPWrite.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.bean;

import java.io.*;
import java.net.*;
import java.util.zip.*;

import com.glub.util.*;

/**
 * The <code>FTPWrite</code> class is responsible for writing data from the
 * FTP server.
 *
 * @author Brian Knight
 * @version $Revision: 47 $, $Date: 2009-05-16 10:10:12 -0700 (Sat, 16 May 2009) $
 * @since 2.5.19
 */

public class FTPWrite implements FTPData {
  /** The control socket */
  private Socket control = null;

  /** The socket to write to (passive). */
  private Socket sock = null;

  /** The socket to write to (active). */
  private ServerSocket servsock = null;

  /** The input stream stream from the socket. */
  private InputStream input = null;

  /** The output stream from the socket. */
  private OutputStream output = null;

  /** Used to read in an ascii transfer. */
  private BufferedReader reader = null;

  /** Used to set the ascii encoding. */
  private String encoding = null;

  /** Used to write in an ascii transfer. */
  private PrintWriter writer = null;

  /** A reference to a Progress object. */
  private Progress progress = null;

  /** The start size of the data being transferred. */
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
   * Create a new <code>FTPWrite</code> object to be used to send binary
   * data passively.
   *
   * @param  s   the data socket.
   * @param  is  the data stream.
   */
  public FTPWrite( Socket s, InputStream is ) {
    this( s, is, null, 0, 0 );
  }

  /**
   * Create a new <code>FTPWrite</code> object to be used to send binary
   * data with progress information passively.
   *
   * @param  s     the data socket.
   * @param  is    the data stream.
   * @param  p     a <code>Progress</code> object which is used to update
   *               upload status.
   * @param  start the start size of the file being uploaded (used in progress).
   * @param  stop  the stop size of the file being uploaded (used in progress).
   */
  public FTPWrite( Socket s, InputStream is, Progress p, 
                   long start, long stop ) {
    sock = s;
    input = is;
    progress = p;
    startsize = start;
    filesize = stop;
  }

  /**
   * Create a new <code>FTPWrite</code> object to be used to send ascii
   * data passively.
   *
   * @param  s   the data socket.
   * @param  r   the data reader.
   */
  public FTPWrite( Socket s, BufferedReader r ) {
    this( s, r, null, 0, 0 );
  }

  /**
   * Create a new <code>FTPWrite</code> object to be used to send ascii
   * data with progress information passively.
   *
   * @param  s     the data socket.
   * @param  r     the data reader.
   * @param  p     a <code>Progress</code> object which is used to update
   *               upload status.
   * @param  start the start size of the file being uploaded (used in progress).
   * @param  stop  the stop size of the file being uploaded (used in progress).
   */
  public FTPWrite( Socket s, BufferedReader r, Progress p, 
                   long start, long stop ) {
    sock = s;
    reader = r;
    progress = p;
    startsize = start;
    filesize = stop;
  }

  /**
   * Create a new <code>FTPWrite</code> object to be used to send ascii
   * data with progress information passively.
   *
   * @param  s     the data socket.
   * @param  r     the data reader.
   * @param  p     a <code>Progress</code> object which is used to update
   *               upload status.
   * @param  start the start size of the file being uploaded (used in progress).
   * @param  stop  the stop size of the file being uploaded (used in progress).
   * @param  e     the data encoding.
   */
  public FTPWrite( Socket s, BufferedReader r, Progress p, 
                   long start, long stop, String e ) {
    sock = s;
    reader = r;
    progress = p;
    startsize = start;
    filesize = stop;
    encoding = e;
  }

  /**
   * Create a new <code>FTPWrite</code> object to be used to send binary
   * data actively.
   *
   * @param  ss  the data server socket.
   * @param  is  the data stream.
   */
  public FTPWrite( ServerSocket ss, InputStream is ) {
    this( ss, is, null, 0, 0 );
  }

  /**
   * Create a new <code>FTPWrite</code> object to be used to send binary
   * data with progress information actively.
   *
   * @param  ss    the data server socket.
   * @param  is    the data stream.
   * @param  p     a <code>Progress</code> object which is used to update
   *               upload status.
   * @param  start the start size of the file being uploaded (used in progress).
   * @param  stop  the stop size of the file being uploaded (used in progress).
   */
  public FTPWrite( ServerSocket ss, InputStream is, Progress p, 
                   long start, long stop ) {
    servsock = ss;
    input = is;
    progress = p;
    startsize = start;
    filesize = stop;
  }

  /**
   * Create a new <code>FTPWrite</code> object to be used to send ascii
   * data actively.
   *
   * @param  ss  the data server socket.
   * @param  r   the data reader.
   */
  public FTPWrite( ServerSocket ss, BufferedReader r ) {
    this( ss, r, null, 0, 0 );
  }

  /**
   * Create a new <code>FTPWrite</code> object to be used to send ascii
   * data with progress information actively.
   *
   * @param  ss    the data server socket.
   * @param  r     the data reader.
   * @param  p     a <code>Progress</code> object which is used to update
   *               upload status.
   * @param  start the start size of the file being uploaded (used in progress).
   * @param  stop  the stop size of the file being uploaded (used in progress).
   */
  public FTPWrite( ServerSocket ss, BufferedReader r, Progress p, 
                   long start, long stop ) {
    servsock = ss;
    reader = r;
    progress = p;
    startsize = start;
    filesize = stop;
  }

  /**
   * Create a new <code>FTPWrite</code> object to be used to send ascii
   * data with progress information actively.
   *
   * @param  ss    the data server socket.
   * @param  r     the data reader.
   * @param  p     a <code>Progress</code> object which is used to update
   *               upload status.
   * @param  start the start size of the file being uploaded (used in progress).
   * @param  stop  the stop size of the file being uploaded (used in progress).
   * @param  e     the data encoding.
   */
  public FTPWrite( ServerSocket ss, BufferedReader r, Progress p, 
                   long start, long stop, String e ) {
    servsock = ss;
    reader = r;
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
    //Socket s = null;
    OutputStream output = null;
    PrintWriter writer = null;

    boolean debug = GTOverride.getBoolean("glub.debug");

    if ((input == null && reader == null) ||
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

      output = sock.getOutputStream();

      if ( zLibCompressed ) {
        output = new DeflaterOutputStream( output, new Deflater(), 1024 );
      }

      long nbytes = startsize;

      if (progress != null)
        progress.startProgress();

      if (input != null) {
        byte[] b = new byte[1024];
        boolean wroteData = false;

        int len = 0;
        while ((len = input.read( b )) >= 0) {
          output.write( b, 0, len );
          wroteData = true;
          nbytes += len;
          if (progress != null) 
            progress.updateProgress( nbytes, filesize );
        }

        // there seems to be a bug where an empty file that doesn't write
        // anything to the stream causes an exception, so write nothing
        if (!wroteData) {
          output.write( new byte[1], 0, 0 );
        }

        output.flush();

        if ( output instanceof DeflaterOutputStream ) {
          ((DeflaterOutputStream)output).finish();
        }
      } else {
        String line = null;

        OutputStreamWriter osw = null;

        if ( encoding != null )
          osw = new OutputStreamWriter( output, encoding );
        else
          osw = new OutputStreamWriter( output );

        writer = new PrintWriter( new BufferedWriter( osw ) );

        if ( debug ) {
          System.out.println( "Output Encoding = " + osw.getEncoding() );
        }

        while ((line = reader.readLine()) != null) {
          writer.print( line + "\r\n" ); // no println, writing to remote system
          nbytes += (line.length() + 1);
          if (progress != null) 
            progress.updateProgress( nbytes, filesize );
        }
        writer.flush();
        if ( output instanceof DeflaterOutputStream ) {
          ((DeflaterOutputStream)output).finish();
        }
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
            //System.out.println("waiting for abort");
          }
          catch ( InterruptedException ie ) {
            //ie.printStackTrace();
          }
        }
	cleanup();
        throw new FTPAbortException( "ABOR command successful." );
      }
    }
  }

  private void _abortSockets() {
    // wait 10 seconds just in case the server doesn't drop you...
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

