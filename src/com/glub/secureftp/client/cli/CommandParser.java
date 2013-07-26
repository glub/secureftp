
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: CommandParser.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import java.io.*;
import java.util.*;

public class CommandParser { 

  public static boolean ignoreErrors = false;

  public final static short COMMAND_NOT_FOUND  = -1;

  protected static Command[] hiddenCommands = 
    {
      new NoOpCommand(), new AmbiguousCommand(), new LoginCommand()
    };
  
  private static Command[] commands = 
    {
      new AccountCommand(), new AppendCommand(), new AsciiCommand(), 
      new BellCommand(), new BinaryCommand(), new ByeCommand(), 
      new CDCommand(), new CDUpCommand(), new CloseCommand(), 
      new DeleteCommand(),  new DebugCommand(), new DirCommand(), 
      new DisconnectCommand(), new GetCommand(), new GlobCommand(), 
      new HashCommand(), new HelpCommand(), new LCDCommand(), 
      new LsCommand(), new LLsCommand(), new MGetCommand(), 
      new MkDirCommand(), new MPutCommand(), new OpenCommand(), 
      new PromptCommand(), new PutCommand(), new PWDCommand(), 
      new QuitCommand(), new QuoteCommand(), new RecvCommand(), 
      new RemoteHelpCommand(), new RenameCommand(), new RmDirCommand(), 
      new SendCommand(), new PasvCommand(), new PortCommand(), 
      new StatusCommand(), new UserCommand(), new VerboseCommand(), 
      new QMarkCommand(), new LPWDCommand(), new TypeCommand(), 
      new LMkDirCommand(), new LDeleteCommand(), new LRmDirCommand(),
      new LRenameCommand(), new DataEncryptCommand(), new AutoCommand(),
      new VersionCommand(), new SiteCommand(), new LogCommand(),
      new MDeleteCommand(), new ChmodCommand(), new CCCCommand(), 
      new ModeZCommand(), new IgnoreErrorsCommand(), new ScriptCommand(), 
      new SleepCommand(), new LMDeleteCommand(), new SyncCommand(), 
      new EbcdicCommand()
    };

  public CommandParser( BufferedReader input ) {
    // handle ^C (on JRE 1.3+)
    if (!System.getProperty("java.version").startsWith("1.2")) {
      Runtime.getRuntime().addShutdownHook( new InterruptThread() );
    }

    ignoreErrors = GTOverride.getBoolean("glub.errors.ignore");

    Arrays.sort(getCommands(), new CommandComparator());

    SecureFTP.getCommandDispatcher().addListener( Automation.class,
                                                  new Automation() );
    SecureFTP.getCommandDispatcher().addListener( CommandPlayer.class,
                                                  new CommandPlayer() );
  }

  protected static Command[] getCommands() { return commands; }

  public boolean parse() {
    boolean result = true;
    PrintStream out = SecureFTP.getFTPSession().getPrintStream();

    try {
      Command command;

      //String currentLine = input.readLine();
      String currentLine = SecureFTP.getInput().readLine();

      if ( SecureFTP.scripted ) {
        if ( currentLine == null ) {
          SecureFTP.popScripting();
          return parse();
        }

        String lcCurLine = currentLine.trim().toLowerCase();

        if ( lcCurLine.equals("") ) {
          return parse();
        }
        else if ( lcCurLine.startsWith("#") ) {
          return parse();
        }
        else if ( lcCurLine.startsWith("user") ) {
          StringTokenizer tok = new StringTokenizer( lcCurLine );
          int tokens = 0;
          while ( tok.hasMoreTokens() ) {
            tokens++;
            if ( tokens > 2 ) {
              out.print("********** ");
              tok.nextToken();
            }
            else {
              out.print(tok.nextToken().trim() + " ");
            }
          }
          out.println("");
        }
        else {
          out.println( currentLine );
        }
      }

      if ( null == currentLine ) {
        command = new NoOpCommand();
      }
      else {
        command = parseCommand( currentLine );
      }

      FTPSession session = SecureFTP.getFTPSession();

      short commandId = COMMAND_NOT_FOUND;

      if ( null != command ) {
        commandId = command.getId();
      }

      switch ( commandId ) {
        case CommandID.NOOP_COMMAND_ID:
          break;

        case CommandID.BYE_COMMAND_ID:
        case CommandID.QUIT_COMMAND_ID:
          postCommand(command);
          result = false;
          break;

        case CommandID.AMBIGUOUS_COMMAND_ID:
          out.println("Ambiguous command.");
          break;

        case COMMAND_NOT_FOUND:
          out.println("Invalid command.");
          break;

        default:
          debugCommand( command );
          SecureFTPError err = postCommand( command );

          if ( SecureFTP.scripted && SecureFTPError.OK != err.getCode() ) {
            out.print("Something went wrong while executing the script. ");

            SecureFTP.exitCode = err.getCode();

            if ( !ignoreErrors ) {
              result = false;
              out.println("Exiting...");
              postCommand( new QuitCommand() );
            }
            else
              out.println("Ignoring...");
          }
          else if ( SecureFTPError.NOT_CONNECTED == err.getCode() ) {
            out.println("Not connected.");
          }
          else if ( session.getBeepWhenDone() && command.getBeepWhenDone() )
            System.out.print('\07');

          //System.out.println( "Code = " + err.getCode() );
      }
    }
    catch (ParserException pe) {
      out.println("Error: " + pe.getMessage());
    }
    catch (IllegalArgumentException iae) {
      out.println("Usage: " + iae.getMessage());
    }
    catch (IOException ioe) { /* ioe.printStackTrace(); */ }
    catch (NullPointerException npe) {}

    return result;
  } 

