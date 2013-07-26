
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: SSLFTP.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.bean;

import com.glub.secureftp.common.*;
import com.glub.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

import java.security.*;
import java.security.cert.*;
import java.security.spec.*;

//import com.sun.net.ssl.*;
//import javax.net.ssl.*;
import javax.net.ssl.SSLSocketFactory;      
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * The <code>SSLFTP</code> class is responsible for handling the SSL 
 * extensions of the File Transfer Protocol.
 *
 * @author Gary Cohen
 * @version $Revision: 47 $, $Date: 2009-12-04 00:33:29 -0800 (Fri, 04 Dec 2009) $
 * @since 2.5.1
 */

public class SSLFTP extends FTP {
  /** Used to set the connection type to <code>implicit</code>. */
  public static final short IMPLICIT_CONNECTION = 0;

  /** Used to set the connection type to <code>explicit</code>. */
  public static final short EXPLICIT_CONNECTION = 1;

  /** This value is used to handle the SSL cert stuff. */ 
  private SSLSessionManager sslSessionManager = null;

  /** This value is used to hold the type of connection to make. */
  private short connectionType = IMPLICIT_CONNECTION;

  /** This value is what we send during an AUTH command (e.g. AUTH TLS). */
  private String authType = "TLS";

  /** This value handles the preSeeding of the SecureRandom object */
  private static SecureRandomThread srt = null;

  /** This value holds the SecureRandom object used in the SSL randomization. */
  private SecureRandom secureRandom = null;

  /** This file holds the certificates */
  private File keyStoreFile = null;

  /** This is the password for the keystore */
  private char[] keyStorePassword = null;

  /** If a password is not supplied, this one will be used. */
  private final String DEFAULT_KEYSTORE_PASS = "sEcUrEfTp";

  /** This is the key store. */
  private KeyStore keyStore = null;

  /** This is a helper class that we use in SSLTrustManager. */
  private SSLKeyStore sslKeyStore = null;

  /** This is used to get key managers. */
  private javax.net.ssl.KeyManagerFactory keyManagerFactory = null;

  /** This is used to get an ssl context. */
  private javax.net.ssl.KeyManager[] keyManagerArray = null;

  /** This handles the trust issues of certificates. */
  private javax.net.ssl.TrustManager[] trustManagerArray = null;

  /** This is the ssl context. It is used in the SSL factories. */
  private javax.net.ssl.SSLContext sslContext = null;

  /** This is used to get ssl sockets. */
  private SSLSocketFactory sslSocketFactory = null;

  /** This is used to get ssl server sockets. */
  private SSLServerSocketFactory sslServerSocketFactory = null;

  /** This value stores whether we are encrypting the data channel. */
  private boolean dataEncryptionOn = false;

  /** This is the orig control socket (used if socket become secure). */
  private Socket origControlSocket = null;

  /** This is the secure control socket (used if socket become secure). */
  private Socket secureControlSocket = null;

  /** Send the data protection level (needs to happen once after login) */
  private boolean sentDataProtectionLevel = false;

  /** How to set the data protection level */
  private boolean encryptData = false;

  /** Debug output */
  private boolean debug = GTOverride.getBoolean("glub.debug");

  /**
   * Create a new <code>SSLFTP</code> object without a key store nor response
   * notification.
   *
   * @param sslSessionManager the Object that handles certificate information
   *                          and decisions that are made based on these 
   *                          certificates.
   * @param hostInfo          the HostInfo to connect to.
   * @param connectionType    the type of connection to make (implicit or 
   *                          explicit).
   *
   * @see #IMPLICIT_CONNECTION
   * @see #EXPLICIT_CONNECTION
   */
  public SSLFTP( SSLSessionManager sslSessionManager, 
                 HostInfo hostInfo, short connectionType ) {
    this( sslSessionManager, hostInfo.getHostName(), hostInfo.getPort(), 
          null, null, null, connectionType, null, null );
  }

  /**
   * Create a new <code>SSLFTP</code> object without a key store nor response
   * notification.
   *
   * @param sslSessionManager the Object that handles certificate information
   *                          and decisions that are made based on these 
   *                          certificates.
   * @param host              the hostname to connect to.
   * @param port              the port to connect to.
   * @param connectionType    the type of connection to make (implicit or 
   *                          explicit).
   *
   * @see #IMPLICIT_CONNECTION
   * @see #EXPLICIT_CONNECTION
   */
  public SSLFTP( SSLSessionManager sslSessionManager, 
                 String host, int port, short connectionType ) {
    this( sslSessionManager, host, port, null, null, null, connectionType,
          null, null );
  }

