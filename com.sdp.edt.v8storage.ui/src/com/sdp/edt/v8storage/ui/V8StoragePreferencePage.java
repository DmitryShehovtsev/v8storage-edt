package com.sdp.edt.v8storage.ui;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class V8StoragePreferencePage
    extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage
{

    public V8StoragePreferencePage()
    {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setTitle(Messages.V8StoragePreferencePage_PageName);
        setDescription(Messages.V8StoragePreferencePage_Description);
    }

    @Override
    public void createFieldEditors()
    {
        FieldPreferencesChecker checkerField = new FieldPreferencesChecker(Activator.PREF_SCRIPT_PATH,
            Messages.V8StoragePreferencePage_ScriptPath, getFieldEditorParent());
        addField(checkerField);
    }

    @Override
    public void init(IWorkbench workbench)
    {
        //
    }
}
