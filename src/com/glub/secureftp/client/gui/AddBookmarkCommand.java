
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: AddBookmarkCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.secureftp.bean.*;
import com.glub.util.*;

import java.io.*;

public class AddBookmarkCommand extends LocalCommand {
  public AddBookmarkCommand() {
    this("add_bookmark", CommandID.BOOKMARK_ADD_COMMAND_ID);
  }

  public AddBookmarkCommand( String commandName, short id ) {
    super(commandName, id, "add bookmark for location");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = new SecureFTPError();
    Bookmark book = new Bookmark();

    FTPSession session = FTPSessionManager.getInstance().getCurrentSession();
    if ( null != session ) {
      book = sessionToBookmark( session );
    }

    String title = LString.getString("AddBookmarkDialog.dialogTitle", 
                                     "Add Bookmark");
    short saveBook = ModifyBookmarkDialog.showDialog( SecureFTP.getBaseFrame(),
                                                      title, book );
    if ( ModifyBookmarkDialog.OK == saveBook ) {
      try {
        BookmarkManager.getInstance().addBookmark( book );
        BookmarkManager.getInstance().writeBookmarks();
        Client.getMenus().updateBookmarks();
      }
      catch ( IOException ioe ) {
        LString lmsg = 
          new LString( "AddBookmarkDialog.saveBookmarkError",
                       "There was a problem saving the bookmarks: [^0]" );
        lmsg.replace( 0, ioe.getMessage() );

        result.setCode( SecureFTPError.PROBLEM_SAVING_BOOKMARK );
	result.setMessage( lmsg.getString() );
      
        ErrorDialog.showDialog( lmsg );
      }
    }

    return result;
  }

  private Bookmark sessionToBookmark( FTPSession session ) {
    Bookmark result = new Bookmark();

    result.setProfile( session.getHostName() );
    result.setHostName( session.getHostName() );
    result.setPort( session.getPort() );
    result.setUserName( session.getUserName() );
    result.setPassword( session.getPassword () );
    result.setSecurityMode( session.getSecurityMode() );
    result.setAnonymous(session.getUserName().toLowerCase().equals("ftp") ||
                       session.getUserName().toLowerCase().equals("anonymous"));
    result.setPassiveConnection( session.getFTPBean().getConnectionType() ==
                                 FTP.PASV_CONNECTION_TYPE );
    result.setProxy( session.usesProxy() );

    /*
    boolean dataEncrypt = false;
    if ( session.isSecure() ) {
      dataEncrypt = ((SSLFTP)session.getFTPBean()).isDataEncryptionOn();
    }
    */
    
    result.setDataEncrypt( session.isSecure() );
    result.setCCCEnabled( session.useCCC() );
    result.setRemoteFolder(((RemotePanel)session.getRemoteUI()).getCurrentDirectory());
    result.setLocalFolder( Client.getLocalView().getCurrentDirectory().getAbsolutePath() );
    
    return result;
  }
}

