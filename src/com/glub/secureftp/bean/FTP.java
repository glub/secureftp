
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: FTP.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.bean;

import com.glub.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.regexp.*;

/**
 * The <code>FTP</code> class is responsible for handling the basic operations
 * of the File Transfer Protocol.
 *
 * @author Gary Cohen
 * @version $Revision: 48 $, $Date: 2010-12-23 22:24:36 -0800 (Thu, 23 Dec 2010) $
 * @since 2.5.10
 */

public class FTP {
  /** The version of the bean. **/
  private static final String VERSION   = "2.5.14";

  /** The datestamp of the bean. **/
  private static String DATESTAMP 	= null;

  /** Used to set the data transfer mode to <code>ascii</code>. */
  public static final short ASCII_TRANSFER_MODE    = 0;

  /** Used to set the data transfer mode to <code>binary</code>; also 
   *  known as <code>image</code>. 
   */
  public static final short BINARY_TRANSFER_MODE   = 1;

  /** Used to set the data transfer mode to <code>auto</code>. This mode
   *  attempts to guess which transfer mode (ascii or binary) to set
   *  based on file type.
   */
  public static final short AUTO_TRANSFER_MODE     = 2;

  /** Used to set the data transfer mode to <code>ebcdic</code>. */
  public static final short EBCDIC_TRANSFER_MODE   = 3;

  /** Used to set the data connection type to <code>passive</code>. */
  public static final short PASV_CONNECTION_TYPE   = 0;

  /** Used to set the data connection type to <code>passive</code>. */
  public static final short PASSIVE_CONNECTION_TYPE= PASV_CONNECTION_TYPE;

  /** Used to set the data connection type to <code>active</code>. */
  public static final short ACTIVE_CONNECTION_TYPE = 1;

  /** This value is used to hold the hostname currently connected to. */
  private String hostName = null;

  /** This value is used to hold the port currently connected to. */
  private int port = 21;

  /** This value is used to hold the username we are currently connected as. */
  private String user = null;

  /** This value is used to hold the user's password. */
  private String password = null;

  /** This value is used to hold the user's account (if one exists). */
  private String account = null; 

  /** This is the control socket (or the command socket). */
  private Socket controlSocket = null;

  /** If mode z is enabled, this is true. */
  protected boolean modeZEnabled = false;

  /** If we are connected, this is true. */
  protected boolean isConnected = false;

  /** If we are logged in, this is true. */
  protected boolean isLoggedIn = false;

  /** This value is used to specify the trasnfer mode (ascii or binary). */
  private short transferMode = ASCII_TRANSFER_MODE;

  /** This value is used to specify the data connection type. */
  private short connectionType = PASV_CONNECTION_TYPE;

  /** 
   * This value is used to help the list engine determine how the handle
   * the list data from the server.
   */
  private short listStyle = FTPServerInfo.LIST_STYLE_UNKNOWN;

  /** 
   * This value sets whether or not we have tried to determine the listing
   * style.
   */ 
  private boolean listStyleSet = false;

  /** This value is used for our regular expressions (used in list). */
  private RECompiler compiler = new RECompiler();

  /** This value is used for our regular expressions (used in list). */
  private RE unixMatcher = new RE();
  private RE winMatcher = new RE();
  private RE netwareMatcher = new RE();
  private RE enginMatcher = new RE();

  /** This value denotes if the server supports the SIZE command */
  private boolean systemSupportsFileSizeCmd = true;

  /** This value denotes if the server supports the REST command */
  private boolean systemSupportsRestCmd = true;

  /** This value denotes if the server supports the PRET command */
  private boolean systemSupportsPretCmd = true;

  /** This value denotes if the server supports the OPTS command */
  private boolean systemSupportsOptsUTF8Cmd = true;

  /** This value denotes if the server should send the UTF8 listing */
  private boolean sendStringDataUTF8 = false;

  /** These values are used if an active port range is specifed */
  protected int lastPortFromRange = 0;
  protected int minPortInRange = 0;
  protected int maxPortInRange = 0;

  /** This stream is used to print the responses returned from the server. */
  protected OutputStream recvCmdStream = null;

  /** This stream is used to print the commands sent to the server. */
  protected OutputStream sendCmdStream = null;

  /** This handles the FTP commands. */
  protected FTPCommand command = null;

  /** Verify the jar is signed by Glub Tech */
  //private final boolean verifyJar = verifyJar(); 

  /** Force passive connections to use the same IP as the control channel */
  private boolean forcePasvToUseControlIP = false;

  /** Debug output */
  private boolean debug = GTOverride.getBoolean("glub.debug");

  /**
   * Create a new <code>FTP</code> object without response notification.
   *
   * @param  hostInfo      the HostInfo to connect to.
   */
  public FTP( HostInfo hostInfo ) {
    this( hostInfo.getHostName(), hostInfo.getPort(), null, null );
  }

  /**
   * Create a new <code>FTP</code> object without response notification.
   *
   * @param  host    the hostname to connect to.
   * @param  port    the port to connect to.
   */
  public FTP( String host, int port ) {
    this( host, port, null, null );
  }

  /**
   * Create a new <code>FTP</code> object with response notification.
   *
   * @param  hostInfo      the HostInfo to connect to.
   * @param  sendCmdStream the commands sent to the server. 
   *                       Pass <code>null</code> if not interested 
   *                       in this data.
   * @param  recvCmdStream the responses returned from the server.
   *                       Pass <code>null</code> if not interested
   *                       in this data.
   */
  public FTP( HostInfo hostInfo,
              OutputStream sendCmdStream, OutputStream recvCmdStream ) {
    this( hostInfo.getHostName(), hostInfo.getPort(), sendCmdStream, 
          recvCmdStream );
  }

  /**
   * Create a new <code>FTP</code> object with response notification.
   *
   * @param  host          the hostname to connect to.
   * @param  port          the port to connect to.
   * @param  sendCmdStream the commands sent to the server. 
   *                       Pass <code>null</code> if not interested 
   *                       in this data.
   * @param  recvCmdStream the responses returned from the server.
   *                       Pass <code>null</code> if not interested
   *                       in this data.
   */
  public FTP( String host, int port, 
              OutputStream sendCmdStream, OutputStream recvCmdStream ) {
    setHostName(host);
    setPort(port);
    setSendCmdStream( sendCmdStream );
    setRecvCmdStream( recvCmdStream );
  }

  /**
   * Connect to the FTP host and port. If the port was not set, we default
   * to 21.
   *
   * @throws FTPConnectException      if the connection fails.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there are socket problems.
   * @throws UnknownHostException     if the host could not be found.
   * @throws IllegalArgumentException if <code>hostName</code> is 
   *                                  <code>null</code>.
   */
  public void connect() throws FTPConnectException, FTPException, IOException, 
                               UnknownHostException, IllegalArgumentException {
    _connect();
  }

  /**
   * Login to the FTP server.
   * 
   * @param user the username to login as.
   * @param pass the password to login as.
   *
   * @throws IOException              if there is a socket problem.
   * @throws FTPBadLoginException     if there is a problem logging in.
   * @throws FTPConnectException      if this is called prior to 
   *                                  <code>connect</code>
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IllegalArgumentException if there is a problem with any of the
   *                                  passed in args.
   *
   * @see #connect()
   */
  public void login( String user, String pass ) 
              throws IOException, FTPBadLoginException, FTPConnectException,
                     FTPException, IllegalArgumentException {
    login( user, pass, null );
  }

  /**
   * Login to the FTP server.
   * 
   * @param user the username to login as.
   * @param pass the password to login as.
   * @param acct the account to use.
   *
   * @throws IOException              if there is a socket problem.
   * @throws FTPBadLoginException     if there is a problem logging in.
   * @throws FTPConnectException      if this is called prior to 
   *                                  <code>connect</code>
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IllegalArgumentException if there is a problem with any of the
   *                                  passed in args.
   *
   * @see #connect()
   */
  public void login( String user, String pass, String acct ) 
              throws IOException, FTPBadLoginException, FTPConnectException,
                     FTPException, IllegalArgumentException {
    _login( user, pass, acct );
  }

  /**
   * Logout from the FTP server.
   *
   * @throws IOException               if there is a socket problem.
   * @throws FTPException              if the FTP server returns an error code.
   */
  public void logout() throws IOException, FTPException {
    _logout();
  }

  /**
   * Sends the username to the FTP server.
   *
   * @param user the username.
   *
   * @throws FTPNeedPasswordException if a password is required. 
   * @throws FTPNeedAccountException  if an accound is required.
   * @throws FTPBadLoginException     if there is a problem logging in.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IllegalArgumentException if there is a problem with the username.
   */
  public void sendUserName( String user ) throws FTPNeedPasswordException,
                                                 FTPNeedAccountException,
                                                 FTPBadLoginException,
                                                 FTPException,
                                                 IllegalArgumentException {
    _sendUserName( user );
  }

  /**
   * Sends the password to the FTP server.
   *
   * @param pass the password.
   *
   * @throws FTPNeedAccountException  if an accound is required.
   * @throws FTPBadLoginException     if there is a problem logging in.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IllegalArgumentException if there is a problem with the password.
   */
  public void sendPassword( String pass ) throws FTPNeedAccountException,
                                                 FTPBadLoginException,
                                                 FTPException,
                                                 IllegalArgumentException {
    _sendPassword( pass );
  }

  /**
   * Sends the account to the FTP server.
   *
   * @param acct the account.
   *
   * @throws FTPBadLoginException     if there is a problem logging in.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IllegalArgumentException if there is a problem with the account.
   */
  public void sendAccount( String acct ) throws FTPBadLoginException,
                                                FTPException, 
                                                IllegalArgumentException {
    _sendAccount( acct );
  }

