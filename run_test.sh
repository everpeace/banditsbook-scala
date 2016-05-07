#!/usr/bin/env bash
#
# The MIT License (MIT)
# 
# Copyright (c) 2016 Shingo Omura
# # 
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
# # 
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
# # 
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

SBT=$(which sbt)
R=$(which r)
OPEN=$(which open)
PLOT_TOOL_DIR="plot"
OUTPUT_DIR=${RUN_TEST_OUTPUT_DIR:-output}

available_algs=(standard-epsilon-greedy standard-softmax exp3 hedge ucb1)

function show_usage(){
    cat <<EOS
usage: ./run_test.sh  alg1 alg2 ...
available algorithm name:
    all ${available_algs[@]}
output dir canbe specified via RUN_TEST_OUTPUT_DIR
EOS
}

if [ $# -eq 0 ]; then
    show_usage
    exit 1;
else
    for alg in $@
    do
        if [ "${alg}" = "all" ]
        then
            target=${available_algs[@]}
            break
        else
            available=false
            for avalg in ${available_algs[@]}
            do
                if [ "${avalg}" = "${alg}" ]
                then
                    available=true
                    break
                fi
            done
            if ${available}
            then
                target+=${alg}
            else
              echo "${alg} is not available."
              show_usage
              exit 1
            fi
        fi
    done
fi
echo "target = ${target[@]}"
cat <<EOS
===============================
Bandits Algorithm Simulations
===============================
EOS

set -e

for alg in ${target[@]}
do
  case ${alg} in
    "standard-epsilon-greedy" )
        main_class=com.github.everpeace.banditsbook.algorithm.epsilon_greedy.TestStandard
        ;;
    "standard-softmax" )
        main_class=com.github.everpeace.banditsbook.algorithm.softmax.TestStandard
        ;;
    "exp3" )
        main_class=com.github.everpeace.banditsbook.algorithm.exp3.TestExp3
        ;;
    "hedge" )
        main_class=com.github.everpeace.banditsbook.algorithm.hedge.TestHedge
        ;;
    "ucb1" )
        main_class=com.github.everpeace.banditsbook.algorithm.ucb.TestUCB1
        ;;
    esac
    ${SBT} -DOUTPUT_DIR=${OUTPUT_DIR} "run-main ${main_class}"
done

cat <<EOS
-----------------------------------------------------------
Plotting simulation results (regarding cumulative rewards)
-----------------------------------------------------------
EOS
R_OPTS="--quiet --no-save"
for alg in ${target[@]}
do
    case ${alg} in
    "standard-epsilon-greedy" )
        r_script=plot_standard_epsilon_greedy.r
        ;;
    "standard-softmax" )
        r_script=plot_standard_softmax.r
        ;;
    "exp3" )
        r_script=plot_exp3.r
        ;;
    "hedge" )
        r_script=plot_hedge.r
        ;;
    "ucb1" )
        r_script=plot_ucb1.r
        ;;
    esac
    ${R} ${R_OPTS} --file=${PLOT_TOOL_DIR}/${r_script} --outdir=${OUTPUT_DIR}
done

if [ ${#target[@]} -ne 0 ]
then
    if [ "z$OPEN" = "z" ]; then
        echo "please open *.png files manually to see simulation results!"
    else
        for alg in ${target[@]}
        do
            ${OPEN} "${OUTPUT_DIR}/test-${alg}-results.png"
        done
    fi
fi
