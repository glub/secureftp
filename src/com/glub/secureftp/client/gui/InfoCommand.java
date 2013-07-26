
//*****************************************************************************
//*
//* (c) Copyright 2005. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: InfoCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;
import com.glub.gui.*;
import com.glub.util.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

public class InfoCommand extends NetworkCommand {
  public InfoCommand() {
    super("info", CommandID.INFO_COMMAND_ID, 1, 1,  
          "file", "get file info");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();
    FTPSession session = FTPSessionManager.getInstance().getCurrentSession();

    RemoteFile file = (RemoteFile)getArgs().get(0);

    String origFileName = file.getFileName();
    String origPerms = file.getPermissions();

    short r = InfoDialog.showDialog( file );

    if ( InfoDialog.OK == r ) {
      String newFileName = InfoDialog.fileNameField.getText().trim();
      String newPerms = InfoDialog.perms.toString();

      boolean changed = false;

      if ( !origPerms.equals(newPerms) ) {
        result = chmod( origFileName, newPerms );
        changed = true;
      }

      if ( !origFileName.equals(newFileName) ) {
        result = rename( origFileName, newFileName );
        changed = true;
      }

      if ( changed )
        DataTransferManager.getInstance().list( session );
    }
  
    return result;
  }

  private SecureFTPError chmod( String fileName, String newPerms ) {
    SecureFTPError result = new SecureFTPError();
    result.setCode( SecureFTPError.OK );
    FTPSession session = FTPSessionManager.getInstance().getCurrentSession();

    int user = getChmodOctal( newPerms.substring(1, 4) );
    int group = getChmodOctal( newPerms.substring(4, 7) );
    int other = getChmodOctal( newPerms.substring(7, 10) );

    String newMode = "" + user + group + other;

    try {
      session.getFTPBean().raw( "SITE CHMOD " + newMode + " " + fileName );
    }
    catch ( FTPConnectionLostException fcle ) {
      SecureFTP.getCommandDispatcher().fireCommand(this, new CloseCommand());
      ErrorDialog.showDialog( new LString("Common.connection_lost",
                                          "Connection lost.") );
      result.setCode( SecureFTPError.NOT_CONNECTED );
    }
    catch ( FTPException fe ) {
      LString msg = new LString("Common.unknown.error",
                                "An error has occurred: [^0]");
      msg.replace( 0, fe.getMessage() );
      ErrorDialog.showDialog( msg );

      result.setCode( SecureFTPError.UNKNOWN );
    }

    return result;
  }

  private int getChmodOctal( String rwx ) {
    int result = -1;

    StringBuffer buf = new StringBuffer( rwx );
    char r = buf.charAt(0);
    char w = buf.charAt(1);
    char x = buf.charAt(2);

    if ( r == '?' || w == '?' || x == '?' ) {
      result = -1;
    }

    // no perms
    else if ( r == '-' && w == '-' && x == '-' ) {
      result = 0;
    }

    // execute only
    else if ( r == '-' && w == '-' && x == 'x' ) {
      result = 1;
    }
    
    // write only
    else if ( r == '-' && w == 'w' && x == '-' ) {
      result = 2;
    }
    
    // write & execute only
    else if ( r == '-' && w == 'w' && x == 'x' ) {
      result = 3;
    }
 
    // read only
    else if ( r == 'r' && w == '-' && x == '-' ) {
      result = 4;
    }

    // read & execute only
    else if ( r == 'r' && w == '-' && x == 'x' ) {
      result = 5;
    }

    // read & write only
    else if ( r == 'r' && w == 'w' && x == '-' ) {
      result = 6;
    }

    // read & write & execute
    else if ( r == 'r' && w == 'w' && x == 'x' ) {
      result = 7;
    }

    return result;
  }

