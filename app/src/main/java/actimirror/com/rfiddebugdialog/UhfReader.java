package actimirror.com.rfiddebugdialog;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by james on 9/10/15.
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class UhfReader implements CommendManager {
    private static NewSendCommendManager manager;
    private static FTD2XXManager ftd2XXManager = null;
    private static int port = 13;
    private static int baudRate = 115200;
//    private static InputStream in;
//    private static OutputStream os;
    private static UhfReader reader;

    private UhfReader() {
    }

    public static UhfReader getInstance(Context context) {
        if(ftd2XXManager == null) {
            ftd2XXManager = new FTD2XXManager(context , 115200 , (byte)1 , (byte)8 , (byte)0 , (byte)0);
        }
//        if(serialPort == null) {
//            try {
//                serialPort = new SerialPort(port, baudRate, 0);
//            } catch (Exception var1) {
//                return null;
//            }
//
//            serialPort.psampoweron();
//            in = serialPort.getInputStream();
//            os = serialPort.getOutputStream();
//        }

        if(manager == null) {
            manager = new NewSendCommendManager(ftd2XXManager);
        }

        if(reader == null) {
            reader = new UhfReader();
        }

        return reader;
    }

    public void powerOn() {
//        serialPort.psampoweron();
    }

    public void powerOff() {
//        serialPort.psampoweroff();
    }

    public boolean setBaudrate() {
        return manager.setBaudrate();
    }

    public byte[] getFirmware() {
        return manager.getFirmware();
    }

    public boolean setOutputPower(int value) {
        return manager.setOutputPower(value);
    }

    public List<byte[]> inventoryRealTime() {
        return manager.inventoryRealTime();
    }

    public void selectEPC(byte[] epc) {
        manager.selectEPC(epc);
    }

    public byte[] readFrom6C(int memBank, int startAddr, int length, byte[] accessPassword) {
        return manager.readFrom6C(memBank, startAddr, length, accessPassword);
    }

    public boolean writeTo6C(byte[] password, int memBank, int startAddr, int dataLen, byte[] data) {
        return manager.writeTo6C(password, memBank, startAddr, dataLen, data);
    }

    public void setSensitivity(int value) {
        manager.setSensitivity(value);
    }

    public boolean lock6C(byte[] password, int memBank, int lockType) {
        return manager.lock6C(password, memBank, lockType);
    }

    public void close() {
        if(manager != null) {
            manager.close();
            manager = null;
        }

//        if(serialPort != null) {
//            serialPort.psampoweroff();
//            serialPort.close(port);
//            serialPort = null;
//        }

        if(reader != null) {
            reader = null;
        }

    }

    public byte checkSum(byte[] data) {
        return (byte)0;
    }

    public int setFrequency(int startFrequency, int freqSpace, int freqQuality) {
        return manager.setFrequency(startFrequency, freqSpace, freqQuality);
    }

    public void setDistance(int distance) {
    }

    public void close(InputStream input, OutputStream output) {
        if(manager != null) {
            manager = null;

            try {
                input.close();
                output.close();
            } catch (IOException var4) {
                var4.printStackTrace();
            }
        }

    }

    public int setWorkArea(int area) {
        return manager.setWorkArea(area);
    }

    public List<byte[]> inventoryMulti() {
        return manager.inventoryMulti();
    }

    public void stopInventoryMulti() {
        manager.stopInventoryMulti();
    }

    public int getFrequency() {
        return manager.getFrequency();
    }

    public int unSelect() {
        return manager.unSelectEPC();
    }

    public void setRecvParam(int mixer_g, int if_g, int trd) {
        manager.setRecvParam(mixer_g, if_g, trd);
    }
}