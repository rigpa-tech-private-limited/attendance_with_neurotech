package co.vaango.attendance.multibiometric;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import com.neurotec.lang.NCore;
import com.neurotec.licensing.NLicenseManager;
import com.neurotec.licensing.gui.LicensingPreferencesFragment;
import co.vaango.attendance.util.EnvironmentUtils;

public final class BiometricApplication extends Application {
	private Activity topActivity;

	public Activity getTopActivity() {
		return topActivity;
	}

	public void setTopActivity(Activity topActivity) {
		this.topActivity = topActivity;
	}
	// ===========================================================
	// Private static fields
	// ===========================================================

	private static final String TAG = BiometricApplication.class.getSimpleName();

	// ===========================================================
	// Public static fields
	// ===========================================================

	public static final String APP_NAME = "multibiometric";
	public static final String SAMPLE_DATA_DIR = EnvironmentUtils.getDataDirectoryPath(EnvironmentUtils.SAMPLE_DATA_DIR_NAME, APP_NAME);

	// ===========================================================
	// Public methods
	// ===========================================================

	@Override
	public void onCreate() {
		super.onCreate();
		try {
			NCore.setContext(this);
//			NLicenseManager.setTrialMode(true);
			NLicenseManager.setTrialMode(LicensingPreferencesFragment.isUseTrial(this));
			System.setProperty("jna.nounpack", "true");
			System.setProperty("java.io.tmpdir", getCacheDir().getAbsolutePath());
		} catch (Exception e) {
			Log.e(TAG, "Exception", e);
		}
	}
}
