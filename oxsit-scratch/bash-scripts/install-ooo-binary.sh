#!/bin/bash


# install the binaries in the standard directory
# this script should be run with the corresponding
# ooo distribution available

#legge la target directory, che è in SRCDIR
#. bin/setup
export OOOVER="3"
export OOOPACKVER=$OOOVER".1"
export OOOREL=".1"
export SRCDIR="/home/beppe/ooo-3.x-installs/"$OOOPACKVER$OOOREL"-pristine"

export OOOPCKVERS="-19"
export ML10N="it"
export DICTSET="dict-de dict-en dict-fr dict-it"
#export DICTSET="dict-en dict-fr"

export ROOTDIR=`pwd`
export TARGETINST=`pwd`-bin
export DUMMYDIR=$ROOTDIR"/dummy-dir"
export PROGDIR="opt"

pushd .

# expand the source installation tarball
cd /home/beppe/ooo-3.x-installs/3.1.1-pristine
export TARFILENAME="OOo_3.1.1_LinuxIntel_install_it_deb.tar.gz"

tar xvf $TARFILENAME

export SRCUNTAR=`pwd`/OOO310_m19_native_packed-1_$ML10N.9420

export SRCDEBS=$SRCUNTAR/DEBS/

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

rm -rf $TARGETINST
mkdir $TARGETINST

pushd .

cd $DUMMYDIR
cd $PROGDIR
for TOMOVE in `ls`
do
    COMMAND="mv $TOMOVE $TARGETINST"
    echo $COMMAND
    $COMMAND
done

cd $TARGETINST"/openoffice.org3/program"; chmod +w *
cd $TARGETINST"/openoffice.org/basis3.1/program"; chmod +w *

popd

rm -rf $DUMMYDIR
rm -rf $SRCUNTAR
