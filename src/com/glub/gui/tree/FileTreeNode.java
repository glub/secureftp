
//*****************************************************************************
//*
//* (c) Copyright 2004. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: FileTreeNode.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.gui.tree;

import com.glub.util.*;

import java.io.*;
import javax.swing.tree.*;

public class FileTreeNode extends DefaultMutableTreeNode {
  //private File file;
  //private boolean isRoot;
  protected static final long serialVersionUID = 1L;


  public FileTreeNode( String rootNodeName ) {
    super( rootNodeName );
    //isRoot = true;
  }

  public FileTreeNode( File f ) {
    super( f );
    //file = f;
  }

  public boolean getAllowsChildren() {
    boolean ret = !isLeaf();
    return ret;
  }

  public boolean isLeaf() {
    boolean ret = !isDirectory();

    if ( !ret && Util.isMacOS() ) {
      if ( getFile() != null && 
           (getFile().getName().endsWith(".app") ||
            getFile().getName().endsWith(".mpkg") ||
            getFile().getName().endsWith(".pkg")) ) {
        ret = true;
      }
    }

    return ret;
  }

  public String toString() {
    if ( ! (getUserObject() instanceof File) ) {
      return (String)getUserObject();
    }

    File file = getFile();
    if (file == null) {
      return "";
    }

    String name = file.getName();
    if (name == null || name.length() == 0) {
      name = file.toString();
    }

    return (name != null) ? name : "";
  }

  public File getFile() {
    if ( getUserObject() instanceof File ) {
      return (File)getUserObject();
    }
    else {
      return null;
    }
  }

  public boolean isDirectory() {
    File file = getFile();
    return file == null || file.isDirectory();
  }

  public void buildChildNodes() {
    File file = getFile();
    if (file == null || !file.isDirectory()) {
      return;
    }

    File[] subFiles = file.listFiles();
    if (subFiles == null) {
      return;
    }

    int numKids = getChildCount();
    for ( int i = 0; i < subFiles.length; i++, numKids++ ) {
      add( new FileTreeNode( subFiles[ i ] ) );
//      getModel().insertNodeInto( new FileTreeNode( subFiles[ i ] ),
//                                 expandNode, numKids );
    }
  }
}

