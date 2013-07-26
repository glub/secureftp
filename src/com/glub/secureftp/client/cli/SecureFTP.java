
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: SecureFTP.java 134 2009-12-13 07:50:11Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

//import sun.misc.Signal;
//import sun.misc.SignalHandler;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import java.io.*;
import java.util.*;

public class SecureFTP {

  public final static String COPYRIGHT    = Version.COPYRIGHT;
  public final static String PROGRAM_NAME = Version.PROGRAM_NAME;
  public       static String VERSION      = Version.VERSION;

  private static int defaultExplicitPort = Constants.DEF_EXPLICIT_SSL_PORT;
  private static int defaultImplicitPort = Constants.DEF_IMPLICIT_SSL_PORT;

  public static boolean bail = false;
  //public static boolean abortCommand = false;

  public static boolean securityDisabled = false;
  public static boolean forceEncrypt = false;
  public static boolean explicitSSLOnly = false;

  private static FTPSession session = null;
  private static CommandDispatcher commandDispatcher = new CommandDispatcher();

  private static BufferedReader stdin = null;
  private static FileInputStream scriptInputStream = null;

  private static ArrayList scriptStateStack = new ArrayList(1);

  public static boolean scripted = false;
  private static boolean startViaScript = false;

  public static int exitCode = SecureFTPError.OK;

  public static void main( String[] args ) {
    boolean canUseJSSE =
      System.getProperty("java.version").startsWith("1.2") ||
      System.getProperty("java.version").startsWith("1.3");

    if ( canUseJSSE && GTOverride.getBoolean("use.jsse") ) {
      // we're ok
      String regFile = System.getProperty("user.dir") + 
                       File.separator +
                       "registration.txt";
      File fRegFile = new File(regFile);
      if ( fRegFile.exists() && null == System.getProperty("registration.file") )
        System.setProperty("registration.file", regFile);
    }
    else if ( System.getProperty("java.version").startsWith("1.0") ||
         System.getProperty("java.version").startsWith("1.1") ||
         canUseJSSE ) {
      if ( !GTOverride.getBoolean("glub.version.override") ) {
        System.out.println("This program requires at the Java 2 Runtime " +
                           "version 1.4 or above.");
        System.exit( SecureFTPError.JAVA_VERSION_INCORRECT );
      }
    }

    SSLFTP.preSeed();

    String startupDir = GTOverride.getString("glub.user.dir");

    securityDisabled = GTOverride.getBoolean("glub.security.override");

    forceEncrypt = GTOverride.getBoolean("glub.dataencrypt.override");

    System.setProperty( "glub.resourceBundle",
                        "com.glub.secureftp.client.resources.strings" );

    if ( securityDisabled ) {
      System.out.println("");
      System.out.println("!!! WARNING !!!: security measures disabled.");
      System.out.println("");
    }

    explicitSSLOnly = GTOverride.getBoolean("glub.ssl.explicit.only");

    defaultImplicitPort = GTOverride.getInt( "default.port.ssl.implicit", 
                                           Constants.DEF_IMPLICIT_SSL_PORT );

    defaultExplicitPort = GTOverride.getInt( "default.port.ssl.explicit", 
                                           Constants.DEF_EXPLICIT_SSL_PORT );

    if ( startupDir != null ) {
      System.setProperty("user.dir", startupDir);
    }

    new SecureFTP( args );
    
    FTPSession curSession = FTPSessionManager.getInstance().getCurrentSession();
    if ( curSession != null ) {
      if ( curSession.getOutputStream() instanceof LogStream )
    	try {
          curSession.getOutputStream().close();
    	}
        catch (IOException ioe) {}
    }

    System.exit( exitCode );
  }

  public SecureFTP( String[] args ) {
    //FTPSignalHandler.install("INT");

    session = new FTPSession();
    session.setCertHandler( new CLISSLCertificateHandler() );

    FTPSessionManager.getInstance().addSession( session );

    boolean loginOnStart = false;

    ArrayList argList = new ArrayList(args.length);

    for ( int i = 0; i < args.length; i++ ) {
      argList.add(args[i]);
    }

    int numOfArgs = argList.size();

    if ( numOfArgs > 0 ) {
      if ( "-help".startsWith((String)argList.get(0)) ) {
        usage();
      }
      else if ( "-script".startsWith((String)argList.get(0)) ) {
        if ( numOfArgs != 2 ) {
          usage();
        }

        pushScripting();

        startViaScript = true;

        String scriptPath = (String)argList.get(1); 
        
        File scriptFile = new File( scriptPath );
        if ( !scriptFile.exists() ) {
            System.err.println( "The file " + scriptFile + 
            " could not be found." );
            usage();
        }
        else {
	        try {
	          setScriptInputStream( new FileInputStream(scriptFile) ); 
	        }
	        catch ( FileNotFoundException fnfe ) {
	          System.err.println( "The file " + scriptFile + 
	                              " could not be found." );
	          usage();
	        }
	        scripted = true;
	        session.setInteractiveOn(false);
	        session.setShowProgress(false);
	        session.setBeepWhenDone(false);
	        argList.remove(1);
	        argList.remove(0);
	        numOfArgs-=2;
        }
      }
    }

    if ( numOfArgs == 1 || numOfArgs == 2 ) {
      String arg1 = (String)argList.get(0);

      int index = -1;

      // look for username
      if ( (index = arg1.indexOf('@')) > 0 ) {
        String user = arg1.substring(0, index);
        arg1 = arg1.substring( index + 1, arg1.length() );
        session.setUserName( user );
      }

      // look for starting path
      if ( (index = arg1.indexOf('/')) > 0 ) {
        String startPath = arg1.substring(index, arg1.length());
        session.setWorkingDir( startPath ); 
        arg1 = arg1.substring(0, index); 
      }

      session.setHostName( arg1 );
      loginOnStart = true;
    }

    if ( numOfArgs == 2 ) {
      try {
        session.setPort( Integer.parseInt((String)argList.get(1)) );
      }
      catch ( NumberFormatException nfe ) {
        usage();
      }
    }
    else if ( numOfArgs > 2 ) {
      usage();
    }

    PrintStream out = session.getPrintStream();

    out.println(PROGRAM_NAME + " v" + VERSION);
    out.println(COPYRIGHT);

    stdin = getInput();
    CommandParser cp = new CommandParser(stdin);

    do {
      if ( loginOnStart ) {
        loginOnStart = false;
        OpenCommand oc = new OpenCommand();
        ArrayList newArgs = new ArrayList();
        newArgs.add(session.getHostName());
        if ( session.getPort() > 0 ) {
          newArgs.add(Integer.toString(session.getPort()));
        }
        oc.setArgs(newArgs);
        SecureFTP.getCommandDispatcher().fireCommand(this, oc);
      }

      if ( session != null && session.isSecure() ) {
        System.out.print("ftps> ");
      }
      else {
        System.out.print("ftp> ");
      }
    } while ( cp.parse() );

    if (System.getProperty("java.version").startsWith("1.2")) {
      printExitMessage();
    }
  }

