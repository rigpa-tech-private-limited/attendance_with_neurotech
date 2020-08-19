package co.vaango.attendance.multibiometric.multimodal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;

import com.neurotec.biometrics.NBiometricCaptureOption;
import com.neurotec.biometrics.NBiometricOperation;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.biometrics.NERecord;
import com.neurotec.biometrics.NETemplate;
import com.neurotec.biometrics.NFRecord;
import com.neurotec.biometrics.NFTemplate;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NLRecord;
import com.neurotec.biometrics.NLTemplate;
import com.neurotec.biometrics.NMatchingResult;
import com.neurotec.biometrics.NSRecord;
import com.neurotec.biometrics.NSTemplate;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.NTemplate;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.standards.BDIFStandard;
import com.neurotec.biometrics.standards.FCRFaceImage;
import com.neurotec.biometrics.standards.FCRecord;
import com.neurotec.biometrics.view.NFaceView;
import com.neurotec.devices.NCamera;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceType;
import com.neurotec.images.NImage;
import com.neurotec.io.NBuffer;
import com.neurotec.media.NMediaFormat;

import co.vaango.attendance.licensing.LicensingManager;
import co.vaango.attendance.multibiometric.Model;
import co.vaango.attendance.R;
import co.vaango.attendance.multibiometric.preferences.FacePreferences;
import co.vaango.attendance.multibiometric.view.CameraControlsView;
import co.vaango.attendance.multibiometric.view.CameraFormatFragment;
import co.vaango.attendance.util.IOUtils;
import co.vaango.attendance.util.NImageUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public final class FaceActivity extends BiometricActivity implements CameraControlsView.CameraControlsListener, CameraFormatFragment.CameraFormatSelectionListener {

    private enum Status {
        CAPTURING,
        OPENING_FILE,
        TEMPLATE_CREATED
    }

    // ===========================================================
    // Private static fields
    // ===========================================================

    private static final String TAG = "scheck";
//    private static final String LICENSING_STATE = "licensing_state";
    private static final String MODALITY_ASSET_DIRECTORY = "faces";

    // ===========================================================
    // Private fields
    // ===========================================================

    private NFaceView mFaceView;
    private CameraControlsView controlsView;

    private boolean mLicensesObtained = false;
    private Status mStatus = Status.CAPTURING;

    // ===========================================================
    // Private methods
    // ===========================================================

    private void startCapturing() {
        Log.d(TAG, "Face startCapturing...");
        NSubject subject = new NSubject();
        NFace face = new NFace();
        face.addPropertyChangeListener(biometricPropertyChanged);
        EnumSet<NBiometricCaptureOption> options = EnumSet.of(NBiometricCaptureOption.STREAM);
        if (FacePreferences.isShowIcaoWarnings(this) || FacePreferences.isShowIcaoTextWarnings(this)) {
//            Log.d(TAG, "Face startCapturing...isShowIcaoWarnings...isShowIcaoTextWarnings");
            options.add(NBiometricCaptureOption.STREAM);
            mFaceView.setShowIcaoArrows(FacePreferences.isShowIcaoWarnings(this));
            mFaceView.setShowIcaoTextWarnings(FacePreferences.isShowIcaoTextWarnings(this));
        }
//        Log.d(TAG, "Face startCapturing isUseLiveness..." + FacePreferences.isUseLiveness(this));
        if (!FacePreferences.isUseLiveness(this)) {
            if (!options.contains(NBiometricCaptureOption.STREAM)) {
                options.add(NBiometricCaptureOption.STREAM);
            }
            for (NDevice device : client.getDeviceManager().getDevices()) {
//                Log.d(TAG, "Face startCapturing device..." + device);
                if (device.getDeviceType().contains(NDeviceType.CAMERA)) {
//                    Log.d(TAG, "Face startCapturing device Front..." + ((NCamera) device).getDisplayName().contains("Front"));
                    if (((NCamera) device).getDisplayName().contains("Front")) {
                        Log.d(TAG, "Face startCapturing client.getFaceCaptureDevice()..." + client.getFaceCaptureDevice());
                        if (!client.getFaceCaptureDevice().equals((NCamera) device)) {
                            client.setFaceCaptureDevice((NCamera) device);
                        }
                    }
                }
            }
        }
        face.setCaptureOptions(options);
        Log.d(TAG, "startCapturing face..." + face);
//        Log.d(TAG, "Face isShowFaceRectangle..." + mFaceView.isShowFaceRectangle());
        mFaceView.setShowFaceRectangle(true);
        mFaceView.setFaceRectangleWidth(1);
        mFaceView.setShowBaseFeaturePoints(false);
        mFaceView.setShowEyes(false);
        mFaceView.setFace(face);
        subject.getFaces().add(face);
        Log.d(TAG, "startCapturing subject..." + subject);
        isDetectStarted = false;
//		Log.d(TAG,"Base64 Face..."+ Base64.encodeToString(subject.getFaces().get(0).getImage().save().toByteArray(), Base64.NO_WRAP));
        capture(subject, (FacePreferences.isShowIcaoWarnings(this) || FacePreferences.isShowIcaoTextWarnings(this)) ? EnumSet.of(NBiometricOperation.ASSESS_QUALITY) : null);

    }

    private void setCameraControlsVisible(final boolean value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                controlsView.setVisibility(value ? View.VISIBLE : View.GONE);
            }
        });
    }

    private NSubject createSubjectFromImage(Uri uri) {
        NSubject subject = null;
        try {
            NImage image = NImageUtils.fromUri(this, uri);
            subject = new NSubject();
            NFace face = new NFace();
            face.setImage(image);
            subject.getFaces().add(face);
        } catch (Exception e) {
            Log.i(TAG, "Failed to load file as NImage");
        }
        return subject;
    }

    private NSubject createSubjectFromFCRecord(Uri uri) {
        NSubject subject = null;
        try {
            FCRecord fcRecord = new FCRecord(IOUtils.toByteBuffer(this, uri), BDIFStandard.ISO);
            subject = new NSubject();
            for (FCRFaceImage img : fcRecord.getFaceImages()) {
                NFace face = new NFace();
                face.setImage(img.toNImage());
                subject.getFaces().add(face);
            }
        } catch (Throwable th) {
            Log.i(TAG, "Failed to load file as FCRecord");
        }
        return subject;
    }

    private NSubject createSubjectFromFile(Uri uri) {
        NSubject subject = null;
        try {
            subject = NSubject.fromMemory(IOUtils.toByteBuffer(this, uri));
        } catch (IOException e) {
            Log.i(TAG, "Failed to load from file");
        }
        return subject;
    }

    // ===========================================================
    // Protected methods
    // ===========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Face OnCreate");
        super.onCreate(savedInstanceState);
        try {
            PreferenceManager.setDefaultValues(this, R.xml.face_preferences, false);
            LinearLayout layout = (LinearLayout) findViewById(R.id.multimodal_biometric_layout);

            controlsView = new CameraControlsView(this, this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

            controlsView.setLayoutParams(params);
            //layout.addView(controlsView);

            mFaceView = new NFaceView(this);
            mFaceView.setShowAge(true);
            layout.addView(mFaceView);
        } catch (Exception e) {
            showError(e);
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "Face onResume ==" + mLicensesObtained + "--" + mStatus);
        super.onResume();
        if (mLicensesObtained && mStatus == Status.CAPTURING) {
            Log.d(TAG, "startCapturing called ==" + mLicensesObtained + "--" + mStatus);
            startCapturing();
        }
    }

    @Override
    protected List<String> getAdditionalComponents() {
        return additionalComponents();
    }

    @Override
    protected List<String> getMandatoryComponents() {
        return mandatoryComponents();
    }

    @Override
    protected Class<?> getPreferences() {
        return FacePreferences.class;
    }

    @Override
    protected void updatePreferences(NBiometricClient client) {
        FacePreferences.updateClient(client, this);
    }

    @Override
    protected boolean isCheckForDuplicates() {
        return FacePreferences.isCheckForDuplicates(this);
    }

    @Override
    protected String getModalityAssetDirectory() {
        return MODALITY_ASSET_DIRECTORY;
    }

    @Override
    protected void onLicensesObtained() {
        Log.d(TAG, "onLicensesObtained called ==" + mLicensesObtained + "--" + mStatus);
        mLicensesObtained = true;
        startCapturing();
    }

    protected void onStartCapturing() {
        stop();
    }

    @Override
    protected void onFileSelected(Uri uri) throws Exception {
        mStatus = Status.OPENING_FILE;

        NSubject subject = createSubjectFromImage(uri);

        if (subject == null) {
            subject = createSubjectFromFCRecord(uri);
        }

        if (subject == null) {
            subject = createSubjectFromFile(uri);
        }

        if (subject != null) {
            if (!subject.getFaces().isEmpty()) {
                mFaceView.setFace(subject.getFaces().get(0));
            }
            extract(subject);
        } else {
            mStatus = Status.CAPTURING;
            showInfo("File did not contain valid information for subject");
        }
    }

    @Override
    protected void onOperationStarted(NBiometricOperation operation) {
        super.onOperationStarted(operation);
        if (operation == NBiometricOperation.CAPTURE) {
            mStatus = Status.CAPTURING;
            setCameraControlsVisible(true);
        }
    }

    @Override
    protected void onOperationCompleted(NBiometricOperation operation, NBiometricTask task) {
        super.onOperationCompleted(operation, task);
        if (task != null && task.getStatus() == NBiometricStatus.OK && operation == NBiometricOperation.CREATE_TEMPLATE) {
            mStatus = Status.TEMPLATE_CREATED;
            setCameraControlsVisible(false);
        }

        if (task == null || (operation == NBiometricOperation.CREATE_TEMPLATE
                && task.getStatus() != NBiometricStatus.OK
                && task.getStatus() != NBiometricStatus.CANCELED
                && task.getStatus() != NBiometricStatus.OPERATION_NOT_ACTIVATED)) {
            if (!mAppIsGoingToBackground) {
                startCapturing();
            }
        }
    }

    @Override
    protected boolean isStopSupported() {
        Log.d(TAG,"isStopSupported");
        return false;
    }

    // ===========================================================
    // 	Public methods
    // ===========================================================

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_FOCUS || keyCode == KeyEvent.KEYCODE_CAMERA) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Log.d(TAG,"onTouchEvent");
            return true;
        }
        return false;
    }

    @Override
    public void onCameraFormatSelected(final NMediaFormat format) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NCamera camera = Model.getInstance().getClient().getFaceCaptureDevice();
                if (camera != null) {
                    camera.setCurrentFormat(format);
                }

            }
        }).start();
    }

    @Override
    public void onSwitchCamera() {
        if (!FacePreferences.isUseLiveness(this)) {
            NCamera currentCamera = client.getFaceCaptureDevice();
            for (NDevice device : client.getDeviceManager().getDevices()) {
                if (device.getDeviceType().contains(NDeviceType.CAMERA)) {
                    if (!device.equals(currentCamera) && currentCamera.isCapturing()) {
                        cancel();
                        client.setFaceCaptureDevice((NCamera) device);
                        startCapturing();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onChangeFormat() {
        CameraFormatFragment.newInstance().show(getFragmentManager(), "formats");
    }

    public static List<String> mandatoryComponents() {
        return Arrays.asList(LicensingManager.LICENSE_DEVICES_CAMERAS,
                LicensingManager.LICENSE_FACE_DETECTION,
                LicensingManager.LICENSE_FACE_EXTRACTION,
                LicensingManager.LICENSE_FACE_MATCHING);
    }

    public static List<String> additionalComponents() {
        return Arrays.asList(LicensingManager.LICENSE_FACE_STANDARDS,
                LicensingManager.LICENSE_FACE_MATCHING_FAST,
                LicensingManager.LICENSE_FACE_SEGMENTS_DETECTION);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MultiModalActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
