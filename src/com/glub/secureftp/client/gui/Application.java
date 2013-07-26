//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: Application.java 128 2009-12-10 09:18:22Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.gui.*;
import com.glub.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.prefs.Preferences;

public class Application extends JFrame implements PreferenceHandler {
  protected final static long serialVersionUID = 1L;
  private Preferences prefs = PreferencesDispatcher.getPreferences(this);
  protected static Client client = null;

  private int appWidth  = 0;
  private int appHeight = 0;
  private int appLocX   = 0;
  private int appLocY   = 0;

  private boolean readingPrefs = false;

  private static final String APPLICATION_STATE    = "ApplicationState";
  private static final String APPLICATION_WIDTH    = "ApplicationWidth";
  private static final String APPLICATION_HEIGHT   = "ApplicationHeight";
  private static final String APPLICATION_LOCX     = "ApplicationLocationX";
  private static final String APPLICATION_LOCY     = "ApplicationLocationY";

  public Application() {
    super( Client.PROGRAM_NAME + " (" + 
           LString.getString("Registration.non_commercial", 
                             "Non-Commercial Use") + ")" );
    client = new Client( Client.APPLICATION );

    client.init( this );

    getContentPane().setLayout( new BorderLayout() );

    // app icon
    ImageIcon icon = 
      new ImageIcon( getClass().getResource("images/appIcon.png") );

    setIconImage( icon.getImage().getScaledInstance(32, 32, 
                                                    Image.SCALE_SMOOTH) );

    // menus
    System.setProperty( "apple.laf.useScreenMenuBar", "true" );
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
    logo.setBorder( BorderFactory.createEmptyBorder(0, 15, 0, 5) );

    logo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    logo.addMouseListener( new MouseAdapter() {
      public void mouseClicked( MouseEvent e ) {
        if ( e.getButton() == MouseEvent.BUTTON1 )
          Util.openURL( "http://www.glub.com" );
        }
      } );

    toolPanel.add( logo );

    getContentPane().add( toolPanel, BorderLayout.NORTH );

    // base split pane
    getContentPane().add( Client.getBaseView(), BorderLayout.CENTER );

    // status bar
    JPanel statusPanel = new JPanel();
    statusPanel.setLayout( new BorderLayout() );

    statusPanel.add( Box.createVerticalStrut(5), BorderLayout.WEST );

    JPanel growPanel = new JPanel();
    growPanel.setLayout( new BorderLayout() );

    ImageIcon growIcon = 
      new ImageIcon( getClass().getResource("images/growbox.png") );

    growPanel.add( new JLabel(growIcon), BorderLayout.SOUTH );

    statusPanel.add( growPanel, BorderLayout.EAST );

    getContentPane().add( statusPanel, BorderLayout.SOUTH );

    setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );

    addWindowListener( new WindowAdapter() {
      public void windowClosed( WindowEvent e ) {
        SecureFTP.getCommandDispatcher().fireCommand( this, new ExitCommand() );
      }
    } );

    addComponentListener( new ComponentAdapter() {
      public void componentResized( ComponentEvent e ) {
        int width = getWidth();
        int height = getHeight(); 

        if ( Frame.NORMAL == getExtendedState() && !readingPrefs ) {
          appWidth = width;
	  appHeight = height;
          setLocX( (int)getLocation().getX() );
	  setLocY( (int)getLocation().getY() );
	}

        if ( null != Client.getToolBar() &&  
            width < Client.getToolBar().getMinimumSize().getWidth() + 160 ) {
          width = (int)Client.getToolBar().getMinimumSize().getWidth() + 160;
        }

        if ( null != Client.getToolBar() && 
             height < Client.getToolBar().getMinimumSize().getHeight() + 200 ) {
          height = (int)Client.getToolBar().getMinimumSize().getHeight() + 200;
        }

        setSize( width, height );
      }

      public void componentMoved( ComponentEvent e ) {
        int tempLocX = (int)getLocation().getX();
	int tempLocY = (int)getLocation().getY();

        if ( tempLocX >= 0 && tempLocY >= 0 && !readingPrefs ) {
          appWidth = getWidth();
	  appHeight = getHeight();
          setLocX( tempLocX );
	  setLocY( tempLocY );
	}
      }
    } );
  }

  public void setVisible( boolean b ) {
    if ( null != Client.getToolBar() ) {
      Client.getToolBar().setupDefaultFocus();
    }

    super.setVisible( b );
  }

  public Dimension getMinimumSize() {
    Dimension result = null;

    if ( null != Client.getToolBar() ) {
      result = Client.getToolBar().getMinimumSize();
    }
    else {
      result = new Dimension(0, 0);
    }

    return result;
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

  private void setLocX( int loc ) { 
    appLocX = loc; 
  }

  private void setLocY( int loc ) { 
    appLocY = loc; 
  }

  public void readPreferences() {
    readingPrefs = true;

    appWidth = prefs.getInt( APPLICATION_WIDTH, 750 );
    appHeight = prefs.getInt( APPLICATION_HEIGHT, 580 );

    int screenWidth = getToolkit().getScreenSize().width;
    int screenHeight = getToolkit().getScreenSize().height;

    if ( appWidth > screenWidth ) {
      appWidth = screenWidth - 100;
    }

    if ( appHeight > screenHeight - 50 ) {
      appHeight = screenHeight - 100;
    }

    setSize( appWidth, appHeight );

    setLocX( prefs.getInt( APPLICATION_LOCX, screenWidth ) );
    setLocY( prefs.getInt( APPLICATION_LOCY, screenHeight ) );

    if ( appLocX > screenWidth - 150 || appLocY > screenHeight - 150 ) {
      setLocX( (screenWidth - appWidth) / 2 );
      setLocY( (screenHeight - appHeight) / 2 );
    }

    setLocation( appLocX, appLocY );
    setExtendedState( prefs.getInt(APPLICATION_STATE, Frame.NORMAL) );

    readingPrefs = false;
  }

  public void writePreferences() {
    prefs.putInt( APPLICATION_WIDTH, appWidth );
    prefs.putInt( APPLICATION_HEIGHT, appHeight );
    prefs.putInt( APPLICATION_LOCX, appLocX );
    prefs.putInt( APPLICATION_LOCY, appLocY );
    prefs.putInt( APPLICATION_STATE, getExtendedState() );
  }
}

