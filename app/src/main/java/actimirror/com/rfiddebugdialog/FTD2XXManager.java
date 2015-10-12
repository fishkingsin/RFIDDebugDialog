package actimirror.com.rfiddebugdialog;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.Gravity;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Created by james on 12/10/15.
 */
public class FTD2XXManager {
    // j2xx
    public D2xxManager ftD2xx = null;
    FT_Device ftDev;
    int DevCount = -1;
    int currentPortIndex = -1;
    int portIndex = -1;

    enum DeviceStatus{
        DEV_NOT_CONNECT,
        DEV_NOT_CONFIG,
        DEV_CONFIG
    }

    boolean INTERNAL_DEBUG_TRACE = false; // Toast message for debug


    final String[] contentFormatItems = {"Character","Hexadecimal"};
    final String[] fontSizeItems = {"5","6","7","8","10","12","14","16","18","20"};
    final String[] echoSettingItems = {"On","Off"};

    // log tag
    final String TT = "Trace";
    final String TXS = "XM-Send";
    final String TXR = "XM-Rec";
    final String TYS = "YM-Send";
    final String TYR = "YM-Rec";
    final String TZS = "ZM-Send";
    final String TZR = "ZM-Rec";

    // handler event
    final int UPDATE_TEXT_VIEW_CONTENT = 0;
    final int UPDATE_SEND_FILE_STATUS = 1;
    final int UPDATE_SEND_FILE_DONE = 2;
    final int ACT_SELECT_SAVED_FILE_NAME = 3;
    final int ACT_SELECT_SAVED_FILE_FOLDER = 4;
    final int ACT_SAVED_FILE_NAME_CREATED = 5;
    final int ACT_SELECT_SEND_FILE_NAME = 6;
    final int MSG_SELECT_FOLDER_NOT_FILE = 7;
    final int MSG_XMODEM_SEND_FILE_TIMEOUT = 8;
    final int UPDATE_MODEM_RECEIVE_DATA = 9;
    final int UPDATE_MODEM_RECEIVE_DATA_BYTES = 10;
    final int UPDATE_MODEM_RECEIVE_DONE = 11;
    final int MSG_MODEM_RECEIVE_PACKET_TIMEOUT = 12;
    final int ACT_MODEM_SELECT_SAVED_FILE_FOLDER = 13;
    final int MSG_MODEM_OPEN_SAVE_FILE_FAIL = 14;
    final int MSG_YMODEM_PARSE_FIRST_PACKET_FAIL = 15;
    final int MSG_FORCE_STOP_SEND_FILE = 16;
    final int UPDATE_ASCII_RECEIVE_DATA_BYTES = 17;
    final int UPDATE_ASCII_RECEIVE_DATA_DONE = 18;
    final int MSG_FORCE_STOP_SAVE_TO_FILE = 19;
    final int UPDATE_ZMODEM_STATE_INFO = 20;
    final int ACT_ZMODEM_AUTO_START_RECEIVE = 21;

    final int MSG_SPECIAL_INFO = 98;
    final int MSG_UNHANDLED_CASE = 99;

    final byte XON = 0x11;    /* Resume transmission */
    final byte XOFF = 0x13;    /* Pause transmission */

    // strings of file transfer protocols
    final String[] protocolItems = {"ASCII","XModem-CheckSum","XModem-CRC","XModem-1KCRC","YModem","ZModem"};
    String currentProtocol;

    final int MODE_GENERAL_UART = 0;
    final int MODE_X_MODEM_CHECKSUM_RECEIVE = 1;
    final int MODE_X_MODEM_CHECKSUM_SEND = 2;
    final int MODE_X_MODEM_CRC_RECEIVE = 3;
    final int MODE_X_MODEM_CRC_SEND = 4;
    final int MODE_X_MODEM_1K_CRC_RECEIVE = 5;
    final int MODE_X_MODEM_1K_CRC_SEND = 6;
    final int MODE_Y_MODEM_1K_CRC_RECEIVE = 7;
    final int MODE_Y_MODEM_1K_CRC_SEND = 8;
    final int MODE_Z_MODEM_RECEIVE = 9;
    final int MODE_Z_MODEM_SEND = 10;
    final int MODE_SAVE_CONTENT_DATA = 11;

    int transferMode = MODE_GENERAL_UART;
    int tempTransferMode = MODE_GENERAL_UART;