  /**
   * Create a new <code>SSLFTP</code> object without a key store but with
   * response notification.
   *
   * @param sslSessionManager the Object that handles certificate information
   *                          and decisions that are made based on these 
   *                          certificates.
   * @param hostInfo          the HostInfo to connect to.
   * @param connectionType    the type of connection to make (implicit or 
   *                          explicit).
   * @param  sendCmdStream    the commands sent to the server.
   *                          Pass <code>null</code> if not interested
   *                          in this data.
   * @param  recvCmdStream    the responses returned from the server.
   *                          Pass <code>null</code> if not interested
   *                          in this data.
   *
   * @see #IMPLICIT_CONNECTION
   * @see #EXPLICIT_CONNECTION
   */
  public SSLFTP( SSLSessionManager sslSessionManager, 
                 HostInfo hostInfo, short connectionType,
                 OutputStream sendCmdStream, OutputStream recvCmdStream ) {
    this( sslSessionManager, hostInfo.getHostName(), hostInfo.getPort(), 
          null, null, null, connectionType, sendCmdStream, recvCmdStream );
  }

  /**
   * Create a new <code>SSLFTP</code> object without a key store but with
   * response notification.
   *
   * @param sslSessionManager the Object that handles certificate information
   *                          and decisions that are made based on these 
   *                          certificates.
   * @param host              the hostname to connect to.
   * @param port              the port to connect to.
   * @param connectionType    the type of connection to make (implicit or 
   *                          explicit).
   * @param  sendCmdStream    the commands sent to the server.
   *                          Pass <code>null</code> if not interested
   *                          in this data.
   * @param  recvCmdStream    the responses returned from the server.
   *                          Pass <code>null</code> if not interested
   *                          in this data.
   *
   * @see #IMPLICIT_CONNECTION
   * @see #EXPLICIT_CONNECTION
   */
  public SSLFTP( SSLSessionManager sslSessionManager, 
                 String host, int port, short connectionType,
                 OutputStream sendCmdStream, OutputStream recvCmdStream ) {
    this( sslSessionManager, host, port, null, null, null, connectionType,
          sendCmdStream, recvCmdStream );
  }

  /**
   * Create a new <code>SSLFTP</code> object with a key store but without
   * response notification.
   *
   * @param sslSessionManager the Object that handles certificate information
   *                          and decisions that are made based on these 
   *                          certificates.
   * @param hostInfo          the HostInfo to connect to.
   * @param keyStoreFile      the file that acts as the key store.
   * @param keyStorePass      the key store's password.
   * @param connectionType    the type of connection to make (implicit or 
   *                          explicit).
   *
   * @see #IMPLICIT_CONNECTION
   * @see #EXPLICIT_CONNECTION
   */
  public SSLFTP( SSLSessionManager sslSessionManager, HostInfo hostInfo, 
                 File keyStoreFile, String keyStorePass, 
                 short connectionType ) {
    this( sslSessionManager, hostInfo.getHostName(), hostInfo.getPort(), 
          keyStoreFile, keyStorePass, null, connectionType, null, null );
  }

  /**
   * Create a new <code>SSLFTP</code> object with a key store but without
   * response notification.
   *
   * @param sslSessionManager the Object that handles certificate information
   *                          and decisions that are made based on these 
   *                          certificates.
   * @param host              the hostname to connect to.
   * @param port              the port to connect to.
   * @param keyStoreFile      the file that acts as the key store.
   * @param keyStorePass      the key store's password.
   * @param connectionType    the type of connection to make (implicit or 
   *                          explicit).
   *
   * @see #IMPLICIT_CONNECTION
   * @see #EXPLICIT_CONNECTION
   */
  public SSLFTP( SSLSessionManager sslSessionManager, String host, int port, 
                 File keyStoreFile, String keyStorePass, 
                 short connectionType ) {
    this( sslSessionManager, host, port, keyStoreFile, keyStorePass, null, 
          connectionType, null, null );
  }

