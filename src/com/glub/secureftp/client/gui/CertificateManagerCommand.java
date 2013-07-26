
//*****************************************************************************
//*
//* (c) Copyright 2005. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: CertificateManagerCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;

public class CertificateManagerCommand extends LocalCommand {
  public CertificateManagerCommand() {
    this("cert_manager", CommandID.CERTIFICATE_MGR_COMMAND_ID);
  }

  public CertificateManagerCommand( String commandName, short id ) {
    super(commandName, id, "cert_manager");
  }

  public SecureFTPError doIt() throws CommandException {
    new EditCertificatesDialog();
    return new SecureFTPError();
  }
}

