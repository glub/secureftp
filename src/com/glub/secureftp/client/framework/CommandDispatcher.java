
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: CommandDispatcher.java 138 2009-12-15 15:52:55Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.framework;

import java.awt.event.ActionListener;
import javax.swing.event.EventListenerList;

public class CommandDispatcher extends EventListenerList {
  protected static final long serialVersionUID = 1L;	

  public CommandDispatcher() {
    super();
  }

  public void addListener( Class c, ActionListener l ) {
    //System.out.println(c);
    add( c, l );
  }

  public void removeListener( Class c, ActionListener l ) {
    remove( c, l );
  }

  public SecureFTPError fireCommand( Object source, Command command ) { 
    return fireCommand( source, command, false );
  }

  public SecureFTPError fireCommand( Object source, Command command, 
                                     boolean recordIt ) { 
    SecureFTPError result = new SecureFTPError();
    try {
      // Guaranteed to return a non-null array
      Object[] listeners = getListenerList();
    
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for ( int i = listeners.length - 2; i >= 0; i -= 2 ) {
        Class[] interfaces = ((Class)listeners[i]).getInterfaces();
        boolean foundInterface = false;
        for ( int j = 0; j < interfaces.length; j++ ) {
          foundInterface = (CommandHandler.class == interfaces[j]);
          if ( foundInterface )
            break;
        }

        boolean dispatchIt = true;

        if ( !recordIt ) {
          dispatchIt = Automation.class != (Class)listeners[i];
        }

        if ( foundInterface && dispatchIt ) {
          // only output for the first listener
          if ( i == 0 &&
               FTPSessionManager.getInstance().getCurrentSession() != null &&
               FTPSessionManager.getInstance().getCurrentSession().isDebugOn() 
               && command.getCommandName().length() > 0 ) {
            System.out.println("Command \"" + command.getCommandName() +
                               "\" dispatched.");
          }

          SecureFTPError tRes = 
            ((CommandHandler)listeners[i+1]).handleCommand( command );

	  if ( listeners[i+1] instanceof CommandPlayer ) {
            result = tRes;
          }
        }
      }
    }
    catch ( NotConnectedCommandException ncce ) {
      result = new SecureFTPError( SecureFTPError.NOT_CONNECTED );
    }
    catch ( CommandException ce ) {
      if ( FTPSessionManager.getInstance().getCurrentSession() != null &&
           FTPSessionManager.getInstance().getCurrentSession().isDebugOn() ) {
        System.out.println(ce.getMessage());
      }
    }

    return result;
  }

  public void fireMTCommand( final Object source, final Command command ) {
    fireMTCommand( source, command, new SecureFTPError() );
  }

  public void fireMTCommand( final Object source, final Command command, 
                             final SecureFTPError result ) {
    fireMTCommand( source, command, Thread.MIN_PRIORITY, 0, result, false );
  }

  public void fireMTCommand( final Object source, final Command command, 
                             final int priority,
                             final SecureFTPError result, 
                             final boolean recordIt ) { 
    fireMTCommand( source, command, priority, 0, result, recordIt );
  }

  public void fireMTCommand( final Object source, final Command command, 
                             final int priority,
                             final int sleep,
                             final SecureFTPError result, 
                             final boolean recordIt ) { 

    Thread t = new Thread() {
      public void run() {
        if ( sleep > 0 ) {
          try {
	    sleep(sleep);
          }
          catch ( Exception e ) {}
        }
        SecureFTPError tempResult = fireCommand( source, command, recordIt );
        result.setCode( tempResult.getCode() );
        result.setMessage( tempResult.getMessage() );
      }
    };
    t.setPriority( priority );
    t.start();
  }

}