  private SecureFTPError rename( String oldName, String newName ) {
    SecureFTPError result = new SecureFTPError();
    result.setCode( SecureFTPError.OK );
    FTPSession session = FTPSessionManager.getInstance().getCurrentSession();

    try {
      session.getFTPBean().rename( oldName, newName );
    }
    catch ( FTPConnectionLostException fcle ) {
      SecureFTP.getCommandDispatcher().fireCommand(this, new CloseCommand());
      ErrorDialog.showDialog( new LString("Common.connection_lost",
                                          "Connection lost.") );
      result.setCode( SecureFTPError.NOT_CONNECTED );
    }
    catch ( FTPException fe ) {
      LString msg = new LString("Common.unknown.error",
                                "An error has occurred: [^0]");
      msg.replace( 0, fe.getMessage() );
      ErrorDialog.showDialog( msg );

      result.setCode( SecureFTPError.UNKNOWN );
    }

    return result;
  }
}

class InfoDialog extends JDialog {
  protected static final long serialVersionUID = 1L;
  public static final short CANCEL = 0;
  public static final short OK = 1;

  protected static short result = CANCEL;

  protected static RemoteFile remoteFile = null;

  protected JPanel buttonPanel = null;
  protected static JButton applyButton = null;

  protected static StringBuffer perms = null;

  protected static JComboBox userPermsCB = null;
  protected static JComboBox groupPermsCB = null;
  protected static JComboBox otherPermsCB = null;

  protected static JTextField fileNameField = null;

  private InfoDialog() {
    super( SecureFTP.getBaseFrame(), 
           LString.getString("InfoDialog.title", "File Info"), 
           true );
    buildDialog();
    pack();
    setLocationRelativeTo( SecureFTP.getBaseFrame() );
    setResizable( false );
    setVisible( true );
  }

  public static short showDialog( RemoteFile file ) {
    userPermsCB = groupPermsCB = otherPermsCB = null;
    remoteFile = file;
    perms = new StringBuffer( file.getPermissions() );
    getFileNameField().setText(file.getFileName());
    new InfoDialog();
    return result;
  }

  protected void buildDialog() {
    getContentPane().setLayout( new BorderLayout() );
    getContentPane().add( getMainPanel(), BorderLayout.NORTH );
    getContentPane().add( getButtonPanel(), BorderLayout.SOUTH );
  }

  protected boolean checkFields() {
    boolean result = true;

    if ( getFileNameField().getText().trim().length() == 0 ) {
      result = false;
    }

    return result;
  }

