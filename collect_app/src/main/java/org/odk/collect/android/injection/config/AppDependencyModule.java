package org.odk.collect.android.injection.config;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.media.MediaPlayer;
import android.telephony.TelephonyManager;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.lifecycle.AbstractSavedStateViewModelFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.work.WorkManager;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;

import org.javarosa.core.reference.ReferenceManager;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.analytics.BlockableFirebaseAnalytics;
import org.odk.collect.analytics.NoopAnalytics;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.application.CollectSettingsChangeHandler;
import org.odk.collect.android.application.initialization.ApplicationInitializer;
import org.odk.collect.android.application.initialization.CollectSettingsPreferenceMigrator;
import org.odk.collect.android.application.initialization.SettingsPreferenceMigrator;
import org.odk.collect.android.backgroundwork.ChangeLock;
import org.odk.collect.android.backgroundwork.FormSubmitManager;
import org.odk.collect.android.backgroundwork.FormUpdateManager;
import org.odk.collect.android.backgroundwork.ReentrantLockChangeLock;
import org.odk.collect.android.backgroundwork.SchedulerFormUpdateAndSubmitManager;
import org.odk.collect.android.configure.ServerRepository;
import org.odk.collect.android.configure.SettingsChangeHandler;
import org.odk.collect.android.configure.SettingsImporter;
import org.odk.collect.android.configure.SharedPreferencesServerRepository;
import org.odk.collect.android.configure.StructureAndTypeSettingsValidator;
import org.odk.collect.android.configure.qr.CachingQRCodeGenerator;
import org.odk.collect.android.configure.qr.QRCodeDecoder;
import org.odk.collect.android.configure.qr.QRCodeGenerator;
import org.odk.collect.android.configure.qr.QRCodeUtils;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.database.DatabaseFormsRepository;
import org.odk.collect.android.database.DatabaseInstancesRepository;
import org.odk.collect.android.database.DatabaseMediaFileRepository;
import org.odk.collect.android.events.RxEventBus;
import org.odk.collect.android.formentry.BackgroundAudioViewModel;
import org.odk.collect.android.formentry.FormEntryViewModel;
import org.odk.collect.android.formentry.media.AudioHelperFactory;
import org.odk.collect.android.formentry.media.ScreenContextAudioHelperFactory;
import org.odk.collect.android.formentry.saving.DiskFormSaver;
import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.odk.collect.android.formmanagement.DiskFormsSynchronizer;
import org.odk.collect.android.formmanagement.FormDownloader;
import org.odk.collect.android.formmanagement.FormMetadataParser;
import org.odk.collect.android.formmanagement.ServerFormDownloader;
import org.odk.collect.android.formmanagement.ServerFormsDetailsFetcher;
import org.odk.collect.android.formmanagement.matchexactly.ServerFormsSynchronizer;
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusRepository;
import org.odk.collect.android.forms.FormSource;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.forms.MediaFileRepository;
import org.odk.collect.android.gdrive.GoogleAccountCredentialGoogleAccountPicker;
import org.odk.collect.android.gdrive.GoogleAccountPicker;
import org.odk.collect.android.gdrive.GoogleApiProvider;
import org.odk.collect.android.geo.MapProvider;
import org.odk.collect.android.instances.InstancesRepository;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.metadata.InstallIDProvider;
import org.odk.collect.android.metadata.SharedPreferencesInstallIDProvider;
import org.odk.collect.android.network.ConnectivityProvider;
import org.odk.collect.android.network.NetworkStateProvider;
import org.odk.collect.android.notifications.NotificationManagerNotifier;
import org.odk.collect.android.notifications.Notifier;
import org.odk.collect.android.openrosa.CollectThenSystemContentTypeMapper;
import org.odk.collect.android.openrosa.OpenRosaFormSource;
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.android.openrosa.OpenRosaResponseParserImpl;
import org.odk.collect.android.openrosa.okhttp.OkHttpConnection;
import org.odk.collect.android.openrosa.okhttp.OkHttpOpenRosaServerClientProvider;
import org.odk.collect.android.permissions.PermissionsChecker;
import org.odk.collect.android.permissions.PermissionsProvider;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.JsonPreferencesGenerator;
import org.odk.collect.android.preferences.PreferencesDataSourceProvider;
import org.odk.collect.android.storage.StorageInitializer;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.AdminPasswordProvider;
import org.odk.collect.android.utilities.AndroidUserAgent;
import org.odk.collect.android.utilities.DeviceDetailsProvider;
import org.odk.collect.android.utilities.ExternalAppIntentProvider;
import org.odk.collect.android.utilities.FileProvider;
import org.odk.collect.android.utilities.FileUtil;
import org.odk.collect.android.utilities.FormsDirDiskFormsSynchronizer;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.utilities.ScreenUtils;
import org.odk.collect.android.utilities.SoftKeyboardController;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.odk.collect.android.version.VersionInformation;
import org.odk.collect.android.views.BarcodeViewDecoder;
import org.odk.collect.async.CoroutineAndWorkManagerScheduler;
import org.odk.collect.async.Scheduler;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.audiorecorder.recording.AudioRecorderFactory;
import org.odk.collect.utilities.Clock;
import org.odk.collect.utilities.UserAgentProvider;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

