
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: ConnectCommand.java 133 2009-12-11 08:44:22Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.secureftp.bean.*;
import com.glub.util.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

import javax.swing.*;

public class ConnectCommand extends LocalCommand {
  private static MakingConnectionDialog makingConnectionDialog = null;

  public ConnectCommand() {
    super("connect", CommandID.CONNECT_COMMAND_ID, 1, 1,
          "bookmark", "connect to bookmark");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    Bookmark book = (Bookmark)getArgs().get( 0 );
    FTPSession session = FTPSessionManager.getInstance().getCurrentSession();

    ConnectThread ct = new ConnectThread( book, session );
    ct.start();

    makingConnectionDialog = 
      new MakingConnectionDialog( SecureFTP.getBaseFrame(), ct );

    return result;
  }

  public static MakingConnectionDialog getMakingConnectionDialog() {
    return makingConnectionDialog;
  }

  public static void disposeDialog() {
    makingConnectionDialog = null;
  }
}

class ConnectThread extends Thread {
  private static final String classPath = "ConnectThread.";
  private Bookmark book = null;
  private FTPSession session = null;
  private boolean tryingToConnect = true;
  private static final int MAX_ATTEMPTS = 3;
  private JTextArea ta = new JTextArea( 4, 20 );

  public ConnectThread( Bookmark book, FTPSession session ) {
    super();
    this.book = book;
    this.session = session;
  }

  public void run() {
    FTP bean = null;

    ta.addMouseListener( new MouseAdapter() {
      public void mouseReleased( MouseEvent e ) {
        LogPopupMenu popupMenu = new LogPopupMenu( ta );
        int macContextMask = MouseEvent.CTRL_DOWN_MASK;
        int unixContextMask = MouseEvent.META_DOWN_MASK;

        int sysContextMask = Util.isMacOS() ? macContextMask : unixContextMask;

        if ( e.isPopupTrigger() ) {
          popupMenu.show( e.getComponent(), e.getX(), e.getY() );
        }
        else if ( !Util.isWindows() &&
                  ((e.getModifiersEx() & macContextMask) == sysContextMask ||
                   (e.getModifiersEx() & unixContextMask) == sysContextMask ||
                   4 == e.getModifiers()) ) {
          popupMenu.show( e.getComponent(), e.getX(), e.getY() );
        }
      }
    } );

    LogStream ls = new LogStream( new ByteArrayOutputStream(), ta );
    session.setOutputStream( ls );

    SecureFTP.getBaseFrame().setCursor( new Cursor(Cursor.WAIT_CURSOR) );

    debug( "Connection type: " + session.getSecurityMode() );

    if ( FTPSession.IMPLICIT_SSL == session.getSecurityMode() ) {
      bean = new SSLFTP( session.getCertHandler(), session.getHostName(),
                         session.getPort(), session.getKeyStoreFile(), null,
                         SSLFTP.IMPLICIT_CONNECTION, session.getOutputStream(),
                         session.getOutputStream() );
    }
    else if ( FTPSession.EXPLICIT_SSL == session.getSecurityMode() ) {
      bean = new SSLFTP( session.getCertHandler(), session.getHostName(),
                         session.getPort(), session.getKeyStoreFile(), null,
                         SSLFTP.EXPLICIT_CONNECTION, session.getOutputStream(),
                         session.getOutputStream() );
    }
    else {
      bean = new FTP( session.getHostName(), session.getPort(), 
                      session.getOutputStream(), session.getOutputStream() );
    }

    if ( session.usesProxy() ) {
      bean.setSocksVProxy( Client.getSocksHostName(), 
                           Client.getSocksPort(), 
                           Client.getSocksUserName(), 
                           Client.getSocksPassword() ); 
    }

    short connType = book.isPassiveConnection() ? FTP.PASSIVE_CONNECTION_TYPE :
                                                  FTP.ACTIVE_CONNECTION_TYPE;
    bean.setConnectionType( connType );
    bean.forcePasvToUseControlIP( Client.forcePasvControlIP() );

    session.setFTPBean( bean );

    // just to make sure we don't try forever
    int connectionAttempts = 0;

    do {
      connect();
      connectionAttempts++;
    }
    while( tryingToConnect && connectionAttempts <= MAX_ATTEMPTS );

  }

