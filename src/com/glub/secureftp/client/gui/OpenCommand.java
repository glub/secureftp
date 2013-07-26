
//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: OpenCommand.java 142 2009-12-16 04:23:16Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import java.io.*;
import java.util.*;

public class OpenCommand extends LocalCommand {
  public OpenCommand() {
    super("open", CommandID.OPEN_COMMAND_ID, 0, 1, "bookmark", 
          "open ftp session");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    // if we have an update available, this we may suppress this dialog
    if ( Client.getSuppressOpenDialog() ) {
      Client.setSuppressOpenDialog( false );
      return result;
    }

    Bookmark bookmark = null;
    
    short ocdResult = OpenConnectionDialog.CANCEL;

    if ( null != getArgs() ) {
      bookmark = (Bookmark)getArgs().get(0);
      ocdResult = OpenConnectionDialog.OK;
    }

    String stashedPassword = "";

    if ( null == bookmark ) {
      bookmark = new Bookmark();
      ocdResult = 
        OpenConnectionDialog.showDialog( SecureFTP.getBaseFrame(), bookmark );
      stashedPassword = bookmark.getPassword();
    }

    if ( OpenConnectionDialog.CANCEL != ocdResult ) {
      final FTPSession session = bookmarkToSession( bookmark );
      FTPSessionManager.getInstance().addSession( session );

      if ( OpenConnectionDialog.SAVE_BOOKMARK == ocdResult ) {
        try {
          if ( !OpenConnectionDialog.getSavePasswordCheckBox().isSelected() &&
               !bookmark.isAnonymous() ) {
            bookmark.setPassword("");
          }

          //System.out.println(bookmark);
          BookmarkManager.getInstance().addBookmark( bookmark );
          BookmarkManager.getInstance().writeBookmarks();
          Client.getMenus().updateBookmarks();
          bookmark.setPassword( stashedPassword );
        }
        catch ( IOException ioe ) {
          LString lmsg = 
            new LString( "AddBookmarkDialog.saveBookmarkError",
                         "There was a problem saving the bookmarks: [^0]" );
            lmsg.replace( 0, ioe.getMessage() );

          ErrorDialog.showDialog( lmsg );
        }
      }

      ConnectCommand connectCmd = new ConnectCommand();
      ArrayList args = new ArrayList( 1 );
      args.add( bookmark );
      connectCmd.setArgs( args );
      result = SecureFTP.getCommandDispatcher().fireCommand( this, connectCmd );
    }

    // at this point if there were any request to suppress, clear it out
    Client.setSuppressOpenDialog( false );

    return result;
  }

  public FTPSession bookmarkToSession( Bookmark bookmark ) {
    FTPSession session = new FTPSession();
    session.setDebugOn( SecureFTP.debug );
    session.setCertHandler( new GUISSLCertificateHandler() );

    session.setUserName( bookmark.getUserName() );
    session.setPassword( bookmark.getPassword() );
    session.setHostName( bookmark.getHostName() );
    session.setPort( bookmark.getPort() );

    int secMode = bookmark.getSecurityMode();
    session.setSecurityMode( (short)secMode );

    boolean isSecure = ConnectionDialog.EXPLICIT_SSL == secMode ||
                       ConnectionDialog.IMPLICIT_SSL == secMode;
    session.setIsSecure( isSecure );

    session.setUseProxy( bookmark.usesProxy() );

    session.setUseCCC( bookmark.isCCCEnabled() );

    return session;
  }
}

