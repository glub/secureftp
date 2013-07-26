
//*****************************************************************************
//*
//* (c) Copyright 2005. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: HelpViewerThread.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import java.net.*;
import javax.help.*;

public class HelpViewerThread extends Thread {
  //Runnable runnable;
  HelpBroker hb = null;
 
  public HelpViewerThread() {
    super();
/*
    runnable = new Runnable() {
      public void run() {
        buildHelp();
      }
    };
*/
  }
 
  public HelpBroker getHelpBroker() { return hb; }

  public void run() {
    //SwingUtilities.invokeLater(runnable);
    buildHelp();
  }

  private void buildHelp() {
    if (hb != null)
      return;

    String helpHS = "com/glub/secureftp/client/resources/help/SecureFTP.hs";
    ClassLoader cl = this.getClass().getClassLoader();
    HelpSet hs = null;
    try {
      URL hsURL = HelpSet.findHelpSet(cl, helpHS);
      hs = new HelpSet(null, hsURL);
    }
    catch ( Exception e ) { return; }
    hb = hs.createHelpBroker();
  }
}
