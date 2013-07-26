
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: OpenCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.util.*;
import com.glub.secureftp.client.framework.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class OpenCommand extends LocalCommand {
  private static boolean displayedConnectionWarning = false;
  private boolean secModeSet = false;

  public OpenCommand() {
    super("open", CommandID.OPEN_COMMAND_ID, 0, 3, "[secmode:implicit_ssl|explicit_ssl|none] host [port]",
          "connect to remote tftp");
  }

  public void verifyArgs() throws IllegalArgumentException {
    PrintStream out = SecureFTP.getFTPSession().getPrintStream();

    if ( getArgs().size() < getMinNumOfArgs() ||
         getArgs().size() > getMaxNumOfArgs() ) {
      throw new IllegalArgumentException( getCommandName() + " " + getUsage() );
    }
    else if (getArgs().size() >= 1) {
      String modeCheck = (String)getArgs().get(0);
      secModeSet = modeCheck.toLowerCase().startsWith("secmode:");
      if (secModeSet) {
	getArgs().remove(0);
        FTPSession session = SecureFTP.getFTPSession();
        String mode = modeCheck.substring("secmode:".length(), modeCheck.length());
        if (mode != null)
          mode = mode.toLowerCase().trim();
        if (mode.equals("implicit_ssl")) {
          session.setSecurityMode( FTPSession.IMPLICIT_SSL );
        }
        else if (mode.equals("explicit_ssl")) {
          session.setSecurityMode( FTPSession.EXPLICIT_SSL );
        }
        else if (mode.equals("none")) {
          session.setSecurityMode( FTPSession.NO_SECURITY );
        }
	else {
          out.println("Security mode unknown... using defaults.");
	}
      }
    }

    if ( getArgs().size() == 2 ) {
      try {
        Integer.parseInt((String)getArgs().get(1));
      }
      catch ( NumberFormatException nfe ) {
        out.println("Error: Bad port number - " + getArgs().get(1));
        throw new IllegalArgumentException( getCommandName() + " " + 
                                            getUsage() );
      }
    }
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();
    FTP ftp = session.getFTPBean();

    if ( ftp != null && ftp.isConnected() ) {
      out.println("Already connected to " + session.getHostName() +
                  ", use close first.");
      return result;
    }

    int numberOfArgs = getArgs().size();

    if ( 0 == numberOfArgs ) {
      if ( session.getHostName() != null ) {
         ArrayList args = new ArrayList(1);
         args.add( session.getHostName() );
         setArgs( args );
      }
      else {
        BufferedReader stdin = SecureFTP.getInput();
        out.print("(to) ");
        try {
          String input = stdin.readLine();
          StringTokenizer tok = new StringTokenizer(input);
          ArrayList args = new ArrayList(1);
          while( tok.hasMoreTokens() ) {
            args.add(tok.nextToken());
          }
          setArgs(args);
        }
        catch ( IOException ioe ) {
          throw new CommandException( ioe.getMessage() );
        }
        catch ( IllegalArgumentException iae ) {
          out.println("Usage: " + getCommandName() + " " +
                             iae.getMessage());
          throw new CommandException("open arg verification failed");
        }
      }
    }

    boolean weSetDefaultPort = false;

    if ( getArgs().size() > 0 ) {
      session.setHostName( (String)getArgs().get(0) );
      if ( getArgs().size() == 1 && session.getPort() == 0 ) {
        if ( SecureFTP.explicitSSLOnly || 
	     secModeSet && (session.getSecurityMode() == FTPSession.EXPLICIT_SSL ||
		            session.getSecurityMode() == FTPSession.NO_SECURITY) ) {
          session.setPort( SecureFTP.getDefaultExplicitSSLPort() );
        }
        else {
          session.setPort( SecureFTP.getDefaultImplicitSSLPort() );
        }
        weSetDefaultPort = true;
      }
      else if ( getArgs().size() == 2 ) {
        try {
          session.setPort( Integer.parseInt((String)getArgs().get(1)) );
        }
        catch ( NumberFormatException nfe ) {
          // at this point, we're okay (passed verification earlier).
        }
      }
    }

    if ( !displayedConnectionWarning &&
         FTPSession.NO_SECURITY != session.getSecurityMode() &&
         SecureFTP.getDefaultExplicitSSLPort() != session.getPort() && 
         SecureFTP.getDefaultImplicitSSLPort() != session.getPort() ) {
      out.println("");
      out.println("!!! WARNING !!!: " +
                  "This is a non-standard port for FTP over SSL.");
      if ( 22 == session.getPort() ) {
        out.println("                 " +
                    "This client does not support FTP over SSH.");
      }
      out.println("");
      out.println("Continuing connection attempt...");
      displayedConnectionWarning = true;
    }
    else if ( !displayedConnectionWarning && SecureFTP.explicitSSLOnly &&
              SecureFTP.getDefaultImplicitSSLPort() == session.getPort() ) {
      out.println("");
      out.println("!!! WARNING !!!: This is a non-standard " +
                         "port for an explicit SSL connection.");
      out.println("");
      out.println("Continuing connection attempt...");
      displayedConnectionWarning = true;
    }

    if ( secModeSet ) {
      // don't touch it, it was set by the user
    }

    // by default we try and make an implicit ssl connection first (unless
    // otherwise overriden by a system property...
    else if ( SecureFTP.explicitSSLOnly && 
         session.getSecurityMode() != FTPSession.NO_SECURITY ) {
      session.setSecurityMode( FTPSession.EXPLICIT_SSL );
    }

    // but... if we are trying to connect to port 21, nix the implicit
    // attempt
    else if ( FTPSession.IMPLICIT_SSL == session.getSecurityMode() &&
         SecureFTP.getDefaultExplicitSSLPort() == session.getPort() ) {
      session.setSecurityMode( FTPSession.EXPLICIT_SSL );
    }

    try {
      FTP bean = null;
      boolean isSecure = session.isSecure();

      OutputStream outputStream = session.getOutputStream();

      if ( FTPSession.IMPLICIT_SSL == session.getSecurityMode() ) {
        bean = new SSLFTP(session.getCertHandler(),
                          session.getHostName(), session.getPort(), 
                          session.getKeyStoreFile(), null,
                          SSLFTP.IMPLICIT_CONNECTION, outputStream, 
                          outputStream); 

        setClientAuth( (SSLFTP)bean, session );

        isSecure = true;
      }
      else if ( FTPSession.EXPLICIT_SSL == session.getSecurityMode() ) {
        bean = new SSLFTP(session.getCertHandler(),
                          session.getHostName(), session.getPort(), 
                          session.getKeyStoreFile(), null,
                          SSLFTP.EXPLICIT_CONNECTION, outputStream, 
                          outputStream); 
       
        setClientAuth( (SSLFTP)bean, session );

        isSecure = true; 
      }
      else {
        bean = new FTP(session.getHostName(), session.getPort(), 
                       outputStream, outputStream); 
        isSecure = false;
      }

      session.setFTPBean( bean );
      session.setIsSecure( isSecure );

      String securityType = " an insecure ";
      if ( isSecure ) {
        if ( FTPSession.IMPLICIT_SSL == session.getSecurityMode() ) {
          securityType = " an implicit SSL ";
        }
        else {
          securityType = " an explicit SSL ";
        }
      }

      out.println("Attempting to make" + securityType + 
                  "connection to " + session.getHostName() + 
                  " on port " + session.getPort() + ".");

      boolean connectionAborted = false;

      try {
        if ( isSecure ) {
          ((SSLFTP)bean).connect( true );
          if ( !bean.isConnected() ) {
            out.println("Connection lost.");
            throw new FTPAuthNotSupportedException();
          }
        }
        else {
          bean.connect();
        }
      }
      catch ( FTPPolicyRestrictionException fpre ) {
        //tryInsecureConnection( session );
        try {
          bean.logout();
          tryAuthSSL( bean, session );
        }
        catch ( FTPPolicyRestrictionException fpre2 ) {
          connectionAborted = tryInsecureConnection( session );
        }
        catch ( FTPAuthNotSupportedException fce2 ) {
          connectionAborted = tryInsecureConnection( session );
        }
      }
      catch ( FTPAuthNotSupportedException fce ) {
        try {
          bean.logout();
          tryAuthSSL( bean, session );
        }
        catch ( FTPPolicyRestrictionException fpre2 ) {
          connectionAborted = tryInsecureConnection( session );
        }
        catch ( FTPAuthNotSupportedException fce2 ) {
          connectionAborted = tryInsecureConnection( session );
        }
      }

      if ( bean.isConnected() ) {
         boolean force = GTOverride.getBoolean("forcePasvToUseControlIP");
         bean.forcePasvToUseControlIP( force );

        if ( !SecureFTP.scripted )
          result = SecureFTP.getCommandDispatcher().fireCommand(this, 
                                                            new LoginCommand());
          if ( result.getCode() == SecureFTPError.LOGIN_FAILED ) {
            session.setUserName(null);
            session.setPassword(null);
            session.setAccount(null);
          }
      }
      else if ( !connectionAborted ) {
        out.println("A connection error has occured.");
      }
    }
    catch ( UnknownHostException uhe ) {
      out.println(uhe.getMessage() + ": unknown host");
      session.clearSession();
    }
    catch ( IOException ioe ) {
      out.println("Connection failed: " + ioe.getMessage());
      tryAgain( session, weSetDefaultPort ); 
    }
    catch ( FTPConnectException ce ) {
      out.println(ce.getMessage());
      //if ( ! SecureFTP.explicitSSLOnly ) {
        tryAgain( session, weSetDefaultPort ); 
      //}
    }
    catch ( FTPException fe ) {
      out.println(fe.getMessage());
      session.clearSession();
    }

    displayedConnectionWarning = false;

    return result;
  }

  public void setClientAuth( SSLFTP ftp, FTPSession session ) {
    PrintStream out = session.getPrintStream();
    File[] certChain = null;
  
    String privateKey = GTOverride.getString("client.private_key");
    String publicCert = GTOverride.getString("client.public_cert");
    String ca         = GTOverride.getString("client.ca");
    String pass       = GTOverride.getString("client.cert.pass");

    if ( null == privateKey || null == publicCert ) {
      try {
        ftp.clearClientAuthentication();
      }
      catch ( Exception e ) {}
      return;
    }

    File privateFile = new File( privateKey );

    if ( null == ca ) {
      certChain = new File[1];
      certChain[0] = new File( publicCert ); 
    }
    else {
      certChain = new File[2];
      certChain[0] = new File( publicCert ); 
      certChain[1] = new File( ca ); 
    }

    try {
      ftp.setClientAuthentication( privateFile, certChain, pass );
    }
    catch ( Exception e ) { 
      try {
        ftp.clearClientAuthentication();
      }
      catch ( Exception cca ) {}
      out.println("There was a problem setting the client authentication: " +
                   e.getMessage()); 
    }
  }

  public void tryAgain( FTPSession session, boolean resetPort ) {
    PrintStream out = SecureFTP.getFTPSession().getPrintStream();

    if ( !secModeSet && FTPSession.IMPLICIT_SSL == session.getSecurityMode() ) {
      session.setSecurityMode( FTPSession.EXPLICIT_SSL );
    }
    else if ( !secModeSet && FTPSession.EXPLICIT_SSL == session.getSecurityMode() ) {
      session.setSecurityMode( FTPSession.NO_SECURITY );
    }
    else {
      if (!secModeSet)
        out.println("Giving up.");
      session.clearSession();
      return;
    }
  
    // if we set the port to the default implicit port and we are here, 
    // set the port to the default explicit port and try again
    if ( resetPort ) {
      session.setPort( SecureFTP.getDefaultExplicitSSLPort() );
    }

    SecureFTP.getCommandDispatcher().fireCommand(this, this);
  }

  private void tryAuthSSL( FTP bean, FTPSession session ) 
                           throws FTPException, IOException {
    PrintStream out = session.getPrintStream();
    OutputStream outputStream = session.getOutputStream();

    out.println("AUTH TLS not suppoted. Trying AUTH SSL...");

    ((SSLFTP)bean).setAuthType("SSL");
    ((SSLFTP)bean).connect( true );

    if ( !bean.isConnected() ) {
      out.println("Connection lost.");
      bean.logout();
      bean = new FTP(session.getHostName(), session.getPort(), 
                     outputStream, outputStream); 
      session.setFTPBean( bean );
      bean.connect();
      throw new FTPAuthNotSupportedException();
    }
  }

  private boolean tryInsecureConnection( FTPSession session ) {
    boolean connectionAborted = false;

    PrintStream out = session.getPrintStream();

    String msg = "A secure connection could not be established.";

    boolean continueWithoutSecurity = SecureFTP.securityDisabled;
 
    if ( !secModeSet && !SecureFTP.securityDisabled && !SecureFTP.scripted ) {
      continueWithoutSecurity = CLIUtil.yesNoPrompt(msg + " Continue anyway?");
    }
    else {
      out.println( msg );
    }

    if ( continueWithoutSecurity ) {
      // let it go...
      session.setIsSecure( false );
      session.setSecurityMode( FTPSession.NO_SECURITY );
    }
    else {
      out.println("Connection aborted.");
      SecureFTP.getCommandDispatcher().fireCommand(this, new CloseCommand());
      connectionAborted = true;
    }

    return connectionAborted;
  }
}

