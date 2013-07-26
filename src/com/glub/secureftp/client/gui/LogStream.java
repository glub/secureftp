
//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LogStream.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import java.awt.*;
import java.io.*;
import javax.swing.*;

public class LogStream extends PrintStream {
  private JTextArea textArea;
  private ByteArrayOutputStream outputStream = null;

  public LogStream( ByteArrayOutputStream baos, JTextArea textArea ) {
    super( baos );
    this.outputStream = baos;
    this.textArea = textArea;
    textArea.setBorder( BorderFactory.createCompoundBorder(textArea.getBorder(),
                        BorderFactory.createLineBorder(Color.white, 2) ) );
    textArea.setEditable( false );
    textArea.setAutoscrolls( true );
  }
 
  public void write( byte[] b ) throws IOException {
    outputStream.write( b );
    outputStream.flush();
    textArea.append( outputStream.toString() );
    textArea.setCaretPosition( textArea.getText().length() );
    outputStream.reset();
  }

  public void write( byte[] b, int off, int len ) {
    outputStream.write( b, off, len );
    try {
      outputStream.flush();
    } catch ( IOException ioe ) {}
    textArea.append( outputStream.toString() );
    textArea.setCaretPosition( textArea.getText().length() );
    outputStream.reset();
  }

  public void write( int b ) {
    outputStream.write( b );
    try {
      outputStream.flush();
    } catch ( IOException ioe ) {}
    textArea.append( outputStream.toString() );
    textArea.setCaretPosition( textArea.getText().length() );
    outputStream.reset();
  }
}