  /**
   * Create a new <code>SSLFTP</code> object with a key store and response
   * notification.
   *
   * @param sslSessionManager the Object that handles certificate information
   *                          and decisions that are made based on these 
   *                          certificates.
   * @param hostInfo          the HostInfo to connect to.
   * @param keyStoreFile      the file that acts as the key store.
   * @param keyStorePass      the key store's password.
   * @param connectionType    the type of connection to make (implicit or 
   *                          explicit).
   * @param  sendCmdStream    the commands sent to the server.
   *                          Pass <code>null</code> if not interested
   *                          in this data.
   * @param  recvCmdStream    the responses returned from the server.
   *                          Pass <code>null</code> if not interested
   *                          in this data.
   *
   * @see #IMPLICIT_CONNECTION
   * @see #EXPLICIT_CONNECTION
   */
  public SSLFTP( SSLSessionManager sslSessionManager, HostInfo hostInfo, 
                 File keyStoreFile, String keyStorePass, short connectionType,
                 OutputStream sendCmdStream, OutputStream recvCmdStream ) {
    this( sslSessionManager, hostInfo.getHostName(), hostInfo.getPort(), 
          keyStoreFile, keyStorePass, null, connectionType, sendCmdStream, 
          recvCmdStream );
  }

  /**
   * Create a new <code>SSLFTP</code> object with a key store and response
   * notification.
   *
   * @param sslSessionManager the Object that handles certificate information
   *                          and decisions that are made based on these 
   *                          certificates.
   * @param host              the hostname to connect to.
   * @param port              the port to connect to.
   * @param keyStoreFile      the file that acts as the key store.
   * @param keyStorePass      the key store's password.
   * @param connectionType    the type of connection to make (implicit or 
   *                          explicit).
   * @param  sendCmdStream    the commands sent to the server.
   *                          Pass <code>null</code> if not interested
   *                          in this data.
   * @param  recvCmdStream    the responses returned from the server.
   *                          Pass <code>null</code> if not interested
   *                          in this data.
   *
   * @see #IMPLICIT_CONNECTION
   * @see #EXPLICIT_CONNECTION
   */
  public SSLFTP( SSLSessionManager sslSessionManager, String host, int port, 
                 File keyStoreFile, String keyStorePass, short connectionType,
                 OutputStream sendCmdStream, OutputStream recvCmdStream ) {
    this( sslSessionManager, host, port, keyStoreFile, keyStorePass, null,
          connectionType, sendCmdStream, recvCmdStream );
  }

  /**
   * Create a new <code>SSLFTP</code> object with a key store, response 
   * notification, and an overridden <code>SecureRandom</code> object.
   *
   * @param sslSessionManager the Object that handles certificate information
   *                          and decisions that are made based on these 
   *                          certificates.
   * @param hostInfo          the HostInfo to connect to.
   * @param keyStoreFile      the file that acts as the key store.
   * @param keyStorePass      the key store's password.
   * @param random            you can override our randomizer with your own.
   * @param connectionType    the type of connection to make (implicit or 
   *                          explicit).
   * @param  sendCmdStream    the commands sent to the server.
   *                          Pass <code>null</code> if not interested
   *                          in this data.
   * @param  recvCmdStream    the responses returned from the server.
   *                          Pass <code>null</code> if not interested
   *                          in this data.
   *
   * @see #IMPLICIT_CONNECTION
   * @see #EXPLICIT_CONNECTION
   */
  public SSLFTP( SSLSessionManager sslSessionManager, HostInfo hostInfo, 
                 File keyStoreFile, String keyStorePass, SecureRandom random,
                 short connectionType, OutputStream sendCmdStream,
                 OutputStream recvCmdStream ) {
    this( sslSessionManager, hostInfo.getHostName(), hostInfo.getPort(), 
          keyStoreFile, keyStorePass, random, connectionType, sendCmdStream, 
          recvCmdStream );
  }

