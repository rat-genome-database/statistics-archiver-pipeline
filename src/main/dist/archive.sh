. /etc/profile

APPDIR=/home/rgddata/pipelines/StatisticsArchiver
SERVER=`hostname -s | tr '[a-z]' '[A-Z]'`
ELIST=mtutaj@mcw.edu
if [ "$SERVER" == "REED" ]; then
    ELIST="jdepons@mcw.edu,szacher@mcw.edu,mtutaj@mcw.edu"
fi

cd $APPDIR
$APPDIR/run.sh > archive.log
mailx -s "[$SERVER] Stats Archive Complete" $ELIST < archive.log

