package com.sdp.edt.v8storage.ui;

import java.io.File;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

public class FieldPreferencesChecker
    extends StringFieldEditor
{

    public FieldPreferencesChecker(String name, String labelText, Composite parent)
    {
        super(name, labelText, parent);
        setEmptyStringAllowed(true);
        setErrorMessage(Messages.V8StoragePreferencePage_InvalidPath);
    }

    @Override
    protected boolean doCheckState()
    {
        String path = getStringValue();
        if (path.isEmpty())
        {
            return true;
        }
        File file = new File(path);
        return file.exists() && file.isFile();
    }
}
