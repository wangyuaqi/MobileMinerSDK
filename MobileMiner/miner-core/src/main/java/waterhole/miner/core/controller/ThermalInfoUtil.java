package waterhole.miner.core.controller;

import android.text.TextUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static waterhole.miner.core.utils.LogUtils.error;

public final class ThermalInfoUtil {

    public static String batteryTemperature = "40";

    private ThermalInfoUtil() {
    }

    public static List<String> getThermalInfo() {
        String result = getThermalInfo("/system/bin/cat", "sys/class/thermal/thermal_zone0/temp");
        if (TextUtils.isEmpty(result)) {
            result = batteryTemperature;
        }
        ArrayList<String> list = new ArrayList<>();
        list.add(result);
        return list;
    }

    private static String getThermalInfo(String... args) {
        ProcessBuilder pB;
        String result = "";
        try {
            pB = new ProcessBuilder(args);
            pB.redirectErrorStream(false);
            Process process = pB.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[1024];
            while (in.read(re) != -1) {
                // System.out.println(new String(re));
                result = new String(re);
            }
            process.destroy();
            in.close();
        } catch (Exception ex) {
            error("ThermalInfoUtil|getThermalInfo: " + ex.getMessage());
        }
        return result;
    }


    public static double getCurrentTemperature() {
        double maxTemperature;
        try {
            List<String> thermalInfo = ThermalInfoUtil.getThermalInfo();
            maxTemperature = -1;
            for (String info : thermalInfo) {
                String temp = info.replaceAll("(\\d+).*", "$1").trim();
                if (TextUtils.isDigitsOnly(temp.replace(".", ""))) {
                    double dTemp = Double.parseDouble(temp);
                    if (maxTemperature < dTemp)
                        maxTemperature = dTemp;
                }
            }
            if (maxTemperature > 1000)
                maxTemperature /= 1000;
            if (maxTemperature > 100)
                maxTemperature /= 10;
        } catch (Exception e) {
            return 40;
        }
        return maxTemperature;
    }
}