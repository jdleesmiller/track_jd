--
-- Run with
-- psql -U john postgres <create_db.sql
--
-- Assumes database does not exist. To drop database:
-- dropdb track_jd 
--
CREATE DATABASE track_jd;

\c track_jd

CREATE TABLE devices (
  device_id SERIAL PRIMARY KEY,
  installation CHAR(36),
  ip INET,
  bdaddr CHAR(17),
  macaddr CHAR(17));

CREATE TABLE gps_records (
  gps_record_id SERIAL PRIMARY KEY,
  device_id INTEGER REFERENCES devices(device_id),
  device_gps_record_id INTEGER,
  utc_time BIGINT,
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  accuracy REAL,
  altitude DOUBLE PRECISION,
  bearing REAL,
  speed REAL,
  num_satellites SMALLINT);

CREATE TABLE network_records (
  network_record_id SERIAL PRIMARY KEY,
  device_id INTEGER REFERENCES devices(device_id),
  device_network_record_id INTEGER,
  utc_time BIGINT,
  event_time BIGINT,
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  accuracy REAL)

CREATE TABLE accelerometer_records (
  accelerometer_record_id SERIAL PRIMARY KEY,
  device_id INTEGER REFERENCES devices(device_id),
  device_accelerometer_record_id INTEGER,
  utc_time BIGINT,
  x REAL,
  y REAL,
  z REAL);

CREATE TABLE orientation_records (
  orientation_record_id SERIAL PRIMARY KEY,
  device_id INTEGER REFERENCES devices(device_id),
  device_orientation_record_id INTEGER,
  utc_time BIGINT,
  azimuth REAL,
  pitch REAL,
  roll REAL);

CREATE TABLE bluetooth_records (
  bluetooth_record_id SERIAL PRIMARY KEY,
  device_id INTEGER REFERENCES devices(device_id),
  device_bluetooth_record_id INTEGER,
  utc_time BIGINT,
  bdaddr CHAR(17),
  RSSI INT8);

