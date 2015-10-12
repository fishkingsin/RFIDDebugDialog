package actimirror.com.rfiddebugdialog;

import actimirror.com.rfiddebugdialog.UhfReader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScreenStateReceiver extends BroadcastReceiver {

	private UhfReader reader ;
	@Override
	public void onReceive(Context context, Intent intent) {
		reader = UhfReader.getInstance(context);
		//ÆÁÁÁ
//		if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
//			reader.powerOn();
//			Log.i("ScreenStateReceiver", "screen on");
//			
//		}//ÆÁÃð
//		else if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
//			reader.powerOff();
//			Log.i("ScreenStateReceiver", "screen off");
//		}

	}

}
