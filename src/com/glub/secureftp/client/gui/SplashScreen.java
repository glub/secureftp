//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: SplashScreen.java 128 2009-12-10 09:18:22Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.gui.*;
import com.glub.util.*;
import com.glub.secureftp.client.framework.*;

import java.awt.*;
import java.awt.font.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

public class SplashScreen {
  private TransparentWindow win;
  private static final int PLAIN = 0;
  private static final int BOLD = 1;
  private static final int HYPERLINK = 2;
  private static String buildInfo = null;

  public SplashScreen() {
    this( false );
  }

  public SplashScreen( boolean aboutBox ) {
    if ( aboutBox ) {
      ImageIcon splashIcon = 
        new ImageIcon( getClass().getResource("images/secure_lock.png"));

      JPanel infoPanel = new JPanel();
    
      String version = Version.VERSION;

      if ( buildInfo == null ) {
        InputStream buildFileStream = 
          getClass().getResourceAsStream("build.info");
        if ( buildFileStream != null ) {
          try {
            Properties buildProp = new Properties();
            buildProp.load( buildFileStream );
            buildInfo = buildProp.getProperty("build.date") + "." + 
                        buildProp.getProperty("build.number");
          }
          catch ( Exception ioe ) { buildInfo = "unknown"; }
        }
        else {
          buildInfo = "unknown";
        }
      }

      boolean isDemo = false;
      if ( Client.getDemoTimeout() != null ) {
	isDemo = true;
      }
      else if ( SecureFTP.isBeta() ) {
        version += " (" + Version.BETA + ")";
      }

      JPanel regPanel = new JPanel();
      regPanel.setLayout( new BoxLayout(regPanel, BoxLayout.Y_AXIS) );

      Object[] items = { 
        Version.PROGRAM_NAME,
        "<html><table cellpadding=0 cellspacing=3><tr><td>gui:</td><td>v" + version + " [" + buildInfo + "]</td></tr><tr><td>lib:</td><td>v" + Version.BEAN_VERSION + " [" + Version.BEAN_DATE + "]</td></tr></table></html>",
        " ",
        regPanel,
        " ",
        "<html><b>Engineering</b>: Gary Cohen</html>",
        "<html><b>Technical Editor:</b> Brian Wallace</html>",
        "<html><b>Artwork:</b> Lev Mazin and Todd Rosenthal</html>",
	"<html><b>Additional Icons:</b> Mark James &lt;http://www.famfamfam.com&gt;</html>",
        "<html><b>Localization:</b> Kaori Mikawa, Lidia Sheinin, Lucien Gentis, <br>&nbsp;&nbsp;&nbsp;Paulo Sebastiao, Dominik Schroeder, Ada Alexandre Metola</html>",
        " ",
        LString.getString("Ad.bean", "Based on Glub Tech's Secure FTP Bean."),
        " ",
        "http://github.com/glub/secureftp",
        " ",
        System.getProperty("java.vendor") + " Java v" + System.getProperty("java.version"),
        " ",
        Version.COPYRIGHT_GUI,
        "Glub Tech, the Glub Tech logo, the lock images and icons,",
        "and Secure FTP are trademarks of Glub Tech, Incorporated.",
        "Java is a registered trademark of Sun Microsystems, Inc.",
        "All rights reserved.",
      };

      int[] attrib = { BOLD, 
                       PLAIN, 
                       PLAIN, 
                       -1, 
                       PLAIN,
                       PLAIN,
                       PLAIN, 
                       PLAIN, 
                       PLAIN, 
                       PLAIN, 
                       PLAIN, 
                       PLAIN, 
                       PLAIN, 
                       HYPERLINK, 
                       PLAIN, 
                       PLAIN, 
                       PLAIN, 
                       PLAIN, 
                       PLAIN, 
                       PLAIN, 
                       PLAIN, 
                       PLAIN };

      infoPanel.add( addItemsToPanel(items, attrib) );

      Object[] dialogItems = { infoPanel };

      String[] options = { LString.getString("Common.button.ok", "OK") };
                         
      String title = LString.getString("AboutBox.dialogTitle",
                                       "About Secure FTP");
      JOptionPane.showOptionDialog( SecureFTP.getBaseFrame(),
	                                  dialogItems,
	                                  title,
	                                  JOptionPane.DEFAULT_OPTION,
	                                  JOptionPane.PLAIN_MESSAGE,
	                                  splashIcon /* icon */,
	                                  options,
	                                  options[0] /* selected button */ );
    }
    else {
      ImageIcon splashIcon = 
        new ImageIcon( getClass().getResource("images/splash.png"));

      String fontName = "Verdana";

      if ( !Util.supportsFont(fontName, SecureFTP.locale) ) {
        fontName = "Default";
      }

      win = new TransparentWindow( SecureFTP.getBaseFrame(), splashIcon );

      int hOffset = 110;
      int vOffset = 280;
      win.addStringOverlay( Version.PROGRAM_NAME + " v" + Version.VERSION,
                            hOffset, vOffset += 15 );
      win.addStringOverlay( Version.COPYRIGHT_GUI, hOffset, vOffset += 15 );
      win.addStringOverlay( "All rights reserved.", hOffset, vOffset += 15 );
      win.setVisible( true );
    }
  }