  /**
   * List the current remote directory, including hidden files. Subject
   * to availiblity on certain servers.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @return a list of <code>RemoteFile</code> objects as a 
   *         <code>RemoteFileList</code>.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket problem.
   *
   * @see RemoteFile
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public RemoteFileList listAll() throws FTPException, IOException {
    return list( (String)null, null, true );
  }

  /**
   * List the current remote directory.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @return a list of <code>RemoteFile</code> objects as a 
   *         <code>RemoteFileList</code>.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket problem.
   *
   * @see RemoteFile
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public RemoteFileList list() throws FTPException, IOException {
    return list( (String)null, null, false );
  }

  /**
   * List the current remote directory with the ability to abort the listing.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @return a list of <code>RemoteFile</code> objects as a 
   *         <code>RemoteFileList</code>.
   *
   * @param abort an Object that allows for the abortion of the 
   *              <code>list</code>.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket problem.
   *
   * @see RemoteFile
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public RemoteFileList list( FTPAbortableTransfer abort ) throws FTPException, 
                                                                  IOException {
    return list( (String)null, abort, false );
  }

  /**
   * List the current remote directory including hidden files with the 
   * ability to abort the listing.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @return a list of <code>RemoteFile</code> objects as a 
   *         <code>RemoteFileList</code>.
   *
   * @param abort an Object that allows for the abortion of the 
   *              <code>list</code>.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket problem.
   *
   * @see RemoteFile
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public RemoteFileList listAll( FTPAbortableTransfer abort ) 
                                             throws FTPException, IOException {
    return list( (String)null, abort, true );
  }

  /**
   * List items on the remote FTP server.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param file         a <code>RemoteFile</code> you
   *                     want to to list from the FTP server. 
   *                     Pass <code>null</code> to list the current directory.
   *
   * @return a list of <code>RemoteFile</code> objects as a 
   *         <code>RemoteFileList</code>.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket problem.
   *
   * @see RemoteFile
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public RemoteFileList list( RemoteFile file )
                                              throws FTPException, IOException {
    return list( file, null, false );
  }

  /**
   * List items on the remote FTP server.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param itemsToList  a space-delimited <code>String</code> of items you 
   *                     want to to list from the FTP server. 
   *                     Pass <code>null</code> to list the current directory.
   *
   * @return a list of <code>RemoteFile</code> objects as a 
   *         <code>RemoteFileList</code>.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket problem.
   *
   * @see RemoteFile
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public RemoteFileList list( String itemsToList )
                                              throws FTPException, IOException {
    return list( itemsToList, null, false );
  }

  /**
   * List items on the remote FTP server with the ability to abort the listing.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param file         a <code>RemoteFile</code> want to to list from the 
   *                     FTP server. 
   * @param abort        an Object that allows for the abortion of the 
   *                     <code>list</code>.
   * @param showHidden   flag to show hidden files (only useful for empty list)
   *
   * @return a list of <code>RemoteFile</code> objects as a 
   *         <code>RemoteFileList</code>.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket problem.
   *
   * @see RemoteFile
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public RemoteFileList list( RemoteFile file, FTPAbortableTransfer abort,
                              boolean showHidden )
                                              throws FTPException, IOException {
    String itemToList = file.getFileName();

    boolean setPWD = false;

    if ( null != file.getMetaData("pwd") ) {
      itemToList = file.getMetaData("pwd") + itemToList;
      setPWD = true;
    }

    // in case there are spaces in the name
    itemToList = Util.searchAndReplace( itemToList, " ", "*", true );

    RemoteFileList result = list( itemToList, abort, showHidden );

    if ( setPWD ) {
      String pwd = pwd();
      if ( file.isDirectory() ) {
        pwd += "/" + file.getFileName() + "/";
      }

      for ( int i = 0; i < result.size(); i++ ) {
        result.getFile(i).setMetaData("pwd", pwd);
      } 
    }

    return result;
  }

  /**
   * List items on the remote FTP server with the ability to abort the listing.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param itemsToList  a space-delimited <code>String</code> of items you 
   *                     want to to list from the FTP server. 
   *                     Pass <code>null</code> to list the current directory.
   * @param abort        an Object that allows for the abortion of the 
   *                     <code>list</code>.
   * @param showHidden   flag to show hidden files (only useful for empty list)
   *
   * @return a list of <code>RemoteFile</code> objects as a 
   *         <code>RemoteFileList</code>.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket problem.
   *
   * @see RemoteFile
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public RemoteFileList list( String itemsToList, FTPAbortableTransfer abort,
                              boolean showHidden )
                                              throws FTPException, IOException {
    aboutToTransferData();
    return _list( itemsToList, abort, showHidden );
  }

  /**
   * Retrieve a file from the FTP server.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param remoteFile   the name of the remote file that
   *                     exists on the FTP server.
   * @param outputFile   a local file that will act as storage for the
   *                     downloaded file. If this parameter is 
   *                     <code>null</code>, a local file will be created with 
   *                     the remote file's name in the current local 
   *                     directory as specified by <code>user.dir</code>.
   * @param restartXfer  restart an interrupted transfer (if available).
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void retrieve( String remoteFile, File outputFile, 
                        boolean restartXfer ) 
                        throws FTPRestartNotSupportedException, 
                               FTPException, IOException {
    retrieve( new RemoteFile(remoteFile), outputFile, restartXfer, null, null );
  }

  /**
   * Retrieve a file from the FTP server.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param remoteFile   the RemoteFile that exists on the FTP server.
   * @param outputFile   a local file that will act as storage for the
   *                     downloaded file. If this parameter is 
   *                     <code>null</code>, a local file will be created with 
   *                     the remote file's name in the current local 
   *                     directory as specified by <code>user.dir</code>.
   * @param restartXfer  restart an interrupted transfer (if available).
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void retrieve( RemoteFile remoteFile, File outputFile, 
                        boolean restartXfer ) 
                        throws FTPRestartNotSupportedException, 
                               FTPException, IOException {
    retrieve( remoteFile, outputFile, restartXfer, null, null );
  }

  /**
   * Retrieve a file from the FTP server.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param remoteFile    the name of the remote file that
   *                      exists on the FTP server.
   * @param outputStream  an output stream that will act as storage for the
   *                      downloaded file.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void retrieve( String remoteFile, OutputStream outputStream ) 
                        throws FTPRestartNotSupportedException, 
                               FTPException, IOException {
    retrieve( new RemoteFile(remoteFile), outputStream, null, null );
  }

  /**
   * Retrieve a file from the FTP server.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param remoteFile    the RemoteFile that exists on the FTP server.
   * @param outputStream  an output stream that will act as storage for the
   *                      downloaded file.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void retrieve( RemoteFile remoteFile, OutputStream outputStream ) 
                        throws FTPRestartNotSupportedException, 
                               FTPException, IOException {
    retrieve( remoteFile, outputStream, null, null );
  }

  /**
   * Retrieve a file from the FTP server with the ability to abort the transfer.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param remoteFile   the name of the remote file that
   *                     exists on the FTP server.
   * @param outputFile   a local file that will act as storage for the
   *                     downloaded file. If this parameter is 
   *                     <code>null</code>, a local file will be created with 
   *                     the remote file's name in the current local 
   *                     directory as specified by <code>user.dir</code>.
   * @param restartXfer  restart an interrupted transfer (if available).
   * @param abort        an Object that allows for the abortion of the 
   *                     <code>retrieve</code>.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void retrieve( String remoteFile, File outputFile, boolean restartXfer,
                        FTPAbortableTransfer abort )
                               throws FTPRestartNotSupportedException, 
                                      FTPException, IOException {
    retrieve( new RemoteFile(remoteFile), outputFile, restartXfer, 
              null, abort );
  }

  /**
   * Retrieve a file from the FTP server with the ability to abort the transfer.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param remoteFile   the RemoteFile that exists on the FTP server.
   * @param outputFile   a local file that will act as storage for the
   *                     downloaded file. If this parameter is 
   *                     <code>null</code>, a local file will be created with 
   *                     the remote file's name in the current local 
   *                     directory as specified by <code>user.dir</code>.
   * @param restartXfer  restart an interrupted transfer (if available).
   * @param abort        an Object that allows for the abortion of the 
   *                     <code>retrieve</code>.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void retrieve( RemoteFile remoteFile, File outputFile, 
                        boolean restartXfer, FTPAbortableTransfer abort )
                               throws FTPRestartNotSupportedException, 
                                      FTPException, IOException {
    retrieve( remoteFile, outputFile, restartXfer, null, abort );
  }

  /**
   * Retrieve a file from the FTP server with the ability to abort the transfer.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param remoteFile    the name of the remote file that
   *                      exists on the FTP server.
   * @param outputStream  an output stream that will act as storage for the
   *                      downloaded file.
   * @param abort         an Object that allows for the abortion of the 
   *                      <code>retrieve</code>.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void retrieve( String remoteFile, OutputStream outputStream,
                        FTPAbortableTransfer abort )
                               throws FTPRestartNotSupportedException, 
                                      FTPException, IOException {
    retrieve( new RemoteFile(remoteFile), outputStream, null, abort );
  }

  /**
   * Retrieve a file from the FTP server with the ability to abort the transfer.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param remoteFile    the RemoteFile that exists on the FTP server.
   * @param outputStream  an output stream that will act as storage for the
   *                      downloaded file.
   * @param abort         an Object that allows for the abortion of the 
   *                      <code>retrieve</code>.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void retrieve( RemoteFile remoteFile, OutputStream outputStream, 
                        FTPAbortableTransfer abort )
                               throws FTPRestartNotSupportedException, 
                                      FTPException, IOException {
    retrieve( remoteFile, outputStream, null, abort );
  }

  /**
   * Retrieve a file from the FTP server with progress information.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param remoteFile   the name of the remote file that
   *                     exists on the FTP server.
   * @param outputFile   a local file that will act as storage for the
   *                     downloaded file. If this parameter is 
   *                     <code>null</code>, a local file will be created with 
   *                     the remote file's name in the current local 
   *                     directory as specified by <code>user.dir</code>.
   * @param restartXfer  restart an interrupted transfer (if available).
   * @param progress     a <code>Progress</code> object which is used to update 
   *                     download status.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void retrieve( String remoteFile, File outputFile, boolean restartXfer,
                        Progress progress ) 
                        throws FTPRestartNotSupportedException,
                               FTPException, IOException {
    retrieve( new RemoteFile(remoteFile), outputFile, restartXfer, 
              progress, null );
  }

  /**
   * Retrieve a file from the FTP server with progress information.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param remoteFile   the RemoteFile that exists on the FTP server.
   * @param outputFile   a local file that will act as storage for the
   *                     downloaded file. If this parameter is 
   *                     <code>null</code>, a local file will be created with 
   *                     the remote file's name in the current local 
   *                     directory as specified by <code>user.dir</code>.
   * @param restartXfer  restart an interrupted transfer (if available).
   * @param progress     a <code>Progress</code> object which is used to update 
   *                     download status.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void retrieve( RemoteFile remoteFile, File outputFile, 
                        boolean restartXfer, Progress progress ) 
                        throws FTPRestartNotSupportedException,
                               FTPException, IOException {
    retrieve( remoteFile, outputFile, restartXfer, progress, null );
  }

  /**
   * Retrieve a file from the FTP server with progress information.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param remoteFile    the name of the remote file that
   *                      exists on the FTP server.
   * @param outputStream  an output stream that will act as storage for the
   *                      downloaded file.
   * @param progress      a <code>Progress</code> object which is used to 
   *                      update download status.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void retrieve( String remoteFile, OutputStream outputStream,
                        Progress progress ) 
                        throws FTPRestartNotSupportedException,
                               FTPException, IOException {
    retrieve( new RemoteFile(remoteFile), outputStream, progress, null );
  }

  /**
   * Retrieve a file from the FTP server with progress information.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param remoteFile    the RemoteFile that exists on the FTP server.
   * @param outputStream  an output stream that will act as storage for the
   *                      downloaded file.
   * @param progress      a <code>Progress</code> object which is used to  
   *                      update download status.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void retrieve( RemoteFile remoteFile, OutputStream outputStream, 
                        Progress progress ) 
                        throws FTPRestartNotSupportedException,
                               FTPException, IOException {
    retrieve( remoteFile, outputStream, progress, null );
  }

  /**
   * Retrieve a file from the FTP server with progress information and 
   * the ability to abort the transfer.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param remoteFile   the name of the remote file that
   *                     exists on the FTP server.
   * @param outputFile   a local file that will act as storage for the
   *                     downloaded file. If this parameter is 
   *                     <code>null</code>, a local file will be created with 
   *                     the remote file's name in the current local 
   *                     directory as specified by <code>user.dir</code>.
   * @param restartXfer  restart an interrupted transfer (if available).
   * @param progress     a <code>Progress</code> object which is used to update 
   *                     download status.
   * @param abort        an Object that allows for the abortion of the 
   *                     <code>retrieve</code>.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void retrieve( String remoteFile, File outputFile, boolean restartXfer,
                        Progress progress, FTPAbortableTransfer abort )
                        throws FTPRestartNotSupportedException,
                               FTPException, IOException {
    retrieve( new RemoteFile(remoteFile), outputFile, restartXfer, 
              progress, abort );
  }

  /**
   * Retrieve a file from the FTP server with progress information and 
   * the ability to abort the transfer.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param remoteFile   the RemoteFile that exists on the FTP server.
   * @param outputFile   a local file that will act as storage for the
   *                     downloaded file. If this parameter is 
   *                     <code>null</code>, a local file will be created with 
   *                     the remote file's name in the current local 
   *                     directory as specified by <code>user.dir</code>.
   * @param restartXfer  restart an interrupted transfer (if available).
   * @param progress     a <code>Progress</code> object which is used to update 
   *                     download status.
   * @param abort        an Object that allows for the abortion of the 
   *                     <code>retrieve</code>.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void retrieve( RemoteFile remoteFile, File outputFile, 
                        boolean restartXfer,
                        Progress progress, FTPAbortableTransfer abort )
                        throws FTPRestartNotSupportedException,
                               FTPException, IOException {
    aboutToTransferData();
    _retrieve( remoteFile, outputFile, restartXfer, progress, abort );
  }

  /**
   * Retrieve a file from the FTP server with progress information and 
   * the ability to abort the transfer.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param remoteFile    the name of the remote file that
   *                      exists on the FTP server.
   * @param outputStream  an output stream that will act as storage for the
   *                      downloaded file.
   * @param progress      a <code>Progress</code> object which is used to 
   *                      update download status.
   * @param abort         an Object that allows for the abortion of the 
   *                      <code>retrieve</code>.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void retrieve( String remoteFile, OutputStream outputStream,
                        Progress progress, FTPAbortableTransfer abort )
                        throws FTPRestartNotSupportedException,
                               FTPException, IOException {
    retrieve( new RemoteFile(remoteFile), outputStream, progress, abort );
  }

  /**
   * Retrieve a file from the FTP server with progress information and 
   * the ability to abort the transfer.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param remoteFile    the RemoteFile that exists on the FTP server.
   * @param outputStream  an output stream that will act as storage for the
   *                      downloaded file.
   * @param progress      a <code>Progress</code> object which is used to 
   *                      update download status.
   * @param abort         an Object that allows for the abortion of the 
   *                      <code>retrieve</code>.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void retrieve( RemoteFile remoteFile, OutputStream outputStream, 
                        Progress progress, FTPAbortableTransfer abort )
                        throws FTPRestartNotSupportedException,
                               FTPException, IOException {
    aboutToTransferData();
    _retrieveByStream( remoteFile, outputStream, false, progress, abort, 0 );
  }

  /**
   * Store a file to the FTP server.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param file         the local file you want to upload.
   * @param restartXfer  restart an interrupted transfer (if available).
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void store( File file, boolean restartXfer ) 
                     throws FTPRestartNotSupportedException,
                            FTPException, IOException,
                            IllegalArgumentException {
    store( file, null, restartXfer, null, null );
  }

  /**
   * Store a file to the FTP server with a specific filename.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param file         the local file you want to upload.
   * @param name         the name you want to save the file as.
   * @param restartXfer  restart an interrupted transfer (if available).
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void store( File file, String name, boolean restartXfer ) 
                     throws FTPRestartNotSupportedException,
                            FTPException, IOException,
                            IllegalArgumentException {
    store( file, name, restartXfer, null, null );
  }

  /**
   * Store a file to the FTP server.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param inputStream     the "local file" input stream you want to upload.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void store( InputStream inputStream ) 
                     throws FTPRestartNotSupportedException,
                            FTPException, IOException,
                            IllegalArgumentException {
    store( inputStream, 0, null, null, null );
  }

  /**
   * Store a file to the FTP server with a specific filename.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param inputStream  the "local file" input stream you want to upload.
   * @param name         the name you want to save the file as.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void store( InputStream inputStream, String name ) 
                     throws FTPRestartNotSupportedException,
                            FTPException, IOException,
                            IllegalArgumentException {
    store( inputStream, 0, name, null, null );
  }

  /**
   * Store a file to the FTP server with the ability to abort the data transfer.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param file         the local file you want to upload.
   * @param restartXfer  restart an interrupted transfer (if available).
   * @param abort        an Object that allows for the abortion of the 
   *                     <code>store</code>.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void store( File file, boolean restartXfer, 
                     FTPAbortableTransfer abort ) 
                     throws FTPRestartNotSupportedException, FTPException, 
                            IOException, IllegalArgumentException {
    store( file, null, restartXfer, null, abort );
  }

  /**
   * Store a file to the FTP server with a specific filename and 
   * with the ability to abort the data transfer.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param file         the local file you want to upload.
   * @param name         the name you want to save the file as.
   * @param restartXfer  restart an interrupted transfer (if available).
   * @param abort        an Object that allows for the abortion of the 
   *                     <code>store</code>.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void store( File file, String name, boolean restartXfer, 
                     FTPAbortableTransfer abort ) 
                     throws FTPRestartNotSupportedException, FTPException, 
                            IOException, IllegalArgumentException {
    store( file, name, restartXfer, null, abort );
  }

  /**
   * Store a file to the FTP server with the ability to abort the data transfer.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param inputStream  the "local file" input stream you want to upload.
   * @param abort        an Object that allows for the abortion of the 
   *                     <code>store</code>.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void store( InputStream inputStream, FTPAbortableTransfer abort ) 
                     throws FTPRestartNotSupportedException, FTPException, 
                            IOException, IllegalArgumentException {
    store( inputStream, 0, null, null, abort );
  }

  /**
   * Store a file to the FTP server with a specific filename and 
   * with the ability to abort the data transfer.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param inputStream  the "local file" input stream you want to upload.
   * @param name         the name you want to save the file as.
   * @param abort        an Object that allows for the abortion of the 
   *                     <code>store</code>.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void store( InputStream inputStream, String name,
                     FTPAbortableTransfer abort ) 
                     throws FTPRestartNotSupportedException, FTPException, 
                            IOException, IllegalArgumentException {
    store( inputStream, 0, name, null, abort );
  }

  /**
   * Store a file to the FTP server with progress information.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param file         the local file you want to upload.
   * @param restartXfer  restart an interrupted transfer (if available).
   * @param progress     a <code>Progress</code> object which is used to update 
   *                     upload status.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void store( File file, boolean restartXfer, Progress progress )
                     throws FTPRestartNotSupportedException, FTPException, 
                            IOException, IllegalArgumentException {
    store( file, null, restartXfer, progress, null );
  }

  /**
   * Store a file to the FTP server with a specific filename and 
   * with progress information.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param file         the local file you want to upload.
   * @param name         the name you want to save the file as.
   * @param restartXfer  restart an interrupted transfer (if available).
   * @param progress     a <code>Progress</code> object which is used to update 
   *                     upload status.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void store( File file, String name, boolean restartXfer, 
                     Progress progress )
                     throws FTPRestartNotSupportedException, FTPException, 
                            IOException, IllegalArgumentException {
    store( file, name, restartXfer, progress, null );
  }

  /**
   * Store a file to the FTP server with progress information.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param inputStream   the "local file" input stream you want to upload.
   * @param localFileSize the size of the "local file".
   * @param progress      a <code>Progress</code> object which is used to  
   *                      update upload status.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void store( InputStream inputStream, long localFileSize, 
                     Progress progress )
                     throws FTPRestartNotSupportedException, FTPException, 
                            IOException, IllegalArgumentException {
    store( inputStream, localFileSize, null, progress, null );
  }

  /**
   * Store a file to the FTP server with a specific filename and 
   * with progress information.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param inputStream   the "local file" input stream you want to upload.
   * @param localFileSize the size of the "local file".
   * @param name          the name you want to save the file as.
   * @param progress      a <code>Progress</code> object which is used to  
   *                      update upload status.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void store( InputStream inputStream, long localFileSize, String name,
                     Progress progress )
                     throws FTPRestartNotSupportedException, FTPException, 
                            IOException, IllegalArgumentException {
    store( inputStream, localFileSize, name, progress, null );
  }

  /**
   * Store a file to the FTP server with progress information and the ability
   * to abort the data transfer.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param file         the local file you want to upload.
   * @param restartXfer  restart an interrupted transfer (if available).
   * @param progress     a <code>Progress</code> object which is used to update 
   *                     upload status.
   * @param abort        an Object that allows for the abortion of the 
   *                     <code>store</code>.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void store( File file, boolean restartXfer, Progress progress, 
                     FTPAbortableTransfer abort ) 
                     throws FTPRestartNotSupportedException, FTPException,
                            IOException, IllegalArgumentException {
    store( file, null, restartXfer, progress, abort );
  }

  /**
   * Store a file to the FTP server with a specific filename and
   * progress information and the ability to abort the data transfer.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param file         the local file you want to upload.
   * @param name         the name you want to save the file as.
   * @param restartXfer  restart an interrupted transfer (if available).
   * @param progress     a <code>Progress</code> object which is used to update 
   *                     upload status.
   * @param abort        an Object that allows for the abortion of the 
   *                     <code>store</code>.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void store( File file, String name, boolean restartXfer, 
                     Progress progress, FTPAbortableTransfer abort ) 
                     throws FTPRestartNotSupportedException, FTPException,
                            IOException, IllegalArgumentException {
    aboutToTransferData();
    _store( file, name, restartXfer, progress, abort );
  }

  /**
   * Store a file to the FTP server with progress information and the ability
   * to abort the data transfer.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param inputStream   the "local file" input stream you want to upload.
   * @param localFileSize the size of the "local file".
   * @param progress      a <code>Progress</code> object which is used to 
   *                      update upload status.
   * @param abort         an Object that allows for the abortion of the 
   *                      <code>store</code>.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void store( InputStream inputStream, long localFileSize, 
                     Progress progress, FTPAbortableTransfer abort ) 
                     throws FTPRestartNotSupportedException, FTPException,
                            IOException, IllegalArgumentException {
    store( inputStream, localFileSize, null, progress, abort );
  }

  /**
   * Store a file to the FTP server with a specific filename and
   * progress information and the ability to abort the data transfer.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param inputStream   the "local file" input stream you want to upload.
   * @param localFileSize the size of the "local file".
   * @param name          the name you want to save the file as.
   * @param progress      a <code>Progress</code> object which is used to 
   *                      update upload status.
   * @param abort         an Object that allows for the abortion of the 
   *                      <code>store</code>.
   *
   * @throws FTPRestartNotSupportedException if the FTP server doesn't support
   *                                         restarting incomplete file xfer.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void store( InputStream inputStream, long localFileSize, String name, 
                     Progress progress, FTPAbortableTransfer abort ) 
                     throws FTPRestartNotSupportedException, FTPException,
                            IOException, IllegalArgumentException {
    aboutToTransferData();
    _storeByStream( inputStream, name, false, progress, abort, 
                    -1L, localFileSize );
  }

  /**
   * Append to a file on the FTP server.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param file         the local file you want to upload.
   * @param appendTo     the remote file name you want to append to.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void append( File file, String appendTo )
                                              throws FTPException, IOException {
    append( file, new RemoteFile(appendTo), null, null );
  }

  /**
   * Append to a file on the FTP server.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param file         the local file you want to upload.
   * @param appendTo     the <code>RemoteFile</code> you want to append to.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void append( File file, RemoteFile appendTo )
                                              throws FTPException, IOException {
    append( file, appendTo, null, null );
  }

  /**
   * Append to a file on the FTP server.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param inputStream   the "local file" stream you want to upload.
   * @param appendTo      the remote file name you want to append to.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void append( InputStream inputStream, String appendTo ) 
                                        throws FTPException, IOException,
                                               IllegalArgumentException {
    append( inputStream, 0, new RemoteFile(appendTo), null, 
            null );
  }

  /**
   * Append to a file on the FTP server.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param inputStream   the "local file" stream you want to upload.
   * @param appendTo      the <code>RemoteFile</code> you want to append to.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void append( InputStream inputStream, RemoteFile appendTo ) 
                                        throws FTPException, IOException,
                                               IllegalArgumentException {
    append( inputStream, 0, appendTo, null, null );
  }

  /**
   * Append to a file on the FTP server with the ability to abort the data 
   * transfer.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param file         the local file you want to upload.
   * @param appendTo     the remote file name you want to append to.
   * @param abort        an Object that allows for the abortion of the 
   *                     <code>append</code>.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void append( File file, String appendTo, FTPAbortableTransfer abort )
                                              throws FTPException, IOException {
    append( file, new RemoteFile(appendTo), null, abort );
  }

  /**
   * Append to a file on the FTP server with the ability to abort the data 
   * transfer.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param file         the local file you want to upload.
   * @param appendTo     the <code>RemoteFile</code> you want to append to.
   * @param abort        an Object that allows for the abortion of the 
   *                     <code>append</code>.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void append( File file, RemoteFile appendTo, 
                      FTPAbortableTransfer abort ) throws FTPException, 
                                                          IOException {
    append( file, appendTo, null, abort );
  }

  /**
   * Append to a file on the FTP server with the ability to abort the data 
   * transfer.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param inputStream   the "local file" stream you want to upload.
   * @param appendTo      the remote file name you want to append to.
   * @param abort         an Object that allows for the abortion of the 
   *                      <code>append</code>.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void append( InputStream inputStream, String appendTo, 
                      FTPAbortableTransfer abort ) throws FTPException, 
                                                     IOException,
                                                     IllegalArgumentException {
    append( inputStream, 0, new RemoteFile(appendTo), null, abort );
  }

  /**
   * Append to a file on the FTP server with the ability to abort the data 
   * transfer.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param inputStream   the "local file" stream you want to upload.
   * @param appendTo      the <code>RemoteFile</code> you want to append to.
   * @param abort         an Object that allows for the abortion of the 
   *                      <code>append</code>.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void append( InputStream inputStream, RemoteFile appendTo, 
                                               FTPAbortableTransfer abort )
                                              throws FTPException, 
                                                     IOException,
                                                     IllegalArgumentException {
    append( inputStream, 0, appendTo, null, abort );
  }

  /**
   * Append to a file on the FTP server with progress information.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param file         the local file you want to upload.
   * @param appendTo     the remote file name you want to append to.
   * @param progress     a <code>Progress</code> object which is used to update 
   *                     upload status.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void append( File file, String appendTo, Progress progress )
                                              throws FTPException, 
                                                     IOException,
                                                     IllegalArgumentException {
    append( file, new RemoteFile(appendTo), progress, null );
  }

  /**
   * Append to a file on the FTP server with progress information.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param file         the local file you want to upload.
   * @param appendTo     the <code>RemoteFile</code> you want to append to.
   * @param progress     a <code>Progress</code> object which is used to update 
   *                     upload status.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void append( File file, RemoteFile appendTo, Progress progress )
                                              throws FTPException, 
                                                     IOException,
                                                     IllegalArgumentException {
    append( file, appendTo, progress, null );
  }

  /**
   * Append to a file on the FTP server with progress information.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param inputStream   the "local file" stream you want to upload.
   * @param localFileSize the size of the "local file"
   * @param appendTo      the remote file name you want to append to.
   * @param progress      a <code>Progress</code> object which is used to 
   *                      update upload status.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void append( InputStream inputStream, long localFileSize, 
                      String appendTo, Progress progress )
                                              throws FTPException, 
                                                     IOException,
                                                     IllegalArgumentException {
    append( inputStream, localFileSize, new RemoteFile(appendTo), progress, 
            null );
  }

  /**
   * Append to a file on the FTP server with progress information.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param inputStream   the "local file" stream you want to upload.
   * @param localFileSize the size of the "local file"
   * @param appendTo      the <code>RemoteFile</code> you want to append to.
   * @param progress      a <code>Progress</code> object which is used to 
   *                      update upload status.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void append( InputStream inputStream, long localFileSize, 
                      RemoteFile appendTo, Progress progress )
                                              throws FTPException, 
                                                     IOException,
                                                     IllegalArgumentException {
    append( inputStream, localFileSize, appendTo, progress, null );
  }

  /**
   * Append to a file on the FTP server with progress information and the 
   * ability to abort the data transfer.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param file         the local file you want to upload.
   * @param appendTo     the remote file name you want to append to.
   * @param progress     a <code>Progress</code> object which is used to update 
   *                     upload status.
   * @param abort        an Object that allows for the abortion of the 
   *                     <code>append</code>.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void append( File file, String appendTo, Progress progress,
                      FTPAbortableTransfer abort )
                                              throws FTPException, 
                                                     IOException,
                                                     IllegalArgumentException {
    append( file, new RemoteFile(appendTo), progress, abort );
  }

  /**
   * Append to a file on the FTP server with progress information and the 
   * ability to abort the data transfer.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param file         the local file you want to upload.
   * @param appendTo     the <code>RemoteFile</code> you want to append to.
   * @param progress     a <code>Progress</code> object which is used to update 
   *                     upload status.
   * @param abort        an Object that allows for the abortion of the 
   *                     <code>append</code>.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void append( File file, RemoteFile appendTo, Progress progress,
                      FTPAbortableTransfer abort )
                                              throws FTPException, 
                                                     IOException,
                                                     IllegalArgumentException {
    aboutToTransferData();
    _append( file, appendTo, progress, abort );
  }

  /**
   * Append to a file on the FTP server with progress information and the 
   * ability to abort the data transfer.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param inputStream   the "local file" stream you want to upload.
   * @param localFileSize the size of the "local file"
   * @param appendTo      the remote file name you want to append to.
   * @param progress      a <code>Progress</code> object which is used to 
   *                      update upload status.
   * @param abort         an Object that allows for the abortion of the 
   *                      <code>append</code>.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void append( InputStream inputStream, long localFileSize, 
                      String appendTo, Progress progress, 
                      FTPAbortableTransfer abort )
                                              throws FTPException, 
                                                     IOException,
                                                     IllegalArgumentException {
    append( inputStream, localFileSize, new RemoteFile(appendTo), progress, 
            abort );
  }

  /**
   * Append to a file on the FTP server with progress information and the 
   * ability to abort the data transfer.
   * <p>
   * Based on what <code>getConnectionType</code> returns, either 
   * <code>pasv</code> or <code>port</code> will be sent in this routine.
   *
   * @param inputStream   the "local file" stream you want to upload.
   * @param localFileSize the size of the "local file"
   * @param appendTo      the <code>RemoteFile</code> you want to append to.
   * @param progress      a <code>Progress</code> object which is used to 
   *                      update upload status.
   * @param abort         an Object that allows for the abortion of the 
   *                      <code>append</code>.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there is a socket or file problem.
   * @throws IllegalArgumentException if there are argument related problems.
   *
   * @see #getConnectionType()
   * @see #setConnectionType(short)
   * @see #pasv()
   * @see #port(HostInfo)
   */
  public void append( InputStream inputStream, long localFileSize, 
                      RemoteFile appendTo, Progress progress, 
                      FTPAbortableTransfer abort )
                                              throws FTPException, 
                                                     IOException,
                                                     IllegalArgumentException {
    aboutToTransferData();
    _appendByStream( inputStream, appendTo, progress, abort, localFileSize );
  }

