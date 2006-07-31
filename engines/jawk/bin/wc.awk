
## Example - wc.awk
## Similar to the Unix wc(1) command.  Byte counts, however,
## are estimates since we have no (easy) way to deduce the
## concrete line (record) separators, particularly for
## ambiguous newlines (\r\n or \n) and for the terminating
## record separator.

import java.io.*;

BEGIN {
  SEPARATOR_COUNT = length(System.getProperty("line.separator"))
  IFS = "[ \t,.?;:]+"
  for (i=0;i<ARGC;i++) {
	arg = ARGV[i]
	if (arg ~ /^-/) {
		for (j=1;j<length(arg);j++) {
			c = arg.charAt(j);
			if (false) ;
			else if (c == 'c') choices[bytes()] = choice_list[++choice_idx] = bytes()
			else if (c == 'l') choices["newlines"] = choice_list[++choice_idx] = "newlines"
			else if (c == 'm') choices[chars()] = choice_list[++choice_idx] = chars()
			else if (c == 'w') choices["words"] = choice_list[++choice_idx] = "words"
			else
				throw new IllegalArgumentException("Invalid switch flag: " c);
		}
		delete ARGV[i]
	} else
		break
  }
  if (! choice_idx) {
	## -lwc by default
	choices["newlines"] = choice_list[++choice_idx] = "newlines"
	choices["words"] = choice_list[++choice_idx] = "words"
	choices[bytes()] = choice_list[++choice_idx] = "bytes"
  }
  if (i<ARGC-1) multiple = 1
  init=1
}
END {
  summarize(filename)
  if (multiple)
	postTotals()
}
function bytes() {
  if (bw == "chars")
	throw new IllegalArgumentException("Cannot assign both -c and -m")
  return bw = "bytes"
}
function chars() {
  if (bw == "bytes")
	throw new IllegalArgumentException("Cannot assign both -c and -m")
  return bw = "chars"
}

! init && filename != FILENAME {
  summarize(filename)
}

{
  init = 0 ; filename = FILENAME

  if (choices["newlines"]) { totals["newlines"]++ ; results["newlines"]++ }
  if (choices["chars"]) { totals["chars"]+=length($0); results["chars"]+=length($0) }
  if (choices["bytes"]) {
	val = $0.toString().getBytes().length+SEPARATOR_COUNT
	totals["bytes"]+=val; results["bytes"]+=val
  }
  if (choices["words"]) { totals["words"]+=NF; results["words"]+=NF }
}

function summarize(filename,	i) {
  printf("%-12s", filename)
  for (i=1;i<=choice_idx;i++) {
	printf("\t%6d", int(results[choice_list[i]]))
	#printf("\t%s", "" (results[choice_list[i]]))
	delete results[choice_list[i]]
  }
  printf("\n")
}

function postTotals(	i) {
  printf("\n")
  printf("%-12s", "totals")
  for (i=1;i<=choice_idx;i++) {
	printf("\t%6d", int(totals[choice_list[i]]))
	delete totals[choice_list[i]]
  }
  printf("\n")
}
