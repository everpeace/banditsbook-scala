#! /usr/bin/Rscript
#
# The MIT License (MIT)
#
# Copyright (c) 2016 Shingo Omura
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

initial.options <- commandArgs(trailingOnly = FALSE)
file.arg.name <- "--file="
script.name <- sub(file.arg.name, "", initial.options[grep(file.arg.name, initial.options)])
script.basename <- dirname(script.name)

source(file.path(script.basename, "plot_cumulative_rewards.r"))
source(file.path(script.basename, "read_data.r"))
library(stringr)

# read data
datafile_path <- file.path(script.basename, "..", "test-standard-softmax-results.csv")
df <- read_data_with_hyper_param(datafile_path)

# plot
g <- plot_cumulative_rewards_with_hyper_param(df)
g <- g + geom_line(aes(y=step+1.0), linetype="dotted")
g <- g + ggtitle("Cumulative Rewards of Standard Softmax for each τ.  (note: dashed line indicates optimal behavior)")
g <- g + theme(plot.title = element_text(hjust = 0))

# print(g)
ggsave(file = str_replace(datafile_path, ".csv", ".png"), plot = g, width = 15, height = 7)
