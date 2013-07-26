
//*****************************************************************************
//*
//* (c) Copyright 2005. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: EditCertificatesDialog.java 128 2009-12-10 09:18:22Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.gui.*;
import com.glub.util.*;
import com.glub.secureftp.common.*;
import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.text.Keymap;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.security.*;
import java.util.*;

public class EditCertificatesDialog extends JDialog {
  protected static final long serialVersionUID = 1L;	
  private JTabbedPane tab = null;

  private EditCertificatesPanel certPanel;
  private JPanel buttonPanel, clientPanel, 
                 certInfoPanel = null;

  private JButton deleteButton, closeButton = null;

  private JLabel commonNameLabel = null;
  private JLabel orgNameLabel = null;
  private JLabel orgUnitLabel = null;
  private JLabel cityLabel = null;
  private JLabel stateLabel = null;
  private JLabel countryLabel = null;
  private JLabel issOrgNameLabel = null;
  private JLabel issOrgUnitLabel = null;
  private JLabel issCityLabel = null;
  private JLabel issStateLabel = null;
  private JLabel issCountryLabel = null;
  private JLabel expDateLabel = null;

  private JTextField privateKeyTextField = null;
  private JTextField publicCertTextField = null;
  private JTextField caCertTextField = null;
  private JPasswordField certPassField = null;

  private JButton generateCertButton = null;

  private static final String classPath = "EditCertificatesDialog.";
  private ArrayList certList = SecureFTP.getCertificates();

  private CertFilter certFilter = null;
  private CertFilter keyFilter = null;
 
  public EditCertificatesDialog() {
    super(SecureFTP.getBaseFrame(),
          LString.getString(classPath + "dialogTitle", "Manage Certificates"), 
          true);

    getContentPane().setLayout(new BorderLayout());

    certPanel = new EditCertificatesPanel( this );

    buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout());
    getButtons();
    addButtonListeners();

    getContentPane().add(certPanel, BorderLayout.WEST);

    tab = new JTabbedPane();

    JPanel main = new JPanel();
    main.setLayout( new BorderLayout() );
    main.add( getCertificateInfoPanel(), BorderLayout.NORTH );

    JPanel delButtonPanel = new JPanel();
    delButtonPanel.setLayout( new BorderLayout() );
    delButtonPanel.add( getDeleteButton(), BorderLayout.WEST );

    main.add( delButtonPanel, BorderLayout.SOUTH );

    tab.add( LString.getString(classPath + "tab.cert", "Trusted Certificates"),
             main );
    tab.add( LString.getString(classPath + "tab.client_cert", 
                               "Personal Certificate"),
             getClientCertificatePanel() );

    addCertificates();

