
//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: Bookmark.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

public class Bookmark {
  private String profile = null;
  private String hostname = null;
  private int port = Constants.DEF_EXPLICIT_SSL_PORT;
  private String username = null;
  private String password = null;
  private int securityMode = FTPSession.EXPLICIT_SSL;
  private boolean anonymous = false;
  private boolean passive = true;
  private boolean dataEncrypt = true;
  private boolean ccc = false;
  private boolean proxy = false;
  private String remoteFolder = null;
  private String localFolder = null;

  public Bookmark() {
    this( null, null, Constants.DEF_EXPLICIT_SSL_PORT,
          System.getProperty("user.name"), null,
          ConnectionDialog.EXPLICIT_SSL, false, true, true, 
          false, false, null, null );
  }

  public Bookmark( String profile, String hostname, int port,
                   String username, String password, int securityMode,
                   boolean anonymous, boolean passive, boolean dataEncrypt,
                   boolean ccc, boolean proxy, String remoteFolder, 
                   String localFolder ) {
    setProfile( profile );
    setHostName( hostname );
    setPort( port );
    setUserName( username );
    setPassword( password );
    setSecurityMode( securityMode );
    setAnonymous( anonymous );
    setPassiveConnection( passive );
    setDataEncrypt( dataEncrypt );
    setCCCEnabled( ccc );
    setProxy( proxy );
    setRemoteFolder( remoteFolder );
    setLocalFolder( localFolder );
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append( "Profile => " + profile );
    buffer.append( ", Hostname => " + hostname );
    buffer.append( ", Port => " + port );
    buffer.append( ", Username => " + username );
    buffer.append( ", Password => **********" );
    buffer.append( ", Security Mode => " + securityMode );
    buffer.append( ", Anonymous => " + anonymous );
    buffer.append( ", Passive Connection => " + passive );
    buffer.append( ", Data Encrypt => " + dataEncrypt );
    buffer.append( ", CCC => " + ccc );
    buffer.append( ", Use Proxy => " + proxy );
    buffer.append( ", Remote Folder => " + remoteFolder );
    buffer.append( ", Local Folder => " + localFolder );
    return buffer.toString();
  }

  public String getProfile() { 
    if ( profile == null || profile.trim().length() == 0 ) {
      profile = hostname;
    }

    return profile; 
  }
  public void setProfile( String profile ) { this.profile = profile; }

  public String getHostName() { return hostname; }
  public void setHostName( String hostname ) { this.hostname = hostname; }

  public int getPort() { return port; }
  public void setPort( int port ) { this.port = port; }

  public String getUserName() { return username; }
  public void setUserName( String username ) { this.username = username; }

  public String getPassword() { return password; }
  public void setPassword( String password ) { this.password = password; }

  public String getEncryptedPassword() { 
    String result = null;
    if ( getPassword() != null && getPassword().trim().length() > 0 ) {
      result = encryptPassword( getPassword() );
    }
    return result;
  }

  public static String encryptPassword( String plainPass ) {
    return EncryptionUtils.encryptPassword( plainPass );
  }

  public void setEncryptedPassword( String encryptedPassword ) { 
    password = decryptPassword( encryptedPassword );
  }

  public static String decryptPassword( String encryptedPassword ) {
    return EncryptionUtils.decryptPassword( encryptedPassword );
  }

  public int getSecurityMode() { return securityMode; }
  public void setSecurityMode( int securityMode ) { 
    this.securityMode = securityMode; 
  }

  public boolean isAnonymous() { return anonymous; }
  public void setAnonymous( boolean anonymous ) { this.anonymous = anonymous; }

  public boolean isPassiveConnection() { return passive; }
  public void setPassiveConnection( boolean passive ) { 
    this.passive = passive; 
  }

  public boolean isDataEncrypted() { 
    boolean result = dataEncrypt;
    
    if ( FTPSession.NO_SECURITY == getSecurityMode() ) {
      result = false;
    }

    return result; 
  }

  public void setDataEncrypt( boolean dataEncrypt ) { 
    this.dataEncrypt = dataEncrypt; 
  }

  public boolean isCCCEnabled() { 
    boolean result = ccc;
    
    if ( FTPSession.NO_SECURITY == getSecurityMode() ) {
      result = false;
    }

    return result; 
  }

  public void setCCCEnabled( boolean enable ) { 
    this.ccc = enable; 
  }

  public String getRemoteFolder() { return remoteFolder; }
  public void setRemoteFolder( String remoteFolder ) { 
    this.remoteFolder = remoteFolder; 
  }

  public String getLocalFolder() { return localFolder; }
  public void setLocalFolder( String localFolder ) { 
    this.localFolder = localFolder; 
  }

  public boolean usesProxy() { return proxy; }
  public void setProxy( boolean proxy ) { this.proxy = proxy; }
}

