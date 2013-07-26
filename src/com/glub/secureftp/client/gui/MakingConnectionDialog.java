
//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: MakingConnectionDialog.java 120 2009-12-04 08:33:29Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;

import com.glub.gui.*;
import com.glub.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MakingConnectionDialog extends JDialog {
  protected static final long serialVersionUID = 1L;
  private static final String classPath = "MakingConnectionDialog.";

  private static ConnectThread connectThread;

  protected MakingConnectionDialog( Frame owner, String title, 
                                    ConnectThread thread ) {
    super( owner, title, false );
    connectThread = thread;
    buildDialog();
    pack();

    addWindowListener( new WindowAdapter() {
      public void windowClosing( WindowEvent e ) {
        cancelConnection();
      }
    } );

    setLocationRelativeTo( owner );
    setResizable( false );
    setVisible( true );
  }

  public MakingConnectionDialog( Frame owner, ConnectThread thread ) {
    this( owner, LString.getString(classPath + "dialogTitle", 
                                   "Making Connection..."), thread );
  }

  protected void buildDialog() {
    JPanel dialogPanel = new JPanel();
    dialogPanel.setLayout( new SpringLayout() );

    int rows = 0;

    dialogPanel.add( getConnectionInfoPanel() );
    rows++;

    dialogPanel.add( getProgressPanel() );
    rows++;

    SpringUtilities.makeCompactGrid( dialogPanel,
                                     rows, 1,  // rows, cols
				     20, 5, // init x, init y
				     20, 5  // pad x, pad y
				   );

    getContentPane().setLayout( new BorderLayout() );
    getContentPane().add( dialogPanel, BorderLayout.CENTER );
    getContentPane().add( getButtonPanel(), BorderLayout.SOUTH );
  }

  protected JPanel getConnectionInfoPanel() {
    JPanel panel = new JPanel();
    panel.setLayout( new BoxLayout(panel, BoxLayout.Y_AXIS) );
    panel.setPreferredSize( new Dimension(300, 35) );

    FTPSession session = FTPSessionManager.getInstance().getCurrentSession();

    JLabel connect = 
      new JLabel( LString.getString("MakingConnectionDialog.connectingTo", 
                                    "Connecting to:") );

    JLabel host = new JLabel( session.getHostName() );

    panel.add( connect );
    panel.add( host );

    return panel;
  }

  protected JPanel getProgressPanel() {
    JPanel panel = new JPanel( new BorderLayout() );

    if ( Util.isMacOS() ) {
      final JProgressBar progressBar = new JProgressBar();
      progressBar.setVisible( true );
    
      panel.add( progressBar, BorderLayout.CENTER );

      Runnable r = new Runnable() {
        public void run() {
          progressBar.setIndeterminate( true );
        }
        };

      SwingUtilities.invokeLater( r );
    }
    else {
     ImageIcon progress = 
       new ImageIcon(getClass().getResource("images/progress.gif"));
      panel.add( new JLabel(progress), BorderLayout.CENTER ); 
    }

    return panel;
  }

  protected JPanel getButtonPanel() {
    JPanel panel = new JPanel();

    JButton cancelButton =
      new JButton( LString.getString("Common.button.cancel", "Cancel") );
    SwingUtilities.getRootPane( this ).setDefaultButton( cancelButton );
    cancelButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        cancelConnection();
	dispose();
      }
    } );

    panel.add( cancelButton );

    return panel;
  }

  public void dispose() {
    super.dispose();
    ConnectCommand.disposeDialog();
  }

  protected void cancelConnection() {
    connectThread.interrupt();
    if ( !Thread.interrupted() ) {
      //connectThread.stop();
      connectThread.interrupt();
    }

    FTPSessionManager.getInstance().removeCurrentSession();
    Client.getMenus().enableMenuBar();
    Client.getMenus().updateMenuBar();
    Client.getToolBar().enableToolBar();
    Client.getToolBar().updateToolBar();

    SecureFTP.getBaseFrame().setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );
  }
}
