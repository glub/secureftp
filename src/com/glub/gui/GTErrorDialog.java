
//*****************************************************************************
//*
//* (c) Copyright 2005. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: GTErrorDialog.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.gui;

import com.glub.util.*;

import java.awt.*;
import javax.swing.*;

public class GTErrorDialog {
  public static void showDialog( Component parent,
                                 LString title, LString message, 
                                 int errorType ) {
    JPanel panel = new JPanel(new SpringLayout()) {
   	  protected static final long serialVersionUID = 1L;
      public Dimension getMaximumSize() {
        Dimension pref = getPreferredSize();
        return new Dimension( pref.width, Integer.MAX_VALUE );
      }
    };

    panel.add( new GTLabel(message, 400) );
    SpringUtilities.makeCompactGrid( panel, 1, 1, 10, 10, 10, 10 );
    JOptionPane.showMessageDialog( parent, 
                                   panel,
                                   title.getString(), errorType );
  }
}

