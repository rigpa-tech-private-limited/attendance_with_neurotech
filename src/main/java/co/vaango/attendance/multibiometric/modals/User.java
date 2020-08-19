package co.vaango.attendance.multibiometric.modals;

public class User {

    private long id;
    private String user_name;
    private int user_type;
    private int face_id;
    private String enroll_status;
    private int sync_status;

    public User(int id, String user_name, int user_type, int face_id,String enroll_status, int sync_status){
        this.id = id;
        this.user_name = user_name;
        this.user_type = user_type;
        this.face_id = face_id;
        this.enroll_status = enroll_status;
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

    public int getUser_type() {
        return user_type;
    }

    public void setUser_type(int user_type) {
        this.user_type = user_type;
    }

    public int getFace_id() {
        return face_id;
    }

    public void setFace_id(int face_id) {
        this.face_id = face_id;
    }

    public String getEnroll_status() {
        return enroll_status;
    }

    public void setEnroll_status(String enroll_status) {
        this.enroll_status = enroll_status;
    }

    public int getSync_status() {
        return sync_status;
    }

    public void setSync_status(int sync_status) {
        this.sync_status = sync_status;
    }

    public String toString(){
        return "\nid : " + id + "\nuser_name : " + user_name + "\nuser_type : " + user_type + "\nface_id : " + face_id + "\nenroll_status : " + enroll_status+ "\nSync status : " + sync_status;
    }
}
