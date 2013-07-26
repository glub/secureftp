
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: CloseCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.secureftp.bean.*;

import java.awt.*;
import java.io.*;
import javax.swing.*;

public class CloseCommand extends NetworkCommand {
  public CloseCommand() {
    this("close", CommandID.CLOSE_COMMAND_ID);
  }

  public CloseCommand( String commandName, short id ) {
    super(commandName, id, "close ftp session");
  }

  public SecureFTPError doIt() throws CommandException {
    Thread t = new Thread() {
      public void run() {
        FTPSession currentSession = 
          FTPSessionManager.getInstance().getCurrentSession();

        if ( null != currentSession && currentSession.isConnected() ) {
          try {
            try {
              if ( currentSession.getAbortableListTransfer() != null ) {
                FTPAbortableTransfer abort = 
                  currentSession.getAbortableListTransfer();
                currentSession.getFTPBean().abort( abort );
              }
            }
            catch ( Exception e ) {}

            currentSession.getFTPBean().logout();
            //System.out.println("logged out");
          }
          catch ( IOException ioe ) {}
          catch ( FTPException ftpe ) {}
        }
      }
    };

    t.start();

    try {
      t.join();
    }
    catch ( Exception e ) {}

    FTPSessionManager.getInstance().removeCurrentSession();
    Client.getRemoteView().removeConnection();

    Client.getToolBar().updateToolBar();
    Client.getMenus().updateMenuBar();

    SecureFTP.getBaseFrame().setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );

    if ( Client.getRemoteView().getTabCount() <= 0 ) {
      SecureFTP.getBaseFrame().requestFocus();
    }
    else {
      Runnable focus = new Runnable() {
        public void run() {
          Client.getRemoteView().getCurrentConnection().getTableView().requestFocus();
        }
      };

      SwingUtilities.invokeLater( focus );
    }

    return new SecureFTPError();
  }
}

