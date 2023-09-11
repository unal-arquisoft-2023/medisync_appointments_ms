CREATE TABLE doctor_availability (
    id SERIAL NOT NULL PRIMARY KEY,
    doctor_id INT NOT NULL,
    specialty specialty_enum NOT NULL, 
    enabled BOOLEAN NOT NULL,
    monday tstzrange NULL DEFAULT NULL,
    tuesday tstzrange NULL DEFAULT NULL,
    wednesday tstzrange NULL DEFAULT NULL,
    thursday tstzrange NULL DEFAULT NULL,
    friday tstzrange NULL DEFAULT NULL,
    saturday tstzrange NULL DEFAULT NULL,
    sunday tstzrange NULL DEFAULT NULL
);