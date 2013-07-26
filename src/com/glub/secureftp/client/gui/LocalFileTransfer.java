
//*****************************************************************************
//*
//* (c) Copyright 2004. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LocalFileTransfer.java 273 2010-07-25 15:12:56Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

/**
 * Local File Transfer class
 */
public class LocalFileTransfer implements Transferable {
  JTable table = null;
  LocalPanel localPanel = null;

  public LocalFileTransfer( LocalPanel localPanel, JTable table ) {
    super();
    this.localPanel = localPanel;
    this.table = table;
  }

  public synchronized DataFlavor[] getTransferDataFlavors() {
    DataFlavor flavor = 
      new DataFlavor( DataFlavor.javaJVMLocalObjectMimeType +
                      "; class=java.io.File", "File" );
    flavor.setHumanPresentableName("LocalFile");
    DataFlavor[] df = {
      flavor
    };
    return df;
  }

  public boolean isDataFlavorSupported( DataFlavor flavor ) {
    boolean result = flavor.equals( DataFlavor.javaFileListFlavor );
    return result;
  }

  public synchronized Object getTransferData( DataFlavor flavor ) 
                                              throws UnsupportedFlavorException,
                                                     IOException {
    ArrayList result = null;

    if ( flavor.equals(DataFlavor.javaFileListFlavor) ) {
      result = new ArrayList();

      int[] rows = table.getSelectedRows();

      for ( int i = 0; i < rows.length; i++ ) {
        if ( rows[i] >= 0 ) {
          File file = 
            (File)table.getValueAt( rows[i], LocalFileTable.FILE_COLUMN );
          result.add( file );
        }
      }
    }
    else {
      throw new UnsupportedFlavorException( flavor );
    }

    return result;
  }
}

