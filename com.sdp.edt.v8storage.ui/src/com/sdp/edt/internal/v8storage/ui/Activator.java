package com.sdp.edt.internal.v8storage.ui;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "com.sdp.edt.internal.v8storage.ui"; //$NON-NLS-1$
    public static final String PREF_SCRIPT_PATH = "scriptPath"; //$NON-NLS-1$
    private static Activator plugin;
    private IPreferenceStore store;

    @Override
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        plugin = this;

        store = new ScopedPreferenceStore(InstanceScope.INSTANCE, PLUGIN_ID);
        store.setDefault(PREF_SCRIPT_PATH, ""); //$NON-NLS-1$
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

    @Override
    public IPreferenceStore getPreferenceStore()
    {
        return store;
    }

}
