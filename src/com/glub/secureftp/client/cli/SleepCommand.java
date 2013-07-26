
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: SleepCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

import com.glub.util.*;

import java.io.PrintStream;

public class SleepCommand extends LocalCommand {
  public SleepCommand() {
    super("sleep", CommandID.SLEEP_COMMAND_ID, 0, 1, "[seconds]",
          "pause the scripted environment");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();
    PrintStream out = SecureFTP.getFTPSession().getPrintStream();

    if ( SecureFTP.scripted ) {
      String sTime = null;

      if ( getArgs().size() == 1 ) 
        sTime = (String)(getArgs().get(0));

      int time = Util.parseInt( sTime, 1 ); 
      if ( time > 0 ) {
        SleepThread st = new SleepThread( time );
        st.start(); 
        try {
          st.join( time * 2000 );
        } catch ( InterruptedException ie ) {}
      }
    }
    else {
      out.println("This command is only available while running a script.");
    }

    return result;
  }
}

class SleepThread extends Thread {
  private long time = 0;

  public SleepThread() {
    this( 1 );
  }

  public SleepThread( long time ) {
    this.time = time;
  }

  public void run() {
    try {
      sleep( time * 1000 );
    } catch ( InterruptedException ie ) {}
  }
}

