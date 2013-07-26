
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: FTPCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.bean;

import java.io.*;
import java.lang.Class;
import java.lang.reflect.Method;
import java.net.*;
import java.text.*;
import java.util.*;

import org.apache.regexp.*;

import com.glub.util.*;

/**
 * The <code>FTPCommand</code> class is responsible for handling the standard 
 * commands used in the File Transfer Protocol.
 *
 * @author Brian Knight
 * @version $Revision: 47 $, $Date: 2009-12-01 23:35:56 -0800 (Tue, 01 Dec 2009) $
 * @since 2.5.5
 */

public class FTPCommand {
  private boolean forcePasvToUseControlIP = false;
  private String controlIP = null;

  PrintWriter cmdWriter;             // output stream for command channel
  BufferedReader cmdReader;          // input stream for command channel

  String reply;                      // entire reply from server
  int replyCode;                     // numeric reply code from server
  String replyMessage;               // message received from server

  RECompiler compiler;               // pattern compiler
  RE matcher;  	 	             // pattern matcher

  DateFormat mdtmFormat = new SimpleDateFormat( "yyyyMMddHHmmss z" );

  OutputStreamWriter recvCmdWriter = null; // data received back from server
  OutputStreamWriter sendCmdWriter = null; // data sent to server

 /**
  * Create a new <code>FTPCommand</code> object.
  *
  * @param reader      based on the input stream from the control socket.
  * @param writer      based on the output stream from the control socket.
  */
  public FTPCommand( BufferedReader reader, PrintWriter writer ) {
    this( reader, writer, null, null );
  }

 /**
  * Create a new <code>FTPCommand</code> object.
  *
  * @param reader          based on the input stream from the control socket.
  * @param writer          based on the output stream from the control socket.
  * @param sendCmdStream   stream used to report commands set to the FTP server.
  * @param recvCmdStream   stream used to report commands received from the 
  *                        FTP server.
  */
  public FTPCommand( BufferedReader reader, PrintWriter writer,
                     OutputStream sendCmdStream, OutputStream recvCmdStream ) {
    cmdReader = reader;
    cmdWriter = writer;

    setSendCmdStream( sendCmdStream );
    setRecvCmdStream( recvCmdStream );

    reply = null;
    replyCode = -1;
    replyMessage = null;

    compiler = new RECompiler();
    matcher = new RE();
  }

 /**
  * Set the stream responsible for getting replies from the FTP server.
  *
  * @param recvCmdStream   the stream used to get replies from the FTP server.
  */
  public void setRecvCmdStream( OutputStream recvCmdStream ) {
    _setRecvCmdStream( recvCmdStream );
  }

 /**
  * Set the stream responsible for getting commands sent to the FTP server.
  *
  * @param sendCmdStream   the stream used to get the commands sent to 
  *                        the FTP server.
  */
  public void setSendCmdStream( OutputStream sendCmdStream ) {
    _setSendCmdStream( sendCmdStream );
  }

 /**
  * Specify the username for logging in to the FTP server.
  *
  * @param     username    name of the user attempting to login.
  *
  * @exception FTPNeedPasswordException if a password is required for login.
  * @exception FTPNeedAccountException  if account name is required for login.
  * @exception FTPBadLoginException     if login is denied.
  * @exception FTPException             if the FTP server returns an error code.
  * @exception IllegalArgumentException if <code>username</code> is missing.
  */
  public synchronized void user(String username)
         throws FTPNeedPasswordException, FTPNeedAccountException,
                FTPBadLoginException, FTPException, IllegalArgumentException {
    _user( username );
  }

 /**
  * Specify the password for logging in to the FTP server.
  *
  * @param     password    password for the user attempting to login
  *
  * @exception FTPNeedAccountException  if account name is required for login.
  * @exception FTPBadLoginException     if login is denied.
  * @exception FTPException             if the FTP server returns an error code.
  * @exception IllegalArgumentException if <code>username</code> is missing.
  */
  public synchronized void pass(String password)
         throws FTPNeedAccountException, FTPBadLoginException,
                FTPException, IllegalArgumentException {
    _pass( password );
  }

 /**
  * Specify the account name to the FTP server.
  *
  * @param     account     account name for the user attempting to login
  *
  * @exception FTPBadLoginException     if login is denied.
  * @exception FTPException             if the FTP server returns an error code.
  * @exception IllegalArgumentException if <code>username</code> is missing.
  */
  public synchronized void acct(String account)
         throws FTPBadLoginException, FTPException, IllegalArgumentException {
    _acct( account );
  }

 /**
  * Quit the FTP session.
  *
  * @exception FTPException if the FTP server returns an error code.
  */
  public synchronized void quit() throws FTPException {
    _quit();
  }

