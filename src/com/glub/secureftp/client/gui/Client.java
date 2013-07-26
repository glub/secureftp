//*****************************************************************************
//*
//* (c) Copyright 2006. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: Client.java 142 2009-12-16 04:23:16Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.prefs.Preferences;
import javax.help.*;

public class Client implements PreferenceHandler {
  public static final short APPLICATION = 1;
  public static final short APPLET = 2;

  private static short clientType = APPLICATION;

  private static String encoding = null;

  protected final static long serialVersionUID = 1L;
  private Preferences prefs = PreferencesDispatcher.getPreferences(this);

  protected static SecureFTPApplet applet = null;

  private static Preferences globalPrefs = null;
  private static Menus menuBar = null;
  private static ToolBar toolBar = null;
  private static BaseView baseView = null;
  private static RemoteView serverView = null;
  private static LocalView localView = null;

//private static final HelpViewerThread hvt = new HelpViewerThread();
  private static HelpViewerThread hvt = null;
  private static CSH.DisplayHelpFromSource help = null;

  private static URL globalBookmarksURL = null;

  private static boolean showHiddenFiles = false;

  private static boolean useModeZCompression = false;

  private static boolean useProxy = false;
  private static String socksHostName = "";
  private static int socksPort = 1080;
  private static String socksUserName = "";
  private static String socksPassword = "";
  private static boolean startWithOpenDialog = true;
  private static boolean autoCheckForUpdate = true;
  private static boolean showCloseTabWarning = true;
  private static boolean forcePasvControlIP = false;
  private static String regSerial = "";
  private static File clientPrivateKey = null;
  private static File clientPublicCert = null;
  private static File clientCACert = null;
  private static String clientCertPassword = null;
  private static int transferMode = 0;
  private static int lastConnectionIndex = 0;
  private static boolean showFullColumnListing = false;
  private static boolean allowRaw = true;
  private static boolean allowDownload = true;
  private static boolean allowUpload = true;

  public static final String PROGRAM_NAME = "Secure FTP " + 
                                            Version.SHORT_VERSION;

  private static final String SHOW_HIDDEN_FILES    = "ShowHiddenFiles";
  private static final String GLOBAL_BOOKMARKS_URL = "GlobalBookmarksURL";
  //private static final String USE_PROXY            = "UseProxy";
  private static final String SOCKS_HOSTNAME       = "SocksHostName";
  private static final String SOCKS_PORT           = "SocksPort";
  private static final String SOCKS_USERNAME       = "SocksUserName";
  private static final String SOCKS_PASSWORD       = "SocksPassword";
  private static final String START_OPEN_DIALOG    = "StartWithOpenDialog";
  private static final String AUTO_CHECK_UPDATE    = "AutoCheckForUpdate";
  private static final String CLOSE_TAB_WARNING    = "CloseTabWarning";
  private static final String FORCE_PASV_CONTROL_IP= "ForcePasvControlIP";
  private static final String USE_MODE_Z_COMPRESSION = "UseModeZCompression";
  private static final String CLIENT_PRIVATE_KEY   = "ClientPrivateKey";
  private static final String CLIENT_PUBLIC_CERT   = "ClientPublicCert";
  private static final String CLIENT_CA_CERT       = "ClientCACert";
  private static final String CLIENT_CERT_PASSWORD = "ClientCertPassword";
  private static final String TRANSFER_MODE        = "TransferMode";
  private static final String FULL_COLUMN_LISTING  = "FullColumnListing";
  private static final String LAST_CONNECTION_INDEX = "LastConnectionIndex";
  private static Calendar demoTimeout              = null;
  private static boolean suppressOpenDialog	   = false;

  public Client( short type ) {
    this( type, null );
  }

  public Client( short type, String encoding ) {
    clientType = type;
    this.encoding = encoding;

    if ( encoding != null && encoding.trim().length() > 0 ) {
      System.setProperty("file.encoding", encoding);
    }
  }

  public void init( Object parentType ) {
    if ( parentType instanceof SecureFTPApplet ) {
      applet = (SecureFTPApplet)parentType;
    }

    globalPrefs = getGlobalPrefs();

    // menus
    if ( clientType == APPLICATION ) {
      System.setProperty( "apple.laf.useScreenMenuBar", "true" );
    }

    menuBar = new Menus();
    
    toolBar = new ToolBar();

    // server tabbed pane
    serverView = new RemoteView();

    // local pane
    localView = new LocalView();

    // preload the help viewer
    hvt = new HelpViewerThread();
    hvt.start();

    // base split pane
    baseView = new BaseView( localView, serverView );

    (new KeepAliveThread()).start();
  }

