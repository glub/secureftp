
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: EditBookmarksCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;

public class EditBookmarksCommand extends LocalCommand {
  public EditBookmarksCommand() {
    this("edit_bookmark", CommandID.BOOKMARK_EDIT_COMMAND_ID);
  }

  public EditBookmarksCommand( String commandName, short id ) {
    super(commandName, id, "edit bookmarks");
  }

  public SecureFTPError doIt() throws CommandException {
    new EditBookmarksDialog();
    return new SecureFTPError();
  }
}

