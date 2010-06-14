
import string;
import sys;
import os;


def run(cut_off, proc, size):
	if (size == 0):
		mask = "Run:SizeA";
	else:
		mask = "Run:SizeB";	
	os.system("make test ARG1=%s ARG2=%s ARG3=%s | grep \"%s\" 1> result.txt" % (cut_off, proc, size, mask));
	rf = file("result.txt");
	str_list = rf.readlines();
	
	for i in range(0, len(str_list)):
		ret = float(str_list[i][30:str_list[i].find("(s)")]);

	for j in range(0, 4):
		os.system("make test ARG1=%s ARG2=%s ARG3=%s | grep \"%s\" 1> result.txt" % (cut_off, proc, size, mask));
		rf = file("result.txt");
		str_list = rf.readlines();
		for k in range(0, len(str_list)):
			ret = min(ret, float(str_list[k][30:str_list[k].find("(s)")]));

	print(ret);


cutoff_start = 4;
cutoff_end = 14;
proc_list = [2,3,4,7,12,17,22];
size = 1;

for m in range(cutoff_start, cutoff_end):
	cutoff_val = pow(2, m);
	print("cutoff value = %d" % cutoff_val);
	for n in range(0, len(proc_list)):
		run(cutoff_val, proc_list[n], size);
	