  /**
   * Create a new <code>SSLFTP</code> object with a key store, response 
   * notification, and an overridden <code>SecureRandom</code> object.
   *
   * @param sslSessionManager the Object that handles certificate information
   *                          and decisions that are made based on these 
   *                          certificates.
   * @param host              the hostname to connect to.
   * @param port              the port to connect to.
   * @param keyStoreFile      the file that acts as the key store.
   * @param keyStorePass      the key store's password.
   * @param random            you can override our randomizer with your own.
   * @param connectionType    the type of connection to make (implicit or 
   *                          explicit).
   * @param  sendCmdStream    the commands sent to the server.
   *                          Pass <code>null</code> if not interested
   *                          in this data.
   * @param  recvCmdStream    the responses returned from the server.
   *                          Pass <code>null</code> if not interested
   *                          in this data.
   *
   * @see #IMPLICIT_CONNECTION
   * @see #EXPLICIT_CONNECTION
   */
  public SSLFTP( SSLSessionManager sslSessionManager, String host, int port, 
                 File keyStoreFile, String keyStorePass, SecureRandom random,
                 short connectionType, OutputStream sendCmdStream,
                 OutputStream recvCmdStream ) {
    super( host, port, sendCmdStream, recvCmdStream );
    _init( sslSessionManager, host, port, keyStoreFile, keyStorePass, random,
           connectionType, sendCmdStream, recvCmdStream );
  }

  /**
   * Specify a private key and public certificate chain to use for client
   * authentication.
   *
   * @param privateKey     The private key for the client
   * @param certList       The public certificates for the client
   *
   * @throws FileNotFoundException
   * @throws IOException
   * @throws InvalidKeySpecException
   * @throws CertificateException
   * @throws KeyStoreException
   */
  public void setClientAuthentication( File privateKey,
                                       File[] certList ) 
                                       throws FileNotFoundException,
                                              IOException,
                                              InvalidKeySpecException,
                                              CertificateException,
                                              KeyStoreException {
    setClientAuthentication( privateKey, certList, null );
  }

  /**
   * Specify a private key and public certificate chain to use for client
   * authentication.
   *
   * @param privateKey     The private key for the client
   * @param certList       The public certificates for the client
   * @param password       The password to access the key/certifificate
   *
   * @throws FileNotFoundException
   * @throws IOException
   * @throws InvalidKeySpecException
   * @throws CertificateException
   * @throws KeyStoreException
   */
  public void setClientAuthentication( File privateKey,
                                       File[] certList, String password ) 
                                       throws FileNotFoundException,
                                              IOException,
                                              InvalidKeySpecException,
                                              CertificateException,
                                              KeyStoreException {
    if ( null == password ) {
      password = "";
    }

    PrivateKey pk = KeyUtil.getPrivateKey( KeyUtil.getKeyFactory(),
                                           privateKey.getAbsolutePath(),
                                           password );
    
    if (debug) {
      String pkInfo = "undefined";
      if (pk != null) {
        pkInfo = pk.getAlgorithm();
      }

      System.out.println("Client Auth: private key alg = " + pkInfo);
    }

    ArrayList al = new ArrayList( certList.length );

    for( int i = 0; i < certList.length; i++ ) {
      String path = certList[i].getAbsolutePath();
      al.add( path );      
    }

    String[] certArray = (String[])al.toArray(new String[1]);
    X509Certificate[] pubCerts = 
      KeyUtil.getCertificateList( KeyUtil.getCertificateFactory(), certArray,
                                  password );

    if (debug) {
      for (int j = 0; j < pubCerts.length; j++) {
        System.out.println("Client Auth: cert " + j + " = " + 
                           pubCerts[j].getSubjectDN().toString());
      }
    }

    setClientAuthentication( pk, pubCerts );
  }

  /**
   * Specify a private key and public certificate chain to use for client
   * authentication.
   *
   * @param privateKey     The private key for the client
   * @param certList       The public certificates for the client
   *
   * @throws KeyStoreException
   */
  public void setClientAuthentication( PrivateKey privateKey,
                                       X509Certificate[] certList ) 
                                       throws KeyStoreException {
    if ( null != keyStore && null != privateKey && certList.length > 0 ) {
      keyStore.setKeyEntry("secureftp_client_key", 
                           privateKey, keyStorePassword, certList);

      try {
        _initSSLContext();
      }
      catch ( Exception e ) {}
    }
  }

  /**
   * Clear the client key from the keystore (if it exists)
   *
   * @throws KeyStoreException
   */
  public void clearClientAuthentication() throws KeyStoreException {
    if ( null != keyStore && keyStore.isKeyEntry("secureftp_client_key") ) {
      keyStore.deleteEntry( "secureftp_client_key" );
      try {
        _initSSLContext();
      }
      catch ( Exception e ) {}
    }
  }