    // X, Y, Z modem - UART MODE: Asynchronous�B8 data��bits�Bno parity�Bone stop��bit
    // X modem + //
    final int PACTET_SIZE_XMODEM_CHECKSUM = 132; // SOH,pkt,~ptk,128data,checksum
    final int PACTET_SIZE_XMODEM_CRC = 133;  	 // SOH,pkt,~ptk,128data,CRC-H,CRC-L
    final int PACTET_SIZE_XMODEM_1K_CRC = 1029;	 // STX,pkt,~ptk,1024data,CRC-H,CRC-L

    final byte SOH = 1;    /* Start Of Header */
    final byte STX = 2;    /* Start Of Header 1K */
    final byte EOT = 4;    /* End Of Transmission */
    final byte ACK = 6;    /* ACKnowlege */
    final byte NAK = 0x15; /* Negative AcKnowlege */
    final byte CAN = 0x18; /* Cancel */
    final byte CHAR_C = 0x43; /* Character 'C' */
    final byte CHAR_G = 0x47; /* Character 'G' */

    final int DATA_SIZE_128 = 128;
    final int DATA_SIZE_256 = 256;
    final int DATA_SIZE_512 = 512;
    final int DATA_SIZE_1K = 1024;

    final int MODEM_BUFFER_SIZE = 2048;
    int[] modemReceiveDataBytes;
    byte[] modemDataBuffer;
    byte[] zmDataBuffer;
    byte receivedPacketNumber = 1;

    boolean bModemGetNak = false;
    boolean bModemGetAck = false;
    boolean bModemGetCharC = false;
    boolean bModemGetCharG = false;

    int totalModemReceiveDataBytes = 0;
    int totalErrorCount = 0;
    boolean bDataReceived = false;
    boolean bReceiveFirstPacket = false;
    boolean bDuplicatedPacket = false;

    boolean bUartModeTaskSet = true;
    boolean bReadDataProcess = true;
    // X modem -//

    // Y modem +//
    final int Y_MODEM_WAIT_ASK_SEND_FILE = 0;
    final int Y_MODEM_SEND_FILE_INFO_PACKET = 1;
    final int Y_MODEM_SEND_FILE_INFO_PACKET_WAIT_ACK = 2;
    final int Y_MODEM_START_SEND_FILE = 3;
    final int Y_MODEM_START_SEND_FILE_WAIT_ACK = 4;
    final int Y_MODEM_START_SEND_FILE_RESEND = 5;
    final int Y_MODEM_SEND_EOT_PACKET = 6;
    final int Y_MODEM_SEND_EOT_PACKET_WAIT_ACT = 7;
    final int Y_MODEM_SEND_LAST_END_PACKET = 8;
    final int Y_MODEM_SEND_LAST_END_PACKET_WAIT_ACK = 9;
    final int Y_MODEM_SEND_FILE_DONE = 10;

    final int DATA_NONE = 0;
    final int DATA_ACK = 1;
    final int DATA_CHAR_C = 2;
    final int DATA_NAK = 3;

    int ymodemState = 0;
    String modemFileName;
    String modemFileSize;
    int modemRemainData = 0;
    // Y modem -//

    // Z modem +//
    final int ZCRC_HEAD_SIZE = 4;

    final byte ZPAD = 0x2A; // '*' 052 Padding character begins frames
    final byte ZDLE = 0x18;
    final byte ZDLEE = ZDLE^0100;   /* Escaped ZDLE as transmitted */

    final byte ZBIN = 0x41;		// 'A' Binary frame indicator (CRC-16)
    final byte ZHEX = 0x42;		// 'B' HEX frame indicator
    final byte ZBIN32 = 0x43;	// 'C' Binary frame with 32 bit CRC

    final byte LF = 0x0A;
    final byte CR = 0x0D;

    final int ZRQINIT = 0;   /* Request receive init */
    final int ZRINIT = 1;   /* Receive init */
    final int ZSINIT = 2;    /* Send init sequence (optional) */
    final int ZACK = 3;      /* ACK to above */
    final int ZFILE = 4;     /* File name from sender */
    final int ZSKIP = 5;     /* To sender: skip this file */
    final int ZNAK = 6;      /* Last packet was garbled */
    final int ZABORT = 7;    /* Abort batch transfers */
    final int ZFIN = 8;      /* Finish session */
    final int ZRPOS = 9;     /* Resume data trans at this position */
    final int ZDATA = 10;    /* Data packet(s) follow */
    final int ZDATA_HEADER = 21;
    final int ZFIN_ACK = 22;