 /**
  * Get system information from the FTP server.
  *
  * @return    system information as returned by the remote server.
  *
  * @exception FTPException if the FTP server returns an error code.
  */
  public synchronized String syst() throws FTPException {
    return _syst();
  }

 /**
  * Delete a file from the FTP server.
  *
  * @param     filename    name of the file to delete.
  *
  * @exception FTPException             if the FTP server returns an error code.
  * @exception IllegalArgumentException if <code>filename</code> is missing.
  */
  public synchronized void delete(String filename)
         throws FTPException, IllegalArgumentException {
    _delete( filename );
  }

 /**
  * Rename a file on the FTP server.
  *
  * @param     from        existing name of the file.
  * @param     to          new name for the file.
  *
  * @exception FTPException             if the FTP server returns an error code.
  * @exception IllegalArgumentException if either <code>from</code> or
  *                                     <code>to</code> is missing.
  */
  public synchronized void rename(String from, String to)
         throws FTPException, IllegalArgumentException {
    _rename( from, to );
  }

 /**
  * Make a directory on the FTP server.
  *
  * @param     dir         new directory name.
  *
  * @exception FTPAccessDeniedException if the directory couldn't be created
  *                                     due to access restrictions.
  * @exception FTPException             if the FTP server returns an error code.
  * @exception IllegalArgumentException if <code>dir</code> is missing.
  */
  public synchronized void mkdir(String dir)
         throws FTPException, FTPAccessDeniedException, 
                IllegalArgumentException {
    _mkdir( dir );
  }

 /**
  * Remove a directory from the FTP server.
  *
  * @param     dir         new directory name.
  *
  * @exception FTPException             if the FTP server returns an error code.
  * @exception IllegalArgumentException if <code>dir</code> is missing.
  */
  public synchronized void rmdir(String dir)
         throws FTPException, IllegalArgumentException {
    _rmdir( dir );
  }

 /**
  * Restart an incomplete file transfer.
  *
  * @param byteOffset         the amount of bytes to seek into the restore.
  *
  * @exception FTPException   if the FTP server returns an error code.
  */
  public synchronized void rest( long byteOffset ) throws FTPException {
    _rest( byteOffset );
  }

 /**
  * Get the modification time of a file on the FTP server.
  * 
  * @param file  the name of the file.
  *
  * @return the file time or <code>null</code> if the time could not be 
  *         determined.
  *
  * @exception FTPNoSuchFileException   if <code>filename</code> does not exist.
  * @exception FTPException   if the FTP server returns an error code.
  * @exception IllegalArgumentException if <code>file</code> is missing.
  */
  public synchronized Date mdtm( String file ) throws FTPNoSuchFileException,
                                                      FTPException, 
                                                      IllegalArgumentException {
    return _mdtm( file );
  }

 /**
  * Change remote directory on the FTP server.
  *
  * @param     dir         the directory name.
  *
  * @exception FTPNotADirectoryException if <code>dir</code> is not a directory.
  * @exception FTPNoSuchFileException    if <code>dir</code> does not exist.
  * @exception FTPException             if the FTP server returns an error code.
  * @exception IllegalArgumentException if <code>dir</code> is missing.
  */
  public synchronized void chdir(String dir)
         throws FTPNotADirectoryException, FTPNoSuchFileException,
                FTPException, IllegalArgumentException {
    _chdir( dir );
  }

 /**
  * Move up one directory on the FTP server.
  *
  * @exception FTPException if the FTP server returns an error code.
  */
  public synchronized void cdup() throws FTPException {
    _cdup();
  }

 /**
  * Get the working directory on the FTP server.
  *
  * @return    the working directory on the FTP server,
  *            or null if the results could not be parsed.
  *
  * @exception FTPException if the FTP server returns an error code.
  */
  public synchronized String pwd() throws FTPException {
    return _pwd();
  }

 /**
  * Abort a data transfer.
  * <P>
  * This method is not synchronized since abort may need to interrupt
  * another method executing on the same object.
  *
  * @param     data        an Object that implements the FTPData interface.
  *
  * @exception FTPException             if the FTP server returns an error code.
  * @exception IllegalArgumentException if <code>data</code> is missing.
  */
  public void abort( FTPData data ) throws FTPException, 
                                           IllegalArgumentException {
    try {
      _abort( data );
    }
    catch ( FTPException fe ) {
      throw fe;
    }
    finally {
      data.abortComplete();
    }
  }

 /**
  * Retrieve a file from the FTP server.
  *
  * @param     file        name of the file.
  * @param     data        an Object that implements the FTPData interface.
  *
  * @exception FTPException             if the FTP server returns an error code.
  * @throws    IOException              if there are socket problems.
  * @exception IllegalArgumentException if either <code>file</code> or
  *                                     <code>data</code> is missing.
  */
  public synchronized void retrieve(String file, FTPData data)
         throws FTPException, IOException, IllegalArgumentException {
    _retrieve( file, data );
  }

