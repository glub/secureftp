
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: Command.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.framework;

import java.util.*;

public abstract class Command {
  private String commandName;
  private short id;
  private ArrayList args;
  private int minNumOfArgs;
  private int maxNumOfArgs;
  private String usage;
  private String helpMessage;
  private boolean beepWhenDone = false;
  private boolean suppressErrors = false;

  public Command( String commandName, short id, String helpMessage ) {
    this( commandName, id, 0, 0, "", helpMessage );
  }

  public Command( String commandName, short id, 
                  int minNumOfArgs, int maxNumOfArgs, 
                  String usage, String helpMessage ) { 
    this.commandName = commandName; 
    this.id = id;
    this.minNumOfArgs = minNumOfArgs;
    this.maxNumOfArgs = maxNumOfArgs;
    this.usage = usage;
    this.helpMessage = helpMessage;
  }

  public String getCommandName() { return commandName; }

  public short getId() { return id; }

  public ArrayList getArgs() { return args; }
  public void      setArgs( ArrayList args ) { this.args = args; }

  public void verifyArgs() throws IllegalArgumentException {
    if ( getMinNumOfArgs() == 0 && getArgs() == null ) {
      return;
    }
    else if ( getArgs().size() < getMinNumOfArgs() ||
              getArgs().size() > getMaxNumOfArgs() ) {
      throw new IllegalArgumentException( getCommandName() + " " + getUsage() + 
                                          " : received " + getArgs().size() + 
                                          " args" );
    }
  }

  public int getMinNumOfArgs() { return minNumOfArgs; }

  public int getMaxNumOfArgs() { return maxNumOfArgs; }

  public String getUsage() { return usage; }

  public String getHelpMessage() { return helpMessage; }

  public boolean getBeepWhenDone() { return beepWhenDone; }
  public void    setBeepWhenDone( boolean beepWhenDone ) {
    this.beepWhenDone = beepWhenDone;
  }

  public abstract SecureFTPError doIt() throws CommandException;

  public boolean suppressErrors() { return suppressErrors; }
  public void suppressErrors( boolean suppress ) { suppressErrors = suppress; }
}

