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
           device_gps_record_id, utc_time, latitude, longitude,
           accuracy, altitude, bearing, speed, num_satellites)
         VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)'
  CSV.parse(csv, headers: false).each do |row|
    $cn.exec(sql, [device_id] + row)
  end
end

#
# Insert network location (coarse location) records.
#
def store_network_records device_id, csv
  sql = 'INSERT INTO network_records (device_id,
           device_network_record_id, utc_time,
           event_time, latitude, longitude, accuracy)
         VALUES ($1, $2, $3, $4, $5, $6, $7)'
  CSV.parse(csv, headers: false).each do |row|
    $cn.exec(sql, [device_id] + row)
  end
end


#
# Insert accelerometer records.
#
def store_accelerometer_records device_id, csv
  sql = 'INSERT INTO accelerometer_records (device_id,
           device_accelerometer_record_id, utc_time, x, y, z)
         VALUES ($1, $2, $3, $4, $5, $6)'
  CSV.parse(csv, headers: false).each do |row|
    $cn.exec(sql, [device_id] + row)
  end
end

#
# Insert orientation records.
#
def store_orientation_records device_id, csv
  sql = 'INSERT INTO orientation_records (device_id,
           device_orientation_record_id, utc_time, azimuth, pitch, roll)
         VALUES ($1, $2, $3, $4, $5, $6)'
  CSV.parse(csv, headers: false).each do |row|
    $cn.exec(sql, [device_id] + row)
  end
end

#
# Insert Bluetooth scan records.
#
def store_bluetooth_records device_id, csv
  sql = 'INSERT INTO bluetooth_records (device_id,
           device_bluetooth_record_id, utc_time, bdaddr, rssi)
         VALUES ($1, $2, $3, $4, $5)'
  CSV.parse(csv, headers: false).each do |row|
    $cn.exec(sql, [device_id] + row)
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

      device_id = lookup_device(req.ip, req.params)

      # this is milliseconds since the epoch
      device_clock = Time.at(req.params['device_clock'].to_i/1e3) rescue nil

      puts "recv from device #{device_id} (#{req.ip} @ #{device_clock})"

      store_gps_records(device_id,
        req.params['gps_v1'][:tempfile]) if req.params['gps_v1']

      store_network_records(device_id,
        req.params['network_v1'][:tempfile]) if req.params['network_v1']

      store_accelerometer_records(device_id,
        req.params['accel_v1'][:tempfile]) if req.params['accel_v1']

      store_orientation_records(device_id,
        req.params['orient_v1'][:tempfile]) if req.params['orient_v1']

      store_bluetooth_records(device_id,
        req.params['bt_v1'][:tempfile]) if req.params['bt_v1']

      [200, {"Content-Type" => "text/plain"}, ""]
    }
  end
  map '/' do
    run Proc.new {|env|
      [200, {"Content-Type" => "text/plain"}, "Hello from the TrackJD server."]
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