  /**
   * Send a raw command to the FTP server.
   *
   * @param rawCmd                    the command sent to the FTP server.
   *
   * @throws FTPException             if the FTP server returns an error code.
   */
  public void raw( String rawCmd ) throws FTPException {
    _raw( rawCmd );
  }

  /**
   * Send a noop command to the FTP server.
   *
   * @throws FTPException             if the FTP server returns an error code.
   */
  public void noop() throws FTPException {
    _noop();
  }

  /**
   * Abort a data transfer from the FTP server.
   *
   * @param  abort                    an Object that will has information that
   *                                  will allow for the abortion of a transfer.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IllegalArgumentException if there is a problem with the 
   *                                  <code>abort</code> argument.
   */
  public void abort( FTPAbortableTransfer abort )
                    throws FTPException, IllegalArgumentException {
    _abort( abort );
  }

  /**
   * Set server to treat string data in UTF8 format
   *
   * @param  on                       enable UTF8 support.
   *
   * @throws FTPException             if the FTP server returns an error code.
   */
  public void setStringDataAsUTF8( boolean on ) throws FTPException {
    if ( systemSupportsOptsUTF8Cmd ) {
      getFTPCommand().raw( "CLNT Secure FTP" ); 
      _optsUTF8( on );
      sendStringDataUTF8 = on;
    }
    else
      throw new FTPException( "System does not support OPTS UTF8 command." );
  }