    getContentPane().add(tab, BorderLayout.CENTER);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);  

    certPanel.setPreferredSize(new Dimension(150, 280));
    certPanel.setSelectedIndex( 0 );

    pack();
    setLocationRelativeTo(SecureFTP.getBaseFrame());
    setResizable(false);
    setVisible(true);
  }

  public void showCertificateInfo( int certIndex ) {
    SSLCertificate cert = null;
   
    if ( certIndex < 0 ) {
      if ( certPanel.getRowCount() > 0 ) {
        certIndex = 0;
        certPanel.setSelectedIndex( certIndex );
      }
    }

    if ( certIndex >= 0 && null != certList && certList.size() > 0 ) {
      deleteButton.setEnabled( true );
      cert = (SSLCertificate)certList.get(certIndex);
    }
    else {
      deleteButton.setEnabled( false );
    }

    if ( null != cert ) {
      commonNameLabel.setText( " " + cert.getCN() );
      orgNameLabel.setText( cert.getOrg() );
      orgUnitLabel.setText( cert.getOU() );
      cityLabel.setText( cert.getLocality() );
      stateLabel.setText( cert.getState() );
      countryLabel.setText( cert.getCountry() );
      issOrgNameLabel.setText( cert.getIssuerOrg() );
      issOrgUnitLabel.setText( cert.getIssuerOU() );
      issCityLabel.setText( cert.getIssuerLocality() );
      issStateLabel.setText( cert.getIssuerState() );
      issCountryLabel.setText( cert.getIssuerCountry() );

      LString expires = 
        new LString(classPath + "expires_cert", "Expires: [^0]");
      expires.replace( 0, cert.getEndDate().toString() );

      expDateLabel.setText( " " + expires.getString() );
    }
    else {
      commonNameLabel.setText( "" );
      orgNameLabel.setText( "" );
      orgUnitLabel.setText( "" );
      cityLabel.setText( "" );
      stateLabel.setText( "" );
      countryLabel.setText( "" );
      issOrgNameLabel.setText( "" );
      issOrgUnitLabel.setText( "" );
      issCityLabel.setText( "" );
      issStateLabel.setText( "" );
      issCountryLabel.setText( "" );
      expDateLabel.setText ( "" );
    }

    pack();
    tab.setSelectedIndex( 0 );
  }

  protected JPanel getCertificateInfoPanel() {
    if ( null == certInfoPanel ) {
      certInfoPanel = new JPanel();
      certInfoPanel.setLayout( new BoxLayout(certInfoPanel, 
                                             BoxLayout.Y_AXIS) );
      commonNameLabel = new JLabel();
      Font curFont = commonNameLabel.getFont();
      commonNameLabel.setFont(new Font(curFont.getFontName(), 
                                       Font.BOLD, curFont.getSize()));

      orgNameLabel = new JLabel();
      orgUnitLabel = new JLabel();
      cityLabel = new JLabel();
      stateLabel = new JLabel();
      countryLabel = new JLabel();

      issOrgNameLabel = new JLabel();
      issOrgUnitLabel = new JLabel();
      issCityLabel = new JLabel();
      issStateLabel = new JLabel();
      issCountryLabel = new JLabel();

      expDateLabel = new JLabel();

      certInfoPanel.add(Box.createVerticalStrut(10));

      certInfoPanel.add(commonNameLabel);
      certInfoPanel.add(Box.createVerticalStrut(5));

      JPanel signee = new JPanel();
      signee.setBorder(
        BorderFactory.createTitledBorder(
          LString.getString("SSLCertificate.issued_to.label", 
                            "Certificate:")) );
      signee.setLayout( new BoxLayout(signee, BoxLayout.Y_AXIS) );

      signee.add(orgNameLabel);
      signee.add(orgUnitLabel);
/*
      signee.add(cityLabel);
      signee.add(stateLabel);
      signee.add(countryLabel);
*/
      signee.add(new JLabel("                                                                               "));

      certInfoPanel.add(signee);
      certInfoPanel.add(Box.createVerticalStrut(5));

      JPanel signer = new JPanel();
      signer.setBorder(
        BorderFactory.createTitledBorder(
          LString.getString("SSLCertificate.issued_from.label", 
                            "Issued From:")) );
      signer.setLayout( new BoxLayout(signer, BoxLayout.Y_AXIS) );

      signer.add(issOrgNameLabel);
      signer.add(issOrgUnitLabel);
/*
      signer.add(issCityLabel);
      signer.add(issStateLabel);
      signer.add(issCountryLabel);
*/
      signer.add(new JLabel("                                                                               "));

      certInfoPanel.add(signer);

      certInfoPanel.add(Box.createVerticalStrut(5));
      certInfoPanel.add( expDateLabel );

      certInfoPanel.add(Box.createVerticalStrut(5));
    }

    return certInfoPanel;
  }

  public JPanel getClientCertificatePanel() {
    if ( null == clientPanel ) {
      JPanel clientCertPanel = new JPanel( new SpringLayout() ) {
    	protected static final long serialVersionUID = 1L;
        public Dimension getMaximumSize() {
          Dimension pref = getPreferredSize();
          return new Dimension( Integer.MAX_VALUE, pref.height );
        }
      };

    String[] labelStrings = {
      LString.getString( classPath + "cert.private", "Private Key:" ),
      LString.getString( classPath + "cert.public", "Public Certificate:" ),
      LString.getString( classPath + "cert.ca", "CA Certificate:" ),
      LString.getString( classPath + "cert.pass", "Passphrase:" ),
    };

    JComponent[] fields = new JComponent[ labelStrings.length ];

    int fieldNum = 0;

    fields[ fieldNum++ ] = getPrivateKeyTextField();
    fields[ fieldNum++ ] = getPublicCertTextField();
    fields[ fieldNum++ ] = getCACertTextField();
    fields[ fieldNum++ ] = getCertPassField();

    JButton[] buttons = new JButton[ labelStrings.length ];

    int buttonNum = 0;

    buttons[ buttonNum++ ] = getBrowseButton( (JTextField)fields[0], false );
    buttons[ buttonNum++ ] = getBrowseButton( (JTextField)fields[1], true );
    buttons[ buttonNum++ ] = getBrowseButton( (JTextField)fields[2], true );
    buttons[ buttonNum++ ] = null;

    clientCertPanel.setBorder(
      BorderFactory.createTitledBorder(
        LString.getString(classPath + "client.border", 
                          "Your Certificate:")) );

    buildFields( clientCertPanel , labelStrings, fields, buttons );

    JPanel buttonsPanel = new JPanel();
    buttonsPanel.setLayout( new BorderLayout() );
    buttonsPanel.add( getGenerateCertButton(), BorderLayout.WEST );

    clientPanel = new JPanel();
    clientPanel.setLayout( new BorderLayout() );
    clientPanel.add( clientCertPanel, BorderLayout.NORTH );
    clientPanel.add( buttonsPanel, BorderLayout.SOUTH );
    }

    return clientPanel;
  }

  protected JTextField getPrivateKeyTextField() {
    if ( null == privateKeyTextField ) {
      privateKeyTextField = new JTextField( 15 );
      File f = Client.getClientPrivateKey();
      if ( null != f )
        privateKeyTextField.setText( f.getAbsolutePath() );
    }

    return privateKeyTextField;
  }

  protected JTextField getPublicCertTextField() {
    if ( null == publicCertTextField ) {
      publicCertTextField = new JTextField( 15 );
      File f = Client.getClientPublicCert();
      if ( null != f )
        publicCertTextField.setText( f.getAbsolutePath() );
    }

    return publicCertTextField;
  }

  protected JTextField getCACertTextField() {
    if ( null == caCertTextField ) {
      caCertTextField = new JTextField( 15 );
      File f = Client.getClientCACert();
      if ( null != f )
        caCertTextField.setText( f.getAbsolutePath() );
    }

    return caCertTextField;
  }

  protected JPasswordField getCertPassField() {
    if ( null == certPassField ) {
      certPassField = new JPasswordField( 15 );
      String s = Client.getClientCertPassword();
      if ( null != s )
        certPassField.setText( s );
    }

    return certPassField;
  }

  protected JButton getBrowseButton( final JTextField textField, 
                                     final boolean cert ) {
    ImageIcon icon =
      new ImageIcon( getClass().getResource("images/browse.png") );
    JButton browseButton = new JButton( icon );
    browseButton.setToolTipText( 
      LString.getString("ConnectionDialog.tooltip.browse", "Browse") );
    browseButton.setPreferredSize(
      new Dimension(icon.getIconWidth() + 20, icon.getIconHeight()) );
    browseButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        File localDir = Client.getLocalView().getCurrentDirectory();

        if ( textField.getText().length() > 0 ) {
          File setFile = new File( textField.getText() );
          if ( setFile.exists() ) {
            if ( setFile.isDirectory() )
              localDir = setFile;
            else
              localDir = setFile.getParentFile();
          }
        }

        JFileChooser fc = new JFileChooser( localDir );
        fc.setFileSelectionMode( JFileChooser.FILES_ONLY );

        if ( cert ) {
          fc.setFileFilter( getCertFilter() );
        }
        else {
          fc.setFileFilter( getKeyFilter() );
        }

        int result =
          fc.showDialog( textField,
                         LString.getString("Common.button.select_file",
                                           "Select File") );
        if ( JFileChooser.APPROVE_OPTION == result ) {
          String file = fc.getSelectedFile().getAbsolutePath();
          textField.setText( file );
          textField.setCaretPosition(0);
        }
      }
    } );

    return browseButton;
  }

  protected JButton getGenerateCertButton() {
    if ( null == generateCertButton ) {
      generateCertButton = 
        new JButton( LString.getString(classPath + "button.generate",
                                       "Generate Certificate...") );
      final Dialog d = this;
      generateCertButton.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          HashMap map = new HashMap();

          new GenerateCertificateDialog( d, map );

          File privateKey = (File)map.get("private");
          File publicCert = (File)map.get("public");

          if ( null != privateKey && null != publicCert ) {
            getPrivateKeyTextField().setText( privateKey.getAbsolutePath() );
            getPublicCertTextField().setText( publicCert.getAbsolutePath() );
          }
        }
      } );
    }

    return generateCertButton;
  }

  private CertFilter getCertFilter() {
    if (null == certFilter) {
      String[] str = { "pem", "der", "pfx", "p12" };
      certFilter =
        new CertFilter(new LString(classPath + "filter.cert", 
                       "Certificate Files (*.pem, *.der, *.pfx, *.p12)"), str);
    }

    return certFilter;
  }

  private CertFilter getKeyFilter() {
    if (null == keyFilter) {
      String[] str = { "pk8", "pfx", "p12" };
      keyFilter =
        new CertFilter( new LString(classPath + "filter.key", 
                            "Private Keys (*.pk8, *.pfx, *.p12)"), str );
    }
    
    return keyFilter;
  }

  protected void buildFields( JPanel panel, String[] labelStrings,
                              JComponent[] fields, JButton[] buttons ) {
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

      if ( null != buttons[i] ) {
        panel.add( buttons[i] );
      }
      else {
        panel.add( new JLabel("") );
      }
    }

    SpringUtilities.makeCompactGrid( panel,
                                     4, 3,        // rows, cols
                                     10, 10,      // init x, init y
                                     10, 30       // pad x, pad y
                                    );
  }

  protected void unSetEnterEvent(JTextField obj) {
    KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    Keymap map = obj.getKeymap();
    map.removeKeyStrokeBinding(enter);
  }
  

  protected void addCertificates() {
    for ( int x = 0; x < certList.size(); x++ ) {
      SSLCertificate c = (SSLCertificate)certList.get(x);
      certPanel.buildRow(c);
    }
  }

  protected JButton getDeleteButton() {
    if ( deleteButton == null ) {
      deleteButton = 
        new JButton( LString.getString("Common.button.remove", "Remove") );

      deleteButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          certList.remove( certPanel.getSelectedIndex() );
	  certPanel.removeSelectedRow();
        }
      });
    }

    return deleteButton;
  }

  protected void getButtons() {
    if ( closeButton == null )
      closeButton = new JButton( LString.getString("Common.button.close", 
                                                   "Close") );

    SwingUtilities.getRootPane( this ).setDefaultButton( closeButton );

    buttonPanel.add(closeButton);
  }

  public boolean checkFields() {
    boolean result = true;

    String pk = getPrivateKeyTextField().getText().trim();
    String pc = getPublicCertTextField().getText().trim();
    String ca = getCACertTextField().getText().trim();
    String pass = new String(getCertPassField().getPassword());

    if ( pk.length() == 0 && pc.length() == 0 && ca.length() == 0 ) {
      Client.setClientPrivateKey( null );
      Client.setClientPublicCert( null );
      Client.setClientCACert( null );
      Client.setClientCertPassword( "" );
    }
    else if ( pk.length() > 0 && pc.length() > 0 && ca.length() > 0 ) {
      File pkf = new File(pk);
      File pcf = new File(pc);
      File caf = new File(ca);

      if ( pkf.exists() && pcf.exists() && caf.exists() ) {
        Client.setClientPrivateKey( pkf );
        Client.setClientPublicCert( pcf );
        Client.setClientCACert( caf );
        Client.setClientCertPassword( pass );
      }
      else
        result = false;
    }
    else if ( pk.length() > 0 && pc.length() > 0 && ca.length() == 0 ) {
      File pkf = new File(pk);
      File pcf = new File(pc);

      if ( pkf.exists() && pcf.exists() ) {
        Client.setClientPrivateKey( pkf );
        Client.setClientPublicCert( pcf );
        Client.setClientCACert( null );
        Client.setClientCertPassword( pass );
      }
      else
        result = false;
    }
    else {
      result = false;
    }

    if ( !result ) {
      getToolkit().beep();
      LString msg = 
        new LString(classPath + "client_cert.error",
                   "There were problems found with your personal certificate.");
      ErrorDialog.showDialog( msg );
      tab.setSelectedIndex( 1 );
    }

    return result;
  }

  protected void addButtonListeners() {
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if ( checkFields() ) {
	  dispose();
        }
      }
    });


    addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if ( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
          dispose();
        }
      }
    });
  }
}

