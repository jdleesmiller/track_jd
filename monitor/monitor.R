#
# Requirements:
# This uses RODBC. Install ODBC and then edit ~/.odbc.ini to include:
# [track_jd]
# Description           = track_jd
# Driver                = PostgreSQL
# Database              = track_jd
#
# Make sure that you can connect with
# isql track_jd
# before attempting to run this script.
#
# What could be easier?
#
library(RODBC)

monitor.jd <- function(lastTimeLong = -1, max.records=500) {
  gps.records <- NULL
  accelerometer.records <- NULL
  orientation.records <- NULL
  lastTimeLongAccel <- lastTimeLong
  while (TRUE) {
    cn <- odbcConnect('track_jd')
    gps.records.new <- sqlQuery(cn, paste(
      'SELECT * FROM gps_records WHERE time > ', lastTimeLong))
    accelerometer.records.new <- sqlQuery(cn, paste(
      'SELECT * FROM accelerometer_records WHERE time > ', lastTimeLongAccel))
    orientation.records.new <- sqlQuery(cn, paste(
      'SELECT * FROM orientation_records WHERE time > ', lastTimeLongAccel))
    odbcClose(cn)
    print(paste(nrow(gps.records.new), '/', nrow(accelerometer.records.new),
          '/', nrow(orientation.records.new),
          'new gps/accel/orientation records'))

    gps.records <- rbind(gps.records, gps.records.new)
    accelerometer.records <- rbind(accelerometer.records,
      accelerometer.records.new)
    orientation.records <- rbind(orientation.records,
      orientation.records.new)

    gps.n <- nrow(gps.records)
    if (gps.n > max.records)
      gps.records <- gps.records[(gps.n-max.records):gps.n,]

    accelerometer.n <- nrow(accelerometer.records)
    if (accelerometer.n > max.records)
      accelerometer.records <-
        accelerometer.records[(accelerometer.n-max.records):accelerometer.n,]

    orientation.n <- nrow(orientation.records)
    if (orientation.n > max.records)
      orientation.records <-
        orientation.records[(orientation.n-max.records):orientation.n,]
    
    if (gps.n > 0 || accelerometer.n > 0 || orientation.n > 0) {
      if (gps.n > 0) {
        lastTimeLong <- max(gps.records$time)
        lastTimeString <- as.POSIXct(lastTimeLong/1000, origin='1970-01-01')
      }

      if (accelerometer.n > 0) {
        lastTimeLongAccel <- max(accelerometer.records$time)
        lastTimeStringAccel <- as.POSIXct(lastTimeLongAccel/1000, origin='1970-01-01')
      }

      if (orientation.n > 0) {
        lastTimeLongOrient <- max(orientation.records$time)
        lastTimeStringOrient <- as.POSIXct(lastTimeLongOrient/1000, origin='1970-01-01')
      }
      opar <- par(mfrow=c(1,3))
      with(gps.records, plot(longitude, latitude, type='l',
        main=lastTimeString))
      with(gps.records, points(longitude, latitude))

      with(accelerometer.records, plot(time, x, type='l',
        main=lastTimeStringAccel, ylim=c(-10,10)))
      with(accelerometer.records, lines(time, y, col='green'))
      with(accelerometer.records, lines(time, z, col='red'))

      with(orientation.records, plot(time, azimuth, type='l',
        main=lastTimeStringAccel, ylim=c(-pi,pi)))
      with(orientation.records, lines(time, pitch, col='green'))
      with(orientation.records, lines(time, roll, col='red'))

      par(opar)
    }

    Sys.sleep(2.5)
  }
}

library(fields)

time.start <- as.numeric(as.POSIXct('2012-05-31 12:01:38'))*1000
time.end <-   as.numeric(as.POSIXct('2012-05-31 12:16:58'))*1000

#time.start <- as.numeric(as.POSIXct('2012-05-31 12:39:00'))*1000
#time.end <-   as.numeric(as.POSIXct('2012-05-31 12:54:00'))*1000

gps.bind.speed <- function(gps) {
  do.call(rbind, by(gps, gsp$device_id, function(gps.d) {
    gps.d <- gps.d[order(gps.d$time),]
    gps.d$step.dist <- NA
    gps.d$step.time <- NA
    n <- nrow(gps.d)
    for (i in 2:n) {
      pos.1 <- with(gps.d[i-1,], matrix(c(longitude,latitude), 1, 2))
      pos.2 <- with(gps.d[i,], matrix(c(longitude,latitude), 1, 2))
      gps.d$step.dist[i] <- rdist.earth(pos.1, pos.2, miles=FALSE)*1000
      gps.d$step.time[i] <- gps.d$time[i] - gps.d$time[i-1]
    }
    gps.d$speed <- with(gps.d, step.dist / step.time)
    gps.d
  }))
}

cn <- odbcConnect('track_jd')
gps <- sqlQuery(cn, 'SELECT * FROM gps_records')
gps.bind.speed(gps)

