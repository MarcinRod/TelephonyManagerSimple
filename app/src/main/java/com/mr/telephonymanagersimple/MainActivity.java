package com.mr.telephonymanagersimple;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NetworkListener.NetworkStateChangedListener {
	TelephonyManager telephonyManager;
	private int MY_PERMISSION_REQUEST_ACCESS = 101;
	View rootView;
	ListView cellInfoListView;
	TextView signalStrengthTextView;

	List<CellInfo> cellInfoList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Prepare the layout variables
		setContentView(R.layout.activity_main);
		rootView = findViewById(R.id.rootView);
		cellInfoListView = findViewById(R.id.cellInfoListView);
		signalStrengthTextView = findViewById(R.id.signalStrengthTextView);

		// Check the required permissions
		if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
				ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// Request the missing permissions
			requestPermission();
			return;
		}
		// Initialize TelephonyManager when permissions are granted
		initTelephonyManager();

	}
	protected void requestPermission() {
		ActivityCompat.requestPermissions(this,
				new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_REQUEST_ACCESS);
	}
	@SuppressLint("MissingPermission")
	private void initTelephonyManager() {
		// Get the TelephonyManager instance
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		// Get the NetworkListener instance.
		// Network listener is used to receive updates from the telephonyManager
		NetworkListener networkListener = NetworkListener.getInstance(this);
		// Specify the recipient of the updates from NetworkListener.
		// In this case MainActivity is the listener of the updates
		networkListener.addListener(this);

		// Attach the networkListener to the telephonyManager.
		// The flags PhoneStateListener.LISTEN_CELL_INFO and PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
		// indicates that the listener will only receive updates about changes in cell info and signal strengths
		telephonyManager.listen(networkListener,
				PhoneStateListener.LISTEN_CELL_INFO | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		//Display the current cellInfo from the telephony manager in the cellInfoList View.
	    cellInfoList = telephonyManager.getAllCellInfo();
		cellInfoListView.setAdapter(new ArrayAdapter<CellInfo>(this,android.R.layout.simple_list_item_1,android.R.id.text1,cellInfoList));
	}
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if(requestCode == MY_PERMISSION_REQUEST_ACCESS){
			// If request is cancelled, the result arrays are empty.
			if (grantResults.length > 1
					&& grantResults[0] == PackageManager.PERMISSION_GRANTED
					&& grantResults[1] == PackageManager.PERMISSION_GRANTED ) {
				// permission was granted! Initialize TelephonyManager
				initTelephonyManager();
			} else {
				// permission denied
				Snackbar.make(rootView,"Permission is required to continue",Snackbar.LENGTH_LONG)
						.setAction("RETRY", new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								// Retry permission request
								requestPermission();
							}
						}).show();
			}
		}
	}

	@Override
	public void cellInfoChangedHandler(List<CellInfo> cellInfo) {
		// Method called when a CellInfo update is received by the NetworkListener
		Snackbar.make(rootView,"Cell info changed!",Snackbar.LENGTH_SHORT).show();
		//Update the displayed list
		cellInfoList = cellInfo;
		((ArrayAdapter)cellInfoListView.getAdapter()).notifyDataSetChanged();
	}

	@Override
	public void signalStrengthsChangedHandler(SignalStrength signalStrength) {
		// Method called when a signal strength update is received by the NetworkListener
		Snackbar.make(rootView,"Signal strength changed!",Snackbar.LENGTH_SHORT).show();

		Locale currentLocale = Locale.getDefault();

		// Get LTE related info. The getLTEparameters method is needed since SignalStrength
		// does not directly give access to LTE parameters. The getLTEparameters method will return
		// a value of 0xffffff if the parameter query is not correct
		int lteSignalStrength = getLTEparameters(signalStrength,"getLteSignalStrength"); // get signal strength
		int lteCqi = getLTEparameters(signalStrength,"getLteCqi"); // get Channel Quality Indicator value
		int lteRsrp = getLTEparameters(signalStrength,"getLteRsrp"); // Get Reference signal received power value
		int lteRssnr = getLTEparameters(signalStrength,"getLteRssnr"); // Get Reference signal Signal to Noise ratio
		// Create a string that holds LTE related information
		String infoLte = String.format(currentLocale,"LTE info:\nSignal Strength: %d [dBm] CQI: %d RSRP: %d [dBm] RSSNR: %d\n",
				lteSignalStrength,
				lteCqi,
				lteRsrp,
				lteRssnr);

		// Get GSM related info:
		int gsmSignalStrength = signalStrength.getGsmSignalStrength(); // get GSM signal strength
		int gsmBitErrorRate = signalStrength.getGsmBitErrorRate(); // Get GSM BER
		// Create a string that holds GSM related information
		String infoGSM = String.format(currentLocale,"GSM info:\nSignal Strength: %d [dBm] BER: %d\n",
				gsmSignalStrength,
				gsmBitErrorRate);

		// Get CDMA related info:
		int cdmaDbm = signalStrength.getCdmaDbm(); // get CDMA signal strength
		int cdmaEcIo = signalStrength.getCdmaEcio();// get CDMA Ec/Ic value (quality indicator)
		// Create a string that holds CDMA related information
		String infoCDMA = String.format(currentLocale,"CDMA info:\nSignal Strength: %d [dBm] Ec/Io: %d\n",
				cdmaDbm,
				cdmaEcIo);
		//Display all information in the signalStrengthTextView
		signalStrengthTextView.setText(infoLte + infoGSM + infoCDMA);
	}
	private int getLTEparameters(SignalStrength signalStrength, String parameterName)
	{
		// Valid LTE parameters:
		// - getLteAsuLevel
		// - getLteCqi
		// - getLteDbm
		// - getLteLevel
		// - getLteRsrp
		// - getLteRsrq
		// - getLteRssnr
		// - getLteSignalStrength
		if(!parameterName.contains("Lte")) {
			// Invalid parameter name
			return 0xffffff;
		}

		try
		{
			Method[] methods = android.telephony.SignalStrength.class.getMethods();

			for (Method mthd : methods)
			{
				Log.i(this.getClass().getSimpleName() +"LTE methods: ", mthd.getName());
				// Find the method to retrieve the desired parameter by its name
				if (mthd.getName().equals(parameterName))
				{
					// The correct method has been found - return the value of the required parameter
					return  (int) mthd.invoke(signalStrength, new Object[]{});
				}
			}
		}
		catch (Exception e)
		{
			Log.e(this.getClass().getSimpleName(), "Exception: " + e.toString());
		}
		// The method was not found in the SignalStrength class - return an arbitrary value
		return 0xffffff;
	}
}