  /**
   * Whether or not the server is treating the string data as UTF8
   *
   * @return                           true if server treats string as UTF8
   */
  public boolean stringDataAsUTF8() { return sendStringDataUTF8; }

  /**
   * Send a pre transfer command.
   */
  private void _pret( int pretType, String arg ) throws FTPException {
    try {
      String pretMsg = "";
      switch( pretType ) {
        case PRET.LIST:
          pretMsg = "PRET LIST";
          break;
        case PRET.NLIST:
          pretMsg = "PRET NLST";
          break;
        case PRET.RETRIEVE:
          pretMsg = "PRET RETR " + arg;
          break;
        case PRET.APPEND:
          pretMsg = "PRET APPE " + arg;
          break;
        case PRET.STORE:
          pretMsg = "PRET STOR " + arg;
          break;
      }

      if ( pretMsg.length() > 0 ) {
        OutputStreamWriter recvCmdWriter = getFTPCommand().recvCmdWriter;
        OutputStreamWriter sendCmdWriter = getFTPCommand().sendCmdWriter;

        getFTPCommand().recvCmdWriter = null;
        getFTPCommand().sendCmdWriter = null;

        getFTPCommand().raw( pretMsg );

        getFTPCommand().recvCmdWriter = recvCmdWriter;
        getFTPCommand().sendCmdWriter = sendCmdWriter;

        int code = getFTPCommand().getReplyCode();
        if ( code >= 200 && code < 300 ) {
          // we're ok
        }
        else {
          systemSupportsPretCmd = false;
        }
      }
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }
  }

  /**
   * Send an option command.
   */
  private void _optsUTF8( boolean state ) throws FTPException {
    try {
      String msg = "OPTS UTF8 " + ((state) ? "ON" : "OFF");

      getFTPCommand().raw( msg );
      int code = getFTPCommand().getReplyCode();
      if ( code >= 200 && code < 300 ) {
        // we're ok
      }
      else {
        systemSupportsOptsUTF8Cmd = false;
      }
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }
  }

  /**
   * Setup a pasv data connection.
   *
   * @return a <code>HostInfo<code> object that is used for the data transfer.
   *
   * @throws FTPException             if the FTP server returns an error code.
   */
  protected HostInfo pasv() throws FTPException {
    HostInfo retInfo = null;

    try {
      retInfo = getFTPCommand().pasv();
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }

    connectionType = PASV_CONNECTION_TYPE;
    return retInfo;
  }

  /**
   * Setup an active data connection.
   *
   * @param hostInfo a <code>HostInfo</code> object that describes the 
   *                 host information used for the data transfer.
   *
   * @throws FTPException             if the FTP server returns an error code.
   */
  protected void port( HostInfo hostInfo ) throws FTPException {
    try {
      getFTPCommand().port( hostInfo );
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }

    connectionType = ACTIVE_CONNECTION_TYPE;
  }

  /**
   * Change to a remote directory on the FTP server.
   *
   * @param dir     the name of the remote directory to change to.
   *
   * @throws FTPNotADirectoryException if the <code>dir</code> arg is not 
   *                                   a directory.
   * @throws FTPNoSuchFileException    if the <code>dir</code> arg could not
   *                                   be found on the server.
   * @throws FTPException              if the FTP server returns an error code.
   * @throws IllegalArgumentException  if there is a problem with the 
   *                                   <code>dir</code> argument.
   */
  public void chdir( String dir ) throws FTPNotADirectoryException, 
                                         FTPNoSuchFileException, FTPException,
                                         IllegalArgumentException {
    RemoteFile newDir = new RemoteFile( dir );
    chdir( newDir );
  }

  /**
   * Change to a remote directory on the FTP server.
   *
   * @param dir     the remote directory to change to.
   *
   * @throws FTPNotADirectoryException if the <code>dir</code> arg is not 
   *                                   a directory.
   * @throws FTPNoSuchFileException    if the <code>dir</code> arg could not
   *                                   be found on the server.
   * @throws FTPException              if the FTP server returns an error code.
   * @throws IllegalArgumentException  if there is a problem with the 
   *                                   <code>dir</code> argument.
   */
  public void chdir( RemoteFile dir ) throws FTPNotADirectoryException, 
                                         FTPNoSuchFileException, FTPException,
                                         IllegalArgumentException {
    _chdir( dir );
  }

  /**
   * Get the server's remote help.
   *
   * @param item     a space-delimited <code>String</code> of items to get
   *                 help on.
   *
   * @throws FTPException              if the FTP server returns an error code.
   */
  public String help( String item ) throws FTPException {
    return _help( item );
  }

  /**
   * Get the current directory on the FTP server.
   *
   * @return the path of the current working directory.
   *
   * @throws FTPException              if the FTP server returns an error code.
   */
  public String pwd() throws FTPException {
    return _pwd();
  }

  /**
   * Set the data transfer mode to ascii.
   *
   * @throws FTPException              if the FTP server returns an error code.
   */
  public void ascii() throws FTPException {
    _ascii();
  }

  /**
   * Set the data transfer mode to ebcdic.
   *
   * @throws FTPException              if the FTP server returns an error code.
   */
  public void ebcdic() throws FTPException {
    _ebcdic();
  }

  /**
   * Set the data transfer mode to auto.
   */
  public void auto() {
    _auto();
  }

  /**
   * Set the data transfer mode to binary (or image).
   *
   * @throws FTPException              if the FTP server returns an error code.
   */
  public void binary() throws FTPException {
    _binary();
  }

  /**
   * Set mode z (on-the-fly compression) data transfer.
   *
   * @throws FTPException              if the FTP server returns an error code.
   */
  public void modeZ() throws FTPException {
    _modeZ();
  }

  /**
   * Delete a file from the FTP server.
   *
   * @param  fileName                 the file to delete on the server.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IllegalArgumentException if there is a problem with the 
   *                                  <code>fileName</code> argument.
   */
  public void delete( String fileName ) throws FTPException, 
                                               IllegalArgumentException {
    delete( new RemoteFile(fileName) );
  }

  /**
   * Delete a file from the FTP server.
   *
   * @param  fileName                 the file to delete on the server.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IllegalArgumentException if there is a problem with the 
   *                                  <code>fileName</code> argument.
   */
  public void delete( RemoteFile fileName ) throws FTPException, 
                                               IllegalArgumentException {
    _delete( fileName );
  }

  /**
   * Rename a file on the FTP server.
   *
   * @param  from                     the old name of the file.
   * @param  to                       the new name of the file.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IllegalArgumentException if there is a problem with the 
   *                                  <code>from</code> or <code>to</code>
   *                                  arguments.
   */
  public void rename( String from, String to ) throws FTPException, 
                                                      IllegalArgumentException {
    _rename( from, to );
  }

 /**
  * Get the size of a file on the FTP server.
  *
  * @param file  the name of the file.
  *
  * @return the size or <code>-1</code> if the size could not be
  *         determined.
  *
  * @exception FTPNoSuchFileException if <code>file</code> is not found.
  * @exception FTPException   if the FTP server returns an error code.
  * @exception IllegalArgumentException if <code>file</code> is missing.
  */
  public long size( String file ) throws FTPNoSuchFileException, FTPException, 
                                         IllegalArgumentException {
    return size( new RemoteFile(file) );
  }

 /**
  * Get the size of a file on the FTP server.
  *
  * @param file  the name of the file.
  *
  * @return the size or <code>-1</code> if the size could not be
  *         determined.
  *
  * @exception FTPNoSuchFileException if <code>file</code> is not found.
  * @exception FTPException   if the FTP server returns an error code.
  * @exception IllegalArgumentException if <code>file</code> is missing.
  */
  public long size( RemoteFile file ) throws FTPNoSuchFileException, 
                                             FTPException, 
                                             IllegalArgumentException {
    return _size( file );
  }

 /**
  * Get the modification time of a file on the FTP server.
  *
  * @param file  the name of the file.
  *
  * @return the file time or <code>null</code> if the time could not be
  *         determined.
  *
  * @exception FTPNoSuchFileException if <code>file</code> is not found.
  * @exception FTPException   if the FTP server returns an error code.
  * @exception IllegalArgumentException if <code>file</code> is missing.
  */
  public Date time( String file ) throws FTPNoSuchFileException,
                                         FTPException, 
                                         IllegalArgumentException {
    return time( new RemoteFile(file) );
  }

