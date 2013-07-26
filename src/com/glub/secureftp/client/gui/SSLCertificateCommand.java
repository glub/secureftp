
//*****************************************************************************
//*
//* (c) Copyright 2004. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: SSLCertificateCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;
import com.glub.gui.*;
import com.glub.util.*;

import java.awt.*;
import java.text.*;
import javax.swing.*;

public class SSLCertificateCommand extends NetworkCommand {
  public static final short NEW_CERTIFICATE = 1;
  public static final short REPLACE_CERTIFICATE = 2;
  public static final short SHOW_CERTIFICATE = 3;

  public SSLCertificateCommand() {
    super("cert", CommandID.SSL_CERTIFICATE_COMMAND_ID, 1, 2,  
          "certificate [notification]", "show SSL certificate information");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = new SecureFTPError();
    SSLCertificate cert = (SSLCertificate)getArgs().get(0);
    short commandType = ((Short)getArgs().get(1)).shortValue();

    MakingConnectionDialog mcd = ConnectCommand.getMakingConnectionDialog();

    if ( null != mcd ) {
      mcd.dispose();
    }

    int dialogResult = 0;

    if ( commandType == NEW_CERTIFICATE ) {
      dialogResult = showNewCertDialog( cert );
    }
    else if ( commandType == REPLACE_CERTIFICATE ) {
      dialogResult = showReplaceCertDialog( cert );
    }
    else {
      dialogResult = showCertDialog( cert );
    }
    
    result.setCode( dialogResult );

    return result;
  }

  private int showNewCertDialog( SSLCertificate cert ) {
    int result = 0;

    String[] options = { 
      LString.getString("SSLCertificate.button.grant_once", 
                        "Grant This Session"),
      LString.getString("SSLCertificate.button.deny", 
                        "Deny"),
      LString.getString("SSLCertificate.button.grant_always", 
                        "Grant Always")
    };

    String title = LString.getString( "SSLCertificate.new_certificate.title",
                                      "New Certificate Encountered" );

    int r =      
      JOptionPane.showOptionDialog( SecureFTP.getBaseFrame(),
                                    getCertInfo( cert ),
                                    title,
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.PLAIN_MESSAGE,
                                    null /* icon */,
                                    options,
                                    options[0] /* selected button */ );

    if ( r == 0 ) {
      result = SecureFTPError.ACCEPT_CERTIFICATE_ONCE;
    }
    else if ( r == 1 ) {
      result = SecureFTPError.DENY_CERTIFICATE;
    }
    else if ( r == 2 ) {
      result =  SecureFTPError.ACCEPT_CERTIFICATE_ALWAYS;
    }

    return result;
  }

  private int showReplaceCertDialog( SSLCertificate cert ) {
    int result = 0;

    String[] options = { 
      LString.getString("SSLCertificate.button.grant_once", 
                        "Grant This Session"),
      LString.getString("SSLCertificate.button.deny", 
                        "Deny"),
      LString.getString("SSLCertificate.button.grant_always", 
                        "Grant Always")
    };

    String title = LString.getString("SSLCertificate.replace_certificate.title",
                                     "Different Certificate Encountered" );

    int r =      
      JOptionPane.showOptionDialog( SecureFTP.getBaseFrame(),
                                    getCertInfo( cert ),
                                    title,
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.PLAIN_MESSAGE,
                                    null /* icon */,
                                    options,
                                    options[1] /* selected button */ );

    if ( r == 0 ) {
      result = SecureFTPError.ACCEPT_CERTIFICATE_ONCE;
    }
    else if ( r == 1 ) {
      result = SecureFTPError.DENY_CERTIFICATE;
    }
    else if ( r == 2 ) {
      result =  SecureFTPError.ACCEPT_CERTIFICATE_ALWAYS;
    }

    return result;
  }

  private int showCertDialog( SSLCertificate cert ) {
    int result = 0;

    String[] options = { 
      LString.getString("Common.button.ok", 
                        "OK"),
    };

    String title = LString.getString( "SSLCertificate.certificate.title",
                                      "Certificate Information" );

    JOptionPane.showOptionDialog( SecureFTP.getBaseFrame(),
                                    getCertInfo( cert ),
                                    title,
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.PLAIN_MESSAGE,
                                    null /* icon */,
                                    options,
                                    options[0] /* selected button */ );

    result =  SecureFTPError.OK;

    return result;
  }