  /**
   * Connect to the FTP host and port with data encyption off by default. 
   * If the port was not set, we default to 21. If you are doing an explicit 
   * SSL connection, the <code>AUTH</code> command is sent here. 
   * By default we attempt an implicit connection.
   *
   * @throws FTPConnectException      if the connection fails.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there are socket problems.
   * @throws UnknownHostException     if the host could not be found.
   * @throws IllegalArgumentException if <code>hostName</code> is
   *                                  <code>null</code>.
   *
   * @see #doExplicitHandshake()
   * @see SSLFTPCommand#auth(String)
   */
  public void connect() throws FTPConnectException, FTPException, IOException, 
                               UnknownHostException, IllegalArgumentException {
    connect( false );
  }

  /**
   * Connect to the FTP host and port. If the port was not set, we default
   * to 21. If you are doing an explicit SSL connection, the <code>AUTH</code>
   * command is sent here. By default we attempt an implicit connection.
   *
   * @param encryptData               the default encryption state of the 
   *                                  data channel
   *
   * @throws FTPConnectException      if the connection fails.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there are socket problems.
   * @throws UnknownHostException     if the host could not be found.
   * @throws IllegalArgumentException if <code>hostName</code> is
   *                                  <code>null</code>.
   *
   * @see #doExplicitHandshake()
   * @see SSLFTPCommand#auth(String)
   */
  public void connect( boolean encryptData ) throws FTPConnectException, 
                                                    FTPException, IOException, 
                                                    UnknownHostException, 
                                                    IllegalArgumentException {
    if ( !keyStoreLoaded ) {
      throw new FTPKeyStoreException( keyStoreErrorMessage );
    }

    _connect();

    this.encryptData = encryptData;
/*
    try {
      setDataEncryptionOn( encryptData );
    }
    catch ( FTPException fe ) {
      // this better work, but in case it doesn't ...
      // eat up the exception
      dataEncryptionOn = false;
    }
*/
  }

  /**
   * This handles an explicit SSL connection by sending the AUTH command 
   * to the FTP server and converting the plaintext control socket into an 
   * SSL control socket.
   *
   * @throws FTPAuthNotSupportedException if the auth command is not supported.
   * @throws FTPException             if the FTP server returns an error code.
   * @throws IOException              if there are socket problems.
   * @throws IllegalArgumentException if the auth type is <code>null</code>. 
   */
  protected void doExplicitHandshake() throws FTPAuthNotSupportedException,
                                              FTPException, IOException,
                                              IllegalArgumentException {
    // send auth ssl command
    try {
      ((SSLFTPCommand)getFTPCommand()).auth( getAuthType() );
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      throw cle;
    }

    // convert plaintext socket into SSL socket
    Socket oldSock = getControlSocket();
/*
    Socket newSock = 
      SSLUtil.createSocket( oldSock, 
                            oldSock.getInetAddress().getHostName(),
                            oldSock.getPort(), sslSocketFactory, false ); 
*/
      if (debug) {
        System.out.print( "Creating explicit socket... " );
      }

    Socket newSock = 
      SSLUtil.createSocket( oldSock, 
                            oldSock.getInetAddress().getHostAddress(),
                            oldSock.getPort(), sslSocketFactory, false ); 

      if (debug) {
        System.out.println( "done" );
      }

    setControlSocket( newSock, true );
  }

  /**
   * Revert a secure connection back to a clear control connection.
   *
   * @throws FTPException             if the FTP server returns an error code.
   */
  public void setClearCommandChannel() throws FTPException, IOException {
    _setClearCommandChannel();
  }

  /**
   * Called before data transfers begin.
   */
  protected void aboutToTransferData() {
    super.aboutToTransferData();
    if ( !sentDataProtectionLevel ) {
      sentDataProtectionLevel = true;

      try {
        setDataEncryptionOn( encryptData );
      }
      catch ( FTPException fe ) {
        // this better work, but in case it doesn't ...
        // eat up the exception
        dataEncryptionOn = false;
      }
    }
  }