 /**
  * Get the modification time of a file on the FTP server.
  *
  * @param file  the name of the file.
  *
  * @return the file time or <code>null</code> if the time could not be
  *         determined.
  *
  * @exception FTPNoSuchFileException if <code>file</code> is not found.
  * @exception FTPException   if the FTP server returns an error code.
  * @exception IllegalArgumentException if <code>file</code> is missing.
  */
  public Date time( RemoteFile file ) throws FTPNoSuchFileException,
                                             FTPException,
                                             IllegalArgumentException {
    return _time( file );
  }

  /**
   * Make a new directory on the FTP server.
   *
   * @param  newDir                   the name of the new directory.
   *
   * @throws FTPAccessDeniedException if the directory couldn't be created
   *                                  due to access restrictions.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IllegalArgumentException if there is a problem with the 
   *                                  <code>newDir</code> argument.
   */
  public void mkdir( String newDir ) throws FTPException, 
                                            FTPAccessDeniedException,
                                            IllegalArgumentException {
    _mkdir( newDir );
  }

  /**
   * Remote a directory from the FTP server.
   *
   * @param  dir                      the directory to delete on the server.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IllegalArgumentException if there is a problem with the 
   *                                  <code>dir</code> argument.
   */
  public void rmdir( String dir ) throws FTPException, 
                                         IllegalArgumentException {
    RemoteFile rf = new RemoteFile(dir);
    rf.setPermissions("d---------");
    rmdir( rf );
  }

  /**
   * Remote a directory from the FTP server.
   *
   * @param  dir                      the directory to delete on the server.
   *
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IllegalArgumentException if there is a problem with the 
   *                                  <code>dir</code> argument.
   */
  public void rmdir( RemoteFile dir ) throws FTPException, 
                                         IllegalArgumentException {
    _rmdir( dir );
  }

  /**
   * Change up one directory on the FTP server.
   *
   * @throws FTPException             if the FTP server returns an error code.
   */
  public void cdup() throws FTPException {
    _cdup();
  }

  /**
   * Make a new control socket.
   *
   * @param hostInfo                  a <code>HostInfo</code> object that
   *                                  describes where to make the socket.
   *
   * @return                          a new instance of a socket.
   *
   * @throws IOException              if there is a socket problem. 
   */
  protected Socket makeControlSocket( HostInfo hostInfo ) throws IOException {
    return new Socket( hostInfo.getInetAddress(), hostInfo.getPort() );
  }

  /**
   * Make a new data socket.
   *
   * @param hostInfo                  a <code>HostInfo</code> object that
   *                                  describes where to make the socket.
   *
   * @return                          a new instance of a socket.
   *
   * @throws IOException              if there is a socket problem. 
   */
  protected Socket makeDataSocket( HostInfo hostInfo ) throws IOException {
    return new Socket( hostInfo.getInetAddress(), hostInfo.getPort() );
  }

  /**
   * Make a new data server socket.
   *
   * @param hostInfo                  a <code>HostInfo</code> object that
   *                                  describes where to make the socket.
   *
   * @return                          a new instance of a server socket.
   *
   * @throws IOException              if there is a socket problem. 
   */
  protected ServerSocket makeDataServerSocket( HostInfo hostInfo ) 
                                                            throws IOException {
    return new ServerSocket( hostInfo.getPort(), 4, hostInfo.getInetAddress() );
  }

  /**
   * Set a range of ports to use during active (port) data connections.
   *
   * @param minPort                   the <code>minPort</code> specifies
   *                                  the minimum port used for active transfers
   * @param maxPort                   the <code>maxPort</code> specifies
   *                                  the maximum port used for active transfers
   *
   * @throws Exception                if the min/max ports are invalid
   */
  public void setActivePortRange( int minPort, int maxPort ) 
                                                              throws Exception {
    if ( minPort < 0 || maxPort < 0 || minPort >= maxPort ) {
      throw new Exception( "The port range is invalid." );
    }

    minPortInRange = minPort;
    maxPortInRange = maxPort;

    lastPortFromRange = (int)(Math.random() * (maxPort - minPort + 1) + minPort);
  }

  protected int getPortFromRange() {
    int result = 0;

    if ( lastPortFromRange == 0 ) {
      result = 0;
    }
    else if ( lastPortFromRange + 1 > maxPortInRange ) {
      lastPortFromRange = minPortInRange;
      result = lastPortFromRange;
    }
    else {
      result = lastPortFromRange++;
    }

    return result;
  }

  /**
   * The data transfer mode is either auto, ascii, or binary (image).
   *
   * @return the transfer mode
   *
   * @see #AUTO_TRANSFER_MODE
   * @see #ASCII_TRANSFER_MODE
   * @see #BINARY_TRANSFER_MODE
   * @see #EBCDIC_TRANSFER_MODE
   */
  public short getTransferMode() { return transferMode; }

  /**
   * The data connection type is either passive or active.
   *
   * @return the connection type
   *
   * @see #PASV_CONNECTION_TYPE
   * @see #ACTIVE_CONNECTION_TYPE
   */
  public int getConnectionType() { return connectionType; }

  /**
   * Set the data connection type.
   *
   * @param type  the connection type.
   *
   * @see #PASV_CONNECTION_TYPE
   * @see #ACTIVE_CONNECTION_TYPE
   */
  public void setConnectionType( short type ) { connectionType = type; }

  /**
   * Whether or not currently connected.
   *
   * @return true if connected.
   */
  public boolean isConnected() { return isConnected; }

  /**
   * Whether or not currently logged in.
   *
   * @return true if logged in.
   */
  public boolean isLoggedIn() { return isLoggedIn; }

  /**
   * Whether or not the server supports restarting broken data transfers.
   *
   * @return true if supported by server.
   */
  public boolean isTransferRestartable() {
    return _isTransferRestartable();
  }

  /**
   * Get the hostname of the FTP server.
   *
   * @return the hostname.
   */
  public String getHostName() { return hostName; }

  /**
   * Set the hostname of the FTP server.
   *
   * @param hostName  the hostname of the FTP server.
   */
  public void setHostName( String hostName ) { this.hostName = hostName; }

  /**
   * Get the port of the FTP server.
   *
   * @return the port.
   */
  public int getPort() { return port; }

  /**
   * Set the port of the FTP server.
   *
   * @param port  the port of the FTP server.
   */
  public void setPort( int port ) { this.port = port; }

  /**
   * Get the username.
   *
   * @return the username.
   */
  public String getUser() { return user; }

  /**
   * Set the username.
   *
   * @param user  the username.
   */
  public void setUser( String user ) { this.user = user; }

  /**
   * Get the password.
   *
   * @return the password.
   */
  public String getPassword() { return password; }

  /**
   * Set the password.
   *
   * @param password  the password.
   */
  public void setPassword( String password ) { this.password = password; }

  /**
   * Get the account name.
   *
   * @return the account name.
   */
  public String getAccount() { return account; }

  /**
   * Set the account name.
   *
   * @param account  the account name.
   */
  public void setAccount( String account ) { this.account = account; }

  /**
   * Get the control socket.
   *
   * @return the control socket.
   */
  public Socket getControlSocket() { return controlSocket; }

  /**
   * Set the control socket.
   *
   * @param controlSocket  the control socket.
   */
  protected void setControlSocket( Socket controlSocket ) throws IOException {
    this.controlSocket = controlSocket;

    BufferedReader inputReader =
      new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
    PrintWriter outputWriter = new PrintWriter(controlSocket.getOutputStream());

    // recreate the FTPCommand object with the new streams
    command = makeFTPCommand( inputReader, outputWriter );
  }

  /**
   * Set the stream responsible for handling respones from the FTP server.
   *
   * @param recvCmdStream  the server response stream.
   *                       Pass <code>null</code> to unset this stream. 
   */
  public void setRecvCmdStream( OutputStream recvCmdStream ) {
    this.recvCmdStream = recvCmdStream;
    if ( null != getFTPCommand() ) {
      getFTPCommand().setRecvCmdStream( recvCmdStream );
    }
  }

  /**
   * Set the stream responsible for handling commands sent to the FTP server.
   *
   * @param sendCmdStream  the commands sent stream.
   *                       Pass <code>null</code> to unset this stream. 
   */
  public void setSendCmdStream( OutputStream sendCmdStream ) {
    this.sendCmdStream = sendCmdStream;
    if ( null != getFTPCommand() ) {
      getFTPCommand().setSendCmdStream( sendCmdStream );
    }
  }

  /**
   * Get the server listing style of the FTP server.
   *
   * @return the list style for the server type
   *
   * @see FTPServerInfo#LIST_STYLE_UNKNOWN
   * @see FTPServerInfo#LIST_STYLE_UNIX
   * @see FTPServerInfo#LIST_STYLE_WINDOWS
   * @see FTPServerInfo#LIST_STYLE_NETWARE
   */
  public short getListStyle() {
    return listStyle;
  }

  /**
   * Set the server type to help with the listing style of the FTP server.
   *
   * @param listStyle      the list style for the server type 
   *                       (UNIX, NETWARE, Windows, Unknown)
   *
   * @see FTPServerInfo#LIST_STYLE_UNKNOWN
   * @see FTPServerInfo#LIST_STYLE_UNIX
   * @see FTPServerInfo#LIST_STYLE_WINDOWS
   * @see FTPServerInfo#LIST_STYLE_NETWARE
   */
  public void setListStyle( short listStyle ) {
    this.listStyle = listStyle;
  }

  /**
   * Set the Socks IV server proxy.
   *
   * @param host           the hostname of the proxy. 
   *                       Pass <code>null</code> to unset the proxy.
   * @param port           the port of the proxy.
   */
  public void setSocksIVProxy( String host, int port ) {
    _setSocksProxy( host, port );
  }

  /**
   * Set the Socks V server proxy.
   *
   * @param host           the hostname of the proxy. 
   *                       Pass <code>null</code> to unset the proxy.
   * @param port           the port of the proxy.
   * @param username       the socks username (can be null for no auth)
   * @param password       the socks password (can be null for no auth)
   */
  public void setSocksVProxy( String host, int port, String username,
                              String password ) {
    Properties systemProps = System.getProperties();
    if ( null != username ) {
      systemProps.put("java.net.socks.username", username);
    }
    else {
      systemProps.remove("java.net.socks.username");
    }

    if ( null != password ) {
      systemProps.put("java.net.socks.password", password);
    }
    else {
      systemProps.remove("java.net.socks.password");
    }

    _setSocksProxy( host, port );
  }

  /**
   * Get the <code>FTPCommand</code> object.
   *
   * @return an <code>FTPCommand</code> object.
   */
  public FTPCommand getFTPCommand() { return command; }

  /**
   * Set the <code>FTPCommand</code> object.
   *
   * @param inputReader   the <code>BufferedReader</code> comes from
   *                      the input stream of the control socket.
   * @param outputWriter  the <code>PrintWriter</code> comes from
   *                      the output stream of the control socket.
   *
   * @return a new instance of an <code>FTPCommand</code> object.
   */
  protected FTPCommand makeFTPCommand( BufferedReader inputReader, 
                                       PrintWriter outputWriter ) { 
    return new FTPCommand( inputReader, outputWriter,
                           sendCmdStream, recvCmdStream );
  }

  /** The version of the bean. */
  public static String getVersion() {
    return VERSION;
  }

  /** The datestamp of the bean. */
  public static String getDateStamp() {
    return getDateStampFromFile();
  }

  /*
   *
   * The methods below are here for obfuscation purposes.
   *
   */

