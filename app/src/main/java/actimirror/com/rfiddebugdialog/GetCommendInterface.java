package actimirror.com.rfiddebugdialog;

import java.util.List;

public interface GetCommendInterface {
    void setBaudrate();

    void getFirmware();

    void setWorkAntenna();

    void inventoryRealTime();

    void selectEPC(byte[] var1);

    void readFrom6C(int var1, int var2, int var3);

    void writeTo6C(byte[] var1, int var2, int var3, byte[] var4);
}