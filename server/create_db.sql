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
  accuracy REAL,
  altitude DOUBLE PRECISION,
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  time BIGINT);

CREATE TABLE accelerometer_records (
  accelerometer_record_id SERIAL PRIMARY KEY,
  device_id INTEGER REFERENCES devices(device_id),
  x REAL,
  y REAL,
  z REAL,
  time BIGINT);

CREATE TABLE orientation_records (
  orientation_record_id SERIAL PRIMARY KEY,
  device_id INTEGER REFERENCES devices(device_id),
  azimuth REAL,
  pitch REAL,
  roll REAL,
  time BIGINT);

CREATE TABLE bluetooth_records (
  bluetooth_record_id SERIAL PRIMARY KEY,
  device_id INTEGER REFERENCES devices(device_id),
  bdaddr CHAR(17),
  RSSI INT8,
  time BIGINT);

