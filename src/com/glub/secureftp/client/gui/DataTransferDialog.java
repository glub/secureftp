
//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: DataTransferDialog.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.secureftp.bean.*;
import com.glub.gui.*;
import com.glub.util.*;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

public class DataTransferDialog extends JDialog implements Progress {
  protected static final long serialVersionUID = 1L;
  private FTPSession session = null;
  private FTPAbortableTransfer abort = null;
  private short transferDirection;
  private JProgressBar progressBar = null;

  private JLabel fileNameLabel = new JLabel("");
  private JLabel estimatedTimeLabel = new JLabel("");
  private JLabel transferRateLabel = new JLabel("");

  private JPanel progressPanel = null;
  private JPanel indeterminatePanel = null;

  private double percentDone = 0.0;
  private int totalPercent = 100;
  private long currentXfer = 0;
  private long total = 0;
  private boolean showProgress = true;
  private boolean abortAttempted = false;

  public DataTransferDialog( Frame parent, FTPSession session, short xferDir,
                             FTPAbortableTransfer abort ) {
    super( parent, 
           LString.getString("DataTransferDialog.title", "File Transfer"), 
           false );
    this.session = session;
    setAbortableTransfer( abort );
    this.transferDirection = xferDir;
    buildDialog();
    pack();

    if ( null != session ) {
      addWindowListener( new WindowAdapter() {
        public void windowClosing( WindowEvent e ) {
          cancelDataTransfer();
        }
      } );
    }
    else {
      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    setLocationRelativeTo( SecureFTP.getBaseFrame() );
    setResizable( false );
    setVisible( true );
  }

  public void dispose() {
    showProgress = false;
    super.dispose();
  }

  protected void buildDialog() {
    JPanel dialogPanel = new JPanel();
    dialogPanel.setLayout( new SpringLayout() );

    dialogPanel.add( getInfoPanel() );
    dialogPanel.add( getProgressPanel() );
    dialogPanel.add( getStatsPanel() );

    SpringUtilities.makeCompactGrid( dialogPanel,
                                     3, 1,  // rows, cols
                                     5, 5, // init x, init y
                                     5, 5  // pad x, pad y
				   );

    getContentPane().setLayout( new BorderLayout() );
    getContentPane().add( dialogPanel, BorderLayout.CENTER );
    getContentPane().add( getButtonPanel(), BorderLayout.SOUTH );
  }

  protected JPanel getInfoPanel() {
    JPanel panel = new JPanel();
    panel.setLayout( new BoxLayout(panel, BoxLayout.Y_AXIS) );

    JLabel downloadLabel = 
      new JLabel( LString.getString("DataTransferDialog.download", 
                                    "Downloading:") );

    JLabel uploadLabel = 
      new JLabel( LString.getString("DataTransferDialog.upload", 
                                    "Uploading:") );

    if ( DataTransferManager.DOWNLOAD == transferDirection ) {
      panel.add( downloadLabel );
    }
    else {
      panel.add( uploadLabel );
    }

    panel.add( fileNameLabel );

    String maxStr = LString.getString("DataTransferDialog.timeLeft", 
               "Estimated time left: [^0] ([^1] [^2] of [^3] [^4] copied)");
    FontMetrics fm = getFontMetrics( getFont() );
    int size = fm.stringWidth(maxStr) + 50;

    panel.setPreferredSize( new java.awt.Dimension(size, 35) );

    return panel;
  }

  public void setAbortableTransfer( FTPAbortableTransfer abort ) {
    this.abort = abort;
  }

  public void setFileName( String name ) { 
    fileNameLabel.setText( name ); 
    fileNameLabel.setToolTipText( name );
  }

  protected JPanel getStatsPanel() {
    JPanel panel = new JPanel();
    panel.setLayout( new BoxLayout(panel, BoxLayout.Y_AXIS) );

    setTimeRemaining( (long)-1, (long)0, (long)0 );
    panel.add( estimatedTimeLabel );

    setTransferRate( (long)-1 );
    panel.add( transferRateLabel );

    return panel;
  }

  public void setTimeRemaining( long timeLeft, long xferBytes, 
                                long totalBytes ) {
    NumberFormat nf = NumberFormat.getIntegerInstance();
    nf.setGroupingUsed( true );
    nf.setMaximumFractionDigits( 2 );

    LString timeLeftStr = 
      new LString("DataTransferDialog.timeLeft", 
               "Estimated time left: [^0] ([^1] [^2] of [^3] [^4] copied)");

    if ( timeLeft < 0 && getProgressBar().isIndeterminate() ) {
      RateMetric xferRM = getRateMetric( xferBytes );
      LString dataXferStr =
        new LString("DataTransferDialog.bytesXferBase", 
                    "Data Transferred: [^0] [^1] copied");
      dataXferStr.replace( 0, nf.format(xferRM.getRate()) );
      dataXferStr.replace( 1, xferRM.getMetric() );
      estimatedTimeLabel.setText( dataXferStr.getString() );
      return;
    }
    else if ( timeLeft < 0 ) {
      estimatedTimeLabel.setText( 
        LString.getString("DataTransferDialog.timeLeftBase", 
                          "Estimated time left:") );
      return;
    }
   

    Date date = new Date();
    date.setTime( timeLeft * 1000 );
    SimpleDateFormat dateFormat = new SimpleDateFormat( "HH:mm:ss" );
    dateFormat.setTimeZone( TimeZone.getTimeZone("GMT") );
    timeLeftStr.replace( 0, dateFormat.format(date) );

    RateMetric currentRM = getRateMetric( xferBytes );
    timeLeftStr.replace( 1, nf.format(currentRM.getRate()) );
    timeLeftStr.replace( 2, currentRM.getMetric() );

    RateMetric totalRM = getRateMetric( totalBytes );
    timeLeftStr.replace( 3, nf.format(totalRM.getRate()) );
    timeLeftStr.replace( 4, totalRM.getMetric() );

    estimatedTimeLabel.setText( timeLeftStr.getString() );
  }

  public void setTransferRate( long bytesPerSec ) {
    LString xferRateStr = 
      new LString("DataTransferDialog.transferRate", 
                  "Transfer rate: [^0] [^1]/sec");

    if ( bytesPerSec <= 0 ) {
      transferRateLabel.setText( 
        LString.getString("DataTransferDialog.transferRateBase", 
                          "Transfer rate:") );
      return;
    }

    RateMetric rm = getRateMetric( bytesPerSec );

    NumberFormat nf = NumberFormat.getIntegerInstance();
    nf.setGroupingUsed( true );
    nf.setMaximumFractionDigits( 0 );

    xferRateStr.replace( 0, nf.format(rm.getRate()) );
    xferRateStr.replace( 1, rm.getMetric() );

    transferRateLabel.setText( xferRateStr.getString() );
  }

  private RateMetric getRateMetric( long totalBytes ) {
    RateMetric rm = new RateMetric();

    short metric = 0;
    double bytes = totalBytes;
  
    // KB / sec
    if ( bytes / 1024 > 1 ) {
      bytes /= 1024;
      metric = 1;
    }

    // MB / sec
    if ( metric == 1 &&  bytes / 1024 > 1 ) {
      bytes /= 1024;
      metric = 2;
    }

    // GB / sec
    if ( metric == 2 && bytes / 1024 > 1 ) {
      bytes /= 1024;
      metric = 3;
    }
    
    rm.setRate( bytes );

    String sMetric = null;

    switch ( metric ) {
      case 0:
        sMetric = LString.getString("Common.xferMetric.bytes", "bytes");
        break;

      case 1:
        sMetric = LString.getString("Common.xferMetric.kb", "KB");
        break;

      case 2:
        sMetric = LString.getString("Common.xferMetric.mb", "MB");
        break;

      case 3:
        sMetric = LString.getString("Common.xferMetric.gb", "GB");
        break;
    }

    rm.setMetric( sMetric );

    return rm;
  }
    
  protected JPanel getProgressPanel() {
    if ( null == progressPanel ) {
      progressPanel = new JPanel( new BorderLayout() );

      progressBar = new JProgressBar( 0, 100 );
      progressPanel.add( progressBar, BorderLayout.CENTER );
    }

    return progressPanel;
  }

  

  protected JPanel getButtonPanel() {
    JPanel panel = new JPanel();

    JButton cancelButton =
      new JButton( LString.getString("Common.button.cancel", "Cancel") );
    cancelButton.setEnabled( null != session );
    SwingUtilities.getRootPane( this ).setDefaultButton( cancelButton );
    cancelButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        Thread cancelThread = new Thread() {
          public void run() {
            setEnabled( false );
            setVisible( false );
            cancelDataTransfer();
          }
        };
        cancelThread.start();
      }
    } );

    panel.add( cancelButton );

    return panel;
  }

  protected void cancelDataTransfer() {
    abortAttempted = true;
    try {
      if ( null != abort ) {
        try {
          session.getFTPBean().abort( abort );
        }
        catch ( IllegalArgumentException iae ) {
          // the FTPData may not yet be set... 
          // so wait a few ticks and try again
          try {
            Thread.sleep( 100 );
          } catch ( InterruptedException ie ) {}
          session.getFTPBean().abort( abort );
        }
      }
    }
    catch ( FTPException fe ) {}
    finally {
      SecureFTP.getCommandDispatcher().fireCommand(this, new LsCommand());
      SecureFTP.getBaseFrame().setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );
      dispose();
    }
  }

  public boolean abortAttempted() { return abortAttempted; }
  
  public void finishProgress() {
    if ( !progressBar.isIndeterminate() ) {
      progressBar.setValue( 100 );
    }
  }

  public void startProgress() {
    showProgress = true;
    
    if ( !progressBar.isIndeterminate() ) {
      progressBar.setValue( 0 );
    }

    DataTransferProgressThread dtpt = new DataTransferProgressThread( this );
    dtpt.setPriority( Thread.MIN_PRIORITY );
    dtpt.start();
  }

  public void updateProgress( long pos, long total ) {
    this.percentDone = ((pos * 1.0) / total) * 100;
    this.currentXfer = pos;
    this.total = total;
    this.totalPercent = 100;
    String baseTitle = 
      LString.getString("DataTransferDialog.title", "File Transfer");
    if ( total > 0 ) {
      if ( progressBar.isIndeterminate() ) {
        setIndeterminate( false );
      }

      LString percentDoneStr = new LString("DataTransferDialog.percentDone", 
                                           "[^0]% completed");
      NumberFormat nf = NumberFormat.getNumberInstance();
      nf.setMaximumFractionDigits( 0 );
      String sPercent =  nf.format( percentDone );
      
      if (percentDone > 100)
    	  sPercent = "--";
      
      percentDoneStr.replace( 0, sPercent );

      setTitle( baseTitle + ": " + percentDoneStr.getString() );
    }
    else {
      if ( !progressBar.isIndeterminate() ) {
        setIndeterminate( true );
      }
      
    }
  }

  private JPanel getIndeterminateProgress() {
    if ( null == indeterminatePanel ) {
      ImageIcon progress = 
        new ImageIcon(getClass().getResource("images/progress.gif"));
      indeterminatePanel = new JPanel( new BorderLayout() );
      indeterminatePanel.add( new JLabel(progress), BorderLayout.CENTER );
    }

    return indeterminatePanel;
  }

  private void setIndeterminate( boolean state ) {
    getProgressBar().setIndeterminate( state );

    if ( state ) {
      // lets use our image for non mac
      if ( !Util.isMacOS() ) {
        getProgressPanel().remove( getProgressBar() );
        getProgressPanel().add( getIndeterminateProgress(), 
                                BorderLayout.CENTER );
      }
    }
    else {
      // use the native progress bar
      getProgressPanel().remove( getIndeterminateProgress() );
      getProgressPanel().add( getProgressBar(), BorderLayout.CENTER );
    }
  }

  public double getPercentDone() { return percentDone; }
  public int getTotalPercent() { return totalPercent; }
  public long getCurrentXfer() { return currentXfer; }
  public long getTotal() { return total; }
  public JProgressBar getProgressBar() { return progressBar; }
  public boolean showProgress() { return showProgress; }

  class RateMetric {
    private double rate;
    private String metric;

    public RateMetric() {
      this( -1, null );
    }

    public RateMetric( long rate, String metric ) {
      setRate( rate );
      setMetric( metric );
    }

    public double getRate() { return rate; }
    public void setRate( double rate ) { this.rate = rate; }

    public String getMetric() { return metric; }
    public void setMetric( String metric ) { this.metric = metric; }
  }
}

