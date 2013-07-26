
//*****************************************************************************
//*
//* (c) Copyright 2005. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: EULADialog.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import javax.swing.*;

public class EULADialog {

  private JTextArea eulaTA;
  private JScrollPane scrollPane;
  private Object[] buttonRow = new Object[] { "Accept", "Cancel" };

  public EULADialog() {
    eulaTA = new JTextArea(eula, 20, 40);
    eulaTA.setWrapStyleWord(true);
    eulaTA.setLineWrap(true);
    eulaTA.setEditable(false);

    scrollPane = new JScrollPane(eulaTA);

    int response = JOptionPane.showOptionDialog( SecureFTP.getBaseFrame(),
						 scrollPane,
						 "End User License Agreement",
						 JOptionPane.YES_NO_OPTION,
						 JOptionPane.PLAIN_MESSAGE,
						 null,
						 buttonRow,
						 buttonRow[0] );
    if ( response == JOptionPane.NO_OPTION ) {
      System.exit(0);
    }
    
  }

  private final static String eula = 
"End User License Agreement for Secure FTP 2.5:\n\n" +

"By clicking on the \"Accept\" button you agree to the following terms:\n\n" +

"Permission to use, copy, and distribute this Secure FTP 2.5 program for " +
"educational, non-profit research, and other non-profit organizations " +
"without fee, commercial purposes " +
"with fee and without a written agreement is hereby granted, provided that " +
"the copyright notice, this paragraph and the following paragraphs " +
"appear in all copies.\n\n" +

"IN NO EVENT SHALL GLUB TECH INCORPORATED BE LIABLE " +
"TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL " +
"DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS SECURE FTP " +
"2.5 PROGRAM, EVEN IF GLUB TECH INCORPORATED HAS BEEN " +
"ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. \n\n" +

"THE SECURE FTP 2.5 PROGRAM PROVIDED HEREIN IS ON AN \"AS IS\" BASIS, AND " +
"GLUB TECH INCORPORATED DOES NOT HAVE ANY OBLIGATION " +
"TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS. " +
"GLUB TECH INCORPORATED MAKES NO REPRESENTATIONS AND " +
"EXTENDS NO WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, " +
"BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS " +
"FOR A PARTICULAR PURPOSE, OR THAT THE USE OF THE SECURE FTP 2.5 PROGRAM WILL " +
"NOT INFRINGE ANY PATENT, TRADEMARK OR OTHER RIGHTS. \n\n" +

"Those desiring to incorporate this Secure FTP 2.5 program into commercial " +
"products should contact: \n\n" +

"Glub Tech \n" +
"PO Box 948484 \n" +
"La Jolla, CA 92037 \n" +
"secureftp@glub.com \n\n" +

"Secure FTP 2.5 may contain encryption technology that is subject to the " +
"U.S. Export Administration Regulations and other U.S. law, and may not " +
"be exported or re-exported to certain countries (currently Afghanistan " +
"(Taliban-controlled areas), Cuba, Iran, Iraq, Libya, North Korea, Serbia " +
"(except Kosovo), Sudan and Syria) or to persons or entities prohibited " +
"from receiving U.S. exports (including Denied Parties, entities on the " +
"Bureau of Export Administration Entity List, and Specially Designated " +
"Nationals). For more information on the U.S. Export Administration " +
"Regulations (EAR), 15 C.F.R. Parts 730-774, and the Bureau of Export " +
"Administration (\"BXA\"), please see the BXA homepage.\n\n" + 

"By accepting this agreement, you are agreeing that you are not " +
"from a country currently affected by a US embargo and that you will " +
"not export this program to a country that is affected by a US " +
"embargo.\n\n" +

"Copyright 1999-2009 Glub Tech Incorporated \n" +
"All Rights Reserved. \n";

}

