package cn.iselab.mooctest.lit.common.enums;

public enum DeviceState {

    /**
     * IDLE
     * BUSY
     * DELETE
     * LOCK
     */

    IDLE(1000),
    BUSY(1001),
    DELETE(1002),
    LOCK(1003);

    private int status;

    DeviceState(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