class DataTransferProgressThread extends Thread {
  private DataTransferDialog dialog = null;

  Runnable getMin, getMax, setValue, paintString, dontPaintString, xferMetrics;
  int minValue = 0, maxValue, value = 0;
  int tenthOfSec = 0;
  long bytesPerSec;

  public DataTransferProgressThread( final DataTransferDialog dialog ) {
    this.dialog = dialog;

    setValue = new Runnable() {
      public void run() {
        JProgressBar progressBar = dialog.getProgressBar();

        if ( !progressBar.isIndeterminate() ) {
          progressBar.setValue(value);
        }
      }
    };

    getMin = new Runnable() {
      public void run() {
        JProgressBar progressBar = dialog.getProgressBar();
        
        if ( !progressBar.isIndeterminate() ) {
          minValue = progressBar.getMinimum();
        }
      }
    };

    getMax = new Runnable() {
      public void run() {
        JProgressBar progressBar = dialog.getProgressBar();
        if ( !progressBar.isIndeterminate() ) {
          maxValue = progressBar.getMaximum();
        }
      }
    };

    xferMetrics = new Runnable() {
      public void run() {
        long byteRate = dialog.getCurrentXfer() - bytesPerSec;
	long secondsRemaining = 
          (dialog.getTotal() - dialog.getCurrentXfer()) / byteRate;
        dialog.setTransferRate( byteRate );
        dialog.setTimeRemaining( secondsRemaining,
                                 dialog.getCurrentXfer(), dialog.getTotal() );
      }
    };

/*
    paintString = new Runnable() {
      public void run() {
        JProgressBar progressBar = dialog.getProgressBar();
        if ( !progressBar.isIndeterminate() ) {
          progressBar.setStringPainted(true);
        }
      }
    };

    dontPaintString = new Runnable() {
      public void run() {
        JProgressBar progressBar = dialog.getProgressBar();
        if ( !progressBar.isIndeterminate() ) {
          progressBar.setStringPainted(false);
        }
      }
    };
*/

  }

