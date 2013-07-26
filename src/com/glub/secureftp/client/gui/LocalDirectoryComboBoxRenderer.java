/*
 * @(#)LocalDirectoryComboBoxEditor.java	1.25 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.glub.secureftp.client.gui;

import com.glub.util.*;
import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.io.*;

public class LocalDirectoryComboBoxRenderer extends DefaultListCellRenderer {
  protected static final long serialVersionUID = 1L;	
  private static JFileChooser chooser = new JFileChooser();
  public Component getListCellRendererComponent( JList list, Object value,
                                                 int index, boolean isSelected,
                                                 boolean cellHasFocus ) {

    Icon icon = UIManager.getIcon("FileView.directoryIcon");

    File curFile = null;

    String machineName = "";
    if ( Util.isWindows() ) {
      machineName = LString.getString("Common.machineName", "My Computer");
    }
    else {
      machineName = LString.getString("Common.machineName.mac_os", "Computer");
    }

    String passedInName = System.getProperty("hostname");
    if ( passedInName != null && passedInName.length() > 0 ) {
      machineName = passedInName;
    }

    if ( null != value && value.equals( machineName ) ) {
        icon = UIManager.getIcon("FileView.computerIcon");

        if ( icon == null ) {
          icon = UIManager.getIcon("FileView.directoryIcon");
        }
    }
    else if ( null != value ) {
      try {
        curFile = new File( value.toString() );

        icon = chooser.getIcon( curFile );
      }
      catch ( Exception e ) {
      }
    }

    setIcon( icon );
    setIconTextGap( 4 );
    setBorder( new EmptyBorder(0, 3, 0, 3) );

    if ( null != curFile ) {
      String path = curFile.getName();
      if ( curFile.getName().length() == 0 ) {
        path = value.toString();
      }
      setText( path );
    }
    else if ( null != value ) {
      setText( value.toString() );
    }

    if ( isSelected ) {
      setBackground( list.getSelectionBackground() );
      setForeground( list.getSelectionForeground() );
    }
    else {
      setBackground( list.getBackground() );
      setForeground( list.getForeground() );
    }

    return this;
  }
}
