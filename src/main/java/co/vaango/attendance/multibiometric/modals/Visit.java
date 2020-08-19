package co.vaango.attendance.multibiometric.modals;

public class Visit {

    private long id;
    private String user_name;
    private long face_id;
    private String in_time;
    private String out_time;
    private int sync_status;

    public Visit(int id, String user_name, long face_id,String in_time,String out_time, int sync_status){
        this.id = id;
        this.user_name = user_name;
        this.face_id = face_id;
        this.in_time = in_time;
        this.out_time = out_time;
        this.sync_status = sync_status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public long getFace_id() {
        return face_id;
    }

    public void setFace_id(long face_id) {
        this.face_id = face_id;
    }

    public String getIn_time() {
        return in_time;
    }

    public void setIn_time(String in_time) {
        this.in_time = in_time;
    }

    public String getOut_time() {
        return out_time;
    }

    public void setOut_time(String out_time) {
        this.out_time = out_time;
    }

    public int getSync_status() {
        return sync_status;
    }

    public void setSync_status(int sync_status) {
        this.sync_status = sync_status;
    }

    public String toString(){
        return "\nid : " + id + "\nName : " + user_name + "\nfaceid : " + face_id+ "\nTime In : " + in_time + "\nOut Time : " + out_time+ "\nSync status : " + sync_status;
    }
}