  public void run() {
    while ( dialog.showProgress() ) {
      try {
        SwingUtilities.invokeAndWait(getMin);
        SwingUtilities.invokeAndWait(getMax);
      }
      catch ( InvocationTargetException ite ) {}
      catch ( InterruptedException ie ) {}

      try {

        sleep( 200 );

        tenthOfSec++;
	if ( tenthOfSec > 9 ) {
          tenthOfSec = 0;
          try {
            SwingUtilities.invokeAndWait(xferMetrics);
          }
          catch ( InvocationTargetException ite ) {}
          catch ( InterruptedException ie ) {}
	  bytesPerSec = dialog.getCurrentXfer();
	}

        double completed = dialog.getPercentDone();
        int total = dialog.getTotalPercent();

        if (total <= 0 || completed > total) {
          value = minValue;
          SwingUtilities.invokeLater(setValue);
          //SwingUtilities.invokeLater(dontPaintString);
        }
        else {
          double percent = (double) completed / total;

          value = (int) (percent * maxValue);
          SwingUtilities.invokeLater(setValue);
          //SwingUtilities.invokeLater(paintString);
        }

      } catch (InterruptedException ie) {}
    }
  }
}

class FileExistsDialog {
  public static int DIRECTION_GET = 0;
  public static int DIRECTION_PUT = 1;

