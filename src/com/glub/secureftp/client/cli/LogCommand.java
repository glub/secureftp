
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LogCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

import java.io.*;

public class LogCommand extends LocalCommand {
  public LogCommand() {
    super("log", CommandID.LOG_COMMAND_ID, 1, 1, "[log-file|off]",
          "log FTP communication");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();

    if ( session.getOutputStream() != System.out ) {
      try {
        session.getOutputStream().close();
      }
      catch ( IOException ioe ) {}
    }

    String logFileStr = (String)getArgs().get(0);
    OutputStream outStream = System.out;

    String logMsg = "Logging disabled.";

    if ( !logFileStr.equalsIgnoreCase("off") ) {
      File logFile = new File(logFileStr);

      if ( !logFile.isAbsolute() ) {
        File currentDir = session.getLocalDir();
        logFile = new File( currentDir, logFileStr );
      }

      if ( logFile.exists() ) {
        if ( logFile.isDirectory() ) {
          System.out.println("A directory with that name already exists.");
          return result;
        }
      }

      try {
        RandomAccessFile appendFile = new RandomAccessFile( logFile, "rw" );

        try {
          appendFile.seek( logFile.length() );
        }
        catch ( IOException ioe ) {}
    
        outStream = new LogStream( appendFile.getFD() );
        logMsg = "Logging to file: " + logFile.getAbsolutePath();
      }
      catch ( IOException ioe ) {
        System.out.println("There was a problem creating the log file.");
        outStream = System.out;
      }
    }

    System.out.println( logMsg );
    session.setOutputStream( outStream );

    if ( session.isConnected() ) {
      session.getFTPBean().setRecvCmdStream( outStream ); 
      session.getFTPBean().setSendCmdStream( outStream ); 
    }

    return result;
  }
}

