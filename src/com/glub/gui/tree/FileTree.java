
//*****************************************************************************
//*
//* (c) Copyright 2004. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: FileTree.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.gui.tree;

import com.glub.util.*;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.event.*;

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.tree.*;

public class FileTree extends JTree implements DropTargetListener {
  private static boolean showHiddenFiles = false;
  private DefaultTreeModel treeModel = null;
  protected static final long serialVersionUID = 1L;

  public FileTree() {
    this( "My Computer", false );
  }

  public FileTree( String rootName, boolean shf ) {
    showHiddenFiles = shf;
    
    String passedInName = System.getProperty("hostname");
    if ( passedInName != null && passedInName.length() > 0 ) {
      rootName = passedInName;
    }

    new DropTarget( this, DnDConstants.ACTION_COPY_OR_MOVE, this );

    setDragEnabled( true );

    FileTreeNode rootNode = new FileTreeNode( rootName );
    treeModel = new DefaultTreeModel( rootNode );

    ToolTipManager.sharedInstance().registerComponent(this);
    
    treeModel.setAsksAllowsChildren( true );
    setModel( treeModel );
    setEditable( false );
    setRootVisible( true );
    setShowsRootHandles( true );
    setCellRenderer( new FileTreeRenderer() );
    getSelectionModel().setSelectionMode
            (TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

    File[] roots = File.listRoots();
    if (roots != null) {
      for (int i = 0; i < roots.length; i++) {
        // on the mac, the roots can be found in /Volumes
        if ( !Util.isMacOS() ) {
          treeModel.insertNodeInto( new FileTreeNode( roots[i] ),
                                    rootNode, rootNode.getChildCount() );
        }
      }

      // on mac, /Volumes lists mounted disks
      if ( Util.isMacOS() ) {
        File volumes = new File("/Volumes");
        File[] mountedDisks = volumes.listFiles();
        int numKids = rootNode.getChildCount();

        int rootDrive = 0;
        for (int i = 0; i < mountedDisks.length; i++) {
          try {
            if ( mountedDisks[i].getCanonicalPath().equals("/") ) {
              rootDrive = i;
              treeModel.insertNodeInto( new FileTreeNode( mountedDisks[i] ),
                                        rootNode, numKids++ );
              break;
            }
          } catch ( Exception e ) {}
        }

        for (int i = 0; i < mountedDisks.length; i++) {
          if ( rootDrive == i ) {
            continue;
          }

          if ( mountedDisks[i].isDirectory() ) {
            treeModel.insertNodeInto( new FileTreeNode( mountedDisks[i] ),
                                      rootNode, numKids++ );
          }
        }
      }
    }

    addTreeExpansionListener( new TreeExpansionListener() {
      // Required by TreeExpansionListener interface.
      public void treeExpanded(TreeExpansionEvent e) {
        //System.err.println( "tree expanded, path = " + e.getPath() );
        TreePath path = e.getPath();
        if (path == null) {
          return;
        }

        FileTreeNode expandNode = (FileTreeNode)path.getLastPathComponent();
        if (expandNode == null) {
          return;
        }

        //expandNode.buildChildNodes();
        File file = expandNode.getFile();
        if (file == null || !file.isDirectory()) {
          return;
        }

        File[] subFiles = file.listFiles();
        if (subFiles == null) {
          return;
        }

        expandNode.removeAllChildren();

        HashMap hiddenFileMap = new HashMap();

        final boolean showHiddenFiles = FileTree.showHiddenFiles();

        if ( !showHiddenFiles ) {
          if ( Util.isMacOS() ) {
            File hiddenFileList = new File(file.getAbsolutePath(), ".hidden");

            if ( hiddenFileList != null && !hiddenFileList.exists() ) {
              hiddenFileList = new File("/.hidden");
            }

            if ( hiddenFileList != null && hiddenFileList.exists() ) {
              FileInputStream fis = null;
              try {
                fis = new FileInputStream( hiddenFileList );
                BufferedReader br = 
                  new BufferedReader( new InputStreamReader(fis) );
                String line = null;
                while ( (line = br.readLine()) != null ) {
                  hiddenFileMap.put( line, "true" );
                }
              }
              catch ( Exception e1 ) {}
              finally {
                  if ( fis != null ) {
                	  try {
                		  fis.close();
                	  }
                	  catch ( IOException ioe ) {}
                  }
              }

              hiddenFileMap.put( "Cleanup At Startup", "true" );
              hiddenFileMap.put( "TheFindByContentFolder", "true" );
              hiddenFileMap.put( "TheVolumeSettingsFolder", "true" );
            }
          }
        }

        int numKids = expandNode.getChildCount();
        for ( int i = 0; i < subFiles.length; i++ ) {
          String fileName = subFiles[i].getName();

          boolean addFile = true;

          if ( !showHiddenFiles && Util.isMacOS() ) {
            if ( fileName.startsWith(".") ) {
              addFile = false;
            }
            else if ( fileName.endsWith("Move&Rename") ) {
              addFile = false;
            }
            else if ( subFiles[i].isHidden() ) {
              addFile = false;
            }
            else if ( hiddenFileMap.get(fileName) != null ) {
              addFile = false;
            }
          }
          else if ( !showHiddenFiles ) {
            if ( FileSystemView.getFileSystemView().isHiddenFile(subFiles[i]) )
              addFile = false;
          }

          if ( addFile ) {
            treeModel.insertNodeInto( new FileTreeNode( subFiles[i] ),
                                      expandNode, numKids++ );
          }
        }

        treeModel.nodeStructureChanged( expandNode );
      }

      // Required by TreeExpansionListener interface.
      public void treeCollapsed(TreeExpansionEvent e) {
        //System.err.println( "tree collapsed, path = " + e.getPath() );
      }
    } );
  }

  // taken from Java Forums (by lavansesh)
  public Point getToolTipLocation( MouseEvent e ) {
    Point location = null;
    Point point = e.getPoint();
    TreePath path = getPathForLocation(point.x, point.y);

    if ( null != path && false == isTextVisible(path) ) {
      TreeCellRenderer renderer = getCellRenderer();
      Component c = 
        renderer.getTreeCellRendererComponent( this, 
                                               path.getLastPathComponent(),
                                               false, false, false, 0, false );

      if ( c instanceof JLabel ) {
        JLabel label = (JLabel)c;

        int icon = label.getIcon().getIconWidth();

        Rectangle cellBounds = getPathBounds(path);
        location = new Point(cellBounds.x + icon + label.getIconTextGap(),
                             cellBounds.y);
      }
    }

    return location;
  }

  private boolean isTextVisible( TreePath path ) {
    boolean result = false;

    /*
    Rectangle cellBounds = getPathBounds(path);
    Rectangle visibleRect = getVisibleRect();

    if ( (visibleRect.width - cellBounds.x) < cellBounds.width ) {
      result = false;
    }
    */

    return result;
  }

  public void drop( DropTargetDropEvent dtde ) {
    try {
      DataFlavor fileFlavor = DataFlavor.javaFileListFlavor;
      Transferable tr = dtde.getTransferable();

      if ( dtde.isDataFlavorSupported(fileFlavor) ) {
        dtde.acceptDrop( DnDConstants.ACTION_COPY );
        java.util.List fileList = 
          (java.util.List)tr.getTransferData( fileFlavor );
        dtde.dropComplete( true );
        final Iterator iter = fileList.iterator();
        if ( iter.hasNext() ) {
          Thread t = new Thread() {
            public void run() {
              changeDir( (File)iter.next() );
            }
          };
          t.start();
        }
      }
      else if ( canHandleDrop(dtde) ) {
        handleDrop(dtde);
      }
      else {
        dtde.rejectDrop();
      }
    }
    catch ( Exception e ) { /* e.printStackTrace(); */ }
  }

  protected boolean canHandleDrop( DropTargetDropEvent dtde ) {
    System.out.println("hello");
    return false;
  }

  protected void handleDrop( DropTargetDropEvent dtde )
                                            throws UnsupportedFlavorException,
                                                   IOException {
  }
  

  public void dragEnter( DropTargetDragEvent e ) {}
  public void dragExit( DropTargetEvent e ) {}
  public void dragOver( DropTargetDragEvent e ) {}
  public void dropActionChanged( DropTargetDragEvent e ) {}

  public void changeDir( File toDir ) {
    if ( !toDir.isDirectory() ) {
      toDir = toDir.getParentFile();
    }

    if ( toDir == null ) {
      return;
    }

    // tokenize the file
    try {
      String fullPath = toDir.getCanonicalPath();
      StringTokenizer tok = 
        new StringTokenizer( fullPath, System.getProperty("file.separator") );

      // get root
      FileTreeNode root = (FileTreeNode)treeModel.getRoot(); 
      Enumeration rootEnum = root.children();

      Vector vector = new Vector();
      vector.add( root );

      FileTreeNode child = null;

      if ( tok.hasMoreTokens() ) {
        String pathToken = tok.nextToken();

        // search root for drive
        while ( rootEnum.hasMoreElements() ) {
            child = (FileTreeNode)rootEnum.nextElement();
          if ( child != null && child.toString().startsWith(pathToken) ) {
            vector.add( child );
            break;
          }
        }
      }

      final TreePath tp = new TreePath( vector.toArray() );
      Thread t = new Thread() { 
        public void run() {
          //System.out.println(tp);
          expandPath( tp );
            setSelectionPath( tp );
            scrollPathToVisible( tp );
        }
      };

      SwingUtilities.invokeAndWait( t );

      // for the rest of the path
      while ( tok.hasMoreElements() ) {
        String pathToken = tok.nextToken();

        Enumeration childEnum = child.children();
        while ( childEnum.hasMoreElements() ) {
          child = (FileTreeNode)childEnum.nextElement();
          if ( child.toString().equals(pathToken) ) {
            vector.add( child );
            break;
          }
        }

        final TreePath tp2 = new TreePath( vector.toArray() );
        t = new Thread() { 
          public void run() {
            //System.out.println(tp2);
            expandPath( tp2 );
            setSelectionPath( tp2 );
            scrollPathToVisible( tp2 );
          }
        };

        SwingUtilities.invokeAndWait( t );
      }
    }
    catch ( Exception e ) {}
  }

  public static boolean showHiddenFiles() { return showHiddenFiles; }

  public static void main( String[] args ) {
    JFrame frame = new JFrame( "filetree" );
    frame.getContentPane().setLayout( new BorderLayout() );
    frame.getContentPane().add( new FileTree(), BorderLayout.CENTER );
    frame.setSize( 640, 480 );
    frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
    frame.setVisible( true );
  }
}

