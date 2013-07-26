
//*****************************************************************************
//*
//* (c) Copyright 2004. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: RemoteFilePopupMenu.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.secureftp.bean.*;
import com.glub.util.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class RemoteFilePopupMenu extends JPopupMenu {
  protected static final long serialVersionUID = 1L;
  private boolean macintosh = Util.isMacOS();

  private int keyMask = ( macintosh ) ? Event.META_MASK : Event.CTRL_MASK;
  
  private RemoteFileTable listing;

  private JMenuItem downloadMI = null;
  private JMenuItem deleteMI = null;
  private JMenuItem renameMI = null;
  private JMenuItem infoMI = null;

  public RemoteFilePopupMenu( RemoteFileTable listing ) {
    super();
    this.listing = listing;
    setupPopupMenu();
    updateMenu();
  }

  protected void setupPopupMenu() {
   String mi = LString.getString("Common.button.download", "Download");
   ImageIcon icon = 
     new ImageIcon(getClass().getResource("images/download_sm.png"));
   downloadMI = new JMenuItem( mi, icon );
   downloadMI.setFont( new Font(getFont().getName(), Font.BOLD, 
                                getFont().getSize()) );
   downloadMI.addActionListener( new ActionListener() {
     public void actionPerformed( ActionEvent e ) {
       FTPSession session =
         FTPSessionManager.getInstance().getCurrentSession();
       RemotePanel rp = (RemotePanel)session.getRemoteUI();
       rp.downloadSelected();
     }
   } );
   add( downloadMI );

   addSeparator();

   mi = LString.getString("Menu.remote.info", "Get Info");
   icon = new ImageIcon(getClass().getResource("images/info_sm.png"));
   infoMI = new JMenuItem( mi, icon );
   if ( Util.isWindows() ) {
     infoMI.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Event.ALT_MASK) );
   }
   else {
     infoMI.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_I, keyMask) );
   }

   infoMI.addActionListener( new ActionListener() {
     public void actionPerformed( ActionEvent e ) {
       FTPSession session =
         FTPSessionManager.getInstance().getCurrentSession();
       RemotePanel rp = (RemotePanel)session.getRemoteUI();
       RemoteFileList selectedFiles =
         rp.getTableView().getSelectedFiles();

       InfoCommand cmd = new InfoCommand();
       ArrayList args = new ArrayList(1);
       args.add( selectedFiles.get(0) );
       cmd.setArgs( args );
       SecureFTP.getCommandDispatcher().fireCommand( this, cmd );
     }
   } );
   add ( infoMI );

   mi = LString.getString("Menu.remote.rename", "Rename...");
   icon = new ImageIcon(getClass().getResource("images/rename_sm.png"));
   renameMI = new JMenuItem( mi, icon );
   renameMI.addActionListener( new ActionListener() {
     public void actionPerformed( ActionEvent e ) {
/*
    	 FTPSession session =
         FTPSessionManager.getInstance().getCurrentSession();
*/
       RemoteFileList selectedFiles = listing.getSelectedFiles();
       RenameCommand cmd = new RenameCommand();
       ArrayList args = new ArrayList(1);
       for( int i = 0; i < selectedFiles.size(); i++ ) {
         args.clear();
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
       MDeleteCommand cmd = new MDeleteCommand();
       ArrayList args = new ArrayList(2);
       args.add( listing.getSelectedFiles() );
       args.add( FTPSessionManager.getInstance().getCurrentSession() );
       cmd.setArgs( args );
       SecureFTP.getCommandDispatcher().fireMTCommand( this, cmd );
     }
   } );
   add( deleteMI );

   addSeparator();

   mi = LString.getString("Menu.remote.new_folder", "New Folder...");
   icon = new ImageIcon(getClass().getResource("images/new_folder_sm.png"));
   JMenuItem jmi = new JMenuItem( mi, icon );
   jmi.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_N, keyMask) );
   jmi.addActionListener( new ActionListener() {
     public void actionPerformed( ActionEvent e ) {
       SecureFTP.getCommandDispatcher().fireMTCommand(this, new MkDirCommand());
     }
   } );
   add( jmi );

   mi = LString.getString("Menu.remote.chdir", "Go to Folder...");
   icon = new ImageIcon(getClass().getResource("images/chdir_sm.png"));
   jmi = new JMenuItem( mi, icon );
   jmi.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_G, keyMask) );
   jmi.addActionListener( new ActionListener() {
     public void actionPerformed( ActionEvent e ) {
       SecureFTP.getCommandDispatcher().fireMTCommand( this, new CDCommand() );
     }
   } );
   add( jmi );

   addSeparator();

   mi = LString.getString("Common.button.select_all", "Select All");
   icon = new ImageIcon(getClass().getResource("images/select_all_sm.png"));
   jmi = new JMenuItem( mi, icon );
   jmi.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_A, keyMask) );
   jmi.addActionListener( new ActionListener() {
     public void actionPerformed( ActionEvent e ) {
       listing.selectAll();
     }
   } );
   add( jmi );

   mi = LString.getString("Common.button.refresh", "Refresh");
   icon = new ImageIcon(getClass().getResource("images/refresh_sm.png"));
   jmi = new JMenuItem( mi, icon );
   jmi.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_R, keyMask) );
   jmi.addActionListener( new ActionListener() {
     public void actionPerformed( ActionEvent e ) {
       SecureFTP.getCommandDispatcher().fireMTCommand( this, new LsCommand() );
     }
   } );
   add( jmi );
  }

  public void updateMenu() {
    RemoteFileList selectedFiles = listing.getSelectedFiles();
    boolean atLeastOneSelected = selectedFiles.size() > 0;

    downloadMI.setEnabled( Client.getAllowDownload() && atLeastOneSelected );
    deleteMI.setEnabled( atLeastOneSelected );
    renameMI.setEnabled( atLeastOneSelected );
    infoMI.setEnabled( selectedFiles.size() == 1 );
  }
}

