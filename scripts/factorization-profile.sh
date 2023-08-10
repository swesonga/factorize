timestamp=`date +%Y-%m-%d_%H%M%S`
duration=5
profiler_path=~/java/binaries/async-profiler/async-profiler-2.9-linux-x64
profilerOutDir=~/

$profiler_path/profiler.sh -e wall -i 5ms -d $duration -o flat        -f "${profilerOutDir}Factorize-wall-flat-${timestamp}.txt" jps
$profiler_path/profiler.sh -e wall -i 5ms -d $duration -o traces      -f "${profilerOutDir}Factorize-wall-traces-${timestamp}.txt" jps
$profiler_path/profiler.sh -e wall -i 5ms -d $duration -o flat,traces -f "${profilerOutDir}Factorize-wall-flat+traces-${timestamp}.txt" jps
$profiler_path/profiler.sh -e wall -i 5ms -d $duration -o tree        -f "${profilerOutDir}Factorize-wall-tree-${timestamp}.html" jps
$profiler_path/profiler.sh -e wall -i 5ms -d $duration -o flamegraph  -f "${profilerOutDir}Factorize-wall-flamegraph-${timestamp}.html" jps

$profiler_path/profiler.sh -e cpu -i 5ms -d $duration -o flat        -f "${profilerOutDir}Factorize-cpu-flat-${timestamp}.txt" jps
$profiler_path/profiler.sh -e cpu -i 5ms -d $duration -o traces      -f "${profilerOutDir}Factorize-cpu-traces-${timestamp}.txt" jps
$profiler_path/profiler.sh -e cpu -i 5ms -d $duration -o flat,traces -f "${profilerOutDir}Factorize-cpu-flat+traces-${timestamp}.txt" jps
$profiler_path/profiler.sh -e cpu -i 5ms -d $duration -o tree        -f "${profilerOutDir}Factorize-cpu-tree-${timestamp}.html" jps

$profiler_path/profiler.sh -e lock -i 5ms -d $duration -o flat        -f "${profilerOutDir}Factorize-lock-flat-${timestamp}.txt" jps
$profiler_path/profiler.sh -e lock -i 5ms -d $duration -o traces      -f "${profilerOutDir}Factorize-lock-traces-${timestamp}.txt" jps
$profiler_path/profiler.sh -e lock -i 5ms -d $duration -o flat,traces -f "${profilerOutDir}Factorize-lock-flat+traces-${timestamp}.txt" jps
$profiler_path/profiler.sh -e lock -i 5ms -d $duration -o tree        -f "${profilerOutDir}Factorize-lock-tree-${timestamp}.html" jps
$profiler_path/profiler.sh -e lock -i 5ms -d $duration -o flamegraph  -f "${profilerOutDir}Factorize-lock-flamegraph-${timestamp}.html" jps
