package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {
    private final int Duration;
    private int currTime;
    private int TickTime;

    public TickBroadcast(int duration, int currTime, int tickTime) {
        Duration = duration;
        this.currTime = currTime;
        TickTime = tickTime;
    }

    public int getDuration() {
        return Duration;
    }

    public int getCurrTime() {
        return currTime;
    }

    public void setCurrTime(int currTime) {
        this.currTime = currTime;
    }

    public int getTickTime() {
        return TickTime;
    }

    public void setTickTime(int tickTime) {
        TickTime = tickTime;
    }
}
