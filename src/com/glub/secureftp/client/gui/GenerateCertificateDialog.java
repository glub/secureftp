
//*****************************************************************************
//*
//* (c) Copyright 2005. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: GenerateCertificateDialog.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.gui.*;
import com.glub.util.*;
import com.glub.secureftp.common.*;
import com.glub.secureftp.client.framework.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class GenerateCertificateDialog extends JDialog {
  protected static final long serialVersionUID = 1L;	
  //private JTabbedPane tab = null;

  private JPanel buttonPanel, mainPanel = null;

  private JButton okButton, cancelButton = null;

  private JTextField commonNameTextField = null;
  private JTextField orgTextField = null;
  private JTextField ouTextField = null;
  private JTextField cityTextField = null;
  private JTextField stateTextField = null;
  private JTextField countryTextField = null;

  private File privateKey = null;
  private File publicCert = null;

  private Map genMap = null;

  private static final String classPath = "GenerateCertificateDialog.";

  public GenerateCertificateDialog( Dialog owner, Map genMap ) {
    super(owner,
          LString.getString(classPath + "dialogTitle", "Generate Certificate"), 
          true);

    this.genMap = genMap;

    getContentPane().setLayout(new BorderLayout());

    buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout());
    buttonPanel.add( getOKButton() );
    buttonPanel.add( getCancelButton() );

    getContentPane().add(getMainPanel(), BorderLayout.NORTH);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);  

    pack();
    setLocationRelativeTo(SecureFTP.getBaseFrame());
    setResizable(false);
    setVisible(true);
  }

  protected JPanel getMainPanel() {
    if ( null == mainPanel ) {
      mainPanel = new JPanel( new SpringLayout() ) {
    	protected static final long serialVersionUID = 1L;
        public Dimension getMaximumSize() {
          Dimension pref = getPreferredSize();
          return new Dimension( Integer.MAX_VALUE, pref.height );
        }
      };

      String[] labelStrings = {
        LString.getString( classPath + "cert.cn", "Your Name:" ),
        LString.getString( classPath + "cert.o", "Company Name:" ),
        LString.getString( classPath + "cert.ou", "Company Division:" ),
        LString.getString( classPath + "cert.city", "City:" ),
        LString.getString( classPath + "cert.state", "State:" ),
        LString.getString( classPath + "cert.country", "Country:" ),
      };

      JComponent[] fields = new JComponent[ labelStrings.length ];

      int fieldNum = 0;

      fields[ fieldNum++ ] = getCNTextField();
      fields[ fieldNum++ ] = getOrgTextField();
      fields[ fieldNum++ ] = getOUTextField();
      fields[ fieldNum++ ] = getCityTextField();
      fields[ fieldNum++ ] = getStateTextField();
      fields[ fieldNum++ ] = getCountryTextField();

      buildFields( mainPanel , labelStrings, fields );
    }

    return mainPanel;
  }

  protected JTextField getCNTextField() {
    if ( null == commonNameTextField ) {
      commonNameTextField = new JTextField( 15 );
    }

    return commonNameTextField;
  }

  protected JTextField getOrgTextField() {
    if ( null == orgTextField ) {
      orgTextField = new JTextField( 15 );
    }

    return orgTextField;
  }

  protected JTextField getOUTextField() {
    if ( null == ouTextField ) {
      ouTextField = new JTextField( 15 );
    }

    return ouTextField;
  }

  protected JTextField getCityTextField() {
    if ( null == cityTextField ) {
      cityTextField = new JTextField( 15 );
    }

    return cityTextField;
  }

  protected JTextField getStateTextField() {
    if ( null == stateTextField ) {
      stateTextField = new JTextField( 15 );
    }

    return stateTextField;
  }

  protected JTextField getCountryTextField() {
    if ( null == countryTextField ) {
      countryTextField = new JTextField( 15 );
    }

    return countryTextField;
  }

  protected void buildFields( JPanel panel, String[] labelStrings,
                              JComponent[] fields ) {
    JLabel[] labels = new JLabel[ labelStrings.length ];

    for ( int i = 0; i < labelStrings.length; i++ ) {
      labels[ i ] = new JLabel( labelStrings[i], JLabel.TRAILING );
      labels[ i ].setLabelFor( fields[i] );

      fields[ i ].addFocusListener( new FocusListener() {
        public void focusGained( FocusEvent e ) {
          Component c = e.getComponent();

          if ( c instanceof JTextField ) {
            ((JTextField)c).selectAll();
          }
        }

        public void focusLost( FocusEvent e ) {}
      } );

      panel.add( labels[i] );
      panel.add( fields[i] );
    }

    SpringUtilities.makeCompactGrid( panel,
                                     6, 2,        // rows, cols
                                     10, 10,      // init x, init y
                                     10, 10       // pad x, pad y
                                    );
  }

  protected JButton getOKButton() {
    if ( okButton == null ) {
      okButton = new JButton( LString.getString(classPath + "button.generate", 
                                                "Generate") );
      okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if ( checkFields() ) {
	    dispose();
          }
        }
      });

      SwingUtilities.getRootPane( this ).setDefaultButton( okButton );
    }

    return okButton;
  }

  protected JButton getCancelButton() {
    if ( cancelButton == null ) {
      cancelButton = new JButton( LString.getString("Common.button.cancel", 
                                                    "Cancel") );
      cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
	  dispose();
        }
      });
    }

    return cancelButton;
  }

  public boolean checkFields() {
    boolean result = true;

    String cn = getCNTextField().getText().trim();
    String o  = getOrgTextField().getText().trim();
    String ou = getOUTextField().getText().trim();
    String city = getCityTextField().getText().trim();
    String state = getStateTextField().getText().trim();
    String country = getCountryTextField().getText().trim();

    LString error = null;

    if ( cn.length() == 0 ) {
      error = new LString(classPath + "cn.error", "Enter your name.");
      result = false;
    }
    else if ( o.length() == 0 ) {
      error = new LString(classPath + "o.error", "Enter your company's name.");
      result = false;
    }
    else if ( ou.length() == 0 ) {
      error = new LString(classPath + "ou.error", "Enter your division.");
      result = false;
    }
    else if ( city.length() == 0 ) {
      error = new LString(classPath + "city.error", "Enter your city.");
      result = false;
    }
    else if ( state.length() == 0 ) {
      error = new LString(classPath + "state.error", "Enter your state.");
      result = false;
    }
    else if ( country.length() == 0 ) {
      error = new LString(classPath + "country.error", "Enter your country.");
      result = false;
    }

    if ( !result ) {
      getToolkit().beep();
      if ( null != error )
        ErrorDialog.showDialog( error );
    }
    else {
      CertInfo ci = new CertInfo( cn, o, ou, city, state, country );
      FTPSession dummySession = new FTPSession();
      File baseDir = dummySession.getKeyStoreFile().getParentFile();
      if ( null != baseDir ) {
        privateKey = new File( baseDir, "private.pk8" );
        publicCert = new File( baseDir, "certificate.der" );

        SecureFTP.getBaseFrame().setCursor( new Cursor(Cursor.WAIT_CURSOR) );

        int days = 1095;
        int daysOverride = 
          GTOverride.getInt("glub.certificate.generate.days", days);
        if ( daysOverride > 0 ) {
          days = daysOverride;
        }

        if (KeyUtil.writeCertAndKey(ci, days, publicCert, privateKey)) {
          genMap.put("private", privateKey);
          genMap.put("public", publicCert);
        }

        SecureFTP.getBaseFrame().setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );
      }
    }

    return result;
  }
}


