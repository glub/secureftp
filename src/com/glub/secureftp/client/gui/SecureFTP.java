//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: SecureFTP.java 142 2009-12-16 04:23:16Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.secureftp.bean.*;
import com.glub.util.*;

import java.awt.*;
import java.util.*;
import java.lang.reflect.*;
import javax.swing.*;

public class SecureFTP {
  private static boolean BETA = false;
  private static Application app = null;
  private static SecureFTPApplet applet = null;
  private static CommandDispatcher commandDispatcher = null;
  public static boolean forceEncrypt = false;
  public static boolean debug = false;
  public static java.util.Locale locale = Locale.getDefault();

  public static void main( String[] args ) {
    if ( System.getProperty("java.version").startsWith("1.0") ||
         System.getProperty("java.version").startsWith("1.1") ||
         System.getProperty("java.version").startsWith("1.2") ||
         System.getProperty("java.version").startsWith("1.3") ) {
      JOptionPane.showMessageDialog( null, 
		                     "Secure FTP requires the Java 2 " +
		                     "Runtime version 1.4 or higher.", 
				     "Error", JOptionPane.ERROR_MESSAGE );
      System.exit( 1 );
    }

    String language = GTOverride.getString("glub.language");

    if ( null != language && language.trim().length() == 5 ) { 
       String country = language.trim().substring(3, 5).toUpperCase();
       language = language.trim().substring(0, 2).toLowerCase();
      locale = new java.util.Locale( language, country, "" );
      java.util.Locale.setDefault( locale );
      System.out.println( "Overriding default locale and setting to: " + 
                           locale );
    }

    try {
      UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
    }
    catch ( Exception e ) {}

    System.setProperty( "apple.laf.useScreenMenuBar", "true" );

    String localDir = GTOverride.getString("glub.user.dir");
 
    if ( localDir == null ) {
      localDir = GTOverride.getString("glub.localdir");
    }

    if ( null != localDir && localDir.indexOf("$HOME") >= 0 ) {
      localDir = Util.searchAndReplace(localDir, "$HOME", 
                                       System.getProperty("user.home"), true);
    }

    if ( null != localDir && localDir.length() > 0 ) {
      java.io.File newLocalDir = new java.io.File( localDir );
      if ( newLocalDir.exists() && newLocalDir.isDirectory() ) {
        try {
          System.setProperty("user.dir", newLocalDir.getCanonicalPath());
        }
        catch ( Exception e ) {}
      }
    }
    
    forceEncrypt = GTOverride.getBoolean("glub.dataencrypt.override");

    debug = GTOverride.getBoolean("glub.debug");
    Debug.enable( isBeta() || debug );

    System.setProperty( "glub.resourceBundle", 
                        "com.glub.secureftp.client.resources.strings" );

    SplashScreen splash = new SplashScreen();

    setCommandDispatcher(new CommandDispatcher());
    getCommandDispatcher().addListener(UIRefresh.class, new UIRefresh());
    getCommandDispatcher().addListener(CommandPlayer.class, new CommandPlayer());

    if ( Util.isMacOS() ) {
      com.apple.eawt.Application macApp = null;

      try {
        Class appClass = com.apple.eawt.Application.class;
        Method getApp = appClass.getMethod("getApplication", null);

        if ( null != getApp ) {
          macApp = (com.apple.eawt.Application)getApp.invoke(SecureFTP.class, 
                                                             null);

          Class appListenerClass = com.apple.eawt.ApplicationListener.class;

          Method appListener =
            macApp.getClass().getMethod("addApplicationListener", 
                                        new Class[] { appListenerClass });

          if ( null != appListener ) {
            appListener.invoke(macApp, 
              new com.apple.eawt.ApplicationListener[] {new MacMenuAdapter()});
          }

          Method prefsMenu = 
            macApp.getClass().getMethod("setEnabledPreferencesMenu",
              new Class[] { boolean.class });

          if ( null != prefsMenu ) {
            prefsMenu.invoke(macApp, new Boolean[] { new Boolean(true) });
          }
        }
      }
      catch ( Exception e ) {}
    }

    app = new Application();

    if ( debug ) {
      System.out.println("Reading prefs");
    }

    PreferencesDispatcher.doReadPrefs();

    splash.dispose();

    if ( debug ) {
      System.out.println("Pre-seeding for SSL");
    }

    SSLFTP.preSeed();

    app.setTitle( Client.PROGRAM_NAME );

    if ( debug ) {
      System.out.println("Showing application");
    }

    app.setVisible( true );

    if ( Client.startWithOpenDialog() ) {
      SecureFTP.getCommandDispatcher().fireMTCommand( SecureFTPApplet.class,
                                                      new OpenCommand(),
                                                      Thread.MIN_PRIORITY,
                                                      250,
                                                      new SecureFTPError(),
                                                      false );
    }
  }

  public static ArrayList getCertificates() {
    FTPSession session = new FTPSession();
    return SSLFTP.getCertificates( session.getKeyStoreFile(), null );
  }

  public static void setCommandDispatcher( CommandDispatcher cd ) {
    commandDispatcher = cd;
  }

  public static CommandDispatcher getCommandDispatcher() {
    return commandDispatcher;
  }

  public static void setAppletInstance( SecureFTPApplet instance ) {
    applet = instance;
  }

  public static Frame getBaseFrame() {
    if ( null != app ) {
      return app.getBaseFrame();
    }
    else if ( null != applet ) {
      return applet.getBaseFrame();
    }
    else 
      return JOptionPane.getRootFrame();
  }

  public static Application getApplication() {
    return app;
  }

  public static boolean isBeta() {
    return BETA;
  }
}

