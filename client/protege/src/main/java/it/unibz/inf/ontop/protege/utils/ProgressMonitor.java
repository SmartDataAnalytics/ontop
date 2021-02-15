package it.unibz.inf.ontop.protege.utils;

import javax.swing.*;
import java.awt.*;

public class ProgressMonitor extends AbstractProgressMonitor {

    private final Component parent;
    private final String cancelOption;
    private final Object message;
    private final boolean indeterminate;

    private JDialog dialog;
    private JOptionPane pane;
    private JProgressBar progressBar;
    private JLabel noteLabel;

    public ProgressMonitor(Component parent, Object message, boolean indeterminate) {
        this.parent = parent;
        this.message = message;
        this.indeterminate = indeterminate;

        this.cancelOption = UIManager.getString("OptionPane.cancelButtonText");
    }

    @Override
    public void open(String status) {
        if (dialog == null && !isDone() && !isCancelled()) {
            noteLabel = new JLabel("", null, SwingConstants.CENTER);

            progressBar = new JProgressBar();
            if (indeterminate) {
                progressBar.setIndeterminate(true);
            }
            else {
                progressBar.setMinimum(0);
                progressBar.setMaximum(100);
                progressBar.setValue(0);
            }

            pane = new JOptionPane(new Object[] { message, noteLabel, progressBar },
                    JOptionPane.INFORMATION_MESSAGE,
                    JOptionPane.DEFAULT_OPTION,
                    IconLoader.getOntopIcon(),
                    new Object[] { cancelOption },
                    null);

            dialog = pane.createDialog(parent, UIManager.getString("ProgressMonitor.progressText"));
            dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

            pane.addPropertyChangeListener(evt -> {
                if (evt.getSource() == pane
                        && evt.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)
                        && cancelOption.equals(evt.getNewValue())) {

                    if (cancelIfPossible()) {
                        pane.setEnabled(false);
                        proceedCancelling();
                    }
                    dialog.setVisible(true);
                }
            });

            dialog.setResizable(true);
            dialog.setVisible(true);
        }
        super.open(status);
    }

    @Override
    public void close() {
        super.close();
        if (dialog != null) {
            dialog.setVisible(false);
            dialog.dispose();
            dialog = null;
        }
    }


    @Override
    public void setProgress(int percentage, String note) {
        if (dialog != null && !isCancelled()) {
            if (!indeterminate) {
                progressBar.setValue(percentage);
            }
            noteLabel.setText(note);
        }
    }

    @Override
    public void setStatus(String status) {
        if (dialog != null)
            noteLabel.setText(status);
    }
}

