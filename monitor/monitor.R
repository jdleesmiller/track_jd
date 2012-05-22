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
  while (TRUE) {
    cn <- odbcConnect('track_jd')
    gps.records.new <- sqlQuery(cn, paste(
      'SELECT * FROM gps_records WHERE time > ', lastTimeLong))
    odbcClose(cn)
    print(paste(nrow(gps.records.new), 'new records'))

    gps.records <- rbind(gps.records, gps.records.new)
    n <- nrow(gps.records)
    if (n > max.records)
      gps.records <- gps.records[(n-max.records):n,]
    
    if (n > 0) {
      lastTimeLong <- max(gps.records$time)
      lastTimeString <- as.POSIXct(lastTimeLong/1000, origin='1970-01-01')

      with(gps.records, plot(longitude, latitude, type='l',
        main=lastTimeString))
      with(gps.records, points(longitude, latitude))
        
    }

    Sys.sleep(2.5)
  }
}
