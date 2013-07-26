
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: GTFileSystemView.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.util;

import javax.swing.filechooser.*;
import java.io.*;
import java.util.*;

public class GTFileSystemView extends FileSystemView {
  public GTFileSystemView() {
    super();
  }

  public File createNewFolder( File file ) throws IOException {
    return FileSystemView.getFileSystemView().createNewFolder( file );
  }

  public static String getProperSystemDisplayName( File file ) {
    String result = getFileSystemView().getSystemDisplayName( file );

    return result;
  }

  public static File[] getProperRoots() {
    File[] result = getFileSystemView().getRoots();

    if ( Util.isMacOS() ) {
      ArrayList resultList = new ArrayList();
      File volumes = new File("/Volumes");
      File[] mountedDisks = volumes.listFiles();

      int rootDrive = 0;
      for ( int i = 0; i < mountedDisks.length; i++ ) {
        try {
          if ( mountedDisks[i].getCanonicalPath().equals("/") ) {
            rootDrive = i;
            resultList.add( mountedDisks[i] );
          }
        }
        catch ( IOException ioe ) {}
      }

      for ( int i = 0; i < mountedDisks.length; i++ ) {
        if ( rootDrive == i ) {
          continue;
        }

        if( mountedDisks[i].isDirectory() ) {
          resultList.add( mountedDisks[i] );
        }
      }

      File[] tempObject = new File[resultList.size()];
      result = (File[])resultList.toArray( tempObject );
    }

    return result;
  }
}