import static androidx.core.content.FileProvider.getUriForFile;
import static org.odk.collect.android.preferences.MetaKeys.KEY_INSTALL_ID;

/**
 * Add dependency providers here (annotated with @Provides)
 * for objects you need to inject
 */
@Module
@SuppressWarnings("PMD.CouplingBetweenObjects")
public class AppDependencyModule {

    @Provides
    Context context(Application application) {
        return application;
    }

    @Provides
    public InstancesDao provideInstancesDao() {
        return new InstancesDao();
    }

    @Provides
    public FormsDao provideFormsDao() {
        return new FormsDao();
    }

    @Provides
    @Singleton
    RxEventBus provideRxEventBus() {
        return new RxEventBus();
    }

    @Provides
    MimeTypeMap provideMimeTypeMap() {
        return MimeTypeMap.getSingleton();
    }

    @Provides
    @Singleton
    UserAgentProvider providesUserAgent() {
        return new AndroidUserAgent();
    }

    @Provides
    @Singleton
    public OpenRosaHttpInterface provideHttpInterface(MimeTypeMap mimeTypeMap, UserAgentProvider userAgentProvider) {
        return new OkHttpConnection(
                new OkHttpOpenRosaServerClientProvider(new OkHttpClient()),
                new CollectThenSystemContentTypeMapper(mimeTypeMap),
                userAgentProvider.getUserAgent()
        );
    }

    @Provides
    WebCredentialsUtils provideWebCredentials(PreferencesDataSourceProvider preferencesDataSourceProvider) {
        return new WebCredentialsUtils(preferencesDataSourceProvider.getGeneralPreferences());
    }