  public static Calendar getDemoTimeout() {
    return demoTimeout;
  }

  public void setDemoTimeout( Calendar timeToDie ) {
    demoTimeout = timeToDie;
  }

  public static short getClientType() {
    return clientType;
  }

  public static String getEncoding() {
    return encoding;
  }

  public static Menus getMenus() {
    return menuBar;
  }

  public static ToolBar getToolBar() {
    return toolBar;
  }

  public static LocalView getLocalView() {
    return localView;
  }

  public static RemoteView getRemoteView() {
    return serverView;
  }

  public static BaseView getBaseView() { return baseView; }

  public static boolean showHiddenFiles() { return showHiddenFiles; }
  public static void setShowHiddenFiles( boolean show ) { 
    showHiddenFiles = show; 
  }

  public static boolean showFullColumnListing() { 
    return showFullColumnListing; 
  }
  public static void setShowFullColumnListing( boolean show ) { 
    showFullColumnListing = show; 
  }

  public static boolean getAllowRaw() { 
    return allowRaw; 
  }
  public static void setAllowRaw( boolean allow ) { 
    allowRaw = allow; 
  }

  public static boolean getAllowDownload() { 
    return allowDownload; 
  }
  public static void setAllowDownload( boolean allow ) { 
    allowDownload = allow; 
  }

  public static boolean getAllowUpload() { 
    return allowUpload; 
  }
  public static void setAllowUpload( boolean allow ) { 
    allowUpload = allow; 
  }

  public static boolean useModeZCompression() { return useModeZCompression; }
  public static void setUseModeZCompression( boolean use ) {
    useModeZCompression = use;
  }

  public static int getTransferMode() { return transferMode; }
  public static void setTransferMode( int mode ) { 
    transferMode = mode; 
    if ( getMenus() != null ) {
      getMenus().updateMenuBar();   
    }
  }

  public static int getLastConnectionIndex() { return lastConnectionIndex; }
  public static void setLastConnectionIndex( int index ) {
    lastConnectionIndex = index;
  }

  public static File getClientPrivateKey() { return clientPrivateKey; }
  public static void setClientPrivateKey( File f ) { clientPrivateKey = f; }

  public static File getClientPublicCert() { return clientPublicCert; }
  public static void setClientPublicCert( File f ) { clientPublicCert = f; }

  public static File getClientCACert() { return clientCACert; }
  public static void setClientCACert( File f ) { clientCACert = f; }

  public static String getClientCertPassword() { return clientCertPassword; }
  public static void setClientCertPassword( String p ) { clientCertPassword = p; }

  public static boolean startWithOpenDialog() { return startWithOpenDialog; }
  public static void setStartWithOpenDialog( boolean start ) { 
    startWithOpenDialog = start; 
  }

  public static boolean autoCheckForUpdate() { return autoCheckForUpdate; }
  public static void setAutoCheckForUpdate( boolean check ) { 
    autoCheckForUpdate = check; 
  }

  public static boolean showCloseTabWarning() { return showCloseTabWarning; }
  public static void setCloseTabWarning( boolean warn ) { 
    showCloseTabWarning = warn; 
  }

  public static boolean forcePasvControlIP() { return forcePasvControlIP; }
  public static void setForcePasvControlIP( boolean force ) { 
    forcePasvControlIP = force; 
  }

  public static URL getGlobalBookmarksURL() {
    return globalBookmarksURL;
  }
  public static void setGlobalBookmarksURL( String url ) {
    if ( url.length() > 0 ) {
      try {
        globalBookmarksURL = new URL( url );
      }
      catch( MalformedURLException murle ) {}
    } 
    else
      globalBookmarksURL = null;
  }

  public static boolean proxySet() {
    return useProxy;
  }
  public static void setUseProxy( boolean use ) { useProxy = use; }

  public static String getSocksHostName() {
    return socksHostName;
  }
  public static void setSocksHostName( String hostName ) { 
    socksHostName = hostName; 
  }
 
  public static int getSocksPort() {
    return socksPort;
  }
  public static void setSocksPort( int port ) { 
    socksPort = port; 
  }
 