  private void _connect() throws FTPConnectException, FTPException, 
                               IOException, 
                               UnknownHostException, IllegalArgumentException {
    if ( null == getHostName() )
      throw new IllegalArgumentException( "Missing host name" );

    HostInfo controlInfo = new HostInfo( getHostName(), getPort() );

    try {
      if ( debug )
        System.out.print( "Making control socket... " );

      controlSocket = makeControlSocket( controlInfo );

      if ( debug )
        System.out.println( "done" );

      // set timeout of 60 seconds
      controlSocket.setSoTimeout( 60000 );
    }
    catch ( IOException ioe ) {
      throw new FTPConnectException("Connection failed.");
    }

    BufferedReader inputReader = 
      new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
    PrintWriter outputWriter = new PrintWriter(controlSocket.getOutputStream());

    // get login messages
    String banner = "";
    StringBuffer fullBanner = new StringBuffer();

    boolean continueReadingBanner = false;

    do {
      try {
        if ( debug )
          System.out.println( "Reading banner" );

        banner = inputReader.readLine();
        if ( debug )
          System.out.println( banner );
      }
      catch ( SocketTimeoutException ste ) {
    	if (inputReader != null)
    	  inputReader.close();
    	if (outputWriter != null)
    	  outputWriter.close();

    	controlSocket.close(); 
        controlSocket = null;

        throw ste;
      }
      catch ( IOException ioe ) {
    	if (inputReader != null)
    	  inputReader.close();
    	if (outputWriter != null)
    	  outputWriter.close();
    	if (controlSocket != null)
    	  controlSocket.close(); 
        throw ioe;
      }

      controlSocket.setSoTimeout( 0 );
      continueReadingBanner = false;

      if ( null != banner ) {
        try {
          String sErrorCode = "unknown";
          int errorCode = 0;

          if ( banner.length() > 3 ) {
            sErrorCode = banner.substring(0, 3);
          }

          try {
            errorCode = Integer.parseInt( sErrorCode );
          }
          catch ( NumberFormatException nfe ) {
            // we should always have an error code... but if the banner is
            // incorrectly formatted just assume it's ok
            errorCode = 220;
            continueReadingBanner = true;
          }

          if ( banner.length() > 3 && errorCode != 220 ) {
            String msg = banner.substring(3, banner.length()-1).trim();
            throw new FTPConnectException(msg);
          }

          if ( null != recvCmdStream ) {
            Util.outputStreamPrintln(recvCmdStream, banner, true);
          }
        }
        catch ( IOException ioe ) {}

        fullBanner.append( banner );
        fullBanner.append( "\r\n" );

        if ( banner.length() >= 4 && banner.charAt(3) == '-' ) {
          continueReadingBanner = true;
        }
      }
    }
    while ( null != banner && continueReadingBanner );

    if ( null == banner ) {
      throw new FTPConnectException("Connection failed.");
    }

    command = makeFTPCommand( inputReader, outputWriter );

    listStyle = FTPServerInfo.lookupListStyleByBanner( fullBanner.toString() );

    isConnected = true;
  }

  private void _login( String user, String pass, String acct ) 
              throws IOException, FTPBadLoginException, FTPConnectException,
                     FTPException, IllegalArgumentException {
    if ( !isConnected ) {
      throw new FTPConnectException("Not connected.");
    }

    try {
      sendUserName( user );
    }
    catch ( FTPNeedPasswordException npe ) {
      try {
        sendPassword( pass );
      }
      catch ( FTPNeedAccountException nae ) {
        sendAccount( acct );
      } 
    }
    catch ( FTPNeedAccountException nae ) {
      sendAccount( acct );
    }

    isLoggedIn = true;
  }  

  private void _logout() throws IOException, FTPException {
    try {
      getFTPCommand().quit();
    }
    catch (FTPException fe) {
      // if we fail here, let it go... there were bigger issues
    }

    isConnected = false;
    isLoggedIn = false;

    getControlSocket().close();
  }

  private void _sendUserName( String user ) throws FTPNeedPasswordException,
                                                   FTPNeedAccountException,
                                                   FTPBadLoginException,
                                                   FTPException,
                                                   IllegalArgumentException {
    setUser( user );

    try {
      getFTPCommand().user( getUser() );
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }

    isLoggedIn = false;
  }

  private void _sendPassword( String pass ) throws FTPNeedAccountException,
                                                   FTPBadLoginException,
                                                   FTPException,
                                                   IllegalArgumentException {
    setPassword( pass );

    try {
      getFTPCommand().pass( getPassword() );
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }

    isLoggedIn = true;
  }

  private void _sendAccount( String acct ) throws FTPBadLoginException,
                                                  FTPException, 
                                                  IllegalArgumentException {
    setAccount( acct );

    try {
      getFTPCommand().acct( getAccount() );
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }

    isLoggedIn = true;
  }

  private RemoteFileList _list( String itemsToList, FTPAbortableTransfer abort,
                                boolean showHidden )
                                              throws FTPException, IOException {
    HostInfo hostInfo = null;
    FTPRead data = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintWriter pw = new PrintWriter( baos );

    if ( !listStyleSet && FTPServerInfo.LIST_STYLE_UNKNOWN == listStyle ) {
      try {
        String systemType = getFTPCommand().syst();
        if ( systemType != null && systemType.length() > 0 ) {
          listStyle = FTPServerInfo.lookupListStyleBySyst( systemType );
        }
      }
      catch ( FTPException fe ) {}
      finally { listStyleSet = true; }
    }

    short lastTransferMode = getTransferMode();

    ascii();

    if ( PASV_CONNECTION_TYPE == connectionType ) {
      if ( systemSupportsPretCmd ) {
        _pret( PRET.LIST, null );
      }

      hostInfo = pasv();
      Socket dataSocket = makeDataSocket( hostInfo );
      data = new FTPRead( dataSocket, pw );
    }
    else {
      hostInfo = new HostInfo( getControlSocket().getLocalAddress(), getPortFromRange() );
      ServerSocket dataServerSocket = makeDataServerSocket( hostInfo );
/*
      hostInfo = new HostInfo(dataServerSocket.getInetAddress().getLocalHost(), 
                              dataServerSocket.getLocalPort());
*/
      hostInfo.setPort( dataServerSocket.getLocalPort() );
      port( hostInfo );
      data = new FTPRead( dataServerSocket, pw );
    }

    data.setZLibCompressed( modeZEnabled );

    RemoteFileList returnList = new RemoteFileList();

    if ( abort != null ) {
      data.setControlSocket( getControlSocket() );
      abort.setFTPData( data );
    }

    if ( FTPServerInfo.LIST_STYLE_UNKNOWN != listStyle ) {
      try {
        getFTPCommand().list( itemsToList, data, showHidden );
      }
      catch ( FTPConnectionLostException cle ) {
        isConnected = false;
        isLoggedIn = false;
        throw cle;
      }
      finally {
        switch ( lastTransferMode ) {
          case ASCII_TRANSFER_MODE:
            break;

          case EBCDIC_TRANSFER_MODE:
            transferMode = EBCDIC_TRANSFER_MODE;
            break;

          case BINARY_TRANSFER_MODE:
            binary();
            break;

          case AUTO_TRANSFER_MODE:
            auto();
            break;
        }
      }
    }
    else {
      try {
        getFTPCommand().nlst( itemsToList, data );
      }
      catch ( FTPConnectionLostException cle ) {
        isConnected = false;
        isLoggedIn = false;
        throw cle;
      }
      finally {
        switch ( lastTransferMode ) {
          case ASCII_TRANSFER_MODE:
          case EBCDIC_TRANSFER_MODE:
            break;

          case BINARY_TRANSFER_MODE:
            binary();
            break;

          case AUTO_TRANSFER_MODE:
            auto();
            break;
        }
      }
    }

    REProgram pattern = null;
    try {
/*
       1            2 3        4           5         6    7  8    9
       -r-xr-xr-x   1 owner    group       500000000 Apr  9  1999 JWW.TST
       d---------   1 owner    group               0 Aug 12  2:23 WUTemp
*/
      pattern = 
         compiler.compile( "^(\\S+)\\s+(\\d+)*\\s?(\\S+)\\s+(\\S+)?\\s.*\\s+" +
                     "(\\S+)\\s+(\\S+)\\s+(\\d\\d?)\\s+(\\d?\\d:?\\d\\d) (.+)$" );
    }
    catch ( RESyntaxException rese ) {}
    catch ( java.lang.Error je ) {}
    unixMatcher.setProgram(pattern);

    try {
/*
	1     2   3   4    5       6
	11108 Feb 24 2009 16:42:59 AESTest.c
*/
      pattern = 
         compiler.compile( "^\\s+(\\S+)\\s+(\\S+)\\s+(\\d\\d?)\\s+(\\d\\d\\d\\d)\\s+(\\d?\\d:\\d\\d:\\d\\d)\\s+(.+)$" );
    }
    catch ( RESyntaxException rese ) {}
    catch ( java.lang.Error je ) {}
    enginMatcher.setProgram(pattern);

    try {
      pattern = compiler.compile( "^(\\S+\\s+\\S+)\\s+(\\S+)\\s+(.+)$" );
    }
    catch ( RESyntaxException rese ) {}
    catch ( java.lang.Error je ) {}
    winMatcher.setProgram(pattern);

    try {
/*
      1            2                       3      4   5  6     7
      - [RWCEAFMS] user                    232960 Jun 16 16:00 File.txt
      d [RWCEAFMS] user                       512 Jun 21 00:53 Dir
*/ 
      pattern =
        compiler.compile( "^(\\S)\\s+\\[RWCEAFMS\\]\\s+(\\S+)\\s+(\\S+)\\s+" +
                          "(\\S+)\\s+(\\d\\d?)\\s+(\\d?\\d:?\\d\\d) (.+)$" );
    }
    catch ( RESyntaxException rese ) {}
    catch ( java.lang.Error je ) {}
    netwareMatcher.setProgram(pattern);

    String listEncoding = new String(baos.toString().getBytes("UTF8"), "UTF8");
    
    String listEncodingOverride = 
      GTOverride.getString("glub.override.list_encoding");

    if (listEncodingOverride == null) {
      listEncodingOverride = 
        GTOverride.getString("glub.list_encoding.override");
    }

    if (listEncodingOverride != null) {
      listEncoding = 
        new String(baos.toString().getBytes(listEncodingOverride),
                                            listEncodingOverride);
    }

    StringReader stringReader = new StringReader(listEncoding);
    BufferedReader listReader = new BufferedReader(stringReader);

    String subdir = "";
    String line = null;

    boolean skipTotalLine = true;

    boolean hadErrors = false;

    for ( int i = 0; (line = listReader.readLine()) != null; i++ ) {
      String mode   = null;
      int link      = 0;
      String user   = null;
      String group  = null;
      long size     = -1;

      Calendar date = null;
      String month  = null;
      String day    = null;
      String year   = null;

      String file   = null;

      if ( line.indexOf("file or directory") >= 0 ) {
        //return new RemoteFileList();
        hadErrors = true;
      }

      if ( line.indexOf(": Permission denied") >= 0 ) {
        throw new FTPPermissionDeniedException();
      }

      if ( skipTotalLine && line.trim().startsWith("total") ) {
        skipTotalLine = false;
        continue;
      }
      else if ( line.trim().length() == 0 ) {
        continue;
      }

      if ( FTPServerInfo.LIST_STYLE_UNIX == listStyle ) {
        if ( unixMatcher.match(line) ) {
          mode  = unixMatcher.getParen(1);
          if ( unixMatcher.getParen(2) != null ) {
            link  = Integer.parseInt(unixMatcher.getParen(2));
          }
          user  = unixMatcher.getParen(3);
          group = unixMatcher.getParen(4);
          try {
            String strSize = unixMatcher.getParen(5);
	    // strip out any commas in the number (stupid /devices dir)
	    strSize = Util.searchAndReplace( strSize, ",", "", true );
            size  = Long.parseLong( strSize );
          }
          catch ( NumberFormatException nfe ) {
            size = -1;
          }
          month = unixMatcher.getParen(6);
          day   = unixMatcher.getParen(7);
          year  = unixMatcher.getParen(8);
          file  = unixMatcher.getParen(9);
          date  = Util.getDate( year, month, day );  
        }
        else if ( line.trim().endsWith(":") ) {
          line = line.trim();
          skipTotalLine = true;
          subdir = line.trim().substring(0, line.length() - 1) + "/";
          mode = "d?????????";
          size = -1;
          file = "";
        }
        else if ( !hadErrors && winMatcher.match(line) ) {
          // stupid windows machine not really displaying in unix list format
          listStyle = FTPServerInfo.LIST_STYLE_WINDOWS;
        }
        else if ( !hadErrors && enginMatcher.match(line) ) {
          // not standard unix server listing
          listStyle = FTPServerInfo.LIST_STYLE_ENGIN;
        }
        else if ( !hadErrors ) {
          // machine not really displaying in unix list format
          listStyle = FTPServerInfo.LIST_STYLE_UNKNOWN;
          return _list( itemsToList, abort, showHidden );
        }
      }

      if ( FTPServerInfo.LIST_STYLE_ENGIN == listStyle ) {
        if ( enginMatcher.match(line) ) {
          mode = "-?????????";
          try {
            size = Long.parseLong( enginMatcher.getParen(1) );
          }
          catch ( NumberFormatException nfe ) {
            size = -1;
          }
          month = enginMatcher.getParen(2);
          day   = enginMatcher.getParen(3);
          year  = enginMatcher.getParen(4);
          String timestamp  = enginMatcher.getParen(5);
          file  = enginMatcher.getParen(6);
          date  = Util.getDate( year, month, day );  
        }
      }

      if ( FTPServerInfo.LIST_STYLE_NETWARE == listStyle ) {
        if ( netwareMatcher.match(line) ) {
          mode = netwareMatcher.getParen(1);
          mode += "?????????";
          user = netwareMatcher.getParen(2);
          try {
            String strSize = netwareMatcher.getParen(3);
	    // strip out any commas in the number (stupid /devices dir)
	    strSize = Util.searchAndReplace( strSize, ",", "", true );
            size = Long.parseLong( strSize );
          }
          catch ( NumberFormatException nfe ) {
            size = -1;
          }
          month = netwareMatcher.getParen(4);
          day   = netwareMatcher.getParen(5);
          year  = netwareMatcher.getParen(6);
          file  = netwareMatcher.getParen(7);
          date  = Util.getDate( year, month, day );  
        }
      }

      if ( FTPServerInfo.LIST_STYLE_WINDOWS == listStyle ) {
        if ( winMatcher.match(line) ) {
          date = Util.getWindowsDate( winMatcher.getParen(1) );
          String sSize = winMatcher.getParen(2);
          if ( sSize.trim().equals("<DIR>") ) {
            mode = "d?????????";
            size = 0;
          }
          else {
            mode = "-?????????";
            try {
              size = Long.parseLong(winMatcher.getParen(2));
            }
            catch ( NumberFormatException nfe ) {
              size = -1;
            }
          }
          file = winMatcher.getParen(3);
        }
      }

      if ( !hadErrors && FTPServerInfo.LIST_STYLE_UNIX != listStyle &&
           FTPServerInfo.LIST_STYLE_WINDOWS != listStyle &&
           FTPServerInfo.LIST_STYLE_ENGIN != listStyle &&
           FTPServerInfo.LIST_STYLE_NETWARE != listStyle ) {
        file = line;
        size = -1;
      }

      if ( subdir.length() > 0 && file != null && (file.equals(".") || file.equals("..")) ) {
        continue;
      }

      if ( file != null ) {
        RemoteFile rf = new RemoteFile(mode, link, user, group, 
                                       size, date, subdir + file, line);
        returnList.add( rf );
      }
    }

    if ( hadErrors ) {
      returnList = new RemoteFileList();
    }
    else if ( returnList.size() == 0 ) {
      returnList.add( new RemoteFile(".") );
    }

    return returnList;
  }

