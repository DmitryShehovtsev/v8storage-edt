package com.sdp.edt.v8storage.ui;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
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
        setDescription("Консольное приложение, реализующее команды синхронизации с хранилищем"); //$NON-NLS-1$
    }

    @Override
    public void createFieldEditors()
    {
        StringFieldEditor scriptEditor =
            new StringFieldEditor(Activator.PREF_SCRIPT_PATH, "Путь к исполняемому файлу:", getFieldEditorParent()); //$NON-NLS-1$
        scriptEditor.setEmptyStringAllowed(true);
        addField(scriptEditor);
    }

    @Override
    public void init(IWorkbench workbench)
    {
        //
    }
}
