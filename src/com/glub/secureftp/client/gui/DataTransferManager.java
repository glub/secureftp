
//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: DataTransferManager.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.secureftp.bean.*;
import com.glub.util.*;

import java.awt.Cursor;
import java.io.*;
import java.util.*;

public class DataTransferManager {
  private static DataTransferManager instance = null;
  public static final short NONE = -1;
  public static final short UPLOAD = 0;
  public static final short DOWNLOAD = 1;
  private HashMap sessions = new HashMap();
  private HashMap listings = new HashMap();
  private HashMap threads = new HashMap();

  private DataTransferManager() {
  }

  public static DataTransferManager getInstance() {
    if ( null == instance ) {
      instance = new DataTransferManager();
    }

    return instance;
  }

  protected void storeTransfer( FTPSession session, DataTransfer dt ) {
    ArrayList transferList = (ArrayList)sessions.get( session );
    if ( null == transferList ) {
      transferList = new ArrayList();
    }
    transferList.add( dt );
    sessions.put( session, transferList );
  }

  protected void storeListing( FTPSession session, DataTransfer dt ) {
    listings.put( session, dt );
  }

  public DataTransfer getListingForSession( FTPSession session ) {
    DataTransfer dt = (DataTransfer)listings.get( session );
    listings.remove( session );
    return dt;
  }

  public ArrayList getTransfersForSession( FTPSession session ) {
    return (ArrayList)sessions.get( session );
  }

  protected DataTransferThread getTransferThread( FTPSession session ) {
    DataTransferThread transferThread = 
      (DataTransferThread)threads.get( session );

    if ( null == transferThread || transferThread.isDone() ) {
      transferThread = new DataTransferThread( session );
      transferThread.start();
      threads.put( session, transferThread );
    }

    return transferThread; 
  }

  public void download( FTPSession session, List fileList ) {
    if ( !Client.getAllowDownload() )
      return;

    DataTransfer dt = new DataTransfer( DataTransfer.FILE, fileList, DOWNLOAD );
    FTPSession copySession = new FTPSession( session );
    storeTransfer( copySession, dt );

    getTransferThread( copySession );
  }

  public void upload( FTPSession session, List fileList, RemoteFile dest ) {
    if ( !Client.getAllowUpload() )
      return;

    DataTransfer dt = new DataTransfer( DataTransfer.FILE, fileList, 
                                        UPLOAD, dest );
    storeTransfer( session, dt );

    getTransferThread( session );
  }

  public void list( FTPSession session ) {
    list( session, null );
  }

  public void list( FTPSession session, RemoteFile dirToList ) {
    ArrayList fileList = new ArrayList(1);
    fileList.add( dirToList );
    DataTransfer dt = new DataTransfer( DataTransfer.LIST, fileList, NONE );

    //storeTransfer( session, dt );
    storeListing( session, dt );

    getTransferThread( session );
  }
}

class DataTransferThread extends Thread {
  private FTPSession session;
  private boolean isDone = false;

  public DataTransferThread( FTPSession session ) {
    super();
    this.session = session;
  }

