/*
 * @(#)DirectoryComboBoxEditor.java	1.25 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.glub.secureftp.client.gui;

import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;

import java.lang.reflect.Method;

/**
 * The default editor for editable combo boxes. The editor is implemented as a JTextField.
 *
 * @version 1.25 01/23/03
 * @author Arnaud Weber
 * @author Mark Davidson
 */
public class DirectoryComboBoxEditor implements ComboBoxEditor, FocusListener {
    protected JTextField editor;
    private Object oldValue;
    private JPanel panel;

    public DirectoryComboBoxEditor() {
      editor = new BorderlessTextField("", 9);
      editor.setBorder( new EmptyBorder(2, 2, 2, 2) );
      editor.setBackground( Color.white );

      Icon icon = UIManager.getIcon("FileView.directoryIcon" );

      panel = new JPanel();
      panel.setBackground( Color.white );
      panel.setLayout( new BorderLayout() );
      panel.setOpaque(true);

      JPanel subPanel = new JPanel();
      subPanel.setLayout( new BorderLayout() );
      subPanel.setBackground( Color.white );
      subPanel.setOpaque(true);
      JLabel iconLabel = new JLabel(icon);
      subPanel.add( Box.createHorizontalStrut(3), BorderLayout.WEST );
      subPanel.add( iconLabel, BorderLayout.CENTER );
      subPanel.add( Box.createHorizontalStrut(3), BorderLayout.EAST );

      panel.add( subPanel, BorderLayout.WEST );
      panel.add( editor, BorderLayout.CENTER );
    }

    public Component getEditorComponent() {
      //return editor;
      return panel;
    }

    /** 
     * Sets the item that should be edited. 
     *
     * @param anObject the displayed value of the editor
     */
    public void setItem(Object anObject) {
      if ( anObject != null )  {
        String item = anObject.toString();
        editor.setText(item);
            
        oldValue = anObject;
      } else {
        editor.setText("");
      }
    }

    public Object getItem() {
      Object newValue = editor.getText();
        
      if (oldValue != null && !(oldValue instanceof String))  {
        // The original value is not a string. Should return the value in it's
        // original type.
        if (newValue.equals(oldValue.toString()))  {
          return oldValue;
        } else {
          // Must take the value from the editor and get the value and cast it to the new type.
          Class cls = oldValue.getClass();
          try {
            Method method = cls.getMethod("valueOf", new Class[]{String.class});
            newValue = method.invoke(oldValue, new Object[] { editor.getText()});
          } catch (Exception ex) {
            // Fail silently and return the newValue (a String object)
          }
        }
      }
      return newValue;
    }

    public void selectAll() {
      editor.selectAll();
      editor.requestFocus();
    }

    // This used to do something but now it doesn't.  It couldn't be
    // removed because it would be an API change to do so.
    public void focusGained(FocusEvent e) {}
    
    // This used to do something but now it doesn't.  It couldn't be
    // removed because it would be an API change to do so.
    public void focusLost(FocusEvent e) {}

    public void addActionListener(ActionListener l) {
      editor.addActionListener(l);
    }

    public void removeActionListener(ActionListener l) {
      editor.removeActionListener(l);
    }

    static class BorderlessTextField extends JTextField {
      protected static final long serialVersionUID = 1L;	
      public BorderlessTextField(String value,int n) {
        super(value,n);
      }

      // workaround for 4530952
      public void setText(String s) {
        if (getText().equals(s)) {
          return;
        }
        super.setText(s);
      }

      public void setBorder(Border b) {}
    }
    
    /**
     * A subclass of DirectoryComboBoxEditor that implements UIResource.
     * DirectoryComboBoxEditor doesn't implement UIResource
     * directly so that applications can safely override the
     * cellRenderer property with BasicListCellRenderer subclasses.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases. The current serialization support is
     * appropriate for short term storage or RMI between applications running
     * the same version of Swing.  As of 1.4, support for long term storage
     * of all JavaBeans<sup><font size="-2">TM</font></sup>
     * has been added to the <code>java.beans</code> package.
     * Please see {@link java.beans.XMLEncoder}.
     */
    public static class UIResource extends DirectoryComboBoxEditor
    implements javax.swing.plaf.UIResource {
    }
}