  public static int CANCEL = 0;
  public static int SKIP = 1;
  public static int REPLACE = 2;
  public static int REPLACE_ALL = 3;
  public static int RESUME = 4;
  public static int RESUME_ALL = 5;
  public static int SKIP_ALL = 6;

  private final static LString REMOTE_SMALLER =
    new LString( "DataTransfer.remote_file_smaller",
                 "The remote file is smaller than the local file." );

  private final static LString REMOTE_LARGER =
    new LString( "DataTransfer.remote_file_larger",
                 "The remote file is larger than the local file." );

  private final static LString REMOTE_OLDER =
    new LString( "DataTransfer.remote_file_older",
	         "The remote file is older than the local file." );

  private final static LString REMOTE_NEWER =
    new LString( "DataTransfer.remote_file_newer",
                 "The remote file is newer than the local file." );

  private final static LString REMOTE_SMALLER_OLDER =
    new LString( "DataTransfer.remote_file_smaller_older",
                 "The remote file is smaller and older than the local file." );

  private final static LString REMOTE_SMALLER_NEWER =
    new LString( "DataTransfer.remote_file_smaller_newer",
                 "The remote file is smaller and newer than the local file." );

  private final static LString REMOTE_LARGER_OLDER =
    new LString( "DataTransfer.remote_file_larger_older",
                 "The remote file is larger and older than the local file." );

  private final static LString REMOTE_LARGER_NEWER =
    new LString( "DataTransfer.remote_file_larger_newer",
                 "The remote file is larger and newer than the local file." );

  public static ButtonGroup choicesGroup = new ButtonGroup();
    
