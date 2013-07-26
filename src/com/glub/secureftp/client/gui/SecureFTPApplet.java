//*****************************************************************************
//*
//* (c) Copyright 2006. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: SecureFTPApplet.java 141 2009-12-16 03:53:52Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.secureftp.bean.*;
import com.glub.gui.*;
import com.glub.net.*;
import com.glub.util.*;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class SecureFTPApplet extends JApplet implements PreferenceHandler {
  protected final static long serialVersionUID = 1L;
  protected static Client client = null;
  private boolean showBorder = false;
  private Bookmark autoConnectBookmark = null;
  private boolean validated = false;

  public void init() {
    try {
      UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
    }
    catch ( Exception e ) {}

    System.setProperty( "glub.resourceBundle",
                        "com.glub.secureftp.client.resources.strings" );

    SecureFTP.setAppletInstance( this );

    String encoding = getParameter("encoding");
    if (encoding != null && encoding.toLowerCase().trim().length() == 0) {
      encoding = null;
    }

    client = new Client( Client.APPLET, encoding );

    String debug = getParameter("debug");
    if (debug != null && debug.toLowerCase().trim().equals("on")) {
      System.err.println("debug on");
      SecureFTP.debug = true;
    }

    SplashScreen splash = new SplashScreen();

    SecureFTP.setCommandDispatcher(new CommandDispatcher());

    SecureFTP.getCommandDispatcher().addListener( UIRefresh.class, 
                                                  new UIRefresh() );
    SecureFTP.getCommandDispatcher().addListener( CommandPlayer.class, 
                                                  new CommandPlayer() );

    getContentPane().setLayout( new BorderLayout(1, 1) );

    String border = getParameter("border");
    if (border != null && border.toLowerCase().trim().equals("on")) {
      showBorder = true;
      setBackground(Color.LIGHT_GRAY);
    }
    else {
      Client.fixComponentColor( this );
    }

    String keepAliveStr = getParameter("keepalive");
    int keepAlive = 0;
    if (keepAliveStr != null) {
      keepAlive = Util.parseInt( keepAliveStr, 0 );
    }
    System.setProperty( "keepalive.seconds", keepAlive + "" );
 
    String allowRawStr = getParameter("allow_raw");
    boolean allowRaw = true;
    if ( allowRawStr != null && 
         allowRawStr.toLowerCase().trim().equals("false") ) {
      allowRaw = false;
    }
    Client.setAllowRaw( allowRaw );

    String allowUploadStr = getParameter("allow_upload");
    boolean allowUpload = true;
    if ( allowUploadStr != null && 
         allowUploadStr.toLowerCase().trim().equals("false") ) {
      allowUpload = false;
    }
    Client.setAllowUpload( allowUpload );

    String allowDownloadStr = getParameter("allow_download");
    boolean allowDownload = true;
    if ( allowDownloadStr != null && 
         allowDownloadStr.toLowerCase().trim().equals("false") ) {
      allowDownload = false;
    }
    Client.setAllowDownload( allowDownload );

    String globalBook = getParameter("globalbookurl");
    if (globalBook != null) {
      System.setProperty("glub.globalbookurl", globalBook.trim());
    }

    String proxyHost = getParameter("proxy_host");
    if (proxyHost != null) {
       Client.setSocksHostName(proxyHost.trim());

       String proxyPortStr = getParameter("proxy_port");
       int proxyPort = 1080;
       if (proxyPortStr != null) {
          proxyPort = Util.parseInt(proxyPortStr.trim(), 1080);
       }

       Client.setSocksPort(proxyPort);
       String proxyUser = getParameter("proxy_user");
       if (proxyUser != null) {
         Client.setSocksUserName(proxyUser.trim());
       }

       String proxyPass = getParameter("proxy_pass");
       if (proxyPass != null) {
         Client.setSocksPassword(Bookmark.decryptPassword(proxyPass.trim()));
       }

       Client.setUseProxy(proxyHost.trim().length() > 0 &&
                          proxyPort > 0);
    }

    String initLocalDir = getParameter("initlocaldir");
    if (initLocalDir == null) {
      initLocalDir = System.getProperty("user.home");
    }
    else if (initLocalDir.trim().startsWith("~")) {
      initLocalDir = Util.searchAndReplace(initLocalDir, "~", 
                                     System.getProperty("user.home"), false);

    }

    System.setProperty("user.dir", initLocalDir);
    
    client.init( this );

    // menus
    setJMenuBar( Client.getMenus() );
  
    // toolbar
    JPanel toolPanel = new JPanel() {
      protected void paintComponent( Graphics g ) {
        Graphics2D g2d = (Graphics2D)g;
        int w = getWidth();
        int h = getHeight();

        GradientPaint gp =
          new GradientPaint( 0, 0, new Color( 222, 222, 222 ),
                             0, h, new Color( 160, 160, 160) );
        g2d.setPaint( gp );
        g2d.fillRect( 0, 0, w, h );

        setOpaque( false );
        super.paintComponent( g );
        setOpaque( true );
      }
    };
    toolPanel.setLayout( new BoxLayout( toolPanel, BoxLayout.LINE_AXIS ) );
    toolPanel.add( Client.getToolBar() );
    toolPanel.add( Box.createHorizontalGlue() );

    ImageIcon logoIcon =
      new ImageIcon(getClass().getResource("images/LOGO.png"));
    JLabel logo = new JLabel( logoIcon );

    logo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    logo.addMouseListener( new MouseAdapter() {
      public void mouseClicked( MouseEvent e ) {
        if ( e.getButton() == MouseEvent.BUTTON1 )
          Util.openURL( "http://www.glub.com" );
        }
      } );

    logo.setBorder( BorderFactory.createEmptyBorder(0, 15, 0, 5) );
    toolPanel.add( logo );

    getContentPane().add( toolPanel, BorderLayout.NORTH );

    // base split pane
    getContentPane().add( Client.getBaseView(), BorderLayout.CENTER );

    // status bar
    JPanel statusPanel = new JPanel();
    Client.fixComponentColor( statusPanel );
    statusPanel.setLayout( new BorderLayout() );

    statusPanel.add( Box.createVerticalStrut(12), BorderLayout.WEST );

    getContentPane().add( statusPanel, BorderLayout.SOUTH );

    PreferencesDispatcher.doReadPrefs();

    splash.dispose();

    SSLFTP.preSeed();
 
    setFocusable( true );
    (new StatusThread(this)).start();

    if ( validated ) {
      Bookmark autoConnect = getAutoConnectBookmark();
      if ( autoConnect != null ) {
        OpenCommand ocd = new OpenCommand();
        ArrayList args = new ArrayList(1);
        args.add(autoConnect);
        ocd.setArgs(args);
        SecureFTP.getCommandDispatcher().fireMTCommand( SecureFTPApplet.class,
                                                        ocd,
                                                        Thread.MIN_PRIORITY, 
                                                        250, 
                                                        new SecureFTPError(), 
                                                        false );
      }
      else if ( Client.startWithOpenDialog() ) {
        SecureFTP.getCommandDispatcher().fireMTCommand( SecureFTPApplet.class,
                                                        new OpenCommand(),
                                                        Thread.MIN_PRIORITY, 
                                                        250, 
                                                        new SecureFTPError(), 
                                                        false );
      }
    }
  }

  public void stop() {
    while( FTPSessionManager.getInstance().hasOpenSessions() ) {
      SecureFTP.getCommandDispatcher().fireCommand( this, new CloseCommand() );
    }
    PreferencesDispatcher.doWritePrefs();
    SecureFTP.setAppletInstance( null );
    autoConnectBookmark = null;
    client.setDemoTimeout( null );
    super.stop();
  }

  public Bookmark getAutoConnectBookmark() {
    if (autoConnectBookmark == null) {
      String host = getParameter("hostname");
      if ( host != null ) {
        autoConnectBookmark = new Bookmark();

        autoConnectBookmark.setHostName(host);

        String user = getParameter("username");
        if ( user != null )
          autoConnectBookmark.setUserName(user);
      
        String pass = getParameter("password");
        if ( pass != null )
          autoConnectBookmark.setEncryptedPassword(pass);

        int defPort = Constants.DEF_EXPLICIT_SSL_PORT;
        String port = getParameter("port");
        boolean portSet = true;
        if ( port == null ) {
          port = "";
          portSet = false;
        }
 
        String secModeStr = getParameter("security");
        if ( secModeStr == null ) {
          secModeStr = "explicit_ssl";
        }
        else {
          secModeStr = secModeStr.toLowerCase().trim();
        }

        short secMode = FTPSession.EXPLICIT_SSL;
        if ( secModeStr.equals("none") ) {
          secMode = FTPSession.NO_SECURITY;
          if (!portSet)
            port = Constants.DEF_EXPLICIT_SSL_PORT + "";
        }
        else if ( secModeStr.equals("implicit_ssl") ) {
          secMode = FTPSession.IMPLICIT_SSL;
          if (!portSet)
            port = Constants.DEF_IMPLICIT_SSL_PORT + "";
        }

        autoConnectBookmark.setSecurityMode( secMode );

        autoConnectBookmark.setPort( Util.parseInt(port, defPort) );

        autoConnectBookmark.setDataEncrypt( getBooleanParameter("ssldata", true) );

        autoConnectBookmark.setCCCEnabled( getBooleanParameter("ccc", false) );

        String localDir = getParameter("localdir");
        if ( localDir != null ) {
          if (localDir.trim().startsWith("~")) {
            localDir = Util.searchAndReplace(localDir, "~", 
                                       System.getProperty("user.home"), false);
          }
          autoConnectBookmark.setLocalFolder( localDir );
        }

        String remoteDir = getParameter("remotedir");
        if ( remoteDir != null ) {
          autoConnectBookmark.setRemoteFolder( remoteDir );
        }

        autoConnectBookmark.setPassiveConnection( getBooleanParameter("pasv", true) );

        String anonStr = getParameter("anon");
        boolean anon = false;
        if ( anonStr != null && anonStr.toLowerCase().trim().equals("true") ) {
          anon = true;
        }
        autoConnectBookmark.setAnonymous( anon );

        autoConnectBookmark.setProxy( getBooleanParameter("proxy", false) );
      }
    }

    return autoConnectBookmark;
  }

  public boolean getBooleanParameter( String label, boolean def ) {
    String resultStr = getParameter( label );
    boolean result = def;

    String searchStr = "true";
    // we want to look for the opposite
    if ( def ) {
      searchStr = "false";
    }

    if (resultStr != null && resultStr.toLowerCase().trim().equals(searchStr)) {
      result = !def;
    }

    if ( SecureFTP.debug ) {
      System.err.println("get bool param " + label + " = " + result);
    }

    return result;
  }

  public int getIntParameter( String label, int def ) {
    String resultStr = getParameter( label );
    int result = def;

    if (resultStr != null) {
      result = Util.parseInt( resultStr, def );
    }

    if ( SecureFTP.debug ) {
      System.err.println("get int param " + label + " = " + result);
    }

    return result;
  }

  public void setSize( int width, int height ) {
    if ( SecureFTP.debug ) {
      System.err.println("width = " + width + ", height = " + height);
    }

    super.setSize( width, height );
    validate();
  }

  public void paint( Graphics g ) {
    super.paint( g );
    if (showBorder) {
      g.drawRect( 0, 0, getSize().width - 1, getSize().height - 1 );
      g.drawRect( 1, 1, getSize().width - 3, getSize().height - 3 );
    }
  }

  public Insets getInsets() {
    if (showBorder) {
      return new Insets(2, 2, 2, 2);
    }
    else
      return super.getInsets();
  }

  public Frame getBaseFrame() {
    Frame f;
    Component component = getContentPane();

    if (component == null)
      f = JOptionPane.getRootFrame();
    else if (component instanceof Frame)
      f = (Frame)component;
    else
      f = JOptionPane.getFrameForComponent(component.getParent());

    return f;
  }

  public void readPreferences() {
    Client.setShowHiddenFiles( getBooleanParameter("show_hidden_files", false) );
    Client.setShowFullColumnListing( getBooleanParameter("show_full_column_listing", false) );
    Client.setUseModeZCompression( getBooleanParameter("use_mode_z", true) );
    Client.setStartWithOpenDialog( getBooleanParameter("start_with_open_dialog", true) );
    Client.setAutoCheckForUpdate( false );
    Client.setCloseTabWarning( getBooleanParameter("show_close_tab_warning", true) );
    Client.setForcePasvControlIP( getBooleanParameter("force_pasv_control_ip", false) );
    Client.setTransferMode( getIntParameter("default_transfer_mode", FTP.AUTO_TRANSFER_MODE) );
    Client.setLastConnectionIndex( 0 );
  }

  public void writePreferences() {
  }
}

class StatusThread extends Thread {
  private JApplet applet;
  private Runnable runnable;

  public StatusThread(JApplet applet) {
    super();
    this.applet = applet;
    runnable = new Runnable() {
      public void run() {
        showStatus();
      }
    };
  }

  public void run() {
    try {
      sleep(1500);
    } catch (Exception e) {}
    SwingUtilities.invokeLater(runnable);
  }

  private void showStatus() {
    applet.getAppletContext().showStatus(Version.PROGRAM_NAME + " " +
                                         Version.SHORT_VERSION);
  }
}
