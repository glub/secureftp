
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: VersionCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

import java.io.*;
import java.util.*;

public class VersionCommand extends LocalCommand {
  private static String buildInfo = null;
  public VersionCommand() {
    super("version", CommandID.VERSION_COMMAND_ID, "get version information");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();
    PrintStream out = SecureFTP.getFTPSession().getPrintStream();

    if ( buildInfo == null ) {
      InputStream buildFileStream = 
        getClass().getResourceAsStream("build.info");
      if ( buildFileStream != null ) {
        try {
          Properties buildProp = new Properties();
          buildProp.load( buildFileStream );
          buildInfo = buildProp.getProperty("build.date") + "." + 
                      buildProp.getProperty("build.number");
        }
        catch ( Exception ioe ) { buildInfo = "unknown"; }
      }
      else {
        buildInfo = "unknown";
      }
    }

    out.println(Version.PROGRAM_NAME + " v" + Version.VERSION + 
                " [" + buildInfo + "]");
    out.println("Glub Tech Secure FTP Bean v" + Version.BEAN_VERSION +
                " [" + Version.BEAN_DATE + "]");
    out.println("Operating System: " + System.getProperty("os.name") + " " + 
                System.getProperty("os.version") + " (" + 
                System.getProperty("os.arch") + ")");
    out.println("Java Version: " + System.getProperty("java.version"));
    //out.println("Java Class Path: " + System.getProperty("java.class.path"));

    return result;
  }
}

