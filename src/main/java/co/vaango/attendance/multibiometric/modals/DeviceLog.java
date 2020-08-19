package co.vaango.attendance.multibiometric.modals;

public class DeviceLog {

    private long id;
    private String status;
    private String title;
    private String description;
    private String time;
    private int sync_status;

    public DeviceLog(int id, String status, String title, String description, String time, int sync_status){
        this.id = id;
        this.status = status;
        this.title = title;
        this.description = description;
        this.time = time;
        this.sync_status = sync_status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getSync_status() {
        return sync_status;
    }

    public void setSync_status(int sync_status) {
        this.sync_status = sync_status;
    }

    public String toString(){
        return "\nid : " + id + "\nStatus : " + status + "\ntitle : " + title+ "\nDescription : " + description + "\nTime : " + time+ "\nSync status : " + sync_status;
    }
}
