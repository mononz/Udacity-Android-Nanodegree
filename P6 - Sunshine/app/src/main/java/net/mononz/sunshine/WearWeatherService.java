package net.mononz.sunshine;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.WearableListenerService;

import net.mononz.sunshine.sync.SunshineSyncAdapter;

public class WearWeatherService extends WearableListenerService {

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                String path = dataEvent.getDataItem().getUri().getPath();
                if (path.equals(SunshineSyncAdapter.ROUTE_FROM_WEAR)) {
                    SunshineSyncAdapter.syncImmediately(this);
                }
            }
        }
    }
}
