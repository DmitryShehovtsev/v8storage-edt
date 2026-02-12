package com.sdp.edt.storagehelper.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PushDialog2
    extends Dialog
{

    private Text HashCommitText;
    private Text MessageCommitText;
    private String inputValue;

    protected PushDialog2(Shell parentShell)
    {
        super(parentShell);
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        Composite container = (Composite)super.createDialogArea(parent);
        container.setLayout(new GridLayout(1, false));

        Label HashCommitLabel = new Label(container, SWT.NONE);
        HashCommitLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        HashCommitText = new Text(container, SWT.BORDER | SWT.MULTI);
        HashCommitText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label MessageCommitLabel = new Label(container, SWT.NONE);
        MessageCommitLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        MessageCommitText = new Text(container, SWT.BORDER | SWT.MULTI);
        MessageCommitText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        HashCommitText.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR)
                {
                    buttonPressed(IDialogConstants.OK_ID);
                }
            }
        });

        MessageCommitText.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR)
                {
                    buttonPressed(IDialogConstants.OK_ID);
                }
            }
        });

        return container;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent)
    {
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).getShell().setDefaultButton(getButton(IDialogConstants.OK_ID));
    }

    @Override
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText("Отправка в хранилище"); //$NON-NLS-1$
    }

    @Override
    protected void buttonPressed(int buttonId)
    {
        if (buttonId == IDialogConstants.OK_ID)
        {
            String text = MessageCommitText.getText().trim();
            if (text.isEmpty())
            {
                MessageDialog.openError(getShell(), "Validation Error", "Не заполнено сообщение коммита"); //$NON-NLS-1$ //$NON-NLS-2$
                return;
            }
            inputValue = text;
        }
        super.buttonPressed(buttonId);
    }

    public String getInputText()
    {
        return inputValue;
    }
}
