package it.unibz.krdb.obda.gui.swing.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

public class DialogUtils {

	public static void showQuickErrorDialog(Component parent, Exception e) {
		showQuickErrorDialog(parent, e, "An Error Has Ocurred");		
	}
	
	public static void showQuickErrorDialog(Component parent, Exception e, String message) {
		// create and configure a text area - fill it with exception text.
		final JTextArea textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setBackground(Color.WHITE);
		textArea.setFont(new Font("Dialog", Font.PLAIN, 12));
		textArea.setEditable(false);
		textArea.setWrapStyleWord(true);
		StringWriter writer = new StringWriter();
		writer.write(e.getMessage());
//		e.printStackTrace(new PrintWriter(writer));
		textArea.setText(writer.toString());

		// stuff it in a scrollpane with a controlled size.
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(480, 150));

		// pass the scrollpane to the joptionpane.
		JOptionPane.showMessageDialog(parent, scrollPane, message, JOptionPane.ERROR_MESSAGE);
	}
	
	public static void centerDialogWRTParent(Component parent, Component dialog) {
		int x;
		int y;

		// Find out our parent
		Container myParent = (Container) parent;
		Point topLeft = myParent.getLocationOnScreen();
		Dimension parentSize = myParent.getSize();
		Dimension mySize = dialog.getSize();

		if (parentSize.width > mySize.width)
			x = ((parentSize.width - mySize.width) / 2) + topLeft.x;
		else
			x = topLeft.x;

		if (parentSize.height > mySize.height)
			y = ((parentSize.height - mySize.height) / 2) + topLeft.y;
		else
			y = topLeft.y;

		dialog.setLocation(x, y);

	}

	public static JDialog createDialogForPanel(JDialog parent, JPanel panel) {
		JDialog newdialog = new JDialog(parent);
		newdialog.getContentPane().add(panel, java.awt.BorderLayout.CENTER);
		newdialog.pack();
		return newdialog;
	}

	public static JDialog createDialogForPanel(JFrame parent, JPanel panel) {
		JDialog newdialog = new JDialog(parent);
		newdialog.getContentPane().add(panel, java.awt.BorderLayout.CENTER);
		newdialog.pack();
		return newdialog;
	}

	public static void setAntializaing(Component component, boolean value) {
		// if (component instanceof JComponent) {
		// ((JComponent)
		// component).putClientProperty(com.sun.java.swing.SwingUtilities2.AA_TEXT_PROPERTY_KEY,
		// value);
		// Component[] children = ((JComponent)component).getComponents();
		// for (int i = 0; i < children.length; i++) {
		// if (children[i] instanceof JComponent) {
		// setAntializaing((JComponent)children[i], value);
		// }
		// if (children[i] instanceof JTree) {
		// TreeCellRenderer rend = ((JTree)children[i]).getCellRenderer();
		// if (rend instanceof JComponent) {
		// ((JComponent)rend).putClientProperty(com.sun.java.swing.SwingUtilities2.AA_TEXT_PROPERTY_KEY,
		// true);
		// }
		// }
		// if (children[i] instanceof JTable) {
		// JTable table = (JTable)children[i];
		// TableCellRenderer crenderer = table.getDefaultRenderer(Object.class);
		// if (crenderer instanceof JComponent) {
		// ((JComponent)crenderer).putClientProperty(com.sun.java.swing.SwingUtilities2.AA_TEXT_PROPERTY_KEY,
		// true);
		// }
		// crenderer = table.getDefaultRenderer(Number.class);
		// if (crenderer instanceof JComponent) {
		// ((JComponent)crenderer).putClientProperty(com.sun.java.swing.SwingUtilities2.AA_TEXT_PROPERTY_KEY,
		// true);
		// }
		// crenderer = table.getDefaultRenderer(Boolean.class);
		// if (crenderer instanceof JComponent) {
		// ((JComponent)crenderer).putClientProperty(com.sun.java.swing.SwingUtilities2.AA_TEXT_PROPERTY_KEY,
		// true);
		// }
		//
		//
		// crenderer = table.getTableHeader().getDefaultRenderer();
		// if (crenderer instanceof JComponent) {
		// ((JComponent)crenderer).putClientProperty(com.sun.java.swing.SwingUtilities2.AA_TEXT_PROPERTY_KEY,
		// true);
		// }
		// }
		//
		// }
		// }

	}

	private static final KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	public static final String dispatchWindowClosingActionMapKey = "com.spodding.tackline.dispatch:WINDOW_CLOSING";

	public static void installEscapeCloseOperation(final JDialog dialog) {
		Action dispatchClosing = new AbstractAction() {
			public void actionPerformed(ActionEvent event) {
				dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
			}
		};
		JRootPane root = dialog.getRootPane();
		root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeStroke, dispatchWindowClosingActionMapKey);
		root.getActionMap().put(dispatchWindowClosingActionMapKey, dispatchClosing);
	}

}
