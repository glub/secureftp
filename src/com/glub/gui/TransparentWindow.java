//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: TransparentWindow.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

// found on java forums. made some improvements. - gary

package com.glub.gui;

import com.glub.util.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class TransparentWindow extends JWindow {
  protected static final long serialVersionUID = 1L;	
  private ArrayList stringOverlay = new ArrayList();
  private Graphics2D tig;
  private Image img;
  private Image tim;
  private Robot r;
  private ImageIcon imageIcon;
  final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

  public TransparentWindow( final ImageIcon image ) {
    this( null, image, false );
  }

  public TransparentWindow( Frame owner, final ImageIcon image ) {
    this( owner, image, false );
  }

  public TransparentWindow( Frame owner, final ImageIcon image, 
                            boolean draggable ) {
    super( owner);

    imageIcon = image;

    try {
      r = new Robot();
    }
    catch (AWTException awe) {
      //System.out.println("robot excepton occurred");
    }

    if ( draggable ) {
      WindowDragger dragger = new WindowDragger();
      addMouseMotionListener(dragger);
      addMouseListener(dragger);
    }

    addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) {
        setSize(0, 0);
        capture();
        setSize(imageIcon.getIconWidth(), imageIcon.getIconHeight());
        setBounds( (screenSize.width / 2) - (getSize().width / 2),
                   (screenSize.height / 2) - (getSize().height / 2),
                   imageIcon.getIconWidth(), imageIcon.getIconHeight() );
      }
    });

    setSize( 0, 0 );
    capture();
    setSize( imageIcon.getIconWidth(), imageIcon.getIconHeight() );

    setLocation( (screenSize.width / 2) - (getSize().width / 2),
               (screenSize.height / 2) - (getSize().height / 2) );

  }

  public void capture() {
    img = r.createScreenCapture(new Rectangle(0, 0, 
                                screenSize.width, screenSize.height));
  }

  public void captureX() {
    Rectangle rect = getBounds();
    setVisible(false);

    Image xmg = r.createScreenCapture(rect);
    img.getGraphics().drawImage(xmg, rect.x, rect.y, rect.width, rect.height,
                                null);
    setVisible(true);
  }

  public void paint(Graphics g) {
    Rectangle rect = g.getClipBounds();
    if (tim == null) {
      tim = createImage(getWidth(), getHeight());
      tig = (Graphics2D)tim.getGraphics();
    }

    if (!rect.getSize().equals(getSize())) {
      captureX();
    }
    else {
      paintP(g);
    }
  }

  public void paintP(Graphics g) {
    tig.drawImage(img, 0, 0, getWidth(), getHeight(), getX(), getY(),
                  getX() + getWidth(), getY() + getHeight(), null);
    tig.drawImage(imageIcon.getImage(), 0, 0, null);

    Font defaultFont = new Font( "Verdana", Font.PLAIN, 10 );

    for( int i = 0; i < stringOverlay.size(); i++ ) {
      StringOverlay ol = (StringOverlay)stringOverlay.get(i);
      Font font = ol.getFont();

      if ( font == null ) {
        font = defaultFont;
      }

      tig.setFont( font );

      tig.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON );

      tig.drawString( ol.getString(), ol.getXPos(), ol.getYPos() );
    }

    g.drawImage(tim, 0, 0, null);
  }

  public void update(Graphics g) {
    // on the mac we flicker...
    if ( !Util.isMacOS() ) {
      this.paint(g);
    }
  }

  public void addStringOverlay( String str, int x, int y ) {
    addStringOverlay( str, x, y, null );
  }

  public void addStringOverlay( String str, int x, int y, Font font ) {
    stringOverlay.add( new StringOverlay(str, x, y, font) );
  }

  /*
  private class BackgroundRefresher extends FocusAdapter {
    public void focusGained(FocusEvent e) {
      setSize(0, 0);
      capture();
      setSize(imageIcon.getIconWidth(), imageIcon.getIconHeight());
    }
  }
*/
  
  private class WindowDragger implements MouseListener, MouseMotionListener {
    private Point mp;

    public void mouseClicked(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {
      if (mp == null)
      {
        return;
      }

      Point p = e.getPoint();
      int x = (getX() + p.x) - mp.x;
      int y = (getY() + p.y) - mp.y;
      setLocation(x, y);
      paintP(getGraphics());
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {
      mp = e.getPoint();
    }
    public void mouseReleased(MouseEvent e) {
      mp = null;
    }
  }
}

class StringOverlay {
  private String str = null;
  private int xPos = 0;
  private int yPos = 0;
  private Font font = null;

  public StringOverlay( String str, int x, int y ) {
    this( str, x, y, null );
  }

  public StringOverlay( String str, int x, int y, Font font ) {
    this.str = str;
    this.xPos = x;
    this.yPos = y; 
    this.font = font;
  }

  public String getString() { return str; }
  public int getXPos() { return xPos; }
  public int getYPos() { return yPos; }
  public Font getFont() { return font; }
}