  private SecureFTPError postCommand( Command command ) {
    return SecureFTP.getCommandDispatcher().fireCommand(this, command);
  }

  private void debugCommand( Command command ) {
    PrintStream out = SecureFTP.getFTPSession().getPrintStream();

    if ( SecureFTP.getFTPSession().isDebugOn() ) {
      out.println("Command \"" + command.getCommandName() + "\" encountered.");
      ArrayList args = command.getArgs();
      for ( int i = 0; i < args.size(); i++ ) {
        out.println("Arg " + i + ": " + args.get(i));
      }
    }
  }

  protected static Command parseCommand( String input ) throws ParserException {
    Command command = null;

    if ( null == input )
      return command;

    // tokenize input
    StringTokenizer tok = new StringTokenizer( input.trim() );
    String commandToken = null;

    if ( tok.hasMoreTokens() ) {
      commandToken = tok.nextToken().trim().toLowerCase();
    }
    else {
      return new NoOpCommand();
    }

    // get command token by name
    for ( int i = 0; i < commands.length; i++ ) {
      Command currentCommand = commands[i];
      String currentCommandName = currentCommand.getCommandName().toLowerCase();

      if ( currentCommandName.equals(commandToken) ) {
        command = currentCommand;
        break;
      }
 
      boolean found = 
        currentCommandName.startsWith(commandToken);

      if ( found && null == command ) {
        command = currentCommand;
      }
      else if ( found ) {
        command = new AmbiguousCommand();
        return command;
      }
    } 

    // set the args (if any)
    if ( null != command ) {
      ArrayList args = new ArrayList(1);
      StringBuffer argBuffer = null;

      while ( tok.hasMoreTokens() ) {
        String token = tok.nextToken();

        if ( token.startsWith("\"") ) {
          if ( argBuffer == null ) {
            argBuffer = new StringBuffer(token.substring(1, token.length()));
            argBuffer.append(" ");
            continue;
          }
          else {
            argBuffer.append(token.substring(0, token.length() - 1));
            token = argBuffer.toString();
            argBuffer = null;
          }
        }
        else if ( token.endsWith("\"") ) {
          argBuffer.append(token.substring(0, token.length() - 1));
          token = argBuffer.toString();
          argBuffer = null;
        }

        if ( null == argBuffer ) {
          args.add( token );
        }
        else if ( token != null ){
          argBuffer.append(token + " ");
        }
      }

      if ( argBuffer != null ) {
        throw new ParserException("Unbalanced quotes.");
      }

      command.setArgs( args );
    }

    return command;
  }

}

class InterruptThread extends Thread {
  public void run() {
    if ( !SecureFTP.bail ) {
      SecureFTP.printExitMessage();
    }
  }
}

class CommandComparator implements Comparator {
  public int compare( Object o1, Object o2 ) {
    return ((Command)o1).getCommandName().compareTo(
                                                ((Command)o2).getCommandName());
  }
}

