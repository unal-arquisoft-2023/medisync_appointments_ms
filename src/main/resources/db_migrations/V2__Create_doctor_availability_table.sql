CREATE TABLE doctor_availability (
    id SERIAL NOT NULL PRIMARY KEY,
    doctor_id INT NOT NULL,
    specialty_id INT NOT NULL, 
    monday tstzrange NULL,
    tuesday tstzrange NULL,
    wednesday tstzrange NULL,
    thursday tstzrange NULL,
    friday tstzrange NULL,
    saturday tstzrange NULL,
    sunday tstzrange NULL
);