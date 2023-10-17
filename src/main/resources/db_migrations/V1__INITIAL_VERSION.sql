CREATE TYPE appointment_status_enum AS enum('Pending', 'Canceled', 'Attended', 'Missed');

CREATE TYPE notification_status_enum AS enum('ToNotify', 'Notified', 'ToCancel');

CREATE TYPE specialty_enum AS enum(
  'General Medicine',
  'Pediatrics',
  'Cardiology',
  'Orthopedics',
  'Dermatology',
  'Gastroenterology',
  'Neurology',
  'Ophthalmology',
  'Oncology',
  'Otolaryngology',
  'Urology',
  'Psychiatry',
  'Obstetrics',
  'Gynecology',
  'Anesthesiology',
  'Radiology',
  'Pathology',
  'Emergency',
  'Family Medicine',
  'Internal Medicine',
  'Surgery',
  'Other'
);

CREATE TABLE doctor_availability (
  id SERIAL NOT NULL PRIMARY KEY,
  doctor_id VARCHAR NOT NULL,
  specialty specialty_enum NOT NULL,
  availability_date DATE NOT NULL,
  start_time TIME,
  end_time TIME
);

CREATE TABLE availability_time_block (
  id SERIAL NOT NULL PRIMARY KEY,
  start_time_block TIME NOT NULL,
  end_time_block TIME NOT NULL,
  block_time_enabled BOOL NOT NULL
);

INSERT INTO
  availability_time_block (
    start_time_block,
    end_time_block,
    block_time_enabled
  )
SELECT
  start + '1 minute',
  start + '20 minutes',
  TRUE
FROM
  generate_series(
    '2023-08-08 7:00:00' :: timestamp,
    '2023-08-08 20:00:00' :: timestamp,
    '20 minutes'
  ) as start;

CREATE TABLE appointment (
  id SERIAL NOT NULL PRIMARY KEY,
  appointment_date DATE NOT NULL,
  block_time_id INT REFERENCES availability_time_block(id),
  doctor_id VARCHAR NOT NULL,
  patient_id VARCHAR NOT NULL,
  medical_record_id INT NOT NULL,
  scheduled_timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
  notification_status notification_status_enum NOT NULL,
  appointment_status appointment_status_enum NOT NULL,
  specialty specialty_enum NOT NULL
);

-- INSERT INTO
--   appointment(
--     appointment_date,
--     block_time_id,
--     doctor_id,
--     patient_id,
--     medical_record_id,
--     notification_status,
--     appointment_status,
--     specialty
--   )
-- VALUES
--   (
--     '2023-10-01' :: date,
--     9,
--     1,
--     222,
--     345,
--     'ToNotify' :: notification_status_enum,
--     'Pending' :: appointment_status_enum,
--     'Cardiology' :: specialty_enum
--   );

-- INSERT INTO
--   doctor_availability (
--     doctor_id,
--     specialty,
--     availability_date,
--     start_time,
--     end_time
--   )
-- VALUES
--   (
--     2,
--     'Cardiology' :: specialty_enum,
--     '2023-10-01' :: date,
--     '9:00' :: time,
--     '10:00' :: time
--   );

CREATE VIEW general_schedule AS with a as (
  select
    a.id as availability_id,
    doctor_id,
    specialty,
    availability_date,
    b.id as block_time_id,
    start_time_block,
    end_time_block
  FROM
    doctor_availability a
    INNER join availability_time_block b on b.block_time_enabled
    and b.start_time_block >= a.start_time
    and b.end_time_block <= a.end_time
)
select
  a.*,
  p.id as appointment_id,
  p.medical_record_id,
  p.notification_status,
  p.appointment_status
from
  a
  left join appointment p on a.doctor_id = p.doctor_id
  and p.appointment_date = a.availability_date
  and p.block_time_id = a.block_time_id
  AND p.specialty = a.specialty --and ( p.appointment_status='Pending'::appointment_status_enum)