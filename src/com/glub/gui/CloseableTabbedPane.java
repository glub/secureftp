
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: CloseableTabbedPane.java 128 2009-12-10 09:18:22Z gary $
//*
//*****************************************************************************

// found on java forum. cleaned up code. - gary

package com.glub.gui;

import com.glub.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;
import java.util.*;

public class CloseableTabbedPane extends JTabbedPane implements ChangeListener {
  private ImageIcon selCloseIcon = null; 
  private ImageIcon relCloseIcon = null;
  private ImageIcon disRelCloseIcon = null;
  private HashMap backgroundMap = new HashMap();
  protected static final long serialVersionUID = 1L;


  public CloseableTabbedPane() {
    this( SwingUtilities.LEFT );
    addChangeListener( this );
  }

  public CloseableTabbedPane( int horizontalTextPosition ) {
    setUI( new CloseableBasicTabbedPaneUI(horizontalTextPosition) );
    //setTabLayoutPolicy( JTabbedPane.SCROLL_TAB_LAYOUT );
    addCloseableIconListener();

    URL url = getClass().getResource("images/close_widget_sel.png");
    if ( null != url ) {
      selCloseIcon = new ImageIcon( url );
    }

    url = getClass().getResource("images/close_widget.png");
    if ( null != url ) {
      relCloseIcon = new ImageIcon( url );
    }

    if ( null != relCloseIcon ) {
      disRelCloseIcon =
        new ImageIcon(GrayFilter.createDisabledImage(relCloseIcon.getImage()));
    }
  }

  public void stateChanged( ChangeEvent e ) {
    resetTabBackground();
  }

  public void resetTabBackground() {
    backgroundMap.clear();
  }

  public void setTabBackground( int index ) {
    backgroundMap.put( new Integer(index), new Object() );
  }

  public boolean tabBackgroundSet( int index ) {
    boolean result = backgroundMap.get( new Integer(index) ) != null;
    return result;
  }

  public ImageIcon getSelectedCloseIcon() { return selCloseIcon; }
  public ImageIcon getEnabledCloseIcon() { return relCloseIcon; }
  public ImageIcon getDisabledCloseIcon() { return disRelCloseIcon; }

  public void addTab( String title, Component component ) {
    addTab( title, relCloseIcon, component, null );
  }

  public void addTab( String title, Component component, String tooltip ) {
    super.addTab( title, relCloseIcon, component, tooltip );
  }

  public Rectangle getSelectedIconRect() {
    return ((CloseableBasicTabbedPaneUI)getUI()).getSelectedIconRect();
  }

  protected void closeTab() {
    remove( getSelectedIndex() );
  }

  protected void addCloseableIconListener() {
    addMouseListener( new MouseAdapter() {
      public void mousePressed( MouseEvent e ) {
        if ( getTabCount() > 0 ) {
          Rectangle r = getSelectedIconRect();
          if ( MouseEvent.BUTTON1 == e.getButton() &&
               r.contains(e.getPoint()) ) {
            if ( getTabCount() > 0 )
	      setIconAt( getSelectedIndex(), selCloseIcon );
          }
	  else {
            if ( getTabCount() > 0 )
	      setIconAt( getSelectedIndex(), relCloseIcon );
	  }
        }
      }

      public void mouseEntered( MouseEvent e ) {
        if ( getTabCount() > 0 )
          setIconAt( getSelectedIndex(), relCloseIcon );
      }

      public void mouseExited( MouseEvent e ) {
        if ( getTabCount() > 0 )
          setIconAt( getSelectedIndex(), relCloseIcon );
      }

      public void mouseReleased( MouseEvent e ) {
        if ( getTabCount() > 0 )
          setIconAt( getSelectedIndex(), relCloseIcon );
      }

      public void mouseClicked( MouseEvent e ) {
        if ( getTabCount() > 0 ) {
          Rectangle r = getSelectedIconRect();
          if ( MouseEvent.BUTTON1 == e.getButton() && 
               r.contains(e.getPoint()) ) {
            closeTab();
          }
        }
      }
    } );
  }

  public String getToolTipText( MouseEvent e ) {
    String tip = super.getToolTipText( e );
    Rectangle r = getSelectedIconRect();
    if ( r.contains(e.getPoint()) ) {
      tip = LString.getString("Common.button.close", "Close");
    }

    return tip;
  }
}

class CloseableBasicTabbedPaneUI extends BasicTabbedPaneUI {
  private Rectangle selIconRect;
  private int horizontalTextPosition = SwingUtilities.LEFT;

  public CloseableBasicTabbedPaneUI() {
    this( SwingUtilities.LEFT );
  }

  public CloseableBasicTabbedPaneUI( int horizontalTextPosition ) {
    this.horizontalTextPosition = horizontalTextPosition;
  }

  public Rectangle getSelectedIconRect() {
    return selIconRect;
  }

  protected void layoutLabel( int tabPlacement, FontMetrics metrics,
                              int tabIndex, String title, Icon icon, 
                              Rectangle tabRect, Rectangle iconRect, 
                              Rectangle textRect, boolean isSelected ) {
    textRect.x = 0; textRect.y = 0;
    iconRect.x = 0; iconRect.y = 0;

    CloseableTabbedPane tp = (CloseableTabbedPane)tabPane;

    if ( !tp.tabBackgroundSet(tabIndex) && tabIndex == tp.getSelectedIndex() ) {
      //tp.setBackgroundAt(tabIndex, Color.GRAY);
      tp.setTabBackground(tabIndex);
      tp.setForegroundAt(tabIndex, null);
      if ( tp.getEnabledCloseIcon() != null && !mouseDown(tp, tabIndex) ) {
        tp.setIconAt(tabIndex, tp.getEnabledCloseIcon());
      }
    }
    else if ( !tp.tabBackgroundSet(tabIndex) ) {
      //tp.setBackgroundAt(tabIndex, null);
      tp.setTabBackground(tabIndex);
      tp.setForegroundAt(tabIndex, Color.GRAY);
      if ( tp.getDisabledCloseIcon() != null && !mouseDown(tp, tabIndex) ) {
        tp.setIconAt(tabIndex, tp.getDisabledCloseIcon());
      }
    }

    SwingUtilities.layoutCompoundLabel((JComponent) tabPane, metrics,
                                       title, icon, SwingUtilities.CENTER, 
                                       SwingUtilities.CENTER,
                                       SwingUtilities.CENTER, 
                                       horizontalTextPosition, tabRect, 
                                       iconRect, textRect, textIconGap + 2);

    selIconRect = iconRect;
  }

  private boolean mouseDown( CloseableTabbedPane tp, int thisTab ) {
    return ( tp.getIconAt(thisTab) == tp.getSelectedCloseIcon() ); 
  }
}
