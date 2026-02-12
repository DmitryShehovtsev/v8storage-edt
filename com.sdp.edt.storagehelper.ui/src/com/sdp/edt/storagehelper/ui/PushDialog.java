package com.sdp.edt.storagehelper.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
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

    private Text hashCommitText;
    private Text messageCommitText;
    private String hashValue;
    private String messageValue;
    private String instructionText; // Генерируется внутри
    private final CommitHandler handler; // Callback для обработки ввода

    public PushDialog(Shell parentShell, CommitHandler handler)
    {
        super(parentShell);
        this.handler = handler;
    }

    @Override
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText("Отправка в хранилище"); //$NON-NLS-1$
        newShell.setSize(400, 400);
        //newShell.setMinimumSize(200, 200);

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
        // Немодальный стиль: MODELESS + RESIZE + TITLE + BORDER
        return SWT.MODELESS | SWT.RESIZE | SWT.TITLE | SWT.BORDER;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 10;
        layout.marginWidth = 10;
        layout.verticalSpacing = 5;
        container.setLayout(layout);

        // Вручную добавляем title label (как в TitleAreaDialog)
        Label titleLabel = new Label(container, SWT.NONE);
        titleLabel.setText("Отправка коммита"); //$NON-NLS-1$
        titleLabel.setFont(
            org.eclipse.jface.resource.JFaceResources.getFont(org.eclipse.jface.resource.JFaceResources.BANNER_FONT));
        titleLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Генерируем instructionText (после создания shell, чтобы GC работал)
        instructionText = GitUtil.getInstructionFromGit(null);

        // Read-only Text для instructionText
        Text instructionTextWidget = new Text(container, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
        GridData instructionData = new GridData(SWT.FILL, SWT.FILL, true, false);
        instructionData.heightHint = computeTextHeightHint(2);
        instructionData.widthHint = 300;
        instructionTextWidget.setLayoutData(instructionData);
        instructionTextWidget.setText(instructionText);

        Label hashLabel = new Label(container, SWT.NONE);
        hashLabel.setText("Хэш коммита-предка:"); //$NON-NLS-1$
        hashLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

        hashCommitText = new Text(container, SWT.BORDER | SWT.SINGLE);
        hashCommitText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        hashCommitText.addKeyListener(createEnterKeyListener());
        hashCommitText.setText(GitUtil.getParentHash(null));

        Label messageLabel = new Label(container, SWT.NONE);
        messageLabel.setText("Сообщение коммита:"); //$NON-NLS-1$
        messageLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

        messageCommitText = new Text(container, SWT.BORDER | SWT.MULTI | SWT.WRAP);
        GridData messageData = new GridData(SWT.FILL, SWT.FILL, true, true);
        messageData.heightHint = computeTextHeightHint(5);
        messageData.widthHint = 300;
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
        if (buttonId == IDialogConstants.OK_ID)
        {
            String message = messageCommitText.getText().trim();
            if (message.isEmpty())
            {
                MessageDialog.openError(getShell(), "Проверка", "Не заполнено сообщение коммита."); //$NON-NLS-1$ //$NON-NLS-2$
                return;
            }
            hashValue = hashCommitText.getText().trim();
            messageValue = message;
            // Вызываем callback и закрываем диалог
            if (handler != null)
            {
                handler.onCommit(hashValue, messageValue);
            }
            close();
        }
        else if (buttonId == IDialogConstants.CANCEL_ID)
        {
            close();
        }
        super.buttonPressed(buttonId);
    }

    private KeyAdapter createEnterKeyListener()
    {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR)
                {
                    buttonPressed(IDialogConstants.OK_ID);
                }
            }
        };
    }

    private int computeTextHeightHint(int numLines)
    {
        GC gc = new GC(getShell());
        FontMetrics fm = gc.getFontMetrics();
        int lineHeight = fm.getHeight();
        gc.dispose();
        return lineHeight * numLines + 20;
    }

    // Геттеры больше не нужны, т.к. данные передаются в callback
    // Но оставим для совместимости, если нужно
    public String getHash()
    {
        return hashValue;
    }

    public String getMessage()
    {
        return messageValue;
    }
}
