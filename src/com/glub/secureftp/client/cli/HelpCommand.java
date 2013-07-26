
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: HelpCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

import java.io.PrintStream;
import java.util.*;

public class HelpCommand extends LocalCommand {
  public HelpCommand() {
    this("help", CommandID.HELP_COMMAND_ID);
  }

  public HelpCommand( String commandName, short id ) {
    super(commandName, id, 0, 9999, "[command1 command2 ...]", 
          "print local help information"); 
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    PrintStream out = SecureFTP.getFTPSession().getPrintStream();

    ArrayList helpArgs = getArgs();
    if ( helpArgs.size() > 0 ) {
      try {
        for ( int i = 0; i < helpArgs.size(); i++ ) {
          Command helpCmd = CommandParser.parseCommand((String)helpArgs.get(i));
          if ( null == helpCmd ) {
            out.println("Invalid help command: " + helpArgs.get(i));
          }
          else if ( CommandID.AMBIGUOUS_COMMAND_ID == helpCmd.getId() ) {
            out.println("Ambiguous help command: " + helpArgs.get(i));
          }
          else {
            String tab = "\t";
            String commandName = helpCmd.getCommandName();
            if ( commandName.length() <= 7 ) {
              tab += "\t";
            }
            out.println(commandName + tab + helpCmd.getHelpMessage());
            out.println("\t\tUsage: " + commandName + " " + 
                               helpCmd.getUsage());
          }
        }
      }
      catch (ParserException pe) {
        out.println(pe.getMessage());
      }
    }
    else {
      int count = 1;
      int row = 1;
      int col = 0;
      double maxCols = 5.0;

      Command[] commands = CommandParser.getCommands();

      int maxRows = (int)Math.ceil(commands.length / maxCols );

      out.println("Commands may be abbreviated. Commands are:");
      out.println("");

      for ( int i = 0; i < maxRows * maxCols; i++ ) {
        int item = row + (col * maxRows) - 1;

        String commandName = "";

        if ( item >= commands.length ) {
          commandName = " ";
        }
        else {
          commandName = commands[item].getCommandName().trim();
        }

        if ( count % 5 == 0 ) {
          out.println( commandName );
          row++;
          col = 0;
        }
        else {
          out.print( commandName + "\t" ); 
          if ( commandName.length() <= 7 ) {
            out.print( "\t" );
          }
          col++;
        }
        count++;
      }

      if ( (count - 1) % 5 != 0 ) {
        out.println("");
      }

      out.println("");
      out.println("Usage: help [command1 command2 ...]");
    }

    return result;
  }
}

class QMarkCommand extends HelpCommand {
  public QMarkCommand() {
    super("?", CommandID.QMARK_COMMAND_ID);
  }
}

