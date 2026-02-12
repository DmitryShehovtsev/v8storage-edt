package com.sdp.edt.storagehelper.ui;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "com.sdp.edt.storagehelper.ui"; //$NON-NLS-1$

    public static final String PREF_TEST_FILE_PATH = "testFilePath"; //$NON-NLS-1$
    public static final String PREF_SCRIPT_PATH = "scriptPath"; //$NON-NLS-1$
    public static final String PREF_DESCRIBE_FILE_PATH = "describeFilePath"; //$NON-NLS-1$
    public static final String PREF_DEFAULT_DESCRIPTION = "defaultDescription"; //$NON-NLS-1$

    private static Activator plugin;

    @Override
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        plugin = this;

        IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, PLUGIN_ID);
        store.setDefault(PREF_DEFAULT_DESCRIPTION, "Описание не предоставлено"); //$NON-NLS-1$
    }

    @Override
    public void stop(BundleContext context) throws Exception
    {
        plugin = null;
        super.stop(context);
    }

    public static Activator getDefault()
    {
        return plugin;
    }

}
