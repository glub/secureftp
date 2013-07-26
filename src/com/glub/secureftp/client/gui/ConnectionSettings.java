
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: ConnectionSettings.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

public class ConnectionSettings {
  private boolean dataEncryptionOn = true;
  private String startingRemoteFolder = null;
  private String startingLocalFolder = null;
  private boolean passiveConnection = true;
  private boolean anonymous = false;
  private boolean saveBookmark = false;
  private int securityMode = ConnectionDialog.EXPLICIT_SSL;

  public ConnectionSettings() {
    setDataEncryptionOn( true );
    setStartingRemoteFolder( null );
    setStartingLocalFolder( null );
    setPassiveConnection( true );
    setAnonymous( false );
    setSaveBookmark( false );
    setSecurityMode( ConnectionDialog.EXPLICIT_SSL );
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append( "Data Encryption On => " + dataEncryptionOn );
    buffer.append( ", Starting Remote Folder => " + startingRemoteFolder );
    buffer.append( ", Starting Local Folder => " + startingLocalFolder );
    buffer.append( ", Passive Connection => " + passiveConnection );
    buffer.append( ", Anonymous => " + anonymous );
    buffer.append( ", Save Bookmark => " + saveBookmark );
    buffer.append( ", Security Mode => " + securityMode );
    return buffer.toString();
  }

  public boolean isDataEncryptionOn() { return dataEncryptionOn; }
  public void setDataEncryptionOn( boolean on ) {
    dataEncryptionOn = on;
  }

  public String getStartingRemoteFolder() { return startingRemoteFolder; }
  public void setStartingRemoteFolder( String folder ) {
    startingRemoteFolder = folder;
  }

  public String getStartingLocalFolder() { return startingLocalFolder; }
  public void setStartingLocalFolder( String folder ) {
    startingLocalFolder = folder;
  }

  public boolean isPassiveConnection() { return passiveConnection; }
  public void setPassiveConnection( boolean passive ) {
    passiveConnection = passive;
  }

  public boolean isAnonymous() { return anonymous; }
  public void setAnonymous( boolean anonymous ) {
    this.anonymous = anonymous;
  }

  public boolean saveBookmark() { return saveBookmark; }
  public void setSaveBookmark( boolean save ) {
    saveBookmark = save;
  }

  public int getSecurityMode() { return securityMode; }
  public void setSecurityMode( int securityMode ) {
    this.securityMode = securityMode;
  }
}

