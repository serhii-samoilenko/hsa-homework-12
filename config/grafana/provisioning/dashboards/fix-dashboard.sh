#!/bin/bash
die() {
	echo "$*" 1>&2
	exit 1
}

transform() {
	local replacement=$1
	jq --arg replacement $replacement 'del(.__inputs)
		|if .panels != null then .panels=(.panels|map(.datasource=$replacement)) else . end
		|if .rows != null then .rows=(.rows|map(.panels=(.panels|map(.datasource=$replacement))))else . end
		|.templating.list=(.templating.list|map(.datasource=$replacement))
		|.annotations.list=(.annotations.list|map(.datasource=$replacement))'
}

r=$1
test -n "$r" || die "$0 {replacement} [{file}]"

f=$2
if test -n "$f" -a "$f" != "-"; then
	echo "$f" 1>&2
	transform $r < "$f" > "$f.new" && mv "$f.new" "$f"
else
	transform $r
fi