    final int ZEOF = 11;     /* End of file */
    final int ZFERR = 12;    /* Fatal Read or Write error Detected */
    final int ZCRC = 13;     /* Request for file CRC and response */
    final int ZCHALLENGE = 14;   /* Receiver's Challenge */
    final int ZCOMPL = 15;   /* Request is complete */
    final int ZCAN = 16;     /* Other end canned session with CAN*5 */
    final int ZFREECNT = 17; /* Request for free bytes on filesystem */
    final int ZCOMMAND = 18; /* Command from sending program */
    final int ZSTDERR = 19;  /* Output to standard error, data follows */
    final int ZOO = 20;

    final int ZCRCE = 0x68; // no data
    final int ZCRCG = 0x69; // more data
    final int ZCRCW = 0x6B; // file info end

    final int ZDLE_END_SIZE_4 = 4; // zdle ZCRC? crc1 crc2
    final int ZDLE_END_SIZE_5 = 5; // zdle ZCRC? zdle crc1 crc2 || zdle ZCRC? crc1 zdle crc2
    final int ZDLE_END_SIZE_6 = 6; // zdle ZCRC? zdle crc1 zdle crc2

    final int ZF0 = 3;   /* First flags byte */
    final int ZF1 = 2;
    final int ZF2 = 1;
    final int ZF3 = 0;
    final int ZP0 = 0;   /* Low order 8 bits of position */
    final int ZP1 = 1;
    final int ZP2 = 2;
    final int ZP3 = 3;   /* High order 8 bits of file position */

    int zmodemState = 0;

    // fixed pattern, used to check ZRQINIT
    final int ZMS_0 = 0;
    final int ZMS_1 = 1; // r
    final int ZMS_2 = 2; // z
    final int ZMS_3 = 3; // \r
    final int ZMS_4 = 4; // ZPAD (ZRQINIT)
    final int ZMS_5 = 5; // ZPAD
    final int ZMS_6 = 6; // ZDLE
    final int ZMS_7 = 7; // ZHEX
    final int ZMS_8 = 8; // 0x30
    final int ZMS_9 = 9; // 0x30
    final int ZMS_10 = 10; // 0x30
    final int ZMS_11 = 11; // 0x30
    final int ZMS_12 = 12; // 0x30
    final int ZMS_13 = 13; // 0x30
    final int ZMS_14 = 14; // 0x30
    final int ZMS_15 = 15; // 0x30
    final int ZMS_16 = 16; // 0x30
    final int ZMS_17 = 17; // 0x30
    final int ZMS_18 = 18; // 0x30
    final int ZMS_19 = 19; // 0x30
    final int ZMS_20 = 20; // 0x30
    final int ZMS_21 = 21; // 0x30 (14th 0x30)
    final int ZMS_22 = 22; // 0x0D
    final int ZMS_23 = 23; // 0x0A
    final int ZMS_24 = 24; // 0x11
    int zmStartState = 0;
    // Z modem -//

    // general data count
    int totalReceiveDataBytes = 0;
    int totalUpdateDataBytes = 0;

    ReadThread readThread; // read data from USB

    // variables
    final int UI_READ_BUFFER_SIZE = 10240; // Notes: 115K:1440B/100ms, 230k:2880B/100ms
    byte[] writeBuffer;
    byte[] readBuffer;
    char[] readBufferToChar;
    int actualNumBytes;

    int baudRate; /* baud rate */
    byte stopBit; /* 1:1stop bits, 2:2 stop bits */
    byte dataBit; /* 8:8bit, 7: 7bit */
    byte parity; /* 0: none, 1: odd, 2: even, 3: mark, 4: space */
    byte flowControl; /* 0:none, 1: CTS/RTS, 2:DTR/DSR, 3:XOFF/XON */
    public Context global_context;
    boolean uart_configured = false;

    String uartSettings  = "";

    //public final int maxReadLength = 256;
    byte[] usbdata;
    char[] readDataToText;
    public int iavailable = 0;

    // file access//
    FileInputStream inputstream;
    FileOutputStream outputstream;

    FileWriter file_writer;
    FileReader file_reader;
    FileInputStream fis_open;
    FileOutputStream fos_save;
    BufferedOutputStream buf_save;
    boolean WriteFileThread_start = false;

    String fileNameInfo;
    String sFileName;
    int iFileSize = 0;
    int sendByteCount = 0;
    long start_time, end_time;
    long cal_time_1, cal_time_2;

    // data buffer
    byte[] writeDataBuffer;
    byte[] readDataBuffer; /* circular buffer */