  /**
   * Set the control socket.
   *
   * @param controlSocket        the control socket.
   * @param saveInsecureSocket   allow the old socket to be saved so CCC can
   *                             be used.
   */
  protected void setControlSocket( Socket controlSocket, 
                                   boolean saveInsecureSocket ) 
                                                            throws IOException {
    if ( saveInsecureSocket ) {
      origControlSocket = getControlSocket();
    }

    setControlSocket( controlSocket );
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
    Socket cs = null;

    if ( IMPLICIT_CONNECTION == connectionType  ) {
      cs = SSLUtil.createSocket( hostInfo.getHostName(), hostInfo.getPort(),
                                 sslSocketFactory );
    }
    else {
      cs = super.makeControlSocket( hostInfo );
    }

    return cs;
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
    Socket ds = null;

    if ( dataEncryptionOn ) {
/*
      ds = SSLUtil.createSocket( hostInfo.getHostName(), hostInfo.getPort(),
                                 sslSocketFactory );
*/
      ds = SSLUtil.createSocket( hostInfo.getHostAddress(), hostInfo.getPort(),
                                 sslSocketFactory );
    }
    else {
      ds = super.makeDataSocket(hostInfo);
    }

    return ds;
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
    ServerSocket dss = null;

    if ( dataEncryptionOn ) {
      dss = sslServerSocketFactory.createServerSocket(hostInfo.getPort());
      ((SSLServerSocket)dss).setUseClientMode(true);
    }
    else {
      dss = super.makeDataServerSocket(hostInfo);
    }

    return dss;
  }

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
    return new SSLFTPCommand( inputReader, outputWriter,
                              sendCmdStream, recvCmdStream );
  }

  /**
   * Logout from the FTP server.
   *
   * @throws IOException               if there is a socket problem.
   * @throws FTPException              if the FTP server returns an error code.
   */
  public void logout() throws IOException, FTPException {
    super.logout();
    if ( origControlSocket != null ) {
      origControlSocket.close();
      origControlSocket = null;
    }

    if ( secureControlSocket != null ) {
      secureControlSocket.close();
      secureControlSocket = null;
    }
  }

  /**
   * Whether or not data encryption is being done.
   *
   * @return true if data encryption is on.
   */
  public boolean isDataEncryptionOn() {
    return dataEncryptionOn;
  }

  /**
   * Forces the encryption of the data channel on or off.
   * Note: this method is <b>NOT</b> recommended as it doesn't check the
   * status of the server and just makes assumptions that the data connection
   * is on/off. 
   *
   * @param on                true if data encrytion is to be on, false if off.
   *
   * @throws FTPException     if the FTP server returns an error code.
   */
  public void forceDataEncryptionOn( boolean on ) 
                                   throws FTPException {
    dataEncryptionOn = on;
    sentDataProtectionLevel = true;
  }

  /**
   * Set the encryption of the data channel on or off.
   *
   * @param on                true if data encrytion is to be on, false if off.
   *
   * @throws FTPException     if the FTP server returns an error code.
   */
  public void setDataEncryptionOn( boolean on ) throws FTPException {
    _setDataEncryptionOn( on );
  }

  /**
   * Get the <code>AUTH</code> type we are sending during the AUTH command.
   *
   * @return the <code>AUTH</code> type (e.g. SSL).
   *
   * @see SSLFTPCommand#auth(String)
   */
  public String getAuthType() { return authType; }

  /**
   * This allows the default <code>AUTH</code> type to be set from SSL to
   * some other type (such as TLS).
   *
   * @param authType the <code>AUTH</code> type to send during the AUTH command.
   *
   * @see SSLFTPCommand#auth(String)
   */
  public void setAuthType( String authType ) { this.authType = authType; }

  /**
   * This allows the <code>SecureRandom</code> object to be generated prior
   * to being used. This object takes a significant amount of time to be
   * generated and it is advised that this call be done on program 
   * initialization. It is threaded for performance.
   *
   * @see SecureRandom
   */
  public static void preSeed() {
    srt = new SecureRandomThread();
    srt.setPriority( Thread.MIN_PRIORITY );
    srt.start();
  }

  /**
   * This allows the list of <code>SSLCertificate</code> objects to be returned
   * from the <code>KeyStore</code>.
   *
   * @see SSLCertificate
   *
   * @return an ArrayList of SSLCertificate objects
   */
  public static ArrayList getCertificates( File keyStoreFile, 
                                           String keyStorePass ) {
    return _getCertificates(keyStoreFile, keyStorePass);
  }

  /** Get default keystore password */
  private String getDefaultKeyStorePass() { return DEFAULT_KEYSTORE_PASS; }

  /** KeyStore loaded successfully */
  private boolean keyStoreLoaded = false;

  /** KeyStore error message */
  private String keyStoreErrorMessage = "";

  /*
   *
   * The methods below are here for obfuscation purposes.
   *
   */

  private void _init( SSLSessionManager ssm, String host, 
                      int port, File ksf, String keyStorePass, 
                      SecureRandom random, short connectionType, 
                      OutputStream sendCmdStream, OutputStream recvCmdStream ) {
    sslSessionManager = ssm;

    this.connectionType = connectionType;

    keyStoreFile = ksf;

    if ( keyStorePass == null ) {
      keyStorePass = getDefaultKeyStorePass();
    }

    keyStorePassword = keyStorePass.toCharArray(); 

    secureRandom = random;

    try {
      // load the keystore
      keyStore = KeyUtil.getKeyStore( keyStoreFile, keyStorePassword ); 

      sslKeyStore = new SSLKeyStore( keyStore, keyStoreFile, keyStorePassword );

      InetAddress hostAddress = null;
      try {
        InetAddress.getByName( host );
      }
      catch ( UnknownHostException uhe ) {}

      // get the trust managers
      trustManagerArray = new SSLTrustManager[] { 
        new SSLTrustManager( sslKeyStore, sslSessionManager, hostAddress,
                             recvCmdStream ) 
      };

      // get ssl context
      if ( srt != null && srt.isSeeding() ) {
        sslSessionManager.randomSeedIsGenerating();
        try {
          if ( debug ) {
            System.out.print( "Waiting for seeding to finish... " );
          }

          srt.join();

          if ( debug ) {
            System.out.println( "done." );
          }

          secureRandom = srt.getRandom();
          sslSessionManager.randomSeedGenerated();
        }
        catch (InterruptedException ie) {
          if ( debug ) {
            System.out.println( "Something went wrong with the seeding: " + ie.getMessage() );
          }
        }
      }
      else if ( srt != null ) {
        secureRandom = srt.getRandom();
      }

      if ( debug )
        System.out.print( "SSL context initialization... " );

      _initSSLContext();

      if ( debug )
        System.out.println( "done" );

      keyStoreLoaded = true;
    }
    catch ( KeyStoreException kse ) {
      if (debug)
    	  kse.printStackTrace();
    }
    catch ( CertificateException ce ) {
      if (debug)
    	  ce.printStackTrace();
    }
    catch ( UnrecoverableKeyException uke ) {
      if (debug)
    	uke.printStackTrace();
    }
    catch ( KeyManagementException kme ) {
      if (debug)
    	kme.printStackTrace();
    }
    catch ( IOException ioe ) {
      if (debug)
        ioe.printStackTrace();

      keyStoreLoaded = false;
      keyStoreErrorMessage = ioe.getMessage();
    }
  }

  private static ArrayList _getCertificates( File keyStoreFile, 
                                             String keyStorePass ) {
   ArrayList result = new ArrayList();

   if ( null == keyStorePass ) {
     keyStorePass = "sEcUrEfTp";
   }

   try {
     KeyStore ks = KeyUtil.getKeyStore( keyStoreFile, 
                                        keyStorePass.toCharArray() ); 
     Enumeration e = ks.aliases();
     String alias = null;
     while ( e.hasMoreElements() ) {
       X509Certificate cert = 
         (X509Certificate)ks.getCertificate( alias = (String)e.nextElement() );
       if ( !ks.isKeyEntry(alias) )
         result.add( new SSLCertificate(cert) );
     } 
   }
   catch ( Exception e ) {}

   return result;
  }

  private void _connect() throws FTPConnectException, FTPException, 
                                 IOException, UnknownHostException, 
                                 IllegalArgumentException {
    if ( debug )
      System.out.println( "Making connection" );

    super.connect();

    // if we are doing an explicit SSL connection, do an AUTH SSL here
    if ( EXPLICIT_CONNECTION == connectionType ) {
      if ( debug )
        System.out.println( "Doing explicit handshake" );
      doExplicitHandshake();
    }
  }

  private void _setClearCommandChannel() throws FTPException, IOException {
    if ( null == origControlSocket ) {
      throw new FTPException( "Clear command channel could not be set." );
    }

    try {
      ((SSLFTPCommand)getFTPCommand()).ccc();

      secureControlSocket = getControlSocket();
      setControlSocket( origControlSocket, false ); 

      origControlSocket = null;
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      dataEncryptionOn = false;
      throw cle;
    }
  }

  private void _setDataEncryptionOn( boolean on ) throws FTPException {
    dataEncryptionOn = on;
 
    sentDataProtectionLevel = true;

    try {
      ((SSLFTPCommand)getFTPCommand()).pbsz(0);
    }
    catch ( FTPConnectionLostException cle ) {
      isConnected = false;
      isLoggedIn = false;
      dataEncryptionOn = false;
      throw cle;
    }
    catch ( FTPPolicyRestrictionException fpre ) {}
    catch ( FTPException fe ) {
      dataEncryptionOn = false;
      throw fe;
    }

    if ( dataEncryptionOn ) {
      try {
      ((SSLFTPCommand)getFTPCommand()).prot(SSLFTPCommand.PRIVATE_DATA_CHANNEL);
      }
      catch ( FTPConnectionLostException cle ) {
        isConnected = false;
        isLoggedIn = false;
        throw cle;
      }
      catch ( FTPPolicyRestrictionException fpre ) {
        dataEncryptionOn = false;
      }
      catch ( FTPException fe ) {
        // if we can't turn the data encryption on, turn it off
        dataEncryptionOn = false;
        throw fe;
      }
    }
    else {
      try {
      ((SSLFTPCommand)getFTPCommand()).prot(SSLFTPCommand.CLEAR_DATA_CHANNEL);
      }
      catch ( FTPPolicyRestrictionException fpre ) {
        dataEncryptionOn = false;
      }
      catch ( FTPConnectionLostException cle ) {
        isConnected = false;
        isLoggedIn = false;
        throw cle;
      }
    }
  }

  private void _initSSLContext() throws KeyManagementException,
                                        UnrecoverableKeyException,
                                        KeyStoreException {
    // get the key manager factory 
    keyManagerFactory = KeyUtil.getKeyManagerFactory( keyStore, 
                                                      keyStorePassword );

    keyManagerArray = keyManagerFactory.getKeyManagers();

    // get the ssl context
    sslContext = SSLUtil.getContext( keyManagerArray, 
                                     trustManagerArray, secureRandom );

    // build ssl socket factory
    sslSocketFactory = SSLUtil.getSocketFactory( sslContext );

    // build ssl server socket factory
    sslServerSocketFactory = SSLUtil.getServerSocketFactory( sslContext );
  }
}