 /**
  * Store a file on the FTP server.
  *
  * @param     file        name of the file.
  * @param     data        an Object that implements the FTPData interface.
  *
  * @exception FTPException             if the FTP server returns an error code.
  * @throws    IOException              if there are socket problems.
  * @exception IllegalArgumentException if either <code>file</code> or
  *                                     <code>data</code> is missing.
  */
  public synchronized void store(String file, FTPData data)
         throws FTPException, IOException, IllegalArgumentException {
    _store( file, data );
  }

 /**
  * Append to a file on the FTP server.
  *
  * @param     file        name of the file.
  * @param     data        an Object that implements the FTPData interface.
  *
  * @exception FTPException             if the FTP server returns an error code.
  * @throws    IOException              if there are socket problems.
  * @exception IllegalArgumentException if either <code>file</code> or
  *                                     <code>data</code> is missing.
  */
  public synchronized void append(String file, FTPData data)
         throws FTPException, IOException, IllegalArgumentException {
    _append( file, data );
  }

 /**
  * Set the type of transfer.
  *
  * @param     t        the transfer type ('A' = ascii, 'I' = binary image).
  *
  * @exception FTPException if the FTP server returns an error code.
  */
  public synchronized void type(char t) throws FTPException {
    _type( t );
  }

 /**
  * Mode Z compression
  *
  * @exception FTPException if the FTP server returns an error code.
  */
  public synchronized void modeZ() throws FTPException {
    _modeZ();
  }

 /**
  * Get the size of a file from the FTP server.
  *
  * @param     filename    name of the file.
  *
  * @return    size of the file, or -1 if the results could not be parsed.
  *
  * @exception FTPNoSuchFileException   if <code>filename</code> does not exist.
  * @exception FTPException             if the FTP server returns an error code.
  * @exception IllegalArgumentException if <code>filename</code> is missing.
  */
  public synchronized long size(String filename) throws FTPNoSuchFileException,
                                                      FTPException, 
                                                      IllegalArgumentException {
    return _size( filename );
  }

 /**
  * List files in a directory on the FTP server.
  *
  * @param     itemsToList the items to list.
  * @param     data        an Object that implements the FTPData interface.
  * @param     showHidden  flag to show hidden files (subject to availibility)
  *
  * @exception FTPException             if the FTP server returns an error code.
  * @throws    IOException              if there are socket problems.
  * @exception IllegalArgumentException if <code>data</code> is missing.
  */
  public synchronized void list(String itemsToList, FTPData data, 
                                boolean showHidden)
         throws FTPException, IOException, IllegalArgumentException {
    _list( itemsToList, data, showHidden );
  }

 /**
  * NList files in a directory on the FTP server.
  *
  * @param     itemsToList the items to list.
  * @param     data        an Object that implements the FTPData interface.
  *
  * @exception FTPException             if the FTP server returns an error code.
  * @throws    IOException              if there are socket problems.
  * @exception IllegalArgumentException if <code>data</code> is missing.
  */
  public synchronized void nlst(String itemsToList, FTPData data)
         throws FTPException, IOException, IllegalArgumentException {
    _nlst( itemsToList, data );
  }

 /**
  * Request a PORT data transfer from the FTP server.
  *
  * @param     hostInfo    HostInfo for the local listening socket.
  *
  * @exception FTPException             if the FTP server returns an error code.
  * @exception IllegalArgumentException if hostInfo is null
  */
  public synchronized void port( HostInfo hostInfo )
                           throws FTPException, IllegalArgumentException {
    _port( hostInfo );
  }

 /**
  * Request a PASV data transfer from the FTP server.
  *
  * @return    HostInfo for connecting to the remote data socket,
               or null if the results could not be parsed.
  *
  * @exception FTPException if the FTP server returns an error code.
  */
  public synchronized HostInfo pasv() throws FTPException {
    return _pasv();
  }

 /**
  * Get help from the FTP server.
  *
  * @param     item    item to get help on from server
  *
  * @exception FTPException if the FTP server returns an error code.
  */
  public synchronized String help(String item) throws FTPException {
    return _help( item );
  }

 /**
  * Send a raw command to the FTP server.
  *
  * @param     rawCmd    command to send to the server
  *
  * @exception FTPException if the FTP server returns an error code.
  */
  public synchronized void raw( String rawCmd ) throws FTPException {
    _raw( rawCmd );
  }

 /**
  * Send a noop to the FTP server.
  *
  * @exception FTPException if the FTP server returns an error code.
  */
  public synchronized void noop() throws FTPException {
    _noop();
  }

