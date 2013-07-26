/*
 * @(#)RemoteDirectoryComboBoxEditor.java	1.25 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.glub.secureftp.client.gui;

import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;

public class RemoteDirectoryComboBoxRenderer extends DefaultListCellRenderer {
  protected static final long serialVersionUID = 1L;	
  public Component getListCellRendererComponent( JList list, Object value,
                                                 int index, boolean isSelected,
                                                 boolean cellHasFocus ) {
    Icon icon = UIManager.getIcon("FileView.directoryIcon");

    setIcon( icon );
    setIconTextGap( 4 );
    setBorder( new EmptyBorder( 0, 2, 0, 2 ) );

    if ( null != value ) {
      String path = value.toString();
      setText( path );
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