  private void _retrieve( RemoteFile remoteFile, File outputFile, 
                          boolean restartXfer,
                          Progress progress, FTPAbortableTransfer abort )
                          throws FTPRestartNotSupportedException,
                                 FTPException, IOException {
    if ( outputFile == null ) {
      String newOutFile = remoteFile.getFileName();

      StringTokenizer tok = new StringTokenizer( newOutFile, "/" );
      while ( tok.hasMoreTokens() ) {
        newOutFile = tok.nextToken();
      } 

      outputFile = new File( newOutFile );
    }

    FileOutputStream fos = null;

    RandomAccessFile raf = null;

    if ( !systemSupportsRestCmd ) {
      restartXfer = false;
    }

    long localFileSize = 0;
    if ( restartXfer ) {
      raf = new RandomAccessFile( outputFile, "rw" );
      localFileSize = raf.length();

      if ( remoteFile.getFileSize() == localFileSize ) {
        localFileSize = 0;
      }

      raf.setLength( localFileSize );

      /*
      // give a little room for error
      if ( localFileSize > 3 ) {
        localFileSize -= 3;
      }
      */

      raf.seek( localFileSize );

      fos = new FileOutputStream( raf.getFD() ); 
    }
    else {
      fos = new FileOutputStream( outputFile );
    }

    try {
      _retrieveByStream( remoteFile, fos, restartXfer, progress, abort, 
                         localFileSize );
    }
    finally {
      if ( null != raf ) {
        try {
          raf.close();
        }
        catch ( IOException ioe ) {}
      }
    }
  }

  private void _retrieveByStream( RemoteFile remoteFile, OutputStream os, 
                                  boolean restartXfer, Progress progress, 
                                  FTPAbortableTransfer abort, 
                                  long localFileSize )
                                 throws FTPRestartNotSupportedException,
                                        FTPException, IOException {
    if ( os == null ) {
      throw new IllegalArgumentException("Missing file output stream.");
    }

    String remoteFileName = remoteFile.getFileName();
    long remoteFileSize = remoteFile.getFileSize();

    if ( !systemSupportsRestCmd ) {
      restartXfer = false;
    }

    try {
      if ( restartXfer && systemSupportsFileSizeCmd && remoteFileSize == -1 ) {
        remoteFileSize = getFTPCommand().size( remoteFileName ); 
      }
    }
    catch ( FTPNoSuchFileException nsfe ) {}
    catch ( FTPException fe ) {
      systemSupportsFileSizeCmd = false;
    }

    FTPRead data = null;
    HostInfo hostInfo = null;

    short xferMode = transferMode;
   
    String encodingOverride = 
      GTOverride.getString("glub.xfer_encoding.override");

    if ( AUTO_TRANSFER_MODE == transferMode ) {
      if ( FileTypeDecider.isAscii(remoteFileName) ) {
        xferMode = ASCII_TRANSFER_MODE;
        ascii();
      }
      else {
        xferMode = BINARY_TRANSFER_MODE;
        binary();
      }
      transferMode = AUTO_TRANSFER_MODE;
    }

    if ( PASV_CONNECTION_TYPE == connectionType ) {
      if ( systemSupportsPretCmd ) {
        _pret( PRET.RETRIEVE, remoteFileName );
      }

      hostInfo = pasv();
      Socket dataSocket = makeDataSocket( hostInfo );

      if ( ASCII_TRANSFER_MODE == xferMode ) {
        data = new FTPRead( dataSocket, new PrintWriter(os), 
                            progress, localFileSize, remoteFileSize, encodingOverride );
      }
      else if ( EBCDIC_TRANSFER_MODE == xferMode ) {
        data = new FTPRead( dataSocket, new PrintWriter(os), 
                            progress, localFileSize, remoteFileSize, "Cp1047" );
      }
      else {
        data = new FTPRead( dataSocket, os, progress, localFileSize, 
                            remoteFileSize );
      }
    }
    else {
      hostInfo = new HostInfo( getControlSocket().getLocalAddress(), getPortFromRange() );
      ServerSocket dataServerSocket = makeDataServerSocket( hostInfo );
/*
      hostInfo = new HostInfo(dataServerSocket.getInetAddress().getLocalHost(), 
                              dataServerSocket.getLocalPort());
*/
      hostInfo.setPort( dataServerSocket.getLocalPort() );
      port( hostInfo );

      if ( ASCII_TRANSFER_MODE == xferMode ) {
        data = new FTPRead( dataServerSocket, new PrintWriter(os), 
                            progress, localFileSize, remoteFileSize, encodingOverride );
      }
      else if ( EBCDIC_TRANSFER_MODE == xferMode ) {
        data = new FTPRead( dataServerSocket, new PrintWriter(os), 
                            progress, localFileSize, remoteFileSize, "Cp1047" );
      }
      else {
        data = new FTPRead( dataServerSocket, os, progress, localFileSize, 
                            remoteFileSize );
      }
    }

    data.setZLibCompressed( modeZEnabled );

    if ( abort != null ) {
      data.setControlSocket( getControlSocket() );
      abort.setFTPData( data );
    }

    if ( restartXfer ) {
      try {
        getFTPCommand().rest( localFileSize );
      }
      catch ( FTPConnectionLostException cle ) {
        try {
          os.close();
        }
        catch ( IOException ioe ) {}
        isConnected = false;
        isLoggedIn = false;
        throw cle;
      }
      catch ( FTPException fe ) {
        try {
          os.close();
        }
        catch ( IOException ioe ) {}
	systemSupportsRestCmd = false;
        throw new FTPRestartNotSupportedException( fe.getMessage() );
      }
    }

    if ( null != remoteFile.getMetaData("pwd") ) {
      remoteFileName = remoteFile.getMetaData("pwd") + remoteFileName;
    }

    try {
      getFTPCommand().retrieve( remoteFileName, data );
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }
    finally {
      try {
        os.close();
      }
      catch ( IOException ioe ) {}
    }
  }

  private void _store( File file, String name, boolean restartXfer, 
                       Progress progress, FTPAbortableTransfer abort ) 
                       throws FTPRestartNotSupportedException, FTPException,
                              IOException, IllegalArgumentException {
    if ( file == null ) {
      throw new IllegalArgumentException("Missing local file.");
    }

    FileInputStream fis = null;

    long remoteFileSize = 0;

    String remoteFileName = name;
    if ( null == remoteFileName ) {
      remoteFileName = file.getName();
    }

    RandomAccessFile raf = null;

    if ( !systemSupportsRestCmd ) {
      restartXfer = false;
    }

    if ( restartXfer ) {
      raf = new RandomAccessFile( file, "r" );

      try {
        if ( systemSupportsFileSizeCmd ) {
          remoteFileSize = getFTPCommand().size( remoteFileName );
        }
      }
      catch ( FTPNoSuchFileException fnfe ) {
        // if the file is not found, that's ok... there's just nothing
        // to restart
        remoteFileSize = 0;
      }
      catch ( FTPConnectionLostException cle ) {
        isConnected = false;
        isLoggedIn = false;
	raf.close();
        throw cle;
      }
      catch ( FTPException fe ) {
        systemSupportsFileSizeCmd = false;
      }

      if ( remoteFileSize >= file.length() ) {
        remoteFileSize = 0;
      }
      /*
      else if ( remoteFileSize > 3 ) {
        // give a little room for error
        remoteFileSize -= 3;
      }
      */

      raf.seek( remoteFileSize );

      fis = new FileInputStream( raf.getFD() ); 
    }
    else {
      fis = new FileInputStream( file );
    }

    try {
      _storeByStream( fis, remoteFileName, restartXfer, progress, abort, 
                      remoteFileSize, file.length() );
    }
    finally {
      try {
        if ( null != raf ) {
          raf.close();
	}
      }
      catch ( IOException ioe ) {}
    }
  }

  private void _storeByStream( InputStream is, String remoteFileName, 
                               boolean restartXfer, Progress progress, 
                               FTPAbortableTransfer abort, long remoteFileSize, 
                               long localFileSize ) 
                               throws FTPRestartNotSupportedException, 
                                      FTPException, IOException, 
                                      IllegalArgumentException {
    HostInfo hostInfo = null;
    FTPWrite data = null;

    String encodingOverride = 
      GTOverride.getString("glub.xfer_encoding.override");

    if ( is == null ) {
      throw new IllegalArgumentException("Missing file input stream.");
    }

    if ( !systemSupportsRestCmd ) {
      restartXfer = false;
    }

    if ( !restartXfer ) {
      remoteFileSize = 0;
    }

    if ( systemSupportsFileSizeCmd && null != progress && 
         remoteFileSize == -1 ) {
      try {
        remoteFileSize = getFTPCommand().size( remoteFileName );
      }
      catch ( FTPNoSuchFileException fnfe ) {
        // if the file is not found, that's ok
        remoteFileSize = 0;
      }
      catch ( FTPConnectionLostException cle ) {
        isConnected = false;
        isLoggedIn = false;
        throw cle;
      }
      catch ( FTPException fe ) {
        systemSupportsFileSizeCmd = false;
      }
    }

    short xferMode = transferMode;

    if ( AUTO_TRANSFER_MODE == transferMode ) {
      if ( FileTypeDecider.isAscii(remoteFileName) ) {
        xferMode = ASCII_TRANSFER_MODE;
        ascii();
      }
      else {
        xferMode = BINARY_TRANSFER_MODE;
        binary();
      }
      transferMode = AUTO_TRANSFER_MODE;
    }

    if ( PASV_CONNECTION_TYPE == connectionType ) {
      if ( systemSupportsPretCmd ) {
        _pret( PRET.STORE, remoteFileName );
      }

      hostInfo = pasv();
      Socket dataSocket = makeDataSocket( hostInfo );

      if ( ASCII_TRANSFER_MODE == xferMode ) {
        data = new FTPWrite( dataSocket, 
                             new BufferedReader(new InputStreamReader(is)),
                             progress, remoteFileSize, localFileSize, encodingOverride );
      }
      else if ( EBCDIC_TRANSFER_MODE == xferMode ) {
        data = new FTPWrite( dataSocket, 
                             new BufferedReader(new InputStreamReader(is)),
                             progress, remoteFileSize, localFileSize, "Cp1047" );
      }
      else {
        data = new FTPWrite( dataSocket, is, progress, 
                             remoteFileSize, localFileSize );
      }
    }
    else {
      hostInfo = new HostInfo( getControlSocket().getLocalAddress(), getPortFromRange() );
      ServerSocket dataServerSocket = makeDataServerSocket( hostInfo );
/*
      hostInfo = new HostInfo(dataServerSocket.getInetAddress().getLocalHost(), 
                              dataServerSocket.getLocalPort());
*/
      hostInfo.setPort( dataServerSocket.getLocalPort() );
      port( hostInfo );

      if ( ASCII_TRANSFER_MODE == xferMode ) {
        data = new FTPWrite( dataServerSocket, 
                             new BufferedReader(new InputStreamReader(is)), 
                             progress, remoteFileSize, localFileSize, encodingOverride );
      }
      else if ( EBCDIC_TRANSFER_MODE == xferMode ) {
        data = new FTPWrite( dataServerSocket, 
                             new BufferedReader(new InputStreamReader(is)), 
                             progress, remoteFileSize, localFileSize, "Cp1047" );
      }
      else {
        data = new FTPWrite( dataServerSocket, is, progress, 
                             remoteFileSize, localFileSize );
      }
    }

    data.setZLibCompressed( modeZEnabled );

    if ( abort != null ) {
      data.setControlSocket( getControlSocket() );
      abort.setFTPData( data );
    }

    if ( remoteFileSize <= 0 ) {
      restartXfer = false;
    }

    if ( restartXfer ) {
      try {
        getFTPCommand().rest( remoteFileSize );
      }
      catch ( FTPConnectionLostException cle ) {
        try {
          is.close();
        }
        catch ( IOException ioe ) {}
        isConnected = false;
        isLoggedIn = false;
        throw cle;
      }
      catch ( FTPException fe ) {
        try {
          is.close();
        }
        catch ( IOException ioe ) {}
	systemSupportsRestCmd = false;
        throw new FTPRestartNotSupportedException( fe.getMessage() );
      }
    }

    try {
      if ( restartXfer ) {
        getFTPCommand().append( remoteFileName, data );
      }
      else {
        getFTPCommand().store( remoteFileName, data );
      }
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }
    finally {
      try {
        is.close();
      }
      catch ( IOException ioe ) {}
    }
  }