 /**
  * Get the last full reply from the FTP server.
  *
  * @return    the last reply from the FTP server.
  */
  public synchronized String getReply() {
    return reply;
  }

 /**
  * Get the last reply code from the FTP server.
  *
  * @return    the last reply code from the FTP server.
  */
  public synchronized int getReplyCode() {
    return replyCode;
  }

 /**
  * Get the last reply message from the FTP server.
  *
  * @return    the last reply message from the FTP server.
  */
  public synchronized String getReplyMessage() {
    return replyMessage;
  }

 /**
  * Send a command to the FTP server.
  * 
  * @param cmd   the command to send to the FTP server.  
  *
  * @exception FTPException if the FTP server returns an error code.
  */
  protected void sendCmd(String cmd) throws FTPException {
    sendCmd( cmd, null );
  }

 /**
  * Send a command to the FTP server with command masking for client reporting.
  * This is useful for PASS and ACCT so nobody sees what actually was sent.
  * 
  * @param cmd       the command to send to the FTP server.  
  * @param cmdMask   the command mask that will be returned to the client. 
  *
  * @exception FTPException if the FTP server returns an error code.
  */
  protected void sendCmd(String cmd, String cmdMask) 
                              throws FTPException {
    _sendCmd( cmd, cmdMask );
  }

 /**
  * Receive the message back from the FTP server.
  *
  * @exception FTPException if the FTP server returns an error code.
  */
  //protected synchronized void recvCmd() throws FTPException {
  protected void recvCmd() throws FTPException {
    _recvCmd();
  }

  /**
   * Forces passive data transfers to use the control socket IP address.
   *
   * @param on                true if to use control socket IP, false if off.
   */ 
  public void forcePasvToUseControlIP( boolean on ) {
    forcePasvToUseControlIP = on;
  } 

  /**
   * Sets the control channel IP address.
   *
   * @param ip                the control socket IP
   * @see #forcePasvToUseControlIP(boolean)
   */ 
  public void setControlIP( String ip ) {
    controlIP = ip;
  } 

  /*
   *
   * The methods below are here for obfuscation purposes.
   *
   */

  private void _setRecvCmdStream( OutputStream recvCmdStream ) {
    if ( recvCmdStream == null ) {
      recvCmdWriter = null;
    }
    else {
      recvCmdWriter = new OutputStreamWriter(recvCmdStream);
    }
  }

  private void _setSendCmdStream( OutputStream sendCmdStream ) {
    if ( sendCmdStream == null ) {
      sendCmdWriter = null;
    }
    else {
      sendCmdWriter = new OutputStreamWriter(sendCmdStream);
    }
  }

  private synchronized void _user(String username)
         throws FTPNeedPasswordException, FTPNeedAccountException,
                FTPBadLoginException, FTPException, IllegalArgumentException {
    if (username == null || username.trim().length() == 0)
      throw new IllegalArgumentException( "Missing username" );

    sendCmd("USER " + username);
    recvCmd();

    // Login was successful if reply code is 230
    if (replyCode == 230) { }

    else if (replyCode == 331)
      throw new FTPNeedPasswordException();

    else if (replyCode == 332)
      throw new FTPNeedAccountException();

    else if (replyCode == 530)
      throw new FTPBadLoginException(replyMessage);

    else
      throw new FTPException(replyMessage);
  }

  private synchronized void _pass(String password)
         throws FTPNeedAccountException, FTPBadLoginException,
                FTPException, IllegalArgumentException {
    if (password == null || password.trim().length() == 0)
      throw new IllegalArgumentException( "Missing password" );

    sendCmd("PASS " + password, "PASS **********");
    recvCmd();

    // Login was successful if reply code is 202 or 230
    if (replyCode == 202 || replyCode == 230) { }

    else if (replyCode == 332)
      throw new FTPNeedAccountException();

    else if (replyCode == 530)
      throw new FTPBadLoginException(replyMessage);

    else
      throw new FTPException(replyMessage);
  }

  private synchronized void _acct(String account)
         throws FTPBadLoginException, FTPException, IllegalArgumentException {
    if (account == null || account.trim().length() == 0)
      throw new IllegalArgumentException( "Missing account" );

    sendCmd("ACCT " + account, "ACCT **********");
    recvCmd();

    if (replyCode == 202 || replyCode == 230) { }

    else if (replyCode == 530)
      throw new FTPBadLoginException(replyMessage);

    else
      throw new FTPException(replyMessage);
  }

  private synchronized void _quit() throws FTPException {
    sendCmd("QUIT");
    recvCmd();

    if (replyCode != 221)
      throw new FTPException(replyMessage);
  }

