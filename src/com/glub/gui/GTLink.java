
//*****************************************************************************
//*
//* (c) Copyright 2009. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: GTLink.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.gui;

import com.glub.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GTLink extends JLabel {
  public GTLink( final String url ) {
    this( url, url );
  }

  public GTLink( final String displayURL, final String actualURL ) {
    super( "<html><font color=blue><u>" +
             displayURL +
           "</u></font></html>" );
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    addMouseListener( new MouseAdapter() { 
      public void mouseClicked( MouseEvent e ) {
        if ( e.getButton() == MouseEvent.BUTTON1 &&
             actualURL != null ) {
          Util.openURL( actualURL );
        }
      }
    } );
  }
}