    @Provides
    public FormDownloader providesFormDownloader(FormSource formSource, FormsRepository formsRepository, StoragePathProvider storagePathProvider, Analytics analytics) {
        return new ServerFormDownloader(formSource, formsRepository, new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE)), storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS), new FormMetadataParser(ReferenceManager.instance()), analytics);
    }

    @Provides
    @Singleton
    public Analytics providesAnalytics(Application application) {
        try {
            return new BlockableFirebaseAnalytics(application);
        } catch (IllegalStateException e) {
            // Couldn't setup Firebase so use no-op instance
            return new NoopAnalytics();
        }
    }

    @Provides
    public PermissionsProvider providesPermissionsProvider(PermissionsChecker permissionsChecker) {
        return new PermissionsProvider(permissionsChecker);
    }

    @Provides
    public ReferenceManager providesReferenceManager() {
        return ReferenceManager.instance();
    }

    @Provides
    public AudioHelperFactory providesAudioHelperFactory(Scheduler scheduler) {
        return new ScreenContextAudioHelperFactory(scheduler, MediaPlayer::new);
    }

    @Provides
    public ActivityAvailability providesActivityAvailability(Context context) {
        return new ActivityAvailability(context);
    }

    @Provides
    @Singleton
    public StorageInitializer providesStorageInitializer() {
        return new StorageInitializer();
    }

    @Provides
    @Singleton
    public PreferencesDataSourceProvider providesPreferencesRepository(Context context) {
        return new PreferencesDataSourceProvider(context);
    }

    @Provides
    InstallIDProvider providesInstallIDProvider(PreferencesDataSourceProvider preferencesDataSourceProvider) {
        return new SharedPreferencesInstallIDProvider(preferencesDataSourceProvider.getMetaPreferences(), KEY_INSTALL_ID);
    }

    @Provides
    public DeviceDetailsProvider providesDeviceDetailsProvider(Context context, InstallIDProvider installIDProvider) {
        return new DeviceDetailsProvider() {

            @Override
            @SuppressLint({"MissingPermission", "HardwareIds"})
            public String getDeviceId() {
                return installIDProvider.getInstallID();
            }

            @Override
            @SuppressLint({"MissingPermission", "HardwareIds"})
            public String getLine1Number() {
                TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                return telMgr.getLine1Number();
            }
        };
    }

    @Provides
    @Singleton
    public MapProvider providesMapProvider() {
        return new MapProvider();
    }

    @Provides
    public StoragePathProvider providesStoragePathProvider() {
        return new StoragePathProvider();
    }

    @Provides
    public AdminPasswordProvider providesAdminPasswordProvider(PreferencesDataSourceProvider preferencesDataSourceProvider) {
        return new AdminPasswordProvider(preferencesDataSourceProvider.getAdminPreferences());
    }

    @Provides
    public FormUpdateManager providesFormUpdateManger(Scheduler scheduler, PreferencesDataSourceProvider preferencesDataSourceProvider, Application application) {
        return new SchedulerFormUpdateAndSubmitManager(scheduler, preferencesDataSourceProvider.getGeneralPreferences(), application);
    }

    @Provides
    public FormSubmitManager providesFormSubmitManager(Scheduler scheduler, PreferencesDataSourceProvider preferencesDataSourceProvider, Application application) {
        return new SchedulerFormUpdateAndSubmitManager(scheduler, preferencesDataSourceProvider.getGeneralPreferences(), application);
    }

    @Provides
    public NetworkStateProvider providesConnectivityProvider() {
        return new ConnectivityProvider();
    }

    @Provides
    public QRCodeGenerator providesQRCodeGenerator(Context context) {
        return new CachingQRCodeGenerator();
    }

    @Provides
    public VersionInformation providesVersionInformation() {
        return new VersionInformation(() -> BuildConfig.VERSION_NAME);
    }

    @Provides
    public FileProvider providesFileProvider(Context context) {
        return filePath -> getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", new File(filePath));
    }

    @Provides
    public WorkManager providesWorkManager(Context context) {
        return WorkManager.getInstance(context);
    }

    @Provides
    public Scheduler providesScheduler(WorkManager workManager) {
        return new CoroutineAndWorkManagerScheduler(workManager);
    }

    @Singleton
    @Provides
    public ApplicationInitializer providesApplicationInitializer(Application application, UserAgentProvider userAgentProvider,
                                                                 SettingsPreferenceMigrator preferenceMigrator, PropertyManager propertyManager,
                                                                 Analytics analytics, StorageInitializer storageInitializer, PreferencesDataSourceProvider preferencesDataSourceProvider) {
        return new ApplicationInitializer(application, userAgentProvider, preferenceMigrator, propertyManager, analytics, storageInitializer, preferencesDataSourceProvider);
    }

    @Provides
    public SettingsPreferenceMigrator providesPreferenceMigrator(PreferencesDataSourceProvider preferencesDataSourceProvider) {
        return new CollectSettingsPreferenceMigrator(preferencesDataSourceProvider.getMetaPreferences());
    }

    @Provides
    @Singleton
    public PropertyManager providesPropertyManager(RxEventBus eventBus, PermissionsProvider permissionsProvider, DeviceDetailsProvider deviceDetailsProvider, PreferencesDataSourceProvider preferencesDataSourceProvider) {
        return new PropertyManager(eventBus, permissionsProvider, deviceDetailsProvider, preferencesDataSourceProvider);
    }

    @Provides
    public ServerRepository providesServerRepository(Context context, PreferencesDataSourceProvider preferencesDataSourceProvider) {
        return new SharedPreferencesServerRepository(context.getString(R.string.default_server_url), preferencesDataSourceProvider.getMetaPreferences());
    }

    @Provides
    public SettingsChangeHandler providesSettingsChangeHandler(PropertyManager propertyManager, FormUpdateManager formUpdateManager, ServerRepository serverRepository, Analytics analytics, PreferencesDataSourceProvider preferencesDataSourceProvider) {
        return new CollectSettingsChangeHandler(propertyManager, formUpdateManager, serverRepository, analytics, preferencesDataSourceProvider);
    }

    @Provides
    public SettingsImporter providesCollectSettingsImporter(PreferencesDataSourceProvider preferencesDataSourceProvider, SettingsPreferenceMigrator preferenceMigrator, SettingsChangeHandler settingsChangeHandler) {
        HashMap<String, Object> generalDefaults = GeneralKeys.DEFAULTS;
        Map<String, Object> adminDefaults = AdminKeys.getDefaults();
        return new SettingsImporter(
                preferencesDataSourceProvider.getGeneralPreferences(),
                preferencesDataSourceProvider.getAdminPreferences(),
                preferenceMigrator,
                new StructureAndTypeSettingsValidator(generalDefaults, adminDefaults),
                generalDefaults,
                adminDefaults,
                settingsChangeHandler
        );
    }

    @Provides
    public BarcodeViewDecoder providesBarcodeViewDecoder() {
        return new BarcodeViewDecoder();
    }

    @Provides
    public QRCodeDecoder providesQRCodeDecoder() {
        return new QRCodeUtils();
    }

    @Provides
    public FormsRepository providesFormRepository() {
        return new DatabaseFormsRepository();
    }

    @Provides
    public MediaFileRepository providesMediaFileRepository() {
        return new DatabaseMediaFileRepository(new FormsDao(), new FileUtil());
    }

    @Provides
    public FormSource providesFormSource(PreferencesDataSourceProvider preferencesDataSourceProvider, Context context, OpenRosaHttpInterface openRosaHttpInterface, WebCredentialsUtils webCredentialsUtils, Analytics analytics) {
        String serverURL = preferencesDataSourceProvider.getGeneralPreferences().getString(GeneralKeys.KEY_SERVER_URL);
        String formListPath = preferencesDataSourceProvider.getGeneralPreferences().getString(GeneralKeys.KEY_FORMLIST_URL);

        return new OpenRosaFormSource(serverURL, formListPath, openRosaHttpInterface, webCredentialsUtils, analytics, new OpenRosaResponseParserImpl());
    }

    @Provides
    public DiskFormsSynchronizer providesDiskFormSynchronizer() {
        return new FormsDirDiskFormsSynchronizer();
    }

    @Provides
    @Singleton
    public SyncStatusRepository providesServerFormSyncRepository() {
        return new SyncStatusRepository();
    }

    @Provides
    public ServerFormsDetailsFetcher providesServerFormDetailsFetcher(FormsRepository formsRepository, MediaFileRepository mediaFileRepository, FormSource formSource, DiskFormsSynchronizer diskFormsSynchronizer) {
        return new ServerFormsDetailsFetcher(formsRepository, mediaFileRepository, formSource, diskFormsSynchronizer);
    }

    @Provides
    public ServerFormsSynchronizer providesServerFormSynchronizer(ServerFormsDetailsFetcher serverFormsDetailsFetcher, FormsRepository formsRepository, FormDownloader formDownloader, InstancesRepository instancesRepository) {
        return new ServerFormsSynchronizer(serverFormsDetailsFetcher, formsRepository, instancesRepository, formDownloader);
    }

    @Provides
    public Notifier providesNotifier(Application application, PreferencesDataSourceProvider preferencesDataSourceProvider) {
        return new NotificationManagerNotifier(application, preferencesDataSourceProvider);
    }

    @Provides
    @Named("FORMS")
    @Singleton
    public ChangeLock providesFormsChangeLock() {
        return new ReentrantLockChangeLock();
    }

    @Provides
    @Named("INSTANCES")
    @Singleton
    public ChangeLock providesInstancesChangeLock() {
        return new ReentrantLockChangeLock();
    }

    @Provides
    public InstancesRepository providesInstancesRepository() {
        return new DatabaseInstancesRepository();
    }

    @Provides
    public GoogleApiProvider providesGoogleApiProvider(Context context) {
        return new GoogleApiProvider(context);
    }

    @Provides
    public GoogleAccountPicker providesGoogleAccountPicker(Context context) {
        return new GoogleAccountCredentialGoogleAccountPicker(GoogleAccountCredential
                .usingOAuth2(context, Collections.singletonList(DriveScopes.DRIVE))
                .setBackOff(new ExponentialBackOff()));
    }

    @Provides
    ScreenUtils providesScreenUtils(Context context) {
        return new ScreenUtils(context);
    }

    @Provides
    public AudioRecorder providesAudioRecorder(Application application) {
        return new AudioRecorderFactory(application).create();
    }

    @Provides
    public FormSaveViewModel.FactoryFactory providesFormSaveViewModelFactoryFactory(Analytics analytics, Scheduler scheduler, AudioRecorder audioRecorder) {
        return (owner, defaultArgs) -> new AbstractSavedStateViewModelFactory(owner, defaultArgs) {
            @NonNull
            @Override
            protected <T extends ViewModel> T create(@NonNull String key, @NonNull Class<T> modelClass, @NonNull SavedStateHandle handle) {
                return (T) new FormSaveViewModel(handle, System::currentTimeMillis, new DiskFormSaver(), new MediaUtils(), analytics, scheduler, audioRecorder);
            }
        };
    }

    @Provides
    public Clock providesClock() {
        return System::currentTimeMillis;
    }

    @Provides
    public SoftKeyboardController provideSoftKeyboardController() {
        return new SoftKeyboardController();
    }

    @Provides
    public JsonPreferencesGenerator providesJsonPreferencesGenerator(PreferencesDataSourceProvider preferencesDataSourceProvider) {
        return new JsonPreferencesGenerator(preferencesDataSourceProvider);
    }

    @Provides
    @Singleton
    public PermissionsChecker providesPermissionsChecker(Context context) {
        return new PermissionsChecker(context);
    }

    @Provides
    @Singleton
    public ExternalAppIntentProvider providesExternalAppIntentProvider() {
        return new ExternalAppIntentProvider();
    }

    @Provides
    public FormEntryViewModel.Factory providesFormEntryViewModelFactory(Clock clock, Analytics analytics) {
        return new FormEntryViewModel.Factory(clock, analytics);
    }

    @Provides
    public BackgroundAudioViewModel.Factory providesBackgroundAudioViewModelFactory(AudioRecorder audioRecorder, PreferencesDataSourceProvider preferencesDataSourceProvider, PermissionsChecker permissionsChecker, Clock clock, Analytics analytics) {
        return new BackgroundAudioViewModel.Factory(audioRecorder, preferencesDataSourceProvider.getGeneralPreferences(), permissionsChecker, clock, analytics);
    }
}