  private Object[] getCertInfo( SSLCertificate cert ) {
    int certPanelItems = 0;

    JPanel certPanel = new JPanel();    
    certPanel.setLayout( new BoxLayout(certPanel, BoxLayout.Y_AXIS) );    

    certPanel.add( 
      addItemToPanel(LString.getString("SSLCertificate.issued_to.label",
                                       "Certificate:")) ); 

    if ( cert.getCN() != null && cert.getCN().length() > 0 ) {
      certPanel.add( addItemToPanel(cert.getCN(), true) );
      certPanelItems++;
    }

    if ( cert.getOrg() != null && cert.getOrg().length() > 0 ) {
      certPanel.add( addItemToPanel(cert.getOrg(), true) );
      certPanelItems++;
    }

    if ( cert.getOU() != null && cert.getOU().length() > 0 ) {
      certPanel.add( addItemToPanel(cert.getOU(), true) );
      certPanelItems++;
    }

    StringBuffer locality = new StringBuffer();
    boolean foundCity = false;
    boolean foundState = false;
    boolean foundCountry = false;

    if ( cert.getLocality() != null && cert.getLocality().length() > 0 &&
         !cert.getLocality().equals("?") ) {
      foundCity = true;
      locality.append( cert.getLocality() );
    }

    if ( cert.getState() != null && cert.getState().length() > 0 &&
         !cert.getState().equals("?") ) {
      if ( foundCity ) {
        locality.append( ", " );
      }

      foundState = true;
      locality.append( cert.getState() );
    }

    if ( cert.getCountry() != null && cert.getCountry().length() > 0 &&
         !cert.getCountry().equals("?") ) {
      if ( foundCity || foundState ) {
        locality.append( ", " );
      }

      foundCountry = true;
      locality.append( cert.getCountry() );
    }

    if ( foundCity || foundState || foundCountry ) {
      certPanelItems++;
      certPanel.add( addItemToPanel(locality.toString(), true) );
    }

    int issuePanelItems = 0;

    JPanel issuePanel = new JPanel();    
    issuePanel.setLayout( new BoxLayout(issuePanel, BoxLayout.Y_AXIS) );    

    issuePanel.add( 
      addItemToPanel(LString.getString("SSLCertificate.issued_from.label",
                                       "Issued From:")) ); 

    if ( cert.getIssuerCN() != null && cert.getIssuerCN().length() > 0 ) {
      issuePanel.add( addItemToPanel(cert.getIssuerCN(), true) );
      issuePanelItems++;
    }

    if ( cert.getIssuerOrg() != null && cert.getIssuerOrg().length() > 0 ) {
      issuePanel.add( addItemToPanel(cert.getIssuerOrg(), true) );
      issuePanelItems++;
    }

    if ( cert.getIssuerOU() != null && cert.getIssuerOU().length() > 0 ) {
      issuePanel.add( addItemToPanel(cert.getIssuerOU(), true) );
      issuePanelItems++;
    }

    locality.delete( 0, locality.length() );
    foundCity = false;
    foundState = false;
    foundCountry = false;

    if ( cert.getIssuerLocality() != null && 
         cert.getIssuerLocality().length() > 0 &&
         !cert.getIssuerLocality().equals("?") ) {
      foundCity = true;
      locality.append( cert.getIssuerLocality() );
    }

    if ( cert.getIssuerState() != null && cert.getIssuerState().length() > 0 &&
         !cert.getIssuerState().equals("?") ) {
      if ( foundCity ) {
        locality.append( ", " );
      }

      foundState = true;
      locality.append( cert.getIssuerState() );
    }

    if ( cert.getIssuerCountry() != null && 
         cert.getIssuerCountry().length() > 0 &&
         !cert.getIssuerCountry().equals("?") ) {
      if ( foundCity || foundState ) {
        locality.append( ", " );
      }

      foundCountry = true;
      locality.append( cert.getIssuerCountry() );
    }

    if ( foundCity || foundState || foundCountry ) {
      issuePanelItems++;
      issuePanel.add( addItemToPanel(locality.toString(), true) );
    }

    if ( certPanelItems < issuePanelItems ) {
      for( int i = certPanelItems; i < issuePanelItems; i++ ) {
        certPanel.add( addItemToPanel("", true) );
      }
    }
    else if ( issuePanelItems < certPanelItems ) {
      for( int i = issuePanelItems; i < certPanelItems; i++ ) {
        issuePanel.add( addItemToPanel("", true) );
      }
    }

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout( new BoxLayout(mainPanel, BoxLayout.X_AXIS) );

    mainPanel.add( certPanel );
    mainPanel.add( issuePanel );

    LString dateValid = 
      new LString("SSLCertificate.date_valid.label",
                  "This certificate is valid from [^0] to [^1]");
    DateFormat df = DateFormat.getDateInstance();
    dateValid.replace( 0, df.format(cert.getStartDate()) );
    dateValid.replace( 1, df.format(cert.getEndDate()) );

    LString bitStrength = new LString( "SSLCertificate.key_length.label",
                                       "Key Size: [^0] bits" );
    bitStrength.replace( 0, (new Integer(cert.getBitStrength())).toString() );

    Object[] dialogItems = { 
      mainPanel,
      addItemToPanel( bitStrength.getString() ),
      addItemToPanel( LString.getString("SSLCertificate.fingerprint.label", 
                                        "Certificate Fingerprint:") ),
      addItemToPanel( cert.getFingerprint(), true ),
      addItemToPanel( LString.getString("SSLCertificate.serial_number.label", 
                                        "Serial Number:") + " " +
                      cert.getSerialNumber() ), 
      addItemToPanel( dateValid.getString() ),
      addItemToPanel( "                                                   " +
                      "                                                   " +
                      "                         ", true ),
    };    

    return dialogItems;
  }

  private JPanel addItemToPanel( String item ) {
    return addItemToPanel( item, false );
  }

  private JPanel addItemToPanel( String item, boolean indent ) {
    JPanel panel = new JPanel( new SpringLayout() ) {
    	protected static final long serialVersionUID = 1L;
        public Dimension getMaximumSize() {
          Dimension pref = getPreferredSize();
          return new Dimension( Integer.MAX_VALUE, pref.height );
        }
    };

    JLabel label = new JLabel( item );

    String fontName = label.getFont().getFontName();

    if ( !Util.supportsFont("Verdana", SecureFTP.locale) )
      fontName = "Default";

    Font infoFont = new Font( fontName,
                              label.getFont().getStyle(),
                              10 );

    label.setFont( infoFont );

    panel.add( label );

    if ( indent ) {
      SpringUtilities.makeCompactGrid( panel,
                                       1, 1, 		      // rows, cols
                                       15, 3,                 // init x, init y
                                       15, 0                  // pad x, pad y
                                     );
    }
    else {
      SpringUtilities.makeCompactGrid( panel,
                                       1, 1, 		     // rows, cols
                                       0, 3,                 // init x, init y
                                       0, 0                  // pad x, pad y
                                     );
    }


    return panel;
  }
}
