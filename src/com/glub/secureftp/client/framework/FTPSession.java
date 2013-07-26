
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: FTPSession.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.framework;

import com.glub.secureftp.bean.*;

import java.awt.*;
import java.io.*;
import java.net.*;
import javax.net.ssl.*;

public class FTPSession {

  public static final short NO_SECURITY  = 0;
  public static final short EXPLICIT_SSL = 1;
  public static final short IMPLICIT_SSL = 2;

  private String userName = null;
  private String password = null;
  private String account  = null;
  private String hostName = null;
  private int port = Constants.DEF_IMPLICIT_SSL_PORT;
  private File localDir = null;
  private String pwd = "";
  private boolean interactiveOn = true;
  private boolean globOn = true;
  private boolean beepWhenDone = true;
  private boolean debugOn = false;
  private boolean showProgress = true;
  private boolean reportVerbose = true;
  private boolean isSecure = false;
  private boolean canRestartTransfer = true;
  private FTP ftpBean = null;
  private short securityMode = IMPLICIT_SSL;
  private boolean maskPass = true;
  private OutputStream outputStream = System.out;
  private boolean supportsMDTM = true;
  private boolean supportsSIZE = true;
  private boolean testForREST = true;
  private boolean supportsREST = false;
  private boolean useProxy = false;
  private boolean useCCC = false;
  private FTPAbortableTransfer listAbort = null;
  private boolean isTransferringData = false;
  private long lastCommandSent = 0L;

  private File keyStoreFile = null;
  private SSLCertificateHandler certHandler = null;
  private SSLCertificate currentCertificate = null;

  private Container remoteUI = null;

  public FTPSession() {
    this( null, null, null, null, 0, false, null );
  }

  public FTPSession( String userName, String password, String account, 
                     String hostName, int port, boolean isSecure,
                     SSLCertificateHandler certHandler ) {
    setUserName( userName );
    setPassword( password );
    setAccount( account );
    setHostName( hostName );
    setPort( port ); 
    setIsSecure( isSecure );
    setCertHandler( certHandler );

    setOutputStream( System.out );

    try {
      File startDir = new File(System.getProperty("user.dir"));

      // this should never happen!!!
      if (!startDir.exists() || !startDir.isDirectory())
    	  return;
      
      setLocalDir( new File(startDir.getCanonicalPath()) );
    } catch ( Exception e ) {}

    initKeyStoreFile();
  }

  public FTPSession ( FTPSession session ) {
    setUserName( session.getUserName() );
    setPassword( session.getPassword() );
    setAccount( session.getAccount() );
    setHostName( session.getHostName() );
    setPort( session.getPort() );
    setIsSecure( session.isSecure() );
    setCertHandler( session.getCertHandler() );
    setOutputStream( session.getOutputStream() );
    try {
      setLocalDir( session.getLocalDir() );
    } catch ( Exception e ) {}
    setWorkingDir( session.getWorkingDir() );
    setInteractiveOn( session.isInteractiveOn() );
    setGlobOn( session.isGlobOn() );
    setBeepWhenDone( session.getBeepWhenDone() );
    setDebugOn( session.isDebugOn() );
    setShowProgress( session.showProgress() );
    setReportVerbose( session.reportVerbose() );
    setSecurityMode( session.getSecurityMode() );
    setTransferRestartable( session.canRestartTransfer() );
    setMaskPass( session.maskPass() );
    setFTPBean( session.getFTPBean() );
    setRemoteUI( session.getRemoteUI() );
    initKeyStoreFile();
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append( "Hostname => " + getHostName() );
    buffer.append( ", Username => " + getUserName() );
    buffer.append( ", Password => **********" );
    buffer.append( ", Account => " + getAccount() );
    buffer.append( ", Port => " + getPort() );
    buffer.append( ", Security Mode => " + getSecurityMode() );
    buffer.append( ", Is Secure => " + isSecure() );
    buffer.append( ", Local Dir => " + localDir.getAbsolutePath() );
    buffer.append( ", Current Working Dir => " + getWorkingDir() );
    return buffer.toString();
  }

  public void clearSession() {
    setHostName(null);
    setPort(0);
    setUserName(null);
    setPassword(null);
    setAccount(null);
    setIsSecure(false);
    setCurrentCertificate(null);
    setSecurityMode(IMPLICIT_SSL);
  }

  public boolean isTransferringData() { return isTransferringData; }
  public void setTransferringData( boolean transferring ) { 
    isTransferringData = transferring; 
  }

  public long getLastCommandSent() { return lastCommandSent; }
  public void setLastCommandSent( long now ) { lastCommandSent = now; }

  public String getUserName() { return userName; }
  public void   setUserName( String userName ) { 
    if ( null != userName ) {
      userName = userName.trim();
    }
    this.userName = userName; 
  }

  public String getPassword() { return password; }
  public void   setPassword( String password ) { this.password = password; }

  public String getAccount() { return account; }
  public void   setAccount( String account ) { this.account = account; }

  public String getHostName() { return hostName; }
  public void   setHostName( String hostName ) { 
    if ( null != hostName ) {
      hostName = hostName.trim();
    }
    this.hostName = hostName;
  } 

  public int  getPort() { return port; }
  public void setPort( int port ) { this.port = port; }

