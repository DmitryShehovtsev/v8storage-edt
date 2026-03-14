package com.sdp.edt.internal.v8storage.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PushDialog
    extends Dialog
{
    private static PushDialog instance;

    private Text hashCommitText;
    private Text messageCommitText;
    private Text commitInfoText;
    private IProject project;
    private ICommitHandler handler;

    private PushDialog(Shell parentShell, ICommitHandler handler)
    {
        super(parentShell);
        this.handler = handler;
        this.project = CommonUI.getActiveProject();
    }

    public static void show(Shell parentShell, ICommitHandler handler)
    {
        if (instance != null && instance.getShell() != null && !instance.getShell().isDisposed())
        {
            instance.handler = handler;
            instance.updateContent();
            instance.getShell().forceActive();
        }
        else
        {
            instance = new PushDialog(parentShell, handler);
            instance.open();
            instance.getShell().addDisposeListener(e -> instance = null);
        }
    }

    private void updateContent()
    {
        project = CommonUI.getActiveProject();
        commitInfoText.setText(GitActions.getCommitContextInfo(project));
        hashCommitText.setText(GitActions.getParentHash(project));
        messageCommitText.setText(""); //$NON-NLS-1$
        messageCommitText.setFocus();
    }

    @Override
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText(Messages.PushDialog_Header);
        newShell.setSize(420, 400);

        Shell parent = getParentShell();
        if (parent != null)
        {
            Rectangle parentBounds = parent.getBounds();
            Point dialogSize = newShell.getSize();
            int x = parentBounds.x + (parentBounds.width - dialogSize.x) / 2;
            int y = parentBounds.y + (parentBounds.height - dialogSize.y) / 2;
            newShell.setLocation(x, y);
        }
    }

    @Override
    protected int getShellStyle()
    {
        return SWT.MODELESS | SWT.RESIZE | SWT.TITLE | SWT.BORDER;
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 10;
        layout.marginWidth = 10;
        container.setLayout(layout);

        Label hintLabel = new Label(container, SWT.WRAP);
        hintLabel.setText(Messages.PushDialog_Description);
        hintLabel.setForeground(container.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BORDER));
        hintLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        addSpacer(container);

        commitInfoText = new Text(container, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
        commitInfoText.setText(GitActions.getCommitContextInfo(project));
        commitInfoText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        addSpacer(container);

        Label hashLabel = new Label(container, SWT.NONE);
        hashLabel.setText(Messages.PushDialog_hashLabel);
        hashLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

        hashCommitText = new Text(container, SWT.BORDER | SWT.SINGLE);
        hashCommitText.setText(GitActions.getParentHash(project));
        hashCommitText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        hashCommitText.addKeyListener(new KeyAdapter()
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

        Label messageLabel = new Label(container, SWT.NONE);
        messageLabel.setText(Messages.PushDialog_messageLabel);
        messageLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

        messageCommitText = new Text(container, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        GridData messageData = new GridData(SWT.FILL, SWT.FILL, true, true);
        messageCommitText.setLayoutData(messageData);
        messageCommitText.setFocus();

        return container;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent)
    {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void buttonPressed(int buttonId)
    {
        switch (buttonId)
        {
        case IDialogConstants.OK_ID:
            String message = messageCommitText.getText().trim();
            if (message.isEmpty())
            {
                MessageDialog.openError(getShell(), Messages.PushDialog_check, Messages.PushDialog_checkMessage);
                return;
            }
            String hashValue = hashCommitText.getText().trim();
            if (handler != null)
            {
                handler.onCommit(hashValue, message);
            }
            close();
            break;
        case IDialogConstants.CANCEL_ID:
            close();
            break;
        }

        super.buttonPressed(buttonId);
    }

    private void addSpacer(Composite container)
    {
        Label spacer = new Label(container, SWT.NONE);
        GridData spacerData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        spacerData.heightHint = 10;
        spacer.setLayoutData(spacerData);
    }
}
