//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: ToolBar.java 131 2009-12-11 01:23:42Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.secureftp.bean.*;
import com.glub.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class ToolBar extends JToolBar {
  protected static final long serialVersionUID = 1L;
  private JButton openButton = null;
  private JButton closeButton = null;
  private JButton downloadButton = null;
  private JButton uploadButton = null;
  private JButton renameButton = null;
  private JButton deleteButton = null;
  private JButton newFolderButton = null;
  private JButton changeFolderButton = null;
  private JButton refreshButton = null;
  private JButton stopButton = null;

  private boolean toolsAreDisabled = false;

  private ArrayList buttonList = new ArrayList();

  public ToolBar() {
    super( "Secure FTP", JToolBar.HORIZONTAL );
    setOpaque( true );
    setFloatable( false );
    setRollover( true );
    setupToolBar();
    setBackground( Color.DARK_GRAY );

    Client.fixComponentColor( this );
  }

  public void updateUI() {
    // i'm overriding the L&F to have the gradient
  }

  public void setupDefaultFocus() {
    if ( null != openButton ) {
      openButton.requestFocus();
      updateUI();
    }
  }

  private JButton createButton( String name, LString tip, 
                                boolean enabled, boolean focusable ) {
    ImageIcon icon = new ImageIcon( getClass().getResource("images/" + name + ".png") );
    ImageIcon pressed = new ImageIcon( getClass().getResource("images/" + name + "_sel.png") );
    ImageIcon rollover = new ImageIcon( getClass().getResource("images/" + name + "_over.png") );

    JButton button = new JButton( icon );
    button.setPressedIcon( pressed );
    button.setRolloverIcon( rollover );
    button.setEnabled( enabled );
    button.setOpaque( true );
    button.setFocusable( true );
    button.setVerticalTextPosition( SwingConstants.BOTTOM );
    button.setHorizontalTextPosition( SwingConstants.CENTER );
    button.setBorder( BorderFactory.createEmptyBorder(5, 5, 5, 5) );
    button.setContentAreaFilled( false );
    button.setToolTipText( tip.getString() );

    return button;
  }

  private void setupToolBar() {
    openButton = 
      createButton( "connect", 
                     new LString("Toolbar.tooltip.connect", 
                                 "Connect to an FTP server"), 
                     true, true );
    openButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        OpenCommand oc = new OpenCommand();
        if (Client.getClientType() == Client.APPLET &&
            Client.applet.getAutoConnectBookmark() != null) {
          ArrayList args = new ArrayList(1);
          args.add( Client.applet.getAutoConnectBookmark() );
          oc.setArgs(args);
        }
        SecureFTP.getCommandDispatcher().fireCommand( this, oc );
      }
    } );

    buttonList.add( openButton );

    closeButton = 
      createButton( "disconnect", 
                     new LString("Toolbar.tooltip.disconnect", 
                                 "Disconnect from an FTP server"), 
                     false, false );
    closeButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        SecureFTP.getCommandDispatcher().fireCommand( this, 
		                                      new CloseCommand() );
      }
    } );

    buttonList.add( closeButton );

    downloadButton = 
      createButton( "download",
                    new LString("Common.tooltip.download",
                                "Download a file or folder"),
                    false, false );
    downloadButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        FTPSession session = 
          FTPSessionManager.getInstance().getCurrentSession();
        RemotePanel rp = (RemotePanel)session.getRemoteUI();
        rp.downloadSelected();
      }
    } );

    if ( Client.getAllowDownload() )
      buttonList.add( downloadButton );

    uploadButton = 
      createButton( "upload",
                    new LString("Common.tooltip.upload",
                                "Upload a file or folder"),
                    false, false );
    uploadButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        Client.getLocalView().uploadSelected();
      }
    } );

    if ( Client.getAllowUpload() )
      buttonList.add( uploadButton );

    renameButton = 
      createButton( "rename",
                    new LString("Common.tooltip.rename",
                                "Rename a file or folder"),
                    false, false );
    renameButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        FTPSession session = 
          FTPSessionManager.getInstance().getCurrentSession();
        RemotePanel rp = (RemotePanel)session.getRemoteUI();
        RemoteFileList selectedFiles =
          rp.getTableView().getSelectedFiles();
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

    buttonList.add( renameButton );

    deleteButton = 
      createButton( "delete",
                     new LString("Common.tooltip.delete",
                                 "Delete a file or folder"),
                     false, false );
    deleteButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        FTPSession session = 
          FTPSessionManager.getInstance().getCurrentSession();
        RemotePanel rp = (RemotePanel)session.getRemoteUI();
        RemoteFileList selectedFiles =
          rp.getTableView().getSelectedFiles();
        MDeleteCommand cmd = new MDeleteCommand();
        ArrayList args = new ArrayList(2);
        args.add( selectedFiles );
        args.add( session );
        cmd.setArgs( args );
        SecureFTP.getCommandDispatcher().fireMTCommand( this, cmd );
      }
    } );

    buttonList.add( deleteButton );

    newFolderButton = 
      createButton( "new_folder",
                    new LString("Common.tooltip.new_folder", 
                             "Create a new folder"),
                    false, false );
    newFolderButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        SecureFTP.getCommandDispatcher().fireMTCommand( this, 
		                                        new MkDirCommand() );
      }
    } );

    buttonList.add( newFolderButton );

    changeFolderButton = 
      createButton( "chdir",
                    new LString("Common.tooltip.chdir",
                                "Go to a new folder"),
                    false, false );
    changeFolderButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        SecureFTP.getCommandDispatcher().fireMTCommand( this, new CDCommand() );
      }
    } );

    buttonList.add( changeFolderButton );

    refreshButton = 
      createButton( "refresh",
                     new LString("Common.tooltip.refresh",
                                 "Refresh directory listing"),
                     false, false );
    refreshButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        SecureFTP.getCommandDispatcher().fireMTCommand( this, new LsCommand() );
      }
    } );

    buttonList.add( refreshButton );

    JButton helpButton = 
      createButton( "help",
                    new LString("Menu.help", "Help"),
                    true, false );
    helpButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        HelpCommand hc = new HelpCommand();
        ArrayList args = new ArrayList(1);
        args.add(e);
        hc.setArgs(args);
        SecureFTP.getCommandDispatcher().fireCommand( this, hc );
      }
    } );

    stopButton = 
      createButton( "stop",
                    new LString("Common.tooltip.stop",
                                "Cancel operation"),
                    false, false );
    stopButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        final FTPSession session = 
          FTPSessionManager.getInstance().getCurrentSession();
        if ( session != null && session.getAbortableListTransfer() != null ) {
          final FTPAbortableTransfer abort = session.getAbortableListTransfer();
          Thread t = new Thread() {
            public void run() {
              try {
              session.getFTPBean().abort( abort );
              }
              catch ( FTPException fe ) {}
            }
          };
          t.start();
        }
      }
    } );

    buttonList.add( stopButton );

    add( openButton );
    add( closeButton );

    addSeparator();

    if ( Client.getAllowUpload() )
      add( uploadButton );

    if ( Client.getAllowDownload() )
      add( downloadButton );

    if ( Client.getAllowUpload() || Client.getAllowDownload() )
      addSeparator();

    add( stopButton );
    add( refreshButton );
    add( newFolderButton );
    add( changeFolderButton );
    add( renameButton );
    add( deleteButton );
    addSeparator();
    add( helpButton );
  }

  public void updateToolBar() {
    if ( toolsAreDisabled ) {
      return;
    }

    FTPSessionManager ftpsMgr = FTPSessionManager.getInstance();

    if ( null == ftpsMgr ) {
      disableToolBar();
      return;
    }

    openButton.setEnabled( true );

    FTPSession session = ftpsMgr.getCurrentSession();

    if ( session == null ) {
      disableToolBar();
      openButton.setEnabled( true );
      return;
    }

    closeButton.setEnabled( ftpsMgr.hasOpenSessions() );  

    if ( null != session ) {
      FTP bean = session.getFTPBean();

      if ( null != bean ) {
        int xferMode = bean.getTransferMode();

        boolean autoMode = false;
	boolean asciiMode = false;
	boolean binaryMode = false;

	switch ( xferMode ) {
          case FTP.AUTO_TRANSFER_MODE:
            autoMode = true;
	    break;

	  case FTP.ASCII_TRANSFER_MODE:
            asciiMode = true;
	    break;

	  case FTP.BINARY_TRANSFER_MODE:
            binaryMode = true;
	    break;
	}

        RemotePanel remotePanel = (RemotePanel)session.getRemoteUI();

        LocalView localView = Client.getLocalView();
        boolean atLeastOneLocalSelected = 
          localView.getSelectedFiles().size() > 0;

        if ( null != remotePanel ) {
          RemoteFileList selectedFiles =
            remotePanel.getTableView().getSelectedFiles();

          boolean atLeastOneSelected = selectedFiles.size() > 0;

          renameButton.setEnabled( atLeastOneSelected );
          deleteButton.setEnabled( atLeastOneSelected );
          downloadButton.setEnabled( atLeastOneSelected && 
                                     Client.getAllowDownload() &&
                                     null != session.getLocalDir() );

          uploadButton.setEnabled( atLeastOneLocalSelected &&
                                   Client.getAllowUpload() && 
                                   null != session.getLocalDir() );

        }
      }

      refreshButton.setEnabled( session.isLoggedIn() );
      newFolderButton.setEnabled( session.isLoggedIn() );
      changeFolderButton.setEnabled( session.isLoggedIn() );
      stopButton.setEnabled( null != session.getAbortableListTransfer() );
    }
  }

  public synchronized void enableToolBar() {
    toolsAreDisabled = false;
    updateToolBar();
  }

  public synchronized void disableToolBar() {
    toolsAreDisabled = true;

    for( int i = 0; i < buttonList.size(); i++ ) {
      ((JButton)buttonList.get(i)).setEnabled( false );
    }
  }

  protected void paintComponent( Graphics g ) {
    Graphics2D g2d = (Graphics2D)g;
    int w = getWidth();
    int h = getHeight();

    GradientPaint gp = 
      new GradientPaint( 0, 0, new Color( 222, 222, 222 ),
                         0, h, new Color( 160, 160, 160) );
    g2d.setPaint( gp );
    g2d.fillRect( 0, 0, w, h );

    setOpaque( false );
    super.paintComponent( g );
    setOpaque( true );
  }
}