/**
 * This handles the creation of the <code>SecureRandom</code> object.
 */
class SecureRandomThread extends Thread {
  private SecureRandom random = null;
  private boolean isSeeding = true;

  public void run() {
    isSeeding = true;
    byte[] b = new byte[1];

    try {
      random = SecureRandom.getInstance("SHA1PRNG");
    }
    catch (java.security.NoSuchAlgorithmException nsae) { }

    // Force SecureRandom object to seed itself
    if (random != null)
    	random.nextBytes(b);

    isSeeding = false;
  }

  public SecureRandom getRandom() {
    return random;
  }

  public boolean isSeeding() { 
    return isSeeding; 
  }
}

/**
 * This is a helper object for the SSLTrustManager.
 */
class SSLKeyStore {
  public KeyStore keyStore = null;
  public File keyStoreFile = null;
  public char[] keyStorePass = null;

  public SSLKeyStore( KeyStore keyStore, File keyStoreFile, 
                      char[] keyStorePass ) {
    setKeyStore( keyStore );
    setKeyStoreFile( keyStoreFile );
    setKeyStorePass( keyStorePass );
  }

  public KeyStore getKeyStore() { return keyStore; }
  public void setKeyStore( KeyStore keyStore ) { this.keyStore = keyStore; }

  public File getKeyStoreFile() { return keyStoreFile; }
  public void setKeyStoreFile( File keyStoreFile ) { 
    this.keyStoreFile = keyStoreFile; 
  }

  public char[] getKeyStorePass() { return keyStorePass; }
  public void setKeyStorePass( char[] keyStorePass ) { 
    this.keyStorePass = keyStorePass; 
  }
}
