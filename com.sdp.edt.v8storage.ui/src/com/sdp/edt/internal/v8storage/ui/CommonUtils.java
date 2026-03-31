package com.sdp.edt.internal.v8storage.ui;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;

import com._1c.g5.v8.dt.internal.platform.services.ui.infobases.export.ConfigurationOrExtension;
import com._1c.g5.v8.dt.platform.services.core.infobases.InfobaseAccessType;
import com._1c.g5.v8.dt.platform.services.core.infobases.export.ExportConfigurationFileException;
import com._1c.g5.v8.dt.platform.services.core.infobases.export.IExportConfigurationFileService;
import com._1c.g5.v8.dt.platform.services.core.runtimes.environments.IResolvableRuntimeInstallation;
import com._1c.g5.v8.dt.platform.services.core.runtimes.environments.IResolvableRuntimeInstallationManager;
import com._1c.g5.v8.dt.platform.services.core.runtimes.environments.MatchingRuntimeNotFound;
import com._1c.g5.v8.dt.platform.services.core.runtimes.execution.ComponentExecutorInfo;
import com._1c.g5.v8.dt.platform.services.core.runtimes.execution.ILaunchableRuntimeComponent;
import com._1c.g5.v8.dt.platform.services.core.runtimes.execution.IRuntimeComponentManager;
import com._1c.g5.v8.dt.platform.services.core.runtimes.execution.IThickClientLauncher;
import com._1c.g5.v8.dt.platform.services.core.runtimes.execution.RuntimeExecutionException;
import com._1c.g5.v8.dt.platform.services.model.InfobaseReference;
import com._1c.g5.v8.dt.platform.services.model.RuntimeInstallation;
import com._1c.g5.v8.dt.platform.services.ui.PlatformServicesUiPlugin;
import com._1c.g5.v8.dt.platform.services.ui.model.ExportConfigurationFileCommand;
import com._1c.g5.wiring.ServiceAccess;
import com._1c.g5.wiring.ServiceSupplier;
import com.e1c.g5.dt.applications.ApplicationException;
import com.e1c.g5.dt.applications.ApplicationUpdateState;
import com.e1c.g5.dt.applications.ApplicationUpdateType;
import com.e1c.g5.dt.applications.ExecutionContext;
import com.e1c.g5.dt.applications.IApplication;
import com.e1c.g5.dt.applications.IApplicationManager;
import com.sdp.edt.internal.v8storage.Activator;

public class CommonUtils
{

    private ServiceSupplier<IApplicationManager> applicationManager =
        ServiceAccess.supplier(IApplicationManager.class, Activator.getDefault());

    private ServiceSupplier<IExportConfigurationFileService> exportConfigurationFileService =
        ServiceAccess.supplier(IExportConfigurationFileService.class, Activator.getDefault());

    private ServiceSupplier<IResolvableRuntimeInstallationManager> resolvableRuntimeInstallationManager =
        ServiceAccess.supplier(IResolvableRuntimeInstallationManager.class, Activator.getDefault());

    private ServiceSupplier<IRuntimeComponentManager> componentManager =
        ServiceAccess.supplier(IRuntimeComponentManager.class, Activator.getDefault());

    private ExportConfigurationFileCommand command;
    private IResolvableRuntimeInstallation installation;

    public ApplicationUpdateState applicationUpdate(IProject project, IProgressMonitor monitor, Shell shell)
        throws InvocationTargetException
    {
        IApplicationManager applicationManager = getApplicationManager();
        Optional<IApplication> application = applicationManager.getDefaultApplication(project);
        ExecutionContext context = new ExecutionContext(Map.of("activeShell", shell)); //$NON-NLS-1$
        applicationManager.prepare(application.get(), "debug", context, monitor); //$NON-NLS-1$
        ApplicationUpdateState updateState =
            applicationManager.update(application.get(), ApplicationUpdateType.INCREMENTAL, context, monitor);
        return updateState;
    }

    public void applicationConfigDump(IProject project, String fileName, IProgressMonitor monitor)
        throws InvocationTargetException
    {
        IResolvableRuntimeInstallation resolvable;
        InfobaseReference infobase = this.command.getDescription().getInfobase();
        ConfigurationOrExtension source = this.command.getSource();
        String extensionName = source.isExtension() ? source.getName() : null;
        IPath projectLocation = project.getLocation();
        fileName = (fileName == "") ? "1cv8" : fileName; //$NON-NLS-1$ //$NON-NLS-2$
        Path destination = Path.of(projectLocation.toOSString(), fileName);
        IApplicationManager applicationManager = getApplicationManager();
        IResolvableRuntimeInstallationManager resolvableRuntimeInstallationManager =
            getResolvableRuntimeInstallationManager();
        IRuntimeComponentManager componentManager = getComponentManager();
        IExportConfigurationFileService exportConfigurationFileService = getExportConfigurationFileService();

        try
        {
            project = Optional.ofNullable(this.command.getAssociatedProjectToLoad()).orElseGet(() -> {
                return applicationManager.findApplicationByInfobase(infobase)
                    .map(IApplication::getProject)
                    .orElse((IProject)null);
            });
        }
        catch (ApplicationException e)
        {
            PlatformServicesUiPlugin.log(e.getStatus());
            project = null;
        }

        try
        {
            resolvable = this.installation != null ? this.installation
                : resolvableRuntimeInstallationManager.resolveByProjectAndInfobase(
                    "com._1c.g5.v8.dt.platform.services.core.runtimeType.EnterprisePlatform", project, infobase, //$NON-NLS-1$
                    InfobaseAccessType.UPDATE);
        }
        catch (MatchingRuntimeNotFound var12)
        {
            throw new InvocationTargetException(var12);
        }

        RuntimeInstallation resolved = resolvable.resolve(
            List.of("com._1c.g5.v8.dt.platform.services.core.componentTypes.ThickClient"), infobase.getAppArch()); //$NON-NLS-1$

        ComponentExecutorInfo<ILaunchableRuntimeComponent, IThickClientLauncher> executor;
        try
        {
            executor =
                componentManager.resolveExecutor(ILaunchableRuntimeComponent.class, IThickClientLauncher.class,
                    resolved, "com._1c.g5.v8.dt.platform.services.core.componentTypes.ThickClient"); //$NON-NLS-1$
        }
        catch (RuntimeExecutionException var11)
        {
            throw new InvocationTargetException(var11);
        }

        try
        {
            exportConfigurationFileService.exportConfigurationOrExtension(infobase,
                executor.getComponent(), executor.getExecutor(), extensionName, destination, monitor);
        }
        catch (ExportConfigurationFileException var10)
        {
            throw new InvocationTargetException(var10);
        }
    }

    public void dispose()
    {
        applicationManager.close();
        componentManager.close();
        exportConfigurationFileService.close();
        resolvableRuntimeInstallationManager.close();
    }

    private IApplicationManager getApplicationManager()
    {
        return applicationManager.get();
    }

    private IRuntimeComponentManager getComponentManager()
    {
        return componentManager.get();
    }

    private IExportConfigurationFileService getExportConfigurationFileService()
    {
        return exportConfigurationFileService.get();
    }

    private IResolvableRuntimeInstallationManager getResolvableRuntimeInstallationManager()
    {
        return resolvableRuntimeInstallationManager.get();
    }

}
