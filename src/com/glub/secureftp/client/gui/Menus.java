//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: Menus.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import com.glub.secureftp.bean.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class Menus extends JMenuBar {
  protected static final long serialVersionUID = 1L;
  private JMenu fileMenu = null;
  private JMenu remoteMenu = null;
  private JMenu transferMenu = null;
  private JMenu bookmarksMenu = null;
  private JMenu windowMenu = null;
  private JMenu helpMenu = null;

  private ButtonGroup transferGroup = new ButtonGroup();
  private ButtonGroup windowGroup = new ButtonGroup();

  private JMenuItem disconnectMI = null;

  private JMenuItem downloadMI = null;
  private JMenuItem uploadMI = null;

  private JMenuItem localRenameMI = null;
  private JMenuItem localDeleteMI = null;

  private JMenuItem remoteRefreshMI = null;
  private JMenuItem remoteMkDirMI = null;
  private JMenuItem remoteCDMI = null;
  private JMenuItem remoteRenameMI = null;
  private JMenuItem remoteDeleteMI = null;
  private JMenuItem remoteRawMI = null;
  private JMenuItem remoteInfoMI = null;
  private JMenuItem remoteSelectAllMI = null;

  private JMenuItem autoMI = null;
  private JMenuItem asciiMI = null;
  private JMenuItem binaryMI = null;
  private JMenuItem ebcdicMI = null;

  private JMenuItem certMI = null;

  private boolean macintosh = Util.isMacOS();

  private boolean menusAreDisabled = false;

  private int keyMask = ( macintosh ) ? Event.META_MASK : Event.CTRL_MASK;

  public Menus() {
    super();

    add( getFileMenu() );
    add( getRemoteMenu() );
    add( getBookmarksMenu() );
    add( getWindowMenu() );
    add( getHelpMenu() );
  }

  private JMenu getFileMenu() {
    if ( null == fileMenu ) {
      fileMenu = new JMenu( LString.getString("Menu.file", "File", true) );
      if ( !macintosh ) {
        fileMenu.setMnemonic( LString.getString("Menu.mnemonic.file", 
                              "F").charAt(0) );
      }

      fileMenu.setEnabled( true );

     ImageIcon icon = 
       new ImageIcon(getClass().getResource("images/connect_sm.png"));

      JMenuItem connectMI = 
        new JMenuItem( LString.getString("Menu.file.connect", "Connect...", 
                                         true), icon );
      if ( !macintosh ) {
        connectMI.setMnemonic( LString.getString("Menu.file.mnemonic.connect",
                               "C").charAt(0) );
      }
      connectMI.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_O, 
                                                       keyMask) );
      connectMI.setEnabled( true );
      connectMI.addActionListener( new ActionListener() {
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

     icon = 
       new ImageIcon(getClass().getResource("images/disconnect_sm.png"));

      disconnectMI = 
        new JMenuItem( LString.getString("Menu.file.disconnect", 
                                         "Disconnect", true), icon );
      if ( !macintosh ) {
        disconnectMI.setMnemonic( 
          LString.getString("Menu.file.mnemonic.disconnect", "D").charAt(0) );
      }
      disconnectMI.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_W, 
                                                          keyMask) );
      disconnectMI.setEnabled( false );
      disconnectMI.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          SecureFTP.getCommandDispatcher().fireCommand( this, 
		                                        new CloseCommand() );
	  boolean hasOpenSessions = 
            FTPSessionManager.getInstance().hasOpenSessions();
          disconnectMI.setEnabled( hasOpenSessions );
	}
      } );

      icon = 
        new ImageIcon(getClass().getResource("images/prefs_sm.png"));

      JMenuItem prefsMI = 
        new JMenuItem( LString.getString("Menu.file.prefs", "Preferences", 
                                         true), icon );
      if ( !macintosh ) {
        prefsMI.setMnemonic( LString.getString("Menu.file.mnemonic.prefs", 
                                              "P").charAt(0) );
      }
      prefsMI.setEnabled( true );
      prefsMI.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          SecureFTP.getCommandDispatcher().fireCommand(this, 
		                                      new PreferencesCommand());
	}
      } );

      JMenuItem exitMI = 
        new JMenuItem( LString.getString("Menu.file.exit", "Exit", true) );
      if ( !macintosh ) {
        exitMI.setMnemonic( LString.getString("Menu.file.mnemonic.exit", 
                                              "x").charAt(0) );
      }
      exitMI.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_Q, keyMask) );
      exitMI.setEnabled( true );
      exitMI.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          SecureFTP.getCommandDispatcher().fireCommand( this, 
		                                        new ExitCommand() );
	}
      } );

     icon = 
       new ImageIcon(getClass().getResource("images/refresh_sm.png"));

      JMenuItem refreshMI = 
        new JMenuItem( LString.getString("Common.button.refresh", "Refresh", 
                                         true), icon );
      if ( !macintosh ) {
        refreshMI.setMnemonic( LString.getString("Menu.remote.mnemonic.refresh",
                               "R").charAt(0) );
      }
      refreshMI.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_R, 
                                keyMask | Event.SHIFT_MASK) );
      refreshMI.setEnabled( true );
      refreshMI.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          Client.getLocalView().refresh();
          Client.getLocalView().setFocus();
	}
      } );

     icon = 
       new ImageIcon(getClass().getResource("images/chdir_sm.png"));

      JMenuItem cdMI =
        new JMenuItem(LString.getString("Menu.remote.chdir", 
                                        "Go to Folder...", true), icon);
      if ( !macintosh ) {
        cdMI.setMnemonic( LString.getString("Menu.file.mnemonic.chdir",
                                            "G").charAt(0) );
      } 
      cdMI.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_G, 
                           keyMask | Event.SHIFT_MASK) );
      cdMI.setEnabled( true );
      cdMI.addActionListener( new ActionListener() {
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

     icon = 
       new ImageIcon(getClass().getResource("images/new_folder_sm.png"));

      JMenuItem mkDirMI =
        new JMenuItem(LString.getString("Menu.remote.new_folder",
                                        "New Folder...", true), icon);
      if ( !macintosh ) {
       mkDirMI.setMnemonic( LString.getString("Menu.remote.mnemonic.new_folder",
                                              "N").charAt(0) );
      } 
      mkDirMI.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_N, 
                           keyMask | Event.SHIFT_MASK) );
      mkDirMI.setEnabled( true );
      mkDirMI.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          SecureFTP.getCommandDispatcher().fireCommand( this, 
		                                        new LMkDirCommand() );
        }
      } );

      icon = 
        new ImageIcon(getClass().getResource("images/rename_sm.png"));

      localRenameMI = 
        new JMenuItem( LString.getString("Menu.remote.rename", "Rename...", 
                                         true), icon );
      if ( !macintosh ) {
        localRenameMI.setMnemonic(
           LString.getString("Menu.local.mnemonic.rename", "e").charAt(0) );
      }
      localRenameMI.setEnabled( false );
      localRenameMI.addActionListener( new ActionListener() {
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

      icon = 
        new ImageIcon(getClass().getResource("images/delete_sm.png"));

      localDeleteMI = 
        new JMenuItem( LString.getString("Common.button.delete", "Delete", 
                                         true), icon );
      if ( !macintosh ) {
        localDeleteMI.setMnemonic(
                                LString.getString("Menu.local.mnemonic.delete",
                                                  "t").charAt(0) );
      }
      localDeleteMI.setEnabled( false );
      localDeleteMI.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          ArrayList fl = Client.getLocalView().getSelectedFiles();
          LMDeleteCommand mdc = new LMDeleteCommand();
          ArrayList args = new ArrayList(1);
          args.add( fl );
          mdc.setArgs( args );
          SecureFTP.getCommandDispatcher().fireMTCommand( this, mdc );
	}
      } );

      icon = 
        new ImageIcon(getClass().getResource("images/select_all_sm.png"));

      JMenuItem localSelectAllMI = 
        new JMenuItem( LString.getString("Common.button.select_all", 
                                         "Select All", true), icon );
      localSelectAllMI.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_A, 
                                       keyMask | Event.SHIFT_MASK) );
      if ( !macintosh ) {
        localSelectAllMI.setMnemonic(
                                LString.getString("Menu.local.mnemonic.select",
                                                  "A").charAt(0) );
      }
      localSelectAllMI.setEnabled( true );
      localSelectAllMI.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          Client.getLocalView().selectAll();
	}
      } );

      fileMenu.add( connectMI );
      fileMenu.add( disconnectMI );

      fileMenu.addSeparator();

      fileMenu.add( localSelectAllMI );
      fileMenu.add( refreshMI );
      fileMenu.add( mkDirMI );
      fileMenu.add( cdMI );
      fileMenu.add( localRenameMI );
      fileMenu.add( localDeleteMI );

      if ( !macintosh || Client.getClientType() == Client.APPLET ) {
        fileMenu.addSeparator();
        fileMenu.add( prefsMI );
        if ( Client.getClientType() == Client.APPLICATION )
          fileMenu.add( exitMI );
      }
    }    

    return fileMenu;
  }

  private JMenu getRemoteMenu() {
    if ( null == remoteMenu ) {
      remoteMenu = new JMenu( LString.getString("Menu.remote", "Remote") );
      if ( !macintosh ) {
        remoteMenu.setMnemonic( LString.getString("Menu.mnemonic.remote", 
                                "R").charAt(0) );
      }

      remoteMenu.setEnabled( false );

      ImageIcon icon = 
        new ImageIcon(getClass().getResource("images/download_sm.png"));

      downloadMI = 
        new JMenuItem(LString.getString("Common.button.download", "Download", 
                                        true), icon);
      if ( !macintosh ) {
        downloadMI.setMnemonic(
                              LString.getString("Menu.remote.mnemonic.download",
                                                "D").charAt(0) );
      }
      downloadMI.setEnabled( false );
      downloadMI.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          FTPSession session =
            FTPSessionManager.getInstance().getCurrentSession();
          RemotePanel rp = (RemotePanel)session.getRemoteUI();
          rp.downloadSelected();
	}
      } );

      icon = 
        new ImageIcon(getClass().getResource("images/upload_sm.png"));

      uploadMI = 
        new JMenuItem(LString.getString("Common.button.upload", "Upload", 
                                        true), icon);
      if ( !macintosh ) {
        uploadMI.setMnemonic( LString.getString("Menu.remote.mnemonic.upload",
                                                "U").charAt(0) );
      }
      uploadMI.setEnabled( false );
      uploadMI.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          Client.getLocalView().uploadSelected();
	}
      } );

      icon = 
        new ImageIcon(getClass().getResource("images/refresh_sm.png"));

      remoteRefreshMI = 
        new JMenuItem( LString.getString("Common.button.refresh", "Refresh", 
                                         true), icon );
      if ( !macintosh ) {
        remoteRefreshMI.setMnemonic(
                               LString.getString("Menu.remote.mnemonic.refresh",
                                                 "R").charAt(0) );
      }
      remoteRefreshMI.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_R, 
                                      keyMask) );
      remoteRefreshMI.setEnabled( false );
      remoteRefreshMI.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          SecureFTP.getCommandDispatcher().fireMTCommand( this, 
		                                          new LsCommand() );
	}
      } );

      icon = 
        new ImageIcon(getClass().getResource("images/new_folder_sm.png"));

      remoteMkDirMI = 
        new JMenuItem( LString.getString("Menu.remote.new_folder", 
                                         "New Folder...", true), icon );
      if ( !macintosh ) {
        remoteMkDirMI.setMnemonic(
                            LString.getString("Menu.remote.mnemonic.new_folder",
                                              "N").charAt(0) );
      }
      remoteMkDirMI.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_N, 
                                    keyMask) );
      remoteMkDirMI.setEnabled( false );
      remoteMkDirMI.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          SecureFTP.getCommandDispatcher().fireMTCommand( this, 
		                                          new MkDirCommand() );
	}
      } );

      icon = 
        new ImageIcon(getClass().getResource("images/chdir_sm.png"));

      remoteCDMI = 
        new JMenuItem( LString.getString("Menu.remote.chdir", 
                                         "Go to Folder...", true), icon );
      if ( !macintosh ) {
        remoteCDMI.setMnemonic(
                               LString.getString("Menu.local.mnemonic.chdir",
                                                 "G").charAt(0) );
      }
      remoteCDMI.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_G, 
                                 keyMask) );
      remoteCDMI.setEnabled( false );
      remoteCDMI.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          SecureFTP.getCommandDispatcher().fireMTCommand( this, 
                                                          new CDCommand() );
	}
      } );

      remoteRawMI = 
        new JMenuItem( LString.getString("Menu.remote.raw", 
                                         "Send Raw command...", true) );
      if ( !macintosh ) {
        remoteRawMI.setMnemonic(
                               LString.getString("Menu.remote.mnemonic.raw",
                                                 "w").charAt(0) );
      }
      remoteRawMI.setEnabled( false );
      remoteRawMI.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          SecureFTP.getCommandDispatcher().fireCommand( this, 
                                                        new RawCommand() );
	}
      } );

      icon = 
        new ImageIcon(getClass().getResource("images/info_sm.png"));

      remoteInfoMI = 
        new JMenuItem( LString.getString("Menu.remote.info", 
                                         "Get Info", true), icon );
      if ( !macintosh ) {
        remoteInfoMI.setMnemonic(
                               LString.getString("Menu.remote.mnemonic.info",
                                                 "I").charAt(0) );
      }
      if ( Util.isWindows() ) {
        remoteInfoMI.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 
                                     Event.ALT_MASK) );
      }
      else {
        remoteInfoMI.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_I, 
                                     keyMask) );
      }
      remoteInfoMI.setEnabled( false );
      remoteInfoMI.addActionListener( new ActionListener() {
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

      icon = 
        new ImageIcon(getClass().getResource("images/rename_sm.png"));

      remoteRenameMI = 
        new JMenuItem( LString.getString("Menu.remote.rename", "Rename...", 
                                         true), icon );
      if ( !macintosh ) {
        remoteRenameMI.setMnemonic(
                                LString.getString("Menu.local.mnemonic.rename",
                                                  "e").charAt(0) );
      }
      remoteRenameMI.setEnabled( false );
      remoteRenameMI.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          FTPSession session = 
            FTPSessionManager.getInstance().getCurrentSession();
          RemotePanel rp = (RemotePanel)session.getRemoteUI();
          RemoteFileList selectedFiles =
            rp.getTableView().getSelectedFiles();
          RenameCommand cmd = new RenameCommand();
          for( int i = 0; i < selectedFiles.size(); i++ ) {
            ArrayList args = new ArrayList(1);
            args.add( selectedFiles.get(i) );
            cmd.setArgs( args );
            SecureFTP.getCommandDispatcher().fireCommand( this, cmd );
          }
	}
      } );

      icon = 
        new ImageIcon(getClass().getResource("images/delete_sm.png"));

      remoteDeleteMI = 
        new JMenuItem( LString.getString("Common.button.delete", "Delete", 
                                         true), icon );
      if ( !macintosh ) {
        remoteDeleteMI.setMnemonic(
                                LString.getString("Menu.local.mnemonic.delete",
                                                  "t").charAt(0) );
      }
      remoteDeleteMI.setEnabled( false );
      remoteDeleteMI.addActionListener( new ActionListener() {
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

      icon = 
        new ImageIcon(getClass().getResource("images/select_all_sm.png"));

      remoteSelectAllMI = 
        new JMenuItem( LString.getString("Common.button.select_all", 
                                         "Select All", true), icon );
      remoteSelectAllMI.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_A, 
                                        keyMask) );
      if ( !macintosh ) {
        remoteSelectAllMI.setMnemonic(
                                LString.getString("Menu.local.mnemonic.select",
                                                  "A").charAt(0) );
      }
      remoteSelectAllMI.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          FTPSession session =
            FTPSessionManager.getInstance().getCurrentSession();
          if ( session != null ) {
            RemotePanel rp = (RemotePanel)session.getRemoteUI();
            rp.getTableView().selectAll();
          }
	}
      } );

      if ( Client.getAllowUpload() )
        remoteMenu.add( uploadMI );

      if ( Client.getAllowDownload() )
        remoteMenu.add( downloadMI );

      if ( Client.getAllowUpload() || Client.getAllowDownload() )
        remoteMenu.addSeparator();

      remoteMenu.add( getTransferMenu() );
      remoteMenu.addSeparator();

      if ( Client.getAllowRaw() ) {
        remoteMenu.add( remoteRawMI );
        remoteMenu.addSeparator();
      }

      remoteMenu.add( remoteSelectAllMI );
      remoteMenu.add( remoteRefreshMI );
      remoteMenu.add( remoteMkDirMI );
      remoteMenu.add( remoteCDMI );
      remoteMenu.addSeparator();
      remoteMenu.add( remoteInfoMI );
      remoteMenu.add( remoteRenameMI );
      remoteMenu.add( remoteDeleteMI );
    }

    return remoteMenu;
  }

  private JMenu getBookmarksMenu() {
    if ( null == bookmarksMenu ) {
      bookmarksMenu = new JMenu( LString.getString("Menu.bookmarks", 
                                                   "Bookmarks") );
      if ( !macintosh ) {
        bookmarksMenu.setMnemonic( LString.getString("Menu.mnemonic.bookmarks", 
                                                     "B").charAt(0) );
      }

      bookmarksMenu.setEnabled( true );

      ImageIcon icon = 
        new ImageIcon(getClass().getResource("images/book_add_sm.png"));

      JMenuItem addBookMI = 
        new JMenuItem( LString.getString("Menu.bookmarks.add", 
                                         "Add Bookmark...", true), icon );
      if ( !macintosh ) {
        addBookMI.setMnemonic( 
          LString.getString("Menu.bookmarks.mnemonic.add", 
                            "A").charAt(0) );
      }
      addBookMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, keyMask));
      addBookMI.setEnabled( true );
      addBookMI.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          SecureFTP.getCommandDispatcher().fireCommand( this, 
		                                     new AddBookmarkCommand() );
	}
      } );

      icon = 
        new ImageIcon(getClass().getResource("images/book_edit_sm.png"));

      JMenuItem editBookMI = 
        new JMenuItem( LString.getString("Menu.bookmarks.edit", 
                                         "Manage Bookmarks...", true), icon );
      if ( !macintosh ) {
        editBookMI.setMnemonic( 
          LString.getString("Menu.bookmarks.mnemonic.edit", 
                            "M").charAt(0) );
      }
      editBookMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, keyMask));
      //boolean hasBookmarks = BookmarkManager.getInstance().hasBookmarks();
      boolean hasBookmarks = false;
      editBookMI.setEnabled( hasBookmarks );
      editBookMI.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          SecureFTP.getCommandDispatcher().fireCommand( this, 
		                                   new EditBookmarksCommand() );
	}
      } );

      bookmarksMenu.add( addBookMI );
      bookmarksMenu.add( editBookMI );
      bookmarksMenu.addSeparator();
      //updateBookmarks();
    }

    return bookmarksMenu;
  }

  public JMenu getTransferMenu() {
    if ( null == transferMenu ) {
      transferMenu = new JMenu( LString.getString("Menu.transfer", 
                                                  "Transfer Mode") );
      if ( !macintosh ) {
        transferMenu.setMnemonic( LString.getString("Menu.mnemonic.transfer", 
                                                     "T").charAt(0) );
      }

      transferMenu.setEnabled(false);

      autoMI =
        (JMenuItem)transferMenu.add( new JRadioButtonMenuItem(
           LString.getString("Common.xferMode.auto", "Auto", true)) );
      if ( !macintosh ) {
        autoMI.setMnemonic( LString.getString("Menu.transfer.mnemonic.auto", 
                                              "A").charAt(0) );
      }
      autoMI.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          SecureFTP.getCommandDispatcher().fireMTCommand( this,
                                                       new AutoXferCommand() );
	}
      } );

      asciiMI =
        (JMenuItem)transferMenu.add( new JRadioButtonMenuItem(
           LString.getString("Common.xferMode.ascii", "Text", true)) );
      if ( !macintosh ) {
        asciiMI.setMnemonic( LString.getString("Menu.transfer.mnemonic.ascii", 
                                               "T").charAt(0) );
      }
      asciiMI.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          SecureFTP.getCommandDispatcher().fireMTCommand( this,
                                                       new AsciiXferCommand() );
	}
      } );

      binaryMI =
        (JMenuItem)transferMenu.add( new JRadioButtonMenuItem(
           LString.getString("Common.xferMode.binary", "Binary", true)) );
      if ( !macintosh ) {
        binaryMI.setMnemonic( LString.getString("Menu.transfer.mnemonic.binary",
                                                "B").charAt(0) );
      }
      binaryMI.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          SecureFTP.getCommandDispatcher().fireMTCommand( this,
                                                      new BinaryXferCommand() );
	}
      } );

      ebcdicMI =
        (JMenuItem)transferMenu.add( new JRadioButtonMenuItem(
           LString.getString("Common.xferMode.ebcdic", "EBCDIC", true)) );
      if ( !macintosh ) {
        binaryMI.setMnemonic( LString.getString("Menu.transfer.mnemonic.ebcdic",
                                                "E").charAt(0) );
      }
      ebcdicMI.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          SecureFTP.getCommandDispatcher().fireMTCommand( this,
                                                      new EbcdicXferCommand() );
	}
      } );

      transferGroup.add( autoMI );
      transferGroup.add( asciiMI );
      transferGroup.add( binaryMI );
      transferGroup.add( ebcdicMI );
    }

    return transferMenu;
  }

  public JMenu getWindowMenu() {
    if ( null == windowMenu ) {
      windowMenu = new JMenu( LString.getString("Menu.window", "Window") );
      if ( !macintosh ) {
        windowMenu.setMnemonic( LString.getString("Menu.mnemonic.window", 
                                                     "W").charAt(0) );
      }

      windowMenu.setEnabled(FTPSessionManager.getInstance().hasOpenSessions());
    }

    return windowMenu;
  }

  public JMenu getHelpMenu() {
    if ( null == helpMenu ) {
      helpMenu = new JMenu( LString.getString("Menu.help", "Help") );
      if ( !macintosh ) {
        helpMenu.setMnemonic( LString.getString("Menu.mnemonic.help", 
                                                "H").charAt(0) );
      }

      helpMenu.setEnabled( true );

      ImageIcon icon = 
        new ImageIcon(getClass().getResource("images/help_sm.png"));

      JMenuItem helpMI = 
        new JMenuItem( LString.getString("Menu.help.help", 
                                         "Help...", true), icon );
      if ( !macintosh ) {
        helpMI.setMnemonic( 
          LString.getString("Menu.help.mnemonic.help", "H").charAt(0) );
        helpMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
      }
      else {
        helpMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH,
                                                     keyMask));
      }

      helpMI.setEnabled( true );
      helpMI.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          HelpCommand hc = new HelpCommand();
          ArrayList args = new ArrayList(1);
          args.add(e);
          hc.setArgs(args);
          SecureFTP.getCommandDispatcher().fireCommand( this, hc );
	}
      } );

      icon = 
        new ImageIcon(getClass().getResource("images/cert_sm.png"));

      certMI = 
        new JMenuItem(LString.getString("Menu.help.cert_manager", 
                                        "Manage Certificates...", true), icon);
      if ( !macintosh ) {
        certMI.setMnemonic( 
          LString.getString("Menu.help.mnemonic.cert_manager", "C").charAt(0) );
      }

      certMI.setEnabled( true );
      certMI.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          SecureFTP.getCommandDispatcher().fireCommand( this, 
		                              new CertificateManagerCommand() );
        }
      } );

      icon = 
        new ImageIcon(getClass().getResource("images/about_sm.png"));

      JMenuItem aboutMI = 
        new JMenuItem( LString.getString("Menu.help.about", 
                                         "About Secure FTP...", true), icon );
      if ( !macintosh ) {
        aboutMI.setMnemonic( 
          LString.getString("Menu.help.mnemonic.about", "A").charAt(0) );
      }
      aboutMI.setEnabled( true );
      aboutMI.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          SecureFTP.getCommandDispatcher().fireCommand( this, 
		                                        new AboutCommand() );
	}
      } );

      icon = 
        new ImageIcon(getClass().getResource("images/updates_sm.png"));

      helpMenu.add( helpMI );

      // handled in the application menu on the mac
      if ( !macintosh || Client.getClientType() == Client.APPLET ) {
        helpMenu.add( aboutMI );
      }

      helpMenu.addSeparator();
      helpMenu.add( certMI );
    }

    return helpMenu;
  }

  public void updateMenuBar() {
    if ( menusAreDisabled ) {
      return;
    }

    FTPSessionManager mgr = FTPSessionManager.getInstance();
    FTPSession session = mgr.getCurrentSession();
    boolean openSessions = mgr.hasOpenSessions();

    ArrayList localSelectedFiles =
      Client.getLocalView().getSelectedFiles();
    File currentLocalDir = 
      Client.getLocalView().getCurrentDirectory();
    boolean atLeastOneLocalSelected = localSelectedFiles.size() > 0 &&
                                      null != currentLocalDir;

    localRenameMI.setEnabled( atLeastOneLocalSelected );
    localDeleteMI.setEnabled( atLeastOneLocalSelected );
    uploadMI.setEnabled( atLeastOneLocalSelected && Client.getAllowUpload() );

    RemotePanel remotePanel = null;

    if ( null != session ) {
      remotePanel = (RemotePanel)session.getRemoteUI();
    }

    if ( null != remotePanel ) {
      RemoteFileList selectedFiles =
        remotePanel.getTableView().getSelectedFiles();

      boolean atLeastOneSelected = selectedFiles.size() > 0;
      remoteRenameMI.setEnabled( atLeastOneSelected );
      remoteDeleteMI.setEnabled( atLeastOneSelected );
      downloadMI.setEnabled( atLeastOneSelected && Client.getAllowDownload() &&
                             null != session.getLocalDir() );
      remoteInfoMI.setEnabled( selectedFiles.size() == 1 );
    }

    disconnectMI.setEnabled( openSessions );
    remoteRefreshMI.setEnabled( openSessions );
    remoteRawMI.setEnabled( openSessions );
    remoteMkDirMI.setEnabled( openSessions );
    remoteCDMI.setEnabled( openSessions );
    remoteSelectAllMI.setEnabled( openSessions );
    transferMenu.setEnabled( openSessions );
    windowMenu.setEnabled( openSessions );

    remoteMenu.setEnabled( openSessions );

    if ( openSessions && null != session && null != session.getFTPBean() ) {
      int xferMode = session.getFTPBean().getTransferMode();
    
      boolean autoMode = false;
      boolean asciiMode = false;
      boolean binaryMode = false;
      boolean ebcdicMode = false;
    
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
      
        case FTP.EBCDIC_TRANSFER_MODE:
          ebcdicMode = true;
          break;
      }

      autoMI.setSelected( autoMode );
      asciiMI.setSelected( asciiMode );
      binaryMI.setSelected( binaryMode );
      ebcdicMI.setSelected( ebcdicMode );
    }

    updateWindowMenu();
  }

  public synchronized void enableMenuBar() {
    menusAreDisabled = false;
    fileMenu.setEnabled( true );
    remoteMenu.setEnabled( true );
    bookmarksMenu.setEnabled( true );
    windowMenu.setEnabled( true );
    updateMenuBar();
  }

  public synchronized void disableMenuBar() {
    menusAreDisabled = true;
    fileMenu.setEnabled( false );
    remoteMenu.setEnabled( false );
    bookmarksMenu.setEnabled( false );
    windowMenu.setEnabled( false );
  }

  public void updateBookmarks() {
    JMenu menu = getBookmarksMenu();
    BookmarkManager bm = BookmarkManager.getInstance();

    int menuCount = menu.getItemCount();
    int numOfStandardMenuItems = 2;

    // enable/disable the Edit Bookmarks menu item (item 1)
    boolean hasBookmarks = BookmarkManager.getInstance().hasBookmarks();
    JMenuItem editBookmarkMI = menu.getItem(1);
    if ( null != editBookmarkMI ) {
      editBookmarkMI.setEnabled( hasBookmarks );
    }

    // remove all menus
    for( int i = menuCount - 1; i > numOfStandardMenuItems; i-- ) {
      menu.remove( i );
    }

    boolean addGlobalSep = bm.hasGlobalBookmarks();

    // add the bookmarks
    for( int i = 0; i < bm.size(); i++ ) {
      if ( addGlobalSep && !bm.isGlobalBookmark(i) ) {
        addGlobalSep = false;
        menu.addSeparator();
      }

      Bookmark b = bm.getBookmark( i );
      JMenuItem mi = new JMenuItem( b.getProfile() );

      // the number added is based on the ascii value
      if ( i < 9 ) {
        mi.setAccelerator(KeyStroke.getKeyStroke(i + 49, keyMask));
      }

      mi.setToolTipText( b.getUserName() + "@" + b.getHostName() + ":" + 
                         b.getPort() );

      mi.setEnabled( true );
      final int bookNum = i;
      mi.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          Command launchBookCmd = new LaunchBookmarkCommand();
	  ArrayList args = new ArrayList( 1 );
	  args.add( new Integer(bookNum) );
	  launchBookCmd.setArgs( args );
          SecureFTP.getCommandDispatcher().fireCommand( this, launchBookCmd );
	}
      } );
      menu.add( mi );
    }
  }

  public void refreshWindowMenu() {
    JMenu menu = getWindowMenu();
    int selectedTab = Client.getRemoteView().getSelectedIndex();

    JMenuItem selectedItem = menu.getItem( selectedTab );
    windowGroup.setSelected( selectedItem.getModel(), true );
  }

  public void updateWindowMenu() {
    JMenu menu = getWindowMenu();
    final RemoteView sv = Client.getRemoteView();

    int menuCount = menu.getItemCount();
    int numOfStandardMenuItems = 0;

    // remove all menus
    for( int i = menuCount - 1; i >= numOfStandardMenuItems; i-- ) {
      windowGroup.remove( menu.getItem(i) );
      menu.remove( i );
    }

    // add the windows
    for( int i = 0; i < sv.getTabCount(); i++ ) {
      final int tabNum = i;

      String tabName = sv.getTitleAt( tabNum );
      JRadioButtonMenuItem mi = new JRadioButtonMenuItem( tabName );
      windowGroup.add( mi );
      mi.setEnabled( true );

      boolean isSelected = sv.getSelectedIndex() == tabNum;
      windowGroup.setSelected( mi.getModel(), isSelected );

      mi.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          sv.setSelectedIndex( tabNum );
	}
      } );

      menu.add( mi );
    }
  }
}

