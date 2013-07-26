
//*****************************************************************************
//*
//* (c) Copyright 2005. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LocalFilePopupMenu.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class LocalFilePopupMenu extends JPopupMenu {
  protected static final long serialVersionUID = 1L;
  private boolean macintosh = Util.isMacOS();

  private int keyMask = ( macintosh ) ? Event.META_MASK : Event.CTRL_MASK;
  
  private LocalFileTable listing;

  private JMenuItem uploadMI = null;
  private JMenuItem deleteMI = null;
  private JMenuItem renameMI = null;

  public LocalFilePopupMenu( LocalFileTable listing ) {
    super();
    this.listing = listing;
    setupPopupMenu();
    updateMenu();
  }

  protected void setupPopupMenu() {
   String mi = LString.getString("Common.button.upload", "Upload");
   ImageIcon icon =
     new ImageIcon(getClass().getResource("images/upload_sm.png"));
   uploadMI = new JMenuItem( mi, icon );
   uploadMI.setFont( new Font(getFont().getName(), Font.BOLD, 
                                getFont().getSize()) );
   uploadMI.addActionListener( new ActionListener() {
     public void actionPerformed( ActionEvent e ) {
       Client.getLocalView().uploadSelected();
     }
   } );
   add( uploadMI );

   addSeparator();

   mi = LString.getString("Menu.remote.rename", "Rename...");
   icon =
     new ImageIcon(getClass().getResource("images/rename_sm.png"));
   renameMI = new JMenuItem( mi, icon );
   renameMI.addActionListener( new ActionListener() {
     public void actionPerformed( ActionEvent e ) {
       LocalView lv = Client.getLocalView();
       ArrayList selectedFiles = lv.getSelectedFiles();
       LRenameCommand cmd = new LRenameCommand();
       for( int i = 0; i < selectedFiles.size(); i++ ) {
         ArrayList args = new ArrayList(1);
         args.add( selectedFiles.get(i) );
         cmd.setArgs( args );
         SecureFTP.getCommandDispatcher().fireCommand( this, cmd );
       }
     }
   } );
   add( renameMI );

   mi = LString.getString("Common.button.delete", "Delete");
   icon = new ImageIcon(getClass().getResource("images/delete_sm.png"));
   deleteMI = new JMenuItem( mi, icon );
   //deleteMI.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0) );
   deleteMI.addActionListener( new ActionListener() {
     public void actionPerformed( ActionEvent e ) {
       ArrayList fl = Client.getLocalView().getSelectedFiles();
       LMDeleteCommand mdc = new LMDeleteCommand();
       ArrayList args = new ArrayList(1);
       args.add( fl );
       mdc.setArgs( args );
       SecureFTP.getCommandDispatcher().fireMTCommand( this, mdc );
     }
   } );
   add( deleteMI );

   addSeparator();

   mi = LString.getString("Menu.remote.new_folder", "New Folder...");
   icon = new ImageIcon(getClass().getResource("images/new_folder_sm.png"));
   JMenuItem jmi = new JMenuItem( mi, icon );
   jmi.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_N, 
                                              keyMask | Event.SHIFT_MASK) );
   jmi.addActionListener( new ActionListener() {
     public void actionPerformed( ActionEvent e ) {
       SecureFTP.getCommandDispatcher().fireCommand(this, new LMkDirCommand());
     }
   } );
   add( jmi );

   mi = LString.getString("Menu.remote.chdir", "Go to Folder...");
   icon = new ImageIcon(getClass().getResource("images/chdir_sm.png"));
   jmi = new JMenuItem( mi, icon );
   jmi.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_G,
                                              keyMask | Event.SHIFT_MASK) );
   jmi.addActionListener( new ActionListener() {
     public void actionPerformed( ActionEvent e ) {
       File localDir = Client.getLocalView().getCurrentDirectory();
       JFileChooser fc = new JFileChooser( localDir );
       fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
       int result =
       fc.showDialog( SecureFTP.getBaseFrame(),
                      LString.getString("Common.button.select_folder",
                                        "Select Folder") );
       if ( JFileChooser.APPROVE_OPTION == result ) {
         Client.getLocalView().changeDirectory( fc.getSelectedFile() );
       }
     }
   } );
   add( jmi );

   addSeparator();

   mi = LString.getString("Common.button.select_all", "Select All");
   icon = new ImageIcon(getClass().getResource("images/select_all_sm.png"));
   jmi = new JMenuItem( mi, icon );
   jmi.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_A, 
                                              keyMask | Event.SHIFT_MASK) );
   jmi.addActionListener( new ActionListener() {
     public void actionPerformed( ActionEvent e ) {
       listing.selectAll();
     }
   } );
   add( jmi );

   mi = LString.getString("Common.button.refresh", "Refresh");
   icon = new ImageIcon(getClass().getResource("images/refresh_sm.png"));
   jmi = new JMenuItem( mi, icon );
   jmi.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_R,
                       keyMask | Event.SHIFT_MASK) );
   jmi.addActionListener( new ActionListener() {
     public void actionPerformed( ActionEvent e ) {
       Client.getLocalView().refresh();
       Client.getLocalView().setFocus();
     }
   } );
   add( jmi );
  }

  public void updateMenu() {
    FTPSession session = FTPSessionManager.getInstance().getCurrentSession();

    ArrayList selectedFiles = listing.getSelectedFiles();
    boolean atLeastOneSelected = selectedFiles.size() > 0;

    uploadMI.setEnabled( Client.getAllowUpload() && 
                         atLeastOneSelected && null != session );
    deleteMI.setEnabled( atLeastOneSelected );
    renameMI.setEnabled( atLeastOneSelected );
  }
}

