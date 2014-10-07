
public class BackupInterval extends Communicator implements Runnable {
double interval;

public BackupInterval(double interval) {
    this.interval = interval;
}

public void run() {
    System.out.println("BackupInterval thread run");
    startBackupIntervals();
}

private void startBackupIntervals() {
    try {
        while (true) {
            System.out.println("Backup interval fired, doing backup now.");
            if (interval > 0) {
                emit("doBackup", true);
            }
            Thread.sleep((long)(interval * 60 * 1000));
        }
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}

public double getInterval() {
    return interval;
}

public void setInterval(double interval) {
    this.interval = interval;
    //emit("wake", true);
}
}