  private synchronized String _syst() throws FTPException {
    sendCmd("SYST");
    recvCmd();

    //if ((replyCode / 200) != 1)
    if (replyCode < 200 || replyCode >= 300)
      throw new FTPException( replyMessage );

    return replyMessage;
  }

  private synchronized void _delete(String filename)
         throws FTPException, IllegalArgumentException {
    if (filename == null || filename.trim().length() == 0)
      throw new IllegalArgumentException( "Missing filename" );

    sendCmd("DELE " + filename);
    recvCmd();

    //if (replyCode != 250)
    if (replyCode < 200 || replyCode >= 300) 
      throw new FTPException(replyMessage);
  }

  private synchronized void _rename(String from, String to)
         throws FTPException, IllegalArgumentException {
    if (from == null || from.trim().length() == 0)
      throw new IllegalArgumentException( "Missing 'from' parameter" );

    if (to   == null || to.trim().length() == 0)
      throw new IllegalArgumentException( "Missing 'to' parameter" );

    sendCmd("RNFR " + from);
    recvCmd();

    if (replyCode != 350)
      throw new FTPException(replyMessage);

    sendCmd("RNTO " + to);
    recvCmd();

    //if (replyCode == 250) { }  // Rename was successful
    if (replyCode >= 200 && replyCode < 300) { } 

    else if (replyCode == 532)
      throw new FTPNeedAccountException(replyMessage);

    else
      throw new FTPException(replyMessage);
  }

  private synchronized void _mkdir(String dir)
         throws FTPException, FTPAccessDeniedException,
                IllegalArgumentException {
    if (dir == null || dir.trim().length() == 0)
      throw new IllegalArgumentException( "Missing 'dir' parameter" );

    sendCmd("MKD " + dir);
    recvCmd();

    if (replyCode != 257) {
      if ( replyMessage.indexOf("denied") > 0 ) {
        throw new FTPAccessDeniedException(replyMessage);
      }
      else {
        throw new FTPException(replyMessage);
      }
    }
  }

  private synchronized void _rmdir(String dir)
         throws FTPException, IllegalArgumentException {
    if (dir == null || dir.trim().length() == 0)
      throw new IllegalArgumentException( "Missing 'dir' parameter" );

    sendCmd("RMD " + dir);
    recvCmd();

    //if (replyCode != 250)
    if (replyCode < 200 || replyCode >= 300) 
      throw new FTPException(replyMessage);
  }

  private synchronized void _rest( long byteOffset ) throws FTPException {
    if ( byteOffset < 0 ) {
      byteOffset = 0;
    }

    sendCmd("REST " + byteOffset);
    recvCmd();

    if (replyCode != 350)
      throw new FTPException(replyMessage);
  }

  private synchronized Date _mdtm( String file ) throws FTPNoSuchFileException,
                                           FTPException, 
                                           IllegalArgumentException {
    Date result = null;

    if ( file == null || file.trim().length() == 0 ) 
      throw new IllegalArgumentException( "Missing 'file' parameter" );
   
    sendCmd("MDTM " + file);
    recvCmd();

    if ((replyCode / 550) == 1) {
       throw new FTPNoSuchFileException(replyMessage);
    }
    else if (replyCode / 213 != 1) {
       throw new FTPException(replyMessage);
    }

    int milliSecLoc = replyMessage.indexOf('.');
    String sDate = replyMessage;
    if ( milliSecLoc > 0 ) {
      sDate = replyMessage.substring(0, milliSecLoc);
    }
 
    // fix stupid y2k bug!
    if ( sDate.length() == 15 ) {
      StringBuffer strBuf = new StringBuffer( sDate );
      strBuf.delete( 0, 3 );
      strBuf.insert( 0, "20" );
      sDate = strBuf.toString();
    }

    try {
      //Calendar cal = Calendar.getInstance();
      result = mdtmFormat.parse( sDate + " GMT" );
    }
    catch ( ParseException pe ) {}

    return result;
  }
 
  private synchronized void _chdir(String dir)
         throws FTPNotADirectoryException, FTPNoSuchFileException,
                FTPPermissionDeniedException, FTPException, 
                IllegalArgumentException {
    if (dir == null || dir.trim().length() == 0)
      throw new IllegalArgumentException( "Missing 'dir' parameter" );

    sendCmd("CWD " + dir);
    recvCmd();

    //if (replyCode == 250) {
    if (replyCode >= 200 && replyCode < 300) {
      // status probably ok
      // unless the stupid server returns an ok code when it shouldn't!
      if ( replyMessage.trim().equals("<virtual directory>") &&
           !dir.trim().equals("/") )
        throw new FTPNotADirectoryException(replyMessage);
    }
    else if (replyCode == 550) {
      if (replyMessage.toLowerCase().indexOf("not a directory") >= 0 ||
          replyMessage.toLowerCase().indexOf("directory name is invalid") >= 0)
        throw new FTPNotADirectoryException(replyMessage);

      else if (replyMessage.toLowerCase().indexOf("no such") >= 0)
        throw new FTPNoSuchFileException(replyMessage);

      else if (replyMessage.toLowerCase().indexOf("permission denied") >= 0)
        throw new FTPPermissionDeniedException(replyMessage);

      else
        throw new FTPException(replyMessage);
    }
    else {
      throw new FTPException(replyMessage);
    }
  }

