CREATE TABLE appointment (
    id SERIAL NOT NULL PRIMARY KEY,
    date TIMESTAMP NOT NULL,
    doctor_id INT NOT NULL,
    patient_id INT NOT NULL,
    medical_record_id INT NOT NULL,
    date_of_scheduling TIMESTAMP  NOT NULL DEFAULT NOW()
);
