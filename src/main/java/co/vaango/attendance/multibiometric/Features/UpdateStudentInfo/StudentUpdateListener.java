package co.vaango.attendance.multibiometric.Features.UpdateStudentInfo;

import co.vaango.attendance.multibiometric.Features.CreateStudent.Student;

public interface StudentUpdateListener {
    void onStudentInfoUpdated(Student student, int position);
}
