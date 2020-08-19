package co.vaango.attendance.multibiometric;

import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.client.NClusterBiometricConnection;
import com.neurotec.lang.NCore;
import co.vaango.attendance.multibiometric.preferences.FacePreferences;
import co.vaango.attendance.multibiometric.preferences.FingerPreferences;
import co.vaango.attendance.multibiometric.preferences.IrisPreferences;
import co.vaango.attendance.multibiometric.preferences.VoicePreferences;
import co.vaango.attendance.util.IOUtils;

public final class Model {

	// ===========================================================
	// Private static fields
	// ===========================================================

	private static Model sInstance;

	// ===========================================================
	// Public static methods
	// ===========================================================

	public static Model getInstance() {
		synchronized (Model.class) {
			if (sInstance == null) {
				sInstance = new Model();
			}
			return sInstance;
		}
	}

	// ===========================================================
	// Private fields
	// ===========================================================

	private NBiometricClient mClient;
	private NClusterBiometricConnection mConnection;
	private NSubject mSubject;

	private NSubject[] mSubjects;

	// ===========================================================
	// Private constructor
	// ===========================================================

	private Model() {
		mClient = new NBiometricClient();
		mClient.setDatabaseConnectionToSQLite(IOUtils.combinePath(NCore.getContext().getFilesDir().getAbsolutePath(), "BiometricsV50.db"));
		mClient.setUseDeviceManager(true);
		mClient.setMatchingWithDetails(true);
		mClient.setProperty("Faces.IcaoUnnaturalSkinToneThreshold", 10);
		mClient.setProperty("Faces.IcaoSkinReflectionThreshold", 10);
//		mConnection = new NClusterBiometricConnection();
//		mConnection.setHost("192.168.1.7");
//		mConnection.setAdminPort(Integer.parseInt("24932"));
//		mClient.getRemoteConnections().add(mConnection);
		mClient.initialize();
		mSubjects = new NSubject[]{};
		mSubject = new NSubject();
	}

	// ===========================================================
	// Public methods
	// ===========================================================

	public NBiometricClient getClient() {
		return mClient;
	}

	public NSubject getSubject() {
		return mSubject;
	}

	/**
	 * Subjects contain copy of subject list from biometric client
	 * so that list could be accessible while continuous tasks are being
	 * performed on biometric client like capturing from camera
	 */
	public NSubject[] getSubjects() {
		return mSubjects;
	}

	/**
	 * Subjects contain copy of subject list from biometric client
	 * so that list could be accessible while continuous tasks are being
	 * performed on biometric client like capturing from camera
	 */
	public void setSubjects(NSubject[] subjects) {
		this.mSubjects = subjects;
	}

	public void update() {
//		FingerPreferences.updateClient(mClient, NCore.getContext());
		FacePreferences.updateClient(mClient, NCore.getContext());
//		VoicePreferences.updateClient(mClient, NCore.getContext());
//		IrisPreferences.updateClient(mClient, NCore.getContext());
	}
}