  protected JPanel getButtonPanel() {
    if ( null == buttonPanel ) {
      buttonPanel = new JPanel( new FlowLayout() );
   
      applyButton = 
        new JButton( LString.getString("Common.button.change", "Change") );

      applyButton.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          if ( checkFields() ) {
            result = OK;
            dispose();
          }
          else {
            getToolkit().beep();
          }
        }
      } );

      applyButton.setEnabled( false );

      buttonPanel.add( applyButton );
    
      JButton button =
        new JButton( LString.getString("Common.button.close", "Close") );
    
      button.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          result = CANCEL;
          dispose();
        }
      } );

      SwingUtilities.getRootPane( this ).setDefaultButton( button );

      buttonPanel.add(button);
    }

    return buttonPanel;
  }

  protected JPanel getMainPanel() {
    JPanel panel = new JPanel();

    panel.setLayout( new BoxLayout(panel, BoxLayout.Y_AXIS) );

    panel.add( Box.createVerticalStrut(5) );
    panel.add( getNamePanel() );
    panel.add( getPermsPanel() );

    return panel;
  }

  protected JPanel getNamePanel() {
    JPanel panel = new JPanel( new SpringLayout() ) {
      protected static final long serialVersionUID = 1L;
      public Dimension getMaximumSize() {
        Dimension pref = getPreferredSize();
        return new Dimension( Integer.MAX_VALUE, pref.height );
      }
    };

    String[] labelStrings = {
      LString.getString( "InfoDialog.filename", "Name:" ),
      "",
      LString.getString( "InfoDialog.date", "Date:" ),
      "",
      LString.getString( "InfoDialog.size", "Size:" ),
    };

    JComponent[] fields = new JComponent[ labelStrings.length ];

    int fieldNum = 0;

    fields[ fieldNum++ ] = getFileNameField();
    fields[ fieldNum++ ] = new JLabel("");
    fields[ fieldNum++ ] = getDateLabel();
    fields[ fieldNum++ ] = new JLabel("");
    fields[ fieldNum++ ] = getSizeLabel();

    buildFields( panel, labelStrings, fields );

    return panel;
  }

  protected JPanel getPermsPanel() {
    JPanel panel = new JPanel( new SpringLayout() ) {
      protected static final long serialVersionUID = 1L;
      public Dimension getMaximumSize() {
        Dimension pref = getPreferredSize();
        return new Dimension( Integer.MAX_VALUE, pref.height );
      }
    };

    panel.setBorder( 
          BorderFactory.createTitledBorder(
            LString.getString("InfoDialog.perms", "Ownership & Permissions:")) );

    String[] labelStrings = {
      LString.getString( "InfoDialog.user", "User:" ),
      LString.getString( "InfoDialog.access", "Access:" ),
      "",
      LString.getString( "InfoDialog.group", "Group:" ),
      LString.getString( "InfoDialog.access", "Access:" ),
      "",
      LString.getString( "InfoDialog.others", "Others:" ),
    };

    JComponent[] fields = new JComponent[ labelStrings.length ];

    int fieldNum = 0;

    fields[ fieldNum++ ] = new JLabel(remoteFile.getUser());
    fields[ fieldNum++ ] = getUserPermsComboBox();
    fields[ fieldNum++ ] = new JLabel("");
    fields[ fieldNum++ ] = new JLabel(remoteFile.getGroup());
    fields[ fieldNum++ ] = getGroupPermsComboBox();
    fields[ fieldNum++ ] = new JLabel("");
    fields[ fieldNum++ ] = getOtherPermsComboBox();

    buildFields( panel, labelStrings, fields );

    return panel;
  }

  protected static JTextField getFileNameField() {
    if ( null == fileNameField ) {
      int size = 25;

      if ( Util.isMacOS() ) 
        size = 15;

      fileNameField = new JTextField( size );
      fileNameField.addKeyListener( new KeyListener() {
        public void keyReleased( KeyEvent ke ) {}
        public void keyPressed( KeyEvent ke ) {}
        public void keyTyped( KeyEvent ke ) {
          applyButton.setEnabled( true );
        } 
      } );
    }

    return fileNameField;
  }

  protected JLabel getDateLabel() {
    Calendar cal = remoteFile.getDate();

    DateFormat timeDateFormat =
      new SimpleDateFormat(LString.getString("Common.timeDateFormat",
                                             "M/d/yyyy h:mm a") );

    DateFormat dateFormat =
      new SimpleDateFormat(LString.getString("Common.dateFormat",
                                             "M/d/yyyy") );

    JLabel dateLabel = new JLabel("-");

    if ( null != cal ) {
      if ( 0 == cal.get(Calendar.HOUR) && 0 == cal.get(Calendar.MINUTE) &&
           0 == cal.get(Calendar.SECOND) ) {
        dateLabel = new JLabel( dateFormat.format(cal.getTime()) );
      }
      else {
        dateLabel = new JLabel( timeDateFormat.format(cal.getTime()) );
      }
    }

    return dateLabel;
  }


  protected JLabel getSizeLabel() {
    JLabel sizeLabel = new JLabel("-");

    Long size = new Long(remoteFile.getFileSize());
    double kb = size.doubleValue() / 1024;
    double mb = kb / 1024;
    double gb = mb / 1024;

    String bytesStr = LString.getString("Common.xferMetric.bytes", "bytes");
    String kbStr = LString.getString("Common.xferMetric.kb", "KB");
    String mbStr = LString.getString("Common.xferMetric.mb", "MB");
    String gbStr = LString.getString("Common.xferMetric.gb", "GB");

    if ( kb >= 1 && mb < 1 ) {
      sizeLabel = new JLabel(sizeFormat(kb, 2) + " " + kbStr);
    }
    else if ( mb >= 1 && gb < 1 ) {
      sizeLabel = new JLabel(sizeFormat(mb, 2) + " " + mbStr);
    }
    else if ( gb >= 1 ) {
      sizeLabel = new JLabel(sizeFormat(gb, 2) + " " + gbStr);
    }
    else {
      sizeLabel = new JLabel(sizeFormat(size.doubleValue(), 2) + " " + bytesStr );
    }

    return sizeLabel;
  }

  public String sizeFormat( double val, int decimals ) {
    DecimalFormat df = new DecimalFormat("#.##");
    df.setGroupingUsed( true );
    df.setMaximumFractionDigits( decimals );
    String result = df.format( val );
    return result;
  }

  protected static JComboBox getUserPermsComboBox() {
    if ( null == userPermsCB ) {
      userPermsCB = getAccessComboBox( 1, 2, 3 );
    }

    return userPermsCB;
  }

  protected static JComboBox getGroupPermsComboBox() {
    if ( null == groupPermsCB ) {
      groupPermsCB = getAccessComboBox( 4, 5, 6 );
    }

    return groupPermsCB;
  }

  protected static JComboBox getOtherPermsComboBox() {
    if ( null == otherPermsCB ) {
      otherPermsCB = getAccessComboBox( 7, 8, 9 );
    }

    return otherPermsCB;
  }

  private static JComboBox getAccessComboBox( final int readOffset, 
                                              final int writeOffset, 
                                              final int executeOffset ) {

    final JComboBox result = new JComboBox();
    result.addItem( LString.getString("InfoDialog.rw", "Read & Write") );
    result.addItem( LString.getString("InfoDialog.ro", "Read only") );

    if ( remoteFile.isDirectory() ) {
      result.addItem( LString.getString("InfoDialog.wo", 
                                        "Write only (Drop Box)") );
    }

    result.addItem( LString.getString("InfoDialog.noAccess", "No Access") );

    char r = perms.charAt(readOffset);
    char w = perms.charAt(writeOffset);
    char x = perms.charAt(executeOffset);

    if ( r == '?' || w == '?' || x == '?' ) {
      result.removeAllItems();
      result.addItem( "-" );
      result.setEnabled( false );
    }

    if ( r == 'r' && w == 'w' ) {
      result.setSelectedIndex(0);
    }
    else if ( r == 'r' ) {
      result.setSelectedIndex(1);
    }
    else if ( remoteFile.isDirectory() && w == 'w' ) {
      result.setSelectedIndex(2);
    }
    else
      result.setSelectedIndex(result.getItemCount() - 1);

    result.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {

        switch ( result.getSelectedIndex() ) {
          case 0: // read and write
            perms.replace( readOffset, writeOffset + 1, "rw" );
            if ( remoteFile.isDirectory() ) {
              perms.replace( executeOffset, executeOffset + 1, "x" );
            }
            break;

          case 1: // read only
            perms.replace( readOffset, writeOffset + 1, "r-" );
            if ( remoteFile.isDirectory() ) {
              perms.replace( executeOffset, executeOffset + 1, "x" );
            }
            break;

          case 2: // write only if dir, otherwise no access
            if ( remoteFile.isDirectory() ) {
              perms.replace( readOffset, executeOffset + 1, "-wx" );
            }
            else {
              perms.replace( readOffset, writeOffset + 1, "--" );
            }
            break;

          case 3: // no access
            perms.replace( readOffset, executeOffset + 1, "---" );
            break;
        }

        applyButton.setEnabled( true );
      }
    } );

    return result;
  }

  protected void setupEscapeForCancel( Component component ) {
    component.addKeyListener( new KeyAdapter() {
       public void keyPressed(KeyEvent e) {
         if ( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
           result = CANCEL;
           dispose();
         }
       }
     } );
  }

  protected void buildFields( JPanel panel, String[] labelStrings,
                                        JComponent[] fields ) {
    JLabel[] labels = new JLabel[ labelStrings.length ];
        
    for ( int i = 0; i < labelStrings.length; i++ ) { 
      setupEscapeForCancel( fields[i] );
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
                                     labelStrings.length, 2, // rows, cols
                                     10, 5,                 // init x, init y
                                     5, 5                    // pad x, pad y
                                   );
  }
}


