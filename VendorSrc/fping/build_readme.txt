���\�[�X�R�[�h����
http://slackbuilds.org/repository/13.37/network/fping6/
fping_2.4b2-to-ipv6.orig.tar.gz

http://fping.sourceforge.net/
fping.tar.gz (�𓀂����fping_2.4b2�������)

���p�b�`�𓖂Ă�
fping.c��1600�s�ڂ�����

this_reply = timeval_diff( &current_time, &sent_time );
if (this_reply > timeout) {   //���ǋL
    return num_jobs;          //���ǋL
}                             //���ǋL
if( this_reply > max_reply ) max_reply = this_reply;

���r���h����
RHEL5_32��RHEL5_64�Ńr���h����B

# tar xzvf fping.tar.gz
# cd fping-2.4b2
# ./configure
# make
fping���ł���B

# tar xzvf 
# cd fping-2.4b2_to-ipv6
# ./configure
# make
fping���ł���B

fping�����L�̂悤�Ƀ��l�[������B
fping_x86 (32bit ipv4)
fping6_x86 (32bit ipv6)
fping_x86_64 (64bit ipv4)
fping6_x86_64 (64bit ipv6)

���m�F1
4��fping�̈ˑ����C�u�������m�F����B
32bit
# ldd fping
    linux-gate.so.1 =>  (0x00887000)
    libc.so.6 => /lib/libc.so.6 (0x00691000)
    /lib/ld-linux.so.2 (0x00673000)
# ldd fping6
    linux-gate.so.1 =>  (0x00947000)
    libc.so.6 => /lib/libc.so.6 (0x00691000)
    /lib/ld-linux.so.2 (0x00673000)

64bit
# ldd fping
    linux-vdso.so.1 =>  (0x00007fffe89ff000)
    libc.so.6 => /lib64/libc.so.6 (0x00000035cd000000)
    /lib64/ld-linux-x86-64.so.2 (0x00000035cc800000)
# ldd fping6
    linux-vdso.so.1 =>  (0x00007fffa43fd000)
    libc.so.6 => /lib64/libc.so.6 (0x00000038bd200000)
    /lib64/ld-linux-x86-64.so.2 (0x00000038bce00000)

���ʂ̒���16�i�����͊��ɂ���ĕς��B

���m�F2
32bit
# ./fping_x86 172.26.98.xxx
����
# ./fping_x86_64 172.26.98.xxx
���s

64bit
# ./fping_x86 172.26.98.xxx
���s
# ./fping_x86_64 172.26.98.xxx
����

���z�u
fping_x86, fping6_x86, fping_x86_64, fping6_x86_64��
HinemosPackageBuilder/hinemos_manager/common_rhel/hinemos/sbin
�ɔz�u����B
VendorSrc�ɔz�u���Ȃ��悤�ɒ��ӂ��Ă��������B
