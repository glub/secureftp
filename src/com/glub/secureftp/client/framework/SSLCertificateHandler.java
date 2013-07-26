
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: SSLCertificateHandler.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.framework;

import com.glub.secureftp.bean.*;

public abstract class SSLCertificateHandler implements SSLSessionManager {

  public abstract void setCurrentCertificate( SSLCertificate cert );

  public abstract short newCertificateEncountered( SSLCertificate cert );

  public abstract short replaceCertificate( SSLCertificate oldCert, 
                                   SSLCertificate newCert );

  public abstract boolean continueWithoutServerCertificate();

  public abstract boolean continueWithExpiredCertificate( SSLCertificate cert );

  public abstract boolean continueWithInvalidCertificate( SSLCertificate cert );

  public abstract boolean continueWithCertificateHostMismatch( 
		                                      SSLCertificate cert,
                                                      String actualHost,
                                                      String certHost );

  public abstract void randomSeedIsGenerating();

  public abstract void randomSeedGenerated();

}
