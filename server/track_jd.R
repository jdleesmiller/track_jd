#
# Attempt at simple server in R... this does seem to mostly work, but I
# abandoned this in favour of the Ruby server when I had to work on it on a
# train without Google.
#
# source('track_jd_server.R'); server <- track_jd.run()
# source('track_jd_server.R'); server$stop(); server <- track_jd.run()
#

library(Rook)

track_jd.app <- function(env) {
  req <- Request$new(env)
  res <- Response$new()
  print(req$POST())
  res$write('<html><body>hi</body></html>')
  res$finish()
}

#
# Start server and run it.
#
track_jd.run <- function() {
  server <- Rhttpd$new()
  server$add(name='track_jd', app=Rook::URLMap$new('/log' = track_jd.app))
  server$start(listen='0.0.0.0', port=3666)
  server
}