  private synchronized void _cdup() throws FTPException {
    sendCmd("CDUP");
    recvCmd();

    //if (replyCode != 250)
    if (replyCode < 200 || replyCode >= 300) 
      throw new FTPException(replyMessage);
  }

  private synchronized String _pwd() throws FTPException {
    String remoteDir = null;

    sendCmd("PWD");
    recvCmd();

    //if (replyCode != 257)
    if (replyCode < 200 || replyCode >= 300)
      throw new FTPException(replyMessage);

    String temp = replyMessage;

    int start = temp.indexOf('"');

    if (start >= 0) {
      int end = temp.indexOf('"', start + 1);

      if (end >= 0)
        remoteDir = temp.substring(start + 1, end);

      // there is some stupid FTP servers that don't properly send back
      // a pwd.
      if ( remoteDir != null && remoteDir.trim().equals("<virtual directory>") )
        remoteDir = "";
    }
    else {
      // there is some stupid FTP servers that don't properly send back
      // a pwd.
      if ( replyMessage.trim().equals("<virtual directory>") )
        remoteDir = "";
    }

    return remoteDir;
  }

  private void _abort( FTPData data ) throws FTPException {

    if ( data == null ) {
      throw new IllegalArgumentException( "Missing 'data' parameter" );
    }

    Socket control = data.getControlSocket();

    if ( control != null ) {
      byte interpretAs = (byte)255;
      byte interruptedProcess = (byte)244;

      try {
        Method urgentDataMethod = null;
        Class[] args = { int.class };
        urgentDataMethod = control.getClass().getMethod("sendUrgentData", args);
        if ( null != urgentDataMethod ) {
          control.sendUrgentData( interpretAs );
          control.sendUrgentData( interruptedProcess );
        }
      }
      catch ( SocketException se ) {
        // in case the sendUrgentData doesn't exist
      }
      catch ( Exception e ) {
        throw new FTPException( e.getMessage() );
      } 
    }
    
    sendCmd("ABOR");

    data.abortTransfer();

    recvCmd(); 

    // Aborted in time or data transfer error occurred
    if (replyCode == 226 || replyCode == 426 || replyCode == 550) {
      //String abortReply = replyMessage;

      try {
        // Still need to get response to the ABOR command
        recvCmd();
      }
      catch (Exception e) { 
      }

      if (replyCode == 425) {
        // transfer cancelled
	throw new FTPAbortException(replyMessage);
      }
      else if (replyCode < 200 || replyCode >= 300)
        throw new FTPException(replyMessage);
    }

    // Response for ABOR only, maybe not doing data transfer
    else if (replyCode == 225) {
      // Nothing else to get
      throw new FTPAbortException("ABOR command successful.");
    }

    else {
      throw new FTPException(replyMessage);
    }
  }

  private synchronized void _retrieve(String file, FTPData data)
         throws FTPException, IOException {
    if (file == null || file.trim().length() == 0)
      throw new IllegalArgumentException( "Missing 'file' parameter" );

    if (data == null)
      throw new IllegalArgumentException( "Missing 'data' parameter" );

    sendCmd("RETR " + file);

    // Start of transfer
    recvCmd();

    if (replyCode / 100 != 1)
      throw new FTPException(replyMessage);

    data.doTransfer();

    // End of transfer
    recvCmd();

    //if (replyCode / 200 != 1)
    if (replyCode < 200 || replyCode >= 300)
      throw new FTPException(replyMessage);
  }

  private synchronized void _store(String file, FTPData data)
         throws FTPException, IOException {
    if (file == null || file.trim().length() == 0)
      throw new IllegalArgumentException( "Missing 'file' parameter" );

    if (data == null)
      throw new IllegalArgumentException( "Missing 'data' parameter" );

    sendCmd("STOR " + file);

    // Start of transfer
    recvCmd();

    if (replyCode / 100 != 1)
      throw new FTPException(replyMessage);

    data.doTransfer();

    // End of transfer
    recvCmd();

    //if (replyCode / 200 != 1)
    if (replyCode < 200 || replyCode >= 300)
      throw new FTPException(replyMessage);
  }

