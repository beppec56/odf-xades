#!/bin/bash
#
# to install language package

# install the binaries in the standard directory

#legge la target directory, che è in SRCDIR

export BUILDDATE="090112"
export OOOVER="3"
export OOOPACKVER=$OOOVER".1"
export OOOREL=".1"

export SRCDIR="/home/beppe/ooo-3.x-installs/"$OOOPACKVER$OOOREL"-pristine"
export OOOPCKVERS="-19"
OOOVERSION=$OOOPACKVER$OOOREL$OOOPCKVERS"_i386.deb"

export TARFILENAME="OOo_3.1.1_LinuxIntel_langpack_en-US_deb.tar.gz"

echo "$TARFILENAME"
echo "$OOOVERSION"

export ROOTDIR=`pwd`
export TARGETINST=`pwd`-bin
export DUMMYDIR=$ROOTDIR"/dummy-dir"
export PROGDIR="opt"

pushd .

#cd $OOBUILDDIR

#pwd

#. ./LinuxX86Env.Set.sh


#extract languages (only en_US)
cd $SRCDIR

tar xvf $TARFILENAME

export SRCUNTAR=`pwd`/OOO310_m19_native_packed-1_en-US.9420
export SRCDEBS=$SRCUNTAR/DEBS

popd
echo "$ROOTDIR $TARGETINST $SRCDEBS $SRCUNTAR"

cd $ROOTDIR

rm -rf $DUMMYDIR
mkdir $DUMMYDIR

for AFILE in `ls $SRCDEBS/*deb`
do
    COMMAND="dpkg-deb -x $AFILE $DUMMYDIR"
    echo $COMMAND
    $COMMAND
done

#rm -rf $TARGETINST
#mkdir $TARGETINST

pushd .

cd $DUMMYDIR
cd $PROGDIR
for TOMOVE in `ls`
do
    COMMAND="cp -Rpvrf $TOMOVE $TARGETINST"
    echo $COMMAND
    $COMMAND
done

cd $TARGETINST"/openoffice.org3/program"; chmod +w *
cd $TARGETINST"/openoffice.org/basis3.1/program"; chmod +w *

popd

rm -rf $DUMMYDIR
rm -rf $SRCUNTAR

#now link it in
#disabled, this  does no longer work after m233
#COMMAND=$SOLARENV"/bin/linkoo "$TARGETINST" "$OOBUILDDIR" --dry-run"
#COMMAND=$SOLARENV"/bin/linkoo "$TARGETINST" "$SRC_ROOT
#echo $COMMAND
# $COMMAND

