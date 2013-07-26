
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: AppendCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

import java.io.*;

public class AppendCommand extends NetworkCommand {
  public AppendCommand() {
    super("append", CommandID.APPEND_COMMAND_ID, 1, 2,
          "local-file [remote-file]", "append to a file");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();
    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    String remoteFileName = null;
    if ( getArgs().size() == 2 ) {
      remoteFileName = (String)getArgs().get(1);
    }

    ProgressThread pt = null;

    try {
      if ( session.showProgress() ) {
        pt = new ProgressThread();
        pt.start();
      }

      session.getFTPBean().append( new File((String)getArgs().get(0)), 
                                   remoteFileName, pt );
    }
    catch ( IOException ioe ) {
      out.println(ioe.getMessage());
    }
    catch ( FTPException fe ) {
      out.println(fe.getMessage());
    }
    finally {
      if ( session.showProgress() && null != pt && pt.isRunning() ) {
        pt.finishProgress();
      }
    }

    return result;
  }
}