    int iTotalBytes;
    int iReadIndex;

    final int MAX_NUM_BYTES = 65536;

    boolean bReadTheadEnable = false;
    boolean bContentFormatHex = false;
    public FTD2XXManager(Context context ,
                         int _baudRate,  /* baud rate */
                         byte _stopBit, /* 1:1stop bits, 2:2 stop bits */
                         byte _dataBit, /* 8:8bit, 7: 7bit */
                         byte _parity, /* 0: none, 1: odd, 2: even, 3: mark, 4: space */
                         byte _flowControl/* 0:none, 1: CTS/RTS, 2:DTR/DSR, 3:XOFF/XON */) {
        global_context  = context;
        baudRate        = _baudRate;
        dataBit         = _dataBit;
        stopBit         = _stopBit;
        parity          = _parity;
        flowControl = _flowControl;
        createDeviceList();
        if(DevCount > 0)
        {
            connectFunction();
            setUARTInfoString();
            setConfig(baudRate, dataBit, stopBit, parity, flowControl);
        }

    }


    public void createDeviceList()
    {
        int tempDevCount = ftD2xx.createDeviceInfoList(global_context);

        if (tempDevCount > 0)
        {
            if( DevCount != tempDevCount )
            {
                DevCount = tempDevCount;
                //updatePortNumberSelector();
            }
        }
        else
        {
            DevCount = -1;
            currentPortIndex = -1;
        }
    }

    public void disconnectFunction()
    {
        DevCount = -1;
        currentPortIndex = -1;
        bReadTheadEnable = false;
        try
        {
            Thread.sleep(50);
        }
        catch (InterruptedException e) {e.printStackTrace();}

        if(ftDev != null)
        {
            if( true == ftDev.isOpen())
            {
                ftDev.close();
            }
        }
    }

    public void connectFunction()
    {
        if( portIndex + 1 > DevCount)
        {
            portIndex = 0;
        }

        if( currentPortIndex == portIndex
                && ftDev != null
                && true == ftDev.isOpen() )
        {
            //Toast.makeText(global_context,"Port("+portIndex+") is already opened.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(true == bReadTheadEnable)
        {
            bReadTheadEnable = false;
            try
            {
                Thread.sleep(50);
            }
            catch (InterruptedException e) {e.printStackTrace();}
        }

        if(null == ftDev)
        {
            ftDev = ftD2xx.openByIndex(global_context, portIndex);
        }
        else
        {
            ftDev = ftD2xx.openByIndex(global_context, portIndex);
        }
        uart_configured = false;

        if(ftDev == null)
        {
            midToast("Open port("+portIndex+") NG!", Toast.LENGTH_LONG);
            return;
        }

        if (true == ftDev.isOpen())
        {
            currentPortIndex = portIndex;
            //Toast.makeText(global_context, "open device port(" + portIndex + ") OK", Toast.LENGTH_SHORT).show();

            if(false == bReadTheadEnable)
            {
                readThread = new ReadThread(handler);
                readThread.start();
            }
        }
        else
        {
            midToast("Open port("+portIndex+") NG!", Toast.LENGTH_LONG);
        }
    }

    public DeviceStatus checkDevice()
    {
        if(ftDev == null || false == ftDev.isOpen())
        {
            midToast("Need to connect to cable.",Toast.LENGTH_SHORT);
            return DeviceStatus.DEV_NOT_CONNECT;
        }
        else if(false == uart_configured)
        {
            //midToast("CHECK: uart_configured == false", Toast.LENGTH_SHORT);
            midToast("Need to configure UART.",Toast.LENGTH_SHORT);
            return DeviceStatus.DEV_NOT_CONFIG;
        }

        return DeviceStatus.DEV_CONFIG;

    }

    public void setUARTInfoString()
    {
        String parityString, flowString;

        switch(parity)
        {
            case 0: parityString = new String("None"); break;
            case 1: parityString = new String("Odd"); break;
            case 2: parityString = new String("Even"); break;
            case 3: parityString = new String("Mark"); break;
            case 4: parityString = new String("Space"); break;
            default: parityString = new String("None"); break;
        }

        switch(flowControl)
        {
            case 0: flowString = new String("None"); break;
            case 1: flowString = new String("CTS/RTS"); break;
            case 2: flowString = new String("DTR/DSR"); break;
            case 3: flowString = new String("XOFF/XON"); break;
            default: flowString = new String("None"); break;
        }

        uartSettings = "Port " + portIndex + "; UART Setting  -  Baudrate:" + baudRate + "  StopBit:" + stopBit
                + "  DataBit:" + dataBit + "  Parity:" + parityString
                + "  FlowControl:" + flowString;

        resetStatusData();
    }

    public void setConfig(int baud, byte dataBits, byte stopBits, byte parity, byte flowControl)
    {
        // configure port
        // reset to UART mode for 232 devices
        ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);

        ftDev.setBaudRate(baud);

        switch (dataBits)
        {
            case 7:
                dataBits = D2xxManager.FT_DATA_BITS_7;
                break;
            case 8:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
            default:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
        }

        switch (stopBits)
        {
            case 1:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
            case 2:
                stopBits = D2xxManager.FT_STOP_BITS_2;
                break;
            default:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
        }

        switch (parity)
        {
            case 0:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
            case 1:
                parity = D2xxManager.FT_PARITY_ODD;
                break;
            case 2:
                parity = D2xxManager.FT_PARITY_EVEN;
                break;
            case 3:
                parity = D2xxManager.FT_PARITY_MARK;
                break;
            case 4:
                parity = D2xxManager.FT_PARITY_SPACE;
                break;
            default:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
        }

        ftDev.setDataCharacteristics(dataBits, stopBits, parity);

        short flowCtrlSetting;
        switch (flowControl)
        {
            case 0:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
            case 1:
                flowCtrlSetting = D2xxManager.FT_FLOW_RTS_CTS;
                break;
            case 2:
                flowCtrlSetting = D2xxManager.FT_FLOW_DTR_DSR;
                break;
            case 3:
                flowCtrlSetting = D2xxManager.FT_FLOW_XON_XOFF;
                break;
            default:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
        }

        ftDev.setFlowControl(flowCtrlSetting, XON, XOFF);

        setUARTInfoString();
        midToast(uartSettings,Toast.LENGTH_SHORT);

        uart_configured = true;
    }

