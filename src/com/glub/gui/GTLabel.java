
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: GTLabel.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.gui;

import com.glub.util.*;

import java.awt.*;
import javax.swing.*;
import java.util.StringTokenizer;

/***********************************************************************
 * Label with line wrapping.
 *
 * @ver	1.0
 ***********************************************************************/
public class GTLabel extends JPanel {
  protected static final long serialVersionUID = 1L;	
  private int     labelWidth    = 0;
  private boolean lineWrap      = true;
  private String  originalText  = null;

  public GTLabel( LString str, int width ) {
    this( str.getString(), width );
  }

  public GTLabel( String text, int width ) {
    super();
    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
    labelWidth = width;
    setText( text );
  }

  public String getText() {
    return originalText;
  }

  public void setText( LString str ) {
    setText( str.toString() );
  }

  public void setText( String text ) {
    if (text != null && lineWrap) {
      StringTokenizer tok = new StringTokenizer( text, " \n", true );
      StringBuffer buf = null;

      FontMetrics metrics = getFontMetrics( getFont() );
      int pos = 0;

      for ( int i = 0; tok.hasMoreTokens(); i++ ) {
        String next = tok.nextToken();
        boolean doWrap = false;
        boolean doAppend = true;

        // Skip spaces at the beginning of a line
        if (pos == 0 && next.equals( " " ))
          continue;

        int size = metrics.stringWidth( next );
        int temp = pos + size;

        // Reset position when a newline is found
        if (next.equals( "\n" )) {
          doWrap = true;
          doAppend = false;
        }
        else if (pos != 0 && temp > labelWidth) {
          doWrap = true;
        }

        if (doWrap) {
          if (buf == null)
            add( Box.createVerticalStrut( 5 ) );
          else
            add( new JLabel( buf.toString() ) );

          buf = null;
          pos = 0;
        }

        if (doAppend) {
          if (buf == null)
            buf = new StringBuffer( next );
          else
            buf.append( next );

          pos += size;
        }
      }

      if (buf != null) {
        add( new JLabel( buf.toString() ) );
        buf = null;
      }
    }

    originalText = text;
  }

  public void setLineWrap( boolean wrap ) {
    lineWrap = wrap;
  }

  public void setLabelWidth( int width ) {
    labelWidth = width;
  }

  public void update( Graphics g ) {
    super.update( g );
    setText( originalText );
  }

// End of class GTLabel
}

