require 'bundler/setup'
require 'rack'
require 'thin'
require 'pg'
require 'csv'

#
# database (pg refers to postgresql)
#
$cn = PGconn.open(dbname: 'track_jd')

#
# Return the device_id used to record the source of the logged data. If this is
# a new source, we create a new device_id for it and return that.
#
def lookup_device ip, params, try_insert=true
  # normalise identifiers
  installation = (params['installation'] || '').downcase.strip
  bdaddr       = (params['bdaddr'] || '').downcase.strip
  macaddr      = (params['macaddr'] || '').downcase.strip

  values = [installation, ip, bdaddr, macaddr]

  ids = $cn.exec('
SELECT device_id FROM devices
WHERE installation=$1 AND ip=$2 AND bdaddr=$3 AND macaddr=$4', values)
  device_id = nil
  ids.each do |id|
    return id['device_id'].to_i
  end

  if try_insert
    # try again once
    result = $cn.exec('
INSERT INTO devices(installation, ip, bdaddr, macaddr)
VALUES($1, $2, $3, $4)', values)
    return lookup_device(ip, params, false)
  else
    raise "device not found after insert: #{values.inspect}"
  end
end

#
# Insert GPS records.
#
def store_gps_records device_id, csv
  sql = 'INSERT INTO gps_records (device_id,
           accuracy, altitude, latitude, longitude, time)
         VALUES ($1, $2, $3, $4, $5, $6)'
  CSV.parse(csv, headers: true).each do |row|
    $cn.exec(sql, [device_id, row['accuracy'], row['altitude'],
      row['latitude'], row['longitude'], row['time']])
  end
end

#
# Insert accelerometer records.
#
def store_accelerometer_records device_id, csv
  sql = 'INSERT INTO accelerometer_records (device_id, x, y, z, time)
         VALUES ($1, $2, $3, $4, $5)'
  CSV.parse(csv, headers: true).each do |row|
    $cn.exec(sql, [device_id, row['x'], row['y'], row['z'], row['time']])
  end
end

#
# Insert orientation records.
#
def store_orientation_records device_id, csv
  sql = 'INSERT INTO orientation_records (device_id, azimuth, pitch, roll, time)
         VALUES ($1, $2, $3, $4, $5)'
  CSV.parse(csv, headers: true).each do |row|
    $cn.exec(sql, [device_id, row['azimuth'], row['pitch'], row['roll'],
             row['time']])
  end
end
#
# Insert Bluetooth scan records.
#
def store_bluetooth_records device_id, csv
  sql = 'INSERT INTO bluetooth_records (device_id, bdaddr, rssi, time)
         VALUES ($1, $2, $3, $4)'
  CSV.parse(csv, headers: true).each do |row|
    $cn.exec(sql, [device_id, row['bdaddr'], row['rssi'], row['time']])
  end
end


#
# define web server 
#
# Note: this doesn't do anything smart to avoid duplicates; it's assumed that
# the device doesn't send data more than once, but it would be sensible to trap
# that case here.
#
track_jd = Rack::Builder.new do
  map '/log' do
    run Proc.new {|env|
      req = Rack::Request.new(env)

      device_id = lookup_device req.ip, req.params

      store_gps_records(device_id, req.params['gps']) if req.params['gps']

      store_accelerometer_records(device_id, req.params['accel']) if
        req.params['accel']

      store_orientation_records(device_id, req.params['orient']) if
        req.params['orient']

      store_bluetooth_records(device_id, req.params['bt']) if req.params['bt']

      [200, {"Content-Type" => "text/plain"}, ""]
    }
  end
end

#
# run web server
#
Rack::Handler::Thin.run track_jd, :Port => 3666

#
# cleanup
#
$cn.close

