CREATE TYPE appointment_status_enum AS enum('Pending','Canceled','Attended','Missed');
CREATE TYPE notification_status_enum AS enum('ToNotify','Notified','ToCancel');

CREATE TABLE appointment (
    id SERIAL NOT NULL PRIMARY KEY,
    date TIMESTAMP NOT NULL,
    doctor_id INT NOT NULL,
    patient_id INT NOT NULL,
    medical_record_id INT NOT NULL,
    date_of_scheduling TIMESTAMP  NOT NULL DEFAULT NOW(),
    status appointment_status_enum NOT NULL,
    notification_status notification_status_enum NOT NULL
);
