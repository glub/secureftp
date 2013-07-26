//*****************************************************************************
//*
//* (c) Copyright 2005. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LogPopupMenu.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.util.*;

import javax.swing.*;

import java.awt.event.*;
import java.io.*;

public class LogPopupMenu extends JPopupMenu {
  protected static final long serialVersionUID = 1L;
  private JMenuItem saveLogMI = null;
  private JMenuItem clearLogMI = null;

  private JTextArea log = null;

  public LogPopupMenu( JTextArea textArea ) {
    super();
    log = textArea;
    setupPopupMenu();
  }

  protected void setupPopupMenu() {
   String mi = LString.getString("Log.clear", "Clear log");
   clearLogMI = new JMenuItem( mi );
   clearLogMI.addActionListener( new ActionListener() {
     public void actionPerformed( ActionEvent e ) {
       log.setText("");
     }
   } );

   mi = LString.getString("Log.save", "Save log...");
   saveLogMI = new JMenuItem( mi );
   saveLogMI.addActionListener( new ActionListener() {
     public void actionPerformed( ActionEvent e ) {
       File localDir = Client.getLocalView().getCurrentDirectory();
       JFileChooser fc = new JFileChooser( localDir );        
       fc.setFileSelectionMode( JFileChooser.FILES_ONLY );
       int result =
         fc.showDialog( SecureFTP.getBaseFrame(),
                        LString.getString("Common.button.select_file",
                                          "Select File") );
       if ( JFileChooser.APPROVE_OPTION == result ) {
         File outputFile = fc.getSelectedFile();
         try {
           FileOutputStream fos = new FileOutputStream( outputFile );
           PrintWriter writer = new PrintWriter( fos );
           writer.println( log.getText() );
           writer.flush();
           writer.close();
           fos.close();
         }
         catch ( Exception ex ) {}
       }
     }
   } );

   add( saveLogMI );
   add( clearLogMI );
  }
}