  public static String getSocksUserName() {
    return socksUserName;
  }
  public static void setSocksUserName( String userName ) { 
    socksUserName = userName; 
  }
 
  public static String getSocksPassword() {
    return socksPassword;
  }
  public static void setSocksPassword( String password ) { 
    socksPassword = password; 
  }
 
  private void initBookmarks() {
    getMenus().updateBookmarks();
  }

  public static void showHelpViewer( ActionEvent e ) {
    try {
      if (hvt != null) {
        hvt.join();
        help = new CSH.DisplayHelpFromSource(hvt.getHelpBroker());
	//hvt.getHelpBroker().setDisplayed(true);
        hvt = null;
      }

      help.actionPerformed(e);
    }
    catch ( InterruptedException ie ) {}
  }

  public void readPreferences() {
    String globalBook = prefs.get(GLOBAL_BOOKMARKS_URL, "");
    String overrideURL = GTOverride.getString("glub.globalbookurl");
    if ( null != overrideURL && overrideURL.trim().length() > 0 ) {
      globalBook = overrideURL.trim();
    }
    setGlobalBookmarksURL( globalBook );

    if ( clientType == APPLET ) {
      applet.readPreferences();
    }
    else {
      setSocksHostName( prefs.get(SOCKS_HOSTNAME, "") );
      setSocksPort( prefs.getInt(SOCKS_PORT, 1080) );
      setSocksUserName( prefs.get(SOCKS_USERNAME, "") );
      String encPass = prefs.get(SOCKS_PASSWORD, "");
      if ( encPass.trim().length() > 0 ) {
        setSocksPassword( Bookmark.decryptPassword(encPass) );
      }
      else { 
        setSocksPassword( "" );
      }

      setShowHiddenFiles( prefs.getBoolean(SHOW_HIDDEN_FILES, false) );
      setShowFullColumnListing( prefs.getBoolean(FULL_COLUMN_LISTING, false) );
      setUseModeZCompression( prefs.getBoolean(USE_MODE_Z_COMPRESSION, true) );
      setStartWithOpenDialog( prefs.getBoolean(START_OPEN_DIALOG, true) );
      setAutoCheckForUpdate( prefs.getBoolean(AUTO_CHECK_UPDATE, true) );
      setCloseTabWarning( prefs.getBoolean(CLOSE_TAB_WARNING, true) );
      setForcePasvControlIP( prefs.getBoolean(FORCE_PASV_CONTROL_IP, false) );
      setTransferMode( prefs.getInt(TRANSFER_MODE, FTP.AUTO_TRANSFER_MODE) );
      setLastConnectionIndex( prefs.getInt(LAST_CONNECTION_INDEX, 0) );
    }

    String cpk = prefs.get(CLIENT_PRIVATE_KEY, "");
    if ( cpk.length() > 0 ) {
      setClientPrivateKey( new File(cpk) );
    }
    else {
      setClientPrivateKey( null );
    }

    String cpc = prefs.get(CLIENT_PUBLIC_CERT, "");
    if ( cpc.length() > 0 ) {
      setClientPublicCert( new File(cpc) );
    }
    else {
      setClientPublicCert( null );
    }

    String cca = prefs.get(CLIENT_CA_CERT, "");
    if ( cca.length() > 0 ) {
      setClientCACert( new File(cca) );
    }
    else {
      setClientCACert( null );
    }

    String decryptedPass = 
      Bookmark.decryptPassword( prefs.get(CLIENT_CERT_PASSWORD, "") );
    setClientCertPassword( decryptedPass );

    initBookmarks();
  }