  public void run() {
    SecureFTP.getBaseFrame().setCursor( new Cursor(Cursor.WAIT_CURSOR) );

    isDone = false;
    session.setTransferringData( true );

    ArrayList dataTransfers = 
      DataTransferManager.getInstance().getTransfersForSession( session );

    DataTransfer dt = null;

    while ( (null != dataTransfers && dataTransfers.size() > 0) || 
      null != (dt=DataTransferManager.getInstance().getListingForSession(session)) ) {

      if ( null != dataTransfers && dataTransfers.size() > 0 ) {
        dt = (DataTransfer)dataTransfers.get( 0 );
        dataTransfers.remove( 0 );
      }

      String currentDir = session.getWorkingDir();
      boolean changedDir = false;

      if ( DataTransferManager.UPLOAD == dt.getDirection() ) {
        RemoteFile destination = dt.getDestination();

        String destPath = destination.getFileName();
        if ( !destPath.endsWith("/") ) {
          destPath += "/";
        }

        if ( destination.getFileName().length() == 0 &&
             session.getWorkingDir().length() == 0 ) {
          // fix stupid case where cwd is empty and so is the dest
        }
        else if ( !destination.getFileName().equals(".") &&
             !destPath.equals(session.getWorkingDir()) ) {
          try {
            session.getFTPBean().chdir( destination );
            changedDir = true;
          }
          catch ( FTPPermissionDeniedException fde ) {
            LString msg = new LString("CDCommand.permission_denied", 
                                      "[^0]: Permission denied.");
            msg.replace( 0, destPath );
            ErrorDialog.showDialog( msg );
            continue;
          }
          catch ( FTPException fe ) { 
            fe.printStackTrace(); 
          }
        }
      }

      Iterator iter = dt.getFileList().iterator();

      FTPAbortableTransfer abort = null;
      DataTransferDialog dialog = null;

      if ( DataTransfer.FILE == dt.getType() ) {
        dialog = new DataTransferDialog( SecureFTP.getBaseFrame(), session, 
                                         dt.getDirection(), abort );
      }

      MGetCommand mgc = new MGetCommand();
      ArrayList mgcArgs = new ArrayList(4);
      mgc.setArgs( mgcArgs );

      MPutCommand mpc = new MPutCommand();
      ArrayList mpcArgs = new ArrayList(4);
      mpc.setArgs( mpcArgs );

      while ( iter.hasNext() ) {
        DataTransfer listTransfer = 
          DataTransferManager.getInstance().getListingForSession( session );

        // if a listing was requested in between transfers
        if ( null != listTransfer ) {
          RemoteFile dirToList = (RemoteFile)listTransfer.getFileList().get(0);
          list( dirToList );
        }
        // if a listing was requested all by itself
        else if ( DataTransfer.LIST == dt.getType() ) {
          RemoteFile dirToList = (RemoteFile)iter.next();
          list( dirToList );
        }
        else if ( DataTransferManager.UPLOAD == dt.getDirection() ) {
          File file = (File)iter.next();
	  dialog.setFileName( file.getName() );

	  //System.out.println( "put " + file + " to " + dt.getDestination() );

          ArrayList args = mpc.getArgs();
          args.clear();
          args.add( file );
          args.add( session );
          args.add( dialog );
          args.add( mpc.getLastCommand() );
          mpc.setArgs( args );

          SecureFTPError result = 
            SecureFTP.getCommandDispatcher().fireCommand( this, mpc );

          if ( SecureFTPError.NOT_CONNECTED == result.getCode() ) {
            SecureFTP.getBaseFrame().setCursor( 
              new Cursor(Cursor.DEFAULT_CURSOR) );
          }
          else if ( SecureFTPError.TRANSFER_ABORTED == result.getCode() ) {
            list();
            break;
          }
        }
        else {
          RemoteFile remoteFile = (RemoteFile)iter.next(); 

          ArrayList args = mgc.getArgs();
          args.clear();
          args.add( remoteFile );
          args.add( session );
          args.add( dialog );
          args.add( mgc.getLastCommand() );
          mgc.setArgs( args );

          SecureFTPError result = 
            SecureFTP.getCommandDispatcher().fireCommand( this, mgc );

          if ( SecureFTPError.NOT_CONNECTED == result.getCode() ) {
            SecureFTP.getBaseFrame().setCursor( 
              new Cursor(Cursor.DEFAULT_CURSOR) );
          }
          else if ( SecureFTPError.TRANSFER_ABORTED == result.getCode() ) {
            break;
          }
        }
      }

      if ( !changedDir && DataTransferManager.UPLOAD == dt.getDirection() ) {
        list();
        ArrayList selectionList = new ArrayList( dt.getFileList().size() );
        for ( int i = 0; i < dt.getFileList().size(); i++ ) {
          File f = ((File)dt.getFileList().get(i));
          RemoteFile rf = new RemoteFile( f.getName() );
          selectionList.add( rf );
        }
        RemotePanel rp = (RemotePanel)session.getRemoteUI();
        rp.getTableView().selectFiles( selectionList );
      }
      else if ( DataTransferManager.DOWNLOAD == dt.getDirection() ) {
        Client.getLocalView().refresh();
        ArrayList selectionList = new ArrayList( dt.getFileList().size() );
        for ( int i = 0; i < dt.getFileList().size(); i++ ) {
          RemoteFile rf = ((RemoteFile)dt.getFileList().get(i));
          File f = new File( session.getLocalDir(), rf.getFileName() );
          selectionList.add( f );
        }
        Client.getLocalView().selectFiles( selectionList );
      }

      if ( null != dialog ) {
        dialog.dispose();
      }

      if ( changedDir ) {
        try {
          session.getFTPBean().chdir( currentDir );
        }
        catch ( FTPException fe ) { fe.printStackTrace(); }
      }
/*
      else if ( DataTransferManager.UPLOAD == dt.getDirection() ) {
        list();
      }
*/
    }

    isDone = true;
    session.setTransferringData( false );
    SecureFTP.getBaseFrame().setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );
  }

  private void list() { 
    list(null); 
  }

  private void list( RemoteFile dirToList ) {
    RemotePanel rp = (RemotePanel)session.getRemoteUI();

    if ( null == rp ) {
      return;
    }

    RemoteFileList rfl = null;
    FTPAbortableTransfer abort = new FTPAbortableTransfer();
    session.setAbortableListTransfer( abort );
    Client.getToolBar().updateToolBar();

    try {
      if ( null != dirToList ) {
        rfl = session.getFTPBean().list( dirToList, abort, 
                                         Client.showHiddenFiles() );
      }
      else {
        if (Client.showHiddenFiles())
          rfl = session.getFTPBean().listAll( abort );
        else
          rfl = session.getFTPBean().list( abort );
      }
    }
    catch ( FTPAbortException fae ) {
      //System.out.println("List aborted");
      rfl = new RemoteFileList();
    }
    catch ( FTPConnectionLostException fcle ) {
      SecureFTP.getCommandDispatcher().fireCommand( this, new CloseCommand() );
      ErrorDialog.showDialog( new LString("Common.connection_lost",
                                          "Connection lost.") );
    }
    catch ( Exception fe ) {
      rfl = new RemoteFileList();
    }
    finally {
      // remove hidden files
      if ( !Client.showHiddenFiles() && null != rfl && rfl.size() > 0 ) {
        for ( int i = rfl.size() - 1; i > 0; i-- ) {
          if ( rfl.getFile(i).getFileName().startsWith(".") ) {
            rfl.remove( i );
          }
        }
      }

      rp.buildTable( rfl );
      session.setAbortableListTransfer( null );
      Client.getToolBar().updateToolBar();
      Client.getMenus().updateMenuBar();
      rp.getTableView().validate();
      rp.getTableView().repaint();
    }
  }

  public boolean isDone() { return isDone; }
}

class DataTransfer {
  public static final short FILE = 0;
  public static final short LIST = 1;

  private List fileList;
  private short type;
  private short direction;
  private RemoteFile destination;

  public DataTransfer( short type, List fileList ) {
    this( type, fileList, (short)-1, null );
  }

  public DataTransfer( short type, List fileList, short direction ) {
    this( type, fileList, direction, null );
  }

  public DataTransfer( short type, List fileList, short direction,
                       RemoteFile destination ) {
    this.type = type;
    this.fileList = fileList;
    this.direction = direction;

    if ( null == destination ) {
      this.destination = new RemoteFile();
      this.destination.setFileName( "." );
    }
    else {
      this.destination = destination;
    }
  }

  public short getType() { return type; }
  public List getFileList() { return fileList; }
  public short getDirection() { return direction; }
  public RemoteFile getDestination() { return destination; }
}
