
//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: ScriptCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

import java.io.*;

public class ScriptCommand extends LocalCommand {
  public ScriptCommand() {
    super("script", CommandID.SCRIPT_COMMAND_ID, 1, 1, "script-file",
          "file that contains script to run");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    SecureFTP.pushScripting();

    FTPSession session = SecureFTP.getFTPSession();

    String scriptPath = (String)(getArgs().get(0));
    File currentDirectory = session.getLocalDir(); 
    File scriptFile = new File( scriptPath );

    if ( !scriptFile.isAbsolute() ) {
      scriptFile = new File( currentDirectory, scriptPath );
    }

    try {
      SecureFTP.setScriptInputStream( new FileInputStream(scriptFile) );
      SecureFTP.scripted = true;
      session.setInteractiveOn(false);
      session.setShowProgress(false);
      session.setBeepWhenDone(false);
    }
    catch ( FileNotFoundException fnfe ) {
      System.err.println( "The file " + scriptFile + " could not be found." );
      SecureFTP.popScripting();
    }

    return result;
  }
}