  public void writePreferences() {
    if ( SecureFTP.debug ) {
      System.err.println("Writing prefs");
    }

    if ( clientType != APPLET ) {
      String urlPath = "";
      if ( null != getGlobalBookmarksURL() ) {
        urlPath = getGlobalBookmarksURL().toExternalForm();
      }
      else
        urlPath = "";

      prefs.put( GLOBAL_BOOKMARKS_URL, urlPath );

      prefs.put( SOCKS_HOSTNAME, getSocksHostName() );
      prefs.putInt( SOCKS_PORT, getSocksPort() );
      prefs.put( SOCKS_USERNAME, getSocksUserName() );
      if ( getSocksPassword().length() > 0 ) {
        prefs.put( SOCKS_PASSWORD, 
                   Bookmark.encryptPassword(getSocksPassword()) );
      }
      else {
        prefs.put( SOCKS_PASSWORD, "" );
      }

      prefs.putBoolean( SHOW_HIDDEN_FILES, showHiddenFiles() );
      prefs.putBoolean( FULL_COLUMN_LISTING, showFullColumnListing() );
      prefs.putBoolean( USE_MODE_Z_COMPRESSION, useModeZCompression() );
      prefs.putBoolean( START_OPEN_DIALOG, startWithOpenDialog() );
      prefs.putBoolean( AUTO_CHECK_UPDATE, autoCheckForUpdate() );
      prefs.putBoolean( CLOSE_TAB_WARNING, showCloseTabWarning() );
      prefs.putBoolean( FORCE_PASV_CONTROL_IP, forcePasvControlIP() );
    }

    if ( null != clientPrivateKey && clientPrivateKey.length() > 0 ) {
      prefs.put( CLIENT_PRIVATE_KEY, clientPrivateKey.getAbsolutePath() );
    }
    else {
      prefs.put( CLIENT_PRIVATE_KEY, "" );
    }

    if ( null != clientPublicCert && clientPublicCert.length() > 0 ) {
      prefs.put( CLIENT_PUBLIC_CERT, clientPublicCert.getAbsolutePath() );
    }
    else {
      prefs.put( CLIENT_PUBLIC_CERT, "" );
    }

    if ( null != clientCACert && clientCACert.length() > 0 ) {
      prefs.put( CLIENT_CA_CERT, clientCACert.getAbsolutePath() );
    }
    else {
      prefs.put( CLIENT_CA_CERT, "" );
    }

    String encryptedPass = "";
    if ( null != clientCertPassword && clientCertPassword.length() > 0 ) {
      encryptedPass = Bookmark.encryptPassword( clientCertPassword );
    }
    prefs.put( CLIENT_CERT_PASSWORD, encryptedPass );

    prefs.putInt( TRANSFER_MODE, getTransferMode() );

    prefs.putInt( LAST_CONNECTION_INDEX, getLastConnectionIndex() );
  }

  private static Preferences getGlobalPrefs() {
    Preferences prefs = null;
    try {
      prefs = Preferences.systemNodeForPackage(
                Class.forName("com.glub.secureftp.client.gui.SecureFTP"));
    }
    catch ( Exception e ) { 
      e.printStackTrace(); 
    }
    return prefs;
  }

  public static void fixComponentColor( Component c ) {
    Color backgroundColor = (Color)c.getBackground();
    if ( null == backgroundColor )
      return;
    Color firefoxColor = new Color( 238, 238, 238 );
    if ( clientType == APPLET && backgroundColor.getRGB() == firefoxColor.getRGB() ) {
      c.setBackground( Color.LIGHT_GRAY );
    }
  }

  public static void setSuppressOpenDialog( boolean suppress ) {
    suppressOpenDialog = suppress;
  }

  public static boolean getSuppressOpenDialog() { return suppressOpenDialog; }
}

class KeepAliveThread extends Thread {
  private static final int FIVE_SECONDS_IN_MILLIS = 5 * 1000;
  private int keepAliveMS = 60 * 1000;
  private boolean runThread = true;

  public KeepAliveThread() {
    super();

    int tempKeepAlive = GTOverride.getInt( "keepalive.seconds", 60 );

    // if tempKeepAlive is less than 5 seconds, it's not valid
    if ( tempKeepAlive < 5 ) {
      runThread = false;
    }
    else {
      keepAliveMS = tempKeepAlive * 1000;
    }
  }

  public void run() {
    while( runThread ) {
      try {
        // sleep 5 seconds
        sleep( FIVE_SECONDS_IN_MILLIS );
      }
      catch ( Exception e ) {}

      long currentTime = System.currentTimeMillis();

      int numOfSessions = FTPSessionManager.getInstance().getNumberOfOpenSessions();
      for( int i = 0; i < numOfSessions; i++ ) {
        FTPSession session = FTPSessionManager.getInstance().getSession( i );
        if ( session != null ) {
          if ( ! session.isTransferringData() && session.getLastCommandSent() + (keepAliveMS) <= currentTime && session.getFTPBean().isConnected() ) {
            try {
              session.getFTPBean().pwd();
              session.setLastCommandSent( currentTime );
            }
            catch ( FTPConnectionLostException fcle ) {
              // connection lost
            }
            catch ( Exception e ) { e.printStackTrace(); }
          }
        }
      }
    }
  }
}
