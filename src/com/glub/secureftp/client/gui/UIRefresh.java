
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: UIRefresh.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;

public class UIRefresh implements CommandHandler {
  public void actionPerformed(java.awt.event.ActionEvent e){}

  public SecureFTPError handleCommand( Command c ) throws CommandException {
    Client.getMenus().updateMenuBar();
    Client.getToolBar().updateToolBar();
    return new SecureFTPError();
  }
}