  private void _append( File file, RemoteFile appendTo, Progress progress,
                        FTPAbortableTransfer abort )
                                              throws FTPException, 
                                                     IOException,
                                                     IllegalArgumentException {
    if ( file == null ) {
      throw new IllegalArgumentException("Missing local file.");
    }
    else if ( appendTo == null || 
              appendTo.getFileName().length() == 0 ) {
      throw new IllegalArgumentException("Missing file to append to.");
    }

    FileInputStream fis = new FileInputStream( file );

    _appendByStream( fis, appendTo, progress, abort, file.length() );
  }

  private void _appendByStream( InputStream is, RemoteFile appendTo, 
                                Progress progress, FTPAbortableTransfer abort,
                                long localFileLength )
                                              throws FTPException, 
                                                     IOException,
                                                     IllegalArgumentException {
    if ( is == null ) {
      throw new IllegalArgumentException("Missing input stream.");
    }

    HostInfo hostInfo = null;
    FTPWrite data = null;

    String encodingOverride = 
      GTOverride.getString("glub.xfer_encoding.override");

    short xferMode = transferMode;

    if ( AUTO_TRANSFER_MODE == transferMode ) {
      if ( FileTypeDecider.isAscii(appendTo.getFileName()) ) {
        xferMode = ASCII_TRANSFER_MODE;
        ascii();
      }
      else {
        xferMode = BINARY_TRANSFER_MODE;
        binary();
      }
      transferMode = AUTO_TRANSFER_MODE;
    }

    if ( PASV_CONNECTION_TYPE == connectionType ) {
      if ( systemSupportsPretCmd ) {
        _pret( PRET.APPEND, appendTo.getFileName() );
      }

      hostInfo = pasv();
      Socket dataSocket = makeDataSocket( hostInfo );

      if ( ASCII_TRANSFER_MODE == xferMode ) {
        data = new FTPWrite( dataSocket, 
                             new BufferedReader(new InputStreamReader(is)),
                             progress, 0, localFileLength, encodingOverride );
      }
      else if ( EBCDIC_TRANSFER_MODE == xferMode ) {
        data = new FTPWrite( dataSocket, 
                             new BufferedReader(new InputStreamReader(is)),
                             progress, 0, localFileLength, "Cp1047" );
      }
      else {
        data = new FTPWrite( dataSocket, is, progress, 0, localFileLength );
      }
    }
    else {
      hostInfo = new HostInfo( getControlSocket().getLocalAddress(), getPortFromRange() );
      ServerSocket dataServerSocket = makeDataServerSocket( hostInfo );
/*
      hostInfo = new HostInfo(dataServerSocket.getInetAddress().getLocalHost(), 
                              dataServerSocket.getLocalPort());
*/
      hostInfo.setPort( dataServerSocket.getLocalPort() );
      port( hostInfo );

      if ( ASCII_TRANSFER_MODE == xferMode ) {
        data = new FTPWrite( dataServerSocket, 
                             new BufferedReader(new InputStreamReader(is)), 
                             progress, 0, localFileLength, encodingOverride );
      }
      else if ( EBCDIC_TRANSFER_MODE == xferMode ) {
        data = new FTPWrite( dataServerSocket, 
                             new BufferedReader(new InputStreamReader(is)), 
                             progress, 0, localFileLength, "Cp1047" );
      }
      else {
        data = new FTPWrite( dataServerSocket, is, progress, 0, 
                             localFileLength );
      }
    }

    data.setZLibCompressed( modeZEnabled );

    if ( abort != null ) {
      data.setControlSocket( getControlSocket() );
      abort.setFTPData( data );
    }

    try {
      getFTPCommand().append( appendTo.getFileName(), data );
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }
  }

  private void _raw( String rawCmd ) throws FTPException {
    try {
      getFTPCommand().raw( rawCmd );
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }
  }

  private void _noop() throws FTPException {
    try {
      getFTPCommand().noop();
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }
  }

  private void _abort( FTPAbortableTransfer abort )
                       throws FTPException, IllegalArgumentException {
    if ( abort == null ) {
      throw new IllegalArgumentException( "Missing item to abort." );
    }

    FTPData data = abort.getFTPData();
    if ( data == null ) {
      throw new IllegalArgumentException( "Missing FTPData to abort." );
    }

    try {
      getFTPCommand().abort( abort.getFTPData() );
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }
  }

  private void _chdir( RemoteFile dir ) throws FTPNotADirectoryException, 
                                               FTPNoSuchFileException,
                                               FTPPermissionDeniedException, 
                                               FTPException,
                                               IllegalArgumentException {
    String fullDir = dir.getFileName();
    if ( null != dir.getMetaData("pwd") ) {
      fullDir = dir.getMetaData("pwd") + fullDir;
    }

    try {
      getFTPCommand().chdir( fullDir );
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }
  }

  private String _help( String item ) throws FTPException {
    try {
      return getFTPCommand().help( item );
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }
  }

  private String _pwd() throws FTPException {
    try {
      return getFTPCommand().pwd(); 
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }
  }

  private void _ascii() throws FTPException {
    try {
      getFTPCommand().type( 'A' );
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }

    transferMode = ASCII_TRANSFER_MODE;
  }

  private void _ebcdic() throws FTPException {
    OutputStream holdStream = recvCmdStream;

    getFTPCommand().setRecvCmdStream( null );
    _ascii();
    getFTPCommand().setRecvCmdStream( holdStream );

    transferMode = EBCDIC_TRANSFER_MODE;

    if ( null != recvCmdStream ) {
      try {
        Util.outputStreamPrintln(recvCmdStream, "200 Type set to EBCDIC.", true);
      } catch (IOException ioe){}
    }
  }

  private void _auto() {
    transferMode = AUTO_TRANSFER_MODE;

    if ( null != recvCmdStream ) {
      try {
        Util.outputStreamPrintln(recvCmdStream, "TYPE O", false);
        Util.outputStreamPrintln(recvCmdStream, "200 Type set to Auto.", true);
      } catch (IOException ioe){}
    }
  }

  private void _binary() throws FTPException {
    try {
      getFTPCommand().type( 'I' );
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }

    transferMode = BINARY_TRANSFER_MODE;
  }

  private void _modeZ() throws FTPException {
    try {
      getFTPCommand().modeZ();
      modeZEnabled = true;
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      modeZEnabled = false;
      throw cle;
    }
    catch ( FTPException e ) {
      modeZEnabled = false;
      throw e;
    }
  }

  private void _delete( RemoteFile file ) throws FTPException, 
                                                     IllegalArgumentException {
    String fileName = file.getFileName();

    if ( null != file.getMetaData("pwd") ) {
      fileName = file.getMetaData("pwd") + fileName;
    }

    try {
      getFTPCommand().delete( fileName );
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }
  }

  private void _rename( String from, String to ) throws FTPException, 
                                                      IllegalArgumentException {
    try {
      getFTPCommand().rename( from, to );
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }
  }

  private long _size( RemoteFile file ) throws FTPNoSuchFileException,
                                               FTPException,
                                               IllegalArgumentException {
    if ( file.getFileSize() >= 0 ) {
      return file.getFileSize();
    }

    try {
      return getFTPCommand().size( file.getFileName() );
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }
  }

  private Date _time( RemoteFile file ) throws FTPNoSuchFileException,
                                               FTPException,
                                               IllegalArgumentException {
    try {
      String fileName = file.getFileName();
      Date result = getFTPCommand().mdtm( fileName );
      return result;
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }
  }

  private void _mkdir( String newDir ) throws FTPException, 
                                              FTPAccessDeniedException,
                                              IllegalArgumentException {
    try {
      getFTPCommand().mkdir( newDir );
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }
  }

  private void _rmdir( RemoteFile dir ) throws FTPException, 
                                               IllegalArgumentException {
    if ( !dir.isDirectory() && !dir.isLink() ) {
      throw new IllegalArgumentException("File is not a directory.");
    }

    String dirName = dir.getFileName();

    if ( null != dir.getMetaData("pwd") ) {
      dirName = dir.getMetaData("pwd") + dirName;
    }

    try {
      getFTPCommand().rmdir( dirName );
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }
  }

  private void _cdup() throws FTPException {
    try {
      getFTPCommand().cdup(); 
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }
  }

  private boolean _isTransferRestartable() {
    boolean result = false;
    try {
       getFTPCommand().rest(0);
       result = true;
    }
    catch ( FTPException fe ) {}
    return result;
  }

  private void _setSocksProxy( String host, int port ) {
    Properties systemProps = System.getProperties();
    
    if ( host != null && host.trim().length() > 0 ) {
      systemProps.put("socksProxySet",  "true");
      systemProps.put("socksProxyHost", host.trim());
      systemProps.put("socksProxyPort", (new Integer(port)).toString());
    }
    else {
      systemProps.put("socksProxySet", "false");
      systemProps.remove("socksProxyHost");
      systemProps.remove("socksProxyPort");
    }

    System.setProperties( systemProps );
  }

/*
  private boolean verifyJar() {
    // until the url for the jar can be retrieved from a war file, we should
    // not do this - gary - 8/13/2003

    // java 1.2 doesn't support rsa signed jar files
    if (System.getProperty("java.version").startsWith("1.2")) {    
      return true;
    }
    else {
      return JarUtil.verifyJar( this.getClass(), "Secure FTP Bean" );
    }
    return true;
  }
*/

  /**
   * Forces passive data transfers to use the control socket IP address.
   *
   * @param on                true if to use control socket IP, false if off.
   */
  public void forcePasvToUseControlIP( boolean on ) {
    forcePasvToUseControlIP = on;
  }

  /**
   * Called before data transfers begin.
   */
  protected void aboutToTransferData() {
    getFTPCommand().forcePasvToUseControlIP( forcePasvToUseControlIP );
    if ( forcePasvToUseControlIP ) {
      String controlIP = getControlSocket().getInetAddress().getHostAddress();
      getFTPCommand().setControlIP( controlIP );
    }
  }

  private static String getDateStampFromFile() {
    if ( DATESTAMP == null ) {
      Class c = FTP.class;
      InputStream buildFileStream = c.getResourceAsStream("build.info");
      if ( buildFileStream != null ) {
        try {
          Properties buildProp = new Properties();
          buildProp.load( buildFileStream );
          DATESTAMP = buildProp.getProperty("build.date");
        }
        catch ( Exception ioe ) { DATESTAMP = "unknown"; }
      }
      else {
        DATESTAMP = "unknown";
      }
    }

    return DATESTAMP;
  }
}

class FileTypeDecider {
  private static String[] textExtensions = {
    "txt", "html", "htm", "xhtml", "pl", "sh", "xml", "java", "xsd", "bat",
    "cpp", "c", "hpp", "h", "vb", "ccs", "dss", "lsp", "pgr", "htc", "rtx",
    "tsv", "wml", "wmls", "hdml", "etx", "talk", "spc", "xsl", "sgml", "sgm",
    "rtf", "asc", "bas", "xaml", "php", "properties"
  };

  private static Hashtable text = setupHash();

  private static Hashtable setupHash() {
    text = new Hashtable();

    for (int i = 0; i < textExtensions.length; i++) {
      text.put( textExtensions[i], new Boolean(true) );
    }

    return text;
  }

  private static String getFileExtension( String filename ) {
    int index = filename.lastIndexOf('.');

    if (index >= 0)
      return filename.substring(index + 1);
    else
      return null;
  }

  public static boolean isAscii( String filename ) {
    String temp = getFileExtension( filename );
    return (temp != null && text.get(temp) != null);
  }
}