  public void connect() {
    Client.getMenus().disableMenuBar();
    Client.getToolBar().disableToolBar();

    boolean successfulConnect = false;
    boolean loginFailed = false;

    FTP bean = session.getFTPBean();
    String hostName = session.getHostName();

    try {
      debug( "Connection attempt begun" );

      if ( session.isSecure() ) {
        // set the client cert if available
        setClientCert( (SSLFTP)bean );

        ((SSLFTP)bean).connect( book.isDataEncrypted() );
      }
      else {
        bean.connect();
      }
      tryingToConnect = false;

      debug( "Connected" );

      if ( isInterrupted() ) {
        debug( "Operation cancelled" );
        return;
      }

      int loginAttempts = 1;

      if ( session.getUserName().length() == 0 ) {
        if ( !AuthenticationDialog.showDialog(SecureFTP.getBaseFrame(),
                                              session) ) {
          loginAttempts = MAX_ATTEMPTS + 1;
	}
      }

      if ( isInterrupted() ) {
        debug( "Operation cancelled" );
        return;
      }


      do {
        if ( isInterrupted() ) {
          debug( "Operation cancelled" );
          return;
        }

        try {
          debug( "Logging in" );

          bean.login( session.getUserName(), session.getPassword() );

          if ( isInterrupted() ) {
            debug( "Operation cancelled" );
            return;
          }

          debug( "Logged in" );

	  loginFailed = false;
	}
	catch ( IllegalArgumentException iae ) {
          debug( iae.getMessage() );

          boolean infoChanged = false;
          if ( loginAttempts <= MAX_ATTEMPTS ) {
            infoChanged =
              AuthenticationDialog.showDialog( SecureFTP.getBaseFrame(), 
                                               session );
          }

	  if ( !infoChanged ) {
            loginAttempts = MAX_ATTEMPTS + 1;
	    loginFailed = true;
	  }
	  else {
	    loginAttempts++;
	  }
	}
	catch ( FTPBadLoginException fble ) {
          debug( fble.getMessage() );

          boolean infoChanged = false;
          if ( loginAttempts <= MAX_ATTEMPTS ) {
            infoChanged =
              AuthenticationDialog.showDialog( SecureFTP.getBaseFrame(), 
                                               session );
          }

	  if ( !infoChanged ) {
            loginAttempts = MAX_ATTEMPTS + 1;
	    loginFailed = true;
	  }
	  else {
	    loginAttempts++;
	  }
	}
	catch ( FTPNeedPasswordException fnpe ) {
          debug( fnpe.getMessage() );

          boolean infoChanged = false;
          if ( loginAttempts <= MAX_ATTEMPTS ) {
            infoChanged =
              AuthenticationDialog.showDialog( SecureFTP.getBaseFrame(), 
                                               session );
          }

	  if ( !infoChanged ) {
            loginAttempts = MAX_ATTEMPTS + 1;
	    loginFailed = true;
	  }
	  else {
	    loginAttempts++;
	  }
	}
	catch ( FTPException fe ) {
          debug( fe.getMessage() );

	  loginAttempts = MAX_ATTEMPTS + 1;
	  loginFailed = true;
	}
      } while ( bean.isConnected() && !bean.isLoggedIn() && 
                !loginFailed );

      if ( isInterrupted() ) {
        debug( "Operation cancelled" );
        return;
      }

      if ( bean.isConnected() && bean.isLoggedIn() ) {
        successfulConnect = true;
      }
    }
    catch ( UnknownHostException uhe ) {
      LString lmsg = 
        new LString( classPath + "unknownHost", "[^0]: unknown host" );
      lmsg.replace( 0, uhe.getMessage() );

      ErrorDialog.showDialog( 
        new LString(classPath + "connectionFailed.dialogTitle",
                    "Connection Failed"), lmsg );
      tryingToConnect = false;
    }
    catch ( IOException ioe ) {
      LString lmsg = 
        new LString(classPath + "connectionFailed1", "Connection failed: [^0]");
      lmsg.replace( 0, ioe.getMessage() );

      ErrorDialog.showDialog(
        new LString(classPath + "connectionFailed.dialogTitle",
                    "Connection Failed"), lmsg );
      tryingToConnect = false;
    }
    catch ( FTPPolicyRestrictionException fpre ) {
      boolean tryAgain = tryAuthSSL();
      if ( tryAgain ) {
        return;
      }
    }
    catch ( FTPAuthNotSupportedException fanse ) {
      boolean tryAgain = tryAuthSSL();
      if ( tryAgain ) {
        return;
      }
    }
    catch ( FTPException ce ) { 
      LString lmsg = 
        new LString(classPath + "connectionFailed2", "[^0]: connection failed");

      if ( null != hostName ) {
        lmsg.replace( 0, hostName );
      }
      else {
        lmsg.replace( 0, "" );
      }

      ErrorDialog.showDialog(
        new LString(classPath + "connectionFailed.dialogTitle",
                    "Connection Failed"), lmsg );
      tryingToConnect = false;
    }

    if ( isInterrupted() ) {
      debug( "Operation cancelled" );
      return;
    }

    if ( successfulConnect ) {
      debug( "Connection successful" );

      if ( SecureFTP.forceEncrypt && session.isSecure() ) {
        try {
          ((SSLFTP)bean).forceDataEncryptionOn( true );
        }
        catch ( FTPException fe ) {
          debug( fe.getMessage() );
        }
      }

      if ( isInterrupted() ) {
        debug( "Operation cancelled" );
        return;
      }

      if ( session.useCCC() && session.isSecure() ) {
        LString lmsg = 
          new LString(classPath + "cccNotSupported", 
                      "Clear Command Channel request not supported by server.");
        try {
          ((SSLFTP)bean).setClearCommandChannel();
        }
        catch ( FTPException fe ) { 
          ErrorDialog.showDialog(
            new LString(classPath + "connectionFailed.dialogTitle",
                        "Connection Failed"), lmsg );
          successfulConnect = false;
        }
        catch ( IOException ioe ) { 
          ErrorDialog.showDialog(
            new LString(classPath + "connectionFailed.dialogTitle",
                        "Connection Failed"), lmsg );
          successfulConnect = false;
        }
      }

      if ( isInterrupted() ) {
        debug( "Operation cancelled" );
        return;
      }

      try {
        if ((System.getProperty("file.encoding") != null &&
             System.getProperty("file.encoding").toLowerCase().equals("utf8")) ||
             (Client.getEncoding() != null && Client.getEncoding().toLowerCase().equals("utf8"))) {
          session.getFTPBean().setSendCmdStream(null);
          session.getFTPBean().setRecvCmdStream(null);

          bean.setStringDataAsUTF8( true );

          session.getFTPBean().setSendCmdStream(session.getOutputStream());
          session.getFTPBean().setRecvCmdStream(session.getOutputStream());
        }
      }
      catch ( Exception e ) {}

      if ( isInterrupted() ) {
        debug( "Operation cancelled" );
        return;
      }

      MakingConnectionDialog mcd = ConnectCommand.getMakingConnectionDialog();

      if ( null != mcd ) {
        mcd.dispose();
      }

      SecureFTP.getBaseFrame().setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );

      if ( successfulConnect ) {
        session.setTransferRestartable( bean.isTransferRestartable() );

        boolean buildComboBox = true;
        
        if ( book.getRemoteFolder() != null && 
             book.getRemoteFolder().length() > 0 ) {
        	buildComboBox = false;
        }
        
        Client.getRemoteView().addConnection( session, ta, buildComboBox );

        int transferMode = Client.getTransferMode();

        Command transferModeCmd = null;

        switch( transferMode ) {
          case FTP.BINARY_TRANSFER_MODE:
            transferModeCmd = new BinaryXferCommand();
            break;

          case FTP.ASCII_TRANSFER_MODE:
            transferModeCmd = new AsciiXferCommand();
            break;

          case FTP.AUTO_TRANSFER_MODE:
          default:
            transferModeCmd = new AutoXferCommand();
            break;
        }

        SecureFTP.getCommandDispatcher().fireCommand( this, transferModeCmd );

        if ( Client.useModeZCompression() ) {
          try {
            bean.modeZ();   
          }
          catch ( FTPException fe ) {
          }
        }

        if ( book.getLocalFolder() != null && 
             book.getLocalFolder().length() > 0 ) {
          File newDir = new File( book.getLocalFolder() );
          Client.getLocalView().changeDirectory( newDir );
        }

        if ( book.getRemoteFolder() != null && 
             book.getRemoteFolder().length() > 0 ) {
          RemoteFile newDir = new RemoteFile( book.getRemoteFolder() );
          ((RemotePanel)session.getRemoteUI()).changeDirectory( newDir, false );
        }

        ((RemotePanel)session.getRemoteUI()).setFocus();
      }
      else {
        FTPSessionManager.getInstance().removeCurrentSession();
      }
    }
    else {
      SecureFTP.getBaseFrame().setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );
      LString lmsg = 
        new LString(classPath + "loginFailed", "[^0]: login failed");