  private synchronized void _append(String file, FTPData data)
         throws FTPException, IOException {
    if (file == null || file.trim().length() == 0)
      throw new IllegalArgumentException( "Missing 'file' parameter" );

    if (data == null)
      throw new IllegalArgumentException( "Missing 'data' parameter" );

    sendCmd("APPE " + file);

    // Start of transfer
    recvCmd();

    if (replyCode / 100 != 1)
      throw new FTPException(replyMessage);

    data.doTransfer();

    // End of transfer
    recvCmd();

    //if (replyCode / 200 != 1)
    if (replyCode < 200 || replyCode >= 300)
      throw new FTPException(replyMessage);
  }

  private synchronized void _type(char t) throws FTPException {
    sendCmd("TYPE " + t);
    recvCmd();

    //if (replyCode != 200)
    if (replyCode < 200 || replyCode >= 300)
      throw new FTPException(replyMessage);
  }

  private synchronized void _modeZ() throws FTPException {
    sendCmd("MODE Z");
    recvCmd();

    if ( replyCode < 200 || replyCode >= 300) {
      throw new FTPException(replyMessage);
    }
  }

  private synchronized long _size(String filename) throws FTPException {
    long value = -1;

    if (filename == null || filename.trim().length() == 0)
      throw new IllegalArgumentException( "Missing filename" );

    sendCmd("SIZE " + filename);
    recvCmd();

    try {
      //if ((replyCode / 200) == 1) {
      if (replyCode >= 200 && replyCode < 300) {
        value = Long.parseLong(replyMessage.trim());
      }
      else if ((replyCode / 550) == 1) {
        throw new FTPNoSuchFileException(replyMessage);
      }
      else {
        throw new FTPException(replyMessage);
      }
    }
    catch (NumberFormatException nfe) { }

    return value;
  }

  private synchronized void _list(String itemsToList, FTPData data, 
                                  boolean showHidden)
         throws FTPException, IOException {
    if (data == null)
      throw new IllegalArgumentException( "Missing 'data' parameter" );

    boolean listAll = showHidden;
    String listAllStr = System.getProperty("glub.disableListWithDashA");
    if ( null != listAllStr ) {
      Boolean value = new Boolean( listAllStr );
      listAll = value.booleanValue();
    }

    String listOptions = "";
    if ( listAll ) {
      listOptions = " -a";
    }

    if ( itemsToList != null && itemsToList.trim().length() > 0 ) {
      sendCmd("LIST" + listOptions + " " + itemsToList.trim());
    }
    else {
      sendCmd("LIST" + listOptions);
    }

    // Start of transfer
    recvCmd();

    if (replyCode / 100 != 1)
      throw new FTPException(replyMessage);

    data.doTransfer();

    // End of transfer
    recvCmd();

    //if (replyCode / 200 != 1)
    if (replyCode < 200 || replyCode >= 300)
      throw new FTPException(replyMessage);
  }

  private synchronized void _nlst(String itemsToList, FTPData data)
         throws FTPException, IOException {
    if (data == null)
      throw new IllegalArgumentException( "Missing 'data' parameter" );

    if ( itemsToList != null && itemsToList.trim().length() > 0 ) {
      sendCmd("NLST " + itemsToList.trim());
    }
    else {
      sendCmd("NLST");
    }

    // Start of transfer
    recvCmd();

    if (replyCode / 100 != 1)
      throw new FTPException(replyMessage);

    data.doTransfer();

    // End of transfer
    recvCmd();

    //if (replyCode / 200 != 1)
    if (replyCode < 200 || replyCode >= 300)
      throw new FTPException(replyMessage);
  }

  private synchronized void _port( HostInfo hostInfo )
                           throws FTPException, IllegalArgumentException {
    if (hostInfo == null)
      throw new IllegalArgumentException( "Missing hostInfo" );

    int port   = hostInfo.getPort();
    int hiPort = port >> 8;
    int loPort = port & 0xff;

    String hostAddress = hostInfo.getHostAddress();
    hostAddress = Util.searchAndReplace( hostAddress, ".", ",", true );
    sendCmd("PORT " + hostAddress + ',' + hiPort + ',' + loPort);
    recvCmd();

    //if (replyCode != 200)
    if (replyCode < 200 || replyCode >= 300)
      throw new FTPException(replyMessage);
  }

