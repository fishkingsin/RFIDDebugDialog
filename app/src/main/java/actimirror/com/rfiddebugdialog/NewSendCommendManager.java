package actimirror.com.rfiddebugdialog;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class NewSendCommendManager implements CommendManager {
//    private InputStream in;
//    private OutputStream out;
    private FTD2XXManager ftd2XXManager;
    private final byte HEAD = -69;
    private final byte END = 126;
    public static final byte RESPONSE_OK = 0;
    public static final byte ERROR_CODE_ACCESS_FAIL = 22;
    public static final byte ERROR_CODE_NO_CARD = 9;
    public static final byte ERROR_CODE_READ_SA_OR_LEN_ERROR = -93;
    public static final byte ERROR_CODE_WRITE_SA_OR_LEN_ERROR = -77;
    public static final int SENSITIVE_HIHG = 3;
    public static final int SENSITIVE_MIDDLE = 2;
    public static final int SENSITIVE_LOW = 1;
    public static final int SENSITIVE_VERY_LOW = 0;
    private byte[] selectEPC = null;

    public NewSendCommendManager(FTD2XXManager _ftd2XXManager) {
//        this.in = serialPortInput;
//        this.out = serialportOutput;
        this.ftd2XXManager = _ftd2XXManager;
    }

    private void sendCMD(byte[] cmd) {
//        try {
            this.ftd2XXManager.sendData(cmd.length,cmd);
//            this.out.write(cmd);
//            this.out.flush();
//        } catch (IOException var3) {
//            var3.printStackTrace();
//        }

    }

    public boolean setBaudrate() {
        byte[] cmd = new byte[0];
        return false;
    }

    public byte[] getFirmware() {
        byte[] cmd = new byte[]{(byte)-69, (byte)0, (byte)3, (byte)0, (byte)1, (byte)0, (byte)4, (byte)126};
        byte[] version = null;
        this.sendCMD(cmd);
        byte[] response = this.read();
        if(response != null) {
            byte[] resolve = this.handlerResponse(response);
            if(resolve != null && resolve.length > 1) {
                version = new byte[resolve.length - 1];
                System.arraycopy(resolve, 1, version, 0, resolve.length - 1);
            }
        }

        return version;
    }

    public void setSensitivity(int value) {
        byte[] cmd = new byte[]{(byte)-69, (byte)0, (byte)-16, (byte)0, (byte)4, (byte)2, (byte)6, (byte)0, (byte)-96, (byte)-100, (byte)126};
        cmd[5] = (byte)value;
        cmd[cmd.length - 2] = this.checkSum(cmd);
        this.sendCMD(cmd);
        byte[] response = this.read();
        if(response != null) {
            Log.e("setSensitivity ", Tools.Bytes2HexString(response, response.length));
        }

    }

    private byte[] read() {
        Object responseData = null;
        byte[] response = null;
        int available = 0;
        int index = 0;
        int headIndex = 0;

        try {
            while(index < 10) {
                Thread.sleep(50L);
                available =  this.ftd2XXManager.iavailable;
//                available = this.in.available();
                if(available > 7) {
                    break;
                }

                ++index;
            }

            if(available > 0) {
                byte[] var8 = new byte[available];
//                this.in.read(var8);
                this.ftd2XXManager.readData(available,var8);

                for(int e1 = 0; e1 < available; ++e1) {
                    if(var8[e1] == -69) {
                        headIndex = e1;
                        break;
                    }
                }

                response = new byte[available - headIndex];
                System.arraycopy(var8, headIndex, response, 0, response.length);
            }
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return response;
    }

    public boolean setOutputPower(int value) {
        boolean mixer = true;
        boolean if_g = true;
        boolean trd = true;
        byte mixer1;
        byte if_g1;
        short trd1;
        switch(value) {
            case 16:
                mixer1 = 1;
                if_g1 = 1;
                trd1 = 432;
                break;
            case 17:
                mixer1 = 1;
                if_g1 = 3;
                trd1 = 432;
                break;
            case 18:
                mixer1 = 2;
                if_g1 = 4;
                trd1 = 432;
                break;
            case 19:
                mixer = true;
                if_g = true;
                trd = true;
            case 20:
                mixer = true;
                if_g = true;
                trd = true;
            case 21:
                mixer1 = 2;
                if_g1 = 6;
                trd1 = 560;
                break;
            case 22:
                mixer1 = 3;
                if_g1 = 6;
                trd1 = 624;
                break;
            case 23:
                mixer1 = 4;
                if_g1 = 6;
                trd1 = 624;
                break;
            default:
                mixer1 = 6;
                if_g1 = 7;
                trd1 = 624;
        }

        return this.setRecvParam(mixer1, if_g1, trd1);
    }

    public boolean setRecvParam(int mixer_g, int if_g, int trd) {
        byte[] cmd = new byte[]{(byte)-69, (byte)0, (byte)-16, (byte)0, (byte)4, (byte)3, (byte)6, (byte)1, (byte)-80, (byte)-82, (byte)126};
        Object recv = null;
        Object content = null;
        cmd[5] = (byte)mixer_g;
        cmd[6] = (byte)if_g;
        cmd[7] = (byte)(trd / 256);
        cmd[8] = (byte)(trd % 256);
        cmd[9] = this.checkSum(cmd);
        this.sendCMD(cmd);
        byte[] recv1 = this.read();
        if(recv1 != null) {
            byte[] content1 = this.handlerResponse(recv1);
            if(content1 != null) {
                return true;
            }
        }

        return false;
    }

    public List<byte[]> inventoryMulti() {
        this.unSelectEPC();
        ArrayList list = new ArrayList();
        byte[] cmd = new byte[]{(byte)-69, (byte)0, (byte)39, (byte)0, (byte)3, (byte)34, (byte)39, (byte)16, (byte)-125, (byte)126};
        this.sendCMD(cmd);
        byte[] response = this.read();
        if(response != null) {
            int responseLength = response.length;
            int start = 0;
            if(responseLength > 15) {
                while(responseLength > 5) {
                    int paraLen = response[start + 4] & 255;
                    int singleCardLen = paraLen + 7;
                    if(singleCardLen > responseLength) {
                        break;
                    }

                    byte[] sigleCard = new byte[singleCardLen];
                    System.arraycopy(response, start, sigleCard, 0, singleCardLen);
                    byte[] resolve = this.handlerResponse(sigleCard);
                    if(resolve != null && paraLen > 5) {
                        byte[] epcBytes = new byte[paraLen - 5];
                        System.arraycopy(resolve, 4, epcBytes, 0, paraLen - 5);
                        list.add(epcBytes);
                    }

                    start += singleCardLen;
                    responseLength -= singleCardLen;
                }
            } else {
                this.handlerResponse(response);
            }
        }

        return list;
    }

    public void stopInventoryMulti() {
        byte[] cmd = new byte[]{(byte)-69, (byte)0, (byte)40, (byte)0, (byte)0, (byte)40, (byte)126};
        this.sendCMD(cmd);
        byte[] recv = this.read();
    }

    public List<byte[]> inventoryRealTime() {
        this.unSelectEPC();
        byte[] cmd = new byte[]{(byte)-69, (byte)0, (byte)34, (byte)0, (byte)0, (byte)34, (byte)126};
        this.sendCMD(cmd);
        ArrayList list = new ArrayList();
        byte[] response = this.read();
        if(response != null) {
            int responseLength = response.length;
            int start = 0;
            if(responseLength > 15) {
                while(responseLength > 5) {
                    int paraLen = response[start + 4] & 255;
                    int singleCardLen = paraLen + 7;
                    if(singleCardLen > responseLength) {
                        break;
                    }

                    byte[] sigleCard = new byte[singleCardLen];
                    System.arraycopy(response, start, sigleCard, 0, singleCardLen);
                    byte[] resolve = this.handlerResponse(sigleCard);
                    if(resolve != null && paraLen > 5) {
                        byte[] epcBytes = new byte[paraLen - 5];
                        System.arraycopy(resolve, 4, epcBytes, 0, paraLen - 5);
                        list.add(epcBytes);
                    }

                    start += singleCardLen;
                    responseLength -= singleCardLen;
                }
            } else {
                this.handlerResponse(response);
            }
        }

        return list;
    }

    public void selectEPC(byte[] epc) {
        byte[] cmd = new byte[]{(byte)-69, (byte)0, (byte)18, (byte)0, (byte)1, (byte)0, (byte)19, (byte)126};
        this.selectEPC = epc;
        this.sendCMD(cmd);
        byte[] response = this.read();
    }

    public int unSelectEPC() {
        byte[] cmd = new byte[]{(byte)-69, (byte)0, (byte)18, (byte)0, (byte)1, (byte)1, (byte)20, (byte)126};
        this.sendCMD(cmd);
        byte[] response = this.read();
        return 0;
    }

    private void setSelectPara() {
        byte[] cmd = new byte[]{(byte)-69, (byte)0, (byte)12, (byte)0, (byte)19, (byte)1, (byte)0, (byte)0, (byte)0, (byte)32, (byte)96, (byte)0, (byte)1, (byte)97, (byte)5, (byte)-72, (byte)3, (byte)72, (byte)12, (byte)-48, (byte)0, (byte)3, (byte)-47, (byte)-98, (byte)88, (byte)126};
        if(this.selectEPC != null) {
            Log.e("", "select epc");
            System.arraycopy(this.selectEPC, 0, cmd, 12, this.selectEPC.length);
            cmd[cmd.length - 2] = this.checkSum(cmd);
            this.sendCMD(cmd);
            byte[] var2 = this.read();
        }

    }

    public byte[] readFrom6C(int memBank, int startAddr, int length, byte[] accessPassword) {
        this.setSelectPara();
        byte[] cmd = new byte[]{(byte)-69, (byte)0, (byte)57, (byte)0, (byte)9, (byte)0, (byte)0, (byte)0, (byte)0, (byte)3, (byte)0, (byte)0, (byte)0, (byte)8, (byte)77, (byte)126};
        byte[] data = null;
        if(accessPassword != null && accessPassword.length == 4) {
            System.arraycopy(accessPassword, 0, cmd, 5, 4);
            cmd[9] = (byte)memBank;
            int response;
            int resolve;
            if(startAddr <= 255) {
                cmd[10] = 0;
                cmd[11] = (byte)startAddr;
            } else {
                response = startAddr / 256;
                resolve = startAddr % 256;
                cmd[10] = (byte)response;
                cmd[11] = (byte)resolve;
            }

            if(length <= 255) {
                cmd[12] = 0;
                cmd[13] = (byte)length;
            } else {
                response = length / 256;
                resolve = length % 256;
                cmd[12] = (byte)response;
                cmd[13] = (byte)resolve;
            }

            cmd[14] = this.checkSum(cmd);
            this.sendCMD(cmd);
            byte[] response1 = this.read();
            if(response1 != null) {
                Log.e("readFrom6c response", Tools.Bytes2HexString(response1, response1.length));
                byte[] resolve1 = this.handlerResponse(response1);
                if(resolve1 != null) {
                    Log.e("readFrom6c resolve", Tools.Bytes2HexString(resolve1, resolve1.length));
                    if(resolve1[0] == 57) {
                        int lengData = resolve1.length - resolve1[1] - 2;
                        data = new byte[lengData];
                        System.arraycopy(resolve1, resolve1[1] + 2, data, 0, lengData);
                    } else {
                        data = new byte[]{resolve1[1]};
                    }
                }
            }

            return data;
        } else {
            return null;
        }
    }

    public boolean writeTo6C(byte[] password, int memBank, int startAddr, int dataLen, byte[] data) {
        this.setSelectPara();
        if(password != null && password.length == 4) {
            boolean writeFlag = false;
            int cmdLen = 16 + data.length;
            int parameterLen = 9 + data.length;
            byte[] cmd = new byte[cmdLen];
            cmd[0] = -69;
            cmd[1] = 0;
            cmd[2] = 73;
            int response;
            int resolve;
            if(parameterLen < 256) {
                cmd[3] = 0;
                cmd[4] = (byte)parameterLen;
            } else {
                response = parameterLen / 256;
                resolve = parameterLen % 256;
                cmd[3] = (byte)response;
                cmd[4] = (byte)resolve;
            }

            System.arraycopy(password, 0, cmd, 5, 4);
            cmd[9] = (byte)memBank;
            if(startAddr < 256) {
                cmd[10] = 0;
                cmd[11] = (byte)startAddr;
            } else {
                response = startAddr / 256;
                resolve = startAddr % 256;
                cmd[10] = (byte)response;
                cmd[11] = (byte)resolve;
            }

            if(dataLen < 256) {
                cmd[12] = 0;
                cmd[13] = (byte)dataLen;
            } else {
                response = dataLen / 256;
                resolve = dataLen % 256;
                cmd[12] = (byte)response;
                cmd[13] = (byte)resolve;
            }

            System.arraycopy(data, 0, cmd, 14, data.length);
            cmd[cmdLen - 2] = this.checkSum(cmd);
            cmd[cmdLen - 1] = 126;
            this.sendCMD(cmd);

            try {
                Thread.sleep(50L);
            } catch (InterruptedException var12) {
                var12.printStackTrace();
            }

            byte[] response1 = this.read();
            if(response1 != null) {
                byte[] resolve1 = this.handlerResponse(response1);
                if(resolve1 != null && resolve1[0] == 73 && resolve1[resolve1.length - 1] == 0) {
                    writeFlag = true;
                }
            }

            return writeFlag;
        } else {
            return false;
        }
    }

    public boolean lock6C(byte[] password, int memBank, int lockType) {
        return false;
    }

    public void close() {
//        try {
            ftd2XXManager.disconnectFunction();
//            this.in.close();
//            this.out.close();
//        } catch (IOException var2) {
//            var2.printStackTrace();
//        }

    }

    public byte checkSum(byte[] data) {
        byte crc = 0;

        for(int i = 1; i < data.length - 2; ++i) {
            crc += data[i];
        }

        return crc;
    }

    private byte[] handlerResponse(byte[] response) {
        byte[] data = null;
        boolean crc = false;
        int responseLength = response.length;
        if(response[0] != -69) {
            Log.e("handlerResponse", "head error");
            return data;
        } else if(response[responseLength - 1] != 126) {
            Log.e("handlerResponse", "end error");
            return data;
        } else if(responseLength < 7) {
            return data;
        } else {
            int lengthHigh = response[3] & 255;
            int lengthLow = response[4] & 255;
            int dataLength = lengthHigh * 256 + lengthLow;
            byte crc1 = this.checkSum(response);
            if(crc1 != response[responseLength - 2]) {
                Log.e("handlerResponse", "crc error");
                return data;
            } else {
                if(dataLength != 0 && responseLength == dataLength + 7) {
                    Log.e("handlerResponse", "response right");
                    data = new byte[dataLength + 1];
                    data[0] = response[2];
                    System.arraycopy(response, 5, data, 1, dataLength);
                }

                return data;
            }
        }
    }

    public int setFrequency(int startFrequency, int freqSpace, int freqQuality) {
        int frequency = 1;
        if(startFrequency > 840125 && startFrequency < 844875) {
            frequency = (startFrequency - 840125) / 250;
        } else if(startFrequency > 920125 && startFrequency < 924875) {
            frequency = (startFrequency - 920125) / 250;
        } else if(startFrequency > 865100 && startFrequency < 867900) {
            frequency = (startFrequency - 865100) / 200;
        } else if(startFrequency > 902250 && startFrequency < 927750) {
            frequency = (startFrequency - 902250) / 500;
        }

        byte[] cmd = new byte[]{(byte)-69, (byte)0, (byte)-85, (byte)0, (byte)1, (byte)4, (byte)-80, (byte)126};
        cmd[5] = (byte)frequency;
        cmd[6] = this.checkSum(cmd);
        this.sendCMD(cmd);
        byte[] recv = this.read();
        if(recv != null) {
            Log.e("frequency", Tools.Bytes2HexString(recv, recv.length));
        }

        return 0;
    }

    public int setWorkArea(int area) {
        byte[] cmd = new byte[]{(byte)-69, (byte)0, (byte)7, (byte)0, (byte)1, (byte)1, (byte)9, (byte)126};
        cmd[5] = (byte)area;
        cmd[6] = this.checkSum(cmd);
        this.sendCMD(cmd);
        byte[] recv = this.read();
        if(recv != null) {
            Log.e("setWorkArea", Tools.Bytes2HexString(recv, recv.length));
            this.handlerResponse(recv);
        }

        return 0;
    }

    public int getFrequency() {
        byte[] cmd = new byte[]{(byte)-69, (byte)0, (byte)-86, (byte)0, (byte)0, (byte)-86, (byte)126};
        this.sendCMD(cmd);
        byte[] recv = this.read();
        if(recv != null) {
            Log.e("getFrequency", Tools.Bytes2HexString(recv, recv.length));
            this.handlerResponse(recv);
        }

        return 0;
    }
}