class EditCertificatesPanel extends JPanel {
  protected static final long serialVersionUID = 1L;
  private KeyStore keyStore = null;

  private JTable tableView;
  private JScrollPane scrollPane;
  private EditCertificatesDialog dialog;
  private static final String classPath = "EditCertificatesDialog.";
  private FTPSession dummySession = new FTPSession();

  private CertificateTableModel model;

  protected boolean valid = false;

  public EditCertificatesPanel( EditCertificatesDialog dialog ) {
    super();
    this.dialog = dialog;
    setLayout(new BorderLayout());
    scrollPane = createTable();
    scrollPane.setBackground(Color.white);

    try {
      keyStore = KeyUtil.getKeyStore( dummySession.getKeyStoreFile(), 
                                      "sEcUrEfTp".toCharArray() );
    }
    catch ( Exception e ) {}

    add(scrollPane, BorderLayout.CENTER);
    add(Box.createVerticalStrut(5), BorderLayout.NORTH);
    add(Box.createHorizontalStrut(5), BorderLayout.WEST);
    add(Box.createHorizontalStrut(10), BorderLayout.EAST);

    MouseAdapter listMouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) { 
        getECD().showCertificateInfo( tableView.getSelectedRow() );
      }
    };

    setEnterBehavior();

    tableView.addMouseListener(listMouseListener);
    valid = true;
  }

  protected EditCertificatesDialog getECD() {
    return dialog;
  }

  public JScrollPane createTable() {
    JScrollPane pane = new JScrollPane();

    model = new CertificateTableModel();

    tableView = new JTable(model) {
      protected static final long serialVersionUID = 1L;
      public void valueChanged( ListSelectionEvent e ) {
        super.valueChanged(e);
	if ( valid && null != getECD() ) {
          int selectedRow = getSelectedRow();
          if ( selectedRow < 0 ) {
            selectedRow = 0;
          }
          getECD().showCertificateInfo( selectedRow );
	}
      }
    };

    tableView.setRowHeight(20);
    tableView.setRowSelectionAllowed(true);
    tableView.setColumnSelectionAllowed(false);
    tableView.getTableHeader().setReorderingAllowed(false);
    tableView.setIntercellSpacing(new Dimension(0, 0));
    tableView.setShowHorizontalLines(true);
    tableView.setShowVerticalLines(false);
    tableView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    model.addColumn( LString.getString(classPath + "column.certificate", 
                                       "Certificate") );

    TableColumnModel tcm = tableView.getColumnModel();
    TableColumn tc = tcm.getColumn( 0 );
    TableCellRenderer tcr = new CertificateRenderer();
    tc.setCellRenderer( tcr );

    pane.getViewport().setBackground(Color.white);
    pane.getViewport().add(tableView);
 
    return ( pane );
  }

  public void setSelectedIndex( int index ) {
    tableView.changeSelection( index, index, false, false );
  }

  public int getSelectedIndex() {
    return tableView.getSelectedRow();
  }

  public void removeSelectedRow() {
    if ( tableView.getSelectedRowCount() <= 0 ) return;

    int tableSelectedRow = tableView.getSelectedRow();

    /* remove the cert from the keystore */
    if ( null != keyStore ) {
      try {
        SSLCertificate c = 
          (SSLCertificate)tableView.getValueAt(tableSelectedRow, 0);

        String alias = c.getX509Certificate().getSubjectDN().toString();

        keyStore.deleteEntry( alias );

        FileOutputStream fos = 
         new FileOutputStream(dummySession.getKeyStoreFile());
        keyStore.store( fos, "sEcUrEfTp".toCharArray() );
      }
      catch ( Exception e ) { e.printStackTrace(); }
    }
    
    /* remove row from table */
    model.removeRow(tableSelectedRow);
  }

  public void buildRow(SSLCertificate cert) { 
    Vector row = new Vector();
    row.add(cert);
    model.addRow(row);
    setColumnWidths();
  }

  public void removeAllRows() {
    int totalRows = model.getRowCount() ;
    for ( int i = totalRows-1; i >= 0; i-- ) {
      model.removeRow(i);
    }
  }

  private void setColumnWidths() {

    TableColumn bCol = 
      tableView.getColumn( LString.getString(classPath + "column.certificate", 
                                       "Certificate") );

    bCol.setMinWidth(60);
    bCol.setResizable(false);

    tableView.sizeColumnsToFit(0);

  }


  protected int getRowCount() {
    return tableView.getRowCount();
  }


  protected void clearSelections() {
    tableView.removeRowSelectionInterval(0, tableView.getRowCount() - 1);
  }

  public void setEnterBehavior() {
    tableView.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                          KeyStroke.getKeyStroke("ENTER"),"stop-and-next-cell");

    tableView.getActionMap().put("stop-and-next-cell", new AbstractAction() {
      protected static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
        if ( dialog.checkFields() ) {
	  dialog.dispose();
        }
      }
    } );
  }
}