      if ( null != hostName ) {
        lmsg.replace( 0, hostName );
      }
      else {
        lmsg.replace( 0, "" );
      }

      if ( loginFailed ) {
        ErrorDialog.showDialog(
          new LString(classPath + "loginFailed.dialogTitle",
                      "Login Failed"), lmsg );
      }
      
      tryingToConnect = false;

      FTPSessionManager.getInstance().removeCurrentSession();
    }

    // make the dialog go away!
    MakingConnectionDialog mcd = ConnectCommand.getMakingConnectionDialog();

    if ( null != mcd ) {
      mcd.dispose();
    }

    SecureFTP.getBaseFrame().setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );

    Client.getMenus().enableMenuBar();
    Client.getToolBar().enableToolBar();
  }

  public void debug( String msg ) {
    if ( session.isDebugOn() )
     System.out.println( msg );
  }

  protected boolean tryAuthSSL() {
    boolean tryAgain = false;
    FTP bean = session.getFTPBean();
    String hostName = session.getHostName();

    try {
      bean.logout();
    }
    catch ( Exception e ) {}

    if ( session.isSecure() && ((SSLFTP)bean).getAuthType().equals("TLS") ) {
      // try AUTH SSL before giving up
      ((SSLFTP)bean).setAuthType( "SSL" );
      tryAgain = true;
    } 
    else if ( session.isSecure() ) {
      // try insecure connection
      String msg = LString.getString( classPath + "makeInsecureConnection",
        "A secure connection (explicit SSL) could not be established.\n" +
        "Do you want to continue with an insecure connection?" ); 

      String[] options = { LString.getString("Common.button.yes", "Yes"),
                           LString.getString("Common.button.no", "No") };

      int result = 
        JOptionPane.showOptionDialog( SecureFTP.getBaseFrame(), msg, 
                  LString.getString(classPath + "connectionFailed.dialogTitle", 
                                                "Connection Failed"), 
                                      JOptionPane.YES_NO_OPTION, 
                                      JOptionPane.QUESTION_MESSAGE,
                                      null,
                                      options,
                                      options[1]);
      tryAgain = ( JOptionPane.YES_OPTION == result );
      if ( tryAgain ) {
        bean = new FTP( session.getHostName(), session.getPort(), 
                        session.getOutputStream(), session.getOutputStream() );
	session.setIsSecure( false );
	session.setFTPBean( bean );
      }
      else {
        tryingToConnect = false;
      }
    }
    else {
      // give up
      LString lmsg = 
        new LString(classPath + "connectionFailed2", "[^0]: connection failed");

      if ( null != hostName ) {
        lmsg.replace( 0, hostName );
      }
      else {
        lmsg.replace( 0, "" );
      }

      ErrorDialog.showDialog(  
        new LString(classPath + "connectionFailed.dialogTitle",
                    "Connection Failed"), lmsg );
      tryingToConnect = false;
    }

    return tryAgain;
  }

  private void setClientCert( SSLFTP ftp ) {
    File privateFile = Client.getClientPrivateKey();
    File publicFile = Client.getClientPublicCert();
    File caFile = Client.getClientCACert();
    String password = Client.getClientCertPassword();

    File[] certs = null;

    if ( privateFile != null && publicFile != null ) {
      if ( null == caFile ) {
        certs = new File[1];
        certs[0] = publicFile;
      }
      else {
        certs = new File[2];
        certs[0] = publicFile;
        certs[1] = caFile;
      }

      try {
        ftp.setClientAuthentication( privateFile, certs, password );
      }
      catch ( Exception e ) {
        try {
          ftp.clearClientAuthentication();
        }
        catch ( Exception e1 ) {}
      }
    }
    else {
      try {
        ftp.clearClientAuthentication();
      }
      catch ( Exception e1 ) {}
    }
  }
}

