
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LaunchBookmarkCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;

import java.util.*;

public class LaunchBookmarkCommand extends LocalCommand {
  public LaunchBookmarkCommand() {
    super("launch_bookmark", CommandID.BOOKMARK_LAUNCH_COMMAND_ID, 1, 1,
          "bookmark id", "launch bookmark");
  }

  public SecureFTPError doIt() throws CommandException {
    super.doIt();
    Integer id = (Integer)getArgs().get(0);

    int offset = 2;

     BookmarkManager bm = BookmarkManager.getInstance();

    if ( bm.hasGlobalBookmarks() ) {
      if ( bm.isGlobalBookmark(id.intValue()) ) {
        offset = 2;
      }
      else {
        offset = 3;
      }
    }

    Client.setLastConnectionIndex( offset + id.intValue() );
    Bookmark book = bm.getBookmark(id.intValue());
    OpenCommand openCmd = new OpenCommand();
    ArrayList args = new ArrayList(1);
    args.add( book );
    openCmd.setArgs( args );
    return SecureFTP.getCommandDispatcher().fireCommand( this, openCmd );
  }
}