class CertificateTableModel extends DefaultTableModel {
  protected static final long serialVersionUID = 1L;
  public CertificateTableModel() {
    super();
  }

  public boolean isCellEditable(int row, int col) {
    return false;
  }
}

class CertificateRenderer extends DefaultTableCellRenderer {
  protected static final long serialVersionUID = 1L;
  protected void setValue( Object value ) {
    SSLCertificate cert = (SSLCertificate)value;
    if ( null != cert )
      setText( cert.getCN() );
  }
}

class CertFilter extends javax.swing.filechooser.FileFilter {

  private String filter = null;
  private String[] filterOn = null;

  public CertFilter(LString filterLabel, String[] filterOn) {
    super();
    filter = filterLabel.getString();
    this.filterOn = filterOn;
  }

  public boolean accept(File f) {
    boolean accept = f.isDirectory();

    if (! accept) {
      String suffix = getSuffix(f);

      if (suffix != null) {
        for (int i=0; i < filterOn.length; i++) {
          if (! accept)
            accept = suffix.equals(filterOn[i]);
        }   
      } 
    }

    return accept;
  }

  public String getDescription() {
    return filter;
  }

  public String getSuffix( File f ) {
    String s = f.getPath(), suffix = null;
    int i = s.lastIndexOf('.');

    if ( i > 0 && i < s.length() - 1 )
      suffix = s.substring(i+1).toLowerCase();

    return suffix;
  }
}