  public static InputStream getInputStream() {
    if ( scriptInputStream != null ) {
      return scriptInputStream;
    }
    else {
      return System.in;
    }
  }

  public static BufferedReader getInput() {
    if ( stdin == null ) {
      stdin = new BufferedReader(new InputStreamReader(getInputStream()));
    }
    return stdin;
  }

  public static void setScriptInputStream( FileInputStream fis ) {
    stdin = null;    
    scriptInputStream = fis;
  }

  public static void pushScripting() {
    ScriptState ss = new ScriptState( stdin, scripted, session );
    scriptStateStack.add( ss );
  }

  public static void popScripting() {
    int topOfStack = scriptStateStack.size() - 1;
    ScriptState ss = ( (ScriptState)scriptStateStack.get(topOfStack) );
    scriptStateStack.remove( topOfStack );
    stdin = ss.getInput();
    scripted = ss.isScripted();
    session.setInteractiveOn( ss.isInteractiveOn() );
    session.setShowProgress( ss.showProgress() );
    session.setBeepWhenDone( ss.getBeepWhenDone() );
    
    if ( startViaScript && scriptStateStack.size() == 0 ) {
      getFTPSession().getPrintStream().println( "quit" );
      
      try {
        if ( ss.getInput() != null )
      	  ss.getInput().close();
      }
      catch (IOException ioe) {}

      System.exit( exitCode ); 
    }
  }

  public void usage() {
    bail = true;
    System.out.println(PROGRAM_NAME + " v" + VERSION);

    String progName = "java -jar secureftp2.jar";

    if ( Util.isMacOS() ) {
      progName = "ftps";
    }

    System.out.println("usage: " + progName + 
                       " [-script script-file | host [port]]");
    System.exit(-1);
  }

  public static int getDefaultImplicitSSLPort() { return defaultImplicitPort; }
  public static int getDefaultExplicitSSLPort() { return defaultExplicitPort; }

  public static FTPSession getFTPSession() { return session; }
  public static void setFTPSession( FTPSession newSession ) { 
    session = newSession; 
  }

  public static CommandDispatcher getCommandDispatcher() { 
    return commandDispatcher; 
  }

  public static void printExitMessage() {
    PrintStream out = getFTPSession().getPrintStream();

    out.println("");
    out.println("Thank you for using " + SecureFTP.PROGRAM_NAME + ".");
    out.print("Be sure and check out our other secure software at ");
    out.println("http://www.glub.com");

    if ( !scripted ) {
      try {
        Thread.sleep(500);
      } catch ( InterruptedException ie ) {}
    }
  }
}

/*
class FTPSignalHandler implements SignalHandler {
  private SignalHandler oldHandler;

  // Static method to install the signal handler
  public static FTPSignalHandler install(String signalName) {
    Signal diagSignal = new Signal(signalName);
    FTPSignalHandler diagHandler = new FTPSignalHandler();
    diagHandler.oldHandler = Signal.handle(diagSignal,diagHandler);
    return diagHandler;
  }

  // Signal handler method
  public void handle(Signal sig) {
    try {
      if ( sig.toString().equals("SIGINT") ) {

          if ( SecureFTP.getFTPSession() != null && 
               SecureFTP.getFTPSession().isSecure() ) {
            System.out.print("ftps> ");
          }
          else {
            System.out.print("ftp> ");
          }

        //SecureFTP.abortCommand = true;
        System.out.println("");
        NoOpCommand nop = new NoOpCommand();
        SecureFTP.getCommandDispatcher().fireCommand(this, nop);
      }
      else {
        // Chain back to previous handler, if one exists
        if ( oldHandler != SIG_DFL && oldHandler != SIG_IGN ) {
            oldHandler.handle(sig);
        }
      }
    }
    catch ( Exception e ) {}
  }
}
*/
