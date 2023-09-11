CREATE TYPE appointment_status_enum AS enum('Pending','Canceled','Attended','Missed');
CREATE TYPE notification_status_enum AS enum('ToNotify','Notified','ToCancel');
CREATE TYPE specialty_enum AS enum( 'General Medicine', 'Pediatrics', 'Cardiology', 'Orthopedics', 'Dermatology', 'Gastroenterology', 'Neurology', 'Ophthalmology', 'Oncology', 'Otolaryngology', 'Urology', 'Psychiatry', 'Obstetrics', 'Gynecology', 'Anesthesiology', 'Radiology', 'Pathology', 'Emergency', 'Family Medicine', 'Internal Medicine', 'Surgery', 'Other');
CREATE TABLE appointment (
    id SERIAL NOT NULL PRIMARY KEY,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    doctor_id INT NOT NULL,
    patient_id INT NOT NULL,
    medical_record_id INT NOT NULL,
    date_of_scheduling TIMESTAMP  NOT NULL DEFAULT NOW(),
    status appointment_status_enum NOT NULL,
    notification_status notification_status_enum NOT NULL,
    specialty specialty_enum NOT NULL
);
