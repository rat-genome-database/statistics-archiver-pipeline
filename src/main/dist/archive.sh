. /etc/profile

APPDIR=/home/rgddata/pipelines/statistics-archiver-pipeline
SERVER=`hostname -s | tr '[a-z]' '[A-Z]'`
ELIST=mtutaj@mcw.edu
if [ "$SERVER" == "REED" ]; then
    ELIST="jdepons@mcw.edu mtutaj@mcw.edu"
fi

cd $APPDIR
$APPDIR/_run.sh > archive.log
mailx -s "[$SERVER] Stats Archive Complete" $ELIST < archive.log