  public void dispose() { 
    if ( null != win ) {
      win.dispose(); 
    }
  }

  private JPanel addItemsToPanel( Object[] items, int[] attrib ) {
    JPanel panel = new JPanel( new SpringLayout() ) {
    	protected static final long serialVersionUID = 1L;
    	public Dimension getMaximumSize() {
          Dimension pref = getPreferredSize();
          return new Dimension( Integer.MAX_VALUE, pref.height );
        }
    };

    for ( int i = 0; i < items.length; i++ ) {
      JLabel label = null;

      if ( items[i] instanceof JPanel ) {
        panel.add( (JPanel)items[i] );
        continue;
      }
      else if ( items[i] instanceof ImageIcon ) {
        label = new JLabel( (ImageIcon)items[i] );
      }
      else if ( attrib[i] == HYPERLINK ) {
        String url = items[i].toString();
        label = new GTLink( url, url );
      }
      else {
        label = new JLabel( items[i].toString() );
      }

      HashMap attribs = new HashMap();
      attribs.put( TextAttribute.SIZE, new Float(11));
      attribs.put( TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR );
      label.setForeground( Color.BLACK );

      if ( BOLD == attrib[i] ) {
        attribs.put( TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD );
        attribs.put( TextAttribute.SIZE, new Float(14));
      }

      Font infoFont = new Font( attribs );

      label.setFont( infoFont );

      panel.add( label );
    }

    SpringUtilities.makeCompactGrid( panel,
                                     items.length, 1, // rows, cols
                                     0, 3,            // init x, init y
                                     0, 0             // pad x, pad y
                                   );

    return panel;
  }
}

class ScrollerPanel extends JPanel {
  protected static final long serialVersionUID = 1L;
  private String text;

  protected Color backColor = Color.WHITE;
  protected Color foreColor = Color.BLACK;

  private Font font;
  private FontMetrics fm;

  private Image offscreenImage;
  private Graphics offscreenGraphics;

  protected int stringHeight, stringWidth, xPos, yPos;

  private ScrollThread scrollThread;

  public ScrollerPanel() {
    super();

    String fontName = "Verdana";

    if ( !Util.supportsFont(fontName, SecureFTP.locale) ) {
      fontName = "Default";
    }

    font = new Font( fontName, Font.PLAIN, 10 );
    fm = getFontMetrics( font );
  }

  public void scrollText( String text ) {
    this.text = text;

    scrollThread = new ScrollThread();
    scrollThread.start();
  }

  public void dispose() {
    scrollThread.exit();
  }

  public void update( Graphics g ) {
    paint( g );
  }

  public void paint ( Graphics g ) {
    if ( offscreenGraphics != null ) {
      offscreenGraphics.setColor( getBackground() );
      offscreenGraphics.fillRect( 0, 0, getWidth(), getHeight() );
      offscreenGraphics.setFont( font );
      offscreenGraphics.setColor( foreColor );
      offscreenGraphics.drawString( text, xPos, yPos );
      g.drawImage( offscreenImage, 0, 0, this );
    }
    else {
      super.paint(g);
    }
  }

  class ScrollThread extends Thread {
    private boolean scroll = true;

    public ScrollThread() {
      stringHeight = fm.getHeight();
      stringWidth = fm.stringWidth( text );
      //yPos = (getHeight() + stringHeight/2) / 2;    
      yPos = 6;
    }

    public void run() {
      while( scroll ) {
        if ( offscreenImage != null ) {
          offscreenGraphics = offscreenImage.getGraphics();
          for( xPos = getWidth(); xPos > 0 - stringWidth; xPos = xPos - 5 ) {
            try {
              sleep(100);
            }
            catch ( InterruptedException e ) {}
            repaint();
          } 
        }
        else {
          int w = getWidth();
          int h = getHeight();
          if ( w <= 0 ) w = 100;
          if ( h <= 0 ) h = 50;
          offscreenImage = createImage( w, h );
          try {
            sleep(250);
          }
          catch ( InterruptedException e ) {}
        }
      }
    }

    public void exit() { scroll = false; } 
  }
}
