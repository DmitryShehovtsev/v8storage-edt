package com.sdp.edt.v8storagesync.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.sdp.edt.v8storagesync.ui.Activator;

public class V8StoragePreferencePage
    extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage
{

    public V8StoragePreferencePage()
    {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    public void createFieldEditors()
    {
        Link link = new Link(getFieldEditorParent(), SWT.NONE);
        String Description =
            String.format("%s\n%s", Messages.V8StoragePreferencePage_Description, "<a>http://www.example.com</a>"); //$NON-NLS-1$ //$NON-NLS-2$
        link.setText(Description);
        link.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                Program.launch(e.text);
            }
        });
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        link.setLayoutData(gd);

        addSpacer();

        FieldPreferencesChecker checkerField = new FieldPreferencesChecker(Activator.PREF_SCRIPT_PATH,
            Messages.V8StoragePreferencePage_ScriptPath, getFieldEditorParent());
        addField(checkerField);
    }

    @Override
    public void init(IWorkbench workbench)
    {
        //
    }

    private void addSpacer()
    {
        Label spacer = new Label(getFieldEditorParent(), SWT.NONE);
        GridData spacerData = new GridData();
        spacerData.horizontalSpan = 2;
        spacerData.heightHint = 10;
        spacer.setLayoutData(spacerData);
    }
}
