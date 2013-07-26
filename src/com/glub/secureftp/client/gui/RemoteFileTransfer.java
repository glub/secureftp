
//*****************************************************************************
//*
//* (c) Copyright 2004. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: RemoteFileTransfer.java 273 2010-07-25 15:12:56Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.bean.*;

import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

/**
 * Remote File Transfer class
 */
public class RemoteFileTransfer implements Transferable {
  JTable table = null;
  RemotePanel remotePanel = null;

  private static DataFlavor arrayListFlavor;

  static {
    arrayListFlavor = new DataFlavor( DataFlavor.javaJVMLocalObjectMimeType +
                                      "; class=java.util.ArrayList", 
                                      "ArrayList" );
  }

  public RemoteFileTransfer( RemotePanel remotePanel, JTable table ) {
    super();
    this.remotePanel = remotePanel;
    this.table = table;
  }

  public synchronized DataFlavor[] getTransferDataFlavors() {
    arrayListFlavor.setHumanPresentableName("RemoteFileList");
    DataFlavor[] df = {
      arrayListFlavor
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

    if ( flavor.equals(arrayListFlavor) ) {
      result = new ArrayList();

      int[] rows = table.getSelectedRows();
      //RemoteFileList rfl = remotePanel.getCurrentListing();

      for ( int i = 0; i < rows.length; i++ ) {
        if ( rows[i] >= 0 ) {
          RemoteFile remoteFile = 
            (RemoteFile)table.getValueAt(rows[i], RemoteFileTable.FILE_COLUMN);
          result.add( remoteFile );
        }
      }
    /*
        File newFile = new File( rf.getFileName() );
        boolean fileCreated = newFile.createNewFile();
        FTPSession session = FTPSessionManager.getInstance().getCurrentSession();
        try {
          session.getFTPBean().retrieve(rf, newFile, false);
        }
        catch ( Exception e ){}

      //result.add( File.createTempFile("secureftp", null) );
        result.add( newFile );
      }
	  */
    }
    else {
      throw new UnsupportedFlavorException( flavor );
    }

    return result;
  }
}

