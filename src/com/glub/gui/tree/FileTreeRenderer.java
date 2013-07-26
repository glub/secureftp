
//*****************************************************************************
//*
//* (c) Copyright 2004. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: FileTreeRenderer.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.gui.tree;

import java.awt.*;
import java.io.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.tree.*;

public class FileTreeRenderer extends DefaultTreeCellRenderer {
  private FileSystemView fsv = FileSystemView.getFileSystemView();
  protected static final long serialVersionUID = 1L;
  public Component getTreeCellRendererComponent( JTree tree, Object value,
                                                 boolean sel, boolean exp,
                                                 boolean leaf, int row,
                                                 boolean hasFocus ) {

    super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, 
                                       row, hasFocus);

    File file = null;

    TreePath tp = tree.getPathForRow( row );
    int size = 0;

    setToolTipText( value.toString() );

    if ( null != tp && null != tp.getPath() ) {
      size = tp.getPath().length;

      for ( int i = 1; i < size; i++ ) { 
        String filePathComp = (tp.getPath()[i]).toString();

        if ( null == file ) {
          file = new File( filePathComp );
        }
        else {
          file = new File( file, filePathComp );
        }
      }
    }

    Icon icon = null;

    if ( null != file ) {
      String canonicalPath = "";
      try {
        canonicalPath = file.getCanonicalPath();
      }
      catch ( Exception e ) {}

      if ( canonicalPath.length() > 0 &&
           canonicalPath.equals(fsv.getRoots()[0].getPath()) ) {
        icon = fsv.getSystemIcon(fsv.getRoots()[0]);
      }
      else if (canonicalPath.length() > 0 &&
               canonicalPath.equals(fsv.getHomeDirectory().getPath()) ) {
          icon = fsv.getSystemIcon(fsv.getHomeDirectory());
      }
      else if ( canonicalPath.length() > 0 &&
                canonicalPath.equals(fsv.getDefaultDirectory().getPath()) ) {
        icon = fsv.getSystemIcon(fsv.getDefaultDirectory());
      }
      else if ( file.exists() || fsv.isDrive(file) ) {
        try {
            icon = fsv.getSystemIcon(file);
        }
        catch ( Exception e ) {
          icon = UIManager.getIcon("FileView.fileIcon");
        }
      }
      else {
        icon = UIManager.getIcon("FileView.fileIcon");
      }
    }
    else {
      icon = fsv.getSystemIcon(fsv.getParentDirectory(File.listRoots()[0]));
    }

    if ( null != icon ) {
     setIcon( icon );
    }

    return this;
  }
}