  private synchronized HostInfo _pasv() throws FTPException {
    HostInfo hostInfo = null;
    String dataAddr = null;
    int dataPort = -1;

    sendCmd("PASV");
    recvCmd();

    if (replyCode != 227)
      throw new FTPException(replyMessage);

    //String temp = replyMessage;

    try {
      REProgram pattern = 
        compiler.compile("(\\d+,\\d+,\\d+,\\d+),(\\d+),(\\d+)");

      matcher.setProgram(pattern);

      if (matcher.match(replyMessage)) {
        if ( forcePasvToUseControlIP ) {
          dataAddr = controlIP;
          if ( dataAddr == null )
            dataAddr = matcher.getParen(1).replace(',', '.');
        }
        else {
          dataAddr = matcher.getParen(1).replace(',', '.');
        }

        dataPort = (Integer.parseInt(matcher.getParen(2)) << 8)
          | Integer.parseInt(matcher.getParen(3));

        hostInfo = new HostInfo( dataAddr, dataPort );
      }
    }
    catch (Exception e) {
      // Leave hostInfo as null to indicate failure
    }

    return hostInfo;
  }

  private synchronized String _help(String item) throws FTPException {
    if ( item == null )
      item = "";

    String command = "HELP " + item;
    sendCmd(command.trim());
    recvCmd();

    if (replyCode != 214)
      throw new FTPException(replyMessage);

    return reply;
  }

  private synchronized void _raw( String rawCmd ) throws FTPException {
    sendCmd(rawCmd);
    recvCmd();
  }

  private synchronized void _noop() throws FTPException {
    sendCmd("NOOP");
    recvCmd();
  }

  private void _sendCmd(String cmd, String cmdMask) 
                              throws FTPException {
    cmdWriter.print(cmd + "\r\n");
    cmdWriter.flush();

    if ( sendCmdWriter != null ) {
      try {
        if ( cmdMask != null ) {
          sendCmdWriter.write(cmdMask);
        }
        else {
          sendCmdWriter.write(cmd);
        }
        sendCmdWriter.write(System.getProperty("line.separator"));
        sendCmdWriter.flush();
      }
      catch ( IOException ioe ) {
        throw new FTPException("Cannot log send command: " + ioe.getMessage());
      }
    }

    // Flush the stream and check its error state
    if (cmdWriter.checkError()) {
      throw new FTPConnectionLostException("Connection lost.");
    }
  }

  //private synchronized void _recvCmd() throws FTPException {
  private void _recvCmd() throws FTPException {
    StringWriter writer = new StringWriter();

    replyCode = -1;
    replyMessage = null;

    recvWrite(writer);

    reply = writer.toString();

    // strip the new line char at the end
    if ( reply.length() > 1 ) {
      reply = reply.substring(0, reply.length() - 1);
    }

    if ( recvCmdWriter != null && reply != null ) {
      try {
        recvCmdWriter.write(reply);
        recvCmdWriter.write(System.getProperty("line.separator"));
        recvCmdWriter.flush();
      }
      catch ( IOException ioe ) {
        throw new FTPException("Cannot log recv command: " + ioe.getMessage());
      }
    }

    try {
      replyMessage = reply.substring(4).trim();
    }
    catch (Exception e) {
      replyMessage = "";
    }

    if (replyCode == 421) {
      cleanup();
      throw new FTPConnectionLostException("Connection lost.");
    }
  }

  //private synchronized void recvWrite(Writer writer) throws FTPException {
  private void recvWrite(Writer writer) throws FTPException {
    String line;

    try {
      PrintWriter out = new PrintWriter(writer, false);
  
      if ((line = cmdReader.readLine()) == null) {
        return;
      }
      
      out.print(line + "\r\n");
      out.flush();
  
      if ( line.length() < 3 ) {
        return;
      }

      try {
        replyCode = Integer.parseInt(line.substring(0, 3));
      }
      catch (NumberFormatException nfe) {
    	out.close();
        throw new FTPException( line );
      }
  
      // Multi-line responses are indicated by a hyphen after the reply code
      if (line.charAt(3) != '-')
        return;
  
      while ((line = cmdReader.readLine()) != null) {
        //out.println(line);
        out.print(line + "\r\n");
        out.flush();
        out.close();

        if ( line.length() > 3 ) {
          try {
            int rc = Integer.parseInt(line.substring(0, 3));

            // Multi-line is done when the reply code is followed by a space
            if (line.charAt(3) == ' ' && rc == replyCode)
              break;
          }
          catch (NumberFormatException e) {
            // Multi-line responses do not need a reply code on every line,
            // so this exception may occur under normal circumstances.
          }
        }
      }
    }
    catch (InterruptedIOException iioe) {
      // NEED TO DO: handle this
      //debug.println("read interrupted: " + iioe.getMessage());
      throw new FTPException(iioe.getMessage());
    }
/*
    catch (javax.net.ssl.SSLException sse) {
      sse.printStackTrace();
    }
*/
    catch (IOException ioe) {
      throw new FTPConnectionLostException("Connection lost: " + 
                                           ioe.getMessage());
    }
  }

  /**
   * Housekeeping for the control socket.
   */
  private synchronized void cleanup() {
    Util.close( cmdReader );
    Util.close( cmdWriter );
  }

}

