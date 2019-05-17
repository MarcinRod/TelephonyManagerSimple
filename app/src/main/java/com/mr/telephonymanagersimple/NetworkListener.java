package com.mr.telephonymanagersimple;

import android.content.Context;
import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;

import java.util.List;

class NetworkListener extends PhoneStateListener {
	private static NetworkListener instance;
	Context context;
	NetworkStateChangedListener listener;
	// Default constructor of the NetworkListener
	private NetworkListener(Context context){
		this.context = context;
	}
	// Method for retrieving the single instance of the NetworkListener
	public static NetworkListener getInstance(Context context){
		if(instance == null){
			instance = new NetworkListener(context);
		}
		return instance;
	}
	// Method for adding a listener to the updates received by NetworkListener
	public void addListener(NetworkStateChangedListener listener){
		this.listener = listener;
	}
	@Override
	public void onCellInfoChanged(List<CellInfo> cellInfo) {
		// Called when a CellInfo has changed
		if(listener != null){
			// If the listener exists send the cellInfo variable to the handler
			listener.cellInfoChangedHandler(cellInfo);
		}

	}

	@Override
	public void onSignalStrengthsChanged(SignalStrength signalStrength) {
		if(listener != null) {
			// If the listener exists send the signalStrength variable to the handler
			listener.signalStrengthsChangedHandler(signalStrength);
		}
	}

	// Interface used for creating a connection between NetworkListener
	// and the Activity that is using it.
	public interface NetworkStateChangedListener{
		void cellInfoChangedHandler(List<CellInfo> cellInfo);
		void signalStrengthsChangedHandler(SignalStrength signalStrength);
	}
}