  public SSLCertificate getCurrentCertificate() { 
    if ( null == currentCertificate ) { 
      Socket socket = getFTPBean().getControlSocket();
      if ( socket instanceof SSLSocket ) {
        javax.net.ssl.SSLSession session = ((SSLSocket)socket).getSession();
        try {
          java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate)session.getPeerCertificates()[0];
          currentCertificate = new SSLCertificate( cert );
        }
        catch ( Exception e ) { 
          currentCertificate = new SSLCertificate( null );
        }
      }
    }
    return currentCertificate; 
  }
  public void setCurrentCertificate(SSLCertificate cert) { 
    currentCertificate = cert; 
  }
  public File getKeyStoreFile() { return keyStoreFile; }
  public void initKeyStoreFile() {
    String home = (String) System.getProperty("user.home") + 
                   File.separator + ".secureftp";

    File dir = new File(home);

    if (! dir.isAbsolute())
      dir = new File( (String) System.getProperty("user.dir") +
                      File.separator + home );

    if (! dir.isDirectory()) {
      if (! dir.mkdirs()) {
        return;
      }
    }

    keyStoreFile = new File( dir, "cert.key" );
  } 

  public File getLocalDir() { return localDir; }
  public void setLocalDir( File localDir ) throws FileNotFoundException {
    if ( null == localDir ) {
      // it's ok, just disable downloading
    }
    else if ( !localDir.isDirectory() )
      throw new FileNotFoundException(localDir + " is not a directory");
    else if ( !localDir.exists() )
      throw new FileNotFoundException(localDir + " does not exist");

    this.localDir = localDir;
  }
  public void   setLocalDir( String localDirStr ) throws FileNotFoundException {
    setLocalDir( new File(localDirStr) );
  }

  public String getWorkingDir() { return pwd; }
  public void setWorkingDir( String workingDir ) { pwd = workingDir; }

  public boolean isInteractiveOn() { return interactiveOn; }
  public void    setInteractiveOn( boolean interactiveOn ) { 
    this.interactiveOn = interactiveOn; 
  }

  public boolean isGlobOn() { return globOn; }
  public void    setGlobOn( boolean globOn ) { 
    this.globOn = globOn; 
  }

  public boolean getBeepWhenDone() { return beepWhenDone; }
  public void    setBeepWhenDone( boolean beepWhenDone ) { 
    this.beepWhenDone = beepWhenDone; 
  }

  public boolean isDebugOn() { return debugOn; }
  public void    setDebugOn( boolean debugOn ) { this.debugOn = debugOn; }

  public boolean isConnected() { 
    boolean isConnected = false;
    if ( getFTPBean() != null ) {
      isConnected = getFTPBean().isConnected();
    }

    if ( !isConnected ) {
      //setIsSecure(false);
      clearSession();
    }

    return isConnected; 
  }

  public boolean isLoggedIn() { 
    boolean isLoggedIn = false;
    if ( getFTPBean() != null ) {
      isLoggedIn = getFTPBean().isLoggedIn();
    }
    return isLoggedIn; 
  }

  public boolean showProgress() { return showProgress; }
  public void    setShowProgress( boolean showProgress ) { 
    this.showProgress = showProgress; 
  }

  public boolean reportVerbose() { return reportVerbose; }
  public void    setReportVerbose( boolean reportVerbose ) { 
    this.reportVerbose = reportVerbose; 
  }

  public boolean isSecure() { return isSecure; }
  public void    setIsSecure( boolean isSecure ) { 
    this.isSecure = isSecure; 
  }

  public short getSecurityMode() { return securityMode; }
  public void setSecurityMode( short securityMode ) { 
    this.securityMode = securityMode; 
  }

  public boolean canRestartTransfer() { return canRestartTransfer; }
  public void setTransferRestartable( boolean canRestart ) { 
    canRestartTransfer = canRestart;
  }

  public OutputStream getOutputStream() { return outputStream; }
  public void setOutputStream( OutputStream outputStream ) {
    this.outputStream = outputStream;
  }

  public PrintStream getPrintStream() {
    PrintStream ps = null;
    if ( System.out == getOutputStream() || !reportVerbose() ) {
      ps = System.out;
    }
    else {
      ps = new PrintStream( getOutputStream() );
    }

    return ps;
  }

  public boolean maskPass() { return maskPass; }
  public void setMaskPass( boolean maskPass ) { this.maskPass = maskPass; }

  public FTP getFTPBean() { return ftpBean; }
  public void setFTPBean( FTP ftpBean ) {
    this.ftpBean = ftpBean;
  }

  public SSLCertificateHandler getCertHandler() {
    return certHandler;
  }
  
  public void setCertHandler( SSLCertificateHandler certHandler ) {
    this.certHandler = certHandler;
  }

  public Container getRemoteUI() { return remoteUI; }
  public void setRemoteUI( Container remoteUI ) { this.remoteUI = remoteUI; }

  public boolean supportsMDTM() { return supportsMDTM; }
  public void setSupportsMDTM( boolean supports ) { supportsMDTM = supports; }

  public boolean supportsSIZE() { return supportsSIZE; }
  public void setSupportsSIZE( boolean supports ) { supportsSIZE = supports; }

  public boolean testForREST() { return testForREST; }
  public void setTestForREST( boolean test ) { testForREST = test; }

  public boolean supportsREST() { return supportsREST; }
  public void setSupportsREST( boolean supports ) { supportsREST = supports; }

  public boolean usesProxy() { return useProxy; }
  public void setUseProxy( boolean useProxy ) { this.useProxy = useProxy; }

  public boolean useCCC() { return useCCC; }
  public void setUseCCC( boolean useCCC ) { this.useCCC = useCCC; }

  public FTPAbortableTransfer getAbortableListTransfer() { return listAbort; }
  public void setAbortableListTransfer( FTPAbortableTransfer abort ) {
    listAbort = abort;
  }
}