  public static int showDialog( int direction, String fileName, 
                                Date remoteModTime, long localModTime,
                                long remoteFileSize, long localFileSize,
                                boolean resumable ) {
    if ( GTOverride.getBoolean("glub.transfer.force.replace_all") ) {
      return FileExistsDialog.REPLACE_ALL;
    }

    LString exists = 
      new LString( "DataTransfer.file_exists", 
                   "The file \"[^0]\" already exists." );
    exists.replace( 0, fileName );

    JPanel infoPanel = new JPanel();
    infoPanel.setLayout( new BoxLayout(infoPanel, BoxLayout.Y_AXIS) );

    infoPanel.add( new GTLabel(exists, 300) );
    infoPanel.add( Box.createVerticalStrut(10) );

    LString moreInfo = null;

    boolean resumeDefault = resumable;

    // test to see if we have mod time of files
    if ( null != remoteModTime && remoteModTime.getTime() != localModTime ) {
        //resumable = false;
      resumeDefault = false;

      if ( remoteModTime.getTime() > localModTime ) {
        if ( remoteFileSize > localFileSize ) {
          // remote newer and larger
          moreInfo = REMOTE_LARGER_NEWER;

          if ( direction == DIRECTION_PUT ) {
            resumable = false;
          }
        }
        else if ( remoteFileSize < localFileSize ) {
          // remote newer and smaller
          moreInfo = REMOTE_SMALLER_NEWER;

          if ( direction == DIRECTION_GET ) {
            resumable = false;
          }
        }
        else {
          // remote newer
          moreInfo = REMOTE_NEWER;
        }
      }
      else {
        if ( remoteFileSize > localFileSize ) {
          // remote older and larger
          moreInfo = REMOTE_LARGER_OLDER;
          resumable = false;
        }
        else if ( remoteFileSize < localFileSize ) {
          // remote older and smaller
          moreInfo = REMOTE_SMALLER_OLDER;
          resumable = false;
        }
        else {
          // remote older
          moreInfo = REMOTE_OLDER;
          resumable = false;
        }
      }
    }
    else if ( remoteFileSize > localFileSize ) {
      // remote larger
      moreInfo = REMOTE_LARGER;

      if ( direction == DIRECTION_PUT ) {
        resumable = false;
      }
    }
    else if ( remoteFileSize < localFileSize ) {
      // remote smaller
      moreInfo = REMOTE_SMALLER;

      if ( direction == DIRECTION_GET ) {
        resumable = false;
      }
    }

    if ( null != moreInfo ) {
      infoPanel.add( new GTLabel(moreInfo, 300) );
      infoPanel.add( Box.createVerticalStrut(10) );
    }

    JPanel choicesPanel = new JPanel();
    choicesPanel.setLayout( new BoxLayout(choicesPanel, BoxLayout.X_AXIS) );

    JRadioButton replaceRB = 
      new JRadioButton(LString.getString("DataTransfer.button.replace", 
                                         "Replace"));

    JRadioButton skipRB = 
      new JRadioButton(LString.getString("DataTransfer.button.skip", 
                                         "Skip"));

    JRadioButton resumeRB = 
      new JRadioButton(LString.getString("DataTransfer.button.resume", 
                                         "Resume"));

    choicesPanel.add( replaceRB );
    choicesPanel.add( skipRB );
    choicesPanel.add( resumeRB );

    choicesGroup.add( replaceRB );
    choicesGroup.add( skipRB );
    choicesGroup.add( resumeRB );

    choicesPanel.setBorder( 
      new TitledBorder(LString.getString("DataTransfer.options.title", 
                                         "Options")) );

    if ( resumeDefault ) {
      resumeRB.setSelected( true );
    }
    else {
      replaceRB.setSelected( true );
    }

    resumeRB.setEnabled( resumable );

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout( new BorderLayout() );

    mainPanel.add( infoPanel, BorderLayout.CENTER );
    mainPanel.add( choicesPanel, BorderLayout.SOUTH );

    LString title = 
      new LString("DataTransfer.file_exists.title", "File Exists");

    String[] buttons = { LString.getString("Common.button.ok", "OK"),
                         LString.getString("Common.button.cancel", "Cancel"), 
                         LString.getString("DataTransfer.button.apply_to_all",
                                           "Apply to All") };


    int r = JOptionPane.showOptionDialog( SecureFTP.getBaseFrame(),
                                          mainPanel,
                                          title.getString(),
                                          JOptionPane.YES_NO_CANCEL_OPTION,
                                          JOptionPane.QUESTION_MESSAGE,
                                          null,
                                          buttons,
                                          buttons[0] ); 

    boolean applyToAll = ( r == JOptionPane.CANCEL_OPTION );

    int result = CANCEL;

    if ( r == JOptionPane.YES_OPTION || applyToAll ) {
      if ( replaceRB.isSelected() ) {
        if ( applyToAll ) {
          result = REPLACE_ALL;
        }
        else {
          result = REPLACE;
        }
      }
      else if ( skipRB.isSelected() ) {
        if ( applyToAll ) {
          result = SKIP_ALL;
        }
        else {
          result = SKIP;
        }
      }
      else if ( resumeRB.isSelected() ) {
        if ( applyToAll ) {
          result = RESUME_ALL;
        }
        else {
          result = RESUME;
        }
      }
    }

    return result;
  }
}