    public void sendData(int numBytes, byte[] buffer)
    {
        if (ftDev.isOpen() == false) {
            DLog.e(TT, "SendData: device not open");
            Toast.makeText(global_context, "Device not open!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (numBytes > 0)
        {
            ftDev.write(buffer, numBytes);
        }
    }

    public void sendData(byte buffer)
    {
        DLog.e(TT,"send buf:"+ Integer.toHexString(buffer));
        byte tmpBuf[] = new byte[1];
        tmpBuf[0] = buffer;
        ftDev.write(tmpBuf, 1);
    }
// j2xx functions -

    // get the first byte of incoming data
    public byte firstData()
    {
        if(iTotalBytes > 0)
        {
            return readDataBuffer[iReadIndex];
        }

        return 0x00;
    }

    // For zmWaitReadData: Write data at offset of buffer.
    public byte readData(int numBytes, int offset, byte[] buffer)
    {
        byte intstatus = 0x00; /* success by default */

		/* should be at least one byte to read */
        if ((numBytes < 1) || (0 == iTotalBytes))
        {
            actualNumBytes = 0;
            intstatus = 0x01;
            return intstatus;
        }

        if (numBytes > iTotalBytes)
        {
            numBytes = iTotalBytes;
        }

		/* update the number of bytes available */
        iTotalBytes -= numBytes;
        actualNumBytes = numBytes;

		/* copy to the user buffer */
        for (int count = offset; count < numBytes+offset; count++)
        {
            buffer[count] = readDataBuffer[iReadIndex];
            iReadIndex++;
            iReadIndex %= MAX_NUM_BYTES;
        }

        return intstatus;
    }

    public byte readData(int numBytes, byte[] buffer)
    {
        byte intstatus = 0x00; /* success by default */

		/* should be at least one byte to read */
        if ((numBytes < 1) || (0 == iTotalBytes))
        {
            actualNumBytes = 0;
            intstatus = 0x01;
            return intstatus;
        }

        if (numBytes > iTotalBytes)
        {
            numBytes = iTotalBytes;
        }

		/* update the number of bytes available */
        iTotalBytes -= numBytes;
        actualNumBytes = numBytes;

		/* copy to the user buffer */
        for (int count = 0; count < numBytes; count++)
        {
            buffer[count] = readDataBuffer[iReadIndex];
            iReadIndex++;
            iReadIndex %= MAX_NUM_BYTES;
        }

        return intstatus;
    }

    public void resetStatusData()
    {
        String tempStr = "Format - " + (bContentFormatHex?"Hexadecimal":"Character") +"\n"+ uartSettings;
        String tmp = tempStr.replace("\\n", "\n");
//        uartInfo.setText(tmp);
    }

    public void updateStatusData(String str)
    {
//        String temp;
//        if(null == fileNameInfo)
//            temp = "\n" + str;
//        else
//            temp = fileNameInfo + "\n" + str;
//
//        String tmp = temp.replace("\\n", "\n");
//        uartInfo.setText(tmp);
    }

    // call this API to show message
    public void midToast(String str, int showTime)
    {
        Toast toast = Toast.makeText(global_context, str, showTime);
        toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL , 0, 0);

        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        v.setTextColor(Color.YELLOW);
        toast.show();
    }

    class ReadThread extends Thread
    {
        final int USB_DATA_BUFFER = 8192;

        Handler mHandler;
        ReadThread(Handler h)
        {
            mHandler = h;
            this.setPriority(MAX_PRIORITY);
        }

        public void run()
        {
            byte[] usbdata = new byte[USB_DATA_BUFFER];
            int readcount = 0;
            int iWriteIndex = 0;
            bReadTheadEnable = true;

            while (true == bReadTheadEnable)
            {
                try
                {
                    Thread.sleep(10);
                }
                catch (InterruptedException e) {e.printStackTrace();}

                DLog.e(TT,"iTotalBytes:"+iTotalBytes);
                while(iTotalBytes > (MAX_NUM_BYTES - (USB_DATA_BUFFER+1)))
                {
                    try
                    {
                        Thread.sleep(50);
                    }
                    catch (InterruptedException e) {e.printStackTrace();}
                }

                readcount = ftDev.getQueueStatus();
                iavailable = readcount;
                //Log.e(">>@@","iavailable:" + iavailable);
                if (readcount > 0)
                {
                    if(readcount > USB_DATA_BUFFER)
                    {
                        readcount = USB_DATA_BUFFER;
                    }
                    ftDev.read(usbdata, readcount);

                    if( (MODE_X_MODEM_CHECKSUM_SEND == transferMode)
                            ||(MODE_X_MODEM_CRC_SEND == transferMode)
                            ||(MODE_X_MODEM_1K_CRC_SEND == transferMode) )
                    {
                        for (int i = 0; i < readcount; i++)
                        {
                            modemDataBuffer[i] = usbdata[i];
                            DLog.e(TXS,"RT usbdata["+i+"]:("+usbdata[i]+")");
                        }

                        if(NAK == modemDataBuffer[0])
                        {
                            DLog.e(TXS,"get response - NAK");
                            bModemGetNak = true;
                        }
                        else if(ACK == modemDataBuffer[0])
                        {
                            DLog.e(TXS,"get response - ACK");
                            bModemGetAck = true;
                        }
                        else if(CHAR_C == modemDataBuffer[0])
                        {
                            DLog.e(TXS,"get response - CHAR_C");
                            bModemGetCharC = true;
                        }
                        if(CHAR_G == modemDataBuffer[0])
                        {
                            DLog.e(TXS,"get response - CHAR_G");
                            bModemGetCharG = true;
                        }
                    }
                    else
                    {
                        totalReceiveDataBytes += readcount;
                        //DLog.e(TT,"totalReceiveDataBytes:"+totalReceiveDataBytes);

                        //DLog.e(TT,"readcount:"+readcount);
                        for (int count = 0; count < readcount; count++)
                        {
                            readDataBuffer[iWriteIndex] = usbdata[count];
                            iWriteIndex++;
                            iWriteIndex %= MAX_NUM_BYTES;
                        }

                        if (iWriteIndex >= iReadIndex)
                        {
                            iTotalBytes = iWriteIndex - iReadIndex;
                        }
                        else
                        {
                            iTotalBytes = (MAX_NUM_BYTES - iReadIndex) + iWriteIndex;
                        }

                        //DLog.e(TT,"iTotalBytes:"+iTotalBytes);
                        if( (MODE_X_MODEM_CHECKSUM_RECEIVE == transferMode)
                                || (MODE_X_MODEM_CRC_RECEIVE == transferMode)
                                || (MODE_X_MODEM_1K_CRC_RECEIVE == transferMode)
                                || (MODE_Y_MODEM_1K_CRC_RECEIVE == transferMode)
                                || (MODE_Z_MODEM_RECEIVE == transferMode)
                                || (MODE_Z_MODEM_SEND == transferMode) )
                        {
                            modemReceiveDataBytes[0] += readcount;
                            DLog.e(TT,"modemReceiveDataBytes:"+modemReceiveDataBytes[0]);
                        }
                    }
                }
            }

            DLog.e(TT, "read thread terminate...");;
        }
    }
    final Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case UPDATE_TEXT_VIEW_CONTENT:
                    if (actualNumBytes > 0)
                    {
                        totalUpdateDataBytes += actualNumBytes;
                        for(int i=0; i<actualNumBytes; i++)
                        {
                            readBufferToChar[i] = (char)readBuffer[i];
                        }
//                        appendData(String.copyValueOf(readBufferToChar, 0, actualNumBytes));
                    }
                    break;

                case UPDATE_SEND_FILE_STATUS:
                {
                    String temp = currentProtocol;
                    if(sendByteCount <= 10240)
                        temp += " Send:" + sendByteCount + "B("
                                + new java.text.DecimalFormat("#.00").format(sendByteCount/(iFileSize/(double)100))+"%)";
                    else
                        temp += " Send:" +  new java.text.DecimalFormat("#.00").format(sendByteCount/(double)1024) + "KB("
                                + new java.text.DecimalFormat("#.00").format(sendByteCount/(iFileSize/(double)100))+"%)";

                    updateStatusData(temp);
                }
                break;

                case UPDATE_SEND_FILE_DONE:
                {
                    midToast("Send file Done.", Toast.LENGTH_SHORT);

                    String temp = currentProtocol;
                    if(0 == iFileSize)
                    {
                        temp += " - The sent file is 0 byte";
                    }
                    else if(iFileSize < 100)
                    {
                        temp += " Send:" + sendByteCount + "B("
                                + new java.text.DecimalFormat("#.00").format(sendByteCount*100/iFileSize)+"%)";
                    }
                    else
                    {
                        if(sendByteCount <= 10240)
                            temp += " Send:" + sendByteCount + "B("
                                    + new java.text.DecimalFormat("#.00").format(sendByteCount/(iFileSize/(double)100))+"%)";
                        else
                            temp += " Send:" + new java.text.DecimalFormat("#.00").format(sendByteCount/(double)1024) + "KB("
                                    + new java.text.DecimalFormat("#.00").format(sendByteCount/(iFileSize/(double)100))+"%)";
                    }

                    Double diffime = (double)(end_time-start_time)/1000;
                    temp += " in " + diffime.toString() + " seconds";

                    updateStatusData(temp);

//                    resetSendButton();
                }
                break;

                case ACT_SELECT_SAVED_FILE_NAME:
//                    setProtocolMode();

                    DLog.e(TT,"ACT_SELECT_SAVED_FILE_NAME transferMode:"+transferMode+" UART:" + (bUartModeTaskSet?"True":"False"));
//                    saveFileAction();
                    break;

                case ACT_SELECT_SAVED_FILE_FOLDER:
//                    getSelectedFolder();
                    break;

                case ACT_SAVED_FILE_NAME_CREATED:
//                    setProtocolMode();

                    DLog.e(TT,"ACT_SAVED_FILE_NAME_CREATED transferMode:"+transferMode+" UART:" + (bUartModeTaskSet?"True":"False"));
//                    fGetFile = new File((String)msg.obj);
//                    saveFileAction();
                    break;

                case ACT_SELECT_SEND_FILE_NAME:
//                    setProtocolMode();

//                    sendFileAction();
                    break;

                case MSG_SELECT_FOLDER_NOT_FILE:
                    midToast("Do not pick a file.\n" +
                            "Plesae press \"Select Directory\" button to select current directory.", Toast.LENGTH_LONG);
                    break;

                case MSG_XMODEM_SEND_FILE_TIMEOUT:
                {
                    String temp = currentProtocol + " - No response when send file.";
                    midToast(temp, Toast.LENGTH_LONG);
                    updateStatusData(temp);

//                    resetSendButton();
                }
                break;

                case UPDATE_MODEM_RECEIVE_DATA:
                    midToast(currentProtocol + " - Receiving data...",Toast.LENGTH_LONG);

                case UPDATE_MODEM_RECEIVE_DATA_BYTES:
                {
                    String temp = currentProtocol;
                    if(totalModemReceiveDataBytes <= 10240)
                        temp += " Receive " + totalModemReceiveDataBytes + "Bytes";
                    else
                        temp += " Receive " +  new java.text.DecimalFormat("#.00").format(totalModemReceiveDataBytes/(double)1024) + "KBytes";

                    updateStatusData(temp);
                }
                break;

                case UPDATE_MODEM_RECEIVE_DONE:
                {
//                    saveFileActionDone();

                    String temp = currentProtocol;
                    if(totalModemReceiveDataBytes <= 10240)
                        temp += " Receive " + totalModemReceiveDataBytes + "Bytes";
                    else
                        temp += " Receive " +  new java.text.DecimalFormat("#.00").format(totalModemReceiveDataBytes/(double)1024) + "KBytes";

                    Double diffime = (double)(end_time-start_time)/1000;
                    temp += " in " + diffime.toString() + " seconds";

                    updateStatusData(temp);
                }
                break;

                case MSG_MODEM_RECEIVE_PACKET_TIMEOUT:
                {
                    midToast( currentProtocol + " - No Incoming Data.", Toast.LENGTH_LONG);
                    String temp = currentProtocol;
                    if(totalModemReceiveDataBytes <= 10240)
                        temp += " Receive " + totalModemReceiveDataBytes + "Bytes";
                    else
                        temp += " Receive " +  new java.text.DecimalFormat("#.00").format(totalModemReceiveDataBytes/(double)1024) + "KBytes";

                    updateStatusData(temp);
//                    saveFileActionDone();
                }
                break;

                case ACT_MODEM_SELECT_SAVED_FILE_FOLDER:
//                    setProtocolMode();

//                    getModemSelectedFolder();
                    break;

                case MSG_MODEM_OPEN_SAVE_FILE_FAIL:
                    midToast(currentProtocol + " - Open save file fail!", Toast.LENGTH_LONG);
                    break;

                case MSG_YMODEM_PARSE_FIRST_PACKET_FAIL:
                    midToast("YModem - Can't parse packet due to incorrect data format!", Toast.LENGTH_LONG);
//                    resetLogButton();
                    break;

                case MSG_FORCE_STOP_SEND_FILE:
                    midToast("Stop sending file.", Toast.LENGTH_LONG);
                    break;

                case UPDATE_ASCII_RECEIVE_DATA_BYTES:
                {
                    String temp = currentProtocol;
                    if(totalReceiveDataBytes <= 10240)
                        temp += " Receive " + totalReceiveDataBytes + "Bytes";
                    else
                        temp += " Receive " +  new java.text.DecimalFormat("#.00").format(totalReceiveDataBytes/(double)1024) + "KBytes";

                    long tempTime = System.currentTimeMillis();
                    Double diffime = (double)(tempTime-start_time)/1000;
                    temp += " in " + diffime.toString() + " seconds";

                    updateStatusData(temp);
                }
                break;

                case UPDATE_ASCII_RECEIVE_DATA_DONE:
//                    saveFileActionDone();
                    break;

                case MSG_FORCE_STOP_SAVE_TO_FILE:
                    midToast("Stop saving to file.", Toast.LENGTH_LONG);
                    break;

                case UPDATE_ZMODEM_STATE_INFO:
                    updateStatusData("zmodemState:"+zmodemState);

                    if(ZOO == zmodemState)
                    {
                        midToast("ZModem revice file done.", Toast.LENGTH_SHORT);
                    }
                    break;

                case ACT_ZMODEM_AUTO_START_RECEIVE:
                    bUartModeTaskSet = false;
                    transferMode = MODE_Z_MODEM_RECEIVE;
                    currentProtocol = "ZModem";


                    receivedPacketNumber = 1;
                    modemReceiveDataBytes[0] = 0;
                    totalModemReceiveDataBytes = 0;
                    bDataReceived = false;
                    bReceiveFirstPacket = false;
                    fileNameInfo = null;

//                    setLogButton();

                    zmodemState = ZRINIT;
                    start_time = System.currentTimeMillis();
//                    ZModemReadDataThread zmReadThread = new ZModemReadDataThread(handler);
//                    zmReadThread.start();
                    break;


                case MSG_SPECIAL_INFO:

                    midToast("INFO:" + (String)(msg.obj), Toast.LENGTH_LONG);
                    break;

                case MSG_UNHANDLED_CASE:
                    if(msg.obj != null)
                        midToast("UNHANDLED CASE:"+ (String)(msg.obj), Toast.LENGTH_LONG);
                    else
                        midToast("UNHANDLED CASE ?", Toast.LENGTH_LONG);
                    break;
                default:
                    midToast("NG CASE", Toast.LENGTH_LONG);
                    //Toast.makeText(global_context, ".", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
